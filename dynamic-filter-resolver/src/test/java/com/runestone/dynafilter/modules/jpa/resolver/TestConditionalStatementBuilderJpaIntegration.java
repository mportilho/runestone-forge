package com.runestone.dynafilter.modules.jpa.resolver;

import com.runestone.converters.impl.runtime.DefaultRuntimeDataConversionService;
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
import com.runestone.dynafilter.core.operation.FilterOperation;
import com.runestone.dynafilter.core.operation.types.Equals;
import com.runestone.dynafilter.core.operation.types.GreaterOrEquals;
import com.runestone.dynafilter.core.operation.types.LessOrEquals;
import com.runestone.dynafilter.core.resolver.DynamicFilterResolver;
import com.runestone.dynafilter.core.resolver.FilterDecorator;
import com.runestone.dynafilter.modules.jpa.api.JpaFilterOperationService;
import com.runestone.dynafilter.modules.jpa.tools.app.database.InMemoryDatabaseApplication;
import com.runestone.dynafilter.modules.jpa.tools.app.database.PersonRepository;
import com.runestone.dynafilter.modules.jpa.tools.app.database.jpamodels.Person;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Constructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ContextConfiguration(classes = InMemoryDatabaseApplication.class)
final class TestConditionalStatementBuilderJpaIntegration {

    private static final LocalDateTime SINCE = LocalDateTime.of(2026, 10, 12, 12, 0);
    private static final LocalDateTime UNTIL = SINCE.plusDays(2);
    private static final LocalDateTime RECENT = SINCE.plusDays(1);
    private static final LocalDateTime OLD = SINCE.minusDays(10);

    @Autowired
    private PersonRepository repository;
    @Autowired
    private EntityManager entityManager;

    private Person aliceOld;
    private Person aliceRecent;
    private Person aliceLater;
    private Person aliceFuture;
    private Person bobRecent;

