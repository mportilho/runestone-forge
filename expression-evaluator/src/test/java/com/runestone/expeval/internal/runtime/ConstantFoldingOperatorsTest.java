package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.internal.execution.plan.*;

import com.runestone.expeval.environment.ExpressionEnvironment;
import com.runestone.expeval.testing.ExpressionCompilerInspector;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Constant folding for standard operators")
class ConstantFoldingOperatorsTest {

    private static final ExpressionEnvironment ENV =
            ExpressionEnvironment.builder().addMathFunctions().build();

    private final ExpressionCompilerInspector inspector = new ExpressionCompilerInspector();

    @Test
    @DisplayName("Binary operator with constant operands is folded into ExecutableLiteral")
    void binaryOpFolded() {
        CompiledExpression compiled = compile("1 + 2 * 3");
        ExecutableNode result = compiled.executionPlan().resultExpression();

        assertThat(result).isInstanceOf(ExecutableLiteral.class);
        assertThat(((ExecutableLiteral) result).precomputed()).isEqualTo(new BigDecimal("7"));
    }

    @Test
    @DisplayName("Unary operator with constant operand is folded into ExecutableLiteral")
    void unaryOpFolded() {
        CompiledExpression compiled = inspector.compileLogical("!true or !false", ENV);
        ExecutableNode result = compiled.executionPlan().resultExpression();

        assertThat(result).isInstanceOf(ExecutableLiteral.class);
        assertThat(((ExecutableLiteral) result).precomputed()).isEqualTo(true);
    }

    @Test
    @DisplayName("Ternary operator with constant operands is folded into ExecutableLiteral")
    void ternaryOpFolded() {
        CompiledExpression compiled = inspector.compileLogical("5 between 1 and 10", ENV);
        ExecutableNode result = compiled.executionPlan().resultExpression();

        assertThat(result).isInstanceOf(ExecutableLiteral.class);
        assertThat(((ExecutableLiteral) result).precomputed()).isEqualTo(true);
    }

    @Test
    @DisplayName("Postfix operator with constant operand is folded into ExecutableLiteral")
    void postfixOpFolded() {
        CompiledExpression compiled = compile("50%");
        ExecutableNode result = compiled.executionPlan().resultExpression();

        assertThat(result).isInstanceOf(ExecutableLiteral.class);
        assertThat(((ExecutableLiteral) result).precomputed()).isEqualTo(new BigDecimal("0.50"));
    }

    private CompiledExpression compile(String source) {
        return inspector.compileMath(source, ENV);
    }
}
