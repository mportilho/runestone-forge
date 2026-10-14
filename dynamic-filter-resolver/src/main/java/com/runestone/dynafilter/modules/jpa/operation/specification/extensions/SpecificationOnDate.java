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

package com.runestone.dynafilter.modules.jpa.operation.specification.extensions;

import com.runestone.converters.RuntimeDataConversionService;
import com.runestone.dynafilter.core.exceptions.DynamicFilterConfigurationException;
import com.runestone.dynafilter.core.model.FilterData;
import com.runestone.dynafilter.core.operation.support.FilterDataRequirements;
import com.runestone.dynafilter.modules.jpa.support.JpaComparisons;
import com.runestone.dynafilter.modules.jpa.support.JpaPaths;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;

public class SpecificationOnDate<T> implements Specification<T> {

    private static final String OPERATION_NAME = "OnDate";

    private final FilterData filterData;
    private final RuntimeDataConversionService dataConversionService;
    private final ZoneId zoneId;

    public SpecificationOnDate(FilterData filterData, RuntimeDataConversionService dataConversionService, ZoneId zoneId) {
        this.filterData = Objects.requireNonNull(filterData, "filterData cannot be null");
        this.dataConversionService = Objects.requireNonNull(dataConversionService, "dataConversionService cannot be null");
        this.zoneId = Objects.requireNonNull(zoneId, "zoneId cannot be null");
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        FilterDataRequirements.requireShape(filterData, 1, 1, OPERATION_NAME);
        JpaPaths.ResolvedJpaPath<?> resolution = JpaPaths.resolveAttributePath(filterData.path()[0], filterData, root, query);
        Expression<?> expression = resolution.expression();
        LocalDate date = toLocalDate(FilterDataRequirements.requireValue(filterData.findOneValue(), OPERATION_NAME, "date"));

        Class<?> expressionType = expression.getJavaType();
        if (LocalDate.class.equals(expressionType)) {
            return criteriaBuilder.equal(expression, date);
        }
        Object start = startOfDay(expressionType, date);
        Object nextDay = startOfNextDay(expressionType, date);
        return criteriaBuilder.and(
                JpaComparisons.greaterThanOrEqualTo(criteriaBuilder, expression, start),
                JpaComparisons.lessThan(criteriaBuilder, expression, nextDay)
        );
    }

    private LocalDate toLocalDate(Object value) {
        if (value instanceof LocalDate date) {
            return date;
        }
        if (value instanceof LocalDateTime dateTime) {
            return dateTime.toLocalDate();
        }
        if (value instanceof Instant instant) {
            return instant.atZone(zoneId).toLocalDate();
        }
        if (value instanceof java.sql.Date date) {
            return date.toLocalDate();
        }
        if (value instanceof Date date) {
            return date.toInstant().atZone(zoneId).toLocalDate();
        }
        return dataConversionService.convert(value, LocalDate.class);
    }

    private Object startOfDay(Class<?> targetType, LocalDate date) {
        if (LocalDateTime.class.equals(targetType)) {
            return date.atStartOfDay();
        }
        if (Instant.class.equals(targetType)) {
            return date.atStartOfDay(zoneId).toInstant();
        }
        if (Date.class.isAssignableFrom(targetType)) {
            return Date.from(date.atStartOfDay(zoneId).toInstant());
        }
        throw unsupportedType(targetType);
    }

    private Object startOfNextDay(Class<?> targetType, LocalDate date) {
        return startOfDay(targetType, date.plusDays(1));
    }

    private static DynamicFilterConfigurationException unsupportedType(Class<?> targetType) {
        return new DynamicFilterConfigurationException("OnDate supports LocalDate, LocalDateTime, Instant and Date paths, but found %s"
                .formatted(targetType.getCanonicalName()));
    }

}
