package com.runestone.expeval_mk3.support;

import com.runestone.expeval_mk3.internal.parser.ExpressionParser;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

public final class ParserRetentionAssertions {

    private ParserRetentionAssertions() {
    }

    public static void assertReleased(ExpressionParser parser) throws ReflectiveOperationException {
        Object parserContext = parserContext(parser);
        Lexer lexer = (Lexer) fieldValue(parserContext, "lexer");
        CommonTokenStream tokens = (CommonTokenStream) fieldValue(parserContext, "tokens");
        Parser antlrParser = (Parser) fieldValue(parserContext, "parser");
        Object interpreter = antlrParser.getInterpreter();

        assertThat(fieldValue(parserContext, "source")).isNull();
        assertThat(lexer.getInputStream().size()).isZero();
        assertThat(tokens.getTokens()).isEmpty();
        assertThat(antlrParser.getContext()).isNull();
        assertThat(antlrParser.getErrorHandler()).isExactlyInstanceOf(DefaultErrorStrategy.class);
        assertThat(inheritedFieldValue(interpreter, "_input")).isNull();
        assertThat(inheritedFieldValue(interpreter, "_outerContext")).isNull();
        assertThat(inheritedFieldValue(interpreter, "mergeCache")).isNull();
        assertThat(inheritedFieldValue(interpreter, "_dfa")).isNull();
    }

    private static Object parserContext(ExpressionParser parser) throws ReflectiveOperationException {
        Field contextField = ExpressionParser.class.getDeclaredField("context");
        contextField.setAccessible(true);
        ThreadLocal<?> threadLocal = (ThreadLocal<?>) contextField.get(parser);
        return threadLocal.get();
    }

    private static Object fieldValue(Object target, String fieldName) throws ReflectiveOperationException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }

    private static Object inheritedFieldValue(Object target, String fieldName) throws ReflectiveOperationException {
        Class<?> type = target.getClass();
        while (type != null) {
            try {
                Field field = type.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(target);
            } catch (NoSuchFieldException exception) {
                type = type.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }
}
