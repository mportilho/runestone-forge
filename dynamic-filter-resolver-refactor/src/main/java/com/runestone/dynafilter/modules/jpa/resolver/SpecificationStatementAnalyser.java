package com.runestone.dynafilter.modules.jpa.resolver;

import com.runestone.dynafilter.core.operation.FilterOperationService;
import com.runestone.dynafilter.core.statement.CompoundStatement;
import com.runestone.dynafilter.core.statement.LogicOperator;
import com.runestone.dynafilter.core.statement.LogicalStatement;
import com.runestone.dynafilter.core.statement.NegatedStatement;
import com.runestone.dynafilter.core.statement.NoOpStatement;
import com.runestone.dynafilter.core.statement.StatementAnalyser;
import org.springframework.data.jpa.domain.Specification;

import java.util.Objects;

public final class SpecificationStatementAnalyser implements StatementAnalyser<Specification<?>> {

    private final FilterOperationService<Specification<?>> operationService;

    public SpecificationStatementAnalyser(FilterOperationService<Specification<?>> operationService) {
        this.operationService = Objects.requireNonNull(operationService, "operationService must not be null");
    }

    @Override
    public Specification<?> visit(LogicalStatement statement) {
        return operationService.createFilter(statement.filterData());
    }

    @Override
    public Specification<?> visit(CompoundStatement statement) {
        Specification<Object> left = cast(analyse(statement.leftStatement()));
        Specification<Object> right = cast(analyse(statement.rightStatement()));
        if (statement.logicOperator() == LogicOperator.DISJUNCTION) {
            return left.or(right);
        }
        return left.and(right);
    }

    @Override
    public Specification<?> visit(NegatedStatement statement) {
        return Specification.not(analyse(statement.statement()));
    }

    @Override
    public Specification<?> visit(NoOpStatement statement) {
        return Specification.unrestricted();
    }

    @SuppressWarnings("unchecked")
    private static Specification<Object> cast(Specification<?> specification) {
        return (Specification<Object>) specification;
    }
}
