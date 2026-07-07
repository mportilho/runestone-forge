package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.api.ExpressionType;
import com.runestone.expeval_mk3.api.NullType;
import com.runestone.expeval_mk3.api.ScalarType;
import com.runestone.expeval_mk3.api.UnknownType;
import com.runestone.expeval_mk3.api.VectorType;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

final class SemanticSymbolFlowBuilder {

    private final Map<NodeId, ExpressionType> knownExpressionTypes = new LinkedHashMap<>();
    private final List<CurrentItemSource> currentItemSources = new ArrayList<>();
    private final List<FunctionCallSource> functionCalls = new ArrayList<>();
    private final List<ConcreteTypeRestriction> concreteRestrictions = new ArrayList<>();
    private final List<SameTypeRestriction> sameTypeRestrictions = new ArrayList<>();
    private final List<VectorElementTypeRestriction> vectorElementTypeRestrictions = new ArrayList<>();
    private final List<MembershipTypeRestriction> membershipTypeRestrictions = new ArrayList<>();
    private final List<RegexLeftOperandRestriction> regexLeftOperandRestrictions = new ArrayList<>();
    private final List<RegexPatternSource> regexPatternSources = new ArrayList<>();
    private final List<TypeJoinRestriction> joinRestrictions = new ArrayList<>();
    private final List<NumericConstantRestriction> numericConstantRestrictions = new ArrayList<>();
    private final List<NumericSource> numericSources = new ArrayList<>();
    private final List<OffsetDateTimeLiteralSource> offsetDateTimeLiteralSources = new ArrayList<>();
    private final Set<NodeId> emptyVectorNodes = new HashSet<>();
    private final Map<NodeId, com.runestone.expeval_mk3.internal.source.SourceSpan> sourceSpans = new LinkedHashMap<>();
    private int maxCurrentItemDepth;

    SemanticSymbolFlow build(ExpressionFileNode file) {
        List<AssignmentSymbolFlow> assignments = new ArrayList<>(file.assignments().size());
        for (AssignmentNode assignment : file.assignments()) {
            ExpressionSummary expression = collectExpression(assignment.expression(), 0);
            List<AssignedSymbolSource> targetSymbols = AssignedSymbolSource.from(assignment.target());
            if (isEmptyVectorLiteral(assignment.expression())) {
                for (AssignedSymbolSource targetSymbol : targetSymbols) {
                    emptyVectorNodes.add(targetSymbol.nodeId());
                }
            }
            assignments.add(new AssignmentSymbolFlow(
                    expression.reads(),
                    expression.rootReadOrNull(),
                    assignment.expression().id(),
                    targetSymbols,
                    expression.typeOrNull(),
                    AssignmentSymbolFlow.metadata(
                            assignment.sourceSpan(),
                            destructuringTargetSpan(assignment.target()).orElse(null),
                            knownSourceShape(assignment.expression()).orElse(null))));
        }

        List<SymbolReadSource> resultReads = file.resultExpression()
                .map(expression -> collectExpression(expression, 0).reads())
                .orElseGet(List::of);
        return new SemanticSymbolFlow(
                assignments,
                resultReads,
                functionCalls,
                knownExpressionTypes,
                new CurrentItemFlow(currentItemSources, maxCurrentItemDepth),
                new TypeRestrictionFlow(
                        concreteRestrictions,
                        sameTypeRestrictions,
                        vectorElementTypeRestrictions,
                        membershipTypeRestrictions,
                        regexLeftOperandRestrictions,
                        regexPatternSources,
                        joinRestrictions,
                        numericConstantRestrictions,
                        numericSources,
                        offsetDateTimeLiteralSources,
                        emptyVectorNodes,
                        sourceSpans));
    }

