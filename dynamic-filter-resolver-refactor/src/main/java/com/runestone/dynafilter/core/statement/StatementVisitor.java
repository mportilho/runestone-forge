package com.runestone.dynafilter.core.statement;

public interface StatementVisitor<R> {

    R visit(LogicalStatement statement);

    R visit(CompoundStatement statement);

    R visit(NegatedStatement statement);

    R visit(NoOpStatement statement);
}
