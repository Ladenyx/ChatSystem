#!/usr/bin/env python3
"""Run one member-owned DDL/Test pair in an isolated temporary database."""
import argparse
from concurrent.futures import ThreadPoolExecutor
import io
import os
from pathlib import Path
import re
import shutil
import subprocess
import sys
import tarfile
import tempfile
import time
import uuid

SCRIPT = Path(__file__).resolve().parent
FLOWS = {
    "auth-friends": ("10-auth-and-friends.sql", "test-auth-friends.sql"),
    "messaging-e2ee-spam": ("20-messaging-e2ee-spam.sql", "test-messaging-e2ee-spam.sql"),
    "logs-analytics": ("30-logs-and-analytics.sql", "test-logs-analytics.sql"),
}
TEST_DATABASE_OPTIONS = ("--template=template0", "--encoding=UTF8",
                         "--locale-provider=builtin", "--builtin-locale=C.UTF-8")


def execute(command, *, env=None, input_data=None):
    result = subprocess.run(command, input=input_data, stdout=subprocess.PIPE,
                            stderr=subprocess.PIPE, env=env)
    if result.returncode:
        stdout = result.stdout.decode(errors="replace").strip()
        if stdout:
            print(stdout, file=sys.stderr)
        if result.stderr:
            print(result.stderr.decode(errors="replace"), file=sys.stderr)
        raise subprocess.CalledProcessError(result.returncode, command)
    return result


def show_checks(result):
    for line in result.stdout.decode().splitlines():
        if line.strip():
            print(line)
    if result.stderr:
        print(result.stderr.decode().strip())


def pg_binaries(directory, *names):
    bins = {}
    for name in names:
        executable = name + ".exe" if os.name == "nt" else name
        found = str(directory / executable) if directory else shutil.which(executable)
        if not found or not Path(found).is_file():
            raise ValueError(f"Cannot find {executable}; set --pg-bin to PostgreSQL 18/bin")
        bins[name] = found
    return bins


def run_competing_writes(run_sql, first_write, second_write):
    session = """
        SET statement_timeout = '20s';
        SET TIME ZONE 'UTC';
        BEGIN ISOLATION LEVEL READ COMMITTED;
    """
    # A takes an advisory lock after writing. B sees that lock before writing.
    # A commits only after PostgreSQL reports that B is blocked by A.
    first = session + first_write + """
            DO $$
            BEGIN
                PERFORM pg_advisory_xact_lock(701);
                FOR attempt IN 1..400 LOOP
                    PERFORM pg_stat_clear_snapshot();
                    IF EXISTS (SELECT FROM pg_stat_activity
                               WHERE application_name='chatsystem_second_writer'
                                 AND pg_backend_pid()=ANY(pg_blocking_pids(pid))) THEN
                        RETURN;
                    END IF;
                    PERFORM pg_sleep(0.025);
                END LOOP;
                RAISE EXCEPTION 'Second write did not wait for the first transaction';
            END $$;
            COMMIT;
        """
    second = session + """
            SET application_name='chatsystem_second_writer';
            DO $$
            BEGIN
                FOR attempt IN 1..400 LOOP
                    IF NOT pg_try_advisory_lock(701) THEN
                        RETURN;
                    END IF;
                    PERFORM pg_advisory_unlock(701);
                    PERFORM pg_sleep(0.025);
                END LOOP;
                RAISE EXCEPTION 'First transaction did not become ready';
            END $$;
        """ + second_write + "COMMIT;"
    with ThreadPoolExecutor(max_workers=2) as pool:
        first_result = pool.submit(run_sql, first)
        second_result = pool.submit(run_sql, second)
        return first_result.result().stdout.strip(), second_result.result().stdout.strip()


