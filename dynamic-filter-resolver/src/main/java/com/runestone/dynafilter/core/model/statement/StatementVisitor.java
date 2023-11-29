package com.runestone.dynafilter.core.model.statement;

public interface StatementVisitor {

    void visit(NegatedStatement negatedStatement);

    void visit(CompoundStatement compoundStatement);

    void visit(LogicalStatement logicalStatement);

}
