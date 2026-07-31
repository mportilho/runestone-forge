package com.runestone.expeval_mk3.api;

import com.runestone.expeval_mk3.internal.runtime.CountingClock;
import com.runestone.expeval_mk3.internal.runtime.RuntimeServices;
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
 * Proves issue #96's deterministic, all-or-nothing input preparation and the {@link RuntimeServices}
 * clock seam through the public compile/compute pipeline.
 */
class RuntimeInputPreparationTest {

    @Test
    void rejectsTheLexicographicallySmallestUnknownOverrideKeyFirstRegardlessOfMapIterationOrder() {
        CompiledExpression expression = ExpressionCompiler.compileOrThrow("1 + 2", ExpressionEnvironment.standard());
        Map<String, Object> overrides = new LinkedHashMap<>();
        overrides.put("zzz", BigDecimal.ONE);
        overrides.put("aaa", BigDecimal.ONE);
        overrides.put("mmm", BigDecimal.ONE);

        assertThatThrownBy(() -> expression.compute(overrides))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("aaa");
    }

    @Test
    void validatesDeclaredSymbolOverridesInCanonicalOrderRegardlessOfMapIterationOrder() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("bFixed", BigDecimal.ONE, ExternalSymbolOverwritePolicy.FIXED)
                .externalSymbol("aFixed", BigDecimal.ONE, ExternalSymbolOverwritePolicy.FIXED)
                .build();
        CompiledExpression expression = ExpressionCompiler.compileOrThrow("aFixed + bFixed", environment);
        Map<String, Object> overrides = new LinkedHashMap<>();
        overrides.put("bFixed", BigDecimal.TEN);
        overrides.put("aFixed", BigDecimal.TEN);

        assertThatThrownBy(() -> expression.compute(overrides))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("aFixed");
    }

    @Test
    void rejectsAnInvalidOverrideWithoutObservingAnyEarlierValidOverridesProviderEffect() {
        ExpressionCompilerTest.CountingFunctions functions = new ExpressionCompilerTest.CountingFunctions();
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functionsFrom(functions, FunctionPurity.IMPURE)
                .externalSymbol("aValid", BigDecimal.ONE, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .externalSymbol("zInvalid", BigDecimal.ONE, ExternalSymbolOverwritePolicy.FIXED)
                .build();
        CompiledExpression expression = ExpressionCompiler.compileOrThrow("bump(aValid)", environment);

        assertThatThrownBy(() -> expression.compute(Map.of("aValid", BigDecimal.TEN, "zInvalid", BigDecimal.TEN)))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(functions.invocations()).isZero();
    }

    @Test
    void rejectsANullOverrideWithoutObservingAnyEarlierValidOverridesProviderEffect() {
        ExpressionCompilerTest.CountingFunctions functions = new ExpressionCompilerTest.CountingFunctions();
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functionsFrom(functions, FunctionPurity.IMPURE)
                .externalSymbol("aValid", BigDecimal.ONE, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .externalSymbol("zNullable", BigDecimal.ONE, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
        CompiledExpression expression = ExpressionCompiler.compileOrThrow("bump(aValid)", environment);
        Map<String, Object> overrides = new LinkedHashMap<>();
        overrides.put("aValid", BigDecimal.TEN);
        overrides.put("zNullable", null);

        assertThatThrownBy(() -> expression.compute(overrides)).isInstanceOf(IllegalArgumentException.class);

        assertThat(functions.invocations()).isZero();
    }

    @Test
    void rejectsAnOverrideWithAnInvalidElementInsideANestedContainerWithoutAnyProviderEffect() {
        ExpressionCompilerTest.CountingFunctions functions = new ExpressionCompilerTest.CountingFunctions();
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functionsFrom(functions, FunctionPurity.IMPURE)
                .externalSymbol(
                        "items",
                        new CollectionType(ScalarType.NUMBER),
                        List.of(BigDecimal.ONE),
                        ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
        CompiledExpression expression = ExpressionCompiler.compileOrThrow("trackNumber(items[0])", environment);

        assertThatThrownBy(() -> expression.compute(Map.of("items", Arrays.asList(BigDecimal.ONE, null))))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(functions.invocations()).isZero();
    }

    @Test
    void neverConsultsTheClockWhenNoCurrentTemporalValueIsUsed() {
        CountingClock clock = new CountingClock(Clock.fixed(Instant.parse("2024-01-01T00:00:00Z"), ZoneOffset.UTC));
        CompiledExpression expression = ExpressionCompiler.compileOrThrow(
                "1 + 2", ExpressionEnvironment.standard(), RuntimeServices.withClock(clock));

        expression.compute();

        assertThat(clock.callCount()).isZero();
    }

    @Test
    void consultsTheClockExactlyOnceAndTruncatesToWholeSecondsWhenACurrentTemporalValueIsUsed() {
        Instant instantWithNanos = Instant.parse("2024-03-15T10:20:30.123456789Z");
        CountingClock clock = new CountingClock(Clock.fixed(instantWithNanos, ZoneOffset.UTC));
        ExpressionEnvironment environment = ExpressionEnvironment.builder().zoneId(ZoneOffset.UTC).build();
        CompiledExpression expression = ExpressionCompiler.compileOrThrow(
                "currDateTime", environment, RuntimeServices.withClock(clock));

        Object result = expression.compute();

        assertThat(result).isEqualTo(LocalDateTime.of(2024, 3, 15, 10, 20, 30));
        assertThat(clock.callCount()).isEqualTo(1);
    }
}
