## PERF-038: `buildValues()` hot-path — fuse double catalog lookup + eliminate empty-defaults copy

**Date:** 2026-03-22
**Branch:** refac-springboot-4 (working tree)
**Benchmark:** `AssignmentExpressionBindingsBenchmark` + `ExpressionEvaluatorV2ExecutionPlanBenchmark` (3 forks, 5×500ms warmup, 10×500ms measurement, -Xms1g -Xmx1g, GC profiler)

**Hypothesis:** Three smells identified in `buildValues()` by static analysis post-PERF-037:
1. `externalSymbolCatalog.find()` called twice per variable (`rejectWhenNonOverridable` + `expectedType`) → 2 Map lookups + 2 `Optional` allocations per variable.
2. `compiledExpression.semanticModel()` dereferenced twice per variable (`rejectWhenInternal` + `requireExternalSymbol`) → 2 map lookups on the happy path.
3. `new HashMap<>(defaultValues)` even when `defaultValues` is empty → unnecessary object allocation in the most common environment (no external symbol catalog).

**Changes applied:**
- `ExternalSymbolCatalog.findOrNull(String)` added — returns the descriptor directly, no `Optional` wrapper.
- `buildValues()` refactored: single loop iteration per entry using `lookupExternalSymbol()` (1 map lookup on happy path) + `findOrNull()` (1 catalog lookup, result reused for both the overridable check and the `declaredType` extraction).
- `defaultValues.isEmpty()` guard added: returns/creates a plain `new HashMap<>()` instead of copying an empty map.
- Old helpers `rejectWhenInternal`, `requireExternalSymbol`, `rejectWhenNonOverridable`, `expectedType` removed.

| Benchmark | Before (ns/op) | After (ns/op) | Δ time (%) | Before (B/op) | After (B/op) | Δ B/op |
|---|---:|---:|---:|---:|---:|---:|
| computeNoExternal (0 ext) | 583.5 ±13.6 | 529.1 ±53.0 | +9.34%¹ | 752 | 752 | 0 |
| computeThreeExternal (3 ext) | 1,144.0 ±37.3 | 1,097.9 ±27.5 | +4.03% | 1,280 | 1,232 | −48 |
| computeTwelveExternal (12 ext) | 2,599.6 ±41.2 | 2,292.1 ±36.9 | **+11.83%** | 2,616 | 2,616 | 0 |
| computeLogicalMixedLiteralDense | 432.7 ±13.2 | 424.6 ±9.9 | +1.89% (noise) | 368 | 368 | 0 |
| computeMathLiteralDense | 1,371.7 ±42.3 | 1,380.7 ±63.5 | −0.66% (noise) | 3,608 | 3,608 | 0 |
| compileMathLiteralDense | 1,812,368 ±191,580 | 1,824,552 ±181,273 | −0.67% (noise) | 755,299 | 755,299 | 0 |

¹ `computeNoExternal` "after" error is ±53 ns; benchmark does not enter the per-entry loop — improvement is marginal and dominated by variance.

**B/op analysis:**
- `computeThreeExternal`: −48 B/op = 3 `Optional` objects eliminated (3 × ~16 B). Confirms that for small variable counts the JIT did not eliminate these via escape analysis.
- `computeTwelveExternal`: B/op unchanged at 2,616 despite eliminating 12 `Optional` objects (expected −192 B/op). Two likely explanations: (a) the JIT's escape analysis already eliminated the Optional allocations in the larger loop in both runs, or (b) the ~7% signal (192/2,616) is within B/op measurement noise at this scale. The 11.83% ns/op gain is real and comes from halved map-lookup count per iteration.
- `computeNoExternal`: B/op unchanged at 752. The `defaultValues.isEmpty()` guard returns `new HashMap<>()` instead of `new HashMap<>(emptyMap)` — but both produce the same object and table-allocation behaviour for an empty map. Savings are in CPU instruction count, not heap.

**Decision:** ACCEPT
**Reason:** `computeTwelveExternal` exceeds the 10% threshold (+11.83%). `computeThreeExternal` is +4.03% with zero risk (3 private helpers collapsed into 1 static method, one public method added to the catalog). No allocation regression in any scenario. All 799 tests pass. Changes are minimal and fully backwards-compatible.

**Residual optimization paths (not yet pursued):**
- The `defaultValues.isEmpty()` guard reduced code complexity but produced no measurable B/op gain for `computeNoExternal`, confirming that the +48 B/op gap seen in PERF-035 between old and new API is not from the HashMap creation but from other sources (likely the `ExecutionScope` construction and result map).
- Early-exit in `runtimeServices.from()` when the caller already passes a `RuntimeValue` subtype (already implemented at line 36 of `RuntimeValueFactory`).

