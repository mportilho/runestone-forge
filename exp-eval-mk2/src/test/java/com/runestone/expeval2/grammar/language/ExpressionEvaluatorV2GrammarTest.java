package com.runestone.expeval2.grammar.language;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

class ExpressionEvaluatorV2GrammarTest {

    @Test
    void bareReferenceShouldRemainGenericInUntypedAssignment() {
        String tree = parseMathTree("a = foo; 1");

        assertThat(tree)
            .contains("(allEntityTypes (genericEntity (referenceTarget foo)))")
            .doesNotContain("(numericEntity foo)");
    }

    @Test
    void genericDecisionShouldRemainGenericInUntypedAssignment() {
        String tree = parseMathTree("a = if true then foo else bar endif; 1");

        assertThat(tree)
            .contains("(genericEntity if")
            .doesNotContain("(mathDecisionExpression");
    }

    @Test
    void explicitCastShouldStayDistinctFromTypeHint() {
        String castTree = parseMathTree("a = <number>(foo); 1");
        String hintTree = parseMathTree("<number>foo + 1");

        assertThat(castTree)
            .contains("(castExpression (numberCastExpression <number> (")
            .doesNotContain("(numericEntity <number> (referenceTarget foo))");

        assertThat(hintTree)
            .contains("(numericEntity <number> (referenceTarget foo))")
            .doesNotContain("(numberCastExpression <number> (");
    }

    @Test
    void unaryMinusShouldBindAfterExponentiation() {
        String tree = parseMathTree("-2^2");

        assertThat(tree)
            .contains("(unaryMathExpression - (unaryMathExpression (powerMathExpression")
            .contains("(numericEntity 2))) ^ (unaryMathExpression");
    }

    @Test
    void unaryMinusShouldAcceptWhitespaceSeparatedOperand() {
        String tree = parseMathTree("- 2");

        assertThat(tree)
            .contains("(unaryMathExpression - (unaryMathExpression")
            .doesNotContain("no viable alternative");
    }

    @Test
    void destructuringAssignmentShouldTreatFunctionAsVectorInTypedContext() {
        String tree = parseMathTree("[a,b] = makeVec(); 1");

        assertThat(tree)
            .contains("(assignmentExpression (vectorOfVariables [ a , b ]) = (vectorEntity (referenceTarget (function makeVec ( )))) ;)");
    }

    private String parseMathTree(String input) {
        ExpressionEvaluatorV2Lexer lexer = new ExpressionEvaluatorV2Lexer(CharStreams.fromString(input));
        lexer.removeErrorListeners();
        lexer.addErrorListener(FailingErrorListener.INSTANCE);

        ExpressionEvaluatorV2Parser parser = new ExpressionEvaluatorV2Parser(new CommonTokenStream(lexer));
        parser.removeErrorListeners();
        parser.addErrorListener(FailingErrorListener.INSTANCE);

        return parser.mathStart().toStringTree(parser);
    }

    private static final class FailingErrorListener extends BaseErrorListener {

        private static final FailingErrorListener INSTANCE = new FailingErrorListener();

        @Override
        public void syntaxError(
            Recognizer<?, ?> recognizer,
            Object offendingSymbol,
            int line,
            int charPositionInLine,
            String msg,
            RecognitionException e
        ) {
            fail("syntax error at %d:%d - %s".formatted(line, charPositionInLine, msg));
        }
    }
}
