-- Thanh vien 3: checks and workstream-specific fixtures for Login logs, App open events, Indexes.
-- Shared helpers and fixed-UUID users are loaded first; the caller rolls back.

-- Namespace 8 is local to these login-event fixtures.
INSERT INTO public.login_logs (id, user_id, login_at, session_scope) VALUES
(pg_temp.id(8,1), pg_temp.id(0,1), '2026-11-17 02:00:00+00', 'USER_CLIENT'),
(pg_temp.id(8,2), pg_temp.id(0,1), '2026-11-17 03:00:00+00', 'USER_CLIENT'),
(pg_temp.id(8,3), pg_temp.id(0,1), '2026-11-17 03:00:00+00', 'USER_CLIENT'),
(pg_temp.id(8,4), pg_temp.id(0,2), '2026-11-17 04:00:00+00', 'USER_CLIENT'),
(pg_temp.id(8,5), pg_temp.id(0,4), '2026-11-17 05:00:00+00', 'USER_CLIENT'),
(pg_temp.id(8,6), pg_temp.id(0,4), '2026-11-17 06:00:00+00', 'ADMIN_APP');

SELECT pg_temp.assert_true('logs-analytics/login metadata only',
    (SELECT array_agg(column_name::text ORDER BY ordinal_position)
        = ARRAY['id','user_id','login_at','session_scope']
     FROM information_schema.columns
     WHERE table_schema='public' AND table_name='login_logs'));
SELECT pg_temp.assert_true('logs-analytics/UUID event primary key',
    EXISTS (SELECT FROM pg_constraint c JOIN pg_attribute a
        ON a.attrelid=c.conrelid AND a.attnum=ANY(c.conkey)
        WHERE c.conrelid='public.login_logs'::regclass AND c.contype='p'
            AND cardinality(c.conkey)=1 AND a.attname='id' AND a.atttypid='uuid'::regtype));
SELECT pg_temp.assert_true('logs-analytics/personal history ordered by time then ID',
    (SELECT array_agg(id ORDER BY login_at DESC, id DESC)
        = ARRAY[pg_temp.id(8,3),pg_temp.id(8,2),pg_temp.id(8,1)]
     FROM public.login_logs WHERE user_id=pg_temp.id(0,1)));
SELECT pg_temp.assert_true('logs-analytics/system history includes both applications',
    (SELECT array_agg(id ORDER BY login_at DESC, id DESC)
        = ARRAY[pg_temp.id(8,6),pg_temp.id(8,5),pg_temp.id(8,4),
                pg_temp.id(8,3),pg_temp.id(8,2),pg_temp.id(8,1)]
     FROM public.login_logs));
SELECT pg_temp.assert_true('logs-analytics/account with no logins has empty history',
    NOT EXISTS (SELECT FROM public.login_logs WHERE user_id=pg_temp.id(0,3)));
SELECT pg_temp.assert_true('logs-analytics/admin role can receive either session scope',
    (SELECT count(*)=2 AND count(*) FILTER (WHERE l.session_scope='USER_CLIENT')=1
        AND count(*) FILTER (WHERE l.session_scope='ADMIN_APP')=1
     FROM public.login_logs l JOIN public.users u ON u.id=l.user_id
     WHERE u.id=pg_temp.id(0,4) AND u.role='ADMIN'));
SELECT pg_temp.assert_true('logs-analytics/display names come from account UUID',
    (SELECT count(*)=3 AND bool_and(u.username='alice01' AND u.full_name='Nguyễn Minh An')
     FROM public.login_logs l LEFT JOIN public.users u ON u.id=l.user_id
     WHERE l.user_id=pg_temp.id(0,1)));
SELECT pg_temp.assert_true('logs-analytics/display time in Ho Chi Minh City',
    (SELECT login_at AT TIME ZONE 'Asia/Ho_Chi_Minh' = TIMESTAMP '2026-11-17 09:00:00'
     FROM public.login_logs WHERE id=pg_temp.id(8,1)));

-- Explicit timestamps above are fixtures; production inserts use the server default.
DO $$
DECLARE
    before_insert TIMESTAMPTZ := clock_timestamp();
    recorded public.login_logs%ROWTYPE;
