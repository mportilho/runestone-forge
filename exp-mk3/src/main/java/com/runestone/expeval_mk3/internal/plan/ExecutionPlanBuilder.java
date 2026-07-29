package com.runestone.expeval_mk3.internal.plan;

import ch.obermuhlner.math.big.BigDecimalMath;
import com.runestone.expeval_mk3.api.CollectionType;
import com.runestone.expeval_mk3.api.ExpressionEnvironment;
import com.runestone.expeval_mk3.api.ExpressionType;
import com.runestone.expeval_mk3.api.FunctionDescriptor;
import com.runestone.expeval_mk3.internal.ast.AssignmentNode;
import com.runestone.expeval_mk3.internal.ast.AssignmentTargetNode;
import com.runestone.expeval_mk3.internal.ast.BetweenNode;
import com.runestone.expeval_mk3.internal.ast.BinaryOperationNode;
import com.runestone.expeval_mk3.internal.ast.BinaryOperator;
import com.runestone.expeval_mk3.internal.ast.CallArgument;
import com.runestone.expeval_mk3.internal.ast.CallNavigationLink;
import com.runestone.expeval_mk3.internal.ast.CollectionLiteralNode;
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
import com.runestone.expeval_mk3.internal.ast.LambdaCallArgument;
import com.runestone.expeval_mk3.internal.ast.LambdaNode;
import com.runestone.expeval_mk3.internal.ast.LiteralNode;
import com.runestone.expeval_mk3.internal.ast.MembershipNode;
import com.runestone.expeval_mk3.internal.ast.NavigationChainNode;
import com.runestone.expeval_mk3.internal.ast.NavigationLink;
import com.runestone.expeval_mk3.internal.ast.NullCoalesceNode;
import com.runestone.expeval_mk3.internal.ast.PostfixOperationNode;
import com.runestone.expeval_mk3.internal.ast.PropertyNavigationLink;
import com.runestone.expeval_mk3.internal.ast.SliceSubscriptNavigationLink;
import com.runestone.expeval_mk3.internal.ast.StringKeySubscriptNavigationLink;
import com.runestone.expeval_mk3.internal.ast.UnaryOperationNode;
import com.runestone.expeval_mk3.internal.ast.UnaryOperator;
import com.runestone.expeval_mk3.internal.ast.WildcardNavigationLink;
import com.runestone.expeval_mk3.internal.runtime.CollectionOperationExecutor;
import com.runestone.expeval_mk3.internal.runtime.CollectionOperationExecutors;
import com.runestone.expeval_mk3.internal.runtime.ExecutableBranch;
import com.runestone.expeval_mk3.internal.runtime.ExecutableLambda;
import com.runestone.expeval_mk3.internal.runtime.ExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.ExecutableOperationArguments;
import com.runestone.expeval_mk3.internal.runtime.ExecutionScope;
import com.runestone.expeval_mk3.internal.runtime.ExpressionRuntime;
import com.runestone.expeval_mk3.internal.semantics.CollectionOperationBinding;
import com.runestone.expeval_mk3.internal.semantics.SemanticModel;
import com.runestone.expeval_mk3.internal.semantics.SymbolBinding;
import com.runestone.expeval_mk3.internal.semantics.WildcardNavigationBinding;

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
        return switch (node) {
            case LiteralNode literal -> {
                Object value = required(model.preparedValues(), literal.id(), "prepared literal value");
                yield scope -> value;
            }
            case CollectionLiteralNode collection -> {
                List<ExecutableNode> elements = collection.elements().stream()
                        .map(element -> buildNode(element, model, environment))
                        .toList();
                yield scope -> ExpressionRuntime.materialize(elements, scope);
            }
            case IdentifierNode identifier -> {
                SymbolBinding binding = required(model.symbolBindings(), identifier.id(), "symbol binding");
                yield scope -> scope.read(binding.frameSlot());
            }
            case CurrentItemNode currentItem -> {
                SymbolBinding binding = required(model.symbolBindings(), currentItem.id(), "current item binding");
                yield scope -> scope.read(binding.frameSlot());
            }
            case CurrentTemporalValueNode currentTemporalValue -> switch (currentTemporalValue.kind()) {
                case DATE -> ExecutionScope::currentDate;
                case TIME -> ExecutionScope::currentTime;
                case DATE_TIME -> ExecutionScope::currentDateTime;
            };
            case GroupedExpressionNode grouped -> buildNode(grouped.expression(), model, environment);
            case BinaryOperationNode binary -> buildBinary(binary, model, requireEnvironment(environment));
            case UnaryOperationNode unary -> {
                ExecutableNode operand = buildNode(unary.operand(), model, environment);
                yield scope -> unary.operator() == UnaryOperator.NEGATE
                        ? ExpressionRuntime.number(operand.execute(scope)).negate()
                        : !ExpressionRuntime.bool(operand.execute(scope));
            }
            case PostfixOperationNode postfix -> {
                ExecutableNode operand = buildNode(postfix.operand(), model, environment);
                yield scope -> ExpressionRuntime.executePostfix(
                        ExpressionRuntime.number(operand.execute(scope)), postfix, requireEnvironment(environment));
            }
            case BetweenNode between -> buildBetween(between, model, environment);
            case MembershipNode membership -> buildMembership(membership, model, environment);
            case NullCoalesceNode coalesce -> {
                List<ExecutableNode> operands = coalesce.operands().stream()
                        .map(operand -> buildNode(operand, model, environment))
                        .toList();
                yield scope -> {
                    for (ExecutableNode operand : operands) {
                        Object value = operand.execute(scope);
                        if (value != null) {
                            return value;
                        }
                    }
                    return null;
                };
            }
            case ConditionalNode conditional -> buildConditional(conditional, model, environment);
            case FunctionCallNode functionCall -> buildFunctionCall(functionCall, model, environment);
            case NavigationChainNode navigation -> buildNavigationChain(navigation, model, environment);
        };
    }

    private ExecutableNode buildBinary(
            BinaryOperationNode binary,
            SemanticModel model,
            ExpressionEnvironment environment) {
        ExecutableNode left = buildNode(binary.left(), model, environment);
        ExecutableNode right = buildNode(binary.right(), model, environment);
        return switch (binary.operator()) {
            case ADD -> scope -> ExpressionRuntime.number(left.execute(scope)).add(ExpressionRuntime.number(right.execute(scope)));
            case SUBTRACT -> scope -> ExpressionRuntime.number(left.execute(scope)).subtract(ExpressionRuntime.number(right.execute(scope)));
            case MULTIPLY -> scope -> ExpressionRuntime.number(left.execute(scope)).multiply(ExpressionRuntime.number(right.execute(scope)), environment.mathContext());
            case DIVIDE -> scope -> ExpressionRuntime.number(left.execute(scope)).divide(ExpressionRuntime.number(right.execute(scope)), environment.mathContext());
            case MODULO -> scope -> ExpressionRuntime.number(left.execute(scope)).remainder(ExpressionRuntime.number(right.execute(scope)), environment.mathContext());
            case ROOT -> scope -> BigDecimalMath.root(
                    ExpressionRuntime.number(right.execute(scope)), ExpressionRuntime.number(left.execute(scope)), environment.mathContext());
            case EXPONENTIATE -> scope -> ExpressionRuntime.pow(
                    ExpressionRuntime.number(left.execute(scope)), ExpressionRuntime.number(right.execute(scope)), environment.mathContext());
            case CONCATENATE -> scope -> (String) left.execute(scope) + right.execute(scope);
            case LOGICAL_AND -> scope -> ExpressionRuntime.bool(left.execute(scope)) && ExpressionRuntime.bool(right.execute(scope));
            case LOGICAL_OR -> scope -> ExpressionRuntime.bool(left.execute(scope)) || ExpressionRuntime.bool(right.execute(scope));
            case LOGICAL_NAND -> scope -> {
                boolean leftValue = ExpressionRuntime.bool(left.execute(scope));
                boolean rightValue = ExpressionRuntime.bool(right.execute(scope));
                return !(leftValue && rightValue);
            };
            case LOGICAL_NOR -> scope -> {
                boolean leftValue = ExpressionRuntime.bool(left.execute(scope));
                boolean rightValue = ExpressionRuntime.bool(right.execute(scope));
                return !(leftValue || rightValue);
            };
            case LOGICAL_XOR -> scope -> ExpressionRuntime.bool(left.execute(scope)) ^ ExpressionRuntime.bool(right.execute(scope));
            case LOGICAL_XNOR -> scope -> !(ExpressionRuntime.bool(left.execute(scope)) ^ ExpressionRuntime.bool(right.execute(scope)));
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
        return scope -> ExpressionRuntime.structuralEquals(left.execute(scope), right.execute(scope), operandType) != negated;
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
        return scope -> predicate.test(ExpressionRuntime.compareValues(left.execute(scope), right.execute(scope), type));
    }

    private ExecutableNode buildBetween(BetweenNode between, SemanticModel model, ExpressionEnvironment environment) {
        ExecutableNode valueNode = buildNode(between.value(), model, environment);
        ExecutableNode lowerNode = buildNode(between.lowerBound(), model, environment);
        ExecutableNode upperNode = buildNode(between.upperBound(), model, environment);
        ExpressionType type = model.resolvedTypes().get(between.value().id());
        return scope -> {
            Object value = valueNode.execute(scope);
            if (ExpressionRuntime.compareValues(value, lowerNode.execute(scope), type) < 0) {
                return between.negated();
            }
            boolean inside = ExpressionRuntime.compareValues(value, upperNode.execute(scope), type) <= 0;
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
                    if (ExpressionRuntime.structuralEquals(element, value, type.elementType())) {
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
        return scope -> ExpressionRuntime.executeConditional(branches, elseExpression, scope);
    }

    private ExecutableNode buildFunctionCall(FunctionCallNode functionCall, SemanticModel model, ExpressionEnvironment environment) {
        FunctionDescriptor descriptor = required(model.functionBindings(), functionCall.id(), "function binding");
        List<ExecutableNode> arguments = functionCall.arguments().stream()
                .map(ExpressionCallArgument.class::cast)
                .map(argument -> buildNode(argument.expression(), model, environment))
                .toList();
        return scope -> ExpressionRuntime.invokeFunction(descriptor, arguments, scope);
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
            return scope -> ExpressionRuntime.indexedValue(receiver.execute(scope), index);
        }
        if (link instanceof SliceSubscriptNavigationLink slice) {
            return scope -> ExpressionRuntime.slicedValues(receiver.execute(scope), slice, environment.maxMaterializedSize());
        }
        if (link instanceof StringKeySubscriptNavigationLink stringKey) {
            return scope -> ExpressionRuntime.mapKeyValue(receiver.execute(scope), stringKey.key(), stringKey.safe(), stringKey.sourceSpan());
        }
        if (link instanceof FilterNavigationLink filter) {
            ExecutableNode predicate = buildNode(filter.predicate(), model, environment);
            SymbolBinding currentItem = required(model.symbolBindings(), filter.id(), "filter current item binding");
            return scope -> ExpressionRuntime.filteredValues(
                    receiver.execute(scope),
                    predicate,
                    currentItem.frameSlot(),
                    scope,
                    environment.maxMaterializedSize());
        }
        if (link instanceof WildcardNavigationLink wildcard) {
            WildcardNavigationBinding binding = required(
                    model.wildcardNavigationBindings(), wildcard.id(), "wildcard navigation binding");
            return scope -> ExpressionRuntime.wildcardValues(
                    receiver.execute(scope),
                    wildcard.safe(),
                    binding,
                    environment.maxMaterializedSize());
        }
        if (link instanceof PropertyNavigationLink property) {
            return scope -> ExpressionRuntime.propertyValue(receiver.execute(scope), property);
        }
        if (link instanceof CallNavigationLink call) {
            CollectionOperationBinding binding = required(
                    model.collectionOperationBindings(), call.id(), "collection operation binding");
            ExecutableOperationArguments arguments = buildOperationArguments(call, binding, model, environment);
            CollectionOperationExecutor executor = CollectionOperationExecutors.executorFor(binding.identity());
            return scope -> ExpressionRuntime.executeCollectionOperation(
                    executor,
                    binding,
                    receiver.execute(scope),
                    call.safe(),
                    environment.mathContext(),
                    environment.maxMaterializedSize(),
                    arguments,
                    scope);
        }
        throw new IllegalArgumentException("unsupported planned navigation link: " + link.getClass().getSimpleName());
    }

    private ExecutableOperationArguments buildOperationArguments(
            CallNavigationLink call,
            CollectionOperationBinding binding,
            SemanticModel model,
            ExpressionEnvironment environment) {
        ArrayList<ExecutableNode> valueArguments = new ArrayList<>();
        ArrayList<ExecutableLambda> lambdaArguments = new ArrayList<>();
        int lambdaIndex = 0;
        for (CallArgument argument : call.arguments()) {
            if (argument instanceof ExpressionCallArgument expressionArgument) {
                valueArguments.add(buildNode(expressionArgument.expression(), model, environment));
                continue;
            }
            LambdaCallArgument lambdaArgument = (LambdaCallArgument) argument;
            CollectionOperationBinding.LambdaBinding lambdaBinding = binding.lambdaBindings().get(lambdaIndex++);
            LambdaNode lambda = lambdaArgument.lambda();
            lambdaArguments.add(new ExecutableLambda(
                    buildNode(lambda.body(), model, environment), lambdaBinding.currentItemFrameSlot()));
        }
        return new ExecutableOperationArguments(valueArguments, lambdaArguments);
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
}
