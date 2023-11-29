package com.runestone.dynafilter.modules.jpa.resolver;

import com.runestone.dynafilter.core.model.statement.CompoundStatement;
import com.runestone.dynafilter.core.model.statement.LogicalStatement;
import com.runestone.dynafilter.core.model.statement.NegatedStatement;
import com.runestone.dynafilter.core.model.statement.StatementAnalyser;
import com.runestone.dynafilter.core.operation.FilterOperationService;
import org.springframework.data.jpa.domain.Specification;

public class SpecificationStatementAnalyser implements StatementAnalyser<Specification<?>> {

    private final FilterOperationService<Specification<?>> filterOperationService;

    public SpecificationStatementAnalyser(FilterOperationService<Specification<?>> filterOperationService) {
        this.filterOperationService = filterOperationService;
    }

    @Override
    public Specification<?> analyseNegationStatement(NegatedStatement negatedStatement) {
        Specification<?> specification = negatedStatement.getStatement().acceptAnalyser(this);
        return Specification.not(specification);
    }

    @Override
    public Specification<?> analyseLogicalStatement(LogicalStatement logicalStatement) {
        return filterOperationService.createFilter(logicalStatement.getFilterData());
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Specification<?> analyseCompoundStatement(CompoundStatement compoundStatement) {
        Specification leftSpecification = compoundStatement.getLeftStatement().acceptAnalyser(this);
        Specification rightSpecification = compoundStatement.getRightStatement().acceptAnalyser(this);
        return compoundStatement.getLogicOperator().isConjunction() ? leftSpecification.and(rightSpecification) : leftSpecification.or(rightSpecification);
    }

}