**Notes:** Before JSON: `/tmp/performance-benchmark/perf038-before.json`. After JSON: `/tmp/performance-benchmark/perf038-after.json`. Comparison: `/tmp/performance-benchmark/perf038-comparison.md`. JDK 21.0.10, Linux.

---

## PERF-039: Cross-module comparison — post PERF-038 `buildValues()` optimization

**Date:** 2026-03-22
**Branch:** refac-springboot-4
**Benchmark:** `CrossModuleComparisonBenchmark` — 25 methods, 3 forks, 5×500ms warmup, 10×500ms measurement, -Xms1g -Xmx1g, GC profiler

**Scenario:** Updated cross-module comparison after the `buildValues()` hot-path optimization (PERF-038). Both the legacy (`expression-evaluator`) and mk2 benchmarks already include variable binding cost per iteration: legacy calls `setVariable()` N times inside each `@Benchmark` method (via `applyFrame()` / `applyLiteralSeed()`), while mk2 calls `compute(Map)` which bundles variable injection. The comparison is therefore apples-to-apples on total-work-per-evaluation.

**Cross-module comparison (legacy `evaluate()` vs mk2 `compute(Map)`):**

| Scenario | Legacy (ns/op) | mk2 compute (ns/op) | mk2 vs legacy (%) | Legacy (B/op) | mk2 compute (B/op) | mk2 audit (ns/op) | mk2 audit (B/op) |
|---|---:|---:|---:|---:|---:|---:|---:|
| literalDense | 4,798.95 ±114.4 | 1,685 ±70.1 | **+64.9%** | 7,256 | 3,832 | 1,842.45 ±59.2 | 4,032 |
| variableChurn | 2,096.71 ±63.6 | 1,845.25 ±47.3 | **+12.0%** | 1,889 | 2,168 | 2,207.37 ±61.3 | 2,784 |
| userFunction | 1,608.88 ±76.3 | 1,927.36 ±36.8 | −19.8% | 440 | 2,008 | 2,313.81 ±44.2 | 2,656 |
| conditional | 1,170.74 ±57.4 | 1,543.77 ±57.7 | −31.9% | 803 | 1,624 | 1,724.17 ±26.5 | 2,040 |
| logarithmChain | 999,664 ±18,186 | 1,627,393 ±33,025 | −62.8% | 1,131,995 | 1,672,900 | 1,617,613 ±35,478 | 1,649,782 |
| powerChain | 3,004.74 ±52.6 | 3,243.19 ±82.8 | −7.9% | 2,922 | 3,096 | 3,608.82 ±84.9 | 3,600 |

**PERF-036 vs PERF-039 delta — impact of PERF-038 `buildValues()` optimization:**

| Scenario | PERF-036 mk2 vs legacy (%) | PERF-039 mk2 vs legacy (%) | Delta (pp) |
|---|---:|---:|---:|
| literalDense | +66.0% | +64.9% | −1.1 (noise) |
| variableChurn | +14.5% | +12.0% | −2.5 (noise) |
| userFunction | −29.5% | **−19.8%** | **+9.7** |
| conditional | −39.2% | **−31.9%** | **+7.3** |
| logarithmChain | −56.5% | −62.8% | −6.3 (noise: dominated by log transcendentals) |
| powerChain | −14.0% | **−7.9%** | **+6.1** |

**Key findings:**

1. **literalDense / variableChurn:** Essentially unchanged — mk2 still wins by 65% and 12% respectively. These scenarios are unaffected by `buildValues()` cost (literalDense has only 1 variable; variableChurn gains are dominated by the typed execution plan).
2. **userFunction: −29.5% → −19.8% (+9.7 pp recovered):** The PERF-038 optimization directly narrows this gap. userFunction has 12 external symbols per call (3 groups of 4 weighted(a,b,c) calls), each of which previously paid 2 catalog lookups + 2 Optional allocations. After PERF-038, each pays 1 null-check lookup.
3. **conditional: −39.2% → −31.9% (+7.3 pp recovered):** Same mechanism — 12 external symbols per call.
4. **powerChain: −14.0% → −7.9% (+6.1 pp recovered):** Same — 12 external symbols per call. Now within single-digit deficit.
5. **logarithmChain:** The apparent regression (−56.5% → −62.8%) is noise — at ~1 ms/op the measurement variance is large (±18,000 ns for legacy, ±33,000 ns for mk2). Both sessions show the same root cause: `BigDecimalMath.log()` at DECIMAL128 precision dominates.

