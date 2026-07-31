package com.runestone.expeval_mk3.internal.semantics;

import com.runestone.expeval_mk3.api.CollectionType;
import com.runestone.expeval_mk3.api.ExpressionEnvironment;
import com.runestone.expeval_mk3.api.ExternalSymbolOverwritePolicy;
import com.runestone.expeval_mk3.api.MapType;
import com.runestone.expeval_mk3.api.ScalarType;
import com.runestone.expeval_mk3.internal.ast.AssignmentNode;
import com.runestone.expeval_mk3.internal.ast.BinaryOperationNode;
import com.runestone.expeval_mk3.internal.ast.DestructuringAssignmentTargetNode;
import com.runestone.expeval_mk3.internal.ast.ExpressionFileNode;
import com.runestone.expeval_mk3.internal.ast.ExpressionNode;
import com.runestone.expeval_mk3.internal.ast.GroupedExpressionNode;
import com.runestone.expeval_mk3.internal.ast.NavigationChainNode;
import com.runestone.expeval_mk3.internal.ast.NavigationLink;
import com.runestone.expeval_mk3.internal.ast.PostfixOperationNode;
import com.runestone.expeval_mk3.internal.ast.UnaryOperationNode;
import com.runestone.expeval_mk3.internal.ast.SemanticAstBuildSuccess;
import com.runestone.expeval_mk3.internal.ast.SemanticAstBuilder;
import com.runestone.expeval_mk3.internal.diagnostics.DiagnosticCode;
import com.runestone.expeval_mk3.internal.parser.ExpressionParser;
import com.runestone.expeval_mk3.internal.parser.ParseSuccess;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class NumericFactsAndDeferredChecksTest {

    @Test
    void integerLiteralIsIntegralKnownWithParity() {
        ExpressionFileNode ast = ast("5");
        SemanticResolutionResult result = new SemanticResolver().resolve(ast, ExpressionEnvironment.standard());

        assertThat(result).isInstanceOfSatisfying(SemanticResolutionSuccess.class, success -> {
            NumericFact fact = success.model().numericFactOf(ast.resultExpression().orElseThrow().id());
            assertThat(fact.shape()).isEqualTo(NumericFactShape.INTEGRAL_KNOWN);
            assertThat(fact.parity().signum()).isEqualTo(1);
            assertThat(fact.parity().numeratorOdd()).isTrue();
            assertThat(fact.parity().denominatorOdd()).isTrue();
        });
    }

    @Test
    void fractionalLiteralIsFractionalKnown() {
        ExpressionFileNode ast = ast("2.5");
        SemanticResolutionResult result = new SemanticResolver().resolve(ast, ExpressionEnvironment.standard());

        assertThat(result).isInstanceOfSatisfying(SemanticResolutionSuccess.class, success ->
                assertThat(success.model().numericFactOf(ast.resultExpression().orElseThrow().id()).shape())
                        .isEqualTo(NumericFactShape.FRACTIONAL_KNOWN));
    }

    @Test
    void groupingALiteralPreservesItsNumericFact() {
        ExpressionFileNode ast = ast("(5)");
        GroupedExpressionNode grouped = (GroupedExpressionNode) ast.resultExpression().orElseThrow();
        SemanticResolutionResult result = new SemanticResolver().resolve(ast, ExpressionEnvironment.standard());

        assertThat(result).isInstanceOfSatisfying(SemanticResolutionSuccess.class, success -> {
            NumericFact fact = success.model().numericFactOf(grouped.id());
            assertThat(fact.shape()).isEqualTo(NumericFactShape.INTEGRAL_KNOWN);
            assertThat(fact.parity().signum()).isEqualTo(1);
        });
    }

    @Test
    void negatingALiteralFlipsSignAndPreservesIntegralShape() {
        ExpressionFileNode ast = ast("-3");
        UnaryOperationNode negation = (UnaryOperationNode) ast.resultExpression().orElseThrow();
        SemanticResolutionResult result = new SemanticResolver().resolve(ast, ExpressionEnvironment.standard());

        assertThat(result).isInstanceOfSatisfying(SemanticResolutionSuccess.class, success -> {
            NumericFact fact = success.model().numericFactOf(negation.id());
            assertThat(fact.shape()).isEqualTo(NumericFactShape.INTEGRAL_KNOWN);
            assertThat(fact.parity().signum()).isEqualTo(-1);
        });
    }

    @Test
    void additionOfTwoIntegralOperandsStaysIntegralWithoutParity() {
        ExpressionFileNode ast = ast("1 + 2");
        BinaryOperationNode addition = (BinaryOperationNode) ast.resultExpression().orElseThrow();
        SemanticResolutionResult result = new SemanticResolver().resolve(ast, ExpressionEnvironment.standard());

        assertThat(result).isInstanceOfSatisfying(SemanticResolutionSuccess.class, success -> {
            NumericFact fact = success.model().numericFactOf(addition.id());
            assertThat(fact.shape()).isEqualTo(NumericFactShape.INTEGRAL_KNOWN);
            assertThat(fact.hasParity()).isFalse();
        });
    }

    @Test
    void additionInvolvingAFractionalOperandIsUnknownShape() {
        ExpressionFileNode ast = ast("1 + 2.5");
        BinaryOperationNode addition = (BinaryOperationNode) ast.resultExpression().orElseThrow();
        SemanticResolutionResult result = new SemanticResolver().resolve(ast, ExpressionEnvironment.standard());

        assertThat(result).isInstanceOfSatisfying(SemanticResolutionSuccess.class, success ->
                assertThat(success.model().numericFactOf(addition.id()).shape())
                        .isEqualTo(NumericFactShape.UNKNOWN_NUMERIC_VALUE_SHAPE));
    }

    @Test
    void dynamicSymbolAdditionIsUnknownShape() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("x", ScalarType.NUMBER, BigDecimal.ONE, ExternalSymbolOverwritePolicy.FIXED)
                .build();
        ExpressionFileNode ast = ast("x + 1");
        BinaryOperationNode addition = (BinaryOperationNode) ast.resultExpression().orElseThrow();
        SemanticResolutionResult result = new SemanticResolver().resolve(ast, environment);

        assertThat(result).isInstanceOfSatisfying(SemanticResolutionSuccess.class, success ->
                assertThat(success.model().numericFactOf(addition.id()).shape())
                        .isEqualTo(NumericFactShape.UNKNOWN_NUMERIC_VALUE_SHAPE));
    }

    @Test
    void negativeBaseWithEvenReducedExponentDenominatorIsComplexDomainError() {
        assertSemanticFailure("(-8) ^ 0.5", DiagnosticCode.SEMANTIC_POWER_COMPLEX_DOMAIN);
    }

    @Test
    void negativeBaseWithOddReducedExponentDenominatorIsReal() {
        assertSemanticSuccess("(-8) ^ 3");
    }

    @Test
    void zeroBaseWithNegativeExponentIsUndefined() {
        assertSemanticFailure("0 ^ -1", DiagnosticCode.SEMANTIC_POWER_UNDEFINED);
    }

    @Test
    void zeroBaseWithZeroExponentIsDefinedAsOne() {
        assertSemanticSuccess("0 ^ 0");
    }

    @Test
    void positiveConstantBaseWithDynamicExponentNeedsNoDeferredCheck() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("exponent", ScalarType.NUMBER, BigDecimal.ONE, ExternalSymbolOverwritePolicy.FIXED)
                .build();
        ExpressionFileNode ast = ast("2 ^ exponent");
        SemanticResolutionResult result = new SemanticResolver().resolve(ast, environment);

        assertThat(result).isInstanceOfSatisfying(SemanticResolutionSuccess.class, success ->
                assertThat(success.model().deferredChecks()).isEmpty());
    }

    @Test
    void dynamicBaseWithConstantExponentNeedsAPowerDeferredCheck() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("base", ScalarType.NUMBER, BigDecimal.ONE, ExternalSymbolOverwritePolicy.FIXED)
                .build();
        ExpressionFileNode ast = ast("base ^ 2");
        BinaryOperationNode power = (BinaryOperationNode) ast.resultExpression().orElseThrow();
        SemanticResolutionResult result = new SemanticResolver().resolve(ast, environment);

        assertThat(result).isInstanceOfSatisfying(SemanticResolutionSuccess.class, success ->
                assertThat(success.model().deferredChecks())
                        .containsExactly(new PowerRealDomainDeferredCheck(power.id(), power.operatorSpan())));
    }

    @Test
    void radicandNegativeWithEvenReducedDegreeNumeratorIsComplexDomainError() {
        assertSemanticFailure("2 root (-8)", DiagnosticCode.SEMANTIC_ROOT_COMPLEX_DOMAIN);
    }

    @Test
    void radicandNegativeWithOddReducedDegreeNumeratorIsReal() {
        assertSemanticSuccess("3 root (-27)");
    }

    @Test
    void rootDegreeZeroIsUndefined() {
        assertSemanticFailure("0 root 4", DiagnosticCode.SEMANTIC_ROOT_UNDEFINED);
    }

    @Test
    void zeroRadicandWithNegativeDegreeIsUndefined() {
        assertSemanticFailure("(-2) root 0", DiagnosticCode.SEMANTIC_ROOT_UNDEFINED);
    }

    @Test
    void oddPositiveConstantDegreeWithDynamicRadicandNeedsNoDeferredCheck() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("radicand", ScalarType.NUMBER, BigDecimal.ONE, ExternalSymbolOverwritePolicy.FIXED)
                .build();
        ExpressionFileNode ast = ast("3 root radicand");
        SemanticResolutionResult result = new SemanticResolver().resolve(ast, environment);

        assertThat(result).isInstanceOfSatisfying(SemanticResolutionSuccess.class, success ->
                assertThat(success.model().deferredChecks()).isEmpty());
    }

    @Test
    void dynamicDegreeWithConstantRadicandNeedsARootDeferredCheck() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("degree", ScalarType.NUMBER, BigDecimal.ONE, ExternalSymbolOverwritePolicy.FIXED)
                .build();
        ExpressionFileNode ast = ast("degree root 9");
        BinaryOperationNode root = (BinaryOperationNode) ast.resultExpression().orElseThrow();
        SemanticResolutionResult result = new SemanticResolver().resolve(ast, environment);

        assertThat(result).isInstanceOfSatisfying(SemanticResolutionSuccess.class, success ->
                assertThat(success.model().deferredChecks())
                        .containsExactly(new RootRealDomainDeferredCheck(root.id(), root.operatorSpan())));
    }

    @Test
    void constantFactorialResolvesWithoutDiagnostics() {
        ExpressionFileNode ast = ast("5!");
        PostfixOperationNode postfix = (PostfixOperationNode) ast.resultExpression().orElseThrow();
        SemanticResolutionResult result = new SemanticResolver().resolve(ast, ExpressionEnvironment.standard());

        assertThat(result).isInstanceOfSatisfying(SemanticResolutionSuccess.class, success -> {
            assertThat(success.model().deferredChecks()).isEmpty();
            assertThat(success.model().numericFactOf(postfix.id()).shape()).isEqualTo(NumericFactShape.INTEGRAL_KNOWN);
        });
    }

    @Test
    void negativeConstantFactorialIsASemanticError() {
        assertSemanticFailure("(-1)!", DiagnosticCode.SEMANTIC_FACTORIAL_NEGATIVE);
    }

    @Test
    void fractionalConstantFactorialIsASemanticError() {
        assertSemanticFailure("2.5!", DiagnosticCode.SEMANTIC_FACTORIAL_NOT_INTEGRAL);
    }

    @Test
    void constantFactorialAboveMaxFactorialInputIsASemanticError() {
        assertSemanticFailure("1001!", DiagnosticCode.SEMANTIC_FACTORIAL_EXCEEDS_MAXIMUM);
    }

    @Test
    void dynamicFactorialOperandRegistersTheThreeDeferredChecks() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("n", ScalarType.NUMBER, BigDecimal.ONE, ExternalSymbolOverwritePolicy.FIXED)
                .build();
        ExpressionFileNode ast = ast("n!");
        PostfixOperationNode postfix = (PostfixOperationNode) ast.resultExpression().orElseThrow();
        SemanticResolutionResult result = new SemanticResolver().resolve(ast, environment);

        assertThat(result).isInstanceOfSatisfying(SemanticResolutionSuccess.class, success ->
                assertThat(success.model().deferredChecks()).containsExactlyInAnyOrder(
                        new FactorialIntegralDeferredCheck(postfix.id(), postfix.operations().getFirst().sourceSpan()),
                        new FactorialNonNegativeDeferredCheck(postfix.id(), postfix.operations().getFirst().sourceSpan()),
                        new FactorialMaxBoundDeferredCheck(
                                postfix.id(), postfix.operations().getFirst().sourceSpan(), environment.maxFactorialInput())));
    }

    @Test
    void destructuringADynamicShapedCollectionRegistersAMinimumSizeDeferredCheck() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("values", new CollectionType(ScalarType.NUMBER), List.of(BigDecimal.ONE),
                        ExternalSymbolOverwritePolicy.FIXED)
                .build();
        ExpressionFileNode ast = ast("[first, second] := values;");
        AssignmentNode assignment = ast.assignments().getFirst();
        DestructuringAssignmentTargetNode target = (DestructuringAssignmentTargetNode) assignment.target();
        SemanticResolutionResult result = new SemanticResolver().resolve(ast, environment);

        assertThat(result).isInstanceOfSatisfying(SemanticResolutionSuccess.class, success ->
                assertThat(success.model().deferredChecks()).containsExactly(
                        new DestructuringMinimumSizeDeferredCheck(target.id(), target.sourceSpan(), 2)));
    }

    @Test
    void indexSubscriptOnADynamicShapedCollectionRegistersABoundsDeferredCheck() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("items", new CollectionType(ScalarType.NUMBER), List.of(BigDecimal.ONE),
                        ExternalSymbolOverwritePolicy.FIXED)
                .build();
        ExpressionFileNode ast = ast("items[0]");
        NavigationLink link = navigationChain(ast.resultExpression().orElseThrow()).links().getFirst();
        SemanticResolutionResult result = new SemanticResolver().resolve(ast, environment);

        assertThat(result).isInstanceOfSatisfying(SemanticResolutionSuccess.class, success ->
                assertThat(success.model().deferredChecks()).containsExactly(
                        new SubscriptBoundsDeferredCheck(link.id(), link.sourceSpan())));
    }

    @Test
    void sliceSubscriptOnADynamicShapedCollectionRegistersABoundsDeferredCheck() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("items", new CollectionType(ScalarType.NUMBER), List.of(BigDecimal.ONE),
                        ExternalSymbolOverwritePolicy.FIXED)
                .build();
        ExpressionFileNode ast = ast("items[0:1]");
        NavigationLink link = navigationChain(ast.resultExpression().orElseThrow()).links().getFirst();
        SemanticResolutionResult result = new SemanticResolver().resolve(ast, environment);

        assertThat(result).isInstanceOfSatisfying(SemanticResolutionSuccess.class, success ->
                assertThat(success.model().deferredChecks()).containsExactly(
                        new SubscriptBoundsDeferredCheck(link.id(), link.sourceSpan())));
    }

    @Test
    void wildcardOnADynamicShapedCollectionRegistersAMaterializationDeferredCheck() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("items", new CollectionType(ScalarType.NUMBER), List.of(BigDecimal.ONE),
                        ExternalSymbolOverwritePolicy.FIXED)
                .build();
        ExpressionFileNode ast = ast("items[*]");
        NavigationLink link = navigationChain(ast.resultExpression().orElseThrow()).links().getFirst();
        SemanticResolutionResult result = new SemanticResolver().resolve(ast, environment);

        assertThat(result).isInstanceOfSatisfying(SemanticResolutionSuccess.class, success ->
                assertThat(success.model().deferredChecks()).containsExactly(new MaterializationLimitDeferredCheck(
                        link.id(), link.sourceSpan(), environment.maxMaterializedSize())));
    }

    @Test
    void wildcardOnAMapRegistersAMaterializationDeferredCheck() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("m", new MapType(ScalarType.NUMBER), Map.of("a", BigDecimal.ONE),
                        ExternalSymbolOverwritePolicy.FIXED)
                .build();
        ExpressionFileNode ast = ast("m[*]");
        NavigationLink link = navigationChain(ast.resultExpression().orElseThrow()).links().getFirst();
        SemanticResolutionResult result = new SemanticResolver().resolve(ast, environment);

        assertThat(result).isInstanceOfSatisfying(SemanticResolutionSuccess.class, success ->
                assertThat(success.model().deferredChecks()).containsExactly(new MaterializationLimitDeferredCheck(
                        link.id(), link.sourceSpan(), environment.maxMaterializedSize())));
    }

    private static void assertSemanticSuccess(String source) {
        SemanticResolutionResult result = new SemanticResolver().resolve(ast(source), ExpressionEnvironment.standard());
        assertThat(result).isInstanceOf(SemanticResolutionSuccess.class);
    }

    private static void assertSemanticFailure(String source, DiagnosticCode expectedCode) {
        SemanticResolutionResult result = new SemanticResolver().resolve(ast(source), ExpressionEnvironment.standard());

        assertThat(result).isInstanceOfSatisfying(SemanticResolutionFailure.class, failure ->
                assertThat(failure.diagnostics())
                        .extracting(diagnostic -> diagnostic.code())
                        .contains(expectedCode.name()));
    }

    private static NavigationChainNode navigationChain(ExpressionNode expression) {
        return (NavigationChainNode) expression;
    }

    private static ExpressionFileNode ast(String source) {
        ParseSuccess parse = (ParseSuccess) new ExpressionParser().parse(source);
        return ((SemanticAstBuildSuccess) new SemanticAstBuilder().build(parse)).file();
    }
}
