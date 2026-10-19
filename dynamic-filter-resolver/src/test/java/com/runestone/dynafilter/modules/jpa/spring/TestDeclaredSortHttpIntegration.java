package com.runestone.dynafilter.modules.jpa.spring;

import com.runestone.dynafilter.core.exceptions.DynamicFilterConfigurationException;
import com.runestone.dynafilter.core.exceptions.InvalidSortException;
import com.runestone.dynafilter.core.generator.ConditionalStatement;
import com.runestone.dynafilter.core.generator.annotation.AnnotationStatementGenerator;
import com.runestone.dynafilter.core.generator.annotation.AnnotationStatementInput;
import com.runestone.dynafilter.core.generator.annotation.Conjunction;
import com.runestone.dynafilter.core.generator.annotation.ConjunctionFrom;
import com.runestone.dynafilter.core.generator.annotation.Filter;
import com.runestone.dynafilter.core.generator.annotation.FilterTarget;
import com.runestone.dynafilter.core.generator.annotation.Disjunction;
import com.runestone.dynafilter.core.operation.FilterOperationService;
import com.runestone.dynafilter.core.operation.types.Equals;
import com.runestone.dynafilter.core.operation.types.Between;
import com.runestone.dynafilter.core.operation.types.Decorated;
import com.runestone.dynafilter.core.operation.types.Dynamic;
import com.runestone.dynafilter.core.operation.types.extensions.IsEmptyCollection;
import com.runestone.dynafilter.core.operation.FilterOperation;
import com.runestone.dynafilter.core.operation.FilterOperationMetadata;
import com.runestone.dynafilter.core.model.modifiers.ModIgnorePath;
import com.runestone.dynafilter.modules.jpa.api.JpaFilterOperationContributor;
import com.runestone.dynafilter.modules.jpa.api.JpaSortPolicy;
import com.runestone.dynafilter.modules.jpa.tools.app.database.InMemoryDatabaseApplication;
import com.runestone.dynafilter.modules.jpa.tools.app.database.PersonRepository;
import com.runestone.dynafilter.modules.jpa.tools.app.database.jpamodels.Person;
import com.runestone.dynafilter.modules.openapi.DynaFilterOperationCustomizer;
import io.swagger.v3.oas.models.Operation;
import jakarta.persistence.EntityManager;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.SortHandlerMethodArgumentResolver;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import java.util.stream.Collectors;
import java.util.concurrent.atomic.AtomicInteger;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DataJpaTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@ContextConfiguration(classes = {InMemoryDatabaseApplication.class, TestDeclaredSortHttpIntegration.HttpConfiguration.class})
class TestDeclaredSortHttpIntegration {
    @Autowired
    private PersonRepository repository;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private PeopleController controller;
    @Autowired
    private WebMvcConfigurer webMvcConfigurer;
    @Autowired
    private SortHandlerMethodArgumentResolver sortResolver;
    @Autowired
    private InvalidSortExceptionHandler errorHandler;
    @Autowired
    private DynaFilterOperationCustomizer customizer;
    @Autowired
    private AnnotationStatementGenerator generator;
    @Autowired
    private FilterOperationService<?> operations;

    private MockMvc mvc;

