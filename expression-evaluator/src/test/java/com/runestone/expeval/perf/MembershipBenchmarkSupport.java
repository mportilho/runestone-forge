package com.runestone.expeval.perf;

import com.runestone.expeval.api.LogicalExpression;
import com.runestone.expeval.environment.ExpressionEnvironment;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Pre-compiled expressions and fixed bindings for the membership operator benchmark.
 *
 * <p>All expressions are compiled once at construction time (no per-call compile cost).
 * Bindings are static maps to keep allocation out of the measured loop.
 *
 * <p>Scenarios:
 * <ul>
 *   <li><b>numericHitFirst</b>  — BigDecimal subject matches first element of a 5-element vector</li>
 *   <li><b>numericHitLast</b>   — BigDecimal subject matches last element of a 5-element vector</li>
 *   <li><b>numericMiss</b>      — BigDecimal subject not present in a 5-element vector (full scan)</li>
 *   <li><b>numericLargeHit</b>  — BigDecimal subject matches last element of a 50-element vector</li>
 *   <li><b>numericLargeMiss</b> — BigDecimal subject not present in a 50-element vector (full scan)</li>
 *   <li><b>stringMiss</b>       — String subject not present in a 5-element string vector</li>
 *   <li><b>externalVectorHit</b>— subject variable checked against an externally-supplied List</li>
 *   <li><b>notInMiss</b>        — NOT_IN with BigDecimal subject absent from vector (full scan)</li>
 * </ul>
 */
public final class MembershipBenchmarkSupport {

    // -------------------------------------------------------------------------
    // Small numeric literal vector (5 elements) — hit and miss
    // -------------------------------------------------------------------------

    private static final LogicalExpression NUMERIC_HIT_FIRST =
            LogicalExpression.compile("1 in [1, 2, 3, 4, 5]");

    private static final LogicalExpression NUMERIC_HIT_LAST =
            LogicalExpression.compile("5 in [1, 2, 3, 4, 5]");

    private static final LogicalExpression NUMERIC_MISS =
            LogicalExpression.compile("9 in [1, 2, 3, 4, 5]");

    // -------------------------------------------------------------------------
    // Large numeric literal vector (50 elements) — folded hit/miss
    // -------------------------------------------------------------------------

    private static final LogicalExpression NUMERIC_LARGE_HIT;
    private static final LogicalExpression NUMERIC_LARGE_MISS;

    static {
        String elements = IntStream.rangeClosed(1, 50)
                .mapToObj(Integer::toString)
                .collect(Collectors.joining(", ", "[", "]"));
        NUMERIC_LARGE_HIT  = LogicalExpression.compile("50 in " + elements);
        NUMERIC_LARGE_MISS = LogicalExpression.compile("99 in " + elements);
    }

    // -------------------------------------------------------------------------
    // String literal vector (5 elements)
    // -------------------------------------------------------------------------

    private static final LogicalExpression STRING_MISS =
            LogicalExpression.compile("\"z\" in [\"a\", \"b\", \"c\", \"d\", \"e\"]");

    // -------------------------------------------------------------------------
    // External (dynamic) vector via symbol
    // -------------------------------------------------------------------------

    private static final ExpressionEnvironment EXTERNAL_ENV = ExpressionEnvironment.builder()
            .registerExternalSymbol("valor", BigDecimal.ZERO, true)
            .registerExternalSymbol("lista", List.of(), true)
            .build();

    private static final LogicalExpression EXTERNAL_VECTOR_HIT =
            LogicalExpression.compile("valor in <vector>lista", EXTERNAL_ENV);

    private static final Map<String, Object> EXTERNAL_HIT_BINDINGS = Map.of(
            "valor", BigDecimal.valueOf(50),
            "lista", IntStream.rangeClosed(1, 50)
                    .mapToObj(BigDecimal::valueOf)
                    .collect(Collectors.toUnmodifiableList())
    );

    // -------------------------------------------------------------------------
    // NOT_IN
    // -------------------------------------------------------------------------

    private static final LogicalExpression NOT_IN_MISS =
            LogicalExpression.compile("9 not in [1, 2, 3, 4, 5]");

    // -------------------------------------------------------------------------
    // Public evaluation methods (called from JMH benchmark methods)
    // -------------------------------------------------------------------------

    public boolean evaluateNumericHitFirst() {
        return NUMERIC_HIT_FIRST.compute();
    }

    public boolean evaluateNumericHitLast() {
        return NUMERIC_HIT_LAST.compute();
    }

    public boolean evaluateNumericMiss() {
        return NUMERIC_MISS.compute();
    }

    public boolean evaluateNumericLargeHit() {
        return NUMERIC_LARGE_HIT.compute();
    }

    public boolean evaluateNumericLargeMiss() {
        return NUMERIC_LARGE_MISS.compute();
    }

    public boolean evaluateStringMiss() {
        return STRING_MISS.compute();
    }

    public boolean evaluateExternalVectorHit() {
        return EXTERNAL_VECTOR_HIT.compute(EXTERNAL_HIT_BINDINGS);
    }

    public boolean evaluateNotInMiss() {
        return NOT_IN_MISS.compute();
    }
}
