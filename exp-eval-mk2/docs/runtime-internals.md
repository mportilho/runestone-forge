# exp-eval-mk2 — Runtime Internals Reference

Verified findings about the runtime architecture, type system, and coercion behavior of the `exp-eval-mk2` module.
Use this document as the starting point before investigating the codebase from scratch.

---

## 1. Compilation Pipeline

```
source string
  → ExpressionEvaluatorV2ParserFacade (ANTLR, SLL + LL fallback)
  → SemanticAstBuilder             (parse tree → typed AST Nodes)
  → SemanticResolver               (AST → SemanticModel with SymbolRef + ResolvedType)
  → ExecutionPlanBuilder           (SemanticModel → ExecutionPlan of ExecutableNode)
  → MathEvaluator / LogicalEvaluator (ExecutionPlan + ExecutionScope → plain Java Object)
```

Compiled expressions are cached in `ExpressionCompiler` by `(source, environmentId, resultType)` via Caffeine.
Cache size and TTL are configurable — see section 9.

> **Runtime value representation**: The runtime works with plain Java objects (`BigDecimal`, `Boolean`, `String`,
> `LocalDate`, `LocalTime`, `LocalDateTime`, `List<Object>`). There is no `RuntimeValue` wrapper type.

---

## 2. Type System

### ResolvedType hierarchy

| Class | Instances | Maps from Java types |
|---|---|---|
| `ScalarType` | `NUMBER`, `BOOLEAN`, `STRING`, `DATE`, `TIME`, `DATETIME` | Numbers, boolean, String, LocalDate, LocalTime, LocalDateTime |
| `VectorType` | `VectorType.INSTANCE` | Arrays, Collections |
| `UnknownType` | `UnknownType.INSTANCE` | `Temporal`, `Object`, and anything not recognised |

### `ResolvedTypes.fromJavaType(Class<?>)` — key mappings

| Java type | ResolvedType |
|---|---|
| `BigDecimal`, `Integer`, `Long`, etc. (any `Number`) | `ScalarType.NUMBER` |
| `boolean` / `Boolean` | `ScalarType.BOOLEAN` |
| `CharSequence` / `String` | `ScalarType.STRING` |
| `LocalDate` | `ScalarType.DATE` |
| `LocalTime` | `ScalarType.TIME` |
| `LocalDateTime` | `ScalarType.DATETIME` |
| Any array or `Collection` subtype | `VectorType.INSTANCE` |
| `Temporal`, `Object`, unrecognised | `UnknownType.INSTANCE` |

> **Key implication**: `DateTimeFunctions` parameters are typed `Temporal` → `UnknownType.INSTANCE`, so semantic type-checking is always skipped for those arguments.

### `ResolvedTypes.merge(left, right)`

- Both equal → return left.
- Either is `UnknownType` → return the other (known) type.
- Otherwise → return `UnknownType.INSTANCE`.

---

## 3. RuntimeCoercionService.coerce() — resolution order

Called for every function argument before invocation (`AbstractObjectEvaluator`).

```
coerce(Object value, Class<?> targetType):
  1. value == null                             → return null
  2. targetType == Object.class
     OR targetType.isInstance(value)           → return value as-is   ← Temporal works here
  3. targetType == BigDecimal.class            → asNumber(value)
  4. targetType == Double / double             → asDouble(value)
  5. targetType == Integer / int               → asInt(value)
  6. targetType == Long / long                 → asLong(value)
  7. targetType == Boolean / boolean           → asBoolean(value)
  8. targetType == String                      → asString(value)
  9. targetType == LocalDate                   → asDate(value)
 10. targetType == LocalTime                   → asTime(value)
 11. targetType == LocalDateTime               → asDateTime(value)
 12. value instanceof List<?> elements:
       a. List.isAssignableFrom(targetType)    → new ArrayList<>(elements)
       b. !targetType.isArray()                → conversionService.convert(value, targetType)
       c. targetType.isArray()                 → see array coercion (section 4)
 13. fallback: conversionService.convert(value, targetType)
```

Step 2 is why `DateTimeFunctions(Temporal, Temporal)` works: `Temporal.isInstance(LocalDate)` = true.

Step 13 falls to `DefaultDataConversionService`.

---

## 4. Array-parameter coercion

