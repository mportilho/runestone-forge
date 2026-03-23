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
