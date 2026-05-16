package com.runestone.dynafilter.core.statement;

import java.util.Objects;

public final class CompoundStatement extends AbstractStatement {

    private final AbstractStatement leftStatement;
    private final AbstractStatement rightStatement;
    private final LogicOperator logicOperator;

    public CompoundStatement(AbstractStatement leftStatement, AbstractStatement rightStatement, LogicOperator logicOperator) {
        this.leftStatement = Objects.requireNonNull(leftStatement, "leftStatement must not be null");
        this.rightStatement = Objects.requireNonNull(rightStatement, "rightStatement must not be null");
        this.logicOperator = Objects.requireNonNull(logicOperator, "logicOperator must not be null");
    }

    public AbstractStatement leftStatement() {
        return leftStatement;
    }

    public AbstractStatement rightStatement() {
        return rightStatement;
    }

    public LogicOperator logicOperator() {
        return logicOperator;
    }
}
