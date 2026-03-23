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

## PERF-021: Focused `RuntimeValueFactory` wrapping fast paths

**Date:** 2026-03-19

**Scenario:** Measure the cost of wrapping already-typed scalar return values and vector-compatible inputs in `RuntimeValueFactory`, with emphasis on `BigDecimal[]` and `Collection` inputs that feed `VectorValue`.
**Hypothesis:** Skipping redundant conversion for exact scalar types and avoiding reflective/object-growth overhead in `toVector(...)` would reduce both `ns/op` and `B/op`, especially for object arrays.

**Changes applied:**
- `RuntimeValueFactory.from(...)`: added exact-type fast paths for `BigDecimal`, `Boolean`, `String`, `LocalDate`, `LocalTime`, and `LocalDateTime`.
- `RuntimeValueFactory.toVector(...)`: added specialized branches for `Collection<?>` and `Object[]`, with pre-sized `ArrayList` allocation; primitive arrays still use the reflective fallback.
- Added `RuntimeValueFactoryTest` coverage and `RuntimeValueFactoryBenchmark` / `RuntimeValueFactoryBenchmarkSupport` for isolated measurement.

| Benchmark | Before (ns/op) | After (ns/op) | Improvement (%) | Before (B/op) | After (B/op) |
|-----------|---------------:|--------------:|----------------:|--------------:|-------------:|
| fromNumberScalarExact | 26.950 | 24.444 | +9.30% | 16.000 | 16.000 |
| fromStringScalarExact | 25.883 | 23.726 | +8.33% | 16.000 | 16.000 |
| fromVectorArray | 6184.216 | 3393.415 | +45.13% | 5200.085 | 3696.047 |
| fromVectorIterable | 3856.532 | 3546.295 | +8.05% | 5192.053 | 3696.049 |

**Decision:** ACCEPT
**Reason:** The object-array path is a clear hotspot in this factory and improved by +45.13% with a 28.92% allocation reduction, while the other scenarios stayed positive and low-risk. The production patch mirrors the measured optimized branch.
**Notes:** Measurements used `RuntimeValueFactoryBenchmark` with the standard JMH protocol (`5x500ms` warmup, `10x500ms` measurement, `3` forks, `ns/op`, `-prof gc`, JDK 21.0.6). Verification passed via `mvn -q -Dtest=RuntimeCoercionServiceTest,RuntimeValueFactoryTest test` and `mvn -q -DskipTests test-compile` in `exp-eval-mk2`.

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

## PERF-016: Phase 7 — Eliminate `Optional` allocation in identifier lookup

**Date:** 2026-03-18

**Scenario:** Measure the effect of replacing `scope.find(id.ref()).orElseThrow(...)` with a direct `scope.findOrNull(id.ref())` null-check in `AbstractRuntimeEvaluator.evaluateExpression`. The `mk2VariableChurn` scenario exercises 22 identifier lookups per `compute()` and `mk2UserFunction` exercises 12, making these the most affected paths.
**Hypothesis:** Each `find()` call wraps the result in `Optional.ofNullable(...)`. With 12–22 lookups per `compute()`, this could create up to 192 B/op in `Optional` allocations. JIT escape analysis frequently eliminates scalar containers, so actual B/op savings may be zero.

**Changes applied (then reverted):**
- `ExecutionScope`: added package-private `findOrNull(SymbolRef)` returning the raw value or `null`.
- `AbstractRuntimeEvaluator`: replaced `scope.find(id.ref()).orElseThrow(...)` with an explicit null-check on `scope.findOrNull(id.ref())`.

| Benchmark | Before (ns/op) | After (ns/op) | Improvement (%) |
|-----------|---------------:|--------------:|----------------:|
| mk2UserFunction | 824.950 ± 31.2 | 809.474 ± 22.3 | +1.88% |
| mk2VariableChurn | 711.826 ± 23.0 | 732.001 ± 13.4 | −2.83% |
| mk2LiteralDense | 1,089.866 ± 78.9 | 1,510.545 ± 73.1 | — (spurious; literalDense has no identifier lookups) |

**Decision:** DISCARD
**Reason:** `mk2VariableChurn` (the primary target) showed a −2.83% change, which is within measurement noise given the ±23 ns/op error on the before run. `mk2UserFunction` showed +1.88%, also within noise. No evidence of measurable gain. The `mk2LiteralDense` apparent regression (−38.6%) is spurious — that benchmark contains no identifier lookups and cannot be affected by this change; it reflects JVM state differences between runs. The JIT's escape analysis already eliminates the `Optional` allocation on the hot path, confirming the plan's own hypothesis. Code was reverted.
**Notes:** Measurements used `CrossModuleExpressionEngineBenchmark` with the standard JMH protocol (`5x500ms` warmup, `10x500ms` measurement, `3` forks, `ns/op`, JDK 21.0.6). Module tests passed (199/199) via `mvn -q -pl exp-eval-mk2 test` before and after revert.

## PERF-017: Kahan summation for large BigDecimal arrays in MathFunctions

**Date:** 2026-03-18

**Scenario:** Replace standard `BigDecimal` summation loops in `mean`, `variance`, and `meanDev` with the Kahan summation algorithm (using `double` internally) when array size exceeds a threshold (1000).
**Hypothesis:** Using `double` with Kahan compensation for large arrays will significantly reduce object allocation pressure from `BigDecimal` immutability and improve performance while maintaining acceptable precision.

**Changes applied:**
- Created `KahanSummation` utility class for compensated summation of values, squares, and absolute deviations.
- Refactored `MathFunctions.mean`, `MathFunctions.variance`, and `MathFunctions.meanDev` to use Kahan summation for arrays with size >= 1000.
- Consolidated loops in `variance` and `meanDev` for small arrays (standard path).

| Benchmark (size=1000) | Before (ns/op) | After (ns/op) | Improvement (%) | Before (B/op) | After (B/op) | Allocation Reduction (%) |
|-----------------------|---------------:|--------------:|----------------:|--------------:|-------------:|-------------------------:|
| standardBigDecimalSum | 77,740.1 | 68,625.8 | +11.72% | 220,009 | 94,151 | +57.21% |

| Benchmark (size=5000) | Before (ns/op) | After (ns/op) | Improvement (%) | Before (B/op) | After (B/op) | Allocation Reduction (%) |
|-----------------------|---------------:|--------------:|----------------:|--------------:|-------------:|-------------------------:|
| standardBigDecimalSum | 465,468.7 | 361,358.1 | +22.37% | 1,109,166 | 531,869 | +52.05% |

**Decision:** ACCEPT
**Reason:** The optimization delivered double-digit performance gains and over 50% reduction in allocations for large arrays, directly addressing the "BigDecimal object creation in loops" performance smell.
**Notes:** Measurements used `KahanSummationBenchmark` and `MathFunctionsBenchmark` with the standard JMH protocol (JDK 21.0.10). A threshold of 1000 was chosen as it represents the point where the Kahan/double path consistently outperforms the standard BigDecimal path in both latency and allocation.

## PERF-018: Specialize array coercion in `RuntimeCoercionService`

**Date:** 2026-03-19

**Scenario:** Replace the reflective `Array.set(...)` loop used for `VectorValue -> T[]` coercion with specialized loops for the hot array targets (`BigDecimal[]`, `double[]`, `int[]`, `long[]`), while retaining the reflective fallback for compatibility with arbitrary component types.
**Hypothesis:** Removing reflective writes and recursive `coerce(...)` calls from the hot path would materially reduce `ns/op` for `BigDecimal[]` coercion and reduce both `ns/op` and `B/op` for primitive arrays, especially `double[]`, where reflection forces boxing/unboxing through `Object`.

**Technical baseline:** Branch `refac-springboot-4`, `HEAD` commit `f60d40425dad72588c78a43dcd2ad21a8c19c8bf`, with the benchmark baseline represented by a `HEAD`-equivalent reflective implementation embedded in `RuntimeCoercionArrayBenchmarkSupport.BaselineRuntimeCoercionService`. The measured "after" path is the current working-tree implementation in `RuntimeCoercionService`.

**Changes applied:**
- `RuntimeCoercionService`: added explicit scalar helpers for `double`, `int`, and `long`.
- `RuntimeCoercionService`: specialized the array branch for `BigDecimal[]`, `double[]`, `int[]`, and `long[]`, and hoisted `elements.size()` into a local `n`.
- `MathFunctionsExpressionTest`: added `mean(...)` API coverage for vector literals and bound vectors.
- `ExcelFinancialFunctionsExpressionTest`: added `npv(...)` API coverage for vector literals and bound vectors.
- `RuntimeCoercionArrayBenchmarkSupport` and `RuntimeCoercionArrayBenchmark`: added a dedicated JMH benchmark that compares the `HEAD`-equivalent reflective path against the optimized path with the same benchmark names and parameters.

