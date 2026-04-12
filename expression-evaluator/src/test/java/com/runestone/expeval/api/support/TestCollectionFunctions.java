package com.runestone.expeval.api.support;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;

/**
 * Public top-level provider registered via {@link com.runestone.expeval.environment.ExpressionEnvironmentBuilder#registerStaticProvider}.
 * Must be a public class so that {@link java.lang.invoke.MethodHandles#lookup()} from inside the
 * library can create method handles for each function.
 */
public final class TestCollectionFunctions {

    private TestCollectionFunctions() {}

    /**
     * {@code ..distinctCount()} — number of unique elements in a list.
     */
    public static BigDecimal distinctCount(List<BigDecimal> list) {
        return BigDecimal.valueOf(list.stream().distinct().count());
    }

    /**
     * {@code ..countAbove(threshold)} — count of elements strictly greater than threshold.
     */
    public static BigDecimal countAbove(List<BigDecimal> list, BigDecimal threshold) {
        return BigDecimal.valueOf(
                list.stream().filter(v -> v.compareTo(threshold) > 0).count());
    }

    /**
     * {@code ..top(n)} — the {@code n} largest elements as a new list.
     * Return type {@code List} maps to {@code VectorType}, so the result can be
     * further chained with {@code ..sum()}, {@code ..count()}, {@code [?(...)]}, etc.
     */
    public static List<BigDecimal> top(List<BigDecimal> list, BigDecimal n) {
        return list.stream()
                .sorted(java.util.Comparator.<BigDecimal>naturalOrder().reversed())
                .limit(n.longValue())
                .toList();
    }

    /**
     * {@code ..normalizedSum(divisor)} — sums the list and divides by {@code divisor}.
     * The leading {@link MathContext} parameter is automatically bound to the
     * environment's math context, so callers never supply it in the expression.
     */
    public static BigDecimal normalizedSum(MathContext ctx, List<BigDecimal> list, BigDecimal divisor) {
        BigDecimal sum = list.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(divisor, ctx);
    }
}
