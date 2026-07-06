package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.internal.parser.ExpressionParser;
import com.runestone.expeval_mk3.internal.parser.ParseResult;
import com.runestone.expeval_mk3.internal.parser.ParseSuccess;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

final class AstTestSupport {

    private AstTestSupport() {
    }

    static ExpressionFileNode build(String source) {
        ParseResult parseResult = new ExpressionParser().parse(source);
        assertThat(parseResult).isInstanceOf(ParseSuccess.class);
        SemanticAstResult result = new SemanticAstBuilder().build((ParseSuccess) parseResult);
        assertThat(result).isInstanceOf(SemanticAstSuccess.class);
        return ((SemanticAstSuccess) result).file();
    }

    static List<AstNode> flatten(ExpressionFileNode file) {
        List<AstNode> nodes = new ArrayList<>();
        collect(file, nodes);
        return List.copyOf(nodes);
    }

    private static void collect(AstNode node, List<AstNode> nodes) {
        nodes.add(node);
        switch (node) {
            case AssignmentNode assignment -> {
                collect(assignment.target(), nodes);
                collect(assignment.expression(), nodes);
            }
            case DestructuringAssignmentTargetNode destructuring -> {
                for (IdentifierAssignmentTargetNode element : destructuring.elements()) {
                    collect(element, nodes);
                }
            }
            case ConditionalBranchNode branch -> {
                collect(branch.condition(), nodes);
                collect(branch.resultExpression(), nodes);
            }
            case ExpressionFileNode file -> {
                for (AssignmentNode assignment : file.assignments()) {
                    collect(assignment, nodes);
                }
                file.resultExpression().ifPresent(expression -> collect(expression, nodes));
            }
            case BinaryOperationNode binary -> {
                collect(binary.left(), nodes);
                collect(binary.right(), nodes);
            }
            case BetweenNode between -> {
                collect(between.value(), nodes);
                collect(between.lowerBound(), nodes);
                collect(between.upperBound(), nodes);
            }
            case ConditionalNode conditional -> {
                for (ConditionalBranchNode branch : conditional.branches()) {
                    collect(branch, nodes);
                }
                collect(conditional.elseExpression(), nodes);
            }
            case FunctionCallNode functionCall -> {
                for (ExpressionNode argument : functionCall.arguments()) {
                    collect(argument, nodes);
                }
            }
            case GroupedExpressionNode grouped -> collect(grouped.expression(), nodes);
            case MembershipNode membership -> {
                collect(membership.value(), nodes);
                collect(membership.candidates(), nodes);
            }
            case NavigationChainNode navigationChain -> {
                collect(navigationChain.receiver(), nodes);
                for (NavigationLink link : navigationChain.links()) {
                    collect(link, nodes);
                }
            }
            case NullCoalescenceNode nullCoalescence -> {
                for (ExpressionNode operand : nullCoalescence.operands()) {
                    collect(operand, nodes);
                }
            }
            case PostfixOperationNode postfix -> collect(postfix.operand(), nodes);
            case UnaryOperationNode unary -> collect(unary.operand(), nodes);
            case VectorLiteralNode vectorLiteral -> {
                for (ExpressionNode element : vectorLiteral.elements()) {
                    collect(element, nodes);
                }
            }
            case CollectionOperationNavigationLink collectionOperation -> {
                for (CollectionOperationArgument argument : collectionOperation.arguments()) {
                    collect(argument, nodes);
                }
            }
            case FilterNavigationLink filter -> collect(filter.predicate(), nodes);
            case LambdaCollectionOperationArgument lambdaArgument -> collect(lambdaArgument.lambda(), nodes);
            case LambdaNode lambda -> collect(lambda.body(), nodes);
            case MethodNavigationLink method -> {
                for (ExpressionNode argument : method.arguments()) {
                    collect(argument, nodes);
                }
            }
            case PositionalCollectionOperationArgument positional -> collect(positional.expression(), nodes);
            case CurrentItemNode ignored -> {
            }
            case CurrentTemporalValueNode ignored -> {
            }
            case IdentifierAssignmentTargetNode ignored -> {
            }
            case IdentifierNode ignored -> {
            }
            case LiteralNode ignored -> {
            }
            case PropertyNavigationLink ignored -> {
            }
            case SubscriptNavigationLink ignored -> {
            }
            case WildcardNavigationLink ignored -> {
            }
        }
    }
}
