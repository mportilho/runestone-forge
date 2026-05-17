# Advanced Topics

## Constant Folding

During compilation, the engine evaluates subexpressions whose inputs are fully known at compile time. The resulting `ExecutionPlan` contains pre-computed literals instead of operations, so those subexpressions cost nothing at evaluation time.

Four sources of foldable inputs:

1. **Numeric and string literals** — `2 * pi` is folded because both `2` and `pi` are constants.
2. **External symbols with `overridable=false`** — registered as compile-time constants.
3. **Internal variables assigned constants** — assignment blocks propagate simple constant assignments to later expressions.
4. **Foldable functions** — functions registered with `foldable=true` whose arguments are all constants.

```java
ExpressionEnvironment env = ExpressionEnvironment.builder()
    .addTrigonometryFunctions()   // pi and trig functions are foldable
    .registerExternalSymbol("TAX_RATE", new BigDecimal("0.12"), false) // constant
    .build();

// At compile time: "2 * pi" and "TAX_RATE" are folded.
// At runtime: only "amount" is read from bindings.
MathExpression tax = MathExpression.compile("amount * TAX_RATE * 2 * pi", env);
```

Folded external symbols and folded internal variables still produce `VariableRead` events in the audit trail — captured once at compile time and pre-stored, so they appear at the start of every `computeWithAudit()` trace. Folded function calls appear in the audit trail as well. See [Validation and Audit](06-validation-and-audit.md#constant-folding-and-the-audit-trail) for details.

Only mark a custom function as foldable if it is pure: no side effects, no dependency on external state, same inputs always produce the same output.

### Navigation Folding

If a property or collection chain starts from a compile-time constant root, the compiler folds the largest deterministic prefix it can prove safe:

```java
ExpressionEnvironment env = ExpressionEnvironment.builder()
    .registerExternalSymbol("PRICES", List.of(5, 15, 25), false)
    .build();

MathExpression expr = MathExpression.compile("PRICES[1:3]..sum()", env);
// The slice and sum are computed during compilation. compute() returns the precomputed result.
```

Foldable navigation steps include typed property access, typed method calls with constant arguments, reflective property access, index access, map-key lookup, slices, wildcards, filters using only constants and `@`, map projections, `..map(@ -> expr)`, and foldable collection functions.

The compiler stops folding at runtime-dependent boundaries: overridable symbols, dynamic date/time literals, deep scan, reflective method calls without type hints, non-foldable collection functions, or predicates/transforms that capture runtime values. If a candidate folded step would turn a runtime navigation failure into a compile-time failure, that step is left for runtime evaluation instead.

## Thread Safety

`ExpressionEnvironment`, `MathExpression`, `LogicalExpression`, and `AssignmentExpression` are all thread-safe after construction. Share them freely across threads.

`ExpressionEnvironmentBuilder` is **not** thread-safe. Build your environment during initialization in a single thread.

The `ExecutionPlan` inside a compiled expression is a read-only structure after compilation. Each call to `compute()` creates a new `ExecutionScope` that holds the per-call state (bindings, dynamic instants, assignment scope). That scope is never shared between concurrent calls.

```java
// Safe: single compiled expression, many concurrent evaluations
private static final MathExpression PRICING = MathExpression.compile("...", env);

// Per-request — each call gets its own scope
CompletableFuture<BigDecimal> f1 = CompletableFuture.supplyAsync(() ->
    PRICING.compute(bindingsForRequest1));
CompletableFuture<BigDecimal> f2 = CompletableFuture.supplyAsync(() ->
    PRICING.compute(bindingsForRequest2));
```

`currDate`, `currTime`, and `currDateTime` are re-evaluated on each `compute()` call. Multiple references to `currDate` within a single expression resolve to the same instant (cached inside the `ExecutionScope` for that call). Across calls, each gets a fresh value.

## Using a Custom Compiler

By default, all expressions share a JVM-wide singleton `ExpressionCompiler` with a shared Caffeine cache. If you need separate cache lifecycle, isolation between tenants, or different TTL policies per module, pass an explicit compiler:

```java
CacheConfig config = CacheConfig.of(512, Duration.ofHours(1));
ExpressionCompiler compiler = new ExpressionCompiler(config);

MathExpression expr = MathExpression.compile("a + b", env, compiler);
```

The compiler is independent of the environment. You can combine any compiler with any environment. When the compiler is garbage-collected, its cache is released.

## Custom Type Coercion

The default conversion service handles the standard types (`BigDecimal`, `String`, `Boolean`, `LocalDate`, `LocalTime`, `LocalDateTime`). To use domain types that the engine does not know about natively, supply a `DataConversionService`:

```java
ExpressionEnvironment env = ExpressionEnvironment.builder()
    .addAllFunctions()
    .conversionService(myConversionService)
    .build();
```

The conversion service is called when the engine needs to coerce a binding value to the type expected by an operation or function argument.

## ExpressionEnvironmentId and Cache Sharing

Two environments built with identical static configuration produce the same `ExpressionEnvironmentId` (a SHA-256 hash over the ordered configuration). They share cache entries in the singleton compiler.

The hash covers: registered static providers (by class), registered external symbols (sorted by name and value), `MathContext`, and `transcendentalMathContext`. Instance-based providers use `System.identityHashCode`, so two separate provider objects always produce different IDs even if they are instances of the same class.

Practical implication: if you build many environments that differ only in instance providers, you accumulate separate cache entries per provider instance. Use static providers when sharing cache is important.

## AssignmentExpression in Depth

`AssignmentExpression` supports sequential computation with intermediate variables:

```java
AssignmentExpression calc = AssignmentExpression.compile("""
    base = price * qty;
    tax = base * taxRate;
    discount = if(qty > 10; base * 0.05; 0);
    total = base + tax - discount;
    """, env);

Map<String, Object> results = calc.compute(bindings);
// → {base=..., tax=..., discount=..., total=...}
```

Destructuring assigns a vector to multiple variables in one step:

```
[subtotal, tax, shipping] = invoice..ds(amount);
total = subtotal + tax + shipping;
```

Variables defined earlier in the block are visible to later statements. The returned map contains all assigned variables, including intermediates. If you only need `total`, ignore the rest — the map is always complete.

## Validation Before Persisting Expressions

User-submitted expressions should be validated before storage. `validate()` returns `userVariables()` — the set of variable names the expression will read from bindings. Store this alongside the expression so the application knows what data to fetch at evaluation time without parsing the expression again.

```java
ValidationResult v = MathExpression.compile("...", env).validate(userInput);
if (v.valid()) {
    persistRule(userInput, v.userVariables());
}
```
