package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.environment.ExpressionEnvironment;
import com.runestone.expeval.internal.grammar.ExpressionResultType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Comprehensive unit tests for function folding in ExecutableFunctionCall and ExecutionPlanBuilder.
 *
 * <p>Folding is a compile-time optimization that evaluates foldable functions with constant
 * arguments before execution, storing both the precomputed result and the original arguments
 * for audit trails.
 *
 * <p>Skipped categories:
 * <ul>
 *   <li>Concurrency — plan building is stateless; compilation is synchronized by the Caffeine cache.
 *   <li>Authorization — function folding has no security boundary.
 * </ul>
 */
@DisplayName("Function folding in ExecutableFunctionCall and ExecutionPlanBuilder")
class ExecutableFunctionCallFoldingTest {

    private static final ExpressionEnvironment ENV =
            ExpressionEnvironment.builder().addMathFunctions().build();

    private final ExpressionCompiler compiler = new ExpressionCompiler();

    // -----------------------------------------------------------------------
    // Happy Path: Foldable Functions with All-Literal Arguments
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Foldable function with all-literal arguments → folded at plan build time")
    class HappyPathFoldableFunction {

        @Test
        @DisplayName("Unary foldable function (ln) with literal argument is folded")
        void unaryFoldableFunctionIsFolded() {
            // ln(2) in binary context: ln(2) + n → left operand should be folded
            CompiledExpression compiled = compile("ln(2) + n");

            ExecutableNode result = compiled.executionPlan().resultExpression();
            assertThat(result).isInstanceOf(ExecutableBinaryOp.class);

            ExecutableBinaryOp binOp = (ExecutableBinaryOp) result;
            ExecutableFunctionCall call = (ExecutableFunctionCall) binOp.left();
            assertThat(call.isFolded()).isTrue();
            assertThat(call.foldedResult()).isNotNull();
        }

        @Test
        @DisplayName("Binary foldable function (log) with two literal arguments is folded")
        void binaryFoldableFunctionIsFolded() {
            CompiledExpression compiled = compile("log(2, 8)");

            ExecutableFunctionCall call = extractFunctionCall(compiled);

            assertThat(call.isFolded()).isTrue();
            assertThat(call.foldedResult()).isNotNull();
            assertThat(call.foldedArgs()).hasSize(2);
        }

        @Test
        @DisplayName("Foldable result is precomputed and stored in foldedResult field")
        void foldedResultContainsPrecomputedValue() {
            CompiledExpression compiled = compile("ln(1)");

            ExecutableFunctionCall call = extractFunctionCall(compiled);

            assertThat(call.foldedResult()).isNotNull();
            // ln(1) = 0
            assertThat((BigDecimal) call.foldedResult()).isEqualByComparingTo("0");
        }

        @Test
        @DisplayName("Folded arguments are stored for audit trail")
        void foldedArgsAreStored() {
            CompiledExpression compiled = compile("ln(2.5)");

            ExecutableFunctionCall call = extractFunctionCall(compiled);

            assertThat(call.foldedArgs()).isNotNull().hasSize(1);
            assertThat(call.foldedArgs()[0]).isInstanceOf(BigDecimal.class);
            assertThat((BigDecimal) call.foldedArgs()[0]).isEqualByComparingTo("2.5");
        }

        @Test
        @DisplayName("Nested foldable calls: ln(lb(256)) folds outer call to ln(8)")
        void nestedFoldableCallsAreFolded() {
            CompiledExpression compiled = compile("ln(lb(256))");

            ExecutableFunctionCall outer = extractFunctionCall(compiled);

            assertThat(outer.isFolded()).isTrue();
            // ln(8) ≈ 2.0794
            assertThat((BigDecimal) outer.foldedResult()).isCloseTo(
                    new BigDecimal("2.0794415417"),
                    org.assertj.core.api.Assertions.within(new BigDecimal("0.0000000001"))
            );
        }

        @Test
        @DisplayName("Two foldable inner calls: log(lb(4), lb(256)) folds to log(2, 8) = 3")
        void nestedFoldableWithTwoFoldedArgsAreFolded() {
            CompiledExpression compiled = compile("log(lb(4), lb(256))");

            ExecutableFunctionCall outer = extractFunctionCall(compiled);

            assertThat(outer.isFolded()).isTrue();
            assertThat((BigDecimal) outer.foldedResult()).isEqualByComparingTo("3");
        }

