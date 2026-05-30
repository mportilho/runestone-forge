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

## Missing Registration

If an application uses a custom operation in `@Filter` without registering a JPA contributor, startup validation fails when the JPA servlet configuration is active.

The error includes the custom operation type and the filter path, for example:

```text
Filter operation 'com.example.filters.FullTextSearch' used by path 'description' is not registered for JPA specifications
```

## OpenAPI Behavior

Custom operations are documented using the common parameter schema:

- If the filter target field is resolved, the parameter schema follows that field type.
- If the target field cannot be resolved, the parameter falls back to `StringSchema`.
- `Dynamic.class`, `IsIn.class`, and `IsNull.class` keep their specialized schemas.

No OpenAPI-specific SPI is required for scalar custom operations. Add one later only if a custom operation needs a special parameter shape.
