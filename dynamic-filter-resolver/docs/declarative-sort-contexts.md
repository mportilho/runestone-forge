# Declarative sort associations and persistence contexts

This increment extends the `DECLARED` policy introduced by issue #181. It does not change the current `LEGACY` default. The default change belongs to the final migration increment of spec #180.

```java
@EnableDynamicFilterServletConfiguration(sortPolicy = JpaSortPolicy.DECLARED)
class WebConfiguration {
}
```

The same global policy controls repository validation and OpenAPI documentation. In `LEGACY`, the customizer does not advertise a restrictive allowlist. Without servlet integration, configure the repository's `JpaSortPolicy` explicitly as in the first increment.

## Common case

One `ConditionalStatement` and one `Pageable` or `Sort` associate automatically. There is no extra association annotation or list of sort names.

```java
@FilterTarget(Person.class)
record Search(
        @Filter(path = "name", parameters = "query", operation = Equals.class) String query
) {
}

@GetMapping("/people")
Page<Person> people(@ConjunctionFrom(Search.class) ConditionalStatement statement, Pageable pageable) {
    return repository.findAll(statement, pageable);
}
```

The existing `FilterTarget` on the search contract supplies the JPA root, so the endpoint does not repeat it. For filter metadata using `Specification<Person>` or an inherited typed Specification interface, the root can also come from the generic type. This metadata inference does not add restrictive sort interception to inherited Specification repository APIs. An external search class without a root can receive `@FilterTarget(Person.class)` on the parameter when inference is impossible. Response DTOs and their fields are not sources of root or catalog discovery.

The adapter finds the unique open persistence unit whose metamodel manages that root. Several units can coexist without extra configuration if only one manages the root. Missing or ambiguous contexts fail as server configuration errors during controller warmup or documentation construction. A contract without associated ordering does not trigger sort-context validation.

## Multiple arguments

When the signature is ambiguous, associate each ordering argument with exactly one contract using matching Spring `@Qualifier` values. Do not associate by position.

```java
@GetMapping("/paired")
Result paired(
        @Qualifier("left") @ConjunctionFrom(Search.class) ConditionalStatement left,
        @Qualifier("left") Pageable leftPage,
        @Qualifier("right") @ConjunctionFrom(IdentifierSearch.class) ConditionalStatement right,
        @Qualifier("right") Pageable rightPage) {
    return combine(repository.findAll(left, leftPage), repository.findAll(right, rightPage));
}
```

Each ordering argument needs one matching contract. Missing matches, duplicate matching contracts, and reuse of the same contract by multiple ordering arguments are configuration errors. Additional contracts may remain unassociated when they are not used with ordering. The adapter never merges catalogs. Each repository invocation still supplies its own statement and ordering explicitly; MVC associations do not select or dispatch repositories.

With Spring Data defaults, the query parameters are `left_sort` and `right_sort`, with pagination parameters such as `left_page` and `right_size`. The parser receives repeated values in precedence order:

```text
?left_sort=query,desc&left_sort=name,asc&right_sort=identifier,asc
```

The customizer reads the resolver actually selected by MVC, including the sort resolver inside Pageable. It reflects configured names, qualifier delimiters and property delimiters. For example, a sort resolver configured with `setSortParameter("order")`, `setQualifierDelimiter("-")` and `setPropertyDelimiter(":")` uses:

```text
?left-order=query:desc&left-order=name:asc&right-order=identifier:asc
```

The same Spring-managed customizer also participates in Springdoc's parameter customization. It records the origin of generated sort parameters and removes obsolete generated names after adding the configured names. It does not identify generated parameters by their descriptions or delete independent request parameters named `sort`.

An expression can group properties before its direction and optional `ignorecase`. Direction can be omitted and defaults to ascending for that expression. `x-sort-names` lists effective public names, not complete sort expressions. OpenAPI documents an array of expressions using query `form` style and `explode=true`, not a string enum pretending to encode the grammar.

There is no additional request parser or early MVC sort validation. The repository validates all parsed orders through `JpaSortCatalog`. Unsupported custom resolver implementations cause a documentation configuration error rather than publication of an assumed grammar. Standalone MVC integration can provide the configured Pageable or Sort resolver as a bean when there is no handler adapter.

## Exceptional persistence-unit binding

If the same entity belongs to multiple units, register one optional `JpaSortPersistenceUnitResolver` bean. It can distinguish contracts through their qualifiers or other declared parameter metadata:

```java
@Bean
JpaSortPersistenceUnitResolver sortPersistenceUnits(
        @Qualifier("firstEntityManagerFactory") EntityManagerFactory first,
        @Qualifier("secondEntityManagerFactory") EntityManagerFactory second) {
    return (parameter, entity) -> {
        Qualifier qualifier = parameter.getAnnotation(Qualifier.class);
        if (qualifier == null) {
            return null;
        }
        return switch (qualifier.value()) {
            case "left" -> first;
            case "right" -> second;
            default -> null;
        };
    };
}
```

Return `null` for automatic unambiguous inference. A selected factory must be registered, open, and manage the root. Invalid explicit bindings fail without falling back. `@Primary` does not resolve a persistence-unit ambiguity. Neither catalog unions nor intersections are used. The application must still call the repository belonging to the selected unit.

## Cache ownership and compatibility

Declarative plans remain owned by the statement generator. Effective validation is cached separately by contract and effective entity inside each factory owner, with at most 4096 entries per catalog. Factories with different mappings do not share validation even if the entity class is identical. No Criteria execution objects are cached.

Context discovery discards closed or removed factories on the next lookup and validates replacement factories independently. Servlet bean destruction clears owned caches. There is no global factory registry retaining closed units indefinitely.

The public two-argument `DynaFilterOperationCustomizer` constructor is unchanged. A Spring-managed instance receives catalog integration automatically through `BeanFactoryAware`; external frameworks do not need a new constructor, BASA dependency, or consumer changes. The regression fixture executes a client compiled against the original constructor signature without recompiling it against the updated implementation.

Catalogs and associated filter schemas use the shared JPA metamodel support, including singular navigation and property access. Each argument uses its own resolved entity metadata. Sort-policy exceptions remain a later increment. These associations do not change authorization predicates, graphs, counts, or sort coverage outside the existing direct ConditionalStatement repository overloads.
