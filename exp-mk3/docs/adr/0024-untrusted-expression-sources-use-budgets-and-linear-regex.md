# ADR 0024: Resource Enforcement Uses Trust Modes, Traversal Steps, and Linear Regex

## Status

Accepted during Etapa 12 planning on 2026-09-07.

## Context

Tenants may supply expression sources and runtime values, so accidental limits alone are insufficient:
recursive syntax can exhaust the Java stack, chained collection operations can multiply bounded
materializations, and `java.util.regex.Pattern` can spend exponential time on a short adversarial
pattern. A generic timeout cannot safely stop parser or regex work in-process, while treating registered
Java providers as hostile would require a process sandbox outside the evaluator's architecture.

## Decision

`ExpressionEnvironment` selects a public `ExpressionTrustMode`:

- `TRUSTED` is the default. It enforces compilation-time resource limits (source length, tokens,
  projected syntax depth, AST nodes, Current Item depth, constant/value shape checks, literal regex
  limits, and preflight of constant expansion), but does not enforce runtime resource limits.
- `SAFE` enforces the complete resource policy, including runtime value shape, materialization,
  dynamic regex, factorial/deferred checks, and traversal-step limits. Resource exhaustion is terminal
  for the affected phase or execution.
- `UNSAFE` enforces none of the expression resource limits. It is for formulas and values whose resource
  behavior is wholly trusted by the integrator.

All modes preserve functional language contracts: type/nullability and boundary coercion contracts,
structured non-resource diagnostics, and RE2/J as the only regex engine. `ExpressionResourceLimits`
remains validated against its defaults and absolute ceilings in every mode; it is enforced only where the
selected trust mode applies it. Resource diagnostics follow enforcement: none in `UNSAFE`, compilation
diagnostics only in `TRUSTED`, and all applicable diagnostics in `SAFE`.

The evaluator does not implement an internal wall-clock timeout and does not catch fatal JVM errors;
call deadlines belong to the integrator. Registered environments, function providers and Java members
remain trusted components rather than sandboxed code.

The cumulative execution guard rail is a Traversal Step Limit used only at runtime in `SAFE`, not a cost
model. A step is charged for each item or entry reached while iterating a collection or map or while
recursively converting, validating or materializing one. Nested traversals share the same counter and
lazy operations charge only reached items. Built-in invocations, constant scalar nodes,
algorithm-specific complexity, numeric precision, regex input products, output length and work inside
trusted providers are not translated into steps.

An individually expensive operation is bounded by its local structural limit instead: text and output
length, regex pattern and input size, collection cardinality, numeric shape, factorial input, or another
explicit parameter governing growth. Those local limits must make one accepted invocation safe. Under
`TRUSTED` and `SAFE`, folding does not execute function calls or collection operations whose work is
input-dependent; constant expansion is checked before allocation. Every `SAFE` execution receives a fresh
traversal allowance. An over-limit traversal fails before its next item or entry. Previous provider
effects are not rolled back.

The Expression Engine cache combines an exact entry-count limit with a conservative retained-weight
budget for source-controlled keys and compilation results. Weight is calibrated from JVM layouts but
does not claim exact heap accounting for trusted shared environment components. A compilation result
that exceeds the resident budget may be returned without becoming resident. Parser `ThreadLocal` state is
cleared in every mode. Cache bounds and parser cleanup are Engine lifecycle guarantees, not expression
runtime enforcement.

All regular expressions controlled by the language use RE2/J's linear-time subset. This includes
literal regex operators and built-ins that receive patterns dynamically. Unsupported constructs such
as backreferences and lookaround fail through stable structured diagnostics, with no fallback to
`java.util.regex.Pattern`.

## Consequences

The accepted regex language is intentionally narrower than Java regex and must be documented. Traversal
step counts are an operational guard rail and are not part of semantic equivalence with the Oraculo Sem
Otimizacoes: an optimization may eliminate protected steps, and callers must not depend on an exact
count. `TRUSTED` and `UNSAFE` must add zero B/op and stay within ±1% of the baseline for scalar
and collection execution; `SAFE` retains the scalar gate and permits up to 5% paired collection
regression with zero allocation per traversal-step debit. Trusted provider code can still block, allocate without
bound or violate its concurrency contract; containing that code requires isolation by the embedding
application and is outside `exp-mk3`.
