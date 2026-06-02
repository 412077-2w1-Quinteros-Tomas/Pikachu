# 03 - Modelo de Dominio

## Entidades Persistentes (JPA)

### CardEntity

Representa una carta del TCG. Los datos se obtienen de la API pokemontcg.io y se almacenan localmente.

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | UUID | Identificador único (PK) |
| `externalId` | String | ID de la API externa (único) |
| `name` | String | Nombre de la carta |
| `hp` | Integer | Puntos de vida (null para Trainer/Energy) |
| `types` | JSON | Tipos de la carta (Fire, Water, etc.) |
| `stage` | Enum | Basic, Stage1, Stage2, EX, Mega |
| `attacks` | JSON | Lista de ataques con nombre, costo, daño, efecto |
| `abilities` | JSON | Lista de habilidades |
| `weakness` | String | Debilidad (tipo) |
| `resistance` | String | Resistencia (tipo) |
| `retreatCost` | Integer | Costo de retirada |
| `imageUrl` | String | URL de la imagen |
| `rarity` | String | Rareza |
| `cardNumber` | String | Número en el set |
| `setId` | String | ID del set (default: "xy1") |

### DeckEntity

Representa un mazo construido por un jugador.

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | UUID | Identificador único (PK) |
| `name` | String | Nombre del mazo |
| `creatorId` | String | ID del creador |
| `isActive` | Boolean | Si el mazo está activo |
| `createdAt` | Timestamp | Fecha de creación |
| `updatedAt` | Timestamp | Fecha de última actualización |

### DeckCardEntity

Relación muchos a muchos entre Deck y Card con cantidad.

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | UUID | Identificador único (PK) |
| `deckId` | UUID | FK a DeckEntity |
| `cardId` | UUID | FK a CardEntity |
| `quantity` | Integer | Cantidad de copias (> 0) |

### MatchEntity

Representa una partida entre dos jugadores.

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | UUID | Identificador único (PK) |
| `player1Id` | String | ID del jugador 1 (creador) |
| `player2Id` | String | ID del jugador 2 (invitado, nullable) |
| `status` | Enum | WAITING, SETUP, ACTIVE, FINISHED |
| `winnerId` | String | ID del ganador (nullable) |
| `createdAt` | Timestamp | Fecha de creación |
| `updatedAt` | Timestamp | Fecha de última actualización |

### GameStateEntity

Persistencia del estado del tablero en formato JSON.

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | UUID | Identificador único (PK) |
| `matchId` | UUID | FK a MatchEntity |
| `boardState` | JSONB | Estado completo del tablero |
| `actionLog` | JSONB | Log inmutable de acciones |
| `turnData` | JSONB | Datos del turno actual |
| `createdAt` | Timestamp | Fecha de creación |
| `updatedAt` | Timestamp | Fecha de última actualización |

## Modelos del Engine (en memoria, no persistentes)

### GameCard (interfaz)

Contrato base para todos los tipos de carta en juego.

```java
public interface GameCard {
    String getId();
    String getName();
    CardType getType();
}
```

### PokemonCard

Carta de Pokémon con HP, ataques, habilidades y stage.

```java
public class PokemonCard implements GameCard {
    String id;
    String name;
    Integer hp;
    PokemonStage stage;
    Set<EnergyType> types;
    List<Attack> attacks;
    List<Ability> abilities;
    EnergyType weakness;
    EnergyType resistance;
    Integer retreatCost;
    String imageUrl;
}
```

### EnergyCard

Carta de energía con tipo.

```java
public class EnergyCard implements GameCard {
    String id;
    String name;
    EnergyType energyType;
}
```

### TrainerCard

Carta de entrenador con subtipo.

```java
public class TrainerCard implements GameCard {
    String id;
    String name;
    TrainerSubtype subtype;
    String effect;
}
```

### PokemonInPlay

Instancia de un Pokémon en el tablero con estado dinámico.

```java
public class PokemonInPlay {
    PokemonCard card;
    Integer currentHp;
    Integer damageCounters;
    List<EnergyCard> attachedEnergy;
    TrainerCard tool;
    Set<SpecialCondition> specialConditions;
    boolean isActive;
}
```

### PlayerBoard

Estado completo de un jugador en el juego.

