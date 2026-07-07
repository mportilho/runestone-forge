package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.api.ExpressionEnvironment;
import com.runestone.expeval_mk3.api.ScalarType;
import com.runestone.expeval_mk3.api.UnknownType;
import com.runestone.expeval_mk3.internal.diagnostics.DiagnosticCategory;
import com.runestone.expeval_mk3.internal.diagnostics.DiagnosticCode;
import com.runestone.expeval_mk3.internal.diagnostics.DiagnosticSeverity;
import com.runestone.expeval_mk3.internal.diagnostics.ExpressionDiagnostic;
import com.runestone.expeval_mk3.internal.semantics.FrameSlotKind;
import com.runestone.expeval_mk3.internal.semantics.ResolvedSymbol;
import com.runestone.expeval_mk3.internal.semantics.ResolvedSymbolKind;
import com.runestone.expeval_mk3.internal.semantics.SemanticModel;
import com.runestone.expeval_mk3.internal.semantics.SemanticResolver;
import com.runestone.expeval_mk3.internal.semantics.SemanticResolutionResult;
import com.runestone.expeval_mk3.internal.source.SourceSpan;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class SemanticResolverTest {

    private final SemanticResolver resolver = new SemanticResolver();

    @Test
    @DisplayName("non-empty expression files resolve to a planejable semantic model without diagnostics")
    void nonEmptyExpressionFilesResolveToSemanticModelWithoutDiagnostics() {
        ExpressionFileNode ast = AstTestSupport.build("1");

        SemanticResolutionResult result = resolver.resolve(ast, ExpressionEnvironment.standard());

        assertThat(result.hasErrors()).isFalse();
        assertThat(result.diagnostics()).isEmpty();
        assertThat(result.model())
                .get()
                .satisfies(model -> {
                    assertThat(model.sourceTree()).isSameAs(ast);
                    assertThat(model.diagnostics()).isEmpty();
                });
    }

    @Test
    @DisplayName("semantic warnings coexist with a planejable semantic model")
    void semanticWarningsCoexistWithSemanticModel() {
        ExpressionFileNode ast = AstTestSupport.build("amount := 1; amount");
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("amount", ScalarType.NUMBER)
                .build();

        SemanticResolutionResult result = resolver.resolve(ast, environment);

        assertThat(result.hasErrors()).isFalse();
        assertThat(result.model()).isPresent();
        assertThat(result.diagnostics())
                .singleElement()
                .satisfies(diagnostic -> {
                    assertThat(diagnostic.category()).isEqualTo(DiagnosticCategory.SEMANTIC);
                    assertThat(diagnostic.code()).isEqualTo(DiagnosticCode.SEMANTIC_EXTERNAL_SYMBOL_SHADOWED);
                    assertThat(diagnostic.severity()).isEqualTo(DiagnosticSeverity.WARNING);
                    assertThat(diagnostic.span()).isEqualTo(new SourceSpan(0, 6, 1, 1));
                });
        assertThat(result.model())
                .get()
                .extracting(model -> model.diagnostics())
                .isEqualTo(result.diagnostics());
    }

    @Test
    @DisplayName("external symbols resolve by declared catalog name and preserve type information")
    void externalSymbolsResolveByNameAndPreserveTypeInformation() {
        ExpressionFileNode ast = AstTestSupport.build("[enabled, amount, threshold, lateBound]");
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("enabled", ScalarType.BOOLEAN)
                .externalSymbol("amount", ScalarType.NUMBER)
                .externalSymbolWithDefault("threshold", new BigDecimal("12.50"))
                .externalSymbol("lateBound")
                .build();

        SemanticModel model = resolveModel(ast, environment);

        ResolvedSymbol amount = model.externalSymbols().get(0);
        ResolvedSymbol enabled = model.externalSymbols().get(1);
        ResolvedSymbol lateBound = model.externalSymbols().get(2);
        ResolvedSymbol threshold = model.externalSymbols().get(3);
        assertThat(amount.name()).isEqualTo("amount");
        assertThat(amount.type()).isEqualTo(ScalarType.NUMBER);
        assertThat(enabled.name()).isEqualTo("enabled");
        assertThat(enabled.type()).isEqualTo(ScalarType.BOOLEAN);
        assertThat(lateBound.name()).isEqualTo("lateBound");
        assertThat(lateBound.type()).isEqualTo(UnknownType.INSTANCE);
        assertThat(threshold.name()).isEqualTo("threshold");
        assertThat(threshold.type()).isEqualTo(ScalarType.NUMBER);
        assertThat(model.symbolByNodeId())
                .containsEntry(identifier(ast, "amount", 0).id(), amount)
                .containsEntry(identifier(ast, "enabled", 0).id(), enabled)
                .containsEntry(identifier(ast, "lateBound", 0).id(), lateBound)
                .containsEntry(identifier(ast, "threshold", 0).id(), threshold);
    }

    @Test
    @DisplayName("assignment right-hand sides resolve before a new target shadows an external symbol")
    void assignmentRightHandSideResolvesBeforeNewTargetShadowsExternalSymbol() {
        ExpressionFileNode ast = AstTestSupport.build("amount := amount; amount");
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("amount", ScalarType.NUMBER)
                .build();

        SemanticResolutionResult result = resolver.resolve(ast, environment);

        assertThat(result.hasErrors()).isFalse();
        SemanticModel model = result.model().orElseThrow();
        ResolvedSymbol externalAmount = model.externalSymbols().getFirst();
        ResolvedSymbol internalAmount = model.internalSymbols().getFirst();
        assertThat(model.symbolByNodeId())
                .containsEntry(identifier(ast, "amount", 0).id(), externalAmount)
                .containsEntry(identifier(ast, "amount", 1).id(), internalAmount);
        assertThat(internalAmount.slot()).isZero();
        assertThat(result.diagnostics())
                .singleElement()
                .satisfies(diagnostic -> {
                    assertThat(diagnostic.code()).isEqualTo(DiagnosticCode.SEMANTIC_EXTERNAL_SYMBOL_SHADOWED);
                    assertThat(diagnostic.severity()).isEqualTo(DiagnosticSeverity.WARNING);
                });
    }

    @Test
    @DisplayName("reassignment reads the previous internal symbol and keeps one stable slot")
    void reassignmentReadsPreviousInternalSymbolAndKeepsOneStableSlot() {
        ExpressionFileNode ast = AstTestSupport.build("total := 1; total := total; total");

        SemanticModel model = resolveModel(ast, ExpressionEnvironment.standard());

        ResolvedSymbol total = model.internalSymbols().getFirst();
        assertThat(total.name()).isEqualTo("total");
        assertThat(total.kind()).isEqualTo(ResolvedSymbolKind.INTERNAL);
        assertThat(total.slot()).isZero();
        assertThat(total.type()).isEqualTo(ScalarType.NUMBER);
        assertThat(targets(ast, "total"))
                .extracting(target -> model.symbolByNodeId().get(target.id()))
                .containsExactly(total, total);
        assertThat(model.symbolByNodeId())
                .containsEntry(identifier(ast, "total", 0).id(), total)
                .containsEntry(identifier(ast, "total", 1).id(), total);
    }

    @Test
    @DisplayName("reassignment with an unknown external symbol makes the unified internal type unknown")
    void reassignmentWithUnknownExternalSymbolMakesUnifiedInternalTypeUnknown() {
        ExpressionFileNode ast = AstTestSupport.build("total := 1; total := lateBound; total");
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("lateBound")
                .build();

        SemanticModel model = resolveModel(ast, environment);

        ResolvedSymbol total = model.internalSymbols().getFirst();
        assertThat(total.slot()).isZero();
        assertThat(total.type()).isEqualTo(UnknownType.INSTANCE);
        assertThat(model.symbolByNodeId())
                .containsEntry(identifier(ast, "lateBound", 0).id(), model.externalSymbols().getFirst())
                .containsEntry(identifier(ast, "total", 0).id(), total);
    }

    @Test
    @DisplayName("reserved current temporal names cannot be direct assignment targets")
    void reservedCurrentTemporalNamesCannotBeDirectAssignmentTargets() {
        ExpressionFileNode ast = fileWithDirectAssignmentTarget("currDate", new SourceSpan(0, 8, 1, 1));

        SemanticResolutionResult result = resolver.resolve(ast, ExpressionEnvironment.standard());

        assertThat(result.hasErrors()).isTrue();
        assertThat(result.model()).isEmpty();
        assertThat(result.diagnostics())
                .singleElement()
                .satisfies(diagnostic -> {
                    assertThat(diagnostic.code()).isEqualTo(DiagnosticCode.SEMANTIC_RESERVED_SYMBOL_ASSIGNMENT);
                    assertThat(diagnostic.span()).isEqualTo(new SourceSpan(0, 8, 1, 1));
                });
    }

    @Test
    @DisplayName("reserved current temporal names cannot be destructuring assignment targets")
    void reservedCurrentTemporalNamesCannotBeDestructuringAssignmentTargets() {
        ExpressionFileNode ast = fileWithDestructuringAssignmentTarget();

        SemanticResolutionResult result = resolver.resolve(
                ast,
                ExpressionEnvironment.builder().externalSymbol("pair").build());

        assertThat(result.hasErrors()).isTrue();
        assertThat(result.model()).isEmpty();
        assertThat(result.diagnostics())
                .singleElement()
                .satisfies(diagnostic -> {
                    assertThat(diagnostic.code()).isEqualTo(DiagnosticCode.SEMANTIC_RESERVED_SYMBOL_ASSIGNMENT);
                    assertThat(diagnostic.span()).isEqualTo(new SourceSpan(8, 16, 1, 9));
                });
    }

    @Test
    @DisplayName("unresolved symbol reads produce semantic errors and no model")
    void unresolvedSymbolReadsProduceSemanticErrorsAndNoModel() {
        ExpressionFileNode ast = AstTestSupport.build("total := missing; total");

        SemanticResolutionResult result = resolver.resolve(ast, ExpressionEnvironment.standard());

        assertThat(result.hasErrors()).isTrue();
        assertThat(result.model()).isEmpty();
        assertThat(result.diagnostics())
                .singleElement()
                .satisfies(diagnostic -> {
                    assertThat(diagnostic.code()).isEqualTo(DiagnosticCode.SEMANTIC_UNRESOLVED_SYMBOL);
                    assertThat(diagnostic.span()).isEqualTo(new SourceSpan(9, 16, 1, 10));
                });
    }

    @Test
    @DisplayName("current item outside filter or lambda context produces a semantic error and no model")
    void currentItemOutsideContextProducesSemanticErrorAndNoModel() {
        ExpressionFileNode ast = AstTestSupport.build("@");

        SemanticResolutionResult result = resolver.resolve(ast, ExpressionEnvironment.standard());

        assertThat(result.hasErrors()).isTrue();
        assertThat(result.model()).isEmpty();
        assertThat(result.diagnostics())
                .singleElement()
                .satisfies(diagnostic -> {
                    assertThat(diagnostic.code()).isEqualTo(DiagnosticCode.SEMANTIC_CURRENT_ITEM_OUTSIDE_CONTEXT);
                    assertThat(diagnostic.span()).isEqualTo(new SourceSpan(0, 1, 1, 1));
                });
    }

    @Test
    @DisplayName("current item depth above the environment limit produces a semantic error and no model")
    void currentItemDepthAboveEnvironmentLimitProducesSemanticErrorAndNoModel() {
        ExpressionFileNode ast = AstTestSupport.build("items[?(@.children[?(@.active)])]");
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("items")
                .maxCurrentItemDepth(1)
                .build();

        SemanticResolutionResult result = resolver.resolve(ast, environment);

        assertThat(result.hasErrors()).isTrue();
        assertThat(result.model()).isEmpty();
        assertThat(result.diagnostics())
                .singleElement()
                .satisfies(diagnostic -> {
                    assertThat(diagnostic.code()).isEqualTo(DiagnosticCode.SEMANTIC_CURRENT_ITEM_DEPTH_EXCEEDED);
                    assertThat(diagnostic.span()).isEqualTo(ast.sourceSpan());
                });
    }

    @Test
    @DisplayName("frame layout orders internal symbols, canonical external symbols, then current item slots")
    void frameLayoutOrdersInternalSymbolsExternalSymbolsThenCurrentItemSlots() {
        ExpressionFileNode ast = AstTestSupport.build("beta := 1; alpha := 2; items[?(@.children[?(@.active)])]");
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("zeta", ScalarType.STRING)
                .externalSymbol("items")
                .externalSymbol("amount", ScalarType.NUMBER)
                .build();

        SemanticModel model = resolveModel(ast, environment);

        assertThat(model.frameLayout().slots())
                .extracting(slot -> slot.kind() + ":" + slot.name() + ":" + slot.index())
                .containsExactly(
                        FrameSlotKind.INTERNAL_SYMBOL + ":beta:0",
                        FrameSlotKind.INTERNAL_SYMBOL + ":alpha:1",
                        FrameSlotKind.EXTERNAL_SYMBOL + ":amount:2",
                        FrameSlotKind.EXTERNAL_SYMBOL + ":items:3",
                        FrameSlotKind.EXTERNAL_SYMBOL + ":zeta:4",
                        FrameSlotKind.CURRENT_ITEM + ":@1:5",
                        FrameSlotKind.CURRENT_ITEM + ":@2:6");
        assertThat(model.frameLayout().frameSize()).isEqualTo(7);
    }

    @Test
    @DisplayName("empty expression files resolve to a stable semantic error and no model")
    void emptyExpressionFilesResolveToStableSemanticErrorAndNoModel() {
        ExpressionFileNode ast = AstTestSupport.build("");

        SemanticResolutionResult result = resolver.resolve(ast, ExpressionEnvironment.standard());

        assertThat(result.hasErrors()).isTrue();
        assertThat(result.model()).isEmpty();
        assertThat(result.diagnostics())
                .singleElement()
                .satisfies(diagnostic -> assertThat(diagnostic)
                        .isEqualTo(ExpressionDiagnostic.error(
                                DiagnosticCategory.SEMANTIC,
                                DiagnosticCode.SEMANTIC_EMPTY_EXPRESSION_FILE,
                                "Expression file must contain at least one assignment or result expression",
                                new SourceSpan(0, 0, 1, 1))));
    }

    private SemanticModel resolveModel(ExpressionFileNode ast, ExpressionEnvironment environment) {
        SemanticResolutionResult result = resolver.resolve(ast, environment);
        assertThat(result.hasErrors()).isFalse();
        assertThat(result.diagnostics()).isEmpty();
        return result.model().orElseThrow();
    }

    private static IdentifierNode identifier(ExpressionFileNode ast, String name, int occurrence) {
        return AstTestSupport.flatten(ast).stream()
                .filter(IdentifierNode.class::isInstance)
                .map(IdentifierNode.class::cast)
                .filter(identifier -> identifier.name().equals(name))
                .skip(occurrence)
                .findFirst()
                .orElseThrow();
    }

    private static List<IdentifierAssignmentTargetNode> targets(ExpressionFileNode ast, String name) {
        return AstTestSupport.flatten(ast).stream()
                .filter(IdentifierAssignmentTargetNode.class::isInstance)
                .map(IdentifierAssignmentTargetNode.class::cast)
                .filter(target -> target.name().equals(name))
                .toList();
    }

    private static ExpressionFileNode fileWithDirectAssignmentTarget(String name, SourceSpan targetSpan) {
        return new ExpressionFileNode(
                new NodeId(0),
                new SourceSpan(0, 13, 1, 1),
                List.of(new AssignmentNode(
                        new NodeId(1),
                        new SourceSpan(0, 13, 1, 1),
                        new IdentifierAssignmentTargetNode(new NodeId(2), targetSpan, name),
                        new LiteralNode(new NodeId(3), new SourceSpan(12, 13, 1, 13), new LongLiteralValue(1L)))),
                Optional.empty());
    }

    private static ExpressionFileNode fileWithDestructuringAssignmentTarget() {
        return new ExpressionFileNode(
                new NodeId(0),
                new SourceSpan(0, 24, 1, 1),
                List.of(new AssignmentNode(
                        new NodeId(1),
                        new SourceSpan(0, 24, 1, 1),
                        new DestructuringAssignmentTargetNode(
                                new NodeId(2),
                                new SourceSpan(0, 17, 1, 1),
                                List.of(
                                        new IdentifierAssignmentTargetNode(
                                                new NodeId(3),
                                                new SourceSpan(1, 6, 1, 2),
                                                "first"),
                                        new IdentifierAssignmentTargetNode(
                                                new NodeId(4),
                                                new SourceSpan(8, 16, 1, 9),
                                                "currTime"))),
                        new IdentifierNode(new NodeId(5), new SourceSpan(21, 25, 1, 22), "pair"))),
                Optional.empty());
    }
}
