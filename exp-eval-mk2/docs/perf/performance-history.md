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
