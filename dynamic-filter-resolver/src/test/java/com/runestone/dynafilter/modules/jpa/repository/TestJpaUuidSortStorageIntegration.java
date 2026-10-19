package com.runestone.dynafilter.modules.jpa.repository;

import com.runestone.converters.impl.runtime.DefaultRuntimeDataConversionService;
import com.runestone.dynafilter.core.exceptions.DynamicFilterConfigurationException;
import com.runestone.dynafilter.core.exceptions.InvalidSortException;
import com.runestone.dynafilter.core.generator.ConditionalStatement;
import com.runestone.dynafilter.core.generator.SortContract;
import com.runestone.dynafilter.core.generator.annotation.AnnotationStatementGenerator;
import com.runestone.dynafilter.core.generator.annotation.AnnotationStatementInput;
import com.runestone.dynafilter.core.generator.annotation.Conjunction;
import com.runestone.dynafilter.core.generator.annotation.Filter;
import com.runestone.dynafilter.core.generator.annotation.SortAlias;
import com.runestone.dynafilter.core.generator.annotation.SortPolicy;
import com.runestone.dynafilter.core.operation.types.Equals;
import com.runestone.dynafilter.modules.jpa.api.JpaConditionalStatements;
import com.runestone.dynafilter.modules.jpa.api.JpaFilterOperationService;
import com.runestone.dynafilter.modules.jpa.resolver.SpecificationDynamicFilterResolver;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Convert;
import jakarta.persistence.Converter;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.persistenceunit.PersistenceManagedTypes;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TestJpaUuidSortStorageIntegration {
    private static final UUID FIRST = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID SECOND = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID THIRD = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final UUID FOURTH = UUID.fromString("00000000-0000-0000-0000-000000000004");

    @ParameterizedTest
    @ValueSource(strings = {"BINARY", "CHAR"})
    void uuidFilterPublishesBothAliasAndExactPathWithoutAFilterValue(String storage) {
        try (var factory = factory(storage); var em = factory.createEntityManager()) {
            var repository = repository(em);
            em.getTransaction().begin();
            em.persist(new UuidEntry(SECOND));
            em.persist(new UuidEntry(FIRST));
            em.getTransaction().commit();
            em.clear();
            var statement = statement(UuidSearch.class);

            assertThat(repository.findAll(statement, Sort.by("identifier")))
                    .extracting(entry -> entry.id).containsExactly(FIRST, SECOND);
            assertThat(repository.findAll(statement, Sort.by(Sort.Direction.DESC, "id")))
                    .extracting(entry -> entry.id).containsExactly(SECOND, FIRST);
        }
    }

    @Test
    void binaryUuidCanBeDeclaredOnlyForSortingWithoutAFictitiousFilter() {
        try (var factory = factory("BINARY"); var em = factory.createEntityManager()) {
            var repository = repository(em);
            em.getTransaction().begin();
            em.persist(new UuidEntry(SECOND));
            em.persist(new UuidEntry(FIRST));
            em.getTransaction().commit();
            em.clear();
            var statement = statement(SortOnlyUuidSearch.class);

            assertThat(statement.statementWrapper().allFilters()).isEmpty();
            assertThat(repository.findAll(statement, Sort.by("id")))
                    .extracting(entry -> entry.id).containsExactly(FIRST, SECOND);
            assertThat(repository.findAll(statement, Sort.by(Sort.Direction.DESC, "identifier")))
                    .extracting(entry -> entry.id).containsExactly(SECOND, FIRST);
        }
    }

    @Test
    void binaryUuidInASingularAssociationPublishesItsAliasAndExactPath() {
        try (var factory = factory("BINARY"); var em = factory.createEntityManager()) {
            var repository = repository(em);
            var firstOwner = new UuidOwner(FIRST);
            var secondOwner = new UuidOwner(SECOND);
            em.getTransaction().begin();
            em.persist(firstOwner);
            em.persist(secondOwner);
            em.persist(new UuidEntry(FIRST, secondOwner));
            em.persist(new UuidEntry(SECOND, firstOwner));
            em.getTransaction().commit();
            em.clear();
            var statement = statement(OwnerUuidSearch.class);

            assertThat(repository.findAll(statement, Sort.by("ownerIdentifier")))
                    .extracting(entry -> entry.id).containsExactly(SECOND, FIRST);
            assertThat(repository.findAll(statement, Sort.by(Sort.Direction.DESC, "owner.id")))
                    .extracting(entry -> entry.id).containsExactly(FIRST, SECOND);
        }
    }

    @Test
    void binaryUuidTieBreakerPreservesScopePaginationCountAndGraphs() {
        try (var factory = factory("BINARY"); var em = factory.createEntityManager()) {
            var repository = repository(em);
            var owner = new UuidOwner(FIRST);
            var outside = new UuidEntry(FIRST, owner);
            outside.scope = "outside";
            em.getTransaction().begin();
            em.persist(owner);
            em.persist(outside);
            em.persist(new UuidEntry(FOURTH, owner));
            em.persist(new UuidEntry(THIRD, owner));
            em.persist(new UuidEntry(SECOND, owner));
            em.getTransaction().commit();
            var source = statement(PageUuidSearch.class);
            var statement = JpaConditionalStatements.<UuidEntry>restrict(source,
                    (root, query, builder) -> builder.equal(root.get("scope"), "allowed"));
            var graph = em.createEntityGraph(UuidEntry.class);
            graph.addAttributeNodes("owner");
            factory.addNamedEntityGraph("uuid-sort-owner", graph);

            for (boolean useGraph : List.of(false, true)) {
                var expected = List.of(SECOND, THIRD, FOURTH);
                for (int pageNumber = 0; pageNumber < expected.size(); pageNumber++) {
                    em.clear();
                    var pageable = PageRequest.of(pageNumber, 1, Sort.by("query", "id"));
                    var page = useGraph ? repository.findAll(statement, pageable, "uuid-sort-owner")
                            : repository.findAll(statement, pageable);
                    assertThat(page.getContent()).extracting(entry -> entry.id).containsExactly(expected.get(pageNumber));
                    assertThat(page.getTotalElements()).isEqualTo(3L);
                    assertThat(page.getNumber()).isEqualTo(pageNumber);
                    assertThat(page.getSort()).isEqualTo(Sort.by("label", "id"));
                    if (useGraph) {
                        assertThat(factory.getPersistenceUnitUtil().isLoaded(page.getContent().getFirst(), "owner")).isTrue();
                    }
                }
            }
            assertThat(repository.count(statement)).isEqualTo(3L);
            assertThat(repository.count(source)).isEqualTo(4L);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"BINARY", "CHAR"})
    void uuidDomainConvertedToBytesRemainsSortableByAliasAndExactPath(String storage) {
        try (var factory = factory(storage); var em = factory.createEntityManager()) {
            var repository = repository(em);
            em.getTransaction().begin();
            em.persist(new UuidEntry(SECOND));
            em.persist(new UuidEntry(FIRST));
            em.getTransaction().commit();
            em.clear();
            var statement = statement(ConvertedUuidSearch.class);

            assertThat(repository.findAll(statement, Sort.by("converted")))
                    .extracting(entry -> entry.id).containsExactly(FIRST, SECOND);
            assertThat(repository.findAll(statement, Sort.by(Sort.Direction.DESC, "convertedId")))
                    .extracting(entry -> entry.id).containsExactly(SECOND, FIRST);
        }
    }

    @Test
    void binaryStorageOfAStringDomainIsRejectedEvenWhenItsRelationalTypeIsUuid() {
        try (var factory = factory("BINARY"); var em = factory.createEntityManager()) {
            var repository = repository(em);
            var statement = statement(RelationalUuidSearch.class);
            var statistics = factory.unwrap(SessionFactory.class).getStatistics();
            statistics.clear();

            for (String name : List.of("uuidText", "textIdentifier")) {
                assertThatThrownBy(() -> repository.findAll(statement, Sort.by(name)))
                        .isExactlyInstanceOf(InvalidSortException.class);
            }
            var explicit = ConditionalStatement.builder().sortContract(SortContract.builder().include("uuidText").build()).build();
            assertThatThrownBy(() -> repository.findAll(explicit, Sort.by("uuidText")))
                    .isExactlyInstanceOf(DynamicFilterConfigurationException.class);
            assertThat(statistics.getPrepareStatementCount()).isZero();
        }
    }

    @Test
    void charStoragePreservesSortableStringDomainsConvertedToUuid() {
        try (var factory = factory("CHAR"); var em = factory.createEntityManager()) {
            var repository = repository(em);
            em.getTransaction().begin();
            em.persist(new UuidEntry(SECOND));
            em.persist(new UuidEntry(FIRST));
            em.getTransaction().commit();
            em.clear();
            var statement = statement(RelationalUuidSearch.class);

            assertThat(repository.findAll(statement, Sort.by("textIdentifier")))
                    .extracting(entry -> entry.id).containsExactly(FIRST, SECOND);
            assertThat(repository.findAll(statement, Sort.by(Sort.Direction.DESC, "uuidText")))
                    .extracting(entry -> entry.id).containsExactly(SECOND, FIRST);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"BINARY", "CHAR"})
    void undeclaredUuidsAndUnsafeSortsAreRejectedBeforeSqlEvenWithAnInternalScope(String storage) {
        try (var factory = factory(storage); var em = factory.createEntityManager()) {
            var repository = repository(em);
            var source = statement(NameSearch.class);
            var scoped = JpaConditionalStatements.<UuidEntry>restrict(source,
                    (root, query, builder) -> builder.equal(root.get("id"), FIRST));
            var statistics = factory.unwrap(SessionFactory.class).getStatistics();
            statistics.clear();

            for (var statement : List.of(source, scoped)) {
                for (var sort : List.of(Sort.by("query", "id"), Sort.by("identifier"), Sort.by("missing"),
                        Sort.by("length(id)"), JpaSort.unsafe("id"), JpaSort.unsafe("label"))) {
                    assertThatThrownBy(() -> repository.findAll(statement, sort)).isExactlyInstanceOf(InvalidSortException.class);
                    assertThatThrownBy(() -> repository.findAll(statement, PageRequest.of(0, 1, sort)))
                            .isExactlyInstanceOf(InvalidSortException.class);
                }
            }
            assertThat(statistics.getPrepareStatementCount()).isZero();
        }
    }

    @Test
    void canonicalUuidExclusionsStillRemoveEveryAlias() {
        try (var factory = factory("BINARY"); var em = factory.createEntityManager()) {
            var repository = repository(em);
            var statement = statement(ExcludedUuidSearch.class);
            assertThat(repository.findAll(statement, Sort.by("query"))).isEmpty();
            var statistics = factory.unwrap(SessionFactory.class).getStatistics();
            statistics.clear();

            for (String name : List.of("id", "identifier", "convertedId", "converted")) {
                assertThatThrownBy(() -> repository.findAll(statement, Sort.by(name)))
                        .isExactlyInstanceOf(InvalidSortException.class);
            }
            assertThat(statistics.getPrepareStatementCount()).isZero();
        }
    }

    @Test
    void uuidDomainDoesNotOverrideTheLobVetoForAutomaticOrExplicitDeclarations() {
        try (var factory = factory("BINARY"); var em = factory.createEntityManager()) {
            var repository = repository(em);
            var statement = statement(LobUuidSearch.class);
            var statistics = factory.unwrap(SessionFactory.class).getStatistics();
            statistics.clear();

            for (String name : List.of("lobId", "lobIdentifier")) {
                assertThatThrownBy(() -> repository.findAll(statement, Sort.by(name)))
                        .isExactlyInstanceOf(InvalidSortException.class);
            }
            var explicit = ConditionalStatement.builder().sortContract(SortContract.builder().include("lobId").build()).build();
            assertThatThrownBy(() -> repository.findAll(explicit, Sort.by("lobId")))
                    .isExactlyInstanceOf(DynamicFilterConfigurationException.class);
            assertThat(statistics.getPrepareStatementCount()).isZero();
        }
    }

    @Test
    void theSameContractKeepsBinaryAndCharPersistenceUnitEligibilityIsolated() {
        var statement = statement(RelationalUuidSearch.class);
        try (var binary = factory("BINARY"); var chars = factory("CHAR");
             var binaryEm = binary.createEntityManager(); var charEm = chars.createEntityManager()) {
            var binaryRepository = repository(binaryEm);
            var charRepository = repository(charEm);
            charEm.getTransaction().begin();
            charEm.persist(new UuidEntry(SECOND));
            charEm.persist(new UuidEntry(FIRST));
            charEm.getTransaction().commit();
            charEm.clear();

            assertThat(charRepository.findAll(statement, Sort.by("textIdentifier")))
                    .extracting(entry -> entry.id).containsExactly(FIRST, SECOND);
            assertThatThrownBy(() -> binaryRepository.findAll(statement, Sort.by("textIdentifier")))
                    .isExactlyInstanceOf(InvalidSortException.class);
            assertThat(charRepository.findAll(statement, Sort.by("textIdentifier")))
                    .extracting(entry -> entry.id).containsExactly(FIRST, SECOND);
        }
    }

    private ConditionalStatement statement(Class<?> search) {
        var generator = new AnnotationStatementGenerator();
        return new ConditionalStatement(generator.generateStatements(new AnnotationStatementInput(search, null), Map.of()), null);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private DynamicFilterJpaRepositoryImpl<UuidEntry, UUID> repository(EntityManager em) {
        var repository = new DynamicFilterJpaRepositoryImpl<UuidEntry, UUID>(
                JpaEntityInformationSupport.getEntityInformation(UuidEntry.class, em), em);
        repository.setDynamicFilterResolver((com.runestone.dynafilter.core.resolver.DynamicFilterResolver)
                new SpecificationDynamicFilterResolver(new JpaFilterOperationService(DefaultRuntimeDataConversionService.standard())));
        return repository;
    }

    private EntityManagerFactory factory(String uuidJdbcType) {
        var bean = new LocalContainerEntityManagerFactoryBean();
        bean.setPersistenceUnitName("uuid-sort-" + UUID.randomUUID());
        bean.setDataSource(new DriverManagerDataSource("jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1", "sa", ""));
        bean.setManagedTypes(PersistenceManagedTypes.of(UuidEntry.class.getName(), UuidOwner.class.getName()));
        bean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        bean.setJpaPropertyMap(Map.of("hibernate.hbm2ddl.auto", "create-drop",
                "hibernate.type.preferred_uuid_jdbc_type", uuidJdbcType,
                "hibernate.generate_statistics", "true"));
        bean.afterPropertiesSet();
        return bean.getObject();
    }

    @Conjunction(@Filter(path = "id", parameters = "identifier", operation = Equals.class))
    interface UuidSearch {
    }

    @SortPolicy(include = "id", aliases = @SortAlias(name = "identifier", path = "id"))
    interface SortOnlyUuidSearch {
    }

    @Conjunction(@Filter(path = "owner.id", parameters = "ownerIdentifier", operation = Equals.class))
    interface OwnerUuidSearch {
    }

    @Conjunction(@Filter(path = "label", parameters = "query", operation = Equals.class))
    interface NameSearch {
    }

    @SortPolicy(include = "id")
    interface PageUuidSearch extends NameSearch {
    }

    @Conjunction(@Filter(path = "convertedId", parameters = "converted", operation = Equals.class))
    interface ConvertedUuidSearch {
    }

    @Conjunction(@Filter(path = "uuidText", parameters = "textIdentifier", operation = Equals.class))
    interface RelationalUuidSearch {
    }

    @SortPolicy(exclude = {"id", "convertedId"})
    interface ExcludedUuidSearch extends UuidSearch, ConvertedUuidSearch, NameSearch {
    }

    @Conjunction(@Filter(path = "lobId", parameters = "lobIdentifier", operation = Equals.class))
    interface LobUuidSearch {
    }

    @Entity(name = "SortUuidEntry")
    public static class UuidEntry {
        @Id
        private UUID id;
        @ManyToOne(fetch = FetchType.LAZY)
        private UuidOwner owner;
        private String label = "same";
        private String scope = "allowed";
        @Convert(converter = UuidBinaryConverter.class)
        private UUID convertedId;
        @Convert(converter = StringUuidConverter.class)
        private String uuidText;
        @Lob
        @Convert(converter = UuidBinaryConverter.class)
        private UUID lobId;

        protected UuidEntry() {
        }

        UuidEntry(UUID id) {
            this(id, null);
        }

        UuidEntry(UUID id, UuidOwner owner) {
            this.id = id;
            this.owner = owner;
            this.convertedId = id;
            this.uuidText = id.toString();
        }
    }

    @Entity(name = "SortUuidOwner")
    public static class UuidOwner {
        @Id
        private UUID id;

        protected UuidOwner() {
        }

        UuidOwner(UUID id) {
            this.id = id;
        }
    }

    @Converter
    public static final class UuidBinaryConverter implements AttributeConverter<UUID, byte[]> {
        @Override
        public byte[] convertToDatabaseColumn(UUID value) {
            return value == null ? null : ByteBuffer.allocate(16)
                    .putLong(value.getMostSignificantBits()).putLong(value.getLeastSignificantBits()).array();
        }

        @Override
        public UUID convertToEntityAttribute(byte[] value) {
            if (value == null) {
                return null;
            }
            var buffer = ByteBuffer.wrap(value);
            return new UUID(buffer.getLong(), buffer.getLong());
        }
    }

    @Converter
    public static final class StringUuidConverter implements AttributeConverter<String, UUID> {
        @Override
        public UUID convertToDatabaseColumn(String value) {
            return value == null ? null : UUID.fromString(value);
        }

        @Override
        public String convertToEntityAttribute(UUID value) {
            return value == null ? null : value.toString();
        }
    }
}
