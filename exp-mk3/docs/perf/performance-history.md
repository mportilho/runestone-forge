# Performance History

## 2026-08-30 - Comparacao de runtime com expression-evaluator

Purpose: compare steady-state execution of `exp-mk3` and the previous `expression-evaluator` with
the same expressions, inputs, registered functions, and registered object types. Compilation and
environment construction are outside the measured path. The benchmark setup checks that both
engines produce equivalent results before each fork starts measuring.

Environment: Eclipse Temurin 21.0.8+9-LTS; Maven 3.9.16; JMH 1.37; Linux x86_64; Intel Core
i7-7700HQ; `-Xms1g -Xmx1g`; one thread; three forks; 5x500 ms warmup and 10x500 ms measurement;
99.9% confidence intervals; GC profiler enabled; commit `69a3e01` plus the comparison fixture.

Results are `ns/op +/- 99.9% CI`; lower is better. Performance delta is
`(legacy - MK3) / legacy`, so a positive value means MK3 is faster. Allocation delta shows the
relative increase in MK3 bytes per operation.

| Scenario | Previous module | MK3 | MK3 performance | Previous B/op | MK3 B/op | MK3 allocation |
|---|---:|---:|---:|---:|---:|---:|
| 12 variables | 666.96 +/- 13.49 | 1,148.34 +/- 20.30 | -72.18% | 704 | 1,280 | +81.82% |
| 4 function calls, arity 3 | 609.00 +/- 11.16 | 1,127.14 +/- 21.26 | -85.08% | 544 | 1,112 | +104.41% |
| 3-level object navigation | 132.21 +/- 4.17 | 86.90 +/- 1.17 | +34.27% | 88 | 112 | +27.27% |

Verdict: **MIXED.** MK3 is 1.52x as fast for registered object navigation, but has 1.72x the
latency for the multiple-variable expression and 1.85x for the function-call expression. It
allocates more in all three scenarios. These results characterize the selected hot execution paths
only and do not compare compilation/startup time.

Benchmark: `com.runestone.expeval_mk3.perf.jmh.LegacyComparisonBenchmark`.

Command: `run-jmh.sh exp-mk3 LegacyComparisonBenchmark /tmp/performance-benchmark/legacy-comparison.json`.

### Root-cause controls

The same plans were measured both with no override map and with a 12-entry override map containing
exactly the same values as the defaults. This holds arithmetic and function results constant and
isolates runtime input preparation. Results use the same three-fork standard protocol as the main
comparison.

| Scenario | Engine | No overrides (ns/op / B/op) | Same values through overrides | Override cost |
|---|---|---:|---:|---:|
| 12 variables | Previous | 416.96 / 40 | 671.96 / 104 | +255.00 ns / +64 B |
| 12 variables | MK3 | 149.50 / 112 | 1,081.05 / 680 | +931.56 ns / +568 B |
| 4 function calls | Previous | 342.70 / 480 | 609.42 / 544 | +266.72 ns / +64 B |
| 4 function calls | MK3 | 176.07 / 544 | 1,116.98 / 1,112 | +940.90 ns / +568 B |

Without overrides, MK3 is 64.15% faster for the arithmetic plan and 48.62% faster for the function
plan. Supplying the equivalent map adds approximately 675 ns and 504 B/op more to MK3 than to the
previous evaluator in both controls, which is sufficient to reverse both comparisons.

The cause is `ExecutionPlan.executeAssignments`: MK3 first scans every override key to reject unknown
names, then scans every declared symbol and performs `containsKey`, `get`, coercion, and a binding-map
lookup. The previous evaluator scans the supplied entries once and performs one binding lookup per
entry. In addition, `BoundaryCoercion.convertOverride` eagerly builds `"external symbol '<name>' override"`
for every successful exact-type conversion. JFR allocation sampling for the MK3 arithmetic path
attributed 46.86% of allocation pressure to `byte[]` created through `StringConcatHelper`, 21.11% to
`Object[]`, and 0.97% to `ArrayList` iterators; the corresponding legacy profile was dominated by
`BigDecimal` (85.28%) and its one override array (12.27%), with no string-concatenation site present.

Conclusion: the regressions are in named override validation/preparation, not in arithmetic node
execution or function dispatch. The first optimization candidate is to remove eager success-path
diagnostic-string construction; the larger CPU opportunity is to reduce the two-pass, multi-lookup
override binding while preserving deterministic unknown-symbol diagnostics and overwrite validation.

### Override preparation optimization

The proposed changes were implemented and measured against a fresh same-session baseline. Exact-type
scalar and object overrides now bypass diagnostic-label construction. Plans where every declared
external symbol has a frame slot stage raw overrides directly in the cloned frame, validate unknown
names in one map pass, and then coerce in canonical symbol order. This preserves deterministic
diagnostics and all-or-nothing execution without a per-call staging array. Empty override maps skip
override traversal entirely. Plans containing declared-but-unused symbols retain the general path.

| MK3 benchmark | Before (ns/op / B/op) | After (ns/op / B/op) | Latency gain | Allocation change |
|---|---:|---:|---:|---:|
| 12 variables, changing overrides | 1,145.30 / 1,288 | 509.69 / 696 | +55.50% | -592 B |
| 12 variables, default-value overrides | 1,088.79 / 680 | 461.31 / 96 | +57.63% | -584 B |
| 12 variables, no overrides | 144.16 / 104 | 120.19 / 96 | +16.63% | -8 B |
| 4 function calls, changing overrides | 1,131.61 / 1,128 | 477.24 / 536 | +57.83% | -592 B |
| 4 function calls, default-value overrides | 1,054.09 / 1,120 | 477.08 / 536 | +54.74% | -584 B |
| 4 function calls, no overrides | 169.09 / 544 | 145.56 / 536 | +13.91% | -8 B |

The isolated cost of supplying the 12-entry default-value map fell from approximately 945 ns and
576 B/op to 341 ns and effectively 0 B/op for arithmetic, and from 885 ns and 576 B/op to 332 ns and
effectively 0 B/op for function calls. This confirms both causes identified above.

A fresh post-change paired comparison reverses the original result:

| Scenario | Previous module (ns/op / B/op) | Optimized MK3 (ns/op / B/op) | MK3 latency |
|---|---:|---:|---:|
| 12 variables | 663.82 / 704 | 462.91 / 696 | +30.27% faster |
| 4 function calls, arity 3 | 594.97 / 544 | 476.77 / 536 | +19.87% faster |

Verdict: **ACCEPT.** Every changed-path benchmark improved by more than 10%, MK3 is now faster than
the previous evaluator in both formerly regressing scenarios, and it allocates 8 B/op less in each.
Raw results: `/tmp/performance-benchmark/mk3-overrides-before-2026-08-30.json`,
`/tmp/performance-benchmark/mk3-overrides-after-2026-08-30.json`, and
`/tmp/performance-benchmark/legacy-comparison-optimized-2026-08-30.json`.

## 2026-08-30 - Etapa 10 Deployment Java 21 Software Verdict (issue #147)

Purpose: repeat the binding Etapa 10 verdict on the deployment JVM and prepare the implementation,
architecture, and public contract for closure. This is the **production implementation result**. The issue #139
fixture remains pre-production evidence and the issue #146 entry below remains the historical local
gate; neither substitutes for this repetition against the current tree.

Environment: Eclipse Temurin 21.0.8+9-LTS for Maven, both baseline/candidate builds, the JMH launcher,
and every fork; Maven 3.9.16; JMH 1.37; Linux x86_64; `-Xms1g -Xmx1g`; one thread; three forks,
3x250 ms warmup and 5x250 ms measurement unless a command below says otherwise. JMH reports 99.9%
confidence intervals. Explicit `JAVA_HOME`, `PATH`, `-jvm`, and Java executable arguments prevent the
host's unrelated default Java 26 executable from entering any measured process.

### Production result

Results are `ns/op +/- 99.9% CI / B/op`:

| Production shape | Normal result (`compute`) | Memory result (`computeWithMemory`) | Memory plus indexed sink |
|---|---:|---:|---:|
| empty | 18.52 +/- 2.24 / 24 | 41.79 +/- 0.82 / 104 | 53.68 +/- 1.70 / 104 |
| dense, 4/4 | 80.76 +/- 14.71 / 56 | 134.77 +/- 10.26 / 200 | 214.08 +/- 39.79 / 200 |
| prefix, 1/4 | 44.22 +/- 4.53 / 56 | 91.95 +/- 3.17 / 224 | 118.93 +/- 2.94 / 224 |
| alternating, 4/8 | 73.55 +/- 1.35 / 136 | 180.27 +/- 3.16 / 408 | 255.40 +/- 14.23 / 408 |
| sparse, 1/8 | 31.08 +/- 1.62 / 56 | 101.02 +/- 3.44 / 312 | 144.23 +/- 2.81 / 312 |
| opaque repeated, 0/0 | 346.15 +/- 49.07 / 392 | 383.31 +/- 51.63 / 472 | 573.66 +/- 101.51 / 472 |

The indexed sink adds less than `0.002 B/op` in every scenario, the GC-profiler measurement floor, and
therefore zero evaluator allocation. It reads every variable name/origin, calculation node ID, complete
source span, kind, name, and value. The result column includes ordinary result and working-frame
allocation. The memory column additionally includes execution-local working recorder allocation, the
final envelope, exact retained value columns, and the exact ordinal sidecar only for gapped reachability.
Materializacao Publica still occurs once before freeze and does not recursively copy captured values.

| Consumption shape | Projection only | Indexed | Lists by `get` | Lists with iterators |
|---|---:|---:|---:|---:|
| empty | 3.83 +/- 0.08 / 24 | 9.46 +/- 0.42 / **0** | 10.54 +/- 0.33 / 24 | 17.83 +/- 0.39 / 80 |
| dense | 7.44 +/- 0.10 / 48 | 41.81 +/- 4.10 / **0** | 48.14 +/- 0.67 / 120 | 70.83 +/- 1.89 / 232 |
| prefix | 7.40 +/- 0.09 / 48 | 17.62 +/- 0.53 / **0** | 18.46 +/- 0.30 / 48 | 35.38 +/- 0.56 / 160 |
| alternating | 7.41 +/- 0.08 / 48 | 49.07 +/- 1.24 / **0** | 58.87 +/- 0.71 / 144 | 81.70 +/- 0.85 / 256 |
| sparse | 7.41 +/- 0.10 / 48 | 26.81 +/- 0.35 / **0** | 28.90 +/- 0.53 / 72 | 46.36 +/- 1.12 / 184 |
| opaque repeated | 3.84 +/- 0.07 / 24 | 55.56 +/- 0.92 / **0** | 69.15 +/- 1.27 / 24 | 61.77 +/- 4.20 / 80 |

The list columns deliberately attribute projection, transient entry, and iterator allocation to the
consumer rather than to indexed persistence. JFR samples for normal execution contain only the ordinary
`ExecutionScope` and frame. Memory execution adds `CalculationRecorder`, its working value array,
`DefaultCalculationMemory`, and exact payload arrays. Indexed traversal samples no evaluator class.
No view, entry, reconstructed key, map, lambda, builder, or transfer holder appears in that path.

### Phase, storage, branch, and retention gates

The independent phase fixture measured existing Materializacao Publica at
`20.59 +/- 0.31 ns / 104 B`. Across the representative matrix, append capture ranged from
`9.58 +/- 0.16 ns / 56 B` with no points to `41.82 +/- 0.87 ns / 128 B` for dense assignments;
append freeze ranged from `14.05 +/- 0.45 ns / 56 B` to `39.54 +/- 0.84 ns / 128 B`. Required lazy
gapped capture/freeze measured `40.82 +/- 1.07 ns / 176 B` and
`33.17 +/- 0.81 ns / 128 B`. These are working-allocation and freeze figures, separate from the normal
result, retained payload, consumer projections, and sink.

The complete `capture -> materialize -> freeze -> indexed sink` control preserves the storage verdict.
Append beats frame-tail for the dominant one-point shape (`64.44 +/- 1.40 ns / 208 B` versus
`72.68 +/- 3.22 ns / 216 B`), current maximum dense shape (`150.03 +/- 7.11 / 224` versus
`155.83 +/- 8.98 / 232`), required nested dense (`93.09 +/- 1.03 / 224` versus
`106.37 +/- 2.60 / 240`), and dense assignments (`192.45 +/- 26.34 / 240` versus
`225.68 +/- 32.26 / 272`). Frame-tail remains the working-allocation side of the Pareto frontier for
gapped shapes: required lazy gapped measured `163.92 +/- 5.61 / 336` against append
`170.14 +/- 2.56 / 408`. Retained payloads remain identical and columnar, so current-corpus prevalence
keeps append-only as production storage and frame-tail only as the documented control.

The deployment-JVM publication comparison preserves the columnar verdict across all 11 representative
shapes: eager entries were slower and allocated more than frame-tail columnar in every shape. For dense
`S=4`,
Append-columnar measured `78.45 +/- 1.24 ns / 352 B`, frame-tail columnar
`95.50 +/- 2.37 ns / 368 B`, and frame-tail eager entries `153.31 +/- 5.41 ns / 672 B`.
Prebuilt columnar and eager sequential sinks both allocate zero at the evaluator boundary; eager is
slightly faster there (`88.70 +/- 1.80` versus `94.27 +/- 2.55 ns`), but its eager publication cost and
allocation leave exact columnar publication Pareto-winning for the complete flow.

Mode-first remains the winning inactive branch shape. For one markable node it measured
`1.476 +/- 0.025 ns` against slot-first `1.647 +/- 0.036` and fused absolute-slot
`1.658 +/- 0.022`; for eight opaque descendants it measured `3.336 +/- 0.055` against
`4.550 +/- 0.108` and `4.518 +/- 0.068`. Active forms were equivalent at about 2.69 ns for one point
and 4.53-4.56 ns for opaque descendants, with maximum reported error of 0.10 ns. Count-during-capture remains preferable at
`32.27 +/- 0.58 ns / 152 B` versus count-during-freeze at `34.10 +/- 0.63 ns / 152 B`.
`PrintInlining` confirms hot inlining of `ExecutionPlan.compute`, `FunctionCallExecutableNode.execute`,
and `ExecutionScope.captureCalculation` on Temurin 21.

The paired pre-capture baseline at commit `9bb5bf9` and current candidate used independent Java 21
builds and identical 5x500 ms warmup, 10x500 ms measurement, and three-fork settings. Two complete
runs measured `64.48 +/- 1.38` and `62.94 +/- 0.74 ns/op` at baseline versus
`67.90 +/- 1.12` and `68.59 +/- 1.95 ns/op` for the candidate: reproducible increases of 5.3% and
9.0%. Allocation is unchanged at 104.001 B/op, proving zero B/op added to normal `compute()`. The
latency exception is accepted rather than hidden: it is the mandatory inactive recorder test on a
marked node; the synthetic comparison above selects mode-first, the smaller-helper experiment recorded
under issue #146 was slower, and removing the test would require a second immutable plan contrary to
ADR 0023.

JOL repeated the exact production layout on this JVM: `ExecutionScope`, `CalculationRecorder`, and
`DefaultCalculationMemory` are 32 bytes each; dense/prefix/gapped retained memory graphs are respectively
800/408/680 bytes; 32 memories sharing one plan retain 4,872 bytes versus 9,088 bytes for one memory per
plan. Exact final arrays and the gapped-only ordinal sidecar remain within budget. JOL could not attach
Instrumentation or the Serviceability Agent, so compressed-reference details are inferred as in issue
#145. Hardware counters remain unavailable on this deployment host because `perf_event_paranoid=4` and
the process lacks `CAP_PERFMON`; both JMH `perfnorm` and direct `perf stat` failed. No branches/op or
branch-misses/op claim is inferred from latency.

Final verdict: **CONDITIONAL ACCEPT; hardware-counter gate pending.** Every executable Java 21 gate on
this host supports append-only capture, mode-first branching, count-during-capture, exact columnar
publication, and frame-tail's control-only status, but issue #147 cannot be closed until branches/op and
branch-misses/op run on a host with perf-event access. The suite passed
1,511 tests with 50 skipped, zero failures, and zero errors (`runestone-toolkit`: 343/0/0/0;
`exp-mk3`: 1,168/50/0/0). The skip count is the existing unsupported-oracle subset in the corpus gate.

Commands:

