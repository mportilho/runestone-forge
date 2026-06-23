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
class TestJpaIntervalPredicates {

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Mock
    @SuppressWarnings("rawtypes")
    private Expression startExpression;

    @Mock
    @SuppressWarnings("rawtypes")
    private Expression endExpression;

    @Mock
    private Predicate startPredicate;

    @Mock
    private Predicate endPredicate;

    @Mock
    private Predicate nullEndPredicate;

    @Mock
    private Predicate openEndedPredicate;

    @Mock
    private Predicate resultPredicate;

    @Test
    @DisplayName("effectiveAt with closed bounds includes start and end edges")
    @SuppressWarnings({"rawtypes", "unchecked"})
    void testEffectiveAtClosedIncludesBothEdges() {
        when(criteriaBuilder.lessThanOrEqualTo(any(Expression.class), eq("reference-start"))).thenReturn(startPredicate);
        when(criteriaBuilder.greaterThanOrEqualTo(any(Expression.class), eq("reference-end"))).thenReturn(endPredicate);
        stubOpenEndedIntervalComposition();

        Predicate result = JpaIntervalPredicates.effectiveAt(
                criteriaBuilder,
                startExpression,
                endExpression,
                "reference-start",
                "reference-end",
                IntervalBounds.CLOSED
        );

        assertThat(result).isSameAs(resultPredicate);
        verify(criteriaBuilder).lessThanOrEqualTo(any(Expression.class), eq("reference-start"));
        verify(criteriaBuilder).greaterThanOrEqualTo(any(Expression.class), eq("reference-end"));
    }

    @Test
    @DisplayName("effectiveAt with open bounds excludes start and end edges")
    @SuppressWarnings({"rawtypes", "unchecked"})
    void testEffectiveAtOpenExcludesBothEdges() {
        when(criteriaBuilder.lessThan(any(Expression.class), eq("reference-start"))).thenReturn(startPredicate);
        when(criteriaBuilder.greaterThan(any(Expression.class), eq("reference-end"))).thenReturn(endPredicate);
        stubOpenEndedIntervalComposition();

        Predicate result = JpaIntervalPredicates.effectiveAt(
                criteriaBuilder,
                startExpression,
                endExpression,
                "reference-start",
                "reference-end",
                IntervalBounds.OPEN
        );

        assertThat(result).isSameAs(resultPredicate);
        verify(criteriaBuilder).lessThan(any(Expression.class), eq("reference-start"));
        verify(criteriaBuilder).greaterThan(any(Expression.class), eq("reference-end"));
    }

    @Test
    @DisplayName("effectiveAt with half-open bounds includes start and excludes end")
    @SuppressWarnings({"rawtypes", "unchecked"})
    void testEffectiveAtHalfOpenIncludesStartAndExcludesEnd() {
        when(criteriaBuilder.lessThanOrEqualTo(any(Expression.class), eq("reference-start"))).thenReturn(startPredicate);
        when(criteriaBuilder.greaterThan(any(Expression.class), eq("reference-end"))).thenReturn(endPredicate);
        stubOpenEndedIntervalComposition();

        Predicate result = JpaIntervalPredicates.effectiveAt(
                criteriaBuilder,
                startExpression,
                endExpression,
                "reference-start",
                "reference-end",
                IntervalBounds.START_INCLUSIVE_END_EXCLUSIVE
        );

        assertThat(result).isSameAs(resultPredicate);
        verify(criteriaBuilder).lessThanOrEqualTo(any(Expression.class), eq("reference-start"));
        verify(criteriaBuilder).greaterThan(any(Expression.class), eq("reference-end"));
    }

    @Test
    @DisplayName("effectiveAt with start-exclusive end-inclusive bounds excludes start and includes end")
    @SuppressWarnings({"rawtypes", "unchecked"})
    void testEffectiveAtStartExclusiveEndInclusiveExcludesStartAndIncludesEnd() {
        when(criteriaBuilder.lessThan(any(Expression.class), eq("reference-start"))).thenReturn(startPredicate);
        when(criteriaBuilder.greaterThanOrEqualTo(any(Expression.class), eq("reference-end"))).thenReturn(endPredicate);
        stubOpenEndedIntervalComposition();

        Predicate result = JpaIntervalPredicates.effectiveAt(
                criteriaBuilder,
                startExpression,
                endExpression,
                "reference-start",
                "reference-end",
                IntervalBounds.START_EXCLUSIVE_END_INCLUSIVE
        );

        assertThat(result).isSameAs(resultPredicate);
        verify(criteriaBuilder).lessThan(any(Expression.class), eq("reference-start"));
        verify(criteriaBuilder).greaterThanOrEqualTo(any(Expression.class), eq("reference-end"));
    }

