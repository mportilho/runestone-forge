package com.runestone.expeval.operation;

import com.runestone.expeval.expression.Expression;
import com.runestone.expeval.tools.CacheCheckVisitor;
import com.runestone.expeval.tools.OperationCollectorVisitor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class TestExpressionCopy {

    private static CacheCheckVisitor cacheVisitor;

    @BeforeAll
    public static void beforeClass() {
        cacheVisitor = new CacheCheckVisitor();
    }

    @Test
    public void testSimpleCopy() {
        Expression original = new Expression("(c) * (a + b)");

        Expression copiedExpression = original.copy();
        assertThat(original.visitOperations(cacheVisitor.reset())).isEqualByComparingTo(0);
        assertThat(copiedExpression.visitOperations(cacheVisitor.reset())).isEqualByComparingTo(0);

        original.setVariable("a", 1);
        original.setVariable("b", 2);
        original.setVariable("c", 3);
        assertThat(original.visitOperations(cacheVisitor.reset())).isEqualByComparingTo(0);

        copiedExpression.setVariable("a", 1);
        copiedExpression.setVariable("b", 2);
        copiedExpression.setVariable("c", 3);
        assertThat(copiedExpression.visitOperations(cacheVisitor.reset())).isEqualByComparingTo(0);

        original.evaluate();
        assertThat(original.visitOperations(cacheVisitor.reset())).isEqualByComparingTo(6);
        assertThat(copiedExpression.visitOperations(cacheVisitor.reset())).isEqualByComparingTo(0);

        copiedExpression.evaluate();
        assertThat(original.visitOperations(cacheVisitor.reset())).isEqualByComparingTo(6);
        assertThat(copiedExpression.visitOperations(cacheVisitor.reset())).isEqualByComparingTo(6);

        Set<AbstractOperation> originalVisited = original.visitOperations(new OperationCollectorVisitor());
        Set<AbstractOperation> copiedVisited = copiedExpression.visitOperations(new OperationCollectorVisitor());
        assertThat(originalVisited).doesNotContainAnyElementsOf(copiedVisited);
    }

    @Test
    public void testCacheWithAndBooleanExpression() {
        Expression original = new Expression("if (0 != 0 and a > b and 1 = 1) then 1 else 0 endif");
        original.setVariable("a", 1);
        original.setVariable("b", 2);

        Expression copiedExpression = original.copy();
        assertThat(original.visitOperations(cacheVisitor.reset())).isEqualByComparingTo(0);
        assertThat(copiedExpression.visitOperations(cacheVisitor.reset())).isEqualTo(0);

        original.evaluate();
        assertThat(original.visitOperations(cacheVisitor.reset())).isEqualByComparingTo(8);
        assertThat(copiedExpression.visitOperations(cacheVisitor.reset())).isEqualTo(0);

        copiedExpression = original.copy();
        assertThat(original.visitOperations(cacheVisitor.reset())).isEqualByComparingTo(8);
        assertThat(copiedExpression.visitOperations(cacheVisitor.reset())).isEqualTo(8);

        Set<AbstractOperation> originalVisited = original.visitOperations(new OperationCollectorVisitor());
        Set<AbstractOperation> copiedVisited = copiedExpression.visitOperations(new OperationCollectorVisitor());
        assertThat(originalVisited).doesNotContainAnyElementsOf(copiedVisited);
    }

}
