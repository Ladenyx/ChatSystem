\set ON_ERROR_STOP on
\ir selection.sql
BEGIN;
SET LOCAL search_path = public;
SET LOCAL TIME ZONE 'UTC';
\ir environment.sql
\ir ../anchors/users.sql
\ir ../anchors/conversations.sql
\if :run_messaging
\ir ../domains/messaging.sql
\endif
\if :run_e2ee
\ir ../domains/e2ee.sql
\endif
\if :run_spam
\ir ../domains/spam.sql
\endif
COMMIT;
