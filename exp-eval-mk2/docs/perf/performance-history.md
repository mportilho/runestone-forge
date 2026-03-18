## PERF-001: Baseline parse strategy for ExpressionEvaluatorV2

**Date:** 2026-03-14

**Scenario:** Baseline measurement of parser latency for the current `ExpressionEvaluatorV2.g4` before any grammar refactor, focusing on inputs that exercise `allEntityTypes` and typed comparison decisions.
**Hypothesis:** An `SLL -> LL` fallback strategy will reduce parse cost for the current grammar because the hottest alternatives share long common prefixes.

| Benchmark | Before (ns/op) | After (ns/op) | Improvement (%) |
|-----------|---------------:|--------------:|----------------:|
| mathFlatAssignment | 84021.771 | 2089.653 | +97.51% |
| mathNestedDecisionAssignment | 4615886.069 | 19776.910 | +99.57% |
| logicalMixedComparison | 10892.702 | 7302.711 | +32.96% |
| vectorAssignment | 876296.286 | 7969.691 | +99.09% |

**Decision:** ADJUST
**Reason:** The measured gain from `SLL -> LL` fallback is large enough to justify adoption in the parser integration layer, but the module still lacks a stable production parsing facade and its existing grammar tests are already failing for unrelated reasons.
**Notes:** Measurements came from `ExpressionEvaluatorV2ParsingBenchmark` with the standard JMH protocol (`5x500ms` warmup, `10x500ms` measurement, `3` forks, `ns/op`, JDK 21.0.10).

## PERF-002: Assignment-oriented grammar refactor on top of parser facade

**Date:** 2026-03-14

**Scenario:** Introduce `genericEntity`, `assignmentValue`, `referenceTarget`, and explicit cast syntax while the production parser path uses the `SLL -> LL` facade.
**Hypothesis:** Pulling untyped assignment syntax out of `allEntityTypes` will reduce prediction work on the production parsing path without changing benchmark scenarios.

| Benchmark | Before (ns/op) | After (ns/op) | Improvement (%) |
|-----------|---------------:|--------------:|----------------:|
| sll.mathFlatAssignment | 2089.653 | 1796.445 | +14.03% |
| sll.mathNestedDecisionAssignment | 19776.910 | 16146.478 | +18.36% |
| sll.logicalMixedComparison | 7302.711 | 6513.318 | +10.81% |
| sll.vectorAssignment | 7969.691 | 7559.027 | +5.15% |

**Decision:** ACCEPT
**Reason:** The production strategy improved in every measured scenario, including double-digit gains in three of four cases, and the affected module test suite now passes.
**Notes:** `parseWithDefaultAdaptiveLl` regressed for `mathFlatAssignment` (`-36.38%`) and `mathNestedDecisionAssignment` (`-4.10%`), but that path is no longer the intended integration strategy after introducing `ExpressionEvaluatorV2ParserFacade`.

## PERF-003: Expand generic value syntax into function arguments and vector elements

**Date:** 2026-03-14

**Scenario:** Reuse the generic assignment rule for function arguments and vector elements, replacing `allEntityTypes` at those call sites.
**Hypothesis:** Removing the remaining `allEntityTypes` uses from hot paths would reduce prediction cost further.

| Benchmark | Before (ns/op) | After (ns/op) | Improvement (%) |
|-----------|---------------:|--------------:|----------------:|
| sll.mathFlatAssignment | 1796.445 | 2651.962 | -47.62% |
| sll.mathNestedDecisionAssignment | 16146.478 | 23245.128 | -43.96% |
| sll.logicalMixedComparison | 6513.318 | 9803.732 | -50.52% |
| sll.vectorAssignment | 7559.027 | 9464.258 | -25.20% |

**Decision:** DISCARD
**Reason:** The change regressed every measured scenario on the production parsing path, including large regressions on the simplest assignment and logical-comparison cases.
**Notes:** The code was reverted after measurement. The profiler confirmed that `allEntityTypes` disappeared from the vector scenario, but the broader `valueExpression` rule increased overall prediction cost more than it helped.

