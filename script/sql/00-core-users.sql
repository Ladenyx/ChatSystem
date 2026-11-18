-- Canonical shared users anchor and PostgreSQL environment.
SET LOCAL search_path = public;
SET LOCAL TIME ZONE 'UTC';
-- Common PostgreSQL 18 environment. Included within bootstrap transaction.
DO $$
BEGIN
    IF current_setting('server_version_num')::integer NOT BETWEEN 180000 AND 189999 THEN
        RAISE EXCEPTION 'This schema targets PostgreSQL 18';
    END IF;
END $$;

CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE EXTENSION IF NOT EXISTS unaccent;

-- Canonical shared users anchor. All domains reference public.users(id).
-- Coordinate changes to this file; do not create private copies in domain scripts.
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username TEXT NOT NULL,
    email TEXT NOT NULL,
    full_name TEXT NOT NULL,
    address TEXT,
    date_of_birth DATE,
    gender TEXT,
    role TEXT NOT NULL DEFAULT 'USER',
    status TEXT NOT NULL DEFAULT 'ACTIVE',
    status_before_delete TEXT,
    password_hash TEXT,
    email_verified_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT users_username_check CHECK (username COLLATE "C" ~ '^[A-Za-z0-9_-]{6,30}$'),
    CONSTRAINT users_email_check CHECK (email = btrim(email) AND length(email) BETWEEN 1 AND 254
        AND email ~ '^[^@[:space:]]+@[^@.[:space:]]+(\.[^@.[:space:]]+)*$'),
    CONSTRAINT users_name_check CHECK (full_name = btrim(full_name, E' \t\n\r\f\v') AND length(full_name) BETWEEN 1 AND 100),
    CONSTRAINT users_address_check CHECK (address = btrim(address, E' \t\n\r\f\v') AND length(address) <= 255),
    CONSTRAINT users_birth_date_check CHECK (isfinite(date_of_birth)
        AND date_of_birth <= (CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Ho_Chi_Minh')::date),
    CONSTRAINT users_gender_check CHECK (gender IN ('Nam', 'Nữ', 'Khác')),
    CONSTRAINT users_password_hash_check CHECK (password_hash ~ '[^[:space:]]'),
    CONSTRAINT users_created_at_check CHECK (isfinite(created_at)),
    CONSTRAINT users_email_verified_at_check CHECK (isfinite(email_verified_at)),
    CONSTRAINT users_role_check CHECK (role IN ('USER', 'ADMIN')),
    CONSTRAINT users_status_check CHECK (status IN ('ACTIVE', 'LOCKED', 'DELETED')),
    CONSTRAINT users_deleted_check CHECK (
        (status = 'DELETED' AND deleted_at IS NOT NULL AND isfinite(deleted_at)
            AND deleted_at >= created_at AND status_before_delete IS NOT NULL
            AND status_before_delete IN ('ACTIVE', 'LOCKED'))
        OR (status <> 'DELETED' AND deleted_at IS NULL AND status_before_delete IS NULL)
    )
);
CREATE UNIQUE INDEX users_username_uq ON users (lower(username));
CREATE UNIQUE INDEX users_email_uq ON users (lower(email));
CREATE INDEX users_name_trgm_idx ON users USING GIN (lower(full_name) gin_trgm_ops);

-- Account IDs and usernames are permanent; a new account after purge gets a new ID.
CREATE FUNCTION protect_user_identity() RETURNS TRIGGER
LANGUAGE plpgsql AS $$
BEGIN
    IF NEW.id IS DISTINCT FROM OLD.id OR NEW.username IS DISTINCT FROM OLD.username THEN
        RAISE EXCEPTION 'Account ID and username are immutable'
            USING ERRCODE = '23514', CONSTRAINT = 'users_identity_immutable';
    END IF;
    RETURN NEW;
END $$;
CREATE TRIGGER users_protect_identity BEFORE UPDATE OF id, username ON users
FOR EACH ROW EXECUTE FUNCTION protect_user_identity();
