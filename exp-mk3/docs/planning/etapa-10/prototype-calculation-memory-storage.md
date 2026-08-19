# PROTOTYPE ONLY: Calculation-memory storage

> Historical result. Issue #139 added the binding persistable-contract benchmark and reopened the
> frame-tail storage decision. See
> [`binding-persistable-payload-benchmark.md`](binding-persistable-payload-benchmark.md). Dense plus
> bitmap remains discarded; the historical numbers below are preserved as prior evidence.

Question: which of the three most promising execution-local storage models should Etapa 10 use for
`computeWithMemory()`?

This prototype is intentionally outside production code. It compares:

1. An extended execution frame whose calculation tail is transferred to the returned memory.
2. An append-only recorder with a lazy slot-ID sidecar and compact ownership transfer at freeze.
3. A dense `Object[]` plus `long[]` presence bitmap compacted at freeze.

The JMH fixture is
[`CalculationMemoryStoragePrototypeBenchmark`](../../../src/test/java/com/runestone/expeval_mk3/perf/jmh/CalculationMemoryStoragePrototypeBenchmark.java).
Each capture benchmark includes the normal frame clone/copy, calculation writes, variable inclusion,
and immutable-memory freeze. Separate traversal benchmarks consume equivalent logical memories.

## Scenarios

| Scenario | Static calculation slots | Reached pattern |
|---|---:|---|
| `VARIABLES_ONLY` | 0 | no calculation slots |
| `SMALL_DENSE` | 4 | all 4 |
| `MEDIUM_DENSE` | 64 | all 64 |
| `MEDIUM_ALTERNATING` | 64 | every other slot |
| `LARGE_NONE` | 256 | no slots reached |
| `LARGE_DENSE` | 256 | all 256 |
| `LARGE_PREFIX` | 256 | first 16 slots |
| `LARGE_SPARSE` | 256 | 4 spread-out slots |

All scenarios include a 24-element normal frame, eight effective variables, unbound internal slots,
and captured nulls. Trial setup rejects any strategy that produces a different size or checksum.

## Run

Build the benchmark classpath from the repository root:

```bash
mvn -q -pl exp-mk3 -am test-compile
mvn -q -pl exp-mk3 dependency:build-classpath \
  -Dmdep.includeScope=test \
  -Dmdep.outputFile=target/jmh-cp.txt
CP="exp-mk3/target/test-classes:exp-mk3/target/classes:$(cat exp-mk3/target/jmh-cp.txt)"
java -cp "$CP" org.openjdk.jmh.Main \
  '.*CalculationMemoryStoragePrototypeBenchmark.*' \
  -wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns \
  -jvmArgs '-Xms1g -Xmx1g' -prof gc -foe true
```

Pin `JAVA_HOME` and verify `java -version` and `mvn -version` before recording a verdict.

## Verdict

Environment: Temurin 25.0.3, JMH 1.37, Linux x86_64, `-Xms1g -Xmx1g`, 5x500 ms warmup,
10x500 ms measurement, 3 forks, and the GC profiler. Maven and JMH were explicitly pinned to the same
JVM. The module targets Java 21, so this is a local comparative verdict; the production gate must be
repeated on the Java 21 deployment JVM.

### Capture and freeze

Each cell is `ns/op / B/op`; lower is better. `B/op` includes discarded working arrays as well as the
returned memory.

