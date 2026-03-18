package com.runestone.expeval2.internal.runtime;

import com.runestone.expeval2.internal.ast.AssignmentNode;
import com.runestone.expeval2.internal.ast.BinaryOperationNode;
import com.runestone.expeval2.internal.ast.ConditionalNode;
import com.runestone.expeval2.internal.ast.DestructuringAssignmentNode;
import com.runestone.expeval2.internal.ast.ExpressionFileNode;
import com.runestone.expeval2.internal.ast.ExpressionNode;
import com.runestone.expeval2.internal.ast.FunctionCallNode;
import com.runestone.expeval2.internal.ast.IdentifierNode;
import com.runestone.expeval2.internal.ast.LiteralNode;
import com.runestone.expeval2.internal.ast.NodeId;
import com.runestone.expeval2.internal.ast.PostfixOperationNode;
import com.runestone.expeval2.internal.ast.SimpleAssignmentNode;
import com.runestone.expeval2.internal.ast.SourceSpan;
import com.runestone.expeval2.internal.ast.UnaryOperationNode;
import com.runestone.expeval2.internal.ast.VectorLiteralNode;
import com.runestone.expeval2.catalog.ExternalSymbolDescriptor;
import com.runestone.expeval2.catalog.FunctionDescriptor;
import com.runestone.expeval2.internal.grammar.ExpressionResultType;
import com.runestone.expeval2.types.ResolvedType;
import com.runestone.expeval2.types.ResolvedTypes;
import com.runestone.expeval2.types.ScalarType;
import com.runestone.expeval2.types.UnknownType;
import com.runestone.expeval2.types.VectorType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
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
        ResolvedType resultType = session.resolveExpression(file.resultExpression());
        if (context.resultType() == ExpressionResultType.MATH && resultType != UnknownType.INSTANCE && resultType != ScalarType.NUMBER) {
            session.error("RESULT_TYPE_MISMATCH", "math expressions must resolve to NUMBER", file.resultExpression().sourceSpan());
        }
        if (context.resultType() == ExpressionResultType.LOGICAL && resultType != UnknownType.INSTANCE && resultType != ScalarType.BOOLEAN) {
            session.error("RESULT_TYPE_MISMATCH", "logical expressions must resolve to BOOLEAN", file.resultExpression().sourceSpan());
        }
        session.resolvedTypes.put(file.nodeId(), resultType);
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

        private ResolutionSession(ResolutionContext context) {
            this.context = context;
        }

        private void collectInternalSymbols(List<AssignmentNode> assignments) {
            for (AssignmentNode assignment : assignments) {
                switch (assignment) {
                    case SimpleAssignmentNode simpleAssignment -> registerInternal(simpleAssignment.targetName());
                    case DestructuringAssignmentNode destructuringAssignment ->
                        destructuringAssignment.targetNames().forEach(this::registerInternal);
                }
            }
        }

        private void resolveAssignment(AssignmentNode assignment) {
            switch (assignment) {
                case SimpleAssignmentNode simpleAssignment -> {
                    SymbolRef symbolRef = internalSymbolsByName.get(simpleAssignment.targetName());
                    ResolvedType valueType = resolveExpression(simpleAssignment.value());
                    resolvedTypes.put(simpleAssignment.nodeId(), valueType);
                    symbolByNodeId.put(simpleAssignment.nodeId(), symbolRef);
                    assignmentUsages.computeIfAbsent(symbolRef, ignored -> new ArrayList<>()).add(simpleAssignment);
                    internalTypes.put(symbolRef, valueType);
                }
                case DestructuringAssignmentNode destructuringAssignment -> {
                    ResolvedType valueType = resolveExpression(destructuringAssignment.value());
                    resolvedTypes.put(destructuringAssignment.nodeId(), valueType == UnknownType.INSTANCE ? UnknownType.INSTANCE : VectorType.INSTANCE);
                    for (String targetName : destructuringAssignment.targetNames()) {
                        SymbolRef symbolRef = internalSymbolsByName.get(targetName);
                        assignmentUsages.computeIfAbsent(symbolRef, ignored -> new ArrayList<>()).add(destructuringAssignment);
                        internalTypes.putIfAbsent(symbolRef, UnknownType.INSTANCE);
                    }
                }
            }
        }

        private ResolvedType resolveExpression(ExpressionNode node) {
            ResolvedType cached = resolvedTypes.get(node.nodeId());
            if (cached != null) {
                return cached;
            }
            ResolvedType resolvedType = switch (node) {
                case LiteralNode literalNode -> inferLiteralType(literalNode);
                case IdentifierNode identifierNode -> resolveIdentifier(identifierNode);
                case FunctionCallNode functionCallNode -> resolveFunctionCall(functionCallNode);
                case ConditionalNode conditionalNode -> resolveConditional(conditionalNode);
                case UnaryOperationNode unaryOperationNode -> resolveUnary(unaryOperationNode);
                case BinaryOperationNode binaryOperationNode -> resolveBinary(binaryOperationNode);
                case PostfixOperationNode postfixOperationNode -> resolvePostfix(postfixOperationNode);
                case VectorLiteralNode vectorLiteralNode -> resolveVector(vectorLiteralNode);
            };
            resolvedTypes.put(node.nodeId(), resolvedType);
            return resolvedType;
        }

        private ResolvedType inferLiteralType(LiteralNode node) {
            String value = node.value();
            if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
                return ScalarType.BOOLEAN;
            }
            if ("currDate".equals(value) || canParseDate(value)) {
                return ScalarType.DATE;
            }
            if ("currTime".equals(value) || canParseTime(value)) {
                return ScalarType.TIME;
            }
            if ("currDateTime".equals(value) || canParseDateTime(value)) {
                return ScalarType.DATETIME;
            }
            if (value.startsWith("\"") && value.endsWith("\"")) {
                return ScalarType.STRING;
            }
            try {
                new BigDecimal(value);
                return ScalarType.NUMBER;
            } catch (NumberFormatException ignored) {
                return UnknownType.INSTANCE;
            }
        }

        private ResolvedType resolveIdentifier(IdentifierNode node) {
            SymbolRef symbolRef = internalSymbolsByName.get(node.name());
            if (symbolRef != null) {
                symbolByNodeId.put(node.nodeId(), symbolRef);
                identifierUsages.computeIfAbsent(symbolRef, ignored -> new ArrayList<>()).add(node);
                return internalTypes.getOrDefault(symbolRef, UnknownType.INSTANCE);
            }

            symbolRef = externalSymbolsByName.computeIfAbsent(node.name(), name -> new SymbolRef(name, SymbolKind.EXTERNAL));
            symbolByNodeId.put(node.nodeId(), symbolRef);
            identifierUsages.computeIfAbsent(symbolRef, ignored -> new ArrayList<>()).add(node);
            return context.externalSymbolCatalog().find(node.name())
                .map(ExternalSymbolDescriptor::declaredType)
                .orElse(UnknownType.INSTANCE);
        }

        private ResolvedType resolveFunctionCall(FunctionCallNode node) {
            List<ResolvedType> argumentTypes = node.arguments().stream()
                .map(this::resolveExpression)
                .toList();
            List<FunctionDescriptor> candidates = List.copyOf(context.functionCatalog().findCandidates(node.functionName()));
            if (candidates.isEmpty()) {
                error("UNKNOWN_FUNCTION", "unknown function '" + node.functionName() + "'", node.sourceSpan());
                return UnknownType.INSTANCE;
            }
            List<FunctionDescriptor> arityMatches = candidates.stream()
                .filter(descriptor -> descriptor.arity() == node.arguments().size())
                .toList();
            if (arityMatches.isEmpty()) {
                error("INVALID_FUNCTION_ARITY", "invalid arity for function '" + node.functionName() + "'", node.sourceSpan());
                return UnknownType.INSTANCE;
            }
            List<FunctionDescriptor> compatibleMatches = arityMatches.stream()
                .filter(descriptor -> matchesArguments(descriptor, argumentTypes))
                .toList();
            if (compatibleMatches.size() > 1) {
                error("AMBIGUOUS_FUNCTION", "ambiguous function call '" + node.functionName() + "'", node.sourceSpan());
                return UnknownType.INSTANCE;
            }
            if (compatibleMatches.isEmpty()) {
                error("INCOMPATIBLE_FUNCTION_ARGUMENTS", "incompatible arguments for function '" + node.functionName() + "'", node.sourceSpan());
                return UnknownType.INSTANCE;
            }
            FunctionDescriptor descriptor = compatibleMatches.getFirst();
            ResolvedFunctionBinding binding = new ResolvedFunctionBinding(descriptor.functionRef(), descriptor, descriptor.returnType());
            functionBindings.put(node.nodeId(), binding);
            return descriptor.returnType();
        }

        private boolean matchesArguments(FunctionDescriptor descriptor, List<ResolvedType> argumentTypes) {
            for (int index = 0; index < argumentTypes.size(); index++) {
                ResolvedType actualType = argumentTypes.get(index);
                ResolvedType expectedType = descriptor.parameterResolvedTypes().get(index);
                if (actualType != UnknownType.INSTANCE && expectedType != UnknownType.INSTANCE && !actualType.equals(expectedType)) {
                    return false;
                }
            }
            return true;
        }

        private ResolvedType resolveConditional(ConditionalNode node) {
            node.conditions().forEach(condition -> expectType(resolveExpression(condition), ScalarType.BOOLEAN, "condition", condition.sourceSpan()));
            ResolvedType resultType = UnknownType.INSTANCE;
            for (ExpressionNode result : node.results()) {
                resultType = ResolvedTypes.merge(resultType, resolveExpression(result));
            }
            return ResolvedTypes.merge(resultType, resolveExpression(node.elseExpression()));
        }

        private ResolvedType resolveUnary(UnaryOperationNode node) {
            ResolvedType operandType = resolveExpression(node.operand());
            return switch (node.operator()) {
                case NEGATE -> expectType(operandType, ScalarType.NUMBER, "unary negate", node.sourceSpan());
                case LOGICAL_NOT -> expectType(operandType, ScalarType.BOOLEAN, "logical not", node.sourceSpan());
                case SQRT, MODULUS -> expectType(operandType, ScalarType.NUMBER, "numeric unary operator", node.sourceSpan());
            };
        }

        private ResolvedType resolveBinary(BinaryOperationNode node) {
            ResolvedType leftType = resolveExpression(node.left());
            ResolvedType rightType = resolveExpression(node.right());
            return switch (node.operator()) {
                case ADD, SUBTRACT, MULTIPLY, DIVIDE, MODULO, POWER, ROOT ->
                    arithmeticType(leftType, rightType, node.sourceSpan());
                case AND, OR, XOR, XNOR, NAND, NOR -> {
                    expectType(leftType, ScalarType.BOOLEAN, "logical operator", node.left().sourceSpan());
                    yield expectType(rightType, ScalarType.BOOLEAN, "logical operator", node.right().sourceSpan());
                }
                case GREATER_THAN, GREATER_THAN_OR_EQUAL, LESS_THAN, LESS_THAN_OR_EQUAL, EQUAL, NOT_EQUAL -> {
                    if (!compatibleComparison(leftType, rightType)) {
                        error("INCOMPATIBLE_COMPARISON", "comparison uses incompatible operand types", node.sourceSpan());
                    }
                    yield ScalarType.BOOLEAN;
                }
            };
        }

        private ResolvedType resolvePostfix(PostfixOperationNode node) {
            return expectType(resolveExpression(node.operand()), ScalarType.NUMBER, "postfix operator", node.sourceSpan());
        }

        private ResolvedType resolveVector(VectorLiteralNode node) {
            node.elements().forEach(this::resolveExpression);
            return VectorType.INSTANCE;
        }

        private ResolvedType arithmeticType(ResolvedType leftType, ResolvedType rightType, SourceSpan sourceSpan) {
            expectType(leftType, ScalarType.NUMBER, "arithmetic operator", sourceSpan);
            expectType(rightType, ScalarType.NUMBER, "arithmetic operator", sourceSpan);
            return ScalarType.NUMBER;
        }

        private ResolvedType expectType(
            ResolvedType actualType,
            ScalarType expectedType,
            String operation,
            SourceSpan sourceSpan
        ) {
            if (actualType != UnknownType.INSTANCE && actualType != expectedType) {
                error("TYPE_MISMATCH", operation + " expects " + expectedType + " but found " + actualType, sourceSpan);
                return UnknownType.INSTANCE;
            }
            return expectedType;
        }

        private boolean compatibleComparison(ResolvedType leftType, ResolvedType rightType) {
            if (leftType == UnknownType.INSTANCE || rightType == UnknownType.INSTANCE) {
                return true;
            }
            return leftType.equals(rightType);
        }

        private void registerInternal(String name) {
            internalSymbolsByName.computeIfAbsent(name, ignored -> new SymbolRef(name, SymbolKind.INTERNAL));
        }

        private void error(String code, String message, SourceSpan sourceSpan) {
            issues.add(new SemanticIssue(code, SemanticIssueSeverity.ERROR, message, sourceSpan));
        }

        private boolean canParseDate(String value) {
            try {
                LocalDate.parse(value);
                return true;
            } catch (DateTimeParseException ignored) {
                return false;
            }
        }

        private boolean canParseTime(String value) {
            try {
                LocalTime.parse(value);
                return true;
            } catch (DateTimeParseException ignored) {
                return false;
            }
        }

        private boolean canParseDateTime(String value) {
            try {
                if (value.contains("+") || value.endsWith("Z")) {
                    OffsetDateTime.parse(value);
                } else {
                    LocalDateTime.parse(value);
                }
                return true;
            } catch (DateTimeParseException ignored) {
                return false;
            }
        }
    }
}
