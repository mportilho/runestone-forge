package com.runestone.dynafilter.modules.jpa.resolver;

import com.runestone.converters.impl.runtime.DefaultRuntimeDataConversionService;
import com.runestone.dynafilter.core.exceptions.DynamicFilterConfigurationException;
import com.runestone.dynafilter.core.generator.ConditionalStatement;
import com.runestone.dynafilter.core.generator.StatementWrapper;
import com.runestone.dynafilter.core.model.FilterData;
import com.runestone.dynafilter.core.model.FilterRequestData;
import com.runestone.dynafilter.core.model.statement.AbstractStatement;
import com.runestone.dynafilter.core.model.statement.CompoundStatement;
import com.runestone.dynafilter.core.model.statement.LogicOperator;
import com.runestone.dynafilter.core.model.statement.LogicalStatement;
import com.runestone.dynafilter.core.model.statement.NegatedStatement;
import com.runestone.dynafilter.core.model.statement.NoOpStatement;
import com.runestone.dynafilter.core.model.statement.StatementAnalyser;
import com.runestone.dynafilter.core.model.statement.StatementVisitor;
import com.runestone.dynafilter.core.operation.FilterOperation;
import com.runestone.dynafilter.core.operation.FilterOperationMetadata;
import com.runestone.dynafilter.core.operation.types.Equals;
import com.runestone.dynafilter.core.operation.types.GreaterOrEquals;
import com.runestone.dynafilter.core.operation.types.extensions.ContainsAll;
import com.runestone.dynafilter.core.operation.types.extensions.IsBlank;
import com.runestone.dynafilter.core.resolver.DynamicFilterResolver;
import com.runestone.dynafilter.core.resolver.FilterDecorator;
import com.runestone.dynafilter.modules.jpa.api.JpaFilterOperationService;
import com.runestone.dynafilter.modules.jpa.tools.app.database.InMemoryDatabaseApplication;
import com.runestone.dynafilter.modules.jpa.tools.app.database.PersonRepository;
import com.runestone.dynafilter.modules.jpa.tools.app.database.jpamodels.Person;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Constructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ContextConfiguration(classes = InMemoryDatabaseApplication.class)
class TestMandatoryRestrictionsJpaIntegration {

    private static final LocalDateTime OLD = LocalDateTime.of(2026, 10, 1, 12, 0);
    private static final LocalDateTime RECENT = LocalDateTime.of(2026, 10, 13, 12, 0);

    @Autowired
    private PersonRepository repository;
    @Autowired
    private EntityManager entityManager;

    private Person aliceOld;
    private Person aliceRecent;
    private Person aliceRecentLater;

