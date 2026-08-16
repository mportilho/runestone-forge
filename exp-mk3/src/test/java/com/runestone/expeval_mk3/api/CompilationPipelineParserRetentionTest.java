package com.runestone.expeval_mk3.api;

import com.runestone.expeval_mk3.internal.parser.ExpressionParser;
import com.runestone.expeval_mk3.internal.runtime.RuntimeServices;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Proves the module-shared {@link ExpressionParser} the pipeline uses never leaves the previous
 * source or its buffered tokens in the calling thread's context, regardless of how compilation ends:
 * clean success, a parser-level syntax failure, or a semantic-resolution failure raised after parsing
 * already returned.
 */
class CompilationPipelineParserRetentionTest {

    @Test
    void releasesTheParserThreadContextAfterASuccessfulCompilation() throws ReflectiveOperationException {
        CompilationPipeline.compile("1 + 2", ExpressionEnvironment.standard(), RuntimeServices.systemDefault());

        assertParserThreadContextIsReleased();
    }

    @Test
    void releasesTheParserThreadContextAfterASyntaxFailure() throws ReflectiveOperationException {
        CompilationPipeline.compile("1 +", ExpressionEnvironment.standard(), RuntimeServices.systemDefault());

        assertParserThreadContextIsReleased();
    }

    @Test
    void releasesTheParserThreadContextAfterASemanticFailure() throws ReflectiveOperationException {
        CompilationPipeline.compile("missing", ExpressionEnvironment.standard(), RuntimeServices.systemDefault());

        assertParserThreadContextIsReleased();
    }

    private static void assertParserThreadContextIsReleased() throws ReflectiveOperationException {
        Field parserField = CompilationPipeline.class.getDeclaredField("PARSER");
        parserField.setAccessible(true);
        ExpressionParser parser = (ExpressionParser) parserField.get(null);

        Field contextField = ExpressionParser.class.getDeclaredField("context");
        contextField.setAccessible(true);
        ThreadLocal<?> threadLocal = (ThreadLocal<?>) contextField.get(parser);
        Object parserContext = threadLocal.get();

        Field sourceField = parserContext.getClass().getDeclaredField("source");
        sourceField.setAccessible(true);
        Field tokensField = parserContext.getClass().getDeclaredField("tokens");
        tokensField.setAccessible(true);

        assertThat(sourceField.get(parserContext)).isNull();
        assertThat(((CommonTokenStream) tokensField.get(parserContext)).getTokens()).isEmpty();
    }
}
