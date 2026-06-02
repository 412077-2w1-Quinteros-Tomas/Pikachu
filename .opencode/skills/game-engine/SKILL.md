---
name: game-engine
description: Implements Game Engine logic for the Pokemon TCG project. Use when creating or editing files under engine/, writing game rules, state machine, combat pipeline, status effects, or any game logic in the backend. Triggers on engine, game-engine, game rule, state machine, combat, attack, status effect, turn, victory, knockout, match state.
---

# Game Engine Skill - Pokemon TCG

You are implementing the game engine for a digital Pokemon Trading Card Game (set XY1). The engine is the single source of truth for all game rules.

## Architecture Overview

### Package Structure

```
ar.edu.utn.frc.tup.piii.engine/
├── GameEngine.java          (Facade - unified interface to subsystems)
├── combat/
│   ├── AttackResolver.java   (orchestrates the attack pipeline)
│   ├── DamageCalculator.java (base damage, weakness, resistance)
│   └── steps/
│       ├── AttackStep.java            (interface for pipeline steps)
│       ├── EnergyValidationStep.java  (step 1: check energy cost)
│       ├── ConfusionCheckStep.java    (step 2: confused flip)
│       ├── TargetSelectionStep.java   (step 3: select defender)
│       ├── PreAttackEffectStep.java   (step 4: pre-damage effects)
│       ├── AttackModifierStep.java    (step 5: stadium/tools/abilities)
│       ├── DamageCalculationStep.java (step 6: final damage calc)
│       └── PostDamageEffectStep.java  (step 7: post-damage effects)
├── effects/
│   ├── StatusEffectStrategy.java   (interface)
│   ├── AsleepEffect.java
│   ├── BurnedEffect.java
│   ├── ConfusedEffect.java
│   ├── ParalyzedEffect.java
│   ├── PoisonedEffect.java
│   └── StatusEffectManager.java
├── events/
│   ├── GameEvent.java
│   ├── GameEventType.java
│   └── GameEventPublisher.java
├── models/
│   ├── GameCard.java        (interface)
│   ├── PokemonCard.java
│   ├── EnergyCard.java
│   ├── TrainerCard.java
│   ├── PokemonInPlay.java
│   ├── PlayerBoard.java
│   ├── GameBoard.java
│   ├── TurnContext.java
│   └── MatchSnapshot.java
├── rules/
│   ├── RuleValidator.java
│   ├── TurnManager.java
│   └── VictoryConditionChecker.java
└── state/
    ├── MatchState.java       (interface)
    ├── WaitingState.java
    ├── SetupState.java
    ├── ActiveState.java
    └── FinishedState.java
```

## Required Design Patterns

### 1. State Pattern (`engine/state/`)

Match lifecycle goes through: WAITING → SETUP → ACTIVE → FINISHED

Inside ACTIVE: sub-phases DRAW → MAIN → ATTACK → BETWEEN_TURNS

```java
public interface MatchState {
    GamePhase getPhase();
    void handleAction(GameEngine engine, GameActionMessage action);
    void enter(GameEngine engine);
    void exit(GameEngine engine);
}
```

**WaitingState**: accepts `join`, `ready` actions. Transitions to SetupState when both players ready.

**SetupState**: shuffles decks, draws 7 cards, mulligan logic, place basic Pokemon, set 6 prize cards, coin flip for first turn. Transitions to ActiveState (DRAW phase).

**ActiveState**: manages DRAW → MAIN → ATTACK → BETWEEN_TURNS sub-phases. Each sub-phase has different allowed actions.

**FinishedState**: terminal state, no actions allowed.

### 2. Strategy Pattern (`engine/effects/`)

```java
public interface StatusEffectStrategy {
    SpecialCondition getType();
    void applyBetweenTurns(PokemonInPlay pokemon, GameBoard board);
    void onAttack(PokemonInPlay pokemon, AttackContext context);
    boolean canCoexist(SpecialCondition other);
    void remove(PokemonInPlay pokemon);
}
```

**Incompatibility rules**:
- Asleep, Confused, Paralyzed are MUTUALLY EXCLUSIVE (applying one replaces the others)
- Burned and Poisoned CAN coexist with any condition (even each other)
- All conditions are cleared by: evolution or retreat

**Between-turns processing order**:
1. Poisoned: +1 damage counter
2. Burned: +2 damage counters, flip to remove (heads = removed)
3. Asleep: flip to wake (heads = wakes up, tails = skip turn)
4. Paralyzed: auto-removes after the turn is skipped

### 3. Chain of Responsibility (`engine/combat/steps/`)

7-step attack pipeline executed in strict order:

```java
public interface AttackStep {
    void execute(AttackContext context);
    void setNext(AttackStep next);
}
```

Chain: EnergyValidation → ConfusionCheck → TargetSelection → PreAttackEffect → AttackModifier → DamageCalculation → PostDamageEffect

**Step details**:
1. **EnergyValidation**: Check attacker has required energy for the attack. If not, action is invalid.
2. **ConfusionCheck**: If attacker is Confused, flip coin. Heads = proceed normally. Tails = attack fails, 30 self-damage.
3. **TargetSelection**: Target is ALWAYS the opponent's active Pokemon.
4. **PreAttackEffect**: Execute effects before damage (e.g., "discard 1 energy from the defending Pokemon").
5. **AttackModifier**: Apply damage modifiers from Stadium card, Pokemon Tools, Abilities.
6. **DamageCalculation**: Base damage. Weakness = x2 if attacker type matches defender's weakness. Resistance = -20 if attacker type matches defender's resistance. Minimum 0. 1 damage counter per 10 damage (round up).
7. **PostDamageEffect**: Apply special conditions and other post-damage effects (e.g., "the defending Pokemon is now Burned").

