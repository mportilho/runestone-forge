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

package com.runestone.dynafilter.modules.jpa.resolver;

import com.runestone.converters.impl.DefaultDataConversionService;
import com.runestone.dynafilter.core.model.FilterData;
import com.runestone.dynafilter.core.model.statement.CompoundStatement;
import com.runestone.dynafilter.core.model.statement.LogicOperator;
import com.runestone.dynafilter.core.model.statement.LogicalStatement;
import com.runestone.dynafilter.core.model.statement.NegatedStatement;
import com.runestone.dynafilter.core.model.statement.NoOpStatement;
import com.runestone.dynafilter.core.operation.FilterOperationService;
import com.runestone.dynafilter.core.operation.types.Like;
import com.runestone.dynafilter.modules.jpa.operation.SpecificationFilterOperationService;
import com.runestone.dynafilter.modules.jpa.tools.MockStatementFactory;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.jpa.domain.Specification;

import static org.assertj.core.api.Assertions.assertThat;

public class TestSpecificationStatementAnalyser {

    private SpecificationFilterOperationService filterOperationService;

    @BeforeEach
    public void setup() {
        filterOperationService = Mockito.spy(new SpecificationFilterOperationService(new DefaultDataConversionService()));
    }

    @Test
    public void testLogicalStatement() {
        SpecificationStatementAnalyser analyser = new SpecificationStatementAnalyser(filterOperationService);
        LogicalStatement logicalStatementOnName = MockStatementFactory.createLogicalStatementOnName();
        Specification<?> specification = analyser.analyseLogicalStatement(logicalStatementOnName);

        Assertions.assertThat(specification).isNotNull();
        Mockito.verify(filterOperationService, Mockito.times(1)).createFilter(Mockito.eq(logicalStatementOnName.getFilterData()));
    }

    @Test
    public void testNegationStatement() {
        SpecificationStatementAnalyser analyser = new SpecificationStatementAnalyser(filterOperationService);
        LogicalStatement logicalStatementOnName = MockStatementFactory.createLogicalStatementOnName();
        Specification<?> specification = analyser.analyseNegationStatement(new NegatedStatement(logicalStatementOnName));

        Assertions.assertThat(specification).isNotNull();
        Mockito.verify(filterOperationService, Mockito.times(1)).createFilter(Mockito.eq(logicalStatementOnName.getFilterData()));
    }

    @Test
    public void testCompoundStatement() {
        SpecificationStatementAnalyser analyser = new SpecificationStatementAnalyser(filterOperationService);
        LogicalStatement logicalStatementOnName = MockStatementFactory.createLogicalStatementOnName();
        LogicalStatement logicalStatementOnClientJob = MockStatementFactory.createLogicalStatementOnClientJob();
        CompoundStatement compoundStatement = new CompoundStatement(logicalStatementOnName, logicalStatementOnClientJob, LogicOperator.DISJUNCTION);
        Specification<?> specification = analyser.analyseCompoundStatement(compoundStatement);

        Assertions.assertThat(specification).isNotNull();
        Mockito.verify(filterOperationService, Mockito.times(2)).createFilter(Mockito.any());
        Mockito.verify(filterOperationService, Mockito.times(1)).createFilter(Mockito.eq(logicalStatementOnName.getFilterData()));
        Mockito.verify(filterOperationService, Mockito.times(1)).createFilter(Mockito.eq(logicalStatementOnClientJob.getFilterData()));
    }

    @Test
    @DisplayName("Compound statement with conjunction delegates to CriteriaBuilder.and")
    public void testCompoundConjunctionAppliesAndPredicate() {
        FilterOperationService<Specification<?>> operationService = mockOperationService();
        SpecificationStatementAnalyser analyser = new SpecificationStatementAnalyser(operationService);
        LogicalStatement leftStatement = MockStatementFactory.createLogicalStatementOnName();
        LogicalStatement rightStatement = MockStatementFactory.createLogicalStatementOnClientJob();
        Predicate leftPredicate = Mockito.mock(Predicate.class);
        Predicate rightPredicate = Mockito.mock(Predicate.class);
        Predicate combinedPredicate = Mockito.mock(Predicate.class);
        CriteriaBuilder builder = Mockito.mock(CriteriaBuilder.class);
        Root<Object> root = mockRoot();
        CriteriaQuery<?> query = Mockito.mock(CriteriaQuery.class);

        Mockito.doReturn(specificationReturning(leftPredicate)).when(operationService).createFilter(leftStatement.getFilterData());
        Mockito.doReturn(specificationReturning(rightPredicate)).when(operationService).createFilter(rightStatement.getFilterData());
        Mockito.when(builder.and(leftPredicate, rightPredicate)).thenReturn(combinedPredicate);

        Specification<Object> specification = asObjectSpecification(analyser.analyseCompoundStatement(
                new CompoundStatement(leftStatement, rightStatement, LogicOperator.CONJUNCTION)));

        assertThat(specification.toPredicate(root, query, builder)).isSameAs(combinedPredicate);
        Mockito.verify(builder).and(leftPredicate, rightPredicate);
        Mockito.verify(builder, Mockito.never()).or(Mockito.any(), Mockito.any());
    }

