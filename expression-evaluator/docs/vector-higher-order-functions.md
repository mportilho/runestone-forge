# Plan: Vector Higher-Order Functions (sum, prod) for expression-evaluator

## Context

The `expression-evaluator` module already supports collection navigation with filtering (`[?(@ > 1)]`),
aggregations (`..sum()`, `..avg()`), and deep-scan operations. This plan extends those mechanisms
to add `..sum(@ -> transform)` and `..prod(@ -> transform)` as **first-class collection navigation steps**,
reusing the existing `FilterContext` / `@` infrastructure while introducing an explicit lambda marker
so the syntax does not look like an ordinary method call whose argument merely happens to be an expression.

### New syntax

```text
[1, 2, 3]..sum(@ -> @ * 2^@)        // Σ f(element)                            → 34
[1, 2, 3]..prod(@ -> @ * 2^@)       // Π f(element)
[1, 2, 3]..sum()                    // sum of all elements                     → 6
[1, 2, 3]..prod()                   // product of all elements                 → 6
```

The `@` token remains the **current-element placeholder** — the same concept used today inside
`[?(<pred>)]` filter predicates. The new `->` marker makes the higher-order intent explicit:
`sum(@ -> expr)` reads as "sum the result of evaluating `expr` for each element bound to `@`".

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

### 1.1 Add an explicit lambda marker and extend `referenceTarget` to allow `@`

This syntax needs two parser-level changes:

1. a named `ARROW` token (`->`) so transforms are explicitly marked as higher-order expressions
2. `AT memberChain*` in `referenceTarget` so `@` can appear inside the transform body

The `@` token (`AT`) already exists as a lexer rule. It is currently only valid inside the
`filterValue` production (`filterValue : AT memberChain* # currentElementFilterValue`). Adding it
to `referenceTarget` makes it valid in any expression context — numeric, logical, string — because
all entity types eventually bottom out at `referenceTarget`.

```antlr
ARROW              : '->' ;

referenceTarget
    : function                                                           # functionReferenceTarget
    | IDENTIFIER memberChain*                                            # identifierReferenceTarget
    | AT memberChain*                                                    # atReferenceTarget
    ;

memberChain
    : DOUBLE_PERIOD IDENTIFIER LPAREN collectionFunctionArguments? RPAREN # collectionFunctionAccess
    | PERIOD MULT                                                         # childWildcardAccess
    | PERIOD IDENTIFIER LPAREN
          (allEntityTypes (COMMA allEntityTypes)*)?
      RPAREN                                                              # methodCallAccess
    | SAFE_NAV IDENTIFIER LPAREN
          (allEntityTypes (COMMA allEntityTypes)*)?
      RPAREN                                                              # safeMethodCallAccess
    | PERIOD IDENTIFIER                                                   # propertyAccess
    | SAFE_NAV IDENTIFIER                                                 # safePropertyAccess
    | subscript                                                           # subscriptAccess
    ;

collectionFunctionArguments
    : lambdaTransform                                                    # lambdaCollectionFunctionArguments
    | allEntityTypes (COMMA allEntityTypes)*                             # positionalCollectionFunctionArguments
    ;

lambdaTransform
    : AT ARROW allEntityTypes                                            # atLambdaTransform
    ;
```

This enables:
- `@ * 2` — `@` as a numeric primary (via `numericEntity` → `numericReferenceOperation`)
- `@ > 1` — `@` on the left side of a math comparison
- `@ -> @ * 2^@` — explicit per-element transform for `sum`/`prod`

`ARROW` should be a named lexer token, not a parser literal, to keep the grammar consistent with
the rest of the file.

### 1.2 SLL/LL prediction note

Both alternatives of `collectionFunctionArguments` can start with the `AT` token:
`lambdaTransform` starts with `AT ARROW`, while the positional alternative can start with `AT` via
`allEntityTypes → ... → atReferenceTarget`. ANTLR ALL(*) resolves this with a 2-token lookahead
(`AT` then `ARROW` vs anything else), but SLL prediction may not look far enough and will fall back
to LL. This is acceptable — the existing parser already uses SLL-with-LL-fallback (`PredictionStrategy`)
— but verify no parse errors appear on valid expressions.

