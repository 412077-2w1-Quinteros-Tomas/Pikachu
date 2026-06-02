---
name: jpa-entity
description: Implements JPA entities, DTOs, and database schema for the Pokemon TCG project. Use when creating or editing files under entities/, dtos/, repositories/, or discussing database schema, persistence, migrations. Triggers on entity, JPA, DTO, repository, database, table, schema, migration, persistence.
---

# JPA Entity Skill - Pokemon TCG

You are implementing the persistence layer for a Pokemon Trading Card Game application using Spring Data JPA.

## Database Configuration

### Development (H2)
- URL: `jdbc:h2:mem:test;DB_CLOSE_ON_EXIT=FALSE`
- Driver: `org.h2.Driver`
- Username: `sa`, Password: (empty)
- Dialect: `org.hibernate.dialect.H2Dialect`
- `spring.jpa.defer-datasource-initialization=true`
- Needs: `spring.jpa.hibernate.ddl-auto=update`

### Production (PostgreSQL)
- URL: `jdbc:postgresql://localhost:5432/pokemon_tcg`
- Driver: `org.postgresql.Driver`
- Dialect: `org.hibernate.dialect.PostgreSQLDialect`
- `spring.jpa.hibernate.ddl-auto=validate`

## Entity Definitions

### BaseEntity

All entities extend `BaseEntity` which provides:

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@MappedSuperclass
public abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

### CardEntity

```java
@Entity
@Table(name = "cards")
public class CardEntity extends BaseEntity {
    @Column(name = "external_id", unique = true, nullable = false, length = 50)
    private String externalId;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "hp")
    private Integer hp;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "types", columnDefinition = "JSON")
    private Set<EnergyType> types;

    @Enumerated(EnumType.STRING)
    @Column(name = "stage", length = 20)
    private PokemonStage stage;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "attacks", columnDefinition = "JSON")
    private List<AttackData> attacks;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "abilities", columnDefinition = "JSON")
    private List<AbilityData> abilities;

    @Column(name = "weakness", length = 50)
    private String weakness;

    @Column(name = "resistance", length = 50)
    private String resistance;

    @Column(name = "retreat_cost")
    private Integer retreatCost;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "rarity", length = 50)
    private String rarity;

    @Column(name = "card_number", length = 20)
    private String cardNumber;

    @Column(name = "set_id", length = 20)
    @Builder.Default
    private String setId = "xy1";
}
```

**Note**: `AttackData` and `AbilityData` should be `@Embeddable` classes or stored as JSON.

### DeckEntity

```java
@Entity
@Table(name = "decks")
public class DeckEntity extends BaseEntity {
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "creator_id", length = 100)
    private String creatorId;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @OneToMany(mappedBy = "deck", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DeckCardEntity> cards = new ArrayList<>();
}
```

### DeckCardEntity

```java
@Entity
@Table(name = "deck_cards", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"deck_id", "card_id"})
})
public class DeckCardEntity extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deck_id", nullable = false)
    private DeckEntity deck;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    private CardEntity card;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;
}
```

### MatchEntity

```java
@Entity
@Table(name = "matches")
public class MatchEntity extends BaseEntity {
    @Column(name = "player1_id", nullable = false, length = 100)
    private String player1Id;

    @Column(name = "player2_id", length = 100)
    private String player2Id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    @Builder.Default
    private MatchStatus status = MatchStatus.WAITING;

    @Column(name = "winner_id", length = 100)
    private String winnerId;
}
```

**Note**: `MatchStatus` is an enum: `WAITING, SETUP, ACTIVE, FINISHED` (distinct from `GamePhase` in engine).

### GameStateEntity

```java
@Entity
@Table(name = "game_states")
public class GameStateEntity extends BaseEntity {
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private MatchEntity match;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "board_state", columnDefinition = "JSON")
    private String boardState;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "action_log", columnDefinition = "JSON")
    private String actionLog;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "turn_data", columnDefinition = "JSON")
    private String turnData;
}
```