    private ExpressionSummary collectExpression(ExpressionNode expression, int currentItemDepth) {
        sourceSpans.put(expression.id(), expression.sourceSpan());
        return switch (expression) {
            case BetweenNode between -> collectBetween(between, currentItemDepth);
            case BinaryOperationNode binary -> collectBinaryOperation(binary, currentItemDepth);
            case ConditionalNode conditional -> collectConditional(conditional, currentItemDepth);
            case CurrentItemNode currentItem -> collectCurrentItem(currentItem, currentItemDepth);
            case CurrentTemporalValueNode currentTemporalValue -> known(
                    currentTemporalValue.id(),
                    currentTemporalValueType(currentTemporalValue.kind()));
            case FunctionCallNode functionCall -> collectFunctionCall(functionCall, currentItemDepth);
            case GroupedExpressionNode grouped -> collectExpression(grouped.expression(), currentItemDepth);
            case IdentifierNode identifier -> read(identifier);
            case LiteralNode literal -> collectLiteral(literal);
            case MembershipNode membership -> collectMembership(membership, currentItemDepth);
            case NavigationChainNode navigationChain -> collectNavigationChain(navigationChain, currentItemDepth);
            case NullCoalescenceNode nullCoalescence -> collectNullCoalescence(nullCoalescence, currentItemDepth);
            case PostfixOperationNode postfix -> collectPostfixOperation(postfix, currentItemDepth);
            case UnaryOperationNode unary -> collectUnaryOperation(unary, currentItemDepth);
            case VectorLiteralNode vectorLiteral -> collectVectorLiteral(vectorLiteral, currentItemDepth);
        };
    }

    private ExpressionSummary collectBinaryOperation(BinaryOperationNode binary, int currentItemDepth) {
        ExpressionSummary operands = combine(
                collectExpression(binary.left(), currentItemDepth),
                collectExpression(binary.right(), currentItemDepth));
        NodeId leftId = binary.left().id();
        NodeId rightId = binary.right().id();
        return switch (binary.operator()) {
            case ADD, SUBTRACT, MULTIPLY, DIVIDE, MODULO, ROOT, EXPONENTIATE -> {
                requireConcrete(leftId, ScalarType.NUMBER, binary.operatorSpan());
                requireConcrete(rightId, ScalarType.NUMBER, binary.operatorSpan());
                numericSources.add(new NumericSource(binary.id(), NumericSourceKind.OPERATION));
                collectInvalidNumericConstant(binary);
                yield withType(operands, binary.id(), ScalarType.NUMBER);
            }
            case LOGICAL_OR, LOGICAL_AND, LOGICAL_NAND, LOGICAL_NOR, LOGICAL_XOR, LOGICAL_XNOR -> {
                requireConcrete(leftId, ScalarType.BOOLEAN, binary.operatorSpan());
                requireConcrete(rightId, ScalarType.BOOLEAN, binary.operatorSpan());
                yield withType(operands, binary.id(), ScalarType.BOOLEAN);
            }
            case GREATER_THAN, GREATER_THAN_OR_EQUAL, LESS_THAN, LESS_THAN_OR_EQUAL -> {
                sameTypeRestrictions.add(new SameTypeRestriction(
                        List.of(leftId, rightId),
                        binary.operatorSpan(),
                        TypeCompatibilityMode.SAME_ORDERABLE_TYPE));
                yield withType(operands, binary.id(), ScalarType.BOOLEAN);
            }
            case EQUAL, NOT_EQUAL -> {
                sameTypeRestrictions.add(new SameTypeRestriction(
                        List.of(leftId, rightId),
                        binary.operatorSpan(),
                        TypeCompatibilityMode.SAME_TYPE));
                yield withType(operands, binary.id(), ScalarType.BOOLEAN);
            }
            case REGEX_MATCH, REGEX_NOT_MATCH -> {
                regexLeftOperandRestrictions.add(new RegexLeftOperandRestriction(
                        expressionTypeNodeId(binary.left()),
                        binary.left().sourceSpan()));
                requireConcrete(expressionTypeNodeId(binary.right()), ScalarType.STRING, binary.operatorSpan());
                collectRegexPatternSource(binary.right());
                yield withType(operands, binary.id(), ScalarType.BOOLEAN);
            }
            case CONCAT -> {
                requireConcrete(leftId, ScalarType.STRING, binary.operatorSpan());
                requireConcrete(rightId, ScalarType.STRING, binary.operatorSpan());
                yield withType(operands, binary.id(), ScalarType.STRING);
            }
        };
    }

