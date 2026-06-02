# 02 - Arquitectura

## Arquitectura General

```
┌─────────────────────────────────────────────────────────────────┐
│                        CLIENT (Angular)                         │
│  ┌──────────┐  ┌──────────────┐  ┌──────────┐  ┌────────────┐  │
│  │ Pokedex  │  │ Deck Builder │  │  Lobby   │  │ Game Board │  │
│  │ Feature  │  │   Feature    │  │ Feature  │  │  Feature   │  │
│  └────┬─────┘  └──────┬───────┘  └────┬─────┘  └─────┬──────┘  │
│       │               │               │               │         │
│  ┌────┴───────────────┴───────────────┴───────────────┴──────┐  │
│  │                    Core Layer                              │  │
│  │  Services │ Interceptors │ Models │ Constants │ Pipes │ UI │  │
│  └────────────────────────┬──────────────────────────────────┘  │
└───────────────────────────┼─────────────────────────────────────┘
                            │
              HTTP REST API │  WebSocket (tiempo real)
                            │
┌───────────────────────────┼─────────────────────────────────────┐
│                        SERVER (Spring Boot)                      │
│  ┌─────────┐  ┌──────────┐  ┌──────────┐  ┌──────────────────┐  │
│  │  Card   │  │   Deck   │  │  Match   │  │   Game Engine    │  │
│  │Controller│  │Controller│  │Controller│  │   (Core Logic)   │  │
│  └────┬────┘  └────┬─────┘  └────┬─────┘  └────────┬─────────┘  │
│       │            │            │                   │            │
│  ┌────┴────────────┴────────────┴───────────────────┴────────┐  │
│  │                    Service Layer                           │  │
│  │  CardService │ DeckService │ MatchService │ GameStateSvc  │  │
│  └────────────────────────┬──────────────────────────────────┘  │
│                           │                                     │
│  ┌────────────────────────┴──────────────────────────────────┐  │
│  │                  Repository Layer (JPA)                    │  │
│  │  CardRepo │ DeckRepo │ MatchRepo │ GameStateRepo          │  │
│  └────────────────────────┬──────────────────────────────────┘  │
└───────────────────────────┼─────────────────────────────────────┘
                            │
                    ┌───────┴───────┐
                    │  H2 (dev)     │
                    │  PostgreSQL   │
                    │  (prod)       │
                    └───────────────┘
```

## Patrones de Diseño (RNF-04)

### State Pattern

**Ubicación:** `engine/state/`

**Propósito:** Gestionar el ciclo de vida del match con transiciones de estado claras y testeables.

```
MatchState (interfaz)
├── WaitingState   → espera jugadores
├── SetupState     → configuración inicial (mulligan, monedas)
├── ActiveState    → juego activo (DRAW → MAIN → ATTACK → BETWEEN_TURNS)
└── FinishedState  → partida terminada
```

**Transiciones:**
```
WAITING ──[player joins]──► SETUP ──[both ready]──► ACTIVE
                                                        │
                        ┌───────────────────────────────┤
                        ▼                               │
                    DRAW ──► MAIN ──► ATTACK ──► BETWEEN_TURNS
                        │                               │
                        └─────────── loop ──────────────┘
                                                        │
                          [win condition met] ──► FINISHED
```

### Strategy Pattern

**Ubicación:** `engine/effects/`

**Propósito:** Implementar el comportamiento de las condiciones especiales de forma intercambiable.

```
StatusEffectStrategy (interfaz)
├── BurnedEffect     → entre turnos: 2 contadores de daño, flip para quitar
├── PoisonedEffect   → entre turnos: 1 contador de daño
├── AsleepEffect     → entre turnos: flip para despertar, sino saltea turno
├── ParalyzedEffect  → saltea próximo turno, se quita después
└── ConfusedEffect   → al atacar: flip heads o auto-daño 30
```

### Chain of Responsibility

**Ubicación:** `engine/combat/steps/`

**Propósito:** Pipeline de resolución de ataques con 7 pasos encadenados.

```
EnergyValidationStep → ConfusionCheckStep → TargetSelectionStep
→ PreAttackEffectStep → AttackModifierStep → DamageCalculationStep
→ PostDamageEffectStep
```

Cada paso recibe el contexto del ataque y decide si continuar o no.

### Observer Pattern

**Ubicación:** `engine/events/`

**Propósito:** Broadcasting de eventos del juego a los clientes WebSocket.

```
GameEventType (enum) → GameEvent (modelo) → GameEventPublisher (publicador)
```

### Repository Pattern

**Ubicación:** `repositories/`

**Propósito:** Abstracción del acceso a datos sobre JPA.

```
CardRepository extends JpaRepository<CardEntity, UUID>
DeckRepository extends JpaRepository<DeckEntity, UUID>
MatchRepository extends JpaRepository<MatchEntity, UUID>
GameStateRepository extends JpaRepository<GameStateEntity, UUID>
```

### Facade Pattern

**Ubicación:** `engine/GameEngine.java`

**Propósito:** Interfaz unificada para los subsistemas complejos del engine (state machine, combat, rules, effects).

### Mapper Pattern

**Ubicación:** `mappers/`

**Propósito:** Conversión DTO ↔ Entity usando ModelMapper.

```
CardMapper, DeckMapper, MatchMapper
```

## Estructura de Paquetes Backend

