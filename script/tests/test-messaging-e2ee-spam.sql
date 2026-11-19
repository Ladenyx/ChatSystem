-- Thanh vien 1: isolated fixtures and storage checks. The caller owns rollback.
-- Fixture suffixes 101..109 are private to this file; shared users 1..4 are untouched.
-- Byte fixtures test storage shape only, not cryptography.

-- Opaque bytes below are structural fixtures, not valid cryptographic material.
INSERT INTO users (id, username, email, full_name, role, created_at) VALUES
('00000000-0000-0000-0000-000000000101', 'msp_alice', 'msp_alice@example.test', 'Nguyễn Minh An', 'USER', '2026-01-01 UTC'),
('00000000-0000-0000-0000-000000000102', 'msp_bob', 'msp_bob@example.test', 'Trần Bình', 'USER', '2026-01-01 UTC'),
('00000000-0000-0000-0000-000000000103', 'msp_carol', 'msp_carol@example.test', 'Lê Chi', 'USER', '2026-01-01 UTC'),
('00000000-0000-0000-0000-000000000104', 'msp_admin', 'msp_admin@example.test', 'Phạm Duy', 'ADMIN', '2026-01-01 UTC'),
('00000000-0000-0000-0000-000000000105', 'msp_dave', 'msp_dave@example.test', 'Hoàng Đạt', 'USER', '2026-01-01 UTC');

INSERT INTO conversations (id, type, group_name, created_by_user_id, created_at) VALUES
('10000000-0000-0000-0000-000000000101', 'DIRECT', NULL, '00000000-0000-0000-0000-000000000101', '2026-01-01 UTC'),
('10000000-0000-0000-0000-000000000102', 'GROUP', 'Nhóm học Java', '00000000-0000-0000-0000-000000000101', '2026-01-01 UTC'),
('10000000-0000-0000-0000-000000000103', 'GROUP', 'Nhóm giữ lịch sử', '00000000-0000-0000-0000-000000000105', '2026-01-01 UTC');

INSERT INTO conversation_members (conversation_id, user_id, role, joined_at)
SELECT '10000000-0000-0000-0000-000000000102', id,
    CASE WHEN username = 'msp_alice' THEN 'ADMIN' ELSE 'MEMBER' END, '2026-01-01 UTC'::timestamptz
FROM users WHERE username IN ('msp_alice', 'msp_bob', 'msp_carol');
INSERT INTO conversation_members (conversation_id, user_id, role, joined_at) VALUES
('10000000-0000-0000-0000-000000000103', '00000000-0000-0000-0000-000000000103', 'ADMIN', '2026-01-01 UTC'),
('10000000-0000-0000-0000-000000000103', '00000000-0000-0000-0000-000000000105', 'MEMBER', '2026-01-01 UTC');

INSERT INTO messages (id, conversation_id, sender_user_id, created_at, content_plain, content_cipher, crypto_metadata) VALUES
('20000000-0000-0000-0000-000000000101', '10000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000101', '2026-11-16 08:00 UTC', 'Xin chào', NULL, NULL),
('20000000-0000-0000-0000-000000000102', '10000000-0000-0000-0000-000000000102', '00000000-0000-0000-0000-000000000102', '2026-11-16 08:00 UTC', NULL, decode('aabb', 'hex'), '{"fixture":true}'),
('20000000-0000-0000-0000-000000000103', '10000000-0000-0000-0000-000000000103', '00000000-0000-0000-0000-000000000105', '2026-11-16 08:00 UTC', NULL, decode('ccdd', 'hex'), '{"fixture":true}');

INSERT INTO e2ee_devices (id, user_id, encryption_public_key, signing_public_key, created_at, trusted_at, trust_method)
SELECT ('30000000-0000-0000-0000-' || lpad((n+100)::text, 12, '0'))::uuid,
    ('00000000-0000-0000-0000-' || lpad((n+100)::text, 12, '0'))::uuid,
    decode('aa', 'hex'), decode('bb', 'hex'), '2026-01-01 UTC', '2026-01-01 UTC', 'INITIAL'
FROM generate_series(1, 5) n;
INSERT INTO e2ee_devices (id, user_id, encryption_public_key, signing_public_key, created_at, trusted_at, trust_method, approved_by_device_id, trust_proof) VALUES
('30000000-0000-0000-0000-000000000106', '00000000-0000-0000-0000-000000000101', decode('cc','hex'), decode('dd','hex'), '2026-01-01 UTC', '2026-01-02 UTC', 'TRUSTED_DEVICE', '30000000-0000-0000-0000-000000000101', '{"fixture":true}');

INSERT INTO e2ee_key_backups (id, user_id, revision, encrypted_payload, crypto_metadata, created_at, replaced_at) VALUES
('40000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000101', 1, decode('aa','hex'), '{}', '2026-01-01 UTC', '2026-11-01 UTC'),
('40000000-0000-0000-0000-000000000102', '00000000-0000-0000-0000-000000000101', 2, decode('bb','hex'), '{}', '2026-11-01 UTC', NULL);

INSERT INTO e2ee_group_key_epochs (id, conversation_id, epoch_no, created_at, retired_at) VALUES
('50000000-0000-0000-0000-000000000101', '10000000-0000-0000-0000-000000000102', 1, '2026-01-01 UTC', '2026-11-01 UTC'),
('50000000-0000-0000-0000-000000000102', '10000000-0000-0000-0000-000000000102', 2, '2026-11-01 UTC', NULL),
('50000000-0000-0000-0000-000000000103', '10000000-0000-0000-0000-000000000103', 1, '2026-01-01 UTC', NULL);

INSERT INTO e2ee_group_key_envelopes (epoch_id, recipient_device_id, issued_by_device_id, wrapped_key, crypto_metadata) VALUES
('50000000-0000-0000-0000-000000000102', '30000000-0000-0000-0000-000000000102', '30000000-0000-0000-0000-000000000101', decode('aa','hex'), '{}'),
('50000000-0000-0000-0000-000000000103', '30000000-0000-0000-0000-000000000103', '30000000-0000-0000-0000-000000000105', decode('bb','hex'), '{}');

INSERT INTO spam_reports (id, reporter_user_id, reported_user_id, conversation_id, reason, received_at, request_fingerprint) VALUES
('60000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000102', '10000000-0000-0000-0000-000000000102', 'Quảng cáo lặp lại', '2026-11-16 09:00 UTC', decode(repeat('ab',32),'hex'));

