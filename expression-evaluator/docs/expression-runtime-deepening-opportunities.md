# Expression Runtime Deepening Opportunities

This note records seven architecture opportunities for improving the `expression-evaluator` Expression Runtime with a performance and low-GC lens.

The opportunities use the domain vocabulary from the repository `CONTEXT.md` and the architecture vocabulary from the codebase-design glossary: module, interface, implementation, depth, deep, shallow, seam, adapter, leverage, locality, and deletion test.

No ADRs were found under `docs/adr/` when these opportunities were captured.

## 1. Deepen the read-only Execution Scope path

**Recommendation strength:** Strong

**Domain terms:** Expression Runtime, Compiled Expression, Execution Scope, Dynamic Instant, Audit Trail.

**Files/modules involved:**

- `src/main/java/com/runestone/expeval/internal/runtime/ExpressionRuntimeSupport.java`
- `src/main/java/com/runestone/expeval/internal/runtime/ExecutionScope.java`
- `src/main/java/com/runestone/expeval/internal/runtime/AbstractObjectEvaluator.java`
- `src/test/java/com/runestone/expeval/perf/jmh/navigation/collection/MembershipBenchmark.java`
- `src/test/java/com/runestone/expeval/perf/jmh/navigation/collection/CollectionNavigationBenchmark.java`
- `src/test/java/com/runestone/expeval/perf/jmh/evaluation/core/BooleanValueBenchmark.java`

**Problem:**

Every `compute()` currently builds an `ExecutionScope`, even when the Compiled Expression has no assignments, no Audit Trail, and only reads immutable defaults or already-coerced overrides. The `ExecutionScope` module is deep for assignments, Dynamic Instant consistency, `UNBOUND`, and Audit Trail, but shallow for tiny read-only Expressions because the interface carries mutable/audit/general-layer concerns into the hottest Expression Runtime seam.

**Deletion test:**

`ExecutionScope` cannot be deleted because assignments, Dynamic Instant consistency, `UNBOUND`, and Audit Trail depend on it. For no-assignment/no-audit read-only plans, deleting the mutable parts from that path should not remove behavior; it should only prevent those concerns from crossing the read-only seam.

**Performance/GC hypothesis:**

Deepening the read-only Expression Runtime path could reduce the minimum allocation floor for small Expressions and reduce branch cost in identifier lookup. The expected impact is strongest for tiny repeated Expressions such as membership checks, index access, direct count, boolean chains, and default-only Expressions.

**Benefits:**

- locality: read-only lookup rules concentrate in one module.
- leverage: every small no-audit Compiled Expression benefits.
- interface: tests can still exercise the public compute seam.
- GC: fewer per-evaluation objects on the smallest hot paths.

**Validation:**

- `src/test/java/com/runestone/expeval/internal/runtime/ExecutionScopeTest.java`
- `src/test/java/com/runestone/expeval/internal/runtime/NullMembershipTest.java`
- `src/test/java/com/runestone/expeval/api/DynamicLiteralExpressionTest.java`
- `src/test/java/com/runestone/expeval/api/AuditTrailExpressionTest.java`
- `src/test/java/com/runestone/expeval/perf/jmh/navigation/collection/MembershipBenchmark.java`
- `src/test/java/com/runestone/expeval/perf/jmh/navigation/collection/CollectionNavigationBenchmark.java`
- `src/test/java/com/runestone/expeval/perf/jmh/evaluation/core/BooleanValueBenchmark.java`

## 2. Deepen External Symbol override locality

**Recommendation strength:** Strong

**Domain terms:** External Symbol, Execution Scope, Runtime Value.

**Files/modules involved:**

- `src/main/java/com/runestone/expeval/internal/runtime/ExpressionRuntimeSupport.java`
- `src/main/java/com/runestone/expeval/internal/runtime/ExternalBindingPlan.java`
- `src/main/java/com/runestone/expeval/internal/runtime/ExecutionScope.java`
- `src/main/java/com/runestone/expeval/api/MathExpression.java`
- `src/main/java/com/runestone/expeval/api/LogicalExpression.java`
- `src/main/java/com/runestone/expeval/api/AssignmentExpression.java`
- `src/test/java/com/runestone/expeval/perf/jmh/runtime/bindings/BindingsOverlayBenchmark.java`

**Problem:**

The public `compute(Map<String, Object>)` adapter is a necessary seam, but repeated evaluation with stable External Symbol shape still pays for override array allocation, `UNBOUND` fill, name lookup through `externalBindingsByName`, overridability checks, and Runtime Value coercion on each call. The Map adapter has a broad interface and poor locality for callers that repeatedly supply the same External Symbol shape.

**Deletion test:**

