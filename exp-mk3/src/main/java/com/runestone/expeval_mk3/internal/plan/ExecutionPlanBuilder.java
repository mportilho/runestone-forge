package com.runestone.expeval_mk3.internal.plan;

import com.runestone.expeval_mk3.api.CollectionOperationCatalog;
import com.runestone.expeval_mk3.api.ExpressionEnvironment;
import com.runestone.expeval_mk3.api.ExternalSymbol;
import com.runestone.expeval_mk3.api.FunctionDescriptor;
import com.runestone.expeval_mk3.api.RuntimeNullability;
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
import com.runestone.expeval_mk3.internal.ast.NodeId;
import com.runestone.expeval_mk3.internal.ast.NullCoalesceNode;
import com.runestone.expeval_mk3.internal.ast.PostfixOperationNode;
import com.runestone.expeval_mk3.internal.ast.PostfixOperator;
import com.runestone.expeval_mk3.internal.ast.PostfixOperatorOccurrence;
import com.runestone.expeval_mk3.internal.ast.SliceSubscriptNavigationLink;
import com.runestone.expeval_mk3.internal.ast.StringKeySubscriptNavigationLink;
import com.runestone.expeval_mk3.internal.ast.SubscriptBounds;
import com.runestone.expeval_mk3.internal.ast.UnaryOperationNode;
import com.runestone.expeval_mk3.internal.ast.WildcardNavigationLink;
import com.runestone.expeval_mk3.internal.runtime.BetweenExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.BinaryExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.CollectionLiteralExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.CollectionOperationExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.CollectionOperationExecutor;
import com.runestone.expeval_mk3.internal.runtime.CollectionOperationExecutors;
import com.runestone.expeval_mk3.internal.runtime.CollectionOperationRuntimeBinding;
import com.runestone.expeval_mk3.internal.runtime.ConditionalExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.ConstantExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.ContextualMemberExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.CurrentTemporalExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.ExecutableBranch;
import com.runestone.expeval_mk3.internal.runtime.ExecutableLambda;
import com.runestone.expeval_mk3.internal.runtime.ExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.ExecutableOperationArguments;
import com.runestone.expeval_mk3.internal.runtime.FilterExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.FrameReadExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.FunctionCallExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.IndexSubscriptExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.MapKeySubscriptExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.MembershipExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.NullCoalesceExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.PostfixExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.RegisteredMethodExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.RegisteredPropertyExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.SliceSubscriptExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.UnaryExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.WildcardExecutableNode;
import com.runestone.expeval_mk3.internal.semantics.CollectionOperationBinding;
import com.runestone.expeval_mk3.internal.semantics.ContextualMemberNavigationBinding;
import com.runestone.expeval_mk3.internal.semantics.DeferredCheck;
import com.runestone.expeval_mk3.internal.semantics.DestructuringMinimumSizeDeferredCheck;
import com.runestone.expeval_mk3.internal.semantics.FilterNavigationBinding;
import com.runestone.expeval_mk3.internal.semantics.IndexSubscriptNavigationBinding;
import com.runestone.expeval_mk3.internal.semantics.MapKeySubscriptNavigationBinding;
import com.runestone.expeval_mk3.internal.semantics.NavigationBinding;
import com.runestone.expeval_mk3.internal.semantics.RegisteredMethodNavigationBinding;
import com.runestone.expeval_mk3.internal.semantics.RegisteredPropertyNavigationBinding;
import com.runestone.expeval_mk3.internal.semantics.SemanticModel;
import com.runestone.expeval_mk3.internal.semantics.SliceSubscriptNavigationBinding;
import com.runestone.expeval_mk3.internal.semantics.SymbolBinding;
import com.runestone.expeval_mk3.internal.semantics.WildcardNavigationBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Builds the immutable, non-optimized {@link ExecutionPlan} from a successful {@code SemanticModel}.
 * Every violation of the Etapa 4 completeness contract (missing type, binding, prepared value, or
 * numeric fact) is an internal bug in this builder, never a late user diagnostic; it fails as an
 * {@link IllegalStateException} instead of inferring, resolving, or rediscovering the missing decision.
 */
public final class ExecutionPlanBuilder {

    private static final List<PlanTransformation> TRANSFORMATIONS = List.of();

