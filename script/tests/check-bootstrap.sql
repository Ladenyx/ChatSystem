-- Read-only acceptance check for a freshly initialized, empty application DB.
\set ON_ERROR_STOP on
\pset tuples_only on
\pset format unaligned
DO $$
DECLARE
    relation_name TEXT;
    has_rows BOOLEAN;
BEGIN
    IF current_setting('server_version_num')::integer NOT BETWEEN 180000 AND 189999 THEN
        RAISE EXCEPTION 'Bootstrap acceptance requires PostgreSQL 18';
    END IF;
    IF current_setting('server_encoding') <> 'UTF8' OR current_setting('client_encoding') <> 'UTF8' THEN
        RAISE EXCEPTION 'Bootstrap acceptance requires UTF-8 on server and client';
    END IF;
    IF (SELECT count(*) FROM pg_extension WHERE extname IN ('pg_trgm','unaccent')) <> 2 THEN
        RAISE EXCEPTION 'Search extensions are missing';
    END IF;
    IF public.unaccent('Nguyễn Ánh') <> 'Nguyen Anh' THEN
        RAISE EXCEPTION 'Vietnamese UTF-8 text did not survive the bootstrap connection';
    END IF;
    FOREACH relation_name IN ARRAY ARRAY['users','friendships','message_deletions',
        'e2ee_group_key_envelopes','spam_report_evidences','message_send_events']
    LOOP
        IF to_regclass(format('public.%I',relation_name)) IS NULL THEN
            RAISE EXCEPTION 'Bootstrap did not load all domains: missing %',relation_name;
        END IF;
    END LOOP;
    FOR relation_name IN SELECT tablename FROM pg_tables WHERE schemaname='public'
    LOOP
        EXECUTE format('SELECT EXISTS (SELECT FROM public.%I)',relation_name) INTO has_rows;
        IF has_rows THEN RAISE EXCEPTION 'Unexpected bootstrap/test data in %',relation_name; END IF;
    END LOOP;
END $$;
SELECT format('Bootstrap verified: PostgreSQL %s; UTF-8; pg_trgm + unaccent; %s tables; %s columns; %s foreign keys; no application data',
    current_setting('server_version'),
    (SELECT count(*) FROM pg_tables WHERE schemaname='public'),
    (SELECT count(*) FROM information_schema.columns WHERE table_schema='public'),
    (SELECT count(*) FROM pg_constraint WHERE connamespace='public'::regnamespace AND contype='f'));