The Map adapter cannot be deleted because it is the public evaluation interface. The repeated per-call name lookup, fill, and coercion work would pass the deletion test for stable binding-shape workloads if a deeper module owned that work outside the inner Expression Runtime loop.

**Performance/GC hypothesis:**

Moving stable External Symbol shape work behind a deeper module could reduce per-call `Object[]` allocation, map lookup overhead, and repeated coercion for override-heavy Expressions. The expected impact is strongest for variable churn, many-default/sparse-override Expressions, and repeated compute loops with identical binding names.

**Benefits:**

- locality: External Symbol shape rules concentrate in one module.
- leverage: every repeated override-heavy Expression benefits.
- interface: the Map adapter can remain while a deeper implementation absorbs stable shape work.
- GC: fewer override arrays and less repeated fill work.

**Validation:**

- `src/test/java/com/runestone/expeval/perf/jmh/runtime/bindings/BindingsOverlayBenchmark.java`
- `src/test/java/com/runestone/expeval/perf/jmh/runtime/bindings/AssignmentExpressionBindingsBenchmark.java`
- `src/test/java/com/runestone/expeval/perf/jmh/evaluation/core/ObjectEvaluatorBenchmark.java`
- `src/test/java/com/runestone/expeval/internal/runtime/ExecutionScopeTest.java`
- API behavior for unknown External Symbols, Internal Symbol rejection, non-overridable symbols, and Runtime Value coercion.

## 3. Replace Dynamic Instant map depth with fixed domain depth

**Recommendation strength:** Worth exploring

**Domain terms:** Dynamic Instant, Execution Scope, Audit Trail.

**Files/modules involved:**

- `src/main/java/com/runestone/expeval/internal/runtime/ExecutionScope.java`
- `src/main/java/com/runestone/expeval/internal/runtime/ExecutableDynamicLiteral.java`
- `src/main/java/com/runestone/expeval/internal/runtime/DynamicInstant.java`
- `src/main/java/com/runestone/expeval/internal/runtime/AbstractObjectEvaluator.java`

**Problem:**

`currDate`, `currTime`, and `currDateTime` are a fixed domain of three Dynamic Instants, but `ExecutionScope.resolveDynamic(...)` uses an `EnumMap` and `computeIfAbsent`. That is a general-purpose adapter for a very small fixed-shape cache, so the interface is wider than the domain rule: resolve each Dynamic Instant once per Execution Scope and emit Audit Trail reads when audit is active.

**Deletion test:**

Dynamic Instant caching cannot be deleted because repeated reads inside one Execution Scope must return the same value. The `EnumMap` abstraction is shallow: deleting that general map should not delete the domain rule.

**Performance/GC hypothesis:**

Replacing the generic map with fixed-domain storage could remove the first-read `EnumMap` allocation for Expressions using Dynamic Instants. The impact is focused but clean for temporal-heavy Expressions, especially repeated Dynamic Instant reads with and without Audit Trail.

**Benefits:**

- locality: the fixed Dynamic Instant rule lives in one implementation.
- depth: the module interface matches the domain instead of a generic map adapter.
- GC: no per-scope `EnumMap` allocation.
- tests: the same Execution Scope seam remains the test surface.

**Validation:**

- `src/test/java/com/runestone/expeval/api/DynamicLiteralExpressionTest.java`
- `src/test/java/com/runestone/expeval/api/AuditTrailExpressionTest.java`
- Add or extend JMH coverage for repeated `currDate`, `currTime`, and `currDateTime` reads with and without Audit Trail.

## 4. Extend Scalar Aggregation depth across safe Navigation Step categories

**Recommendation strength:** Worth exploring

**Domain terms:** Collection Navigation, Navigation Step, Scalar Aggregation, Deep Scan, Collection Function, Audit Trail, Fold Barrier.

**Files/modules involved:**

- `src/main/java/com/runestone/expeval/internal/runtime/CollectionScalarAggregationPlanner.java`
- `src/main/java/com/runestone/expeval/internal/runtime/DefaultCollectionScalarAggregationProgram.java`
- `src/main/java/com/runestone/expeval/internal/runtime/CollectionNavigationOps.java`
- `src/main/java/com/runestone/expeval/internal/runtime/PropertyChainOps.java`
- `src/test/java/com/runestone/expeval/internal/runtime/ScalarAggregationPlanTest.java`

**Problem:**

The current Scalar Aggregation program is already a successful deepening: eligible no-audit `compute()` chains avoid intermediate `List` and `Map` Runtime Value materialization. The remaining friction is that Deep Scan, Collection Function, and Audit Trail paths still fall back to `CollectionNavigationOps`, where aggregation may materialize intermediate Runtime Values and use a numeric-list barrier. Some exclusions may be true semantic Fold Barriers; others may only be missing implementation depth.

