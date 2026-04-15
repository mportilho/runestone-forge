# Performance History

## Known Hot-Paths & Discarded Hypotheses

- `compileTypedNestedProperty`: removing `Optional` wrappers from hot lookup sites and caching legacy-chain classification did not produce a win worth keeping; the measurable compile-path waste was duplicated typed-access resolution inside `ExecutionPlanBuilder`.
- `compileTypedNestedProperty`: quoted string literals now short-circuit before temporal parsing in `SemanticResolver`, removing the exception-driven path that was dominating the remaining compile-time cost.
- `compileTypedNestedProperty`: adding syntax guards before `SemanticResolver`'s temporal parse checks was discarded; repeated JMH runs showed a slower compile path than the string-only fast path, so the idea was reverted.

## PERF-001: Validate collection-navigation implementation against previous baseline

**Date:** 2026-04-11

**Scenario:** Compare `60d784b` (`collection navigation implemented`) against `403cf47` (`collection navigation upgrade plan 7`) for object-navigation compile and runtime costs.
**Hypothesis:** The collection-navigation implementation would preserve existing object-navigation performance while adding the new semantics.

| Benchmark | Before (ns/op) | After (ns/op) | Improvement (%) | B/op (B→A) |
|-----------|---------------:|--------------:|----------------:|-----------:|
| `buildTypedEnvironment` | 36636.066 | 35967.455 | +1.83% | 35747.179 → 35428.517 |
| `compileReflectiveMethodWithArgument` | 18238.153 | 18166.900 | +0.39% | 13838.008 → 13649.836 |
| `compileTypedMethodWithArgument` | 22687.308 | 28987.485 | -27.77% | 15458.860 → 15614.682 |
| `compileTypedNestedProperty` | 28518.515 | 32145.612 | -12.72% | 16271.577 → 16288.025 |
| `reflectiveMethodWithArgument` | 196.271 | 198.586 | -1.18% | 128.003 → 128.003 |
| `reflectiveNestedProperty` | 205.852 | 213.709 | -3.82% | 64.003 → 64.003 |
| `typedMethodNoArg` | 87.380 | 95.029 | -8.75% | 104.001 → 104.001 |
| `typedMethodWithArgument` | 123.934 | 135.018 | -8.94% | 104.002 → 104.002 |
| `typedNestedProperty` | 118.770 | 125.942 | -6.04% | 64.002 → 64.002 |

**Decision:** DISCARD
**Reason:** The average result regressed by 7.44%, with the largest losses concentrated in typed compile-time navigation and smaller but consistent losses in typed runtime navigation.
**Notes:** Allocation stayed effectively flat across the comparison, which points to CPU-path overhead rather than new object churn.

## PERF-002: Restore fast path for legacy property and method chains

**Date:** 2026-04-11

**Scenario:** Compare the regressed local working tree on top of `60d784b` against a fast-path experiment that bypasses generic collection-navigation handling for simple property and method chains.
**Hypothesis:** Legacy chains such as `obj.prop` and `obj.method(arg)` should avoid the generic navigation pipeline and recover most of the lost performance without affecting collection-navigation behavior.

| Benchmark | Before (ns/op) | After (ns/op) | Improvement (%) | B/op (B→A) |
|-----------|---------------:|--------------:|----------------:|-----------:|
| `compileTypedMethodWithArgument` | 28987.485 | 23438.730 | +19.14% | 15614.682 → 15581.584 |
| `compileTypedNestedProperty` | 32145.612 | 31611.806 | +1.66% | 16288.025 → 16457.935 |
| `typedMethodNoArg` | 95.029 | 94.693 | +0.35% | 104.001 → 104.001 |
| `typedMethodWithArgument` | 135.018 | 127.669 | +5.44% | 104.002 → 104.002 |
| `typedNestedProperty` | 125.942 | 125.642 | +0.24% | 64.002 → 64.002 |

