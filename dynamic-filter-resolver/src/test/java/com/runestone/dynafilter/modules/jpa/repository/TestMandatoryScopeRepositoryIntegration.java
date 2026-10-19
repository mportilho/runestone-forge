package com.runestone.dynafilter.modules.jpa.repository;

import com.runestone.converters.impl.runtime.DefaultRuntimeDataConversionService;
import com.runestone.dynafilter.core.exceptions.DynamicFilterConfigurationException;
import com.runestone.dynafilter.core.generator.ConditionalStatement;
import com.runestone.dynafilter.core.generator.StatementWrapper;
import com.runestone.dynafilter.core.generator.annotation.Filter;
import com.runestone.dynafilter.core.model.FilterData;
import com.runestone.dynafilter.core.model.FilterRequestData;
import com.runestone.dynafilter.core.model.statement.AbstractStatement;
import com.runestone.dynafilter.core.model.statement.LogicalStatement;
import com.runestone.dynafilter.core.model.statement.NoOpStatement;
import com.runestone.dynafilter.core.operation.types.Equals;
import com.runestone.dynafilter.core.operation.types.GreaterOrEquals;
import com.runestone.dynafilter.core.resolver.DynamicFilterResolver;
import com.runestone.dynafilter.core.resolver.FilterDecorator;
import com.runestone.dynafilter.modules.jpa.api.JpaConditionalStatements;
import com.runestone.dynafilter.modules.jpa.api.JpaFilterOperationService;
import com.runestone.dynafilter.modules.jpa.resolver.Fetching;
import com.runestone.dynafilter.modules.jpa.resolver.FetchingFilterDecorator;
import com.runestone.dynafilter.modules.jpa.resolver.SpecificationDynamicFilterResolver;
import com.runestone.dynafilter.modules.jpa.tools.app.database.InMemoryDatabaseApplication;
import com.runestone.dynafilter.modules.jpa.tools.app.database.PersonRepository;
import com.runestone.dynafilter.modules.jpa.tools.app.database.PhoneRepository;
import com.runestone.dynafilter.modules.jpa.tools.app.database.jpamodels.Person;
import com.runestone.dynafilter.modules.jpa.tools.app.database.jpamodels.Phone;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Persistence;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ContextConfiguration(classes = InMemoryDatabaseApplication.class)
class TestMandatoryScopeRepositoryIntegration {

    private static final LocalDateTime OLD = LocalDateTime.of(2026, 10, 1, 12, 0);
    private static final LocalDateTime RECENT = LocalDateTime.of(2026, 10, 13, 12, 0);
    private static final BigDecimal HEIGHT = BigDecimal.valueOf(100);
    private static final String GRAPH = "mandatory-scope-repository-phones";
    private static final List<FilterRequestData> DECLARED_FILTERS = List.of(
            declaration("since"), declaration("identifier"));

    @Autowired
    private PersonRepository repository;
    @Autowired
    private PhoneRepository phoneRepository;
    @Autowired
    private EntityManager entityManager;

    private Person aliceOld;
    private Person aliceRecent;
    private Person aliceLater;

    @BeforeEach
    @SuppressWarnings({"unchecked", "rawtypes"})
    void setUp() {
        var resolver = new SpecificationDynamicFilterResolver(
                new JpaFilterOperationService(DefaultRuntimeDataConversionService.standard()));
        repository.setDynamicFilterResolver((DynamicFilterResolver) resolver);
        phoneRepository.setDynamicFilterResolver((DynamicFilterResolver) resolver);
        repository.deleteAll();
        // Deliberately insert out of date order so translating a date alias into an ID alias is observable.
        aliceLater = save("alice", RECENT.plusHours(1), HEIGHT);
        aliceOld = save("alice", OLD, HEIGHT);
        aliceRecent = save("alice", RECENT, HEIGHT);
        save("alice", RECENT.plusHours(2), BigDecimal.valueOf(200));
        save("bob", RECENT, HEIGHT);
        save("bob", RECENT.plusHours(3), HEIGHT);
        save("bob", OLD, HEIGHT);
        entityManager.flush();
        entityManager.clear();

        var graph = entityManager.createEntityGraph(Person.class);
        graph.addAttributeNodes("phones");
        entityManager.getEntityManagerFactory().addNamedEntityGraph(GRAPH, graph);
    }

