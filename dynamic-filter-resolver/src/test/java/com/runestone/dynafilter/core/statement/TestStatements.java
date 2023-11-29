package com.runestone.dynafilter.core.statement;

import com.runestone.dynafilter.core.model.FilterData;
import com.runestone.dynafilter.core.model.statement.*;
import com.runestone.dynafilter.core.operation.types.Equals;
import com.runestone.dynafilter.core.operation.types.Like;
import com.runestone.dynafilter.core.statement.tool.StringStatementAnalyser;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestStatements {

    @Test
    public void testStatements() {
        var stringStatementAnalyser = new StringStatementAnalyser();
        var filterDataName = FilterData.of("name", new String[]{"clientName"}, String.class, Like.class, new Object[]{"Joe"});
        var filterDataJob = FilterData.of("job", new String[]{"clientJob"}, String.class, Equals.class, new Object[]{"Developer"});
        var compoundStatementNode = getCompoundStatementNode(filterDataName, filterDataJob);
        Assertions.assertThat(compoundStatementNode.acceptAnalyser(stringStatementAnalyser))
                .isEqualTo("((name Like Joe CONJUNCTION job Equals Developer) DISJUNCTION (name Like Joe CONJUNCTION job Equals Developer))");
    }

    @Test
    public void testStatementsWithNegation() {
        var stringStatementAnalyser = new StringStatementAnalyser();
        var filterDataName = new FilterData("name", new String[]{"clientName"}, String.class, Like.class, true, new Object[]{"Joe"}, null, "");
        var filterDataJob = new FilterData("job", new String[]{"clientJob"}, String.class, Equals.class, true, new Object[]{"Developer"}, null, "");

        var compoundStatementNode = getCompoundStatementNode(filterDataName, filterDataJob);
        Assertions.assertThat(compoundStatementNode.acceptAnalyser(stringStatementAnalyser))
                .isEqualTo("((!name Like Joe CONJUNCTION !job Equals Developer) DISJUNCTION (!name Like Joe CONJUNCTION !job Equals Developer))");
    }

    private static CompoundStatement getCompoundStatementNode(FilterData filterDataName, FilterData filterDataJob) {
        AbstractStatement logicalStatementName = new LogicalStatement(filterDataName);
        AbstractStatement logicalStatementJob = new LogicalStatement(filterDataJob);

        logicalStatementName = filterDataName.negate() ? new NegatedStatement(logicalStatementName) : logicalStatementName;
        logicalStatementJob = filterDataJob.negate() ? new NegatedStatement(logicalStatementJob) : logicalStatementJob;

        var compoundStatementLeft = new CompoundStatement(logicalStatementName, logicalStatementJob, LogicOperator.CONJUNCTION);
        var compoundStatementRight = new CompoundStatement(logicalStatementName, logicalStatementJob, LogicOperator.CONJUNCTION);
        return new CompoundStatement(compoundStatementLeft, compoundStatementRight, LogicOperator.DISJUNCTION);
    }

}
