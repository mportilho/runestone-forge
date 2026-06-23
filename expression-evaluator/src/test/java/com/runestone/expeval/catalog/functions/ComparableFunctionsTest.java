package com.runestone.expeval.catalog.functions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ComparableFunctions Tests")
class ComparableFunctionsTest {

    @Nested
    @DisplayName("max()")
    class MaxTests {

        @Test
        @DisplayName("Throws exception on null array")
        void throwsOnNullArray() {
            assertThatThrownBy(() -> ComparableFunctions.max(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Throws exception on empty array")
        void throwsOnEmptyArray() {
            Integer[] input = new Integer[0];
            assertThatThrownBy(() -> ComparableFunctions.max(input))
                    .isInstanceOf(ArrayIndexOutOfBoundsException.class);
        }

        @Test
        @DisplayName("Single value")
        void singleValue() {
            Integer[] input = {10};
            assertThat(ComparableFunctions.max(input)).isEqualTo(10);
        }

        @Test
        @DisplayName("Two values, first is greater")
        void twoValuesFirstGreater() {
            Integer[] input = {20, 10};
            assertThat(ComparableFunctions.max(input)).isEqualTo(20);
        }

        @Test
        @DisplayName("Two values, second is greater")
        void twoValuesSecondGreater() {
            Integer[] input = {10, 20};
            assertThat(ComparableFunctions.max(input)).isEqualTo(20);
        }

        @Test
        @DisplayName("Multiple values")
        void multipleValues() {
            Integer[] input = {10, 30, 20};
            assertThat(ComparableFunctions.max(input)).isEqualTo(30);
        }

        @Test
        @DisplayName("Multiple values with strings")
        void multipleValuesWithStrings() {
            String[] input = {"a", "c", "b"};
            assertThat(ComparableFunctions.max(input)).isEqualTo("c");
        }

        @Test
        @DisplayName("Multiple values, all equal")
        void multipleValuesAllEqual() {
            Integer[] input = {5, 5, 5};
            assertThat(ComparableFunctions.max(input)).isEqualTo(5);
        }

        @Test
        @DisplayName("Throws exception if array contains null")
        void throwsOnNullElement() {
            Integer[] input = {10, null, 20};
            assertThatThrownBy(() -> ComparableFunctions.max(input))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("min()")
    class MinTests {

        @Test
        @DisplayName("Throws exception on null array")
        void throwsOnNullArray() {
            assertThatThrownBy(() -> ComparableFunctions.min(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Throws exception on empty array")
        void throwsOnEmptyArray() {
            Integer[] input = new Integer[0];
            assertThatThrownBy(() -> ComparableFunctions.min(input))
                    .isInstanceOf(ArrayIndexOutOfBoundsException.class);
        }

        @Test
        @DisplayName("Single value")
        void singleValue() {
            Integer[] input = {10};
            assertThat(ComparableFunctions.min(input)).isEqualTo(10);
        }

        @Test
        @DisplayName("Two values, first is smaller")
        void twoValuesFirstSmaller() {
            Integer[] input = {10, 20};
            assertThat(ComparableFunctions.min(input)).isEqualTo(10);
        }

        @Test
        @DisplayName("Two values, second is smaller")
        void twoValuesSecondSmaller() {
            Integer[] input = {20, 10};
            assertThat(ComparableFunctions.min(input)).isEqualTo(10);
        }

        @Test
        @DisplayName("Multiple values")
        void multipleValues() {
            Integer[] input = {30, 10, 20};
            assertThat(ComparableFunctions.min(input)).isEqualTo(10);
        }

        @Test
        @DisplayName("Multiple values with strings")
        void multipleValuesWithStrings() {
            String[] input = {"c", "a", "b"};
            assertThat(ComparableFunctions.min(input)).isEqualTo("a");
        }

        @Test
        @DisplayName("Multiple values, all equal")
        void multipleValuesAllEqual() {
            Integer[] input = {5, 5, 5};
            assertThat(ComparableFunctions.min(input)).isEqualTo(5);
        }

        @Test
        @DisplayName("Throws exception if array contains null")
        void throwsOnNullElement() {
            Integer[] input = {10, null, 20};
            assertThatThrownBy(() -> ComparableFunctions.min(input))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}