### Root cause (historical)

When a catalog function takes an array parameter (e.g., `mean(BigDecimal[] p)`), the expression engine:

1. Evaluates the argument as a `List<Object>`.
2. Calls `coerce(List<Object>, BigDecimal[].class)`.
3. List step branches to array coercion (step 12c).

### Implementation — specialized fast paths + reflective fallback

```java
// reached when: value instanceof List<?> elements AND targetType.isArray()
int n = elements.size();
Class<?> componentType = targetType.getComponentType();

if (componentType == BigDecimal.class) { ... }  // direct loop, no reflection
if (componentType == double.class)    { ... }  // direct loop
if (componentType == int.class)       { ... }  // direct loop
if (componentType == long.class)      { ... }  // direct loop
if (!componentType.isPrimitive()) {
    Object[] array = (Object[]) Array.newInstance(componentType, n);
    for (int i = 0; i < n; i++) array[i] = coerce(elements.get(i), componentType);
    return array;
}
// remaining primitive types — reflective fallback
Object array = Array.newInstance(componentType, n);
for (int i = 0; i < n; i++) Array.set(array, i, coerce(elements.get(i), componentType));
return array;
```

`BigDecimal[]` and the primitive fast paths avoid reflective writes entirely. Reference arrays such as `Comparable[]` also avoid `Array.set(...)` via a direct `Object[]` write path; only unsupported primitive arrays still use the reflective fallback.

### Functions working via the expression API

| Catalog class | Working | Not working |
|---|---|---|
| `TrigonometryFunctions` | all | — |
| `MathFunctions` | all (incl. array-param methods) | — |
| `LogarithmFunctions` | all | — |
| `DateTimeFunctions` | all | — |
| `ExcelFinancialFunctions` | `fv`, `pv`, `pmt`, `nper`, `npv` | — |
| `ComparableFunctions` | `max(T[])`, `min(T[])` | — |

> **Grammar constraint**: The empty vector literal `[]` is not valid syntax (`vectorEntity` requires ≥ 1 element), so empty-array edge cases cannot be exercised via the expression API.

---

## 5. Overload disambiguation

The `SemanticResolver` resolves overloaded function names via `matchesArguments()`:

- If the **actual** argument type **or** the **expected** parameter type is `UnknownType.INSTANCE`, the check is **skipped** (always passes). This is how `DateTimeFunctions` parameters work.
- Otherwise, `actualType == expectedType` must hold.
- If multiple overloads match → `AMBIGUOUS_FUNCTION` semantic error.
- `FunctionCatalog.findExact(name, arity)` returns empty when there are multiple candidates with the same arity, forcing explicit disambiguation via `findCandidates`.

### ExcelFinancialFunctions overload disambiguation

`fv`, `pmt` each have two 5-argument overloads:
- `fv(BigDecimal, BigDecimal, BigDecimal, BigDecimal, boolean)` — 5th param: `BOOLEAN`
- `fv(BigDecimal, int, BigDecimal, BigDecimal, int)` — 5th param: `NUMBER`

Disambiguation in expressions:
- `fv(0.05, 10, -100, -1000, false)` → 5th arg is BOOLEAN → resolves to the first overload ✓
- `fv(0.05, 10, -100, -1000, 0)` → 5th arg is NUMBER → resolves to the second overload ✓

The `int` parameters in the second overload are coerced at runtime via `DefaultDataConversionService.convertPrimitiveTypes()` → `number.intValue()`.

---

## 6. Constant folding

`ExecutionPlanBuilder` can pre-compute nodes at build time to avoid repeated evaluation at runtime.

### Vector literal folding

`ExecutableVectorLiteral` pre-computes all-constant vectors at build time and stores a `foldedValue: List<Object>`.
`isFolded()` returns true when `foldedValue != null`; the evaluator skips element evaluation in that case.

### Function call folding

`ExecutableFunctionCall` stores `foldedArgs` and `foldedResult` when:
- The `FunctionDescriptor` is marked `foldable = true` (all built-in catalog providers are foldable by default).
- All arguments are constant or themselves pre-computed.

`isFolded()` returns `foldedResult != null`. The evaluator short-circuits directly to `foldedResult`.

---

## 7. ExecutionScope — two-layer architecture