    /**
     * Builds the plan that will actually run: the non-optimized form with any installed transformation
     * applied. No transformation is installed in this phase, so this currently returns the same plan
     * shape as {@link #buildUnoptimized}.
     */
    public ExecutionPlan build(SemanticModel model, ExpressionEnvironment environment) {
        ExecutionPlan plan = buildUnoptimized(model, environment);
        for (PlanTransformation transformation : TRANSFORMATIONS) {
            plan = transformation.apply(plan);
        }
        return plan;
    }

    /**
     * Builds the plan directly from the basic, non-optimized nodes and runtime, skipping the
     * transformation step. Etapas 7-8 use this path as the equivalence oracle against transformed plans;
     * this phase installs no transformation, so {@link #build} and this method produce the same plan
     * shape.
     */
    ExecutionPlan buildUnoptimized(SemanticModel model, ExpressionEnvironment environment) {
        Objects.requireNonNull(model, "model");
        Objects.requireNonNull(environment, "environment");
        Map<NodeId, List<DeferredCheck>> deferredChecksByNode = model.deferredChecks().stream()
                .collect(Collectors.groupingBy(DeferredCheck::nodeId));
        ExecutableNode result = model.ast().resultExpression()
                .map(expression -> buildNode(expression, model, environment, deferredChecksByNode))
                .orElse(null);
        List<ExternalBindingPlan> externalBindings = model.frameLayout().externalBindings().stream()
                .map(binding -> new ExternalBindingPlan(binding.requireExternalSymbol(), binding.frameSlot()))
                .toList();
        Set<String> frameResidentNames = model.frameLayout().externalBindings().stream()
                .map(binding -> binding.requireExternalSymbol().name())
                .collect(Collectors.toSet());
        List<ExternalSymbol> declaredButUnusedSymbols = environment.externalSymbols().values().stream()
                .filter(symbol -> !frameResidentNames.contains(symbol.name()))
                .toList();
        List<AssignmentExecutable> assignments = model.ast().assignments().stream()
                .map(assignment -> buildAssignment(assignment, model, environment, deferredChecksByNode))
                .toList();
        return new ExecutionPlan(
                result,
                assignments,
                externalBindings,
                declaredButUnusedSymbols,
                model.frameLayout().frameSize(),
                environment.boundaryCoercion(),
                environment.zoneId());
    }

    private AssignmentExecutable buildAssignment(
            AssignmentNode assignment,
            SemanticModel model,
            ExpressionEnvironment environment,
            Map<NodeId, List<DeferredCheck>> deferredChecksByNode) {
        ExecutableNode expression = buildNode(assignment.expression(), model, environment, deferredChecksByNode);
        AssignmentTargetNode target = assignment.target();
        if (target instanceof IdentifierAssignmentTargetNode identifier) {
            SymbolBinding binding = required(model.symbolBindings(), identifier.id(), "assignment target binding");
            return AssignmentExecutable.identifier(target.id(), target.sourceSpan(), binding.frameSlot(), expression);
        }
        if (target instanceof DestructuringAssignmentTargetNode destructuring) {
            int[] frameSlots = destructuring.elements().stream()
                    .mapToInt(element -> required(model.symbolBindings(), element.id(), "assignment target binding")
                            .frameSlot())
                    .toArray();
            DestructuringMinimumSizeDeferredCheck minimumSizeCheck = deferredChecksByNode
                    .getOrDefault(destructuring.id(), List.of()).stream()
                    .filter(DestructuringMinimumSizeDeferredCheck.class::isInstance)
                    .map(DestructuringMinimumSizeDeferredCheck.class::cast)
                    .findFirst()
                    .orElse(null);
            return AssignmentExecutable.destructuring(
                    target.id(), target.sourceSpan(), frameSlots, expression, minimumSizeCheck);
        }
        throw new IllegalArgumentException("unsupported assignment target: " + target.getClass().getSimpleName());
    }

