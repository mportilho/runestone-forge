package com.runestone.dynafilter.perf.jmh;

import com.runestone.converters.impl.DefaultDataConversionService;
import com.runestone.dynafilter.core.generator.annotation.AnnotationStatementGenerator;
import com.runestone.dynafilter.core.generator.annotation.AnnotationStatementInput;
import com.runestone.dynafilter.core.generator.annotation.Conjunction;
import com.runestone.dynafilter.core.generator.annotation.Filter;
import com.runestone.dynafilter.core.operation.types.Equals;
import com.runestone.dynafilter.core.resolver.FilterDecorator;
import com.runestone.dynafilter.core.transformer.FilterValueContext;
import com.runestone.dynafilter.core.transformer.FilterValueTransformer;
import com.runestone.dynafilter.core.transformer.FilterValueTransformerRegistry;
import com.runestone.dynafilter.modules.jpa.api.JpaFilterOperationService;
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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(3)
public class FilterValueTransformerArgumentResolverBenchmark {

    @Benchmark
    public void resolveHttpArgument(ResolverState state, Blackhole blackhole) throws Exception {
        blackhole.consume(state.resolver.resolveArgument(state.parameter, null, state.request, null));
    }

    @State(Scope.Benchmark)
    public static class ResolverState {

        SpecificationDynamicFilterArgumentResolver resolver;
        MethodParameter parameter;
        ServletWebRequest request;

        @Setup
        public void setup() throws NoSuchMethodException {
            FilterValueTransformerRegistry registry = new FilterValueTransformerRegistry();
            registry.register(IdentityTransformer.class, new IdentityTransformer());
            AnnotationStatementGenerator generator = new AnnotationStatementGenerator(null, registry.toResolver());
            Method method = ControllerFixture.class.getDeclaredMethod("search", HttpFilter.class);
            parameter = new MethodParameter(method, 0);
            generator.warmup(new AnnotationStatementInput(
                    parameter.getParameterType(), parameter.getParameterAnnotations()));

            JpaFilterOperationService operationService = new JpaFilterOperationService(new DefaultDataConversionService());
            resolver = new SpecificationDynamicFilterArgumentResolver(
                    generator, new SpecificationDynamicFilterResolver(operationService),
                    input -> FilterDecorator.of(List.of()));
            MockHttpServletRequest servletRequest = new MockHttpServletRequest();
            servletRequest.addParameter("value", "alias");
            request = new ServletWebRequest(servletRequest);
        }
    }

    private static final class ControllerFixture {
        @SuppressWarnings("unused")
        void search(HttpFilter filter) {
        }
    }

    @Conjunction(@Filter(path = "target", parameters = "value", operation = Equals.class,
            transformers = IdentityTransformer.class))
    private interface HttpFilter extends Specification<Object> {
    }

    private static final class IdentityTransformer implements FilterValueTransformer {
        @Override
        public Object transform(Object value, FilterValueContext context) {
            return value;
        }
    }
}
