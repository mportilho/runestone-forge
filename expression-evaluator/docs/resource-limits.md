# Resource limits and trust modes

`ExpressionEnvironment` defaults to `ExpressionTrustMode.TRUSTED`. Configure the resource policy
through one immutable value:

```java
ExpressionResourceLimits limits = ExpressionResourceLimits.builder()
        .maxCurrentItemDepth(32)
        .maxMaterializedSize(10_000)
        .maxFactorialInput(1_000)
        .maxTraversalSteps(1_000_000)
        .build();
ExpressionEnvironment environment = ExpressionEnvironment.builder()
        .trustMode(ExpressionTrustMode.SAFE)
        .resourceLimits(limits)
        .build();
```

`environment.resourceLimits()` returns that snapshot; `environment.trustMode()` returns the selected
mode. The former direct limit methods on the environment and its builder have been removed.
Builders are mutable; built environments and resource-limit values are immutable. Reusing a builder
does not change an earlier snapshot.

| Mode | Compilation resource checks | Runtime resource checks |
|---|---|---|
| `UNSAFE` | No | No |
| `TRUSTED` (default) | Yes | No |
| `SAFE` | Yes | Yes |

The migration in #167 applies this policy to current-item nesting, container materialization and
factorial inputs. Defaults are compilation inputs, whereas overrides, Java provider results and
public results use the runtime policy. Folding uses compilation limits: an over-limit folding
attempt remains executable, with the runtime policy selected by the environment. In `TRUSTED` and
`SAFE`, folding does not invoke functions or collection operations whose work depends on input. All modes retain
typing, non-null boundaries, immutable public snapshots, numeric-domain checks, bounded engine
caching and parser-context cleanup.

In `TRUSTED` and `SAFE`, function calls and collection operations with input-dependent work remain
unfolded; this avoids a compilation work meter and prevents providers or large traversals from running
during folding. `UNSAFE` retains unrestricted folding. Scalar operators whose work is bounded by source,
AST and value-shape limits remain foldable.

All thirteen properties from the [stage 12 limits table](planning/etapa-12/etapa-12-endurecimento-verificacao.md#defaults-e-tetos-iniciais)
are available through `defaults()`, `builder()` and property getters. Mutators reject values outside
the inclusive range from zero to the documented absolute ceiling, reporting the property, supplied
value and accepted range. This validation applies in every trust mode. Zero allows zero capacity;
there is no public unlimited sentinel.

`maxTraversalSteps` is enforced only at runtime in `SAFE`. Each evaluator-controlled collection item
or map entry reached by iteration, recursive conversion, validation, or materialization consumes one
step from the execution-local allowance; nested traversals and lambdas share it, while lazy operations
consume only visits they actually reach. Zero therefore rejects the first such visit. Every execution,
including a reentrant execution started by a provider, receives a fresh allowance.

Traversal steps deliberately do **not** estimate CPU time, elapsed time, invocation count, algorithmic
complexity, provider work, or financial cost. Built-ins and providers are not charged merely for being
called, custom-provider internals remain trusted, and expensive isolated operations use their local
shape limits instead. `TRUSTED` and `UNSAFE` have no runtime traversal counter.

The corpus environment adapter builds the same aggregate from its existing limit fields. Historical
cases with an environment retain `SAFE` behavior; `trustMode` selects another policy explicitly.
