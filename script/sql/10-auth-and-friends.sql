-- Thanh vien 2: Auth/Sessions and Friends/Blocks.
-- users is defined only in 00-core-users.sql.
-- Transaction control belongs to schema.sql/the isolated test runner.
-- Cross-table workflow, current-time eligibility, throttling and credential verification
-- belong to the server transaction; these CHECKs validate stored row consistency.

CREATE TABLE pending_registrations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username TEXT NOT NULL,
    email TEXT NOT NULL,
    password_hash TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMPTZ NOT NULL DEFAULT (CURRENT_TIMESTAMP + INTERVAL '30 minutes'),
    email_verified_at TIMESTAMPTZ,
    cancelled_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    CONSTRAINT pending_registrations_username_check CHECK (
        username COLLATE "C" ~ '^[A-Za-z0-9_-]{6,30}$'),
    CONSTRAINT pending_registrations_email_check CHECK (
        email = btrim(email) AND length(email) BETWEEN 1 AND 254
        AND email ~ '^[^@[:space:]]+@[^@.[:space:]]+(\.[^@.[:space:]]+)*$'),
    CONSTRAINT pending_registrations_password_hash_check CHECK (password_hash ~ '[^[:space:]]'),
    CONSTRAINT pending_registrations_expiry_check CHECK (
        isfinite(created_at) AND expires_at = created_at + INTERVAL '30 minutes'),
    CONSTRAINT pending_registrations_verification_check CHECK (
        email_verified_at >= created_at AND email_verified_at < expires_at),
    CONSTRAINT pending_registrations_cancellation_check CHECK (
        isfinite(cancelled_at) AND cancelled_at >= created_at),
    CONSTRAINT pending_registrations_completion_check CHECK (
        completed_at IS NULL OR (email_verified_at IS NOT NULL
            AND completed_at >= email_verified_at AND completed_at < expires_at)),
    CONSTRAINT pending_registrations_end_check CHECK (cancelled_at IS NULL OR completed_at IS NULL)
);
-- Deliberately non-unique: separate attempts may reuse an email/username.
CREATE INDEX pending_registrations_email_idx ON pending_registrations (lower(email));
CREATE INDEX pending_registrations_expiry_idx ON pending_registrations (expires_at);

CREATE TABLE email_verification_codes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    purpose TEXT NOT NULL,
    pending_registration_id UUID REFERENCES pending_registrations(id) ON DELETE CASCADE,
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    target_email TEXT NOT NULL,
    code_verifier TEXT NOT NULL,
    issued_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMPTZ NOT NULL DEFAULT (CURRENT_TIMESTAMP + INTERVAL '10 minutes'),
    failed_attempts INTEGER NOT NULL DEFAULT 0,
    consumed_at TIMESTAMPTZ,
    invalidated_at TIMESTAMPTZ,
    CONSTRAINT email_verification_codes_context_check CHECK (
        (purpose = 'REGISTRATION' AND pending_registration_id IS NOT NULL AND user_id IS NULL)
        OR (purpose IN ('ACCOUNT_EMAIL', 'EMAIL_CHANGE')
            AND user_id IS NOT NULL AND pending_registration_id IS NULL)),
    CONSTRAINT email_verification_codes_email_check CHECK (
        target_email = btrim(target_email) AND length(target_email) BETWEEN 1 AND 254
        AND target_email ~ '^[^@[:space:]]+@[^@.[:space:]]+(\.[^@.[:space:]]+)*$'),
    CONSTRAINT email_verification_codes_verifier_check CHECK (code_verifier ~ '[^[:space:]]'),
    CONSTRAINT email_verification_codes_expiry_check CHECK (
        isfinite(issued_at) AND expires_at = issued_at + INTERVAL '10 minutes'),
    CONSTRAINT email_verification_codes_attempts_check CHECK (failed_attempts BETWEEN 0 AND 5),
    CONSTRAINT email_verification_codes_consumption_check CHECK (
        consumed_at IS NULL OR (consumed_at >= issued_at AND consumed_at < expires_at
            AND failed_attempts < 5 AND invalidated_at IS NULL)),
    CONSTRAINT email_verification_codes_invalidation_check CHECK (
        isfinite(invalidated_at) AND invalidated_at >= issued_at)
);
CREATE INDEX email_verification_codes_registration_idx
    ON email_verification_codes (pending_registration_id, issued_at DESC)
    WHERE pending_registration_id IS NOT NULL;
CREATE INDEX email_verification_codes_user_idx
    ON email_verification_codes (user_id, issued_at DESC) WHERE user_id IS NOT NULL;
CREATE INDEX email_verification_codes_expiry_idx ON email_verification_codes (expires_at);

