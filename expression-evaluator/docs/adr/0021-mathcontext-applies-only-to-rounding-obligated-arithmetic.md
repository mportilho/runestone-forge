# ADR 0021: `MathContext` Applies Only to Rounding-Obligated Arithmetic

## Status

Accepted; amends the `MathContext` applicability clause of ADR 0017

## Context

Issue #123 set out to make `MathContext` usage symmetric across every operation that "produces a new value by arithmetic," on the premise that `sum` accumulating without a context while `avg` divides with one, and `%` computing a remainder without a context while other arithmetic uses one, was an unexplained asymmetry. The originally planned fix was to *add* `MathContext` to `sum`'s accumulation loop and to `%`.

Revisiting the premise: `BigDecimal` arithmetic has three distinct scale behaviors, not one. `add`/`subtract` produce a result scaled to `max(scale1, scale2)` — bounded per operation, and not compounding across repeated operations the way multiplication does. `multiply` produces a result scaled to `scale1 + scale2` — under repetition (a `reduce` with `*`, or a compounding loop such as `npv`'s per-period rate), scale grows without bound and so does the cost of every subsequent operation. `divide`, `^` (power), `root`, and every transcendental function are obligated to round: their true result is not representable as a finite decimal in general, so a `MathContext` is not an optional precision knob for them but a termination requirement.

`BigDecimal.remainder(BigDecimal, MathContext)` is not "the exact remainder, rounded." It is defined in terms of an intermediate quotient computed under that `MathContext`; if the quotient's precision can't represent the true integer quotient, the operation throws `ArithmeticException` in cases where the context-free `remainder(BigDecimal)` succeeds. Applying `MathContext` to `%` is therefore not a performance trade-off available to make — it changes what number the operation computes, in a direction that isn't equivalent to "the remainder, rounded."

Given these behaviors, symmetric application of one `MathContext` semantics to every arithmetic operation is not achievable without either rounding operations that never need it (losing significant digits for no benefit) or changing what an operation computes (`%`). The asymmetry ADR 0017 already documented — `+`/`-`/`*`/`/`/`^`/`root` under `mathContext`, `remainder` exact — was itself not quite right either: `+` and `-` were grouped with the rounding-obligated operations despite not needing rounding.

## Decision

`MathContext` from the environment applies only where an operation is rounding-obligated (the result is not exactly representable at the operation's natural scale) or where scale grows under repetition:

- `+`, `-`, unary negation, `abs`, and comparisons remain exact. Their result scale is bounded by the operands' scale and does not compound; rounding them would only discard digits the language accepted as exact input.
- The `*` operator (`BinaryExecutableNode`) always applies `MathContext`, including for a single multiplication whose operand scales would not by themselves require rounding. A scale-dependent rule — round only above some threshold — would make the result depend on operand scale in a way that isn't a statable, testable contract for a user-facing operator over arbitrary operands; the simple rule is round always. The same "round always" rule applies to any hand-written internal multiplication that is loop-carried or otherwise accumulates across repetition (e.g. `npv`'s per-period rate), since that is exactly the unbounded-growth condition this ADR treats as rounding-obligated. It does not extend to single-shot internal multiplications inside built-in function bodies (e.g. most of `FinancialBuiltInFunctions`'s formulas): those operands are already bounded by the function's own precision policy — frequently one operand is itself the result of a `mathContext`-governed `pow`/`log` — and forcing an extra rounding pass on them is not required by this ADR.
- `/`, `^`, `root`, `reciprocal`, and every `transcendentalMathContext`-governed function apply their respective context unconditionally; this is unchanged from ADR 0017.
- `%` (`remainder`) remains exact, per ADR 0017. This ADR does not change that clause; it restates why: applying a context there computes a different, not-more-precise, number.

Collection accumulation follows from applying this rule pointwise: `sum` accumulates with exact `add`, matching term-by-term `+`; `avg` sums exactly and applies `mathContext` once, at the single division that produces the average. Built-in accumulation loops that use `add`/`subtract` internally (e.g. `variance`, `meanDev`, `harmonicMean` in `MathBuiltInFunctions`) drop `MathContext` from those calls; loops that compound by repeated multiplication (e.g. the per-period rate in `FinancialBuiltInFunctions.npv`) must apply `MathContext` to that multiplication even where the surrounding file otherwise uses exact single-shot multiplies, because repetition is exactly the condition this ADR treats as rounding-obligated.

## Consequences

This changes observable values for `+` and `-`, and for the `npv` per-period accumulator, relative to the current implementation guarded by ADR 0017. The Expression Corpus (ADR 0001) is the executable record of this contract; every corpus case whose expected result depended on `+`/`-` rounding, or on `npv`'s unrounded per-period compounding, must be recalculated with a stated justification, not silently regenerated. `sum` and `%` are unaffected in code — the asymmetry issue #123 set out to close dissolves because `sum` and `%` were already exact; issue #123's original plan to add `MathContext` to them is superseded by this ADR. The Oracle (ADR 0019) and the optimized plan share this code path, so equivalence continues to hold by construction; only the value both sides produce changes. Because this is a value-changing correction, it is sequenced as preparatory work ahead of Etapa 8, which requires semantics to stop moving before node specialization proceeds.
