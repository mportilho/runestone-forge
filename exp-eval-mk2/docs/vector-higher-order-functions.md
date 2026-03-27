# Plan: Vector Higher-Order Functions (filter, sum, prod) for exp-eval-mk2

## Context

The `exp-eval-mk2` module currently supports vector literals (`[1, 2, 3]`) and functions that
accept arrays (`max([1,2,3])`), but has no way to pass a **transformation or predicate inline**
with a call. The user wants to write expressions like:

```
filter([1, 2, 3], (x) => x > 1)
sum([1, 2, 3], (x) => x * 2^x)
prod([1, 2, 3], (x) => x * 2^x)
```

None of this is possible today: the grammar has no arrow/lambda syntax, the type system has no
`LambdaType`, and there are no higher-order built-ins. Everything must be built from scratch while
fitting into the existing compilation pipeline.

---

## Architecture overview (existing pipeline recap)

```
Source → [ANTLR] → AST (ExpressionNode tree)
       → [SemanticResolver] → SemanticModel (type bindings, function bindings)
       → [ExecutionPlanBuilder] → ExecutionPlan (ExecutableNode tree)
       → [AbstractObjectEvaluator] → Object result
```

Key constraints:
- `ExpressionNode` and `ExecutableNode` are **sealed interfaces** — every new node type must be
  added to the `permits` clause.
- `ResolvedType` is a **sealed interface** — `LambdaType` must be added to `permits`.
- `SymbolKind` has `EXTERNAL` and `INTERNAL` — lambda params need a new `LAMBDA` kind so they
  don't collide in the scope map.
- `ExecutionScope` is package-private in `internal.runtime`, so the child-scope factory method
  can be added there without breaking encapsulation.

---

## Phase 1 — Grammar

**File:** `exp-eval-mk2/src/main/antlr4/com/runestone/expeval2/internal/grammar/ExpressionEvaluatorV2.g4`

### 1.1 New lexer token
Add after the `NEQ` token (line 62), before the date/time tokens:

```antlr
ARROW : '=>' ;
```

`=>` doesn't conflict with existing tokens (`>=` is `GE`, `=` is `EQ`, `>` is `GT`).

### 1.2 New parser rule

```antlr
lambdaExpression
    : LPAREN IDENTIFIER (COMMA IDENTIFIER)* RPAREN ARROW allEntityTypes  # multiParamLambdaOperation
    | IDENTIFIER ARROW allEntityTypes                                      # singleParamLambdaOperation
    ;
```

- Multi-param: `(x, y) => x + y`
- Single-param (no parens): `x => x + 1`

### 1.3 Add to `allEntityTypes` (FIRST alternative — highest priority)

```antlr
allEntityTypes
    : lambdaExpression                                                     # lambdaEntityType   ← NEW
    | mathExpression                                                       # mathEntityType
    | logicalExpression                                                    # logicalEntityType
    ...
```

Putting `lambdaExpression` first lets ANTLR's LL(*) prediction pick it up before ambiguous
`(x)` parenthesized-expression paths. The `ARROW` token makes it unambiguous at lookahead ≤ 4.

### 1.4 Regenerate the parser

Follow the ANTLR regeneration steps in `CLAUDE.md`. Copy only the generated `.java` files into:
`exp-eval-mk2/src/main/java/com/runestone/expeval2/internal/grammar/`

---

## Phase 2 — Type system

### 2.1 New `LambdaType`
**New file:** `src/main/java/com/runestone/expeval2/types/LambdaType.java`

```java
package com.runestone.expeval2.types;

public enum LambdaType implements ResolvedType {
    INSTANCE
}
```

Singleton enum — same pattern as `VectorType` and `UnknownType`.

### 2.2 Update `ResolvedType` sealed interface
**File:** `src/main/java/com/runestone/expeval2/types/ResolvedType.java`

Add `LambdaType` to the `permits` clause:
```java
public sealed interface ResolvedType permits ScalarType, UnknownType, VectorType, ObjectType, NullType, LambdaType {
}
```

