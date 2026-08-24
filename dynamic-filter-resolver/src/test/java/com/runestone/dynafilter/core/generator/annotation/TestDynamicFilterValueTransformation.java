package com.runestone.dynafilter.core.generator.annotation;

import com.runestone.dynafilter.core.exceptions.StatementGenerationException;
import com.runestone.dynafilter.core.model.FilterData;
import com.runestone.dynafilter.core.model.statement.AbstractStatement;
import com.runestone.dynafilter.core.model.statement.CompoundStatement;
import com.runestone.dynafilter.core.model.statement.LogicalStatement;
import com.runestone.dynafilter.core.model.statement.NegatedStatement;
import com.runestone.dynafilter.core.model.statement.StatementVisitor;
import com.runestone.dynafilter.core.operation.types.Between;
import com.runestone.dynafilter.core.operation.types.Dynamic;
import com.runestone.dynafilter.core.operation.types.Equals;
import com.runestone.dynafilter.core.operation.types.IsIn;
import com.runestone.dynafilter.core.transformer.FilterValueContext;
import com.runestone.dynafilter.core.transformer.FilterValueTransformer;
import com.runestone.dynafilter.core.transformer.FilterValueTransformerRegistry;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TestDynamicFilterValueTransformation {

    @Test
    void transformsOnlyTheEqPayloadWithTheEffectiveOperationContext() {
        CapturingTransformer transformer = new CapturingTransformer();

        FilterData data = generate(generator(transformer), "eq", "value");

        assertThat(data.operation()).isEqualTo(Equals.class);
        assertThat(data.values()).containsExactly("VALUE");
        assertThat(transformer.values).containsExactly("value");
        assertThat(transformer.contexts).singleElement().satisfies(context ->
                assertThat(context.operation()).isEqualTo(Equals.class));
    }

    @Test
    void transformsBothBetweenBoundsWithOnePrecompiledContext() {
        CapturingTransformer transformer = new CapturingTransformer();
        AnnotationStatementGenerator generator = generator(transformer);

        FilterData first = generate(generator, "bt", "a", "z");
        FilterData second = generate(generator, "bt", "b", "y");

        assertThat(first.operation()).isEqualTo(Between.class);
        assertThat(first.parameters()).containsExactly("valueFrom", "valueTo");
        assertThat(first.values()).containsExactly("A", "Z");
        assertThat(transformer.contexts).allMatch(context -> context.operation().equals(Between.class));
        assertThat(transformer.contexts).allMatch(context -> context == transformer.contexts.getFirst());
        assertThat(second.values()).containsExactly("B", "Y");
    }

    @Test
    void transformsFlatAndNestedInPayloadsWithoutTransformingTheOpcode() {
        CapturingTransformer transformer = new CapturingTransformer();
        AnnotationStatementGenerator generator = generator(transformer);

        FilterData flat = generate(generator, "in", "a", "b");
        FilterData nestedArray = generate(generator, "nin", new Object[]{"c", null, "d"});
        FilterData nestedList = generate(generator, "in", List.of("e", "f"));

        assertThat(flat.operation()).isEqualTo(IsIn.class);
        assertThat(flat.values()).containsExactly((Object) new Object[]{"A", "B"});
        assertThat(nestedArray.negate()).isTrue();
        assertThat((Object[]) nestedArray.values()[0]).containsExactly("C", null, "D");
        assertThat(nestedList.values()[0]).asList().containsExactly("E", "F");
        assertThat(transformer.values).containsExactly("a", "b", "c", "d", "e", "f");
        assertThat(transformer.contexts).allMatch(context -> context.operation().equals(IsIn.class));
    }

    @Test
    void validatesBetweenArityBeforeInvokingTransformers() {
        CapturingTransformer transformer = new CapturingTransformer();

        assertThatThrownBy(() -> generate(generator(transformer), "bt", "only"))
                .isInstanceOf(StatementGenerationException.class)
                .hasMessage("Between operation must have two values");
        assertThat(transformer.values).isEmpty();
    }

    @Test
    void reportsDynamicPayloadPositionOnFailure() {
        FilterValueTransformer transformer = (value, context) -> {
            if (value.equals("bad")) {
                throw new IllegalStateException("boom");
            }
            return value;
        };

        assertThatThrownBy(() -> generate(generator(transformer), "in", "ok", "bad"))
                .isInstanceOf(StatementGenerationException.class)
                .hasMessageContaining("multivalue position 1")
                .hasRootCauseMessage("boom");
    }

    private static AnnotationStatementGenerator generator(FilterValueTransformer transformer) {
        FilterValueTransformerRegistry registry = new FilterValueTransformerRegistry();
        registry.register(FilterValueTransformer.class, transformer);
        return new AnnotationStatementGenerator(null, registry.toResolver());
    }

    private static FilterData generate(AnnotationStatementGenerator generator, Object... dynamicValues) {
        var result = generator.generateStatements(new AnnotationStatementInput(DynamicFilter.class, null),
                Map.of("value", dynamicValues));
        FilterDataFinder visitor = new FilterDataFinder();
        result.statement().acceptVisitor(visitor);
        return visitor.data;
    }

    @Conjunction(@Filter(path = "target", parameters = "value", operation = Dynamic.class,
            transformers = FilterValueTransformer.class))
    private interface DynamicFilter {
    }

    private static final class CapturingTransformer implements FilterValueTransformer {
        private final List<Object> values = new ArrayList<>();
        private final List<FilterValueContext> contexts = new ArrayList<>();

        @Override
        public Object transform(Object value, FilterValueContext context) {
            values.add(value);
            contexts.add(context);
            return value.toString().toUpperCase();
        }
    }

    private static final class FilterDataFinder implements StatementVisitor {
        private FilterData data;

        @Override
        public void visit(NegatedStatement statement) {
            statement.getStatement().acceptVisitor(this);
        }

        @Override
        public void visit(CompoundStatement statement) {
            statement.getLeftStatement().acceptVisitor(this);
            statement.getRightStatement().acceptVisitor(this);
        }

        @Override
        public void visit(LogicalStatement statement) {
            data = statement.getFilterData();
        }
    }
}
