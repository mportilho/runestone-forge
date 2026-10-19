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
import org.springframework.beans.factory.annotation.Qualifier;
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
    @Autowired
    private FilterConfigurationAnalyserBeanPostProcessor analyser;
    @Autowired
    private org.springframework.beans.factory.config.AutowireCapableBeanFactory beanFactory;

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
    void qualifiedPageablesPublishOnlyTheirAssociatedCatalogAndUseConfiguredParser() throws Exception {
        var handler = new HandlerMethod(controller, PeopleController.class.getDeclaredMethod("paired",
                ConditionalStatement.class, Pageable.class, ConditionalStatement.class, Pageable.class));
        var operation = customizer.customize(new Operation().parameters(new ArrayList<>()), handler);
        var left = operation.getParameters().stream().filter(p -> p.getName().equals("left-order")).findFirst().orElseThrow();
        var right = operation.getParameters().stream().filter(p -> p.getName().equals("right-order")).findFirst().orElseThrow();
        assertThat(left.getExtensions().get("x-sort-names")).isEqualTo(List.of("name", "query"));
        assertThat(right.getExtensions().get("x-sort-names")).isEqualTo(List.of("id", "identifier"));
        assertThat(right.getExample()).isEqualTo(List.of("id:asc"));
        assertThat(right.getStyle()).isEqualTo(io.swagger.v3.oas.models.parameters.Parameter.StyleEnum.FORM);
        assertThat(right.getExplode()).isTrue();

        mvc.perform(get("/paired").param("left-order", "query:asc", "name:desc")
                        .param("right-order", "identifier:desc", "id:asc").param("left_size", "2").param("right_size", "2"))
                .andExpect(status().isOk()).andExpect(content().string("Alice,Bob|Alice,Bob"));
        mvc.perform(get("/paired").param("left-order", "identifier:asc"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.rejectedNames[0]").value("identifier"));
        mvc.perform(get("/paired").param("right-order", "query:asc"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.rejectedNames[0]").value("query"));
    }

    @Test
    void documentationUsesThePageableParserSelectedByMvcInsteadOfAnUnrelatedSortBean() throws Exception {
        var nestedSort = new SortHandlerMethodArgumentResolver();
        nestedSort.setSortParameter("nested");
        nestedSort.setPropertyDelimiter(";");
        var pageable = new PageableHandlerMethodArgumentResolver(nestedSort);
        var adapter = new org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter();
        adapter.setArgumentResolvers(List.of(pageable, sortResolver));
        var beans = new DefaultListableBeanFactory();
        beans.registerSingleton("adapter", adapter);
        beans.registerSingleton("unrelatedSort", sortResolver);
        beans.registerSingleton("policy", JpaSortPolicy.DECLARED);
        beans.registerSingleton("context", new JpaSortContext(generator, factoryProvider()));
        var documentation = new DynaFilterOperationCustomizer(null, operations);
        documentation.setBeanFactory(beans);
        var handler = new HandlerMethod(controller, PeopleController.class.getDeclaredMethod("people", ConditionalStatement.class, Pageable.class));
        var operation = documentation.customize(new Operation().parameters(new ArrayList<>()), handler);
        var parameter = operation.getParameters().stream().filter(p -> p.getName().equals("nested")).findFirst().orElseThrow();
        assertThat(parameter.getExample()).isEqualTo(List.of("name;asc"));
        assertThat(operation.getParameters()).noneMatch(p -> p.getName().equals("order"));

        List<HandlerMethodArgumentResolver> filters = new ArrayList<>();
        webMvcConfigurer.addArgumentResolvers(filters);
        filters.add(pageable);
        var configuredMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(filters.toArray(HandlerMethodArgumentResolver[]::new)).build();
        configuredMvc.perform(get("/people").param("nested", "query;desc", "name;asc").param("size", "2"))
                .andExpect(status().isOk()).andExpect(content().string("3:Zoe,Bob"));
    }

    private org.springframework.beans.factory.ObjectProvider<EntityManagerFactory> factoryProvider() {
        var beans = new DefaultListableBeanFactory();
        beans.registerSingleton("factory", entityManager.getEntityManagerFactory());
        return beans.getBeanProvider(EntityManagerFactory.class);
    }

    @Test
    void unresolvedAssociationsFailBothDocumentationAndWarmup() throws Exception {
        for (Object endpoint : List.of(new MissingQualifier(), new DuplicateQualifier(), new MissingMatch(), new DuplicateOrdering())) {
            var handler = new HandlerMethod(endpoint, endpoint.getClass().getDeclaredMethods()[0]);
            assertThatThrownBy(() -> customizer.customize(new Operation().parameters(new ArrayList<>()), handler))
                    .isInstanceOf(DynamicFilterConfigurationException.class).hasMessageContaining("@Qualifier");
            assertThatThrownBy(() -> analyser.postProcessAfterInitialization(endpoint, "invalid"))
                    .isInstanceOf(DynamicFilterConfigurationException.class).hasMessageContaining("@Qualifier");
        }
    }

    @Test
    void aContractWithoutAssociatedOrderingNeedsNoSortContext() throws Exception {
        var endpoint = new PartiallyAssociated();
        var handler = new HandlerMethod(endpoint, PartiallyAssociated.class.getDeclaredMethods()[0]);
        var operation = customizer.customize(new Operation().parameters(new ArrayList<>()), handler);
        assertThat(operation.getParameters().stream().filter(p -> p.getExtensions() != null && p.getExtensions().containsKey("x-sort-names")))
                .singleElement().satisfies(p -> assertThat(p.getName()).isEqualTo("selected-order"));
        assertThat(org.assertj.core.api.Assertions.catchThrowable(() -> analyser.postProcessAfterInitialization(endpoint, "valid"))).isNull();
    }

    @Test
    void legacyDocumentationDoesNotPublishARestrictiveAllowlist() throws Exception {
        var beans = new DefaultListableBeanFactory();
        beans.registerSingleton("policy", JpaSortPolicy.LEGACY);
        var documentation = new DynaFilterOperationCustomizer(null, operations);
        documentation.setBeanFactory(beans);
        var handler = new HandlerMethod(controller, PeopleController.class.getDeclaredMethod("people", ConditionalStatement.class, Pageable.class));
        var operation = documentation.customize(new Operation().parameters(new ArrayList<>()), handler);
        assertThat(operation.getParameters()).noneMatch(p -> p.getExtensions() != null && p.getExtensions().containsKey("x-sort-names"));
    }

    @Test
    void aPreviouslyCompiledFrameworkClientKeepsTheConstructorAndReceivesAutomaticIntegration() throws Exception {
        byte[] binary;
        try (var stream = java.util.Objects.requireNonNull(getClass().getResourceAsStream("/compat/LegacyCustomizerClient.class.base64"))) {
            binary = java.util.Base64.getMimeDecoder().decode(stream.readAllBytes());
        }
        var client = new ClassLoader(getClass().getClassLoader()) {
            Class<?> defineClient() {
                return defineClass("compat.LegacyCustomizerClient", binary, 0, binary.length);
            }
        }.defineClient();
        var instance = (DynaFilterOperationCustomizer) client.getMethod("create", FilterOperationService.class).invoke(null, operations);
        var managed = (DynaFilterOperationCustomizer) beanFactory.initializeBean(instance, "precompiledFrameworkCustomizer");
        var handler = new HandlerMethod(controller, PeopleController.class.getDeclaredMethod("people", ConditionalStatement.class, Pageable.class));
        var operation = managed.customize(new Operation(), handler);
        assertThat(operation.getParameters().stream().filter(p -> p.getName().equals("order")).findFirst().orElseThrow()
                .getExtensions().get("x-sort-names")).isEqualTo(List.of("name", "query"));
    }

    @Test
    void springdocGeneratedSortIsReconciledWithoutRemovingUnrelatedParameters() throws Exception {
        var handler = new HandlerMethod(controller, PeopleController.class.getDeclaredMethod("paired",
                ConditionalStatement.class, Pageable.class, ConditionalStatement.class, Pageable.class));
        var operation = springdocPageableParameters();
        var audit = new io.swagger.v3.oas.models.parameters.Parameter().name("audit").in("query")
                .description("Independent request parameter").schema(new io.swagger.v3.oas.models.media.StringSchema());
        operation.addParametersItem(audit);
        assertThat(operation.getParameters()).anyMatch(p -> p.getName().equals("sort"));
        customizer.customize(operation, handler);
        assertThat(operation.getParameters()).noneMatch(p -> p.getName().equals("sort"));
        assertThat(operation.getParameters()).contains(audit);
        assertThat(operation.getParameters()).extracting(io.swagger.v3.oas.models.parameters.Parameter::getName)
                .contains("left-order", "right-order", "page", "size", "audit");
        assertThat(operation.getParameters()).allSatisfy(p -> {
            if (p.getExtensions() != null) {
                assertThat(p.getExtensions()).doesNotContainKey("x-runestone-generated-sort");
            }
        });
    }

    private Operation springdocPageableParameters() throws Exception {
        var properties = new org.springdoc.core.properties.SpringDocConfigProperties();
        var modelBuilder = new org.springdoc.core.service.GenericParameterService(
                new org.springdoc.core.utils.PropertyResolverUtils(new DefaultListableBeanFactory(),
                        new org.springframework.context.support.StaticMessageSource(), properties),
                java.util.Optional.empty(), new org.springdoc.core.providers.ObjectMapperProvider(properties), java.util.Optional.empty());
        var method = SpringdocModel.class.getDeclaredMethods()[0];
        var extracted = org.springdoc.core.extractor.DelegatingMethodParameter.customize(null,
                new org.springframework.core.MethodParameter[]{new org.springframework.core.MethodParameter(method, 0)},
                java.util.Optional.empty(), new org.springdoc.core.extractor.MethodParameterPojoExtractor(
                        new org.springdoc.core.utils.SchemaUtils(java.util.Optional.empty())), false);
        var operation = new Operation().parameters(new ArrayList<>());
        for (var field : extracted) {
            var parameter = modelBuilder.buildParameterFromDoc(field.getParameterAnnotation(io.swagger.v3.oas.annotations.Parameter.class),
                    new io.swagger.v3.oas.models.Components(), null, java.util.Locale.ROOT);
            parameter.setName(field.getParameterName());
            parameter.setIn("query");
            for (var callback : beanFactory.getBeanProvider(org.springdoc.core.customizers.ParameterCustomizer.class).stream().toList()) {
                parameter = callback.customize(parameter, field);
            }
            operation.addParametersItem(parameter);
        }
        return operation;
    }

    static class SpringdocModel {
        public void parameters(@org.springdoc.core.annotations.ParameterObject org.springdoc.core.converters.models.Pageable pageable) {
        }
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

    static class MissingQualifier {
        public void search(@ConjunctionFrom(DeclaredNameSearch.class) ConditionalStatement left, Pageable leftPage,
                           @ConjunctionFrom(DeclaredIdentifierSearch.class) ConditionalStatement right, Pageable rightPage) {
        }
    }

    static class DuplicateQualifier {
        public void search(@Qualifier("same") @ConjunctionFrom(DeclaredNameSearch.class) ConditionalStatement left,
                           @Qualifier("same") @ConjunctionFrom(DeclaredIdentifierSearch.class) ConditionalStatement right,
                           @Qualifier("same") Pageable page) {
        }
    }

    static class MissingMatch {
        public void search(@Qualifier("left") @ConjunctionFrom(DeclaredNameSearch.class) ConditionalStatement left,
                           @Qualifier("right") @ConjunctionFrom(DeclaredIdentifierSearch.class) ConditionalStatement right,
                           @Qualifier("unknown") Pageable page) {
        }
    }

    static class DuplicateOrdering {
        public void search(@Qualifier("same") @ConjunctionFrom(DeclaredNameSearch.class) ConditionalStatement statement,
                           @Qualifier("same") Pageable page, @Qualifier("same") org.springframework.data.domain.Sort sort) {
        }
    }

    static class PartiallyAssociated {
        public void search(ConditionalStatement noSortContext,
                           @Qualifier("selected") @ConjunctionFrom(DeclaredNameSearch.class) ConditionalStatement statement,
                           @Qualifier("selected") org.springframework.data.domain.Sort sort) {
        }
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
        String people(@ConjunctionFrom(DeclaredNameSearch.class) ConditionalStatement statement, Pageable pageable) {
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

        @GetMapping("/paired")
        String paired(@Qualifier("left") @ConjunctionFrom(DeclaredNameSearch.class) ConditionalStatement left,
                      @Qualifier("left") Pageable leftPage,
                      @Qualifier("right") @ConjunctionFrom(DeclaredIdentifierSearch.class) ConditionalStatement right,
                      @Qualifier("right") Pageable rightPage) {
            return repository.findAll(left, leftPage).stream().map(Person::getName).collect(Collectors.joining(","))
                    + "|" + repository.findAll(right, rightPage).stream().map(Person::getName).collect(Collectors.joining(","));
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
            resolver.setQualifierDelimiter("-");
            return resolver;
        }

        @Bean
        DynaFilterOperationCustomizer filterCustomizer(FilterOperationService<?> operations) {
            // This is the existing constructor used by external frameworks.
            return new DynaFilterOperationCustomizer(null, operations);
        }
    }
}