```
ar.edu.utn.frc.tup.piii/
├── Application.java
├── configs/
│   ├── CorsConfig.java
│   ├── MappersConfig.java
│   ├── SpringDocConfig.java
│   └── WebSocketConfig.java
├── controllers/
│   ├── CardController.java
│   ├── DeckController.java
│   ├── MatchController.java
│   └── PingController.java
├── dtos/
│   ├── card/
│   │   ├── CardDTO.java
│   │   └── CardFilterDTO.java
│   ├── common/
│   │   └── ErrorApi.java
│   ├── deck/
│   │   ├── CreateDeckDTO.java
│   │   ├── DeckDTO.java
│   │   └── DeckValidationResultDTO.java
│   └── match/
│       ├── CreateMatchDTO.java
│       ├── MatchDTO.java
│       └── MatchStateDTO.java
├── entities/
│   ├── BaseEntity.java
│   ├── CardEntity.java
│   ├── DeckCardEntity.java
│   ├── DeckEntity.java
│   ├── GameStateEntity.java
│   └── MatchEntity.java
├── engine/
│   ├── GameEngine.java
│   ├── combat/
│   │   ├── AttackResolver.java
│   │   ├── DamageCalculator.java
│   │   └── steps/
│   │       ├── AttackModifierStep.java
│   │       ├── AttackStep.java
│   │       ├── ConfusionCheckStep.java
│   │       ├── DamageCalculationStep.java
│   │       ├── EnergyValidationStep.java
│   │       ├── PostDamageEffectStep.java
│   │       ├── PreAttackEffectStep.java
│   │       └── TargetSelectionStep.java
│   ├── effects/
│   │   ├── AsleepEffect.java
│   │   ├── BurnedEffect.java
│   │   ├── ConfusedEffect.java
│   │   ├── ParalyzedEffect.java
│   │   ├── PoisonedEffect.java
│   │   ├── StatusEffectManager.java
│   │   └── StatusEffectStrategy.java
│   ├── events/
│   │   ├── GameEvent.java
│   │   ├── GameEventPublisher.java
│   │   └── GameEventType.java
│   ├── models/
│   │   ├── EnergyCard.java
│   │   ├── GameBoard.java
│   │   ├── GameCard.java
│   │   ├── MatchSnapshot.java
│   │   ├── PlayerBoard.java
│   │   ├── PokemonCard.java
│   │   ├── PokemonInPlay.java
│   │   ├── TrainerCard.java
│   │   └── TurnContext.java
│   ├── rules/
│   │   ├── RuleValidator.java
│   │   ├── TurnManager.java
│   │   └── VictoryConditionChecker.java
│   └── state/
│       ├── ActiveState.java
│       ├── FinishedState.java
│       ├── MatchState.java
│       ├── SetupState.java
│       └── WaitingState.java
├── enums/
│   ├── CardType.java
│   ├── EnergyType.java
│   ├── GamePhase.java
│   ├── GameZone.java
│   ├── PokemonStage.java
│   ├── SpecialCondition.java
│   ├── TrainerSubtype.java
│   └── TurnPhase.java
├── exceptions/
│   ├── DeckValidationException.java
│   ├── EntityNotFoundException.java
│   ├── GameRuleViolationException.java
│   ├── GlobalExceptionHandler.java
│   └── InvalidActionException.java
├── mappers/
│   ├── CardMapper.java
│   ├── DeckMapper.java
│   └── MatchMapper.java
├── repositories/
│   ├── CardRepository.java
│   ├── DeckRepository.java
│   ├── GameStateRepository.java
│   └── MatchRepository.java
├── services/
│   ├── CardService.java
│   ├── DeckService.java
│   ├── GameStatePersistenceService.java
│   └── MatchService.java
└── websocket/
    ├── GameWebSocketHandler.java
    └── messages/
        ├── GameActionMessage.java
        └── GameStateUpdateMessage.java
```

## Estructura de Carpetas Frontend

```
src/app/
├── app.ts / app.html / app.css
├── app.routes.ts
├── app.config.ts
├── core/
│   ├── constants/
│   │   ├── card-types.ts
│   │   ├── energy-types.ts
│   │   ├── game-phases.ts
│   │   └── special-conditions.ts
│   ├── guards/
│   │   └── .gitkeep
│   ├── interceptors/
│   │   └── error.interceptor.ts
│   └── services/
│       └── websocket.service.ts
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
    ├── components/
    │   ├── card/
    │   ├── card-stack/
    │   ├── damage-counter/
    │   ├── energy-badge/
    │   ├── game-log/
    │   └── status-indicator/
    ├── directives/
    │   └── draggable.directive.ts
    ├── models/
    │   ├── card.model.ts
    │   ├── deck.model.ts
    │   ├── game.model.ts
    │   └── match.model.ts
    ├── pipes/
    │   ├── energy-type.pipe.ts
    │   └── hp-display.pipe.ts
    └── ui/
        ├── button/
        ├── loading-spinner/
        └── modal/
```

## Diagramas UML Sugeridos

Los diagramas se implementarán en `BE/docs/app_doc/diagrams/` ampliando los archivos `.puml` existentes:

1. **Class Diagram** (`class.puml`) - Entidades JPA + modelos del engine
2. **Component Diagram** (`components.puml`) - Arquitectura completa BE + FE
3. **Sequence Diagram** (`sequences.puml`) - Flujo de una partida completa
4. **State Diagram** (nuevo) - Máquina de estados del match
5. **ER Diagram** (nuevo) - Modelo relacional de la DB
