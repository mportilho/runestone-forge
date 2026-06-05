# Multipath Filter Plan

This document describes the planned evolution of `@Filter` to support operations that can target multiple entity paths while preserving the existing single-path behavior.

## Implementation Status

Legend:

- `[ ]` Not started
- `[~]` In progress
- `[x]` Done
- `[!]` Blocked or needs decision

| Status | Area | Item |
|---|---|---|
| `[ ]` | Annotation API | Add `paths()` to `@Filter` while keeping `path()` for single-path usage. |
| `[ ]` | Annotation validation | Reject filters that define both `path` and `paths`. |
| `[ ]` | Annotation validation | Reject filters with neither `path` nor `paths`. |
| `[ ]` | Annotation validation | Reject blank path values and blank path segments. |
| `[ ]` | Runtime model | Add `paths()` to `FilterData`. |
| `[ ]` | Runtime model | Keep `FilterData.path()` as a single-path convenience method. |
| `[ ]` | Runtime model | Add `paths()` to `FilterRequestData`. |
| `[ ]` | Operation metadata | Add `FilterPathCardinality`. |
| `[ ]` | Operation metadata | Add path cardinality to `FilterOperationMetadata`. |
| `[ ]` | Registry validation | Validate configured path count against operation metadata. |
| `[ ]` | Built-in operations | Register all existing built-in operations as exactly-one-path operations. |
| `[ ]` | Decorated filters | Restrict `Decorated` to exactly one path. |
| `[ ]` | OpenAPI | Support multipath only for field-independent value shapes. |
| `[ ]` | OpenAPI | Reject multipath for `TARGET_FIELD` value shapes. |
| `[ ]` | Repository sort | Do not translate sort properties for multipath filters. |
| `[ ]` | JPA API | Expose a public helper for resolving JPA paths with joins. |
| `[ ]` | Tests | Add unit tests for annotation path normalization and validation. |
| `[ ]` | Tests | Add tests for path cardinality validation. |
| `[ ]` | Tests | Add OpenAPI tests for allowed and rejected multipath operations. |
| `[ ]` | Tests | Add repository sort tests for multipath filters. |
| `[ ]` | Tests | Add a custom multipath operation registered through a contributor. |
| `[ ]` | Documentation | Update custom operation documentation with a multipath example. |

## Goals

- Allow a single filter operation to target multiple entity paths.
- Preserve existing `@Filter(path = "...")` source compatibility for single-path filters.
- Avoid assigning implicit `OR` or `AND` semantics in the core framework.
- Let each `FilterOperation` decide how multiple paths are interpreted.
- Keep existing built-in operations conservative and single-path by default.
- Make custom JPA operations able to resolve multiple paths consistently with built-in operations.

## Non-Goals

- Do not replace `path()` with `String[] path()`.
- Do not make every existing operation automatically multipath.
- Do not add implicit core-level `OR` or `AND` behavior for multiple paths.
- Do not add a new built-in public multipath operation in the first implementation.
- Do not support multipath `Decorated` filters in the first implementation.
- Do not translate repository sort properties for multipath filters in the first implementation.

## Current State

`@Filter` currently has a single required path:

```java
String path();
```

That path is propagated as a `String` through the runtime model:

- `FilterData.path()`
- `FilterRequestData.path()`
- `DefaultStatementGenerator.createFilterData(...)`
- `AnnotationStatementGenerator`
- `TypeAnnotationUtils.findFilterField(...)`
- `JpaPredicateUtils.computeAttributePath(...)`
- `DynaFilterOperationCustomizer`
- `FilterConfigurationAnalyserBeanPostProcessor`
- `DynamicFilterJpaRepositoryImpl.updateSortFilterPath(...)`
- `StatementWrapper.findDecoratedFilterByPath(...)`

The current model assumes one filter targets one entity path. Multiple request parameters are already supported, but they represent multiple input values for the same target path, such as `Between` using `min` and `max`.

## Design Decisions

### Keep `path` And Add `paths`

The annotation should evolve to:

```java
String path() default "";

String[] paths() default {};
```

Existing single-path usage remains readable:

```java
@Filter(path = "name", parameters = "name", operation = Like.class)
```

New multipath usage is explicit:

```java
@Filter(paths = {"name", "description"}, parameters = "q", operation = FullTextLike.class)
```

Rules:

