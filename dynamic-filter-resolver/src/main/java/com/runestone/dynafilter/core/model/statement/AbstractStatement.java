package com.runestone.dynafilter.core.model.statement;

public abstract class AbstractStatement {

    public abstract <R> R acceptAnalyser(StatementAnalyser<R> statementAnalyser);

    public abstract void acceptVisitor(StatementVisitor visitor);

}
