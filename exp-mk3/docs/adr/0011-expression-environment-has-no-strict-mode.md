# ADR 0011: Expression Environment Has No Strict Mode

## Status

Accepted

## Context

The original environment plan included a strict mode mainly to reject residual `UnknownType` and runtime-deferred semantic decisions. Later decisions require all accepted semantic models to have known types and removed unknown-type deferral from successful resolution.

## Decision

The Ambiente de Expressao does not expose strict mode. Semantic resolution always requires known types, and deferred checks are limited to runtime value preconditions for already-typed constructs.

## Consequences

Environment identity, builders, tests, and semantic resolution should remove strict-mode configuration. Future policies such as treating warnings as errors or rejecting expressions that may produce runtime null should be introduced under explicit names rather than reusing strict mode.
