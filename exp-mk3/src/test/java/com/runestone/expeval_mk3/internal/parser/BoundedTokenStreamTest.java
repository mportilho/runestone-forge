package com.runestone.expeval_mk3.internal.parser;

import com.runestone.expeval_mk3.internal.grammar.ExpressionEvaluatorLexer;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BoundedTokenStreamTest {

    @Test
    void stopsRequestingTokensImmediatelyAfterFirstExcessIncludingHiddenChannel() {
        String source = "1 + " + "2+".repeat(10_000) + "3";
        var lexer = new CountingLexer(source);
        var stream = new ExpressionParser.BoundedTokenStream(lexer);
        stream.configure(1, AntlrSourcePositions.from(source));

        assertThatThrownBy(stream::fill).isInstanceOf(CompilationLimitException.class);

        assertThat(lexer.requests).isEqualTo(2);
        assertThat(lexer.getCharIndex()).isEqualTo(2);
        assertThat(stream.getTokens()).hasSize(2);
    }

    @Test
    void eofDoesNotConsumeBudget() {
        var lexer = new CountingLexer("");
        var stream = new ExpressionParser.BoundedTokenStream(lexer);
        stream.configure(0, AntlrSourcePositions.from(""));

        stream.fill();

        assertThat(lexer.requests).isOne();
        assertThat(stream.getTokens()).extracting(Token::getType).containsExactly(Token.EOF);
    }

    private static final class CountingLexer extends ExpressionEvaluatorLexer {
        private int requests;

        private CountingLexer(String source) {
            super(CharStreams.fromString(source));
        }

        @Override
        public Token nextToken() {
            requests++;
            return super.nextToken();
        }
    }
}
