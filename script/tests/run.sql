\set ON_ERROR_STOP on
\pset tuples_only on
\pset format unaligned
\ir ../common/selection.sql
DO $$
BEGIN
    IF current_database() !~ '^chatsystem_schema_test(_(all|messaging|e2ee|spam)_[0-9a-f]{12})?$' THEN
        RAISE EXCEPTION 'Use only a dedicated chatsystem_schema_test database';
    END IF;
END $$;
BEGIN;
SET LOCAL search_path = public;
SET LOCAL TIME ZONE 'UTC';
\ir common/assertions.sql
\ir common/fixtures.sql
\ir common/cases.sql
\if :run_messaging
\ir messaging/fixtures.sql
\ir messaging/cases.sql
\endif
\if :run_e2ee
\ir e2ee/fixtures.sql
\ir e2ee/cases.sql
\endif
\if :run_spam
\ir spam/fixtures.sql
\ir spam/cases.sql
\endif
SELECT format('%s scaffold checks passed (domain=%s); business-domain checks are pending', count(*), :'domain') FROM db_checks;
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
