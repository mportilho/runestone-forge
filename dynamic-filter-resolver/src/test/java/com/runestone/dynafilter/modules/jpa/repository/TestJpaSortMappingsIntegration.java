package com.runestone.dynafilter.modules.jpa.repository;

import com.runestone.converters.impl.runtime.DefaultRuntimeDataConversionService;
import com.runestone.dynafilter.core.generator.ConditionalStatement;
import com.runestone.dynafilter.core.generator.SortContract;
import com.runestone.dynafilter.core.generator.StatementWrapper;
import com.runestone.dynafilter.core.generator.annotation.*;
import com.runestone.dynafilter.core.operation.types.Equals;
import com.runestone.dynafilter.core.operation.types.IsIn;
import com.runestone.dynafilter.core.operation.types.Decorated;
import com.runestone.dynafilter.core.operation.types.extensions.AnyFieldLike;
import com.runestone.dynafilter.core.operation.types.extensions.ContainsAll;
import com.runestone.dynafilter.modules.jpa.spring.EnableDynamicFilterServletConfiguration;
import com.runestone.dynafilter.modules.jpa.api.JpaConditionalStatements;
import com.runestone.dynafilter.modules.jpa.api.JpaFilterOperationService;
import com.runestone.dynafilter.modules.jpa.api.JpaSortPolicy;
import com.runestone.dynafilter.modules.jpa.resolver.SpecificationDynamicFilterResolver;
import com.runestone.dynafilter.modules.jpa.tools.sortmodel.SortEntry;
import com.runestone.dynafilter.modules.jpa.tools.sortmodel.SortOwner;
import com.runestone.dynafilter.modules.jpa.tools.sortmodel.SpecialSortEntry;
import com.runestone.dynafilter.modules.jpa.spring.JpaSortContext;
import com.runestone.dynafilter.modules.jpa.spring.FilterConfigurationAnalyserBeanPostProcessor;
import com.runestone.dynafilter.modules.jpa.spring.InvalidSortExceptionHandler;
import com.runestone.dynafilter.modules.jpa.spring.SpecificationDynamicFilterArgumentResolver;
import com.runestone.dynafilter.modules.openapi.DynaFilterOperationCustomizer;
import io.swagger.v3.oas.models.Operation;
import com.runestone.dynafilter.core.exceptions.DynamicFilterConfigurationException;
import com.runestone.dynafilter.core.exceptions.InvalidSortException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.data.web.SortHandlerMethodArgumentResolver;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.method.HandlerMethod;
import jakarta.persistence.EntityManagerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@DataJpaTest(properties = {"spring.jpa.properties.hibernate.generate_statistics=true",
        "spring.jpa.properties.hibernate.type.wrapper_array_handling=LEGACY"})
@ContextConfiguration(classes = TestJpaSortMappingsIntegration.PersistenceConfiguration.class)
class TestJpaSortMappingsIntegration {
    @Autowired
    private EntityManager entityManager;
    private DynamicFilterJpaRepositoryImpl<SortEntry, Long> repository;
    private final AnnotationStatementGenerator generator = new AnnotationStatementGenerator();

