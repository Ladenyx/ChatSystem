-- Common PostgreSQL 18 environment. Included within bootstrap transaction.
DO $$
BEGIN
    IF current_setting('server_version_num')::integer NOT BETWEEN 180000 AND 189999 THEN
        RAISE EXCEPTION 'This schema targets PostgreSQL 18';
    END IF;
END $$;

CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE EXTENSION IF NOT EXISTS unaccent;

