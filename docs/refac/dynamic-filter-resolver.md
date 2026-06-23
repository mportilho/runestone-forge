# Refactoring Analysis: dynamic-filter-resolver

## Module Summary

`dynamic-filter-resolver` provides a dynamic filtering framework for Spring Data JPA repositories, annotation-based statement generation, repository support, Spring MVC integration, and SpringDoc/OpenAPI integration.

The architecture appears intended to follow a `core` plus adapter/module split:

- `core`: framework-agnostic filter model, operations, statement generation, resolver/decorator contracts.
- `modules.jpa`: JPA/Spring Data implementation using `Specification`.
- `modules.jpa.spring`: Spring MVC/Servlet integration.
- `modules.openapi`: OpenAPI documentation integration.

The intent is good, but the boundaries have drifted. The most important issue is that `core` currently imports Spring Data JPA. After that, the largest maintainability issue is duplicated implementation across operation/specification classes.

## Current Structure

Approximate package concentration in `src/main/java`:

| Package | Approx. Java Files | Role |
|---|---:|---|
| `com.runestone.dynafilter.core.generator.annotation` | 15 | Public annotations, annotation metadata, scanner/cache/validation utilities, annotation statement generator. |
| `com.runestone.dynafilter.core.operation.types` | 13 | Marker interfaces for operations such as `Equals`, `Less`, `Between`, `Like`. |
| `com.runestone.dynafilter.modules.jpa.operation.specification` | 12 | JPA `Specification` implementations for each operation. |
| `com.runestone.dynafilter.core.model.statement` | 8 | Statement model and visitor types. |
| `com.runestone.dynafilter.modules.jpa.spring` | 6 | Spring MVC/Servlet configuration, argument resolver, decorator factory. |
| `com.runestone.dynafilter.core.generator` | 6 | Statement generator contracts/builders. |
| `com.runestone.dynafilter.modules.jpa.resolver` | 5 | JPA resolver and decorators. |

## Structure Assessment

- Organization: mixed; `core` + adapters, but with layer-style packages inside each area.
- Clarity: drifting.
- Main architectural violation: `core` imports `org.springframework.data.jpa.domain.Specification`.
- Main duplication hotspot: JPA specification classes and operation marker interfaces.
- Main package smell: `core.generator.annotation` mixes API annotations with internal scanning/cache/validation logic.

## Detailed Findings

| Severity | Issue | Evidence | Recommendation |
|---|---|---|---|
| High | `core` depends on Spring Data JPA | `TypeAnnotationUtils.java:34` imports `org.springframework.data.jpa.domain.Specification`; `TypeAnnotationUtils.java:399-401` extracts entity type from `Specification<T>`. | Move JPA-specific target type resolution to `modules.jpa`, for example `JpaFilterTargetResolver`. Keep `core` limited to annotation metadata and generic target resolution through `@FilterTarget`. |
| High | `TypeAnnotationUtils` has too many responsibilities | `TypeAnnotationUtils.java` has 442 lines and handles cache, decorators, statement annotation discovery, validation, recursive annotation scanning, field path lookup, and JPA target class resolution. | Split into `AnnotationMetadataCache`, `FilterAnnotationScanner`, `FilterAnnotationValidator`, `FilterFieldPathResolver`, and JPA-specific resolver. |
| High | JPA specifications duplicate constructor/fields/path/conversion/ignore-case logic | `SpecificationGreater.java:35-52` and `SpecificationLess.java:35-52` are nearly identical except the CriteriaBuilder operation. Similar repetition exists in equals, greater-or-equals, less-or-equals, between, in, like variants. | Add `AbstractJpaSpecificationOperation` or `JpaPredicateContext` to centralize shared mechanics. |
| High | String `LIKE` specifications differ only in wildcard placement | `SpecificationLike.java:45-56`, `SpecificationStartsWith.java:45-56`, `SpecificationEndsWith.java:45-57`. | Introduce `StringLikeSpecificationOperation` with `LikeMode.CONTAINS`, `STARTS_WITH`, `ENDS_WITH`. |
| Medium-high | Public repository API exposes internal wiring and typo | `DynamicFilterJpaRepository.java:183` exposes `convertoToSpecification`; `DynamicFilterJpaRepository.java:192` exposes `setDynamicFilterResolver` marked as internal. | Add correctly named `convertToSpecification`; deprecate typo if compatibility matters. Move setter to package-private internal interface or replace wiring mechanism. |
| Medium-high | Public annotations and internal scanner classes are in same package | `core.generator.annotation` contains `Filter`, `Conjunction`, `Disjunction`, `FilterTarget`, plus `TypeAnnotationUtils`, `FilterAnnotationData`, `VirtualAnnotationHolder`, `AnnotationStatementGenerator`. | Move public annotations to `core.annotation` or `api.annotation`; move scanner/model internals to subpackages. |
| Medium | Operation model is verbose and not open for extension | `Equals.java` is an empty marker interface; `ComparisonOperation.java` maps operation codes to marker classes; `SpecificationFilterOperationService.java:42-54` maintains another manual map to JPA implementations. | Evolve toward `FilterOperator` as enum/value object plus adapter-specific registry/factory. Keep marker interfaces temporarily for compatibility. |
| Medium | JPA path parsing is duplicated | `JpaPredicateUtils` and `FetchingFilterDecorator` both parse dot-separated paths. | Extract `JpaPathParser` returning a common `ParsedJpaPath`. |
| Medium | Spring package name is too broad | `modules.jpa.spring` contains Web MVC argument resolver, servlet config, decorator factory, and configuration analyzer. | Split into `spring.webmvc`, `spring.boot` or `spring.config`, and `spring.decorator`. |
| Medium | `DynamicFilterServletAutoConfiguration` naming may mislead | It appears enabled through annotation import rather than Boot auto-configuration metadata. | Rename to `DynamicFilterServletConfiguration` if opt-in, or add real Boot auto-configuration metadata if automatic. |
| Medium | OpenAPI depends indirectly on JPA target resolution | `DynaFilterOperationCustomizer` calls `TypeAnnotationUtils.findFilterTargetClass`, and that method has JPA-specific logic. | Introduce `FilterTargetClassResolver` and inject a JPA implementation where needed. |
| Low-medium | `targetType` and `format` metadata appear underused | `Filter.targetType()` and `Filter.format()` are copied to data objects, but sampled JPA specs use `expression.getJavaType()` for conversion. | Define whether these are runtime behavior or documentation metadata. Implement or document/deprecate accordingly. |

