-- Member 3: login logs and durable registration, app-open and send statistics.
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
UPDATE public.users SET status='DELETED',status_before_delete='ACTIVE',deleted_at='2026-10-01 UTC'
WHERE id=pg_temp.id(0,81);
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
    AND (SELECT count(*)=1 FROM public.app_open_accounts WHERE user_id=pg_temp.id(0,4)));

-- Re-login creates another login log but returns the original app-open event.
INSERT INTO public.login_logs (id,user_id,login_at,session_scope)
VALUES (pg_temp.id(8,7),pg_temp.id(0,4),'2026-11-17 07:00:00+00','USER_CLIENT');
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
    (SELECT count(*)=3 AND bool_and(account_key<>user_id) FROM public.app_open_accounts
     WHERE user_id IN (pg_temp.id(0,1),pg_temp.id(0,2),pg_temp.id(0,4))));

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
    '23001');

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
UPDATE public.users SET status='DELETED',status_before_delete='ACTIVE',deleted_at='2026-10-01 UTC'
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

-- Steps 5.1 and 5.3 share the same fixed input and expected results in both flows.
SELECT :'flow' = 'all' AS analytics_integrated \gset
CREATE TEMP TABLE analytics_params (
    singleton BOOLEAN PRIMARY KEY DEFAULT TRUE CHECK (singleton),
    from_date DATE NOT NULL, through_date DATE NOT NULL, chart_year INTEGER NOT NULL,
    name_query TEXT, sort_by TEXT NOT NULL DEFAULT 'created_at',
    sort_direction TEXT NOT NULL DEFAULT 'DESC', login_user_id UUID,
    activity_metric TEXT NOT NULL DEFAULT 'app_opens', count_operator TEXT, count_value BIGINT,
    CHECK (from_date BETWEEN DATE '0001-01-01' AND DATE '9998-12-31'
        AND through_date BETWEEN from_date AND DATE '9998-12-31'),
    CHECK (chart_year BETWEEN 1 AND 9998),
    CHECK (sort_by IN ('name','created_at') AND sort_direction IN ('ASC','DESC')),
    CHECK (activity_metric IN ('app_opens','direct_receivers','groups_chatted')),
    CHECK ((count_operator IS NULL AND count_value IS NULL)
        OR (count_operator IS NOT NULL AND count_value IS NOT NULL
            AND count_operator IN ('=','<','>') AND count_value>=0))
);
INSERT INTO analytics_params (from_date,through_date,chart_year) VALUES ('2030-11-01','2030-11-30',2030);
-- This test-only lookup remembers generated fixture keys after user purge.
-- Creation/send facts themselves always come from the persistent public tables.
CREATE TEMP TABLE analytics_fixture_keys (number INTEGER PRIMARY KEY, account_key UUID UNIQUE NOT NULL);
CREATE FUNCTION pg_temp.analytics_key(number INTEGER) RETURNS UUID LANGUAGE sql STABLE AS $$
    SELECT account_key FROM pg_temp.analytics_fixture_keys WHERE analytics_fixture_keys.number=$1
$$;

INSERT INTO public.users (id,username,email,full_name,role,status,created_at,deleted_at,status_before_delete) VALUES
(pg_temp.id(0,201),'ref0201','ref201@example.test','An','USER','ACTIVE','2030-10-31 17:00:00+00',NULL,NULL),
(pg_temp.id(0,202),'ref0202','ref202@example.test','Binh','USER','LOCKED','2030-10-30 17:00:00+00',NULL,NULL),
(pg_temp.id(0,203),'ref0203','ref203@example.test','Chi','USER','ACTIVE','2030-10-31 16:59:59.999999+00',NULL,NULL),
(pg_temp.id(0,204),'ref0204','ref204@example.test','Dung','USER','ACTIVE','2030-11-30 16:59:59.999999+00',NULL,NULL),
(pg_temp.id(0,205),'ref0205','ref205@example.test','Echo','USER','DELETED','2030-10-14 17:00:00+00','2030-11-15 17:00:00+00','ACTIVE'),
(pg_temp.id(0,206),'ref0206','ref206@example.test','Free %_','USER','ACTIVE','2030-10-31 17:00:00+00',NULL,NULL),
(pg_temp.id(0,207),'ref0207','ref207@example.test','An','ADMIN','ACTIVE','2030-10-31 17:00:00+00',NULL,NULL),
(pg_temp.id(0,208),'ref0208','ref208@example.test','Huy','USER','ACTIVE','2030-10-31 17:00:00+00',NULL,NULL),
(pg_temp.id(0,209),'ref0209','ref209@example.test','Ivy','USER','ACTIVE','2030-10-31 17:00:00+00',NULL,NULL),
(pg_temp.id(0,210),'ref0210','ref210@example.test','Jade','USER','ACTIVE','2030-10-31 17:00:00+00',NULL,NULL),
(pg_temp.id(0,211),'ref0211','ref211@example.test','Gone','USER','DELETED','2030-10-31 17:00:00+00','2030-11-09 17:00:00+00','ACTIVE'),
(pg_temp.id(0,212),'ref0212','ref212@example.test','Lost','USER','ACTIVE','2030-11-19 17:00:00+00',NULL,NULL);
INSERT INTO analytics_fixture_keys
SELECT n,a.account_key FROM generate_series(201,212) n
JOIN public.app_open_accounts a ON a.user_id=pg_temp.id(0,n);

-- Scope the real registration ledger to this file's fixture identities.
CREATE TEMP VIEW analytics_registration_facts AS
SELECT r.account_key,r.registered_at AS created_at
FROM public.account_registration_events r JOIN analytics_fixture_keys k USING (account_key);
CREATE TEMP TABLE analytics_pending_signups (request_id UUID PRIMARY KEY, requested_at TIMESTAMPTZ);
INSERT INTO analytics_pending_signups
SELECT pg_temp.id(1,n),'2030-11-01 00:00:00+07'::timestamptz FROM generate_series(201,203) n;

INSERT INTO public.app_open_events (id,account_key,launch_id,opened_at) VALUES
(pg_temp.id(8,2201),pg_temp.analytics_key(201),pg_temp.id(9,2201),'2030-10-31 17:00:00+00'),
(pg_temp.id(8,2202),pg_temp.analytics_key(201),pg_temp.id(9,2202),'2030-11-01 17:00:00+00'),
(pg_temp.id(8,2203),pg_temp.analytics_key(202),pg_temp.id(9,2203),'2030-10-31 05:00:00+00'),
(pg_temp.id(8,2204),pg_temp.analytics_key(204),pg_temp.id(9,2204),'2030-11-30 16:59:59.999999+00'),
(pg_temp.id(8,2205),pg_temp.analytics_key(205),pg_temp.id(9,2205),'2030-11-15 05:00:00+00'),
(pg_temp.id(8,2206),pg_temp.analytics_key(209),pg_temp.id(9,2206),'2030-10-31 17:00:00+00'),
(pg_temp.id(8,2207),pg_temp.analytics_key(212),pg_temp.id(9,2207),'2030-11-20 05:00:00+00'),
(pg_temp.id(8,2208),pg_temp.analytics_key(210),pg_temp.id(9,2208),'2030-11-30 17:00:00+00');
CREATE TEMP VIEW analytics_open_facts AS
SELECT e.* FROM public.app_open_events e JOIN pg_temp.analytics_registration_facts r USING (account_key);
INSERT INTO public.login_logs (id,user_id,login_at,session_scope) VALUES
(pg_temp.id(8,3201),pg_temp.id(0,201),'2030-10-31 17:00:00+00','USER_CLIENT'),
(pg_temp.id(8,3202),pg_temp.id(0,201),'2030-11-02 02:00:00+00','USER_CLIENT'),
(pg_temp.id(8,3203),pg_temp.id(0,201),'2030-11-02 02:00:00+00','USER_CLIENT'),
(pg_temp.id(8,3204),pg_temp.id(0,202),'2030-11-03 02:00:00+00','USER_CLIENT'),
(pg_temp.id(8,3205),pg_temp.id(0,207),'2030-11-04 02:00:00+00','ADMIN_APP');