| Scenario | Frame tail | Append | Dense bitmap |
|---|---:|---:|---:|
| `VARIABLES_ONLY` | **21.4 / 144** | 30.0 / 184 | 33.0 / 216 |
| `SMALL_DENSE` | **28.8 / 160** | 58.5 / 200 | 64.3 / 256 |
| `MEDIUM_DENSE` | **259.9 / 400** | 442.2 / 1,056 | 632.7 / 736 |
| `MEDIUM_ALTERNATING` | **112.5 / 400** | 477.0 / 1,730.7 | 357.6 / 784 |
| `LARGE_NONE` | 174.5 / 1,168 | **30.1 / 184** | 186.8 / 1,272 |
| `LARGE_DENSE` | **1,049.7 / 1,168** | 1,815.6 / 3,560 | 2,523.8 / 2,296 |
| `LARGE_PREFIX` | 171.7 / 1,168 | **150.4 / 448** | 214.0 / 1,336 |
| `LARGE_SPARSE` | 172.8 / 1,168 | **94.2 / 424** | 206.9 / 1,352 |

### Complete traversal

Append and dense freeze to the same compact representation, so their traversal scores are equivalent.
The frame tail scans the static calculation tail and therefore pays for `S` even when few points were
reached.

| Scenario | Frame tail | Compact append/dense |
|---|---:|---:|
| `VARIABLES_ONLY` | 13.1 ns | **12.4 ns** |
| `SMALL_DENSE` | 23.8 ns | **20.5 ns** |
| `MEDIUM_DENSE` | **128.7 ns** | 134.9-135.8 ns |
| `MEDIUM_ALTERNATING` | 77.6 ns | **68.7-68.8 ns** |
| `LARGE_NONE` | 70.8 ns | **12.3-12.5 ns** |
| `LARGE_DENSE` | **478.6 ns** | 513.7-514.1 ns |
| `LARGE_PREFIX` | 156.5 ns | **42.1-42.2 ns** |
| `LARGE_SPARSE` | 94.4 ns | **21.0 ns** |

Traversal is allocation-free for all three alternatives within profiler precision.

### Decision

1. **Use the extended frame as the first Etapa 10 implementation.** It wins both latency and allocation
   for every measured case through `S=64`, and again for a large dense plan. It also has the smallest
   implementation surface: one execution array, no recorder, no growth policy, no freeze copy, and no
   second runtime representation.
2. **Keep append-only as a documented fallback, not production code.** It decisively wins when a plan
   has 256 possible points and reaches zero or only a small subset. Reconsider it only if instrumentation
   over real compiled plans finds a material population with large `S` and low `K/S`.
3. **Discard dense plus bitmap.** Frame tail or append dominates it in every scenario. It adds two
   working arrays and an `O(S)` freeze scan without owning a useful part of the Pareto frontier.

The module corpus supports choosing the simpler first implementation: its 212 runtime expressions have
source-length `p50=22`, `p95=60`, and `max=166` characters. Observable calculation slots are fewer than
source tokens, and collection-navigation descendants are deliberately opaque, so even `S=64` is a
conservative large case for the current module. Source length is only a proxy; production plan telemetry,
not this corpus statistic, is the gate for adding an append fallback.

The later persistence-oriented review selected exact columnar value payloads, prebuilt standalone keys,
allocation-free indexed traversal, and immutable `List` projections. Full key/value consumption remains
the dominant operation, but it no longer requires a transient `VariableEntry`/`CalculationEntry` object
for a persistence adapter. Before production, the binding follow-up benchmark must include capture,
freeze, indexed consumption, list consumption, and a sequential no-I/O persistence sink. It must also
compare branch order and per-capture versus post-execution reach counting. If frame-tail or compact
publication stops winning representative current-scale cases, that choice is reopened; append remains
the only capture-storage fallback candidate.

The fixture's contiguous variable prefix and pre-known reached count are not representative of the real
runtime. The binding benchmark must use explicit interleaved variable slots, full-result versus
assignments-only schemas, result materialization before freeze, and no temporary execution holder.

Raw JSON results are retained outside the repository pending cleanup approval:

- `/tmp/opencode/exp-mk3-calculation-memory-prototype/capture-final.json`
- `/tmp/opencode/exp-mk3-calculation-memory-prototype/traverse-final.json`
- `/tmp/opencode/exp-mk3-calculation-memory-prototype/large-dense-final.json`
