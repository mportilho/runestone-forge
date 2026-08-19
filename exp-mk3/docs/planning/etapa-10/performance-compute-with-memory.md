# JVM Performance Research for `computeWithMemory()`

Date: 2026-08-16; resolved after prototype and design session on 2026-08-18
Scope: Java 21, `exp-mk3` only, research and measurement guidance; no production-code change.

Final status: the storage prototype selected the execution-frame tail as the implementation base. The
subsequent persistence-oriented review selected exact columnar value payloads, prebuilt standalone keys,
allocation-free indexed traversal, and immutable `List` projections for convenience. Eager entry arrays
are retained only as a benchmark control because they duplicate the transient object graph that a
persistence adapter normally consumes once. The final plan is
[`etapa-10-memoria-de-calculo.md`](./etapa-10-memoria-de-calculo.md), and ADR 0023 records the
architectural choice. A binding JMH gate with the real API and a sequential persistence sink remains
required before production.

## Fixed architecture

This report assumes the agreed design rather than reopening it:

- One cached immutable execution plan serves `compute()` and `computeWithMemory()`.
- Existing executable-node classes carry an immutable primitive `calculationSlot`; inactive encoding is an internal benchmark decision.
- There are no `Captured*` node variants, wrappers, decorators, or second plan.
- The planner marks only observable calculation frontiers. Calls inside opaque collection navigation are not captured.
- Normal execution uses the existing frame length. Memory execution may extend that execution-local frame with calculation slots, or use an execution-local recorder if measurement favors it, and returns the result plus compact immutable calculation memory.

