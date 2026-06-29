## Benchmark Organization

Current benchmark packages and JMH commands are documented in [`benchmark-organization.md`](benchmark-organization.md). Historical entries keep the benchmark labels emitted by JMH at the time of each run, even when later package reorganizations change fully qualified names.

---

## PERF-006: Compile-time Constant Folding policy extraction

**Date:** 2026-06-29

**Scenario:** Validate stage 3 of `docs/prd/expression-runtime-performance-deepening.md`:
constant-folding rules, Fold Barriers, folded symbol reads, folded function calls, folded vectors,
and property-chain prefix folding were extracted from `ExecutionPlanBuilder` into the compile-time
`ConstantFoldingPolicy`. Runtime evaluators and executable hot-path classes were not changed.

**Hypothesis:** Planning and compile-path allocation should stay materially neutral while improving
locality of folding rules. Steady-state `compute()` allocation should remain unchanged because the
execution hot path was not modified.

**Machine:** OpenJDK 25.0.3 / Linux x86-64, -Xms1g -Xmx1g
**JMH config:** 3 forks x 5 warmup + 10 measurement iterations x 500 ms each; `AverageTime / ns`; GC profiler enabled

**Baseline:** Fresh detached worktree at `HEAD` before the stage 3 change, created under
`/tmp/opencode/runestone-forge-stage3-baseline`.

| Benchmark | Before (ns/op) | After (ns/op) | Delta | B/op Before | B/op After |
|---|---:|---:|---:|---:|---:|
| planning.ExpressionEvaluatorExecutionPlanBenchmark.compileLogicalMixedLiteralDense | 134,639.7 | 130,002.0 | +3.44% | 81,511 | 81,473 |
| planning.ExpressionEvaluatorExecutionPlanBenchmark.compileMathLiteralDense | 1,456,425.5 | 1,470,554.2 | -0.97% | 761,973 | 762,496 |
| planning.ExpressionEvaluatorExecutionPlanBenchmark.computeLogicalMixedLiteralDense | 8.6 | 8.7 | -1.16% | 40 | 40 |
| planning.ExpressionEvaluatorExecutionPlanBenchmark.computeMathLiteralDense | 8.8 | 9.6 | -8.32% | 40 | 40 |
| startup.compilation.CompilePathAllocationBenchmark.compileFunctionCacheHit | 13.6 | 13.2 | +2.92% | 0 | 0 |
| startup.compilation.CompilePathAllocationBenchmark.compileFunctionCacheMiss | 38,576.5 | 38,190.9 | +1.00% | 53,168 | 52,550 |
| startup.compilation.CompilePathAllocationBenchmark.compileSimpleCacheHit | 13.4 | 13.5 | -0.44% | 0 | 0 |

**Decision:** ACCEPT with residual noise risk. The compile-path benchmark accepted the change and
cache-miss allocation improved (`53,168 -> 52,550 B/op`). Planning compile measurements were within
minor movement and allocation remained materially stable. The planning report flagged runtime
`compute*` controls as throughput regressions, but those measurements kept identical allocation
(`40 B/op`), included a wide `computeMathLiteralDense` error interval caused by outliers, and no
runtime evaluator or executable hot-path class changed in this stage. Treat those controls as
run-to-run noise unless a future dedicated evaluation benchmark reproduces the slowdown.

**Notes:**

- Raw JSON results were saved to `/tmp/performance-benchmark/expeval-stage3-planning-before.json`,
  `/tmp/performance-benchmark/expeval-stage3-planning-after.json`,
  `/tmp/performance-benchmark/expeval-stage3-compilation-before.json`, and
  `/tmp/performance-benchmark/expeval-stage3-compilation-after.json`.
- Comparison reports were saved to `/tmp/performance-benchmark/expeval-stage3-planning-comparison.md`
  and `/tmp/performance-benchmark/expeval-stage3-compilation-comparison.md`.
- A first same-worktree run also showed unchanged `compute()` allocation and similar compile-path
  behavior, but its artifacts were intentionally superseded by the retained detached-worktree run.

---

## PERF-005: Compile-time member binding handoff

**Date:** 2026-06-29

