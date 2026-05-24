# Compute Performance Plan

This document lists candidate improvements for runtime paths reached by
`MathExpression.compute(...)`, `LogicalExpression.compute(...)`, and
`AssignmentExpression.compute(...)`.

The scope here is runtime evaluation only. Compilation benchmarks such as
`compileTypedMethodWithArgument` are intentionally excluded except when a runtime
change also affects the planning model.

## Current Runtime Path

```text
MathExpression.compute(...) / LogicalExpression.compute(...)
  -> ExpressionRuntimeSupport.compute*
  -> createExecutionScope(...)
  -> MathEvaluator / LogicalEvaluator
  -> AbstractObjectEvaluator.evaluateExpr(...)
  -> operators, functions, navigation, filters, maps, and aggregations
```

## Measurement Context

The current-state JMH run recorded in `docs/perf/performance-history.md` shows
slowdowns across all rows comparable with PERF-000/PERF-001. This is not a
same-session A/B result, but the signal is broad enough to justify focused
runtime investigation.

Relevant current artifacts:

- Raw JSON: `/tmp/performance-benchmark/expression-evaluator-current-20260523.json`
- Summary: `/tmp/performance-benchmark/expression-evaluator-current-20260523-summary.md`
- Performance history entry: `docs/perf/performance-history.md`, `PERF-002`

## Optimization Candidates

### 1. Avoid per-chain `PropertyAccessEvaluator` allocation

Priority: High

Runtime area:

- `internal.navigation.PropertyChainOps`
- `internal.navigation.PropertyAccessEvaluator`

Current concern:

- `PropertyChainOps.evaluatePropertyChain(...)` creates a new `PropertyAccessEvaluator` for each property-chain evaluation.
- This directly affects object navigation benchmarks such as `typedNestedProperty`, `typedMethodNoArg`, `typedMethodWithArgument`, `reflectiveNestedProperty`, and `reflectiveMethodWithArgument`.
- The current benchmark shows higher `B/op` for several navigation paths, which is consistent with extra per-evaluation allocation.

Recommended change:

- Make `PropertyAccessEvaluator` stateless/static where possible, or reuse one instance per evaluator instead of allocating it inside each property-chain evaluation.
- Keep the call path direct; do not introduce a generic navigation service that adds more dispatch.

Validation:

- Run `ObjectNavigationBenchmark` before and after.
- Accept only if navigation `ns/op` improves and `B/op` does not increase.

### 2. Avoid `Object[]` allocation in reflective method invocation

Priority: High

Runtime area:

- `internal.navigation.PropertyAccessEvaluator.invokeReflectiveMethod(...)`

Current concern:

- Reflective method calls allocate an `Object[]` before dispatch even when arity is small.
- The later invocation path already has arity-specific branches, but the array has already been created.

Recommended change:

- Add direct arity-specific evaluation paths for reflective methods with arities `0..6`.
- Keep the existing array path only for arities greater than `6`.

Validation:

- Run `ObjectNavigationBenchmark.reflectiveMethodWithArgument`.
- Confirm `B/op` drops or stays stable, and `ns/op` improves.

### 3. Add arity fast paths for collection functions

Priority: Medium-high

Runtime area:

- `internal.navigation.CollectionFunctionEvaluator`

Current concern:

- `CollectionFunctionEvaluator.evaluate(...)` always builds an `Object[]` for the implicit collection/map argument plus explicit arguments.
- This affects `customFunctionCount` and collection-function navigation paths.

Recommended change:

- Add fast paths for total arities `1..6`, preserving the implicit `current` argument.
- Keep array allocation only for arities greater than `6`.
- Do not use a generic invocation helper unless JMH proves it preserves the existing fast paths.

Validation:

- Run `CollectionNavigationBenchmark.customFunctionCount`.
- Include `ObjectEvaluatorBenchmark.userFunction` to ensure normal function calls do not regress.

### 4. Make vector aggregations single-pass

Priority: High

Runtime area:

- `internal.navigation.VectorAggregationEvaluator`

Current concern:

- Aggregation currently materializes a `List<BigDecimal>` before computing `sum`, `avg`, `min`, `max`, or `prod`.
- This creates avoidable allocation and an extra traversal.

Recommended change:

- Accumulate directly while iterating over the source list.
- For transformed aggregations, push the filter context and immediately aggregate the transformed value.
- Preserve existing empty-list semantics: `SUM -> BigDecimal.ZERO`, `PROD -> BigDecimal.ONE`, others -> `null`.

Validation:

- Run `VectorMapTransformBenchmark`.
- Pay special attention to `mapComputedFieldAvg`, `mapComputedFieldSum`, and `wildcardProjectionSum`.

### 5. Fuse filter/map/projection with count or aggregation

Priority: Medium

Runtime area:

- `internal.navigation.CollectionPredicateTransformEvaluator`
- `internal.navigation.CollectionAccessOps`
- `internal.navigation.VectorAggregationEvaluator`
- planning classes under `internal.execution.plan`

Current concern:

- Some expressions materialize intermediate collections and then immediately count or aggregate them.
- Examples include filter followed by count, map followed by sum, and projection followed by aggregation.

Recommended change:

- Add planning-time detection for common chains such as `filter -> count`, `filter -> sum`, `map -> sum`, and `projection -> aggregation`.
- Execute these as fused operations that stream through the source once.
- Keep this as a second-stage optimization because it changes planning shape more than the earlier local changes.

