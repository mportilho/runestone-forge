# Refactoring Analysis: expression-evaluator

## Module Summary

`expression-evaluator` is the most complex module in the repository. It implements an expression language with a compilation pipeline:

1. Parse source text with ANTLR.
2. Build semantic AST nodes.
3. Resolve symbols, types, functions, operators, and navigation semantics.
4. Build an execution plan.
5. Evaluate math, logical, and assignment expressions.

The conceptual pipeline is sound, but the package structure does not fully reflect it. Most classes after AST construction currently live under `com.runestone.expeval.internal.runtime`, which has become a broad package containing compiler, semantic resolver, execution plan, evaluators, coercion services, navigation operations, and audit support.

## Current Structure

Approximate package concentration in `src/main/java`:

| Package | Approx. Java Files | Role |
|---|---:|---|
| `com.runestone.expeval.internal.runtime` | 47 | Compiler, semantic model/resolver, execution plan, executable nodes, evaluators, runtime services, navigation operations, audit. |
| `com.runestone.expeval.internal.ast` | 23 | AST records/nodes and AST validation helpers. |
| `com.runestone.expeval.api` | 15 | Public expression facade, validation, exceptions, audit result types. |
| `com.runestone.expeval.internal.grammar` | 15 | ANTLR generated sources and parser facade. |
| `com.runestone.expeval.types` | 9 | Resolved type model. |
| `com.runestone.expeval.catalog` | 9 | Function, external symbol, type hint descriptors/catalogs. |
| `com.runestone.expeval.catalog.functions` | 7 | Built-in function providers. |
| `com.runestone.expeval.internal.navigation` | 6 | Navigation enums/cache/introspection support, but not the main navigation execution logic. |
| `com.runestone.expeval.environment` | 3 | Environment and builder. |
| `com.runestone.expeval.internal.ast.mapping` | 1 | `SemanticAstBuilder`, currently very large. |

Large manual files:

| File | Lines | Concern |
|---|---:|---|
| `SemanticAstBuilder.java` | 1423 | Parse-tree to AST mapping for many sublanguages. |
| `ExecutionPlanBuilder.java` | 1160 | Plan building, folding, symbol indexing, literal materialization, navigation planning. |
| `SemanticResolver.java` | 991 | Symbol resolution, type inference, overload resolution, navigation resolution, operator validation. |
| `ExpressionEnvironmentBuilder.java` | 416 | Function registration, reflective scanning, type metadata discovery, environment ID hashing. |
| `AbstractObjectEvaluator.java` | 392 | Main object evaluator and function invocation flow. |
| `PropertyChainOps.java` | 381 | Runtime navigation/property-chain behavior. |
| `CollectionNavigationOps.java` | 327 | Collection/map navigation behavior. |

## Structure Assessment

- Organization: mixed layered compiler/runtime pipeline.
- Clarity: drifting.
- Main package smell: `internal.runtime` is a package for nearly everything after AST.
- Main boundary smell: public API still imports internal runtime support types, but `ExpressionEnvironment` no longer exposes runtime services.
- Main duplication hotspots: runtime invocation, constant folding evaluation, navigation classification, string literal `@`, source pointer formatting.

## Refactoring Direction

The desired direction is to separate code by stable responsibilities in the expression pipeline, not to create one class for every branch, operator, grammar alternative, or special case.

Good split candidates:

- A pipeline phase with its own vocabulary and invariants, such as semantic resolution, execution planning, evaluation, runtime coercion, navigation, or audit.
- A cohesive responsibility that is used from multiple branches, such as symbol binding, navigation typing, executable node construction, literal materialization, or runtime invocation.
- A collaborator that can be characterized and tested without reproducing the entire compiler pipeline.

Avoid split candidates:

- One class per `if`/`switch` branch.
- One class per operator unless the operator family has materially different rules and enough behavior to justify it.
- Generic `*Utils` classes that only move private methods without creating a clearer boundary.
- Public interfaces for collaborators that have one implementation and no external polymorphic consumer.

