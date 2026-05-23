# Refactoring Analysis: expression-evaluator

## Module Summary

`expression-evaluator` is the most complex module in the repository. It implements an expression language with a compilation pipeline:

1. Parse source text with ANTLR.
2. Build semantic AST nodes.
3. Resolve symbols, types, functions, operators, and navigation semantics.
4. Build an execution plan.
5. Evaluate math, logical, and assignment expressions.

The conceptual pipeline is sound, but the package structure does not fully reflect it. Most classes after AST construction currently live under `com.runestone.expeval.internal.runtime`, which has become a broad package containing compiler, semantic resolver, execution plan, evaluators, coercion services, navigation operations, and audit support.

## Refactoring Direction

This refactor should favor cohesive phase-level collaborators over many narrow helper classes.

Good extraction candidates own a complete decision area, such as semantic navigation, executable node planning, symbol resolution, overload resolution, or constant folding orchestration. Avoid extracting every repeated private method into a standalone class when the result forces related rules to be understood across several files.

Use these rules for the remaining work:

- Prefer one cohesive collaborator per responsibility cluster.
- Keep tightly coupled rules together even when the class becomes moderately large.
- Extract small utility-like collaborators only when they represent stable shared knowledge, not just similar-looking code.
- Do not split member access, current-element handling, collection function resolution, and navigation typing into separate semantic classes unless a real reuse point appears.
- Do not introduce compatibility wrappers or deprecated aliases unless there is an explicit external compatibility requirement.
- Defer package moves until behavior-preserving responsibility splits are stable and tested.

Current direction for the semantic pass:

```text
SemanticResolver
  - orchestrates the semantic pass
  - owns resolution session state and SemanticModel assembly

NavigationTypeResolver
  - owns property-chain typing end to end
  - owns @ filter scope during navigation predicates
  - owns typed member lookup for navigation
  - owns collection/map/vector navigation typing
  - owns collection-function overload resolution for navigation calls

SemanticSymbolResolver
  - owns assignment symbols, identifiers, and external symbols

FunctionOverloadResolver
  - owns normal function-call overload resolution

OperatorTypeChecker
  - owns operator typing rules

ResultTypeValidator
  - owns final expression result-type validation
```

## Baseline Structure

Approximate package concentration in `src/main/java` before the current refactoring work:

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

Large manual files at the baseline snapshot:

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
- Main boundary smell: public API imports and exposes classes from `internal.runtime`.
- Main duplication hotspots: runtime invocation, constant folding evaluation, navigation classification, string literal `@`, source pointer formatting.
- Refactoring style risk: extracting very small helpers can move repetition instead of removing it. Prefer larger cohesive collaborators when rules share context and lifecycle.

## Detailed Findings

