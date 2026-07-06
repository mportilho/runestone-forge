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
                    between.operatorSpan(),
                    between.negated(),
                    assignExpression(between.lowerBound()),
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
                    conditional.sourceForm(),
                    conditional.branches().stream().map(this::assignConditionalBranch).toList(),
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
                    assignExpression(membership.value()),
                    membership.operatorSpan(),
                    membership.negated(),
                    assignExpression(membership.candidates()));
            case NullCoalescenceNode nullCoalescence -> new NullCoalescenceNode(
                    next(),
                    nullCoalescence.sourceSpan(),
                    nullCoalescence.operands().stream().map(this::assignExpression).toList(),
                    nullCoalescence.operatorSpans());
            case NavigationChainNode navigationChain -> new NavigationChainNode(
                    next(),
                    navigationChain.sourceSpan(),
                    assignExpression(navigationChain.receiver()),
                    navigationChain.links().stream().map(this::assignNavigationLink).toList());
            case PostfixOperationNode postfix -> new PostfixOperationNode(
                    next(),
                    postfix.sourceSpan(),
                    assignExpression(postfix.operand()),
                    postfix.operators());
            case UnaryOperationNode unary -> new UnaryOperationNode(
                    next(),
                    unary.sourceSpan(),
                    unary.operator(),
                    unary.operatorSpan(),
                    assignExpression(unary.operand()));
            case VectorLiteralNode vectorLiteral -> new VectorLiteralNode(
                    next(),
                    vectorLiteral.sourceSpan(),
                    vectorLiteral.elements().stream().map(this::assignExpression).toList());
        };
    }

    private NavigationLink assignNavigationLink(NavigationLink link) {
        return switch (link) {
            case MethodNavigationLink method -> new MethodNavigationLink(
                    next(),
                    method.sourceSpan(),
                    method.memberName(),
                    method.safeNavigation(),
                    method.arguments().stream().map(this::assignExpression).toList());
            case PropertyNavigationLink property -> new PropertyNavigationLink(
                    next(),
                    property.sourceSpan(),
                    property.memberName(),
                    property.safeNavigation());
            case SubscriptNavigationLink subscript -> new SubscriptNavigationLink(
                    next(),
                    subscript.sourceSpan(),
                    subscript.subscript(),
                    subscript.safeNavigation());
            case WildcardNavigationLink wildcard -> new WildcardNavigationLink(next(), wildcard.sourceSpan());
        };
    }

    private ConditionalBranchNode assignConditionalBranch(ConditionalBranchNode branch) {
        return new ConditionalBranchNode(
                next(),
                branch.sourceSpan(),
                assignExpression(branch.condition()),
                assignExpression(branch.resultExpression()));
    }

    private NodeId next() {
        return new NodeId(nextId++);
    }
}
