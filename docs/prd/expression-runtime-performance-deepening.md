# PRD: Expression Runtime Performance Deepening

## Problem Statement

The Expression Runtime must be as fast as possible and generate the least possible GC while remaining testable and AI-navigable. The current runtime already contains several performance-aware choices, including plain Java Runtime Values, array-backed Execution Scope storage, arity-specialized Function Invocation paths, precomputed member handles for Type Hint navigation, and compile-time Constant Folding.

Architectural friction remains in four areas: Function Invocation and Audit Trail policy are split across normal functions and Collection Functions; Member Binding is duplicated between semantic resolution and planning; Constant Folding is embedded inside expression planning; and Collection Navigation behavior is spread across several modules. These areas reduce locality and make it harder to improve runtime speed safely without accidentally adding allocations or dispatch overhead.

## Solution

Deepen the Expression Runtime around performance-preserving seams. The work should concentrate decisions and tests without adding objects to the evaluation hot path.

The recommended delivery order is:

1. Deepen Function Invocation, coercion, and Audit Trail policy while preserving existing arity-specialized fast paths.
2. Promote typed Member Binding decisions into a shared compile-time seam so object member resolution is performed once before Execution Plan construction.
3. Extract Constant Folding policy from expression planning into a compile-time module that can increase plan reduction without increasing per-evaluation cost.
4. Revisit Object Navigation and Collection Navigation only after benchmark baselines exist, because this area has the highest GC and throughput risk.

The design must prioritize zero additional allocation in normal `compute()` evaluation paths. Audit Trail paths may allocate Audit Events, but only when audit is explicitly requested.

## User Stories

1. As a library consumer, I want repeated Math Expression evaluation to stay fast, so that expressions can run safely on request paths.
2. As a library consumer, I want repeated Logical Expression evaluation to stay fast, so that predicates can run safely in filtering and validation paths.
3. As a library consumer, I want Assignment Expression evaluation to avoid unnecessary allocation, so that assignment-heavy expressions do not create GC pressure.
4. As a library consumer, I want Function Invocation to preserve arity-specialized fast paths, so that common function calls do not allocate temporary argument arrays.
5. As a library consumer, I want Collection Function calls to behave consistently with normal function calls, so that the Audit Trail is complete and predictable.
6. As a library consumer using `compute()`, I want Audit Trail logic to be bypassed cheaply, so that audit support does not slow non-audit evaluation.
7. As a library consumer using `computeWithAudit()`, I want every Function Invocation to appear in the Audit Trail, so that function behavior can be inspected after evaluation.
8. As a library consumer using Collection Navigation, I want Collection Function audit behavior to match documented Function Invocation behavior, so that audit output does not depend on syntax shape.
9. As a maintainer, I want Function Invocation policy in one module, so that argument evaluation, coercion, result normalization, and Audit Trail behavior have locality.
10. As a maintainer, I want Function Invocation refactoring to preserve zero-allocation behavior for normal arities when audit is disabled, so that architecture work does not regress throughput.
11. As a maintainer, I want Function Invocation benchmarks before and after changes, so that regressions in throughput and allocation are caught empirically.
12. As a maintainer, I want typed Member Binding resolved once before Execution Plan construction, so that semantic resolution and planning cannot diverge.
13. As a maintainer, I want object member overload decisions to have locality, so that changes to Type Hint behavior are made in one place.
14. As a maintainer, I want Member Binding to remain compile-time only, so that evaluation does not consult semantic maps or allocate binding objects.
15. As a maintainer, I want the Execution Plan to retain only runtime-needed member data, so that compiled expression cache entries do not grow unnecessarily.
16. As a maintainer, I want Constant Folding policy separated from expression planning, so that foldability rules and Fold Barriers are easier to test.
17. As a maintainer, I want Constant Folding to reduce evaluation work, so that deterministic Expression fragments are not repeatedly evaluated.
18. As a maintainer, I want folded Function Invocation to preserve Audit Trail behavior, so that runtime observations remain correct after compile-time reduction.
19. As a maintainer, I want Fold Barriers to remain explicit, so that Constant Folding never captures Dynamic Instants, caller-provided External Symbols, or unsafe runtime failures.
20. As a maintainer, I want expression planning changes to avoid retaining unnecessary objects in the Execution Plan, so that the compiled expression cache does not increase memory pressure.
21. As a maintainer, I want Object Navigation to continue using precomputed handles when Type Hints are present, so that typed access remains faster than reflective access.
22. As a maintainer, I want reflective Object Navigation to remain a fallback, so that dynamic expressions still work without becoming the common hot path.
23. As a maintainer, I want Collection Navigation to avoid per-step context allocation, so that filters, projections, map transforms, and aggregations do not create avoidable GC.
24. As a maintainer, I want Collection Navigation loops to remain explicit loops, so that streams and iterator-heavy abstractions do not increase allocation or dispatch overhead.
25. As a maintainer, I want Navigation Step semantics to become more local without forcing virtual dispatch per step, so that AI-navigability improves without hurting JIT inlining.
26. As a maintainer, I want Current Element and Map Entry Context behavior to be explicit, so that `@`, `@.key`, and `@.value` are easy to reason about and test.
27. As a maintainer, I want Deep Scan behavior to remain allocation-conscious, so that recursive traversal does not allocate scratch structures on every evaluation.
28. As a maintainer, I want any changes to Deep Scan result ownership to be benchmarked, so that semantic safety and GC cost are balanced intentionally.
29. As a maintainer, I want Runtime Value to remain a plain Java value, so that the runtime does not add wrapper allocations around every intermediate result.
30. As a maintainer, I want Execution Scope to remain array-backed, so that symbol lookup does not regress into map lookup during evaluation.
31. As a maintainer, I want coercion to happen at runtime edges, so that Runtime Values are not repeatedly normalized without need.
32. As a maintainer, I want performance-sensitive refactors to include JMH validation, so that architectural depth is accepted only when performance is preserved or improved.
33. As a maintainer, I want tests to exercise public expression behavior where possible, so that implementation deepening does not overfit to current internal shapes.
34. As a maintainer, I want targeted internal tests only where the seam is intentionally internal, so that runtime guarantees such as folding and plan shape remain verifiable.
35. As an AI agent working in the codebase, I want domain terms such as Function Invocation, Member Binding, Collection Navigation, Constant Folding, Fold Barrier, and Audit Trail to be stable, so that future changes are easier to navigate.

