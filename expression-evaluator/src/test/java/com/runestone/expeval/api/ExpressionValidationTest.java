package com.runestone.expeval.api;

import com.runestone.expeval.environment.ExpressionEnvironment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

@DisplayName("Expression validation — ValidationResult API")
class ExpressionValidationTest {

    // -------------------------------------------------------------------------
    // ValidationResult record
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("ValidationResult record")
    class ValidationResultBehavior {

        @Test
        @DisplayName("ok() is valid with empty issues and preserves source")
        void okIsValidWithEmptyIssues() {
            var result = ValidationResult.ok("1 + 1", Set.of(), Set.of(), Set.of());

            assertThat(result.valid()).isTrue();
            assertThat(result.issues()).isEmpty();
            assertThat(result.source()).isEqualTo("1 + 1");
        }

        @Test
        @DisplayName("failed() is not valid and carries the provided issues")
        void failedIsNotValidWithIssues() {
            var issue = new CompilationIssue("SOME_CODE", "some message");
            var result = ValidationResult.failed("bad expr", List.of(issue));

            assertThat(result.valid()).isFalse();
            assertThat(result.issues()).containsExactly(issue);
            assertThat(result.source()).isEqualTo("bad expr");
        }

        @Test
        @DisplayName("issues list is unmodifiable after construction")
        void issuesListIsUnmodifiable() {
            var result = ValidationResult.ok("1 + 1", Set.of(), Set.of(), Set.of());

            assertThat(result.issues()).isUnmodifiable();
        }

        @Test
        @DisplayName("null source throws NullPointerException")
        void nullSourceThrows() {
            assertThatNullPointerException()
                .isThrownBy(() -> new ValidationResult(null, true, List.of(), Set.of(), Set.of(), Set.of()));
        }

        @Test
        @DisplayName("null issues list throws NullPointerException")
        void nullIssuesThrows() {
            assertThatNullPointerException()
                .isThrownBy(() -> new ValidationResult("expr", true, null, Set.of(), Set.of(), Set.of()));
        }

        @Test
        @DisplayName("null assignedVariables throws NullPointerException")
        void nullAssignedVariablesThrows() {
            assertThatNullPointerException()
                .isThrownBy(() -> new ValidationResult("expr", true, List.of(), null, Set.of(), Set.of()));
        }

        @Test
        @DisplayName("null userVariables throws NullPointerException")
        void nullUserVariablesThrows() {
            assertThatNullPointerException()
                .isThrownBy(() -> new ValidationResult("expr", true, List.of(), Set.of(), null, Set.of()));
        }

        @Test
        @DisplayName("null functions throws NullPointerException")
        void nullFunctionsThrows() {
            assertThatNullPointerException()
                .isThrownBy(() -> new ValidationResult("expr", true, List.of(), Set.of(), Set.of(), null));
        }

        @Test
        @DisplayName("formatMessage() on a valid result contains the source expression")
        void formatMessageValidContainsSource() {
            var result = ValidationResult.ok("1 + 1", Set.of(), Set.of(), Set.of());

            assertThat(result.formatMessage()).contains("1 + 1");
        }

        @Test
        @DisplayName("formatMessage() on an invalid result contains the source line")
        void formatMessageInvalidContainsSourceLine() {
            var result = MathExpression.validate("missing() + 1");

            assertThat(result.formatMessage()).contains("missing() + 1");
        }

        @Test
        @DisplayName("formatMessage() on an invalid result contains the caret pointer")
        void formatMessageInvalidContainsCaret() {
            var result = MathExpression.validate("missing() + 1");

            assertThat(result.formatMessage()).contains("^");
        }

        @Test
        @DisplayName("formatMessage() on an invalid result contains the issue code")
        void formatMessageInvalidContainsIssueCode() {
            var result = MathExpression.validate("missing() + 1");

            assertThat(result.formatMessage()).contains("UNKNOWN_FUNCTION");
        }

        @Test
        @DisplayName("formatMessage() on a syntax error contains SYNTAX_ERROR code")
        void formatMessageSyntaxErrorContainsCode() {
            var result = MathExpression.validate("a = ; 1");

            assertThat(result.formatMessage()).contains("SYNTAX_ERROR");
        }

