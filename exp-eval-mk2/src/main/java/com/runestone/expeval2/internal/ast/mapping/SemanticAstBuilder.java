package com.runestone.expeval2.internal.ast.mapping;

import com.runestone.expeval2.grammar.language.ExpressionEvaluatorV2BaseVisitor;
import com.runestone.expeval2.grammar.language.ExpressionEvaluatorV2Parser;
import com.runestone.expeval2.internal.ast.*;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public final class SemanticAstBuilder {

    public ExpressionFileNode buildMath(ExpressionEvaluatorV2Parser.MathStartContext root) {
        Objects.requireNonNull(root, "root must not be null");
        ExpressionEvaluatorV2Parser.MathInputContext input = (ExpressionEvaluatorV2Parser.MathInputContext) root;
        NodeFactory nodeFactory = new NodeFactory();
        return new ExpressionFileNode(
            nodeFactory.nextId("file"),
            nodeFactory.sourceSpan(input),
            buildAssignments(input.assignmentExpression(), nodeFactory),
            new ExpressionVisitor(nodeFactory).visit(input.mathExpression())
        );
    }

    public ExpressionFileNode buildLogical(ExpressionEvaluatorV2Parser.LogicalStartContext root) {
        Objects.requireNonNull(root, "root must not be null");
        ExpressionEvaluatorV2Parser.LogicalInputContext input = (ExpressionEvaluatorV2Parser.LogicalInputContext) root;
        NodeFactory nodeFactory = new NodeFactory();
        return new ExpressionFileNode(
            nodeFactory.nextId("file"),
            nodeFactory.sourceSpan(input),
            buildAssignments(input.assignmentExpression(), nodeFactory),
            new ExpressionVisitor(nodeFactory).visit(input.logicalExpression())
        );
    }

    private static List<AssignmentNode> buildAssignments(
        List<ExpressionEvaluatorV2Parser.AssignmentExpressionContext> assignments,
        NodeFactory nodeFactory
    ) {
        List<AssignmentNode> nodes = new ArrayList<>(assignments.size());
        ExpressionVisitor expressionVisitor = new ExpressionVisitor(nodeFactory);
        for (ExpressionEvaluatorV2Parser.AssignmentExpressionContext assignment : assignments) {
            nodes.add(buildAssignment(assignment, expressionVisitor, nodeFactory));
        }
        return List.copyOf(nodes);
    }

    private static AssignmentNode buildAssignment(
        ExpressionEvaluatorV2Parser.AssignmentExpressionContext assignment,
        ExpressionVisitor expressionVisitor,
        NodeFactory nodeFactory
    ) {
        if (assignment instanceof ExpressionEvaluatorV2Parser.AssignmentOperationContext simpleAssignment) {
            return new SimpleAssignmentNode(
                nodeFactory.nextId("assign"),
                nodeFactory.sourceSpan(simpleAssignment),
                simpleAssignment.IDENTIFIER().getText(),
                buildAssignmentValue(simpleAssignment.assignmentValue(), expressionVisitor)
            );
        }
        if (assignment instanceof ExpressionEvaluatorV2Parser.DestructuringAssignmentOperationContext destructuringAssignment) {
            if (!(destructuringAssignment.vectorOfVariables() instanceof ExpressionEvaluatorV2Parser.VectorOfVariablesOperationContext vectorOfVariables)) {
                throw new IllegalStateException("unsupported vectorOfVariables context: "
                    + destructuringAssignment.vectorOfVariables().getClass().getSimpleName());
            }
            List<String> targetNames = vectorOfVariables.IDENTIFIER().stream()
                .map(TerminalNode::getText)
                .toList();
            ExpressionNode value = destructuringAssignment.vectorEntity() != null
                ? expressionVisitor.visit(destructuringAssignment.vectorEntity())
                : expressionVisitor.visit(destructuringAssignment.function());
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
        ExpressionEvaluatorV2Parser.AssignmentValueContext assignmentValue,
        ExpressionVisitor expressionVisitor
    ) {
        if (assignmentValue instanceof ExpressionEvaluatorV2Parser.MathAssignmentValueContext mathAssignmentValue) {
            return expressionVisitor.visit(mathAssignmentValue.mathExpression());
        }
        if (assignmentValue instanceof ExpressionEvaluatorV2Parser.LogicalAssignmentValueContext logicalAssignmentValue) {
            return expressionVisitor.visit(logicalAssignmentValue.logicalExpression());
        }
        if (assignmentValue instanceof ExpressionEvaluatorV2Parser.GenericAssignmentValueContext genericAssignmentValue) {
            return expressionVisitor.visit(genericAssignmentValue.genericEntity());
        }
        if (assignmentValue instanceof ExpressionEvaluatorV2Parser.StringAssignmentValueContext stringAssignmentValue) {
            return expressionVisitor.visit(stringAssignmentValue.stringEntity());
        }
        if (assignmentValue instanceof ExpressionEvaluatorV2Parser.DateAssignmentValueContext dateAssignmentValue) {
            return expressionVisitor.visit(dateAssignmentValue.dateEntity());
        }
        if (assignmentValue instanceof ExpressionEvaluatorV2Parser.TimeAssignmentValueContext timeAssignmentValue) {
            return expressionVisitor.visit(timeAssignmentValue.timeEntity());
        }
        if (assignmentValue instanceof ExpressionEvaluatorV2Parser.DateTimeAssignmentValueContext dateTimeAssignmentValue) {
            return expressionVisitor.visit(dateTimeAssignmentValue.dateTimeEntity());
        }
        if (assignmentValue instanceof ExpressionEvaluatorV2Parser.VectorAssignmentValueContext vectorAssignmentValue) {
            return expressionVisitor.visit(vectorAssignmentValue.vectorEntity());
        }
        throw new IllegalStateException("unsupported assignment value: " + assignmentValue.getClass().getSimpleName());
    }

    private static final class ExpressionVisitor extends ExpressionEvaluatorV2BaseVisitor<ExpressionNode> {

        private final NodeFactory nodeFactory;

        private ExpressionVisitor(NodeFactory nodeFactory) {
            this.nodeFactory = nodeFactory;
        }

        @Override
        public ExpressionNode visitSumOperation(ExpressionEvaluatorV2Parser.SumOperationContext ctx) {
            return visit(ctx.sumExpression());
        }

        @Override
        public ExpressionNode visitAdditiveOperation(ExpressionEvaluatorV2Parser.AdditiveOperationContext ctx) {
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
        public ExpressionNode visitMultiplicativeOperation(ExpressionEvaluatorV2Parser.MultiplicativeOperationContext ctx) {
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
        public ExpressionNode visitUnaryMinusOperation(ExpressionEvaluatorV2Parser.UnaryMinusOperationContext ctx) {
            return new UnaryOperationNode(
                nodeFactory.nextId("unary"),
                nodeFactory.sourceSpan(ctx),
                UnaryOperator.NEGATE,
                visit(ctx.unaryExpression())
            );
        }

        @Override
        public ExpressionNode visitRootExpressionOperation(ExpressionEvaluatorV2Parser.RootExpressionOperationContext ctx) {
            return visit(ctx.rootExpression());
        }

        @Override
        public ExpressionNode visitRootChainOperation(ExpressionEvaluatorV2Parser.RootChainOperationContext ctx) {
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
        public ExpressionNode visitExponentiationOperation(ExpressionEvaluatorV2Parser.ExponentiationOperationContext ctx) {
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
        public ExpressionNode visitPostfixOperation(ExpressionEvaluatorV2Parser.PostfixOperationContext ctx) {
            ExpressionNode current = visit(ctx.primaryMathExpression());
            for (int childIndex = 1; childIndex < ctx.getChildCount(); childIndex++) {
                TerminalNode operator = (TerminalNode) ctx.getChild(childIndex);
                current = new PostfixOperationNode(
                    nodeFactory.nextId("postfix"),
                    nodeFactory.sourceSpan(current.sourceSpan(), nodeFactory.sourceSpan(operator.getSymbol())),
                    operator.getSymbol().getType() == ExpressionEvaluatorV2Parser.PERCENT
                        ? PostfixOperator.PERCENT
                        : PostfixOperator.FACTORIAL,
                    current
                );
            }
            return current;
        }

        @Override
        public ExpressionNode visitMathExpressionParenthesisOperation(ExpressionEvaluatorV2Parser.MathExpressionParenthesisOperationContext ctx) {
            return visit(ctx.mathExpression());
        }

        @Override
        public ExpressionNode visitSquareRootOperation(ExpressionEvaluatorV2Parser.SquareRootOperationContext ctx) {
            return new UnaryOperationNode(
                nodeFactory.nextId("unary"),
                nodeFactory.sourceSpan(ctx),
                UnaryOperator.SQRT,
                visit(ctx.mathExpression())
            );
        }

        @Override
        public ExpressionNode visitModulusOperation(ExpressionEvaluatorV2Parser.ModulusOperationContext ctx) {
            return new UnaryOperationNode(
                nodeFactory.nextId("unary"),
                nodeFactory.sourceSpan(ctx),
                UnaryOperator.MODULUS,
                visit(ctx.mathExpression())
            );
        }

        @Override
        public ExpressionNode visitNumericEntityOperation(ExpressionEvaluatorV2Parser.NumericEntityOperationContext ctx) {
            return visit(ctx.numericEntity());
        }

        @Override
        public ExpressionNode visitLogicalOrOperation(ExpressionEvaluatorV2Parser.LogicalOrOperationContext ctx) {
            return visit(ctx.logicalOrExpression());
        }

        @Override
        public ExpressionNode visitLogicalOrChainOperation(ExpressionEvaluatorV2Parser.LogicalOrChainOperationContext ctx) {
            return foldLogicalChain(ctx.logicalAndExpression(), BinaryOperator.OR);
        }

        @Override
        public ExpressionNode visitLogicalAndChainOperation(ExpressionEvaluatorV2Parser.LogicalAndChainOperationContext ctx) {
            return foldLogicalChain(ctx.logicalComparisonExpression(), BinaryOperator.AND);
        }

        @Override
        public ExpressionNode visitLogicalComparisonOperation(ExpressionEvaluatorV2Parser.LogicalComparisonOperationContext ctx) {
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
        public ExpressionNode visitMathComparisonOperation(ExpressionEvaluatorV2Parser.MathComparisonOperationContext ctx) {
            return comparisonNode(ctx.mathExpression(0), ctx.comparisonOperator(), ctx.mathExpression(1));
        }

        @Override
        public ExpressionNode visitStringComparisonOperation(ExpressionEvaluatorV2Parser.StringComparisonOperationContext ctx) {
            return comparisonNode(ctx.stringEntity(0), ctx.comparisonOperator(), ctx.stringEntity(1));
        }

        @Override
        public ExpressionNode visitDateComparisonOperation(ExpressionEvaluatorV2Parser.DateComparisonOperationContext ctx) {
            return comparisonNode(ctx.dateEntity(0), ctx.comparisonOperator(), ctx.dateEntity(1));
        }

        @Override
        public ExpressionNode visitTimeComparisonOperation(ExpressionEvaluatorV2Parser.TimeComparisonOperationContext ctx) {
            return comparisonNode(ctx.timeEntity(0), ctx.comparisonOperator(), ctx.timeEntity(1));
        }

        @Override
        public ExpressionNode visitDateTimeComparisonOperation(ExpressionEvaluatorV2Parser.DateTimeComparisonOperationContext ctx) {
            return comparisonNode(ctx.dateTimeEntity(0), ctx.comparisonOperator(), ctx.dateTimeEntity(1));
        }

        @Override
        public ExpressionNode visitLogicalBitwiseOperation(ExpressionEvaluatorV2Parser.LogicalBitwiseOperationContext ctx) {
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
        public ExpressionNode visitLogicalNotOperation(ExpressionEvaluatorV2Parser.LogicalNotOperationContext ctx) {
            return new UnaryOperationNode(
                nodeFactory.nextId("unary"),
                nodeFactory.sourceSpan(ctx),
                UnaryOperator.LOGICAL_NOT,
                visit(ctx.logicalNotExpression())
            );
        }

        @Override
        public ExpressionNode visitLogicalPrimaryOperation(ExpressionEvaluatorV2Parser.LogicalPrimaryOperationContext ctx) {
            return visit(ctx.logicalPrimary());
        }

        @Override
        public ExpressionNode visitLogicalExpressionParenthesisOperation(ExpressionEvaluatorV2Parser.LogicalExpressionParenthesisOperationContext ctx) {
            return visit(ctx.logicalExpression());
        }

        @Override
        public ExpressionNode visitLogicalEntityOperation(ExpressionEvaluatorV2Parser.LogicalEntityOperationContext ctx) {
            return visit(ctx.logicalEntity());
        }

        @Override
        public ExpressionNode visitFunctionCallOperation(ExpressionEvaluatorV2Parser.FunctionCallOperationContext ctx) {
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
        public ExpressionNode visitFunctionReferenceTarget(ExpressionEvaluatorV2Parser.FunctionReferenceTargetContext ctx) {
            return visit(ctx.function());
        }

        @Override
        public ExpressionNode visitIdentifierReferenceTarget(ExpressionEvaluatorV2Parser.IdentifierReferenceTargetContext ctx) {
            return new IdentifierNode(
                nodeFactory.nextId("identifier"),
                nodeFactory.sourceSpan(ctx),
                ctx.IDENTIFIER().getText()
            );
        }

        @Override
        public ExpressionNode visitLogicalConstantOperation(ExpressionEvaluatorV2Parser.LogicalConstantOperationContext ctx) {
            return new LiteralNode(
                nodeFactory.nextId("literal"),
                nodeFactory.sourceSpan(ctx),
                ctx.getText()
            );
        }

        @Override
        public ExpressionNode visitLogicalReferenceOperation(ExpressionEvaluatorV2Parser.LogicalReferenceOperationContext ctx) {
            return visit(ctx.referenceTarget());
        }

        @Override
        public ExpressionNode visitLogicalDecisionOperation(ExpressionEvaluatorV2Parser.LogicalDecisionOperationContext ctx) {
            return conditionalNode(ctx, ctx.logicalExpression());
        }

        @Override
        public ExpressionNode visitLogicalFunctionDecisionOperation(ExpressionEvaluatorV2Parser.LogicalFunctionDecisionOperationContext ctx) {
            return conditionalNode(ctx, ctx.logicalExpression());
        }

        @Override
        public ExpressionNode visitNumericReferenceOperation(ExpressionEvaluatorV2Parser.NumericReferenceOperationContext ctx) {
            return visit(ctx.referenceTarget());
        }

        @Override
        public ExpressionNode visitNumericConstantOperation(ExpressionEvaluatorV2Parser.NumericConstantOperationContext ctx) {
            return new LiteralNode(
                nodeFactory.nextId("literal"),
                nodeFactory.sourceSpan(ctx),
                ctx.NUMBER().getText()
            );
        }

        @Override
        public ExpressionNode visitMathDecisionOperation(ExpressionEvaluatorV2Parser.MathDecisionOperationContext ctx) {
            return conditionalNode(ctx, ctx.logicalExpression(), ctx.mathExpression());
        }

        @Override
        public ExpressionNode visitMathFunctionDecisionOperation(ExpressionEvaluatorV2Parser.MathFunctionDecisionOperationContext ctx) {
            return conditionalNode(ctx, ctx.logicalExpression(), ctx.mathExpression());
        }

        @Override
        public ExpressionNode visitStringConstantOperation(ExpressionEvaluatorV2Parser.StringConstantOperationContext ctx) {
            return new LiteralNode(
                nodeFactory.nextId("literal"),
                nodeFactory.sourceSpan(ctx),
                ctx.STRING().getText()
            );
        }

        @Override
        public ExpressionNode visitStringReferenceOperation(ExpressionEvaluatorV2Parser.StringReferenceOperationContext ctx) {
            return visit(ctx.referenceTarget());
        }

        @Override
        public ExpressionNode visitStringDecisionOperation(ExpressionEvaluatorV2Parser.StringDecisionOperationContext ctx) {
            return conditionalNode(ctx, ctx.logicalExpression(), ctx.stringEntity());
        }

        @Override
        public ExpressionNode visitStringFunctionDecisionOperation(ExpressionEvaluatorV2Parser.StringFunctionDecisionOperationContext ctx) {
            return conditionalNode(ctx, ctx.logicalExpression(), ctx.stringEntity());
        }

        @Override
        public ExpressionNode visitDateConstantOperation(ExpressionEvaluatorV2Parser.DateConstantOperationContext ctx) {
            return literal(ctx, ctx.DATE().getText());
        }

        @Override
        public ExpressionNode visitDateCurrentValueOperation(ExpressionEvaluatorV2Parser.DateCurrentValueOperationContext ctx) {
            return literal(ctx, ctx.NOW_DATE().getText());
        }

        @Override
        public ExpressionNode visitDateReferenceOperation(ExpressionEvaluatorV2Parser.DateReferenceOperationContext ctx) {
            return visit(ctx.referenceTarget());
        }

        @Override
        public ExpressionNode visitDateDecisionOperation(ExpressionEvaluatorV2Parser.DateDecisionOperationContext ctx) {
            return conditionalNode(ctx, ctx.logicalExpression(), ctx.dateEntity());
        }

        @Override
        public ExpressionNode visitDateFunctionDecisionOperation(ExpressionEvaluatorV2Parser.DateFunctionDecisionOperationContext ctx) {
            return conditionalNode(ctx, ctx.logicalExpression(), ctx.dateEntity());
        }

        @Override
        public ExpressionNode visitTimeConstantOperation(ExpressionEvaluatorV2Parser.TimeConstantOperationContext ctx) {
            return literal(ctx, ctx.TIME().getText());
        }

        @Override
        public ExpressionNode visitTimeCurrentValueOperation(ExpressionEvaluatorV2Parser.TimeCurrentValueOperationContext ctx) {
            return literal(ctx, ctx.NOW_TIME().getText());
        }

        @Override
        public ExpressionNode visitTimeReferenceOperation(ExpressionEvaluatorV2Parser.TimeReferenceOperationContext ctx) {
            return visit(ctx.referenceTarget());
        }

        @Override
        public ExpressionNode visitTimeDecisionOperation(ExpressionEvaluatorV2Parser.TimeDecisionOperationContext ctx) {
            return conditionalNode(ctx, ctx.logicalExpression(), ctx.timeEntity());
        }

        @Override
        public ExpressionNode visitTimeFunctionDecisionOperation(ExpressionEvaluatorV2Parser.TimeFunctionDecisionOperationContext ctx) {
            return conditionalNode(ctx, ctx.logicalExpression(), ctx.timeEntity());
        }

        @Override
        public ExpressionNode visitDateTimeConstantOperation(ExpressionEvaluatorV2Parser.DateTimeConstantOperationContext ctx) {
            String value = ctx.TIME_OFFSET() == null
                ? ctx.DATETIME().getText()
                : ctx.DATETIME().getText() + ctx.TIME_OFFSET().getText();
            return literal(ctx, value);
        }

        @Override
        public ExpressionNode visitDateTimeCurrentValueOperation(ExpressionEvaluatorV2Parser.DateTimeCurrentValueOperationContext ctx) {
            return literal(ctx, ctx.NOW_DATETIME().getText());
        }

        @Override
        public ExpressionNode visitDateTimeReferenceOperation(ExpressionEvaluatorV2Parser.DateTimeReferenceOperationContext ctx) {
            return visit(ctx.referenceTarget());
        }

        @Override
        public ExpressionNode visitDateTimeDecisionOperation(ExpressionEvaluatorV2Parser.DateTimeDecisionOperationContext ctx) {
            return conditionalNode(ctx, ctx.logicalExpression(), ctx.dateTimeEntity());
        }

        @Override
        public ExpressionNode visitDateTimeFunctionDecisionOperation(ExpressionEvaluatorV2Parser.DateTimeFunctionDecisionOperationContext ctx) {
            return conditionalNode(ctx, ctx.logicalExpression(), ctx.dateTimeEntity());
        }

        @Override
        public ExpressionNode visitGenericDecisionExpression(ExpressionEvaluatorV2Parser.GenericDecisionExpressionContext ctx) {
            return conditionalNode(ctx, ctx.logicalExpression(), ctx.genericEntity());
        }

        @Override
        public ExpressionNode visitGenericFunctionDecisionExpression(ExpressionEvaluatorV2Parser.GenericFunctionDecisionExpressionContext ctx) {
            return conditionalNode(ctx, ctx.logicalExpression(), ctx.genericEntity());
        }

        @Override
        public ExpressionNode visitCastExpressionOperation(ExpressionEvaluatorV2Parser.CastExpressionOperationContext ctx) {
            return visit(ctx.castExpression());
        }

        @Override
        public ExpressionNode visitTypeCastOperation(ExpressionEvaluatorV2Parser.TypeCastOperationContext ctx) {
            return visit(ctx.genericEntity());
        }

        @Override
        public ExpressionNode visitReferenceTargetOperation(ExpressionEvaluatorV2Parser.ReferenceTargetOperationContext ctx) {
            return visit(ctx.referenceTarget());
        }

        @Override
        public ExpressionNode visitVectorOfEntitiesOperation(ExpressionEvaluatorV2Parser.VectorOfEntitiesOperationContext ctx) {
            return new VectorLiteralNode(
                nodeFactory.nextId("vector"),
                nodeFactory.sourceSpan(ctx),
                ctx.allEntityTypes().stream()
                    .map(this::visitAllEntityType)
                    .toList()
            );
        }

        @Override
        public ExpressionNode visitVectorReferenceOperation(ExpressionEvaluatorV2Parser.VectorReferenceOperationContext ctx) {
            return visit(ctx.referenceTarget());
        }

        @Override
        public ExpressionNode visitVectorDecisionOperation(ExpressionEvaluatorV2Parser.VectorDecisionOperationContext ctx) {
            return conditionalNode(ctx, ctx.logicalExpression(), ctx.vectorEntity());
        }

        @Override
        public ExpressionNode visitVectorFunctionDecisionOperation(ExpressionEvaluatorV2Parser.VectorFunctionDecisionOperationContext ctx) {
            return conditionalNode(ctx, ctx.logicalExpression(), ctx.vectorEntity());
        }

        private ExpressionNode comparisonNode(
            ParserRuleContext leftContext,
            ExpressionEvaluatorV2Parser.ComparisonOperatorContext operatorContext,
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
            List<ExpressionEvaluatorV2Parser.LogicalExpressionContext> logicalExpressions
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

        private ExpressionNode visitAllEntityType(ExpressionEvaluatorV2Parser.AllEntityTypesContext ctx) {
            if (ctx instanceof ExpressionEvaluatorV2Parser.MathEntityTypeContext mathEntityType) {
                return visit(mathEntityType.mathExpression());
            }
            if (ctx instanceof ExpressionEvaluatorV2Parser.LogicalEntityTypeContext logicalEntityType) {
                return visit(logicalEntityType.logicalExpression());
            }
            if (ctx instanceof ExpressionEvaluatorV2Parser.StringEntityTypeContext stringEntityType) {
                return visit(stringEntityType.stringEntity());
            }
            if (ctx instanceof ExpressionEvaluatorV2Parser.DateEntityTypeContext dateEntityType) {
                return visit(dateEntityType.dateEntity());
            }
            if (ctx instanceof ExpressionEvaluatorV2Parser.TimeEntityTypeContext timeEntityType) {
                return visit(timeEntityType.timeEntity());
            }
            if (ctx instanceof ExpressionEvaluatorV2Parser.DateTimeEntityTypeContext dateTimeEntityType) {
                return visit(dateTimeEntityType.dateTimeEntity());
            }
            if (ctx instanceof ExpressionEvaluatorV2Parser.VectorEntityTypeContext vectorEntityType) {
                return visit(vectorEntityType.vectorEntity());
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

        private BinaryOperator comparisonOperator(ExpressionEvaluatorV2Parser.ComparisonOperatorContext ctx) {
            return switch (ctx) {
                case ExpressionEvaluatorV2Parser.GreaterThanOperatorContext ignored -> BinaryOperator.GREATER_THAN;
                case ExpressionEvaluatorV2Parser.GreaterThanOrEqualOperatorContext ignored -> BinaryOperator.GREATER_THAN_OR_EQUAL;
                case ExpressionEvaluatorV2Parser.LessThanOperatorContext ignored -> BinaryOperator.LESS_THAN;
                case ExpressionEvaluatorV2Parser.LessThanOrEqualOperatorContext ignored -> BinaryOperator.LESS_THAN_OR_EQUAL;
                case ExpressionEvaluatorV2Parser.EqualOperatorContext ignored -> BinaryOperator.EQUAL;
                case ExpressionEvaluatorV2Parser.NotEqualOperatorContext ignored -> BinaryOperator.NOT_EQUAL;
                default -> throw new IllegalStateException("unsupported comparison operator: " + ctx.getClass().getSimpleName());
            };
        }

        private BinaryOperator tokenTypeToAdditiveOperator(Object payload) {
            Token token = (Token) payload;
            return token.getType() == ExpressionEvaluatorV2Parser.PLUS ? BinaryOperator.ADD : BinaryOperator.SUBTRACT;
        }

        private BinaryOperator tokenTypeToMultiplicativeOperator(Object payload) {
            Token token = (Token) payload;
            return switch (token.getType()) {
                case ExpressionEvaluatorV2Parser.MULT -> BinaryOperator.MULTIPLY;
                case ExpressionEvaluatorV2Parser.DIV -> BinaryOperator.DIVIDE;
                case ExpressionEvaluatorV2Parser.MODULO -> BinaryOperator.MODULO;
                default -> throw new IllegalStateException("unsupported multiplicative operator: " + token.getText());
            };
        }

        private BinaryOperator tokenTypeToBitwiseOperator(Object payload) {
            Token token = (Token) payload;
            return switch (token.getType()) {
                case ExpressionEvaluatorV2Parser.NAND -> BinaryOperator.NAND;
                case ExpressionEvaluatorV2Parser.NOR -> BinaryOperator.NOR;
                case ExpressionEvaluatorV2Parser.XOR -> BinaryOperator.XOR;
                case ExpressionEvaluatorV2Parser.XNOR -> BinaryOperator.XNOR;
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
