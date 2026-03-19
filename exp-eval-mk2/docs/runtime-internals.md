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

Compiled expressions are cached in `ExpressionCompiler` by `(source, environmentId, resultType)` via Caffeine (max 1 024 entries).

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
| `addFastExcelFunctions()` | `DoubleExcelFinancialFunctions` (double overloads) |
| `addDateTimeFunctions()` | `DateTimeFunctions` |
| `addAllFunctions()` | All of the above except `DoubleExcelFinancialFunctions` |

`registerStaticProvider(Class<?>)` discovers all public static methods via `getMethods()`.

---

## 10. Public API usage pattern

```java
// Math result
ExpressionEnvironment env = ExpressionEnvironment.builder()
        .addTrigonometryFunctions()
        .build();

BigDecimal result = MathExpression.compile("sin(x)", env)
        .setValue("x", new BigDecimal("1.5707963267948966"))
        .compute();

// Logical result
boolean ok = LogicalExpression.compile("atan2(1, 0) > 1", env).compute();

// Date function — pass LocalDate via setValue
ExpressionEnvironment dateEnv = ExpressionEnvironment.builder()
        .addDateTimeFunctions()
        .build();

BigDecimal days = MathExpression.compile("daysBetween(d1, d2)", dateEnv)
        .setValue("d1", LocalDate.of(2024, 1, 1))
        .setValue("d2", LocalDate.of(2024, 12, 31))
        .compute(); // returns BigDecimal 365
```