        @Test
        @DisplayName("formatMessage() on a syntax error contains position reference")
        void formatMessageSyntaxErrorContainsPosition() {
            var result = MathExpression.validate("a = ; 1");

            assertThat(result.formatMessage()).contains("at 1:");
        }
    }

    // -------------------------------------------------------------------------
    // MathExpression.validate
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("MathExpression.validate")
    class MathValidation {

        @Test
        @DisplayName("returns valid for a syntactically and semantically correct expression")
        void validConstantExpression() {
            var result = MathExpression.validate("1 + 2 * 3");

            assertThat(result.valid()).isTrue();
            assertThat(result.issues()).isEmpty();
        }

        @Test
        @DisplayName("returns valid for expression with free variables — resolved at evaluation time")
        void validExpressionWithFreeVariables() {
            var result = MathExpression.validate("principal * rate");

            assertThat(result.valid()).isTrue();
        }

        @Test
        @DisplayName("returns valid when external symbols are declared in the environment")
        void validExpressionWithDeclaredExternalSymbols() {
            ExpressionEnvironment env = ExpressionEnvironment.builder()
                .registerExternalSymbol("principal", BigDecimal.ONE, true)
                .build();

            var result = MathExpression.validate("principal * 1.1", env);

            assertThat(result.valid()).isTrue();
        }

        @Test
        @DisplayName("does not throw on syntax error — returns invalid result")
        void syntaxErrorDoesNotThrow() {
            var result = MathExpression.validate("a = ; 1");

            assertThat(result.valid()).isFalse();
        }

        @Test
        @DisplayName("syntax error has SYNTAX_ERROR issue code")
        void syntaxErrorHasSyntaxErrorCode() {
            var result = MathExpression.validate("a = ; 1");

            assertThat(result.issues())
                .isNotEmpty()
                .allSatisfy(issue -> assertThat(issue.code()).isEqualTo("SYNTAX_ERROR"));
        }

        @Test
        @DisplayName("does not throw on unknown function — returns invalid result")
        void unknownFunctionDoesNotThrow() {
            var result = MathExpression.validate("missing() + 1");

            assertThat(result.valid()).isFalse();
        }

        @Test
        @DisplayName("unknown function has UNKNOWN_FUNCTION issue code")
        void unknownFunctionHasCorrectCode() {
            var result = MathExpression.validate("missing() + 1");

            assertThat(result.issues())
                .isNotEmpty()
                .anySatisfy(issue -> assertThat(issue.code()).isEqualTo("UNKNOWN_FUNCTION"));
        }

        @Test
        @DisplayName("does not throw on type mismatch — returns invalid result")
        void typeMismatchDoesNotThrow() {
            ExpressionEnvironment env = ExpressionEnvironment.builder()
                .registerExternalSymbol("flag", true, false)
                .build();

            var result = MathExpression.validate("flag + 1", env);

            assertThat(result.valid()).isFalse();
        }

        @Test
        @DisplayName("type mismatch issue carries source position")
        void typeMismatchIssueHasPosition() {
            ExpressionEnvironment env = ExpressionEnvironment.builder()
                .registerExternalSymbol("flag", true, false)
                .build();

            var result = MathExpression.validate("flag + 1", env);

            assertThat(result.issues())
                .isNotEmpty()
                .allSatisfy(issue -> assertThat(issue.position()).isNotNull());
        }

        @Test
        @DisplayName("formatMessage() mirrors ExpressionCompilationException formatting for semantic errors")
        void formatMessageMirrorsExceptionForSemanticErrors() {
            var result = MathExpression.validate("missing() + 1");

            String message = result.formatMessage();
            assertThat(message)
                .contains("missing() + 1")
                .contains("^")
                .contains("UNKNOWN_FUNCTION")
                .contains("at 1:0");
        }

        @Test
        @DisplayName("formatMessage() includes caret under the invalid type in type-mismatch error")
        void formatMessageIncludesCaretForTypeMismatch() {
            ExpressionEnvironment env = ExpressionEnvironment.builder()
                .registerExternalSymbol("flag", true, false)
                .build();

            var result = MathExpression.validate("flag + 1", env);

            assertThat(result.formatMessage())
                .contains("flag + 1")
                .contains("^");
        }

