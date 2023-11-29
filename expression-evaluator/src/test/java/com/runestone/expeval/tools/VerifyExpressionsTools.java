package com.runestone.expeval.tools;

import com.runestone.expeval.expression.Expression;
import com.runestone.expeval.operation.AbstractOperation;
import org.assertj.core.api.Assertions;

import java.util.Set;

public class VerifyExpressionsTools {

    public static void commonVerifications(Expression expression) {
        CacheCheckVisitor cacheVisitor = new CacheCheckVisitor();
        Expression copy = expression.copy();

        int cacheCount = expression.visitOperations(cacheVisitor.reset());
        int copyCacheCount = copy.visitOperations(cacheVisitor.reset());

        Set<AbstractOperation> expressionCollection = expression.visitOperations(new OperationCollectorVisitor());
        Set<AbstractOperation> copyCollection = copy.visitOperations(new OperationCollectorVisitor());

        Assertions.assertThat(cacheCount).isEqualTo(copyCacheCount);
        Assertions.assertThat(expressionCollection)
                .doesNotContainAnyElementsOf(copyCollection)
                .hasSize(copyCollection.size());
    }

    public static void checkCache(Expression expression, int expectedCacheCount) {
        CacheCheckVisitor cacheVisitor = new CacheCheckVisitor();
        int cacheCount = expression.visitOperations(cacheVisitor.reset());
        Assertions.assertThat(cacheCount).isEqualTo(expectedCacheCount);
    }

    public static void checkWarmUpCache(Expression expression, int expectedCacheCount) {
        CacheCheckVisitor cacheVisitor = new CacheCheckVisitor();
        Expression copy = expression.copy().warmUp();
        int cacheCount = copy.visitOperations(cacheVisitor.reset());
        Assertions.assertThat(cacheCount).isEqualTo(expectedCacheCount);
    }

}
