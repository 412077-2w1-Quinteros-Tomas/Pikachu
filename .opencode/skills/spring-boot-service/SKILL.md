---
name: spring-boot-service
description: Implements Spring Boot backend services, controllers, DTOs, mappers, and repositories for the Pokemon TCG project. Use when creating or editing files under BE/src, particularly controllers/, services/, dtos/, mappers/, repositories/, configs/, exceptions/. Triggers on controller, service, DTO, mapper, repository, endpoint, REST API, Spring Boot.
---

# Spring Boot Service Skill - Pokemon TCG

You are implementing the Spring Boot backend for a Pokemon Trading Card Game application. Follow these conventions strictly.

## Project Setup

- **Java 21** with Spring Boot 4.0
- **Base package**: `ar.edu.utn.frc.tup.piii`
- **Build**: Maven
- **DB (dev)**: H2 in-memory
- **DB (prod)**: PostgreSQL
- **Dependencies**: Lombok, ModelMapper, SpringDoc (Swagger), Validation, Actuator, JPA

## Package Structure

```
ar.edu.utn.frc.tup.piii/
├── Application.java
├── configs/       → CorsConfig, MappersConfig, SpringDocConfig, WebSocketConfig
├── controllers/   → CardController, DeckController, MatchController, PingController
├── dtos/
│   ├── card/      → CardDTO, CardFilterDTO
│   ├── common/    → ErrorApi
│   ├── deck/      → CreateDeckDTO, DeckDTO, DeckValidationResultDTO
│   └── match/     → CreateMatchDTO, MatchDTO, MatchStateDTO
├── entities/      → BaseEntity, CardEntity, DeckCardEntity, DeckEntity, MatchEntity, GameStateEntity
├── engine/         → Game engine (separate skill)
├── enums/          → CardType, EnergyType, GamePhase, GameZone, PokemonStage, SpecialCondition, TrainerSubtype, TurnPhase
├── exceptions/     → DeckValidationException, EntityNotFoundException, GameRuleViolationException, GlobalExceptionHandler, InvalidActionException
├── mappers/        → CardMapper, DeckMapper, MatchMapper
├── repositories/   → CardRepository, DeckRepository, GameStateRepository, MatchRepository
├── services/       → CardService, DeckService, MatchService, GameStatePersistenceService
└── websocket/      → GameWebSocketHandler, messages/
```

## Coding Conventions

### Entities

- Extend `BaseEntity` (which provides `id` as UUID and audit fields)
- Use Lombok `@Data`, `@AllArgsConstructor`, `@NoArgsConstructor`, `@Builder`
- Use JPA annotations (`@Entity`, `@Table`, `@Column`, etc.)
- JSON fields use `@JdbcTypeCode(SqlTypes.JSON)` or JPA `@Convert` with custom converter
- All entity classes currently exist as stubs (empty classes `{}`) - fill them in

### DTOs

- Use Lombok `@Data`, `@AllArgsConstructor`, `@NoArgsConstructor`, `@Builder`
- Use `@NotBlank`, `@NotEmpty`, `@NotNull` from `jakarta.validation.constraints` for validation
- Keep DTOs in sub-packages by domain: `dtos/card/`, `dtos/deck/`, `dtos/match/`
- Use `ErrorApi` for error responses (already implemented)

### Repositories

- Extend `JpaRepository<EntityName, UUID>`
- Use Spring Data JPA derived query methods
- Custom queries use `@Query` with JPQL

```java
@Repository
public interface CardRepository extends JpaRepository<CardEntity, UUID> {
    List<CardEntity> findBySetId(String setId);
    List<CardEntity> findByStage(PokemonStage stage);
    Optional<CardEntity> findByExternalId(String externalId);
}
```

### Mappers

- Inject the `ModelMapper` bean for simple mappings
- Use `@Qualifier("mergerMapper")` ModelMapper bean for update operations (only maps non-null fields)
- Custom mapping logic goes in the mapper class itself as methods

```java
@Component
public class CardMapper {
    private final ModelMapper modelMapper;

    public CardMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public CardDTO mapToDto(CardEntity entity) {
        return modelMapper.map(entity, CardDTO.class);
    }
}
```

### Services