    private ExpressionSummary collectConditional(ConditionalNode conditional, int currentItemDepth) {
        List<ExpressionSummary> summaries = new ArrayList<>(conditional.branches().size() * 2 + 1);
        List<NodeId> resultNodeIds = new ArrayList<>(conditional.branches().size() + 1);
        for (ConditionalBranchNode branch : conditional.branches()) {
            summaries.add(collectExpression(branch.condition(), currentItemDepth));
            requireConcrete(branch.condition().id(), ScalarType.BOOLEAN, branch.condition().sourceSpan());
            summaries.add(collectExpression(branch.resultExpression(), currentItemDepth));
            resultNodeIds.add(branch.resultExpression().id());
        }
        summaries.add(collectExpression(conditional.elseExpression(), currentItemDepth));
        resultNodeIds.add(conditional.elseExpression().id());
        joinRestrictions.add(new TypeJoinRestriction(conditional.id(), resultNodeIds, conditional.sourceSpan()));
        return combine(summaries);
    }

    private ExpressionSummary collectBetween(BetweenNode between, int currentItemDepth) {
        ExpressionSummary operands = combine(
                collectExpression(between.value(), currentItemDepth),
                collectExpression(between.lowerBound(), currentItemDepth),
                collectExpression(between.upperBound(), currentItemDepth));
        sameTypeRestrictions.add(new SameTypeRestriction(
                List.of(between.value().id(), between.lowerBound().id(), between.upperBound().id()),
                between.operatorSpan(),
                TypeCompatibilityMode.SAME_ORDERABLE_TYPE));
        return withType(operands, between.id(), ScalarType.BOOLEAN);
    }

    private ExpressionSummary collectMembership(MembershipNode membership, int currentItemDepth) {
        ExpressionSummary operands = combine(
                collectExpression(membership.value(), currentItemDepth),
                collectExpression(membership.candidates(), currentItemDepth));
        membershipTypeRestrictions.add(new MembershipTypeRestriction(
                membership.value().id(),
                membership.candidates().id(),
                membership.operatorSpan()));
        return withType(operands, membership.id(), ScalarType.BOOLEAN);
    }

    private ExpressionSummary collectCurrentItem(CurrentItemNode currentItem, int currentItemDepth) {
        currentItemSources.add(new CurrentItemSource(currentItem.id(), currentItemDepth, currentItem.sourceSpan()));
        maxCurrentItemDepth = Math.max(maxCurrentItemDepth, currentItemDepth);
        return known(currentItem.id(), UnknownType.INSTANCE);
    }

    private ExpressionSummary collectFunctionCall(FunctionCallNode functionCall, int currentItemDepth) {
        List<ExpressionNode> argumentNodes = functionCall.arguments();
        ArrayList<ExpressionSummary> arguments = new ArrayList<>(argumentNodes.size());
        ArrayList<NodeId> argumentNodeIds = new ArrayList<>(argumentNodes.size());
        for (ExpressionNode argument : argumentNodes) {
            arguments.add(collectExpression(argument, currentItemDepth));
            argumentNodeIds.add(functionArgumentTypeNodeId(argument));
        }
        functionCalls.add(new FunctionCallSource(
                functionCall.id(),
                functionCall.name().value(),
                argumentNodeIds,
                functionCall.sourceSpan()));
        return combine(arguments);
    }

    private NodeId functionArgumentTypeNodeId(ExpressionNode argument) {
        return expressionTypeNodeId(argument);
    }

    private NodeId expressionTypeNodeId(ExpressionNode expression) {
        return ungroup(expression).id();
    }

    private ExpressionNode ungroup(ExpressionNode expression) {
        ExpressionNode current = expression;
        while (current instanceof GroupedExpressionNode grouped) {
            current = grouped.expression();
        }
        return current;
    }

    private ExpressionSummary collectLiteral(LiteralNode literal) {
        collectNumericSource(literal.id(), literal.value());
        if (literal.value() instanceof OffsetDateTimeLiteralValue dateTimeValue) {
            offsetDateTimeLiteralSources.add(new OffsetDateTimeLiteralSource(literal.id(), dateTimeValue.value()));
        }
        return known(literal.id(), literalType(literal.value()));
    }