BEGIN
    INSERT INTO public.login_logs (user_id, session_scope)
        VALUES (pg_temp.id(0,3), 'USER_CLIENT') RETURNING * INTO recorded;
    PERFORM pg_temp.assert_true('logs-analytics/server supplies event ID and current instant',
        recorded.id IS NOT NULL AND recorded.login_at BETWEEN before_insert AND clock_timestamp());
    DELETE FROM public.login_logs WHERE id=recorded.id;
END $$;

SELECT pg_temp.expect_error('logs-analytics/duplicate event ID rejected',
    $$INSERT INTO public.login_logs SELECT * FROM public.login_logs WHERE id=pg_temp.id(8,1)$$,
    '23505', 'login_logs_pkey');
SELECT pg_temp.expect_error('logs-analytics/unknown account rejected',
    $$INSERT INTO public.login_logs (user_id,session_scope) VALUES (pg_temp.id(0,999),'USER_CLIENT')$$,
    '23503', 'login_logs_user_id_fkey');
SELECT pg_temp.expect_error('logs-analytics/unknown session scope rejected',
    $$INSERT INTO public.login_logs (user_id,session_scope) VALUES (pg_temp.id(0,1),'OTHER')$$,
    '23514', 'login_logs_session_scope_check');
SELECT pg_temp.expect_error('logs-analytics/session scope required',
    $$INSERT INTO public.login_logs (user_id) VALUES (pg_temp.id(0,1))$$, '23502');
SELECT pg_temp.expect_error('logs-analytics/login time required',
    $$INSERT INTO public.login_logs (user_id,login_at,session_scope)
      VALUES (pg_temp.id(0,1),NULL,'USER_CLIENT')$$, '23502');
SELECT pg_temp.expect_error('logs-analytics/event ID required',
    $$INSERT INTO public.login_logs (id,user_id,session_scope)
      VALUES (NULL,pg_temp.id(0,1),'USER_CLIENT')$$, '23502');

-- Isolated accounts exercise the FK contract without changing shared fixtures.
INSERT INTO public.users (id,username,email,full_name,created_at)
VALUES (pg_temp.id(0,81),'reused81','reused81@example.test','Tên trước purge','2026-01-01 UTC');
INSERT INTO public.login_logs (id,user_id,login_at,session_scope) VALUES
(pg_temp.id(8,81),pg_temp.id(0,81),'2026-09-01 UTC','USER_CLIENT'),
(pg_temp.id(8,82),pg_temp.id(0,81),'2026-09-02 UTC','USER_CLIENT');
UPDATE public.users SET status='DELETED',deleted_at='2026-10-01 UTC' WHERE id=pg_temp.id(0,81);
SELECT pg_temp.assert_true('logs-analytics/soft deletion preserves account history',
    (SELECT count(*)=2 AND bool_and(u.status='DELETED' AND u.username='reused81')
     FROM public.login_logs l JOIN public.users u ON u.id=l.user_id
     WHERE l.user_id=pg_temp.id(0,81)));
DELETE FROM public.users WHERE id=pg_temp.id(0,81);
SELECT pg_temp.assert_true('logs-analytics/purge preserves events and removes account identity',
    (SELECT count(*)=2 AND bool_and(user_id IS NULL)
     FROM public.login_logs WHERE id IN (pg_temp.id(8,81),pg_temp.id(8,82))));

INSERT INTO public.users (id,username,email,full_name,created_at)
VALUES (pg_temp.id(0,82),'reused81','reused81@example.test','Tên sau purge','2026-11-01 UTC');
INSERT INTO public.login_logs (id,user_id,login_at,session_scope)
VALUES (pg_temp.id(8,83),pg_temp.id(0,82),'2026-11-17 UTC','USER_CLIENT');
SELECT pg_temp.assert_true('logs-analytics/reused username does not inherit old history',
    (SELECT array_agg(id ORDER BY login_at DESC,id DESC)=ARRAY[pg_temp.id(8,83)]
     FROM public.login_logs WHERE user_id=pg_temp.id(0,82)));