### 1.3 Regenerate the parser

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
 * {@code ..sum()}, {@code ..sum(@ -> @ * 2)}, {@code ..prod()}, {@code ..prod(@ -> @ * 2)}, etc.
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

### 3.2 Update `SemanticAstBuilder` visitor for `collectionFunctionAccess`

**File:** `src/main/java/com/runestone/expeval/internal/ast/mapping/SemanticAstBuilder.java`

The grammar rule `collectionFunctionAccess` still matches `..IDENTIFIER(args?)`, but now the argument
payload can be either ordinary positional arguments or a single `lambdaTransform`. Its visitor should:

- keep ordinary `CollectionFunctionStep` handling unchanged for regular collection functions
- recognize `sum` and `prod` as vector aggregations
- accept an optional `@ -> ...` transform only for `sum` and `prod`
- reject lambda-style arguments for non-higher-order built-ins such as `avg`, `min`, `max`, and `count`

One straightforward shape is:

```java
@Override
public MemberAccess visitCollectionFunctionAccess(
        ExpressionEvaluatorParser.CollectionFunctionAccessContext ctx) {
    String name = ctx.IDENTIFIER().getText();
    var argBlock = ctx.collectionFunctionArguments();
    ExpressionNode transform = extractLambdaTransform(argBlock);
    List<ExpressionNode> args = extractPositionalArguments(argBlock);

    return switch (name) {
        case "sum"   -> validateAggregationArguments(name, transform, args,
                            new VectorAggregationStep(VectorAggregationKind.SUM, transform));
        case "prod"  -> validateAggregationArguments(name, transform, args,
                            new VectorAggregationStep(VectorAggregationKind.PROD, transform));
        case "avg"   -> validateNoLambda(name, transform, new VectorAggregationStep(VectorAggregationKind.AVG));
        case "min"   -> validateNoLambda(name, transform, new VectorAggregationStep(VectorAggregationKind.MIN));
        case "max"   -> validateNoLambda(name, transform, new VectorAggregationStep(VectorAggregationKind.MAX));
        case "count" -> validateNoLambda(name, transform, new VectorAggregationStep(VectorAggregationKind.COUNT));
        case "keys"   -> validateNoLambda(name, transform, new MapProjectionStep(MapProjectionKind.KEYS));
        case "values" -> validateNoLambda(name, transform, new MapProjectionStep(MapProjectionKind.VALUES));
        default -> {
            validateNoLambda(name, transform, null);
            yield new CollectionFunctionStep(name, args);
        }
    };
}
```

`extractLambdaTransform(...)` should return `null` when the argument block is absent or positional,
and otherwise visit the `allEntityTypes` child from `AT ARROW allEntityTypes`.
`validateAggregationArguments(...)` should reject positional arguments for `sum` and `prod`, so
`..sum(1 + 2)` does not remain valid under a more ambiguous spelling.

> **`ds()` compatibility**: `buildCollectionFunctionStep` currently accesses deep-scan arguments via
> `ctx.allEntityTypes()` (lines 549, 561). When the grammar changes `collectionFunctionAccess` to use
> `collectionFunctionArguments?`, these calls break. The `ds` case must be updated to extract the
> property name from `ctx.collectionFunctionArguments().positional...` (the `positionalCollectionFunctionArguments`
> alternative), since `ds("propName")` always uses positional args. Concretely:
>
> ```java
> if ("ds".equals(name)) {
>     List<ExpressionNode> dsArgs = extractPositionalArguments(argBlock);
>     String propName = dsArgs.isEmpty() ? null : ((LiteralNode) dsArgs.get(0)).value().toString();
>     return new PropertyChainNode.DeepScanStep(propName);
> }
> ```

### 3.3 Update visitor for `atReferenceTarget`

