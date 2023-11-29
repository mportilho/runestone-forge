package com.runestone.dynafilter.core.model.statement;

public class CompoundStatement extends AbstractStatement {

    private final AbstractStatement leftStatement;
    private final AbstractStatement rightStatement;
    private final LogicOperator logicOperator;

    public CompoundStatement(AbstractStatement leftStatement, AbstractStatement rightStatement, LogicOperator logicOperator) {
        this.leftStatement = leftStatement;
        this.rightStatement = rightStatement;
        this.logicOperator = logicOperator;
    }

    @Override
    public <R> R acceptAnalyser(StatementAnalyser<R> statementAnalyser) {
        return statementAnalyser.analyseCompoundStatement(this);
    }

    @Override
    public void acceptVisitor(StatementVisitor visitor) {
        leftStatement.acceptVisitor(visitor);
        rightStatement.acceptVisitor(visitor);
        visitor.visit(this);
    }

    public AbstractStatement getLeftStatement() {
        return leftStatement;
    }

    public AbstractStatement getRightStatement() {
        return rightStatement;
    }

    public LogicOperator getLogicOperator() {
        return logicOperator;
    }

}