def check_app_open_concurrency(run_sql):
    """Use two psql connections to verify a retry blocked on an uncommitted event."""
    account = "00000000-0000-0000-0000-000000000701"
    run_sql(f"""
        INSERT INTO public.users (id,username,email,full_name,created_at)
        VALUES ('{account}','race701','race701@example.test','Concurrent fixture','2026-01-01 UTC');
    """)
    for launch_number, launch_state in [(1, "first launch"), (2, "later launch")]:
        launch = f"90000000-0000-0000-0000-{launch_number:012d}"
        record = f"""
            SELECT public.record_app_open('{account}','{launch}','USER_CLIENT') AS event_id \\gset
            SELECT id,opened_at FROM public.app_open_events WHERE id=:'event_id';
        """
        original, repeated = run_competing_writes(run_sql, record, record)
        stored = run_sql(f"""
            SET TIME ZONE 'UTC';
            SELECT e.id,e.opened_at
            FROM public.app_open_events e JOIN public.app_open_accounts a USING (account_key)
            WHERE a.user_id='{account}' AND e.launch_id='{launch}';
        """).stdout.strip()
        if len(stored.splitlines()) != 1 or original != stored or repeated != stored:
            raise ValueError(f"Concurrent app open ({launch_state}) must keep one ID and timestamp")
        print(f"Concurrent app open ({launch_state}) preserved the event ID and timestamp")


def check_message_send_concurrency(run_sql):
    """Concurrent retry keeps the first accepted instant without an UPDATE."""
    sender = "00000000-0000-0000-0000-000000000721"
    recipient = "00000000-0000-0000-0000-000000000722"
    message = "20000000-0000-0000-0000-000000000721"
    run_sql(f"""
        INSERT INTO public.users (id,username,email,full_name,created_at) VALUES
          ('{sender}','race721','race721@example.test','Send fixture','2026-01-01 UTC'),
          ('{recipient}','race722','race722@example.test','Recipient fixture','2026-01-01 UTC');
    """)

    def record(accepted_at):
        return f"""
            SELECT public.record_message_send('{message}','{sender}','{recipient}',NULL,
                '{accepted_at} UTC') AS message_id \\gset
            SELECT message_id,accepted_at FROM public.message_send_events WHERE message_id=:'message_id';
        """

    original, repeated = run_competing_writes(
        run_sql, record("2026-11-01 01:00"), record("2026-11-02 02:00"))
    stored = run_sql(f"""
        SET TIME ZONE 'UTC';
        SELECT message_id,accepted_at FROM public.message_send_events WHERE message_id='{message}';
    """).stdout.strip()
    expected = f"{message}|2026-11-01 01:00:00+00".encode()
    if original != expected or repeated != expected or stored != expected:
        raise ValueError("Concurrent accepted-send retries must preserve the first event and timestamp")
    print("Concurrent accepted-send retry preserved one event and its first acceptance time")


def check_spam_review_concurrency(run_sql):
    """Exercise a guarded DEC-10 transition as reference SQL for the backend."""
    report = "60000000-0000-0000-0000-000000000711"
    admin_a = "00000000-0000-0000-0000-000000000713"
    admin_b = "00000000-0000-0000-0000-000000000714"
    run_sql(f"""
        INSERT INTO public.users (id,username,email,full_name,role) VALUES
          ('00000000-0000-0000-0000-000000000711','race711','race711@example.test','Reporter','USER'),
          ('00000000-0000-0000-0000-000000000712','race712','race712@example.test','Reported','USER'),
          ('{admin_a}','race713','race713@example.test','First administrator','ADMIN'),
          ('{admin_b}','race714','race714@example.test','Second administrator','ADMIN');
        INSERT INTO public.conversations (id,type)
        VALUES ('10000000-0000-0000-0000-000000000711','DIRECT');
        INSERT INTO public.spam_reports (id,reporter_user_id,reported_user_id,
            conversation_id,reason,received_at,request_fingerprint)
        VALUES ('{report}','00000000-0000-0000-0000-000000000711','00000000-0000-0000-0000-000000000712',
            '10000000-0000-0000-0000-000000000711','Fixture report','2026-11-01 00:00 UTC',
            sha256(convert_to('fixture','UTF8')));
    """)

    def review(admin, note, reviewed_at):
        return f"""
            UPDATE public.spam_reports
            SET review_status='REVIEWED', reviewed_by_user_id='{admin}',
                reviewed_at='{reviewed_at} UTC', review_note='{note}'
            WHERE id='{report}' AND review_status='PENDING'
            RETURNING review_status,reviewed_by_user_id,review_note,
                to_char(reviewed_at AT TIME ZONE 'UTC','YYYY-MM-DD HH24:MI:SS');
        """

    first, second = run_competing_writes(
        run_sql, review(admin_a, "First review", "2026-11-01 01:00"),
        review(admin_b, "Second review", "2026-11-01 02:00"))
    stored = run_sql(f"""
        SELECT review_status,reviewed_by_user_id,review_note,
            to_char(reviewed_at AT TIME ZONE 'UTC','YYYY-MM-DD HH24:MI:SS')
        FROM public.spam_reports WHERE id='{report}';
    """).stdout.strip()
    expected = f"REVIEWED|{admin_a}|First review|2026-11-01 01:00:00".encode()
    if first != expected or second or stored != expected:
        raise ValueError("Concurrent spam review must preserve the first administrator and note")
    print("Concurrent spam review (reference SQL) accepted one transition and preserved the first review")