**Deletion test:**

The current exclusions cannot simply be deleted because behavior and Audit Trail ordering must stay exact. The opportunity is to run the deletion test by category: deleting fallback for a category is valid only when the lost complexity does not reappear in callers and the deep Scalar Aggregation module can preserve the same behavior locally.

**Performance/GC hypothesis:**

Extending Scalar Aggregation depth could reduce allocation in deep-scan aggregation, collection-function aggregation, and audited scalar aggregation. The best candidates are collection-heavy chains such as Deep Scan count, map projection sum, and Audit Trail variants of existing Scalar Aggregation benchmarks.

**Benefits:**

- locality: barrier rules become explicit by Navigation Step category.
- leverage: more Collection Navigation chains avoid materialization.
- interface: existing Expression Runtime behavior remains the test surface.
- GC: fewer intermediate Runtime Values in collection-heavy paths.

**Validation:**

- `src/test/java/com/runestone/expeval/internal/runtime/ScalarAggregationPlanTest.java`
- `src/test/java/com/runestone/expeval/api/CollectionNavigationTest.java`
- `src/test/java/com/runestone/expeval/api/OverridableSymbolCollectionNavigationTest.java`
- `src/test/java/com/runestone/expeval/api/FoldedSymbolCollectionNavigationTest.java`
- `src/test/java/com/runestone/expeval/perf/jmh/navigation/collection/CollectionNavigationBenchmark.java`
- `src/test/java/com/runestone/expeval/perf/jmh/navigation/collection/ScalarAggregationBenchmark.java`
- Add Audit Trail variants for Scalar Aggregation benchmarks before changing audited paths.

## 5. Make Navigation Step scratch ownership local

**Recommendation strength:** Worth exploring

**Domain terms:** Navigation Step, Current Element, Map Entry Context, Runtime Value, Deep Scan, Collection Navigation.

**Files/modules involved:**

- `src/main/java/com/runestone/expeval/internal/runtime/FilterContextStack.java`
- `src/main/java/com/runestone/expeval/internal/navigation/FilterContext.java`
- `src/main/java/com/runestone/expeval/internal/runtime/DeepScanContext.java`
- `src/main/java/com/runestone/expeval/internal/runtime/CollectionNavigationOps.java`
- `src/main/java/com/runestone/expeval/internal/runtime/DefaultCollectionScalarAggregationProgram.java`

**Problem:**

Thread-local scratch improves allocation locality for filters, map transforms, Deep Scan, and Scalar Aggregation. The remaining friction is lifetime locality: `FilterContextStack.pop()` decrements depth but does not clear the previous Current Element or Map Entry Context references, and `DeepScanContext` clears scratch at the start of the next scan. That keeps allocation low, but Runtime Values from large object graphs can remain retained by worker threads longer than intended.

**Deletion test:**

Deleting thread-local scratch would fail the performance deletion test because previous performance work depends on avoiding per-element allocation. Deleting retained references at the end of each Navigation Step should preserve the low-allocation implementation while improving GC locality. Deep Scan final-result ownership needs special care because its result is itself a Runtime Value.

**Performance/GC hypothesis:**

Throughput may be neutral or slightly slower because clearing references has a cost. GC behavior should improve for large collections, maps, and deep object graphs by avoiding accidental thread-local retention across evaluations.

**Benefits:**

- locality: scratch lifetime becomes part of the Navigation Step implementation.
- leverage: pooled-thread workloads become safer without removing scratch depth.
- GC: fewer retained Runtime Values after evaluation.
- interface: callers still see the same Collection Navigation behavior.

**Validation:**

- `src/test/java/com/runestone/expeval/api/CollectionNavigationTest.java`
- `src/test/java/com/runestone/expeval/api/ObjectNavigationCircularReferenceTest.java`
- `src/test/java/com/runestone/expeval/perf/jmh/navigation/collection/CollectionNavigationBenchmark.java`
- `src/test/java/com/runestone/expeval/perf/jmh/navigation/collection/ScalarAggregationBenchmark.java`
- Add a retention-oriented test or benchmark using large filter inputs and large Deep Scan graphs.

## 6. Re-check Function Invocation depth for typed Object Navigation

**Recommendation strength:** Worth exploring

**Domain terms:** Function Invocation, Object Navigation, Collection Function, Runtime Value, Audit Trail.

**Files/modules involved:**

- `src/main/java/com/runestone/expeval/internal/runtime/RuntimeInvocationSupport.java`
- `src/main/java/com/runestone/expeval/internal/runtime/PropertyChainOps.java`
- `src/main/java/com/runestone/expeval/internal/runtime/ExecutablePropertyChain.java`
- `src/main/java/com/runestone/expeval/internal/navigation/TypeIntrospectionSupport.java`
- `docs/perf/performance-history.md`

