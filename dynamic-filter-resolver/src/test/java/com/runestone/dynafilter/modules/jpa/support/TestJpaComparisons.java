/*
 * MIT License
 * <p>
 * Copyright (c) 2023-2023 Marcelo Silva Portilho
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.runestone.dynafilter.modules.jpa.support;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestJpaComparisons {

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Mock
    @SuppressWarnings("rawtypes")
    private Expression expression;

    @Mock
    private Predicate predicate;

    @Mock
    private Predicate nullPredicate;

    @Mock
    private Predicate comparisonPredicate;

    @Mock
    private Predicate combinedPredicate;

    @Test
    @DisplayName("comparable delegates number values to numeric predicates")
    @SuppressWarnings({"rawtypes", "unchecked"})
    void testComparableDelegatesNumbersToNumericPredicate() {
        Predicate result = JpaComparisons.comparable(
                expression,
                10,
                (ignoredExpression, ignoredValue) -> {
                    throw new AssertionError("Comparable predicate must not be used for numbers");
                },
                (numericExpression, numericValue) -> {
                    assertThat(numericExpression).isSameAs(expression);
                    assertThat(numericValue).isEqualTo(10);
                    return predicate;
                }
        );

        assertThat(result).isSameAs(predicate);
    }

    @Test
    @DisplayName("comparable delegates non-number values to comparable predicates")
    @SuppressWarnings({"rawtypes", "unchecked"})
    void testComparableDelegatesNonNumbersToComparablePredicate() {
        Predicate result = JpaComparisons.comparable(
                expression,
                "value",
                (comparableExpression, comparableValue) -> {
                    assertThat(comparableExpression).isSameAs(expression);
                    assertThat(comparableValue).isEqualTo("value");
                    return predicate;
                },
                (ignoredExpression, ignoredValue) -> {
                    throw new AssertionError("Numeric predicate must not be used for strings");
                }
        );

        assertThat(result).isSameAs(predicate);
    }

    @Test
    @DisplayName("compare routes less-than operators to CriteriaBuilder")
    @SuppressWarnings({"rawtypes", "unchecked"})
    void testCompareRoutesLessThanOperators() {
        when(criteriaBuilder.lessThan(any(Expression.class), eq("a"))).thenReturn(predicate);
        when(criteriaBuilder.lessThanOrEqualTo(any(Expression.class), eq("b"))).thenReturn(comparisonPredicate);

        assertThat(JpaComparisons.compare(criteriaBuilder, expression, ComparisonOperator.LESS_THAN, "a"))
                .isSameAs(predicate);
        assertThat(JpaComparisons.compare(criteriaBuilder, expression, ComparisonOperator.LESS_THAN_OR_EQUAL_TO, "b"))
                .isSameAs(comparisonPredicate);

        verify(criteriaBuilder).lessThan(any(Expression.class), eq("a"));
        verify(criteriaBuilder).lessThanOrEqualTo(any(Expression.class), eq("b"));
    }

    @Test
    @DisplayName("compare routes greater-than operators to CriteriaBuilder")
    @SuppressWarnings({"rawtypes", "unchecked"})
    void testCompareRoutesGreaterThanOperators() {
        when(criteriaBuilder.gt(any(Expression.class), eq(10))).thenReturn(predicate);
        when(criteriaBuilder.ge(any(Expression.class), eq(20))).thenReturn(comparisonPredicate);

        assertThat(JpaComparisons.compare(criteriaBuilder, expression, ComparisonOperator.GREATER_THAN, 10))
                .isSameAs(predicate);
        assertThat(JpaComparisons.compare(criteriaBuilder, expression, ComparisonOperator.GREATER_THAN_OR_EQUAL_TO, 20))
                .isSameAs(comparisonPredicate);

        verify(criteriaBuilder).gt(any(Expression.class), eq(10));
        verify(criteriaBuilder).ge(any(Expression.class), eq(20));
    }

    @Test
    @DisplayName("nullOr combines null checks with the selected comparison")
    @SuppressWarnings({"rawtypes", "unchecked"})
    void testNullOrCombinesNullCheckWithSelectedComparison() {
        when(criteriaBuilder.isNull(expression)).thenReturn(nullPredicate);
        when(criteriaBuilder.ge(any(Expression.class), eq(20))).thenReturn(comparisonPredicate);
        when(criteriaBuilder.or(nullPredicate, comparisonPredicate)).thenReturn(combinedPredicate);

        Predicate result = JpaComparisons.nullOr(criteriaBuilder, expression, ComparisonOperator.GREATER_THAN_OR_EQUAL_TO, 20);

        assertThat(result).isSameAs(combinedPredicate);
        verify(criteriaBuilder).isNull(expression);
        verify(criteriaBuilder).ge(any(Expression.class), eq(20));
        verify(criteriaBuilder).or(nullPredicate, comparisonPredicate);
    }

    @Test
    @DisplayName("public comparison entry points reject null mandatory arguments")
    void testPublicComparisonEntryPointsRejectNullMandatoryArguments() {
        assertThatNullPointerException()
                .isThrownBy(() -> JpaComparisons.comparable(null, "value", (left, right) -> predicate, (left, right) -> predicate))
                .withMessage("expression cannot be null");
        assertThatNullPointerException()
                .isThrownBy(() -> JpaComparisons.compare(null, expression, ComparisonOperator.LESS_THAN, "value"))
                .withMessage("criteriaBuilder cannot be null");
        assertThatNullPointerException()
                .isThrownBy(() -> JpaComparisons.compare(criteriaBuilder, expression, null, "value"))
                .withMessage("operator cannot be null");
        assertThatNullPointerException()
                .isThrownBy(() -> JpaComparisons.nullOr(criteriaBuilder, null, ComparisonOperator.LESS_THAN, "value"))
                .withMessage("expression cannot be null");
    }

}