def check_concurrency(flow, run_sql):
    if flow in ("logs-analytics", "all"):
        check_app_open_concurrency(run_sql)
        check_message_send_concurrency(run_sql)
    if flow in ("messaging-e2ee-spam", "all"):
        check_spam_review_concurrency(run_sql)


def prepare_source(destination, flow):
    """Snapshot this checkout; generate selected entry points only in temp storage."""
    for path in SCRIPT.rglob("*.sql"):
        if path.is_symlink():
            raise ValueError(f"SQL symlinks are not supported: {path}")
        target = destination / path.relative_to(SCRIPT)
        target.parent.mkdir(parents=True, exist_ok=True)
        shutil.copyfile(path, target)
    if flow == "all":
        return "schema.sql", "tests/run.sql"
    ddl, test = FLOWS[flow]
    (destination / "_bootstrap.sql").write_text(
        "\\set ON_ERROR_STOP on\nBEGIN;\n\\ir sql/00-core-users.sql\n"
        f"\\ir sql/{ddl}\nCOMMIT;\n", encoding="utf-8")
    run = (destination / "tests/run.sql").read_text(encoding="utf-8")
    directives = {"\\ir " + files[1] for files in FLOWS.values()}
    for directive in directives:
        if sum(line.strip() == directive for line in run.splitlines()) != 1:
            raise ValueError(f"tests/run.sql must include exactly once: {directive}")
    selected = [line for line in run.splitlines()
                if line.strip() not in directives or line.strip() == "\\ir " + test]
    (destination / "tests/_run_selected.sql").write_text("\n".join(selected) + "\n", encoding="utf-8")
    return "_bootstrap.sql", "tests/_run_selected.sql"


