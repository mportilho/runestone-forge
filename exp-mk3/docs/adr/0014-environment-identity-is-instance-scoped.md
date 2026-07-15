# ADR 0014: Environment Identity Is Instance-Scoped

## Status

Accepted

## Context

The original environment identity was a SHA-256 fingerprint of every compilation-relevant setting, catalog, implementation descriptor, and external-symbol default. Keeping that fingerprint complete required environment-wide canonical serialization, reflective traversal of object defaults, stable implementation IDs, provider IDs, and broad tests that duplicated every environment contract. This complexity existed to let separately built but equivalent environments share compiled plans.

## Decision

Each built `Ambiente de Expressao` receives an internally generated, opaque UUID string as its `Identificador de Instancia do Ambiente`. The identifier cannot be supplied or persisted by callers. It remains stable only for that environment instance; separately built environments receive different identifiers even when their configurations are equal. `ExpressionEnvironment.standard()` retains one identifier during the JVM lifetime because it is a singleton.

## Consequences

The compilation cache key remains `(source, environmentId)`, but cache sharing is intentionally limited to reuse of the same environment instance. Different environment contents are cache-safe because they cannot share an identifier; equivalent independently built environments deliberately do not share plans. Content fingerprinting, `ExpressionEnvironmentId`, identity-only canonical serialization, stable implementation IDs, and provider IDs are removed. Conversion-profile identity, deterministic catalog behavior, function signatures, and descriptive Java implementation metadata remain where they serve contracts other than environment identity. This ADR amends the environment-identity clauses of ADRs 0004, 0005, and 0013.
