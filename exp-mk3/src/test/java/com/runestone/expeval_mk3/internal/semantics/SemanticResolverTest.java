package com.runestone.expeval_mk3.internal.semantics;

import com.runestone.expeval_mk3.api.CollectionType;
import com.runestone.expeval_mk3.api.ExpressionEnvironment;
import com.runestone.expeval_mk3.api.RuntimeNullability;
import com.runestone.expeval_mk3.api.ScalarType;
import com.runestone.expeval_mk3.internal.ast.BinaryOperationNode;
import com.runestone.expeval_mk3.internal.ast.CollectionLiteralNode;
import com.runestone.expeval_mk3.internal.ast.ExpressionFileNode;
import com.runestone.expeval_mk3.internal.ast.SemanticAstBuildSuccess;
import com.runestone.expeval_mk3.internal.ast.SemanticAstBuilder;
import com.runestone.expeval_mk3.internal.diagnostics.DiagnosticCode;
import com.runestone.expeval_mk3.internal.parser.ExpressionParser;
import com.runestone.expeval_mk3.internal.parser.ParseSuccess;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SemanticResolverTest {

    @Test
    void contextualizesAnEmptyCollectionWithoutLeavingUnknownSemanticFacts() {
        ExpressionFileNode ast = ast("[] = [1]");
        BinaryOperationNode equality = (BinaryOperationNode) ast.resultExpression().orElseThrow();
        CollectionLiteralNode empty = (CollectionLiteralNode) equality.left();

        SemanticResolutionResult result = new SemanticResolver().resolve(ast, ExpressionEnvironment.standard());

        assertThat(result).isInstanceOfSatisfying(SemanticResolutionSuccess.class, success -> {
            SemanticModel model = success.model();
            assertThat(model.resolvedTypes()).containsEntry(empty.id(), new CollectionType(ScalarType.NUMBER));
            assertThat(model.resolvedTypes().keySet()).contains(
                    equality.id(), equality.left().id(), equality.right().id());
            assertThat(model.runtimeNullability().values())
                    .containsOnly(RuntimeNullability.NEVER_NULL);
            assertThat(model.symbolBindings()).isEmpty();
        });
    }

    @Test
    void rejectsAnUnconstrainedEmptyCollectionWithItsStableSpan() {
        SemanticResolutionResult result = new SemanticResolver().resolve(ast("[]"), ExpressionEnvironment.standard());

        assertThat(result).isInstanceOfSatisfying(SemanticResolutionFailure.class, failure -> {
            assertThat(failure.diagnostics()).singleElement().satisfies(diagnostic -> {
                assertThat(diagnostic.code()).isEqualTo(DiagnosticCode.SEMANTIC_EMPTY_COLLECTION_REQUIRES_CONTEXT);
                assertThat(diagnostic.span().offset()).isZero();
                assertThat(diagnostic.span().endOffset()).isEqualTo(2);
                assertThat(diagnostic.span().line()).isEqualTo(1);
                assertThat(diagnostic.span().column()).isEqualTo(1);
            });
        });
    }

    @Test
    void keepsTheEmptyLiteralSpanWhenGroupingDoesNotProvideContext() {
        SemanticResolutionResult result = new SemanticResolver().resolve(ast("([])"), ExpressionEnvironment.standard());

        assertThat(result).isInstanceOfSatisfying(SemanticResolutionFailure.class, failure -> {
            assertThat(failure.diagnostics()).singleElement().satisfies(diagnostic -> {
                assertThat(diagnostic.code()).isEqualTo(DiagnosticCode.SEMANTIC_EMPTY_COLLECTION_REQUIRES_CONTEXT);
                assertThat(diagnostic.span().offset()).isEqualTo(1);
                assertThat(diagnostic.span().endOffset()).isEqualTo(3);
            });
        });
    }

    private static ExpressionFileNode ast(String source) {
        ParseSuccess parse = (ParseSuccess) new ExpressionParser().parse(source);
        return ((SemanticAstBuildSuccess) new SemanticAstBuilder().build(parse)).file();
    }
}