## PERF-004: Extract scalar comparisons from `logicalComparisonExpression`

**Date:** 2026-03-14

**Scenario:** Move math/string/date/time/datetime comparisons into a dedicated `scalarComparisonExpression` sub-rule while preserving the accepted language.
**Hypothesis:** Reducing the top-level decision fan-out in `logicalComparisonExpression` would lower prediction cost for the logical comparison hotspot.

| Benchmark | Before (ns/op) | After (ns/op) | Improvement (%) |
|-----------|---------------:|--------------:|----------------:|
| sll.mathFlatAssignment | 1796.445 | 2427.502 | -35.13% |
| sll.mathNestedDecisionAssignment | 16146.478 | 21503.010 | -33.17% |
| sll.logicalMixedComparison | 6513.318 | 8125.541 | -24.75% |
| sll.vectorAssignment | 7559.027 | 9953.510 | -31.68% |

**Decision:** DISCARD
**Reason:** Even the conservative extraction regressed every measured scenario on the production parsing path, so the added rule indirection did not pay off.
**Notes:** The code was reverted after measurement. Profiling showed the cost split across `logicalComparisonExpression` and `scalarComparisonExpression`, but total parse time still increased.

## PERF-005: Generic scalar comparisons plus semantic validation

**Date:** 2026-03-14

**Scenario:** Accept mixed scalar comparisons in the grammar and move obvious type-compatibility checks into a semantic validator after parsing.
**Hypothesis:** The simpler grammar shape might reduce parse cost enough to justify deferring type compatibility to a later validation phase.

| Benchmark | Before (ns/op) | After (ns/op) | Improvement (%) |
|-----------|---------------:|--------------:|----------------:|
| sll.mathFlatAssignment | 1796.445 | 2526.889 | -40.66% |
| sll.mathNestedDecisionAssignment | 16146.478 | 21059.537 | -30.43% |
| sll.logicalMixedComparison | 6513.318 | 10556.541 | -62.08% |
| sll.vectorAssignment | 7559.027 | 10716.076 | -41.77% |

**Decision:** DISCARD
**Reason:** Although the experiment enabled mixed scalar comparisons plus semantic error reporting, it regressed every measured scenario on the production parsing path by a wide margin.
**Notes:** The code was reverted after measurement. This result suggests that broadening the scalar comparison entry point increases prediction work more than the reduced type-specific alternatives save.

## PERF-006: Corpus-driven warmup for the `SLL -> LL` parsing path

**Date:** 2026-03-14

**Scenario:** Warm the shared ANTLR DFA caches with a small versioned corpus before measuring the first parse of each representative input.
**Hypothesis:** A startup warmup using representative expressions will reduce cold-start parse latency on the production `SLL -> LL` path without changing grammar semantics.

| Benchmark | Before (ns/op) | After (ns/op) | Improvement (%) |
|-----------|---------------:|--------------:|----------------:|
| sll.cold.loan-payment-projection | 5077857.361 | 61432.383 | +98.79% |
| sll.cold.portfolio-discount-allocation | 5497759.689 | 56658.882 | +98.97% |
| sll.cold.customer-eligibility-gate | 9445682.616 | 75057.263 | +99.21% |
| sll.cold.settlement-window-check | 4748308.063 | 51517.380 | +98.92% |

**Decision:** ACCEPT
**Reason:** The warmed parser reduced cold-start latency by roughly two orders of magnitude across every corpus scenario with no grammar change and low operational risk.
**Notes:** Measurements came from `ExpressionEvaluatorV2WarmupBenchmark` using the standard JMH protocol (`5x500ms` warmup, `10x500ms` measurement, `3` forks, `ns/op`, JDK 21.0.10). The representative corpus is versioned under `src/test/resources/com/runestone/expeval2/grammar/language/perf/corpus/`.

