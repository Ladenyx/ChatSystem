-- Thanh vien 1: Conversations, Messages, E2EE and Spam (U-12..U-26, A-08..A-11).
-- Included inside the caller transaction; public.users(id) is the shared anchor.
-- These constraints validate storage, not sessions, permissions or cryptographic proofs.

-- Conversations and self-deletion markers. Recall keeps the message row, without its payload.
CREATE TABLE conversations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    type TEXT NOT NULL,
    group_name TEXT,
    created_by_user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    closed_at TIMESTAMPTZ,
    CONSTRAINT conversations_type_check CHECK (type IN ('DIRECT', 'GROUP')),
    CONSTRAINT conversations_name_check CHECK (
        (type = 'DIRECT' AND group_name IS NULL)
        OR (type = 'GROUP' AND group_name IS NOT NULL AND group_name ~ '[^[:space:]]')
    ),
    CONSTRAINT conversations_closed_check CHECK (closed_at IS NULL OR closed_at >= created_at)
);
CREATE INDEX conversations_creator_idx ON conversations (created_by_user_id);
CREATE INDEX conversations_group_name_idx ON conversations USING GIN (lower(group_name) gin_trgm_ops)
    WHERE type = 'GROUP';

CREATE TABLE conversation_members (
    conversation_id UUID NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role TEXT NOT NULL DEFAULT 'MEMBER',
    joined_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    left_at TIMESTAMPTZ,
    PRIMARY KEY (conversation_id, user_id),
    CONSTRAINT conversation_members_role_check CHECK (role IN ('ADMIN', 'MEMBER')),
    CONSTRAINT conversation_members_left_check CHECK (left_at IS NULL OR left_at >= joined_at)
);
CREATE INDEX conversation_members_user_idx ON conversation_members (user_id, conversation_id);

CREATE TABLE messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id UUID NOT NULL REFERENCES conversations(id) ON DELETE RESTRICT,
    sender_user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    content_plain TEXT,
    content_cipher BYTEA,
    crypto_metadata JSONB,
    recalled_at TIMESTAMPTZ,
    CONSTRAINT messages_recall_time_check CHECK (
        recalled_at IS NULL OR recalled_at BETWEEN created_at AND created_at + INTERVAL '48 hours'
    ),
    CONSTRAINT messages_payload_check CHECK (
        (recalled_at IS NOT NULL AND content_plain IS NULL AND content_cipher IS NULL AND crypto_metadata IS NULL)
        OR (recalled_at IS NULL AND (
            (content_plain IS NOT NULL AND length(content_plain) > 0 AND content_cipher IS NULL AND crypto_metadata IS NULL)
            OR (content_plain IS NULL AND content_cipher IS NOT NULL AND octet_length(content_cipher) > 0
                AND crypto_metadata IS NOT NULL AND jsonb_typeof(crypto_metadata) = 'object')
        ))
    )
);
CREATE INDEX messages_history_idx ON messages (conversation_id, created_at DESC, id DESC);
CREATE INDEX messages_sender_idx ON messages (sender_user_id, created_at);
CREATE INDEX messages_plain_trgm_idx ON messages USING GIN (content_plain gin_trgm_ops)
    WHERE recalled_at IS NULL AND content_plain IS NOT NULL;

-- A cross-table payload invariant cannot be expressed by a row CHECK alone.
CREATE FUNCTION check_message_conversation_type() RETURNS TRIGGER
LANGUAGE plpgsql AS $$
DECLARE
    conversation_type TEXT;
BEGIN
    SELECT type INTO conversation_type FROM conversations WHERE id = NEW.conversation_id;
    IF FOUND AND NEW.recalled_at IS NULL AND (
        (conversation_type = 'GROUP' AND NEW.content_plain IS NOT NULL)
        OR (conversation_type = 'DIRECT' AND NEW.content_cipher IS NOT NULL)
    ) THEN
        RAISE EXCEPTION USING ERRCODE = '23514', CONSTRAINT = 'messages_conversation_payload_check',
            MESSAGE = 'Message payload must match its conversation type';
    END IF;
    RETURN NEW;