-- Only server-accepted metadata contributes. NULL accepted_at means not accepted.
CREATE TEMP TABLE analytics_message_inputs (
    message_id UUID PRIMARY KEY,
    sender_key UUID NOT NULL REFERENCES analytics_fixture_keys(account_key),
    recipient_key UUID REFERENCES analytics_fixture_keys(account_key), group_id UUID,
    accepted_at TIMESTAMPTZ,
    CHECK ((recipient_key IS NOT NULL)<>(group_id IS NOT NULL))
);
INSERT INTO analytics_message_inputs VALUES
(pg_temp.id(2,4201),pg_temp.analytics_key(201),pg_temp.analytics_key(202),NULL,'2030-11-01 02:00:00+00'),
(pg_temp.id(2,4202),pg_temp.analytics_key(201),pg_temp.analytics_key(202),NULL,'2030-11-01 03:00:00+00'),
(pg_temp.id(2,4203),pg_temp.analytics_key(201),pg_temp.analytics_key(203),NULL,'2030-11-02 02:00:00+00'),
(pg_temp.id(2,4204),pg_temp.analytics_key(201),NULL,pg_temp.id(1,4201),'2030-11-01 02:00:00+00'),
(pg_temp.id(2,4205),pg_temp.analytics_key(201),NULL,pg_temp.id(1,4201),'2030-11-01 03:00:00+00'),
(pg_temp.id(2,4206),pg_temp.analytics_key(201),NULL,pg_temp.id(1,4202),'2030-11-02 02:00:00+00'),
(pg_temp.id(2,4207),pg_temp.analytics_key(202),pg_temp.analytics_key(201),NULL,'2030-10-31 17:00:00+00'),
(pg_temp.id(2,4208),pg_temp.analytics_key(208),pg_temp.analytics_key(201),NULL,'2030-11-01 02:00:00+00'),
(pg_temp.id(2,4209),pg_temp.analytics_key(206),pg_temp.analytics_key(201),NULL,NULL),
(pg_temp.id(2,4210),pg_temp.analytics_key(206),NULL,pg_temp.id(1,4201),NULL),
(pg_temp.id(2,4211),pg_temp.analytics_key(202),pg_temp.analytics_key(203),NULL,'2030-10-31 16:59:59.999999+00'),
(pg_temp.id(2,4212),pg_temp.analytics_key(201),pg_temp.analytics_key(209),NULL,'2030-11-30 17:00:00+00'),
(pg_temp.id(2,4213),pg_temp.analytics_key(212),pg_temp.analytics_key(201),NULL,'2030-11-20 06:00:00+00'),
(pg_temp.id(2,4214),pg_temp.analytics_key(212),NULL,pg_temp.id(1,4201),'2030-11-20 06:00:00+00');
CREATE TEMP TABLE analytics_friendship_inputs (
    user_a UUID, user_b UUID, is_accepted BOOLEAN NOT NULL,
    PRIMARY KEY (user_a,user_b)
);
INSERT INTO analytics_friendship_inputs
SELECT pg_temp.id(0,a),pg_temp.id(0,b),accepted FROM (VALUES
    (201,202,TRUE),(201,203,TRUE),(202,203,TRUE),(202,204,TRUE),(203,204,TRUE),
    (202,205,TRUE),(203,211,TRUE),(202,209,TRUE),(203,210,TRUE),(202,207,TRUE),
    (204,208,TRUE),(202,201,TRUE),(201,206,FALSE),(201,201,TRUE),
    (201,205,TRUE),(205,206,TRUE),(201,209,TRUE),(210,201,TRUE)) AS f(a,b,accepted);
CREATE TEMP TABLE analytics_block_inputs (
    blocker_id UUID, blocked_id UUID,
    PRIMARY KEY (blocker_id,blocked_id)
);
INSERT INTO analytics_block_inputs VALUES (pg_temp.id(0,201),pg_temp.id(0,209)),(pg_temp.id(0,210),pg_temp.id(0,201));

\if :analytics_integrated
-- D completed self-registration. The pending requests and F's Admin-issued
-- initial setup are separate from the completed accounts counted via users.
INSERT INTO public.pending_registrations (id,username,email,password_hash,created_at,expires_at)
SELECT request_id,'signup' || right(request_id::text,3),
       'signup' || right(request_id::text,3) || '@example.test','fixture-hash',
       requested_at,requested_at + INTERVAL '30 minutes'
FROM analytics_pending_signups;
INSERT INTO public.pending_registrations
    (id,username,email,password_hash,created_at,expires_at,email_verified_at,completed_at)
SELECT pg_temp.id(1,204),username,email,'fixture-hash',created_at - INTERVAL '10 minutes',
       created_at + INTERVAL '20 minutes',created_at,created_at
FROM public.users WHERE id=pg_temp.id(0,204);
INSERT INTO public.password_reset_tokens
    (id,user_id,purpose,temporary_password_hash,issued_at,expires_at)
SELECT pg_temp.id(1,206),id,'INITIAL_SETUP','fixture-hash',created_at,created_at + INTERVAL '15 minutes'
FROM public.users WHERE id=pg_temp.id(0,206);

INSERT INTO public.user_sessions (id,user_id,scope,token_hash,created_at)
SELECT pg_temp.id(1,207),user_id,session_scope,'fixture-admin-session',login_at
FROM public.login_logs WHERE id=pg_temp.id(8,3205);

INSERT INTO public.friendships (user_low_id,user_high_id,created_at)
SELECT DISTINCT LEAST(f.user_a,f.user_b),GREATEST(f.user_a,f.user_b),
       GREATEST(a.created_at,b.created_at,'2030-11-02 00:00:00+07'::timestamptz)
FROM analytics_friendship_inputs f
JOIN public.users a ON a.id=f.user_a JOIN public.users b ON b.id=f.user_b
WHERE f.is_accepted AND f.user_a<>f.user_b;
INSERT INTO public.friend_requests (id,sender_id,recipient_id,created_at)
SELECT pg_temp.id(1,4501),user_a,user_b,'2030-11-02 00:00:00+07'
FROM analytics_friendship_inputs WHERE NOT is_accepted;
INSERT INTO public.blocks (blocker_id,blocked_id,created_at)
SELECT blocker_id,blocked_id,'2030-11-03 00:00:00+07' FROM analytics_block_inputs;

CREATE TEMP TABLE analytics_direct_threads AS
SELECT pg_temp.id(1,4300 + row_number() OVER (ORDER BY low_key,high_key)::integer) AS conversation_id,
       low_key,high_key
FROM (
    SELECT DISTINCT LEAST(sender_key,recipient_key) AS low_key,GREATEST(sender_key,recipient_key) AS high_key
    FROM analytics_message_inputs WHERE recipient_key IS NOT NULL AND accepted_at IS NOT NULL
) AS pairs;
INSERT INTO public.conversations (id,type,group_name,created_at)
SELECT conversation_id,'DIRECT',NULL,'2030-10-31 16:59:59.999999+00'::timestamptz
FROM analytics_direct_threads
UNION ALL
SELECT DISTINCT group_id,'GROUP','Nhóm ' || right(group_id::text,4),'2030-11-01 00:00:00+07'::timestamptz
FROM analytics_message_inputs WHERE group_id IS NOT NULL;
INSERT INTO public.conversation_members (conversation_id,user_id,joined_at)
SELECT d.conversation_id,k.user_id,GREATEST(c.created_at,u.created_at)
FROM analytics_direct_threads d JOIN public.conversations c ON c.id=d.conversation_id
JOIN public.app_open_accounts k ON k.account_key IN (d.low_key,d.high_key)
JOIN public.users u ON u.id=k.user_id
UNION ALL
SELECT c.id,pg_temp.id(0,n),'2030-11-01 00:00:00+07'::timestamptz
FROM public.conversations c CROSS JOIN generate_series(201,203) n
WHERE c.id IN (pg_temp.id(1,4201),pg_temp.id(1,4202));
INSERT INTO public.conversation_members (conversation_id,user_id,joined_at)
VALUES (pg_temp.id(1,4201),pg_temp.id(0,212),'2030-11-20 05:30:00+00');

INSERT INTO public.messages
    (id,conversation_id,sender_user_id,created_at,content_plain,content_cipher,crypto_metadata)
SELECT m.message_id,COALESCE(m.group_id,d.conversation_id),k.user_id,m.accepted_at,
       CASE WHEN m.group_id IS NULL THEN 'Fixture message' END,
       CASE WHEN m.group_id IS NOT NULL THEN decode('01','hex') END,
       CASE WHEN m.group_id IS NOT NULL THEN '{}'::jsonb END
FROM analytics_message_inputs m JOIN public.app_open_accounts k ON k.account_key=m.sender_key
LEFT JOIN analytics_direct_threads d ON d.low_key=LEAST(m.sender_key,m.recipient_key)
    AND d.high_key=GREATEST(m.sender_key,m.recipient_key) AND m.recipient_key IS NOT NULL
WHERE m.accepted_at IS NOT NULL;

-- Reference server transaction: persist each real accepted message and its
-- statistics in the same outer transaction; no messaging-module DDL is changed.
DO $$ BEGIN
PERFORM public.record_message_send(m.id,m.sender_user_id,cm.user_id,
       CASE WHEN c.type='GROUP' THEN c.id END,m.created_at)
FROM public.messages m JOIN public.conversations c ON c.id=m.conversation_id
JOIN analytics_message_inputs i ON i.message_id=m.id
LEFT JOIN public.conversation_members cm ON c.type='DIRECT' AND cm.conversation_id=c.id
    AND cm.user_id<>m.sender_user_id;
END $$;
CREATE TEMP VIEW analytics_friendships AS
SELECT user_low_id AS user_a,user_high_id AS user_b FROM public.friendships;
CREATE TEMP VIEW analytics_blocks AS SELECT blocker_id,blocked_id FROM public.blocks;
\else
-- Standalone flow exercises the same storage API without loading messaging DDL.
DO $$ BEGIN
PERFORM public.record_message_send(i.message_id,s.user_id,r.user_id,i.group_id,i.accepted_at)
FROM analytics_message_inputs i
JOIN public.app_open_accounts s ON s.account_key=i.sender_key
LEFT JOIN public.app_open_accounts r ON r.account_key=i.recipient_key
WHERE i.accepted_at IS NOT NULL;
END $$;
CREATE TEMP VIEW analytics_friendships AS SELECT user_a,user_b FROM analytics_friendship_inputs WHERE is_accepted;
CREATE TEMP VIEW analytics_blocks AS SELECT blocker_id,blocked_id FROM analytics_block_inputs;
\endif

-- Queries read the deployed ledger in both modes, including after purge/recall.
CREATE TEMP VIEW analytics_sends AS
SELECT s.* FROM public.message_send_events s JOIN analytics_fixture_keys k ON k.account_key=s.sender_key;

