# ADR 0020: Reflection-Free Invocation Is Generated at Environment Build Time

## Status

Accepted

## Context

Calling a registered function or a registered Java member is the most expensive per-call mechanism in the executed plan. Reflection proper is already confined to environment construction: methods are unreflected into `MethodHandle`s while catalogs are assembled, and nothing looks a member up by name at runtime. What the runtime pays is the slow entry points that remain — arguments are collected into a freshly allocated array on every call and passed through `invokeWithArguments`, which spreads that array and adapts each argument's type, and every argument and result additionally crosses a boundary-coercion filter implemented as a virtual call.

Two routes remove that cost, and they differ in where the cost is paid rather than in what they compute.

One route keeps `MethodHandle` and calls `invokeExact` against a handle whose type is fixed in advance, which eliminates the array and the per-call adaptation but requires a distinct exact signature per shape and produces no new classes.

The other route asks `LambdaMetafactory` to spin a small implementation class that calls the target directly through a dedicated functional interface. That is the faster of the two once warm, and it is how the JDK itself implements lambda linkage. It also has a cost that `invokeExact` does not have: each generated entry point is a class, held for the lifetime of the classloader that defined it, and consuming metaspace.

Where that class generation happens therefore decides whether it is an amortized setup cost or an unbounded leak. The module's environment is already defined as an immutable configuration whose identity is instance-scoped, and the intended usage is that an application builds its environments once and reuses them, sharing compiled plans while the same instance lives. If that holds, generation at environment build time is paid once and repaid by every execution. If an application instead builds an environment per request or per tenant, the same design produces a new set of classes per environment and grows metaspace without bound.

Function invocation and member access do not exhaust the surface the strategy assigned to this phase, but two of its named items turn out to have no target in this module. Provider methods declared as varargs are rejected during import and varargs candidates are dropped by the Java type catalog, so there is no call site at which a varargs array could be preallocated. Public field navigation is outside v2, so every registered member is a method and there is nothing for a `VarHandle` to bind to.

## Decision

Reflection-free invocation is implemented by entry points generated once during `ExpressionEnvironment` construction, one per function descriptor and one per registered member, shared by every plan compiled against that environment. No entry point is generated per plan call site.

Arities one through four are linked through `LambdaMetafactory` against dedicated functional interfaces. Remaining arities use `invokeExact` against a handle pre-adapted at the same point in the lifecycle. Both routes are reached through the same binding recorded in the plan, so a node does not know which one it uses.

A long-lived environment is a stated precondition of this design, not an assumption left implicit. An application that rebuilds environments frequently pays class generation repeatedly and should expect metaspace growth proportional to the number of environments it constructs.

Boundary coercion filters are elided only where the resolved type of an argument is exactly the canonical type of the parameter, which makes the conversion a proven identity. Every other case keeps its filter. Eliding a conversion filter never elides a provider non-null result validation, which is a boundary contract rather than a conversion.

Safe navigation stays a null check performed before invocation. It is not implemented by catching an exception from the invocation, and no reflective fallback exists for an unknown type or member — those remain semantic errors, per ADR 0010.

The alternative of using `invokeExact` at every arity, with no class generation at all, is recorded as rejected for the long-lived-environment case and remains the correct design if that precondition ever stops holding.

## Consequences

Invocation performance becomes a property of the environment rather than of the compiled expression. Two plans compiled against the same environment share the same entry points; two environments built from identical configuration do not, which is consistent with instance-scoped environment identity but means that measuring invocation cost requires holding the environment fixed.

The precondition is now something callers can violate silently. Nothing fails when an application builds an environment per request; it simply pays setup cost per request and accumulates classes. That is the cost of choosing the faster route, and it is why the precondition is written here rather than left in a benchmark note.

Because generation happens during environment construction, a malformed registration surfaces while the environment is being built, alongside the other setup-time validations, rather than on first execution. Environment construction becomes correspondingly more expensive, which is acceptable precisely because it happens once.

Boundary coercion elision makes the runtime's argument path depend on a semantic fact that must be present and correct. As with every other transformation in this module, missing metadata costs performance and never correctness: an argument whose type is not proven exactly canonical keeps its filter.

Preallocated varargs arrays and `VarHandle`-based accessors leave the plan for this phase entirely. Reintroducing either would require first reintroducing what they act on — varargs provider methods, or public field navigation — each of which is a language decision rather than a performance decision.