INSERT INTO spam_report_evidences VALUES
('60000000-0000-0000-0000-000000000101', '70000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000102', '2026-11-15 UTC', 'Tin được chọn từ snapshot', true, false, 1),
('60000000-0000-0000-0000-000000000101', '20000000-0000-0000-0000-000000000102', '00000000-0000-0000-0000-000000000102', '2026-11-16 08:00 UTC', 'Tin vừa Selected vừa Context', true, true, 2),
('60000000-0000-0000-0000-000000000101', '70000000-0000-0000-0000-000000000103', '00000000-0000-0000-0000-000000000103', '2026-11-16 08:01 UTC', 'Tin ngữ cảnh của thành viên khác', false, true, 3);

-- ERD contract: columns, nullability and keys of the three editable SVGs in docs/databases/erd/.
-- Keep this independently reviewed expectation in sync when the data model changes.
CREATE TEMP TABLE messaging_erd_columns (table_name TEXT, column_name TEXT, data_type TEXT, nullable BOOLEAN, pk BOOLEAN, fk BOOLEAN);
INSERT INTO messaging_erd_columns VALUES
('conversations','id','uuid',false,true,false),
('conversations','type','text',false,false,false),
('conversations','group_name','text',true,false,false),
('conversations','created_by_user_id','uuid',true,false,true),
('conversations','created_at','timestamp with time zone',false,false,false),
('conversations','closed_at','timestamp with time zone',true,false,false),
('conversation_members','conversation_id','uuid',false,true,true),
('conversation_members','user_id','uuid',false,true,true),
('conversation_members','role','text',false,false,false),
('conversation_members','joined_at','timestamp with time zone',false,false,false),
('conversation_members','left_at','timestamp with time zone',true,false,false),
('messages','id','uuid',false,true,false),
('messages','conversation_id','uuid',false,false,true),
('messages','sender_user_id','uuid',true,false,true),
('messages','created_at','timestamp with time zone',false,false,false),
('messages','content_plain','text',true,false,false),
('messages','content_cipher','bytea',true,false,false),
('messages','crypto_metadata','jsonb',true,false,false),
('messages','recalled_at','timestamp with time zone',true,false,false),
('message_deletions','message_id','uuid',false,true,true),
('message_deletions','user_id','uuid',false,true,true),
('message_deletions','deleted_at','timestamp with time zone',false,false,false),
('e2ee_devices','id','uuid',false,true,false),
('e2ee_devices','user_id','uuid',false,false,true),
('e2ee_devices','encryption_public_key','bytea',false,false,false),
('e2ee_devices','signing_public_key','bytea',false,false,false),
('e2ee_devices','created_at','timestamp with time zone',false,false,false),
('e2ee_devices','trusted_at','timestamp with time zone',true,false,false),
('e2ee_devices','trust_method','text',true,false,false),
('e2ee_devices','approved_by_device_id','uuid',true,false,true),
('e2ee_devices','trust_proof','jsonb',true,false,false),
('e2ee_devices','revoked_at','timestamp with time zone',true,false,false),
('e2ee_key_backups','id','uuid',false,true,false),
('e2ee_key_backups','user_id','uuid',false,false,true),
('e2ee_key_backups','revision','bigint',false,false,false),
('e2ee_key_backups','encrypted_payload','bytea',false,false,false),
('e2ee_key_backups','crypto_metadata','jsonb',false,false,false),
('e2ee_key_backups','created_at','timestamp with time zone',false,false,false),
('e2ee_key_backups','replaced_at','timestamp with time zone',true,false,false),
('e2ee_group_key_envelopes','epoch_id','uuid',false,true,true),
('e2ee_group_key_envelopes','recipient_device_id','uuid',false,true,true),
('e2ee_group_key_envelopes','issued_by_device_id','uuid',true,false,true),
('e2ee_group_key_envelopes','wrapped_key','bytea',false,false,false),
('e2ee_group_key_envelopes','crypto_metadata','jsonb',false,false,false),
('e2ee_group_key_envelopes','created_at','timestamp with time zone',false,false,false),
('e2ee_group_key_epochs','id','uuid',false,true,false),
('e2ee_group_key_epochs','conversation_id','uuid',false,false,true),
('e2ee_group_key_epochs','epoch_no','bigint',false,false,false),
('e2ee_group_key_epochs','created_at','timestamp with time zone',false,false,false),
('e2ee_group_key_epochs','retired_at','timestamp with time zone',true,false,false),
('spam_reports','id','uuid',false,true,false),
('spam_reports','reporter_user_id','uuid',false,false,true),
('spam_reports','reported_user_id','uuid',false,false,true),
('spam_reports','conversation_id','uuid',false,false,true),
('spam_reports','reason','text',false,false,false),
('spam_reports','received_at','timestamp with time zone',false,false,false),
('spam_reports','request_fingerprint','bytea',false,false,false),
('spam_reports','review_status','text',false,false,false),
('spam_reports','reviewed_by_user_id','uuid',true,false,true),
('spam_reports','reviewed_at','timestamp with time zone',true,false,false),
('spam_reports','review_note','text',true,false,false),
('spam_report_evidences','report_id','uuid',false,true,true),
('spam_report_evidences','source_message_id','uuid',false,true,false),
('spam_report_evidences','sender_user_id','uuid',false,false,true),
('spam_report_evidences','message_created_at','timestamp with time zone',false,false,false),
('spam_report_evidences','content_plain','text',false,false,false),
('spam_report_evidences','is_selected','boolean',false,false,false),
('spam_report_evidences','is_context','boolean',false,false,false),
('spam_report_evidences','display_order','integer',false,false,false),
('spam_report_archives','id','uuid',false,true,false),
('spam_report_archives','received_on','date',false,false,false),
('spam_report_archives','reviewed_on','date',true,false,false),
('spam_report_archives','archived_on','date',false,false,false),
('spam_report_archives','review_status','text',false,false,false),
('spam_report_archives','selected_count','integer',false,false,false),
('spam_report_archives','context_count','integer',false,false,false),
('spam_report_archives','overlap_count','integer',false,false,false),
('spam_report_archives','evidence_count','integer',false,false,false),
('spam_report_request_tombstones','request_digest','bytea',false,true,false);

