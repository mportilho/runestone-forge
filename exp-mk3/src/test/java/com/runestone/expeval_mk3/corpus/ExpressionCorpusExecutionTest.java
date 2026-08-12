package com.runestone.expeval_mk3.corpus;

import com.fasterxml.jackson.databind.JsonNode;
import com.runestone.expeval_mk3.api.ExpressionCompilationResult;
import com.runestone.expeval_mk3.api.ExpressionDiagnostic;
import com.runestone.expeval_mk3.api.ExpressionExecutionException;
import com.runestone.expeval_mk3.api.ExpressionCompiler;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

        var compiled = ExpressionCompiler.compileOrThrow(expressionCase.source(), ExpressionCaseEnvironments.environment(expressionCase));
        if (expressionCase.phase() == CasePhase.SEMANTIC) {
            return;
        }

        ExpectedResult expected = (ExpectedResult) expressionCase.expectedOutcome();
        Object actual = compiled.asResult().compute(ExpressionCaseEnvironments.inputs(expressionCase));
        assertExpectedValue(expressionCase.id(), expected.type(), expected.result(), actual);
    }

    static Stream<Arguments> semanticAndRuntimeCases() {
        return ExpressionCaseLoader.loadAll().stream()
                .filter(expressionCase -> expressionCase.phase() == CasePhase.SEMANTIC
                        || expressionCase.phase() == CasePhase.RUNTIME)
                .map(expressionCase -> Arguments.of(expressionCase.id(), expressionCase));
    }

    private static void assertInvalidCase(ExpressionCase expressionCase) {
        if (expressionCase.expectedOutcome() instanceof ExpectedRuntimeError expected) {
            assertThatThrownBy(() -> ExpressionCompiler.compileOrThrow(
                            expressionCase.source(), ExpressionCaseEnvironments.environment(expressionCase))
                    .asResult()
                    .compute(ExpressionCaseEnvironments.inputs(expressionCase)))
                    .as(expressionCase.id())
                    .isInstanceOf(runtimeErrorType(expected.type()))
                    .hasMessageContaining(expected.messageContains());
            return;
        }

        if (expressionCase.expectedOutcome() instanceof ExpectedRuntimeDiagnostic expected) {
            assertThatThrownBy(() -> ExpressionCompiler.compileOrThrow(
                            expressionCase.source(), ExpressionCaseEnvironments.environment(expressionCase))
                    .asResult()
                    .compute(ExpressionCaseEnvironments.inputs(expressionCase)))
                    .as(expressionCase.id())
                    .isInstanceOf(ExpressionExecutionException.class)
                    .satisfies(thrown -> {
                        ExpressionDiagnostic actual = ((ExpressionExecutionException) thrown).diagnostic();
                        assertThat(actual.category().name()).as(expressionCase.id()).isEqualTo("RUNTIME");
                        assertThat(actual.code()).as(expressionCase.id()).isEqualTo(expected.code());
                        if (!expected.spans().isEmpty()) {
                            assertThat(actual.primarySpan()).as(expressionCase.id()).contains(expected.spans().getFirst());
                        }
                    });
            return;
        }

        ExpectedDiagnostic expected = (ExpectedDiagnostic) expressionCase.expectedOutcome();
        ExpressionCompilationResult result =
                ExpressionCompiler.compile(expressionCase.source(), ExpressionCaseEnvironments.environment(expressionCase));
        assertThat(result)
                .as(expressionCase.id())
                .isInstanceOfSatisfying(ExpressionCompilationResult.Failure.class, failure -> {
                    ExpressionDiagnostic actual = failure.diagnostics().getFirst();
                    assertThat(actual.category().name()).isEqualTo(expected.category());
                    assertThat(actual.code()).isEqualTo(expected.code());
                    if (!expected.spans().isEmpty()) {
                        assertThat(actual.primarySpan()).contains(expected.spans().getFirst());
                    }
                });
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
        assertThat(actual).as(caseId).isEqualTo(ExpressionCaseEnvironments.scalarValue(type, expected));
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

    private static Class<? extends Throwable> runtimeErrorType(String type) {
        return switch (type) {
            case "ArithmeticException" -> ArithmeticException.class;
            case "IllegalStateException" -> IllegalStateException.class;
            case "ExpressionExecutionException" -> ExpressionExecutionException.class;
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

    /**
     * Returns a collection larger than the corpus cases' {@code maxMaterializedSize} so a navigation
     * link over a member-returned collection can reach the runtime materialization limit; a collection
     * arriving as an external symbol is already bounded by the boundary coercion.
     */
    public static final class CollectionMethodProvider {

        public List<BigDecimal> itens() {
            return List.of(
                    BigDecimal.ONE,
                    new BigDecimal("2"),
                    new BigDecimal("3"),
                    new BigDecimal("4"),
                    new BigDecimal("5"));
        }

        public BigDecimal[] valores() {
            return new BigDecimal[] {BigDecimal.ONE, new BigDecimal("2"), new BigDecimal("3")};
        }

        public Map<String, BigDecimal> mapa() {
            Map<String, BigDecimal> values = new LinkedHashMap<>();
            values.put("a", BigDecimal.ONE);
            values.put("b", new BigDecimal("2"));
            return values;
        }
    }

    public static final class FailingWildcardChildProvider {

        public BigDecimal first() {
            throw new IllegalStateException("first failed");
        }
    }

    /**
     * A {@code contrato} fixture whose {@code indice} is not {@code "PRE"}, exercising the canonical
     * Etapa 8 corpus expression's {@code else} branch (issue #124).
     */
    public static final class PostFixedContractProvider {

        public String getIndice() {
            return "POS";
        }
    }

    /**
     * Deliberately violates the registered-member non-null contract and the invocation-failure boundary so
     * a safe navigation link over each member form (property, call, argument, nested receiver) proves it
     * does not mask the corresponding runtime or semantic failure.
     */
    public static final class NavigationContractProvider {

        public String getName() {
            return "Ana";
        }

        public String getIndice() {
            return "PRE";
        }

        public String getMissing() {
            return null;
        }

        public String getBroken() {
            throw new IllegalStateException("broken accessor");
        }

        public CollectionMethodProvider getCatalog() {
            return new CollectionMethodProvider();
        }

        public String describe() {
            return "described";
        }

        public String fail() {
            throw new IllegalStateException("call failed");
        }

        public BigDecimal echo(BigDecimal value) {
            return value;
        }

        public Map<String, BigDecimal> attributes() {
            Map<String, BigDecimal> values = new LinkedHashMap<>();
            values.put("present", null);
            return values;
        }
    }
}
