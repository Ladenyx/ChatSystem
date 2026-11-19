-- Member 3: durable registration, app-open and accepted-send statistics.
-- Transaction control belongs to schema.sql/the isolated test runner.

-- Successful application-session grants only. The authentication service supplies
-- the actual granted scope, independently of users.role, in the grant transaction.
-- No dependency on the authentication workstream's session table.
CREATE TABLE public.login_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID,
    login_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    session_scope TEXT NOT NULL,
    CONSTRAINT login_logs_user_id_fkey FOREIGN KEY (user_id)
        REFERENCES public.users(id) ON DELETE SET NULL,
    CONSTRAINT login_logs_session_scope_check
        CHECK (session_scope IN ('USER_CLIENT', 'ADMIN_APP'))
);

-- Time plus event ID gives a total order, including simultaneous logins.
CREATE INDEX login_logs_login_at_id_idx
    ON public.login_logs (login_at DESC, id DESC);
CREATE INDEX login_logs_user_id_login_at_id_idx
    ON public.login_logs (user_id, login_at DESC, id DESC);

COMMENT ON TABLE public.login_logs IS
    'Successful session grants; metadata only, no passwords or session tokens.';
COMMENT ON COLUMN public.login_logs.user_id IS
    'Account UUID; NULL after purge. Resolve display names from users, never by username.';
COMMENT ON COLUMN public.login_logs.login_at IS
    'Server event time; display in Asia/Ho_Chi_Minh. Not an app-open counter.';
COMMENT ON COLUMN public.login_logs.session_scope IS
    'Granted application scope: USER_CLIENT or ADMIN_APP, independent of account role.';

-- Shared statistics identity for registrations, app opens and accepted sends.
-- Keep the existing table/API name; an account need not open the app to have a key.
-- A separate, random statistics key survives purge without retaining the users ID.
-- UNIQUE permits multiple purged (NULL) identities but only one key per live user.
CREATE TABLE public.app_open_accounts (
    account_key UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID UNIQUE,
    CONSTRAINT app_open_accounts_user_id_fkey FOREIGN KEY (user_id)
        REFERENCES public.users(id) ON DELETE SET NULL
);

CREATE FUNCTION public.protect_statistics_identity() RETURNS TRIGGER
LANGUAGE plpgsql AS $$
BEGIN
    IF NEW.account_key IS DISTINCT FROM OLD.account_key
        OR (NEW.user_id IS DISTINCT FROM OLD.user_id AND NEW.user_id IS NOT NULL)
        OR (NEW.user_id IS NULL AND OLD.user_id IS NOT NULL
            AND EXISTS (SELECT FROM public.users WHERE id=OLD.user_id)) THEN
        RAISE EXCEPTION 'Statistics identity cannot be changed or reassigned'
            USING ERRCODE = '23514', CONSTRAINT = 'statistics_identity_immutable';
    END IF;
    RETURN NEW;
END $$;
-- The only identity transition is the FK's SET NULL after physical user purge.
CREATE TRIGGER app_open_accounts_identity_immutable
BEFORE UPDATE ON public.app_open_accounts
FOR EACH ROW EXECUTE FUNCTION public.protect_statistics_identity();

CREATE TABLE public.account_registration_events (
    account_key UUID PRIMARY KEY,
    registered_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT account_registration_events_account_key_fkey FOREIGN KEY (account_key)
        REFERENCES public.app_open_accounts(account_key) ON DELETE RESTRICT,
    CONSTRAINT account_registration_events_time_check CHECK (isfinite(registered_at))
);
CREATE INDEX account_registration_events_time_key_idx
    ON public.account_registration_events (registered_at, account_key);

-- Preserve known creation times when this workstream is loaded after existing users.
INSERT INTO public.app_open_accounts (user_id)
SELECT id FROM public.users ON CONFLICT (user_id) DO NOTHING;
INSERT INTO public.account_registration_events (account_key, registered_at)
SELECT a.account_key,u.created_at FROM public.app_open_accounts a
JOIN public.users u ON u.id=a.user_id
ON CONFLICT (account_key) DO NOTHING;

CREATE FUNCTION public.record_account_registration() RETURNS TRIGGER
LANGUAGE plpgsql AS $$
DECLARE
    statistics_key UUID;
BEGIN
    INSERT INTO public.app_open_accounts (user_id) VALUES (NEW.id)
        RETURNING account_key INTO statistics_key;
    INSERT INTO public.account_registration_events (account_key, registered_at)
        VALUES (statistics_key, NEW.created_at);
    RETURN NEW;
END $$;
-- Covers USER/ADMIN, CLI bootstrap and Admin-created accounts, even without login.
-- Pending registrations are not users and therefore do not fire this trigger.
CREATE TRIGGER users_record_registration AFTER INSERT ON public.users
FOR EACH ROW EXECUTE FUNCTION public.record_account_registration();