```bash
export JAVA_HOME=/home/marcelo/.sdkman/candidates/java/21.0.8-tem
export PATH="$JAVA_HOME/bin:$PATH"
mkdir -p /tmp/opencode/issue-147
mvn -pl exp-mk3 -am test
mvn -pl exp-mk3 -am -DskipTests test-compile
mvn -q -pl exp-mk3 dependency:build-classpath \
  -Dmdep.outputFile=target/jmh-cp.txt -DincludeScope=test
JMH_CP="runestone-toolkit/target/classes:exp-mk3/target/test-classes:exp-mk3/target/classes:$(tr -d '\n' < exp-mk3/target/jmh-cp.txt)"

"$JAVA_HOME/bin/java" -cp "$JMH_CP" org.openjdk.jmh.Main \
  'CalculationMemoryProductionBenchmark' -wi 3 -i 5 -w 250ms -r 250ms -f 3 -tu ns \
  -jvm "$JAVA_HOME/bin/java" -jvmArgs '-Xms1g -Xmx1g' -prof gc -rf json \
  -rff /tmp/opencode/issue-147/production-gates-java21.json -foe true

"$JAVA_HOME/bin/java" -cp "$JMH_CP" org.openjdk.jmh.Main \
  'CalculationMemoryStoragePrototypeBenchmark.representative(Frame|Append)Columnar' \
  -wi 3 -i 5 -w 250ms -r 250ms -f 3 -tu ns -jvm "$JAVA_HOME/bin/java" \
  -jvmArgs '-Xms1g -Xmx1g' -prof gc -rf json \
  -rff /tmp/opencode/issue-147/frame-append-control-java21.json -foe true

"$JAVA_HOME/bin/java" -cp "$JMH_CP" org.openjdk.jmh.Main \
  'CalculationMemoryStoragePrototypeBenchmark.(representativeAppendCapture|representativeAppendFreeze|materializePublicResult)$' \
  -p slotCount=4 -p reachability=DENSE -wi 3 -i 5 -w 250ms -r 250ms -f 3 -tu ns \
  -jvm "$JAVA_HOME/bin/java" -jvmArgs '-Xms1g -Xmx1g' -prof gc -rf json \
  -rff /tmp/opencode/issue-147/phase-attribution-java21.json -foe true

"$JAVA_HOME/bin/java" -cp "$JMH_CP" org.openjdk.jmh.Main \
  'CalculationMemoryStoragePrototypeBenchmark.branch(SlotFirst|ModeFirst|FusedAbsoluteSlot)$' \
  -wi 3 -i 5 -w 250ms -r 250ms -f 3 -tu ns -jvm "$JAVA_HOME/bin/java" \
  -jvmArgs '-Xms1g -Xmx1g' -prof gc -rf json \
  -rff /tmp/opencode/issue-147/branch-shapes-java21.json -foe true

"$JAVA_HOME/bin/java" -cp "$JMH_CP" org.openjdk.jmh.Main \
  'CalculationMemoryStoragePrototypeBenchmark.countDuring(Capture|Freeze)$' \
  -p slotCount=4 -p reachability=DENSE -wi 3 -i 5 -w 250ms -r 250ms -f 3 -tu ns \
  -jvm "$JAVA_HOME/bin/java" -jvmArgs '-Xms1g -Xmx1g' -prof gc -rf json \
  -rff /tmp/opencode/issue-147/count-strategy-java21.json -foe true

"$JAVA_HOME/bin/java" -cp "$JMH_CP" org.openjdk.jmh.Main \
  'CalculationMemoryStoragePrototypeBenchmark.(computeWithMemoryFrameColumnar|computeWithMemoryAppendColumnar|computeWithMemoryFrameEager|traverseIndexed|traverseLists|traverseEagerEntries|consumeColumnarSequentially|consumeEagerSequentially)$' \
  -p slotCount=4 -p reachability=DENSE -wi 3 -i 5 -w 250ms -r 250ms -f 3 -tu ns \
  -jvm "$JAVA_HOME/bin/java" -jvmArgs '-Xms1g -Xmx1g' -prof gc -rf json \
  -rff /tmp/opencode/issue-147/columnar-eager-java21.json -foe true

"$JAVA_HOME/bin/java" -cp "$JMH_CP" org.openjdk.jmh.Main \
  'CalculationMemoryStoragePrototypeBenchmark.representative(FrameColumnar|AppendColumnar|FrameEager)$' \
  -wi 3 -i 5 -w 250ms -r 250ms -f 3 -tu ns -jvm "$JAVA_HOME/bin/java" \
  -jvmArgs '-Xms1g -Xmx1g' -prof gc -rf json \
  -rff /tmp/opencode/issue-147/representative-publication-java21.json -foe true

# Run at 9bb5bf9 and the current tree, each compiled by the exported Maven/JDK.
git worktree add --detach /tmp/opencode/issue-147-baseline 9bb5bf9
cd /tmp/opencode/issue-147-baseline
mvn -pl exp-mk3 -am -DskipTests test-compile
mvn -q -pl exp-mk3 dependency:build-classpath \
  -Dmdep.outputFile=target/jmh-cp.txt -DincludeScope=test
JMH_CP="runestone-toolkit/target/classes:exp-mk3/target/test-classes:exp-mk3/target/classes:$(tr -d '\n' < exp-mk3/target/jmh-cp.txt)"
RESULT=/tmp/opencode/issue-147/baseline-function-java21.json
"$JAVA_HOME/bin/java" -cp "$JMH_CP" org.openjdk.jmh.Main \
  'FunctionInvocationBenchmark.arityOneOptimized' -wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns \
  -jvm "$JAVA_HOME/bin/java" -jvmArgs '-Xms1g -Xmx1g' -prof gc -rf json -rff "$RESULT" -foe true

cd /home/marcelo/dev/git/runestone-forge
mvn -pl exp-mk3 -am -DskipTests test-compile
mvn -q -pl exp-mk3 dependency:build-classpath \
  -Dmdep.outputFile=target/jmh-cp.txt -DincludeScope=test
JMH_CP="runestone-toolkit/target/classes:exp-mk3/target/test-classes:exp-mk3/target/classes:$(tr -d '\n' < exp-mk3/target/jmh-cp.txt)"
RESULT=/tmp/opencode/issue-147/candidate-function-java21.json
"$JAVA_HOME/bin/java" -cp "$JMH_CP" org.openjdk.jmh.Main \
  'FunctionInvocationBenchmark.arityOneOptimized' -wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns \
  -jvm "$JAVA_HOME/bin/java" -jvmArgs '-Xms1g -Xmx1g' -prof gc -rf json -rff "$RESULT" -foe true
git worktree remove /tmp/opencode/issue-147-baseline

"$JAVA_HOME/bin/java" -cp "$JMH_CP" org.openjdk.jmh.Main \
  'CalculationMemoryProductionBenchmark.(compute|computeWithMemory|traverseIndexed)$' \
  -p scenario=DENSE -wi 2 -i 3 -w 500ms -r 500ms -f 1 -tu ns \
  -jvm "$JAVA_HOME/bin/java" -jvmArgs '-Xms1g -Xmx1g' \
  -prof 'jfr:dir=/tmp/opencode/issue-147/jfr;configName=profile' -foe true

"$JAVA_HOME/bin/java" -cp "$JMH_CP" org.openjdk.jmh.Main \
  'CalculationMemoryProductionBenchmark.compute$' -p scenario=DENSE \
  -wi 2 -i 2 -w 250ms -r 250ms -f 1 -tu ns -jvm "$JAVA_HOME/bin/java" \
  -jvmArgs '-Xms1g -Xmx1g -XX:+UnlockDiagnosticVMOptions -XX:+PrintInlining' -foe true \
  > /tmp/opencode/issue-147/print-inlining-dense-java21.txt

"$JAVA_HOME/bin/java" -cp "$JMH_CP" org.openjdk.jmh.Main \
  'CalculationMemoryProductionBenchmark.(compute|computeWithMemory)$' \
  -p scenario=OPAQUE_REPEATED -wi 2 -i 3 -w 250ms -r 250ms -f 1 -tu ns \
  -jvm "$JAVA_HOME/bin/java" -jvmArgs '-Xms1g -Xmx1g' -prof perfnorm -foe true \
  > /tmp/opencode/issue-147/perfnorm-opaque-java21.txt 2>&1

"$JAVA_HOME/bin/java" --add-opens java.base/java.lang=ALL-UNNAMED \
  --add-opens java.base/java.util=ALL-UNNAMED \
  --add-opens java.base/java.lang.invoke=ALL-UNNAMED -cp "$JMH_CP" \
  com.runestone.expeval_mk3.perf.jmh.CalculationMemoryProductionLayoutReport

perf stat -e branches,branch-misses -- "$JAVA_HOME/bin/java" -version
git diff --check
```

The baseline and candidate commands were each repeated once with `RESULT` changed to
`baseline-function-rerun-java21.json` and `candidate-function-rerun-java21.json`. Benchmark artifacts:
`/tmp/opencode/issue-147/production-gates-java21.json`, `frame-append-control-java21.json`,
`phase-attribution-java21.json`, `branch-shapes-java21.json`, `count-strategy-java21.json`,
`columnar-eager-java21.json`, `representative-publication-java21.json`,
`{baseline,candidate}-function-java21.json`,
`{baseline,candidate}-function-rerun-java21.json`, `jfr/`,
`print-inlining-dense-java21.txt`, `perfnorm-opaque-java21.txt`, `perf-stat-java21.txt`, and
`jol-production-layout-java21.txt`.

## 2026-08-30 - Etapa 10 Final Local Performance and Allocation Gates (issue #146)

Purpose: close the local production gates for normal execution, calculation-memory publication, indexed
persistence, convenience-list traversal, and the retained frame-tail control. The production JMH now
keeps capture inactive on the same marked plan in `compute()`, measures `computeWithMemory()` before
consumption, consumes a prebuilt memory independently, and separates projection, indexed `List.get`, and
iterator costs. Its scenarios cover empty, dense, prefix, alternating, sparse, and eight repeated opaque
descendants. Every setup checks the expected reached-point count.

Environment: Eclipse Temurin 21.0.8+9-LTS, JMH 1.37, Linux x86_64, `-Xms1g -Xmx1g`, three forks,
3x250 ms warmup, 5x250 ms measurement, GC profiler, one thread. Scores below are `ns/op +/- 99.9% CI /
B/op`; allocation values below `0.01 B/op` are displayed as zero.

| Production shape | `compute` | `computeWithMemory` | Full indexed flow |
|---|---:|---:|---:|
| empty | 19.02 +/- 3.04 / 24 | 40.50 +/- 0.86 / 104 | 54.94 +/- 2.79 / 104 |
| dense, 4/4 | 81.06 +/- 13.91 / 56 | 131.78 +/- 7.04 / 200 | 210.44 +/- 38.86 / 200 |
| prefix, 1/4 | 40.72 +/- 3.78 / 56 | 91.23 +/- 2.82 / 224 | 115.32 +/- 2.84 / 224 |
| alternating, 4/8 | 72.31 +/- 2.81 / 136 | 179.52 +/- 4.50 / 408 | 258.49 +/- 10.65 / 408 |
| sparse, 1/8 | 29.16 +/- 0.59 / 56 | 105.26 +/- 22.81 / 312 | 142.21 +/- 1.62 / 312 |
| opaque repeated, 0/0 | 348.45 +/- 58.29 / 392 | 377.16 +/- 46.48 / 472 | 571.41 +/- 95.81 / 472 |

`compute()` has exactly the ordinary result and working-frame allocation and JFR records no
`CalculationRecorder`, `CalculationMemory`, view, entry, or provenance-key allocation. In particular,
the marked dense plan remains at 56 B/op with capture inactive. The opaque repeated setup verifies the
result-changing output of all eight `opaqueMark(@)` descendants while publishing no calculation,
confirming that collection bodies remain outside the observable frontier. `computeWithMemory()` performs
the one existing public materialization
before freeze; indexed consumption adds no bytes to any complete flow.

| Consumption shape | Projection only | Indexed traversal | Lists by `get` | Lists with iterators |
|---|---:|---:|---:|---:|
| empty | 3.81 +/- 0.06 / 24 | 9.83 +/- 0.18 / **0** | 10.49 +/- 0.18 / 24 | 17.60 +/- 0.25 / 80 |
| dense | 7.42 +/- 0.07 / 48 | 41.08 +/- 0.81 / **0** | 48.95 +/- 0.86 / 120 | 69.91 +/- 1.35 / 232 |
| prefix | 7.44 +/- 0.06 / 48 | 17.42 +/- 0.25 / **0** | 18.74 +/- 0.37 / 48 | 35.42 +/- 0.91 / 160 |
| alternating | 7.43 +/- 0.16 / 48 | 48.06 +/- 0.46 / **0** | 59.73 +/- 0.62 / 144 | 82.79 +/- 2.21 / 256 |
| sparse | 7.46 +/- 0.12 / 48 | 26.83 +/- 0.81 / **0** | 29.21 +/- 0.69 / 72 | 46.66 +/- 1.19 / 184 |
| opaque repeated | 3.82 +/- 0.06 / 24 | 56.82 +/- 2.69 / **0** | 68.78 +/- 1.48 / 24 | 62.19 +/- 4.59 / 80 |

The no-I/O indexed sink reads every variable name/origin, calculation node ID, source-span field,
kind, name, and value. JFR observes no evaluator allocation in that benchmark. List JFR attributes the
explicit costs to the two `DefaultCalculationMemory` projection classes and transient `VariableEntry` /
`CalculationEntry` records. The GC-profiler delta in the iterator column additionally measures the two
`AbstractList` iterators.

The first production run exposed an accidental eager validation-message allocation in
`DefaultCalculationMemory`: every successful variable check constructed one `String` and one `byte[]`.
An explicit null branch preserves the indexed error message while removing both objects. Dense
`computeWithMemory()` fell from 240 to 200 B/op; empty, prefix, alternating, sparse, and opaque cases each
fell by 40 B/op. Final JFR samples contain only the normal execution scope/frame plus the selected
append recorder/value array, exact variable/calculation columns, final memory, and result envelope.
There are no per-execution views, entries, key reconstructions, maps, lambdas, builders, or temporary
transfer holders. The prior deterministic graph/JOL gate, rather than the dense JFR sample, verifies
that gapped cases add only the designed exact ordinal sidecar.

Capture, existing Materializacao Publica, and freeze were also measured independently with the binding
fixture, rather than inferred from end-to-end subtraction. The public materialization control was
20.55 +/- 0.40 ns and 104 B/op. Append capture ranged from 9.66/56 for no points through 17.00/80 for the
current one-point shape to 42.14/128 for the required dense assignments shape, with latency errors of
0.19, 0.43, and 0.91 ns/op; freeze respectively measured 13.70/56, 15.37/56, and 23.61/72, with errors
of 0.21, 0.24, and 0.45 ns/op. The required lazy gapped control measured 40.94 +/- 0.88 ns and 176 B/op
for capture and 33.42 +/- 0.82 ns and 128 B/op for freeze. These fixture numbers attribute the phases;
the production table above remains the authoritative complete-flow result.

The normal marked-function path was additionally compared against commit `9bb5bf9`, immediately before
dynamic calculation points added the inactive capture branch, using the unchanged
`FunctionInvocationBenchmark.arityOneOptimized` on the same machine and JVM. Two complete runs measured
65.30 +/- 0.72 and 64.83 +/- 1.52 ns/op before versus 66.60 +/- 1.56 and 67.15 +/- 0.83 ns/op after,
with allocation unchanged at 104.0009 B/op in every run. The point deltas are +1.98% and +3.58%, while
the confidence bands touch or overlap at their boundary. Investigation isolated the mandatory null
recorder test; splitting it from replay/append into a smaller fast-path method worsened the result to
67.92 +/- 0.94 ns/op and was discarded. The retained implementation is therefore justified as the
smallest one-plan form: zero allocation delta, about 2 ns absolute measured cost, no statistically clean
separation at the 99.9% confidence level, and no faster branch shape found without violating the design.

The repeated Java 21 benchmark-fixture frame-tail control leaves the ADR 0023 decision unchanged. This
control models the complete `capture -> materialize -> freeze -> sink` flow with production-derived
shapes, but intentionally does not add a second storage strategy to the production runtime. Append wins
the current dominant one-point shape by 12.9% latency and 8 B/op (64.49 +/- 2.18 / 208 versus
74.08 +/- 1.25 / 216), the current dense two-point shape by 1.8% and 8 B/op
(154.70 +/- 8.22 / 224 versus 157.48 +/- 9.91 / 232), and required nested dense by 13.7% and 16 B/op
(92.30 +/- 3.52 / 224 versus 106.91 +/- 2.68 / 240). Frame-tail remains cheaper for gapped shapes (for
required lazy gapped, 162.05 +/- 7.37 / 336 versus 178.41 +/- 6.74 / 408), but does not overturn the
documented current-corpus tie-break and is not promoted to a second production representation.

`PrintInlining` confirms that `ExecutionPlan.compute`, `FunctionCallExecutableNode.execute`, and the
62-byte `ExecutionScope.captureCalculation` are hot-inlined in the marked dense normal path. Hardware
branch counters and `perfasm` could not run on this host: both JMH `perfnorm` and direct `perf stat`
reported no supported events because `perf_event_paranoid=4` and the process lacks `CAP_PERFMON`.
Consequently branches/op and branch-misses/op remain an explicit deployment-host limitation; no claim is
made from wall-clock timing in their place. The local verdict is **ACCEPT, provisional on repeating the
hardware-counter check in the deployment environment**. The quantitative allocation, JFR, JOL,
functional, and Java 21 compatibility gates pass locally.

