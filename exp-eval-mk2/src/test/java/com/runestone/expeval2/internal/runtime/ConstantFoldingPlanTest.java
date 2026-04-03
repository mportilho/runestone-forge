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
            ExpressionEnvironment.builder()
                    .addMathFunctions()
                    .addStringFunctions()
                    .build();

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
        @DisplayName("ln(lb(256)) is folded — inner lb(256) is constant so outer ln is also foldable")
        void nestedFoldableFunctionsAreFolded() {
            // lb(256)=8 is folded first; ln(8) then receives a folded node as argument → also folded
            CompiledExpression compiled = compile("ln(lb(256))");

            ExecutableFunctionCall call = resultFunctionCall(compiled);

            assertThat(call.isFolded()).isTrue();
        }

        @Test
        @DisplayName("log(lb(4), lb(256)) is folded — both scalar arguments are folded inner calls")
        void nestedFoldableWithTwoFoldedArgsIsFolded() {
            // lb(4)=2, lb(256)=8; log(2, 8) = 3
            CompiledExpression compiled = compile("log(lb(4), lb(256))");

            ExecutableFunctionCall call = resultFunctionCall(compiled);

            assertThat(call.isFolded()).isTrue();
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

    @Nested
    @DisplayName("Operators folding → reduced to ExecutableLiteral")
    class OperatorsFolding {

        @Test
        @DisplayName("Binary arithmetic (1 + 2 * 3) is folded to 7")
        void binaryArithmeticIsFolded() {
            CompiledExpression compiled = compile("1 + 2 * 3");

            ExecutableNode result = compiled.executionPlan().resultExpression();
            assertThat(result).isInstanceOf(ExecutableLiteral.class);
            assertThat(((ExecutableLiteral) result).precomputed()).isEqualTo(new BigDecimal("7"));
        }

        @Test
        @DisplayName("String function (toUpper(\"a\") = \"A\") is folded")
        void stringConcatenationIsFolded() {
            // Strings must use DOUBLE QUOTES, and equality is '='
            CompiledExpression compiled = compileLogical("toUpper(\"a\") = \"A\"");

            ExecutableNode result = compiled.executionPlan().resultExpression();
            // The whole comparison (BinaryOp) should be folded to true
            assertThat(result).isInstanceOf(ExecutableLiteral.class);
            assertThat(((ExecutableLiteral) result).precomputed()).isEqualTo(true);
        }

        @Test
        @DisplayName("Logical comparison (10 > 5) is folded to true")
        void logicalComparisonIsFolded() {
            CompiledExpression compiled = compileLogical("10 > 5");

            ExecutableNode result = compiled.executionPlan().resultExpression();
            assertThat(result).isInstanceOf(ExecutableLiteral.class);
            assertThat(((ExecutableLiteral) result).precomputed()).isEqualTo(true);
        }

        @Test
        @DisplayName("Unary operator (-10) is folded to -10")
        void unaryOperatorIsFolded() {
            CompiledExpression compiled = compile("-10");

            ExecutableNode result = compiled.executionPlan().resultExpression();
            assertThat(result).isInstanceOf(ExecutableLiteral.class);
            assertThat(((ExecutableLiteral) result).precomputed()).isEqualTo(new BigDecimal("-10"));
        }

        @Test
        @DisplayName("Postfix operator (10%) is folded (divided by 100)")
        void postfixOperatorIsFolded() {
            CompiledExpression compiled = compile("10%");

            ExecutableNode result = compiled.executionPlan().resultExpression();
            assertThat(result).isInstanceOf(ExecutableLiteral.class);
            assertThat((BigDecimal) ((ExecutableLiteral) result).precomputed()).isEqualByComparingTo("0.1");
        }

        @Test
        @DisplayName("Ternary between (5 between 1 and 10) is folded to true")
        void ternaryBetweenIsFolded() {
            CompiledExpression compiled = compileLogical("5 between 1 and 10");

            ExecutableNode result = compiled.executionPlan().resultExpression();
            assertThat(result).isInstanceOf(ExecutableLiteral.class);
            assertThat(((ExecutableLiteral) result).precomputed()).isEqualTo(true);
        }

        @Test
        @DisplayName("Null coalescing (x ?? 2) is partially folded if x is known (not possible with pure literals on LHS in grammar)")
        void nullCoalescingFolding() {
            // Since literals on LHS of ?? are restricted by grammar/semantics, 
            // we test it via a context where it's allowed or simply skip if not applicable for pure folding.
            // But we can test: if x is a folded function call.
            CompiledExpression compiled = compile("ln(1) ?? 2");

            ExecutableNode result = compiled.executionPlan().resultExpression();
            assertThat(result).isInstanceOf(ExecutableLiteral.class);
            assertThat((BigDecimal) ((ExecutableLiteral) result).precomputed()).isEqualByComparingTo("0");
        }
    }

    @Nested
    @DisplayName("Vector folding")
    class VectorFolding {

        @Test
        @DisplayName("mean([1, 2, 3]) is folded")
        void allLiteralVectorIsFolded() {
            // Vectors are usually arguments to functions. We test folding of the vector within the function.
            CompiledExpression compiled = compile("mean([1, 2, 3])");

            ExecutableNode result = compiled.executionPlan().resultExpression();
            // Functions remain as ExecutableFunctionCall but marked as folded to preserve audit trail
            assertThat(result).isInstanceOf(ExecutableFunctionCall.class);
            ExecutableFunctionCall call = (ExecutableFunctionCall) result;
            assertThat(call.isFolded()).isTrue();
            assertThat((BigDecimal) call.foldedResult()).isEqualByComparingTo("2");
        }

        @Test
        @DisplayName("mean([1, x, 3]) is NOT folded")
        void vectorWithVariableIsNotFolded() {
            CompiledExpression compiled = compile("mean([1, x, 3])");

            ExecutableNode result = compiled.executionPlan().resultExpression();
            assertThat(result).isInstanceOf(ExecutableFunctionCall.class);
            ExecutableFunctionCall call = (ExecutableFunctionCall) result;
            assertThat(call.isFolded()).isFalse();
            
            ExecutableVectorLiteral vector = (ExecutableVectorLiteral) call.arguments().get(0);
            assertThat(vector.isFolded()).isFalse();
        }
    }

    // -----------------------------------------------------------------------
    // Non-foldable cases — isFolded() must remain false
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Function call that must NOT be folded")
    class NotFolded {

        @Test
        @DisplayName("ln(lb(x)) is not folded — inner lb receives a variable, so it is not folded and outer ln cannot fold either")
        void nestedFunctionWithVariableIsNotFolded() {
            CompiledExpression compiled = compile("ln(lb(x))");

            ExecutableFunctionCall outer = resultFunctionCall(compiled);

            assertThat(outer.isFolded()).isFalse();
            // The inner lb(x) is also not folded
            ExecutableFunctionCall inner = (ExecutableFunctionCall) outer.arguments().getFirst();
            assertThat(inner.isFolded()).isFalse();
        }

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
        @DisplayName("mean([1, 2, 3]) in logical context is folded")
        void meanWithVectorLiteralIsFolded() {
            CompiledExpression compiled = compileLogical("mean([1, 2, 3]) > 0");

            ExecutableNode result = compiled.executionPlan().resultExpression();
            assertThat(result).isInstanceOf(ExecutableLiteral.class);
            assertThat(((ExecutableLiteral) result).precomputed()).isEqualTo(true);
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