END $$;
CREATE TRIGGER messages_conversation_payload_trigger
    BEFORE INSERT OR UPDATE ON messages FOR EACH ROW EXECUTE FUNCTION check_message_conversation_type();

CREATE FUNCTION keep_conversation_type() RETURNS TRIGGER
LANGUAGE plpgsql AS $$
BEGIN
    IF NEW.type IS DISTINCT FROM OLD.type THEN
        RAISE EXCEPTION USING ERRCODE = '23514', CONSTRAINT = 'conversations_immutable_type_check',
            MESSAGE = 'Conversation type is immutable';
    END IF;
    RETURN NEW;
END $$;
CREATE TRIGGER conversations_type_trigger
    BEFORE UPDATE OF type ON conversations FOR EACH ROW EXECUTE FUNCTION keep_conversation_type();

CREATE TABLE message_deletions (
    message_id UUID NOT NULL REFERENCES messages(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    deleted_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (message_id, user_id)
);
CREATE INDEX message_deletions_user_idx ON message_deletions (user_id, message_id);

-- DEC-05: public device data, encrypted backups and wrapped group keys only.
-- Proof validation, key distribution, rotation and retention jobs belong to the server/client.
CREATE TABLE e2ee_devices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    encryption_public_key BYTEA NOT NULL,
    signing_public_key BYTEA NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    trusted_at TIMESTAMPTZ,
    trust_method TEXT,
    approved_by_device_id UUID,
    trust_proof JSONB,
    revoked_at TIMESTAMPTZ,
    CONSTRAINT e2ee_devices_owner_id_uq UNIQUE (user_id, id),
    CONSTRAINT e2ee_devices_approver_fk FOREIGN KEY (user_id, approved_by_device_id)
        REFERENCES e2ee_devices(user_id, id) ON DELETE NO ACTION,
    CONSTRAINT e2ee_devices_keys_check CHECK (
        octet_length(encryption_public_key) > 0 AND octet_length(signing_public_key) > 0
    ),
    CONSTRAINT e2ee_devices_times_check CHECK (
        (trusted_at IS NULL OR trusted_at >= created_at)
        AND (revoked_at IS NULL OR revoked_at >= created_at)
        AND (trusted_at IS NULL OR revoked_at IS NULL OR revoked_at >= trusted_at)
    ),
    CONSTRAINT e2ee_devices_trust_check CHECK (
        (trusted_at IS NULL AND trust_method IS NULL AND approved_by_device_id IS NULL AND trust_proof IS NULL)
        OR (trusted_at IS NOT NULL AND trust_method IS NOT NULL AND (
            (trust_method = 'INITIAL' AND approved_by_device_id IS NULL AND trust_proof IS NULL)
            OR (trust_method = 'TRUSTED_DEVICE' AND approved_by_device_id IS NOT NULL
                AND approved_by_device_id <> id AND trust_proof IS NOT NULL AND jsonb_typeof(trust_proof) = 'object')
            OR (trust_method = 'RECOVERY' AND approved_by_device_id IS NULL
                AND trust_proof IS NOT NULL AND jsonb_typeof(trust_proof) = 'object')
        ))
    )
);
CREATE INDEX e2ee_devices_approver_idx ON e2ee_devices (user_id, approved_by_device_id);

CREATE TABLE e2ee_key_backups (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    revision BIGINT NOT NULL,
    encrypted_payload BYTEA NOT NULL,
    crypto_metadata JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    replaced_at TIMESTAMPTZ,
    CONSTRAINT e2ee_key_backups_revision_uq UNIQUE (user_id, revision),
    CONSTRAINT e2ee_key_backups_revision_check CHECK (revision > 0),
    CONSTRAINT e2ee_key_backups_payload_check CHECK (
        octet_length(encrypted_payload) > 0 AND jsonb_typeof(crypto_metadata) = 'object'
    ),
    CONSTRAINT e2ee_key_backups_replaced_check CHECK (replaced_at IS NULL OR replaced_at >= created_at)
);
CREATE UNIQUE INDEX e2ee_key_backups_current_uq ON e2ee_key_backups (user_id) WHERE replaced_at IS NULL;