Validation:

- Run `CollectionNavigationBenchmark` and `VectorMapTransformBenchmark`.
- Confirm semantic parity with existing navigation tests before relying on JMH.

### 6. Add fast path for read-only/folded compute calls

Priority: Medium

Runtime area:

- `internal.runtime.ExpressionRuntimeSupport`
- `internal.execution.eval.ExecutionScope`

Current concern:

- Every non-audited `compute()` call creates an `ExecutionScope`, even for read-only expressions with no assignments and no user values.
- Fully folded expressions still show a small fixed allocation floor.

Recommended change:

- Consider a cached read-only scope only when the expression is safe: no assignments, no audit, no user overrides, and no mutable dynamic-literal cache requirements.
- Alternatively, add a direct return path for fully folded result expressions.
- Prefer the direct folded-result path first because it is lower risk.

Validation:

- Run `ExpressionEvaluatorExecutionPlanBenchmark.computeMathLiteralDense` and `computeLogicalMixedLiteralDense`.
- Also run `BooleanValueBenchmark` to ensure normal read-only expressions do not regress.

### 7. Specialize symbol reads

Priority: Medium

Runtime area:

- `internal.execution.eval.SymbolValueEvaluator`
- `internal.execution.eval.ExecutionScope`
- `internal.execution.plan.SymbolReadPlanner`

Current concern:

- Every identifier read checks whether the symbol is `@`, then routes through `ExecutionScope.find(SymbolRef)`.
- `ExecutionScope.find(...)` branches on symbol kind and mutable/read-only layers for every read.

Recommended change:

- During planning, create specialized executable reads for external, internal, and current-element symbols.
- Alternatively, add specialized scope methods such as `findExternal(index)` and `findInternal(index)` and use them from specialized executable nodes.

Validation:

- Run `BooleanValueBenchmark`, `MembershipBenchmark`, `ObjectEvaluatorBenchmark.variableChurn`, and assignment binding benchmarks.

### 8. Reduce `ThreadLocal` use for current-element access

Priority: Low-medium

Runtime area:

- `internal.execution.eval.FilterContextStack`
- `internal.execution.eval.SymbolValueEvaluator`
- collection filter/map evaluators

Current concern:

- Every `@` read currently reaches `FilterContextStack.INSTANCE.get().peek()`.
- This may matter in filter/map predicates with many element reads.

Recommended change:

- Consider storing current filter context in `ExecutionScope`, or passing an explicit evaluation context through filter/map evaluation.
- Treat this as a later change because it touches more call sites and can complicate concurrency semantics.

Validation:

- Run filter-heavy `CollectionNavigationBenchmark` and `VectorMapTransformBenchmark` cases.
- Use JMH before accepting; this is not a guaranteed win.

### 9. Re-check evaluator dispatch only after profiling

Priority: Low until proven by profiler

Runtime area:

- `internal.execution.eval.AbstractObjectEvaluator`
- `internal.execution.eval.StructuredExpressionEvaluator`
- `internal.execution.eval.NodeEvaluator`

Current concern:

- The current slowdown includes cases with unchanged allocation, so CPU dispatch overhead is possible.
- Older measurements indicated the `NodeEvaluator` callback was not a regression.

Recommended change:

- Do not inline or roll back evaluator structure without profiler evidence.
- Use JFR or JMH profilers first. Only change this area if dispatch shows up as a top runtime cost.

Validation:

- Run focused JMH with profiler support before making code changes.

## Recommended Order

1. Avoid per-chain `PropertyAccessEvaluator` allocation.
2. Avoid `Object[]` allocation in reflective method invocation.
3. Add arity fast paths for collection functions.
4. Make vector aggregations single-pass.
5. Add folded/read-only compute fast path if allocation floor remains relevant.
6. Consider fused filter/map/projection plus aggregation/count.
7. Specialize symbol reads.
8. Rework current-element context only if profiling proves it is hot.
9. Revisit evaluator dispatch only with profiler evidence.

## Focused Benchmark Command

Use a focused subset for each change instead of running the full suite every time:

```shell
/home/marcelo/.agents/skills/performance-benchmark/scripts/run-jmh.sh . \
  "ObjectNavigationBenchmark|CollectionNavigationBenchmark|VectorMapTransformBenchmark|BooleanValueBenchmark|ObjectEvaluatorBenchmark|MembershipBenchmark" \
  "/tmp/performance-benchmark/expression-evaluator-compute-after.json"
```

## Acceptance Criteria

- Accept if affected benchmarks improve by at least `10%` without regressions in adjacent runtime paths.
- Accept `1-10%` only for small, low-risk changes with stable or lower `B/op`.
- Reject or adjust if `B/op` increases on hot paths or if navigation/function invocation regress.
- Always run the existing functional tests that cover the touched runtime area before trusting JMH.

## Guardrails

- Do not optimize compile-time benchmarks as part of this runtime plan unless the same change also affects `compute()`.
- Preserve arity-specific invocation fast paths for arities `0..6`.
- Avoid generic helpers that allocate arrays, varargs, or policy objects on hot paths.
- Keep reflection and type metadata discovery cached by type and policy.
- Avoid invasive evaluator rewrites without JFR/JMH profiler evidence.