Commands:

```bash
mvn -pl exp-mk3 -am -DskipTests test-compile
mvn -q -pl exp-mk3 dependency:build-classpath -Dmdep.outputFile=target/jmh-cp.txt -DincludeScope=test
JMH_CP="runestone-toolkit/target/classes:exp-mk3/target/test-classes:exp-mk3/target/classes:$(tr -d '\n' < exp-mk3/target/jmh-cp.txt)"

/home/marcelo/.sdkman/candidates/java/21.0.8-tem/bin/java -cp "$JMH_CP" org.openjdk.jmh.Main \
  'CalculationMemoryProductionBenchmark' -wi 3 -i 5 -w 250ms -r 250ms -f 3 -tu ns \
  -jvmArgs '-Xms1g -Xmx1g' -prof gc -rf json \
  -rff /tmp/opencode/issue-146/production-gates-final-java21.json -foe true

/home/marcelo/.sdkman/candidates/java/21.0.8-tem/bin/java -cp "$JMH_CP" org.openjdk.jmh.Main \
  'CalculationMemoryStoragePrototypeBenchmark.representative(Frame|Append)Columnar' \
  -wi 3 -i 5 -w 250ms -r 250ms -f 3 -tu ns -jvmArgs '-Xms1g -Xmx1g' -prof gc \
  -rf json -rff /tmp/opencode/issue-146/frame-append-control-java21.json -foe true

/home/marcelo/.sdkman/candidates/java/21.0.8-tem/bin/java -cp "$JMH_CP" org.openjdk.jmh.Main \
  'CalculationMemoryStoragePrototypeBenchmark.(representativeAppendCapture|representativeAppendFreeze|materializePublicResult)$' \
  -p slotCount=4 -p reachability=DENSE -wi 3 -i 5 -w 250ms -r 250ms -f 3 -tu ns \
  -jvmArgs '-Xms1g -Xmx1g' -prof gc -rf json \
  -rff /tmp/opencode/issue-146/phase-attribution-java21.json -foe true

/home/marcelo/.sdkman/candidates/java/21.0.8-tem/bin/java -cp "$JMH_CP" org.openjdk.jmh.Main \
  'CalculationMemoryProductionBenchmark.(computeWithMemory|traverseIndexed|traverseListsByIndex)$' \
  -p scenario=DENSE -wi 2 -i 3 -w 500ms -r 500ms -f 1 -tu ns \
  -jvmArgs '-Xms1g -Xmx1g' -prof 'jfr:dir=/tmp/opencode/issue-146/jfr;configName=profile' -foe true

/home/marcelo/.sdkman/candidates/java/21.0.8-tem/bin/java -cp "$JMH_CP" org.openjdk.jmh.Main \
  'CalculationMemoryProductionBenchmark.compute$' -p scenario=DENSE -wi 2 -i 2 \
  -w 250ms -r 250ms -f 1 -tu ns \
  -jvmArgs '-Xms1g -Xmx1g -XX:+UnlockDiagnosticVMOptions -XX:+PrintInlining' -foe true

# The same JFR profiler invocation was run separately for compute and computeWithMemory, with its
# dir changed to /tmp/opencode/issue-146/jfr-compute-normal and /tmp/opencode/issue-146/jfr-final.
# The opaque scenario was rerun after adding its result-changing setup assertion.
RESULT=/tmp/opencode/issue-146/opaque-final-java21.json
/home/marcelo/.sdkman/candidates/java/21.0.8-tem/bin/java -cp "$JMH_CP" org.openjdk.jmh.Main \
  'CalculationMemoryProductionBenchmark' -p scenario=OPAQUE_REPEATED \
  -wi 3 -i 5 -w 250ms -r 250ms -f 3 -tu ns -jvmArgs '-Xms1g -Xmx1g' -prof gc \
  -rf json -rff "$RESULT" -foe true

/home/marcelo/.sdkman/candidates/java/21.0.8-tem/bin/java -cp "$JMH_CP" org.openjdk.jmh.Main \
  'CalculationMemoryProductionBenchmark.(compute|computeWithMemory)$' -p scenario=OPAQUE_REPEATED \
  -wi 2 -i 3 -w 250ms -r 250ms -f 1 -tu ns -jvmArgs '-Xms1g -Xmx1g' -prof perfnorm -foe true

# Run from both a local clone at 9bb5bf9 and the current tree, preserving separate JSON files.
/home/marcelo/.sdkman/candidates/java/21.0.8-tem/bin/java -cp "$JMH_CP" org.openjdk.jmh.Main \
  'FunctionInvocationBenchmark.arityOneOptimized' -wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns \
  -jvmArgs '-Xms1g -Xmx1g' -prof gc -rf json -rff "$RESULT" -foe true

perf stat -e branches,branch-misses -- /home/marcelo/.sdkman/candidates/java/21.0.8-tem/bin/java -version
mvn -pl exp-mk3 -am test
```

Artifacts: `/tmp/opencode/issue-146/production-gates-final-java21.{json,txt}`,
`/tmp/opencode/issue-146/opaque-final-java21.{json,txt}`,
`/tmp/opencode/issue-146/frame-append-control-java21.{json,txt}`,
`/tmp/opencode/issue-146/phase-attribution-java21.{json,txt}`,
`/tmp/opencode/issue-146/{pre-capture-function,post-capture-function}-java21.{json,txt}` and their
`*-rerun-java21.{json,txt}` repeats,
`/tmp/opencode/issue-146/jfr-final/`, `/tmp/opencode/issue-146/jfr-compute-normal/`,
`/tmp/opencode/issue-146/print-inlining-dense-java21.txt`, and
`/tmp/opencode/issue-146/perfnorm-opaque-java21.txt`.

## 2026-08-30 - Etapa 10 Retention and Production Layout (issue #145)

Purpose: prove execution-local ownership and deterministic non-retention for `CalculationMemory`, then
measure the production node, schema, recorder, plan, and final-memory layouts. Functional gates inspect
the object graph by identity without `System.gc()` and cover concurrent success/failure, reentrant
`computeWithMemory()`, memo values, Item Atual restoration, exact columns, and uncached list projections.

Environment: Eclipse Temurin 21.0.8+9-LTS, JOL 0.17, Linux x86_64, 64-bit VM, compressed ordinary and
class references with 3-bit shifts, 8-byte object alignment. JOL could not attach Instrumentation or the
Serviceability Agent in this environment, so reference base/shift details are inferred; shallow sizes and
relative retained-footprint comparisons use the reported VM model. JVM module opens used for graph
inspection were `java.lang`, `java.util`, and `java.lang.invoke`.

| Shallow production object | Bytes |
|---|---:|
| function/property/current-temporal calculation node | 32-40 |
| registered method calculation node | 48 |
| memoized node | 40 |
| folded-provenance constant / static calculation group | 32 / 24 |
| execution scope / recorder / final memory | 32 each |
| calculation schema / variable schema | 24 each |

| Production graph | Instances | Bytes |
|---|---:|---:|
| Plan with 10 executable nodes | 10,793 | 411,032 |
| Plan with 100 executable nodes | 11,430 | 429,264 |
| Plan with 1,000 executable nodes | 15,030 | 531,264 |
| Empty memory | 4 | 88 |
| Dense memory, 3 calculations | 27 | 800 |
| Prefix memory, 1 of 2 calculations | 16 | 408 |
| Gapped memory, 2 of 3 calculations | 25 | 680 |
| 32 memories from distinct plans | 319 | 9,088 |
| 32 memories from one shared plan | 164 | 4,872 |

Verdict for issue #145's retention/layout gate: **ACCEPT.** Final memories retain exact value arrays,
add an exact ordinal sidecar only for gaps, and do not reach the plan, executable nodes, AST, semantic
model, environment, provider, source, execution scope, recorder, memo slots, or Item Atual slots. The
inverse graph proves a retained plan does not reach prior memories or override values. Sharing one plan
saves 4,216 retained bytes across 32 memories by sharing prebuilt keys; list projections occur only when
callers retain them and are not cached.
No unexpected retention or nonlinear plan growth was found.

Command: `MAVEN_OPTS='--add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED --add-opens java.base/java.lang.invoke=ALL-UNNAMED' mvn -q -pl exp-mk3 -Dexec.classpathScope=test -Dexec.mainClass=com.runestone.expeval_mk3.perf.jmh.CalculationMemoryProductionLayoutReport org.codehaus.mojo:exec-maven-plugin:3.5.0:java`.
Raw output: `/tmp/opencode/issue-145/jol-production-layout.txt`.

## 2026-08-26 - Etapa 10 Production Calculation Capture (issue #141)

Purpose: verify the selected append-only recorder in the real `ExecutionPlan.compute()` and
`computeWithMemory()` paths, including no-point, one-point dense, and leading-gap execution.

Environment: Eclipse Temurin 21.0.8+9-LTS, JMH 1.37, Linux x86_64, `-Xms1g -Xmx1g`, three forks,
5x500 ms warmup, 10x500 ms measurement, GC profiler.

Results (`ns/op / B/op`): normal `compute()` measured 15.41/24 (`S=0`), 29.49/56 (`S=1`), and
28.69/56 (leading-gap source). `computeWithMemory()` measured 49.83/144, 80.42/232, and 130.33/344,
respectively, including exact payload construction and indexed consumption. Normal execution uses no
recorder and does not extend the frame. Raw output:
`/tmp/opencode/issue-141/java21-production-path.json`.

## 2026-08-26 - Etapa 10 Capture-Storage Reconciliation (issue #155)

Purpose: resolve the frame-tail versus append-only decision reopened by issue #139 before calculation
capture enters production. The gate corrected append capacity so it depends only on static `S`, derived
`F/S` from 144 current corpus plans, and measured corpus and required lazy/dense shapes.

Environment: Eclipse Temurin 21.0.8+9-LTS, JMH 1.37, Linux x86_64, `-Xms1g -Xmx1g`. The final
end-to-end run used three forks, 5x500 ms warmup and 10x500 ms measurement; a no-fork run paired capture,
freeze and full flow in one JVM. Both used the GC profiler and JSON output.

Verdict: **ACCEPT append-only capture for production; retain frame-tail only as a benchmark control.**
The dominant current shape (`S=1`) improved end-to-end latency by 12.5% and saved 8 B/op; current dense
`S=2` improved by 5.6% and also saved 8 B/op. Gapped shapes favored frame-tail by 4.2-6.1% and used
48-72 fewer B/op. JOL retained graphs were identical. The explicit tie-break prioritizes
current-corpus end-to-end latency, then working allocation, and therefore selects append without claiming
universal dominance. Monotonic append remains valid under lazy reachability, folding groups,
per-occurrence CSE replay, and collection opacity. Columnar publication, mode-first branching, and
increment-during-capture counting remain accepted. Full rationale and results are in
`docs/planning/etapa-10/calculation-capture-storage-reconciliation.md`.

## 2026-08-19 - Etapa 10 Binding Persistable-Payload Gate (issue #139)

Purpose: repeat the storage decision with exact columnar publication, standalone prebuilt keys,
interleaved frame slots, full/assignments schemas, eager-entry control, complete key/value consumption,
branch forms and reach counting. The fixture covers the Cartesian product of `S=0,1,4,16,64,256`
and empty, dense, prefix, one-point, alternating and sparse reach.

Environment: Eclipse Temurin 25.0.3+9-LTS, JMH 1.37, Linux x86_64, `-Xms1g -Xmx1g`, two forks,
3x250 ms warmup, 5x250 ms measurement, `gc` profiler and JSON output. Maven, JMH driver and forks used
the same pinned JVM. The then-outstanding Java 21 deployment rerun was completed by issue #155.

| Representative integrated flow | Frame columnar ns/B | Append columnar ns/B | Frame eager ns/B |
|---|---:|---:|---:|
| `S=4`, dense | 272.0 / 312 | **188.4 / 296** | 195.7 / 600 |
| `S=64`, dense | 1,011.7 / **824** | **925.1** / 1,120 | 1,143.7 / 2,520 |
| `S=64`, alternating | 722.2 / **864** | **642.5** / 1,320 | 718.9 / 1,648 |
| `S=256`, sparse 4 | 700.3 / 1,352 | **200.7 / 328** | 744.4 / 1,608 |

Historical verdict, resolved by issue #155: **REOPEN frame-tail storage and BLOCK production calculation-memory changes; ACCEPT columnar
publication, mode-first branch and per-capture counting.** Exact freeze removes frame-tail's
historical small/dense dominance. Frame and append both remain Pareto-relevant, so append is not
automatically promoted. An integrated `capture -> materialize -> freeze -> sink` matrix confirms that
columnar remains Pareto-winning despite eager's lower retained graph for a single large sparse memory.
The corrected no-loop branch fixture selects mode-first to protect normal inactive execution. Dense bitmap remains discarded. Commands, all phase
scores, JOL retained bytes, limitations and retained result paths are documented in
`docs/planning/etapa-10/binding-persistable-payload-benchmark.md`.

## 2026-08-18 - Etapa 10 Calculation-Memory Storage Prototype

Purpose: choose among an extended execution frame, an append-only recorder with lazy slot sidecar,
and dense values plus presence bitmap before implementing `computeWithMemory()`. The throwaway JMH
prototype includes frame preparation, variable inclusion, capture, freeze, and separate complete
traversal. It covers `S=0,4,64,256` and dense, alternating, prefix, empty, and sparse reachability.

Environment: Temurin 25.0.3, JMH 1.37, Linux x86_64, `-Xms1g -Xmx1g`, 5x500 ms warmup,
10x500 ms measurement, 3 forks, and `gc` profiler. The module target remains Java 21; rerun the final
production implementation on the deployment JVM.

| Shape | Frame tail ns/op / B/op | Append ns/op / B/op | Dense ns/op / B/op | Winner |
|---|---:|---:|---:|---|
| `S=0, K=0` | 21.4 / 144 | 30.0 / 184 | 33.0 / 216 | frame |
| `S=4, K=4` | 28.8 / 160 | 58.5 / 200 | 64.3 / 256 | frame |
| `S=64, K=64` | 259.9 / 400 | 442.2 / 1,056 | 632.7 / 736 | frame |
| `S=64, K=32` alternating | 112.5 / 400 | 477.0 / 1,730.7 | 357.6 / 784 | frame |
| `S=256, K=0` | 174.5 / 1,168 | 30.1 / 184 | 186.8 / 1,272 | append |
| `S=256, K=256` | 1,049.7 / 1,168 | 1,815.6 / 3,560 | 2,523.8 / 2,296 | frame |
| `S=256, K=16` prefix | 171.7 / 1,168 | 150.4 / 448 | 214.0 / 1,336 | append |
| `S=256, K=4` sparse | 172.8 / 1,168 | 94.2 / 424 | 206.9 / 1,352 | append |

Verdict: **ACCEPT frame tail as the first implementation; DISCARD dense; defer append.** Frame tail
wins every current-scale case and is the smallest design. Append remains evidence for a future fallback
only if real plan telemetry finds many large, sparsely reached layouts. Full results and traversal scores
are recorded in `docs/planning/etapa-10/prototype-calculation-memory-storage.md`.

Initial public-contract follow-up (superseded on 2026-08-18) selected eagerly materialized variable and
calculation entry lists. A persistence-oriented review replaced that publication choice with exact
columnar values, prebuilt standalone keys, allocation-free indexed traversal, and immutable `List`
projections. Frame-tail remains the capture base, but the verdict is conditional on a binding follow-up
JMH covering capture, freeze, indexed/list consumption, a sequential no-I/O persistence sink, branch
order, and reach counting. A representative loss reopens the affected choice before production; it does
not promote append automatically. The final gate must run on the Java 21 deployment JVM.

## 2026-08-13 - Etapa 8 Final Performance Gate (issue #130)

Purpose: close Etapa 8 without adding another optimization. All four tracked suites and every JMH
benchmark introduced by the etapa were rebuilt once and run back-to-back from commit `2bbe766`, then
the retained and rejected mechanisms, ADR 0020, the module glossary, and both Etapa 8 plans were
checked against the resulting tree.

Commands used:

```bash
mvn -pl exp-mk3 -am -DskipTests test-compile
mvn -q -pl exp-mk3 dependency:build-classpath -Dmdep.outputFile="target/jmh-cp.txt" -DincludeScope=test

java -cp "runestone-toolkit/target/classes:exp-mk3/target/test-classes:exp-mk3/target/classes:$(tr -d '\n' < exp-mk3/target/jmh-cp.txt)" \
  org.openjdk.jmh.Main \
  "(Phase5BaselineBenchmark|Phase6NavigationBenchmark|Phase7FoldingGateBenchmark\\.(navigationPrefix|membershipDownload|assertionElision)|CollectionOperationsBenchmark)" \
  -wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns -jvmArgs "-Xms1g -Xmx1g" -prof gc \
  -rf json -rff "/tmp/opencode/issue-130-tracked-hotpaths.json" -foe true

java ... org.openjdk.jmh.Main "Phase7FoldingGateBenchmark.richConstantCompilation" \
  -wi 15 -i 15 -w 1s -r 1s -f 3 -tu ns -jvmArgs "-Xms1g -Xmx1g" -prof gc \
  -rf json -rff "/tmp/opencode/issue-130-phase7-compilation.json" -foe true

java ... org.openjdk.jmh.Main \
  "(FunctionInvocationBenchmark|ComparisonAndEqualityBenchmark|RegisteredNavigationInvocationBenchmark|RemainingNodeSpecializationBenchmark|ExecutionScopePoolBenchmark)" \
  -wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns -jvmArgs "-Xms1g -Xmx1g" -prof gc \
  -rf json -rff "/tmp/opencode/issue-130-etapa8-benchmarks.json" -foe true
```

Environment: OpenJDK 26.0.1 (module release target 21), JMH 1.37, Linux 7.0.0-29-generic x86_64,
Intel Core i7-7700HQ, AC power, turbo enabled, `-Xms1g -Xmx1g`, and `gc` profiler. The unprivileged
session exposed the governor as `powersave`; changing it required interactive sudo. Every result in
this gate used that same environment back-to-back. Consequently, historical day-to-day deltas are
classified by code cause rather than by the obsolete standalone +/-1% rule, and same-run
optimized/Oracle pairs decide ambiguous cases, as required by issue #130.

### Four tracked suites

The reference column is the 2026-08-08 governor-stable re-baseline. Every tracked point moved in the
faster direction; there is no historical latency regression to attribute. The large improvements
with an identified Etapa 8 cause are the decimal arithmetic and comparison nodes in Phase 5,
prepared registered-member invocation in Phase 6, and the same invocation/allocation reductions in
collection operations. Remaining improvements have no isolated Etapa 8 cause and are classified as
machine/run drift, not as optimization claims.

| Benchmark | Reference (ns/op) | Final (ns/op) | Delta | B/op final | Classification |
|---|---:|---:|---:|---:|---|
| `Phase5.arithmeticCompute` | 213.54 | 168.42 +/- 2.93 | -21.1% | 160 | identified: decimal specialization |
| `Phase5.logicalCompute` | 263.60 | 233.46 +/- 3.21 | -11.4% | 208 | identified: comparison/equality specialization |
| `Phase5.fullUncachedCompilation` | 12572.19 | 11631.99 +/- 1914.64 | -7.5% | 17706.5 | no regression; drift |
| `Phase5.materializationCompute` | 121.94 | 98.76 +/- 4.48 | -19.0% | 184 | no regression; drift |
| `Phase6.filter` | 276.83 | 241.51 +/- 7.49 | -12.8% | 336 | no regression; drift |
| `Phase6.subscript` | 33.01 | 29.52 +/- 0.40 | -10.6% | 24 | no regression; drift |
| `Phase6.slice` | 168.49 | 150.90 +/- 4.56 | -10.4% | 280 | no regression; drift |
| `Phase6.propertyChain` | 17.64 | 16.62 +/- 1.13 | -5.8% | 24 | prepared property route plus drift |
| `Phase6.methodChain` | 187.54 | 23.36 +/- 0.81 | -87.5% | 64 | identified: prepared method route |
| `Phase6.nestedLambda` | 448.16 | 326.29 +/- 17.73 | -27.2% | 616 | identified: prepared invocation |
| `Collection.allShortCircuit` | 43.34 | 32.91 +/- 0.39 | -24.1% | 24 | identified: comparison specialization |
| `Collection.map` | 470.94 | 275.97 +/- 5.77 | -41.4% | 392 | identified: invocation/allocation reduction |
| `Collection.mapThenSum` | 426.24 | 259.15 +/- 8.11 | -39.2% | 424 | identified: invocation/allocation reduction |
| `Collection.reduce` | 318.69 | 169.79 +/- 3.43 | -46.7% | 408 | identified: invocation/allocation reduction |
| `Collection.safeCall` | 484.55 | 286.72 +/- 4.69 | -40.8% | 392 | identified: invocation/allocation reduction |
| `Collection.sortBy` | 533.35 | 449.15 +/- 8.99 | -15.8% | 653.3 | identified: invocation/allocation reduction |
| `Collection.sum` | 92.48 | 80.57 +/- 1.08 | -12.9% | 184 | no regression; drift |
| `Collection.wildcardMaterialization` | 219.43 | 186.93 +/- 2.87 | -14.8% | 360 | no regression; drift |

Phase 7 remains directly protected by its Oracle pairs:

| Shape | Optimized (ns/op) | Oracle (ns/op) | Delta | B/op optimized -> Oracle |
|---|---:|---:|---:|---:|
| Navigation-prefix fold | 13.78 +/- 0.20 | 21.19 +/- 0.42 | -35.0% | 24 -> 24 |
| Membership download | 25.58 +/- 0.45 | 276.57 +/- 4.06 | -90.8% | 16 -> 504 |
| Assertion elision | 51.62 +/- 0.67 | 61.69 +/- 1.04 | -16.3% | 104 -> 104 |
| Rich constant compilation | 138487 +/- 2215 | 135255 +/- 12465 | +2.4% | 139837 -> 127947 |

The compilation point's intervals overlap widely. It has no identified execution-code regression
and its paired Oracle measurement classifies it as neutral machine/GC noise rather than a failure.

### Etapa 8 benchmarks and final decisions

The table is a compact point-estimate summary. Confidence intervals are retained in the three JSON
result files named above and in each mechanism's authoritative permanence entry immediately below in
this history; every keep/remove decision uses those intervals rather than point estimates alone.

| Mechanism | Optimized (ns/op) | Oracle (ns/op) | Delta | B/op optimized -> Oracle | Final decision |
|---|---:|---:|---:|---:|---|
| Number comparison, 16x | 328.51 | 471.01 | -30.3% | 208 -> 200 | keep |
| String comparison, 16x | 262.32 | 421.04 | -37.7% | 160 -> 152 | keep |
| Number equality, 16x | 318.05 | 451.40 | -29.5% | 208 -> 200 | keep |
| Scale-mismatch number equality, 16x | 330.92 | 560.49 | -41.0% | 208 -> 200 | keep |
| String equality, 16x | 251.42 | 352.62 | -28.7% | 160 -> 152 | keep |
| Registered property | 131.61 | 134.16 | -1.9% | 168 -> 168 | keep; issue #127 pair was decisive at -5.0% |
| Registered method | 154.08 | 314.70 | -51.0% | 208 -> 440 | keep |
| Binary null coalescence | 23.70 | 27.38 | -13.5% | 56 -> 56 | keep |
| Two-branch conditional | 23.95 | 25.73 | -6.9% | 40 -> 40 | keep |
| Decimal add | 21.39 | 26.73 | -20.0% | 24 -> 56 | keep |
| Decimal subtract | 24.80 | 27.22 | -8.9% | 64 -> 96 | keep; allocation and prior pair confirm |
| Decimal multiply | 24.15 | 27.83 | -13.2% | 64 -> 96 | keep |
| Decimal modulo | 26.95 | 32.62 | -17.4% | 24 -> 56 | keep |

`FunctionInvocationBenchmark` produced 72.76/72.90, 169.28/171.82, 325.12/324.68, and
420.80/406.57 ns/op for optimized/Oracle at arities 1, 2, 4, and 5, with identical allocation in
each pair. Those two plan forms intentionally share one `FunctionDescriptor` entry point and cannot
discriminate the mechanism. The arity-two -1.5% and arity-five +3.5% points therefore have no
code-path cause and are classified as ordering noise; the issue #125 same-tree before/after
measurement remains the valid evidence (-28.5% to -65.5% and lower allocation).

The rejected candidate shapes are still present in `RemainingNodeSpecializationBenchmark` as
negative controls. Their final-tree optimized/Oracle point estimates were: number `between` -1.4%,
string `between` +4.0%, N-ary concat -2.6%, one-branch conditional -0.3%, and decimal division -2.0%.
Both arms now build the same generic node for those shapes, so these deltas are expected run noise,
not evidence to restore a removed implementation. The provisional measurements that decided removal
remain in the issue #128 entry: `between` was neutral, N-ary concat regressed 13.2%, one-branch
conditional was neutral, and decimal division was neutral. Binary concat and three-branch
conditional remain explicit never-specialized controls. Their final-tree pairs were
37.62/38.24 ns/op (-1.6%) and 30.57/29.82 ns/op (+2.5%), respectively. Both forms build the same
generic node in each control, so both deltas are classified as same-path run noise, with no code
cause and no regression verdict.

`ExecutionScopePoolBenchmark` measured the retained fresh-scope implementation at
`1597.36 +/- 20.82 ns/op`, `2232.02 B/op`, reproducing issue #129's pre-pool arm. The provisional
pool remains removed because it saved only 3.58%, not the declared 20% allocation threshold.

### Consolidated verdict

Etapa 8 passes its final performance gate. Kept: prepared reflection-free function and registered
member invocation, exact argument-coercion elision, comparison/equality specialization, binary null
coalescence, fixed two-branch conditional, and decimal add/subtract/multiply/modulo specialization.
Removed by measurement: number/string `between`, N-ary concat, one-branch conditional, decimal
division, and the `ExecutionScope` pool. No typed `ExecutableNode` entry point was introduced because
no benchmark required it. No public flag, system property, or API selects the Oracle or invocation
route.

The seven macro-plan corrections are confirmed in the final docs: specialization is additive;
power/root specialization and short-circuit reordering are out; small-constant caching is out;
historical direct-getter criteria were replaced by paired gates; preallocated varargs arrays and
`VarHandle` accessors have no target. ADR 0020 remains accurate with its issue #125 amendment. The
three glossary terms describe mechanisms that actually remain. The detailed plan, decision record,
and macro plan were updated where they still described measured-out candidates or the scope pool as
pending.

`mvn -pl exp-mk3 -am test` passed after the documentation reconciliation: 343 upstream toolkit tests
and 1062 `exp-mk3` tests, with zero failures or errors. The 50 reported skips are diagnostic Corpus de
Expressoes cases that have no executable plan for the equivalence executor, not disabled or excluded
tests; no `@Disabled` test exists. This run includes the complete executable corpus in optimized and
Oracle forms, both jqwik plan-equivalence properties (1000 tries each), shared-plan concurrency, and
plan non-retention.

## 2026-08-13 - Issue 129: ExecutionScope Pool Permanence Decision

Purpose: issue #129 evaluated whether reusing an `ExecutionScope` and its frame pays on the Etapa 8
canonical expression after the invocation and navigation allocation reductions had landed. The gate
was declared before implementation: keep the pool only if it reduces allocation by at least 20%
without making `ns/op` worse outside the error band.

The provisional implementation kept one idle `ExecutionScope` per thread and `ExecutionPlan`. It
removed the cached scope from its slot while executing, so a reentrant call allocated an independent
scope instead of corrupting the outer frame. Reuse copied the complete immutable frame template back
over the frame before applying overrides. That copy re-seeded every appended memo slot with `UNBOUND`
and cleared assignment and current-item slots; it also cleared the cached current-time instant and
installed the call's `Clock`. The implementation reused the existing concrete `ExecutionScope`
rather than adding a subclass, so it did not make `scope.read` megamorphic alongside
`ConstantFoldSentinelScope`.

`ExecutionScopePoolBenchmark` executes the canonical expression directly through a prebuilt optimized
`ExecutionPlan`, with its `Clock`, overrides, environment, and plan held in JMH state. The before and
after arms were run back-to-back from the same working tree; only the provisional production pool
changed between them.

Command used for each arm:

```bash
mvn -pl exp-mk3 -am -DskipTests test-compile
mvn -q -pl exp-mk3 dependency:build-classpath -Dmdep.outputFile="target/jmh-cp.txt" -DincludeScope=test
java -cp "runestone-toolkit/target/classes:exp-mk3/target/test-classes:exp-mk3/target/classes:$(tr -d '\n' < exp-mk3/target/jmh-cp.txt)" \
  org.openjdk.jmh.Main "ExecutionScopePoolBenchmark" \
  -wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns \
  -jvmArgs "-Xms1g -Xmx1g" -prof gc \
  -rf json -rff "/tmp/opencode/exp-mk3-scope-pool-issue-129-<variant>.json" -foe true
```

`<variant>` was `before` for the fresh-scope arm and `after` for the provisional pooled arm.

Environment: OpenJDK 26.0.1 (module release target 21), JMH 1.37, `-Xms1g -Xmx1g`, 5x500ms
warmup, 10x500ms measurement, 3 forks, `gc` profiler. The host exposed only the `powersave` governor
to the unprivileged session; changing it required interactive sudo. Both paired arms used that same
governor back-to-back. This limitation can affect latency, but not the exact steady-state allocation
counts on which the gate already fails by a wide margin.

| Variant | ns/op | B/op | Allocation delta |
|---|---:|---:|---:|
| Fresh scope and cloned frame | 1600.239 +/- 23.060 | 2232.022 | baseline |
| Reused scope and copied frame | 1623.476 +/- 16.606 | 2152.023 | **-3.58%** |

**Decision: remove the pool.** It saves only 80 B/op (3.58%), far below the required 20%. Its latency
point estimate is 1.45% slower, although the confidence intervals overlap, so no latency regression
outside the error band is claimed. The allocation condition independently and decisively fails.
`ExecutionPlan` therefore continues cloning its frame template and constructing a fresh
`ExecutionScope` per call; no disabled pool, flag, property, mutable reset API, or `ThreadLocal`
remains in production. The benchmark remains as the reproducible canonical measurement.

## 2026-08-12 - Issue 128: Familias Especializadas Restantes

Purpose: issue #128 measured each remaining Etapa 8 node-specialization candidate against the
Unoptimized Oracle built from the same `SemanticModel`. The retained changes are additive:
`buildOracle` still constructs `NullCoalesceExecutableNode`, `ConditionalExecutableNode`, and
`BinaryExecutableNode`, while the optimized builder selects a fixed binary coalescence node when the
left operand is `MAY_BE_NULL`, a fixed two-branch conditional node after the existing lazy fold, and
switch-free decimal nodes for add, subtract, multiply, and modulo. No algebraic rewrite or decimal to
`long` conversion is present.

The first complete run used one oversized environment for every case. It made input preparation
dominate the mechanisms (roughly 1.2-2.0 us/op) and left most confidence intervals overlapping. The
reviewed run below corrected the benchmark, giving each parameter only the overridable symbols its
expression uses. This is the isolation used for the decision. The binary-concatenation and
three-branch-conditional parameters are negative controls: neither is specialized.

Command used:

```bash
mvn -pl exp-mk3 -am -DskipTests test-compile
mvn -q -pl exp-mk3 dependency:build-classpath -Dmdep.outputFile="target/jmh-cp.txt" -DincludeScope=test
java -cp "runestone-toolkit/target/classes:exp-mk3/target/test-classes:exp-mk3/target/classes:$(tr -d '\n' < exp-mk3/target/jmh-cp.txt)" \
  org.openjdk.jmh.Main "RemainingNodeSpecializationBenchmark" \
  -wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns \
  -jvmArgs "-Xms1g -Xmx1g" -prof gc \
  -rf json -rff "/tmp/performance-benchmark/exp-mk3-remaining-specialization-issue-128-isolated.json" \
  -foe true
```

After review, retained candidates and the one-branch conditional were rerun against the final tree
with the same protocol and:

```bash
java ... org.openjdk.jmh.Main "RemainingNodeSpecializationBenchmark" \
  -p "benchmarkCase=COALESCE_BINARY,CONDITIONAL_ONE,CONDITIONAL_TWO,DECIMAL_ADD,DECIMAL_SUBTRACT,DECIMAL_MULTIPLY,DECIMAL_MODULO" \
  -wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns -jvmArgs "-Xms1g -Xmx1g" -prof gc \
  -rf json -rff "/tmp/performance-benchmark/exp-mk3-remaining-specialization-issue-128-final.json" -foe true
```

Environment: OpenJDK 26.0.1 (module release target 21), JMH 1.37, `-Xms1g -Xmx1g`, 5x500ms
warmup, 10x500ms measurement, 3 forks, `gc` profiler.

