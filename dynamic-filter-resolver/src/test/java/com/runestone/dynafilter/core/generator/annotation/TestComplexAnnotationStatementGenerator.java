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
import com.runestone.dynafilter.core.generator.annotation.testquery.*;
import com.runestone.dynafilter.core.generator.annotation.tool.FilterDataCounterVisitor;
import com.runestone.dynafilter.core.generator.annotation.tool.NegatedStmtCounterVisitor;
import com.runestone.dynafilter.core.generator.annotation.tool.StatementTypeCounterVisitor;
import com.runestone.dynafilter.core.generator.annotation.tool.ValueFinderVisitor;
import com.runestone.dynafilter.core.model.statement.AbstractStatement;
import com.runestone.dynafilter.core.model.statement.LogicOperator;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class TestComplexAnnotationStatementGenerator {

    @Test
    public void testCreateStmtWithConjunctionAndDisjunction() {
        var annotationStatementInput = new AnnotationStatementInput(FullConstantValuedConjunction.class, null);
        AnnotationStatementGenerator generator = new AnnotationStatementGenerator();

        StatementWrapper statementWrapper = generator.generateStatements(annotationStatementInput, null);
        Assertions.assertThat(statementWrapper).isNotNull();
        AbstractStatement statement = statementWrapper.statement();
        Assertions.assertThat(FilterDataCounterVisitor.count(statement)).isEqualTo(5);
        Assertions.assertThat(StatementTypeCounterVisitor.count(statement, LogicOperator.CONJUNCTION)).isEqualTo(3);
        Assertions.assertThat(StatementTypeCounterVisitor.count(statement, LogicOperator.DISJUNCTION)).isEqualTo(1);
        Assertions.assertThat(NegatedStmtCounterVisitor.countFilters(statement)).isEqualTo(1);
        Assertions.assertThat(ValueFinderVisitor.find("deleted", statement)).isEqualTo(new Object[]{"false"});
        Assertions.assertThat(ValueFinderVisitor.find("name", statement)).isEqualTo(new Object[]{"John"});
        Assertions.assertThat(ValueFinderVisitor.find("status", statement)).isEqualTo(new Object[]{"OK"});
        Assertions.assertThat(ValueFinderVisitor.find("birthday", statement)).isEqualTo(new Object[]{"12/12/2012"});
        Assertions.assertThat(ValueFinderVisitor.find("height", statement)).isEqualTo(new Object[]{"170"});
    }

    @Test
    public void testCreateStmtWithConjunctionAndDisjunctionNegatingAll() {
        var annotationStatementInput = new AnnotationStatementInput(FullConstantValuedConjunctionNegatingAll.class, null);
        AnnotationStatementGenerator generator = new AnnotationStatementGenerator();

        StatementWrapper statementWrapper = generator.generateStatements(annotationStatementInput, null);
        Assertions.assertThat(statementWrapper).isNotNull();
        AbstractStatement statement = statementWrapper.statement();
        Assertions.assertThat(FilterDataCounterVisitor.count(statement)).isEqualTo(5);
        Assertions.assertThat(StatementTypeCounterVisitor.count(statement, LogicOperator.CONJUNCTION)).isEqualTo(3);
        Assertions.assertThat(StatementTypeCounterVisitor.count(statement, LogicOperator.DISJUNCTION)).isEqualTo(1);
        Assertions.assertThat(NegatedStmtCounterVisitor.countFilters(statement)).isEqualTo(8);
        Assertions.assertThat(ValueFinderVisitor.find("deleted", statement)).isEqualTo(new Object[]{"false"});
        Assertions.assertThat(ValueFinderVisitor.find("name", statement)).isEqualTo(new Object[]{"John"});
        Assertions.assertThat(ValueFinderVisitor.find("status", statement)).isEqualTo(new Object[]{"OK"});
        Assertions.assertThat(ValueFinderVisitor.find("birthday", statement)).isEqualTo(new Object[]{"12/12/2012"});
        Assertions.assertThat(ValueFinderVisitor.find("height", statement)).isEqualTo(new Object[]{"170"});
    }

    @Test
    public void testCreateStmtWithDisjunctionAndConjunction() {
        var annotationStatementInput = new AnnotationStatementInput(FullConstantValuedDisjunction.class, null);
        AnnotationStatementGenerator generator = new AnnotationStatementGenerator();

        StatementWrapper statementWrapper = generator.generateStatements(annotationStatementInput, null);
        Assertions.assertThat(statementWrapper).isNotNull();
        AbstractStatement statement = statementWrapper.statement();
        Assertions.assertThat(FilterDataCounterVisitor.count(statement)).isEqualTo(5);
        Assertions.assertThat(StatementTypeCounterVisitor.count(statement, LogicOperator.CONJUNCTION)).isEqualTo(1);
        Assertions.assertThat(StatementTypeCounterVisitor.count(statement, LogicOperator.DISJUNCTION)).isEqualTo(3);
        Assertions.assertThat(NegatedStmtCounterVisitor.countFilters(statement)).isEqualTo(1);
        Assertions.assertThat(ValueFinderVisitor.find("deleted", statement)).isEqualTo(new Object[]{"false"});
        Assertions.assertThat(ValueFinderVisitor.find("name", statement)).isEqualTo(new Object[]{"John"});
        Assertions.assertThat(ValueFinderVisitor.find("status", statement)).isEqualTo(new Object[]{"OK"});
        Assertions.assertThat(ValueFinderVisitor.find("birthday", statement)).isEqualTo(new Object[]{"12/12/2012"});
        Assertions.assertThat(ValueFinderVisitor.find("height", statement)).isEqualTo(new Object[]{"170"});
    }

    @Test
    public void testCreateStmtWithDisjunctionAndConjunctionNegatingAll() {
        var annotationStatementInput = new AnnotationStatementInput(FullConstantValuedDisjunctionNegatingAll.class, null);
        AnnotationStatementGenerator generator = new AnnotationStatementGenerator();

        StatementWrapper statementWrapper = generator.generateStatements(annotationStatementInput, null);
        Assertions.assertThat(statementWrapper).isNotNull();
        AbstractStatement statement = statementWrapper.statement();
        Assertions.assertThat(FilterDataCounterVisitor.count(statement)).isEqualTo(5);
        Assertions.assertThat(StatementTypeCounterVisitor.count(statement, LogicOperator.CONJUNCTION)).isEqualTo(1);
        Assertions.assertThat(StatementTypeCounterVisitor.count(statement, LogicOperator.DISJUNCTION)).isEqualTo(3);
        Assertions.assertThat(NegatedStmtCounterVisitor.countFilters(statement)).isEqualTo(8);
        Assertions.assertThat(ValueFinderVisitor.find("deleted", statement)).isEqualTo(new Object[]{"false"});
        Assertions.assertThat(ValueFinderVisitor.find("name", statement)).isEqualTo(new Object[]{"John"});
        Assertions.assertThat(ValueFinderVisitor.find("status", statement)).isEqualTo(new Object[]{"OK"});
        Assertions.assertThat(ValueFinderVisitor.find("birthday", statement)).isEqualTo(new Object[]{"12/12/2012"});
        Assertions.assertThat(ValueFinderVisitor.find("height", statement)).isEqualTo(new Object[]{"170"});
    }

    @Test
    public void testCreateStmtWithOppositeStmtsOnly() {
        var annotationStatementInput = new AnnotationStatementInput(SearchMovies.class, null);
        AnnotationStatementGenerator generator = new AnnotationStatementGenerator();
        Map<String, Object> map = Map.of(
                "actors", new String[]{"John", "Doe"},
                "director", "John Doe"
        );

        StatementWrapper statementWrapper = generator.generateStatements(annotationStatementInput, map);
        Assertions.assertThat(statementWrapper).isNotNull();
        AbstractStatement statement = statementWrapper.statement();
        Assertions.assertThat(FilterDataCounterVisitor.count(statement)).isEqualTo(2);
        Assertions.assertThat(StatementTypeCounterVisitor.count(statement, LogicOperator.CONJUNCTION)).isEqualTo(0);
        Assertions.assertThat(StatementTypeCounterVisitor.count(statement, LogicOperator.DISJUNCTION)).isEqualTo(1);
        Assertions.assertThat(NegatedStmtCounterVisitor.countFilters(statement)).isEqualTo(0);
        Assertions.assertThat(ValueFinderVisitor.find("actors", statement)).isEqualTo(new Object[]{new Object[]{"John", "Doe"}});
        Assertions.assertThat(ValueFinderVisitor.find("director", statement)).isEqualTo(new Object[]{"John Doe"});
    }

    @Test
    public void testCreateStmtWithEmptyConjunction() {
        var annotationStatementInput = new AnnotationStatementInput(EmptyConjunction.class, null);
        AnnotationStatementGenerator generator = new AnnotationStatementGenerator();

        StatementWrapper statementWrapper = generator.generateStatements(annotationStatementInput, null);
        Assertions.assertThat(statementWrapper).isNull();
    }

    @Test
    public void testCreateStmtFromComplexInterface() {
        var annotationStatementInput = new AnnotationStatementInput(SearchPeopleAndGames.class, null);
        AnnotationStatementGenerator generator = new AnnotationStatementGenerator();
        Map<String, Object> map = Map.of(
                "name", "John",
                "genre", "Action",
                "all", "true",
                "deleted", "false"
        );

        StatementWrapper statementWrapper = generator.generateStatements(annotationStatementInput, map);
        Assertions.assertThat(statementWrapper).isNotNull();
        AbstractStatement statement = statementWrapper.statement();
        Assertions.assertThat(FilterDataCounterVisitor.count(statement)).isEqualTo(6);
        Assertions.assertThat(StatementTypeCounterVisitor.count(statement, LogicOperator.CONJUNCTION)).isEqualTo(4);
        Assertions.assertThat(StatementTypeCounterVisitor.count(statement, LogicOperator.DISJUNCTION)).isEqualTo(1);
        Assertions.assertThat(NegatedStmtCounterVisitor.countFilters(statement)).isEqualTo(0);
        Assertions.assertThat(ValueFinderVisitor.find("name", statement)).isEqualTo(new Object[]{"John"});
        Assertions.assertThat(ValueFinderVisitor.find("genre", statement)).isEqualTo(new Object[]{"Action"});
        Assertions.assertThat(ValueFinderVisitor.find("specialClient", statement)).isEqualTo(new Object[]{"${allSpecialClients}"});
        Assertions.assertThat(ValueFinderVisitor.find("priceInterval", statement)).isEqualTo(new Object[]{"0", "1000"});
        Assertions.assertThat(ValueFinderVisitor.find("all", statement)).isEqualTo(new Object[]{"true"});
        Assertions.assertThat(ValueFinderVisitor.find("deleted", statement)).isEqualTo(new Object[]{"false"});
    }

}
