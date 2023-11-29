package com.runestone.dynafilter.core.generator.annotation;

import com.runestone.dynafilter.core.generator.StatementWrapper;
import com.runestone.dynafilter.core.generator.ValueExpressionResolver;
import com.runestone.dynafilter.core.generator.annotation.testdata.interfaces.StatusOkInterface;
import com.runestone.dynafilter.core.generator.annotation.testquery.SearchGames;
import com.runestone.dynafilter.core.generator.annotation.testquery.SearchMusic;
import com.runestone.dynafilter.core.generator.annotation.testquery.SearchPeople;
import com.runestone.dynafilter.core.generator.annotation.tool.FilterDataCounterVisitor;
import com.runestone.dynafilter.core.generator.annotation.tool.NegatedStmtCounterVisitor;
import com.runestone.dynafilter.core.generator.annotation.tool.StatementTypeCounterVisitor;
import com.runestone.dynafilter.core.generator.annotation.tool.ValueFinderVisitor;
import com.runestone.dynafilter.core.model.statement.AbstractStatement;
import com.runestone.dynafilter.core.model.statement.LogicOperator;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;

public class TestAnnotationStatementGenerator {

    private static ValueExpressionResolver<String> getValueExpressionResolver() {
        return value -> {
            if (value != null) {
                return value.startsWith("${") && value.endsWith("}") ? value.substring(2, value.length() - 1) + "-Resolved" : value;
            }
            return null;
        };
    }

    private static ValueExpressionResolver<Object> getValueExpressionResolver(Map<String, Object> valueMapper) {
        return value -> {
            if (value != null) {
                if (value.startsWith("${") && value.endsWith("}")) {
                    String key = value.substring(2, value.length() - 1);
                    Object result = valueMapper.get(key);
                    return result != null ? result : key + "-Resolved";
                }
                return value;
            }
            return null;
        };
    }

    @Test
    public void testCreateStmtWithNullValuedInput() {
        var annotationStatementInput = new AnnotationStatementInput(null, null);
        AnnotationStatementGenerator generator = new AnnotationStatementGenerator();

        StatementWrapper statementWrapper = generator.generateStatements(annotationStatementInput, null);
        Assertions.assertThat(statementWrapper).isNull();
    }

    @Test
    public void testCreateStmtWithNoProvidedParameters() {
        var annotationStatementInput = new AnnotationStatementInput(SearchPeople.class, null);
        AnnotationStatementGenerator generator = new AnnotationStatementGenerator();

        StatementWrapper statementWrapper = generator.generateStatements(annotationStatementInput, null);
        Assertions.assertThat(statementWrapper).isNull();
    }

    @Test
    public void testCreateStmtWithConstantValueOnly() {
        var annotationStatementInput = new AnnotationStatementInput(null, StatusOkInterface.class.getAnnotations());
        AnnotationStatementGenerator generator = new AnnotationStatementGenerator();

        StatementWrapper statementWrapper = generator.generateStatements(annotationStatementInput, null);
        Assertions.assertThat(statementWrapper).isNotNull();
        AbstractStatement statement = statementWrapper.statement();
        Assertions.assertThat(FilterDataCounterVisitor.count(statement)).isEqualTo(1);
        Assertions.assertThat(StatementTypeCounterVisitor.count(statement, LogicOperator.CONJUNCTION)).isEqualTo(0);
        Assertions.assertThat(StatementTypeCounterVisitor.count(statement, LogicOperator.DISJUNCTION)).isEqualTo(0);
        Assertions.assertThat(NegatedStmtCounterVisitor.countFilters(statement)).isEqualTo(0);
    }

    @Test
    public void testCreateStmtWithConstDefValuesWithOneParam() {
        var annotationStatementInput = new AnnotationStatementInput(SearchGames.class, null);
        AnnotationStatementGenerator generator = new AnnotationStatementGenerator(getValueExpressionResolver(Map.of("allSpecialClients", "false")));
        Map<String, Object> parameters = Map.of("genre", "adventure");

        StatementWrapper statementWrapper = generator.generateStatements(annotationStatementInput, parameters);
        Assertions.assertThat(statementWrapper).isNotNull();
        AbstractStatement statement = statementWrapper.statement();
        Assertions.assertThat(FilterDataCounterVisitor.count(statement)).isEqualTo(3);
        Assertions.assertThat(StatementTypeCounterVisitor.count(statement, LogicOperator.CONJUNCTION)).isEqualTo(2);
        Assertions.assertThat(StatementTypeCounterVisitor.count(statement, LogicOperator.DISJUNCTION)).isEqualTo(0);
        Assertions.assertThat(NegatedStmtCounterVisitor.countFilters(statement)).isEqualTo(0);
        Assertions.assertThat(ValueFinderVisitor.find("genre", statement)).isEqualTo(new Object[]{"adventure"});
        Assertions.assertThat(ValueFinderVisitor.find("specialClient", statement)).isEqualTo(new Object[]{"false"});
        Assertions.assertThat(ValueFinderVisitor.find("priceInterval", statement)).isEqualTo(new Object[]{"0", "1000"});
    }

