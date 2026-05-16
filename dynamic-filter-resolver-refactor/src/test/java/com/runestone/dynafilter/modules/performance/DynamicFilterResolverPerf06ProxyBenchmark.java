package com.runestone.dynafilter.modules.performance;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.springframework.data.jpa.domain.Specification;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.mock;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3)
@Measurement(iterations = 5)
@Fork(1)
public class DynamicFilterResolverPerf06ProxyBenchmark {

    @Benchmark
    public void reflectiveProxyInvocation(ProxyInvocationState state, Blackhole blackhole) {
        blackhole.consume(state.reflectiveProxy.toPredicate(state.root, state.query, state.criteriaBuilder));
    }

    @Benchmark
    public void directProxyInvocation(ProxyInvocationState state, Blackhole blackhole) {
        blackhole.consume(state.directProxy.toPredicate(state.root, state.query, state.criteriaBuilder));
    }

    @State(Scope.Benchmark)
    public static class ProxyInvocationState {

        Specification<Object> delegate;
        Specification<Object> reflectiveProxy;
        Specification<Object> directProxy;
        Root<Object> root;
        CriteriaQuery<?> query;
        CriteriaBuilder criteriaBuilder;

        @Setup
        @SuppressWarnings("unchecked")
        public void setUp() {
            Predicate predicate = mock(Predicate.class);
            delegate = (root, query, criteriaBuilder) -> predicate;
            reflectiveProxy = (Specification<Object>) Proxy.newProxyInstance(
                    Specification.class.getClassLoader(),
                    new Class<?>[]{Specification.class},
                    new ReflectiveInvocationHandler(delegate)
            );
            directProxy = (Specification<Object>) Proxy.newProxyInstance(
                    Specification.class.getClassLoader(),
                    new Class<?>[]{Specification.class},
                    new DirectInvocationHandler(delegate)
            );
            root = mock(Root.class);
            query = mock(CriteriaQuery.class);
            criteriaBuilder = mock(CriteriaBuilder.class);
        }
    }

    private record ReflectiveInvocationHandler(Specification<Object> delegate) implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return method.invoke(delegate, args);
        }
    }

    private record DirectInvocationHandler(Specification<Object> delegate) implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            if (method.getName().equals("toPredicate")) {
                return delegate.toPredicate((Root<Object>) args[0], (CriteriaQuery<?>) args[1], (CriteriaBuilder) args[2]);
            }
            throw new UnsupportedOperationException("Only toPredicate is part of the measured contract");
        }
    }
}
