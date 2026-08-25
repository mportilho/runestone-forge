package com.runestone.expeval_mk3.internal.semantics;

import com.runestone.expeval_mk3.api.DiagnosticCategory;
import com.runestone.expeval_mk3.api.ExpressionDiagnostic;
import com.runestone.expeval_mk3.api.ExpressionEnvironment;
import com.runestone.expeval_mk3.api.ExternalSymbolOverwritePolicy;
import com.runestone.expeval_mk3.api.ScalarType;
import com.runestone.expeval_mk3.api.SourceSpan;
import com.runestone.expeval_mk3.internal.ast.AssignmentNode;
import com.runestone.expeval_mk3.internal.ast.BinaryOperationNode;
import com.runestone.expeval_mk3.internal.ast.CurrentItemNode;
import com.runestone.expeval_mk3.internal.ast.ExpressionFileNode;
import com.runestone.expeval_mk3.internal.ast.ExpressionNode;
import com.runestone.expeval_mk3.internal.ast.FunctionCallNode;
import com.runestone.expeval_mk3.internal.ast.FunctionName;
import com.runestone.expeval_mk3.internal.ast.IdentifierAssignmentTargetNode;
import com.runestone.expeval_mk3.internal.ast.LambdaCallArgument;
import com.runestone.expeval_mk3.internal.ast.LambdaNode;
import com.runestone.expeval_mk3.internal.ast.LiteralNode;
import com.runestone.expeval_mk3.internal.ast.NodeId;
import com.runestone.expeval_mk3.internal.ast.SemanticAstBuildSuccess;
import com.runestone.expeval_mk3.internal.ast.SemanticAstBuilder;
import com.runestone.expeval_mk3.internal.parser.ExpressionParser;
import com.runestone.expeval_mk3.internal.parser.ParseSuccess;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Proves the Etapa 4 gate from issue #93: {@code SemanticResolutionSuccess} can only carry a
 * {@link SemanticModel} whose mandatory metadata families are complete, and construction fails loudly
 * the instant any family is missing an entry for a node that survived into the model. This is the
 * combination the four blocking tickets (#89-#92) each covered individually but never proved together.
 */
class SemanticModelCompletenessGateTest {

    private static final String RICH_SOURCE = """
            items := [1, 2, 3];
            [first, second] := items;
            total := first + second / 2 - 1 ^ 2 root 3;
            fact := second!;
            filtered := items[?(@ > 1)];
            mapped := items.map(@ -> @ * 2);
            wide := items[*];
            sub := items[0:1];
            matched := "abc" =~ "a.c";
            coalesced := items[0] ?? 0;
            branch := if total > 0 then total else -total endif;
            sqrt(abs(branch))""";

    @Test
    void positiveCompletenessWalkAcceptsARichModelCoveringEveryMandatoryFamily() {
        SemanticResolutionResult result = new SemanticResolver().resolve(ast(RICH_SOURCE), environment());

        assertThat(result).isInstanceOfSatisfying(SemanticResolutionSuccess.class, success -> {
            SemanticModel model = success.model();
            assertThat(model.resolvedTypes()).isNotEmpty();
            assertThat(model.resolvedTypes().keySet())
                    .isSubsetOf(model.runtimeNullability().keySet())
                    .isSubsetOf(model.expressionPurity().keySet());
            assertThat(model.deferredChecks()).isNotEmpty();
            assertThat(model.numericFacts()).isNotEmpty();
            assertThat(model.frameLayout()).isNotNull();
        });
    }

    @Test
    void rejectsAModelMissingAResolvedType() {
        SemanticModel model = resolve("1 + 2");
        BinaryOperationNode addition = (BinaryOperationNode) ast("1 + 2").resultExpression().orElseThrow();
        Map<NodeId, com.runestone.expeval_mk3.api.ExpressionType> resolvedTypes = new HashMap<>(model.resolvedTypes());
        resolvedTypes.remove(addition.left().id());

        assertThatThrownBy(() -> rebuild(model, builder -> builder.resolvedTypes = resolvedTypes))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("resolved type");
    }

    @Test
    void rejectsAModelMissingRuntimeNullability() {
        SemanticModel model = resolve("1 + 2");
        BinaryOperationNode addition = (BinaryOperationNode) ast("1 + 2").resultExpression().orElseThrow();
        Map<NodeId, com.runestone.expeval_mk3.api.RuntimeNullability> nullability =
                new HashMap<>(model.runtimeNullability());
        nullability.remove(addition.left().id());

        assertThatThrownBy(() -> rebuild(model, builder -> builder.runtimeNullability = nullability))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("runtime nullability");
    }

    @Test
    void rejectsAModelMissingPurity() {
        SemanticModel model = resolve("1 + 2");
        BinaryOperationNode addition = (BinaryOperationNode) ast("1 + 2").resultExpression().orElseThrow();
        Map<NodeId, Boolean> purity = new HashMap<>(model.expressionPurity());
        purity.remove(addition.left().id());

        assertThatThrownBy(() -> rebuild(model, builder -> builder.expressionPurity = purity))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("purity");
    }

    @Test
    void rejectsAModelMissingAPreparedLiteralValue() {
        SemanticModel model = resolve("1 + 2");
        BinaryOperationNode addition = (BinaryOperationNode) ast("1 + 2").resultExpression().orElseThrow();
        Map<NodeId, Object> preparedValues = new HashMap<>(model.preparedValues());
        preparedValues.remove(((LiteralNode) addition.left()).id());

        assertThatThrownBy(() -> rebuild(model, builder -> builder.preparedValues = preparedValues))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("prepared literal value");
    }

    @Test
    void rejectsAModelMissingACollectionShape() {
        SemanticModel model = resolve("[1, 2]");
        ExpressionNode collection = ast("[1, 2]").resultExpression().orElseThrow();
        Map<NodeId, CollectionShape> collectionShapes = new HashMap<>(model.collectionShapes());
        collectionShapes.remove(collection.id());

        assertThatThrownBy(() -> rebuild(model, builder -> builder.collectionShapes = collectionShapes))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("collection shape");
    }

    @Test
    void rejectsAModelMissingASymbolBindingForAnIdentifier() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("x", ScalarType.NUMBER, BigDecimal.ONE, ExternalSymbolOverwritePolicy.FIXED)
                .build();
        SemanticModel model = resolve("x", environment);
        ExpressionNode identifier = ast("x").resultExpression().orElseThrow();
        Map<NodeId, SymbolBinding> symbolBindings = new HashMap<>(model.symbolBindings());
        symbolBindings.remove(identifier.id());

        assertThatThrownBy(() -> rebuild(model, builder -> builder.symbolBindings = symbolBindings))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("symbol binding");
    }

    @Test
    void rejectsAModelMissingAnAssignmentTargetBinding() {
        SemanticModel model = resolve("value := 1;");
        AssignmentNode assignment = ast("value := 1;").assignments().getFirst();
        NodeId targetId = ((IdentifierAssignmentTargetNode) assignment.target()).id();
        Map<NodeId, SymbolBinding> symbolBindings = new HashMap<>(model.symbolBindings());
        symbolBindings.remove(targetId);

        assertThatThrownBy(() -> rebuild(model, builder -> builder.symbolBindings = symbolBindings))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("assignment target binding");
    }

    @Test
    void rejectsAModelMissingAnEqualityBinding() {
        SemanticModel model = resolve("1 = 1");
        BinaryOperationNode equality = (BinaryOperationNode) ast("1 = 1").resultExpression().orElseThrow();
        Map<NodeId, com.runestone.expeval_mk3.api.ExpressionType> equalityOperandTypes =
                new HashMap<>(model.equalityOperandTypes());
        equalityOperandTypes.remove(equality.id());

        assertThatThrownBy(() -> rebuild(model, builder -> builder.equalityOperandTypes = equalityOperandTypes))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("equality binding");
    }

    @Test
    void rejectsAModelMissingAPreparedRegexPattern() {
        SemanticModel model = resolve("\"abc\" =~ \"a.c\"");
        BinaryOperationNode regexMatch = (BinaryOperationNode) ast("\"abc\" =~ \"a.c\"").resultExpression().orElseThrow();
        Map<NodeId, Object> preparedValues = new HashMap<>(model.preparedValues());
        preparedValues.remove(regexMatch.id());

        assertThatThrownBy(() -> rebuild(model, builder -> builder.preparedValues = preparedValues))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("prepared regex pattern");
    }

    @Test
    void rejectsAModelWhereAGlobalFunctionCallCarriesALambdaArgument() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("x", ScalarType.NUMBER, BigDecimal.ONE, ExternalSymbolOverwritePolicy.FIXED)
                .build();
        SemanticModel model = resolve("sqrt(x)", environment);
        FunctionCallNode originalCall = (FunctionCallNode) ast("sqrt(x)").resultExpression().orElseThrow();
        SourceSpan span = new SourceSpan(0, 0, 1, 1);
        LambdaNode lambda = new LambdaNode(
                new NodeId(500), span, new CurrentItemNode(new NodeId(501), span), span,
                new CurrentItemNode(new NodeId(502), span));
        FunctionCallNode tampered = new FunctionCallNode(
                originalCall.id(), originalCall.sourceSpan(), new FunctionName("sqrt"),
                List.of(new LambdaCallArgument(lambda)));
        ExpressionFileNode tamperedAst = new ExpressionFileNode(
                model.ast().id(), model.ast().sourceSpan(), model.ast().assignments(), java.util.Optional.of(tampered));

        assertThatThrownBy(() -> rebuild(model, builder -> builder.ast = tamperedAst))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("unsupported lambda argument");
    }

    @Test
    void rejectsAModelMissingAFunctionBinding() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("x", ScalarType.NUMBER, BigDecimal.ONE, ExternalSymbolOverwritePolicy.FIXED)
                .build();
        SemanticModel model = resolve("sqrt(x)", environment);
        Map<NodeId, com.runestone.expeval_mk3.api.FunctionDescriptor> functionBindings =
                new HashMap<>(model.functionBindings());
        functionBindings.clear();

        assertThatThrownBy(() -> rebuild(model, builder -> builder.functionBindings = functionBindings))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("function binding");
    }

    @Test
    void rejectsAModelMissingANavigationBinding() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("items", new com.runestone.expeval_mk3.api.CollectionType(ScalarType.NUMBER),
                        List.of(BigDecimal.ONE), ExternalSymbolOverwritePolicy.FIXED)
                .build();
        SemanticModel model = resolve("items[0]", environment);
        Map<NodeId, NavigationBinding> navigationBindings = new HashMap<>(model.navigationBindings());
        navigationBindings.clear();

        assertThatThrownBy(() -> rebuild(model, builder -> builder.navigationBindings = navigationBindings))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("navigation binding");
    }

    @Test
    void rejectsAModelMissingANumericFactForAKnownNumericNode() {
        SemanticModel model = resolve("1 + 2");
        BinaryOperationNode addition = (BinaryOperationNode) ast("1 + 2").resultExpression().orElseThrow();
        Map<NodeId, NumericFact> numericFacts = new HashMap<>(model.numericFacts());
        numericFacts.remove(addition.id());

        assertThatThrownBy(() -> rebuild(model, builder -> builder.numericFacts = numericFacts))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("numeric fact");
    }

    @Test
    void rejectsAModelWithADeferredCheckReferencingAnUnknownNode() {
        SemanticModel model = resolve("5!");
        List<DeferredCheck> deferredChecks = new java.util.ArrayList<>(model.deferredChecks());
        deferredChecks.add(new SubscriptBoundsDeferredCheck(new NodeId(999_999), new SourceSpan(0, 0, 1, 1)));

        assertThatThrownBy(() -> rebuild(model, builder -> builder.deferredChecks = deferredChecks))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("referencing unknown node");
    }

    @Test
    void rejectsAModelWhereAnExternalBindingIsMissingFromTheFrameLayout() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("x", ScalarType.NUMBER, BigDecimal.ONE, ExternalSymbolOverwritePolicy.FIXED)
                .build();
        SemanticModel model = resolve("x", environment);

        assertThatThrownBy(() -> rebuild(model, builder -> builder.frameLayout = new FrameLayout(List.of(), 0, 1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("canonical frame layout");
    }

    @Test
    void successRejectsErrorSeverityWarnings() {
        ExpressionDiagnostic error = ExpressionDiagnostic.error(
                DiagnosticCategory.SEMANTIC, "SEMANTIC_TEST", "boom", new SourceSpan(0, 0, 1, 1));

        assertThatThrownBy(() -> new SemanticResolutionSuccess(resolve("1"), List.of(error)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void failureRejectsAWarningOnlyDiagnosticList() {
        ExpressionDiagnostic warning = ExpressionDiagnostic.warning(
                DiagnosticCategory.SEMANTIC, "SEMANTIC_TEST", "heads up", new SourceSpan(0, 0, 1, 1));

        assertThatThrownBy(() -> new SemanticResolutionFailure(List.of(warning)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static SemanticModel resolve(String source) {
        return resolve(source, ExpressionEnvironment.standard());
    }

    private static SemanticModel resolve(String source, ExpressionEnvironment environment) {
        SemanticResolutionSuccess result =
                (SemanticResolutionSuccess) new SemanticResolver().resolve(ast(source), environment);
        return result.model();
    }

    private static void rebuild(SemanticModel model, java.util.function.Consumer<Builder> mutator) {
        Builder builder = new Builder(model);
        mutator.accept(builder);
        builder.build();
    }

    private static ExpressionEnvironment environment() {
        return ExpressionEnvironment.builder()
                .build();
    }

    private static ExpressionFileNode ast(String source) {
        ParseSuccess parse = (ParseSuccess) new ExpressionParser().parse(source);
        return ((SemanticAstBuildSuccess) new SemanticAstBuilder().build(parse)).file();
    }

    /** Mutable staging area mirroring {@link SemanticModel}'s constructor arguments for malformed-model tests. */
    private static final class Builder {
        private ExpressionFileNode ast;
        private Map<NodeId, com.runestone.expeval_mk3.api.ExpressionType> resolvedTypes;
        private Map<NodeId, com.runestone.expeval_mk3.api.RuntimeNullability> runtimeNullability;
        private Map<NodeId, Object> preparedValues;
        private Map<NodeId, CollectionShape> collectionShapes;
        private Map<NodeId, Boolean> expressionPurity;
        private Map<NodeId, SymbolBinding> symbolBindings;
        private Map<NodeId, com.runestone.expeval_mk3.api.ExpressionType> equalityOperandTypes;
        private Map<NodeId, com.runestone.expeval_mk3.api.FunctionDescriptor> functionBindings;
        private Map<NodeId, NavigationBinding> navigationBindings;
        private Map<NodeId, NumericFact> numericFacts;
        private List<DeferredCheck> deferredChecks;
        private FrameLayout frameLayout;

        private Builder(SemanticModel model) {
            this.ast = model.ast();
            this.resolvedTypes = model.resolvedTypes();
            this.runtimeNullability = model.runtimeNullability();
            this.preparedValues = model.preparedValues();
            this.collectionShapes = model.collectionShapes();
            this.expressionPurity = model.expressionPurity();
            this.symbolBindings = model.symbolBindings();
            this.equalityOperandTypes = model.equalityOperandTypes();
            this.functionBindings = model.functionBindings();
            this.navigationBindings = model.navigationBindings();
            this.numericFacts = model.numericFacts();
            this.deferredChecks = model.deferredChecks();
            this.frameLayout = model.frameLayout();
        }

        private void build() {
            new SemanticModel(
                    ast,
                    resolvedTypes,
                    runtimeNullability,
                    preparedValues,
                    collectionShapes,
                    expressionPurity,
                    symbolBindings,
                    equalityOperandTypes,
                    functionBindings,
                    navigationBindings,
                    numericFacts,
                    deferredChecks,
                    frameLayout);
        }
    }
}