## Boundary Problem: Core Imports JPA

The highest-priority architectural issue is this dependency direction:

```text
core.generator.annotation.TypeAnnotationUtils
  -> org.springframework.data.jpa.domain.Specification
```

That means a supposedly framework-agnostic package knows about Spring Data JPA. This blocks clean future adapters and makes OpenAPI inherit a JPA assumption indirectly.

Preferred direction:

```text
modules.jpa -> core
modules.openapi -> core
core -> no Spring/JPA imports
```

Recommended extraction:

```text
com.runestone.dynafilter.core.annotation.scanner
  FilterTargetResolver                  // generic contract if needed
  AnnotationFilterTargetResolver         // @FilterTarget / ConditionalStatement rules only

com.runestone.dynafilter.modules.jpa.metadata
  JpaSpecificationTargetResolver         // Specification<T> rules
```

The JPA resolver can combine generic annotation resolution with `Specification<T>` handling. `TypeAnnotationUtils` should no longer know `Specification` exists.

## Duplicated Specifications

### Comparable Operations

Current pattern in `SpecificationGreater` and `SpecificationLess`:

```java
Expression<? extends Comparable<?>> expression = JpaPredicateUtils.computeAttributePath(filterData, root);
Object value = dataConversionService.convert(filterData.findOneValue(), expression.getJavaType());
if (expression.getJavaType().equals(String.class) && filterData.hasModifier(ModIgnoreCase.class)) {
    expression = criteriaBuilder.upper((Expression<String>) expression);
    value = value != null ? value.toString().toUpperCase() : null;
}
return JpaPredicateUtils.toComparablePredicate(expression, value, criteriaBuilder::greaterThan, criteriaBuilder::gt);
```

Only the final operator changes among:

- greater than;
- greater than or equals;
- less than;
- less than or equals.

Recommended internal abstraction:

```text
ComparableJpaSpecification<T>
  - FilterData
  - DataConversionService
  - ComparablePredicateFactory
```

The existing classes can remain as compatibility wrappers:

```java
public final class SpecificationGreater<T> extends ComparableJpaSpecification<T> {
    public SpecificationGreater(FilterData filterData, DataConversionService conversionService) {
        super(filterData, conversionService, ComparableOperator.GREATER_THAN);
    }
}
```

### LIKE Operations

Current variants:

- `SpecificationLike`: `%value%`
- `SpecificationStartsWith`: `value%`
- `SpecificationEndsWith`: `%value`

Recommended internal abstraction:

```text
StringLikeJpaSpecification<T>
  - LikeMode.CONTAINS
  - LikeMode.STARTS_WITH
  - LikeMode.ENDS_WITH
```

This centralizes:

- path resolution;
- conversion to `String`;
- `ModIgnoreCase` handling;
- wildcard generation;
- `criteriaBuilder.like` invocation.

## Operation Model Simplification

The current model uses many marker interfaces:

```java
public interface Equals<T> extends FilterOperation<T> {
}
```

Then operation codes map to marker interfaces:

```java
EQ(Equals.class)
```

Then adapter service maps marker interfaces to implementations:

```java
operations.put(Equals.class, filterData -> new SpecificationEquals<>(filterData, conversionService));
```

This works, but it makes adding operations noisy. A cleaner model would be:

