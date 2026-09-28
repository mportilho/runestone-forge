#!/usr/bin/env python3
"""Evaluate Etapa 12 JMH JSON artifacts against the versioned manifest."""

import argparse
import json
import math
import sys
from dataclasses import dataclass
from pathlib import Path


@dataclass(frozen=True)
class Measurement:
    benchmark: str
    params: dict[str, str]
    score: float
    lower: float
    upper: float
    unit: str


@dataclass(frozen=True)
class Verdict:
    gate: str
    status: str
    detail: str


def finite_number(value: object, description: str) -> float:
    if not isinstance(value, (int, float)) or isinstance(value, bool) or not math.isfinite(value):
        raise ValueError(f"{description} must be a finite number")
    return float(value)


def load_results(directory: Path, run: str) -> list[dict]:
    results: list[dict] = []
    for path in sorted(directory.glob("*.json")):
        with path.open(encoding="utf-8") as source:
            document = json.load(source)
        if not isinstance(document, list):
            raise ValueError(f"JMH result must be an array: {path}")
        for result in document:
            result["_stage12Run"] = run
            results.append(result)
    if not results:
        raise ValueError(f"no JMH JSON results found below {directory}")
    return results


def matches(result: dict, selector: dict) -> bool:
    if result.get("_stage12Run", "candidate") != selector.get("run", "candidate"):
        return False
    if result.get("benchmark") != selector["benchmark"]:
        return False
    actual_params = result.get("params", {})
    return all(str(actual_params.get(name)) == str(value) for name, value in selector.get("params", {}).items())


def select(results: list[dict], selector: dict, gate: str) -> list[dict]:
    selected = [result for result in results if matches(result, selector)]
    if not selected:
        raise ValueError(f"gate {gate} selector matched no result: {selector}")
    return selected


def measurement(result: dict, metric: str) -> Measurement:
    data = result.get("primaryMetric") if metric == "score" else result.get("secondaryMetrics", {}).get(metric)
    if not isinstance(data, dict):
        raise ValueError(f"{result.get('benchmark')} does not contain metric {metric}")
    score = finite_number(data.get("score"), f"{metric} score")
    confidence = data.get("scoreConfidence")
    if isinstance(confidence, list) and len(confidence) == 2:
        lower = finite_number(confidence[0], f"{metric} lower confidence bound")
        upper = finite_number(confidence[1], f"{metric} upper confidence bound")
    else:
        lower = score
        upper = score
    return Measurement(
        str(result.get("benchmark")),
        {str(key): str(value) for key, value in result.get("params", {}).items()},
        score,
        lower,
        upper,
        str(data.get("scoreUnit", "")),
    )


def one(results: list[dict], selector: dict, metric: str, gate: str) -> Measurement:
    selected = select(results, selector, gate)
    if len(selected) != 1:
        raise ValueError(f"gate {gate} selector matched {len(selected)} results; expected one")
    return measurement(selected[0], metric)


def ratio(numerator: float, denominator: float, gate: str) -> float:
    if denominator <= 0:
        raise ValueError(f"gate {gate} cannot divide by non-positive metric {denominator}")
    return numerator / denominator


def evaluate_comparison(gate: dict, results: list[dict]) -> Verdict:
    gate_id = gate["id"]
    metric = gate["metric"]
    candidate = one(results, gate["candidate"], metric, gate_id)
    control = one(results, gate["control"], metric, gate_id)
    limit = float(gate["limit"])
    gate_type = gate["type"]

    if gate_type == "max-additional":
        observed = candidate.score - control.score
        status = "PASS" if observed <= limit else "FAIL"
        detail = f"additional={observed:.6g} {candidate.unit}; limit={limit:.6g}"
    elif gate_type in {"max-regression", "max-ratio"}:
        maximum = 1.0 + limit if gate_type == "max-regression" else limit
        observed = ratio(candidate.score, control.score, gate_id)
        if observed <= maximum:
            status = "PASS"
        elif ratio(candidate.lower, control.upper, gate_id) <= maximum:
            status = "INCONCLUSIVE"
        else:
            status = "FAIL"
        detail = f"candidate/control={observed:.6g}; maximum={maximum:.6g}"
    elif gate_type == "min-ratio":
        observed = ratio(control.score, candidate.score, gate_id)
        if observed >= limit:
            status = "PASS"
        elif ratio(control.upper, candidate.lower, gate_id) >= limit:
            status = "INCONCLUSIVE"
        else:
            status = "FAIL"
        detail = f"control/candidate={observed:.6g}; minimum={limit:.6g}"
    else:
        raise ValueError(f"unsupported comparison gate type: {gate_type}")
    return Verdict(gate_id, status, detail)


