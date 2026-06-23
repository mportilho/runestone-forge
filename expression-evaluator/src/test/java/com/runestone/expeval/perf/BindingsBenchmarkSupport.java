package com.runestone.expeval.perf;

import com.runestone.expeval.environment.ExpressionEnvironment;

import java.math.BigDecimal;
import java.util.Map;

public final class BindingsBenchmarkSupport {

    public static final String MANY_DEFAULTS_EXPRESSION =
            "a + b + c + d + e + f + g + h + i + j + k + l + m + n + o + p";
    public static final String SINGLE_OVERRIDE_EXPRESSION = "a + 1";
    public static final String DOUBLE_OVERRIDE_EXPRESSION = "a + b";

    private static final ExpressionEnvironment MANY_DEFAULTS_ENVIRONMENT = buildManyDefaultsEnvironment();
    private static final ExpressionEnvironment NO_DEFAULTS_ENVIRONMENT = ExpressionEnvironment.builder().build();

    private static final Map<String, Object> NO_OVERRIDES = Map.of();
    private static final Map<String, Object> ONE_OVERRIDE = Map.of("a", new BigDecimal("100"));
    private static final Map<String, Object> TWO_OVERRIDES = Map.of(
            "a", new BigDecimal("100"),
            "h", new BigDecimal("200")
    );
    private static final Map<String, Object> ONE_OVERRIDE_NO_DEFAULTS = Map.of("a", new BigDecimal("100"));
    private static final Map<String, Object> TWO_OVERRIDES_NO_DEFAULTS = Map.of(
            "a", new BigDecimal("100"),
            "b", new BigDecimal("200")
    );

    private BindingsBenchmarkSupport() {
    }

    public static ExpressionEnvironment manyDefaultsEnvironment() {
        return MANY_DEFAULTS_ENVIRONMENT;
    }

    public static ExpressionEnvironment noDefaultsEnvironment() {
        return NO_DEFAULTS_ENVIRONMENT;
    }

    public static Map<String, Object> noOverrides() {
        return NO_OVERRIDES;
    }

    public static Map<String, Object> oneOverride() {
        return ONE_OVERRIDE;
    }

    public static Map<String, Object> twoOverrides() {
        return TWO_OVERRIDES;
    }

    public static Map<String, Object> oneOverrideNoDefaults() {
        return ONE_OVERRIDE_NO_DEFAULTS;
    }

    public static Map<String, Object> twoOverridesNoDefaults() {
        return TWO_OVERRIDES_NO_DEFAULTS;
    }

    private static ExpressionEnvironment buildManyDefaultsEnvironment() {
        return ExpressionEnvironment.builder()
                .registerExternalSymbol("a", new BigDecimal("1"), true)
                .registerExternalSymbol("b", new BigDecimal("2"), true)
                .registerExternalSymbol("c", new BigDecimal("3"), true)
                .registerExternalSymbol("d", new BigDecimal("4"), true)
                .registerExternalSymbol("e", new BigDecimal("5"), true)
                .registerExternalSymbol("f", new BigDecimal("6"), true)
                .registerExternalSymbol("g", new BigDecimal("7"), true)
                .registerExternalSymbol("h", new BigDecimal("8"), true)
                .registerExternalSymbol("i", new BigDecimal("9"), true)
                .registerExternalSymbol("j", new BigDecimal("10"), true)
                .registerExternalSymbol("k", new BigDecimal("11"), true)
                .registerExternalSymbol("l", new BigDecimal("12"), true)
                .registerExternalSymbol("m", new BigDecimal("13"), true)
                .registerExternalSymbol("n", new BigDecimal("14"), true)
                .registerExternalSymbol("o", new BigDecimal("15"), true)
                .registerExternalSymbol("p", new BigDecimal("16"), true)
                .build();
    }
}
