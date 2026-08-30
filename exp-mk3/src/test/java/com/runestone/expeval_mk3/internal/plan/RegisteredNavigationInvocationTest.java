package com.runestone.expeval_mk3.internal.plan;

import com.runestone.expeval_mk3.api.ExpressionEnvironment;
import com.runestone.expeval_mk3.api.ExpressionExecutionException;
import com.runestone.expeval_mk3.api.ExternalSymbolOverwritePolicy;
import com.runestone.expeval_mk3.api.FunctionPurity;
import com.runestone.expeval_mk3.api.FunctionSignature;
import com.runestone.expeval_mk3.api.JavaMethodDescriptor;
import com.runestone.expeval_mk3.api.JavaPropertyDescriptor;
import com.runestone.expeval_mk3.api.JavaTypeDescriptor;
import com.runestone.expeval_mk3.api.ObjectType;
import com.runestone.expeval_mk3.api.ScalarType;
import com.runestone.expeval_mk3.internal.semantics.SemanticModel;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RegisteredNavigationInvocationTest {

    @Test
    void preparedPropertyAndMethodEntryPointsPreserveNavigationEquivalence() {
        ExpressionEnvironment environment = environment();

        assertEquivalent("provider?.name ?? \"fallback\"", environment);
        assertEquivalent("provider?.increment(41) ?? 0", environment);
        assertEquivalent("provider.sum3(1, 2, 3)", environment);
        assertEquivalent("provider.sum4(1, 2, 3, 4)", environment);
    }

    @Test
    void preparedEntryPointsPreservePropertyAndMethodFailures() {
        ExpressionEnvironment environment = environment();

        assertEquivalent("provider?.broken ?? \"fallback\"", environment);
        assertEquivalent("provider?.fail() ?? \"fallback\"", environment);
        assertCausePreserved("provider?.broken ?? \"fallback\"", environment);
        assertCausePreserved("provider?.fail() ?? \"fallback\"", environment);
    }

    @Test
    void nonCanonicalRegisteredMethodArgumentKeepsItsNumericConversion() throws Throwable {
        ExpressionEnvironment environment = environment();
        JavaTypeDescriptor type = environment.javaTypes().find(Provider.class).orElseThrow();
        JavaPropertyDescriptor property = type.findProperty("name").orElseThrow();
        JavaMethodDescriptor method = type.findMethod(
                new FunctionSignature("increment", List.of(ScalarType.NUMBER))).orElseThrow();
        JavaMethodDescriptor sum3 = type.findMethod(new FunctionSignature(
                "sum3", List.of(ScalarType.NUMBER, ScalarType.NUMBER, ScalarType.NUMBER))).orElseThrow();
        JavaMethodDescriptor sum4 = type.findMethod(new FunctionSignature(
                "sum4", List.of(ScalarType.NUMBER, ScalarType.NUMBER, ScalarType.NUMBER, ScalarType.NUMBER)))
                .orElseThrow();
        Provider receiver = new Provider();

        assertThat(property.invoke(receiver)).isEqualTo("prepared");
        assertThat(method.invoke(receiver, new BigDecimal("41"))).isEqualTo(new BigDecimal("42"));
        assertThat(sum3.invoke(receiver, BigDecimal.ONE, BigDecimal.TWO, new BigDecimal("3")))
                .isEqualTo(new BigDecimal("6"));
        assertThat(sum4.invokeArray(new Object[] {
                receiver, BigDecimal.ONE, BigDecimal.TWO, new BigDecimal("3"), new BigDecimal("4")}))
                .isEqualTo(new BigDecimal("10"));
        assertThatNullPointerException().isThrownBy(() -> property.invoke(null)).withMessage("receiver");
        assertThatNullPointerException().isThrownBy(() -> method.invoke(receiver, null)).withMessage("argument0");
    }

    @Test
    void arrayEntryPointPreservesIndexedNullFailures() {
        JavaMethodDescriptor sum4 = environment().javaTypes()
                .find(Provider.class)
                .orElseThrow()
                .findMethod(new FunctionSignature(
                        "sum4", List.of(ScalarType.NUMBER, ScalarType.NUMBER, ScalarType.NUMBER, ScalarType.NUMBER)))
                .orElseThrow();
        Provider receiver = new Provider();

        assertThatNullPointerException()
                .isThrownBy(() -> sum4.invokeArray(new Object[] {
                    null, BigDecimal.ONE, BigDecimal.TWO, new BigDecimal("3"), new BigDecimal("4")
                }))
                .withMessage("receiverAndArguments[0]");
        assertThatNullPointerException()
                .isThrownBy(() -> sum4.invokeArray(new Object[] {
                    receiver, BigDecimal.ONE, BigDecimal.TWO, null, new BigDecimal("4")
                }))
                .withMessage("receiverAndArguments[3]");
    }

    private static void assertEquivalent(String source, ExpressionEnvironment environment) {
        SemanticModel model = OraclePlanFixtures.resolve(source, environment);
        PlanEquivalenceHarness.assertEquivalent(model, environment, Map.of(), Clock.systemUTC());
    }

    private static void assertCausePreserved(String source, ExpressionEnvironment environment) {
        SemanticModel model = OraclePlanFixtures.resolve(source, environment);
        for (ExecutionPlan plan : List.of(
                OraclePlanFixtures.buildOptimized(model, environment),
                OraclePlanFixtures.buildOracle(model, environment))) {
            assertThatThrownBy(() -> plan.compute(Map.of(), Clock.systemUTC()))
                    .isInstanceOf(ExpressionExecutionException.class)
                    .hasCauseInstanceOf(IllegalStateException.class);
        }
    }

    private static ExpressionEnvironment environment() {
        ObjectType providerType = new ObjectType(Provider.class.getName());
        return ExpressionEnvironment.builder()
                .registerJavaType(Provider.class)
                .registerJavaTypeMethod(Provider.class, "increment", FunctionPurity.PURE, int.class)
                .registerJavaTypeMethod(
                        Provider.class,
                        "sum3",
                        FunctionPurity.PURE,
                        BigDecimal.class,
                        BigDecimal.class,
                        BigDecimal.class)
                .registerJavaTypeMethod(
                        Provider.class,
                        "sum4",
                        FunctionPurity.PURE,
                        BigDecimal.class,
                        BigDecimal.class,
                        BigDecimal.class,
                        BigDecimal.class)
                .registerJavaTypeMethod(Provider.class, "fail", FunctionPurity.PURE)
                .externalSymbol(
                        "provider",
                        providerType,
                        new Provider(),
                        ExternalSymbolOverwritePolicy.FIXED)
                .build();
    }

    public static final class Provider {

        public String getName() {
            return "prepared";
        }

        public String getBroken() {
            throw new IllegalStateException("broken property");
        }

        public int increment(int value) {
            return value + 1;
        }

        public BigDecimal sum3(BigDecimal first, BigDecimal second, BigDecimal third) {
            return first.add(second).add(third);
        }

        public BigDecimal sum4(BigDecimal first, BigDecimal second, BigDecimal third, BigDecimal fourth) {
            return first.add(second).add(third).add(fourth);
        }

        public String fail() {
            throw new IllegalStateException("broken method");
        }
    }
}
