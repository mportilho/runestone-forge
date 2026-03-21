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
  → MathEvaluator / LogicalEvaluator (ExecutionPlan + ExecutionScope → RuntimeValue)
```

Compiled expressions are cached in `ExpressionCompiler` by `(source, environmentId, resultType)` via Caffeine.
Cache size and TTL are configurable — see section 10.

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
| `String` | `ScalarType.STRING` |
| `LocalDate` | `ScalarType.DATE` |
| `LocalTime` | `ScalarType.TIME` |
| `LocalDateTime` | `ScalarType.DATETIME` |
| Any array or `Collection` subtype | `VectorType.INSTANCE` |
| `Temporal`, `Object`, unrecognised | `UnknownType.INSTANCE` |

> **Key implication**: `DateTimeFunctions` parameters are typed `Temporal` → `UnknownType.INSTANCE`, so semantic type-checking is always skipped for those arguments.

---

## 3. RuntimeValue variants

| Subtype | `raw()` returns | Created from |
|---|---|---|
| `NumberValue(BigDecimal)` | `BigDecimal` | Numbers |
| `BooleanValue(boolean)` | `Boolean` | booleans |
| `StringValue(String)` | `String` | strings |
| `DateValue(LocalDate)` | `LocalDate` | LocalDate |
| `TimeValue(LocalTime)` | `LocalTime` | LocalTime |
| `DateTimeValue(LocalDateTime)` | `LocalDateTime` | LocalDateTime |
| `VectorValue(List<RuntimeValue>)` | `List<RuntimeValue>` | arrays / iterables |
| `NullValue.INSTANCE` | `null` | null inputs |

> **Critical**: `VectorValue.raw()` returns `List<RuntimeValue>`, **not** the original array.
> This is the source of the array-coercion limitation described in section 5.

---

## 4. RuntimeCoercionService.coerce() — resolution order

Called for every function argument before invocation (`AbstractRuntimeEvaluator.evaluateFunctionCall`).

```
coerce(RuntimeValue value, Class<?> targetType):
  1. targetType == RuntimeValue.class          → return value as-is
  2. value == NullValue                        → return null
  3. targetType.isInstance(value.raw())        → return value.raw() directly   ← Temporal works here
  4. targetType == BigDecimal.class            → asNumber(value)
  5. targetType == Double / double             → asDouble(value)
  6. targetType == Integer / int               → asInt(value)
  7. targetType == Long / long                 → asLong(value)
  8. targetType == Boolean / boolean           → asBoolean(value)
  9. targetType == String                      → asString(value)
 10. targetType == LocalDate                   → asDate(value)
 11. targetType == LocalTime                   → asTime(value)
 12. targetType == LocalDateTime               → asDateTime(value)
 13. List.isAssignableFrom(targetType) && VectorValue → elements().stream().map(raw()).toList()
 14. fallback: conversionService.convert(value.raw(), targetType)
```

Step 3 is why `DateTimeFunctions(Temporal, Temporal)` works: `Temporal.isInstance(LocalDate)` = true.

Step 14 falls to `DefaultDataConversionService`, which has **no converter** for `List<RuntimeValue> → T[]`.

---

## 5. Array-parameter coercion — implemented fix

### Root cause (historical)

When a catalog function takes an array parameter (e.g., `mean(BigDecimal[] p)`), the expression engine:

1. Evaluates the argument as a `VectorValue` wrapping `List<RuntimeValue>`.
2. Calls `coerce(VectorValue, BigDecimal[].class)`.
3. `BigDecimal[].class.isInstance(List<RuntimeValue>)` = **false** → steps 3–10 did not match.
4. Fell to `DefaultDataConversionService.convert(List<RuntimeValue>, BigDecimal[].class)` → no converter → threw `NoDataConverterFoundException`.

### Fix applied — specialized arrays, reference-array fast path, reflective primitive fallback

```java
if (targetType.isArray() && value instanceof RuntimeValue.VectorValue(List<RuntimeValue> elements)) {
    int n = elements.size();
    Class<?> componentType = targetType.getComponentType();

    if (componentType == BigDecimal.class) {
        BigDecimal[] array = new BigDecimal[n];
        for (int i = 0; i < n; i++) {
            array[i] = asNumber(elements.get(i));
        }
        return array;
    }
    if (componentType == double.class) {
        double[] array = new double[n];
        for (int i = 0; i < n; i++) {
            array[i] = asDouble(elements.get(i));
        }
        return array;
    }
    if (componentType == int.class) {
        int[] array = new int[n];
        for (int i = 0; i < n; i++) {
            array[i] = asInt(elements.get(i));
        }
        return array;
    }
    if (componentType == long.class) {
        long[] array = new long[n];
        for (int i = 0; i < n; i++) {
            array[i] = asLong(elements.get(i));
        }
        return array;
    }
    if (!componentType.isPrimitive()) {
        Object[] array = (Object[]) java.lang.reflect.Array.newInstance(componentType, n);
        for (int i = 0; i < n; i++) {
            array[i] = coerce(elements.get(i), componentType);
        }
        return array;
    }

    Object array = java.lang.reflect.Array.newInstance(componentType, n);
    for (int i = 0; i < n; i++) {
        Array.set(array, i, coerce(elements.get(i), componentType));
    }
    return array;
}
```

`BigDecimal[]` and the primitive fast paths avoid reflective writes entirely. Reference arrays such as `Comparable[]` also avoid `Array.set(...)` via a direct `Object[]` write path; only unsupported primitive arrays still use the reflective fallback.

### Functions now working via the expression API (after fix)

| Catalog class | Now working | Still not working |
|---|---|---|
| `TrigonometryFunctions` | all | — |
| `MathFunctions` | `ln`, `lb`, `log`, `rule3d`, `rule3i`, and now all array-param methods | — |
| `DateTimeFunctions` | all | — |
| `ExcelFinancialFunctions` | `fv`, `pv`, `pmt`, `nper`, and now `npv` | — |
| `ComparableFunctions` | `max(T[])`, `min(T[])` | — |

> **Grammar constraint**: The empty vector literal `[]` is not valid syntax (`vectorOfEntitiesOperation` requires ≥ 1 element), so empty-array edge cases cannot be exercised via the expression API.

---

## 6. Overload disambiguation

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

## 7. RuntimeValueFactory.from() — wrapping raw values

Called when seeding values from `setValue()` and when wrapping function return values.

```
from(Object rawValue, ResolvedType expectedType):
  - null                        → NullValue
  - already a RuntimeValue      → return as-is
  - expectedType == null or UNKNOWN → derive type from rawValue.getClass()
  - effectiveType == VECTOR     → toVector(rawValue)  [handles arrays and Iterables]
  - effectiveType == NUMBER     → NumberValue(convert(rawValue, BigDecimal.class))
  - effectiveType == BOOLEAN    → BooleanValue(convert(rawValue, Boolean.class))
  - etc.
  - fallback switch on rawValue:
      LocalDate    → DateValue
      LocalTime    → TimeValue
      LocalDateTime → DateTimeValue
      Number       → NumberValue(convert(..., BigDecimal.class))
      CharSequence → StringValue
      Iterable/array → toVector(rawValue)
