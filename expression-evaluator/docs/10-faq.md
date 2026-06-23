# FAQ

## How do I register my own function?

Create a class with `public static` methods (or use an instance), then register it as a provider:

```java
public class MyFunctions {
    public static BigDecimal discount(BigDecimal price, BigDecimal rate) {
        return price.multiply(BigDecimal.ONE.subtract(rate));
    }
}

ExpressionEnvironment env = ExpressionEnvironment.builder()
    .registerStaticProvider(MyFunctions.class, true) // true = foldable if args are constants
    .build();

MathExpression expr = MathExpression.compile("discount(price, 0.1)", env);
```

See [Environment Configuration](02-environment-configuration.md#custom-providers) for discovery rules and parameter type constraints.

## Can I use the engine without Spring?

Yes. The library has no Spring dependency. It works in any Java 21+ application. `ExpressionEnvironment.builder()` is a plain builder — no Spring context needed.

## How do I control the expression cache size?

Set system properties before the JVM starts:

```
-Dexpeval.cache.maximumSize=2048
-Dexpeval.cache.ttlSeconds=3600
```

For isolated cache control per module, create a dedicated `ExpressionEngine`:

```java
ExpressionEngine engine = new ExpressionEngine(new CacheConfig(256, Duration.ofMinutes(30)));
MathExpression expr = engine.compileMath("...", env);
```

## Is it thread-safe to share one `MathExpression` across threads?

Yes. Compiled expressions are immutable and thread-safe. Each `compute()` call creates a fresh execution scope — no shared mutable state between calls. The intended pattern is to compile once and reuse the same object for the lifetime of the application.

## Can I use custom domain objects as bindings?

Yes. Pass them in the `Map` to `compute()`. To get compile-time validation of property access (and better error messages), register a type hint:

```java
ExpressionEnvironment env = ExpressionEnvironment.builder()
    .addAllFunctions()
    .registerTypeHint(Customer.class)
    .build();

MathExpression expr = MathExpression.compile("customer.score * weight", env);
expr.compute(Map.of("customer", customerObj, "weight", 1.5));
```

Without the type hint, navigation still works at runtime via reflection. With it, property names are validated at compile time.

## What is the difference between `MathExpression` and `AssignmentExpression`?

`MathExpression` evaluates a single expression and returns one `BigDecimal`. `AssignmentExpression` evaluates a sequence of named assignments and returns a `Map<String, Object>` with all assigned variables.

Use `MathExpression` for a single computed value. Use `AssignmentExpression` when you need multiple derived values that may depend on each other:

```java
// MathExpression: one result
MathExpression total = MathExpression.compile("base + base * taxRate", env);

// AssignmentExpression: multiple results, with intermediate variables
AssignmentExpression breakdown = AssignmentExpression.compile("""
    base = price * qty;
    tax = base * taxRate;
    total = base + tax
    """, env);
Map<String, Object> results = breakdown.compute(bindings);
// → {base=..., tax=..., total=...}
```

## How do I debug an expression that returns the wrong result?

Use `computeWithAudit()` to see every variable read, function call, and assignment:

```java
AuditResult<BigDecimal> result = expr.computeWithAudit(bindings);
System.out.println("Result: " + result.value());
System.out.println("Variables: " + result.trace().variableSnapshot());
result.trace().functionCalls().forEach(call ->
    System.out.println(call.functionName() + "(" + call.arguments() + ") → " + call.result())
);
```

Common causes: a binding value is a different type than expected, a variable name is misspelled (and falling back to an external symbol default), or `MathContext` precision is truncating intermediate results.

## Does the engine support expressions inside function arguments?

Yes. Function arguments are full expressions:

```
pmt(annualRate / 12; years * 12; principal)
mean(prices[?(@ > threshold)])
if(score > 90; bonus * 1.2; bonus)
```

Nesting is unrestricted — expressions can be composed arbitrarily deep.

## What happens if a binding key does not match any variable in the expression?

Extra bindings are ignored. The engine only reads the variables the expression references. If a required variable is missing and has no registered external symbol default, evaluation throws `ExpressionEvaluationException`.

Use `validate()` to get the exact set of variable names an expression needs before evaluating it:

```java
ValidationResult v = expr.validate(expressionSource);
Set<String> required = v.userVariables(); // exactly what compute() will look for
```
