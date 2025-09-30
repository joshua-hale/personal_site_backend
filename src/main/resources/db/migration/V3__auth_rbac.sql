-- Users: core identity (no single role column here)
CREATE TABLE IF NOT EXISTS users (

    id             BIGSERIAL PRIMARY KEY,
    email          VARCHAR(255) UNIQUE NOT NULL,
    username       VARCHAR(100) UNIQUE NOT NULL,
    password_hash  VARCHAR(255) NOT NULL,

    is_active      BOOLEAN NOT NULL DEFAULT TRUE,
    last_login_at  TIMESTAMPTZ NULL,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
    );

-- Roles: catalog of roles (USER, ADMIN, etc.)
CREATE TABLE IF NOT EXISTS roles (
    id    BIGSERIAL PRIMARY KEY,
    name  VARCHAR(100) UNIQUE NOT NULL
    );

-- Join table: many-to-many between users and roles
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
    );

-- Helpful indexes for lookups
CREATE INDEX IF NOT EXISTS idx_user_roles_user ON user_roles (user_id);
CREATE INDEX IF NOT EXISTS idx_user_roles_role ON user_roles (role_id);

-- Sessions for cookie-based auth (recommended)
CREATE TABLE IF NOT EXISTS sessions (
    id             BIGSERIAL PRIMARY KEY,
    user_id        BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    session_token  VARCHAR(255) UNIQUE NOT NULL,
    expires_at     TIMESTAMPTZ NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
    );

-- Seed basic roles
INSERT INTO roles (name)
VALUES ('USER'), ('ADMIN')
    ON CONFLICT (name) DO NOTHING;
