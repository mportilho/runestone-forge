package com.runestone.dynafilter.core.generator.annotation;

import com.runestone.dynafilter.core.exceptions.StatementGenerationException;
import com.runestone.dynafilter.core.generator.annotation.testdata.interfaces.CombinedAnnotations;
import com.runestone.dynafilter.core.generator.annotation.testdata.interfaces.StatusOkInterface;
import com.runestone.dynafilter.core.operation.types.Equals;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.util.Map;
import java.util.concurrent.Executors;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TestAnnotationStatementGeneratorPlan {

    @Test
    void cachesEquivalentInputsInTheGeneratorInstance() {
        AnnotationStatementGenerator generator = new AnnotationStatementGenerator(null, 10);
        AnnotationStatementInput first = input();
        AnnotationStatementInput equivalent = input();

        generator.generateStatements(first, Map.of());
        generator.generateStatements(equivalent, Map.of());

        assertThat(generator.planCacheSize()).isOne();
    }

    @Test
    void doesNotSharePlansBetweenGeneratorInstances() {
        AnnotationStatementGenerator firstGenerator = new AnnotationStatementGenerator(null, 10);
        AnnotationStatementGenerator secondGenerator = new AnnotationStatementGenerator(null, 10);

        firstGenerator.warmup(input());

        assertThat(firstGenerator.planCacheSize()).isOne();
        assertThat(secondGenerator.planCacheSize()).isZero();
    }

    @Test
    void clearsOnlyTheSelectedGeneratorCache() {
        AnnotationStatementGenerator firstGenerator = new AnnotationStatementGenerator(null, 10);
        AnnotationStatementGenerator secondGenerator = new AnnotationStatementGenerator(null, 10);
        firstGenerator.warmup(input());
        secondGenerator.warmup(input());

        firstGenerator.clearCache();

        assertThat(firstGenerator.planCacheSize()).isZero();
        assertThat(secondGenerator.planCacheSize()).isOne();
    }

    @Test
    void boundsTheGeneratorPlanCache() {
        AnnotationStatementGenerator generator = new AnnotationStatementGenerator(null, 10);

        for (int i = 0; i < 100; i++) {
            generator.warmup(new AnnotationStatementInput(null, new Annotation[]{new SyntheticAnnotation(i)}));
        }

        assertThat(generator.planCacheSize()).isLessThanOrEqualTo(10);
    }

    @Test
    void safelySharesAWarmedPlanBetweenThreads() throws Exception {
        AnnotationStatementGenerator generator = new AnnotationStatementGenerator(null, 10);
        AnnotationStatementInput input = input();
        generator.warmup(input);

        try (var executor = Executors.newFixedThreadPool(8)) {
            var tasks = java.util.stream.IntStream.range(0, 100)
                    .mapToObj(index -> (java.util.concurrent.Callable<Object>) () ->
                            generator.generateStatements(input, Map.of("status", "OK")).statement())
                    .toList();

            assertThat(executor.invokeAll(tasks))
                    .allSatisfy(future -> assertThat(future.get()).isNotNull());
        }
        assertThat(generator.planCacheSize()).isOne();
    }

    @Test
    void preservesRuntimeErrorOrderingAfterStructuralCompilation() {
        AnnotationStatementGenerator generator = new AnnotationStatementGenerator();
        AnnotationStatementInput input = new AnnotationStatementInput(RequiredBeforeInvalid.class, null);

        assertThatThrownBy(() -> generator.generateStatements(input, Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Parameter 'required' required");
    }

    @Test
    void preservesParameterResolutionOrderWithinAFilter() {
        AnnotationStatementGenerator generator = new AnnotationStatementGenerator(value -> {
            throw new IllegalStateException("resolver failure");
        });
        AnnotationStatementInput input = new AnnotationStatementInput(ResolverFailureBeforeInvalid.class, null);

        assertThatThrownBy(() -> generator.generateStatements(input, Map.of()))
                .isInstanceOf(StatementGenerationException.class)
                .hasMessage("Provided expression resolver threw an error")
                .hasRootCauseMessage("resolver failure");
    }

    private static AnnotationStatementInput input() {
        return new AnnotationStatementInput(CombinedAnnotations.class, StatusOkInterface.class.getAnnotations());
    }

    @Conjunction({
            @Filter(path = "first", parameters = "required", operation = Equals.class, required = true),
            @Filter(path = "second", parameters = "", operation = Equals.class)
    })
    private interface RequiredBeforeInvalid {
    }

    @Conjunction(@Filter(
            path = "value",
            parameters = {"valid", ""},
            defaultValues = {"expression", "unused"},
            operation = Equals.class
    ))
    private interface ResolverFailureBeforeInvalid {
    }

    @Retention(RUNTIME)
    private @interface SyntheticMarker {
        int value();
    }

    private record SyntheticAnnotation(int value) implements Annotation {

        @Override
        public Class<? extends Annotation> annotationType() {
            return SyntheticMarker.class;
        }
    }
}