Prefer a small number of package-private `final` collaborators per phase. Keep branch-specific code as private methods or compact strategy tables inside those collaborators until a second real responsibility appears.

When extracting collaborators, preserve object lifetime. Prefer moving existing state and behavior to collaborators created once during compiler/environment construction, or to stateless shared instances, instead of allocating new helper objects during expression compilation or evaluation loops.

## Detailed Findings

| Severity | Issue | Evidence | Recommendation |
|---|---|---|---|
| Medium | Public API implementation depends on internal runtime types | `MathExpression`, `LogicalExpression`, and `AssignmentExpression` store `ExpressionRuntimeSupport`; `ExpressionEngine` imports `ExpressionCompilationCache`, `ExpressionRuntimeSupport`, and `SemanticModel`. These are implementation dependencies, not public method signatures. | Keep these dependencies package-internal to the module for now. If the boundary must be stricter later, move the public facade implementation bridge behind an internal engine/runtime facade without exposing compiler or runtime services publicly. |
| Resolved | Public environment exposed internal runtime services | Previously, `ExpressionEnvironment` imported `RuntimeServices`, stored it, and exposed `public RuntimeServices runtimeServices()`. This has been removed. `RuntimeServices` and `RuntimeCoercionService` are now package-private under `internal.runtime`. | Keep runtime services internal. Do not reintroduce `ExpressionEnvironment.runtimeServices()` or public accessors for coercion/runtime internals. |
| High | `internal.runtime` mixes too many phases | 47 Java files include `ExpressionCompiler`, `SemanticResolver`, `ExecutionPlanBuilder`, `Executable*`, `MathEvaluator`, `LogicalEvaluator`, `RuntimeCoercionService`, `PropertyChainOps`, `AuditCollector`. | Split by pipeline phase: compiler, semantic, execution plan, execution eval, runtime services, navigation, audit. |
| High | `SemanticResolver` has too many reasons to change | 991 lines covering assignment symbols, literal type inference, identifiers, `@`, function overloads, property/member navigation, collection functions, operators, result type validation. | Extract a few cohesive phase collaborators: symbol/scope resolution, callable/operator typing, and navigation typing. Keep individual operator and literal branches inside those collaborators unless they grow into independent responsibilities. |
| High | `ExecutionPlanBuilder` mixes plan construction with folding/evaluation concerns | 1160 lines covering symbol indexing, defaults, external binding plans, audit event estimation, node building, navigation building, foldability rules, literal materialization, constant folding evaluator. | Split into broad planning responsibilities: binding/index planning, executable expression planning, navigation planning, and constant folding/literal materialization. Avoid one planner per node kind. |
| Medium-high | `SemanticAstBuilder` is a parse mapping god class | 1423 lines; maps math/logical/assignment, literals, operators, property chains, filters, lambdas, collection functions, spans. | Do not include `SemanticAstBuilder` in this refactoring scope. It is grammar-adjacent, broad, and risky; leave it unchanged until compiler/runtime package boundaries are stable. |
| Medium-high | Evaluation logic is duplicated between runtime and constant folding | Runtime evaluation lives in `AbstractObjectEvaluator`; folding evaluator lives inside `ExecutionPlanBuilder`. Both handle binary short-circuit, conditionals, vectors, function calls. | Share the common evaluation mechanics where it stays cheap, or keep a dedicated constant-folding path that reuses operator/function helpers. Avoid a new policy/interpreter layer if it adds dispatch to hot evaluation. |
| Medium | Runtime invocation by arity appears in multiple places | Invocation logic exists in `FunctionDescriptor`, `AbstractObjectEvaluator`, and `PropertyChainOps`. | Extract `RuntimeInvocationSupport` or `MethodHandleInvoker`, preserving fast paths 0-6 in one place. |
| Medium | Main navigation code is not in `internal.navigation` | `internal.navigation` contains enums/cache/introspection, while `PropertyChainOps`, `CollectionNavigationOps`, `ExecutablePropertyChain`, `FilterContextStack`, `DeepScanContext` are in `internal.runtime`. | Move navigation execution/planning classes under navigation or execution-navigation subpackages. |
| Medium | Sentinel `@` is represented as repeated string literal | Search found relevant occurrences in `SemanticAstBuilder`, `SemanticResolver`, `ExecutionPlanBuilder`, `AbstractObjectEvaluator`, and `PropertyChainOps`. | Introduce `LanguageSymbols.CURRENT_ELEMENT` or `SymbolKind.CURRENT_ELEMENT`. Avoid fake external `SymbolRef("@", SymbolKind.EXTERNAL)`. |
| Medium | Type introspection policy is split | `ExpressionEnvironmentBuilder` discovers type hints via public methods/fields; `TypeIntrospectionSupport` walks declared members and hierarchy for reflective access. | Introduce a shared `TypeMetadataDiscoverer` with explicit policies for public API metadata vs runtime fallback. |
| Medium | `FunctionDescriptor` mixes catalog metadata and invocation mechanics | It stores metadata and method handles, and provides arity-specific `invoke` overloads. | Decide whether catalog is runtime-facing. If not, split `FunctionDescriptor` metadata from `FunctionInvoker`. |
| Low-medium | AST and executable navigation models are parallel | `PropertyChainNode` and `ExecutablePropertyChain` have corresponding step types. Some duplication is expected, but every new navigation feature touches many places. | Consider `ResolvedNavigationChain` as an intermediate semantic model between AST and executable plan. |
| Low-medium | Source pointer formatting likely repeats across exceptions | Compilation/parsing/evaluation exceptions format source snippets and carets. | Extract `SourcePointerFormatter` or `CompilationMessageFormatter`. |

