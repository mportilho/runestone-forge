# ADR 0023: Calculation Memory Uses One Plan and Append-only Capture

## Status

Accepted; storage reconciled on 2026-08-26

## Context

Expression explanation was originally planned as a lazily built instrumented plan whose decorators emitted events into a bounded ring buffer. The actual dominant use is a deterministic mini audit of participating variables and reached function-like calculation frontiers, normally consumed once by a persistence adapter. Prototypes compared an extended execution-frame tail, append-only storage with a lazy ordinal sidecar, and dense values with a presence bitmap. The binding payload gate reopened frame-tail because neither remaining strategy dominated. The corrected Java 21 reconciliation gate derived current scale from the corpus: append-only won latency and allocation for dominant `S=1` and dense `S=2`, while gapped shapes remained Pareto tradeoffs with lower frame-tail working allocation. Retained payloads were identical; dense plus bitmap remained dominated.

## Decision

`compute()` and `computeWithMemory()` share one immutable cached execution plan. Markable executable nodes carry one immutable primitive calculation ordinal. Normal execution keeps the exact-size frame and has no recorder. Memory execution activates one execution-local append-only recorder when the plan has calculation points. Values are appended in public order; the dense prefix needs no ordinal storage, while the first gap creates a lazy sidecar. Count distinguishes reached null from absence. After successful public result materialization, exact columnar arrays are transferred or trimmed and temporary recorder storage is discarded. Keys and source spans are prebuilt in a standalone schema shared by memories without retaining the plan. `CalculationMemory` offers allocation-free indexed key/value traversal for persistence and immutable `List` projections for convenience; entry records are not created or cached during computation. There is no instrumented plan, decorator tree, event stream, ring buffer, snapshot, adaptive storage, or capture inside opaque collection operations. Frame-tail remains a benchmark control, not a second production strategy.

## Consequences

Normal execution gains at most one predictable mode-first test in markable nodes and must retain zero additional B/op; any reproducible latency regression above 1% is investigated without introducing a second plan. Returned memories own exact execution-value arrays and standalone provenance only, never working recorder arrays, frames, list views, the plan graph, or the environment. Consumers that use the indexed API add no evaluator allocation per entry or view; consumers that request a `List` pay for its stateless projection and transient entry records at that boundary. No persistence sink/visitor is part of the evaluator API: indexed access avoids callback dispatch and leaves resource, transaction, and checked-exception ownership with the adapter. Folding transfers provenance to the replacement constant, memoization records each reached source occurrence from its memoized node, and optimized plans must produce the same memory as the internal unoptimized oracle. The Java 21 reconciliation and its decision rule are recorded in `docs/planning/etapa-10/calculation-capture-storage-reconciliation.md`; final production integration still runs paired JMH and allocation gates.