        @Test
        @DisplayName("Foldable as subnode in binary expression: ln(2) * n")
        void foldableSubnodeInBinaryOpRemainsFolded() {
            CompiledExpression compiled = compile("ln(2) * n");

            ExecutableNode result = compiled.executionPlan().resultExpression();
            assertThat(result).isInstanceOf(ExecutableBinaryOp.class);

            ExecutableNode left = ((ExecutableBinaryOp) result).left();
            assertThat(left).isInstanceOf(ExecutableFunctionCall.class);
            assertThat(((ExecutableFunctionCall) left).isFolded()).isTrue();
        }

        @Test
        @DisplayName("Vector with all-literal elements: mean([1, 2, 3]) is folded")
        void vectorWithLiteralElementsIsFolded() {
            CompiledExpression compiled = compile("mean([1, 2, 3])");

            ExecutableFunctionCall call = extractFunctionCall(compiled);

            assertThat(call.isFolded()).isTrue();
            assertThat((BigDecimal) call.foldedResult()).isEqualByComparingTo("2");
        }
    }

    // -----------------------------------------------------------------------
    // Non-Foldable Cases: Dynamic Arguments
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Function with dynamic arguments → NOT folded")
    class NonFoldableWithDynamicArgs {

        @Test
        @DisplayName("Function with variable argument is NOT folded")
        void functionWithVariableArgumentIsNotFolded() {
            CompiledExpression compiled = compile("ln(x)");

            ExecutableFunctionCall call = extractFunctionCall(compiled);

            assertThat(call.isFolded()).isFalse();
            assertThat(call.foldedResult()).isNull();
            assertThat(call.foldedArgs()).isNull();
        }

        @Test
        @DisplayName("Function with computed argument is NOT folded")
        void functionWithComputedArgumentIsNotFolded() {
            CompiledExpression compiled = compile("ln(x + 1) + 0");

            ExecutableNode result = compiled.executionPlan().resultExpression();
            assertThat(result).isInstanceOf(ExecutableBinaryOp.class);

            ExecutableFunctionCall call = (ExecutableFunctionCall) ((ExecutableBinaryOp) result).left();
            assertThat(call.isFolded()).isFalse();
        }

        @Test
        @DisplayName("Function with mixed arguments (some constant, some dynamic) is NOT folded")
        void functionWithMixedArgumentsIsNotFolded() {
            CompiledExpression compiled = compile("log(10, x)");

            ExecutableFunctionCall call = extractFunctionCall(compiled);

            assertThat(call.isFolded()).isFalse();
        }

        @Test
        @DisplayName("Nested with dynamic inner call is NOT folded")
        void functionWithNestedDynamicCallIsNotFolded() {
            CompiledExpression compiled = compile("ln(lb(x)) + 0");

            ExecutableNode result = compiled.executionPlan().resultExpression();
            ExecutableFunctionCall outer = (ExecutableFunctionCall) ((ExecutableBinaryOp) result).left();

            assertThat(outer.isFolded()).isFalse();
            ExecutableFunctionCall inner = (ExecutableFunctionCall) outer.arguments().getFirst();
            assertThat(inner.isFolded()).isFalse();
        }

        @Test
        @DisplayName("Vector with dynamic element is NOT folded")
        void vectorWithDynamicElementIsNotFolded() {
            CompiledExpression compiled = compile("mean([1, x, 3])");

            ExecutableFunctionCall call = extractFunctionCall(compiled);

            assertThat(call.isFolded()).isFalse();
            ExecutableVectorLiteral vector = (ExecutableVectorLiteral) call.arguments().get(0);
            assertThat(vector.isFolded()).isFalse();
        }
    }

    // -----------------------------------------------------------------------
    // Non-Foldable User Functions: Not Marked Foldable
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("User-registered functions without foldable flag")
    class NonFoldableUserFunctions {

        @Test
        @DisplayName("User function with literal arguments is NOT folded (not marked foldable)")
        void userFunctionWithLiteralArgumentsIsNotFolded() {
            ExpressionEnvironment envWithUser = ExpressionEnvironment.builder()
                    .registerStaticProvider(UserFunctionFixture.class)
                    .build();

            CompiledExpression compiled = compiler.compile(
                    "tripleOf(5)",
                    ExpressionResultType.MATH,
                    envWithUser
            );

            ExecutableFunctionCall call = extractFunctionCall(compiled);

            assertThat(call.isFolded()).isFalse();
            assertThat(call.binding().descriptor().isFoldable()).isFalse();
        }
    }