def native(args, database):
    if os.name == "nt":
        raise ValueError("--native uses Unix sockets. On Windows use --local-server or the Docker runner")
    bins = pg_binaries(args.pg_bin, "initdb", "pg_ctl", "createdb", "psql")
    version = execute([bins["initdb"], "--version"]).stdout.decode().strip()
    if not re.search(r"PostgreSQL\)?\s+18\.", version):
        raise ValueError(f"PostgreSQL 18 required, found {version}")
    env = {key: value for key, value in os.environ.items() if not key.startswith("PG")}
    env["PGCLIENTENCODING"] = "UTF8"
    with tempfile.TemporaryDirectory(prefix="cs-pg18-", dir="/tmp") as tmp:
        root = Path(tmp)
        source = root / "source"
        bootstrap, checks = prepare_source(source, args.flow)
        data, sock, log = root / "data", root / "socket", root / "postgres.log"
        sock.mkdir()
        execute([bins["initdb"], "-D", str(data), "-U", "chatsystem_test_owner",
                 "--encoding=UTF8", "--locale-provider=builtin", "--builtin-locale=C.UTF-8",
                 "--auth-local=trust", "--auth-host=scram-sha-256"], env=env)
        try:
            execute([bins["pg_ctl"], "-D", str(data), "-l", str(log), "-o",
                     f"-c listen_addresses='' -c unix_socket_directories='{sock}'",
                     "-w", "-t", "30", "start"], env=env)
            connection = ["-h", str(sock), "-p", "5432", "-U", "chatsystem_test_owner"]
            execute([bins["createdb"], *connection, database], env=env)
            psql = [bins["psql"], *connection, "-d", database, "-X", "-q",
                    "-v", "ON_ERROR_STOP=1", "-v", "flow=" + args.flow]
            execute(psql + ["-f", str(source / bootstrap)], env=env)
            show_checks(execute(psql + ["-f", str(source / checks)], env=env))
            check_concurrency(args.flow, lambda sql: execute(
                psql + ["-At", "-f", "-"], env=env, input_data=sql.encode("utf-8")))
        except subprocess.CalledProcessError:
            if log.exists():
                print(log.read_text(), file=sys.stderr)
            raise
        finally:
            if (data / "postmaster.pid").exists():
                execute([bins["pg_ctl"], "-D", str(data), "-m", "fast", "-w", "-t", "30", "stop"], env=env)


def local_server(args, database):
    """Run psql against a PostgreSQL 18 server already installed on this machine."""
    bins = pg_binaries(args.pg_bin, "createdb", "dropdb", "psql")
    client = execute([bins["psql"], "--version"]).stdout.decode().strip()
    if not re.search(r"PostgreSQL\)?\s+18\.", client):
        raise ValueError(f"PostgreSQL 18 psql required, found {client}")
    env = {**os.environ, "PGCLIENTENCODING": "UTF8", "PGCONNECT_TIMEOUT": "10"}
    # A host address and no service override keep the installed-server test on
    # loopback even when the terminal has libpq connection defaults configured.
    for name in ("PGHOSTADDR", "PGSERVICE", "PGSERVICEFILE"):
        env.pop(name, None)
    connection = ["-h", "127.0.0.1", "-p", str(args.pg_port or 5432),
                  "-U", args.pg_user or "postgres", "--no-password"]

    def pg(tool, *params, input_data=None):
        return execute([bins[tool], *connection, *params], env=env, input_data=input_data)

    psql = ["-X", "-q", "-v", "ON_ERROR_STOP=1"]
    server = pg("psql", *psql, "-At", "-d", "postgres", "-c",
                "SELECT current_setting('server_version_num'), version()").stdout.decode().strip()
    version, detail = server.split("|", 1)
    if not 180000 <= int(version) < 190000:
        raise ValueError(f"PostgreSQL 18 server required, found {detail}")
    print(f"Client: {client}\nServer: {detail}", flush=True)
    with tempfile.TemporaryDirectory(prefix="cs sql source ") as tmp:
        source = Path(tmp)
        bootstrap, checks = prepare_source(source, args.flow)
        pg("createdb", *TEST_DATABASE_OPTIONS, "--maintenance-db=postgres", database)
        try:
            target = [*psql, "-d", database, "-v", "flow=" + args.flow]
            pg("psql", *target, "-f", str(source / bootstrap))
            show_checks(pg("psql", *target, "-At", "-c",
                "SELECT format('Server encoding=%s; client encoding=%s; extensions=%s',"
                "current_setting('server_encoding'),current_setting('client_encoding'),"
                "(SELECT string_agg(extname,',' ORDER BY extname) FROM pg_extension"
                " WHERE extname IN ('pg_trgm','unaccent')))"))
            show_checks(pg("psql", *target, "-f", str(source / checks)))
            check_concurrency(args.flow, lambda sql: pg(
                "psql", *target, "-At", "-f", "-", input_data=sql.encode("utf-8")))
        finally:
            pg("dropdb", "--if-exists", "--force", "--maintenance-db=postgres", database)