SELECT pg_temp.assert_true('messaging/schema/ERD columns and nullability: ' || t.table_name,
    NOT EXISTS (
        SELECT FROM (
            SELECT column_name,data_type,nullable FROM messaging_erd_columns WHERE table_name=t.table_name
        ) e FULL JOIN (
            SELECT column_name,data_type,is_nullable='YES' AS nullable FROM information_schema.columns
            WHERE table_schema='public' AND table_name=t.table_name
        ) a USING (column_name)
        WHERE e.column_name IS NULL OR a.column_name IS NULL
            OR e.data_type IS DISTINCT FROM a.data_type OR e.nullable IS DISTINCT FROM a.nullable
    ))
FROM (SELECT DISTINCT table_name FROM messaging_erd_columns) t;

SELECT pg_temp.assert_true('messaging/schema/ERD PK: ' || e.table_name,
    bool_and(e.pk = EXISTS (
        SELECT FROM pg_constraint c JOIN pg_attribute a ON a.attrelid=c.conrelid AND a.attnum=ANY(c.conkey)
        WHERE c.contype='p' AND c.conrelid=('public.'||e.table_name)::regclass AND a.attname=e.column_name
    ))) FROM messaging_erd_columns e GROUP BY e.table_name;

SELECT pg_temp.assert_true('messaging/schema/ERD FK participation: ' || e.table_name,
    bool_and(e.fk = EXISTS (
        SELECT FROM pg_constraint c JOIN pg_attribute a ON a.attrelid=c.conrelid AND a.attnum=ANY(c.conkey)
        WHERE c.contype='f' AND c.conrelid=('public.'||e.table_name)::regclass AND a.attname=e.column_name
    ))) FROM messaging_erd_columns e GROUP BY e.table_name;

-- FK endpoints and delete actions are part of the physical model, including the
-- same-account composite approver key and the deliberately unlinked snapshots/archive.
CREATE TEMP TABLE messaging_erd_foreign_keys (
    table_name TEXT, columns TEXT[], referenced_table TEXT, referenced_columns TEXT[], delete_action TEXT
);
INSERT INTO messaging_erd_foreign_keys VALUES
('conversations',ARRAY['created_by_user_id'],'users',ARRAY['id'],'n'),
('conversation_members',ARRAY['conversation_id'],'conversations',ARRAY['id'],'c'),
('conversation_members',ARRAY['user_id'],'users',ARRAY['id'],'c'),
('messages',ARRAY['conversation_id'],'conversations',ARRAY['id'],'r'),
('messages',ARRAY['sender_user_id'],'users',ARRAY['id'],'n'),
('message_deletions',ARRAY['message_id'],'messages',ARRAY['id'],'c'),
('message_deletions',ARRAY['user_id'],'users',ARRAY['id'],'c'),
('e2ee_devices',ARRAY['user_id'],'users',ARRAY['id'],'c'),
('e2ee_devices',ARRAY['user_id','approved_by_device_id'],'e2ee_devices',ARRAY['user_id','id'],'a'),
('e2ee_key_backups',ARRAY['user_id'],'users',ARRAY['id'],'c'),
('e2ee_group_key_epochs',ARRAY['conversation_id'],'conversations',ARRAY['id'],'r'),
('e2ee_group_key_envelopes',ARRAY['epoch_id'],'e2ee_group_key_epochs',ARRAY['id'],'r'),
('e2ee_group_key_envelopes',ARRAY['recipient_device_id'],'e2ee_devices',ARRAY['id'],'c'),
('e2ee_group_key_envelopes',ARRAY['issued_by_device_id'],'e2ee_devices',ARRAY['id'],'n'),
('spam_reports',ARRAY['reporter_user_id'],'users',ARRAY['id'],'r'),
('spam_reports',ARRAY['reported_user_id'],'users',ARRAY['id'],'r'),
('spam_reports',ARRAY['reviewed_by_user_id'],'users',ARRAY['id'],'r'),
('spam_reports',ARRAY['conversation_id'],'conversations',ARRAY['id'],'r'),
('spam_report_evidences',ARRAY['report_id'],'spam_reports',ARRAY['id'],'c'),
('spam_report_evidences',ARRAY['sender_user_id'],'users',ARRAY['id'],'r');

WITH actual AS (
    SELECT c.conrelid::regclass::text AS table_name,
        ARRAY(SELECT a.attname::text FROM unnest(c.conkey) WITH ORDINALITY AS k(num,pos)
            JOIN pg_attribute a ON a.attrelid=c.conrelid AND a.attnum=k.num ORDER BY k.pos) AS columns,
        c.confrelid::regclass::text AS referenced_table,
        ARRAY(SELECT a.attname::text FROM unnest(c.confkey) WITH ORDINALITY AS k(num,pos)
            JOIN pg_attribute a ON a.attrelid=c.confrelid AND a.attnum=k.num ORDER BY k.pos) AS referenced_columns,
        c.confdeltype::text AS delete_action
    FROM pg_constraint c
    WHERE c.contype='f' AND c.conrelid IN (
        SELECT ('public.'||table_name)::regclass FROM messaging_erd_columns
    )
)
SELECT pg_temp.assert_true('messaging/schema/ERD FK targets and delete actions',
    NOT EXISTS (
        (SELECT * FROM messaging_erd_foreign_keys EXCEPT SELECT * FROM actual)
        UNION ALL
        (SELECT * FROM actual EXCEPT SELECT * FROM messaging_erd_foreign_keys)
    ));

SELECT pg_temp.assert_true('messaging/schema/runtime model does not put a single delivery status on group messages',
    NOT EXISTS (SELECT FROM information_schema.columns WHERE table_schema='public' AND table_name='messages' AND column_name='status'));

SELECT pg_temp.assert_true('messaging/valid direct/group fixtures', (SELECT count(*) = 3 FROM messages WHERE conversation_id IN (pg_temp.id(1,101),pg_temp.id(1,102),pg_temp.id(1,103))));
SELECT pg_temp.expect_error('messaging/membership unique per user/conversation',
    $$INSERT INTO conversation_members (conversation_id,user_id) VALUES (pg_temp.id(1,102),pg_temp.id(0,101))$$, '23505');
SELECT pg_temp.expect_error('messaging/membership user FK',
    $$INSERT INTO conversation_members (conversation_id,user_id) VALUES (pg_temp.id(1,102),pg_temp.id(0,999))$$, '23503');
SELECT pg_temp.expect_error('messaging/membership roles',
    $$UPDATE conversation_members SET role='OWNER' WHERE user_id=pg_temp.id(0,102)$$, '23514', 'conversation_members_role_check');