-- Reference analytics queries read the same source contract in either flow.
CREATE TEMP VIEW analytics_bounds AS
SELECT p.*, p.from_date::timestamp AT TIME ZONE 'Asia/Ho_Chi_Minh' AS starts_at,
       (p.through_date + 1)::timestamp AT TIME ZONE 'Asia/Ho_Chi_Minh' AS ends_at
FROM pg_temp.analytics_params p;

CREATE TEMP VIEW analytics_accounts AS
SELECT r.account_key,k.user_id,r.created_at,
       COALESCE(u.username,'Người dùng đã xóa') AS username,
       COALESCE(u.full_name,'Người dùng đã xóa') AS full_name,
       u.email,COALESCE(u.status,'PURGED') AS status,
       COALESCE(k.user_id,r.account_key) AS sort_id
FROM pg_temp.analytics_registration_facts r
LEFT JOIN public.app_open_accounts k ON k.account_key=r.account_key
LEFT JOIN public.users u ON u.id=k.user_id;

-- Keep literal-substring semantics while making the live-name predicate match
-- the existing users_name_trgm_idx on lower(users.full_name). Escape ! first.
CREATE TEMP VIEW analytics_name_filter AS
SELECT '%'||replace(replace(replace(lower(btrim(COALESCE(name_query,''))),
    '!','!!'),'%','!%'),'_','!_')||'%' AS name_pattern
FROM pg_temp.analytics_params;

CREATE TEMP VIEW analytics_name_matches AS
SELECT u.id FROM public.users u CROSS JOIN pg_temp.analytics_name_filter p
WHERE lower(u.full_name) LIKE p.name_pattern ESCAPE '!';

-- Separate live users from purged labels so COALESCE does not wrap the indexed
-- full_name expression. The branches are disjoint, so UNION ALL preserves counts.
CREATE TEMP VIEW analytics_filtered_accounts AS
SELECT a.* FROM pg_temp.analytics_accounts a JOIN pg_temp.analytics_name_matches n ON n.id=a.user_id
UNION ALL
SELECT a.* FROM pg_temp.analytics_accounts a CROSS JOIN pg_temp.analytics_name_filter p
WHERE a.user_id IS NULL AND lower('Người dùng đã xóa') LIKE p.name_pattern ESCAPE '!';

CREATE FUNCTION pg_temp.analytics_count_matches(actual BIGINT, operator TEXT, threshold BIGINT)
RETURNS BOOLEAN LANGUAGE sql IMMUTABLE AS $$
    SELECT CASE WHEN operator IS NULL THEN TRUE
        WHEN operator='=' THEN actual=threshold
        WHEN operator='<' THEN actual<threshold
        WHEN operator='>' THEN actual>threshold ELSE FALSE END
$$;

-- A-05 / A-07. Pass the live account UUID; NULL selects system-wide history.
CREATE TEMP VIEW analytics_login_history AS
SELECT l.id,l.user_id,l.login_at,l.login_at AT TIME ZONE 'Asia/Ho_Chi_Minh' AS local_login_at,
       COALESCE(u.username,'Người dùng đã xóa') AS username,
       COALESCE(u.full_name,'Người dùng đã xóa') AS full_name,l.session_scope
FROM public.login_logs l LEFT JOIN public.users u ON u.id=l.user_id
CROSS JOIN pg_temp.analytics_bounds p
WHERE (p.login_user_id IS NULL OR l.user_id=p.login_user_id)
    AND l.login_at>=p.starts_at AND l.login_at<p.ends_at
ORDER BY l.login_at DESC,l.id DESC;

-- A-12. The retained facts contain only completed accounts, never signup requests.
CREATE TEMP VIEW analytics_new_registrations AS
SELECT a.* FROM pg_temp.analytics_filtered_accounts a CROSS JOIN pg_temp.analytics_bounds p
WHERE a.created_at>=p.starts_at AND a.created_at<p.ends_at
ORDER BY CASE WHEN p.sort_by='name' AND p.sort_direction='ASC' THEN lower(a.full_name) END ASC,
         CASE WHEN p.sort_by='name' AND p.sort_direction='DESC' THEN lower(a.full_name) END DESC,
         CASE WHEN p.sort_by='created_at' AND p.sort_direction='ASC' THEN a.created_at END ASC,
         CASE WHEN p.sort_by='created_at' AND p.sort_direction='DESC' THEN a.created_at END DESC,
         a.sort_id ASC;

CREATE TEMP VIEW analytics_months AS
SELECT m.month_no,
       make_date(p.chart_year,m.month_no,1)::timestamp AT TIME ZONE 'Asia/Ho_Chi_Minh' AS starts_at,
       (make_date(p.chart_year,m.month_no,1)::timestamp + INTERVAL '1 month')
           AT TIME ZONE 'Asia/Ho_Chi_Minh' AS ends_at
FROM pg_temp.analytics_params p CROSS JOIN generate_series(1,12) AS m(month_no);

-- A-13 / A-16. Charts ignore list/name/count filters and always contain 12 months.
CREATE TEMP VIEW analytics_monthly_registrations AS
SELECT m.month_no,count(r.account_key) AS registrations
FROM pg_temp.analytics_months m LEFT JOIN pg_temp.analytics_registration_facts r
    ON r.created_at>=m.starts_at AND r.created_at<m.ends_at
GROUP BY m.month_no ORDER BY m.month_no;

CREATE TEMP VIEW analytics_monthly_openers AS
SELECT m.month_no,count(DISTINCT e.account_key) AS unique_openers
FROM pg_temp.analytics_months m LEFT JOIN pg_temp.analytics_open_facts e
    ON e.opened_at>=m.starts_at AND e.opened_at<m.ends_at
GROUP BY m.month_no ORDER BY m.month_no;

-- A-15. Aggregate each source before joining; only successful sends count.
CREATE TEMP VIEW analytics_activity_counts AS
WITH opens AS (
    SELECT e.account_key,count(*) AS app_opens
    FROM pg_temp.analytics_open_facts e CROSS JOIN pg_temp.analytics_bounds p
    WHERE e.opened_at>=p.starts_at AND e.opened_at<p.ends_at GROUP BY e.account_key
), sends AS (
    SELECT s.sender_key AS account_key,count(DISTINCT s.recipient_key) AS direct_receivers,
           count(DISTINCT s.group_id) AS groups_chatted
    FROM pg_temp.analytics_sends s CROSS JOIN pg_temp.analytics_bounds p
    WHERE s.accepted_at>=p.starts_at AND s.accepted_at<p.ends_at GROUP BY s.sender_key
), active_keys AS (
    SELECT account_key FROM opens UNION SELECT account_key FROM sends
)
SELECT a.*,COALESCE(o.app_opens,0) AS app_opens,COALESCE(s.direct_receivers,0) AS direct_receivers,
       COALESCE(s.groups_chatted,0) AS groups_chatted
FROM active_keys k JOIN pg_temp.analytics_accounts a USING (account_key)
LEFT JOIN opens o USING (account_key) LEFT JOIN sends s USING (account_key);

CREATE TEMP VIEW analytics_activity AS
SELECT a.* FROM pg_temp.analytics_activity_counts a
JOIN pg_temp.analytics_filtered_accounts f ON f.account_key=a.account_key
CROSS JOIN pg_temp.analytics_params p
WHERE pg_temp.analytics_count_matches(CASE p.activity_metric
        WHEN 'app_opens' THEN a.app_opens WHEN 'direct_receivers' THEN a.direct_receivers
        WHEN 'groups_chatted' THEN a.groups_chatted END,p.count_operator,p.count_value)
ORDER BY CASE WHEN p.sort_by='name' AND p.sort_direction='ASC' THEN lower(a.full_name) END ASC,
         CASE WHEN p.sort_by='name' AND p.sort_direction='DESC' THEN lower(a.full_name) END DESC,
         CASE WHEN p.sort_by='created_at' AND p.sort_direction='ASC' THEN a.created_at END ASC,
         CASE WHEN p.sort_by='created_at' AND p.sort_direction='DESC' THEN a.created_at END DESC,
         a.sort_id ASC;

-- A-14. Current graph, independent of the activity/date filters.
CREATE TEMP VIEW analytics_friend_counts AS
WITH live_accounts AS (
    SELECT * FROM pg_temp.analytics_accounts WHERE status IN ('ACTIVE','LOCKED')
), undirected AS (
    SELECT user_a AS root_id,user_b AS friend_id FROM pg_temp.analytics_friendships
    UNION
    SELECT user_b,user_a FROM pg_temp.analytics_friendships
), edges AS (
    SELECT e.* FROM undirected e
    JOIN live_accounts a ON a.user_id=e.root_id JOIN live_accounts b ON b.user_id=e.friend_id
    WHERE e.root_id<>e.friend_id AND NOT EXISTS (
        SELECT FROM pg_temp.analytics_blocks x
        WHERE (x.blocker_id=e.root_id AND x.blocked_id=e.friend_id)
           OR (x.blocked_id=e.root_id AND x.blocker_id=e.friend_id))
), reach AS (
    SELECT root_id,friend_id FROM edges
    UNION
    SELECT a.root_id,b.friend_id FROM edges a JOIN edges b ON b.root_id=a.friend_id
), direct_counts AS (
    SELECT root_id,count(*) AS direct_friends FROM edges GROUP BY root_id
), expanded_counts AS (
    SELECT r.root_id,count(*) AS expanded_friends FROM reach r
    WHERE r.root_id<>r.friend_id AND NOT EXISTS (
        SELECT FROM pg_temp.analytics_blocks x
        WHERE (x.blocker_id=r.root_id AND x.blocked_id=r.friend_id)
           OR (x.blocked_id=r.root_id AND x.blocker_id=r.friend_id))
    GROUP BY r.root_id
)
SELECT a.*,COALESCE(d.direct_friends,0) AS direct_friends,COALESCE(e.expanded_friends,0) AS expanded_friends
FROM live_accounts a LEFT JOIN direct_counts d ON d.root_id=a.user_id
LEFT JOIN expanded_counts e ON e.root_id=a.user_id;

