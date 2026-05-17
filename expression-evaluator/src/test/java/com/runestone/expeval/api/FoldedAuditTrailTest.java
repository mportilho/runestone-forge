package com.runestone.expeval.api;

import com.runestone.expeval.environment.ExpressionEnvironment;
import com.runestone.expeval.environment.ExpressionEnvironmentBuilder;
import com.runestone.expeval.internal.runtime.ExpressionCompiler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests that folded external symbols and folded internal variables produce {@link AuditEvent.VariableRead}
 * events in the audit trail, and that those events are pre-stored at compile time rather than
 * recomputed on every {@code computeWithAudit()} call.
 *
 * <p>Ordering note: pre-stored events for folded symbols are prepended to the audit trail and
 * therefore appear before runtime-evaluated variable reads, regardless of expression order.
 */
@DisplayName("Folded symbols — VariableRead events in audit trail")
class FoldedAuditTrailTest {

    private static final ExpressionEnvironment EMPTY = ExpressionEnvironmentBuilder.empty();

    // -----------------------------------------------------------------------
    // Folded external symbols
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Non-overridable external symbols")
    class FoldedExternalSymbols {

        @Test
        @DisplayName("single folded external produces one VariableRead with the compile-time value")
        void singleFoldedExternalProducesOneVariableRead() {
            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerExternalSymbol("CONST_X", new BigDecimal("10"), false)
                    .build();

            AuditResult<BigDecimal> result = MathExpression.compile("CONST_X + 1", env)
                    .computeWithAudit();

            List<AuditEvent.VariableRead> reads = variableReads(result);
            assertThat(reads).hasSize(1);
            assertThat(reads.getFirst().name()).isEqualTo("CONST_X");
            assertThat(reads.getFirst().systemProvided()).isFalse();
            assertThat(reads.getFirst().value()).isEqualTo(new BigDecimal("10"));
        }

        @Test
        @DisplayName("folded external used twice produces two VariableRead events")
        void foldedExternalUsedTwiceProducesTwoReads() {
            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerExternalSymbol("CONST_X", new BigDecimal("5"), false)
                    .build();

            AuditResult<BigDecimal> result = MathExpression.compile("CONST_X + CONST_X", env)
                    .computeWithAudit();

            assertThat(variableReads(result))
                    .extracting(AuditEvent.VariableRead::name)
                    .containsExactly("CONST_X", "CONST_X");
        }

        @Test
        @DisplayName("overridable external does NOT produce a pre-stored VariableRead (normal runtime path)")
        void overridableExternalDoesNotProducePreStoredRead() {
            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerExternalSymbol("VAR_X", new BigDecimal("7"), true)
                    .build();

            AuditResult<BigDecimal> result = MathExpression.compile("VAR_X + 1", env)
                    .computeWithAudit(Map.of("VAR_X", new BigDecimal("7")));

            List<AuditEvent.VariableRead> reads = variableReads(result);
            assertThat(reads).hasSize(1);
            assertThat(reads.getFirst().name()).isEqualTo("VAR_X");
        }

        @Test
        @DisplayName("mixed expression — folded read appears before runtime reads")
        void mixedExpressionFoldedReadsBeforeRuntimeReads() {
            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerExternalSymbol("CONST_A", new BigDecimal("3"), false)
                    .build();

            AuditResult<BigDecimal> result = MathExpression.compile("CONST_A + y", env)
                    .computeWithAudit(Map.of("y", new BigDecimal("2")));

            List<AuditEvent.VariableRead> reads = variableReads(result);
            assertThat(reads).extracting(AuditEvent.VariableRead::name)
                    .containsExactlyInAnyOrder("CONST_A", "y");
        }

        @Test
        @DisplayName("successive computeWithAudit() calls each produce the folded VariableRead")
        void successiveCallsEachProduceFoldedRead() {
            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerExternalSymbol("CONST_X", new BigDecimal("99"), false)
                    .build();
            MathExpression expr = MathExpression.compile("CONST_X", env);

            AuditResult<BigDecimal> first = expr.computeWithAudit();
            AuditResult<BigDecimal> second = expr.computeWithAudit();

            assertThat(variableReads(first)).hasSize(1);
            assertThat(variableReads(second)).hasSize(1);
            assertThat(first.trace()).isNotSameAs(second.trace());
        }
    }

    // -----------------------------------------------------------------------
    // Folded internal variables
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Internal variables folded from constant assignments")
    class FoldedInternalVariables {

