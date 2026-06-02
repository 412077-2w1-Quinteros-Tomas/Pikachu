# 08 - Plan de Implementación

## Cronograma: 6 Semanas

> **Nota:** Este cronograma es sujeto a cambios. Se revisará semanalmente al momento de hacer merge a `develop`.

---

## Semana 1: Fundación + Sistema de Cartas + Pokedex

**Objetivo:** Cartas sincronizadas desde API, Pokedex funcional, configuración base

### Backend

| # | Tarea | Archivos | Dependencias |
|---|---|---|---|
| 1 | Configurar `.gitignore` para secrets | `.gitignore` (root + BE) | - |
| 2 | Configurar API key por variable de entorno | `application.properties` | Tarea 1 |
| 3 | Implementar BaseEntity | `entities/BaseEntity.java` | - |
| 4 | Implementar todos los enums | `enums/*.java` (8 archivos) | - |
| 5 | Implementar excepciones custom | `exceptions/*.java` (4 archivos) | - |
| 6 | Implementar GlobalExceptionHandler | `exceptions/GlobalExceptionHandler.java` | Tarea 5 |
| 7 | Implementar CorsConfig | `configs/CorsConfig.java` | - |
| 8 | Implementar CardEntity | `entities/CardEntity.java` | Tarea 3, 4 |
| 9 | Implementar CardRepository | `repositories/CardRepository.java` | Tarea 8 |
| 10 | Implementar CardDTO + CardFilterDTO | `dtos/card/*.java` | - |
| 11 | Implementar CardMapper | `mappers/CardMapper.java` | Tarea 8, 10 |
| 12 | Implementar CardService (CRUD) | `services/CardService.java` | Tarea 9, 11 |
| 13 | Implementar CardController | `controllers/CardController.java` | Tarea 12 |
| 14 | Integración API pokemontcg.io | Nuevo: `services/ExternalCardApiService.java` | Tarea 2 |
| 15 | Endpoint sync + seed data xy1 | `CardController.sync()` | Tarea 14 |

### Frontend

| # | Tarea | Archivos | Dependencias |
|---|---|---|---|
| 1 | Agregar `provideHttpClient` a app.config | `app.config.ts` | - |
| 2 | Implementar error.interceptor | `core/interceptors/error.interceptor.ts` | - |
| 3 | Implementar CardModel | `shared/models/card.model.ts` | - |
| 4 | Implementar constantes | `core/constants/*.ts` (4 archivos) | - |
| 5 | Implementar CardService HTTP | `features/deck-builder/services/card.service.ts` | Tarea 1, 2 |
| 6 | Implementar CardComponent | `shared/components/card/` | Tarea 3 |
| 7 | Implementar PokedexPage | `features/pokedex/pages/pokedex-page/` | Tarea 5, 6 |

### Entregable Semana 1

- Pokedex funcional con datos reales de cartas xy1
- API REST de cartas operativa
- Configuración base completa (CORS, exceptions, HTTP client)

---

## Semana 2: Deck Builder con Validación

**Objetivo:** CRUD completo de mazos con validación de reglas

### Backend

| # | Tarea | Archivos | Dependencias |
|---|---|---|---|
| 1 | Implementar DeckEntity | `entities/DeckEntity.java` | Semana 1 |
| 2 | Implementar DeckCardEntity | `entities/DeckCardEntity.java` | Semana 1 |
| 3 | Implementar DeckRepository | `repositories/DeckRepository.java` | Tarea 1, 2 |
| 4 | Implementar DTOs de deck | `dtos/deck/*.java` (3 archivos) | Tarea 1, 2 |
| 5 | Implementar DeckMapper | `mappers/DeckMapper.java` | Tarea 1, 2, 4 |
| 6 | Implementar DeckValidationService | Nuevo: `services/DeckValidationService.java` | Semana 1 (enums) |
| 7 | Implementar DeckService | `services/DeckService.java` | Tarea 3, 5, 6 |
| 8 | Implementar DeckController | `controllers/DeckController.java` | Tarea 7 |
| 9 | Tests: DeckService + validación | `services/DeckServiceTest.java` | Tarea 6, 7 |

### Frontend

| # | Tarea | Archivos | Dependencias |
|---|---|---|---|
| 1 | Implementar DeckModel | `shared/models/deck.model.ts` | Semana 1 (CardModel) |
| 2 | Implementar DeckService HTTP | `features/deck-builder/services/deck.service.ts` | Semana 1 |
| 3 | Implementar CardCatalog | `features/deck-builder/components/card-catalog/` | Semana 1 |
| 4 | Implementar DeckEditor | `features/deck-builder/components/deck-editor/` | Tarea 2 |
| 5 | Implementar DeckList | `features/deck-builder/components/deck-list/` | Tarea 2 |
| 6 | Implementar DeckSummary | `features/deck-builder/components/deck-summary/` | Tarea 2 |
| 7 | Implementar CardDetailModal | `features/deck-builder/components/card-detail-modal/` | Semana 1 |
| 8 | Implementar DeckBuilderPage | `features/deck-builder/pages/deck-builder-page/` | Tarea 3-7 |
| 9 | Implementar DraggableDirective | `shared/directives/draggable.directive.ts` | - |
| 10 | Agregar ruta deck-builder | `app.routes.ts` | Tarea 8 |

