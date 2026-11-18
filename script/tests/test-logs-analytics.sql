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