    @Test
    @DisplayName("Compound statement with disjunction delegates to CriteriaBuilder.or")
    public void testCompoundDisjunctionAppliesOrPredicate() {
        FilterOperationService<Specification<?>> operationService = mockOperationService();
        SpecificationStatementAnalyser analyser = new SpecificationStatementAnalyser(operationService);
        LogicalStatement leftStatement = MockStatementFactory.createLogicalStatementOnName();
        LogicalStatement rightStatement = MockStatementFactory.createLogicalStatementOnClientJob();
        Predicate leftPredicate = Mockito.mock(Predicate.class);
        Predicate rightPredicate = Mockito.mock(Predicate.class);
        Predicate combinedPredicate = Mockito.mock(Predicate.class);
        CriteriaBuilder builder = Mockito.mock(CriteriaBuilder.class);
        Root<Object> root = mockRoot();
        CriteriaQuery<?> query = Mockito.mock(CriteriaQuery.class);

        Mockito.doReturn(specificationReturning(leftPredicate)).when(operationService).createFilter(leftStatement.getFilterData());
        Mockito.doReturn(specificationReturning(rightPredicate)).when(operationService).createFilter(rightStatement.getFilterData());
        Mockito.when(builder.or(leftPredicate, rightPredicate)).thenReturn(combinedPredicate);

        Specification<Object> specification = asObjectSpecification(analyser.analyseCompoundStatement(
                new CompoundStatement(leftStatement, rightStatement, LogicOperator.DISJUNCTION)));

        assertThat(specification.toPredicate(root, query, builder)).isSameAs(combinedPredicate);
        Mockito.verify(builder).or(leftPredicate, rightPredicate);
        Mockito.verify(builder, Mockito.never()).and(Mockito.any(), Mockito.any());
    }

    @Test
    @DisplayName("Negated statement delegates to CriteriaBuilder.not")
    public void testNegationAppliesNotPredicate() {
        FilterOperationService<Specification<?>> operationService = mockOperationService();
        SpecificationStatementAnalyser analyser = new SpecificationStatementAnalyser(operationService);
        LogicalStatement statement = MockStatementFactory.createLogicalStatementOnName();
        Predicate predicate = Mockito.mock(Predicate.class);
        Predicate negatedPredicate = Mockito.mock(Predicate.class);
        CriteriaBuilder builder = Mockito.mock(CriteriaBuilder.class);
        Root<Object> root = mockRoot();
        CriteriaQuery<?> query = Mockito.mock(CriteriaQuery.class);

        Mockito.doReturn(specificationReturning(predicate)).when(operationService).createFilter(statement.getFilterData());
        Mockito.when(builder.not(predicate)).thenReturn(negatedPredicate);

        Specification<Object> specification = asObjectSpecification(analyser.analyseNegationStatement(new NegatedStatement(statement)));

        assertThat(specification.toPredicate(root, query, builder)).isSameAs(negatedPredicate);
        Mockito.verify(builder).not(predicate);
    }

    @Test
    @DisplayName("NoOp statement produces an unrestricted specification")
    public void testNoOpStatementProducesUnrestrictedSpecification() {
        SpecificationStatementAnalyser analyser = new SpecificationStatementAnalyser(filterOperationService);
        CriteriaBuilder builder = Mockito.mock(CriteriaBuilder.class);
        Root<Object> root = mockRoot();
        CriteriaQuery<?> query = Mockito.mock(CriteriaQuery.class);

        Specification<Object> specification = asObjectSpecification(analyser.analyseNoOpStatement(new NoOpStatement()));

        assertThat(specification.toPredicate(root, query, builder)).isNull();
        Mockito.verifyNoInteractions(builder);
    }