## Public/Internal Boundary Problem

The package name `internal.runtime` says implementation detail. The `ExpressionEnvironment` runtime-service leak has been fixed, but public API facades still depend on internal runtime support types internally.

Completed boundary fix:

```text
com.runestone.expeval.environment.ExpressionEnvironment
  no longer imports, stores, or exposes RuntimeServices

com.runestone.expeval.internal.runtime.RuntimeServices
  package-private

com.runestone.expeval.internal.runtime.RuntimeCoercionService
  package-private
```

The compile cache now owns the runtime support lifecycle:

```text
ExpressionEngine.compile*(...)
  -> ExpressionCompilationCache.compileRuntime(...)
  -> cached ExpressionRuntimeSupport
  -> internal RuntimeServices created only on cache miss
```

This avoids recreating runtime/coercion services and evaluator wrappers on compile cache hits.

Remaining boundary concern:

Current pattern:

```text
com.runestone.expeval.api.ExpressionEngine
  -> com.runestone.expeval.internal.runtime.ExpressionCompilationCache
  -> com.runestone.expeval.internal.runtime.ExpressionRuntimeSupport

com.runestone.expeval.api.MathExpression / LogicalExpression / AssignmentExpression
  -> com.runestone.expeval.internal.runtime.ExpressionRuntimeSupport
```

This is an implementation dependency from public facade classes to internal runtime classes. It is not currently exposed in public method signatures, so it is lower risk than the former `ExpressionEnvironment.runtimeServices()` leak.

Recommended options:

### Option A: Keep Internal Implementation Dependencies

```text
com.runestone.expeval.api.ExpressionEngine
  -> internal runtime/cache implementation
```

Use this if the module is not trying to enforce a strict Java Platform Module System boundary yet.

Pros:

- Smallest step.
- No new abstraction.
- No public compiler or runtime surface.

Cons:

- Public facade implementation still imports `internal` packages.

### Option B: Internal Facade Behind Public API

```text
com.runestone.expeval.api.ExpressionEngine
  -> internal.engine.ExpressionEngineRuntime
  -> internal compiler/runtime packages
```

Use this if you want a clearer seam between public API classes and compiler/runtime implementation without exposing compiler customization to consumers.

Pros:

- Public facade classes depend on one internal bridge instead of many runtime implementation types.
- Public surface stays small.

Cons:

- Slightly more abstraction.
- Only worth it if strict package/API isolation matters.

Recommended: Option A for now. Do not promote `ExpressionCompiler` or runtime services unless there is a concrete external customization requirement.