Add a visitor for the new grammar alternative. Follow the exact same convention as `buildFilterValue`
(line 732-742 of `SemanticAstBuilder`): emit `IdentifierNode("@")` when the chain is empty, and
`PropertyChainNode("@", chain)` otherwise. This keeps a single AST shape for `@` across both filter
predicates and transform bodies, avoiding two divergent paths in the resolver and evaluator.

```java
@Override
public ExpressionNode visitAtReferenceTarget(
        ExpressionEvaluatorParser.AtReferenceTargetContext ctx) {
    List<ExpressionEvaluatorParser.MemberChainContext> memberChains = ctx.memberChain();
    if (memberChains.isEmpty()) {
        // Bare '@' — same IdentifierNode convention as currentElementFilterValue.
        return new IdentifierNode(nodeFactory.nextId("identifier"), nodeFactory.sourceSpan(ctx), "@");
    }
    List<PropertyChainNode.MemberAccess> chain = memberChains.stream()
            .map(this::visitMemberChain)
            .toList();
    return new PropertyChainNode(nodeFactory.nextId("at"), nodeFactory.sourceSpan(ctx), "@", chain);
}
```

---

## Phase 4 — Semantic resolution

### 4.1 ~~Resolve `@` identifier~~ — **não é necessário**

A Fase 4.1 original propunha relaxar a checagem de `@` em `resolveIdentifier` e `resolvePropertyChain`
para aceitar `UnknownType` mesmo fora de um filtro. **Isso seria excessivamente permissivo** — deixaria
`@` escapar para contextos completamente inválidos (topo de expressão, argumentos de função, etc.).

O mecanismo existente já é suficiente: `resolveIdentifier` e `resolvePropertyChain` rejeitam `@` quando
`filterElementType == null`. A Fase 4.2 a seguir corrige o root cause definindo `filterElementType`
durante a resolução do transform. **Nenhuma alteração em `resolveIdentifier` ou `resolvePropertyChain`
é necessária.**

### 4.2 Resolve `VectorAggregationStep` with transform

**File:** `src/main/java/com/runestone/expeval/internal/runtime/SemanticResolver.java`

Extend the existing aggregation case to also resolve the optional transform, reusing the existing
`resolveFilterPredicate` helper — it saves and restores `filterElementType`, which is exactly what makes
`@` valid inside the transform body without relaxing the global check:

```java
case VectorAggregationStep(VectorAggregationKind kind, ExpressionNode transform) -> {
    if (transform != null) {
        // Sets filterElementType = UnknownType for the duration of transform resolution,
        // which is what makes '@' valid inside the body (same mechanism as [?(...)] filters).
        resolveFilterPredicate(transform, UnknownType.INSTANCE, node);
    }
    // result type: SUM/AVG/MIN/MAX/PROD/COUNT → SCALAR (ScalarType.NUMBER)
}
```

`resolveFilterPredicate` (lines 598-607) already handles save/restore:

```java
private void resolveFilterPredicate(ExpressionNode predicate, ResolvedType elementType,
                                    @Nullable PropertyChainNode node) {
    ResolvedType savedElementType = filterElementType;
    filterElementType = elementType;
    try {
        resolveExpression(predicate);
    } finally {
        filterElementType = savedElementType;
    }
}
```

No new helper needed — calling `resolveFilterPredicate(transform, UnknownType.INSTANCE, node)` is
sufficient and correct.

---

## Phase 5 — Execution layer

### 5.1 Extend `ExecutableVectorAggregation`

**File:** `src/main/java/com/runestone/expeval/internal/runtime/ExecutablePropertyChain.java`

Replace the existing `ExecutableVectorAggregation` record:

```java
/** {@code ..sum()}, {@code ..sum(@ -> @ * 2)}, {@code ..prod()}, {@code ..prod(@ -> @)}, etc. */
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

### 5.2 Build executable nodes in `ExecutionPlanBuilder`

**File:** `src/main/java/com/runestone/expeval/internal/runtime/ExecutionPlanBuilder.java`

In the member-access building switch:

```java
case VectorAggregationStep(VectorAggregationKind kind, ExpressionNode transform) ->
    new ExecutableVectorAggregation(kind,
        transform == null ? null : buildNode(transform, ...));
