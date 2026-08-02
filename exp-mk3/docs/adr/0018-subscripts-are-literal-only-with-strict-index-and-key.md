# ADR 0018: Subscripts Are Literal-Only With Strict Index and Key

## Status

Accepted

## Context

The frozen grammar admits only two subscript payloads that carry a value: a quoted `STRING` for map keys and a `signedInteger` for indices and slice bounds. There is no production for `x[i]`, `x[n + 1]`, or `x[someKey]`. Every subscript payload is therefore known during AST construction, and the semantic resolver can classify subscript bounds against a statically known collection shape whenever one exists.

Until now this was an unstated consequence of the grammar rather than a recorded language contract. The closing phase of the language surface is the moment to decide whether dynamic subscripts belong to v2, and to fix the out-of-range behavior of each subscript form, because the runtime already behaves one way and later optimization phases must preserve it as an equivalence oracle.

Index, slice, and map-key access answer different questions. An index or a textual key names one element that the caller asserts exists. A slice names a range and is normally used to take a bounded window of an arbitrary-length collection, where requiring the caller to clamp the bounds first would be both verbose and impossible to write without dynamic arithmetic in the subscript.

A caller who is not in a position to assert that an element exists needs a way to say so. Safe navigation was previously read as covering only a null receiver, but under the language's other contracts no ordinary value is ever null: external symbols require non-null defaults and overrides, assignments reject possible nulls, and registered members declare non-null results. Read that way, safe navigation is inert syntax that marks a result possibly null without any path that produces null. The subscript family is where a language-level notion of legitimate absence is actually needed, so safe navigation is what expresses it.

## Decision

Subscript payloads are literal-only in v2. Dynamic index and dynamic key subscripts are not part of the language, are not emulated by any collection operation, and are not a reason to change the grammar. The parse error is the contract.

Strictness is a property of the link, not of the subscript form. A link written without safe navigation asserts that what it names exists; a link written with safe navigation tolerates its absence and yields a possibly null result that the surrounding expression must discharge.

Index access on a strict link is strict. A negative index is normalized from the end of the collection as `index + size`. A normalized index outside `[0, size)` is a failure, never a clamped element. When the receiver's collection shape is statically fixed, the violation is a compilation diagnostic; otherwise semantic resolution emits a typed deferred check and the failure is a runtime diagnostic.

Textual map-key access on a strict link is strict. A key absent from the map is a failure.

The same two forms on a safe link yield null instead of failing: an out-of-range index and an absent key are legitimate absence, so `values?.[7]` and `attributes?.["missing"]` produce null, and a statically out-of-range index on a safe link is not a compilation diagnostic. Because the result is possibly null, it must be discharged by null coalescence or by a following safe link before it reaches any context that requires a value; `attributes?.["missing"] ?? 0` is the idiom for an optional entry.

Slice access is tolerant on both link forms. Each bound is normalized from the end when negative and then clamped into `[0, size]`; an omitted bound means the start or the end of the collection. A normalized end below the normalized start yields an empty collection, as does a start at or beyond the size. A slice therefore never fails because of its bounds; it fails only when the materialized result exceeds the environment's materialization limit.

Safe navigation tolerates absence and a null receiver for its own link, and nothing else. It does not mask a receiver whose type cannot be subscripted, a failing accessor, a failing filter predicate, an exceeded materialization limit, or any failure of a following link. It also does not propagate along the chain: each following link must declare itself safe or receive an already discharged value.

## Consequences

The language accepts a smaller surface than its host platforms, and callers who need element selection driven by a computed value must express it with a filter or a collection operation instead of a subscript. In exchange, every subscript payload is a compile-time constant, static bounds checking is possible whenever a collection shape is known, and no subscript needs a value-carrying execution node for its payload.

Index and key failures need stable runtime diagnostic codes with a source span, while slice needs none, so the diagnostic surface of the subscript family stays small. Later optimization phases must preserve strict index, strict key, tolerant safe forms, and clamped slice exactly, including the empty results produced by inverted or out-of-range slice bounds; a rewrite that turns a failing strict index into a clamped access, that turns an absent key into a null on a strict link, or that turns a safe absence into a failure, is not equivalence-preserving.

Safe navigation becomes a load-bearing part of the language rather than inert syntax, and the possibly-null metadata that semantic resolution already produces for safe links acquires a real runtime counterpart. The strict forms remain the default, so an expression that does not mention `?.` still fails loudly on absent data.

Introducing dynamic subscripts after general availability would be a grammar change with a new binding form, new deferred checks, and a new decision about whether the strict forms stay strict; recording the exclusion now makes that cost explicit rather than accidental.
