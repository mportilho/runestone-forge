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

import com.runestone.converters.DataConversionService;
import com.runestone.dynafilter.core.exceptions.DynamicFilterConfigurationException;
import com.runestone.dynafilter.core.model.FilterData;
import com.runestone.dynafilter.core.operation.types.Equals;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestJpaSpecifications {

    @Mock
    private DataConversionService conversionService;

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Mock
    @SuppressWarnings("rawtypes")
    private CriteriaQuery query;

    @Mock
    @SuppressWarnings("rawtypes")
    private Root root;

    @Mock
    @SuppressWarnings("rawtypes")
    private Path startPath;

    @Mock
    @SuppressWarnings("rawtypes")
    private Path endPath;

    @Mock
    @SuppressWarnings("rawtypes")
    private Path valuePath;

    @Mock
    private Predicate startPredicate;

    @Mock
    private Predicate endPredicate;

    @Mock
    private Predicate nullEndPredicate;

    @Mock
    private Predicate nullValuePredicate;

    @Mock
    private Predicate comparisonPredicate;

    @Mock
    private Predicate openEndedPredicate;

    @Mock
    private Predicate resultPredicate;

    @Test
    @DisplayName("effectiveAt converts the reference value to each interval path type")
    @SuppressWarnings({"rawtypes", "unchecked"})
    void testEffectiveAtConvertsReferenceToEachIntervalPathType() {
        LocalDate startReference = LocalDate.of(2024, 1, 1);
        LocalDateTime endReference = LocalDateTime.of(2024, 1, 1, 12, 0);
        FilterData filterData = filterData(new String[]{"start", "end"}, new String[]{"reference"}, new Object[]{"raw-reference"});
        stubSimplePath("start", startPath, LocalDate.class);
        stubSimplePath("end", endPath, LocalDateTime.class);
        when(conversionService.convert("raw-reference", LocalDate.class)).thenReturn(startReference);
        when(conversionService.convert("raw-reference", LocalDateTime.class)).thenReturn(endReference);
        when(criteriaBuilder.lessThanOrEqualTo(any(Expression.class), eq(startReference))).thenReturn(startPredicate);
        when(criteriaBuilder.greaterThanOrEqualTo(any(Expression.class), eq(endReference))).thenReturn(endPredicate);
        stubOpenEndedComposition(endPath);

        Predicate result = JpaSpecifications.effectiveAt(filterData, conversionService, IntervalBounds.CLOSED)
                .toPredicate(root, query, criteriaBuilder);

        assertThat(result).isSameAs(resultPredicate);
        verify(conversionService).convert("raw-reference", LocalDate.class);
        verify(conversionService).convert("raw-reference", LocalDateTime.class);
    }

    @Test
    @DisplayName("periodOverlaps converts filter end to the start path type and filter start to the end path type")
    @SuppressWarnings({"rawtypes", "unchecked"})
    void testPeriodOverlapsConvertsValuesToOppositeBoundaryPathTypes() {
        LocalDate convertedFilterEnd = LocalDate.of(2024, 1, 31);
        LocalDateTime convertedFilterStart = LocalDateTime.of(2024, 1, 1, 0, 0);
        FilterData filterData = filterData(new String[]{"start", "end"}, new String[]{"filterStart", "filterEnd"}, new Object[]{"raw-start", "raw-end"});
        stubSimplePath("start", startPath, LocalDate.class);
        stubSimplePath("end", endPath, LocalDateTime.class);
        when(conversionService.convert("raw-end", LocalDate.class)).thenReturn(convertedFilterEnd);
        when(conversionService.convert("raw-start", LocalDateTime.class)).thenReturn(convertedFilterStart);
        when(criteriaBuilder.lessThan(any(Expression.class), eq(convertedFilterEnd))).thenReturn(startPredicate);
        when(criteriaBuilder.greaterThan(any(Expression.class), eq(convertedFilterStart))).thenReturn(endPredicate);
        stubOpenEndedComposition(endPath);

        Predicate result = JpaSpecifications.periodOverlaps(filterData, conversionService, IntervalBounds.START_INCLUSIVE_END_EXCLUSIVE)
                .toPredicate(root, query, criteriaBuilder);

        assertThat(result).isSameAs(resultPredicate);
        verify(conversionService).convert("raw-end", LocalDate.class);
        verify(conversionService).convert("raw-start", LocalDateTime.class);
    }

    @Test
    @DisplayName("nullOrComparison converts the value to the path type and combines null with comparison")
    @SuppressWarnings({"rawtypes", "unchecked"})
    void testNullOrComparisonConvertsValueAndCombinesNullWithComparison() {
        FilterData filterData = filterData(new String[]{"age"}, new String[]{"age"}, new Object[]{"18"});
        stubSimplePath("age", valuePath, Integer.class);
        when(conversionService.convert("18", Integer.class)).thenReturn(18);
        when(criteriaBuilder.isNull(valuePath)).thenReturn(nullValuePredicate);
        when(criteriaBuilder.lt(any(Expression.class), eq(18))).thenReturn(comparisonPredicate);
        when(criteriaBuilder.or(nullValuePredicate, comparisonPredicate)).thenReturn(resultPredicate);

        Predicate result = JpaSpecifications.nullOrComparison(filterData, conversionService, ComparisonOperator.LESS_THAN)
                .toPredicate(root, query, criteriaBuilder);

        assertThat(result).isSameAs(resultPredicate);
        verify(conversionService).convert("18", Integer.class);
        verify(criteriaBuilder).or(nullValuePredicate, comparisonPredicate);
    }

    @Test
    @DisplayName("specification factories reject null mandatory arguments")
    void testSpecificationFactoriesRejectNullMandatoryArguments() {
        FilterData filterData = filterData(new String[]{"age"}, new String[]{"age"}, new Object[]{"18"});

        assertThatNullPointerException()
                .isThrownBy(() -> JpaSpecifications.effectiveAt(null, conversionService, IntervalBounds.CLOSED))
                .withMessage("filterData cannot be null");
        assertThatNullPointerException()
                .isThrownBy(() -> JpaSpecifications.periodOverlaps(filterData, null, IntervalBounds.CLOSED))
                .withMessage("conversionService cannot be null");
        assertThatNullPointerException()
                .isThrownBy(() -> JpaSpecifications.nullOrComparison(filterData, conversionService, null))
                .withMessage("operator cannot be null");
    }

    @Test
    @DisplayName("effectiveAt validates the expected filter shape when executed")
    void testEffectiveAtValidatesExpectedFilterShapeWhenExecuted() {
        FilterData filterData = filterData(new String[]{"start"}, new String[]{"reference"}, new Object[]{"raw-reference"});

        assertThatExceptionOfType(DynamicFilterConfigurationException.class)
                .isThrownBy(() -> JpaSpecifications.effectiveAt(filterData, conversionService, IntervalBounds.CLOSED)
                        .toPredicate(root, query, criteriaBuilder))
                .withMessage("EffectiveAtClosed requires exactly 2 path(s)");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void stubSimplePath(String attribute, Path path, Class<?> javaType) {
        when(root.getJavaType()).thenReturn(Object.class);
        when(root.get(attribute)).thenReturn(path);
        when(path.getJavaType()).thenReturn(javaType);
    }

    private void stubOpenEndedComposition(Path<?> nullableEndPath) {
        when(criteriaBuilder.isNull(nullableEndPath)).thenReturn(nullEndPredicate);
        when(criteriaBuilder.or(nullEndPredicate, endPredicate)).thenReturn(openEndedPredicate);
        when(criteriaBuilder.and(startPredicate, openEndedPredicate)).thenReturn(resultPredicate);
    }

    private static FilterData filterData(String[] paths, String[] parameters, Object[] values) {
        return new FilterData(paths, parameters, Object.class, Equals.class, false, values, null, null);
    }

}