SELECT pg_temp.assert_true('logs-analytics/system log labels purged accounts without reassigning them',
    (SELECT count(*)=2 AND bool_and(u.id IS NULL
        AND COALESCE(u.username,'Người dùng đã xóa')='Người dùng đã xóa'
        AND COALESCE(u.full_name,'Người dùng đã xóa')='Người dùng đã xóa')
     FROM public.login_logs l LEFT JOIN public.users u ON u.id=l.user_id
     WHERE l.id IN (pg_temp.id(8,81),pg_temp.id(8,82))));

-- App-open fixtures use namespace 9 for launch IDs and users 91-93 for purge.
CREATE TEMP TABLE app_open_first (id UUID, opened_at TIMESTAMPTZ);
DO $$
DECLARE
    before_insert TIMESTAMPTZ := clock_timestamp();
    event_id UUID;
BEGIN
    event_id := public.record_app_open(pg_temp.id(0,4),pg_temp.id(9,1),'USER_CLIENT');
    INSERT INTO app_open_first SELECT id,opened_at FROM public.app_open_events WHERE id=event_id;
    PERFORM pg_temp.assert_true('logs-analytics/app open server supplies ID and time',
        (SELECT id IS NOT NULL AND opened_at BETWEEN before_insert AND clock_timestamp()
         FROM app_open_first));
END $$;
SELECT pg_temp.assert_true('logs-analytics/admin using User Client creates one app open',
    (SELECT count(*)=1 FROM public.app_open_events e JOIN public.app_open_accounts a USING (account_key)
     WHERE a.user_id=pg_temp.id(0,4)));
SELECT pg_temp.assert_true('logs-analytics/Admin App creates no app open',
    public.record_app_open(pg_temp.id(0,4),pg_temp.id(9,2),'ADMIN_APP') IS NULL);
SELECT pg_temp.assert_true('logs-analytics/Admin App leaves open counts unchanged',
    (SELECT count(*)=1 FROM public.app_open_events)
    AND (SELECT count(*)=1 FROM public.app_open_accounts));

-- Re-login creates another login log but returns the original app-open event.
INSERT INTO public.login_logs (user_id,session_scope) VALUES (pg_temp.id(0,4),'USER_CLIENT');
CREATE TEMP TABLE app_open_retry AS
    SELECT public.record_app_open(pg_temp.id(0,4),pg_temp.id(9,1),'USER_CLIENT') AS id;
SELECT pg_temp.assert_true('logs-analytics/re-login adds log without another app open',
    (SELECT count(*)=3 FROM public.login_logs WHERE user_id=pg_temp.id(0,4))
    AND (SELECT count(*)=1 FROM public.app_open_events));
SELECT pg_temp.assert_true('logs-analytics/retry preserves first event ID and timestamp',
    (SELECT e.id=r.id AND e.opened_at=f.opened_at
     FROM app_open_first f JOIN public.app_open_events e USING (id) CROSS JOIN app_open_retry r));
DO $$ BEGIN
    PERFORM public.record_app_open(pg_temp.id(0,4),pg_temp.id(9,2),'USER_CLIENT');
END $$;
SELECT pg_temp.assert_true('logs-analytics/new launch adds open but not unique account',
    (SELECT count(*)=2 AND count(DISTINCT account_key)=1 FROM public.app_open_events));
DO $$ BEGIN
    PERFORM public.record_app_open(pg_temp.id(0,1),pg_temp.id(9,1),'USER_CLIENT');
    PERFORM public.record_app_open(pg_temp.id(0,2),pg_temp.id(9,1),'USER_CLIENT');
END $$;
SELECT pg_temp.assert_true('logs-analytics/accounts sharing one launch count separately',
    (SELECT count(*)=3 AND count(DISTINCT account_key)=3
     FROM public.app_open_events WHERE launch_id=pg_temp.id(9,1)));
SELECT pg_temp.assert_true('logs-analytics/statistics key differs from account UUID',
    (SELECT count(*)=3 AND bool_and(account_key<>user_id) FROM public.app_open_accounts));

