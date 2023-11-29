package com.runestone.expeval.operation.values;

import com.runestone.expeval.expression.Expression;
import com.runestone.expeval.tools.VerifyExpressionsTools;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

public class TestAssignedVectors {

    @Test
    public void testWithSpreadFunction() {
        Expression expression = new Expression("""
                [a, b, c] := spread(10, 1, [1, 2, 3]);
                """);
        VerifyExpressionsTools.checkWarmUpCache(expression, 10);
        expression.evaluate();
        Assertions.assertThat(expression.getVariables()).isEmpty();
        Assertions.assertThat(expression.getAssignedVariables()).containsExactlyInAnyOrderEntriesOf(Map.of(
                "a", BigDecimal.valueOf(2),
                "b", BigDecimal.valueOf(3),
                "c", BigDecimal.valueOf(5))
        );
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 11);
        VerifyExpressionsTools.checkWarmUpCache(expression, 11);
        Assertions.assertThat(expression.toString()).isEqualTo("""
                [a, b, c] := spread(10, 1, [1, 2, 3]);""");
    }

    @Test
    public void testWithDistributeFunction() {
        Expression expression = new Expression("""
                [a, b, c, d] := distribute(20, 1, [0, 0, 0, 0], [3, 6, 9, 12]);
                """);
        VerifyExpressionsTools.checkWarmUpCache(expression, 17);
        expression.evaluate();
        Assertions.assertThat(expression.getVariables()).isEmpty();
        Assertions.assertThat(expression.getAssignedVariables()).containsExactlyInAnyOrderEntriesOf(Map.of(
                "a", BigDecimal.valueOf(3),
                "b", BigDecimal.valueOf(6),
                "c", BigDecimal.valueOf(9),
                "d", BigDecimal.valueOf(2))
        );
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 18);
        VerifyExpressionsTools.checkWarmUpCache(expression, 18);
        Assertions.assertThat(expression.toString()).isEqualTo("""
                [a, b, c, d] := distribute(20, 1, [0, 0, 0, 0], [3, 6, 9, 12]);""");
    }

    @Test
    public void testMultipleVectorAssignmentsAndSimpleAssignment() {
        Expression expression = new Expression("""
                [a, b, c, d] := distribute(20, 1, [0, 0, 0, 0], [3, 6, 9, 12]);
                [x, y, z] := spread(10, 1, [1, 2, 3]);
                abc := 21;
                a + b + c + d + x + y + z + abc
                """);
        VerifyExpressionsTools.checkWarmUpCache(expression, 37);
        Assertions.assertThat(expression.<BigDecimal>evaluate()).isEqualTo(BigDecimal.valueOf(51));
        Assertions.assertThat(expression.getVariables()).isEmpty();
        Assertions.assertThat(expression.getAssignedVariables()).containsExactlyInAnyOrderEntriesOf(Map.of(
                "a", BigDecimal.valueOf(3),
                "b", BigDecimal.valueOf(6),
                "c", BigDecimal.valueOf(9),
                "d", BigDecimal.valueOf(2),
                "x", BigDecimal.valueOf(2),
                "y", BigDecimal.valueOf(3),
                "z", BigDecimal.valueOf(5),
                "abc", BigDecimal.valueOf(21))
        );
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 37);
        VerifyExpressionsTools.checkWarmUpCache(expression, 37);
        Assertions.assertThat(expression.toString()).isEqualTo("""
                [a, b, c, d] := distribute(20, 1, [0, 0, 0, 0], [3, 6, 9, 12]);
                [x, y, z] := spread(10, 1, [1, 2, 3]);
                abc := 21;
                3 + 6 + 9 + 2 + 2 + 3 + 5 + 21""");
    }

    @Test
    public void testMultipleVectorAssignmentsAndInBetweenSimpleAssignment() {
        Expression expression = new Expression("""
                abc := 20;
                [a, b, c, d] := distribute(20, 1, [0, 0, 0, 0], [3, 6, 9, 12]);
                xyz := 1;
                [x, y, z] := spread(10, 1, [1, 2, 3]);
                a + b + c + d + x + y + z + abc + xyz
                """);
        VerifyExpressionsTools.checkWarmUpCache(expression, 40);
        Assertions.assertThat(expression.<BigDecimal>evaluate()).isEqualTo(BigDecimal.valueOf(51));
        Assertions.assertThat(expression.getVariables()).isEmpty();
        Assertions.assertThat(expression.getAssignedVariables()).containsExactlyInAnyOrderEntriesOf(Map.of(
                "a", BigDecimal.valueOf(3),
                "b", BigDecimal.valueOf(6),
                "c", BigDecimal.valueOf(9),
                "d", BigDecimal.valueOf(2),
                "x", BigDecimal.valueOf(2),
                "y", BigDecimal.valueOf(3),
                "z", BigDecimal.valueOf(5),
                "abc", BigDecimal.valueOf(20),
                "xyz", BigDecimal.valueOf(1))
        );
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 40);
        VerifyExpressionsTools.checkWarmUpCache(expression, 40);
        Assertions.assertThat(expression.toString()).isEqualTo("""
                abc := 20;
                [a, b, c, d] := distribute(20, 1, [0, 0, 0, 0], [3, 6, 9, 12]);
                xyz := 1;
                [x, y, z] := spread(10, 1, [1, 2, 3]);
                3 + 6 + 9 + 2 + 2 + 3 + 5 + 20 + 1""");
    }

}
