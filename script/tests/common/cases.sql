SELECT pg_temp.assert_true('PostgreSQL 18 with required extensions',
    current_setting('server_version_num')::int BETWEEN 180000 AND 189999
    AND (SELECT count(*)=2 FROM pg_extension WHERE extname IN ('pg_trgm','unaccent')));
SELECT pg_temp.assert_true('shared fixture anchors loaded',
    (SELECT count(*)=2 FROM users) AND (SELECT count(*)=2 FROM conversations));
SELECT pg_temp.assert_true('users anchor has one UUID primary key',
    EXISTS (SELECT FROM pg_constraint c JOIN pg_attribute a ON a.attrelid=c.conrelid AND a.attnum=ANY(c.conkey)
        WHERE c.conrelid='users'::regclass AND c.contype='p' AND cardinality(c.conkey)=1
            AND a.attname='id' AND a.atttypid='uuid'::regtype));
SELECT pg_temp.expect_error('username uniqueness ignores case',
    $$INSERT INTO users (username,email,full_name) VALUES ('ALICE01','other@example.test','Khác')$$,'23505','users_username_uq');
SELECT pg_temp.expect_error('email uniqueness ignores case',
    $$INSERT INTO users (username,email,full_name) VALUES ('other03','BOB@example.test','Khác')$$,'23505','users_email_uq');
SELECT pg_temp.expect_error('profile name required',
    $$INSERT INTO users (username,email,full_name) VALUES ('other03','other@example.test',NULL)$$,'23502');
SELECT pg_temp.expect_error('conversation references canonical user',
    $$INSERT INTO conversations (type,created_by_user_id) VALUES ('DIRECT',pg_temp.id(0,999))$$,'23503');
SELECT pg_temp.expect_error('group name required',
    $$INSERT INTO conversations (type,group_name) VALUES ('GROUP','   ')$$,'23514');
SELECT pg_temp.expect_error('direct conversation has no group name',
    $$INSERT INTO conversations (type,group_name) VALUES ('DIRECT','Tên nhóm')$$,'23514');
SELECT pg_temp.expect_error('conversation type immutable',
    $$UPDATE conversations SET type='GROUP' WHERE id=pg_temp.id(1,1)$$,'23514','conversations_immutable_type_check');
SELECT pg_temp.expect_error('soft-delete metadata consistent',
    $$UPDATE users SET status='DELETED' WHERE id=pg_temp.id(0,2)$$,'23514');
DELETE FROM users WHERE id=pg_temp.id(0,1);
SELECT pg_temp.assert_true('removing creator preserves shared conversations',
    (SELECT count(*)=2 AND bool_and(created_by_user_id IS NULL) FROM conversations));
-- Restore the common fixture before domain-owned fixtures and checks.
INSERT INTO users (id,username,email,full_name,created_at)
VALUES (pg_temp.id(0,1),'alice01','alice@example.test','Nguyễn Minh An','2026-01-01 UTC');
UPDATE conversations SET created_by_user_id=pg_temp.id(0,1);
