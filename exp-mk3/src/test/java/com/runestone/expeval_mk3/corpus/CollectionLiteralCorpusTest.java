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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CollectionLiteralCorpusTest {

    private static final Set<String> CASE_IDS = Set.of(
            "runtime.assignment.destructuring.001",
            "runtime.collection.filter.001",
            "runtime.collection.index.001",
            "runtime.collection.index-negative.001",
            "runtime.collection.nested-lambda.001",
            "runtime.collection.numbers.001",
            "runtime.collection.empty-context.001",
            "runtime.collection.equality.001",
            "runtime.collection.order.001",
            "runtime.collection.inequality.001",
            "runtime.collection.slice.closed.001",
            "runtime.collection.slice.open-end.001",
            "runtime.collection.slice.open-start.001",
            "runtime.collection.operations-lambda.001",
            "runtime.collection.reduce-atomicity.001",
            "runtime.collection.reduce.001",
            "runtime.collection.reduce-empty.001",
            "runtime.collection.reduce-safe.001",
            "runtime.collection.safe-map.001",
            "runtime.collection.sort-by-atomicity.001",
            "runtime.collection.sort-by-date.001",
            "runtime.collection.sort-by-datetime.001",
            "runtime.collection.sort-by.001",
            "runtime.collection.sort-by-safe.001",
            "runtime.collection.sort-by-string.001",
            "runtime.collection.sort-by-time.001",
            "runtime.map.operations-lambda.001",
            "semantic.map.entry-escape.001",
            "runtime.map.equality.001",
            "semantic.collection.empty.001",
            "semantic.collection.limit.001",
            "semantic.collection.lambda-depth.001",
            "semantic.collection.lambda-nullable.001",
            "semantic.collection.lambda-type.001",
            "semantic.collection.reduce-type.001",
            "semantic.collection.sort-by-direction.001",
            "semantic.collection.sort-by-key-type.001",
            "semantic.collection.sort-by-limit.001",
            "semantic.destructuring.duplicate.001",
            "semantic.destructuring.fixed-size.001",
            "semantic.filter.depth.001",
            "runtime.destructuring.dynamic-size.001");

    @Test
    void executesTheCollectionLiteralSliceFromTheExpressionCorpus() {
        List<ExpressionCase> cases = ExpressionCaseLoader.loadAll().stream()
                .filter(expressionCase -> CASE_IDS.contains(expressionCase.id()))
                .toList();

        assertThat(cases).hasSize(CASE_IDS.size());
        for (ExpressionCase expressionCase : cases) {
            if (expressionCase.kind() == CaseKind.INVALID) {
                if (expressionCase.expectedOutcome() instanceof ExpectedRuntimeError expected) {
                    assertThatThrownBy(() -> ExpressionCompiler.compile(
                                    expressionCase.source(), environment(expressionCase)).compute())
                            .as(expressionCase.id())
                            .isInstanceOf(runtimeErrorType(expected.type()))
                            .hasMessageContaining(expected.messageContains());
                    continue;
                }
                ExpectedDiagnostic expected = (ExpectedDiagnostic) expressionCase.expectedOutcome();
                assertThatThrownBy(() -> ExpressionCompiler.compile(
                        expressionCase.source(), environment(expressionCase)))
                        .as(expressionCase.id())
                        .isInstanceOfSatisfying(ExpressionCompilationException.class, failure -> {
                            CompilationDiagnostic actual = failure.diagnostics().getFirst();
                            assertThat(actual.code()).isEqualTo(expected.code());
                            if (!expected.spans().isEmpty()) {
                                assertThat(actual.offset()).isEqualTo(expected.spans().getFirst().offset());
                                assertThat(actual.endOffset()).isEqualTo(expected.spans().getFirst().endOffset());
                            }
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
        ExpressionEnvironment.Builder builder = ExpressionEnvironment.builder();
        JsonNode environment = expressionCase.root().get("environment");
        if (environment != null) {
            JsonNode maxMaterializedSize = environment.get("maxMaterializedSize");
            if (maxMaterializedSize != null) {
                builder.maxMaterializedSize(maxMaterializedSize.intValue());
            }
            JsonNode maxFactorialInput = environment.get("maxFactorialInput");
            if (maxFactorialInput != null) {
                builder.maxFactorialInput(maxFactorialInput.intValue());
            }
            JsonNode maxCurrentItemDepth = environment.get("maxCurrentItemDepth");
            if (maxCurrentItemDepth != null) {
                builder.maxCurrentItemDepth(maxCurrentItemDepth.intValue());
            }
        }
        if (expressionCase.id().equals("runtime.map.equality.001")) {
            Map<String, Object> left = new LinkedHashMap<>();
            left.put("first", List.of(new BigDecimal("1.0")));
            left.put("second", List.of(new BigDecimal("2")));
            Map<String, Object> right = new LinkedHashMap<>();
            right.put("second", List.of(new BigDecimal("2.00")));
            right.put("first", List.of(new BigDecimal("1")));
            MapType type = new MapType(new CollectionType(ScalarType.NUMBER));
            return builder
                    .externalSymbol("left", type, left, ExternalSymbolOverwritePolicy.FIXED)
                    .externalSymbol("right", type, right, ExternalSymbolOverwritePolicy.FIXED)
                    .build();
        }
        if (expressionCase.id().equals("runtime.map.operations-lambda.001")
                || expressionCase.id().equals("runtime.map.all.001")
                || expressionCase.id().equals("runtime.map.any-key.001")
                || expressionCase.id().equals("semantic.map.entry-escape.001")) {
            Map<String, Object> source = new LinkedHashMap<>();
            source.put("b", new BigDecimal("2"));
            source.put("A", BigDecimal.ONE);
            return builder
                    .externalSymbol("m", new MapType(ScalarType.NUMBER), source, ExternalSymbolOverwritePolicy.FIXED)
                    .build();
        }
        return builder.build();
    }

    private static Object expectedValue(String type, JsonNode value) {
        return switch (type) {
            case "NUMBER" -> new BigDecimal(value.textValue());
            case "BOOLEAN" -> value.booleanValue();
            case "STRING" -> value.textValue();
            case "DATE" -> LocalDate.parse(value.textValue());
            case "TIME" -> LocalTime.parse(value.textValue());
            case "DATETIME" -> LocalDateTime.parse(value.textValue());
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

    private static Class<? extends Throwable> runtimeErrorType(String type) {
        return switch (type) {
            case "ArithmeticException" -> ArithmeticException.class;
            case "IllegalStateException" -> IllegalStateException.class;
            default -> throw new IllegalArgumentException("Unsupported runtime error type: " + type);
        };
    }
}
