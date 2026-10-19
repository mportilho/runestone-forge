package com.runestone.dynafilter.modules.jpa.repository;

import com.runestone.converters.impl.runtime.DefaultRuntimeDataConversionService;
import com.runestone.dynafilter.core.generator.ConditionalStatement;
import com.runestone.dynafilter.core.exceptions.InvalidSortException;
import com.runestone.dynafilter.core.exceptions.DynamicFilterConfigurationException;
import com.runestone.dynafilter.core.model.FilterData;
import com.runestone.dynafilter.core.model.modifiers.ModIgnoreCase;
import com.runestone.dynafilter.core.model.modifiers.ModIgnorePath;
import com.runestone.dynafilter.core.operation.FilterOperation;
import com.runestone.dynafilter.core.operation.FilterOperationMetadata;
import com.runestone.dynafilter.core.operation.types.Between;
import com.runestone.dynafilter.core.operation.types.Dynamic;
import com.runestone.dynafilter.core.operation.types.extensions.IsEmptyCollection;
import com.runestone.dynafilter.core.transformer.FilterValueTransformer;
import com.runestone.dynafilter.core.transformer.FilterValueContext;
import com.runestone.dynafilter.core.operation.types.Decorated;
import com.runestone.dynafilter.core.operation.types.IsIn;
import com.runestone.dynafilter.modules.jpa.api.JpaConditionalStatements;
import com.runestone.dynafilter.core.generator.annotation.AnnotationStatementGenerator;
import com.runestone.dynafilter.core.generator.annotation.AnnotationStatementInput;
import com.runestone.dynafilter.core.generator.annotation.Conjunction;
import com.runestone.dynafilter.core.generator.annotation.Filter;
import com.runestone.dynafilter.core.operation.types.Equals;
import com.runestone.dynafilter.modules.jpa.api.JpaFilterOperationService;
import com.runestone.dynafilter.modules.jpa.api.JpaSortPolicy;
import com.runestone.dynafilter.modules.jpa.resolver.SpecificationDynamicFilterResolver;
import com.runestone.dynafilter.modules.jpa.tools.app.database.InMemoryDatabaseApplication;
import com.runestone.dynafilter.modules.jpa.tools.app.database.jpamodels.Person;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.hibernate.SessionFactory;
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.List;
import java.util.function.Function;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@ContextConfiguration(classes = InMemoryDatabaseApplication.class)
class TestDeclaredSortRepositoryIntegration {

    @Autowired
    private EntityManager entityManager;
    private DynamicFilterJpaRepositoryImpl<Person, Long> repository;
    private final AnnotationStatementGenerator generator = new AnnotationStatementGenerator();
    private static final String GRAPH = "declared-sort-graph";

    @BeforeEach
    @SuppressWarnings({"rawtypes", "unchecked"})
    void setUp() throws Exception {
        repository = new DynamicFilterJpaRepositoryImpl<>(
                JpaEntityInformationSupport.getEntityInformation(Person.class, entityManager), entityManager);
        repository.setDynamicFilterResolver((com.runestone.dynafilter.core.resolver.DynamicFilterResolver)
                new SpecificationDynamicFilterResolver(new JpaFilterOperationService(DefaultRuntimeDataConversionService.standard(), List.of(
                        registry -> registry.register(NameMatch.class, FilterOperationMetadata.stringValue(), context ->
                                (root, query, builder) -> builder.equal(root.get("name"), context.filterData().values()[0]))))));
        repository.setSortPolicy(JpaSortPolicy.DECLARED);
        repository.deleteAll();
        save("Zoe");
        save("Alice");
        save("Bob");
        entityManager.flush();
        entityManager.clear();
        var graph = entityManager.createEntityGraph(Person.class);
        graph.addAttributeNodes("name");
        entityManager.getEntityManagerFactory().addNamedEntityGraph(GRAPH, graph);
    }

    @Test
    void declaredAliasAndExactPathSortWithoutReceivingAFilterValue() {
        var statement = statement(Search.class);

        assertThat(repository.findAll(statement, Sort.by("query"))).extracting(Person::getName)
                .containsExactly("Alice", "Bob", "Zoe");
        assertThat(repository.findAll(statement, Sort.by(Sort.Direction.DESC, "name"))).extracting(Person::getName)
                .containsExactly("Zoe", "Bob", "Alice");
    }

