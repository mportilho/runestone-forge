package com.runestone.dynafilter.performance;

import com.runestone.converters.impl.DefaultDataConversionService;
import com.runestone.dynafilter.core.generator.annotation.AnnotationStatementInput;
import com.runestone.dynafilter.core.generator.annotation.TypeAnnotationUtils;
import com.runestone.dynafilter.core.model.FilterData;
import com.runestone.dynafilter.core.model.statement.AbstractStatement;
import com.runestone.dynafilter.core.model.statement.CompoundStatement;
import com.runestone.dynafilter.core.model.statement.LogicOperator;
import com.runestone.dynafilter.core.model.statement.LogicalStatement;
import com.runestone.dynafilter.core.operation.types.Equals;
import com.runestone.dynafilter.core.resolver.FilterDecorator;
import com.runestone.dynafilter.modules.jpa.operation.SpecificationFilterOperationService;
import com.runestone.dynafilter.modules.jpa.resolver.Fetching;
import com.runestone.dynafilter.modules.jpa.resolver.FetchingFilterDecorator;
import com.runestone.dynafilter.modules.jpa.resolver.SpecificationStatementAnalyser;
import com.runestone.dynafilter.modules.jpa.tools.app.database.InMemoryDatabaseApplication;
import com.runestone.dynafilter.modules.jpa.tools.app.database.jpamodels.Person;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.domain.Specification;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(1)
@Threads(1)
public class DynamicFilterResolverPerf02Benchmark {

    @Benchmark
    public void perf02_specification_toPredicate_manyFilters(JpaPredicateState state, Blackhole blackhole) {
        CriteriaBuilder criteriaBuilder = state.entityManager.getCriteriaBuilder();
        CriteriaQuery<Person> query = criteriaBuilder.createQuery(Person.class);
        Root<Person> root = query.from(Person.class);

        Predicate predicate = state.heavySpecification.toPredicate(root, query, criteriaBuilder);
        blackhole.consume(predicate);
    }

    @Benchmark
    public void perf02_specification_toPredicate_repeatedNestedPath(JpaPredicateState state, Blackhole blackhole) {
        CriteriaBuilder criteriaBuilder = state.entityManager.getCriteriaBuilder();
        CriteriaQuery<Person> query = criteriaBuilder.createQuery(Person.class);
        Root<Person> root = query.from(Person.class);

        Predicate predicate = state.repeatedNestedPathSpecification.toPredicate(root, query, criteriaBuilder);
        blackhole.consume(predicate);
    }

    @Benchmark
    public void perf02_fetchingDecorator_deepPaths(JpaPredicateState state, Blackhole blackhole) {
        CriteriaBuilder criteriaBuilder = state.entityManager.getCriteriaBuilder();
        CriteriaQuery<Person> query = criteriaBuilder.createQuery(Person.class);
        Root<Person> root = query.from(Person.class);

        Predicate predicate = state.deepFetchingSpecification.toPredicate(root, query, criteriaBuilder);
        blackhole.consume(predicate);
        blackhole.consume(root.getFetches().size());
    }

    @Benchmark
    public void perf02_fetchingDecorator_overlappingPaths(JpaPredicateState state, Blackhole blackhole) {
        CriteriaBuilder criteriaBuilder = state.entityManager.getCriteriaBuilder();
        CriteriaQuery<Person> query = criteriaBuilder.createQuery(Person.class);
        Root<Person> root = query.from(Person.class);

        Predicate predicate = state.overlappingFetchingSpecification.toPredicate(root, query, criteriaBuilder);
        blackhole.consume(predicate);
        blackhole.consume(root.getFetches().size());
    }

    @Benchmark
    public void perf02_annotationUtils_reusedInput_afterCacheGrowth(CacheGrowthState state, Blackhole blackhole) {
        blackhole.consume(TypeAnnotationUtils.findAnnotationData(state.reusedInput));
    }