        @Test
        @DisplayName("null source throws NullPointerException")
        void nullSourceThrows() {
            assertThatNullPointerException()
                .isThrownBy(() -> MathExpression.validate(null));
        }

        @Test
        @DisplayName("null environment throws NullPointerException")
        void nullEnvironmentThrows() {
            assertThatNullPointerException()
                .isThrownBy(() -> MathExpression.validate("1 + 1", null));
        }

        @Test
        @DisplayName("validating twice returns the same valid result — cache is transparent")
        void idempotentValidation() {
            var first = MathExpression.validate("1 + 2");
            var second = MathExpression.validate("1 + 2");

            assertThat(first.valid()).isTrue();
            assertThat(second.valid()).isTrue();
        }

        @Test
        @DisplayName("validate followed by compile does not fail — cache is shared")
        void validateThenCompileSucceeds() {
            MathExpression.validate("10 * 5");

            var expression = MathExpression.compile("10 * 5");
            assertThat(expression.compute()).isEqualByComparingTo("50");
        }
    }

    // -------------------------------------------------------------------------
    // LogicalExpression.validate
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("LogicalExpression.validate")
    class LogicalValidation {

        @Test
        @DisplayName("returns valid for a correct logical expression")
        void validLogicalExpression() {
            var result = LogicalExpression.validate("1 > 0");

            assertThat(result.valid()).isTrue();
            assertThat(result.issues()).isEmpty();
        }

        @Test
        @DisplayName("returns valid for expression with free variables")
        void validLogicalExpressionWithFreeVariables() {
            var result = LogicalExpression.validate("x > 10");

            assertThat(result.valid()).isTrue();
        }

        @Test
        @DisplayName("does not throw on syntax error — returns invalid result")
        void syntaxErrorDoesNotThrow() {
            var result = LogicalExpression.validate("true and");

            assertThat(result.valid()).isFalse();
            assertThat(result.issues()).isNotEmpty();
        }

        @Test
        @DisplayName("incompatible comparison types returns invalid result with issue code")
        void incompatibleComparisonTypesIsInvalid() {
            ExpressionEnvironment env = ExpressionEnvironment.builder()
                .registerExternalSymbol("n", BigDecimal.ONE, false)
                .registerExternalSymbol("d", LocalDate.now(), false)
                .build();

            var result = LogicalExpression.validate("n > d", env);

            assertThat(result.valid()).isFalse();
            assertThat(result.issues())
                .anySatisfy(issue -> assertThat(issue.code()).isEqualTo("INCOMPATIBLE_COMPARISON"));
        }

        @Test
        @DisplayName("formatMessage() includes source line and caret for syntax error")
        void formatMessageIncludesSourceForSyntaxError() {
            var result = LogicalExpression.validate("true and");

            assertThat(result.formatMessage())
                .contains("true and")
                .contains("^")
                .contains("at 1:");
        }

        @Test
        @DisplayName("null source throws NullPointerException")
        void nullSourceThrows() {
            assertThatNullPointerException()
                .isThrownBy(() -> LogicalExpression.validate(null));
        }
    }

    // -------------------------------------------------------------------------
    // AssignmentExpression.validate
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("AssignmentExpression.validate")
    class AssignmentValidation {

        @Test
        @DisplayName("returns valid for a correct assignment expression")
        void validAssignmentExpression() {
            var result = AssignmentExpression.validate("a = 1; b = 2;");

            assertThat(result.valid()).isTrue();
            assertThat(result.issues()).isEmpty();
        }

        @Test
        @DisplayName("does not throw on syntax error — returns invalid result")
        void syntaxErrorDoesNotThrow() {
            var result = AssignmentExpression.validate("a = ;");

            assertThat(result.valid()).isFalse();
            assertThat(result.issues()).isNotEmpty();
        }

        @Test
        @DisplayName("syntax error issue carries line and column in source position")
        void syntaxErrorIssueHasPosition() {
            var result = AssignmentExpression.validate("a = ;");

            assertThat(result.issues())
                .isNotEmpty()
                .allSatisfy(issue -> {
                    assertThat(issue.position()).isNotNull();
                    assertThat(issue.position().line()).isPositive();
                });
        }

        @Test
        @DisplayName("null source throws NullPointerException")
        void nullSourceThrows() {
            assertThatNullPointerException()
                .isThrownBy(() -> AssignmentExpression.validate(null));
        }
    }

