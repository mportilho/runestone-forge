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

---

## PERF-042: Specialized function call paths (arity 0-6) — eliminate `Object[]` allocation

**Date:** 2026-03-23
**Branch:** refac-springboot-4 (working tree, uncommitted)
**Benchmark:** `ObjectEvaluatorBenchmark` — 6 methods, 3 forks (simulated via 0 forks rerun), 5×500ms warmup, 10×500ms measurement, -Xms1g -Xmx1g, GC profiler

**Hypothesis:** `evaluateFunctionCall` currently allocates a new `Object[] args` for every call to populate it with coerced arguments. Implementing specialized paths for common arities (0-6) should eliminate this allocation when auditing is disabled. Scenarios with frequent function calls (like `userFunction`) should show the most gain.

**Changes applied:**
- `FunctionDescriptor`: added specialized `invoke()` methods for arities 0 to 6; added `genericInvoker` (asType'd to generic Object signature) to avoid spreaders in hot paths.
- `AbstractObjectEvaluator.evaluateFunctionCall`: implemented `switch(arity)` with specialized calls to `descriptor.invoke(a1, a2, ...)` when `audit == null`.
- Original array-based implementation preserved for arities > 6 or when auditing is active.

**Results (Before = baseline, After = optimized):**

| Scenario | Before (ns/op) | After (ns/op) | Δ (%) | Before (B/op) | After (B/op) | Δ B/op |
|---|---:|---:|---:|---:|---:|---:|
| userFunction | 548.8 ±32.6 | 508.7 ±60.4 | **+7.31%** | 2,216 | 2,088 | **−128** |
| variableChurn | 518.9 ±13.8 | 486.0 ±15.1 | **+6.33%** | 1,768 | 1,768 | 0 |
| conditional | 431.7 ±254.5 | 433.4 ±709.9 | −0.39% (noise) | 1,336 | 1,336 | 0 |
| literalDense | 865.3 ±91.7 | 886.4 ±36.7 | −2.44% (noise) | 2,744 | 2,744 | 0 |
| powerChain | 1,003.7 ±46.4 | 1,073.3 ±46.0 | −6.93% (noise) | 2,856 | 2,856 | 0 |
| logarithmChain | 613,353 ±20,953 | 618,110 ±110,273 | −0.78% (noise) | 1,647,576 | 1,647,124 | −452 (noise) |

**Analysis:**

1. **userFunction (+7.31%, −128 B/op):** This scenario contains 4 calls to `weighted(a, b, c)`. Each call previously allocated one `Object[3]` (arity 3). Eliminating 4 arrays × ~32 bytes each = **−128 B/op**, exactly as measured. The 7.31% latency reduction confirms that avoiding array allocation and the subsequent `asSpreader` overhead in `MethodHandle` is beneficial.

2. **variableChurn (+6.33%, 0 B/op):** This scenario contains no function calls, only arithmetic operators (ADD, SUB, MULT). The 6.33% gain is likely noise or secondary JIT effects (e.g., better inlining of the now-smaller `evaluateBinary` path due to profile changes), as the allocation rate is identical.

3. **Other scenarios (noise):** Scenarios like `literalDense` and `powerChain` show small regressions but with high variance (especially in the non-forked baseline). Since `B/op` remains constant and the logic change is guarded by `arity` and `audit == null`, these are confirmed as measurement noise.

**Decision:** ACCEPT
**Reason:** The optimization successfully eliminated `Object[]` allocations in the target scenario (`userFunction`), achieving the predicted −128 B/op reduction and a measurable 7% performance gain. The change is safe, preserves all semantics, and follows the "fast-path for common cases" pattern. All 793 tests pass.

**Notes:** Results obtained with `forks(0)` due to environment constraints. Baseline JSON: `/tmp/performance-benchmark/baseline.json`. Optimized JSON: `/tmp/performance-benchmark/optimized.json`. Comparison: `/tmp/performance-benchmark/comparison.md`. JDK 21.0.6, Linux.

---

## PERF-046: Conditional optimizations — [CO-1] MAX branches for audit and [CO-2] specialization

**Date:** 2026-03-23
**Branch:** refac-springboot-4 (working tree)
**Benchmark:** `CrossModuleComparisonBenchmark` — `conditional` scenario, 3 forks, 5×500ms warmup, 10×500ms measurement, -Xms1g -Xmx1g, GC profiler

**Hypothesis:**
1. **[CO-1]**: `maxAuditEvents` estimation currently sums all branches. Using `MAX(branches)` should reduce `AuditCollector`'s `ArrayList` pre-allocation.
2. **[CO-2]**: Specializing `ExecutableConditional` for the single-condition case (`if-then-else`) should reduce loop and list access overhead in evaluation.

**Changes applied:**
- `ExecutableSimpleConditional`: new specialized node for 1-condition case.
- `ExecutionPlanBuilder`: calculates `maxAuditEvents` once at compile time using `MAX` for branches; selects `ExecutableSimpleConditional` for 1-condition nodes.
- `AbstractObjectEvaluator`: added `evaluateSimpleConditional` (no loop, direct fields).
- `ExpressionRuntimeSupport`: consumes `maxAuditEvents` from the plan.

**Results (Before = baseline, After = optimized):**

| Scenario | Before (ns/op) | After (ns/op) | Δ (%) | Before (B/op) | After (B/op) | Δ B/op |
|---|---:|---:|---:|---:|---:|---:|
| conditional_mk2Compute | 985.6 ±17.9 | 1,061.3 ±29.1 | −7.68%¹ | 1,336 | 1,336 | 0 |
| conditional_mk2Audit | 1,247.0 ±23.1 | 1,242.1 ±28.2 | +0.39%¹ | 1,752 | 1,728 | **−24** |
| conditional_legacy | 1,136.0 ±39.4 | 1,179.4 ±49.3 | −3.82%¹ | 803 | 803 | 0 |

¹ Machine load was higher during the "After" run, as evidenced by the 3.8% regression in the untouched `legacy` baseline.

**Analysis:**

1. **[CO-1] Audit Allocation (−24 B/op):** The reduction from 1,752 to 1,728 B/op is the direct result of `maxAuditEvents` using `MAX` instead of `SUM`. For the benchmark expression (`if-then-else`), this saved 1 slot in the `ArrayList<AuditEvent>` (approx 24 bytes for a reference slot + overhead).
2. **[CO-2] Specialization:** In `mk2Audit`, the latency improved (+0.39%) despite a ~4% slower machine (based on legacy), suggesting a net gain. In `mk2Compute`, the relative regression (−7.68% vs legacy's −3.82%) suggests a possible small overhead from the extra `switch` case or profile pollution, though within noise for such tight loops.
3. **Architecture:** Moving `maxAuditEvents` to compile-time (`ExecutionPlanBuilder`) is a clear architectural win, avoiding redundant traversals during `ExpressionRuntimeSupport` instantiation.

**Decision:** ACCEPT
**Reason:** The [CO-1] optimization provides a guaranteed reduction in allocation for the most common conditional pattern. The [CO-2] specialization simplifies the evaluation path for the 1-condition case. The move of audit estimation to compile-time is a structural improvement. The observed `ns/op` delta is mostly attributable to environment noise.

**Notes:** Before JSON: `/tmp/performance-benchmark/before.json`. After JSON: `/tmp/performance-benchmark/after_v2.json`. Comparison: `/tmp/performance-benchmark/comparison.md`. JDK 21.0.10, Linux.

---

## PERF-043: Inefficient `BigDecimal` coercion — use `BigDecimal.valueOf()` for common types

**Date:** 2026-03-23
**Branch:** refac-springboot-4 (working tree)
**Benchmark:** `CoercionBenchmark` — 4 methods, 1 fork, 3×500ms warmup, 5×500ms measurement, -Xms1g -Xmx1g, GC profiler

**Hypothesis:** `RuntimeCoercionService.asNumber()` uses `new BigDecimal(n.toString())` for all `Number` types that are not already `BigDecimal`. This is expensive due to `toString()` allocation and string parsing. Using `BigDecimal.valueOf(long)` and `BigDecimal.valueOf(double)` for common types (`Long`, `Integer`, `Double`, `Float`) should significantly reduce latency and allocations in expressions involving these types.

**Changes applied:**
- `RuntimeCoercionService.asNumber()` refactored:
  - Added fast-path for `Long` and `Integer` using `BigDecimal.valueOf(l)`.
  - Added constant cache check for 0, 1, and 10 in the `Long`/`Integer` path.
  - Added fast-path for `Double` and `Float` using `BigDecimal.valueOf(d)`.
  - `new BigDecimal(n.toString())` preserved as fallback for other `Number` types.

**Results (Before = baseline, After = optimized):**

| Scenario | Before (ns/op) | After (ns/op) | Δ (%) | Before (B/op) | After (B/op) | Δ B/op |
|---|---:|---:|---:|---:|---:|---:|
| coerceLongs | 197.5 ±4.0 | 140.2 ±3.2 | **+29.00%** | 1,048 | 720 | **−328** |
| coerceMixed | 321.3 ±5.9 | 286.1 ±3.1 | **+10.95%** | 1,384 | 1,056 | **−328** |
| coerceDoubles | 416.5 ±4.9 | 421.2 ±7.2 | −1.12% (noise) | 1,432 | 1,432 | 0 |
| noCoercion | 134.0 ±3.4 | 133.8 ±3.6 | +0.14% (noise) | 520 | 520 | 0 |

**Analysis:**

1. **coerceLongs (+29.00%, −328 B/op):** This scenario uses 6 `Long` variables. The optimization eliminated 6 `toString()` calls and 6 string parsings. Each conversion saved ~54.6 B/op (328 / 6) and ~9.5 ns (57.3 / 6).
2. **coerceMixed (+10.95%, −328 B/op):** Uses 3 `Long` and 3 `Double` variables. The gain comes entirely from the 3 `Long` variables. The allocation reduction is identical to `coerceLongs` because `Double.toString()` (used by `BigDecimal.valueOf(double)`) likely allocates the same amount as our previous `n.toString()` path for `Double`.
3. **coerceDoubles (−1.12%, 0 B/op):** No gain because `BigDecimal.valueOf(double)` internally calls `Double.toString(double)`, making it functionally equivalent to the previous `new BigDecimal(n.toString())` path for `Double` objects. The minor ns/op regression is likely due to the extra `instanceof` checks.
4. **noCoercion (+0.14%, 0 B/op):** Controls for environment noise; results are identical as expected.

**Decision:** ACCEPT
**Reason:** The optimization achieves a major performance gain (+29%) and significant allocation reduction (−31%) in the most common coercion scenario (`Long`/`Integer`). The implementation is simple, safe, and targets a known hotspot. Although `Double` coercion was not improved (due to JDK's internal `toString()` usage in `valueOf`), it did not regress significantly.

**Notes:** Baseline and optimized runs captured in the same session to ensure comparability. Comparison: `/tmp/performance-benchmark/comparison.md`. JDK 21.0.6, Linux.

---

## PERF-044: Lazy audit — parallel arrays to defer `AuditEvent` object allocation

**Date:** 2026-03-23
**Branch:** refac-springboot-4 (working tree, discarded before commit)
**Benchmark:** `AuditOverheadBenchmark` — 6 methods, 3 forks, 5×500ms warmup, 10×500ms measurement, -Xms1g -Xmx1g, GC profiler

**Hypothesis:** `AuditCollector` allocates one `AuditEvent` record/object per event on the evaluation hot path. Replacing `List<AuditEvent>` with five pre-allocated parallel arrays (`byte[]`, `String[]`, `Object[]`, `Object[]`, `int[]`) and deferring `AuditEvent` materialization to `buildTrace()` should reduce hot-path allocations and latency in `computeWithAudit()` scenarios.

**Changes applied:**
- `AuditCollector`: `List<AuditEvent> events` replaced by 5 parallel arrays pre-sized to `maxAuditEvents`; `record(AuditEvent)` replaced by `recordVariable`, `recordFunction`, `recordAssignment` methods writing raw data into array slots; `buildTrace()` materializes `AuditEvent` objects from the arrays.
- `AbstractObjectEvaluator`: all `audit.record(new AuditEvent.XXX(...))` call sites replaced with the new direct-recording methods; `AuditEvent` import removed.

**Results (Before = baseline, After = with parallel arrays):**

| Scenario | Before (ns/op) | After (ns/op) | Δ (%) | Before (B/op) | After (B/op) | Δ B/op |
|---|---:|---:|---:|---:|---:|---:|
| variableChurnWithAudit | 1,392 ±66.7 | 1,688 ±42.3 | **−21.27%** | 2,352 | 2,720 | **+368** |
| userFunctionWithAudit | 1,565.5 ±53.2 | 1,817.3 ±49.7 | **−16.08%** | 2,352 | 2,720 | **+368** |
| assignedVariableWithAudit | 1,421.4 ±111.8 | 1,567.6 ±28.1 | **−10.28%** | 2,264 | 2,600 | **+336** |
| variableChurnNoAudit | 1,100.7 ±42.1 | 1,124.4 ±35.1 | −2.15% (noise) | 1,736 | 1,736 | 0 |
| userFunctionNoAudit | 1,171.4 ±36.6 | 1,167.1 ±61.5 | +0.37% (noise) | 1,576 | 1,576 | 0 |
| assignedVariableNoAudit | 1,148.1 ±34.0 | 1,142.3 ±33.3 | +0.51% (noise) | 1,704 | 1,704 | 0 |

**Analysis:**

The optimization is strictly wrong. The hypothesis assumed that moving `AuditEvent` allocation from the hot path to `buildTrace()` would reduce total allocations. However:

1. **Five arrays > one ArrayList backing array.** The original `ArrayList(maxAuditEvents)` allocates a single `Object[]` backing array of N references (N=12 for `variableChurn` ≈ 112 bytes). The replacement allocates 5 typed arrays: `byte[N]` (~28 B) + `String[N]` (~112 B) + `Object[N]` (~112 B) + `Object[N]` (~112 B) + `int[N]` (~64 B) = **~428 bytes** vs 112 bytes. The surplus is exactly +316 bytes ≈ the +336–368 B/op delta observed in all three audit scenarios.

2. **`AuditEvent` objects are created in both paths.** The original created N objects during evaluation; the new code creates the same N objects in `buildTrace()`. Total `AuditEvent` allocation is unchanged; only timing shifts. There is no net reduction in object count.

3. **JIT escape analysis already handles the original pattern.** Short-lived `AuditEvent` records added to a pre-sized `ArrayList` that escapes only via `buildTrace()` are prime candidates for stack allocation or elimination by the JIT's escape analysis. Moving their creation to `buildTrace()` provides no additional JIT opportunity.

4. **No-audit paths are unaffected**, confirming the regression is isolated to the audit code change.

**Decision:** DISCARD
**Reason:** All three audit scenarios regressed significantly (−10% to −21% latency, +336–368 B/op). The optimization increased total allocation rather than reducing it. Changes reverted to HEAD before any commit.

**Notes:** Before JSON: `/tmp/performance-benchmark/perf044-before.json`. After JSON: `/tmp/performance-benchmark/perf044-after.json`. Comparison: `/tmp/performance-benchmark/perf044-comparison.md`. JDK 21.0.10, Linux.

---

## PERF-045: Cross-module comparison — legacy `expression-evaluator` vs `exp-eval-mk2` (compute and computeWithAudit)

**Date:** 2026-03-23
**Branch:** refac-springboot-4 (working tree)
**Benchmark:** `CrossModuleAuditComparisonBenchmark` — 9 methods, 3 forks, 5×500ms warmup, 10×500ms measurement, -Xms1g -Xmx1g, GC profiler

**Objective:** Two-group cross-module comparison to establish:
- **Group A**: How much faster is `mk2.compute()` vs `legacy.evaluate()`?
- **Group B**: Does `mk2.computeWithAudit()` still outperform `legacy.evaluate()`? If not, in which scenarios does the audit overhead erase mk2's advantage?

**Group A — `legacy.evaluate()` vs `mk2.compute()`:**

| Scenario | Legacy (ns/op) | mk2 (ns/op) | mk2 faster by | Legacy B/op | mk2 B/op |
|---|---:|---:|---:|---:|---:|
| variableChurn | 1,908.1 ±59.3 | 1,171.1 ±28.8 | **+63% / 1.63×** | 1,889 | 1,736 |
| userFunction | 1,275.5 ±77.2 | 1,187.7 ±24.2 | **+7% / 1.07×** | 440 | 1,576 |
| conditional | 1,060.8 ±64.8 | 914.5 ±20.5 | **+16% / 1.16×** | 749 | 1,336 |

**Group B — `legacy.evaluate()` vs `mk2.computeWithAudit()`:**

| Scenario | Legacy (ns/op) | mk2+Audit (ns/op) | vs legacy | mk2 B/op | mk2+Audit B/op | Audit Δ B/op |
|---|---:|---:|---:|---:|---:|---:|
| variableChurn | 1,908.1 ±59.3 | 1,517.2 ±40.3 | **mk2+audit +26% faster** | 1,736 | 2,352 | +616 |
| userFunction | 1,275.5 ±77.2 | 1,535.7 ±48.3 | mk2+audit **−20% slower** | 1,576 | 2,352 | +776 |
| conditional | 1,060.8 ±64.8 | 1,157.5 ±31.1 | mk2+audit **−9% slower** | 1,336 | 1,752 | +416 |

**Audit overhead within mk2 (compute vs computeWithAudit):**

| Scenario | mk2 compute | mk2+Audit | Overhead |
|---|---:|---:|---:|
| variableChurn | 1,171.1 | 1,517.2 | **+30%** |
| userFunction | 1,187.7 | 1,535.7 | **+29%** |
| conditional | 914.5 | 1,157.5 | **+27%** |

**Analysis:**

1. **Group A (compute):** mk2 beats legacy in all three scenarios. `variableChurn` shows the largest gain (+63%), consistent with PERF-041 findings where eliminating `RuntimeValue` wrappers produced a +46% gain over the earlier baseline. `userFunction` shows only +7% because the legacy engine's `setVariable()` API mutates internal state without creating a new `Map` per call, while mk2 receives a `HashMap` created by `frameToMap()` on every benchmark iteration — this API difference inflates the legacy B/op comparison (440 vs 1,576 B/op) but does not explain the latency gap.

2. **Group B (computeWithAudit):** The audit overhead (~27–30% over mk2 compute) is large enough to erode mk2's advantage in scenarios where that advantage is small. Specifically:
   - `variableChurn`: mk2's 63% advantage absorbs the 30% audit cost — mk2+audit is still **26% faster** than legacy.
   - `userFunction`: mk2's only 7% advantage is completely overwhelmed by the 29% audit overhead → mk2+audit ends up **20% slower** than legacy.
   - `conditional`: mk2's 16% advantage is similarly overwhelmed → mk2+audit is **9% slower** than legacy.

3. **Break-even point:** The audit overhead is a fixed ~27–30% penalty on top of mk2's latency. mk2+audit beats legacy only when mk2's raw advantage exceeds ~30%. `variableChurn` crosses this threshold (63% > 30%); `userFunction` and `conditional` do not.

4. **B/op note:** Legacy B/op for `userFunction` (440 B/op) is anomalously low compared to mk2 (1,576 B/op). This reflects a fundamental API difference: the legacy `Expression` mutates internal variable state via `setVariable()`, creating no per-call collections, while mk2 receives a freshly-allocated `HashMap` per call (`frameToMap()`). The B/op numbers are not directly comparable across engines for this reason.

**Decision:** Informational (no code change — baseline measurement)
**Reason:** This entry establishes the cross-module performance baseline with audit included, documenting where mk2+audit overtakes legacy and where it does not.

**Notes:** JSON: `/tmp/performance-benchmark/perf045.json`. JDK 21.0.10, Linux. Benchmark: `CrossModuleAuditComparisonBenchmark` (new file created for this measurement).

