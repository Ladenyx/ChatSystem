-- Shared smoke checks for the canonical users anchor.
SELECT pg_temp.assert_true('core/PostgreSQL 18 with required extensions',
    current_setting('server_version_num')::int BETWEEN 180000 AND 189999
    AND (SELECT count(*)=2 FROM pg_extension WHERE extname IN ('pg_trgm','unaccent')));
SELECT pg_temp.assert_true('core/fixed fixture identities loaded',
    (SELECT count(*)=4 AND count(*) FILTER(WHERE role='ADMIN')=1 FROM users));
SELECT pg_temp.assert_true('core/one UUID primary key',
    EXISTS (SELECT FROM pg_constraint c JOIN pg_attribute a ON a.attrelid=c.conrelid AND a.attnum=ANY(c.conkey)
        WHERE c.conrelid='users'::regclass AND c.contype='p' AND cardinality(c.conkey)=1
            AND a.attname='id' AND a.atttypid='uuid'::regtype));
SELECT pg_temp.expect_error('core/duplicate identity rejected',
    $$INSERT INTO users SELECT * FROM users LIMIT 1$$,'23505');
SELECT pg_temp.expect_error('core/username uniqueness ignores case',
    $$INSERT INTO users (username,email,full_name) VALUES ('ALICE01','other@example.test','Khác')$$,'23505','users_username_uq');
SELECT pg_temp.expect_error('core/email uniqueness ignores case',
    $$INSERT INTO users (username,email,full_name) VALUES ('other05','BOB@example.test','Khác')$$,'23505','users_email_uq');
SELECT pg_temp.expect_error('core/profile name required',
    $$INSERT INTO users (username,email,full_name) VALUES ('other05','other@example.test',NULL)$$,'23502');
SELECT pg_temp.expect_error('core/username shape',
    $$INSERT INTO users (username,email,full_name) VALUES ('bad space','shape@example.test','Khác')$$,
    '23514','users_username_check');
SELECT pg_temp.expect_error('core/role domain',
    $$UPDATE users SET role='OWNER' WHERE id=pg_temp.id(0,2)$$,'23514');
SELECT pg_temp.expect_error('core/soft-delete metadata consistent',
    $$UPDATE users SET status='DELETED' WHERE id=pg_temp.id(0,2)$$,'23514');
