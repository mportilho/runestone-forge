# Filter Value Transformers

Filter value transformers interpret filter input before a filter operation converts and compares it. They let an application accept aliases or domain-specific input formats while continuing to use standard operations such as `Equals`, `Between`, and `IsIn`.

## Declaration And Order

Implement `FilterValueTransformer` and declare its class in `Filter.transformers`. Transformers run in declaration order, and repeated classes run repeatedly. Spring `@Order` does not change this sequence.

```java
final class CustomerAliasTransformer implements FilterValueTransformer {
    private final CustomerAliasService aliases;

    CustomerAliasTransformer(CustomerAliasService aliases) {
        this.aliases = aliases;
    }

    @Override
    public Object transform(Object value, FilterValueContext context) {
        return aliases.resolve(value.toString());
    }
}

@Conjunction(@Filter(
        path = "customerId",
        parameters = "customer",
        operation = Equals.class,
        transformers = CustomerAliasTransformer.class
))
interface CustomerFilter extends Specification<Order> {
}
```

The processing order is:

1. Select values with `constantValues > request parameters > defaultValues` precedence.
2. Resolve configured value expressions.
3. For `Dynamic`, read the operation code without transforming it.
4. Transform each non-null payload value.
5. Build `FilterData` and let the selected operation convert the transformed result to the target attribute type.

Transformers interpret input; they do not select an operation or replace its comparison semantics.

## Transformer Context

Each invocation receives an immutable `FilterValueContext` containing:

- `parameter` and `parameterIndex`: the declared input and its position.
- `paths`: all paths declared by the filter.
- `operation`: the configured operation, or the effective operation selected by `Dynamic`.
- `declaredTargetType`: exactly `Filter.targetType`, including `Object.class` when no explicit type was declared.

The same context can be reused across requests. Do not mutate data reachable from it.

## Non-Spring Registration

Register application-owned instances during bootstrap, publish the immutable resolver snapshot, and give it to the generator:

```java
FilterValueTransformerRegistry registry = new FilterValueTransformerRegistry();
registry.register(CustomerAliasTransformer.class, transformer);

AnnotationStatementGenerator generator =
        new AnnotationStatementGenerator(valueExpressionResolver, registry.toResolver());
```

Each class can be registered once. Registrations added after `toResolver()` do not affect the published resolver. A declared but unregistered class fails plan compilation with `DynamicFilterConfigurationException`.

## Spring Configuration

With `@EnableDynamicFilterServletConfiguration`, declare each transformer as exactly one compatible Spring bean. Both component scanning and `@Bean` methods are supported, including constructor-injected dependencies:

```java
@Bean
CustomerAliasTransformer customerAliasTransformer(CustomerAliasService aliases) {
    return new CustomerAliasTransformer(aliases);
}
```

Only singleton beans are supported. Transformer instances are shared across requests, so implementations must be stateless or thread-safe. Missing beans, multiple compatible beans, prototypes, and request/session scoped beans are configuration errors detected while controller filter plans are validated. There is no reflective fallback or implicit bean registration. The warmed request path does not query the application context.

## Values And Containers

- Scalar values are passed directly through the declared chain.
- Reference arrays and collections are transformed element by element in one pass without mutating the input.
- `null` values and null container elements bypass transformers.
- Empty strings are values and are transformed.
- `Dynamic EQ`, `BT`, and `IN` transform only their payload; the operation code is never transformed.
- Primitive arrays retain the existing scalar behavior and are not transformed element by element.
- A transformer cannot expand one element into multiple elements.
- Reference arrays produce `Object[]`; collections produce an ordered `ArrayList`. Cardinality and encounter order are preserved, including repeated results when distinct set elements transform to the same value. Concrete input collection type and mutability are not preserved.
- Transformed results are not cached.

## Errors

A transformer must return a non-null result for every non-null input. Returning null or throwing an exception aborts generation with `StatementGenerationException`. The original exception remains available as the cause. The message identifies the transformer, parameter, paths, and the multivalue position when applicable.

Configuration failures use `DynamicFilterConfigurationException`, including duplicate portable registrations and invalid Spring bean resolution or scope.

## OpenAPI

Declaring a transformer does not change generated OpenAPI schema, required status, default value, format, or description. The framework cannot infer which aliases or custom textual forms an application transformer accepts.

Consumers must describe every accepted transformed input format manually. Use `Filter.description` for ordinary filters. For `Dynamic` filters, predeclare the named OpenAPI query parameter with a description or provide an OpenAPI customizer, because dynamic schema generation does not apply `Filter.description`. Keep the documented wire format aligned with transformer behavior.
