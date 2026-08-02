package com.runestone.expeval_mk3.internal.semantics;

import com.runestone.expeval_mk3.api.CollectionOperationCatalog.CardinalityPreservation;
import com.runestone.expeval_mk3.api.CollectionOperationCatalog.EvaluationPolicy;
import com.runestone.expeval_mk3.api.CollectionOperationCatalog.MaterializationPolicy;
import com.runestone.expeval_mk3.api.CollectionOperationCatalog.NumericResultFact;
import com.runestone.expeval_mk3.api.CollectionOperationCatalog.OperationIdentity;
import com.runestone.expeval_mk3.api.CollectionType;
import com.runestone.expeval_mk3.api.DiagnosticSeverity;
import com.runestone.expeval_mk3.api.ExpressionEnvironment;
import com.runestone.expeval_mk3.api.ExpressionType;
import com.runestone.expeval_mk3.api.ExternalSymbolOverwritePolicy;
import com.runestone.expeval_mk3.api.FunctionPurity;
import com.runestone.expeval_mk3.api.MapType;
import com.runestone.expeval_mk3.api.RuntimeNullability;
import com.runestone.expeval_mk3.api.ScalarType;
import com.runestone.expeval_mk3.internal.ast.BinaryOperationNode;
import com.runestone.expeval_mk3.internal.ast.CollectionLiteralNode;
import com.runestone.expeval_mk3.internal.ast.ConditionalNode;
import com.runestone.expeval_mk3.internal.ast.ExpressionFileNode;
import com.runestone.expeval_mk3.internal.ast.ExpressionNode;
import com.runestone.expeval_mk3.internal.ast.GroupedExpressionNode;
import com.runestone.expeval_mk3.internal.ast.NavigationChainNode;
import com.runestone.expeval_mk3.internal.ast.NavigationLink;
import com.runestone.expeval_mk3.internal.ast.NullCoalesceNode;
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
                assertThat(diagnostic.code()).isEqualTo(DiagnosticCode.SEMANTIC_EMPTY_COLLECTION_REQUIRES_CONTEXT.name());
                assertThat(diagnostic.primarySpan().orElseThrow().offset()).isZero();
                assertThat(diagnostic.primarySpan().orElseThrow().endOffset()).isEqualTo(2);
                assertThat(diagnostic.primarySpan().orElseThrow().line()).isEqualTo(1);
                assertThat(diagnostic.primarySpan().orElseThrow().column()).isEqualTo(1);
            });
        });
    }

    @Test
    void keepsTheEmptyLiteralSpanWhenGroupingDoesNotProvideContext() {
        SemanticResolutionResult result = new SemanticResolver().resolve(ast("([])"), ExpressionEnvironment.standard());

        assertThat(result).isInstanceOfSatisfying(SemanticResolutionFailure.class, failure -> {
            assertThat(failure.diagnostics()).singleElement().satisfies(diagnostic -> {
                assertThat(diagnostic.code()).isEqualTo(DiagnosticCode.SEMANTIC_EMPTY_COLLECTION_REQUIRES_CONTEXT.name());
                assertThat(diagnostic.primarySpan().orElseThrow().offset()).isEqualTo(1);
                assertThat(diagnostic.primarySpan().orElseThrow().endOffset()).isEqualTo(3);
            });
        });
    }

    @Test
    void shadowingAnExternalSymbolIsAWarningNotAnError() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("x", ScalarType.NUMBER, BigDecimal.TEN, ExternalSymbolOverwritePolicy.FIXED)
                .build();

        SemanticResolutionResult result = new SemanticResolver().resolve(ast("x := 1; x"), environment);

        assertThat(result).isInstanceOfSatisfying(SemanticResolutionSuccess.class, success -> {
            assertThat(success.warnings()).singleElement().satisfies(diagnostic -> {
                assertThat(diagnostic.severity()).isEqualTo(DiagnosticSeverity.WARNING);
                assertThat(diagnostic.code()).isEqualTo(DiagnosticCode.SEMANTIC_SYMBOL_SHADOWING.name());
            });
        });
    }

    @Test
    void shadowingKeepsExternalAndInternalIdentitiesDistinctWhenTheExternalIsReferenced() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("x", ScalarType.NUMBER, BigDecimal.TEN, ExternalSymbolOverwritePolicy.FIXED)
                .build();

        SemanticResolutionResult result = new SemanticResolver().resolve(ast("x := x + 1; x"), environment);

        assertThat(result).isInstanceOfSatisfying(SemanticResolutionSuccess.class, success -> {
            SemanticModel model = success.model();
            assertThat(success.warnings()).singleElement().satisfies(diagnostic ->
                    assertThat(diagnostic.code()).isEqualTo(DiagnosticCode.SEMANTIC_SYMBOL_SHADOWING.name()));
            assertThat(model.frameLayout().externalBindings()).extracting(SymbolBinding::name).containsExactly("x");
            SymbolBinding externalX = model.frameLayout().externalBindings().getFirst();
            SymbolBinding internalX = model.symbolBindings().values().stream()
                    .filter(binding -> !binding.external())
                    .findFirst().orElseThrow();
            assertThat(externalX.frameSlot()).isEqualTo(0);
            assertThat(internalX.frameSlot()).isEqualTo(1);
            assertThat(internalX.name()).isEqualTo("x");
        });
    }

    @Test
    void nestedCurrentItemDepthsInterleaveWithLazilyAllocatedExternalSlots() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("matrix", new CollectionType(new CollectionType(ScalarType.NUMBER)),
                        List.of(List.of(BigDecimal.ONE)), ExternalSymbolOverwritePolicy.FIXED)
                .externalSymbol("threshold", ScalarType.NUMBER, BigDecimal.ZERO, ExternalSymbolOverwritePolicy.FIXED)
                .build();

        SemanticResolutionResult result = new SemanticResolver().resolve(
                ast("matrix[?(@.any(@ -> @ > threshold))]"), environment);

        assertThat(result).isInstanceOfSatisfying(SemanticResolutionSuccess.class, success -> {
            SemanticModel model = success.model();
            FrameLayout frameLayout = model.frameLayout();
            assertThat(frameLayout.externalBindings()).extracting(SymbolBinding::name)
                    .containsExactly("matrix", "threshold");
            assertThat(frameLayout.externalBindings().get(0).frameSlot()).isZero();
            assertThat(frameLayout.externalBindings().get(1).frameSlot()).isEqualTo(3);
            assertThat(frameLayout.frameSize()).isEqualTo(4);

            List<SymbolBinding> currentItemBindings = model.symbolBindings().values().stream()
                    .filter(binding -> binding.name().startsWith("@"))
                    .distinct()
                    .sorted((a, b) -> Integer.compare(a.frameSlot(), b.frameSlot()))
                    .toList();
            assertThat(currentItemBindings).extracting(SymbolBinding::frameSlot).containsExactly(1, 2);
        });
    }

    @Test
    void unusedExternalSymbolsConsumeNoFrameSlot() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("used", ScalarType.NUMBER, BigDecimal.ONE, ExternalSymbolOverwritePolicy.FIXED)
                .externalSymbol("unused", ScalarType.NUMBER, BigDecimal.TEN, ExternalSymbolOverwritePolicy.FIXED)
                .build();

        SemanticResolutionResult result = new SemanticResolver().resolve(ast("used"), environment);

        assertThat(result).isInstanceOfSatisfying(SemanticResolutionSuccess.class, success -> {
            FrameLayout frameLayout = success.model().frameLayout();
            assertThat(frameLayout.externalBindings()).extracting(SymbolBinding::name).containsExactly("used");
            assertThat(frameLayout.frameSize()).isEqualTo(1);
        });
    }

    @Test
    void addingAnUnusedExternalSymbolDoesNotChangeFrameSizeOrExistingSlots() {
        ExpressionEnvironment baseline = ExpressionEnvironment.builder()
                .externalSymbol("used", ScalarType.NUMBER, BigDecimal.ONE, ExternalSymbolOverwritePolicy.FIXED)
                .build();
        ExpressionEnvironment withExtraDeclaration = ExpressionEnvironment.builder()
                .externalSymbol("used", ScalarType.NUMBER, BigDecimal.ONE, ExternalSymbolOverwritePolicy.FIXED)
                .externalSymbol("unused", ScalarType.NUMBER, BigDecimal.TEN, ExternalSymbolOverwritePolicy.FIXED)
                .build();

        FrameLayout baselineLayout = frameLayoutOf(ast("used"), baseline);
        FrameLayout extraLayout = frameLayoutOf(ast("used"), withExtraDeclaration);

        assertThat(extraLayout.frameSize()).isEqualTo(baselineLayout.frameSize());
        assertThat(extraLayout.externalBindings()).extracting(SymbolBinding::frameSlot)
                .isEqualTo(baselineLayout.externalBindings().stream().map(SymbolBinding::frameSlot).toList());
    }

    @Test
    void usedExternalsAreOrderedByFirstSourceReferenceNotDeclarationOrder() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("zeta", ScalarType.NUMBER, BigDecimal.ONE, ExternalSymbolOverwritePolicy.FIXED)
                .externalSymbol("alpha", ScalarType.NUMBER, BigDecimal.TEN, ExternalSymbolOverwritePolicy.FIXED)
                .build();

        SemanticResolutionResult result = new SemanticResolver().resolve(ast("alpha + zeta"), environment);

        assertThat(result).isInstanceOfSatisfying(SemanticResolutionSuccess.class, success -> {
            assertThat(success.model().frameLayout().externalBindings())
                    .extracting(SymbolBinding::name)
                    .containsExactly("alpha", "zeta");
        });
    }

    @Test
    void changingEnvironmentDeclarationOrderDoesNotChangeSlots() {
        ExpressionEnvironment declaredZetaFirst = ExpressionEnvironment.builder()
                .externalSymbol("zeta", ScalarType.NUMBER, BigDecimal.ONE, ExternalSymbolOverwritePolicy.FIXED)
                .externalSymbol("alpha", ScalarType.NUMBER, BigDecimal.TEN, ExternalSymbolOverwritePolicy.FIXED)
                .build();
        ExpressionEnvironment declaredAlphaFirst = ExpressionEnvironment.builder()
                .externalSymbol("alpha", ScalarType.NUMBER, BigDecimal.TEN, ExternalSymbolOverwritePolicy.FIXED)
                .externalSymbol("zeta", ScalarType.NUMBER, BigDecimal.ONE, ExternalSymbolOverwritePolicy.FIXED)
                .build();

        FrameLayout first = frameLayoutOf(ast("alpha + zeta"), declaredZetaFirst);
        FrameLayout second = frameLayoutOf(ast("alpha + zeta"), declaredAlphaFirst);

        assertThat(first.externalBindings()).extracting(SymbolBinding::name)
                .isEqualTo(second.externalBindings().stream().map(SymbolBinding::name).toList());
        assertThat(first.externalBindings()).extracting(SymbolBinding::frameSlot)
                .isEqualTo(second.externalBindings().stream().map(SymbolBinding::frameSlot).toList());
    }

    @Test
    void assignmentOnlyFileResolvesSuccessfullyWithoutAResultExpression() {
        SemanticResolutionResult result = new SemanticResolver().resolve(ast("x := 1;"), ExpressionEnvironment.standard());

        assertThat(result).isInstanceOfSatisfying(SemanticResolutionSuccess.class, success -> {
            assertThat(success.model().ast().resultExpression()).isEmpty();
            assertThat(success.model().frameLayout().frameSize()).isEqualTo(1);
        });
    }

    @Test
    void reassignmentReusesTheSameInternalSlot() {
        SemanticResolutionResult result = new SemanticResolver().resolve(
                ast("x := 1; x := x + 1;"), ExpressionEnvironment.standard());

        assertThat(result).isInstanceOfSatisfying(SemanticResolutionSuccess.class, success -> {
            assertThat(success.model().symbolBindings().values())
                    .extracting(SymbolBinding::frameSlot)
                    .containsOnly(0);
        });
    }

    @Test
    void destructuringTargetsReceiveIndividualSlotsInTextualOrder() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("values", new CollectionType(ScalarType.NUMBER), List.of(BigDecimal.ONE),
                        ExternalSymbolOverwritePolicy.FIXED)
                .build();

        SemanticResolutionResult result = new SemanticResolver().resolve(
                ast("[first, second] := values;"), environment);

        assertThat(result).isInstanceOfSatisfying(SemanticResolutionSuccess.class, success -> {
            List<SymbolBinding> bindings = success.model().symbolBindings().values().stream()
                    .filter(binding -> !binding.external())
                    .sorted((a, b) -> Integer.compare(a.frameSlot(), b.frameSlot()))
                    .toList();
            assertThat(bindings).extracting(SymbolBinding::name).containsExactly("first", "second");
            assertThat(bindings).extracting(SymbolBinding::frameSlot).containsExactly(1, 2);
        });
    }

    @Test
    void completelyEmptyFileFailsWithTheStablePositionedEmptyFileDiagnostic() {
        SemanticResolutionResult result = new SemanticResolver().resolve(ast(""), ExpressionEnvironment.standard());

        assertThat(result).isInstanceOfSatisfying(SemanticResolutionFailure.class, failure -> {
            assertThat(failure.diagnostics()).singleElement().satisfies(diagnostic -> {
                assertThat(diagnostic.code()).isEqualTo(DiagnosticCode.SEMANTIC_EMPTY_EXPRESSION.name());
                assertThat(diagnostic.primarySpan().orElseThrow().offset()).isZero();
                assertThat(diagnostic.primarySpan().orElseThrow().endOffset()).isZero();
                assertThat(diagnostic.primarySpan().orElseThrow().line()).isEqualTo(1);
                assertThat(diagnostic.primarySpan().orElseThrow().column()).isEqualTo(1);
            });
        });
    }

    private static FrameLayout frameLayoutOf(ExpressionFileNode ast, ExpressionEnvironment environment) {
        SemanticResolutionResult result = new SemanticResolver().resolve(ast, environment);
        return ((SemanticResolutionSuccess) result).model().frameLayout();
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
        ExpressionFileNode ast = ast("items?.sum() ?? 0");
        NavigationChainNode navigation = navigationChain(ast.resultExpression().orElseThrow());
        NavigationLink link = navigation.links().getFirst();

        SemanticResolutionResult result = new SemanticResolver().resolve(ast, environment);

        assertThat(result).isInstanceOfSatisfying(SemanticResolutionSuccess.class, success -> {
            SemanticModel model = success.model();
            assertThat(model.navigationBindings()).containsKey(link.id());
            CollectionOperationBinding binding = (CollectionOperationBinding) model.navigationBindings().get(link.id());
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
    void dischargesSafeOperationNullabilityThroughGroupingAndAssignmentViaCoalesce() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol(
                        "items",
                        new CollectionType(ScalarType.NUMBER),
                        List.of(BigDecimal.ONE),
                        ExternalSymbolOverwritePolicy.FIXED)
                .build();

        assertResultNullability("(items?.sum() ?? 0)", environment, RuntimeNullability.NEVER_NULL);
        assertResultNullability("value := items?.sum() ?? 0; value", environment, RuntimeNullability.NEVER_NULL);
    }

    @Test
    void rejectsNullableFinalResultAndAssignmentWithoutDischarge() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol(
                        "items",
                        new CollectionType(ScalarType.NUMBER),
                        List.of(BigDecimal.ONE),
                        ExternalSymbolOverwritePolicy.FIXED)
                .build();

        assertSingleDiagnostic(
                "(items?.sum())", environment, DiagnosticCode.SEMANTIC_NULLABLE_RESULT_NOT_ALLOWED);
        assertSingleDiagnostic(
                "value := items?.sum(); value", environment, DiagnosticCode.SEMANTIC_NULLABLE_ASSIGNMENT_NOT_ALLOWED);
    }

    @Test
    void rejectsNullableOperandWithoutDischarge() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("m", new MapType(ScalarType.NUMBER), Map.of("a", BigDecimal.ONE),
                        ExternalSymbolOverwritePolicy.FIXED)
                .build();

        assertSingleDiagnostic(
                "(m?.[\"a\"] + 1) ?? 0", environment, DiagnosticCode.SEMANTIC_NULLABLE_OPERAND_NOT_ALLOWED);
    }

    @Test
    void rejectsNullableCollectionOperationArgumentWithoutDischarge() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol(
                        "items",
                        new CollectionType(ScalarType.NUMBER),
                        List.of(BigDecimal.ONE),
                        ExternalSymbolOverwritePolicy.FIXED)
                .externalSymbol("m", new MapType(ScalarType.NUMBER), Map.of("a", BigDecimal.ONE),
                        ExternalSymbolOverwritePolicy.FIXED)
                .build();

        assertSingleDiagnostic(
                "(items.reduce(m?.[\"a\"], @ -> @.accumulator + @.item)) ?? 0",
                environment,
                DiagnosticCode.SEMANTIC_NULLABLE_ARGUMENT_NOT_ALLOWED);
    }

    @Test
    void rejectsNullableReceiverOfNonSafeNavigationWithoutDischarge() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("m", new MapType(new CollectionType(ScalarType.NUMBER)),
                        Map.of("a", List.of(BigDecimal.ONE)), ExternalSymbolOverwritePolicy.FIXED)
                .build();

        assertSingleDiagnostic(
                "(m?.[\"a\"][0]) ?? 0", environment, DiagnosticCode.SEMANTIC_NULLABLE_RECEIVER_NOT_ALLOWED);
    }

    @Test
    void rejectsNullableFilterPredicateWithoutDischarge() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol(
                        "items",
                        new CollectionType(ScalarType.NUMBER),
                        List.of(BigDecimal.ONE),
                        ExternalSymbolOverwritePolicy.FIXED)
                .externalSymbol("m", new MapType(ScalarType.BOOLEAN), Map.of("flag", Boolean.TRUE),
                        ExternalSymbolOverwritePolicy.FIXED)
                .build();

        assertSingleDiagnostic(
                "items[?(m?.[\"flag\"])]", environment, DiagnosticCode.SEMANTIC_NULLABLE_PREDICATE_NOT_ALLOWED);
    }

    @Test
    void rejectsNullableCollectionLiteralElementWithoutDischarge() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("m", new MapType(ScalarType.NUMBER), Map.of("a", BigDecimal.ONE),
                        ExternalSymbolOverwritePolicy.FIXED)
                .build();

        assertSingleDiagnostic(
                "[m?.[\"a\"]]", environment, DiagnosticCode.SEMANTIC_NULLABLE_OPERAND_NOT_ALLOWED);
    }

    @Test
    void propagatesNullabilityAcrossNestedSafeNavigationLinks() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("m", new MapType(new MapType(ScalarType.NUMBER)),
                        Map.of("a", Map.of("b", BigDecimal.ONE)), ExternalSymbolOverwritePolicy.FIXED)
                .build();
        ExpressionFileNode ast = ast("m?.[\"a\"]?.[\"b\"] ?? 0");
        NavigationChainNode navigation = navigationChain(ast.resultExpression().orElseThrow());
        NavigationLink firstLink = navigation.links().get(0);
        NavigationLink secondLink = navigation.links().get(1);

        SemanticResolutionResult result = new SemanticResolver().resolve(ast, environment);

        assertThat(result).isInstanceOfSatisfying(SemanticResolutionSuccess.class, success -> {
            SemanticModel model = success.model();
            assertThat(model.runtimeNullability()).containsEntry(firstLink.id(), RuntimeNullability.MAY_BE_NULL);
            assertThat(model.runtimeNullability()).containsEntry(secondLink.id(), RuntimeNullability.MAY_BE_NULL);
            assertThat(model.runtimeNullability())
                    .containsEntry(ast.resultExpression().orElseThrow().id(), RuntimeNullability.NEVER_NULL);
        });
    }

    @Test
    void nullCoalesceIsMayBeNullOnlyWhenEveryOperandMayBeNull() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("m", new MapType(ScalarType.NUMBER), Map.of("a", BigDecimal.ONE, "b", BigDecimal.TEN),
                        ExternalSymbolOverwritePolicy.FIXED)
                .build();
        ExpressionFileNode ast = ast("(m?.[\"a\"] ?? m?.[\"b\"]) ?? 0");
        NullCoalesceNode innerCoalesce = (NullCoalesceNode) ((GroupedExpressionNode)
                ((NullCoalesceNode) ast.resultExpression().orElseThrow()).operands().getFirst()).expression();

        SemanticResolutionResult result = new SemanticResolver().resolve(ast, environment);

        assertThat(result).isInstanceOfSatisfying(SemanticResolutionSuccess.class, success -> {
            SemanticModel model = success.model();
            assertThat(model.runtimeNullability()).containsEntry(innerCoalesce.id(), RuntimeNullability.MAY_BE_NULL);
            assertThat(model.runtimeNullability())
                    .containsEntry(ast.resultExpression().orElseThrow().id(), RuntimeNullability.NEVER_NULL);
        });
    }

    @Test
    void conditionalIsMayBeNullWhenAnyBranchMayBeNull() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("m", new MapType(ScalarType.NUMBER), Map.of("a", BigDecimal.ONE),
                        ExternalSymbolOverwritePolicy.FIXED)
                .build();
        ExpressionFileNode ast = ast("(if true then m?.[\"a\"] else 0 endif) ?? 0");
        ConditionalNode conditional = (ConditionalNode) ((GroupedExpressionNode)
                ((NullCoalesceNode) ast.resultExpression().orElseThrow()).operands().getFirst()).expression();

        SemanticResolutionResult result = new SemanticResolver().resolve(ast, environment);

        assertThat(result).isInstanceOfSatisfying(SemanticResolutionSuccess.class, success -> {
            SemanticModel model = success.model();
            assertThat(model.runtimeNullability()).containsEntry(conditional.id(), RuntimeNullability.MAY_BE_NULL);
            assertThat(model.runtimeNullability())
                    .containsEntry(ast.resultExpression().orElseThrow().id(), RuntimeNullability.NEVER_NULL);
        });
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
            CollectionOperationBinding binding = (CollectionOperationBinding) success.model().navigationBindings().get(link.id());
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
        ExpressionFileNode ast = ast("object?.[*] ?? []");
        NavigationChainNode navigation = navigationChain(ast.resultExpression().orElseThrow());
        NavigationLink link = navigation.links().getFirst();

        SemanticResolutionResult result = new SemanticResolver().resolve(ast, environment);

        assertThat(result).isInstanceOfSatisfying(SemanticResolutionSuccess.class, success -> {
            SemanticModel model = success.model();
            assertThat(model.resolvedTypes()).containsEntry(link.id(), new CollectionType(ScalarType.NUMBER));
            assertThat(model.runtimeNullability()).containsEntry(link.id(), RuntimeNullability.MAY_BE_NULL);
            assertThat(model.collectionShapes()).containsEntry(link.id(), new CollectionShape(2));
            assertThat(model.navigationBindings()).containsKey(link.id());
            WildcardNavigationBinding binding = (WildcardNavigationBinding) model.navigationBindings().get(link.id());
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
                "m?.[*] ?? []",
                environment,
                WildcardNavigationBinding.ReceiverKind.MAP,
                ScalarType.STRING,
                RuntimeNullability.MAY_BE_NULL,
                CollectionShape.unknown());
    }

    @Test
    void bindsStringKeyMapSubscriptTypeAndNullability() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("m", new MapType(ScalarType.NUMBER), Map.of("A", BigDecimal.ONE),
                        ExternalSymbolOverwritePolicy.FIXED)
                .build();

        assertStringKeySubscript("m[\"A\"]", environment, RuntimeNullability.NEVER_NULL);
        assertStringKeySubscript("m?.[\"A\"] ?? 0", environment, RuntimeNullability.MAY_BE_NULL);
    }

    @Test
    void recordsUnknownCollectionShapeForStringKeyMapSubscriptCollectionValues() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("m", new MapType(new CollectionType(ScalarType.NUMBER)),
                        Map.of("A", List.of(BigDecimal.ONE)), ExternalSymbolOverwritePolicy.FIXED)
                .build();
        ExpressionFileNode ast = ast("m[\"A\"]");
        NavigationChainNode navigation = (NavigationChainNode) ast.resultExpression().orElseThrow();
        NavigationLink link = navigation.links().getFirst();

        SemanticResolutionResult result = new SemanticResolver().resolve(ast, environment);

        assertThat(result).isInstanceOfSatisfying(SemanticResolutionSuccess.class, success ->
                assertThat(success.model().collectionShapes()).containsEntry(link.id(), CollectionShape.unknown()));
    }

    @Test
    void rejectsStringKeySubscriptOnNonMapReceivers() {
        assertSemanticDiagnostic("items := [1]; items[\"A\"]", ExpressionEnvironment.standard());
        ExpressionEnvironment scalarEnvironment = ExpressionEnvironment.builder()
                .externalSymbol("n", ScalarType.NUMBER, BigDecimal.ONE, ExternalSymbolOverwritePolicy.FIXED)
                .build();
        assertSemanticDiagnostic("n[\"A\"]", scalarEnvironment);

        ExpressionEnvironment objectEnvironment = ExpressionEnvironment.builder()
                .registerJavaType(WildcardChildProvider.class)
                .externalSymbol("object", new WildcardChildProvider(), ExternalSymbolOverwritePolicy.FIXED)
                .build();
        assertSemanticDiagnostic("object[\"A\"]", objectEnvironment);
    }

    @Test
    void bindsRegisteredObjectPropertyNavigationWithAccessorHandleAndNullability() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .registerJavaType(PropertyProvider.class)
                .externalSymbol("object", new PropertyProvider(BigDecimal.TEN), ExternalSymbolOverwritePolicy.FIXED)
                .build();
        ExpressionFileNode ast = ast("object.amount");
        NavigationChainNode navigation = (NavigationChainNode) ast.resultExpression().orElseThrow();
        NavigationLink link = navigation.links().getFirst();

        SemanticResolutionResult result = new SemanticResolver().resolve(ast, environment);

        assertThat(result).isInstanceOfSatisfying(SemanticResolutionSuccess.class, success -> {
            SemanticModel model = success.model();
            RegisteredPropertyNavigationBinding binding =
                    (RegisteredPropertyNavigationBinding) model.navigationBindings().get(link.id());
            assertThat(binding.resultType()).isEqualTo(ScalarType.NUMBER);
            assertThat(binding.resultNullability()).isEqualTo(RuntimeNullability.NEVER_NULL);
            assertThat(binding.accessorHandle()).isNotNull();
            assertThat(binding.pure()).isTrue();
            assertThat(model.resolvedTypes()).containsEntry(link.id(), ScalarType.NUMBER);
        });
    }

    @Test
    void rejectsUnregisteredObjectPropertyMember() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .registerJavaType(PropertyProvider.class)
                .externalSymbol("object", new PropertyProvider(BigDecimal.TEN), ExternalSymbolOverwritePolicy.FIXED)
                .build();

        SemanticResolutionResult result = new SemanticResolver().resolve(ast("object.missing"), environment);

        assertThat(result).isInstanceOfSatisfying(SemanticResolutionFailure.class, failure ->
                assertThat(failure.diagnostics()).singleElement().satisfies(diagnostic ->
                        assertThat(diagnostic.code()).isEqualTo(DiagnosticCode.SEMANTIC_UNKNOWN_MEMBER.name())));
    }

    @Test
    void bindsRegisteredObjectMethodNavigationWithInvocationHandleAndNullability() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .registerJavaTypeMethod(PropertyProvider.class, "scaledAmount", BigDecimal.class)
                .externalSymbol("object", new PropertyProvider(BigDecimal.TEN), ExternalSymbolOverwritePolicy.FIXED)
                .build();
        ExpressionFileNode ast = ast("object.scaledAmount(2)");
        NavigationChainNode navigation = (NavigationChainNode) ast.resultExpression().orElseThrow();
        NavigationLink link = navigation.links().getFirst();

        SemanticResolutionResult result = new SemanticResolver().resolve(ast, environment);

        assertThat(result).isInstanceOfSatisfying(SemanticResolutionSuccess.class, success -> {
            SemanticModel model = success.model();
            RegisteredMethodNavigationBinding binding =
                    (RegisteredMethodNavigationBinding) model.navigationBindings().get(link.id());
            assertThat(binding.resultType()).isEqualTo(ScalarType.NUMBER);
            assertThat(binding.resultNullability()).isEqualTo(RuntimeNullability.NEVER_NULL);
            assertThat(binding.invocationHandle()).isNotNull();
            assertThat(binding.pure()).isFalse();
            assertThat(model.resolvedTypes()).containsEntry(link.id(), ScalarType.NUMBER);
        });
    }

    @Test
    void propagatesDeclaredRegisteredMethodPurityToTheNavigationBinding() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .registerJavaTypeMethod(PropertyProvider.class, "scaledAmount", FunctionPurity.PURE, BigDecimal.class)
                .externalSymbol("object", new PropertyProvider(BigDecimal.TEN), ExternalSymbolOverwritePolicy.FIXED)
                .build();
        ExpressionFileNode ast = ast("object.scaledAmount(2)");
        NavigationChainNode navigation = (NavigationChainNode) ast.resultExpression().orElseThrow();
        NavigationLink link = navigation.links().getFirst();

        SemanticResolutionResult result = new SemanticResolver().resolve(ast, environment);

        assertThat(result).isInstanceOfSatisfying(SemanticResolutionSuccess.class, success -> {
            SemanticModel model = success.model();
            RegisteredMethodNavigationBinding binding =
                    (RegisteredMethodNavigationBinding) model.navigationBindings().get(link.id());
            assertThat(binding.pure()).isTrue();
        });
    }

    @Test
    void rejectsUnregisteredObjectMethodMember() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .registerJavaTypeMethod(PropertyProvider.class, "scaledAmount", BigDecimal.class)
                .externalSymbol("object", new PropertyProvider(BigDecimal.TEN), ExternalSymbolOverwritePolicy.FIXED)
                .build();

        SemanticResolutionResult result = new SemanticResolver().resolve(ast("object.missing()"), environment);

        assertThat(result).isInstanceOfSatisfying(SemanticResolutionFailure.class, failure ->
                assertThat(failure.diagnostics()).singleElement().satisfies(diagnostic ->
                        assertThat(diagnostic.code()).isEqualTo(DiagnosticCode.SEMANTIC_UNKNOWN_MEMBER.name())));
    }

    @Test
    void rejectsMethodCallOnMapEntryWithoutLeakingItsInternalClassName() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("m", new MapType(ScalarType.NUMBER), Map.of("a", BigDecimal.ONE),
                        ExternalSymbolOverwritePolicy.FIXED)
                .build();

        SemanticResolutionResult result = new SemanticResolver().resolve(ast("m.map(@ -> @.badMethod())"), environment);

        assertThat(result).isInstanceOfSatisfying(SemanticResolutionFailure.class, failure ->
                assertThat(failure.diagnostics()).singleElement().satisfies(diagnostic -> {
                    assertThat(diagnostic.code()).isEqualTo(DiagnosticCode.SEMANTIC_UNKNOWN_MEMBER.name());
                    assertThat(diagnostic.message()).doesNotContain("com.runestone").contains("@.k").contains("@.v");
                }));
    }

    @Test
    void rejectsMethodCallOnReductionItemWithoutLeakingItsInternalClassName() {
        SemanticResolutionResult result = new SemanticResolver()
                .resolve(ast("items := [1]; items.reduce(0, @ -> @.badMethod())"), ExpressionEnvironment.standard());

        assertThat(result).isInstanceOfSatisfying(SemanticResolutionFailure.class, failure ->
                assertThat(failure.diagnostics()).singleElement().satisfies(diagnostic -> {
                    assertThat(diagnostic.code()).isEqualTo(DiagnosticCode.SEMANTIC_UNKNOWN_MEMBER.name());
                    assertThat(diagnostic.message())
                            .doesNotContain("com.runestone")
                            .contains("@.accumulator")
                            .contains("@.item");
                }));
    }

    @Test
    void rejectsLambdaArgumentForRegisteredObjectMethod() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .registerJavaTypeMethod(PropertyProvider.class, "scaledAmount", BigDecimal.class)
                .externalSymbol("object", new PropertyProvider(BigDecimal.TEN), ExternalSymbolOverwritePolicy.FIXED)
                .build();

        SemanticResolutionResult result = new SemanticResolver()
                .resolve(ast("object.scaledAmount(@ -> @)"), environment);

        assertThat(result).isInstanceOfSatisfying(SemanticResolutionFailure.class, failure ->
                assertThat(failure.diagnostics()).singleElement().satisfies(diagnostic ->
                        assertThat(diagnostic.code())
                                .isEqualTo(DiagnosticCode.SEMANTIC_LAMBDA_ARGUMENT_UNSUPPORTED.name())));
    }

    @Test
    void rejectsPropertyNavigationOnMapReceiver() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("m", new MapType(ScalarType.NUMBER), Map.of("a", BigDecimal.ONE),
                        ExternalSymbolOverwritePolicy.FIXED)
                .build();

        assertSemanticDiagnostic("m.a", environment);
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
            assertThat(((CollectionOperationBinding) model.navigationBindings().get(link.id())).identity())
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
        NavigationChainNode navigation = navigationChain(ast.resultExpression().orElseThrow());
        NavigationLink link = navigation.links().getFirst();

        SemanticResolutionResult result = new SemanticResolver().resolve(ast, environment);

        assertThat(result).isInstanceOfSatisfying(SemanticResolutionSuccess.class, success -> {
            WildcardNavigationBinding binding = (WildcardNavigationBinding) success.model().navigationBindings().get(link.id());
            assertThat(binding.receiverKind()).isEqualTo(receiverKind);
            assertThat(binding.elementType()).isEqualTo(elementType);
            assertThat(binding.resultNullability()).isEqualTo(nullability);
            assertThat(binding.resultShape()).isEqualTo(shape);
            assertThat(success.model().runtimeNullability()).containsEntry(link.id(), nullability);
        });
    }

    private static void assertStringKeySubscript(
            String source,
            ExpressionEnvironment environment,
            RuntimeNullability expectedNullability) {
        ExpressionFileNode ast = ast(source);
        NavigationChainNode navigation = navigationChain(ast.resultExpression().orElseThrow());
        NavigationLink link = navigation.links().getFirst();

        SemanticResolutionResult result = new SemanticResolver().resolve(ast, environment);

        assertThat(result).isInstanceOfSatisfying(SemanticResolutionSuccess.class, success -> {
            SemanticModel model = success.model();
            assertThat(model.resolvedTypes()).containsEntry(link.id(), ScalarType.NUMBER);
            assertThat(model.runtimeNullability()).containsEntry(link.id(), expectedNullability);
            assertThat(model.collectionShapes()).doesNotContainKey(link.id());
            assertThat(model.runtimeNullability()).containsEntry(navigation.id(), expectedNullability);
        });
    }

    private static NavigationChainNode navigationChain(ExpressionNode expression) {
        if (expression instanceof NavigationChainNode navigation) {
            return navigation;
        }
        if (expression instanceof NullCoalesceNode coalesce
                && coalesce.operands().getFirst() instanceof NavigationChainNode navigation) {
            return navigation;
        }
        throw new IllegalArgumentException("expected a navigation chain expression: " + expression);
    }

    private static void assertSingleDiagnostic(
            String source, ExpressionEnvironment environment, DiagnosticCode expectedCode) {
        SemanticResolutionResult result = new SemanticResolver().resolve(ast(source), environment);

        assertThat(result).isInstanceOfSatisfying(SemanticResolutionFailure.class, failure ->
                assertThat(failure.diagnostics()).singleElement().satisfies(diagnostic -> {
                    assertThat(diagnostic.code()).isEqualTo(expectedCode.name());
                    assertThat(diagnostic.severity()).isEqualTo(DiagnosticSeverity.ERROR);
                    assertThat(diagnostic.primarySpan()).isPresent();
                    assertThat(diagnostic.suggestion()).isPresent();
                }));
    }

    private static void assertSemanticDiagnostic(String source, ExpressionEnvironment environment) {
        SemanticResolutionResult result = new SemanticResolver().resolve(ast(source), environment);

        assertThat(result).isInstanceOfSatisfying(SemanticResolutionFailure.class, failure ->
                assertThat(failure.diagnostics()).singleElement().satisfies(diagnostic ->
                        assertThat(diagnostic.code())
                                .isEqualTo(DiagnosticCode.SEMANTIC_NAVIGATION_RECEIVER_NOT_SUPPORTED.name())));
    }

    public static final class ImpureFunctions {

        public BigDecimal touch(BigDecimal value) {
            return value;
        }
    }

    public record PropertyProvider(BigDecimal amount) {

        public BigDecimal scaledAmount(BigDecimal factor) {
            return amount.multiply(factor);
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
