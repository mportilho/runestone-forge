package com.runestone.dynafilter.core.generator.annotation.tool;

import com.runestone.dynafilter.core.model.statement.*;

public class ValueFinderVisitor implements StatementVisitor {

    private final String parameter;
    private Object[] value = null;

    public ValueFinderVisitor(String parameter) {
        this.parameter = parameter;
    }

    public static Object[] find(String parameter, AbstractStatement statement) {
        ValueFinderVisitor visitor = new ValueFinderVisitor(parameter);
        statement.acceptVisitor(visitor);
        return visitor.getValue();
    }

    @Override
    public void visit(NegatedStatement negatedStatement) {
        negatedStatement.getStatement().acceptVisitor(this);
    }

    @Override
    public void visit(CompoundStatement compoundStatement) {
        compoundStatement.getLeftStatement().acceptVisitor(this);
        compoundStatement.getRightStatement().acceptVisitor(this);
    }

    @Override
    public void visit(LogicalStatement logicalStatement) {
        if (logicalStatement.getFilterData().path().equals(parameter)) {
            value = logicalStatement.getFilterData().values();
        }
    }

    public String getParameter() {
        return parameter;
    }

    public Object[] getValue() {
        return value;
    }
}