CREATE TEMP VIEW analytics_friend_network AS
SELECT a.* FROM pg_temp.analytics_friend_counts a
JOIN pg_temp.analytics_filtered_accounts f ON f.account_key=a.account_key
CROSS JOIN pg_temp.analytics_params p
WHERE pg_temp.analytics_count_matches(a.direct_friends,p.count_operator,p.count_value)
ORDER BY CASE WHEN p.sort_by='name' AND p.sort_direction='ASC' THEN lower(a.full_name) END ASC,
         CASE WHEN p.sort_by='name' AND p.sort_direction='DESC' THEN lower(a.full_name) END DESC,
         CASE WHEN p.sort_by='created_at' AND p.sort_direction='ASC' THEN a.created_at END ASC,
         CASE WHEN p.sort_by='created_at' AND p.sort_direction='DESC' THEN a.created_at END DESC,
         a.sort_id ASC;

SELECT pg_temp.assert_true('logs-analytics/reference/Vietnam inclusive dates become half-open UTC bounds',
    (SELECT starts_at=TIMESTAMPTZ '2030-10-31 17:00:00+00'
        AND ends_at=TIMESTAMPTZ '2030-11-30 17:00:00+00' FROM analytics_bounds));
SELECT pg_temp.assert_true('logs-analytics/reference/system login query orders by time and ID',
    (SELECT array_agg(id)=ARRAY[pg_temp.id(8,3205),pg_temp.id(8,3204),pg_temp.id(8,3203),
        pg_temp.id(8,3202),pg_temp.id(8,3201)] FROM analytics_login_history));
UPDATE analytics_params SET login_user_id=pg_temp.id(0,201);
SELECT pg_temp.assert_true('logs-analytics/reference/personal login query uses account UUID',
    (SELECT array_agg(id)=ARRAY[pg_temp.id(8,3203),pg_temp.id(8,3202),pg_temp.id(8,3201)]
     FROM analytics_login_history));
UPDATE analytics_params SET login_user_id=NULL;
SELECT pg_temp.assert_true('logs-analytics/reference/registrations count completed accounts including admin-created and deleted',
    (SELECT array_agg(user_id ORDER BY user_id)=ARRAY[
            pg_temp.id(0,201),pg_temp.id(0,204),pg_temp.id(0,206),pg_temp.id(0,207),pg_temp.id(0,208),
            pg_temp.id(0,209),pg_temp.id(0,210),pg_temp.id(0,211),pg_temp.id(0,212)]
        AND count(*) FILTER (WHERE status='DELETED')=1 FROM analytics_new_registrations)
    AND (SELECT count(*)=3 FROM analytics_pending_signups));
\if :analytics_integrated
SELECT pg_temp.assert_true('logs-analytics/integration/self and Admin creation count while temporary requests do not',
    (SELECT EXISTS (
        SELECT FROM public.pending_registrations p JOIN analytics_new_registrations a
            ON a.username=p.username AND a.email=p.email AND a.created_at=p.completed_at
        WHERE p.id=pg_temp.id(1,204) AND a.user_id=pg_temp.id(0,204)))
    AND EXISTS (
        SELECT FROM public.password_reset_tokens t JOIN analytics_new_registrations a ON a.user_id=t.user_id
        WHERE t.id=pg_temp.id(1,206) AND t.purpose='INITIAL_SETUP' AND a.user_id=pg_temp.id(0,206))
    AND (SELECT count(*)=3 AND bool_and(p.completed_at IS NULL) FROM public.pending_registrations p
         JOIN analytics_pending_signups s ON s.request_id=p.id)
    AND NOT EXISTS (
        SELECT FROM public.pending_registrations p JOIN analytics_pending_signups s ON s.request_id=p.id
        JOIN analytics_new_registrations a ON a.username=p.username OR a.email=p.email));
SELECT pg_temp.assert_true('logs-analytics/integration/Admin session alone has one log and zero opens',
    (SELECT count(*)=1 FROM public.user_sessions s JOIN analytics_login_history l
         ON (s.user_id,s.scope,s.created_at)=(l.user_id,l.session_scope,l.login_at)
     WHERE s.id=pg_temp.id(1,207) AND s.scope='ADMIN_APP')
    AND NOT EXISTS (SELECT FROM analytics_open_facts WHERE account_key=pg_temp.analytics_key(207)));
\endif
SELECT pg_temp.assert_true('logs-analytics/reference/registration chart has twelve months including zeros',
    (SELECT array_agg(registrations)=ARRAY[0,0,0,0,0,0,0,0,0,3,9,0]::bigint[]
     FROM analytics_monthly_registrations));
SELECT pg_temp.assert_true('logs-analytics/reference/monthly openers count accounts not opens or daily totals',
    (SELECT array_agg(unique_openers)=ARRAY[0,0,0,0,0,0,0,0,0,1,5,1]::bigint[]
     FROM analytics_monthly_openers));
SELECT pg_temp.assert_true('logs-analytics/reference/activity union and three independently aggregated metrics',
    (SELECT jsonb_object_agg(user_id,jsonb_build_array(app_opens,direct_receivers,groups_chatted))
        = jsonb_build_object(pg_temp.id(0,201),jsonb_build_array(2,2,2),
            pg_temp.id(0,202),jsonb_build_array(0,1,0),pg_temp.id(0,204),jsonb_build_array(1,0,0),
            pg_temp.id(0,205),jsonb_build_array(1,0,0),pg_temp.id(0,208),jsonb_build_array(0,1,0),
            pg_temp.id(0,209),jsonb_build_array(1,0,0),pg_temp.id(0,212),jsonb_build_array(1,1,1))
     FROM analytics_activity));
SELECT pg_temp.assert_true('logs-analytics/reference/previous-month opener sending now has zero current opens',
    (SELECT app_opens=0 AND direct_receivers=1 FROM analytics_activity WHERE user_id=pg_temp.id(0,202))
    AND NOT EXISTS (SELECT FROM analytics_open_facts WHERE account_key=pg_temp.analytics_key(202)
        AND opened_at>='2030-11-01 00:00:00+07' AND opened_at<'2030-12-01 00:00:00+07'));
SELECT pg_temp.assert_true('logs-analytics/reference/receive-only pending rejected and Admin-App-only accounts are inactive',
    NOT EXISTS (SELECT FROM analytics_activity WHERE user_id IN
        (pg_temp.id(0,203),pg_temp.id(0,206),pg_temp.id(0,207),pg_temp.id(0,210)))
    AND public.record_app_open(pg_temp.id(0,207),pg_temp.id(9,2209),'ADMIN_APP') IS NULL);
CREATE TEMP TABLE analytics_before_display_changes AS TABLE analytics_activity_counts;
\if :analytics_integrated
INSERT INTO public.message_deletions (message_id,user_id,deleted_at)
VALUES (pg_temp.id(2,4208),pg_temp.id(0,208),'2030-11-01 10:00:00+07');
SELECT pg_temp.assert_true('logs-analytics/integration/personal deletion keeps the only send for H',
    (SELECT app_opens=0 AND direct_receivers=1 AND groups_chatted=0
     FROM analytics_activity_counts WHERE user_id=pg_temp.id(0,208)));
UPDATE public.messages SET recalled_at='2030-11-01 11:00:00+07',content_plain=NULL
WHERE id=pg_temp.id(2,4208);
INSERT INTO public.messages (id,conversation_id,sender_user_id,created_at,content_plain)
SELECT id,conversation_id,sender_user_id,'2030-11-03 00:00:00+07','Retried message'
FROM public.messages WHERE id=pg_temp.id(2,4201)
ON CONFLICT (id) DO NOTHING;
INSERT INTO public.messages (id,conversation_id,sender_user_id,created_at,content_cipher,crypto_metadata)
SELECT id,conversation_id,sender_user_id,'2030-11-03 00:00:00+07',decode('02','hex'),crypto_metadata
FROM public.messages WHERE id=pg_temp.id(2,4204)
ON CONFLICT (id) DO NOTHING;
SELECT pg_temp.assert_true('logs-analytics/integration/accepted message ID keeps its original send',
    (SELECT count(*)=1 AND min(created_at)='2030-11-01 02:00:00+00'
        AND min(content_plain)='Fixture message' FROM public.messages WHERE id=pg_temp.id(2,4201))
    AND (SELECT created_at='2030-11-01 02:00:00+00' AND content_cipher=decode('01','hex')
         FROM public.messages WHERE id=pg_temp.id(2,4204))
    AND NOT EXISTS (SELECT FROM public.messages WHERE id IN (pg_temp.id(2,4209),pg_temp.id(2,4210)))
    AND (SELECT recalled_at IS NOT NULL AND content_plain IS NULL
         FROM public.messages WHERE id=pg_temp.id(2,4208)));
