package com.runestone.expeval_mk3.internal.semantics;

import com.runestone.expeval_mk3.api.CurrentTemporalValue;
import com.runestone.expeval_mk3.api.CollectionType;
import com.runestone.expeval_mk3.api.ExpressionEnvironment;
import com.runestone.expeval_mk3.api.ExpressionType;
import com.runestone.expeval_mk3.api.ExternalSymbol;
import com.runestone.expeval_mk3.api.FunctionDescriptor;
import com.runestone.expeval_mk3.api.FunctionResolution;
import com.runestone.expeval_mk3.api.NullType;
import com.runestone.expeval_mk3.api.NumericMode;
import com.runestone.expeval_mk3.api.ScalarType;
import com.runestone.expeval_mk3.api.StandardFunctionGroup;
import com.runestone.expeval_mk3.api.UnknownType;
import com.runestone.expeval_mk3.api.VectorType;
import com.runestone.expeval_mk3.internal.ast.AssignedSymbolSource;
import com.runestone.expeval_mk3.internal.ast.AssignmentSymbolFlow;
import com.runestone.expeval_mk3.internal.ast.ConcreteTypeRestriction;
import com.runestone.expeval_mk3.internal.ast.CurrentItemSource;
import com.runestone.expeval_mk3.internal.ast.ExpressionSemanticTree;
import com.runestone.expeval_mk3.internal.ast.FunctionCallSource;
import com.runestone.expeval_mk3.internal.ast.MembershipTypeRestriction;
import com.runestone.expeval_mk3.internal.ast.NodeId;
import com.runestone.expeval_mk3.internal.ast.NumericConstantRestriction;
import com.runestone.expeval_mk3.internal.ast.NumericSource;
import com.runestone.expeval_mk3.internal.ast.NumericSourceKind;
import com.runestone.expeval_mk3.internal.ast.SameTypeRestriction;
import com.runestone.expeval_mk3.internal.ast.SemanticSymbolFlow;
import com.runestone.expeval_mk3.internal.ast.SymbolReadSource;
import com.runestone.expeval_mk3.internal.ast.TypeJoinRestriction;
import com.runestone.expeval_mk3.internal.ast.TypeRestrictionFlow;
import com.runestone.expeval_mk3.internal.ast.TypeCompatibilityMode;
import com.runestone.expeval_mk3.internal.ast.VectorElementTypeRestriction;
import com.runestone.expeval_mk3.internal.diagnostics.DiagnosticCategory;
import com.runestone.expeval_mk3.internal.diagnostics.DiagnosticCode;
import com.runestone.expeval_mk3.internal.diagnostics.DiagnosticSeverity;
import com.runestone.expeval_mk3.internal.diagnostics.ExpressionDiagnostic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class SemanticResolver {

    private static final Set<String> ASSERTION_FUNCTION_NAMES = StandardFunctionGroup.ASSERTION.requiredFunctionNames();

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

        state.resolveFunctionBindings(symbolFlow, environment, diagnostics, FunctionBindingPass.INFER_RESULT_TYPES);
        state.resolveTypeRestrictions(symbolFlow, environment, diagnostics);
        state.resolveFunctionBindings(symbolFlow, environment, diagnostics, FunctionBindingPass.REPORT_DIAGNOSTICS);
        state.propagateStrictUnknownExemptions(symbolFlow.assignments());
        if (environment.strictMode()) {
            state.rejectStrictUnknowns(symbolFlow.typeRestrictions(), diagnostics);
        }
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

    private static String displayType(ExpressionType type) {
        return type.toString();
    }

    private enum FunctionBindingPass {
        INFER_RESULT_TYPES {
            @Override
            boolean reportsDiagnostics() {
                return false;
            }
        },
        REPORT_DIAGNOSTICS {
            @Override
            boolean reportsDiagnostics() {
                return true;
            }
        };

        abstract boolean reportsDiagnostics();
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
                boolean created = state.ensureInternal(
                        target.name(),
                        assignmentType,
                        assignment.sourceSpan(),
                        symbolFlow.typeRestrictions().emptyVectorNodes().contains(target.nodeId()),
                        diagnostics);
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
        private final Map<String, ExpressionType> inferredExternalTypesByName = new LinkedHashMap<>();
        private final Map<NodeId, SymbolBinding> symbolBindings = new LinkedHashMap<>();
        private final Map<NodeId, ExpressionType> resolvedTypes = new LinkedHashMap<>();
        private final Map<NodeId, ResolvedFunctionBinding> functionBindings = new LinkedHashMap<>();
        private final List<ResidualTypeCheck> residualTypeChecks = new ArrayList<>();
        private final Set<NodeId> emptyVectorExemptNodeIds = new HashSet<>();
        private final Set<NodeId> functionArgumentUnknownNodeIds = new HashSet<>();
        private final Set<NodeId> strictUnknownExemptNodeIds = new HashSet<>();

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
                if (internalSymbol.emptyVectorDerived()) {
                    emptyVectorExemptNodeIds.add(read.nodeId());
                }
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

        private boolean ensureInternal(
                String name,
                ExpressionType assignmentType,
                com.runestone.expeval_mk3.internal.source.SourceSpan sourceSpan,
                boolean emptyVectorDerived,
                List<ExpressionDiagnostic> diagnostics) {
            InternalSymbolState existing = internalSymbolsByName.get(name);
            if (existing != null) {
                existing.updateType(assignmentType, sourceSpan, emptyVectorDerived, diagnostics);
                return false;
            }
            internalSymbolsByName.put(name, new InternalSymbolState(
                    name,
                    internalSymbolsByName.size(),
                    assignmentType,
                    emptyVectorDerived));
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

        private void resolveFunctionBindings(
                SemanticSymbolFlow symbolFlow,
                ExpressionEnvironment environment,
                List<ExpressionDiagnostic> diagnostics,
                FunctionBindingPass pass) {
            if (pass.reportsDiagnostics()) {
                functionBindings.clear();
                functionArgumentUnknownNodeIds.clear();
                strictUnknownExemptNodeIds.clear();
            }
            for (FunctionCallSource functionCall : symbolFlow.functionCalls()) {
                resolveFunctionBinding(functionCall, environment, diagnostics, pass);
            }
        }

        private void resolveFunctionBinding(
                FunctionCallSource functionCall,
                ExpressionEnvironment environment,
                List<ExpressionDiagnostic> diagnostics,
                FunctionBindingPass pass) {
            List<ExpressionType> argumentTypes = functionCall.argumentNodeIds().stream()
                    .map(this::effectiveType)
                    .toList();
            FunctionResolution resolution = environment.functionCatalog().resolveSemantic(functionCall.name(), argumentTypes);
            switch (resolution.kind()) {
                case EXACT_MATCH, UNKNOWN_ARGUMENT_MATCH -> bindResolvedFunction(
                        functionCall,
                        resolution.descriptor().orElseThrow(),
                        argumentTypes,
                        environment,
                        diagnostics,
                        pass);
                case AMBIGUOUS -> {
                    if (pass.reportsDiagnostics()) {
                        diagnostics.add(ExpressionDiagnostic.error(
                                DiagnosticCategory.SEMANTIC,
                                DiagnosticCode.SEMANTIC_AMBIGUOUS_FUNCTION_CALL,
                                "Function call is ambiguous for unknown arguments; use a declared type or an explicit assertion function: "
                                        + functionCall.name(),
                                functionCall.sourceSpan()));
                    }
                }
                case BOUNDARY_COERCION_MATCH, NO_MATCH -> {
                    if (pass.reportsDiagnostics()) {
                        diagnostics.add(ExpressionDiagnostic.error(
                                DiagnosticCategory.SEMANTIC,
                                DiagnosticCode.SEMANTIC_FUNCTION_ARGUMENT_TYPE_MISMATCH,
                                "No semantic function signature matches " + functionCall.name()
                                        + '(' + displayTypes(argumentTypes) + ')',
                                functionCall.sourceSpan()));
                    }
                }
            }
        }

        private void bindResolvedFunction(
                FunctionCallSource functionCall,
                FunctionDescriptor descriptor,
                List<ExpressionType> argumentTypes,
                ExpressionEnvironment environment,
                List<ExpressionDiagnostic> diagnostics,
                FunctionBindingPass pass) {
            ExpressionType currentType = effectiveType(functionCall.nodeId());
            if (pass.reportsDiagnostics() && currentType != UnknownType.INSTANCE && !currentType.equals(descriptor.returnType())) {
                diagnostics.add(ExpressionDiagnostic.error(
                        DiagnosticCategory.SEMANTIC,
                        DiagnosticCode.SEMANTIC_TYPE_RESTRICTION_CONFLICT,
                        "Function result type conflicts with surrounding expression constraints: "
                                + displayType(descriptor.returnType()) + " and " + displayType(currentType),
                        functionCall.sourceSpan()));
            }
            resolvedTypes.put(functionCall.nodeId(), descriptor.returnType());
            ArrayList<NodeId> unknownArgumentNodeIds = new ArrayList<>();
            ResolvedFunctionBinding.UnknownArgumentHandling unknownArgumentHandling =
                    ResolvedFunctionBinding.UnknownArgumentHandling.NONE;
            for (int index = 0; index < argumentTypes.size(); index++) {
                if (argumentTypes.get(index) != UnknownType.INSTANCE) {
                    continue;
                }
                NodeId argumentNodeId = functionCall.argumentNodeIds().get(index);
                unknownArgumentNodeIds.add(argumentNodeId);
                if (pass.reportsDiagnostics()) {
                    functionArgumentUnknownNodeIds.add(argumentNodeId);
                }
                if (descriptor.parameterTypes().get(index) == UnknownType.INSTANCE || isAssertionFunction(descriptor)) {
                    continue;
                }
                unknownArgumentHandling = ResolvedFunctionBinding.UnknownArgumentHandling.RESIDUAL_CHECK;
                if (pass.reportsDiagnostics()) {
                    addResidualOrStrictDiagnostic(
                            environment,
                            functionCall.sourceSpan(),
                            "Function argument type remains unknown for " + functionCall.name(),
                            diagnostics);
                }
            }
            functionBindings.put(functionCall.nodeId(), new ResolvedFunctionBinding(
                    descriptor,
                    unknownArgumentHandling,
                    unknownArgumentNodeIds));
            if (pass.reportsDiagnostics() && isAssertionFunction(descriptor)) {
                strictUnknownExemptNodeIds.add(functionCall.nodeId());
            }
        }

        private static boolean isAssertionFunction(FunctionDescriptor descriptor) {
            return ASSERTION_FUNCTION_NAMES.contains(descriptor.languageName())
                    && descriptor.parameterTypes().size() == 1
                    && descriptor.parameterTypes().getFirst() == UnknownType.INSTANCE;
        }

        private static String displayTypes(List<ExpressionType> types) {
            if (types.isEmpty()) {
                return "";
            }
            StringBuilder result = new StringBuilder();
            for (int index = 0; index < types.size(); index++) {
                if (index > 0) {
                    result.append(", ");
                }
                result.append(displayType(types.get(index)));
            }
            return result.toString();
        }

        private void resolveTypeRestrictions(
                SemanticSymbolFlow symbolFlow,
                ExpressionEnvironment environment,
                List<ExpressionDiagnostic> diagnostics) {
            TypeRestrictionFlow typeRestrictions = symbolFlow.typeRestrictions();
            for (ConcreteTypeRestriction restriction : typeRestrictions.concreteRestrictions()) {
                constrainNode(restriction.nodeId(), restriction.requiredType(), restriction.sourceSpan(), diagnostics);
            }
            for (SameTypeRestriction restriction : typeRestrictions.sameTypeRestrictions()) {
                resolveSameTypeRestriction(restriction, environment, diagnostics);
            }
            for (VectorElementTypeRestriction restriction : typeRestrictions.vectorElementTypeRestrictions()) {
                resolveVectorElementTypeRestriction(restriction, environment, diagnostics);
            }
            for (MembershipTypeRestriction restriction : typeRestrictions.membershipTypeRestrictions()) {
                resolveMembershipTypeRestriction(restriction, environment, typeRestrictions, diagnostics);
            }
            propagateTargetTypesToAssignmentExpressions(symbolFlow.assignments());
            for (TypeJoinRestriction restriction : typeRestrictions.joinRestrictions()) {
                resolveJoinRestriction(restriction, environment, diagnostics);
            }
            applyAssignmentInferredTypes(symbolFlow.assignments(), diagnostics);
            for (NumericConstantRestriction restriction : typeRestrictions.numericConstantRestrictions()) {
                diagnostics.add(ExpressionDiagnostic.error(
                        DiagnosticCategory.SEMANTIC,
                        DiagnosticCode.SEMANTIC_INVALID_NUMERIC_CONSTANT,
                        "Numeric operator has an invalid constant operand: " + restriction.kind(),
                        restriction.sourceSpan()));
            }
        }

        private void constrainNode(
                NodeId nodeId,
                ExpressionType requiredType,
                com.runestone.expeval_mk3.internal.source.SourceSpan sourceSpan,
                List<ExpressionDiagnostic> diagnostics) {
            ExpressionType currentType = effectiveType(nodeId);
            if (currentType == UnknownType.INSTANCE) {
                bindInferredType(nodeId, requiredType);
                return;
            }
            if (currentType.equals(requiredType)) {
                return;
            }
            diagnostics.add(ExpressionDiagnostic.error(
                    DiagnosticCategory.SEMANTIC,
                    DiagnosticCode.SEMANTIC_OPERATOR_TYPE_MISMATCH,
                    "Operator requires " + displayType(requiredType)
                            + " but operand is " + displayType(currentType),
                    sourceSpan));
        }

        private void resolveSameTypeRestriction(
                SameTypeRestriction restriction,
                ExpressionEnvironment environment,
                List<ExpressionDiagnostic> diagnostics) {
            ExpressionType concreteType = null;
            boolean hasUnknown = false;
            for (NodeId nodeId : restriction.nodeIds()) {
                ExpressionType type = effectiveType(nodeId);
                if (type == UnknownType.INSTANCE) {
                    hasUnknown = true;
                    continue;
                }
                if (type == NullType.INSTANCE && restriction.mode() == TypeCompatibilityMode.SAME_TYPE) {
                    continue;
                }
                if (restriction.mode() == TypeCompatibilityMode.SAME_ORDERABLE_TYPE && !isOrderable(type)) {
                    diagnostics.add(ExpressionDiagnostic.error(
                            DiagnosticCategory.SEMANTIC,
                            DiagnosticCode.SEMANTIC_OPERATOR_TYPE_MISMATCH,
                            "Operator requires orderable operands but operand is " + displayType(type),
                            restriction.sourceSpan()));
                    return;
                }
                if (concreteType == null) {
                    concreteType = type;
                    continue;
                }
                if (!concreteType.equals(type)) {
                    diagnostics.add(ExpressionDiagnostic.error(
                            DiagnosticCategory.SEMANTIC,
                            DiagnosticCode.SEMANTIC_TYPE_RESTRICTION_CONFLICT,
                            "Type restriction conflict between " + displayType(concreteType)
                                    + " and " + displayType(type),
                            restriction.sourceSpan()));
                    return;
                }
            }
            if (concreteType != null) {
                for (NodeId nodeId : restriction.nodeIds()) {
                    if (effectiveType(nodeId) == UnknownType.INSTANCE) {
                        bindInferredType(nodeId, concreteType);
                    }
                }
                return;
            }
            if (hasUnknown) {
                addResidualOrStrictDiagnostic(
                        environment,
                        restriction.sourceSpan(),
                        "Operator compatibility depends on unknown operand types",
                        diagnostics);
            }
        }

        private void resolveVectorElementTypeRestriction(
                VectorElementTypeRestriction restriction,
                ExpressionEnvironment environment,
                List<ExpressionDiagnostic> diagnostics) {
            if (restriction.elementNodeIds().isEmpty()) {
                resolvedTypes.put(restriction.vectorNodeId(), new VectorType(UnknownType.INSTANCE));
                return;
            }
            ExpressionType elementType = resolveCommonElementType(
                    restriction.elementNodeIds(),
                    restriction.sourceSpan(),
                    environment,
                    diagnostics);
            resolvedTypes.put(restriction.vectorNodeId(), new VectorType(elementType));
        }

        private void resolveMembershipTypeRestriction(
                MembershipTypeRestriction restriction,
                ExpressionEnvironment environment,
                TypeRestrictionFlow typeRestrictions,
                List<ExpressionDiagnostic> diagnostics) {
            ExpressionType candidatesType = effectiveType(restriction.candidatesNodeId());
            if (candidatesType == UnknownType.INSTANCE) {
                addResidualOrStrictDiagnostic(
                        environment,
                        restriction.sourceSpan(),
                        "Membership candidates type is unknown",
                        diagnostics);
                return;
            }
            ExpressionType elementType = switch (candidatesType) {
                case VectorType vector -> vector.elementType();
                case CollectionType collection -> collection.elementType();
                default -> null;
            };
            if (elementType == null) {
                diagnostics.add(ExpressionDiagnostic.error(
                        DiagnosticCategory.SEMANTIC,
                        DiagnosticCode.SEMANTIC_OPERATOR_TYPE_MISMATCH,
                        "Membership candidates must be a vector or collection",
                        restriction.sourceSpan()));
                return;
            }
            if (elementType == UnknownType.INSTANCE) {
                if (!typeRestrictions.emptyVectorNodes().contains(restriction.candidatesNodeId())) {
                    addResidualOrStrictDiagnostic(
                            environment,
                            restriction.sourceSpan(),
                            "Membership candidate element type is unknown",
                            diagnostics);
                }
                return;
            }
            ExpressionType valueType = effectiveType(restriction.valueNodeId());
            if (valueType == UnknownType.INSTANCE) {
                bindInferredType(restriction.valueNodeId(), elementType);
                return;
            }
            if (valueType != NullType.INSTANCE && !valueType.equals(elementType)) {
                diagnostics.add(ExpressionDiagnostic.error(
                        DiagnosticCategory.SEMANTIC,
                        DiagnosticCode.SEMANTIC_OPERATOR_TYPE_MISMATCH,
                        "Membership value type does not match candidate element type",
                        restriction.sourceSpan()));
            }
        }

        private ExpressionType resolveCommonElementType(
                List<NodeId> elementNodeIds,
                com.runestone.expeval_mk3.internal.source.SourceSpan sourceSpan,
                ExpressionEnvironment environment,
                List<ExpressionDiagnostic> diagnostics) {
            ExpressionType commonType = UnknownType.INSTANCE;
            boolean hasUnknown = false;
            for (NodeId elementNodeId : elementNodeIds) {
                ExpressionType elementType = effectiveType(elementNodeId);
                if (elementType == UnknownType.INSTANCE) {
                    hasUnknown = true;
                    continue;
                }
                if (commonType == UnknownType.INSTANCE || commonType == NullType.INSTANCE) {
                    commonType = elementType;
                    continue;
                }
                if (elementType != NullType.INSTANCE && !commonType.equals(elementType)) {
                    diagnostics.add(ExpressionDiagnostic.error(
                            DiagnosticCategory.SEMANTIC,
                            DiagnosticCode.SEMANTIC_TYPE_RESTRICTION_CONFLICT,
                            "Vector literal elements do not share a common type",
                            sourceSpan));
                    return UnknownType.INSTANCE;
                }
            }
            if (hasUnknown) {
                if (commonType != UnknownType.INSTANCE && commonType != NullType.INSTANCE) {
                    for (NodeId elementNodeId : elementNodeIds) {
                        if (effectiveType(elementNodeId) == UnknownType.INSTANCE) {
                            bindInferredType(elementNodeId, commonType);
                        }
                    }
                    return commonType;
                }
                addResidualOrStrictDiagnostic(
                        environment,
                        sourceSpan,
                        "Vector literal element type depends on unknown values",
                        diagnostics);
                return UnknownType.INSTANCE;
            }
            return commonType;
        }

        private void resolveJoinRestriction(
                TypeJoinRestriction restriction,
                ExpressionEnvironment environment,
                List<ExpressionDiagnostic> diagnostics) {
            ExpressionType joinedType = effectiveType(restriction.resultNodeId());
            if (joinedType == UnknownType.INSTANCE) {
                joinedType = null;
            }
            boolean hasUnknown = false;
            for (NodeId branchNodeId : restriction.branchNodeIds()) {
                ExpressionType branchType = effectiveType(branchNodeId);
                if (branchType == UnknownType.INSTANCE) {
                    hasUnknown = true;
                    continue;
                }
                if (branchType == NullType.INSTANCE) {
                    if (joinedType == null) {
                        joinedType = NullType.INSTANCE;
                    }
                    continue;
                }
                if (joinedType == null || joinedType == NullType.INSTANCE) {
                    joinedType = branchType;
                    continue;
                }
                if (!joinedType.equals(branchType)) {
                    diagnostics.add(ExpressionDiagnostic.error(
                            DiagnosticCategory.SEMANTIC,
                            DiagnosticCode.SEMANTIC_TYPE_RESTRICTION_CONFLICT,
                            "Expression branches do not join: " + displayType(joinedType)
                                    + " and " + displayType(branchType),
                            restriction.sourceSpan()));
                    return;
                }
            }
            if (hasUnknown && joinedType != null && joinedType != NullType.INSTANCE) {
                for (NodeId branchNodeId : restriction.branchNodeIds()) {
                    if (effectiveType(branchNodeId) == UnknownType.INSTANCE) {
                        bindInferredType(branchNodeId, joinedType);
                    }
                }
                resolvedTypes.put(restriction.resultNodeId(), joinedType);
                return;
            }
            if (!hasUnknown) {
                if (joinedType != null && joinedType != NullType.INSTANCE) {
                    resolvedTypes.put(restriction.resultNodeId(), joinedType);
                    return;
                }
                if (joinedType == NullType.INSTANCE) {
                    resolvedTypes.put(restriction.resultNodeId(), NullType.INSTANCE);
                    return;
                }
            }
            resolvedTypes.put(restriction.resultNodeId(), UnknownType.INSTANCE);
            addResidualOrStrictDiagnostic(
                    environment,
                    restriction.sourceSpan(),
                    "Expression branch type join depends on unknown types",
                    diagnostics);
        }

        private void propagateTargetTypesToAssignmentExpressions(List<AssignmentSymbolFlow> assignments) {
            for (AssignmentSymbolFlow assignment : assignments) {
                Optional<SymbolReadSource> rootRead = assignment.expressionRootRead();
                if (assignment.expressionType().isPresent()) {
                    continue;
                }
                for (AssignedSymbolSource target : assignment.targetSymbols()) {
                    InternalSymbolState internalSymbol = internalSymbolsByName.get(target.name());
                    if (internalSymbol != null && internalSymbol.type() != UnknownType.INSTANCE) {
                        rootRead.ifPresent(read -> {
                            if (effectiveType(read.nodeId()) == UnknownType.INSTANCE) {
                                bindInferredType(read.nodeId(), internalSymbol.type());
                            }
                        });
                        resolvedTypes.put(assignment.expressionNodeId(), internalSymbol.type());
                        break;
                    }
                }
            }
        }

        private void applyAssignmentInferredTypes(
                List<AssignmentSymbolFlow> assignments,
                List<ExpressionDiagnostic> diagnostics) {
            for (AssignmentSymbolFlow assignment : assignments) {
                if (assignment.expressionType().isPresent()) {
                    continue;
                }
                if (assignment.expressionRootRead().isPresent()) {
                    continue;
                }
                ExpressionType expressionType = resolvedTypes.get(assignment.expressionNodeId());
                if (expressionType == null || expressionType == UnknownType.INSTANCE) {
                    continue;
                }
                for (AssignedSymbolSource target : assignment.targetSymbols()) {
                    InternalSymbolState internalSymbol = internalSymbolsByName.get(target.name());
                    if (internalSymbol == null) {
                        continue;
                    }
                    if (internalSymbol.type() == UnknownType.INSTANCE) {
                        internalSymbol.forceType(expressionType);
                    } else {
                        internalSymbol.updateType(expressionType, assignment.sourceSpan(), false, diagnostics);
                    }
                    updateBoundNodes(new InternalBinding(target.name()), internalSymbol.type());
                }
            }
        }

        private void propagateStrictUnknownExemptions(List<AssignmentSymbolFlow> assignments) {
            for (AssignmentSymbolFlow assignment : assignments) {
                if (!strictUnknownExemptNodeIds.contains(assignment.expressionNodeId())) {
                    continue;
                }
                for (AssignedSymbolSource target : assignment.targetSymbols()) {
                    strictUnknownExemptNodeIds.add(target.nodeId());
                    markBoundNodesStrictUnknownExempt(new InternalBinding(target.name()));
                }
            }
        }

        private void addResidualOrStrictDiagnostic(
                ExpressionEnvironment environment,
                com.runestone.expeval_mk3.internal.source.SourceSpan sourceSpan,
                String description,
                List<ExpressionDiagnostic> diagnostics) {
            if (environment.strictMode()) {
                diagnostics.add(ExpressionDiagnostic.error(
                        DiagnosticCategory.SEMANTIC,
                        DiagnosticCode.SEMANTIC_STRICT_UNKNOWN_TYPE_RESTRICTION,
                        description,
                        sourceSpan));
                return;
            }
            residualTypeChecks.add(new ResidualTypeCheck(
                    DiagnosticCode.SEMANTIC_STRICT_UNKNOWN_TYPE_RESTRICTION,
                    sourceSpan,
                    description));
        }

        private void rejectStrictUnknowns(TypeRestrictionFlow typeRestrictions, List<ExpressionDiagnostic> diagnostics) {
            for (Map.Entry<NodeId, ExpressionType> entry : resolvedTypes.entrySet()) {
                if (typeRestrictions.emptyVectorNodes().contains(entry.getKey())) {
                    continue;
                }
                if (emptyVectorExemptNodeIds.contains(entry.getKey())) {
                    continue;
                }
                if (functionArgumentUnknownNodeIds.contains(entry.getKey())) {
                    continue;
                }
                if (strictUnknownExemptNodeIds.contains(entry.getKey())) {
                    continue;
                }
                if (hasResidualUnknown(entry.getValue())) {
                    diagnostics.add(ExpressionDiagnostic.error(
                            DiagnosticCategory.SEMANTIC,
                            DiagnosticCode.SEMANTIC_STRICT_UNKNOWN_TYPE_RESTRICTION,
                            "Strict mode rejects unresolved expression type",
                            typeRestrictions.sourceSpans().getOrDefault(
                                    entry.getKey(),
                                    new com.runestone.expeval_mk3.internal.source.SourceSpan(0, 0, 1, 1))));
                }
            }
        }

        private ExpressionType effectiveType(NodeId nodeId) {
            SymbolBinding binding = symbolBindings.get(nodeId);
            if (binding instanceof ExternalBinding external) {
                ExpressionType inferredType = inferredExternalTypesByName.get(external.name());
                if (inferredType != null) {
                    return inferredType;
                }
            }
            return resolvedTypes.getOrDefault(nodeId, UnknownType.INSTANCE);
        }

        private void bindInferredType(NodeId nodeId, ExpressionType type) {
            SymbolBinding binding = symbolBindings.get(nodeId);
            if (binding instanceof InternalBinding internal) {
                internalSymbolsByName.get(internal.name()).forceType(type);
                updateBoundNodes(internal, type);
                return;
            }
            if (binding instanceof ExternalBinding external) {
                inferredExternalTypesByName.put(external.name(), type);
                updateBoundNodes(external, type);
                return;
            }
            resolvedTypes.put(nodeId, type);
        }

        private void updateBoundNodes(SymbolBinding binding, ExpressionType type) {
            for (Map.Entry<NodeId, SymbolBinding> entry : symbolBindings.entrySet()) {
                if (entry.getValue().equals(binding)) {
                    resolvedTypes.put(entry.getKey(), type);
                }
            }
        }

        private void markBoundNodesStrictUnknownExempt(SymbolBinding binding) {
            for (Map.Entry<NodeId, SymbolBinding> entry : symbolBindings.entrySet()) {
                if (entry.getValue().equals(binding)) {
                    strictUnknownExemptNodeIds.add(entry.getKey());
                }
            }
        }

        private boolean hasResidualUnknown(ExpressionType type) {
            return switch (type) {
                case UnknownType ignored -> true;
                case VectorType vector -> hasResidualUnknown(vector.elementType());
                case CollectionType collection -> hasResidualUnknown(collection.elementType());
                case com.runestone.expeval_mk3.api.MapType map -> hasResidualUnknown(map.valueType());
                default -> false;
            };
        }

        private static boolean isOrderable(ExpressionType type) {
            return type == ScalarType.NUMBER
                    || type == ScalarType.STRING
                    || type == ScalarType.DATE
                    || type == ScalarType.TIME
                    || type == ScalarType.DATETIME;
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
                ExpressionType externalType = inferredExternalTypesByName.getOrDefault(
                        externalSymbol.name(),
                        externalSymbol.type());
                externalSymbols.add(new NamedResolvedSymbol(
                        externalSymbol.name(),
                        ResolvedSymbolKind.EXTERNAL,
                        externalType,
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
                    resolvedTypes.putIfAbsent(entry.getKey(), symbol.type());
                }
            }

            List<FrameSlot> slots = new ArrayList<>(
                    internalSymbols.size() + externalSymbols.size() + currentItemSymbols.size());
            addSlots(slots, FrameSlotKind.INTERNAL_SYMBOL, internalSymbols);
            addSlots(slots, FrameSlotKind.EXTERNAL_SYMBOL, externalSymbols);
            addSlots(slots, FrameSlotKind.CURRENT_ITEM, currentItemSymbols);
            return new SemanticModelSymbols(
                    resolvedTypes,
                    numericKinds(symbolFlow.typeRestrictions().numericSources(), environment),
                    residualTypeChecks,
                    functionBindings,
                    symbolByNodeId,
                    new ResolvedSymbolSets(internalSymbols, externalSymbols, currentItemSymbols),
                    new FrameLayout(slots));
        }

        private Map<NodeId, NumericKind> numericKinds(List<NumericSource> numericSources, ExpressionEnvironment environment) {
            Map<NodeId, NumericKind> kinds = new LinkedHashMap<>();
            if (environment.numericMode() == NumericMode.DECIMAL) {
                for (Map.Entry<NodeId, ExpressionType> entry : resolvedTypes.entrySet()) {
                    if (entry.getValue() == ScalarType.NUMBER) {
                        kinds.put(entry.getKey(), NumericKind.DECIMAL);
                    }
                }
                return kinds;
            }
            for (NumericSource source : numericSources) {
                if (resolvedTypes.get(source.nodeId()) != ScalarType.NUMBER) {
                    continue;
                }
                NumericKind kind = switch (source.kind()) {
                    case INTEGRAL -> NumericKind.INTEGRAL;
                    case FLOATING, OPERATION -> NumericKind.FLOATING;
                };
                kinds.put(source.nodeId(), kind);
            }
            for (Map.Entry<NodeId, ExpressionType> entry : resolvedTypes.entrySet()) {
                if (entry.getValue() == ScalarType.NUMBER) {
                    kinds.putIfAbsent(entry.getKey(), NumericKind.FLOATING);
                }
            }
            return kinds;
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
        private boolean emptyVectorDerived;

        private InternalSymbolState(String name, int slot, ExpressionType type, boolean emptyVectorDerived) {
            this.name = Objects.requireNonNull(name, "name");
            this.slot = slot;
            this.type = Objects.requireNonNull(type, "type");
            this.emptyVectorDerived = emptyVectorDerived;
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

        private boolean emptyVectorDerived() {
            return emptyVectorDerived;
        }

        private void updateType(
                ExpressionType nextType,
                com.runestone.expeval_mk3.internal.source.SourceSpan sourceSpan,
                boolean nextEmptyVectorDerived,
                List<ExpressionDiagnostic> diagnostics) {
            ExpressionType unifiedType = unify(type, nextType);
            if (unifiedType == null) {
                diagnostics.add(ExpressionDiagnostic.error(
                        DiagnosticCategory.SEMANTIC,
                        DiagnosticCode.SEMANTIC_TYPE_RESTRICTION_CONFLICT,
                        "Symbol type restriction conflict for " + name + ": "
                                + displayType(type) + " and " + displayType(nextType),
                        sourceSpan));
                return;
            }
            type = unifiedType;
            emptyVectorDerived = nextEmptyVectorDerived;
        }

        private void forceType(ExpressionType nextType) {
            type = Objects.requireNonNull(nextType, "nextType");
            emptyVectorDerived = false;
        }

        private static ExpressionType unify(ExpressionType currentType, ExpressionType nextType) {
            Objects.requireNonNull(currentType, "currentType");
            Objects.requireNonNull(nextType, "nextType");
            if (currentType == UnknownType.INSTANCE && nextType == UnknownType.INSTANCE) {
                return UnknownType.INSTANCE;
            }
            if (currentType == UnknownType.INSTANCE) {
                return nextType;
            }
            if (nextType == UnknownType.INSTANCE) {
                return currentType;
            }
            if (currentType.equals(nextType)) {
                return currentType;
            }
            return null;
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
