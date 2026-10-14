# Fluent conditional statement construction

`ConditionalStatement.builder()` starts empty; `ConditionalStatement.builder(existing)` preserves the source's ordinary
expression, declared metadata, decorated data, decorators and every mandatory restriction. Both return the public final core
`ConditionalStatementBuilder`. `build()` returns a new `ConditionalStatement` accepted by the existing repository methods.

```java
import com.runestone.dynafilter.core.generator.ConditionalStatement;
import com.runestone.dynafilter.core.model.FilterData;
import com.runestone.dynafilter.core.operation.types.Equals;

// The application obtains the trusted identity; the library does not read authentication state.
FilterData owner = FilterData.of("ownerId", new String[]{"applicationOwner"}, Long.class,
        Equals.class, new Object[]{authenticatedOwnerId});

ConditionalStatement scoped = ConditionalStatement.builder(requestStatement)
        .restrict(owner)
        .and(applicationFilter)
        .decorate(ordinaryDecorator)
        .build();

var records = repository.findAll(scoped);
```

Construction has no JPA or security dependency. Existing `FilterData` constructors and `of` work; the
[single-slot factories](programmatic-filters.md) are optional conveniences, not a prerequisite.

## Ordinary grouping versus mandatory scope

- `and(FilterData)` / `or(FilterData)` honor filter-level negation. The `AbstractStatement` overloads retain supplied grouped
  or custom expressions as operands. Source expressions are also kept grouped, not flattened.
- Ordinary chains associate **left to right**, without SQL precedence: `and(a).or(b).and(c)` means `(a OR b) AND c`.
  To obtain `a OR (b AND c)`, pass a grouped `CompoundStatement(b, c, CONJUNCTION)` to `or`.
- In an empty ordinary chain, the first AND or OR establishes the operand. An entirely empty `build()` is valid and unrestricted.
  Native ordinary no-op expressions retain omission semantics, not a true predicate that would swallow the first OR.
- `restrict(FilterData)` and `restrict(AbstractStatement)` accumulate mandatory expressions by AND in a separate list.
  They are applied **after** ordinary resolution and all decorators, regardless of where `restrict` occurs in the fluent chain.
  Each mandatory expression can contain grouping, OR or negation; no restriction-removal or replacement operation is provided.
- Rebuilding an already restricted statement retains every existing restriction. A later ordinary OR or replacement decorator
  cannot demote or discard it. An empty ordinary chain can still carry mandatory scope.

Native no-op descendants and missing native operands, operators or filter data are rejected immediately when supplied to
`restrict`, using the same `DynamicFilterConfigurationException` invariants as direct model construction. Custom statements
retain the existing extension contract. See [mandatory restrictions](mandatory-restrictions.md) for final execution, absence
validation, supported repository entrypoints and the limits of application-supplied scope.

For an existing Spring Data JPA `Specification<T>`, use the JPA-only
[`JpaConditionalStatements.restrict(original, specification)` helper](mandatory-restrictions.md#existing-jpa-specifications).
It appends an opaque mandatory restriction through this builder, without adding Spring Data dependencies to the core.

## Decorators and decorated data

`decorate(FilterDecorator<?>)` appends an ordinary decorator. The source decorator runs first, followed by additions in order.
The wildcard API **does not prove compile-time compatibility** between arbitrary decorator types: consumers must supply
decorators matching the resolver's runtime filter type. Restrictions do not pass through this chain. Null executable decorator
results retain the established `StatementGenerationException` contract; fetching-only ordinary predicates remain valid.

`decoratedFilter(FilterData)` exposes the same filter data under **each supplied path** for lookup through
`StatementWrapper.findDecoratedFilterByPath`. It does not automatically generate an ordinary condition. Path collisions with
source data, earlier additions or repeated paths in the supplied data raise `IllegalArgumentException`; no entries are overwritten
and a rejected addition leaves the builder unchanged. Data can be added before or after its decorator because lookup happens
when the built statement is resolved.

Existing `allFilters` request metadata and declared sorting aliases are preserved. Ordinary programmatic filters, mandatory
restrictions and additional decorated data are **not** automatically registered as public parameters or new sorting aliases.

## Null arguments and ownership

Null source statements, expression arguments, filter arguments and decorators raise `NullPointerException` immediately.
They do not mean optional omission. A null **value slot inside a non-null `FilterData`** remains permitted and retains the
operation's existing meaning; the library does not infer whether a value represents a valid application identity.

The builder is **mutable, reusable and not thread-safe**. Each build creates a new complete statement. Later builder operations
cannot structurally change earlier ordinary conditions, restriction lists, decorator composition or decorated-data containers.
Different builders can derive independent compositions from the same source without mutating it.

Existing trees, filters, arrays, application values, collaborators and unchanged metadata containers are shared. Only modified
containers are isolated: the builder owns its additions, and completed restriction lists, composed decorator lists and changed
decorated-data maps are stabilized independently of further builder use. There are no deep snapshots or persistent/copy-on-write
structures, and no guarantee of deep immutability.

Consumers must not mutate shared statements, `FilterData` arrays, metadata containers, mutable values or stateful collaborators
while derived statements are in use, including between page-content and count queries. Obtain identity per request and do not
store it in shared decorator singletons; reusable decorators retain their stateless/thread-safe contract.
