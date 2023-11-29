package com.runestone.dynafilter.core.generator.annotation.tool;

import com.runestone.dynafilter.core.model.statement.*;

public class NegatedStmtCounterVisitor implements StatementVisitor {

    private int count = 0;

    public static int countFilters(AbstractStatement statement) {
        NegatedStmtCounterVisitor visitor = new NegatedStmtCounterVisitor();
        statement.acceptVisitor(visitor);
        return visitor.getCount();
    }

    @Override
    public void visit(NegatedStatement negatedStatement) {
        negatedStatement.getStatement().acceptVisitor(this);
        count++;
    }

    @Override
    public void visit(CompoundStatement compoundStatement) {
        // do nothing
    }

    @Override
    public void visit(LogicalStatement logicalStatement) {
        // do nothing
    }

    public int getCount() {
        return count;
    }
}