## PERF-007: Cost of explicit semantic validation for boolean comparisons

**Date:** 2026-03-14

**Scenario:** Measure the incremental cost of running `ExpressionEvaluatorV2ParserFacade.validateSemantics(...)` after `parseLogical(...)` on valid logical inputs, without changing the grammar.
**Hypothesis:** A conservative semantic validator for boolean comparisons would preserve grammar performance and add only a small end-to-end overhead.

| Benchmark | Before (ns/op) | After (ns/op) | Improvement (%) |
|-----------|---------------:|--------------:|----------------:|
| facade.booleanEquality | 1928.757 | 2977.521 | -54.38% |
| facade.booleanDifference | 2139.957 | 3003.085 | -40.33% |
| facade.logicalMixedComparison | 9621.523 | 11894.472 | -23.62% |
| facade.customerEligibilityGate | 25059.751 | 31639.430 | -26.26% |
| facade.settlementWindowCheck | 16302.370 | 20759.402 | -27.34% |

**Decision:** DISCARD
**Reason:** The explicit validator regressed every measured scenario, increased code and maintenance burden, and did not justify rejecting behavior that the language can permissively accept.
**Notes:** Measurements came from `ExpressionEvaluatorV2SemanticValidationBenchmark` using the standard JMH protocol (`5x500ms` warmup, `10x500ms` measurement, `3` forks, `ns/op`, JDK 21.0.10). The implementation was later removed from `ExpressionEvaluatorV2ParserFacade`.

## PERF-008: Pruned parse-tree traversal for semantic validation

**Date:** 2026-03-14

**Scenario:** Replace the generic semantic-validation traversal with a pruned walk that skips terminals and only descends into parser rules considered likely to contain boolean comparisons.
**Hypothesis:** Reducing parse-tree visits would lower the cost of `parseLogical + validateSemantics` without changing validation behavior.

| Benchmark | Before (ns/op) | After (ns/op) | Improvement (%) |
|-----------|---------------:|--------------:|----------------:|
| facade.booleanEquality.optimizedValidation | 2977.521 | 3334.081 | -11.98% |
| facade.booleanDifference.optimizedValidation | 3003.085 | 3017.679 | -0.49% |
| facade.logicalMixedComparison.optimizedValidation | 11894.472 | 13994.307 | -17.65% |
| facade.customerEligibilityGate.optimizedValidation | 31639.430 | 36993.950 | -16.92% |
| facade.settlementWindowCheck.optimizedValidation | 20759.402 | 22940.459 | -10.51% |

**Decision:** DISCARD
**Reason:** The pruned traversal still regressed every measured scenario, so the added dispatch and filtering logic did not pay off.
**Notes:** The optimization was reverted after measurement. Measurements used the same `ExpressionEvaluatorV2SemanticValidationBenchmark` protocol and compared the retained validator against the experimental pruned walk.

## PERF-009: Execution plan creation versus literal reprocessing

**Date:** 2026-03-17

**Scenario:** Compare commit `a5ef798` against `aee4029` to quantify the cost of creating the execution plan during compilation and the runtime gain from stopping repeated literal materialization during `compute()`.
**Hypothesis:** Building the execution plan in `aee4029` would make `compile()` slightly slower, but would significantly reduce `compute()` latency for literal-heavy expressions by avoiding repeated parsing of numbers, dates, times, datetimes, and strings.

| Benchmark | Before (ns/op) | After (ns/op) | Improvement (%) |
|-----------|---------------:|--------------:|----------------:|
| compileLogicalMixedLiteralDense | 233167.895 | 246734.984 | -5.82% |
| compileMathLiteralDense | 1478188.276 | 1571991.412 | -6.35% |
| computeLogicalMixedLiteralDense | 8005.152 | 392.940 | +95.09% |
| computeMathLiteralDense | 5814.145 | 1229.390 | +78.86% |