## Implementation Decisions

- The first implementation target is Function Invocation, coercion, and Audit Trail policy.
- Function Invocation must remain a performance-focused module, not an object-heavy abstraction.
- The existing arity-specialized Function Invocation paths for common arities must be preserved.
- Collection Function invocation should use the same audit policy as normal Function Invocation.
- Audit Event argument arrays may be allocated only when audit is active.
- Normal `compute()` must not allocate audit argument arrays.
- The Function Invocation module may accept audit as an optional runtime concern, but the audit check must remain cheap.
- Runtime Value must remain a plain Java value. No dedicated Runtime Value wrapper is allowed.
- No per-invocation call context object should be introduced in the evaluation hot path.
- No per-navigation-step context object should be introduced in the evaluation hot path.
- Member Binding should be deepened as a compile-time decision shared by semantic resolution and planning.
- The Execution Plan should carry only the member data needed for evaluation, such as resolved handles, parameter types, return type, and safety semantics.
- Evaluation must not consult the Semantic Model for member lookup.
- Constant Folding should be deepened as compile-time policy, not evaluation-time indirection.
- Fold Barriers must protect Dynamic Instants, caller-overridable External Symbols, unknown values, runtime-only state, and failure timing.
- Constant Folding should reduce Execution Plan work without adding retained objects that increase cache memory pressure.
- Object Navigation and Collection Navigation should not be restructured into virtual dispatch per Navigation Step without benchmark proof.
- Existing loop-based Collection Navigation should remain loop-based unless a benchmark proves an alternative is better.
- Execution Scope must remain array-backed.
- Reflection must remain fallback behavior for Object Navigation without Type Hints.
- Precomputed member handles must remain the preferred path for Object Navigation with Type Hints.
- Coercion should remain edge-oriented: external defaults, external overrides, function results, and typed member results.
- Any architectural deepening must improve locality and leverage without increasing GC in normal evaluation.
- The runtime internals documentation must be updated when behavior changes, especially Audit Trail behavior, overload rules, Constant Folding, or navigation semantics.

