---
name: angular-feature
description: Implements Angular 20 frontend features, components, services, and models for the Pokemon TCG project. Use when creating or editing files under FE/src/app/, including components, services, models, pipes, directives, and routes. Triggers on Angular, component, service, signal, frontend, FE, page, feature.
---

# Angular Feature Skill - Pokemon TCG

You are implementing the Angular 20 frontend for a Pokemon Trading Card Game application. Follow these conventions strictly as defined in `FE/AGENTS.md`.

## Project Setup

- **Angular 20** with standalone components (NO NgModules)
- **TypeScript 5.9** with strict mode
- Frontend root: `FE/`
- Source: `FE/src/app/`
- Style: CSS (not SCSS/SASS)
- Convention: short filenames (`.ts`, `.html`, `.css`) — NOT `.component.ts`
- Package manager: npm

## Architecture: Feature-Driven

```
src/app/
├── app.ts / app.html / app.css
├── app.routes.ts          (lazy-loaded routes)
├── app.config.ts           (providers)
├── core/
│   ├── constants/          (card-types, energy-types, game-phases, special-conditions)
│   ├── guards/             (auth guards, game guards)
│   ├── interceptors/       (error.interceptor.ts)
│   └── services/           (websocket.service.ts)
├── features/
│   ├── deck-builder/
│   │   ├── components/
│   │   │   ├── card-catalog/
│   │   │   ├── card-detail-modal/
│   │   │   ├── deck-editor/
│   │   │   ├── deck-list/
│   │   │   └── deck-summary/
│   │   ├── pages/
│   │   │   └── deck-builder-page/
│   │   └── services/
│   │       ├── card.service.ts
│   │       └── deck.service.ts
│   ├── game/
│   │   ├── components/
│   │   │   ├── active-pokemon/
│   │   │   ├── bench-area/
│   │   │   ├── action-panel/
│   │   │   ├── attack-selector/
│   │   │   ├── deck-pile/
│   │   │   ├── discard-pile/
│   │   │   ├── energy-attachment/
│   │   │   ├── evolution-selector/
│   │   │   ├── game-board/     (MUST be created from scratch)
│   │   │   ├── game-over/
│   │   │   ├── hand-area/
│   │   │   ├── notification-toast/
│   │   │   ├── opponent-area/
│   │   │   ├── player-area/
│   │   │   ├── prize-cards/
│   │   │   ├── setup-phase/
│   │   │   └── stadium-area/
│   │   ├── pages/
│   │   │   └── game-page/
│   │   └── services/
│   │       ├── game-state.service.ts
│   │       └── game-websocket.service.ts
│   ├── lobby/
│   │   ├── components/
│   │   │   ├── create-match-dialog/
│   │   │   └── match-list/
│   │   ├── pages/
│   │   │   └── lobby-page/
│   │   └── services/
│   │       └── lobby.service.ts
│   └── pokedex/
│       └── pages/
│           └── pokedex-page/
└── shared/
    ├── components/        (card, card-stack, damage-counter, energy-badge, game-log, status-indicator)
    ├── directives/        (draggable.directive.ts)
    ├── models/             (card.model.ts, deck.model.ts, game.model.ts, match.model.ts)
    ├── pipes/             (energy-type.pipe.ts, hp-display.pipe.ts)
    └── ui/                (button, loading-spinner, modal)
```

## MANDATORY Coding Conventions

### Components

```typescript
@Component({
  selector: 'app-example',
  imports: [CommonModule],
  templateUrl: './example.html',
  styleUrl: './example.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ExampleComponent {
  // DO NOT add "standalone: true" — it's the default in Angular 20
}
```

### Signals (State Management)

Always use Angular signals for state:

```typescript
export class SomeService {
  private readonly items = signal<Item[]>([]);
  private readonly loading = signal(false);
  private readonly error = signal<string | null>(null);

  readonly itemsList = computed(() => this.items());
  readonly isLoading = computed(() => this.loading());
}
```

### Component I/O

Use `input()` and `output()` functions, NOT decorators:

```typescript
export class CardComponent {
  card = input.required<CardModel>();
  isSelected = input<boolean>(false);
  cardClicked = output<CardModel>();
}
```

### Templates

Use native Angular 20 control flow, NOT `*ngIf`/`*ngFor`/`*ngSwitch`:

```html
@if (cards().length > 0) {
  @for (card of cards(); track card.id) {
    <app-card [card]="card" (cardClicked)="onSelect($event)" />
  } @empty {
    <p>No cards found</p>
  }
}
```

### Services

Use `providedIn: 'root'` and `inject()` function:

```typescript
@Injectable({ providedIn: 'root' })
export class CardService {
  private readonly http = inject(HttpClient);
}
```

### Styling

- DO NOT use `ngClass` — use `class` bindings: `[class.active]="isSelected()"`
- DO NOT use `ngStyle` — use `style` bindings: `[style.color]="color()"`
- DO NOT use `@HostBinding`/`@HostListener` — use `host` object in `@Component`

```typescript
@Component({
  // ...
  host: {
    '[class.dragging]': 'isDragging()',
    '(dragstart)': 'onDragStart($event)'
  }
})
```

## Routing Configuration

