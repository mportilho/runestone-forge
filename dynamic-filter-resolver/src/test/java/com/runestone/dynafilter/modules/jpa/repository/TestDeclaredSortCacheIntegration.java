package com.runestone.dynafilter.modules.jpa.repository;

import com.runestone.dynafilter.core.exceptions.InvalidSortException;
import com.runestone.dynafilter.core.generator.ConditionalStatement;
import com.runestone.dynafilter.core.generator.SortContract;
import com.runestone.dynafilter.core.generator.StatementWrapper;
import com.runestone.dynafilter.core.model.statement.NoOpStatement;
import com.runestone.dynafilter.modules.jpa.api.JpaSortPolicy;
import com.runestone.dynafilter.modules.jpa.tools.app.database.jpamodels.FilterExtensionRecord;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

class TestDeclaredSortCacheIntegration {

    @Test
    void publicRepositoryReusesEffectiveValidationAndDoesNotShareItAcrossPersistenceUnits() {
        try (var firstFactory = factory("first"); var secondFactory = factory("second");
             var firstEm = firstFactory.createEntityManager(); var secondEm = secondFactory.createEntityManager()) {
            var first = observed(firstEm);
            var second = observed(secondEm);
            var statement = statement("alias");

            reject(first, statement);
            reject(first, statement);
            assertThat(first.metamodelLookups()).hasValue(1);
            assertThat(second.metamodelLookups()).hasValue(0);
            reject(second, statement);
            assertThat(second.metamodelLookups()).hasValue(1);
            reject(first, statement);
            assertThat(first.metamodelLookups()).hasValue(1);
        }
    }

    @Test
    void effectiveValidationIsBoundedAsObservedByRediscoveryAfterCapacityIsExceeded() {
        try (var factory = factory("bounded"); var em = factory.createEntityManager()) {
            var observed = observed(em);
            var statements = new ArrayList<ConditionalStatement>();
            for (int i = 0; i < 4097; i++) {
                var statement = statement("alias" + i);
                statements.add(statement);
                reject(observed, statement);
            }
            assertThat(observed.metamodelLookups()).hasValue(4097);
            for (var statement : statements.reversed()) {
                reject(observed, statement);
            }
            // An unbounded cache would perform no further metamodel discovery on the second pass.
            assertThat(observed.metamodelLookups().get()).isGreaterThan(4097);
        }
    }

    private static void reject(ObservedRepository observed, ConditionalStatement statement) {
        // Catalog validation runs at the public boundary even if a later order is rejected, without any Criteria mocks.
        assertThatThrownBy(() -> observed.repository().findAll(statement, Sort.by("name", "notPublished")))
                .isInstanceOfSatisfying(InvalidSortException.class,
                        exception -> assertThat(exception.rejectedNames()).containsExactly("notPublished"));
    }

    private static ConditionalStatement statement(String alias) {
        var contract = new SortContract(List.of(new SortContract.Candidate("name", List.of(alias))));
        return new ConditionalStatement(new StatementWrapper(new NoOpStatement(), Map.of(), List.of(), contract), null);
    }

    private static ObservedRepository observed(EntityManager realEm) {
        var lookups = new AtomicInteger();
        var realFactory = realEm.getEntityManagerFactory();
        var factory = mock(EntityManagerFactory.class, delegatesTo(realFactory));
        doAnswer(invocation -> {
            lookups.incrementAndGet();
            return realFactory.getMetamodel();
        }).when(factory).getMetamodel();
        var em = mock(EntityManager.class, delegatesTo(realEm));
        doReturn(factory).when(em).getEntityManagerFactory();
        var repository = new DynamicFilterJpaRepositoryImpl<FilterExtensionRecord, Long>(
                JpaEntityInformationSupport.getEntityInformation(FilterExtensionRecord.class, realEm), em);
        repository.setSortPolicy(JpaSortPolicy.DECLARED);
        return new ObservedRepository(repository, lookups);
    }

    private static org.hibernate.SessionFactory factory(String name) {
        return new Configuration().addAnnotatedClass(FilterExtensionRecord.class)
                .setProperty("hibernate.connection.url", "jdbc:h2:mem:sort-cache-" + name)
                .setProperty("hibernate.hbm2ddl.auto", "create-drop")
                .setProperty("hibernate.show_sql", "false")
                .buildSessionFactory();
    }

    private record ObservedRepository(DynamicFilterJpaRepositoryImpl<FilterExtensionRecord, Long> repository,
                                      AtomicInteger metamodelLookups) {
    }
}