    @Test
    void graphConvenienceVariantsTranslateDeclaredSortAliasesExactlyOnceWithoutChangingScope() {
        var statement = scoped(new NoOpStatement(), null);
        var sort = Sort.by("since");

        assertThat(repository.findAll(statement, sort, GRAPH)).extracting(Person::getId)
                .containsExactly(aliceOld.getId(), aliceRecent.getId(), aliceLater.getId());
        var page = repository.findAll(statement, PageRequest.of(0, 1, sort), GRAPH);
        assertThat(page.getContent()).extracting(Person::getId).containsExactly(aliceOld.getId());
        assertThat(page.getTotalElements()).isEqualTo(3);
    }

    @Test
    void pageContentAndEffectiveCountApplyOrdinaryFiltersAndEverySharedRestrictionWithoutMutation() {
        var ownerData = FilterData.of("name", new String[]{"mandatoryOwner"}, String.class, Equals.class, new Object[]{"alice"});
        var heightData = FilterData.of("height", new String[]{"mandatoryHeight"}, BigDecimal.class, Equals.class, new Object[]{HEIGHT});
        var recentData = FilterData.of("registerDate", new String[]{"since"}, LocalDateTime.class,
                GreaterOrEquals.class, new Object[]{RECENT.toString()});
        var wrapper = new StatementWrapper(new LogicalStatement(recentData), Map.of(), DECLARED_FILTERS, null);
        var statement = new ConditionalStatement(wrapper, null,
                List.of(new LogicalStatement(ownerData), new LogicalStatement(heightData)));
        var pageable = PageRequest.of(0, 1, Sort.by("since"));

        // A full first page with another scoped result forces count execution instead of a short-page shortcut.
        var first = repository.findAll(statement, pageable);
        assertThat(first.getContent()).extracting(Person::getId).containsExactly(aliceRecent.getId());
        assertThat(first.getTotalElements()).isEqualTo(2);
        assertThat(first.getTotalPages()).isEqualTo(2);
        var second = repository.findAll(statement, pageable.next());
        assertThat(second.getContent()).extracting(Person::getId).containsExactly(aliceLater.getId());
        assertThat(second.getTotalElements()).isEqualTo(2);
        assertThat(repository.count(statement)).isEqualTo(2);
        assertThat(repository.findAll(statement, pageable).getContent()).extracting(Person::getId)
                .containsExactly(aliceRecent.getId());

        assertThat(pageable.getSort()).isEqualTo(Sort.by("since"));
        assertThat(statement.statementWrapper()).isSameAs(wrapper);
        assertThat(wrapper.allFilters()).containsExactlyElementsOf(DECLARED_FILTERS);
        assertThat(wrapper.decoratedFilters()).isEmpty();
        assertThat(ownerData.values()).containsExactly("alice");
        assertThat(heightData.values()).containsExactly(HEIGHT);
        assertThat(recentData.values()).containsExactly(RECENT.toString());
        assertThat(ownerData.parameters()).containsExactly("mandatoryOwner");
        assertThat(heightData.parameters()).containsExactly("mandatoryHeight");
    }

    @Test
    void specificationExistsScopeIsRebuiltForContentAndCountAndIntersectsTheCompleteFunctionalFilter() {
        save("alice", RECENT.plusHours(4), HEIGHT, "private");
        entityManager.flush();
        entityManager.clear();
        var source = scoped(recent(), null);
        var roots = new ArrayList<Root<?>>();
        var queries = new ArrayList<CriteriaQuery<?>>();
        var sharedPhones = sharedPhoneScope();
        Specification<Person> scope = (root, query, builder) -> {
            roots.add(root);
            queries.add(query);
            return sharedPhones.toPredicate(root, query, builder);
        };
        var statement = JpaConditionalStatements.restrict(source, scope);

        var page = repository.findAll(statement, PageRequest.of(0, 1, Sort.by("since")));

        assertThat(page.getContent()).extracting(Person::getId).containsExactly(aliceRecent.getId());
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(queries).hasSize(2);
        assertThat(queries.getFirst().getResultType()).isEqualTo(Person.class);
        assertThat(queries.getLast().getResultType()).isEqualTo(Long.class);
        assertThat(queries.getFirst()).isNotSameAs(queries.getLast());
        assertThat(roots.getFirst()).isNotSameAs(roots.getLast());
        assertThat(repository.findAll(statement, Sort.by("since"))).extracting(Person::getId)
                .containsExactly(aliceRecent.getId(), aliceLater.getId());
        assertThat(source.restrictions()).hasSize(2);
        assertThat(source.statementWrapper().allFilters()).containsExactlyElementsOf(DECLARED_FILTERS);
    }