| Severity | Issue | Evidence | Recommendation |
|---|---|---|---|
| High | Public API exposes internal compiler type | `MathExpression.java:5` imports `com.runestone.expeval.internal.runtime.ExpressionCompiler`; `MathExpression.java:80` exposes it in a public method. Similar pattern exists in logical and assignment expression facades. | Move `ExpressionCompiler` to a public package such as `com.runestone.expeval.compiler`, or create a public interface/facade and keep implementation internal. |
| High | Public environment exposes internal runtime services | `ExpressionEnvironment.java:7` imports `RuntimeServices`; `ExpressionEnvironment.java:21` stores it; `ExpressionEnvironment.java:69` exposes `public RuntimeServices runtimeServices()`. | Make runtime service access internal or expose a narrow public runtime/conversion interface if needed. |
| High | `internal.runtime` mixes too many phases | 47 Java files include `ExpressionCompiler`, `SemanticResolver`, `ExecutionPlanBuilder`, `Executable*`, `MathEvaluator`, `LogicalEvaluator`, `RuntimeCoercionService`, `PropertyChainOps`, `AuditCollector`. | Split by pipeline phase: compiler, semantic, execution plan, execution eval, runtime services, navigation, audit. |
| High | `SemanticResolver` has too many reasons to change | 991-line baseline covering assignment symbols, literal type inference, identifiers, `@`, function overloads, property/member navigation, collection functions, operators, result type validation. | Extract cohesive responsibility clusters. Keep navigation typing together in `NavigationTypeResolver`; keep symbols, normal function overloads, operators, and result validation in separate collaborators. Avoid splitting navigation into tiny helpers that duplicate context. |
| High | `ExecutionPlanBuilder` mixes plan construction with folding/evaluation concerns | 1160-line baseline covering symbol indexing, defaults, external binding plans, audit event estimation, node building, navigation building, foldability rules, literal materialization, constant folding evaluator. | Keep `ExecutionPlanBuilder` as orchestrator and group planning concerns into cohesive collaborators. Use small support classes only for stable standalone invariants such as symbol indexes or literal materialization. |
| Medium-high | `SemanticAstBuilder` is a parse mapping god class | 1423 lines; maps math/logical/assignment, literals, operators, property chains, filters, lambdas, collection functions, spans. | Split by mapping area: assignment, expression, literal, navigation, filter, source span/node factory. |
| Medium-high | Evaluation logic is duplicated between runtime and constant folding | Runtime evaluation lives in `AbstractObjectEvaluator`; folding evaluator lives inside `ExecutionPlanBuilder`. Both handle binary short-circuit, conditionals, vectors, function calls. | Create a reusable `ExecutableNodeInterpreter` or make constant folding use the same evaluator with a restricted `EvaluationPolicy`. |
| Medium | Runtime invocation by arity appears in multiple places | Invocation logic exists in `FunctionDescriptor`, `AbstractObjectEvaluator`, and `PropertyChainOps`. | Extract `RuntimeInvocationSupport` or `MethodHandleInvoker`, preserving fast paths 0-6 in one place. |
| Medium | Main navigation code is not in `internal.navigation` | `internal.navigation` contains enums/cache/introspection, while `PropertyChainOps`, `CollectionNavigationOps`, `ExecutablePropertyChain`, `FilterContextStack`, `DeepScanContext` are in `internal.runtime`. | Move navigation execution/planning classes under navigation or execution-navigation subpackages. |
| Medium | Sentinel `@` is represented as repeated string literal | Search found relevant occurrences in `SemanticAstBuilder`, `SemanticResolver`, `ExecutionPlanBuilder`, `AbstractObjectEvaluator`, and `PropertyChainOps`. | Introduce `LanguageSymbols.CURRENT_ELEMENT` or `SymbolKind.CURRENT_ELEMENT`. Avoid fake external `SymbolRef("@", SymbolKind.EXTERNAL)`. |
| Medium | Type introspection policy is split | `ExpressionEnvironmentBuilder` discovers type hints via public methods/fields; `TypeIntrospectionSupport` walks declared members and hierarchy for reflective access. | Introduce a shared `TypeMetadataDiscoverer` with explicit policies for public API metadata vs runtime fallback. |
| Medium | `FunctionDescriptor` mixes catalog metadata and invocation mechanics | It stores metadata and method handles, and provides arity-specific `invoke` overloads. | Decide whether catalog is runtime-facing. If not, split `FunctionDescriptor` metadata from `FunctionInvoker`. |
| Low-medium | AST and executable navigation models are parallel | `PropertyChainNode` and `ExecutablePropertyChain` have corresponding step types. Some duplication is expected, but every new navigation feature touches many places. | Consider `ResolvedNavigationChain` as an intermediate semantic model between AST and executable plan. |
| Low-medium | Source pointer formatting likely repeats across exceptions | Compilation/parsing/evaluation exceptions format source snippets and carets. | Extract `SourcePointerFormatter` or `CompilationMessageFormatter`. |

## Public/Internal Boundary Problem

The package name `internal.runtime` says implementation detail, but several public APIs expose it.

Current pattern:

```text
com.runestone.expeval.api.MathExpression
  -> com.runestone.expeval.internal.runtime.ExpressionCompiler
  -> com.runestone.expeval.internal.runtime.ExpressionRuntimeSupport
```

This means `ExpressionCompiler` is already part of the public contract in practice. The code should make that explicit.

Recommended options:

### Option A: Promote `ExpressionCompiler`

```text
com.runestone.expeval.compiler.ExpressionCompiler
```

Use this if consumers are expected to inject/configure compilers directly.

Pros:

- Honest API.
- Easier dependency injection.
- Avoids public imports from `internal`.

Cons:

- Requires package migration and compatibility strategy.

### Option B: Public Interface, Internal Implementation

```text
com.runestone.expeval.compiler.ExpressionCompiler
  interface or public facade

com.runestone.expeval.internal.compiler.DefaultExpressionCompiler
  implementation
```

Use this if you want to preserve freedom to change implementation internals.

Pros:

- Stronger boundary.
- Public surface can stay small.

Cons:

- Slightly more abstraction.
- Only worth it if alternative implementations or strict API isolation matter.

