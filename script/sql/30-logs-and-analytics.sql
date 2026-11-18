-- Thanh vien 3: Login logs, App open events, Indexes.
-- Transaction control belongs to schema.sql/the isolated test runner.

-- Successful application-session grants only. The authentication service supplies
-- the actual granted scope, independently of users.role, in the grant transaction.
-- No dependency on the authentication workstream's future sessions table.
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

-- A separate, random statistics key survives purge without retaining the users ID.
-- UNIQUE permits multiple purged (NULL) identities but only one key per live user.
CREATE TABLE public.app_open_accounts (
    account_key UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID UNIQUE,
    CONSTRAINT app_open_accounts_user_id_fkey FOREIGN KEY (user_id)
        REFERENCES public.users(id) ON DELETE SET NULL
);

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

COMMENT ON TABLE public.app_open_accounts IS
    'Independent app-open statistics identities; purge removes the link to users.';
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
