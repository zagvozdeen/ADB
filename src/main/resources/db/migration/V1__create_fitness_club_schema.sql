CREATE TYPE attendance_status AS ENUM ('PENDING', 'ATTENDED', 'ABSENT');

CREATE TABLE IF NOT EXISTS clients
(
    id         SERIAL PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name  VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS memberships
(
    id          SERIAL PRIMARY KEY,
    client_id   INTEGER     NOT NULL UNIQUE REFERENCES clients (id) ON DELETE RESTRICT,
    valid_from  TIMESTAMPTZ NOT NULL,
    valid_until TIMESTAMPTZ NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT memberships_valid_period CHECK (valid_until > valid_from)
);

CREATE TABLE IF NOT EXISTS trainers
(
    id         SERIAL PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name  VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS training_sessions
(
    id         SERIAL PRIMARY KEY,
    trainer_id INTEGER      NOT NULL REFERENCES trainers (id) ON DELETE RESTRICT,
    title      VARCHAR(255) NOT NULL,
    starts_at  TIMESTAMPTZ  NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS training_sessions_trainer_id_idx ON training_sessions (trainer_id);

CREATE TABLE IF NOT EXISTS bookings
(
    id                SERIAL PRIMARY KEY,
    client_id         INTEGER           NOT NULL REFERENCES clients (id) ON DELETE RESTRICT,
    session_id        INTEGER           NOT NULL REFERENCES training_sessions (id) ON DELETE RESTRICT,
    attendance_status attendance_status NOT NULL DEFAULT 'PENDING',
    created_at        TIMESTAMPTZ       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMPTZ       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version           BIGINT            NOT NULL DEFAULT 0,
    CONSTRAINT bookings_client_session_key UNIQUE (client_id, session_id)
);
CREATE INDEX IF NOT EXISTS bookings_session_id_idx ON bookings (session_id);
