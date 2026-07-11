# ADR 0013: Numeric Semantics Are Decimal-Only

## Status

Accepted

## Context

The original v2 plan exposed a `NumericMode` with `DECIMAL` and `FAST`. `FAST` implied public configuration, environment identity changes, primitive `long`/`double` execution paths, overflow fallback to decimal nodes, and dedicated performance gates. During Etapa 4 planning, the semantic model was simplified around known types, explicit null handling, and a single predictable numeric contract.

## Decision

Expression numeric semantics are decimal-only for the planned v2. The `Ambiente de Expressao` does not expose `NumericMode` while there is no second semantic mode. `MathContext` and `transcendentalMathContext` remain environment settings. `Fato Numerico` remains internal metadata for validation and planning, such as preserving integral facts for `root` and factorial, but it does not select a public `FAST` mode.

## Consequences

`ExpressionEnvironmentId` does not include numeric mode. Existing references to `NumericMode`, `NumericMode.FAST`, primitive numeric public execution paths, `computeAsLong`, `computeAsDouble`, FAST-specific JMH gates, and structural fallback from FAST to decimal should be removed from the current plan and code during the preparation/saneamento work. Future numeric specialization may still optimize implementation details, but it must preserve decimal semantics unless a new ADR introduces a different public contract.
