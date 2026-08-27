# Binding benchmark: persistable calculation-memory payload

> Historical binding result. Issue #155 resolved the reopened storage decision in favor of append-only
> capture on Java 21. See
> [`calculation-capture-storage-reconciliation.md`](calculation-capture-storage-reconciliation.md).

Issue #139 evolves the historical storage prototype into the persistable contract required before
Etapa 10 production work. The fixture remains entirely under `src/test`: this ticket introduces no
calculation-memory production code.

## Contract modeled

`CalculationMemoryStoragePrototypeBenchmark` now uses:

- prebuilt `VariableKey`, `CalculationKey`, provenance and source-span objects;
- a standalone schema containing only explicit variable slots and public keys, with no plan, node,
  environment, source, AST or semantic-model reference;
- interleaved external/internal variable slots, Item Atual slots and memo slots;
- full and assignments-only variable schemas sharing the same calculation metadata;
- exact variable and calculation value columns plus a gapped-only ordinal sidecar;
- immutable on-demand list projections and an eager-entry control;
- identical public payload checks for frame-tail and append-only capture;
- public-result materialization before freeze; and
- a sequential no-I/O sink that consumes every key field and representative scalar/collection values.

The parameter matrix is the Cartesian product of `S=0,1,4,16,64,256` and `EMPTY`, `DENSE`,
`PREFIX`, `ONE_POINT`, `ALTERNATING`, and `SPARSE`. A JUnit fixture executes all 36 combinations.
Dense plus bitmap was not reintroduced; its dominated historical result remains in
`prototype-calculation-memory-storage.md`.

## Environment and commands

Effective JVM for Maven, the JMH driver and every fork: Eclipse Temurin 25.0.3+9-LTS. JMH 1.37,
Linux x86_64, fixed `-Xms1g -Xmx1g` heap. The module release target remains Java 21, but no Java 21
deployment JVM is installed in this workspace. These results are binding for the local design gate,
not the required deployment compatibility rerun.

The scored runs used two forks, 3x250 ms warmup, 5x250 ms measurement, `gc` profiler and JSON output.
The shorter iterations allow the complete 36-case matrix to run in one paired invocation; every
reported pair therefore used the same JVM, process protocol and machine state.

```bash
export JAVA_HOME=/home/marcelo/.sdkman/candidates/java/25.0.3-tem
export PATH="$JAVA_HOME/bin:$PATH"
mvn -q -pl exp-mk3 -am test-compile
mvn -q -pl exp-mk3 dependency:build-classpath \
  -Dmdep.includeScope=test -Dmdep.outputFile=target/jmh-cp.txt
CP="exp-mk3/target/test-classes:exp-mk3/target/classes:runestone-toolkit/target/classes:$(cat exp-mk3/target/jmh-cp.txt)"

java -cp "$CP" org.openjdk.jmh.Main \
  '.*CalculationMemoryStoragePrototypeBenchmark.(computeWithMemoryFrameColumnar|computeWithMemoryAppendColumnar|computeWithMemoryFrameEager|traverseIndexed|traverseLists|consumeColumnarSequentially)' \
  -wi 3 -i 5 -w 250ms -r 250ms -f 2 -tu ns \
  -jvmArgs '-Xms1g -Xmx1g' -prof gc -rf json -rff full-matrix.json -foe true

java -cp "$CP" org.openjdk.jmh.Main \
  '.*CalculationMemoryStoragePrototypeBenchmark.(captureFrameTail|captureAppend|materializePublicResult|freezeFrameTail|freezeAppend|freezeEagerEntries|traverseEagerEntries|consumeEagerSequentially|countDuringCapture|countDuringFreeze)' \
  -p slotCount=64 -wi 3 -i 5 -w 250ms -r 250ms -f 2 -tu ns \
  -jvmArgs '-Xms1g -Xmx1g' -prof gc -rf json -rff phase-attribution.json -foe true

java -cp "$CP" org.openjdk.jmh.Main \
  '.*CalculationMemoryStoragePrototypeBenchmark.branch.*' \
  -wi 3 -i 5 -w 250ms -r 250ms -f 2 -tu ns \
  -jvmArgs '-Xms1g -Xmx1g' -prof gc -rf json -rff branches-v2.json -foe true

java -cp "$CP" org.openjdk.jmh.Main \
  '.*CalculationMemoryStoragePrototypeBenchmark.computeAndConsume.*' \
  -wi 3 -i 5 -w 250ms -r 250ms -f 2 -tu ns \
  -jvmArgs '-Xms1g -Xmx1g' -prof gc -rf json -rff integrated-flow-v2.json -foe true

java -cp "$CP" org.openjdk.jmh.Main \
  '.*CalculationMemoryStoragePrototypeBenchmark.branch.*' \
  -wi 3 -i 1 -w 250ms -r 250ms -f 1 -tu ns \
  -jvmArgs '-Xms1g -Xmx1g -XX:+UnlockDiagnosticVMOptions -XX:+PrintInlining'

java -Djol.magicFieldOffset=true -cp "$CP" \
  com.runestone.expeval_mk3.perf.jmh.CalculationMemoryStorageLayoutReport
```