**Problem:**

`RuntimeInvocationSupport` centralizes Function Invocation, Collection Function invocation, and Object Navigation method-handle invocation. This module has policy leverage for arity-specialized invocation, coercion, and Audit Trail argument ownership. The open friction is typed Object Navigation: the performance history records a previous unresolved `typedMethodWithArgument` risk after invocation consolidation, suggesting the shared seam may reduce hot-path locality for method-handle calls.

**Deletion test:**

The centralized Function Invocation policy cannot be deleted globally because coercion and Audit Trail correctness depend on it. The typed Object Navigation call shape is the deletion-test target: if it needs a more local implementation, duplicated policy must be avoided or justified by benchmark evidence.

**Performance/GC hypothesis:**

The main upside is speed rather than allocation. Potential gains are in typed method calls with arguments, reflective method calls after cache warmup, and Collection Function invocation where receiver plus arguments create more call-shape pressure.

**Benefits:**

- locality: Object Navigation hot shape can be measured separately.
- leverage: shared Function Invocation policy remains deep where it earns its seam.
- interface: the Expression Runtime interface stays unchanged.
- tests: benchmarks decide whether the seam is real or hypothetical.

**Validation:**

- `src/test/java/com/runestone/expeval/perf/jmh/navigation/object/ObjectNavigationBenchmark.java`
- `src/test/java/com/runestone/expeval/perf/jmh/navigation/collection/CollectionNavigationBenchmark.java`
- `src/test/java/com/runestone/expeval/perf/jmh/runtime/audit/AuditOverheadBenchmark.java`
- `src/test/java/com/runestone/expeval/api/ObjectNavigationTest.java`
- `src/test/java/com/runestone/expeval/internal/runtime/ObjectNavigationPlanTest.java`

## 7. Push Constant Folding through constant destructuring

**Recommendation strength:** Worth exploring

**Domain terms:** Constant Folding, Fold Barrier, Internal Symbol, Runtime Value, Assignment Expression, Audit Trail.

**Files/modules involved:**

- `src/main/java/com/runestone/expeval/internal/runtime/ExecutionPlanBuilder.java`
- `src/main/java/com/runestone/expeval/internal/runtime/ConstantFoldingPolicy.java`
- `src/main/java/com/runestone/expeval/internal/runtime/ExecutableDestructuringAssignment.java`
- `src/main/java/com/runestone/expeval/internal/runtime/AbstractObjectEvaluator.java`
- `docs/internal/runtime-internals.md`

**Problem:**

Simple assignments participate in Constant Folding, but destructuring assignments do not publish per-target constant bindings during plan build. That makes destructuring a Fold Barrier even when the right-hand Runtime Value is a folded vector. Expressions such as `[a,b,c] = [10,20,30]; a+b+c` still pay for vector handling, assignment writes, and Internal Symbol reads at runtime.

**Deletion test:**

Destructuring assignment execution cannot be deleted for Assignment Expression outputs or Audit Trail events. For math/logical Compiled Expressions with constant destructuring and no audit, per-target runtime assignment and subsequent reads are deletion-test candidates if observable behavior remains equivalent.

**Performance/GC hypothesis:**

Publishing per-target constants during Constant Folding could reduce internal assignment array use, vector handling, assignment writes, and identifier reads for constant destructuring. Function-backed destructuring remains a Fold Barrier unless the Function Invocation is foldable.

**Benefits:**

- locality: destructuring fold rules live in Constant Folding instead of runtime callers.
- leverage: less assignment churn for every constant destructuring Expression.
- depth: the Execution Plan absorbs more compile-time behavior.
- tests: Assignment Expression and Audit Trail cases protect observable behavior.

**Validation:**

- `src/test/java/com/runestone/expeval/internal/runtime/ConstantFoldingPlanTest.java`
- `src/test/java/com/runestone/expeval/api/DestructuringAssignmentExpressionTest.java`
- `src/test/java/com/runestone/expeval/api/AssignmentExpressionTest.java`
- `src/test/java/com/runestone/expeval/api/AuditTrailExpressionTest.java`
- `src/test/java/com/runestone/expeval/perf/jmh/evaluation/assignment/DestructuringAssignmentBenchmark.java`

## Top recommendation

Start with **Deepen the read-only Execution Scope path**.

It attacks the smallest recurring Expression Runtime allocation floor, has clear locality, and has the cleanest deletion test: mutable assignments, Dynamic Instant consistency, and Audit Trail stay behind their existing semantics while no-assignment/no-audit Compiled Expressions get a deeper module.