```

`setValue("name", LocalDate.of(...))` creates a `DateValue` via the fallback switch (step: `LocalDate → DateValue`).

### Return value wrapping for array-returning functions

`distribute` and `spread` return `BigDecimal[]`. `RuntimeValueFactory.from(BigDecimal[], NUMBER_VECTOR)` → `toVector()` converts each element to `NumberValue` → `VectorValue(List<NumberValue>)`. This is correct for assignment contexts.

### Long return type (DateTimeFunctions)

All `DateTimeFunctions` return `Long`. `ResolvedTypes.fromJavaType(Long.class)` = `ScalarType.NUMBER`. So `RuntimeValueFactory.from(365L, NUMBER)` → `NumberValue(BigDecimal.valueOf(365))`. `MathEvaluator.compute()` returns a `BigDecimal`. ✓

---

## 8. Expression grammar — key points for test authoring

### Confirmed: date/datetime literals work as function-call arguments

The `allEntityTypes` grammar rule (used for function call arguments) includes `dateEntity` and `dateTimeEntity`.
Because ANTLR's lexer is deterministic and greedy, `2024-01-01` is tokenized as a single `DATE` token
(not `NUMBER MINUS NUMBER MINUS NUMBER`), and `2024-01-01T10:00` as a single `DATETIME` token.
As a result, `daysBetween(2024-01-01, 2024-12-31)` and `hoursBetween(2024-01-01T00:00, 2024-01-02T00:00)`
both parse and evaluate correctly.

Verified in `DateTimeFunctionsExpressionTest`.



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

`[1, 2, 3]` is a valid vector literal that compiles as `ExecutableVectorLiteral` → `VectorValue` at runtime. Array-parameter functions consume it through the coercion path described in section 5.

---

## 9. ExpressionEnvironmentBuilder — convenience methods

| Method | Registers |
|---|---|
| `addMathFunctions()` | `MathFunctions` |
| `addTrigonometryFunctions()` | `TrigonometryFunctions` |
| `addComparableFunctions()` | `ComparableFunctions` |
| `addExcelFunctions()` | `ExcelFinancialFunctions` (BigDecimal overloads) |
| `addDateTimeFunctions()` | `DateTimeFunctions` |
| `addAllFunctions()` | All of the above |

`registerStaticProvider(Class<?>)` discovers all public static methods via `getMethods()`.

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

Two compilations share an entry when `(source, environmentId, resultType)` are identical. `ExpressionEnvironmentId` is derived deterministically from the environment's function providers, external symbols, and `MathContext`, so environments built from identical configurations share cache entries automatically.

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
        .setValue("a", 1).setValue("b", 2).setValue("c", 3)
        .compute(); // 7

// Logical — returns boolean
boolean ok = LogicalExpression.compile("x > 10")
        .setValue("x", 15)
        .compute(); // true

// Assignments — returns Map<String, Object>
Map<String, Object> vars = AssignmentExpression.compile("x = a + 1; y = x * 2;")
        .setValue("a", 5)
        .compute(); // {x=6, y=12}
```

### Functions and external symbols

```java
ExpressionEnvironment env = ExpressionEnvironment.builder()
        .addMathFunctions()
        .addTrigonometryFunctions()
        .registerExternalSymbol("rate", BigDecimal.valueOf(0.05), false)
        .build();

BigDecimal result = MathExpression.compile("sin(x) + rate", env)
        .setValue("x", new BigDecimal("1.5707963267948966"))
        .compute();
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
        .setValue("a", 2).setValue("b", 4)
        .computeWithAudit();

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
        .setValue("d1", LocalDate.of(2024, 1, 1))
        .setValue("d2", LocalDate.of(2024, 12, 31))
        .compute(); // 365
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
