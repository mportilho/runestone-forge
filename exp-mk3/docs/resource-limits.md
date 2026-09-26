# Resource limits and trust modes

`ExpressionEnvironment` defaults to `ExpressionTrustMode.TRUSTED`. Configure the resource policy
through one immutable value:

```java
ExpressionResourceLimits limits = ExpressionResourceLimits.builder()
        .maxCurrentItemDepth(32)
        .maxMaterializedSize(10_000)
        .maxFactorialInput(1_000)
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
attempt remains executable, with the runtime policy selected by the environment. All modes retain
typing, non-null boundaries, immutable public snapshots, numeric-domain checks, bounded engine
caching and parser-context cleanup.

In `TRUSTED`, container-returning functions and Java properties/methods remain unfolded: their runtime adapters are
unbounded, so invoking them to attempt a fold could consume an entire provider iterable before
checking its size. `SAFE` retains bounded folding of these calls; `UNSAFE` retains unrestricted
folding. Scalar function folding remains available in all modes.

All thirteen properties from the [stage 12 limits table](planning/etapa-12/etapa-12-endurecimento-verificacao.md#defaults-e-tetos-iniciais)
are available through `defaults()`, `builder()` and property getters. Mutators reject values outside
the inclusive range from zero to the documented absolute ceiling, reporting the property, supplied
value and accepted range. This validation applies in every trust mode. Zero allows zero capacity;
there is no public unlimited sentinel.

The remaining enforcement increments are tracked separately: source/token/syntax/AST budgets in
#168, value shape and expansions in #169, and evaluation/folding work in #170. Their configuration
properties are validated by this contract; their enforcement belongs to those increments.

The corpus environment adapter builds the same aggregate from its existing limit fields. Historical
cases with an environment retain `SAFE` behavior; `trustMode` selects another policy explicitly.
