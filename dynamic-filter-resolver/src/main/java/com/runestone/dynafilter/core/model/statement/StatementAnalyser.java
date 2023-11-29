package com.runestone.dynafilter.core.model.statement;

public interface StatementAnalyser<R> {

    R analyseNegationStatement(NegatedStatement negatedStatement);

    R analyseLogicalStatement(LogicalStatement logicalStatement);

    R analyseCompoundStatement(CompoundStatement compoundStatement);

}
