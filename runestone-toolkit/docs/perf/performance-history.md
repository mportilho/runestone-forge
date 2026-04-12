# Performance History

## Known Hot-Paths & Discarded Hypotheses

- None recorded yet.

## PERF-001: Early assignable short-circuit and numeric fast paths in `DefaultDataConversionService`

**Date:** 2026-03-19

**Scenario:** Reduce per-call overhead in `DefaultDataConversionService.convert(...)` for assignable targets and common boxed numeric conversions.
**Hypothesis:** Checking `targetType.isInstance(source)` and adding direct `Number -> Integer/Long/Double` fast paths before generic converter lookup would remove avoidable dispatch and boxing on hot conversions.

| Benchmark | Before (ns/op) | After (ns/op) | Improvement (%) | B/op (B→A) |
|-----------|---------------:|--------------:|----------------:|-----------:|
| `convertAssignableNumber` | 332.988 | 0.484 | +99.85% | 24.005 → ≈ 0 |
| `convertAssignableTemporal` | 370.149 | 0.488 | +99.87% | 24.005 → ≈ 0 |
| `convertNumberToDoublePrimitive` | 4.915 | 4.712 | +4.13% | ≈ 0 → ≈ 0 |
| `convertNumberToDoubleWrapper` | 6.435 | 4.952 | +23.05% | 24.000 → 24.000 |
| `convertNumberToIntegerPrimitive` | 8.811 | 3.752 | +57.41% | 24.000 → ≈ 0 |
| `convertNumberToIntegerWrapper` | 8.014 | 3.548 | +55.73% | 24.000 → ≈ 0 |
| `convertNumberToLongPrimitive` | 8.562 | 3.669 | +57.15% | 24.000 → ≈ 0 |
| `convertNumberToLongWrapper` | 8.667 | 3.293 | +62.01% | 24.000 → ≈ 0 |

**Decision:** ACCEPT
**Reason:** Seven of eight scenarios improved strongly, with the assignable-target benchmarks improving by roughly two orders of magnitude and eliminating their previous allocation cost.
**Notes:** The `Double` wrapper case still allocates `24 B/op`; the likely remaining cost is wrapper allocation semantics rather than converter dispatch.

## PERF-002: High-performance `Collection/List -> array` in `DefaultDataConversionService`

**Date:** 2026-03-19

**Scenario:** Add a native `Collection/List -> array` conversion path that avoids reflective per-element stores and specializes primitive array targets.
**Hypothesis:** Replacing a reflective baseline based on `Array.set(...)` with direct indexed writes and primitive-specific loops would materially improve throughput, especially for numeric primitive arrays.

| Benchmark | Before (ns/op) | After (ns/op) | Improvement (%) | B/op (B→A) |
|-----------|---------------:|--------------:|----------------:|-----------:|
| `convertBigDecimalListToBigDecimalArray` | 1653.735 | 120.799 | +92.70% | 272.023 → 272.002 |
| `convertBigDecimalListToDoubleArray` | 1667.899 | 72.057 | +95.68% | 2064.023 → 528.001 |
| `convertBigDecimalListToIntArray` | 1617.162 | 63.682 | +96.06% | 272.022 → 272.001 |
| `convertBigDecimalListToLongArray` | 1560.998 | 69.283 | +95.56% | 528.022 → 528.001 |
| `convertIntegerListToNumberArray` | 1574.433 | 119.618 | +92.40% | 272.022 → 272.002 |
| `convertStringListToIntegerArray` | 1909.040 | 635.097 | +66.73% | 1808.026 → 1808.009 |

**Decision:** ACCEPT
**Reason:** All measured scenarios cleared the acceptance threshold by a wide margin, and the primitive-array cases also cut allocation substantially by removing reflective store overhead.
**Notes:** The `String -> Integer[]` case still allocates about `1808 B/op`, which is consistent with the actual conversion and boxed-array result rather than reflection.
