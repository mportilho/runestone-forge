package com.runestone.dynafilter.core.statement;

public abstract sealed class AbstractStatement permits CompoundStatement, LogicalStatement, NegatedStatement, NoOpStatement {
}