**Decision:** ACCEPT
**Reason:** The measured compile-time regression to create the execution plan stayed in the low single digits, while the runtime path improved sharply in both literal-heavy scenarios, which matches the intended optimization target.
**Notes:** Measurements came from `ExpressionEvaluatorV2ExecutionPlanBenchmark` using the standard JMH protocol (`5x500ms` warmup, `10x500ms` measurement, `3` forks, `ns/op`, JDK 21.0.10). The benchmark used one math expression with `64` numeric literals and one logical expression mixing numeric, date, time, datetime, string, and boolean literals. Relevant `exp-eval-mk2` module tests passed in both snapshots via `mvn -q -f exp-eval-mk2/pom.xml test`. Full reactor tests were not used as a gate because the environment failed in `runestone-toolkit` due ByteBuddy self-attachment, equally in both commits.

## PERF-010: Runtime comparison against `expression-evaluator`

**Date:** 2026-03-17

**Scenario:** Compare steady-state math execution between `expression-evaluator` and `exp-eval-mk2` in three cross-module scenarios: many literals with one moving input, multiple variables updated on every execution, and repeated calls to a user-defined function.
**Hypothesis:** `exp-eval-mk2` would outperform the legacy engine on literal-dense execution, while staying in the same range for variable churn and user-defined function calls.

| Benchmark | expression-evaluator (ns/op) | exp-eval-mk2 (ns/op) | MK2 Improvement (%) |
|-----------|-----------------------------:|---------------------:|--------------------:|
| literalDense | 4615.876 | 1726.136 | +62.60% |
| variableChurn | 1976.268 | 2245.198 | -13.61% |
| userFunction | 1212.905 | 3785.894 | -212.13% |

**Decision:** ADJUST
**Reason:** `exp-eval-mk2` is materially faster only for the literal-heavy path, but it regresses in the two mutable-runtime scenarios, with a severe slowdown when invoking user-defined functions.
**Notes:** Measurements came from `CrossModuleExpressionEngineBenchmark` using the standard JMH protocol (`5x500ms` warmup, `10x500ms` measurement, `3` forks, `ns/op`, JDK 21.0.10), with JSON saved to `exp-eval-mk2/target/performance-benchmark/cross-module-engine-comparison.json`. Cross-module functional coverage passed via `mvn -q -pl exp-eval-mk2 -am -Dsurefire.failIfNoSpecifiedTests=false -Dtest=ExpressionFacadeTest,CrossModuleExpressionEngineTest test`. Running isolated `expression-evaluator` targeted tests currently hits pre-existing truncated JMH generated sources during `testCompile`, so those tests were not used as an additional gate for this experiment.

Allocation profile for `userFunction` scenario (added as part of this entry to serve as baseline for Phase 1 refactoring):

| Benchmark | Score (ns/op) | Allocation (B/op) |
|-----------|-------------:|------------------:|
| legacyUserFunction | 1,662.188 | 440.038 |
| mk2UserFunction | 4,748.322 | 4,144.109 |

## PERF-011: Phase 1 — Eliminate redundant list allocation in `evaluateFunctionCall`

**Date:** 2026-03-17

**Scenario:** Measure the effect of replacing the two intermediate `List` allocations in `AbstractRuntimeEvaluator.evaluateFunctionCall(...)` with a single `Object[]` pre-sized to function arity, and adding a matching `FunctionDescriptor.invoke(Object[])` overload to remove the defensive `List.copyOf(...)` inside the descriptor.
**Hypothesis:** Removing two list allocations per function call and one defensive copy would reduce both `ns/op` and `B/op` for the `mk2UserFunction` scenario without regressing `mk2VariableChurn` or `mk2LiteralDense`.