The current runtime already creates a fresh [`ExecutionScope`](../../../src/main/java/com/runestone/expeval_mk3/internal/runtime/ExecutionScope.java) and clones its frame for each call. A prior per-thread whole-scope pool saved only 80 B/op (3.58%) and did not improve latency, so it was removed ([performance history](../../perf/performance-history.md#2026-08-13---issue-129-executionscope-pool-permanence-decision)). That result does not prove that every recorder optimization will fail, but it sets a high evidence bar for reuse and pooling.

## Executive conclusion

Keep the execution-local state on heap and ordinary. For this runtime, the first implementation to measure should be:

1. Append calculation slots after the normal frame, without enlarging the cached frame template.
2. Keep normal `compute()` on the current exact-size clone; let `computeWithMemory()` make one `Arrays.copyOf` to the larger memory-enabled size.
3. Read variables through explicit schema slot arrays, because semantic variables and current-item slots are interleaved; write only dynamic calculation-frontier values into the appended slots.
4. Use the zero-initialized tail's `null` as "not reached" and one private sentinel as "reached with null", avoiding fill, bitmap, recorder object, and second values array.
5. Materialize the ordinary public result first, then freeze successful execution into exact value arrays and an ordinal sidecar only for gaps; discard the temporary frame afterward. Lambda/current-item slots must still obey their existing `finally` discipline.
6. Prebuild public keys in standalone provenance metadata shared by memories without any back-reference to `ExecutionPlan`, executable nodes, source text, or the environment.
7. Expose allocation-free indexed key/value access for persistence and immutable `variables()` / `calculations()` projections for convenience. Do not create or cache entry records during compute/freeze.

This shape exploits an allocation the runtime already makes and avoids a recorder while execution is in
progress. Compact publication means the frame is discarded after freeze and each returned memory owns
only reached values plus optional ordinals, while keys and source spans are amortized by a standalone
schema. The append recorder below remains the principal control and may win for unusually large, sparse
plans. Capture storage and publication layout are separate benchmark axes.

## Ranked recommendations

| Rank | Recommendation | Expected effect | Confidence / gate |
|---:|---|---|---|
| 1 | Append calculation slots to the execution frame only for `computeWithMemory()`, then freeze exact reached-value arrays | One execution array and no recorder, growth policy, or per-entry object in the hot computation | Prototype winner; binding recheck with compact publication |
| 2 | Append recorder with lazy values allocation and lazy ordinal sidecar | Working allocation scales with reached `K`, not static `S`, before producing the same compact payload | High if monotonic execution is provable; use as the main control |
| 3 | Standalone prebuilt keys plus exact columnar values and optional ordinals | Amortizes provenance, prevents plan retention, and avoids eager entry records before persistence | High; compare against eager entries with JMH/JOL |
| 4 | Compare slot-first, mode-first, and fused absolute-slot branch shapes | Protects `compute()` and opaque repeated descendants without assuming source shape predicts machine code | Binding generated-code and branch-counter gate |
| 5 | Read participating variables from canonical frame slots and preserve both external/internal shadowed keys | Eliminates a variable recorder and preserves the effective values that explain execution | High; folding and ordering tests required |
| 6 | Compare per-capture counting against post-execution counting | Trades a write at every reached point and a scope field against an extra linear freeze scan | Binding capture/freeze split benchmark |
| 7 | Keep append-only only as a documented fallback; discard dense and adaptive forms | Avoids a second production representation without real large-sparse demand | Prototype evidence; reconsider only from production telemetry |

## Primary candidate: calculation tail in the execution frame

Let `F` be the normal frame length, including CSE memo slots, and `S` the number of dynamic calculation slots. The planner assigns every marked node an absolute slot in `[F, F + S)`. The cached `frameTemplate` remains length `F`.

- `compute()` keeps the existing `frameTemplate.clone()` and therefore cannot retain or allocate calculation storage.
- `computeWithMemory()` creates `Arrays.copyOf(frameTemplate, F + S)`, so the appended slots are already null without an explicit fill ([JLS default initialization][S1], [`Arrays.copyOf`][S25]).
- Variables remain in their explicit canonical semantic frame slots during execution. Freeze reads those slots directly into exact value arrays; no variable recorder or entry object is maintained.
- Full-result and assignments-only views use distinct compact variable-slot projections over shared key metadata, so result-only externals are never copied into assignment memory.
- A private `CAPTURED_NULL` sentinel distinguishes a reached nullable frontier from an untouched null tail slot.
- Materialize the ordinary public result before allocating publication storage. After success, scan
  participating variable slots and the calculation tail into exact value arrays, plus `int[K]` ordinals
  only when reached points are not the dense schema prefix. Then let the complete frame become
  unreachable. Current-item slots are restored in `finally`; a failure publishes no memory.
- `CalculationMemory` owns final value arrays and shares standalone prebuilt keys. It creates a small
  stateless immutable list projection only when a list method is requested; the indexed persistence path
  creates no view. It must not hold the frame, `ExecutionScope`, or `ExecutionPlan`.

The recording helper can use frame length as the execution-mode discriminator:

```java
void recordCalculation(int absoluteSlot, Object value) {
    if (absoluteSlot < frame.length) {
        frame[absoluteSlot] = value == null ? CAPTURED_NULL : value;
    }
}
```

The shown helper is only one candidate. Benchmark slot-first (`slot` check before touching scope),
mode-first (inactive `compute()` exits before checking the slot), and an absolute-slot encoding whose
inactive sentinel makes one frame-length comparison cover both conditions. `perfasm` must prove helper
inlining and bounds-check elimination. Opaque collection descendants are a required control because they
may execute repeatedly while deliberately remaining unmarked.

This candidate is especially strong when the memory includes variables: the frame already contains
the participating effective external and assigned values. With compressed references its temporary
incremental payload is approximately `4 * S` bytes, rounded for alignment, and it avoids a recorder
object and sidecar. Freeze copies only references into exact value arrays; it does not recursively
copy values. If a later public contract requires detached collection copies, measure that policy
separately because it can dominate every recorder choice.

The risk is temporary `O(S)` allocation and scan work when very few points are reached. Current-item
restoration must be tested, and JOL/JFR must compare complete compact memories and discarded working
storage against append. A large plan with sparse reached frontiers may make append preferable.

### Keep scope alive without a transfer holder

Current production methods return raw results and discard `ExecutionScope` before the API performs
public materialization. The memory route must split that internal lifecycle while keeping scope and raw
result only in local variables: prepare extended frame, execute, publicly materialize once, freeze, and
construct the final envelope. Do not allocate a `PreparedExecution`/tuple merely to cross this seam.
For assignments, read assigned slots directly while building the public map instead of allocating the
current intermediate raw `ArrayList`.

## Recommended recorder shape

### Append control with an implicit dense prefix

Let `S` be the plan's number of marked frontiers and `K` the number reached in one execution.

The recorder conceptually holds:

```java
Object[] values;       // null until first record
int[] capturedSlots;   // null while values[0..count) map to slots with the same indices
int count;
int totalSlots;
```

On `record(slot, value)`:

- Append `value` at `values[count]`. A value may itself be null; `count`, not nullness, is the presence marker.
- If `capturedSlots == null` and `slot == count`, no slot metadata is written.
- On the first `slot != count`, allocate the sidecar, backfill the existing identity prefix (`0..count-1`), then append this and later slot IDs.
- Grow with plain arrays only when necessary. Benchmark a small initial capacity against allocating `S` directly; do not guess a universal threshold.

At freeze:

- `K == 0`: return shared empty calculation memory without allocating arrays.
- `K == values.length`: transfer ownership directly.
- Otherwise: copy exactly `K` elements. The oversized temporary becomes unreachable with the recorder.
- Transfer or trim `capturedSlots` similarly. A null sidecar means `slotAt(i) == i`.

Fresh Java arrays are already initialized to default values, so a fresh dense buffer needs no explicit `Arrays.fill` ([JLS 10.6 and 4.12.5][S1]). OpenJDK's own `ArrayList` grows with `Arrays.copyOf`, trims to size, and explicitly nulls removed references so they stop being retained ([JDK 21 `ArrayList` source][S2]).

### Required invariant

The append control relies on two properties:

- Every marked frontier executes at most once per top-level computation.
- Reached calculation slots are strictly increasing.

The agreed exclusion of calls inside opaque collection navigation removes the main repeated-execution case. The planner should assign slots in source evaluation order, not arbitrary AST or node-construction order. Add structural tests for skipped conditional branches, short-circuit operators, null coalescence, assignments followed by a result, and lazy CSE occurrences. If either property cannot be guaranteed, reject append as a candidate; frame tail remains correct without that invariant.

### Dense control design

The correct dense control is `Object[S]` plus a `long[(S + 63) >>> 6]` presence bitmap. It provides O(1) indexed writes and supports recorded nulls. It costs O(S) allocation and zeroing even when `K` is small, and compact output requires an O(S) scan plus a copy when branches leave gaps.

Do not fill the values array with a sentinel. A bitmap is smaller and separates value from presence. Do not use `boolean[]` until measured against `long[]`; the bitmap has much less retained metadata for large `S`.

### Adaptive control design

Only if measurements show a crossover, prototype sparse append that promotes to dense after a measured occupancy threshold. Promotion adds branches, copying, and two representations, so it is not the initial recommendation. Keep the threshold internal and derive it from representative JMH distributions rather than exposing configuration.

## Branch and JIT shape

The node must compute exactly once and record only the already-computed reference. Source shape alone
does not establish the cheapest branch. Compare these three shapes:

- **Slot-first:** reject unmarked opaque descendants before touching capture state.
- **Mode-first:** reject normal `compute()` before validating a calculation slot.
- **Fused absolute slot:** encode an inactive slot so one comparison against frame length covers mode and
  marking.

The winning form may differ between ordinary marked calls and unmarked calls repeated inside an opaque
collection operation, so both workloads are binding. A null-object recorder remains excluded because it
forces dispatch on normal execution without eliminating the decision.

Keep any recorder or frame reference execution-local, and keep `recordCalculation` small enough to inline. Ordinary array loads/stores are sufficient. HotSpot C2 already performs inlining, conditional profiling, global code motion, array range-check elimination, and frequency-based block layout; the JDK 21 compiler defaults enable escape analysis, allocation elimination, and frequency-based block layout ([HotSpot glossary][S3], [JDK 21 C2 flags][S4]). Manual branchless tricks should not replace this clear shape without assembly evidence.

### Escape analysis expectations

Do not budget on scalar replacement of the returned memory, views, or value arrays: they escape by definition. A small recorder wrapper may be eliminated if inlining exposes it, but its arrays and retained result cannot be. JDK 21 C2 explicitly finds non-escaping allocations and scalar-replaceable candidates, with bounded support for scalar replacement of constant-sized arrays ([JDK 21 escape analysis source][S5], [C2 flags][S4]). Deep executable-node calls and an escaping result reduce the opportunity.

Use final concrete classes and direct indexed methods. Do not add lambdas, method handles, per-entry
objects, or generic collection builders to the compute/indexed path in the hope that escape analysis will
erase them. Public entry records exist only when a caller explicitly traverses a `List` projection.
Verify rather than assume by comparing normal runs with
`-XX:-DoEscapeAnalysis` / `-XX:-EliminateAllocations`, inspecting `perfasm`, and checking normalized
allocation.

## Compact immutable public memory

### Separate values from provenance

Store execution values per result, but provenance once per compiled plan in a standalone schema.
Prebuild `VariableKey` and `CalculationKey` objects once because they are the public form consumed by
persistence and can be shared safely without retaining the plan. A primitive interleaved schema remains
a control, for example:

```text
[nodeId, offset, endOffset, line, column, nodeId, offset, ...]
```

Use a small `byte[]` only for metadata with a deliberately stable bounded code space. The primitive
control must include the cost of reconstructing public keys during every audit; it may not claim savings
by moving work outside the measured operation. Do not box slot IDs or duplicate metadata per execution.

The schema must be independently immutable and must not be a non-static inner object of the plan. `CalculationMemory -> CalculationSchema` is acceptable; `CalculationMemory -> ExecutionPlan` is not. This preserves compact provenance sharing without keeping the full node tree, bindings, constants, or providers alive.

### Columnar payload and immutable projections

The public contract exposes counts and indexed `keyAt`/`valueAt` methods alongside
`List<VariableEntry>` and `List<CalculationEntry>`. Persistence adapters should use indexed access:
it reads prebuilt keys and captured values without allocating an entry wrapper. The index is the stable
public order, never an internal frame slot.

The two lists are final immutable random-access projections over the same payload. A non-empty list
method creates one stateless projection object per call; callers using this compatibility surface should
retain that returned list for the duration of their operation. Empty lists are shared singletons. `get`
creates the requested public record, but neither `CalculationMemory` nor the list caches projections or
entries. This preserves the idiomatic API without making indexed persistence pay for views or records.
Do not materialize a list via `ArrayList`/`List.copyOf`; Java does not expose trusted-array adoption for
arbitrary public arrays ([Java 21 `List`][S7], [JDK 21 `ImmutableCollections` source][S8]). Benchmark
compute-only, indexed full consumption, and list full consumption separately so projection cost remains
visible rather than hidden.

### Value ownership and public materialization

The calculation-memory contract established for Etapa 10 does not promise detached snapshots. It should expose the canonical references that participated in execution, including registered object values, while the ordinary public result keeps its existing materialization contract. This avoids a second recursive copy of captured collections/maps for either storage candidate.

Provider and external-symbol boundaries must still perform their existing eager type, null, and size validation before values enter the frame. Runtime-produced collections are already canonical expression values. The materialized entries expose those validated references; they must document that registered object values are identities, not historical object-state snapshots.

If the public final result is also a calculation frontier, keep that reached point in `calculations()`;
the result and the point serve different public roles. Reuse the canonical value reference when public
materialization does not replace it, but do not introduce a general identity map. A future
detached-snapshot option would be a separate, explicitly measured feature because recursive copying can
dominate every recorder choice.

## Retained-memory and GC rules

- The returned memory strongly owns every captured value by design. Weak/soft references would make calculation memory nondeterministic; Java defines weakly reachable referents as reclaimable ([Java 21 `java.lang.ref`][S9]).
- Return exact-sized value arrays and optional exact ordinal arrays. Never return a view backed by an oversized recorder or execution frame.
- A failed execution should leave its fresh frame/recorder working storage unreachable. No cleanup is needed unless reuse is introduced.
- Never keep prior values in `ThreadLocal`, a shared pool, a generation-stamped array, an identity-dedup cache, or a plan field.
- Provenance schemas and public keys must not retain source text, AST/semantic models, executable nodes, environment objects, or public views.
- Heap tests must retain a `CalculationMemory`, drop the engine/compiled expression, and prove the plan can be reclaimed while values and the standalone schema remain reachable.

An object remains strongly reachable whenever a live object graph points to it ([Java 21 reference reachability][S9]). Therefore generation stamps solve logical clearing but not GC clearing: stale references in a reused `Object[]` still retain old values. OpenJDK collections explicitly null vacated array elements for this reason in practice ([S2]).

## Techniques not recommended

| Technique | Decision | Reason |
|---|---|---|
| Generation stamps / lazy clearing | Reject for fresh buffers; conditional only for primitive presence state in a proven pool | Fresh arrays are zeroed already. Stamps do not clear stale object references and add an `int[]`, generation checks, and wraparound handling. A pooled reference buffer must still clear every touched element. |
| `ThreadLocal` recorder or arena | Reject by default | Each live thread retains its copy while the thread and `ThreadLocal` are accessible ([Java 21 `ThreadLocal`][S10]). It can retain the largest buffer and last values, multiplies footprint across virtual threads, complicates reentrancy, and conflicts with the module's negative whole-scope pool result. |
| Shared object pool | Reject by default | Requires synchronization or partitioning, clearing in `finally`, bounded capacity, and reentrancy handling. The returned memory escapes, so either ownership cannot transfer or every result needs another copy. |
| Scoped values | Reject | Scoped values are a JDK 21 preview API for immutable, bounded, one-way context propagation, not mutable result collection ([JEP 446][S11]). They add hidden lookup and preview enablement while `ExecutionScope` is already explicit. |
| Virtual threads | Reject as a compute optimization | Virtual threads improve throughput for highly concurrent blocking work, not CPU-bound latency, and OpenJDK explicitly says they are not faster threads and should not be pooled ([JEP 444][S12]). They are relevant only to compatibility testing. |
| `MemorySegment` / off-heap arena | Reject | The FFM API is preview in JDK 21 and targets foreign memory/functions; its layouts store primitive/address data, not GC-tracked Java object references ([JEP 442][S13]). Captured values must remain heap references, while tiny primitive provenance does not justify lifetime checks, native allocation, copying, and preview flags. |
| `VarHandle` | Reject | The recorder is execution-local and needs plain array access. `VarHandle` adds signature-polymorphic dynamic type checks and memory-ordering modes intended for fields, arrays, or off-heap structures; atomic/acquire/release semantics provide no value here ([Java 21 `VarHandle`][S14]). |
| `ClassValue` | Reject | It lazily associates data with a Java `Class`, while capture metadata varies per plan and node instance, not per executable-node implementation class ([Java 21 `ClassValue`][S15]). |
| `MethodHandle` / `LambdaMetafactory` recorder | Reject | A direct call plus array store is simpler. `LambdaMetafactory` linkage requires a direct implementation handle and capture can allocate a function object ([Java 21 `LambdaMetafactory`][S16]); adapted/bound handles are particularly unsuitable. Method-handle invocation also retains dynamic type checks ([Java 21 `MethodHandle`][S17]). Keep these mechanisms for the module's already-proven provider/member invocation seam, not local recording. |
| Per-node generated lambdas or hidden classes | Reject | They increase linkage, class/metaspace, code-cache, and retained-plan footprint and approach a second execution representation. They belong, if ever, to the separately gated Tier 1 effort, not this optional recorder. |
| Eager entry record per reached item | Reject as default; retain as JMH control | It creates a transient object graph before a persistence adapter creates rows/bytes/entities. Compact indexed traversal avoids it; list consumers can still request projections explicitly. |
| Content-based value deduplication / interning | Reject | Hashing/equality can exceed the cost saved, BigDecimal scale is observable in this module, mutable external values are unsafe keys, and any cross-execution cache prolongs reachability. Duplicate references already share the underlying object. |
| General `IdentityHashMap` deduplication | Reject by default | Identity semantics are correct for graph-copy reuse, but the map is another table allocation and is intended for rare topology-preserving transformations ([Java 21 `IdentityHashMap`][S18]). Consider only for measured repeated deep collection materialization above a slot-count threshold. |
| Hand-packed `short` slot fields or bit fields | Reject initially | They impose plan-size limits and decoding work, while HotSpot field layout and alignment determine whether bytes are actually saved. Keep `int calculationSlot`; use JOL before considering packing. |

## Object layout proof

Adding an `int calculationSlot` affects every participating long-lived node instance, even when its value is `-1`. Do not estimate this from source field order. JOL reports actual field layout, headers, alignment gaps, reachable footprint, and reference paths for the running VM ([OpenJDK JOL][S19]).

Measure at least:

- representative small and large executable-node records before/after the slot field;
- the complete plan graph for 10, 100, and 1,000 nodes;
- empty, dense, prefix-sparse, and gapped compact `CalculationMemory` results, including list views;
- one memory from each of many plans and many memories from one plan, so standalone-schema cost and
  key sharing are visible separately;
- the target deployment heap settings, because compressed references and object alignment change the result;
- both shallow layout (`ClassLayout`) and retained graph (`GraphLayout`).

Field packing is successful only if total plan-graph bytes decrease on supported deployment configurations without adding decode cost to the hot path. Primitive arrays are the dependable compact representation for repeated metadata; manual declaration order is not a contract.

## JMH proof plan

### Benchmark matrix

Add a dedicated paired benchmark in the existing JMH package. Build plans at `@Setup(Level.Trial)` and use non-final runtime overrides so C2 cannot fold the expression; official JMH samples warn against predictable final inputs and hand-written repetition loops ([constant-fold sample][S20], [loop sample][S21]). Consume both the result and memory through `Blackhole` or return the aggregate.

Measure these dimensions:

| Dimension | Cases |
|---|---|
| API | `compute`; `computeWithMemory` return only; indexed full traversal; list full traversal; sequential no-I/O persistence sink |
| Marked slots `S` | 0, 1, 4, 16, 64, 256 |
| Reached slots `K` | 0, dense, dense prefix, one early slot, alternating gaps, one selected conditional branch |
| Value shape | nullable scalar, `BigDecimal`, string/temporal, small collection, nested collection |
| Capture storage | extended execution frame and append/lazy-sidecar; dense+bitmap remains the already-rejected historical control |
| Publication | compact columnar payload, eager entry arrays control, prebuilt keys, primitive schema reconstruction control |
| Hot-path shape | slot-first, mode-first, fused absolute slot; per-capture count versus post-execution count |
| Threads | 1 and available-CPU contention run; recorder remains execution-local |
| Plan shape | arithmetic chain, interleaved variable/current-item slots, assignments+result, short-circuit/coalesce, nested conditional, cross-assignment/result CSE, opaque collection/filter control |

Do not put many computations inside one benchmark invocation to amplify the delta; JMH's official loop sample shows that this enables unrolling, pipelining, and hoisting and produces fictional per-operation costs ([S21]). Use larger expression plans to amplify real node/recording work.

### Metrics and gates

Use the module's established protocol: multiple forks, fixed heap, same-run pairs, `-prof gc`, JSON output, and confidence intervals.

Pin the JVM explicitly before measuring. In the current workspace, direct `java -version` resolves OpenJDK 26.0.1 while `mvn -version` resolves Temurin 25.0.3, although the module targets Java 21. A paired verdict is valid only when build, baseline, candidate, and JMH forks use the same recorded JVM; run the final compatibility gate on the Java 21 deployment target as well.

- **Normal-path gate:** a marked plan executed with inactive memory storage must remain within the established same-run noise band of the pre-recording/control implementation. Investigate any reproducible >1% latency or B/op change.
- **Delta attribution:** report ordinary public-result materialization separately from capture/publication.
  `computeWithMemory()` must invoke it exactly once. Container-heavy cases remain in the matrix because
  their existing recursive copies may dominate absolute B/op, but they cannot hide the memory delta.
- **Memory-path selection:** choose a Pareto winner on capture `ns/op`, end-to-end indexed-consumption
  `ns/op`, normalized `B/op`, and retained bytes. A lower allocation variant does not win by moving
  disproportionate CPU into freeze or the normal persistence traversal.
- **Compactness gate:** compare total working allocation and retained bytes, including on-demand views,
  value arrays, optional ordinals, and shared keys/schema. Frame-tail pays temporary `O(F + S)` capacity and
  append pays growth/compaction proportional to reached `K`; neither returned memory retains working
  storage.
- **Empty gate:** no participating variables and `K == 0` returns a shared empty memory. With variables
  but no dynamic points, both arms publish only the exact variable-value payload.
- **Indexed traversal gate:** full key/value consumption allocates zero evaluator bytes per operation.
- **Structural allocation gate:** beyond the normal result and working frame, the indexed publication
  target is one envelope, one memory object, at most `Object[V]`, `Object[K]`, and a gapped-only
  `int[K]`. Keys, spans, lists, iterators, entries, lambdas, builders, and maps are absent from the
  per-execution indexed path. No temporary execution holder is permitted.
- **List traversal gate:** measure all transient records explicitly; it is compatibility cost, not the
  baseline persistence cost.
- **Persistence sink gate:** consume every key field and representative value into a sequential no-I/O
  sink. Real disk/database latency is excluded because it would hide evaluator differences.
- **Concurrency gate:** results from a shared plan remain isolated with no synchronization or shared mutable recorder state.

JMH's official profiler sample recommends `-prof gc` for normalized allocation and churn, cross-checking both, and multiple forks; it also documents `perf`, `perfnorm`, and `perfasm` for branches, loads/stores, cache misses, and generated code ([S22]). Run `perfasm` with one fork because code addresses differ across forks.

### JIT and branch checks

Run focused forks with:

- `-prof perfasm` to verify the unmarked path is the slot test and fall-through, and that `recordCalculation` inlines;
- `-prof perfnorm` to compare branches/op, branch misses/op, loads/stores, and cache misses for slot-first versus recorder-first condition order;
- `-XX:-DoEscapeAnalysis` and `-XX:-EliminateAllocations` as diagnostic comparisons, not production recommendations;
- `-XX:+PrintCompilation -XX:+PrintInlining` in a non-scoring diagnostic run to confirm compilation and inlining state.

## JFR and reachability proof plan

Use JFR after JMH identifies finalists, because JMH allocation is the quantitative gate while JFR attributes cost to stacks.

1. Run a sustained single variant with `JFR.start settings=profile` or `-XX:StartFlightRecording=settings=profile`.
2. Inspect `jdk.ObjectAllocationInNewTLAB`, `jdk.ObjectAllocationOutsideTLAB`, `jdk.ObjectAllocationSample`, execution samples, compilation/inlining, deoptimization, and GC events. These allocation events and payloads are defined in JDK 21's JFR metadata ([JDK 21 JFR metadata][S23]).
3. Confirm indexed-path allocations are the result container, exact value/ordinal arrays, and unavoidable public result materialization, not list views, entry records, key reconstruction, recorder wrappers in the frame arm, lambdas, maps, duplicate collection copies, or pooled-buffer cleanup artifacts. List benchmarks must attribute their view and record allocations separately.
4. For leak investigation only, use `jcmd ... JFR.dump path-to-gc-root=true` or a heap dump. The JDK tool guide warns that GC-root collection can pause the application and should be enabled only when investigating a suspected leak ([Java 21 `jcmd`][S24]).
5. Retain many memories from many plans, drop engines/plans, force no correctness assumption about GC timing, and inspect paths. The only expected roots are the test holder to `CalculationMemory`, exact value/ordinal arrays, captured values, and standalone keys/schemas. Views appear only in the separate fixture that explicitly retains them.
6. Separately drop memories while keeping compiled plans and verify captured value graphs disappear. This catches accidental recorder/schema storage on the plan.

Also run `jcmd GC.class_histogram` and JOL graph footprints for controlled test fixtures. `GC.class_histogram` and heap dumps are high-impact diagnostics, so keep them out of scored JMH runs ([S24]).

## Decision sequence

1. The storage prototype proved frame-tail as the current-scale winner, append as the large-sparse control, and dense+bitmap as dominated.
2. Define prebuilt public keys, compact columnar payload, indexed access, and immutable list projections in the benchmark fixture.
3. Benchmark frame-tail and append across compute-only, freeze, indexed consumption, list consumption, and a sequential no-I/O persistence sink; retain eager entries as a control.
4. Benchmark slot/mode branch order and reach counting independently from storage/publication.
5. Reopen storage or publication if it is no longer Pareto-winning in representative current-scale cases; otherwise land only frame-tail plus compact publication.
6. Prove current-item restoration, no plan back-reference, and optimized/Oracle memory equivalence.
7. Measure node-plan growth and key/schema alternatives with JOL; profile allocation stacks and generated branches with JFR/JMH profilers.
8. Repeat the complete verdict on Java 21 and consider append in production only if later telemetry finds a material population of large sparse plans.

## Primary sources

- [S1] [Java Language Specification 21, Chapter 10, arrays and default initialization](https://docs.oracle.com/javase/specs/jls/se21/html/jls-10.html)
- [S2] [OpenJDK 21 `ArrayList` source](https://github.com/openjdk/jdk/blob/jdk-21-ga/src/java.base/share/classes/java/util/ArrayList.java)
- [S3] [OpenJDK HotSpot Glossary](https://openjdk.org/groups/hotspot/docs/HotSpotGlossary.html)
- [S4] [OpenJDK 21 C2 flags (`DoEscapeAnalysis`, `EliminateAllocations`, array scalar-replacement limit, block layout)](https://github.com/openjdk/jdk/blob/jdk-21-ga/src/hotspot/share/opto/c2_globals.hpp)
- [S5] [OpenJDK 21 C2 escape-analysis implementation](https://github.com/openjdk/jdk/blob/jdk-21-ga/src/hotspot/share/opto/escape.cpp)
- [S6] [Java 21 `AbstractList` Javadoc](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/AbstractList.html)
- [S7] [Java 21 `List` Javadoc](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/List.html)
- [S8] [OpenJDK 21 immutable-collections source](https://github.com/openjdk/jdk/blob/jdk-21-ga/src/java.base/share/classes/java/util/ImmutableCollections.java)
- [S9] [Java 21 `java.lang.ref` reachability specification](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/ref/package-summary.html#reachability)
- [S10] [Java 21 `ThreadLocal` Javadoc](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/ThreadLocal.html)
- [S11] [JEP 446: Scoped Values (Preview)](https://openjdk.org/jeps/446)
- [S12] [JEP 444: Virtual Threads](https://openjdk.org/jeps/444)
- [S13] [JEP 442: Foreign Function & Memory API (Third Preview)](https://openjdk.org/jeps/442)
- [S14] [Java 21 `VarHandle` Javadoc](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/invoke/VarHandle.html)
- [S15] [Java 21 `ClassValue` Javadoc](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/ClassValue.html)
- [S16] [Java 21 `LambdaMetafactory` Javadoc](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/invoke/LambdaMetafactory.html)
- [S17] [Java 21 `MethodHandle` Javadoc](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/invoke/MethodHandle.html)
- [S18] [Java 21 `IdentityHashMap` Javadoc](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/IdentityHashMap.html)
- [S19] [OpenJDK Java Object Layout project](https://openjdk.org/projects/code-tools/jol/)
- [S20] [OpenJDK JMH sample 10: constant folding](https://github.com/openjdk/jmh/blob/master/jmh-samples/src/main/java/org/openjdk/jmh/samples/JMHSample_10_ConstantFold.java)
- [S21] [OpenJDK JMH sample 11: loops](https://github.com/openjdk/jmh/blob/master/jmh-samples/src/main/java/org/openjdk/jmh/samples/JMHSample_11_Loops.java)
- [S22] [OpenJDK JMH sample 35: profilers](https://github.com/openjdk/jmh/blob/master/jmh-samples/src/main/java/org/openjdk/jmh/samples/JMHSample_35_Profilers.java)
- [S23] [OpenJDK 21 JFR event metadata](https://github.com/openjdk/jdk/blob/jdk-21-ga/src/hotspot/share/jfr/metadata/metadata.xml)
- [S24] [Java 21 `jcmd` tool guide](https://docs.oracle.com/en/java/javase/21/docs/specs/man/jcmd.html)
- [S25] [Java 21 `Arrays.copyOf` Javadoc](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Arrays.html#copyOf(java.lang.Object%5B%5D,int))
