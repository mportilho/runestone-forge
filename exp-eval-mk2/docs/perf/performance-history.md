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
