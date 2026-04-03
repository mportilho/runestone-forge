package com.runestone.expeval2.internal.runtime;

import com.runestone.expeval2.environment.ExpressionEnvironment;
import com.runestone.expeval2.internal.grammar.ExpressionResultType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Conditional folding — ExecutionPlan structure")
class ConditionalConstantFoldingTest {

    private static final ExpressionEnvironment ENV =
            ExpressionEnvironment.builder().addMathFunctions().build();

    private final ExpressionCompiler compiler = new ExpressionCompiler();

    @Test
    @DisplayName("if true then 1 else x is folded to 1")
    void ifTrueThen1ElseXIsFoldedTo1() {
        CompiledExpression compiled = compile("if true then 1 else x endif");

        ExecutableNode result = compiled.executionPlan().resultExpression();
        assertThat(result).isInstanceOf(ExecutableLiteral.class);
        assertThat(((ExecutableLiteral) result).precomputed()).isEqualTo(new BigDecimal("1"));
    }

    @Test
    @DisplayName("if false then x else 2 is folded to 2")
    void ifFalseThenXElse2IsFoldedTo2() {
        CompiledExpression compiled = compile("if false then x else 2 endif");

        ExecutableNode result = compiled.executionPlan().resultExpression();
        assertThat(result).isInstanceOf(ExecutableLiteral.class);
        assertThat(((ExecutableLiteral) result).precomputed()).isEqualTo(new BigDecimal("2"));
    }

    @Test
    @DisplayName("if x > 0 then 1 elsif true then 2 else x is folded to if x > 0 then 1 else 2")
    void nestedIfWithConstantTruePrunesBranch() {
        CompiledExpression compiled = compile("if x > 0 then 1 elsif true then 2 else x endif");

        ExecutableNode result = compiled.executionPlan().resultExpression();
        assertThat(result).isInstanceOf(ExecutableSimpleConditional.class);
        
        ExecutableSimpleConditional sc = (ExecutableSimpleConditional) result;
        assertThat(sc.thenExpression()).isInstanceOf(ExecutableLiteral.class);
        assertThat(((ExecutableLiteral) sc.thenExpression()).precomputed()).isEqualTo(new BigDecimal("1"));
        assertThat(sc.elseExpression()).isInstanceOf(ExecutableLiteral.class);
        assertThat(((ExecutableLiteral) sc.elseExpression()).precomputed()).isEqualTo(new BigDecimal("2"));
    }

    @Test
    @DisplayName("if false then 1 elsif false then 2 else 3 is folded to 3")
    void allFalseIsFoldedToElse() {
        CompiledExpression compiled = compile("if false then 1 elsif false then 2 else 3 endif");

        ExecutableNode result = compiled.executionPlan().resultExpression();
        assertThat(result).isInstanceOf(ExecutableLiteral.class);
        assertThat(((ExecutableLiteral) result).precomputed()).isEqualTo(new BigDecimal("3"));
    }

    @Test
    @DisplayName("if x > 0 then 1 elsif false then 2 else 3 is folded to if x > 0 then 1 else 3")
    void internalFalseBranchIsPruned() {
        CompiledExpression compiled = compile("if x > 0 then 1 elsif false then 2 else 3 endif");

        ExecutableNode result = compiled.executionPlan().resultExpression();
        assertThat(result).isInstanceOf(ExecutableSimpleConditional.class);
        
        ExecutableSimpleConditional sc = (ExecutableSimpleConditional) result;
        assertThat(sc.elseExpression()).isInstanceOf(ExecutableLiteral.class);
        assertThat(((ExecutableLiteral) sc.elseExpression()).precomputed()).isEqualTo(new BigDecimal("3"));
    }

    @Test
    @DisplayName("if true then (if false then 1 else 2 endif) else 3 endif is folded to 2")
    void fullyNestedConstantIsFolded() {
        CompiledExpression compiled = compile("if true then (if false then 1 else 2 endif) else 3 endif");

        ExecutableNode result = compiled.executionPlan().resultExpression();
        assertThat(result).isInstanceOf(ExecutableLiteral.class);
        assertThat(((ExecutableLiteral) result).precomputed()).isEqualTo(new BigDecimal("2"));
    }

    private CompiledExpression compile(String source) {
        return compiler.compile(source, ExpressionResultType.MATH, ENV);
    }
}
