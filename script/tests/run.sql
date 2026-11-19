\set ON_ERROR_STOP on
\pset tuples_only on
\pset format unaligned
\if :{?flow}
\else
\set flow all
\endif
DO $$
BEGIN
    IF current_database() !~ '^chatsystem_schema_test(_(all|auth_friends|messaging_e2ee_spam|logs_analytics)_[0-9a-f]{12})?$' THEN
        RAISE EXCEPTION 'Use only a dedicated chatsystem_schema_test database';
    END IF;
END $$;
BEGIN;
SET LOCAL search_path = public;
SET LOCAL TIME ZONE 'UTC';
\ir assertions.sql
\ir fixtures.sql
\ir test-core-users.sql
\ir test-auth-friends.sql
\ir test-messaging-e2ee-spam.sql
\ir test-logs-analytics.sql
SELECT format('%s SQL checks passed (flow=%s)',count(*),:'flow') FROM db_checks;
ROLLBACK;
DO $$
DECLARE
    table_name TEXT;
    has_rows BOOLEAN;
BEGIN
    FOR table_name IN SELECT tablename FROM pg_tables WHERE schemaname='public'
    LOOP
        EXECUTE format('SELECT EXISTS (SELECT FROM public.%I)',table_name) INTO has_rows;
        IF has_rows THEN RAISE EXCEPTION 'Leftover test data in %',table_name; END IF;
    END LOOP;
    RAISE NOTICE 'All application tables are empty after rollback';
END $$;