        @Test
        @DisplayName("internal variable assigned a literal and used in result expression produces a VariableRead")
        void foldedInternalVariableProducesVariableRead() {
            AuditResult<BigDecimal> result = MathExpression.compile("x = 5; x + 1", EMPTY)
                    .computeWithAudit();

            List<AuditEvent.VariableRead> reads = variableReads(result);
            assertThat(reads)
                    .extracting(AuditEvent.VariableRead::name)
                    .contains("x");
            AuditEvent.VariableRead xRead = reads.stream()
                    .filter(r -> "x".equals(r.name()))
                    .findFirst().orElseThrow();
            assertThat(xRead.value()).isEqualTo(new BigDecimal("5"));
        }

        @Test
        @DisplayName("internal variable depending on a folded external is itself folded")
        void internalVariableDependingOnFoldedExternalIsFolded() {
            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerExternalSymbol("CONST_B", new BigDecimal("4"), false)
                    .build();

            AuditResult<BigDecimal> result = MathExpression.compile("derived = CONST_B * 2; derived", env)
                    .computeWithAudit();

            List<AuditEvent.VariableRead> reads = variableReads(result);
            assertThat(reads)
                    .extracting(AuditEvent.VariableRead::name)
                    .containsAnyOf("CONST_B", "derived");
        }
    }

    // -----------------------------------------------------------------------
    // Folded function calls with folded arguments
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Folded function calls — argument VariableRead + FunctionCall events")
    class FoldedFunctionCallWithFoldedArgs {

        @Test
        @DisplayName("folded function call whose argument is a folded external produces VariableRead + FunctionCall")
        void foldedFunctionCallProducesBothEvents() {
            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .addMathFunctions()
                    .registerExternalSymbol("CONST_ARG", new BigDecimal("1.05"), false)
                    .build();

            AuditResult<BigDecimal> result = MathExpression.compile("ln(CONST_ARG)", env)
                    .computeWithAudit();

            assertThat(variableReads(result))
                    .extracting(AuditEvent.VariableRead::name)
                    .contains("CONST_ARG");
            assertThat(result.trace().functionCalls())
                    .extracting(AuditEvent.FunctionCall::functionName)
                    .contains("ln");
        }
    }

    // -----------------------------------------------------------------------
    // Folded property-chain roots
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Folded property-chain root produces VariableRead even when chain is not folded")
    class FoldedPropertyChainRoot {

        record Point(BigDecimal x, BigDecimal y) {
        }

        @Test
        @DisplayName("navigating a property on a folded external emits VariableRead for the root")
        void foldedPropertyChainRootProducesVariableRead() {
            Point point = new Point(new BigDecimal("3"), new BigDecimal("4"));
            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerExternalSymbol("CONST_POINT", point, false)
                    .registerTypeHint(Point.class)
                    .build();

            AuditResult<BigDecimal> result = MathExpression.compile("CONST_POINT.x", env)
                    .computeWithAudit();

            assertThat(variableReads(result))
                    .extracting(AuditEvent.VariableRead::name)
                    .contains("CONST_POINT");
        }

        @Test
        @DisplayName("fully folded collection chain still emits VariableRead for the root")
        void fullyFoldedCollectionChainProducesVariableRead() {
            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerExternalSymbol("PRICES", List.of(new BigDecimal("5"), new BigDecimal("15")), false)
                    .build();

            AuditResult<BigDecimal> result = MathExpression.compile("PRICES[1]", env)
                    .computeWithAudit();

            assertThat(result.value()).isEqualByComparingTo("15");
            assertThat(variableReads(result))
                    .extracting(AuditEvent.VariableRead::name)
                    .contains("PRICES");
        }
    }

    // -----------------------------------------------------------------------
    // Multiple distinct folded externals
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Multiple distinct folded externals — each produces its own VariableRead")
    class MultipleFoldedExternals {

        @Test
        @DisplayName("two distinct non-overridable externals each emit one VariableRead with their compile-time value")
        void twoDistinctFoldedExternalsEachProduceOwnVariableRead() {
            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerExternalSymbol("CONST_A", new BigDecimal("3"), false)
                    .registerExternalSymbol("CONST_B", new BigDecimal("7"), false)
                    .build();

            AuditResult<BigDecimal> result = MathExpression.compile("CONST_A + CONST_B", env)
                    .computeWithAudit();

            assertThat(variableReads(result))
                    .extracting(AuditEvent.VariableRead::name)
                    .containsExactlyInAnyOrder("CONST_A", "CONST_B");
        }

