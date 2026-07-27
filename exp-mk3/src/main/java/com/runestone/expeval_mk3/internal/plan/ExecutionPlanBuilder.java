package com.runestone.expeval_mk3.internal.plan;

import ch.obermuhlner.math.big.BigDecimalMath;
import com.runestone.expeval_mk3.api.CollectionType;
import com.runestone.expeval_mk3.api.ExpressionEnvironment;
import com.runestone.expeval_mk3.api.ExpressionType;
import com.runestone.expeval_mk3.api.FunctionDescriptor;
import com.runestone.expeval_mk3.api.MapType;
import com.runestone.expeval_mk3.api.ScalarType;
import com.runestone.expeval_mk3.internal.ast.AssignmentNode;
import com.runestone.expeval_mk3.internal.ast.AssignmentTargetNode;
import com.runestone.expeval_mk3.internal.ast.BetweenNode;
import com.runestone.expeval_mk3.internal.ast.BinaryOperationNode;
import com.runestone.expeval_mk3.internal.ast.BinaryOperator;
import com.runestone.expeval_mk3.internal.ast.CollectionLiteralNode;
import com.runestone.expeval_mk3.internal.ast.ConditionalBranchNode;
import com.runestone.expeval_mk3.internal.ast.ConditionalNode;
import com.runestone.expeval_mk3.internal.ast.CurrentItemNode;
import com.runestone.expeval_mk3.internal.ast.CurrentTemporalValueNode;
import com.runestone.expeval_mk3.internal.ast.DestructuringAssignmentTargetNode;
import com.runestone.expeval_mk3.internal.ast.ExpressionCallArgument;
import com.runestone.expeval_mk3.internal.ast.ExpressionNode;
import com.runestone.expeval_mk3.internal.ast.FilterNavigationLink;
import com.runestone.expeval_mk3.internal.ast.FunctionCallNode;
import com.runestone.expeval_mk3.internal.ast.GroupedExpressionNode;
import com.runestone.expeval_mk3.internal.ast.IdentifierAssignmentTargetNode;
import com.runestone.expeval_mk3.internal.ast.IdentifierNode;
import com.runestone.expeval_mk3.internal.ast.IndexSubscriptNavigationLink;
import com.runestone.expeval_mk3.internal.ast.LiteralNode;
import com.runestone.expeval_mk3.internal.ast.MembershipNode;
import com.runestone.expeval_mk3.internal.ast.NavigationChainNode;
import com.runestone.expeval_mk3.internal.ast.NavigationLink;
import com.runestone.expeval_mk3.internal.ast.NodeId;
import com.runestone.expeval_mk3.internal.ast.NullCoalesceNode;
import com.runestone.expeval_mk3.internal.ast.PostfixOperationNode;
import com.runestone.expeval_mk3.internal.ast.PostfixOperator;
import com.runestone.expeval_mk3.internal.ast.SliceSubscriptNavigationLink;
import com.runestone.expeval_mk3.internal.ast.SubscriptBounds;
import com.runestone.expeval_mk3.internal.ast.UnaryOperationNode;
import com.runestone.expeval_mk3.internal.ast.UnaryOperator;
import com.runestone.expeval_mk3.internal.runtime.ExecutionScope;
import com.runestone.expeval_mk3.internal.semantics.SemanticModel;
import com.runestone.expeval_mk3.internal.semantics.SymbolBinding;

