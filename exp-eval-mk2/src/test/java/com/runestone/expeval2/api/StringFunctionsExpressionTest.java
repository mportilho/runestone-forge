package com.runestone.expeval2.api;

import com.runestone.expeval2.environment.ExpressionEnvironment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("StringFunctions via expression API")
class StringFunctionsExpressionTest {

    private static final ExpressionEnvironment ENV = ExpressionEnvironment.builder().addStringFunctions().build();
    private static final ExpressionEnvironment ALL_FUNCTIONS_ENV = ExpressionEnvironment.builder().addAllFunctions().build();

    @Nested
    @DisplayName("concatenation")
    class Concatenation {

        @Test
        @DisplayName("concat joins a vector of strings in an expression")
        void joinsMultipleStrings() {
            Object value = AssignmentExpression.compile("x = concat([\"run\", \"stone\", \"-\", \"forge\"]);", ENV)
                    .compute()
                    .get("x");

            assertThat(value).isEqualTo("runstone-forge");
        }
    }

    @Nested
    @DisplayName("registration")
    class Registration {

        @Test
        @DisplayName("addAllFunctions registers StringFunctions")
        void addAllFunctionsRegistersStringFunctions() {
            Object value = AssignmentExpression.compile("x = toUpper(\"forge\");", ALL_FUNCTIONS_ENV)
                    .compute()
                    .get("x");

            assertThat(value).isEqualTo("FORGE");
        }
    }

    @Nested
    @DisplayName("case conversion and trimming")
    class CaseConversionAndTrimming {

        @Test
        @DisplayName("toUpper and toLower convert case with locale-stable rules")
        void convertsCase() {
            var result = AssignmentExpression.compile("""
                    upper = toUpper("Forge");
                    lower = toLower("Forge");
                    """, ENV).compute();

            assertThat(result.get("upper")).isEqualTo("FORGE");
            assertThat(result.get("lower")).isEqualTo("forge");
        }

        @Test
        @DisplayName("trim variants remove surrounding whitespace on the expected side")
        void trimsWhitespace() {
            var result = AssignmentExpression.compile("""
                    full = trim("  forge  ");
                    left = trimLeft("  forge  ");
                    right = trimRight("  forge  ");
                    """, ENV).compute();

            assertThat(result.get("full")).isEqualTo("forge");
            assertThat(result.get("left")).isEqualTo("forge  ");
            assertThat(result.get("right")).isEqualTo("  forge");
        }
    }

    @Nested
    @DisplayName("substring family")
    class SubstringFamily {

        @Test
        @DisplayName("substring supports begin index and begin/end overloads")
        void supportsSubstringOverloads() {
            var result = AssignmentExpression.compile("""
                    from = substring("runestone", 3);
                    range = substring("runestone", 3, 7);
                    """, ENV).compute();

            assertThat(result.get("from")).isEqualTo("estone");
            assertThat(result.get("range")).isEqualTo("esto");
        }

        @Test
        @DisplayName("substring before and after delimiters returns the expected segments")
        void extractsSegmentsAroundDelimiter() {
            var result = AssignmentExpression.compile("""
                    before = substringBefore("alpha/beta/gamma", "/");
                    after = substringAfter("alpha/beta/gamma", "/");
                    beforeLast = substringBeforeLast("alpha/beta/gamma", "/");
                    afterLast = substringAfterLast("alpha/beta/gamma", "/");
                    """, ENV).compute();

            assertThat(result.get("before")).isEqualTo("alpha");
            assertThat(result.get("after")).isEqualTo("beta/gamma");
            assertThat(result.get("beforeLast")).isEqualTo("alpha/beta");
            assertThat(result.get("afterLast")).isEqualTo("gamma");
        }
    }

    @Nested
    @DisplayName("padding, repetition and replacement")
    class PaddingRepetitionAndReplacement {

