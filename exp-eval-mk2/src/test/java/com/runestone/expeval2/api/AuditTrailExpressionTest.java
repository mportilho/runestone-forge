package com.runestone.expeval2.api;

import com.runestone.expeval2.environment.ExpressionEnvironment;
import com.runestone.expeval2.environment.ExpressionEnvironmentBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the audit trail feature: {@link MathExpression#computeWithAudit()} and
 * {@link LogicalExpression#computeWithAudit()}.
 *
 * <p>Skipped categories:
 * <ul>
 *   <li>Null / Invalid input — compile-time and runtime error paths are covered by the existing
 *       expression engine tests; {@code computeWithAudit()} adds no new error paths.
 *   <li>Concurrency — evaluation is single-threaded per scope by design.
 * </ul>
 */
@DisplayName("Audit trail — computeWithAudit()")
class AuditTrailExpressionTest {

    private static final ExpressionEnvironment EMPTY = ExpressionEnvironmentBuilder.empty();
    private static final ExpressionEnvironment WITH_MATH =
            ExpressionEnvironment.builder().addMathFunctions().build();

    // -----------------------------------------------------------------------
    // Audit result integrity
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Audit result integrity")
    class AuditResultIntegrity {

        @Test
        @DisplayName("computeWithAudit() returns the same numeric value as compute()")
        void mathComputeWithAuditReturnsSameValueAsCompute() {
            MathExpression expr = MathExpression.compile("x + y", EMPTY)
                    .setValue("x", new BigDecimal("3"))
                    .setValue("y", new BigDecimal("7"));

            assertThat(expr.computeWithAudit().value())
                    .isEqualByComparingTo(expr.compute());
        }

        @Test
        @DisplayName("computeWithAudit() returns the same boolean value as compute()")
        void logicalComputeWithAuditReturnsSameValueAsCompute() {
            LogicalExpression expr = LogicalExpression.compile("x > 0", EMPTY)
                    .setValue("x", new BigDecimal("5"));

            assertThat(expr.computeWithAudit().value())
                    .isEqualTo(expr.compute());
        }

        @Test
        @DisplayName("evaluationTime in the trace is non-negative")
        void evaluationTimeIsNonNegative() {
            AuditResult<BigDecimal> result = MathExpression.compile("x + 1", EMPTY)
                    .setValue("x", BigDecimal.ONE)
                    .computeWithAudit();

            assertThat(result.trace().evaluationTime().toNanos()).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("successive calls on the same expression produce independent trace instances")
        void subsequentCallsProduceIndependentTraces() {
            MathExpression expr = MathExpression.compile("x", EMPTY)
                    .setValue("x", BigDecimal.TEN);

            AuditResult<BigDecimal> first = expr.computeWithAudit();
            AuditResult<BigDecimal> second = expr.computeWithAudit();

            assertThat(first.trace()).isNotSameAs(second.trace());
            assertThat(first.trace().events()).isNotSameAs(second.trace().events());
        }
    }

    // -----------------------------------------------------------------------
    // VariableRead events
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("VariableRead events — user-supplied variables")
    class VariableReadEvents {

        @Test
        @DisplayName("a single variable emits exactly one VariableRead event with the correct fields")
        void singleVariableEmitsOneVariableReadEvent() {
            AuditResult<BigDecimal> result = MathExpression.compile("x", EMPTY)
                    .setValue("x", new BigDecimal("42"))
                    .computeWithAudit();

            assertThat(result.trace().events())
                    .singleElement()
                    .isInstanceOfSatisfying(AuditEvent.VariableRead.class, e -> {
                        assertThat(e.name()).isEqualTo("x");
                        assertThat(e.systemProvided()).isFalse();
                        assertThat(e.value()).isEqualTo(new BigDecimal("42"));
                    });
        }

        @Test
        @DisplayName("two distinct variables each emit one VariableRead event in left-to-right order")
        void multipleDistinctVariablesEachEmitOneEventInOrder() {
            AuditResult<BigDecimal> result = MathExpression.compile("x + y", EMPTY)
                    .setValue("x", new BigDecimal("3"))
                    .setValue("y", new BigDecimal("7"))
                    .computeWithAudit();

            List<AuditEvent.VariableRead> reads = result.trace().events().stream()
                    .filter(AuditEvent.VariableRead.class::isInstance)
                    .map(AuditEvent.VariableRead.class::cast)
                    .toList();

            assertThat(reads)
                    .extracting(AuditEvent.VariableRead::name)
                    .containsExactly("x", "y");
        }

        @Test
        @DisplayName("same variable read twice in one expression emits two VariableRead events")
        void sameVariableReadTwiceEmitsTwoEvents() {
            AuditResult<BigDecimal> result = MathExpression.compile("x + x", EMPTY)
                    .setValue("x", new BigDecimal("5"))
                    .computeWithAudit();

            long readCount = result.trace().events().stream()
                    .filter(AuditEvent.VariableRead.class::isInstance)
                    .count();

            assertThat(readCount).isEqualTo(2);
        }

        @Test
        @DisplayName("variableSnapshot contains all variable names mapped to their resolved values")
        void variableSnapshotContainsAllVariablesWithTheirValues() {
            AuditResult<BigDecimal> result = MathExpression.compile("a + b", EMPTY)
                    .setValue("a", new BigDecimal("10"))
                    .setValue("b", new BigDecimal("20"))
                    .computeWithAudit();

            assertThat(result.trace().variableSnapshot())
                    .containsEntry("a", new BigDecimal("10"))
                    .containsEntry("b", new BigDecimal("20"));
        }

        @Test
        @DisplayName("variableSnapshot has one entry per name even when the variable is read twice")
        void variableSnapshotHasOneEntryPerVariableName() {
            AuditResult<BigDecimal> result = MathExpression.compile("x + x", EMPTY)
                    .setValue("x", new BigDecimal("9"))
                    .computeWithAudit();

            assertThat(result.trace().variableSnapshot())
                    .hasSize(1)
                    .containsEntry("x", new BigDecimal("9"));
        }

        @Test
        @DisplayName("expression with only literal operands produces no VariableRead events")
        void expressionWithOnlyLiteralsProducesNoVariableReadEvents() {
            AuditResult<BigDecimal> result = MathExpression.compile("1 + 2", EMPTY)
                    .computeWithAudit();

            assertThat(result.trace().events())
                    .filteredOn(AuditEvent.VariableRead.class::isInstance)
                    .isEmpty();
        }
    }

    // -----------------------------------------------------------------------
    // FunctionCall events
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("FunctionCall events — catalog functions")
    class FunctionCallEvents {

        @Test
        @DisplayName("a single function call emits exactly one FunctionCall event")
        void singleFunctionCallEmitsOneFunctionCallEvent() {
            AuditResult<BigDecimal> result = MathExpression.compile("mean([2, 4])", WITH_MATH)
                    .computeWithAudit();

            assertThat(result.trace().functionCalls()).hasSize(1);
        }

        @Test
        @DisplayName("FunctionCall event carries the function name, input arguments, raw result, and call depth")
        void functionCallEventContainsNameInputsAndResult() {
            AuditResult<BigDecimal> result = MathExpression.compile("mean([2, 4])", WITH_MATH)
                    .computeWithAudit();

            AuditEvent.FunctionCall call = result.trace().functionCalls().getFirst();
            assertThat(call.functionName()).isEqualTo("mean");
            assertThat(call.inputArgs()).isNotEmpty();
            assertThat((BigDecimal) call.result()).isEqualByComparingTo("3");
            assertThat(call.callDepth()).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("multiple function calls are emitted in left-to-right evaluation order")
        void multipleFunctionCallsAreEmittedInEvaluationOrder() {
            // mean([4,6])=5 is evaluated before mean([1,2,3])=2
            AuditResult<BigDecimal> result = MathExpression.compile("mean([4, 6]) + mean([1, 2, 3])", WITH_MATH)
                    .computeWithAudit();

            List<AuditEvent.FunctionCall> calls = result.trace().functionCalls();
            assertThat(calls)
                    .hasSize(2)
                    .extracting(AuditEvent.FunctionCall::functionName)
                    .containsExactly("mean", "mean");
            assertThat((BigDecimal) calls.get(0).result()).isEqualByComparingTo("5");
            assertThat((BigDecimal) calls.get(1).result()).isEqualByComparingTo("2");
        }

        @Test
        @DisplayName("pure arithmetic expression produces no FunctionCall events")
        void expressionWithNoFunctionCallsHasEmptyFunctionCallsList() {
            AuditResult<BigDecimal> result = MathExpression.compile("x * 2 + 1", EMPTY)
                    .setValue("x", new BigDecimal("3"))
                    .computeWithAudit();

            assertThat(result.trace().functionCalls()).isEmpty();
        }
    }

    // -----------------------------------------------------------------------
    // Dynamic literal events (currDate, currTime, currDateTime)
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Dynamic literal events — currDate, currTime, currDateTime")
    class DynamicLiteralEvents {

        @Test
        @DisplayName("currDate emits a VariableRead with name 'currDate', systemProvided=true, and a LocalDate value")
        void currDateEmitsVariableReadWithSystemProvidedTrue() {
            AuditResult<Boolean> result = LogicalExpression.compile("currDate = currDate", EMPTY)
                    .computeWithAudit();

            AuditEvent.VariableRead first = result.trace().events().stream()
                    .filter(AuditEvent.VariableRead.class::isInstance)
                    .map(AuditEvent.VariableRead.class::cast)
                    .findFirst().orElseThrow();

            assertThat(first.name()).isEqualTo("currDate");
            assertThat(first.systemProvided()).isTrue();
            assertThat(first.value()).isInstanceOf(LocalDate.class);
        }

        @Test
        @DisplayName("currTime emits a VariableRead with name 'currTime', systemProvided=true, and a LocalTime value")
        void currTimeEmitsVariableReadWithSystemProvidedTrue() {
            AuditResult<Boolean> result = LogicalExpression.compile("currTime = currTime", EMPTY)
                    .computeWithAudit();

            AuditEvent.VariableRead first = result.trace().events().stream()
                    .filter(AuditEvent.VariableRead.class::isInstance)
                    .map(AuditEvent.VariableRead.class::cast)
                    .findFirst().orElseThrow();

            assertThat(first.name()).isEqualTo("currTime");
            assertThat(first.systemProvided()).isTrue();
            assertThat(first.value()).isInstanceOf(LocalTime.class);
        }

        @Test
        @DisplayName("currDateTime emits a VariableRead with name 'currDateTime', systemProvided=true, and a LocalDateTime value")
        void currDateTimeEmitsVariableReadWithSystemProvidedTrue() {
            AuditResult<Boolean> result = LogicalExpression.compile("currDateTime = currDateTime", EMPTY)
                    .computeWithAudit();

            AuditEvent.VariableRead first = result.trace().events().stream()
                    .filter(AuditEvent.VariableRead.class::isInstance)
                    .map(AuditEvent.VariableRead.class::cast)
                    .findFirst().orElseThrow();

            assertThat(first.name()).isEqualTo("currDateTime");
            assertThat(first.systemProvided()).isTrue();
            assertThat(first.value()).isInstanceOf(LocalDateTime.class);
        }

        @Test
        @DisplayName("dynamic literal appears in variableSnapshot with a non-null value")
        void dynamicLiteralAppearsInVariableSnapshot() {
            AuditResult<Boolean> result = LogicalExpression.compile("currDate = currDate", EMPTY)
                    .computeWithAudit();

            assertThat(result.trace().variableSnapshot())
                    .containsKey("currDate")
                    .extractingByKey("currDate").isInstanceOf(LocalDate.class);
        }

        @Test
        @DisplayName("dynamic literal and user variable coexist in variableSnapshot")
        void dynamicLiteralAndUserVariableBothAppearInVariableSnapshot() {
            AuditResult<Boolean> result = LogicalExpression.compile("age > 0 and currDate = currDate", EMPTY)
                    .setValue("age", new BigDecimal("25"))
                    .computeWithAudit();

            assertThat(result.trace().variableSnapshot())
                    .containsKeys("age", "currDate");
        }

        @Test
        @DisplayName("same dynamic literal referenced twice emits two events whose values are the same instance")
        void dynamicLiteralReadTwiceEmitsTwoEventsWithSameValueInstance() {
            // The scope caches the resolved value so both events reference the identical LocalDate object
            AuditResult<Boolean> result = LogicalExpression.compile("currDate = currDate", EMPTY)
                    .computeWithAudit();

            List<Object> values = result.trace().events().stream()
                    .filter(AuditEvent.VariableRead.class::isInstance)
                    .map(AuditEvent.VariableRead.class::cast)
                    .filter(e -> "currDate".equals(e.name()))
                    .map(AuditEvent.VariableRead::value)
                    .toList();

            assertThat(values).hasSize(2);
            assertThat(values.get(0)).isSameAs(values.get(1));
        }
    }

    // -----------------------------------------------------------------------
    // AssignmentEvent events
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("AssignmentEvent events — variable assignments")
    class AssignmentEvents {

        @Test
        @DisplayName("simple assignment emits one AssignmentEvent with the target name and assigned value")
        void simpleAssignmentEmitsOneAssignmentEvent() {
            AuditResult<BigDecimal> result = MathExpression.compile("fee = 10; fee + principal",
                            ExpressionEnvironment.builder().build())
                    .setValue("principal", new BigDecimal("90"))
                    .computeWithAudit();

            List<AuditEvent.AssignmentEvent> assignments = result.trace().events().stream()
                    .filter(AuditEvent.AssignmentEvent.class::isInstance)
                    .map(AuditEvent.AssignmentEvent.class::cast)
                    .toList();

            assertThat(assignments)
                    .singleElement()
                    .satisfies(e -> {
                        assertThat(e.targetName()).isEqualTo("fee");
                        assertThat((BigDecimal) e.newValue()).isEqualByComparingTo("10");
                    });
        }

        @Test
        @DisplayName("destructuring assignment emits one AssignmentEvent per target variable in declaration order")
        void destructuringAssignmentEmitsOneEventPerTarget() {
            AuditResult<BigDecimal> result = MathExpression.compile("[left, right] = [10, 20]; left + right",
                            ExpressionEnvironment.builder().build())
                    .computeWithAudit();

            List<AuditEvent.AssignmentEvent> assignments = result.trace().events().stream()
                    .filter(AuditEvent.AssignmentEvent.class::isInstance)
                    .map(AuditEvent.AssignmentEvent.class::cast)
                    .toList();

            assertThat(assignments)
                    .hasSize(2)
                    .extracting(AuditEvent.AssignmentEvent::targetName)
                    .containsExactly("left", "right");
            assertThat((BigDecimal) assignments.get(0).newValue()).isEqualByComparingTo("10");
            assertThat((BigDecimal) assignments.get(1).newValue()).isEqualByComparingTo("20");
        }
    }

    // -----------------------------------------------------------------------
    // Destructuring assignment audit — spread() as RHS
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Destructuring assignment audit — spread() as RHS")
    class DestructuringAssignmentAudit {

        @Test
        @DisplayName("canonical example emits one AssignmentEvent per target with spread-computed values")
        void canonicalSpreadExampleEmitsAssignmentEventsWithCorrectValues() {
            // spread(100, -1, [100, 10, 40]): total=150, direction<0
            // proportional slots (scale 0, HALF_EVEN): [67, 7, 27], sum=101, diff=-1
            // backward adjustment on last non-zero ref (index 2): 27-1=26 → [67, 7, 26]
            AuditResult<BigDecimal> result = MathExpression.compile(
                            "[pagtoPrincipal, pagtoCorrecao, pagtoJuros] = spread(100, -1, [100, 10, 40]); 0",
                            WITH_MATH)
                    .computeWithAudit();

            List<AuditEvent.AssignmentEvent> assignments = result.trace().events().stream()
                    .filter(AuditEvent.AssignmentEvent.class::isInstance)
                    .map(AuditEvent.AssignmentEvent.class::cast)
                    .toList();

            assertThat(assignments)
                    .hasSize(3)
                    .extracting(AuditEvent.AssignmentEvent::targetName)
                    .containsExactly("pagtoPrincipal", "pagtoCorrecao", "pagtoJuros");
            assertThat((BigDecimal) assignments.get(0).newValue()).isEqualByComparingTo("67");
            assertThat((BigDecimal) assignments.get(1).newValue()).isEqualByComparingTo("7");
            assertThat((BigDecimal) assignments.get(2).newValue()).isEqualByComparingTo("26");
        }

        @Test
        @DisplayName("FunctionCall(spread) appears before AssignmentEvents in the event stream")
        void spreadFunctionCallPrecedesAssignmentEventsInStream() {
            AuditResult<BigDecimal> result = MathExpression.compile(
                            "[a, b, c] = spread(100, -1, [100, 10, 40]); 0", WITH_MATH)
                    .computeWithAudit();

            List<AuditEvent> events = result.trace().events();
            int spreadIndex = -1;
            int firstAssignIndex = -1;
            for (int i = 0; i < events.size(); i++) {
                AuditEvent e = events.get(i);
                if (e instanceof AuditEvent.FunctionCall fc && "spread".equals(fc.functionName())) {
                    spreadIndex = i;
                } else if (e instanceof AuditEvent.AssignmentEvent && firstAssignIndex == -1) {
                    firstAssignIndex = i;
                }
            }

            assertThat(spreadIndex).as("FunctionCall(spread) must appear in the event stream").isGreaterThanOrEqualTo(0);
            assertThat(firstAssignIndex).as("at least one AssignmentEvent must appear").isGreaterThanOrEqualTo(0);
            assertThat(spreadIndex).isLessThan(firstAssignIndex);
        }

        @Test
        @DisplayName("destructured vars used in result expression appear in variableSnapshot()")
        void destructuredVarsUsedInResultAppearInVariableSnapshot() {
            // spread(100, 1, [1, 3]) → [25, 75]; then a+b is evaluated, producing VariableRead events
            AuditResult<BigDecimal> result = MathExpression.compile(
                            "[a, b] = spread(100, 1, [1, 3]); a + b", WITH_MATH)
                    .computeWithAudit();

            Map<String, Object> snapshot = result.trace().variableSnapshot();
            assertThat(snapshot).containsKeys("a", "b");
            assertThat((BigDecimal) snapshot.get("a")).isEqualByComparingTo("25");
            assertThat((BigDecimal) snapshot.get("b")).isEqualByComparingTo("75");
        }

        @Test
        @DisplayName("destructured vars NOT used in result still appear in variableSnapshot() via AssignmentEvent")
        void destructuredVarsUnusedInResultStillAppearInVariableSnapshot() {
            // vars are assigned but never read; variableSnapshot() now also collects AssignmentEvents
            AuditResult<BigDecimal> result = MathExpression.compile(
                            "[pagtoPrincipal, pagtoCorrecao, pagtoJuros] = spread(100, -1, [100, 10, 40]); 0",
                            WITH_MATH)
                    .computeWithAudit();

            Map<String, Object> snapshot = result.trace().variableSnapshot();
            assertThat(snapshot).containsKeys("pagtoPrincipal", "pagtoCorrecao", "pagtoJuros");
            assertThat((BigDecimal) snapshot.get("pagtoPrincipal")).isEqualByComparingTo("67");
            assertThat((BigDecimal) snapshot.get("pagtoCorrecao")).isEqualByComparingTo("7");
            assertThat((BigDecimal) snapshot.get("pagtoJuros")).isEqualByComparingTo("26");
        }
    }

    // -----------------------------------------------------------------------
    // Short-circuit evaluation
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Short-circuit evaluation — audit respects skipped branches")
    class ShortCircuitEvaluation {

        @Test
        @DisplayName("AND short-circuit does not emit a VariableRead for the right operand when left is false")
        void andShortCircuitSkipsRightVariableWhenLeftIsFalse() {
            // x < 0 evaluates to false (x=1), so y on the right is never read
            AuditResult<Boolean> result = LogicalExpression.compile("x < 0 and y > 0", EMPTY)
                    .setValue("x", new BigDecimal("1"))
                    .setValue("y", new BigDecimal("10"))
                    .computeWithAudit();

            assertThat(result.value()).isFalse();
            List<String> readNames = result.trace().events().stream()
                    .filter(AuditEvent.VariableRead.class::isInstance)
                    .map(AuditEvent.VariableRead.class::cast)
                    .map(AuditEvent.VariableRead::name)
                    .toList();
            assertThat(readNames).contains("x").doesNotContain("y");
        }

        @Test
        @DisplayName("OR short-circuit does not emit a VariableRead for the right operand when left is true")
        void orShortCircuitSkipsRightVariableWhenLeftIsTrue() {
            // x > 0 evaluates to true (x=5), so y on the right is never read
            AuditResult<Boolean> result = LogicalExpression.compile("x > 0 or y > 0", EMPTY)
                    .setValue("x", new BigDecimal("5"))
                    .setValue("y", new BigDecimal("10"))
                    .computeWithAudit();

            assertThat(result.value()).isTrue();
            List<String> readNames = result.trace().events().stream()
                    .filter(AuditEvent.VariableRead.class::isInstance)
                    .map(AuditEvent.VariableRead.class::cast)
                    .map(AuditEvent.VariableRead::name)
                    .toList();
            assertThat(readNames).contains("x").doesNotContain("y");
        }

        @Test
        @DisplayName("AND does emit a VariableRead for the right operand when left is true")
        void andEvaluatesRightVariableWhenLeftIsTrue() {
            // x > 0 is true (x=5), so y is also read
            AuditResult<Boolean> result = LogicalExpression.compile("x > 0 and y > 0", EMPTY)
                    .setValue("x", new BigDecimal("5"))
                    .setValue("y", new BigDecimal("10"))
                    .computeWithAudit();

            List<String> readNames = result.trace().events().stream()
                    .filter(AuditEvent.VariableRead.class::isInstance)
                    .map(AuditEvent.VariableRead.class::cast)
                    .map(AuditEvent.VariableRead::name)
                    .toList();
            assertThat(readNames).contains("x", "y");
        }
    }
}