SELECT pg_temp.expect_error('messaging/membership time ordering',
    $$UPDATE conversation_members SET left_at=joined_at-interval '1 second' WHERE user_id=pg_temp.id(0,102)$$, '23514');
SELECT pg_temp.expect_error('messaging/group name cannot contain only whitespace',
    $$INSERT INTO conversations (type,group_name) VALUES ('GROUP',E' \n\t\r ')$$,
    '23514', 'conversations_name_check');
SELECT pg_temp.expect_error('messaging/group name is required',
    $$INSERT INTO conversations (type,group_name) VALUES ('GROUP','   ')$$, '23514');
SELECT pg_temp.expect_error('messaging/conversation type domain',
    $$INSERT INTO conversations (type) VALUES ('CHANNEL')$$, '23514');
SELECT pg_temp.expect_error('messaging/close cannot predate creation',
    $$UPDATE conversations SET closed_at=created_at-interval '1 second' WHERE id=pg_temp.id(1,102)$$,
    '23514', 'conversations_closed_check');
SELECT pg_temp.expect_error('messaging/direct conversation has no group name',
    $$INSERT INTO conversations (type,group_name) VALUES ('DIRECT','Tên nhóm')$$, '23514');
SELECT pg_temp.expect_error('messaging/conversation type stays stable for payloads',
    $$UPDATE conversations SET type='GROUP' WHERE id=pg_temp.id(1,101)$$, '23514', 'conversations_immutable_type_check');
SELECT pg_temp.expect_error('messaging/group payload cannot be plaintext',
    $$UPDATE messages SET content_plain='Lộ bản rõ',content_cipher=NULL,crypto_metadata=NULL WHERE id=pg_temp.id(2,102)$$,
    '23514', 'messages_conversation_payload_check');
SELECT pg_temp.expect_error('messaging/direct payload uses plaintext representation',
    $$UPDATE messages SET content_plain=NULL,content_cipher=decode('aa','hex'),crypto_metadata='{}' WHERE id=pg_temp.id(2,101)$$,
    '23514', 'messages_conversation_payload_check');
SELECT pg_temp.expect_error('messaging/live message must carry content',
    $$UPDATE messages SET content_plain=NULL WHERE id=pg_temp.id(2,101)$$, '23514', 'messages_payload_check');
SELECT pg_temp.expect_error('messaging/cipher metadata must be an object',
    $$UPDATE messages SET crypto_metadata='[]' WHERE id=pg_temp.id(2,102)$$, '23514');
SELECT pg_temp.expect_error('messaging/plaintext and ciphertext are mutually exclusive',
    $$UPDATE messages SET content_cipher=decode('aa','hex'),crypto_metadata='{}' WHERE id=pg_temp.id(2,101)$$,
    '23514');
SELECT pg_temp.expect_error('messaging/ciphertext cannot be empty',
    $$UPDATE messages SET content_cipher=decode('','hex') WHERE id=pg_temp.id(2,102)$$,
    '23514', 'messages_payload_check');
SELECT pg_temp.expect_error('messaging/ciphertext requires metadata',
    $$UPDATE messages SET crypto_metadata=NULL WHERE id=pg_temp.id(2,102)$$,
    '23514', 'messages_payload_check');
SELECT pg_temp.expect_error('messaging/message conversation cannot be null',
    $$UPDATE messages SET conversation_id=NULL WHERE id=pg_temp.id(2,101)$$, '23502');
SELECT pg_temp.expect_error('messaging/message conversation FK',
    $$UPDATE messages SET conversation_id=pg_temp.id(1,999) WHERE id=pg_temp.id(2,101)$$, '23503');
SELECT pg_temp.expect_error('messaging/stable message ID prevents duplicate storage',
    $$INSERT INTO messages SELECT * FROM messages WHERE id=pg_temp.id(2,101)$$, '23505');

INSERT INTO message_deletions (message_id,user_id) VALUES (pg_temp.id(2,101),pg_temp.id(0,101));
SELECT pg_temp.assert_true('messaging/self deletion preserves source content',
    (SELECT content_plain='Xin chào' FROM messages WHERE id=pg_temp.id(2,101)));
SELECT pg_temp.assert_true('messaging/self deletion does not hide for everyone',
    NOT EXISTS (SELECT FROM message_deletions WHERE message_id=pg_temp.id(2,101) AND user_id=pg_temp.id(0,102)));
SELECT pg_temp.expect_error('messaging/self deletion is unique per user/message',
    $$INSERT INTO message_deletions (message_id,user_id) VALUES (pg_temp.id(2,101),pg_temp.id(0,101))$$, '23505');
SELECT pg_temp.expect_error('messaging/self deletion requires a source message',
    $$INSERT INTO message_deletions (message_id,user_id) VALUES (pg_temp.id(2,999),pg_temp.id(0,101))$$, '23503');
SELECT pg_temp.expect_error('messaging/recall must clear payload',
    $$UPDATE messages SET recalled_at=created_at+interval '1 hour' WHERE id=pg_temp.id(2,101)$$, '23514');
SELECT pg_temp.expect_error('messaging/recall cannot predate message',
    $$UPDATE messages SET content_plain=NULL,recalled_at=created_at-interval '1 second' WHERE id=pg_temp.id(2,101)$$, '23514');
SELECT pg_temp.expect_error('messaging/recall one microsecond beyond 48 hours is invalid',
    $$UPDATE messages SET content_plain=NULL,recalled_at=created_at+interval '48 hours 0.000001 seconds' WHERE id=pg_temp.id(2,101)$$,
    '23514', 'messages_recall_time_check');
SELECT pg_temp.expect_error('messaging/recall after 48 hours is invalid',
    $$UPDATE messages SET content_plain=NULL,recalled_at=created_at+interval '49 hours' WHERE id=pg_temp.id(2,101)$$, '23514');
UPDATE messages SET content_plain=NULL,recalled_at=created_at+interval '48 hours' WHERE id=pg_temp.id(2,101);
SELECT pg_temp.assert_true('messaging/recall retains a tombstone and message ID',
    (SELECT recalled_at IS NOT NULL AND content_plain IS NULL FROM messages WHERE id=pg_temp.id(2,101)));
SELECT pg_temp.assert_true('messaging/timestamps represent the same UTC instant',
    (SELECT created_at='2026-11-16 15:00+07'::timestamptz FROM messages WHERE id=pg_temp.id(2,102)));
