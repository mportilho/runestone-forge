# ADR 0005: Collection Operations Use a Dedicated Catalog

## Status

Accepted; environment-identity clauses amended by ADR 0014

## Context

Collection-operation syntax such as `..sum()`, `..map(@ -> e)`, `..keys()`, and user-provided `..op()` extensions looks similar to a function call, but it has receiver-specific semantics, optional Item Atual behavior, materialization policy, and future pipeline-optimization requirements. Reusing the global `FunctionCatalog` would obscure those semantics and make overload/function binding look responsible for navigation behavior.

## Decision

Collection operations are resolved through a dedicated, extensible collection-operation catalog owned by the Ambiente de Expressao. Built-in and user-provided operations declare their receiver requirements, argument shape, return type, purity/materialization flags, and Item Atual policy separately from global functions. Cache safety follows the owning environment instance identity defined by ADR 0014 rather than a canonical catalog fingerprint.

## Consequences

The semantic resolver produces navigation bindings for collection operations instead of function bindings. User extensions can still delegate internally to registered functions, but `..op()` remains navigation over a receiver rather than a global function call. Separately built environments do not share compiled plans, even when their catalogs are equivalent.