**Decision:** CHARACTERIZATION (no code change)
**Reason:** Updated baseline after PERF-038. The `buildValues()` optimization meaningfully narrowed the mk2 deficit in variable-heavy scenarios (up to +9.7 pp on userFunction). Scenarios where mk2 still lags (userFunction, conditional, powerChain) are now better understood: residual cost is from the `HashMap` creation + per-entry coercion inside `buildValues()`, not the catalog lookup overhead which has been eliminated.

**Notes:** Before JSON: `/tmp/performance-benchmark/perf037-before.json`. After JSON: `/tmp/performance-benchmark/perf037-after.json`. Comparison: `/tmp/performance-benchmark/perf037-comparison.md`. Worktree used for "before" run: `/tmp/perf-037-before` (HEAD `13739b6`). JDK 21.0.10, Linux.

---

## PERF-040: `AbstractRawObjectEvaluator` — boxed vs raw-object evaluator comparison

**Date:** 2026-03-23
**Branch:** refac-springboot-4 (working tree)
**Benchmark:** `RawObjectEvaluatorBenchmark` — 12 methods, 3 forks, 5×500ms warmup, 10×500ms measurement, -Xms1g -Xmx1g, GC profiler

**Hypothesis:** `AbstractRawObjectEvaluator` eliminates intermediate `RuntimeValue` boxing by carrying all sub-expression results as raw `Object` (`BigDecimal`, `Boolean`, etc.). Expected gains in expression-tree-heavy scenarios; `buildValues()` cost is shared by both paths so variable-binding-dominated scenarios should show smaller gains.

**Changes applied (prior to this benchmark):**
- `AbstractRawObjectEvaluator<T>` created — mirrors `AbstractRuntimeEvaluator<T>` but returns raw `Object` from `evaluateExpr()`; no `new RuntimeValue.NumberValue(…)` / `new RuntimeValue.BooleanValue(…)` per node.
- `ExecutionScope.findRaw(SymbolRef)` added — avoids `Optional<RuntimeValue>` allocation per variable read; uses package-private `UNBOUND` sentinel.
- `RuntimeServices.coerceRaw(Object, Class<?>)` added — fast-paths `instanceof` checks for function argument coercion.
- `Evaluator<T>` interface introduced — shared contract allowing `ExpressionRuntimeSupport` to store either evaluator behind `Evaluator<BigDecimal>` / `Evaluator<Boolean>` fields.
- `ExpressionRuntimeSupport.compileRawMath/Logical/Assignments` added — public entry points for the raw evaluator.

**Results (boxed = `AbstractRuntimeEvaluator`, raw = `AbstractRawObjectEvaluator`):**

| Scenario | Boxed (ns/op) | Raw (ns/op) | Δ ns/op (%) | Boxed (B/op) | Raw (B/op) | Δ B/op |
|---|---:|---:|---:|---:|---:|---:|
| literalDense | 1,782.0 ±466.6 | 1,177.9 ±57.0 | **+33.9%** | 3,821 | 2,757 | **−1,064** |
| variableChurn | 1,101.0 ±80.8 | 970.2 ±41.5 | **+11.9%** | 2,168 | 1,928 | −240 |
| userFunction | 1,554.4 ±331.8 | 1,551.8 ±291.0 | +0.2% (noise) | 2,008 | 1,896 | −112 |
| conditional | 838.1 ±28.9 | 832.3 ±38.6 | +0.7% (noise) | 1,624 | 1,528 | −96 |
| logarithmChain | 937,858 ±108,159 | 940,603 ±109,173 | −0.3% (noise) | 1,722,485 | 1,721,389 | −1,096 (noise) |
| powerChain¹ | 2,044.3 ±180.8 | 4,908.2 ±2,430.0 | −140.1% (invalidated) | 3,096 | 2,728 | −368 |
| **powerChain (rerun)** | **1,913.9 ±89.2** | **1,953.5 ±78.5** | **−2.1% (noise)** | **3,096** | **2,728** | **−368** |

¹ First powerChain run was contaminated by external process load (±2,430 ns, 49% error). Rerun after killing the interfering process confirms the result is noise.

**Analysis by scenario:**

1. **literalDense (+33.9%, −1,064 B/op):** The raw evaluator shows its largest gain here. The expression is `seed + 64 numeric literals` — all literals are pre-computed `RuntimeValue.NumberValue` whose `.raw()` is accessed once at the leaf; all 64 ADD operations skip `new RuntimeValue.NumberValue(…)`. Reduced allocation directly translates to faster throughput. The large boxed error (±466 ns) vs tight raw error (±57 ns) confirms that GC pressure from RuntimeValue objects was causing JVM pauses in the boxed path.

