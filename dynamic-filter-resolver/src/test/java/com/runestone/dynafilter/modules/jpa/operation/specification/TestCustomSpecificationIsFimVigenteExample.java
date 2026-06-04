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

package com.runestone.dynafilter.modules.jpa.operation.specification;

import com.runestone.converters.DataConversionService;
import com.runestone.converters.impl.DefaultDataConversionService;
import com.runestone.dynafilter.core.exceptions.DynamicFilterConfigurationException;
import com.runestone.dynafilter.core.model.FilterData;
import com.runestone.dynafilter.core.operation.FilterOperation;
import com.runestone.dynafilter.modules.jpa.tools.app.database.jpamodels.Person;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestCustomSpecificationIsFimVigenteExample {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-05-30T10:15:30Z"), ZoneId.of("UTC"));
    private static final DataConversionService CONVERSION_SERVICE = new DefaultDataConversionService();

    @Mock
    private CriteriaBuilder builder;

    @Mock
    @SuppressWarnings("rawtypes")
    private CriteriaQuery query;

    @Mock
    @SuppressWarnings("rawtypes")
    private Root root;

    @Mock
    @SuppressWarnings("rawtypes")
    private Path path;

    @Mock
    private Predicate nullPredicate;

    @Mock
    private Predicate expiredPredicate;

    @Mock
    private Predicate activePredicate;

    @Mock
    private Predicate negatedPredicate;

    @BeforeEach
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void setup() {
        when(root.getJavaType()).thenReturn(Person.class);
        when(root.get("birthday")).thenReturn(path);
        when(path.getJavaType()).thenReturn(LocalDate.class);
        when(builder.isNull(path)).thenReturn(nullPredicate);
        when(builder.lessThan(any(Expression.class), eq(LocalDate.of(2026, 5, 30)))).thenReturn(expiredPredicate);
        when(builder.or(nullPredicate, expiredPredicate)).thenReturn(activePredicate);
    }

    @Test
    @DisplayName("IsFimVigente true applies null or before fixed clock predicate")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void testTrueAppliesNullOrBeforeNowPredicate() {
        SpecificationIsFimVigente<Person> specification = new SpecificationIsFimVigente<>(
                filterData(true), CONVERSION_SERVICE, FIXED_CLOCK
        );

        Predicate predicate = specification.toPredicate(root, query, builder);

        Assertions.assertThat(predicate).isSameAs(activePredicate);
        verify(builder, never()).not(any(Predicate.class));
    }

    @Test
    @DisplayName("IsFimVigente false applies the negation of the true predicate")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void testFalseAppliesNegatedPredicate() {
        when(builder.not(activePredicate)).thenReturn(negatedPredicate);
        SpecificationIsFimVigente<Person> specification = new SpecificationIsFimVigente<>(
                filterData(false), CONVERSION_SERVICE, FIXED_CLOCK
        );

        Predicate predicate = specification.toPredicate(root, query, builder);

        Assertions.assertThat(predicate).isSameAs(negatedPredicate);
        verify(builder).not(activePredicate);
    }

    private static FilterData filterData(boolean value) {
        return FilterData.of("birthday", new String[]{"vigente"}, LocalDate.class, IsFimVigente.class, new Object[]{value});
    }

    private interface IsFimVigente<T> extends FilterOperation<T> {
    }

    private static final class SpecificationIsFimVigente<T> implements Specification<T> {

        private final FilterData filterData;
        private final DataConversionService conversionService;
        private final Clock clock;

        private SpecificationIsFimVigente(FilterData filterData, DataConversionService conversionService, Clock clock) {
            this.filterData = Objects.requireNonNull(filterData, "filterData cannot be null");
            this.conversionService = Objects.requireNonNull(conversionService, "conversionService cannot be null");
            this.clock = Objects.requireNonNull(clock, "clock cannot be null");
        }

        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
            JpaPredicateUtils.PathResolution<?> resolution = JpaPredicateUtils.resolveAttributePath(filterData.path()[0], filterData, root);
            if (resolution.crossedPluralAssociation()) {
                query.distinct(true);
            }
            Expression<?> expression = resolution.expression();
            Object now = nowFor(expression.getJavaType(), clock);
            Predicate activePredicate = criteriaBuilder.or(
                    criteriaBuilder.isNull(expression),
                    JpaPredicateUtils.toComparablePredicate(expression, now, criteriaBuilder::lessThan, criteriaBuilder::lt)
            );
            Boolean active = conversionService.convert(filterData.findOneValue(), Boolean.class);
            return Boolean.TRUE.equals(active) ? activePredicate : criteriaBuilder.not(activePredicate);
        }

        private static Object nowFor(Class<?> pathType, Clock clock) {
            if (Date.class.isAssignableFrom(pathType)) {
                return Date.from(clock.instant());
            }
            if (Instant.class.equals(pathType)) {
                return clock.instant();
            }
            if (LocalDate.class.equals(pathType)) {
                return LocalDate.now(clock);
            }
            if (LocalDateTime.class.equals(pathType)) {
                return LocalDateTime.now(clock);
            }
            if (LocalTime.class.equals(pathType)) {
                return LocalTime.now(clock);
            }
            if (OffsetDateTime.class.equals(pathType)) {
                return OffsetDateTime.now(clock);
            }
            if (ZonedDateTime.class.equals(pathType)) {
                return ZonedDateTime.now(clock);
            }
            throw new DynamicFilterConfigurationException(
                    "Unsupported temporal type for IsFimVigente: " + pathType.getCanonicalName()
            );
        }
    }
}