    @Test
    void bothIntervalBoundsSortTheSameAttributeInEitherDirectionWithAndWithoutValues() {
        for (var values : List.<Map<String, Object>>of(Map.of(), Map.of("heightMin", "160", "heightMax", "180"))) {
            var statement = statement(IntervalSearch.class, values);
            for (String name : List.of("heightMin", "heightMax", "height")) {
                assertThat(repository.findAll(statement, Sort.by(name))).extracting(Person::getName)
                        .containsExactly("Alice", "Bob", "Zoe");
                assertThat(repository.findAll(statement, Sort.by(Sort.Direction.DESC, name))).extracting(Person::getName)
                        .containsExactly("Zoe", "Bob", "Alice");
            }
        }
    }

    @Test
    void compoundPathsAreIndependentAndLiteralCoincidencesNeverSelectTheFirstPath() {
        for (Class<?> contract : List.of(CompoundSearch.class, ReversedCompoundSearch.class)) {
            var statement = statement(contract);
            assertThat(repository.findAll(statement, Sort.by("name"))).extracting(Person::getName)
                    .containsExactly("Alice", "Bob", "Zoe");
            assertThat(repository.findAll(statement, Sort.by("id"))).extracting(Person::getName)
                    .containsExactly("Zoe", "Alice", "Bob");
            assertThatThrownBy(() -> repository.findAll(statement, Sort.by("term"))).isInstanceOf(InvalidSortException.class);
        }
        for (Class<?> contract : List.of(LiteralFirstSearch.class, LiteralLastSearch.class)) {
            assertThat(repository.findAll(statement(contract), Sort.by("name"))).extracting(Person::getName)
                    .containsExactly("Alice", "Bob", "Zoe");
        }
    }

    @Test
    void omittingAnIneligibleCompoundDestinationNeverPromotesItsAlias() {
        var statement = statement(PartiallyEligibleSearch.class, Map.of("term", "Alice"));

        assertThat(repository.findAll(statement, Sort.by("name"))).extracting(Person::getName).containsExactly("Alice");
        for (String name : List.of("term", "virtual")) {
            assertThatThrownBy(() -> repository.findAll(statement, Sort.by(name))).isInstanceOf(InvalidSortException.class);
        }
    }

    @Test
    void specialVirtualAndCollectionFiltersStillRunAfterTheirSortContributionsAreOmitted() {
        var virtual = statement(VirtualSearch.class, Map.of("special", "Bob"));
        assertThat(repository.findAll(virtual)).extracting(Person::getName).containsExactly("Bob");
        assertThatThrownBy(() -> repository.findAll(virtual, Sort.by("special"))).isInstanceOf(InvalidSortException.class);
        assertThatThrownBy(() -> repository.findAll(virtual, Sort.by("virtual"))).isInstanceOf(InvalidSortException.class);

        assertThat(repository.findAll(statement(CollectionSearch.class, Map.of("emptyPhones", true)))).hasSize(3);
        assertThat(repository.findAll(statement(CollectionSearch.class, Map.of("emptyPhones", false)))).isEmpty();
        assertThatThrownBy(() -> repository.findAll(statement(CollectionSearch.class), Sort.by("phones")))
                .isInstanceOf(InvalidSortException.class);
    }

    @Test
    void dynamicOperationsAndSyntheticBetweenAliasesNeverChangeThePublicCatalog() {
        var absent = statement(DynamicSearch.class);
        var between = statement(DynamicSearch.class, Map.of("dynamicHeight", new String[]{"BT", "160", "180"}));
        var equals = statement(DynamicSearch.class, Map.of("dynamicHeight", new String[]{"EQ", "170"}));

        assertThat(between.statementWrapper().sortContract()).isSameAs(absent.statementWrapper().sortContract());
        assertThat(repository.findAll(between, Sort.by("dynamicHeight"))).extracting(Person::getName)
                .containsExactly("Alice", "Bob", "Zoe");
        assertThat(repository.findAll(equals, Sort.by("height"))).extracting(Person::getName).containsExactly("Bob");
        for (var statement : List.of(absent, between, equals)) {
            for (String name : List.of("dynamicHeightFrom", "dynamicHeightTo", "BT", "EQ")) {
                assertThatThrownBy(() -> repository.findAll(statement, Sort.by(name))).isInstanceOf(InvalidSortException.class);
            }
        }
    }