### 4. Observer Pattern (`engine/events/`)

```java
public enum GameEventType {
    CARD_DRAWN, POKEMON_PLACED, ENERGY_ATTACHED, TRAINER_PLAYED,
    POKEMON_EVOLVED, POKEMON_RETREATED, ATTACK_EXECUTED,
    POKEMON_KNOCKED_OUT, PRIZE_TAKEN,
    SPECIAL_CONDITION_APPLIED, SPECIAL_CONDITION_REMOVED,
    TURN_CHANGED, PHASE_CHANGED, GAME_FINISHED
}
```

Events are broadcast via `GameEventPublisher` to WebSocket clients after every action.

### 5. Facade Pattern (`GameEngine.java`)

`GameEngine` is the single entry point for all game operations. It delegates to subsystems:
- State machine (MatchState)
- Combat (AttackResolver)
- Rules (RuleValidator, TurnManager)
- Effects (StatusEffectManager)
- Victory (VictoryConditionChecker)
- Events (GameEventPublisher)

```java
@Service
public class GameEngine {
    // Delegates to subsystems, never implements game logic directly
    public MatchSnapshot processAction(UUID matchId, GameActionMessage action) { ... }
    public MatchSnapshot getMatchSnapshot(UUID matchId) { ... }
}
```

## Game Rules Reference

### Turn Structure (RF-01.2)

Each turn has 3 phases:
- **DRAW**: Draw 1 card (skip if first turn of starting player). If deck empty → LOSE.
- **MAIN**: Place Basic Pokemon (unlimited), Evolve (only from next turn), Attach energy (max 1/turn), Play Item (unlimited), Play Supporter (max 1/turn), Play Stadium (max 1/turn), Play Tool (1 per Pokemon, unlimited per turn), Use Ability, Retreat (max 1/turn, costs energy).
- **ATTACK**: Declare attack. Ends the turn. Cannot attack on first turn of starting player.

### Knockout (RF-01.4)

When damage counters × 10 ≥ Pokemon HP:
1. Discard the KO'd Pokemon and all attached cards (energy, tools)
2. Opponent takes prize cards: 1 for normal, 2 for Pokemon-EX
3. Owner must promote a bench Pokemon to active, or if bench is empty → LOSE

### Victory Conditions (RF-01.6)

A player wins if:
1. Takes their last prize card
2. Opponent has no Pokemon in play (active or bench)
3. Opponent cannot draw from empty deck

Sudden Death: if both players win simultaneously → restart with 1 prize card each.

### Deck Validation (RF-04.2)

- Exactly 60 cards
- Max 4 copies of same name (except Basic Energy - unlimited)
- Max 1 ACE SPEC card per deck
- At least 1 Basic Pokemon

## Key Model Definitions

### PokemonInPlay

Instance of a Pokemon on the board with dynamic state:
- `card`: PokemonCard reference
- `currentHp`: remaining HP
- `damageCounters`: accumulated damage
- `attachedEnergy`: List<EnergyCard>
- `tool`: TrainerCard (or null)
- `specialConditions`: Set<SpecialCondition>
- `isActive`: boolean
- `turnsInPlay`: int (for evolution timing)

### TurnContext

Tracks per-turn state:
- `currentPlayerId`: whose turn
- `phase`: DRAW/ACTIONS/ATTACK/BETWEEN_TURNS
- `isFirstTurn`: is this the first turn of the game
- `hasDrawn`: has player drawn this turn
- `hasPlayedTrainer`: has Supporter been played
- `hasEvolved`: has evolution happened
- `hasAttachedEnergy`: has energy been attached
- `hasUsedAbility`: has ability been used
- `hasRetreated`: has retreat been used

### MatchSnapshot

Complete serializable state for transfer/persistence:
- `matchId`, `phase`, `board` (GameBoard), `turn` (TurnContext), `eventLog` (List<GameEvent>), `winnerId`

### GameBoard

- `player1Board`: PlayerBoard
- `player2Board`: PlayerBoard
- `sharedStadium`: TrainerCard (or null)

### PlayerBoard

- `playerId`, `activePokemon` (PokemonInPlay), `bench` (List<PokemonInPlay>, max 5), `hand`, `deck`, `discardPile`, `prizeCards` (6), `prizeCardsTaken` (int)

## Important Constraints

- **Backend is the ONLY source of truth**. All rule validation happens server-side.
- **After every action**, the full state is persisted and broadcast to both clients.
- **Mulligan rule**: If a player has no Basic Pokemon in initial hand, reveal hand, reshuffle deck, redraw 7. Opponent draws 1 extra card per mulligan. Repeat until both have a Basic.
- **Sudden Death**: If both players win simultaneously, restart with 1 prize card each.
- No REST calls during gameplay - all actions go through WebSocket.

## Implementation Checklist

When implementing engine classes, ensure:
- [ ] Class uses correct package: `ar.edu.utn.frc.tup.piii.engine.*`
- [ ] State classes implement `MatchState` interface
- [ ] Effect classes implement `StatusEffectStrategy` interface
- [ ] Attack steps implement `AttackStep` interface
- [ ] GameEngine delegates to subsystems (never implements logic directly)
- [ ] All state changes are persisted via `GameStatePersistenceService`
- [ ] All state changes emit events via `GameEventPublisher`
- [ ] Enums are in `ar.edu.utn.frc.tup.piii.enums` package
- [ ] Models are in `ar.edu.utn.frc.tup.piii.engine.models` package