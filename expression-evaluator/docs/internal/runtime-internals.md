# expression-evaluator — Runtime Internals Reference

Verified reference for the current `expression-evaluator` runtime. Use this document before reverse-engineering behavior from scattered tests or implementation details.

---

## 1. Public Surface

### Compile-time entry points

- `MathExpression.compile(...)`
- `LogicalExpression.compile(...)`
- `AssignmentExpression.compile(...)`
- `MathExpression.validate(...)`
- `LogicalExpression.validate(...)`
- `AssignmentExpression.validate(...)`

### Evaluation entry points

- `MathExpression.compute(...)` → `BigDecimal`
- `LogicalExpression.compute(...)` → `boolean`
- `AssignmentExpression.compute(...)` → `Map<String, Object>`
- `computeWithAudit(...)` on all three APIs → `AuditResult<T>`

### Runtime configuration

- `ExpressionEnvironment.builder()`
- `ExpressionEnvironmentBuilder.empty()`
- `CacheConfig`
- `ExpressionRuntimeSupport`
- `ExpressionCompiler`

The default environment is empty. No function providers are registered unless the caller adds them explicitly.

---

## 2. End-to-End Pipeline

```text
source string
  -> ExpressionEvaluatorParserFacade
  -> SemanticAstBuilder
  -> SemanticResolver
  -> ExecutionPlanBuilder
  -> MathEvaluator / LogicalEvaluator
```

### Stage responsibilities

1. `ExpressionEvaluatorParserFacade`
   Wraps the ANTLR parser and uses `PredictionStrategy` to try `SLL` first, then `LL` fallback.
2. `SemanticAstBuilder`
   Converts parse trees into typed AST nodes under `internal.ast`.
3. `SemanticResolver`
   Resolves identifiers, assignments, function overloads, property chains, and `ResolvedType`s into a `SemanticModel`.
4. `ExecutionPlanBuilder`
   Assigns deterministic symbol indices, seeds external defaults, folds constants, compiles regex patterns, and produces an `ExecutionPlan`.
5. `MathEvaluator` / `LogicalEvaluator`
   Execute the compiled plan inside an `ExecutionScope`.

`ExpressionCompiler` caches the compiled result by `(source, environmentId, resultType)`.

---

## 3. Runtime Value Model

The engine carries plain Java objects through the runtime. There is no dedicated `RuntimeValue` wrapper layer.

### Common runtime shapes

| Expression kind | Runtime representation |
|---|---|
| number | `BigDecimal` |
| boolean | `Boolean` |
| text | `String` |
| date | `LocalDate` |
| time | `LocalTime` |
| datetime | `LocalDateTime` |
| vector | `List<Object>` |
| object navigation roots and members | arbitrary Java objects |

Normalization happens through `RuntimeServices.coerceToResolvedType(...)` at the edges:

- external symbol defaults
- external symbol overrides
- function results
- property/method return values when the member has a resolved type

---

## 4. Environment Assembly

`ExpressionEnvironmentBuilder` constructs the runtime contract:

- function catalog
- external symbol catalog
- type-hint catalog
- data conversion service
- `MathContext`
- transcendental `MathContext`
- deterministic `ExpressionEnvironmentId`

### Built-in provider helpers

| Builder method | Registers |
|---|---|
| `addComparableFunctions()` | `ComparableFunctions` |
| `addDateTimeFunctions()` | `DateTimeFunctions` |
| `addExcelFunctions()` | `ExcelFinancialFunctions` |
| `addMathFunctions()` | `MathFunctions` + `LogarithmFunctions` |
| `addStringFunctions()` | `StringFunctions` |
| `addTrigonometryFunctions()` | `TrigonometryFunctions` |
| `addAllFunctions()` | all of the above |

### Provider discovery rules

- `registerStaticProvider(Class<?>)` discovers public static methods via `getMethods()`.
- `registerInstanceProvider(Object)` discovers public instance methods on the given object.
- Each discovered method becomes a `FunctionDescriptor`.
- A leading `MathContext` parameter is injected at environment-build time and is hidden from expression-call arity.

### `MathContext` injection

- `TrigonometryFunctions` and `LogarithmFunctions` receive `transcendentalMathContext`.
- All other providers receive `mathContext`.
- Both contexts default to `MathContext.DECIMAL128`.

### Trigonometric constants

If `TrigonometryFunctions` is registered, the builder also registers these non-overridable external symbols:

- `pi`
- `π`
- `e`
- `tau`
- `τ`

### Environment identity

`ExpressionEnvironmentId` is derived from a sorted list of:

- static provider class names
- instance provider class names plus identity hash codes
- external symbol names, declared types, and overridability
- registered type hints
- both `MathContext` settings

The builder hashes that content with SHA-256 and stores the first 8 bytes as a 16-character hex string.

