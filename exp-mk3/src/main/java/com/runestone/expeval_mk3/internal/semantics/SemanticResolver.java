package com.runestone.expeval_mk3.internal.semantics;

import com.runestone.expeval_mk3.api.CollectionOperationCatalog;
import com.runestone.expeval_mk3.api.CollectionType;
import com.runestone.expeval_mk3.api.ExpressionEnvironment;
import com.runestone.expeval_mk3.api.ExpressionType;
import com.runestone.expeval_mk3.api.FunctionDescriptor;
import com.runestone.expeval_mk3.api.FunctionLookupResult;
import com.runestone.expeval_mk3.api.FunctionSignature;
import com.runestone.expeval_mk3.api.JavaMethodDescriptor;
import com.runestone.expeval_mk3.api.JavaPropertyDescriptor;
import com.runestone.expeval_mk3.api.JavaTypeDescriptor;
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
import com.runestone.expeval_mk3.internal.ast.CallNavigationLink;
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
import com.runestone.expeval_mk3.internal.ast.LambdaCallArgument;
import com.runestone.expeval_mk3.internal.ast.LambdaNode;
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
import com.runestone.expeval_mk3.internal.ast.PropertyNavigationLink;
import com.runestone.expeval_mk3.internal.ast.SliceSubscriptNavigationLink;
import com.runestone.expeval_mk3.internal.ast.StringKeySubscriptNavigationLink;
import com.runestone.expeval_mk3.internal.ast.StringLiteralValue;
import com.runestone.expeval_mk3.internal.ast.SubscriptBounds;
import com.runestone.expeval_mk3.internal.ast.TimeLiteralValue;
import com.runestone.expeval_mk3.internal.ast.UnaryOperationNode;
import com.runestone.expeval_mk3.internal.ast.UnaryOperator;
import com.runestone.expeval_mk3.internal.ast.WildcardNavigationLink;
import com.runestone.expeval_mk3.internal.diagnostics.DiagnosticCode;
import com.runestone.expeval_mk3.api.DiagnosticCategory;
import com.runestone.expeval_mk3.api.DiagnosticSeverity;
import com.runestone.expeval_mk3.api.ExpressionDiagnostic;
import com.runestone.expeval_mk3.api.SourceSpan;

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

        private static final ObjectType MAP_ENTRY_TYPE = new ObjectType("com.runestone.expeval_mk3.internal.MapEntry");
        private static final ObjectType REDUCTION_ITEM_TYPE = new ObjectType(
                "com.runestone.expeval_mk3.internal.ReductionItem");
        private static final String MAP_ENTRY_KEY_MEMBER = "k";
        private static final String MAP_ENTRY_VALUE_MEMBER = "v";
        private static final String REDUCTION_ACCUMULATOR_MEMBER = "accumulator";
        private static final String REDUCTION_ITEM_MEMBER = "item";

        private final ExpressionEnvironment environment;
        private final List<ExpressionDiagnostic> diagnostics = new ArrayList<>();
        private final List<ExpressionDiagnostic> warnings = new ArrayList<>();
        private final Map<NodeId, ExpressionType> resolvedTypes = new HashMap<>();
        private final Map<NodeId, RuntimeNullability> nullability = new HashMap<>();
        private final Map<NodeId, Object> preparedValues = new HashMap<>();
        private final Map<NodeId, CollectionShape> collectionShapes = new HashMap<>();
        private final Map<NodeId, Boolean> expressionPurity = new HashMap<>();
        private final Map<NodeId, SymbolBinding> symbolBindings = new HashMap<>();
        private final Map<NodeId, ExpressionType> equalityOperandTypes = new HashMap<>();
        private final Map<NodeId, FunctionDescriptor> functionBindings = new HashMap<>();
        private final Map<NodeId, NavigationBinding> navigationBindings = new HashMap<>();
        private final Map<String, SymbolBinding> visibleBindings = new LinkedHashMap<>();
        private final Map<String, CollectionShape> visibleCollectionShapes = new HashMap<>();
        private final List<SymbolBinding> externalBindings = new ArrayList<>();
        private final List<SymbolBinding> currentItemBindings = new ArrayList<>();
        private final List<Integer> currentItemSlotsByDepth = new ArrayList<>();
        private final Map<Integer, ExpressionType> mapEntryValueTypesByFrameSlot = new HashMap<>();
        private final Map<Integer, ReductionItemTypes> reductionItemTypesByFrameSlot = new HashMap<>();
        private int nextFrameSlot;

        private ResolutionSession(ExpressionEnvironment environment) {
            this.environment = environment;
        }

        private SemanticResolutionResult resolve(ExpressionFileNode ast) {
            for (AssignmentNode assignment : ast.assignments()) {
                resolveAssignment(assignment);
            }
            if (ast.assignments().isEmpty() && ast.resultExpression().isEmpty()) {
                // ast.sourceSpan() falls back to the EOF token position, offset 0/line 1/column 1 for a
                // literally empty source, matching the decided stable span for this diagnostic.
                diagnostic(
                        DiagnosticCode.SEMANTIC_EMPTY_EXPRESSION,
                        "Expression file has no assignments and no result expression",
                        ast.sourceSpan());
            } else if (ast.resultExpression().isPresent()) {
                ExpressionNode result = ast.resultExpression().orElseThrow();
                Resolution resolution = resolveExpression(result, null);
                if (resolution.pending()) {
                    emptyCollectionDiagnostic(resolution.pendingSpan());
                } else if (resolution.known()) {
                    rejectIfNullable(
                            result,
                            DiagnosticCode.SEMANTIC_NULLABLE_RESULT_NOT_ALLOWED,
                            "Result expression may be null at runtime");
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
                    navigationBindings,
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
            RuntimeNullability valueNullability = nullabilityOf(assignment.expression().id());
            if (rejectIfNullable(
                    assignment.expression(),
                    DiagnosticCode.SEMANTIC_NULLABLE_ASSIGNMENT_NOT_ALLOWED,
                    "Assignment right-hand side may be null at runtime")) {
                valueNullability = RuntimeNullability.NEVER_NULL;
            }
            bindAssignmentTarget(
                    assignment.target(),
                    value.type(),
                    assignment.expression().id(),
                    valueNullability);
        }

        private void bindAssignmentTarget(
                AssignmentTargetNode target,
                ExpressionType valueType,
                NodeId valueNodeId,
                RuntimeNullability valueNullability) {
            if (target instanceof IdentifierAssignmentTargetNode identifier) {
                SymbolBinding binding = bindInternal(
                        identifier.name(), valueType, valueNullability, identifier.sourceSpan());
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
                    SymbolBinding binding = bindInternal(
                            element.name(),
                            collectionType.elementType(),
                            RuntimeNullability.NEVER_NULL,
                            element.sourceSpan());
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

        private SymbolBinding bindInternal(
                String name,
                ExpressionType type,
                RuntimeNullability runtimeNullability,
                SourceSpan span) {
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
            if (shadowsExternalSymbol(existing, name)) {
                warning(
                        DiagnosticCode.SEMANTIC_SYMBOL_SHADOWING,
                        "Internal symbol shadows external symbol '" + name + "'",
                        span);
            }
            SymbolBinding binding = SymbolBinding.internal(name, type, nextFrameSlot++, runtimeNullability);
            visibleBindings.put(name, binding);
            return binding;
        }

        private boolean shadowsExternalSymbol(SymbolBinding existing, String name) {
            if (existing != null) {
                return existing.external();
            }
            return environment.externalSymbols().contains(name);
        }

        private void updateVisibleCollectionShape(String name, ExpressionType type, CollectionShape shape) {
            if (type instanceof CollectionType) {
                visibleCollectionShapes.put(name, shape == null ? CollectionShape.unknown() : shape);
                return;
            }
            visibleCollectionShapes.remove(name);
        }

        private Resolution resolveExpression(ExpressionNode expression, ExpressionType expectedType) {
            return switch (expression) {
                case LiteralNode literal -> resolveLiteral(literal, expectedType);
                case CollectionLiteralNode collection -> resolveCollection(collection, expectedType);
                case BinaryOperationNode binary -> resolveBinary(binary);
                case UnaryOperationNode unary -> resolveUnary(unary);
                case PostfixOperationNode postfix -> resolvePostfix(postfix);
                case BetweenNode between -> resolveBetween(between);
                case MembershipNode membership -> resolveMembership(membership);
                case NullCoalesceNode coalesce -> resolveNullCoalesce(coalesce, expectedType);
                case ConditionalNode conditional -> resolveConditional(conditional, expectedType);
                case FunctionCallNode functionCall -> resolveFunctionCall(functionCall);
                case NavigationChainNode navigation -> resolveNavigationChain(navigation);
                case CurrentItemNode currentItem -> resolveCurrentItem(currentItem);
                case CurrentTemporalValueNode currentTemporalValue -> resolveCurrentTemporalValue(currentTemporalValue);
                case IdentifierNode identifier -> resolveIdentifier(identifier);
                case GroupedExpressionNode grouped -> resolveGrouped(grouped, expectedType);
            };
        }

        private Resolution resolveGrouped(GroupedExpressionNode grouped, ExpressionType expectedType) {
            Resolution resolution = resolveExpression(grouped.expression(), expectedType);
            if (resolution.known()) {
                recordValue(grouped.id(), resolution.type(), nullabilityOf(grouped.expression().id()));
                recordPure(grouped.id(), purityOf(grouped.expression().id()));
                recordCollectionShape(grouped.id(), resolution.type(), collectionShapes.get(grouped.expression().id()));
            }
            return resolution;
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
            recordPure(literal.id(), true);
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
                recordPure(collection.id(), true);
                return Resolution.known(type);
            }

            ExpressionType elementType = expectedElementType;
            List<ExpressionNode> pendingElements = new ArrayList<>();
            boolean anyElementNullable = false;
            for (ExpressionNode element : collection.elements()) {
                Resolution elementResolution = resolveExpression(element, elementType);
                if (elementResolution.invalid()) {
                    continue;
                }
                if (elementResolution.pending()) {
                    pendingElements.add(element);
                    continue;
                }
                anyElementNullable |= rejectIfNullable(
                        element,
                        DiagnosticCode.SEMANTIC_NULLABLE_OPERAND_NOT_ALLOWED,
                        "Collection literal element may be null at runtime");
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
                Resolution pendingResolution = resolveExpression(pendingElement, elementType);
                if (pendingResolution.known()) {
                    anyElementNullable |= rejectIfNullable(
                            pendingElement,
                            DiagnosticCode.SEMANTIC_NULLABLE_OPERAND_NOT_ALLOWED,
                            "Collection literal element may be null at runtime");
                }
            }
            if (anyElementNullable) {
                return Resolution.invalidResolution();
            }
            CollectionType collectionType = new CollectionType(elementType);
            recordValue(collection.id(), collectionType);
            recordPure(collection.id(), allPure(collection.elements()));
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
            if (rejectNullableOperands(binary.left(), binary.right())) {
                return Resolution.invalidResolution();
            }
            recordValue(binary.id(), resultType);
            recordPure(binary.id(), purityOf(binary.left().id()) && purityOf(binary.right().id()));
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
            if (rejectNullableOperands(binary.left(), binary.right())) {
                return Resolution.invalidResolution();
            }
            recordValue(binary.id(), ScalarType.BOOLEAN);
            recordPure(binary.id(), purityOf(binary.left().id()) && purityOf(binary.right().id()));
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
            if (rejectNullableOperands(binary.left(), binary.right())) {
                return Resolution.invalidResolution();
            }
            equalityOperandTypes.put(binary.id(), left.type());
            recordValue(binary.id(), ScalarType.BOOLEAN);
            recordPure(binary.id(), purityOf(binary.left().id()) && purityOf(binary.right().id()));
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
            if (rejectNullableOperands(binary.left(), binary.right())) {
                return Resolution.invalidResolution();
            }
            try {
                preparedValues.put(binary.id(), Pattern.compile((String) preparedValues.get(binary.right().id())));
            } catch (PatternSyntaxException exception) {
                diagnostic(DiagnosticCode.SEMANTIC_REGEX_PATTERN_INVALID, "Invalid regex pattern", binary.right().sourceSpan());
                return Resolution.invalidResolution();
            }
            recordValue(binary.id(), ScalarType.BOOLEAN);
            recordPure(binary.id(), purityOf(binary.left().id()) && purityOf(binary.right().id()));
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
            if (rejectNullableOperands(unary.operand())) {
                return Resolution.invalidResolution();
            }
            recordValue(unary.id(), operandType);
            recordPure(unary.id(), purityOf(unary.operand().id()));
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
            if (rejectNullableOperands(postfix.operand())) {
                return Resolution.invalidResolution();
            }
            recordValue(postfix.id(), ScalarType.NUMBER);
            recordPure(postfix.id(), purityOf(postfix.operand().id()));
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
            if (rejectNullableOperands(between.value(), between.lowerBound(), between.upperBound())) {
                return Resolution.invalidResolution();
            }
            recordValue(between.id(), ScalarType.BOOLEAN);
            recordPure(between.id(), purityOf(between.value().id())
                    && purityOf(between.lowerBound().id())
                    && purityOf(between.upperBound().id()));
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
            if (rejectNullableOperands(membership.element(), membership.collection())) {
                return Resolution.invalidResolution();
            }
            recordValue(membership.id(), ScalarType.BOOLEAN);
            recordPure(membership.id(), purityOf(membership.element().id()) && purityOf(membership.collection().id()));
            return Resolution.known(ScalarType.BOOLEAN);
        }

        private Resolution resolveNullCoalesce(NullCoalesceNode coalesce, ExpressionType expectedType) {
            ExpressionType resultType = expectedType;
            List<ExpressionNode> pending = new ArrayList<>();
            boolean anyOperandNeverNull = false;
            for (ExpressionNode operand : coalesce.operands()) {
                Resolution resolution = resolveExpression(operand, resultType);
                if (resolution.invalid()) {
                    continue;
                }
                if (resolution.pending()) {
                    pending.add(operand);
                    continue;
                }
                anyOperandNeverNull |= nullabilityOf(operand.id()) == RuntimeNullability.NEVER_NULL;
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
                Resolution resolution = resolveExpression(operand, resultType);
                if (resolution.known()) {
                    anyOperandNeverNull |= nullabilityOf(operand.id()) == RuntimeNullability.NEVER_NULL;
                }
            }
            recordValue(
                    coalesce.id(),
                    resultType,
                    anyOperandNeverNull ? RuntimeNullability.NEVER_NULL : RuntimeNullability.MAY_BE_NULL);
            recordPure(coalesce.id(), allPure(coalesce.operands()));
            return Resolution.known(resultType);
        }

        private Resolution resolveConditional(ConditionalNode conditional, ExpressionType expectedType) {
            ExpressionType resultType = expectedType;
            List<ExpressionNode> pendingConsequences = new ArrayList<>();
            boolean anyBranchMayBeNull = false;
            for (ConditionalBranchNode branch : conditional.branches()) {
                Resolution condition = resolveExpression(branch.condition(), ScalarType.BOOLEAN);
                if (!condition.invalid() && (!condition.known() || condition.type() != ScalarType.BOOLEAN)) {
                    diagnostic(
                            DiagnosticCode.SEMANTIC_OPERATOR_TYPE_MISMATCH,
                            "Conditional branch condition must be boolean",
                            branch.condition().sourceSpan());
                } else if (condition.known()) {
                    rejectIfNullable(
                            branch.condition(),
                            DiagnosticCode.SEMANTIC_NULLABLE_PREDICATE_NOT_ALLOWED,
                            "Conditional branch condition may be null at runtime");
                }
                Resolution consequence = resolveExpression(branch.consequence(), resultType);
                resultType = mergeConditionalType(resultType, consequence, pendingConsequences, branch.consequence());
                if (consequence.known()) {
                    anyBranchMayBeNull |= nullabilityOf(branch.consequence().id()) == RuntimeNullability.MAY_BE_NULL;
                }
            }
            Resolution elseResolution = resolveExpression(conditional.elseExpression(), resultType);
            resultType = mergeConditionalType(resultType, elseResolution, pendingConsequences, conditional.elseExpression());
            if (elseResolution.known()) {
                anyBranchMayBeNull |= nullabilityOf(conditional.elseExpression().id()) == RuntimeNullability.MAY_BE_NULL;
            }
            if (resultType == null) {
                return pendingConsequences.isEmpty()
                        ? Resolution.invalidResolution()
                        : Resolution.pendingResolution(pendingConsequences.getFirst().sourceSpan());
            }
            for (ExpressionNode pendingConsequence : pendingConsequences) {
                Resolution resolution = resolveExpression(pendingConsequence, resultType);
                if (resolution.known()) {
                    anyBranchMayBeNull |= nullabilityOf(pendingConsequence.id()) == RuntimeNullability.MAY_BE_NULL;
                }
            }
            recordValue(
                    conditional.id(),
                    resultType,
                    anyBranchMayBeNull ? RuntimeNullability.MAY_BE_NULL : RuntimeNullability.NEVER_NULL);
            recordPure(conditional.id(), conditionalPure(conditional));
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
                if (rejectIfNullable(
                        expressionArgument.expression(),
                        DiagnosticCode.SEMANTIC_NULLABLE_ARGUMENT_NOT_ALLOWED,
                        "Function argument may be null at runtime")) {
                    invalid = true;
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
            recordPure(functionCall.id(), descriptor.pure() && functionArgumentsPure(functionCall.arguments()));
            return Resolution.known(descriptor.returnType());
        }

        private Resolution resolveNavigationChain(NavigationChainNode navigation) {
            Resolution receiver = resolveExpression(navigation.receiver(), initialNavigationReceiverContext(navigation));
            if (receiver.invalid() || receiver.pending()) {
                return invalidForPending(receiver);
            }

            ExpressionType currentType = receiver.type();
            CollectionShape currentShape = collectionShapes.get(navigation.receiver().id());
            RuntimeNullability currentNullability = nullabilityOf(navigation.receiver().id());
            SourceSpan currentReceiverSpan = navigation.receiver().sourceSpan();
            boolean currentPure = purityOf(navigation.receiver().id());
            ContextualItemMembers currentContextualMembers = contextualItemMembers(navigation.receiver(), currentType);
            for (NavigationLink link : navigation.links()) {
                if (!link.safe() && currentNullability == RuntimeNullability.MAY_BE_NULL) {
                    nullableDiagnostic(
                            DiagnosticCode.SEMANTIC_NULLABLE_RECEIVER_NOT_ALLOWED,
                            "Navigation receiver may be null at runtime",
                            currentReceiverSpan);
                    return Resolution.invalidResolution();
                }
                LinkResolution linkResolution = resolveNavigationLink(
                        link, currentType, currentShape, currentPure, currentContextualMembers);
                if (linkResolution.invalid()) {
                    return Resolution.invalidResolution();
                }
                currentType = linkResolution.type();
                currentShape = linkResolution.shape();
                currentNullability = linkResolution.nullability();
                currentPure = linkResolution.pure();
                currentContextualMembers = linkResolution.contextualItemMembers();
                currentReceiverSpan = link.sourceSpan();
                recordValue(link.id(), currentType, currentNullability);
                recordPure(link.id(), currentPure);
                recordCollectionShape(link.id(), currentType, currentShape);
            }
            recordValue(navigation.id(), currentType, currentNullability);
            recordPure(navigation.id(), currentPure);
            recordCollectionShape(navigation.id(), currentType, currentShape);
            return Resolution.known(currentType);
        }

        private ExpressionType initialNavigationReceiverContext(NavigationChainNode navigation) {
            NavigationLink firstLink = navigation.links().getFirst();
            if (!(firstLink instanceof CallNavigationLink call)
                    || !emptyCollectionReceiver(navigation.receiver())) {
                return null;
            }
            CollectionOperationCatalog.Descriptor descriptor = environment.collectionOperations()
                    .find(call.memberName().value())
                    .orElse(null);
            if (descriptor == null) {
                return null;
            }
            return switch (descriptor.identity()) {
                case SUM, AVG, COUNT -> new CollectionType(ScalarType.NUMBER);
                case ALL, ANY, KEYS, MAP, REDUCE, SORT_BY, VALUES, CUSTOM -> null;
            };
        }

        private static boolean emptyCollectionReceiver(ExpressionNode receiver) {
            if (receiver instanceof CollectionLiteralNode collection) {
                return collection.elements().isEmpty();
            }
            return receiver instanceof GroupedExpressionNode grouped && emptyCollectionReceiver(grouped.expression());
        }

        private LinkResolution resolveNavigationLink(
                NavigationLink link,
                ExpressionType receiverType,
                CollectionShape receiverShape,
                boolean receiverPure,
                ContextualItemMembers contextualMembers) {
            if (link instanceof IndexSubscriptNavigationLink index) {
                return resolveIndexSubscript(index, receiverType, receiverShape, receiverPure);
            }
            if (link instanceof SliceSubscriptNavigationLink slice) {
                return resolveSliceSubscript(slice, receiverType, receiverShape, receiverPure);
            }
            if (link instanceof StringKeySubscriptNavigationLink stringKey) {
                return resolveStringKeySubscript(stringKey, receiverType, receiverPure);
            }
            if (link instanceof FilterNavigationLink filter) {
                return resolveFilter(filter, receiverType, receiverPure);
            }
            if (link instanceof WildcardNavigationLink wildcard) {
                return resolveWildcard(wildcard, receiverType, receiverShape, receiverPure);
            }
            if (link instanceof PropertyNavigationLink property) {
                return resolveProperty(property, receiverType, receiverPure, contextualMembers);
            }
            if (link instanceof CallNavigationLink call) {
                if (receiverType instanceof ObjectType objectType) {
                    return resolveRegisteredMethod(call, objectType, receiverPure);
                }
                return resolveCollectionOperation(call, receiverType, receiverShape, receiverPure);
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
                CollectionShape receiverShape,
                boolean receiverPure) {
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
            navigationBindings.put(index.id(), new IndexSubscriptNavigationBinding(
                    collectionType.elementType(), RuntimeNullability.NEVER_NULL, receiverPure));
            return LinkResolution.known(collectionType.elementType(), null, RuntimeNullability.NEVER_NULL, receiverPure);
        }

        private LinkResolution resolveSliceSubscript(
                SliceSubscriptNavigationLink slice,
                ExpressionType receiverType,
                CollectionShape receiverShape,
                boolean receiverPure) {
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
            navigationBindings.put(slice.id(), new SliceSubscriptNavigationBinding(
                    collectionType.elementType(), RuntimeNullability.NEVER_NULL, receiverPure));
            return LinkResolution.known(
                    new CollectionType(collectionType.elementType()), shape, RuntimeNullability.NEVER_NULL, receiverPure);
        }

        private LinkResolution resolveStringKeySubscript(
                StringKeySubscriptNavigationLink stringKey,
                ExpressionType receiverType,
                boolean receiverPure) {
            if (!(receiverType instanceof MapType mapType)) {
                diagnostic(
                        DiagnosticCode.SEMANTIC_OPERATOR_TYPE_MISMATCH,
                        "String key subscript requires a map receiver",
                        stringKey.sourceSpan());
                return LinkResolution.invalidResolution();
            }
            RuntimeNullability nullability = stringKey.safe()
                    ? RuntimeNullability.MAY_BE_NULL
                    : RuntimeNullability.NEVER_NULL;
            CollectionShape shape = mapType.valueType() instanceof CollectionType
                    ? CollectionShape.unknown()
                    : null;
            navigationBindings.put(stringKey.id(), new MapKeySubscriptNavigationBinding(
                    mapType.valueType(), nullability, receiverPure));
            return LinkResolution.known(mapType.valueType(), shape, nullability, receiverPure);
        }

        private LinkResolution resolveFilter(FilterNavigationLink filter, ExpressionType receiverType, boolean receiverPure) {
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
            if (rejectIfNullable(
                    filter.predicate(),
                    DiagnosticCode.SEMANTIC_NULLABLE_PREDICATE_NOT_ALLOWED,
                    "Filter predicate may be null at runtime")) {
                return LinkResolution.invalidResolution();
            }
            boolean filterPure = receiverPure && purityOf(filter.predicate().id());
            navigationBindings.put(filter.id(), new FilterNavigationBinding(
                    collectionType.elementType(), RuntimeNullability.NEVER_NULL, currentItem.frameSlot(), filterPure));
            return LinkResolution.known(
                    new CollectionType(collectionType.elementType()),
                    CollectionShape.unknown(),
                    RuntimeNullability.NEVER_NULL,
                    filterPure);
        }

        private LinkResolution resolveWildcard(
                WildcardNavigationLink wildcard,
                ExpressionType receiverType,
                CollectionShape receiverShape,
                boolean receiverPure) {
            ExpressionType elementType;
            WildcardNavigationBinding.ReceiverKind receiverKind;
            CollectionShape resultShape = CollectionShape.unknown();
            List<com.runestone.expeval_mk3.api.JavaWildcardChildDescriptor> objectChildren = List.of();
            if (receiverType instanceof CollectionType collectionType) {
                receiverKind = WildcardNavigationBinding.ReceiverKind.COLLECTION;
                elementType = collectionType.elementType();
                resultShape = receiverShape == null ? CollectionShape.unknown() : receiverShape;
            } else if (receiverType instanceof MapType mapType) {
                receiverKind = WildcardNavigationBinding.ReceiverKind.MAP;
                elementType = mapType.valueType();
            } else if (receiverType instanceof ObjectType objectType) {
                receiverKind = WildcardNavigationBinding.ReceiverKind.OBJECT;
                JavaTypeDescriptor descriptor = environment.javaTypes().find(objectType).orElse(null);
                if (descriptor == null || descriptor.wildcardChildType().isEmpty()) {
                    diagnostic(
                            DiagnosticCode.SEMANTIC_OPERATOR_TYPE_MISMATCH,
                            "Object wildcard navigation requires explicitly registered homogeneous child members",
                            wildcard.sourceSpan());
                    return LinkResolution.invalidResolution();
                }
                objectChildren = descriptor.wildcardChildren();
                if (objectChildren.size() > environment.maxMaterializedSize()) {
                    diagnostic(
                            DiagnosticCode.SEMANTIC_MATERIALIZATION_LIMIT_EXCEEDED,
                            "Object wildcard navigation exceeds maxMaterializedSize "
                                    + environment.maxMaterializedSize(),
                            wildcard.sourceSpan());
                    return LinkResolution.invalidResolution();
                }
                elementType = descriptor.wildcardChildType().orElseThrow();
                resultShape = new CollectionShape(objectChildren.size());
            } else {
                diagnostic(
                        DiagnosticCode.SEMANTIC_OPERATOR_TYPE_MISMATCH,
                        "Wildcard navigation requires a collection, map, or registered object receiver",
                        wildcard.sourceSpan());
                return LinkResolution.invalidResolution();
            }
            RuntimeNullability resultNullability = wildcard.safe()
                    ? RuntimeNullability.MAY_BE_NULL
                    : RuntimeNullability.NEVER_NULL;
            navigationBindings.put(wildcard.id(), new WildcardNavigationBinding(
                    receiverKind,
                    receiverType,
                    elementType,
                    resultNullability,
                    resultShape,
                    CollectionOperationCatalog.MaterializationPolicy.MATERIALIZES,
                    receiverPure,
                    objectChildren));
            return LinkResolution.known(
                    new CollectionType(elementType),
                    resultShape,
                    resultNullability,
                    receiverPure);
        }

        private LinkResolution resolveProperty(
                PropertyNavigationLink property,
                ExpressionType receiverType,
                boolean receiverPure,
                ContextualItemMembers contextualMembers) {
            if (receiverType == MAP_ENTRY_TYPE) {
                ContextualMemberNavigationBinding.Member member = switch (property.memberName().value()) {
                    case MAP_ENTRY_KEY_MEMBER -> ContextualMemberNavigationBinding.Member.MAP_ENTRY_KEY;
                    case MAP_ENTRY_VALUE_MEMBER -> ContextualMemberNavigationBinding.Member.MAP_ENTRY_VALUE;
                    default -> null;
                };
                if (member == null) {
                    diagnostic(
                            DiagnosticCode.SEMANTIC_UNKNOWN_SYMBOL,
                            "Map entry exposes only @.k and @.v",
                            property.sourceSpan());
                    return LinkResolution.invalidResolution();
                }
                ExpressionType resultType = member == ContextualMemberNavigationBinding.Member.MAP_ENTRY_KEY
                        ? ScalarType.STRING
                        : Objects.requireNonNull(contextualMembers.mapEntryValueType(), "mapEntryValueType");
                RuntimeNullability nullability = property.safe()
                        ? RuntimeNullability.MAY_BE_NULL
                        : RuntimeNullability.NEVER_NULL;
                navigationBindings.put(property.id(), new ContextualMemberNavigationBinding(
                        member, resultType, nullability, receiverPure));
                return LinkResolution.known(resultType, null, nullability, receiverPure);
            }
            if (receiverType == REDUCTION_ITEM_TYPE) {
                ReductionItemTypes reductionTypes = Objects.requireNonNull(
                        contextualMembers.reductionItemTypes(), "reductionItemTypes");
                ContextualMemberNavigationBinding.Member member = switch (property.memberName().value()) {
                    case REDUCTION_ACCUMULATOR_MEMBER -> ContextualMemberNavigationBinding.Member.REDUCTION_ACCUMULATOR;
                    case REDUCTION_ITEM_MEMBER -> ContextualMemberNavigationBinding.Member.REDUCTION_ITEM;
                    default -> null;
                };
                if (member == null) {
                    diagnostic(
                            DiagnosticCode.SEMANTIC_UNKNOWN_SYMBOL,
                            "Reduction item exposes only @.accumulator and @.item",
                            property.sourceSpan());
                    return LinkResolution.invalidResolution();
                }
                ExpressionType resultType = member == ContextualMemberNavigationBinding.Member.REDUCTION_ACCUMULATOR
                        ? reductionTypes.accumulatorType()
                        : reductionTypes.itemType();
                RuntimeNullability nullability = property.safe()
                        ? RuntimeNullability.MAY_BE_NULL
                        : RuntimeNullability.NEVER_NULL;
                navigationBindings.put(property.id(), new ContextualMemberNavigationBinding(
                        member, resultType, nullability, receiverPure));
                return LinkResolution.known(resultType, null, nullability, receiverPure);
            }
            if (receiverType instanceof MapType) {
                diagnostic(
                        DiagnosticCode.SEMANTIC_OPERATOR_TYPE_MISMATCH,
                        "Map values are accessed with a textual subscript, not a property",
                        property.sourceSpan());
                return LinkResolution.invalidResolution();
            }
            if (receiverType instanceof ObjectType objectType) {
                return resolveRegisteredProperty(property, objectType, receiverPure);
            }
            diagnostic(
                    DiagnosticCode.SEMANTIC_UNSUPPORTED_EXPRESSION,
                    "Property navigation is not supported by this compilation slice",
                    property.sourceSpan());
            return LinkResolution.invalidResolution();
        }

        private LinkResolution resolveRegisteredProperty(
                PropertyNavigationLink property,
                ObjectType objectType,
                boolean receiverPure) {
            JavaTypeDescriptor descriptor = environment.javaTypes().find(objectType).orElse(null);
            JavaPropertyDescriptor propertyDescriptor = descriptor == null
                    ? null
                    : descriptor.findProperty(property.memberName().value()).orElse(null);
            if (propertyDescriptor == null) {
                diagnostic(
                        DiagnosticCode.SEMANTIC_UNKNOWN_SYMBOL,
                        "No registered property '" + property.memberName().value() + "' on " + objectType,
                        property.sourceSpan());
                return LinkResolution.invalidResolution();
            }
            RuntimeNullability nullability = property.safe()
                    ? RuntimeNullability.MAY_BE_NULL
                    : RuntimeNullability.NEVER_NULL;
            navigationBindings.put(property.id(), new RegisteredPropertyNavigationBinding(
                    objectType,
                    propertyDescriptor.type(),
                    nullability,
                    propertyDescriptor.accessorHandle(),
                    propertyDescriptor.implementationMetadata(),
                    receiverPure));
            return LinkResolution.known(propertyDescriptor.type(), null, nullability, receiverPure);
        }

        private LinkResolution resolveRegisteredMethod(
                CallNavigationLink call,
                ObjectType objectType,
                boolean receiverPure) {
            List<ExpressionType> argumentTypes = new ArrayList<>(call.arguments().size());
            boolean invalid = false;
            for (CallArgument argument : call.arguments()) {
                if (!(argument instanceof ExpressionCallArgument expressionArgument)) {
                    diagnostic(
                            DiagnosticCode.SEMANTIC_LAMBDA_ARGUMENT_UNSUPPORTED,
                            "Lambda arguments are not supported for registered object methods",
                            call.sourceSpan());
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
                if (rejectIfNullable(
                        expressionArgument.expression(),
                        DiagnosticCode.SEMANTIC_NULLABLE_ARGUMENT_NOT_ALLOWED,
                        "Registered method argument may be null at runtime")) {
                    invalid = true;
                    continue;
                }
                argumentTypes.add(argumentResolution.type());
            }
            if (invalid) {
                return LinkResolution.invalidResolution();
            }
            JavaTypeDescriptor descriptor = environment.javaTypes().find(objectType).orElse(null);
            JavaMethodDescriptor methodDescriptor = descriptor == null
                    ? null
                    : descriptor.findMethod(new FunctionSignature(call.memberName().value(), argumentTypes))
                            .orElse(null);
            if (methodDescriptor == null) {
                diagnostic(
                        DiagnosticCode.SEMANTIC_UNKNOWN_SYMBOL,
                        "No registered method '" + call.memberName().value() + "' on " + objectType
                                + " for arguments " + argumentTypes,
                        call.sourceSpan());
                return LinkResolution.invalidResolution();
            }
            RuntimeNullability nullability = call.safe()
                    ? RuntimeNullability.MAY_BE_NULL
                    : RuntimeNullability.NEVER_NULL;
            navigationBindings.put(call.id(), new RegisteredMethodNavigationBinding(
                    objectType,
                    methodDescriptor.returnType(),
                    nullability,
                    methodDescriptor.invocationHandle(),
                    methodDescriptor.implementationMetadata()));
            return LinkResolution.known(methodDescriptor.returnType(), null, nullability, false);
        }

        private LinkResolution resolveCollectionOperation(
                CallNavigationLink call,
                ExpressionType receiverType,
                CollectionShape receiverShape,
                boolean receiverPure) {
            CollectionOperationCatalog.ReceiverKind receiverKind = receiverKind(receiverType);
            if (receiverKind == null) {
                diagnostic(
                        DiagnosticCode.SEMANTIC_OPERATOR_TYPE_MISMATCH,
                        "Collection operation requires a collection or map receiver",
                        call.sourceSpan());
                return LinkResolution.invalidResolution();
            }
            CollectionOperationCatalog.Descriptor descriptor = environment.collectionOperations()
                    .find(call.memberName().value())
                    .orElse(null);
            if (descriptor == null) {
                diagnostic(
                        DiagnosticCode.SEMANTIC_UNKNOWN_FUNCTION,
                        "No collection operation registered for " + call.memberName().value(),
                        call.sourceSpan());
                return LinkResolution.invalidResolution();
            }
            if (!descriptor.receivers().contains(receiverKind)) {
                diagnostic(
                        DiagnosticCode.SEMANTIC_OPERATOR_TYPE_MISMATCH,
                        "Collection operation '" + descriptor.name() + "' is not compatible with receiver type",
                        call.sourceSpan());
                return LinkResolution.invalidResolution();
            }
            if (!supportedOperation(descriptor.identity())) {
                resolveUnsupportedOperationArguments(call);
                diagnostic(
                        DiagnosticCode.SEMANTIC_UNSUPPORTED_EXPRESSION,
                        "Collection operation '" + descriptor.name() + "' is not supported by this compilation slice",
                        call.sourceSpan());
                return LinkResolution.invalidResolution();
            }
            if (call.arguments().size() != descriptor.arguments().size()) {
                resolveUnsupportedOperationArguments(call);
                diagnostic(
                        DiagnosticCode.SEMANTIC_OPERATOR_TYPE_MISMATCH,
                        "Collection operation '" + descriptor.name() + "' does not accept these arguments",
                        call.sourceSpan());
                return LinkResolution.invalidResolution();
            }
            OperationArgumentResolution arguments = resolveOperationArguments(
                    call, descriptor, receiverType);
            if (arguments.invalid()) {
                return LinkResolution.invalidResolution();
            }
            List<CollectionOperationBinding.LambdaBinding> lambdaBindings = arguments.lambdaBindings();
            if (descriptor.receiverItemConstraint() == CollectionOperationCatalog.ReceiverItemConstraint.NUMBER_ITEM
                    && (!(receiverType instanceof CollectionType collectionType)
                    || collectionType.elementType() != ScalarType.NUMBER)) {
                diagnostic(
                        DiagnosticCode.SEMANTIC_OPERATOR_TYPE_MISMATCH,
                        "Collection operation '" + descriptor.name() + "' requires numeric collection items",
                        call.sourceSpan());
                return LinkResolution.invalidResolution();
            }
            if (descriptor.identity() == CollectionOperationCatalog.OperationIdentity.AVG
                    && receiverShape != null
                    && receiverShape.fixed()
                    && CollectionOperationWiring.isAverageOfEmptyCollectionUndefined(receiverShape.fixedSize())) {
                diagnostic(
                        DiagnosticCode.SEMANTIC_OPERATOR_TYPE_MISMATCH,
                        "Average over a known empty collection is not defined",
                        call.sourceSpan());
                return LinkResolution.invalidResolution();
            }
            ExpressionType resultType = collectionOperationResultType(
                    descriptor, receiverType, arguments.valueArgumentTypes(), lambdaBindings);
            RuntimeNullability resultNullability = call.safe()
                    ? RuntimeNullability.MAY_BE_NULL
                    : descriptor.resultNullability();
            boolean operationPure = descriptor.intrinsicPurity() == CollectionOperationCatalog.IntrinsicPurity.PURE
                    && receiverPure
                    && arguments.pure()
                    && lambdaBindingsPure(lambdaBindings);
            navigationBindings.put(call.id(), CollectionOperationBinding.fromDescriptor(
                    descriptor, receiverType, resultType, resultNullability, lambdaBindings, operationPure));
            CollectionShape resultShape = descriptor.cardinalityPreservation()
                    == CollectionOperationCatalog.CardinalityPreservation.PRESERVES_RECEIVER_CARDINALITY
                    ? receiverShape
                    : null;
            return LinkResolution.known(resultType, resultShape, resultNullability, operationPure);
        }

        private OperationArgumentResolution resolveOperationArguments(
                CallNavigationLink call,
                CollectionOperationCatalog.Descriptor descriptor,
                ExpressionType receiverType) {
            if (descriptor.arguments().isEmpty()) {
                return OperationArgumentResolution.valid(List.of(), List.of(), true);
            }
            List<CollectionOperationBinding.LambdaBinding> lambdaBindings = new ArrayList<>();
            List<ExpressionType> valueArgumentTypes = new ArrayList<>();
            boolean pure = true;
            for (int index = 0; index < descriptor.arguments().size(); index++) {
                CollectionOperationCatalog.ArgumentContract contract = descriptor.arguments().get(index);
                CallArgument argument = call.arguments().get(index);
                if (contract.kind() == CollectionOperationCatalog.ArgumentKind.VALUE) {
                    ValueArgumentResolution valueResolution = resolveValueArgument(
                            call, descriptor, argument, contract, valueArgumentTypes);
                    if (valueResolution.invalid()) {
                        return OperationArgumentResolution.invalidResolution();
                    }
                    valueArgumentTypes.add(valueResolution.type());
                    pure = pure && valueResolution.pure();
                    continue;
                }
                if (!(argument instanceof LambdaCallArgument lambdaArgument)) {
                    resolveUnsupportedOperationArguments(call);
                    diagnostic(
                            DiagnosticCode.SEMANTIC_OPERATOR_TYPE_MISMATCH,
                            "Collection operation '" + descriptor.name() + "' requires a lambda argument",
                            call.sourceSpan());
                    return OperationArgumentResolution.invalidResolution();
                }
                LambdaResolution lambdaResolution = resolveLambdaArgument(
                        lambdaArgument.lambda(), contract, receiverType, valueArgumentTypes);
                if (lambdaResolution.invalid()) {
                    return OperationArgumentResolution.invalidResolution();
                }
                lambdaBindings.add(lambdaResolution.lambdaBinding());
            }
            return OperationArgumentResolution.valid(valueArgumentTypes, lambdaBindings, pure);
        }

        private ValueArgumentResolution resolveValueArgument(
                CallNavigationLink call,
                CollectionOperationCatalog.Descriptor descriptor,
                CallArgument argument,
                CollectionOperationCatalog.ArgumentContract contract,
                List<ExpressionType> valueArgumentTypes) {
            if (!(argument instanceof ExpressionCallArgument expressionArgument)) {
                diagnostic(
                        DiagnosticCode.SEMANTIC_OPERATOR_TYPE_MISMATCH,
                        "Collection operation '" + descriptor.name() + "' requires a value argument",
                        call.sourceSpan());
                return ValueArgumentResolution.invalidResolution();
            }
            ExpressionNode expression = expressionArgument.expression();
            Resolution value = resolveExpression(expression, expectedArgumentType(contract, valueArgumentTypes));
            if (value.invalid() || value.pending()) {
                invalidForPending(value);
                return ValueArgumentResolution.invalidResolution();
            }
            if (!typeMatches(contract, value.type(), valueArgumentTypes)) {
                diagnostic(
                        DiagnosticCode.SEMANTIC_OPERATOR_TYPE_MISMATCH,
                        "Collection operation value argument is not compatible with its contract",
                        expression.sourceSpan());
                return ValueArgumentResolution.invalidResolution();
            }
            if (rejectIfNullable(
                    expression,
                    DiagnosticCode.SEMANTIC_NULLABLE_ARGUMENT_NOT_ALLOWED,
                    "Collection operation value argument may be null at runtime")) {
                return ValueArgumentResolution.invalidResolution();
            }
            if (containsContextualItem(value.type())) {
                diagnostic(
                        DiagnosticCode.SEMANTIC_OPERATOR_TYPE_MISMATCH,
                        "Collection operation value argument must be a known source value",
                        expression.sourceSpan());
                return ValueArgumentResolution.invalidResolution();
            }
            if (!valueConstraintMatches(contract, expression)) {
                diagnostic(
                        DiagnosticCode.SEMANTIC_OPERATOR_TYPE_MISMATCH,
                        "Collection operation value argument violates its literal value constraint",
                        expression.sourceSpan());
                return ValueArgumentResolution.invalidResolution();
            }
            return ValueArgumentResolution.valid(value.type(), purityOf(expression.id()));
        }

        private LambdaResolution resolveLambdaArgument(
                LambdaNode lambda,
                CollectionOperationCatalog.ArgumentContract contract,
                ExpressionType receiverType,
                List<ExpressionType> valueArgumentTypes) {
            if (currentItemBindings.size() >= environment.maxCurrentItemDepth()) {
                diagnostic(
                        DiagnosticCode.SEMANTIC_CURRENT_ITEM_DEPTH_EXCEEDED,
                        "Lambda exceeds maxCurrentItemDepth " + environment.maxCurrentItemDepth(),
                        lambda.sourceSpan());
                return LambdaResolution.invalidResolution();
            }
            int depth = currentItemBindings.size();
            int currentItemSlot = currentItemFrameSlot(depth);
            SymbolBinding currentItem = SymbolBinding.internal(
                    "@" + depth,
                    currentItemType(contract.currentItemTypeRule(), receiverType, currentItemSlot, valueArgumentTypes),
                    currentItemSlot);
            symbolBindings.put(lambda.currentItem().id(), currentItem);
            currentItemBindings.add(currentItem);
            Resolution body = resolveExpression(lambda.body(), expectedArgumentType(contract, valueArgumentTypes));
            currentItemBindings.remove(currentItemBindings.size() - 1);
            if (body.invalid() || body.pending()) {
                invalidForPending(body);
                return LambdaResolution.invalidResolution();
            }
            if (!typeMatches(contract, body.type(), valueArgumentTypes)) {
                diagnostic(
                        DiagnosticCode.SEMANTIC_OPERATOR_TYPE_MISMATCH,
                        "Lambda result is not compatible with collection operation contract",
                        lambda.body().sourceSpan());
                return LambdaResolution.invalidResolution();
            }
            if (rejectIfNullable(
                    lambda.body(),
                    DiagnosticCode.SEMANTIC_NULLABLE_ARGUMENT_NOT_ALLOWED,
                    "Lambda result may be null at runtime")) {
                return LambdaResolution.invalidResolution();
            }
            if (containsContextualItem(body.type())) {
                diagnostic(
                        DiagnosticCode.SEMANTIC_OPERATOR_TYPE_MISMATCH,
                        "Lambda result must be a known source value",
                        lambda.body().sourceSpan());
                return LambdaResolution.invalidResolution();
            }
            RuntimeNullability bodyNullability = nullabilityOf(lambda.body().id());
            return LambdaResolution.valid(new CollectionOperationBinding.LambdaBinding(
                    lambda, body.type(), bodyNullability, currentItem.frameSlot(), purityOf(lambda.body().id())));
        }

        private static ExpressionType expectedArgumentType(
                CollectionOperationCatalog.ArgumentContract contract,
                List<ExpressionType> valueArgumentTypes) {
            return switch (contract.typeRule()) {
                case BOOLEAN -> ScalarType.BOOLEAN;
                case STRING -> ScalarType.STRING;
                case SAME_AS_ARGUMENT_0 -> valueArgumentTypes.getFirst();
                case ANY_KNOWN_TYPE, ORDERABLE_SCALAR -> null;
            };
        }

        private static boolean typeMatches(
                CollectionOperationCatalog.ArgumentContract contract,
                ExpressionType type,
                List<ExpressionType> valueArgumentTypes) {
            return switch (contract.typeRule()) {
                case ANY_KNOWN_TYPE -> true;
                case BOOLEAN -> type == ScalarType.BOOLEAN;
                case STRING -> type == ScalarType.STRING;
                case ORDERABLE_SCALAR -> contract.typeRule().concreteTypes().contains(type);
                case SAME_AS_ARGUMENT_0 -> type.equals(valueArgumentTypes.getFirst());
            };
        }

        private static boolean valueConstraintMatches(
                CollectionOperationCatalog.ArgumentContract contract,
                ExpressionNode expression) {
            if (contract.valueConstraint() == CollectionOperationCatalog.ValueConstraint.NONE_REQUIRED) {
                return true;
            }
            if (!(expression instanceof LiteralNode literal) || !(literal.value() instanceof StringLiteralValue value)) {
                return false;
            }
            return contract.valueConstraint().allowedTextValues().contains(value.value());
        }

        private ExpressionType currentItemType(
                CollectionOperationCatalog.CurrentItemTypeRule rule,
                ExpressionType receiverType,
                int currentItemSlot,
                List<ExpressionType> valueArgumentTypes) {
            if (rule == CollectionOperationCatalog.CurrentItemTypeRule.RECEIVER_ITEM_OR_MAP_ENTRY) {
                if (receiverType instanceof CollectionType collectionType) {
                    return collectionType.elementType();
                }
                if (receiverType instanceof MapType mapType) {
                    mapEntryValueTypesByFrameSlot.put(currentItemSlot, mapType.valueType());
                    return MAP_ENTRY_TYPE;
                }
            }
            if (rule == CollectionOperationCatalog.CurrentItemTypeRule.REDUCTION_ITEM
                    && receiverType instanceof CollectionType collectionType) {
                reductionItemTypesByFrameSlot.put(currentItemSlot,
                        new ReductionItemTypes(valueArgumentTypes.getFirst(), collectionType.elementType()));
                return REDUCTION_ITEM_TYPE;
            }
            throw new IllegalArgumentException("unsupported current item type rule: " + rule);
        }

        private ContextualItemMembers contextualItemMembers(ExpressionNode expression, ExpressionType type) {
            if (type != MAP_ENTRY_TYPE && type != REDUCTION_ITEM_TYPE) {
                return ContextualItemMembers.none();
            }
            if (expression instanceof GroupedExpressionNode grouped) {
                return contextualItemMembers(grouped.expression(), type);
            }
            SymbolBinding binding = symbolBindings.get(expression.id());
            if (binding == null) {
                return ContextualItemMembers.none();
            }
            if (type == MAP_ENTRY_TYPE) {
                return new ContextualItemMembers(mapEntryValueTypesByFrameSlot.get(binding.frameSlot()), null);
            }
            return new ContextualItemMembers(null, reductionItemTypesByFrameSlot.get(binding.frameSlot()));
        }

        private void resolveUnsupportedOperationArguments(CallNavigationLink call) {
            for (CallArgument argument : call.arguments()) {
                if (argument instanceof ExpressionCallArgument expressionArgument) {
                    resolveExpression(expressionArgument.expression(), null);
                    continue;
                }
                diagnostic(
                        DiagnosticCode.SEMANTIC_LAMBDA_ARGUMENT_UNSUPPORTED,
                        "Lambda arguments are not supported by this collection operation slice",
                        call.sourceSpan());
            }
        }

        private static CollectionOperationCatalog.ReceiverKind receiverKind(ExpressionType receiverType) {
            if (receiverType instanceof CollectionType) {
                return CollectionOperationCatalog.ReceiverKind.COLLECTION;
            }
            if (receiverType instanceof MapType) {
                return CollectionOperationCatalog.ReceiverKind.MAP;
            }
            return null;
        }

        private static boolean supportedOperation(CollectionOperationCatalog.OperationIdentity identity) {
            return CollectionOperationWiring.semanticsSupported(identity);
        }

        private static ExpressionType collectionOperationResultType(
                CollectionOperationCatalog.Descriptor descriptor,
                ExpressionType receiverType,
                List<ExpressionType> valueArgumentTypes,
                List<CollectionOperationBinding.LambdaBinding> lambdaBindings) {
            return switch (descriptor.resultTypeRule()) {
                case BOOLEAN_RESULT -> ScalarType.BOOLEAN;
                case NUMBER_RESULT -> ScalarType.NUMBER;
                case COLLECTION_OF_MAP_KEYS -> new CollectionType(ScalarType.STRING);
                case COLLECTION_OF_MAP_VALUES -> new CollectionType(((MapType) receiverType).valueType());
                case COLLECTION_OF_LAMBDA_RESULT -> new CollectionType(lambdaBindings.getFirst().resultType());
                case SAME_AS_VALUE_ARGUMENT_0 -> valueArgumentTypes.getFirst();
                case SAME_AS_RECEIVER_COLLECTION -> receiverType;
            };
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
            recordValue(currentItem.id(), binding.type(), binding.runtimeNullability());
            recordPure(currentItem.id(), true);
            return Resolution.known(binding.type());
        }

        private Resolution resolveCurrentTemporalValue(CurrentTemporalValueNode currentTemporalValue) {
            ScalarType type = switch (currentTemporalValue.kind()) {
                case DATE -> ScalarType.DATE;
                case TIME -> ScalarType.TIME;
                case DATE_TIME -> ScalarType.DATETIME;
            };
            recordValue(currentTemporalValue.id(), type);
            recordPure(currentTemporalValue.id(), true);
            return Resolution.known(type);
        }

        private Resolution resolveIdentifier(IdentifierNode identifier) {
            SymbolBinding binding = visibleBindings.get(identifier.name());
            if (binding == null) {
                binding = bindExternalIfDeclared(identifier.name());
            }
            if (binding == null) {
                diagnostic(
                        DiagnosticCode.SEMANTIC_UNKNOWN_SYMBOL,
                        "Unknown symbol '" + identifier.name() + "'",
                        identifier.sourceSpan());
                return Resolution.invalidResolution();
            }
            symbolBindings.put(identifier.id(), binding);
            recordValue(identifier.id(), binding.type(), binding.runtimeNullability());
            recordPure(identifier.id(), true);
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

        private void recordValue(NodeId nodeId, ExpressionType type, RuntimeNullability runtimeNullability) {
            resolvedTypes.put(nodeId, type);
            nullability.put(nodeId, runtimeNullability);
        }

        private void recordPure(NodeId nodeId, boolean pure) {
            expressionPurity.put(nodeId, pure);
        }

        private boolean purityOf(NodeId nodeId) {
            return expressionPurity.getOrDefault(nodeId, true);
        }

        private boolean allPure(List<? extends ExpressionNode> expressions) {
            for (ExpressionNode expression : expressions) {
                if (!purityOf(expression.id())) {
                    return false;
                }
            }
            return true;
        }

        private boolean conditionalPure(ConditionalNode conditional) {
            for (ConditionalBranchNode branch : conditional.branches()) {
                if (!purityOf(branch.condition().id()) || !purityOf(branch.consequence().id())) {
                    return false;
                }
            }
            return purityOf(conditional.elseExpression().id());
        }

        private boolean functionArgumentsPure(List<CallArgument> arguments) {
            for (CallArgument argument : arguments) {
                ExpressionCallArgument expressionArgument = (ExpressionCallArgument) argument;
                if (!purityOf(expressionArgument.expression().id())) {
                    return false;
                }
            }
            return true;
        }

        private static boolean lambdaBindingsPure(List<CollectionOperationBinding.LambdaBinding> lambdaBindings) {
            for (CollectionOperationBinding.LambdaBinding lambdaBinding : lambdaBindings) {
                if (!lambdaBinding.pure()) {
                    return false;
                }
            }
            return true;
        }

        private RuntimeNullability nullabilityOf(NodeId nodeId) {
            RuntimeNullability value = nullability.get(nodeId);
            if (value == null) {
                throw new IllegalStateException("no runtime nullability recorded for " + nodeId);
            }
            return value;
        }

        private boolean rejectNullableOperands(ExpressionNode... operands) {
            boolean rejected = false;
            for (ExpressionNode operand : operands) {
                rejected |= rejectIfNullable(
                        operand,
                        DiagnosticCode.SEMANTIC_NULLABLE_OPERAND_NOT_ALLOWED,
                        "Operator operand may be null at runtime");
            }
            return rejected;
        }

        private boolean rejectIfNullable(ExpressionNode expression, DiagnosticCode code, String message) {
            if (nullabilityOf(expression.id()) != RuntimeNullability.MAY_BE_NULL) {
                return false;
            }
            nullableDiagnostic(code, message, expression.sourceSpan());
            return true;
        }

        private void nullableDiagnostic(DiagnosticCode code, String message, SourceSpan span) {
            diagnostics.add(ExpressionDiagnostic.builder(DiagnosticCategory.SEMANTIC, DiagnosticSeverity.ERROR, code.name(), message)
                    .primarySpan(span)
                    .suggestion("Discharge the possible null value with '??' before using it here")
                    .build());
        }

        private void recordCollectionShape(NodeId nodeId, ExpressionType type, CollectionShape shape) {
            if (type instanceof CollectionType && shape != null) {
                collectionShapes.put(nodeId, shape);
            }
        }

        private void diagnostic(DiagnosticCode code, String message, SourceSpan span) {
            diagnostics.add(ExpressionDiagnostic.error(DiagnosticCategory.SEMANTIC, code.name(), message, span));
        }

        private void warning(DiagnosticCode code, String message, SourceSpan span) {
            warnings.add(ExpressionDiagnostic.warning(DiagnosticCategory.SEMANTIC, code.name(), message, span));
        }

        private static boolean orderable(ExpressionType type) {
            return type == ScalarType.NUMBER
                    || type == ScalarType.STRING
                    || type == ScalarType.DATE
                    || type == ScalarType.TIME
                    || type == ScalarType.DATETIME;
        }

        private static boolean supportsStructuralEquality(ExpressionType type) {
            if (containsContextualItem(type)) {
                return false;
            }
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

        private static boolean containsContextualItem(ExpressionType type) {
            if (type == MAP_ENTRY_TYPE || type == REDUCTION_ITEM_TYPE) {
                return true;
            }
            if (type instanceof CollectionType collectionType) {
                return containsContextualItem(collectionType.elementType());
            }
            if (type instanceof MapType mapType) {
                return containsContextualItem(mapType.valueType());
            }
            return false;
        }

        private SymbolBinding bindExternalIfDeclared(String name) {
            return environment.externalSymbols().find(name).map(symbol -> {
                SymbolBinding binding = SymbolBinding.external(symbol, nextFrameSlot++);
                externalBindings.add(binding);
                visibleBindings.put(symbol.name(), binding);
                updateVisibleCollectionShape(symbol.name(), symbol.type(), CollectionShape.unknown());
                return binding;
            }).orElse(null);
        }
    }

    private record LiteralResolution(ExpressionType type, Object value) {
    }

    private record LinkResolution(
            ExpressionType type,
            CollectionShape shape,
            RuntimeNullability nullability,
            ContextualItemMembers contextualItemMembers,
            boolean pure,
            boolean invalid) {

        private static LinkResolution known(ExpressionType type, CollectionShape shape) {
            return known(type, shape, RuntimeNullability.NEVER_NULL);
        }

        private static LinkResolution known(
                ExpressionType type,
                CollectionShape shape,
                RuntimeNullability nullability) {
            return known(type, shape, nullability, true);
        }

        private static LinkResolution known(
                ExpressionType type,
                CollectionShape shape,
                RuntimeNullability nullability,
                boolean pure) {
            return new LinkResolution(
                    Objects.requireNonNull(type, "type"),
                    shape,
                    Objects.requireNonNull(nullability, "nullability"),
                    ContextualItemMembers.none(),
                    pure,
                    false);
        }

        private static LinkResolution invalidResolution() {
            return new LinkResolution(null, null, RuntimeNullability.NEVER_NULL, ContextualItemMembers.none(), true, true);
        }
    }

    private record OperationArgumentResolution(
            List<ExpressionType> valueArgumentTypes,
            List<CollectionOperationBinding.LambdaBinding> lambdaBindings,
            boolean pure,
            boolean invalid) {

        private OperationArgumentResolution {
            valueArgumentTypes = List.copyOf(Objects.requireNonNull(valueArgumentTypes, "valueArgumentTypes"));
            lambdaBindings = List.copyOf(Objects.requireNonNull(lambdaBindings, "lambdaBindings"));
        }

        private static OperationArgumentResolution valid(
                List<ExpressionType> valueArgumentTypes,
                List<CollectionOperationBinding.LambdaBinding> lambdaBindings,
                boolean pure) {
            return new OperationArgumentResolution(valueArgumentTypes, lambdaBindings, pure, false);
        }

        private static OperationArgumentResolution invalidResolution() {
            return new OperationArgumentResolution(List.of(), List.of(), true, true);
        }
    }

    private record ValueArgumentResolution(ExpressionType type, boolean pure, boolean invalid) {

        private static ValueArgumentResolution valid(ExpressionType type, boolean pure) {
            return new ValueArgumentResolution(Objects.requireNonNull(type, "type"), pure, false);
        }

        private static ValueArgumentResolution invalidResolution() {
            return new ValueArgumentResolution(null, true, true);
        }
    }

    private record ContextualItemMembers(
            ExpressionType mapEntryValueType,
            ReductionItemTypes reductionItemTypes) {

        private static ContextualItemMembers none() {
            return new ContextualItemMembers(null, null);
        }
    }

    private record ReductionItemTypes(ExpressionType accumulatorType, ExpressionType itemType) {

        private ReductionItemTypes {
            Objects.requireNonNull(accumulatorType, "accumulatorType");
            Objects.requireNonNull(itemType, "itemType");
        }
    }

    private record LambdaResolution(
            CollectionOperationBinding.LambdaBinding lambdaBinding,
            boolean invalid) {

        private static LambdaResolution valid(CollectionOperationBinding.LambdaBinding lambdaBinding) {
            return new LambdaResolution(Objects.requireNonNull(lambdaBinding, "lambdaBinding"), false);
        }

        private static LambdaResolution invalidResolution() {
            return new LambdaResolution(null, true);
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
