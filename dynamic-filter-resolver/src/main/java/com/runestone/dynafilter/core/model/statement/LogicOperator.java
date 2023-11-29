package com.runestone.dynafilter.core.model.statement;

public enum LogicOperator {

    CONJUNCTION, DISJUNCTION;

    public LogicOperator opposite() {
        return CONJUNCTION.equals(this) ? DISJUNCTION : CONJUNCTION;
    }

    public boolean isConjunction() {
        return CONJUNCTION.equals(this);
    }

}
