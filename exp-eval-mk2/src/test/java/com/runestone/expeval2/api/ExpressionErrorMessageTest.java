package com.runestone.expeval2.api;

import com.runestone.expeval2.environment.ExpressionEnvironment;
import com.runestone.expeval2.environment.ExpressionEnvironmentBuilder;
import com.runestone.expeval2.internal.grammar.ParsingException;
import com.runestone.expeval2.types.ScalarType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

@DisplayName("Exception error messages — formatted output with source pointer")
class ExpressionErrorMessageTest {

    // -------------------------------------------------------------------------
    // Parsing errors
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Parsing errors — invalid syntax")
    class ParsingErrors {

        @Test
        @DisplayName("includes source line and syntax-error position")
        void includesSourceLineAndSyntaxErrorPosition() {
            assertThatThrownBy(() -> MathExpression.compile("a = ; 1"))
                .isInstanceOf(ParsingException.class)
                .hasMessageContaining("a = ; 1")
                .hasMessageContaining("syntax error at 1:4");
        }

        @Test
        @DisplayName("includes pointer caret at error column")
        void includesPointerCaretAtErrorColumn() {
            // "a = ; 1" — error at column 4, pointer should appear at that offset
            assertThatThrownBy(() -> MathExpression.compile("a = ; 1"))
                .isInstanceOf(ParsingException.class)
                .hasMessageContaining("^");
        }

        @Test
        @DisplayName("includes source line and syntax-error position in logical expression")
        void includesSourceLineForLogicalExpression() {
            assertThatThrownBy(() -> LogicalExpression.compile("true and"))
                .isInstanceOf(ParsingException.class)
                .hasMessageContaining("true and")
                .hasMessageContaining("syntax error at 1:")
                .hasMessageContaining("^");
        }
    }

    // -------------------------------------------------------------------------
    // Compilation errors
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Compilation errors — semantic issues")
    class CompilationErrors {

        @Test
        @DisplayName("UNKNOWN_FUNCTION: formats message with source line and pointer under function call")
        void formatsUnknownFunctionError() {
            assertThatThrownBy(() -> MathExpression.compile("missing() + 1"))
                .isInstanceOf(ExpressionCompilationException.class)
                .hasMessageContaining("missing() + 1")
                .hasMessageContaining("^")
                .hasMessageContaining("UNKNOWN_FUNCTION")
                .hasMessageContaining("at 1:0");
        }

        @Test
        @DisplayName("INVALID_FUNCTION_ARITY: formats message with source line and pointer under function call")
        void formatsInvalidFunctionArityError() {
            ExpressionEnvironment env = new ExpressionEnvironmentBuilder()
                .registerStaticProvider(FunctionFixture.class)
                .build();

            assertThatThrownBy(() -> MathExpression.compile("bonus(1, 2)", env))
                .isInstanceOf(ExpressionCompilationException.class)
                .hasMessageContaining("bonus(1, 2)")
                .hasMessageContaining("^")
                .hasMessageContaining("INVALID_FUNCTION_ARITY")
                .hasMessageContaining("at 1:");
        }

        @Test
        @DisplayName("AMBIGUOUS_FUNCTION: formats message with source line and pointer under function call")
        void formatsAmbiguousFunctionError() {
            ExpressionEnvironment env = new ExpressionEnvironmentBuilder()
                .registerStaticProvider(FunctionFixture.class)
                .build();

            assertThatThrownBy(() -> MathExpression.compile("pick(value) + 1", env))
                .isInstanceOf(ExpressionCompilationException.class)
                .hasMessageContaining("pick(value) + 1")
                .hasMessageContaining("^")
                .hasMessageContaining("AMBIGUOUS_FUNCTION")
                .hasMessageContaining("at 1:");
        }

        @Test
        @DisplayName("TYPE_MISMATCH: formats message when boolean symbol used in arithmetic")
        void formatsTypeMismatchError() {
            ExpressionEnvironment env = ExpressionEnvironment.builder()
                .registerExternalSymbol("flag", ScalarType.BOOLEAN, true, false)
                .build();

            assertThatThrownBy(() -> MathExpression.compile("flag + 1", env))
                .isInstanceOf(ExpressionCompilationException.class)
                .hasMessageContaining("flag + 1")
                .hasMessageContaining("^")
                .hasMessageContaining("TYPE_MISMATCH")
                .hasMessageContaining("at 1:");
        }

        @Test
        @DisplayName("RESULT_TYPE_MISMATCH: formats message when boolean symbol is the math result")
        void formatsResultTypeMismatchError() {
            ExpressionEnvironment env = ExpressionEnvironment.builder()
                .registerExternalSymbol("flag", ScalarType.BOOLEAN, true, false)
                .build();

            assertThatThrownBy(() -> MathExpression.compile("flag", env))
                .isInstanceOf(ExpressionCompilationException.class)
                .hasMessageContaining("flag")
                .hasMessageContaining("^")
                .hasMessageContaining("RESULT_TYPE_MISMATCH")
                .hasMessageContaining("at 1:");
        }