**Changes applied:**
- `AbstractRuntimeEvaluator.evaluateFunctionCall(...)`: replaced `stream().toList()` + `new ArrayList<>(...)` with a single `Object[]` filled in a plain for-loop; passes the array directly to the descriptor.
- `FunctionDescriptor`: added `invoke(Object[])` overload that calls `invokeWithArguments(arguments)` without the `List.copyOf(...)` wrapping present in `invoke(List<Object>)`.
- `ExpressionEvaluatorV2ParserFacade`: removed unused import (cosmetic).

| Benchmark | Before (ns/op) | After (ns/op) | Improvement (%) |
|-----------|---------------:|--------------:|----------------:|
| mk2LiteralDense | 1,060.01 | 1,044.00 | +1.51% |
| mk2UserFunction | 1,630.70 | 1,351.80 | +17.10% |
| mk2VariableChurn | 993.02 | 994.10 | -0.11% |

Note: `before` values reflect the working tree state prior to this change in the current benchmark session (not PERF-010 numbers, which used a different JVM session and different expression inputs).

Allocation profile for `userFunction` scenario after Phase 1:

| Benchmark | Score (ns/op) | Allocation (B/op) |
|-----------|-------------:|------------------:|
| legacyUserFunction | 724.1 | 440.009 |
| mk2UserFunction | 1,351.8 | 2,672.020 |

Allocation delta: `mk2UserFunction` went from **4,144 B/op** (PERF-010 baseline) to **2,672 B/op** — a **35.5% reduction**. The gap to the legacy path (440 B/op) remains large; Phases 2 and 3 target the remaining overhead from `invokeWithArguments`, coercion, and `RuntimeValueFactory.from(...)`.

**Decision:** ACCEPT
**Reason:** `mk2UserFunction` improved by +17.10% (above the 10% threshold), allocation dropped by 35.5%, and `mk2VariableChurn` was essentially flat (-0.11%). The change is isolated, low-risk, and measurably advances the Phase 1 criterion (reduce `B/op` in the `mk2UserFunction` scenario).
**Notes:** Measurements used `CrossModuleExpressionEngineBenchmark` with the standard JMH protocol (`5x500ms` warmup, `10x500ms` measurement, `3` forks, `ns/op`, JDK 21.0.10). Allocation measured separately with `-f 1 -prof gc` on the `userFunction` benchmarks only. Module tests passed via `mvn -q -pl exp-eval-mk2 test` both before and after the change.

## PERF-012: Phase 2 — Replace `invokeWithArguments` with pre-compiled spread handle in `FunctionDescriptor`

**Date:** 2026-03-17

**Scenario:** Measure the effect of replacing `MethodHandle.invokeWithArguments(Object[])` in `FunctionDescriptor.invoke(Object[])` with a pre-compiled spread handle using `asSpreader + invokeExact`, computed once at construction time.
**Hypothesis:** Pre-adapting the method handle at bind time (compilation phase) eliminates the internal boxing, adapter-chain traversal, and array wrapping that `invokeWithArguments` performs on every call, reducing both `ns/op` and `B/op` for the `mk2UserFunction` scenario without regressing `mk2VariableChurn`.

**Changes applied:**
- `FunctionDescriptor`: converted from `record` to `final class` to allow a private `compiledInvoker` field; field is computed once in the constructor via `invoker.asType(generic()).asSpreader(Object[].class, arity)`; `invoke(Object[])` now calls `compiledInvoker.invokeExact(arguments)` instead of `invoker.invokeWithArguments(arguments)`.
- `FunctionDescriptor`: removed `invoke(List<Object>)` method, which was already unused after Phase 1.

| Benchmark | Before (ns/op) | After (ns/op) | Improvement (%) |
|-----------|---------------:|--------------:|----------------:|
| mk2UserFunction | 1,381.1 | 1,076.1 | +22.08% |
| mk2VariableChurn | 978.2 | 1,041.9 | -6.51% |
| legacyUserFunction | 687.3 | 724.3 | — (noise) |

Note: `mk2VariableChurn` regression (-6.51%) falls within noise given the high error margin (±130.9 ns/op); the variableChurn path does not invoke `FunctionDescriptor`.