\else
INSERT INTO analytics_message_inputs
SELECT * FROM analytics_message_inputs WHERE message_id IN (pg_temp.id(2,4201),pg_temp.id(2,4204))
ON CONFLICT (message_id) DO NOTHING;
\endif
SELECT pg_temp.assert_true('logs-analytics/reference/historical activity counts remain unchanged',
    NOT EXISTS (
        (SELECT * FROM analytics_activity_counts EXCEPT ALL SELECT * FROM analytics_before_display_changes)
        UNION ALL
        (SELECT * FROM analytics_before_display_changes EXCEPT ALL SELECT * FROM analytics_activity_counts))
    AND (SELECT count(*)=12 FROM analytics_sends WHERE message_id BETWEEN pg_temp.id(2,4201) AND pg_temp.id(2,4214)));
SELECT pg_temp.assert_true('logs-analytics/reference/friend network matches hand-calculated graph',
    (SELECT jsonb_object_agg(user_id,jsonb_build_array(direct_friends,expanded_friends))
        = jsonb_build_object(pg_temp.id(0,201),jsonb_build_array(2,4),pg_temp.id(0,202),jsonb_build_array(5,7),
            pg_temp.id(0,203),jsonb_build_array(4,7),pg_temp.id(0,204),jsonb_build_array(3,7),
            pg_temp.id(0,206),jsonb_build_array(0,0),pg_temp.id(0,207),jsonb_build_array(1,5),
            pg_temp.id(0,208),jsonb_build_array(1,3),pg_temp.id(0,209),jsonb_build_array(1,4),
            pg_temp.id(0,210),jsonb_build_array(1,3),pg_temp.id(0,212),jsonb_build_array(0,0))
     FROM analytics_friend_network));
SELECT pg_temp.assert_true('logs-analytics/reference/friend expansion keeps locked users excludes deleted and never shrinks direct set',
    (SELECT count(*)=10 AND bool_and(expanded_friends>=direct_friends)
        AND count(*) FILTER (WHERE status='LOCKED')=1
        AND count(*) FILTER (WHERE status IN ('DELETED','PURGED'))=0 FROM analytics_friend_network));

UPDATE analytics_params SET count_operator='=',count_value=0;
SELECT pg_temp.assert_true('logs-analytics/reference/zero friend filter keeps isolated accounts',
    (SELECT array_agg(user_id ORDER BY user_id)=ARRAY[pg_temp.id(0,206),pg_temp.id(0,212)]
     FROM analytics_friend_network));
SELECT pg_temp.assert_true('logs-analytics/reference/zero open filter keeps senders without adding inactive accounts',
    (SELECT array_agg(user_id ORDER BY user_id)=ARRAY[pg_temp.id(0,202),pg_temp.id(0,208)] FROM analytics_activity));
UPDATE analytics_params SET count_operator='=',count_value=2;
SELECT pg_temp.assert_true('logs-analytics/reference/friend equals filter applies to direct count',
    (SELECT array_agg(user_id)=ARRAY[pg_temp.id(0,201)] FROM analytics_friend_network));
UPDATE analytics_params SET count_operator='<';
SELECT pg_temp.assert_true('logs-analytics/reference/friend less-than filter includes zero',
    (SELECT array_agg(user_id ORDER BY user_id)=ARRAY[pg_temp.id(0,206),pg_temp.id(0,207),pg_temp.id(0,208),
        pg_temp.id(0,209),pg_temp.id(0,210),pg_temp.id(0,212)] FROM analytics_friend_network));
SELECT pg_temp.assert_true('logs-analytics/reference/activity less-than filter uses selected metric',
    (SELECT array_agg(user_id ORDER BY user_id)=ARRAY[pg_temp.id(0,202),pg_temp.id(0,204),pg_temp.id(0,205),
        pg_temp.id(0,208),pg_temp.id(0,209),pg_temp.id(0,212)] FROM analytics_activity));
UPDATE analytics_params SET count_operator='>';
SELECT pg_temp.assert_true('logs-analytics/reference/friend greater-than filter applies to direct count',
    (SELECT array_agg(user_id ORDER BY user_id)=ARRAY[pg_temp.id(0,202),pg_temp.id(0,203),pg_temp.id(0,204)]
     FROM analytics_friend_network));
UPDATE analytics_params SET count_value=1;
SELECT pg_temp.assert_true('logs-analytics/reference/activity greater-than filter',
    (SELECT array_agg(user_id)=ARRAY[pg_temp.id(0,201)] FROM analytics_activity));
UPDATE analytics_params SET activity_metric='direct_receivers';
SELECT pg_temp.assert_true('logs-analytics/reference/activity can filter distinct direct receivers',
    (SELECT array_agg(user_id)=ARRAY[pg_temp.id(0,201)] FROM analytics_activity));
UPDATE analytics_params SET activity_metric='groups_chatted',count_operator='=',count_value=2;
SELECT pg_temp.assert_true('logs-analytics/reference/activity can filter distinct groups',
    (SELECT array_agg(user_id)=ARRAY[pg_temp.id(0,201)] FROM analytics_activity));
UPDATE analytics_params SET count_operator=NULL,count_value=NULL,name_query='  aN  ',sort_by='name',sort_direction='ASC';
SELECT pg_temp.assert_true('logs-analytics/reference/all lists trim and case-fold the same substring filter',
    (SELECT array_agg(user_id)=ARRAY[pg_temp.id(0,201),pg_temp.id(0,207)] FROM analytics_new_registrations)
    AND (SELECT array_agg(user_id)=ARRAY[pg_temp.id(0,201)] FROM analytics_activity)
    AND (SELECT array_agg(user_id)=ARRAY[pg_temp.id(0,201),pg_temp.id(0,207)] FROM analytics_friend_network));
SELECT pg_temp.assert_true('logs-analytics/reference/list filters do not change chart counts',
    (SELECT registrations=9 FROM analytics_monthly_registrations WHERE month_no=11)
    AND (SELECT unique_openers=5 FROM analytics_monthly_openers WHERE month_no=11));
UPDATE public.users SET full_name='Nguyễn Minh An' WHERE id=pg_temp.id(0,201);
UPDATE analytics_params SET name_query='  NGUYỄN  ';
SELECT pg_temp.assert_true('logs-analytics/reference/Vietnamese name search ignores case after trim',
    (SELECT array_agg(user_id)=ARRAY[pg_temp.id(0,201)] FROM analytics_new_registrations)
    AND (SELECT array_agg(user_id)=ARRAY[pg_temp.id(0,201)] FROM analytics_activity)
    AND (SELECT array_agg(user_id)=ARRAY[pg_temp.id(0,201)] FROM analytics_friend_network));
UPDATE public.users SET full_name='An' WHERE id=pg_temp.id(0,201);
UPDATE analytics_params SET name_query='%_';
SELECT pg_temp.assert_true('logs-analytics/reference/name percent and underscore are literal substring characters',
    (SELECT array_agg(user_id)=ARRAY[pg_temp.id(0,206)] FROM analytics_new_registrations)
    AND (SELECT array_agg(user_id)=ARRAY[pg_temp.id(0,206)] FROM analytics_friend_network));
UPDATE analytics_params SET name_query='   ';
SELECT pg_temp.assert_true('logs-analytics/reference/blank name filter is removed',
    (SELECT count(*)=9 FROM analytics_new_registrations) AND (SELECT count(*)=7 FROM analytics_activity)
    AND (SELECT count(*)=10 FROM analytics_friend_network));
SELECT pg_temp.assert_true('logs-analytics/reference/name ascending uses account ID as tie breaker',
    (SELECT array_agg(user_id)=ARRAY[pg_temp.id(0,201),pg_temp.id(0,207),pg_temp.id(0,204),
        pg_temp.id(0,206),pg_temp.id(0,211),pg_temp.id(0,208),pg_temp.id(0,209),
        pg_temp.id(0,210),pg_temp.id(0,212)] FROM analytics_new_registrations));
UPDATE analytics_params SET sort_direction='DESC';
SELECT pg_temp.assert_true('logs-analytics/reference/name descending retains a stable ID tie breaker',
    (SELECT array_agg(user_id)=ARRAY[pg_temp.id(0,212),pg_temp.id(0,210),pg_temp.id(0,209),
        pg_temp.id(0,208),pg_temp.id(0,211),pg_temp.id(0,206),pg_temp.id(0,204),
        pg_temp.id(0,201),pg_temp.id(0,207)] FROM analytics_new_registrations));
UPDATE analytics_params SET sort_by='created_at',sort_direction='ASC';
SELECT pg_temp.assert_true('logs-analytics/reference/created ascending with tied timestamps',
    (SELECT array_agg(user_id)=ARRAY[pg_temp.id(0,201),pg_temp.id(0,206),pg_temp.id(0,207),pg_temp.id(0,208),
        pg_temp.id(0,209),pg_temp.id(0,210),pg_temp.id(0,211),pg_temp.id(0,212),pg_temp.id(0,204)]
     FROM analytics_new_registrations));
SELECT pg_temp.assert_true('logs-analytics/reference/activity and friends also sort by creation time',
    (SELECT array_agg(user_id)=ARRAY[pg_temp.id(0,205),pg_temp.id(0,202),pg_temp.id(0,201),
        pg_temp.id(0,208),pg_temp.id(0,209),pg_temp.id(0,212),pg_temp.id(0,204)] FROM analytics_activity)
    AND (SELECT array_agg(user_id)=ARRAY[pg_temp.id(0,202),pg_temp.id(0,203),pg_temp.id(0,201),
        pg_temp.id(0,206),pg_temp.id(0,207),pg_temp.id(0,208),pg_temp.id(0,209),
        pg_temp.id(0,210),pg_temp.id(0,212),pg_temp.id(0,204)] FROM analytics_friend_network));
