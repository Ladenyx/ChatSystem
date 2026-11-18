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

-- App open events and the remaining analytics are implemented separately (4.2+).
