# Performance History

## 2026-07-28 - Issue 80 Collection Operations Baseline

Purpose: baseline for the unified collection language runtime gate. The benchmark compiles each expression once in `@Setup(Level.Trial)` and measures hot `CompiledExpression.compute()` execution through resolved Operacao de Colecao runtime plans.

Command used:

```bash
mvn -pl exp-mk3 -am -DskipTests test-compile
mvn -q -pl exp-mk3 dependency:build-classpath -Dmdep.outputFile="exp-mk3/target/jmh-cp.txt" -DincludeScope=test
java -cp "runestone-toolkit/target/classes:exp-mk3/target/test-classes:exp-mk3/target/classes:$(tr -d '\n' < exp-mk3/target/jmh-cp.txt)" \
  org.openjdk.jmh.Main "CollectionOperationsBenchmark" \
  -wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns \
  -jvmArgs "-Xms1g -Xmx1g" -prof gc \
  -rf json -rff "/tmp/performance-benchmark/exp-mk3-collection-operations-issue-80.json" \
  -foe true
```

The explicit `runestone-toolkit/target/classes` classpath entry keeps the benchmark on the same reactor build as the tests. Running the module-local JMH helper without this entry resolved an older local Maven artifact and failed before measurement.

Environment:

- JDK: Temurin OpenJDK 21.0.6+7-LTS
- JMH: 1.37
- OS: Linux 6.18.33.2-microsoft-standard-WSL2 amd64
- JVM args: `-Xms1g -Xmx1g`
- Warmup: 5 iterations, 500 ms each
- Measurement: 10 iterations, 500 ms each
- Forks: 3
- Profiler: `gc`

Results:

| Benchmark | Score | Error | Units | B/op |
|---|---:|---:|---|---:|
| `allShortCircuit` | 161.00 | 13.54 | ns/op | 304.0 |
| `map` | 316.09 | 24.54 | ns/op | 472.0 |
| `mapThenSum` | 356.52 | 14.49 | ns/op | 664.0 |
| `reduce` | 369.42 | 38.55 | ns/op | 648.0 |
| `safeCall` | 302.62 | 25.81 | ns/op | 464.0 |
| `sortBy` | 471.30 | 38.35 | ns/op | 776.0 |
| `sum` | 212.41 | 9.35 | ns/op | 472.0 |
| `wildcardMaterialization` | 208.28 | 12.56 | ns/op | 453.3 |

Verdict: baseline recorded. This run is not a before/after optimization comparison; it is the reproducible baseline for future specialized collection-operation work. The measured expressions exercise compiled plans and do not intentionally include runtime reflection or textual operation lookup in the benchmark body.