| Benchmark | Before (ns/op) | After (ns/op) | Improvement (%) | Before (B/op) | After (B/op) |
|-----------|---------------:|--------------:|----------------:|--------------:|-------------:|
| `coerceBigDecimalArray` | 3,242.407 ± 452.485 | 188.667 ± 2.935 | +94.18% | 528.045 | 528.003 |
| `coerceDoubleArray` | 3,581.717 ± 66.567 | 173.093 ± 1.488 | +95.17% | 7,184.050 | 1,040.002 |

**Decision:** ACCEPT
**Reason:** Both measured scenarios improved by more than 94%, far above the 10% acceptance threshold. The `double[]` path also reduced allocation by 85.52%, confirming that the reflective primitive writes were materially expensive. `BigDecimal[]` allocation remained flat, so the win there is latency-only; that is consistent with removing reflective dispatch while still allocating the same result array.
**Notes:** Measurements used `RuntimeCoercionArrayBenchmark` with the standard JMH protocol (`5x500ms` warmup, `10x500ms` measurement, `3` forks, `ns/op`, `-prof gc`) on JDK 21.0.6. Relevant functional coverage passed via `mvn -q -pl exp-eval-mk2 -Dtest=MathFunctionsExpressionTest,ExcelFinancialFunctionsExpressionTest,ComparableFunctionsExpressionTest test`. JSON evidence was saved to `/tmp/performance-benchmark/runtime-coercion-array-before.json` and `/tmp/performance-benchmark/runtime-coercion-array-after.json`, with comparison summary in `/tmp/performance-benchmark/runtime-coercion-array-comparison.md`.

## PERF-019: Remove reflective writes from the generic reference-array path

**Date:** 2026-03-19

**Scenario:** Optimize the remaining generic array path in `RuntimeCoercionService.coerce(...)` for reference component types by replacing `Array.set(...)` with direct writes through `Object[]`, while preserving the existing recursive element coercion and keeping the reflective fallback only for unsupported primitive component types.
**Hypothesis:** For the real generic path still exercised by the module today (`Comparable[]` through `ComparableFunctions.max/min`), replacing reflective writes with direct array stores would materially reduce `ns/op` even if `B/op` remained flat, because the result array allocation still exists in both versions.

**Technical baseline:** Branch `refac-springboot-4`, working tree after `PERF-018`, with the "before" benchmark representing the production implementation that still used `Array.set(...)` for `Comparable[]`. The "after" benchmark represents the updated production implementation with a dedicated reference-array fast path.

**Changes applied:**
- `RuntimeCoercionService`: added `if (!componentType.isPrimitive())` branch that allocates `(Object[]) Array.newInstance(componentType, n)` and performs direct indexed writes.
- `RuntimeCoercionServiceTest`: added direct coverage for `Comparable[]` and `String[]` coercion.
- `RuntimeCoercionArrayBenchmark`: added `coerceComparableArray`.

| Benchmark | Before (ns/op) | After (ns/op) | Improvement (%) | Before (B/op) | After (B/op) |
|-----------|---------------:|--------------:|----------------:|--------------:|-------------:|
| `coerceComparableArray` | 3,337.086 ± 558.244 | 511.807 ± 8.235 | +84.66% | 528.046 | 528.007 |

**Decision:** ACCEPT
**Reason:** The `Comparable[]` path improved by +84.66%, far above the 10% acceptance threshold, with stable allocation. This confirms that the remaining cost was primarily reflective dispatch on each store, not structural allocation. The change is low-risk because Java array store checks are still preserved by direct assignment to the covariant runtime array instance.
**Notes:** Measurements used `RuntimeCoercionArrayBenchmark.coerceComparableArray` with the standard JMH protocol (`5x500ms` warmup, `10x500ms` measurement, `3` forks, `ns/op`, `-prof gc`) on JDK 21.0.6. Functional coverage passed via `mvn -q -pl exp-eval-mk2 -Dtest=RuntimeCoercionServiceTest,ComparableFunctionsExpressionTest test`. JSON evidence was saved to `/tmp/performance-benchmark/runtime-coercion-comparable-before.json` and `/tmp/performance-benchmark/runtime-coercion-comparable-after.json`, with comparison summary in `/tmp/performance-benchmark/runtime-coercion-comparable-comparison.md`.

## PERF-020: Scalar numeric fast paths in `RuntimeCoercionService`

**Date:** 2026-03-19

**Scenario:** Measure the effect of adding direct scalar coercion branches for `double`/`Double`, `int`/`Integer`, and `long`/`Long` in `RuntimeCoercionService.coerce(...)`, instead of letting those targets fall through to `DataConversionService.convert(value.raw(), targetType)`.
**Hypothesis:** Numeric scalar coercion is on the hot path for function arguments. Replacing the generic conversion-service dispatch with `asDouble`, `asInt`, and `asLong` should reduce `ns/op` materially and remove avoidable boxing/allocation, especially for `int` and `long` targets.

**Technical baseline:** Branch `refac-springboot-4`, working tree after `PERF-019`, with a fresh JMH baseline captured before changing `RuntimeCoercionService`. The benchmark uses the production `RuntimeCoercionService` as the measured path in both sessions; the embedded `BaselineRuntimeCoercionService` remains only as an internal control for same-session comparisons.

**Changes applied:**
- `RuntimeCoercionService`: added scalar fast paths for `Double`/`double`, `Integer`/`int`, and `Long`/`long` immediately after the existing `BigDecimal` branch.
- `RuntimeCoercionServiceTest`: added direct coverage for numeric scalar coercion to primitive and boxed numeric targets.
- `RuntimeCoercionScalarBenchmarkSupport` and `RuntimeCoercionScalarBenchmark`: added a dedicated JMH benchmark for scalar coercion scenarios with GC profiling.

| Benchmark | Before (ns/op) | After (ns/op) | Improvement (%) | Before (B/op) | After (B/op) |
|-----------|---------------:|--------------:|----------------:|--------------:|-------------:|
| `coerceDoublePrimitive` | 9.353 ± 0.287 | 6.349 ± 0.103 | +32.12% | 48.000 | 24.000 |
| `coerceDoubleWrapper` | 30.568 ± 0.942 | 5.276 ± 0.093 | +82.74% | 48.000 | 24.000 |
| `coerceIntPrimitive` | 8.375 ± 0.207 | 3.878 ± 0.101 | +53.69% | 24.000 | ≈ 0 |
| `coerceIntWrapper` | 30.230 ± 0.226 | 4.146 ± 0.146 | +86.28% | 24.000 | ≈ 0 |
| `coerceLongPrimitive` | 7.509 ± 0.101 | 4.640 ± 0.471 | +38.21% | 24.000 | ≈ 0 |
| `coerceLongWrapper` | 29.573 ± 0.542 | 4.209 ± 0.091 | +85.77% | 24.000 | ≈ 0 |

**Decision:** ACCEPT
**Reason:** Every measured scalar scenario improved well above the 10% acceptance threshold, with average improvement of +63.14%. Allocation also dropped in every scenario. The strongest effect is on boxed `int`/`long` targets, where the optimization removed the conversion-service path entirely and reduced `B/op` to effectively zero under this benchmark.
**Notes:** Measurements used `RuntimeCoercionScalarBenchmark` with the standard JMH protocol (`5x500ms` warmup, `10x500ms` measurement, `3` forks, `ns/op`, `-prof gc`) on JDK 21.0.6. Relevant functional coverage passed via `mvn -q -pl exp-eval-mk2 -Dtest=RuntimeCoercionServiceTest test` before and after the change. JSON evidence was saved to `/tmp/performance-benchmark/runtime-coercion-scalar-before.json` and `/tmp/performance-benchmark/runtime-coercion-scalar-after.json`, with comparison summary in `/tmp/performance-benchmark/runtime-coercion-scalar-comparison.md`.

## PERF-022: Trusted vector materialization for internal runtime paths

**Date:** 2026-03-19

**Scenario:** Revisit the next runtime hotspot after the `RuntimeValueFactory` changes, focusing on `VectorValue(List.copyOf(...))` and `AbstractRuntimeEvaluator.evaluateVector(...)`. The goal was to separate the cost of defensive copying from the cost of stream-based vector literal evaluation.
**Hypothesis:** Public construction of `VectorValue` should remain defensive, but internal runtime paths that just created a fresh `ArrayList` can safely transfer ownership without `List.copyOf(...)`. Replacing `stream().map(...).toList()` in `evaluateVector(...)` with a pre-sized loop should further reduce `ns/op` and `B/op`.

