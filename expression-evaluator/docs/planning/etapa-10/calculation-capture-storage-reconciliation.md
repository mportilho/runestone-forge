# Calculation-capture storage reconciliation

Issue #155 resolves the storage decision reopened by issue #139 before calculation capture enters the
production runtime. The public representation remains the accepted exact columnar payload with prebuilt
keys and an optional ordinal sidecar.

## Decision rule

Both candidates satisfy the semantic invariants and remain on the broad synthetic Pareto frontier. The
tie is therefore resolved in this order:

1. Reject a candidate that cannot preserve public ordinal order, reached nulls, folding provenance,
   per-occurrence CSE publication, or collection opacity.
2. Prefer end-to-end latency on corpus-derived current-scale shapes. Normalized working allocation is
   the secondary metric, provided retained payload size is equal and no shape has unbounded growth.
3. If current-scale latency remains inconclusive, prefer the simpler representation with lower
   worst-case working allocation.
4. Treat `S=0` separately: production bypasses calculation storage and publishes variable-only memory.
5. Keep `S=4/8` required-behavior shapes and the broad `S=16/64/256` matrix as stress guards, not as
   frequency weights for the current corpus.

## Corpus inventory

`CalculationMemoryCorpusShapeReport` resolves and builds every valid semantic/runtime corpus case, then
reports optimized frame length `F`, participating variable count `V`, and statically observable
calculation points `S` under the Etapa 10 inventory rules. Collection-operation arguments and repeated
bodies are opaque, and proven identity assertions eliminated by the optimizer add no point.

| Corpus measure | Count | `F` | `V` | `S` |
|---|---:|---:|---:|---:|
| Valid semantic/runtime plans | 144 | | | |
| p50 | | 1 | 1 | 0 |
| p95 | | 2 | 2 | 1 |
| maximum | | 8 | 8 | 2 |

Twenty-two current cases have at least one point: 18 have `S=1` and four have `S=2`. Static path analysis
finds 15 mandatory `S=1/K=1` cases, three safe `S=1/K=0..1` cases, three mandatory `S=2/K=2` cases, and
one safe chain with `S=2/K=0..2`. The benchmark includes the dense, empty, prefix, and gapped shapes
implied by those ranges instead of assuming the successful corpus fixture is the only future path.

Raw inventory: `/tmp/opencode/issue-155/corpus-shapes-final.csv`.

## Environment and method

- Eclipse Temurin 21.0.8+9-LTS, JMH 1.37, Linux x86_64
- fixed `-Xms1g -Xmx1g`
- final end-to-end run: 3 forks, 5x500 ms warmup, 10x500 ms measurement
- same-process diagnostic: no fork, 3x250 ms warmup, 5x250 ms measurement
- GC profiler and JSON output
- Maven, JMH driver, and forks pinned to the same Java 21 installation

The same-process run pairs frame-tail and append-only capture, freeze, and complete sequential indexed
consumption under one JVM. The forked run supplies the stronger end-to-end confidence intervals. Append
initial capacity is derived only from static `S` and capped at eight; it never uses future reached count
`K`. The first ordinal gap allocates the sidecar and growth is capped by `S`.

Raw results:

- `/tmp/opencode/issue-155/java21-representative-corrected-same-process.json`
- `/tmp/opencode/issue-155/java21-representative-corrected-same-process.log`
- `/tmp/opencode/issue-155/java21-representative-corrected-forked.json`
- `/tmp/opencode/issue-155/java21-representative-corrected-forked.log`
- `/tmp/opencode/issue-155/java21-safe-reachability-forked.json`
- `/tmp/opencode/issue-155/java21-safe-reachability-forked.log`
- `/tmp/opencode/issue-155/java21-safe-reachability-same-process.json`
- `/tmp/opencode/issue-155/java21-safe-reachability-same-process.log`
- `/tmp/opencode/issue-155/retained-layout-final.csv`

## End-to-end results

Each cell is `ns/op / B/op`; lower is better. The operation includes capture, public result
materialization, exact columnar freeze, envelope construction, and sequential indexed consumption.

