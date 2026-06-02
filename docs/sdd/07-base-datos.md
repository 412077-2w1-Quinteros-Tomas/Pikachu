# 07 - Base de Datos

## Configuración Actual

### H2 (Desarrollo)

Configurado en `application.properties`:

```properties
spring.datasource.url=jdbc:h2:mem:test;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.show-sql=true
spring.jpa.defer-datasource-initialization=true
```

**Nota:** Se necesita agregar `spring.jpa.hibernate.ddl-auto=update` para que H2 cree las tablas automáticamente durante el desarrollo.

### PostgreSQL (Producción)

Se configurará mediante un perfil de Spring separado:

```properties
# application-prod.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/pokemon_tcg
spring.datasource.driverClassName=org.postgresql.Driver
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=validate
```

## Schema SQL

### Tabla: cards

```sql
CREATE TABLE cards (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    external_id VARCHAR(50) UNIQUE,
    name VARCHAR(200) NOT NULL,
    hp INTEGER,
    types JSON,
    stage VARCHAR(20),
    attacks JSON,
    abilities JSON,
    weakness VARCHAR(50),
    resistance VARCHAR(50),
    retreat_cost INTEGER,
    image_url VARCHAR(500),
    rarity VARCHAR(50),
    card_number VARCHAR(20),
    set_id VARCHAR(20) DEFAULT 'xy1',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_cards_name ON cards(name);
CREATE INDEX idx_cards_stage ON cards(stage);
CREATE INDEX idx_cards_set_id ON cards(set_id);
```

### Tabla: decks

```sql
CREATE TABLE decks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    creator_id VARCHAR(100),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_decks_creator ON decks(creator_id);
```

### Tabla: deck_cards

```sql
CREATE TABLE deck_cards (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    deck_id UUID NOT NULL REFERENCES decks(id) ON DELETE CASCADE,
    card_id UUID NOT NULL REFERENCES cards(id),
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    UNIQUE(deck_id, card_id)
);

CREATE INDEX idx_deck_cards_deck ON deck_cards(deck_id);
CREATE INDEX idx_deck_cards_card ON deck_cards(card_id);
```

### Tabla: matches

```sql
CREATE TABLE matches (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    player1_id VARCHAR(100) NOT NULL,
    player2_id VARCHAR(100),
    status VARCHAR(20) DEFAULT 'WAITING',
    winner_id VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_matches_status ON matches(status);
CREATE INDEX idx_matches_player1 ON matches(player1_id);
CREATE INDEX idx_matches_player2 ON matches(player2_id);
```

### Tabla: game_states

```sql
CREATE TABLE game_states (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    match_id UUID NOT NULL REFERENCES matches(id) ON DELETE CASCADE,
    board_state JSON,
    action_log JSON,
    turn_data JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_game_states_match ON game_states(match_id);
```

## Migraciones Versionadas

Las migraciones se crearán en `BE/src/main/resources/db/migration/` durante la semana 6:

```
db/migration/
├── V1__create_cards_table.sql
├── V2__create_decks_tables.sql
├── V3__create_matches_tables.sql
├── V4__create_game_states_table.sql
└── V5__seed_xy1_cards.sql
```

## Relación con la API Externa

Las cartas se sincronizan desde pokemontcg.io una sola vez al inicio:

1. El endpoint `POST /api/cards/sync` llama a la API externa
2. Se obtienen todas las cartas del set `xy1`
3. Se insertan en la tabla `cards` con `external_id` como identificador externo
4. Las consultas posteriores usan la DB local, no la API externa

**API Key:** Se configura mediante variable de entorno `POKEMON_TCG_API_KEY`. Nunca se commitea al repositorio.

```properties
# application.properties
pokemon.api.key=${POKEMON_TCG_API_KEY}
```

```
# .env (NO commitear)
POKEMON_TCG_API_KEY=<tu-api-key>
```

## Índices

| Tabla | Índice | Propósito |
|---|---|---|
| `cards` | `idx_cards_name` | Búsqueda por nombre |
| `cards` | `idx_cards_stage` | Filtrado por stage |
| `cards` | `idx_cards_set_id` | Filtrado por set |
| `decks` | `idx_decks_creator` | Mazos por jugador |
| `deck_cards` | `idx_deck_cards_deck` | Cartas de un mazo |
| `deck_cards` | `idx_deck_cards_card` | Mazos que contienen una carta |
| `matches` | `idx_matches_status` | Partidas por estado (lobby) |
| `matches` | `idx_matches_player1` | Partidas del jugador 1 |
| `matches` | `idx_matches_player2` | Partidas del jugador 2 |
| `game_states` | `idx_game_states_match` | Estado de una partida |
