package com.runestone.assertions;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

public class TestAsserts {

    @Test
    public void testIsPositive() {
        Assertions.assertThat(Asserts.isPositive(null)).isFalse();
        Assertions.assertThat(Asserts.isPositive(BigDecimal.valueOf(-1))).isFalse();
        Assertions.assertThat(Asserts.isPositive(BigDecimal.ZERO)).isFalse();
        Assertions.assertThat(Asserts.isPositive(BigDecimal.ONE)).isTrue();
    }

    @Test
    public void testIsPositiveOrZero() {
        Assertions.assertThat(Asserts.isPositiveOrZero(null)).isFalse();
        Assertions.assertThat(Asserts.isPositiveOrZero(BigDecimal.valueOf(-1))).isFalse();
        Assertions.assertThat(Asserts.isPositiveOrZero(BigDecimal.ZERO)).isTrue();
        Assertions.assertThat(Asserts.isPositiveOrZero(BigDecimal.ONE)).isTrue();
    }

    @Test
    public void testIsPositiveOrNull() {
        Assertions.assertThat(Asserts.isPositiveOrNull(null)).isTrue();
        Assertions.assertThat(Asserts.isPositiveOrNull(BigDecimal.valueOf(-1))).isFalse();
        Assertions.assertThat(Asserts.isPositiveOrNull(BigDecimal.ZERO)).isFalse();
        Assertions.assertThat(Asserts.isPositiveOrNull(BigDecimal.ONE)).isTrue();
    }

    @Test
    public void testIsPositiveOrZeroOrNull() {
        Assertions.assertThat(Asserts.isPositiveOrZeroOrNull(null)).isTrue();
        Assertions.assertThat(Asserts.isPositiveOrZeroOrNull(BigDecimal.valueOf(-1))).isFalse();
        Assertions.assertThat(Asserts.isPositiveOrZeroOrNull(BigDecimal.ZERO)).isTrue();
        Assertions.assertThat(Asserts.isPositiveOrZeroOrNull(BigDecimal.ONE)).isTrue();
    }

    @Test
    public void testIsNegative() {
        Assertions.assertThat(Asserts.isNegative(null)).isFalse();
        Assertions.assertThat(Asserts.isNegative(BigDecimal.valueOf(-1))).isTrue();
        Assertions.assertThat(Asserts.isNegative(BigDecimal.ZERO)).isFalse();
        Assertions.assertThat(Asserts.isNegative(BigDecimal.ONE)).isFalse();
    }

    @Test
    public void testIsNegativeOrZero() {
        Assertions.assertThat(Asserts.isNegativeOrZero(null)).isFalse();
        Assertions.assertThat(Asserts.isNegativeOrZero(BigDecimal.valueOf(-1))).isTrue();
        Assertions.assertThat(Asserts.isNegativeOrZero(BigDecimal.ZERO)).isTrue();
        Assertions.assertThat(Asserts.isNegativeOrZero(BigDecimal.ONE)).isFalse();
    }

    @Test
    public void testIsNegativeOrZeroOrNull() {
        Assertions.assertThat(Asserts.isNegativeOrZeroOrNull(null)).isTrue();
        Assertions.assertThat(Asserts.isNegativeOrZeroOrNull(BigDecimal.valueOf(-1))).isTrue();
        Assertions.assertThat(Asserts.isNegativeOrZeroOrNull(BigDecimal.ZERO)).isTrue();
        Assertions.assertThat(Asserts.isNegativeOrZeroOrNull(BigDecimal.ONE)).isFalse();
    }

    @Test
    public void testIsNegativeOrNull() {
        Assertions.assertThat(Asserts.isNegativeOrNull(null)).isTrue();
        Assertions.assertThat(Asserts.isNegativeOrNull(BigDecimal.valueOf(-1))).isTrue();
        Assertions.assertThat(Asserts.isNegativeOrNull(BigDecimal.ZERO)).isFalse();
        Assertions.assertThat(Asserts.isNegativeOrNull(BigDecimal.ONE)).isFalse();
    }

    @Test
    public void testIsZero() {
        Assertions.assertThat(Asserts.isZero(null)).isFalse();
        Assertions.assertThat(Asserts.isZero(BigDecimal.valueOf(-1))).isFalse();
        Assertions.assertThat(Asserts.isZero(BigDecimal.ZERO)).isTrue();
        Assertions.assertThat(Asserts.isZero(BigDecimal.ONE)).isFalse();
    }

