# Scalar Aggregation Deepening Design

Design note for the implemented Expression Runtime deepening that optimizes Collection Navigation chains ending in Scalar Aggregation while preserving current public behavior.

The first implementation covers ordinary `compute()` evaluation. `computeWithAudit()` intentionally remains on the existing fallback path.

---

## Goal

Deepen the Collection Navigation module so eligible chains ending in Scalar Aggregation can avoid materializing intermediate Runtime Values during ordinary `compute()` evaluation.

Examples in scope:

```text
prices..sum()
orders..sum(@ -> @.price * @.qty)
books[?(@.author = "Alice")].price..avg()
nums..map(@ -> @ * 2)..sum()
inventory[?(@.key.domain = "acme")]..values()..sum()
```

The aim is lower GC and faster hot-path evaluation without changing the public Expression Runtime interface.

---

## Domain Term

`Scalar Aggregation` is the domain term for a Collection Navigation operation that reduces a collection or map to a scalar Runtime Value such as count, sum, average, minimum, maximum, or product.

The term is recorded in the root `CONTEXT.md` glossary.

---

## Decisions

1. First target: Scalar Aggregation in Collection Navigation.
2. First runtime path: optimize ordinary `compute()` only.
3. Audit path: `computeWithAudit()` remains on the current implementation until a dedicated audit-preserving optimization is designed and measured.
4. Syntax scope: eligible Collection Navigation chains excluding Deep Scan and collection functions.
5. Planning location: eligibility is decided once while building the Execution Plan.
6. Initial shape: package-private opaque program attached to `ExecutablePropertyChain`.
7. Runtime seam: `PropertyChainOps` invokes the program only when audit is inactive.
8. Semantics: preserve barriers between Navigation Steps.
9. Scratch: use bounded ThreadLocal scratch behind the module implementation, clear references in `finally`, and discard over-capacity buffers.
10. Validation: add focused JMH cases and capture a fresh baseline before implementation.

---

## Non-Goals

1. Do not optimize Deep Scan in the first slice.
2. Do not optimize collection functions in the first slice.
3. Do not introduce a public API or public adapter.
4. Do not change `computeWithAudit()` behavior or Audit Trail event ordering.
5. Do not introduce Runtime Value wrapper objects.
6. Do not replace the existing fallback behavior in `PropertyChainOps` or `CollectionNavigationOps`.
7. Do not use a fully streaming pipeline if it changes error order or evaluation order.

---

## Current Friction

Before this deepening, `PropertyChainOps` evaluated a chain one Navigation Step at a time, and `CollectionNavigationOps` returned concrete `List` or `Map` Runtime Values for wildcard, slice, filter, map projection, map transform, and aggregation preparation.

That makes the seam between Navigation Steps shallow: the interface is the materialized Runtime Value, so allocation policy leaks across the Expression Runtime.

The deletion test showed the gap: deleting `CollectionNavigationOps` would move most logic into `PropertyChainOps`, not remove complexity. The module packaged operations, but it did not yet own a deep execution model for Scalar Aggregation.

---

## Proposed Module

Create a package-private module in `com.runestone.expeval.internal.runtime`.

Implemented names:

```java
final class CollectionScalarAggregationPlanner { ... }

interface CollectionScalarAggregationProgram { ... }

record ScalarAggregationRuntime(...) { ... }

final class DefaultCollectionScalarAggregationProgram { ... }
```

The seam is:

```java
final class CollectionScalarAggregationPlanner {

    static CollectionScalarAggregationProgram planOrNull(
            List<ExecutablePropertyChain.ExecutableAccess> chain) {
        // Returns null when the chain is not eligible.
    }
}
```

```java
interface CollectionScalarAggregationProgram {

    int startIndex();

    Object compute(
            Object current,
            ExecutionScope scope,
            ScalarAggregationRuntime runtime);
}
```

```java
record ScalarAggregationRuntime(
        String source,
        RuntimeServices runtimeServices,
        MathContext mathContext,
        NodeEvaluator evaluator) {
}
```

`ExecutablePropertyChain` carries an optional `CollectionScalarAggregationProgram`. The original chain remains the behavioral source of truth and the fallback path.

---

## Seam Placement

Compile-time seam:

```text
ExecutionPlanBuilder -> CollectionScalarAggregationPlanner
```

`ExecutionPlanBuilder` decides eligibility once after normal chain construction and after constant folding has had first chance to fold the chain.

Runtime seam:

```text
PropertyChainOps -> CollectionScalarAggregationProgram
```

`PropertyChainOps` evaluates the root and any non-optimized prefix with existing behavior, then calls the program at `startIndex()` only when `scope.audit() == null`.

Fallback seam:

```text
ExecutablePropertyChain original chain -> existing PropertyChainOps / CollectionNavigationOps
```

If no program exists, or audit is active, the current implementation runs unchanged.

---

## Eligibility

The planner returns a program only when all rules hold:

1. The final Navigation Step is `ExecutableVectorAggregation`.
2. No Navigation Step appears after the terminal Scalar Aggregation.
3. The terminal aggregation kind is `COUNT`, `SUM`, `AVG`, `MIN`, `MAX`, or `PROD`.
4. The optimized suffix excludes `ExecutableDeepScan`.
5. The optimized suffix excludes `ExecutableCollectionFunction`.
6. Method invocation is not eligible as a projected collection step.
7. Property projection is eligible only for `ExecutableFieldGet` and `ReflectivePropertyAccess`, matching current list-projection behavior.
8. Eligible Collection Navigation steps are index, slice, wildcard, filter, map projection, map transform, and property projection.
9. `ExecutableVectorAggregation.transform()` is eligible because current aggregation already evaluates terminal transforms.
10. Ineligibility returns `null`; it is not a semantic error.

