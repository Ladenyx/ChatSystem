#!/usr/bin/env python3
"""Run one member-owned DDL/Test pair in an isolated temporary database."""
import argparse
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


def execute(command, *, env=None, input_data=None):
    result = subprocess.run(command, input=input_data, stdout=subprocess.PIPE,
                            stderr=subprocess.PIPE, env=env)
    if result.returncode:
        if result.stdout:
            print(result.stdout.decode(errors="replace"), file=sys.stderr)
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
        raise ValueError("Use the Docker runner on Windows; --native is for Linux/macOS")
    bins = {}
    for name in ("initdb", "pg_ctl", "createdb", "psql"):
        found = str(args.pg_bin / name) if args.pg_bin else shutil.which(name)
        if not found or not Path(found).is_file():
            raise ValueError(f"Cannot find {name}; set --pg-bin to PostgreSQL 18/bin")
        bins[name] = found
    version = execute([bins["initdb"], "--version"]).stdout.decode().strip()
    if not re.search(r"PostgreSQL\)?\s+18\.", version):
        raise ValueError(f"PostgreSQL 18 required, found {version}")
    env = {key: value for key, value in os.environ.items() if not key.startswith("PG")}
    with tempfile.TemporaryDirectory(prefix="cs-pg18-", dir="/tmp") as tmp:
        root = Path(tmp)
        source = root / "source"
        bootstrap, checks = prepare_source(source, args.flow)
        data, sock, log = root / "data", root / "socket", root / "postgres.log"
        sock.mkdir()
        execute([bins["initdb"], "-D", str(data), "-U", "chatsystem_test_owner",
                 "--encoding=UTF8", "--no-locale", "--auth-local=trust", "--auth-host=scram-sha-256"], env=env)
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
        except subprocess.CalledProcessError:
            if log.exists():
                print(log.read_text(), file=sys.stderr)
            raise
        finally:
            if (data / "postmaster.pid").exists():
                execute([bins["pg_ctl"], "-D", str(data), "-m", "fast", "-w", "-t", "30", "stop"], env=env)


def docker(args, database, token):
    compose = ["docker", "compose", "-f", str(SCRIPT.parent / "compose.yaml")]
    if args.project:
        compose += ["--project-name", args.project]
    prefix = compose + ["exec", "-T", "db"]

    def pg(tool, *parameters):
        # Fixed shell program; variable arguments are passed only as positional data.
        return execute(prefix + ["sh", "-c",
            'pg_tool=$1; shift; exec "$pg_tool" --username="$POSTGRES_USER" "$@"',
            "sh", tool, *parameters])

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
            execute(prefix + ["sh", "-c", 'mkdir -m 700 "$1" && tar -xf - -C "$1"',
                              "sh", directory], input_data=buffer.getvalue())
            copied = True
            pg("createdb", "--template=template0", "--encoding=UTF8", database)
            created = True
            psql = ["-X", "-q", "--set=ON_ERROR_STOP=1", "--set=flow=" + args.flow, "--dbname=" + database]
            pg("psql", *psql, "--file=" + directory + "/" + bootstrap)
            show_checks(pg("psql", *psql, "--file=" + directory + "/" + checks))
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
    parser.add_argument("--native", action="store_true", help="Use a private PostgreSQL 18 cluster on Linux/macOS")
    parser.add_argument("--pg-bin", type=Path, help="Native PostgreSQL 18 executable directory")
    parser.add_argument("--project", help="Docker Compose project name when using a non-default project")
    args = parser.parse_args()
    if args.pg_bin and not args.native:
        parser.error("--pg-bin requires --native")
    if args.project and args.native:
        parser.error("--project applies only to Docker")
    token = uuid.uuid4().hex[:12]
    database = "chatsystem_schema_test_" + args.flow.replace("-", "_") + "_" + token
    started = time.perf_counter()
    print(f"Flow={args.flow}; isolated database={database}", flush=True)
    try:
        if args.native:
            native(args, database)
        else:
            docker(args, database, token)
    except (subprocess.CalledProcessError, OSError, ValueError) as error:
        print(f"Database checks failed: {error}", file=sys.stderr)
        return 1
    print(f"Completed and cleaned up in {time.perf_counter() - started:.2f}s", flush=True)
    return 0


if __name__ == "__main__":
    sys.exit(main())
