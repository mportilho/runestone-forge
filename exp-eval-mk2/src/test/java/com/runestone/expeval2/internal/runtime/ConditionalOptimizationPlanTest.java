package com.runestone.expeval2.internal.runtime;

import com.runestone.expeval2.environment.ExpressionEnvironment;
import com.runestone.expeval2.internal.grammar.ExpressionResultType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Conditional optimization — ExecutionPlan specialization")
class ConditionalOptimizationPlanTest {

    private static final ExpressionEnvironment ENV =
            ExpressionEnvironment.builder().addMathFunctions().build();

    private final ExpressionCompiler compiler = new ExpressionCompiler();

    @Test
    @DisplayName("if-then-else with 1 condition uses ExecutableSimpleConditional")
    void simpleConditionalUsesSpecializedNode() {
        CompiledExpression compiled = compile("if 1 > 0 then 1 else 0 endif");

        ExecutableNode result = compiled.executionPlan().resultExpression();
        assertThat(result).isInstanceOf(ExecutableSimpleConditional.class);
        
        ExecutableSimpleConditional sc = (ExecutableSimpleConditional) result;
        assertThat(sc.condition()).isInstanceOf(ExecutableBinaryOp.class);
        assertThat(sc.thenExpression()).isInstanceOf(ExecutableLiteral.class);
        assertThat(sc.elseExpression()).isInstanceOf(ExecutableLiteral.class);
    }

    @Test
    @DisplayName("if-then-elseif-then-else with 2 conditions uses ExecutableConditional")
    void multipleConditionsUseGeneralNode() {
        CompiledExpression compiled = compile("if 1 > 0 then 1 elsif 2 > 0 then 2 else 0 endif");

        ExecutableNode result = compiled.executionPlan().resultExpression();
        assertThat(result).isInstanceOf(ExecutableConditional.class);
        
        ExecutableConditional c = (ExecutableConditional) result;
        assertThat(c.conditions()).hasSize(2);
        assertThat(c.results()).hasSize(2);
    }

    @Test
    @DisplayName("Evaluate simple conditional correctly - then branch")
    void evaluateSimpleConditionalThenBranch() {
        ExpressionRuntimeSupport runtime = ExpressionRuntimeSupport.compileMath("if x > 0 then 1 else 0 endif", ENV);
        
        BigDecimal result = runtime.computeMath(Map.of("x", new BigDecimal("10")));
        assertThat(result).isEqualByComparingTo("1");
    }

    @Test
    @DisplayName("Evaluate simple conditional correctly - else branch")
    void evaluateSimpleConditionalElseBranch() {
        ExpressionRuntimeSupport runtime = ExpressionRuntimeSupport.compileMath("if x > 0 then 1 else 0 endif", ENV);
        
        BigDecimal result = runtime.computeMath(Map.of("x", new BigDecimal("-10")));
        assertThat(result).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("maxAuditEvents uses MAX of branches for conditionals [CO-1]")
    void maxAuditEventsUsesMaxOfBranches() {
        // if cond then (a+b) else (c) endif
        // cond: 1 event (if variable) or 0 (if literal comparison like 1 > 0)
        // In "if x > 0 then a + b else c endif":
        // x > 0: 1 event (read x)
        // a + b: 2 events (read a, read b)
        // c: 1 event (read c)
        // Total expected: 1 (cond) + max(2, 1) = 3
        
        CompiledExpression compiled = compile("if x > 0 then a + b else c endif");
        assertThat(compiled.executionPlan().maxAuditEvents()).isEqualTo(3);
    }

    private CompiledExpression compile(String source) {
        return compiler.compile(source, ExpressionResultType.MATH, ENV);
    }
}
