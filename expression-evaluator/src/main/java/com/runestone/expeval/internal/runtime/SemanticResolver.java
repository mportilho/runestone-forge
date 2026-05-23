package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.api.IssueCode;
import com.runestone.expeval.internal.ast.AssignmentNode;
import com.runestone.expeval.internal.ast.BinaryOperationNode;
import com.runestone.expeval.internal.ast.ConditionalNode;
import com.runestone.expeval.internal.ast.DestructuringAssignmentNode;
import com.runestone.expeval.internal.ast.ExpressionFileNode;
import com.runestone.expeval.internal.ast.ExpressionNode;
import com.runestone.expeval.internal.ast.FunctionCallNode;
import com.runestone.expeval.internal.ast.IdentifierNode;
import com.runestone.expeval.internal.ast.LiteralNode;
import com.runestone.expeval.internal.ast.NodeId;
import com.runestone.expeval.internal.ast.PostfixOperationNode;
import com.runestone.expeval.internal.ast.PropertyChainNode;
import com.runestone.expeval.internal.ast.SimpleAssignmentNode;
import com.runestone.expeval.internal.ast.SourceSpan;
import com.runestone.expeval.internal.ast.TernaryOperationNode;
import com.runestone.expeval.internal.ast.UnaryOperationNode;
import com.runestone.expeval.internal.ast.VectorLiteralNode;
import com.runestone.expeval.types.ResolvedType;
import com.runestone.expeval.types.ResolvedTypes;
import com.runestone.expeval.types.ScalarType;
import com.runestone.expeval.types.UnknownType;
import com.runestone.expeval.types.VectorType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class SemanticResolver {

    public SemanticModel resolve(ExpressionFileNode file, ResolutionContext context) {
        Objects.requireNonNull(file, "file must not be null");
        Objects.requireNonNull(context, "context must not be null");
        ResolutionSession session = new ResolutionSession(context);
        session.collectInternalSymbols(file.assignments());
        for (AssignmentNode assignment : file.assignments()) {
            session.resolveAssignment(assignment);
        }
        if (file.resultExpression() != null) {
            ResolvedType resultType = session.resolveExpression(file.resultExpression());
            session.validateResultType(file.resultExpression(), resultType);
            session.resolvedTypes.put(file.nodeId(), resultType);
        } else {
            session.resolvedTypes.put(file.nodeId(), UnknownType.INSTANCE);
        }
        return new SemanticModel(
            file,
            session.resolvedTypes,
            session.symbolByNodeId,
            session.identifierUsages,
            session.assignmentUsages,
            session.externalSymbolsByName,
            session.internalSymbolsByName,
            session.functionBindings,
            session.issues
        );
    }

    private static final class ResolutionSession {

        private final ResolutionContext context;
        private final Map<NodeId, ResolvedType> resolvedTypes = new LinkedHashMap<>();
        private final Map<NodeId, SymbolRef> symbolByNodeId = new LinkedHashMap<>();
        private final Map<SymbolRef, List<IdentifierNode>> identifierUsages = new LinkedHashMap<>();
        private final Map<SymbolRef, List<AssignmentNode>> assignmentUsages = new LinkedHashMap<>();
        private final Map<String, SymbolRef> externalSymbolsByName = new LinkedHashMap<>();
        private final Map<String, SymbolRef> internalSymbolsByName = new LinkedHashMap<>();
        private final Map<NodeId, ResolvedFunctionBinding> functionBindings = new LinkedHashMap<>();
        private final Map<SymbolRef, ResolvedType> internalTypes = new LinkedHashMap<>();
        private final List<SemanticIssue> issues = new ArrayList<>();
        private final OperatorTypeChecker operatorTypeChecker;
        private final ResultTypeValidator resultTypeValidator;
        private final SemanticSymbolResolver symbolResolver;
        private final FunctionOverloadResolver functionOverloadResolver;
        private final NavigationTypeResolver navigationTypeResolver;

        private ResolutionSession(ResolutionContext context) {
            this.context = context;
            this.operatorTypeChecker = new OperatorTypeChecker(this::error);
            this.resultTypeValidator = new ResultTypeValidator(this::error);
            this.symbolResolver = new SemanticSymbolResolver(
                    context.externalSymbolCatalog(),
                    symbolByNodeId,
                    identifierUsages,
                    assignmentUsages,
                    externalSymbolsByName,
                    internalSymbolsByName,
                    internalTypes,
                    this::error);
            this.functionOverloadResolver = new FunctionOverloadResolver(
                    context.functionCatalog(),
                    functionBindings,
                    this::error);
            this.navigationTypeResolver = new NavigationTypeResolver(
                    symbolResolver,
                    context.typeHintCatalog(),
                    context.functionCatalog(),
                    functionBindings,
                    this::resolveExpression,
                    this::error);
        }

        private void collectInternalSymbols(List<AssignmentNode> assignments) {
            symbolResolver.collectInternalSymbols(assignments);
        }

        private void resolveAssignment(AssignmentNode assignment) {
            switch (assignment) {
                case SimpleAssignmentNode simpleAssignment -> {
                    ResolvedType valueType = resolveExpression(simpleAssignment.value());
                    resolvedTypes.put(simpleAssignment.nodeId(), valueType);
                    symbolResolver.resolveSimpleAssignment(simpleAssignment, valueType);
                }
                case DestructuringAssignmentNode destructuringAssignment -> {
                    ResolvedType valueType = resolveExpression(destructuringAssignment.value());
                    resolvedTypes.put(destructuringAssignment.nodeId(),
                            symbolResolver.resolveDestructuringAssignment(destructuringAssignment, valueType));
                }
            }
        }

        private ResolvedType resolveExpression(ExpressionNode node) {
            ResolvedType cached = resolvedTypes.get(node.nodeId());
            if (cached != null) {
                return cached;
            }
            ResolvedType resolvedType = switch (node) {
                case LiteralNode literalNode -> LiteralTypeInferencer.infer(literalNode);
                case IdentifierNode identifierNode -> resolveIdentifier(identifierNode);
                case PropertyChainNode propertyChainNode -> resolvePropertyChain(propertyChainNode);
                case FunctionCallNode functionCallNode -> resolveFunctionCall(functionCallNode);
                case ConditionalNode conditionalNode -> resolveConditional(conditionalNode);
                case UnaryOperationNode unaryOperationNode -> resolveUnary(unaryOperationNode);
                case BinaryOperationNode binaryOperationNode -> resolveBinary(binaryOperationNode);
                case TernaryOperationNode ternaryOperationNode -> resolveTernary(ternaryOperationNode);
                case PostfixOperationNode postfixOperationNode -> resolvePostfix(postfixOperationNode);
                case VectorLiteralNode vectorLiteralNode -> resolveVector(vectorLiteralNode);
            };
            resolvedTypes.put(node.nodeId(), resolvedType);
            return resolvedType;
        }

        private ResolvedType resolveIdentifier(IdentifierNode node) {
            return symbolResolver.resolveIdentifier(node, navigationTypeResolver.currentFilterElementType());
        }

        private ResolvedType resolvePropertyChain(PropertyChainNode node) {
            return navigationTypeResolver.resolve(node);
        }

        private ResolvedType resolveFunctionCall(FunctionCallNode node) {
            List<ResolvedType> argumentTypes = node.arguments().stream()
                .map(this::resolveExpression)
                .toList();
            return functionOverloadResolver.resolve(node, argumentTypes);
        }

        private ResolvedType resolveConditional(ConditionalNode node) {
            node.conditions().forEach(condition -> operatorTypeChecker.expectType(
                    resolveExpression(condition),
                    ScalarType.BOOLEAN,
                    "condition",
                    condition.sourceSpan()));
            ResolvedType resultType = UnknownType.INSTANCE;
            for (ExpressionNode result : node.results()) {
                resultType = ResolvedTypes.merge(resultType, resolveExpression(result));
            }
            return ResolvedTypes.merge(resultType, resolveExpression(node.elseExpression()));
        }

        private ResolvedType resolveUnary(UnaryOperationNode node) {
            return operatorTypeChecker.resolveUnary(node, resolveExpression(node.operand()));
        }

        private ResolvedType resolveBinary(BinaryOperationNode node) {
            ResolvedType leftType = resolveExpression(node.left());
            ResolvedType rightType = resolveExpression(node.right());
            return operatorTypeChecker.resolveBinary(node, leftType, rightType);
        }

        private ResolvedType resolveTernary(TernaryOperationNode node) {
            ResolvedType valueType = resolveExpression(node.first());
            ResolvedType lowerType = resolveExpression(node.second());
            ResolvedType upperType = resolveExpression(node.third());
            return operatorTypeChecker.resolveTernary(node, valueType, lowerType, upperType);
        }

        private ResolvedType resolvePostfix(PostfixOperationNode node) {
            return operatorTypeChecker.resolvePostfix(node, resolveExpression(node.operand()));
        }

        private ResolvedType resolveVector(VectorLiteralNode node) {
            node.elements().forEach(this::resolveExpression);
            return VectorType.INSTANCE;
        }

        private void error(IssueCode code, String message, SourceSpan sourceSpan) {
            issues.add(new SemanticIssue(code, SemanticIssueSeverity.ERROR, message, sourceSpan));
        }

        private void validateResultType(ExpressionNode expression, ResolvedType resultType) {
            resultTypeValidator.validate(context.resultType(), expression, resultType);
        }
    }
}