SELECT pg_temp.expect_error('messaging/deleting nonempty conversation cannot cascade chat history',
    $$DELETE FROM conversations WHERE id=pg_temp.id(1,101)$$, '23001');

INSERT INTO e2ee_devices (id,user_id,encryption_public_key,signing_public_key)
VALUES (pg_temp.id(3,107),pg_temp.id(0,101),decode('aa','hex'),decode('bb','hex'));
SELECT pg_temp.assert_true('e2ee/pending device has no trust metadata',
    (SELECT trusted_at IS NULL AND trust_method IS NULL FROM e2ee_devices WHERE id=pg_temp.id(3,107)));
UPDATE e2ee_devices SET revoked_at=created_at WHERE id=pg_temp.id(3,107);
SELECT pg_temp.assert_true('e2ee/pending device can be revoked without granting trust',
    (SELECT revoked_at IS NOT NULL AND trusted_at IS NULL AND trust_method IS NULL
        FROM e2ee_devices WHERE id=pg_temp.id(3,107)));
SELECT pg_temp.expect_error('e2ee/trusted device needs a method, including SQL NULL case',
    $$UPDATE e2ee_devices SET trust_method=NULL WHERE id=pg_temp.id(3,101)$$, '23514', 'e2ee_devices_trust_check');
SELECT pg_temp.expect_error('e2ee/trusted-device approval needs public proof',
    $$UPDATE e2ee_devices SET trust_proof=NULL WHERE id=pg_temp.id(3,106)$$, '23514');
SELECT pg_temp.expect_error('e2ee/approval must belong to the same account',
    $$UPDATE e2ee_devices SET approved_by_device_id=pg_temp.id(3,102) WHERE id=pg_temp.id(3,106)$$,
    '23503', 'e2ee_devices_approver_fk');
SELECT pg_temp.expect_error('e2ee/device cannot approve itself',
    $$UPDATE e2ee_devices SET approved_by_device_id=id WHERE id=pg_temp.id(3,106)$$, '23514');
SELECT pg_temp.expect_error('e2ee/recovery cannot invent an approving device',
    $$UPDATE e2ee_devices SET trust_method='RECOVERY' WHERE id=pg_temp.id(3,106)$$, '23514');
UPDATE e2ee_devices SET trust_method='RECOVERY',approved_by_device_id=NULL WHERE id=pg_temp.id(3,106);
SELECT pg_temp.assert_true('e2ee/recovery proof is independent of approver FK',
    (SELECT trust_method='RECOVERY' AND trust_proof IS NOT NULL FROM e2ee_devices WHERE id=pg_temp.id(3,106)));
SELECT pg_temp.expect_error('e2ee/revocation cannot precede recorded trust',
    $$UPDATE e2ee_devices SET revoked_at=created_at+interval '12 hours' WHERE id=pg_temp.id(3,106)$$,
    '23514', 'e2ee_devices_times_check');
SELECT pg_temp.expect_error('e2ee/public keys cannot be empty bytes',
    $$UPDATE e2ee_devices SET signing_public_key=decode('','hex') WHERE id=pg_temp.id(3,101)$$, '23514');
SELECT pg_temp.expect_error('e2ee/device trust cannot predate creation',
    $$UPDATE e2ee_devices SET trusted_at=created_at-interval '1 second' WHERE id=pg_temp.id(3,101)$$, '23514');
SELECT pg_temp.expect_error('e2ee/one current backup per account',
    $$INSERT INTO e2ee_key_backups (user_id,revision,encrypted_payload,crypto_metadata)
      VALUES (pg_temp.id(0,101),3,decode('cc','hex'),'{}')$$,
    '23505', 'e2ee_key_backups_current_uq');
SELECT pg_temp.expect_error('e2ee/backup revision unique within an account',
    $$INSERT INTO e2ee_key_backups (user_id,revision,encrypted_payload,crypto_metadata,replaced_at)
      VALUES (pg_temp.id(0,101),1,decode('cc','hex'),'{}',CURRENT_TIMESTAMP)$$, '23505');
SELECT pg_temp.expect_error('e2ee/backup metadata must be an object',
    $$UPDATE e2ee_key_backups SET crypto_metadata='[]' WHERE id=pg_temp.id(4,101)$$, '23514');
SELECT pg_temp.expect_error('e2ee/backup revision must be positive',
    $$UPDATE e2ee_key_backups SET revision=0 WHERE id=pg_temp.id(4,101)$$,
    '23514', 'e2ee_key_backups_revision_check');
SELECT pg_temp.expect_error('e2ee/backup ciphertext cannot be empty',
    $$UPDATE e2ee_key_backups SET encrypted_payload=decode('','hex') WHERE id=pg_temp.id(4,101)$$,
    '23514', 'e2ee_key_backups_payload_check');
SELECT pg_temp.expect_error('e2ee/replacement cannot predate backup',
    $$UPDATE e2ee_key_backups SET replaced_at=created_at-interval '1 second' WHERE id=pg_temp.id(4,101)$$, '23514');
UPDATE e2ee_key_backups SET replaced_at='2026-11-16 UTC' WHERE id=pg_temp.id(4,102);
INSERT INTO e2ee_key_backups (user_id,revision,encrypted_payload,crypto_metadata,created_at)
VALUES (pg_temp.id(0,101),3,decode('cc','hex'),'{}','2026-11-16 UTC');
SELECT pg_temp.assert_true('e2ee/replacement preserves past backup revisions',
    (SELECT count(*)=3 AND count(*) FILTER (WHERE replaced_at IS NULL)=1 FROM e2ee_key_backups WHERE user_id=pg_temp.id(0,101)));
SELECT pg_temp.assert_true('e2ee/backup replacement does not delete historical epochs/envelopes',
    (SELECT count(*)=3 FROM e2ee_group_key_epochs WHERE id IN (pg_temp.id(5,101),pg_temp.id(5,102),pg_temp.id(5,103))) AND (SELECT count(*)=2 FROM e2ee_group_key_envelopes WHERE epoch_id IN (pg_temp.id(5,101),pg_temp.id(5,102),pg_temp.id(5,103))));
SELECT pg_temp.expect_error('e2ee/epochs are only for groups',
    $$INSERT INTO e2ee_group_key_epochs (conversation_id,epoch_no) VALUES (pg_temp.id(1,101),1)$$,
    '23514', 'e2ee_group_key_epochs_group_check');
