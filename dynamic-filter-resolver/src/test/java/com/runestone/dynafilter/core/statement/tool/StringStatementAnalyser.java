package com.runestone.dynafilter.core.statement.tool;

import com.runestone.dynafilter.core.model.statement.CompoundStatement;
import com.runestone.dynafilter.core.model.statement.LogicalStatement;
import com.runestone.dynafilter.core.model.statement.NegatedStatement;
import com.runestone.dynafilter.core.model.statement.StatementAnalyser;

public class StringStatementAnalyser implements StatementAnalyser<String> {

    @Override
    public String analyseNegationStatement(NegatedStatement negatedStatement) {
        return "!" + negatedStatement.getStatement().acceptAnalyser(this);
    }

    @Override
    public String analyseLogicalStatement(LogicalStatement logicalStatement) {
        return logicalStatement.getFilterData().path() + " " +
                logicalStatement.getFilterData().operation().getSimpleName() + " " +
                logicalStatement.getFilterData().findOneValue();
    }

    @Override
    public String analyseCompoundStatement(CompoundStatement compoundStatement) {
        return "(" +
                compoundStatement.getLeftStatement().acceptAnalyser(this) +
                " " +
                compoundStatement.getLogicOperator().name() +
                " " +
                compoundStatement.getRightStatement().acceptAnalyser(this) +
                ")";
    }
}
