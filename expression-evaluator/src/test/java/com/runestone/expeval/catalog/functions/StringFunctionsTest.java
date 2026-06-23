package com.runestone.expeval.catalog.functions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

@DisplayName("StringFunctions Tests")
class StringFunctionsTest {

    @Nested
    @DisplayName("concat()")
    class ConcatTests {

        @Test
        @DisplayName("Concatenates multiple strings")
        void concatenatesMultipleStrings() {
            assertThat(StringFunctions.concat(new String[]{"alpha", "beta", "gamma"})).isEqualTo("alphabetagamma");
            assertThat(StringFunctions.concat(new String[]{"alpha"})).isEqualTo("alpha");
            assertThat(StringFunctions.concat(new String[]{})).isEmpty();
        }

        @Test
        @DisplayName("Rejects null arguments in array")
        void rejectsNullArguments() {
            assertThatNullPointerException()
                    .isThrownBy(() -> StringFunctions.concat(new String[]{"alpha", null, "gamma"}))
                    .withMessage("value must not be null");
        }
    }

    @Nested
    @DisplayName("substring family")
    class SubstringFamilyTests {

        @Test
        @DisplayName("Returns documented values when separator is empty")
        void returnsDocumentedValuesWhenSeparatorIsEmpty() {
            assertThat(StringFunctions.substringBefore("alpha/beta", "")).isEmpty();
            assertThat(StringFunctions.substringAfter("alpha/beta", "")).isEqualTo("alpha/beta");
            assertThat(StringFunctions.substringBeforeLast("alpha/beta", "")).isEqualTo("alpha/beta");
            assertThat(StringFunctions.substringAfterLast("alpha/beta", "")).isEmpty();
        }

        @Test
        @DisplayName("Returns unchanged or empty values when separator is absent")
        void returnsExpectedValuesWhenSeparatorIsAbsent() {
            assertThat(StringFunctions.substringBefore("alpha/beta", "|")).isEqualTo("alpha/beta");
            assertThat(StringFunctions.substringAfter("alpha/beta", "|")).isEmpty();
            assertThat(StringFunctions.substringBeforeLast("alpha/beta", "|")).isEqualTo("alpha/beta");
            assertThat(StringFunctions.substringAfterLast("alpha/beta", "|")).isEmpty();
        }
    }

    @Nested
    @DisplayName("padLeft() and padRight()")
    class PaddingTests {

        @Test
        @DisplayName("Returns original text when requested size does not exceed length")
        void returnsOriginalTextWhenSizeDoesNotExceedLength() {
            assertThat(StringFunctions.padLeft("forge", 5)).isEqualTo("forge");
            assertThat(StringFunctions.padRight("forge", 3, "0")).isEqualTo("forge");
        }

        @Test
        @DisplayName("Rejects empty padding")
        void rejectsEmptyPadding() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> StringFunctions.padLeft("7", 3, ""))
                    .withMessage("padding must not be empty");

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> StringFunctions.padRight("7", 3, ""))
                    .withMessage("padding must not be empty");
        }
    }

    @Nested
    @DisplayName("replaceFirst()")
    class ReplaceFirstTests {

        @Test
        @DisplayName("Prefixes replacement when target is empty")
        void prefixesReplacementWhenTargetIsEmpty() {
            String value = StringFunctions.replaceFirst("forge", "", "pre-");

            assertThat(value).isEqualTo("pre-forge");
        }

        @Test
        @DisplayName("Returns original text when target is not found")
        void returnsOriginalTextWhenTargetIsNotFound() {
            String value = StringFunctions.replaceFirst("forge", "zzz", "x");

            assertThat(value).isEqualTo("forge");
        }
    }

    @Nested
    @DisplayName("replaceAll()")
    class ReplaceAllTests {

        @Test
        @DisplayName("Preserves regex replacement semantics with capture groups")
        void preservesRegexReplacementSemantics() {
            String value = StringFunctions.replaceAll("a1b2c3", "(\\d)", "[$1]");

            assertThat(value).isEqualTo("a[1]b[2]c[3]");
        }
    }

    @Nested
    @DisplayName("split()")
    class SplitTests {

        @Test
        @DisplayName("Preserves trailing empty values")
        void preservesTrailingEmptyValues() {
            List<String> value = StringFunctions.split("a,b,", ",");

            assertThat(value).containsExactly("a", "b", "");
        }

        @Test
        @DisplayName("Supports regex delimiters")
        void supportsRegexDelimiters() {
            List<String> value = StringFunctions.split("a1b22c", "\\d+");

            assertThat(value).containsExactly("a", "b", "c");
        }
    }

    @Nested
    @DisplayName("join()")
    class JoinTests {

        @Test
        @DisplayName("Returns empty string for empty list")
        void returnsEmptyStringForEmptyList() {
            String value = StringFunctions.join(List.of(), ",");

            assertThat(value).isEmpty();
        }

        @Test
        @DisplayName("Converts null elements using String.valueOf")
        void convertsNullElementsUsingStringValueOf() {
            String value = StringFunctions.join(Arrays.asList("a", null, 2), "|");

            assertThat(value).isEqualTo("a|null|2");
        }

        @Test
        @DisplayName("Rejects null list")
        void rejectsNullList() {
            assertThatNullPointerException()
                    .isThrownBy(() -> StringFunctions.join(null, ","))
                    .withMessage("values must not be null");
        }
    }
}
