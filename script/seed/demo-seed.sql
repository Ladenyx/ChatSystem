-- Idempotent demo seed for local development and course testing (Đợt 1).
-- Passwords verified by Pbkdf2PasswordHasherTest:
--   sysadmin             -> Admin@123
--   user01..03,locked01,deleted01 -> User@123
BEGIN;

INSERT INTO public.users (
    username, email, full_name, role, status, status_before_delete,
    password_hash, email_verified_at, created_at, deleted_at
)
SELECT
    v.username,
    v.email,
    v.full_name,
    v.role,
    v.status,
    v.status_before_delete,
    v.password_hash,
    v.email_verified_at,
    v.created_at,
    v.deleted_at
FROM (
    VALUES
        (
            'sysadmin',
            'sysadmin@whatsup.local',
            'Quản Trị Hệ Thống',
            'ADMIN',
            'ACTIVE',
            NULL::text,
            'pbkdf2$sha256$65536$V2hhdHNVcEFkbWluU2FsdA==$aXmt/Mq+SpYsUB63qBsjcmXrmetLHFiThTOSbsY/hnQ=',
            CURRENT_TIMESTAMP - INTERVAL '30 days',
            CURRENT_TIMESTAMP - INTERVAL '30 days',
            NULL::timestamptz
        ),
        (
            'user01',
            'minhan@whatsup.local',
            'Nguyễn Minh An',
            'USER',
            'ACTIVE',
            NULL::text,
            'pbkdf2$sha256$65536$V2hhdHNVcFVzZXJTYWx0MQ==$g5dLpqBRue3mhrFRB+Xvbc91lZ1ojydYcTPWXhAPJAI=',
            CURRENT_TIMESTAMP - INTERVAL '25 days',
            CURRENT_TIMESTAMP - INTERVAL '25 days',
            NULL::timestamptz
        ),
        (
            'user02',
            'giahuy@whatsup.local',
            'Trần Gia Huy',
            'USER',
            'ACTIVE',
            NULL::text,
            'pbkdf2$sha256$65536$V2hhdHNVcFVzZXJTYWx0MQ==$g5dLpqBRue3mhrFRB+Xvbc91lZ1ojydYcTPWXhAPJAI=',
            CURRENT_TIMESTAMP - INTERVAL '20 days',
            CURRENT_TIMESTAMP - INTERVAL '20 days',
            NULL::timestamptz
        ),
        (
            'user03',
            'thaovy@whatsup.local',
            'Lê Thảo Vy',
            'USER',
            'ACTIVE',
            NULL::text,
            'pbkdf2$sha256$65536$V2hhdHNVcFVzZXJTYWx0MQ==$g5dLpqBRue3mhrFRB+Xvbc91lZ1ojydYcTPWXhAPJAI=',
            CURRENT_TIMESTAMP - INTERVAL '15 days',
            CURRENT_TIMESTAMP - INTERVAL '15 days',
            NULL::timestamptz
        ),
        (
            'locked01',
            'quocbao@whatsup.local',
            'Phạm Quốc Bảo',
            'USER',
            'LOCKED',
            NULL::text,
            'pbkdf2$sha256$65536$V2hhdHNVcFVzZXJTYWx0MQ==$g5dLpqBRue3mhrFRB+Xvbc91lZ1ojydYcTPWXhAPJAI=',
            CURRENT_TIMESTAMP - INTERVAL '10 days',
            CURRENT_TIMESTAMP - INTERVAL '10 days',
            NULL::timestamptz
        ),
        (
            'deleted01',
            'haidang@whatsup.local',
            'Võ Hải Đăng',
            'USER',
            'DELETED',
            'ACTIVE',
            'pbkdf2$sha256$65536$V2hhdHNVcFVzZXJTYWx0MQ==$g5dLpqBRue3mhrFRB+Xvbc91lZ1ojydYcTPWXhAPJAI=',
            CURRENT_TIMESTAMP - INTERVAL '8 days',
            CURRENT_TIMESTAMP - INTERVAL '8 days',
            CURRENT_TIMESTAMP - INTERVAL '2 days'
        )
) AS v(
    username, email, full_name, role, status, status_before_delete,
    password_hash, email_verified_at, created_at, deleted_at
)
WHERE NOT EXISTS (
    SELECT 1 FROM public.users u WHERE lower(u.username) = lower(v.username)
);

COMMIT;