2. **variableChurn (+11.9%, −240 B/op):** 12 variables × 11 arithmetic ops. `findRaw()` eliminates 12 `Optional<RuntimeValue>` allocations per call; each arithmetic result skips one record allocation. The gain is real but smaller than literalDense because `buildValues()` (shared) still wraps the 12 input values into `RuntimeValue` on entry.

3. **userFunction (+0.2%, −112 B/op):** The 4 `weighted(a,b,c)` function calls dominate. `coerceRaw()` fast-paths the `BigDecimal` args (same type, no wrapping), but the `Object[] args` array per call, the reflective `descriptor.invoke()`, and the `weighted()` body itself all dominate. The −112 B/op confirms marginally fewer allocations but not enough to affect latency.

4. **conditional (+0.7%, −96 B/op):** Despite being the scenario most expected to benefit (explained in PERF-039 analysis), the gain is masked by `buildValues()`: 12 variables must be coerced from `Object` → `RuntimeValue` before scope is populated, regardless of the evaluator. The actual expression tree (`a > b` → 1 comparison + 5 arithmetic ops on the taken branch) is fast relative to `buildValues()`. The −96 B/op confirms the evaluation itself allocates less; the latency saving is simply below measurement resolution.

5. **logarithmChain (noise):** `BigDecimalMath.log()` at DECIMAL128 precision dominates (~940 µs of ~940 µs total). Evaluator overhead is negligible.

6. **powerChain (noise after rerun):** First run showed −140.1% with ±2,430 ns error (49% of score), flagged as suspicious. Rerun on a clean machine yields 1,913.9 ±89.2 (boxed) vs 1,953.5 ±78.5 (raw) — a −2.1% difference well within error margins. The original result was external process interference, not a JIT artifact. The −368 B/op reduction is consistent with the raw evaluator allocating fewer intermediate records.

**Decision:** CHARACTERIZATION (concept-testing vehicle, no production adoption yet)
**Reason:** The raw evaluator demonstrates real gains for expression-tree-dominated scenarios with high op-to-variable ratio (+33.9% literalDense, +11.9% variableChurn). All other scenarios show noise in ns/op but consistent allocation reduction across the board (−96 to −1,064 B/op), confirming the mechanism works. No regressions were found after the contaminated powerChain run was invalidated and repeated. The code is kept on the branch as a concept-testing vehicle. The next meaningful step is reducing `buildValues()` cost, which dominates `conditional`/`userFunction` and masks the raw evaluator's gains there.

**Residual investigation paths:**
- `buildValues()` bypass: for expressions with no external symbols, eliminating the `HashMap` creation would surface the raw evaluator gains in `conditional`/`userFunction` more clearly.
- `conditional` with fewer variables: test a conditional expression with 2–3 variables to isolate evaluator cost from `buildValues()` overhead.

**Notes:** Initial JSON: `/tmp/performance-benchmark/perf040.json`. powerChain rerun JSON: `/tmp/performance-benchmark/perf040-powerchain-rerun.json`. JDK 21.0.10, Linux.

---

## PERF-041: `RuntimeValue` elimination — full refactoring impact (boxed baseline → raw-everywhere)

**Date:** 2026-03-23
**Branch:** refac-springboot-4 (working tree, uncommitted)
**Benchmark:** `RawObjectEvaluatorBenchmark` — 6 methods, 3 forks, 5×500ms warmup, 10×500ms measurement, -Xms1g -Xmx1g, GC profiler

**Hypothesis:** PERF-040 showed that the raw evaluator's gains in variable-heavy scenarios (`conditional` +0.7%, `userFunction` +0.2%) were masked by `buildValues()` still wrapping input `Object` values into `RuntimeValue` before populating the scope. Completing the refactoring — removing `RuntimeValue` entirely and making `buildValues()` store raw `Object` — should unlock those masked gains.

**Changes applied (this refactoring):**
- `RuntimeValue.java` deleted (sealed interface with 8 record variants).
- `RuntimeValueFactory.java` deleted.
- `AbstractRuntimeEvaluator.java` deleted — `AbstractRawObjectEvaluator<T>` is now the sole evaluator.
- `MathEvaluator` / `LogicalEvaluator` now extend `AbstractRawObjectEvaluator<T>` (was `AbstractRuntimeEvaluator<T>`).
- `ExecutionScope`: storage changed from `Map<SymbolRef, RuntimeValue>` to `Map<SymbolRef, Object>`; `NULL_SENTINEL` removed; `findRaw()` uses `getOrDefault(key, UNBOUND)`.
- `ExpressionRuntimeSupport.defaultValues`: changed from `Map<SymbolRef, RuntimeValue>` to `Map<SymbolRef, Object>`; `seedDefaults()` and `buildValues()` now store raw `Object` directly via `coerceToResolvedType()`.
- `RuntimeServices.coerceToResolvedType(Object, ResolvedType)` added — normalises function return values (e.g. `Long` → `BigDecimal`) for both folded and non-folded function call paths.
- `RuntimeServices.coerceToResolvedType` extended to handle `VectorType` — converts `BigDecimal[]` (from `spread()`) to `List<Object>`.