CREATE TABLE e2ee_group_key_epochs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id UUID NOT NULL REFERENCES conversations(id) ON DELETE RESTRICT,
    epoch_no BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    retired_at TIMESTAMPTZ,
    CONSTRAINT e2ee_group_key_epochs_number_uq UNIQUE (conversation_id, epoch_no),
    CONSTRAINT e2ee_group_key_epochs_number_check CHECK (epoch_no > 0),
    CONSTRAINT e2ee_group_key_epochs_retired_check CHECK (retired_at IS NULL OR retired_at >= created_at)
);

CREATE FUNCTION check_epoch_group() RETURNS TRIGGER
LANGUAGE plpgsql AS $$
BEGIN
    IF EXISTS (SELECT FROM conversations WHERE id = NEW.conversation_id AND type <> 'GROUP') THEN
        RAISE EXCEPTION USING ERRCODE = '23514', CONSTRAINT = 'e2ee_group_key_epochs_group_check',
            MESSAGE = 'A group key epoch must reference a GROUP conversation';
    END IF;
    RETURN NEW;
END $$;
CREATE TRIGGER e2ee_group_key_epochs_group_trigger
    BEFORE INSERT OR UPDATE ON e2ee_group_key_epochs FOR EACH ROW EXECUTE FUNCTION check_epoch_group();

CREATE TABLE e2ee_group_key_envelopes (
    epoch_id UUID NOT NULL REFERENCES e2ee_group_key_epochs(id) ON DELETE RESTRICT,
    recipient_device_id UUID NOT NULL REFERENCES e2ee_devices(id) ON DELETE CASCADE,
    issued_by_device_id UUID REFERENCES e2ee_devices(id) ON DELETE SET NULL,
    wrapped_key BYTEA NOT NULL,
    crypto_metadata JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (epoch_id, recipient_device_id),
    CONSTRAINT e2ee_group_key_envelopes_payload_check CHECK (
        octet_length(wrapped_key) > 0 AND jsonb_typeof(crypto_metadata) = 'object'
    )
);
CREATE INDEX e2ee_group_key_envelopes_recipient_idx ON e2ee_group_key_envelopes (recipient_device_id, epoch_id);
CREATE INDEX e2ee_group_key_envelopes_issuer_idx ON e2ee_group_key_envelopes (issued_by_device_id);

-- Active reports retain identity links. Restrict deletion until DEC-10 archival
-- and tombstone creation have been completed in the same server transaction.
CREATE TABLE spam_reports (
    id UUID PRIMARY KEY,
    reporter_user_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    reported_user_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    conversation_id UUID NOT NULL REFERENCES conversations(id) ON DELETE RESTRICT,
    reason TEXT NOT NULL,
    received_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    request_fingerprint BYTEA NOT NULL,
    review_status TEXT NOT NULL DEFAULT 'PENDING',
    reviewed_by_user_id UUID REFERENCES users(id) ON DELETE RESTRICT,
    reviewed_at TIMESTAMPTZ,
    review_note TEXT,
    CONSTRAINT spam_reports_parties_check CHECK (reporter_user_id <> reported_user_id),
    CONSTRAINT spam_reports_reason_check CHECK (reason ~ '[^[:space:]]'),
    CONSTRAINT spam_reports_fingerprint_check CHECK (octet_length(request_fingerprint) = 32),
    CONSTRAINT spam_reports_review_check CHECK (
        (review_status = 'PENDING' AND reviewed_by_user_id IS NULL AND reviewed_at IS NULL AND review_note IS NULL)
        OR (review_status = 'REVIEWED' AND reviewed_by_user_id IS NOT NULL AND reviewed_at IS NOT NULL
            AND reviewed_at >= received_at AND reviewed_at < received_at + INTERVAL '4320 hours'
            AND review_note IS NOT NULL AND review_note ~ '[^[:space:]]')
    )
);
CREATE INDEX spam_reports_received_idx ON spam_reports (received_at DESC, id);
CREATE INDEX spam_reports_reporter_idx ON spam_reports (reporter_user_id);
CREATE INDEX spam_reports_reported_idx ON spam_reports (reported_user_id, received_at DESC);
CREATE INDEX spam_reports_reviewer_idx ON spam_reports (reviewed_by_user_id);
CREATE INDEX spam_reports_conversation_idx ON spam_reports (conversation_id);