def evaluate_additional_slope(gate: dict, results: list[dict]) -> Verdict:
    gate_id = gate["id"]
    parameter = gate["parameter"]
    candidate_results = select(results, gate["candidate"], gate_id)
    control_results = select(results, gate["control"], gate_id)

    def points(selected: list[dict]) -> dict[float, Measurement]:
        values: dict[float, Measurement] = {}
        for result in selected:
            params = result.get("params", {})
            if parameter not in params:
                raise ValueError(f"gate {gate_id} result has no parameter {parameter}")
            parameter_value = finite_number(float(params[parameter]), parameter)
            if parameter_value in values:
                raise ValueError(f"gate {gate_id} has duplicate parameter value {parameter_value}")
            values[parameter_value] = measurement(result, gate["metric"])
        return values

    candidates = points(candidate_results)
    controls = points(control_results)
    if candidates.keys() != controls.keys() or len(candidates) < 2:
        raise ValueError(f"gate {gate_id} requires matching candidate/control parameter series")
    ordered = sorted(candidates)
    first = ordered[0]
    last = ordered[-1]
    first_additional = candidates[first].score - controls[first].score
    last_additional = candidates[last].score - controls[last].score
    observed = (last_additional - first_additional) / (last - first)
    limit = float(gate["limit"])
    status = "PASS" if observed <= limit else "FAIL"
    detail = f"additional allocation slope={observed:.6g} B/{parameter}; maximum={limit:.6g}"
    return Verdict(gate_id, status, detail)


def evaluate_absolute(gate: dict, results: list[dict]) -> Verdict:
    value = one(results, gate["selector"], gate["metric"], gate["id"])
    limit = float(gate["limit"])
    status = "PASS" if value.score <= limit else "FAIL"
    return Verdict(gate["id"], status, f"observed={value.score:.6g} {value.unit}; maximum={limit:.6g}")


def evaluate_growth(gate: dict, results: list[dict]) -> Verdict:
    selected = select(results, gate["selector"], gate["id"])
    parameter = gate["parameter"]
    points: list[tuple[float, Measurement]] = []
    for result in selected:
        params = result.get("params", {})
        if parameter not in params:
            raise ValueError(f"gate {gate['id']} result has no parameter {parameter}")
        points.append((finite_number(float(params[parameter]), parameter), measurement(result, gate["metric"])))
    points.sort(key=lambda point: point[0])
    if len(points) < 2 or any(current[0] <= previous[0] for previous, current in zip(points, points[1:])):
        raise ValueError(f"gate {gate['id']} requires at least two distinct increasing parameter values")
    limit = float(gate["limit"])
    worst = 0.0
    for previous, current in zip(points, points[1:]):
        input_ratio = current[0] / previous[0]
        normalized_growth = ratio(current[1].score, previous[1].score, gate["id"]) / input_ratio
        worst = max(worst, normalized_growth)
    status = "PASS" if worst <= limit else "FAIL"
    return Verdict(gate["id"], status, f"worst normalized growth={worst:.6g}; maximum={limit:.6g}")


def evaluate(manifest: dict, results: list[dict]) -> list[Verdict]:
    expected = {benchmark for family in manifest["families"] for benchmark in family["benchmarks"]}
    actual = {
        str(result.get("benchmark"))
        for result in results
        if result.get("_stage12Run", "candidate") == "candidate"
    }
    verdicts = [
        Verdict("manifest-coverage", "PASS" if not expected - actual else "FAIL",
                "all benchmarks present" if not expected - actual else "missing: " + ", ".join(sorted(expected - actual)))
    ]
    for gate in manifest["gates"]:
        if gate["type"] in {"max-additional", "max-regression", "max-ratio", "min-ratio"}:
            verdicts.append(evaluate_comparison(gate, results))
        elif gate["type"] == "max-additional-slope":
            verdicts.append(evaluate_additional_slope(gate, results))
        elif gate["type"] == "absolute-max":
            verdicts.append(evaluate_absolute(gate, results))
        elif gate["type"] == "linear-growth":
            verdicts.append(evaluate_growth(gate, results))
        else:
            raise ValueError(f"unsupported gate type: {gate['type']}")
    return verdicts


def write_report(path: Path, verdicts: list[Verdict]) -> None:
    overall = "PASS" if all(verdict.status == "PASS" for verdict in verdicts) else "FAIL"
    payload = {
        "overall": overall,
        "gates": [verdict.__dict__ for verdict in verdicts],
    }
    path.write_text(json.dumps(payload, indent=2) + "\n", encoding="utf-8")


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("manifest", type=Path)
    parser.add_argument("results", type=Path)
    parser.add_argument("report", type=Path)
    parser.add_argument("--baseline", type=Path)
    args = parser.parse_args()
    try:
        with args.manifest.open(encoding="utf-8") as source:
            manifest = json.load(source)
        results = load_results(args.results, "candidate")
        if args.baseline is not None:
            results.extend(load_results(args.baseline, "baseline"))
        verdicts = evaluate(manifest, results)
        write_report(args.report, verdicts)
        for verdict in verdicts:
            print(f"{verdict.status:12} {verdict.gate}: {verdict.detail}")
        return 0 if all(verdict.status == "PASS" for verdict in verdicts) else 1
    except (OSError, ValueError, json.JSONDecodeError) as error:
        print(f"stage12 evaluation error: {error}", file=sys.stderr)
        return 2


if __name__ == "__main__":
    raise SystemExit(main())