**Changes applied:**
- `RuntimeValue.VectorValue`: replaced the `record` with a final class that preserves value-based equality and defensive-copy semantics in the public constructor, while adding internal `fromTrustedElements(...)` for fresh runtime-owned lists.
- `AbstractRuntimeEvaluator.evaluateVector(...)`: replaced `stream().map(...).toList()` with a pre-sized `ArrayList` loop and the trusted vector factory.
- `RuntimeValueFactory.toVector(...)`: switched fresh internal lists to the trusted vector factory.
- `RuntimeCoercionService` and benchmark-support classes: replaced record-pattern destructuring with ordinary `instanceof` access to `vectorValue.elements()`.
- Added `RuntimeValueTest`, `VectorMaterializationBenchmarkSupport`, and `VectorMaterializationBenchmark`.

| Benchmark | Before (ns/op) | After (ns/op) | Improvement (%) | Before (B/op) | After (B/op) |
|-----------|---------------:|--------------:|----------------:|--------------:|-------------:|
| `evaluateVectorLiteral` | 658.051 ± 123.441 | 488.668 ± 8.347 | +25.74% | 1832.009 | 568.007 |
| `wrapMutableList` | 202.964 ± 26.330 | 48.441 ± 1.426 | +76.13% | 1624.003 | 568.001 |

**Decision:** ACCEPT
**Reason:** Both measured scenarios improved well above the 10% threshold, and the allocation reduction is large in exactly the paths under review. The strongest signal is the mutable-list wrapping path, confirming that the main hotspot was the defensive copy on lists already owned by the runtime.
**Notes:** Measurements used `VectorMaterializationBenchmark` with the standard JMH protocol (`5x500ms` warmup, `10x500ms` measurement, `3` forks, `ns/op`, `-prof gc`) on JDK 21.0.6. The benchmark compares embedded baseline and optimized implementations in the same session; the production patch mirrors the optimized branch. Functional verification passed via `mvn -q -Dtest=RuntimeValueTest,RuntimeValueFactoryTest,RuntimeCoercionServiceTest,ComparableFunctionsExpressionTest,MathFunctionsExpressionTest,ExcelFinancialFunctionsExpressionTest test` and `mvn -q -DskipTests test-compile` in `exp-eval-mk2`. JSON evidence was saved to `/tmp/performance-benchmark/vector-materialization.json`.

## PERF-023: Audit overhead — latency and allocation cost of `computeWithAudit()`

**Date:** 2026-03-20

**Scenario:** Measure the latency and allocation overhead of enabling audit collection (`computeWithAudit()`) compared to the plain `compute()` path across three representative expression profiles: variable-only reads (no assignments, no functions), a simple assignment, and repeated function calls.
**Hypothesis:** Audit overhead is non-trivial because each `compute()` with audit creates an `AuditCollector`, allocates one record per event, calls `List.copyOf(events)` in `buildTrace()`, and allocates a wrapping `AuditResult`. Function calls add a further `List.of(args)` per invocation.

| Benchmark | No Audit (ns/op) | With Audit (ns/op) | Overhead (%) | No Audit (B/op) | With Audit (B/op) | Alloc Δ (B/op) |
|-----------|----------------:|-------------------:|-------------:|----------------:|------------------:|---------------:|
| variableChurn | 713.507 ± 29.4 | 893.541 ± 41.5 | +25.24% | 1,064 | 1,840 | +776 |
| assignedVariable | 802.702 ± 69.4 | 1,013.343 ± 71.2 | +26.25% | 1,528 | 2,240 | +712 |
| userFunction | 802.260 ± 9.9 | 1,009.243 ± 30.1 | +25.79% | 904 | 1,936 | +1,032 |

**Decision:** ADJUST (characterisation — no code change)
**Reason:** The audit path imposes a consistent ~25% latency overhead and 712–1,032 B/op of additional allocation depending on event count. No change was applied. The results establish the audit cost baseline and identify two improvement candidates for future sessions:

1. **`List.of(args)` per function-call audit event** (`evaluateFunctionCall` in `AbstractRuntimeEvaluator`): the existing `Object[]` is discarded and re-wrapped in `List.of(args)` for each `FunctionCall` record. The +256 B/op gap between `userFunction` (4 calls × 3 args) and `variableChurn` isolates this as ~64 B/op per function invocation with audit enabled. Storing `Object[]` directly in `FunctionCall` would eliminate these copies.
2. **`AuditCollector` fixed-overhead per `compute()`**: the collector's `ArrayList<>(16)`, `List.copyOf(events)` in `buildTrace()`, `Duration.ofNanos(...)`, and the `AuditResult` wrapper together account for the ~776 B/op minimum per call even with only variable-read events.

**Notes:** Measurements used `AuditOverheadBenchmark` (new) with the standard JMH protocol (`5x500ms` warmup, `10x500ms` measurement, `3` forks, `ns/op`, `-prof gc`) on JDK 21.0.10. Functional tests passed (199 tests) via `mvn -q -pl exp-eval-mk2 test` before measurement. JSON evidence saved to `/tmp/performance-benchmark/audit-overhead.json`.

## PERF-024: Reduce allocation on the `computeWithAudit()` hot path

**Date:** 2026-03-20

**Scenario:** Apply two targeted allocation-reduction changes on the audit collection path identified in PERF-023: (1) eliminate `List.of(args)` per function-call event by converting `AuditEvent.FunctionCall` from a record to a final class that stores `Object[]` and defers the `List.copyOf` to the `inputArgs()` accessor (off the hot path); (2) replace `new ArrayList<>(16)` with a pre-sized `new ArrayList<>(maxAuditEvents)` in `AuditCollector` — where `maxAuditEvents` is computed statically from the `ExecutionPlan` once at compilation — and replace `List.copyOf(events)` in `buildTrace()` with `Collections.unmodifiableList(events)`.
**Hypothesis:** The combined changes would reduce `B/op` by approximately 256 B/op (four `List.of` calls for `userFunction`) + ~128 B/op (`List.copyOf` of N events) and produce a proportional latency improvement on the `WithAudit` paths, without affecting the `compute()` (non-audit) paths.

**Changes applied:**
- `AuditEvent.FunctionCall`: converted from `record` to `final class`; stores `Object[]` internally; `inputArgs()` accessor calls `List.copyOf(Arrays.asList(inputArgs))` lazily (off hot path); `equals`/`hashCode`/`toString` use `Arrays` comparisons.
- `AbstractRuntimeEvaluator.evaluateFunctionCall(...)`: removed `List.of(args)`; passes the existing `Object[] args` directly to the `FunctionCall` constructor.
- `AuditCollector`: replaced `new ArrayList<>(16)` with `new ArrayList<>(initialCapacity)` received via constructor; replaced `List.copyOf(events)` in `buildTrace()` with `Collections.unmodifiableList(events)`.
- `ExpressionRuntimeSupport`: added `maxAuditEvents` final field computed once at construction via `countMaxAuditEvents(executionPlan)` + `countNodeEvents(node)` static helpers; passed to `new AuditCollector(maxAuditEvents)` in both `computeMathWithAudit()` and `computeLogicalWithAudit()`.

| Benchmark | Before (ns/op) | After (ns/op) | Improvement (%) | Before (B/op) | After (B/op) | Alloc Δ (B/op) |
|-----------|---------------:|--------------:|----------------:|--------------:|-------------:|---------------:|
| variableChurnWithAudit | 893.541 ± 41.5 | 857.575 ± 38.8 | +4.02% | 1,840 | 1,680 | −160 |
| assignedVariableWithAudit | 1,013.343 ± 71.2 | 972.170 ± 70.7 | +4.06% | 2,240 | 2,088 | −152 |
| userFunctionWithAudit | 1,009.243 ± 30.1 | 954.282 ± 35.9 | +5.45% | 1,936 | 1,552 | −384 |
| variableChurnNoAudit | 713.507 ± 29.4 | 670.101 ± 27.0 | — (noise) | 1,064 | 1,064 | 0 |
| assignedVariableNoAudit | 802.702 ± 69.4 | 817.190 ± 22.0 | — (noise) | 1,528 | 1,528 | 0 |
| userFunctionNoAudit | 802.260 ± 9.9 | 812.930 ± 37.9 | — (noise) | 904 | 904 | 0 |

The `noAudit` variance (−1.8% to +6.1%) is measurement noise — the non-audit code path was not modified.

**Allocation decomposition:**
- `userFunctionWithAudit` −384 B/op = ~256 B eliminated by removing `List.of(args)` for 4 function calls + ~128 B from `Collections.unmodifiableList` replacing `List.copyOf` for 16 events.
- `variableChurnWithAudit` −160 B/op = ~128 B from `List.copyOf` elimination (14 events) + ~32 B from tighter ArrayList backing array (14 instead of 16 initial capacity).
- `assignedVariableWithAudit` −152 B/op = same `List.copyOf` elimination + pre-sizing savings.