---

## 5. Type System

### `ResolvedType` variants in active use

| Type | Meaning | Produced by |
|---|---|---|
| `ScalarType` | number, boolean, string, date, time, datetime | literals, function signatures, coercible Java types |
| `VectorType.INSTANCE` | vectors | arrays, collections, vector literals |
| `ObjectType(javaClass)` | typed object navigation target | registered type hints and hinted member return types |
| `UnknownType.INSTANCE` | runtime-known but semantically unresolved | unhinted objects, unknown symbols, ambiguous dynamic cases |
| `NullType.INSTANCE` | null value in the type lattice | literal `null`, safe-navigation propagation |

### `ResolvedTypes.fromJavaType(Class<?>)`

| Java type | Resolved type |
|---|---|
| primitive numeric types, `Number`, `BigDecimal`, `BigInteger` | `ScalarType.NUMBER` |
| `boolean`, `Boolean` | `ScalarType.BOOLEAN` |
| `CharSequence`, `String` | `ScalarType.STRING` |
| `LocalDate` | `ScalarType.DATE` |
| `LocalTime` | `ScalarType.TIME` |
| `LocalDateTime` | `ScalarType.DATETIME` |
| arrays, `Collection` | `VectorType.INSTANCE` |
| anything else | `UnknownType.INSTANCE` |

`ObjectType` is not returned by `ResolvedTypes.fromJavaType(...)`. It is introduced only when a class is registered through `registerTypeHint(...)`.

### `ResolvedTypes.merge(left, right)`

The merge rules are:

1. same type → keep it
2. `NullType` on one side → return the other side
3. `UnknownType` on one side → return the other side
4. otherwise → `UnknownType.INSTANCE`

That rule is used heavily in conditionals and `??`.

---

## 6. Type Hints And Object Navigation

`registerTypeHint(Class<?>)` is the switch that turns object navigation from "best effort" into statically-known navigation.

### Metadata discovery

For each registered type, the builder creates `TypeMetadata` with:

1. record component accessors
2. zero-arg JavaBean getters (`getXxx()` and boolean `isXxx()`)
3. public fields
4. instance methods

For members that return another registered type, the resolved type becomes `ObjectType(thatClass)`, which enables multi-hop typed navigation.

### Semantic behavior

`SemanticResolver` resolves a chain from left to right:

- root symbol
- property or method access
- optional safe navigation

If the current type is `ObjectType`, the resolver validates:

- property existence
- method existence
- method arity
- overload ambiguity
- argument compatibility

If the current type is `UnknownType`, the resolver tolerates the chain and keeps the result as `UnknownType`. This is why property chains can still compile without registered type hints.

### Runtime behavior

`ExecutablePropertyChain` has two execution modes:

- typed access with precomputed `MethodHandle`s
- reflective fallback through `ReflectiveAccessCache`

### Null handling in chains

- `obj.prop` or `obj.method()` throws `ExpressionEvaluationException` with code `NULL_IN_CHAIN` when a null intermediate value is reached.
- `obj?.prop` or `obj?.method()` returns `null` for the rest of the chain.
- `??` can be used after a reference expression to provide a default.

---

## 7. External Symbols And Execution Scope

### External symbol registration

`registerExternalSymbol(name, defaultValue, overridable)` records:

- symbol name
- declared type
- default value
- overridability

The declared type comes from the default value class, except when that class is a registered type hint. In that case the declared type becomes `ObjectType`.

### Compile-time treatment

During plan building:

- external and internal symbols receive deterministic zero-based indices, sorted alphabetically within each group
- defaults are stored in an array aligned with external symbol indices
- binding metadata is stored in `ExternalBindingPlan`
- non-overridable external symbols are inserted into the folding map immediately

That last rule means constants registered as non-overridable can be folded into `ExecutableLiteral`s before runtime.

### `ExecutionScope` layout

`ExecutionScope` uses array-backed layers, not maps.

#### Mutable scopes

Used when the expression has assignments.

- layer 1: internal assignment results
- layer 2: external overrides
- layer 3: external defaults

#### Read-only scopes

Used when the expression has no assignments.

- layer 1: shared values or overrides
- layer 2: defaults when needed

### `UNBOUND`

`ExecutionScope.UNBOUND` is a sentinel distinct from `null`.

- `UNBOUND` means "no binding exists"
- `null` means "binding exists and its value is null"

This distinction matters for:

- nullable inputs
- destructuring assignments with missing elements
- null-coalescing logic
- audit correctness

### Dynamic instants

`currDate`, `currTime`, and `currDateTime` are resolved once per scope and cached in an `EnumMap<DynamicInstant, Object>`.

Repeated reads inside one evaluation return the same instant.

---

## 8. Runtime Coercion

There are two coercion layers:

1. `RuntimeServices.coerce(...)`
   Coerces to a specific Java parameter type.
