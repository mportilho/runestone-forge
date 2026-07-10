# ADR 0007: Integer Literals Are Decimal-Only

## Status

Accepted

## Context

The current grammar accepts hexadecimal and octal integer literals, and the original Etapa 4 plan only rejected them in subscripts and slices. That creates a special rule where the same token form is valid in arithmetic but invalid in navigation indexes, and it forces the AST, pretty-printer, corpus, and semantic resolver to preserve integer literal bases only to reject some of them later.

## Decision

Expression integer literals are decimal-only in all source positions. Hexadecimal and octal forms such as `0x10` and `077` are removed from the grammar instead of being accepted generally or rejected only in subscripts.

## Consequences

The grammar, AST builder, AST value model, pretty-printer, and corpus must be simplified to remove integer literal base handling. The Etapa 4 resolver no longer owns a hex/octal-in-subscript policy; migration diagnostics may still recognize old hex/octal-looking source forms when that can be done without reintroducing them as valid grammar.
