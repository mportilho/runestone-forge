#!/usr/bin/env python3
"""Validate and query the machine-readable Etapa 12 JMH manifest."""

import argparse
import json
import re
import sys
from pathlib import Path


def load_manifest(path: Path) -> dict:
    with path.open(encoding="utf-8") as source:
        manifest = json.load(source)
    if manifest.get("schemaVersion") != 1:
        raise ValueError("unsupported or missing schemaVersion")
    protocol = manifest.get("protocol")
    families = manifest.get("families")
    if not isinstance(protocol, dict) or not isinstance(families, list) or not families:
        raise ValueError("manifest requires protocol and at least one family")
    positive_integers = ("jdkMajor", "forks", "warmupIterations", "measurementIterations", "threads")
    for name in positive_integers:
        value = protocol.get(name)
        if type(value) is not int or value <= 0:
            raise ValueError(f"protocol {name} must be a positive integer")
    for name in ("warmupTime", "measurementTime"):
        value = protocol.get(name)
        if not isinstance(value, str) or not re.fullmatch(r"[1-9][0-9]*(?:ns|us|ms|s|m)", value):
            raise ValueError(f"protocol {name} must be a positive JMH duration")
    if not isinstance(protocol.get("heap"), str) or not re.fullmatch(r"[1-9][0-9]*[kKmMgG]", protocol["heap"]):
        raise ValueError("protocol heap must be a positive JVM memory size")
    for name in ("jdkVendor", "profiler"):
        if not isinstance(protocol.get(name), str) or not protocol[name]:
            raise ValueError(f"protocol {name} must be a non-empty string")
    seen_ids: set[str] = set()
    seen_benchmarks: set[str] = set()
    for family in families:
        family_id = family.get("id")
        classification = family.get("classification")
        benchmarks = family.get("benchmarks")
        metrics = family.get("metrics")
        if not isinstance(family_id, str) or not re.fullmatch(r"[a-z][a-z0-9-]*", family_id):
            raise ValueError(f"invalid family id: {family_id!r}")
        if family_id in seen_ids:
            raise ValueError(f"duplicate family id: {family_id}")
        if classification not in {"binding", "characterization"}:
            raise ValueError(f"invalid classification for {family_id}: {classification!r}")
        if not isinstance(benchmarks, list) or not benchmarks:
            raise ValueError(f"family {family_id} has no benchmarks")
        if not isinstance(metrics, list) or not metrics or not all(isinstance(item, str) and item for item in metrics):
            raise ValueError(f"family {family_id} has invalid metrics")
        seen_ids.add(family_id)
        for benchmark in benchmarks:
            if not isinstance(benchmark, str) or benchmark in seen_benchmarks:
                raise ValueError(f"invalid or duplicate benchmark: {benchmark!r}")
            seen_benchmarks.add(benchmark)
    return manifest


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("manifest", type=Path)
    parser.add_argument("command", choices=("validate", "protocol", "families", "regex", "arguments"))
    parser.add_argument("argument", nargs="?")
    args = parser.parse_args()
    try:
        manifest = load_manifest(args.manifest)
        if args.command == "validate":
            return 0
        if args.command == "protocol":
            if args.argument not in manifest["protocol"]:
                raise ValueError(f"unknown protocol property: {args.argument!r}")
            print(manifest["protocol"][args.argument])
            return 0
        if args.command == "families":
            for family in manifest["families"]:
                print(family["id"])
            return 0
        family = next((item for item in manifest["families"] if item["id"] == args.argument), None)
        if family is None:
            raise ValueError(f"unknown family: {args.argument!r}")
        if args.command == "arguments":
            for argument in family.get("jmhArguments", []):
                if not isinstance(argument, str) or "\n" in argument:
                    raise ValueError(f"invalid JMH argument for {family['id']}: {argument!r}")
                print(argument)
            return 0
        print("^(?:" + "|".join(re.escape(item) for item in family["benchmarks"]) + ")$")
        return 0
    except (OSError, ValueError, json.JSONDecodeError) as error:
        print(f"stage12 manifest error: {error}", file=sys.stderr)
        return 2


if __name__ == "__main__":
    raise SystemExit(main())
