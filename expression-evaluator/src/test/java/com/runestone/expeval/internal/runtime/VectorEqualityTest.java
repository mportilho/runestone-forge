package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.api.LogicalExpression;
import com.runestone.expeval.environment.ExpressionEnvironment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Vector Equality Unit Tests")
class VectorEqualityTest {

    private final ExpressionEnvironment env = ExpressionEnvironment.builder().build();

    @Nested
    @DisplayName("Happy Path")
    class HappyPath {

        @Test
        @DisplayName("Identical numeric vectors are equal")
        void identicalNumericVectorsAreEqual() {
            boolean result = LogicalExpression.compile("v1 = v2", env)
                    .compute(Map.of(
                            "v1", List.of(new BigDecimal("1"), new BigDecimal("2")),
                            "v2", List.of(new BigDecimal("1"), new BigDecimal("2"))));
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Vectors with numbers of different scales are equal")
        void vectorsWithDifferentScalesAreEqual() {
            boolean result = LogicalExpression.compile("v1 = v2", env)
                    .compute(Map.of(
                            "v1", List.of(new BigDecimal("1.0")),
                            "v2", List.of(new BigDecimal("1.000"))));
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Vectors with mixed types are equal if elements are identical")
        void mixedTypeVectorsAreEqual() {
            boolean result = LogicalExpression.compile("v1 = v2", env)
                    .compute(Map.of(
                            "v1", List.of(new BigDecimal("1"), "text", true),
                            "v2", List.of(new BigDecimal("1.0"), "text", true)));
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Nested vectors are compared recursively")
        void nestedVectorsAreEqualRecursively() {
            boolean result = LogicalExpression.compile("v1 = v2", env)
                    .compute(Map.of(
                            "v1", List.of(List.of(new BigDecimal("1")), new BigDecimal("2")),
                            "v2", List.of(List.of(new BigDecimal("1.0")), new BigDecimal("2.0"))));
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("Boundary / Edge Cases")
    class BoundaryCases {

        @Test
        @DisplayName("Vectors of different sizes are not equal")
        void differentSizeVectorsAreNotEqual() {
            boolean result = LogicalExpression.compile("v1 = v2", env)
                    .compute(Map.of(
                            "v1", List.of(new BigDecimal("1"), new BigDecimal("2")),
                            "v2", List.of(new BigDecimal("1"), new BigDecimal("2"), new BigDecimal("3"))));
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Vectors with different element order are not equal")
        void differentElementOrderIsNotEqual() {
            boolean result = LogicalExpression.compile("v1 = v2", env)
                    .compute(Map.of(
                            "v1", List.of(new BigDecimal("1"), new BigDecimal("2")),
                            "v2", List.of(new BigDecimal("2"), new BigDecimal("1"))));
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Empty vectors are equal")
        void emptyVectorsAreEqual() {
            boolean result = LogicalExpression.compile("v1 = v2", env)
                    .compute(Map.of(
                            "v1", List.of(),
                            "v2", List.of()));
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("Null Handling")
    class NullHandling {

        @Test
        @DisplayName("Vectors with null elements are equal if nulls are in the same position")
        void vectorsWithNullElementsAreEqual() {
            HashMap<String, Object> vars = new HashMap<>();
            vars.put("v1", Arrays.asList(null, new BigDecimal("1")));
            vars.put("v2", Arrays.asList(null, new BigDecimal("1.0")));
            boolean result = LogicalExpression.compile("v1 = v2", env)
                    .compute(vars);
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Vector is not equal to a null value")
        void vectorIsNotEqualToNullValue() {
            HashMap<String, Object> vars = new HashMap<>();
            vars.put("v1", List.of(new BigDecimal("1")));
            vars.put("v2", null);
            boolean result = LogicalExpression.compile("v1 = v2", env)
                    .compute(vars);
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Null element vs non-null element in vector results in non-equality")
        void nullVsNonNullInVectorIsNotEqual() {
            HashMap<String, Object> vars = new HashMap<>();
            vars.put("v1", Arrays.asList(null, new BigDecimal("1")));
            vars.put("v2", Arrays.asList(new BigDecimal("2"), new BigDecimal("1")));
            boolean result = LogicalExpression.compile("v1 = v2", env)
                    .compute(vars);
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("Negative Cases")
    class NegativeCases {

        @Test
        @DisplayName("Vectors with different element values are not equal")
        void vectorsWithDifferentValuesAreNotEqual() {
            boolean result = LogicalExpression.compile("v1 = v2", env)
                    .compute(Map.of(
                            "v1", List.of(new BigDecimal("1")),
                            "v2", List.of(new BigDecimal("2"))));
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Vectors with different element types are not equal")
        void vectorsWithDifferentTypesAreNotEqual() {
            // String vs Boolean - no number coercion path
            boolean result = LogicalExpression.compile("v1 = v2", env)
                    .compute(Map.of(
                            "v1", List.of("true"),
                            "v2", List.of(true)));
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Vectors with different types that coerce to same value are EQUAL (Engine Behavior)")
        void vectorsWithCoercibleTypesAreEqual() {
            // Engine coerces to Number if one side is Number. "1" coerces to 1.
            boolean result = LogicalExpression.compile("v1 = v2", env)
                    .compute(Map.of(
                            "v1", List.of(new BigDecimal("1")),
                            "v2", List.of("1")));
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Comparing Number with non-coercible String in vector throws Exception (Engine Behavior)")
        void comparingNumberWithNonCoercibleStringThrowsException() {
            assertThatThrownBy(() -> LogicalExpression.compile("v1 = v2", env)
                    .compute(Map.of(
                            "v1", List.of(new BigDecimal("1")),
                            "v2", List.of("abc"))))
                    .isInstanceOf(RuntimeException.class); // NumberFormatException propagated
        }
    }

    @Nested
    @DisplayName("Unsupported Comparisons")
    class UnsupportedComparisons {
        @Test
        @DisplayName("Greater than comparison between vectors is not supported")
        void greaterThanBetweenVectorsThrowsException() {
            assertThatThrownBy(() -> LogicalExpression.compile("v1 > v2", env)
                    .compute(Map.of(
                            "v1", List.of(new BigDecimal("1")),
                            "v2", List.of(new BigDecimal("2")))))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("unsupported comparison");
        }
    }
}