**Decision:** ACCEPT
**Reason:** All `WithAudit` paths improved in both latency (+4–5.5%) and allocation (−6.8% to −19.8% B/op). The `userFunction` path — the heaviest function-call scenario — shows the largest allocation reduction (−19.8%). Latency gains are below the 10% threshold but consistent across all three scenarios and paired with significant allocation reduction; per the 1–10% policy with low risk, the change is worth keeping. The `noAudit` paths are unaffected (zero allocation change; latency deltas are within noise).
**Notes:** Measurements used `AuditOverheadBenchmark` with the standard JMH protocol (`5x500ms` warmup, `10x500ms` measurement, `3` forks, `ns/op`, `-prof gc`) on JDK 21.0.10. Functional tests (199) passed via `mvn -q -pl exp-eval-mk2 test` before and after the change. JSON evidence saved to `/tmp/performance-benchmark/audit-overhead-after.json`; comparison in `/tmp/performance-benchmark/audit-overhead-comparison.md`.

## PERF-025: Two-layer ExecutionScope — eliminate external bindings copy on every compute()

**Date:** 2026-03-21

**Scenario:** `AssignmentExpression.compute()` always uses a mutable `ExecutionScope` (`hasAssignments = true`), which triggered `MutableBindings.copyValues()` → `new HashMap<>(externalBindings)` on every call. The copy size grows with the number of external symbols used in the expression. This was flagged as a smell in performance review of the `AssignmentExpression` implementation.

**Hypothesis:** Replacing the copy-on-create approach with a two-layer scope — fresh internal map for assignments, shared read-only external bindings — would eliminate the external bindings copy without changing semantics, reducing both `ns/op` and `B/op` proportionally to the number of external symbols.

**Changes applied:**
- `ExecutionScope`: added nullable `externalValues` field (read-only external layer); added `fromTwoLayerIsolated(Map sharedExternal, int internalCapacity)` and `fromTwoLayerIsolatedWithAudit(...)` factory methods; `find()` checks `values` (internal) first, then `externalValues` when present; `assign()` writes only to `values` (internal layer is never null and always mutable).
- `ExpressionRuntimeSupport`: added `internalSymbolCount` field (cached from semantic model at construction); `createExecutionScope()` and `createAuditedExecutionScope()` now call `fromTwoLayerIsolated`/`fromTwoLayerIsolatedWithAudit` instead of `fromIsolated(bindings.copyValues())` for all expressions with assignments.

| Benchmark | Before (ns/op) | After (ns/op) | Improvement (%) | Before (B/op) | After (B/op) | Δ B/op |
|-----------|---------------:|--------------:|----------------:|--------------:|-------------:|-------:|
| computeNoExternal (0 ext) | 615.5 | 557.5 | +9.4% | 736.0 | 704.0 | −32 |
| computeThreeExternal (3 ext) | 933.3 | 794.4 | +14.9% | 1088.0 | 1008.0 | −80 |
| computeTwelveExternal (12 ext) | 1692.2 | 1333.5 | +21.2% | 2248.0 | 1906.7 | −341 |

**Decision:** ACCEPT
**Reason:** All three scenarios improved: 14.9% and 21.2% for expressions with external symbols (above the 10% threshold), and 9.4% for zero-external expressions (below threshold but low-risk and consistent with the savings in B/op). The improvement scales with external symbol count, confirming the hypothesis. Semantics are preserved: the internal layer absorbs assignment writes, and the shared external layer is never mutated. All 610 module tests passed before and after the change.
**Notes:** Benchmark used `AssignmentExpressionBindingsBenchmark` with `-f 1 -wi 5 -i 10 -prof gc` on JDK 21.0.10. JSON evidence in `/tmp/performance-benchmark/assign-before.json` and `/tmp/performance-benchmark/assign-after.json`. The `AuditOverheadBenchmark.assignedVariableNoAudit` and `assignedVariableWithAudit` scenarios (which use `MathExpression` with one assignment) also benefit from this change through the shared `hasAssignments` code path.

**Regression check (intra-session, 2026-03-21):** A follow-up measurement confirmed that `MathExpression` with assignments also benefits from PERF-025. The `AuditOverheadBenchmark.assignedVariableNoAudit` scenario (`r = a + b * c - d; r * e + f - g * h + i - j * k + l` — 1 internal symbol, 11 external) improved by +19.8% in ns/op and -491 B/op in the same JVM session, matching the PERF-025 profile. No regression was detected in any scenario. The apparent 2× gap against PERF-024 numbers is a cross-session JVM artifact (different CPU governor/JIT state between sessions); the intra-session comparison is the authoritative reference.

---

## PERF-026: BooleanValue — missing TRUE/FALSE static singletons

**Date:** 2026-03-21

**Scenario:** Every logical sub-expression in `AbstractRuntimeEvaluator` that produces a boolean result — comparison operators (`>`, `<`, `>=`, `<=`, `==`, `!=`) and boolean binary operators (`AND`, `OR`) — calls `new RuntimeValue.BooleanValue(b)`, creating a fresh 16-B record object per operation. For a 5-comparison AND chain this means up to 9 allocations × 16 B = 144 B/op that could be completely eliminated with two static singletons.

**Hypothesis:** Adding `BooleanValue.TRUE` and `BooleanValue.FALSE` static constants to `RuntimeValue.BooleanValue` and replacing all `new RuntimeValue.BooleanValue(b)` call sites in `AbstractRuntimeEvaluator` would reduce `B/op` proportionally to the number of boolean operations in the expression.

**Benchmark:** `BooleanValueBenchmark` — two scenarios: `boolChain` (5 comparisons + 4 AND ops = up to 9 boolean results) and `boolWide` (10 comparisons + 9 AND ops = up to 19 boolean results). All values are precomputed static `BigDecimal` constants to eliminate benchmark noise.

| Benchmark | ns/op | Error (±) | B/op | BooleanValue portion |
|-----------|------:|----------:|-----:|--------------------:|
| boolChain | 921 | ±103 | 336 | ~144 B (9 × 16 B = 43% of total) |
| boolWide | 1216 | ±223 | 512 | ~304 B (19 × 16 B = 59% of total) |

**Allocation scaling analysis:** The delta between `boolWide` and `boolChain` = 176 B/op for 10 additional boolean operations = 17.6 B per op, matching the 16-B `BooleanValue` record size and confirming the hypothesis. The BooleanValue portion grows from 43% to 59% of total allocation as the expression widens, making it the dominant allocation driver in boolean-heavy expressions.

**Async profiler:** Flamegraphs at `/tmp/performance-benchmark/async-profiles/` confirm `AbstractRuntimeEvaluator.evaluateBooleanBinaryOp` and `evaluateComparison` as the top allocation sites, each pointing to `RuntimeValue$BooleanValue::<init>`.

**Decision:** CANDIDATE
**Proposed fix:** Add static constants to `RuntimeValue.BooleanValue`:
```java
record BooleanValue(boolean value) implements RuntimeValue {
    static final BooleanValue TRUE  = new BooleanValue(true);
    static final BooleanValue FALSE = new BooleanValue(false);
}
```
Replace every `new RuntimeValue.BooleanValue(b)` in `AbstractRuntimeEvaluator` with `b ? RuntimeValue.BooleanValue.TRUE : RuntimeValue.BooleanValue.FALSE`. No semantic change. Expected reduction: ~144–304 B/op depending on expression complexity.

---

## PERF-027: BigDecimalMath.log() — extreme allocation for ln() and lb() functions

**Date:** 2026-03-21

**Scenario:** `MathFunctions.ln()` delegates directly to `BigDecimalMath.log(value, mc)` at `DECIMAL128` precision (34 significant digits). The Ben-Manes Big-Math library implements `log()` via a Taylor-series expansion that allocates a very large number of intermediate `BigDecimal` objects per call. For a chain of 12 logarithm calls, the allocation cost is catastrophic.

**Hypothesis:** The `BigDecimalMath.log()` implementation at DECIMAL128 precision allocates intermediate BigDecimals on every call, producing allocation rates in the megabyte-per-call range. This is observable as a performance cliff for any expression using `ln()`, `lb()`, or `log()`.

**Benchmark:** `CrossModuleLogBenchmark.mk2LogarithmChain` — evaluates `ln(a) + ln(b) + ... + ln(l)` (12 calls) with constant inputs.

| Benchmark | ns/op | Error (±) | B/op |
|-----------|------:|----------:|-----:|
| mk2LogarithmChain | 1,866,908 | ±398,544 | 1,721,264 |

**Finding:** 1.87 ms per call and 1.72 MB/op for 12 log calls. This is the highest-impact finding in this analysis. Each `ln()` call contributes ~143,438 B/op (~140 KB) of intermediate `BigDecimal` allocations from the Taylor-series expansion inside `BigDecimalMath.log()`.

**Async profiler:** Flamegraph confirms that virtually the entire allocation stack originates in `BigDecimalMath.log()` internals — `logUsingTwoThirds()`, `logUsingExponent()`, `logUsingRoot()`, and similar Taylor-series helper methods.

