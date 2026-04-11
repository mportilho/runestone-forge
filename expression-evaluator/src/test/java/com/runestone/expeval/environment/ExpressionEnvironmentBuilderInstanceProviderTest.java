package com.runestone.expeval.environment;

import com.runestone.expeval.api.MathExpression;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.MathContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExpressionEnvironmentBuilderInstanceProviderTest {

    // --- Fixtures ---

    static final class ScalerFixture {
        private final BigDecimal factor;

        ScalerFixture(BigDecimal factor) {
            this.factor = factor;
        }

        public BigDecimal scale(BigDecimal value) {
            return value.multiply(factor);
        }

        public BigDecimal scaleWithMathContext(MathContext mathContext, BigDecimal value) {
            return value.multiply(factor, mathContext);
        }
    }

    static final class MixedFixture {
        public BigDecimal instanceDouble(BigDecimal x) {
            return x.multiply(BigDecimal.TWO);
        }

        public static BigDecimal staticTriple(BigDecimal x) {
            return x.multiply(new BigDecimal("3"));
        }
    }

    static final class AdderFixture {
        public BigDecimal addTen(BigDecimal x) {
            return x.add(BigDecimal.TEN);
        }
    }

    static final class StaticOnlyFixture {
        public static BigDecimal triple(BigDecimal x) {
            return x.multiply(new BigDecimal("3"));
        }
    }

    static final class PrecisionDivideFixture {
        public BigDecimal divide(MathContext ctx, BigDecimal a, BigDecimal b) {
            return a.divide(b, ctx);
        }
    }

    // --- Tests ---

    @Test
    void shouldRejectNullInstanceProvider() {
        ExpressionEnvironmentBuilder builder = ExpressionEnvironment.builder();

        assertThatThrownBy(() -> builder.registerInstanceProvider(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldReturnBuilderForFluentChaining() {
        ExpressionEnvironmentBuilder builder = ExpressionEnvironment.builder();

        ExpressionEnvironmentBuilder returned = builder.registerInstanceProvider(new ScalerFixture(BigDecimal.TWO));

        assertThat(returned).isSameAs(builder);
    }

    @Test
    void shouldRegisterInstanceMethodsInFunctionCatalog() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .registerInstanceProvider(new ScalerFixture(new BigDecimal("3")))
                .build();

        BigDecimal result = MathExpression.compile("scale(5)", environment).compute();

        assertThat(result).isEqualByComparingTo("15");
    }

    @Test
    void shouldRegisterInstanceMethodsInFunctionCatalogWithMathContext() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .registerInstanceProvider(new ScalerFixture(new BigDecimal("3")))
                .build();

        BigDecimal result = MathExpression.compile("scaleWithMathContext(5)", environment).compute();

        assertThat(result).isEqualByComparingTo("15");
    }

    @Test
    void shouldUseInstanceStateWhenEvaluating() {
        ExpressionEnvironment envWith3 = ExpressionEnvironment.builder()
                .registerInstanceProvider(new ScalerFixture(new BigDecimal("3")))
                .build();
        ExpressionEnvironment envWith7 = ExpressionEnvironment.builder()
                .registerInstanceProvider(new ScalerFixture(new BigDecimal("7")))
                .build();

        BigDecimal result3 = MathExpression.compile("scale(2)", envWith3).compute();
        BigDecimal result7 = MathExpression.compile("scale(2)", envWith7).compute();

        assertThat(result3).isEqualByComparingTo("6");
        assertThat(result7).isEqualByComparingTo("14");
    }

    @Test
    void shouldExcludeStaticMethodsFromInstanceProvider() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .registerInstanceProvider(new MixedFixture())
                .build();

        BigDecimal instanceResult = MathExpression.compile("instanceDouble(4)", environment).compute();
        assertThat(instanceResult).isEqualByComparingTo("8");

        assertThatThrownBy(() -> MathExpression.compile("staticTriple(4)", environment))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldExcludeObjectDeclaredMethods() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .registerInstanceProvider(new ScalerFixture(BigDecimal.ONE))
                .build();

        assertThatThrownBy(() -> MathExpression.compile("hashCode()", environment))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldAccumulateMultipleInstanceProviders() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .registerInstanceProvider(new ScalerFixture(new BigDecimal("2")))
                .registerInstanceProvider(new AdderFixture())
                .build();

        BigDecimal scaled = MathExpression.compile("scale(5)", environment).compute();
        BigDecimal added = MathExpression.compile("addTen(5)", environment).compute();

        assertThat(scaled).isEqualByComparingTo("10");
        assertThat(added).isEqualByComparingTo("15");
    }

    @Test
    void shouldCombineInstanceAndStaticProviders() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .registerInstanceProvider(new ScalerFixture(new BigDecimal("2")))
                .registerStaticProvider(StaticOnlyFixture.class)
                .build();

        BigDecimal instanceResult = MathExpression.compile("scale(5)", environment).compute();
        BigDecimal staticResult = MathExpression.compile("triple(5)", environment).compute();

        assertThat(instanceResult).isEqualByComparingTo("10");
        assertThat(staticResult).isEqualByComparingTo("15");
    }

    @Test
    void shouldInjectMathContextForInstanceMethodWithMathContextFirstParameter() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .registerInstanceProvider(new PrecisionDivideFixture())
                .build();

        BigDecimal result = MathExpression.compile("divide(1, 3)", environment).compute();

        assertThat(result.toPlainString()).startsWith("0.333333333");
    }
}
