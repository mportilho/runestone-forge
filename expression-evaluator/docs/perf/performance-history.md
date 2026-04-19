# Performance History — expression-evaluator

## PERF-001: Collection Navigation — vector higher order functions (bf4f453)

**Date:** 2026-04-19

**Scenario:** Commit `bf4f453` ("Implementação vector higher order function") added new vector HOF capabilities. Baseline: `8a0f6da` (parent of bf4f453). Measured: HEAD `af5d656`.
**Hypothesis:** New HOF dispatch path may add overhead to existing collection navigation code paths.

| Benchmark           | Before (ns/op) | After (ns/op) | Δ (%)  | B/op (B→A) |
|---------------------|---------------:|--------------:|-------:|-----------:|
| indexAccess         |   72.2 ±2.0    |   70.8 ±1.7   | +1.90% |   64 → 64  |
| customFunctionCount |  305.9 ±7.9    |  307.7 ±10.0  | -0.60% |  296 → 296 |
| mapFilterCount      |  731.8 ±37.9   |  725.6 ±43.8  | +0.85% |  352 → 352 |
| listFilterCount     |  585.0 ±30.6   |  602.9 ±33.7  | -3.06% |  216 → 216 |
| mapValuesCount      |   72.7 ±2.7    |   76.3 ±3.9   | -5.03% |   64 → 64  |
| deepScanCount       | 1,265.2 ±78.3  | 1,367.9 ±94.3 | -8.11% |  528 → 528 |

**Average Δ: -2.34%**

**Decision:** ACCEPT (functional addition)
**Reason:** This commit introduces new functionality (vector HOF), not a performance optimization. The regressions in `deepScanCount` (-8.11%), `mapValuesCount` (-5.03%), and `listFilterCount` (-3.06%) are moderate overhead costs for the added dispatch capability. Allocations (B/op) are unchanged across all benchmarks — no new object creation was introduced. The cost is acceptable given the feature benefit.
**Notes:** `deepScanCount` warrants monitoring; if this path becomes a bottleneck, investigate HOF dispatch cost in the deep-scan walk.
