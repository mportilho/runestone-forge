## PERF-001: Early assignable short-circuit and numeric fast paths in `DefaultDataConversionService`

**Date:** 2026-03-19

**Scenario:** Reduce per-call overhead in `DefaultDataConversionService.convert(...)` by returning immediately when the source already matches the target type via `targetType.isInstance(source)`, and by adding direct `Number -> Integer/Long/Double` fast paths before the generic converter lookup.
**Hypothesis:** Moving these checks ahead of `getConverter(...)` and `fallbackConversion(...)` would eliminate avoidable map lookups, converter dispatch, and boxing on hot conversions, especially for assignable targets and boxed numeric wrappers.

**Technical baseline:** Working tree state before the optimization in the current session, measured with a fresh JMH baseline using `DataConversionServiceBenchmark` on JDK 21.0.6. Functional safety was validated with `mvn -q -pl runestone-toolkit -Dtest=TestDataConversionService test` rerun outside the sandbox because Mockito inline mocking cannot self-attach reliably inside the sandbox.

**Changes applied:**
- `DefaultDataConversionService`: broadened the identity fast path from exact-class equality to `targetType.isInstance(source)` at the top of `convert(...)`.
- `DefaultDataConversionService`: added direct `Number -> Integer/Long/Double` conversions for both wrapper and primitive targets before generic converter lookup.
- `TestDataConversionService`: added coverage for numeric wrapper conversions and assignable-target short-circuit behavior.
- `DataConversionServiceBenchmark`: added a dedicated JMH benchmark for assignable targets plus numeric wrapper/primitive targets with GC profiling.

| Benchmark | Before (ns/op) | After (ns/op) | Improvement (%) | Before (B/op) | After (B/op) |
|---|---:|---:|---:|---:|---:|
| `convertAssignableNumber` | 332.988 | 0.484 | +99.85% | 24.005 | ≈ 0 |
| `convertAssignableTemporal` | 370.149 | 0.488 | +99.87% | 24.005 | ≈ 0 |
| `convertNumberToDoublePrimitive` | 4.915 | 4.712 | +4.13% | ≈ 0 | ≈ 0 |
| `convertNumberToDoubleWrapper` | 6.435 | 4.952 | +23.05% | 24.000 | 24.000 |
| `convertNumberToIntegerPrimitive` | 8.811 | 3.752 | +57.41% | 24.000 | ≈ 0 |
| `convertNumberToIntegerWrapper` | 8.014 | 3.548 | +55.73% | 24.000 | ≈ 0 |
| `convertNumberToLongPrimitive` | 8.562 | 3.669 | +57.15% | 24.000 | ≈ 0 |
| `convertNumberToLongWrapper` | 8.667 | 3.293 | +62.01% | 24.000 | ≈ 0 |

**Decision:** ACCEPT
**Reason:** Seven of eight scenarios improved by more than 23%, and the two assignable-target cases improved by roughly two orders of magnitude while eliminating the previous `24 B/op` allocation. The only small gain was `double` primitive (+4.13%), but it is still positive and came with no behavioral risk. The measured average improvement across all scenarios was +57.40%.
**Notes:** The `Double` wrapper scenario still allocates `24 B/op` after the change. This is measured data, not inference. The likely cause is wrapper allocation semantics for `Double.valueOf(...)`, unlike `Integer` and `Long`, which benefit from JVM caching in the benchmark range. That remaining allocation is a narrower follow-up topic and does not block this optimization.