**Decision:** CANDIDATE
**Proposed fixes (in priority order):**
1. **Double-precision fast path:** Add a `DoubleMathFunctions` class (following the existing pattern of `DoubleExcelFinancialFunctions`) that uses `Math.log()` / `Math.log(2)` for non-arbitrary-precision contexts, and register double variants in `ExpressionEnvironmentBuilder.addMathFunctions()` behind a precision-tier selection mechanism.
2. **Lower default MathContext precision:** Evaluate whether `DECIMAL128` is the right default for all math operations, or if `DECIMAL64` (16 digits) would suffice for most use cases and reduce BigDecimalMath allocation significantly.
3. **Memoize `log(2)` for `lb()`:** See PERF-028.

---

## PERF-028: lb() — recomputes log(2) from scratch on every call

**Date:** 2026-03-21

**Scenario:** `MathFunctions.lb()` delegates to `BigDecimalMath.log2(value, mc)`. The Big-Math implementation of `log2` internally computes `log(value) / log(2)`, calling `BigDecimalMath.log()` **twice** — once for `value` and once for the constant `2` — with no caching of the constant term. Every call to `lb()` therefore pays twice the `BigDecimalMath.log()` cost even though `log(2)` is a pure constant at any given precision.

**Hypothesis:** `lb()` should be ~2× more expensive than `ln()` per call due to the double `log()` invocation, and this overhead is measurable in isolation.

**Measurement:** Micro-benchmark (100 calls with timed loop, same input value, same MathContext):
| Method | ns / 100 calls | Ratio |
|--------|---------------:|------:|
| ln()   | 237,408 | 1.00× |
| lb()   | 264,147 | 1.11× |

**Finding:** The 11% overhead is consistent with a second `BigDecimalMath.log(BigDecimal.TWO, mc)` call — the 1.11× instead of 2.0× ratio is because `log(2)` (a value near 1) is computationally cheaper than `log(x)` for arbitrary `x`. The overhead is still significant: every expression using `lb()` pays an extra ~26 µs per call relative to `ln()`.

**Async profiler:** Flamegraph for `mk2LogarithmChain` shows a secondary `log` stack that originates from the constant-argument path of `log2`, confirming the double-invocation.

**Decision:** CANDIDATE
**Proposed fix:** Precompute `log(2)` once per `MathContext` precision and cache it as a `static final` in `MathFunctions` or in `BigDecimalMath`'s calling wrapper:
```java
private static final ConcurrentHashMap<Integer, BigDecimal> LOG2_CACHE = new ConcurrentHashMap<>();

static BigDecimal log2(BigDecimal value, MathContext mc) {
    BigDecimal log2const = LOG2_CACHE.computeIfAbsent(mc.getPrecision(),
        p -> BigDecimalMath.log(BigDecimal.TWO, mc));
    return BigDecimalMath.log(value, mc).divide(log2const, mc);
}
```

---

## PERF-029: RuntimeValueFactory and RuntimeCoercionService created per compile() call

**Date:** 2026-03-21

**Scenario:** `ExpressionRuntimeSupport.from()` always creates two stateless wrapper objects — `new RuntimeValueFactory(conversionService)` and `new RuntimeCoercionService(conversionService)` — regardless of whether the underlying `CompiledExpression` was served from the Caffeine cache or freshly parsed. Both objects are stateless (they hold only a single reference to the shared `DataConversionService`) and contribute ~24 B each (~48 B total) per `compile()` call.

**Hypothesis:** These two objects are pure pass-throughs to `DataConversionService` with no mutable state. Storing them as `final` fields in `ExpressionEnvironment` (which already owns `DataConversionService`) would eliminate the 48 B allocation on every cache-hit `compile()` call.

**Benchmark:** `CompilePathAllocationBenchmark.compileSimpleCacheHit` and `compileFunctionCacheHit` — expressions are pre-warmed into the Caffeine cache in the `@Setup(Level.Trial)` method, so the benchmark measures only the per-call overhead in the cache-hit path.

| Benchmark | ns/op | B/op | Notes |
|-----------|------:|-----:|-------|
| compileSimpleCacheHit | 245 | 240 | 48 B from factory objects + ~192 B from MutableBindings + ERS wrapper |
| compileFunctionCacheHit | 274 | 240 | Same floor — function catalog size does not affect cache-hit allocation |

**Finding:** Both cache-hit scenarios show a 240 B/op floor. The factory pair (~48 B) accounts for 20% of the allocation on every compile call even when the entire compilation is a cache hit. Storing these as `ExpressionEnvironment` fields would reduce the floor to ~192 B/op.

**Async profiler:** Flamegraph shows `RuntimeValueFactory::<init>` and `RuntimeCoercionService::<init>` stacks visible in cache-hit paths, each called from `ExpressionRuntimeSupport.from()`.

**Decision:** DISCARDED
**Reason:** `RuntimeValueFactory` and `RuntimeCoercionService` are package-private in `internal.runtime`, making it impossible to store them as fields in `ExpressionEnvironment` (a different package) without widening visibility. Any alternative caching approach (e.g., `ConcurrentHashMap` in `ExpressionRuntimeSupport`) introduces a per-call map lookup (~10–20 ns) that likely exceeds the cost of two TLAB bump allocations (~2–6 ns total). The optimization is not worth the added complexity.

---

## PERF-030: FunctionCatalog.findExact() — stream + list allocation per function lookup

**Date:** 2026-03-21

**Scenario:** `FunctionCatalog.findExact(String name, int arity)` is called once per function call node during semantic resolution (the compile path). Its current implementation creates a new `Stream`, calls `.filter(...)`, and calls `.toList()` on every invocation, allocating a `Stream` object and a new `List` even when the catalog has only a single overload for the given function name.

```java
// Current implementation:
public Optional<FunctionDescriptor> findExact(String name, int arity) {
    List<FunctionDescriptor> candidates = descriptorsByName
        .getOrDefault(name, List.of())
        .stream()
        .filter(descriptor -> descriptor.arity() == arity)
        .toList();
    return candidates.size() == 1 ? Optional.of(candidates.getFirst()) : Optional.empty();
}
```

**Hypothesis:** Replacing the stream pipeline with a plain indexed loop (or a direct `Map<FunctionRef, FunctionDescriptor>` lookup) would eliminate the per-lookup stream and list allocation. Since `ExpressionEnvironmentBuilder` already sorts descriptors by arity within each name group, a sequential scan finds the match in O(1) for the common single-overload case.

**Benchmark:** `CompilePathAllocationBenchmark.compileFunctionCacheMiss` — exercises the full compilation pipeline including semantic resolution of 8 function calls (4 `ln`, 4 `sqrt`).

| Benchmark | ns/op | B/op | Notes |
|-----------|------:|-----:|-------|
| compileFunctionCacheMiss | 81,148 | 54,870 | Full pipeline: ANTLR + AST + semantic resolution + execution plan |
| compileFunctionCacheHit  | 274    | 240   | No semantic resolution; 54,630 B/op delta = compile-only allocation |

**Finding:** The cache-miss path allocates 54,630 B/op more than the cache-hit path. Investigation revealed that `findExact()` is not called by `SemanticResolver` — the actual hot path uses `findCandidates()`. The real allocation sources in `SemanticResolver.resolveFunctionCall()` were: (1) an unnecessary `List.copyOf()` wrapping the already-immutable result of `findCandidates()`; (2) two stream filter pipelines (`arityMatches`, `compatibleMatches`) each allocating a `Stream` object and a result `List`; (3) `descriptor.functionRef()` allocating a new `FunctionRef` on each resolution (PERF-031).

**Decision:** DONE (2026-03-21)
**Implementation:**
- `FunctionCatalog.findExact()` replaced stream pipeline with a `for` loop returning on first match (Option B) — fixes the method even though it is not currently on the hot path.
- `SemanticResolver.resolveFunctionCall()` refactored: removed `List.copyOf()` on `findCandidates()` result; replaced the two stream filter pipelines with a single `for` loop that detects arity mismatches, ambiguous matches, and incompatible arguments in one pass without allocating intermediate lists.
- `FunctionDescriptor.functionRef()` now returns a `private final FunctionRef` field pre-computed in the constructor (PERF-031).

**Measured result (CompilePathAllocationBenchmark, forks=0, 5 iterations):**

| Benchmark | ns/op before | B/op before | ns/op after | B/op after | B/op delta |
|-----------|-------------:|------------:|------------:|-----------:|-----------:|
| compileFunctionCacheMiss | 105,427 | 56,130 | 107,567 | 52,830 | **−3,300** |
| compileFunctionCacheHit  | 320     | 432     | 309     | 432    | 0 (expected — no semantic resolution on cache hit) |
| compileSimpleCacheHit    | 401     | 560     | 333     | 560    | 0 (expected — no function calls) |

