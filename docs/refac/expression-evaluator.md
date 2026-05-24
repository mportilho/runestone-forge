# Refactoring Analysis: expression-evaluator

## Module Summary

`expression-evaluator` is the most complex module in the repository. It implements an expression language with a compilation pipeline:

1. Parse source text with ANTLR.
2. Build semantic AST nodes.
3. Resolve symbols, types, functions, operators, and navigation semantics.
4. Build an execution plan.
5. Evaluate math, logical, and assignment expressions.

The conceptual pipeline is sound, but the package structure does not fully reflect it. Most classes after AST construction historically lived under `com.runestone.expeval.internal.runtime`, which became a broad package containing compiler, semantic resolver, execution plan, evaluators, coercion services, navigation operations, and audit support. The current refactor is splitting that package by phase.

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
- Do not refactor `SemanticAstBuilder`; it is considered acceptable as-is and should stay out of this refactoring plan.

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

Current direction for runtime evaluation:

```text
AbstractObjectEvaluator / ConstantNodeEvaluator
  - dispatch evaluator-specific node cases
  - delegate shared structured expression behavior

StructuredExpressionEvaluator
  - owns conditional, simple conditional, unary, binary, ternary, postfix, regex, null-coalesce, and vector evaluation
  - is shared by normal runtime evaluation and constant-folding evaluation
  - preserves runtime null-error wrapping while allowing constant folding to keep its internal exception behavior
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
- Clarity: mostly clear after the package split.
- Main package smell: `internal.runtime` has been reduced to runtime support/coercion.
- Main boundary status: the former public compiler bridge has been removed; `ExpressionEngine` is now the public cache/configuration handle and compiled-plan types remain internal.
- Main duplication hotspots: runtime invocation. Constant folding/runtime structured evaluation, navigation classification, language symbol `@`, source pointer formatting, and type metadata discovery policies have already been centralized.
- Refactoring style risk: extracting very small helpers can move repetition instead of removing it. Prefer larger cohesive collaborators when rules share context and lifecycle.

## Detailed Findings

| Severity | Issue | Evidence | Recommendation |
|---|---|---|---|
| Addressed | Public compiler bridge exposed internals | The public `ExpressionCompiler` and `ExpressionCompilerAccess` types were removed. Public cache/configuration now goes through `com.runestone.expeval.api.ExpressionEngine`, while `DefaultExpressionCompiler` and `CompiledExpression` remain under `internal.compiler`. White-box tests still centralize internal compilation through `src/test/java/com/runestone/expeval/testing/ExpressionCompilerInspector`. | Keep `ExpressionEngine` as the public stateful API. Do not expose `ExpressionResultType`, `CompiledExpression`, `SemanticModel`, or `ExecutionPlan` unless a stable public compiled-handle API is intentionally designed. |
| Addressed | Public environment exposed internal runtime services | `ExpressionEnvironment` imported, stored, and exposed `RuntimeServices` from `internal.runtime`. | `RuntimeServices` is now created internally from `ExpressionEnvironment`; the public environment API no longer exposes runtime implementation details. It remains public only inside the `internal` namespace so split packages can share it. |
| Addressed | `internal.runtime` mixed too many phases | Compiler implementation now lives in `internal.compiler`, semantic resolution/model classes now live in `internal.semantic`, `AuditCollector` now lives in `internal.audit`, execution-plan model/planning/folding classes now live in `internal.execution.plan`, evaluation now lives in `internal.execution.eval`, and navigation execution now lives in `internal.navigation`. `internal.runtime` is now limited to `ExpressionRuntimeSupport`, `RuntimeServices`, and `RuntimeCoercionService`. | Keep runtime support/coercion stable unless a clear boundary appears. Avoid moving `ExpressionRuntimeSupport` unless the public facade imports are intentionally changed. |
| Addressed | `SemanticResolver` had too many reasons to change | The baseline class covered assignment symbols, literal type inference, identifiers, `@`, function overloads, property/member navigation, collection functions, operators, and result type validation. | It is now a semantic-pass orchestrator delegating to cohesive collaborators such as `LiteralTypeInferencer`, `SemanticSymbolResolver`, `FunctionOverloadResolver`, `OperatorTypeChecker`, `ResultTypeValidator`, and `NavigationTypeResolver`. |
| Addressed | `ExecutionPlanBuilder` mixed plan construction with folding/evaluation concerns | The baseline builder covered symbol indexing, defaults, external binding plans, audit event estimation, node building, navigation building, foldability rules, literal materialization, and constant folding evaluator setup. | It is now an orchestrator delegating to planning collaborators such as `SymbolIndexAllocator`, `ExternalBindingPlanner`, `AuditEventEstimator`, `LiteralMaterializer`, `ExecutableNodeBuilder`, property-chain planners, and folding support. |
| Accepted | `SemanticAstBuilder` stays as-is | 1423-line parse mapping class; despite its size, the current structure is considered acceptable for this project. | Do not split or refactor `SemanticAstBuilder` as part of this plan. |
| Addressed | Structured evaluation logic was duplicated between runtime and constant folding | Runtime evaluation and constant folding both handled binary short-circuit, conditionals, vectors, regex, postfix/unary/ternary, and null coalescing. | `StructuredExpressionEvaluator` now owns shared structured/operator node evaluation for both paths. Function calls intentionally remain evaluator-specific because runtime audit and fold-time behavior differ. |
| Medium | Runtime invocation by arity appears in multiple places | Invocation logic exists in `FunctionDescriptor`, function-call evaluators, and `PropertyAccessEvaluator`. | Avoid extracting this if it adds objects or argument-array allocation on hot paths. Any future extraction must preserve zero-extra-object invocation paths for arities 0-6. |
| Addressed | Main navigation code is not in `internal.navigation` | `internal.navigation` now contains navigation enums/cache/introspection plus runtime navigation execution: `PropertyChainOps`, `CollectionAccessOps`, `CollectionPredicateTransformEvaluator`, `CollectionFunctionEvaluator`, `VectorAggregationEvaluator`, `PropertyAccessEvaluator`, `DeepScanEvaluator`, and `DeepScanContext`. `ExecutablePropertyChain` remains with the execution-plan model in `internal.execution.plan`; `FilterContextStack` remains in `internal.execution.eval`. | Keep navigation execution together unless a future split is backed by a clearer runtime boundary. |
| Addressed | Sentinel `@` was represented as repeated string literal | Language-level current-element references now use `LanguageSymbols.CURRENT_ELEMENT`. Remaining raw `"@"` strings are environment-id/debug fingerprints, not language-symbol checks. | No further action unless new language-symbol literals are introduced. |
| Addressed | Type introspection policy was split | `ExpressionEnvironmentBuilder` discovered type hints via public methods/fields while runtime fallback walked declared members and hierarchy for reflective access. | `TypeMetadataDiscoverer` now centralizes both policies: public-only metadata for registered type hints and declared-member handles for runtime fallback. Runtime navigation reads cached handles directly from package-private `ReflectiveAccessCache`. |
| Accepted | `FunctionDescriptor` keeps invocation mechanics | It stores catalog metadata and implements arity-specific invocation directly. | Keep this in place for now: avoiding an extra object on the function hot path is worth the small organizational trade-off. |
| Low-medium | AST and executable navigation models are parallel | `PropertyChainNode` and `ExecutablePropertyChain` have corresponding step types. Some duplication is expected, but every new navigation feature touches many places. | Consider `ResolvedNavigationChain` as an intermediate semantic model between AST and executable plan. |
| Addressed | Source pointer formatting repeated across exceptions | Compilation, semantic, validation, and evaluation messages formatted source snippets and carets independently. | `SourcePointerFormatter` now centralizes source-line, caret-span, issue code, position, and message formatting. |

## Public/Internal Boundary Status

The public API now exposes expression facades plus `ExpressionEngine` for cache/configuration lifecycle. Low-level compiled-plan types are internal.

Resolved boundary issues:

```text
com.runestone.expeval.api.MathExpression / LogicalExpression / AssignmentExpression
  -> com.runestone.expeval.api.ExpressionEngine, when isolated cache/configuration is needed

