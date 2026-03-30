package com.runestone.expeval2.internal.runtime;

import com.runestone.expeval2.api.LogicalExpression;
import com.runestone.expeval2.environment.ExpressionEnvironment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("null in / null not in membership operator")
class NullMembershipTest {

    private final ExpressionEnvironment env = ExpressionEnvironment.builder().build();

    @Nested
    @DisplayName("null IN vector")
    class NullIn {

        @Test
        @DisplayName("null found in literal vector containing null")
        void nullFoundInLiteralVectorWithNull() {
            boolean result = LogicalExpression.compile("null in [1, null, 3]", env).compute(Map.of());
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("null not found in literal vector with all non-null elements")
        void nullNotFoundInLiteralVectorWithoutNull() {
            boolean result = LogicalExpression.compile("null in [1, 2, 3]", env).compute(Map.of());
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("null found in external vector variable containing null")
        void nullFoundInExternalVectorWithNull() {
            HashMap<String, Object> vars = new HashMap<>();
            vars.put("myVec", Arrays.asList(null, "x"));
            boolean result = LogicalExpression.compile("null in myVec", env).compute(vars);
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("null not found in external vector variable with no null elements")
        void nullNotFoundInExternalVectorWithoutNull() {
            boolean result = LogicalExpression.compile("null in myVec", env)
                    .compute(Map.of("myVec", List.of("a", "b")));
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("null NOT IN vector")
    class NullNotIn {

        @Test
        @DisplayName("null not in vector that contains null returns false")
        void nullNotInVectorWithNullReturnsFalse() {
            boolean result = LogicalExpression.compile("null not in [1, null, 3]", env).compute(Map.of());
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("null not in vector with no null elements returns true")
        void nullNotInVectorWithoutNullReturnsTrue() {
            boolean result = LogicalExpression.compile("null not in [1, 2, 3]", env).compute(Map.of());
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("null not in external vector variable containing null returns false")
        void nullNotInExternalVectorWithNullReturnsFalse() {
            HashMap<String, Object> vars = new HashMap<>();
            vars.put("myVec", Arrays.asList(null, "x"));
            boolean result = LogicalExpression.compile("null not in myVec", env).compute(vars);
            assertThat(result).isFalse();
        }
    }
}
