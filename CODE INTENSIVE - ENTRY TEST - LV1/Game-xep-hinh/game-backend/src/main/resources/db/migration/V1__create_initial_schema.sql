CREATE TABLE players (
    id UUID PRIMARY KEY,
    nickname VARCHAR(30) NOT NULL,
    recovery_code_hash VARCHAR(64) NOT NULL UNIQUE,
    ip_hmac VARCHAR(64) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('ACTIVE', 'BLOCKED')),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    last_active_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE player_devices (
    id UUID PRIMARY KEY,
    player_id UUID NOT NULL REFERENCES players(id),
    device_token_hash VARCHAR(64) NOT NULL UNIQUE,
    user_agent VARCHAR(500),
    expires_at TIMESTAMPTZ NOT NULL,
    last_used_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ
);

CREATE TABLE game_sessions (
    id UUID PRIMARY KEY,
    player_id UUID NOT NULL REFERENCES players(id),
    initial_board JSONB NOT NULL,
    current_board JSONB NOT NULL,
    moves JSONB NOT NULL DEFAULT '[]'::jsonb,
    move_count INTEGER NOT NULL DEFAULT 0 CHECK (move_count >= 0),
    active_elapsed_seconds INTEGER NOT NULL DEFAULT 0 CHECK (active_elapsed_seconds >= 0),
    status VARCHAR(20) NOT NULL CHECK (status IN ('PLAYING', 'SAVED', 'WON', 'ENDED', 'ABANDONED')),
    validation_status VARCHAR(20) NOT NULL CHECK (validation_status IN ('PENDING', 'VALID', 'INVALID')),
    started_at TIMESTAMPTZ NOT NULL,
    saved_at TIMESTAMPTZ,
    resumed_at TIMESTAMPTZ,
    finished_at TIMESTAMPTZ,
    last_activity_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE admins (
    id UUID PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'ADMIN',
    status VARCHAR(20) NOT NULL CHECK (status IN ('ACTIVE', 'BLOCKED')),
    last_login_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE admin_audit_logs (
    id UUID PRIMARY KEY,
    admin_id UUID NOT NULL REFERENCES admins(id),
    action VARCHAR(50) NOT NULL,
    target_type VARCHAR(30) NOT NULL,
    target_id UUID NOT NULL,
    reason TEXT NOT NULL,
    metadata JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE UNIQUE INDEX uq_player_active_game
    ON game_sessions(player_id)
    WHERE status IN ('PLAYING', 'SAVED');

CREATE INDEX idx_game_player_created
    ON game_sessions(player_id, created_at DESC);

CREATE INDEX idx_game_leaderboard
    ON game_sessions(active_elapsed_seconds, move_count, finished_at)
    WHERE status = 'WON' AND validation_status = 'VALID';

CREATE INDEX idx_game_finished_at
    ON game_sessions(finished_at DESC)
    WHERE status = 'WON' AND validation_status = 'VALID';