SELECT pg_temp.expect_error('e2ee/epoch conversation FK',
    $$INSERT INTO e2ee_group_key_epochs (conversation_id,epoch_no) VALUES (pg_temp.id(1,999),1)$$,
    '23503');
SELECT pg_temp.expect_error('e2ee/retirement cannot predate epoch creation',
    $$UPDATE e2ee_group_key_epochs SET retired_at=created_at-interval '1 second' WHERE id=pg_temp.id(5,101)$$,
    '23514', 'e2ee_group_key_epochs_retired_check');
SELECT pg_temp.expect_error('e2ee/epoch numbering is unique per group',
    $$INSERT INTO e2ee_group_key_epochs (conversation_id,epoch_no) VALUES (pg_temp.id(1,102),1)$$, '23505');
SELECT pg_temp.assert_true('e2ee/different groups may use the same epoch number',
    (SELECT count(*)=2 FROM e2ee_group_key_epochs WHERE epoch_no=1 AND conversation_id IN (pg_temp.id(1,102),pg_temp.id(1,103))));
SELECT pg_temp.expect_error('e2ee/one envelope per epoch and recipient device',
    $$INSERT INTO e2ee_group_key_envelopes SELECT * FROM e2ee_group_key_envelopes WHERE epoch_id=pg_temp.id(5,102)$$, '23505');
SELECT pg_temp.expect_error('e2ee/envelope recipient device FK',
    $$UPDATE e2ee_group_key_envelopes SET recipient_device_id=pg_temp.id(3,999) WHERE epoch_id=pg_temp.id(5,102)$$, '23503');
SELECT pg_temp.expect_error('e2ee/envelope epoch FK',
    $$UPDATE e2ee_group_key_envelopes SET epoch_id=pg_temp.id(5,999) WHERE epoch_id=pg_temp.id(5,102)$$,
    '23503');
SELECT pg_temp.expect_error('e2ee/wrapped key cannot be empty',
    $$UPDATE e2ee_group_key_envelopes SET wrapped_key=decode('','hex') WHERE epoch_id=pg_temp.id(5,102)$$, '23514');
SELECT pg_temp.expect_error('e2ee/epoch deletion cannot cascade existing envelopes',
    $$DELETE FROM e2ee_group_key_epochs WHERE id=pg_temp.id(5,102)$$, '23001');
UPDATE e2ee_group_key_epochs SET retired_at='2026-11-16 UTC' WHERE id=pg_temp.id(5,102);
UPDATE e2ee_devices SET revoked_at='2026-11-16 UTC' WHERE id=pg_temp.id(3,101);
SELECT pg_temp.assert_true('e2ee/retirement/revocation metadata preserve existing envelopes',
    (SELECT count(*)=2 FROM e2ee_group_key_envelopes WHERE epoch_id IN (pg_temp.id(5,101),pg_temp.id(5,102),pg_temp.id(5,103))));

INSERT INTO message_deletions (message_id,user_id) VALUES (pg_temp.id(2,103),pg_temp.id(0,105));
DELETE FROM users WHERE id=pg_temp.id(0,105);
SELECT pg_temp.assert_true('e2ee/purge preserves other members and conversation',
    EXISTS (SELECT FROM conversation_members WHERE conversation_id=pg_temp.id(1,103) AND user_id=pg_temp.id(0,103))
    AND (SELECT created_by_user_id IS NULL FROM conversations WHERE id=pg_temp.id(1,103)));
SELECT pg_temp.assert_true('e2ee/purge unlinks sender without deleting shared chat',
    (SELECT sender_user_id IS NULL AND content_cipher IS NOT NULL FROM messages WHERE id=pg_temp.id(2,103)));
SELECT pg_temp.assert_true('messaging/purge removes only the users self-deletion markers',
    NOT EXISTS (SELECT FROM message_deletions WHERE user_id=pg_temp.id(0,105))
    AND EXISTS (SELECT FROM message_deletions WHERE message_id=pg_temp.id(2,101) AND user_id=pg_temp.id(0,101)));
SELECT pg_temp.assert_true('e2ee/purge preserves another recipients wrapped key',
    (SELECT issued_by_device_id IS NULL AND wrapped_key=decode('bb','hex') FROM e2ee_group_key_envelopes WHERE epoch_id=pg_temp.id(5,103)));

INSERT INTO users (id,username,email,full_name) VALUES (pg_temp.id(0,106),'msp_extra','msp_extra@example.test','Người dùng thử');
INSERT INTO e2ee_devices (id,user_id,encryption_public_key,signing_public_key,created_at,trusted_at,trust_method)
VALUES (pg_temp.id(3,108),pg_temp.id(0,106),decode('aa','hex'),decode('bb','hex'),'2026-01-01 UTC','2026-01-01 UTC','INITIAL');
INSERT INTO e2ee_devices (id,user_id,encryption_public_key,signing_public_key,created_at,trusted_at,trust_method,approved_by_device_id,trust_proof)
VALUES (pg_temp.id(3,109),pg_temp.id(0,106),decode('aa','hex'),decode('bb','hex'),'2026-01-01 UTC','2026-01-02 UTC','TRUSTED_DEVICE',pg_temp.id(3,108),'{}');
INSERT INTO e2ee_group_key_envelopes (epoch_id,recipient_device_id,issued_by_device_id,wrapped_key,crypto_metadata)
VALUES (pg_temp.id(5,103),pg_temp.id(3,108),pg_temp.id(3,103),decode('cc','hex'),'{}');
DELETE FROM users WHERE id=pg_temp.id(0,106);
SELECT pg_temp.assert_true('e2ee/owner purge removes a complete same-account approval graph',
    NOT EXISTS (SELECT FROM e2ee_devices WHERE user_id=pg_temp.id(0,106)));
SELECT pg_temp.assert_true('e2ee/recipient purge keeps other devices envelopes and chat history',
    NOT EXISTS (SELECT FROM e2ee_group_key_envelopes WHERE recipient_device_id=pg_temp.id(3,108))
    AND EXISTS (SELECT FROM e2ee_group_key_envelopes
        WHERE epoch_id=pg_temp.id(5,103) AND recipient_device_id=pg_temp.id(3,103))
    AND EXISTS (SELECT FROM messages WHERE id=pg_temp.id(2,103)));

SELECT pg_temp.assert_true('spam/evidence union keeps overlap as one row with two labels',
    (SELECT count(*)=3 AND count(*) FILTER(WHERE is_selected)=2 AND count(*) FILTER(WHERE is_context)=2
        AND count(*) FILTER(WHERE is_selected AND is_context)=1 FROM spam_report_evidences WHERE report_id=pg_temp.id(6,101)));
