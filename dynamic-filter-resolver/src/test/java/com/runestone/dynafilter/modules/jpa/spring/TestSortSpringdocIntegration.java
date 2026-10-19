package com.runestone.dynafilter.modules.jpa.spring;

import com.runestone.dynafilter.core.generator.ConditionalStatement;
import com.runestone.dynafilter.core.generator.annotation.ConjunctionFrom;
import com.runestone.dynafilter.core.generator.annotation.Filter;
import com.runestone.dynafilter.core.generator.annotation.FilterTarget;
import com.runestone.dynafilter.core.operation.FilterOperationService;
import com.runestone.dynafilter.core.operation.types.Equals;
import com.runestone.dynafilter.modules.jpa.tools.app.database.InMemoryDatabaseApplication;
import com.runestone.dynafilter.modules.jpa.tools.app.database.PersonRepository;
import com.runestone.dynafilter.modules.jpa.tools.app.database.jpamodels.Person;
import com.runestone.dynafilter.modules.openapi.DynaFilterOperationCustomizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springdoc.core.annotations.ParameterObject;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.SortHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {InMemoryDatabaseApplication.class, TestSortSpringdocIntegration.WebConfiguration.class},
        properties = {"spring.jpa.hibernate.ddl-auto=create-drop", "springdoc.cache.disabled=true"})
class TestSortSpringdocIntegration {
    @Autowired
    private WebApplicationContext context;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void aDeclarativeFilterNamedSortIsNotDeletedAsObsoleteGeneratedOrdering() throws Exception {
        var response = mvc.perform(get("/v3/api-docs")).andExpect(status().isOk()).andReturn();
        var document = io.swagger.v3.core.util.Json.mapper().readTree(response.getResponse().getContentAsString());
        var parameters = document.path("paths").path("/documented-filter").path("get").path("parameters");
        assertThat(parameters.findValuesAsText("name")).contains("sort", "order");
        var filter = java.util.stream.StreamSupport.stream(parameters.spliterator(), false)
                .filter(p -> p.path("name").asText().equals("sort")).findFirst().orElseThrow();
        assertThat(filter.path("description").asText()).isEqualTo("Person name filter");
        mvc.perform(get("/documented-filter").param("sort", "Alice").param("order", "sort:asc"))
                .andExpect(status().isOk());
    }

    @Test
    void realSpringdocEndpointPublishesConfiguredQualifiedSortAndPreservesExplicitQueries() throws Exception {
        var response = mvc.perform(get("/v3/api-docs")).andExpect(status().isOk()).andReturn();
        var document = io.swagger.v3.core.util.Json.mapper().readTree(response.getResponse().getContentAsString());
        var parameters = document.path("paths").path("/documented-pair").path("get").path("parameters");
        List<String> names = new ArrayList<>();
        parameters.forEach(parameter -> names.add(parameter.path("name").asText()));
        assertThat(names).contains("left-order", "right-order", "audit").doesNotContain("sort");
        assertThat(parameters.toString()).contains("property:desc").doesNotContain("x-runestone-generated-sort");
        var explicit = document.path("paths").path("/documented-query").path("get").path("parameters");
        assertThat(explicit.findValuesAsText("name")).contains("sort", "order");
        mvc.perform(get("/documented-pair").param("left-order", "query:asc").param("right-order", "identifier:desc"))
                .andExpect(status().isOk());
        mvc.perform(get("/documented-pair").param("right-order", "query:asc")).andExpect(status().isBadRequest());
        mvc.perform(get("/documented-query").param("sort", "independent").param("order", "query:asc"))
                .andExpect(status().isOk());
    }

    @RestController
    static class Endpoint {
        private final PersonRepository repository;

        Endpoint(PersonRepository repository) {
            this.repository = repository;
        }

        @GetMapping("/documented-pair")
        public String paired(
                @io.swagger.v3.oas.annotations.Parameter(hidden = true) @Qualifier("left")
                @ConjunctionFrom(DeclaredNameSearch.class) ConditionalStatement left,
                @Qualifier("left") @ParameterObject Pageable leftPage,
                @io.swagger.v3.oas.annotations.Parameter(hidden = true) @Qualifier("right")
                @ConjunctionFrom(DeclaredIdentifierSearch.class) ConditionalStatement right,
                @Qualifier("right") @ParameterObject Pageable rightPage,
                @RequestParam(defaultValue = "") String audit) {
            return repository.findAll(left, leftPage).getTotalElements() + "|" + repository.findAll(right, rightPage).getTotalElements() + audit;
        }

        @GetMapping("/documented-query")
        public String query(@ConjunctionFrom(DeclaredNameSearch.class) ConditionalStatement statement,
                            @ParameterObject Pageable pageable, @RequestParam("sort") String query) {
            return repository.findAll(statement, pageable).getTotalElements() + query;
        }

        @GetMapping("/documented-filter")
        public Long filter(@ConjunctionFrom(SortNamedSearch.class) ConditionalStatement statement, @ParameterObject Pageable pageable) {
            return repository.findAll(statement, pageable).getTotalElements();
        }
    }

    @FilterTarget(Person.class)
    record SortNamedSearch(@Filter(path = "name", parameters = "sort", operation = Equals.class, description = "Person name filter") String name) {
    }

    @Configuration(proxyBeanMethods = false)
    @EnableDynamicFilterServletConfiguration
    static class WebConfiguration {
        @Bean
        Endpoint endpoint(PersonRepository repository) {
            return new Endpoint(repository);
        }

        @Bean
        OperationCustomizer externalFrameworkCustomizer(FilterOperationService<?> operations) {
            return new DynaFilterOperationCustomizer(null, operations);
        }

        @Bean
        WebMvcConfigurer orderingResolvers() {
            var sort = new SortHandlerMethodArgumentResolver();
            sort.setSortParameter("order");
            sort.setQualifierDelimiter("-");
            sort.setPropertyDelimiter(":");
            return new WebMvcConfigurer() {
                @Override
                public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
                    resolvers.add(new PageableHandlerMethodArgumentResolver(sort));
                    resolvers.add(sort);
                }
            };
        }
    }
}