    private void collectRegexPatternSource(ExpressionNode expression) {
        ExpressionNode patternExpression = ungroup(expression);
        if (patternExpression instanceof LiteralNode literal && literal.value() instanceof StringLiteralValue stringValue) {
            regexPatternSources.add(new RegexPatternSource(
                    literal.id(),
                    stringValue.value(),
                    literal.sourceSpan()));
        }
    }

    private ExpressionSummary collectUnaryOperation(UnaryOperationNode unary, int currentItemDepth) {
        ExpressionSummary operand = collectExpression(unary.operand(), currentItemDepth);
        return switch (unary.operator()) {
            case NEGATE -> {
                requireConcrete(unary.operand().id(), ScalarType.NUMBER, unary.operatorSpan());
                numericSources.add(new NumericSource(unary.id(), NumericSourceKind.OPERATION));
                yield withType(operand, unary.id(), ScalarType.NUMBER);
            }
            case LOGICAL_NOT -> {
                requireConcrete(unary.operand().id(), ScalarType.BOOLEAN, unary.operatorSpan());
                yield withType(operand, unary.id(), ScalarType.BOOLEAN);
            }
        };
    }

    private ExpressionSummary collectPostfixOperation(PostfixOperationNode postfix, int currentItemDepth) {
        ExpressionSummary operand = collectExpression(postfix.operand(), currentItemDepth);
        for (PostfixOperatorOccurrence occurrence : postfix.operators()) {
            requireConcrete(postfix.operand().id(), ScalarType.NUMBER, occurrence.sourceSpan());
        }
        numericSources.add(new NumericSource(postfix.id(), NumericSourceKind.OPERATION));
        return withType(operand, postfix.id(), ScalarType.NUMBER);
    }

    private ExpressionSummary collectNavigationChain(NavigationChainNode navigationChain, int currentItemDepth) {
        List<ExpressionSummary> summaries = new ArrayList<>(navigationChain.links().size() + 1);
        summaries.add(collectExpression(navigationChain.receiver(), currentItemDepth));
        for (NavigationLink link : navigationChain.links()) {
            summaries.add(collectNavigationLink(link, currentItemDepth));
        }
        return combine(summaries);
    }

    private ExpressionSummary collectNavigationLink(NavigationLink link, int currentItemDepth) {
        return switch (link) {
            case CollectionOperationNavigationLink collectionOperation -> combine(collectionOperation.arguments().stream()
                    .map(argument -> collectCollectionOperationArgument(argument, currentItemDepth))
                    .toList());
            case FilterNavigationLink filter -> collectCurrentItemContext(filter.predicate(), currentItemDepth + 1);
            case MethodNavigationLink method -> combine(method.arguments().stream()
                    .map(argument -> collectExpression(argument, currentItemDepth))
                    .toList());
            case PropertyNavigationLink ignored -> ExpressionSummary.empty();
            case SubscriptNavigationLink ignored -> ExpressionSummary.empty();
            case WildcardNavigationLink ignored -> ExpressionSummary.empty();
        };
    }

    private ExpressionSummary collectCollectionOperationArgument(
            CollectionOperationArgument argument,
            int currentItemDepth) {
        return switch (argument) {
            case LambdaCollectionOperationArgument lambdaArgument -> collectCurrentItemContext(
                    lambdaArgument.lambda().body(),
                    currentItemDepth + 1);
            case PositionalCollectionOperationArgument positional -> collectExpression(
                    positional.expression(),
                    currentItemDepth);
        };
    }

    private ExpressionSummary collectCurrentItemContext(ExpressionNode expression, int currentItemDepth) {
        maxCurrentItemDepth = Math.max(maxCurrentItemDepth, currentItemDepth);
        return collectExpression(expression, currentItemDepth);
    }