### Entregable Semana 2

- Deck builder funcional con validación (60 cartas, 4 copias máx por nombre, 1+ Basic)
- CRUD completo de mazos

---

## Semana 3: Lobby + Matches + WebSocket

**Objetivo:** Sistema de partidas, WebSocket funcional, lobby en tiempo real

### Backend

| # | Tarea | Archivos | Dependencias |
|---|---|---|---|
| 1 | Implementar MatchEntity | `entities/MatchEntity.java` | Semana 1 |
| 2 | Implementar MatchRepository | `repositories/MatchRepository.java` | Tarea 1 |
| 3 | Implementar DTOs de match | `dtos/match/*.java` (3 archivos) | Tarea 1 |
| 4 | Implementar MatchMapper | `mappers/MatchMapper.java` | Tarea 1, 3 |
| 5 | Implementar MatchService | `services/MatchService.java` | Tarea 2, 4 |
| 6 | Implementar MatchController | `controllers/MatchController.java` | Tarea 5 |
| 7 | Implementar WebSocketConfig | `configs/WebSocketConfig.java` | - |
| 8 | Implementar GameWebSocketHandler | `websocket/GameWebSocketHandler.java` | Tarea 7 |
| 9 | Implementar mensajes WebSocket | `websocket/messages/*.java` (2 archivos) | Tarea 7 |
| 10 | Implementar GameStateEntity | `entities/GameStateEntity.java` | Semana 1 |
| 11 | Implementar GameStateRepository | `repositories/GameStateRepository.java` | Tarea 10 |
| 12 | Implementar GameStatePersistenceService | `services/GameStatePersistenceService.java` | Tarea 11 |
| 13 | Tests: MatchService | `services/MatchServiceTest.java` | Tarea 5 |

### Frontend

| # | Tarea | Archivos | Dependencias |
|---|---|---|---|
| 1 | Implementar MatchModel | `shared/models/match.model.ts` | - |
| 2 | Implementar WebsocketService core | `core/services/websocket.service.ts` | - |
| 3 | Implementar LobbyService | `features/lobby/services/lobby.service.ts` | Tarea 1, 2 |
| 4 | Implementar MatchList | `features/lobby/components/match-list/` | Tarea 3 |
| 5 | Implementar CreateMatchDialog | `features/lobby/components/create-match-dialog/` | Tarea 3 |
| 6 | Implementar LobbyPage | `features/lobby/pages/lobby-page/` | Tarea 4, 5 |
| 7 | Agregar ruta lobby | `app.routes.ts` | Tarea 6 |

### Entregable Semana 3

- Lobby funcional con creación y unión a partidas
- WebSocket conectado y operativo
- Persistencia de estado de partida

---

## Semana 4: Game Engine - State Machine + Rules

**Objetivo:** Motor de juego con máquina de estados, gestión de turnos, reglas

### Backend

| # | Tarea | Archivos | Dependencias |
|---|---|---|---|
| 1 | Implementar engine models | `engine/models/*.java` (9 archivos) | Semana 1 (enums) |
| 2 | Implementar state machine | `engine/state/*.java` (5 archivos) | Tarea 1 |
| 3 | Implementar event system | `engine/events/*.java` (3 archivos) | Tarea 1 |
| 4 | Implementar TurnManager | `engine/rules/TurnManager.java` | Tarea 1 |
| 5 | Implementar RuleValidator | `engine/rules/RuleValidator.java` | Tarea 1 |
| 6 | Implementar VictoryConditionChecker | `engine/rules/VictoryConditionChecker.java` | Tarea 1 |
| 7 | Implementar GameEngine facade | `engine/GameEngine.java` | Tarea 1-6 |
| 8 | Integrar WebSocket con GameEngine | `GameWebSocketHandler.java` (modificar) | Semana 3, Tarea 7 |
| 9 | Tests: Engine básico | `engine/**/*Test.java` | Tarea 1-7 |

### Frontend

| # | Tarea | Archivos | Dependencias |
|---|---|---|---|
| 1 | Implementar GameModel | `shared/models/game.model.ts` | Semana 1 |
| 2 | Implementar GameStateService | `features/game/services/game-state.service.ts` | Tarea 1 |
| 3 | Implementar GameWebsocketService | `features/game/services/game-websocket.service.ts` | Semana 3, Tarea 2 |
| 4 | Configurar routing completo | `app.routes.ts` | Semana 2, 3 |

### Entregable Semana 4

