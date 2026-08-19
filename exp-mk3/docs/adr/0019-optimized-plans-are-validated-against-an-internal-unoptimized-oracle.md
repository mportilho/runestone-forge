# ADR 0019: Optimized Plans Are Validated Against an Internal Unoptimized Oracle

## Status

Accepted

## Context

Until the language surface closed, the executed plan was a direct, mechanical projection of a successful semantic model: one execution node per source construct, no rewriting, no evaluation at compilation time. From the first compilation optimization onward that stops being true, and the executed plan may differ structurally from what the source says.

The module's numeric semantics make that divergence unusually easy to get wrong. Every number is a `BigDecimal` under an environment `MathContext`, so scale and rounding are observable parts of a result, not incidental detail. Power and root are defined over the real domain with `big-math` and fail outside it. Membership compares numbers by `compareTo` and everything else by `equals`. Factorial, subscript bounds, and the materialization limit are enforced as deferred checks decided during semantic resolution. A rewrite can therefore preserve the "obvious" value and still change scale, change which failure is raised, change where it is raised, or make a failure disappear entirely.

Testing an optimization against hand-written expected values does not scale to that surface, because the expected values themselves encode the same assumptions the optimization makes. What does scale is comparing the optimized plan against a form of the same expression that is known not to optimize anything — provided that form is produced by the same pipeline, so that a difference can only come from the optimization and never from a second implementation drifting away.

There is also a question of who may select that form. Exposing it as a public switch would turn an internal verification device into a supported execution mode that callers could enable in production, that would need its own compatibility promise, and that would invite the two forms to be tuned independently.

Finally, the boundary between compilation-time failure and execution-time failure is a language contract, not an implementation detail. A constant subexpression that fails — `2/0`, a power outside the real domain, a factorial beyond its bound — could in principle be reported at compilation time, since its failure is already knowable. Doing so would move a runtime diagnostic to compilation and change what a caller observes for an expression that may never be evaluated on that path.

## Decision

Every optimizing transformation of an execution plan is validated by equivalence against an unoptimized oracle produced by the same plan-building pipeline. Equivalence is required in value, scale, rounding, domain, failure code, failure source span, observable evaluation order, and observable effects. When Calculation Memory is requested, equivalence also covers participating variable keys/values and reached calculation keys/values, including their order and runtime nulls. A transformation without such a proof is not installed, and "it looks obviously safe" is not a proof.

The oracle is selectable only inside the module. There is no public flag, no system property, no separate runtime, and no supported execution mode built on it. The pipeline offers one optimized entry point and one oracle entry point, and both build from the same nodes and the same semantic metadata.

A constant subexpression that fails while being folded is left unfolded, so that it fails during execution exactly as the oracle would, with the same diagnostic code and the same source span. Folding never produces a new compilation diagnostic and never produces a poisoned constant value. A deferred check is discharged only when its subtree folds successfully; a failed check abandons the fold and preserves both the node and the check.

Eligibility for any transformation that reuses, discards, or relocates a computation rests on purity that was declared during semantic resolution and recorded in the semantic model. The optimizer does not infer purity, does not assume that a registered method is pure because it looks like an accessor, and does not rediscover any semantic rule in order to justify a rewrite.

## Consequences

Equivalence runs as part of ordinary verification rather than as a one-off review: the whole expression corpus is executed in both forms on every build, and property-based generation covers effects, failures, scale, and the real domain. That cost is accepted deliberately, because it is what makes every later optimization phase — specialized nodes, tiered compilation, collection pipeline fusion — able to inherit a ready criterion instead of inventing its own.

ADR 0023 extends that permanent verification to Calculation Memory. Constant folding must retain static provenance for every markable source occurrence it collapses, and a memo hit must publish the points of the reached occurrence that it skips executing, using only execution-local captured values and never reinvoking providers.

The unoptimized form must therefore keep working, keep being exercised, and keep being maintained for as long as optimizations exist. It is not dead code that survived a phase; it is the reference semantics. Deleting it later would mean deleting the only mechanical answer to "did this rewrite change behavior".

Optimizations that cannot be proven equivalent stay out, even when other languages perform them routinely. Arithmetic identities, modulo rewrites, power and root rewrites, and reordering of short-circuit operands are all in that category under decimal semantics with a configurable `MathContext`. The engine will be slower than one that assumes IEEE-754 semantics, and that trade is the point of the decimal contract rather than a defect of it.

Because failing constants are left unfolded, an expression can carry a subtree that is known to fail and still compile successfully. That is intentional: the failure surfaces if and only if evaluation reaches it, exactly as before optimization. It also means a poorly formed constant is not detected earlier just because folding examined it.

Keeping the oracle internal means callers cannot reproduce a suspected optimization bug by switching it off in their own deployment. Diagnosing such a report requires reproducing the expression and environment inside the module's own tests, which is accepted in exchange for not turning a verification device into a public contract.

Making declared purity the gate for reuse means an unannotated registered method blocks folding and memoization of anything downstream of it. The cost of missing metadata is lost performance, never wrong results, and that asymmetry is the reason purity is declared during resolution instead of guessed during optimization.
