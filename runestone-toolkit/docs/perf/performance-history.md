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

## PERF-003: Exact and assignable runtime converter lookup caches

**Date:** 2026-07-12

**Scenario:** Measure and reduce lookup overhead in `DefaultRuntimeDataConversionService` for exact runtime converters and assignable runtime converters.
**Hypothesis:** Replacing per-call exact pair-key allocation with an immutable `Class<?>` identity index and caching assignable converter selection by runtime source type would remove avoidable allocation and repeated type-distance scans.

| Benchmark | Before (ns/op) | After (ns/op) | Improvement (%) | B/op (B→A) |
|-----------|---------------:|--------------:|----------------:|-----------:|
| `canConvertAssignableNumberToLong` | 814.018 | 25.036 | +96.92% | 426.678 → ≈ 0 |
| `canConvertExactStringToInteger` | 13.657 | 7.968 | +41.66% | 8.000 → ≈ 0 |
| `convertAssignableNumberToLong` | 831.068 | 40.017 | +95.18% | 464.012 → 24.001 |
| `convertAssignableNumberToString` | 843.796 | 39.736 | +95.29% | 488.012 → 48.001 |
| `convertContainerStringListToIntegerArray` | 576.018 | 554.350 | +3.76% | 48.008 → 48.008 |
| `convertExactLocalDateToTemporal` | 0.998 | 1.004 | -0.65% | ≈ 0 → ≈ 0 |
| `convertExactStringToInteger` | 27.073 | 23.299 | +13.94% | 16.000 → 16.000 |

**Decision:** ACCEPT
**Reason:** Six of seven measured scenarios improved, with the exact lookup and assignable lookup benchmarks eliminating lookup allocation and the assignable conversion paths improving by roughly one order of magnitude. The neutral `LocalDate -> Temporal` case is an identity conversion that bypasses converter lookup and stayed within noise.
**Notes:** JMH ran on OpenJDK 26.0.1 with JMH 1.37. Raw result files were captured at `/tmp/performance-benchmark/issue-50-before.json`, `/tmp/performance-benchmark/issue-50-after.json`, and `/tmp/performance-benchmark/issue-50-comparison.md`.

## PERF-004: Exact and assignable foldable converter lookup caches

**Date:** 2026-07-12

**Scenario:** Reduce lookup overhead in `DefaultDataConversionService` for exact foldable converters and assignable foldable converters without changing `Foldable Conversion` semantics.
**Hypothesis:** Replacing per-call exact pair-key allocation with an immutable `Class<?>` index and caching assignable converter selection by runtime source type would remove avoidable allocation and repeated type-distance scans.

| Benchmark | Before (ns/op) | After (ns/op) | Improvement (%) | B/op (B->A) |
|-----------|---------------:|--------------:|----------------:|-----------:|
| `canConvertAssignableNumberToLong` | 603.400 | 36.727 | +93.91% | 443 -> ≈ 0 |
| `canConvertExactStringToInteger` | 12.181 | 8.867 | +27.21% | 24 -> ≈ 0 |
| `convertAssignableNumber` | 10.098 | 3.358 | +66.74% | 24 -> ≈ 0 |
| `convertAssignableNumberToLong` | 663.397 | 50.471 | +92.39% | 440 -> ≈ 0 |
| `convertAssignableTemporal` | 11.310 | 7.598 | +32.82% | 24 -> ≈ 0 |
| `convertContainerStringListToIntegerArray` | 1486.237 | 1428.779 | +3.87% | 312 -> 96 |
| `convertExactStringToInteger` | 24.650 | 20.716 | +15.96% | 40 -> 16 |
| `convertNumberToDoublePrimitive` | 631.016 | 38.629 | +93.88% | 440 -> 24 |
| `convertNumberToDoubleWrapper` | 625.864 | 37.884 | +93.95% | 459 -> 24 |
| `convertNumberToIntegerPrimitive` | 708.652 | 48.446 | +93.16% | 451 -> ≈ 0 |
| `convertNumberToIntegerWrapper` | 681.322 | 47.829 | +92.98% | 451 -> ≈ 0 |
| `convertNumberToLongPrimitive` | 648.009 | 50.855 | +92.15% | 403 -> ≈ 0 |
| `convertNumberToLongWrapper` | 674.265 | 50.682 | +92.48% | 440 -> ≈ 0 |

**Decision:** ACCEPT
**Reason:** All measured scenarios improved. Exact `canConvert(...)` eliminated its previous lookup allocation, assignable `canConvert(...)` and `convert(...)` improved by roughly one order of magnitude, and container conversion did not regress.
**Notes:** JMH ran on OpenJDK 26.0.1 with JMH 1.37. Raw result files were captured at `/tmp/performance-benchmark/issue-51-before.json`, `/tmp/performance-benchmark/issue-51-after.json`, and `/tmp/performance-benchmark/issue-51-comparison.md`.

