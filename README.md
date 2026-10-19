# runestone-forge

Java development toolkit

## Dynamic Filter Resolver

- [Programmatic single-field filters](dynamic-filter-resolver/docs/programmatic-filters.md)
- [JPA filter operations](dynamic-filter-resolver/docs/jpa-filter-operations.md)
- [Filter value transformers](dynamic-filter-resolver/docs/filter-value-transformers.md)
- [Mandatory statement restrictions](dynamic-filter-resolver/docs/mandatory-restrictions.md)
- [Existing JPA Specifications as mandatory scope](dynamic-filter-resolver/docs/mandatory-restrictions.md#existing-jpa-specifications)
- [Fluent conditional statement builder](dynamic-filter-resolver/docs/conditional-statement-builder.md)
- [Declarative sorting](#declarative-sorting)
- [Optional sort exceptions](#optional-sort-exceptions)
- [Programmatic sorting](#programmatic-sorting)
- [Sort associations and persistence contexts](#sort-associations-and-persistence-contexts)
- [Sorting migration and API coverage](#sorting-migration-and-api-coverage)

### Declarative sorting

`JpaSortPolicy.DECLARED` is the restrictive default. In servlet applications, keep the existing opt-in configuration without any new option:

```java
@EnableDynamicFilterServletConfiguration
class FilterConfiguration {
}
```

Repository implementations also default to `DECLARED` outside MVC, even without policy injection. No setter, `@SortPolicy`, per-endpoint flag or second list of attributes is needed for the common case. The servlet integration remains opt-in; this default introduces no Spring Boot autodiscovery or new dependency. Sorting applies to direct repository overloads accepting `ConditionalStatement` with `Sort` or `Pageable`, including entity graphs. It does not replace record authorization or mandatory restrictions.

For an entity `Product` with scalar attributes `price`, `name` and `description`, the search contract needs no second list of sortable attributes:

```java
@FilterTarget(Product.class)
class ProductSearch {
    @Filter(path = "price", parameters = {"priceMin", "priceMax"}, operation = Between.class)
    String price;

    @Filter(path = {"name", "description"}, parameters = "term", operation = AnyFieldLike.class)
    String term;
}

@GetMapping("/products")
Page<Product> products(@ConjunctionFrom(ProductSearch.class) ConditionalStatement statement,
                       Pageable pageable) {
    return repository.findAll(statement, pageable);
}
```

With the standard Spring Data parser:

- `?sort=priceMin,asc` and `?sort=priceMax,desc` sort `price`, even without filter values. Both aliases accept either direction.
- `?sort=name,asc&sort=description,desc` sorts the two compound destinations individually, in the requested precedence.
- `?sort=term,asc` returns HTTP 400 with code `INVALID_SORT`. A compound parameter is not an alias for its first path and never expands into multiple orders. Parameter and path arrays are not zipped.
- If a compound parameter itself is named `description`, `sort=description,asc` still selects the literal `description` path, regardless of its position in the declaration.

OpenAPI publishes the same effective names in `x-sort-names` and follows the configured Spring Data parameter name and delimiter. It retains legitimately documented filter parameters even when they contribute no automatic sort names.

Hidden filters, constants and `Decorated` filters contribute no automatic sort names. This omission does not prohibit another public declaration from publishing the same path. Non-persistent paths and structurally ineligible destinations are omitted with internal DEBUG diagnostics. Omitting one compound destination never promotes the compound parameter to an alias. A valid collection or custom virtual filter continues to generate its predicates.

`ModIgnorePath` does not prohibit sorting a real eligible JPA path. Dynamic and custom operations follow the same structural discovery rules. Request values, dynamic opcodes, synthetic `From`/`To` aliases, value transformers and predicate modifiers such as `ModIgnoreCase` do not change the sort catalog or implicitly modify orders. Only declarations actually used by the search contract contribute, including supported interfaces, meta-annotations and `From` sources. Entity attributes alone do not publish new names.

Conflicting accepted names with different destinations are server configuration errors, detected during warmup when context is available, or on first repository use for later contracts. Invalid requested names receive HTTP 400 with `ProblemDetail`, code `INVALID_SORT` and `rejectedNames`, without hidden fields, exclusions, SQL or internal omission reasons. The handler catches only `InvalidSortException`, not server configuration errors, and requires no BASA dependency. To replace it, register a bean extending `InvalidSortExceptionHandler` and override `handleInvalidSort`. Singular JPA navigation, property access and special mappings are supported.

### Optional sort exceptions

Use `SortPolicy` and `SortAlias` from `com.runestone.dynafilter.core.generator.annotation`
only when the automatic contract needs an exception. Put the policy on the search
type or its endpoint parameter, not on an entity, repository or response DTO.
It does not select the global `JpaSortPolicy` mode.

For a product with persistent `name`, `code`, `description`, `price` and `internalNote`:

```java
@FilterTarget(Product.class)
@SortPolicy(
        include = "description",
        aliases = @SortAlias(name = "term", path = "name"),
        exclude = {"price", "internalNote"}
)
class ProductSearch {
    @Filter(path = {"name", "code"}, parameters = "term", operation = AnyFieldLike.class)
    String term;

    @Filter(path = "price", parameters = {"priceMin", "priceMax"}, operation = Between.class)
    String price;
}

@GetMapping("/products")
Page<Product> products(@ConjunctionFrom(ProductSearch.class)
                       @SortPolicy(include = "price", aliases = @SortAlias(name = "label", path = "name"))
                       ConditionalStatement statement, Pageable pageable) {
    return repository.findAll(statement, pageable);
}
```

`description` is sort-only, without a fictitious filter. `term` deliberately sorts
`name`, not relevance or a computed boolean. `label`, `name` and `code` are also
accepted. `price`, `priceMin` and `priceMax` are forbidden despite the parameter's
include. `internalNote` is a preventive exclusion, even though no filter publishes it.
The policy creates no new filter parameters and does not inspect the response DTO.

Includes, aliases and exclusions add up across the declarative sources actually
used, including supported interfaces, meta-annotations and `From` sources. Canonical
exclusions win globally and remove every name targeting that exact path. They do
not block subtrees. For example, excluding `owner` does not forbid an otherwise
published `owner.name`. Exclusions require a persistent path but need no sortable
terminal, so an association, collection, LOB or binary attribute can be excluded
preventively. Typos in exclusions are server configuration errors.

An explicit include can publish a real path omitted from a hidden or constant
declaration, but cannot bypass structural JPA limitations or canonical exclusions.
Invalid explicit paths and aliases, conflicting destinations and aliases redefining
eligible literal paths are configuration errors, not HTTP 400 errors. Validation
runs during warmup when the sort context is available, otherwise on first ordered
repository use. OpenAPI uses the same validated catalog, including these exceptions.

### Programmatic sorting

Build a public sort contract independently of the conditions used to filter records:

```java
import com.runestone.dynafilter.core.generator.ConditionalStatement;
import com.runestone.dynafilter.core.generator.SortContract;
import com.runestone.dynafilter.core.model.FilterData;
import com.runestone.dynafilter.core.operation.types.Equals;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

var contract = SortContract.builder()
        .include("registerDate", "since")
        .build();
var statement = ConditionalStatement.builder()
        .sortContract(contract)
        .restrict(FilterData.of("name", new String[]{"internalOwner"}, String.class,
                Equals.class, new Object[]{"alice"}))
        .build();

var page = repository.findAll(statement, PageRequest.of(0, 20, Sort.by("since")));
```

Both `since` and the exact path `registerDate` are accepted. Neither `name` nor
`internalOwner` becomes sortable merely because it appears in a restriction.
The same rule applies to ordinary `and`/`or` conditions and decorated filter data.
Sorting does not grant access to records or replace authorization restrictions.

The core contract has no Spring or JPA dependencies. Its builder can declare several
paths and aliases with repeated `include` calls. An optional `.root(Person.class)`
limits reuse to that JPA entity or a JPA entity subtype. Without a root, the adapter
validates paths against the repository's effective entity. Explicit invalid paths
and conflicting names are server configuration errors, not rejected client sorts.
Use `.exclude("name", "phones")` for the same canonical exclusions as `@SortPolicy`.
Exclusions are immutable, participate in contract equality and survive rebuilds,
restrictions and decorators without automatically publishing internal conditions.

`ConditionalStatement.builder(source)` preserves the same immutable contract through
rebuilds, `and`, `or`, `restrict`, and decorators, including predicate replacement.
Calling `sortContract` explicitly replaces the builder's contract without changing
the source or statements already built. Reusing either builder does not mutate
contracts previously produced. Existing immutable candidates are shared, not deeply copied.

In `DECLARED` mode, statements without a catalog still support unsorted queries;
ordered requests fail with `InvalidSortException` without inferring permissions from
internal conditions. Direct repository overloads taking `ConditionalStatement` and
`Sort` or `Pageable`, including entity graphs and sorted unpaged requests, use the
same catalog. Converted Specifications, fluent callbacks, inherited APIs without
`ConditionalStatement`, and internal `query.orderBy` calls are not intercepted.

Programmatic paths use the same metamodel validation as declarative
paths, including singular navigation, embeddables, inherited attributes and property
access. Collections, binary attributes, LOBs and non-scalar terminals remain
ineligible. Structural
eligibility does not guarantee portable ordering semantics, indexing, or query cost
across databases.

### Sort associations and persistence contexts

One search contract and one `Pageable` or `Sort` argument associate automatically.
If the search type already identifies the entity, no extra root declaration is needed;
otherwise use `@FilterTarget(Product.class)` on the search contract or its parameter.
No sort context is required for a contract without associated ordering.

With multiple search or ordering arguments, match each pair using Spring's `@Qualifier`:

```java
Page<Product> products(@Qualifier("products") @ConjunctionFrom(ProductSearch.class) ConditionalStatement statement,
                       @Qualifier("products") Pageable pageable) {
    return repository.findAll(statement, pageable);
}
```

With the standard parser this pair uses `products_sort`, for example
`?products_sort=priceMax,desc&products_sort=name,asc`. Repeated parameters preserve
precedence. Names and delimiters in OpenAPI follow the configured Spring Data resolver,
including qualifiers. `x-sort-names` lists property names, not the complete sort grammar.
Unresolved or duplicate associations are server configuration errors, never a union
of catalogs. MVC still uses the Spring Data parser; validation occurs at the repository,
not in a second MVC parser or validator.

The JPA context selects the single open persistence unit managing the declared root.
If several units manage that entity, provide a `JpaSortPersistenceUnitResolver` bean
returning the registered `EntityManagerFactory` for each contract parameter. This
selects metadata for warmup and documentation, not repository dispatch. Missing roots,
missing units or unresolved ambiguity fail as server configuration errors rather than
publishing an approximate catalog. Effective validation is cached per contract, entity
and factory identity; closed units and context destruction release those caches.

### Sorting migration and API coverage

The default change is intentionally incompatible. Names previously forwarded to
Spring Data now fail unless the effective catalog publishes them. Missing or empty
catalogs reject ordered requests; unsorted queries still work without one. Internal
conditions and authorization restrictions never supply implicit permissions.

For temporary compatibility, choose legacy once for the application:

```java
@EnableDynamicFilterServletConfiguration(sortPolicy = JpaSortPolicy.LEGACY)
class FilterConfiguration {
}
```

The same `JpaSortPolicy` bean controls repository wiring, warmup and OpenAPI.
A consumer-provided bean overrides the annotation's default. Outside servlet integration,
the existing `DynamicFilterJpaRepositoryBeanPostProcessor` can consume a global
`JpaSortPolicy.LEGACY` bean. For manually constructed repositories, apply the shared
policy through `repository.setSortPolicy(JpaSortPolicy.LEGACY)` during application
wiring, not in managers or per request. Omitting a selection never means legacy.

Legacy preserves the original permissive translation: only the first parameter and
first path of a filter are considered, the first translatable mapping wins, and
unknown names pass through to Spring Data. It does not enforce a catalog or advertise
a restrictive OpenAPI list. Both modes preserve sorted `Pageable.unpaged` requests.

Restrictive validation covers all six direct `findAll` overloads whose first argument
is `ConditionalStatement` and whose second argument is `Sort` or `Pageable`: without
a graph, with a graph name, and with an explicit graph type plus name. This includes
convenience graph overloads and sorted unpaged requests. All orders are validated
before content or count SQL; unknown names, expressions and `JpaSort.unsafe` fail
without partial execution. Direction, precedence, ignore-case and provider-supported
options remain intact, including explicit null precedence through the JPA Criteria API. No default sort,
id tie-breaker or page-size limit is added.

Converted Specifications, fluent callbacks, inherited APIs without a statement and
internal `query.orderBy` calls do not receive this sort validation. Existing mandatory
scope guarantees still apply when those APIs consume the scoped statement or its
converted Specification. Sorting never replaces authorization for content or count.

JPA eligibility permits singular to-one navigation, embeddable scalar leaves,
inherited attributes and property access. BASIC values and JPA converters need not
implement `Comparable`. Provider-recognized `@Formula` and `@JoinFormula` mappings
follow the same rules. Collections, plural traversals, binary attributes, LOBs and
entity or embeddable terminals are not eligible. Structural eligibility does not
promise portable SQL ordering, collation, index use or cost across databases.

With Hibernate, LOB and binary checks use the persistence unit's effective JDBC
mapping, including LOBs declared in `orm.xml` and scalar Java values converted to
binary storage. Explicit includes and aliases cannot bypass these exclusions.
The standard JPA metamodel does not expose JDBC storage types. Other providers use
Java type and `@Lob` checks; exclude paths whose XML or provider-specific mappings
hide LOB or binary storage from those checks.

Sorting does not reuse joins with an `ON` condition from a filter or restriction.
Such conditions must not replace a persistent association's sort key with null.
HTTP sort-context warmup applies to mapped controller endpoints, including
`@HttpExchange`, not public helpers.
Warmup and OpenAPI also use the search and policy annotations inherited by MVC
from controller interfaces.

`StatementWrapper` remains a record, now with the fourth component `SortContract
sortContract`. Direct construction must supply that argument; `null` becomes
`SortContract.EMPTY`, which does not authorize sorting. The old three-argument
constructor is not retained. Recompile direct consumers and update record
deconstruction patterns, reflected record components and constructor lookups.
Generated `equals`, `hashCode` and `toString` now include the sort contract, so
wrappers with different contracts are not equal. Other compatibility commitments
remain, including the existing two-argument `DynaFilterOperationCustomizer`
constructor used by previously compiled framework clients.
