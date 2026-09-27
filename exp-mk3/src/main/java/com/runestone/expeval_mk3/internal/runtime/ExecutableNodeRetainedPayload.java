package com.runestone.expeval_mk3.internal.runtime;

/**
 * Explicit source-controlled payload traversal for immutable executable nodes. Environment-prepared
 * descriptors, bindings, math contexts, and invokers are deliberately not exposed to the visitor.
 */
public final class ExecutableNodeRetainedPayload {

    private ExecutableNodeRetainedPayload() {
    }

    public static void visit(ExecutableNode node, Visitor visitor) {
        switch (node) {
            case StaticCalculationConstantExecutableNode constant -> constant.visitRetainedPayload(visitor);
            case ConstantExecutableNode constant -> visitor.constant(constant);
            case FrameReadExecutableNode ignored -> {
            }
            case CurrentTemporalExecutableNode current -> visitor.value(current.replaySlots());
            case AddDecimalExecutableNode binary -> binary(visitor, binary.left(), binary.right());
            case BinaryNullCoalesceExecutableNode binary -> binary(visitor, binary.left(), binary.right());
            case ComparableComparisonExecutableNode binary -> binary(visitor, binary.left(), binary.right());
            case EqualsEqualityExecutableNode binary -> binary(visitor, binary.left(), binary.right());
            case ModuloDecimalExecutableNode binary -> binary(visitor, binary.left(), binary.right());
            case MultiplyDecimalExecutableNode binary -> binary(visitor, binary.left(), binary.right());
            case NumberComparisonExecutableNode binary -> binary(visitor, binary.left(), binary.right());
            case NumberEqualityExecutableNode binary -> binary(visitor, binary.left(), binary.right());
            case SubtractDecimalExecutableNode binary -> binary(visitor, binary.left(), binary.right());
            case BetweenExecutableNode between -> {
                visitor.node(between.value());
                visitor.node(between.lowerBound());
                visitor.node(between.upperBound());
            }
            case BinaryExecutableNode binary -> binary.visitRetainedPayload(visitor);
            case CollectionLiteralExecutableNode collection -> visitor.value(collection.elements());
            case CollectionOperationExecutableNode operation -> {
                visitor.node(operation.receiver());
                visitor.value(operation.arguments());
            }
            case ConditionalExecutableNode conditional -> {
                visitor.value(conditional.branches());
                visitor.node(conditional.elseExpression());
            }
            case ContextualMemberExecutableNode member -> visitor.node(member.receiver());
            case FilterExecutableNode filter -> {
                visitor.node(filter.receiver());
                visitor.node(filter.predicate());
            }
            case FunctionCallExecutableNode function -> {
                visitor.value(function.arguments());
                visitor.value(function.preparedRegexCall());
                visitor.value(function.replaySlots());
            }
            case HashLookupMembershipExecutableNode membership -> {
                visitor.node(membership.element());
                visitor.value(membership.lookup());
            }
            case IndexSubscriptExecutableNode subscript -> {
                visitor.node(subscript.receiver());
                visitor.value(subscript.index());
            }
            case MapKeySubscriptExecutableNode subscript -> {
                visitor.node(subscript.receiver());
                visitor.value(subscript.key());
            }
            case MembershipExecutableNode membership -> {
                visitor.node(membership.element());
                visitor.node(membership.collection());
            }
            case MemoizedExecutableNode memoized -> memoized.visitRetainedPayload(visitor);
            case NullCoalesceExecutableNode coalesce -> visitor.value(coalesce.operands());
            case OracleRegisteredMethodExecutableNode method -> method(visitor, method.receiver(), method.arguments(), method.replaySlots());
            case OracleRegisteredPropertyExecutableNode property -> {
                visitor.node(property.receiver());
                visitor.value(property.replaySlots());
            }
            case PostfixExecutableNode postfix -> postfix.visitRetainedPayload(visitor);
            case RegisteredMethodExecutableNode method -> method(visitor, method.receiver(), method.arguments(), method.replaySlots());
            case RegisteredPropertyExecutableNode property -> {
                visitor.node(property.receiver());
                visitor.value(property.replaySlots());
            }
            case SliceSubscriptExecutableNode subscript -> {
                visitor.node(subscript.receiver());
                visitor.value(subscript.startBound());
                visitor.value(subscript.endBound());
            }
            case SortedNumberMembershipExecutableNode membership -> {
                visitor.node(membership.element());
                visitor.value(membership.sortedElements());
            }
            case TwoBranchConditionalExecutableNode conditional -> {
                visitor.value(conditional.first());
                visitor.value(conditional.second());
                visitor.node(conditional.elseExpression());
            }
            case UnaryExecutableNode unary -> visitor.node(unary.operand());
            case WildcardExecutableNode wildcard -> visitor.node(wildcard.receiver());
            default -> throw new IllegalStateException(
                    "missing retained-payload traversal for executable node " + node.getClass().getName());
        }
    }

    private static void binary(Visitor visitor, ExecutableNode left, ExecutableNode right) {
        visitor.node(left);
        visitor.node(right);
    }

    private static void method(
            Visitor visitor, ExecutableNode receiver, java.util.List<ExecutableNode> arguments, int[] replaySlots) {
        visitor.node(receiver);
        visitor.value(arguments);
        visitor.value(replaySlots);
    }

    public interface Visitor {

        void node(ExecutableNode node);

        void value(Object value);

        void constant(ConstantExecutableNode constant);
    }
}
