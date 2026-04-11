package com.runestone.expeval.api;

import com.runestone.expeval.environment.ExpressionEnvironmentBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Dynamic literal expressions")
class DynamicLiteralExpressionTest {

    @Test
    @DisplayName("currDate evaluates to the same value when referenced twice in a single expression")
    void currDateIsSameWithinSingleExecution() {
        boolean result = LogicalExpression.compile("currDate = currDate",
                ExpressionEnvironmentBuilder.empty()).compute();

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("currTime evaluates to the same value when referenced twice in a single expression")
    void currTimeIsSameWithinSingleExecution() {
        boolean result = LogicalExpression.compile("currTime = currTime",
                ExpressionEnvironmentBuilder.empty()).compute();

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("currDateTime evaluates to the same value when referenced twice in a single expression")
    void currDateTimeIsSameWithinSingleExecution() {
        boolean result = LogicalExpression.compile("currDateTime = currDateTime",
                ExpressionEnvironmentBuilder.empty()).compute();

        assertThat(result).isTrue();
    }
}
