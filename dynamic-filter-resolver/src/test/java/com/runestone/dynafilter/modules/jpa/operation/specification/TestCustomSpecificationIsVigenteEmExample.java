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
import com.runestone.converters.impl.stable.DefaultDataConversionService;
import com.runestone.dynafilter.core.exceptions.DynamicFilterConfigurationException;
import com.runestone.dynafilter.core.generator.StatementWrapper;
import com.runestone.dynafilter.core.generator.annotation.AnnotationStatementGenerator;
import com.runestone.dynafilter.core.generator.annotation.AnnotationStatementInput;
import com.runestone.dynafilter.core.generator.annotation.Conjunction;
import com.runestone.dynafilter.core.generator.annotation.Filter;
import com.runestone.dynafilter.core.model.FilterData;
import com.runestone.dynafilter.core.model.statement.LogicalStatement;
import com.runestone.dynafilter.core.operation.FilterOperation;
import com.runestone.dynafilter.modules.jpa.support.JpaComparisons;
import com.runestone.dynafilter.modules.jpa.support.JpaPaths;
import com.runestone.dynafilter.modules.jpa.tools.app.database.jpamodels.Person;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestCustomSpecificationIsVigenteEmExample {

    private static final LocalDate DATA_VIGENTE = LocalDate.of(2026, 5, 30);
    private static final DataConversionService CONVERSION_SERVICE = DefaultDataConversionService.standard();

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
    private Path dataInicioVigenciaPath;

    @Mock
    @SuppressWarnings("rawtypes")
    private Path dataFimVigenciaPath;

    @Mock
    private Predicate inicioInclusivoPredicate;

    @Mock
    private Predicate fimExclusivoPredicate;

    @Mock
    private Predicate vigentePredicate;

    @Test
    @DisplayName("IsVigenteEm applies a semi-open validity interval predicate")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void testAppliesSemiOpenIntervalPredicate() {
        setupPredicateStubs();
        SpecificationIsVigenteEm<Person> specification = new SpecificationIsVigenteEm<>(
                filterData(DATA_VIGENTE), CONVERSION_SERVICE
        );

        Predicate predicate = specification.toPredicate(root, query, builder);

        Assertions.assertThat(predicate).isSameAs(vigentePredicate);
        verify(builder).lessThanOrEqualTo(dataInicioVigenciaPath, DATA_VIGENTE);
        verify(builder).greaterThan(dataFimVigenciaPath, DATA_VIGENTE);
        verify(builder).and(inicioInclusivoPredicate, fimExclusivoPredicate);
    }

    @Test
    @DisplayName("Filter annotation maps IsVigenteEm with two validity paths and one date parameter")
    public void testAnnotationMapsTwoPathsAndOneParameter() {
        AnnotationStatementInput input = new AnnotationStatementInput(SearchWithIsVigenteEm.class, null);
        AnnotationStatementGenerator generator = new AnnotationStatementGenerator();

        StatementWrapper statementWrapper = generator.generateStatements(input, Map.of("dataVigente", DATA_VIGENTE));

        Assertions.assertThat(statementWrapper.statement()).isInstanceOf(LogicalStatement.class);
        LogicalStatement statement = (LogicalStatement) statementWrapper.statement();
        FilterData filterData = statement.getFilterData();
        Assertions.assertThat(filterData.path()).containsExactly("dataInicioVigencia", "dataFimVigencia");
        Assertions.assertThat(filterData.parameters()).containsExactly("dataVigente");
        Assertions.assertThat(filterData.operation()).isEqualTo(IsVigenteEm.class);
        Assertions.assertThat(filterData.values()).containsExactly(DATA_VIGENTE);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void setupPredicateStubs() {
        when(root.getJavaType()).thenReturn(Person.class);
        when(root.get("dataInicioVigencia")).thenReturn(dataInicioVigenciaPath);
        when(root.get("dataFimVigencia")).thenReturn(dataFimVigenciaPath);
        when(dataInicioVigenciaPath.getJavaType()).thenReturn(LocalDate.class);
        when(dataFimVigenciaPath.getJavaType()).thenReturn(LocalDate.class);
        when(builder.lessThanOrEqualTo(any(Expression.class), eq(DATA_VIGENTE))).thenReturn(inicioInclusivoPredicate);
        when(builder.greaterThan(any(Expression.class), eq(DATA_VIGENTE))).thenReturn(fimExclusivoPredicate);
        when(builder.and(inicioInclusivoPredicate, fimExclusivoPredicate)).thenReturn(vigentePredicate);
    }

    private static FilterData filterData(Object value) {
        return new FilterData(
                new String[]{"dataInicioVigencia", "dataFimVigencia"},
                new String[]{"dataVigente"},
                LocalDate.class,
                IsVigenteEm.class,
                false,
                new Object[]{value},
                null,
                null
        );
    }

    @Conjunction(@Filter(
            path = {"dataInicioVigencia", "dataFimVigencia"},
            parameters = "dataVigente",
            operation = IsVigenteEm.class
    ))
    private interface SearchWithIsVigenteEm {
    }

    private interface IsVigenteEm<T> extends FilterOperation<T> {
    }

    private static final class SpecificationIsVigenteEm<T> implements Specification<T> {

        private final FilterData filterData;
        private final DataConversionService conversionService;

        private SpecificationIsVigenteEm(FilterData filterData, DataConversionService conversionService) {
            this.filterData = Objects.requireNonNull(filterData, "filterData cannot be null");
            this.conversionService = Objects.requireNonNull(conversionService, "conversionService cannot be null");
        }

        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
            if (filterData.path().length != 2) {
                throw new DynamicFilterConfigurationException("IsVigenteEm requires exactly two paths: dataInicioVigencia and dataFimVigencia");
            }
            JpaPaths.ResolvedJpaPath<? extends Comparable<?>> inicioResolution = JpaPaths.resolveAttributePath(filterData.path()[0], filterData, root);
            JpaPaths.ResolvedJpaPath<? extends Comparable<?>> fimResolution = JpaPaths.resolveAttributePath(filterData.path()[1], filterData, root);
            if (inicioResolution.crossedPluralAssociation() || fimResolution.crossedPluralAssociation()) {
                query.distinct(true);
            }
            Expression<? extends Comparable<?>> inicioExpression = inicioResolution.expression();
            Expression<? extends Comparable<?>> fimExpression = fimResolution.expression();
            Object value = filterData.findOneValue();

            Object inicioValue = conversionService.convert(value, inicioExpression.getJavaType());
            Object fimValue = conversionService.convert(value, fimExpression.getJavaType());
            Predicate inicioInclusivo = JpaComparisons.comparable(
                    inicioExpression, inicioValue, criteriaBuilder::lessThanOrEqualTo, criteriaBuilder::le
            );
            Predicate fimExclusivo = JpaComparisons.comparable(
                    fimExpression, fimValue, criteriaBuilder::greaterThan, criteriaBuilder::gt
            );
            return criteriaBuilder.and(inicioInclusivo, fimExclusivo);
        }
    }
}
