# Known Limitations

## Empty Vectors Are Invalid

`[]` is rejected by the parser. Vectors require at least one element:

```
// Invalid — parser error
[]

// Valid
[1]
["placeholder"]
```

If your use case requires an empty collection as a default, pass it as a binding from Java rather than expressing it as a literal.

## Variadic Parameters in Custom Providers

Java variadics (`BigDecimal...`) do not work in custom function providers. The `MethodHandle` produced for a variadic method has different invocation semantics and causes a `ClassCastException` at runtime when the engine calls it.

Use explicit array parameters instead:

```java
// Does NOT work
public static BigDecimal total(BigDecimal... values) { ... }

// Works
public static BigDecimal total(BigDecimal[] values) { ... }
```

## No Lambda in avg, min, max, count Deep-Scan

Only `sum` and `prod` accept a lambda for element transformation in deep scan:

```
items..sum(@ -> price * qty)    // OK
items..prod(@ -> multiplier)    // OK

items..avg(@ -> price)          // compile error
items..min(@ -> value)          // compile error
items..max(@ -> value)          // compile error
items..count(@ -> expr)         // compile error
```

Use `..ds(prop)` to project first, then aggregate:

```
items..ds(price)..avg()   // average of all prices
```

## No `..filter()` Deep-Scan

There is no `..filter()` aggregation. Use the subscript predicate instead:

```
// Does NOT exist
items..filter(@ -> price > 10)

// Correct approach
items[?(@ > 10)]          // on a flat collection
items..ds(price)[?(@ > 10)] // after projecting a property
```

## `currDate`, `currTime`, `currDateTime` Are Literals

These keywords are date/time literals, not variable references. The `<type>` type-hint marker applies to variable references and function calls, not to these literals.

```
// Invalid
<date>currDate

// Valid
d = currDate;      // use directly in assignments
if(currDate > startDate; ...)   // use directly in expressions
```

## `||` Is String-Only

The concatenation operator `||` only accepts string operands. Numbers and booleans must be placed in the string type-hint context explicitly:

```
// Fails at parse time
1 || " item"

// Works
<text>(qty) || " items"
```

## Recursion

Custom functions cannot call themselves recursively through an expression. The engine has no mechanism to express recursive function calls in the expression language.

## Numeric Precision and Float/Double Inputs

The engine uses `BigDecimal` for all arithmetic. Java `float` and `double` bindings are converted to `BigDecimal` using their string representation, which can introduce apparent precision issues if the original `double` is itself imprecise (`0.1 + 0.2` in floating-point arithmetic).

To avoid this, pass `BigDecimal` bindings directly:

```java
// May produce unexpected results due to float imprecision
expr.compute(Map.of("x", 0.1f));

// Predictable
expr.compute(Map.of("x", new BigDecimal("0.1")));
```

## Global Cache

The singleton `ExpressionCompiler` maintains one Caffeine cache shared across all environments in the JVM. Cache size and TTL are set at startup via system properties and cannot be changed at runtime. If you need independent caches, create a dedicated `ExpressionCompiler` instance for each scope and pass it explicitly to `compile()`. See [Advanced Topics](08-advanced-topics.md#using-a-custom-compiler).

## Navigation Depth

There is no configurable depth limit for object navigation. The only protection is circular reference detection — if the engine detects a cycle, it throws `ExpressionEvaluationException`. Very deep object graphs will work, but stack overflow is theoretically possible in extreme cases.