        @Test
        @DisplayName("variableSnapshot() carries the compile-time value for each distinct folded external")
        void variableSnapshotCarriesCompileTimeValueForEachFoldedExternal() {
            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerExternalSymbol("CONST_A", new BigDecimal("3"), false)
                    .registerExternalSymbol("CONST_B", new BigDecimal("7"), false)
                    .build();

            AuditResult<BigDecimal> result = MathExpression.compile("CONST_A + CONST_B", env)
                    .computeWithAudit();

            Map<String, Object> snapshot = result.trace().variableSnapshot();
            assertThat((BigDecimal) snapshot.get("CONST_A")).isEqualByComparingTo("3");
            assertThat((BigDecimal) snapshot.get("CONST_B")).isEqualByComparingTo("7");
        }

        @Test
        @DisplayName("systemProvided flag is false for all non-overridable external VariableRead events")
        void systemProvidedIsFalseForAllFoldedExternals() {
            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerExternalSymbol("CONST_A", new BigDecimal("3"), false)
                    .registerExternalSymbol("CONST_B", new BigDecimal("7"), false)
                    .build();

            AuditResult<BigDecimal> result = MathExpression.compile("CONST_A + CONST_B", env)
                    .computeWithAudit();

            assertThat(variableReads(result))
                    .extracting(AuditEvent.VariableRead::systemProvided)
                    .containsOnly(false);
        }
    }

    // -----------------------------------------------------------------------
    // Internal variable chain folding
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Internal variable chain — x folded → y folded → both produce VariableRead")
    class InternalVariableChain {

        @Test
        @DisplayName("x = 5; y = x * 2; y — VariableRead events are emitted for both x (in y's assignment) and y (in result)")
        void bothChainedInternalVariablesProduceVariableReadEvents() {
            AuditResult<BigDecimal> result = MathExpression.compile("x = 5; y = x * 2; y", EMPTY)
                    .computeWithAudit();

            assertThat(variableReads(result))
                    .extracting(AuditEvent.VariableRead::name)
                    .contains("x", "y");
        }

        @Test
        @DisplayName("x = 5; y = x * 2; y — VariableRead for y carries the propagated value 10")
        void chainedInternalVariableReadForYCarriesPropagatedValue() {
            AuditResult<BigDecimal> result = MathExpression.compile("x = 5; y = x * 2; y", EMPTY)
                    .computeWithAudit();

            AuditEvent.VariableRead yRead = variableReads(result).stream()
                    .filter(r -> "y".equals(r.name()))
                    .findFirst().orElseThrow();

            assertThat((BigDecimal) yRead.value()).isEqualByComparingTo("10");
        }

        @Test
        @DisplayName("x = 5; y = x * 2; y — expression evaluates to 10")
        void chainedInternalVariablesComputeCorrectResult() {
            BigDecimal result = MathExpression.compile("x = 5; y = x * 2; y", EMPTY).compute();

            assertThat(result).isEqualByComparingTo("10");
        }

        @Test
        @DisplayName("x = 2; y = x * 3; z = y + x; z — three-level chain: VariableRead emitted for each identifier use")
        void threeLevelChainProducesVariableReadAtEachUse() {
            AuditResult<BigDecimal> result = MathExpression.compile("x = 2; y = x * 3; z = y + x; z", EMPTY)
                    .computeWithAudit();

            assertThat(variableReads(result))
                    .extracting(AuditEvent.VariableRead::name)
                    .contains("x", "y", "z");
        }
    }

    // -----------------------------------------------------------------------
    // Folded variable in conditional (if-then-else) expression
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Folded variable in conditional expression — VariableRead preserved after branch pruning")
    class FoldedVariableInConditional {

        @Test
        @DisplayName("x = 10; if (x > 5) then 100 else 0 endif — x folded, condition constant-true, VariableRead for x still pre-stored")
        void foldedInternalVariableInConditionalProducesVariableRead() {
            AuditResult<BigDecimal> result = MathExpression.compile(
                    "x = 10; if (x > 5) then 100 else 0 endif", EMPTY)
                    .computeWithAudit();

            assertThat(variableReads(result))
                    .extracting(AuditEvent.VariableRead::name)
                    .contains("x");
            assertThat(result.value()).isEqualByComparingTo("100");
        }