## Testing Decisions

- The highest test seam for user-visible behavior is the public Expression interface: Math Expression, Logical Expression, Assignment Expression, and Audit Trail results.
- The Function Invocation seam should be tested through expressions that call normal functions, folded functions, and Collection Functions.
- Collection Function audit behavior needs explicit coverage because current audit behavior is the strongest known documentation/implementation friction.
- Tests should assert that `computeWithAudit()` records Function Invocation events for Collection Functions.
- Tests should assert that ordinary `compute()` behavior remains unchanged for Collection Functions.
- Tests should preserve existing folded Function Invocation audit behavior.
- Member Binding behavior should be tested with Type Hint object navigation, method overloads, arity mismatch, incompatible arguments, and ambiguity.
- Member Binding tests should prefer semantic outcomes and public compilation errors over brittle internal shape assertions.
- Internal Execution Plan tests may remain where they verify intentional runtime guarantees such as Constant Folding, folded audit preservation, and typed Object Navigation planning.
- Constant Folding tests should verify behavior through both computed results and Audit Trail equivalence when folding changes observable event timing.
- Fold Barrier tests should cover Dynamic Instants, overridable External Symbols, non-foldable functions, reflective method calls, runtime failure timing, and Current Element usage.
- Collection Navigation tests should cover Current Element, Map Entry Context, filters, map transforms, projections, aggregations, and Deep Scan behavior.
- Performance validation is mandatory for every change that touches evaluation hot paths.
- Function Invocation changes require JMH coverage for normal functions, string functions, math functions, and audit overhead.
- Collection Navigation changes require JMH coverage for list navigation, map navigation, filters, projections, map transforms, aggregations, and Deep Scan scenarios.
- Object Navigation changes require JMH coverage for Type Hint access and reflective fallback access.
- Constant Folding and planning changes require JMH coverage for planning throughput and compile-path allocation.
- Benchmark review must include throughput and allocation (`B/op`) before and after changes.
- A change that improves locality but regresses normal `compute()` allocation is not acceptable unless the user explicitly accepts the trade-off.

## Performance Requirements

- Normal `compute()` must not allocate more than the current baseline for common Function Invocation arities.
- Normal `compute()` must not allocate audit structures.
- Function Invocation arities from zero through six must avoid generic argument arrays when audit is disabled.
- `computeWithAudit()` may allocate Audit Events and owned audit argument arrays.
- Object Navigation with Type Hints must keep precomputed access behavior.
- Collection Navigation must not allocate a context object per Navigation Step.
- Collection Navigation must not introduce streams in evaluation loops.
- Execution Scope lookup must remain array-backed.
- Runtime Value must not be wrapped in a new object model.
- Any additional object retained in a Compiled Expression must be justified by reduced evaluation cost, improved correctness, or measurable compile-time locality.
- Any proposed polymorphism in the evaluation path must be proven by JMH or avoided.

## Out of Scope

- Changing the public Expression syntax.
- Adding a Runtime Value wrapper layer.
- Replacing Execution Scope with map-backed storage.
- Rewriting all Object Navigation and Collection Navigation in one step.
- Introducing new public Expression APIs for this work.
- Changing Function Catalog registration semantics.
- Changing Type Hint discovery semantics unless required by Member Binding correctness.
- Changing Audit Trail event types beyond what is needed to make Function Invocation behavior consistent.
- Optimizing cold compile paths at the expense of hot evaluation paths.
- Accepting architecture-only refactors without benchmark validation when the evaluation hot path is affected.

## Further Notes

- Root `CONTEXT.md` now defines the domain language used by this PRD.
- No ADRs were present during the architecture review, so no known ADR conflict is captured here.
- The top recommendation remains Function Invocation, coercion, and Audit Trail because it has a concrete behavior inconsistency and the smallest blast radius.
- Object Navigation and Collection Navigation should be treated as a later performance project because the risk of accidental GC or dispatch regression is high.
- The design goal is deep modules with small interfaces, but the performance constraint is stricter: depth must come from concentrated decisions and compile-time work, not from added runtime allocation.
