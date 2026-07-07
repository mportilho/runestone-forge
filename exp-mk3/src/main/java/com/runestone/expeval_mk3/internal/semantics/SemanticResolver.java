package com.runestone.expeval_mk3.internal.semantics;

import com.runestone.expeval_mk3.api.CurrentTemporalValue;
import com.runestone.expeval_mk3.api.ExpressionEnvironment;
import com.runestone.expeval_mk3.api.ExpressionType;
import com.runestone.expeval_mk3.api.ExternalSymbol;
import com.runestone.expeval_mk3.api.UnknownType;
import com.runestone.expeval_mk3.internal.ast.AssignedSymbolSource;
import com.runestone.expeval_mk3.internal.ast.AssignmentSymbolFlow;
import com.runestone.expeval_mk3.internal.ast.CurrentItemSource;
import com.runestone.expeval_mk3.internal.ast.ExpressionSemanticTree;
import com.runestone.expeval_mk3.internal.ast.NodeId;
import com.runestone.expeval_mk3.internal.ast.SemanticSymbolFlow;
import com.runestone.expeval_mk3.internal.ast.SymbolReadSource;
import com.runestone.expeval_mk3.internal.diagnostics.DiagnosticCategory;
import com.runestone.expeval_mk3.internal.diagnostics.DiagnosticCode;
import com.runestone.expeval_mk3.internal.diagnostics.DiagnosticSeverity;
import com.runestone.expeval_mk3.internal.diagnostics.ExpressionDiagnostic;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class SemanticResolver {

    public SemanticResolutionResult resolve(ExpressionSemanticTree sourceTree, ExpressionEnvironment environment) {
        Objects.requireNonNull(sourceTree, "sourceTree");
        Objects.requireNonNull(environment, "environment");

        List<ExpressionDiagnostic> diagnostics = new ArrayList<>();
        if (sourceTree.isEmptyFile()) {
            diagnostics.add(ExpressionDiagnostic.error(
                    DiagnosticCategory.SEMANTIC,
                    DiagnosticCode.SEMANTIC_EMPTY_EXPRESSION_FILE,
                    "Expression file must contain at least one assignment or result expression",
                    sourceTree.sourceSpan()));
            return SemanticResolutionResult.withoutModel(diagnostics);
        }

        SemanticSymbolFlow symbolFlow = sourceTree.symbolFlow();
        if (symbolFlow.maxCurrentItemDepth() > environment.maxCurrentItemDepth()) {
            diagnostics.add(ExpressionDiagnostic.error(
                    DiagnosticCategory.SEMANTIC,
                    DiagnosticCode.SEMANTIC_CURRENT_ITEM_DEPTH_EXCEEDED,
                    "Current item depth exceeds environment limit: " + symbolFlow.maxCurrentItemDepth(),
                    sourceTree.sourceSpan()));
        }
        SymbolResolutionState state = resolveSymbols(symbolFlow, environment, diagnostics);
        if (hasErrors(diagnostics)) {
            return SemanticResolutionResult.withoutModel(diagnostics);
        }

        SemanticModelSymbols modelSymbols = state.toResolvedModelSymbols(environment, symbolFlow);
        return SemanticResolutionResult.withModel(new SemanticModel(
                sourceTree,
                environment,
                diagnostics,
                modelSymbols));
    }

    private static boolean hasErrors(List<ExpressionDiagnostic> diagnostics) {
        return diagnostics.stream().anyMatch(diagnostic -> diagnostic.severity() == DiagnosticSeverity.ERROR);
    }

    private SymbolResolutionState resolveSymbols(
            SemanticSymbolFlow symbolFlow,
            ExpressionEnvironment environment,
            List<ExpressionDiagnostic> diagnostics) {
        SymbolResolutionState state = new SymbolResolutionState(symbolFlow.knownExpressionTypes());
        for (AssignmentSymbolFlow assignment : symbolFlow.assignments()) {
            for (SymbolReadSource read : assignment.expressionReads()) {
                state.bindRead(read, environment, diagnostics);
            }

            ExpressionType assignmentType = assignment.expressionType()
                    .or(() -> assignment.expressionRootRead().flatMap(state::typeOfBoundRead))
                    .orElse(UnknownType.INSTANCE);
            for (AssignedSymbolSource target : assignment.targetSymbols()) {
                if (CurrentTemporalValue.findBySimpleName(target.name()).isPresent()) {
                    diagnostics.add(ExpressionDiagnostic.error(
                            DiagnosticCategory.SEMANTIC,
                            DiagnosticCode.SEMANTIC_RESERVED_SYMBOL_ASSIGNMENT,
                            "Reserved name cannot be assigned: " + target.name(),
                            target.sourceSpan()));
                    continue;
                }
                boolean created = state.ensureInternal(target.name(), assignmentType);
                if (created && environment.externalSymbols().contains(target.name())) {
                    diagnostics.add(ExpressionDiagnostic.warning(
                            DiagnosticCategory.SEMANTIC,
                            DiagnosticCode.SEMANTIC_EXTERNAL_SYMBOL_SHADOWED,
                            "Internal symbol shadows external symbol: " + target.name(),
                            target.sourceSpan()));
                }
                state.bindTarget(target);
            }
        }

        for (SymbolReadSource read : symbolFlow.resultReads()) {
            state.bindRead(read, environment, diagnostics);
        }
        for (CurrentItemSource currentItem : symbolFlow.currentItemSources()) {
            state.bindCurrentItem(currentItem, environment, diagnostics);
        }
        return state;
    }

    private static final class SymbolResolutionState {

        private final Map<String, InternalSymbolState> internalSymbolsByName = new LinkedHashMap<>();
        private final Map<NodeId, SymbolBinding> symbolBindings = new LinkedHashMap<>();
        private final Map<NodeId, ExpressionType> resolvedTypes = new LinkedHashMap<>();

        private SymbolResolutionState(Map<NodeId, ExpressionType> knownExpressionTypes) {
            resolvedTypes.putAll(knownExpressionTypes);
        }

        private void bindRead(
                SymbolReadSource read,
                ExpressionEnvironment environment,
                List<ExpressionDiagnostic> diagnostics) {
            InternalSymbolState internalSymbol = internalSymbolsByName.get(read.name());
            if (internalSymbol != null) {
                symbolBindings.put(read.nodeId(), new InternalBinding(read.name()));
                resolvedTypes.put(read.nodeId(), internalSymbol.type());
                return;
            }

            Optional<ExternalSymbol> externalSymbol = environment.externalSymbols().find(read.name());
            if (externalSymbol.isPresent()) {
                symbolBindings.put(read.nodeId(), new ExternalBinding(read.name()));
                resolvedTypes.put(read.nodeId(), externalSymbol.orElseThrow().type());
            }
            if (externalSymbol.isEmpty()) {
                diagnostics.add(ExpressionDiagnostic.error(
                        DiagnosticCategory.SEMANTIC,
                        DiagnosticCode.SEMANTIC_UNRESOLVED_SYMBOL,
                        "Symbol is not declared or assigned before use: " + read.name(),
                        read.sourceSpan()));
            }
        }

        private boolean ensureInternal(String name, ExpressionType assignmentType) {
            InternalSymbolState existing = internalSymbolsByName.get(name);
            if (existing != null) {
                existing.updateType(assignmentType);
                return false;
            }
            internalSymbolsByName.put(name, new InternalSymbolState(name, internalSymbolsByName.size(), assignmentType));
            return true;
        }

        private void bindTarget(AssignedSymbolSource target) {
            InternalSymbolState internalSymbol = internalSymbolsByName.get(target.name());
            symbolBindings.put(target.nodeId(), new InternalBinding(target.name()));
            resolvedTypes.put(target.nodeId(), internalSymbol.type());
        }

        private void bindCurrentItem(
                CurrentItemSource currentItem,
                ExpressionEnvironment environment,
                List<ExpressionDiagnostic> diagnostics) {
            if (currentItem.depth() == 0) {
                diagnostics.add(ExpressionDiagnostic.error(
                        DiagnosticCategory.SEMANTIC,
                        DiagnosticCode.SEMANTIC_CURRENT_ITEM_OUTSIDE_CONTEXT,
                        "Current item can only be used inside a filter or lambda",
                        currentItem.sourceSpan()));
                return;
            }
            symbolBindings.put(currentItem.nodeId(), new CurrentItemBinding(currentItem.depth()));
            resolvedTypes.put(currentItem.nodeId(), UnknownType.INSTANCE);
        }

        private Optional<ExpressionType> typeOfBoundRead(SymbolReadSource read) {
            return Optional.ofNullable(resolvedTypes.get(read.nodeId()));
        }

        private SemanticModelSymbols toResolvedModelSymbols(
                ExpressionEnvironment environment,
                SemanticSymbolFlow symbolFlow) {
            List<ResolvedSymbol> internalSymbols = internalSymbolsByName.values().stream()
                    .<ResolvedSymbol>map(symbol -> new NamedResolvedSymbol(
                            symbol.name(),
                            ResolvedSymbolKind.INTERNAL,
                            symbol.type(),
                            symbol.slot()))
                    .toList();

            int externalSlotBase = internalSymbols.size();
            List<ResolvedSymbol> externalSymbols = new ArrayList<>(environment.externalSymbols().size());
            int externalOffset = 0;
            for (ExternalSymbol externalSymbol : environment.externalSymbols().values()) {
                externalSymbols.add(new NamedResolvedSymbol(
                        externalSymbol.name(),
                        ResolvedSymbolKind.EXTERNAL,
                        externalSymbol.type(),
                        externalSlotBase + externalOffset));
                externalOffset++;
            }

            int currentItemSlotBase = externalSlotBase + externalSymbols.size();
            List<ResolvedSymbol> currentItemSymbols = new ArrayList<>(symbolFlow.maxCurrentItemDepth());
            for (int depth = 1; depth <= symbolFlow.maxCurrentItemDepth(); depth++) {
                currentItemSymbols.add(new CurrentItemResolvedSymbol(
                        depth,
                        UnknownType.INSTANCE,
                        currentItemSlotBase + depth - 1));
            }

            Map<String, ResolvedSymbol> internalByName = byName(internalSymbols);
            Map<String, ResolvedSymbol> externalByName = byName(externalSymbols);
            Map<Integer, ResolvedSymbol> currentItemByDepth = currentItemByDepth(currentItemSymbols);
            Map<NodeId, ResolvedSymbol> symbolByNodeId = new LinkedHashMap<>();
            for (Map.Entry<NodeId, SymbolBinding> entry : symbolBindings.entrySet()) {
                ResolvedSymbol symbol = switch (entry.getValue()) {
                    case InternalBinding internal -> internalByName.get(internal.name());
                    case ExternalBinding external -> externalByName.get(external.name());
                    case CurrentItemBinding currentItem -> currentItemByDepth.get(currentItem.depth());
                };
                if (symbol != null) {
                    symbolByNodeId.put(entry.getKey(), symbol);
                    resolvedTypes.put(entry.getKey(), symbol.type());
                }
            }

            List<FrameSlot> slots = new ArrayList<>(
                    internalSymbols.size() + externalSymbols.size() + currentItemSymbols.size());
            addSlots(slots, FrameSlotKind.INTERNAL_SYMBOL, internalSymbols);
            addSlots(slots, FrameSlotKind.EXTERNAL_SYMBOL, externalSymbols);
            addSlots(slots, FrameSlotKind.CURRENT_ITEM, currentItemSymbols);
            return new SemanticModelSymbols(
                    resolvedTypes,
                    symbolByNodeId,
                    new ResolvedSymbolSets(internalSymbols, externalSymbols, currentItemSymbols),
                    new FrameLayout(slots));
        }

        private static void addSlots(List<FrameSlot> slots, FrameSlotKind kind, List<ResolvedSymbol> symbols) {
            for (ResolvedSymbol symbol : symbols) {
                slots.add(new FrameSlot(symbol.slot(), kind, symbol.name(), symbol.type()));
            }
        }

        private static Map<String, ResolvedSymbol> byName(List<ResolvedSymbol> symbols) {
            Map<String, ResolvedSymbol> symbolsByName = new LinkedHashMap<>();
            for (ResolvedSymbol symbol : symbols) {
                symbolsByName.put(symbol.name(), symbol);
            }
            return symbolsByName;
        }

        private static Map<Integer, ResolvedSymbol> currentItemByDepth(List<ResolvedSymbol> symbols) {
            Map<Integer, ResolvedSymbol> symbolsByDepth = new LinkedHashMap<>();
            for (ResolvedSymbol symbol : symbols) {
                if (symbol instanceof CurrentItemResolvedSymbol currentItemSymbol) {
                    symbolsByDepth.put(currentItemSymbol.currentItemDepth(), symbol);
                }
            }
            return symbolsByDepth;
        }
    }

    private static final class InternalSymbolState {

        private final String name;
        private final int slot;
        private ExpressionType type;

        private InternalSymbolState(String name, int slot, ExpressionType type) {
            this.name = Objects.requireNonNull(name, "name");
            this.slot = slot;
            this.type = Objects.requireNonNull(type, "type");
        }

        private String name() {
            return name;
        }

        private int slot() {
            return slot;
        }

        private ExpressionType type() {
            return type;
        }

        private void updateType(ExpressionType nextType) {
            type = unify(type, nextType);
        }

        private static ExpressionType unify(ExpressionType currentType, ExpressionType nextType) {
            Objects.requireNonNull(currentType, "currentType");
            Objects.requireNonNull(nextType, "nextType");
            if (currentType == UnknownType.INSTANCE || nextType == UnknownType.INSTANCE) {
                return UnknownType.INSTANCE;
            }
            if (currentType.equals(nextType)) {
                return currentType;
            }
            return UnknownType.INSTANCE;
        }
    }

    private sealed interface SymbolBinding permits CurrentItemBinding, ExternalBinding, InternalBinding {
    }

    private record InternalBinding(String name) implements SymbolBinding {

        private InternalBinding {
            Objects.requireNonNull(name, "name");
        }
    }

    private record ExternalBinding(String name) implements SymbolBinding {

        private ExternalBinding {
            Objects.requireNonNull(name, "name");
        }
    }

    private record CurrentItemBinding(int depth) implements SymbolBinding {

        private CurrentItemBinding {
            if (depth < 1) {
                throw new IllegalArgumentException("depth must be one or greater");
            }
        }
    }

}