    @Test
    void specificationScopeSurvivesLaterReplacementDecoratorAndGraphPaginationWithoutLosingAliases() {
        save("alice", RECENT.plusHours(4), HEIGHT, "private");
        entityManager.flush();
        entityManager.clear();
        var source = scoped(equal("name", "searchOwner", String.class, "bob"), fetching());
        var restricted = JpaConditionalStatements.restrict(source, sharedPhoneScope());
        FilterDecorator<Specification<?>> replacement = (filter, wrapper) ->
                (root, query, builder) -> builder.conjunction();
        var statement = ConditionalStatement.builder(restricted).decorate(replacement).build();

        var page = repository.findAll(statement, PageRequest.of(0, 1, Sort.by("since")), GRAPH);

        assertThat(page.getContent()).extracting(Person::getId).containsExactly(aliceOld.getId());
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertPhonesLoaded(page.getContent());
        assertThat(repository.count(statement)).isEqualTo(3);
        assertThat(repository.findAll(source)).isEmpty();
        assertThat(statement.statementWrapper().allFilters()).containsExactlyElementsOf(DECLARED_FILTERS);
        assertThatThrownBy(() -> repository.findAll(statement, Sort.by("mandatoryOwner")))
                .isInstanceOf(RuntimeException.class).hasMessageContaining("mandatoryOwner");
    }

    @Test
    void repeatedSpecificationRestrictionsAccumulateByAndWithoutChangingEarlierStatements() {
        var source = scoped(new NoOpStatement(), null);
        var first = JpaConditionalStatements.<Person>restrict(source, (root, query, builder) ->
                builder.greaterThanOrEqualTo(root.get("registerDate"), RECENT));
        var second = JpaConditionalStatements.<Person>restrict(first, (root, query, builder) ->
                builder.lessThan(root.get("registerDate"), RECENT.plusHours(1)));
        var conflict = JpaConditionalStatements.<Person>restrict(second, (root, query, builder) ->
                builder.equal(root.get("name"), "bob"));

        assertThat(repository.findAll(second)).extracting(Person::getId).containsExactly(aliceRecent.getId());
        assertThat(repository.count(second)).isEqualTo(1);
        assertThat(repository.findAll(conflict)).isEmpty();
        assertThat(repository.count(first)).isEqualTo(2);
        assertThat(repository.count(source)).isEqualTo(3);
        assertThat(source.restrictions()).hasSize(2);
        assertThat(first.restrictions()).hasSize(3);
        assertThat(second.restrictions()).hasSize(4);
    }

    @ParameterizedTest(name = "explicit predicate is true: {0}")
    @ValueSource(booleans = {false, true})
    void specificationRestrictionsAcceptExplicitTrueAndFalse(boolean allowed) {
        var statement = JpaConditionalStatements.<Person>restrict(scoped(recent(), null),
                (root, query, builder) -> allowed ? builder.conjunction() : builder.disjunction());

        assertThat(repository.findAll(statement)).hasSize(allowed ? 2 : 0);
        assertThat(repository.count(statement)).isEqualTo(allowed ? 2 : 0);
    }

    @Test
    void absentSpecificationPredicatesFailInContentAndInEffectiveCountRatherThanOmittingScope() {
        var source = scoped(recent(), null);
        var absent = JpaConditionalStatements.restrict(source, Specification.<Person>unrestricted());

        assertThatThrownBy(() -> repository.findAll(absent))
                .isInstanceOf(DynamicFilterConfigurationException.class).hasMessageContaining("predicate");
        assertThatThrownBy(() -> repository.count(absent))
                .isInstanceOf(DynamicFilterConfigurationException.class).hasMessageContaining("predicate");
        var absentOnCount = JpaConditionalStatements.<Person>restrict(source, (root, query, builder) ->
                query.getResultType() == Long.class ? null : builder.conjunction());
        assertThatThrownBy(() -> repository.findAll(absentOnCount, PageRequest.of(0, 1, Sort.by("since"))))
                .isInstanceOf(DynamicFilterConfigurationException.class).hasMessageContaining("predicate");
    }

