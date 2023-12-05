/*
 * MIT License
 * <p>
 * Copyright (c) 2023-2023 Marcelo Silva Portilho
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