UPDATE analytics_params SET sort_direction='DESC';
SELECT pg_temp.assert_true('logs-analytics/reference/created descending is the default registration order',
    (SELECT array_agg(user_id)=ARRAY[pg_temp.id(0,204),pg_temp.id(0,212),pg_temp.id(0,201),pg_temp.id(0,206),
        pg_temp.id(0,207),pg_temp.id(0,208),pg_temp.id(0,209),pg_temp.id(0,210),pg_temp.id(0,211)]
     FROM analytics_new_registrations));

SELECT pg_temp.expect_error('logs-analytics/reference/reversed dates are rejected',
    $$UPDATE analytics_params SET from_date='2030-12-01'$$,'23514');
SELECT pg_temp.expect_error('logs-analytics/reference/invalid chart year is rejected',
    $$UPDATE analytics_params SET chart_year=0$$,'23514');
SELECT pg_temp.expect_error('logs-analytics/reference/negative threshold is rejected',
    $$UPDATE analytics_params SET count_operator='=',count_value=-1$$,'23514');
SELECT pg_temp.expect_error('logs-analytics/reference/unknown comparison is rejected',
    $$UPDATE analytics_params SET count_operator='!=',count_value=0$$,'23514');
SELECT pg_temp.expect_error('logs-analytics/reference/unknown sort is rejected',
    $$UPDATE analytics_params SET sort_by='email'$$,'23514');
UPDATE analytics_params SET chart_year=2031,from_date='2031-01-01',through_date='2031-12-31';
SELECT pg_temp.assert_true('logs-analytics/reference/empty year returns twelve zeros not missing rows',
    (SELECT count(*)=12 AND bool_and(registrations=0) FROM analytics_monthly_registrations)
    AND (SELECT count(*)=12 AND bool_and(unique_openers=0) FROM analytics_monthly_openers)
    AND NOT EXISTS (SELECT FROM analytics_new_registrations) AND NOT EXISTS (SELECT FROM analytics_activity));
SELECT pg_temp.assert_true('logs-analytics/reference/friend graph is not limited by an activity date range',
    (SELECT count(*)=10 FROM analytics_friend_network));
UPDATE analytics_params SET chart_year=2030;
DO $$
DECLARE m INTEGER; expected BIGINT; actual BIGINT; matches BOOLEAN := TRUE;
BEGIN
    FOR m IN 1..12 LOOP
        UPDATE analytics_params SET from_date=make_date(2030,m,1),
            through_date=(make_date(2030,m,1)+INTERVAL '1 month'-INTERVAL '1 day')::date;
        SELECT registrations INTO expected FROM analytics_monthly_registrations WHERE month_no=m;
        SELECT count(*) INTO actual FROM analytics_new_registrations;
        matches := matches AND actual=expected;
    END LOOP;
    PERFORM pg_temp.assert_true('logs-analytics/reference/each registration chart month matches its unfiltered detail list',matches);
END $$;
UPDATE analytics_params SET from_date='2030-11-01',through_date='2030-11-30',name_query=NULL;

-- Purge detaches identity without deleting persistent historical facts.
INSERT INTO public.login_logs (id,user_id,login_at,session_scope)
VALUES (pg_temp.id(8,3212),pg_temp.id(0,212),'2030-11-20 05:00:00+00','USER_CLIENT');
\if :analytics_integrated
INSERT INTO public.user_sessions (id,user_id,scope,token_hash,created_at)
SELECT pg_temp.id(1,212),user_id,session_scope,'fixture-purged-session',login_at
FROM public.login_logs WHERE id=pg_temp.id(8,3212);
\endif
UPDATE public.users SET status='DELETED',status_before_delete='ACTIVE',deleted_at='2030-11-29 00:00:00+07'
WHERE id=pg_temp.id(0,212);
SELECT pg_temp.assert_true('logs-analytics/reference/soft deletion keeps L in history and activity with its UUID',
    (SELECT status='DELETED' FROM analytics_new_registrations WHERE account_key=pg_temp.analytics_key(212))
    AND (SELECT user_id=pg_temp.id(0,212) FROM analytics_login_history WHERE id=pg_temp.id(8,3212))
    AND (SELECT app_opens=1 AND direct_receivers=1 AND groups_chatted=1
         FROM analytics_activity WHERE user_id=pg_temp.id(0,212))
    AND NOT EXISTS (SELECT FROM analytics_friend_network WHERE user_id=pg_temp.id(0,212)));
DELETE FROM public.users WHERE id=pg_temp.id(0,212);
SELECT pg_temp.assert_true('logs-analytics/reference/purge keeps registration activity and monthly opener totals',
    (SELECT registrations=9 FROM analytics_monthly_registrations WHERE month_no=11)
    AND (SELECT unique_openers=5 FROM analytics_monthly_openers WHERE month_no=11)
    AND (SELECT count(*)=7 FROM analytics_activity)
    AND (SELECT user_id IS NULL AND username='Người dùng đã xóa'
         FROM analytics_login_history WHERE id=pg_temp.id(8,3212))
    AND (SELECT user_id IS NULL AND full_name='Người dùng đã xóa'
                AND app_opens=1 AND direct_receivers=1 AND groups_chatted=1
         FROM analytics_activity WHERE account_key=pg_temp.analytics_key(212)));
INSERT INTO public.users (id,username,email,full_name,created_at)
VALUES (pg_temp.id(0,213),'ref0212','ref212@example.test','New Lost','2030-11-30 17:00:00+00');
INSERT INTO analytics_fixture_keys
SELECT 213,account_key FROM public.app_open_accounts WHERE user_id=pg_temp.id(0,213);
UPDATE analytics_params SET login_user_id=pg_temp.id(0,213);
SELECT pg_temp.assert_true('logs-analytics/reference/username reuse does not transfer historical registrations or activity',
    (SELECT user_id IS NULL AND full_name='Người dùng đã xóa' FROM analytics_new_registrations
     WHERE account_key=pg_temp.analytics_key(212))
    AND NOT EXISTS (SELECT FROM analytics_activity WHERE user_id=pg_temp.id(0,213))
    AND NOT EXISTS (SELECT FROM analytics_login_history)
    AND (SELECT registrations=1 FROM analytics_monthly_registrations WHERE month_no=12)
    AND (SELECT unique_openers=5 FROM analytics_monthly_openers WHERE month_no=11));
UPDATE analytics_params SET login_user_id=NULL;
\if :analytics_integrated
SELECT pg_temp.assert_true('logs-analytics/integration/purge removes sessions while keeping anonymized login and app open',
    NOT EXISTS (SELECT FROM public.user_sessions WHERE id=pg_temp.id(1,212))
    AND (SELECT user_id IS NULL FROM public.login_logs WHERE id=pg_temp.id(8,3212))
    AND (SELECT count(*)=1 FROM public.app_open_events WHERE account_key=pg_temp.analytics_key(212)));
SELECT pg_temp.assert_true('logs-analytics/integration/purged sender keeps durable accepted-send metadata',
    (SELECT count(*)=2 AND bool_and(sender_user_id IS NULL) FROM public.messages
     WHERE id IN (pg_temp.id(2,4213),pg_temp.id(2,4214)))
    AND (SELECT count(*)=2 FROM public.message_send_events WHERE sender_key=pg_temp.analytics_key(212)));
\endif

INSERT INTO public.app_open_events (id,account_key,launch_id,opened_at) VALUES
(pg_temp.id(8,2210),pg_temp.analytics_key(201),pg_temp.id(9,2210),'2030-12-31 16:59:59.999999+00'),
(pg_temp.id(8,2211),pg_temp.analytics_key(201),pg_temp.id(9,2211),'2030-12-31 17:00:00+00');
UPDATE analytics_params SET from_date='2030-12-31',through_date='2030-12-31';
SELECT pg_temp.assert_true('logs-analytics/reference/last day of year excludes midnight of next Vietnam year',
    (SELECT app_opens=1 AND direct_receivers=0 AND groups_chatted=0
     FROM analytics_activity WHERE user_id=pg_temp.id(0,201))
    AND (SELECT unique_openers=2 FROM analytics_monthly_openers WHERE month_no=12));
UPDATE analytics_params SET chart_year=2031,from_date='2031-01-01',through_date='2031-01-01';
SELECT pg_temp.assert_true('logs-analytics/reference/midnight starts next Vietnam day month and year',
    (SELECT app_opens=1 FROM analytics_activity WHERE user_id=pg_temp.id(0,201))
    AND (SELECT array_agg(unique_openers)=ARRAY[1,0,0,0,0,0,0,0,0,0,0,0]::bigint[]
         FROM analytics_monthly_openers));

-- Step 5.2: schema contracts, not planner/timing assertions on tiny fixtures.
SELECT pg_temp.assert_true('logs-analytics/indexes/system login order is covered',
    EXISTS (SELECT FROM pg_indexes WHERE schemaname='public' AND tablename='login_logs'
        AND indexdef LIKE '%USING btree (login_at DESC, id DESC)'));
SELECT pg_temp.assert_true('logs-analytics/indexes/account login order is covered',
    EXISTS (SELECT FROM pg_indexes WHERE schemaname='public' AND tablename='login_logs'
        AND indexdef LIKE '%USING btree (user_id, login_at DESC, id DESC)'));