SELECT pg_temp.expect_error('spam/evidence source unique within report',
    $$INSERT INTO spam_report_evidences SELECT * FROM spam_report_evidences WHERE report_id=pg_temp.id(6,101) LIMIT 1$$, '23505');
SELECT pg_temp.expect_error('spam/evidence display order unique within report',
    $$INSERT INTO spam_report_evidences VALUES (pg_temp.id(6,101),pg_temp.id(7,999),pg_temp.id(0,102),CURRENT_TIMESTAMP,'Tin',true,false,2)$$,
    '23505', 'spam_report_evidences_order_uq');
SELECT pg_temp.expect_error('spam/evidence must have at least one label',
    $$UPDATE spam_report_evidences SET is_selected=false,is_context=false WHERE report_id=pg_temp.id(6,101) AND source_message_id=pg_temp.id(7,101)$$, '23514');
SELECT pg_temp.expect_error('spam/evidence plaintext cannot be null',
    $$UPDATE spam_report_evidences SET content_plain=NULL WHERE report_id=pg_temp.id(6,101) AND source_message_id=pg_temp.id(7,101)$$, '23502');
SELECT pg_temp.expect_error('spam/evidence ordering is positive',
    $$UPDATE spam_report_evidences SET display_order=0 WHERE report_id=pg_temp.id(6,101) AND source_message_id=pg_temp.id(7,101)$$, '23514');
SELECT pg_temp.expect_error('spam/evidence sender identity FK',
    $$UPDATE spam_report_evidences SET sender_user_id=pg_temp.id(0,999) WHERE report_id=pg_temp.id(6,101) AND source_message_id=pg_temp.id(7,101)$$, '23503');
SELECT pg_temp.expect_error('spam/report cannot target its own reporter',
    $$UPDATE spam_reports SET reported_user_id=reporter_user_id WHERE id=pg_temp.id(6,101)$$, '23514');
SELECT pg_temp.expect_error('spam/report reason cannot contain only whitespace',
    $$UPDATE spam_reports SET reason=E' \n\t\r ' WHERE id=pg_temp.id(6,101)$$,
    '23514', 'spam_reports_reason_check');
SELECT pg_temp.expect_error('spam/report reason must contain text',
    $$UPDATE spam_reports SET reason='   ' WHERE id=pg_temp.id(6,101)$$, '23514');
SELECT pg_temp.expect_error('spam/report fingerprint is SHA256-sized',
    $$UPDATE spam_reports SET request_fingerprint=decode('aa','hex') WHERE id=pg_temp.id(6,101)$$, '23514');
SELECT pg_temp.expect_error('spam/pending report has no partial review metadata',
    $$UPDATE spam_reports SET reviewed_by_user_id=pg_temp.id(0,104) WHERE id=pg_temp.id(6,101)$$, '23514');
SELECT pg_temp.expect_error('spam/review requires all fields together',
    $$UPDATE spam_reports SET review_status='REVIEWED' WHERE id=pg_temp.id(6,101)$$, '23514');
SELECT pg_temp.expect_error('spam/report status domain',
    $$UPDATE spam_reports SET review_status='CANCELLED' WHERE id=pg_temp.id(6,101)$$, '23514');
UPDATE spam_reports SET review_status='REVIEWED',reviewed_by_user_id=pg_temp.id(0,104),
    reviewed_at=received_at+interval '1 hour',review_note='Đã xem xét bằng chứng' WHERE id=pg_temp.id(6,101);
SELECT pg_temp.assert_true('spam/complete reviewed report is valid',
    (SELECT review_status='REVIEWED' AND reviewed_by_user_id=pg_temp.id(0,104) FROM spam_reports WHERE id=pg_temp.id(6,101)));
SELECT pg_temp.expect_error('spam/review note cannot contain only whitespace',
    $$UPDATE spam_reports SET review_note=E' \n\t\r ' WHERE id=pg_temp.id(6,101)$$,
    '23514', 'spam_reports_review_check');
SELECT pg_temp.expect_error('spam/review note must contain text',
    $$UPDATE spam_reports SET review_note=' ' WHERE id=pg_temp.id(6,101)$$, '23514');
SELECT pg_temp.expect_error('spam/recorded review cannot occur at or after retention boundary',
    $$UPDATE spam_reports SET reviewed_at=received_at+interval '4320 hours' WHERE id=pg_temp.id(6,101)$$, '23514');
SELECT pg_temp.expect_error('spam/target purge requires full report archival first',
    $$DELETE FROM users WHERE id=pg_temp.id(0,102)$$, '23001');
SELECT pg_temp.expect_error('spam/context-only sender purge requires full report archival first',
    $$DELETE FROM users WHERE id=pg_temp.id(0,103)$$, '23001');
SELECT pg_temp.expect_error('spam/reviewer purge requires full report archival first',
    $$DELETE FROM users WHERE id=pg_temp.id(0,104)$$, '23001');
SELECT pg_temp.expect_error('spam/reporter purge requires full report archival first',
    $$DELETE FROM users WHERE id=pg_temp.id(0,101)$$, '23001');
SELECT pg_temp.assert_true('spam/failed purge does not partly unlink source data',
    (SELECT sender_user_id=pg_temp.id(0,102) AND content_cipher IS NOT NULL FROM messages WHERE id=pg_temp.id(2,102)));

UPDATE messages SET content_cipher=NULL,crypto_metadata=NULL,recalled_at=created_at+interval '1 hour' WHERE id=pg_temp.id(2,102);
SELECT pg_temp.assert_true('spam/recall leaves the submitted evidence snapshot unchanged',
    (SELECT content_plain='Tin vừa Selected vừa Context' AND is_selected AND is_context
        FROM spam_report_evidences WHERE report_id=pg_temp.id(6,101) AND source_message_id=pg_temp.id(2,102)));
DELETE FROM messages WHERE id=pg_temp.id(2,102);
SELECT pg_temp.assert_true('spam/evidence survives physical removal of live source',
    (SELECT count(*)=3 FROM spam_report_evidences WHERE report_id=pg_temp.id(6,101)));

