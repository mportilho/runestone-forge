package com.runestone.dynafilter.core.statement;

import java.util.Objects;

public final class NegatedStatement extends AbstractStatement {

    private final AbstractStatement statement;

    public NegatedStatement(AbstractStatement statement) {
        this.statement = Objects.requireNonNull(statement, "statement must not be null");
    }

    public AbstractStatement statement() {
        return statement;
    }
}
