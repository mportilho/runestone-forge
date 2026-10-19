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
- [Sort associations and persistence contexts](dynamic-filter-resolver/docs/declarative-sort-contexts.md)

### Declarative sorting

The restrictive mode is currently a global opt-in. The default remains `JpaSortPolicy.LEGACY` until the final sorting increment. In servlet applications, select the mode on the existing configuration:

```java
@EnableDynamicFilterServletConfiguration(sortPolicy = JpaSortPolicy.DECLARED)
class FilterConfiguration {
}
```

For repositories configured without servlet integration, set the same `JpaSortPolicy.DECLARED` policy on the dynamic filter repository factory or implementation. Sorting applies to direct repository overloads accepting `ConditionalStatement` with `Sort` or `Pageable`, including entity graphs. It does not replace record authorization or mandatory restrictions.

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

Conflicting accepted names with different destinations are server configuration errors, detected during warmup when context is available, or on first repository use for later contracts. Invalid requested names receive the specific HTTP 400 response without internal omission reasons. Singular JPA navigation, property access and special mappings are supported. The global default change belongs to a later increment.

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
import com.runestone.dynafilter.modules.jpa.api.JpaSortPolicy;
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

repository.setSortPolicy(JpaSortPolicy.DECLARED);
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

This increment keeps the existing `LEGACY` default. Select `DECLARED` through the
existing shared JPA policy configuration; the explicit setter above shows direct
repository use. Programmatic paths use the same metamodel validation as declarative
paths, including singular navigation, embeddables, inherited attributes and property
access. Collections, binary attributes, LOBs and non-scalar terminals remain
ineligible. Structural
eligibility does not guarantee portable ordering semantics, indexing, or query cost
across databases.