Recommended: Option A as the smallest honest step, unless binary compatibility constraints require wrappers.

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

com.runestone.expeval.compiler
  ExpressionCompiler
  CompilerOptions, if needed later

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
  AssignmentAstMapper
  ExpressionAstMapper
  LiteralAstMapper
  NavigationAstMapper
  SourceSpanFactory

com.runestone.expeval.internal.compiler
  CompilationPipeline
  CompiledExpression
  CompilationCacheKey

com.runestone.expeval.internal.semantic
  SemanticResolver
  SemanticModel
  SemanticIssue
  ResolutionContext
  SymbolRef
  SymbolKind
  ResolvedFunctionBinding
  SymbolResolver
  FunctionOverloadResolver
  OperatorTypeChecker
  NavigationTypeResolver

com.runestone.expeval.internal.execution.plan
  ExecutionPlanBuilder
  ExecutionPlan
  ExecutableNode
  Executable*
  ConstantFolder
  LiteralMaterializer

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

## `SemanticResolver` Split

Direction update: do not continue splitting this class by every private method. The semantic pass should be split by responsibility clusters that own their context. Navigation is the clearest example: current-element scope, member lookup, collection/map/vector steps, deep scan, filters, and navigation collection functions should stay together because they form one semantic rule system.

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

Target ownership:

```text
SemanticResolver
  - orchestrates the semantic pass and builds SemanticModel

SemanticSession
  - owns mutable maps/issues during one resolution

SymbolResolver
  - assignments, external symbols, identifiers, current element

LiteralTypeInferencer
  - literal -> ResolvedType

FunctionOverloadResolver
  - catalog lookup and overload disambiguation for normal function calls

NavigationTypeResolver
  - property chain typing
  - current element @ scope while resolving filter predicates
  - typed property/method lookup used by navigation
  - collection/map/vector navigation steps
  - deep scan, filters, vector mapping/aggregation
  - collection-function overload resolution for navigation calls

OperatorTypeChecker
  - unary/binary/ternary/postfix/coalesce/regex rules
```

This should be done one cohesive group at a time. Avoid changing behavior while moving logic. Do not create one-class wrappers for individual operations such as `resolveProperty`, `resolveMethod`, or `resolveCollectionFunction` unless those operations become shared across more than one responsibility cluster. If they are only used by navigation typing, keep them inside `NavigationTypeResolver`.

## `ExecutionPlanBuilder` Split

Direction update: keep the plan builder refactor centered on cohesive planning roles. The goal is not to maximize the number of extracted classes; it is to keep each class at one abstraction level without scattering a single planning rule across many files.

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

Target ownership:

```text
ExecutionPlanBuilder
  - orchestrates plan building

SymbolIndexAllocator
  - deterministic symbol index assignment

ExternalBindingPlanner
  - defaults and binding plans

AuditEventEstimator
  - max audit event count

ExecutableNodeBuilder
  - expression/assignment AST -> executable node
  - delegates only when a sub-area has its own cohesive rule set

NavigationPlanBuilder
  - property-chain AST/resolved navigation -> executable navigation
  - should own related root/member/function planning rules together

LiteralMaterializer
  - AST literal -> runtime literal value

ConstantFolder
  - foldability rules and fold orchestration, if folding logic grows beyond local readability

ConstantExpressionEvaluator
  - execution of foldable executable subtrees, ideally sharing runtime evaluator logic
```

Small collaborators such as `SymbolIndexAllocator`, `ExternalBindingPlanner`, `AuditEventEstimator`, and `LiteralMaterializer` are acceptable because they encode stable standalone invariants. Do not keep extracting tiny classes from planning code when the new class would only wrap one branch and require the reader to jump back to the caller to understand the rule.

## `SemanticAstBuilder` Split

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

Suggested extraction:

```text
SemanticAstBuilder
  - public entrypoint and orchestration

AssignmentAstMapper
  - assignment parse contexts

ExpressionAstMapper
  - math/logical expression parse contexts

LiteralAstMapper
  - string, number, boolean, date/time/datetime literals

NavigationAstMapper
  - property chain, wildcard, slice, deep scan, map projection, aggregation

FilterAstMapper
  - filter expressions and current element references

OperatorMapper
  - token/context -> AST operator enum

SourceSpanFactory
  - parser context/token -> SourceSpan

NodeFactory
  - node IDs and common construction helpers
```