    @Test
    public void testIsZeroOrNull() {
        Assertions.assertThat(Asserts.isZeroOrNull(null)).isTrue();
        Assertions.assertThat(Asserts.isZeroOrNull(BigDecimal.valueOf(-1))).isFalse();
        Assertions.assertThat(Asserts.isZeroOrNull(BigDecimal.ZERO)).isTrue();
        Assertions.assertThat(Asserts.isZeroOrNull(BigDecimal.ONE)).isFalse();
    }

    @Test
    public void testIsBothNullOrNonNUll() {
        Assertions.assertThat(Asserts.isBothNullOrNonNUll(null, null)).isTrue();
        Assertions.assertThat(Asserts.isBothNullOrNonNUll(null, BigDecimal.ZERO)).isFalse();
        Assertions.assertThat(Asserts.isBothNullOrNonNUll(BigDecimal.ZERO, null)).isFalse();
        Assertions.assertThat(Asserts.isBothNullOrNonNUll(BigDecimal.ZERO, BigDecimal.ZERO)).isTrue();
    }

    @Test
    public void testIsOnlyOneNonNull() {
        Assertions.assertThat(Asserts.isOnlyOneNonNull(null, null)).isFalse();
        Assertions.assertThat(Asserts.isOnlyOneNonNull(null, BigDecimal.ZERO)).isTrue();
        Assertions.assertThat(Asserts.isOnlyOneNonNull(BigDecimal.ZERO, null)).isTrue();
        Assertions.assertThat(Asserts.isOnlyOneNonNull(BigDecimal.ZERO, BigDecimal.ZERO)).isFalse();
    }

    @Test
    public void testIsBetweenWithBigDecimal() {
        Assertions.assertThat(Asserts.isBetween(BigDecimal.valueOf(1), BigDecimal.valueOf(0), BigDecimal.valueOf(2))).isTrue();
        Assertions.assertThat(Asserts.isBetween(BigDecimal.valueOf(0), BigDecimal.valueOf(0), BigDecimal.valueOf(2))).isTrue();
        Assertions.assertThat(Asserts.isBetween(BigDecimal.valueOf(2), BigDecimal.valueOf(0), BigDecimal.valueOf(2))).isTrue();
        Assertions.assertThat(Asserts.isBetween(BigDecimal.valueOf(3), BigDecimal.valueOf(0), BigDecimal.valueOf(2))).isFalse();
        Assertions.assertThat(Asserts.isBetween(BigDecimal.valueOf(-1), BigDecimal.valueOf(0), BigDecimal.valueOf(2))).isFalse();
    }

    @Test
    public void testIsBetweenWithBigDouble() {
        Assertions.assertThat(Asserts.isBetween(1d, 0d, 2d)).isTrue();
        Assertions.assertThat(Asserts.isBetween(0d, 0d, 2d)).isTrue();
        Assertions.assertThat(Asserts.isBetween(2d, 0d, 2d)).isTrue();
        Assertions.assertThat(Asserts.isBetween(3d, 0d, 2d)).isFalse();
        Assertions.assertThat(Asserts.isBetween(-1d, 0d, 2d)).isFalse();
    }

    @Test
    public void testIsEmptyArray() {
        Assertions.assertThat(Asserts.isEmpty((Object[]) null)).isTrue();
        Assertions.assertThat(Asserts.isEmpty(new Object[]{})).isTrue();
        Assertions.assertThat(Asserts.isEmpty(new Object[]{null})).isFalse();
        Assertions.assertThat(Asserts.isEmpty(new Object[]{null, null})).isFalse();
    }

    @Test
    public void testIsEmptyString() {
        Assertions.assertThat(Asserts.isEmpty((String) null)).isTrue();
        Assertions.assertThat(Asserts.isEmpty("")).isTrue();
        Assertions.assertThat(Asserts.isEmpty(" ")).isFalse();
        Assertions.assertThat(Asserts.isEmpty("a")).isFalse();
    }

    @Test
    public void testIsBlankString() {
        Assertions.assertThat(Asserts.isBlank((String) null)).isTrue();
        Assertions.assertThat(Asserts.isBlank("")).isTrue();
        Assertions.assertThat(Asserts.isBlank(" ")).isTrue();
        Assertions.assertThat(Asserts.isBlank("a")).isFalse();
    }

}
