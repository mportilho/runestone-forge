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

package com.runestone.dynafilter.core.generator.annotation;

import com.runestone.dynafilter.core.generator.StatementWrapper;
import com.runestone.dynafilter.core.generator.annotation.AnnotationStatementGenerator;
import com.runestone.dynafilter.core.generator.annotation.AnnotationStatementInput;
import com.runestone.dynafilter.core.generator.annotation.testdata.interfaces.SearchWithFilters;
import com.runestone.dynafilter.core.generator.annotation.tool.FilterDataCounterVisitor;
import com.runestone.dynafilter.core.generator.annotation.tool.NegatedStmtCounterVisitor;
import com.runestone.dynafilter.core.generator.annotation.tool.StatementTypeCounterVisitor;
import com.runestone.dynafilter.core.generator.annotation.tool.ValueFinderVisitor;
import com.runestone.dynafilter.core.model.statement.AbstractStatement;
import com.runestone.dynafilter.core.model.statement.LogicOperator;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class TestStatementGenWithFilterDecorators {

    @Test
    public void testStmtGenWithFilterDecoratorsWithoutValue() {
        var annotationStatementInput = new AnnotationStatementInput(SearchWithFilters.class, null);
        AnnotationStatementGenerator generator = new AnnotationStatementGenerator();
        Map<String, Object> parameters = Map.of("name", "John");

        StatementWrapper statementWrapper = generator.generateStatements(annotationStatementInput, parameters);
        Assertions.assertThat(statementWrapper).isNotNull();
        AbstractStatement statement = statementWrapper.statement();
        Assertions.assertThat(FilterDataCounterVisitor.count(statement)).isEqualTo(1);
        Assertions.assertThat(StatementTypeCounterVisitor.count(statement, LogicOperator.CONJUNCTION)).isEqualTo(0);
        Assertions.assertThat(StatementTypeCounterVisitor.count(statement, LogicOperator.DISJUNCTION)).isEqualTo(0);
        Assertions.assertThat(NegatedStmtCounterVisitor.countFilters(statement)).isEqualTo(0);
        Assertions.assertThat(ValueFinderVisitor.find("name", statement)).isEqualTo(new Object[]{"John"});
        Assertions.assertThat(statementWrapper.decoratedFilters()).isEmpty();
    }

    @Test
    public void testStmtGenWithFilterDecoratorsWithValue() {
        var annotationStatementInput = new AnnotationStatementInput(SearchWithFilters.class, null);
        AnnotationStatementGenerator generator = new AnnotationStatementGenerator();
        Map<String, Object> parameters = Map.of(
                "name", "John",
                "decorValue", 123
        );

        StatementWrapper statementWrapper = generator.generateStatements(annotationStatementInput, parameters);
        Assertions.assertThat(statementWrapper).isNotNull();
        AbstractStatement statement = statementWrapper.statement();
        Assertions.assertThat(FilterDataCounterVisitor.count(statement)).isEqualTo(1);
        Assertions.assertThat(StatementTypeCounterVisitor.count(statement, LogicOperator.CONJUNCTION)).isEqualTo(0);
        Assertions.assertThat(StatementTypeCounterVisitor.count(statement, LogicOperator.DISJUNCTION)).isEqualTo(0);
        Assertions.assertThat(NegatedStmtCounterVisitor.countFilters(statement)).isEqualTo(0);
        Assertions.assertThat(ValueFinderVisitor.find("name", statement)).isEqualTo(new Object[]{"John"});
        Assertions.assertThat(ValueFinderVisitor.find("decorValue", statement)).isNull();
        Assertions.assertThat(statementWrapper.decoratedFilters()).hasSize(1);
        Assertions.assertThat(statementWrapper.findDecoratedFilterByPath("decorValue").orElseThrow().values()).isEqualTo(new Object[]{123});
    }

}