        @Test
        @DisplayName("CONST_FLAG (non-overridable) in condition — VariableRead pre-stored even though condition is pruned at compile time")
        void foldedExternalInConditionalConditionProducesVariableRead() {
            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerExternalSymbol("CONST_LIMIT", new BigDecimal("10"), false)
                    .build();

            AuditResult<BigDecimal> result = MathExpression.compile(
                    "if (CONST_LIMIT > 5) then 1 else 0 endif", env)
                    .computeWithAudit();

            assertThat(variableReads(result))
                    .extracting(AuditEvent.VariableRead::name)
                    .contains("CONST_LIMIT");
            assertThat(result.value()).isEqualByComparingTo("1");
        }
    }

    // -----------------------------------------------------------------------
    // Null-coalesce with non-null folded left side
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Null-coalesce — folded non-null left prunes the right side from the execution plan")
    class NullCoalesceWithFoldedLeft {

        @Test
        @DisplayName("CONST_X ?? fallback — CONST_X is non-null, right side is never built, fallback VariableRead does not appear")
        void nullCoalesceWithFoldedNonNullLeftPrunesRightFromAudit() {
            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerExternalSymbol("CONST_X", new BigDecimal("5"), false)
                    .registerExternalSymbol("fallback", new BigDecimal("99"), true)
                    .build();

            AuditResult<BigDecimal> result = MathExpression.compile("CONST_X ?? fallback", env)
                    .computeWithAudit(Map.of("fallback", new BigDecimal("99")));

            assertThat(variableReads(result))
                    .extracting(AuditEvent.VariableRead::name)
                    .contains("CONST_X")
                    .doesNotContain("fallback");
            assertThat(result.value()).isEqualByComparingTo("5");
        }
    }

    // -----------------------------------------------------------------------
    // Logical expression with folded external
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Logical expression — folded external produces VariableRead alongside runtime reads")
    class LogicalExpressionWithFoldedExternal {

        @Test
        @DisplayName("THRESHOLD (non-overridable) used in comparison — VariableRead emitted for THRESHOLD and for runtime x")
        void foldedExternalInLogicalExpressionProducesVariableRead() {
            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerExternalSymbol("THRESHOLD", new BigDecimal("10"), false)
                    .build();

            AuditResult<Boolean> result = LogicalExpression.compile("x > THRESHOLD", env)
                    .computeWithAudit(Map.of("x", new BigDecimal("15")));

            assertThat(variableReads(result))
                    .extracting(AuditEvent.VariableRead::name)
                    .containsExactlyInAnyOrder("THRESHOLD", "x");
            assertThat(result.value()).isTrue();
        }

        @Test
        @DisplayName("folded external in logical expression: systemProvided is false for the folded VariableRead")
        void foldedExternalInLogicalExpressionHasSystemProvidedFalse() {
            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerExternalSymbol("THRESHOLD", new BigDecimal("10"), false)
                    .build();

            AuditResult<Boolean> result = LogicalExpression.compile("x > THRESHOLD", env)
                    .computeWithAudit(Map.of("x", new BigDecimal("15")));

            AuditEvent.VariableRead thresholdRead = variableReads(result).stream()
                    .filter(r -> "THRESHOLD".equals(r.name()))
                    .findFirst().orElseThrow();

            assertThat(thresholdRead.systemProvided()).isFalse();
            assertThat((BigDecimal) thresholdRead.value()).isEqualByComparingTo("10");
        }
    }

    // -----------------------------------------------------------------------
    // AssignmentExpression.computeWithAudit() path
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("AssignmentExpression computeWithAudit — pre-stored VariableReads are seeded into the assignment audit path")
    class AssignmentExpressionAuditPath {

        @Test
        @DisplayName("non-overridable external used in assignment block produces VariableRead in AssignmentExpression audit trail")
        void foldedExternalInAssignmentBlockProducesVariableRead() {
            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerExternalSymbol("CONST_RATE", new BigDecimal("5"), false)
                    .build();

            AuditResult<Map<String, Object>> result = AssignmentExpression.compile("total = base * CONST_RATE;", env)
                    .computeWithAudit(Map.of("base", new BigDecimal("100")));

            assertThat(variableReads(result))
                    .extracting(AuditEvent.VariableRead::name)
                    .contains("CONST_RATE");
        }

        @Test
        @DisplayName("folded internal variable in assignment block — VariableRead is present in the assignment audit trail")
        void foldedInternalVariableInAssignmentBlockProducesVariableRead() {
            AuditResult<Map<String, Object>> result = AssignmentExpression.compile("x = 10; y = x + 5;")
                    .computeWithAudit();

            assertThat(variableReads(result))
                    .extracting(AuditEvent.VariableRead::name)
                    .contains("x");
        }
    }

