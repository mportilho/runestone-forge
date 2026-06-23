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

import java.util.Objects;

public final class JpaIntervalPredicates {

    private JpaIntervalPredicates() {
    }

    public static Predicate effectiveAt(
            CriteriaBuilder criteriaBuilder,
            Expression<?> startExpression,
            Expression<?> endExpression,
            Object startReference,
            Object endReference,
            IntervalBounds bounds
    ) {
        Objects.requireNonNull(criteriaBuilder, "criteriaBuilder cannot be null");
        Objects.requireNonNull(startExpression, "startExpression cannot be null");
        Objects.requireNonNull(endExpression, "endExpression cannot be null");
        Objects.requireNonNull(bounds, "bounds cannot be null");

        Predicate started = switch (bounds) {
            case CLOSED, START_INCLUSIVE_END_EXCLUSIVE -> JpaComparisons.lessThanOrEqualTo(criteriaBuilder, startExpression, startReference);
            case OPEN, START_EXCLUSIVE_END_INCLUSIVE -> JpaComparisons.lessThan(criteriaBuilder, startExpression, startReference);
        };
        Predicate endMatches = switch (bounds) {
            case CLOSED, START_EXCLUSIVE_END_INCLUSIVE -> JpaComparisons.greaterThanOrEqualTo(criteriaBuilder, endExpression, endReference);
            case OPEN, START_INCLUSIVE_END_EXCLUSIVE -> JpaComparisons.greaterThan(criteriaBuilder, endExpression, endReference);
        };
        return criteriaBuilder.and(started, criteriaBuilder.or(criteriaBuilder.isNull(endExpression), endMatches));
    }

    public static Predicate periodOverlaps(
            CriteriaBuilder criteriaBuilder,
            Expression<?> periodStartExpression,
            Expression<?> periodEndExpression,
            Object filterStartForEndExpression,
            Object filterEndForStartExpression,
            IntervalBounds bounds
    ) {
        Objects.requireNonNull(criteriaBuilder, "criteriaBuilder cannot be null");
        Objects.requireNonNull(periodStartExpression, "periodStartExpression cannot be null");
        Objects.requireNonNull(periodEndExpression, "periodEndExpression cannot be null");
        Objects.requireNonNull(bounds, "bounds cannot be null");

        Predicate startBeforeFilterEnd = switch (bounds) {
            case CLOSED -> JpaComparisons.lessThanOrEqualTo(criteriaBuilder, periodStartExpression, filterEndForStartExpression);
            case OPEN, START_INCLUSIVE_END_EXCLUSIVE, START_EXCLUSIVE_END_INCLUSIVE -> JpaComparisons.lessThan(criteriaBuilder, periodStartExpression, filterEndForStartExpression);
        };
        Predicate endAfterFilterStart = switch (bounds) {
            case CLOSED -> JpaComparisons.greaterThanOrEqualTo(criteriaBuilder, periodEndExpression, filterStartForEndExpression);
            case OPEN, START_INCLUSIVE_END_EXCLUSIVE, START_EXCLUSIVE_END_INCLUSIVE -> JpaComparisons.greaterThan(criteriaBuilder, periodEndExpression, filterStartForEndExpression);
        };
        return criteriaBuilder.and(startBeforeFilterEnd, criteriaBuilder.or(criteriaBuilder.isNull(periodEndExpression), endAfterFilterStart));
    }

}
