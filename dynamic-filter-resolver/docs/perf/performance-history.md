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
