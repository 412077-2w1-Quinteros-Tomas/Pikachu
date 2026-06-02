# 05 - Game Engine

> **Nota:** Esta sección tiene alta probabilidad de cambios durante el desarrollo. Los modelos en memoria y el pipeline de combate se ajustarán según los edge cases que surjan durante la implementación.

## Overview

El Game Engine es el núcleo del sistema. Es responsable de:

- Gestionar el ciclo de vida de la partida (máquina de estados)
- Validar todas las acciones del jugador
- Resolver combates y calcular daño
- Aplicar efectos de estado
- Determinar condiciones de victoria

**Principio:** El backend es la única fuente de verdad. Todas las acciones se validan en el servidor antes de aplicarlas.

## Máquina de Estados (State Pattern)

### Interfaz MatchState

```java
public interface MatchState {
    GamePhase getPhase();
    void handleAction(GameEngine engine, GameActionMessage action);
    void enter(GameEngine engine);
    void exit(GameEngine engine);
}
```

### WaitingState

- **Entrada:** Partida creada, esperando jugadores
- **Acciones permitidas:** `join`, `ready`
- **Transición:** A `SetupState` cuando ambos jugadores están listos

### SetupState

- **Entrada:** Ambos jugadores conectados y listos
- **Acciones:**
  - Barajar mazos
  - Robar 7 cartas
  - Mostrar mano (solo al jugador correspondiente)
  - Colocar Pokémon Básico en activo
  - Colocar hasta 5 Pokémon en banca
  - Separar 6 cartas de premio
  - Lógica de mulligan (si no hay básicos: mostrar mano, rebarajar, oponente roba +1 por cada mulligan)
  - Lanzar moneda para primer turno
- **Transición:** A `ActiveState` (fase DRAW) cuando ambos terminan el setup

### ActiveState

- **Sub-fases:** DRAW → MAIN → ATTACK → BETWEEN_TURNS
- **DRAW:** Robar 1 carta (salta si es primer turno del jugador inicial)
- **MAIN:** Acciones del turno (colocar básicos, evolucionar, attachar energía, jugar trainers, retirar)
- **ATTACK:** Resolver ataque (pipeline de 7 pasos), termina el turno
- **BETWEEN_TURNS:** Procesar condiciones especiales (Poisoned → Burned → Asleep → Paralyzed)
- **Transición:** Loop entre sub-fases hasta condición de victoria

### FinishedState

- **Entrada:** Se cumple una condición de victoria
- **Acciones:** Ninguna (estado terminal)
- **Resultado:** Registrar ganador, persistir estado final

## Pipeline de Ataque (Chain of Responsibility)

### Interfaz AttackStep

```java
public interface AttackStep {
    void execute(AttackContext context);
    void setNext(AttackStep next);
}
```

### Pasos del Pipeline

1. **EnergyValidationStep** - Verificar que el Pokémon tiene la energía necesaria para el ataque
2. **ConfusionCheckStep** - Si está confundido, flip: heads = normal, tails = auto-daño 30
3. **TargetSelectionStep** - Seleccionar objetivo (Pokémon activo del oponente)
4. **PreAttackEffectStep** - Ejecutar efectos previos al daño (ej: "descartar energía del oponente")
5. **AttackModifierStep** - Aplicar modificadores (Stadium, Tools, Abilities que modifican daño)
6. **DamageCalculationStep** - Calcular daño final:
   - Daño base del ataque
   - Debilidad: ×2 si el tipo del atacante es débil del defensor
   - Resistencia: -20 si el tipo del atacante es resistente del defensor
   - Mínimo 0
   - 1 contador de daño por cada 10 de daño
7. **PostDamageEffectStep** - Aplicar efectos posteriores (condiciones especiales, efectos del ataque)

### AttackContext

```java
public class AttackContext {
    PokemonInPlay attacker;
    PokemonInPlay defender;
    Attack selectedAttack;
    Integer baseDamage;
    Integer finalDamage;
    List<SpecialCondition> effectsToApply;
    boolean attackSuccessful;
    String failureReason;
}
```

## Efectos de Estado (Strategy Pattern)

### Interfaz StatusEffectStrategy

```java
public interface StatusEffectStrategy {
    SpecialCondition getType();
    void applyBetweenTurns(PokemonInPlay pokemon, GameBoard board);
    void onAttack(PokemonInPlay pokemon, AttackContext context);
    boolean canCoexist(SpecialCondition other);
    void remove(PokemonInPlay pokemon);
}
```