## PERF-005: Specialized runtime container and array conversion paths

**Date:** 2026-07-12

**Scenario:** Reduce reflective per-element overhead in `DefaultRuntimeDataConversionService` for runtime `Collection -> array` and `array -> array` conversions.
**Hypothesis:** Replacing `Array.set(...)` on array targets with direct reference-array writes and primitive-array loops, plus a targeted `int[] -> long[]` source fast path, would reduce reflective dispatch and primitive boxing while preserving runtime element conversion semantics.

| Benchmark | Before (ns/op) | After (ns/op) | Improvement (%) | B/op (B->A) |
|-----------|---------------:|--------------:|----------------:|-----------:|
| `convertArrayIntToLongArray` | 1544.821 | 209.370 | +86.45% | 208 -> 80 |
| `convertArrayStringToIntegerArray` | 989.420 | 145.621 | +85.28% | 48 -> 48 |
| `convertContainerBigDecimalListToDoubleArray` | 765.187 | 243.954 | +68.12% | 272 -> 272 |
| `convertContainerBigDecimalListToIntArray` | 881.183 | 242.045 | +72.53% | 48 -> 48 |
| `convertContainerBigDecimalListToLongArray` | 818.355 | 256.639 | +68.64% | 80 -> 80 |
| `convertContainerStringListToIntegerArray` | 564.692 | 183.425 | +67.52% | 48 -> 48 |

**Decision:** ACCEPT
**Reason:** All issue-targeted container and array scenarios improved well above the 10% acceptance threshold. `int[] -> long[]` also reduced allocation by removing `Array.get(...)` boxing for the measured primitive source path.
**Notes:** JMH ran on OpenJDK 26.0.1 with JMH 1.37. Raw result files were captured at `/tmp/performance-benchmark/issue-52-before.json`, `/tmp/performance-benchmark/issue-52-after.json`, and `/tmp/performance-benchmark/issue-52-comparison.md`. The comparison script reported small regressions in unrelated scalar conversion benchmarks (`convertAssignableNumberToLong` and `convertExactStringToInteger`), but those paths are not touched by this change and their confidence intervals overlapped; they were treated as run noise rather than a blocker for the targeted container/array change.

## PERF-006: Runtime enum exact-name lookup fast path

**Date:** 2026-07-12

**Scenario:** Reduce runtime enum conversion overhead for string inputs that already match exact enum constant names while preserving case-insensitive string lookup and ordinal conversion behavior.
**Hypothesis:** Trying an exact enum-name map lookup before normalizing the input with `toUpperCase(Locale.ROOT)` would reduce exact-name conversion time without materially regressing lowercase, mixed-case, or ordinal inputs.

| Benchmark | Before (ns/op) | After (ns/op) | Improvement (%) | B/op (B->A) |
|-----------|---------------:|--------------:|----------------:|-----------:|
| `convertLargeEnumExactName` | 17.752 | 12.393 | +30.19% | ≈ 0 -> ≈ 0 |
| `convertLargeEnumLowercaseName` | 49.427 | 42.683 | +13.64% | 48 -> 48 |
| `convertLargeEnumMixedCaseName` | 51.675 | 49.740 | +3.74% | 48 -> 48 |
| `convertLargeEnumOrdinal` | 5.382 | 5.467 | -1.59% | ≈ 0 -> ≈ 0 |
| `convertSmallEnumExactName` | 15.178 | 12.823 | +15.51% | ≈ 0 -> ≈ 0 |
| `convertSmallEnumLowercaseName` | 40.561 | 40.216 | +0.85% | 48 -> 48 |
| `convertSmallEnumMixedCaseName` | 53.049 | 49.230 | +7.20% | 48 -> 48 |
| `convertSmallEnumOrdinal` | 5.329 | 5.546 | -4.06% | ≈ 0 -> ≈ 0 |

**Decision:** ACCEPT
**Reason:** Exact-name enum conversion improved by 15-30% with no allocation increase. Lowercase and mixed-case fallback string inputs were neutral to improved. The ordinal path reported small percentage regressions, but the absolute changes were sub-nanosecond, the confidence intervals overlapped, allocation stayed at approximately zero, and the ordinal lookup path was not changed by the refactor.
**Notes:** JMH ran on OpenJDK 26.0.1 with JMH 1.37. Raw result files were captured at `/tmp/performance-benchmark/issue-54-before.json`, `/tmp/performance-benchmark/issue-54-after.json`, and `/tmp/performance-benchmark/issue-54-comparison.md`. The comparison script reported `DISCARD` because the ordinal percentage deltas crossed its generic regression threshold; this was overridden as non-material run noise for the untouched ordinal path.
