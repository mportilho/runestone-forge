package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.api.ExpressionEnvironment;
import com.runestone.expeval_mk3.api.ExpressionType;
import com.runestone.expeval_mk3.api.FunctionDescriptor;
import com.runestone.expeval_mk3.api.FunctionSignature;
import com.runestone.expeval_mk3.api.NullType;
import com.runestone.expeval_mk3.api.NumericMode;
import com.runestone.expeval_mk3.api.ScalarType;
import com.runestone.expeval_mk3.api.UnknownType;
import com.runestone.expeval_mk3.api.VectorType;
import com.runestone.expeval_mk3.internal.diagnostics.DiagnosticCategory;
import com.runestone.expeval_mk3.internal.diagnostics.DiagnosticCode;
import com.runestone.expeval_mk3.internal.diagnostics.DiagnosticSeverity;
import com.runestone.expeval_mk3.internal.diagnostics.ExpressionDiagnostic;
import com.runestone.expeval_mk3.internal.semantics.FrameSlotKind;
import com.runestone.expeval_mk3.internal.semantics.NumericKind;
import com.runestone.expeval_mk3.internal.semantics.PreparedOffsetDateTimeLiteralValue;
import com.runestone.expeval_mk3.internal.semantics.PreparedRegexPatternValue;
import com.runestone.expeval_mk3.internal.semantics.ResolvedFunctionBinding;
import com.runestone.expeval_mk3.internal.semantics.ResolvedSymbol;
import com.runestone.expeval_mk3.internal.semantics.ResolvedSymbolKind;
import com.runestone.expeval_mk3.internal.semantics.SemanticModel;
import com.runestone.expeval_mk3.internal.semantics.SemanticResolver;
import com.runestone.expeval_mk3.internal.semantics.SemanticResolutionResult;
import com.runestone.expeval_mk3.internal.source.SourceSpan;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Collections;
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
        ExpressionFileNode ast = AstTestSupport.build(
                "enabledCopy := enabled; amountCopy := amount; thresholdCopy := threshold; lateBound");
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
    @DisplayName("reassignment with an unknown external symbol keeps the concrete internal type")
    void reassignmentWithUnknownExternalSymbolKeepsConcreteInternalType() {
        ExpressionFileNode ast = AstTestSupport.build("total := 1; total := lateBound; total");
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("lateBound")
                .build();

        SemanticModel model = resolveModel(ast, environment);

        ResolvedSymbol total = model.internalSymbols().getFirst();
        assertThat(total.slot()).isZero();
        assertThat(total.type()).isEqualTo(ScalarType.NUMBER);
        assertThat(model.symbolByNodeId())
                .containsEntry(identifier(ast, "lateBound", 0).id(), model.externalSymbols().getFirst())
                .containsEntry(identifier(ast, "total", 0).id(), total);
    }

    @Test
    @DisplayName("concrete restrictions propagate through alias assignments")
    void concreteRestrictionsPropagateThroughAliasAssignments() {
        ExpressionFileNode ast = AstTestSupport.build("value := lateBound; value + 1");
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("lateBound")
                .strictMode(true)
                .build();

        SemanticModel model = resolveModel(ast, environment);

        assertThat(model.internalSymbols().getFirst().type()).isEqualTo(ScalarType.NUMBER);
        assertThat(model.externalSymbols().getFirst().type()).isEqualTo(ScalarType.NUMBER);
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
    @DisplayName("literal and vector nodes resolve to expression types")
    void literalAndVectorNodesResolveToExpressionTypes() {
        ExpressionFileNode ast = AstTestSupport.build("flag := true; text := \"text\"; empty := []; [1, null, 2]");

        SemanticModel model = resolveModel(ast, ExpressionEnvironment.standard());

        assertThat(model.resolvedTypes())
                .containsEntry(literal(ast, LongLiteralValue.class, 0).id(), ScalarType.NUMBER)
                .containsEntry(literal(ast, BooleanLiteralValue.class, 0).id(), ScalarType.BOOLEAN)
                .containsEntry(literal(ast, StringLiteralValue.class, 0).id(), ScalarType.STRING)
                .containsEntry(literal(ast, NullLiteralValue.class, 0).id(), NullType.INSTANCE)
                .containsEntry(vector(ast, 0).id(), new VectorType(UnknownType.INSTANCE))
                .containsEntry(vector(ast, 1).id(), new VectorType(ScalarType.NUMBER));
    }

    @Test
    @DisplayName("offset datetime literals keep AST source value and expose environment-normalized prepared value")
    void offsetDateTimeLiteralsKeepAstSourceValueAndExposeEnvironmentNormalizedPreparedValue() {
        ExpressionFileNode ast = AstTestSupport.build("dt\"2024-01-01T10:00:00+02:00\"");
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .zoneId(ZoneId.of("UTC"))
                .build();

        SemanticModel model = resolveModel(ast, environment);

        LiteralNode literal = literal(ast, OffsetDateTimeLiteralValue.class, 0);
        assertThat(((OffsetDateTimeLiteralValue) literal.value()).value())
                .isEqualTo(OffsetDateTime.parse("2024-01-01T10:00:00+02:00"));
        assertThat(model.preparedValues())
                .containsEntry(
                        literal.id(),
                        new PreparedOffsetDateTimeLiteralValue(LocalDateTime.parse("2024-01-01T08:00:00")));
    }

    @Test
    @DisplayName("regex pattern literals compile during semantic resolution")
    void regexPatternLiteralsCompileDuringSemanticResolution() {
        ExpressionFileNode ast = AstTestSupport.build("text =~ \"[a-z]+\"");
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("text", ScalarType.STRING)
                .build();

        SemanticModel model = resolveModel(ast, environment);

        LiteralNode patternLiteral = literal(ast, StringLiteralValue.class, 0);
        assertThat(model.preparedValues())
                .extractingByKey(patternLiteral.id())
                .satisfies(prepared -> {
                    assertThat(prepared).isInstanceOf(PreparedRegexPatternValue.class);
                    PreparedRegexPatternValue regex = (PreparedRegexPatternValue) prepared;
                    assertThat(regex.pattern().pattern()).isEqualTo("[a-z]+");
                    assertThat(regex.pattern().matcher("abc").matches()).isTrue();
                });
    }

    @Test
    @DisplayName("invalid regex patterns produce semantic diagnostics on the pattern literal span")
    void invalidRegexPatternsProduceSemanticDiagnosticsOnPatternLiteralSpan() {
        ExpressionFileNode ast = AstTestSupport.build("text =~ \"[\"");
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("text", ScalarType.STRING)
                .build();

        SemanticResolutionResult result = resolver.resolve(ast, environment);

        assertThat(result.hasErrors()).isTrue();
        assertThat(result.model()).isEmpty();
        assertThat(result.diagnostics())
                .singleElement()
                .satisfies(diagnostic -> {
                    assertThat(diagnostic.code()).isEqualTo(DiagnosticCode.SEMANTIC_INVALID_REGEX_PATTERN);
                    assertThat(diagnostic.span()).isEqualTo(new SourceSpan(8, 11, 1, 9));
                });
    }

    @Test
    @DisplayName("unknown regex left operands become non-strict residual checks instead of inferred strings")
    void unknownRegexLeftOperandsBecomeNonStrictResidualChecksInsteadOfInferredStrings() {
        ExpressionFileNode ast = AstTestSupport.build("lateBound =~ \"x\"");
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("lateBound")
                .build();

        SemanticModel model = resolveModel(ast, environment);

        assertThat(model.externalSymbols().getFirst().type()).isEqualTo(UnknownType.INSTANCE);
        assertThat(model.residualTypeChecks())
                .singleElement()
                .satisfies(check -> assertThat(check.description())
                        .contains("Regex left operand type remains unknown"));
    }

    @Test
    @DisplayName("regex left operand checks use grouped expression types")
    void regexLeftOperandChecksUseGroupedExpressionTypes() {
        ExpressionFileNode ast = AstTestSupport.build("(text) =~ \"x\"");
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("text", ScalarType.STRING)
                .strictMode(true)
                .build();

        SemanticModel model = resolveModel(ast, environment);

        assertThat(model.residualTypeChecks()).isEmpty();
    }

    @Test
    @DisplayName("regex left operand checks run after joins resolve concrete string types")
    void regexLeftOperandChecksRunAfterJoinsResolveConcreteStringTypes() {
        ExpressionFileNode ast = AstTestSupport.build("(null ?? \"text\") =~ \"x\"");
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .strictMode(true)
                .build();

        SemanticModel model = resolveModel(ast, environment);

        assertThat(model.residualTypeChecks()).isEmpty();
    }

    @Test
    @DisplayName("regex operators produce a didactic diagnostic for incompatible left operands")
    void regexOperatorsProduceDidacticDiagnosticForIncompatibleLeftOperands() {
        ExpressionFileNode ast = AstTestSupport.build("5!~\"x\"");

        SemanticResolutionResult result = resolver.resolve(ast, ExpressionEnvironment.standard());

        assertThat(result.hasErrors()).isTrue();
        assertThat(result.model()).isEmpty();
        assertThat(result.diagnostics())
                .singleElement()
                .satisfies(diagnostic -> {
                    assertThat(diagnostic.code()).isEqualTo(DiagnosticCode.SEMANTIC_REGEX_LEFT_OPERAND_TYPE_MISMATCH);
                    assertThat(diagnostic.message())
                            .contains("Regex matching")
                            .contains("string left operand")
                            .contains("5!~\"x\"");
                    assertThat(diagnostic.span()).isEqualTo(new SourceSpan(0, 1, 1, 1));
                });
    }

    @Test
    @DisplayName("vector literals reject incompatible concrete element types")
    void vectorLiteralsRejectIncompatibleConcreteElementTypes() {
        ExpressionFileNode ast = AstTestSupport.build("[1, true]");

        SemanticResolutionResult result = resolver.resolve(ast, ExpressionEnvironment.standard());

        assertThat(result.hasErrors()).isTrue();
        assertThat(result.model()).isEmpty();
        assertThat(result.diagnostics())
                .singleElement()
                .satisfies(diagnostic -> {
                    assertThat(diagnostic.code()).isEqualTo(DiagnosticCode.SEMANTIC_TYPE_RESTRICTION_CONFLICT);
                    assertThat(diagnostic.span()).isEqualTo(new SourceSpan(0, 9, 1, 1));
                });
    }

    @Test
    @DisplayName("vector literals infer unknown elements from a concrete common element")
    void vectorLiteralsInferUnknownElementsFromConcreteCommonElement() {
        ExpressionFileNode ast = AstTestSupport.build("[1, lateBound]");
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("lateBound")
                .strictMode(true)
                .build();

        SemanticModel model = resolveModel(ast, environment);

        assertThat(model.resolvedTypes())
                .containsEntry(identifier(ast, "lateBound", 0).id(), ScalarType.NUMBER)
                .containsEntry(vector(ast, 0).id(), new VectorType(ScalarType.NUMBER));
    }

    @Test
    @DisplayName("empty vector element unknown does not fail strict mode")
    void emptyVectorElementUnknownDoesNotFailStrictMode() {
        ExpressionFileNode ast = AstTestSupport.build("[]");
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .strictMode(true)
                .build();

        SemanticModel model = resolveModel(ast, environment);

        assertThat(model.resolvedTypes()).containsEntry(vector(ast, 0).id(), new VectorType(UnknownType.INSTANCE));
    }

    @Test
    @DisplayName("empty vector assigned to a symbol does not fail strict mode")
    void emptyVectorAssignedToSymbolDoesNotFailStrictMode() {
        ExpressionFileNode ast = AstTestSupport.build("values := []; values");
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .strictMode(true)
                .build();

        SemanticModel model = resolveModel(ast, environment);

        assertThat(model.internalSymbols().getFirst().type()).isEqualTo(new VectorType(UnknownType.INSTANCE));
        assertThat(model.resolvedTypes()).containsEntry(identifier(ast, "values", 0).id(), new VectorType(UnknownType.INSTANCE));
    }

    @Test
    @DisplayName("external symbol concrete restrictions update resolved symbol metadata")
    void externalSymbolConcreteRestrictionsUpdateResolvedSymbolMetadata() {
        ExpressionFileNode ast = AstTestSupport.build("lateBound + 1");
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("lateBound")
                .strictMode(true)
                .build();

        SemanticModel model = resolveModel(ast, environment);

        ResolvedSymbol lateBound = model.externalSymbols().getFirst();
        assertThat(lateBound.name()).isEqualTo("lateBound");
        assertThat(lateBound.type()).isEqualTo(ScalarType.NUMBER);
        assertThat(model.symbolByNodeId()).containsEntry(identifier(ast, "lateBound", 0).id(), lateBound);
    }

    @Test
    @DisplayName("concrete symbol restrictions unify and reject incompatible reassignments")
    void concreteSymbolRestrictionsRejectIncompatibleReassignments() {
        ExpressionFileNode ast = AstTestSupport.build("total := 1; total := true; total");

        SemanticResolutionResult result = resolver.resolve(ast, ExpressionEnvironment.standard());

        assertThat(result.hasErrors()).isTrue();
        assertThat(result.model()).isEmpty();
        assertThat(result.diagnostics())
                .singleElement()
                .satisfies(diagnostic -> {
                    assertThat(diagnostic.code()).isEqualTo(DiagnosticCode.SEMANTIC_TYPE_RESTRICTION_CONFLICT);
                    assertThat(diagnostic.span()).isEqualTo(new SourceSpan(12, 26, 1, 13));
                });
    }

    @Test
    @DisplayName("operators reject incompatible concrete operand types without boundary coercion")
    void operatorsRejectIncompatibleConcreteOperandTypesWithoutBoundaryCoercion() {
        ExpressionFileNode ast = AstTestSupport.build("1 + \"2\"");

        SemanticResolutionResult result = resolver.resolve(ast, ExpressionEnvironment.standard());

        assertThat(result.hasErrors()).isTrue();
        assertThat(result.model()).isEmpty();
        assertThat(result.diagnostics())
                .singleElement()
                .satisfies(diagnostic -> {
                    assertThat(diagnostic.code()).isEqualTo(DiagnosticCode.SEMANTIC_OPERATOR_TYPE_MISMATCH);
                    assertThat(diagnostic.span()).isEqualTo(new SourceSpan(2, 3, 1, 3));
                });
    }

    @Test
    @DisplayName("membership operators reject incompatible concrete candidate element types")
    void membershipOperatorsRejectIncompatibleConcreteCandidateElementTypes() {
        ExpressionFileNode ast = AstTestSupport.build("1 in [true]");

        SemanticResolutionResult result = resolver.resolve(ast, ExpressionEnvironment.standard());

        assertThat(result.hasErrors()).isTrue();
        assertThat(result.model()).isEmpty();
        assertThat(result.diagnostics())
                .singleElement()
                .satisfies(diagnostic -> {
                    assertThat(diagnostic.code()).isEqualTo(DiagnosticCode.SEMANTIC_OPERATOR_TYPE_MISMATCH);
                    assertThat(diagnostic.span()).isEqualTo(new SourceSpan(2, 4, 1, 3));
                });
    }

    @Test
    @DisplayName("abstract orderable restrictions defer in non-strict mode and fail in strict mode")
    void abstractOrderableRestrictionsDeferInNonStrictModeAndFailInStrictMode() {
        ExpressionFileNode nonStrictAst = AstTestSupport.build("left < right");
        ExpressionEnvironment nonStrictEnvironment = ExpressionEnvironment.builder()
                .externalSymbol("left")
                .externalSymbol("right")
                .build();

        SemanticModel model = resolveModel(nonStrictAst, nonStrictEnvironment);

        assertThat(model.residualTypeChecks())
                .singleElement()
                .satisfies(check -> assertThat(check.code())
                        .isEqualTo(DiagnosticCode.SEMANTIC_STRICT_UNKNOWN_TYPE_RESTRICTION));

        ExpressionFileNode strictAst = AstTestSupport.build("left < right");
        ExpressionEnvironment strictEnvironment = nonStrictEnvironment.toBuilder()
                .strictMode(true)
                .build();

        SemanticResolutionResult result = resolver.resolve(strictAst, strictEnvironment);

        assertThat(result.hasErrors()).isTrue();
        assertThat(result.model()).isEmpty();
        assertThat(result.diagnostics())
                .anySatisfy(diagnostic -> {
                    assertThat(diagnostic.code()).isEqualTo(DiagnosticCode.SEMANTIC_STRICT_UNKNOWN_TYPE_RESTRICTION);
                    assertThat(diagnostic.span()).isEqualTo(new SourceSpan(5, 6, 1, 6));
                });
    }

    @Test
    @DisplayName("null coalescence joins null with a concrete non-null type")
    void nullCoalescenceJoinsNullWithConcreteNonNullType() {
        ExpressionFileNode ast = AstTestSupport.build("null ?? 1");

        SemanticModel model = resolveModel(ast, ExpressionEnvironment.standard());

        NullCoalescenceNode coalescence = node(ast, NullCoalescenceNode.class, 0);
        assertThat(model.resolvedTypes()).containsEntry(coalescence.id(), ScalarType.NUMBER);
    }

    @Test
    @DisplayName("joined assignment expressions update the internal symbol type")
    void joinedAssignmentExpressionsUpdateInternalSymbolType() {
        ExpressionFileNode ast = AstTestSupport.build("value := null ?? 1; value");

        SemanticModel model = resolveModel(ast, ExpressionEnvironment.standard());

        assertThat(model.internalSymbols().getFirst().type()).isEqualTo(ScalarType.NUMBER);
        assertThat(model.resolvedTypes()).containsEntry(identifier(ast, "value", 0).id(), ScalarType.NUMBER);
    }

    @Test
    @DisplayName("downstream concrete restrictions refine unknown null-join branches")
    void downstreamConcreteRestrictionsRefineUnknownNullJoinBranches() {
        ExpressionFileNode ast = AstTestSupport.build("value := null ?? lateBound; value + 1");
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("lateBound")
                .strictMode(true)
                .build();

        SemanticModel model = resolveModel(ast, environment);

        assertThat(model.externalSymbols().getFirst().type()).isEqualTo(ScalarType.NUMBER);
        assertThat(model.internalSymbols().getFirst().type()).isEqualTo(ScalarType.NUMBER);
    }

    @Test
    @DisplayName("later concrete assignment refines a symbol first assigned from unknown")
    void laterConcreteAssignmentRefinesSymbolFirstAssignedFromUnknown() {
        ExpressionFileNode ast = AstTestSupport.build("value := lateBound; value := 1; value");
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("lateBound")
                .build();

        SemanticModel model = resolveModel(ast, environment);

        assertThat(model.internalSymbols().getFirst().type()).isEqualTo(ScalarType.NUMBER);
    }

    @Test
    @DisplayName("empty vector strict exemption does not survive unrelated unknown reassignment")
    void emptyVectorStrictExemptionDoesNotSurviveUnknownReassignment() {
        ExpressionFileNode ast = AstTestSupport.build("values := []; values := lateBound; values");
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("lateBound")
                .strictMode(true)
                .build();

        SemanticResolutionResult result = resolver.resolve(ast, environment);

        assertThat(result.hasErrors()).isTrue();
        assertThat(result.diagnostics())
                .anySatisfy(diagnostic -> assertThat(diagnostic.code())
                        .isEqualTo(DiagnosticCode.SEMANTIC_STRICT_UNKNOWN_TYPE_RESTRICTION));
    }

    @Test
    @DisplayName("root and numeric operators validate numeric operands and provable constants")
    void rootAndNumericOperatorsValidateNumericOperandsAndProvableConstants() {
        ExpressionFileNode typeMismatch = AstTestSupport.build("2 root true");

        SemanticResolutionResult mismatchResult = resolver.resolve(typeMismatch, ExpressionEnvironment.standard());

        assertThat(mismatchResult.hasErrors()).isTrue();
        assertThat(mismatchResult.diagnostics())
                .singleElement()
                .satisfies(diagnostic -> assertThat(diagnostic.code())
                        .isEqualTo(DiagnosticCode.SEMANTIC_OPERATOR_TYPE_MISMATCH));

        ExpressionFileNode invalidConstant = AstTestSupport.build("0 root 9");

        SemanticResolutionResult constantResult = resolver.resolve(invalidConstant, ExpressionEnvironment.standard());

        assertThat(constantResult.hasErrors()).isTrue();
        assertThat(constantResult.diagnostics())
                .singleElement()
                .satisfies(diagnostic -> assertThat(diagnostic.code())
                        .isEqualTo(DiagnosticCode.SEMANTIC_INVALID_NUMERIC_CONSTANT));
    }

    @Test
    @DisplayName("numeric category is populated conservatively from numeric mode")
    void numericCategoryIsPopulatedConservativelyFromNumericMode() {
        ExpressionFileNode decimalAst = AstTestSupport.build("1 + 2.5");
        SemanticModel decimalModel = resolveModel(decimalAst, ExpressionEnvironment.standard());

        assertThat(decimalModel.numericKinds().values()).containsOnly(NumericKind.DECIMAL);

        ExpressionFileNode fastAst = AstTestSupport.build("1 + 2.5");
        ExpressionEnvironment fastEnvironment = ExpressionEnvironment.builder()
                .numericMode(NumericMode.FAST)
                .build();
        SemanticModel fastModel = resolveModel(fastAst, fastEnvironment);

        assertThat(fastModel.numericKinds())
                .containsEntry(literal(fastAst, LongLiteralValue.class, 0).id(), NumericKind.INTEGRAL)
                .containsEntry(literal(fastAst, DecimalLiteralValue.class, 0).id(), NumericKind.FLOATING)
                .containsEntry(node(fastAst, BinaryOperationNode.class, 0).id(), NumericKind.FLOATING);
    }

    @Test
    @DisplayName("exact global function calls resolve to function bindings and result types")
    void exactGlobalFunctionCallsResolveToFunctionBindingsAndResultTypes() {
        FunctionDescriptor numberScore = pureFunction("score", ScalarType.NUMBER, ScalarType.NUMBER);
        FunctionDescriptor textScore = pureFunction("score", ScalarType.STRING, ScalarType.STRING);
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .withoutStandardFunctions()
                .function(textScore)
                .function(numberScore)
                .build();
        ExpressionFileNode ast = AstTestSupport.build("score(1)");

        SemanticModel model = resolveModel(ast, environment);

        FunctionCallNode call = node(ast, FunctionCallNode.class, 0);
        assertThat(model.functionBindings())
                .containsEntry(call.id(), new ResolvedFunctionBinding(
                        numberScore,
                        ResolvedFunctionBinding.UnknownArgumentHandling.NONE,
                        List.of()));
        assertThat(model.resolvedTypes()).containsEntry(call.id(), ScalarType.NUMBER);
    }

    @Test
    @DisplayName("global function calls reject concrete incompatible arguments without boundary coercion")
    void globalFunctionCallsRejectConcreteIncompatibleArgumentsWithoutBoundaryCoercion() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .withoutStandardFunctions()
                .function(pureFunction("score", ScalarType.NUMBER, ScalarType.NUMBER))
                .build();
        ExpressionFileNode ast = AstTestSupport.build("score(\"12\")");

        SemanticResolutionResult result = resolver.resolve(ast, environment);

        assertThat(result.hasErrors()).isTrue();
        assertThat(result.model()).isEmpty();
        assertThat(result.diagnostics())
                .singleElement()
                .satisfies(diagnostic -> {
                    assertThat(diagnostic.code()).isEqualTo(DiagnosticCode.SEMANTIC_FUNCTION_ARGUMENT_TYPE_MISMATCH);
                    assertThat(diagnostic.span()).isEqualTo(new SourceSpan(0, 11, 1, 1));
                });
    }

    @Test
    @DisplayName("unknown function arguments bind only when one semantic signature is possible")
    void unknownFunctionArgumentsBindOnlyWhenOneSemanticSignatureIsPossible() {
        FunctionDescriptor onlyScore = pureFunction("score", ScalarType.BOOLEAN, ScalarType.NUMBER);
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .withoutStandardFunctions()
                .externalSymbol("lateBound")
                .function(onlyScore)
                .build();
        ExpressionFileNode ast = AstTestSupport.build("score(lateBound)");

        SemanticModel model = resolveModel(ast, environment);

        FunctionCallNode call = node(ast, FunctionCallNode.class, 0);
        assertThat(model.functionBindings())
                .containsEntry(call.id(), new ResolvedFunctionBinding(
                        onlyScore,
                        ResolvedFunctionBinding.UnknownArgumentHandling.RESIDUAL_CHECK,
                        List.of(identifier(ast, "lateBound", 0).id())));
        assertThat(model.resolvedTypes()).containsEntry(call.id(), ScalarType.BOOLEAN);
        assertThat(model.externalSymbols().getFirst().type()).isEqualTo(UnknownType.INSTANCE);
    }

    @Test
    @DisplayName("ambiguous unknown function overloads produce a semantic diagnostic")
    void ambiguousUnknownFunctionOverloadsProduceSemanticDiagnostic() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .withoutStandardFunctions()
                .externalSymbol("lateBound")
                .function(pureFunction("score", ScalarType.NUMBER, ScalarType.NUMBER))
                .function(pureFunction("score", ScalarType.NUMBER, ScalarType.STRING))
                .build();
        ExpressionFileNode ast = AstTestSupport.build("score(lateBound)");

        SemanticResolutionResult result = resolver.resolve(ast, environment);

        assertThat(result.hasErrors()).isTrue();
        assertThat(result.model()).isEmpty();
        assertThat(result.diagnostics())
                .singleElement()
                .satisfies(diagnostic -> {
                    assertThat(diagnostic.code()).isEqualTo(DiagnosticCode.SEMANTIC_AMBIGUOUS_FUNCTION_CALL);
                    assertThat(diagnostic.message()).contains("declared type").contains("assertion function");
                    assertThat(diagnostic.span()).isEqualTo(new SourceSpan(0, 16, 1, 1));
                });
    }

    @Test
    @DisplayName("function arguments use post-restriction expression types for overload selection")
    void functionArgumentsUsePostRestrictionExpressionTypesForOverloadSelection() {
        FunctionDescriptor numberScore = pureFunction("score", ScalarType.BOOLEAN, ScalarType.NUMBER);
        FunctionDescriptor textScore = pureFunction("score", ScalarType.STRING, ScalarType.STRING);
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .withoutStandardFunctions()
                .function(textScore)
                .function(numberScore)
                .build();
        ExpressionFileNode ast = AstTestSupport.build("score(null ?? 1)");

        SemanticModel model = resolveModel(ast, environment);

        FunctionCallNode call = node(ast, FunctionCallNode.class, 0);
        assertThat(model.functionBindings())
                .containsEntry(call.id(), new ResolvedFunctionBinding(
                        numberScore,
                        ResolvedFunctionBinding.UnknownArgumentHandling.NONE,
                        List.of()));
        assertThat(model.resolvedTypes()).containsEntry(call.id(), ScalarType.BOOLEAN);
    }

    @Test
    @DisplayName("grouped function arguments use the grouped expression type for overload selection")
    void groupedFunctionArgumentsUseGroupedExpressionTypeForOverloadSelection() {
        FunctionDescriptor numberScore = pureFunction("score", ScalarType.BOOLEAN, ScalarType.NUMBER);
        FunctionDescriptor textScore = pureFunction("score", ScalarType.STRING, ScalarType.STRING);
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .withoutStandardFunctions()
                .function(textScore)
                .function(numberScore)
                .build();
        ExpressionFileNode ast = AstTestSupport.build("score((null ?? 1))");

        SemanticModel model = resolveModel(ast, environment);

        FunctionCallNode call = node(ast, FunctionCallNode.class, 0);
        assertThat(model.functionBindings())
                .containsEntry(call.id(), new ResolvedFunctionBinding(
                        numberScore,
                        ResolvedFunctionBinding.UnknownArgumentHandling.NONE,
                        List.of()));
        assertThat(model.resolvedTypes()).containsEntry(call.id(), ScalarType.BOOLEAN);
    }

    @Test
    @DisplayName("assertion functions refine only the call result without changing the argument symbol type")
    void assertionFunctionsRefineOnlyTheCallResultWithoutChangingTheArgumentSymbolType() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("lateBound")
                .build();
        ExpressionFileNode ast = AstTestSupport.build("value := asNumber(lateBound); lateBound");

        SemanticModel model = resolveModel(ast, environment);

        FunctionCallNode call = node(ast, FunctionCallNode.class, 0);
        assertThat(model.functionBindings())
                .extractingByKey(call.id())
                .extracting(binding -> ((ResolvedFunctionBinding) binding).descriptor().signature())
                .isEqualTo(FunctionSignature.of("asNumber", List.of(UnknownType.INSTANCE)));
        assertThat(model.functionBindings())
                .extractingByKey(call.id())
                .extracting(binding -> ((ResolvedFunctionBinding) binding).unknownArgumentCheck())
                .isEqualTo(false);
        assertThat(model.resolvedTypes()).containsEntry(call.id(), ScalarType.NUMBER);
        assertThat(model.internalSymbols().getFirst().type()).isEqualTo(ScalarType.NUMBER);
        assertThat(model.externalSymbols().getFirst().type()).isEqualTo(UnknownType.INSTANCE);
    }

    @Test
    @DisplayName("strict mode accepts unknown arguments explicitly consumed by assertion functions")
    void strictModeAcceptsUnknownArgumentsExplicitlyConsumedByAssertionFunctions() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("lateBound")
                .strictMode(true)
                .build();
        ExpressionFileNode ast = AstTestSupport.build("asNumber(lateBound)");

        SemanticModel model = resolveModel(ast, environment);

        assertThat(model.externalSymbols().getFirst().type()).isEqualTo(UnknownType.INSTANCE);
        assertThat(model.residualTypeChecks()).isEmpty();
    }

    @Test
    @DisplayName("strict mode accepts unknown arguments consumed by asVector assertions")
    void strictModeAcceptsUnknownArgumentsConsumedByAsVectorAssertions() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("lateBound")
                .strictMode(true)
                .build();
        ExpressionFileNode ast = AstTestSupport.build("asVector(lateBound)");

        SemanticModel model = resolveModel(ast, environment);

        assertThat(model.resolvedTypes()).containsEntry(
                node(ast, FunctionCallNode.class, 0).id(),
                new VectorType(UnknownType.INSTANCE));
        assertThat(model.externalSymbols().getFirst().type()).isEqualTo(UnknownType.INSTANCE);
        assertThat(model.residualTypeChecks()).isEmpty();
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

    private static <T extends AstNode> T node(ExpressionFileNode ast, Class<T> type, int occurrence) {
        return AstTestSupport.flatten(ast).stream()
                .filter(type::isInstance)
                .map(type::cast)
                .skip(occurrence)
                .findFirst()
                .orElseThrow();
    }

    private static <T extends LiteralValue> LiteralNode literal(ExpressionFileNode ast, Class<T> valueType, int occurrence) {
        return AstTestSupport.flatten(ast).stream()
                .filter(LiteralNode.class::isInstance)
                .map(LiteralNode.class::cast)
                .filter(literal -> valueType.isInstance(literal.value()))
                .skip(occurrence)
                .findFirst()
                .orElseThrow();
    }

    private static VectorLiteralNode vector(ExpressionFileNode ast, int occurrence) {
        return node(ast, VectorLiteralNode.class, occurrence);
    }

    private static FunctionDescriptor pureFunction(
            String languageName,
            ExpressionType returnType,
            ExpressionType... parameterTypes) {
        FunctionDescriptor.Builder builder = FunctionDescriptor.builder(languageName)
                .parameterTypes(List.of(parameterTypes))
                .returnType(returnType)
                .implementationHandle(argumentHandle(languageName, parameterTypes.length), "test:" + languageName);
        return builder.pure().build();
    }

    private static MethodHandle argumentHandle(String languageName, int arity) {
        return MethodHandles.dropArguments(
                MethodHandles.constant(Object.class, languageName),
                0,
                Collections.nCopies(arity, Object.class));
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