    @Test
    void realIgnorePathAndCustomOperationsContributeStructurallyWithoutChangingSortSemantics() {
        for (Class<?> contract : List.of(IgnorePathSearch.class, CustomSearch.class)) {
            assertThat(repository.findAll(statement(contract), Sort.by("query"))).extracting(Person::getName)
                    .containsExactly("Alice", "Bob", "Zoe");
            assertThat(repository.findAll(statement(contract, Map.of("query", "Alice")), Sort.by("query")))
                    .extracting(Person::getName).containsExactly("Alice");
        }
        // A lowercase-only value cannot match mixed-case names without the predicate modifier.
        assertThat(repository.findAll(statement(IgnorePathSearch.class, Map.of("query", "alice")), Sort.by("query")))
                .extracting(Person::getName).containsExactly("Alice");
    }

    @Test
    void sortNeverInvokesFilterValueTransformers() {
        var transformations = new java.util.concurrent.atomic.AtomicInteger();
        var transformingGenerator = new AnnotationStatementGenerator(null, type -> (value, context) -> {
            transformations.incrementAndGet();
            return value.toString().trim();
        });
        var input = new AnnotationStatementInput(TransformedSearch.class, null);
        var absent = new ConditionalStatement(transformingGenerator.generateStatements(input, Map.of()), null);
        assertThat(repository.findAll(absent, Sort.by("query"))).hasSize(3);
        assertThat(transformations.get()).isZero();
        var active = new ConditionalStatement(transformingGenerator.generateStatements(input, Map.of("query", " Alice ")), null);
        assertThat(repository.findAll(active, Sort.by("query"))).extracting(Person::getName).containsExactly("Alice");
        assertThat(transformations.get()).isEqualTo(1);
    }

    @Test
    void everyDirectOverloadRejectsAllInvalidOrdersBeforeSqlIncludingUnsafeKnownNames() {
        var statement = statement(Search.class);
        List<Function<Sort, ?>> calls = List.of(
                sort -> repository.findAll(statement, sort),
                sort -> repository.findAll(statement, sort, GRAPH),
                sort -> repository.findAll(statement, sort, EntityGraph.EntityGraphType.LOAD, GRAPH),
                sort -> repository.findAll(statement, PageRequest.of(0, 1, sort)),
                sort -> repository.findAll(statement, PageRequest.of(0, 1, sort), GRAPH),
                sort -> repository.findAll(statement, PageRequest.of(0, 1, sort), EntityGraph.EntityGraphType.LOAD, GRAPH),
                sort -> repository.findAll(statement, Pageable.unpaged(sort)),
                sort -> repository.findAll(statement, Pageable.unpaged(sort), GRAPH));
        var statistics = entityManager.getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics();
        statistics.clear();

        for (var call : calls) {
            for (var sort : List.of(Sort.by("query", "id", "height"), JpaSort.unsafe("name"),
                    Sort.by("length(name)"), Sort.by("name.extra"))) {
                assertThatThrownBy(() -> call.apply(sort)).isInstanceOf(InvalidSortException.class);
            }
        }
        assertThat(statistics.getPrepareStatementCount()).isZero();
        assertThatThrownBy(() -> repository.findAll(statement, Sort.by("query", "id", "height")))
                .isInstanceOfSatisfying(InvalidSortException.class,
                        exception -> assertThat(exception.rejectedNames()).containsExactly("id", "height"));
    }

    @Test
    void emptyCatalogFailsClosedButUnsortedQueriesNeedNoCatalog() {
        for (var statement : List.of(ConditionalStatement.builder().build(), statement(EmptySearch.class))) {
            assertThat(repository.findAll(statement, Sort.unsorted())).hasSize(3);
            assertThat(repository.findAll(statement, Pageable.unpaged()).getTotalElements()).isEqualTo(3);
            assertThatThrownBy(() -> repository.findAll(statement, Sort.by("name"))).isInstanceOf(InvalidSortException.class);
        }
    }