    @Benchmark
    public void perf02_annotationUtils_newEquivalentInput_afterCacheGrowth(CacheGrowthState state, Blackhole blackhole) {
        AnnotationStatementInput newEquivalentInput =
                new AnnotationStatementInput(InMemoryDatabaseApplication.class, InMemoryDatabaseApplication.class.getAnnotations());
        blackhole.consume(TypeAnnotationUtils.findAnnotationData(newEquivalentInput));
    }

    @State(Scope.Benchmark)
    public static class JpaPredicateState {

        private ConfigurableApplicationContext applicationContext;
        private EntityManager entityManager;

        private Specification<Person> heavySpecification;
        private Specification<Person> repeatedNestedPathSpecification;
        private Specification<Person> deepFetchingSpecification;
        private Specification<Person> overlappingFetchingSpecification;

        @Setup(Level.Trial)
        @SuppressWarnings({"unchecked", "rawtypes"})
        public void setup() {
            this.applicationContext = new SpringApplicationBuilder(InMemoryDatabaseApplication.class)
                    .web(WebApplicationType.NONE)
                    .properties(
                            "spring.datasource.url=jdbc:h2:mem:perf02-bench;DB_CLOSE_DELAY=-1;MODE=LEGACY",
                            "spring.datasource.driverClassName=org.h2.Driver",
                            "spring.datasource.username=sa",
                            "spring.datasource.password=",
                            "spring.jpa.hibernate.ddl-auto=create-drop",
                            "spring.jpa.show-sql=false",
                            "spring.main.lazy-initialization=true",
                            "logging.level.root=ERROR"
                    )
                    .run();

            EntityManagerFactory emf = applicationContext.getBean(EntityManagerFactory.class);
            this.entityManager = emf.createEntityManager();

            SpecificationFilterOperationService operationService = new SpecificationFilterOperationService(new DefaultDataConversionService());
            SpecificationStatementAnalyser analyser = new SpecificationStatementAnalyser(operationService);

            AbstractStatement statement = createHeavyConjunction(50);
            this.heavySpecification = (Specification<Person>) statement.acceptAnalyser(analyser);
            AbstractStatement repeatedPathStatement = createRepeatedNestedConjunction(50);
            this.repeatedNestedPathSpecification = (Specification<Person>) repeatedPathStatement.acceptAnalyser(analyser);

            Fetching[] fetchings = DeepFetchPaths.class.getAnnotationsByType(Fetching.class);
            FilterDecorator<Specification<?>> decorator = new FetchingFilterDecorator(Arrays.asList(fetchings));
            Specification<Person> baseSpec = (root, query, cb) -> cb.like(root.get("name"), "J%");
            this.deepFetchingSpecification = (Specification<Person>) decorator.decorate(baseSpec, null);
            Fetching[] overlappingFetchings = OverlappingFetchPaths.class.getAnnotationsByType(Fetching.class);
            FilterDecorator<Specification<?>> overlappingDecorator = new FetchingFilterDecorator(Arrays.asList(overlappingFetchings));
            this.overlappingFetchingSpecification = (Specification<Person>) overlappingDecorator.decorate(baseSpec, null);
        }