    @Test
    @DisplayName("periodOverlaps with closed bounds includes both overlap edges")
    @SuppressWarnings({"rawtypes", "unchecked"})
    void testPeriodOverlapsClosedIncludesBothEdges() {
        when(criteriaBuilder.lessThanOrEqualTo(any(Expression.class), eq("filter-end"))).thenReturn(startPredicate);
        when(criteriaBuilder.greaterThanOrEqualTo(any(Expression.class), eq("filter-start"))).thenReturn(endPredicate);
        stubOpenEndedIntervalComposition();

        Predicate result = JpaIntervalPredicates.periodOverlaps(
                criteriaBuilder,
                startExpression,
                endExpression,
                "filter-start",
                "filter-end",
                IntervalBounds.CLOSED
        );

        assertThat(result).isSameAs(resultPredicate);
        verify(criteriaBuilder).lessThanOrEqualTo(any(Expression.class), eq("filter-end"));
        verify(criteriaBuilder).greaterThanOrEqualTo(any(Expression.class), eq("filter-start"));
    }

    @Test
    @DisplayName("periodOverlaps with open bounds excludes both overlap edges")
    @SuppressWarnings({"rawtypes", "unchecked"})
    void testPeriodOverlapsOpenExcludesBothEdges() {
        when(criteriaBuilder.lessThan(any(Expression.class), eq("filter-end"))).thenReturn(startPredicate);
        when(criteriaBuilder.greaterThan(any(Expression.class), eq("filter-start"))).thenReturn(endPredicate);
        stubOpenEndedIntervalComposition();

        Predicate result = JpaIntervalPredicates.periodOverlaps(
                criteriaBuilder,
                startExpression,
                endExpression,
                "filter-start",
                "filter-end",
                IntervalBounds.OPEN
        );

        assertThat(result).isSameAs(resultPredicate);
        verify(criteriaBuilder).lessThan(any(Expression.class), eq("filter-end"));
        verify(criteriaBuilder).greaterThan(any(Expression.class), eq("filter-start"));
    }

    @Test
    @DisplayName("periodOverlaps with half-open bounds uses exclusive overlap comparisons")
    @SuppressWarnings({"rawtypes", "unchecked"})
    void testPeriodOverlapsHalfOpenUsesExclusiveOverlapComparisons() {
        when(criteriaBuilder.lessThan(any(Expression.class), eq("filter-end"))).thenReturn(startPredicate);
        when(criteriaBuilder.greaterThan(any(Expression.class), eq("filter-start"))).thenReturn(endPredicate);
        stubOpenEndedIntervalComposition();

        Predicate result = JpaIntervalPredicates.periodOverlaps(
                criteriaBuilder,
                startExpression,
                endExpression,
                "filter-start",
                "filter-end",
                IntervalBounds.START_INCLUSIVE_END_EXCLUSIVE
        );

        assertThat(result).isSameAs(resultPredicate);
        verify(criteriaBuilder).lessThan(any(Expression.class), eq("filter-end"));
        verify(criteriaBuilder).greaterThan(any(Expression.class), eq("filter-start"));
    }

    @Test
    @DisplayName("periodOverlaps with start-exclusive end-inclusive bounds uses exclusive overlap comparisons")
    @SuppressWarnings({"rawtypes", "unchecked"})
    void testPeriodOverlapsStartExclusiveEndInclusiveUsesExclusiveOverlapComparisons() {
        when(criteriaBuilder.lessThan(any(Expression.class), eq("filter-end"))).thenReturn(startPredicate);
        when(criteriaBuilder.greaterThan(any(Expression.class), eq("filter-start"))).thenReturn(endPredicate);
        stubOpenEndedIntervalComposition();

        Predicate result = JpaIntervalPredicates.periodOverlaps(
                criteriaBuilder,
                startExpression,
                endExpression,
                "filter-start",
                "filter-end",
                IntervalBounds.START_EXCLUSIVE_END_INCLUSIVE
        );

        assertThat(result).isSameAs(resultPredicate);
        verify(criteriaBuilder).lessThan(any(Expression.class), eq("filter-end"));
        verify(criteriaBuilder).greaterThan(any(Expression.class), eq("filter-start"));
    }

    @Test
    @DisplayName("interval predicate methods reject null mandatory arguments")
    void testIntervalPredicateMethodsRejectNullMandatoryArguments() {
        assertThatNullPointerException()
                .isThrownBy(() -> JpaIntervalPredicates.effectiveAt(null, startExpression, endExpression, "a", "b", IntervalBounds.CLOSED))
                .withMessage("criteriaBuilder cannot be null");
        assertThatNullPointerException()
                .isThrownBy(() -> JpaIntervalPredicates.effectiveAt(criteriaBuilder, null, endExpression, "a", "b", IntervalBounds.CLOSED))
                .withMessage("startExpression cannot be null");
        assertThatNullPointerException()
                .isThrownBy(() -> JpaIntervalPredicates.periodOverlaps(criteriaBuilder, startExpression, endExpression, "a", "b", null))
                .withMessage("bounds cannot be null");
    }

    private void stubOpenEndedIntervalComposition() {
        when(criteriaBuilder.isNull(endExpression)).thenReturn(nullEndPredicate);
        when(criteriaBuilder.or(nullEndPredicate, endPredicate)).thenReturn(openEndedPredicate);
        when(criteriaBuilder.and(startPredicate, openEndedPredicate)).thenReturn(resultPredicate);
    }

}