    @Test
    void onlyTheObservablePredicateOfAnOpaqueSpecificationIsValidated() {
        Specification<Person> explicitFalse = (root, query, builder) -> builder.disjunction();
        var composite = Specification.<Person>unrestricted().or(explicitFalse);
        var statement = JpaConditionalStatements.restrict(scoped(recent(), null), composite);

        assertThat(repository.findAll(statement)).isEmpty();
        assertThat(repository.count(statement)).isZero();
    }

    @Test
    void subresourceSpecificationCorrelatesAuthorizationWithItsOwnerInBothContentAndCount() {
        save("alice", RECENT.plusHours(4), HEIGHT, "private");
        entityManager.flush();
        entityManager.clear();
        var source = ConditionalStatement.builder()
                .and(equal("number", "number", String.class, "shared"))
                .restrict(equal("person.height", "ownerHeight", BigDecimal.class, HEIGHT))
                .build();
        var roots = new ArrayList<Root<?>>();
        var queries = new ArrayList<CriteriaQuery<?>>();
        Specification<Phone> ownerScope = (root, query, builder) -> {
            roots.add(root);
            queries.add(query);
            var subquery = query.subquery(Integer.class);
            var owner = subquery.from(Person.class);
            subquery.select(builder.literal(1)).where(
                    builder.equal(owner.get("id"), root.get("person").get("id")),
                    builder.equal(owner.get("name"), "alice"),
                    builder.greaterThanOrEqualTo(owner.get("registerDate"), RECENT));
            return builder.exists(subquery);
        };
        var statement = JpaConditionalStatements.restrict(source, ownerScope);

        var page = phoneRepository.findAll(statement, PageRequest.of(0, 2, Sort.by("person.registerDate", "id")));

        var expectedFirstPage = aliceRecent.getPhones().stream().map(Phone::getId).sorted().toList();
        assertThat(page.getContent()).extracting(Phone::getId).containsExactlyElementsOf(expectedFirstPage);
        assertThat(page.getTotalElements()).isEqualTo(4);
        assertThat(queries).hasSize(2);
        assertThat(queries.getFirst().getResultType()).isEqualTo(Phone.class);
        assertThat(queries.getLast().getResultType()).isEqualTo(Long.class);
        assertThat(roots.getFirst()).isNotSameAs(roots.getLast());
        assertThat(phoneRepository.findAll(statement)).extracting(phone -> phone.getPerson().getId())
                .containsExactlyInAnyOrder(aliceRecent.getId(), aliceRecent.getId(), aliceLater.getId(), aliceLater.getId());
        assertThat(phoneRepository.count(statement)).isEqualTo(4);
    }

    @ParameterizedTest(name = "replaces ordinary filter: {0}")
    @ValueSource(booleans = {false, true})
    void decoratorsCannotEscapeScopeInListingsPagingAggregatesConvertedSpecificationsOrFluentQueries(boolean replacement) {
        FilterDecorator<Specification<?>> decorator = replacement
                ? (filter, wrapper) -> (root, query, builder) -> builder.conjunction()
                : (filter, wrapper) -> filter.or((root, query, builder) -> builder.equal(root.get("name"), "bob"));
        var statement = scoped(recent(), decorator);
        List<Long> expected = replacement
                ? List.of(aliceOld.getId(), aliceRecent.getId(), aliceLater.getId())
                : List.of(aliceRecent.getId(), aliceLater.getId());

        assertThat(repository.findAll(statement)).extracting(Person::getId).containsExactlyInAnyOrderElementsOf(expected);
        assertThat(repository.findAll(statement, Sort.by("since"))).extracting(Person::getId).containsExactlyElementsOf(expected);
        assertThat(repository.count(statement)).isEqualTo(expected.size());
        assertThat(repository.exists(statement)).isTrue();
        var page = repository.findAll(statement, PageRequest.of(0, 1, Sort.by("since")));
        assertThat(page.getContent()).extracting(Person::getId).containsExactly(expected.getFirst());
        assertThat(page.getTotalElements()).isEqualTo(expected.size());

        var converted = repository.convertoToSpecification(statement);
        assertThat(repository.findAll(converted, Sort.by("registerDate"))).extracting(Person::getId).containsExactlyElementsOf(expected);
        assertThat(repository.count(converted)).isEqualTo(expected.size());
        assertThat(repository.exists(converted)).isTrue();

        // FluentQuery uses Spring Data entity paths, not the repository's statement-sort alias translation.
        List<Person> fluent = repository.findBy(statement, query -> query.sortBy(Sort.by("registerDate")).all());
        assertThat(fluent).extracting(Person::getId).containsExactlyElementsOf(expected);
        long count = repository.findBy(statement, query -> query.count());
        boolean exists = repository.findBy(statement, query -> query.exists());
        assertThat(count).isEqualTo(expected.size());
        assertThat(exists).isTrue();
        var fluentPage = repository.findBy(statement, query -> query.page(PageRequest.of(0, 1, Sort.by("registerDate"))));
        assertThat(fluentPage.getContent()).extracting(Person::getId).containsExactly(expected.getFirst());
        assertThat(fluentPage.getTotalElements()).isEqualTo(expected.size());
    }

