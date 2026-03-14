package com.runestone.expeval2.grammar.language;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExpressionEvaluatorV2ParserFacadeTest {

    @Test
    void shouldParseMathExpressionWithSllWhenFastPathSucceeds() {
        ExpressionEvaluatorV2ParserFacade facade = new ExpressionEvaluatorV2ParserFacade();

        ExpressionEvaluatorV2ParserFacade.ParseResult<ExpressionEvaluatorV2Parser.MathStartContext> result = facade.parseMath("a = foo; 1");

        assertThat(result.predictionStrategy()).isEqualTo(ExpressionEvaluatorV2ParserFacade.PredictionStrategy.SLL);
        assertThat(result.root().getText()).isEqualTo("a=foo;1<EOF>");
    }

    @Test
    void shouldParseLogicalExpressionWithSllWhenFastPathSucceeds() {
        ExpressionEvaluatorV2ParserFacade facade = new ExpressionEvaluatorV2ParserFacade();

        ExpressionEvaluatorV2ParserFacade.ParseResult<ExpressionEvaluatorV2Parser.LogicalStartContext> result =
            facade.parseLogical("if true then 10 else 20 endif > if false then 1 else 2 endif and !false");

        assertThat(result.predictionStrategy()).isEqualTo(ExpressionEvaluatorV2ParserFacade.PredictionStrategy.SLL);
        assertThat(result.root().getText()).contains("iftruethen10else20endif");
    }

    @Test
    void shouldFallbackToLlWhenSllCancels() {
        AtomicInteger sllCalls = new AtomicInteger();
        AtomicInteger llCalls = new AtomicInteger();
        ExpressionEvaluatorV2ParserFacade facade = new ExpressionEvaluatorV2ParserFacade((input, entryPoint, predictionStrategy) -> {
            if (predictionStrategy == ExpressionEvaluatorV2ParserFacade.PredictionStrategy.SLL) {
                sllCalls.incrementAndGet();
                throw new ParseCancellationException();
            }

            llCalls.incrementAndGet();
            return switch (entryPoint) {
                case MATH -> new ExpressionEvaluatorV2Parser.MathStartContext(null, 0);
                case LOGICAL -> new ExpressionEvaluatorV2Parser.LogicalStartContext(null, 0);
            };
        });

        ExpressionEvaluatorV2ParserFacade.ParseResult<ExpressionEvaluatorV2Parser.MathStartContext> result = facade.parseMath("1");

        assertThat(result.predictionStrategy()).isEqualTo(ExpressionEvaluatorV2ParserFacade.PredictionStrategy.LL_FALLBACK);
        assertThat(result.root()).isNotNull();
        assertThat(sllCalls.get()).isEqualTo(1);
        assertThat(llCalls.get()).isEqualTo(1);
    }

    @Test
    void shouldExposeCollectedSyntaxErrorsWhenLlParseFails() {
        ExpressionEvaluatorV2ParserFacade facade = new ExpressionEvaluatorV2ParserFacade();

        assertThatThrownBy(() -> facade.parseMath("a = ; 1"))
            .isInstanceOf(ExpressionEvaluatorV2ParserFacade.ParsingException.class)
            .hasMessageContaining("failed to parse input at 1:4");
    }

    @Test
    void shouldRejectBlankInput() {
        ExpressionEvaluatorV2ParserFacade facade = new ExpressionEvaluatorV2ParserFacade();

        assertThatThrownBy(() -> facade.parseMath("   "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("input must not be blank");
    }
}
