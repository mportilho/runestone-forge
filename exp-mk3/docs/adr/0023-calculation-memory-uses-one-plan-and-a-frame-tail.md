# ADR 0023: Calculation Memory Uses One Plan and a Frame Tail

## Status

Accepted; compact publication refined on 2026-08-18

## Context

Expression explanation was originally planned as a lazily built instrumented plan whose decorators emitted events into a bounded ring buffer. The actual dominant use is a deterministic mini audit of participating variables and reached function-like calculation frontiers, normally consumed once by a persistence adapter. A prototype compared three execution-local storage models: an extended execution-frame tail, append-only storage with a lazy slot sidecar, and dense values with a presence bitmap. The frame tail won every measured current-scale case through 64 static points and the 256-point dense case, while append-only won only large sparse cases; dense plus bitmap was always dominated. Eager entry records would nevertheless add a second transient object graph before serialization or persistence creates its own representation.

## Decision

`compute()` and `computeWithMemory()` share one immutable cached execution plan. Markable executable nodes carry one immutable primitive calculation slot; its inactive encoding and branch order are selected by generated-code and JMH evidence. Normal execution keeps the exact-size frame. Memory execution extends that frame with calculation slots and, after successful public result materialization, freezes only exact columnar value arrays plus an ordinal sidecar when reachability has gaps. Keys and source spans are prebuilt in a standalone schema shared by memories without retaining the plan. `CalculationMemory` offers allocation-free indexed key/value traversal for persistence and immutable `List` projections for convenience; entry records are not created or cached during computation. There is no instrumented plan, decorator tree, event stream, ring buffer, snapshot, or capture inside opaque collection operations. Append-only remains a benchmarked fallback for future evidence of large sparse production plans, not a second production strategy.

## Consequences

Normal execution gains at most one predictable test in markable nodes and must retain zero additional B/op; any reproducible latency regression above 1% is investigated without introducing a second plan. Returned memories own exact execution-value arrays and standalone provenance only, never working frames, list views, the plan graph, or the environment. Consumers that use the indexed API add no evaluator allocation per entry or view; consumers that request a `List` pay for its stateless projection and transient entry records at that boundary. No persistence sink/visitor is part of the evaluator API: indexed access avoids callback dispatch and leaves resource, transaction, and checked-exception ownership with the adapter. Folding transfers provenance to the replacement constant, memoization records each reached source occurrence from its memoized node, and optimized plans must produce the same memory as the internal unoptimized oracle. A final JMH gate must compare capture, freeze, allocation-free sequential consumption, list consumption, branch order, and reach-count strategies, and may reopen either storage or publication before production code. The production verdict must be repeated on the Java 21 deployment JVM.
