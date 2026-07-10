# ADR 0010: Semantic Model Requires Known Types

## Status

Accepted

## Context

The original plan included `UnknownType`, external symbols without defaults, deferred navigation over unknown receivers, and strict-mode rejection of residual unknowns. Later planning decisions made external symbols strongly declared through non-null defaults, removed the source `null` literal, and required registered functions, Java members, and collection operations to expose known contracts.

## Decision

Accepted expressions must resolve to known semantic types at compilation time. Missing Java metadata, unconstrained empty containers, ambiguous function contracts, and unresolved navigation targets are semantic errors rather than unknown types deferred to runtime.

## Consequences

The semantic resolver no longer produces `UnknownType` in a successful `Modelo Semantico`. Deferred checks are limited to runtime value preconditions for already-typed constructs, such as factorial bounds, root degree constraints, subscript bounds, receiver null checks, and materialization limits. Strict mode no longer exists to reject residual unknown types unless a later decision gives it a different purpose.