        @Test
        @DisplayName("INCOMPATIBLE_COMPARISON: formats message when a number is compared to a date")
        void formatsIncompatibleComparisonError() {
            // n (NUMBER) > d (DATE) — types differ, neither is UNKNOWN
            ExpressionEnvironment env = ExpressionEnvironment.builder()
                .registerExternalSymbol("n", ScalarType.NUMBER, BigDecimal.ONE, false)
                .registerExternalSymbol("d", ScalarType.DATE, LocalDate.now(), false)
                .build();

            assertThatThrownBy(() -> LogicalExpression.compile("n > d", env))
                .isInstanceOf(ExpressionCompilationException.class)
                .hasMessageContaining("n > d")
                .hasMessageContaining("^")
                .hasMessageContaining("INCOMPATIBLE_COMPARISON")
                .hasMessageContaining("at 1:");
        }

        @Test
        @DisplayName("exposes source expression via source() accessor")
        void exposesSourceViaAccessor() {
            assertThatThrownBy(() -> MathExpression.compile("missing() + 1"))
                .isInstanceOf(ExpressionCompilationException.class)
                .asInstanceOf(type(ExpressionCompilationException.class))
                .extracting(ExpressionCompilationException::source)
                .isEqualTo("missing() + 1");
        }

        @Test
        @DisplayName("exposes issue with source position via issues() accessor")
        void exposesIssuePositionViaAccessor() {
            assertThatThrownBy(() -> MathExpression.compile("missing() + 1"))
                .isInstanceOf(ExpressionCompilationException.class)
                .asInstanceOf(type(ExpressionCompilationException.class))
                .satisfies(ex -> {
                    assertThat(ex.issues()).hasSize(1);
                    CompilationIssue issue = ex.issues().getFirst();
                    assertThat(issue.code()).isEqualTo("UNKNOWN_FUNCTION");
                    assertThat(issue.position()).isNotNull();
                    assertThat(issue.position().line()).isEqualTo(1);
                    assertThat(issue.position().column()).isEqualTo(0);
                });
        }
    }

    // -------------------------------------------------------------------------
    // Evaluation errors
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Evaluation errors — unbound variables")
    class EvaluationErrors {

        @Test
        @DisplayName("UNBOUND_VARIABLE: formats message with pointer under missing variable in math expression")
        void formatsUnboundVariableInMathExpression() {
            assertThatThrownBy(() -> MathExpression.compile("principal * 1.1").compute())
                .isInstanceOf(ExpressionEvaluationException.class)
                .hasMessageContaining("principal * 1.1")
                .hasMessageContaining("^")
                .hasMessageContaining("UNBOUND_VARIABLE")
                .hasMessageContaining("at 1:0");
        }

        @Test
        @DisplayName("UNBOUND_VARIABLE: pointer is aligned to variable column when not at expression start")
        void pointerAlignedToVariableColumn() {
            // "1 + principal" — 'principal' starts at column 4
            assertThatThrownBy(() -> MathExpression.compile("1 + principal").compute())
                .isInstanceOf(ExpressionEvaluationException.class)
                .hasMessageContaining("1 + principal")
                .hasMessageContaining("^")
                .hasMessageContaining("UNBOUND_VARIABLE")
                .hasMessageContaining("at 1:4");
        }

        @Test
        @DisplayName("UNBOUND_VARIABLE: formats message with pointer under missing variable in logical expression")
        void formatsUnboundVariableInLogicalExpression() {
            assertThatThrownBy(() -> LogicalExpression.compile("x > 10").compute())
                .isInstanceOf(ExpressionEvaluationException.class)
                .hasMessageContaining("x > 10")
                .hasMessageContaining("^")
                .hasMessageContaining("UNBOUND_VARIABLE")
                .hasMessageContaining("at 1:0");
        }

        @Test
        @DisplayName("message includes actionable setValue() hint with variable name")
        void messageIncludesActionableSetValueHint() {
            assertThatThrownBy(() -> MathExpression.compile("principal * 1.1").compute())
                .isInstanceOf(ExpressionEvaluationException.class)
                .hasMessageContaining("setValue(\"principal\"");
        }

        @Test
        @DisplayName("exposes source expression via source() accessor")
        void exposesSourceViaAccessor() {
            assertThatThrownBy(() -> MathExpression.compile("principal * 1.1").compute())
                .isInstanceOf(ExpressionEvaluationException.class)
                .asInstanceOf(type(ExpressionEvaluationException.class))
                .extracting(ExpressionEvaluationException::source)
                .isEqualTo("principal * 1.1");
        }
    }

    // -------------------------------------------------------------------------
    // Fixtures
    // -------------------------------------------------------------------------

    public static final class FunctionFixture {

        private FunctionFixture() {
        }

        public static BigDecimal bonus(BigDecimal principal) {
            return principal.multiply(BigDecimal.valueOf(0.10));
        }

        public static BigDecimal pick(BigDecimal value) {
            return value;
        }

        public static BigDecimal pick(String value) {
            return BigDecimal.valueOf(value.length());
        }
    }
}