```java
public class PlayerBoard {
    String playerId;
    PokemonInPlay activePokemon;
    List<PokemonInPlay> bench;           // max 5
    List<GameCard> hand;                 // cartas en mano
    List<GameCard> deck;                 // mazo (orden preservado)
    List<GameCard> discardPile;          // pila de descarte
    List<GameCard> prizeCards;           // cartas de premio (6)
    Integer prizeCardsTaken;
    StadiumCard stadium;
}
```

### GameBoard

Tablero completo de la partida.

```java
public class GameBoard {
    PlayerBoard player1Board;
    PlayerBoard player2Board;
    StadiumCard sharedStadium;
}
```

### TurnContext

Contexto del turno actual.

```java
public class TurnContext {
    String currentPlayerId;
    TurnPhase phase;
    boolean isFirstTurn;
    boolean hasDrawn;
    boolean hasPlayedTrainer;
    boolean hasEvolved;
    boolean hasAttachedEnergy;
    boolean hasUsedAbility;
    boolean hasRetreated;
}
```

### MatchSnapshot

Snapshot serializable del estado completo para transferencia/persistencia.

```java
public class MatchSnapshot {
    String matchId;
    GamePhase phase;
    GameBoard board;
    TurnContext turn;
    List<GameEvent> eventLog;
    String winnerId;
}
```

## Enums

### CardType

| Valor | Descripción |
|---|---|
| `POKEMON` | Carta de Pokémon |
| `ENERGY` | Carta de energía |
| `TRAINER` | Carta de entrenador |

### EnergyType

| Valor | Descripción |
|---|---|
| `FIRE` | Fuego |
| `WATER` | Agua |
| `GRASS` | Planta |
| `LIGHTNING` | Rayo |
| `PSYCHIC` | Psíquica |
| `FIGHTING` | Lucha |
| `DARKNESS` | Oscura |
| `METAL` | Metálica |
| `FAIRY` | Hada |
| `DRAGON` | Dragón |
| `COLORLESS` | Incolora |

### PokemonStage

| Valor | Descripción |
|---|---|
| `BASIC` | Pokémon básico |
| `STAGE1` | Evolución nivel 1 |
| `STAGE2` | Evolución nivel 2 |
| `EX` | Pokémon-EX (2 premios al KO) |
| `MEGA` | Mega evolución |

### TrainerSubtype

| Valor | Descripción |
|---|---|
| `ITEM` | Objeto (ilimitados por turno) |
| `SUPPORTER` | Adiestrador (1 por turno) |
| `STADIUM` | Estadio (1 por turno, zona compartida) |
| `TOOL` | Herramienta Pokémon (1 por Pokémon) |
| `PLASMA` | Equipo Plasma |

### SpecialCondition

| Valor | Descripción |
|---|---|
| `ASLEEP` | Dormido |
| `BURNED` | Quemado |
| `CONFUSED` | Confundido |
| `PARALYZED` | Paralizado |
| `POISONED` | Envenenado |

**Reglas de incompatibilidad:**
- Asleep, Confused y Paralyzed son mutuamente excluyentes
- Burned y Poisoned pueden coexistir con cualquier otra condición

### GamePhase

| Valor | Descripción |
|---|---|
| `WAITING` | Esperando jugadores |
| `SETUP` | Configuración inicial |
| `DRAW` | Fase de robo |
| `MAIN` | Fase de acciones |
| `ATTACK` | Fase de ataque |
| `BETWEEN_TURNS` | Procesamiento entre turnos |
| `FINISHED` | Partida terminada |

### TurnPhase

| Valor | Descripción |
|---|---|
| `DRAW` | Robar carta |
| `ACTIONS` | Acciones del turno |
| `ATTACK` | Atacar |
| `BETWEEN_TURNS` | Efectos entre turnos |

### GameZone

| Valor | Descripción |
|---|---|
| `HAND` | Mano del jugador |
| `DECK` | Mazo |
| `DISCARD` | Pila de descarte |
| `ACTIVE` | Pokémon activo |
| `BENCH` | Banca |
| `PRIZE` | Cartas de premio |
| `STADIUM` | Estadio |
| `TOOL` | Herramienta equipada |
| `ENERGY` | Energía attachada |

## Relaciones entre Entidades

```
CardEntity 1 ──── N DeckCardEntity N ──── 1 DeckEntity

MatchEntity 1 ──── 1 GameStateEntity
```

- Una carta puede estar en muchos mazos
- Un mazo tiene muchas cartas (a través de DeckCardEntity)
- Una partida tiene exactamente un estado de juego
- Un jugador puede tener muchos mazos y muchas partidas
