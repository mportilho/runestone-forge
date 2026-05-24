## Benchmark Organization

Current benchmark packages and JMH commands are documented in [`benchmark-organization.md`](benchmark-organization.md). Historical entries keep the benchmark labels emitted by JMH at the time of each run, even when later package reorganizations change fully qualified names.

---

## PERF-001: NodeEvaluator callback — no regression + CollectionNavigation baseline

**Date:** 2026-04-21

**Scenario:** Confirm that extracting collection-navigation logic into `CollectionNavigationOps` /
`PropertyChainOps` and threading calls through the `NodeEvaluator` functional interface does not
regress hot paths. The interface is monomorphic at each call site (`this::evaluateExpr` captured in
`AbstractObjectEvaluator`), so JIT can speculate-inline it.

**Hypothesis:** NodeEvaluator indirection adds zero measurable overhead; JIT inlines the single
target. VectorMapTransform benchmarks should match or beat PERF-000. CollectionNavigation
benchmarks are recorded as a new baseline (no prior numbers).

**Machine:** OpenJDK 25.0.2 / Linux x86-64, -Xms1g -Xmx1g  
**JMH config:** 3 forks × 5 warmup + 10 measurement iterations × 500 ms each; `AverageTime / ns`

### VectorMapTransform — vs PERF-000

| Benchmark | PERF-000 (ns/op) | This run (ns/op) | Delta | B/op |
|---|---:|---:|---:|---:|
| collection.VectorMapTransformBenchmark.wildcardProjectionSum | 501.48 | 435.74 | **−13.1%** | 872.0 |
| collection.VectorMapTransformBenchmark.mapExtractPropertySum | 732.43 | 676.90 | −7.6% | 968.0 |
| collection.VectorMapTransformBenchmark.mapEntryValuesSum | 733.13 | 606.22 | **−17.3%** | 696.0 |
| collection.VectorMapTransformBenchmark.mapChainedSum | 979.91 | 893.13 | −8.9% | 1080.0 |
| collection.VectorMapTransformBenchmark.mapComputedFieldSum | 1076.35 | 974.40 | −9.5% | 1224.0 |
| collection.VectorMapTransformBenchmark.mapComputedFieldAvg | 1933.02 | 1747.99 | −9.6% | 2381.7 |

All paths improved. No regression.

### CollectionNavigation — new baseline (applyFilter, applyAggregation paths)

| Benchmark | Score (ns/op) | B/op |
|---|---:|---:|
| collection.CollectionNavigationBenchmark.indexAccess | 70.85 | 64.0 |
| collection.CollectionNavigationBenchmark.mapValuesCount | 74.36 | 64.0 |
| collection.CollectionNavigationBenchmark.customFunctionCount | 297.31 | 296.0 |
| collection.CollectionNavigationBenchmark.listFilterCount | 595.36 | 216.0 |
| collection.CollectionNavigationBenchmark.mapFilterCount | 736.64 | 352.0 |
| collection.CollectionNavigationBenchmark.deepScanCount | 1402.36 | 528.0 |

**Decision:** ACCEPT — all VectorMapTransform paths improved ≥ 7.6 %; CollectionNavigation
recorded as first-run baseline.

**Notes:**
- `mapEntryValuesSum` allocation dropped from 864 B/op to 696 B/op alongside the 17 % speedup —
  consistent with a leaner code path post-refactor.
- `NodeEvaluator` lambda (`this::evaluateExpr`) is monomorphic per evaluator instance; C2 inlines
  it after the warmup period, as confirmed by the absence of regression.

---

## PERF-000: Baseline

**Date:** 2026-04-21

**Scenario:** Baseline capture for the current `expression-evaluator` for key tests. The
remaining suite exercises expression parsing, evaluation, bindings, navigation, membership,
vector transforms and destructuring.

**Hypothesis:** First measurement of the filtered current state. No before/after comparison exists
yet for this exact benchmark set.

**Machine:** OpenJDK 25.0.2 / Linux x86-64, -Xms1g -Xmx1g  
**JMH config:** 3 forks × 5 warmup + 10 measurement iterations × 500 ms each; `AverageTime / ns`