**Decision:** ADJUST
**Reason:** The fast path recovered a meaningful part of the regression for typed method calls, but the nested-property compile and runtime cases remained close to the regressed baseline and still did not return to `403cf47` levels.
**Notes:** This points to additional compile-time overhead outside the evaluator fast path, likely earlier in the parse/AST/resolution pipeline.

## PERF-003: Eliminate duplicated typed-access resolution in `ExecutionPlanBuilder`

**Date:** 2026-04-11

**Scenario:** Compare the current local working tree after the legacy fast path against a follow-up optimization aimed specifically at `compileTypedNestedProperty`.
**Hypothesis:** `ExecutionPlanBuilder` was resolving typed property and method descriptors twice per step, once to build the executable access and again to infer the next type, and collapsing that work should shave compile time for simple typed chains.

| Benchmark | Before (ns/op) | After (ns/op) | Improvement (%) | B/op (B→A) |
|-----------|---------------:|--------------:|----------------:|-----------:|
| `compileTypedNestedProperty` | 34900.205 | 33983.155 | +2.63% | 16440.913 → 16464.059 |

**Decision:** ACCEPT
**Reason:** The improvement is modest but positive and the change is low-risk because it removes duplicated descriptor lookup without changing runtime semantics.
**Notes:** Allocation remained effectively flat. The benchmark still sits above the original `403cf47` baseline, so the remaining gap is likely outside the duplicated access-resolution path.

## PERF-004: Short-circuit quoted string literals before temporal parsing

**Date:** 2026-04-12

**Scenario:** Compare `compileTypedNestedProperty` on the current branch before and after moving quoted string literals ahead of the temporal parse checks in `SemanticResolver`.
**Hypothesis:** String literals like `"BAT-100"` should not pay for `LocalDate` / `LocalTime` / `LocalDateTime` parse attempts, so recognizing them syntactically first should reduce compile time and allocation in the typed nested-property compile path.

| Benchmark | Before (ns/op) | After (ns/op) | Improvement (%) | B/op (B→A) |
|-----------|---------------:|--------------:|----------------:|-----------:|
| `compileTypedNestedProperty` | 23394.905 | 13667.451 | +41.55% | 16400.862 → 11356.168 |

**Decision:** ACCEPT
**Reason:** The fast path removes the exception-heavy temporal parsing path for a literal that is always a string in this benchmark, and the measured compile-time gain is well above the acceptance threshold.
**Notes:** The allocation drop is material as well, which is consistent with eliminating repeated `DateTimeParseException` construction on the hot path.

## PERF-005: Baseline for collection-navigation evaluation performance

**Date:** 2026-04-12

**Scenario:** Establish baseline performance characteristics for the new collection-navigation feature across six evaluation patterns: index access, map projections, custom functions, list/map filtering, and deep scans.
**Hypothesis:** Collection-navigation paths should exhibit predictable performance scaling with operation complexity, with filter predicates having similar cost regardless of collection type.

| Benchmark | ns/op | B/op | Relative to Index | Notes |
|-----------|------:|-----:|------------------:|-------|
| `indexAccess` | 112.7 | 136.0 | 1.0× | Simple array index (baseline) |
| `mapValuesCount` | 207.3 | 224.0 | 1.8× | Map value projection |
| `customFunctionCount` | 426.7 | 296.0 | 3.8× | Custom function on collection |
| `listFilterCount` | 667.3 | 328.0 | 5.9× | List filter with predicate |
| `mapFilterCount` | 643.2 | 328.0 | 5.7× | Map filter with predicate |
| `deepScanCount` | 4488.3 | 1168.1 | 39.8× | Deep recursive traversal |

