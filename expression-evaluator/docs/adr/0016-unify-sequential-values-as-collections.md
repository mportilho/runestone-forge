# ADR 0016: Unify Sequential Values as Collections

## Status

Accepted

## Context

The language previously distinguished vectors created by expressions from externally supplied collections, used `..` for collection operations, and split wildcard expansion between `[*]` and `.*`. That distinction leaked source provenance into public types, duplicated parsing and AST paths, prevented uniform safe navigation, and made mixed value/lambda argument contracts difficult to express.

## Decision

The language has one sequential `CollectionType<T>`, represented canonically as an immutable ordered list and used for literals, external arrays and iterables, provider containers, navigation, and operation results. Collections are indexable, sliceable, filterable, wildcard-expandable, and assignment-destructurable. `VectorType` and all vector-specific public, AST, descriptor, corpus, and diagnostic concepts are removed without compatibility aliases.

Receiver calls use ordinary `.` or safe `?.` syntax. The source-faithful AST records an unresolved call with a common ordered list of value or lambda arguments; the semantic resolver classifies it from the receiver's known type. Collections and maps consult the dedicated collection-operation catalog, nominal objects consult only explicitly exposed methods, and runtime plans retain a resolved operation identity rather than performing textual lookup. Global functions, reflected providers, and registered object methods do not accept lambdas in the initial version.

`[*]` is the only wildcard spelling. It expands collection elements, map values in canonical key order, or an explicitly registered homogeneous set of object child members. `..` and `.*` are ordinary parse errors, with no compatibility handling because no released source contract used them.

The official collection operations are `all`, `any`, `count`, `keys`, `map`, `sum`, `values`, `avg`, `reduce`, and `sortBy`. Their descriptors declare ordered value/lambda argument contracts, contextual current-item types, result rules, purity, evaluation policy, and materialization. `reduce` exposes an Item de Reducao through `@.accumulator` and `@.item`. The initial public API does not permit custom collection operations, while preserving an internal extension seam.

## Consequences

All Java containers crossing an expression boundary become bounded immutable snapshots. Source iteration order defines collection order; maps use locale-independent lexicographic key order. Provider parameters declared as sets remain explicit boundary coercions that preserve first occurrence and remove duplicates. Java container overloads that collapse to the same language signature are rejected while building the environment.

ADR 0005 remains authoritative for the dedicated catalog and semantic operation bindings but not for `..`, early source-AST classification, or public custom-operation registration. ADR 0015 remains authoritative for setup-time provider resolution but not for distinct vector/list versus collection/iterable language types.
