package com.runestone.dynafilter.core.model.statement;

public class NoOpStatement extends AbstractStatement {

    @Override
    public <R> R acceptAnalyser(StatementAnalyser<R> statementAnalyser) {
        return statementAnalyser.analyseNoOpStatement(this);
    }

    @Override
    public void acceptVisitor(StatementVisitor visitor) {
        visitor.visit(this);
    }
}
