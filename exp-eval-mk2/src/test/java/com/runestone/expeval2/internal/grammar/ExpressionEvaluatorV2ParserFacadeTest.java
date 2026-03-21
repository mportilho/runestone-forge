package com.runestone.expeval2.internal.grammar;

import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExpressionEvaluatorV2ParserFacadeTest {

    @Test
    void shouldParseMathExpressionWithSllWhenFastPathSucceeds() {
        ExpressionEvaluatorV2ParserFacade facade = new ExpressionEvaluatorV2ParserFacade();

        ParseResult<com.runestone.expeval2.internal.grammar.ExpressionEvaluatorV2Parser.MathStartContext> result = facade.parseMath("a = foo; 1");

        assertThat(result.predictionStrategy()).isEqualTo(PredictionStrategy.SLL);
        assertThat(result.root().getText()).isEqualTo("a=foo;1<EOF>");
    }

    @Test
    void shouldParseLogicalExpressionWithSllWhenFastPathSucceeds() {
        ExpressionEvaluatorV2ParserFacade facade = new ExpressionEvaluatorV2ParserFacade();

        ParseResult<com.runestone.expeval2.internal.grammar.ExpressionEvaluatorV2Parser.LogicalStartContext> result =
            facade.parseLogical("if true then 10 else 20 endif > if false then 1 else 2 endif and !false");

        assertThat(result.predictionStrategy()).isEqualTo(PredictionStrategy.SLL);
        assertThat(result.root().getText()).contains("iftruethen10else20endif");
    }

    @Test
    void shouldFallbackToLlWhenSllCancels() {
        AtomicInteger sllCalls = new AtomicInteger();
        AtomicInteger llCalls = new AtomicInteger();
        ExpressionEvaluatorV2ParserFacade facade = new ExpressionEvaluatorV2ParserFacade(new TestParseExecutor() {
            @Override
            com.runestone.expeval2.internal.grammar.ExpressionEvaluatorV2Parser.MathStartContext parseMath(PredictionStrategy predictionStrategy) {
                if (predictionStrategy == PredictionStrategy.SLL) {
                    sllCalls.incrementAndGet();
                    throw new ParseCancellationException();
                }

                llCalls.incrementAndGet();
                return new com.runestone.expeval2.internal.grammar.ExpressionEvaluatorV2Parser.MathStartContext(null, 0);
            }
        });

        ParseResult<com.runestone.expeval2.internal.grammar.ExpressionEvaluatorV2Parser.MathStartContext> result = facade.parseMath("1");

        assertThat(result.predictionStrategy()).isEqualTo(PredictionStrategy.LL_FALLBACK);
        assertThat(result.root()).isNotNull();
        assertThat(sllCalls.get()).isEqualTo(1);
        assertThat(llCalls.get()).isEqualTo(1);
    }

    @Test
    void shouldExposeCollectedSyntaxErrorsWhenLlParseFails() {
        ExpressionEvaluatorV2ParserFacade facade = new ExpressionEvaluatorV2ParserFacade();

        assertThatThrownBy(() -> facade.parseMath("a = ; 1"))
            .isInstanceOf(ParsingException.class)
            .hasMessageContaining("failed to parse input at 1:4");
    }

    @Test
    void shouldRejectBlankInput() {
        ExpressionEvaluatorV2ParserFacade facade = new ExpressionEvaluatorV2ParserFacade();

        assertThatThrownBy(() -> facade.parseMath("   "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("input must not be blank");
    }

    @Test
    void shouldWarmUpInputsUsingSameSllThenLlFallbackFlow() {
        AtomicInteger sllCalls = new AtomicInteger();
        AtomicInteger llCalls = new AtomicInteger();
        ExpressionEvaluatorV2ParserFacade facade = new ExpressionEvaluatorV2ParserFacade(new TestParseExecutor() {
            @Override
            com.runestone.expeval2.internal.grammar.ExpressionEvaluatorV2Parser.MathStartContext parseMath(PredictionStrategy predictionStrategy) {
                if (predictionStrategy == PredictionStrategy.SLL) {
                    sllCalls.incrementAndGet();
                } else {
                    llCalls.incrementAndGet();
                }
                return new com.runestone.expeval2.internal.grammar.ExpressionEvaluatorV2Parser.MathStartContext(null, 0);
            }

            @Override
            com.runestone.expeval2.internal.grammar.ExpressionEvaluatorV2Parser.LogicalStartContext parseLogical(PredictionStrategy predictionStrategy) {
                if (predictionStrategy == PredictionStrategy.SLL) {
                    sllCalls.incrementAndGet();
                    throw new ParseCancellationException();
                }

                llCalls.incrementAndGet();
                return new com.runestone.expeval2.internal.grammar.ExpressionEvaluatorV2Parser.LogicalStartContext(null, 0);
            }
        });

        facade.warmUp(List.of(
            new WarmupInput("a = foo; 1", ExpressionResultType.MATH),
            new WarmupInput("true", ExpressionResultType.LOGICAL)
        ));

        assertThat(sllCalls.get()).isEqualTo(2);
        assertThat(llCalls.get()).isEqualTo(1);
    }

    @Test
    void shouldRejectBlankWarmupInput() {
        ExpressionEvaluatorV2ParserFacade facade = new ExpressionEvaluatorV2ParserFacade();

        assertThatThrownBy(() -> facade.warmUp(List.of(
            new WarmupInput("   ", ExpressionResultType.MATH)
        )))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("input must not be blank");
    }

    private abstract static class TestParseExecutor implements ParseExecutor {

        @Override
        public com.runestone.expeval2.internal.grammar.ExpressionEvaluatorV2Parser.MathStartContext parseMath(
            String input,
            PredictionStrategy predictionStrategy
        ) {
            return parseMath(predictionStrategy);
        }

        @Override
        public com.runestone.expeval2.internal.grammar.ExpressionEvaluatorV2Parser.LogicalStartContext parseLogical(
            String input,
            PredictionStrategy predictionStrategy
        ) {
            return parseLogical(predictionStrategy);
        }

        @Override
        public com.runestone.expeval2.internal.grammar.ExpressionEvaluatorV2Parser.AssignmentStartContext parseAssignments(
            String input,
            PredictionStrategy predictionStrategy
        ) {
            throw new UnsupportedOperationException("assignment parsing not stubbed");
        }

        com.runestone.expeval2.internal.grammar.ExpressionEvaluatorV2Parser.MathStartContext parseMath(
            PredictionStrategy predictionStrategy
        ) {
            throw new UnsupportedOperationException("math parsing not stubbed");
        }

        com.runestone.expeval2.internal.grammar.ExpressionEvaluatorV2Parser.LogicalStartContext parseLogical(
            PredictionStrategy predictionStrategy
        ) {
            throw new UnsupportedOperationException("logical parsing not stubbed");
        }
    }
}
