-- Auth schema checks. Namespace 8 is reserved here for isolated auth fixture IDs.
-- Shared fixtures/helpers are loaded first; the runner rolls back every write.
-- Server workflows (OTP verification, account creation, password change, session
-- replacement) are not implemented by DDL and are not claimed by these checks.

SELECT pg_temp.assert_true('auth/schema has five tables and 49 columns',
    (SELECT count(*) = 49 AND count(DISTINCT table_name) = 5
     FROM information_schema.columns WHERE table_schema = 'public'
        AND table_name IN ('users', 'pending_registrations', 'email_verification_codes',
            'password_reset_tokens', 'user_sessions')));
SELECT pg_temp.assert_true('auth/four foreign keys; pending requests do not reference users',
    (SELECT count(*) = 4 FROM pg_constraint WHERE contype = 'f'
        AND conrelid IN ('pending_registrations'::regclass, 'email_verification_codes'::regclass,
            'password_reset_tokens'::regclass, 'user_sessions'::regclass))
    AND NOT EXISTS (SELECT FROM pg_constraint WHERE contype = 'f'
        AND conrelid = 'pending_registrations'::regclass));

INSERT INTO users (id, username, email, full_name) VALUES
    (pg_temp.id(8,1), 'auth001', 'auth001@example.test', 'Tài khoản chưa thiết lập');
INSERT INTO users (id, username, email, full_name, address, date_of_birth, gender,
    password_hash, email_verified_at) VALUES
    (pg_temp.id(8,2), 'auth002', 'auth002@example.test', 'Tài khoản đầy đủ', 'Hồ Chí Minh',
     DATE '2000-02-29', 'Khác', 'test-only-password-hash', CURRENT_TIMESTAMP - INTERVAL '1 minute');
SELECT pg_temp.assert_true('auth/initial setup allows absent password and optional profile',
    (SELECT password_hash IS NULL AND email_verified_at IS NULL AND gender IS NULL
        AND address IS NULL AND date_of_birth IS NULL AND role = 'USER' AND status = 'ACTIVE'
     FROM users WHERE id = pg_temp.id(8,1)));
SELECT pg_temp.assert_true('auth/verified email may precede final account creation',
    (SELECT email_verified_at < created_at AND gender = 'Khác'
     FROM users WHERE id = pg_temp.id(8,2)));
SELECT pg_temp.expect_error('auth/username remains immutable',
    $$UPDATE users SET username='auth999' WHERE id=pg_temp.id(8,1)$$,
    '23514', 'users_identity_immutable');
SELECT pg_temp.expect_error('auth/account ID remains immutable',
    $$UPDATE users SET id=pg_temp.id(8,99) WHERE id=pg_temp.id(8,1)$$,
    '23514', 'users_identity_immutable');
SELECT pg_temp.expect_error('auth/username accepts ASCII only',
    $$INSERT INTO users (username,email,full_name) VALUES ('ábcd001','ascii@example.test','Tên')$$,
    '23514', 'users_username_check');
SELECT pg_temp.expect_error('auth/email requires structure',
    $$UPDATE users SET email='invalid@@example.test' WHERE id=pg_temp.id(8,1)$$,
    '23514', 'users_email_check');
SELECT pg_temp.expect_error('auth/email must be trimmed',
    $$UPDATE users SET email=' auth001@example.test' WHERE id=pg_temp.id(8,1)$$,
    '23514', 'users_email_check');