CREATE TABLE password_reset_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    purpose TEXT NOT NULL,
    temporary_password_hash TEXT NOT NULL,
    issued_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMPTZ NOT NULL DEFAULT (CURRENT_TIMESTAMP + INTERVAL '15 minutes'),
    authenticated_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    revoked_at TIMESTAMPTZ,
    CONSTRAINT password_reset_tokens_purpose_check CHECK (
        purpose IN ('INITIAL_SETUP', 'USER_RESET', 'ADMIN_RESET')),
    CONSTRAINT password_reset_tokens_hash_check CHECK (temporary_password_hash ~ '[^[:space:]]'),
    CONSTRAINT password_reset_tokens_expiry_check CHECK (
        isfinite(issued_at) AND expires_at = issued_at + INTERVAL '15 minutes'),
    CONSTRAINT password_reset_tokens_authentication_check CHECK (
        authenticated_at >= issued_at AND authenticated_at < expires_at),
    CONSTRAINT password_reset_tokens_completion_check CHECK (
        completed_at IS NULL OR (authenticated_at IS NOT NULL
            AND completed_at >= authenticated_at AND completed_at < expires_at AND revoked_at IS NULL)),
    CONSTRAINT password_reset_tokens_revocation_check CHECK (
        isfinite(revoked_at) AND revoked_at >= issued_at
            AND (authenticated_at IS NULL OR revoked_at >= authenticated_at))
);
CREATE INDEX password_reset_tokens_user_idx ON password_reset_tokens (user_id, issued_at DESC);
CREATE INDEX password_reset_tokens_expiry_idx ON password_reset_tokens (expires_at);

CREATE TABLE user_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    scope TEXT NOT NULL,
    token_hash TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    revoked_at TIMESTAMPTZ,
    CONSTRAINT user_sessions_scope_check CHECK (scope IN ('USER_CLIENT', 'ADMIN_APP')),
    CONSTRAINT user_sessions_token_hash_check CHECK (token_hash ~ '[^[:space:]]'),
    CONSTRAINT user_sessions_token_hash_uq UNIQUE (token_hash),
    CONSTRAINT user_sessions_created_at_check CHECK (isfinite(created_at)),
    CONSTRAINT user_sessions_revocation_check CHECK (isfinite(revoked_at) AND revoked_at >= created_at)
);
CREATE INDEX user_sessions_user_idx ON user_sessions (user_id, created_at DESC);
-- No expiry policy was approved. Retain revoked history; allow multiple User sessions.
CREATE UNIQUE INDEX user_sessions_one_live_admin_uq ON user_sessions (user_id)
    WHERE scope = 'ADMIN_APP' AND revoked_at IS NULL;

-- One request per send attempt. The client supplies a stable id for retries.
CREATE TABLE friend_requests (
    id UUID PRIMARY KEY,
    sender_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    recipient_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    status TEXT NOT NULL DEFAULT 'PENDING',
    cancel_reason TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMPTZ,
    CONSTRAINT friend_requests_distinct_users_check CHECK (sender_id <> recipient_id),
    CONSTRAINT friend_requests_status_check CHECK (
        status IN ('PENDING', 'ACCEPTED', 'REJECTED', 'CANCELLED')),
    CONSTRAINT friend_requests_created_at_check CHECK (isfinite(created_at)),
    CONSTRAINT friend_requests_resolution_check CHECK (
        (status = 'PENDING' AND resolved_at IS NULL AND cancel_reason IS NULL)
        OR (status IN ('ACCEPTED', 'REJECTED') AND resolved_at IS NOT NULL
            AND isfinite(resolved_at) AND resolved_at >= created_at AND cancel_reason IS NULL)
        OR (status = 'CANCELLED' AND resolved_at IS NOT NULL
            AND isfinite(resolved_at) AND resolved_at >= created_at
            AND cancel_reason IS NOT NULL
            AND cancel_reason IN ('SENDER_WITHDREW', 'BLOCKED'))
    )
);
-- A pending request excludes a second request in either direction.
CREATE UNIQUE INDEX friend_requests_pending_pair_uq ON friend_requests (
    LEAST(sender_id, recipient_id), GREATEST(sender_id, recipient_id))
    WHERE status = 'PENDING';
CREATE INDEX friend_requests_recipient_pending_idx ON friend_requests (recipient_id, created_at DESC)
    WHERE status = 'PENDING';
CREATE INDEX friend_requests_sender_pending_idx ON friend_requests (sender_id, created_at DESC)
    WHERE status = 'PENDING';

-- A canonical unordered pair represents a two-way friendship.
CREATE TABLE friendships (
    user_low_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    user_high_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_low_id, user_high_id),
    CONSTRAINT friendships_order_check CHECK (user_low_id < user_high_id),
    CONSTRAINT friendships_created_at_check CHECK (isfinite(created_at))
);
CREATE INDEX friendships_user_high_idx ON friendships (user_high_id, user_low_id);

-- Blocks are directional. Either direction suppresses direct interaction in server logic.
CREATE TABLE blocks (
    blocker_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    blocked_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (blocker_id, blocked_id),
    CONSTRAINT blocks_distinct_users_check CHECK (blocker_id <> blocked_id),
    CONSTRAINT blocks_created_at_check CHECK (isfinite(created_at))
);
CREATE INDEX blocks_blocked_idx ON blocks (blocked_id, blocker_id);

-- Account status, request transitions, and block/friendship atomicity are server rules.
-- Soft deletion preserves rows; physical account purge cascades through these FKs.