        @Test
        @DisplayName("padLeft and padRight support default and custom fill values")
        void padsText() {
            var result = AssignmentExpression.compile("""
                    leftDefault = padLeft("7", 3);
                    leftCustom = padLeft("7", 3, "0");
                    rightDefault = padRight("7", 3);
                    rightCustom = padRight("7", 4, "xy");
                    """, ENV).compute();

            assertThat(result.get("leftDefault")).isEqualTo("  7");
            assertThat(result.get("leftCustom")).isEqualTo("007");
            assertThat(result.get("rightDefault")).isEqualTo("7  ");
            assertThat(result.get("rightCustom")).isEqualTo("7xyx");
        }

        @Test
        @DisplayName("repeat duplicates a string the requested number of times")
        void repeatsText() {
            Object value = AssignmentExpression.compile("x = repeat(\"ab\", 3);", ENV)
                    .compute()
                    .get("x");

            assertThat(value).isEqualTo("ababab");
        }

        @Test
        @DisplayName("replace variants distinguish literal replace, first-only replace and regex replaceAll")
        void replacesText() {
            var result = AssignmentExpression.compile("""
                    literal = replace("banana", "na", "!");
                    first = replaceFirst("banana", "na", "!");
                    regex = replaceAll("a1b2c3", "\\\\d", "_");
                    """, ENV).compute();

            assertThat(result.get("literal")).isEqualTo("ba!!");
            assertThat(result.get("first")).isEqualTo("ba!na");
            assertThat(result.get("regex")).isEqualTo("a_b_c_");
        }
    }

    @Nested
    @DisplayName("search and predicates")
    class SearchAndPredicates {

        @Test
        @DisplayName("indexOf and lastIndexOf return numeric positions")
        void findsIndexes() {
            BigDecimal first = MathExpression.compile("indexOf(\"banana\", \"na\")", ENV).compute();
            BigDecimal last = MathExpression.compile("lastIndexOf(\"banana\", \"na\")", ENV).compute();

            assertThat(first).isEqualByComparingTo(new BigDecimal("2"));
            assertThat(last).isEqualByComparingTo(new BigDecimal("4"));
        }

        @Test
        @DisplayName("length returns the size of the string")
        void measuresLength() {
            BigDecimal value = MathExpression.compile("length(\"forge\")", ENV).compute();

            assertThat(value).isEqualByComparingTo(new BigDecimal("5"));
        }

        @Test
        @DisplayName("startsWith endsWith and contains evaluate string membership predicates")
        void evaluatesMembershipPredicates() {
            boolean value = LogicalExpression.compile(
                    "startsWith(\"runestone\", \"run\") and endsWith(\"runestone\", \"stone\") and contains(\"runestone\", \"nest\")",
                    ENV
            ).compute();

            assertThat(value).isTrue();
        }

        @Test
        @DisplayName("isEmpty and isBlank distinguish empty and whitespace-only content")
        void evaluatesBlanknessPredicates() {
            boolean value = LogicalExpression.compile(
                    "isEmpty(\"\") and isBlank(\"   \") and !isBlank(\"forge\")",
                    ENV
            ).compute();

            assertThat(value).isTrue();
        }
    }

    @Nested
    @DisplayName("vector interop")
    class VectorInterop {

        @Test
        @DisplayName("split returns a vector preserving trailing empty values")
        void splitReturnsVector() {
            Object value = AssignmentExpression.compile("x = split(\"a,b,\", \",\");", ENV)
                    .compute()
                    .get("x");

            assertThat(value).isEqualTo(List.of("a", "b", ""));
        }

        @Test
        @DisplayName("join accepts literal vectors and custom delimiters")
        void joinAcceptsVectorLiterals() {
            Object value = AssignmentExpression.compile("x = join([\"run\", \"stone\", \"forge\"], \"-\");", ENV)
                    .compute()
                    .get("x");

            assertThat(value).isEqualTo("run-stone-forge");
        }

        @Test
        @DisplayName("join consumes the vector produced by split")
        void joinConsumesSplitOutput() {
            Object value = AssignmentExpression.compile("x = join(split(\"a,b,c\", \",\"), \";\");", ENV)
                    .compute()
                    .get("x");

            assertThat(value).isEqualTo("a;b;c");
        }
    }
}
