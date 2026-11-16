#!/usr/bin/env python3
"""Compatibility entry point for the isolated native PostgreSQL runner."""
from pathlib import Path
import runpy
import sys

sys.argv.insert(1, "--native")
runpy.run_path(str(Path(__file__).with_name("test-db.py")), run_name="__main__")
