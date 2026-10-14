# ADR 0012: External Symbol Overrides Cannot Be Null

## Status

Accepted

## Context

ADR 0009 removed the source `null` literal but still allowed runtime null values to originate from external symbol overrides. During Etapa 4 planning, that exception weakened the external symbol contract: every external symbol already requires a non-null default and a known type, but an override could still replace that value with null and make every overridable symbol potentially null.

## Decision

External symbol overrides cannot be null. Every external symbol keeps a non-null default, and every runtime override must also provide a non-null value that passes boundary coercion for the symbol's known type. Under the Etapa 4 semantic model, the normal language-level source of `MAY_BE_NULL` is safe navigation. Java nulls returned by external data, registered members, maps, or collections are boundary/runtime contract violations rather than normal expression values.

## Consequences

Overridable external symbols are not automatically `MAY_BE_NULL` merely because they are overridable. A null override is a boundary input error at execution time, not a semantic type decision and not a source-language null value. The semantic resolver can treat external symbols, registered Java members, map values, and collection elements as non-null by contract; safe navigation is the construct that explicitly introduces possible runtime null into the expression model.