SELECT pg_temp.expect_error('auth/future birth date rejected',
    $$UPDATE users SET date_of_birth=(CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Ho_Chi_Minh')::date+1
      WHERE id=pg_temp.id(8,1)$$, '23514', 'users_birth_date_check');
SELECT pg_temp.expect_error('auth/profile gender domain',
    $$UPDATE users SET gender='UNKNOWN' WHERE id=pg_temp.id(8,1)$$, '23514', 'users_gender_check');
SELECT pg_temp.expect_error('auth/address length',
    $$UPDATE users SET address=repeat('x',256) WHERE id=pg_temp.id(8,1)$$,
    '23514', 'users_address_check');
SELECT pg_temp.expect_error('auth/empty permanent hash rejected',
    $$UPDATE users SET password_hash='' WHERE id=pg_temp.id(8,1)$$,
    '23514', 'users_password_hash_check');
SELECT pg_temp.expect_error('auth/soft deletion requires previous state',
    $$UPDATE users SET status='DELETED',deleted_at=CURRENT_TIMESTAMP WHERE id=pg_temp.id(8,1)$$,
    '23514', 'users_deleted_check');
UPDATE users SET status='LOCKED' WHERE id=pg_temp.id(8,1);
UPDATE users SET status_before_delete=status,status='DELETED',deleted_at=CURRENT_TIMESTAMP
    WHERE id=pg_temp.id(8,1);
SELECT pg_temp.assert_true('auth/soft deletion retains original locked state',
    (SELECT status='DELETED' AND status_before_delete='LOCKED' FROM users WHERE id=pg_temp.id(8,1)));
SELECT pg_temp.expect_error('auth/deleted username remains reserved',
    $$INSERT INTO users (username,email,full_name) VALUES ('AUTH001','new@example.test','Tên')$$,
    '23505', 'users_username_uq');
SELECT pg_temp.expect_error('auth/deleted email remains reserved',
    $$INSERT INTO users (username,email,full_name) VALUES ('auth003','AUTH001@example.test','Tên')$$,
    '23505', 'users_email_uq');
SELECT pg_temp.expect_error('auth/restored account must clear deletion metadata',
    $$UPDATE users SET status='LOCKED',deleted_at=NULL WHERE id=pg_temp.id(8,1)$$,
    '23514', 'users_deleted_check');
UPDATE users SET status=status_before_delete,status_before_delete=NULL,deleted_at=NULL
    WHERE id=pg_temp.id(8,1);
SELECT pg_temp.assert_true('auth/restore preserves lock and clears deletion metadata',
    (SELECT status='LOCKED' AND status_before_delete IS NULL AND deleted_at IS NULL
     FROM users WHERE id=pg_temp.id(8,1)));
UPDATE users SET status='ACTIVE' WHERE id=pg_temp.id(8,1);

INSERT INTO pending_registrations (id,username,email,password_hash) VALUES
    (pg_temp.id(8,101),'signup1','signup@example.test','test-only-registration-hash'),
    (pg_temp.id(8,102),'signup1','signup@example.test','test-only-registration-hash');
SELECT pg_temp.assert_true('auth/repeated pending signup neither reserves unique identity nor creates user',
    (SELECT count(*)=2 FROM pending_registrations WHERE email='signup@example.test')
    AND NOT EXISTS (SELECT FROM users WHERE username='signup1')
    AND (SELECT bool_and(expires_at=created_at+INTERVAL '30 minutes') FROM pending_registrations));
SELECT pg_temp.expect_error('auth/pending expiry fixed at 30 minutes',
    $$UPDATE pending_registrations SET expires_at=expires_at+INTERVAL '1 second'
      WHERE id=pg_temp.id(8,101)$$, '23514', 'pending_registrations_expiry_check');
SELECT pg_temp.expect_error('auth/signup completion requires verification',
    $$UPDATE pending_registrations SET completed_at=created_at+INTERVAL '2 minutes'
      WHERE id=pg_temp.id(8,101)$$, '23514', 'pending_registrations_completion_check');
UPDATE pending_registrations SET email_verified_at=created_at+INTERVAL '1 minute'
    WHERE id=pg_temp.id(8,101);
SELECT pg_temp.expect_error('auth/signup cannot complete at expiry boundary',
    $$UPDATE pending_registrations SET completed_at=expires_at WHERE id=pg_temp.id(8,101)$$,
    '23514', 'pending_registrations_completion_check');
UPDATE pending_registrations SET completed_at=created_at+INTERVAL '2 minutes'
    WHERE id=pg_temp.id(8,101);
SELECT pg_temp.expect_error('auth/signup cancellation excludes completion',
    $$UPDATE pending_registrations SET cancelled_at=created_at+INTERVAL '3 minutes'
      WHERE id=pg_temp.id(8,101)$$, '23514', 'pending_registrations_end_check');
SELECT pg_temp.assert_true('auth/pending completion records metadata without implicit user creation',
    NOT EXISTS (SELECT FROM users WHERE username='signup1'));

INSERT INTO email_verification_codes (id,purpose,pending_registration_id,target_email,code_verifier)
    VALUES (pg_temp.id(8,201),'REGISTRATION',pg_temp.id(8,102),'signup@example.test','test-only-otp-verifier');
INSERT INTO email_verification_codes (id,purpose,user_id,target_email,code_verifier) VALUES
    (pg_temp.id(8,202),'ACCOUNT_EMAIL',pg_temp.id(8,1),'auth001@example.test','test-only-otp-verifier'),
    (pg_temp.id(8,203),'EMAIL_CHANGE',pg_temp.id(8,2),'changed@example.test','test-only-otp-verifier');
SELECT pg_temp.assert_true('auth/OTP contexts and ten minute default',
    (SELECT count(*)=3 AND bool_and(expires_at=issued_at+INTERVAL '10 minutes' AND failed_attempts=0)
     FROM email_verification_codes));
SELECT pg_temp.expect_error('auth/OTP cannot have both parents',
    $$UPDATE email_verification_codes SET user_id=pg_temp.id(8,1) WHERE id=pg_temp.id(8,201)$$,
    '23514', 'email_verification_codes_context_check');
SELECT pg_temp.expect_error('auth/OTP cannot have no parent',
    $$UPDATE email_verification_codes SET pending_registration_id=NULL WHERE id=pg_temp.id(8,201)$$,
    '23514', 'email_verification_codes_context_check');
SELECT pg_temp.expect_error('auth/OTP purpose must match parent type',
    $$UPDATE email_verification_codes SET purpose='REGISTRATION' WHERE id=pg_temp.id(8,202)$$,
    '23514', 'email_verification_codes_context_check');
SELECT pg_temp.expect_error('auth/OTP unknown purpose rejected',
    $$UPDATE email_verification_codes SET purpose='LOGIN' WHERE id=pg_temp.id(8,202)$$,
    '23514', 'email_verification_codes_context_check');
SELECT pg_temp.expect_error('auth/OTP requires existing registration',
    $$UPDATE email_verification_codes SET pending_registration_id=pg_temp.id(8,999)
      WHERE id=pg_temp.id(8,201)$$, '23503');
SELECT pg_temp.expect_error('auth/OTP requires existing user',
    $$UPDATE email_verification_codes SET user_id=pg_temp.id(8,999) WHERE id=pg_temp.id(8,202)$$, '23503');
SELECT pg_temp.expect_error('auth/OTP cannot extend expiry',
    $$UPDATE email_verification_codes SET expires_at=expires_at+INTERVAL '1 second'
      WHERE id=pg_temp.id(8,201)$$, '23514', 'email_verification_codes_expiry_check');
SELECT pg_temp.expect_error('auth/OTP attempts cannot be negative',
    $$UPDATE email_verification_codes SET failed_attempts=-1 WHERE id=pg_temp.id(8,201)$$,
    '23514', 'email_verification_codes_attempts_check');
UPDATE email_verification_codes SET failed_attempts=5 WHERE id=pg_temp.id(8,201);
SELECT pg_temp.expect_error('auth/fifth OTP failure prevents successful consumption',
    $$UPDATE email_verification_codes SET consumed_at=issued_at+INTERVAL '1 minute'
      WHERE id=pg_temp.id(8,201)$$, '23514', 'email_verification_codes_consumption_check');
SELECT pg_temp.expect_error('auth/OTP rejected at exact expiry',
    $$UPDATE email_verification_codes SET consumed_at=expires_at WHERE id=pg_temp.id(8,202)$$,
    '23514', 'email_verification_codes_consumption_check');
UPDATE email_verification_codes SET invalidated_at=issued_at+INTERVAL '1 minute'
    WHERE id=pg_temp.id(8,202);
SELECT pg_temp.expect_error('auth/invalidated OTP cannot be consumed',
    $$UPDATE email_verification_codes SET consumed_at=issued_at+INTERVAL '2 minutes'
      WHERE id=pg_temp.id(8,202)$$, '23514', 'email_verification_codes_consumption_check');
UPDATE email_verification_codes SET consumed_at=issued_at+INTERVAL '1 minute'
    WHERE id=pg_temp.id(8,203);
SELECT pg_temp.assert_true('auth/consumed OTP retains separate target email',
    (SELECT target_email='changed@example.test' AND consumed_at IS NOT NULL
     FROM email_verification_codes WHERE id=pg_temp.id(8,203))
    AND (SELECT email='auth002@example.test' FROM users WHERE id=pg_temp.id(8,2)));

INSERT INTO password_reset_tokens (id,user_id,purpose,temporary_password_hash) VALUES
    (pg_temp.id(8,301),pg_temp.id(8,1),'INITIAL_SETUP','test-only-temporary-hash'),
    (pg_temp.id(8,302),pg_temp.id(8,2),'USER_RESET','test-only-temporary-hash'),
    (pg_temp.id(8,303),pg_temp.id(8,2),'ADMIN_RESET','test-only-temporary-hash');
SELECT pg_temp.assert_true('auth/reset purposes and fifteen minute default',
    (SELECT count(*)=3 AND bool_and(expires_at=issued_at+INTERVAL '15 minutes') FROM password_reset_tokens));
SELECT pg_temp.assert_true('auth/issuing temporary password preserves permanent password',
    (SELECT password_hash='test-only-password-hash' FROM users WHERE id=pg_temp.id(8,2)));
SELECT pg_temp.expect_error('auth/reset requires existing user',
    $$UPDATE password_reset_tokens SET user_id=pg_temp.id(8,999) WHERE id=pg_temp.id(8,301)$$, '23503');
SELECT pg_temp.expect_error('auth/reset completion requires authentication',
    $$UPDATE password_reset_tokens SET completed_at=issued_at+INTERVAL '2 minutes'
      WHERE id=pg_temp.id(8,301)$$, '23514', 'password_reset_tokens_completion_check');
SELECT pg_temp.expect_error('auth/temporary password rejected at exact expiry',
    $$UPDATE password_reset_tokens SET authenticated_at=expires_at WHERE id=pg_temp.id(8,301)$$,
    '23514', 'password_reset_tokens_authentication_check');
UPDATE password_reset_tokens SET authenticated_at=issued_at+INTERVAL '1 minute'
    WHERE id=pg_temp.id(8,302);
SELECT pg_temp.expect_error('auth/reset authentication does not extend expiry',
    $$UPDATE password_reset_tokens SET expires_at=expires_at+INTERVAL '1 minute'
      WHERE id=pg_temp.id(8,302)$$, '23514', 'password_reset_tokens_expiry_check');
SELECT pg_temp.expect_error('auth/password save rejected at original expiry boundary',
    $$UPDATE password_reset_tokens SET completed_at=expires_at WHERE id=pg_temp.id(8,302)$$,
    '23514', 'password_reset_tokens_completion_check');
UPDATE password_reset_tokens SET completed_at=issued_at+INTERVAL '2 minutes'
    WHERE id=pg_temp.id(8,302);
UPDATE password_reset_tokens SET revoked_at=issued_at+INTERVAL '1 minute'
    WHERE id=pg_temp.id(8,303);
SELECT pg_temp.expect_error('auth/revoked reset cannot complete',
    $$UPDATE password_reset_tokens SET authenticated_at=issued_at+INTERVAL '1 minute',
      completed_at=issued_at+INTERVAL '2 minutes' WHERE id=pg_temp.id(8,303)$$,
    '23514', 'password_reset_tokens_completion_check');

INSERT INTO user_sessions (id,user_id,scope,token_hash) VALUES
    (pg_temp.id(8,401),pg_temp.id(8,1),'USER_CLIENT','test-only-user-token-1'),
    (pg_temp.id(8,402),pg_temp.id(8,1),'USER_CLIENT','test-only-user-token-2'),
    (pg_temp.id(8,403),pg_temp.id(0,4),'ADMIN_APP','test-only-admin-token-1'),
    (pg_temp.id(8,404),pg_temp.id(0,4),'USER_CLIENT','test-only-admin-user-token');
SELECT pg_temp.assert_true('auth/multiple User sessions and independent scopes',
    (SELECT count(*)=2 FROM user_sessions WHERE user_id=pg_temp.id(8,1) AND revoked_at IS NULL)
    AND (SELECT count(*)=2 FROM user_sessions WHERE user_id=pg_temp.id(0,4) AND revoked_at IS NULL));
SELECT pg_temp.expect_error('auth/session token globally unique',
    $$INSERT INTO user_sessions (user_id,scope,token_hash)
      VALUES (pg_temp.id(8,2),'USER_CLIENT','test-only-user-token-1')$$,
    '23505', 'user_sessions_token_hash_uq');
SELECT pg_temp.expect_error('auth/second unrevoked Admin session rejected',
    $$INSERT INTO user_sessions (user_id,scope,token_hash)
      VALUES (pg_temp.id(0,4),'ADMIN_APP','test-only-admin-token-2')$$,
    '23505', 'user_sessions_one_live_admin_uq');
SELECT pg_temp.expect_error('auth/session requires existing user',
    $$INSERT INTO user_sessions (user_id,scope,token_hash)
      VALUES (pg_temp.id(8,999),'USER_CLIENT','test-only-orphan-token')$$, '23503');
SELECT pg_temp.expect_error('auth/session scope domain',
    $$UPDATE user_sessions SET scope='ALL' WHERE id=pg_temp.id(8,401)$$,
    '23514', 'user_sessions_scope_check');
SELECT pg_temp.expect_error('auth/revocation cannot precede session creation',
    $$UPDATE user_sessions SET revoked_at=created_at-INTERVAL '1 second'
      WHERE id=pg_temp.id(8,401)$$, '23514', 'user_sessions_revocation_check');
UPDATE user_sessions SET revoked_at=created_at WHERE id=pg_temp.id(8,403);
INSERT INTO user_sessions (user_id,scope,token_hash)
    VALUES (pg_temp.id(0,4),'ADMIN_APP','test-only-admin-token-2');
SELECT pg_temp.assert_true('auth/Admin replacement retains history and leaves User session intact',
    (SELECT count(*)=2 AND count(*) FILTER (WHERE revoked_at IS NULL)=1
     FROM user_sessions WHERE user_id=pg_temp.id(0,4) AND scope='ADMIN_APP')
    AND (SELECT revoked_at IS NULL FROM user_sessions WHERE id=pg_temp.id(8,404)));

-- Purge deletes auth data; soft deletion is only an UPDATE and does not cascade.
UPDATE users SET status_before_delete=status,status='DELETED',deleted_at=CURRENT_TIMESTAMP
    WHERE id=pg_temp.id(8,1);
SELECT pg_temp.assert_true('auth/soft delete does not physically remove auth history',
    EXISTS (SELECT FROM user_sessions WHERE user_id=pg_temp.id(8,1))
    AND EXISTS (SELECT FROM password_reset_tokens WHERE user_id=pg_temp.id(8,1)));
DELETE FROM users WHERE id=pg_temp.id(8,1);
SELECT pg_temp.assert_true('auth/purge removes account credentials and sessions',
    NOT EXISTS (SELECT FROM email_verification_codes WHERE user_id=pg_temp.id(8,1))
    AND NOT EXISTS (SELECT FROM password_reset_tokens WHERE user_id=pg_temp.id(8,1))
    AND NOT EXISTS (SELECT FROM user_sessions WHERE user_id=pg_temp.id(8,1)));
DELETE FROM pending_registrations WHERE id=pg_temp.id(8,102);
SELECT pg_temp.assert_true('auth/pending cleanup removes its OTP and preserves users and other contexts',
    NOT EXISTS (SELECT FROM email_verification_codes WHERE id=pg_temp.id(8,201))
    AND EXISTS (SELECT FROM users WHERE id=pg_temp.id(8,2))
    AND EXISTS (SELECT FROM email_verification_codes WHERE id=pg_temp.id(8,203)));

-- Friends/Blocks storage checks. Namespace 9 avoids the shared user fixtures.
SELECT pg_temp.assert_true('friends/three tables and six user foreign keys',
    (SELECT count(*)=3 FROM pg_tables WHERE schemaname='public'
        AND tablename IN ('friend_requests','friendships','blocks'))
    AND (SELECT count(*)=6 FROM pg_constraint WHERE contype='f'
        AND conrelid IN ('friend_requests'::regclass,'friendships'::regclass,'blocks'::regclass)));

INSERT INTO users (id,username,email,full_name) VALUES
    (pg_temp.id(9,1),'friend01','friend01@example.test','Friend One'),
    (pg_temp.id(9,2),'friend02','friend02@example.test','Friend Two'),
    (pg_temp.id(9,3),'friend03','friend03@example.test','Friend Three'),
    (pg_temp.id(9,4),'friend04','friend04@example.test','Friend Four');

INSERT INTO friend_requests (id,sender_id,recipient_id,status) VALUES
    (pg_temp.id(9,101),pg_temp.id(9,1),pg_temp.id(9,2),'PENDING');
SELECT pg_temp.assert_true('friends/pending request stores endpoints and no resolution',
    (SELECT sender_id=pg_temp.id(9,1) AND recipient_id=pg_temp.id(9,2)
        AND resolved_at IS NULL AND cancel_reason IS NULL
     FROM friend_requests WHERE id=pg_temp.id(9,101)));
SELECT pg_temp.expect_error('friends/no reverse pending request for same pair',
    $$INSERT INTO friend_requests (id,sender_id,recipient_id,status)
      VALUES (pg_temp.id(9,102),pg_temp.id(9,2),pg_temp.id(9,1),'PENDING')$$,
    '23505','friend_requests_pending_pair_uq');
SELECT pg_temp.expect_error('friends/no duplicate pending request in same direction',
    $$INSERT INTO friend_requests (id,sender_id,recipient_id,status)
      VALUES (pg_temp.id(9,103),pg_temp.id(9,1),pg_temp.id(9,2),'PENDING')$$,
    '23505','friend_requests_pending_pair_uq');
SELECT pg_temp.expect_error('friends/request cannot target sender',
    $$INSERT INTO friend_requests (id,sender_id,recipient_id,status)
      VALUES (pg_temp.id(9,104),pg_temp.id(9,1),pg_temp.id(9,1),'PENDING')$$,
    '23514','friend_requests_distinct_users_check');
SELECT pg_temp.expect_error('friends/request requires existing recipient',
    $$INSERT INTO friend_requests (id,sender_id,recipient_id,status)
      VALUES (pg_temp.id(9,105),pg_temp.id(9,1),pg_temp.id(9,999),'PENDING')$$,
    '23503');
SELECT pg_temp.expect_error('friends/request rejects unknown status',
    $$INSERT INTO friend_requests (id,sender_id,recipient_id,status)
      VALUES (pg_temp.id(9,106),pg_temp.id(9,1),pg_temp.id(9,3),'EXPIRED')$$,
    '23514');
SELECT pg_temp.expect_error('friends/pending request cannot have cancellation reason',
    $$UPDATE friend_requests SET cancel_reason='BLOCKED' WHERE id=pg_temp.id(9,101)$$,
    '23514','friend_requests_resolution_check');
SELECT pg_temp.expect_error('friends/pending request cannot have resolution time',
    $$UPDATE friend_requests SET resolved_at=created_at WHERE id=pg_temp.id(9,101)$$,
    '23514','friend_requests_resolution_check');
SELECT pg_temp.expect_error('friends/terminal request requires resolution time',
    $$UPDATE friend_requests SET status='REJECTED' WHERE id=pg_temp.id(9,101)$$,
    '23514','friend_requests_resolution_check');
SELECT pg_temp.expect_error('friends/cancelled request requires reason',
    $$UPDATE friend_requests SET status='CANCELLED',resolved_at=created_at
      WHERE id=pg_temp.id(9,101)$$, '23514','friend_requests_resolution_check');
SELECT pg_temp.expect_error('friends/rejected request cannot carry cancel reason',
    $$UPDATE friend_requests SET status='REJECTED',resolved_at=created_at,
      cancel_reason='BLOCKED' WHERE id=pg_temp.id(9,101)$$,
    '23514','friend_requests_resolution_check');
SELECT pg_temp.expect_error('friends/unknown cancellation reason rejected',
    $$UPDATE friend_requests SET status='CANCELLED',resolved_at=created_at,
      cancel_reason='EXPIRED' WHERE id=pg_temp.id(9,101)$$,
    '23514','friend_requests_resolution_check');
SELECT pg_temp.expect_error('friends/resolution cannot precede creation',
    $$UPDATE friend_requests SET status='REJECTED',resolved_at=created_at-INTERVAL '1 second'
      WHERE id=pg_temp.id(9,101)$$, '23514','friend_requests_resolution_check');
UPDATE friend_requests SET status='REJECTED',resolved_at=created_at WHERE id=pg_temp.id(9,101);
INSERT INTO friend_requests (id,sender_id,recipient_id,status) VALUES
    (pg_temp.id(9,107),pg_temp.id(9,2),pg_temp.id(9,1),'PENDING');
UPDATE friend_requests SET status='CANCELLED',cancel_reason='SENDER_WITHDREW',
    resolved_at=created_at WHERE id=pg_temp.id(9,107);
INSERT INTO friend_requests (id,sender_id,recipient_id,status) VALUES
    (pg_temp.id(9,108),pg_temp.id(9,1),pg_temp.id(9,2),'PENDING');
UPDATE friend_requests SET status='CANCELLED',cancel_reason='BLOCKED',
    resolved_at=created_at WHERE id=pg_temp.id(9,108);
SELECT pg_temp.assert_true('friends/history allows new request after rejection or cancellation',
    (SELECT count(*)=3 AND count(*) FILTER (WHERE status='PENDING')=0
     FROM friend_requests WHERE sender_id IN (pg_temp.id(9,1),pg_temp.id(9,2))
       AND recipient_id IN (pg_temp.id(9,1),pg_temp.id(9,2))));

INSERT INTO blocks (blocker_id,blocked_id) VALUES
    (pg_temp.id(9,1),pg_temp.id(9,2)),(pg_temp.id(9,2),pg_temp.id(9,1));
SELECT pg_temp.assert_true('friends/blocks are directional and may coexist',
    (SELECT count(*)=2 FROM blocks WHERE blocker_id IN (pg_temp.id(9,1),pg_temp.id(9,2))
        AND blocked_id IN (pg_temp.id(9,1),pg_temp.id(9,2))));
SELECT pg_temp.expect_error('friends/self block rejected',
    $$INSERT INTO blocks (blocker_id,blocked_id) VALUES (pg_temp.id(9,1),pg_temp.id(9,1))$$,
    '23514','blocks_distinct_users_check');
SELECT pg_temp.expect_error('friends/duplicate directional block rejected',
    $$INSERT INTO blocks (blocker_id,blocked_id) VALUES (pg_temp.id(9,1),pg_temp.id(9,2))$$,
    '23505','blocks_pkey');
SELECT pg_temp.expect_error('friends/block requires existing account',
    $$INSERT INTO blocks (blocker_id,blocked_id) VALUES (pg_temp.id(9,1),pg_temp.id(9,999))$$,
    '23503');
DELETE FROM blocks WHERE blocker_id=pg_temp.id(9,1) AND blocked_id=pg_temp.id(9,2);
SELECT pg_temp.assert_true('friends/unblock leaves reverse direction intact',
    NOT EXISTS (SELECT FROM blocks WHERE blocker_id=pg_temp.id(9,1) AND blocked_id=pg_temp.id(9,2))
    AND EXISTS (SELECT FROM blocks WHERE blocker_id=pg_temp.id(9,2) AND blocked_id=pg_temp.id(9,1)));
DELETE FROM blocks WHERE blocker_id=pg_temp.id(9,2) AND blocked_id=pg_temp.id(9,1);

INSERT INTO friendships (user_low_id,user_high_id) VALUES (pg_temp.id(9,1),pg_temp.id(9,2));
SELECT pg_temp.expect_error('friends/self friendship rejected',
    $$INSERT INTO friendships (user_low_id,user_high_id)
      VALUES (pg_temp.id(9,2),pg_temp.id(9,2))$$,
    '23514','friendships_order_check');
SELECT pg_temp.expect_error('friends/reversed friendship pair rejected',
    $$INSERT INTO friendships (user_low_id,user_high_id)
      VALUES (pg_temp.id(9,2),pg_temp.id(9,1))$$,
    '23514','friendships_order_check');
SELECT pg_temp.expect_error('friends/duplicate canonical friendship rejected',
    $$INSERT INTO friendships (user_low_id,user_high_id)
      VALUES (pg_temp.id(9,1),pg_temp.id(9,2))$$,
    '23505','friendships_pkey');
SELECT pg_temp.expect_error('friends/friendship requires existing account',
    $$INSERT INTO friendships (user_low_id,user_high_id)
      VALUES (pg_temp.id(9,1),pg_temp.id(9,999))$$,
    '23503');
INSERT INTO friend_requests (id,sender_id,recipient_id,status) VALUES
    (pg_temp.id(9,110),pg_temp.id(9,2),pg_temp.id(9,3),'PENDING');
UPDATE friend_requests SET status='ACCEPTED',resolved_at=created_at WHERE id=pg_temp.id(9,110);
INSERT INTO friendships (user_low_id,user_high_id) VALUES (pg_temp.id(9,2),pg_temp.id(9,3));
SELECT pg_temp.assert_true('friends/accepted request history coexists with current friendship',
    (SELECT status='ACCEPTED' AND cancel_reason IS NULL AND resolved_at IS NOT NULL
     FROM friend_requests WHERE id=pg_temp.id(9,110))
    AND EXISTS (SELECT FROM friendships
        WHERE user_low_id=pg_temp.id(9,2) AND user_high_id=pg_temp.id(9,3)));
INSERT INTO friend_requests (id,sender_id,recipient_id,status) VALUES
    (pg_temp.id(9,109),pg_temp.id(9,1),pg_temp.id(9,3),'PENDING');
INSERT INTO blocks (blocker_id,blocked_id) VALUES (pg_temp.id(9,1),pg_temp.id(9,4));
UPDATE users SET status_before_delete=status,status='DELETED',deleted_at=CURRENT_TIMESTAMP
    WHERE id=pg_temp.id(9,1);
SELECT pg_temp.assert_true('friends/soft deletion retains relationships and pending invitation',
    EXISTS (SELECT FROM friendships WHERE user_low_id=pg_temp.id(9,1))
    AND EXISTS (SELECT FROM blocks WHERE blocker_id=pg_temp.id(9,1))
    AND EXISTS (SELECT FROM friend_requests WHERE id=pg_temp.id(9,109) AND status='PENDING'));
UPDATE users SET status=status_before_delete,status_before_delete=NULL,deleted_at=NULL
    WHERE id=pg_temp.id(9,1);
UPDATE users SET status='LOCKED' WHERE id=pg_temp.id(9,1);
SELECT pg_temp.assert_true('friends/lock retains relationships without schema-side action gate',
    EXISTS (SELECT FROM friendships WHERE user_low_id=pg_temp.id(9,1))
    AND EXISTS (SELECT FROM blocks WHERE blocker_id=pg_temp.id(9,1))
    AND EXISTS (SELECT FROM friend_requests WHERE id=pg_temp.id(9,109) AND status='PENDING'));
DELETE FROM users WHERE id=pg_temp.id(9,1);
SELECT pg_temp.assert_true('friends/purge cascades only relationships of removed account',
    NOT EXISTS (SELECT FROM friendships WHERE user_low_id=pg_temp.id(9,1) OR user_high_id=pg_temp.id(9,1))
    AND NOT EXISTS (SELECT FROM blocks WHERE blocker_id=pg_temp.id(9,1) OR blocked_id=pg_temp.id(9,1))
    AND NOT EXISTS (SELECT FROM friend_requests WHERE sender_id=pg_temp.id(9,1)
        OR recipient_id=pg_temp.id(9,1))
    AND EXISTS (SELECT FROM users WHERE id=pg_temp.id(9,2))
    AND EXISTS (SELECT FROM friendships
        WHERE user_low_id=pg_temp.id(9,2) AND user_high_id=pg_temp.id(9,3)));
DELETE FROM users WHERE id=pg_temp.id(9,3);
SELECT pg_temp.assert_true('friends/purge cascades from high friendship endpoint',
    NOT EXISTS (SELECT FROM friendships
        WHERE user_low_id=pg_temp.id(9,2) AND user_high_id=pg_temp.id(9,3))
    AND EXISTS (SELECT FROM users WHERE id=pg_temp.id(9,2)));
DELETE FROM users WHERE id IN (pg_temp.id(9,2),pg_temp.id(9,4));