    @BeforeEach
    void setUp() throws Exception {
        repository.deleteAll();
        for (String name : List.of("Zoe", "Bob", "Alice")) {
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
        entityManager.flush();
        entityManager.clear();
        List<HandlerMethodArgumentResolver> resolvers = new ArrayList<>();
        webMvcConfigurer.addArgumentResolvers(resolvers);
        resolvers.add(new PageableHandlerMethodArgumentResolver(sortResolver));
        resolvers.add(sortResolver);
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(errorHandler, new GenericFallback())
                .setCustomArgumentResolvers(resolvers.toArray(HandlerMethodArgumentResolver[]::new)).build();
    }

    @Test
    void springDataConfiguredParserAcceptsDocumentedNamesWithoutFilterValuesAndCountsThePage() throws Exception {
        var operation = new Operation().parameters(new ArrayList<>());
        var handler = new HandlerMethod(controller, PeopleController.class.getDeclaredMethod("people", ConditionalStatement.class, Pageable.class));
        customizer.customize(operation, handler);
        var parameter = operation.getParameters().stream().filter(value -> value.getName().equals("order")).findFirst().orElseThrow();
        assertThat(parameter.getExtensions().get("x-sort-names")).isEqualTo(List.of("name", "query"));
        assertThat(parameter.getExample()).isEqualTo(List.of("name:asc"));
        assertThat(parameter.getDescription()).contains("property:desc", "precedence");
        assertThat(parameter.getSchema().getItems().getEnum()).isNull();

        for (String name : List.of("query", "name")) {
            mvc.perform(get("/people").param("order", name + ":asc").param("size", "1"))
                    .andExpect(status().isOk()).andExpect(content().string("3:Alice"));
        }
        mvc.perform(get("/people").param("order", "query:desc", "name:asc").param("size", "2"))
                .andExpect(status().isOk()).andExpect(content().string("3:Zoe,Bob"));
    }

    @Test
    void invalidSortReturnsSpecificProblemDespiteGenericFallbackAndExecutesNoSql() throws Exception {
        var statistics = entityManager.getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics();
        statistics.clear();

        mvc.perform(get("/people").param("order", "query:asc", "id:desc"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.code").value("INVALID_SORT"))
                .andExpect(jsonPath("$.rejectedNames[0]").value("id"))
                .andExpect(jsonPath("$.allowedNames").doesNotExist());
        assertThat(statistics.getPrepareStatementCount()).isZero();
    }

    @Test
    void openApiAndExecutionShareTheIntervalCompoundAndOmissionCatalog() throws Exception {
        var operation = new Operation().parameters(new ArrayList<>());
        var handler = new HandlerMethod(controller, PeopleController.class.getDeclaredMethod("discovery", ConditionalStatement.class, Pageable.class));
        customizer.customize(operation, handler);
        var order = operation.getParameters().stream().filter(value -> value.getName().equals("order")).findFirst().orElseThrow();
        var names = List.of("dynamicHeight", "height", "heightMax", "heightMin", "id", "name");
        assertThat(order.getExtensions().get("x-sort-names")).isEqualTo(names);
        assertThat(operation.getParameters()).extracting(io.swagger.v3.oas.models.parameters.Parameter::getName)
                .contains("heightMin", "heightMax", "term", "partial", "decorated", "emptyPhones", "special")
                .doesNotContain("privateBirthday", "constantDate");

        for (String name : names) {
            String expected = name.equals("id") ? "3:Zoe,Bob,Alice" : "3:Alice,Bob,Zoe";
            mvc.perform(get("/discovery").param("order", name + ":asc"))
                    .andExpect(status().isOk()).andExpect(content().string(expected));
        }
        for (String bound : List.of("heightMin", "heightMax")) {
            mvc.perform(get("/discovery").param("heightMin", "165").param("heightMax", "185").param("order", bound + ":desc"))
                    .andExpect(status().isOk()).andExpect(content().string("2:Zoe,Bob"));
        }
        mvc.perform(get("/discovery").param("special", "Alice").param("order", "name:asc"))
                .andExpect(status().isOk()).andExpect(content().string("1:Alice"));
        mvc.perform(get("/discovery").param("emptyPhones", "false").param("order", "name:asc"))
                .andExpect(status().isOk()).andExpect(content().string("0:"));
    }

    @Test
    void omittedAndAmbiguousNamesReturnOnlyTheSpecificPublicErrorBeforeSql() throws Exception {
        var statistics = entityManager.getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics();
        statistics.clear();
        for (String name : List.of("term", "partial", "virtual", "special", "phones", "emptyPhones", "weight",
                "decorated", "birthday", "privateBirthday", "registerDate", "constantDate", "dynamicHeightFrom", "dynamicHeightTo")) {
            mvc.perform(get("/discovery").param("order", name + ":asc"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("INVALID_SORT"))
                    .andExpect(jsonPath("$.rejectedNames[0]").value(name))
                    .andExpect(jsonPath("$.allowedNames").doesNotExist())
                    .andExpect(jsonPath("$.reason").doesNotExist());
        }
        assertThat(statistics.getPrepareStatementCount()).isZero();
    }

    @Test
    void configurationErrorsAreNotMappedToClientErrors() throws Exception {
        mvc.perform(get("/configuration-error")).andExpect(status().isInternalServerError());
    }

    @Test
    @SuppressWarnings("unchecked")
    void warmupDetectsAliasVersusCompoundLiteralCollisionsInEitherDeclarationOrder() {
        var beans = new DefaultListableBeanFactory();
        beans.registerSingleton("factory", entityManager.getEntityManagerFactory());
        beans.registerSingleton("policy", JpaSortPolicy.DECLARED);
        beans.registerSingleton("catalog", new JpaSortContext(generator, beans.getBeanProvider(EntityManagerFactory.class)));
        var analyser = new FilterConfigurationAnalyserBeanPostProcessor((FilterOperationService<Specification<?>>) operations, generator);
        analyser.setBeanFactory(beans);

        for (Object candidate : List.of(new CollisionController(), new ReversedCollisionController())) {
            assertThatThrownBy(() -> analyser.postProcessAfterInitialization(candidate, "collision"))
                    .isInstanceOf(DynamicFilterConfigurationException.class).hasMessageContaining("Conflicting sort name 'name'");
        }
    }

    @Test
    void nonMvcCallersCanCatchTheSpecificExceptionThroughTheSpringRepositoryProxy() throws Exception {
        var method = PeopleController.class.getDeclaredMethod("people", ConditionalStatement.class, Pageable.class);
        var input = new AnnotationStatementInput(ConditionalStatement.class, method.getParameters()[0].getAnnotations());
        var statement = new ConditionalStatement(generator.generateStatements(input, java.util.Map.of()), null);
        assertThatThrownBy(() -> repository.findAll(statement, org.springframework.data.domain.Sort.by("id")))
                .isExactlyInstanceOf(InvalidSortException.class);
    }

    @Test
    void openApiReusesEffectiveValidationAndLifecycleCleanupInvalidatesIt() throws Exception {
        var calls = new AtomicInteger();
        var realFactory = entityManager.getEntityManagerFactory();
        var observedFactory = mock(EntityManagerFactory.class, delegatesTo(realFactory));
        doAnswer(invocation -> {
            calls.incrementAndGet();
            return realFactory.getMetamodel();
        }).when(observedFactory).getMetamodel();
        var beans = new DefaultListableBeanFactory();
        beans.registerSingleton("factory", observedFactory);
        var owner = new JpaSortContext(generator, beans.getBeanProvider(EntityManagerFactory.class));
        beans.registerSingleton("catalogOwner", owner);
        beans.registerSingleton("policy", JpaSortPolicy.DECLARED);
        beans.registerSingleton("sortResolver", sortResolver);
        var documentation = new DynaFilterOperationCustomizer(null, operations);
        documentation.setBeanFactory(beans);
        var handler = new HandlerMethod(controller, PeopleController.class.getDeclaredMethod("people", ConditionalStatement.class, Pageable.class));

        documentation.customize(new Operation().parameters(new ArrayList<>()), handler);
        int firstDiscovery = calls.get();
        documentation.customize(new Operation().parameters(new ArrayList<>()), handler);
        // Context matching checks the unit each time, but effective attribute validation is reused.
        assertThat(calls.get() - firstDiscovery).isEqualTo(1);
        owner.clearCache();
        int beforeRediscovery = calls.get();
        documentation.customize(new Operation().parameters(new ArrayList<>()), handler);
        assertThat(calls.get() - beforeRediscovery).isEqualTo(2);
    }

    @FilterTarget(Person.class)
    static class Search {
        @Filter(path = "name", parameters = "query", operation = Equals.class)
        private String query;
    }

    @FilterTarget(Person.class)
    static class DiscoverySearch {
        @Filter(path = "height", parameters = {"heightMin", "heightMax"}, operation = Between.class)
        String height;
        @Filter(path = {"id", "name"}, parameters = "term", operation = NameMatch.class)
        String term;
        @Filter(path = {"virtual", "name"}, parameters = "partial", operation = NameMatch.class, modifiers = ModIgnorePath.class)
        String partial;
        @Filter(path = "birthday", parameters = "privateBirthday", operation = Equals.class, hidden = true)
        String privateBirthday;
        @Filter(path = "registerDate", parameters = "constantDate", operation = Decorated.class, constantValues = "unused")
        String constantDate;
        @Filter(path = "weight", parameters = "decorated", operation = Decorated.class)
        String decorated;
        @Filter(path = "phones", parameters = "emptyPhones", operation = IsEmptyCollection.class)
        String emptyPhones;
        @Filter(path = "virtual", parameters = "special", operation = NameMatch.class, modifiers = ModIgnorePath.class)
        String special;
        @Filter(path = "height", parameters = "dynamicHeight", operation = Dynamic.class)
        String dynamicHeight;
    }

    interface NameMatch extends FilterOperation<Specification<?>> {
    }

    static class CollisionController {
        @GetMapping("/collision")
        void search(@FilterTarget(Person.class)
                    @Conjunction({
                            @Filter(path = {"id", "name"}, parameters = "term", operation = NameMatch.class),
                            @Filter(path = "height", parameters = "name", operation = Equals.class)
                    }) ConditionalStatement statement, Pageable pageable) {
        }
    }

    static class ReversedCollisionController {
        @GetMapping("/collision")
        void search(@FilterTarget(Person.class)
                    @Disjunction({
                            @Filter(path = "height", parameters = "name", operation = Equals.class),
                            @Filter(path = {"name", "id"}, parameters = "term", operation = NameMatch.class)
                    }) ConditionalStatement statement, Pageable pageable) {
        }
    }

    @RestController
    static class PeopleController {
        private final PersonRepository repository;

        PeopleController(PersonRepository repository) {
            this.repository = repository;
        }

        @GetMapping("/people")
        String people(@ConjunctionFrom(Search.class) ConditionalStatement statement, Pageable pageable) {
            var page = repository.findAll(statement, pageable);
            return page.getTotalElements() + ":" + page.stream().map(Person::getName).collect(Collectors.joining(","));
        }

        @GetMapping("/discovery")
        String discovery(@ConjunctionFrom(DiscoverySearch.class) ConditionalStatement statement, Pageable pageable) {
            var page = repository.findAll(statement, pageable);
            return page.getTotalElements() + ":" + page.stream().map(Person::getName).collect(Collectors.joining(","));
        }

        @GetMapping("/configuration-error")
        String configurationError() {
            throw new DynamicFilterConfigurationException("server-only diagnostic");
        }
    }

    @RestControllerAdvice
    @Order(Ordered.LOWEST_PRECEDENCE)
    static class GenericFallback {
        @ExceptionHandler(Exception.class)
        ProblemDetail fallback(Exception exception) {
            return ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Configuration(proxyBeanMethods = false)
    @EnableDynamicFilterServletConfiguration(sortPolicy = JpaSortPolicy.DECLARED)
    static class HttpConfiguration {
        @Bean
        JpaFilterOperationContributor nameMatchContributor() {
            return registry -> registry.register(NameMatch.class, FilterOperationMetadata.stringValue(), context ->
                    (root, query, builder) -> builder.equal(root.get("name"), context.filterData().values()[0]));
        }

        @Bean
        PeopleController peopleController(PersonRepository repository) {
            return new PeopleController(repository);
        }

        @Bean
        SortHandlerMethodArgumentResolver sortResolver() {
            var resolver = new SortHandlerMethodArgumentResolver();
            resolver.setSortParameter("order");
            resolver.setPropertyDelimiter(":");
            return resolver;
        }

        @Bean
        DynaFilterOperationCustomizer filterCustomizer(FilterOperationService<?> operations) {
            // This is the existing constructor used by external frameworks.
            return new DynaFilterOperationCustomizer(null, operations);
        }
    }
}