### Implementaciones

#### BurnedEffect

- **Entre turnos:** Colocar 2 contadores de daño
- **Remoción:** Flip de moneda al final del turno: heads = se quita
- **Coexistencia:** Puede coexistir con cualquier condición excepto otra quemadura

#### PoisonedEffect

- **Entre turnos:** Colocar 1 contador de daño
- **Remoción:** Se mantiene hasta evolución o retirada
- **Coexistencia:** Puede coexistir con cualquier condición excepto otro envenenamiento

#### AsleepEffect

- **Entre turnos:** Flip de moneda: heads = despierta, tails = permanece dormido (saltea turno)
- **Coexistencia:** Mutuamente excluyente con Confused y Paralyzed

#### ParalyzedEffect

- **Efecto:** Saltea el próximo turno del Pokémon
- **Remoción:** Se quita automáticamente después de saltar un turno
- **Coexistencia:** Mutuamente excluyente con Asleep y Confused

#### ConfusedEffect

- **Al atacar:** Flip de moneda: heads = ataque normal, tails = 30 de auto-daño
- **Coexistencia:** Mutuamente excluyente con Asleep y Paralyzed

### StatusEffectManager

```java
public class StatusEffectManager {
    Map<SpecialCondition, StatusEffectStrategy> effects;

    void addEffect(PokemonInPlay pokemon, SpecialCondition condition);
    void removeEffect(PokemonInPlay pokemon, SpecialCondition condition);
    void processBetweenTurns(PokemonInPlay pokemon, GameBoard board);
    void onAttack(PokemonInPlay pokemon, AttackContext context);
    void clearAll(PokemonInPlay pokemon);  // al evolucionar o retirar
}
```

## Reglas del Juego

### TurnManager

```java
public class TurnManager {
    String getCurrentPlayerId();
    void nextTurn();
    boolean canDraw(String playerId);
    boolean canPerformAction(String playerId, ActionType action);
    void setPhase(TurnPhase phase);
    TurnPhase getPhase();
}
```

**Reglas de turno:**

| Regla | Descripción |
|---|---|
| Robo | 1 carta por turno (salta en primer turno del jugador inicial) |
| Energía | Máximo 1 energía attachada por turno |
| Supporter | Máximo 1 carta Supporter por turno |
| Stadium | Máximo 1 carta Stadium jugada por turno |
| Evolución | Solo se puede evolucionar en el turno siguiente a colocar el Pokémon |
| Retiro | 1 retiro por turno (cuesta energía según retreat cost) |
| Ataque | 1 ataque por turno, termina el turno |

### RuleValidator

```java
public class RuleValidator {
    void validateAction(GameActionMessage action, GameBoard board, TurnContext turn);
    void validateDeck(List<GameCard> deck);
    void validateEvolution(PokemonCard base, PokemonCard evolution);
    void validateEnergyAttachment(EnergyCard energy, PokemonInPlay pokemon, TurnContext turn);
}
```

### VictoryConditionChecker

```java
public class VictoryConditionChecker {
    boolean checkVictory(GameBoard board, String playerId);
    String getWinner(GameBoard board);
}
```

**Condiciones de victoria:**

1. **Última carta de premio tomada** - El jugador que toma su última carta de premio gana
2. **Knockout total** - El oponente no tiene Pokémon en juego ni en banca
3. **Mazo vacío** - El oponente no puede robar carta al comenzar su turno

**Sudden Death:** Si ambos jugadores ganan simultáneamente, se reinicia la partida con 1 carta de premio cada uno.

## Knockout

Cuando el daño total ≥ HP de un Pokémon:

1. El Pokémon queda Fuera de Combate
2. Se descartan todas las cartas attachadas (energía, herramientas)
3. El oponente toma cartas de premio:
   - 1 carta para Pokémon normal
   - 2 cartas para Pokémon-EX
4. El dueño del KO debe:
   - Colocar un Pokémon de la banca como activo, o
   - Si no tiene Pokémon en banca: pierde la partida

## Persistencia del Estado

Después de cada acción, el estado se persiste en `GameStateEntity`:

- `boardState`: Estado completo del tablero (manos, mazos, banca, activo, descarte, premios)
- `actionLog`: Log inmutable de acciones (turno, jugador, tipo de acción, resultado)
- `turnData`: Datos del turno actual (fase, flags de acciones realizadas)