    @BeforeEach
    @SuppressWarnings({"unchecked", "rawtypes"})
    void setUp() {
        repository.setDynamicFilterResolver((DynamicFilterResolver) new SpecificationDynamicFilterResolver(
                new JpaFilterOperationService(DefaultRuntimeDataConversionService.standard())));
        repository.deleteAll();
        aliceOld = save("alice", OLD);
        aliceRecent = save("alice", RECENT);
        aliceRecentLater = save("alice", RECENT.plusHours(1));
        save("bob", RECENT);
        save("bob", OLD);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void intersectsRecentUserFilterWithOwnerScopeAfterOrdinaryDecorators() {
        var recent = new LogicalStatement(FilterData.of("registerDate", new String[]{"since"}, LocalDateTime.class,
                GreaterOrEquals.class, new Object[]{RECENT.toString()}));
        var statement = statement(recent, null, owner("alice"));

        assertThat(repository.findAll(statement)).extracting(Person::getId)
                .containsExactlyInAnyOrder(aliceRecent.getId(), aliceRecentLater.getId());
    }

    @ParameterizedTest
    @EnumSource(LogicOperator.class)
    @SuppressWarnings({"unchecked", "rawtypes"})
    void validSiblingCannotMaskAnAbsentMandatoryLeafPredicate(LogicOperator operator) {
        var service = new JpaFilterOperationService(DefaultRuntimeDataConversionService.standard(), List.of(registry ->
                registry.register(AbsentCondition.class, FilterOperationMetadata.targetField(),
                        context -> (root, query, builder) -> null)));
        repository.setDynamicFilterResolver((DynamicFilterResolver) new SpecificationDynamicFilterResolver(service));
        var absent = new LogicalStatement(FilterData.of("name", new String[]{"owner"}, String.class,
                AbsentCondition.class, new Object[]{"alice"}));
        var group = new CompoundStatement(owner("alice"), absent, operator);

        assertThatThrownBy(() -> repository.findAll(statement(new NoOpStatement(), null, group)))
                .isInstanceOf(DynamicFilterConfigurationException.class)
                .hasMessageContaining("predicate");
    }

    @Test
    void ownerScopeWorksWithoutOrdinaryFiltersAndUnrestrictedNoOpStillSelectsEverything() {
        assertThat(repository.findAll(statement(new NoOpStatement(), null, owner("alice"))))
                .extracting(Person::getId).containsExactlyInAnyOrder(aliceOld.getId(), aliceRecent.getId(), aliceRecentLater.getId());
        assertThat(repository.findAll(statement(new NoOpStatement(), null))).hasSize(5);
    }

    @Test
    void decoratorsCanReplaceOrBroadenOrdinaryConditionsButCannotEscapeMandatoryScope() {
        FilterDecorator<Specification<?>> replacement = (filter, wrapper) -> (root, query, builder) -> builder.conjunction();
        FilterDecorator<Specification<?>> broadening = (filter, wrapper) -> filter.or(
                (root, query, builder) -> builder.equal(root.get("name"), "bob"));
        var ordinary = equal("registerDate", LocalDateTime.class, RECENT);

        assertThat(repository.findAll(statement(ordinary, replacement, owner("alice"))))
                .extracting(Person::getId).containsExactlyInAnyOrder(aliceOld.getId(), aliceRecent.getId(), aliceRecentLater.getId());
        assertThat(repository.findAll(statement(ordinary, broadening, owner("alice"))))
                .extracting(Person::getId).containsExactly(aliceRecent.getId());
        assertThat(repository.findAll(statement(ordinary, FilterDecorator.of(List.of(broadening, replacement)), owner("alice"))))
                .extracting(Person::getId).containsExactlyInAnyOrder(aliceOld.getId(), aliceRecent.getId(), aliceRecentLater.getId());
    }

    @Test
    void accumulatedRestrictionsPreserveGroupingAndNegationAndConflictingOwnersSelectNothing() {
        var grouped = new CompoundStatement(owner("alice"), owner("bob"), LogicOperator.DISJUNCTION);
        var statement = statement(new NoOpStatement(), null, grouped,
                new NegatedStatement(owner("bob")), equal("registerDate", LocalDateTime.class, RECENT));

        assertThat(repository.findAll(statement)).extracting(Person::getId).containsExactly(aliceRecent.getId());
        assertThat(repository.findAll(statement(owner("bob"), null, owner("alice")))).isEmpty();
        var negatedGroup = new NegatedStatement(new CompoundStatement(owner("bob"),
                equal("registerDate", LocalDateTime.class, OLD), LogicOperator.DISJUNCTION));
        assertThat(repository.findAll(statement(new NoOpStatement(), null, negatedGroup)))
                .extracting(Person::getId).containsExactlyInAnyOrder(aliceRecent.getId(), aliceRecentLater.getId());
    }

    @Test
    void scopeAppliesToSingleResultsPagingTotalsSortCountExistsSpecificationAndFluentQueries() {
        var scope = statement(new NoOpStatement(), null, owner("alice"));
        var single = statement(equal("registerDate", LocalDateTime.class, RECENT), null, owner("alice"));
        var conflict = statement(owner("bob"), null, owner("alice"));

        assertThat(repository.findOne(single)).get().extracting(Person::getId).isEqualTo(aliceRecent.getId());
        assertThat(repository.findOne(conflict)).isEmpty();
        assertThat(repository.count(scope)).isEqualTo(3);
        assertThat(repository.count(conflict)).isZero();
        assertThat(repository.exists(single)).isTrue();
        assertThat(repository.exists(conflict)).isFalse();
        var page = repository.findAll(scope, PageRequest.of(0, 1, Sort.by("registerDate")));
        assertThat(page.getContent()).extracting(Person::getId).containsExactly(aliceOld.getId());
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(repository.findAll(scope, Sort.by(Sort.Direction.DESC, "registerDate")))
                .extracting(Person::getId).containsExactly(aliceRecentLater.getId(), aliceRecent.getId(), aliceOld.getId());
        assertThat(repository.findAll(repository.convertoToSpecification(scope))).extracting(Person::getId)
                .containsExactlyInAnyOrder(aliceOld.getId(), aliceRecent.getId(), aliceRecentLater.getId());
        List<Person> fluent = repository.findBy(scope, query -> query.sortBy(Sort.by("registerDate")).all());
        assertThat(fluent).extracting(Person::getId).containsExactly(aliceOld.getId(), aliceRecent.getId(), aliceRecentLater.getId());
    }

    @Test
    void entityGraphEntrypointsKeepScopeForListingSingleResultsAndPageCounts() {
        var graph = entityManager.createEntityGraph(Person.class);
        graph.addAttributeNodes("phones");
        String graphName = "mandatory-scope-phones";
        entityManager.getEntityManagerFactory().addNamedEntityGraph(graphName, graph);
        var scope = statement(new NoOpStatement(), null, owner("alice"));
        var single = statement(equal("registerDate", LocalDateTime.class, RECENT), null, owner("alice"));
        var sort = Sort.by("registerDate");
        var pageRequest = PageRequest.of(0, 1, sort);

        assertThat(repository.findOne(single, EntityGraph.EntityGraphType.LOAD, graphName).getId()).isEqualTo(aliceRecent.getId());
        assertThat(repository.findAll(scope, EntityGraph.EntityGraphType.FETCH, graphName))
                .extracting(Person::getId).containsExactlyInAnyOrder(aliceOld.getId(), aliceRecent.getId(), aliceRecentLater.getId());
        assertThat(repository.findAll(scope, sort, EntityGraph.EntityGraphType.LOAD, graphName))
                .extracting(Person::getId).containsExactly(aliceOld.getId(), aliceRecent.getId(), aliceRecentLater.getId());
        assertThat(repository.findAll(scope, sort, graphName))
                .extracting(Person::getId).containsExactly(aliceOld.getId(), aliceRecent.getId(), aliceRecentLater.getId());
        for (var page : List.of(repository.findAll(scope, pageRequest, EntityGraph.EntityGraphType.LOAD, graphName),
                repository.findAll(scope, pageRequest, graphName))) {
            assertThat(page.getContent()).extracting(Person::getId).containsExactly(aliceOld.getId());
            assertThat(page.getTotalElements()).isEqualTo(3);
        }
    }

    @Test
    void restrictionMetadataDoesNotCreateSortAliasesAndDeclaredAliasesStillWork() {
        var alias = new FilterRequestData(new String[]{"registerDate"}, new String[]{"since"}, LocalDateTime.class,
                GreaterOrEquals.class, "false", null, null, null, false, false, List.of(), null);
        var wrapper = new StatementWrapper(new NoOpStatement(), Map.of(), List.of(alias));
        var scope = new ConditionalStatement(wrapper, null, List.of(owner("alice")));

        assertThat(repository.findAll(scope, Sort.by("since"))).extracting(Person::getId)
                .containsExactly(aliceOld.getId(), aliceRecent.getId(), aliceRecentLater.getId());
        assertThatThrownBy(() -> repository.findAll(scope, Sort.by("owner")))
                .isInstanceOf(RuntimeException.class).hasMessageContaining("owner");
    }

    @Test
    void customRestrictionsWorkAndOrdinaryNullPredicatesRemainOptional() {
        var custom = new CustomSpecificationStatement((root, query, builder) -> builder.equal(root.get("name"), "alice"));
        var ordinary = new CustomSpecificationStatement((root, query, builder) -> null);

        assertThat(repository.findAll(statement(ordinary, null, custom))).extracting(Person::getId)
                .containsExactlyInAnyOrder(aliceOld.getId(), aliceRecent.getId(), aliceRecentLater.getId());
        assertThatThrownBy(() -> repository.findAll(statement(new NoOpStatement(), null, new CustomSpecificationStatement(null))))
                .isInstanceOf(DynamicFilterConfigurationException.class).hasMessageContaining("executable filter");
        assertThatThrownBy(() -> repository.findAll(statement(new NoOpStatement(), null, ordinary)))
                .isInstanceOf(DynamicFilterConfigurationException.class).hasMessageContaining("predicate");
        for (var absent : List.of(ordinary, new CustomSpecificationStatement(null))) {
            for (var operator : LogicOperator.values()) {
                var group = new CompoundStatement(owner("alice"), absent, operator);
                assertThatThrownBy(() -> repository.findAll(statement(new NoOpStatement(), null, group)))
                        .isInstanceOf(DynamicFilterConfigurationException.class);
            }
            assertThatThrownBy(() -> repository.findAll(statement(new NoOpStatement(), null, new NegatedStatement(absent))))
                    .isInstanceOf(DynamicFilterConfigurationException.class);
        }
    }

    @Test
    void trueAndOperationSpecificNullOrEmptyValuesAreNotAbsentMandatoryConditions() {
        var trueRestriction = new LogicalStatement(FilterData.of("phones", new String[]{"phones"}, Long.class,
                ContainsAll.class, new Object[]{new Long[0]}));
        var nullValue = new LogicalStatement(FilterData.of("name", new String[]{"name"}, String.class,
                IsBlank.class, new Object[]{null}));

        assertThat(repository.findAll(statement(new NoOpStatement(), null, trueRestriction))).hasSize(5);
        assertThat(repository.findAll(statement(new NoOpStatement(), null, trueRestriction, owner("alice"))))
                .extracting(Person::getId).containsExactlyInAnyOrder(aliceOld.getId(), aliceRecent.getId(), aliceRecentLater.getId());
        assertThat(repository.findAll(statement(new NoOpStatement(), null, nullValue, owner("alice"))))
                .extracting(Person::getId).containsExactlyInAnyOrder(aliceOld.getId(), aliceRecent.getId(), aliceRecentLater.getId());
    }

    @Test
    void fetchingOnlyDecoratorsKeepTheirNullPredicateAndStillApplyFinalScope() {
        var decorator = new FetchingFilterDecorator(List.of(FetchPhones.class.getAnnotation(Fetching.class)));
        var scope = statement(new NoOpStatement(), decorator, owner("alice"));

        var selected = repository.findAll(scope);
        assertThat(selected).extracting(Person::getId)
                .containsExactlyInAnyOrder(aliceOld.getId(), aliceRecent.getId(), aliceRecentLater.getId());
        assertThat(selected).allSatisfy(person -> assertThat(Persistence.getPersistenceUtil().isLoaded(person, "phones")).isTrue());
        assertThat(repository.count(scope)).isEqualTo(3);
    }

    @ParameterizedTest(name = "absent executable={0}, grouping={1}")
    @MethodSource("absentConditionCases")
    @SuppressWarnings({"unchecked", "rawtypes"})
    void extensionsCannotReturnAbsentConditionsEvenInsideNativeNegationAndGroups(boolean absentExecutable, String grouping) {
        var service = new JpaFilterOperationService(DefaultRuntimeDataConversionService.standard(), List.of(registry ->
                registry.register(AbsentCondition.class, FilterOperationMetadata.targetField(),
                        context -> absentExecutable ? null : (root, query, builder) -> null)));
        repository.setDynamicFilterResolver((DynamicFilterResolver) new SpecificationDynamicFilterResolver(service));
        var absent = new LogicalStatement(FilterData.of("name", new String[]{"owner"}, String.class,
                AbsentCondition.class, new Object[]{"alice"}));
        AbstractStatement restriction = switch (grouping) {
            case "root" -> absent;
            case "negation" -> new NegatedStatement(absent);
            case "and-left" -> new CompoundStatement(absent, owner("alice"), LogicOperator.CONJUNCTION);
            case "and-right" -> new CompoundStatement(owner("alice"), absent, LogicOperator.CONJUNCTION);
            case "or-left" -> new CompoundStatement(absent, owner("alice"), LogicOperator.DISJUNCTION);
            case "or-right" -> new CompoundStatement(owner("alice"), absent, LogicOperator.DISJUNCTION);
            case "negated-group" -> new NegatedStatement(new CompoundStatement(owner("alice"), absent, LogicOperator.DISJUNCTION));
            default -> throw new IllegalArgumentException(grouping);
        };

        assertThatThrownBy(() -> repository.findAll(statement(new NoOpStatement(), null, restriction)))
                .isInstanceOf(DynamicFilterConfigurationException.class)
                .hasMessageContaining(absentExecutable ? "executable filter" : "predicate");
    }

    private static Stream<Arguments> absentConditionCases() {
        return Stream.of(false, true).flatMap(absentExecutable -> Stream.of("root", "negation", "and-left", "and-right",
                "or-left", "or-right", "negated-group").map(grouping -> Arguments.of(absentExecutable, grouping)));
    }

    private static LogicalStatement equal(String path, Class<?> targetType, Object value) {
        return new LogicalStatement(FilterData.of(path, new String[]{path}, targetType, Equals.class, new Object[]{value}));
    }

    @Fetching("phones")
    private interface FetchPhones {
    }

    private static final class CustomSpecificationStatement extends AbstractStatement {
        private final Specification<?> specification;

        private CustomSpecificationStatement(Specification<?> specification) {
            this.specification = specification;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <R> R acceptAnalyser(StatementAnalyser<R> analyser) {
            return (R) specification;
        }

        @Override
        public void acceptVisitor(StatementVisitor visitor) {
            // This extension is opaque; only its observable executable result is part of the restriction contract.
        }
    }

    private interface AbsentCondition extends FilterOperation<Specification<?>> {
    }

    private static ConditionalStatement statement(AbstractStatement ordinary, FilterDecorator<Specification<?>> decorator,
                                                   AbstractStatement... restrictions) {
        return new ConditionalStatement(new StatementWrapper(ordinary, null, null), decorator, List.of(restrictions));
    }

    private static LogicalStatement owner(String owner) {
        return new LogicalStatement(FilterData.of("name", new String[]{"owner"}, String.class, Equals.class, new Object[]{owner}));
    }

    private Person save(String owner, LocalDateTime date) {
        try {
            Constructor<Person> constructor = Person.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            Person person = constructor.newInstance();
            ReflectionTestUtils.setField(person, "name", owner);
            ReflectionTestUtils.setField(person, "registerDate", date);
            return repository.save(person);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not create person fixture", e);
        }
    }
}
