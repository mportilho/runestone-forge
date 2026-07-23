package com.runestone.expeval_mk3.corpus;

import com.fasterxml.jackson.databind.JsonNode;
import com.runestone.expeval_mk3.api.CompilationDiagnostic;
import com.runestone.expeval_mk3.api.CollectionType;
import com.runestone.expeval_mk3.api.ExternalSymbolOverwritePolicy;
import com.runestone.expeval_mk3.api.MapType;
import com.runestone.expeval_mk3.api.ScalarType;
import com.runestone.expeval_mk3.api.ExpressionCompiler;
import com.runestone.expeval_mk3.api.ExpressionEnvironment;
import com.runestone.expeval_mk3.api.ExpressionCompilationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CollectionLiteralCorpusTest {

    private static final Set<String> CASE_IDS = Set.of(
            "runtime.collection.numbers.001",
            "runtime.collection.empty-context.001",
            "runtime.collection.equality.001",
            "runtime.collection.order.001",
            "runtime.collection.inequality.001",
            "runtime.map.equality.001",
            "semantic.collection.empty.001",
            "semantic.collection.limit.001");

    @Test
    void executesTheCollectionLiteralSliceFromTheExpressionCorpus() {
        List<ExpressionCase> cases = ExpressionCaseLoader.loadAll().stream()
                .filter(expressionCase -> CASE_IDS.contains(expressionCase.id()))
                .toList();

        assertThat(cases).hasSize(CASE_IDS.size());
        for (ExpressionCase expressionCase : cases) {
            if (expressionCase.kind() == CaseKind.INVALID) {
                ExpectedDiagnostic expected = (ExpectedDiagnostic) expressionCase.expectedOutcome();
                assertThatThrownBy(() -> ExpressionCompiler.compile(
                        expressionCase.source(), environment(expressionCase)))
                        .as(expressionCase.id())
                        .isInstanceOfSatisfying(ExpressionCompilationException.class, failure -> {
                            CompilationDiagnostic actual = failure.diagnostics().getFirst();
                            assertThat(actual.code()).isEqualTo(expected.code());
                            assertThat(actual.offset()).isEqualTo(expected.spans().getFirst().offset());
                            assertThat(actual.endOffset()).isEqualTo(expected.spans().getFirst().endOffset());
                        });
                continue;
            }
            ExpectedResult expected = (ExpectedResult) expressionCase.expectedOutcome();
            Object actual = ExpressionCompiler.compile(expressionCase.source(), environment(expressionCase)).compute();
            assertThat(actual)
                    .as(expressionCase.id())
                    .isEqualTo(expectedValue(expected.type(), expected.result()));
            if (actual instanceof List<?> values) {
                assertThatThrownBy(() -> ((List<Object>) values).add(new Object()))
                        .as(expressionCase.id() + " immutable result")
                        .isInstanceOf(UnsupportedOperationException.class);
            }
        }
    }

    private static ExpressionEnvironment environment(ExpressionCase expressionCase) {
        if (expressionCase.id().equals("semantic.collection.limit.001")) {
            return ExpressionEnvironment.builder().maxMaterializedSize(2).build();
        }
        if (expressionCase.id().equals("runtime.map.equality.001")) {
            Map<String, Object> left = new LinkedHashMap<>();
            left.put("first", List.of(new BigDecimal("1.0")));
            left.put("second", List.of(new BigDecimal("2")));
            Map<String, Object> right = new LinkedHashMap<>();
            right.put("second", List.of(new BigDecimal("2.00")));
            right.put("first", List.of(new BigDecimal("1")));
            MapType type = new MapType(new CollectionType(ScalarType.NUMBER));
            return ExpressionEnvironment.builder()
                    .externalSymbol("left", type, left, ExternalSymbolOverwritePolicy.FIXED)
                    .externalSymbol("right", type, right, ExternalSymbolOverwritePolicy.FIXED)
                    .build();
        }
        return ExpressionEnvironment.standard();
    }

    private static Object expectedValue(String type, JsonNode value) {
        return switch (type) {
            case "NUMBER" -> new BigDecimal(value.textValue());
            case "BOOLEAN" -> value.booleanValue();
            case "STRING" -> value.textValue();
            case "COLLECTION" -> expectedCollection(value);
            default -> throw new IllegalArgumentException("Unsupported expected corpus type: " + type);
        };
    }

    private static List<Object> expectedCollection(JsonNode values) {
        List<Object> result = new ArrayList<>(values.size());
        for (JsonNode value : values) {
            result.add(expectedValue(value.get("type").textValue(), value.get("value")));
        }
        return List.copyOf(result);
    }
}
