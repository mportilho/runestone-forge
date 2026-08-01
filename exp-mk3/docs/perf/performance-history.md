# Performance History

## 2026-08-01 - Issue 105 Phase 5 JMH Baseline

Purpose: record a reproducible Phase 5 baseline — not a before/after optimization claim and not a
CI performance gate — for hot arithmetic-view compute, hot logical-view compute, full uncached
compilation, and fixed-size Public Materialization, using the final result-oriented compiler and
view APIs (`ExpressionCompiler`, `MathExpression`, `LogicalExpression`, `ResultExpression`).

Benchmark: `Phase5BaselineBenchmark` (`exp-mk3/src/test/java/com/runestone/expeval_mk3/perf/jmh`).

Four cases:

- `arithmeticCompute` — `a + b * 2` compiled once in `@Setup(Level.Trial)` to a `MathExpression`,
  then `compute(Map.of("a", 3, "b", 5))` measured per operation (hot path with runtime overrides).
- `logicalCompute` — `(a > 0) xor (b > 0) xor c` compiled once to a `LogicalExpression`, then
  `compute(overrides)` measured per operation. `xor` never short-circuits, so every operand is
  evaluated on every call, unlike `and`/`or`.
- `fullUncachedCompilation` — `ExpressionCompiler.compile("a + b * 2", environment)` executed in
  full (parsing, AST construction, semantic resolution, planning, result construction) on every
  measured operation. No compiled artifact is reused across invocations; only the environment and
  source string are built outside the measured method, in `@Setup(Level.Trial)`.
- `materializationCompute` — `[1, 2, 3, 4, 5, 6, 7, 8]` compiled once to a `ResultExpression`, then
  `compute()` measured per operation, exercising `PublicMaterialization`'s defensive-copy path over
  a fixed-size (8-element) list on every call.

All benchmark results are consumed through a JMH `Blackhole` so the JVM cannot eliminate the work.
The existing cold-parsing `ParsingBenchmark#coldParser` `SingleShot` measurement (startup
characterization) remains separate and is not folded into this warmed, per-operation baseline.

Command used:

```bash
mvn -q -N install
mvn -q -pl runestone-toolkit -am install -DskipTests
mvn -q -pl exp-mk3 -am -DskipTests test-compile
cd exp-mk3 && mvn -q dependency:build-classpath -Dmdep.outputFile="target/jmh-cp.txt" -DincludeScope=test && cd ..
java -cp "runestone-toolkit/target/classes:exp-mk3/target/test-classes:exp-mk3/target/classes:$(tr -d '\n' < exp-mk3/target/jmh-cp.txt)" \
  org.openjdk.jmh.Main "Phase5BaselineBenchmark" \
  -wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns \
  -jvmArgs "-Xms1g -Xmx1g" -prof gc \
  -rf json -rff "/tmp/performance-benchmark/exp-mk3-phase5-baseline-issue-105.json" \
  -foe true
```

Environment:

- Commit: `c600db38d01028f7ea6a3f76d7a415bbfb3a5aa3`
- JDK: OpenJDK 26.0.1 (Homebrew build, mixed mode, sharing)
- JMH: 1.37
- OS: Linux 7.0.0-28-generic x86_64
- JVM args: `-Xms1g -Xmx1g`
- Warmup: 5 iterations, 500 ms each
- Measurement: 10 iterations, 500 ms each
- Forks: 3
- Profiler: `gc`

Results:

| Benchmark | Score | Error | Units | B/op |
|---|---:|---:|---|---:|
| `arithmeticCompute` | 174.96 | 3.33 | ns/op | 216.0 |
| `logicalCompute` | 220.68 | 25.31 | ns/op | 208.0 |
| `fullUncachedCompilation` | 9008.50 | 359.04 | ns/op | 16393.7 |
| `materializationCompute` | 157.56 | 7.97 | ns/op | 386.7 |

Verdict: baseline recorded, all four cases measured from the same run protocol and environment so
the table is internally comparable. `fullUncachedCompilation` at ~9.0 µs/op is roughly two orders
of magnitude above the ~150-220 ns/op hot-path cases, consistent with running the whole compile
pipeline (parse, AST build, semantic resolution, planning, result construction) per operation
rather than hitting a cached artifact. No pass/fail threshold or CI gate is attached to these
numbers; this is a reference point for later Phase 5/6 work. Generated JSON was inspected during
analysis and is not versioned here.

`mvn -pl exp-mk3 -am test` was run and green (647 tests, 0 failures/errors) both before this
benchmark was added and again after, confirming the functional/concurrency gate was unaffected.

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
