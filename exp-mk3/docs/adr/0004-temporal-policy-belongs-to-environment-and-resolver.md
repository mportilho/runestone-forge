# ADR 0004: Temporal Policy Belongs to Environment and Resolver

## Status

Accepted

## Context

Etapa 3 defines a single `DATETIME` type and says offset date-time literals are normalized in the environment time zone. ADR 0002 decided that the Arvore Semantica de Expressao must remain independent of the Ambiente de Expressao, preserving date-time literals with offsets as their source-faithful values instead of normalizing them during AST construction.

The evaluator also has dynamic current temporal values such as `currDate`, `currTime`, and `currDateTime`. These values are derived during execution, but converting an execution instant into local date/time values requires an explicit time-zone policy.

## Decision

The Ambiente de Expressao owns temporal policy through an explicit time zone. That time zone is part of the Identificador de Ambiente because it affects compilation semantics and cache safety.

The standard Ambiente de Expressao uses the JVM default time zone as its default `ZoneId`. Callers that need reproducible temporal semantics and stable Identificadores de Ambiente across machines, containers, or deployments must configure the environment time zone explicitly.

The Arvore Semantica de Expressao continues to preserve offset date-time literals as source-faithful values. The semantic resolver applies the Ambiente de Expressao time zone when assigning the single `DATETIME` type to offset date-time literals, producing the normalized local date-time value used by later semantic and execution phases.

Semantic metadata may retain the original offset literal, the environment time zone, and the normalized value for diagnostics and audit. The hot execution plan only needs the normalized `DATETIME` value.

Current temporal values are derived from one execution instant per evaluation and use the same Ambiente de Expressao time zone. The execution clock is a runtime dependency and test seam, not part of the Identificador de Ambiente, unless a future compilation feature makes clock identity affect compilation.

## Consequences

The same source can compile to different temporal semantics under different environment time zones, and those environments will not share the same compiled-plan cache key.

The standard environment is convenient for local or host-relative evaluation, but its Identificador de Ambiente can differ across JVMs with different default time zones.

The semantic tree remains deterministic for a given source and does not leak environment policy into parsing or AST construction.

Temporal diagnostics and audit can explain offset normalization without adding branches or metadata to the hot runtime path.
