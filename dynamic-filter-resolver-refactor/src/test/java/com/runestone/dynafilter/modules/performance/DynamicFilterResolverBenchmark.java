package com.runestone.dynafilter.modules.performance;

import com.runestone.dynafilter.core.annotation.ConjunctionFrom;
import com.runestone.dynafilter.core.annotation.Filter;
import com.runestone.dynafilter.core.annotation.FilterTarget;
import com.runestone.dynafilter.core.decorator.FilterDecoratorFactory;
import com.runestone.dynafilter.core.generator.StatementWrapper;
import com.runestone.dynafilter.core.generator.annotation.AnnotationStatementGenerator;
import com.runestone.dynafilter.core.generator.annotation.AnnotationStatementInput;
import com.runestone.dynafilter.core.operation.Equals;
import com.runestone.dynafilter.core.operation.GreaterOrEquals;
import com.runestone.dynafilter.core.operation.IsIn;
import com.runestone.dynafilter.modules.jpa.resolver.SpecificationDynamicFilterResolver;
import com.runestone.dynafilter.modules.jpa.spring.SpecificationDynamicFilterArgumentResolver;
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
import org.springframework.core.MethodParameter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3)
@Measurement(iterations = 5)
@Fork(1)
public class DynamicFilterResolverBenchmark {

    @Benchmark
    public void generateStatements(StatementState state, Blackhole blackhole) {
        blackhole.consume(state.generator.generateStatements(state.input, state.parameters));
    }

    @Benchmark
    public void resolveSpecification(StatementState state, Blackhole blackhole) {
        blackhole.consume(state.resolver.createFilter(state.statementWrapper, null));
    }

    @Benchmark
    public void resolveMvcArgument(ArgumentResolverState state, Blackhole blackhole) throws Exception {
        blackhole.consume(state.argumentResolver.resolveArgument(state.methodParameter, null, state.webRequest, null));
    }

    @Benchmark
    public void reuseAnnotationInputCache(StatementState state, Blackhole blackhole) {
        blackhole.consume(com.runestone.dynafilter.core.generator.annotation.TypeAnnotationUtils.listAllFilterRequestData(state.input));
    }

    @State(Scope.Benchmark)
    public static class StatementState {

        AnnotationStatementGenerator generator;
        SpecificationDynamicFilterResolver resolver;
        AnnotationStatementInput input;
        Map<String, Object> parameters;
        StatementWrapper statementWrapper;

        @Setup
        public void setUp() throws Exception {
            generator = new AnnotationStatementGenerator();
            resolver = new SpecificationDynamicFilterResolver();
            Method method = BenchmarkController.class.getDeclaredMethod("list", org.springframework.data.jpa.domain.Specification.class);
            MethodParameter methodParameter = new MethodParameter(method, 0);
            input = new AnnotationStatementInput(methodParameter.getParameterType(), methodParameter.getParameterAnnotations());
            parameters = Map.of("name", "Ada", "height", "1.70", "tag", "vip");
            statementWrapper = generator.generateStatements(input, parameters);
        }
    }

    @State(Scope.Benchmark)
    public static class ArgumentResolverState {

        SpecificationDynamicFilterArgumentResolver argumentResolver;
        MethodParameter methodParameter;
        ServletWebRequest webRequest;

        @Setup
        public void setUp() throws Exception {
            AnnotationStatementGenerator generator = new AnnotationStatementGenerator();
            SpecificationDynamicFilterResolver resolver = new SpecificationDynamicFilterResolver();
            FilterDecoratorFactory decoratorFactory = input -> null;
            argumentResolver = new SpecificationDynamicFilterArgumentResolver(generator, resolver, decoratorFactory);
            Method method = BenchmarkController.class.getDeclaredMethod("list", org.springframework.data.jpa.domain.Specification.class);
            methodParameter = new MethodParameter(method, 0);
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addParameter("name", "Ada");
            request.addParameter("height", "1.70");
            request.addParameter("tag", "vip");
            webRequest = new ServletWebRequest(request);
        }
    }

    private static final class BenchmarkController {

        void list(@ConjunctionFrom(BenchmarkFilters.class) org.springframework.data.jpa.domain.Specification<BenchmarkEntity> specification) {
        }
    }

    @FilterTarget(BenchmarkEntity.class)
    private static final class BenchmarkFilters {

        @Filter(path = "name", parameters = "name", operation = Equals.class)
        private String name;

        @Filter(path = "height", parameters = "height", operation = GreaterOrEquals.class)
        private String height;

        @Filter(path = "tags", parameters = "tag", operation = IsIn.class)
        private String tag;
    }

    private static final class BenchmarkEntity {

        private String name;
        private BigDecimal height;
        private LocalDate birthday;
        private String[] tags;
    }
}
