package com.runestone.dynafilter.core.generator.annotation.tool;

import com.runestone.dynafilter.core.model.statement.*;

public class StatementTypeCounterVisitor implements StatementVisitor {

    private final LogicOperator logicOperator;
    private int counter = 0;

    public static int count(AbstractStatement statement, LogicOperator logicOperator) {
        StatementTypeCounterVisitor visitor = new StatementTypeCounterVisitor(logicOperator);
        statement.acceptVisitor(visitor);
        return visitor.getCounter();
    }

    public StatementTypeCounterVisitor(LogicOperator logicOperator) {
        this.logicOperator = logicOperator;
    }

    @Override
    public void visit(NegatedStatement negatedStatement) {
        negatedStatement.getStatement().acceptVisitor(this);
    }

    @Override
    public void visit(CompoundStatement compoundStatement) {
        if (compoundStatement.getLogicOperator().equals(logicOperator)) {
            counter++;
        }
    }

    @Override
    public void visit(LogicalStatement logicalStatement) {
        // do nothing
    }

    public int getCounter() {
        return counter;
    }
}