| Benchmark | Score (ns/op) | B/op |
|---|---:|---:|
| collection.MembershipBenchmark.notInMiss | 14.12 | 40.0 |
| collection.MembershipBenchmark.numericMiss | 14.15 | 40.0 |
| collection.MembershipBenchmark.stringMiss | 14.18 | 40.0 |
| collection.MembershipBenchmark.numericLargeHit | 14.24 | 40.0 |
| collection.MembershipBenchmark.numericLargeMiss | 14.26 | 40.0 |
| collection.MembershipBenchmark.numericHitLast | 14.59 | 40.0 |
| collection.MembershipBenchmark.numericHitFirst | 15.49 | 40.0 |
| bindings.BindingsOverlayBenchmark.noDefaultsOneOverride | 79.73 | 104.0 |
| bindings.BindingsOverlayBenchmark.noDefaultsTwoOverrides | 107.16 | 104.0 |
| navigation.ObjectNavigationBenchmark.typedMethodNoArg | 119.43 | 128.0 |
| collection.DestructuringAssignmentBenchmark.vectorLiteralNoAudit | 135.67 | 152.0 |
| navigation.ObjectNavigationBenchmark.typedNestedProperty | 161.80 | 88.0 |
| navigation.ObjectNavigationBenchmark.typedMethodWithArgument | 165.22 | 128.0 |
| collection.MembershipBenchmark.externalVectorHit | 206.26 | 64.0 |
| bindings.AssignmentExpressionBindingsBenchmark.computeNoExternal | 214.73 | 392.0 |
| navigation.ObjectNavigationBenchmark.reflectiveMethodWithArgument | 245.21 | 152.0 |
| navigation.ObjectNavigationBenchmark.reflectiveNestedProperty | 292.00 | 88.0 |
| collection.DestructuringAssignmentBenchmark.vectorLiteralWithAudit | 298.97 | 480.0 |
| collection.DestructuringAssignmentBenchmark.spread3SlotsNoAudit | 302.81 | 208.0 |
| bindings.BindingsOverlayBenchmark.manyDefaultsNoOverrides | 482.85 | 520.0 |
| collection.DestructuringAssignmentBenchmark.spread3SlotsWithAudit | 493.71 | 568.0 |
| collection.VectorMapTransformBenchmark.wildcardProjectionSum | 501.48 | 872.0 |
| bindings.AssignmentExpressionBindingsBenchmark.computeThreeExternal | 509.40 | 624.0 |
| collection.DestructuringAssignmentBenchmark.spread5SlotsNoAudit | 523.99 | 304.0 |
| bindings.BindingsOverlayBenchmark.manyDefaultsOneOverride | 606.28 | 720.0 |
| bindings.BindingsOverlayBenchmark.manyDefaultsTwoOverrides | 625.23 | 720.0 |
| evaluation.BooleanValueBenchmark.boolChain | 669.75 | 560.0 |
| collection.VectorMapTransformBenchmark.mapExtractPropertySum | 732.43 | 968.0 |
| collection.VectorMapTransformBenchmark.mapEntryValuesSum | 733.13 | 864.0 |
| collection.DestructuringAssignmentBenchmark.spread5SlotsWithAudit | 748.98 | 776.0 |
| evaluation.ObjectEvaluatorBenchmark.conditional | 783.08 | 896.0 |
| evaluation.BooleanValueBenchmark.boolWide | 921.17 | 600.0 |
| collection.VectorMapTransformBenchmark.mapChainedSum | 979.91 | 1080.0 |
| collection.VectorMapTransformBenchmark.mapComputedFieldSum | 1076.35 | 1224.0 |
| evaluation.ObjectEvaluatorBenchmark.variableChurn | 1103.97 | 1296.0 |
| evaluation.ObjectEvaluatorBenchmark.userFunction | 1136.58 | 976.0 |
| bindings.AssignmentExpressionBindingsBenchmark.computeTwelveExternal | 1223.11 | 1152.0 |
| evaluation.ObjectEvaluatorBenchmark.literalDense | 1544.81 | 2656.0 |
| collection.VectorMapTransformBenchmark.mapComputedFieldAvg | 1933.02 | 2381.7 |
| evaluation.ObjectEvaluatorBenchmark.powerChain | 2606.76 | 2096.0 |
| navigation.ObjectNavigationBenchmark.compileTypedNestedProperty | 16189.80 | 11372.4 |
| navigation.ObjectNavigationBenchmark.compileReflectiveMethodWithArgument | 24229.99 | 14063.5 |
| navigation.ObjectNavigationBenchmark.compileTypedMethodWithArgument | 29972.42 | 15144.8 |

**Decision:** BASELINE (first measurement for this filtered benchmark set)

**Notes:**
- Allocation is stable within each scenario; the `B/op` values above are the `gc.alloc.rate.norm`
  measurements captured by JMH.