def docker(args, database, token):
    compose = ["docker", "compose", "-f", str(SCRIPT.parent / "compose.yaml")]
    if args.project:
        compose += ["--project-name", args.project]
    prefix = compose + ["exec", "-T", "db"]

    def pg(tool, *parameters, input_data=None):
        # Fixed shell program; variable arguments are passed only as positional data.
        return execute(prefix + ["sh", "-c",
            'pg_tool=$1; shift; export PGCLIENTENCODING=UTF8; exec "$pg_tool" --username="$POSTGRES_USER" "$@"',
            "sh", tool, *parameters], input_data=input_data)

    with tempfile.TemporaryDirectory(prefix="cs-sqlsrc-") as tmp:
        source = Path(tmp)
        bootstrap, checks = prepare_source(source, args.flow)
        buffer = io.BytesIO()
        with tarfile.open(fileobj=buffer, mode="w") as archive:
            for path in sorted(source.rglob("*.sql")):
                archive.add(path, arcname=str(path.relative_to(source)), recursive=False)
        directory = "/tmp/chatsystem-schema-" + token
        copied = created = False
        try:
            execute(prefix + ["mkdir", "-m", "700", directory])
            copied = True
            execute(prefix + ["tar", "-xf", "-", "-C", directory], input_data=buffer.getvalue())
            pg("createdb", *TEST_DATABASE_OPTIONS, database)
            created = True
            psql = ["-X", "-q", "--set=ON_ERROR_STOP=1", "--set=flow=" + args.flow, "--dbname=" + database]
            pg("psql", *psql, "--file=" + directory + "/" + bootstrap)
            show_checks(pg("psql", *psql, "--file=" + directory + "/" + checks))
            check_concurrency(args.flow, lambda sql: pg(
                "psql", *psql, "-At", "-f", "-", input_data=sql.encode("utf-8")))
        finally:
            try:
                if created:
                    pg("dropdb", "--if-exists", "--force", database)
            finally:
                if copied:
                    execute(prefix + ["sh", "-c", 'rm -rf -- "$1"', "sh", directory])


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--flow", "--domain", dest="flow", choices=("all", *FLOWS), default="all")
    backend = parser.add_mutually_exclusive_group()
    backend.add_argument("--native", action="store_true", help="Use a private PostgreSQL 18 cluster on Linux/macOS")
    backend.add_argument("--local-server", action="store_true", help="Use an installed PostgreSQL 18 server on loopback")
    parser.add_argument("--pg-bin", type=Path, help="Native PostgreSQL 18 executable directory")
    parser.add_argument("--pg-port", type=int, help="Installed server port (default: 5432)")
    parser.add_argument("--pg-user", help="Installed server role with CREATEDB (default: postgres)")
    parser.add_argument("--project", help="Docker Compose project name when using a non-default project")
    args = parser.parse_args()
    if args.pg_bin and not (args.native or args.local_server):
        parser.error("--pg-bin requires --native or --local-server")
    if (args.pg_port is not None or args.pg_user is not None) and not args.local_server:
        parser.error("--pg-port and --pg-user require --local-server")
    if args.pg_port is not None and not 1 <= args.pg_port <= 65535:
        parser.error("--pg-port must be between 1 and 65535")
    if args.project and (args.native or args.local_server):
        parser.error("--project applies only to Docker")
    token = uuid.uuid4().hex[:12]
    database = "chatsystem_schema_test_" + args.flow.replace("-", "_") + "_" + token
    started = time.perf_counter()
    print(f"Flow={args.flow}; isolated database={database}", flush=True)
    try:
        if args.native:
            native(args, database)
        elif args.local_server:
            local_server(args, database)
        else:
            docker(args, database, token)
    except (subprocess.CalledProcessError, OSError, ValueError) as error:
        print(f"Database checks failed: {error}", file=sys.stderr)
        return 1
    print(f"Completed and cleaned up in {time.perf_counter() - started:.2f}s", flush=True)
    return 0


if __name__ == "__main__":
    sys.exit(main())
