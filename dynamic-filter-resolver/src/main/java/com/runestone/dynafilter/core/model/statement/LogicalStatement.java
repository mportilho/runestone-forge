package com.runestone.dynafilter.core.model.statement;

import com.runestone.dynafilter.core.model.FilterData;

public class LogicalStatement extends AbstractStatement {

    private final FilterData filterData;

    public LogicalStatement(FilterData filterData) {
        this.filterData = filterData;
    }

    @Override
    public <R> R acceptAnalyser(StatementAnalyser<R> statementAnalyser) {
        return statementAnalyser.analyseLogicalStatement(this);
    }

    @Override
    public void acceptVisitor(StatementVisitor visitor) {
        visitor.visit(this);
    }

    public FilterData getFilterData() {
        return filterData;
    }
}