    @Test
    void automaticallyIneligibleDeclarationsDoNotPublishNamesOrDisallowAnIndependentPublicOrigin() {
        var statement = statement(OmittedSearch.class);
        assertThat(repository.findAll(statement, Sort.by("query"))).extracting(Person::getName)
                .containsExactly("Alice", "Bob", "Zoe");
        for (String name : List.of("privateName", "height", "constantHeight", "decorated", "phones", "virtual")) {
            assertThatThrownBy(() -> repository.findAll(statement, Sort.by(name))).isInstanceOf(InvalidSortException.class);
        }
        var constant = statement(ConstantSearch.class);
        assertThat(repository.findAll(constant)).extracting(Person::getName).containsExactly("Alice");
        assertThatThrownBy(() -> repository.findAll(constant, Sort.by("name"))).isInstanceOf(InvalidSortException.class);
    }

    @Test
    void conflictingEligibleNamesAreConfigurationErrorsRatherThanClientErrors() {
        assertThatThrownBy(() -> repository.findAll(statement(CollidingSearch.class), Sort.by("name")))
                .isInstanceOf(DynamicFilterConfigurationException.class).isNotInstanceOf(InvalidSortException.class);
    }

    @Test
    void compositionAndReplacementDecoratorsPreserveCatalogPredicatesRestrictionsAndEffectiveCount() {
        var source = statement(Search.class);
        var names = FilterData.of("name", new String[]{"internalScope"}, String.class, IsIn.class, new Object[]{new String[]{"Alice", "Bob"}});
        var derived = ConditionalStatement.builder(source)
                .and(FilterData.of("name", new String[]{"internalAnd"}, String.class, Equals.class, new Object[]{"Zoe"}))
                .or(FilterData.of("name", new String[]{"internalOr"}, String.class, Equals.class, new Object[]{"Zoe"}))
                .restrict(names)
                .decorate((com.runestone.dynafilter.core.resolver.FilterDecorator<org.springframework.data.jpa.domain.Specification<?>>)
                        (filter, wrapper) -> (root, query, builder) -> builder.conjunction())
                .build();
        derived = ConditionalStatement.builder(derived).build();
        var statement = JpaConditionalStatements.<Person>restrict(derived,
                (root, query, builder) -> builder.notEqual(root.get("name"), "Zoe"));

        assertThat(statement.statementWrapper().sortContract()).isSameAs(source.statementWrapper().sortContract());
        for (boolean graph : List.of(false, true)) {
            var pageable = PageRequest.of(0, 1, Sort.by(Sort.Order.desc("query").ignoreCase(), Sort.Order.asc("name")));
            var page = graph ? repository.findAll(statement, pageable, GRAPH) : repository.findAll(statement, pageable);
            assertThat(page.getContent()).extracting(Person::getName).containsExactly("Bob");
            assertThat(page.getTotalElements()).isEqualTo(2);
            var next = graph ? repository.findAll(statement, pageable.next(), GRAPH) : repository.findAll(statement, pageable.next());
            assertThat(next.getContent()).extracting(Person::getName).containsExactly("Alice");
        }
        assertThat(repository.findAll(source)).hasSize(3);
        assertThatThrownBy(() -> repository.findAll(statement, Sort.by("internalScope"))).isInstanceOf(InvalidSortException.class);
    }

    @Test
    void unpagedSortKeepsAllResultsAndGraphsInBothPolicies() {
        var statement = statement(Search.class);
        for (var policy : JpaSortPolicy.values()) {
            repository.setSortPolicy(policy);
            var pageable = Pageable.unpaged(Sort.by(Sort.Direction.DESC, "query"));
            for (var page : List.of(repository.findAll(statement, pageable), repository.findAll(statement, pageable, GRAPH),
                    repository.findAll(statement, pageable, EntityGraph.EntityGraphType.LOAD, GRAPH))) {
                assertThat(page.getContent()).extracting(Person::getName).containsExactly("Zoe", "Bob", "Alice");
                assertThat(page.getTotalElements()).isEqualTo(3);
                assertThat(page.getPageable().isUnpaged()).isTrue();
                assertThat(page.getSort()).isEqualTo(Sort.by(Sort.Direction.DESC, "name"));
            }
        }
    }

    @Test
    void explicitlySelectedLegacyRetainsPermissiveFirstParameterAndFirstPathTranslation() {
        repository.setSortPolicy(JpaSortPolicy.LEGACY);
        var statement = statement(LegacySearch.class);
        assertThat(repository.findAll(statement, Sort.by("compound"))).extracting(Person::getName)
                .containsExactly("Alice", "Bob", "Zoe");
        assertThat(repository.findAll(statement, Sort.by("id"))).extracting(Person::getName)
                .containsExactly("Zoe", "Alice", "Bob");
        assertThatThrownBy(() -> repository.findAll(statement, Sort.by("second")))
                .isNotInstanceOf(InvalidSortException.class).hasMessageContaining("second");
    }

