package com.runestone.expeval_mk3.internal.semantics;

import com.runestone.expeval_mk3.api.CollectionType;
import com.runestone.expeval_mk3.api.ExpressionEnvironment;
import com.runestone.expeval_mk3.api.ExpressionType;
import com.runestone.expeval_mk3.api.ExternalSymbol;
import com.runestone.expeval_mk3.api.FunctionDescriptor;
import com.runestone.expeval_mk3.api.FunctionLookupResult;
import com.runestone.expeval_mk3.api.MapType;
import com.runestone.expeval_mk3.api.ObjectType;
import com.runestone.expeval_mk3.api.RuntimeNullability;
import com.runestone.expeval_mk3.api.ScalarType;
import com.runestone.expeval_mk3.internal.ast.AssignmentNode;
import com.runestone.expeval_mk3.internal.ast.AssignmentTargetNode;
import com.runestone.expeval_mk3.internal.ast.BetweenNode;
import com.runestone.expeval_mk3.internal.ast.BigIntegerLiteralValue;
import com.runestone.expeval_mk3.internal.ast.BinaryOperationNode;
import com.runestone.expeval_mk3.internal.ast.BinaryOperator;
import com.runestone.expeval_mk3.internal.ast.BooleanLiteralValue;
import com.runestone.expeval_mk3.internal.ast.CallArgument;
import com.runestone.expeval_mk3.internal.ast.CollectionLiteralNode;
import com.runestone.expeval_mk3.internal.ast.ConditionalBranchNode;
import com.runestone.expeval_mk3.internal.ast.ConditionalNode;
import com.runestone.expeval_mk3.internal.ast.CurrentItemNode;
import com.runestone.expeval_mk3.internal.ast.CurrentTemporalValueNode;
import com.runestone.expeval_mk3.internal.ast.DateLiteralValue;
import com.runestone.expeval_mk3.internal.ast.DecimalLiteralValue;
import com.runestone.expeval_mk3.internal.ast.DestructuringAssignmentTargetNode;
import com.runestone.expeval_mk3.internal.ast.ExpressionCallArgument;
import com.runestone.expeval_mk3.internal.ast.ExpressionFileNode;
import com.runestone.expeval_mk3.internal.ast.ExpressionNode;
import com.runestone.expeval_mk3.internal.ast.FilterNavigationLink;
import com.runestone.expeval_mk3.internal.ast.FunctionCallNode;
import com.runestone.expeval_mk3.internal.ast.GroupedExpressionNode;
import com.runestone.expeval_mk3.internal.ast.IdentifierAssignmentTargetNode;
import com.runestone.expeval_mk3.internal.ast.IdentifierNode;
import com.runestone.expeval_mk3.internal.ast.IndexSubscriptNavigationLink;
import com.runestone.expeval_mk3.internal.ast.LiteralNode;
import com.runestone.expeval_mk3.internal.ast.LiteralValue;
import com.runestone.expeval_mk3.internal.ast.LocalDateTimeLiteralValue;
import com.runestone.expeval_mk3.internal.ast.LongLiteralValue;
import com.runestone.expeval_mk3.internal.ast.MembershipNode;
import com.runestone.expeval_mk3.internal.ast.NavigationChainNode;
import com.runestone.expeval_mk3.internal.ast.NavigationLink;
import com.runestone.expeval_mk3.internal.ast.NodeId;
import com.runestone.expeval_mk3.internal.ast.NullCoalesceNode;
import com.runestone.expeval_mk3.internal.ast.OffsetDateTimeLiteralValue;
import com.runestone.expeval_mk3.internal.ast.PostfixOperationNode;
import com.runestone.expeval_mk3.internal.ast.SliceSubscriptNavigationLink;
import com.runestone.expeval_mk3.internal.ast.StringLiteralValue;
import com.runestone.expeval_mk3.internal.ast.SubscriptBounds;
import com.runestone.expeval_mk3.internal.ast.TimeLiteralValue;
import com.runestone.expeval_mk3.internal.ast.UnaryOperationNode;
import com.runestone.expeval_mk3.internal.ast.UnaryOperator;
import com.runestone.expeval_mk3.internal.diagnostics.DiagnosticCategory;
import com.runestone.expeval_mk3.internal.diagnostics.DiagnosticCode;
import com.runestone.expeval_mk3.internal.diagnostics.ExpressionDiagnostic;
import com.runestone.expeval_mk3.internal.source.SourceSpan;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public final class SemanticResolver {

    public SemanticResolutionResult resolve(ExpressionFileNode ast, ExpressionEnvironment environment) {
        Objects.requireNonNull(ast, "ast");
        Objects.requireNonNull(environment, "environment");
        return new ResolutionSession(environment).resolve(ast);
    }

    private static final class ResolutionSession {

        private final ExpressionEnvironment environment;
        private final List<ExpressionDiagnostic> diagnostics = new ArrayList<>();
        private final List<ExpressionDiagnostic> warnings = new ArrayList<>();
        private final Map<NodeId, ExpressionType> resolvedTypes = new HashMap<>();
        private final Map<NodeId, RuntimeNullability> nullability = new HashMap<>();
        private final Map<NodeId, Object> preparedValues = new HashMap<>();
        private final Map<NodeId, CollectionShape> collectionShapes = new HashMap<>();
        private final Map<NodeId, SymbolBinding> symbolBindings = new HashMap<>();
        private final Map<NodeId, ExpressionType> equalityOperandTypes = new HashMap<>();
        private final Map<NodeId, FunctionDescriptor> functionBindings = new HashMap<>();
        private final Map<String, SymbolBinding> visibleBindings = new LinkedHashMap<>();
        private final Map<String, CollectionShape> visibleCollectionShapes = new HashMap<>();
        private final List<SymbolBinding> externalBindings = new ArrayList<>();
        private final List<SymbolBinding> currentItemBindings = new ArrayList<>();
        private final List<Integer> currentItemSlotsByDepth = new ArrayList<>();
        private int nextFrameSlot;

        private ResolutionSession(ExpressionEnvironment environment) {
            this.environment = environment;
            buildExternalBindings(environment);
        }

        private SemanticResolutionResult resolve(ExpressionFileNode ast) {
            for (AssignmentNode assignment : ast.assignments()) {
                resolveAssignment(assignment);
            }
            if (ast.resultExpression().isEmpty()) {
                diagnostic(DiagnosticCode.SEMANTIC_EMPTY_EXPRESSION, "Expression file has no result expression", ast.sourceSpan());
            } else {
                Resolution resolution = resolveExpression(ast.resultExpression().orElseThrow(), null);
                if (resolution.pending()) {
                    emptyCollectionDiagnostic(resolution.pendingSpan());
                }
            }
            if (!diagnostics.isEmpty()) {
                return new SemanticResolutionFailure(diagnostics);
            }
            return new SemanticResolutionSuccess(new SemanticModel(
                    ast,
                    resolvedTypes,
                    nullability,
                    preparedValues,
                    collectionShapes,
                    symbolBindings,
                    equalityOperandTypes,
                    functionBindings,
                    new FrameLayout(externalBindings, nextFrameSlot)), warnings);
        }

        private void resolveAssignment(AssignmentNode assignment) {
            Resolution value = resolveExpression(assignment.expression(), null);
            if (value.pending()) {
                emptyCollectionDiagnostic(value.pendingSpan());
                return;
            }
            if (value.invalid()) {
                return;
            }
            bindAssignmentTarget(assignment.target(), value.type(), assignment.expression().id());
        }

        private void bindAssignmentTarget(AssignmentTargetNode target, ExpressionType valueType, NodeId valueNodeId) {
            if (target instanceof IdentifierAssignmentTargetNode identifier) {
                SymbolBinding binding = bindInternal(identifier.name(), valueType, identifier.sourceSpan());
                symbolBindings.put(identifier.id(), binding);
                updateVisibleCollectionShape(identifier.name(), valueType, collectionShapes.get(valueNodeId));
                return;
            }
            if (target instanceof DestructuringAssignmentTargetNode destructuring) {
                if (!(valueType instanceof CollectionType collectionType)) {
                    diagnostic(
                            DiagnosticCode.SEMANTIC_ASSIGNMENT_TARGET_MISMATCH,
                            "Destructuring assignment requires a collection value",
                            target.sourceSpan());
                    return;
                }
                if (!destructuringTargetsAreUnique(destructuring)) {
                    return;
                }
                CollectionShape shape = collectionShapes.get(valueNodeId);
                if (shape != null && shape.fixed() && shape.fixedSize() < destructuring.elements().size()) {
                    diagnostic(
                            DiagnosticCode.SEMANTIC_DESTRUCTURING_SIZE_MISMATCH,
                            "Destructuring source has fewer elements than targets",
                            destructuring.sourceSpan());
                    return;
                }
                for (IdentifierAssignmentTargetNode element : destructuring.elements()) {
                    SymbolBinding binding = bindInternal(element.name(), collectionType.elementType(), element.sourceSpan());
                    symbolBindings.put(element.id(), binding);
                }
                return;
            }
            diagnostic(DiagnosticCode.SEMANTIC_UNSUPPORTED_EXPRESSION, "Unsupported assignment target", target.sourceSpan());
        }

        private boolean destructuringTargetsAreUnique(DestructuringAssignmentTargetNode destructuring) {
            Set<String> names = new HashSet<>();
            boolean unique = true;
            for (IdentifierAssignmentTargetNode element : destructuring.elements()) {
                if (!names.add(element.name())) {
                    diagnostic(
                            DiagnosticCode.SEMANTIC_DUPLICATE_ASSIGNMENT_TARGET,
                            "Destructuring assignment target contains duplicate symbol '" + element.name() + "'",
                            element.sourceSpan());
                    unique = false;
                }
            }
            return unique;
        }

        private SymbolBinding bindInternal(String name, ExpressionType type, SourceSpan span) {
            SymbolBinding existing = visibleBindings.get(name);
            if (existing != null && !existing.external()) {
                if (!existing.type().equals(type)) {
                    diagnostic(
                            DiagnosticCode.SEMANTIC_ASSIGNMENT_TARGET_MISMATCH,
                            "Internal symbol assignment must keep the original type",
                            span);
                }
                return existing;
            }
            if (existing != null && existing.external()) {
                warning(
                        DiagnosticCode.SEMANTIC_SYMBOL_SHADOWING,
                        "Internal symbol shadows external symbol '" + name + "'",
                        span);
            }
            SymbolBinding binding = SymbolBinding.internal(name, type, nextFrameSlot++);
            visibleBindings.put(name, binding);
            return binding;
        }

        private void updateVisibleCollectionShape(String name, ExpressionType type, CollectionShape shape) {
            if (type instanceof CollectionType) {
                visibleCollectionShapes.put(name, shape == null ? CollectionShape.unknown() : shape);
                return;
            }
            visibleCollectionShapes.remove(name);
        }

        private Resolution resolveExpression(ExpressionNode expression, ExpressionType expectedType) {
            if (expression instanceof LiteralNode literal) {
                return resolveLiteral(literal, expectedType);
            }
            if (expression instanceof CollectionLiteralNode collection) {
                return resolveCollection(collection, expectedType);
            }
            if (expression instanceof BinaryOperationNode binary) {
                return resolveBinary(binary);
            }
            if (expression instanceof UnaryOperationNode unary) {
                return resolveUnary(unary);
            }
            if (expression instanceof PostfixOperationNode postfix) {
                return resolvePostfix(postfix);
            }
            if (expression instanceof BetweenNode between) {
                return resolveBetween(between);
            }
            if (expression instanceof MembershipNode membership) {
                return resolveMembership(membership);
            }
            if (expression instanceof NullCoalesceNode coalesce) {
                return resolveNullCoalesce(coalesce, expectedType);
            }
            if (expression instanceof ConditionalNode conditional) {
                return resolveConditional(conditional, expectedType);
            }
            if (expression instanceof FunctionCallNode functionCall) {
                return resolveFunctionCall(functionCall);
            }
            if (expression instanceof NavigationChainNode navigation) {
                return resolveNavigationChain(navigation);
            }
            if (expression instanceof CurrentItemNode currentItem) {
                return resolveCurrentItem(currentItem);
            }
            if (expression instanceof CurrentTemporalValueNode currentTemporalValue) {
                return resolveCurrentTemporalValue(currentTemporalValue);
            }
            if (expression instanceof IdentifierNode identifier) {
                return resolveIdentifier(identifier);
            }
            if (expression instanceof GroupedExpressionNode grouped) {
                Resolution resolution = resolveExpression(grouped.expression(), expectedType);
                if (resolution.known()) {
                    recordValue(grouped.id(), resolution.type());
                    recordCollectionShape(grouped.id(), resolution.type(), collectionShapes.get(grouped.expression().id()));
                }
                return resolution;
            }
            diagnostic(
                    DiagnosticCode.SEMANTIC_UNSUPPORTED_EXPRESSION,
                    "Expression construct is not supported by this compilation slice",
                    expression.sourceSpan());
            return Resolution.invalidResolution();
        }

        private Resolution resolveLiteral(LiteralNode literal, ExpressionType expectedType) {
            LiteralResolution resolution = literalResolution(literal.value());
            if (expectedType != null && !expectedType.equals(resolution.type())) {
                diagnostic(
                        DiagnosticCode.SEMANTIC_OPERATOR_TYPE_MISMATCH,
                        "Literal is not compatible with expected type " + expectedType,
                        literal.sourceSpan());
                return Resolution.invalidResolution();
            }
            recordValue(literal.id(), resolution.type());
            preparedValues.put(literal.id(), resolution.value());
            return Resolution.known(resolution.type());
        }

        private LiteralResolution literalResolution(LiteralValue value) {
            return switch (value) {
                case LongLiteralValue literal -> new LiteralResolution(
                        ScalarType.NUMBER, BigDecimal.valueOf(literal.value()));
                case BigIntegerLiteralValue literal -> new LiteralResolution(
                        ScalarType.NUMBER, new BigDecimal(literal.value()));
                case DecimalLiteralValue literal -> new LiteralResolution(ScalarType.NUMBER, literal.value());
                case BooleanLiteralValue literal -> new LiteralResolution(ScalarType.BOOLEAN, literal.value());
                case StringLiteralValue literal -> new LiteralResolution(ScalarType.STRING, literal.value());
                case DateLiteralValue literal -> new LiteralResolution(ScalarType.DATE, literal.value());
                case TimeLiteralValue literal -> new LiteralResolution(ScalarType.TIME, literal.value());
                case LocalDateTimeLiteralValue literal -> new LiteralResolution(ScalarType.DATETIME, literal.value());
                case OffsetDateTimeLiteralValue literal -> new LiteralResolution(
                        ScalarType.DATETIME,
                        environment.normalizeOffsetDateTimeLiteral(literal.value()).normalizedLocalDateTime());
            };
        }

        private Resolution resolveCollection(CollectionLiteralNode collection, ExpressionType expectedType) {
            if (collection.elements().size() > environment.maxMaterializedSize()) {
                diagnostic(
                        DiagnosticCode.SEMANTIC_MATERIALIZATION_LIMIT_EXCEEDED,
                        "Collection literal exceeds maxMaterializedSize " + environment.maxMaterializedSize(),
                        collection.sourceSpan());
                return Resolution.invalidResolution();
            }

            ExpressionType expectedElementType = null;
            if (expectedType instanceof CollectionType expectedCollection) {
                expectedElementType = expectedCollection.elementType();
            } else if (expectedType != null) {
                diagnostic(
                        DiagnosticCode.SEMANTIC_COLLECTION_ELEMENT_TYPE_MISMATCH,
                        "Collection literal is not compatible with expected type " + expectedType,
                        collection.sourceSpan());
                return Resolution.invalidResolution();
            }

            if (collection.elements().isEmpty()) {
                collectionShapes.put(collection.id(), new CollectionShape(0));
                if (expectedElementType == null) {
                    return Resolution.pendingResolution(collection.sourceSpan());
                }
                CollectionType type = new CollectionType(expectedElementType);
                recordValue(collection.id(), type);
                return Resolution.known(type);
            }

            ExpressionType elementType = expectedElementType;
            List<ExpressionNode> pendingElements = new ArrayList<>();
            for (ExpressionNode element : collection.elements()) {
                Resolution elementResolution = resolveExpression(element, elementType);
                if (elementResolution.invalid()) {
                    continue;
                }
                if (elementResolution.pending()) {
                    pendingElements.add(element);
                    continue;
                }
                if (elementType == null) {
                    elementType = elementResolution.type();
                } else if (!elementType.equals(elementResolution.type())) {
                    diagnostic(
                            DiagnosticCode.SEMANTIC_COLLECTION_ELEMENT_TYPE_MISMATCH,
                            "Collection elements must have one known type",
                            element.sourceSpan());
                }
            }

            if (elementType == null) {
                if (pendingElements.isEmpty()) {
                    return Resolution.invalidResolution();
                }
                return Resolution.pendingResolution(pendingElements.getFirst().sourceSpan());
            }
            for (ExpressionNode pendingElement : pendingElements) {
                resolveExpression(pendingElement, elementType);
            }
            CollectionType collectionType = new CollectionType(elementType);
            recordValue(collection.id(), collectionType);
            collectionShapes.put(collection.id(), new CollectionShape(collection.elements().size()));
            return Resolution.known(collectionType);
        }

        private Resolution resolveBinary(BinaryOperationNode binary) {
            return switch (binary.operator()) {
                case ADD, SUBTRACT, MULTIPLY, DIVIDE, MODULO, ROOT, EXPONENTIATE -> resolveSameTypeBinary(
                        binary, ScalarType.NUMBER, ScalarType.NUMBER);
                case CONCATENATE -> resolveSameTypeBinary(binary, ScalarType.STRING, ScalarType.STRING);
                case LOGICAL_AND, LOGICAL_OR, LOGICAL_NAND, LOGICAL_NOR, LOGICAL_XOR, LOGICAL_XNOR -> resolveSameTypeBinary(
                        binary, ScalarType.BOOLEAN, ScalarType.BOOLEAN);
                case GREATER_THAN, GREATER_THAN_OR_EQUAL, LESS_THAN, LESS_THAN_OR_EQUAL -> resolveComparison(binary);
                case EQUAL, NOT_EQUAL -> resolveEquality(binary);
                case REGEX_MATCH, REGEX_NOT_MATCH -> resolveRegex(binary);
            };
        }

        private Resolution resolveSameTypeBinary(
                BinaryOperationNode binary,
                ExpressionType operandType,
                ExpressionType resultType) {
            Resolution left = resolveExpression(binary.left(), operandType);
            Resolution right = resolveExpression(binary.right(), operandType);
            if (left.invalid() || right.invalid()) {
                return Resolution.invalidResolution();
            }
            if (!left.known() || !right.known() || !left.type().equals(operandType) || !right.type().equals(operandType)) {
                diagnostic(
                        DiagnosticCode.SEMANTIC_OPERATOR_TYPE_MISMATCH,
                        "Operator requires operands of type " + operandType,
                        binary.operatorSpan());
                return Resolution.invalidResolution();
            }
            recordValue(binary.id(), resultType);
            return Resolution.known(resultType);
        }

        private Resolution resolveComparison(BinaryOperationNode binary) {
            Resolution left = resolveExpression(binary.left(), null);
            Resolution right = resolveExpression(binary.right(), left.known() ? left.type() : null);
            if (left.invalid() || right.invalid() || left.pending() || right.pending()) {
                return invalidForPending(left, right);
            }
            if (!left.type().equals(right.type()) || !orderable(left.type())) {
                diagnostic(
                        DiagnosticCode.SEMANTIC_OPERATOR_TYPE_MISMATCH,
                        "Comparison operands must have the same orderable type",
                        binary.operatorSpan());
                return Resolution.invalidResolution();
            }
            recordValue(binary.id(), ScalarType.BOOLEAN);
            return Resolution.known(ScalarType.BOOLEAN);
        }

        private Resolution resolveEquality(BinaryOperationNode binary) {
            Resolution left = resolveExpression(binary.left(), null);
            Resolution right = resolveExpression(binary.right(), left.known() ? left.type() : null);
            if (left.pending() && right.known()) {
                left = resolveExpression(binary.left(), right.type());
            } else if (right.pending() && left.known()) {
                right = resolveExpression(binary.right(), left.type());
            }
            if (left.invalid() || right.invalid() || left.pending() || right.pending()) {
                return invalidForPending(left, right);
            }
            if (!left.type().equals(right.type())) {
                diagnostic(
                        DiagnosticCode.SEMANTIC_EQUALITY_TYPE_MISMATCH,
                        "Equality operands must have the same known type",
                        binary.operatorSpan());
                return Resolution.invalidResolution();
            }
            if (!supportsStructuralEquality(left.type())) {
                diagnostic(
                        DiagnosticCode.SEMANTIC_OBJECT_EQUALITY_NOT_SUPPORTED,
                        "Nominal object values do not support generic equality",
                        binary.operatorSpan());
                return Resolution.invalidResolution();
            }
            equalityOperandTypes.put(binary.id(), left.type());
            recordValue(binary.id(), ScalarType.BOOLEAN);
            return Resolution.known(ScalarType.BOOLEAN);
        }

        private Resolution resolveRegex(BinaryOperationNode binary) {
            boolean literalPattern = binary.right() instanceof LiteralNode;
            Resolution left = resolveExpression(binary.left(), ScalarType.STRING);
            Resolution right = resolveExpression(binary.right(), ScalarType.STRING);
            if (!literalPattern) {
                diagnostic(
                        DiagnosticCode.SEMANTIC_REGEX_PATTERN_INVALID,
                        "Regex pattern must be a string literal",
                        binary.right().sourceSpan());
                return Resolution.invalidResolution();
            }
            if (left.invalid() || right.invalid()) {
                return Resolution.invalidResolution();
            }
            if (!left.known() || !right.known() || left.type() != ScalarType.STRING || right.type() != ScalarType.STRING) {
                diagnostic(
                        DiagnosticCode.SEMANTIC_OPERATOR_TYPE_MISMATCH,
                        "Regex operators require text operands",
                        binary.operatorSpan());
                return Resolution.invalidResolution();
            }
            try {
                preparedValues.put(binary.id(), Pattern.compile((String) preparedValues.get(binary.right().id())));
            } catch (PatternSyntaxException exception) {
                diagnostic(DiagnosticCode.SEMANTIC_REGEX_PATTERN_INVALID, "Invalid regex pattern", binary.right().sourceSpan());
                return Resolution.invalidResolution();
            }
            recordValue(binary.id(), ScalarType.BOOLEAN);
            return Resolution.known(ScalarType.BOOLEAN);
        }

        private Resolution resolveUnary(UnaryOperationNode unary) {
            ExpressionType operandType = unary.operator() == UnaryOperator.NEGATE ? ScalarType.NUMBER : ScalarType.BOOLEAN;
            Resolution operand = resolveExpression(unary.operand(), operandType);
            if (operand.invalid()) {
                return Resolution.invalidResolution();
            }
            if (!operand.known() || !operand.type().equals(operandType)) {
                diagnostic(
                        DiagnosticCode.SEMANTIC_OPERATOR_TYPE_MISMATCH,
                        "Unary operator requires operand of type " + operandType,
                        unary.operatorSpan());
                return Resolution.invalidResolution();
            }
            recordValue(unary.id(), operandType);
            return Resolution.known(operandType);
        }

        private Resolution resolvePostfix(PostfixOperationNode postfix) {
            Resolution operand = resolveExpression(postfix.operand(), ScalarType.NUMBER);
            if (operand.invalid()) {
                return Resolution.invalidResolution();
            }
            if (!operand.known() || operand.type() != ScalarType.NUMBER) {
                diagnostic(
                        DiagnosticCode.SEMANTIC_OPERATOR_TYPE_MISMATCH,
                        "Postfix numeric operator requires a number",
                        postfix.sourceSpan());
                return Resolution.invalidResolution();
            }
            recordValue(postfix.id(), ScalarType.NUMBER);
            return Resolution.known(ScalarType.NUMBER);
        }

        private Resolution resolveBetween(BetweenNode between) {
            Resolution value = resolveExpression(between.value(), null);
            Resolution lower = resolveExpression(between.lowerBound(), value.known() ? value.type() : null);
            Resolution upper = resolveExpression(between.upperBound(), value.known() ? value.type() : null);
            if (value.invalid() || lower.invalid() || upper.invalid() || value.pending() || lower.pending() || upper.pending()) {
                return invalidForPending(value, lower, upper);
            }
            if (!value.type().equals(lower.type()) || !value.type().equals(upper.type()) || !orderable(value.type())) {
                diagnostic(
                        DiagnosticCode.SEMANTIC_OPERATOR_TYPE_MISMATCH,
                        "Between operands must have the same orderable type",
                        between.operatorSpan());
                return Resolution.invalidResolution();
            }
            recordValue(between.id(), ScalarType.BOOLEAN);
            return Resolution.known(ScalarType.BOOLEAN);
        }

        private Resolution resolveMembership(MembershipNode membership) {
            Resolution element = resolveExpression(membership.element(), null);
            ExpressionType expectedCollection = element.known() ? new CollectionType(element.type()) : null;
            Resolution collection = resolveExpression(membership.collection(), expectedCollection);
            if (element.invalid() || collection.invalid() || element.pending() || collection.pending()) {
                return invalidForPending(element, collection);
            }
            if (collection.type() instanceof CollectionType collectionType) {
                if (!collectionType.elementType().equals(element.type())) {
                    diagnostic(
                            DiagnosticCode.SEMANTIC_OPERATOR_TYPE_MISMATCH,
                            "Membership element type must match collection element type",
                            membership.operatorSpan());
                    return Resolution.invalidResolution();
                }
            } else if (collection.type() instanceof MapType) {
                if (element.type() != ScalarType.STRING) {
                    diagnostic(
                            DiagnosticCode.SEMANTIC_OPERATOR_TYPE_MISMATCH,
                            "Map membership requires a text key",
                            membership.operatorSpan());
                    return Resolution.invalidResolution();
                }
            } else {
                diagnostic(
                        DiagnosticCode.SEMANTIC_OPERATOR_TYPE_MISMATCH,
                        "Membership requires a collection or map",
                        membership.operatorSpan());
                return Resolution.invalidResolution();
            }
            recordValue(membership.id(), ScalarType.BOOLEAN);
            return Resolution.known(ScalarType.BOOLEAN);
        }

        private Resolution resolveNullCoalesce(NullCoalesceNode coalesce, ExpressionType expectedType) {
            ExpressionType resultType = expectedType;
            List<ExpressionNode> pending = new ArrayList<>();
            for (ExpressionNode operand : coalesce.operands()) {
                Resolution resolution = resolveExpression(operand, resultType);
                if (resolution.invalid()) {
                    continue;
                }
                if (resolution.pending()) {
                    pending.add(operand);
                    continue;
                }
                if (resultType == null) {
                    resultType = resolution.type();
                } else if (!resultType.equals(resolution.type())) {
                    diagnostic(
                            DiagnosticCode.SEMANTIC_OPERATOR_TYPE_MISMATCH,
                            "Null coalescence operands must have the same type",
                            operand.sourceSpan());
                }
            }
            if (resultType == null) {
                return pending.isEmpty()
                        ? Resolution.invalidResolution()
                        : Resolution.pendingResolution(pending.getFirst().sourceSpan());
            }
            for (ExpressionNode operand : pending) {
                resolveExpression(operand, resultType);
            }
            recordValue(coalesce.id(), resultType);
            return Resolution.known(resultType);
        }

        private Resolution resolveConditional(ConditionalNode conditional, ExpressionType expectedType) {
            ExpressionType resultType = expectedType;
            List<ExpressionNode> pendingConsequences = new ArrayList<>();
            for (ConditionalBranchNode branch : conditional.branches()) {
                Resolution condition = resolveExpression(branch.condition(), ScalarType.BOOLEAN);
                if (!condition.invalid() && (!condition.known() || condition.type() != ScalarType.BOOLEAN)) {
                    diagnostic(
                            DiagnosticCode.SEMANTIC_OPERATOR_TYPE_MISMATCH,
                            "Conditional branch condition must be boolean",
                            branch.condition().sourceSpan());
                }
                Resolution consequence = resolveExpression(branch.consequence(), resultType);
                resultType = mergeConditionalType(resultType, consequence, pendingConsequences, branch.consequence());
            }
            Resolution elseResolution = resolveExpression(conditional.elseExpression(), resultType);
            resultType = mergeConditionalType(resultType, elseResolution, pendingConsequences, conditional.elseExpression());
            if (resultType == null) {
                return pendingConsequences.isEmpty()
                        ? Resolution.invalidResolution()
                        : Resolution.pendingResolution(pendingConsequences.getFirst().sourceSpan());
            }
            for (ExpressionNode pendingConsequence : pendingConsequences) {
                resolveExpression(pendingConsequence, resultType);
            }
            recordValue(conditional.id(), resultType);
            return Resolution.known(resultType);
        }

        private ExpressionType mergeConditionalType(
                ExpressionType currentType,
                Resolution branchResolution,
                List<ExpressionNode> pendingConsequences,
                ExpressionNode expression) {
            if (branchResolution.invalid()) {
                return currentType;
            }
            if (branchResolution.pending()) {
                pendingConsequences.add(expression);
                return currentType;
            }
            if (currentType == null) {
                return branchResolution.type();
            }
            if (!currentType.equals(branchResolution.type())) {
                diagnostic(
                        DiagnosticCode.SEMANTIC_CONDITIONAL_TYPE_MISMATCH,
                        "Conditional consequences must have the same type",
                        expression.sourceSpan());
            }
            return currentType;
        }

        private Resolution resolveFunctionCall(FunctionCallNode functionCall) {
            List<ExpressionType> argumentTypes = new ArrayList<>(functionCall.arguments().size());
            boolean invalid = false;
            for (CallArgument argument : functionCall.arguments()) {
                if (!(argument instanceof ExpressionCallArgument expressionArgument)) {
                    diagnostic(
                            DiagnosticCode.SEMANTIC_LAMBDA_ARGUMENT_UNSUPPORTED,
                            "Lambda arguments are not supported for global functions in this slice",
                            functionCall.sourceSpan());
                    invalid = true;
                    continue;
                }
                Resolution argumentResolution = resolveExpression(expressionArgument.expression(), null);
                if (argumentResolution.invalid() || argumentResolution.pending()) {
                    invalid = true;
                    if (argumentResolution.pending()) {
                        emptyCollectionDiagnostic(argumentResolution.pendingSpan());
                    }
                    continue;
                }
                argumentTypes.add(argumentResolution.type());
            }
            if (invalid) {
                return Resolution.invalidResolution();
            }
            FunctionLookupResult lookup = environment.functions().resolve(functionCall.name().value(), argumentTypes);
            if (lookup.status() != FunctionLookupResult.Status.EXACT_MATCH) {
                diagnostic(
                        DiagnosticCode.SEMANTIC_UNKNOWN_FUNCTION,
                        "No function registered for signature " + functionCall.name().value() + argumentTypes,
                        functionCall.sourceSpan());
                return Resolution.invalidResolution();
            }
            FunctionDescriptor descriptor = lookup.descriptor().orElseThrow();
            functionBindings.put(functionCall.id(), descriptor);
            recordValue(functionCall.id(), descriptor.returnType());
            return Resolution.known(descriptor.returnType());
        }

        private Resolution resolveNavigationChain(NavigationChainNode navigation) {
            Resolution receiver = resolveExpression(navigation.receiver(), null);
            if (receiver.invalid() || receiver.pending()) {
                return invalidForPending(receiver);
            }

            ExpressionType currentType = receiver.type();
            CollectionShape currentShape = collectionShapes.get(navigation.receiver().id());
            for (NavigationLink link : navigation.links()) {
                LinkResolution linkResolution = resolveNavigationLink(link, currentType, currentShape);
                if (linkResolution.invalid()) {
                    return Resolution.invalidResolution();
                }
                currentType = linkResolution.type();
                currentShape = linkResolution.shape();
                recordValue(link.id(), currentType);
                recordCollectionShape(link.id(), currentType, currentShape);
            }
            recordValue(navigation.id(), currentType);
            recordCollectionShape(navigation.id(), currentType, currentShape);
            return Resolution.known(currentType);
        }

        private LinkResolution resolveNavigationLink(
                NavigationLink link,
                ExpressionType receiverType,
                CollectionShape receiverShape) {
            if (link instanceof IndexSubscriptNavigationLink index) {
                return resolveIndexSubscript(index, receiverType, receiverShape);
            }
            if (link instanceof SliceSubscriptNavigationLink slice) {
                return resolveSliceSubscript(slice, receiverType, receiverShape);
            }
            if (link instanceof FilterNavigationLink filter) {
                return resolveFilter(filter, receiverType);
            }
            diagnostic(
                    DiagnosticCode.SEMANTIC_UNSUPPORTED_EXPRESSION,
                    "Navigation link is not supported by this compilation slice",
                    link.sourceSpan());
            return LinkResolution.invalidResolution();
        }

        private LinkResolution resolveIndexSubscript(
                IndexSubscriptNavigationLink index,
                ExpressionType receiverType,
                CollectionShape receiverShape) {
            if (!(receiverType instanceof CollectionType collectionType)) {
                diagnostic(
                        DiagnosticCode.SEMANTIC_OPERATOR_TYPE_MISMATCH,
                        "Index subscript requires a collection receiver",
                        index.sourceSpan());
                return LinkResolution.invalidResolution();
            }
            if (receiverShape != null && receiverShape.fixed()
                    && !SubscriptBounds.indexWithinFixedSize(index.index().value(), receiverShape.fixedSize())) {
                diagnostic(
                        DiagnosticCode.SEMANTIC_COLLECTION_INDEX_OUT_OF_BOUNDS,
                        "Collection index is outside the known collection bounds",
                        index.sourceSpan());
                return LinkResolution.invalidResolution();
            }
            return LinkResolution.known(collectionType.elementType(), null);
        }

        private LinkResolution resolveSliceSubscript(
                SliceSubscriptNavigationLink slice,
                ExpressionType receiverType,
                CollectionShape receiverShape) {
            if (!(receiverType instanceof CollectionType collectionType)) {
                diagnostic(
                        DiagnosticCode.SEMANTIC_OPERATOR_TYPE_MISMATCH,
                        "Slice subscript requires a collection receiver",
                        slice.sourceSpan());
                return LinkResolution.invalidResolution();
            }
            CollectionShape shape = CollectionShape.unknown();
            if (receiverShape != null && receiverShape.fixed()) {
                int size = receiverShape.fixedSize();
                int start = SubscriptBounds.normalizedSliceBound(slice.start(), size, 0);
                int end = SubscriptBounds.normalizedSliceBound(slice.end(), size, size);
                shape = new CollectionShape(Math.max(end - start, 0));
            }
            return LinkResolution.known(new CollectionType(collectionType.elementType()), shape);
        }

        private LinkResolution resolveFilter(FilterNavigationLink filter, ExpressionType receiverType) {
            if (!(receiverType instanceof CollectionType collectionType)) {
                diagnostic(
                        DiagnosticCode.SEMANTIC_OPERATOR_TYPE_MISMATCH,
                        "Filter requires a collection receiver",
                        filter.sourceSpan());
                return LinkResolution.invalidResolution();
            }
            if (currentItemBindings.size() >= environment.maxCurrentItemDepth()) {
                diagnostic(
                        DiagnosticCode.SEMANTIC_CURRENT_ITEM_DEPTH_EXCEEDED,
                        "Filter exceeds maxCurrentItemDepth " + environment.maxCurrentItemDepth(),
                        filter.sourceSpan());
                return LinkResolution.invalidResolution();
            }
            int depth = currentItemBindings.size();
            SymbolBinding currentItem = SymbolBinding.internal(
                    "@" + depth, collectionType.elementType(), currentItemFrameSlot(depth));
            symbolBindings.put(filter.id(), currentItem);
            currentItemBindings.add(currentItem);
            Resolution predicate = resolveExpression(filter.predicate(), ScalarType.BOOLEAN);
            currentItemBindings.remove(currentItemBindings.size() - 1);
            if (predicate.invalid() || predicate.pending()) {
                invalidForPending(predicate);
                return LinkResolution.invalidResolution();
            }
            if (predicate.type() != ScalarType.BOOLEAN) {
                diagnostic(
                        DiagnosticCode.SEMANTIC_OPERATOR_TYPE_MISMATCH,
                        "Filter predicate must be boolean",
                        filter.predicate().sourceSpan());
                return LinkResolution.invalidResolution();
            }
            return LinkResolution.known(new CollectionType(collectionType.elementType()), CollectionShape.unknown());
        }

        private int currentItemFrameSlot(int depth) {
            if (depth == currentItemSlotsByDepth.size()) {
                currentItemSlotsByDepth.add(nextFrameSlot++);
            }
            return currentItemSlotsByDepth.get(depth);
        }

        private Resolution resolveCurrentItem(CurrentItemNode currentItem) {
            if (currentItemBindings.isEmpty()) {
                diagnostic(
                        DiagnosticCode.SEMANTIC_CURRENT_ITEM_OUT_OF_SCOPE,
                        "Current item is only available inside filters and lambdas",
                        currentItem.sourceSpan());
                return Resolution.invalidResolution();
            }
            SymbolBinding binding = currentItemBindings.getLast();
            symbolBindings.put(currentItem.id(), binding);
            recordValue(currentItem.id(), binding.type());
            return Resolution.known(binding.type());
        }

        private Resolution resolveCurrentTemporalValue(CurrentTemporalValueNode currentTemporalValue) {
            ScalarType type = switch (currentTemporalValue.kind()) {
                case DATE -> ScalarType.DATE;
                case TIME -> ScalarType.TIME;
                case DATE_TIME -> ScalarType.DATETIME;
            };
            recordValue(currentTemporalValue.id(), type);
            return Resolution.known(type);
        }

        private Resolution resolveIdentifier(IdentifierNode identifier) {
            SymbolBinding binding = visibleBindings.get(identifier.name());
            if (binding == null) {
                diagnostic(
                        DiagnosticCode.SEMANTIC_UNKNOWN_SYMBOL,
                        "Unknown symbol '" + identifier.name() + "'",
                        identifier.sourceSpan());
                return Resolution.invalidResolution();
            }
            symbolBindings.put(identifier.id(), binding);
            recordValue(identifier.id(), binding.type());
            recordCollectionShape(identifier.id(), binding.type(), visibleCollectionShapes.get(binding.name()));
            return Resolution.known(binding.type());
        }

        private Resolution invalidForPending(Resolution... resolutions) {
            for (Resolution resolution : resolutions) {
                if (resolution.pending()) {
                    emptyCollectionDiagnostic(resolution.pendingSpan());
                }
            }
            return Resolution.invalidResolution();
        }

        private void emptyCollectionDiagnostic(SourceSpan span) {
            diagnostic(
                    DiagnosticCode.SEMANTIC_EMPTY_COLLECTION_REQUIRES_CONTEXT,
                    "Empty collection literal requires a known element type from local context",
                    span);
        }

        private void recordValue(NodeId nodeId, ExpressionType type) {
            resolvedTypes.put(nodeId, type);
            nullability.put(nodeId, RuntimeNullability.NEVER_NULL);
        }

        private void recordCollectionShape(NodeId nodeId, ExpressionType type, CollectionShape shape) {
            if (type instanceof CollectionType && shape != null) {
                collectionShapes.put(nodeId, shape);
            }
        }

        private void diagnostic(DiagnosticCode code, String message, SourceSpan span) {
            diagnostics.add(new ExpressionDiagnostic(DiagnosticCategory.SEMANTIC, code, message, span));
        }

        private void warning(DiagnosticCode code, String message, SourceSpan span) {
            warnings.add(new ExpressionDiagnostic(DiagnosticCategory.SEMANTIC, code, message, span));
        }

        private static boolean orderable(ExpressionType type) {
            return type == ScalarType.NUMBER
                    || type == ScalarType.STRING
                    || type == ScalarType.DATE
                    || type == ScalarType.TIME
                    || type == ScalarType.DATETIME;
        }

        private static boolean supportsStructuralEquality(ExpressionType type) {
            if (type instanceof ObjectType) {
                return false;
            }
            if (type instanceof CollectionType collectionType) {
                return supportsStructuralEquality(collectionType.elementType());
            }
            if (type instanceof MapType mapType) {
                return supportsStructuralEquality(mapType.valueType());
            }
            return true;
        }

        private void buildExternalBindings(ExpressionEnvironment environment) {
            for (ExternalSymbol symbol : environment.externalSymbols().values()) {
                SymbolBinding binding = SymbolBinding.external(symbol, nextFrameSlot++);
                externalBindings.add(binding);
                visibleBindings.put(symbol.name(), binding);
                updateVisibleCollectionShape(symbol.name(), symbol.type(), CollectionShape.unknown());
            }
        }
    }

    private record LiteralResolution(ExpressionType type, Object value) {
    }

    private record LinkResolution(ExpressionType type, CollectionShape shape, boolean invalid) {

        private static LinkResolution known(ExpressionType type, CollectionShape shape) {
            return new LinkResolution(Objects.requireNonNull(type, "type"), shape, false);
        }

        private static LinkResolution invalidResolution() {
            return new LinkResolution(null, null, true);
        }
    }

    private record Resolution(ExpressionType type, SourceSpan pendingSpan, boolean pending, boolean invalid) {

        private static Resolution known(ExpressionType type) {
            return new Resolution(Objects.requireNonNull(type, "type"), null, false, false);
        }

        private static Resolution pendingResolution(SourceSpan span) {
            return new Resolution(null, Objects.requireNonNull(span, "span"), true, false);
        }

        private static Resolution invalidResolution() {
            return new Resolution(null, null, false, true);
        }

        private boolean known() {
            return type != null && !pending && !invalid;
        }
    }
}