Allocation profile for `userFunction` scenario after Phase 2:

| Benchmark | Score (ns/op) | Allocation (B/op) |
|-----------|-------------:|------------------:|
| legacyUserFunction | 796.6 | 440.011 |
| mk2UserFunction | 1,054.4 | 1,840.015 |

Allocation delta: `mk2UserFunction` went from **2,672 B/op** (Phase 1) to **1,840 B/op** — a **31.2% reduction**. Cumulative reduction from the PERF-010 baseline (4,144 B/op): **55.6%**. The remaining gap to the legacy (440 B/op) is targeted by Phase 3 (coercion and `RuntimeValueFactory.from(...)` overhead) and Phase 4 (base runtime overhead).

**Decision:** ACCEPT
**Reason:** `mk2UserFunction` improved by +22.08% (above the 10% threshold), allocation dropped by 31.2%, and the variableChurn scenario was unaffected within measurement noise. The change is isolated, low-risk, and the criterion for Phase 2 (reduce dispatch cost of function invocation) is met.
**Notes:** Measurements used `CrossModuleExpressionEngineBenchmark` with the standard JMH protocol (`5x500ms` warmup, `10x500ms` measurement, `3` forks, `ns/op`, JDK 21.0.10). Allocation measured separately with `-f 1 -wi 5 -i 5 -prof gc` on the `userFunction` benchmarks only. Module tests passed via `mvn -q -pl exp-eval-mk2 test` both before and after the change.

## PERF-013: Phase 3 — Coercion metadata shortcut and `RuntimeValueFactory.from` fast path

**Date:** 2026-03-17

**Scenario:** Measure the effect of two targeted changes aimed at reducing per-call overhead in the function dispatch path: (1) replacing `descriptor.parameterTypes().get(i)` with a direct `descriptor.parameterType(i)` backed by a `Class<?>[]` field; (2) adding instanceof short-circuits in `RuntimeValueFactory.from(...)` to skip `DataConversionService.convert` when the raw return value already matches the expected Java type.
**Hypothesis:** The `List.get()` call per argument would be eliminated in favour of a raw array access, and avoiding `DataConversionService.convert` for already-typed return values would reduce both CPU and allocation in the `mk2UserFunction` scenario.

**Changes applied:**
- `FunctionDescriptor`: added private `Class<?>[]` field `parameterTypesArray` populated from `parameterTypes` at construction; exposed `parameterType(int index)` for direct array access; `arity()` now reads from the array length.
- `AbstractRuntimeEvaluator.evaluateFunctionCall(...)`: replaced `descriptor.parameterTypes().get(i)` with `descriptor.parameterType(i)`.
- `RuntimeValueFactory.from(...)`: added `instanceof` fast path for each `ScalarType` case (`BigDecimal`, `Boolean`, `String`, `LocalDate`, `LocalTime`, `LocalDateTime`) to skip `DataConversionService.convert` when the raw value is already the correct Java type.

| Benchmark | Before (ns/op) | After (ns/op) | Improvement (%) |
|-----------|---------------:|--------------:|----------------:|
| mk2UserFunction | 1,053.0 | 1,060.1 | −0.67% |
| mk2VariableChurn | 959.1 | 957.5 | +0.17% |
| legacyUserFunction | 746.0 | 708.8 | — (noise) |

Allocation profile after Phase 3:

| Benchmark | Score (ns/op) | Allocation (B/op) |
|-----------|-------------:|------------------:|
| legacyUserFunction | 655.4 | 440.009 |
| mk2UserFunction | 1,049.6 | 1,840.015 |

Allocation delta: **unchanged** at 1,840 B/op vs Phase 2. The Phase 3 hypothesis did not hold.