    @ParameterizedTest
    @EnumSource(EntityGraph.EntityGraphType.class)
    void singleResultAndGraphVariantsRetainEveryRestrictionAndLoadRequestedAssociations(EntityGraph.EntityGraphType graphType) {
        var single = scoped(equal("registerDate", "date", LocalDateTime.class, RECENT), null);
        assertThat(repository.findOne(single)).get().extracting(Person::getId).isEqualTo(aliceRecent.getId());
        entityManager.clear();
        var graphedSingle = repository.findOne(single, graphType, GRAPH);
        assertThat(graphedSingle.getId()).isEqualTo(aliceRecent.getId());
        assertPhonesLoaded(List.of(graphedSingle));
        entityManager.clear();

        var statement = scoped(recent(), null);
        var graphed = repository.findAll(statement, graphType, GRAPH);
        assertThat(graphed).extracting(Person::getId).containsExactlyInAnyOrder(aliceRecent.getId(), aliceLater.getId());
        assertPhonesLoaded(graphed);
        entityManager.clear();
        var sorted = repository.findAll(statement, Sort.by(Sort.Direction.DESC, "since"), graphType, GRAPH);
        assertThat(sorted).extracting(Person::getId).containsExactly(aliceLater.getId(), aliceRecent.getId());
        assertPhonesLoaded(sorted);
        entityManager.clear();
        var page = repository.findAll(statement, PageRequest.of(0, 1, Sort.by("since")), graphType, GRAPH);
        assertThat(page.getContent()).extracting(Person::getId).containsExactly(aliceRecent.getId());
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertPhonesLoaded(page.getContent());
    }

    @ParameterizedTest(name = "only out-of-scope records match at hour offset {0}")
    @ValueSource(ints = {2, 3})
    void ordinaryMatchesOutsideEitherRestrictionCannotBeObservedByResultsOrAggregates(int hourOffset) {
        var ordinary = equal("registerDate", "date", LocalDateTime.class, RECENT.plusHours(hourOffset));
        // One date matches Alice with a different height; the other matches Bob with the allowed height.
        var unrestricted = new ConditionalStatement(new StatementWrapper(ordinary, Map.of(), DECLARED_FILTERS, null), null);
        assertThat(repository.exists(unrestricted)).isTrue();
        var statement = scoped(ordinary, null);

        assertThat(repository.findOne(statement)).isEmpty();
        assertThat(repository.findAll(statement)).isEmpty();
        assertThat(repository.count(statement)).isZero();
        assertThat(repository.exists(statement)).isFalse();
        var page = repository.findAll(statement, PageRequest.of(0, 1, Sort.by("since")));
        assertThat(page.getContent()).isEmpty();
        assertThat(page.getTotalElements()).isZero();
        var converted = repository.convertoToSpecification(statement);
        assertThat(repository.findAll(converted)).isEmpty();
        assertThat(repository.count(converted)).isZero();
        assertThat(repository.exists(converted)).isFalse();
        List<Person> fluent = repository.findBy(statement, query -> query.all());
        long fluentCount = repository.findBy(statement, query -> query.count());
        boolean fluentExists = repository.findBy(statement, query -> query.exists());
        assertThat(fluent).isEmpty();
        assertThat(fluentCount).isZero();
        assertThat(fluentExists).isFalse();
    }