## Proposed Package Target

Recommended long-term structure:

```text
com.runestone.expeval.api
  MathExpression
  LogicalExpression
  AssignmentExpression
  ValidationResult
  CompilationIssue
  public exceptions/audit types

com.runestone.expeval.compiler, optional only if external compiler customization becomes required
  ExpressionCompiler
  CompilerOptions

com.runestone.expeval.environment
com.runestone.expeval.catalog
com.runestone.expeval.catalog.functions
com.runestone.expeval.types

com.runestone.expeval.internal.grammar
  ANTLR generated types
  Parser facade

com.runestone.expeval.internal.ast
  AST node records
  AST validation

com.runestone.expeval.internal.ast.mapping
  SemanticAstBuilder
  unchanged for this refactoring scope

com.runestone.expeval.internal.compiler
  CompilationPipeline
  CompiledExpression
  CompilationCacheKey
  ExpressionCompilationCache, if kept as the compiled-runtime cache boundary

com.runestone.expeval.internal.semantic
  SemanticResolver
  SemanticModel
  SemanticIssue
  ResolutionContext
  SymbolRef
  SymbolKind
  ResolvedFunctionBinding
  SymbolScopeResolver
  CallableTypeResolver
  NavigationTypeResolver

com.runestone.expeval.internal.execution.plan
  ExecutionPlanBuilder
  ExecutionPlan
  ExecutableNode
  Executable*
  BindingPlanBuilder
  ExecutableExpressionBuilder
  NavigationPlanBuilder
  ConstantFoldingSupport

com.runestone.expeval.internal.execution.eval
  MathEvaluator
  LogicalEvaluator
  AbstractObjectEvaluator
  OperatorEvaluator
  ExecutionScope
  NodeEvaluator

com.runestone.expeval.internal.navigation
  NavigationMode
  MapProjectionKind
  VectorAggregationKind
  PropertyChainOps
  CollectionNavigationOps
  ExecutablePropertyChain, if treated as navigation-specific executable plan
  FilterContext
  FilterContextStack
  DeepScanContext
  ReflectiveAccessCache
  TypeIntrospectionSupport

com.runestone.expeval.internal.runtime
  RuntimeServices
  RuntimeCoercionService
  DynamicInstant
  RuntimeInvocationSupport

com.runestone.expeval.internal.audit
  AuditCollector
```

The exact package names matter less than the direction: compiler orchestration, semantic resolution, execution plan, evaluation, runtime services, navigation, and audit should not all live in one package.

`RuntimeServices` and `RuntimeCoercionService` must remain implementation details. They can move to a narrower runtime/coercion package later, but they should stay package-private and should not be exposed from `ExpressionEnvironment` or public expression facades.

## Completed Runtime Boundary Refactor

Implemented changes:

- Removed `ExpressionEnvironment.runtimeServices()`.
- Removed the `RuntimeServices` field from `ExpressionEnvironment`.
- Made `RuntimeServices` package-private.
- Made `RuntimeCoercionService` constructor package-private; the class was already package-private.
- Added `ExpressionCompilationCache.compileRuntime(...)`.
- Changed `ExpressionCompilationCache` to cache `ExpressionRuntimeSupport` instead of only `CompiledExpression`.
- Changed `ExpressionEngine` to use the cached runtime support directly.
- Kept `ExpressionCompilationCache.compile(...)` returning `CompiledExpression` for internal compatibility.

Performance result:

- JMH benchmark: `CompilePathAllocationBenchmark`.
- Recorded in `expression-evaluator/docs/perf/performance-history.md` as `PERF-002`.
- Cache-hit allocation dropped from `160 B/op` to approximately `0 B/op`.
- Cache-hit time improved by about `82%` for both simple and function-heavy compile cache hits.
- Cache-miss time and allocation stayed effectively unchanged.

Follow-up guardrail:

- Do not recreate `ExpressionRuntimeSupport.from(compiled, environment)` in the public compile path after a cache hit.
- Prefer `ExpressionCompilationCache.compileRuntime(...)` whenever the caller needs an evaluable expression runtime.
- Keep `RuntimeServices` ownership inside the cache miss path so runtime/coercion services are built once per cached runtime support instance.

## `SemanticResolver` Responsibility Split

Current responsibilities include:

- Collect internal symbols from assignments.
- Resolve assignment targets and values.
- Infer literal types, including date/time parsing.
- Resolve identifiers and external symbols.
- Resolve current element symbol `@`.
- Resolve property chains and navigation modes.
- Resolve collection function calls.
- Resolve method/property overloads.
- Resolve normal function overloads.
- Validate unary, binary, ternary, postfix, regex, and coalesce operators.
- Validate final expression result type.

Suggested extraction:

```text
SemanticResolver
  - orchestrates the semantic pass

SemanticSession
  - owns mutable maps/issues during one resolution

SymbolScopeResolver
  - assignments, external symbols, identifiers, current element, and result symbol lookup

CallableTypeResolver
  - catalog lookup, overload disambiguation, callable result types, and operator-family type rules

NavigationTypeResolver
  - property chains, member calls, collection/map navigation, deep scan, filters, and current element scoping inside navigation
```

Keep literal inference and individual operator branches as private methods inside these collaborators at first. Extract them only if they become independently testable responsibilities with repeated use or complex state.

This should be done by extracting private methods into package-private `final` classes one responsibility group at a time. Avoid changing behavior while moving logic.

## `ExecutionPlanBuilder` Responsibility Split

Current responsibilities include:

- Assign deterministic indexes to symbols.
- Seed defaults for external symbols.
- Build external binding plans.
- Estimate audit events.
- Build executable assignments.
- Build executable expression nodes.
- Build property-chain executable steps.
- Detect and fold constant expressions.
- Decide whether navigation is foldable.
- Materialize literal values.
- Evaluate constant subtrees.

Suggested extraction:

```text
ExecutionPlanBuilder
  - orchestrates plan building

BindingPlanBuilder
  - deterministic symbol index assignment, external defaults, external binding plans, and audit event capacity estimates

ExecutableExpressionBuilder
  - AST/semantic node -> executable node for scalar, logical, conditional, vector, assignment, and callable expressions

NavigationPlanBuilder
  - property-chain AST/resolved navigation -> executable navigation, including collection/map/deep-scan steps

ConstantFoldingSupport
  - foldability rules, literal materialization, and execution of foldable executable subtrees, ideally sharing runtime evaluator logic where it does not add overhead
```

First extraction candidate: `BindingPlanBuilder`. It has a clear responsibility boundary and can absorb symbol index allocation without creating a tiny allocator class that is only meaningful inside the builder.

## `SemanticAstBuilder` Scope Exclusion

Current responsibilities include:

- Build math input file.
- Build logical input file.
- Build assignment input file.
- Build simple assignment and destructuring assignment nodes.
- Visit expression parse contexts.
- Map operators from grammar tokens to AST operators.
- Build literal nodes.
- Build vector literals.
- Build property chains.
- Build filters and current element references.
- Build collection function/lambda-like structures.
- Build source spans and node IDs.

Do not change `SemanticAstBuilder` as part of this refactoring. Although it has multiple responsibilities, it is tightly coupled to the grammar and parse-tree shape. Changing it while also moving semantic, plan, runtime, and navigation boundaries would make regressions harder to isolate.

Deferred extraction, only after the rest of the package boundaries are stable:

```text
SemanticAstBuilder
  - public entrypoint and orchestration

AssignmentAstMapper
  - assignment parse contexts

ExpressionAstMapper
  - math/logical expression parse contexts

NavigationAstMapper
  - property chain, wildcard, slice, deep scan, map projection, aggregation, filter expressions, and current element references

LiteralAndOperatorMapper
  - string, number, boolean, date/time/datetime literals and token/context -> AST operator enum

AstNodeFactory
  - node IDs, parser context/token -> SourceSpan, and common construction helpers
```

Until that later phase, keep `SemanticAstBuilder` exactly as it is. Do not rename, split, move, or opportunistically clean it while performing the current refactoring.