com.runestone.expeval.environment.ExpressionEnvironment
  no longer exposes RuntimeServices

com.runestone.expeval.internal.compiler.CompiledExpression
  remains internal and is only used by runtime/test-support internals
```

`ExpressionEngine` owns the public stateful cache lifecycle without exposing the compiled representation.

Current test strategy:

```text
src/test/java/com/runestone/expeval/testing/ExpressionCompilerInspector
  -> centralizes white-box compilation for AST/SemanticModel/ExecutionPlan assertions
```

Repository tests should use `ExpressionCompilerInspector` when they need `CompiledExpression`, `SemanticModel`, or `ExecutionPlan` internals. This keeps the current test capability while making the white-box dependency explicit.

Rejected option: make low-level compilation public.

```text
com.runestone.expeval.api.ExpressionEngine
  compile(String, ExpressionTarget, ExpressionEnvironment) -> CompiledExpressionHandle
```

Use this if external callers should compile once and operate on a lower-level compiled representation without using `MathExpression`, `LogicalExpression`, or `AssignmentExpression`.

Pros:

- Fully explicit public compile API.
- Enables advanced consumers without reaching into `api` expression facades.

Cons:

- Requires designing and supporting public replacements for `ExpressionResultType` and `CompiledExpression`.
- Larger API commitment.

Current decision: do not expose a public compiled-handle API. The existing public expression facades cover normal usage, while `ExpressionEngine` covers isolated cache/configuration lifecycle.

## Proposed Package Target

Recommended long-term structure:

```text
com.runestone.expeval.api
  ExpressionEngine
  MathExpression
  LogicalExpression
  AssignmentExpression
  ValidationResult
  CompilationIssue
  public exceptions/audit types

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
  DefaultExpressionCompiler
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
  SymbolIndexAllocator
  ResolvedFunctionBinding
  SymbolResolver
  FunctionOverloadResolver
  OperatorTypeChecker
  NavigationTypeResolver
  NavigationStepClassifier

