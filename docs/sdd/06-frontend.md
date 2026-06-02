# 06 - Frontend

## Estándares de Código (AGENTS.md)

El proyecto sigue las mejores prácticas de Angular definidas en `FE/AGENTS.md`:

- **Standalone components** (sin NgModules, `standalone: true` NO se setea explícitamente)
- **Signals** para gestión de estado local
- **`input()` / `output()`** en lugar de decoradores
- **`computed()`** para estado derivado
- **OnPush** change detection en todos los componentes
- **Control flow nativo** (`@if`, `@for`, `@switch`) en lugar de `*ngIf`/`*ngFor`
- **Reactive forms** preferidos sobre template-driven
- **`providedIn: 'root'`** para servicios singleton
- **`inject()`** en lugar de inyección por constructor
- **Sin `ngClass`/`ngStyle`** - usar bindings de `class`/`style`
- **Sin `@HostBinding`/`@HostListener`** - usar objeto `host` en el decorator

## Features

### Pokedex (`/pokedex`)

Consulta y visualización de cartas del set xy1.

| Componente | Responsabilidad |
|---|---|
| `PokedexPage` | Página principal con grid de cartas |
| `CardComponent` (shared) | Visualización individual de carta |

**Servicios:** `CardService` (HTTP)

**Estado:** Lista de cartas, filtros, paginación

### Deck Builder (`/deck-builder`)

Construcción y gestión de mazos con validación.

| Componente | Responsabilidad |
|---|---|
| `DeckBuilderPage` | Página principal del deck builder |
| `CardCatalogComponent` | Catálogo de cartas con filtros y búsqueda |
| `CardDetailModalComponent` | Modal con detalle de carta |
| `DeckEditorComponent` | Editor del mazo actual (agregar/quitar cartas) |
| `DeckListComponent` | Lista de mazos guardados |
| `DeckSummaryComponent` | Resumen del mazo (conteo por tipo, validación) |

**Servicios:** `CardService`, `DeckService`

**Estado:** Cartas disponibles, mazo actual, errores de validación

### Lobby (`/lobby`)

Creación y unión a partidas.

| Componente | Responsabilidad |
|---|---|
| `LobbyPage` | Página principal del lobby |
| `MatchListComponent` | Lista de partidas disponibles |
| `CreateMatchDialogComponent` | Diálogo para crear nueva partida |

**Servicios:** `LobbyService`, `WebsocketService`

**Estado:** Partidas disponibles, partida actual

### Game (`/game/:matchId`)

Tablero de juego interactivo en tiempo real.

| Componente | Responsabilidad |
|---|---|
| `GamePage` | Página principal del juego |
| `GameBoardComponent` | Contenedor principal del tablero (crear desde cero) |
| `PlayerAreaComponent` | Área del jugador local |
| `OpponentAreaComponent` | Área del oponente |
| `ActivePokemonComponent` | Pokémon activo del jugador |
| `BenchAreaComponent` | Banca del jugador (hasta 5 Pokémon) |
| `HandAreaComponent` | Mano del jugador (cartas ocultas del oponente) |
| `StadiumAreaComponent` | Zona de estadio |
| `ActionPanelComponent` | Panel de acciones (botones contextuales) |
| `AttackSelectorComponent` | Selector de ataque |
| `DeckPileComponent` | Visualización del mazo (cantidad) |
| `DiscardPileComponent` | Pila de descarte |
| `PrizeCardsComponent` | Cartas de premio (cantidad restante) |
| `SetupPhaseComponent` | Fase de configuración inicial |
| `EvolutionSelectorComponent` | Selector de evolución |
| `EnergyAttachmentComponent` | UI para attachar energía |
| `GameOverComponent` | Pantalla de fin de partida |
| `NotificationToastComponent` | Notificaciones visuales |

**Servicios:** `GameStateService`, `GameWebsocketService`

**Estado:** Tablero, turno, fase, carta seleccionada, notificaciones

## Routing

```typescript
export const routes: Routes = [
  {
    path: 'pokedex',
    loadComponent: () => import('./features/pokedex/pages/pokedex-page/pokedex-page')
      .then(m => m.PokedexPage)
  },
  {
    path: 'deck-builder',
    loadComponent: () => import('./features/deck-builder/pages/deck-builder-page/deck-builder-page')
      .then(m => m.DeckBuilderPage)
  },
  {
    path: 'lobby',
    loadComponent: () => import('./features/lobby/pages/lobby-page/lobby-page')
      .then(m => m.LobbyPage)
  },
  {
    path: 'game/:matchId',
    loadComponent: () => import('./features/game/pages/game-page/game-page')
      .then(m => m.GamePage)
  },
  {
    path: '',
    redirectTo: 'pokedex',
    pathMatch: 'full'
  },
  {
    path: '**',
    redirectTo: ''
  }
];
```

