package com.runestone.dynafilter.core.generator.annotation;

import com.runestone.dynafilter.core.exceptions.DynamicFilterConfigurationException;
import com.runestone.dynafilter.core.exceptions.StatementGenerationException;
import com.runestone.dynafilter.core.generator.annotation.tool.ValueFinderVisitor;
import com.runestone.dynafilter.core.operation.types.Equals;
import com.runestone.dynafilter.core.transformer.FilterValueContext;
import com.runestone.dynafilter.core.transformer.FilterValueTransformer;
import com.runestone.dynafilter.core.transformer.FilterValueTransformerRegistry;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TestScalarFilterValueTransformation {

    @Test
    void transformsRequestDefaultAndConstantValuesInDeclarationOrder() {
        List<String> calls = new ArrayList<>();
        FilterValueTransformerRegistry registry = registry(
                new PrefixTransformer(calls), new SuffixTransformer(calls));
        AnnotationStatementGenerator generator = new AnnotationStatementGenerator(null, registry.toResolver());

        var result = generator.generateStatements(new AnnotationStatementInput(ValueSources.class, null),
                Map.of("request", "request-value", "constant", "ignored-request-value"));

        assertThat(ValueFinderVisitor.find("requestPath", result.statement())).containsExactly("prefix-request-value-suffix");
        assertThat(ValueFinderVisitor.find("defaultPath", result.statement())).containsExactly("prefix-default-value-suffix");
        assertThat(ValueFinderVisitor.find("constantPath", result.statement())).containsExactly("prefix-constant-value-suffix");
        assertThat(calls).containsExactly("prefix", "suffix", "prefix", "suffix", "prefix", "suffix");
    }

    @Test
    void exposesImmutableContextForEachDeclaredParameter() {
        CapturingTransformer transformer = new CapturingTransformer();
        FilterValueTransformerRegistry registry = registry(transformer);
        AnnotationStatementGenerator generator = new AnnotationStatementGenerator(null, registry.toResolver());

        generator.generateStatements(new AnnotationStatementInput(ContextFilter.class, null),
                Map.of("first", "one", "second", "two"));

        assertThat(transformer.contexts).hasSize(2);
        FilterValueContext second = transformer.contexts.get(1);
        assertThat(second.parameter()).isEqualTo("second");
        assertThat(second.parameterIndex()).isEqualTo(1);
        assertThat(second.paths()).containsExactly("primary", "secondary");
        assertThat(second.operation()).isEqualTo(Equals.class);
        assertThat(second.declaredTargetType()).isEqualTo(Integer.class);
        assertThatThrownBy(() -> second.paths().add("other"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void skipsNullButTransformsEmptyStrings() {
        CapturingTransformer transformer = new CapturingTransformer();
        AnnotationStatementGenerator generator = new AnnotationStatementGenerator(null, registry(transformer).toResolver());

        generator.generateStatements(new AnnotationStatementInput(NullableValues.class, null), Map.of("empty", ""));

        assertThat(transformer.values).containsExactly("");
    }

    @Test
    void reportsNullResultsAndTransformerFailuresWithContext() {
        FilterValueTransformer nullReturningImplementation = (value, context) -> null;
        AnnotationStatementGenerator nullGenerator = new AnnotationStatementGenerator(null,
                type -> nullReturningImplementation);
        AnnotationStatementGenerator failingGenerator = new AnnotationStatementGenerator(null,
                registry(new FailingTransformer()).toResolver());

        assertThatThrownBy(() -> nullGenerator.generateStatements(
                new AnnotationStatementInput(NullResultFilter.class, null), Map.of("value", "input")))
                .isInstanceOf(StatementGenerationException.class)
                .hasMessageContaining(NullTransformer.class.getCanonicalName())
                .hasMessageContaining("value")
                .hasMessageContaining("target");

        assertThatThrownBy(() -> failingGenerator.generateStatements(
                new AnnotationStatementInput(FailingFilter.class, null), Map.of("value", "input")))
                .isInstanceOf(StatementGenerationException.class)
                .hasMessageContaining(FailingTransformer.class.getCanonicalName())
                .hasRootCauseMessage("boom");
    }

    @Test
    void failsDuringPlanCompilationWhenTransformerIsMissing() {
        AnnotationStatementGenerator generator = new AnnotationStatementGenerator();

        assertThatThrownBy(() -> generator.warmup(new AnnotationStatementInput(NullResultFilter.class, null)))
                .isInstanceOf(DynamicFilterConfigurationException.class)
                .hasMessageContaining(NullTransformer.class.getCanonicalName());
    }

    @Test
    void resolvesTransformersOnlyOnceWhileCompilingThePlanAndReusesContexts() {
        CapturingTransformer transformer = new CapturingTransformer();
        AtomicInteger resolutions = new AtomicInteger();
        AnnotationStatementGenerator generator = new AnnotationStatementGenerator(null, type -> {
            resolutions.incrementAndGet();
            return transformer;
        });
        AnnotationStatementInput input = new AnnotationStatementInput(ContextFilter.class, null);

        generator.generateStatements(input, Map.of("first", "one", "second", "two"));
        generator.generateStatements(input, Map.of("first", "three", "second", "four"));

        assertThat(resolutions).hasValue(1);
        assertThat(transformer.contexts.get(0)).isSameAs(transformer.contexts.get(2));
        assertThat(transformer.contexts.get(1)).isSameAs(transformer.contexts.get(3));
    }

    @Test
    void leavesTheTransformedRepresentationForOperationConversion() {
        AnnotationStatementGenerator generator = new AnnotationStatementGenerator(null,
                registry(new NumericTextTransformer()).toResolver());

        var result = generator.generateStatements(new AnnotationStatementInput(ConversionOrderFilter.class, null),
                Map.of("number", "forty-two"));

        assertThat(ValueFinderVisitor.find("numberPath", result.statement()))
                .containsExactly("42")
                .allMatch(String.class::isInstance);
    }

    @Test
    void safelySharesBoundTransformersAndContextsBetweenThreads() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        CountingTransformer transformer = new CountingTransformer(calls);
        AnnotationStatementGenerator generator = new AnnotationStatementGenerator(null, registry(transformer).toResolver());
        AnnotationStatementInput input = new AnnotationStatementInput(ConcurrentFilter.class, null);
        generator.warmup(input);

        try (var executor = Executors.newFixedThreadPool(8)) {
            var tasks = IntStream.range(0, 100)
                    .mapToObj(index -> (java.util.concurrent.Callable<Object>) () ->
                            generator.generateStatements(input, Map.of("value", index)).statement())
                    .toList();

            assertThat(executor.invokeAll(tasks)).allSatisfy(future -> assertThat(future.get()).isNotNull());
        }
        assertThat(calls).hasValue(100);
    }

    @SafeVarargs
    private static FilterValueTransformerRegistry registry(FilterValueTransformer... transformers) {
        FilterValueTransformerRegistry registry = new FilterValueTransformerRegistry();
        for (FilterValueTransformer transformer : transformers) {
            @SuppressWarnings("unchecked")
            Class<FilterValueTransformer> type = (Class<FilterValueTransformer>) transformer.getClass();
            registry.register(type, transformer);
        }
        return registry;
    }

    @Conjunction({
            @Filter(path = "requestPath", parameters = "request", operation = Equals.class,
                    transformers = {PrefixTransformer.class, SuffixTransformer.class}),
            @Filter(path = "defaultPath", parameters = "default", defaultValues = "default-value", operation = Equals.class,
                    transformers = {PrefixTransformer.class, SuffixTransformer.class}),
            @Filter(path = "constantPath", parameters = "constant", constantValues = "constant-value", operation = Equals.class,
                    transformers = {PrefixTransformer.class, SuffixTransformer.class})
    })
    private interface ValueSources {
    }

    @Conjunction(@Filter(path = {"primary", "secondary"}, parameters = {"first", "second"},
            targetType = Integer.class, operation = Equals.class, transformers = CapturingTransformer.class))
    private interface ContextFilter {
    }

    @Conjunction(@Filter(path = "nullable", parameters = {"missing", "empty"}, operation = Equals.class,
            transformers = CapturingTransformer.class))
    private interface NullableValues {
    }

    @Conjunction(@Filter(path = "target", parameters = "value", operation = Equals.class,
            transformers = NullTransformer.class))
    private interface NullResultFilter {
    }

    @Conjunction(@Filter(path = "target", parameters = "value", operation = Equals.class,
            transformers = FailingTransformer.class))
    private interface FailingFilter {
    }

    @Conjunction(@Filter(path = "numberPath", parameters = "number", targetType = Integer.class,
            operation = Equals.class, transformers = NumericTextTransformer.class))
    private interface ConversionOrderFilter {
    }

    @Conjunction(@Filter(path = "target", parameters = "value", operation = Equals.class,
            transformers = CountingTransformer.class))
    private interface ConcurrentFilter {
    }

    public static final class PrefixTransformer implements FilterValueTransformer {
        private final List<String> calls;

        PrefixTransformer(List<String> calls) {
            this.calls = calls;
        }

        @Override
        public Object transform(Object value, FilterValueContext context) {
            calls.add("prefix");
            return "prefix-" + value;
        }
    }

    public static final class SuffixTransformer implements FilterValueTransformer {
        private final List<String> calls;

        SuffixTransformer(List<String> calls) {
            this.calls = calls;
        }

        @Override
        public Object transform(Object value, FilterValueContext context) {
            calls.add("suffix");
            return value + "-suffix";
        }
    }

    public static final class CapturingTransformer implements FilterValueTransformer {
        private final List<Object> values = new ArrayList<>();
        private final List<FilterValueContext> contexts = new ArrayList<>();

        @Override
        public Object transform(Object value, FilterValueContext context) {
            values.add(value);
            contexts.add(context);
            return value;
        }
    }

    public static final class NullTransformer implements FilterValueTransformer {
        @Override
        public Object transform(Object value, FilterValueContext context) {
            return null;
        }
    }

    public static final class FailingTransformer implements FilterValueTransformer {
        @Override
        public Object transform(Object value, FilterValueContext context) {
            throw new IllegalStateException("boom");
        }
    }

    public static final class NumericTextTransformer implements FilterValueTransformer {
        @Override
        public Object transform(Object value, FilterValueContext context) {
            return "42";
        }
    }

    public static final class CountingTransformer implements FilterValueTransformer {
        private final AtomicInteger calls;

        CountingTransformer(AtomicInteger calls) {
            this.calls = calls;
        }

        @Override
        public Object transform(Object value, FilterValueContext context) {
            calls.incrementAndGet();
            return value;
        }
    }
}
