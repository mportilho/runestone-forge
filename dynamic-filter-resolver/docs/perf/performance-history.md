# Performance History

## 2026-08-24: Compiled annotation plan

Issue: [#150](https://github.com/mportilho/runestone-forge/issues/150)

The annotation statement generator now performs one lookup in a bounded, instance-local compiled-plan cache per generation. The global cache remains limited to immutable annotation-derived metadata and does not retain application-context objects.

### Protocol

- JDK: Temurin 21.0.6+7-LTS
- JMH: 1.37
- Mode: average time, nanoseconds
- Warmup: 5 iterations of 500 ms
- Measurement: 10 iterations of 500 ms
- Forks: 3
- Heap: `-Xms1g -Xmx1g`
- Profiler: `gc`
- Input: `SearchPeopleAndGames`
- Cold case: a new generator per invocation with the global structural metadata cache warm
- Warm case: one shared generator and precompiled input

Run the benchmark from `dynamic-filter-resolver` after `mvn test-compile -DskipTests`:

```shell
mvn -q dependency:build-classpath -Dmdep.outputFile=target/jmh-cp.txt -DincludeScope=test
CP="$(tr -d '\n' < target/jmh-cp.txt):target/test-classes:target/classes"
java -cp "$CP" org.openjdk.jmh.Main AnnotationStatementGeneratorPlanBenchmark -t 1 -prof gc -tu ns
java -cp "$CP" org.openjdk.jmh.Main AnnotationStatementGeneratorPlanBenchmark -t 8 -prof gc -tu ns
```

### Results

| Threads | Case | Before ns/op | After ns/op | Change | Before B/op | After B/op |
|---:|---|---:|---:|---:|---:|---:|
| 1 | Warm | 1783.4 | 755.4 | 57.6% faster | 3733 | 1808 |
| 8 | Warm | 4132.3 | 1293.7 | 68.7% faster | 3744 | 1808 |
| 1 | Cold | 1851.5 | 4290.7 | 131.7% slower | 3760 | 6320 |
| 8 | Cold | 3808.4 | 6570.3 | 72.5% slower | 3733 | 6323 |

Decision: accept. The measured target is the warmed request path, which is substantially faster and allocates about 52% less. The expected cold-path cost is the one-time compilation of immutable metadata into the generator-local plan.

## 2026-08-24: Scalar filter value transformers

Issue: [#151](https://github.com/mportilho/runestone-forge/issues/151)

Scalar transformer instances and immutable parameter contexts are now bound into the compiled annotation plan. The dispatch benchmark compares the bound chain with direct calls to the same transformer instances, using both identity transformers and transformers that allocate replacement values.

### Protocol

- JDK: Temurin 21.0.6+7-LTS
- JMH: 1.37
- Mode: average time, nanoseconds
- Warmup: 5 iterations of 500 ms
- Measurement: 10 iterations of 500 ms
- Forks: 3
- Heap: `-Xms1g -Xmx1g`
- Profiler: `gc`
- Threads: 1 and 8
- Baseline: detached worktree at `23e0e78`, measured in the same session

```shell
java -Xms1g -Xmx1g -cp "$CP" org.openjdk.jmh.Main ScalarFilterValueTransformerBenchmark -t 1 -prof gc -tu ns
java -Xms1g -Xmx1g -cp "$CP" org.openjdk.jmh.Main ScalarFilterValueTransformerBenchmark -t 8 -prof gc -tu ns
```

### Identity-transformer results

| Threads | Transformers | Bound chain ns/op | Direct ns/op | Framework overhead | Bound chain B/op |
|---:|---:|---:|---:|---:|---:|
| 1 | 0 | 0.589 | 0.424 | not applicable | ~0 |
| 1 | 1 | 1.170 | 1.808 | -35.3% | ~0 |
| 1 | 3 | 3.267 | 2.977 | 9.7% | ~0 |
| 8 | 0 | 1.038 | 0.772 | not applicable | ~0 |
| 8 | 1 | 1.858 | 3.818 | -51.3% | ~0 |
| 8 | 3 | 6.109 | 6.179 | -1.1% | ~0 |

The sub-nanosecond direct baseline for zero transformers is included only as a sanity check; its percentage is not used as a gate.

### Replacement-transformer results

| Threads | Transformers | Bound chain ns/op | Direct ns/op | Framework overhead | Bound chain B/op | Direct B/op |
|---:|---:|---:|---:|---:|---:|---:|
| 1 | 1 | 1.982 | 2.140 | -7.4% | 16 | 16 |
| 1 | 3 | 5.917 | 5.794 | 2.1% | 48 | 48 |
| 8 | 1 | 5.767 | 6.093 | -5.4% | 16 | 16 |
| 8 | 3 | 18.526 | 17.497 | 5.9% | 48 | 48 |

The warmed end-to-end regression benchmark is the meaningful zero-transformer gate:

| Threads | Baseline ns/op | After ns/op | Change | Baseline B/op | After B/op |
|---:|---:|---:|---:|---:|---:|
| 1 | 573.8 | 558.7 | 2.6% faster | 1808 | 1808 |
| 8 | 1490.6 | 1397.8 | 6.2% faster | 1808 | 1808 |

Decision: accept. The warmed path without transformers adds no allocation and does not regress. Chains with one and three transformers allocate no context or chain structure per invocation, and measured overhead against direct transformer calls remains below 10%.

## 2026-08-24: Multivalue and dynamic filter value transformers

Issue: [#152](https://github.com/mportilho/runestone-forge/issues/152)

Reference arrays and collections are transformed in one pass into a single output container. Dynamic filters resolve their opcode before dispatch and reuse contexts compiled for the effective operation.

### Protocol

- JDK: Temurin 21.0.6+7-LTS
- JMH: 1.37
- Mode: average time, nanoseconds
- Warmup: 5 iterations of 500 ms
- Measurement: 10 iterations of 500 ms
- Forks: 3
- Heap: `-Xms1g -Xmx1g`
- Profiler: `gc`
- Threads: 1 and 8
- Container: four-element reference array
- Transformers: replacement transformers allocating 16 bytes per call

```shell
java -Xms1g -Xmx1g -cp "$CP" org.openjdk.jmh.Main MultiValueFilterValueTransformerBenchmark -t 1 -prof gc -tu ns
java -Xms1g -Xmx1g -cp "$CP" org.openjdk.jmh.Main MultiValueFilterValueTransformerBenchmark -t 8 -prof gc -tu ns
java -Xms1g -Xmx1g -cp "$CP" org.openjdk.jmh.Main FilterValueTransformerFlowBenchmark -t 1 -prof gc -tu ns
java -Xms1g -Xmx1g -cp "$CP" org.openjdk.jmh.Main FilterValueTransformerFlowBenchmark -t 8 -prof gc -tu ns
```

### Results

| Threads | Flow | Transformers | Bound chain ns/op | Direct ns/op | Framework overhead | Bound chain B/op | Direct B/op |
|---:|---|---:|---:|---:|---:|---:|---:|
| 1 | Normal | 1 | 13.222 | 13.176 | 0.3% | 96 | 96 |
| 1 | Normal | 3 | 26.272 | 26.963 | -2.6% | 224 | 224 |
| 1 | Dynamic | 1 | 15.384 | 14.158 | 8.7% | 96 | 96 |
| 1 | Dynamic | 3 | 28.023 | 28.773 | -2.6% | 224 | 224 |
| 8 | Normal | 1 | 38.351 | 36.872 | 4.0% | 96 | 96 |
| 8 | Normal | 3 | 79.759 | 81.826 | -2.5% | 224 | 224 |
| 8 | Dynamic | 1 | 41.430 | 40.046 | 3.5% | 96 | 96 |
| 8 | Dynamic | 3 | 80.880 | 82.893 | -2.4% | 224 | 224 |

The allocation totals are identical to direct calls: one 32-byte output array plus 16 bytes for each replacement produced by the transformer. Increasing the chain from one to three stages does not allocate another container.

The end-to-end benchmark covers warmed plan lookup, value selection, transformation, dynamic opcode parsing, payload assembly, and `FilterData` creation with identity transformers:

| Threads | Flow | Transformers | ns/op | B/op |
|---:|---|---:|---:|---:|
| 1 | Normal scalar | 1 | 93.564 | 296 |
| 1 | Normal scalar | 3 | 83.716 | 296 |
| 1 | Normal container | 1 | 64.897 | 328 |
| 1 | Normal container | 3 | 69.427 | 328 |
| 1 | Dynamic scalar | 1 | 69.398 | 368 |
| 1 | Dynamic scalar | 3 | 69.850 | 368 |
| 1 | Dynamic container | 1 | 74.179 | 400 |
| 1 | Dynamic container | 3 | 81.206 | 400 |
| 8 | Normal scalar | 1 | 172.540 | 296 |
| 8 | Normal scalar | 3 | 202.975 | 296 |
| 8 | Normal container | 1 | 164.308 | 328 |
| 8 | Normal container | 3 | 173.021 | 328 |
| 8 | Dynamic scalar | 1 | 186.452 | 368 |
| 8 | Dynamic scalar | 3 | 188.298 | 368 |
| 8 | Dynamic container | 1 | 210.062 | 400 |
| 8 | Dynamic container | 3 | 200.502 | 400 |

For each flow, allocation is unchanged when the chain grows from one to three identity transformers. This confirms that contexts and intermediate containers are not allocated per transformer.

The warmed no-transformer regression gate was compared with the #151 measurements:

| Threads | #151 ns/op | #152 ns/op | Change | #151 B/op | #152 B/op |
|---:|---:|---:|---:|---:|---:|
| 1 | 558.7 | 493.6 | 11.7% faster | 1808 | 1808 |
| 8 | 1397.8 | 1282.8 | 8.2% faster | 1808 | 1808 |

Decision: accept. All measured framework overhead remains below 10%, transformed containers add exactly one container allocation for the complete chain, dynamic contexts add no request-time allocation, and the no-transformer path retains its allocation profile without latency regression.