| Shape | Frame tail | Append-only | Interpretation |
|---|---:|---:|---|
| Corpus p50, `F=1,V=1,S=0,K=0` | 45.06 / 152 | **42.01 / 152** | no calculation storage in production |
| Corpus p95, `F=2,V=2,S=1,K=1` | 74.70 / 216 | **65.40 / 208** | append wins latency and allocation |
| Corpus safe skipped, `F=1,V=1,S=1,K=0` | 50.72 / **152** | **47.19** / 192 | latency favors append; frame saves 40 B/op |
| Corpus maximum dense, `F=8,V=8,S=2,K=2` | 154.97 / 232 | **146.31 / 224** | append wins latency and allocation |
| Corpus maximum gapped, `F=8,V=8,S=2,K=1` | **163.71 / 312** | 170.84 / 360 | frame wins latency and allocation |
| Corpus safe skipped, `F=1,V=1,S=2,K=0` | 49.77 / **160** | **48.42** / 192 | latency tied; frame saves 32 B/op |
| Corpus safe prefix, `F=1,V=1,S=2,K=1` | 63.63 / **184** | **58.40** / 200 | latency favors append; frame saves 16 B/op |
| Required nested dense, `F=4,V=2,S=4,K=4` | 107.83 / 240 | **93.33 / 224** | append wins both |
| Required assignment dense, `F=8,V=6,S=8,K=8` | 214.37 / 272 | **182.71 / 240** | append wins both |
| Required lazy prefix, `F=4,V=2,S=4,K=1` | 80.80 / **232** | **73.92** / 248 | latency favors append; frame saves 16 B/op |
| Required lazy gapped, `F=8,V=4,S=8,K=3` | **161.39 / 336** | 171.95 / 408 | frame wins both |

The dominant mandatory corpus shape (`S=1,K=1`) gives append-only a statistically resolved 12.5%
latency win and 8 B/op reduction. Dense `S=2`, the maximum current corpus shape, improves by 5.6% and
also saves 8 B/op. Safe skipped/prefix shapes favor append latency but frame allocation; non-prefix
gaps favor frame-tail. The required dense extensions favor append while lazy extensions remain mixed.
This evidence supports a
single append-only strategy for current workloads, but does not claim universal dominance.

JOL reports identical retained graphs for both candidates in every shape because freeze produces the
same exact schema and value/ordinal arrays. Working recorder/frame arrays are not retained.

## Semantic validation

The performance fixture models storage from `(F,V,S,reached ordinals)`. A separate functional prototype,
`CalculationCaptureOrderingPrototypeTest`, executes the required empty/root/nested, assignment/result,
short-circuit, conditional, coalescence, folding replay, CSE miss/hit, and opaque-collection flows. It
asserts monotonic unique ordinals, provider invocation counts, and frame-tail/append payload equivalence.
The same invariants must remain covered by production tests in issues #141-#143:

- inventory ordinals follow evaluation order, with assignments before result and children before their
  enclosing frontier;
- lazy branches omit ordinal ranges but never execute an earlier ordinal afterward;
- every non-opaque source occurrence executes at most once per top-level computation;
- folded replacements replay the original ordered provenance group;
- every reached memoized occurrence publishes its own ordered group without invoking providers again;
- collection operations, lambdas, and repeated predicates/bodies are opaque and contribute no writes.

These constraints make append writes monotonic. The recorder keeps an implicit dense prefix and creates
an ordinal sidecar only on the first gap. Count represents presence, including reached runtime null.

## Verdict

**ACCEPT append-only as the single production calculation-capture strategy.** Keep frame-tail only as a
benchmark control. Keep exact columnar publication, mode-first branching, and increment-during-capture
counting from issue #139. Dense plus bitmap and adaptive/multiple production strategies remain discarded.

This verdict authorized issue #141 after its frame-tail wording was updated. The production integration
below preserves recorder-free `compute()`, proves semantic parity with the unoptimized oracle for the
non-folded/non-memoized scope of #141, and measures the real plan path. Frame-tail remains in the storage
prototype as the paired control for future capture changes.

## Production integration check

Issue #141 added a paired benchmark over the real compiled-plan path after the storage verdict. Temurin
21.0.8, three forks, 5x500 ms warmup, 10x500 ms measurement, `-Xms1g -Xmx1g`, and the GC profiler gave:

| Production shape | `compute()` | `computeWithMemory()` |
|---|---:|---:|
| `S=0` | 15.41 ns / 24 B | 49.83 ns / 144 B |
| dense `S=1,K=1` | 29.49 ns / 56 B | 80.42 ns / 232 B |
| leading gap `S=2,K=1` | 28.69 ns / 56 B | 130.33 ns / 344 B |

The normal route constructs `ExecutionScope` through its recorder-free constructor and retains the
existing exact frame size. The memory route pays for the recorder, exact public payload, envelope, and
indexed consumption shown above. Raw results are in
`/tmp/opencode/issue-141/java21-production-path.json` and `.log`.
