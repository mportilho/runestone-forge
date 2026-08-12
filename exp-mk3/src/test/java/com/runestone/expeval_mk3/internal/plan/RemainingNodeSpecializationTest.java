package com.runestone.expeval_mk3.internal.plan;

import com.runestone.expeval_mk3.api.ExpressionEnvironment;
import com.runestone.expeval_mk3.api.ExternalSymbolOverwritePolicy;
import com.runestone.expeval_mk3.api.FunctionPurity;
import com.runestone.expeval_mk3.api.ScalarType;
import com.runestone.expeval_mk3.internal.runtime.AddDecimalExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.BetweenExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.BinaryExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.BinaryNullCoalesceExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.ConditionalExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.ModuloDecimalExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.MultiplyDecimalExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.NullCoalesceExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.SubtractDecimalExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.TwoBranchConditionalExecutableNode;
import com.runestone.expeval_mk3.internal.semantics.SemanticModel;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RemainingNodeSpecializationTest {

    @Test
    void binaryAndNaryCoalescenceAgreeWithTheOracle() {
        EffectProbe probe = new EffectProbe();
        ExpressionEnvironment environment = effectEnvironment(probe);

        assertEquivalent("map(1)?.[\"missing\"] ?? track(2)", environment, probe);
        assertThat(probe.snapshot()).containsExactly(1, 2);
        assertOptimizedNode("map(1)?.[\"missing\"] ?? track(2)", environment, BinaryNullCoalesceExecutableNode.class);

        assertEquivalent("map(1)?.[\"missing\"] ?? map(2)?.[\"missing\"] ?? track(3)", environment, probe);
        assertThat(probe.snapshot()).containsExactly(1, 2, 3);
        assertOptimizedNode(
                "map(1)?.[\"missing\"] ?? map(2)?.[\"missing\"] ?? track(3)",
                environment, NullCoalesceExecutableNode.class);
    }

    @Test
    void numberAndComparableBetweenAgreeWithTheOracleAndPreserveShortCircuiting() {
        EffectProbe probe = new EffectProbe();
        ExpressionEnvironment environment = effectEnvironment(probe);

        assertEquivalent("number(0, 1) between number(1, 2) and number(10, 3)", environment, probe);
        assertThat(probe.snapshot()).containsExactly(1, 2);
        assertOptimizedNode(
                "number(0, 1) between number(1, 2) and number(10, 3)", environment, BetweenExecutableNode.class);

        ExpressionEnvironment stringEnvironment = ExpressionEnvironment.builder()
                .externalSymbol("value", ScalarType.STRING, "m", ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .externalSymbol("lower", ScalarType.STRING, "a", ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .externalSymbol("upper", ScalarType.STRING, "z", ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
        assertEquivalent("value between lower and upper", stringEnvironment, null);
        assertOptimizedNode("value between lower and upper", stringEnvironment, BetweenExecutableNode.class);
    }

    @Test
    void naryAndBinaryConcatenationAgreeWithTheOracle() {
        ExpressionEnvironment environment = stringEnvironment();

        assertEquivalent("a || b || c || d", environment, null);
        assertEquivalent("a || b", environment, null);
        assertOptimizedNode("a || b || c || d", environment, BinaryExecutableNode.class);
        assertOptimizedNode("a || b", environment, BinaryExecutableNode.class);
    }

    @Test
    void smallAndLargerConditionalsAgreeWithTheOracleAndPreserveEffects() {
        EffectProbe probe = new EffectProbe();
        ExpressionEnvironment environment = effectEnvironment(probe);

        assertEquivalent("if truth(1, false) then track(10) else track(20) endif", environment, probe);
        assertThat(probe.snapshot()).containsExactly(1, 20);
        assertOptimizedNode(
                "if truth(1, false) then track(10) else track(20) endif",
                environment, ConditionalExecutableNode.class);

        assertEquivalent(
                "if truth(1, false) then track(10) elsif truth(2, true) then track(20) else track(30) endif",
                environment, probe);
        assertThat(probe.snapshot()).containsExactly(1, 2, 20);
        assertOptimizedNode(
                "if truth(1, false) then track(10) elsif truth(2, true) then track(20) else track(30) endif",
                environment, TwoBranchConditionalExecutableNode.class);

        assertEquivalent(
                "if truth(1, false) then track(10) elsif truth(2, false) then track(20) "
                        + "elsif truth(3, true) then track(30) else track(40) endif",
                environment, probe);
        assertThat(probe.snapshot()).containsExactly(1, 2, 3, 30);
        assertOptimizedNode(
                "if truth(1, false) then track(10) elsif truth(2, false) then track(20) "
                        + "elsif truth(3, true) then track(30) else track(40) endif",
                environment, ConditionalExecutableNode.class);
    }

    @Test
    void decimalBinaryOperationsPreserveValueScaleAndFailures() {
        ExpressionEnvironment environment = numberEnvironment();

        for (String source : List.of("a + b", "a - b", "a * b", "a / b", "a mod b", "a / zero", "a mod zero")) {
            assertEquivalent(source, environment, null);
        }
        assertEquivalent("a ^ b", environment, null);
        assertEquivalent("b root a", environment, null);
        assertOptimizedNode("a + b", environment, AddDecimalExecutableNode.class);
        assertOptimizedNode("a - b", environment, SubtractDecimalExecutableNode.class);
        assertOptimizedNode("a * b", environment, MultiplyDecimalExecutableNode.class);
        assertOptimizedNode("a mod b", environment, ModuloDecimalExecutableNode.class);
        assertOptimizedNode("a / b", environment, BinaryExecutableNode.class);
    }

    private static void assertEquivalent(String source, ExpressionEnvironment environment, EffectProbe probe) {
        SemanticModel model = OraclePlanFixtures.resolve(source, environment);
        if (probe == null) {
            PlanEquivalenceHarness.assertEquivalent(model, environment, Map.of(), Clock.systemUTC());
            return;
        }
        PlanEquivalenceHarness.assertEquivalent(
                model, environment, Map.of(), Clock.systemUTC(), probe::reset, probe::snapshot);
    }

    private static void assertOptimizedNode(
            String source, ExpressionEnvironment environment, Class<?> expectedNodeType) {
        SemanticModel model = OraclePlanFixtures.resolve(source, environment);
        ExecutionPlan optimized = OraclePlanFixtures.buildOptimized(model, environment);
        ExecutionPlan oracle = OraclePlanFixtures.buildOracle(model, environment);

        assertThat(optimized.resultExpression()).isInstanceOf(expectedNodeType);
        assertThat(oracle.resultExpression()).isInstanceOf(
                expectedNodeType == BetweenExecutableNode.class || expectedNodeType == BinaryExecutableNode.class
                        || expectedNodeType == NullCoalesceExecutableNode.class
                        || expectedNodeType == ConditionalExecutableNode.class
                        ? expectedNodeType
                        : oracleNodeType(expectedNodeType));
    }

    private static Class<?> oracleNodeType(Class<?> specializedNodeType) {
        if (specializedNodeType == BinaryNullCoalesceExecutableNode.class) {
            return NullCoalesceExecutableNode.class;
        }
        if (specializedNodeType == TwoBranchConditionalExecutableNode.class) {
            return ConditionalExecutableNode.class;
        }
        return BinaryExecutableNode.class;
    }

    private static ExpressionEnvironment effectEnvironment(EffectProbe probe) {
        return ExpressionEnvironment.builder()
                .functionsFrom(probe, FunctionPurity.IMPURE)
                .build();
    }

    private static ExpressionEnvironment stringEnvironment() {
        return ExpressionEnvironment.builder()
                .externalSymbol("a", ScalarType.STRING, "a", ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .externalSymbol("b", ScalarType.STRING, "b", ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .externalSymbol("c", ScalarType.STRING, "c", ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .externalSymbol("d", ScalarType.STRING, "d", ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
    }

    private static ExpressionEnvironment numberEnvironment() {
        return ExpressionEnvironment.builder()
                .externalSymbol("a", ScalarType.NUMBER, new BigDecimal("10.00"), ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .externalSymbol("b", ScalarType.NUMBER, new BigDecimal("3.0"), ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .externalSymbol("zero", ScalarType.NUMBER, BigDecimal.ZERO, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
    }

    public static final class EffectProbe {
        private final List<Integer> calls = new ArrayList<>();

        public Map<String, BigDecimal> map(BigDecimal tag) {
            calls.add(tag.intValueExact());
            return new LinkedHashMap<>();
        }

        public BigDecimal track(BigDecimal tag) {
            calls.add(tag.intValueExact());
            return tag;
        }

        public BigDecimal number(BigDecimal value, BigDecimal tag) {
            calls.add(tag.intValueExact());
            return value;
        }

        public Boolean truth(BigDecimal tag, Boolean result) {
            calls.add(tag.intValueExact());
            return result;
        }

        void reset() {
            calls.clear();
        }

        List<Integer> snapshot() {
            return List.copyOf(calls);
        }
    }
}
