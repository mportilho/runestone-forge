package com.runestone.dynafilter.core.decorator;

import com.runestone.dynafilter.core.generator.StatementWrapper;
import com.runestone.dynafilter.core.statement.NoOpStatement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CompositeFilterDecoratorTest {

    @Test
    @DisplayName("applies decorators in declaration order")
    void appliesDecoratorsInOrder() {
        FilterDecorator<String> decorator = FilterDecorator.of(
                (filter, wrapper) -> filter + "A",
                (filter, wrapper) -> filter + "B"
        );

        String result = decorator.decorate("filter", wrapper());

        assertThat(result).isEqualTo("filterAB");
    }

    @Test
    @DisplayName("fails when a decorator returns null")
    void failsWhenDecoratorReturnsNull() {
        FilterDecorator<String> decorator = FilterDecorator.of((filter, wrapper) -> null);

        assertThatThrownBy(() -> decorator.decorate("filter", wrapper()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must not return null");
    }

    private static StatementWrapper wrapper() {
        return new StatementWrapper(new NoOpStatement(), null, null);
    }
}