JSON, logs and the complete JOL CSV are retained under `/tmp/opencode/issue-139/` and
`/tmp/opencode/issue-139-retained-bytes.csv` pending explicit cleanup approval.

## Results

Selected return-flow cells are `ns/op / B/op`; lower is better. The JSON contains all 36 cells for each
benchmark. Return flow includes capture, one public materialization, freeze and the computation
envelope, but not subsequent consumption.

| Shape | Frame + columnar | Append + columnar | Frame + eager |
|---|---:|---:|---:|
| `S=0`, empty | **53.6 / 288** | 56.7 / 312 | 75.9 / 472 |
| `S=1`, dense | 63.4 / 304 | **59.8 / 296** | 87.1 / 512 |
| `S=4`, dense | 102.1 / 320 | **71.9 / 304** | 114.4 / 600 |
| `S=16`, prefix 4 | 94.1 / 368 | **71.9 / 304** | 133.7 / 648 |
| `S=64`, dense | 554.8 / **800** | **509.4** / 1,112 | 720.1 / 2,520 |
| `S=64`, alternating | **367.9 / 840** | 422.7 / 1,312 | 461.2 / 1,624 |
| `S=256`, dense | 1,711.9 / **2,336** | **1,515.6** / 4,280 | 3,049.4 / 8,664 |
| `S=256`, sparse 4 | 552.7 / 1,360 | **76.8 / 336** | 665.3 / 1,608 |

Phase attribution at `S=64` explains the crossover:

| Reach | Tail capture | Append capture | Tail freeze | Append freeze |
|---|---:|---:|---:|---:|
| empty | 58.6 / 392 | **25.6 / 168** | 52.2 / 96 | 46.2 / 96 |
| dense | **356.0 / 392** | 461.5 / 984 | 183.6 / 352 | **35.8 / 80** |
| alternating | **157.2 / 392** | 374.5 / 1,152 | 238.1 / 368 | **35.8 / 80** |
| sparse 4 | 58.9 / 392 | **45.7 / 208** | 158.1 / 144 | **36.0 / 80** |

Public result materialization is independently stable at 8.8-8.9 ns/op and 56 B/op. It occurs once
in every candidate. At `S=64`, eager freeze ranges from 69.5 ns/280 B when empty to 408.3 ns/2,072 B
when dense. Because eager consumption is faster after publication, a second full 36-cell matrix
integrates `capture -> materialize -> freeze -> sequential sink` in one measured operation:

| Shape | Frame columnar | Append columnar | Frame eager |
|---|---:|---:|---:|
| `S=0`, empty | **121.5 / 264** | 123.2 / 304 | 125.4 / 456 |
| `S=1`, dense | 146.9 / 296 | **145.1 / 288** | 157.2 / 512 |
| `S=4`, dense | 272.0 / 312 | **188.4 / 296** | 195.7 / 600 |
| `S=16`, prefix 4 | 219.4 / 360 | **179.7 / 296** | 228.4 / 648 |
| `S=64`, dense | 1,011.7 / **824** | **925.1** / 1,120 | 1,143.7 / 2,520 |
| `S=64`, alternating | 722.2 / **864** | **642.5** / 1,320 | 718.9 / 1,648 |
| `S=256`, dense | 4,252.0 / **2,360** | **3,468.8** / 4,306.0 | 3,992.3 / 8,697.8 |
| `S=256`, sparse 4 | 700.3 / 1,352 | **200.7 / 328** | 744.4 / 1,608 |