    @Test
    public void testCreateStmtWithConstDefValuesWithMultiParams() {
        var annotationStatementInput = new AnnotationStatementInput(SearchGames.class, null);
        AnnotationStatementGenerator generator = new AnnotationStatementGenerator(getValueExpressionResolver(Map.of("allSpecialClients", "false")));
        Map<String, Object> parameters = Map.of(
                "genre", "adventure",
                "title", "Jump Master",
                "minDate", LocalDate.of(2021, 1, 1),
                "maxDate", LocalDate.of(2021, 12, 31),
                "minPrice", 10.0,
                "maxPrice", 100.0,
                "tags", new Object[]{"multiplayer", "forgotten-realms", "rpg"}
        );

        StatementWrapper statementWrapper = generator.generateStatements(annotationStatementInput, parameters);
        Assertions.assertThat(statementWrapper).isNotNull();
        AbstractStatement statement = statementWrapper.statement();
        Assertions.assertThat(FilterDataCounterVisitor.count(statement)).isEqualTo(6);
        Assertions.assertThat(StatementTypeCounterVisitor.count(statement, LogicOperator.CONJUNCTION)).isEqualTo(5);
        Assertions.assertThat(StatementTypeCounterVisitor.count(statement, LogicOperator.DISJUNCTION)).isEqualTo(0);
        Assertions.assertThat(NegatedStmtCounterVisitor.countFilters(statement)).isEqualTo(0);
        Assertions.assertThat(ValueFinderVisitor.find("genre", statement)).isEqualTo(new Object[]{"adventure"});
        Assertions.assertThat(ValueFinderVisitor.find("title", statement)).isEqualTo(new Object[]{"Jump Master"});
        Assertions.assertThat(ValueFinderVisitor.find("specialClient", statement)).isEqualTo(new Object[]{"false"});
        Assertions.assertThat(ValueFinderVisitor.find("dateSearchInterval", statement)).isEqualTo(new Object[]{LocalDate.of(2021, 1, 1), LocalDate.of(2021, 12, 31)});
        Assertions.assertThat(ValueFinderVisitor.find("priceInterval", statement)).isEqualTo(new Object[]{10.0, 100.0});
        Assertions.assertThat(ValueFinderVisitor.find("tags", statement)).isEqualTo(new Object[][]{{"multiplayer", "forgotten-realms", "rpg"}});
    }

    @Test
    public void testThrowOnUnfulfilledRequiredParam() {
        var annotationStatementInput = new AnnotationStatementInput(SearchGames.class, null);
        AnnotationStatementGenerator generator = new AnnotationStatementGenerator();
        Assertions.assertThatThrownBy(() -> generator.generateStatements(annotationStatementInput, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("genre");
    }

    @Test
    public void testCreateStmtWithExpressionResolver() {
        var annotationStatementInput = new AnnotationStatementInput(SearchGames.class, null);
        AnnotationStatementGenerator generator = new AnnotationStatementGenerator(getValueExpressionResolver(Map.of("allSpecialClients", true)));
        Map<String, Object> parameters = Map.of("genre", "adventure");

        StatementWrapper statementWrapper = generator.generateStatements(annotationStatementInput, parameters);
        Assertions.assertThat(statementWrapper).isNotNull();
        AbstractStatement statement = statementWrapper.statement();
        Assertions.assertThat(FilterDataCounterVisitor.count(statement)).isEqualTo(3);
        Assertions.assertThat(StatementTypeCounterVisitor.count(statement, LogicOperator.CONJUNCTION)).isEqualTo(2);
        Assertions.assertThat(StatementTypeCounterVisitor.count(statement, LogicOperator.DISJUNCTION)).isEqualTo(0);
        Assertions.assertThat(NegatedStmtCounterVisitor.countFilters(statement)).isEqualTo(0);
        Assertions.assertThat(ValueFinderVisitor.find("genre", statement)).isEqualTo(new Object[]{"adventure"});
        Assertions.assertThat(ValueFinderVisitor.find("specialClient", statement)).isEqualTo(new Object[]{true});
        Assertions.assertThat(ValueFinderVisitor.find("priceInterval", statement)).isEqualTo(new Object[]{"0", "1000"});
    }

    @Test
    public void testCreateStmtWithConstantValueExpressionResolver() {
        var annotationStatementInput = new AnnotationStatementInput(SearchMusic.class, null);
        AnnotationStatementGenerator generator = new AnnotationStatementGenerator(getValueExpressionResolver(Map.of("showOnlyOnSale", "true")));
        Map<String, Object> parameters = Map.of("title", "Down by the River");

        StatementWrapper statementWrapper = generator.generateStatements(annotationStatementInput, parameters);
        Assertions.assertThat(statementWrapper).isNotNull();
        AbstractStatement statement = statementWrapper.statement();
        Assertions.assertThat(FilterDataCounterVisitor.count(statement)).isEqualTo(3);
        Assertions.assertThat(StatementTypeCounterVisitor.count(statement, LogicOperator.CONJUNCTION)).isEqualTo(2);
        Assertions.assertThat(StatementTypeCounterVisitor.count(statement, LogicOperator.DISJUNCTION)).isEqualTo(0);
        Assertions.assertThat(NegatedStmtCounterVisitor.countFilters(statement)).isEqualTo(1);
        Assertions.assertThat(ValueFinderVisitor.find("title", statement)).isEqualTo(new Object[]{"Down by the River"});
        Assertions.assertThat(ValueFinderVisitor.find("onSalePeriod", statement)).isEqualTo(new Object[]{"false"});
        Assertions.assertThat(ValueFinderVisitor.find("priceInterval", statement)).isEqualTo(new Object[]{"minPrice-Resolved", "maxPrice-Resolved"});
    }

}
