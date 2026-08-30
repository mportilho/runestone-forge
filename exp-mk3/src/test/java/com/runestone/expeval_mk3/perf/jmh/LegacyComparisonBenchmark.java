package com.runestone.expeval_mk3.perf.jmh;

import com.runestone.expeval.environment.ExpressionEnvironmentBuilder;
import com.runestone.expeval_mk3.api.ExpressionEngine;
import com.runestone.expeval_mk3.api.ExpressionEnvironment;
import com.runestone.expeval_mk3.api.ExternalSymbolOverwritePolicy;
import com.runestone.expeval_mk3.api.FunctionPurity;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Like-for-like steady-state comparison between MK3 and the previous evaluator. Compilation and
 * environment construction are deliberately outside the measured path.
 */
@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(value = 3, jvmArgsAppend = {"-Xms1g", "-Xmx1g"})
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class LegacyComparisonBenchmark {

    private static final String[] VARIABLE_NAMES = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l"};
    private static final String MULTIPLE_VARIABLES =
            "a * b + c * d - e + f * g - h + i * j + k - l + (a * c) + (b * d)";
    private static final String FUNCTION_CALLS =
            "weighted(a, b, c) + weighted(d, e, f) + weighted(g, h, i) + weighted(j, k, l)";
    private static final String OBJECT_NAVIGATION = "customer.address.district.code = \"D-100\"";

    @Benchmark
    public BigDecimal multipleVariablesLegacy(NumericState state) {
        return state.legacyVariables.compute(state.nextValues());
    }

    @Benchmark
    public BigDecimal multipleVariablesMk3(NumericState state) {
        return state.mk3Variables.compute(state.nextValues());
    }

    @Benchmark
    public BigDecimal multipleVariablesWithoutOverridesLegacy(NumericState state) {
        return state.legacyVariables.compute();
    }

    @Benchmark
    public BigDecimal multipleVariablesWithoutOverridesMk3(NumericState state) {
        return state.mk3Variables.compute();
    }

    @Benchmark
    public BigDecimal multipleVariablesWithDefaultOverridesLegacy(NumericState state) {
        return state.legacyVariables.compute(state.defaultOverrides);
    }

    @Benchmark
    public BigDecimal multipleVariablesWithDefaultOverridesMk3(NumericState state) {
        return state.mk3Variables.compute(state.defaultOverrides);
    }

    @Benchmark
    public BigDecimal functionCallsLegacy(NumericState state) {
        return state.legacyFunctions.compute(state.nextValues());
    }

    @Benchmark
    public BigDecimal functionCallsMk3(NumericState state) {
        return state.mk3Functions.compute(state.nextValues());
    }

    @Benchmark
    public BigDecimal functionCallsWithoutOverridesLegacy(NumericState state) {
        return state.legacyFunctions.compute();
    }

    @Benchmark
    public BigDecimal functionCallsWithoutOverridesMk3(NumericState state) {
        return state.mk3Functions.compute();
    }

    @Benchmark
    public BigDecimal functionCallsWithDefaultOverridesLegacy(NumericState state) {
        return state.legacyFunctions.compute(state.defaultOverrides);
    }

    @Benchmark
    public BigDecimal functionCallsWithDefaultOverridesMk3(NumericState state) {
        return state.mk3Functions.compute(state.defaultOverrides);
    }

    @Benchmark
    public boolean objectNavigationLegacy(NavigationState state) {
        return state.legacy.compute(state.nextValues());
    }

    @Benchmark
    public boolean objectNavigationMk3(NavigationState state) {
        return state.mk3.compute(state.nextValues());
    }

    @State(Scope.Thread)
    public static class NumericState {

        private com.runestone.expeval.api.MathExpression legacyVariables;
        private com.runestone.expeval_mk3.api.MathExpression mk3Variables;
        private com.runestone.expeval.api.MathExpression legacyFunctions;
        private com.runestone.expeval_mk3.api.MathExpression mk3Functions;
        private Map<String, Object> defaultOverrides;
        private Map<String, Object>[] values;
        private int index;

        @Setup(Level.Trial)
        public void setUp() {
            var legacyBuilder = com.runestone.expeval.environment.ExpressionEnvironment.builder();
            ExpressionEnvironment.Builder mk3Builder = ExpressionEnvironment.builder();
            for (String name : VARIABLE_NAMES) {
                legacyBuilder.registerExternalSymbol(name, BigDecimal.ONE, true);
                mk3Builder.externalSymbol(name, BigDecimal.ONE, ExternalSymbolOverwritePolicy.OVERRIDABLE);
            }

            var legacyVariablesEnvironment = legacyBuilder.build();
            var mk3VariablesEnvironment = mk3Builder.build();
            legacyVariables = com.runestone.expeval.api.MathExpression.compile(
                    MULTIPLE_VARIABLES, legacyVariablesEnvironment);
            mk3Variables = ExpressionEngine.defaultEngine()
                    .compileOrThrow(MULTIPLE_VARIABLES, mk3VariablesEnvironment)
                    .asMath();

            var legacyFunctionEnvironment = copyLegacySymbols()
                    .registerStaticProvider(FunctionProvider.class, true)
                    .build();
            var mk3FunctionEnvironment = copyMk3Symbols()
                    .functionsFrom(FunctionProvider.class, FunctionPurity.PURE)
                    .build();
            legacyFunctions = com.runestone.expeval.api.MathExpression.compile(
                    FUNCTION_CALLS, legacyFunctionEnvironment);
            mk3Functions = ExpressionEngine.defaultEngine()
                    .compileOrThrow(FUNCTION_CALLS, mk3FunctionEnvironment)
                    .asMath();

            defaultOverrides = defaultOverrides();
            values = numericValues();
            requireEqual(legacyVariables.compute(values[0]), mk3Variables.compute(values[0]), MULTIPLE_VARIABLES);
            requireEqual(legacyFunctions.compute(values[0]), mk3Functions.compute(values[0]), FUNCTION_CALLS);
        }

        Map<String, Object> nextValues() {
            return values[index++ & (values.length - 1)];
        }
    }

    @State(Scope.Thread)
    public static class NavigationState {

        private com.runestone.expeval.api.LogicalExpression legacy;
        private com.runestone.expeval_mk3.api.LogicalExpression mk3;
        private Map<String, Object>[] values;
        private int index;

        @Setup(Level.Trial)
        public void setUp() {
            Customer defaultCustomer = customer(0);
            var legacyEnvironment = com.runestone.expeval.environment.ExpressionEnvironment.builder()
                    .registerTypeHint(Customer.class)
                    .registerTypeHint(Address.class)
                    .registerTypeHint(District.class)
                    .registerExternalSymbol("customer", defaultCustomer, true)
                    .build();
            var mk3Environment = ExpressionEnvironment.builder()
                    .registerJavaType(Customer.class)
                    .registerJavaType(Address.class)
                    .registerJavaType(District.class)
                    .externalSymbol("customer", defaultCustomer, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                    .build();

            legacy = com.runestone.expeval.api.LogicalExpression.compile(OBJECT_NAVIGATION, legacyEnvironment);
            mk3 = ExpressionEngine.defaultEngine().compileOrThrow(OBJECT_NAVIGATION, mk3Environment).asLogical();
            values = navigationValues();
            if (legacy.compute(values[0]) != mk3.compute(values[0])) {
                throw new IllegalStateException("Evaluator results differ for: " + OBJECT_NAVIGATION);
            }
        }

        Map<String, Object> nextValues() {
            return values[index++ & (values.length - 1)];
        }
    }

    public static final class FunctionProvider {

        private static final BigDecimal HALF = new BigDecimal("0.5");
        private static final BigDecimal ONE_AND_HALF = new BigDecimal("1.5");
        private static final BigDecimal QUARTER = new BigDecimal("0.25");

        private FunctionProvider() {
        }

        public static BigDecimal weighted(BigDecimal left, BigDecimal middle, BigDecimal right) {
            return left.multiply(HALF).add(middle.multiply(ONE_AND_HALF)).subtract(right.multiply(QUARTER));
        }
    }

    public record Customer(String name, Address address) {
    }

    public record Address(String city, District district) {
    }

    public record District(String code) {
    }

    private static ExpressionEnvironmentBuilder copyLegacySymbols() {
        ExpressionEnvironmentBuilder builder = com.runestone.expeval.environment.ExpressionEnvironment.builder();
        for (String name : VARIABLE_NAMES) {
            builder.registerExternalSymbol(name, BigDecimal.ONE, true);
        }
        return builder;
    }

    private static ExpressionEnvironment.Builder copyMk3Symbols() {
        ExpressionEnvironment.Builder builder = ExpressionEnvironment.builder();
        for (String name : VARIABLE_NAMES) {
            builder.externalSymbol(name, BigDecimal.ONE, ExternalSymbolOverwritePolicy.OVERRIDABLE);
        }
        return builder;
    }

    private static Map<String, Object> defaultOverrides() {
        Map<String, Object> values = new HashMap<>();
        for (String name : VARIABLE_NAMES) {
            values.put(name, BigDecimal.ONE);
        }
        return Map.copyOf(values);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object>[] numericValues() {
        Map<String, Object>[] frames = (Map<String, Object>[]) new Map<?, ?>[256];
        for (int frame = 0; frame < frames.length; frame++) {
            Map<String, Object> values = new HashMap<>();
            for (int variable = 0; variable < VARIABLE_NAMES.length; variable++) {
                values.put(VARIABLE_NAMES[variable], BigDecimal.valueOf(frame + variable + 1L, 1));
            }
            frames[frame] = Map.copyOf(values);
        }
        return frames;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object>[] navigationValues() {
        Map<String, Object>[] frames = (Map<String, Object>[]) new Map<?, ?>[256];
        for (int index = 0; index < frames.length; index++) {
            frames[index] = Map.of("customer", customer(index));
        }
        return frames;
    }

    private static Customer customer(int index) {
        String code = (index & 1) == 0 ? "D-100" : "D-200";
        return new Customer("Customer-" + index, new Address("City-" + (index & 15), new District(code)));
    }

    private static void requireEqual(BigDecimal legacy, BigDecimal mk3, String source) {
        if (legacy.compareTo(mk3) != 0) {
            throw new IllegalStateException("Evaluator results differ for: " + source);
        }
    }
}
