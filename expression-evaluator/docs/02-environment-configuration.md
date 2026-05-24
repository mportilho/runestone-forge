# Environment Configuration

`ExpressionEnvironment` is the configuration object that the compiler consults during both compilation and evaluation. It holds the function catalog, external symbols, type hints, math precision settings, and cache configuration. Once built, it is immutable and thread-safe.

## Building an Environment

```java
ExpressionEnvironment env = ExpressionEnvironment.builder()
    .addAllFunctions()
    .withMathContext(MathContext.DECIMAL64)
    .build();
```

The builder collects settings; `build()` finalizes them and produces a stable `ExpressionEnvironmentId`. That ID is a SHA-256 hash of the configuration — two environments with identical settings share the same ID and therefore the same cache entries.

> [!NOTE]
> Instance-based function providers are an exception: they use `System.identityHashCode`, so two separate provider instances always produce different IDs even if they are the same class with the same methods.

## Registering Functions

### Built-in Providers

| Method | What it registers |
|---|---|
| `addAllFunctions()` | All built-in providers at once |
| `addMathFunctions()` | Arithmetic, statistics, rounding — `mean`, `sum`, `sqrt`, `round`, etc. |
| `addStringFunctions()` | String manipulation — `concat`, `toUpper`, `trim`, `split`, `replace`, etc. |
| `addDateTimeFunctions()` | Date/time arithmetic — `daysBetween`, `addDay`, `setMonth`, etc. |
| `addTrigonometryFunctions()` | Trig functions plus constants `pi`, `e`, `tau` |
| `addComparableFunctions()` | `max(T[])` and `min(T[])` |
| `addExcelFunctions()` | Financial functions — `pmt`, `pv`, `fv`, `npv`, `nper`, `ipmt`, `ppmt` |

### Custom Providers

Register your own functions through static or instance providers:

```java
// Static methods — all public static methods on the class are discovered
env.builder().registerStaticProvider(MyMathUtils.class, true); // true = foldable

// Instance methods — all public non-static methods are discovered
MyDomainService svc = new MyDomainService();
env.builder().registerInstanceProvider(svc, false); // false = not foldable
```

**Method discovery rules:**
- Static providers: public static methods only, excluding `Object` methods, synthetic, and bridge.
- Instance providers: public non-static methods only, same exclusions.
- Overloads by arity are all registered as separate entries.
- The `MathContext` type is special: if the **first parameter** is `MathContext`, the runtime injects it automatically. It does not count toward the function's arity as seen in expressions. Trig and log functions receive `transcendentalMathContext`; others receive `mathContext`.
- Supported parameter and return types: `BigDecimal`, `String`, `Boolean`, `LocalDate`, `LocalTime`, `LocalDateTime`, arrays and `List<?>` of those types, `Integer`, `Double`. Primitive types (`int`, `double`, `boolean`) are also supported — the built-in providers themselves use them.

**The `foldable` flag:** a foldable function whose arguments are all compile-time constants gets evaluated once during compilation. Subsequent evaluations skip the call entirely. Only mark functions as foldable if they are pure — same inputs always produce the same output.

> [!WARNING]
> Variadics (`BigDecimal...`) do not work in providers. The `MethodHandle` produced by `unreflect()` for a variadic method has different invocation semantics and causes a `ClassCastException` at runtime. Use explicit array parameters (`BigDecimal[]`) instead.

## Registering External Symbols

External symbols are named values the compiler knows about at compile time. Two modes:

```java
ExpressionEnvironment env = ExpressionEnvironment.builder()
    .registerExternalSymbol("TAX_RATE", new BigDecimal("0.12"), false)  // constant
    .registerExternalSymbol("userScore", 0, true)                        // overridable
    .build();
```

- `overridable=false`: treated as a constant. The compiler folds it into the execution plan — no lookup at runtime, zero overhead. Supplying this symbol in `compute(Map)` is invalid and raises an error because the compiled plan already captured the registered value. When using `computeWithAudit()`, a pre-stored `VariableRead` event is emitted for the folded value.
- `overridable=true`: the default value is used if `compute()` does not supply it. If `compute()` supplies it, the binding wins.

The type of the symbol is inferred from the default value. Pass a `BigDecimal` for a numeric symbol, a `String` for text, a `Boolean` for logical, and so on.

## Registering Type Hints

Type hints tell the compiler what fields and methods an object exposes, enabling validation at compile time instead of at runtime.

```java
ExpressionEnvironment env = ExpressionEnvironment.builder()
    .addAllFunctions()
    .registerTypeHint(Customer.class)
    .registerTypeHint(Order.class)
    .build();
```

The compiler discovers: record components, JavaBean getters, public fields, and public instance methods. Without a type hint, navigation still works through reflection at runtime, but a typo in a property name will only fail when the expression is evaluated — not when it is compiled.

When you have type hints registered, `validate()` and compilation produce clearer error messages pointing to the exact property or method that does not exist.

## Configuring Math Precision

The default is `MathContext.DECIMAL128` — 34 significant digits. That is enough for most use cases. Reduce precision if you need faster arithmetic or are working with inputs that don't need full decimal precision.

```java
ExpressionEnvironment env = ExpressionEnvironment.builder()
    .withMathContext(MathContext.DECIMAL64)           // general arithmetic
    .withTranscendentalMathContext(MathContext.DECIMAL32) // sin, cos, ln, etc.
    .build();
```

Transcendental functions (trig and logarithms) use a separate context because they are typically more expensive and the required precision is often lower than general arithmetic. Setting them separately avoids imposing DECIMAL128 overhead on every `sin()` call.

## Configuring the Cache

The expression compiler is a JVM-wide singleton by default. Cache settings are read from system properties:

| Property | Default | Effect |
|---|---|---|
| `expeval.cache.maximumSize` | 1024 | Max cached expressions per compiler instance |
| `expeval.cache.ttlSeconds` | none | If set, entries expire after this many seconds |

Set them at JVM startup:

```
-Dexpeval.cache.maximumSize=2048 -Dexpeval.cache.ttlSeconds=3600
```

To use a separate engine with its own cache — useful when different parts of the application need different cache policies — create an `ExpressionEngine` instance explicitly:

```java
ExpressionEngine engine = new ExpressionEngine(CacheConfig.defaults());
MathExpression expr = engine.compileMath("a + b", env);
```

> [!NOTE]
> Two environments with identical static configuration (same providers, symbols, `MathContext`) share the same `ExpressionEnvironmentId` and therefore the same cache entries. If you have many environments with slightly different configurations, they each consume separate cache space.

## Complete Builder Reference

| Method | Description |
|---|---|
| `addAllFunctions()` | Registers all built-in function providers |
| `addMathFunctions()` | Math and statistics functions |
| `addStringFunctions()` | String manipulation functions |
| `addDateTimeFunctions()` | Date/time arithmetic functions |
| `addTrigonometryFunctions()` | Trig functions and `pi`, `e`, `tau` constants |
| `addComparableFunctions()` | `max` and `min` |
| `addExcelFunctions()` | Financial functions |
| `registerStaticProvider(Class, foldable)` | Register static methods as functions |
| `registerInstanceProvider(Object, foldable)` | Register instance methods as functions |
| `registerExternalSymbol(name, default, overridable)` | Register a named symbol |
| `registerTypeHint(Class)` | Enable typed navigation for a class |
| `withMathContext(MathContext)` | Set precision for general arithmetic |
| `withTranscendentalMathContext(MathContext)` | Set precision for trig and log functions |
| `conversionService(DataConversionService)` | Customize type coercion for domain types |