import java.lang.invoke.MethodHandle;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public final class ExecutionPlanBuilder {

    public ExecutionPlan build(SemanticModel model, ExpressionEnvironment environment) {
        Objects.requireNonNull(model, "model");
        Objects.requireNonNull(environment, "environment");
        ExpressionNode result = model.ast().resultExpression().orElseThrow(
                () -> new IllegalArgumentException("semantic model must have a result expression"));
        List<ExternalBindingPlan> externalBindings = model.frameLayout().externalBindings().stream()
                .map(binding -> new ExternalBindingPlan(binding.requireExternalSymbol(), binding.frameSlot()))
                .toList();
        List<Consumer<ExecutionScope>> assignments = model.ast().assignments().stream()
                .map(assignment -> buildAssignment(assignment, model, environment))
                .toList();
        return new ExecutionPlan(
                buildNode(result, model, environment),
                assignments,
                externalBindings,
                model.frameLayout().frameSize(),
                environment.boundaryCoercion(),
                environment.zoneId());
    }

    private Consumer<ExecutionScope> buildAssignment(
            AssignmentNode assignment,
            SemanticModel model,
            ExpressionEnvironment environment) {
        ExecutableNode expression = buildNode(assignment.expression(), model, environment);
        AssignmentTargetNode target = assignment.target();
        if (target instanceof IdentifierAssignmentTargetNode identifier) {
            SymbolBinding binding = required(model.symbolBindings(), identifier.id(), "assignment target binding");
            return scope -> scope.write(binding.frameSlot(), expression.execute(scope));
        }
        if (target instanceof DestructuringAssignmentTargetNode destructuring) {
            List<SymbolBinding> bindings = destructuring.elements().stream()
                    .map(element -> required(model.symbolBindings(), element.id(), "assignment target binding"))
                    .toList();
            return scope -> {
                List<?> values = (List<?>) expression.execute(scope);
                if (values.size() < bindings.size()) {
                    throw new IllegalStateException("destructuring source does not contain enough elements");
                }
                for (int index = 0; index < bindings.size(); index++) {
                    scope.write(bindings.get(index).frameSlot(), values.get(index));
                }
            };
        }
        throw new IllegalArgumentException("unsupported assignment target: " + target.getClass().getSimpleName());
    }

    private ExecutableNode buildNode(ExpressionNode node, SemanticModel model, ExpressionEnvironment environment) {
        if (node instanceof LiteralNode literal) {
            Object value = required(model.preparedValues(), literal.id(), "prepared literal value");
            return scope -> value;
        }
        if (node instanceof CollectionLiteralNode collection) {
            List<ExecutableNode> elements = collection.elements().stream()
                    .map(element -> buildNode(element, model, environment))
                    .toList();
            return scope -> materialize(elements, scope);
        }
        if (node instanceof IdentifierNode identifier) {
            SymbolBinding binding = required(model.symbolBindings(), identifier.id(), "symbol binding");
            return scope -> scope.read(binding.frameSlot());
        }
        if (node instanceof CurrentItemNode currentItem) {
            SymbolBinding binding = required(model.symbolBindings(), currentItem.id(), "current item binding");
            return scope -> scope.read(binding.frameSlot());
        }
        if (node instanceof CurrentTemporalValueNode currentTemporalValue) {
            return switch (currentTemporalValue.kind()) {
                case DATE -> ExecutionScope::currentDate;
                case TIME -> ExecutionScope::currentTime;
                case DATE_TIME -> ExecutionScope::currentDateTime;
            };
        }
        if (node instanceof GroupedExpressionNode grouped) {
            return buildNode(grouped.expression(), model, environment);
        }
        if (node instanceof BinaryOperationNode binary) {
            return buildBinary(binary, model, requireEnvironment(environment));
        }
        if (node instanceof UnaryOperationNode unary) {
            ExecutableNode operand = buildNode(unary.operand(), model, environment);
            return scope -> unary.operator() == UnaryOperator.NEGATE
                    ? number(operand.execute(scope)).negate()
                    : !bool(operand.execute(scope));
        }
        if (node instanceof PostfixOperationNode postfix) {
            ExecutableNode operand = buildNode(postfix.operand(), model, environment);
            return scope -> executePostfix(number(operand.execute(scope)), postfix, requireEnvironment(environment));
        }
        if (node instanceof BetweenNode between) {
            return buildBetween(between, model, environment);
        }
        if (node instanceof MembershipNode membership) {
            return buildMembership(membership, model, environment);
        }
        if (node instanceof NullCoalesceNode coalesce) {
            List<ExecutableNode> operands = coalesce.operands().stream()
                    .map(operand -> buildNode(operand, model, environment))
                    .toList();
            return scope -> {
                for (ExecutableNode operand : operands) {
                    Object value = operand.execute(scope);
                    if (value != null) {
                        return value;
                    }
                }
                return null;
            };
        }
        if (node instanceof ConditionalNode conditional) {
            return buildConditional(conditional, model, environment);
        }
        if (node instanceof FunctionCallNode functionCall) {
            return buildFunctionCall(functionCall, model, environment);
        }
        if (node instanceof NavigationChainNode navigation) {
            return buildNavigationChain(navigation, model, environment);
        }
        throw new IllegalArgumentException("unsupported planned node: " + node.getClass().getSimpleName());
    }

    private ExecutableNode buildBinary(
            BinaryOperationNode binary,
            SemanticModel model,
            ExpressionEnvironment environment) {
        ExecutableNode left = buildNode(binary.left(), model, environment);
        ExecutableNode right = buildNode(binary.right(), model, environment);
        return switch (binary.operator()) {
            case ADD -> scope -> number(left.execute(scope)).add(number(right.execute(scope)));
            case SUBTRACT -> scope -> number(left.execute(scope)).subtract(number(right.execute(scope)));
            case MULTIPLY -> scope -> number(left.execute(scope)).multiply(number(right.execute(scope)), environment.mathContext());
            case DIVIDE -> scope -> number(left.execute(scope)).divide(number(right.execute(scope)), environment.mathContext());
            case MODULO -> scope -> number(left.execute(scope)).remainder(number(right.execute(scope)), environment.mathContext());
            case ROOT -> scope -> BigDecimalMath.root(number(right.execute(scope)), number(left.execute(scope)), environment.mathContext());
            case EXPONENTIATE -> scope -> pow(number(left.execute(scope)), number(right.execute(scope)), environment.mathContext());
            case CONCATENATE -> scope -> (String) left.execute(scope) + right.execute(scope);
            case LOGICAL_AND -> scope -> bool(left.execute(scope)) && bool(right.execute(scope));
            case LOGICAL_OR -> scope -> bool(left.execute(scope)) || bool(right.execute(scope));
            case LOGICAL_NAND -> scope -> {
                boolean leftValue = bool(left.execute(scope));
                boolean rightValue = bool(right.execute(scope));
                return !(leftValue && rightValue);
            };
            case LOGICAL_NOR -> scope -> {
                boolean leftValue = bool(left.execute(scope));
                boolean rightValue = bool(right.execute(scope));
                return !(leftValue || rightValue);
            };
            case LOGICAL_XOR -> scope -> bool(left.execute(scope)) ^ bool(right.execute(scope));
            case LOGICAL_XNOR -> scope -> !(bool(left.execute(scope)) ^ bool(right.execute(scope)));
            case GREATER_THAN -> comparison(left, right, model.resolvedTypes().get(binary.left().id()), comparison -> comparison > 0);
            case GREATER_THAN_OR_EQUAL -> comparison(left, right, model.resolvedTypes().get(binary.left().id()), comparison -> comparison >= 0);
            case LESS_THAN -> comparison(left, right, model.resolvedTypes().get(binary.left().id()), comparison -> comparison < 0);
            case LESS_THAN_OR_EQUAL -> comparison(left, right, model.resolvedTypes().get(binary.left().id()), comparison -> comparison <= 0);
            case EQUAL, NOT_EQUAL -> equality(binary, left, right, model);
            case REGEX_MATCH, REGEX_NOT_MATCH -> regex(binary, left, model);
        };
    }

    private ExecutableNode equality(
            BinaryOperationNode binary,
            ExecutableNode left,
            ExecutableNode right,
            SemanticModel model) {
        ExpressionType operandType = required(model.equalityOperandTypes(), binary.id(), "equality operand type");
        boolean negated = binary.operator() == BinaryOperator.NOT_EQUAL;
        return scope -> structuralEquals(left.execute(scope), right.execute(scope), operandType) != negated;
    }

    private ExecutableNode regex(BinaryOperationNode binary, ExecutableNode left, SemanticModel model) {
        Pattern pattern = (Pattern) required(model.preparedValues(), binary.id(), "prepared regex pattern");
        boolean negated = binary.operator() == BinaryOperator.REGEX_NOT_MATCH;
        return scope -> pattern.matcher((String) left.execute(scope)).matches() != negated;
    }

    private ExecutableNode comparison(
            ExecutableNode left,
            ExecutableNode right,
            ExpressionType type,
            java.util.function.IntPredicate predicate) {
        return scope -> predicate.test(compareValues(left.execute(scope), right.execute(scope), type));
    }

    private ExecutableNode buildBetween(BetweenNode between, SemanticModel model, ExpressionEnvironment environment) {
        ExecutableNode valueNode = buildNode(between.value(), model, environment);
        ExecutableNode lowerNode = buildNode(between.lowerBound(), model, environment);
        ExecutableNode upperNode = buildNode(between.upperBound(), model, environment);
        ExpressionType type = model.resolvedTypes().get(between.value().id());
        return scope -> {
            Object value = valueNode.execute(scope);
            if (compareValues(value, lowerNode.execute(scope), type) < 0) {
                return between.negated();
            }
            boolean inside = compareValues(value, upperNode.execute(scope), type) <= 0;
            return inside != between.negated();
        };
    }

    private ExecutableNode buildMembership(MembershipNode membership, SemanticModel model, ExpressionEnvironment environment) {
        ExecutableNode elementNode = buildNode(membership.element(), model, environment);
        ExecutableNode collectionNode = buildNode(membership.collection(), model, environment);
        ExpressionType collectionType = model.resolvedTypes().get(membership.collection().id());
        return scope -> {
            Object element = elementNode.execute(scope);
            boolean contains;
            if (collectionType instanceof CollectionType type) {
                contains = false;
                for (Object value : (List<?>) collectionNode.execute(scope)) {
                    if (structuralEquals(element, value, type.elementType())) {
                        contains = true;
                        break;
                    }
                }
            } else {
                contains = ((Map<?, ?>) collectionNode.execute(scope)).containsKey(element);
            }
            return contains != membership.negated();
        };
    }

    private ExecutableNode buildConditional(ConditionalNode conditional, SemanticModel model, ExpressionEnvironment environment) {
        List<ExecutableBranch> branches = conditional.branches().stream()
                .map(branch -> new ExecutableBranch(
                        buildNode(branch.condition(), model, environment),
                        buildNode(branch.consequence(), model, environment)))
                .toList();
        ExecutableNode elseExpression = buildNode(conditional.elseExpression(), model, environment);
        return scope -> {
            for (ExecutableBranch branch : branches) {
                if (bool(branch.condition().execute(scope))) {
                    return branch.consequence().execute(scope);
                }
            }
            return elseExpression.execute(scope);
        };
    }

    private ExecutableNode buildFunctionCall(FunctionCallNode functionCall, SemanticModel model, ExpressionEnvironment environment) {
        FunctionDescriptor descriptor = required(model.functionBindings(), functionCall.id(), "function binding");
        List<ExecutableNode> arguments = functionCall.arguments().stream()
                .map(ExpressionCallArgument.class::cast)
                .map(argument -> buildNode(argument.expression(), model, environment))
                .toList();
        return scope -> invokeFunction(descriptor, arguments, scope);
    }

    private ExecutableNode buildNavigationChain(
            NavigationChainNode navigation,
            SemanticModel model,
            ExpressionEnvironment environment) {
        ExecutableNode current = buildNode(navigation.receiver(), model, environment);
        for (NavigationLink link : navigation.links()) {
            current = buildNavigationLink(link, current, model, environment);
        }
        return current;
    }

    private ExecutableNode buildNavigationLink(
            NavigationLink link,
            ExecutableNode receiver,
            SemanticModel model,
            ExpressionEnvironment environment) {
        if (link instanceof IndexSubscriptNavigationLink index) {
            return scope -> indexedValue(receiver.execute(scope), index);
        }
        if (link instanceof SliceSubscriptNavigationLink slice) {
            return scope -> slicedValues(receiver.execute(scope), slice, environment.maxMaterializedSize());
        }
        if (link instanceof FilterNavigationLink filter) {
            ExecutableNode predicate = buildNode(filter.predicate(), model, environment);
            SymbolBinding currentItem = required(model.symbolBindings(), filter.id(), "filter current item binding");
            return scope -> filteredValues(
                    receiver.execute(scope),
                    predicate,
                    currentItem.frameSlot(),
                    scope,
                    environment.maxMaterializedSize());
        }
        throw new IllegalArgumentException("unsupported planned navigation link: " + link.getClass().getSimpleName());
    }

    private static Object invokeFunction(
            FunctionDescriptor descriptor,
            List<ExecutableNode> argumentNodes,
            ExecutionScope scope) {
        Object[] arguments = new Object[argumentNodes.size()];
        for (int index = 0; index < argumentNodes.size(); index++) {
            arguments[index] = Objects.requireNonNull(argumentNodes.get(index).execute(scope), "function argument");
        }
        MethodHandle handle = descriptor.implementationHandle();
        try {
            return Objects.requireNonNull(handle.invokeWithArguments(arguments), "function result");
        } catch (RuntimeException | Error exception) {
            throw exception;
        } catch (Throwable exception) {
            // MethodHandle.invokeWithArguments declares Throwable; this boundary preserves provider failures.
            throw new IllegalStateException("function invocation failed: " + descriptor, exception);
        }
    }

    private static List<Object> materialize(List<ExecutableNode> elements, ExecutionScope scope) {
        ArrayList<Object> values = new ArrayList<>(elements.size());
        for (ExecutableNode element : elements) {
            values.add(Objects.requireNonNull(element.execute(scope), "collection element"));
        }
        return List.copyOf(values);
    }

    private static Object indexedValue(Object receiver, IndexSubscriptNavigationLink index) {
        List<?> values = (List<?>) receiver;
        int resolvedIndex = SubscriptBounds.normalizedIndex(index.index().value(), values.size());
        return Objects.requireNonNull(values.get(resolvedIndex), "collection element");
    }

    private static List<Object> slicedValues(
            Object receiver,
            SliceSubscriptNavigationLink slice,
            int maxMaterializedSize) {
        List<?> values = (List<?>) receiver;
        int start = SubscriptBounds.normalizedSliceBound(slice.start(), values.size(), 0);
        int end = SubscriptBounds.normalizedSliceBound(slice.end(), values.size(), values.size());
        if (end < start) {
            end = start;
        }
        requireMaterializedSize(end - start, maxMaterializedSize);
        ArrayList<Object> result = new ArrayList<>(end - start);
        for (int index = start; index < end; index++) {
            result.add(Objects.requireNonNull(values.get(index), "collection element"));
        }
        return List.copyOf(result);
    }

    private static List<Object> filteredValues(
            Object receiver,
            ExecutableNode predicate,
            int currentItemSlot,
            ExecutionScope scope,
            int maxMaterializedSize) {
        List<?> values = (List<?>) receiver;
        ArrayList<Object> result = new ArrayList<>();
        for (Object value : values) {
            Object item = Objects.requireNonNull(value, "collection element");
            Object previous = scope.replace(currentItemSlot, item);
            try {
                if (bool(predicate.execute(scope))) {
                    if (result.size() == maxMaterializedSize) {
                        throw new IllegalStateException(
                                "materialized collection exceeds maxMaterializedSize " + maxMaterializedSize);
                    }
                    result.add(item);
                }
            } finally {
                scope.restore(currentItemSlot, previous);
            }
        }
        return List.copyOf(result);
    }

    private static void requireMaterializedSize(int size, int maxMaterializedSize) {
        if (size > maxMaterializedSize) {
            throw new IllegalStateException("materialized collection exceeds maxMaterializedSize " + maxMaterializedSize);
        }
    }

    private static BigDecimal executePostfix(
            BigDecimal initial,
            PostfixOperationNode postfix,
            ExpressionEnvironment environment) {
        BigDecimal result = initial;
        for (var operation : postfix.operations()) {
            result = operation.operator() == PostfixOperator.PERCENT
                    ? result.movePointLeft(2)
                    : factorial(result, environment.maxFactorialInput());
        }
        return result;
    }

    private static BigDecimal factorial(BigDecimal value, int maxFactorialInput) {
        BigDecimal normalized = value.stripTrailingZeros();
        if (normalized.scale() > 0) {
            throw new ArithmeticException("factorial input must be integral: " + value);
        }
        BigInteger integerValue = normalized.toBigInteger();
        if (integerValue.signum() < 0 || integerValue.compareTo(BigInteger.valueOf(maxFactorialInput)) > 0) {
            throw new ArithmeticException("factorial input out of range: " + value);
        }
        int integer = integerValue.intValue();
        BigInteger result = BigInteger.ONE;
        for (int factor = 2; factor <= integer; factor++) {
            result = result.multiply(BigInteger.valueOf(factor));
        }
        return new BigDecimal(result);
    }

    private static BigDecimal pow(BigDecimal base, BigDecimal exponent, MathContext mathContext) {
        BigDecimal normalizedExponent = exponent.stripTrailingZeros();
        if (normalizedExponent.scale() <= 0) {
            BigInteger integerValue = normalizedExponent.toBigInteger();
            if (integerValue.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0
                    || integerValue.compareTo(BigInteger.valueOf((long) Integer.MIN_VALUE + 1)) < 0) {
                return BigDecimalMath.pow(base, exponent, mathContext);
            }
            int integerExponent = integerValue.intValue();
            if (integerExponent >= 0) {
                return base.pow(integerExponent, mathContext);
            }
            return BigDecimal.ONE.divide(base.pow(-integerExponent, mathContext), mathContext);
        }
        return BigDecimalMath.pow(base, exponent, mathContext);
    }

    private static boolean structuralEquals(Object left, Object right, ExpressionType type) {
        if (type == ScalarType.NUMBER) {
            return ((BigDecimal) left).compareTo((BigDecimal) right) == 0;
        }
        if (type instanceof CollectionType collectionType) {
            List<?> leftValues = (List<?>) left;
            List<?> rightValues = (List<?>) right;
            if (leftValues.size() != rightValues.size()) {
                return false;
            }
            for (int index = 0; index < leftValues.size(); index++) {
                if (!structuralEquals(leftValues.get(index), rightValues.get(index), collectionType.elementType())) {
                    return false;
                }
            }
            return true;
        }
        if (type instanceof MapType mapType) {
            Map<?, ?> leftValues = (Map<?, ?>) left;
            Map<?, ?> rightValues = (Map<?, ?>) right;
            if (!leftValues.keySet().equals(rightValues.keySet())) {
                return false;
            }
            for (Object key : leftValues.keySet()) {
                if (!structuralEquals(leftValues.get(key), rightValues.get(key), mapType.valueType())) {
                    return false;
                }
            }
            return true;
        }
        return left.equals(right);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static int compareValues(Object left, Object right, ExpressionType type) {
        if (type == ScalarType.NUMBER) {
            return ((BigDecimal) left).compareTo((BigDecimal) right);
        }
        return ((Comparable) left).compareTo(right);
    }

    private static BigDecimal number(Object value) {
        return (BigDecimal) value;
    }

    private static boolean bool(Object value) {
        return (Boolean) value;
    }

    private static ExpressionEnvironment requireEnvironment(ExpressionEnvironment environment) {
        return Objects.requireNonNull(environment, "environment");
    }

    private static <K, V> V required(Map<K, V> values, K key, String description) {
        V value = values.get(key);
        if (value == null) {
            throw new IllegalStateException("semantic model is missing " + description + " for " + key);
        }
        return value;
    }

    private record ExecutableBranch(ExecutableNode condition, ExecutableNode consequence) {
    }
}
