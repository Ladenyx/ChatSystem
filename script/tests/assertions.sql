CREATE TEMP TABLE db_checks (label TEXT PRIMARY KEY);

-- Fixture namespaces: 0=user, 1=conversation, 2=message, 3=device,
-- 4=backup, 5=epoch, 6=report, 7=snapshot-only message ID.
CREATE FUNCTION pg_temp.id(namespace INTEGER, number INTEGER) RETURNS UUID
LANGUAGE sql IMMUTABLE AS $$
    SELECT (namespace::text || '0000000-0000-0000-0000-' || lpad(number::text, 12, '0'))::uuid
$$;

CREATE FUNCTION pg_temp.assert_true(label TEXT, actual BOOLEAN) RETURNS VOID
LANGUAGE plpgsql AS $$
BEGIN
    IF actual IS DISTINCT FROM TRUE THEN
        RAISE EXCEPTION 'Check failed: %', label;
    END IF;
    INSERT INTO db_checks VALUES (label);
END $$;

CREATE FUNCTION pg_temp.expect_error(label TEXT, command TEXT, expected_state TEXT, expected_constraint TEXT DEFAULT NULL)
RETURNS VOID LANGUAGE plpgsql AS $$
DECLARE
    actual_state TEXT;
    actual_constraint TEXT;
BEGIN
    BEGIN
        EXECUTE command;
    EXCEPTION WHEN OTHERS THEN
        GET STACKED DIAGNOSTICS actual_state = RETURNED_SQLSTATE, actual_constraint = CONSTRAINT_NAME;
        IF actual_state <> expected_state OR
            (expected_constraint IS NOT NULL AND actual_constraint IS DISTINCT FROM expected_constraint) THEN
            RAISE EXCEPTION 'Check %: expected % / %, received % / %',
                label, expected_state, expected_constraint, actual_state, actual_constraint;
        END IF;
        INSERT INTO db_checks VALUES (label);
        RETURN;
    END;
    RAISE EXCEPTION 'Check %: expected error %, but statement succeeded', label, expected_state;
END $$;
