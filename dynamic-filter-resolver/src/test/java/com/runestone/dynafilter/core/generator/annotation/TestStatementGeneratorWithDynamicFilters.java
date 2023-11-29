package com.runestone.dynafilter.core.generator.annotation;

import com.runestone.dynafilter.core.exceptions.StatementGenerationException;
import com.runestone.dynafilter.core.generator.StatementWrapper;
import com.runestone.dynafilter.core.generator.annotation.testdata.interfaces.SearchDynamic;
import com.runestone.dynafilter.core.generator.annotation.tool.FilterDataCounterVisitor;
import com.runestone.dynafilter.core.generator.annotation.tool.NegatedStmtCounterVisitor;
import com.runestone.dynafilter.core.generator.annotation.tool.StatementTypeCounterVisitor;
import com.runestone.dynafilter.core.generator.annotation.tool.ValueFinderVisitor;
import com.runestone.dynafilter.core.model.statement.AbstractStatement;
import com.runestone.dynafilter.core.model.statement.LogicOperator;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class TestStatementGeneratorWithDynamicFilters {

    @Test
    public void testCreateStmtWithPositiveDynamicOperation() {
        var annotationStatementInput = new AnnotationStatementInput(null, SearchDynamic.class.getAnnotations());
        AnnotationStatementGenerator generator = new AnnotationStatementGenerator();
        Map<String, Object> map = Map.of(
                "name", "John",
                "age", new Object[]{"ge", 18}
        );

        StatementWrapper statementWrapper = generator.generateStatements(annotationStatementInput, map);
        Assertions.assertThat(statementWrapper).isNotNull();
        AbstractStatement statement = statementWrapper.statement();
        Assertions.assertThat(FilterDataCounterVisitor.count(statement)).isEqualTo(2);
        Assertions.assertThat(StatementTypeCounterVisitor.count(statement, LogicOperator.CONJUNCTION)).isEqualTo(1);
        Assertions.assertThat(StatementTypeCounterVisitor.count(statement, LogicOperator.DISJUNCTION)).isEqualTo(0);
        Assertions.assertThat(NegatedStmtCounterVisitor.countFilters(statement)).isEqualTo(0);
        Assertions.assertThat(ValueFinderVisitor.find("name", statement)).isEqualTo(new Object[]{"John"});
        Assertions.assertThat(ValueFinderVisitor.find("age", statement)).isEqualTo(new Object[]{18});
    }

    @Test
    public void testCreateStmtWithNegativeDynamicOperation() {
        var annotationStatementInput = new AnnotationStatementInput(null, SearchDynamic.class.getAnnotations());
        AnnotationStatementGenerator generator = new AnnotationStatementGenerator();
        Map<String, Object> map = Map.of(
                "name", "John",
                "age", new Object[]{"nlt", 18}
        );

        StatementWrapper statementWrapper = generator.generateStatements(annotationStatementInput, map);
        Assertions.assertThat(statementWrapper).isNotNull();
        AbstractStatement statement = statementWrapper.statement();
        Assertions.assertThat(FilterDataCounterVisitor.count(statement)).isEqualTo(2);
        Assertions.assertThat(StatementTypeCounterVisitor.count(statement, LogicOperator.CONJUNCTION)).isEqualTo(1);
        Assertions.assertThat(StatementTypeCounterVisitor.count(statement, LogicOperator.DISJUNCTION)).isEqualTo(0);
        Assertions.assertThat(NegatedStmtCounterVisitor.countFilters(statement)).isEqualTo(1);
        Assertions.assertThat(ValueFinderVisitor.find("name", statement)).isEqualTo(new Object[]{"John"});
        Assertions.assertThat(ValueFinderVisitor.find("age", statement)).isEqualTo(new Object[]{18});
    }

    @Test
    public void testCreateStmtWithIsInOperationSingleArray() {
        var annotationStatementInput = new AnnotationStatementInput(null, SearchDynamic.class.getAnnotations());
        AnnotationStatementGenerator generator = new AnnotationStatementGenerator();
        Map<String, Object> map = Map.of(
                "name", "John",
                "age", new Object[]{"in", 18, 19, 20, 21}
        );

        StatementWrapper statementWrapper = generator.generateStatements(annotationStatementInput, map);
        Assertions.assertThat(statementWrapper).isNotNull();
        AbstractStatement statement = statementWrapper.statement();
        Assertions.assertThat(FilterDataCounterVisitor.count(statement)).isEqualTo(2);
        Assertions.assertThat(StatementTypeCounterVisitor.count(statement, LogicOperator.CONJUNCTION)).isEqualTo(1);
        Assertions.assertThat(StatementTypeCounterVisitor.count(statement, LogicOperator.DISJUNCTION)).isEqualTo(0);
        Assertions.assertThat(NegatedStmtCounterVisitor.countFilters(statement)).isEqualTo(0);
        Assertions.assertThat(ValueFinderVisitor.find("name", statement)).isEqualTo(new Object[]{"John"});
        Assertions.assertThat(ValueFinderVisitor.find("age", statement)).isEqualTo(new Object[]{new Object[]{18, 19, 20, 21}});
    }

    @Test
    public void testCreateStmtWithIsInOperationSingleArrayOneValue() {
        var annotationStatementInput = new AnnotationStatementInput(null, SearchDynamic.class.getAnnotations());
        AnnotationStatementGenerator generator = new AnnotationStatementGenerator();
        Map<String, Object> map = Map.of(
                "name", "John",
                "age", new Object[]{"in", 18}
        );

        StatementWrapper statementWrapper = generator.generateStatements(annotationStatementInput, map);
        Assertions.assertThat(statementWrapper).isNotNull();
        AbstractStatement statement = statementWrapper.statement();
        Assertions.assertThat(FilterDataCounterVisitor.count(statement)).isEqualTo(2);
        Assertions.assertThat(StatementTypeCounterVisitor.count(statement, LogicOperator.CONJUNCTION)).isEqualTo(1);
        Assertions.assertThat(StatementTypeCounterVisitor.count(statement, LogicOperator.DISJUNCTION)).isEqualTo(0);
        Assertions.assertThat(NegatedStmtCounterVisitor.countFilters(statement)).isEqualTo(0);
        Assertions.assertThat(ValueFinderVisitor.find("name", statement)).isEqualTo(new Object[]{"John"});
        Assertions.assertThat(ValueFinderVisitor.find("age", statement)).isEqualTo(new Object[]{new Object[]{18}});
    }

    @Test
    public void testCreateStmtWithIsInOperationMultiArrayAndNegative() {
        var annotationStatementInput = new AnnotationStatementInput(null, SearchDynamic.class.getAnnotations());
        AnnotationStatementGenerator generator = new AnnotationStatementGenerator();
        Map<String, Object> map = Map.of(
                "name", "John",
                "age", new Object[]{"nin", new Object[]{18, 19, 20, 21}}
        );

        StatementWrapper statementWrapper = generator.generateStatements(annotationStatementInput, map);
        Assertions.assertThat(statementWrapper).isNotNull();
        AbstractStatement statement = statementWrapper.statement();
        Assertions.assertThat(FilterDataCounterVisitor.count(statement)).isEqualTo(2);
        Assertions.assertThat(StatementTypeCounterVisitor.count(statement, LogicOperator.CONJUNCTION)).isEqualTo(1);
        Assertions.assertThat(StatementTypeCounterVisitor.count(statement, LogicOperator.DISJUNCTION)).isEqualTo(0);
        Assertions.assertThat(NegatedStmtCounterVisitor.countFilters(statement)).isEqualTo(1);
        Assertions.assertThat(ValueFinderVisitor.find("name", statement)).isEqualTo(new Object[]{"John"});
        Assertions.assertThat(ValueFinderVisitor.find("age", statement)).isEqualTo(new Object[]{new Object[]{18, 19, 20, 21}});
    }

    @Test
    public void testCreateStmtWithBetweenOperation() {
        var annotationStatementInput = new AnnotationStatementInput(null, SearchDynamic.class.getAnnotations());
        AnnotationStatementGenerator generator = new AnnotationStatementGenerator();
        Map<String, Object> map = Map.of(
                "name", "John",
                "age", new Object[]{"bt", 18, 200}
        );

        StatementWrapper statementWrapper = generator.generateStatements(annotationStatementInput, map);
        Assertions.assertThat(statementWrapper).isNotNull();
        AbstractStatement statement = statementWrapper.statement();
        Assertions.assertThat(FilterDataCounterVisitor.count(statement)).isEqualTo(2);
        Assertions.assertThat(StatementTypeCounterVisitor.count(statement, LogicOperator.CONJUNCTION)).isEqualTo(1);
        Assertions.assertThat(StatementTypeCounterVisitor.count(statement, LogicOperator.DISJUNCTION)).isEqualTo(0);
        Assertions.assertThat(NegatedStmtCounterVisitor.countFilters(statement)).isEqualTo(0);
        Assertions.assertThat(ValueFinderVisitor.find("name", statement)).isEqualTo(new Object[]{"John"});
        Assertions.assertThat(ValueFinderVisitor.find("age", statement)).isEqualTo(new Object[]{18, 200});
    }

    @Test
    public void testCreateStmtWithBetweenOperationThrownOnInvalidValueQuantity() {
        var annotationStatementInput = new AnnotationStatementInput(null, SearchDynamic.class.getAnnotations());
        AnnotationStatementGenerator generator = new AnnotationStatementGenerator();

        Assertions.assertThatThrownBy(() -> generator.generateStatements(annotationStatementInput, Map.of(
                        "name", "John",
                        "age", new Object[]{"bt", 18}
                )))
                .isExactlyInstanceOf(StatementGenerationException.class)
                .hasMessageContaining("Between operation must have two values");

        Assertions.assertThatThrownBy(() -> generator.generateStatements(annotationStatementInput, Map.of(
                        "name", "John",
                        "age", new Object[]{"bt", 18, 21, 200}
                )))
                .isExactlyInstanceOf(StatementGenerationException.class)
                .hasMessageContaining("Between operation must have two values");
    }

    @Test
    public void testCreateStmtThrowOnInvalidNegativeCharacter() {
        var annotationStatementInput = new AnnotationStatementInput(null, SearchDynamic.class.getAnnotations());
        AnnotationStatementGenerator generator = new AnnotationStatementGenerator();
        Map<String, Object> map = Map.of(
                "name", "John",
                "age", new Object[]{"alt", 18}
        );

        Assertions.assertThatThrownBy(() -> generator.generateStatements(annotationStatementInput, map))
                .isExactlyInstanceOf(StatementGenerationException.class)
                .hasMessageContaining("Invalid negating character [a] on path [age]");
    }

    @Test
    public void testCreateStmtThrowOnHavingOneValueParameters() {
        var annotationStatementInput = new AnnotationStatementInput(null, SearchDynamic.class.getAnnotations());
        AnnotationStatementGenerator generator = new AnnotationStatementGenerator();
        Map<String, Object> map = Map.of(
                "name", "John",
                "age", new Object[]{"ge"}
        );

        Assertions.assertThatThrownBy(() -> generator.generateStatements(annotationStatementInput, map))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Values cannot be null or empty");
    }

    @Test
    public void testCreateStmtThrowOnHavingMoreThanTwoValueParameters() {
        var annotationStatementInput = new AnnotationStatementInput(null, SearchDynamic.class.getAnnotations());
        AnnotationStatementGenerator generator = new AnnotationStatementGenerator();
        Map<String, Object> map = Map.of(
                "name", "John",
                "age", new Object[]{"ge", 18, 21}
        );

        Assertions.assertThatThrownBy(() -> generator.generateStatements(annotationStatementInput, map))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Parameters and values must have the same length");
    }

    @Test
    public void testCreateStmtThrowOnSingleValue() {
        var annotationStatementInput = new AnnotationStatementInput(null, SearchDynamic.class.getAnnotations());
        AnnotationStatementGenerator generator = new AnnotationStatementGenerator();
        Map<String, Object> map = Map.of(
                "name", "John",
                "age", 18
        );

        Assertions.assertThatThrownBy(() -> generator.generateStatements(annotationStatementInput, map))
                .isExactlyInstanceOf(StatementGenerationException.class)
                .hasMessageContaining("Dynamic operation must have two values");
    }

    @Test
    public void testCreateStmtThrowOnNotStringFirstValue() {
        var annotationStatementInput = new AnnotationStatementInput(null, SearchDynamic.class.getAnnotations());
        AnnotationStatementGenerator generator = new AnnotationStatementGenerator();
        Map<String, Object> map = Map.of(
                "name", "John",
                "age", new Object[]{12, 23}
        );

        Assertions.assertThatThrownBy(() -> generator.generateStatements(annotationStatementInput, map))
                .isExactlyInstanceOf(StatementGenerationException.class)
                .hasMessageContaining("Dynamic operation must have a string as first value that indicates the operation");
    }

    @Test
    public void testCreateStmtThrowOnInvalidOperationFirstValue() {
        var annotationStatementInput = new AnnotationStatementInput(null, SearchDynamic.class.getAnnotations());
        AnnotationStatementGenerator generator = new AnnotationStatementGenerator();
        Map<String, Object> map = Map.of(
                "name", "John",
                "age", new Object[]{"QWERTY", 21}
        );

        Assertions.assertThatThrownBy(() -> generator.generateStatements(annotationStatementInput, map))
                .isExactlyInstanceOf(StatementGenerationException.class)
                .hasMessageContaining("Invalid comparison operation [QWERTY] on path [age]");
    }

}