### 2.3 New `LambdaValue` runtime interface
**New file:** `src/main/java/com/runestone/expeval2/internal/runtime/LambdaValue.java`

```java
package com.runestone.expeval2.internal.runtime;

@FunctionalInterface
interface LambdaValue {
    Object apply(Object... args);
}
```

Package-private — it is an internal runtime concept, not part of the public API.

### 2.4 Register `LambdaValue` in `ResolvedTypes.fromJavaType()`
**File:** `src/main/java/com/runestone/expeval2/types/ResolvedTypes.java`

In `fromJavaType(Class<?>)`, add a case before the fallthrough:
```java
if (javaType == LambdaValue.class) return LambdaType.INSTANCE;
```

This lets `FunctionDescriptor` resolve the parameter type of `VectorFunctions.filter` etc.
correctly during catalog registration.

---

## Phase 3 — AST layer

### 3.1 New `LambdaNode`
**New file:** `src/main/java/com/runestone/expeval2/internal/ast/LambdaNode.java`

```java
package com.runestone.expeval2.internal.ast;

import java.util.List;
import java.util.Objects;

public record LambdaNode(
        NodeId nodeId,
        SourceSpan sourceSpan,
        List<String> parameters,
        ExpressionNode body
) implements ExpressionNode {
    public LambdaNode {
        AstValidation.requireNodeId(nodeId);
        AstValidation.requireSourceSpan(sourceSpan);
        parameters = AstValidation.copyList(
                Objects.requireNonNull(parameters, "parameters must not be null"), "parameters");
        Objects.requireNonNull(body, "body must not be null");
        if (parameters.isEmpty()) throw new IllegalArgumentException("lambda must have at least one parameter");
    }
}
```

### 3.2 Add `LambdaNode` to `ExpressionNode` sealed interface
**File:** `src/main/java/com/runestone/expeval2/internal/ast/ExpressionNode.java`

```java
public sealed interface ExpressionNode extends Node permits
    BinaryOperationNode, ConditionalNode, FunctionCallNode, IdentifierNode,
    LiteralNode, LambdaNode, PostfixOperationNode, PropertyChainNode,     // ← add LambdaNode
    UnaryOperationNode, VectorLiteralNode {
}
```

### 3.3 Add visitor in `SemanticAstBuilder`
**File:** `src/main/java/com/runestone/expeval2/internal/ast/mapping/SemanticAstBuilder.java`

```java
@Override
public ExpressionNode visitMultiParamLambdaOperation(
        ExpressionEvaluatorV2Parser.MultiParamLambdaOperationContext ctx) {
    List<String> params = ctx.IDENTIFIER().stream()
            .map(ParseTree::getText)
            .toList();
    ExpressionNode body = visitAllEntityType(ctx.allEntityTypes());
    return new LambdaNode(nodeFactory.nextId("lambda"), nodeFactory.sourceSpan(ctx), params, body);
}

@Override
public ExpressionNode visitSingleParamLambdaOperation(
        ExpressionEvaluatorV2Parser.SingleParamLambdaOperationContext ctx) {
    List<String> params = List.of(ctx.IDENTIFIER().getText());
    ExpressionNode body = visitAllEntityType(ctx.allEntityTypes());
    return new LambdaNode(nodeFactory.nextId("lambda"), nodeFactory.sourceSpan(ctx), params, body);
}
```

---

## Phase 4 — Semantic resolution

### 4.1 New `SymbolKind.LAMBDA`
**File:** `src/main/java/com/runestone/expeval2/internal/runtime/SymbolKind.java`

```java
enum SymbolKind {
    EXTERNAL,
    INTERNAL,
    LAMBDA    // ← new: for lambda-bound parameters
}
```

### 4.2 Extend `ResolutionSession` inside `SemanticResolver`

Add a `Deque<Map<String, SymbolRef>> lambdaScopes` field to `ResolutionSession`.

