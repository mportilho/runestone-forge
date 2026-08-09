# ADR 0017: Real Decimal Domain and big-math

## Status

Accepted; `MathContext` applicability clause (paragraph on ordinary arithmetic) amended by ADR 0021

## Context

ADR 0013 selected one decimal numeric semantics but did not define the domain of exponentiation and roots, their rounding boundary, or whether a numerical library's rejected inputs should restrict the expression language. The runtime already depends on `big-math`, while direct delegation rejects every negative radicand and accepts some non-integral roots without distinguishing real, complex, and undefined results.

## Decision

`NUMBER` admits decimal operations classified as mathematically defined with a real result. Results requiring complex numbers and mathematically undefined operations are rejected as distinct diagnostic families. Semantic admissibility does not guarantee that every magnitude is calculable: representation overflow, configured limits, or a `big-math` failure at the requested precision produces a stable calculation/rounding or limit diagnostic rather than a complex-domain or undefined-operation diagnostic.

Every finite decimal is interpreted as its exact canonical reduced rational value when classifying the domain and sign of exponentiation or roots; this classification does not replace the decimal value used for calculation. Canonical `p/q` means coprime integers with `q > 0`; numerator parity uses `abs(p)`. Implementations must derive the required parity from the decimal representation without materializing a denominator proportional to `10^scale`.

For a negative base and reduced exponent `p/q`, exponentiation is real exactly when `q` is odd, and its sign is negative exactly when `abs(p)` is odd. Every nonzero base to exponent zero is `1`, and by language convention `0 ^ 0` is also `1`. Zero to a positive exponent is zero; zero to a negative exponent is undefined.

For `degree root radicand`, a nonzero canonical reduced degree `a/b`, with `b > 0`, is interpreted as exponent `b/a`; a negative radicand has a real result exactly when `abs(a)` is odd. When real, that result is negative exactly when `b` is odd and positive when `b` is even. Degrees may be positive, negative, integral, or fractional when the result remains real and defined. A zero radicand with positive degree is zero, a zero radicand with negative degree is undefined, and degree zero is undefined.

All exponentiation and root calculation in every execution tier uses `big-math`. For a classified negative result, magnitude calculation uses a derived `MathContext` with the requested precision and rounding mode, except that `CEILING` and `FLOOR` are exchanged; other supported modes are unchanged. The language then negates the magnitude exactly, making sign restoration equivalent to rounding a negative result with the environment's original context.

For `base < 0` and a rational exponent classified as real, magnitude is always calculated as `BigDecimalMath.pow(abs(base), exponent, magnitudeContext)`, regardless of the classified result sign. If `abs(p)` is odd, the negative-result context and exact sign restoration apply; if `abs(p)` is even, the original context is used and the positive magnitude is returned.

A negative root degree is never passed directly to `BigDecimalMath.root`. For a nonzero radicand and `degree < 0`, magnitude is defined by `BigDecimalMath.reciprocal(BigDecimalMath.root(abs(radicand), abs(degree), magnitudeContext), magnitudeContext)`. This intermediate rounding is part of the language contract. A positive degree uses `BigDecimalMath.root(abs(radicand), degree, magnitudeContext)` directly. After either path, the language restores the classified sign exactly. `BigComplexMath` is not used. Replacing `big-math` requires a later explicit decision backed by semantic equivalence and performance evidence.

The environment requires positive precision and an effective rounding mode other than `RoundingMode.UNNECESSARY` for both `mathContext` and `transcendentalMathContext`. Ordinary addition, subtraction, multiplication, division, exponentiation, and roots use `mathContext`; transcendental functions use `transcendentalMathContext`. Literals, unary negation, postfix percent, factorial, comparisons, and `BigDecimal.remainder` remain exact. Remainder follows the dividend's sign. Public decimal results preserve their naturally produced scale and are not globally normalized.

Statically provable domain violations are compilation diagnostics. Value-dependent violations become typed deferred checks selected by semantic resolution and consumed by the execution plan without rediscovering the rule.

## Consequences

The semantic resolver needs rational-domain facts and deferred checks beyond the original integral/fractional distinction. The runtime must wrap library failures in stable expression diagnostics rather than expose `big-math` exception types or messages. Tests must cover directional rounding of negative results, negative-base power adapters for odd and even reduced numerators, the negative-degree root adapter, exhaustive zero cases, and extreme decimal scales without building `10^scale`; `BigDecimalMath.pow` must never receive a negative base on an adapted real fractional path. Optimizations and specialized tiers must preserve real-domain classification, scale, rounding, failures, and the mandatory numerical library path; algebraic rewrites are not valid merely because they are equivalent over ideal real arithmetic.
