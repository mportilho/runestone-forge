package com.runestone.expeval.operation.values;

import com.runestone.expeval.expression.Expression;
import com.runestone.expeval.tools.VerifyExpressionsTools;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Map;

public class TestAssignableVariables {

    @Test
    public void testOneAssignedVariable() {
        Expression expression = new Expression("""
                a := b + c;
                a^d
                """);
        VerifyExpressionsTools.checkWarmUpCache(expression, 0);
        expression.setVariable("b", 2);
        expression.setVariable("c", 3);
        expression.setVariable("d", 4);
        Assertions.assertThat(expression.<BigDecimal>evaluate()).isEqualByComparingTo("625");
        Assertions.assertThat(expression.getAssignedVariables()).containsExactlyInAnyOrderEntriesOf(Map.of("a", BigDecimal.valueOf(5)));
        Assertions.assertThat(expression.getVariables()).containsExactlyInAnyOrderEntriesOf(Map.of(
                "b", BigDecimal.valueOf(2),
                "c", BigDecimal.valueOf(3),
                "d", BigDecimal.valueOf(4))
        );
        Assertions.assertThat(expression.toString()).isEqualTo("""
                a := 2 + 3;
                5 ^ 4""");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 7);
        VerifyExpressionsTools.checkWarmUpCache(expression, 7);
    }

    @Test
    public void testMultipleAssignedVariables() {
        Expression expression = new Expression("""
                a := 1 + 2;
                b := 3 + 4;
                c := a * b;
                c - b - a
                """);
        VerifyExpressionsTools.checkWarmUpCache(expression, 13);
        Assertions.assertThat(expression.<BigDecimal>evaluate()).isEqualByComparingTo("11");
        Assertions.assertThat(expression.getAssignedVariables()).containsExactlyInAnyOrderEntriesOf(Map.of(
                "a", BigDecimal.valueOf(3),
                "b", BigDecimal.valueOf(7),
                "c", BigDecimal.valueOf(21))
        );
        Assertions.assertThat(expression.getVariables()).isEmpty();
        Assertions.assertThat(expression.toString()).isEqualTo("""
                a := 1 + 2;
                b := 3 + 4;
                c := 3 * 7;
                21 - 7 - 3""");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 13);
        VerifyExpressionsTools.checkWarmUpCache(expression, 13);
    }

    @Test
    public void testMultipleVariablesAndAssignedVariables() {
        Expression expression = new Expression("""
                a := x + 2;
                b := y + 4;
                c := a * b;
                c - b - a
                """);
        VerifyExpressionsTools.checkWarmUpCache(expression, 2);
        expression.setVariable("x", 1);
        expression.setVariable("y", 3);
        Assertions.assertThat(expression.<BigDecimal>evaluate()).isEqualByComparingTo("11");
        Assertions.assertThat(expression.getAssignedVariables()).containsExactlyInAnyOrderEntriesOf(Map.of(
                "a", BigDecimal.valueOf(3),
                "b", BigDecimal.valueOf(7),
                "c", BigDecimal.valueOf(21))
        );
        Assertions.assertThat(expression.getVariables()).containsExactlyInAnyOrderEntriesOf(Map.of(
                "x", BigDecimal.valueOf(1),
                "y", BigDecimal.valueOf(3))
        );
        Assertions.assertThat(expression.toString()).isEqualTo("""
                a := 1 + 2;
                b := 3 + 4;
                c := 3 * 7;
                21 - 7 - 3""");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 13);
        VerifyExpressionsTools.checkWarmUpCache(expression, 13);
    }

    @Test
    public void testAssignWithBoolean() {
        Expression expression = new Expression("""
                a := true;
                b := false;
                c := a and b;
                c
                """);
        VerifyExpressionsTools.checkWarmUpCache(expression, 7);
        Assertions.assertThat(expression.<Boolean>evaluate()).isFalse();
        Assertions.assertThat(expression.getAssignedVariables()).containsExactlyInAnyOrderEntriesOf(Map.of(
                "a", true,
                "b", false,
                "c", false)
        );
        Assertions.assertThat(expression.getVariables()).isEmpty();
        Assertions.assertThat(expression.toString()).isEqualTo("""
                a := true;
                b := false;
                c := true and false;
                false""");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 7);
        VerifyExpressionsTools.checkWarmUpCache(expression, 7);
    }

    @Test
    public void testAssignWithString() {
        Expression expression = new Expression("""
                a := 'a';
                b := 'b';
                c := concat([a, b]);
                c = 'ab'
                """);
        VerifyExpressionsTools.checkWarmUpCache(expression, 10);
        Assertions.assertThat(expression.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(expression.getAssignedVariables()).containsExactlyInAnyOrderEntriesOf(Map.of(
                "a", "a",
                "b", "b",
                "c", "ab")
        );
        Assertions.assertThat(expression.getVariables()).isEmpty();
        Assertions.assertThat(expression.toString()).isEqualTo("""
                a := 'a';
                b := 'b';
                c := concat(['a', 'b']);
                'ab' = 'ab'""");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 10);
        VerifyExpressionsTools.checkWarmUpCache(expression, 10);
    }

    @Test
    public void testAssignWithDate() {
        Expression expression = new Expression("""
                a := 2021-01-01;
                b := 2021-01-02;
                c := a < b;
                c
                """);
        VerifyExpressionsTools.checkWarmUpCache(expression, 7);
        Assertions.assertThat(expression.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(expression.getAssignedVariables()).containsExactlyInAnyOrderEntriesOf(Map.of(
                "a", LocalDate.parse("2021-01-01"),
                "b", LocalDate.parse("2021-01-02"),
                "c", true)
        );
        Assertions.assertThat(expression.getVariables()).isEmpty();
        Assertions.assertThat(expression.toString()).isEqualTo("""
                a := 2021-01-01;
                b := 2021-01-02;
                c := 2021-01-01 < 2021-01-02;
                true""");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 7);
        VerifyExpressionsTools.checkWarmUpCache(expression, 7);
    }

    @Test
    public void testAssignWithTime() {
        Expression expression = new Expression("""
                a := 02:03:00;
                b := 02:03:01;
                c := a < b;
                c
                """);
        VerifyExpressionsTools.checkWarmUpCache(expression, 7);
        Assertions.assertThat(expression.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(expression.getAssignedVariables()).containsExactlyInAnyOrderEntriesOf(Map.of(
                "a", LocalTime.parse("02:03:00"),
                "b", LocalTime.parse("02:03:01"),
                "c", true)
        );
        Assertions.assertThat(expression.getVariables()).isEmpty();
        Assertions.assertThat(expression.toString()).isEqualTo("""
                a := 02:03;
                b := 02:03:01;
                c := 02:03 < 02:03:01;
                true""");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 7);
        VerifyExpressionsTools.checkWarmUpCache(expression, 7);
    }

    @Test
    public void testAssignWithDateTime() {
        Expression expression = new Expression("""
                a := 2021-01-01T02:03:00;
                b := 2021-01-01T02:03:01;
                c := a < b;
                c
                """);
        VerifyExpressionsTools.checkWarmUpCache(expression, 7);
        Assertions.assertThat(expression.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(expression.getAssignedVariables()).containsExactlyInAnyOrderEntriesOf(Map.of(
                "a", ZonedDateTime.of(LocalDate.parse("2021-01-01"), LocalTime.parse("02:03:00"), expression.getOptions().zoneId()),
                "b", ZonedDateTime.of(LocalDate.parse("2021-01-01"), LocalTime.parse("02:03:01"), expression.getOptions().zoneId()),
                "c", true)
        );
        Assertions.assertThat(expression.getVariables()).isEmpty();
        Assertions.assertThat(expression.toString()).isEqualTo("""
                a := 2021-01-01T02:03:00;
                b := 2021-01-01T02:03:01;
                c := 2021-01-01T02:03:00 < 2021-01-01T02:03:01;
                true""");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 7);
        VerifyExpressionsTools.checkWarmUpCache(expression, 7);
    }

    // ---------------------------------- Arrays ----------------------------------

    @Test
    public void testAssignWithNumberArrays() {
        Expression expression = new Expression("""
                a := [1, 2, 3];
                min(a)
                """);
        VerifyExpressionsTools.checkWarmUpCache(expression, 7);
        Assertions.assertThat(expression.<BigDecimal>evaluate()).isEqualByComparingTo("1");
        Assertions.assertThat(expression.getAssignedVariables()).containsExactlyInAnyOrderEntriesOf(Map.of(
                "a", new BigDecimal[]{BigDecimal.valueOf(1), BigDecimal.valueOf(2), BigDecimal.valueOf(3)})
        );
        Assertions.assertThat(expression.getVariables()).isEmpty();
        Assertions.assertThat(expression.toString()).isEqualTo("""
                a := [1, 2, 3];
                min([1, 2, 3])""");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 7);
        VerifyExpressionsTools.checkWarmUpCache(expression, 7);
    }

    @Test
    public void testAssignWithBooleanArrays() {
        Expression expression = new Expression("""
                a := [true, false];
                max(a) = !min(a)
                """);
        VerifyExpressionsTools.checkWarmUpCache(expression, 9);
        Assertions.assertThat(expression.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(expression.getAssignedVariables()).containsExactlyInAnyOrderEntriesOf(Map.of(
                "a", new Boolean[]{true, false})
        );
        Assertions.assertThat(expression.getVariables()).isEmpty();
        Assertions.assertThat(expression.toString()).isEqualTo("""
                a := [true, false];
                max([true, false]) = !min([true, false])""");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 9);
        VerifyExpressionsTools.checkWarmUpCache(expression, 9);
    }

    @Test
    public void testAssignWithStringArrays() {
        Expression expression = new Expression("""
                a := ['a', 'b'];
                concat(a) = 'ab'
                """);
        VerifyExpressionsTools.checkWarmUpCache(expression, 8);
        Assertions.assertThat(expression.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(expression.getAssignedVariables()).containsExactlyInAnyOrderEntriesOf(Map.of(
                "a", new String[]{"a", "b"})
        );
        Assertions.assertThat(expression.getVariables()).isEmpty();
        Assertions.assertThat(expression.toString()).isEqualTo("""
                a := ['a', 'b'];
                concat(['a', 'b']) = 'ab'""");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 8);
        VerifyExpressionsTools.checkWarmUpCache(expression, 8);
    }

    @Test
    public void testAssignWithDateArrays() {
        Expression expression = new Expression("""
                a := [2021-01-01, 2021-01-02];
                max(a) = 2021-01-02
                """);
        VerifyExpressionsTools.checkWarmUpCache(expression, 8);
        Assertions.assertThat(expression.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(expression.getAssignedVariables()).containsExactlyInAnyOrderEntriesOf(Map.of(
                "a", new LocalDate[]{LocalDate.parse("2021-01-01"), LocalDate.parse("2021-01-02")})
        );
        Assertions.assertThat(expression.getVariables()).isEmpty();
        Assertions.assertThat(expression.toString()).isEqualTo("""
                a := [2021-01-01, 2021-01-02];
                max([2021-01-01, 2021-01-02]) = 2021-01-02""");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 8);
        VerifyExpressionsTools.checkWarmUpCache(expression, 8);
    }

    @Test
    public void testAssignWithTimeArrays() {
        Expression expression = new Expression("""
                a := [02:03:00, 02:03:01];
                max(a) = 02:03:01
                """);
        VerifyExpressionsTools.checkWarmUpCache(expression, 8);
        Assertions.assertThat(expression.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(expression.getAssignedVariables()).containsExactlyInAnyOrderEntriesOf(Map.of(
                "a", new LocalTime[]{LocalTime.parse("02:03:00"), LocalTime.parse("02:03:01")})
        );
        Assertions.assertThat(expression.getVariables()).isEmpty();
        Assertions.assertThat(expression.toString()).isEqualTo("""
                a := [02:03, 02:03:01];
                max([02:03, 02:03:01]) = 02:03:01""");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 8);
        VerifyExpressionsTools.checkWarmUpCache(expression, 8);
    }

    @Test
    public void testAssignWithDateTimeArrays() {
        Expression expression = new Expression("""
                a := [2021-01-01T02:03:00, 2021-01-01T02:03:01];
                max(a) = 2021-01-01T02:03:01
                """);
        VerifyExpressionsTools.checkWarmUpCache(expression, 8);
        Assertions.assertThat(expression.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(expression.getAssignedVariables()).containsExactlyInAnyOrderEntriesOf(Map.of(
                "a", new ZonedDateTime[]{
                        ZonedDateTime.of(LocalDate.parse("2021-01-01"), LocalTime.parse("02:03:00"), expression.getOptions().zoneId()),
                        ZonedDateTime.of(LocalDate.parse("2021-01-01"), LocalTime.parse("02:03:01"), expression.getOptions().zoneId())})
        );
        Assertions.assertThat(expression.getVariables()).isEmpty();
        Assertions.assertThat(expression.toString()).isEqualTo("""
                a := [2021-01-01T02:03:00, 2021-01-01T02:03:01];
                max([2021-01-01T02:03:00, 2021-01-01T02:03:01]) = 2021-01-01T02:03:01""");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 8);
        VerifyExpressionsTools.checkWarmUpCache(expression, 8);
    }

    @Test
    public void testAssignWithVariableArrays() {
        Expression expression = new Expression("""
                a := [x, y, z];
                max(a)
                """);
        VerifyExpressionsTools.checkWarmUpCache(expression, 0);
        expression.setVariable("x", 1);
        expression.setVariable("y", 2);
        expression.setVariable("z", 3);
        Assertions.assertThat(expression.<BigDecimal>evaluate()).isEqualByComparingTo("3");
        Assertions.assertThat(expression.getAssignedVariables()).containsExactlyInAnyOrderEntriesOf(Map.of(
                "a", new BigDecimal[]{BigDecimal.valueOf(1), BigDecimal.valueOf(2), BigDecimal.valueOf(3)})
        );
        Assertions.assertThat(expression.getVariables()).containsExactlyInAnyOrderEntriesOf(Map.of(
                "x", BigDecimal.valueOf(1),
                "y", BigDecimal.valueOf(2),
                "z", BigDecimal.valueOf(3))
        );
        Assertions.assertThat(expression.toString()).isEqualTo("""
                a := [1, 2, 3];
                max([1, 2, 3])""");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 7);
        VerifyExpressionsTools.checkWarmUpCache(expression, 7);
    }

    @Test
    public void testAssignWithVariableArrayExpression() {
        Expression expression = new Expression("""
                a := [1 + 2, 2 * 2, min(5, 6, 7)];
                max(a)
                """);
        VerifyExpressionsTools.checkWarmUpCache(expression, 14);
        Assertions.assertThat(expression.<BigDecimal>evaluate()).isEqualByComparingTo("5");
        Assertions.assertThat(expression.getAssignedVariables()).containsExactlyInAnyOrderEntriesOf(Map.of(
                "a", new BigDecimal[]{BigDecimal.valueOf(3), BigDecimal.valueOf(4), BigDecimal.valueOf(5)})
        );
        Assertions.assertThat(expression.getVariables()).isEmpty();
        Assertions.assertThat(expression.toString()).isEqualTo("""
                a := [1 + 2, 2 * 2, min(5, 6, 7)];
                max([3, 4, 5])""");
        VerifyExpressionsTools.commonVerifications(expression);
        VerifyExpressionsTools.checkCache(expression, 14);
        VerifyExpressionsTools.checkWarmUpCache(expression, 14);
    }

}