```

### 5.3 Evaluate in `AbstractObjectEvaluator`

**File:** `src/main/java/com/runestone/expeval/internal/runtime/AbstractObjectEvaluator.java`

In the `evaluatePropertyChain` step dispatch, extend the switch:

```java
case ExecutableVectorAggregation va -> applyAggregation(current, va.kind(), va.transform(), scope);
```

#### Extend `applyAggregation` for optional transform and `PROD`

The existing `applyAggregation(current, kind)` is replaced by a version that also accepts the optional
transform and scope. Keep the imperative loop style of the existing implementation for consistency.

```java
private Object applyAggregation(
        Object current,
        VectorAggregationKind kind,
        @Nullable ExecutableNode transform,
        ExecutionScope scope) {

    if (current instanceof Map<?, ?> m && kind == VectorAggregationKind.COUNT) {
        return BigDecimal.valueOf(m.size());
    }
    List<?> list = requireList(current, "aggregation");
    if (kind == VectorAggregationKind.COUNT) {
        return BigDecimal.valueOf(list.size());
    }
    // PROD with empty list → multiplicative identity
    if (list.isEmpty()) {
        return kind == VectorAggregationKind.PROD ? BigDecimal.ONE : null;
    }

    List<BigDecimal> values = toNumericList(list, transform, scope);
    BigDecimal acc = values.getFirst();
    switch (kind) {
        case SUM -> {
            for (int i = 1; i < values.size(); i++) acc = acc.add(values.get(i), mathContext);
            return acc;
        }
        case AVG -> {
            for (int i = 1; i < values.size(); i++) acc = acc.add(values.get(i), mathContext);
            return acc.divide(BigDecimal.valueOf(values.size()), mathContext);
        }
        case MIN -> {
            for (int i = 1; i < values.size(); i++) {
                BigDecimal v = values.get(i);
                if (v.compareTo(acc) < 0) acc = v;
            }
            return acc;
        }
        case MAX -> {
            for (int i = 1; i < values.size(); i++) {
                BigDecimal v = values.get(i);
                if (v.compareTo(acc) > 0) acc = v;
            }
            return acc;
        }
        case PROD -> {
            for (int i = 1; i < values.size(); i++) acc = acc.multiply(values.get(i), mathContext);
            return acc;
        }
        default -> throw new IllegalStateException("Unhandled aggregation kind: " + kind);
    }
}
```

`toNumericList` applies the transform per element, binding `@` via the existing `FilterContextStack`.

> **API note:** `FilterContextStack` does not have a `push()` returning a context object.
> The correct pattern — matching `applyFilter` — is `pushElement(element)` / `pop()` per element:

```java
private List<BigDecimal> toNumericList(List<?> list, @Nullable ExecutableNode transform,
                                       ExecutionScope scope) {
    if (transform == null) {
        List<BigDecimal> result = new ArrayList<>(list.size());
        for (Object element : list) result.add(asBigDecimal(element));
        return result;
    }
    // Reuse FilterContextStack for per-element @-binding (same mechanism as [?(...)] filters).
    // pushElement + pop per iteration (not push-once + rebind) to support nested transforms.
    FilterContextStack stack = FILTER_CTX.get();
    List<BigDecimal> result = new ArrayList<>(list.size());
    for (Object element : list) {
        stack.pushElement(element);
        try {
            result.add(asBigDecimal(evaluateExpr(transform, scope)));
        } finally {
            stack.pop();
        }
    }
    return result;
}
```

Note: `AVG`, `MIN`, `MAX` do not accept a transform in this plan (they already exist without one).
If a transform is passed to these, throw `IllegalArgumentException` at plan-build time.

---

## Phase 6 — Tests

**New test file:** `src/test/java/com/runestone/expeval/api/VectorHigherOrderFunctionsTest.java`

Cover:

```java
// sum with transform
"[1, 2, 3]..sum(@ -> @ * 2)"                  // → 12
"[1, 2, 3]..sum(@ -> @ ^ 2)"                  // → 14
"[1, 2, 3]..sum(@ -> @ * 2^@)"                // → 34