`ExecutionScope` separates external (predefined) symbols from internal (assignment) symbols to keep concurrent evaluation safe.

- **External layer**: shared, read-only `Map<SymbolRef, Object>` built once per evaluation call from the user-supplied `Map<String, Object>` merged with external symbol defaults.
- **Internal layer**: fresh mutable `HashMap` pre-sized to `internalSymbolCount`; used exclusively for assignment targets.

The sentinel `ExecutionScope.UNBOUND` (distinct from `null`) marks missing bindings so that explicit `null` values are propagated correctly.

### Dynamic instant caching

`currDate`, `currTime`, and `currDateTime` are evaluated once per scope and cached in an `EnumMap<DynamicInstant, Object>` on the scope.
Subsequent reads within the same evaluation return the same instant, ensuring consistency.

---

## 8. Expression grammar — key points for test authoring

### Confirmed: date/datetime literals work as function-call arguments

The `allEntityTypes` grammar rule (used for function call arguments) includes `dateEntity` and `dateTimeEntity`.
Because ANTLR's lexer is deterministic and greedy, `2024-01-01` is tokenized as a single `DATE` token
(not `NUMBER MINUS NUMBER MINUS NUMBER`), and `2024-01-01T10:00` as a single `DATETIME` token.
As a result, `daysBetween(2024-01-01, 2024-12-31)` and `hoursBetween(2024-01-01T00:00, 2024-01-02T00:00)`
both parse and evaluate correctly.

### Date and time literals

| Grammar token | Example | Java type |
|---|---|---|
| `DATE` | `2024-12-31` | `LocalDate` |
| `DATETIME` | `2024-12-31T12:30` | `LocalDateTime` |
| `TIME` | `12:30` | `LocalTime` |
| `currDate` | `currDate` | `LocalDate.now()` |
| `currDateTime` | `currDateTime` | `LocalDateTime.now()` |
| `currTime` | `currTime` | `LocalTime.now()` |

### Type-hinted variable references

`<date>varName`, `<datetime>varName`, `<time>varName`, `<number>varName` — explicit type annotation in the expression. Without annotation, the type is inferred from the context or defaults to `UnknownType`.

### Function call arguments

Arguments use the `allEntityTypes` rule, which accepts `mathExpression`, `logicalExpression`, `dateEntity`, `timeEntity`, `dateTimeEntity`, `stringEntity`, or `vectorEntity`. Unary minus (`-100`) parses correctly as a `mathExpression`.

### Vector literal

`[1, 2, 3]` is a valid vector literal that compiles as `ExecutableVectorLiteral` → `List<Object>` at runtime. Array-parameter functions consume it through the coercion path described in section 4.

### Assignment expressions

`IDENTIFIER = value ;` (simple) or `[id1, id2, ...] = vectorExpr ;` (destructuring).
The operator is `=` and each statement is terminated by `;`.

### Conditional expressions

```
if condition then value elsif condition then value else value endif
```

Also available in function-call syntax: `if(cond ; val ; cond ; val ; ... ; else_val)`.
Conditionals are valid in all entity contexts (math, logical, string, date, time, datetime, vector).

Internally, a single `if/then/else/endif` compiles to the optimised `ExecutableSimpleConditional`; multi-branch conditions compile to `ExecutableConditional`.

---

## 9. ExpressionEnvironmentBuilder — convenience methods

| Method | Registers |
|---|---|
| `addMathFunctions()` | `MathFunctions` + `LogarithmFunctions` |
| `addTrigonometryFunctions()` | `TrigonometryFunctions` |
| `addComparableFunctions()` | `ComparableFunctions` |
| `addExcelFunctions()` | `ExcelFinancialFunctions` |
| `addDateTimeFunctions()` | `DateTimeFunctions` |
| `addAllFunctions()` | All of the above |

`registerStaticProvider(Class<?>, boolean foldable)` and `registerInstanceProvider(Object, boolean foldable)` are the low-level registration methods.

`registerStaticProvider(Class<?>)` discovers all public static methods via `getMethods()`.

**MathContext injection**: If the first parameter of a provider method is `MathContext`, it is bound at environment-build time:
- `TrigonometryFunctions` and `LogarithmFunctions` receive `transcendentalMathContext` (default `DECIMAL128`).
- All other providers receive `mathContext` (default `DECIMAL128`).
- The `MathContext` parameter is hidden from the expression call site (arity seen by the catalog is reduced by 1).

