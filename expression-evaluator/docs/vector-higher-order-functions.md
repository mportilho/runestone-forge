# Plan: Vector Higher-Order Functions (filter, sum, prod) for expression-evaluator

## Context

The `expression-evaluator` module already supports collection navigation with filtering (`[?(@ > 1)]`),
aggregations (`..sum()`, `..avg()`), and deep-scan operations. This plan extends those mechanisms
to add `..filter(pred)`, `..sum(transform)`, and `..prod(transform)` as **first-class collection
navigation steps**, reusing the existing `FilterContext` / `@` infrastructure instead of
introducing a separate lambda/closure system.

### New syntax

```
[1, 2, 3]..filter(@ > 1)           // keeps elements where predicate is true  → [2, 3]
[1, 2, 3]..sum(@ * 2^@)             // Σ f(element)                            → 34
[1, 2, 3]..prod(@ * 2^@)            // Π f(element)
[1, 2, 3]..prod()                   // product of all elements                 → 6
prices..filter(@ > 10)..sum()       // composable with existing steps
books..filter(@.price < 10)..count()// element is a Record/Map — access fields via @.field
```

The `@` token is the **current-element placeholder** — the same concept used today inside
`[?(<pred>)]` filter predicates. Its runtime value comes from `FilterContext.element()`.

---

## Architecture overview (existing pipeline recap)

```
Source → [ANTLR] → AST (ExpressionNode tree)
       → [SemanticResolver] → SemanticModel (type bindings, function bindings)
       → [ExecutionPlanBuilder] → ExecutionPlan (ExecutableNode tree)
       → [AbstractObjectEvaluator] → Object result
```

Key existing infrastructure this plan builds on:
- `PropertyChainNode.MemberAccess` — sealed interface with 13 step types; new types added here.
- `ExecutablePropertyChain.ExecutableAccess` — mirrors `MemberAccess`; new executables added here.
- `VectorAggregationKind` — `SUM`, `AVG`, `MIN`, `MAX`, `COUNT`; `PROD` added here.
- `FilterContext` — thread-local element binding used by `[?(pred)]`; reused for transform steps.
- `AbstractObjectEvaluator.FilterContextStack` — pooled `FilterContext` instances; reused as-is.

---

## Phase 1 — Grammar

**File:** `expression-evaluator/src/main/antlr4/com/runestone/expeval/internal/grammar/ExpressionEvaluator.g4`

### 1.1 Extend `referenceTarget` to allow `@`

The `@` token (`AT`) already exists as a lexer rule. It is currently only valid inside the
`filterValue` production (`filterValue : AT memberChain* # currentElementFilterValue`). Adding it
to `referenceTarget` makes it valid in any expression context — numeric, logical, string — because
all entity types eventually bottom out at `referenceTarget`.

```antlr
referenceTarget
    : function                                                           # functionReferenceTarget
    | IDENTIFIER memberChain*                                            # identifierReferenceTarget
    | AT memberChain*                                                    # atReferenceTarget          // ← NEW
    ;
```

This single addition enables:
- `@ * 2` — `@` as a numeric primary (via `numericEntity` → `numericReferenceOperation`)
- `@ > 1` — `@` on the left side of a math comparison
- `@.price < 10` — property navigation on the current element
- `@.name = "Alice"` — string equality on an element field

No new tokens are needed; `AT` is already declared.

### 1.2 Regenerate the parser

Follow the ANTLR regeneration steps in `CLAUDE.md`. Copy only the generated `.java` files into:
`expression-evaluator/src/main/java/com/runestone/expeval/internal/grammar/`

---

## Phase 2 — `VectorAggregationKind`

**File:** `src/main/java/com/runestone/expeval/internal/navigation/VectorAggregationKind.java`

Add `PROD`:

```java
public enum VectorAggregationKind {
    SUM,
    AVG,
    MIN,
    MAX,
    COUNT,
    PROD    // ← new: product of elements (or transformed elements)
}
```

---

## Phase 3 — AST layer

