package com.runestone.expeval.api;

import com.runestone.expeval.environment.ExpressionEnvironment;
import com.runestone.expeval.internal.runtime.CompiledExpression;
import com.runestone.expeval.internal.runtime.ExpressionRuntimeSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ExpressionEngine")
class ExpressionEngineTest {

    private static final ExpressionEnvironment ENV = ExpressionEnvironment.builder().build();

    @Test
    @DisplayName("compiles math expressions with explicit cache configuration")
    void compilesMathExpressionWithExplicitCacheConfiguration() {
        ExpressionEngine engine = ExpressionEngine.builder()
                .cacheConfig(new CacheConfig(64, Duration.ofMinutes(30)))
                .build();

        BigDecimal result = engine.compileMath("3 + 4", ENV).compute();

        assertThat(result).isEqualByComparingTo("7");
    }

    @Test
    @DisplayName("compiles logical expressions through public overload")
    void compilesLogicalExpressionThroughPublicOverload() {
        ExpressionEngine engine = new ExpressionEngine(CacheConfig.defaults());

        boolean result = LogicalExpression.compile("5 > 3", ENV, engine).compute();

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("compiles assignment expressions through public overload")
    void compilesAssignmentExpressionThroughPublicOverload() {
        ExpressionEngine engine = new ExpressionEngine(CacheConfig.defaults());

        Map<String, Object> result = AssignmentExpression
                .compile("total = 6 * 7;", ENV, engine)
                .compute();

        assertThat(result).containsKey("total");
        assertThat((BigDecimal) result.get("total")).isEqualByComparingTo("42");
    }

    @Test
    @DisplayName("reuses compiled expressions inside the same engine")
    void reusesCompiledExpressionsInsideSameEngine() throws ReflectiveOperationException {
        ExpressionEngine engine = new ExpressionEngine(CacheConfig.defaults());

        MathExpression first = engine.compileMath("10 + 5", ENV);
        MathExpression second = engine.compileMath("10 + 5", ENV);

        assertThat(compiledExpressionOf(second)).isSameAs(compiledExpressionOf(first));
    }

    @Test
    @DisplayName("invalidating one engine does not affect another engine")
    void invalidatingOneEngineDoesNotAffectAnotherEngine() throws ReflectiveOperationException {
        ExpressionEngine engineA = new ExpressionEngine(CacheConfig.defaults());
        ExpressionEngine engineB = new ExpressionEngine(CacheConfig.defaults());
        MathExpression fromA = engineA.compileMath("20 + 1", ENV);
        MathExpression fromB = engineB.compileMath("20 + 1", ENV);

        engineA.invalidateCache();

        MathExpression afterInvalidationA = engineA.compileMath("20 + 1", ENV);
        MathExpression afterInvalidationB = engineB.compileMath("20 + 1", ENV);
        assertThat(compiledExpressionOf(afterInvalidationA)).isNotSameAs(compiledExpressionOf(fromA));
        assertThat(compiledExpressionOf(afterInvalidationB)).isSameAs(compiledExpressionOf(fromB));
    }

    @Test
    @DisplayName("validates expressions with the engine cache")
    void validatesExpressionsWithEngineCache() {
        ExpressionEngine engine = new ExpressionEngine(CacheConfig.defaults());

        ValidationResult result = MathExpression.validate("missing() + 1", ENV, engine);

        assertThat(result.valid()).isFalse();
        assertThat(result.issues())
                .isNotEmpty()
                .anySatisfy(issue -> assertThat(issue.code()).isEqualTo(IssueCode.UNKNOWN_FUNCTION));
    }

    @Test
    @DisplayName("rejects null configuration and engine arguments")
    void rejectsNullConfigurationAndEngineArguments() {
        assertThatThrownBy(() -> new ExpressionEngine(null))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> MathExpression.compile("1 + 1", ENV, (ExpressionEngine) null))
                .isInstanceOf(NullPointerException.class);
    }

    private static CompiledExpression compiledExpressionOf(MathExpression expression) throws ReflectiveOperationException {
        Field runtimeField = MathExpression.class.getDeclaredField("runtime");
        runtimeField.setAccessible(true);
        ExpressionRuntimeSupport runtime = (ExpressionRuntimeSupport) runtimeField.get(expression);
        return runtime.getCompiledExpression();
    }
}