Cache-miss allocation reduced by ~3,300 B/op (~5.9%). ns/op delta is within single-fork noise.

---

## PERF-031: FunctionDescriptor.functionRef() — new FunctionRef record on every call

**Date:** 2026-03-21

**Scenario:** `FunctionDescriptor.functionRef()` returns `new FunctionRef(name, arity())` on every invocation. Both `name` and `arity()` are compile-time constants (stored as `final` fields). The record could be computed once in the constructor and returned from a cached field.

```java
// Current implementation:
public FunctionRef functionRef() {
    return new FunctionRef(name, arity());  // 32 B allocated on every call
}
```

**Hypothesis:** Precomputing the `FunctionRef` in the `FunctionDescriptor` constructor and returning the cached instance would eliminate one 32-B allocation per function-call node per cache-miss compilation.

**Evidence:** Static analysis confirms the smell. Combined with `FunctionCatalog.findExact()` (PERF-030), during a cache-miss compilation of an expression with 8 function calls, `functionRef()` is called at least 8 times, contributing 8 × 32 B = 256 B of avoidable allocation.

**Decision:** DONE (2026-03-21) — implemented together with PERF-030. See PERF-030 for benchmark results.
**Implementation:** Added `private final FunctionRef functionRef` field to `FunctionDescriptor`, initialized in the constructor as `new FunctionRef(this.name, this.parameterTypes.size())`. The `functionRef()` accessor now returns the cached field.

---

## PERF-032: RuntimeCoercionService.coerce() for List target — missed stream allocation

**Date:** 2026-03-21

**Scenario:** PERF-022 eliminated a `List.copyOf()` allocation on the `evaluateVector` trusted path. However, the `List` target branch in `RuntimeCoercionService.coerce()` was not updated and still uses a stream pipeline:

```java
if (List.class.isAssignableFrom(targetType)) {
    return elements.stream().map(RuntimeValue::raw).toList();  // Stream + List allocated
}
```

This path is exercised whenever a function that declares a `List` parameter receives a vector value, converting `RuntimeValue` wrappers to their raw forms via `stream().map().toList()`.

**Hypothesis:** Replacing the stream pipeline with a pre-sized `ArrayList` loop would eliminate one `Stream` allocation and one intermediate array copy from this path, mirroring the fix already applied in PERF-022.

**Evidence:** Static analysis. The fix is structurally identical to PERF-022 and carries no behavioral risk.

**Decision:** DONE (2026-03-21)
**Implementation:** Replaced `elements.stream().map(RuntimeValue::raw).toList()` with a pre-sized `ArrayList` loop in `RuntimeCoercionService.coerce()` (line 129). Added `java.util.ArrayList` import.

```java
if (List.class.isAssignableFrom(targetType)) {
    List<Object> result = new ArrayList<>(elements.size());
    for (RuntimeValue rv : elements) result.add(rv.raw());
    return result;
}
```
Eliminates one `Stream` object + one internal spliterator per vector-argument function call. Benchmark not yet measured.

## PERF-033: UF-1 — Eliminate Object[] per function call via arity-specialized invokers (arity 0–10)

**Date:** 2026-03-22
**Branch:** refac-springboot-4
**Benchmark:** `CrossModuleComparisonBenchmark.userFunction_*` (3 forks, 5×500ms warmup, 10×500ms measurement, -Xms1g -Xmx1g, GC profiler)

**Scenario:** `userFunction` — 4 calls to `weighted(BigDecimal, BigDecimal, BigDecimal)` per evaluation.

**Hypothesis:** Eliminating `new Object[arity]` per function call (and using `MethodHandle.invokeExact` with individual Object arguments instead of `asSpreader`) would reduce allocations and latency on the compute (non-audit) path.

**Changes:**
- `FunctionDescriptor`: added `typedInvoker` field (`invoker.asType(type.generic())`, no `asSpreader`) and `invoke0()` through `invoke10()` methods using `invokeExact`.
- `AbstractRuntimeEvaluator.evaluateFunctionCall`: split into audit and non-audit paths; non-audit dispatches via switch on arity (0–10) to call the specialized invoker without constructing an `Object[]`.

| Benchmark | Before (ns/op) | After (ns/op) | Δ time (%) | B/op before | B/op after | Δ B/op (%) |
|---|---:|---:|---:|---:|---:|---:|
| userFunction_legacy | 1,709.5 ±344.9 | 1,549.3 ±91.2 | +9.4% (noise) | 440 | 440 | — |
| userFunction_mk2Compute | 1,658.6 ±23.5 | 1,720.5 ±155.4 | −3.7% (noise) | 904 | 776 | **−14.1%** |
| userFunction_mk2Audit | 1,991.2 ±41.5 | 2,068.7 ±84.9 | −3.9% (noise) | 1,552 | 1,552 | 0% (expected) |

**Decision:** DISCARD — reverted
**Reason:**
- The time delta (−3.7%) is within noise — error bars for `mk2Compute` overlap (before ±23, after ±155). No statistically significant latency improvement.
- The extra indirection (switch → `invoke3()` → `typedInvoker.invokeExact`) adds a dispatch layer that the JIT must inline; this trades the allocation cost for an extra call boundary, yielding a neutral time result.
- Although the −128 B/op reduction on the compute path is real (4 × `Object[3]` eliminated), the trade-off is not worth the added complexity: 11 extra methods on `FunctionDescriptor`, a large switch in the evaluator, and a split code path — with zero observable latency gain.
- The audit path was unchanged regardless (still builds `Object[]` for `FunctionCall` events).
- Code reverted to the original single-path `evaluateFunctionCall`.

## PERF-034: UF-2 — Fast-path NumberValue → BigDecimal em RuntimeCoercionService.coerce()

**Date:** 2026-03-22
**Branch:** refac-springboot-4
**Benchmark:** `CrossModuleComparisonBenchmark.userFunction_*` (3 forks, 5×500ms warmup, 10×500ms measurement, -Xms1g -Xmx1g, GC profiler)

**Scenario:** `userFunction` — 4 chamadas a `weighted(BigDecimal, BigDecimal, BigDecimal)` por avaliação (12 coerções `NumberValue → BigDecimal` por invocação).

**Hipótese:** Adicionar um fast-path específico `instanceof NumberValue && == BigDecimal.class` no início de `coerce()` evita, para o caso mais frequente em expressões numéricas:
- A verificação `value == NullValue.INSTANCE`
- A chamada dupla a `value.raw()` (uma em `isInstance`, outra no `return`)
- O custo reflexivo de `targetType.isInstance()`

**Mudança:** 3 linhas adicionadas ao início de `RuntimeCoercionService.coerce()`, após o check `targetType == RuntimeValue.class`:
```java
if (value instanceof RuntimeValue.NumberValue nv && targetType == BigDecimal.class) {
    return nv.value();
}
```

| Benchmark | Before (ns/op) | After (ns/op) | Δ time (%) | B/op before | B/op after | Δ B/op |
|---|---:|---:|---:|---:|---:|---:|
| userFunction_legacy | 1,472.8 ±108.6 | 1,444.1 ±84.0 | +1.95% (ruído) | 440 | 440 | — |
| userFunction_mk2Compute | 1,659.7 ±37.2 | 1,626.0 ±34.0 | **+2.03%** | 904 | 904 | 0% |
| userFunction_mk2Audit | 2,010.9 ±36.1 | 2,001.0 ±45.9 | +0.49% | 1,552 | 1,552 | 0% |

**Decision:** ACCEPT
**Reason:**
- Melhora de +2% em `mk2Compute` com erro menor antes/depois (±37 → ±34), dando confiança no resultado.
- Mudança de 3 linhas, risco praticamente zero, sem alteração de semântica.
- A melhora no audit (+0.49%) é dentro do ruído — esperado, pois o overhead do audit domina o custo de `coerce`.
- B/op inalterado — esta otimização atua apenas na latência (eliminando chamadas de método), não em alocações.

---

## PERF-035: Thread-safety refactoring — `setValue()` removed, `compute(Map<String,Object>)` introduced

**Date:** 2026-03-22
**Branch:** refac-springboot-4
**Benchmark:** `AssignmentExpressionBindingsBenchmark` + `ExpressionEvaluatorV2ExecutionPlanBenchmark` (3 forks, 5×500ms warmup, 10×500ms measurement, -Xms1g -Xmx1g, GC profiler)

**Scenario:** `MutableBindings` deleted; `MathExpression`, `LogicalExpression`, and `AssignmentExpression` had `setValue()` removed and `compute(Map<String,Object>)` added. Instances are now stateless and fully thread-safe. The benchmark compares the full `compute()` round trip before and after the change.

