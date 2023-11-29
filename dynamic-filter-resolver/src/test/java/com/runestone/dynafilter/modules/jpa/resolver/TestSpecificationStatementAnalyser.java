package com.runestone.dynafilter.modules.jpa.resolver;

import com.runestone.converters.impl.DefaultDataConversionService;
import com.runestone.dynafilter.core.model.statement.CompoundStatement;
import com.runestone.dynafilter.core.model.statement.LogicOperator;
import com.runestone.dynafilter.core.model.statement.LogicalStatement;
import com.runestone.dynafilter.core.model.statement.NegatedStatement;
import com.runestone.dynafilter.modules.jpa.operation.SpecificationFilterOperationService;
import com.runestone.dynafilter.modules.jpa.tools.MockStatementFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.jpa.domain.Specification;

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

}