### 3.1 Extend `VectorAggregationStep` with optional transform

**File:** `src/main/java/com/runestone/expeval/internal/ast/PropertyChainNode.java`

Replace the existing `VectorAggregationStep` record:

```java
/**
 * {@code ..sum()}, {@code ..sum(@ * 2)}, {@code ..prod()}, {@code ..prod(@ * 2)}, etc.
 * When {@code transform} is non-null it is evaluated per element (with {@code @} bound to that
 * element via {@code FilterContext}) and the result is aggregated.
 */
public record VectorAggregationStep(
        VectorAggregationKind kind,
        @Nullable ExpressionNode transform
) implements MemberAccess {
    public VectorAggregationStep {
        Objects.requireNonNull(kind, "kind must not be null");
    }

    /** Convenience constructor for no-transform aggregation (e.g. {@code ..sum()}). */
    public VectorAggregationStep(VectorAggregationKind kind) {
        this(kind, null);
    }
}
```

### 3.2 New `FilterFunctionStep`

Add to `PropertyChainNode.MemberAccess` sealed interface (and its `permits` clause):

```java
/**
 * {@code ..filter(pred)} — function-call syntax for element-wise filtering.
 * The predicate expression may use {@code @} to refer to the current element.
 * Semantically equivalent to {@code [?(pred)]} but written as a chained function call.
 */
public record FilterFunctionStep(ExpressionNode predicate) implements MemberAccess {
    public FilterFunctionStep {
        Objects.requireNonNull(predicate, "predicate must not be null");
    }
}
```

Update the `permits` clause of `MemberAccess`:

```java
public sealed interface MemberAccess permits
        PropertyAccess, SafePropertyAccess, MethodCallAccess, SafeMethodCallAccess,
        CollectionIndexStep, MapKeyStep, CollectionSliceStep, WildcardStep,
        FilterPredicateStep, FilterFunctionStep,                              // ← add FilterFunctionStep
        DeepScanStep, CollectionFunctionStep, MapProjectionStep,
        VectorAggregationStep {}
```

### 3.3 Update `SemanticAstBuilder` visitor for `collectionFunctionAccess`

**File:** `src/main/java/com/runestone/expeval/internal/ast/mapping/SemanticAstBuilder.java`

The grammar rule `collectionFunctionAccess` matches `..IDENTIFIER(args?)`. Its visitor currently
recognizes "sum", "avg", "min", "max", "count", "keys", "values" by name. Extend the switch:

```java
@Override
public MemberAccess visitCollectionFunctionAccess(
        ExpressionEvaluatorParser.CollectionFunctionAccessContext ctx) {
    String name = ctx.IDENTIFIER().getText();
    List<ExpressionEvaluatorParser.AllEntityTypesContext> args = ctx.allEntityTypes();

    return switch (name) {
        case "sum"   -> new VectorAggregationStep(VectorAggregationKind.SUM,
                            args.isEmpty() ? null : visitAllEntityType(args.get(0)));
        case "avg"   -> new VectorAggregationStep(VectorAggregationKind.AVG, null);
        case "min"   -> new VectorAggregationStep(VectorAggregationKind.MIN, null);
        case "max"   -> new VectorAggregationStep(VectorAggregationKind.MAX, null);
        case "count" -> new VectorAggregationStep(VectorAggregationKind.COUNT, null);
        case "prod"  -> new VectorAggregationStep(VectorAggregationKind.PROD,      // ← new
                            args.isEmpty() ? null : visitAllEntityType(args.get(0)));
        case "filter" -> {                                                           // ← new
            if (args.isEmpty()) throw new IllegalArgumentException("..filter() requires a predicate argument");
            yield new FilterFunctionStep(visitAllEntityType(args.get(0)));
        }
        case "keys"   -> new MapProjectionStep(MapProjectionKind.KEYS);
        case "values" -> new MapProjectionStep(MapProjectionKind.VALUES);
        default -> buildCollectionFunctionStep(name, args);  // existing catalog-function path
    };
}
```