- `path` and `paths` are mutually exclusive.
- One of `path` or `paths` must be present.
- `path` must not be blank.
- `paths` must not be empty when used.
- Each item in `paths` must not be blank.
- A single value in `paths` is valid but should generally be reserved for callers that build annotation values programmatically or want uniform style.

### Operation Owns Multipath Semantics

The core framework must not decide whether multiple paths mean `OR`, `AND`, weighted full-text search, a date-window expression, or some other domain-specific behavior.

The core responsibilities are:

- normalize annotation path configuration;
- expose paths in runtime metadata;
- validate path cardinality against operation metadata;
- provide helper APIs for common infrastructure concerns, such as JPA path resolution.

The operation responsibilities are:

- decide how paths are combined;
- decide how values relate to paths;
- create the concrete filter object, such as a JPA `Specification<?>`.

### Path Cardinality Belongs In Operation Metadata

`FilterOperationRegistry` already records operation metadata through `FilterOperationMetadata`. The multipath capability should be represented there, not in the annotation alone.

Proposed value object:

```java
public record FilterPathCardinality(int min, Integer max) {

    public static FilterPathCardinality exactlyOne() {
        return new FilterPathCardinality(1, 1);
    }

    public static FilterPathCardinality oneOrMore() {
        return new FilterPathCardinality(1, null);
    }

    public static FilterPathCardinality between(int min, int max) {
        return new FilterPathCardinality(min, max);
    }
}
```

`max == null` means unbounded.

`FilterOperationMetadata` should evolve from:

```java
public record FilterOperationMetadata(FilterValueShape valueShape) {
}
```

to:

```java
public record FilterOperationMetadata(
        FilterValueShape valueShape,
        FilterPathCardinality pathCardinality
) {
}
```

Factory methods should default to `FilterPathCardinality.exactlyOne()` to preserve current behavior.

Example:

```java
public static FilterOperationMetadata targetField() {
    return new FilterOperationMetadata(FilterValueShape.TARGET_FIELD, FilterPathCardinality.exactlyOne());
}

public static FilterOperationMetadata stringValue(FilterPathCardinality pathCardinality) {
    return new FilterOperationMetadata(FilterValueShape.STRING, pathCardinality);
}
```

### Built-In Operations Remain Single-Path

All current built-in JPA operations should be registered as exactly-one-path operations:

- `Between`
- `EndsWith`
- `Equals`
- `Greater`
- `GreaterOrEquals`
- `IsIn`
- `IsNull`
- `Less`
- `LessOrEquals`
- `Like`
- `StartsWith`

This avoids silently changing existing behavior.

### `FilterData` Gets `paths()` And Keeps `path()`

`FilterData` should become multipath-capable while preserving the ergonomic single-path API.

Conceptually:

```java
public record FilterData(
        List<String> paths,
        String[] parameters,
        Class<?> targetType,
        Class<? extends FilterOperation> operation,
        boolean negate,
        Object[] values,
        List<Class<? extends FilterModifier>> modifiers,
        String description
) {

    public String path() {
        if (paths.size() != 1) {
            throw new IllegalStateException("Expected exactly one path but found " + paths.size());
        }
        return paths.getFirst();
    }
}
```

Implementation details:

- Keep constructors or factory methods that accept `String path` where practical.
- Convert a single path to `List.of(path)` internally.
- Prefer immutable path lists.
- Keep error messages explicit when an old single-path operation accidentally receives multiple paths.

### `FilterRequestData` Mirrors `FilterData`

`FilterRequestData` is used for documentation and repository sort translation. It should also expose `paths()` and keep a single-path `path()` convenience method.

This keeps OpenAPI and sort logic able to distinguish single-path and multipath filters.

### Validation Boundaries

There are two kinds of validation.

Structural annotation validation belongs near annotation processing:

- missing path configuration;
- both `path` and `paths` configured;
- blank path values;
- empty `parameters`;
- mismatched `defaultValues` and `constantValues` sizes.

Operation-aware validation belongs where `FilterOperationMetadata` is available:

- operation supports the configured number of paths;
- operation is registered;
- OpenAPI can document the configured operation safely.

Potential validation locations:

- `TypeAnnotationUtils` for structural annotation validation.
- `AbstractFilterOperationService.createFilter(...)` for runtime operation metadata validation.
- `FilterConfigurationAnalyserBeanPostProcessor` for startup validation in Spring MVC/JPA usage.
- `DynaFilterOperationCustomizer` for OpenAPI-specific validation.

## OpenAPI Rules