SELECT pg_temp.expect_error('logs-analytics/duplicate account launch rejected',
    $$INSERT INTO public.app_open_events (account_key,launch_id)
      SELECT account_key,launch_id FROM public.app_open_events LIMIT 1$$,
    '23505','app_open_events_account_launch_key');
SELECT pg_temp.expect_error('logs-analytics/one statistics key per live account',
    $$INSERT INTO public.app_open_accounts (user_id) VALUES (pg_temp.id(0,4))$$,
    '23505','app_open_accounts_user_id_key');
SELECT pg_temp.expect_error('logs-analytics/app open requires statistics key',
    $$INSERT INTO public.app_open_events (launch_id) VALUES (pg_temp.id(9,3))$$,'23502');
SELECT pg_temp.expect_error('logs-analytics/app open requires launch ID',
    $$INSERT INTO public.app_open_events (account_key)
      SELECT account_key FROM public.app_open_accounts LIMIT 1$$,'23502');
SELECT pg_temp.expect_error('logs-analytics/app open requires timestamp',
    $$INSERT INTO public.app_open_events (account_key,launch_id,opened_at)
      SELECT account_key,pg_temp.id(9,3),NULL FROM public.app_open_accounts LIMIT 1$$,'23502');
SELECT pg_temp.expect_error('logs-analytics/app open rejects unknown statistics key',
    $$INSERT INTO public.app_open_events (account_key,launch_id) VALUES (pg_temp.id(9,999),pg_temp.id(9,3))$$,
    '23503','app_open_events_account_key_fkey');
SELECT pg_temp.expect_error('logs-analytics/record app open requires account',
    $$SELECT public.record_app_open(NULL,pg_temp.id(9,3),'USER_CLIENT')$$,'22004');
SELECT pg_temp.expect_error('logs-analytics/record app open requires launch',
    $$SELECT public.record_app_open(pg_temp.id(0,1),NULL,'USER_CLIENT')$$,'22004');
SELECT pg_temp.expect_error('logs-analytics/record app open requires granted scope',
    $$SELECT public.record_app_open(pg_temp.id(0,1),pg_temp.id(9,3),NULL)$$,'22004');
SELECT pg_temp.expect_error('logs-analytics/record app open rejects unknown scope',
    $$SELECT public.record_app_open(pg_temp.id(0,1),pg_temp.id(9,3),'OTHER')$$,'22023');
SELECT pg_temp.expect_error('logs-analytics/record app open rejects purged or missing account',
    $$SELECT public.record_app_open(pg_temp.id(0,999),pg_temp.id(9,3),'USER_CLIENT')$$,
    '23503','app_open_accounts_user_id_fkey');
SELECT pg_temp.expect_error('logs-analytics/statistics identity rejects unknown account',
    $$INSERT INTO public.app_open_accounts (user_id) VALUES (pg_temp.id(0,999))$$,
    '23503','app_open_accounts_user_id_fkey');
SELECT pg_temp.expect_error('logs-analytics/cannot delete statistics key with historical events',
    $$DELETE FROM public.app_open_accounts WHERE user_id=pg_temp.id(0,4)$$,
    '23001','app_open_events_account_key_fkey');

INSERT INTO public.users (id,username,email,full_name,created_at) VALUES
(pg_temp.id(0,91),'open0091','open91@example.test','Tài khoản mở app 91','2026-01-01 UTC'),
(pg_temp.id(0,92),'open0092','open92@example.test','Tài khoản mở app 92','2026-01-01 UTC');
DO $$ BEGIN
    PERFORM public.record_app_open(pg_temp.id(0,91),pg_temp.id(9,91),'USER_CLIENT');
    PERFORM public.record_app_open(pg_temp.id(0,92),pg_temp.id(9,91),'USER_CLIENT');
    PERFORM public.record_app_open(pg_temp.id(0,91),pg_temp.id(9,92),'USER_CLIENT');
END $$;
CREATE TEMP TABLE app_open_purge_keys AS
    SELECT account_key FROM public.app_open_accounts WHERE user_id IN (pg_temp.id(0,91),pg_temp.id(0,92));