Routes in `app.routes.ts` use lazy loading:

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
  { path: '', redirectTo: 'pokedex', pathMatch: 'full' },
  { path: '**', redirectTo: 'pokedex' }
];
```

## App Config

Needs `provideHttpClient()` added:

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

## Model Definitions

All model files exist as stubs. Implement them based on the backend DTOs:

### card.model.ts

```typescript
export interface CardModel {
  id: string;
  externalId: string;
  name: string;
  hp: number | null;
  types: string[];
  stage: string;
  attacks: AttackModel[];
  abilities: AbilityModel[];
  weakness: string | null;
  resistance: string | null;
  retreatCost: number;
  imageUrl: string;
  rarity: string;
  cardNumber: string;
  setId: string;
}

export interface AttackModel {
  name: string;
  cost: string[];
  damage: number;
  effect?: string;
}

export interface AbilityModel {
  name: string;
  effect: string;
}

export interface CardFilterModel {
  name?: string;
  type?: string;
  stage?: string;
  setId?: string;
  page?: number;
  size?: number;
}
```

### deck.model.ts

```typescript
export interface DeckModel {
  id: string;
  name: string;
  creatorId: string;
  cards: DeckCardModel[];
  totalCards: number;
  isValid: boolean;
  validationErrors: string[];
}

export interface DeckCardModel {
  cardId: string;
  card?: CardModel;
  quantity: number;
}

export interface CreateDeckModel {
  name: string;
  cards: { cardId: string; quantity: number }[];
}

export interface DeckValidationResultModel {
  isValid: boolean;
  errors: string[];
  totalCards: number;
  basicPokemonCount: number;
  aceSpecCount: number;
  cardNameCounts: Record<string, number>;
}
```

### game.model.ts

```typescript
export interface GameBoardModel {
  player1Board: PlayerBoardModel;
  player2Board: PlayerBoardModel;
  sharedStadium: TrainerCardModel | null;
}

export interface PlayerBoardModel {
  playerId: string;
  activePokemon: PokemonInPlayModel | null;
  bench: PokemonInPlayModel[];
  hand: GameCardModel[];
  deckCount: number;
  discardPile: GameCardModel[];
  prizeCardsCount: number;
  prizeCardsTaken: number;
}

export interface PokemonInPlayModel {
  card: PokemonCardModel;
  currentHp: number;
  damageCounters: number;
  attachedEnergy: EnergyCardModel[];
  tool: TrainerCardModel | null;
  specialConditions: string[];
  isActive: boolean;
  turnsInPlay: number;
}

export interface TurnContextModel {
  currentPlayerId: string;
  phase: string;
  isFirstTurn: boolean;
  hasDrawn: boolean;
  hasPlayedTrainer: boolean;
  hasEvolved: boolean;
  hasAttachedEnergy: boolean;
  hasUsedAbility: boolean;
  hasRetreated: boolean;
}

export interface MatchSnapshotModel {
  matchId: string;
  phase: string;
  board: GameBoardModel;
  turn: TurnContextModel;
  eventLog: GameEventModel[];
  winnerId: string | null;
}

export interface GameEventModel {
  type: string;
  playerId: string;
  description: string;
  timestamp: string;
}
```

### match.model.ts

```typescript
export interface MatchModel {
  id: string;
  player1Id: string;
  player2Id: string | null;
  status: string;
  winnerId: string | null;
  createdAt: string;
}

export interface CreateMatchModel {
  player1Id: string;
  player1DeckId: string;
}

export interface MatchStateModel {
  matchId: string;
  phase: string;
  currentPlayerId: string;
  player1: PlayerStateModel;
  player2: PlayerStateModel;
  recentEvents: GameEventModel[];
}

export interface PlayerStateModel {
  playerId: string;
  activePokemon: PokemonInPlayModel | null;
  bench: PokemonInPlayModel[];
  handSize: number;
  deckSize: number;
  prizeCardsRemaining: number;
}
```

## WebSocket Messages

```typescript
export interface GameActionMessage {
  matchId: string;
  type: GameActionType;
  payload: Record<string, unknown>;
  timestamp: string;
}

export type GameActionType =
  | 'DRAW_CARD'
  | 'PLACE_POKEMON'
  | 'ATTACH_ENERGY'
  | 'PLAY_TRAINER'
  | 'EVOLVE'
  | 'RETREAT'
  | 'ATTACK'
  | 'USE_ABILITY'
  | 'SURRENDER'
  | 'READY'
  | 'SET_ACTIVE';

export interface GameStateUpdateMessage {
  type: 'STATE_UPDATE' | 'EVENT' | 'ERROR' | 'NOTIFICATION';
  state?: MatchSnapshotModel;
  event?: GameEventModel;
  error?: string;
  notification?: string;
}
```

## Checklist

When implementing FE components:
- [ ] File uses short naming convention: `card.ts`, `card.html`, `card.css` (NOT `card.component.ts`)
- [ ] `changeDetection: ChangeDetectionStrategy.OnPush` is set
- [ ] NO `standalone: true` in decorator (Angular 20 default)
- [ ] Uses `input()` / `output()` functions, NOT `@Input()` / `@Output()` decorators
- [ ] Uses `computed()` for derived state
- [ ] Uses `signal()` for component state
- [ ] Uses `inject()` for dependency injection, NOT constructor injection
- [ ] Uses `@if` / `@for` / `@switch` in templates, NOT `*ngIf` / `*ngFor`
- [ ] Uses `providedIn: 'root'` in services
- [ ] Uses `host` object for host bindings, NOT `@HostBinding`/`@HostListener`
- [ ] Uses `[class.xxx]` bindings, NOT `ngClass`
- [ ] Uses `[style.xxx]` bindings, NOT `ngStyle`
- [ ] Lazy-loaded routes with `loadComponent`
- [ ] Service uses `HttpClient` for REST and `WebsocketService` for game actions