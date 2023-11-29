package com.runestone.dynafilter.core.generator.annotation.tool;

import com.runestone.dynafilter.core.generator.StatementWrapper;
import com.runestone.dynafilter.core.generator.annotation.AnnotationStatementGenerator;
import com.runestone.dynafilter.core.generator.annotation.AnnotationStatementInput;
import com.runestone.dynafilter.core.generator.annotation.testdata.interfaces.SearchWithFilters;
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