In `resolveExpression`, add a case for `LambdaNode`:

```java
case LambdaNode lambda -> resolveLambda(lambda);
```

New method:
```java
private ResolvedType resolveLambda(LambdaNode node) {
    // Build SymbolRef for each parameter and push a new lambda scope
    Map<String, SymbolRef> paramRefs = new LinkedHashMap<>();
    for (String param : node.parameters()) {
        SymbolRef ref = new SymbolRef(param, SymbolKind.LAMBDA);
        paramRefs.put(param, ref);
        resolvedTypes.put(/* nodeId for the param reference */ ..., UnknownType.INSTANCE);
    }
    lambdaScopes.push(paramRefs);

    // Store SymbolRef map in SemanticModel for later use by ExecutionPlanBuilder
    lambdaParametersByNodeId.put(node.nodeId(), paramRefs);

    // Resolve the body with lambda params in scope
    ResolvedType bodyType = resolveExpression(node.body());
    lambdaScopes.pop();

    resolvedTypes.put(node.nodeId(), LambdaType.INSTANCE);
    return LambdaType.INSTANCE;
}
```

In `resolveIdentifier`, check `lambdaScopes` before `externalSymbolsByName` / `internalSymbolsByName`:
```java
for (Map<String, SymbolRef> scope : lambdaScopes) {
    if (scope.containsKey(node.name())) {
        SymbolRef ref = scope.get(node.name());
        symbolByNodeId.put(node.nodeId(), ref);
        return UnknownType.INSTANCE;   // runtime type is unknown at compile time
    }
}
// ... existing external/internal resolution
```

### 4.3 Extend `SemanticModel`
**File:** `src/main/java/com/runestone/expeval2/internal/runtime/SemanticModel.java`

Add:
```java
Map<NodeId, Map<String, SymbolRef>> lambdaParametersByNodeId;

public Optional<Map<String, SymbolRef>> findLambdaParameters(NodeId nodeId) {
    return Optional.ofNullable(lambdaParametersByNodeId.get(nodeId));
}
```

---

## Phase 5 — Execution layer

### 5.1 New `ExecutableLambda`
**New file:** `src/main/java/com/runestone/expeval2/internal/runtime/ExecutableLambda.java`

```java
package com.runestone.expeval2.internal.runtime;

import java.util.List;
import java.util.Objects;

record ExecutableLambda(
        List<SymbolRef> parameterRefs,
        ExecutableNode body
) implements ExecutableNode {
    ExecutableLambda {
        parameterRefs = List.copyOf(Objects.requireNonNull(parameterRefs, "parameterRefs must not be null"));
        Objects.requireNonNull(body, "body must not be null");
    }
}
```

### 5.2 Add `ExecutableLambda` to `ExecutableNode` sealed interface
**File:** `src/main/java/com/runestone/expeval2/internal/runtime/ExecutableNode.java`

Add `ExecutableLambda` to the `permits` clause.

### 5.3 Add child-scope factory to `ExecutionScope`
**File:** `src/main/java/com/runestone/expeval2/internal/runtime/ExecutionScope.java`

```java
/**
 * Creates a read-only child scope that overlays lambda-parameter bindings
 * on top of the parent scope's resolved values.
 */
ExecutionScope withLambdaLayer(Map<SymbolRef, Object> lambdaBindings) {
    // The lambda bindings are the primary layer; parent's values becomes secondary.
    // Audit is inherited from parent.
    return new ExecutionScope(lambdaBindings, values, secondaryValues, false, audit);
}
```

Note: `tertiaryValues` only handles two levels; if a parent already has three layers, consider
composing differently. In practice, nested lambdas should be handled by chaining
`withLambdaLayer` calls.

### 5.4 Build `ExecutableLambda` in `ExecutionPlanBuilder`
**File:** `src/main/java/com/runestone/expeval2/internal/runtime/ExecutionPlanBuilder.java`

In the main `buildNode` switch, add:

```java
case LambdaNode lambda -> {
    Map<String, SymbolRef> paramRefs = model.findLambdaParameters(lambda.nodeId())
            .orElseThrow(() -> new IllegalStateException("missing lambda parameters for node " + lambda.nodeId()));
    List<SymbolRef> refs = List.copyOf(paramRefs.values());
    ExecutableNode body = buildNode(lambda.body(), model, runtimeServices, externalSymbolCatalog, typeHintCatalog);
    yield new ExecutableLambda(refs, body);
}
```

Also update `isConstantNode` and `countNodeEvents` to handle `ExecutableLambda` (lambdas are never
constant-folded; their event cost equals the body's cost).

### 5.5 Evaluate `ExecutableLambda` in `AbstractObjectEvaluator`
**File:** `src/main/java/com/runestone/expeval2/internal/runtime/AbstractObjectEvaluator.java`

In the main `evaluateExpr` switch:

```java
case ExecutableLambda lambda -> buildLambdaValue(lambda, scope);
```

New private method:

```java
private LambdaValue buildLambdaValue(ExecutableLambda node, ExecutionScope capturedScope) {
    List<SymbolRef> paramRefs = node.parameterRefs();
    ExecutableNode body = node.body();
    return args -> {
        Map<SymbolRef, Object> bindings = new HashMap<>(paramRefs.size() * 2);
        for (int i = 0; i < paramRefs.size(); i++) {
            bindings.put(paramRefs.get(i), args[i]);
        }
        ExecutionScope lambdaScope = capturedScope.withLambdaLayer(bindings);
        return evaluateExpr(body, lambdaScope);
    };
}
```

---

## Phase 6 — Vector built-in functions

### 6.1 New `VectorFunctions` class
**New file:** `src/main/java/com/runestone/expeval2/catalog/functions/VectorFunctions.java`

```java
package com.runestone.expeval2.catalog.functions;

import com.runestone.expeval2.internal.runtime.LambdaValue;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

public class VectorFunctions {

    // filter([1, 2, 3], (x) => x > 1)  →  [2, 3]
    public static List<Object> filter(List<Object> vector, LambdaValue predicate) {
        List<Object> result = new ArrayList<>();
        for (Object element : vector) {
            Object test = predicate.apply(element);
            if (Boolean.TRUE.equals(test)) {
                result.add(element);
            }
        }
        return result;
    }

    // sum([1, 2, 3], (x) => x * 2)  →  Σ f(x)
    public static BigDecimal sum(MathContext mc, List<Object> vector, LambdaValue transform) {
        BigDecimal acc = BigDecimal.ZERO;
        for (Object element : vector) {
            Object val = transform.apply(element);
            acc = acc.add(toBigDecimal(val), mc);
        }
        return acc;
    }

    // prod([1, 2, 3], (x) => x * 2)  →  Π f(x)
    public static BigDecimal prod(MathContext mc, List<Object> vector, LambdaValue transform) {
        BigDecimal acc = BigDecimal.ONE;
        for (Object element : vector) {
            Object val = transform.apply(element);
            acc = acc.multiply(toBigDecimal(val), mc);
        }
        return acc;
    }

    private static BigDecimal toBigDecimal(Object value) {
        return switch (value) {
            case BigDecimal bd -> bd;
            case Number n     -> BigDecimal.valueOf(n.doubleValue());
            default -> throw new IllegalArgumentException("lambda returned non-numeric value: " + value);
        };
    }
}
```

`MathContext` as the first parameter of `sum`/`prod` is automatically injected by
`ExpressionEnvironmentBuilder` (existing behaviour in `toDescriptor`). It is not visible at the
call site.

**Important note on foldability:** `VectorFunctions` must be registered as **non-foldable**
(`foldable = false`) because the lambda closure captures runtime scope and cannot be evaluated
at compile time.

### 6.2 Register in `ExpressionEnvironmentBuilder`
**File:** `src/main/java/com/runestone/expeval2/environment/ExpressionEnvironmentBuilder.java`

```java
public ExpressionEnvironmentBuilder addVectorFunctions() {
    return registerStaticProvider(VectorFunctions.class, false);
}
```

Add to `addAllFunctions()`:
```java
.addVectorFunctions()
```

---

## Phase 7 — Tests

**New test file:** `src/test/java/com/runestone/expeval2/api/VectorHigherOrderFunctionsTest.java`

Cover:
- `filter([1, 2, 3], x => x > 1)` → `[2, 3]`
- `filter([1, 2, 3], (x) => x > 1)` → `[2, 3]`  (multi-param syntax with 1 param)
- `sum([1, 2, 3], x => x * 2)` → `12`
- `prod([1, 2, 3], x => x)` → `6`
- `sum([1, 2, 3], x => x^2)` → `14`
- Variable in lambda body (`sum([a, b, c], x => x + offset)`) where `offset` is external
- Nested: `filter(filter([1,2,3,4], x => x > 1), y => y < 4)` → `[2, 3]`
- Empty vector: `filter([], x => x > 0)` → `[]`
- Type error in lambda predicate returning non-boolean (should produce a semantic/runtime error)

---

## Critical files to modify

| File | Change |
|------|--------|
| `grammar/ExpressionEvaluatorV2.g4` | Add `ARROW` token, `lambdaExpression` rule, integrate into `allEntityTypes` |
| `grammar/` (generated) | Regenerate with ANTLR |
| `types/ResolvedType.java` | Add `LambdaType` to `permits` |
| `types/LambdaType.java` | **New** — singleton enum |
| `types/ResolvedTypes.java` | Map `LambdaValue.class` → `LambdaType.INSTANCE` |
| `internal/runtime/LambdaValue.java` | **New** — package-private functional interface |
| `internal/ast/ExpressionNode.java` | Add `LambdaNode` to `permits` |
| `internal/ast/LambdaNode.java` | **New** — record with `List<String> parameters` + `ExpressionNode body` |
| `internal/ast/mapping/SemanticAstBuilder.java` | Add two visitor methods for lambda rules |
| `internal/runtime/SymbolKind.java` | Add `LAMBDA` enum constant |
| `internal/runtime/SemanticResolver.java` | Lambda scope stack in `ResolutionSession`; resolve lambda identifier refs |
| `internal/runtime/SemanticModel.java` | Add `lambdaParametersByNodeId` map + accessor |
| `internal/runtime/ExecutableNode.java` | Add `ExecutableLambda` to `permits` |
| `internal/runtime/ExecutableLambda.java` | **New** — record with `List<SymbolRef>` + `ExecutableNode body` |
| `internal/runtime/ExecutionScope.java` | Add `withLambdaLayer(Map<SymbolRef, Object>)` factory |
| `internal/runtime/ExecutionPlanBuilder.java` | Handle `LambdaNode` case; update `isConstantNode` / `countNodeEvents` |
| `internal/runtime/AbstractObjectEvaluator.java` | Handle `ExecutableLambda` → produce `LambdaValue` closure |
| `catalog/functions/VectorFunctions.java` | **New** — `filter`, `sum`, `prod` |
| `environment/ExpressionEnvironmentBuilder.java` | Add `addVectorFunctions()`, include in `addAllFunctions()` |

---

## Verification

```shell
# 1. Regenerate grammar (see CLAUDE.md)
# 2. Build + test
mvn clean test -pl exp-eval-mk2

# 3. Run specific test class
mvn clean test -pl exp-eval-mk2 -Dtest=VectorHigherOrderFunctionsTest

# 4. Full build to catch cross-module breakage
mvn clean install
```

Manually verify edge cases:
- `sum([1,2,3], x => x * 2^x)` = 1·2 + 2·4 + 3·8 = 2 + 8 + 24 = **34**
- `prod([1,2,3], x => x)` = 1·2·3 = **6**
- `filter([1,2,3], x => x > 1)` = **[2, 3]**