    private ExecutableNode buildNode(
            ExpressionNode node,
            SemanticModel model,
            ExpressionEnvironment environment,
            Map<NodeId, List<DeferredCheck>> deferredChecksByNode) {
        return switch (node) {
            case LiteralNode literal -> new ConstantExecutableNode(
                    literal.id(), literal.sourceSpan(),
                    required(model.preparedValues(), literal.id(), "prepared literal value"));
            case CollectionLiteralNode collection -> new CollectionLiteralExecutableNode(
                    collection.id(), collection.sourceSpan(),
                    collection.elements().stream()
                            .map(element -> buildNode(element, model, environment, deferredChecksByNode))
                            .toList());
            case IdentifierNode identifier -> new FrameReadExecutableNode(
                    identifier.id(), identifier.sourceSpan(),
                    required(model.symbolBindings(), identifier.id(), "symbol binding").frameSlot());
            case CurrentItemNode currentItem -> new FrameReadExecutableNode(
                    currentItem.id(), currentItem.sourceSpan(),
                    required(model.symbolBindings(), currentItem.id(), "current item binding").frameSlot());
            case CurrentTemporalValueNode currentTemporalValue -> new CurrentTemporalExecutableNode(
                    currentTemporalValue.id(), currentTemporalValue.sourceSpan(), currentTemporalValue.kind());
            case GroupedExpressionNode grouped -> buildNode(grouped.expression(), model, environment, deferredChecksByNode);
            case BinaryOperationNode binary -> buildBinary(binary, model, requireEnvironment(environment), deferredChecksByNode);
            case UnaryOperationNode unary -> new UnaryExecutableNode(
                    unary.id(), unary.sourceSpan(), unary.operator(),
                    buildNode(unary.operand(), model, environment, deferredChecksByNode));
            case PostfixOperationNode postfix -> buildPostfix(postfix, model, requireEnvironment(environment), deferredChecksByNode);
            case BetweenNode between -> buildBetween(between, model, environment, deferredChecksByNode);
            case MembershipNode membership -> buildMembership(membership, model, environment, deferredChecksByNode);
            case NullCoalesceNode coalesce -> new NullCoalesceExecutableNode(
                    coalesce.id(), coalesce.sourceSpan(),
                    coalesce.operands().stream()
                            .map(operand -> buildNode(operand, model, environment, deferredChecksByNode))
                            .toList());
            case ConditionalNode conditional -> buildConditional(conditional, model, environment, deferredChecksByNode);
            case FunctionCallNode functionCall -> buildFunctionCall(functionCall, model, environment, deferredChecksByNode);
            case NavigationChainNode navigation -> buildNavigationChain(navigation, model, environment, deferredChecksByNode);
        };
    }

    private ExecutableNode buildBinary(
            BinaryOperationNode binary,
            SemanticModel model,
            ExpressionEnvironment environment,
            Map<NodeId, List<DeferredCheck>> deferredChecksByNode) {
        ExecutableNode left = buildNode(binary.left(), model, environment, deferredChecksByNode);
        ExecutableNode right = buildNode(binary.right(), model, environment, deferredChecksByNode);
        BinaryOperator operator = binary.operator();
        return switch (operator) {
            case ADD, SUBTRACT, MULTIPLY, DIVIDE, MODULO, ROOT, EXPONENTIATE, CONCATENATE -> BinaryExecutableNode.arithmetic(
                    binary.id(), binary.sourceSpan(), operator, left, right, environment.mathContext(),
                    deferredChecksByNode.getOrDefault(binary.id(), List.of()));
            case LOGICAL_AND, LOGICAL_OR, LOGICAL_NAND, LOGICAL_NOR, LOGICAL_XOR, LOGICAL_XNOR ->
                    BinaryExecutableNode.logical(binary.id(), binary.sourceSpan(), operator, left, right);
            case GREATER_THAN, GREATER_THAN_OR_EQUAL, LESS_THAN, LESS_THAN_OR_EQUAL -> BinaryExecutableNode.comparison(
                    binary.id(), binary.sourceSpan(), operator, left, right, model.resolvedTypes().get(binary.left().id()));
            case EQUAL, NOT_EQUAL -> BinaryExecutableNode.equality(
                    binary.id(), binary.sourceSpan(), operator, left, right,
                    required(model.equalityOperandTypes(), binary.id(), "equality operand type"));
            case REGEX_MATCH, REGEX_NOT_MATCH -> BinaryExecutableNode.regex(
                    binary.id(), binary.sourceSpan(), operator, left,
                    (Pattern) required(model.preparedValues(), binary.id(), "prepared regex pattern"));
        };
    }