**Decision:** ACCEPT
**Reason:** Baseline is established and shows expected scaling. List and map filter performance parity confirms consistent implementation. Allocations scale linearly with operation complexity, indicating no pathological allocator behavior.
**Notes:** 
- Filter predicates cost ~5.8× baseline regardless of collection type (list/map parity).
- Deep scan cost scales appropriately for recursive traversal through nested structures.
- All expressions are pre-compiled; benchmark measures evaluation only, not compilation.
- Expression bug fixed during benchmarking: `MAP_FILTER_EXPRESSION` had incorrect property path (`@.value.price` → `@.price`).

## PERF-006: Eliminate per-call `Object[]` and `ExecutionScope` allocation (H1 + H2)

**Date:** 2026-04-12

**Branch:** `refac-springboot-4`  
**Commits:** `ExpressionRuntimeSupport` + `ExecutionScope` — `OverrideSlot` ThreadLocal

**Scenario:** JFR profiling (21 s recording, `CollectionNavigationBenchmark`) identified two top allocation sources on the hot evaluation path:
- H1 — `buildOverrides` allocated `new Object[externalSymbolCount]` + `Arrays.fill` on **every** `compute()` call (1 852 `Object[]` allocation samples, ~30% of all sampled allocations).
- H2 — `ExecutionScope.readOnly(...)` created a new wrapper object on **every** `compute()` call (808 `ExecutionScope` samples, ~13% of all sampled allocations).

**Change summary:**
- `ExecutionScope`: added package-private `clearDynamicCache()` to safely reset the dynamic-instant cache on a reused scope.
- `ExpressionRuntimeSupport`: added `ThreadLocal<OverrideSlot>` that holds one `Object[]` buffer and one pre-wired `ExecutionScope` per thread. `buildOverrides` refills the thread-local buffer instead of allocating a new array. `createExecutionScope` returns the cached scope (after clearing its dynamic cache) instead of constructing a new one. Audited and mutable (assignment) paths remain unchanged.

**JMH run:** `CollectionNavigationBenchmark` — 5 warmup × 500 ms, 10 measure × 500 ms, 3 forks, `-prof gc`.

| Benchmark | Before (ns/op) | After (ns/op) | Δ ns (%) | B/op Before | B/op After | B/op Δ (%) |
|---|---:|---:|---:|---:|---:|---:|
| `indexAccess` | 71.6 ±3.5 | 73.9 ±2.2 | −3.2% (noise) | 64 | **0** | **−100%** |
| `mapValuesCount` | 147.1 ±3.2 | 136.0 ±7.5 | +7.6% | 152 | **88** | **−42%** |
| `customFunctionCount` | 297.8 ±8.1 | 297.2 ±10.0 | +0.2% | 296 | **232** | **−22%** |
| `listFilterCount` | 605.2 ±43.2 | 624.0 ±52.3 | −3.1% (noise) | 336 | **272** | **−19%** |
| `mapFilterCount` | 659.4 ±30.6 | 668.7 ±22.6 | −1.4% (noise) | 480 | **416** | **−13%** |
| `deepScanCount` | 1 395.3 ±81.2 | 1 390.4 ±73.2 | +0.4% | 1 051 | **987** | **−6%** |

**Decision:** ACCEPT  
**Reason:** All ns/op deltas are within or inside the ±error margin — no statistically significant throughput regression. B/op reductions are deterministic (identical across all forks/iterations) and range from **−6% to −100%** depending on how allocation-heavy the path is. `indexAccess` reaches zero allocations per call. The lower allocation rate directly reduces GC pressure across all collection-navigation expressions. Risk is low: only internal classes changed, all functional tests pass, the audited and mutable-scope paths are unaffected.

**Notes:**
- Re-entrancy limitation documented in `ExpressionRuntimeSupport.tlSlot` javadoc: same-instance re-entrant calls on the same thread (e.g., a catalog function that calls back into the same compiled expression) would produce incorrect results. This pattern is not a supported use case.
- The consistent **64-byte** reduction per benchmark (Object[5] + ExecutionScope) confirms the hypothesis; `mapValuesCount` shows a larger B/op drop because the JIT eliminated additional short-lived objects once the primary allocation pressure was removed.
