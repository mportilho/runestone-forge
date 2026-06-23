# Getting Started

The expression-evaluator compiles a text expression into a reusable execution plan and evaluates it against bindings you supply at runtime. Compilation is the slow part — do it once, then call `compute()` as often as you need.

Three expression types exist, each returning a different result:

| Type | Returns | Example |
|---|---|---|
| `MathExpression` | `BigDecimal` | `"a + b * 2"` |
| `LogicalExpression` | `Boolean` | `"score >= 700 and status = 'active'"` |
| `AssignmentExpression` | `Map<String, Object>` | `"tax = price * 0.1; total = price + tax"` |

## Minimum Working Example

```java
ExpressionEnvironment env = ExpressionEnvironment.builder()
    .addAllFunctions()
    .build();

MathExpression expr = MathExpression.compile("a + b * 2", env);
BigDecimal result = expr.compute(Map.of("a", 10, "b", 5)); // → 20
```

That's the full loop: build an environment, compile, evaluate. The `Map` keys are variable names; values are coerced to the right type automatically.

## Adding to Your Project

```xml
<dependency>
    <groupId>io.github.runestone-forge</groupId>
    <artifactId>expression-evaluator</artifactId>
    <version>1.1.0.1-SNAPSHOT</version>
</dependency>
```

**Transitive dependencies you need to know about:**

| Library | Version | Why |
|---|---|---|
| Caffeine | 3.2.0 | Expression cache |
| ANTLR4 runtime | 4.13.1 | Grammar parsing |
| big-math | 2.3.2 | Transcendental math functions |

**Requirements:** Java 21 or later. The library uses sealed interfaces, records, and `MethodHandle` — none of those backport to earlier versions. No Spring dependency; the library works in any Java application.

## The Three Expression Types

### MathExpression

Evaluates to a `BigDecimal`. Supports arithmetic, comparisons that produce a number, and any expression whose result is numeric.

```java
MathExpression price = MathExpression.compile(
    "if(qty >= 100; base * 0.85; if(qty >= 50; base * 0.90; base))",
    env
);
BigDecimal result = price.compute(Map.of("qty", 75, "base", new BigDecimal("100")));
// → 90.00
```

### LogicalExpression

Evaluates to a `Boolean`. Useful for eligibility rules, validation, and conditional checks.

```java
LogicalExpression eligible = LogicalExpression.compile(
    "age >= 18 and status = 'active' and score between 700 and 850",
    env
);
boolean ok = eligible.compute(Map.of("age", 25, "status", "active", "score", 750));
// → true
```

### AssignmentExpression

Evaluates a sequence of assignments and returns all assigned variables as a `Map<String, Object>`. Variables defined earlier in the sequence are available to later assignments.

```java
AssignmentExpression calc = AssignmentExpression.compile("""
    base = price * qty;
    tax = base * taxRate;
    discount = if(qty > 10; base * 0.05; 0);
    total = base + tax - discount
    """, env);

Map<String, Object> results = calc.compute(
    Map.of("price", 50, "qty", 15, "taxRate", new BigDecimal("0.12"))
);
// → {base=750, tax=90.00, discount=37.50, total=802.50}
```

## Reusing Compiled Expressions

Compiled expressions are immutable and thread-safe. The intended pattern is to compile once during application startup and reuse the same object across requests.

```java
// At startup — do this once
private static final MathExpression PRICING = MathExpression.compile("...", env);

// At request time — call as many times as needed
BigDecimal price = PRICING.compute(requestBindings);
```

Compiling the same expression with the same environment twice returns a cached result from Caffeine. There is no cost to calling `compile()` with the same arguments repeatedly, but caching the compiled reference yourself avoids the lookup overhead.

## Next Steps

- [Environment Configuration](02-environment-configuration.md) — register custom functions and symbols, configure cache and precision
- [Expression Syntax](03-expression-syntax.md) — full operator and literal reference
- [Built-in Functions](04-built-in-functions.md) — catalog of available math, string, date/time, and financial functions
