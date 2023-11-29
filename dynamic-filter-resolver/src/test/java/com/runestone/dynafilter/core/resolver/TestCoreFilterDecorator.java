package com.runestone.dynafilter.core.resolver;

import com.runestone.dynafilter.core.exceptions.StatementGenerationException;
import com.runestone.dynafilter.core.generator.StatementWrapper;
import com.runestone.dynafilter.modules.jpa.tools.MockStatementFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class TestCoreFilterDecorator {

    @Test
    public void testCompositeFilterDecorator() {
        FilterDecorator<String> decorator1 = (s, statement) -> s + "1";
        FilterDecorator<String> decorator2 = (s, statement) -> s + "2";
        FilterDecorator<String> composite = FilterDecorator.of(List.of(decorator1, decorator2));
        String decorated = composite.decorate("Test", new StatementWrapper(MockStatementFactory.createLogicalStatementOnName(), null));
        Assertions.assertThat(decorated).isEqualTo("Test12");
    }

    @Test
    public void testCompositeThrowOnReturnNull() {
        FilterDecorator<String> decorator1 = (s, statement) -> s + "1";
        FilterDecorator<String> decorator2 = (s, statement) -> null;
        FilterDecorator<String> composite = FilterDecorator.of(List.of(decorator1, decorator2));
        Assertions.assertThatThrownBy(() -> composite.decorate("Test", new StatementWrapper(MockStatementFactory.createLogicalStatementOnName(), null)))
                .isExactlyInstanceOf(StatementGenerationException.class)
                .hasMessageContaining("Filter decorator returned null");
    }

}