        @TearDown(Level.Trial)
        public void tearDown() {
            if (entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
            if (applicationContext != null) {
                applicationContext.close();
            }
        }

        private static AbstractStatement createHeavyConjunction(int totalFilters) {
            List<FilterData> filters = new ArrayList<>(totalFilters);
            for (int i = 0; i < totalFilters; i++) {
                int pathIdx = i % 10;
                FilterData filterData = switch (pathIdx) {
                    case 0 -> createFilterData("name", "name" + i);
                    case 1 -> createFilterData("addresses.street", "street" + i);
                    case 2 -> createFilterData("addresses.number", Integer.toString(100 + i));
                    case 3 -> createFilterData("addresses.location.city", "city" + i);
                    case 4 -> createFilterData("addresses.location.state", "ST" + (i % 27));
                    case 5 -> createFilterData("phones.number", "11999" + i);
                    case 6 -> createFilterData("height", new BigDecimal("180.50"));
                    case 7 -> createFilterData("weight", new BigDecimal("82.40"));
                    case 8 -> createFilterData("birthday", LocalDate.of(1990, 1, 1));
                    default -> createFilterData("registerDate", LocalDateTime.of(2024, 1, 1, 10, 30));
                };
                filters.add(filterData);
            }

            AbstractStatement statement = new LogicalStatement(filters.getFirst());
            for (int i = 1; i < filters.size(); i++) {
                statement = new CompoundStatement(statement, new LogicalStatement(filters.get(i)), LogicOperator.CONJUNCTION);
            }
            return statement;
        }

        private static AbstractStatement createRepeatedNestedConjunction(int totalFilters) {
            List<FilterData> filters = new ArrayList<>(totalFilters);
            for (int i = 0; i < totalFilters; i++) {
                FilterData filterData;
                if (i % 2 == 0) {
                    filterData = createFilterData("addresses.location.state", "state-" + i);
                } else {
                    filterData = createFilterData("addresses.location.city", "city-" + i);
                }
                filters.add(filterData);
            }

            AbstractStatement statement = new LogicalStatement(filters.getFirst());
            for (int i = 1; i < filters.size(); i++) {
                statement = new CompoundStatement(statement, new LogicalStatement(filters.get(i)), LogicOperator.CONJUNCTION);
            }
            return statement;
        }

        private static FilterData createFilterData(String path, Object value) {
            return new FilterData(
                    path,
                    new String[]{path.replace('.', '_')},
                    null,
                    Equals.class,
                    false,
                    new Object[]{value},
                    List.of(),
                    null
            );
        }
    }

    @State(Scope.Benchmark)
    public static class CacheGrowthState {

        private AnnotationStatementInput reusedInput;

        @Setup(Level.Trial)
        public void setup() {
            TypeAnnotationUtils.clearCaches();
            this.reusedInput = new AnnotationStatementInput(InMemoryDatabaseApplication.class, InMemoryDatabaseApplication.class.getAnnotations());

            for (int i = 0; i < 20_000; i++) {
                Annotation[] syntheticAnnotations = new Annotation[]{new SyntheticAnnotation(i)};
                AnnotationStatementInput input = new AnnotationStatementInput(null, syntheticAnnotations);
                TypeAnnotationUtils.findAnnotationData(input);
            }

            TypeAnnotationUtils.findAnnotationData(reusedInput);
        }

        @TearDown(Level.Trial)
        public void tearDown() {
            TypeAnnotationUtils.clearCaches();
        }
    }

    @Fetching({"addresses", "addresses.location", "phones"})
    @Fetching(value = {"addresses.location"}, joinType = jakarta.persistence.criteria.JoinType.INNER)
    @Fetching(value = {"addresses.person"}, joinType = jakarta.persistence.criteria.JoinType.LEFT)
    @Target(TYPE)
    @Retention(RUNTIME)
    private @interface DeepFetchPaths {
    }

    @Fetching("addresses")
    @Fetching({"addresses.location", "addresses.person"})
    @Fetching({"phones", "phones.person"})
    @Fetching("addresses.location")
    @Target(TYPE)
    @Retention(RUNTIME)
    private @interface OverlappingFetchPaths {
    }

    @Retention(RUNTIME)
    private @interface SyntheticMarker {
        int value();
    }

    private static final class SyntheticAnnotation implements Annotation {

        private final int value;

        private SyntheticAnnotation(int value) {
            this.value = value;
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return SyntheticMarker.class;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof SyntheticAnnotation other)) {
                return false;
            }
            return value == other.value;
        }

        @Override
        public int hashCode() {
            return 127 * "value".hashCode() ^ Integer.hashCode(value);
        }

        @Override
        public String toString() {
            return "@SyntheticMarker(value=" + value + ')';
        }
    }
}