SELECT pg_temp.assert_true('logs-analytics/indexes/date-first app-open aggregate is covered',
    EXISTS (SELECT FROM pg_indexes WHERE schemaname='public' AND tablename='app_open_events'
        AND indexdef LIKE '%USING btree (opened_at, account_key)'));
SELECT pg_temp.assert_true('logs-analytics/indexes/account-first date-window count is covered',
    EXISTS (SELECT FROM pg_indexes WHERE schemaname='public' AND tablename='app_open_events'
        AND indexdef LIKE '%USING btree (account_key, opened_at)'));
SELECT pg_temp.assert_true('logs-analytics/indexes/shared name trigram index is reused',
    EXISTS (SELECT FROM pg_indexes WHERE schemaname='public' AND tablename='users'
        AND indexdef LIKE '%USING gin (lower(full_name) gin_trgm_ops)'));
SELECT pg_temp.assert_true('logs-analytics/indexes/no duplicate equivalent indexes on owned event tables',
    NOT EXISTS (SELECT FROM pg_index a JOIN pg_index b
        ON b.indrelid=a.indrelid AND b.indexrelid>a.indexrelid
        AND b.indkey=a.indkey AND b.indclass=a.indclass AND b.indoption=a.indoption
        AND b.indcollation=a.indcollation
        AND pg_get_expr(b.indexprs,b.indrelid) IS NOT DISTINCT FROM pg_get_expr(a.indexprs,a.indrelid)
        AND pg_get_expr(b.indpred,b.indrelid) IS NOT DISTINCT FROM pg_get_expr(a.indpred,a.indrelid)
        WHERE a.indrelid IN ('public.login_logs'::regclass,'public.app_open_events'::regclass,
            'public.app_open_accounts'::regclass)));
UPDATE public.users SET full_name='Free! \%_' WHERE id=pg_temp.id(0,206);
UPDATE analytics_params SET from_date='2030-11-01',through_date='2030-11-30',name_query='! \%_';
SELECT pg_temp.assert_true('logs-analytics/indexes/GIN-compatible filter keeps escape backslash percent underscore literal',
    (SELECT array_agg(user_id)=ARRAY[pg_temp.id(0,206)] FROM analytics_new_registrations)
    AND (SELECT array_agg(user_id)=ARRAY[pg_temp.id(0,206)] FROM analytics_friend_network)
    AND NOT EXISTS (SELECT FROM analytics_activity));
UPDATE analytics_params SET name_query='  NGƯỜI DÙNG ĐÃ XÓA  ';
SELECT pg_temp.assert_true('logs-analytics/indexes/purged-label branch remains searchable after name optimization',
    (SELECT array_agg(account_key)=ARRAY[pg_temp.analytics_key(212)] FROM analytics_new_registrations)
    AND (SELECT array_agg(account_key)=ARRAY[pg_temp.analytics_key(212)] FROM analytics_activity)
    AND NOT EXISTS (SELECT FROM analytics_friend_network));

-- Durable retention contract: these probes query only deployed public ledgers.
-- Historical dates deliberately exceed both suggested 12/24-month alternatives.
INSERT INTO public.users (id,username,email,full_name,role,created_at) VALUES
(pg_temp.id(0,601),'ledger601','ledger601@example.test','Ledger admin','ADMIN','2000-01-01 UTC'),
(pg_temp.id(0,602),'ledger602','ledger602@example.test','Ledger recipient B','USER','2000-01-01 UTC'),
(pg_temp.id(0,603),'ledger603','ledger603@example.test','Ledger recipient C','USER','2000-01-01 UTC');
CREATE TEMP TABLE analytics_retention_probe_keys AS
SELECT user_id,account_key FROM public.app_open_accounts
WHERE user_id IN (pg_temp.id(0,601),pg_temp.id(0,602),pg_temp.id(0,603));

SELECT pg_temp.assert_true('logs-analytics/retention/all accounts register before any app open',
    (SELECT count(*)=3 AND bool_and(r.registered_at=TIMESTAMPTZ '2000-01-01 UTC')
     FROM public.account_registration_events r JOIN analytics_retention_probe_keys k USING (account_key))
    AND NOT EXISTS (SELECT FROM public.app_open_events e
        JOIN analytics_retention_probe_keys k USING (account_key)));
SELECT pg_temp.assert_true('logs-analytics/retention/CLI-style ADMIN creation counts once',
    (SELECT count(*)=1 FROM public.account_registration_events r
     JOIN public.app_open_accounts a USING (account_key) JOIN public.users u ON u.id=a.user_id
     WHERE u.id=pg_temp.id(0,601) AND u.role='ADMIN'));
SELECT pg_temp.expect_error('logs-analytics/retention/live identity cannot be detached before purge',
    $$UPDATE public.app_open_accounts SET user_id=NULL WHERE user_id=pg_temp.id(0,601)$$,
    '23514','statistics_identity_immutable');
SELECT pg_temp.expect_error('logs-analytics/retention/statistics key is immutable',
    $$UPDATE public.app_open_accounts SET account_key=pg_temp.id(9,6690) WHERE user_id=pg_temp.id(0,601)$$,
    '23514','statistics_identity_immutable');
UPDATE public.users SET created_at='2000-01-02 UTC' WHERE id=pg_temp.id(0,601);
SELECT pg_temp.assert_true('logs-analytics/retention/profile writes do not rewrite original registration time',
    (SELECT registered_at=TIMESTAMPTZ '2000-01-01 UTC' FROM public.account_registration_events
     WHERE account_key=(SELECT account_key FROM analytics_retention_probe_keys WHERE user_id=pg_temp.id(0,601))));
SELECT pg_temp.expect_error('logs-analytics/retention/registration events are immutable',
    $$UPDATE public.account_registration_events SET registered_at=registered_at+INTERVAL '1 day'
      WHERE account_key=(SELECT account_key FROM analytics_retention_probe_keys WHERE user_id=pg_temp.id(0,601))$$,
    '23514','statistics_event_immutable');

\if :analytics_integrated
INSERT INTO public.conversations (id,type,group_name,created_at)
VALUES (pg_temp.id(1,6601),'GROUP','Retention fixture','2000-01-01 UTC');
INSERT INTO public.conversation_members (conversation_id,user_id,role,joined_at)
SELECT pg_temp.id(1,6601),pg_temp.id(0,n),CASE WHEN n=601 THEN 'ADMIN' ELSE 'MEMBER' END,'2000-01-02 UTC'::timestamptz
FROM generate_series(601,603) n;
INSERT INTO public.messages (id,conversation_id,sender_user_id,created_at,content_cipher,crypto_metadata)
VALUES (pg_temp.id(2,6603),pg_temp.id(1,6601),pg_temp.id(0,601),'2001-10-12 UTC',decode('01','hex'),'{}');
\endif

DO $$ BEGIN
    PERFORM public.record_message_send(pg_temp.id(2,6601),pg_temp.id(0,601),pg_temp.id(0,602),NULL,'2001-10-10 UTC');
    PERFORM public.record_message_send(pg_temp.id(2,6602),pg_temp.id(0,601),pg_temp.id(0,603),NULL,'2001-10-11 UTC');
    PERFORM public.record_message_send(pg_temp.id(2,6603),pg_temp.id(0,601),NULL,pg_temp.id(1,6601),'2001-10-12 UTC');
    PERFORM public.record_message_send(pg_temp.id(2,6604),pg_temp.id(0,601),NULL,pg_temp.id(1,6601),'2001-10-13 UTC');
END $$;
SELECT pg_temp.assert_true('logs-analytics/retention/distinct historical destinations are two people and one group',
    (SELECT count(*)=4 AND count(DISTINCT recipient_key)=2 AND count(DISTINCT group_id)=1
     FROM public.message_send_events
     WHERE sender_key=(SELECT account_key FROM analytics_retention_probe_keys WHERE user_id=pg_temp.id(0,601))));
DO $$ BEGIN
    PERFORM public.record_message_send(pg_temp.id(2,6601),pg_temp.id(0,601),pg_temp.id(0,602),NULL,'2001-11-01 UTC');
END $$;
SELECT pg_temp.assert_true('logs-analytics/retention/retry preserves first acceptance month',
    (SELECT accepted_at=TIMESTAMPTZ '2001-10-10 UTC' FROM public.message_send_events WHERE message_id=pg_temp.id(2,6601))
    AND (SELECT count(*)=4 FROM public.message_send_events
        WHERE sender_key=(SELECT account_key FROM analytics_retention_probe_keys WHERE user_id=pg_temp.id(0,601))));
SELECT pg_temp.expect_error('logs-analytics/retention/message ID cannot change recipient',
    $$SELECT public.record_message_send(pg_temp.id(2,6601),pg_temp.id(0,601),pg_temp.id(0,603),NULL,'2001-10-10 UTC')$$,
    '23505','message_send_events_pkey');
SELECT pg_temp.expect_error('logs-analytics/retention/message ID cannot change sender',
    $$SELECT public.record_message_send(pg_temp.id(2,6601),pg_temp.id(0,603),pg_temp.id(0,602),NULL,'2001-10-10 UTC')$$,
    '23505','message_send_events_pkey');
SELECT pg_temp.expect_error('logs-analytics/retention/message ID cannot change destination type',
    $$SELECT public.record_message_send(pg_temp.id(2,6601),pg_temp.id(0,601),NULL,pg_temp.id(1,6601),'2001-10-10 UTC')$$,
    '23505','message_send_events_pkey');
SELECT pg_temp.expect_error('logs-analytics/retention/send event time cannot be rewritten',
    $$UPDATE public.message_send_events SET accepted_at=accepted_at+INTERVAL '1 day' WHERE message_id=pg_temp.id(2,6601)$$,
    '23514','statistics_event_immutable');