Confidence intervals overlap in several close latency cells, so point-score ordering is not treated as
dominance. Columnar publication remains Pareto-winning: it cuts hundreds to thousands of B/op against
eager in every shape, while integrated latency is comparable or better except the empty point score.
Eager therefore does not win by moving cost from publication into consumption.

Indexed traversal is allocation-free within profiler precision through `S=64` (at most 0.24 B/op
profiler noise). The dense `S=256` sink reported 32 B/op and list traversal 241 B/op; these are
JIT/escape-analysis effects at the largest case and do not affect publication allocation. List
projection records were scalar-replaced in smaller/sparse cases and become visible in the dense large
case, which is why list cost remains explicitly separate from indexed persistence.

JOL retained graphs include values and the standalone shared schema:

| Shape | Columnar bytes | Eager bytes |
|---|---:|---:|
| `S=0`, empty | 1,344 | 1,392 |
| `S=4`, dense | **2,264** | 2,392 |
| `S=64`, dense | **11,816** | 13,144 |
| `S=64`, alternating | 11,352 | **7,264** |
| `S=256`, dense | **43,592** | 48,760 |
| `S=256`, sparse 4 | 38,808 | **2,248** |

The sparse difference is intentional: retaining one columnar memory retains its complete standalone
schema, while eager entries retain only reached keys. The schema is shared by every memory from the
same compiled expression, so this cost is amortized across executions but still matters for a single
long-lived sparse memory. It keeps eager on the retained-byte frontier but does not offset eager's
complete-flow CPU/allocation loss.

## Branch and counting verdicts

The corrected branch fixture represents eight distinct opaque descendants without a repetition loop;
trial-level setup also removes frame allocation from measured B/op. `mode-first` is clearly best on the
normal inactive path: 1.52 +/- 0.02 ns for markable nodes and 3.42 +/- 0.12 ns for repeated opaque
descendants, versus fused at 1.71 +/- 0.05 and 4.56 +/- 0.04 ns. Fused wins the active markable path
(2.39 +/- 0.05 versus mode-first 3.34 +/- 0.07 ns), while active opaque scores are close (4.57 +/- 0.12
versus 4.65 +/- 0.22 ns). Choose **mode-first** as the Pareto winner because `compute()` is the primary
path and it wins both inactive shapes decisively; slot-first is dominated. All arms allocate effectively
zero bytes.

Incrementing during capture beats post-capture counting for empty, prefix, one-point, alternating and
sparse reachability. At alternating `S=64`, it is 155.5 ns versus 212.2 ns; at sparse, 59.1 ns versus
65.1 ns. Dense scores overlap (351.6 versus 342.5 ns with roughly 70-97 ns errors). Choose
**increment during capture**, because post-counting has no allocation advantage and loses decisively on
gapped representative loads.

The diagnostic `PrintInlining` run confirms all three per-node helpers and all three eight-descendant
methods inline as hot code. A `perfnorm` attempt failed with `No supported events` on this host, so
branches/op and branch-miss counters are unavailable locally. This limits the branch verdict to JMH
latency, allocation and verified inlining; it does not waive the Java 21 deployment rerun.

## Binding verdict

- **Storage: REOPEN; production work BLOCKED.** Exact compact freeze changes the historical result.
  Frame-tail remains Pareto-relevant for alternating reach and dense allocation, but append dominates
  it in complete current-scale `S=1`, `S=4`, prefix and large-sparse cases. The accepted frame-tail
  default therefore fails the issue's stop rule. This result does not automatically promote append;
  the production storage decision must be reconciled before runtime code is introduced.
- **Publication: ACCEPT columnar.** It remains Pareto-winning through its substantial complete-flow
  allocation advantage with overlapping latency in close cells, keeps indexed consumption
  allocation-free through current scale, and remains Pareto-relevant when retained schema bytes are
  included.
- **Branch: ACCEPT mode-first.** It decisively protects both normal inactive shapes and remains
  competitive for repeated active opaque descendants without a second plan.
- **Reach count: ACCEPT increment during capture.** It wins gapped loads and ties within uncertainty on
  dense reach.
- **Dense bitmap: remains discarded historical evidence.** It was not restored as a production
  candidate.

The Java 21 deployment rerun remains mandatory after the reopened storage decision is resolved. No
production calculation-memory implementation is authorized by this local result.
