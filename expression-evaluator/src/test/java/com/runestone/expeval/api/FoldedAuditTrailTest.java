package com.runestone.expeval.api;

import com.runestone.expeval.environment.ExpressionEnvironment;
import com.runestone.expeval.environment.ExpressionEnvironmentBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests that folded external symbols and folded internal variables produce {@link AuditEvent.VariableRead}
 * events in the audit trail, and that those events are pre-stored at compile time rather than
 * recomputed on every {@code computeWithAudit()} call.
 *
 * <p>Ordering note: pre-stored events for folded symbols are prepended to the audit trail and
 * therefore appear before runtime-evaluated variable reads, regardless of expression order.
 */
@DisplayName("Folded symbols — VariableRead events in audit trail")
class FoldedAuditTrailTest {

    private static final ExpressionEnvironment EMPTY = ExpressionEnvironmentBuilder.empty();

    // -----------------------------------------------------------------------
    // Folded external symbols
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Non-overridable external symbols")
    class FoldedExternalSymbols {

        @Test
        @DisplayName("single folded external produces one VariableRead with the compile-time value")
        void singleFoldedExternalProducesOneVariableRead() {
            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerExternalSymbol("CONST_X", new BigDecimal("10"), false)
                    .build();

            AuditResult<BigDecimal> result = MathExpression.compile("CONST_X + 1", env)
                    .computeWithAudit();

            List<AuditEvent.VariableRead> reads = variableReads(result);
            assertThat(reads).hasSize(1);
            assertThat(reads.getFirst().name()).isEqualTo("CONST_X");
            assertThat(reads.getFirst().systemProvided()).isFalse();
            assertThat(reads.getFirst().value()).isEqualTo(new BigDecimal("10"));
        }

        @Test
        @DisplayName("folded external used twice produces two VariableRead events")
        void foldedExternalUsedTwiceProducesTwoReads() {
            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerExternalSymbol("CONST_X", new BigDecimal("5"), false)
                    .build();

            AuditResult<BigDecimal> result = MathExpression.compile("CONST_X + CONST_X", env)
                    .computeWithAudit();

            assertThat(variableReads(result))
                    .extracting(AuditEvent.VariableRead::name)
                    .containsExactly("CONST_X", "CONST_X");
        }

        @Test
        @DisplayName("overridable external does NOT produce a pre-stored VariableRead (normal runtime path)")
        void overridableExternalDoesNotProducePreStoredRead() {
            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerExternalSymbol("VAR_X", new BigDecimal("7"), true)
                    .build();

            AuditResult<BigDecimal> result = MathExpression.compile("VAR_X + 1", env)
                    .computeWithAudit(Map.of("VAR_X", new BigDecimal("7")));

            List<AuditEvent.VariableRead> reads = variableReads(result);
            assertThat(reads).hasSize(1);
            assertThat(reads.getFirst().name()).isEqualTo("VAR_X");
        }

        @Test
        @DisplayName("mixed expression — folded read appears before runtime reads")
        void mixedExpressionFoldedReadsBeforeRuntimeReads() {
            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerExternalSymbol("CONST_A", new BigDecimal("3"), false)
                    .build();

            AuditResult<BigDecimal> result = MathExpression.compile("CONST_A + y", env)
                    .computeWithAudit(Map.of("y", new BigDecimal("2")));

            List<AuditEvent.VariableRead> reads = variableReads(result);
            assertThat(reads).extracting(AuditEvent.VariableRead::name)
                    .containsExactlyInAnyOrder("CONST_A", "y");
        }

        @Test
        @DisplayName("successive computeWithAudit() calls each produce the folded VariableRead")
        void successiveCallsEachProduceFoldedRead() {
            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerExternalSymbol("CONST_X", new BigDecimal("99"), false)
                    .build();
            MathExpression expr = MathExpression.compile("CONST_X", env);

            AuditResult<BigDecimal> first = expr.computeWithAudit();
            AuditResult<BigDecimal> second = expr.computeWithAudit();

            assertThat(variableReads(first)).hasSize(1);
            assertThat(variableReads(second)).hasSize(1);
            assertThat(first.trace()).isNotSameAs(second.trace());
        }
    }

    // -----------------------------------------------------------------------
    // Folded internal variables
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Internal variables folded from constant assignments")
    class FoldedInternalVariables {

        @Test
        @DisplayName("internal variable assigned a literal and used in result expression produces a VariableRead")
        void foldedInternalVariableProducesVariableRead() {
            AuditResult<BigDecimal> result = MathExpression.compile("x = 5; x + 1", EMPTY)
                    .computeWithAudit();

            List<AuditEvent.VariableRead> reads = variableReads(result);
            assertThat(reads)
                    .extracting(AuditEvent.VariableRead::name)
                    .contains("x");
            AuditEvent.VariableRead xRead = reads.stream()
                    .filter(r -> "x".equals(r.name()))
                    .findFirst().orElseThrow();
            assertThat(xRead.value()).isEqualTo(new BigDecimal("5"));
        }

        @Test
        @DisplayName("internal variable depending on a folded external is itself folded")
        void internalVariableDependingOnFoldedExternalIsFolded() {
            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerExternalSymbol("CONST_B", new BigDecimal("4"), false)
                    .build();

            AuditResult<BigDecimal> result = MathExpression.compile("derived = CONST_B * 2; derived", env)
                    .computeWithAudit();

            List<AuditEvent.VariableRead> reads = variableReads(result);
            assertThat(reads)
                    .extracting(AuditEvent.VariableRead::name)
                    .containsAnyOf("CONST_B", "derived");
        }
    }

    // -----------------------------------------------------------------------
    // Folded function calls with folded arguments
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Folded function calls — argument VariableRead + FunctionCall events")
    class FoldedFunctionCallWithFoldedArgs {

        @Test
        @DisplayName("folded function call whose argument is a folded external produces VariableRead + FunctionCall")
        void foldedFunctionCallProducesBothEvents() {
            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .addMathFunctions()
                    .registerExternalSymbol("CONST_ARG", new BigDecimal("1.05"), false)
                    .build();

            AuditResult<BigDecimal> result = MathExpression.compile("ln(CONST_ARG)", env)
                    .computeWithAudit();

            assertThat(variableReads(result))
                    .extracting(AuditEvent.VariableRead::name)
                    .contains("CONST_ARG");
            assertThat(result.trace().functionCalls())
                    .extracting(AuditEvent.FunctionCall::functionName)
                    .contains("ln");
        }
    }

    // -----------------------------------------------------------------------
    // Folded property-chain roots
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Folded property-chain root produces VariableRead even when chain is not folded")
    class FoldedPropertyChainRoot {

        record Point(BigDecimal x, BigDecimal y) {
        }

        @Test
        @DisplayName("navigating a property on a folded external emits VariableRead for the root")
        void foldedPropertyChainRootProducesVariableRead() {
            Point point = new Point(new BigDecimal("3"), new BigDecimal("4"));
            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerExternalSymbol("CONST_POINT", point, false)
                    .registerTypeHint(Point.class)
                    .build();

            AuditResult<BigDecimal> result = MathExpression.compile("CONST_POINT.x", env)
                    .computeWithAudit();

            assertThat(variableReads(result))
                    .extracting(AuditEvent.VariableRead::name)
                    .contains("CONST_POINT");
        }
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private static <T> List<AuditEvent.VariableRead> variableReads(AuditResult<T> result) {
        return result.trace().events().stream()
                .filter(AuditEvent.VariableRead.class::isInstance)
                .map(AuditEvent.VariableRead.class::cast)
                .toList();
    }
}