- Annotate with `@Service`
- Use `@Transactional` for write operations
- Throw custom exceptions: `EntityNotFoundException`, `DeckValidationException`, `GameRuleViolationException`, `InvalidActionException`
- Inject repositories and mappers via constructor injection

```java
@Service
public class CardService {
    private final CardRepository cardRepository;
    private final CardMapper cardMapper;

    public CardService(CardRepository cardRepository, CardMapper cardMapper) {
        this.cardRepository = cardRepository;
        this.cardMapper = cardMapper;
    }
}
```

### Controllers

- Annotate with `@RestController` and `@RequestMapping("/api/resource-name")`
- Use `@Operation`, `@ApiResponses`, `@ApiResponse` from SpringDoc
- Validate input with `@Valid`
- Return appropriate HTTP status codes:
  - `200 OK` for GET/PUT
  - `201 Created` for POST
  - `204 No Content` for DELETE
  - `400 Bad Request` for validation errors
  - `404 Not Found` for missing entities
  - `409 Conflict` for invalid game actions

```java
@RestController
@RequestMapping("/api/cards")
public class CardController {
    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @Operation(summary = "Get all cards", description = "Returns paginated list of cards")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error",
            content = @Content(schema = @Schema(implementation = ErrorApi.class)))
    })
    @GetMapping
    public ResponseEntity<Page<CardDTO>> getAllCards(CardFilterDTO filter) {
        return ResponseEntity.ok(cardService.getAllCards(filter));
    }
}
```

### Exceptions

All custom exceptions already exist as stubs. Fill them in:

```java
public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(String message) {
        super(message);
    }
}
```

`GlobalExceptionHandler` should use `@RestControllerAdvice` and map:
- `EntityNotFoundException` → 404
- `DeckValidationException` → 400
- `GameRuleViolationException` → 409
- `InvalidActionException` → 409

### Config Classes

- `CorsConfig`: Allow Angular dev server origin (localhost:4200)
- `MappersConfig`: Already implemented - provides `ModelMapper` and `mergerMapper` beans
- `SpringDocConfig`: Already implemented - configures Swagger UI
- `WebSocketConfig`: Needs implementation for STOMP over WebSocket

## API Endpoints Reference

### Cards (`/api/cards`)

| Method | Path | Description |
|---|---|---|
| GET | `/api/cards` | List cards with pagination/filters |
| GET | `/api/cards/{id}` | Get card by ID |
| GET | `/api/cards/search?name=` | Search cards by name |
| POST | `/api/cards/sync` | Sync cards from pokemontcg.io API |

### Decks (`/api/decks`)

| Method | Path | Description |
|---|---|---|
| GET | `/api/decks` | List decks for user |
| GET | `/api/decks/{id}` | Get deck detail |
| POST | `/api/decks` | Create new deck |
| PUT | `/api/decks/{id}` | Update deck |
| DELETE | `/api/decks/{id}` | Delete deck |
| POST | `/api/decks/{id}/validate` | Validate deck rules |

### Matches (`/api/matches`)

| Method | Path | Description |
|---|---|---|
| GET | `/api/matches` | List available matches (lobby) |
| GET | `/api/matches/{id}` | Get match detail |
| POST | `/api/matches` | Create new match |
| POST | `/api/matches/{id}/join` | Join a match |
| POST | `/api/matches/{id}/leave` | Leave a match |

### Ping (`/ping`)

Already implemented.

## External API Integration

The `CardService.syncCards()` method must:
1. Call `https://api.pokemontcg.io/v2/cards?q=set:xy1&pageSize=250` with API key header
2. Map the response to `CardEntity` objects
3. Save all cards to the database (upsert by `externalId`)
4. API key comes from `pokemon.api.key` property (env variable `POKEMON_TCG_API_KEY`)

## Checklist

When implementing BE classes:
- [ ] Correct package: `ar.edu.utn.frc.tup.piii.{subpackage}`
- [ ] Lombok annotations present (`@Data`, `@Builder`, etc.)
- [ ] Constructor injection (no `@Autowired` on fields)
- [ ] `@Transactional` on service write methods
- [ ] SpringDoc annotations on controller methods
- [ ] Bean Validation on DTO input fields
- [ ] Custom exceptions thrown from services, not generic ones
- [ ] GlobalExceptionHandler handles all custom exceptions
- [ ] UUID as primary key type for all entities