    @Test
    void mandatoryFilterParametersDoNotBecomePublicSortAliases() {
        var statement = scoped(recent(), null);

        assertThat(repository.findAll(statement, Sort.by("since"))).extracting(Person::getId)
                .containsExactly(aliceRecent.getId(), aliceLater.getId());
        assertThatThrownBy(() -> repository.findAll(statement, Sort.by("mandatoryOwner")))
                .isInstanceOf(RuntimeException.class).hasMessageContaining("mandatoryOwner");
        assertThatThrownBy(() -> repository.findAll(statement, PageRequest.of(0, 1, Sort.by("mandatoryHeight")), GRAPH))
                .isInstanceOf(RuntimeException.class).hasMessageContaining("mandatoryHeight");
    }

    @ParameterizedTest(name = "fetching-only ordinary predicate: {0}")
    @ValueSource(booleans = {false, true})
    void fetchingDecoratorPreservesScopedContentAndCountsWithAndWithoutEntityGraphs(boolean fetchingOnly) {
        var statement = scoped(fetchingOnly ? new NoOpStatement() : recent(), fetching());
        List<Long> expected = fetchingOnly
                ? List.of(aliceOld.getId(), aliceRecent.getId(), aliceLater.getId())
                : List.of(aliceRecent.getId(), aliceLater.getId());

        var selected = repository.findAll(statement, Sort.by("since"));
        assertThat(selected).extracting(Person::getId).containsExactlyElementsOf(expected);
        assertPhonesLoaded(selected);
        entityManager.clear();
        var page = repository.findAll(statement, PageRequest.of(0, 1, Sort.by("since")));
        assertThat(page.getContent()).extracting(Person::getId).containsExactly(expected.getFirst());
        assertThat(page.getTotalElements()).isEqualTo(expected.size());
        assertPhonesLoaded(page.getContent());
        assertThat(repository.count(statement)).isEqualTo(expected.size());
        entityManager.clear();

        var graphPage = repository.findAll(statement, PageRequest.of(0, 1, Sort.by("since")), GRAPH);
        assertThat(graphPage.getContent()).extracting(Person::getId).containsExactly(expected.getFirst());
        assertThat(graphPage.getTotalElements()).isEqualTo(expected.size());
        assertPhonesLoaded(graphPage.getContent());
        entityManager.clear();
        var converted = repository.convertoToSpecification(statement);
        var convertedResults = repository.findAll(converted, Sort.by("registerDate"));
        assertThat(convertedResults).extracting(Person::getId).containsExactlyElementsOf(expected);
        assertPhonesLoaded(convertedResults);
        assertThat(repository.count(converted)).isEqualTo(expected.size());
    }

    @Test
    void pluralMandatoryConditionsKeepDistinctScopedResultsCountsAndExistence() {
        var statement = pluralScope(null);
        assertPluralContentAndCount(statement);
        assertThat(repository.exists(statement)).isTrue();
    }

    @Test
    void fetchedPluralMandatoryConditionsKeepDistinctScopedContentAndEffectiveCounts() {
        var statement = pluralScope(fetching());
        assertPhonesLoaded(assertPluralContentAndCount(statement));
    }

    @Test
    void replacementDecoratorCombinedWithFetchingAndEntityGraphCannotUnscopePageTotals() {
        FilterDecorator<Specification<?>> replacement = (filter, wrapper) ->
                (root, query, builder) -> builder.conjunction();
        var decorator = FilterDecorator.of(List.of(replacement, fetching()));
        var statement = scoped(equal("name", "searchOwner", String.class, "bob"), decorator);

        var page = repository.findAll(statement, PageRequest.of(0, 1, Sort.by("since")), GRAPH);
        assertThat(page.getContent()).extracting(Person::getId).containsExactly(aliceOld.getId());
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertPhonesLoaded(page.getContent());
        assertThat(repository.count(statement)).isEqualTo(3);
        entityManager.clear();
        var listed = repository.findAll(statement, Sort.by("since"), GRAPH);
        assertThat(listed).extracting(Person::getId).containsExactly(aliceOld.getId(), aliceRecent.getId(), aliceLater.getId());
        assertPhonesLoaded(listed);
    }

    private static FetchingFilterDecorator fetching() {
        return new FetchingFilterDecorator(List.of(FetchPhones.class.getAnnotation(Fetching.class)));
    }

