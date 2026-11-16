\set ON_ERROR_STOP on
BEGIN;
\ir sql/00-core-users.sql
\ir sql/10-auth-and-friends.sql
\ir sql/20-messaging-e2ee-spam.sql
\ir sql/30-logs-and-analytics.sql
COMMIT;