- Game engine funcional con state machine
- Gestión de turnos y validación de reglas
- Integración WebSocket ↔ Engine

---

## Semana 5: Game Engine - Combat + Effects + Game Board UI

**Objetivo:** Pipeline de combate completo, efectos de estado, tablero interactivo

### Backend

| # | Tarea | Archivos | Dependencias |
|---|---|---|---|
| 1 | Implementar AttackResolver | `engine/combat/AttackResolver.java` | Semana 4 |
| 2 | Implementar DamageCalculator | `engine/combat/DamageCalculator.java` | Tarea 1 |
| 3 | Implementar attack steps | `engine/combat/steps/*.java` (8 archivos) | Tarea 1, 2 |
| 4 | Implementar status effects | `engine/effects/*.java` (7 archivos) | Semana 4 |
| 5 | Integración completa del engine | `GameEngine.java` (completar) | Tarea 1-4 |
| 6 | Tests exhaustivos del engine | `engine/**/*Test.java` | Tarea 1-5 |
| 7 | Tests de integración: flujo completo | `engine/GameEngineIntegrationTest.java` | Tarea 5 |

### Frontend

| # | Tarea | Archivos | Dependencias |
|---|---|---|---|
| 1 | Implementar GamePage | `features/game/pages/game-page/` | Semana 4 |
| 2 | Implementar GameBoard (crear desde cero) | `features/game/components/game-board/` | Semana 4 |
| 3 | Implementar PlayerArea + OpponentArea | `player-area/`, `opponent-area/` | Tarea 2 |
| 4 | Implementar ActivePokemon + BenchArea + HandArea | `active-pokemon/`, `bench-area/`, `hand-area/` | Tarea 2 |
| 5 | Implementar ActionPanel + AttackSelector | `action-panel/`, `attack-selector/` | Tarea 2 |
| 6 | Implementar DeckPile + DiscardPile + PrizeCards | `deck-pile/`, `discard-pile/`, `prize-cards/` | Tarea 2 |
| 7 | Implementar SetupPhase + EvolutionSelector | `setup-phase/`, `evolution-selector/` | Tarea 2 |
| 8 | Implementar EnergyAttachment + StadiumArea | `energy-attachment/`, `stadium-area/` | Tarea 2 |
| 9 | Implementar GameOver + NotificationToast | `game-over/`, `notification-toast/` | Tarea 2 |
| 10 | Implementar shared components | `shared/components/*` (6 componentes) | - |
| 11 | Implementar shared UI | `shared/ui/*` (3 componentes) | - |
| 12 | Implementar pipes | `shared/pipes/*.ts` (2 archivos) | - |
| 13 | Integración WebSocket ↔ Game Board | Varios | Tarea 1-12 |

### Entregable Semana 5

- Game engine completo con todas las reglas
- Tablero de juego interactivo con multiplayer en tiempo real

---

## Semana 6: Testing, Documentación y Pulido

**Objetivo:** Cumplir todos los requisitos no funcionales, documentación final

### Backend

| # | Tarea | Archivos |
|---|---|---|
| 1 | JaCoCo ≥ 80% global | Tests faltantes |
| 2 | JaCoCo ≥ 90% componentes críticos | Tests de engine |
| 3 | Checkstyle: fix all violations | Todo el código BE |
| 4 | PMD: fix all violations | Todo el código BE |
| 5 | Swagger/OpenAPI review | Auto-generado |
| 6 | Scripts SQL de migración | `resources/db/migration/*.sql` |
| 7 | Perfil PostgreSQL | `application-prod.properties` |

### Frontend

| # | Tarea | Archivos |
|---|---|---|
| 1 | Tests unitarios de componentes | `.spec.ts` faltantes |
| 2 | Test E2E (mínimo 1) | Nuevo archivo E2E |
| 3 | Bug fixing general | Varios |
| 4 | Performance tuning | Varios |

### Documentación

| # | Tarea | Archivos |
|---|---|---|
| 1 | README con instrucciones de setup | `README.md` |
| 2 | Documentación técnica | `docs/sdd/` (este documento) |
| 3 | Diagramas UML | `BE/docs/app_doc/diagrams/*.puml` |
| 4 | JavaDoc | `mvn javadoc:javadoc` |

### Entregable Semana 6

- Aplicación lista para entrega con toda la documentación
- Cobertura de testing cumplida
- Scripts de migración y seed

---

## Dependencias entre Semanas

```
Semana 1 (Cards + Pokedex)
    │
    ├──► Semana 2 (Deck Builder) ──────────────────────┐
    │                                                   │
    ├──► Semana 3 (Lobby + WebSocket) ──► Semana 4 ────┤
    │                                    (Engine State) │
    │                                                   │
    └──────────────────────────────────► Semana 5 ─────┤
                                       (Combat + UI)   │
                                                       │
                                       Semana 6 ◄──────┘
                                    (Testing + Docs)
```