// prod without transform (product of elements)
"[1, 2, 3]..prod()"                            // → 6
"[2, 3, 4]..prod()"                            // → 24

// prod with transform
"[1, 2, 3]..prod(@ -> @ * 2)"                 // → 2 * 4 * 6 = 48

// sum without transform (existing — must still work)
"[1, 2, 3]..sum()"                             // → 6

// external variable in transform expression
"[1, 2, 3]..sum(@ -> @ + offset)"             // offset is external symbol

// edge cases
"[]..prod()"                                   // → 1 (identity)
"[5]..sum(@ -> @ * 2)"                        // → 10

// invalid usages
"[1, 2, 3]..sum(@ * 2)"                       // reject: transform requires explicit @ -> marker
"[1, 2, 3]..sum(1 + 2)"                       // reject: positional args are not valid for sum/prod
"[1, 2, 3]..avg(@ -> @ * 2)"                  // reject: avg does not accept a lambda in this plan
"[1, 2, 3]..custom(@ -> @ * 2)"               // reject: custom collection functions keep positional args only
```

---

## Critical files to modify

| File | Change |
|------|--------|
| `grammar/ExpressionEvaluator.g4` | Add `ARROW`, `collectionFunctionArguments`, `lambdaTransform`, and `AT memberChain*` in `referenceTarget` |
| `grammar/` (generated) | Regenerate with ANTLR |
| `internal/navigation/VectorAggregationKind.java` | Add `PROD` |
| `internal/ast/PropertyChainNode.java` | Add `@Nullable ExpressionNode transform` to `VectorAggregationStep` |
| `internal/ast/mapping/SemanticAstBuilder.java` | Extend `visitCollectionFunctionAccess` for `prod`/`sum(@ -> expr)`; add `visitAtReferenceTarget` |
| `internal/runtime/SemanticResolver.java` | Resolve transform in `VectorAggregationStep` via `resolveFilterPredicate` (no changes to `resolveIdentifier`) |
| `internal/runtime/ExecutablePropertyChain.java` | Add optional `transform` to `ExecutableVectorAggregation` |
| `internal/runtime/ExecutionPlanBuilder.java` | Handle updated `VectorAggregationStep` |
| `internal/runtime/AbstractObjectEvaluator.java` | Extend `applyAggregation` for transform and `PROD` |

**No new files** beyond the test class. No lambda types, no new catalog functions, no new type-system
entries.

---

## What this plan does NOT introduce

This plan introduces only a **narrow lambda marker** for `sum` and `prod`: `@ -> expr`.
It does **not** introduce a general-purpose lambda/closure system such as `(x) => x * 2`.

| Original | Replaced by |
|----------|-------------|
| `ARROW` lexer token (`=>`) | `ARROW` lexer token (`->`) used only for `@ -> expr` |
| `lambdaExpression` grammar rule | Narrow `lambdaTransform : AT ARROW allEntityTypes` |
| `LambdaType` / `LambdaValue` | Not needed |
| `LambdaNode` / `ExecutableLambda` | Not needed |
| `SymbolKind.LAMBDA` | Not needed |
| Lambda scope stack in `SemanticResolver` | Not needed |
| `ExecutionScope.withLambdaLayer` | Not needed |
| `VectorFunctions` catalog class | Not needed |

The `@` element placeholder and `FilterContext` pooling — both already implemented for `[?(pred)]`
— are reused for the new steps. The only new syntax marker is `->`, and only inside
`..sum(@ -> ...)` / `..prod(@ -> ...)`.

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
| `[1, 2, 3]..sum(@ -> @ * 2^@)` | 1·2 + 2·4 + 3·8 = **34** |
| `[1, 2, 3]..prod()` | 1·2·3 = **6** |
| `[]..prod()` | **1** (multiplicative identity) |