    // -------------------------------------------------------------------------
    // Cross-cutting: compile() still throws — validate() is opt-in non-throwing
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("validate() vs compile() contract")
    class ValidateVsCompileContract {

        @ParameterizedTest(name = "[{index}] invalid: {0}")
        @DisplayName("validate() never throws for any invalid expression while compile() does")
        @ValueSource(strings = {"missing() + 1", "a = ; 1", "true and"})
        void validateNeverThrowsForInvalidExpressions(String source) {
            var result = MathExpression.validate(source);
            assertThat(result.valid()).isFalse();
        }
    }

    // -------------------------------------------------------------------------
    // Expression metadata in valid result
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Expression metadata in valid result")
    class ValidationResultMetadata {

        private static final ExpressionEnvironment WITH_MATH =
                ExpressionEnvironment.builder().addMathFunctions().build();

        @Test
        @DisplayName("userVariables contains all free variables referenced in expression")
        void userVariablesContainsAllFreeVariables() {
            var result = MathExpression.validate("principal * rate");

            assertThat(result.userVariables()).containsExactlyInAnyOrder("principal", "rate");
        }

        @Test
        @DisplayName("userVariables contains declared external symbols")
        void userVariablesContainsDeclaredExternalSymbols() {
            ExpressionEnvironment env = ExpressionEnvironment.builder()
                .registerExternalSymbol("x", BigDecimal.ONE, true)
                .build();

            var result = MathExpression.validate("x + 1", env);

            assertThat(result.userVariables()).contains("x");
        }

        @Test
        @DisplayName("userVariables is empty for constant-only expressions")
        void userVariablesEmptyForConstantExpression() {
            var result = MathExpression.validate("1 + 2 * 3");

            assertThat(result.userVariables()).isEmpty();
        }

        @Test
        @DisplayName("assignedVariables contains variables assigned within the expression")
        void assignedVariablesContainsInternalVariables() {
            var result = AssignmentExpression.validate("a = 1; b = 2;");

            assertThat(result.assignedVariables()).containsExactlyInAnyOrder("a", "b");
        }

        @Test
        @DisplayName("assignedVariables is empty for expressions without assignments")
        void assignedVariablesEmptyForNonAssignmentExpression() {
            var result = MathExpression.validate("1 + 2");

            assertThat(result.assignedVariables()).isEmpty();
        }

        @Test
        @DisplayName("functions contains all called function names")
        void functionsContainsAllCalledFunctions() {
            var result = MathExpression.validate("mean([1, 2]) + mean([3, 4])", WITH_MATH);

            assertThat(result.functions()).containsExactlyInAnyOrder("mean");
        }

        @Test
        @DisplayName("functions deduplicates when same function is called multiple times")
        void functionsDeduplicatedForRepeatedCalls() {
            var result = MathExpression.validate("mean([1, 2]) + mean([3, 4])", WITH_MATH);

            assertThat(result.functions()).hasSize(1);
        }

        @Test
        @DisplayName("functions contains distinct names for multiple different functions")
        void functionsContainsDistinctNamesForMultipleFunctions() {
            var result = MathExpression.validate("mean([1, 2]) + ln(10)", WITH_MATH);

            assertThat(result.functions()).containsExactlyInAnyOrder("mean", "ln");
        }

        @Test
        @DisplayName("functions is empty for expressions without function calls")
        void functionsEmptyForExpressionWithoutFunctionCalls() {
            var result = MathExpression.validate("x * 2");

            assertThat(result.functions()).isEmpty();
        }

        @Test
        @DisplayName("failed result has empty assignedVariables, userVariables, and functions")
        void failedResultHasEmptyMetadata() {
            var result = MathExpression.validate("missing() + 1");

            assertThat(result.assignedVariables()).isEmpty();
            assertThat(result.userVariables()).isEmpty();
            assertThat(result.functions()).isEmpty();
        }

        @Test
        @DisplayName("all metadata sets are unmodifiable")
        void metadataSetsAreUnmodifiable() {
            var result = MathExpression.validate("x + 1");

            assertThat(result.assignedVariables()).isUnmodifiable();
            assertThat(result.userVariables()).isUnmodifiable();
            assertThat(result.functions()).isUnmodifiable();
        }
    }
}