    // -----------------------------------------------------------------------
    // Type Coercion During Folding
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Type coercion during function folding")
    class TypeCoercionDuringFolding {

        @Test
        @DisplayName("Folded function coerces numeric argument to parameter type")
        void numericCoercionInFolding() {
            CompiledExpression compiled = compile("ln(10)");

            ExecutableFunctionCall call = extractFunctionCall(compiled);

            assertThat(call.foldedArgs()[0]).isInstanceOf(BigDecimal.class);
        }

        @Test
        @DisplayName("Result of folded function is in expected type (BigDecimal for math)")
        void resultTypeInFolding() {
            CompiledExpression compiled = compile("ln(2.5)");

            ExecutableFunctionCall call = extractFunctionCall(compiled);

            assertThat(call.foldedResult()).isInstanceOf(BigDecimal.class);
        }

        @Test
        @DisplayName("Multi-argument function coerces all arguments correctly")
        void multiArgCoercionInFolding() {
            CompiledExpression compiled = compile("log(2.5, 100)");

            ExecutableFunctionCall call = extractFunctionCall(compiled);

            assertThat(call.foldedArgs()).hasSize(2);
            assertThat(call.foldedArgs()[0]).isInstanceOf(BigDecimal.class);
            assertThat(call.foldedArgs()[1]).isInstanceOf(BigDecimal.class);
        }
    }

    // -----------------------------------------------------------------------
    // Error Path: Invalid Arguments to Foldable Functions
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Error handling during function folding")
    class ErrorPathDuringFolding {

        @Test
        @DisplayName("Null argument to foldable function fails at compile time")
        void nullArgumentToFoldableFunctionFails() {
            assertThatThrownBy(() -> compile("ln(null)"))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("Invalid type argument to foldable function fails at compile time")
        void invalidTypeArgumentFails() {
            assertThatThrownBy(() -> compile("ln(\"hello\")"))
                    .isInstanceOf(Exception.class);
        }
    }

    // -----------------------------------------------------------------------
    // Folding in Different Expression Contexts
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Function folding in different expression contexts")
    class FoldingContexts {

        @Test
        @DisplayName("Folded function in assignment: x = ln(2.7); x")
        void foldedFunctionInAssignment() {
            CompiledExpression compiled = compileAssignments("x = ln(2.7); x");

            ExecutionPlan plan = compiled.executionPlan();
            ExecutableSimpleAssignment assignment = (ExecutableSimpleAssignment) plan.assignments().get(0);

            assertThat(assignment.value()).isInstanceOf(ExecutableFunctionCall.class);
            assertThat(((ExecutableFunctionCall) assignment.value()).isFolded()).isTrue();
        }

        @Test
        @DisplayName("Folded function in vector literal: mean([ln(1), ln(2.7), ln(10)])")
        void foldedFunctionInVectorLiteral() {
            CompiledExpression compiled = compile("mean([ln(1), ln(2.7), ln(10)])");

            ExecutableNode result = compiled.executionPlan().resultExpression();
            assertThat(result).isInstanceOf(ExecutableFunctionCall.class);

            ExecutableFunctionCall call = (ExecutableFunctionCall) result;
            ExecutableVectorLiteral vector = (ExecutableVectorLiteral) call.arguments().get(0);

            // All vector elements should be folded function calls
            for (ExecutableNode element : vector.elements()) {
                assertThat(element).isInstanceOf(ExecutableFunctionCall.class);
                assertThat(((ExecutableFunctionCall) element).isFolded()).isTrue();
            }
        }

        @Test
        @DisplayName("Folded function in null-coalescing: ln(1) ?? 999")
        void foldedFunctionInNullCoalescing() {
            CompiledExpression compiled = compile("ln(1) ?? 999");

            ExecutableNode result = compiled.executionPlan().resultExpression();
            // ln(1) = 0 (not null), so entire expression reduces to ExecutableLiteral
            assertThat(result).isInstanceOf(ExecutableLiteral.class);
            assertThat((BigDecimal) ((ExecutableLiteral) result).precomputed())
                    .isEqualByComparingTo("0");
        }
    }