    private static Specification<Person> sharedPhoneScope() {
        return (root, query, builder) -> {
            var subquery = query.subquery(Integer.class);
            var phone = subquery.from(Phone.class);
            subquery.select(builder.literal(1)).where(
                    builder.equal(phone.get("person").get("id"), root.get("id")),
                    builder.equal(phone.get("number"), "shared"));
            return builder.exists(subquery);
        };
    }

    private ConditionalStatement pluralScope(FilterDecorator<Specification<?>> decorator) {
        save("alice", RECENT.plusHours(4), HEIGHT, "private");
        entityManager.flush();
        entityManager.clear();
        var base = scoped(recent(), decorator);
        return new ConditionalStatement(base.statementWrapper(), base.filterDecorator(),
                List.of(base.restrictions().getFirst(), base.restrictions().getLast(),
                        equal("phones.number", "mandatoryPhone", String.class, "shared")));
    }

    private List<Person> assertPluralContentAndCount(ConditionalStatement statement) {
        // Each matching person has two matching children, but counts and pages must count root identities once.
        assertThat(repository.findAll(statement)).extracting(Person::getId)
                .containsExactlyInAnyOrder(aliceRecent.getId(), aliceLater.getId());
        var page = repository.findAll(statement, PageRequest.of(0, 1, Sort.by("since")));
        assertThat(page.getContent()).extracting(Person::getId).containsExactly(aliceRecent.getId());
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(repository.count(statement)).isEqualTo(2);
        return page.getContent();
    }

    @Fetching("phones")
    private interface FetchPhones {
    }

    private static void assertPhonesLoaded(List<Person> people) {
        assertThat(people).allSatisfy(person -> {
            assertThat(Persistence.getPersistenceUtil().isLoaded(person, "phones")).isTrue();
            assertThat(person.getPhones()).extracting(Phone::getNumber).containsExactly("shared", "shared");
        });
    }

    private static LogicalStatement recent() {
        return new LogicalStatement(FilterData.of("registerDate", new String[]{"since"}, LocalDateTime.class,
                GreaterOrEquals.class, new Object[]{RECENT}));
    }

    private static ConditionalStatement scoped(AbstractStatement ordinary, FilterDecorator<Specification<?>> decorator) {
        return new ConditionalStatement(new StatementWrapper(ordinary, Map.of(), DECLARED_FILTERS, null), decorator,
                List.of(equal("name", "mandatoryOwner", String.class, "alice"),
                        equal("height", "mandatoryHeight", BigDecimal.class, HEIGHT)));
    }

    private static LogicalStatement equal(String path, String parameter, Class<?> targetType, Object value) {
        return new LogicalStatement(FilterData.of(path, new String[]{parameter}, targetType, Equals.class, new Object[]{value}));
    }

    private Person save(String owner, LocalDateTime date, BigDecimal height) {
        return save(owner, date, height, "shared");
    }

    private Person save(String owner, LocalDateTime date, BigDecimal height, String number) {
        try {
            Constructor<Person> constructor = Person.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            Person person = constructor.newInstance();
            ReflectionTestUtils.setField(person, "name", owner);
            ReflectionTestUtils.setField(person, "registerDate", date);
            ReflectionTestUtils.setField(person, "height", height);
            ReflectionTestUtils.setField(person, "phones", new HashSet<Phone>());
            repository.save(person);
            for (int i = 0; i < 2; i++) {
                Phone phone = new Phone();
                ReflectionTestUtils.setField(phone, "number", number);
                ReflectionTestUtils.setField(phone, "person", person);
                entityManager.persist(phone);
                person.getPhones().add(phone);
            }
            return person;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not create person fixture", e);
        }
    }

    private static FilterRequestData declaration(String field) {
        try {
            return FilterRequestData.of(RequestFilters.class.getDeclaredField(field).getAnnotation(Filter.class));
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("Missing request filter declaration", e);
        }
    }

    private static final class RequestFilters {
        @Filter(path = "registerDate", parameters = "since", targetType = LocalDateTime.class, operation = GreaterOrEquals.class)
        private LocalDateTime since;

        @Filter(path = "id", parameters = "registerDate", targetType = Long.class, operation = Equals.class)
        private Long identifier;
    }
}
