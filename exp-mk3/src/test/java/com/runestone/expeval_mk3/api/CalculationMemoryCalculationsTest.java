package com.runestone.expeval_mk3.api;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CalculationMemoryCalculationsTest {

    @Test
    void publishesAssignmentsBeforeNestedResultCallsInEvaluationOrder() {
        MarkingFunctions functions = new MarkingFunctions();
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functionsFrom(functions, FunctionPurity.IMPURE)
                .build();
        String source = "assigned := mark(1); mark(mark(2))";
        CompiledExpression expression = ExpressionEngine.defaultEngine().compileOrThrow(source, environment);

        CalculationMemory assignmentMemory = expression.asAssignments().computeWithMemory().memory();
        ComputationWithMemory<BigDecimal> computation = expression.asMath().computeWithMemory();
        CalculationMemory memory = computation.memory();

        assertThat(computation.result()).isEqualByComparingTo("2");
        assertThat(memory.calculations()).extracting(CalculationEntry::value)
                .containsExactly(new BigDecimal("1"), new BigDecimal("2"), new BigDecimal("2"));
        assertThat(memory.calculations()).extracting(entry -> entry.key().name())
                .containsExactly("mark", "mark", "mark");
        assertThat(memory.calculations()).extracting(entry -> spanText(source, entry.key().sourceSpan()))
                .containsExactly("mark(1)", "mark(2)", "mark(mark(2))");
        assertThat(memory.calculations()).extracting(entry -> entry.key().kind())
                .containsOnly(CalculationKind.FUNCTION);
        assertThat(memory.calculations()).extracting(entry -> entry.key().nodeId())
                .doesNotHaveDuplicates();
        assertThat(assignmentMemory.calculations()).hasSize(1);
        assertThat(assignmentMemory.calculationKeyAt(0)).isSameAs(memory.calculationKeyAt(0));
        assertThat(functions.values()).containsExactly(
                new BigDecimal("1"), new BigDecimal("1"), new BigDecimal("2"), new BigDecimal("2"));
    }

    @Test
    void preservesConditionalReachabilityAndUsesTheReachedKeyForLeadingGaps() {
        MarkingFunctions functions = new MarkingFunctions();
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("enabled", false, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .functionsFrom(functions, FunctionPurity.IMPURE)
                .build();
        CompiledExpression expression = ExpressionEngine.defaultEngine().compileOrThrow(
                "if enabled then mark(1) else mark(2) endif", environment);

        CalculationMemory disabled = expression.asMath().computeWithMemory().memory();
        CalculationMemory enabled = expression.asMath().computeWithMemory(Map.of("enabled", true)).memory();

        assertThat(disabled.calculations()).extracting(CalculationEntry::value)
                .containsExactly(new BigDecimal("2"));
        assertThat(enabled.calculations()).extracting(CalculationEntry::value)
                .containsExactly(new BigDecimal("1"));
        assertThat(disabled.calculationKeyAt(0).sourceSpan().offset())
                .isGreaterThan(enabled.calculationKeyAt(0).sourceSpan().offset());
    }

    @Test
    void shortCircuitOmitsTheUnreachedFunction() {
        MarkingFunctions functions = new MarkingFunctions();
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functionsFrom(functions, FunctionPurity.IMPURE)
                .build();
        CompiledExpression expression = ExpressionEngine.defaultEngine().compileOrThrow(
                "markBoolean(false) and markBoolean(true)", environment);

        ComputationWithMemory<Boolean> computation = expression.asLogical().computeWithMemory();

        assertThat(computation.result()).isFalse();
        assertThat(computation.memory().calculations()).extracting(CalculationEntry::value)
                .containsExactly(false);
    }

    @Test
    void nullCoalescenceCapturesOnlyAReachedFallback() {
        MarkingFunctions functions = new MarkingFunctions();
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functionsFrom(functions, FunctionPurity.IMPURE)
                .build();
        CompiledExpression fallback = ExpressionEngine.defaultEngine().compileOrThrow(
                "items := [1]; items?.[7] ?? mark(2)", environment);
        CompiledExpression first = ExpressionEngine.defaultEngine().compileOrThrow(
                "items := [1]; items?.[0] ?? mark(2)", environment);

        ComputationWithMemory<BigDecimal> fallbackResult = fallback.asMath().computeWithMemory();
        ComputationWithMemory<BigDecimal> firstResult = first.asMath().computeWithMemory();

        assertThat(fallbackResult.result()).isEqualByComparingTo("2");
        assertThat(fallbackResult.memory().calculations()).extracting(CalculationEntry::value)
                .containsExactly(new BigDecimal("2"));
        assertThat(firstResult.result()).isEqualByComparingTo("1");
        assertThat(firstResult.memory().calculations()).isEmpty();
    }

    @Test
    void capturesTheMaterializedCurrentTemporalValueWithItsCanonicalKey() {
        Clock clock = Clock.fixed(Instant.parse("2024-03-15T10:20:30.987654321Z"), ZoneOffset.UTC);
        ExpressionEngine engine = ExpressionEngine.builder().clock(clock).build();
        ExpressionEnvironment environment = ExpressionEnvironment.builder().zoneId(ZoneOffset.UTC).build();
        CompiledExpression expression = engine.compileOrThrow("currDateTime", environment);

        ComputationWithMemory<Object> computation = expression.asResult().computeWithMemory();
        CalculationMemory memory = computation.memory();

        assertThat(computation.result()).isEqualTo(LocalDateTime.of(2024, 3, 15, 10, 20, 30));
        assertThat(memory.calculationCount()).isOne();
        assertThat(memory.calculationKeyAt(0).sourceSpan()).isEqualTo(new SourceSpan(0, 12, 1, 1));
        assertThat(memory.calculationKeyAt(0).kind()).isEqualTo(CalculationKind.CURRENT_TEMPORAL);
        assertThat(memory.calculationKeyAt(0).name()).isEqualTo("currDateTime");
        assertThat(memory.calculationValueAt(0)).isSameAs(computation.result());
    }

    @Test
    void calculationProjectionIsImmutableStatelessAndSharesPrebuiltKeys() {
        MarkingFunctions functions = new MarkingFunctions();
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functionsFrom(functions, FunctionPurity.IMPURE)
                .build();
        CompiledExpression expression = ExpressionEngine.defaultEngine().compileOrThrow("mark(1)", environment);
        CalculationMemory first = expression.asMath().computeWithMemory().memory();
        CalculationMemory second = expression.asMath().computeWithMemory().memory();

        assertThat(first.calculationCount()).isOne();
        assertThat(first.calculationKeyAt(0)).isSameAs(second.calculationKeyAt(0));
        assertThat(first.calculations()).isNotSameAs(first.calculations());
        assertThat(first.calculations().getFirst()).isNotSameAs(first.calculations().getFirst());
        assertThatThrownBy(() -> first.calculations().add(first.calculations().getFirst()))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> first.calculationKeyAt(-1)).isInstanceOf(IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> first.calculationValueAt(1)).isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    void collectionOperationBodiesRemainOpaqueToCalculationMemory() {
        MarkingFunctions functions = new MarkingFunctions();
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functionsFrom(functions, FunctionPurity.IMPURE)
                .build();
        CompiledExpression expression = ExpressionEngine.defaultEngine().compileOrThrow(
                "items := [1, 2]; items.map(@ -> mark(@))", environment);

        ComputationWithMemory<Object> computation = expression.asResult().computeWithMemory();

        assertThat(computation.result()).isEqualTo(List.of(BigDecimal.ONE, new BigDecimal("2")));
        assertThat(functions.values()).containsExactly(BigDecimal.ONE, new BigDecimal("2"));
        assertThat(computation.memory().calculations()).isEmpty();
    }

    @Test
    void elidedIdentityAssertionDoesNotCreateACalculationPoint() {
        CompiledExpression expression = ExpressionEngine.defaultEngine().compileOrThrow(
                "asNumber(5)", ExpressionEnvironment.standard());

        ComputationWithMemory<BigDecimal> computation = expression.asMath().computeWithMemory();

        assertThat(computation.result()).isEqualByComparingTo("5");
        assertThat(computation.memory()).isSameAs(CalculationMemory.empty());
    }

    private static String spanText(String source, SourceSpan span) {
        return source.substring(span.offset(), span.endOffset());
    }

    public static final class MarkingFunctions {

        private final java.util.ArrayList<BigDecimal> values = new java.util.ArrayList<>();

        public BigDecimal mark(BigDecimal value) {
            values.add(value);
            return value;
        }

        public boolean markBoolean(boolean value) {
            return value;
        }

        List<BigDecimal> values() {
            return List.copyOf(values);
        }
    }
}
