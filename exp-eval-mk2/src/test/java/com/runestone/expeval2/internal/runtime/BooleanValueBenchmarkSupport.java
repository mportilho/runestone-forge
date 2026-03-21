package com.runestone.expeval2.internal.runtime;

import com.runestone.expeval2.environment.ExpressionEnvironment;
import com.runestone.expeval2.environment.ExpressionEnvironmentBuilder;
import com.runestone.expeval2.internal.grammar.ExpressionResultType;

import java.math.BigDecimal;

/**
 * Supports {@code BooleanValueBenchmark}, measuring allocation cost from evaluating
 * logical expressions that generate multiple boolean intermediate results.
 *
 * <p><b>Smell under investigation:</b> {@link RuntimeValue.BooleanValue} has no static singleton
 * constants for {@code true} and {@code false}. Every comparison ({@code a > b}) and every boolean
 * binary operator ({@code AND}, {@code OR}) in {@link AbstractRuntimeEvaluator} creates a new
 * {@code BooleanValue} record — 16 B each. For a five-comparison AND chain this is up to
 * 9 allocations × 16 B = 144 B per {@code compute()} call that could be eliminated with two
 * static constants.
 *
 * <p><b>Fix:</b> Add {@code BooleanValue.TRUE} and {@code BooleanValue.FALSE} static instances to
 * {@link RuntimeValue.BooleanValue} and replace every {@code new RuntimeValue.BooleanValue(b)}
 * in {@link AbstractRuntimeEvaluator} with the appropriate constant.
 */
public final class BooleanValueBenchmarkSupport {

    // 5 comparisons, 4 AND operations → up to 9 new BooleanValue(16 B) = 144 B/op
    private static final String BOOL_CHAIN_EXPRESSION =
        "a > b and c > d and e > f and g > h and i > j";

    // 10 comparisons, 9 AND operations → up to 19 new BooleanValue = 304 B/op
    private static final String BOOL_WIDE_EXPRESSION =
        "a > b and b > c and c > d and d > e and e > f "
        + "and f > g and g > h and h > i and i > j and j > k";

    private static final String[] CHAIN_VARS = {"a","b","c","d","e","f","g","h","i","j"};
    private static final String[] WIDE_VARS  = {"a","b","c","d","e","f","g","h","i","j","k"};

    // Precomputed BigDecimal values so the benchmark itself allocates nothing extra
    private static final BigDecimal HI = new BigDecimal("10");
    private static final BigDecimal LO = BigDecimal.ONE;
    private static final BigDecimal[] WIDE_VALS = {
        new BigDecimal("10"), new BigDecimal("9"), new BigDecimal("8"), new BigDecimal("7"),
        new BigDecimal("6"),  new BigDecimal("5"), new BigDecimal("4"), new BigDecimal("3"),
        new BigDecimal("2"),  BigDecimal.ONE,       BigDecimal.ZERO
    };

    private final ExpressionRuntimeSupport chainRuntime;
    private final ExpressionRuntimeSupport wideRuntime;

    public BooleanValueBenchmarkSupport() {
        ExpressionEnvironmentBuilder builder = new ExpressionEnvironmentBuilder();
        for (String name : WIDE_VARS) {
            builder.registerExternalSymbol(name, BigDecimal.ONE, true);
        }
        ExpressionEnvironment env = builder.build();
        ExpressionCompiler compiler = new ExpressionCompiler();
        chainRuntime = ExpressionRuntimeSupport.compile(BOOL_CHAIN_EXPRESSION, ExpressionResultType.LOGICAL, env, compiler);
        wideRuntime  = ExpressionRuntimeSupport.compile(BOOL_WIDE_EXPRESSION,  ExpressionResultType.LOGICAL, env, compiler);
    }

    /**
     * Evaluates the 5-comparison AND chain. All comparisons pass (hi > lo),
     * so no short-circuit exits and all 9 BooleanValue allocations occur.
     */
    public boolean evaluateBoolChain() {
        for (int i = 0; i < CHAIN_VARS.length; i++) {
            chainRuntime.setValue(CHAIN_VARS[i], (i % 2 == 0) ? HI : LO);
        }
        return chainRuntime.computeLogical();
    }

    /**
     * Evaluates the 10-comparison wide AND chain. Values are strictly decreasing so
     * all comparisons pass; no short-circuit exits and all 19 BooleanValue allocations occur.
     */
    public boolean evaluateBoolWide() {
        for (int i = 0; i < WIDE_VARS.length; i++) {
            wideRuntime.setValue(WIDE_VARS[i], WIDE_VALS[i]);
        }
        return wideRuntime.computeLogical();
    }
}
