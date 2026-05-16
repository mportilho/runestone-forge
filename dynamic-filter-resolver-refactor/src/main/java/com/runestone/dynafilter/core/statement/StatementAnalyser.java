package com.runestone.dynafilter.core.statement;

import java.util.Objects;

public interface StatementAnalyser<R> extends StatementVisitor<R> {

    default R analyse(AbstractStatement statement) {
        Objects.requireNonNull(statement, "statement must not be null");
        return switch (statement) {
            case LogicalStatement logicalStatement -> visit(logicalStatement);
            case CompoundStatement compoundStatement -> visit(compoundStatement);
            case NegatedStatement negatedStatement -> visit(negatedStatement);
            case NoOpStatement noOpStatement -> visit(noOpStatement);
        };
    }
}
