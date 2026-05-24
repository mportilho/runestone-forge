## PERF-003: Compute-path navigation allocation and single-pass vector aggregation

**Date:** 2026-05-24

**Scenario:** Validate two runtime-only optimizations after PERF-002 identified broad compute-path
slowdowns: remove per-property-chain `PropertyAccessEvaluator` allocation and make vector
aggregations accumulate in one pass instead of materializing an intermediate `List<BigDecimal>`.

**Machine:** OpenJDK 25.0.2 / Linux x86-64, -Xms1g -Xmx1g

**JMH config:** 3 forks x 5 warmup + 10 measurement iterations x 500 ms; `AverageTime / ns`; `-prof gc`

**Command:**

```shell
/home/marcelo/.agents/skills/performance-benchmark/scripts/run-jmh.sh . \
  "ObjectNavigationBenchmark|CollectionNavigationBenchmark|VectorMapTransformBenchmark" \
  "/tmp/performance-benchmark/expression-evaluator-compute-optimized-20260524.json"
```

**Artifacts:**

- Before JSON: `/tmp/performance-benchmark/expression-evaluator-current-20260523.json`
- After JSON: `/tmp/performance-benchmark/expression-evaluator-compute-optimized-20260524.json`
- Comparison: `/tmp/performance-benchmark/expression-evaluator-compute-optimized-comparison-20260524.md`
- Full tool output: `/home/marcelo/.local/share/opencode/tool-output/tool_e5811253d001mQ0WqGkPdugD7O`

### Focused Comparison vs PERF-002 Current State

| Benchmark | Before ns/op | After ns/op | Delta | B/op Before -> After | Status |
|---|---:|---:|---:|---:|---|
| collection.VectorMapTransformBenchmark.mapExtractPropertySum | 895.6 | 699.5 | +21.90% | 728 -> 312 | Significant gain |
| collection.VectorMapTransformBenchmark.mapComputedFieldSum | 1499.2 | 1204.5 | +19.65% | 1080 -> 640 | Significant gain |
| collection.VectorMapTransformBenchmark.mapEntryValuesSum | 1031.4 | 842.8 | +18.29% | 960 -> 472 | Significant gain |
| collection.VectorMapTransformBenchmark.mapChainedSum | 1431.3 | 1188.3 | +16.98% | 1344 -> 760 | Significant gain |
| navigation.ObjectNavigationBenchmark.typedNestedProperty | 216.6 | 186.7 | +13.78% | 104 -> 64 | Significant gain |
| collection.CollectionNavigationBenchmark.listFilterCount | 1058.6 | 916.2 | +13.45% | 312 -> 120 | Significant gain |
| collection.VectorMapTransformBenchmark.mapComputedFieldAvg | 2695.3 | 2340.1 | +13.18% | 2574 -> 1798 | Significant gain |
| collection.VectorMapTransformBenchmark.wildcardProjectionSum | 662.4 | 599.7 | +9.47% | 872 -> 816 | Minor improvement |
| navigation.ObjectNavigationBenchmark.typedMethodNoArg | 159.7 | 145.6 | +8.83% | 144 -> 104 | Minor improvement |
| navigation.ObjectNavigationBenchmark.reflectiveNestedProperty | 351.5 | 326.3 | +7.17% | 104 -> 64 | Minor improvement |
| navigation.ObjectNavigationBenchmark.typedMethodWithArgument | 231.1 | 216.2 | +6.46% | 152 -> 104 | Minor improvement |
| navigation.ObjectNavigationBenchmark.reflectiveMethodWithArgument | 326.7 | 310.0 | +5.11% | 176 -> 128 | Minor improvement |

**Average focused improvement:** +8.34%

**Decision:** ACCEPT - the targeted runtime changes reduce allocation and improve the affected
compute benchmarks, with the largest gains in vector map/aggregation and typed nested-property
navigation. `mapValuesCount` and `compileTypedNestedProperty` were slightly slower in this focused
run, but both are within measurement noise relative to their reported errors and are not direct
targets of the runtime changes.

---

## PERF-002: Current state after expression-evaluator refactor

**Date:** 2026-05-24

**Scenario:** Current-state JMH capture after the structural `expression-evaluator` refactor. This
run intentionally measures the current state only, using the already registered PERF-000/PERF-001
numbers as comparison references.

**Machine:** OpenJDK 25.0.2 / Linux x86-64, -Xms1g -Xmx1g

**JMH config:** 3 forks x 5 warmup + 10 measurement iterations x 500 ms; `AverageTime / ns`; `-prof gc`

**Command:**

```shell
/home/marcelo/.agents/skills/performance-benchmark/scripts/run-jmh.sh . ".*Benchmark" \
  "/tmp/performance-benchmark/expression-evaluator-current-20260523.json"
```

**Artifacts:**

- Raw JSON: `/tmp/performance-benchmark/expression-evaluator-current-20260523.json`
- Generated summary: `/tmp/performance-benchmark/expression-evaluator-current-20260523-summary.md`
- Full tool output: `/home/marcelo/.local/share/opencode/tool-output/tool_e57dabe91001EuGlhbUJ8Qh8n`

### Summary vs registered baselines

| Metric | Count |
|---|---:|
| Current JMH result rows | 98 |
| Rows comparable with PERF-000/PERF-001 | 49 |
| >= 10% slower | 49 |
| Within +/- 10% | 0 |
| >= 10% faster | 0 |

### Largest slowdowns vs registered baselines

| Benchmark | Baseline | Current | Delta | Baseline B/op | Current B/op | Source |
|---|---:|---:|---:|---:|---:|---|
| navigation.ObjectNavigationBenchmark.compileTypedMethodWithArgument | 29972.42 | 75215.05 | +150.9% | 15144.8 | 16409.0 | PERF-000 |
| navigation.ObjectNavigationBenchmark.compileReflectiveMethodWithArgument | 24229.99 | 51604.10 | +113.0% | 14063.5 | 15212.5 | PERF-000 |
| navigation.ObjectNavigationBenchmark.compileTypedNestedProperty | 16189.80 | 33261.86 | +105.4% | 11372.4 | 12266.5 | PERF-000 |
| collection.CollectionNavigationBenchmark.listFilterCount | 595.36 | 1058.63 | +77.8% | 216.0 | 312.0 | PERF-001 |
| collection.VectorMapTransformBenchmark.mapEntryValuesSum | 606.22 | 1031.41 | +70.1% | 696.0 | 960.0 | PERF-001 |
| collection.CollectionNavigationBenchmark.deepScanCount | 1402.36 | 2283.80 | +62.9% | 528.0 | 528.0 | PERF-001 |
| collection.CollectionNavigationBenchmark.indexAccess | 70.85 | 115.09 | +62.4% | 64.0 | 64.0 | PERF-001 |
| collection.VectorMapTransformBenchmark.mapChainedSum | 893.13 | 1431.35 | +60.3% | 1080.0 | 1344.0 | PERF-001 |
| collection.VectorMapTransformBenchmark.mapComputedFieldAvg | 1747.99 | 2695.34 | +54.2% | 2381.7 | 2573.7 | PERF-001 |
| collection.VectorMapTransformBenchmark.mapComputedFieldSum | 974.40 | 1499.19 | +53.9% | 1224.0 | 1080.0 | PERF-001 |

**Decision:** INVESTIGATE - this is a current-only run, not a same-session before/after A/B. However,
the comparison against the registered baselines shows broad slowdown across every comparable row,
including benchmarks with unchanged allocation. The next performance task should rerun a focused
subset repeatedly and profile navigation/compilation hot paths before changing code.

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
