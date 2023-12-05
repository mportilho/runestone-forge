/*
 * MIT License
 * <p>
 * Copyright (c) 2023-2023 Marcelo Silva Portilho
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
