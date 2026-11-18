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

-- Step 5.1: executable reference queries, with explicit TEMP source contracts.
-- Missing registration-retention/chat/friend adapters are not production tables.
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
CREATE FUNCTION pg_temp.analytics_key(number INTEGER) RETURNS UUID LANGUAGE sql IMMUTABLE AS $$
    SELECT pg_temp.id(8,number+1000)
$$;

INSERT INTO public.users (id,username,email,full_name,role,status,created_at,deleted_at) VALUES
(pg_temp.id(0,201),'ref0201','ref201@example.test','An','USER','ACTIVE','2030-10-31 17:00:00+00',NULL),
(pg_temp.id(0,202),'ref0202','ref202@example.test','Binh','USER','LOCKED','2030-10-30 17:00:00+00',NULL),
(pg_temp.id(0,203),'ref0203','ref203@example.test','Chi','USER','ACTIVE','2030-10-31 16:59:59.999999+00',NULL),
(pg_temp.id(0,204),'ref0204','ref204@example.test','Dung','USER','ACTIVE','2030-11-30 16:59:59.999999+00',NULL),
(pg_temp.id(0,205),'ref0205','ref205@example.test','Echo','USER','DELETED','2030-10-14 17:00:00+00','2030-11-15 17:00:00+00'),
(pg_temp.id(0,206),'ref0206','ref206@example.test','Free %_','USER','ACTIVE','2030-10-31 17:00:00+00',NULL),
(pg_temp.id(0,207),'ref0207','ref207@example.test','An','ADMIN','ACTIVE','2030-10-31 17:00:00+00',NULL),
(pg_temp.id(0,208),'ref0208','ref208@example.test','Huy','USER','ACTIVE','2030-10-31 17:00:00+00',NULL),
(pg_temp.id(0,209),'ref0209','ref209@example.test','Ivy','USER','ACTIVE','2030-10-31 17:00:00+00',NULL),
(pg_temp.id(0,210),'ref0210','ref210@example.test','Jade','USER','ACTIVE','2030-10-31 17:00:00+00',NULL),
(pg_temp.id(0,211),'ref0211','ref211@example.test','Gone','USER','DELETED','2030-10-31 17:00:00+00','2030-11-09 17:00:00+00'),
(pg_temp.id(0,212),'ref0212','ref212@example.test','Lost','USER','ACTIVE','2030-11-19 17:00:00+00',NULL);
INSERT INTO public.app_open_accounts (account_key,user_id)
SELECT pg_temp.analytics_key(n),pg_temp.id(0,n) FROM generate_series(201,212) n;

-- Prototype of the retained registration input, to be agreed with member 2.
CREATE TEMP TABLE analytics_registration_facts (
    account_key UUID PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL
);
INSERT INTO analytics_registration_facts
SELECT a.account_key,u.created_at FROM public.app_open_accounts a
JOIN public.users u ON u.id=a.user_id WHERE u.username LIKE 'ref02%';
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
    sender_key UUID NOT NULL REFERENCES analytics_registration_facts(account_key),
    recipient_key UUID REFERENCES analytics_registration_facts(account_key), group_id UUID,
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
(pg_temp.id(2,4211),pg_temp.analytics_key(201),pg_temp.analytics_key(209),NULL,'2030-10-31 16:59:59.999999+00'),
(pg_temp.id(2,4212),pg_temp.analytics_key(201),pg_temp.analytics_key(209),NULL,'2030-11-30 17:00:00+00');
CREATE TEMP TABLE analytics_message_display_state (
    message_id UUID PRIMARY KEY REFERENCES analytics_message_inputs(message_id), recalled BOOLEAN, hidden BOOLEAN
);
INSERT INTO analytics_message_display_state VALUES (pg_temp.id(2,4208),FALSE,FALSE);

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

\ir ../queries/analytics-reference.sql

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
    (SELECT count(*)=9 AND count(*) FILTER (WHERE user_id=pg_temp.id(0,204))=1
        AND count(*) FILTER (WHERE user_id=pg_temp.id(0,207))=1
        AND count(*) FILTER (WHERE status='DELETED')=1 FROM analytics_new_registrations)
    AND (SELECT count(*)=3 FROM analytics_pending_signups));
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
            pg_temp.id(0,209),jsonb_build_array(1,0,0),pg_temp.id(0,212),jsonb_build_array(1,0,0))
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
UPDATE analytics_message_display_state SET recalled=TRUE,hidden=TRUE;
INSERT INTO analytics_message_inputs
SELECT * FROM analytics_message_inputs WHERE message_id=pg_temp.id(2,4201) ON CONFLICT (message_id) DO NOTHING;
SELECT pg_temp.assert_true('logs-analytics/reference/send retry recall and personal deletion do not change metrics',
    NOT EXISTS (
        (SELECT * FROM analytics_activity_counts EXCEPT ALL SELECT * FROM analytics_before_display_changes)
        UNION ALL
        (SELECT * FROM analytics_before_display_changes EXCEPT ALL SELECT * FROM analytics_activity_counts))
    AND (SELECT count(*)=12 FROM analytics_message_inputs));
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

-- Retention-input prototype: purge detaches identity without deleting historical facts.
DELETE FROM public.users WHERE id=pg_temp.id(0,212);
SELECT pg_temp.assert_true('logs-analytics/reference/purge keeps registration activity and monthly opener totals',
    (SELECT registrations=9 FROM analytics_monthly_registrations WHERE month_no=11)
    AND (SELECT unique_openers=5 FROM analytics_monthly_openers WHERE month_no=11)
    AND (SELECT count(*)=7 FROM analytics_activity)
    AND (SELECT user_id IS NULL AND full_name='Người dùng đã xóa' AND app_opens=1
         FROM analytics_activity WHERE account_key=pg_temp.analytics_key(212)));
INSERT INTO public.users (id,username,email,full_name,created_at)
VALUES (pg_temp.id(0,213),'ref0212','ref212@example.test','New Lost','2030-11-30 17:00:00+00');
INSERT INTO public.app_open_accounts (account_key,user_id) VALUES (pg_temp.analytics_key(213),pg_temp.id(0,213));
INSERT INTO analytics_registration_facts VALUES (pg_temp.analytics_key(213),'2030-11-30 17:00:00+00');
SELECT pg_temp.assert_true('logs-analytics/reference/username reuse does not transfer historical registrations or activity',
    (SELECT user_id IS NULL AND full_name='Người dùng đã xóa' FROM analytics_new_registrations
     WHERE account_key=pg_temp.analytics_key(212))
    AND NOT EXISTS (SELECT FROM analytics_activity WHERE user_id=pg_temp.id(0,213))
    AND (SELECT registrations=1 FROM analytics_monthly_registrations WHERE month_no=12)
    AND (SELECT unique_openers=5 FROM analytics_monthly_openers WHERE month_no=11));

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