Multipath documentation should be conservative in the first implementation.

Allowed for multipath:

- `STRING`
- `BOOLEAN`
- `ARRAY`
- `DYNAMIC`

Rejected for multipath in the first implementation:

- `TARGET_FIELD`

Reason: `TARGET_FIELD` currently derives parameter schema from one target field. With multiple paths, fields may have different Java types, Bean Validation annotations, enum values, or JSON schema metadata.

The first implementation should fail fast with a clear message instead of choosing the first path implicitly.

Future options:

- allow field-dependent multipath only when all resolved fields have compatible schemas;
- require explicit `targetType` for field-dependent multipath operations;
- add operation metadata that supplies the OpenAPI schema directly.

## Repository Sort Rules

`DynamicFilterJpaRepositoryImpl.updateSortFilterPath(...)` currently maps a request parameter name to one entity path. This translation is only well-defined for exactly one path.

Rule:

- If a filter has exactly one path, keep current sort translation behavior.
- If a filter has multiple paths, do not translate sort properties for that filter.

Do not use the first path automatically. That would make ordering depend on declaration order and could surprise callers.

Future option: add dedicated metadata for a preferred sortable path. This is intentionally out of scope for the first implementation.

## Decorated Filters

`StatementWrapper` currently stores decorated filters in a map keyed by path:

```java
Map<String, FilterData> decoratedFilters
```

and exposes:

```java
Optional<FilterData> findDecoratedFilterByPath(String path)
```

Multipath decorated filters introduce ambiguous keys and collision behavior. The first implementation should restrict `Decorated` to exactly one path.

Future options:

- index the same decorated filter by all configured paths;
- introduce a separate decorated filter lookup model;
- add a decorator-specific key separate from entity paths.

## Public JPA Path Helper

Custom JPA operations need to resolve paths with the same join behavior as built-in operations. Today, path resolution is implemented internally by `JpaPredicateUtils`, which is package-private under the specification package.

The first implementation should expose a small public helper API in the JPA module.

Possible shape:

```java
public final class JpaFilterPathResolver {

    public static <T> Path<T> computeAttributePath(FilterData filterData, Root<?> root) {
        return computeAttributePath(filterData.path(), filterData, root);
    }

    public static <T> Path<T> computeAttributePath(String path, FilterData filterData, Root<?> root) {
        // Resolve joins using the same rules as built-in operations.
    }

    public static List<Path<?>> computeAttributePaths(FilterData filterData, Root<?> root) {
        // Resolve every path from filterData.paths().
    }
}
```

The helper should preserve current behavior for:

- dot notation paths;
- existing joins reuse;
- join type modifiers such as left or right join;
- collection paths where applicable.

Exact package and method names can be refined during implementation, but the API should be public enough for custom operations outside the internal specification package.

## Custom Multipath Operation Example

The MVP should prove the design with a custom operation registered through `SpecificationFilterOperationContributor`, not by adding a new built-in operation.

Example annotation:

```java
@Filter(
        paths = {"name", "description"},
        parameters = "q",
        operation = AnyLike.class,
        description = "Searches by name or description"
)
```

Example contributor:

```java
registry.register(
        AnyLike.class,
        filterData -> new AnyLikeSpecification<>(filterData, conversionService),
        FilterOperationMetadata.stringValue(FilterPathCardinality.oneOrMore())
);
```

Example operation semantics:

```java
public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
    String value = conversionService.convert(filterData.findOneValue(), String.class);
    String pattern = value == null ? null : "%" + value.toUpperCase() + "%";

    List<Predicate> predicates = filterData.paths().stream()
            .map(path -> JpaFilterPathResolver.<String>computeAttributePath(path, filterData, root))
            .map(path -> builder.like(builder.upper(path), pattern))
            .toList();

    return builder.or(predicates.toArray(Predicate[]::new));
}
```

The operation owns the `OR` semantics. Another operation could use `AND`, compare one input value against two boundary paths, or build a database-specific full-text predicate.

## Implementation Order

