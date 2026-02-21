package com.runestone.dynafilter.performance;

import com.runestone.converters.impl.DefaultDataConversionService;
import com.runestone.dynafilter.core.generator.StatementWrapper;
import com.runestone.dynafilter.core.generator.annotation.AnnotationStatementGenerator;
import com.runestone.dynafilter.core.generator.annotation.AnnotationStatementInput;
import com.runestone.dynafilter.core.generator.annotation.TypeAnnotationUtils;
import com.runestone.dynafilter.core.generator.annotation.testquery.SearchPeopleAndGames;
import com.runestone.dynafilter.modules.jpa.operation.SpecificationFilterOperationService;
import com.runestone.dynafilter.modules.jpa.resolver.SpecificationDynamicFilterResolver;
import com.runestone.dynafilter.modules.jpa.resolver.tools.SearchMultiDataEmployees;
import com.runestone.dynafilter.modules.jpa.spring.SpecificationDynamicFilterArgumentResolver;
import com.runestone.dynafilter.modules.jpa.spring.SpringFilterDecoratorFactory;
import com.runestone.dynafilter.modules.jpa.spring.tools.SearchLanguages;
import com.runestone.dynafilter.modules.jpa.spring.tools.SearchState;
import jakarta.servlet.http.HttpServletRequest;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.mockito.Mockito;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.MethodParameter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 8, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(1)
@Threads(1)
public class DynamicFilterResolverBenchmark {

    @Benchmark
    public void statementGenerator_searchPeopleAndGames(StatementState state, Blackhole blackhole) {
        StatementWrapper statementWrapper = state.generator.generateStatements(state.input, state.parameters);
        blackhole.consume(statementWrapper.statement());
    }

    @Benchmark
    public void specificationResolver_createFilter(StatementState state, Blackhole blackhole) {
        blackhole.consume(state.resolver.createFilter(state.precomputedStatement, null));
    }

    @Benchmark
    public void argumentResolver_interfaceProxy(ArgumentResolverState state, Blackhole blackhole) {
        blackhole.consume(state.argumentResolver.resolveArgument(state.interfaceParameter, null, state.webRequest, null));
    }

    @Benchmark
    public void argumentResolver_fetchingDecorator(ArgumentResolverState state, Blackhole blackhole) {
        blackhole.consume(state.argumentResolver.resolveArgument(state.fetchingParameter, null, state.webRequest, null));
    }

    @Benchmark
    public void annotationUtils_reusedInput(AnnotationCacheState state, Blackhole blackhole) {
        blackhole.consume(TypeAnnotationUtils.findAnnotationData(state.sharedInput));
    }

    @Benchmark
    public void annotationUtils_newInputInstance(AnnotationCacheState state, Blackhole blackhole) {
        AnnotationStatementInput newInput = new AnnotationStatementInput(SearchPeopleAndGames.class, SearchPeopleAndGames.class.getAnnotations());
        blackhole.consume(TypeAnnotationUtils.findAnnotationData(newInput));
    }

    @State(Scope.Benchmark)
    public static class StatementState {
        private AnnotationStatementGenerator generator;
        private SpecificationDynamicFilterResolver resolver;
        private AnnotationStatementInput input;
        private Map<String, Object> parameters;
        private StatementWrapper precomputedStatement;

        @Setup(Level.Trial)
        public void setup() {
            generator = new AnnotationStatementGenerator(null);
            resolver = new SpecificationDynamicFilterResolver(new SpecificationFilterOperationService(new DefaultDataConversionService()));
            input = new AnnotationStatementInput(SearchPeopleAndGames.class, SearchPeopleAndGames.class.getAnnotations());
            parameters = Map.of(
                    "name", "English",
                    "documentNumber", "12345678900",
                    "all", "true",
                    "deleted", "false",
                    "genre", "RPG",
                    "state", "ON_USE",
                    "minCreationDate", "2021-01-01",
                    "maxCreationDate", "2021-12-31"
            );
            precomputedStatement = generator.generateStatements(input, parameters);
        }
    }

    @State(Scope.Benchmark)
    public static class AnnotationCacheState {
        private AnnotationStatementInput sharedInput;

        @Setup(Level.Trial)
        public void setup() {
            sharedInput = new AnnotationStatementInput(SearchPeopleAndGames.class, SearchPeopleAndGames.class.getAnnotations());
        }
    }

    @State(Scope.Benchmark)
    public static class ArgumentResolverState {
        private SpecificationDynamicFilterArgumentResolver argumentResolver;
        private MethodParameter interfaceParameter;
        private MethodParameter fetchingParameter;
        private NativeWebRequest webRequest;

        @SuppressWarnings({"rawtypes", "unchecked"})
        @Setup(Level.Trial)
        public void setup() {
            argumentResolver = createArgumentResolver();

            interfaceParameter = Mockito.mock(MethodParameter.class);
            Mockito.when(interfaceParameter.getParameterType()).thenReturn((Class) SearchState.class);
            Mockito.when(interfaceParameter.getParameterAnnotations()).thenReturn(SearchLanguages.class.getAnnotations());

            fetchingParameter = Mockito.mock(MethodParameter.class);
            Mockito.when(fetchingParameter.getParameterType()).thenReturn((Class) Specification.class);
            Mockito.when(fetchingParameter.getParameterAnnotations()).thenReturn(SearchMultiDataEmployees.class.getAnnotations());

            webRequest = createWebRequest();
        }
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

    private static NativeWebRequest createWebRequest() {
        NativeWebRequest webRequest = Mockito.mock(NativeWebRequest.class);
        HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(webRequest.getNativeRequest(HttpServletRequest.class)).thenReturn(httpServletRequest);
        Mockito.when(webRequest.getParameterMap()).thenReturn(Map.of(
                "name", new String[]{"English"},
                "state", new String[]{"ON_USE"},
                "minCreationDate", new String[]{"2021-01-01"},
                "maxCreationDate", new String[]{"2021-12-31"}
        ));
        Mockito.when(httpServletRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).thenReturn(Map.of(
                "resourceId", 10,
                "tenant", "main"
        ));
        return webRequest;
    }
}