    private ExpressionSummary collectVectorLiteral(VectorLiteralNode vectorLiteral, int currentItemDepth) {
        List<ExpressionSummary> elements = new ArrayList<>(vectorLiteral.elements().size());
        for (ExpressionNode element : vectorLiteral.elements()) {
            elements.add(collectExpression(element, currentItemDepth));
        }
        if (elements.isEmpty()) {
            emptyVectorNodes.add(vectorLiteral.id());
        }
        vectorElementTypeRestrictions.add(new VectorElementTypeRestriction(
                vectorLiteral.id(),
                vectorLiteral.elements().stream().map(ExpressionNode::id).toList(),
                vectorLiteral.sourceSpan()));
        ExpressionType elementType = commonElementType(elements);
        return withType(combine(elements), vectorLiteral.id(), new VectorType(elementType));
    }

    private ExpressionSummary collectNullCoalescence(NullCoalescenceNode nullCoalescence, int currentItemDepth) {
        List<ExpressionSummary> operands = new ArrayList<>(nullCoalescence.operands().size());
        for (ExpressionNode operand : nullCoalescence.operands()) {
            operands.add(collectExpression(operand, currentItemDepth));
        }
        joinRestrictions.add(new TypeJoinRestriction(
                nullCoalescence.id(),
                nullCoalescence.operands().stream().map(ExpressionNode::id).toList(),
                nullCoalescence.sourceSpan()));
        return combine(operands);
    }

    private ExpressionSummary read(IdentifierNode identifier) {
        SymbolReadSource source = new SymbolReadSource(identifier.id(), identifier.name(), identifier.sourceSpan());
        return new ExpressionSummary(List.of(source), source, null);
    }

    private ExpressionSummary known(NodeId nodeId, ExpressionType type) {
        knownExpressionTypes.put(nodeId, type);
        return new ExpressionSummary(List.of(), null, type);
    }

    private ExpressionSummary withType(ExpressionSummary summary, NodeId nodeId, ExpressionType type) {
        knownExpressionTypes.put(nodeId, type);
        return new ExpressionSummary(summary.reads(), summary.rootReadOrNull(), type);
    }

    private void requireConcrete(
            NodeId nodeId,
            ExpressionType type,
            com.runestone.expeval_mk3.internal.source.SourceSpan sourceSpan) {
        concreteRestrictions.add(new ConcreteTypeRestriction(nodeId, type, sourceSpan));
    }

    private ExpressionSummary combine(ExpressionSummary first, ExpressionSummary... rest) {
        List<ExpressionSummary> summaries = new ArrayList<>(rest.length + 1);
        summaries.add(first);
        summaries.addAll(List.of(rest));
        return combine(summaries);
    }

    private ExpressionSummary combine(List<ExpressionSummary> summaries) {
        if (summaries.isEmpty()) {
            return ExpressionSummary.empty();
        }
        List<SymbolReadSource> reads = new ArrayList<>();
        for (ExpressionSummary summary : summaries) {
            reads.addAll(summary.reads());
        }
        return new ExpressionSummary(reads, null, null);
    }

    private ExpressionType commonElementType(List<ExpressionSummary> elements) {
        ExpressionType commonType = UnknownType.INSTANCE;
        for (ExpressionSummary element : elements) {
            if (element.type().isEmpty()) {
                return UnknownType.INSTANCE;
            }
            ExpressionType elementType = element.type().orElseThrow();
            if (commonType == UnknownType.INSTANCE) {
                commonType = elementType;
            } else if (commonType == NullType.INSTANCE) {
                commonType = elementType;
            } else if (elementType != NullType.INSTANCE && !commonType.equals(elementType)) {
                return UnknownType.INSTANCE;
            }
        }
        return commonType;
    }

    private void collectInvalidNumericConstant(BinaryOperationNode binary) {
        if ((binary.operator() == BinaryOperator.DIVIDE || binary.operator() == BinaryOperator.MODULO)
                && isZeroLiteral(binary.right())) {
            numericConstantRestrictions.add(new NumericConstantRestriction(
                    binary.operatorSpan(),
                    NumericConstantRestrictionKind.NON_ZERO_DIVISOR));
        }
        if (binary.operator() == BinaryOperator.ROOT && isZeroLiteral(binary.left())) {
            numericConstantRestrictions.add(new NumericConstantRestriction(
                    binary.operatorSpan(),
                    NumericConstantRestrictionKind.NON_ZERO_ROOT_DEGREE));
        }
    }

