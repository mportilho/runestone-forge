#!/usr/bin/env python3

import importlib.util
import unittest
from pathlib import Path


SCRIPT = Path(__file__).with_name("stage12-evaluate.py")
SPEC = importlib.util.spec_from_file_location("stage12_evaluate", SCRIPT)
MODULE = importlib.util.module_from_spec(SPEC)
assert SPEC.loader is not None
SPEC.loader.exec_module(MODULE)


def result(benchmark, score, error=0.0, allocation=0.0, params=None):
    return {
        "benchmark": benchmark,
        "params": params or {},
        "primaryMetric": {
            "score": score,
            "scoreConfidence": [score - error, score + error],
            "scoreUnit": "ns/op",
        },
        "secondaryMetrics": {
            "gc.alloc.rate.norm": {
                "score": allocation,
                "scoreConfidence": [allocation, allocation],
                "scoreUnit": "B/op",
            }
        },
    }


class Stage12EvaluateTest(unittest.TestCase):

    def test_reports_pass_for_results_within_comparison_and_growth_limits(self):
        manifest = {
            "families": [{"benchmarks": ["control", "candidate", "growth"]}],
            "gates": [
                {
                    "id": "latency",
                    "type": "max-regression",
                    "metric": "score",
                    "candidate": {"benchmark": "candidate"},
                    "control": {"benchmark": "control"},
                    "limit": 0.05,
                },
                {
                    "id": "allocation",
                    "type": "max-additional",
                    "metric": "gc.alloc.rate.norm",
                    "candidate": {"benchmark": "candidate"},
                    "control": {"benchmark": "control"},
                    "limit": 0.01,
                },
                {
                    "id": "growth",
                    "type": "linear-growth",
                    "metric": "score",
                    "selector": {"benchmark": "growth"},
                    "parameter": "size",
                    "limit": 1.25,
                },
            ],
        }
        results = [
            result("control", 100.0, allocation=24.0),
            result("candidate", 104.0, allocation=24.005),
            result("growth", 10.0, params={"size": "10"}),
            result("growth", 82.0, params={"size": "80"}),
        ]

        verdicts = MODULE.evaluate(manifest, results)

        self.assertEqual(["PASS", "PASS", "PASS", "PASS"], [verdict.status for verdict in verdicts])

    def test_reports_inconclusive_when_point_regresses_but_error_bands_overlap(self):
        gate = {
            "id": "latency",
            "type": "max-regression",
            "metric": "score",
            "candidate": {"benchmark": "candidate"},
            "control": {"benchmark": "control"},
            "limit": 0.01,
        }
        results = [result("control", 100.0, error=3.0), result("candidate", 103.0, error=3.0)]

        verdict = MODULE.evaluate_comparison(gate, results)

        self.assertEqual("INCONCLUSIVE", verdict.status)

    def test_compares_candidate_with_an_explicit_baseline_run(self):
        gate = {
            "id": "latency",
            "type": "max-regression",
            "metric": "score",
            "candidate": {"benchmark": "scalar", "params": {"mode": "SAFE"}},
            "control": {"run": "baseline", "benchmark": "scalar", "params": {"mode": "SAFE"}},
            "limit": 0.01,
        }
        baseline = result("scalar", 100.0, params={"mode": "SAFE"})
        baseline["_stage12Run"] = "baseline"
        candidate = result("scalar", 100.5, params={"mode": "SAFE"})
        candidate["_stage12Run"] = "candidate"

        verdict = MODULE.evaluate_comparison(gate, [baseline, candidate])

        self.assertEqual("PASS", verdict.status)

    def test_reports_missing_manifest_benchmark(self):
        manifest = {"families": [{"benchmarks": ["present", "missing"]}], "gates": []}

        verdicts = MODULE.evaluate(manifest, [result("present", 1.0)])

        self.assertEqual("FAIL", verdicts[0].status)
        self.assertIn("missing", verdicts[0].detail)

    def test_baseline_result_does_not_satisfy_candidate_manifest_coverage(self):
        manifest = {"families": [{"benchmarks": ["candidate"]}], "gates": []}
        baseline = result("candidate", 1.0)
        baseline["_stage12Run"] = "baseline"

        verdicts = MODULE.evaluate(manifest, [baseline])

        self.assertEqual("FAIL", verdicts[0].status)
        self.assertIn("candidate", verdicts[0].detail)

    def test_allocation_slope_ignores_constant_safe_scope_cost(self):
        gate = {
            "id": "debit-allocation",
            "type": "max-additional-slope",
            "metric": "gc.alloc.rate.norm",
            "candidate": {"benchmark": "collection", "params": {"mode": "SAFE"}},
            "control": {"benchmark": "collection", "params": {"mode": "TRUSTED"}},
            "parameter": "size",
            "limit": 0.01,
        }
        results = [
            result("collection", 1.0, allocation=400.0, params={"mode": "TRUSTED", "size": "8"}),
            result("collection", 1.0, allocation=440.0, params={"mode": "SAFE", "size": "8"}),
            result("collection", 1.0, allocation=4000.0, params={"mode": "TRUSTED", "size": "128"}),
            result("collection", 1.0, allocation=4040.0, params={"mode": "SAFE", "size": "128"}),
        ]

        verdict = MODULE.evaluate_additional_slope(gate, results)

        self.assertEqual("PASS", verdict.status)


if __name__ == "__main__":
    unittest.main()
