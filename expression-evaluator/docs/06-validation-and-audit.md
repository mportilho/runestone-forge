# Validation and Audit

## Validation

`validate()` parses and semantically checks an expression without evaluating it. Use it to give feedback before persisting a user-submitted expression or before a high-stakes evaluation.

```java
ValidationResult result = expr.validate("age >= 18 and stauts = 'active'");

if (!result.valid()) {
    result.issues().forEach(issue ->
        System.out.println(issue.formatMessage())
    );
}
```

`ValidationResult` exposes:

| Field | Type | Description |
|---|---|---|
| `valid()` | `boolean` | True if no issues were found |
| `issues()` | `List<Issue>` | Syntax and semantic errors |
| `userVariables()` | `Set<String>` | Variables the expression reads from bindings |
| `assignedVariables()` | `Set<String>` | Variables assigned (for `AssignmentExpression`) |
| `functions()` | `Set<String>` | Functions the expression calls |

`Issue.formatMessage()` produces output with a visual pointer to the problem:

```
age >= 18 and stauts = 'active'
              ^^^^^^^
UNKNOWN_SYMBOL at 1:14 — symbol 'stauts' is not registered
```

`userVariables()` is particularly useful when you need to know which bindings an expression will consume before evaluating it — for example, to fetch only the required data.

## Audit Trail

`computeWithAudit()` evaluates the expression and records every variable read, function call, and assignment that occurred during evaluation.

```java
AuditResult<BigDecimal> result = expr.computeWithAudit(Map.of("a", 10, "b", 5));

BigDecimal value = result.value();
ExpressionAuditTrace trace = result.trace();

trace.variableSnapshot();   // Map<String, Object> — all variables read
trace.functionCalls();      // List<FunctionCallEvent> — each call with args and result
trace.assignments();        // List<AssignmentEvent> — each assignment
trace.evaluationTime();     // Duration of the evaluation
```

### Audit Events

Three types of events are recorded in execution order:

**`VariableRead`** — emitted each time a variable is read from bindings:
- `name()` — variable name
- `value()` — value at the time of read
- `systemProvided()` — true for `currDate`, `currTime`, `currDateTime`

**`FunctionCall`** — emitted for each function invocation:
- `functionName()` — name of the function
- `arguments()` — list of argument values at call time
- `result()` — the return value

**`AssignmentEvent`** — emitted for each assignment in an `AssignmentExpression`:
- `variableName()` — variable being assigned
- `value()` — assigned value

### Constant Folding and the Audit Trail

Three behaviors to keep in mind:

1. **Folded symbols produce pre-stored `VariableRead` events.** When an external symbol is registered with `overridable=false`, or when an internal variable is assigned a compile-time constant, the compiler folds it into the execution plan as a literal. The `VariableRead` events for those symbols are captured once at compile time and seeded into every `computeWithAudit()` call automatically — no variable lookup happens at evaluation time, but the reads are still observable.

2. **Pre-stored events appear before runtime reads.** Because folded events are prepended to the audit collector before evaluation starts, they appear at the beginning of the trace regardless of where the symbol appears in the expression.

3. **Folded function calls still emit `FunctionCall` events.** When a function with all-constant arguments is folded during compilation, the result is pre-computed. But the audit trail still records the call as if it ran at evaluation time, for observability.

### Audit Overhead

Based on JMH benchmarks against `compute()`:

| Scenario | Overhead |
|---|---|
| 12 variables, no assignments or functions | +36.6% |
| 1 assignment + variable reads | +30.4% |
| 4 function calls | +22.0% |

The overhead grows with the number of trackable events — variable reads contribute the most. For paths evaluated more than 100,000 times per second, measure whether `computeWithAudit()` fits your latency budget. For debugging and observability use cases, the overhead is predictable and acceptable.

## Combining Validation and Audit

A practical pattern for rule-engine applications:

```java
// At rule submission time
ValidationResult validation = expr.validate(userInput);
if (!validation.valid()) {
    return ValidationResponse.failure(validation.issues());
}
// Persist the expression along with validation.userVariables()
// so you know what data to fetch at evaluation time

// At evaluation time, audit a sample for debugging
AuditResult<Boolean> result = ruleExpr.computeWithAudit(domainBindings);
if (!result.value()) {
    log.debug("Rule failed. Variables: {}", result.trace().variableSnapshot());
}
```