    @BeforeEach
    void setUp() {
        repository = repository(SortEntry.class);
        var zoe = new SortOwner(1L, "Zoe");
        var alice = new SortOwner(2L, "Alice");
        zoe.setManager(alice);
        alice.setManager(alice);
        entityManager.persist(zoe);
        entityManager.persist(alice);
        entityManager.persist(new SortEntry(1L, "Zoe", zoe, UUID.fromString("00000000-0000-0000-0000-000000000003"), "allowed", List.of("match")));
        entityManager.persist(new SortEntry(2L, "Alice", alice, UUID.fromString("00000000-0000-0000-0000-000000000001"), "allowed", List.of("match")));
        entityManager.persist(new SpecialSortEntry(3L, "Bob", alice, UUID.fromString("00000000-0000-0000-0000-000000000002"), "allowed", List.of("other")));
        entityManager.persist(new SortEntry(4L, "Outside", zoe, UUID.fromString("00000000-0000-0000-0000-000000000004"), "outside", List.of("match")));
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void preventiveExclusionsValidateExistenceButDoNotRequireSortableTerminalsOrBlockDescendants() {
        var source = statement(ExactExclusionSearch.class, Map.of());
        assertThat(repository.findAll(source, Sort.by("owner.name", "details.city")))
                .extracting(SortEntry::getId).containsExactly(2L, 3L, 4L, 1L);
        for (String name : List.of("owner", "details", "lobText", "binaryValue", "owner.entries")) {
            assertThatThrownBy(() -> repository.findAll(source, Sort.by(name))).isInstanceOf(InvalidSortException.class);
        }
    }

    @SortPolicy(include = {"owner.name", "details.city"}, exclude = {"owner", "details", "lobText", "binaryValue", "owner.entries"})
    interface ExactExclusionSearch {
    }

    @Test
    void openApiNamesMatchRepositoryAndHttpExecutionForNavigationAndPropertyAccess() throws Exception {
        var operations = new JpaFilterOperationService(DefaultRuntimeDataConversionService.standard());
        var beans = new DefaultListableBeanFactory();
        beans.registerSingleton("factory", entityManager.getEntityManagerFactory());
        beans.registerSingleton("catalog", new JpaSortContext(generator, beans.getBeanProvider(EntityManagerFactory.class)));
        beans.registerSingleton("policy", JpaSortPolicy.DECLARED);
        var sortResolver = new SortHandlerMethodArgumentResolver();
        beans.registerSingleton("sortResolver", sortResolver);
        var customizer = new DynaFilterOperationCustomizer(null, operations);
        customizer.setBeanFactory(beans);
        var controller = new MappingController(repository);
        var method = MappingController.class.getDeclaredMethod("entries", ConditionalStatement.class, Pageable.class);
        var operation = customizer.customize(new Operation().parameters(new ArrayList<>()), new HandlerMethod(controller, method));
        var sort = operation.getParameters().stream().filter(parameter -> parameter.getName().equals("sort")).findFirst().orElseThrow();
        List<String> expected = List.of("city", "computedName", "converted", "convertedValue", "details.city", "formula",
                "formulaOwner.name", "formulaOwnerName", "inheritedName", "managerName", "owner.manager.name", "owner.name", "ownerName", "propertyName");
        assertThat(sort.getExtensions().get("x-sort-names")).isEqualTo(expected);
        var input = new AnnotationStatementInput(ConditionalStatement.class, method.getParameters()[0].getAnnotations());
        var statement = new ConditionalStatement(generator.generateStatements(input, Map.of()), null);
        for (String name : expected) {
            assertThat(repository.findAll(statement, Sort.by(name, "propertyName")))
                    .extracting(SortEntry::getId).containsExactly(2L, 3L, 4L, 1L);
        }
        var mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new InvalidSortExceptionHandler())
                .setCustomArgumentResolvers(new SpecificationDynamicFilterArgumentResolver(generator,
                                new SpecificationDynamicFilterResolver(operations), inputValue -> null),
                        new PageableHandlerMethodArgumentResolver(sortResolver)).build();
        mvc.perform(get("/mapping-entries").param("sort", "city,asc").param("size", "1"))
                .andExpect(status().isOk()).andExpect(content().string("4:2"));
        mvc.perform(get("/mapping-entries").param("sort", "propertyName,desc").param("size", "1"))
                .andExpect(status().isOk()).andExpect(content().string("4:1"));
        mvc.perform(get("/mapping-entries").param("sort", "managerName,asc", "propertyName,asc").param("size", "1"))
                .andExpect(status().isOk()).andExpect(content().string("4:2"));
        for (String name : List.of("tags", "collectionName", "owner", "details", "lobText", "binaryValue", "owner.id", "details.city.extra")) {
            mvc.perform(get("/mapping-entries").param("sort", name + ",asc")).andExpect(status().isBadRequest());
        }
    }