    private ExecutableNode buildPostfix(
            PostfixOperationNode postfix,
            SemanticModel model,
            ExpressionEnvironment environment,
            Map<NodeId, List<DeferredCheck>> deferredChecksByNode) {
        ExecutableNode operand = buildNode(postfix.operand(), model, environment, deferredChecksByNode);
        List<PostfixOperator> operators = postfix.operations().stream().map(PostfixOperatorOccurrence::operator).toList();
        return new PostfixExecutableNode(
                postfix.id(), postfix.sourceSpan(), operand, operators, environment.maxFactorialInput(),
                deferredChecksByNode.getOrDefault(postfix.id(), List.of()));
    }

    private ExecutableNode buildBetween(
            BetweenNode between,
            SemanticModel model,
            ExpressionEnvironment environment,
            Map<NodeId, List<DeferredCheck>> deferredChecksByNode) {
        return new BetweenExecutableNode(
                between.id(),
                between.sourceSpan(),
                between.negated(),
                buildNode(between.value(), model, environment, deferredChecksByNode),
                buildNode(between.lowerBound(), model, environment, deferredChecksByNode),
                buildNode(between.upperBound(), model, environment, deferredChecksByNode),
                model.resolvedTypes().get(between.value().id()));
    }

    private ExecutableNode buildMembership(
            MembershipNode membership,
            SemanticModel model,
            ExpressionEnvironment environment,
            Map<NodeId, List<DeferredCheck>> deferredChecksByNode) {
        return new MembershipExecutableNode(
                membership.id(),
                membership.sourceSpan(),
                membership.negated(),
                buildNode(membership.element(), model, environment, deferredChecksByNode),
                buildNode(membership.collection(), model, environment, deferredChecksByNode),
                model.resolvedTypes().get(membership.collection().id()));
    }

    private ExecutableNode buildConditional(
            ConditionalNode conditional,
            SemanticModel model,
            ExpressionEnvironment environment,
            Map<NodeId, List<DeferredCheck>> deferredChecksByNode) {
        List<ExecutableBranch> branches = conditional.branches().stream()
                .map(branch -> new ExecutableBranch(
                        buildNode(branch.condition(), model, environment, deferredChecksByNode),
                        buildNode(branch.consequence(), model, environment, deferredChecksByNode)))
                .toList();
        ExecutableNode elseExpression = buildNode(conditional.elseExpression(), model, environment, deferredChecksByNode);
        return new ConditionalExecutableNode(conditional.id(), conditional.sourceSpan(), branches, elseExpression);
    }

    private ExecutableNode buildFunctionCall(
            FunctionCallNode functionCall,
            SemanticModel model,
            ExpressionEnvironment environment,
            Map<NodeId, List<DeferredCheck>> deferredChecksByNode) {
        FunctionDescriptor descriptor = required(model.functionBindings(), functionCall.id(), "function binding");
        List<ExecutableNode> arguments = functionCall.arguments().stream()
                .map(ExpressionCallArgument.class::cast)
                .map(argument -> buildNode(argument.expression(), model, environment, deferredChecksByNode))
                .toList();
        return new FunctionCallExecutableNode(functionCall.id(), functionCall.sourceSpan(), descriptor, arguments);
    }

    private ExecutableNode buildNavigationChain(
            NavigationChainNode navigation,
            SemanticModel model,
            ExpressionEnvironment environment,
            Map<NodeId, List<DeferredCheck>> deferredChecksByNode) {
        ExecutableNode current = buildNode(navigation.receiver(), model, environment, deferredChecksByNode);
        for (NavigationLink link : navigation.links()) {
            current = buildNavigationLink(link, current, model, environment, deferredChecksByNode);
        }
        return current;
    }