## Navigation Concerns

Navigation currently crosses many files:

- Grammar rules.
- `SemanticAstBuilder`.
- `PropertyChainNode`.
- `SemanticResolver`.
- `ExecutionPlanBuilder`.
- `ExecutablePropertyChain`.
- `PropertyChainOps`.
- `CollectionNavigationOps`.

Some duplication is expected because AST, semantic model, and executable plan are different layers. The current problem is that rules such as legacy-only chain classification and current element handling are repeated in multiple phases.

Recommended improvements:

- Add `LanguageSymbols.CURRENT_ELEMENT` for `@`.
- Add `NavigationStepClassifier` for legacy/new navigation classification.
- Consider `ResolvedNavigationChain` as an intermediate semantic result.
- Move runtime navigation operations into `internal.navigation` or `internal.execution.navigation`.

Do not split navigation into one class per step type at first. The useful boundary is semantic navigation typing, executable navigation planning, and runtime navigation execution. Step-specific branches can stay inside those three responsibilities until their behavior becomes large enough to justify dedicated collaborators.

## Runtime Invocation Duplication

Invocation concerns appear in:

- `FunctionDescriptor`: stores method handle/invokers and exposes arity-specific invocation.
- `AbstractObjectEvaluator`: evaluates/coerces arguments and invokes functions.
- `PropertyChainOps`: invokes method handles for property-chain method calls.
- `CollectionNavigationOps`: applies collection functions with array-style arguments.

Recommended helper:

```text
RuntimeInvocationSupport
  evaluateArguments(...)
  coerceArguments(...)
  invokeFunctionDescriptor(...)
  invokeMethodHandle(...)
  invokeMethodHandleWithReceiver(...)
```

Keep optimized arity paths if they are important, but put them in one helper. That avoids maintaining arity 0-6 logic in several places.

Do not split invocation into one class per arity. Keep arity-specific fast paths in one implementation so performance-sensitive behavior remains easy to audit.

## Performance Guardrails

These refactorings are primarily structural. They should preserve the runtime performance profile unless a deliberate, benchmark-backed optimization is being made.

- Package moves must be behavior-only refactors and should not change allocation patterns.
- Refactoring must not introduce new per-node, per-expression, per-argument, or per-evaluation helper object allocations unless there is a measured reason.
- Extracted collaborators should be reused through constructor-injected fields, static stateless instances, or existing runtime services instead of being instantiated inside recursive visitors, builders, evaluators, navigation loops, or function invocation paths.
- Evaluation hot paths must not introduce per-node policy dispatch, reflection discovery, generic varargs, or unnecessary `Object[]` allocation.
- Keep arity-specific invocation fast paths for common arities 0 through 6.
- Centralized invocation support must not replace specialized method-handle paths with generic reflection or varargs-based dispatch.
- Reflection/type metadata discovery must remain cached by type and policy, and must not run repeatedly during evaluation.
- Intermediate semantic/navigation models such as `ResolvedNavigationChain` may be introduced during compilation/planning only, not during evaluation.
- Constant folding must avoid re-running the full runtime path when direct literal/operator folding is sufficient.
- Shared evaluator/interpreter logic must not add extra checks, callbacks, policy lookups, or audit hooks to the normal evaluation loop.
- Capture a performance baseline before structural refactors and compare after major extractions.

Recommended baseline coverage:

- Expression compilation with cache hit and cache miss scenarios.
- Simple scalar math evaluation.
- Logical short-circuit evaluation.
- Function invocation for arities 0 through 6.
- Property-chain navigation.
- Collection navigation, map projection, and deep scan.
- Constant folding during compilation.
- Allocation profile for navigation-heavy and function-heavy expressions.

## Recommended Refactoring Order