| Family / shape | Optimized (ns/op) | Oracle (ns/op) | Delta | B/op optimized -> Oracle | Decision |
|---|---:|---:|---:|---:|---|
| Binary null coalescence | 25.077 +/- 0.469 | 28.458 +/- 0.434 | **-11.9%** | 56 -> 56 | Keep |
| Number `between` | 23.220 +/- 0.500 | 23.236 +/- 0.383 | -0.1% | 32 -> 32 | Remove |
| String `between` | 33.178 +/- 0.702 | 33.928 +/- 0.936 | -2.2% | 32 -> 32 | Remove |
| Binary concat control | 38.400 +/- 0.651 | 38.798 +/- 0.777 | -1.0% | 112 -> 112 | Never targeted |
| Four-operand concat | 104.232 +/- 1.672 | 92.085 +/- 1.388 | **+13.2%** | 216 -> 248 | Remove |
| One-branch conditional | 20.493 +/- 0.532 | 20.632 +/- 0.349 | -0.7% | 32 -> 32 | Remove |
| Two-branch conditional | 23.879 +/- 0.285 | 27.333 +/- 0.678 | **-12.6%** | 40 -> 40 | Keep |
| Three-branch conditional control | 30.921 +/- 0.573 | 30.741 +/- 0.389 | +0.6% | 48 -> 48 | Generic |
| Decimal add | 21.962 +/- 0.798 | 26.872 +/- 2.397 | **-18.3%** | 24 -> 56 | Keep |
| Decimal subtract | 26.339 +/- 1.193 | 28.252 +/- 0.598 | **-6.8%** | 64 -> 96 | Keep |
| Decimal multiply | 24.650 +/- 0.408 | 28.029 +/- 0.463 | **-12.1%** | 64 -> 96 | Keep |
| Decimal divide | 646.658 +/- 11.363 | 648.776 +/- 9.564 | -0.3% | 1805 -> 1816 | Remove |
| Decimal modulo | 28.390 +/- 0.673 | 34.398 +/- 0.793 | **-17.5%** | 24 -> 56 | Keep |

**Decisions:** binary coalescence and the fixed two-branch conditional stay. The one-branch
conditional was removed after the final-tree remeasurement put it inside the neutral band (-0.7%).
`between` is removed:
NUMBER is neutral and the small STRING point estimate overlaps the Oracle error band. N-ary
concatenation is removed despite saving 32 B/op because the hand-built `StringBuilder` path is 13.2%
slower; two-operand concatenation remains untouched on `StringConcatFactory`. Decimal add, subtract,
multiply, and modulo stay because each has a non-overlapping 6.8%-18.3% latency gain and saves 32
B/op. Decimal division is removed as predicted: `BigDecimal.divide` dominates and removing the outer
switch is neutral. The source-level scale, `MathContext`, zero checks, failure diagnostics, operand
order, and lazy evaluation policies remain unchanged.

The complete-family result is preserved at
`/tmp/performance-benchmark/exp-mk3-remaining-specialization-issue-128-isolated.json`; retained
variants were remeasured after review against the final selection logic at
`/tmp/performance-benchmark/exp-mk3-remaining-specialization-issue-128-final.json`. The focused
`PlanEquivalenceHarness` suite covers specialized and generic shapes plus ordered effect probes for
coalescence and conditionals.

## 2026-08-12 - Issue 127: Pontos de Entrada Preparados para Navegacao Registrada

Purpose: issue #127 (Etapa 8, increment 5) extends Invocacao Sem Reflexao from global functions to
registered Java properties and methods. Each `JavaPropertyDescriptor` and `JavaMethodDescriptor`
now prepares one entry point while the Ambiente de Expressao's Java type catalog is built, shared by
every optimized plan compiled against that environment. The same `InvocationEntryPoint` mechanism is
used by function and member descriptors: `LambdaMetafactory` is attempted for total MethodHandle
arities zero through four and a pre-adapted `invokeExact` handle is used when linking is unavailable;
larger arities use the pre-adapted spreader route. For instance members, the receiver is parameter
zero and therefore participates in that total arity.

Unlike issue #125's function benchmark, this `optimized`/Oracle pair directly discriminates the
mechanism. `ExecutionPlanBuilder.build` selects registered-member nodes that invoke the descriptor's
prepared entry point, while `buildOracle` retains additive Oracle-only nodes using the previous
`MethodHandle.invoke`/`invokeWithArguments` routes. Both consume the same setup-resolved navigation
binding; no runtime lookup or reflection was introduced. The states use `OVERRIDABLE` object and
argument symbols so Constant Folding cannot remove the navigation.

Command used:

```bash
mvn -pl exp-mk3 -am -DskipTests test-compile
mvn -q -pl exp-mk3 dependency:build-classpath \
  -Dmdep.outputFile="/tmp/opencode/exp-mk3-jmh-cp.txt" -DincludeScope=test
java -cp "runestone-toolkit/target/classes:exp-mk3/target/test-classes:exp-mk3/target/classes:$(tr -d '\n' < /tmp/opencode/exp-mk3-jmh-cp.txt)" \
  org.openjdk.jmh.Main "RegisteredNavigationInvocationBenchmark" \
  -wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns \
  -jvmArgs "-Xms1g -Xmx1g" -prof gc \
  -rf json -rff "/tmp/opencode/registered-navigation-issue-127-reviewed.json" -foe true
```

Environment: OpenJDK 26.0.1, JMH 1.37, `-Xms1g -Xmx1g`, 5x500ms warmup, 10x500ms
measurement, 3 forks, `gc` profiler. `property` evaluates `account.balance`; `method` evaluates
`account.add(increment)` with a canonical `BigDecimal` parameter and receiver.

| Mechanism | Optimized (ns/op) | Oracle (ns/op) | Delta | B/op optimized -> Oracle |
|---|---:|---:|---:|---:|
| Registered property | 127.723 +/- 4.285 | 134.445 +/- 2.223 | **-5.0%** | 168.002 -> 168.002 |
| Registered method | 165.064 +/- 9.141 | 328.117 +/- 7.995 | **-49.7%** | 208.002 -> 440.005 |

**Decision: keep both prepared navigation entry points.** Both optimized confidence intervals are
strictly below their Oracle counterparts. Property access improves without changing allocation;
method invocation removes the per-call receiver-plus-arguments array and the generic handle entry,
cutting time nearly in half and allocation by about 53%. This satisfies the issue's paired gate and
replaces the macro plan's untestable historical "getter direto" criterion with direct same-run
optimized/Oracle evidence.

The focused registered-navigation test, the unchanged Etapa 6 safe-navigation corpus matrix, full
Corpus de Expressoes equivalence, and the module/full-reactor suites were green after the change.

## 2026-08-11 - Issue 126 Etapa 8 Pilot: Comparacao e Igualdade Sem Duplo Despacho

Purpose: issue #126 (Etapa 8, increment 3, the etapa's second pilot) replaces the runtime type
re-dispatch inside `ExpressionRuntime.compareValues`/`structuralEquals` for order comparison and
equality with a node choice made once at plan-build time, from `SemanticModel#resolvedTypes` /
`equalityOperandTypes`. Four new node classes were added, all additive alongside the generic
`BinaryExecutableNode` the Unoptimized Oracle still builds for every case (ADR 0019, following the
issue #119 `in`-constant precedent): `NumberComparisonExecutableNode` and
`ComparableComparisonExecutableNode` for order comparison (`NUMBER` vs. the remaining orderable types
— `STRING`, `DATE`, `TIME`, `DATETIME`), `NumberEqualityExecutableNode` and
`EqualsEqualityExecutableNode` for equality (`NUMBER` vs. the remaining equals-coincident scalars).
`COLLECTION` and `MAP` equality deliberately decline specialization and keep the generic node even
while `optimizing`: the outer dispatch this family exists to skip costs one `instanceof` check against
the recursive element/value walk the generic node's structural comparison already performs — the same
"does not pay for itself" call issue #119's membership download made for collection and map elements.
No typed `ExecutableNode` entry point was introduced: the generic `Object execute(ExecutionScope)`
already differentiates optimized from Oracle in this benchmark, so no paired measurement existed to
justify widening the interface, per the etapa's own gate on typed entry points.

Unlike issue #125's `FunctionInvocationBenchmark`, the `optimized`/`oracle` pair here **does**
discriminate the mechanism directly (`buildOracle` always builds `BinaryExecutableNode`; `build`
builds the specialized node whenever the operand type is not `COLLECTION`/`MAP`), so this single run
is the pilot's pass/fail signal — no git-stash before/after protocol needed. Each state in
`ComparisonAndEqualityBenchmark` chains sixteen comparisons/equalities over the same two operands
(`OVERRIDABLE` external symbols, never literals, so Constant Folding cannot fold a chain away) so the
per-dispatch delta clears measurement noise instead of being swamped by fixed plan overhead.

**A first run of this benchmark chained the three equality states with `or`, and every override was
chosen to make the first chained term `true`.** `BinaryExecutableNode`'s `LOGICAL_OR` branch is Java's
short-circuiting `||`, so all three equality states measured one equality dispatch per invocation, not
sixteen, while the comparison states (chained with `and`, all-true terms, no short-circuit) measured
the full sixteen. That asymmetry, not a genuinely smaller mechanism, produced an artificially small
first-run equality delta (-3% to -4%) next to comparison's -26% to -38%. Caught before being trusted as
the pilot's answer; fixed by chaining every equality state with `and` instead (still all-true operands,
so `&&` never short-circuits and all sixteen dispatches run, matching the comparison states exactly).
The corrected numbers below are the ones this decision is based on.

Command used:

```bash
mvn -q -N install
mvn -q -pl exp-mk3 -am install -DskipTests
mvn -q -pl exp-mk3 dependency:build-classpath -Dmdep.outputFile="exp-mk3/target/jmh-cp.txt" -DincludeScope=test
java -cp "runestone-toolkit/target/classes:exp-mk3/target/test-classes:exp-mk3/target/classes:$(tr -d '\n' < exp-mk3/target/jmh-cp.txt)" \
  org.openjdk.jmh.Main "ComparisonAndEqualityBenchmark" \
  -wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns \
  -jvmArgs "-Xms1g -Xmx1g" -prof gc \
  -rf json -rff "/tmp/performance-benchmark/exp-mk3-comparison-equality-issue-126.json" \
  -foe true
```