2. `RuntimeServices.coerceToResolvedType(...)`
   Normalizes values to a semantic type when the runtime already knows the expected `ResolvedType`.

### `RuntimeCoercionService.coerce(value, targetType)` order

```text
1. targetType null check
2. value == null -> null
3. targetType == Object.class or targetType.isInstance(value) -> value
4. BigDecimal
5. Double / double
6. Integer / int
7. Long / long
8. Boolean / boolean
9. String
10. LocalDate
11. LocalTime
12. LocalDateTime
13. List handling
14. conversionService.convert(...)
```

### List handling

If the runtime value is `List<?>`:

- target `List` → copy into `new ArrayList<>(elements)`
- non-array target → delegate to `conversionService`
- array target → build an array element-by-element

Array coercion has specialized fast paths for:

- `BigDecimal[]`
- `double[]`
- `int[]`
- `long[]`

Reference arrays use a direct `Object[]` write path. Unsupported primitive arrays fall back to reflective `Array.set(...)`.

### `coerceToResolvedType(...)`

Important cases:

- `UnknownType` and `null` resolved type → return value unchanged
- `VectorType` → arrays are converted to `List<Object>`; lists are preserved
- scalar types → convert to `BigDecimal`, `Boolean`, `String`, `LocalDate`, `LocalTime`, or `LocalDateTime`
- `ObjectType` → value is preserved

This method is used when:

- seeding external defaults
- applying user overrides
- returning function results
- returning typed property/method results

---

## 9. Semantic Resolution And Overloads

### Function overload resolution

`SemanticResolver` resolves function calls by:

1. candidate name
2. exact arity
3. compatible argument types

Compatibility rule:

- if actual type is `UnknownType` or `NullType`, it does not reject the candidate
- if expected type is `UnknownType`, it does not reject the candidate
- otherwise actual and expected types must match exactly

Failure modes:

- no name match → `UNKNOWN_FUNCTION`
- name match but no arity match → `INVALID_FUNCTION_ARITY`
- arity match but no compatible overload → `INCOMPATIBLE_FUNCTION_ARGUMENTS`
- more than one compatible overload → `AMBIGUOUS_FUNCTION`

### Method overload resolution

Typed member-method calls use the same overall shape:

- filter by name
- filter by arity
- compare resolved argument types against method parameter Java types converted through `resolveJavaType(...)`

Failure modes:

- `UNKNOWN_METHOD`
- `INVALID_METHOD_ARITY`
- `INCOMPATIBLE_METHOD_ARGUMENTS`
- `AMBIGUOUS_METHOD`

### Top-level result type checks

After semantic resolution:

- math expressions must resolve to `ScalarType.NUMBER`
- logical expressions must resolve to `ScalarType.BOOLEAN`

One special tolerance exists: a top-level property chain that still resolves to `UnknownType` is accepted so dynamic navigation without type hints can compile.

---

## 10. Plan Building And Constant Folding

`ExecutionPlanBuilder` is where most runtime optimizations happen.

### Constant sources

The folding map starts with:

- non-overridable external symbols

Then it grows as the builder walks assignments and expressions.

### Folding behavior

The builder folds:

- binary operators when both sides are constant
- unary operators when the operand is constant
- postfix operators when the operand is constant
- `between` when all three operands are constant
- `??` when the left side is a known non-null constant
- foldable function calls when every argument is constant
- vector literals when every element is constant
- conditionals with constant conditions

### Assignment propagation

Simple assignments participate in constant propagation:

- `x = 10; x + 5` can collapse to a literal result
- reassignment to a non-constant removes the old folded value
- reassignment to a new constant updates the propagated value

Destructuring assignments do not publish per-target constant bindings during plan build.

### Folded function calls keep audit data

`ExecutableFunctionCall.folded(...)` stores:

- original executable arguments
- folded argument values
- precomputed result

At runtime the evaluator returns the folded result directly, but still emits a `FunctionCall` audit event.

### Regex compilation

Regex operators:

- `=~`
- `!~`

are compiled into `ExecutableRegexOp` during plan build. The `Pattern` is created once at compile time, not per evaluation.

### Audit sizing

`ExecutionPlanBuilder` computes `maxAuditEvents`, and `ExpressionRuntimeSupport` uses that to pre-size the single-use `AuditCollector`.

---

## 11. Audit Trail

`computeWithAudit(...)` returns:

- value
- `ExpressionAuditTrace`

### Event types

`AuditEvent` is a sealed interface with:

- `VariableRead`
- `FunctionCall`
- `AssignmentEvent`

### What gets recorded

- every identifier read
- every dynamic instant read
- every function invocation
- every assignment target write

Notes:

