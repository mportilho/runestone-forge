package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.api.ExpressionType;
import com.runestone.expeval_mk3.api.NullType;
import com.runestone.expeval_mk3.api.ScalarType;
import com.runestone.expeval_mk3.api.UnknownType;
import com.runestone.expeval_mk3.api.VectorType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

final class SemanticSymbolFlowBuilder {

    private final Map<NodeId, ExpressionType> knownExpressionTypes = new LinkedHashMap<>();
    private final List<CurrentItemSource> currentItemSources = new ArrayList<>();
    private int maxCurrentItemDepth;

    SemanticSymbolFlow build(ExpressionFileNode file) {
        List<AssignmentSymbolFlow> assignments = new ArrayList<>(file.assignments().size());
        for (AssignmentNode assignment : file.assignments()) {
            ExpressionSummary expression = collectExpression(assignment.expression(), 0);
            assignments.add(new AssignmentSymbolFlow(
                    expression.reads(),
                    expression.rootReadOrNull(),
                    AssignedSymbolSource.from(assignment.target()),
                    expression.typeOrNull()));
        }

        List<SymbolReadSource> resultReads = file.resultExpression()
                .map(expression -> collectExpression(expression, 0).reads())
                .orElseGet(List::of);
        return new SemanticSymbolFlow(
                assignments,
                resultReads,
                knownExpressionTypes,
                new CurrentItemFlow(currentItemSources, maxCurrentItemDepth));
    }

    private ExpressionSummary collectExpression(ExpressionNode expression, int currentItemDepth) {
        return switch (expression) {
            case BetweenNode between -> combine(
                    collectExpression(between.value(), currentItemDepth),
                    collectExpression(between.lowerBound(), currentItemDepth),
                    collectExpression(between.upperBound(), currentItemDepth));
            case BinaryOperationNode binary -> collectBinaryOperation(binary, currentItemDepth);
            case ConditionalNode conditional -> collectConditional(conditional, currentItemDepth);
            case CurrentItemNode currentItem -> collectCurrentItem(currentItem, currentItemDepth);
            case CurrentTemporalValueNode currentTemporalValue -> known(
                    currentTemporalValue.id(),
                    currentTemporalValueType(currentTemporalValue.kind()));
            case FunctionCallNode functionCall -> combine(functionCall.arguments().stream()
                    .map(argument -> collectExpression(argument, currentItemDepth))
                    .toList());
            case GroupedExpressionNode grouped -> collectExpression(grouped.expression(), currentItemDepth);
            case IdentifierNode identifier -> read(identifier);
            case LiteralNode literal -> known(literal.id(), literalType(literal.value()));
            case MembershipNode membership -> combine(
                    collectExpression(membership.value(), currentItemDepth),
                    collectExpression(membership.candidates(), currentItemDepth));
            case NavigationChainNode navigationChain -> collectNavigationChain(navigationChain, currentItemDepth);
            case NullCoalescenceNode nullCoalescence -> combine(nullCoalescence.operands().stream()
                    .map(operand -> collectExpression(operand, currentItemDepth))
                    .toList());
            case PostfixOperationNode postfix -> collectExpression(postfix.operand(), currentItemDepth);
            case UnaryOperationNode unary -> collectExpression(unary.operand(), currentItemDepth);
            case VectorLiteralNode vectorLiteral -> collectVectorLiteral(vectorLiteral, currentItemDepth);
        };
    }

    private ExpressionSummary collectBinaryOperation(BinaryOperationNode binary, int currentItemDepth) {
        ExpressionSummary operands = combine(
                collectExpression(binary.left(), currentItemDepth),
                collectExpression(binary.right(), currentItemDepth));
        return switch (binary.operator()) {
            case ADD, SUBTRACT, MULTIPLY, DIVIDE, MODULO, ROOT, EXPONENTIATE -> withType(
                    operands,
                    binary.id(),
                    ScalarType.NUMBER);
            case LOGICAL_OR, LOGICAL_AND, LOGICAL_NAND, LOGICAL_NOR, LOGICAL_XOR, LOGICAL_XNOR,
                    GREATER_THAN, GREATER_THAN_OR_EQUAL, LESS_THAN, LESS_THAN_OR_EQUAL, EQUAL, NOT_EQUAL,
                    REGEX_MATCH, REGEX_NOT_MATCH -> withType(operands, binary.id(), ScalarType.BOOLEAN);
            case CONCAT -> withType(operands, binary.id(), ScalarType.STRING);
        };
    }

    private ExpressionSummary collectConditional(ConditionalNode conditional, int currentItemDepth) {
        List<ExpressionSummary> summaries = new ArrayList<>(conditional.branches().size() * 2 + 1);
        for (ConditionalBranchNode branch : conditional.branches()) {
            summaries.add(collectExpression(branch.condition(), currentItemDepth));
            summaries.add(collectExpression(branch.resultExpression(), currentItemDepth));
        }
        summaries.add(collectExpression(conditional.elseExpression(), currentItemDepth));
        return combine(summaries);
    }

    private ExpressionSummary collectCurrentItem(CurrentItemNode currentItem, int currentItemDepth) {
        currentItemSources.add(new CurrentItemSource(currentItem.id(), currentItemDepth, currentItem.sourceSpan()));
        maxCurrentItemDepth = Math.max(maxCurrentItemDepth, currentItemDepth);
        return known(currentItem.id(), UnknownType.INSTANCE);
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
        List<ExpressionSummary> elements = vectorLiteral.elements().stream()
                .map(element -> collectExpression(element, currentItemDepth))
                .toList();
        ExpressionType elementType = commonElementType(elements);
        return withType(combine(elements), vectorLiteral.id(), new VectorType(elementType));
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
            } else if (!commonType.equals(elementType)) {
                return UnknownType.INSTANCE;
            }
        }
        return commonType;
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
