# Etapa 12 Pre-hardening Implementation Inventory

This inventory freezes the implementation frontier observed for issue #160 before resource limits,
work debits, diagnostic metadata, or linear regex change production paths. Paths are relative to
`exp-mk3/src/main/java`. Line numbers are intentionally omitted because later Etapa 12 increments will
move code; named types and methods are the stable review anchors.

## Diagnostic codes and emitters

The baseline registry is `internal/diagnostics/DiagnosticCode.java`: **64 codes** in five existing
prefix families (`PARSE_*`, `AST_*`, `SEMANTIC_*`, `RUNTIME_*`; semantic codes include the current
warning). Its issue #160 SHA-256 is
`83db5ce5f17df8e281d9a50a131ffd2ce0512b75d8ff8fe82ba11f026e00e5fb`.

Production references to that registry exist in:

- `api/BoundaryCoercion.java` and `api/ProviderMethodAdapter.java`;
- `internal/parser/ExpressionParser.java`;
- `internal/ast/SemanticAstBuilder.java`;
- `internal/semantics/SemanticResolver.java` and the eight `*DeferredCheck` implementations;
- `internal/diagnostics/RuntimeFailures.java`;
- `internal/runtime/PostfixExecutableNode.java`, `PublicMaterialization.java`, and
  `RealDomainArithmetic.java`.

These files are the initial emitter inventory for the later registry/exhaustiveness gate. A new
emitter outside this list or a registry hash change must update the inventory or replace it with the
automated registry introduced by Etapa 12.

## Official built-ins

`api/StandardBuiltInFunctions.java` and its `BuiltInFunctionGroup` are the authoritative inventory of
official names, overload signatures, and groups: math, transcendental, string, date/time, comparable,
financial, and assertion. The baseline `BuiltInFunctionGroup` contains **99 unique language names**;
the exact overload sets are declared by `expectedSignatures`, `financialExpectedSignatures()`, and
`assertionExpectedSignatures()`. Its issue #160 SHA-256 is
`23c1dee61bfce4f778e1f10ab5e04c2b2b7a23265007e3ab49805dfb0dfde315`.

Implementations are in `api/{Math,Transcendental,String,DateTime,Comparable,Financial,Assertion}BuiltInFunctions.java`.
Every official descriptor must later receive a `WorkCostPolicy` classification; custom reflected
providers are outside that classification and remain trusted/unmetered.

## Variable-work loops

The following production seams perform input-dependent traversal and therefore form the initial work
budget audit list:

| Area | Runtime seam | Work represented |
|---|---|---|
| Collection operations | `internal/runtime/ExpressionRuntime.executeAll/Any/Avg/Map/Reduce/SortBy/Sum/Keys/Values` | item visits, callbacks, key extraction, sorting, copies |
| Navigation collections | `ExpressionRuntime` filter, slice, wildcard, membership, and structural-equality helpers | visited or copied collection/map entries |
| Generic membership | `internal/runtime/MembershipExecutableNode` | candidates visited until match |
| Public values | `internal/runtime/PublicMaterialization` | list/map entries recursively copied |
| Java boundaries | `api/BoundaryCoercion`, `ProviderMethodAdapter`, and `JavaTypeCatalog` | converted, validated, or snapshotted values |
| Factorial | `internal/runtime/PostfixExecutableNode` | multiplications from 2 through the operand |
| String built-ins | `api/StringBuiltInFunctions.concat/join/replaceAll/split` | input traversal, regex, matches, segments, output |
| Numeric built-ins | math, transcendental, and financial built-in implementation classes | collection traversal or argument/precision-dependent algorithms |

The ten official collection operations are exhaustively wired by
`internal/runtime/CollectionOperationExecutors.java`: `ALL`, `ANY`, `AVG`, `COUNT`, `KEYS`, `MAP`,
`REDUCE`, `SORT_BY`, `SUM`, and `VALUES`. The baseline wiring SHA-256 is
`e0a41ad4de60b3735555f359a50f1396c2a3c66e6d3f5c2feb70770f1d5b05a6`.

## Expanding allocators

These are the current sites where tenant-controlled shape or content can expand an allocation before
the new limits are applied:

- `StringBuiltInFunctions`: `concat`, both padding variants through `BuiltInFunctionSupport.pad`,
  `repeat`, `replace`, `replaceFirst`, `replaceAll`, `split`, and `join`;
- `ExpressionRuntime`: collection literal creation, slice, filter, wildcard, map, keys, values,
  sorting, and public operation-result lists;
- `PublicMaterialization`: recursive list and map snapshots;
- `BoundaryCoercion`, `ProviderMethodAdapter`, and `JavaTypeCatalog`: external/provider collection,
  map, and array snapshots or conversions;
- `CalculationRecorder`, `VariableMemorySchema`, and `DefaultCalculationMemory`: arrays published for
  Calculation Memory (bounded by plan shape/reached points rather than a new independent policy);
- parser token buffering, `SemanticAstBuilder`, and semantic/planner collections: structures that grow
  with source tokens or AST nodes and will be bounded by the compilation limits.

This is an audit inventory, not a claim that every listed allocation is currently unsafe or belongs
on the hot path. Later increments replace prose coverage with exhaustive tests where the source has a
closed registry (diagnostics, built-ins, and collection operations).
