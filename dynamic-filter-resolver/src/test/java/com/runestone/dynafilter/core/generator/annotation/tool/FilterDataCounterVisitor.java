package com.runestone.dynafilter.core.generator.annotation.tool;

import com.runestone.dynafilter.core.model.statement.*;

public class FilterDataCounterVisitor implements StatementVisitor {

    private int filterCount = 0;

    public static int count(AbstractStatement statement) {
        FilterDataCounterVisitor visitor = new FilterDataCounterVisitor();
        statement.acceptVisitor(visitor);
        return visitor.getFilterCount();
    }

    @Override
    public void visit(NegatedStatement negatedStatement) {
        negatedStatement.getStatement().acceptVisitor(this);
    }

    @Override
    public void visit(CompoundStatement compoundStatement) {
        // do nothing
    }

    @Override
    public void visit(LogicalStatement logicalStatement) {
        filterCount++;
    }

    public int getFilterCount() {
        return filterCount;
    }
}