CREATE TABLE public.app_open_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_key UUID NOT NULL,
    launch_id UUID NOT NULL,
    opened_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    CONSTRAINT app_open_events_account_key_fkey FOREIGN KEY (account_key)
        REFERENCES public.app_open_accounts(account_key) ON DELETE RESTRICT,
    CONSTRAINT app_open_events_account_launch_key UNIQUE (account_key, launch_id)
);

CREATE INDEX app_open_events_opened_at_account_key_idx
    ON public.app_open_events (opened_at, account_key);
-- Per-account counts in a date window; UNIQUE(account_key, launch_id) cannot
-- constrain opened_at. Keep both column orders for the two distinct workloads.
CREATE INDEX app_open_events_account_key_opened_at_idx
    ON public.app_open_events (account_key, opened_at);

-- Call only after authentication, in the same transaction as the session grant.
-- No session-table dependency; the caller supplies the actual granted scope.
CREATE FUNCTION public.record_app_open(p_user_id UUID, p_launch_id UUID, p_session_scope TEXT)
RETURNS UUID LANGUAGE plpgsql AS $$
DECLARE
    statistics_key UUID;
    event_id UUID;
BEGIN
    IF p_user_id IS NULL OR p_launch_id IS NULL OR p_session_scope IS NULL THEN
        RAISE EXCEPTION 'Account, launch ID and granted session scope are required'
            USING ERRCODE = '22004';
    END IF;
    IF p_session_scope NOT IN ('USER_CLIENT', 'ADMIN_APP') THEN
        RAISE EXCEPTION 'Unknown granted session scope: %', p_session_scope
            USING ERRCODE = '22023';
    END IF;
    IF p_session_scope = 'ADMIN_APP' THEN
        RETURN NULL;
    END IF;

    -- Hold the account against physical deletion until the grant transaction ends.
    PERFORM 1 FROM public.users WHERE id = p_user_id FOR KEY SHARE;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Account does not exist'
            USING ERRCODE = '23503', CONSTRAINT = 'app_open_accounts_user_id_fkey';
    END IF;

    -- No-op conflict updates return the winning IDs even with concurrent callers.
    INSERT INTO public.app_open_accounts AS a (user_id) VALUES (p_user_id)
        ON CONFLICT (user_id) DO UPDATE SET user_id = EXCLUDED.user_id
        RETURNING a.account_key INTO statistics_key;
    INSERT INTO public.app_open_events AS e (account_key, launch_id)
        VALUES (statistics_key, p_launch_id)
        ON CONFLICT (account_key, launch_id) DO UPDATE SET launch_id = EXCLUDED.launch_id
        RETURNING e.id INTO event_id;
    RETURN event_id;
END $$;

-- Snapshots of server-accepted sends. Message/group UUIDs are identifiers only,
-- deliberately not FKs: deleting a message/group must not erase historical counts.
CREATE TABLE public.message_send_events (
    message_id UUID PRIMARY KEY,
    sender_key UUID NOT NULL,
    recipient_key UUID,
    group_id UUID,
    accepted_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT message_send_events_sender_key_fkey FOREIGN KEY (sender_key)
        REFERENCES public.app_open_accounts(account_key) ON DELETE RESTRICT,
    CONSTRAINT message_send_events_recipient_key_fkey FOREIGN KEY (recipient_key)
        REFERENCES public.app_open_accounts(account_key) ON DELETE RESTRICT,
    CONSTRAINT message_send_events_destination_check CHECK (
        (recipient_key IS NOT NULL) <> (group_id IS NOT NULL)
        AND (recipient_key IS NULL OR recipient_key <> sender_key)
    ),
    CONSTRAINT message_send_events_time_check CHECK (isfinite(accepted_at))
);
CREATE INDEX message_send_events_sender_time_idx
    ON public.message_send_events (sender_key, accepted_at);
CREATE INDEX message_send_events_time_sender_idx
    ON public.message_send_events (accepted_at, sender_key);
CREATE INDEX message_send_events_recipient_key_idx
    ON public.message_send_events (recipient_key) WHERE recipient_key IS NOT NULL;

-- Internal server contract: call in the transaction that accepts/stores the message.
-- The server validates the session, conversation, membership, payload and acceptance
-- time. This storage API neither authorizes a send nor requires messaging tables.
CREATE FUNCTION public.record_message_send(
    p_message_id UUID, p_sender_user_id UUID, p_recipient_user_id UUID,
    p_group_id UUID, p_accepted_at TIMESTAMPTZ
) RETURNS UUID LANGUAGE plpgsql AS $$
DECLARE
    sender_statistics_key UUID;
    recipient_statistics_key UUID;
    stored public.message_send_events%ROWTYPE;
