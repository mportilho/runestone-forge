package com.runestone.expeval2.internal.runtime;

import com.runestone.expeval2.environment.ExpressionEnvironment;
import com.runestone.expeval2.internal.grammar.ExpressionResultType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * White-box tests for constant folding at execution-plan level.
 *
 * <p>These tests access package-private types ({@link ExecutableFunctionCall},
 * {@link ExecutionPlan}) to verify that folding is recorded in the plan at build time,
 * before any evaluation occurs.
 *
 * <p>Skipped categories:
 * <ul>
 *   <li>Concurrency — plan building is stateless; the cache guarantees single compilation per key.
 *   <li>Error path — invalid expressions are covered by SemanticResolverTest.
 * </ul>
 */
@DisplayName("Constant folding — ExecutionPlan structure")
class ConstantFoldingPlanTest {

    private static final ExpressionEnvironment ENV =
            ExpressionEnvironment.builder().addMathFunctions().build();

    private final ExpressionCompiler compiler = new ExpressionCompiler();

    // -----------------------------------------------------------------------
    // Foldable functions with literal arguments → isFolded() == true
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Foldable function call with all-literal arguments → folded at build time")
    class FoldedAtBuildTime {

        @Test
        @DisplayName("ln(literal) is folded")
        void lnWithLiteralIsFolded() {
            CompiledExpression compiled = compile("ln(1.05)");

            ExecutableFunctionCall call = resultFunctionCall(compiled);

            assertThat(call.isFolded()).isTrue();
        }

        @Test
        @DisplayName("lb(literal) is folded — lb is the binary logarithm (log base 2)")
        void lbWithLiteralIsFolded() {
            CompiledExpression compiled = compile("lb(256)");

            ExecutableFunctionCall call = resultFunctionCall(compiled);

            assertThat(call.isFolded()).isTrue();
        }

        @Test
        @DisplayName("folded node retains its original arguments for audit use")
        void foldedNodeRetainsFoldedArgs() {
            CompiledExpression compiled = compile("ln(1.05)");

            ExecutableFunctionCall call = resultFunctionCall(compiled);

            assertThat(call.foldedArgs()).isNotNull().hasSize(1);
            assertThat(call.foldedArgs()[0]).isInstanceOf(BigDecimal.class);
            assertThat((BigDecimal) call.foldedArgs()[0]).isEqualByComparingTo("1.05");
        }

        @Test
        @DisplayName("folded node carries a non-null precomputed result")
        void foldedNodeCarriesPrecomputedResult() {
            CompiledExpression compiled = compile("ln(1)");

            ExecutableFunctionCall call = resultFunctionCall(compiled);

            assertThat(call.foldedResult()).isNotNull();
        }

        @Test
        @DisplayName("ln(1.05) in a larger expression — the function-call subnode is folded")
        void lnSubnodeInBinaryOpIsFolded() {
            // ln(1.05) * n — result is a BinaryOp; left child is the folded call
            CompiledExpression compiled = compile("ln(1.05) * n");

            ExecutableNode result = compiled.executionPlan().resultExpression();
            assertThat(result).isInstanceOf(ExecutableBinaryOp.class);

            ExecutableNode left = ((ExecutableBinaryOp) result).left();
            assertThat(left).isInstanceOf(ExecutableFunctionCall.class);
            assertThat(((ExecutableFunctionCall) left).isFolded()).isTrue();
        }
    }

    // -----------------------------------------------------------------------
    // Non-foldable cases — isFolded() must remain false
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Function call that must NOT be folded")
    class NotFolded {

        @Test
        @DisplayName("ln(variable) is not folded — argument is not a literal")
        void lnWithVariableIsNotFolded() {
            CompiledExpression compiled = compile("ln(x)");

            ExecutableFunctionCall call = resultFunctionCall(compiled);

            assertThat(call.isFolded()).isFalse();
        }

        @Test
        @DisplayName("foldedResult is null when not folded")
        void foldedResultIsNullWhenNotFolded() {
            CompiledExpression compiled = compile("ln(x)");

            ExecutableFunctionCall call = resultFunctionCall(compiled);

            assertThat(call.foldedResult()).isNull();
            assertThat(call.foldedArgs()).isNull();
        }

        @Test
        @DisplayName("user-registered function with literal arg is not folded — not marked foldable")
        void userRegisteredFunctionIsNotFolded() {
            ExpressionEnvironment envWithUserFn = ExpressionEnvironment.builder()
                    .registerStaticProvider(PureFunctionFixture.class)
                    .build();

            CompiledExpression compiled = compiler.compile("doubleOf(2)", ExpressionResultType.MATH, envWithUserFn);

            ExecutableFunctionCall call = resultFunctionCall(compiled);

            assertThat(call.isFolded()).isFalse();
        }

        @Test
        @DisplayName("mean([1, 2, 3]) is not folded — vector literal is not an ExecutableLiteral")
        void meanWithVectorLiteralIsNotFolded() {
            CompiledExpression compiled = compile("mean([1, 2, 3])");

            ExecutableFunctionCall call = resultFunctionCall(compiled);

            assertThat(call.isFolded()).isFalse();
        }
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private CompiledExpression compile(String source) {
        return compiler.compile(source, ExpressionResultType.MATH, ENV);
    }

    private static ExecutableFunctionCall resultFunctionCall(CompiledExpression compiled) {
        ExecutableNode result = compiled.executionPlan().resultExpression();
        assertThat(result)
                .as("expected result expression to be a function call")
                .isInstanceOf(ExecutableFunctionCall.class);
        return (ExecutableFunctionCall) result;
    }

    /** Minimal fixture for a user-registered (non-foldable) provider. */
    public static class PureFunctionFixture {
        public static BigDecimal doubleOf(BigDecimal x) {
            return x.multiply(new BigDecimal("2"));
        }
    }
}