    private boolean isZeroLiteral(ExpressionNode expression) {
        if (!(expression instanceof LiteralNode literal)) {
            return false;
        }
        return switch (literal.value()) {
            case BigIntegerLiteralValue value -> BigInteger.ZERO.equals(value.value());
            case DecimalLiteralValue value -> BigDecimal.ZERO.compareTo(value.value()) == 0;
            case LongLiteralValue value -> value.value() == 0;
            default -> false;
        };
    }

    private boolean isEmptyVectorLiteral(ExpressionNode expression) {
        return ungroup(expression) instanceof VectorLiteralNode vectorLiteral && vectorLiteral.elements().isEmpty();
    }

    private Optional<com.runestone.expeval_mk3.internal.source.SourceSpan> destructuringTargetSpan(
            AssignmentTargetNode target) {
        if (!(target instanceof DestructuringAssignmentTargetNode destructuring)) {
            return Optional.empty();
        }
        return Optional.of(destructuring.sourceSpan());
    }

    private Optional<AssignmentSourceShape> knownSourceShape(ExpressionNode expression) {
        ExpressionNode source = ungroup(expression);
        if (source instanceof VectorLiteralNode vectorLiteral) {
            return Optional.of(AssignmentSourceShape.vectorLiteral(
                    vectorLiteral.elements().stream().map(ExpressionNode::id).toList()));
        }
        return Optional.empty();
    }

    private void collectNumericSource(NodeId nodeId, LiteralValue value) {
        switch (value) {
            case BigIntegerLiteralValue ignored -> numericSources.add(new NumericSource(nodeId, NumericSourceKind.INTEGRAL));
            case DecimalLiteralValue ignored -> numericSources.add(new NumericSource(nodeId, NumericSourceKind.FLOATING));
            case LongLiteralValue ignored -> numericSources.add(new NumericSource(nodeId, NumericSourceKind.INTEGRAL));
            default -> {
            }
        }
    }

    private ExpressionType literalType(LiteralValue value) {
        return switch (value) {
            case BigIntegerLiteralValue ignored -> ScalarType.NUMBER;
            case BooleanLiteralValue ignored -> ScalarType.BOOLEAN;
            case DateLiteralValue ignored -> ScalarType.DATE;
            case DecimalLiteralValue ignored -> ScalarType.NUMBER;
            case LocalDateTimeLiteralValue ignored -> ScalarType.DATETIME;
            case LongLiteralValue ignored -> ScalarType.NUMBER;
            case NullLiteralValue ignored -> NullType.INSTANCE;
            case OffsetDateTimeLiteralValue ignored -> ScalarType.DATETIME;
            case StringLiteralValue ignored -> ScalarType.STRING;
            case TimeLiteralValue ignored -> ScalarType.TIME;
        };
    }

    private ExpressionType currentTemporalValueType(CurrentTemporalValueKind kind) {
        return switch (kind) {
            case DATE -> ScalarType.DATE;
            case TIME -> ScalarType.TIME;
            case DATETIME -> ScalarType.DATETIME;
        };
    }

    private static final class ExpressionSummary {

        private final List<SymbolReadSource> reads;
        private final SymbolReadSource rootRead;
        private final ExpressionType type;

        private ExpressionSummary(
                List<SymbolReadSource> reads,
                SymbolReadSource rootRead,
                ExpressionType type) {
            this.reads = List.copyOf(reads);
            this.rootRead = rootRead;
            this.type = type;
        }

        private static ExpressionSummary empty() {
            return new ExpressionSummary(List.of(), null, null);
        }

        private List<SymbolReadSource> reads() {
            return reads;
        }

        private SymbolReadSource rootReadOrNull() {
            return rootRead;
        }

        private ExpressionType typeOrNull() {
            return type;
        }

        private Optional<ExpressionType> type() {
            return Optional.ofNullable(type);
        }
    }
}