BEGIN
    IF p_message_id IS NULL OR p_sender_user_id IS NULL OR p_accepted_at IS NULL THEN
        RAISE EXCEPTION 'Message, sender and acceptance time are required'
            USING ERRCODE = '22004';
    END IF;
    IF NOT isfinite(p_accepted_at)
        OR (p_recipient_user_id IS NOT NULL) = (p_group_id IS NOT NULL) THEN
        RAISE EXCEPTION 'A send requires a finite time and exactly one destination'
            USING ERRCODE = '22023';
    END IF;

    -- Registration allocates keys. Read them without opposite-direction upsert
    -- locks; hold both live accounts against physical purge until this tx ends.
    SELECT a.account_key INTO sender_statistics_key
    FROM public.app_open_accounts a JOIN public.users u ON u.id=a.user_id
    WHERE u.id=p_sender_user_id FOR KEY SHARE OF a,u;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Sender does not exist'
            USING ERRCODE = '23503', CONSTRAINT = 'message_send_events_sender_key_fkey';
    END IF;
    IF p_recipient_user_id IS NOT NULL THEN
        SELECT a.account_key INTO recipient_statistics_key
        FROM public.app_open_accounts a JOIN public.users u ON u.id=a.user_id
        WHERE u.id=p_recipient_user_id FOR KEY SHARE OF a,u;
        IF NOT FOUND THEN
            RAISE EXCEPTION 'Recipient does not exist'
                USING ERRCODE = '23503', CONSTRAINT = 'message_send_events_recipient_key_fkey';
        END IF;
    END IF;

    INSERT INTO public.message_send_events
        (message_id,sender_key,recipient_key,group_id,accepted_at)
    VALUES (p_message_id,sender_statistics_key,recipient_statistics_key,p_group_id,p_accepted_at)
    ON CONFLICT (message_id) DO NOTHING;

    -- A concurrent duplicate may have committed while INSERT waited. This SELECT
    -- reads that committed row at READ COMMITTED and preserves its original time.
    SELECT * INTO STRICT stored FROM public.message_send_events WHERE message_id=p_message_id;
    IF stored.sender_key IS DISTINCT FROM sender_statistics_key
        OR stored.recipient_key IS DISTINCT FROM recipient_statistics_key
        OR stored.group_id IS DISTINCT FROM p_group_id THEN
        RAISE EXCEPTION 'Message ID is already bound to another accepted send'
            USING ERRCODE = '23505', CONSTRAINT = 'message_send_events_pkey';
    END IF;
    RETURN stored.message_id;
END $$;

CREATE FUNCTION public.protect_statistics_event() RETURNS TRIGGER
LANGUAGE plpgsql AS $$
BEGIN
    IF NEW IS DISTINCT FROM OLD THEN
        RAISE EXCEPTION 'Recorded statistics events are immutable'
            USING ERRCODE = '23514', CONSTRAINT = 'statistics_event_immutable';
    END IF;
    RETURN NEW;
END $$;
CREATE TRIGGER account_registration_events_immutable
BEFORE UPDATE ON public.account_registration_events
FOR EACH ROW EXECUTE FUNCTION public.protect_statistics_event();
CREATE TRIGGER message_send_events_immutable
BEFORE UPDATE ON public.message_send_events
FOR EACH ROW EXECUTE FUNCTION public.protect_statistics_event();

COMMENT ON TABLE public.app_open_accounts IS
    'Shared statistics identities, allocated at account creation; purge removes the link to users.';
COMMENT ON COLUMN public.app_open_accounts.user_id IS
    'Live account UUID; NULL after purge. Do not delete the surviving statistics key.';
COMMENT ON TABLE public.app_open_events IS
    'One successful User Client open per statistics account and client launch.';
COMMENT ON COLUMN public.app_open_events.launch_id IS
    'Random UUID created once per User Client process; reuse on retries and re-login.';
COMMENT ON COLUMN public.app_open_events.opened_at IS
    'First server-recorded authentication time; retries preserve this instant.';
COMMENT ON FUNCTION public.record_app_open(UUID, UUID, TEXT) IS
    'Authenticated server writes only; returns the existing/new event ID, or NULL for Admin App.';
COMMENT ON TABLE public.account_registration_events IS
    'One durable creation event for every USER or ADMIN account; no automatic expiry.';
COMMENT ON TABLE public.message_send_events IS
    'Durable accepted-send metadata only; survives purge, recall and message/group removal without payloads.';
COMMENT ON COLUMN public.message_send_events.message_id IS
    'Idempotency/snapshot identifier, not an FK to live messages.';
COMMENT ON COLUMN public.message_send_events.group_id IS
    'Group snapshot identifier, not an FK to live conversations; used only for distinct group counts.';
COMMENT ON FUNCTION public.record_message_send(UUID, UUID, UUID, UUID, TIMESTAMPTZ) IS
    'Internal server writes in the accepted-message transaction; retries preserve the first time and destination.';
