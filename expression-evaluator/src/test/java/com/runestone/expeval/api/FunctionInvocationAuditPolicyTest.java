package com.runestone.expeval.api;

import com.runestone.expeval.api.support.TestAuditFunctions;
import com.runestone.expeval.api.support.TestCollectionFunctions;
import com.runestone.expeval.environment.ExpressionEnvironment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Function invocation audit policy")
class FunctionInvocationAuditPolicyTest {

    private static final List<BigDecimal> PRICES = List.of(
            new BigDecimal("5"),
            new BigDecimal("15"),
            new BigDecimal("25"),
            new BigDecimal("10")
    );

    @Test
    @DisplayName("computeWithAudit() records zero-arity catalog function calls")
    void computeWithAuditRecordsZeroArityFunctionCall() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .registerStaticProvider(TestAuditFunctions.class)
                .build();

        AuditResult<BigDecimal> result = MathExpression.compile("zero()", environment).computeWithAudit();

        assertThat(result.value()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.trace().functionCalls())
                .singleElement()
                .satisfies(call -> {
                    assertThat(call.functionName()).isEqualTo("zero");
                    assertThat(call.inputArgs()).isEmpty();
                    assertThat((BigDecimal) call.result()).isEqualByComparingTo(BigDecimal.ZERO);
                });
    }

    @Test
    @DisplayName("computeWithAudit() records collection function calls with the implicit collection argument")
    void computeWithAuditRecordsCollectionFunctionWithImplicitArgument() {
        ExpressionEnvironment environment = collectionEnvironment();
        MathExpression expression = MathExpression.compile("prices..distinctCount()", environment);

        assertThat(expression.compute(Map.of("prices", PRICES))).isEqualByComparingTo("4");

        AuditResult<BigDecimal> result = expression.computeWithAudit(Map.of("prices", PRICES));

        assertThat(result.value()).isEqualByComparingTo("4");
        assertThat(result.trace().functionCalls())
                .singleElement()
                .satisfies(call -> {
                    assertThat(call.functionName()).isEqualTo("distinctCount");
                    assertThat(call.inputArgs()).containsExactly(PRICES);
                    assertThat((BigDecimal) call.result()).isEqualByComparingTo("4");
                });
    }

    @Test
    @DisplayName("collection function audit records coerced extra arguments after evaluating them")
    void collectionFunctionAuditRecordsExtraArguments() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .registerStaticProvider(TestCollectionFunctions.class)
                .registerExternalSymbol("prices", PRICES, true)
                .registerExternalSymbol("threshold", BigDecimal.TEN, true)
                .build();
        MathExpression expression = MathExpression.compile("prices..countAbove(threshold)", environment);
        Map<String, Object> values = Map.of("prices", PRICES, "threshold", BigDecimal.TEN);

        assertThat(expression.compute(values)).isEqualByComparingTo("2");

        AuditResult<BigDecimal> result = expression.computeWithAudit(values);
        AuditEvent.FunctionCall call = result.trace().functionCalls().getFirst();

        assertThat(result.value()).isEqualByComparingTo("2");
        assertThat(call.functionName()).isEqualTo("countAbove");
        assertThat(call.inputArgs()).containsExactly(PRICES, BigDecimal.TEN);
        assertThat((BigDecimal) call.result()).isEqualByComparingTo("2");
        assertThat(result.trace().events())
                .extracting(event -> switch (event) {
                    case AuditEvent.VariableRead read -> "read:" + read.name();
                    case AuditEvent.FunctionCall functionCall -> "call:" + functionCall.functionName();
                    case AuditEvent.AssignmentEvent assignment -> "assign:" + assignment.targetName();
                })
                .containsExactly("read:prices", "read:threshold", "call:countAbove");
    }

    @Test
    @DisplayName("folded collection functions still emit FunctionCall audit events")
    void foldedCollectionFunctionStillEmitsFunctionCallAuditEvent() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .registerStaticProvider(TestCollectionFunctions.class, true)
                .registerExternalSymbol("PRICES", PRICES, false)
                .build();

        AuditResult<BigDecimal> result = MathExpression.compile("PRICES..distinctCount()", environment, new ExpressionEngine())
                .computeWithAudit();

        assertThat(result.value()).isEqualByComparingTo("4");
        assertThat(result.trace().events())
                .extracting(event -> switch (event) {
                    case AuditEvent.VariableRead read -> "read:" + read.name();
                    case AuditEvent.FunctionCall functionCall -> "call:" + functionCall.functionName();
                    case AuditEvent.AssignmentEvent assignment -> "assign:" + assignment.targetName();
                })
                .containsExactly("read:PRICES", "call:distinctCount");
        assertThat(result.trace().functionCalls())
                .singleElement()
                .satisfies(call -> assertThat(call.inputArgs()).containsExactly(PRICES));
    }

    private static ExpressionEnvironment collectionEnvironment() {
        return ExpressionEnvironment.builder()
                .registerStaticProvider(TestCollectionFunctions.class)
                .registerExternalSymbol("prices", PRICES, true)
                .build();
    }
}
