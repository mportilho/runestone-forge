package com.runestone.expeval_mk3.internal.semantics;

import com.runestone.expeval_mk3.api.CollectionOperationCatalog.CardinalityPreservation;
import com.runestone.expeval_mk3.api.CollectionOperationCatalog.EvaluationPolicy;
import com.runestone.expeval_mk3.api.CollectionOperationCatalog.MaterializationPolicy;
import com.runestone.expeval_mk3.api.CollectionOperationCatalog.NumericResultFact;
import com.runestone.expeval_mk3.api.CollectionOperationCatalog.OperationIdentity;
import com.runestone.expeval_mk3.api.CollectionType;
import com.runestone.expeval_mk3.api.ExpressionEnvironment;
import com.runestone.expeval_mk3.api.ExpressionType;
import com.runestone.expeval_mk3.api.ExternalSymbolOverwritePolicy;
import com.runestone.expeval_mk3.api.FunctionPurity;
import com.runestone.expeval_mk3.api.MapType;
import com.runestone.expeval_mk3.api.RuntimeNullability;
import com.runestone.expeval_mk3.api.ScalarType;
import com.runestone.expeval_mk3.internal.ast.BinaryOperationNode;
import com.runestone.expeval_mk3.internal.ast.CollectionLiteralNode;
import com.runestone.expeval_mk3.internal.ast.ExpressionFileNode;
import com.runestone.expeval_mk3.internal.ast.NavigationChainNode;
import com.runestone.expeval_mk3.internal.ast.NavigationLink;
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

    @Test
    void bindsNavigatedCollectionOperationsByClosedCatalogIdentity() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol(
                        "items",
                        new CollectionType(ScalarType.NUMBER),
                        List.of(BigDecimal.ONE, new BigDecimal("2")),
                        ExternalSymbolOverwritePolicy.FIXED)
                .build();
        ExpressionFileNode ast = ast("items?.sum()");
        NavigationChainNode navigation = (NavigationChainNode) ast.resultExpression().orElseThrow();
        NavigationLink link = navigation.links().getFirst();

        SemanticResolutionResult result = new SemanticResolver().resolve(ast, environment);

        assertThat(result).isInstanceOfSatisfying(SemanticResolutionSuccess.class, success -> {
            SemanticModel model = success.model();
            assertThat(model.collectionOperationBindings()).containsKey(link.id());
            CollectionOperationBinding binding = model.collectionOperationBindings().get(link.id());
            assertThat(binding.identity()).isEqualTo(OperationIdentity.SUM);
            assertThat(binding.receiverType())
                    .isEqualTo(new CollectionType(ScalarType.NUMBER));
            assertThat(binding.resultNullability()).isEqualTo(RuntimeNullability.MAY_BE_NULL);
            assertThat(binding.evaluationPolicy()).isEqualTo(EvaluationPolicy.EAGER);
            assertThat(binding.pure()).isTrue();
            assertThat(binding.materializationPolicy()).isEqualTo(MaterializationPolicy.DOES_NOT_MATERIALIZE);
            assertThat(binding.numericResultFact()).isEqualTo(NumericResultFact.UNKNOWN_NUMERIC_VALUE_SHAPE);
            assertThat(binding.cardinalityPreservation()).isEqualTo(CardinalityPreservation.NOT_APPLICABLE);
            assertThat(model.resolvedTypes()).containsEntry(link.id(), ScalarType.NUMBER);
            assertThat(model.runtimeNullability()).containsEntry(link.id(), RuntimeNullability.MAY_BE_NULL);
            assertThat(model.runtimeNullability()).containsEntry(navigation.id(), RuntimeNullability.MAY_BE_NULL);
        });
    }

    @Test
    void bindsNavigatedMapOperationsWithMaterializedCollectionResultTypes() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("m", new MapType(ScalarType.NUMBER), Map.of("a", BigDecimal.ONE),
                        ExternalSymbolOverwritePolicy.FIXED)
                .build();

        assertMapOperationBinding("m.keys()", environment, OperationIdentity.KEYS, new CollectionType(ScalarType.STRING));
        assertMapOperationBinding("m.values()", environment, OperationIdentity.VALUES, new CollectionType(ScalarType.NUMBER));
        assertMapOperationBinding("m.count()", environment, OperationIdentity.COUNT, ScalarType.NUMBER);
    }

    @Test
    void preservesSafeOperationNullabilityThroughGroupingAndAssignment() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol(
                        "items",
                        new CollectionType(ScalarType.NUMBER),
                        List.of(BigDecimal.ONE),
                        ExternalSymbolOverwritePolicy.FIXED)
                .build();

        assertResultNullability("(items?.sum())", environment, RuntimeNullability.MAY_BE_NULL);
        assertResultNullability("value := items?.sum(); value", environment, RuntimeNullability.MAY_BE_NULL);
    }

    @Test
    void combinesCollectionOperationPurityWithImpureLambdaBindings() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functionsFrom(new ImpureFunctions(), FunctionPurity.IMPURE)
                .build();
        ExpressionFileNode ast = ast("items := [1]; items.map(@ -> touch(@))");
        NavigationChainNode navigation = (NavigationChainNode) ast.resultExpression().orElseThrow();
        NavigationLink link = navigation.links().getFirst();

        SemanticResolutionResult result = new SemanticResolver().resolve(ast, environment);

        assertThat(result).isInstanceOfSatisfying(SemanticResolutionSuccess.class, success -> {
            CollectionOperationBinding binding = success.model().collectionOperationBindings().get(link.id());
            assertThat(binding.pure()).isFalse();
            assertThat(binding.lambdaBindings()).singleElement().satisfies(lambda -> assertThat(lambda.pure()).isFalse());
        });
    }

    @Test
    void bindsWildcardNavigationWithElementTypeCardinalityNullabilityAccessorsAndMaterializationPolicy() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .registerJavaTypeWildcardChildren(WildcardChildProvider.class, "second", "first")
                .externalSymbol("object", new WildcardChildProvider(), ExternalSymbolOverwritePolicy.FIXED)
                .build();
        ExpressionFileNode ast = ast("object?.[*]");
        NavigationChainNode navigation = (NavigationChainNode) ast.resultExpression().orElseThrow();
        NavigationLink link = navigation.links().getFirst();

        SemanticResolutionResult result = new SemanticResolver().resolve(ast, environment);

        assertThat(result).isInstanceOfSatisfying(SemanticResolutionSuccess.class, success -> {
            SemanticModel model = success.model();
            assertThat(model.resolvedTypes()).containsEntry(link.id(), new CollectionType(ScalarType.NUMBER));
            assertThat(model.runtimeNullability()).containsEntry(link.id(), RuntimeNullability.MAY_BE_NULL);
            assertThat(model.collectionShapes()).containsEntry(link.id(), new CollectionShape(2));
            assertThat(model.wildcardNavigationBindings()).containsKey(link.id());
            WildcardNavigationBinding binding = model.wildcardNavigationBindings().get(link.id());
            assertThat(binding.receiverKind()).isEqualTo(WildcardNavigationBinding.ReceiverKind.OBJECT);
            assertThat(binding.elementType()).isEqualTo(ScalarType.NUMBER);
            assertThat(binding.resultShape()).isEqualTo(new CollectionShape(2));
            assertThat(binding.resultNullability()).isEqualTo(RuntimeNullability.MAY_BE_NULL);
            assertThat(binding.materializationPolicy()).isEqualTo(MaterializationPolicy.MATERIALIZES);
            assertThat(binding.objectChildren())
                    .extracting(child -> child.name())
                    .containsExactly("second", "first");
        });
    }

    @Test
    void bindsCollectionAndMapWildcardNavigationTypesAndNullability() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol(
                        "items",
                        new CollectionType(ScalarType.NUMBER),
                        List.of(BigDecimal.ONE, new BigDecimal("2")),
                        ExternalSymbolOverwritePolicy.FIXED)
                .externalSymbol("m", new MapType(ScalarType.STRING), Map.of("b", "B"),
                        ExternalSymbolOverwritePolicy.FIXED)
                .build();

        assertWildcardBinding(
                "items := [1, 2]; items[*]",
                ExpressionEnvironment.standard(),
                WildcardNavigationBinding.ReceiverKind.COLLECTION,
                ScalarType.NUMBER,
                RuntimeNullability.NEVER_NULL,
                new CollectionShape(2));
        assertWildcardBinding(
                "m?.[*]",
                environment,
                WildcardNavigationBinding.ReceiverKind.MAP,
                ScalarType.STRING,
                RuntimeNullability.MAY_BE_NULL,
                CollectionShape.unknown());
    }

    private static ExpressionFileNode ast(String source) {
        ParseSuccess parse = (ParseSuccess) new ExpressionParser().parse(source);
        return ((SemanticAstBuildSuccess) new SemanticAstBuilder().build(parse)).file();
    }

    private static void assertMapOperationBinding(
            String source,
            ExpressionEnvironment environment,
            OperationIdentity identity,
            ExpressionType expectedType) {
        ExpressionFileNode ast = ast(source);
        NavigationChainNode navigation = (NavigationChainNode) ast.resultExpression().orElseThrow();
        NavigationLink link = navigation.links().getFirst();

        SemanticResolutionResult result = new SemanticResolver().resolve(ast, environment);

        assertThat(result).isInstanceOfSatisfying(SemanticResolutionSuccess.class, success -> {
            SemanticModel model = success.model();
            assertThat(model.collectionOperationBindings().get(link.id()).identity())
                    .isEqualTo(identity);
            assertThat(model.resolvedTypes()).containsEntry(link.id(), expectedType);
            assertThat(model.runtimeNullability()).containsEntry(link.id(), RuntimeNullability.NEVER_NULL);
        });
    }

    private static void assertResultNullability(
            String source,
            ExpressionEnvironment environment,
            RuntimeNullability expectedNullability) {
        ExpressionFileNode ast = ast(source);
        SemanticResolutionResult result = new SemanticResolver().resolve(ast, environment);

        assertThat(result).isInstanceOfSatisfying(SemanticResolutionSuccess.class, success ->
                assertThat(success.model().runtimeNullability())
                        .containsEntry(ast.resultExpression().orElseThrow().id(), expectedNullability));
    }

    private static void assertWildcardBinding(
            String source,
            ExpressionEnvironment environment,
            WildcardNavigationBinding.ReceiverKind receiverKind,
            ExpressionType elementType,
            RuntimeNullability nullability,
            CollectionShape shape) {
        ExpressionFileNode ast = ast(source);
        NavigationChainNode navigation = (NavigationChainNode) ast.resultExpression().orElseThrow();
        NavigationLink link = navigation.links().getFirst();

        SemanticResolutionResult result = new SemanticResolver().resolve(ast, environment);

        assertThat(result).isInstanceOfSatisfying(SemanticResolutionSuccess.class, success -> {
            WildcardNavigationBinding binding = success.model().wildcardNavigationBindings().get(link.id());
            assertThat(binding.receiverKind()).isEqualTo(receiverKind);
            assertThat(binding.elementType()).isEqualTo(elementType);
            assertThat(binding.resultNullability()).isEqualTo(nullability);
            assertThat(binding.resultShape()).isEqualTo(shape);
            assertThat(success.model().runtimeNullability()).containsEntry(link.id(), nullability);
        });
    }

    public static final class ImpureFunctions {

        public BigDecimal touch(BigDecimal value) {
            return value;
        }
    }

    public static final class WildcardChildProvider {

        public BigDecimal first() {
            return BigDecimal.ONE;
        }

        public BigDecimal second() {
            return new BigDecimal("2");
        }
    }
}