    @Test
    @DisplayName("Deep statement graph composes logical, compound, negated and no-op statements")
    public void testDeepStatementGraphWithEveryStatementType() {
        FilterOperationService<Specification<?>> operationService = mockOperationService();
        SpecificationStatementAnalyser analyser = new SpecificationStatementAnalyser(operationService);
        LogicalStatement nameStatement = logicalStatement("name", "Joe");
        LogicalStatement jobStatement = logicalStatement("job", "Developer");
        LogicalStatement cityStatement = logicalStatement("city", "Belem");
        LogicalStatement statusStatement = logicalStatement("status", "Active");
        Predicate namePredicate = Mockito.mock(Predicate.class, "namePredicate");
        Predicate jobPredicate = Mockito.mock(Predicate.class, "jobPredicate");
        Predicate cityPredicate = Mockito.mock(Predicate.class, "cityPredicate");
        Predicate statusPredicate = Mockito.mock(Predicate.class, "statusPredicate");
        Predicate notJobPredicate = Mockito.mock(Predicate.class, "notJobPredicate");
        Predicate nameAndNotJobPredicate = Mockito.mock(Predicate.class, "nameAndNotJobPredicate");
        Predicate leftBranchPredicate = Mockito.mock(Predicate.class, "leftBranchPredicate");
        Predicate notStatusPredicate = Mockito.mock(Predicate.class, "notStatusPredicate");
        Predicate finalPredicate = Mockito.mock(Predicate.class, "finalPredicate");
        CriteriaBuilder builder = Mockito.mock(CriteriaBuilder.class);
        Root<Object> root = mockRoot();
        CriteriaQuery<?> query = Mockito.mock(CriteriaQuery.class);

        Mockito.doReturn(specificationReturning(namePredicate)).when(operationService).createFilter(nameStatement.getFilterData());
        Mockito.doReturn(specificationReturning(jobPredicate)).when(operationService).createFilter(jobStatement.getFilterData());
        Mockito.doReturn(specificationReturning(cityPredicate)).when(operationService).createFilter(cityStatement.getFilterData());
        Mockito.doReturn(specificationReturning(statusPredicate)).when(operationService).createFilter(statusStatement.getFilterData());
        Mockito.when(builder.not(jobPredicate)).thenReturn(notJobPredicate);
        Mockito.when(builder.and(namePredicate, notJobPredicate)).thenReturn(nameAndNotJobPredicate);
        Mockito.when(builder.or(nameAndNotJobPredicate, cityPredicate)).thenReturn(leftBranchPredicate);
        Mockito.when(builder.not(statusPredicate)).thenReturn(notStatusPredicate);
        Mockito.when(builder.and(leftBranchPredicate, notStatusPredicate)).thenReturn(finalPredicate);

        CompoundStatement graph = new CompoundStatement(
                new CompoundStatement(
                        new CompoundStatement(
                                nameStatement,
                                new NegatedStatement(new CompoundStatement(jobStatement, new NoOpStatement(), LogicOperator.DISJUNCTION)),
                                LogicOperator.CONJUNCTION),
                        new CompoundStatement(new NoOpStatement(), cityStatement, LogicOperator.CONJUNCTION),
                        LogicOperator.DISJUNCTION),
                new NegatedStatement(statusStatement),
                LogicOperator.CONJUNCTION);

        Specification<Object> specification = asObjectSpecification(graph.acceptAnalyser(analyser));

        assertThat(specification.toPredicate(root, query, builder)).isSameAs(finalPredicate);
        Mockito.verify(builder).not(jobPredicate);
        Mockito.verify(builder).and(namePredicate, notJobPredicate);
        Mockito.verify(builder).or(nameAndNotJobPredicate, cityPredicate);
        Mockito.verify(builder).not(statusPredicate);
        Mockito.verify(builder).and(leftBranchPredicate, notStatusPredicate);
        Mockito.verify(builder, Mockito.never()).or(jobPredicate, null);
        Mockito.verify(builder, Mockito.never()).and(null, cityPredicate);
        Mockito.verify(operationService, Mockito.times(4)).createFilter(Mockito.any());
    }

    @SuppressWarnings("unchecked")
    private static FilterOperationService<Specification<?>> mockOperationService() {
        return Mockito.mock(FilterOperationService.class);
    }

    private static Specification<?> specificationReturning(Predicate predicate) {
        return (root, query, builder) -> predicate;
    }

    private static LogicalStatement logicalStatement(String path, String value) {
        return new LogicalStatement(FilterData.of(path, new String[]{path}, String.class, Like.class, new Object[]{value}));
    }

    @SuppressWarnings("unchecked")
    private static Specification<Object> asObjectSpecification(Specification<?> specification) {
        return (Specification<Object>) specification;
    }

    @SuppressWarnings("unchecked")
    private static Root<Object> mockRoot() {
        return Mockito.mock(Root.class);
    }

}