    @BeforeEach
    @SuppressWarnings({"unchecked", "rawtypes"})
    void setUp() {
        repository.setDynamicFilterResolver((DynamicFilterResolver) new SpecificationDynamicFilterResolver(
                new JpaFilterOperationService(DefaultRuntimeDataConversionService.standard())));
        repository.deleteAll();
        aliceOld = save("alice", OLD);
        aliceRecent = save("alice", RECENT);
        aliceLater = save("alice", RECENT.plusHours(1));
        aliceFuture = save("alice", UNTIL.plusDays(1));
        save("bob", OLD);
        bobRecent = save("bob", RECENT);
        save("bob", UNTIL.plusDays(1));
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void selectsTheRequestedPeriodAndOwnerThroughExistingRepositoryEntrypoints() {
        var statement = ConditionalStatement.builder().and(period()).restrict(owner("alice")).build();

        assertThat(repository.findAll(statement)).extracting(Person::getId)
                .containsExactlyInAnyOrder(aliceRecent.getId(), aliceLater.getId());
        var page = repository.findAll(statement, PageRequest.of(0, 1, Sort.by("registerDate")));
        assertThat(page.getContent()).extracting(Person::getId).containsExactly(aliceRecent.getId());
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(repository.count(statement)).isEqualTo(2);
        assertThat(repository.exists(statement)).isTrue();
        assertThat(repository.findAll(repository.convertoToSpecification(statement))).extracting(Person::getId)
                .containsExactlyInAnyOrder(aliceRecent.getId(), aliceLater.getId());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void ordinaryOrAndDecoratorsCannotEscapeScopeRegardlessOfRestrictionPosition(boolean restrictFirst) {
        var builder = ConditionalStatement.builder();
        if (restrictFirst) {
            builder.restrict(owner("alice"));
        }
        FilterDecorator<Specification<?>> broaden = (filter, wrapper) -> filter.or(
                (root, query, criteria) -> criteria.equal(root.get("name"), "bob"));
        FilterDecorator<Specification<?>> requestedPeriod = (filter, wrapper) -> filter.and((root, query, criteria) -> {
            LocalDateTime since = wrapper.findDecoratedFilterByPath("applicationSince").orElseThrow().findOneValue();
            LocalDateTime until = wrapper.findDecoratedFilterByPath("applicationUntil").orElseThrow().findOneValue();
            return criteria.between(root.get("registerDate"), since, until);
        });
        builder.and(owner("bob")).or(period()).decorate(broaden)
                .decoratedFilter(filter("applicationSince", "sinceInput", LocalDateTime.class, Equals.class, SINCE))
                .decorate(requestedPeriod)
                .decoratedFilter(filter("applicationUntil", "untilInput", LocalDateTime.class, Equals.class, UNTIL));
        if (!restrictFirst) {
            builder.restrict(owner("alice"));
        }

        assertThat(repository.findAll(builder.build())).extracting(Person::getId)
                .containsExactlyInAnyOrder(aliceRecent.getId(), aliceLater.getId());
    }

    @Test
    void rebuildingARestrictedStatementRetainsItsOwnerAndAddsASecondMandatoryConditionAfterReplacement() {
        var source = ConditionalStatement.builder().and(period()).restrict(owner("alice")).build();
        FilterDecorator<Specification<?>> replacement = (filter, wrapper) -> (root, query, criteria) -> criteria.conjunction();
        var rebuilt = ConditionalStatement.builder(source).or(owner("bob")).decorate(replacement)
                .restrict(filter("registerDate", "mandatoryDate", LocalDateTime.class, Equals.class, RECENT)).build();

        assertThat(repository.findAll(rebuilt)).extracting(Person::getId).containsExactly(aliceRecent.getId());
        assertThat(repository.findOne(rebuilt)).get().extracting(Person::getId).isEqualTo(aliceRecent.getId());
        assertThat(repository.findAll(source)).extracting(Person::getId)
                .containsExactlyInAnyOrder(aliceRecent.getId(), aliceLater.getId());
        assertThat(repository.findAll(ConditionalStatement.builder(source).and(owner("bob")).build())).isEmpty();
    }

    @Test
    void emptyAndNativeNoOpOrdinaryOperandsRemainOmittedRatherThanTrueAlternatives() {
        assertThat(repository.findAll(ConditionalStatement.builder().build())).hasSize(7);
        var scoped = ConditionalStatement.builder().restrict(owner("alice")).build();
        assertThat(repository.findAll(scoped)).extracting(Person::getId)
                .containsExactlyInAnyOrder(aliceOld.getId(), aliceRecent.getId(), aliceLater.getId(), aliceFuture.getId());
        var omitted = new CompoundStatement(new NoOpStatement(), new NegatedStatement(new NoOpStatement()), LogicOperator.DISJUNCTION);
        var statement = ConditionalStatement.builder().or(omitted).or(owner("alice")).and(new NoOpStatement()).build();
        assertThat(repository.findAll(statement)).extracting(Person::getId)
                .containsExactlyInAnyOrder(aliceOld.getId(), aliceRecent.getId(), aliceLater.getId(), aliceFuture.getId());
        assertThat(repository.findAll(ConditionalStatement.builder(scoped).or(new NoOpStatement()).build()))
                .extracting(Person::getId).containsExactlyInAnyOrder(aliceOld.getId(), aliceRecent.getId(), aliceLater.getId(), aliceFuture.getId());
    }

    @Test
    void ordinaryChainsAreLeftAssociativeAndExplicitExpressionsAndFilterNegationAreHonored() {
        var date = filter("registerDate", "date", LocalDateTime.class, Equals.class, RECENT);
        var chained = ConditionalStatement.builder().and(owner("alice")).or(owner("bob")).and(date).build();
        assertThat(repository.findAll(chained)).extracting(Person::getId).containsExactlyInAnyOrder(aliceRecent.getId(), bobRecent.getId());

        var grouped = new CompoundStatement(new LogicalStatement(owner("bob")), new LogicalStatement(date), LogicOperator.CONJUNCTION);
        var explicit = ConditionalStatement.builder().and(owner("alice")).or(grouped).build();
        assertThat(repository.findAll(explicit)).extracting(Person::getId)
                .containsExactlyInAnyOrder(aliceOld.getId(), aliceRecent.getId(), aliceLater.getId(), aliceFuture.getId(), bobRecent.getId());

        var notBob = new FilterData(new String[]{"name"}, new String[]{"notOwner"}, String.class, Equals.class,
                true, new Object[]{"bob"}, null, null);
        assertThat(repository.findAll(ConditionalStatement.builder().or(notBob).build())).extracting(Person::getId)
                .containsExactlyInAnyOrder(aliceOld.getId(), aliceRecent.getId(), aliceLater.getId(), aliceFuture.getId());
        assertThat(repository.findAll(ConditionalStatement.builder().restrict(notBob).and(period()).build())).extracting(Person::getId)
                .containsExactlyInAnyOrder(aliceRecent.getId(), aliceLater.getId());
    }

    @ParameterizedTest(name = "undeclared sort alias: {0}")
    @ValueSource(strings = {"applicationDate", "applicationOwner", "decorationParameter"})
    void preservesDeclaredSortAliasesWithoutRegisteringProgrammaticParameterNames(String undeclared) {
        var alias = new FilterRequestData(new String[]{"registerDate"}, new String[]{"declaredDate"}, LocalDateTime.class,
                GreaterOrEquals.class, "false", null, null, null, false, false, List.of(), null);
        var source = new ConditionalStatement(new StatementWrapper(new NoOpStatement(), Map.of(), List.of(alias)), null);
        var statement = ConditionalStatement.builder(source)
                .and(filter("registerDate", "applicationDate", LocalDateTime.class, GreaterOrEquals.class, SINCE))
                .restrict(owner("alice"))
                .decoratedFilter(filter("applicationData", "decorationParameter", String.class, Equals.class, "data")).build();

        assertThat(repository.findAll(statement, Sort.by(Sort.Direction.DESC, "declaredDate"))).extracting(Person::getId)
                .containsExactly(aliceFuture.getId(), aliceLater.getId(), aliceRecent.getId());
        assertThatThrownBy(() -> repository.findAll(statement, Sort.by(undeclared)))
                .isInstanceOf(PropertyReferenceException.class).hasMessageContaining(undeclared);
    }

    private static AbstractStatement period() {
        return new CompoundStatement(new LogicalStatement(filter("registerDate", "since", LocalDateTime.class,
                GreaterOrEquals.class, SINCE.toString())), new LogicalStatement(filter("registerDate", "until", LocalDateTime.class,
                LessOrEquals.class, UNTIL.toString())), LogicOperator.CONJUNCTION);
    }

    private static FilterData owner(String owner) {
        return filter("name", "applicationOwner", String.class, Equals.class, owner);
    }

    private static FilterData filter(String path, String parameter, Class<?> type,
                                     @SuppressWarnings("rawtypes") Class<? extends FilterOperation> operation, Object value) {
        return FilterData.of(path, new String[]{parameter}, type, operation, new Object[]{value});
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
