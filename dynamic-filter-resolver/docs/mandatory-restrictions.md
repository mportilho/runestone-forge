# Mandatory statement restrictions

A complete `ConditionalStatement` keeps ordinary filters/decorators separate from mandatory restrictions. In JPA,
complete-statement resolution produces:

```text
decoratedOrdinaryFilter AND restriction1 AND restriction2 ...
```

Restrictions run after ordinary decorators and do not pass through that decorator chain. An ordinary OR or replacement
decorator cannot discard them. Each restriction can contain its own grouping, OR or negation.

Use the [core builder](conditional-statement-builder.md) for native `FilterData` and `AbstractStatement` expressions:

```java
ConditionalStatement scoped = ConditionalStatement.builder(original)
        .restrict(applicationScope)
        .build();
```

The builder preserves ordinary expressions, declared metadata, decorated data, decorators and previous restrictions.
It does not modify the source or guarantee wrapper identity. Existing collaborators and values remain shared, not deeply
copied. Each additional restriction is appended by AND, without replacement or deduplication.

## Existing JPA Specifications

An application can supply its existing Spring Data JPA `Specification<T>` without implementing the statement/analyser SPI:

```java
import com.runestone.dynafilter.core.generator.ConditionalStatement;
import com.runestone.dynafilter.modules.jpa.api.JpaConditionalStatements;
import org.springframework.data.jpa.domain.Specification;

Specification<Component> authorizedScope = authorizationPolicy.componentScope();
ConditionalStatement scoped = JpaConditionalStatements.restrict(original, authorizedScope);

return repository.findAll(scoped, pageable, "Component.logicalApplication");
```

`Component`, the graph name and the authorization policy above belong to the consuming application, not the library.
The helper is in the JPA integration package of the `dynamic-filter-resolver` JAR; there is no separate Maven JPA module.
Its public signature is:

```java
public static <T> ConditionalStatement restrict(
        ConditionalStatement original, Specification<T> restriction);
```

It uses the existing core builder and an internal adapter. There is no Spring Data overload in the core, new builder,
public adapter factory, or security-context dependency. Repeated helper calls append additional mandatory restrictions.

### Predicate-only contract

The supplied specification may construct its predicate and correlated subqueries, including `EXISTS` against another
entity without an inverse association. For subresources, the application must correlate authorization with the owner
of the root entity; the library does not discover ownership or translate a specification for a different root type.

The specification must:

- Preserve root cardinality. Prefer correlated `EXISTS` for plural relationships instead of root joins that multiply rows.
- Not modify the outer query's selection, WHERE, ordering, grouping, HAVING, distinctness or fetches. Query shaping belongs
  in the appropriate ordinary decorators or repository graph options, not in mandatory scope.
- Rebuild Criteria objects from the supplied root/query/builder on every execution. Never cache a `Root`, `Predicate`
  or subquery across content and count queries.
- Capture stable application-owned identity data and remain re-executable. Do not read changing request state or mutate
  captured data between content and count. Shared specifications must meet their own stateless/thread-safe contract.
- Target the repository entity. The helper's `<T>` does **not** make `ConditionalStatement` generic or establish a
  compile-time relationship with the repository.

These obligations are documented contracts, **not a sandbox**. The specification receives the original Criteria objects;
the library does not inspect or undo its query mutations, correct row multiplication, freeze captured state, prove the
authorization policy, or guarantee early detection of an entity mismatch.

### Absence and compatibility

- A null source or specification raises `NullPointerException` immediately; null is not optional scope.
- The internal expression accepts `SpecificationStatementAnalyser` and its subclasses, including the mandatory analyser.
  Other analysers are rejected with `DynamicFilterConfigurationException`, even if they independently produce Specifications.
- Unsupported resolver backends must reject mandatory restrictions rather than silently omit them.
- A mandatory specification returning a null predicate raises `DynamicFilterConfigurationException` when executed.
  `Specification.unrestricted()` is therefore not a valid mandatory restriction by itself.
- Explicit true (`builder.conjunction()`) and false (`builder.disjunction()`) predicates are supported. Non-null does not
  prove that a scope is meaningfully restrictive.
- Only the **observable returned predicate** of an opaque specification is checked. Spring Data may omit null operands
  inside its own composition; the bridge cannot inspect them. Native mandatory AND/OR/NOT nodes separately validate their
  evaluated operands, but that guarantee does not extend inside an opaque specification.

The opaque adapter does not expose paths or parameters to visitors and does not register public filter/sorting aliases.
Existing declared aliases still work. Visiting only the ordinary statement tree does not traverse the restrictions list.

## Repository execution and limits

The JPA repository's `ConditionalStatement` entrypoints resolve the complete statement for `findOne`, `findAll` (including
sort, pagination and entity-graph variants), `count`, `exists`, `convertoToSpecification` and `findBy`.

Content and count use separate Criteria roots/queries. The same supplied specification is evaluated again for count
**when count is actually executed**; Spring Data can skip count on short pages. Graph hints remain repository options,
not state stored by the adapter. Converted Specifications and fluent-query callbacks use persistent entity paths for
sorting rather than the complete statement's alias translation.

Scope remains **opt-in**. Passing only the wrapper to a resolver, rebuilding with the old restriction-free constructor,
calling inherited methods without the complete statement, or supplying a separate unscoped count specification does not
automatically retain authorization. Do not extract an opaque restriction and move it into an ordinary expression:
mandatory semantics depend on its placement and complete-statement resolution.

The library introduces no shared mutable user context. The consumer still owns authentication, authorization decisions,
data limits, stable identity capture, correct entity/owner correlation and use of the scoped repository entrypoints.
