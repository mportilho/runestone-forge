# ADR 0009: Source Language Has No Null Literal

## Status

Accepted

Partially superseded by ADR 0012 for external overrides and normal runtime-null origins. Source `null` remains absent; safe navigation is the normal language construct that introduces possible runtime null into the semantic model.

## Context

The current grammar accepts `null` as a source literal and the original semantic plan included a `NullType`. During Etapa 4 planning, null-as-bottom and nullable branch/vector behavior proved unstable because the language has no explicit nullable type and function calls should not accept null arguments.

## Decision

The expression source language does not include a `null` literal. Null remains a possible runtime value from Java data, external overrides, navigation results, method results, and safe navigation; source-level protection against runtime null uses explicit language constructs such as `??` and `?.`.

## Consequences

The grammar, AST literal model, corpus, and semantic type system should remove source `null` handling. The semantic resolver should model possible runtime null as binding metadata where useful, not as a normal expression type. Migration diagnostics may still recognize source `null` from older expressions and suggest an explicit default, fallback, or environment value.
