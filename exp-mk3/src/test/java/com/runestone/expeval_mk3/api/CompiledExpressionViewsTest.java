package com.runestone.expeval_mk3.api;

import com.runestone.expeval_mk3.internal.ast.SemanticAstBuildSuccess;
import com.runestone.expeval_mk3.internal.ast.SemanticAstBuilder;
import com.runestone.expeval_mk3.internal.parser.ExpressionParser;
import com.runestone.expeval_mk3.internal.parser.ParseSuccess;
import com.runestone.expeval_mk3.internal.plan.ExecutionPlan;
import com.runestone.expeval_mk3.internal.plan.ExecutionPlanBuilder;
import com.runestone.expeval_mk3.internal.runtime.RuntimeServices;
import com.runestone.expeval_mk3.internal.semantics.SemanticResolutionSuccess;
import com.runestone.expeval_mk3.internal.semantics.SemanticResolver;
import com.runestone.expeval_mk3.support.EnvironmentConfigurations;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CompiledExpressionViewsTest {

    @Test
    void asResultReturnsCanonicalScalarValue() {
        CompiledExpression expression = ExpressionCompiler.compileOrThrow("1 + 2", ExpressionEnvironment.standard());

        assertThat(expression.asResult().compute()).isEqualTo(new BigDecimal("3"));
    }

    @Test
    void asResultReturnsCanonicalCollectionValueAsAnImmutableSnapshot() {
        CompiledExpression expression = ExpressionCompiler.compileOrThrow("[1, 2, 3]", ExpressionEnvironment.standard());

        Object result = expression.asResult().compute();

        assertThat(result).isEqualTo(List.of(new BigDecimal("1"), new BigDecimal("2"), new BigDecimal("3")));
        assertThatThrownBy(() -> ((List<Object>) result).add(BigDecimal.TEN))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void asResultReturnsCanonicalMapValueWithTextualKeysInCanonicalOrder() throws Exception {
        CompiledExpression expression = ExpressionCompiler.compileOrThrow("labels", EnvironmentConfigurations.complete());

        Object result = expression.asResult().compute();

        assertThat(((Map<String, Object>) result).keySet()).containsExactly("tier");
        assertThatThrownBy(() -> ((Map<String, Object>) result).put("region", "east"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void asResultOrdersMultipleMapKeysCanonically() {
        Map<String, BigDecimal> unordered = new LinkedHashMap<>();
        unordered.put("zeta", BigDecimal.ONE);
        unordered.put("alpha", BigDecimal.TEN);
        unordered.put("mu", new BigDecimal("5"));
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("scores", new MapType(ScalarType.NUMBER), unordered, ExternalSymbolOverwritePolicy.FIXED)
                .build();
        CompiledExpression expression = ExpressionCompiler.compileOrThrow("scores", environment);

        Object result = expression.asResult().compute();

        assertThat(((Map<String, Object>) result).keySet()).containsExactly("alpha", "mu", "zeta");
    }

    @Test
    void asResultSupportsOverrides() throws Exception {
        CompiledExpression expression = ExpressionCompiler.compileOrThrow("amount", EnvironmentConfigurations.complete());

        Object result = expression.asResult().compute(Map.of("amount", new BigDecimal("42")));

        assertThat(result).isEqualTo(new BigDecimal("42"));
    }

    @Test
    void asMathReturnsBigDecimalForANumberResultAndSupportsOverrides() throws Exception {
        CompiledExpression expression = ExpressionCompiler.compileOrThrow("amount", EnvironmentConfigurations.complete());

        assertThat(expression.asMath().compute()).isEqualTo(BigDecimal.ONE);
        assertThat(expression.asMath().compute(Map.of("amount", new BigDecimal("7")))).isEqualTo(new BigDecimal("7"));
    }

    @Test
    void asLogicalReturnsBooleanForABooleanResultAndSupportsOverrides() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("flag", true, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
        CompiledExpression expression = ExpressionCompiler.compileOrThrow("flag", environment);

        assertThat(expression.asLogical().compute()).isTrue();
        assertThat(expression.asLogical().compute(Map.of("flag", false))).isFalse();
    }

    @Test
    void asMathRejectsANonNumberResultBeforeExecution() {
        CountingFunctions functions = new CountingFunctions();
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functionsFrom(functions, FunctionPurity.IMPURE)
                .build();
        CompiledExpression expression = ExpressionCompiler.compileOrThrow("markTrue()", environment);

        assertThatThrownBy(expression::asMath)
                .isInstanceOfSatisfying(ExpressionViewException.class, exception -> {
                    assertThat(exception.reason()).isEqualTo(ExpressionViewException.Reason.TYPE_MISMATCH);
                    assertThat(exception.foundType()).isEqualTo(ScalarType.BOOLEAN);
                });
        assertThat(functions.invocations()).isZero();
    }

    @Test
    void asLogicalRejectsANonBooleanResultBeforeExecution() {
        CountingFunctions functions = new CountingFunctions();
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functionsFrom(functions, FunctionPurity.IMPURE)
                .build();
        CompiledExpression expression = ExpressionCompiler.compileOrThrow("markNumber()", environment);

        assertThatThrownBy(expression::asLogical)
                .isInstanceOfSatisfying(ExpressionViewException.class, exception -> {
                    assertThat(exception.reason()).isEqualTo(ExpressionViewException.Reason.TYPE_MISMATCH);
                    assertThat(exception.foundType()).isEqualTo(ScalarType.NUMBER);
                });
        assertThat(functions.invocations()).isZero();
    }

    @Test
    void asResultRejectsADirectObjectTypeResultBeforeExecution() {
        WidgetFunctions functions = new WidgetFunctions();
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functionsFrom(functions, FunctionPurity.IMPURE)
                .registerJavaType(Widget.class)
                .build();
        CompiledExpression expression = ExpressionCompiler.compileOrThrow("markWidget()", environment);

        assertThatThrownBy(expression::asResult)
                .isInstanceOfSatisfying(ExpressionViewException.class, exception -> {
                    assertThat(exception.reason()).isEqualTo(ExpressionViewException.Reason.TYPE_NOT_PUBLICLY_EXPOSABLE);
                    assertThat(exception.foundType()).isEqualTo(new ObjectType(Widget.class.getName()));
                });
        assertThat(functions.invocations()).isZero();
    }

    @Test
    void asResultRejectsANestedObjectTypeResultBeforeExecution() {
        WidgetFunctions functions = new WidgetFunctions();
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functionsFrom(functions, FunctionPurity.IMPURE)
                .registerJavaType(Widget.class)
                .build();
        CompiledExpression expression = ExpressionCompiler.compileOrThrow("[markWidget()]", environment);

        assertThatThrownBy(expression::asResult)
                .isInstanceOfSatisfying(ExpressionViewException.class, exception ->
                        assertThat(exception.reason()).isEqualTo(ExpressionViewException.Reason.TYPE_NOT_PUBLICLY_EXPOSABLE));
        assertThat(functions.invocations()).isZero();
    }

    @Test
    void asMathAndAsLogicalAlsoRejectObjectTypeResultsAsNotPubliclyExposableBeforeExecution() {
        WidgetFunctions functions = new WidgetFunctions();
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functionsFrom(functions, FunctionPurity.IMPURE)
                .registerJavaType(Widget.class)
                .build();
        CompiledExpression expression = ExpressionCompiler.compileOrThrow("markWidget()", environment);

        assertThatThrownBy(expression::asMath)
                .isInstanceOfSatisfying(ExpressionViewException.class, exception ->
                        assertThat(exception.reason()).isEqualTo(ExpressionViewException.Reason.TYPE_NOT_PUBLICLY_EXPOSABLE));
        assertThatThrownBy(expression::asLogical)
                .isInstanceOfSatisfying(ExpressionViewException.class, exception ->
                        assertThat(exception.reason()).isEqualTo(ExpressionViewException.Reason.TYPE_NOT_PUBLICLY_EXPOSABLE));
        assertThat(functions.invocations()).isZero();
    }

    @Test
    void everyViewRejectsAPlanWithoutAResultExpressionBeforeExecution() {
        CountingFunctions functions = new CountingFunctions();
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functionsFrom(functions, FunctionPurity.IMPURE)
                .build();
        CompiledExpression expression = compiledExpressionForAssignmentsOnlySource("x := markNumber();", environment);

        assertThatThrownBy(expression::asResult)
                .isInstanceOfSatisfying(ExpressionViewException.class, exception -> {
                    assertThat(exception.reason()).isEqualTo(ExpressionViewException.Reason.NO_RESULT_EXPRESSION);
                    assertThat(exception.foundType()).isNull();
                });
        assertThatThrownBy(expression::asMath)
                .isInstanceOfSatisfying(ExpressionViewException.class, exception ->
                        assertThat(exception.reason()).isEqualTo(ExpressionViewException.Reason.NO_RESULT_EXPRESSION));
        assertThatThrownBy(expression::asLogical)
                .isInstanceOfSatisfying(ExpressionViewException.class, exception ->
                        assertThat(exception.reason()).isEqualTo(ExpressionViewException.Reason.NO_RESULT_EXPRESSION));
        assertThat(functions.invocations()).isZero();
    }

    @Test
    void resultAndMathViewsShareTheSameImmutablePlan() {
        CompiledExpression expression = ExpressionCompiler.compileOrThrow("1 + 2", ExpressionEnvironment.standard());

        Object compiledPlan = planField(expression);
        assertThat(planField(expression.asResult())).isSameAs(compiledPlan);
        assertThat(planField(expression.asMath())).isSameAs(compiledPlan);
    }

    @Test
    void resultAndLogicalViewsShareTheSameImmutablePlan() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("flag", true, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
        CompiledExpression expression = ExpressionCompiler.compileOrThrow("flag", environment);

        Object compiledPlan = planField(expression);
        assertThat(planField(expression.asResult())).isSameAs(compiledPlan);
        assertThat(planField(expression.asLogical())).isSameAs(compiledPlan);
    }

    private static Object planField(Object target) {
        try {
            Field field = target.getClass().getDeclaredField("plan");
            field.setAccessible(true);
            return field.get(target);
        } catch (ReflectiveOperationException exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Builds a {@code CompiledExpression} for an assignments-only source, bypassing
     * {@link ExpressionCompiler}'s current rejection of such files at compile time, so the views'
     * defensive {@link ExpressionViewException.Reason#NO_RESULT_EXPRESSION} handling can be exercised
     * against a genuine no-result {@code ExecutionPlan}.
     */
    private static CompiledExpression compiledExpressionForAssignmentsOnlySource(
            String source, ExpressionEnvironment environment) {
        ParseSuccess parseSuccess = (ParseSuccess) new ExpressionParser().parse(source);
        SemanticAstBuildSuccess astSuccess =
                (SemanticAstBuildSuccess) new SemanticAstBuilder().build(parseSuccess);
        SemanticResolutionSuccess resolutionSuccess =
                (SemanticResolutionSuccess) new SemanticResolver().resolve(astSuccess.file(), environment);
        ExecutionPlan plan = new ExecutionPlanBuilder().build(resolutionSuccess.model(), environment);
        return new CompiledExpression(plan, RuntimeServices.systemDefault(), resolutionSuccess.warnings());
    }

    public static final class CountingFunctions {

        private final AtomicInteger invocations = new AtomicInteger();

        public Boolean markTrue() {
            invocations.incrementAndGet();
            return true;
        }

        public BigDecimal markNumber() {
            invocations.incrementAndGet();
            return BigDecimal.TEN;
        }

        public int invocations() {
            return invocations.get();
        }
    }

    public static final class WidgetFunctions {

        private final AtomicInteger invocations = new AtomicInteger();

        public Widget markWidget() {
            invocations.incrementAndGet();
            return new Widget("gadget");
        }

        public int invocations() {
            return invocations.get();
        }
    }

    public record Widget(String name) {
    }
}