com.runestone.expeval.internal.execution.plan
  ExecutionPlan
  ExecutableNode
  Executable*
  ExternalBindingPlan
  DynamicInstant
  ExecutionPlanBuilder
  ExecutableNodeBuilder
  ExternalBindingPlanner
  AuditEventEstimator
  FoldabilityAnalyzer
  ConstantNodeValues
  ConstantNodeEvaluator
  LiteralMaterializer
  FunctionCallPlanner
  OperatorNodePlanner
  PropertyChainPlanner and access/root planners

com.runestone.expeval.internal.execution.eval
  OperatorEvaluator
  ExecutionScope
  NodeEvaluator
  StructuredExpressionEvaluator
  FilterContextStack
  Evaluator
  MathEvaluator
  LogicalEvaluator
  AbstractObjectEvaluator
  AssignmentEvaluator
  FunctionCallEvaluator
  SymbolValueEvaluator

com.runestone.expeval.internal.navigation
  NavigationMode
  MapProjectionKind
  VectorAggregationKind
  PropertyChainOps
  CollectionAccessOps
  CollectionPredicateTransformEvaluator
  CollectionFunctionEvaluator
  VectorAggregationEvaluator
  PropertyAccessEvaluator
  DeepScanEvaluator
  FilterContext
  DeepScanContext
  ReflectiveAccessCache
  TypeMetadataDiscoverer

com.runestone.expeval.internal.runtime
  ExpressionRuntimeSupport
  RuntimeServices
  RuntimeCoercionService
  RuntimeInvocationSupport, only if a future implementation preserves hot-path fast paths without extra allocation/indirection

com.runestone.expeval.internal.audit
  AuditCollector
