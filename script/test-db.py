#!/usr/bin/env python3
"""Run one SQL domain in a new isolated database; leave the development volume alone."""
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
DOMAINS = ("all", "messaging", "e2ee", "spam")


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
                    "-v", "ON_ERROR_STOP=1", "-v", "domain=" + args.domain]
            bootstrap = SCRIPT / "schema.sql" if args.domain == "all" else SCRIPT / "common" / "bootstrap.sql"
            execute(psql + ["-f", str(bootstrap)], env=env)
            show_checks(execute(psql + ["-f", str(SCRIPT / "tests" / "run.sql")], env=env))
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
        # Fixed shell program; all variable arguments are passed as positional data.
        return execute(prefix + ["sh", "-c",
            'pg_tool=$1; shift; exec "$pg_tool" --username="$POSTGRES_USER" "$@"',
            "sh", tool, *parameters])

    # Copy this checkout's SQL, not the source mount of another running worktree.
    # Each invocation uses its own directory, so parallel workers cannot overwrite it.
    buffer = io.BytesIO()
    with tarfile.open(fileobj=buffer, mode="w") as archive:
        for path in sorted(SCRIPT.rglob("*.sql")):
            if path.is_symlink():
                raise ValueError(f"SQL symlinks are not supported: {path}")
            archive.add(path, arcname=str(path.relative_to(SCRIPT)), recursive=False)
    directory = "/tmp/chatsystem-schema-" + token
    copied = created = False
    try:
        execute(prefix + ["sh", "-c", 'mkdir -m 700 "$1" && tar -xf - -C "$1"',
                          "sh", directory], input_data=buffer.getvalue())
        copied = True
        pg("createdb", "--template=template0", "--encoding=UTF8", database)
        created = True
        psql = ["-X", "-q", "--set=ON_ERROR_STOP=1", "--set=domain=" + args.domain, "--dbname=" + database]
        bootstrap = "/schema.sql" if args.domain == "all" else "/common/bootstrap.sql"
        pg("psql", *psql, "--file=" + directory + bootstrap)
        show_checks(pg("psql", *psql, "--file=" + directory + "/tests/run.sql"))
    finally:
        try:
            if created:
                pg("dropdb", "--if-exists", "--force", database)
        finally:
            if copied:
                execute(prefix + ["sh", "-c", 'rm -rf -- "$1"', "sh", directory])


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--domain", choices=DOMAINS, default="all")
    parser.add_argument("--native", action="store_true", help="Use a private PostgreSQL 18 cluster on Linux/macOS")
    parser.add_argument("--pg-bin", type=Path, help="Native PostgreSQL 18 executable directory")
    parser.add_argument("--project", help="Docker Compose project name when using a non-default project")
    args = parser.parse_args()
    if args.pg_bin and not args.native:
        parser.error("--pg-bin requires --native")
    if args.project and args.native:
        parser.error("--project applies only to Docker")
    token = uuid.uuid4().hex[:12]
    database = "chatsystem_schema_test_" + args.domain + "_" + token
    started = time.perf_counter()
    print(f"Domain={args.domain}; isolated database={database}", flush=True)
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
