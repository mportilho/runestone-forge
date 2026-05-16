package com.runestone.dynafilter.modules.userflow;

import com.runestone.dynafilter.core.annotation.ConjunctionFrom;
import com.runestone.dynafilter.core.annotation.Filter;
import com.runestone.dynafilter.core.annotation.FilterTarget;
import com.runestone.dynafilter.core.decorator.FilterDecoratorFactory;
import com.runestone.dynafilter.core.generator.annotation.AnnotationStatementGenerator;
import com.runestone.dynafilter.core.operation.Equals;
import com.runestone.dynafilter.modules.jpa.resolver.SpecificationDynamicFilterResolver;
import com.runestone.dynafilter.modules.jpa.spring.SpecificationDynamicFilterArgumentResolver;
import com.runestone.dynafilter.modules.jpa.tools.app.database.jpamodels.Person;
import com.runestone.dynafilter.modules.openapi.DynaFilterOperationCustomizer;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.parameters.Parameter;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EntityScan(basePackageClasses = Person.class)
class ApiDeveloperUserFlowTest {

    private MockMvc mockMvc;

    private PersonFixtureRepository repository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        repository = new PersonFixtureRepository(entityManager);
        repository.deleteAll();
        repository.save(new Person("Ada Lovelace", new BigDecimal("1.70"), new BigDecimal("62.50"),
                LocalDate.of(1815, 12, 10), LocalDateTime.of(2026, 5, 16, 10, 30)));
        repository.save(new Person("Grace Hopper", new BigDecimal("1.68"), new BigDecimal("60.00"),
                LocalDate.of(1906, 12, 9), LocalDateTime.of(2026, 5, 16, 11, 0)));
        entityManager.flush();
        FilterDecoratorFactory decoratorFactory = input -> null;
        SpecificationDynamicFilterArgumentResolver argumentResolver = new SpecificationDynamicFilterArgumentResolver(
                new AnnotationStatementGenerator(),
                new SpecificationDynamicFilterResolver(),
                decoratorFactory
        );
        mockMvc = MockMvcBuilders.standaloneSetup(new FixtureController(repository))
                .setCustomArgumentResolvers(argumentResolver)
                .build();
    }

    @Test
    @DisplayName("controller receives a ready Specification from dynamic request filters")
    void controllerReceivesReadySpecification() throws Exception {
        mockMvc.perform(get("/fixture/people").param("name", "Ada Lovelace"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("[\"Ada Lovelace\"]"));
    }

    @Test
    @DisplayName("constant filter prevails over request input")
    void constantFilterPrevailsOverRequestInput() throws Exception {
        mockMvc.perform(get("/fixture/constant-people").param("name", "Grace Hopper"))
                .andExpect(status().isOk())
                .andExpect(content().json("[\"Ada Lovelace\"]"));
    }

    @Test
    @DisplayName("OpenAPI documents real filter parameters and hides technical/constant parameters")
    void openApiDocumentsRealFilters() throws Exception {
        DynaFilterOperationCustomizer customizer = new DynaFilterOperationCustomizer();
        Operation operation = new Operation().parameters(List.of(new Parameter().name("specification").in("query")));
        Method method = FixtureController.class.getDeclaredMethod("people", Specification.class);

        Operation customized = customizer.customize(operation, new HandlerMethod(new FixtureController(repository), method));

        assertThat(customized.getParameters()).extracting(Parameter::getName).containsExactly("name");
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EntityScan(basePackageClasses = Person.class)
    static class TestApplication {
    }

    @RestController
    @RequestMapping("/fixture")
    static class FixtureController {

        private final PersonFixtureRepository repository;

        FixtureController(PersonFixtureRepository repository) {
            this.repository = repository;
        }

        @GetMapping("/people")
        List<String> people(@ConjunctionFrom(PersonNameFilters.class) Specification<Person> specification) {
            return repository.findNames(specification);
        }

        @GetMapping("/constant-people")
        List<String> constantPeople(@ConjunctionFrom(ConstantPersonFilters.class) Specification<Person> specification) {
            return repository.findNames(specification);
        }
    }

    @Repository
    static class PersonFixtureRepository {

        private final EntityManager entityManager;

        PersonFixtureRepository(EntityManager entityManager) {
            this.entityManager = entityManager;
        }

        void save(Person person) {
            entityManager.persist(person);
        }

        void deleteAll() {
            entityManager.createQuery("delete from Phone").executeUpdate();
            entityManager.createQuery("delete from Address").executeUpdate();
            entityManager.createQuery("delete from Person").executeUpdate();
        }

        List<String> findNames(Specification<Person> specification) {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<Person> query = criteriaBuilder.createQuery(Person.class);
            Root<Person> root = query.from(Person.class);
            query.select(root).where(specification.toPredicate(root, query, criteriaBuilder));
            return entityManager.createQuery(query).getResultStream()
                    .map(Person::getName)
                    .toList();
        }
    }

    @FilterTarget(Person.class)
    static class PersonNameFilters {

        @Filter(path = "name", parameters = "name", operation = Equals.class)
        private String name;
    }

    @FilterTarget(Person.class)
    static class ConstantPersonFilters {

        @Filter(path = "name", parameters = "name", operation = Equals.class, constantValues = "Ada Lovelace")
        private String name;
    }
}
