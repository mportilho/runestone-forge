package com.runestone.dynafilter.performance;

import com.runestone.converters.impl.DefaultDataConversionService;
import com.runestone.dynafilter.core.generator.annotation.AnnotationStatementGenerator;
import com.runestone.dynafilter.modules.jpa.operation.SpecificationFilterOperationService;
import com.runestone.dynafilter.modules.jpa.resolver.SpecificationDynamicFilterResolver;
import com.runestone.dynafilter.modules.jpa.spring.SpecificationDynamicFilterArgumentResolver;
import com.runestone.dynafilter.modules.jpa.spring.SpringFilterDecoratorFactory;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.mockito.Mockito;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(1)
@Threads(1)
public class DynamicFilterResolverPerf06ProxyBenchmark {

    @Benchmark
    public void perf06_proxyInvocation_optimized_toPredicate(ProxyInvocationState state, Blackhole blackhole) {
        blackhole.consume(state.optimizedProxy.toPredicate(state.root, state.query, state.criteriaBuilder));
    }

    @Benchmark
    public void perf06_proxyInvocation_legacyReflective_toPredicate(ProxyInvocationState state, Blackhole blackhole) {
        blackhole.consume(state.legacyProxy.toPredicate(state.root, state.query, state.criteriaBuilder));
    }

    @State(Scope.Benchmark)
    public static class ProxyInvocationState {
        private MarkerSpecification<Object> optimizedProxy;
        private MarkerSpecification<Object> legacyProxy;
        private Root<Object> root;
        private CriteriaQuery<?> query;
        private CriteriaBuilder criteriaBuilder;

        @SuppressWarnings({"unchecked", "rawtypes"})
        @Setup(Level.Trial)
        public void setup() throws Exception {
            Specification<Object> targetSpecification = (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();

            SpecificationDynamicFilterArgumentResolver argumentResolver = createArgumentResolver();
            Method createProxyMethod = SpecificationDynamicFilterArgumentResolver.class
                    .getDeclaredMethod("createProxy", Specification.class, Class.class);
            createProxyMethod.setAccessible(true);
            this.optimizedProxy = (MarkerSpecification<Object>) createProxyMethod.invoke(
                    argumentResolver,
                    targetSpecification,
                    MarkerSpecification.class
            );

            this.legacyProxy = (MarkerSpecification<Object>) Proxy.newProxyInstance(
                    ClassUtils.getDefaultClassLoader(),
                    new Class[]{MarkerSpecification.class},
                    (proxy, method, args) -> method.invoke(targetSpecification, args)
            );

            this.root = Mockito.mock(Root.class);
            this.query = Mockito.mock(CriteriaQuery.class);
            this.criteriaBuilder = Mockito.mock(CriteriaBuilder.class);
            Predicate predicate = Mockito.mock(Predicate.class);
            Mockito.when(criteriaBuilder.conjunction()).thenReturn(predicate);
        }
    }

    private interface MarkerSpecification<T> extends Specification<T> {
    }

    private static SpecificationDynamicFilterArgumentResolver createArgumentResolver() {
        AnnotationStatementGenerator generator = new AnnotationStatementGenerator(null);
        SpecificationFilterOperationService operationService = new SpecificationFilterOperationService(new DefaultDataConversionService());
        SpecificationDynamicFilterResolver resolver = new SpecificationDynamicFilterResolver(operationService);
        GenericApplicationContext applicationContext = new GenericApplicationContext();
        applicationContext.refresh();
        SpringFilterDecoratorFactory decoratorFactory = new SpringFilterDecoratorFactory(applicationContext);
        return new SpecificationDynamicFilterArgumentResolver(generator, resolver, decoratorFactory);
    }
}