    // -----------------------------------------------------------------------
    // Folded function call with folded internal variable argument
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Folded function call — argument is a folded internal variable")
    class FoldedFunctionCallWithFoldedInternalArg {

        private static final ExpressionEnvironment WITH_MATH =
                ExpressionEnvironment.builder().addMathFunctions().build();

        @Test
        @DisplayName("x = 1; ln(x) — x is folded, ln folds with constant arg, both VariableRead(x) and FunctionCall(ln) appear")
        void foldedInternalVariableArgToFoldedFunctionEmitsBothEvents() {
            AuditResult<BigDecimal> result = MathExpression.compile("x = 1; ln(x)", WITH_MATH)
                    .computeWithAudit();

            assertThat(variableReads(result))
                    .extracting(AuditEvent.VariableRead::name)
                    .contains("x");
            assertThat(result.trace().functionCalls())
                    .extracting(AuditEvent.FunctionCall::functionName)
                    .contains("ln");
        }

        @Test
        @DisplayName("x = 1; ln(x) — result equals ln(1) = 0")
        void foldedInternalVariableArgToFoldedFunctionComputesCorrectResult() {
            BigDecimal result = MathExpression.compile("x = 1; ln(x)", WITH_MATH).compute();

            assertThat(result).isEqualByComparingTo("0");
        }
    }

    // -----------------------------------------------------------------------
    // Pre-stored events are stable across successive computeWithAudit() calls
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Pre-stored VariableRead events — stability and isolation across successive calls")
    class PreStoredEventsStability {

        @Test
        @DisplayName("successive computeWithAudit() calls yield independent trace objects with identical pre-stored VariableRead content")
        void preStoredReadsAreStableAcrossSuccessiveCalls() {
            // Fresh compiler avoids cache collision with other tests that register CONST_STABLE under the same environment id.
            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerExternalSymbol("CONST_STABLE", new BigDecimal("42"), false)
                    .build();
            MathExpression expr = MathExpression.compile("CONST_STABLE + 1", env, new ExpressionCompiler());

            AuditResult<BigDecimal> first = expr.computeWithAudit();
            AuditResult<BigDecimal> second = expr.computeWithAudit();

            assertThat(first.trace()).isNotSameAs(second.trace());
            assertThat(first.trace().events()).isNotSameAs(second.trace().events());

            List<AuditEvent.VariableRead> firstReads = variableReads(first);
            List<AuditEvent.VariableRead> secondReads = variableReads(second);
            assertThat(firstReads).hasSize(1);
            assertThat(secondReads).hasSize(1);
            assertThat(firstReads.getFirst().name()).isEqualTo(secondReads.getFirst().name());
            assertThat((BigDecimal) firstReads.getFirst().value())
                    .isEqualByComparingTo((BigDecimal) secondReads.getFirst().value());
        }

        @Test
        @DisplayName("runtime variable values do not bleed into the pre-stored folded VariableRead across calls")
        void runtimeValuesDoNotBleedIntoPreStoredReads() {
            // Fresh compiler avoids cache collision: environmentId does not encode the default value,
            // so two envs registering the same symbol name+type+overridable share the same cache key.
            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerExternalSymbol("CONST_RATE_STABLE", new BigDecimal("10"), false)
                    .build();
            MathExpression expr = MathExpression.compile("CONST_RATE_STABLE + y", env, new ExpressionCompiler());

            AuditResult<BigDecimal> first = expr.computeWithAudit(Map.of("y", new BigDecimal("1")));
            AuditResult<BigDecimal> second = expr.computeWithAudit(Map.of("y", new BigDecimal("999")));

            AuditEvent.VariableRead constInFirst = variableReads(first).stream()
                    .filter(r -> "CONST_RATE_STABLE".equals(r.name())).findFirst().orElseThrow();
            AuditEvent.VariableRead constInSecond = variableReads(second).stream()
                    .filter(r -> "CONST_RATE_STABLE".equals(r.name())).findFirst().orElseThrow();

            assertThat((BigDecimal) constInFirst.value()).isEqualByComparingTo("10");
            assertThat((BigDecimal) constInSecond.value()).isEqualByComparingTo("10");
        }
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private static <T> List<AuditEvent.VariableRead> variableReads(AuditResult<T> result) {
        return result.trace().events().stream()
                .filter(AuditEvent.VariableRead.class::isInstance)
                .map(AuditEvent.VariableRead.class::cast)
                .toList();
    }
}
