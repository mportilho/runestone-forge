package com.runestone.dynafilter.core.generator.annotation;

import com.runestone.dynafilter.core.exceptions.StatementGenerationException;
import com.runestone.dynafilter.core.generator.annotation.tool.ValueFinderVisitor;
import com.runestone.dynafilter.core.operation.types.IsIn;
import com.runestone.dynafilter.core.transformer.FilterValueContext;
import com.runestone.dynafilter.core.transformer.FilterValueTransformer;
import com.runestone.dynafilter.core.transformer.FilterValueTransformerRegistry;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TestMultiValueFilterValueTransformation {

    @Test
    void transformsReferenceArraysWithoutMutatingInputOrRetainingItsComponentType() {
        String[] input = {"1", null, "2"};
        AnnotationStatementGenerator generator = generator(new ParseIntegerTransformer());

        Object transformed = generatedValue(generator, input);

        assertThat(transformed).isExactlyInstanceOf(Object[].class);
        assertThat((Object[]) transformed).containsExactly(1, null, 2);
        assertThat(input).containsExactly("1", null, "2");
    }

    @Test
    void transformsListsAndSetsOnceInEncounterOrderWhilePreservingCardinality() {
        TrackingTransformer transformer = new TrackingTransformer();
        AnnotationStatementGenerator generator = generator(transformer);
        List<String> list = new ArrayList<>(List.of("a", "b", "c"));
        LinkedHashSet<String> set = new LinkedHashSet<>(List.of("a", "b", "c"));

        Object transformedList = generatedValue(generator, list);
        Object transformedSet = generatedValue(generator, set);

        assertThat(transformedList).isInstanceOf(ArrayList.class).asList().containsExactly("A", "B", "C");
        assertThat(transformedSet).isInstanceOf(ArrayList.class).asList().containsExactly("A", "B", "C");
        assertThat(list).containsExactly("a", "b", "c");
        assertThat(set).containsExactly("a", "b", "c");
        assertThat(transformer.values).containsExactly("a", "b", "c", "a", "b", "c");
    }

    @Test
    void preservesSetCardinalityWhenTransformedValuesCollide() {
        AnnotationStatementGenerator generator = generator((value, context) -> "same");
        LinkedHashSet<String> input = new LinkedHashSet<>(List.of("a", "b"));

        Object transformed = generatedValue(generator, input);

        assertThat(transformed).isInstanceOf(List.class).asList().containsExactly("same", "same");
    }

    @Test
    void preservesNullElementsWithoutDispatchingThem() {
        TrackingTransformer transformer = new TrackingTransformer();
        AnnotationStatementGenerator generator = generator(transformer);

        Object transformed = generatedValue(generator, new ArrayList<>(Arrays.asList("a", null, "b")));

        assertThat(transformed).asList().containsExactly("A", null, "B");
        assertThat(transformer.values).containsExactly("a", "b");
    }

    @Test
    void keepsPrimitiveArraysOnTheScalarPath() {
        int[] input = {1, 2};
        AnnotationStatementGenerator generator = generator((value, context) -> {
            assertThat(value).isSameAs(input);
            return "primitive-array";
        });

        assertThat(generatedValue(generator, input)).isEqualTo("primitive-array");
    }

    @Test
    void reportsTheFailingMultivaluePositionOnlyOnFailure() {
        AnnotationStatementGenerator generator = generator((value, context) -> {
            if (value.equals("bad")) {
                throw new IllegalStateException("boom");
            }
            return value;
        });

        assertThatThrownBy(() -> generatedValue(generator, new String[]{"ok", "bad"}))
                .isInstanceOf(StatementGenerationException.class)
                .hasMessageContaining("multivalue position 1")
                .hasRootCauseMessage("boom");
    }

    @Test
    void keepsContainerIdentityWhenNoTransformerIsDeclared() {
        String[] input = {"a", "b"};
        AnnotationStatementGenerator generator = new AnnotationStatementGenerator();
        var result = generator.generateStatements(new AnnotationStatementInput(NoTransformerFilter.class, null),
                Map.of("values", input));

        assertThat(ValueFinderVisitor.find("target", result.statement())[0]).isSameAs(input);
    }

    private static Object generatedValue(AnnotationStatementGenerator generator, Object input) {
        var result = generator.generateStatements(new AnnotationStatementInput(MultiValueFilter.class, null),
                Map.of("values", input));
        return ValueFinderVisitor.find("target", result.statement())[0];
    }

    private static AnnotationStatementGenerator generator(FilterValueTransformer transformer) {
        FilterValueTransformerRegistry registry = new FilterValueTransformerRegistry();
        registry.register(FilterValueTransformer.class, transformer);
        return new AnnotationStatementGenerator(null, registry.toResolver());
    }

    @Conjunction(@Filter(path = "target", parameters = "values", operation = IsIn.class,
            transformers = FilterValueTransformer.class))
    private interface MultiValueFilter {
    }

    @Conjunction(@Filter(path = "target", parameters = "values", operation = IsIn.class))
    private interface NoTransformerFilter {
    }

    private static final class ParseIntegerTransformer implements FilterValueTransformer {
        @Override
        public Object transform(Object value, FilterValueContext context) {
            return Integer.valueOf(value.toString());
        }
    }

    private static final class TrackingTransformer implements FilterValueTransformer {
        private final List<Object> values = new ArrayList<>();

        @Override
        public Object transform(Object value, FilterValueContext context) {
            values.add(value);
            return value.toString().toUpperCase();
        }
    }
}