```

The exact package names matter less than the direction: compiler orchestration, semantic resolution, execution plan, evaluation, runtime services, navigation, and audit should not all live in one package.

Compiler/evaluation/navigation split note: the execution-plan model and planning/folding collaborators now live in `internal.execution.plan`; runtime evaluation helpers and evaluators live in `internal.execution.eval`; `DefaultExpressionCompiler` and `CompiledExpression` live in `internal.compiler`; navigation execution lives in `internal.navigation`. The remaining intentional coupling is that constant folding still calls `PropertyChainOps.evaluatePropertyChain(...)` for property-chain prefix folding, now through the navigation package boundary.

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

## `SemanticAstBuilder` Decision

Decision: do not refactor `SemanticAstBuilder`. Its current structure is accepted as-is, and future refactoring rounds should not propose mapper splits for this class unless this decision is explicitly revisited.

Current responsibilities remain documented for context only:

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

Rejected extraction proposal:

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

Keep `SemanticAstBuilder` as the public class used by the parser facade. Do not move internals behind package-private mapper collaborators under the current plan.

## Navigation Concerns

Navigation currently crosses many files:

- Grammar rules.
- `SemanticAstBuilder`.
- `PropertyChainNode`.
- `SemanticResolver`.
- `ExecutionPlanBuilder`.
- `ExecutablePropertyChain`.
- `PropertyChainOps`.
- `CollectionAccessOps`.
- `CollectionPredicateTransformEvaluator`.
- `CollectionFunctionEvaluator`.
- `VectorAggregationEvaluator`.

Some duplication is expected because AST, semantic model, and executable plan are different layers. The current problem is that rules such as legacy-only chain classification and current element handling are repeated in multiple phases.

Recommended improvements:

- `LanguageSymbols.CURRENT_ELEMENT` for `@` is in place.
- `NavigationStepClassifier` for legacy/new navigation classification is in place.
- Keep semantic navigation typing together in `NavigationTypeResolver`; avoid extracting typed member lookup or collection-function matching into separate classes unless reused outside navigation.
- Consider `ResolvedNavigationChain` as an intermediate semantic result.
- Runtime navigation operations now live in `internal.navigation`.

## Runtime Invocation Duplication

Invocation concerns appear in:

- `FunctionDescriptor`: stores method handle/invokers and exposes arity-specific invocation.
- `AbstractObjectEvaluator`: evaluates/coerces arguments and invokes functions.
- `PropertyChainOps`: invokes method handles for property-chain method calls.
- `CollectionFunctionEvaluator`: applies collection functions with array-style arguments.

Current decision: keep `FunctionDescriptor` invocation mechanics in place for now. Extracting them into a separate object adds allocation/indirection on a hot path for a small organizational gain. Any future invocation extraction must prove it preserves arity-specific fast paths without adding per-descriptor or per-call overhead that matters.

Do not implement the previously proposed generic helper as-is:

```text
RuntimeInvocationSupport
  evaluateArguments(...)
  coerceArguments(...)
  invokeFunctionDescriptor(...)
  invokeMethodHandle(...)
  invokeMethodHandleWithReceiver(...)