    // -----------------------------------------------------------------------
    // Folding with External Symbols
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Folding behavior with external symbols")
    class FoldingWithExternalSymbols {

        @Test
        @DisplayName("Foldable function with external variable (overridable) is NOT folded")
        void foldableWithOverridableExternalNotFolded() {
            CompiledExpression compiled = compile("ln(extVar)");

            ExecutableFunctionCall call = extractFunctionCall(compiled);

            assertThat(call.isFolded()).isFalse();
        }

        @Test
        @DisplayName("Foldable function with non-overridable external (with default) IS folded")
        void foldableWithNonOverridableExternalIsFolded() {
            ExpressionEnvironment envWithExternal = ExpressionEnvironment.builder()
                    .addMathFunctions()
                    .registerExternalSymbol("PI", new BigDecimal("3.14159265"), false)
                    .build();

            CompiledExpression compiled = compiler.compile(
                    "ln(PI)",
                    ExpressionResultType.MATH,
                    envWithExternal
            );

            ExecutableFunctionCall call = extractFunctionCall(compiled);

            assertThat(call.isFolded()).isTrue();
        }
    }

    // -----------------------------------------------------------------------
    // Correctness: Folded Results Have Correct Values
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Verification of folded result correctness")
    class CorrectnessOfFoldedResults {

        @Test
        @DisplayName("Folded ln(1) = 0 (exact)")
        void foldedLnOfOneEqualsZero() {
            CompiledExpression compiled = compile("ln(1)");

            ExecutableFunctionCall call = extractFunctionCall(compiled);

            assertThat((BigDecimal) call.foldedResult()).isEqualByComparingTo("0");
        }

        @Test
        @DisplayName("Folded log(2, 8) = 3 (exact)")
        void foldedLogOf2Base8EqualsThree() {
            CompiledExpression compiled = compile("log(2, 8)");

            ExecutableFunctionCall call = extractFunctionCall(compiled);

            assertThat((BigDecimal) call.foldedResult()).isEqualByComparingTo("3");
        }

        @Test
        @DisplayName("Folded log(10, 100) = 2 (exact)")
        void foldedLogOf10Base100EqualsTwo() {
            CompiledExpression compiled = compile("log(10, 100)");

            ExecutableFunctionCall call = extractFunctionCall(compiled);

            assertThat((BigDecimal) call.foldedResult()).isEqualByComparingTo("2");
        }

        @Test
        @DisplayName("Folded mean([1, 2, 3]) = 2 (exact)")
        void foldedMeanOfOneToThreeEqualsTwo() {
            CompiledExpression compiled = compile("mean([1, 2, 3])");

            ExecutableFunctionCall call = extractFunctionCall(compiled);

            assertThat((BigDecimal) call.foldedResult()).isEqualByComparingTo("2");
        }

        @Test
        @DisplayName("Folded mean([10, 20, 30]) = 20 (exact)")
        void foldedMeanOfTenTwentyThirtyEqualsTwenty() {
            CompiledExpression compiled = compile("mean([10, 20, 30])");

            ExecutableFunctionCall call = extractFunctionCall(compiled);

            assertThat((BigDecimal) call.foldedResult()).isEqualByComparingTo("20");
        }
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private CompiledExpression compile(String source) {
        return compiler.compile(source, ExpressionResultType.MATH, ENV);
    }

    private CompiledExpression compileLogical(String source) {
        return compiler.compile(source, ExpressionResultType.LOGICAL, ENV);
    }

    private CompiledExpression compileAssignments(String source) {
        return compiler.compile(source, ExpressionResultType.MATH, ENV);
    }

    private static ExecutableFunctionCall extractFunctionCall(CompiledExpression compiled) {
        ExecutableNode result = compiled.executionPlan().resultExpression();
        assertThat(result)
                .as("expected result expression to be a function call")
                .isInstanceOf(ExecutableFunctionCall.class);
        return (ExecutableFunctionCall) result;
    }

    /** User-registered function provider without foldable flag. */
    public static class UserFunctionFixture {
        public static BigDecimal tripleOf(BigDecimal x) {
            return x.multiply(new BigDecimal("3"));
        }
    }
}
