package com.runestone.expeval.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExpressionResourceLimitsTest {

    @ParameterizedTest
    @CsvSource({
            "maxSourceLength,16384,262144", "maxTokenCount,4096,65536",
            "maxSyntaxDepth,64,256", "maxAstNodeCount,4096,65536",
            "maxCurrentItemDepth,32,64", "maxMaterializedSize,10000,100000",
            "maxFactorialInput,1000,10000", "maxTextLength,1048576,8388608",
            "maxValueDepth,64,256", "maxNumericPrecision,10000,100000",
            "maxNumericScaleMagnitude,10000,100000", "maxRegexPatternLength,1024,8192",
            "maxTraversalSteps,1000000,100000000"
    })
    void validatesEveryPublicPropertyAtTheMutatorAndBuildsIndependentSnapshots(
            String property, int defaultValue, int ceiling) throws ReflectiveOperationException {
        Method getter = ExpressionResourceLimits.class.getMethod(property);
        Method setter = ExpressionResourceLimits.Builder.class.getMethod(property, int.class);
        ExpressionResourceLimits.Builder builder = ExpressionResourceLimits.builder();
        assertThat(getter.invoke(ExpressionResourceLimits.defaults())).isEqualTo(defaultValue);
        assertThat(getter.invoke(builder.build())).isEqualTo(defaultValue);
        setter.invoke(builder, 0);
        ExpressionResourceLimits zero = builder.build();
        setter.invoke(builder, ceiling);
        ExpressionResourceLimits maximum = builder.build();
        assertThat(getter.invoke(zero)).isEqualTo(0);
        assertThat(getter.invoke(maximum)).isEqualTo(ceiling);
        for (int invalid : new int[]{-1, ceiling + 1, Integer.MIN_VALUE, Integer.MAX_VALUE}) {
            assertThatThrownBy(() -> setter.invoke(builder, invalid))
                    .isInstanceOf(InvocationTargetException.class)
                    .cause().isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(property + " must be in [0, " + ceiling + "]: " + invalid);
            assertThat(getter.invoke(builder.build())).isEqualTo(ceiling);
        }
    }

    @ParameterizedTest
    @EnumSource(ExpressionTrustMode.class)
    void environmentRetainsTheAggregateAndValidatesConfigurationInEveryMode(ExpressionTrustMode mode) {
        ExpressionResourceLimits limits = ExpressionResourceLimits.builder().maxFactorialInput(0).build();
        ExpressionEnvironment.Builder builder = ExpressionEnvironment.builder().trustMode(mode).resourceLimits(limits);
        ExpressionEnvironment environment = builder.build();
        builder.resourceLimits(ExpressionResourceLimits.defaults()).trustMode(ExpressionTrustMode.TRUSTED);
        assertThat(environment.resourceLimits()).isSameAs(limits);
        assertThat(environment.trustMode()).isEqualTo(mode);
        assertThatThrownBy(() -> builder.trustMode(mode).resourceLimits(
                ExpressionResourceLimits.builder().maxMaterializedSize(100_001).build()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> builder.resourceLimits(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> builder.trustMode(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void environmentExposesOnlyTheAggregateAndDefaultsToTrusted() {
        assertThat(ExpressionEnvironment.standard().trustMode()).isEqualTo(ExpressionTrustMode.TRUSTED);
        assertThat(ExpressionEnvironment.builder().trustMode(ExpressionTrustMode.SAFE).build().resourceLimits())
                .isSameAs(ExpressionResourceLimits.defaults());
        for (Class<?> type : new Class<?>[]{ExpressionEnvironment.class, ExpressionEnvironment.Builder.class}) {
            assertThat(type.getMethods()).extracting(Method::getName)
                    .doesNotContain("maxCurrentItemDepth", "maxMaterializedSize", "maxFactorialInput");
        }
        for (Class<?> type : new Class<?>[]{ExpressionResourceLimits.class, ExpressionResourceLimits.Builder.class}) {
            assertThat(type.getMethods()).extracting(Method::getName).doesNotContain("maxEvaluationWork");
        }
    }
}
