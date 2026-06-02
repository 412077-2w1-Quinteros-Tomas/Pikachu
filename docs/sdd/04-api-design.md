# 04 - API Design

## REST Endpoints

### Card Controller (`/api/cards`)

| Método | Path | Descripción | Request | Response |
|---|---|---|---|---|
| GET | `/api/cards` | Listar cartas con paginación y filtros | `?page=0&size=20&type=fire&stage=basic` | `Page<CardDTO>` |
| GET | `/api/cards/{id}` | Obtener carta por ID | - | `CardDTO` |
| GET | `/api/cards/search` | Buscar cartas por nombre | `?name=charizard` | `List<CardDTO>` |
| POST | `/api/cards/sync` | Sincronizar cartas desde pokemontcg.io | `{ "setId": "xy1" }` | `{ "synced": 146 }` |

### Deck Controller (`/api/decks`)

| Método | Path | Descripción | Request | Response |
|---|---|---|---|---|
| GET | `/api/decks` | Listar mazos del usuario | - | `List<DeckDTO>` |
| GET | `/api/decks/{id}` | Obtener detalle de mazo | - | `DeckDTO` |
| POST | `/api/decks` | Crear nuevo mazo | `CreateDeckDTO` | `DeckDTO` |
| PUT | `/api/decks/{id}` | Actualizar mazo | `CreateDeckDTO` | `DeckDTO` |
| DELETE | `/api/decks/{id}` | Eliminar mazo | - | `204 No Content` |
| POST | `/api/decks/{id}/validate` | Validar reglas del mazo | - | `DeckValidationResultDTO` |

### Match Controller (`/api/matches`)

| Método | Path | Descripción | Request | Response |
|---|---|---|---|---|
| GET | `/api/matches` | Listar partidas disponibles (lobby) | - | `List<MatchDTO>` |
| GET | `/api/matches/{id}` | Obtener detalle de partida | - | `MatchDTO` |
| POST | `/api/matches` | Crear nueva partida | `CreateMatchDTO` | `MatchDTO` |
| POST | `/api/matches/{id}/join` | Unirse a una partida | - | `MatchDTO` |
| POST | `/api/matches/{id}/leave` | Abandonar partida | - | `204 No Content` |

### Ping Controller (`/ping`)

| Método | Path | Descripción | Response |
|---|---|---|---|
| GET | `/ping` | Health check | `"pong"` |

## DTOs

### CardDTO

```java
public class CardDTO {
    String id;
    String externalId;
    String name;
    Integer hp;
    Set<EnergyType> types;
    PokemonStage stage;
    List<AttackDTO> attacks;
    List<AbilityDTO> abilities;
    String weakness;
    String resistance;
    Integer retreatCost;
    String imageUrl;
    String rarity;
    String cardNumber;
    String setId;
}
```

### CardFilterDTO

```java
public class CardFilterDTO {
    String name;
    EnergyType type;
    PokemonStage stage;
    String setId;
    Integer page;
    Integer size;
}
```

### CreateDeckDTO

```java
public class CreateDeckDTO {
    @NotBlank String name;
    @NotEmpty List<DeckCardEntryDTO> cards;
}

public class DeckCardEntryDTO {
    String cardId;
    Integer quantity;
}
```

### DeckDTO

```java
public class DeckDTO {
    String id;
    String name;
    String creatorId;
    List<DeckCardDTO> cards;
    Integer totalCards;
    Boolean isValid;
    List<String> validationErrors;
}
```

### DeckValidationResultDTO

```java
public class DeckValidationResultDTO {
    Boolean isValid;
    List<String> errors;
    Integer totalCards;
    Integer basicPokemonCount;
    Map<String, Integer> cardNameCounts;
}
```

### CreateMatchDTO

```java
public class CreateMatchDTO {
    @NotBlank String player1Id;
    String player1DeckId;
}
```

### MatchDTO

```java
public class MatchDTO {
    String id;
    String player1Id;
    String player2Id;
    String status;
    String winnerId;
    LocalDateTime createdAt;
}
```

### MatchStateDTO

```java
public class MatchStateDTO {
    String matchId;
    String phase;
    String currentPlayerId;
    PlayerStateDTO player1;
    PlayerStateDTO player2;
    List<GameEventDTO> recentEvents;
}
```

### ErrorApi

```java
public class ErrorApi {
    String timestamp;
    Integer status;
    String error;
    String message;
    String path;
}
```

## Protocolo WebSocket

### Conexión

```
URL: /ws/game
Protocolo: WebSocket (STOMP sobre WebSocket)
```

### Mensajes Cliente → Servidor

```typescript
interface GameActionMessage {
  matchId: string;
  type: GameActionType;
  payload: Record<string, unknown>;
  timestamp: string;
}

type GameActionType =
  | 'DRAW_CARD'           // Robar carta
  | 'PLACE_POKEMON'       // Colocar Pokémon en banca
  | 'ATTACH_ENERGY'       // Attachar energía
  | 'PLAY_TRAINER'        // Jugar carta de entrenador
  | 'EVOLVE'              // Evolucionar Pokémon
  | 'RETREAT'             // Retirar Pokémon activo
  | 'ATTACK'              // Atacar
  | 'USE_ABILITY'         // Usar habilidad
  | 'SURRENDER'           // Rendirse
  | 'READY'               // Listo para comenzar (setup)
  | 'SET_ACTIVE';         // Cambiar Pokémon activo (tras KO)
```

### Mensajes Servidor → Cliente

```typescript
interface GameStateUpdateMessage {
  type: 'STATE_UPDATE' | 'EVENT' | 'ERROR' | 'NOTIFICATION';
  state?: MatchSnapshot;
  event?: GameEvent;
  error?: string;
  notification?: string;
}

interface GameEvent {
  type: GameEventType;
  playerId: string;
  description: string;
  timestamp: string;
}

type GameEventType =
  | 'CARD_DRAWN'
  | 'POKEMON_PLACED'
  | 'ENERGY_ATTACHED'
  | 'TRAINER_PLAYED'
  | 'POKEMON_EVOLVED'
  | 'POKEMON_RETREATED'
  | 'ATTACK_EXECUTED'
  | 'POKEMON_KNOCKED_OUT'
  | 'PRIZE_TAKEN'
  | 'SPECIAL_CONDITION_APPLIED'
  | 'SPECIAL_CONDITION_REMOVED'
  | 'TURN_CHANGED'
  | 'PHASE_CHANGED'
  | 'GAME_FINISHED';
```

## Códigos de Error HTTP

| Código | Uso |
|---|---|
| 400 | Bad Request (validación fallida) |
| 404 | Not Found (entidad no encontrada) |
| 409 | Conflict (acción no válida en estado actual) |
| 500 | Internal Server Error |

## Seguridad de la API

- El backend valida TODAS las acciones antes de aplicarlas
- La mano del oponente nunca se envía al cliente
- El orden del mazo y las cartas de premio se ocultan al cliente
- Durante el juego, no se permiten llamadas REST directas (solo WebSocket)
