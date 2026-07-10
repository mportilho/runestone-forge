# ADR 0008: External Symbols Require Defaults and Overwrite Policy

## Status

Accepted

## Context

The original Etapa 3 plan allowed external symbols to be declared as name plus default value, name plus declared type without default, or name only with unknown type. That made absence a runtime concern only when an expression actually needed the value, but it also allowed weak input contracts and introduced unknown external symbol types into semantic resolution.

## Decision

Every external symbol declared by an Ambiente de Expressao must have a default value and an explicit overwrite policy. The symbol type is either declared and validated against the default, or inferred from the default; runtime inputs may replace the default only for overridable symbols and must pass boundary coercion.

## Consequences

The external symbol catalog no longer supports type-only or name-only declarations. Semantic resolution receives concrete external symbol types from the environment, non-overridable symbols can be treated as fixed environment values, and unknown types no longer originate from empty external symbol declarations.
