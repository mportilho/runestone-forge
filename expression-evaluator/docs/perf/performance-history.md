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
