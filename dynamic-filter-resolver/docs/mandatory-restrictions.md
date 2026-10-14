# Mandatory statement restrictions

Applications can supply owner, tenant or other scope expressions separately from ordinary search conditions:

```java
var recent = new LogicalStatement(FilterData.of(
        "createdAt", new String[]{"since"}, LocalDateTime.class,
        GreaterOrEquals.class, new Object[]{since}));
var owner = new LogicalStatement(FilterData.of(
        "ownerId", new String[]{"ownerId"}, Long.class,
        Equals.class, new Object[]{authenticatedOwnerId}));

var statement = new ConditionalStatement(
        new StatementWrapper(recent, Map.of(), List.of()),
        ordinaryDecorator,
        List.of(owner));
var records = repository.findAll(statement);
```

Construction uses only core types. Use a `NoOpStatement` as the **ordinary** tree when there are no search conditions.
The two-argument `ConditionalStatement(wrapper, decorator)` constructor still means no mandatory restrictions.

## Execution guarantees

Complete-statement resolution runs in this order:

1. Generate the ordinary filter.
2. Run ordinary decorators in their established order.
3. Resolve every mandatory expression **without** those decorators and intersect all of them with the decorated filter by AND.

Top-level restrictions accumulate; each expression retains its own grouping and negation. Replacing the ordinary filter or
introducing OR through a decorator cannot discard the final restrictions. Restrictions are not added to the ordinary tree,
decorated-filter map or request metadata, and do not create sorting aliases.

The existing JPA resolver applies this phase through all repository entrypoints accepting a complete statement: listings,
single results, sorting, paging (content and totals), entity graphs, `count`, `exists`, `convertoToSpecification` and `findBy`.
No new repository overloads or JPA-specific construction utility are needed.

## Absence, extensions and values

- Direct model construction rejects null restriction lists/elements and native `NoOpStatement` nodes at any depth, including
  AND, OR and negation. Native operands, operators and logical filter data must be present. Invalid mandatory structure raises
  `DynamicFilterConfigurationException`; it is not simplified away.
- JPA rejects absent executable restriction objects and absent mandatory predicates with the same configuration-error contract.
  Every evaluated native constituent is guarded, so a valid sibling cannot hide an absent condition.
- Custom `AbstractStatement` subclasses remain supported. The library validates observable executable results and known native
  structure, not opaque extension internals. Custom implementations must honor their effective-condition contract.
- Null values and empty containers retain each operation's existing meanings; they are not globally forbidden. A non-null true
  predicate is valid, including empty `ContainsAll`. The library does not detect tautologies or prove that a predicate narrows results.
- A decorator returning a null **filter object** raises `StatementGenerationException`, consistently for direct and composite JPA
  decorators. This differs from a legitimate ordinary null **predicate**, ordinary no-op filtering or fetching-only decoration,
  all of which remain supported before the mandatory phase.

## Scope is supplied by the application

The application obtains identity values and chooses its access policy. This library does not read a security context, authorize
users, validate business credentials or protect arbitrary query paths. Supplied restrictions are retained and applied, but a
valid predicate is not proof of a correct access policy. Custom extension code is not sandboxed.

Do not destructure a restricted statement and call only `createFilter(wrapper, decorator)`: that low-level entrypoint does not
carry restrictions and is **not** a substitute for complete-statement resolution. Other repository/query paths without the
complete restricted statement do not become an authorization barrier automatically.

## Ownership and sharing

The restriction-list container is copied and unmodifiable. External structural edits to the input list cannot change a completed
statement. Existing expression trees, `StatementWrapper`, `FilterData`, arrays, metadata, application values and collaborators
are shared, not deeply cloned.

Callers must not mutate shared trees, arrays, metadata containers, mutable values or stateful collaborators while derived
statements are in use, including between page-content and count execution. Request identity must not be stored in shared
decorator singletons; reusable decorators retain their stateless/thread-safe contract. This is not deep immutability or a
guarantee against callers mutating shared objects.

## Custom resolver migration

`DynamicFilterResolver.createFilter(ConditionalStatement)` retains the old wrapper/decorator path for unrestricted statements.
For restricted statements it calls the new generic hook after ordinary resolution:

```java
<R extends T> R applyRestrictions(R decoratedFilter, List<AbstractStatement> restrictions)
```

The default hook raises `DynamicFilterConfigurationException` when restrictions exist. A backend must implement intersection,
preserve grouping/negation, resolve restrictions without ordinary decorators and explicitly reject absent effective conditions.
It must not silently drop restrictions or replace absent mandatory results with unrestricted conditions.

**Resolvers overriding complete-statement resolution themselves must migrate too:** delegate to the interface default when
appropriate, implement the final phase explicitly, or reject restricted statements. An override that simply extracts the old
two components bypasses the default hook and is no longer conforming for restricted input.

## Public record compatibility

`ConditionalStatement` now has a third record component, `List<AbstractStatement> restrictions`. Existing accessor names and
meanings and the two-argument constructor remain available. This does **not** promise universal source/binary/schema compatibility:

- Record patterns must account for the third component.
- Reflection on the canonical constructor or record components sees the new shape.
- Record-aware serialization/deserialization and generated schemas may require migration.
- Generated `equals`, `hashCode` and `toString` now include restrictions.

Consumers relying on those features should review their integration before upgrading. The wildcard decorator contract is unchanged.
