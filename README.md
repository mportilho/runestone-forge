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

Conflicting accepted names with different destinations are server configuration errors, detected during warmup when context is available, or on first repository use for later contracts. Invalid requested names receive the specific HTTP 400 response without internal omission reasons. Singular JPA navigation, property access and special mappings are supported. Optional explicit sort policies and the global default change belong to later increments.
