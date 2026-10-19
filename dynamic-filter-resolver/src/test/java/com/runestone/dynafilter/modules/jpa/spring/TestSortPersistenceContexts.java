package com.runestone.dynafilter.modules.jpa.spring;

import com.runestone.converters.impl.runtime.DefaultRuntimeDataConversionService;
import com.runestone.dynafilter.core.exceptions.DynamicFilterConfigurationException;
import com.runestone.dynafilter.core.exceptions.InvalidSortException;
import com.runestone.dynafilter.core.generator.ConditionalStatement;
import com.runestone.dynafilter.core.generator.annotation.AnnotationStatementGenerator;
import com.runestone.dynafilter.core.generator.annotation.AnnotationStatementInput;
import com.runestone.dynafilter.core.generator.annotation.ConjunctionFrom;
import com.runestone.dynafilter.core.generator.annotation.Filter;
import com.runestone.dynafilter.core.generator.annotation.FilterTarget;
import com.runestone.dynafilter.core.operation.types.Equals;
import com.runestone.dynafilter.modules.jpa.api.JpaFilterOperationService;
import com.runestone.dynafilter.modules.jpa.api.JpaSortPolicy;
import com.runestone.dynafilter.modules.jpa.repository.DynamicFilterJpaRepositoryImpl;
import com.runestone.dynafilter.modules.jpa.resolver.SpecificationDynamicFilterResolver;
import com.runestone.dynafilter.modules.openapi.DynaFilterOperationCustomizer;
import io.swagger.v3.oas.models.Operation;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Id;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport;
import org.springframework.data.web.SortHandlerMethodArgumentResolver;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.persistenceunit.PersistenceManagedTypes;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.web.method.HandlerMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TestSortPersistenceContexts {
    private final AnnotationStatementGenerator generator = new AnnotationStatementGenerator();
    private final JpaFilterOperationService operations = new JpaFilterOperationService(DefaultRuntimeDataConversionService.standard());

    @Test
    void closingAndReplacingAUnitRevalidatesTheSameContractAgainstItsNewMapping() throws Exception {
        var beans = new DefaultListableBeanFactory();
        var owner = new JpaSortContext(generator, beans.getBeanProvider(EntityManagerFactory.class));
        var documentation = documentation(beans, owner);
        var handler = handler();
        try (var first = factory(false)) {
            beans.registerSingleton("unit", first);
            assertThat(names(documentation, handler)).containsExactly("name", "query");
            assertRepository(first, true);
        }
        assertThatThrownBy(() -> names(documentation, handler)).isInstanceOf(DynamicFilterConfigurationException.class);
        beans.destroySingleton("unit");
        try (var replacement = factory(true)) {
            beans.registerSingleton("unit", replacement);
            assertThat(names(documentation, handler)).isEmpty();
            assertRepository(replacement, false);
        } finally {
            owner.clearCache();
        }
    }

    @Test
    void multipleUnitsWithTheSameEntityAreConfigurationErrorsEvenWhenOneIsPrimary() throws Exception {
        try (var first = factory(false); var second = factory(true)) {
            var beans = new DefaultListableBeanFactory();
            var primary = new RootBeanDefinition(EntityManagerFactory.class, () -> first);
            primary.setPrimary(true);
            beans.registerBeanDefinition("first", primary);
            beans.registerSingleton("first", first);
            beans.registerSingleton("second", second);
            var owner = new JpaSortContext(generator, beans.getBeanProvider(EntityManagerFactory.class));
            var documentation = documentation(beans, owner);
            assertThatThrownBy(() -> names(documentation, handler()))
                    .isInstanceOf(DynamicFilterConfigurationException.class).isNotInstanceOf(InvalidSortException.class);
        }
    }

    @Test
    void missingRootOrUnitFailsAsConfigurationInDocumentationAndWarmup() throws Exception {
        var beans = new DefaultListableBeanFactory();
        var owner = new JpaSortContext(generator, beans.getBeanProvider(EntityManagerFactory.class));
        var documentation = documentation(beans, owner);
        var analyser = new FilterConfigurationAnalyserBeanPostProcessor(operations, generator);
        analyser.setBeanFactory(beans);
        for (Object endpoint : List.of(new Endpoint(), new NoRootEndpoint())) {
            var method = endpoint.getClass().getDeclaredMethod("search", ConditionalStatement.class, Pageable.class);
            assertThatThrownBy(() -> names(documentation, new HandlerMethod(endpoint, method)))
                    .isInstanceOf(DynamicFilterConfigurationException.class).isNotInstanceOf(InvalidSortException.class);
            assertThatThrownBy(() -> analyser.postProcessAfterInitialization(endpoint, "invalid"))
                    .isInstanceOf(DynamicFilterConfigurationException.class).isNotInstanceOf(InvalidSortException.class);
        }
    }

    @Test
    void invalidExplicitBindingDoesNotFallBackToAnOtherwiseUniqueUnit() throws Exception {
        try (var registered = factory(false); var unregistered = factory(true)) {
            var beans = new DefaultListableBeanFactory();
            beans.registerSingleton("registered", registered);
            var owner = new JpaSortContext(generator, beans.getBeanProvider(EntityManagerFactory.class), (parameter, entity) -> unregistered);
            assertThatThrownBy(() -> names(documentation(beans, owner), handler()))
                    .isInstanceOf(DynamicFilterConfigurationException.class);
        }
    }

    @Test
    void explicitAdapterBindingKeepsCatalogsIsolatedAndIsAppliedByServletConfiguration() throws Exception {
        try (var first = factory(false); var second = factory(true);
             var context = new AnnotationConfigApplicationContext()) {
            context.registerBean("first", EntityManagerFactory.class, () -> first);
            context.registerBean("second", EntityManagerFactory.class, () -> second);
            context.registerBean(JpaSortPersistenceUnitResolver.class, () -> (parameter, entity) -> {
                var qualifier = parameter.getAnnotation(Qualifier.class);
                return qualifier.value().equals("left") ? first : second;
            });
            context.register(ServletConfiguration.class);
            context.registerBean(BoundEndpoint.class);
            context.refresh();
            var beans = context.getBeanFactory();
            var documentation = new DynaFilterOperationCustomizer(null, context.getBean(JpaFilterOperationService.class));
            documentation.setBeanFactory(beans);
            beans.registerSingleton("sort", new SortHandlerMethodArgumentResolver());
            var handler = new HandlerMethod(new Endpoint(), Endpoint.class.getDeclaredMethod("paired",
                    ConditionalStatement.class, Pageable.class, ConditionalStatement.class, Pageable.class));
            var operation = documentation.customize(new Operation().parameters(new ArrayList<>()), handler);
            assertThat(operation.getParameters().stream().filter(p -> p.getName().equals("left_sort"))
                    .findFirst().orElseThrow().getExtensions().get("x-sort-names")).isEqualTo(List.of("name", "query"));
            assertThat(operation.getParameters().stream().filter(p -> p.getName().equals("right_sort"))
                    .findFirst().orElseThrow().getExtensions().get("x-sort-names")).isEqualTo(List.of());
            assertRepository(first, true);
            assertRepository(second, false);
        }
    }

    private DynaFilterOperationCustomizer documentation(DefaultListableBeanFactory beans, JpaSortContext owner) {
        beans.registerSingleton("owner", owner);
        beans.registerSingleton("policy", JpaSortPolicy.DECLARED);
        beans.registerSingleton("sort", new SortHandlerMethodArgumentResolver());
        var customizer = new DynaFilterOperationCustomizer(null, operations);
        customizer.setBeanFactory(beans);
        return customizer;
    }

    private HandlerMethod handler() throws NoSuchMethodException {
        return new HandlerMethod(new Endpoint(), Endpoint.class.getDeclaredMethod("search", ConditionalStatement.class, Pageable.class));
    }

    @SuppressWarnings("unchecked")
    private List<String> names(DynaFilterOperationCustomizer documentation, HandlerMethod handler) {
        var operation = documentation.customize(new Operation().parameters(new ArrayList<>()), handler);
        return (List<String>) operation.getParameters().stream().filter(p -> p.getName().equals("sort"))
                .findFirst().orElseThrow().getExtensions().get("x-sort-names");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void assertRepository(EntityManagerFactory factory, boolean nameIsPersistent) throws NoSuchMethodException {
        try (EntityManager em = factory.createEntityManager()) {
            var repository = new DynamicFilterJpaRepositoryImpl<UnitRecord, Long>(
                    JpaEntityInformationSupport.getEntityInformation(UnitRecord.class, em), em);
            repository.setDynamicFilterResolver((com.runestone.dynafilter.core.resolver.DynamicFilterResolver)
                    new SpecificationDynamicFilterResolver(operations));
            em.getTransaction().begin();
            em.persist(new UnitRecord(1L, "Zoe"));
            em.persist(new UnitRecord(2L, "Alice"));
            em.getTransaction().commit();
            var statement = new ConditionalStatement(generator.generateStatements(
                    new AnnotationStatementInput(ConditionalStatement.class, handler().getMethod().getParameters()[0].getAnnotations()), Map.of()), null);
            if (nameIsPersistent) {
                assertThat(repository.findAll(statement, Sort.by("query"))).extracting(record -> record.id).containsExactly(2L, 1L);
            } else {
                assertThatThrownBy(() -> repository.findAll(statement, Sort.by("query"))).isExactlyInstanceOf(InvalidSortException.class);
                assertThat(repository.findAll(statement, Sort.unsorted())).hasSize(2);
            }
        }
    }

    private EntityManagerFactory factory(boolean omitName) {
        var bean = new LocalContainerEntityManagerFactoryBean();
        bean.setPersistenceUnitName("unit-" + UUID.randomUUID());
        bean.setDataSource(new DriverManagerDataSource("jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1", "sa", ""));
        bean.setManagedTypes(PersistenceManagedTypes.of(UnitRecord.class.getName()));
        bean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        bean.setJpaPropertyMap(Map.of("hibernate.hbm2ddl.auto", "create-drop"));
        if (omitName) {
            bean.setMappingResources("sort-unit-without-name.xml");
        }
        bean.afterPropertiesSet();
        return bean.getObject();
    }

    @Entity(name = "SortUnitRecord")
    public static class UnitRecord {
        @Id
        private Long id;
        private String name;

        protected UnitRecord() {
        }

        UnitRecord(Long id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    @FilterTarget(UnitRecord.class)
    static class Search {
        @Filter(path = "name", parameters = "query", operation = Equals.class)
        private String query;
    }

    static class Endpoint {
        public void search(@ConjunctionFrom(Search.class) ConditionalStatement statement, Pageable pageable) {
        }

        public void paired(@Qualifier("left") @ConjunctionFrom(Search.class) ConditionalStatement left, @Qualifier("left") Pageable leftPage,
                           @Qualifier("right") @ConjunctionFrom(Search.class) ConditionalStatement right, @Qualifier("right") Pageable rightPage) {
        }
    }

    static class NoRootEndpoint {
        public void search(ConditionalStatement statement, Pageable pageable) {
        }
    }

    @org.springframework.web.bind.annotation.RestController
    static class BoundEndpoint {
        @org.springframework.web.bind.annotation.GetMapping("/bound")
        public void paired(@Qualifier("left") @ConjunctionFrom(Search.class) ConditionalStatement left, @Qualifier("left") Pageable leftPage,
                           @Qualifier("right") @ConjunctionFrom(Search.class) ConditionalStatement right, @Qualifier("right") Pageable rightPage) {
        }
    }

    @Configuration(proxyBeanMethods = false)
    @EnableDynamicFilterServletConfiguration
    static class ServletConfiguration {
    }
}
