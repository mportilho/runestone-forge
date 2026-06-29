package com.runestone.expeval.api;

import com.runestone.expeval.environment.ExpressionEnvironment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Object navigation member binding")
class ObjectNavigationMemberBindingTest {

    @Test
    @DisplayName("type-hinted navigation binds nested properties and selected method overload")
    void shouldUseTypeHintedPropertyNavigationAndSelectedMethodOverload() {
        BindingRoot subject = new BindingRoot(new OverloadedMembers(), BigDecimal.TEN);
        ExpressionEnvironment environment = environment(subject);

        BigDecimal result = MathExpression.compile("subject.members.value(\"abcd\") + subject.amount", environment)
                .compute(Map.of("subject", subject));

        assertThat(result).isEqualByComparingTo("14");
    }

    @Test
    @DisplayName("method overload binding uses argument types")
    void shouldSelectCompatibleMethodOverloadByArgumentType() {
        BindingRoot subject = new BindingRoot(new OverloadedMembers(), BigDecimal.ZERO);
        ExpressionEnvironment environment = environment(subject);

        BigDecimal result = MathExpression.compile("subject.members.value(2)", environment)
                .compute(Map.of("subject", subject));

        assertThat(result).isEqualByComparingTo("3");
    }

    @Test
    @DisplayName("method binding reports arity mismatches at compile time")
    void shouldRejectMethodArityMismatchAtCompileTime() {
        BindingRoot subject = new BindingRoot(new OverloadedMembers(), BigDecimal.ZERO);
        ExpressionEnvironment environment = environment(subject);

        assertCompileIssue("subject.members.numericOnly(1, 2)", environment, IssueCode.INVALID_METHOD_ARITY);
    }

    @Test
    @DisplayName("method binding reports incompatible arguments at compile time")
    void shouldRejectIncompatibleMethodArgumentsAtCompileTime() {
        BindingRoot subject = new BindingRoot(new OverloadedMembers(), BigDecimal.ZERO);
        ExpressionEnvironment environment = environment(subject);

        assertCompileIssue("subject.members.numericOnly(\"bad\")", environment, IssueCode.INCOMPATIBLE_METHOD_ARGUMENTS);
    }

    @Test
    @DisplayName("method binding reports ambiguous overloads at compile time")
    void shouldRejectAmbiguousMethodOverloadAtCompileTime() {
        BindingRoot subject = new BindingRoot(new OverloadedMembers(), BigDecimal.ZERO);
        ExpressionEnvironment environment = environment(subject);

        assertCompileIssue("subject.members.value(dynamicArg)", environment, IssueCode.AMBIGUOUS_METHOD);
    }

    private static ExpressionEnvironment environment(BindingRoot subject) {
        return ExpressionEnvironment.builder()
                .registerTypeHint(BindingRoot.class)
                .registerTypeHint(OverloadedMembers.class)
                .registerExternalSymbol("subject", subject, true)
                .build();
    }

    private static void assertCompileIssue(
            String expression,
            ExpressionEnvironment environment,
            IssueCode expectedCode) {
        assertThatThrownBy(() -> MathExpression.compile(expression, environment))
                .isInstanceOfSatisfying(ExpressionCompilationException.class, exception ->
                        assertThat(exception.issues())
                                .extracting(CompilationIssue::code)
                                .contains(expectedCode));
    }

    record BindingRoot(OverloadedMembers members, BigDecimal amount) {
    }

    static final class OverloadedMembers {

        public BigDecimal value(BigDecimal input) {
            return input.add(BigDecimal.ONE);
        }

        public BigDecimal value(String input) {
            return BigDecimal.valueOf(input.length());
        }

        public BigDecimal numericOnly(BigDecimal input) {
            return input;
        }
    }
}