    private ExecutableNode buildNavigationLink(
            NavigationLink link,
            ExecutableNode receiver,
            SemanticModel model,
            ExpressionEnvironment environment,
            Map<NodeId, List<DeferredCheck>> deferredChecksByNode) {
        NavigationBinding binding = required(model.navigationBindings(), link.id(), "navigation binding");
        NodeId id = link.id();
        var span = link.sourceSpan();
        return switch (binding) {
            case IndexSubscriptNavigationBinding ignored -> {
                IndexSubscriptNavigationLink index = (IndexSubscriptNavigationLink) link;
                yield new IndexSubscriptExecutableNode(id, span, receiver, index.index().value());
            }
            case SliceSubscriptNavigationBinding ignored -> {
                SliceSubscriptNavigationLink slice = (SliceSubscriptNavigationLink) link;
                yield new SliceSubscriptExecutableNode(
                        id, span, receiver, SubscriptBounds.rawValue(slice.start()),
                        SubscriptBounds.rawValue(slice.end()), environment.maxMaterializedSize());
            }
            case MapKeySubscriptNavigationBinding mapKeyBinding -> {
                StringKeySubscriptNavigationLink stringKey = (StringKeySubscriptNavigationLink) link;
                boolean safe = mapKeyBinding.resultNullability() == RuntimeNullability.MAY_BE_NULL;
                yield new MapKeySubscriptExecutableNode(id, span, receiver, stringKey.key(), safe);
            }
            case FilterNavigationBinding filterBinding -> {
                FilterNavigationLink filter = (FilterNavigationLink) link;
                ExecutableNode predicate = buildNode(filter.predicate(), model, environment, deferredChecksByNode);
                yield new FilterExecutableNode(
                        id, span, receiver, predicate, filterBinding.currentItemFrameSlot(),
                        environment.maxMaterializedSize());
            }
            case ContextualMemberNavigationBinding memberBinding -> {
                boolean safe = memberBinding.resultNullability() == RuntimeNullability.MAY_BE_NULL;
                yield new ContextualMemberExecutableNode(id, span, receiver, memberBinding.member(), safe);
            }
            case RegisteredPropertyNavigationBinding propertyBinding -> {
                boolean safe = propertyBinding.resultNullability() == RuntimeNullability.MAY_BE_NULL;
                yield new RegisteredPropertyExecutableNode(id, span, receiver, safe, propertyBinding);
            }
            case RegisteredMethodNavigationBinding methodBinding -> {
                CallNavigationLink call = (CallNavigationLink) link;
                boolean safe = methodBinding.resultNullability() == RuntimeNullability.MAY_BE_NULL;
                List<ExecutableNode> arguments = call.arguments().stream()
                        .map(ExpressionCallArgument.class::cast)
                        .map(argument -> buildNode(argument.expression(), model, environment, deferredChecksByNode))
                        .toList();
                yield new RegisteredMethodExecutableNode(id, span, receiver, safe, methodBinding, arguments);
            }
            case WildcardNavigationBinding wildcardBinding -> {
                WildcardNavigationLink wildcard = (WildcardNavigationLink) link;
                yield new WildcardExecutableNode(
                        id, span, receiver, wildcard.safe(), wildcardBinding, environment.maxMaterializedSize());
            }
            case CollectionOperationBinding operationBinding -> {
                CallNavigationLink call = (CallNavigationLink) link;
                ExecutableOperationArguments arguments = buildOperationArguments(call, operationBinding, model, environment, deferredChecksByNode);
                CollectionOperationExecutor executor = CollectionOperationExecutors.executorFor(operationBinding.identity());
                CollectionOperationRuntimeBinding runtimeBinding = new CollectionOperationRuntimeBinding(
                        operationBinding.receiverType(),
                        operationBinding.identity() == CollectionOperationCatalog.OperationIdentity.SORT_BY
                                ? operationBinding.lambdaBindings().getFirst().resultType()
                                : null);
                yield new CollectionOperationExecutableNode(
                        id, span, receiver, call.safe(), executor, runtimeBinding,
                        environment.mathContext(), environment.maxMaterializedSize(), arguments);
            }
        };
    }

    private ExecutableOperationArguments buildOperationArguments(
            CallNavigationLink call,
            CollectionOperationBinding binding,
            SemanticModel model,
            ExpressionEnvironment environment,
            Map<NodeId, List<DeferredCheck>> deferredChecksByNode) {
        ArrayList<ExecutableNode> valueArguments = new ArrayList<>();
        ArrayList<ExecutableLambda> lambdaArguments = new ArrayList<>();
        int lambdaIndex = 0;
        for (CallArgument argument : call.arguments()) {
            if (argument instanceof ExpressionCallArgument expressionArgument) {
                valueArguments.add(buildNode(expressionArgument.expression(), model, environment, deferredChecksByNode));
                continue;
            }
            LambdaCallArgument lambdaArgument = (LambdaCallArgument) argument;
            CollectionOperationBinding.LambdaBinding lambdaBinding = binding.lambdaBindings().get(lambdaIndex++);
            LambdaNode lambda = lambdaArgument.lambda();
            lambdaArguments.add(new ExecutableLambda(
                    buildNode(lambda.body(), model, environment, deferredChecksByNode), lambdaBinding.currentItemFrameSlot()));
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
