-- V1: Schema inicial del Pokémon TCG
-- Compatible con PostgreSQL

CREATE TABLE IF NOT EXISTS card (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at  TIMESTAMP NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP NOT NULL DEFAULT now(),
    external_id VARCHAR(50)  UNIQUE NOT NULL,
    name        VARCHAR(200) NOT NULL,
    card_type   VARCHAR(20)  NOT NULL,
    hp          INTEGER,
    stage       VARCHAR(20),
    types       TEXT,
    attacks     TEXT,
    abilities   TEXT,
    weakness    VARCHAR(50),
    resistance  VARCHAR(50),
    retreat_cost INTEGER DEFAULT 0,
    image_url   TEXT,
    rarity      VARCHAR(100),
    card_number VARCHAR(20),
    set_id      VARCHAR(20) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_card_set_id ON card(set_id);
CREATE INDEX IF NOT EXISTS idx_card_name   ON card(name);
CREATE INDEX IF NOT EXISTS idx_card_type   ON card(card_type);

CREATE TABLE IF NOT EXISTS deck (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at  TIMESTAMP NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP NOT NULL DEFAULT now(),
    name        VARCHAR(200) NOT NULL,
    description TEXT
);

CREATE TABLE IF NOT EXISTS deck_card (
    id       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    deck_id  UUID NOT NULL REFERENCES deck(id) ON DELETE CASCADE,
    card_id  UUID NOT NULL REFERENCES card(id),
    quantity INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT uq_deck_card UNIQUE (deck_id, card_id)
);

CREATE TABLE IF NOT EXISTS match_entity (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at  TIMESTAMP NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP NOT NULL DEFAULT now(),
    name        VARCHAR(200),
    player1     VARCHAR(100) NOT NULL,
    player2     VARCHAR(100),
    deck1_id    UUID REFERENCES deck(id),
    deck2_id    UUID REFERENCES deck(id),
    status      VARCHAR(20) NOT NULL DEFAULT 'WAITING'
);

CREATE TABLE IF NOT EXISTS game_state (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at     TIMESTAMP NOT NULL DEFAULT now(),
    updated_at     TIMESTAMP NOT NULL DEFAULT now(),
    match_id       UUID NOT NULL UNIQUE REFERENCES match_entity(id) ON DELETE CASCADE,
    state_json     TEXT,
    turn_number    INTEGER DEFAULT 0,
    current_player VARCHAR(100)
);
