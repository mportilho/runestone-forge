package com.runestone.expeval_mk3.api;

import com.runestone.expeval_mk3.internal.runtime.CountingClock;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Proves issue #96's deterministic, all-or-nothing input preparation and an {@link ExpressionEngine}'s
 * clock seam through the public compile/compute pipeline.
 */
class RuntimeInputPreparationTest {

    @Test
    void rejectsTheLexicographicallySmallestUnknownOverrideKeyFirstRegardlessOfMapIterationOrder() {
        MathExpression expression = ExpressionEngine.defaultEngine().compileOrThrow("1 + 2", ExpressionEnvironment.standard()).asMath();
        Map<String, Object> overrides = new LinkedHashMap<>();
        overrides.put("zzz", BigDecimal.ONE);
        overrides.put("aaa", BigDecimal.ONE);
        overrides.put("mmm", BigDecimal.ONE);

        assertThatThrownBy(() -> expression.compute(overrides))
                .isInstanceOf(ExpressionExecutionException.class)
                .hasMessageContaining("aaa")
                .satisfies(exception -> assertThat(((ExpressionExecutionException) exception).diagnostic().code())
                        .isEqualTo("RUNTIME_INVALID_EXTERNAL_INPUT"));
    }

    @Test
    void validatesDeclaredSymbolOverridesInCanonicalOrderRegardlessOfMapIterationOrder() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("bFixed", BigDecimal.ONE, ExternalSymbolOverwritePolicy.FIXED)
                .externalSymbol("aFixed", BigDecimal.ONE, ExternalSymbolOverwritePolicy.FIXED)
                .build();
        MathExpression expression = ExpressionEngine.defaultEngine().compileOrThrow("aFixed + bFixed", environment).asMath();
        Map<String, Object> overrides = new LinkedHashMap<>();
        overrides.put("bFixed", BigDecimal.TEN);
        overrides.put("aFixed", BigDecimal.TEN);

        assertThatThrownBy(() -> expression.compute(overrides))
                .isInstanceOf(ExpressionExecutionException.class)
                .hasMessageContaining("aFixed")
                .satisfies(exception -> assertThat(((ExpressionExecutionException) exception).diagnostic().code())
                        .isEqualTo("RUNTIME_INVALID_EXTERNAL_INPUT"));
    }

    @Test
    void rejectsAnInvalidOverrideWithoutObservingAnyEarlierValidOverridesProviderEffect() {
        ExpressionEngineTest.CountingFunctions functions = new ExpressionEngineTest.CountingFunctions();
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functionsFrom(functions, FunctionPurity.IMPURE)
                .externalSymbol("aValid", BigDecimal.ONE, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .externalSymbol("zInvalid", BigDecimal.ONE, ExternalSymbolOverwritePolicy.FIXED)
                .build();
        MathExpression expression = ExpressionEngine.defaultEngine().compileOrThrow("bump(aValid)", environment).asMath();

        assertThatThrownBy(() -> expression.compute(Map.of("aValid", BigDecimal.TEN, "zInvalid", BigDecimal.TEN)))
                .isInstanceOf(ExpressionExecutionException.class)
                .satisfies(exception -> assertThat(((ExpressionExecutionException) exception).diagnostic().code())
                        .isEqualTo("RUNTIME_INVALID_EXTERNAL_INPUT"));

        assertThat(functions.invocations()).isZero();
    }

    @Test
    void rejectsANullOverrideWithoutObservingAnyEarlierValidOverridesProviderEffect() {
        ExpressionEngineTest.CountingFunctions functions = new ExpressionEngineTest.CountingFunctions();
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functionsFrom(functions, FunctionPurity.IMPURE)
                .externalSymbol("aValid", BigDecimal.ONE, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .externalSymbol("zNullable", BigDecimal.ONE, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
        MathExpression expression = ExpressionEngine.defaultEngine().compileOrThrow("bump(aValid)", environment).asMath();
        Map<String, Object> overrides = new LinkedHashMap<>();
        overrides.put("aValid", BigDecimal.TEN);
        overrides.put("zNullable", null);

        assertThatThrownBy(() -> expression.compute(overrides)).isInstanceOf(IllegalArgumentException.class);

        assertThat(functions.invocations()).isZero();
    }

    @Test
    void rejectsAnOverrideWithAnInvalidElementInsideANestedContainerWithoutAnyProviderEffect() {
        ExpressionEngineTest.CountingFunctions functions = new ExpressionEngineTest.CountingFunctions();
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functionsFrom(functions, FunctionPurity.IMPURE)
                .externalSymbol(
                        "items",
                        new CollectionType(ScalarType.NUMBER),
                        List.of(BigDecimal.ONE),
                        ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
        MathExpression expression = ExpressionEngine.defaultEngine().compileOrThrow("trackNumber(items[0])", environment).asMath();

        assertThatThrownBy(() -> expression.compute(Map.of("items", Arrays.asList(BigDecimal.ONE, null))))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(functions.invocations()).isZero();
    }

    @Test
    void neverConsultsTheClockWhenNoCurrentTemporalValueIsUsed() {
        CountingClock clock = new CountingClock(Clock.fixed(Instant.parse("2024-01-01T00:00:00Z"), ZoneOffset.UTC));
        ExpressionEngine engine = ExpressionEngine.builder().clock(clock).build();
        MathExpression expression = engine.compileOrThrow("1 + 2", ExpressionEnvironment.standard()).asMath();

        expression.compute();

        assertThat(clock.callCount()).isZero();
    }

    @Test
    void consultsTheClockExactlyOnceAndTruncatesToWholeSecondsWhenACurrentTemporalValueIsUsed() {
        Instant instantWithNanos = Instant.parse("2024-03-15T10:20:30.123456789Z");
        CountingClock clock = new CountingClock(Clock.fixed(instantWithNanos, ZoneOffset.UTC));
        ExpressionEngine engine = ExpressionEngine.builder().clock(clock).build();
        ExpressionEnvironment environment = ExpressionEnvironment.builder().zoneId(ZoneOffset.UTC).build();
        ResultExpression expression = engine.compileOrThrow("currDateTime", environment).asResult();

        Object result = expression.compute();

        assertThat(result).isEqualTo(LocalDateTime.of(2024, 3, 15, 10, 20, 30));
        assertThat(clock.callCount()).isEqualTo(1);
    }
}
