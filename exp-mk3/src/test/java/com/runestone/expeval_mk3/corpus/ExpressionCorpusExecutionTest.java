package com.runestone.expeval_mk3.corpus;

import com.fasterxml.jackson.databind.JsonNode;
import com.runestone.expeval_mk3.api.CollectionType;
import com.runestone.expeval_mk3.api.CompilationDiagnostic;
import com.runestone.expeval_mk3.api.ExpressionCompilationException;
import com.runestone.expeval_mk3.api.ExpressionCompiler;
import com.runestone.expeval_mk3.api.ExpressionEnvironment;
import com.runestone.expeval_mk3.api.ExpressionType;
import com.runestone.expeval_mk3.api.ExternalSymbolOverwritePolicy;
import com.runestone.expeval_mk3.api.MapType;
import com.runestone.expeval_mk3.api.ObjectType;
import com.runestone.expeval_mk3.api.ScalarType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExpressionCorpusExecutionTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("semanticAndRuntimeCases")
    void semanticAndRuntimeExpressionCasesAreExecutableGates(String caseId, ExpressionCase expressionCase) {
        if (expressionCase.kind() == CaseKind.INVALID) {
            assertInvalidCase(expressionCase);
            return;
        }

        var compiled = ExpressionCompiler.compile(expressionCase.source(), environment(expressionCase));
        if (expressionCase.phase() == CasePhase.SEMANTIC) {
            return;
        }

        ExpectedResult expected = (ExpectedResult) expressionCase.expectedOutcome();
        Object actual = compiled.compute(inputs(expressionCase));
        assertExpectedValue(expressionCase.id(), expected.type(), expected.result(), actual);
    }

    private static Stream<Arguments> semanticAndRuntimeCases() {
        return ExpressionCaseLoader.loadAll().stream()
                .filter(expressionCase -> expressionCase.phase() == CasePhase.SEMANTIC
                        || expressionCase.phase() == CasePhase.RUNTIME)
                .map(expressionCase -> Arguments.of(expressionCase.id(), expressionCase));
    }

    private static void assertInvalidCase(ExpressionCase expressionCase) {
        if (expressionCase.expectedOutcome() instanceof ExpectedRuntimeError expected) {
            assertThatThrownBy(() -> ExpressionCompiler.compile(
                            expressionCase.source(), environment(expressionCase))
                    .compute(inputs(expressionCase)))
                    .as(expressionCase.id())
                    .isInstanceOf(runtimeErrorType(expected.type()))
                    .hasMessageContaining(expected.messageContains());
            return;
        }

        ExpectedDiagnostic expected = (ExpectedDiagnostic) expressionCase.expectedOutcome();
        assertThatThrownBy(() -> ExpressionCompiler.compile(expressionCase.source(), environment(expressionCase)))
                .as(expressionCase.id())
                .isInstanceOfSatisfying(ExpressionCompilationException.class, failure -> {
                    CompilationDiagnostic actual = failure.diagnostics().getFirst();
                    assertThat(actual.category()).isEqualTo(expected.category());
                    assertThat(actual.code()).isEqualTo(expected.code());
                    if (!expected.spans().isEmpty()) {
                        assertThat(actual.offset()).isEqualTo(expected.spans().getFirst().offset());
                        assertThat(actual.endOffset()).isEqualTo(expected.spans().getFirst().endOffset());
                    }
                });
    }

    private static ExpressionEnvironment environment(ExpressionCase expressionCase) {
        ExpressionEnvironment.Builder builder = ExpressionEnvironment.builder();
        JsonNode environment = expressionCase.root().get("environment");
        applyEnvironmentFields(builder, environment);
        applyExternalSymbols(builder, environment == null ? null : environment.get("symbols"));
        return builder.build();
    }

    private static void applyEnvironmentFields(ExpressionEnvironment.Builder builder, JsonNode environment) {
        if (environment == null) {
            return;
        }
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

    private static void applyExternalSymbols(ExpressionEnvironment.Builder builder, JsonNode symbols) {
        if (symbols == null) {
            return;
        }
        Iterator<Map.Entry<String, JsonNode>> fields = symbols.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> symbol = fields.next();
            JsonNode declaration = symbol.getValue();
            String type = declaration.get("type").textValue();
            if (type.startsWith("OBJECT_")) {
                applyJavaObjectSymbol(builder, symbol.getKey(), type, declaration.get("default"));
                continue;
            }
            builder.externalSymbol(
                    symbol.getKey(),
                    expressionType(type),
                    typedValue(type, declaration.get("default")),
                    ExternalSymbolOverwritePolicy.valueOf(declaration.get("overwritePolicy").textValue()));
        }
    }

    private static void applyJavaObjectSymbol(
            ExpressionEnvironment.Builder builder, String symbolName, String type, JsonNode declaration) {
        List<String> wildcardChildren = textList(declaration.get("wildcardChildren"));
        boolean ordered = declaration.path("ordered").asBoolean(true);
        if ("OBJECT_WILDCARD_CHILD_PROVIDER".equals(type)) {
            registerWildcardChildProvider(builder, symbolName, wildcardChildren, ordered);
        } else if ("OBJECT_FAILING_WILDCARD_CHILD_PROVIDER".equals(type)) {
            builder
                    .registerJavaTypeWildcardChildren(
                            FailingWildcardChildProvider.class,
                            wildcardChildren.toArray(String[]::new))
                    .externalSymbol(
                            symbolName,
                            new ObjectType(FailingWildcardChildProvider.class.getName()),
                            new FailingWildcardChildProvider(),
                            ExternalSymbolOverwritePolicy.FIXED);
        } else {
            throw new IllegalArgumentException("Unsupported corpus object type: " + type);
        }
    }

    private static void registerWildcardChildProvider(
            ExpressionEnvironment.Builder builder,
            String symbolName,
            List<String> wildcardChildren,
            boolean ordered) {
        if (ordered) {
            builder.registerJavaTypeWildcardChildren(WildcardChildProvider.class, wildcardChildren.toArray(String[]::new));
        } else {
            builder.registerJavaTypeWildcardChildren(WildcardChildProvider.class, Set.copyOf(wildcardChildren));
        }
        builder.externalSymbol(
                symbolName,
                new ObjectType(WildcardChildProvider.class.getName()),
                new WildcardChildProvider(),
                ExternalSymbolOverwritePolicy.FIXED);
    }

    private static List<String> textList(JsonNode node) {
        List<String> values = new ArrayList<>(node.size());
        for (JsonNode value : node) {
            values.add(value.textValue());
        }
        return List.copyOf(values);
    }

    private static Map<String, Object> inputs(ExpressionCase expressionCase) {
        JsonNode inputs = expressionCase.root().get("inputs");
        if (inputs == null) {
            return Map.of();
        }
        Map<String, Object> values = new LinkedHashMap<>();
        Iterator<Map.Entry<String, JsonNode>> fields = inputs.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> input = fields.next();
            JsonNode value = input.getValue();
            values.put(input.getKey(), typedValue(value.get("type").textValue(), value));
        }
        return Map.copyOf(values);
    }

    private static ExpressionType expressionType(String type) {
        return switch (type) {
            case "NUMBER" -> ScalarType.NUMBER;
            case "BOOLEAN" -> ScalarType.BOOLEAN;
            case "STRING" -> ScalarType.STRING;
            case "DATE" -> ScalarType.DATE;
            case "TIME" -> ScalarType.TIME;
            case "DATETIME" -> ScalarType.DATETIME;
            case "COLLECTION_NUMBER" -> new CollectionType(ScalarType.NUMBER);
            case "MAP_NUMBER" -> new MapType(ScalarType.NUMBER);
            case "MAP_COLLECTION_NUMBER" -> new MapType(new CollectionType(ScalarType.NUMBER));
            default -> throw new IllegalArgumentException("Unsupported corpus expression type: " + type);
        };
    }

    private static Object typedValue(String type, JsonNode node) {
        return switch (type) {
            case "NUMBER", "BOOLEAN", "STRING", "DATE", "TIME", "DATETIME" -> scalarValue(type, node.get("value"));
            case "COLLECTION" -> expectedCollection(node);
            case "COLLECTION_NUMBER" -> expectedCollection(node);
            case "MAP_NUMBER" -> mapValue(node);
            case "MAP_COLLECTION_NUMBER" -> mapCollectionNumber(node);
            default -> throw new IllegalArgumentException("Unsupported corpus value type: " + type);
        };
    }

    private static Object scalarValue(String type, JsonNode value) {
        return switch (type) {
            case "NUMBER" -> new BigDecimal(value.textValue());
            case "BOOLEAN" -> value.booleanValue();
            case "STRING" -> value.textValue();
            case "DATE" -> LocalDate.parse(value.textValue());
            case "TIME" -> LocalTime.parse(value.textValue());
            case "DATETIME" -> LocalDateTime.parse(value.textValue());
            default -> throw new IllegalArgumentException("Unsupported scalar corpus type: " + type);
        };
    }

    private static Map<String, Object> mapCollectionNumber(JsonNode value) {
        Map<String, Object> result = new LinkedHashMap<>();
        value.fields().forEachRemaining(entry -> result.put(entry.getKey(), expectedCollection(entry.getValue())));
        return Map.copyOf(result);
    }

    private static Map<String, Object> mapValue(JsonNode value) {
        Map<String, Object> result = new LinkedHashMap<>();
        value.fields().forEachRemaining(entry -> {
            JsonNode typedValue = entry.getValue();
            result.put(entry.getKey(), typedValue(typedValue.get("type").textValue(), typedValue));
        });
        return Map.copyOf(result);
    }

    private static void assertExpectedValue(String caseId, String type, JsonNode expected, Object actual) {
        if ("COLLECTION".equals(type)) {
            assertThat(actual).as(caseId).isInstanceOf(List.class);
            assertExpectedCollection(caseId, expected, (List<?>) actual);
            assertThatThrownBy(() -> ((List<?>) actual).add(null))
                    .as(caseId + " immutable result")
                    .isInstanceOf(UnsupportedOperationException.class);
            return;
        }
        if ("NUMBER".equals(type)) {
            assertThat((BigDecimal) actual).as(caseId).isEqualByComparingTo(new BigDecimal(expected.textValue()));
            return;
        }
        assertThat(actual).as(caseId).isEqualTo(scalarValue(type, expected));
    }

    private static void assertExpectedCollection(String caseId, JsonNode expected, List<?> actual) {
        assertThat(actual).as(caseId).hasSize(expected.size());
        for (int index = 0; index < expected.size(); index++) {
            JsonNode expectedItem = expected.get(index);
            assertExpectedValue(
                    caseId + '[' + index + ']',
                    expectedItem.get("type").textValue(),
                    expectedItem.get("value"),
                    actual.get(index));
        }
    }

    private static List<Object> expectedCollection(JsonNode values) {
        List<Object> result = new ArrayList<>(values.size());
        for (JsonNode value : values) {
            result.add(typedValue(value.get("type").textValue(), value));
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

    public static final class WildcardChildProvider {

        public BigDecimal first() {
            return BigDecimal.ONE;
        }

        public BigDecimal second() {
            return new BigDecimal("2");
        }
    }

    public static final class FailingWildcardChildProvider {

        public BigDecimal first() {
            throw new IllegalStateException("first failed");
        }
    }
}
