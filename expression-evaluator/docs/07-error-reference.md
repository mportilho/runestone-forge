# Error Reference

Errors fall into two categories: compilation errors (caught before any evaluation runs) and runtime errors (thrown during `compute()`).

## Compilation Errors

These surface as `SemanticResolutionException` or `ParsingException` from `compile()` or `validate()`. Each has an `IssueCode` that identifies the exact problem.

### Syntax Errors

| Code | Description | Example Expression |
|---|---|---|
| `SYNTAX_ERROR` | The token stream does not match the grammar | `a + + b` |

### Semantic Errors

| Code | Description | How to Fix |
|---|---|---|
| `RESULT_TYPE_MISMATCH` | The expression returns a type incompatible with the requested result | Compiling a string expression as `MathExpression` |
| `TYPE_MISMATCH` | Operand types are incompatible with the operator | Applying arithmetic to a date |
| `INVALID_CURRENT_ELEMENT` | `@` used outside a filter or lambda context | `@` in a plain expression |
| `INVALID_MEMBER_ACCESS` | Property access on a non-object type | Accessing `.name` on a `BigDecimal` |
| `INVALID_MAP_PROPERTY_ACCESS` | Key access pattern not valid for the map type | |
| `INVALID_METHOD_ARITY` | Wrong number of arguments for an instance method | |
| `INVALID_FUNCTION_ARITY` | Wrong number of arguments for a function | `sqrt(9, 2)` |
| `UNKNOWN_PROPERTY` | Property not found on the type (requires type hint) | `customer.nme` when type hint is registered |
| `UNKNOWN_METHOD` | Method not found on the type | |
| `UNKNOWN_FUNCTION` | Function name not in the catalog | `foo(x)` when `foo` is not registered |
| `UNKNOWN_COLLECTION_FUNCTION` | Aggregation or deep-scan function not recognized | `list..foo()` |
| `AMBIGUOUS_METHOD` | Multiple instance methods match the call | Register fewer overloads, or rename |
| `AMBIGUOUS_FUNCTION` | Multiple registered functions match the call | |
| `INCOMPATIBLE_COMPARISON` | Comparing types that cannot be ordered against each other | |
| `INCOMPATIBLE_IN_OPERANDS` | Right-hand operand of `in` is not a compatible vector | |
| `INCOMPATIBLE_METHOD_ARGUMENTS` | Argument types do not match the method signature | |
| `INCOMPATIBLE_FUNCTION_ARGUMENTS` | Argument types do not match the function signature | |
| `INCOMPATIBLE_COLLECTION_FUNCTION_ARGUMENTS` | Lambda passed to an aggregation that does not accept one | `list..avg(@ -> x)` |

### Error Message Format

Compilation errors include a visual pointer to the position in the source:

```
age >= 18 and stauts = 'active'
              ^^^^^^^
UNKNOWN_SYMBOL at 1:14 — symbol 'stauts' is not registered
```

`Issue.formatMessage()` on each entry in `ValidationResult.issues()` produces this output.

## Runtime Errors

Runtime errors surface as `ExpressionEvaluationException` during `compute()` or `computeWithAudit()`. They carry inline error descriptions rather than a typed enum.

| Description | Cause | How to Fix |
|---|---|---|
| `NULL_IN_CHAIN` | Property accessed on a null object without `?.` | Use `?.` for null-safe navigation |
| `INDEX_OUT_OF_BOUNDS` | Array or list index out of range | Check collection size before accessing by index |
| `TYPE_MISMATCH` | Value type incompatible with the operation at runtime | Validate input types before passing them as bindings |
| `ZERO_DIVISION` | Division by zero | Guard divisors with `if(d != 0; a / d; 0)` |

### `FunctionInvocationException`

When a custom function throws an exception internally, the engine wraps it in `FunctionInvocationException`. This exception exposes:

- `functionName()` — the name of the function that threw
- `getCause()` — the original exception

There is no `IssueCode` on this exception — it is a runtime wrapper, not a semantic error.

```java
try {
    BigDecimal result = expr.compute(bindings);
} catch (FunctionInvocationException e) {
    log.error("Function {} failed: {}", e.functionName(), e.getCause().getMessage());
} catch (ExpressionEvaluationException e) {
    log.error("Evaluation error: {}", e.getMessage());
}
```

## Quick Diagnosis Guide

| Symptom | Likely Cause |
|---|---|
| `UNKNOWN_FUNCTION` at compile time | Provider not registered, or method is not `public` / not `static` |
| `INVALID_FUNCTION_ARITY` | Wrong argument count; check if the function has overloads |
| `AMBIGUOUS_FUNCTION` | Two registered methods have the same name and argument count; rename one |
| `NULL_IN_CHAIN` at runtime | Use `?.` throughout the navigation chain |
| `FunctionInvocationException` | Custom function threw; check the `getCause()` |
| Result is off but no error | `MathContext` precision too low; check `withMathContext()` |
| Cache not invalidating | The `ExpressionEnvironmentId` did not change; verify configuration actually changed |