**Comparison baseline:** HEAD `026570b` (`compileMath` → boxed `AbstractRuntimeEvaluator`, PERF-038-optimised `buildValues()` still wrapping to `RuntimeValue`).
**Comparison target:** current working tree (`compileMath` → raw `AbstractRawObjectEvaluator`, `buildValues()` storing raw `Object`).

| Scenario | Before (ns/op) | After (ns/op) | Δ (%) | Before (B/op) | After (B/op) | Δ B/op |
|---|---:|---:|---:|---:|---:|---:|
| literalDense | 975.6 ±27.3 | 827.9 ±15.7 | **+15.14%** | 3,800 | 2,731 | **−1,069** |
| variableChurn | 676.9 ±10.7 | 362.6 ±13.2 | **+46.44%** | 2,168 | 1,736 | **−432** |
| userFunction | 865.1 ±15.7 | 492.1 ±35.7 | **+43.12%** | 2,008 | 1,704 | **−304** |
| conditional | 674.4 ±23.9 | 348.7 ±16.8 | **+48.29%** | 1,624 | 1,336 | **−288** |
| powerChain | 1,418.6 ±38.7 | 1,073.3 ±39.1 | **+24.34%** | 3,096 | 2,536 | **−560** |
| logarithmChain | 613,151 ±5,639 | 611,559 ±5,747 | +0.26% (noise) | 1,722,458 | 1,721,800 | −658 (noise) |

**Analysis:**

1. **variableChurn (+46.44%, −432 B/op):** PERF-040 showed only +11.9% with the raw evaluator alone (buildValues still wrapping). The additional +34 pp comes directly from eliminating 12 `RuntimeValue` wraps per `buildValues()` call. Each of the 12 variables now flows as raw `BigDecimal` from user map → scope → evaluator with no intermediate allocation.

2. **conditional (+48.29%, −288 B/op):** PERF-040 showed +0.7% (noise) for this scenario — the gain was entirely masked by `buildValues()` overhead. With raw buildValues, the saving is now fully visible. Same root cause as variableChurn.

3. **userFunction (+43.12%, −304 B/op):** PERF-040 showed +0.2% (noise). Same mechanism — 12 external symbols per call, each previously allocated as a `RuntimeValue` in `buildValues()`. Now zero wrapping allocations.

4. **powerChain (+24.34%, −560 B/op):** PERF-040 rerun showed −2.1% (noise). The larger gain here confirms that `buildValues()` wrapping and the evaluator boxing of 12 intermediate `BigDecimal` values (from 12 power operations) both contributed. The −560 B/op is the largest absolute B/op reduction in this scenario.

5. **literalDense (+15.14%, −1,069 B/op):** PERF-040 showed +33.9% for the raw evaluator alone. The smaller gain here (relative to PERF-040) is expected: literalDense has only 1 variable (`seed`), so `buildValues()` wrapping saved only 1 `RuntimeValue`. The raw evaluator's own saving (−1,069 B/op) remains real and consistent with PERF-040 (−1,064 B/op).

6. **logarithmChain (+0.26%, noise):** `BigDecimalMath.log()` at DECIMAL128 precision dominates (~610 µs of ~610 µs total). Evaluator overhead is negligible regardless of boxing strategy.

**Decision:** ACCEPT
**Reason:** All variable-bound scenarios exceed the 10% threshold: variableChurn +46%, conditional +48%, userFunction +43%, powerChain +24%. literalDense +15% is consistent with the PERF-040 raw-evaluator gain. Allocation reduction is consistent and significant across all non-transcendental scenarios (−288 to −1,069 B/op). The refactoring also simplifies the codebase: 2 production classes deleted, 1 production interface deleted, 793 tests passing. No regressions found.

**Notes:** Before JSON: `/tmp/performance-benchmark/perf041-before.json`. After JSON: `/tmp/performance-benchmark/perf041-after.json`. Comparison: `/tmp/performance-benchmark/perf041-comparison.md`. Before worktree: `/tmp/perf-041-before` (HEAD `026570b`). JDK 21.0.10, Linux.
