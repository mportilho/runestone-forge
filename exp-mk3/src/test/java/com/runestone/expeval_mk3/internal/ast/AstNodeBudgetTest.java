package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.internal.parser.ExpressionParser;
import com.runestone.expeval_mk3.internal.parser.ParseSuccess;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AstNodeBudgetTest {

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "1|2", "a:=1;|4", "[a,b]:=[1,2];|8", "1+2*3|6",
            "if true then 1 else 2 endif|6", "f(@ -> @)|5", "a.b(1)[0]|6",
            "[1,2,3]|5", "a[?(@=1)]|7", "a??b??c|5", "1%%|3"
    })
    void countsAllNodeKindsDuringConstructionAndIdentityAssignment(String source, int nodes) {
        var parsed = (ParseSuccess) new ExpressionParser().parse(source);
        var builder = new SemanticAstBuilder();
        for (int limit = nodes - 1; limit <= nodes + 1; limit++) {
            var result = builder.build(parsed, limit);
            if (limit < nodes) {
                assertThat(result).isInstanceOf(SemanticAstBuildFailure.class);
                assertThat(((SemanticAstBuildFailure) result).diagnostics()).extracting(diagnostic -> diagnostic.code())
                        .containsExactly("COMPILE_AST_NODE_COUNT_EXCEEDED");
            } else {
                assertThat(result).isInstanceOf(SemanticAstBuildSuccess.class);
            }
        }
    }

    @Test
    void identityAssignmentDefensivelyRejectsAnOverBudgetTree() {
        var parsed = (ParseSuccess) new ExpressionParser().parse("1+2");
        var file = ((SemanticAstBuildSuccess) new SemanticAstBuilder().build(parsed)).file();
        assertThatThrownBy(() -> new AstNodeIdAssigner(3).assign(file)).isInstanceOf(AstNodeLimitException.class);
    }

    @Test
    void terminalNodeExhaustionReplacesEarlierLiteralDiagnostics() {
        var parsed = (ParseSuccess) new ExpressionParser().parse("[d\"2024-02-30\",1,2]");
        var result = new SemanticAstBuilder().build(parsed, 2);
        assertThat(result).isInstanceOf(SemanticAstBuildFailure.class);
        assertThat(((SemanticAstBuildFailure) result).diagnostics()).extracting(diagnostic -> diagnostic.code())
                .containsExactly("COMPILE_AST_NODE_COUNT_EXCEEDED");
    }
}