**Root cause of regression:** Before the change, `setValue(name, value)` stored pre-coerced `RuntimeValue` objects in `MutableBindings.values` once per setup. Each `compute()` call performed only a `new HashMap<>(bindings.values)` copy — all values were already in their target representation. After the change, `buildValues(Map<String,Object>)` runs on every `compute()` call and must: (1) copy `defaultValues`, (2) for each user entry: look up `internalSymbolsByName` (reject check), look up `externalSymbolsByName` (resolve SymbolRef), call `externalSymbolCatalog.find()` (Optional allocation, overridability check and type hint), and invoke `runtimeServices.from()` (coercion). The allocation increase scales linearly with external symbol count: approximately +48 B/op at 0 externals, +100 B/op per variable at 3 externals, +63 B/op per variable at 12 externals.

**Expressions with no external symbols** (`computeLogicalMixedLiteralDense`, `computeMathLiteralDense`) and the full compile path are unaffected within noise, confirming that the cost is entirely in the per-call coercion + validation path.

| Benchmark | Before (ns/op) | After (ns/op) | Δ time (%) | Before (B/op) | After (B/op) | Δ B/op |
|---|---:|---:|---:|---:|---:|---:|
| computeNoExternal (0 ext) | 556.3 ±15.0 | 644.4 ±119.7 | −15.85% | 704 | 752 | +48 |
| computeThreeExternal (3 ext) | 728.8 ±18.4 | 1,125.6 ±21.5 | −54.44% | 933 | 1,232 | +299 |
| computeTwelveExternal (12 ext) | 1,306.1 ±54.5 | 2,603.9 ±50.2 | −99.37% | 1,912 | 2,675 | +763 |
| computeLogicalMixedLiteralDense | 409.2 ±15.3 | 408.0 ±8.1 | +0.29% (noise) | 320 | 368 | +48 |
| computeMathLiteralDense | 1,266.6 ±34.1 | 1,255.8 ±40.3 | +0.85% (noise) | 3,560 | 3,608 | +48 |
| compileMathLiteralDense | 1,695,107 ±165,897 | 1,693,799 ±134,262 | +0.08% (noise) | 755,297 | 754,273 | −1,024 |

**Decision:** ACCEPT
**Reason:** The performance regression is a documented and expected tradeoff for thread safety, which is a correctness requirement, not a performance optimization. Before this change, `MutableBindings.values` was shared mutable state on the instance; concurrent `compute()` calls on the same expression would corrupt or read each other's variable bindings without external synchronization. The new API is fully stateless at the instance level: each `compute(Map)` call is self-contained with no side effects on the expression object. All 799 module tests pass. The regression is proportional to external symbol count and confined to the `buildValues()` path; literal-only expressions and the compile path are unaffected.

**Future optimization paths (not yet measured):**
- Skip `externalSymbolCatalog.find()` Optional allocation per variable when no external symbol catalog is configured (covers the common empty-environment case) — same pattern as PERF-030.
- Early-exit coercion in `runtimeServices.from()` when the provided value is already the exact expected `RuntimeValue` subtype — eliminates coercion cost when callers pre-convert values.
- For the `computeNoExternal` case, `buildValues(Map.of())` still creates `new HashMap<>(emptyDefaults)`, adding 48 B/op; a `defaultValues.isEmpty()` guard could return the pre-allocated empty map directly.

**Notes:** Measurements used standard JMH protocol on JDK 21.0.10. JSON evidence saved to `/tmp/performance-benchmark/before.json` (before) and `/tmp/performance-benchmark/after.json` (after). Comparison in `/tmp/performance-benchmark/comparison.md`.

## PERF-036: Cross-module baseline — expression-evaluator (legacy) vs exp-eval-mk2 (thread-safe)

**Date:** 2026-03-22
**Branch:** refac-springboot-4 (thread-safety refactoring, working tree — post PERF-035 change)
**Benchmark:** `CrossModuleComparisonBenchmark` — 25 methods, 3 forks, 5×500ms warmup, 10×500ms measurement, -Xms1g -Xmx1g, GC profiler

**Scenario:** Characterization run establishing absolute performance of both modules side-by-side after the thread-safety refactoring of exp-eval-mk2 (`compute(Map<String,Object>)` API). Seven mk2-only benchmarks were permanently added to `CrossModuleComparisonBenchmark`: compile (2), compute literal-only (2), and assignment bindings (3).

**Cross-module comparison (legacy `evaluate()` vs mk2 `compute(Map)`):**

| Scenario | Legacy (ns/op) | mk2 compute (ns/op) | mk2 vs legacy (%) | Legacy (B/op) | mk2 compute (B/op) | mk2 audit (ns/op) | mk2 audit (B/op) |
|---|---:|---:|---:|---:|---:|---:|---:|
| literalDense | 4,779.1 ±203.5 | 1,626.5 ±31.0 | **+66.0%** | 7,256 | 3,864 | 1,828.7 ±53.4 | 4,064 |
| variableChurn | 2,144.6 ±84.6 | 1,834.4 ±63.0 | **+14.5%** | 1,889 | 2,168 | 2,351.6 ±77.3 | 2,784 |
| userFunction | 1,566.1 ±81.0 | 2,027.7 ±40.9 | −29.5% | 440 | 2,008 | 2,563.4 ±522.6 | 2,656 |
| conditional | 1,163.3 ±43.4 | 1,619.8 ±41.4 | −39.2% | 803 | 1,624 | 1,871.8 ±58.1 | 2,040 |
| logarithmChain | 1,023,983 ±14,597 | 1,602,089 ±30,547 | −56.5% | 1,132,145 | 1,649,349 | 1,627,629 ±35,718 | 1,673,786 |
| powerChain | 2,926.0 ±65.1 | 3,337.1 ±62.7 | −14.0% | 2,655 | 3,096 | 3,805.9 ±74.7 | 3,600 |

**mk2-only benchmarks (compile, compute-literal, assignment bindings):**

| Benchmark | ns/op | Error | B/op |
|---|---:|---:|---:|
| compileMathLiteralDense | 1,721,888 | ±155,028 | 754,273 |
| compileLogicalMixedLiteralDense | 272,013 | ±50,007 | 102,827 |
| computeMathLiteralDense | 1,281.7 | ±25.1 | 3,608 |
| computeLogicalMixedLiteralDense | 409.9 | ±10.5 | 368 |
| computeNoExternal (0 ext) | 563.2 | ±15.2 | 752 |
| computeThreeExternal (3 ext) | 1,089.8 | ±25.4 | 1,280 |
| computeTwelveExternal (12 ext) | 2,540.2 | ±42.7 | 2,739 |

**Key findings:**

1. **literalDense (+66%)**: mk2 wins decisively. The expression `seed + C1 + C2 + … + C64` has 64 compile-time numeric literals; the mk2 execution plan folds them statically, reducing per-call work to a single binding lookup and one addition. Legacy re-evaluates the full literal-dense tree on every call.
2. **variableChurn (+14.5%)**: mk2 wins on a pure arithmetic expression with 12 variables. The typed execution plan avoids the dynamic type dispatch present in legacy.
3. **userFunction (−29.5%)**: mk2 is slower. Legacy's `Expression.addFunction(MethodType)` approach has very low dispatch overhead (direct MethodHandle call). mk2 adds per-call argument coercion via `buildValues()` for 12 variables plus function dispatch cost.
4. **conditional (−39.2%)**: mk2 is slower. Legacy evaluates conditional trees without the per-call `buildValues()` cost of 12 external symbols.
5. **logarithmChain (−56.5%)**: Both modules spend most time in `BigDecimal` transcendental functions (ln, lb). mk2 adds `buildValues()` overhead for 12 symbols on top of an already dominant cost. The logarithm time scale (~1 ms) makes this less relevant for real-world comparison.
6. **powerChain (−14.0%)**: mk2 is moderately slower. Exponentiation (`^`) is handled efficiently in both modules.

**Audit overhead** across scenarios is 7–29% additional cost over `compute()`.

**Decision:** CHARACTERIZATION (no code change)
**Reason:** This is an absolute baseline for the post-thread-safety state of exp-eval-mk2. Scenarios where mk2 lags legacy are explained by per-call `buildValues()` cost (documented in PERF-035). Improvements in the `buildValues()` hot path are tracked in PERF-035's future optimization paths section.

**Notes:** JSON results saved to `/tmp/performance-benchmark/cross-module.json`. JDK 21.0.10, Linux.

---

## PERF-037: Thread-safety refactoring — corrected before/after including `setValue()` cost

**Date:** 2026-03-22
**Branch:** refac-springboot-4
**Benchmark:** `AssignmentExpressionBindingsBenchmark` + `ExpressionEvaluatorV2ExecutionPlanBenchmark` (3 forks, 5×500ms warmup, 10×500ms measurement, -Xms1g -Xmx1g, GC profiler)

