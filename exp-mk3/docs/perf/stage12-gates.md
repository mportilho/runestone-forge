# Etapa 12 Local Performance Gates

This document defines the reproducible local performance boundary for Etapa 12. The machine-readable
manifest consumed by the runner is [`stage12-gates.json`](stage12-gates.json); this page explains its
classification and operation. Raw measurements are generated under `exp-mk3/target/stage12/` and are
not committed automatically.

The pre-hardening code frontier for diagnostic emitters, official built-ins, variable-work loops, and
expanding allocators is frozen in
[`stage12-implementation-inventory.md`](stage12-implementation-inventory.md).

## Protocol

- Eclipse Temurin JDK 21, using the same Java home reported by Maven for compilation and every fork.
- Fixed `-Xms1g -Xmx1g`, one thread, three forks.
- Five 500 ms warm-up iterations and ten 500 ms measurement iterations.
- The cache family uses twelve warm-up iterations because compilation and Caffeine admission were still
  visibly tier-compiling after the fifth iteration; measurement remains ten 500 ms iterations.
- JMH average time plus the GC profiler; JSON is the machine-readable result.
- Dedicated traversal-allocation methods disable C2 allocation elimination. This keeps source-level
  execution objects observable and prevents fork-dependent scalar replacement from producing false
  B/op regressions; latency methods retain the normal optimized JVM.
- Traversal overhead is measured against commit `17ccc49bfdc2ac5c11c0aa2db610f38d1154bb7a`, the
  last implementation before traversal debits. The runner exports that revision to an isolated temporary
  directory, overlays only the current benchmark fixture, and runs the same protocol on the same
  machine. It does not check out or modify the working tree.
- JOL records production plan, scalar `ExecutionScope`, and Calculation Memory layouts.
- JFR records the scalar arithmetic benchmark in a controlled JMH fork for attribution evidence; it
  is not an automatic pass/fail gate.

The runner rejects a Maven JDK other than 21. Machine, operating system, JDK, Maven, commit, dirty-tree
indicator, protocol, commands, logs, JSON, JOL, JFR, and an artifact inventory are retained together.
`verdict.json` and `verdict.txt` contain the automatic result for every encoded gate. A point estimate
outside a latency threshold whose confidence bands still overlap is `INCONCLUSIVE` and fails the run so
the affected measurement is repeated rather than accepted or rejected from noise. The original verdict,
raw repeat, reconciled evaluator inputs, and final verdict are retained together when this occurs.

## Manifest

| Family | Classification | Current coverage |
|---|---|---|
| Scalar | Binding | Arithmetic, logic, and registered function invocation |
| Collections | Binding | `map`, composed map/sum, short circuit, sorting, reduction, wildcard, filter, nested lambda |
| Compilation | Binding | Warm parsing and full uncached compilation |
| Cache | Binding | Uncached pipeline, miss, pure hit, and hit plus math view |
| Regex | Binding | RE2/J literal operator, dynamic replacement/split, and adversarial non-match growth |
| Calculation Memory | Binding | Dense-shape normal compute, capture/freeze, indexed persistence, indexed and list traversal |

The scalar and collection families also include `Stage12TraversalBenchmark` in all trust modes. Scalar
and `UNSAFE`/`TRUSTED` collection results are paired with the pre-debit baseline. Allocation comparisons
use the dedicated methods with allocation elimination disabled. `SAFE` collection
allocation uses 8- and 128-item inputs: a constant scope cost is allowed, while growth in additional
bytes per reached item is not.

“Binding” means later Etapa 12 work must preserve the metrics, comparisons, and thresholds encoded in
the JSON manifest. “Characterization” records behavior without making its latency a compatibility
threshold. Regex binds absence of a backtracking fallback and approximately linear adversarial growth;
its latency and allocation on small benign patterns remain characterization data.

## Commands

Run the complete baseline from the repository root:

```bash
exp-mk3/scripts/run-stage12-gates.sh
```

To exercise only the Maven/JMH infrastructure smoke gate:

```bash
mvn -pl exp-mk3 -Pjmh-infrastructure-gate verify
```

The smoke profile executes a benchmark with GC profiling and writes
`exp-mk3/target/stage12/infrastructure-gate.json`; it no longer merely lists discovered benchmarks.

The runner executes the `stage12-stress` Maven profile after the everyday suite. The runner has no Git
versioning, tagging, publishing, or remote-writing command.

## Reading results

The initial baseline establishes the local reproducible frontier; it does not compare measurements
from different days as a verdict. The final runner uses paired controls in one execution where available
and an alternating baseline/candidate run for traversal overhead. The GC profiler decides allocation
gates. JFR explains CPU/allocation stacks and JOL verifies layout; neither substitutes for JMH JSON.