**Scenario:** Validate stage 2 of `docs/prd/expression-runtime-performance-deepening.md`:
typed object member binding is resolved once during semantic resolution and handed to
`ExecutionPlanBuilder` as compile-time data. The execution plan still stores only runtime-needed
member data: method/property handles, parameter types, return type, member name, and safe-navigation
flag.

**Hypothesis:** Type-hinted object navigation should preserve steady-state evaluation allocation and
avoid material throughput regressions. Compile-path allocation may move slightly because the binding
handoff is compile-only and is not retained by `ExecutionPlan` or compiled expression cache entries.

**Machine:** OpenJDK 25.0.3 / Linux x86-64, -Xms1g -Xmx1g
**JMH config:** 3 forks x 5 warmup + 10 measurement iterations x 500 ms each; `AverageTime / ns`; GC profiler enabled

**Baseline:** Fresh detached worktree at `HEAD` before the stage 2 change, created under
`/tmp/opencode/runestone-forge-etapa2-baseline`.

| Benchmark | Before (ns/op) | After (ns/op) | Delta | B/op Before | B/op After |
|---|---:|---:|---:|---:|---:|
| navigation.object.ObjectNavigationBenchmark.buildTypedEnvironment | 54,524.7 | 56,338.6 | -3.33% | 48,147 | 48,769 |
| navigation.object.ObjectNavigationBenchmark.compileReflectiveMethodWithArgument | 10,249.0 | 10,007.9 | +2.35% | 14,111 | 14,207 |
| navigation.object.ObjectNavigationBenchmark.compileTypedMethodWithArgument | 12,983.1 | 12,754.8 | +1.76% | 15,108 | 14,917 |
| navigation.object.ObjectNavigationBenchmark.compileTypedNestedProperty | 7,175.2 | 6,831.9 | +4.78% | 11,456 | 12,000 |
| navigation.object.ObjectNavigationBenchmark.reflectiveMethodWithArgument | 209.2 | 212.6 | -1.62% | 144 | 144 |
| navigation.object.ObjectNavigationBenchmark.reflectiveNestedProperty | 225.6 | 217.5 | +3.57% | 88 | 72 |
| navigation.object.ObjectNavigationBenchmark.typedMethodNoArg | 99.4 | 95.7 | +3.77% | 128 | 128 |
| navigation.object.ObjectNavigationBenchmark.typedMethodWithArgument | 142.3 | 139.4 | +2.09% | 128 | 128 |
| navigation.object.ObjectNavigationBenchmark.typedNestedProperty | 138.1 | 141.7 | -2.62% | 88 | 88 |

**Decision:** ACCEPT - steady-state type-hinted object navigation preserved allocation, typed method
navigation improved modestly, and the slower runtime measurements were small with unchanged
allocation. The compile-only binding handoff increased allocation in `compileTypedNestedProperty`
(`11,456 -> 12,000 B/op`) while improving its measured throughput; this is accepted because the
binding map is not retained in the execution plan or expression cache.

**Notes:**

- Raw JSON results were saved to `/tmp/performance-benchmark/expeval-stage2-before.json` and
  `/tmp/performance-benchmark/expeval-stage2-after.json`.
- The comparison report was saved to `/tmp/performance-benchmark/expeval-stage2-comparison.md`.
- `buildTypedEnvironment` does not execute the changed compile handoff and is treated as a control
  measurement for run-to-run noise.

---

## PERF-004: Function invocation audit policy for collection functions

**Date:** 2026-06-29

**Scenario:** Validate the first stage of `docs/prd/expression-runtime-performance-deepening.md`:
centralized Function Invocation, coercion, and Audit Trail policy while preserving the existing
arity-specialized no-audit fast paths. The change also makes Collection Function calls visible in
`computeWithAudit()` without allocating audit argument arrays in ordinary `compute()`.

**Hypothesis:** Normal function invocation and collection-function invocation should keep unchanged
allocation in `compute()`. Audit paths may keep their existing audit-event allocation, but should not
materially regress throughput.

**Machine:** OpenJDK 25.0.3 / Linux x86-64, -Xms1g -Xmx1g
**JMH config:** 3 forks x 5 warmup + 10 measurement iterations x 500 ms each; `AverageTime / ns`; GC profiler enabled

