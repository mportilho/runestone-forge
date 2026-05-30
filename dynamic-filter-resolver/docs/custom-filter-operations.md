# Dynamic Filter Resolver - Custom Filter Operations

This guide shows how an application can define a custom filter operation and use it directly in `@Filter(operation = ...)`.

## 1. Define The Operation Contract

Create a public interface that extends `FilterOperation<T>`. The interface is the operation key used by annotations and adapter registries.

```java
package com.example.filters;

import com.runestone.dynafilter.core.operation.FilterOperation;

public interface FullTextSearch<T> extends FilterOperation<T> {
}
```

## 2. Use The Operation In A Filter

Use the custom operation type in the same way as built-in operations such as `Equals.class` or `Like.class`.

```java
package com.example.api;

import com.example.filters.FullTextSearch;
import com.runestone.dynafilter.core.generator.annotation.Conjunction;
import com.runestone.dynafilter.core.generator.annotation.Filter;
import org.springframework.data.jpa.domain.Specification;

@Conjunction(@Filter(
        path = "description",
        parameters = "q",
        operation = FullTextSearch.class,
        description = "Full-text search on description"
))
public interface SearchProducts<T> extends Specification<T> {
}
```

## 3. Implement The JPA Specification

The JPA adapter still needs a concrete `Specification<?>` factory for the custom operation.

```java
package com.example.filters;

import com.runestone.converters.DataConversionService;
import com.runestone.dynafilter.core.model.FilterData;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.Objects;

public final class FullTextSearchSpecification<T> implements Specification<T> {

    private final FilterData filterData;
    private final DataConversionService conversionService;

    public FullTextSearchSpecification(FilterData filterData, DataConversionService conversionService) {
        this.filterData = Objects.requireNonNull(filterData, "filterData cannot be null");
        this.conversionService = Objects.requireNonNull(conversionService, "conversionService cannot be null");
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
        Path<String> path = root.get(filterData.path());
        String value = conversionService.convert(filterData.findOneValue(), String.class);
        String pattern = value == null ? null : "%" + value.toUpperCase() + "%";
        return builder.like(builder.upper(path), pattern);
    }
}
```

This example is intentionally small. Real implementations can reuse project utilities for nested paths, joins, attribute-type conversion, and modifiers such as `ModIgnoreCase`.

## 4. Register The JPA Contributor

Declare a `SpecificationFilterOperationContributor` bean. The auto-configuration collects all contributors and applies them after the built-in operations.

```java
package com.example.config;

import com.example.filters.FullTextSearch;
import com.example.filters.FullTextSearchSpecification;
import com.runestone.converters.DataConversionService;
import com.runestone.dynafilter.core.operation.FilterOperationMetadata;
import com.runestone.dynafilter.modules.jpa.operation.SpecificationFilterOperationContributor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class DynamicFilterConfiguration {

    @Bean
    SpecificationFilterOperationContributor fullTextSearchOperation(DataConversionService conversionService) {
        return registry -> registry.register(
                FullTextSearch.class,
                filterData -> new FullTextSearchSpecification<>(filterData, conversionService)
        );
    }
}
```

Duplicate registrations fail fast. This includes attempts to override built-in operations.

## Operation Metadata

Custom operations can also describe the expected input value shape through `FilterOperationMetadata`. The metadata lives in `core` and is independent from JPA, OpenAPI, SpringDoc, or any concrete schema type.

Available shapes:

- `targetField()` documents and interprets values like the target field type. This is the default when metadata is omitted.
- `stringValue()` documents the parameter as a string.
- `booleanValue()` documents the parameter as a boolean.
- `arrayValue()` documents the parameter as an array.
- `rangeValue()` documents the parameter as an array with exactly two items.
- `dynamicValue()` documents the pseudo-operation value used by `Dynamic.class`.
- `none()` marks a pseudo-operation with no documentable request parameter, used by `Decorated.class`.

Example for a custom boolean operation:

```java
@Bean
SpecificationFilterOperationContributor isFimVigenteOperation(
        DataConversionService conversionService,
        Clock clock
) {
    return registry -> registry.register(
            IsFimVigente.class,
            filterData -> new SpecificationIsFimVigente<>(filterData, conversionService, clock),
            FilterOperationMetadata.booleanValue()
    );
}
```

When this contributor is used by the OpenAPI customizer, `@Filter(operation = IsFimVigente.class)` is documented as a boolean parameter even if the target field is a temporal type.

`IsFimVigente` can remain an application-defined operation:

```java
public interface IsFimVigente<T> extends FilterOperation<T> {
}
```

Recommended runtime semantics for that operation:

- `true`: `fimVigencia IS NULL OR fimVigencia < now`
- `false`: negation of `fimVigencia IS NULL OR fimVigencia < now`
- absent parameter: no filter is generated, unless the application configures `defaultValues` or `required`

Use an injectable `Clock` for `now` so tests can fix the current instant and applications can control the time zone.

## Missing Registration

If an application uses a custom operation in `@Filter` without registering a JPA contributor, startup validation fails when the JPA servlet configuration is active.

The error includes the custom operation type and the filter path, for example:

```text
Filter operation 'com.example.filters.FullTextSearch' used by path 'description' is not registered for JPA specifications
```

## OpenAPI Behavior

OpenAPI documentation uses operation metadata when available:

- `BOOLEAN` becomes a boolean schema.
- `ARRAY` becomes an array schema.
- `RANGE` becomes an array schema with `minItems = 2` and `maxItems = 2`.
- `STRING` becomes a string schema.
- `TARGET_FIELD` follows the target field type.
- `DYNAMIC` becomes an array schema with at least two items.
- `NONE` is not exposed as an OpenAPI parameter.

Every operation known by a `FilterOperationService` must provide metadata. A metadata lookup for an unregistered operation fails with the same configuration error style used by runtime filter creation.

`Dynamic.class` and `Decorated.class` are still pseudo-operations and are not registered as JPA operations. Their metadata is exposed by the operation service so OpenAPI and other integrations can apply the correct behavior without treating missing metadata as normal.
