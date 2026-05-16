package com.runestone.dynafilter.core.generator.annotation;

import com.runestone.dynafilter.core.annotation.Conjunction;
import com.runestone.dynafilter.core.annotation.Disjunction;
import com.runestone.dynafilter.core.annotation.Filter;
import com.runestone.dynafilter.core.annotation.Statement;
import com.runestone.dynafilter.core.exception.StatementGenerationException;
import com.runestone.dynafilter.core.generator.StatementWrapper;
import com.runestone.dynafilter.core.operation.Equals;
import com.runestone.dynafilter.core.statement.CompoundStatement;
import com.runestone.dynafilter.core.statement.LogicalStatement;
import com.runestone.dynafilter.core.statement.NegatedStatement;
import com.runestone.dynafilter.core.statement.NoOpStatement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AnnotationStatementGeneratorTest {

    private final AnnotationStatementGenerator generator = new AnnotationStatementGenerator();

    @Test
    @DisplayName("generates NoOpStatement when no filter is applicable")
    void generatesNoOpStatement() {
        StatementWrapper wrapper = generator.generateStatements(input(OptionalFilters.class), Map.of());

        assertThat(wrapper.statement()).isInstanceOf(NoOpStatement.class);
        assertThat(wrapper.allFilters()).hasSize(1);
    }

    @Test
    @DisplayName("generates a logical statement for one applicable filter")
    void generatesLogicalStatement() {
        StatementWrapper wrapper = generator.generateStatements(input(OptionalFilters.class), Map.of("name", "Ada"));

        assertThat(wrapper.statement()).isInstanceOf(LogicalStatement.class);
        LogicalStatement logicalStatement = (LogicalStatement) wrapper.statement();
        assertThat(logicalStatement.filterData().values()).containsExactly("Ada");
    }

    @Test
    @DisplayName("combines multiple root statements with conjunction")
    void combinesMultipleRootStatements() {
        StatementWrapper wrapper = generator.generateStatements(
                new AnnotationStatementInput(null, MultiRootController.class.getDeclaredMethods()[0].getParameters()[0].getAnnotations()),
                Map.of("name", "Ada", "city", "Belem")
        );

        assertThat(wrapper.statement()).isInstanceOf(CompoundStatement.class);
    }

    @Test
    @DisplayName("required filter absent fails instead of generating NoOpStatement")
    void requiredFilterAbsentFails() {
        assertThatThrownBy(() -> generator.generateStatements(input(RequiredFilters.class), Map.of()))
                .isInstanceOf(StatementGenerationException.class)
                .hasMessageContaining("Required filter");
    }

    @Test
    @DisplayName("negated filter is wrapped in NegatedStatement")
    void negatedFilterIsWrapped() {
        StatementWrapper wrapper = generator.generateStatements(input(NegatedFilters.class), Map.of("name", "Ada"));

        assertThat(wrapper.statement()).isInstanceOf(NegatedStatement.class);
    }

    private static AnnotationStatementInput input(Class<?> type) {
        return new AnnotationStatementInput(type, type.getAnnotations());
    }

    @Conjunction(@Filter(path = "name", parameters = "name", operation = Equals.class))
    private static class OptionalFilters {
    }

    @Conjunction(@Filter(path = "name", parameters = "name", operation = Equals.class, required = true))
    private static class RequiredFilters {
    }

    @Conjunction(@Filter(path = "name", parameters = "name", operation = Equals.class, negate = "true"))
    private static class NegatedFilters {
    }

    private static class MultiRootController {

        @SuppressWarnings("unused")
        void search(
                @Conjunction(@Filter(path = "name", parameters = "name", operation = Equals.class))
                @Disjunction(value = @Filter(path = "city", parameters = "city", operation = Equals.class), conjunctions = @Statement)
                Object filter
        ) {
        }
    }
}
