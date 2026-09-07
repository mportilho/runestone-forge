package com.runestone.expeval_mk3.api;

import com.runestone.expeval_mk3.internal.parser.ExpressionParser;
import com.runestone.expeval_mk3.internal.runtime.RuntimeServices;
import com.runestone.expeval_mk3.support.ParserRetentionAssertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

/**
 * Proves the module-shared {@link ExpressionParser} the pipeline uses never leaves the previous
 * source or its buffered tokens in the calling thread's context, regardless of how compilation ends:
 * clean success, a parser-level syntax failure, or a semantic-resolution failure raised after parsing
 * already returned.
 */
class CompilationPipelineParserRetentionTest {

    @Test
    void releasesTheParserThreadContextAfterASuccessfulCompilation() throws ReflectiveOperationException {
        String source = new String("1 + 2");
        CompilationPipeline.compile(source, ExpressionEnvironment.standard(), RuntimeServices.systemDefault());

        assertParserThreadContextIsReleased();
    }

    @Test
    void releasesTheParserThreadContextAfterASyntaxFailure() throws ReflectiveOperationException {
        String source = new String("1 +");
        CompilationPipeline.compile(source, ExpressionEnvironment.standard(), RuntimeServices.systemDefault());

        assertParserThreadContextIsReleased();
    }

    @Test
    void releasesTheParserThreadContextAfterASemanticFailure() throws ReflectiveOperationException {
        String source = new String("missing");
        CompilationPipeline.compile(source, ExpressionEnvironment.standard(), RuntimeServices.systemDefault());

        assertParserThreadContextIsReleased();
    }

    private static void assertParserThreadContextIsReleased() throws ReflectiveOperationException {
        Field parserField = CompilationPipeline.class.getDeclaredField("PARSER");
        parserField.setAccessible(true);
        ExpressionParser parser = (ExpressionParser) parserField.get(null);

        ParserRetentionAssertions.assertReleased(parser);
    }
}
