package com.runestone.dynafilter.modules.performance;

import com.runestone.dynafilter.core.model.FilterData;
import com.runestone.dynafilter.core.operation.Equals;
import com.runestone.dynafilter.core.operation.IsIn;
import com.runestone.dynafilter.modules.jpa.operation.specification.JpaPredicateUtils;
import com.runestone.dynafilter.modules.jpa.operation.specification.SpecificationIsIn;
import com.runestone.dynafilter.modules.jpa.resolver.Fetching;
import com.runestone.dynafilter.modules.jpa.resolver.FetchingFilterDecorator;
import com.runestone.dynafilter.modules.jpa.tools.app.database.jpamodels.Person;
import com.runestone.dynafilter.modules.jpa.tools.app.database.jpamodels.Produto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3)
@Measurement(iterations = 5)
@Fork(1)
public class DynamicFilterResolverPerf02Benchmark {

    @Benchmark
    public void repeatedNestedPath(JpaPredicateState state, Blackhole blackhole) {
        blackhole.consume(JpaPredicateUtils.computeAttributePath(state.cityFilter, state.personRoot));
        blackhole.consume(JpaPredicateUtils.computeAttributePath(state.cityFilter, state.personRoot));
    }

    @Benchmark
    public void elementCollectionInPredicate(JpaPredicateState state, Blackhole blackhole) {
        blackhole.consume(state.<Produto>cast(state.isIn.createFilter(state.tipoFilter))
                .toPredicate(state.produtoRoot, state.produtoQuery, state.criteriaBuilder));
    }

    @Benchmark
    public void overlappingFetchDecorator(JpaPredicateState state, Blackhole blackhole) {
        blackhole.consume(state.<Person>cast(state.fetchingDecorator.decorate(org.springframework.data.jpa.domain.Specification.unrestricted(), state.statementWrapper))
                .toPredicate(state.personRoot, state.personQuery, state.criteriaBuilder));
    }

    @State(Scope.Benchmark)
    public static class JpaPredicateState {

        EntityManagerFactory entityManagerFactory;
        LocalContainerEntityManagerFactoryBean entityManagerFactoryBean;
        EntityManager entityManager;
        CriteriaBuilder criteriaBuilder;
        CriteriaQuery<Person> personQuery;
        CriteriaQuery<Produto> produtoQuery;
        Root<Person> personRoot;
        Root<Produto> produtoRoot;
        FilterData cityFilter;
        FilterData tipoFilter;
        SpecificationIsIn isIn;
        FetchingFilterDecorator fetchingDecorator;
        com.runestone.dynafilter.core.generator.StatementWrapper statementWrapper;

        @Setup
        public void setUp() {
            entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
            entityManagerFactoryBean.setDataSource(dataSource());
            entityManagerFactoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
            entityManagerFactoryBean.setPackagesToScan(Person.class.getPackageName());
            entityManagerFactoryBean.setJpaPropertyMap(jpaProperties());
            entityManagerFactoryBean.afterPropertiesSet();
            entityManagerFactory = entityManagerFactoryBean.getObject();
            entityManager = entityManagerFactory.createEntityManager();
            criteriaBuilder = entityManager.getCriteriaBuilder();
            personQuery = criteriaBuilder.createQuery(Person.class);
            produtoQuery = criteriaBuilder.createQuery(Produto.class);
            personRoot = personQuery.from(Person.class);
            produtoRoot = produtoQuery.from(Produto.class);
            cityFilter = new FilterData("addresses.location.city", new String[]{"city"}, Object.class, Equals.class,
                    false, new Object[]{"Belem"}, List.of(), "");
            tipoFilter = new FilterData("tipos", new String[]{"tipo"}, Object.class, IsIn.class,
                    false, new Object[]{"ELETRONICO"}, List.of(), "");
            isIn = new SpecificationIsIn(new com.runestone.converters.impl.DefaultDataConversionService(false));
            fetchingDecorator = new FetchingFilterDecorator(List.of(fetching("addresses"), fetching("addresses.location")));
            statementWrapper = new com.runestone.dynafilter.core.generator.StatementWrapper(
                    new com.runestone.dynafilter.core.statement.NoOpStatement(), null, null);
        }

        @TearDown
        public void tearDown() {
            if (entityManager != null) {
                entityManager.close();
            }
            if (entityManagerFactory != null) {
                entityManagerFactory.close();
            }
            if (entityManagerFactoryBean != null) {
                entityManagerFactoryBean.destroy();
            }
        }

        private static DriverManagerDataSource dataSource() {
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName("org.h2.Driver");
            dataSource.setUrl("jdbc:h2:mem:dynafilter-perf;DB_CLOSE_DELAY=-1");
            dataSource.setUsername("sa");
            dataSource.setPassword("");
            return dataSource;
        }

        @SuppressWarnings("unchecked")
        private <T> org.springframework.data.jpa.domain.Specification<T> cast(org.springframework.data.jpa.domain.Specification<?> specification) {
            return (org.springframework.data.jpa.domain.Specification<T>) specification;
        }

        private static Map<String, Object> jpaProperties() {
            return Map.of(
                    "hibernate.hbm2ddl.auto", "create-drop",
                    "hibernate.show_sql", "false"
            );
        }

        private static Fetching fetching(String path) {
            return new Fetching() {
                @Override
                public String path() {
                    return path;
                }

                @Override
                public Class<?> joinType() {
                    return Object.class;
                }

                @Override
                public Class<? extends java.lang.annotation.Annotation> annotationType() {
                    return Fetching.class;
                }
            };
        }
    }
}