**Note**: `boardState`, `actionLog`, and `turnData` are stored as JSON strings. They will be serialized/deserialized using Jackson `ObjectMapper` (already configured with `JavaTimeModule` in `MappersConfig`).

## Enums

All enums are in `ar.edu.utn.frc.tup.piii.enums`:

| Enum | Values |
|---|---|
| `CardType` | `POKEMON, ENERGY, TRAINER` |
| `EnergyType` | `FIRE, WATER, GRASS, LIGHTNING, PSYCHIC, FIGHTING, DARKNESS, METAL, FAIRY, DRAGON, COLORLESS` |
| `GamePhase` | `WAITING, SETUP, DRAW, MAIN, ATTACK, BETWEEN_TURNS, FINISHED` |
| `GameZone` | `HAND, DECK, DISCARD, ACTIVE, BENCH, PRIZE, STADIUM, TOOL, ENERGY` |
| `PokemonStage` | `BASIC, STAGE1, STAGE2, EX, MEGA` |
| `SpecialCondition` | `ASLEEP, BURNED, CONFUSED, PARALYZED, POISONED` |
| `TrainerSubtype` | `ITEM, SUPPORTER, STADIUM, TOOL, ACE_SPEC, PLASMA` |
| `TurnPhase` | `DRAW, ACTIONS, ATTACK, BETWEEN_TURNS` |

All enums should be stored as `STRING` in the database (`@Enumerated(EnumType.STRING)`).

## Database Indexes

Reference `docs/sdd/07-base-datos.md` for the complete index definitions:
- `cards`: idx on `name`, `stage`, `set_id`
- `decks`: idx on `creator_id`
- `deck_cards`: idx on `deck_id`, `card_id`; unique constraint on `(deck_id, card_id)`
- `matches`: idx on `status`, `player1_id`, `player2_id`
- `game_states`: idx on `match_id`

## Repositories

All repositories extend `JpaRepository<EntityName, UUID>`:

```java
@Repository
public interface CardRepository extends JpaRepository<CardEntity, UUID> {
    List<CardEntity> findBySetId(String setId);
    Page<CardEntity> findByStage(PokemonStage stage, Pageable pageable);
    Optional<CardEntity> findByExternalId(String externalId);
    List<CardEntity> findByNameContainingIgnoreCase(String name);
}
```

## DTO Mapping Rules

- Entity → DTO: Use ModelMapper via mapper classes
- DTO → Entity: Use ModelMapper via mapper classes
- For partial updates: Use `mergerMapper` (only maps non-null fields)
- GameState serialization: Use Jackson ObjectMapper to/from JSON strings

## Migrations

Migrations go in `BE/src/main/resources/db/migration/`:
- `V1__create_cards_table.sql`
- `V2__create_decks_tables.sql`
- `V3__create_matches_tables.sql`
- `V4__create_game_states_table.sql`
- `V5__seed_xy1_cards.sql`

For now, H2 auto-creates tables via `ddl-auto=update`. Migrations are for production PostgreSQL.

## Checklist

When implementing entities or DTOs:
- [ ] Entity extends `BaseEntity`
- [ ] Lombok `@Data`, `@Builder`, `@AllArgsConstructor`, `@NoArgsConstructor` present
- [ ] `@Entity` and `@Table(name = "...")` annotations
- [ ] Enum fields use `@Enumerated(EnumType.STRING)`
- [ ] JSON fields use `@JdbcTypeCode(SqlTypes.JSON)` with `columnDefinition = "JSON"`
- [ ] Relationships use correct cascade and fetch types
- [ ] Indexes match the database schema document
- [ ] UUID as primary key type
- [ ] DTO uses validation annotations (`@NotBlank`, etc.) where appropriate
- [ ] Mapper class injects `ModelMapper` and has mapToDto/mapToEntity methods