**Baseline:** Fresh detached worktree at `HEAD` before the stage 1 change, created under
`/tmp/opencode/runestone-forge-etapa1-baseline`.

| Benchmark | Before (ns/op) | After (ns/op) | Delta | B/op Before | B/op After |
|---|---:|---:|---:|---:|---:|
| evaluation.core.ObjectEvaluatorBenchmark.userFunction | 869.22 | 839.74 | +3.4% | 976.0 | 976.0 |
| navigation.collection.CollectionNavigationBenchmark.customFunctionCount | 238.63 | 225.25 | +5.6% | 272.0 | 272.0 |
| runtime.audit.AuditOverheadBenchmark.userFunctionNoAudit | 865.23 | 872.19 | -0.8% | 976.0 | 976.0 |
| runtime.audit.AuditOverheadBenchmark.userFunctionWithAudit | 1011.78 | 1014.62 | -0.3% | 1712.0 | 1712.0 |
| runtime.audit.AuditOverheadBenchmark.assignedVariableNoAudit | 734.54 | 721.54 | +1.8% | 1160.0 | 1160.0 |
| runtime.audit.AuditOverheadBenchmark.assignedVariableWithAudit | 931.52 | 903.33 | +3.0% | 1712.0 | 1712.0 |
| runtime.audit.AuditOverheadBenchmark.variableChurnNoAudit | 772.45 | 778.52 | -0.8% | 1296.0 | 1296.0 |
| runtime.audit.AuditOverheadBenchmark.variableChurnWithAudit | 945.69 | 964.55 | -2.0% | 1904.0 | 1904.0 |

**Additional controls:**

| Benchmark group | Result |
|---|---|
| `runtime.functions.MathFunctionsBenchmark` | 12 parameterized cases were within -1.8% to +5.6%; most allocation stayed unchanged. The largest allocation movement was `testVariance[size=5000]` (`500777 -> 540367 B/op`), a direct catalog-function control not touched by this change and with nearly identical runtime (`-0.03%`). |
| `runtime.functions.StringFunctionsRegexBenchmark` | All four string controls measured slower by 4.4% to 5.8%, with unchanged `B/op`. These direct functions were not modified by the stage 1 change, so this is treated as run-to-run noise rather than a causal regression. |

**Decision:** ACCEPT — the changed evaluation hot paths preserved allocation, ordinary `compute()` did
not add audit allocation, and the collection-function hot path improved in this run. The only slower
measurements were either below 2% on audit/variable controls or came from untouched string controls
with stable allocation.

**Notes:**

- Raw JSON results were saved to `/tmp/performance-benchmark/expeval-stage1-before.json` and
  `/tmp/performance-benchmark/expeval-stage1-after.json`.
- The comparison report was saved to `/tmp/performance-benchmark/expeval-stage1-comparison.md`.
- The Maven local cache needed the project parent POM installed before the baseline JMH run because
  the helper runs from the module directory.

---

## PERF-003: RuntimeInvocationSupport consolidation

**Date:** 2026-05-24

**Scenario:** Validate the runtime invocation refactor that introduced
`RuntimeInvocationSupport` and centralized hot-path invocation for normal function calls,
collection functions, and property-chain method handles while preserving arity-specific fast paths
for 0 through 6 arguments.

**Hypothesis:** The refactor should not materially regress latency or allocation. Collection
function invocation may allocate less because the new helper avoids the previous `Object[]` path for
common arities with an implicit receiver/current collection argument.

**Machine:** OpenJDK 25.0.2 / Linux x86-64, -Xms1g -Xmx1g  
**JMH config:** 3 forks x 5 warmup + 10 measurement iterations x 500 ms each; `AverageTime / ns`; GC profiler enabled

**Baseline:** Fresh detached worktree at `HEAD` before the `RuntimeInvocationSupport` change, created
under `/tmp/opencode/runestone-forge-invocation-baseline`.