    @Test
    void qualifiedContractsKeepTheirOwnPersistentSchemasAndExecuteTheirOwnRepositorySorts() throws Exception {
        var operations = new JpaFilterOperationService(DefaultRuntimeDataConversionService.standard());
        var beans = new DefaultListableBeanFactory();
        beans.registerSingleton("factory", entityManager.getEntityManagerFactory());
        beans.registerSingleton("catalog", new JpaSortContext(generator, beans.getBeanProvider(EntityManagerFactory.class)));
        beans.registerSingleton("policy", JpaSortPolicy.DECLARED);
        var sortResolver = new SortHandlerMethodArgumentResolver();
        beans.registerSingleton("sortResolver", sortResolver);
        var controller = new PairedMappingController(repository, repository(SortOwner.class));
        var analyser = new FilterConfigurationAnalyserBeanPostProcessor(operations, generator);
        analyser.setBeanFactory(beans);
        assertThatCode(() -> analyser.postProcessAfterInitialization(controller, "pairedMappings")).doesNotThrowAnyException();

        var customizer = new DynaFilterOperationCustomizer(null, operations);
        customizer.setBeanFactory(beans);
        var method = PairedMappingController.class.getDeclaredMethod("paired", ConditionalStatement.class,
                Pageable.class, ConditionalStatement.class, Pageable.class);
        var operation = customizer.customize(new Operation(), new HandlerMethod(controller, method));
        assertThat(operation.getParameters()).filteredOn(parameter -> parameter.getName().equals("entries_sort"))
                .singleElement().satisfies(parameter -> assertThat(parameter.getExtensions().get("x-sort-names"))
                        .isEqualTo(List.of("entryQuery", "propertyName")));
        assertThat(operation.getParameters()).filteredOn(parameter -> parameter.getName().equals("owners_sort"))
                .singleElement().satisfies(parameter -> assertThat(parameter.getExtensions().get("x-sort-names"))
                        .isEqualTo(List.of("id", "manager.name", "managerQuery", "ownerId")));
        assertThat(operation.getParameters()).filteredOn(parameter -> parameter.getName().equals("managerQuery"))
                .singleElement().satisfies(parameter -> assertThat(parameter.getSchema().getType()).isEqualTo("string"));
        assertThat(operation.getParameters()).filteredOn(parameter -> parameter.getName().equals("ownerId"))
                .singleElement().satisfies(parameter -> assertThat(parameter.getSchema().getType()).isEqualTo("integer"));

        var mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new InvalidSortExceptionHandler())
                .setCustomArgumentResolvers(new SpecificationDynamicFilterArgumentResolver(generator,
                                new SpecificationDynamicFilterResolver(operations), inputValue -> null),
                        new PageableHandlerMethodArgumentResolver(sortResolver)).build();
        mvc.perform(get("/paired-mappings").param("entryQuery", "Alice").param("managerQuery", "Alice")
                        .param("entries_sort", "entryQuery,asc").param("owners_sort", "managerQuery,asc", "ownerId,desc"))
                .andExpect(status().isOk()).andExpect(content().string("1:2|2:Alice"));
        mvc.perform(get("/paired-mappings").param("entries_sort", "managerQuery,asc"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/paired-mappings").param("owners_sort", "entryQuery,asc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void openApiUsesTheCompiledSubtypeRootInsteadOfTheReusedSourceRoot() throws Exception {
        var beans = new DefaultListableBeanFactory();
        beans.registerSingleton("factory", entityManager.getEntityManagerFactory());
        beans.registerSingleton("catalog", new JpaSortContext(generator, beans.getBeanProvider(EntityManagerFactory.class)));
        beans.registerSingleton("policy", JpaSortPolicy.DECLARED);
        beans.registerSingleton("sortResolver", new SortHandlerMethodArgumentResolver());
        var customizer = new DynaFilterOperationCustomizer(null,
                new JpaFilterOperationService(DefaultRuntimeDataConversionService.standard()));
        customizer.setBeanFactory(beans);
        var controller = new MappingController(repository);
        var method = MappingController.class.getDeclaredMethod("subtypeEntries", ConditionalStatement.class, Pageable.class);
        var operation = customizer.customize(new Operation().parameters(new ArrayList<>()), new HandlerMethod(controller, method));
        var sort = operation.getParameters().stream().filter(parameter -> parameter.getName().equals("sort")).findFirst().orElseThrow();
        assertThat(sort.getExtensions().get("x-sort-names")).isEqualTo(List.of("id"));
        var input = new AnnotationStatementInput(ConditionalStatement.class, method.getParameters()[0].getAnnotations());
        var statement = new ConditionalStatement(generator.generateStatements(input, Map.of()), null);
        assertThat(repository(SpecialSortEntry.class).findAll(statement, Sort.by("id")))
                .extracting(SpecialSortEntry::getId).containsExactly(3L);
    }

    @Test
    void openApiKeepsValidVirtualDecoratedFiltersIndependentFromSort() throws Exception {
        var beans = new DefaultListableBeanFactory();
        beans.registerSingleton("factory", entityManager.getEntityManagerFactory());
        beans.registerSingleton("catalog", new JpaSortContext(generator, beans.getBeanProvider(EntityManagerFactory.class)));
        beans.registerSingleton("policy", JpaSortPolicy.DECLARED);
        beans.registerSingleton("sortResolver", new SortHandlerMethodArgumentResolver());
        var customizer = new DynaFilterOperationCustomizer(null,
                new JpaFilterOperationService(DefaultRuntimeDataConversionService.standard()));
        customizer.setBeanFactory(beans);
        var controller = new MappingController(repository);
        var method = MappingController.class.getDeclaredMethod("virtualEntries", ConditionalStatement.class, Pageable.class);
        var operation = customizer.customize(new Operation().parameters(new ArrayList<>()), new HandlerMethod(controller, method));
        var virtual = operation.getParameters().stream().filter(parameter -> parameter.getName().equals("virtual")).findFirst().orElseThrow();
        assertThat(virtual.getSchema().getType()).isEqualTo("string");
        var sort = operation.getParameters().stream().filter(parameter -> parameter.getName().equals("sort")).findFirst().orElseThrow();
        assertThat(sort.getExtensions().get("x-sort-names")).isEqualTo(List.of("propertyName", "query"));
        var input = new AnnotationStatementInput(ConditionalStatement.class, method.getParameters()[0].getAnnotations());
        var statement = new ConditionalStatement(generator.generateStatements(input, Map.of("virtual", "decorator-input")), null);
        assertThat(statement.statementWrapper().decoratedFilters().get("largeTextStorage").values()).containsExactly("decorator-input");
        assertThat(repository.findAll(statement, Sort.by("query"))).extracting(SortEntry::getId).containsExactly(2L, 3L, 4L, 1L);
        assertThatThrownBy(() -> repository.findAll(statement, Sort.by("virtual"))).isInstanceOf(InvalidSortException.class);
    }

    @Test
    void servletInitializationAcceptsPersistentGettersAndValidVirtualFilters() {
        try (var context = new AnnotationConfigApplicationContext()) {
            context.getBeanFactory().registerSingleton("entityManagerFactory", entityManager.getEntityManagerFactory());
            context.register(ServletConfiguration.class);
            context.registerBean(MappingController.class, () -> new MappingController(repository));

            assertThatCode(context::refresh).doesNotThrowAnyException();
            assertThat(context.getBean(JpaSortContext.class)).isNotNull();
            assertThat(context.getBean(MappingController.class)).isNotNull();
        }
    }

    @Test
    void servletInitializationStillRejectsInvalidOperationsOnPersistentGetters() {
        try (var context = new AnnotationConfigApplicationContext()) {
            context.getBeanFactory().registerSingleton("entityManagerFactory", entityManager.getEntityManagerFactory());
            context.register(ServletConfiguration.class);
            context.registerBean(InvalidPropertyController.class);

            assertThatThrownBy(context::refresh).isInstanceOf(DynamicFilterConfigurationException.class)
                    .hasMessageContaining("requires a collection path").hasMessageContaining("propertyName");
        }
    }

    @Test
    void convertedNonComparableBasicsAndProviderFormulasUseTheSameStructuralRules() {
        var statement = statement(SpecialSearch.class, Map.of());
        for (String name : List.of("converted", "convertedValue", "formula", "computedName")) {
            assertThat(repository.findAll(statement, Sort.by(name))).extracting(SortEntry::getId)
                    .containsExactly(2L, 3L, 4L, 1L);
        }
        assertThat(repository.findAll(statement, Sort.by("formulaOwnerName", "computedName")))
                .extracting(SortEntry::getId).containsExactly(2L, 3L, 4L, 1L);
    }

    @Test
    void ineligibleCandidatesAreOmittedWithoutBreakingLegitimateCollectionPredicatesCountsOrGraphs() {
        var source = statement(CollectionSearch.class, Map.of("tags", new String[]{"match"}));
        var statement = JpaConditionalStatements.<SortEntry>restrict(source,
                (root, query, builder) -> builder.equal(root.get("scope"), "allowed"));
        var graph = entityManager.createEntityGraph(SortEntry.class);
        graph.addAttributeNodes("owner");
        entityManager.getEntityManagerFactory().addNamedEntityGraph("sort-mapping-owner", graph);

        for (boolean useGraph : List.of(false, true)) {
            var pageable = PageRequest.of(0, 1, Sort.by("propertyName"));
            var page = useGraph ? repository.findAll(statement, pageable, EntityGraph.EntityGraphType.LOAD, "sort-mapping-owner")
                    : repository.findAll(statement, pageable);
            assertThat(page.getContent()).extracting(SortEntry::getId).containsExactly(2L);
            assertThat(page.getTotalElements()).isEqualTo(2L);
            var next = useGraph ? repository.findAll(statement, pageable.next(), "sort-mapping-owner")
                    : repository.findAll(statement, pageable.next());
            assertThat(next.getContent()).extracting(SortEntry::getId).containsExactly(1L);
        }
        assertThat(repository.count(statement)).isEqualTo(2L);
        var pluralTraversal = statement(CollectionSearch.class, Map.of("collectionName", "Alice"));
        assertThat(repository.findAll(pluralTraversal, Sort.by("propertyName"))).extracting(SortEntry::getId).containsExactly(2L, 3L);
        assertThat(repository.count(pluralTraversal)).isEqualTo(2L);
        assertThat(repository.findAll(statement, Pageable.unpaged(Sort.by("propertyName")), "sort-mapping-owner").getContent())
                .extracting(SortEntry::getId).containsExactly(2L, 1L);

        var statistics = entityManager.getEntityManagerFactory().unwrap(org.hibernate.SessionFactory.class).getStatistics();
        statistics.clear();
        for (String name : List.of("tags", "owner.entries.propertyName", "collectionName", "owner.entries", "owner", "details",
                "lobText", "largeText", "binaryValue", "boxedBinary", "largeBinary", "missing", "owner.missing",
                "details.city.extra", "owner.name.extra", "details..city", "labelStorage", "owner.id")) {
            assertThatThrownBy(() -> repository.findAll(statement, Sort.by(name))).isInstanceOf(InvalidSortException.class);
        }
        assertThat(statistics.getPrepareStatementCount()).isZero();
    }

    @Test
    void multipleHttpUuidValuesDoNotMakeThePersistentScalarPlural() {
        var statement = statement(UuidSearch.class, Map.of("ids", new String[]{
                "00000000-0000-0000-0000-000000000003", "00000000-0000-0000-0000-000000000001"}));
        assertThat(repository.findAll(statement, Sort.by("ids"))).extracting(SortEntry::getId).containsExactly(2L, 1L);
        assertThat(repository.findAll(statement, Sort.by(Sort.Direction.DESC, "externalId")))
                .extracting(SortEntry::getId).containsExactly(1L, 2L);
    }

    @Test
    void declaredRootAcceptsJpaSubtypesButRejectsUnrelatedEntitiesWithTheSameAttributeNames() {
        var statement = statement(RootSearch.class, Map.of());
        assertThat(repository.findAll(statement, Sort.by("id"))).extracting(SortEntry::getId).containsExactly(1L, 2L, 3L, 4L);
        var subtype = repository(SpecialSortEntry.class);
        assertThat(subtype.findAll(statement, Sort.by("id"))).extracting(SpecialSortEntry::getId).containsExactly(3L);
        var unrelated = repository(SortOwner.class);
        assertThatThrownBy(() -> unrelated.findAll(statement, Sort.by("id")))
                .isInstanceOf(DynamicFilterConfigurationException.class).isNotInstanceOf(InvalidSortException.class);
        var onlySubtype = programmatic(new SortContract(List.of(new SortContract.Candidate("id", List.of())), SpecialSortEntry.class));
        assertThatThrownBy(() -> repository.findAll(onlySubtype, Sort.by("id")))
                .isInstanceOf(DynamicFilterConfigurationException.class).isNotInstanceOf(InvalidSortException.class);
        var nonEntityRoot = programmatic(new SortContract(List.of(new SortContract.Candidate("id", List.of())),
                com.runestone.dynafilter.modules.jpa.tools.sortmodel.SortBase.class));
        assertThatThrownBy(() -> repository.findAll(nonEntityRoot, Sort.by("id")))
                .isInstanceOf(DynamicFilterConfigurationException.class).isNotInstanceOf(InvalidSortException.class);
    }

    @Test
    void explicitProgrammaticContractsShareNavigationAndJpaRootValidation() {
        for (var contract : List.of(SortContract.builder().include("owner.name", "ownerName").include("propertyName").build(),
                SortContract.builder().root(SortEntry.class).include("owner.name", "ownerName").include("propertyName").build())) {
            var source = ConditionalStatement.builder().sortContract(contract).build();
            var statement = JpaConditionalStatements.<SortEntry>restrict(source,
                    (root, query, builder) -> builder.equal(root.get("scope"), "allowed"));
            var sort = Sort.by("ownerName", "propertyName");
            assertThat(repository.findAll(statement, sort)).extracting(SortEntry::getId).containsExactly(2L, 3L, 1L);
            var page = repository.findAll(statement, PageRequest.of(0, 1, sort));
            assertThat(page.getContent()).extracting(SortEntry::getId).containsExactly(2L);
            assertThat(page.getTotalElements()).isEqualTo(3L);
            assertThat(repository(SpecialSortEntry.class).findAll(statement, sort))
                    .extracting(SpecialSortEntry::getId).containsExactly(3L);
            assertThat(source.statementWrapper().sortContract()).isSameAs(contract);
        }
        var rooted = ConditionalStatement.builder().sortContract(
                SortContract.builder().root(SortEntry.class).include("id", "identifier").build()).build();
        assertThatThrownBy(() -> repository(SortOwner.class).findAll(rooted, Sort.by("identifier")))
                .isInstanceOf(DynamicFilterConfigurationException.class).isNotInstanceOf(InvalidSortException.class);
        for (String path : List.of("owner.entries.propertyName", "owner", "details", "lobText", "binaryValue")) {
            var invalid = ConditionalStatement.builder().sortContract(SortContract.builder().include(path).build()).build();
            assertThatThrownBy(() -> repository.findAll(invalid, Sort.by("propertyName")))
                    .isInstanceOf(DynamicFilterConfigurationException.class).isNotInstanceOf(InvalidSortException.class);
        }
    }

    @Test
    void programmaticContractsWithoutDeclaredRootsUseOnlyTheEffectiveJpaEntity() {
        var statement = programmatic(new SortContract(List.of(new SortContract.Candidate("id", List.of("identifier")))));
        assertThat(repository.findAll(statement, Sort.by("identifier"))).extracting(SortEntry::getId).containsExactly(1L, 2L, 3L, 4L);
        assertThat(repository(SortOwner.class).findAll(statement, Sort.by("identifier"))).extracting(SortOwner::getName)
                .containsExactly("Zoe", "Alice");
    }

    private ConditionalStatement programmatic(SortContract contract) {
        var empty = ConditionalStatement.builder().build().statementWrapper();
        return new ConditionalStatement(new StatementWrapper(empty.statement(), empty.decoratedFilters(), empty.allFilters(), contract), null);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private <T> DynamicFilterJpaRepositoryImpl<T, Long> repository(Class<T> entity) {
        var result = new DynamicFilterJpaRepositoryImpl<T, Long>(JpaEntityInformationSupport.getEntityInformation(entity, entityManager), entityManager);
        result.setDynamicFilterResolver((com.runestone.dynafilter.core.resolver.DynamicFilterResolver)
                new SpecificationDynamicFilterResolver(new JpaFilterOperationService(DefaultRuntimeDataConversionService.standard())));
        return result;
    }

    @Test
    void singularNavigationPropertyAccessAndInheritedAttributesSortByPersistentLeaves() {
        var statement = statement(Search.class, Map.of());
        for (String name : List.of("city", "details.city", "inheritedName", "propertyName")) {
            assertThat(repository.findAll(statement, Sort.by(name))).extracting(SortEntry::getId)
                    .containsExactly(2L, 3L, 4L, 1L);
        }
        assertThat(repository.findAll(statement, Sort.by("ownerName", "propertyName"))).extracting(SortEntry::getId)
                .containsExactly(2L, 3L, 4L, 1L);
    }

    @Test
    void compoundNavigationPublishesEligibleLeavesWithoutPromotingAliasesAfterOmission() {
        var statement = statement(CompoundNavigationSearch.class, Map.of());
        for (String path : List.of("owner.name", "details.city", "owner.manager.name")) {
            assertThat(repository.findAll(statement, Sort.by(path, "details.city")))
                    .extracting(SortEntry::getId).containsExactly(2L, 3L, 4L, 1L);
        }
        for (String rejected : List.of("term", "reduced", "owner.entries.propertyName")) {
            assertThatThrownBy(() -> repository.findAll(statement, Sort.by(rejected))).isInstanceOf(InvalidSortException.class);
        }
        var active = statement(CompoundNavigationSearch.class, Map.of("term", "Alice"));
        assertThat(repository.findAll(active, Sort.by(Sort.Direction.DESC, "details.city")))
                .extracting(SortEntry::getId).containsExactly(3L, 2L);
        assertThat(repository.count(active)).isEqualTo(2L);
    }

    @Test
    void sortingNullableSingularAssociationsKeepsContentCountRestrictionsAndGraphs() {
        entityManager.persist(new SortEntry(5L, "Unowned", null,
                UUID.fromString("00000000-0000-0000-0000-000000000005"), "allowed", List.of()));
        entityManager.flush();
        entityManager.clear();
        var source = statement(Search.class, Map.of());
        var statement = JpaConditionalStatements.<SortEntry>restrict(source,
                (root, query, builder) -> builder.equal(root.get("scope"), "allowed"));
        var graph = entityManager.createEntityGraph(SortEntry.class);
        graph.addAttributeNodes("owner");
        entityManager.getEntityManagerFactory().addNamedEntityGraph("nullable-sort-owner", graph);
        var sort = Sort.by("ownerName", "propertyName");
        assertThat(repository.findAll(statement, sort)).extracting(SortEntry::getId).containsExactly(5L, 2L, 3L, 1L);
        assertThat(repository.findAll(statement, sort, "nullable-sort-owner"))
                .extracting(SortEntry::getId).containsExactly(5L, 2L, 3L, 1L);
        for (boolean useGraph : List.of(false, true)) {
            var pageable = PageRequest.of(0, 1, sort);
            var page = useGraph ? repository.findAll(statement, pageable, "nullable-sort-owner")
                    : repository.findAll(statement, pageable);
            assertThat(page.getContent()).extracting(SortEntry::getId).containsExactly(5L);
            assertThat(page.getTotalElements()).isEqualTo(4L);
        }
    }

    private ConditionalStatement statement(Class<?> search, Map<String, Object> parameters) {
        return new ConditionalStatement(generator.generateStatements(new AnnotationStatementInput(search, null), parameters), null);
    }

    @Conjunction({
            @Filter(path = "owner.name", parameters = "ownerName", operation = Equals.class),
            @Filter(path = "details.city", parameters = "city", operation = Equals.class),
            @Filter(path = "inheritedName", parameters = "inheritedName", operation = Equals.class),
            @Filter(path = "propertyName", parameters = "propertyName", operation = Equals.class)
    })
    interface Search {
    }

    @FilterTarget(SortEntry.class)
    @Conjunction({
            @Filter(path = {"owner.name", "details.city"}, parameters = "term", operation = AnyFieldLike.class),
            @Filter(path = {"owner.manager.name", "owner.entries.propertyName"}, parameters = "reduced", operation = AnyFieldLike.class)
    })
    interface CompoundNavigationSearch {
    }

    @Conjunction({
            @Filter(path = "convertedValue", parameters = "converted", operation = Equals.class),
            @Filter(path = "computedName", parameters = "formula", operation = Equals.class),
            @Filter(path = "formulaOwner.name", parameters = "formulaOwnerName", operation = Equals.class)
    })
    interface SpecialSearch {
    }

    @Conjunction({
            @Filter(path = "tags", parameters = "tags", operation = IsIn.class, targetType = String[].class),
            @Filter(path = "owner.entries.propertyName", parameters = "collectionName", operation = Equals.class),
            @Filter(path = "owner.entries", parameters = "entries", operation = Equals.class),
            @Filter(path = "owner", parameters = "owner", operation = Equals.class),
            @Filter(path = "details", parameters = "details", operation = Equals.class),
            @Filter(path = "lobText", parameters = "lobText", operation = Equals.class),
            @Filter(path = "largeText", parameters = "largeText", operation = Equals.class),
            @Filter(path = "binaryValue", parameters = "binaryValue", operation = Equals.class),
            @Filter(path = "boxedBinary", parameters = "boxedBinary", operation = Equals.class),
            @Filter(path = "largeBinary", parameters = "largeBinary", operation = Equals.class),
            @Filter(path = "missing", parameters = "missing", operation = Equals.class),
            @Filter(path = "owner.missing", parameters = "owner.missing", operation = Equals.class),
            @Filter(path = "details.city.extra", parameters = "details.city.extra", operation = Equals.class),
            @Filter(path = "owner.name.extra", parameters = "owner.name.extra", operation = Equals.class),
            @Filter(path = "details..city", parameters = "details..city", operation = Equals.class)
    })
    interface CollectionSearch extends Search {
    }

    @Conjunction(@Filter(path = "externalId", parameters = "ids", operation = IsIn.class, targetType = UUID[].class))
    interface UuidSearch {
    }

    @FilterTarget(SortEntry.class)
    @Conjunction(@Filter(path = "id", parameters = "id", operation = Equals.class))
    interface RootSearch {
    }

    @FilterTarget(SortEntry.class)
    static class DocumentedSearch {
        @Filter(path = "owner.name", parameters = "ownerName", operation = Equals.class)
        private String ownerName;
        @Filter(path = "owner.manager.name", parameters = "managerName", operation = Equals.class)
        private String managerName;
        @Filter(path = "details.city", parameters = "city", operation = Equals.class)
        private String city;
        @Filter(path = "inheritedName", parameters = "inheritedName", operation = Equals.class)
        private String inheritedName;
        @Filter(path = "propertyName", parameters = "propertyName", operation = Equals.class)
        private String propertyName;
        @Filter(path = "computedName", parameters = "formula", operation = Equals.class)
        private String formula;
        @Filter(path = "formulaOwner.name", parameters = "formulaOwnerName", operation = Equals.class)
        private String formulaOwnerName;
        @Filter(path = "convertedValue", parameters = "converted", operation = Equals.class)
        private String converted;
        @Filter(path = "tags", parameters = "tags", operation = IsIn.class)
        private String[] tags;
        @Filter(path = "owner.entries.propertyName", parameters = "collectionName", operation = Equals.class)
        private String collectionName;
        @Filter(path = "owner", parameters = "owner", operation = Equals.class)
        private String owner;
        @Filter(path = "details", parameters = "details", operation = Equals.class)
        private String details;
        @Filter(path = "lobText", parameters = "lobText", operation = Equals.class)
        private String lobText;
        @Filter(path = "binaryValue", parameters = "binaryValue", operation = Equals.class)
        private String binaryValue;
    }

    @FilterTarget(SortEntry.class)
    static class EntrySearch {
        @Filter(path = "propertyName", parameters = "entryQuery", operation = Equals.class)
        private String query;
    }

    @FilterTarget(SortOwner.class)
    static class OwnerSearch {
        @Filter(path = "manager.name", parameters = "managerQuery", operation = Equals.class)
        private String manager;
        @Filter(path = "id", parameters = "ownerId", operation = Equals.class)
        private Long id;
    }

    @RestController
    static class PairedMappingController {
        private final DynamicFilterJpaRepositoryImpl<SortEntry, Long> entries;
        private final DynamicFilterJpaRepositoryImpl<SortOwner, Long> owners;

        PairedMappingController(DynamicFilterJpaRepositoryImpl<SortEntry, Long> entries,
                                DynamicFilterJpaRepositoryImpl<SortOwner, Long> owners) {
            this.entries = entries;
            this.owners = owners;
        }

        @GetMapping("/paired-mappings")
        String paired(@Qualifier("entries") @ConjunctionFrom(EntrySearch.class) ConditionalStatement entryStatement,
                      @Qualifier("entries") Pageable entryPage,
                      @Qualifier("owners") @ConjunctionFrom(OwnerSearch.class) ConditionalStatement ownerStatement,
                      @Qualifier("owners") Pageable ownerPage) {
            var entryResult = entries.findAll(entryStatement, entryPage);
            var ownerResult = owners.findAll(ownerStatement, ownerPage);
            return entryResult.getTotalElements() + ":" + entryResult.getContent().getFirst().getId()
                    + "|" + ownerResult.getTotalElements() + ":" + ownerResult.getContent().getFirst().getName();
        }
    }

    @FilterTarget(SortEntry.class)
    static class IdSearch {
        @Filter(path = "id", parameters = "id", operation = Equals.class)
        private Long id;
    }

    @FilterTarget(SortEntry.class)
    static class VirtualSearch {
        @Filter(path = "propertyName", parameters = "query", operation = Equals.class)
        private String query;
        @Filter(path = "largeTextStorage", parameters = "virtual", operation = Decorated.class)
        private String virtual;
    }

    @FilterTarget(SortEntry.class)
    static class InvalidPropertySearch {
        @Filter(path = "propertyName", parameters = "names", operation = ContainsAll.class)
        private String[] names;
    }

    @RestController
    static class InvalidPropertyController {
        @GetMapping("/invalid-property-operation")
        String entries(@ConjunctionFrom(InvalidPropertySearch.class) ConditionalStatement statement, Pageable pageable) {
            return "unused";
        }
    }

    @RestController
    static class MappingController {
        private final DynamicFilterJpaRepositoryImpl<SortEntry, Long> repository;

        MappingController(DynamicFilterJpaRepositoryImpl<SortEntry, Long> repository) {
            this.repository = repository;
        }

        @GetMapping("/mapping-entries")
        String entries(@ConjunctionFrom(DocumentedSearch.class) ConditionalStatement statement, Pageable pageable) {
            var page = repository.findAll(statement, pageable);
            return page.getTotalElements() + ":" + page.getContent().getFirst().getId();
        }

        @GetMapping("/mapping-subtype-entries")
        String subtypeEntries(@ConjunctionFrom(IdSearch.class) @FilterTarget(SpecialSortEntry.class) ConditionalStatement statement, Pageable pageable) {
            return "unused";
        }

        @GetMapping("/mapping-virtual-entries")
        String virtualEntries(@ConjunctionFrom(VirtualSearch.class) ConditionalStatement statement, Pageable pageable) {
            return "unused";
        }
    }

    @Configuration(proxyBeanMethods = false)
    @EnableDynamicFilterServletConfiguration
    static class ServletConfiguration {
    }

    @Configuration(proxyBeanMethods = false)
    @EnableAutoConfiguration
    @EntityScan(basePackageClasses = SortEntry.class)
    static class PersistenceConfiguration {
    }
}