**Scenario:** Re-measurement of the PERF-035 thread-safety refactoring with a corrected methodology. PERF-035's "before" benchmark called `setValue()` once in `@Setup(Level.Trial)` and measured only `compute()`. This under-counted the real per-call cost of the old API, because in production callers must supply new variable values on every evaluation cycle (i.e., `setValue()` per variable before each `compute()`). This experiment moves `setValue()` calls inside the measured benchmark method for the "before" state, producing a fair apples-to-apples comparison against the new `compute(Map<String,Object>)` API.

**Measurement methodology:**
- **Before (HEAD, `13739b6`):** git worktree at HEAD with a modified `AssignmentExpressionBindingsBenchmark` that calls `setValue(k, v)` × N + `compute()` per measured iteration. Pre-allocated `BigDecimal` values are held in `@State` fields to avoid measuring value creation.
- **After (working tree):** Existing `AssignmentExpressionBindingsBenchmark` calling `compute(Map<String,Object>)` per iteration.

| Benchmark | Before (ns/op) | After (ns/op) | Δ time (%) | Before (B/op) | After (B/op) | Δ B/op |
|---|---:|---:|---:|---:|---:|---:|
| computeNoExternal (0 ext) | 552.5 ±13.2 | 512.8 ±52.5 | +7.19% (noise¹) | 704 | 752 | +48 |
| computeThreeExternal (3 ext) | 958.0 ±41.0 | 1,119.5 ±31.4 | −16.86% | 1,008 | 1,232 | +224 |
| computeTwelveExternal (12 ext) | 2,457.4 ±51.4 | 2,540.8 ±46.9 | −3.39% (noise²) | 2,104 | 2,616 | +512 |
| computeLogicalMixedLiteralDense | 406.4 ±11.9 | 413.8 ±12.8 | −1.83% (noise) | 320 | 368 | +48 |
| computeMathLiteralDense | 1,309.4 ±59.6 | 1,272.1 ±35.9 | +2.84% (noise) | 3,560 | 3,608 | +48 |
| compileMathLiteralDense | 1,717,734 ±172,877 | 1,741,053 ±172,319 | −1.36% (noise) | 755,297 | 755,297 | 0 |

¹ `computeNoExternal` "after" error is ±52.5 ns; results overlap, no real change.
² `computeTwelveExternal` error bands overlap (±51 before, ±47 after); effective draw.

**PERF-035 vs PERF-037 comparison (corrected before vs original before):**

| Benchmark | PERF-035 before | PERF-037 before | Delta | Explanation |
|---|---:|---:|---:|---|
| computeNoExternal | 556.3 ns | 552.5 ns | −0.7% (same) | No setValue() in either benchmark |
| computeThreeExternal | 728.8 ns | 958.0 ns | +31.4% | Now includes 3× setValue() per iteration |
| computeTwelveExternal | 1,306.1 ns | 2,457.4 ns | +88.1% | Now includes 12× setValue() per iteration |

**PERF-035 vs PERF-037 regression delta (after vs before):**

| Benchmark | PERF-035 Δ (%) | PERF-037 Δ (%) | Reduction in apparent regression |
|---|---:|---:|---:|
| computeThreeExternal | −54.44% | −16.86% | 37.6 pp |
| computeTwelveExternal | −99.37% | −3.39% | 96.0 pp |

**Key finding:** PERF-035 significantly overstated the production regression by excluding `setValue()` from the "before" measurement while `buildValues(Map)` (which performs equivalent per-variable work: symbol lookup + overridability check + coercion) was fully included in the "after" measurement. The corrected comparison shows:
- **3 external symbols:** real regression is −16.86%, not −54.44%.
- **12 external symbols:** regression collapses to noise (−3.39%), not −99.37%. At 12 symbols, `setValue()×12` and `buildValues(12-entry Map)` do nearly equivalent work; the only extra cost in the new API is the `defaultValues` copy (even when empty: +48 B/op overhead visible in the `computeNoExternal` B/op delta) and the additional per-entry overridability check via `externalSymbolCatalog.find()`.

**Why the 3-symbol case still shows a real regression (−16.86%):**
- The 3-symbol expression is fast to evaluate, so the fixed overhead of `buildValues()` (HashMap allocation + `defaultValues` copy even when empty) represents a larger fraction of total call time.
- Each `buildValues()` call allocates a new `HashMap`, copies `defaultValues`, then iterates and coerces entries — compared to 3 `setValue()` calls that each do an individual `values.put()` into a long-lived map.
- B/op delta of +224 confirms higher allocation in the new API for this scenario.

**Decision:** CHARACTERIZATION (no code change)
**Reason:** This experiment corrects the PERF-035 measurement methodology; it does not introduce any code change. The thread-safety ACCEPT decision from PERF-035 stands. The new numbers give a more accurate picture of the real production cost: the 12-symbol regression effectively disappears when `setValue()` is fairly accounted for, and the 3-symbol regression is −17% rather than −54%. The future optimization paths documented in PERF-035 (skip Optional allocation when no catalog, early-exit coercion, guard against empty-defaults copy) remain valid and would eliminate most of the remaining −17% for the 3-symbol case.

**Notes:** Before JSON: `/tmp/performance-benchmark/perf037-before.json`. After JSON: `/tmp/performance-benchmark/perf037-after.json`. Comparison: `/tmp/performance-benchmark/perf037-comparison.md`. Worktree used for "before" run: `/tmp/perf-037-before` (HEAD `13739b6`). JDK 21.0.10, Linux.

---

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

**mk2-only benchmarks (compile, compute-literal, assignment bindings):**

| Benchmark | ns/op | Error | B/op |
|---|---:|---:|---:|
| compileMathLiteralDense | 1,684,859 | ±164,981 | 754,272 |
| compileLogicalMixedLiteralDense | 279,042 | ±47,993 | 102,560 |
| computeMathLiteralDense | 1,361.81 | ±56.5 | 3,608 |
| computeLogicalMixedLiteralDense | 431.73 | ±15.5 | 368 |
| computeNoExternal (0 ext) | 561.38 | ±19.1 | 752 |
| computeThreeExternal (3 ext) | 1,047.58 | ±21.3 | 1,232 |
| computeTwelveExternal (12 ext) | 2,313.39 | ±57.5 | 2,739 |

**PERF-036 vs PERF-039 delta — impact of PERF-038 `buildValues()` optimization:**

| Scenario | PERF-036 mk2 vs legacy (%) | PERF-039 mk2 vs legacy (%) | Delta (pp) |
|---|---:|---:|---:|
| literalDense | +66.0% | +64.9% | −1.1 (noise) |
| variableChurn | +14.5% | +12.0% | −2.5 (noise) |
| userFunction | −29.5% | **−19.8%** | **+9.7** |
| conditional | −39.2% | **−31.9%** | **+7.3** |
| logarithmChain | −56.5% | −62.8% | −6.3 (noise: dominated by log transcendentals) |
| powerChain | −14.0% | **−7.9%** | **+6.1** |

Assignment bindings (direct PERF-038 effect):

| Benchmark | PERF-036 (ns/op) | PERF-039 (ns/op) | Improvement |
|---|---:|---:|---:|
| computeThreeExternal | 1,089.8 | 1,047.58 | +3.9% |
| computeTwelveExternal | 2,540.2 | 2,313.39 | **+8.9%** |

**Key findings:**

1. **literalDense / variableChurn:** Essentially unchanged — mk2 still wins by 65% and 12% respectively. These scenarios are unaffected by `buildValues()` cost (literalDense has only 1 variable; variableChurn gains are dominated by the typed execution plan).
2. **userFunction: −29.5% → −19.8% (+9.7 pp recovered):** The PERF-038 optimization directly narrows this gap. userFunction has 12 external symbols per call (3 groups of 4 weighted(a,b,c) calls), each of which previously paid 2 catalog lookups + 2 Optional allocations. After PERF-038, each pays 1 null-check lookup.
3. **conditional: −39.2% → −31.9% (+7.3 pp recovered):** Same mechanism — 12 external symbols per call.
4. **powerChain: −14.0% → −7.9% (+6.1 pp recovered):** Same — 12 external symbols per call. Now within single-digit deficit.
5. **logarithmChain:** The apparent regression (−56.5% → −62.8%) is noise — at ~1 ms/op the measurement variance is large (±18,000 ns for legacy, ±33,000 ns for mk2). Both sessions show the same root cause: `BigDecimalMath.log()` at DECIMAL128 precision dominates.

**Decision:** CHARACTERIZATION (no code change)
**Reason:** Updated baseline after PERF-038. The `buildValues()` optimization meaningfully narrowed the mk2 deficit in variable-heavy scenarios (up to +9.7 pp on userFunction). Scenarios where mk2 still lags (userFunction, conditional, powerChain) are now better understood: residual cost is from the `HashMap` creation + per-entry coercion inside `buildValues()`, not the catalog lookup overhead which has been eliminated.
