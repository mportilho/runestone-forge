package com.runestone.expeval_mk3.internal.ast;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Objects;

final class AstNodeIdAssigner {

    private int nextId;

    ExpressionFileNode assign(ExpressionFileNode file) {
        Objects.requireNonNull(file, "file");
        nextId = 0;
        NodeId fileId = next();
        List<AssignmentNode> assignments = new ArrayList<>(file.assignments().size());
        for (AssignmentNode assignment : file.assignments()) {
            assignments.add(assignAssignment(assignment));
        }
        Optional<ExpressionNode> resultExpression = file.resultExpression().map(this::assignExpression);
        return new ExpressionFileNode(fileId, file.sourceSpan(), assignments, resultExpression);
    }

    private AssignmentNode assignAssignment(AssignmentNode assignment) {
        NodeId assignmentId = next();
        AssignmentTargetNode target = assignTarget(assignment.target());
        ExpressionNode expression = assignExpression(assignment.expression());
        return new AssignmentNode(assignmentId, assignment.sourceSpan(), target, expression);
    }

    private AssignmentTargetNode assignTarget(AssignmentTargetNode target) {
        if (target instanceof IdentifierAssignmentTargetNode identifier) {
            return new IdentifierAssignmentTargetNode(next(), identifier.sourceSpan(), identifier.name());
        }
        throw new IllegalArgumentException("Unsupported assignment target node: " + target.getClass().getName());
    }

    private ExpressionNode assignExpression(ExpressionNode expression) {
        return switch (expression) {
            case BetweenNode between -> new BetweenNode(
                    next(),
                    between.sourceSpan(),
                    assignExpression(between.value()),
                    between.negated(),
                    between.operatorSpan(),
                    assignExpression(between.lowerBound()),
                    between.lowerSeparatorSpan(),
                    assignExpression(between.upperBound()));
            case BinaryOperationNode binary -> new BinaryOperationNode(
                    next(),
                    binary.sourceSpan(),
                    assignExpression(binary.left()),
                    binary.operator(),
                    binary.operatorSpan(),
                    assignExpression(binary.right()));
            case ConditionalNode conditional -> new ConditionalNode(
                    next(),
                    conditional.sourceSpan(),
                    conditional.syntax(),
                    assignBranches(conditional.branches()),
                    conditional.separators(),
                    assignExpression(conditional.elseExpression()));
            case CurrentItemNode currentItem -> new CurrentItemNode(next(), currentItem.sourceSpan());
            case CurrentTemporalValueNode currentTemporalValue -> new CurrentTemporalValueNode(
                    next(),
                    currentTemporalValue.sourceSpan(),
                    currentTemporalValue.kind());
            case FunctionCallNode functionCall -> new FunctionCallNode(
                    next(),
                    functionCall.sourceSpan(),
                    functionCall.name(),
                    functionCall.arguments().stream().map(this::assignExpression).toList());
            case GroupedExpressionNode grouped -> new GroupedExpressionNode(
                    next(),
                    grouped.sourceSpan(),
                    assignExpression(grouped.expression()));
            case IdentifierNode identifier -> new IdentifierNode(next(), identifier.sourceSpan(), identifier.name());
            case LiteralNode literal -> new LiteralNode(next(), literal.sourceSpan(), literal.value());
            case MembershipNode membership -> new MembershipNode(
                    next(),
                    membership.sourceSpan(),
                    assignExpression(membership.element()),
                    membership.negated(),
                    membership.operatorSpan(),
                    assignExpression(membership.collection()));
            case NavigationChainNode navigation -> new NavigationChainNode(
                    next(),
                    navigation.sourceSpan(),
                    assignExpression(navigation.receiver()),
                    navigation.links().stream().map(this::assignNavigationLink).toList());
            case NullCoalesceNode coalesce -> new NullCoalesceNode(
                    next(),
                    coalesce.sourceSpan(),
                    coalesce.operands().stream().map(this::assignExpression).toList(),
                    coalesce.operatorSpans());
            case PostfixOperationNode postfix -> new PostfixOperationNode(
                    next(),
                    postfix.sourceSpan(),
                    assignExpression(postfix.operand()),
                    postfix.operations());
            case UnaryOperationNode unary -> new UnaryOperationNode(
                    next(),
                    unary.sourceSpan(),
                    unary.operator(),
                    unary.operatorSpan(),
                    assignExpression(unary.operand()));
            case VectorLiteralNode vector -> new VectorLiteralNode(
                    next(),
                    vector.sourceSpan(),
                    vector.elements().stream().map(this::assignExpression).toList());
        };
    }

    private NavigationLink assignNavigationLink(NavigationLink link) {
        return switch (link) {
            case CollectionOperationNavigationLink collectionOperation -> new CollectionOperationNavigationLink(
                    next(),
                    collectionOperation.sourceSpan(),
                    collectionOperation.memberName(),
                    collectionOperation.arguments().stream().map(this::assignCollectionOperationArgument).toList());
            case FilterNavigationLink filter -> new FilterNavigationLink(
                    next(),
                    filter.sourceSpan(),
                    assignExpression(filter.predicate()),
                    filter.safe());
            case IndexSubscriptNavigationLink index -> new IndexSubscriptNavigationLink(
                    next(),
                    index.sourceSpan(),
                    index.index(),
                    index.safe());
            case MethodNavigationLink method -> new MethodNavigationLink(
                    next(),
                    method.sourceSpan(),
                    method.memberName(),
                    method.safe(),
                    method.arguments().stream().map(this::assignExpression).toList());
            case PropertyNavigationLink property -> new PropertyNavigationLink(
                    next(),
                    property.sourceSpan(),
                    property.memberName(),
                    property.safe());
            case SliceSubscriptNavigationLink slice -> new SliceSubscriptNavigationLink(
                    next(),
                    slice.sourceSpan(),
                    slice.start(),
                    slice.end(),
                    slice.safe());
            case StringKeySubscriptNavigationLink stringKey -> new StringKeySubscriptNavigationLink(
                    next(),
                    stringKey.sourceSpan(),
                    stringKey.key(),
                    stringKey.safe());
            case WildcardNavigationLink wildcard -> new WildcardNavigationLink(
                    next(),
                    wildcard.sourceSpan(),
                    wildcard.kind(),
                    wildcard.safe());
        };
    }

    private CollectionOperationArgument assignCollectionOperationArgument(CollectionOperationArgument argument) {
        return switch (argument) {
            case LambdaCollectionOperationArgument lambda -> new LambdaCollectionOperationArgument(assignLambda(lambda.lambda()));
            case PositionalCollectionOperationArgument positional -> new PositionalCollectionOperationArgument(
                    assignExpression(positional.expression()));
        };
    }

    private LambdaNode assignLambda(LambdaNode lambda) {
        return new LambdaNode(
                next(),
                lambda.sourceSpan(),
                new CurrentItemNode(next(), lambda.currentItem().sourceSpan()),
                lambda.arrowSpan(),
                assignExpression(lambda.body()));
    }

    private ConditionalBranchNode assignBranch(ConditionalBranchNode branch) {
        return new ConditionalBranchNode(
                next(),
                branch.sourceSpan(),
                assignExpression(branch.condition()),
                assignExpression(branch.consequence()));
    }

    private List<ConditionalBranchNode> assignBranches(List<ConditionalBranchNode> branches) {
        List<ConditionalBranchNode> assigned = new ArrayList<>(branches.size());
        for (ConditionalBranchNode branch : branches) {
            assigned.add(assignBranch(branch));
        }
        return assigned;
    }

    private NodeId next() {
        return new NodeId(nextId++);
    }
}