> **Default environment**: `ExpressionEnvironmentBuilder.empty()` — and the no-environment overloads on `MathExpression`, `LogicalExpression`, and `AssignmentExpression` all use `empty()`. No functions are registered by default. An expression that calls any function without an explicit environment with that function registered will fail with `UNKNOWN_FUNCTION`.

---

## 10. Cache configuration

`ExpressionCompiler` is configurable and can be used both as a JVM-wide singleton and as an injectable component.

### CacheConfig record

```java
public record CacheConfig(long maximumSize, Duration expireAfterWrite)
```

- `maximumSize` — maximum Caffeine cache entries; must be positive.
- `expireAfterWrite` — TTL per entry; `null` disables expiration.
- `CacheConfig.defaults()` — reads `expeval.cache.maximumSize` (default 1 024) and `expeval.cache.ttlSeconds` (default 0 = no TTL) from system properties.

### Singleton lifecycle via ExpressionRuntimeSupport

```java
// Configure before first compilation (silently ignored if already initialized)
ExpressionRuntimeSupport.configure(new CacheConfig(4_096, Duration.ofHours(1)));

// Replace the singleton at any time (atomically)
ExpressionRuntimeSupport.reconfigure(new CacheConfig(8_192, null));

// Clear cache entries without replacing the compiler
ExpressionRuntimeSupport.invalidateCache();
```

### DI / Spring @Bean pattern

```java
@Bean
public ExpressionCompiler expressionCompiler() {
    return new ExpressionCompiler(new CacheConfig(4_096, Duration.ofHours(1)));
}

// Then pass the injected compiler to the three-argument compile overload:
MathExpression expr = MathExpression.compile("a + b", environment, compiler);
```

The three-argument `compile` overloads exist on `MathExpression`, `LogicalExpression`, and `AssignmentExpression`.

### Cache key

Two compilations share an entry when `(source, environmentId, resultType)` are identical. `ExpressionEnvironmentId` is a 16-character hex string derived from SHA-256 of the sorted list of provider class names, instance identity hash codes, external symbol names/types/overridability, and both `MathContext` settings — so environments built from identical configurations share cache entries automatically.

---

## 11. Audit trail

`computeWithAudit()` is available on all three expression types and returns `AuditResult<T>`.

### AuditResult

```java
public record AuditResult<T>(T value, ExpressionAuditTrace trace)
```

- `T` is `BigDecimal` (math), `Boolean` (logical), or `Map<String, Object>` (assignments).

### ExpressionAuditTrace

```java
public record ExpressionAuditTrace(List<AuditEvent> events, Duration evaluationTime)
```

Convenience views:
- `variableSnapshot()` — `LinkedHashMap<String, Object>` of all variable names to their last-seen value during evaluation (assignments first, then reads; iteration order follows first appearance).
- `functionCalls()` — ordered `List<AuditEvent.FunctionCall>` of all function calls during evaluation.

### AuditEvent — sealed interface with three permits

| Subtype | When emitted | Key fields |
|---|---|---|
| `VariableRead(name, systemProvided, value)` | Each time an identifier is resolved | `systemProvided=true` for `currDate`/`currTime`/`currDateTime` |
| `FunctionCall(functionName, inputArgs, result, callDepth)` | After each function invocation | `callDepth=0` for top-level, ≥1 for nested; `inputArgs()` returns immutable `List<Object>` |
| `AssignmentEvent(targetName, newValue)` | After each variable assignment (one per target for destructuring) | — |

> **Hot-path note**: `FunctionCall` uses `Object[]` internally and wraps to `List.copyOf(...)` only on `inputArgs()`, deferring allocation to read time. `AuditCollector` is pre-sized from the execution plan's estimated event count to avoid `ArrayList` growth during evaluation.

---

## 12. ValidationResult — compile-time metadata

`validate()` on each expression type returns `ValidationResult` without throwing.

```java
public record ValidationResult(
    String source,
    boolean valid,
    List<CompilationIssue> issues,
    Set<String> assignedVariables,
    Set<String> userVariables,
    Set<String> functions)
```