| Benchmark | Before (ns/op) | After (ns/op) | Delta | B/op Before | B/op After |
|---|---:|---:|---:|---:|---:|
| navigation.collection.CollectionNavigationBenchmark.customFunctionCount | 400.85 | 404.43 | -0.9% | 296.0 | 272.0 |
| navigation.object.ObjectNavigationBenchmark.typedMethodWithArgument | 190.18 | 199.29 | -4.8% | 128.0 | 128.0 |
| runtime.audit.AuditOverheadBenchmark.userFunctionNoAudit | 1329.67 | 1293.58 | +2.7% | 976.0 | 976.0 |
| runtime.audit.AuditOverheadBenchmark.userFunctionWithAudit | 1648.50 | 1607.87 | +2.5% | 1712.0 | 1712.0 |

**Decision:** ADJUST — the refactor is allocation-positive for collection functions and slightly
faster for direct user-function evaluation, but `typedMethodWithArgument` regressed by 4.8% in this
run. This is below the 10% hard-regression threshold but enough to keep the result as unresolved
risk for a hot method-handle path.

**Notes:**

- `customFunctionCount` allocation dropped by 24 B/op (`296 -> 272`) because the common collection
  function path no longer needs an argument array.
- Direct function evaluation (`userFunctionNoAudit` / `userFunctionWithAudit`) improved modestly with
  unchanged allocation.
- `typedMethodWithArgument` likely pays for the extra helper call shape or reduced inlining in the
  method-handle-with-receiver path; consider either restoring the local specialized switch for
  `ExecutableMethodInvoke` or adding a targeted benchmark/profiler run before accepting the helper
  for property-chain methods.
- Raw JSON and comparison files were generated under `/tmp/performance-benchmark/` and then removed
  by the comparison helper's `--cleanup` mode; numbers above are copied from the JMH and comparison
  output of this run.

---

## PERF-002: Runtime support cached on compile cache hits

**Date:** 2026-05-24

**Scenario:** Validate the change that stops exposing `RuntimeServices` from
`ExpressionEnvironment` and moves runtime-service ownership into the internal compilation/cache
path. `ExpressionCompilationCache` now caches `ExpressionRuntimeSupport`, so cache hits should no
longer allocate `RuntimeServices`, `RuntimeCoercionService`, or evaluator wrappers.

**Hypothesis:** Compile cache hits should become allocation-free and materially faster. Cache misses
should remain within noise because they still execute the full parse, semantic resolution, plan
building, and runtime-support construction pipeline.

**Machine:** OpenJDK 25.0.2 / Linux x86-64, -Xms1g -Xmx1g  
**JMH config:** 3 forks x 5 warmup + 10 measurement iterations x 500 ms each; `AverageTime / ns`; GC profiler enabled

**Baseline:** Fresh `HEAD` worktree before the runtime-cache change, created under
`/tmp/opencode/runestone-forge-benchmark-baseline`.

| Benchmark | Before (ns/op) | After (ns/op) | Delta | B/op Before | B/op After |
|---|---:|---:|---:|---:|---:|
| startup.compilation.CompilePathAllocationBenchmark.compileSimpleCacheHit | 81.31 | 14.72 | **-81.9%** | 160.0 | ~0 |
| startup.compilation.CompilePathAllocationBenchmark.compileFunctionCacheHit | 85.38 | 15.47 | **-81.9%** | 160.0 | ~0 |
| startup.compilation.CompilePathAllocationBenchmark.compileFunctionCacheMiss | 98,745.25 | 96,367.22 | -2.4% | 53,503.2 | 53,459.6 |

**Decision:** ACCEPT — cache-hit paths improved by about 82% and eliminated the measured 160 B/op
allocation. Cache-miss time and allocation stayed effectively unchanged.

**Notes:**

- Results saved to `/tmp/performance-benchmark/expeval-startup-compilation-runtime-cache-before.json`
  and `/tmp/performance-benchmark/expeval-startup-compilation-runtime-cache-after.json`.
- Comparison report saved to
  `/tmp/performance-benchmark/expeval-startup-compilation-runtime-cache-comparison.md`.
- `compileFunctionCacheMiss` has high timing variance in both runs; allocation stayed stable and the
  measured delta is not treated as the main optimization signal.

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
