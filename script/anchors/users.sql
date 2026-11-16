-- Canonical shared users anchor. All domains reference public.users(id).
-- Coordinate changes to this file; do not create private copies in domain scripts.
-- Shared identity/profile dependency. Authentication tables are a subsequent iteration.
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username TEXT NOT NULL,
    email TEXT NOT NULL,
    full_name TEXT NOT NULL,
    role TEXT NOT NULL DEFAULT 'USER',
    status TEXT NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT users_username_check CHECK (username ~ '^[A-Za-z0-9_-]{6,30}$'),
    CONSTRAINT users_email_check CHECK (email = btrim(email) AND length(email) BETWEEN 1 AND 254),
    CONSTRAINT users_name_check CHECK (full_name = btrim(full_name) AND length(full_name) BETWEEN 1 AND 100),
    CONSTRAINT users_role_check CHECK (role IN ('USER', 'ADMIN')),
    CONSTRAINT users_status_check CHECK (status IN ('ACTIVE', 'LOCKED', 'DELETED')),
    CONSTRAINT users_deleted_check CHECK (
        (status = 'DELETED' AND deleted_at IS NOT NULL AND deleted_at >= created_at)
        OR (status <> 'DELETED' AND deleted_at IS NULL)
    )
);
CREATE UNIQUE INDEX users_username_uq ON users (lower(username));
CREATE UNIQUE INDEX users_email_uq ON users (lower(email));
CREATE INDEX users_name_trgm_idx ON users USING GIN (lower(full_name) gin_trgm_ops);