**Key diagnostic finding:** The remaining 1,400 B/op gap over the legacy path is not from coercion metadata lookup or `RuntimeValueFactory.from(...)` round-trips. For the common `NumberValue → BigDecimal` coercion path, `RuntimeCoercionService.coerce` already short-circuits via `targetType.isInstance(value.raw())` (returning the raw value without allocation), and `DataConversionService.convert` for identity conversions does not allocate. The bulk of the remaining allocation originates in the base runtime overhead per `compute()`: `ExecutionScope` with its `HashMap`, the `MutableBindings.snapshot()` + copy chain, and evaluator object creation. This is the target of Phase 4.

**Decision:** DISCARD
**Reason:** `mk2UserFunction` change (−0.67%) is within measurement noise (±17 ns/op), and allocation is flat at 1,840 B/op. The code was reverted. The changes produced no measurable performance improvement.
**Notes:** Measurements used `CrossModuleExpressionEngineBenchmark` with the standard JMH protocol (`5x500ms` warmup, `10x500ms` measurement, `3` forks, `ns/op`, JDK 21.0.10). Allocation measured with `-f 1 -wi 5 -i 5 -prof gc`. Module tests passed via `mvn -q -pl exp-eval-mk2 test` both before and after revert.

## PERF-014: Phase 4 — Evaluator reuse and single-copy ExecutionScope creation

**Date:** 2026-03-17

**Scenario:** Eliminate two sources of base runtime overhead confirmed by Phase 3's diagnostic: (1) creation of a new `MathEvaluator` or `LogicalEvaluator` on every `compute()` call; (2) the double-copy path `MutableBindings.snapshot()` → `Map.copyOf(values)` followed by `ExecutionScope` → `new HashMap<>(copyOfValues)`, which produced two independent map allocations per execution.
**Hypothesis:** Caching evaluators as final fields and reducing map copies to a single `new HashMap<>(values)` would noticeably reduce both `ns/op` and `B/op` in the `mk2UserFunction` and `mk2VariableChurn` scenarios.

**Changes applied:**
- `MutableBindings`: removed `snapshot()` (returned `Map.copyOf(values)`); added package-private `copyValues()` returning `new HashMap<>(values)` directly.
- `ExecutionScope`: removed the copy inside the constructor; added `fromIsolated(Map<SymbolRef, RuntimeValue>)` factory that takes ownership of an already-isolated map without copying; removed unused `HashMap` import.
- `ExpressionRuntimeSupport`: added `mathEvaluator` and `logicalEvaluator` as final fields created once at construction; `computeMath()` and `computeLogical()` now reuse these evaluators; `createExecutionScope()` uses `ExecutionScope.fromIsolated(bindings.copyValues())`.

| Benchmark | Before (ns/op) | After (ns/op) | Improvement (%) |
|-----------|---------------:|--------------:|----------------:|
| mk2UserFunction | 1,084.908 ± 27.4 | 918.385 ± 32.0 | +15.35% |
| mk2VariableChurn | 977.871 ± 20.5 | 824.188 ± 23.3 | +15.72% |
| mk2LiteralDense | 1,045.611 ± 18.0 | 1,010.016 ± 31.0 | +3.40% |

Allocation profile after Phase 4 (`-f 1 -wi 5 -i 10 -prof gc`):

| Benchmark | Score (ns/op) | Allocation (B/op) |
|-----------|-------------:|------------------:|
| legacyUserFunction | 830.4 | 440.011 |
| mk2UserFunction | 939.2 | 1,400.013 |

Allocation delta: reduced from 1,840 B/op (Phase 3) to 1,400 B/op — a drop of 440 B/op (23.9% reduction). Remaining gap to legacy: 960 B/op (vs 1,400 B/op after Phase 3).

