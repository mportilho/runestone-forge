package com.runestone.expeval.internal.ast.mapping;

import com.runestone.expeval.internal.grammar.ExpressionEvaluatorBaseVisitor;
import com.runestone.expeval.internal.grammar.ExpressionEvaluatorParser;
import com.runestone.expeval.internal.ast.*;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public final class SemanticAstBuilder {

    public ExpressionFileNode buildMath(ExpressionEvaluatorParser.MathStartContext root) {
        Objects.requireNonNull(root, "root must not be null");
        ExpressionEvaluatorParser.MathInputContext input = (ExpressionEvaluatorParser.MathInputContext) root;
        NodeFactory nodeFactory = new NodeFactory();
        return new ExpressionFileNode(
                nodeFactory.nextId("file"),
                nodeFactory.sourceSpan(input),
                buildAssignments(input.assignmentExpression(), nodeFactory),
                new ExpressionVisitor(nodeFactory).visit(input.mathExpression())
        );
    }

    public ExpressionFileNode buildAssignmentFile(ExpressionEvaluatorParser.AssignmentStartContext root) {
        Objects.requireNonNull(root, "root must not be null");
        ExpressionEvaluatorParser.AssignmentInputContext input = (ExpressionEvaluatorParser.AssignmentInputContext) root;
        NodeFactory nodeFactory = new NodeFactory();
        return new ExpressionFileNode(
                nodeFactory.nextId("file"),
                nodeFactory.sourceSpan(input),
                buildAssignments(input.assignmentExpression(), nodeFactory),
                null
        );
    }

    public ExpressionFileNode buildLogical(ExpressionEvaluatorParser.LogicalStartContext root) {
        Objects.requireNonNull(root, "root must not be null");
        ExpressionEvaluatorParser.LogicalInputContext input = (ExpressionEvaluatorParser.LogicalInputContext) root;
        NodeFactory nodeFactory = new NodeFactory();
        return new ExpressionFileNode(
                nodeFactory.nextId("file"),
                nodeFactory.sourceSpan(input),
                buildAssignments(input.assignmentExpression(), nodeFactory),
                new ExpressionVisitor(nodeFactory).visit(input.logicalExpression())
        );
    }

    private static List<AssignmentNode> buildAssignments(List<ExpressionEvaluatorParser.AssignmentExpressionContext> assignments, NodeFactory nodeFactory) {
        List<AssignmentNode> nodes = new ArrayList<>(assignments.size());
        ExpressionVisitor expressionVisitor = new ExpressionVisitor(nodeFactory);
        for (ExpressionEvaluatorParser.AssignmentExpressionContext assignment : assignments) {
            nodes.add(buildAssignment(assignment, expressionVisitor, nodeFactory));
        }
        return List.copyOf(nodes);
    }

    private static AssignmentNode buildAssignment(
            ExpressionEvaluatorParser.AssignmentExpressionContext assignment,
            ExpressionVisitor expressionVisitor,
            NodeFactory nodeFactory
    ) {
        if (assignment instanceof ExpressionEvaluatorParser.AssignmentOperationContext simpleAssignment) {
            return new SimpleAssignmentNode(
                    nodeFactory.nextId("assign"),
                    nodeFactory.sourceSpan(simpleAssignment),
                    simpleAssignment.IDENTIFIER().getText(),
                    buildAssignmentValue(simpleAssignment.assignmentValue(), expressionVisitor)
            );
        }
        if (assignment instanceof ExpressionEvaluatorParser.DestructuringAssignmentOperationContext destructuringAssignment) {
            if (!(destructuringAssignment.vectorOfVariables() instanceof ExpressionEvaluatorParser.VectorOfVariablesOperationContext vectorOfVariables)) {
                throw new IllegalStateException("unsupported vectorOfVariables context: "
                                                + destructuringAssignment.vectorOfVariables().getClass().getSimpleName());
            }
            List<String> targetNames = vectorOfVariables.IDENTIFIER().stream()
                    .map(TerminalNode::getText)
                    .toList();
            ExpressionNode value = expressionVisitor.visit(destructuringAssignment.vectorEntity());
            return new DestructuringAssignmentNode(
                    nodeFactory.nextId("assign"),
                    nodeFactory.sourceSpan(destructuringAssignment),
                    targetNames,
                    value
            );
        }
        throw new IllegalStateException("unsupported assignment context: " + assignment.getClass().getSimpleName());
    }

    private static ExpressionNode buildAssignmentValue(
            ExpressionEvaluatorParser.AssignmentValueContext assignmentValue,
            ExpressionVisitor expressionVisitor
    ) {
        if (assignmentValue instanceof ExpressionEvaluatorParser.MathAssignmentValueContext mathAssignmentValue) {
            return expressionVisitor.visit(mathAssignmentValue.mathExpression());
        }
        if (assignmentValue instanceof ExpressionEvaluatorParser.LogicalAssignmentValueContext logicalAssignmentValue) {
            return expressionVisitor.visit(logicalAssignmentValue.logicalExpression());
        }
        if (assignmentValue instanceof ExpressionEvaluatorParser.GenericAssignmentValueContext genericAssignmentValue) {
            return expressionVisitor.visit(genericAssignmentValue.genericEntity());
        }
        if (assignmentValue instanceof ExpressionEvaluatorParser.StringAssignmentValueContext stringAssignmentValue) {
            return expressionVisitor.visit(stringAssignmentValue.stringConcatExpression());
        }
        if (assignmentValue instanceof ExpressionEvaluatorParser.DateAssignmentValueContext dateAssignmentValue) {
            return expressionVisitor.visit(dateAssignmentValue.dateEntity());
        }
        if (assignmentValue instanceof ExpressionEvaluatorParser.TimeAssignmentValueContext timeAssignmentValue) {
            return expressionVisitor.visit(timeAssignmentValue.timeEntity());
        }
        if (assignmentValue instanceof ExpressionEvaluatorParser.DateTimeAssignmentValueContext dateTimeAssignmentValue) {
            return expressionVisitor.visit(dateTimeAssignmentValue.dateTimeEntity());
        }
        if (assignmentValue instanceof ExpressionEvaluatorParser.VectorAssignmentValueContext vectorAssignmentValue) {
            return expressionVisitor.visit(vectorAssignmentValue.vectorEntity());
        }
        throw new IllegalStateException("unsupported assignment value: " + assignmentValue.getClass().getSimpleName());
    }

    private static final class ExpressionVisitor extends ExpressionEvaluatorBaseVisitor<ExpressionNode> {

        private final NodeFactory nodeFactory;

        private ExpressionVisitor(NodeFactory nodeFactory) {
            this.nodeFactory = nodeFactory;
        }

        @Override
        public ExpressionNode visitSumOperation(ExpressionEvaluatorParser.SumOperationContext ctx) {
            return visit(ctx.sumExpression());
        }

        @Override
        public ExpressionNode visitAdditiveOperation(ExpressionEvaluatorParser.AdditiveOperationContext ctx) {
            ExpressionNode current = visit(ctx.multiplicationExpression(0));
            for (int index = 1; index < ctx.multiplicationExpression().size(); index++) {
                ExpressionNode right = visit(ctx.multiplicationExpression(index));
                BinaryOperator operator = tokenTypeToAdditiveOperator(ctx.getChild(2 * index - 1).getPayload());
                current = new BinaryOperationNode(
                        nodeFactory.nextId("binary"),
                        nodeFactory.sourceSpan(current.sourceSpan(), right.sourceSpan()),
                        operator,
                        current,
                        right
                );
            }
            return current;
        }

        @Override
        public ExpressionNode visitMultiplicativeOperation(ExpressionEvaluatorParser.MultiplicativeOperationContext ctx) {
            ExpressionNode current = visit(ctx.unaryExpression(0));
            for (int index = 1; index < ctx.unaryExpression().size(); index++) {
                ExpressionNode right = visit(ctx.unaryExpression(index));
                BinaryOperator operator = tokenTypeToMultiplicativeOperator(ctx.getChild(2 * index - 1).getPayload());
                current = new BinaryOperationNode(
                        nodeFactory.nextId("binary"),
                        nodeFactory.sourceSpan(current.sourceSpan(), right.sourceSpan()),
                        operator,
                        current,
                        right
                );
            }
            return current;
        }

        @Override
        public ExpressionNode visitUnaryMinusOperation(ExpressionEvaluatorParser.UnaryMinusOperationContext ctx) {
            return new UnaryOperationNode(
                    nodeFactory.nextId("unary"),
                    nodeFactory.sourceSpan(ctx),
                    UnaryOperator.NEGATE,
                    visit(ctx.unaryExpression())
            );
        }

        @Override
        public ExpressionNode visitRootExpressionOperation(ExpressionEvaluatorParser.RootExpressionOperationContext ctx) {
            return visit(ctx.rootExpression());
        }

        @Override
        public ExpressionNode visitRootChainOperation(ExpressionEvaluatorParser.RootChainOperationContext ctx) {
            ExpressionNode current = visit(ctx.exponentiationExpression(0));
            for (int index = 1; index < ctx.exponentiationExpression().size(); index++) {
                ExpressionNode right = visit(ctx.exponentiationExpression(index));
                current = new BinaryOperationNode(
                        nodeFactory.nextId("binary"),
                        nodeFactory.sourceSpan(current.sourceSpan(), right.sourceSpan()),
                        BinaryOperator.ROOT,
                        current,
                        right
                );
            }
            return current;
        }

        @Override
        public ExpressionNode visitExponentiationOperation(ExpressionEvaluatorParser.ExponentiationOperationContext ctx) {
            ExpressionNode left = visit(ctx.postfixExpression());
            if (ctx.unaryExpression() == null) {
                return left;
            }
            ExpressionNode right = visit(ctx.unaryExpression());
            return new BinaryOperationNode(
                    nodeFactory.nextId("binary"),
                    nodeFactory.sourceSpan(left.sourceSpan(), right.sourceSpan()),
                    BinaryOperator.POWER,
                    left,
                    right
            );
        }

        @Override
        public ExpressionNode visitPostfixOperation(ExpressionEvaluatorParser.PostfixOperationContext ctx) {
            ExpressionNode current = visit(ctx.primaryMathExpression());
            for (int childIndex = 1; childIndex < ctx.getChildCount(); childIndex++) {
                TerminalNode operator = (TerminalNode) ctx.getChild(childIndex);
                current = new PostfixOperationNode(
                        nodeFactory.nextId("postfix"),
                        nodeFactory.sourceSpan(current.sourceSpan(), nodeFactory.sourceSpan(operator.getSymbol())),
                        operator.getSymbol().getType() == ExpressionEvaluatorParser.PERCENT ? PostfixOperator.PERCENT : PostfixOperator.FACTORIAL,
                        current
                );
            }
            return current;
        }

        @Override
        public ExpressionNode visitMathExpressionParenthesisOperation(ExpressionEvaluatorParser.MathExpressionParenthesisOperationContext ctx) {
            return visit(ctx.mathExpression());
        }

        @Override
        public ExpressionNode visitSquareRootOperation(ExpressionEvaluatorParser.SquareRootOperationContext ctx) {
            return new UnaryOperationNode(
                    nodeFactory.nextId("unary"),
                    nodeFactory.sourceSpan(ctx),
                    UnaryOperator.SQRT,
                    visit(ctx.mathExpression())
            );
        }

        @Override
        public ExpressionNode visitModulusOperation(ExpressionEvaluatorParser.ModulusOperationContext ctx) {
            return new UnaryOperationNode(
                    nodeFactory.nextId("unary"),
                    nodeFactory.sourceSpan(ctx),
                    UnaryOperator.MODULUS,
                    visit(ctx.mathExpression())
            );
        }

        @Override
        public ExpressionNode visitNumericEntityOperation(ExpressionEvaluatorParser.NumericEntityOperationContext ctx) {
            return visit(ctx.numericEntity());
        }

        @Override
        public ExpressionNode visitLogicalOrOperation(ExpressionEvaluatorParser.LogicalOrOperationContext ctx) {
            return visit(ctx.logicalOrExpression());
        }

        @Override
        public ExpressionNode visitLogicalOrChainOperation(ExpressionEvaluatorParser.LogicalOrChainOperationContext ctx) {
            return foldLogicalChain(ctx.logicalAndExpression(), BinaryOperator.OR);
        }

        @Override
        public ExpressionNode visitLogicalAndChainOperation(ExpressionEvaluatorParser.LogicalAndChainOperationContext ctx) {
            return foldLogicalChain(ctx.logicalComparisonExpression(), BinaryOperator.AND);
        }

        @Override
        public ExpressionNode visitLogicalComparisonOperation(ExpressionEvaluatorParser.LogicalComparisonOperationContext ctx) {
            ExpressionNode left = visit(ctx.logicalBitwiseExpression(0));
            if (ctx.comparisonOperator() == null) {
                return left;
            }
            ExpressionNode right = visit(ctx.logicalBitwiseExpression(1));
            return new BinaryOperationNode(
                    nodeFactory.nextId("binary"),
                    nodeFactory.sourceSpan(left.sourceSpan(), right.sourceSpan()),
                    comparisonOperator(ctx.comparisonOperator()),
                    left,
                    right
            );
        }

        @Override
        public ExpressionNode visitMathComparisonOperation(ExpressionEvaluatorParser.MathComparisonOperationContext ctx) {
            return comparisonNode(ctx.mathExpression(0), ctx.comparisonOperator(), ctx.mathExpression(1));
        }

        @Override
        public ExpressionNode visitStringComparisonOperation(ExpressionEvaluatorParser.StringComparisonOperationContext ctx) {
            return comparisonNode(ctx.stringConcatExpression(0), ctx.comparisonOperator(), ctx.stringConcatExpression(1));
        }

        @Override
        public ExpressionNode visitRegexMatchOperation(ExpressionEvaluatorParser.RegexMatchOperationContext ctx) {
            ExpressionNode subject = visit(ctx.stringConcatExpression());
            LiteralNode pattern = new LiteralNode(nodeFactory.nextId("literal"), nodeFactory.sourceSpan(ctx.STRING().getSymbol()), ctx.STRING().getText());
            return new BinaryOperationNode(nodeFactory.nextId("binary"), nodeFactory.sourceSpan(ctx), BinaryOperator.REGEX_MATCH, subject, pattern);
        }

        @Override
        public ExpressionNode visitRegexNotMatchOperation(ExpressionEvaluatorParser.RegexNotMatchOperationContext ctx) {
            ExpressionNode subject = visit(ctx.stringConcatExpression());
            LiteralNode pattern = new LiteralNode(nodeFactory.nextId("literal"), nodeFactory.sourceSpan(ctx.STRING().getSymbol()), ctx.STRING().getText());
            return new BinaryOperationNode(nodeFactory.nextId("binary"), nodeFactory.sourceSpan(ctx), BinaryOperator.REGEX_NOT_MATCH, subject, pattern);
        }

        @Override
        public ExpressionNode visitMathInOperation(ExpressionEvaluatorParser.MathInOperationContext ctx) {
            return membershipNode(ctx.mathExpression(), ctx.vectorEntity(), BinaryOperator.IN);
        }

        @Override
        public ExpressionNode visitStringInOperation(ExpressionEvaluatorParser.StringInOperationContext ctx) {
            return membershipNode(ctx.stringConcatExpression(), ctx.vectorEntity(), BinaryOperator.IN);
        }

        @Override
        public ExpressionNode visitDateInOperation(ExpressionEvaluatorParser.DateInOperationContext ctx) {
            return membershipNode(ctx.dateEntity(), ctx.vectorEntity(), BinaryOperator.IN);
        }

        @Override
        public ExpressionNode visitTimeInOperation(ExpressionEvaluatorParser.TimeInOperationContext ctx) {
            return membershipNode(ctx.timeEntity(), ctx.vectorEntity(), BinaryOperator.IN);
        }

        @Override
        public ExpressionNode visitDateTimeInOperation(ExpressionEvaluatorParser.DateTimeInOperationContext ctx) {
            return membershipNode(ctx.dateTimeEntity(), ctx.vectorEntity(), BinaryOperator.IN);
        }

        @Override
        public ExpressionNode visitLogicalInOperation(ExpressionEvaluatorParser.LogicalInOperationContext ctx) {
            return membershipNode(ctx.logicalBitwiseExpression(), ctx.vectorEntity(), BinaryOperator.IN);
        }

        @Override
        public ExpressionNode visitMathNotInOperation(ExpressionEvaluatorParser.MathNotInOperationContext ctx) {
            return membershipNode(ctx.mathExpression(), ctx.vectorEntity(), BinaryOperator.NOT_IN);
        }

        @Override
        public ExpressionNode visitStringNotInOperation(ExpressionEvaluatorParser.StringNotInOperationContext ctx) {
            return membershipNode(ctx.stringConcatExpression(), ctx.vectorEntity(), BinaryOperator.NOT_IN);
        }

        @Override
        public ExpressionNode visitDateNotInOperation(ExpressionEvaluatorParser.DateNotInOperationContext ctx) {
            return membershipNode(ctx.dateEntity(), ctx.vectorEntity(), BinaryOperator.NOT_IN);
        }

        @Override
        public ExpressionNode visitTimeNotInOperation(ExpressionEvaluatorParser.TimeNotInOperationContext ctx) {
            return membershipNode(ctx.timeEntity(), ctx.vectorEntity(), BinaryOperator.NOT_IN);
        }

        @Override
        public ExpressionNode visitDateTimeNotInOperation(ExpressionEvaluatorParser.DateTimeNotInOperationContext ctx) {
            return membershipNode(ctx.dateTimeEntity(), ctx.vectorEntity(), BinaryOperator.NOT_IN);
        }

        @Override
        public ExpressionNode visitLogicalNotInOperation(ExpressionEvaluatorParser.LogicalNotInOperationContext ctx) {
            return membershipNode(ctx.logicalBitwiseExpression(), ctx.vectorEntity(), BinaryOperator.NOT_IN);
        }

        @Override
        public ExpressionNode visitNullInOperation(ExpressionEvaluatorParser.NullInOperationContext ctx) {
            LiteralNode left = new LiteralNode(nodeFactory.nextId("literal"), nodeFactory.sourceSpan(ctx.NULL().getSymbol()), "null");
            ExpressionNode right = visit(ctx.vectorEntity());
            return new BinaryOperationNode(
                    nodeFactory.nextId("binary"),
                    nodeFactory.sourceSpan(left.sourceSpan(), right.sourceSpan()),
                    BinaryOperator.IN,
                    left,
                    right
            );
        }

        @Override
        public ExpressionNode visitNullNotInOperation(ExpressionEvaluatorParser.NullNotInOperationContext ctx) {
            LiteralNode left = new LiteralNode(nodeFactory.nextId("literal"), nodeFactory.sourceSpan(ctx.NULL().getSymbol()), "null");
            ExpressionNode right = visit(ctx.vectorEntity());
            return new BinaryOperationNode(
                    nodeFactory.nextId("binary"),
                    nodeFactory.sourceSpan(left.sourceSpan(), right.sourceSpan()),
                    BinaryOperator.NOT_IN,
                    left,
                    right
            );
        }

        @Override
        public ExpressionNode visitDateComparisonOperation(ExpressionEvaluatorParser.DateComparisonOperationContext ctx) {
            return comparisonNode(ctx.dateEntity(0), ctx.comparisonOperator(), ctx.dateEntity(1));
        }

        @Override
        public ExpressionNode visitTimeComparisonOperation(ExpressionEvaluatorParser.TimeComparisonOperationContext ctx) {
            return comparisonNode(ctx.timeEntity(0), ctx.comparisonOperator(), ctx.timeEntity(1));
        }

        @Override
        public ExpressionNode visitDateTimeComparisonOperation(ExpressionEvaluatorParser.DateTimeComparisonOperationContext ctx) {
            return comparisonNode(ctx.dateTimeEntity(0), ctx.comparisonOperator(), ctx.dateTimeEntity(1));
        }

        @Override
        public ExpressionNode visitLogicalBitwiseOperation(ExpressionEvaluatorParser.LogicalBitwiseOperationContext ctx) {
            ExpressionNode current = visit(ctx.logicalNotExpression(0));
            for (int index = 1; index < ctx.logicalNotExpression().size(); index++) {
                ExpressionNode right = visit(ctx.logicalNotExpression(index));
                BinaryOperator operator = tokenTypeToBitwiseOperator(ctx.getChild(2 * index - 1).getPayload());
                current = new BinaryOperationNode(
                        nodeFactory.nextId("binary"),
                        nodeFactory.sourceSpan(current.sourceSpan(), right.sourceSpan()),
                        operator,
                        current,
                        right
                );
            }
            return current;
        }

        @Override
        public ExpressionNode visitLogicalNotOperation(ExpressionEvaluatorParser.LogicalNotOperationContext ctx) {
            return new UnaryOperationNode(
                    nodeFactory.nextId("unary"),
                    nodeFactory.sourceSpan(ctx),
                    UnaryOperator.LOGICAL_NOT,
                    visit(ctx.logicalNotExpression())
            );
        }

        @Override
        public ExpressionNode visitLogicalPrimaryOperation(ExpressionEvaluatorParser.LogicalPrimaryOperationContext ctx) {
            return visit(ctx.logicalPrimary());
        }

        @Override
        public ExpressionNode visitLogicalExpressionParenthesisOperation(ExpressionEvaluatorParser.LogicalExpressionParenthesisOperationContext ctx) {
            return visit(ctx.logicalExpression());
        }

        @Override
        public ExpressionNode visitLogicalEntityOperation(ExpressionEvaluatorParser.LogicalEntityOperationContext ctx) {
            return visit(ctx.logicalEntity());
        }

        @Override
        public ExpressionNode visitFunctionCallOperation(ExpressionEvaluatorParser.FunctionCallOperationContext ctx) {
            List<ExpressionNode> arguments = ctx.allEntityTypes().stream()
                    .map(this::visitAllEntityType)
                    .toList();
            return new FunctionCallNode(
                    nodeFactory.nextId("call"),
                    nodeFactory.sourceSpan(ctx),
                    ctx.IDENTIFIER().getText(),
                    arguments
            );
        }

        @Override
        public ExpressionNode visitFunctionReferenceTarget(ExpressionEvaluatorParser.FunctionReferenceTargetContext ctx) {
            return visit(ctx.function());
        }

        @Override
        public ExpressionNode visitIdentifierReferenceTarget(ExpressionEvaluatorParser.IdentifierReferenceTargetContext ctx) {
            String rootIdentifier = ctx.IDENTIFIER().getText();
            List<ExpressionEvaluatorParser.MemberChainContext> memberChains = ctx.memberChain();
            if (memberChains.isEmpty()) {
                return new IdentifierNode(
                        nodeFactory.nextId("identifier"),
                        nodeFactory.sourceSpan(ctx),
                        rootIdentifier
                );
            }
            List<PropertyChainNode.MemberAccess> chain = new ArrayList<>();
            for (ExpressionEvaluatorParser.MemberChainContext member : memberChains) {
                PropertyChainNode.MemberAccess access = switch (member) {
                    case ExpressionEvaluatorParser.PropertyAccessContext p ->
                            new PropertyChainNode.PropertyAccess(p.IDENTIFIER().getText());
                    case ExpressionEvaluatorParser.SafePropertyAccessContext sp ->
                            new PropertyChainNode.SafePropertyAccess(sp.IDENTIFIER().getText());
                    case ExpressionEvaluatorParser.MethodCallAccessContext m -> {
                        List<ExpressionNode> args = m.allEntityTypes().stream()
                                .map(this::visitAllEntityType)
                                .toList();
                        yield new PropertyChainNode.MethodCallAccess(m.IDENTIFIER().getText(), args);
                    }
                    case ExpressionEvaluatorParser.SafeMethodCallAccessContext sm -> {
                        List<ExpressionNode> args = sm.allEntityTypes().stream()
                                .map(this::visitAllEntityType)
                                .toList();
                        yield new PropertyChainNode.SafeMethodCallAccess(sm.IDENTIFIER().getText(), args);
                    }
                    default -> throw new IllegalStateException(
                            "unsupported memberChain context: " + member.getClass().getSimpleName());
                };
                chain.add(access);
            }
            return new PropertyChainNode(
                    nodeFactory.nextId("propertyChain"),
                    nodeFactory.sourceSpan(ctx),
                    rootIdentifier,
                    chain
            );
        }

        @Override
        public ExpressionNode visitLogicalConstantOperation(ExpressionEvaluatorParser.LogicalConstantOperationContext ctx) {
            return new LiteralNode(
                    nodeFactory.nextId("literal"),
                    nodeFactory.sourceSpan(ctx),
                    ctx.getText()
            );
        }

        @Override
        public ExpressionNode visitLogicalReferenceOperation(ExpressionEvaluatorParser.LogicalReferenceOperationContext ctx) {
            ExpressionNode left = visit(ctx.referenceTarget());
            if (ctx.logicalEntity() == null) {
                return left;
            }
            ExpressionNode right = visit(ctx.logicalEntity());
            return new BinaryOperationNode(
                    nodeFactory.nextId("binary"),
                    nodeFactory.sourceSpan(left.sourceSpan(), right.sourceSpan()),
                    BinaryOperator.NULL_COALESCE,
                    left,
                    right
            );
        }

        @Override
        public ExpressionNode visitLogicalDecisionOperation(ExpressionEvaluatorParser.LogicalDecisionOperationContext ctx) {
            return conditionalNode(ctx, ctx.logicalExpression());
        }

        @Override
        public ExpressionNode visitLogicalFunctionDecisionOperation(ExpressionEvaluatorParser.LogicalFunctionDecisionOperationContext ctx) {
            return conditionalNode(ctx, ctx.logicalExpression());
        }

        @Override
        public ExpressionNode visitNumericReferenceOperation(ExpressionEvaluatorParser.NumericReferenceOperationContext ctx) {
            ExpressionNode left = visit(ctx.referenceTarget());
            if (ctx.numericEntity() == null) {
                return left;
            }
            ExpressionNode right = visit(ctx.numericEntity());
            return new BinaryOperationNode(
                    nodeFactory.nextId("binary"),
                    nodeFactory.sourceSpan(left.sourceSpan(), right.sourceSpan()),
                    BinaryOperator.NULL_COALESCE,
                    left,
                    right
            );
        }

        @Override
        public ExpressionNode visitNumericConstantOperation(ExpressionEvaluatorParser.NumericConstantOperationContext ctx) {
            return new LiteralNode(
                    nodeFactory.nextId("literal"),
                    nodeFactory.sourceSpan(ctx),
                    ctx.NUMBER().getText()
            );
        }

        @Override
        public ExpressionNode visitMathDecisionOperation(ExpressionEvaluatorParser.MathDecisionOperationContext ctx) {
            return conditionalNode(ctx, ctx.logicalExpression(), ctx.mathExpression());
        }

        @Override
        public ExpressionNode visitMathFunctionDecisionOperation(ExpressionEvaluatorParser.MathFunctionDecisionOperationContext ctx) {
            return conditionalNode(ctx, ctx.logicalExpression(), ctx.mathExpression());
        }

        @Override
        public ExpressionNode visitStringConcatenationOperation(ExpressionEvaluatorParser.StringConcatenationOperationContext ctx) {
            ExpressionNode current = visit(ctx.stringEntity(0));
            for (int index = 1; index < ctx.stringEntity().size(); index++) {
                ExpressionNode right = visit(ctx.stringEntity(index));
                current = new BinaryOperationNode(
                        nodeFactory.nextId("binary"),
                        nodeFactory.sourceSpan(current.sourceSpan(), right.sourceSpan()),
                        BinaryOperator.CONCATENATE,
                        current,
                        right
                );
            }
            return current;
        }

        @Override
        public ExpressionNode visitStringConstantOperation(ExpressionEvaluatorParser.StringConstantOperationContext ctx) {
            return new LiteralNode(
                    nodeFactory.nextId("literal"),
                    nodeFactory.sourceSpan(ctx),
                    ctx.STRING().getText()
            );
        }

        @Override
        public ExpressionNode visitStringReferenceOperation(ExpressionEvaluatorParser.StringReferenceOperationContext ctx) {
            ExpressionNode left = visit(ctx.referenceTarget());
            if (ctx.stringConcatExpression() == null) {
                return left;
            }
            ExpressionNode right = visit(ctx.stringConcatExpression());
            return new BinaryOperationNode(
                    nodeFactory.nextId("binary"),
                    nodeFactory.sourceSpan(left.sourceSpan(), right.sourceSpan()),
                    BinaryOperator.NULL_COALESCE,
                    left,
                    right
            );
        }

        @Override
        public ExpressionNode visitStringDecisionOperation(ExpressionEvaluatorParser.StringDecisionOperationContext ctx) {
            return conditionalNode(ctx, ctx.logicalExpression(), ctx.stringConcatExpression());
        }

        @Override
        public ExpressionNode visitStringFunctionDecisionOperation(ExpressionEvaluatorParser.StringFunctionDecisionOperationContext ctx) {
            return conditionalNode(ctx, ctx.logicalExpression(), ctx.stringConcatExpression());
        }

        @Override
        public ExpressionNode visitDateConstantOperation(ExpressionEvaluatorParser.DateConstantOperationContext ctx) {
            return literal(ctx, ctx.DATE().getText());
        }

        @Override
        public ExpressionNode visitDateCurrentValueOperation(ExpressionEvaluatorParser.DateCurrentValueOperationContext ctx) {
            return literal(ctx, ctx.NOW_DATE().getText());
        }

        @Override
        public ExpressionNode visitDateReferenceOperation(ExpressionEvaluatorParser.DateReferenceOperationContext ctx) {
            ExpressionNode left = visit(ctx.referenceTarget());
            if (ctx.dateEntity() == null) {
                return left;
            }
            ExpressionNode right = visit(ctx.dateEntity());
            return new BinaryOperationNode(
                    nodeFactory.nextId("binary"),
                    nodeFactory.sourceSpan(left.sourceSpan(), right.sourceSpan()),
                    BinaryOperator.NULL_COALESCE,
                    left,
                    right
            );
        }

        @Override
        public ExpressionNode visitDateDecisionOperation(ExpressionEvaluatorParser.DateDecisionOperationContext ctx) {
            return conditionalNode(ctx, ctx.logicalExpression(), ctx.dateEntity());
        }

        @Override
        public ExpressionNode visitDateFunctionDecisionOperation(ExpressionEvaluatorParser.DateFunctionDecisionOperationContext ctx) {
            return conditionalNode(ctx, ctx.logicalExpression(), ctx.dateEntity());
        }

        @Override
        public ExpressionNode visitTimeConstantOperation(ExpressionEvaluatorParser.TimeConstantOperationContext ctx) {
            return literal(ctx, ctx.TIME().getText());
        }

        @Override
        public ExpressionNode visitTimeCurrentValueOperation(ExpressionEvaluatorParser.TimeCurrentValueOperationContext ctx) {
            return literal(ctx, ctx.NOW_TIME().getText());
        }

        @Override
        public ExpressionNode visitTimeReferenceOperation(ExpressionEvaluatorParser.TimeReferenceOperationContext ctx) {
            ExpressionNode left = visit(ctx.referenceTarget());
            if (ctx.timeEntity() == null) {
                return left;
            }
            ExpressionNode right = visit(ctx.timeEntity());
            return new BinaryOperationNode(
                    nodeFactory.nextId("binary"),
                    nodeFactory.sourceSpan(left.sourceSpan(), right.sourceSpan()),
                    BinaryOperator.NULL_COALESCE,
                    left,
                    right
            );
        }

        @Override
        public ExpressionNode visitTimeDecisionOperation(ExpressionEvaluatorParser.TimeDecisionOperationContext ctx) {
            return conditionalNode(ctx, ctx.logicalExpression(), ctx.timeEntity());
        }

        @Override
        public ExpressionNode visitTimeFunctionDecisionOperation(ExpressionEvaluatorParser.TimeFunctionDecisionOperationContext ctx) {
            return conditionalNode(ctx, ctx.logicalExpression(), ctx.timeEntity());
        }

        @Override
        public ExpressionNode visitDateTimeConstantOperation(ExpressionEvaluatorParser.DateTimeConstantOperationContext ctx) {
            String value = ctx.TIME_OFFSET() == null
                    ? ctx.DATETIME().getText()
                    : ctx.DATETIME().getText() + ctx.TIME_OFFSET().getText();
            return literal(ctx, value);
        }

        @Override
        public ExpressionNode visitDateTimeCurrentValueOperation(ExpressionEvaluatorParser.DateTimeCurrentValueOperationContext ctx) {
            return literal(ctx, ctx.NOW_DATETIME().getText());
        }

        @Override
        public ExpressionNode visitDateTimeReferenceOperation(ExpressionEvaluatorParser.DateTimeReferenceOperationContext ctx) {
            ExpressionNode left = visit(ctx.referenceTarget());
            if (ctx.dateTimeEntity() == null) {
                return left;
            }
            ExpressionNode right = visit(ctx.dateTimeEntity());
            return new BinaryOperationNode(
                    nodeFactory.nextId("binary"),
                    nodeFactory.sourceSpan(left.sourceSpan(), right.sourceSpan()),
                    BinaryOperator.NULL_COALESCE,
                    left,
                    right
            );
        }

        @Override
        public ExpressionNode visitDateTimeDecisionOperation(ExpressionEvaluatorParser.DateTimeDecisionOperationContext ctx) {
            return conditionalNode(ctx, ctx.logicalExpression(), ctx.dateTimeEntity());
        }

        @Override
        public ExpressionNode visitDateTimeFunctionDecisionOperation(ExpressionEvaluatorParser.DateTimeFunctionDecisionOperationContext ctx) {
            return conditionalNode(ctx, ctx.logicalExpression(), ctx.dateTimeEntity());
        }

        @Override
        public ExpressionNode visitGenericDecisionExpression(ExpressionEvaluatorParser.GenericDecisionExpressionContext ctx) {
            return conditionalNode(ctx, ctx.logicalExpression(), ctx.genericEntity());
        }

        @Override
        public ExpressionNode visitGenericFunctionDecisionExpression(ExpressionEvaluatorParser.GenericFunctionDecisionExpressionContext ctx) {
            return conditionalNode(ctx, ctx.logicalExpression(), ctx.genericEntity());
        }

        @Override
        public ExpressionNode visitCastExpressionOperation(ExpressionEvaluatorParser.CastExpressionOperationContext ctx) {
            return visit(ctx.castExpression());
        }

        @Override
        public ExpressionNode visitTypeCastOperation(ExpressionEvaluatorParser.TypeCastOperationContext ctx) {
            return visit(ctx.genericEntity());
        }

        @Override
        public ExpressionNode visitReferenceTargetOperation(ExpressionEvaluatorParser.ReferenceTargetOperationContext ctx) {
            return visit(ctx.referenceTarget());
        }

        @Override
        public ExpressionNode visitVectorOfEntitiesOperation(ExpressionEvaluatorParser.VectorOfEntitiesOperationContext ctx) {
            return new VectorLiteralNode(
                    nodeFactory.nextId("vector"),
                    nodeFactory.sourceSpan(ctx),
                    ctx.allEntityTypes().stream()
                            .map(this::visitAllEntityType)
                            .toList()
            );
        }

        @Override
        public ExpressionNode visitVectorReferenceOperation(ExpressionEvaluatorParser.VectorReferenceOperationContext ctx) {
            ExpressionNode left = visit(ctx.referenceTarget());
            if (ctx.vectorEntity() == null) {
                return left;
            }
            ExpressionNode right = visit(ctx.vectorEntity());
            return new BinaryOperationNode(
                    nodeFactory.nextId("binary"),
                    nodeFactory.sourceSpan(left.sourceSpan(), right.sourceSpan()),
                    BinaryOperator.NULL_COALESCE,
                    left,
                    right
            );
        }

        @Override
        public ExpressionNode visitVectorDecisionOperation(ExpressionEvaluatorParser.VectorDecisionOperationContext ctx) {
            return conditionalNode(ctx, ctx.logicalExpression(), ctx.vectorEntity());
        }

        @Override
        public ExpressionNode visitVectorFunctionDecisionOperation(ExpressionEvaluatorParser.VectorFunctionDecisionOperationContext ctx) {
            return conditionalNode(ctx, ctx.logicalExpression(), ctx.vectorEntity());
        }

        @Override
        public ExpressionNode visitMathBetweenOperation(ExpressionEvaluatorParser.MathBetweenOperationContext ctx) {
            return betweenNode(ctx.mathExpression(0), ctx.mathExpression(1), ctx.mathExpression(2), TernaryOperator.BETWEEN);
        }

        @Override
        public ExpressionNode visitStringBetweenOperation(ExpressionEvaluatorParser.StringBetweenOperationContext ctx) {
            return betweenNode(ctx.stringConcatExpression(0), ctx.stringConcatExpression(1), ctx.stringConcatExpression(2), TernaryOperator.BETWEEN);
        }

        @Override
        public ExpressionNode visitDateBetweenOperation(ExpressionEvaluatorParser.DateBetweenOperationContext ctx) {
            return betweenNode(ctx.dateEntity(0), ctx.dateEntity(1), ctx.dateEntity(2), TernaryOperator.BETWEEN);
        }

        @Override
        public ExpressionNode visitTimeBetweenOperation(ExpressionEvaluatorParser.TimeBetweenOperationContext ctx) {
            return betweenNode(ctx.timeEntity(0), ctx.timeEntity(1), ctx.timeEntity(2), TernaryOperator.BETWEEN);
        }

        @Override
        public ExpressionNode visitDateTimeBetweenOperation(ExpressionEvaluatorParser.DateTimeBetweenOperationContext ctx) {
            return betweenNode(ctx.dateTimeEntity(0), ctx.dateTimeEntity(1), ctx.dateTimeEntity(2), TernaryOperator.BETWEEN);
        }

        @Override
        public ExpressionNode visitMathNotBetweenOperation(ExpressionEvaluatorParser.MathNotBetweenOperationContext ctx) {
            return betweenNode(ctx.mathExpression(0), ctx.mathExpression(1), ctx.mathExpression(2), TernaryOperator.NOT_BETWEEN);
        }

        @Override
        public ExpressionNode visitStringNotBetweenOperation(ExpressionEvaluatorParser.StringNotBetweenOperationContext ctx) {
            return betweenNode(ctx.stringConcatExpression(0), ctx.stringConcatExpression(1), ctx.stringConcatExpression(2), TernaryOperator.NOT_BETWEEN);
        }

        @Override
        public ExpressionNode visitDateNotBetweenOperation(ExpressionEvaluatorParser.DateNotBetweenOperationContext ctx) {
            return betweenNode(ctx.dateEntity(0), ctx.dateEntity(1), ctx.dateEntity(2), TernaryOperator.NOT_BETWEEN);
        }

        @Override
        public ExpressionNode visitTimeNotBetweenOperation(ExpressionEvaluatorParser.TimeNotBetweenOperationContext ctx) {
            return betweenNode(ctx.timeEntity(0), ctx.timeEntity(1), ctx.timeEntity(2), TernaryOperator.NOT_BETWEEN);
        }

        @Override
        public ExpressionNode visitDateTimeNotBetweenOperation(ExpressionEvaluatorParser.DateTimeNotBetweenOperationContext ctx) {
            return betweenNode(ctx.dateTimeEntity(0), ctx.dateTimeEntity(1), ctx.dateTimeEntity(2), TernaryOperator.NOT_BETWEEN);
        }

        private ExpressionNode betweenNode(
                ParserRuleContext valueContext,
                ParserRuleContext lowerContext,
                ParserRuleContext upperContext,
                TernaryOperator operator) {
            ExpressionNode value = visit(valueContext);
            ExpressionNode lower = visit(lowerContext);
            ExpressionNode upper = visit(upperContext);
            return new TernaryOperationNode(
                    nodeFactory.nextId("ternary"),
                    nodeFactory.sourceSpan(value.sourceSpan(), upper.sourceSpan()),
                    operator,
                    value,
                    lower,
                    upper
            );
        }

        private ExpressionNode membershipNode(ParserRuleContext leftContext, ParserRuleContext rightContext, BinaryOperator operator) {
            ExpressionNode left = visit(leftContext);
            ExpressionNode right = visit(rightContext);
            return new BinaryOperationNode(
                    nodeFactory.nextId("binary"),
                    nodeFactory.sourceSpan(left.sourceSpan(), right.sourceSpan()),
                    operator,
                    left,
                    right
            );
        }

        private ExpressionNode comparisonNode(
                ParserRuleContext leftContext,
                ExpressionEvaluatorParser.ComparisonOperatorContext operatorContext,
                ParserRuleContext rightContext
        ) {
            ExpressionNode left = visit(leftContext);
            ExpressionNode right = visit(rightContext);
            return new BinaryOperationNode(
                    nodeFactory.nextId("binary"),
                    nodeFactory.sourceSpan(left.sourceSpan(), right.sourceSpan()),
                    comparisonOperator(operatorContext),
                    left,
                    right
            );
        }

        private ConditionalNode conditionalNode(
                ParserRuleContext ctx,
                List<ExpressionEvaluatorParser.LogicalExpressionContext> logicalExpressions
        ) {
            if (logicalExpressions.size() < 3 || logicalExpressions.size() % 2 == 0) {
                throw new IllegalStateException("logical conditional expression must contain condition/result pairs plus else");
            }
            List<ExpressionNode> conditions = new ArrayList<>(logicalExpressions.size() / 2);
            List<ExpressionNode> results = new ArrayList<>(logicalExpressions.size() / 2);
            for (int index = 0; index < logicalExpressions.size() - 1; index += 2) {
                conditions.add(visit(logicalExpressions.get(index)));
                results.add(visit(logicalExpressions.get(index + 1)));
            }
            return new ConditionalNode(
                    nodeFactory.nextId("conditional"),
                    nodeFactory.sourceSpan(ctx),
                    conditions,
                    results,
                    visit(logicalExpressions.getLast())
            );
        }

        private ConditionalNode conditionalNode(
                ParserRuleContext ctx,
                List<? extends ParserRuleContext> conditionContexts,
                List<? extends ParserRuleContext> resultContexts
        ) {
            if (conditionContexts.isEmpty()) {
                throw new IllegalStateException("conditional expression must contain at least one condition");
            }
            if (resultContexts.size() != conditionContexts.size() + 1) {
                throw new IllegalStateException("conditional expression must contain one else branch");
            }
            List<ExpressionNode> conditions = conditionContexts.stream()
                    .map(this::visit)
                    .toList();
            List<ExpressionNode> results = resultContexts.subList(0, resultContexts.size() - 1).stream()
                    .map(this::visit)
                    .toList();
            return new ConditionalNode(
                    nodeFactory.nextId("conditional"),
                    nodeFactory.sourceSpan(ctx),
                    conditions,
                    results,
                    visit(resultContexts.getLast())
            );
        }

        private ExpressionNode foldLogicalChain(List<? extends ParserRuleContext> contexts, BinaryOperator operator) {
            ExpressionNode current = visit(contexts.getFirst());
            for (int index = 1; index < contexts.size(); index++) {
                ExpressionNode right = visit(contexts.get(index));
                current = new BinaryOperationNode(
                        nodeFactory.nextId("binary"),
                        nodeFactory.sourceSpan(current.sourceSpan(), right.sourceSpan()),
                        operator,
                        current,
                        right
                );
            }
            return current;
        }

        private ExpressionNode visitAllEntityType(ExpressionEvaluatorParser.AllEntityTypesContext ctx) {
            if (ctx instanceof ExpressionEvaluatorParser.MathEntityTypeContext mathEntityType) {
                return visit(mathEntityType.mathExpression());
            }
            if (ctx instanceof ExpressionEvaluatorParser.LogicalEntityTypeContext logicalEntityType) {
                return visit(logicalEntityType.logicalExpression());
            }
            if (ctx instanceof ExpressionEvaluatorParser.StringEntityTypeContext stringEntityType) {
                return visit(stringEntityType.stringConcatExpression());
            }
            if (ctx instanceof ExpressionEvaluatorParser.DateEntityTypeContext dateEntityType) {
                return visit(dateEntityType.dateEntity());
            }
            if (ctx instanceof ExpressionEvaluatorParser.TimeEntityTypeContext timeEntityType) {
                return visit(timeEntityType.timeEntity());
            }
            if (ctx instanceof ExpressionEvaluatorParser.DateTimeEntityTypeContext dateTimeEntityType) {
                return visit(dateTimeEntityType.dateTimeEntity());
            }
            if (ctx instanceof ExpressionEvaluatorParser.VectorEntityTypeContext vectorEntityType) {
                return visit(vectorEntityType.vectorEntity());
            }
            if (ctx instanceof ExpressionEvaluatorParser.NullEntityTypeContext nullEntityType) {
                return literal(nullEntityType, "null");
            }
            throw new IllegalStateException("unsupported allEntityTypes context: " + ctx.getClass().getSimpleName());
        }

        private LiteralNode literal(ParserRuleContext ctx, String value) {
            return new LiteralNode(
                    nodeFactory.nextId("literal"),
                    nodeFactory.sourceSpan(ctx),
                    value
            );
        }

        private BinaryOperator comparisonOperator(ExpressionEvaluatorParser.ComparisonOperatorContext ctx) {
            return switch (ctx) {
                case ExpressionEvaluatorParser.GreaterThanOperatorContext ignored -> BinaryOperator.GREATER_THAN;
                case ExpressionEvaluatorParser.GreaterThanOrEqualOperatorContext ignored ->
                        BinaryOperator.GREATER_THAN_OR_EQUAL;
                case ExpressionEvaluatorParser.LessThanOperatorContext ignored -> BinaryOperator.LESS_THAN;
                case ExpressionEvaluatorParser.LessThanOrEqualOperatorContext ignored ->
                        BinaryOperator.LESS_THAN_OR_EQUAL;
                case ExpressionEvaluatorParser.EqualOperatorContext ignored -> BinaryOperator.EQUAL;
                case ExpressionEvaluatorParser.NotEqualOperatorContext ignored -> BinaryOperator.NOT_EQUAL;
                default ->
                        throw new IllegalStateException("unsupported comparison operator: " + ctx.getClass().getSimpleName());
            };
        }

        private BinaryOperator tokenTypeToAdditiveOperator(Object payload) {
            Token token = (Token) payload;
            return token.getType() == ExpressionEvaluatorParser.PLUS ? BinaryOperator.ADD : BinaryOperator.SUBTRACT;
        }

        private BinaryOperator tokenTypeToMultiplicativeOperator(Object payload) {
            Token token = (Token) payload;
            return switch (token.getType()) {
                case ExpressionEvaluatorParser.MULT -> BinaryOperator.MULTIPLY;
                case ExpressionEvaluatorParser.DIV -> BinaryOperator.DIVIDE;
                case ExpressionEvaluatorParser.MODULO -> BinaryOperator.MODULO;
                default -> throw new IllegalStateException("unsupported multiplicative operator: " + token.getText());
            };
        }

        private BinaryOperator tokenTypeToBitwiseOperator(Object payload) {
            Token token = (Token) payload;
            return switch (token.getType()) {
                case ExpressionEvaluatorParser.NAND -> BinaryOperator.NAND;
                case ExpressionEvaluatorParser.NOR -> BinaryOperator.NOR;
                case ExpressionEvaluatorParser.XOR -> BinaryOperator.XOR;
                case ExpressionEvaluatorParser.XNOR -> BinaryOperator.XNOR;
                default -> throw new IllegalStateException("unsupported logical operator: " + token.getText());
            };
        }
    }

    private static final class NodeFactory {

        private final AtomicLong sequence = new AtomicLong();

        private NodeId nextId(String prefix) {
            return new NodeId(prefix + "-" + sequence.incrementAndGet());
        }

        private SourceSpan sourceSpan(ParserRuleContext context) {
            return sourceSpan(context.getStart(), context.getStop());
        }

        private SourceSpan sourceSpan(Token token) {
            return sourceSpan(token, token);
        }

        private SourceSpan sourceSpan(SourceSpan start, SourceSpan end) {
            return new SourceSpan(
                    start.startOffset(),
                    end.endOffset(),
                    start.startLine(),
                    start.startColumn(),
                    end.endLine(),
                    end.endColumn()
            );
        }

        private SourceSpan sourceSpan(Token start, Token stop) {
            Token effectiveStop = stop == null ? start : stop;
            return new SourceSpan(
                    Math.max(0, start.getStartIndex()),
                    Math.max(0, effectiveStop.getStopIndex()),
                    start.getLine(),
                    start.getCharPositionInLine(),
                    effectiveStop.getLine(),
                    endColumn(effectiveStop)
            );
        }

        private int endColumn(Token token) {
            if (token.getType() == Token.EOF) {
                return token.getCharPositionInLine();
            }
            return token.getCharPositionInLine() + token.getText().length();
        }
    }
}