- dynamic instants are marked as `systemProvided = true`
- ordinary identifiers are marked as `systemProvided = false`
- destructuring assignments emit one `AssignmentEvent` per target
- folded function calls still emit `FunctionCall`

### Convenience views

`ExpressionAuditTrace` exposes:

- `events()`
- `evaluationTime()`
- `variableSnapshot()`
- `functionCalls()`

`variableSnapshot()` is computed in event order and keeps the last seen value for each variable name.

---

## 12. Grammar Notes That Matter In Practice

The grammar file is:

- `expression-evaluator/src/main/antlr4/com/runestone/expeval/internal/grammar/ExpressionEvaluator.g4`

### Entry points

- `mathStart`
- `logicalStart`
- `assignmentStart`

Math and logical inputs may contain leading assignment statements before the final expression.

### Assignments

Simple assignment:

```text
x = value;
```

Destructuring assignment:

```text
[a, b, c] = [1, 2, 3];
```

### Function calls

Function arguments accept comma or semicolon as separators:

```text
max(1, 2)
if(cond; thenValue; elseValue)
```

### Object navigation

- property access: `obj.prop`
- safe property access: `obj?.prop`
- method call: `obj.method(arg)`
- safe method call: `obj?.method(arg)`

### Null coalescing

Reference entities can use `??`:

```text
<text>user.name ?? "anonymous"
```

### Type-hinted references

Reference expressions may be prefixed with:

- `<bool>`
- `<number>`
- `<text>`
- `<date>`
- `<time>`
- `<datetime>`
- `<vector>`

Examples:

- `<number>tax`
- `<date>pedido.data`
- `<vector>items ?? [1, 2, 3]`

### Cast expressions

The grammar also supports explicit casts:

```text
<number>(value)
<text>(obj.codigo)
```

### Literals

| Token | Example | Runtime type |
|---|---|---|
| `NUMBER` | `10`, `3.14`, `0xFF` | `BigDecimal` |
| `STRING` | `"abc"` | `String` |
| `DATE` | `2024-12-31` | `LocalDate` |
| `TIME` | `12:30` | `LocalTime` |
| `DATETIME` | `2024-12-31T12:30` | `LocalDateTime` |
| `NOW_DATE` | `currDate` | `LocalDate` |
| `NOW_TIME` | `currTime` | `LocalTime` |
| `NOW_DATETIME` | `currDateTime` | `LocalDateTime` |
| `NULL` | `null` | `null` |

`DATETIME` may optionally be followed by `TIME_OFFSET`, but semantic/runtime handling currently normalizes datetime values as `LocalDateTime`.

### Collection operators

- membership: `x in [1, 2, 3]`
- negative membership: `x not in [1, 2, 3]`
- range test: `x between 1 and 10`
- negative range test: `x not between 1 and 10`

### Regex operators

- `text =~ "pattern"`
- `text !~ "pattern"`

### Vector literals

Vectors require at least one element:

```text
[1, 2, 3]
```

The empty vector literal `[]` is not valid grammar.

---

## 13. Cache And Lifecycle

### `CacheConfig`

```java
public record CacheConfig(long maximumSize, Duration expireAfterWrite)
```

Defaults come from JVM properties:

- `expeval.cache.maximumSize` default `1024`
- `expeval.cache.ttlSeconds` default `0` meaning no TTL

### Singleton compiler lifecycle

`ExpressionRuntimeSupport` manages a lazily-initialized JVM-wide `ExpressionCompiler`.

Available controls:

- `configure(CacheConfig)`
  Applies only if the singleton has not been initialized yet.
- `reconfigure(CacheConfig)`
  Replaces the singleton immediately.
- `invalidateCache()`
  Clears cached entries without replacing the compiler.

### DI use case

The public APIs also expose overloads that accept an explicit `ExpressionCompiler`, which is the right choice when the compiler lifecycle should be controlled by DI or tests.

---

## 14. Useful Test Anchors

When changing runtime behavior, these tests are the fastest high-signal anchors:

- `api/ObjectNavigationTest`
- `api/ObjectNavigationCircularReferenceTest`
- `api/AuditTrailExpressionTest`
- `api/ExpressionValidationTest`
- `api/DynamicLiteralExpressionTest`
- `internal/runtime/RuntimeCoercionServiceTest`
- `internal/runtime/ConstantFoldingPlanTest`
- `internal/runtime/ConditionalOptimizationPlanTest`
- `internal/runtime/ObjectNavigationPlanTest`
- `internal/runtime/ExecutionScopeTest`
- `internal/runtime/NullMembershipTest`
- `internal/semantic/SemanticResolverTest`

---

## 15. Maintenance Rule

If you change any of the following, update this document in the same change:

- grammar or accepted syntax
- type-hint behavior
- symbol resolution rules
- overload disambiguation
- coercion semantics
- constant folding
- audit trail behavior
- compiler cache lifecycle