CREATE TABLE spam_report_evidences (
    report_id UUID NOT NULL REFERENCES spam_reports(id) ON DELETE CASCADE,
    source_message_id UUID NOT NULL,
    sender_user_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    message_created_at TIMESTAMPTZ NOT NULL,
    content_plain TEXT NOT NULL,
    is_selected BOOLEAN NOT NULL,
    is_context BOOLEAN NOT NULL,
    display_order INTEGER NOT NULL,
    PRIMARY KEY (report_id, source_message_id),
    CONSTRAINT spam_report_evidences_order_uq UNIQUE (report_id, display_order),
    CONSTRAINT spam_report_evidences_labels_check CHECK (is_selected OR is_context),
    CONSTRAINT spam_report_evidences_order_check CHECK (display_order > 0)
);
CREATE INDEX spam_report_evidences_sender_idx ON spam_report_evidences (sender_user_id);
COMMENT ON COLUMN spam_report_evidences.source_message_id IS
    'Snapshot identifier, deliberately not an FK to live messages; retained until report archival.';

-- Independent metadata stores: no mapping back to original reports/accounts.
CREATE TABLE spam_report_archives (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    received_on DATE NOT NULL,
    reviewed_on DATE,
    archived_on DATE NOT NULL,
    review_status TEXT NOT NULL,
    selected_count INTEGER NOT NULL,
    context_count INTEGER NOT NULL,
    overlap_count INTEGER NOT NULL,
    evidence_count INTEGER NOT NULL,
    CONSTRAINT spam_report_archives_counts_check CHECK (
        selected_count BETWEEN 1 AND 20 AND context_count BETWEEN 0 AND 30
        AND overlap_count BETWEEN 0 AND LEAST(selected_count, context_count)
        AND evidence_count = selected_count + context_count - overlap_count
    ),
    CONSTRAINT spam_report_archives_dates_check CHECK (
        archived_on >= received_on AND (reviewed_on IS NULL OR reviewed_on BETWEEN received_on AND archived_on)
    ),
    CONSTRAINT spam_report_archives_review_check CHECK (
        (review_status = 'PENDING' AND reviewed_on IS NULL)
        OR (review_status = 'REVIEWED' AND reviewed_on IS NOT NULL)
    )
);

CREATE TABLE spam_report_request_tombstones (
    request_digest BYTEA PRIMARY KEY,
    CONSTRAINT spam_report_request_tombstones_digest_check CHECK (octet_length(request_digest) = 32)
);

COMMENT ON COLUMN e2ee_devices.trust_proof IS
    'Public proof only. Structural checks and the approver FK do not verify trust or cryptography.';
COMMENT ON COLUMN messages.crypto_metadata IS
    'Opaque protocol metadata for encrypted messages; no plaintext keys or recovery codes.';
COMMENT ON TABLE spam_report_archives IS
    'DEC-10 anonymous metadata: new ID and day precision only; no original report or identity mapping.';
COMMENT ON TABLE spam_report_request_tombstones IS
    'DEC-10 retry guard: SHA-256(domain UTF-8 || 0x00 || UUID bytes), without payload or archive mapping.';