Keep `SemanticAstBuilder` as the public class used by the parser facade. Move internals behind package-private collaborators.

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
- Keep semantic navigation typing together in `NavigationTypeResolver`; avoid extracting typed member lookup or collection-function matching into separate classes unless reused outside navigation.
- Consider `ResolvedNavigationChain` as an intermediate semantic result.
- Move runtime navigation operations into `internal.navigation` or `internal.execution.navigation`.

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

## Performance Guardrails

These refactorings are primarily structural. They should preserve the runtime performance profile unless a deliberate, benchmark-backed optimization is being made.

- Package moves must be behavior-only refactors and should not change allocation patterns.
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
2. Add characterization tests around custom compiler injection, environment runtime access, navigation, constant folding, and function invocation before moving public/internal boundaries.
3. Promote or wrap `ExpressionCompiler` in a public package.
4. Hide or abstract `ExpressionEnvironment.runtimeServices()`.
5. Add `LanguageSymbols.CURRENT_ELEMENT` and replace scattered `"@"` checks.
6. Split oversized classes by cohesive responsibility while keeping current packages stable.
7. For `ExecutionPlanBuilder`, keep the builder as orchestrator and extract cohesive planning roles, not one class per branch.
8. For `SemanticResolver`, keep it as semantic-pass orchestrator and extract responsibility clusters: symbols, literals, normal function overloads, operators, result validation, and navigation typing.
9. Keep semantic navigation typing in one `NavigationTypeResolver` unless a rule becomes truly shared outside navigation.
10. Compare performance against the baseline after each extraction that touches evaluation, invocation, navigation, coercion, reflection, or folding.
11. Create new internal packages only after the responsibility split is stable and tests are green.
12. Move semantic model classes from `internal.runtime` to `internal.semantic`: `SemanticModel`, `SemanticIssue`, `SemanticIssueSeverity`, `ResolutionContext`, `ResolvedFunctionBinding`, `SymbolRef`, `SymbolKind`.
13. Move execution plan records/classes to `internal.execution.plan`: `ExecutionPlan`, `ExecutableNode`, `Executable*`, `ExecutionPlanBuilder`.
14. Move evaluators to `internal.execution.eval`: `MathEvaluator`, `LogicalEvaluator`, `AbstractObjectEvaluator`, `OperatorEvaluator`, `ExecutionScope`, `NodeEvaluator`.
15. Move navigation execution classes to navigation-focused package: `PropertyChainOps`, `CollectionNavigationOps`, `FilterContextStack`, `DeepScanContext`.
16. Split `SemanticAstBuilder` last, because grammar/AST mapping changes tend to be broad and should happen after package boundaries are clearer.

## Suggested Tests

- Public API compatibility tests for `MathExpression.compile(..., compiler)`, `LogicalExpression.compile(..., compiler)`, and `AssignmentExpression.compile(..., compiler)`.
- Tests ensuring old and new compiler package entrypoints behave the same, if a compatibility wrapper is kept.
- Semantic resolver tests grouped by symbol resolution, function overload, operator typing, and navigation typing.
- Plan builder tests for deterministic symbol indexing and external default binding.
- Constant folding tests comparing folded and non-folded evaluation results.
- Navigation tests for legacy property chain, current element `@`, collection filters, map projection, deep scan, vector aggregation.
- Invocation tests covering arities 0 through 6 and varargs/array-like behavior if supported.
- JMH benchmarks for expression compilation cache hit/miss, simple math evaluation, logical short-circuit evaluation, function invocation arities 0 through 6, property-chain navigation, collection navigation, and constant folding.
- Allocation-focused benchmarks for runtime evaluation and navigation-heavy expressions.

## Risk Notes

- Moving `ExpressionCompiler` is potentially breaking because it is already public in method signatures. Use a deprecated delegating class in the old package if compatibility matters.
- Moving many package-private classes can expose hidden dependency cycles. Do the move in small commits/steps.
- Constant folding and runtime evaluation must remain behaviorally identical. Extracting a shared evaluator is valuable but should be backed by tests.
- Navigation is the riskiest area because one language feature touches grammar, AST, semantic resolution, plan building, and runtime evaluation.
- Avoid introducing too many generic helpers. Prefer phase-specific collaborators with clear names over a large `ExpressionUtils` class.
- Avoid micro-extractions that move repeated control flow into several neighboring files. If a new class needs the same source span, current type, filter scope, function bindings, and type metadata as its caller, it probably belongs inside the same cohesive collaborator.
