package com.runestone.expeval.expression;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

public class TestExpressionContext {

    @Test
    public void findValueShouldReturnNullWhenValueIsMissing() {
        ExpressionContext context = new ExpressionContext();

        Assertions.assertThat(context.findValue("missing")).isNull();
    }

    @Test
    public void findValueShouldNotInitializeDictionaryOnReadPathWhenDictionaryIsMissing() throws ReflectiveOperationException {
        ExpressionContext context = new ExpressionContext();
        Field dictionaryField = ExpressionContext.class.getDeclaredField("dictionary");
        dictionaryField.setAccessible(true);

        Assertions.assertThat(dictionaryField.get(context)).isNull();
        Assertions.assertThat(context.findValue("missing")).isNull();
        Assertions.assertThat(dictionaryField.get(context)).isNull();
    }
}
