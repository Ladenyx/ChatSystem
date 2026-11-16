-- Shared conversation reference contract used independently by E2EE and Spam.
-- Messaging-specific tables belong in domains/messaging.sql.
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
        OR (type = 'GROUP' AND group_name IS NOT NULL AND length(btrim(group_name)) > 0)
    ),
    CONSTRAINT conversations_closed_check CHECK (closed_at IS NULL OR closed_at >= created_at)
);
CREATE INDEX conversations_creator_idx ON conversations (created_by_user_id);
CREATE INDEX conversations_group_name_idx ON conversations USING GIN (lower(group_name) gin_trgm_ops)
    WHERE type = 'GROUP';


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