Environment: JDK 26.0.1 (build environment; JDK 21 is the module's compile/target level), JMH 1.37,
`-Xms1g -Xmx1g`, 5×500ms warmup, 10×500ms measurement, 3 forks, `gc` profiler.

| Benchmark | Optimized (ns/op) | Oracle (ns/op) | Delta | B/op optimized → oracle |
|---|---:|---:|---:|---|
| `numberComparison` — 16× `a > b` | 277.24 ± 5.56 | 380.83 ± 7.92 | **-27.2%** | 208 → 200 |
| `stringComparison` — 16× `s > t` | 221.87 ± 3.54 | 361.25 ± 15.14 | **-38.6%** | 160 → 152 |
| `numberEquality` — 16× `a = b` | 275.78 ± 3.17 | 381.26 ± 8.93 | **-27.6%** | 208 → 200 |
| `numberEqualityScaleMismatch` — 16× `a = c` (different scales) | 284.92 ± 5.49 | 481.88 ± 19.76 | **-40.9%** | 208 → 200 |
| `stringEquality` — 16× `s = t` | 221.74 ± 9.05 | 298.12 ± 9.84 | **-25.6%** | 160 → 152 |

Verdict: **the pilot pays, clearly, in both families** (-25.6% to -40.9%, all five states far outside
the ±1% noise band). Comparison and equality land in the same range once measured correctly, which is
the expected result: both mechanisms removed are the same shape — a runtime type re-check
(`ExpressionRuntime.compareValues`/`structuralEquals`) ahead of a cast, replaced by a node choice made
once at construction. The scale-mismatch state shows the largest gain of the five; it is not a
different mechanism, just the same `NumberEqualityExecutableNode` measured with different-scale
operands, confirming the specialized `compareTo` path pays under the exact input shape the ticket's own
risk note calls out as most likely to break silently.

**Decision: both specialized families are kept.** Both margins are unambiguous and clear the ticket's
stop rule ("se a medição pareada não mostrar ganho, a família sai") by a wide margin rather than
triggering it. `COLLECTION`/`MAP` equality was never built in
either specialized form, so there is nothing to discard there — it was a design-time decision, not a
measured one. No typed entry point was added to `ExecutableNode`: this benchmark already isolates the
mechanism through the generic `execute(ExecutionScope)` signature, so introducing a typed default
method now would have no paired measurement behind it, exactly what the etapa's `ExecutableNode`
contract forbids.

`mvn -pl exp-mk3 -am test` was run and green both before this benchmark was added and again after,
confirming the functional/equivalence gate (`PlanEquivalenceHarness`, the extended jqwik property
suite, and the fixed corpus) was unaffected.

## 2026-08-11 - Issue 125 Etapa 8 Pilot: Invocacao Sem Reflexao e Elisao de Coercao de Borda

Purpose: issue #125 (Etapa 8, increment 2, the etapa's own pilot) replaces per-call `Object[]`
allocation plus `MethodHandle.invokeWithArguments` for global function calls with entry points
generated once per `FunctionDescriptor` at `ExpressionEnvironment` build time, and elides
provably-identity argument boundary-coercion filters. This entry is the pilot's pass/fail
measurement, per the etapa's stop rule ("se o piloto do incremento dois não pagar, o trabalho segue
para o incremento quatro assim mesmo").

**A design assumption in ADR 0020 was found false before any production code was written.**
`LambdaMetafactory.metafactory` only accepts a *direct* `MethodHandle` (it calls
`Lookup.revealDirect` internally); every handle this module builds for a function call has already
passed through `asType` (`FunctionDescriptor.fromMethod`), or through
`ProviderMethodAdapter.PreparedMethod.adapt`'s `filterArguments`/`filterReturnValue` chain and,
for exposed-instance providers, `bindTo` (`ReflectedFunctionImporter`). Verified with a 20-line
scratch probe (`MethodHandles.filterArguments` on a direct handle, then `bindTo`) before writing
`FunctionDescriptor`: both fail to link with `LambdaConversionException: ... is not direct or
cannot be cracked`. The adopted mechanism (recorded as an amendment in ADR 0020, not a reversal of
its Decision): attempt `LambdaMetafactory` linking for every arity 0-4 against whatever handle
boundary-coercion elision leaves behind, and fall back to a `MethodHandle` pre-adapted once to
`MethodType.genericMethodType(arity)`, invoked through a fixed `invokeExact` call site per arity,
when linking fails. Both routes are array-free and reflection-free; the executing node does not
know which one backs a given call, exactly as ADR 0020 requires. Arity 5+ always uses `invokeExact`
against a pre-adapted `asSpreader` handle, as originally decided.

**Decision: the pilot pays, clearly, at every arity.** Measured with `FunctionInvocationBenchmark`,
before/after on the same working tree (issue #125's changes stashed for "before", same protocol as
issue #85 above), not the `optimized`/Oracle pairing used elsewhere in this etapa: `build` and
`buildOracle` route a function call through the same `FunctionDescriptor`, hence the same entry
point, so that pairing cannot discriminate this specific mechanism (ADR 0020's Consequences:
"measuring invocation cost requires holding the environment fixed"). The `optimized`/`oracle`
benchmark methods in `FunctionInvocationBenchmark` exist for family consistency with the other
Etapa 8 gates and as a permanent regression net going forward, not as this decision's evidence.

Command used (same protocol as issues #80/#85/#121):

```bash
mvn -q -pl exp-mk3 -am -DskipTests test-compile
mvn -q -pl exp-mk3 dependency:build-classpath -Dmdep.outputFile="target/jmh-cp.txt" -DincludeScope=test
java -cp "runestone-toolkit/target/classes:exp-mk3/target/test-classes:exp-mk3/target/classes:$(tr -d '\n' < exp-mk3/target/jmh-cp.txt)" \
  org.openjdk.jmh.Main "FunctionInvocationBenchmark" \
  -wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns \
  -jvmArgs "-Xms1g -Xmx1g" -prof gc \
  -rf json -rff "/tmp/performance-benchmark/exp-mk3-function-invocation-issue-125-{before,after}.json" \
  -foe true
```

Environment: JDK Temurin 21 (build environment), JMH 1.37, `-Xms1g -Xmx1g`, 5×500ms warmup,
10×500ms measurement, 3 forks, `gc` profiler. All four states call a static provider whose
parameters and return are already the canonical `BigDecimal` NUMBER type, with each argument an
`OVERRIDABLE` external symbol (not a literal) so Constant Folding cannot fold the call away.

| Benchmark (`optimized` state) | Before (ns/op) | After (ns/op) | Delta | B/op before → after |
|---|---:|---:|---:|---|
| `arityOne` — `identity1(a0)` | 185.52 ± 2.87 | 64.05 ± 3.16 | **-65.5%** | 336 → 104 |
| `arityTwo` — `sum2(a0, a1)` | 266.02 ± 1.71 | 148.54 ± 4.47 | **-44.2%** | 445 → 192 |
| `arityFour` — `sum4(a0, a1, a2, a3)` | 433.81 ± 5.22 | 281.66 ± 3.68 | **-35.1%** | 536 → 296 |
| `arityFive` — `sum5(a0, a1, a2, a3, a4)` | 504.32 ± 6.97 | 360.67 ± 6.06 | **-28.5%** | 621 → 392 |

Every arity wins, all far outside the ±1% noise band, and the gain shrinks monotonically from
arity one to arity five — consistent with the mechanism: arity 1-4 gets the full benefit (no
`Object[]`, and for this fully-canonical provider, `LambdaMetafactory`-linked direct dispatch), while
arity 5+ still allocates the argument array (unchanged per ADR 0020, "the arities [invokeExact]
covers" was never claimed array-free) and only gains the pre-adapted-handle/no-per-call-filter part
of the win. `arityFour`'s residual allocation drop (536→296 B/op, more than the ~104 B/op removed
per eliminated argument-boundary-conversion path) is consistent with removing the `Object[]` header
plus four elided `PreparedValue.convert` boundary lambdas that no longer run.

**Continuity decision, per the etapa's stop rule**: the pilot paid, so work continues into increment
four (comparison/equality specialization) on schedule; the "if it fails, proceed anyway" branch of
the rule was not needed.

`mvn -pl exp-mk3 -am test` was run and green (all tracked suites, including
`ExecutionPlanCorpusEquivalenceTest`, `PlanOptimizationEquivalenceTest`'s jqwik property, and the
extended `ExpressionRuntimeTest`/`ReflectedFunctionImporterTest` coverage for arities 0-5 and
non-canonical-argument/provider-null-return contract cases) both before this change (pre-issue-125,
during the stash used for the "before" measurement) and after.

## 2026-08-08 - Subexpressao Comum Memoizada Permanence Decision (issue #121)

Purpose: issue #121 (Etapa 7, increment 6) makes Subexpressao Comum Memoizada — lazy in-place
memoization of a pure, repeated, `@`-free subtree in a frame slot appended past the semantic
`frameSize` — conditioned on measurement: it ships only if a benchmark shows gain, and is removed
with the decision recorded here otherwise. This entry is that measurement and that decision.

**Decision: the feature stays.** Three representative shapes were benchmarked
(`CommonSubexpressionMemoizationBenchmark`, `optimized` vs the Unoptimized Oracle, same protocol as
`Phase7FoldingGateBenchmark`):

| Benchmark | Optimized (ns/op) | Oracle (ns/op) | B/op optimized → oracle |
|---|---:|---:|---|
| `cheapAddMemo` — `(x + 1) + (x + 1)`, `x` `OVERRIDABLE` | 113.93 ± 2.65 | 128.32 ± 1.69 | 218.7 → 216.0 |
| `navigationMemo` — `account.score + account.score * 2`, `account` `OVERRIDABLE` | 111.11 ± 5.05 | 112.72 ± 2.19 | 216.0 → 216.0 |
| `expensiveFunctionMemo` — `sqrt(x) + sqrt(x)`, `x` `OVERRIDABLE` | 2839.91 ± 57.20 | 5422.29 ± 36.44 | 4880.0 → 9392.0 |

None of the three regress: the cheapest case (`cheapAddMemo`) wins by ~11% (`BigDecimal.add`
apparently costs more than a frame read plus a branch), the realistic navigation case is flat
within error bars, and the deliberately expensive case (a repeated `sqrt`, `big-math`-backed and
therefore genuinely costly) wins by ~48% with roughly half the allocation, because the second
occurrence no longer runs `sqrt` at all. `expensiveFunctionMemo` is included specifically as the
case the feature is meant for — Etapa 7's own language corpus already exercises repeated
transcendental/navigation calls, e.g. in interest and pricing formulas — and `cheapAddMemo` as the
adversarial control it was benchmarked against: if the cheapest possible eligible subtree had lost,
that would have been the honest basis for removal regardless of the other two.

**A real self-inflicted regression was caught and fixed before this measurement.** The first
`navigationMemo` run (`account.score + account.score * 2`) showed optimized *losing* by ~9%
(122.50 vs 112.08 ns/op) with 8 extra B/op. The bare identifier `account` also occurs twice in that
source, is external/`OVERRIDABLE`/pure/`@`-free, and is not a `ConstantExecutableNode` — so it was
also being wrapped in a `MemoizedExecutableNode`, adding a frame read and a branch on top of what is
already a single frame read (`FrameReadExecutableNode`), which can never win. Fixed in
`ExecutionPlanBuilder#memoize` (skip wrapping a `FrameReadExecutableNode`, the same reasoning as the
existing `ConstantExecutableNode` skip) and in `CommonSubexpressionAnalyzer` (a plain identifier or
Item Atual read is excluded from occurrence recording entirely, so it never claims a frame slot it
will never use). The table above reflects the fixed code. This is the reason the eligibility rule
in the plan doc is phrased as "no hoisting, ever" rather than "no hoisting when it might not pay" —
the same discipline that prevents a correctness bug here is what caught this performance bug.

Command used, same protocol as `Phase7FoldingGateBenchmark`:

```bash
mvn -q -pl exp-mk3 -am clean test-compile -DskipTests
cd exp-mk3 && mvn -q dependency:build-classpath -Dmdep.outputFile="target/jmh-cp.txt" -DincludeScope=test && cd ..

java -cp "runestone-toolkit/target/classes:exp-mk3/target/test-classes:exp-mk3/target/classes:$(tr -d '\n' < exp-mk3/target/jmh-cp.txt)" \
  org.openjdk.jmh.Main "CommonSubexpressionMemoizationBenchmark" \
  -wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns -jvmArgs "-Xms1g -Xmx1g" -prof gc \
  -rf json -rff "/tmp/performance-benchmark/exp-mk3-cse-issue121-fixed.json" -foe true
```

Environment: commit `23a896b` base (working tree, uncommitted at measurement time); JDK OpenJDK
26.0.1; JMH 1.37; Linux 7.0.0-28-generic x86_64; Intel Core i7-7700HQ, `performance` governor
pinned, AC power; `-Xms1g -Xmx1g`; 5×500ms warmup / 10×500ms measurement; 3 forks; `gc` profiler.

**Non-regression gate on the four existing tracked suites** (±1% band per the issue-85 precedent,
against the 2026-08-08 Governor-Stable Re-Baseline section above, same protocol):

| Benchmark | Baseline (ns/op) | This run (ns/op) | Delta |
|---|---:|---:|---:|
| `Phase6NavigationBenchmark.filter` | 276.83 | 278.33 | +0.5% |
| `Phase6NavigationBenchmark.subscript` | 33.01 | 32.50 | −1.5% |
| `Phase6NavigationBenchmark.slice` | 168.49 | 163.40 | −3.0% |
| `Phase6NavigationBenchmark.propertyChain` | 17.64 | 17.31 | −1.9% |
| `Phase6NavigationBenchmark.methodChain` | 187.54 | 180.99 | −3.5% |
| `Phase6NavigationBenchmark.nestedLambda` | 448.16 | 444.19 | −0.9% |
| `Phase7FoldingGateBenchmark.navigationPrefixOptimized` | 15.60 | 14.94 | −4.2% |
| `Phase7FoldingGateBenchmark.membershipDownloadOptimized` | 28.79 | 27.84 | −3.3% |
| `Phase7FoldingGateBenchmark.assertionElisionOptimized` | 56.77 | 57.10 | +0.6% |
| `CollectionOperationsBenchmark.allShortCircuit` | 43.34 | 43.57 | +0.5% |
| `CollectionOperationsBenchmark.map` | 470.94 | 475.84 | +1.0% |
| `CollectionOperationsBenchmark.mapThenSum` | 426.24 | 431.58 | +1.3% |
| `CollectionOperationsBenchmark.reduce` | 318.69 | 313.26 | −1.7% |
| `CollectionOperationsBenchmark.safeCall` | 484.55 | 485.99 | +0.3% |
| `CollectionOperationsBenchmark.sortBy` | 533.35 | 535.02 | +0.3% |
| `CollectionOperationsBenchmark.sum` | 92.48 | 88.42 | −4.4% |
| `CollectionOperationsBenchmark.wildcardMaterialization` | 219.43 | 201.91 | −8.0% |

None of these expressions contain a repeated eligible subtree, so `CommonSubexpressionAnalyzer`
allocates zero extra frame slots for any of them and the generated plan is byte-for-byte the same
shape as before this issue; every delta above is this machine's ordinary run-to-run noise, the same
order of magnitude the 2026-08-08 re-baseline and re-investigation sections document for genuinely
unchanged code (there, some deltas exceeded 20% with no code change at all). `Phase5BaselineBenchmark`
was not re-run: none of its four expressions (`a + b * 2`, a logical xor chain, and their
compilation) contain a repeated subtree either, and `CommonSubexpressionAnalyzer` running once more
per compilation is the only cost this issue could add to a plan with zero eligible subtrees — its
own cost is bounded by one linear AST walk already paid, in the same pass, by every prior Etapa 7
folding transformation. No exception is needed against the ±1% band in the qualitative sense the
issue-85 precedent asks for: no benchmark here moved because of this issue's code.

**Consequences of keeping the feature**, per the acceptance criteria: `CONTEXT.md` receives the
Subexpressao Comum Memoizada entry (below the existing Etapa 7 terms); the memo mechanism ships as
implemented — lazy in-place memoization on plan-frame-appended slots seeded with the `UNBOUND`
sentinel, eligibility requiring purity, two or more occurrences, no Item Atual read, and no internal
(assignable) symbol read (the last one not written in the original issue text but required to avoid
collapsing two different values of a reassigned variable into one, per `CommonSubexpressionAnalyzer`'s
class doc), and structural-key identity that never uses `NodeId` and compares `FunctionDescriptor`
and the identity-bearing accessor of a navigation binding by identity, never by `equals`.

`mvn -pl exp-mk3 -am test` is green (1027 tests) with this issue's code in place, including the new
`CommonSubexpressionMemoizationTest` (the three traps: memo inside a not-taken lazy branch, a
current-item-dependent subtree recomputing per element, and failure surfacing at the first executed
occurrence with the oracle's code and span) and an extended `PlanOptimizationEquivalenceTest` source
(`... u := sqrt(x) + sqrt(x); r + s + t + u`) so the jqwik equivalence property exercises a real
`MemoizedExecutableNode`, not just the internal-symbol path the original fixed source happened to
take exclusively.

## 2026-08-08 - Governor-Stable Re-Baseline (All Four Tracked Suites)

Purpose: the re-investigation below found that this machine's `powersave` cpufreq governor was a
plausible driver of the drift that made `Phase5BaselineBenchmark`'s `arithmeticCompute`,
`logicalCompute`, and `fullUncachedCompilation` numbers non-reproducible run-to-run. An AC-aware
udev rule + systemd oneshot fallback now pins the governor to `performance` whenever this laptop is
on AC power (verified switching correctly in both plug directions). With that variable controlled,
this run re-records all four tracked benchmark suites — `Phase5BaselineBenchmark`,
`Phase6NavigationBenchmark`, `Phase7FoldingGateBenchmark`, `CollectionOperationsBenchmark` — from
the same working tree, back to back, at the same protocol each was originally recorded under. This
is a fresh reference point, not a before/after optimization claim; no code changed between this run
and the last recorded numbers for any of the four suites.

Command used (repeated per benchmark class/method pattern, same reactor build for all four):

```bash
mvn -q -N install
mvn -q -pl runestone-toolkit -am install -DskipTests
mvn -q -pl exp-mk3 -am clean test-compile -DskipTests
cd exp-mk3 && mvn -q dependency:build-classpath -Dmdep.outputFile="target/jmh-cp.txt" -DincludeScope=test && cd ..

java -cp "runestone-toolkit/target/classes:exp-mk3/target/test-classes:exp-mk3/target/classes:$(tr -d '\n' < exp-mk3/target/jmh-cp.txt)" \
  org.openjdk.jmh.Main "Phase5BaselineBenchmark" \
  -wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns -jvmArgs "-Xms1g -Xmx1g" -prof gc \
  -rf json -rff "/tmp/performance-benchmark/exp-mk3-phase5-rebaseline.json" -foe true

java -cp "...same classpath..." org.openjdk.jmh.Main "Phase6NavigationBenchmark" \
  -wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns -jvmArgs "-Xms1g -Xmx1g" -prof gc \
  -rf json -rff "/tmp/performance-benchmark/exp-mk3-phase6-rebaseline.json" -foe true

java -cp "...same classpath..." org.openjdk.jmh.Main \
  "Phase7FoldingGateBenchmark.(navigationPrefix|membershipDownload|assertionElision)" \
  -wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns -jvmArgs "-Xms1g -Xmx1g" -prof gc \
  -rf json -rff "/tmp/performance-benchmark/exp-mk3-phase7-hotpath-rebaseline.json" -foe true

java -cp "...same classpath..." org.openjdk.jmh.Main "Phase7FoldingGateBenchmark.richConstantCompilation" \
  -wi 15 -i 15 -w 1s -r 1s -f 3 -tu ns -jvmArgs "-Xms1g -Xmx1g" -prof gc \
  -rf json -rff "/tmp/performance-benchmark/exp-mk3-phase7-compilation-rebaseline.json" -foe true

java -cp "...same classpath..." org.openjdk.jmh.Main "CollectionOperationsBenchmark" \
  -wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns -jvmArgs "-Xms1g -Xmx1g" -prof gc \
  -rf json -rff "/tmp/performance-benchmark/exp-mk3-collection-ops-rebaseline.json" -foe true
```

`Phase6NavigationBenchmark.filter` was also re-run alone as a spot check (`276.93 ± 12.64 ns/op`),
matching the full-class run below within error bars, confirming no forked-JVM ordering effect.

Environment:

- Commit: `96ca4b0524dc7fcbb424c8c77791ff2515e3be8c` (`CollectionOperationsBenchmark`'s issue-118
  `safeCall` fix, and `OraclePlanFixtures`/`Phase7FoldingGateBenchmark` from issue #120, are
  uncommitted at measurement time, same as noted in the section below)
- JDK: OpenJDK 26.0.1 (Homebrew build, mixed mode, sharing)
- JMH: 1.37
- OS: Linux 7.0.0-28-generic x86_64
- CPU: Intel Core i7-7700HQ, 2.8 GHz base, laptop, on AC power throughout
- **cpufreq governor: `performance` (pinned via udev/systemd, confirmed for all cores before and
  after each run)**; turbo boost disabled (`intel_pstate/no_turbo=1`, unchanged from prior runs)
- JVM args: `-Xms1g -Xmx1g`
- Warmup/Measurement: 5×500ms / 10×500ms (all cases except `richConstantCompilation`),
  15×1s / 15×1s (`richConstantCompilation`)
- Forks: 3
- Profiler: `gc`

Results:

**`Phase5BaselineBenchmark`**

| Benchmark | Score (ns/op) | Error | B/op | 2026-08-01 baseline (ns/op) |
|---|---:|---:|---:|---:|
| `arithmeticCompute` | 213.54 | 2.93 | 216.0 | 174.96 |
| `logicalCompute` | 263.60 | 4.71 | 208.0 | 220.68 |
| `fullUncachedCompilation` | 12572.19 | 2291.65 | 16353.9 | 9008.50 |
| `materializationCompute` | 121.94 | 1.12 | 184.0 | 157.56 |

**`Phase6NavigationBenchmark`**

| Benchmark | Score (ns/op) | Error | B/op |
|---|---:|---:|---:|
| `filter` | 276.83 | 4.28 | 344.0 |
| `subscript` | 33.01 | 0.32 | 24.0 |
| `slice` | 168.49 | 3.11 | 280.0 |
| `propertyChain` | 17.64 | 0.28 | 24.0 |
| `methodChain` | 187.54 | 2.84 | 306.7 |
| `nestedLambda` | 448.16 | 25.56 | 754.7 |

**`Phase7FoldingGateBenchmark`**, optimized vs oracle:

| Benchmark | Optimized (ns/op) | Oracle (ns/op) | B/op optimized → oracle |
|---|---:|---:|---|
| `navigationPrefix` | 15.60 ± 0.31 | 23.02 ± 0.52 | 24.0 → 24.0 |
| `membershipDownload` | 28.79 ± 0.53 | 300.33 ± 5.38 | 16.0 → 528.0 |
| `assertionElision` | 56.77 ± 0.77 | 225.52 ± 2.21 | 104.0 → 336.0 |
| `richConstantCompilation` | 142457.65 ± 3167.69 | 145566.23 ± 15466.30 | 129524.8 → 127934.2 |

**`CollectionOperationsBenchmark`**

| Benchmark | Score (ns/op) | Error | B/op |
|---|---:|---:|---:|
| `allShortCircuit` | 43.34 | 0.60 | 56.0 |
| `map` | 470.94 | 14.46 | 648.0 |
| `mapThenSum` | 426.24 | 16.19 | 640.0 |
| `reduce` | 318.69 | 4.75 | 704.0 |
| `safeCall` | 484.55 | 17.56 | 624.0 |
| `sortBy` | 533.35 | 7.72 | 690.7 |
| `sum` | 92.48 | 0.66 | 184.0 |
| `wildcardMaterialization` | 219.43 | 8.85 | 392.0 |

Verdict: pinning the governor did **not** make `Phase5BaselineBenchmark`'s three previously
"regressed" cases converge back to their 2026-08-01 numbers — `arithmeticCompute` (213.5 vs
174.96), `logicalCompute` (263.6 vs 220.68), and `fullUncachedCompilation` (12572 vs 9008.5) all
land close to the numbers recorded in the 2026-08-08 re-investigation above (223.5/266.5/11583.7 on
`HEAD` there), not the original baseline. This confirms what that section already concluded from
the `a60674e` control run: the governor was *a* plausible drift mechanism worth eliminating for
future reproducibility, but it is not what separates today's numbers from 2026-08-01's — something
about that recording (a different JVM warm-state, a since-changed default, or another environment
variable never isolated) is not reproducible on this machine regardless of governor policy, and no
further attempt is made here to chase it. `Phase6NavigationBenchmark` and `Phase7FoldingGateBenchmark`
have no prior baseline predating the governor fix to compare against (Phase 6's own baseline already
reflects the pre-fix, `powersave` environment) and `CollectionOperationsBenchmark`'s only prior
same-environment reference is the issue-120 gate run five sections below, which these numbers are
consistent with within error bars for every case. Going forward, this section is the reference point
for all four suites under a controlled, AC-pinned `performance` governor; the governor variable is
now closed as a re-baseline candidate cause.

`mvn -pl exp-mk3 -am test` was run and green (1020 tests, 0 failures/errors, 50 skipped) after this
re-baseline, confirming the functional/concurrency gate was unaffected. No code was changed as part
of this entry. Generated JSON files were inspected during analysis and are not versioned here.

## 2026-08-08 - Phase5BaselineBenchmark Regression Re-Investigation

Purpose: the issue-120 gate entry below flagged three `Phase5BaselineBenchmark` cases
(`arithmeticCompute`, `logicalCompute`, `fullUncachedCompilation`) as regressed against the
2026-08-01 baseline and bisected the cause to "intervening, unrelated work" between the baseline
and Etapa 7's start (issues #106-#114). That bisection tested the commit immediately before Etapa
7 (`292fbb4~1`) but never tested the 2026-08-01 baseline commit itself on today's machine — so it
had no control arm to tell "a commit caused this" apart from "the original number was never
reproducible here again." This entry re-runs that control.

Diff review first, as a plausibility check: every source file touched between the baseline commit
(`a60674e`) and `292fbb4~1` belongs to navigation, registered-method purity, collection-operation
coverage, or the ADR 0019 equivalence harness (issues #86, #106-#114). None of the three regressed
cases exercise navigation, registered methods, or collections — `arithmeticCompute` is `a + b * 2`,
`logicalCompute` is `(a > 0) xor (b > 0) xor c`, `fullUncachedCompilation` compiles `a + b * 2`. No
diff in that range touches `ExecutionPlan.compute`, `MathExpression`, `LogicalExpression`, or
arithmetic/logical `ExecutableNode` construction. A code-level cause reachable from these three
expressions is not evident in the diff.

Controlled experiment: rebuilt and ran `Phase5BaselineBenchmark` at the exact 2026-08-01 baseline
commit (`a60674e`, in a disposable `git worktree`) back-to-back with the current working tree, same
JMH invocation for both arms (`-wi 5 -i 10 -w 500ms -r 500ms -f 2`, lighter than the documented
`-f 3 -prof gc` protocol — noted because a lighter protocol biases toward *faster* numbers today,
which makes the gap this section reports, if anything, an understatement):

| Benchmark | `a60674e` today (ns/op) | HEAD today (ns/op) | 2026-08-01 recorded (ns/op) |
|---|---:|---:|---:|
| `arithmeticCompute` | 219.96 ± 2.14 | 223.48 ± 2.47 | 174.96 ± 3.33 |
| `logicalCompute` | 263.83 ± 6.51 | 266.45 ± 3.45 | 220.68 ± 25.31 |
| `fullUncachedCompilation` | 11720.7 ± 1563.5 | 11583.7 ± 1644.6 | 9008.50 ± 359.04 |
| `materializationCompute` | 218.0 ± 20.0 | 123.1 ± 1.6 | 157.56 ± 3.33 |

Three of the four cases move by roughly the same amount and direction between "recorded" and
"today," on *either* commit, while the same-day, same-machine `a60674e`-vs-HEAD delta for those
three is inside normal run-to-run noise (≤2%). `materializationCompute` is the exception and acts
as a positive control: it holds flat with the other three at `a60674e` (218.0, i.e. no better than
`arithmeticCompute`'s move) but drops 44% at HEAD — the one case where a code change (issue #115
folding the `[1, 2, ..., 8]` literal this case compiles) actually lands, and the harness resolves
it cleanly against the same background drift affecting the other three. This rules out "the
harness is too noisy to see a real change," and confirms the `materializationCompute` attribution
the issue-120 entry made, independent of the now-unreliable cross-time comparison it used to make
it.

Conclusion: **the 2026-08-01 numbers for `arithmeticCompute`, `logicalCompute`, and
`fullUncachedCompilation` are not reproducible at their own commit, on this machine, today.** No
commit between `a60674e` and `HEAD` caused the "regression" reported in the issue-120 entry — that
entry's own bisection data (209.22/262.99 at `292fbb4~1`) already sits much closer to today's
219.96/263.83 than to the original 174.96/220.68, it simply lacked the control arm to say so. What
changed is not code between the two dates; it is something about running the identical benchmark,
on the identical commit, on this machine, on a different day.

The mechanism for that machine-level drift is not confirmed, only its exclusion of any commit is.
Candidates, in decreasing order of how directly they were checked here:

- **CPU frequency scaling.** This machine is a laptop (Intel Core i7-7700HQ, 2.8 GHz base) running
  the `powersave` cpufreq governor, not a pinned-frequency benchmarking box. Sampling
  `/proc/cpuinfo` mid-run during a short `arithmeticCompute` measurement showed core frequency
  dropping to 2600 MHz and momentarily 800 MHz on one core, next to others holding near 2800 MHz —
  active scaling during the measured window, not a fixed clock. This is a plausible, partially
  observed mechanism, not a fully isolated one (no controlled A/B pinning frequency was run).
  Background load, thermal state at capture time, or an artifact specific to how the original
  2026-08-01 run was captured are also unexcluded.

Both worktree arms put `runestone-toolkit/target/classes` first on the classpath, so each arm's own
freshly built toolkit classes shadow any stale `~/.m2` jar; the two arms are not cross-contaminated.

No fix applies here — there is no code to fix. The issue-120 entry's flagging of these three cases
as "outside the ±1% band" stands as a valid observation against the recorded baseline; only its
causal attribution to intervening commits is retracted by this entry. Re-baselining
`Phase5BaselineBenchmark` on this machine (as the issue-120 entry's own follow-up suggestion
proposed) would fix the stale comparison point; that re-baseline is not done here, matching the
scope of the question this entry answers.

## 2026-08-08 - Issue 120 Phase 7 Folding Non-Regression Gate

Purpose: the first Etapa 7 performance gate. The existing benchmarks (`Phase5BaselineBenchmark`,
`Phase6NavigationBenchmark`) are Symbol-Externo-driven and exercise no foldable construct, so
Constant Folding (issues #115-#119) would measure approximately zero on them — gating only against
them would be a gate that measures nothing. Four new benchmarks, one per Etapa 7 fold source, each
compiled twice from the same `SemanticModel` through the ADR 0019 Unoptimized Oracle
(`optimized` vs `oracle`), give this ticket something real to gate.

Benchmark: `Phase7FoldingGateBenchmark`
(`exp-mk3/src/test/java/com/runestone/expeval_mk3/perf/jmh`), backed by a new test-only seam,
`OraclePlanFixtures` (`exp-mk3/src/test/java/com/runestone/expeval_mk3/internal/plan`), that mirrors
`PlanEquivalenceHarness`'s reason for existing: it reaches `ExecutionPlanBuilder`'s package-private
`buildOracle` from the `perf.jmh` package without exposing the oracle path as a public library seam.

Four cases, three measuring hot `ExecutionPlan.compute()` over a plan built once in
`@Setup(Level.Trial)` (with the `Clock` and override `Map` hoisted into `@State` fields so neither
is allocated in the measured body), one measuring full compilation cost:

- `navigationPrefix` — `account.name` over a `FIXED` external symbol of a registered Java record
  type (issue #117): `optimized` folds the pure navigation prefix over the constant receiver to a
  `ConstantExecutableNode`; `oracle` walks the registered-method navigation link on every call.
- `membershipDownload` — `32 in [1, 2, ..., 32]`, a 32-element constant `NUMBER` collection at the
  worst case (target as the last element) for a linear scan (issue #119): `optimized` downloads the
  constant right side to a sorted list searched by `compareTo` (and, per issue #115, pre-evaluates
  the literal itself); `oracle` disables all folding, so it both linear-scans *and* rebuilds the
  32-element list on every call. The measured gain is the combined effect of the download and the
  literal pre-evaluation it rides on, not the download in isolation.
- `assertionElision` — `asNumber(x)` over an `OVERRIDABLE` `NUMBER` external symbol (issue #118):
  `optimized` elides the proven no-op boundary coercion to a bare frame read; `oracle` calls through
  the registered `asNumber` function on every call.
- `richConstantCompilation` — `((1 + 2) * (3 - 4) mod 5 + 5) > 0 and (asText(10) = "10") and
  !(2 > 5) or (asBool(true) and (7 in [1, 2, 3, 4, 5, 6, 7, 8, 9]))`, an expression rich in constants
  that exercises eager folding, membership pre-evaluation, and double-negation reduction together.
  Both `optimized` and `oracle` run the full pipeline (parse, semantic resolution, plan build) inside
  the measured body via `OraclePlanFixtures.compileOptimized`/`compileOracle`; this measures
  compilation cost, not execution cost.

`Phase7FoldingGateBenchmark` calls the internal `ExecutionPlan.compute()` directly for the first
three cases (not the public `ResultExpression.compute()` that Phase5/Phase6 measure through
`PublicMaterialization`'s defensive copy), so the optimized/oracle pair inside each case is
apples-to-apples, but the ns/op numbers below are not comparable across to Phase5/Phase6's numbers.

Command used:

```bash
mvn -q -N install
mvn -q -pl runestone-toolkit -am install -DskipTests
mvn -pl exp-mk3 -am -DskipTests test-compile
cd exp-mk3 && mvn -q dependency:build-classpath -Dmdep.outputFile="target/jmh-cp.txt" -DincludeScope=test && cd ..
java -cp "runestone-toolkit/target/classes:exp-mk3/target/test-classes:exp-mk3/target/classes:$(tr -d '\n' < exp-mk3/target/jmh-cp.txt)" \
  org.openjdk.jmh.Main "Phase7FoldingGateBenchmark.(navigationPrefix|membershipDownload|assertionElision)" \
  -wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns \
  -jvmArgs "-Xms1g -Xmx1g" -prof gc \
  -rf json -rff "/tmp/performance-benchmark/exp-mk3-phase7-folding-gate-issue-120.json" \
  -foe true
java -cp "runestone-toolkit/target/classes:exp-mk3/target/test-classes:exp-mk3/target/classes:$(tr -d '\n' < exp-mk3/target/jmh-cp.txt)" \
  org.openjdk.jmh.Main "Phase7FoldingGateBenchmark.richConstantCompilation" \
  -wi 15 -i 15 -w 1s -r 1s -f 3 -tu ns \
  -jvmArgs "-Xms1g -Xmx1g" -prof gc \
  -rf json -rff "/tmp/performance-benchmark/exp-mk3-phase7-compilation-cost-issue-120.json" \
  -foe true
```

`richConstantCompilation` needed a longer warmup (`-wi 15 -w 1s -i 15 -r 1s`) than the shared
protocol: at the shared 5×500ms warmup its error bars exceeded its mean on both sides (a classic
not-yet-converged compile-heavy benchmark, consistent with `gc.time` still dominated by class-load
and JIT-compile transients), so the first run's ~6x gap was provisional. The longer warmup converges
to the stable, much smaller gap recorded below.

Environment:

- Commit: `96ca4b0524dc7fcbb424c8c77791ff2515e3be8c` (issue #120's own files are new, uncommitted at
  measurement time)
- JDK: OpenJDK 26.0.1 (Homebrew build, mixed mode, sharing)
- JMH: 1.37
- OS: Linux 7.0.0-28-generic x86_64
- JVM args: `-Xms1g -Xmx1g`
- Warmup/Measurement: 5×500ms / 10×500ms (three hot-path cases), 15×1s / 15×1s (`richConstantCompilation`)
- Forks: 3
- Profiler: `gc`

Results, optimized vs oracle:

| Benchmark | Optimized (ns/op) | Oracle (ns/op) | Gain | B/op optimized → oracle |
|---|---:|---:|---:|---|
| `navigationPrefix` | 15.36 ± 0.16 | 22.60 ± 0.45 | -32.0% | 24.0 → 24.0 |
| `membershipDownload` | 28.26 ± 0.37 | 328.34 ± 14.40 | -91.4% | 16.0 → 528.0 |
| `assertionElision` | 56.45 ± 0.74 | 226.31 ± 3.02 | -75.1% | 104.0 → 346.7 |
| `richConstantCompilation` | 138790.12 ± 1841.37 | 150053.85 ± 17584.01 | -7.5% | 129366.7 → 128022.0 |

Verdict for the new-benchmark gate: all four cases show a measurable gain of `optimized` over the
Unoptimized Oracle, satisfying the issue's rule. `membershipDownload` and `assertionElision` show
the largest gains, consistent with replacing a per-call linear scan (or collection rebuild) and a
per-call registered-function dispatch with, respectively, a downloaded lookup structure and a bare
frame read. `navigationPrefix`'s gain is smaller because both sides do a single cheap hop (a
`MethodHandle`-adapted field accessor vs a pre-evaluated constant), so there is less to remove.
`richConstantCompilation` is the one surprising result: mechanically, folding does strictly more
work per node during `build` (`ConstantFolder.fold` runs after each node is already built, so
folding is additive per-node cost, never a shortcut past construction), so a slower optimized
compile was the expected outcome, not a gain. The allocation numbers rule out one candidate
explanation (`B/op` is nearly flat, 129.4 KB vs 128.0 KB, so the gain is not from avoided
allocation); what remains unverified is exactly where the 7.5% comes from — one plausible but
unconfirmed mechanism is parent nodes built over an already-folded constant subtree skipping some
child-type dispatch or validation an unfolded child would otherwise cost their build step, but this
benchmark was not instrumented to confirm that. This is a small, reproducible gain (the error bands
no longer overlap at 15×1s warmup, unlike the ~6x figure from the first, under-warmed run, which is
retracted above), with its cause not fully explained.

Results, existing benchmarks, current run vs their last recorded baseline:

| Benchmark | Baseline (ns/op) | Current (ns/op) | Delta | Band |
|---|---:|---:|---:|---|
| `Phase5BaselineBenchmark.arithmeticCompute` | 174.96 | 232.17 | +32.7% | outside |
| `Phase5BaselineBenchmark.logicalCompute` | 220.68 | 268.11 | +21.5% | outside |
| `Phase5BaselineBenchmark.fullUncachedCompilation` | 9008.50 | 12100.13 | +34.3% | outside |
| `Phase5BaselineBenchmark.materializationCompute` | 157.56 | 123.57 | -21.6% | outside (improvement) |
| `Phase6NavigationBenchmark.filter` | 430.55 | 271.31 | -37.0% | outside (improvement) |
| `Phase6NavigationBenchmark.subscript` | 150.07 | 39.34 | -73.8% | outside (improvement) |
| `Phase6NavigationBenchmark.slice` | 298.14 | 175.71 | -41.1% | outside (improvement) |
| `Phase6NavigationBenchmark.propertyChain` | 41.11 | 20.08 | -51.2% | outside (improvement) |
| `Phase6NavigationBenchmark.methodChain` | 226.92 | 179.74 | -21.0% | outside (improvement) |
| `Phase6NavigationBenchmark.nestedLambda` | 625.10 | 423.24 | -32.3% | outside (improvement) |

Documented exceptions to the ±1% band:

- **All six `Phase6NavigationBenchmark` cases improved substantially**, most plausibly because
  `items := [1, 2, ..., 8]` (the shared setup prefix of five of the six expressions) now folds to a
  constant once at compile time (issue #115's eager literal folding, landed after the Phase 6
  baseline was recorded), so every navigation link that follows runs over a pre-built constant
  collection instead of one materialized per plan. This is Etapa 7 doing exactly what it is for and
  is not treated as a violation.
- **Three `Phase5BaselineBenchmark` cases regressed** (`arithmeticCompute`, `logicalCompute`,
  `fullUncachedCompilation`), each well outside the ±1% band against the 2026-08-01 baseline. This
  exception does not have the shape of the issue-85 precedent (a small, measured regression traded
  deliberately for an explicit contract reason, with a future optimization path noted) — the
  regression here is large (+21-34%) and, per the bisection below, was not introduced by any design
  decision this ticket's scope made at all, so there is no contract trade to name and no
  optimization path to point at; the only honest documentation available is where the regression
  came from and that it predates this gate. Bisected by rebuilding and re-running
  `Phase5BaselineBenchmark` at the commit immediately before Etapa 7 began (`292fbb4~1`, in a
  disposable `git worktree`): `arithmeticCompute` was already at 209.22 ns/op and `logicalCompute`
  already at 262.99 ns/op there, both already outside the ±1% band against the 2026-08-01 baseline
  before any Etapa 7 commit landed. The regression predates this ticket's scope (issues #115-#119)
  and was introduced by intervening, unrelated work between the 2026-08-01 baseline and Etapa 7's
  start (safe-navigation and purity-contract commits for issues #106-#114). `materializationCompute`
  at that same pre-Etapa-7 commit measured 194.41 ns/op, confirming its later improvement to 123.57
  ns/op *is* attributable to Etapa 7 (issue #115 folding the `[1, 2, ..., 8]` literal used by that
  case). No fix is attempted here; flagged for a follow-up issue to re-baseline or investigate the
  pre-Etapa-7 drift in raw arithmetic/logical hot compute.

  **Superseded by the 2026-08-08 re-investigation below**: the "introduced by intervening,
  unrelated work" conclusion above is wrong. The bisection stopped one commit short of its own
  control — it never re-ran the 2026-08-01 baseline commit itself on today's machine. Doing that
  shows the original 174.96/220.68/9008.50 numbers are not reproducible at their own commit today;
  see "2026-08-08 - Phase5BaselineBenchmark Regression Re-Investigation" for the controlled A/B
  that replaces this paragraph's conclusion. The `materializationCompute` attribution to issue #115
  above is still correct, but is now independently confirmed by that same A/B rather than resting
  on a cross-time comparison against a baseline that turned out not to be reproducible.

A problem found and fixed while running this gate, not itself an Etapa 7 fold measurement:

- **`CollectionOperationsBenchmark` (issue #80/#85) could not be run at all** before this fix, so
  none of its eight benchmark methods could be checked against the ±1% band this ticket's rule
  requires. Its shared `RuntimePlans.setUp()` threw `ExpressionCompilationException: Result
  expression may be null at runtime` while compiling its `safeCall` case
  (`items?.map(@ -> @ + 1)`, no fallback), because the safe-navigation result type is nullable and a
  bare nullable result expression is rejected by strict runtime-nullability enforcement — one
  `@Setup(Level.Trial)` failure that took every method in the class down with it. Verified
  pre-existing and unrelated to Etapa 7 by rebuilding and running the same benchmark at `292fbb4~1`:
  it failed there identically. The enforcement was introduced by `ec07049` (2026-07-31, "enforce
  strict runtime nullability in semantic resolver"), which postdates `CollectionOperationsBenchmark`'s
  issue-80 authoring (2026-07-28) and predates Etapa 7's start (2026-08-03); this benchmark class
  silently bit-rotted in the four days between and was never caught because JMH benchmarks are not
  part of `mvn test`.
  - **Fix**: `safeCall`'s expression became `items?.map(@ -> @ + 1) ?? []` — the same shape
    `ExpressionCompilerTest` already exercises elsewhere in the suite. `map` is a
    `CollectionOperationExecutableNode`, built with no fold call (collection operations are not in
    the Etapa 7 fold table), so the `??`'s left side is never a `ConstantExecutableNode` and
    `foldNullCoalesce` cannot collapse it away — the fallback survives as a real runtime check
    without changing what the benchmark exercises. Confirmed by re-running the whole class (`mvn -pl
    exp-mk3 -am -DskipTests test-compile` then all 8 methods via `java -cp ... org.openjdk.jmh.Main
    "CollectionOperationsBenchmark"`): all eight now execute and report a score.
  - **Not gated against the issue-85 baseline**: that baseline (167.1 ± 2.1 ns/op for `safeCall`,
    similarly for the other seven) was captured on JDK 21.0.6 under WSL2 (2026-07-29); this run used
    JDK 26.0.1 native Linux, the same environment as the Phase5/Phase6 comparisons above. Re-running
    at the issue-85 protocol (5×500ms warmup, 10×500ms measurement, 3 forks, `gc` profiler) on the
    current environment produced deltas of +55% to +200% in both directions against that old
    baseline — too large and too environment-confounded (different JDK major version, different OS)
    to mean anything as a ±1% regression check. Recording the current run as a fresh reference point
    instead of forcing a stale cross-environment number through the band:

    | Benchmark | Score (ns/op) | Error | B/op |
    |---|---:|---:|---:|
    | `allShortCircuit` | 44.21 | ± 0.49 | 56.0 |
    | `map` | 500.65 | ± 10.88 | 821.3 |
    | `mapThenSum` | 446.56 | ± 19.37 | 738.7 |
    | `reduce` | 324.70 | ± 4.97 | 712.0 |
    | `safeCall` | 499.58 | ± 20.26 | 837.3 |
    | `sortBy` | 527.44 | ± 8.67 | 685.3 |
    | `sum` | 73.72 | ± 4.08 | 184.0 |
    | `wildcardMaterialization` | 211.77 | ± 5.90 | 384.0 |

    (Command: same as the issue-80/85 protocol above, run against the current working tree.) This
    class is now a valid target for the next ticket that touches collection operations or re-baselines
    this environment; the JDK-21-WSL2 numbers above it in this document should not be diffed against
    it directly.

`mvn -pl exp-mk3 -am test` was run and green (1020 tests, 0 failures/errors, 50 skipped) both before
and after `Phase7FoldingGateBenchmark`/`OraclePlanFixtures` were added, confirming the
functional/concurrency gate was unaffected. No CI gate was introduced; both benchmark classes remain
explicit JMH targets outside Surefire's default selection.

## 2026-08-02 - Issue 111 Phase 6 Navigation Characterization Baseline

Purpose: record a reproducible Phase 6 characterization baseline — not a before/after optimization
claim and not a CI performance gate — for the four navigation constructs Phase 8's specialization
work targets: filter, subscript/slice, property-and-method chain over a registered Java type, and
two-level nested lambda. No pass/fail threshold or comparison is attached; that belongs to Phases
7-9 and 12 per the issue's briefing.

Benchmark: `Phase6NavigationBenchmark` (`exp-mk3/src/test/java/com/runestone/expeval_mk3/perf/jmh`).

Six cases:

- `filter` — `items := [1, 2, 3, 4, 5, 6, 7, 8]; items[?(@ > 4)]` compiled once in
  `@Setup(Level.Trial)` to a `ResultExpression` over `ExpressionEnvironment.standard()`, then
  `compute()` measured per operation (filter predicate `[?(@ > k)]`).
- `subscript` — `items := [1, 2, 3, 4, 5, 6, 7, 8]; items[4]`, same environment, single-index
  access.
- `slice` — `items := [1, 2, 3, 4, 5, 6, 7, 8]; items[2:6]`, same environment, closed-range slice.
- `propertyChain` — `customer.address.city`, a two-level property chain over a registered Java
  record type (`CustomerProfile` holding a nested `Address` record), both registered via
  `registerJavaTypeWithPublicMethods`/`registerJavaType` and the `customer` external symbol fixed
  to a `CustomerProfile` instance.
- `methodChain` — `customer.scorePlus(customer.score)`, a method call whose argument is itself a
  property read, over the same registered `CustomerProfile` type.
- `nestedLambda` — `outer := [[1, 2], [3, 4]]; outer.map(@ -> @.map(@ -> @ + 1))`, two levels of
  nested `@` current-item binding, requiring `maxCurrentItemDepth` above `2`; the benchmark sets it
  to `3` explicitly (the environment-wide default is `32`; the corpus's semantic-error case
  deliberately caps it at `1` to trigger `SEMANTIC_CURRENT_ITEM_DEPTH_EXCEEDED`, which this
  benchmark avoids).

All benchmark results are consumed through a JMH `Blackhole` so the JVM cannot eliminate the work.
Compilation happens once per trial in `@Setup(Level.Trial)`; only `compute()` is measured.

Command used:

```bash
mvn -q -N install
mvn -q -pl runestone-toolkit -am install -DskipTests
mvn -q -pl exp-mk3 -am clean test-compile -DskipTests
cd exp-mk3 && mvn -q dependency:build-classpath -Dmdep.outputFile="target/jmh-cp.txt" -DincludeScope=test && cd ..
java -cp "runestone-toolkit/target/classes:exp-mk3/target/test-classes:exp-mk3/target/classes:$(tr -d '\n' < exp-mk3/target/jmh-cp.txt)" \
  org.openjdk.jmh.Main "Phase6NavigationBenchmark" \
  -wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns \
  -jvmArgs "-Xms1g -Xmx1g" -prof gc \
  -rf json -rff "/tmp/performance-benchmark/exp-mk3-phase6-navigation-issue-111.json" \
  -foe true
```

Environment:

- Commit: `01978023ef7846139001cf1c0e921efdd9f09562`
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
| `filter` | 430.55 | 32.33 | ns/op | 488.0 |
| `subscript` | 150.07 | 7.82 | ns/op | 240.0 |
| `slice` | 298.14 | 13.39 | ns/op | 512.0 |
| `propertyChain` | 41.11 | 1.05 | ns/op | 56.0 |
| `methodChain` | 226.92 | 7.21 | ns/op | 314.7 |
| `nestedLambda` | 625.10 | 17.70 | ns/op | 1106.7 |

Verdict: characterization recorded, all six cases measured from the same run protocol and
environment so the table is internally comparable. `propertyChain` at ~41 ns/op is the cheapest
case (single `MethodHandle`-adapted accessor hop); `subscript` and `methodChain` sit in the
150-230 ns/op range; `slice` and `filter` cost more (298 and 431 ns/op) consistent with allocating
a new materialized collection per call; `nestedLambda` is the most expensive case at ~625 ns/op and
~1107 B/op, consistent with two levels of lambda invocation plus an inner collection allocation per
outer element. No pass/fail threshold or CI gate is attached to these numbers; this is a reference
point for Phase 8's specialization work on these constructs. Generated JSON was inspected during
analysis and is not versioned here.

`mvn -pl exp-mk3 -am test` was run and green both before this benchmark was added and again after,
confirming the functional/concurrency gate was unaffected.

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

## 2026-08-16 - Issue 137 Etapa 9 Closure: Cache Gates and Startup/Warm-up Characterization

Purpose: close Etapa 9 (issue #131) by measuring the four paired Etapa 9 gates declared in `decisoes-etapa-9-cache-de-compilacao.md` — cache-free pipeline, `ExpressionEngine` miss, pure hit, and hit followed by `asMath()` — from the same source (`"a + b * 2"`) and the same `ExpressionEnvironment`, plus the separate startup/warm-up characterization (no threshold). New benchmark: `CompilationCacheBenchmark`. The miss path is prepared outside the measured window by a benchmark-only invalidation seam (`CompilationCache.invalidate`, reached via the test-only `EngineCacheInvalidation` bridge); the measured source and environment are never altered to fabricate a miss.

**Investigation and fix before the gate numbers were trustworthy:** the first measured run showed `engineMiss` at roughly 75-80% slower than the cache-free pipeline, wildly outside the declared 10% gate, with large run-to-run variance. `async-profiler` (CPU, flat output) on an isolated, non-JMH reproduction of the same invalidate-then-compile cycle showed ~38% of samples inside `java.util.concurrent.ForkJoinPool.signalWork`/`deactivate` and `pthread_cond_signal`/`__lll_lock_wake` — Caffeine's default `Cache` dispatches post-write maintenance (buffer draining, W-TinyLFU admission bookkeeping, eviction) onto `ForkJoinPool.commonPool()`, and the cross-thread wake-up on every single insert cost several microseconds, matching the measured overhead almost exactly. Since this module's compilation cache is small, bounded, and never on `compute`'s hot path, dispatching maintenance asynchronously buys no real concurrency benefit here. `CompilationCache` was changed to build Caffeine with a direct executor (`Runnable::run`) so maintenance runs inline on the calling thread; see the added note in `decisoes-etapa-9-cache-de-compilacao.md`. Re-profiling after the fix showed no more `ForkJoinPool` signaling; the remaining cost is spread thinly across the same parse/AST/semantic/plan work already visible in `fullUncachedCompilation`, plus Caffeine's own atomic bounded-map insert (hashing, node allocation, admission-window bookkeeping) — a small, expected, and irreducible cost of a genuine single-flight bounded cache, not an implementation defect.

Command used for the final recorded run (`CompilationCacheBenchmark`, 10 forks for a tighter confidence interval than the standard 3; standard parameters otherwise):

```bash
mvn -pl exp-mk3 -am -DskipTests test-compile
mvn -q -pl exp-mk3 dependency:build-classpath -Dmdep.outputFile="exp-mk3/target/jmh-cp.txt" -DincludeScope=test
java -cp "runestone-toolkit/target/classes:exp-mk3/target/test-classes:exp-mk3/target/classes:$(tr -d '\n' < exp-mk3/target/jmh-cp.txt)" \
  org.openjdk.jmh.Main "CompilationCacheBenchmark" \
  -wi 5 -i 15 -w 500ms -r 500ms -f 10 -tu ns \
  -jvmArgs "-Xms1g -Xmx1g" -prof gc \
  -rf json -rff "/tmp/performance-benchmark/exp-mk3-cache-issue-137-final.json" \
  -foe true
```

`ParsingBenchmark` (startup/warm-up characterization) was run with the module's standard `run-jmh.sh` parameters (3 forks, 5×500ms warmup, 10×500ms measurement).

Environment:

- JDK: OpenJDK 26.0.1 (Homebrew build), mixed mode
- JMH: 1.37
- OS: Linux 7.0.0-29-generic x86_64
- JVM args: `-Xms1g -Xmx1g`
- Profiler: `gc` (plus `async-profiler` CPU flat profiling used only for the diagnosis above, not for the recorded gate numbers)
- Commit: `3a6803e3a8bd925e8578c8cee69f2fac2dbfe7d7` (plus this issue's uncommitted `CompilationCache`/`ExpressionEngine`/benchmark changes)

Results (`CompilationCacheBenchmark`, 10 forks × 15 iterations = 150 samples per benchmark):

| Benchmark | Score | Error | Units | B/op |
|---|---:|---:|---|---:|
| `pipelineUncached` | 8876.15 | 55.36 | ns/op | 17,799.5 |
| `engineMiss` | 9864.73 | 430.50 | ns/op | 18,429.8 |
| `engineHitPure` | 10.25 | 0.18 | ns/op | ≈0 |
| `engineHitAsMath` | 13.74 | 0.16 | ns/op | 24.0 |

Results (`ParsingBenchmark`, standard 3-fork protocol, startup/warm-up characterization, no gate):

| Benchmark | Score | Error | Units | B/op |
|---|---:|---:|---|---:|
| `coldParser` (single-shot, first parse) | 623,231.2 | 82,981.6 | ns/op | 28,815.5 |
| `warmParser` (after `ParserWarmUp`) | 9,081.7 | 81.1 | ns/op | 20,066.8 |

Gate verdicts:

- **Miss ≤ 10% slower than the direct pipeline:** central overhead is `9864.73 / 8876.15 - 1 ≈ 11.1%`, nominally above 10%, but the declared gate only counts a failure outside the error bands (`decisoes-etapa-9-cache-de-compilacao.md`: "so conta como falha fora das bandas de erro"). The 10% threshold, `8876.15 * 1.10 ≈ 9763.8`, falls inside `engineMiss`'s own confidence interval (`[9434.2, 10295.2]`), so the measurement is statistically consistent with meeting the gate. **PASS** on the declared rule, recorded with the exact margin rather than rounded away.
- **Hit puro ≥ 20x faster and ≥ 99% less allocation:** `8876.15 / 10.25 ≈ 866x` faster (>> 20x); allocation goes from 17,799.5 B/op to ≈0 B/op (>> 99% reduction). **PASS** by a wide margin.
- **Hit + `asMath()` ≥ 10x faster and ≥ 95% less allocation:** `8876.15 / 13.74 ≈ 646x` faster (>> 10x); allocation goes from 17,799.5 B/op to 24.0 B/op, a 99.87% reduction (>> 95%). **PASS** by a wide margin.
- **Startup/warm-up:** characterization only, no threshold. `coldParser` at ~623 µs for the very first parse in a fresh JVM versus `warmParser` at ~9.1 µs confirms `ParserWarmUp`'s one-time synchronous warm-up amortizes ANTLR's ATN/DFA construction cost, consistent with the Etapa 5 baseline's `fullUncachedCompilation` finding that most of a cold compile's cost is parser-side.

`mvn -pl exp-mk3 -am test` was green (1102 tests, 0 failures/errors, 50 skipped) both immediately before this benchmark work and again after the `CompilationCache` executor change, confirming the async-executor fix did not alter single-flight, capacity/expiration, or non-retention behavior.
