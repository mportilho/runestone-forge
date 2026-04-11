package com.runestone.expeval.internal.grammar;

import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExpressionEvaluatorParserFacadeTest {

    @Test
    void shouldParseMathExpressionWithSllWhenFastPathSucceeds() {
        ExpressionEvaluatorParserFacade facade = new ExpressionEvaluatorParserFacade();

        ParseResult<com.runestone.expeval.internal.grammar.ExpressionEvaluatorParser.MathStartContext> result = facade.parseMath("a = foo; 1");

        assertThat(result.predictionStrategy()).isEqualTo(PredictionStrategy.SLL);
        assertThat(result.root().getText()).isEqualTo("a=foo;1<EOF>");
    }

    @Test
    void shouldParseLogicalExpressionWithSllWhenFastPathSucceeds() {
        ExpressionEvaluatorParserFacade facade = new ExpressionEvaluatorParserFacade();

        ParseResult<com.runestone.expeval.internal.grammar.ExpressionEvaluatorParser.LogicalStartContext> result =
            facade.parseLogical("if true then 10 else 20 endif > if false then 1 else 2 endif and !false");

        assertThat(result.predictionStrategy()).isEqualTo(PredictionStrategy.SLL);
        assertThat(result.root().getText()).contains("iftruethen10else20endif");
    }

    @Test
    void shouldFallbackToLlWhenSllCancels() {
        AtomicInteger sllCalls = new AtomicInteger();
        AtomicInteger llCalls = new AtomicInteger();
        ExpressionEvaluatorParserFacade facade = new ExpressionEvaluatorParserFacade(new TestParseExecutor() {
            @Override
            com.runestone.expeval.internal.grammar.ExpressionEvaluatorParser.MathStartContext parseMath(PredictionStrategy predictionStrategy) {
                if (predictionStrategy == PredictionStrategy.SLL) {
                    sllCalls.incrementAndGet();
                    throw new ParseCancellationException();
                }

                llCalls.incrementAndGet();
                return new com.runestone.expeval.internal.grammar.ExpressionEvaluatorParser.MathStartContext(null, 0);
            }
        });

        ParseResult<com.runestone.expeval.internal.grammar.ExpressionEvaluatorParser.MathStartContext> result = facade.parseMath("1");

        assertThat(result.predictionStrategy()).isEqualTo(PredictionStrategy.LL_FALLBACK);
        assertThat(result.root()).isNotNull();
        assertThat(sllCalls.get()).isEqualTo(1);
        assertThat(llCalls.get()).isEqualTo(1);
    }

    @Test
    void shouldExposeCollectedSyntaxErrorsWhenLlParseFails() {
        ExpressionEvaluatorParserFacade facade = new ExpressionEvaluatorParserFacade();

        assertThatThrownBy(() -> facade.parseMath("a = ; 1"))
            .isInstanceOf(ParsingException.class)
            .hasMessageContaining("syntax error at 1:4");
    }

    @Test
    void shouldRejectBlankInput() {
        ExpressionEvaluatorParserFacade facade = new ExpressionEvaluatorParserFacade();

        assertThatThrownBy(() -> facade.parseMath("   "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("input must not be blank");
    }

    private abstract static class TestParseExecutor implements ParseExecutor {

        @Override
        public com.runestone.expeval.internal.grammar.ExpressionEvaluatorParser.MathStartContext parseMath(
            String input,
            PredictionStrategy predictionStrategy
        ) {
            return parseMath(predictionStrategy);
        }

        @Override
        public com.runestone.expeval.internal.grammar.ExpressionEvaluatorParser.LogicalStartContext parseLogical(
            String input,
            PredictionStrategy predictionStrategy
        ) {
            return parseLogical(predictionStrategy);
        }

        @Override
        public com.runestone.expeval.internal.grammar.ExpressionEvaluatorParser.AssignmentStartContext parseAssignments(
            String input,
            PredictionStrategy predictionStrategy
        ) {
            throw new UnsupportedOperationException("assignment parsing not stubbed");
        }

        com.runestone.expeval.internal.grammar.ExpressionEvaluatorParser.MathStartContext parseMath(
            PredictionStrategy predictionStrategy
        ) {
            throw new UnsupportedOperationException("math parsing not stubbed");
        }

        com.runestone.expeval.internal.grammar.ExpressionEvaluatorParser.LogicalStartContext parseLogical(
            PredictionStrategy predictionStrategy
        ) {
            throw new UnsupportedOperationException("logical parsing not stubbed");
        }
    }
}
