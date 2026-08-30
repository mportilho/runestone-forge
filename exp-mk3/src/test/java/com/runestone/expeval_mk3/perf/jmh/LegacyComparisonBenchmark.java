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
import java.util.List;
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
    private static final String MEMBERSHIP = "needle in [1, 2, 3, 4, 5, 6, 7]";
    private static final String COLLECTION_FILTER = "prices[?(@ > threshold)][0]";
    private static final String CONDITIONAL = "if enabled then gross * rate else fallback endif";
    private static final String COLLECTION_INDEX = "prices[2]";
    private static final String REGEX_MATCH = "text =~ \"^[A-Z]{3}-\\\\d{4}$\"";
    private static final String COMPARISON_CHAIN = "(a > b) xor (b = c) xor (c < d) xor (d <> a)";

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

    @Benchmark
    public boolean membershipLegacy(MembershipState state) {
        return state.legacy.compute(state.nextValues());
    }

    @Benchmark
    public boolean membershipMk3(MembershipState state) {
        return state.mk3.compute(state.nextValues());
    }

    @Benchmark
    public BigDecimal collectionFilterLegacy(CollectionFilterState state) {
        return state.legacy.compute(state.nextValues());
    }

    @Benchmark
    public BigDecimal collectionFilterMk3(CollectionFilterState state) {
        return state.mk3.compute(state.nextValues());
    }

    @Benchmark
    public BigDecimal conditionalLegacy(ConditionalState state) {
        return state.legacy.compute(state.nextValues());
    }

    @Benchmark
    public BigDecimal conditionalMk3(ConditionalState state) {
        return state.mk3.compute(state.nextValues());
    }

    @Benchmark
    public BigDecimal collectionIndexLegacy(CollectionIndexState state) {
        return state.legacy.compute(state.nextValues());
    }

    @Benchmark
    public BigDecimal collectionIndexMk3(CollectionIndexState state) {
        return state.mk3.compute(state.nextValues());
    }

    @Benchmark
    public boolean regexMatchLegacy(RegexState state) {
        return state.legacy.compute(state.nextValues());
    }

    @Benchmark
    public boolean regexMatchMk3(RegexState state) {
        return state.mk3.compute(state.nextValues());
    }

    @Benchmark
    public boolean comparisonChainLegacy(ComparisonState state) {
        return state.legacy.compute(state.nextValues());
    }

    @Benchmark
    public boolean comparisonChainMk3(ComparisonState state) {
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

    @State(Scope.Thread)
    public static class MembershipState {

        private com.runestone.expeval.api.LogicalExpression legacy;
        private com.runestone.expeval_mk3.api.LogicalExpression mk3;
        private Map<String, Object>[] values;
        private int index;

        @Setup(Level.Trial)
        public void setUp() {
            var legacyEnvironment = com.runestone.expeval.environment.ExpressionEnvironment.builder()
                    .registerExternalSymbol("needle", BigDecimal.ONE, true)
                    .build();
            var mk3Environment = ExpressionEnvironment.builder()
                    .externalSymbol("needle", BigDecimal.ONE, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                    .build();

            legacy = com.runestone.expeval.api.LogicalExpression.compile(MEMBERSHIP, legacyEnvironment);
            mk3 = ExpressionEngine.defaultEngine().compileOrThrow(MEMBERSHIP, mk3Environment).asLogical();
            values = alternatingValues("needle", BigDecimal.valueOf(7), BigDecimal.valueOf(99));
            requireEquivalent(legacy, mk3, values, MEMBERSHIP);
        }

        Map<String, Object> nextValues() {
            return values[index++ & (values.length - 1)];
        }
    }

    @State(Scope.Thread)
    public static class CollectionFilterState {

        private com.runestone.expeval.api.MathExpression legacy;
        private com.runestone.expeval_mk3.api.MathExpression mk3;
        private Map<String, Object>[] values;
        private int index;

        @Setup(Level.Trial)
        public void setUp() {
            List<BigDecimal> prices = List.of(
                    new BigDecimal("5.00"),
                    new BigDecimal("9.25"),
                    new BigDecimal("12.50"),
                    new BigDecimal("18.75"));
            var legacyEnvironment = com.runestone.expeval.environment.ExpressionEnvironment.builder()
                    .registerExternalSymbol("prices", prices, true)
                    .registerExternalSymbol("threshold", BigDecimal.TEN, true)
                    .build();
            var mk3Environment = ExpressionEnvironment.builder()
                    .externalSymbol("prices", prices, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                    .externalSymbol("threshold", BigDecimal.TEN, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                    .build();

            legacy = com.runestone.expeval.api.MathExpression.compile(COLLECTION_FILTER, legacyEnvironment);
            mk3 = ExpressionEngine.defaultEngine().compileOrThrow(COLLECTION_FILTER, mk3Environment).asMath();
            values = alternatingValues("threshold", BigDecimal.TEN, BigDecimal.valueOf(15));
            requireEquivalent(legacy, mk3, values, COLLECTION_FILTER);
        }

        Map<String, Object> nextValues() {
            return values[index++ & (values.length - 1)];
        }
    }

    @State(Scope.Thread)
    public static class ConditionalState {

        private com.runestone.expeval.api.MathExpression legacy;
        private com.runestone.expeval_mk3.api.MathExpression mk3;
        private Map<String, Object>[] values;
        private int index;

        @Setup(Level.Trial)
        public void setUp() {
            var legacyEnvironment = com.runestone.expeval.environment.ExpressionEnvironment.builder()
                    .registerExternalSymbol("enabled", true, true)
                    .registerExternalSymbol("gross", BigDecimal.valueOf(100), true)
                    .registerExternalSymbol("rate", new BigDecimal("0.125"), true)
                    .registerExternalSymbol("fallback", BigDecimal.valueOf(7), true)
                    .build();
            var mk3Environment = ExpressionEnvironment.builder()
                    .externalSymbol("enabled", true, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                    .externalSymbol("gross", BigDecimal.valueOf(100), ExternalSymbolOverwritePolicy.OVERRIDABLE)
                    .externalSymbol("rate", new BigDecimal("0.125"), ExternalSymbolOverwritePolicy.OVERRIDABLE)
                    .externalSymbol("fallback", BigDecimal.valueOf(7), ExternalSymbolOverwritePolicy.OVERRIDABLE)
                    .build();

            legacy = com.runestone.expeval.api.MathExpression.compile(CONDITIONAL, legacyEnvironment);
            mk3 = ExpressionEngine.defaultEngine().compileOrThrow(CONDITIONAL, mk3Environment).asMath();
            values = conditionalValues();
            requireEquivalent(legacy, mk3, values, CONDITIONAL);
        }

        Map<String, Object> nextValues() {
            return values[index++ & (values.length - 1)];
        }
    }

    @State(Scope.Thread)
    public static class CollectionIndexState {

        private com.runestone.expeval.api.MathExpression legacy;
        private com.runestone.expeval_mk3.api.MathExpression mk3;
        private Map<String, Object>[] values;
        private int index;

        @Setup(Level.Trial)
        public void setUp() {
            List<BigDecimal> firstPrices = decimalList("5.00", "9.25", "12.50", "18.75");
            List<BigDecimal> secondPrices = decimalList("2.00", "4.00", "6.25", "8.00");
            var legacyEnvironment = com.runestone.expeval.environment.ExpressionEnvironment.builder()
                    .registerExternalSymbol("prices", firstPrices, true)
                    .build();
            var mk3Environment = ExpressionEnvironment.builder()
                    .externalSymbol("prices", firstPrices, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                    .build();

            legacy = com.runestone.expeval.api.MathExpression.compile(COLLECTION_INDEX, legacyEnvironment);
            mk3 = ExpressionEngine.defaultEngine().compileOrThrow(COLLECTION_INDEX, mk3Environment).asMath();
            values = alternatingValues("prices", firstPrices, secondPrices);
            requireEquivalent(legacy, mk3, values, COLLECTION_INDEX);
        }

        Map<String, Object> nextValues() {
            return values[index++ & (values.length - 1)];
        }
    }

    @State(Scope.Thread)
    public static class RegexState {

        private com.runestone.expeval.api.LogicalExpression legacy;
        private com.runestone.expeval_mk3.api.LogicalExpression mk3;
        private Map<String, Object>[] values;
        private int index;

        @Setup(Level.Trial)
        public void setUp() {
            var legacyEnvironment = com.runestone.expeval.environment.ExpressionEnvironment.builder()
                    .registerExternalSymbol("text", "ABC-2048", true)
                    .build();
            var mk3Environment = ExpressionEnvironment.builder()
                    .externalSymbol("text", "ABC-2048", ExternalSymbolOverwritePolicy.OVERRIDABLE)
                    .build();

            legacy = com.runestone.expeval.api.LogicalExpression.compile(REGEX_MATCH, legacyEnvironment);
            mk3 = ExpressionEngine.defaultEngine().compileOrThrow(REGEX_MATCH, mk3Environment).asLogical();
            values = alternatingValues("text", "ABC-2048", "abc-2048");
            requireEquivalent(legacy, mk3, values, REGEX_MATCH);
        }

        Map<String, Object> nextValues() {
            return values[index++ & (values.length - 1)];
        }
    }

    @State(Scope.Thread)
    public static class ComparisonState {

        private com.runestone.expeval.api.LogicalExpression legacy;
        private com.runestone.expeval_mk3.api.LogicalExpression mk3;
        private Map<String, Object>[] values;
        private int index;

        @Setup(Level.Trial)
        public void setUp() {
            var legacyEnvironment = com.runestone.expeval.environment.ExpressionEnvironment.builder()
                    .registerExternalSymbol("a", BigDecimal.valueOf(8), true)
                    .registerExternalSymbol("b", BigDecimal.valueOf(3), true)
                    .registerExternalSymbol("c", BigDecimal.valueOf(3), true)
                    .registerExternalSymbol("d", BigDecimal.valueOf(9), true)
                    .build();
            var mk3Environment = ExpressionEnvironment.builder()
                    .externalSymbol("a", BigDecimal.valueOf(8), ExternalSymbolOverwritePolicy.OVERRIDABLE)
                    .externalSymbol("b", BigDecimal.valueOf(3), ExternalSymbolOverwritePolicy.OVERRIDABLE)
                    .externalSymbol("c", BigDecimal.valueOf(3), ExternalSymbolOverwritePolicy.OVERRIDABLE)
                    .externalSymbol("d", BigDecimal.valueOf(9), ExternalSymbolOverwritePolicy.OVERRIDABLE)
                    .build();

            legacy = com.runestone.expeval.api.LogicalExpression.compile(COMPARISON_CHAIN, legacyEnvironment);
            mk3 = ExpressionEngine.defaultEngine().compileOrThrow(COMPARISON_CHAIN, mk3Environment).asLogical();
            values = comparisonValues();
            requireEquivalent(legacy, mk3, values, COMPARISON_CHAIN);
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

    @SuppressWarnings("unchecked")
    private static Map<String, Object>[] alternatingValues(String name, Object first, Object second) {
        Map<String, Object>[] frames = (Map<String, Object>[]) new Map<?, ?>[256];
        for (int index = 0; index < frames.length; index++) {
            frames[index] = Map.of(name, (index & 1) == 0 ? first : second);
        }
        return frames;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object>[] conditionalValues() {
        Map<String, Object>[] frames = (Map<String, Object>[]) new Map<?, ?>[256];
        for (int index = 0; index < frames.length; index++) {
            frames[index] = Map.of(
                    "enabled", (index & 1) == 0,
                    "gross", BigDecimal.valueOf(100 + (index & 7)),
                    "rate", new BigDecimal("0.125"),
                    "fallback", BigDecimal.valueOf(7 + (index & 3)));
        }
        return frames;
    }

    private static List<BigDecimal> decimalList(String... values) {
        return java.util.Arrays.stream(values).map(BigDecimal::new).toList();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object>[] comparisonValues() {
        Map<String, Object>[] frames = (Map<String, Object>[]) new Map<?, ?>[256];
        for (int index = 0; index < frames.length; index++) {
            long offset = index & 7;
            frames[index] = Map.of(
                    "a", BigDecimal.valueOf(8 + offset),
                    "b", BigDecimal.valueOf(3 + offset),
                    "c", BigDecimal.valueOf(3 + offset),
                    "d", BigDecimal.valueOf(9 + offset));
        }
        return frames;
    }

    private static void requireEquivalent(
            com.runestone.expeval.api.LogicalExpression legacy,
            com.runestone.expeval_mk3.api.LogicalExpression mk3,
            Map<String, Object>[] values,
            String source) {
        for (Map<String, Object> frame : values) {
            if (legacy.compute(frame) != mk3.compute(frame)) {
                throw new IllegalStateException("Evaluator results differ for: " + source);
            }
        }
    }

    private static void requireEquivalent(
            com.runestone.expeval.api.MathExpression legacy,
            com.runestone.expeval_mk3.api.MathExpression mk3,
            Map<String, Object>[] values,
            String source) {
        for (Map<String, Object> frame : values) {
            requireEqual(legacy.compute(frame), mk3.compute(frame), source);
        }
    }

    private static void requireEqual(BigDecimal legacy, BigDecimal mk3, String source) {
        if (!legacy.equals(mk3)) {
            throw new IllegalStateException(
                    "Evaluator results differ for: " + source + " (legacy=" + legacy + ", mk3=" + mk3 + ')');
        }
    }
}