## Gestión de Estado (Signals)

### GameStateService

```typescript
@Injectable({ providedIn: 'root' })
export class GameStateService {
  private board = signal<GameBoard | null>(null);
  private turn = signal<TurnContext | null>(null);
  private phase = signal<GamePhase | null>(null);
  private selectedCard = signal<GameCard | null>(null);
  private notifications = signal<Notification[]>([]);

  readonly boardState = computed(() => this.board());
  readonly currentTurn = computed(() => this.turn());
  readonly currentPhase = computed(() => this.phase());
  readonly isMyTurn = computed(() => this.turn()?.currentPlayerId === this.myPlayerId());

  updateState(snapshot: MatchSnapshot): void { ... }
  selectCard(card: GameCard | null): void { ... }
  addNotification(message: string): void { ... }
}
```

### GameWebsocketService

```typescript
@Injectable({ providedIn: 'root' })
export class GameWebsocketService {
  private connectionStatus = signal<'connected' | 'disconnected' | 'connecting'>('disconnected');
  private lastMessage = signal<GameStateUpdateMessage | null>(null);

  readonly status = computed(() => this.connectionStatus());

  connect(matchId: string): void { ... }
  disconnect(): void { ... }
  sendAction(action: GameActionMessage): void { ... }
}
```

### CardService

```typescript
@Injectable({ providedIn: 'root' })
export class CardService {
  private cards = signal<CardModel[]>([]);
  private loading = signal(false);
  private filters = signal<CardFilterDTO>({});

  readonly filteredCards = computed(() => { ... });

  loadCards(filters?: CardFilterDTO): void { ... }
  searchCards(name: string): void { ... }
  syncCards(): void { ... }
}
```

### DeckService

```typescript
@Injectable({ providedIn: 'root' })
export class DeckService {
  private decks = signal<DeckModel[]>([]);
  private currentDeck = signal<DeckModel | null>(null);
  private validation = signal<DeckValidationResultDTO | null>(null);

  readonly isValid = computed(() => this.validation()?.isValid ?? false);
  readonly validationErrors = computed(() => this.validation()?.errors ?? []);

  loadDecks(): void { ... }
  createDeck(name: string, cards: DeckCardEntryDTO[]): void { ... }
  updateDeck(id: string, cards: DeckCardEntryDTO[]): void { ... }
  deleteDeck(id: string): void { ... }
  validateDeck(id: string): void { ... }
}
```

### LobbyService

```typescript
@Injectable({ providedIn: 'root' })
export class LobbyService {
  private matches = signal<MatchModel[]>([]);
  private currentMatch = signal<MatchModel | null>(null);

  readonly availableMatches = computed(() => this.matches().filter(m => m.status === 'WAITING'));

  loadMatches(): void { ... }
  createMatch(deckId: string): void { ... }
  joinMatch(matchId: string): void { ... }
  leaveMatch(matchId: string): void { ... }
}
```

## Componentes Compartidos

| Componente | Uso |
|---|---|
| `CardComponent` | Visualización de carta en cualquier contexto |
| `CardStackComponent` | Pila de cartas (mazo, descarte) |
| `DamageCounterComponent` | Contador de daño visual |
| `EnergyBadgeComponent` | Badge de tipo de energía |
| `GameLogComponent` | Log de acciones del juego |
| `StatusIndicatorComponent` | Indicador de condición especial |

## UI Components

| Componente | Uso |
|---|---|
| `ButtonComponent` | Botón reutilizable con variantes |
| `ModalComponent` | Modal genérico |
| `LoadingSpinnerComponent` | Indicador de carga |

## Pipes

| Pipe | Transformación |
|---|---|
| `HpDisplayPipe` | Formatea HP para visualización |
| `EnergyTypePipe` | Convierte tipo de energía a color/icono |

## Directivas

| Directiva | Propósito |
|---|---|
| `DraggableDirective` | Habilitar drag & drop en elementos |

## Configuración Requerida

### app.config.ts

El archivo actual necesita agregar `provideHttpClient()`:

```typescript
export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(withInterceptors([errorInterceptor]))
  ]
};
```