-- Fixed fixture instants straddle the exact September boundaries in Vietnam.
UPDATE public.app_open_events e SET opened_at=CASE
    WHEN a.user_id=pg_temp.id(0,91) AND e.launch_id=pg_temp.id(9,91) THEN TIMESTAMPTZ '2026-08-31 17:00:00+00'
    WHEN a.user_id=pg_temp.id(0,92) THEN TIMESTAMPTZ '2026-09-30 16:59:59.999999+00'
    ELSE TIMESTAMPTZ '2026-09-30 17:00:00+00' END
FROM public.app_open_accounts a WHERE a.account_key=e.account_key
    AND a.user_id IN (pg_temp.id(0,91),pg_temp.id(0,92));
SELECT pg_temp.assert_true('logs-analytics/month boundaries use Vietnam time and exclude next month',
    (SELECT count(*)=2 AND count(DISTINCT e.account_key)=2
     FROM public.app_open_events e JOIN app_open_purge_keys k USING (account_key)
     WHERE opened_at >= TIMESTAMP '2026-09-01' AT TIME ZONE 'Asia/Ho_Chi_Minh'
       AND opened_at < TIMESTAMP '2026-10-01' AT TIME ZONE 'Asia/Ho_Chi_Minh'));
UPDATE public.users SET status='DELETED',deleted_at='2026-10-01 UTC'
WHERE id IN (pg_temp.id(0,91),pg_temp.id(0,92));
SELECT pg_temp.assert_true('logs-analytics/soft deletion preserves app-open identities and events',
    (SELECT count(*)=3 AND count(DISTINCT e.account_key)=2
     FROM public.app_open_events e JOIN public.app_open_accounts a USING (account_key)
     WHERE a.user_id IN (pg_temp.id(0,91),pg_temp.id(0,92))));
DELETE FROM public.users WHERE id IN (pg_temp.id(0,91),pg_temp.id(0,92));
SELECT pg_temp.assert_true('logs-analytics/purge removes user UUID links but retains two statistics keys',
    (SELECT count(*)=2 AND bool_and(a.user_id IS NULL)
     FROM public.app_open_accounts a JOIN app_open_purge_keys k USING (account_key)));
SELECT pg_temp.assert_true('logs-analytics/monthly unique openers remain two after both accounts purged',
    (SELECT count(*)=2 AND count(DISTINCT e.account_key)=2
     FROM public.app_open_events e JOIN app_open_purge_keys k USING (account_key)
     WHERE opened_at >= TIMESTAMP '2026-09-01' AT TIME ZONE 'Asia/Ho_Chi_Minh'
       AND opened_at < TIMESTAMP '2026-10-01' AT TIME ZONE 'Asia/Ho_Chi_Minh'));
SELECT pg_temp.assert_true('logs-analytics/purge also preserves events in other months',
    (SELECT count(*)=1 AND count(DISTINCT e.account_key)=1
     FROM public.app_open_events e JOIN app_open_purge_keys k USING (account_key)
     WHERE date_trunc('month',opened_at AT TIME ZONE 'Asia/Ho_Chi_Minh')=TIMESTAMP '2026-10-01'));
SELECT pg_temp.expect_error('logs-analytics/purged account cannot record another app open',
    $$SELECT public.record_app_open(pg_temp.id(0,91),pg_temp.id(9,91),'USER_CLIENT')$$,
    '23503','app_open_accounts_user_id_fkey');

INSERT INTO public.users (id,username,email,full_name)
VALUES (pg_temp.id(0,93),'open0091','open91@example.test','Đăng ký lại sau purge');
DO $$ BEGIN
    PERFORM public.record_app_open(pg_temp.id(0,93),pg_temp.id(9,91),'USER_CLIENT');
END $$;
SELECT pg_temp.assert_true('logs-analytics/reused username gets a new statistics key and its own event',
    (SELECT count(*)=1 AND bool_and(k.account_key IS NULL)
     FROM public.app_open_accounts a JOIN public.app_open_events e USING (account_key)
     LEFT JOIN app_open_purge_keys k USING (account_key) WHERE a.user_id=pg_temp.id(0,93)));
