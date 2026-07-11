# ADR 0002: Environment-Independent Literal Materialization in the Semantic Tree

## Status

Accepted

Partially superseded by ADR 0013 for `NumericMode`/`FAST`. Literal AST remains environment-independent; numeric execution mode is no longer a public v2 concept.

## Context

Etapa 2 originally said `FLOAT` literals should become the type of the numeric mode, and Etapa 3 normalizes offset date-time literals using the environment time zone. The Arvore Semantica de Expressao is the source-faithful contract between parsing and later semantic/runtime phases, so making it depend on numeric mode or time zone would make the same source produce different trees for different Ambientes de Expressao.

## Decision

The Arvore Semantica de Expressao materializes literals independently of the environment. `INT` becomes `long` or `BigInteger`, `FLOAT` becomes an exact `BigDecimal`, date and time literals become `LocalDate` and `LocalTime`, date-time literals without offset become `LocalDateTime`, and date-time literals with offset become `OffsetDateTime`. `NumericMode` and environment time-zone normalization are applied later by the resolver and plan builder.

## Consequences

The tree stays deterministic for a given source and easier to use in structural round-trip tests. FAST-mode compilation and offset date-time normalization pay a later conversion step, but avoid leaking execution strategy or environment policy into the semantic tree.