    private ConditionalStatement statement(Class<?> contract) {
        return statement(contract, Map.of());
    }

    private ConditionalStatement statement(Class<?> contract, Map<String, Object> values) {
        return new ConditionalStatement(generator.generateStatements(new AnnotationStatementInput(contract, null), values), null);
    }

    private void save(String name) throws Exception {
        var constructor = Person.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        var person = constructor.newInstance();
        ReflectionTestUtils.setField(person, "name", name);
        ReflectionTestUtils.setField(person, "height", new BigDecimal(switch (name) {
            case "Alice" -> "160";
            case "Bob" -> "170";
            default -> "180";
        }));
        repository.save(person);
    }

    @Conjunction(@Filter(path = "name", parameters = "query", operation = Equals.class))
    interface Search {
    }

    interface EmptySearch {
    }

    @Conjunction({
            @Filter(path = "name", parameters = "query", operation = Equals.class),
            @Filter(path = "name", parameters = "privateName", operation = Equals.class, hidden = true),
            @Filter(path = "height", parameters = "constantHeight", operation = Decorated.class, constantValues = "170"),
            @Filter(path = "name", parameters = "decorated", operation = Decorated.class),
            @Filter(path = "phones", parameters = "phones", operation = Equals.class),
            @Filter(path = "virtual", parameters = "virtual", operation = Equals.class)
    })
    interface OmittedSearch {
    }

    @Conjunction({
            @Filter(path = "name", parameters = "query", operation = Equals.class),
            @Filter(path = "id", parameters = "name", operation = Equals.class)
    })
    interface CollidingSearch {
    }

    @Conjunction(@Filter(path = "name", parameters = "constantName", operation = Equals.class, constantValues = "Alice"))
    interface ConstantSearch {
    }

    @Conjunction(@Filter(path = {"name", "id"}, parameters = {"compound", "second"}, operation = Equals.class))
    interface LegacySearch {
    }

    @Conjunction(@Filter(path = "height", parameters = {"heightMin", "heightMax"}, operation = Between.class))
    interface IntervalSearch {
    }

    @Conjunction(@Filter(path = {"id", "name"}, parameters = "term", operation = NameMatch.class))
    interface CompoundSearch {
    }

    @Conjunction(@Filter(path = {"name", "id"}, parameters = "term", operation = NameMatch.class))
    interface ReversedCompoundSearch {
    }

    @Conjunction(@Filter(path = {"name", "id"}, parameters = "name", operation = NameMatch.class))
    interface LiteralFirstSearch {
    }

    @Conjunction(@Filter(path = {"id", "name"}, parameters = "name", operation = NameMatch.class))
    interface LiteralLastSearch {
    }

    @Conjunction(@Filter(path = {"virtual", "name"}, parameters = "term", operation = NameMatch.class, modifiers = ModIgnorePath.class))
    interface PartiallyEligibleSearch {
    }

    @Conjunction(@Filter(path = "virtual", parameters = "special", operation = NameMatch.class, modifiers = ModIgnorePath.class))
    interface VirtualSearch {
    }

    @Conjunction(@Filter(path = "phones", parameters = "emptyPhones", operation = IsEmptyCollection.class))
    interface CollectionSearch {
    }

    @Conjunction(@Filter(path = "height", parameters = "dynamicHeight", operation = Dynamic.class))
    interface DynamicSearch {
    }

    @Conjunction(@Filter(path = "name", parameters = "query", operation = Equals.class, modifiers = {ModIgnorePath.class, ModIgnoreCase.class}))
    interface IgnorePathSearch {
    }

    @Conjunction(@Filter(path = "name", parameters = "query", operation = NameMatch.class))
    interface CustomSearch {
    }

    @Conjunction(@Filter(path = "name", parameters = "query", operation = Equals.class, transformers = Trim.class))
    interface TransformedSearch {
    }

    interface NameMatch extends FilterOperation<Specification<?>> {
    }

    static final class Trim implements FilterValueTransformer {
        @Override
        public Object transform(Object value, FilterValueContext context) {
            return value.toString().trim();
        }
    }
}