---

## Semantic Invariants

The optimized implementation must preserve observable behavior:

1. Evaluate Navigation Steps left-to-right.
2. Preserve current list order and map entry iteration order.
3. Preserve `Current Element` binding for filters, map transforms, and terminal aggregation transforms.
4. Preserve `Map Entry Context` so `@`, `@.key`, and `@.value` behave as they do today.
5. Pair every `FilterContextStack` push with `pop()` in `finally`.
6. Preserve null behavior and safe-navigation behavior for the optimized suffix and existing prefix.
7. Preserve empty aggregation behavior: `SUM -> BigDecimal.ZERO`, `PROD -> BigDecimal.ONE`, `AVG/MIN/MAX -> null`, `COUNT -> BigDecimal.ZERO` for empty collections.
8. Preserve `MathContext` use for `SUM`, `AVG`, and `PROD`.
9. Preserve current numeric coercion behavior through `RuntimeServices.asNumber(...)`.
10. Preserve current boolean coercion behavior through `RuntimeServices.asBoolean(...)`.
11. Preserve current error codes and exception cause wrapping.
12. Do not skip upstream filters, property projections, or map transforms for `COUNT` when those steps would currently evaluate and may fail.

---

## Barrier Preservation

The first implementation should preserve barriers between Navigation Steps.

For this expression:

```text
items[?(@.active)].price..sum()
```

Current behavior is conceptually:

```text
filter all items -> project all prices -> coerce all numbers -> sum
```

Do not rewrite it into a pure item-by-item pipeline if that changes which predicate, property access, or numeric conversion error appears first.

The implementation may use internal scratch to avoid public Runtime Value materialization, but the scratch must preserve step-level ordering.

---

## Scratch Policy

Use bounded ThreadLocal scratch only behind the deep module implementation.

Required rules:

1. Scratch is never returned as a public Runtime Value.
2. Scratch object references are cleared in `finally`.
3. Large buffers are discarded instead of retained indefinitely.
4. Scratch capacity policy is an implementation detail, not part of the module interface.
5. ThreadLocal scratch must not weaken the current thread-safety contract of Compiled Expression evaluation.

This keeps the allocation benefit while reducing retained-GC risk on pooled threads.

Implementation detail: the first implementation retains scratch arrays up to 4,096 elements per thread and discards larger buffers on release.

---

## Testing Strategy

The interface is the test surface. Tests should exercise the same seam used by the Expression Runtime rather than testing each internal scratch operation.

High-signal behavior tests:

1. `books[?(@.author = "Alice")].price..avg()` returns the same value as the fallback path.
2. `nums..map(@ -> @ * 2)..sum()` preserves transform order and result.
3. `nums..map(@ -> fail(@))..count()` still evaluates the transform and fails as before.
4. `inventory[?(@.key.domain = "acme")]..values()..sum()` preserves Map Entry Context.
5. `COUNT`, `SUM`, `AVG`, `MIN`, `MAX`, and `PROD` preserve empty-input behavior.
6. Invalid property projection preserves `UNKNOWN_PROPERTY` or `PROPERTY_ACCESS_ERROR` as today.
7. Null numeric aggregation preserves `NULL_VALUE` behavior.
8. `computeWithAudit()` emits the same Audit Trail as before because it uses fallback.

Existing anchors:

```text
api/CollectionNavigationTest
api/VectorMapTransformTest
api/CollectionNavigationThreadLocalTest
api/AuditTrailExpressionTest
internal/runtime/ObjectNavigationPlanTest
```

---

## Benchmark Strategy

Add focused JMH cases before changing runtime implementation.

Suggested cases:

```text
directSum
directCount
filterPropertyAvg
slicePropertySum
mapTransformSum
mapTransformAvg
mapFilterValuesSum
countAfterMapTransform
```

Benchmark requirements:

1. Capture a fresh before baseline in the current session.
2. Measure both `ns/op` and `B/op` with the GC profiler.
3. Run the same benchmark after implementation with identical JMH parameters.
4. Record results in `expression-evaluator/docs/perf/performance-history.md`.
5. Treat unchanged behavior tests as mandatory before running JMH after implementation.

Relevant existing JMH group:

```text
com.runestone.expeval.perf.jmh.navigation.collection
```

---

## Deletion Test

If this module is deleted, public behavior should remain correct by removing:

1. The optional program field from `ExecutablePropertyChain`.
2. The planner call from `ExecutionPlanBuilder`.
3. The program dispatch from `PropertyChainOps`.

The existing chain and fallback implementation still run.

The lost complexity would reappear as special cases in `PropertyChainOps` and `CollectionNavigationOps`: fused filter/count, projection/sum, map-transform/avg, map-filter/values/sum, accumulator specialization, Current Element binding, and Map Entry Context cleanup.

That means the module earns its seam: deleting it does not delete complexity; it scatters complexity back into callers.

---

## Open Follow-Ups

1. Decide whether to add a small property-access helper to avoid duplicating reflective and typed property error handling.
2. Decide whether a later stage should split compute and audit Execution Plan shapes if the audit fallback branch is measurable.
3. Design a separate Deep Scan optimization only after this Scalar Aggregation module has benchmark evidence.
