package com.runestone.expeval_mk3.corpus;

import com.fasterxml.jackson.databind.JsonNode;
import com.runestone.expeval_mk3.api.ExpressionCompiler;
import com.runestone.expeval_mk3.api.ExpressionEnvironment;
import com.runestone.expeval_mk3.api.ExpressionType;
import com.runestone.expeval_mk3.api.ExternalSymbolOverwritePolicy;
import com.runestone.expeval_mk3.api.ScalarType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ScalarRuntimeCorpusTest {

    private static final Set<String> CASE_IDS = Set.of(
            "runtime.arithmetic.add.001",
            "runtime.arithmetic.decimal.001",
            "runtime.arithmetic.exponentiation.001",
            "runtime.arithmetic.factorial.001",
            "runtime.arithmetic.modulo.001",
            "runtime.arithmetic.multiply-precedence.001",
            "runtime.arithmetic.parentheses.001",
            "runtime.arithmetic.percent.001",
            "runtime.arithmetic.root.001",
            "runtime.assignment.basic.001",
            "runtime.comparison.between.001",
            "runtime.comparison.in.001",
            "runtime.comparison.less-than.001",
            "runtime.comparison.not-in.001",
            "runtime.conditional.classic.001",
            "runtime.conditional.functional.001",
            "runtime.external.default.001",
            "runtime.external.symbols.001",
            "runtime.logical.and.001",
            "runtime.logical.or.001",
            "runtime.logical.xor.001",
            "runtime.null.coalesce.001",
            "runtime.string.concat.001");

    @Test
    void executesTheScalarRuntimeSliceFromTheExpressionCorpus() {
        var cases = ExpressionCaseLoader.loadAll().stream()
                .filter(expressionCase -> CASE_IDS.contains(expressionCase.id()))
                .toList();

        assertThat(cases).hasSize(CASE_IDS.size());
        for (ExpressionCase expressionCase : cases) {
            ExpectedResult expected = (ExpectedResult) expressionCase.expectedOutcome();
            Object actual = ExpressionCompiler.compile(expressionCase.source(), environment(expressionCase))
                    .compute(inputs(expressionCase));

            assertExpectedValue(expressionCase.id(), expected.type(), expected.result(), actual);
        }
    }

    private static void assertExpectedValue(String caseId, String type, JsonNode expected, Object actual) {
        if ("NUMBER".equals(type)) {
            assertThat((BigDecimal) actual)
                    .as(caseId)
                    .isEqualByComparingTo(new BigDecimal(expected.textValue()));
            return;
        }
        assertThat(actual)
                .as(caseId)
                .isEqualTo(expectedValue(type, expected));
    }

    private static ExpressionEnvironment environment(ExpressionCase expressionCase) {
        JsonNode environment = expressionCase.root().get("environment");
        if (environment == null) {
            return ExpressionEnvironment.standard();
        }
        ExpressionEnvironment.Builder builder = ExpressionEnvironment.builder();
        JsonNode maxMaterializedSize = environment.get("maxMaterializedSize");
        if (maxMaterializedSize != null) {
            builder.maxMaterializedSize(maxMaterializedSize.intValue());
        }
        JsonNode maxFactorialInput = environment.get("maxFactorialInput");
        if (maxFactorialInput != null) {
            builder.maxFactorialInput(maxFactorialInput.intValue());
        }
        JsonNode symbols = environment.get("symbols");
        if (symbols != null) {
            Iterator<Map.Entry<String, JsonNode>> fields = symbols.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> symbol = fields.next();
                JsonNode declaration = symbol.getValue();
                builder.externalSymbol(
                        symbol.getKey(),
                        expressionType(declaration.get("type").textValue()),
                        expectedValue(
                                declaration.get("default").get("type").textValue(),
                                declaration.get("default").get("value")),
                        ExternalSymbolOverwritePolicy.valueOf(declaration.get("overwritePolicy").textValue()));
            }
        }
        return builder.build();
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
            values.put(input.getKey(), expectedValue(
                    input.getValue().get("type").textValue(),
                    input.getValue().get("value")));
        }
        return Map.copyOf(values);
    }

    private static ExpressionType expressionType(String type) {
        return switch (type) {
            case "NUMBER" -> ScalarType.NUMBER;
            case "BOOLEAN" -> ScalarType.BOOLEAN;
            case "STRING" -> ScalarType.STRING;
            default -> throw new IllegalArgumentException("Unsupported scalar corpus type: " + type);
        };
    }

    private static Object expectedValue(String type, JsonNode value) {
        return switch (type) {
            case "NUMBER" -> new BigDecimal(value.textValue());
            case "BOOLEAN" -> value.booleanValue();
            case "STRING" -> value.textValue();
            default -> throw new IllegalArgumentException("Unsupported expected corpus type: " + type);
        };
    }
}