### 3.4 Update visitor for `atReferenceTarget`

Add a visitor for the new grammar alternative:

```java
@Override
public ExpressionNode visitAtReferenceTarget(
        ExpressionEvaluatorParser.AtReferenceTargetContext ctx) {
    // Build a PropertyChainNode whose rootIdentifier is "@".
    // SemanticResolver and AbstractObjectEvaluator already handle "@" as the FilterContext element.
    String rootId = "@";
    List<MemberAccess> chain = ctx.memberChain().stream()
            .map(this::visitMemberChain)
            .toList();
    return new PropertyChainNode(nodeFactory.nextId("at"), nodeFactory.sourceSpan(ctx), rootId, chain);
}
```

Note: if the chain is empty this reduces to a plain `IdentifierNode("@")` — adjust to match the
existing convention used for `currentElementFilterValue` in the filter predicate visitor.

---

## Phase 4 — Semantic resolution

### 4.1 Resolve `@` identifier

**File:** `src/main/java/com/runestone/expeval/internal/runtime/SemanticResolver.java`

In `resolveIdentifier` (or wherever identifier nodes are typed), add a check before the external/internal symbol lookup:

```java
if ("@".equals(node.name())) {
    // The current-element placeholder — type is unknown at compile time; resolved via
    // FilterContext at runtime. This is the same behaviour as inside [?(...)] predicates.
    resolvedTypes.put(node.nodeId(), UnknownType.INSTANCE);
    return UnknownType.INSTANCE;
}
```

If `PropertyChainNode` is used instead of `IdentifierNode` for `@`, apply the same logic when
`rootIdentifier.equals("@")`.

### 4.2 Resolve `FilterFunctionStep`

In the `SemanticResolver` walk of `PropertyChainNode.chain`, add a case:

```java
case FilterFunctionStep(ExpressionNode predicate) -> {
    resolveExpression(predicate);   // resolve @-references inside the predicate
    // result mode stays COLLECTION — filter outputs a list
}
```

### 4.3 Resolve `VectorAggregationStep` with transform

Extend the existing aggregation case to also resolve the optional transform:

```java
case VectorAggregationStep(VectorAggregationKind kind, ExpressionNode transform) -> {
    if (transform != null) {
        resolveExpression(transform);  // resolve @-references inside the transform
    }
    // result mode: SUM/AVG/MIN/MAX/PROD → SCALAR; COUNT → SCALAR
}
```

---

## Phase 5 — Execution layer

### 5.1 Extend `ExecutableVectorAggregation`

**File:** `src/main/java/com/runestone/expeval/internal/runtime/ExecutablePropertyChain.java`

Replace the existing `ExecutableVectorAggregation` record:

```java
/** {@code ..sum()}, {@code ..sum(@ * 2)}, {@code ..prod()}, {@code ..prod(@)}, etc. */
record ExecutableVectorAggregation(
        VectorAggregationKind kind,
        @Nullable ExecutableNode transform
) implements ExecutableAccess {
    ExecutableVectorAggregation {
        Objects.requireNonNull(kind, "kind must not be null");
    }

    ExecutableVectorAggregation(VectorAggregationKind kind) {
        this(kind, null);
    }
}
```

### 5.2 New `ExecutableFilterFunction`

Add to `ExecutablePropertyChain.ExecutableAccess` sealed interface:

```java
/** {@code ..filter(pred)} — element-wise filter using FilterContext for {@code @}. */
record ExecutableFilterFunction(ExecutableNode predicate) implements ExecutableAccess {
    ExecutableFilterFunction {
        Objects.requireNonNull(predicate, "predicate must not be null");
    }
}
```

Update the `permits` clause accordingly.

### 5.3 Build executable nodes in `ExecutionPlanBuilder`

**File:** `src/main/java/com/runestone/expeval/internal/runtime/ExecutionPlanBuilder.java`

In the member-access building switch:

```java
case FilterFunctionStep(ExpressionNode predicate) ->
    new ExecutableFilterFunction(buildNode(predicate, ...));

case VectorAggregationStep(VectorAggregationKind kind, ExpressionNode transform) ->
    new ExecutableVectorAggregation(kind,
        transform == null ? null : buildNode(transform, ...));
```

### 5.4 Evaluate in `AbstractObjectEvaluator`

**File:** `src/main/java/com/runestone/expeval/internal/runtime/AbstractObjectEvaluator.java`

In the `evaluatePropertyChain` step dispatch, extend the switch:

```java
case ExecutableFilterFunction ef    -> applyFilterFunction(current, ef.predicate(), scope);
case ExecutableVectorAggregation va -> applyAggregation(current, va.kind(), va.transform(), scope);
```

#### `applyFilterFunction`

Mirrors the existing `applyFilter` for `[?(pred)]`. The `FilterContextStack` from the existing
implementation is reused as-is:

```java
private Object applyFilterFunction(Object current, ExecutableNode predicate, ExecutionScope scope) {
    // Delegate to the same implementation used for ExecutableFilterPredicate.
    // Extract to a shared helper if not already done.
    return applyFilterPredicate(current, predicate, scope);
}
```

If `applyFilter` (for `[?(pred)]`) is not yet a shared method, extract it and call it from both
`ExecutableFilterPredicate` and `ExecutableFilterFunction` cases.

#### Extend `applyAggregation` for optional transform and `PROD`

```java
private Object applyAggregation(
        Object current,
        VectorAggregationKind kind,
        @Nullable ExecutableNode transform,
        ExecutionScope scope) {

    // Resolve numeric stream: apply transform per element if present
    List<BigDecimal> values = toNumericStream(current, transform, scope);

    return switch (kind) {
        case SUM   -> values.stream().reduce(BigDecimal.ZERO, (a, b) -> a.add(b, mathContext));
        case AVG   -> { BigDecimal s = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
                        yield s.divide(BigDecimal.valueOf(values.size()), mathContext); }
        case MIN   -> values.stream().min(Comparator.naturalOrder()).orElse(null);
        case MAX   -> values.stream().max(Comparator.naturalOrder()).orElse(null);
        case COUNT -> BigDecimal.valueOf(collectionSize(current));
        case PROD  -> values.stream().reduce(BigDecimal.ONE, (a, b) -> a.multiply(b, mathContext));
    };
}

private List<BigDecimal> toNumericStream(Object current, @Nullable ExecutableNode transform, ExecutionScope scope) {
    List<?> list = requireList(current);
    if (transform == null) {
        return list.stream().map(this::toBigDecimal).toList();
    }
    // Reuse FilterContextStack for per-element @-binding
    FilterContext ctx = filterContextStack.push();
    try {
        List<BigDecimal> result = new ArrayList<>(list.size());
        for (Object element : list) {
            ctx.bindElement(element);
            Object val = evaluateExpr(transform, scope);
            result.add(toBigDecimal(val));
        }
        return result;
    } finally {
        filterContextStack.pop();
    }
}
```

Note: `AVG`, `MIN`, `MAX` do not accept a transform in this plan (they already exist without one).
If a transform is passed to these, throw `IllegalArgumentException` at plan-build time.

---

## Phase 6 — Tests

**New test file:** `src/test/java/com/runestone/expeval/api/VectorHigherOrderFunctionsTest.java`

Cover:

```java
// filter
"[1, 2, 3]..filter(@ > 1)"                    // → [2, 3]
"[1, 2, 3]..filter(@ != 2)"                   // → [1, 3]
"books..filter(@.price < 10)"                  // element is a Record/Map; books is external

// sum with transform
"[1, 2, 3]..sum(@ * 2)"                        // → 12
"[1, 2, 3]..sum(@ ^ 2)"                        // → 14
"[1, 2, 3]..sum(@ * 2^@)"                      // → 34

// prod without transform (product of elements)
"[1, 2, 3]..prod()"                            // → 6
"[2, 3, 4]..prod()"                            // → 24

// prod with transform
"[1, 2, 3]..prod(@ * 2)"                       // → 2 * 4 * 6 = 48

// composition with existing collection navigation
"[1, 2, 3, 4]..filter(@ > 1)..filter(@ < 4)"  // → [2, 3]
"[1, 2, 3, 4]..filter(@ > 1)..sum()"          // → 9
"prices..filter(@ > 10)..sum()"                // external variable

// sum without transform (existing — must still work)
"[1, 2, 3]..sum()"                             // → 6

// external variable in transform expression
"[1, 2, 3]..sum(@ + offset)"                  // offset is external symbol

// edge cases
"[]..filter(@ > 0)"                            // → []
"[]..prod()"                                   // → 1 (identity)
"[5]..sum(@ * 2)"                              // → 10
```

---

## Critical files to modify

| File | Change |
|------|--------|
| `grammar/ExpressionEvaluator.g4` | Add `AT memberChain*` to `referenceTarget` |
| `grammar/` (generated) | Regenerate with ANTLR |
| `internal/navigation/VectorAggregationKind.java` | Add `PROD` |
| `internal/ast/PropertyChainNode.java` | Add `FilterFunctionStep`; add `@Nullable ExpressionNode transform` to `VectorAggregationStep` |
| `internal/ast/mapping/SemanticAstBuilder.java` | Extend `visitCollectionFunctionAccess` for `filter`/`prod`/`sum(expr)`; add `visitAtReferenceTarget` |
| `internal/runtime/SemanticResolver.java` | Resolve `"@"` identifier as `UnknownType`; resolve transform in `VectorAggregationStep`; resolve predicate in `FilterFunctionStep` |
| `internal/runtime/ExecutablePropertyChain.java` | Add `ExecutableFilterFunction`; add optional `transform` to `ExecutableVectorAggregation` |
| `internal/runtime/ExecutionPlanBuilder.java` | Handle `FilterFunctionStep` and updated `VectorAggregationStep` |
| `internal/runtime/AbstractObjectEvaluator.java` | Add `applyFilterFunction`; extend `applyAggregation` for transform and `PROD`; extract shared filter helper |

**No new files** beyond the test class. No lambda types, no new catalog functions, no new type-system
entries.

---

## What this plan does NOT introduce

The original design proposed a full lambda/closure system (`(x) => x * 2` syntax). That approach is
replaced entirely:

| Original | Replaced by |
|----------|-------------|
| `ARROW` lexer token (`=>`) | Not needed |
| `lambdaExpression` grammar rule | Not needed |
| `LambdaType` / `LambdaValue` | Not needed |
| `LambdaNode` / `ExecutableLambda` | Not needed |
| `SymbolKind.LAMBDA` | Not needed |
| Lambda scope stack in `SemanticResolver` | Not needed |
| `ExecutionScope.withLambdaLayer` | Not needed |
| `VectorFunctions` catalog class | Not needed |

Instead, the `@` element placeholder and `FilterContext` pooling — both already implemented for
`[?(pred)]` — are reused for the new steps.

---

## Verification

```shell
# 1. Regenerate grammar (see CLAUDE.md)
# 2. Build + test
mvn clean test -pl expression-evaluator

# 3. Run specific test class
mvn clean test -pl expression-evaluator -Dtest=VectorHigherOrderFunctionsTest

# 4. Full build to catch cross-module breakage
mvn clean install
```

Manually verify edge cases:

| Expression | Expected |
|---|---|
| `[1, 2, 3]..sum(@ * 2^@)` | 1·2 + 2·4 + 3·8 = **34** |
| `[1, 2, 3]..prod()` | 1·2·3 = **6** |
| `[1, 2, 3]..filter(@ > 1)` | **[2, 3]** |
| `[1, 2, 3, 4]..filter(@ > 1)..filter(@ < 4)..sum()` | 2+3 = **5** |
| `[]..prod()` | **1** (multiplicative identity) |
