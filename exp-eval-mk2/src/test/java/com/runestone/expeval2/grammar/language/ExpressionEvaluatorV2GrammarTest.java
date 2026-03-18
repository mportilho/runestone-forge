package com.runestone.expeval2.internal.grammar;

import com.runestone.expeval2.internal.grammar.ExpressionEvaluatorV2ParserFacade;
import com.runestone.expeval2.internal.grammar.ParseResult;
import org.antlr.v4.runtime.tree.Trees;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class ExpressionEvaluatorV2GrammarTest {

    @Test
    void bareReferenceShouldRemainGenericInUntypedAssignment() {
        String tree = parseMathTree("a = foo; 1");

        assertThat(tree)
            .contains("(assignmentValue (genericEntity (referenceTarget foo)))")
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
            .contains("(castExpression (typeHint <number>) ( (genericEntity (referenceTarget foo)) ))")
            .doesNotContain("(numericEntity <number> (referenceTarget foo))");

        assertThat(hintTree)
            .contains("(numericEntity <number> (referenceTarget foo))")
            .doesNotContain("(castExpression");
    }

    @Test
    void unaryMinusShouldBindAfterExponentiation() {
        String tree = parseMathTree("-2^2");

        assertThat(tree)
            .contains("(unaryExpression - (unaryExpression (rootExpression (exponentiationExpression")
            .contains("(numericEntity 2))) ^ (unaryExpression");
    }

    @Test
    void unaryMinusShouldAcceptWhitespaceSeparatedOperand() {
        String tree = parseMathTree("- 2");

        assertThat(tree)
            .contains("(unaryExpression - (unaryExpression")
            .doesNotContain("no viable alternative");
    }

    @Test
    void destructuringAssignmentShouldTreatFunctionAsVectorInTypedContext() {
        String tree = parseMathTree("[a,b] = makeVec(); 1");

        assertThat(tree)
            .contains("(assignmentExpression (vectorOfVariables [ a , b ]) = (vectorEntity (referenceTarget (function makeVec ( )))) ;)");
    }

    private String parseMathTree(String input) {
        ParseResult<ExpressionEvaluatorV2Parser.MathStartContext> result =
            new ExpressionEvaluatorV2ParserFacade().parseMath(input);
        return Trees.toStringTree(result.root(), Arrays.asList(ExpressionEvaluatorV2Parser.ruleNames));
    }
}