1. Add `paths()` to `@Filter` with default `{}` and make `path()` default to `""`.
2. Add path normalization utilities for annotation data.
3. Add structural validation for `path`/`paths` usage.
4. Add `FilterPathCardinality`.
5. Extend `FilterOperationMetadata` with path cardinality and preserve existing factory defaults.
6. Update built-in operation registration to keep exactly-one-path metadata.
7. Update `FilterData` to store paths while preserving `path()` for single-path callers.
8. Update `FilterRequestData` similarly.
9. Update `DefaultStatementGenerator` and `AnnotationStatementGenerator` to pass normalized paths.
10. Add operation-aware path cardinality validation in the operation service layer.
11. Add startup validation in `FilterConfigurationAnalyserBeanPostProcessor`.
12. Update `TypeAnnotationUtils.findFilterField(...)` call sites to validate every path when needed.
13. Update OpenAPI customization with multipath shape restrictions.
14. Update repository sort translation to skip multipath filters.
15. Restrict `Decorated` to exactly one path.
16. Extract or expose the public JPA path helper.
17. Add custom multipath operation tests using a contributor.
18. Update documentation for custom filter operations.

## Test Plan

### Annotation And Model Tests

- Existing `@Filter(path = "name")` remains valid.
- `@Filter(paths = {"name", "description"})` is valid when operation cardinality allows it.
- Both `path` and `paths` configured fails.
- Neither `path` nor `paths` configured fails.
- Blank `path` fails.
- Blank item in `paths` fails.
- `FilterData.path()` returns the only path when exactly one path exists.
- `FilterData.path()` fails with a clear message when multiple paths exist.
- `FilterData.paths()` returns an immutable list.

### Operation Metadata Tests

- Existing metadata factories default to exactly one path.
- `FilterPathCardinality.exactlyOne()` accepts one path and rejects zero or multiple paths.
- `FilterPathCardinality.oneOrMore()` accepts one and multiple paths, rejects zero paths.
- `FilterPathCardinality.between(2, 3)` accepts only counts in range.
- Operation service rejects a filter whose path count exceeds registered metadata.

### JPA Tests

- Built-in operations still work with single-path filters.
- Built-in operations reject multipath filters.
- Custom multipath operation can resolve multiple simple paths.
- Custom multipath operation can resolve nested paths using joins.
- Join type modifiers are preserved when resolving paths through the public helper.

### OpenAPI Tests

- Multipath operation with `STRING` shape is documented as a string query parameter.
- Multipath operation with `BOOLEAN` shape is documented as a boolean query parameter.
- Multipath operation with `TARGET_FIELD` shape fails with a clear configuration error.
- Single-path field-dependent operations keep current schema behavior.

### Repository Sort Tests

- Single-path filter still translates sort parameter to entity path.
- Multipath filter does not translate sort parameter.
- Mixed filters translate only single-path entries.

### Decorated Filter Tests

- Single-path `Decorated` filter keeps current behavior.
- Multipath `Decorated` filter fails during validation.

## Compatibility Notes

Source compatibility:

- Existing annotation usage with `@Filter(path = "...")` remains source-compatible.
- Existing operations using `filterData.path()` remain source-compatible for single-path filters.

Binary compatibility:

- Changing annotation defaults and record constructors may affect binary compatibility depending on exact implementation.
- Avoid removing existing public methods.
- Prefer adding overloads or convenience constructors where practical.

Behavior compatibility:

- Built-in operations should behave exactly as before for single-path filters.
- Multipath configuration on built-in operations should fail clearly instead of being ignored or interpreted implicitly.

## Risks And Mitigations

| Risk | Impact | Mitigation |
|---|---|---|
| OpenAPI documents the wrong schema for multipath field-dependent operations. | Public API documentation becomes misleading. | Reject multipath for `TARGET_FIELD` in the MVP. |
| Existing custom operations receive multipath data accidentally. | Runtime failures in user code. | Default all operations to exactly one path unless metadata explicitly opts into more. |
| Sort translation chooses a surprising path. | Incorrect or confusing ordering. | Skip sort translation for multipath filters. |
| Decorated filters collide on path keys. | Incorrect decorator behavior. | Restrict `Decorated` to exactly one path. |
| Custom JPA operations duplicate path resolution logic. | Inconsistent joins and bugs in nested paths. | Expose a public JPA path helper. |
| Record constructors become awkward after adding `paths`. | Tests and external code require broad changes. | Preserve single-path factories and overloads where possible. |

## Deferred Ideas

- Add a built-in public multipath operation such as `AnyLike` or `FullTextLike`.
- Support field-dependent multipath OpenAPI schemas when all target fields are compatible.
- Add metadata for a preferred sortable path.
- Add a decorator-specific key model that supports multipath decorated filters.
- Add operation metadata for OpenAPI schema override.
- Add database-specific full-text search operations.
