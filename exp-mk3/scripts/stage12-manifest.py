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
    baseline_commit = protocol.get("traversalBaselineCommit")
    if not isinstance(baseline_commit, str) or not re.fullmatch(r"[0-9a-f]{40}", baseline_commit):
        raise ValueError("protocol traversalBaselineCommit must be a full Git commit")
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
    gates = manifest.get("gates")
    if not isinstance(gates, list) or not gates:
        raise ValueError("manifest requires at least one automatic gate")
    seen_gate_ids: set[str] = set()
    comparison_types = {"max-additional", "max-additional-slope", "max-regression", "max-ratio", "min-ratio"}
    for gate in gates:
        gate_id = gate.get("id")
        gate_type = gate.get("type")
        if not isinstance(gate_id, str) or not re.fullmatch(r"[a-z][a-z0-9-]*", gate_id):
            raise ValueError(f"invalid gate id: {gate_id!r}")
        if gate_id in seen_gate_ids:
            raise ValueError(f"duplicate gate id: {gate_id}")
        seen_gate_ids.add(gate_id)
        if gate_type not in comparison_types | {"absolute-max", "linear-growth"}:
            raise ValueError(f"invalid gate type for {gate_id}: {gate_type!r}")
        if gate.get("metric") not in {"score", "gc.alloc.rate.norm"}:
            raise ValueError(f"invalid metric for {gate_id}: {gate.get('metric')!r}")
        limit = gate.get("limit")
        if not isinstance(limit, (int, float)) or isinstance(limit, bool) or limit < 0:
            raise ValueError(f"gate {gate_id} limit must be a non-negative number")
        selectors = ("candidate", "control") if gate_type in comparison_types else ("selector",)
        for selector_name in selectors:
            selector = gate.get(selector_name)
            if not isinstance(selector, dict) or selector.get("benchmark") not in seen_benchmarks:
                raise ValueError(f"gate {gate_id} has invalid {selector_name} selector")
            params = selector.get("params", {})
            if not isinstance(params, dict) or not all(isinstance(name, str) for name in params):
                raise ValueError(f"gate {gate_id} has invalid {selector_name} parameters")
            if selector.get("run", "candidate") not in {"candidate", "baseline"}:
                raise ValueError(f"gate {gate_id} has invalid {selector_name} run")
        if gate_type in {"linear-growth", "max-additional-slope"} and not isinstance(gate.get("parameter"), str):
            raise ValueError(f"gate {gate_id} requires a parameter")
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
