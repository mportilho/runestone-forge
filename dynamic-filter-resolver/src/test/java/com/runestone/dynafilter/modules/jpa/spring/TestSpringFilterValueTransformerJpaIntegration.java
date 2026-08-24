package com.runestone.dynafilter.modules.jpa.spring;

import com.runestone.dynafilter.core.generator.annotation.Conjunction;
import com.runestone.dynafilter.core.generator.annotation.Filter;
import com.runestone.dynafilter.core.operation.types.Equals;
import com.runestone.dynafilter.core.transformer.FilterValueContext;
import com.runestone.dynafilter.core.transformer.FilterValueTransformer;
import com.runestone.dynafilter.modules.jpa.tools.app.database.InMemoryDatabaseApplication;
import com.runestone.dynafilter.modules.jpa.tools.app.database.PersonRepository;
import com.runestone.dynafilter.modules.jpa.tools.app.database.jpamodels.Person;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DataJpaTest
@ContextConfiguration(classes = {
        InMemoryDatabaseApplication.class,
        TestSpringFilterValueTransformerJpaIntegration.TransformerHttpConfiguration.class
})
class TestSpringFilterValueTransformerJpaIntegration {

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PersonController controller;

    @Autowired
    private WebMvcConfigurer webMvcConfigurer;

    @Autowired
    private HeightAliasTransformer transformer;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        personRepository.deleteAll();
        personRepository.save(person("Tall Person", "180"));
        personRepository.save(person("Short Person", "160"));
        entityManager.flush();
        entityManager.clear();
        transformer.calls.set(0);

        List<HandlerMethodArgumentResolver> resolvers = new ArrayList<>();
        webMvcConfigurer.addArgumentResolvers(resolvers);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(resolvers.toArray(HandlerMethodArgumentResolver[]::new))
                .build();
    }

    @Test
    void transformsAnHttpValueBeforeStandardJpaConversion() throws Exception {
        mockMvc.perform(get("/people/by-height").param("height", "tall"))
                .andExpect(status().isOk())
                .andExpect(content().string("Tall Person"));

        assertThat(transformer.calls).hasValue(1);
    }

    private static Person person(String name, String height) {
        Person person = instantiate(Person.class);
        ReflectionTestUtils.setField(person, "name", name);
        ReflectionTestUtils.setField(person, "height", new BigDecimal(height));
        ReflectionTestUtils.setField(person, "weight", BigDecimal.valueOf(70));
        ReflectionTestUtils.setField(person, "birthday", LocalDate.of(1990, 1, 1));
        ReflectionTestUtils.setField(person, "registerDate", LocalDateTime.of(2024, 1, 1, 10, 30));
        return person;
    }

    private static <T> T instantiate(Class<T> type) {
        try {
            Constructor<T> constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not instantiate " + type.getName(), e);
        }
    }

    @Conjunction(@Filter(path = "height", parameters = "height", operation = Equals.class,
            transformers = HeightAliasTransformer.class))
    interface HeightFilter extends Specification<Person> {
    }

    @RestController
    static class PersonController {
        private final PersonRepository repository;

        PersonController(PersonRepository repository) {
            this.repository = repository;
        }

        @GetMapping("/people/by-height")
        String findByHeight(HeightFilter filter) {
            List<Person> people = repository.findAll(filter);
            return people.isEmpty() ? "" : people.getFirst().getName();
        }
    }

    static final class HeightLookup {
        String resolve(String alias) {
            return alias.equals("tall") ? "180" : alias;
        }
    }

    static final class HeightAliasTransformer implements FilterValueTransformer {
        private final HeightLookup lookup;
        private final AtomicInteger calls = new AtomicInteger();

        HeightAliasTransformer(HeightLookup lookup) {
            this.lookup = lookup;
        }

        @Override
        public Object transform(Object value, FilterValueContext context) {
            calls.incrementAndGet();
            return lookup.resolve(value.toString());
        }
    }

    @Configuration(proxyBeanMethods = false)
    @EnableDynamicFilterServletConfiguration
    static class TransformerHttpConfiguration {
        @Bean
        HeightLookup heightLookup() {
            return new HeightLookup();
        }

        @Bean
        HeightAliasTransformer heightAliasTransformer(HeightLookup lookup) {
            return new HeightAliasTransformer(lookup);
        }

        @Bean
        PersonController personController(PersonRepository repository) {
            return new PersonController(repository);
        }
    }
}
