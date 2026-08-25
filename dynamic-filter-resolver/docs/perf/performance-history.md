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

## 2026-08-24: Final transformer integration gates

Issue: [#154](https://github.com/mportilho/runestone-forge/issues/154)

The final matrix crosses normal scalar, normal container, dynamic scalar, and dynamic container flows with zero, one, and three transformers. Each combination runs with identity and replacement transformers against cold and warmed plan caches. The baseline and final runs use identical production bytecode because this closing issue adds integration tests, documentation, and benchmark coverage without changing the runtime implementation.

### Protocol

- CPU: Intel Core Ultra 7 165U, 7 cores / 14 logical CPUs
- JDK: Temurin 21.0.6+7-LTS
- JMH: 1.37
- Mode: average time, nanoseconds
- Warmup: 3 iterations of 300 ms
- Measurement: 5 iterations of 300 ms
- Forks: 2
- Heap: `-Xms1g -Xmx1g`
- Profiler: `gc`
- Threads: 1 and 8
- Artifacts: `/tmp/performance-benchmark/issue-154/`

Run from `dynamic-filter-resolver` after `mvn clean test-compile -DskipTests` (the clean build is required to regenerate JMH's `META-INF/BenchmarkList`):

```shell
mvn -q dependency:build-classpath -Dmdep.outputFile=target/jmh-cp.txt -DincludeScope=test
CP="$(tr -d '\n' < target/jmh-cp.txt):target/test-classes:target/classes"
PHASE=baseline # Repeat the two flow commands with PHASE=final.

java -Xms1g -Xmx1g -cp "$CP" org.openjdk.jmh.Main FilterValueTransformerFlowBenchmark \
  -wi 3 -i 5 -f 2 -w 300ms -r 300ms -t 1 -prof gc -tu ns -rf json \
  -rff /tmp/performance-benchmark/issue-154/${PHASE}-t1.json
java -Xms1g -Xmx1g -cp "$CP" org.openjdk.jmh.Main FilterValueTransformerFlowBenchmark \
  -wi 3 -i 5 -f 2 -w 300ms -r 300ms -t 8 -prof gc -tu ns -rf json \
  -rff /tmp/performance-benchmark/issue-154/${PHASE}-t8.json
java -Xms1g -Xmx1g -cp "$CP" org.openjdk.jmh.Main FilterValueTransformerArgumentResolverBenchmark \
  -wi 3 -i 5 -f 2 -w 300ms -r 300ms -t 1 -prof gc -tu ns -rf json \
  -rff /tmp/performance-benchmark/issue-154/argument-resolver-t1.json
java -Xms1g -Xmx1g -cp "$CP" org.openjdk.jmh.Main FilterValueTransformerArgumentResolverBenchmark \
  -wi 3 -i 5 -f 2 -w 300ms -r 300ms -t 8 -prof gc -tu ns -rf json \
  -rff /tmp/performance-benchmark/issue-154/argument-resolver-t8.json
java -Xms1g -Xmx1g -cp "$CP" org.openjdk.jmh.Main \
  'ScalarFilterValueTransformerBenchmark|MultiValueFilterValueTransformerBenchmark' \
  -wi 3 -i 5 -f 2 -w 300ms -r 300ms -t 1 -prof gc -tu ns -rf json \
  -rff /tmp/performance-benchmark/issue-154/direct-t1.json
java -Xms1g -Xmx1g -cp "$CP" org.openjdk.jmh.Main \
  'ScalarFilterValueTransformerBenchmark|MultiValueFilterValueTransformerBenchmark' \
  -wi 3 -i 5 -f 2 -w 300ms -r 300ms -t 8 -prof gc -tu ns -rf json \
  -rff /tmp/performance-benchmark/issue-154/direct-t8.json
```

Before measurement, `mvn test -pl dynamic-filter-resolver -am` passed 389 tests with no failures on Java 21.

### Final matrix

The complete final run contains 48 parameter combinations for each thread count (96 measurements total). The table summarizes the observed ranges; every individual result, including score error and GC secondary metrics, remains in `final-t1.json` and `final-t8.json`.

| Threads | Cache | ns/op range | B/op range |
|---:|---|---:|---:|
| 1 | Warm | 76.5 - 350.4 | 296 - 592 |
| 1 | Cold | 693.0 - 56,404.7 | 2,640 - 3,600 |
| 8 | Warm | 235.6 - 2,787.8 | 296 - 605 |
| 8 | Cold | 4,995.4 - 203,843.0 | 2,758 - 3,962 |

The host CPU was shared with other important processes during this closing run. Absolute latency and cross-run comparisons in this matrix are therefore informational; the initial baseline run also lost eight 8-thread cells to fork class-loading failures and is not used for a performance verdict. No additional long-running measurement was made after that environmental limitation was identified.

Allocation remained deterministic in the complete 1-thread warmed matrix:

| Flow | Identity 0 / 1 / 3 B/op | Replacement 0 / 1 / 3 B/op |
|---|---:|---:|
| Normal scalar | 296 / 296 / 296 | 296 / 312 / 344 |
| Normal container | 296 / 328 / 328 | 296 / 392 / 520 |
| Dynamic scalar | 368 / 368 / 368 | 368 / 384 / 416 |
| Dynamic container | 400 / 400 / 400 | 400 / 464 / 592 |

Identity chains allocate no per-operation context or chain structure: moving from one to three transformers leaves `B/op` unchanged. Replacement scalar growth is exactly 16 bytes per returned object. Four-element containers add one 32-byte output array for the entire chain plus the replacement objects (64 bytes for one transformer and 192 bytes for three), not one container per transformer.

### Direct-call gate

The bound-chain and direct-call methods ran together under the same JMH invocation. Across scalar, normal-container, and dynamic-container cases with one and three transformers, framework overhead ranged from faster than direct calls to the following measured maxima:

| Threads | Maximum overhead | Case | Bound B/op | Direct B/op |
|---:|---:|---|---:|---:|
| 1 | 9.3% | Scalar identity, 3 transformers | ~0 | ~0 |
| 8 | 8.9% | Scalar replacement, 3 transformers | 48 | 48 |

All normal and dynamic multivalue cases remained below 6% overhead. Their bound and direct allocation totals were identical: 96 B/op with one transformer and 224 B/op with three transformers.

### Argument resolver

The additional warmed benchmark covers HTTP parameter extraction, the shared compiled generator, transformation, the real `SpecificationDynamicFilterResolver`, standard JPA operation creation, and specification proxy creation.

| Threads | ns/op | B/op |
|---:|---:|---:|
| 1 | 273.6 | 496 |
| 8 | 1,119.0 | 636 |

The 8-thread latency and allocation are retained as observations rather than contention evidence because of the shared host. Deterministic concurrent behavior is covered by the 8-thread matrix and the dedicated concurrent tests; the bound plan contains no lock or mutable request state.

### Gate verdict

- **No-transformer regression:** the controlled #151/#152 gates measured 11.7% and 8.2% improvements with 1,808 B/op unchanged. Issue #154 changes no production runtime code, but a complete same-session rerun of the expanded matrix was not obtained.
- **Enabled-chain overhead:** the fresh bound-versus-direct run stayed below 10% for every measured case, consistent with the controlled #151/#152 results. Its absolute timing remains informational because the host was shared.
- **Allocation:** accepted. Identity chains add no context or chain allocation, and transformed containers allocate one output container for the complete chain.
- **Warmed lookup:** accepted by tests proving zero Spring bean-factory and portable-registry lookups after plan binding.
- **Concurrency:** accepted for deterministic correctness and allocation stability. Quantitative contention verification is deferred because the shared host invalidated the 8-thread timing comparison.
- **Speculative optimization:** none added. The measurements do not justify `MethodHandle`, bytecode generation, pooling, or further dispatch specialization.

Decision: functional integration, OpenAPI behavior, documentation, allocation, direct-dispatch overhead, and lookup gates pass. Complete same-session baseline/final and quantitative contention sign-off are deferred; the user stopped further long-running measurements because the CPU is shared with other important processes. No production optimization was made from the noisy data.

### Java 21 rerun and three-transformer optimization

A subsequent run on an Intel Core i7-7700HQ (4 cores / 8 logical CPUs) with Temurin 21.0.8+9 completed all 389 functional tests and the complete 1-thread and 8-thread matrices. Baseline and final measurements used the same JVM, heap, GC profiler, warmup, measurement, and fork parameters listed above. Artifacts are retained in `/tmp/performance-benchmark/issue-154-rerun/`.

The longer bound-versus-direct confirmation run (5 x 500 ms warmup, 10 x 500 ms measurement, 3 forks) found repeatable overhead above the 10% gate for three-transformer chains. This justified a narrowly scoped, benchmark-driven optimization that unrolls exactly that chain length while retaining the same null checks and exception translation. The optimized run produced:

```shell
java -Xms1g -Xmx1g -cp "$CP" org.openjdk.jmh.Main \
  'ScalarFilterValueTransformerBenchmark|MultiValueFilterValueTransformerBenchmark' \
  -wi 5 -i 10 -f 3 -w 500ms -r 500ms -t 1 -prof gc -tu ns -rf json \
  -rff /tmp/performance-benchmark/issue-154-rerun/direct-{confirm|optimized}-t1.json
```

| Three-transformer benchmark | Before ns/op | After ns/op | Improvement |
|---|---:|---:|---:|
| Scalar identity | 4.898 +/- 0.049 | 3.179 +/- 0.018 | 35.1% |
| Scalar replacement | 8.895 +/- 0.059 | 7.585 +/- 0.062 | 14.7% |
| Normal container | 56.554 +/- 1.921 | 44.646 +/- 1.647 | 21.1% |
| Dynamic container | 54.523 +/- 0.388 | 37.953 +/- 0.306 | 30.4% |

| Threads | Scenario | 1 transformer overhead | 3 transformers overhead | Bound/direct B/op |
|---:|---|---:|---:|---:|
| 1 | Scalar identity | -29.3% | -24.8% | 0 / 0 |
| 1 | Scalar replacement | -5.8% | -2.5% | 16 / 16; 48 / 48 |
| 1 | Normal container | 9.1% | 4.2% | 96 / 96; 224 / 224 |
| 1 | Dynamic container | 8.6% | -13.4% | 96 / 96; 224 / 224 |
| 8 | Scalar identity | -29.9% | -35.3% | 0 / 0 |
| 8 | Scalar replacement | -0.2% | 0.5% | 16 / 16; 48 / 48 |
| 8 | Normal container | 0.1% | -7.6% | 96 / 96; 224 / 224 |
| 8 | Dynamic container | 0.8% | -0.3% | 96 / 96; 224 / 224 |

The three-transformer optimization improved its bound-chain benchmarks by 14.7% to 35.1% at one thread. Maximum framework overhead is now 9.1% at one thread and 0.8% at eight threads. Allocation remains identical to direct calls, and the eight-thread results do not show increasing plan contention.

The warmed 1-thread end-to-end allocation matrix remained deterministic:

| Flow | Identity 0 / 1 / 3 B/op | Replacement 0 / 1 / 3 B/op |
|---|---:|---:|
| Normal scalar | 296 / 296 / 296 | 296 / 312 / 344 |
| Normal container | 296 / 328 / 328 | 296 / 392 / 520 |
| Dynamic scalar | 368 / 368 / 368 | 368 / 384 / 416 |
| Dynamic container | 400 / 400 / 400 | 400 / 464 / 592 |

Sequential end-to-end baseline/final latency scores drifted by up to 12.8% even for duplicate zero-transformer parameter cells executing identical bytecode. Those absolute score deltas are therefore not used to infer a regression. The controlled no-transformer measurements from #151/#152 remain the latency gate: 11.7% and 8.2% faster with 1,808 B/op unchanged. The same-session rerun independently confirms zero additional bytes in every warmed no-transformer flow.

Decision: accept the measured three-transformer optimization. Functional, allocation, direct-dispatch, warmed-lookup, and concurrency gates pass on Java 21. No `MethodHandle`, bytecode generation, pooling, or other speculative mechanism was added.