```

Centralizing all invocation mechanics is only acceptable if it preserves optimized arity paths and does not add objects, generic varargs, or argument-array allocation to function or typed-navigation hot paths. Until that is proven, keep `FunctionDescriptor` invocation in place and limit any future cleanup to local duplication that does not change allocation behavior.

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

1. Done: add characterization tests around custom engine injection, environment runtime access, navigation, constant folding, and function invocation before moving public/internal boundaries.
2. Done: replace the public `ExpressionCompiler` wrapper with `ExpressionEngine` as the public cache/configuration handle.
3. Done: hide `ExpressionEnvironment.runtimeServices()` and keep `RuntimeServices` internal.
4. Done: add `LanguageSymbols.CURRENT_ELEMENT` and replace language-symbol `"@"` checks.
5. Done: split oversized classes by cohesive responsibility while keeping current packages stable.
6. Done: for `ExecutionPlanBuilder`, keep the builder as orchestrator and extract cohesive planning roles, not one class per branch.
7. Done: for `SemanticResolver`, keep it as semantic-pass orchestrator and extract responsibility clusters: symbols, literals, normal function overloads, operators, result validation, and navigation typing.
8. Done: keep semantic navigation typing in one `NavigationTypeResolver` unless a rule becomes truly shared outside navigation.
9. Done: add `ExpressionCompilerInspector` in test sources and centralize white-box compilation through `DefaultExpressionCompiler`.
10. Done: remove `ExpressionCompilerAccess`; runtime and tests now use `DefaultExpressionCompiler` directly inside the internal namespace.
11. Done: keep `ExpressionResultType`, `CompiledExpression`, `SemanticModel`, and `ExecutionPlan` out of the public API.
12. Done: create the first internal package split after the responsibility split stabilized.
13. Done: move semantic model/classes from `internal.runtime` to `internal.semantic`: `SemanticModel`, `SemanticIssue`, `SemanticIssueSeverity`, `ResolutionContext`, `ResolvedFunctionBinding`, `SymbolRef`, `SymbolKind`, `SemanticResolver`, `NavigationStepClassifier`, `SymbolIndexAllocator`, and semantic collaborators.
14. Done: move `AuditCollector` from `internal.runtime` to `internal.audit`.
15. Done: move the execution-plan model to `internal.execution.plan`: `ExecutionPlan`, `ExecutableNode`, `Executable*`, `ExecutableAssignment`, `ExternalBindingPlan`, and `DynamicInstant`. The sealed executable-node family was moved together.
16. Done: move evaluation to `internal.execution.eval`: `ExecutionScope`, `NodeEvaluator`, `OperatorEvaluator`, `StructuredExpressionEvaluator`, `FilterContextStack`, `Evaluator`, `MathEvaluator`, `LogicalEvaluator`, `AbstractObjectEvaluator`, `AssignmentEvaluator`, `FunctionCallEvaluator`, and `SymbolValueEvaluator`.
17. Done: move execution planning/folding collaborators to `internal.execution.plan`: `ExecutionPlanBuilder`, `ExecutableNodeBuilder`, property-chain planners, `ExternalBindingPlanner`, `AuditEventEstimator`, `LiteralMaterializer`, `FoldabilityAnalyzer`, `ConstantNodeValues`, `ConstantNodeEvaluator`, `FunctionCallPlanner`, and `OperatorNodePlanner`.
18. Done: move compiler implementation classes to `internal.compiler`: `DefaultExpressionCompiler` and `CompiledExpression`.
19. Done: move navigation execution classes to `internal.navigation`: `PropertyChainOps`, `CollectionAccessOps`, `CollectionPredicateTransformEvaluator`, `CollectionFunctionEvaluator`, `VectorAggregationEvaluator`, `PropertyAccessEvaluator`, `DeepScanEvaluator`, and `DeepScanContext`.
20. Done: centralize type metadata discovery policy in `TypeMetadataDiscoverer`; type hints use public-only metadata and runtime fallback uses declared-member handles.
21. Next: consider a public compiled-handle API only if an external use case appears; do not expose internal compiled-plan types by default.
22. Ongoing: compare performance against the baseline after each extraction that touches evaluation, invocation, navigation, coercion, reflection, or folding.
23. Permanent decision: do not split `SemanticAstBuilder`; it is intentionally excluded from this refactoring plan.

## Suggested Tests

- Public API compatibility tests for `ExpressionEngine` and `MathExpression.compile(..., engine)`, `LogicalExpression.compile(..., engine)`, and `AssignmentExpression.compile(..., engine)`.
- Tests for the chosen public engine contract: facade-only compiled expressions with isolated cache lifecycle, not a public low-level compiled handle.
- Semantic resolver tests grouped by symbol resolution, function overload, operator typing, and navigation typing.
- Plan builder tests for deterministic symbol indexing and external default binding.
- Constant folding tests comparing folded and non-folded evaluation results.
- Navigation tests for legacy property chain, current element `@`, collection filters, map projection, deep scan, vector aggregation.
- Invocation tests covering arities 0 through 6 and varargs/array-like behavior if supported.
- JMH benchmarks for expression compilation cache hit/miss, simple math evaluation, logical short-circuit evaluation, function invocation arities 0 through 6, property-chain navigation, collection navigation, and constant folding.
- Allocation-focused benchmarks for runtime evaluation and navigation-heavy expressions.

## Risk Notes

- `ExpressionEngine` should remain a public cache/configuration handle. Do not add a public method returning `CompiledExpression` unless a stable compiled-handle API is intentionally designed.
- Moving many package-private classes can expose hidden dependency cycles. Do the move in small commits/steps.
- Constant folding and runtime evaluation must remain behaviorally identical. Extracting a shared evaluator is valuable but should be backed by tests.
- Navigation is the riskiest area because one language feature touches grammar, AST, semantic resolution, plan building, and runtime evaluation.
- Avoid introducing too many generic helpers. Prefer phase-specific collaborators with clear names over a large `ExpressionUtils` class.
- Avoid micro-extractions that move repeated control flow into several neighboring files. If a new class needs the same source span, current type, filter scope, function bindings, and type metadata as its caller, it probably belongs inside the same cohesive collaborator.
