package com.runestone.dynafilter.modules.jpa.repository;

import com.runestone.converters.impl.runtime.DefaultRuntimeDataConversionService;
import com.runestone.dynafilter.core.generator.ConditionalStatement;
import com.runestone.dynafilter.core.exceptions.InvalidSortException;
import com.runestone.dynafilter.core.exceptions.DynamicFilterConfigurationException;
import com.runestone.dynafilter.core.model.FilterData;
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
import org.springframework.data.jpa.repository.EntityGraph;
import org.hibernate.SessionFactory;
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.List;
import java.util.function.Function;

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
                new SpecificationDynamicFilterResolver(new JpaFilterOperationService(DefaultRuntimeDataConversionService.standard())));
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
        return new ConditionalStatement(generator.generateStatements(new AnnotationStatementInput(contract, null), Map.of()), null);
    }

    private void save(String name) throws Exception {
        var constructor = Person.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        var person = constructor.newInstance();
        ReflectionTestUtils.setField(person, "name", name);
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
}
