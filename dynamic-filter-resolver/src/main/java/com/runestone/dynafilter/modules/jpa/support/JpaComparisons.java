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
import java.util.function.BiFunction;

public final class JpaComparisons {

    private JpaComparisons() {
    }

    @SuppressWarnings("unchecked")
    public static Predicate comparable(
            Expression<?> expression,
            Object value,
            BiFunction<Expression<? extends Comparable<Object>>, Comparable<Object>, Predicate> comparablePredicateFunction,
            BiFunction<Expression<Number>, Number, Predicate> numberPredicateFunction
    ) {
        Objects.requireNonNull(expression, "expression cannot be null");
        Objects.requireNonNull(comparablePredicateFunction, "comparablePredicateFunction cannot be null");
        Objects.requireNonNull(numberPredicateFunction, "numberPredicateFunction cannot be null");
        if (value instanceof Number number) {
            return numberPredicateFunction.apply((Expression<Number>) expression, number);
        }
        return comparablePredicateFunction.apply((Expression<? extends Comparable<Object>>) expression, (Comparable<Object>) value);
    }

    public static Predicate compare(CriteriaBuilder criteriaBuilder, Expression<?> expression, ComparisonOperator operator, Object value) {
        Objects.requireNonNull(criteriaBuilder, "criteriaBuilder cannot be null");
        Objects.requireNonNull(operator, "operator cannot be null");
        return switch (operator) {
            case LESS_THAN -> lessThan(criteriaBuilder, expression, value);
            case LESS_THAN_OR_EQUAL_TO -> lessThanOrEqualTo(criteriaBuilder, expression, value);
            case GREATER_THAN -> greaterThan(criteriaBuilder, expression, value);
            case GREATER_THAN_OR_EQUAL_TO -> greaterThanOrEqualTo(criteriaBuilder, expression, value);
        };
    }

    public static Predicate lessThan(CriteriaBuilder criteriaBuilder, Expression<?> expression, Object value) {
        return comparable(expression, value, criteriaBuilder::lessThan, criteriaBuilder::lt);
    }

    public static Predicate lessThanOrEqualTo(CriteriaBuilder criteriaBuilder, Expression<?> expression, Object value) {
        return comparable(expression, value, criteriaBuilder::lessThanOrEqualTo, criteriaBuilder::le);
    }

    public static Predicate greaterThan(CriteriaBuilder criteriaBuilder, Expression<?> expression, Object value) {
        return comparable(expression, value, criteriaBuilder::greaterThan, criteriaBuilder::gt);
    }

    public static Predicate greaterThanOrEqualTo(CriteriaBuilder criteriaBuilder, Expression<?> expression, Object value) {
        return comparable(expression, value, criteriaBuilder::greaterThanOrEqualTo, criteriaBuilder::ge);
    }

    public static Predicate nullOr(CriteriaBuilder criteriaBuilder, Expression<?> expression, ComparisonOperator operator, Object value) {
        Objects.requireNonNull(criteriaBuilder, "criteriaBuilder cannot be null");
        Objects.requireNonNull(expression, "expression cannot be null");
        return criteriaBuilder.or(criteriaBuilder.isNull(expression), compare(criteriaBuilder, expression, operator, value));
    }

}
