# ADR 0024: Untrusted Expression Sources Use Deterministic Budgets and Linear Regex

## Status

Accepted during Etapa 12 planning on 2026-09-07.

## Context

Tenants may supply expression sources and runtime values, so accidental limits alone are insufficient:
recursive syntax can exhaust the Java stack, chained collection operations can multiply bounded
materializations, and `java.util.regex.Pattern` can spend exponential time on a short adversarial
pattern. A generic timeout cannot safely stop parser or regex work in-process, while treating registered
Java providers as hostile would require a process sandbox outside the evaluator's architecture.

## Decision

The evaluator accepts untrusted expression sources under deterministic, non-disableable resource
budgets. Compilation bounds source length, token count, general syntax depth and semantic-tree node
count; compilation and execution cumulatively bound evaluator-controlled variable-cost work and value
shapes. The work budget covers collection traversal, regex, text expansion, expensive official numeric
operations and boundary materialization, but not constant-cost scalar nodes or trusted provider bodies.
Every budget has a generous default in the Ambiente de Expressao and an evidence-backed absolute ceiling.
Untrusted boundary values and evaluator-produced values are also bounded by text length, recursive
value depth, numeric precision and numeric scale magnitude. Resource exhaustion is terminal for the
affected compilation phase or execution rather than subject to continued diagnostic accumulation.
The evaluator does not implement an internal wall-clock timeout and does not catch fatal JVM errors;
call deadlines belong to the integrator. Registered environments, function providers and Java members
remain trusted components rather than sandboxed code.

The Expression Engine cache combines an exact entry-count limit with a conservative retained-weight
budget for source-controlled keys and compilation results. Weight is calibrated from JVM layouts but
does not claim exact heap accounting for trusted shared environment components. A compilation result
that exceeds the resident budget may be returned without becoming resident.

All regular expressions controlled by the language use RE2/J's linear-time subset. This includes
literal regex operators and built-ins that receive patterns dynamically. Unsupported constructs such
as backreferences and lookaround fail through stable structured diagnostics, with no fallback to
`java.util.regex.Pattern`.

## Consequences

The accepted regex language is intentionally narrower than Java regex and must be documented. Limit
checks become part of the public semantic/runtime contract and optimized plans must fail at the same
semantic budget boundary as the Oraculo Sem Otimizacoes. Trusted provider code can still block, allocate
without bound or violate its concurrency contract; containing that code requires isolation by the
embedding application and is outside `exp-mk3`.