INSERT INTO spam_report_archives (id,received_on,archived_on,review_status,selected_count,context_count,overlap_count,evidence_count)
VALUES (pg_temp.id(8,101),'2026-11-16','2026-11-16','PENDING',20,30,0,50);
SELECT pg_temp.assert_true('spam/archive can represent maximum disjoint evidence counts',
    EXISTS (SELECT FROM spam_report_archives WHERE id=pg_temp.id(8,101) AND evidence_count=50));
SELECT pg_temp.expect_error('spam/archive selected bound',
    $$UPDATE spam_report_archives SET selected_count=21,evidence_count=51 WHERE id=pg_temp.id(8,101)$$, '23514');
SELECT pg_temp.expect_error('spam/archive context bound',
    $$UPDATE spam_report_archives SET context_count=31,evidence_count=51 WHERE id=pg_temp.id(8,101)$$, '23514');
SELECT pg_temp.expect_error('spam/archive overlap bound',
    $$UPDATE spam_report_archives SET overlap_count=21,evidence_count=29 WHERE id=pg_temp.id(8,101)$$, '23514');
SELECT pg_temp.expect_error('spam/archive union arithmetic',
    $$UPDATE spam_report_archives SET evidence_count=49 WHERE id=pg_temp.id(8,101)$$, '23514');
SELECT pg_temp.expect_error('spam/archive requires at least one selected message',
    $$UPDATE spam_report_archives SET selected_count=0,evidence_count=30 WHERE id=pg_temp.id(8,101)$$,
    '23514', 'spam_report_archives_counts_check');
SELECT pg_temp.expect_error('spam/archive dates cannot run backwards',
    $$UPDATE spam_report_archives SET archived_on=received_on-1 WHERE id=pg_temp.id(8,101)$$,
    '23514', 'spam_report_archives_dates_check');
SELECT pg_temp.expect_error('spam/pending archive has no review day',
    $$UPDATE spam_report_archives SET reviewed_on=received_on WHERE id=pg_temp.id(8,101)$$, '23514');
INSERT INTO spam_report_request_tombstones VALUES (decode(repeat('ac',32),'hex'));
SELECT pg_temp.expect_error('spam/tombstone digest length',
    $$INSERT INTO spam_report_request_tombstones VALUES (decode('aa','hex'))$$, '23514');
SELECT pg_temp.expect_error('spam/duplicate request digest rejected',
    $$INSERT INTO spam_report_request_tombstones SELECT * FROM spam_report_request_tombstones WHERE request_digest=decode(repeat('ac',32),'hex')$$, '23505');

-- Exercise the storage sequence; the future server must compute canonical data,
-- digest, authorization, retention time and archive transition atomically.
INSERT INTO spam_report_request_tombstones (request_digest)
SELECT sha256(convert_to('ChatSystem:spam-report-request:v1','UTF8') || decode('00','hex') || uuid_send(id))
FROM spam_reports WHERE id=pg_temp.id(6,101);
INSERT INTO spam_report_archives (id,received_on,reviewed_on,archived_on,review_status,
    selected_count,context_count,overlap_count,evidence_count)
SELECT pg_temp.id(8,102),(received_at AT TIME ZONE 'Asia/Ho_Chi_Minh')::date,
    (reviewed_at AT TIME ZONE 'Asia/Ho_Chi_Minh')::date,'2026-11-17','REVIEWED',2,2,1,3
FROM spam_reports WHERE id=pg_temp.id(6,101);
DELETE FROM spam_reports WHERE id=pg_temp.id(6,101);
SELECT pg_temp.assert_true('spam/explicit report removal also removes all attached evidence',
    NOT EXISTS (SELECT FROM spam_report_evidences WHERE report_id=pg_temp.id(6,101)));
SELECT pg_temp.assert_true('spam/retired request digest survives original report deletion',
    EXISTS (SELECT FROM spam_report_request_tombstones
        WHERE request_digest=sha256(convert_to('ChatSystem:spam-report-request:v1','UTF8')
            || decode('00','hex') || uuid_send(pg_temp.id(6,101)))));
DELETE FROM users WHERE id=pg_temp.id(0,102);
SELECT pg_temp.assert_true('spam/purge is possible after identity-bearing report removal',
    NOT EXISTS (SELECT FROM users WHERE id=pg_temp.id(0,102)) AND EXISTS (SELECT FROM conversations WHERE id=pg_temp.id(1,102)));
SELECT pg_temp.assert_true('spam/archive and tombstone tables have no foreign keys',
    NOT EXISTS (SELECT FROM pg_constraint WHERE contype='f'
        AND conrelid IN ('spam_report_archives'::regclass,'spam_report_request_tombstones'::regclass)));

-- Leave the shared fixtures intact for the next workstream in tests/run.sql.
-- Cleanup is scoped to this file; the outer transaction still rolls everything back.
DELETE FROM spam_report_archives WHERE id IN (pg_temp.id(8,101),pg_temp.id(8,102));
DELETE FROM spam_report_request_tombstones
WHERE request_digest IN (decode(repeat('ac',32),'hex'),
    sha256(convert_to('ChatSystem:spam-report-request:v1','UTF8')
        || decode('00','hex') || uuid_send(pg_temp.id(6,101))));
DELETE FROM e2ee_group_key_envelopes
WHERE epoch_id IN (pg_temp.id(5,101),pg_temp.id(5,102),pg_temp.id(5,103));
DELETE FROM messages WHERE conversation_id IN (pg_temp.id(1,101),pg_temp.id(1,102),pg_temp.id(1,103));
DELETE FROM e2ee_group_key_epochs
WHERE conversation_id IN (pg_temp.id(1,101),pg_temp.id(1,102),pg_temp.id(1,103));
DELETE FROM conversations WHERE id IN (pg_temp.id(1,101),pg_temp.id(1,102),pg_temp.id(1,103));
DELETE FROM users WHERE id IN (pg_temp.id(0,101),pg_temp.id(0,102),pg_temp.id(0,103),
    pg_temp.id(0,104),pg_temp.id(0,105),pg_temp.id(0,106));
SELECT pg_temp.assert_true('messaging/integration preserves all shared fixture users',
    (SELECT count(*)=4 AND count(*) FILTER (WHERE role='ADMIN')=1 FROM users
        WHERE id IN (pg_temp.id(0,1),pg_temp.id(0,2),pg_temp.id(0,3),pg_temp.id(0,4))));
SELECT format('%s member-1 storage checks passed',count(*)) FROM db_checks
WHERE label LIKE 'messaging/%' OR label LIKE 'e2ee/%' OR label LIKE 'spam/%';
