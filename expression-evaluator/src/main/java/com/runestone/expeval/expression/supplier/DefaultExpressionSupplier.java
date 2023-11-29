package com.runestone.expeval.expression.supplier;

import com.runestone.expeval.expression.Expression;
import com.runestone.expeval.expression.ExpressionContext;
import com.runestone.expeval.expression.ExpressionOptions;
import com.runestone.memoization.MemoizedExpiringFunction;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * A default implementation of {@link ExpressionSupplier} that caches newly created expressions for 10 minutes by default
 *
 * @author Marcelo Portilho
 */
public class DefaultExpressionSupplier implements ExpressionSupplier {

    private final ExpressionOptions options;
    private final ExpressionContext expressionContext;
    private final MemoizedExpiringFunction<String, Expression> cache;

    /**
     * Creates a new instance of {@link DefaultExpressionSupplier} with the expression options and context that will be used to
     * create new expressions
     *
     * @param options           the expression options
     * @param expressionContext the expression context
     */
    public DefaultExpressionSupplier(ExpressionOptions options, ExpressionContext expressionContext) {
        this.options = Objects.requireNonNull(options, "Expression options cannot be null");
        this.expressionContext = Objects.requireNonNull(expressionContext, "Expression context cannot be null");
        cache = new MemoizedExpiringFunction<>(this::generateNewExpression, 10, TimeUnit.MINUTES);
    }

    @Override
    public Expression createExpression(String expression) {
        return cache.apply(expression).copy();
    }

    private Expression generateNewExpression(String expression) {
        return new Expression(expression, options, expressionContext).warmUp();
    }

}
