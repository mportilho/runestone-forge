package com.runestone.dynafilter.core.model.statement;

public class NegatedStatement extends AbstractStatement {

    private final AbstractStatement statement;

    public NegatedStatement(AbstractStatement statement) {
        this.statement = statement;
    }

    @Override
    public <R> R acceptAnalyser(StatementAnalyser<R> statementAnalyser) {
        return statementAnalyser.analyseNegationStatement(this);
    }

    @Override
    public void acceptVisitor(StatementVisitor visitor) {
        visitor.visit(this);
    }

    public AbstractStatement getStatement() {
        return statement;
    }
}
