package com.runestone.dynafilter.modules.jpa.resolver;

import com.runestone.dynafilter.core.generator.StatementWrapper;
import com.runestone.dynafilter.core.model.FilterData;
import com.runestone.dynafilter.core.modifier.FilterModifier;
import com.runestone.dynafilter.core.modifier.ModIgnoreCase;
import com.runestone.dynafilter.core.operation.Between;
import com.runestone.dynafilter.core.operation.DefinedFilterOperation;
import com.runestone.dynafilter.core.operation.Equals;
import com.runestone.dynafilter.core.operation.IsIn;
import com.runestone.dynafilter.core.operation.Like;
import com.runestone.dynafilter.core.statement.CompoundStatement;
import com.runestone.dynafilter.core.statement.LogicOperator;
import com.runestone.dynafilter.core.statement.LogicalStatement;
import com.runestone.dynafilter.core.statement.NegatedStatement;
import com.runestone.dynafilter.modules.jpa.repository.DynamicFilterJpaRepositoryImpl;
import com.runestone.dynafilter.modules.jpa.tools.app.database.jpamodels.Address;
import com.runestone.dynafilter.modules.jpa.tools.app.database.jpamodels.Location;
import com.runestone.dynafilter.modules.jpa.tools.app.database.jpamodels.Person;
import com.runestone.dynafilter.modules.jpa.tools.app.database.jpamodels.Phone;
import com.runestone.dynafilter.modules.jpa.tools.app.database.jpamodels.Produto;
import com.runestone.dynafilter.modules.jpa.tools.app.database.jpamodels.TipoProduto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SpecificationDynamicFilterResolverJpaTest {

    private final SpecificationDynamicFilterResolver resolver = new SpecificationDynamicFilterResolver();

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("converts equals statement to an executable Specification")
    void convertsEqualsStatementToSpecification() {
        persistPeople();
        Specification<?> specification = resolver.createFilter(wrapper(filter("name", Equals.class, "Ada Lovelace")), null);

        List<Person> people = findAll(Person.class, specification);

        assertThat(people).singleElement().extracting(Person::getName).isEqualTo("Ada Lovelace");
    }

    @Test
    @DisplayName("resolves dot notation through JPA joins")
    void resolvesDotNotationThroughJoins() {
        persistPeople();
        Specification<?> specification = resolver.createFilter(wrapper(filter("addresses.location.city", Equals.class, "Belem")), null);

        List<Person> people = findAll(Person.class, specification);

        assertThat(people).singleElement().extracting(Person::getName).isEqualTo("Ada Lovelace");
    }

    @Test
    @DisplayName("applies case-insensitive LIKE specification")
    void appliesCaseInsensitiveLikeSpecification() {
        persistPeople();
        Specification<?> specification = resolver.createFilter(wrapper(filter(
                "name",
                Like.class,
                new Object[]{"ada"},
                List.of(ModIgnoreCase.class)
        )), null);

        List<Person> people = findAll(Person.class, specification);

        assertThat(people).singleElement().extracting(Person::getName).isEqualTo("Ada Lovelace");
    }

    @Test
    @DisplayName("applies BETWEEN specification with converted LocalDate values")
    void appliesBetweenSpecificationWithConvertedLocalDateValues() {
        persistPeople();
        Specification<?> specification = resolver.createFilter(wrapper(filter(
                "birthday",
                new String[]{"birthdayFrom", "birthdayTo"},
                Between.class,
                new Object[]{"1800-01-01", "1900-01-01"},
                List.of()
        )), null);

        List<Person> people = findAll(Person.class, specification);

        assertThat(people).singleElement().extracting(Person::getName).isEqualTo("Ada Lovelace");
    }

    @Test
    @DisplayName("combines specifications with conjunction")
    void combinesSpecificationsWithConjunction() {
        persistPeople();
        LogicalStatement nameIsAda = new LogicalStatement(filter("name", Equals.class, "Ada Lovelace"));
        LogicalStatement birthdayInRange = new LogicalStatement(filter(
                "birthday",
                new String[]{"birthdayFrom", "birthdayTo"},
                Between.class,
                new Object[]{LocalDate.of(1800, 1, 1), LocalDate.of(1900, 1, 1)},
                List.of()
        ));
        StatementWrapper statementWrapper = new StatementWrapper(
                new CompoundStatement(nameIsAda, birthdayInRange, LogicOperator.CONJUNCTION),
                null,
                null
        );

        List<Person> people = findAll(Person.class, resolver.createFilter(statementWrapper, null));

        assertThat(people).singleElement().extracting(Person::getName).isEqualTo("Ada Lovelace");
    }

    @Test
    @DisplayName("negates specification predicates")
    void negatesSpecificationPredicates() {
        persistPeople();
        StatementWrapper statementWrapper = new StatementWrapper(
                new NegatedStatement(new LogicalStatement(filter("name", Equals.class, "Ada Lovelace"))),
                null,
                null
        );

        List<Person> people = findAll(Person.class, resolver.createFilter(statementWrapper, null));

        assertThat(people).singleElement().extracting(Person::getName).isEqualTo("Grace Hopper");
    }

    @Test
    @DisplayName("applies IN over element collection with distinct result")
    void appliesInOverElementCollection() {
        entityManager.persist(new Produto("Notebook", EnumSet.of(TipoProduto.ELETRONICO, TipoProduto.SERVICO)));
        entityManager.persist(new Produto("Camisa", EnumSet.of(TipoProduto.VESTUARIO)));
        entityManager.flush();
        entityManager.clear();
        Specification<?> specification = resolver.createFilter(wrapper(filter("tipos", IsIn.class, "ELETRONICO")), null);

        List<Produto> produtos = findAll(Produto.class, specification);

        assertThat(produtos).singleElement().extracting(Produto::getNome).isEqualTo("Notebook");
    }

    @Test
    @DisplayName("repository without injected resolver fails explicitly")
    void repositoryWithoutResolverFailsExplicitly() {
        DynamicFilterJpaRepositoryImpl<Person, Long> repository = new DynamicFilterJpaRepositoryImpl<>(Person.class, entityManager);

        assertThatThrownBy(() -> repository.convertToSpecification(new com.runestone.dynafilter.core.generator.ConditionalStatement(
                wrapper(filter("name", Equals.class, "Ada Lovelace")),
                null
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("DynamicFilterResolver was not injected");
    }

    private void persistPeople() {
        Location belem = new Location("Belem", "PA");
        entityManager.persist(belem);
        Person ada = new Person("Ada Lovelace", new BigDecimal("1.70"), new BigDecimal("62.50"),
                LocalDate.of(1815, 12, 10), LocalDateTime.of(2026, 5, 16, 10, 30));
        ada.addAddress(new Address("Rua das Mangueiras", "42", belem));
        ada.addPhone(new Phone("9133334444"));
        entityManager.persist(ada);
        entityManager.persist(new Person("Grace Hopper", new BigDecimal("1.68"), new BigDecimal("60.00"),
                LocalDate.of(1906, 12, 9), LocalDateTime.of(2026, 5, 16, 11, 0)));
        entityManager.flush();
        entityManager.clear();
    }

    private static StatementWrapper wrapper(FilterData filterData) {
        return new StatementWrapper(new LogicalStatement(filterData), null, null);
    }

    private static FilterData filter(String path, Class<? extends DefinedFilterOperation> operation, Object value) {
        return filter(path, operation, new Object[]{value}, List.of());
    }

    private static FilterData filter(
            String path,
            Class<? extends DefinedFilterOperation> operation,
            Object[] values,
            List<Class<? extends FilterModifier>> modifiers
    ) {
        return filter(path, new String[]{path}, operation, values, modifiers);
    }

    private static FilterData filter(
            String path,
            String[] parameters,
            Class<? extends DefinedFilterOperation> operation,
            Object[] values,
            List<Class<? extends FilterModifier>> modifiers
    ) {
        return new FilterData(path, parameters, Object.class, operation, false, values, modifiers, "");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private <T> List<T> findAll(Class<T> entityType, Specification<?> specification) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = criteriaBuilder.createQuery(entityType);
        Root<T> root = query.from(entityType);
        Predicate predicate = ((Specification) specification).toPredicate(root, query, criteriaBuilder);
        query.select(root).where(predicate);
        return entityManager.createQuery(query).getResultList();
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EntityScan(basePackageClasses = Person.class)
    static class TestApplication {
    }
}
