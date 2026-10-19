package com.runestone.dynafilter.modules.jpa.repository;

import com.runestone.converters.impl.runtime.DefaultRuntimeDataConversionService;
import com.runestone.dynafilter.core.exceptions.InvalidSortException;
import com.runestone.dynafilter.core.exceptions.DynamicFilterConfigurationException;
import com.runestone.dynafilter.core.generator.ConditionalStatement;
import com.runestone.dynafilter.core.generator.SortContract;
import com.runestone.dynafilter.core.generator.annotation.AnnotationStatementGenerator;
import com.runestone.dynafilter.core.generator.annotation.AnnotationStatementInput;
import com.runestone.dynafilter.core.generator.annotation.Conjunction;
import com.runestone.dynafilter.core.generator.annotation.Filter;
import com.runestone.dynafilter.core.operation.types.Equals;
import com.runestone.dynafilter.modules.jpa.api.JpaFilterOperationService;
import com.runestone.dynafilter.modules.jpa.resolver.SpecificationDynamicFilterResolver;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Id;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Convert;
import jakarta.persistence.Converter;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.persistenceunit.PersistenceManagedTypes;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import java.util.Map;
import java.util.List;
import java.util.UUID;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TestJpaSortStorageTypesIntegration {

    @Test
    void xmlMappedLobsAreOmittedWithoutChangingTheFilterOrUnsortedQuery() {
        try (var factory = factory(); var em = factory.createEntityManager()) {
            var repository = repository(em);
            em.getTransaction().begin();
            repository.save(new StoredEntry(1L, "Zoe"));
            repository.save(new StoredEntry(2L, "Alice"));
            em.getTransaction().commit();
            var generator = new AnnotationStatementGenerator();
            var input = new AnnotationStatementInput(Search.class, null);
            var absent = new ConditionalStatement(generator.generateStatements(input, Map.of()), null);
            var active = new ConditionalStatement(generator.generateStatements(input, Map.of("query", "Alice")), null);

            assertThat(repository.findAll(absent, Sort.by("id"))).extracting(entry -> entry.id).containsExactly(1L, 2L);
            assertThat(repository.findAll(active)).extracting(entry -> entry.id).containsExactly(2L);
            for (String name : new String[]{"title", "query"}) {
                assertThatThrownBy(() -> repository.findAll(absent, Sort.by(name))).isExactlyInstanceOf(InvalidSortException.class);
            }
        }
    }

    @Test
    void convertedBinaryStorageIsOmittedAndExplicitDeclarationsCannotOverrideStorageRestrictions() {
        try (var factory = factory(); var em = factory.createEntityManager()) {
            var repository = repository(em);
            em.getTransaction().begin();
            repository.save(new StoredEntry(1L, "Zoe"));
            repository.save(new StoredEntry(2L, "Alice"));
            em.getTransaction().commit();
            var generator = new AnnotationStatementGenerator();
            var input = new AnnotationStatementInput(Search.class, null);
            var absent = new ConditionalStatement(generator.generateStatements(input, Map.of()), null);
            var active = new ConditionalStatement(generator.generateStatements(input, Map.of("encoded", "Alice")), null);

            assertThat(repository.findAll(active)).extracting(entry -> entry.id).containsExactly(2L);
            for (String name : List.of("encodedTitle", "encoded")) {
                assertThatThrownBy(() -> repository.findAll(absent, Sort.by(name))).isExactlyInstanceOf(InvalidSortException.class);
            }
            for (String path : List.of("title", "encodedTitle")) {
                var explicit = ConditionalStatement.builder().sortContract(SortContract.builder().include("id").include(path, "extra").build()).build();
                assertThatThrownBy(() -> repository.findAll(explicit, Sort.by("id")))
                        .isExactlyInstanceOf(DynamicFilterConfigurationException.class);
            }
        }
    }

    @Test
    void theSameContractUsesEachPersistenceUnitsEffectiveStorageMapping() {
        var generator = new AnnotationStatementGenerator();
        var statement = new ConditionalStatement(generator.generateStatements(new AnnotationStatementInput(Search.class, null), Map.of()), null);
        try (var plain = factory(false); var lob = factory(true);
             var plainEm = plain.createEntityManager(); var lobEm = lob.createEntityManager()) {
            var plainRepository = repository(plainEm);
            var lobRepository = repository(lobEm);
            plainEm.getTransaction().begin();
            plainRepository.save(new StoredEntry(1L, "Zoe"));
            plainRepository.save(new StoredEntry(2L, "Alice"));
            plainEm.getTransaction().commit();

            assertThat(plainRepository.findAll(statement, Sort.by("query"))).extracting(entry -> entry.id).containsExactly(2L, 1L);
            assertThatThrownBy(() -> lobRepository.findAll(statement, Sort.by("query"))).isExactlyInstanceOf(InvalidSortException.class);
            assertThat(plainRepository.findAll(statement, Sort.by("query"))).extracting(entry -> entry.id).containsExactly(2L, 1L);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private DynamicFilterJpaRepositoryImpl<StoredEntry, Long> repository(EntityManager em) {
        var repository = new DynamicFilterJpaRepositoryImpl<StoredEntry, Long>(
                JpaEntityInformationSupport.getEntityInformation(StoredEntry.class, em), em);
        repository.setDynamicFilterResolver((com.runestone.dynafilter.core.resolver.DynamicFilterResolver)
                new SpecificationDynamicFilterResolver(new JpaFilterOperationService(DefaultRuntimeDataConversionService.standard())));
        return repository;
    }

    private EntityManagerFactory factory() {
        return factory(true);
    }

    private EntityManagerFactory factory(boolean xmlLob) {
        var bean = new LocalContainerEntityManagerFactoryBean();
        bean.setPersistenceUnitName("sort-storage-" + UUID.randomUUID());
        bean.setDataSource(new DriverManagerDataSource("jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1", "sa", ""));
        bean.setManagedTypes(PersistenceManagedTypes.of(StoredEntry.class.getName()));
        bean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        bean.setJpaPropertyMap(Map.of("hibernate.hbm2ddl.auto", "create-drop"));
        if (xmlLob) {
            bean.setMappingResources("sort-storage-types.xml");
        }
        bean.afterPropertiesSet();
        return bean.getObject();
    }

    @Conjunction({
            @Filter(path = "title", parameters = "query", operation = Equals.class),
            @Filter(path = "encodedTitle", parameters = "encoded", operation = Equals.class),
            @Filter(path = "id", parameters = "id", operation = Equals.class)
    })
    interface Search {
    }

    @Entity(name = "SortStoredEntry")
    public static class StoredEntry {
        @Id
        private Long id;
        private String title;
        @Convert(converter = BinaryTitleConverter.class)
        private String encodedTitle;

        protected StoredEntry() {
        }

        StoredEntry(Long id, String title) {
            this.id = id;
            this.title = title;
            this.encodedTitle = title;
        }
    }

    @Converter
    public static final class BinaryTitleConverter implements AttributeConverter<String, byte[]> {
        @Override
        public byte[] convertToDatabaseColumn(String value) {
            return value != null ? value.getBytes(StandardCharsets.UTF_8) : null;
        }

        @Override
        public String convertToEntityAttribute(byte[] value) {
            return value != null ? new String(value, StandardCharsets.UTF_8) : null;
        }
    }
}
