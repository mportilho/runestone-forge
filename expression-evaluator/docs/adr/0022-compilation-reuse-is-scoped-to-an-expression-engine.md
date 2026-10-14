# ADR 0022: Compilation Reuse Is Scoped to an Expression Engine

## Status

Accepted

## Context

Compilation reuse needs a lifecycle owner. A process-wide cache would maximize sharing, but it would also couple otherwise isolated callers and could return a compiled expression carrying the `RuntimeServices` and `Clock` of another caller. Keeping only the static compiler would avoid that coupling but would leave repeated and concurrent calls free to rebuild duplicate immutable plans. Environment identity is already instance-scoped by ADR 0014, so independently built equivalent environments are intentionally not interchangeable.

## Decision

The long-lived Expression Engine owns compilation reuse and its `RuntimeServices`. Within one engine, an exact source string and an Environment Instance Identifier select one resident compilation generation. Concurrent requests for that key share its compilation. Different engines never share cache entries, even when they receive the same source and the same Expression Environment, and the source is not normalized for reuse.

Eviction or expiration ends only the resident generation: compiled expressions and expression views already returned remain valid, while a later compilation request may create a new generation. The cache is never consulted during execution.

## Consequences

The Expression Engine, rather than a static compiler or process-wide plan registry, is the public compilation boundary. Applications can use a default singleton or create isolated engines with distinct runtime clocks and cache policies. Formula catalogs may retain compiled views for their own business lifecycle without putting a cache lookup in the execution path. Sharing across engines, across separately built equivalent environments, or through a distributed second-level cache is deliberately excluded.
