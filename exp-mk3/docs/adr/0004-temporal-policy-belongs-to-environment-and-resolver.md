# ADR 0004: Temporal Policy Belongs to Environment and Resolver

## Status

Accepted; environment-identity clauses amended by ADR 0014

## Context

Etapa 3 defines a single `DATETIME` type and says offset date-time literals are normalized in the environment time zone. ADR 0002 decided that the Arvore Semantica de Expressao must remain independent of the Ambiente de Expressao, preserving date-time literals with offsets as their source-faithful values instead of normalizing them during AST construction.

The evaluator also has dynamic current temporal values such as `currDate`, `currTime`, and `currDateTime`. These values are derived during execution, but converting an execution instant into local date/time values requires an explicit time-zone policy.

## Decision

The Ambiente de Expressao owns temporal policy through an explicit time zone. Cache isolation now follows the per-instance identity defined by ADR 0014 rather than including the time zone in a content fingerprint.

The standard Ambiente de Expressao uses the JVM default time zone as its default `ZoneId`. Callers that need reproducible temporal semantics and stable Identificadores de Ambiente across machines, containers, or deployments must configure the environment time zone explicitly.

The Arvore Semantica de Expressao continues to preserve date-time literals as source-faithful values. The semantic resolver applies the Ambiente de Expressao time zone when assigning the single `DATETIME` type: date-time literals with an explicit offset are converted into the environment time zone, while date-time literals without an explicit offset are interpreted as local date-times in the environment time zone using that zone's effective offset for the local date-time.

Semantic metadata may retain the original literal, whether the offset was explicit or inferred, the environment time zone, the effective offset, and the normalized value for diagnostics. The hot execution plan only needs the normalized `DATETIME` value.

Current temporal values are derived from one execution instant per evaluation and use the same Ambiente de Expressao time zone. The execution clock is a runtime dependency and test seam, not part of environment instance identity.

## Consequences

The same source can compile to different temporal semantics under different environment time zones, and those environments will not share the same compiled-plan cache key.

The standard environment is convenient for local or host-relative evaluation. Its instance identifier is process-local regardless of whether JVM default time zones agree.

The semantic tree remains deterministic for a given source and does not leak environment policy into parsing or AST construction.

Temporal diagnostics can explain offset normalization without adding branches or metadata to the hot runtime path. Calculation Memory records reached current temporal values, not static temporal-literal normalization.