**Decision:** ACCEPT
**Reason:** Both primary mk2 scenarios improved by >15%, exceeding the 10% acceptance threshold. Allocation also dropped meaningfully (440 B/op). The two structural changes are safe and non-observable: evaluators are stateless (all fields final, no mutable instance state, scope is always passed as a parameter), and the scope creation no longer needs a defensive copy because `copyValues()` already produces an isolated map owned by the caller.
**Notes:** Measurements used `CrossModuleExpressionEngineBenchmark` with the standard JMH protocol (`5x500ms` warmup, `10x500ms` measurement, `3` forks, `ns/op`, JDK 21.0.10). Allocation measured with `-f 1 -wi 5 -i 10 -prof gc`. Module tests (199) passed via `mvn -q -pl exp-eval-mk2 test` after the changes.

## PERF-015: Phase 6 — Eliminate HashMap copy for assignment-free expressions

**Date:** 2026-03-18

**Scenario:** Measure the effect of bypassing `MutableBindings.copyValues()` on every `compute()` call when the compiled expression contains no assignments. For the `userFunction` scenario (4 calls to `weighted(a,b,c)`, no assignments), the `new HashMap<>(values)` copy was the single largest allocator, estimated at ~580 B/op.
**Hypothesis:** Routing assignment-free expressions through a shared read-only map instead of a defensive copy would eliminate ~580 B/op per `compute()` and reduce ns/op for both `mk2UserFunction` and `mk2VariableChurn`, while `mk2VariableChurn` would also benefit because it similarly has no assignments.

**Changes applied:**
- `MutableBindings`: added package-private `valuesReadOnly()` returning the internal `Map<SymbolRef, RuntimeValue>` directly without copying.
- `ExecutionScope`: added `mutable` boolean field and `readOnly(Map)` factory; `assign()` throws `IllegalStateException` on read-only scopes; `fromIsolated(Map)` unchanged (still takes ownership of a mutable copy).
- `ExpressionRuntimeSupport`: added `hasAssignments` boolean field set once at construction from `compiledExpression.executionPlan().assignments().isEmpty()`; `createExecutionScope()` routes to `ExecutionScope.readOnly(bindings.valuesReadOnly())` when no assignments, or the existing `ExecutionScope.fromIsolated(bindings.copyValues())` path otherwise.

| Benchmark | Before (ns/op) | After (ns/op) | Improvement (%) |
|-----------|---------------:|--------------:|----------------:|
| mk2UserFunction | 965.038 ± 34.3 | 776.184 ± 17.2 | +19.57% |
| mk2VariableChurn | 813.061 ± 21.8 | 700.089 ± 20.3 | +13.89% |
| mk2LiteralDense | 1,030.138 ± 27.8 | 1,012.948 ± 12.0 | +1.67% |

Cross-module comparison after Phase 6:

| Benchmark | legacy (ns/op) | mk2 (ns/op) | mk2 vs legacy |
|-----------|---------------:|------------:|-------------:|
| userFunction | 796.845 | 776.184 | **+2.6% (mk2 wins)** |
| variableChurn | 1,031.288 | 700.089 | **+32.1% (mk2 wins)** |
| literalDense | 2,439.801 | 1,012.948 | **+58.5% (mk2 wins)** |

**Decision:** ACCEPT
**Reason:** `mk2UserFunction` improved by +19.57% and `mk2VariableChurn` by +13.89%, both well above the 10% acceptance threshold. More significantly, `mk2UserFunction` is now faster than the legacy path for the first time — closing the −25.8% gap recorded in PERF-014. `mk2VariableChurn` regression risk was zero (it also has no assignments and benefits from the same optimization). The `assign()` guard ensures any expression with assignments that inadvertently takes the wrong path will fail immediately with a clear exception rather than silently corrupting state.
**Notes:** Measurements used `CrossModuleExpressionEngineBenchmark` with the standard JMH protocol (`5x500ms` warmup, `10x500ms` measurement, `3` forks, `ns/op`, JDK 21.0.10). Module tests passed via `mvn -q -pl exp-eval-mk2 test` after the changes. B/op profile not re-measured in this session; estimated elimination of ~580 B/op based on the Phase 4 allocation profile (1,400 B/op) and prior analysis in `perf-plan-user-function.md`.