1. Capture a performance baseline for compilation, evaluation, invocation, navigation, constant folding, and allocation before structural changes.
2. Add characterization tests around engine cache behavior, environment/runtime boundaries, navigation, constant folding, and function invocation before moving package boundaries.
3. Keep `ExpressionCompiler` internal unless a concrete external compiler customization requirement appears.
4. Done: hide `ExpressionEnvironment.runtimeServices()` and keep `RuntimeServices` / `RuntimeCoercionService` internal.
5. Done: cache `ExpressionRuntimeSupport` to preserve runtime-service lifetime and avoid cache-hit allocations.
6. Create new internal packages without moving all classes at once.
7. Move semantic model classes from `internal.runtime` to `internal.semantic`: `SemanticModel`, `SemanticIssue`, `SemanticIssueSeverity`, `ResolutionContext`, `ResolvedFunctionBinding`, `SymbolRef`, `SymbolKind`.
8. Move execution plan records/classes to `internal.execution.plan`: `ExecutionPlan`, `ExecutableNode`, `Executable*`, `ExecutionPlanBuilder`.
9. Move evaluators to `internal.execution.eval`: `MathEvaluator`, `LogicalEvaluator`, `AbstractObjectEvaluator`, `OperatorEvaluator`, `ExecutionScope`, `NodeEvaluator`.
10. Move navigation execution classes to navigation-focused package: `PropertyChainOps`, `CollectionNavigationOps`, `FilterContextStack`, `DeepScanContext`.
11. Add `LanguageSymbols.CURRENT_ELEMENT` and replace scattered `"@"` checks.
12. Extract cohesive collaborators from `ExecutionPlanBuilder`, starting with `BindingPlanBuilder` and then `ExecutableExpressionBuilder`.
13. Compare performance against the baseline after each extraction that touches evaluation, invocation, navigation, coercion, reflection, or folding.
14. Extract cohesive collaborators from `SemanticResolver`, starting with `SymbolScopeResolver`, then `CallableTypeResolver` and `NavigationTypeResolver`.
15. Leave `SemanticAstBuilder` unchanged. Revisit it only in a separate refactoring after semantic, execution plan, evaluation, runtime, and navigation boundaries are stable.

## Suggested Tests

- Public API compatibility tests for `MathExpression`, `LogicalExpression`, `AssignmentExpression`, and `ExpressionEngine` compile/validate entrypoints.
- Tests ensuring compiler customization is not exposed accidentally unless a deliberate public compiler contract is introduced later.
- Cache tests ensuring `ExpressionCompilationCache.compileRuntime(...)` reuses the same `ExpressionRuntimeSupport` for identical source, result type, and environment ID.
- Cache tests ensuring different sources, environment IDs, external default values, and conversion-service instances do not share cached runtime support.
- Semantic resolver tests grouped by symbol resolution, function overload, operator typing, and navigation typing.
- Plan builder tests for deterministic symbol indexing and external default binding.
- Constant folding tests comparing folded and non-folded evaluation results.
- Navigation tests for legacy property chain, current element `@`, collection filters, map projection, deep scan, vector aggregation.
- Invocation tests covering arities 0 through 6 and varargs/array-like behavior if supported.
- JMH benchmarks for expression compilation cache hit/miss, simple math evaluation, logical short-circuit evaluation, function invocation arities 0 through 6, property-chain navigation, collection navigation, and constant folding.
- Allocation-focused benchmarks for runtime evaluation and navigation-heavy expressions.

## Risk Notes

- Promoting `ExpressionCompiler` would create a new public contract. Keep it internal unless consumers need explicit compiler customization.
- Re-exposing `RuntimeServices` or `RuntimeCoercionService` would reopen the public/internal boundary leak that was already fixed.
- Caching `ExpressionRuntimeSupport` means cached runtime support must remain immutable and thread-safe. Do not add mutable per-evaluation state to it.
- Moving many package-private classes can expose hidden dependency cycles. Do the move in small commits/steps.
- Constant folding and runtime evaluation must remain behaviorally identical. Extracting a shared evaluator is valuable but should be backed by tests.
- Navigation is the riskiest area because one language feature touches grammar, AST, semantic resolution, plan building, and runtime evaluation.
- Avoid introducing too many generic helpers. Prefer phase-specific collaborators with clear names over a large `ExpressionUtils` class.