| Field | Content when `valid=true` | Content when `valid=false` |
|---|---|---|
| `issues` | empty | one or more `CompilationIssue` with `code`, `message`, optional `position` |
| `assignedVariables` | names of variables assigned within the expression (internal symbols) | empty |
| `userVariables` | names of variables that must be supplied at evaluation time (external/free symbols) | empty |
| `functions` | deduplicated names of all functions called in the expression | empty |

All three sets are unmodifiable (`Set.copyOf`). `failed()` always produces empty sets.

### Issue codes

| Code | Cause |
|---|---|
| `SYNTAX_ERROR` | ANTLR parse failure |
| `UNKNOWN_FUNCTION` | function name not in `FunctionCatalog` |
| `INVALID_FUNCTION_ARITY` | wrong number of arguments |
| `INCOMPATIBLE_FUNCTION_ARGUMENTS` | argument types do not match any overload |
| `AMBIGUOUS_FUNCTION` | multiple overloads match |
| `INCOMPATIBLE_COMPARISON` | comparison operands have incompatible types |

`CompilationIssue.position()` carries `(line, column, endColumn)` for semantic errors and syntax errors; it is `null` for issues without a source location.

### formatMessage()

Returns a human-readable string. For semantic/syntax errors with a position, formats the source line with a caret pointer:

```
  principal * rate + unknown()
                     ^^^^^^^^^
  UNKNOWN_FUNCTION at 1:19 — unknown function 'unknown'
```

---

## 13. Public API usage pattern

### Three expression types

```java
// Math — returns BigDecimal
BigDecimal result = MathExpression.compile("a + b * c")
        .compute(Map.of("a", 1, "b", 2, "c", 3)); // 7

// Logical — returns boolean
boolean ok = LogicalExpression.compile("x > 10")
        .compute(Map.of("x", 15)); // true

// Assignments — returns Map<String, Object>
Map<String, Object> vars = AssignmentExpression.compile("x = a + 1; y = x * 2;")
        .compute(Map.of("a", 5)); // {x=6, y=12}
```

### Functions and external symbols

```java
ExpressionEnvironment env = ExpressionEnvironment.builder()
        .addMathFunctions()
        .addTrigonometryFunctions()
        .registerExternalSymbol("rate", BigDecimal.valueOf(0.05), false)
        .build();

BigDecimal result = MathExpression.compile("sin(x) + rate", env)
        .compute(Map.of("x", new BigDecimal("1.5707963267948966")));
```

### Validation with metadata

```java
ExpressionEnvironment env = ExpressionEnvironment.builder()
        .addMathFunctions()
        .registerExternalSymbol("principal", BigDecimal.ONE, false)
        .build();

ValidationResult vr = MathExpression.validate("mean([principal, 2]) + rate", env);

vr.valid();             // true
vr.userVariables();     // {"principal", "rate"}  — rate is a free variable
vr.assignedVariables(); // {} — no assignments in this expression
vr.functions();         // {"mean"}
vr.formatMessage();     // "expression is valid: mean([principal, 2]) + rate"
```

### Audit trail

```java
ExpressionEnvironment env = ExpressionEnvironment.builder().addMathFunctions().build();

AuditResult<BigDecimal> audit = MathExpression.compile("mean([a, b])", env)
        .computeWithAudit(Map.of("a", 2, "b", 4));

audit.value();                              // 3
audit.trace().evaluationTime();            // Duration
audit.trace().variableSnapshot();          // {a=2, b=4}
audit.trace().functionCalls().getFirst().functionName(); // "mean"
```

### Date functions

```java
ExpressionEnvironment dateEnv = ExpressionEnvironment.builder()
        .addDateTimeFunctions()
        .build();

BigDecimal days = MathExpression.compile("daysBetween(d1, d2)", dateEnv)
        .compute(Map.of(
                "d1", LocalDate.of(2024, 1, 1),
                "d2", LocalDate.of(2024, 12, 31))); // 365
```

### Spring DI with injected compiler

```java
@Bean
public ExpressionCompiler expressionCompiler() {
    return new ExpressionCompiler(new CacheConfig(4_096, Duration.ofHours(1)));
}

// In a service:
@Autowired ExpressionCompiler compiler;

MathExpression expr = MathExpression.compile("a + b", environment, compiler);
```