SELECT pg_temp.expect_error('logs-analytics/retention/only one destination is accepted',
    $$SELECT public.record_message_send(pg_temp.id(2,6690),pg_temp.id(0,601),pg_temp.id(0,602),pg_temp.id(1,6601),'2001-10-10 UTC')$$,
    '22023');
SELECT pg_temp.expect_error('logs-analytics/retention/destination is required',
    $$SELECT public.record_message_send(pg_temp.id(2,6690),pg_temp.id(0,601),NULL,NULL,'2001-10-10 UTC')$$,'22023');
SELECT pg_temp.expect_error('logs-analytics/retention/self-send is not a direct destination',
    $$SELECT public.record_message_send(pg_temp.id(2,6690),pg_temp.id(0,601),pg_temp.id(0,601),NULL,'2001-10-10 UTC')$$,
    '23514','message_send_events_destination_check');
SELECT pg_temp.expect_error('logs-analytics/retention/send ID is required',
    $$SELECT public.record_message_send(NULL,pg_temp.id(0,601),pg_temp.id(0,602),NULL,'2001-10-10 UTC')$$,'22004');
SELECT pg_temp.expect_error('logs-analytics/retention/acceptance time is required',
    $$SELECT public.record_message_send(pg_temp.id(2,6690),pg_temp.id(0,601),pg_temp.id(0,602),NULL,NULL)$$,'22004');
SELECT pg_temp.expect_error('logs-analytics/retention/acceptance time must be finite',
    $$SELECT public.record_message_send(pg_temp.id(2,6690),pg_temp.id(0,601),pg_temp.id(0,602),NULL,'infinity')$$,'22023');
SELECT pg_temp.expect_error('logs-analytics/retention/unknown sender is rejected',
    $$SELECT public.record_message_send(pg_temp.id(2,6690),pg_temp.id(0,699),pg_temp.id(0,602),NULL,'2001-10-10 UTC')$$,
    '23503','message_send_events_sender_key_fkey');
SELECT pg_temp.expect_error('logs-analytics/retention/unknown recipient is rejected',
    $$SELECT public.record_message_send(pg_temp.id(2,6690),pg_temp.id(0,601),pg_temp.id(0,699),NULL,'2001-10-10 UTC')$$,
    '23503','message_send_events_recipient_key_fkey');
SELECT pg_temp.expect_error('logs-analytics/retention/registration key cannot be physically removed',
    $$DELETE FROM public.app_open_accounts WHERE user_id=pg_temp.id(0,603)$$,'23001');

SAVEPOINT analytics_accepted_send_rollback;
\if :analytics_integrated
INSERT INTO public.messages (id,conversation_id,sender_user_id,created_at,content_cipher,crypto_metadata)
VALUES (pg_temp.id(2,6699),pg_temp.id(1,6601),pg_temp.id(0,601),'2001-10-12 UTC',decode('02','hex'),'{}');
\endif
DO $$ BEGIN
    PERFORM public.record_message_send(pg_temp.id(2,6699),pg_temp.id(0,601),NULL,pg_temp.id(1,6601),'2001-10-12 UTC');
END $$;
ROLLBACK TO SAVEPOINT analytics_accepted_send_rollback;
RELEASE SAVEPOINT analytics_accepted_send_rollback;
SELECT pg_temp.assert_true('logs-analytics/retention/aborted acceptance transaction leaves no send event',
    NOT EXISTS (SELECT FROM public.message_send_events WHERE message_id=pg_temp.id(2,6699)));
\if :analytics_integrated
SELECT pg_temp.assert_true('logs-analytics/integration/aborted acceptance also removes the live message',
    NOT EXISTS (SELECT FROM public.messages WHERE id=pg_temp.id(2,6699)));
INSERT INTO public.message_deletions (message_id,user_id) VALUES (pg_temp.id(2,6603),pg_temp.id(0,601));
UPDATE public.messages SET content_cipher=NULL,crypto_metadata=NULL,recalled_at=created_at+INTERVAL '1 hour'
WHERE id=pg_temp.id(2,6603);
SELECT pg_temp.assert_true('logs-analytics/integration/self-deletion and recall preserve accepted-send metadata',
    EXISTS (SELECT FROM public.message_send_events WHERE message_id=pg_temp.id(2,6603)));
UPDATE public.conversations SET closed_at='2002-01-01 UTC' WHERE id=pg_temp.id(1,6601);
SELECT pg_temp.assert_true('logs-analytics/integration/closing a group keeps historical distinct group count',
    (SELECT count(DISTINCT group_id)=1 FROM public.message_send_events
     WHERE sender_key=(SELECT account_key FROM analytics_retention_probe_keys WHERE user_id=pg_temp.id(0,601))));
DELETE FROM public.messages WHERE id=pg_temp.id(2,6603);
DELETE FROM public.conversations WHERE id=pg_temp.id(1,6601);
SELECT pg_temp.assert_true('logs-analytics/integration/removing live message and group does not erase send facts',
    (SELECT count(*)=2 AND count(DISTINCT group_id)=1 FROM public.message_send_events WHERE group_id=pg_temp.id(1,6601)));
\endif

DELETE FROM public.users WHERE id=pg_temp.id(0,602);
SELECT pg_temp.assert_true('logs-analytics/retention/recipient purge keeps both historical direct destinations',
    (SELECT count(DISTINCT recipient_key)=2 FROM public.message_send_events
     WHERE sender_key=(SELECT account_key FROM analytics_retention_probe_keys WHERE user_id=pg_temp.id(0,601)))
    AND (SELECT user_id IS NULL FROM public.app_open_accounts
        WHERE account_key=(SELECT account_key FROM analytics_retention_probe_keys WHERE user_id=pg_temp.id(0,602))));
SELECT pg_temp.expect_error('logs-analytics/retention/purged recipient cannot be relinked by another send',
    $$SELECT public.record_message_send(pg_temp.id(2,6690),pg_temp.id(0,601),pg_temp.id(0,602),NULL,'2002-10-10 UTC')$$,
    '23503','message_send_events_recipient_key_fkey');
INSERT INTO public.users (id,username,email,full_name,created_at)
VALUES (pg_temp.id(0,604),'ledger602','ledger602@example.test','New account, same name','2002-01-01 UTC');
DO $$ BEGIN
    PERFORM public.record_message_send(pg_temp.id(2,6605),pg_temp.id(0,601),pg_temp.id(0,604),NULL,'2002-10-10 UTC');
END $$;
SELECT pg_temp.assert_true('logs-analytics/retention/reused username has its own registration and statistics identity',
    (SELECT a.account_key<>old.account_key AND r.registered_at=TIMESTAMPTZ '2002-01-01 UTC'
     FROM public.app_open_accounts a JOIN public.account_registration_events r USING (account_key)
     CROSS JOIN analytics_retention_probe_keys old
     WHERE a.user_id=pg_temp.id(0,604) AND old.user_id=pg_temp.id(0,602)));
SELECT pg_temp.expect_error('logs-analytics/retention/purged identity cannot be assigned to a new account',
    $$UPDATE public.app_open_accounts SET user_id=pg_temp.id(0,604)
      WHERE account_key=(SELECT account_key FROM analytics_retention_probe_keys WHERE user_id=pg_temp.id(0,602))$$,
    '23514','statistics_identity_immutable');
SELECT pg_temp.assert_true('logs-analytics/retention/new account does not merge into old historical destination',
    (SELECT count(DISTINCT recipient_key)=3 FROM public.message_send_events
     WHERE sender_key=(SELECT account_key FROM analytics_retention_probe_keys WHERE user_id=pg_temp.id(0,601)))
    AND (SELECT count(DISTINCT recipient_key)=2 FROM public.message_send_events
        WHERE sender_key=(SELECT account_key FROM analytics_retention_probe_keys WHERE user_id=pg_temp.id(0,601))
            AND accepted_at>=TIMESTAMPTZ '2001-10-01 UTC' AND accepted_at<TIMESTAMPTZ '2001-11-01 UTC'));
DELETE FROM public.users WHERE id=pg_temp.id(0,601);
SELECT pg_temp.assert_true('logs-analytics/retention/sender purge preserves registration and five accepted sends',
    (SELECT count(*)=3 FROM public.account_registration_events r JOIN analytics_retention_probe_keys k USING (account_key))
    AND (SELECT count(*)=5 FROM public.message_send_events
        WHERE sender_key=(SELECT account_key FROM analytics_retention_probe_keys WHERE user_id=pg_temp.id(0,601)))
    AND (SELECT user_id IS NULL FROM public.app_open_accounts
        WHERE account_key=(SELECT account_key FROM analytics_retention_probe_keys WHERE user_id=pg_temp.id(0,601))));
SELECT pg_temp.expect_error('logs-analytics/retention/purged sender cannot create or resurrect send history',
    $$SELECT public.record_message_send(pg_temp.id(2,6690),pg_temp.id(0,601),pg_temp.id(0,604),NULL,'2002-10-10 UTC')$$,
    '23503','message_send_events_sender_key_fkey');
SELECT pg_temp.assert_true('logs-analytics/retention/send ledger contains metadata only',
    (SELECT array_agg(column_name::text ORDER BY ordinal_position)
        = ARRAY['message_id','sender_key','recipient_key','group_id','accepted_at']
     FROM information_schema.columns WHERE table_schema='public' AND table_name='message_send_events'));
