# ADR 0003: Optional Field for the Top-Level Result Expression

## Status

Accepted

## Context

The Java guideline for this repository says to use `Optional<T>` for absent return values, never as a field or parameter. The top-level Arvore Semantica de Expressao is a deliberate exception because the grammar itself defines an Arquivo de Expressao as zero or more assignments followed by an optional result expression.

## Decision

`ExpressionFileNode` uses `Optional<ExpressionNode>` for its `resultExpression` field. This exception is limited to the immutable root AST record: optionality is central to the source grammar, and the sealed-variant alternative would duplicate the root node shape only to encode one optional child.

## Consequences

Other AST records should not treat this as permission to use `Optional` fields. Required children remain non-null, collections remain immutable and non-null, and other optional structural cases need their own justification.