```java
enum FilterOperator {
    EQUALS,
    LESS_THAN,
    LESS_THAN_OR_EQUALS,
    GREATER_THAN,
    GREATER_THAN_OR_EQUALS,
    LIKE,
    STARTS_WITH,
    ENDS_WITH,
    IN,
    BETWEEN,
    IS_NULL
}
```

Then each adapter owns:

```text
Map<FilterOperator, JpaSpecificationFactory>
```

This should be done carefully because operation marker interfaces may be part of the public API. Prefer an incremental path:

1. Introduce `FilterOperator` internally.
2. Map existing marker classes to `FilterOperator`.
3. Refactor adapter registry to use `FilterOperator` internally.
4. Deprecate marker interfaces only in a later major version if desired.

## Package Reorganization Proposal

Recommended target structure:

```text
com.runestone.dynafilter.core.annotation
  Filter
  Conjunction
  Disjunction
  ConjunctionFrom
  DisjunctionFrom
  FilterDecorators
  FilterTarget
  Statement

com.runestone.dynafilter.core.annotation.model
  AnnotationStatementInput
  FilterAnnotationData
  FilterAnnotationStatement
  VirtualAnnotationHolder

com.runestone.dynafilter.core.annotation.scanner
  AnnotationMetadataCache
  FilterAnnotationScanner
  FilterAnnotationValidator
  FilterDecoratorMetadataResolver
  FilterFieldPathResolver

com.runestone.dynafilter.core.generator
  StatementGenerator
  DefaultStatementGenerator
  AnnotationStatementGenerator
  ValueExpressionResolver

com.runestone.dynafilter.core.model
com.runestone.dynafilter.core.model.statement
com.runestone.dynafilter.core.operation
com.runestone.dynafilter.core.resolver

com.runestone.dynafilter.modules.jpa.metadata
  JpaSpecificationTargetResolver

com.runestone.dynafilter.modules.jpa.path
  JpaPathParser
  ParsedJpaPath

com.runestone.dynafilter.modules.jpa.operation
com.runestone.dynafilter.modules.jpa.operation.specification
com.runestone.dynafilter.modules.jpa.repository
com.runestone.dynafilter.modules.jpa.resolver

com.runestone.dynafilter.modules.jpa.spring.webmvc
com.runestone.dynafilter.modules.jpa.spring.config
com.runestone.dynafilter.modules.jpa.spring.decorator

com.runestone.dynafilter.modules.openapi
```

Do not move everything at once. First extract classes with no API package rename, then consider package movement when tests are stable.

## Recommended Refactoring Order

1. Add tests around `TypeAnnotationUtils.findFilterTargetClass` behavior before changing it.
2. Extract JPA-specific `Specification<T>` detection from `TypeAnnotationUtils` into `modules.jpa.metadata`.
3. Update `FilterConfigurationAnalyserBeanPostProcessor` and OpenAPI customizer to use an explicit resolver instead of calling a static utility with hidden JPA behavior.
4. Introduce `JpaPredicateContext` or `AbstractJpaSpecificationOperation`.
5. Refactor comparable specs: `Greater`, `GreaterOrEquals`, `Less`, `LessOrEquals`.
6. Refactor string like specs: `Like`, `StartsWith`, `EndsWith`.
7. Extract `JpaPathParser` and use it from both predicate utilities and fetching decorator.
8. Split `TypeAnnotationUtils` into scanner/cache/validator/field resolver.
9. Clean `DynamicFilterJpaRepository` public API: add `convertToSpecification`, deprecate `convertoToSpecification`, and hide internal setter.
10. Reorganize Spring packages once behavior-preserving extractions are done.

## Suggested Tests

- Unit tests for generic target resolution without Spring Data JPA dependency.
- JPA-specific tests for resolving `Specification<T>` entity type.
- Parameterized tests for comparable operations, using one test matrix for `GT`, `GE`, `LT`, `LE`.
- Parameterized tests for `LIKE`, `STARTS_WITH`, `ENDS_WITH`, including `ModIgnoreCase` and `null` value behavior.
- Tests for dotted path parsing shared by predicates and fetch decorators.
- Repository API compatibility tests if public method deprecation is introduced.

## Risk Notes

- Moving public annotations is a breaking source/binary change. Avoid moving them until a major version or keep deprecated bridge annotations if required.
- Removing marker interfaces may break users who implemented custom operations. Keep compatibility while introducing `FilterOperator` internally.
- `DynamicFilterJpaRepository.convertoToSpecification` is misspelled but public. Correcting by replacement is breaking; add a correctly spelled method and deprecate the old one first.
- Spring lifecycle changes around `SpringFilterDecoratorFactory` can alter bean creation timing. Refactor with integration tests.

## Optional Future Module Split

If this library keeps growing, a Maven split would make the architecture clearer:

```text
dynamic-filter-core
dynamic-filter-jpa
dynamic-filter-spring-webmvc
dynamic-filter-spring-boot-starter
dynamic-filter-openapi
```

This is not necessary as the first refactor. It becomes useful when consumers need core without Spring Data JPA or OpenAPI dependencies.
