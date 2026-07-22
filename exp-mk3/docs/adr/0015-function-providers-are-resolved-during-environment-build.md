# ADR 0015: Function Providers Are Resolved During Environment Build

## Status

Accepted; vector/collection mapping clauses partially superseded by ADR 0016

## Context

ADR 0006 established explicit reflected function providers, mandatory purity declarations, directly declared public methods, and setup-time handle adaptation. Importing descriptors independently of an `Ambiente de Expressao`, however, cannot validate registered domain types or the configured boundary-coercion profile, and makes convenient provider registration diverge from the environment snapshot identified and cached as one instance.

## Decision

An `ExpressionEnvironment.Builder` accepts explicitly supplied static providers, provider instances, and instance exposure types. Import-all is the convenient default; selection by name or exact Java signature, exact-method renaming, and replacement of previously registered custom functions are explicit alternatives. Every import declares one `FunctionPurity`; providers requiring different purity contracts use separate selective imports. Built-in functions cannot be replaced.

Provider imports remain unresolved declarations until `build()`. The build first establishes registered Java types and boundary coercion, then atomically discovers eligible directly declared public methods, infers their language types, proves required inbound and outbound adaptations, prepares invocation handles, and validates names and collisions. Configuration order is irrelevant, invalid providers fail the whole build, and all discovered configuration problems are reported deterministically rather than ignored. No classpath scanning, annotation discovery, dependency-injection lookup, module opening, or runtime method lookup is performed.

Function signatures may use canonical scalar Java types, numeric types supported bidirectionally by the configured coercion profile, exact nominal registered Java types, arrays as vectors, concrete generic containers, and recursively known generic elements. Lists are vectors, collections and iterables are collections, and text-keyed maps are maps. Raw, wildcard, unresolved generic, ambiguous container/object, varargs, asynchronous, optional, stream, void, nullable, or otherwise unadaptable contracts are rejected. Boundary coercion adapts the already selected Java invocation but never creates or disambiguates overloads; nominal object compatibility remains exact.

Provider calls receive non-null canonical values and must return complete non-null values. Results are normalized immediately to canonical immutable expression values, checked recursively for nulls, and bounded by the environment materialization limit. Declared checked exceptions are allowed; provider failures are exposed as expression-execution failures with function and source context while preserving their causes, except fatal JVM errors, which propagate. A folding failure for constant arguments is a compilation diagnostic rather than a silent fallback to runtime.

## Consequences

A built environment strongly retains each supplied provider instance, and every plan cached under that environment's instance identifier invokes that same instance. Provider instances and conversion profiles must remain valid, concurrency-safe, and behaviorally stable for the environment lifetime; purity and foldability are caller promises that the engine does not verify by inspecting or copying state. Reflection, compatibility discovery, and handle adaptation stay off the execution path, while failures caused by invalid Java boundaries remain distinguishable from expression type resolution and source diagnostics. This ADR extends ADR 0006 and relies on the instance-scoped cache boundary established by ADR 0014.
