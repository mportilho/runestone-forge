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

## 2026-07-29 - Issue 85 OperationIdentity Registry Collapse

Purpose: verify the runtime-dispatch impact of collapsing the three hand-synchronized `OperationIdentity` switches (`SemanticResolver.supportedOperation`, the `ExpressionRuntime.executeCollectionOperation` dispatch switch, and the duplicated empty-average rule) into a `CollectionOperationWiring` table (`internal.semantics`) plus a `CollectionOperationExecutors` registry of `CollectionOperationExecutor` method-reference lambdas (`internal.runtime`). The executor is resolved once per compiled navigation link in `ExecutionPlanBuilder` (plan-build time), not per `compute()` call, specifically to avoid a per-evaluation registry lookup.

Before/after used the same `CollectionOperationsBenchmark` from the issue-80 baseline above (git-stashed the issue-85 change to capture "before" on the same working tree, then popped it for "after").

Command used:

```bash
mvn -q -N install
mvn -q -pl exp-mk3 -am install -DskipTests
<java-performance-skill>/scripts/run-jmh.sh exp-mk3 "CollectionOperationsBenchmark" /tmp/performance-benchmark/before.json   # switch-based dispatch (stashed)
<java-performance-skill>/scripts/run-jmh.sh exp-mk3 "CollectionOperationsBenchmark" /tmp/performance-benchmark/after.json    # registry-based dispatch
python3 <java-performance-skill>/scripts/compare-results.py before.json after.json comparison.md
```

Environment: same as the issue-80 baseline (JDK 21.0.6, JMH 1.37, WSL2, `-Xms1g -Xmx1g`, 5×500ms warmup, 10×500ms measurement, 3 forks, `gc` profiler).

Results:

| Benchmark | Before (ns/op) | After (ns/op) | Delta | B/op before → after |
|---|---:|---:|---:|---|
| `allShortCircuit` | 97.7 ± 1.6 | 100.0 ± 1.6 | -2.30% | 296 → 261 |
| `map` | 168.2 ± 5.4 | 166.1 ± 2.7 | +1.23% | 464 → 488 |
| `mapThenSum` | 189.2 ± 1.9 | 194.5 ± 5.6 | -2.79% | 680 → 664 |
| `reduce` | 204.4 ± 1.5 | 210.8 ± 3.7 | -3.13% | 648 → 648 |
| `safeCall` | 165.3 ± 4.0 | 167.1 ± 2.1 | -1.03% | 480 → 480 |
| `sortBy` | 239.1 ± 5.2 | 251.5 ± 6.6 | -5.19% | 744 → 792 |
| `sum` | 108.3 ± 1.9 | 108.7 ± 1.8 | -0.40% | 456 → 395 |
| `wildcardMaterialization` | 124.4 ± 7.0 | 117.5 ± 1.7 | +5.57% | 461 → 480 |

Verdict per the skill's lower-is-better thresholds: **6 of 8 benchmarks land outside the ±1% noise band, all as regressions** (`allShortCircuit`, `mapThenSum`, `reduce`, `safeCall`, `sortBy` beyond -1%; `sum` inside noise). Mechanical `run-jmh.sh`/`compare-results.py` output classifies this **DISCARD** on pure ns/op grounds.

This is a real, small (~1-5%) per-call regression, most likely the extra indirection of invoking a captured `CollectionOperationExecutor` lambda instance instead of a direct call inside a `tableswitch`-compiled `switch` expression, even though the executor lookup itself was moved to plan-build time to avoid a per-`compute()` registry hit. The regression is a documented, deliberate trade for the issue's explicit ask: a single source of truth for "which `OperationIdentity` is accepted at which layer," replacing three independently hand-synchronized switches that could silently disagree. No further optimization attempted in this pass (e.g. dispatching through an array indexed by `identity.ordinal()` instead of an `EnumMap` + lambda might close some of the gap) — flagged here for a follow-up if the regression proves unacceptable under sustained load.
