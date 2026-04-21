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

---

## PERF-002: Baseline — `..map(@ -> expr)` vector transform evaluation cost

**Date:** 2026-04-21

**Scenario:** First JMH measurement of the `..map(@ -> expr)` feature (branch `refac-springboot-4`).
Six scenarios cover the full feature surface: wildcard projection (baseline), map steps over lists
(property extraction, computed field, chained maps) and over `Map` objects.
All expressions are pre-compiled; benchmarks exercise only the evaluation phase.

**Hypothesis:** Baseline capture — no prior number exists. Documents the steady-state cost of each
scenario for future optimisation comparisons.

**Machine:** OpenJDK 25.0.2 / Linux x86-64, -Xms1g -Xmx1g  
**JMH config:** 3 forks × 5 warmup + 10 measurement iterations × 500 ms each; `AverageTime / ns`

| Benchmark             | Score (ns/op) | ±Error | B/op  | Notes                                            |
|-----------------------|--------------:|-------:|------:|--------------------------------------------------|
| wildcardProjectionSum |        479.59 |  13.19 |   872 | Baseline: `books[*].price..sum()`                |
| mapExtractPropertySum |        713.65 |  16.58 |   968 | `..map(@ -> @.price)..sum()` — +49% vs baseline  |
| mapEntryValuesSum     |        712.69 |  37.33 |   864 | `mapObj..map(@ -> @.value*2)..sum()` — Map path  |
| mapChainedSum         |        923.76 |  44.98 |   912 | Two chained map steps — +93% vs baseline         |
| mapComputedFieldSum   |      1 062.48 |  21.47 | 1 224 | `..map(@ -> @.price * @.qty)..sum()` — +122%     |
| mapComputedFieldAvg   |      1 833.86 | 105.19 | 2 214 | `..map(@ -> @.price * @.qty)..avg()` — +283%     |

**Decision:** BASELINE (first measurement — no change applied)

**Notes:**
- `wildcardProjectionSum` is the cheapest path (no transform lambda); serves as the cost floor.
- `mapExtractPropertySum` and `mapEntryValuesSum` cost ~713 ns/op, showing the per-element lambda
  adds ~234 ns over wildcard projection for a 4-element list (~59 ns/element).
- `mapComputedFieldAvg` is ~2.7× slower than `mapComputedFieldSum` despite the same map step;
  the extra ~771 ns and ~990 B come from BigDecimal division inside `avg`. If this is a hot path,
  computing sum + count separately and dividing once is a candidate optimisation.
- Allocations are dominated by `ArrayList` creation in `applyMapTransform` and BigDecimal
  temporaries; no unexpected retention observed.

---

## PERF-003: Collection Navigation — current state after `..map()` addition

**Date:** 2026-04-21

**Scenario:** Re-measurement of `CollectionNavigationBenchmark` (same as PERF-001) after adding the
`..map(@ -> expr)` feature. Compared against the PERF-001 "after" numbers (HOF commit, 2026-04-19).
Goal: detect any regression introduced by the new `ExecutableVectorMap` dispatch arm in
`evaluatePropertyChain()`.

**Hypothesis:** The added switch case for `ExecutableVectorMap` may slow down the common path even
when the map step is never taken, due to JIT re-optimisation of the switch dispatch.

**Machine:** OpenJDK 25.0.2 / Linux x86-64, -Xms1g -Xmx1g  
**JMH config:** 3 forks × 5 warmup + 10 measurement iterations × 500 ms each; `AverageTime / ns`

| Benchmark           | PERF-001 (ns/op) | Current (ns/op) | Δ (%)   | B/op (unchanged) |
|---------------------|-----------------:|----------------:|--------:|-----------------:|
| indexAccess         |             70.8 |           78.54 |  −10.9% |               64 |
| mapValuesCount      |             76.3 |           82.60 |   −8.3% |               64 |
| customFunctionCount |            307.7 |          341.23 |  −10.9% |              296 |
| listFilterCount     |            602.9 |          694.45 |  −15.2% |              216 |
| mapFilterCount      |            725.6 |          816.95 |  −12.6% |              352 |
| deepScanCount       |          1 367.9 |        1 622.50 |  −18.6% |              528 |

**Decision:** MONITOR

**Reason:** All regressions are 8–19% on paths that do not execute the new `..map()` arm at all.
B/op is unchanged across all benchmarks, ruling out allocation churn. Two explanations are equally
plausible: (1) the additional `case ExecutableVectorMap` in the sealed-interface switch reshapes JIT
inlining or de-optimises the dispatch for all arms; (2) normal day-to-day JMH variance (different
thermal/frequency state across two separate runs two days apart). `deepScanCount` shows high error
(±183 ns, ~11%) which further limits confidence. Before treating this as a confirmed regression,
re-run both benchmarks in the same session on the same commit to isolate JVM variance from code
change effect.

**Notes:** If confirmed, a targeted mitigation is to extract the `..map()` arm into a separate
helper invoked from a hot-guard check before the main switch, keeping the common-case dispatch
slim.
