package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.internal.diagnostics.DiagnosticCategory;
import com.runestone.expeval_mk3.internal.diagnostics.DiagnosticCode;
import com.runestone.expeval_mk3.internal.diagnostics.ExpressionDiagnostic;
import com.runestone.expeval_mk3.internal.grammar.ExpressionEvaluatorBaseVisitor;
import com.runestone.expeval_mk3.internal.grammar.ExpressionEvaluatorParser;
import com.runestone.expeval_mk3.internal.parser.ParseSuccess;
import com.runestone.expeval_mk3.internal.source.SourceSpan;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

final class SemanticAstBuilder extends ExpressionEvaluatorBaseVisitor<Optional<ExpressionNode>> {

    private final List<ExpressionDiagnostic> diagnostics = new ArrayList<>();

    SemanticAstResult build(ParseSuccess parseSuccess) {
        Objects.requireNonNull(parseSuccess, "parseSuccess");
        diagnostics.clear();
        if (!(parseSuccess.tree() instanceof ExpressionEvaluatorParser.StartInputContext start)) {
            throw new IllegalArgumentException(
                    "Unsupported parser start context: " + parseSuccess.tree().getClass().getName());
        }

        List<AssignmentNode> assignments = new ArrayList<>(start.assignmentExpression().size());
        for (ExpressionEvaluatorParser.AssignmentExpressionContext assignmentContext : start.assignmentExpression()) {
            buildAssignment(assignmentContext).ifPresent(assignments::add);
        }
        Optional<ExpressionNode> resultExpression = start.expression() == null ? Optional.empty() : visit(start.expression());
        if (!diagnostics.isEmpty()) {
            return new SemanticAstFailure(diagnostics);
        }
        List<AstNode> topLevelNodes = new ArrayList<>(assignments.size() + resultExpression.map(expression -> 1).orElse(0));
        topLevelNodes.addAll(assignments);
        resultExpression.ifPresent(topLevelNodes::add);
        ExpressionFileNode unassigned = new ExpressionFileNode(
                NodeId.UNASSIGNED,
                fileSpan(topLevelNodes, start),
                assignments,
                resultExpression);
        return new SemanticAstSuccess(new AstNodeIdAssigner().assign(unassigned));
    }

    private Optional<AssignmentNode> buildAssignment(ExpressionEvaluatorParser.AssignmentExpressionContext context) {
        if (context instanceof ExpressionEvaluatorParser.AssignmentOperationContext assignment) {
            TerminalNode identifier = assignment.IDENTIFIER();
            Optional<ExpressionNode> expression = visit(assignment.expression());
            if (expression.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(new AssignmentNode(
                    NodeId.UNASSIGNED,
                    span(assignment),
                    new IdentifierAssignmentTargetNode(
                            NodeId.UNASSIGNED,
                            span(identifier.getSymbol()),
                            identifier.getText()),
                    expression.orElseThrow()));
        }
        throw unsupported(context, "destructuring assignment");
    }

    @Override
    public Optional<ExpressionNode> visitComparisonOperation(ExpressionEvaluatorParser.ComparisonOperationContext context) {
        Optional<ExpressionNode> left = visit(context.bitwiseLogicalExpression(0));
        Optional<ExpressionNode> right = visit(context.bitwiseLogicalExpression(1));
        if (left.isEmpty() || right.isEmpty()) {
            return Optional.empty();
        }
        ExpressionNode leftNode = left.orElseThrow();
        ExpressionNode rightNode = right.orElseThrow();
        return Optional.of(new BinaryOperationNode(
                NodeId.UNASSIGNED,
                span(leftNode, rightNode),
                leftNode,
                comparisonOperator(context.comparisonOperator()),
                span(context.comparisonOperator()),
                rightNode));
    }

    @Override
    public Optional<ExpressionNode> visitInOperation(ExpressionEvaluatorParser.InOperationContext context) {
        return buildMembership(
                context,
                context.bitwiseLogicalExpression(0),
                context.bitwiseLogicalExpression(1),
                context.NOT_KW() != null,
                context.NOT_KW() == null ? span(context.IN().getSymbol()) : span(context.NOT_KW().getSymbol(), context.IN().getSymbol()));
    }

    @Override
    public Optional<ExpressionNode> visitNinOperation(ExpressionEvaluatorParser.NinOperationContext context) {
        return buildMembership(
                context,
                context.bitwiseLogicalExpression(0),
                context.bitwiseLogicalExpression(1),
                true,
                span(context.NIN().getSymbol()));
    }

    @Override
    public Optional<ExpressionNode> visitBetweenOperation(ExpressionEvaluatorParser.BetweenOperationContext context) {
        Optional<ExpressionNode> value = visit(context.bitwiseLogicalExpression(0));
        Optional<ExpressionNode> lowerBound = visit(context.bitwiseLogicalExpression(1));
        Optional<ExpressionNode> upperBound = visit(context.bitwiseLogicalExpression(2));
        if (value.isEmpty() || lowerBound.isEmpty() || upperBound.isEmpty()) {
            return Optional.empty();
        }
        SourceSpan operatorSpan = context.NOT_KW() == null
                ? span(context.BETWEEN().getSymbol())
                : span(context.NOT_KW().getSymbol(), context.BETWEEN().getSymbol());
        return Optional.of(new BetweenNode(
                NodeId.UNASSIGNED,
                span(context),
                value.orElseThrow(),
                operatorSpan,
                context.NOT_KW() != null,
                lowerBound.orElseThrow(),
                upperBound.orElseThrow()));
    }

    @Override
    public Optional<ExpressionNode> visitRegexMatchOperation(ExpressionEvaluatorParser.RegexMatchOperationContext context) {
        return buildRegexOperation(
                context,
                context.bitwiseLogicalExpression(),
                context.STRING(),
                BinaryOperator.REGEX_MATCH,
                context.REGEX_MATCH().getSymbol());
    }

    @Override
    public Optional<ExpressionNode> visitRegexNotMatchOperation(ExpressionEvaluatorParser.RegexNotMatchOperationContext context) {
        return buildRegexOperation(
                context,
                context.bitwiseLogicalExpression(),
                context.STRING(),
                BinaryOperator.REGEX_NOT_MATCH,
                context.REGEX_NOT_MATCH().getSymbol());
    }

    @Override
    public Optional<ExpressionNode> visitExpressionOperation(ExpressionEvaluatorParser.ExpressionOperationContext context) {
        return visit(context.coalesceExpression());
    }

    @Override
    public Optional<ExpressionNode> visitCoalesceOperation(ExpressionEvaluatorParser.CoalesceOperationContext context) {
        if (context.orExpression().size() == 1) {
            return visit(context.orExpression(0));
        }
        return buildNullCoalescence(context);
    }

    @Override
    public Optional<ExpressionNode> visitLogicalOrOperation(ExpressionEvaluatorParser.LogicalOrOperationContext context) {
        if (context.andExpression().size() == 1) {
            return visit(context.andExpression(0));
        }
        return buildLeftAssociative(context.andExpression(), context, ExpressionEvaluatorParser.OR);
    }

    @Override
    public Optional<ExpressionNode> visitLogicalAndOperation(ExpressionEvaluatorParser.LogicalAndOperationContext context) {
        if (context.comparisonExpression().size() == 1) {
            return visit(context.comparisonExpression(0));
        }
        return buildLeftAssociative(context.comparisonExpression(), context, ExpressionEvaluatorParser.AND);
    }

    @Override
    public Optional<ExpressionNode> visitBitwisePassthroughOperation(ExpressionEvaluatorParser.BitwisePassthroughOperationContext context) {
        return visit(context.bitwiseLogicalExpression());
    }

    @Override
    public Optional<ExpressionNode> visitLogicalBitwiseOperation(ExpressionEvaluatorParser.LogicalBitwiseOperationContext context) {
        if (context.concatExpression().size() == 1) {
            return visit(context.concatExpression(0));
        }
        return buildLeftAssociative(
                context.concatExpression(),
                context,
                ExpressionEvaluatorParser.NAND,
                ExpressionEvaluatorParser.NOR,
                ExpressionEvaluatorParser.XOR,
                ExpressionEvaluatorParser.XNOR);
    }

    @Override
    public Optional<ExpressionNode> visitStringConcatenationOperation(ExpressionEvaluatorParser.StringConcatenationOperationContext context) {
        if (context.additiveExpression().size() == 1) {
            return visit(context.additiveExpression(0));
        }
        return buildLeftAssociative(context.additiveExpression(), context, ExpressionEvaluatorParser.CONCAT);
    }

    @Override
    public Optional<ExpressionNode> visitAdditiveOperation(ExpressionEvaluatorParser.AdditiveOperationContext context) {
        if (context.multiplicativeExpression().size() == 1) {
            return visit(context.multiplicativeExpression(0));
        }
        return buildLeftAssociative(
                context.multiplicativeExpression(),
                context,
                ExpressionEvaluatorParser.PLUS,
                ExpressionEvaluatorParser.MINUS);
    }

    @Override
    public Optional<ExpressionNode> visitMultiplicativeOperation(ExpressionEvaluatorParser.MultiplicativeOperationContext context) {
        if (context.unaryExpression().size() == 1) {
            return visit(context.unaryExpression(0));
        }
        return buildLeftAssociative(
                context.unaryExpression(),
                context,
                ExpressionEvaluatorParser.MULT,
                ExpressionEvaluatorParser.DIV,
                ExpressionEvaluatorParser.MODULO);
    }

    @Override
    public Optional<ExpressionNode> visitUnaryMinusOperation(ExpressionEvaluatorParser.UnaryMinusOperationContext context) {
        Optional<ExpressionNode> operand = visit(context.unaryExpression());
        return operand.map(expression -> new UnaryOperationNode(
                NodeId.UNASSIGNED,
                span(context),
                UnaryOperator.NEGATE,
                span(context.MINUS().getSymbol()),
                expression));
    }

    @Override
    public Optional<ExpressionNode> visitLogicalNotOperation(ExpressionEvaluatorParser.LogicalNotOperationContext context) {
        Optional<ExpressionNode> operand = visit(context.unaryExpression());
        return operand.map(expression -> new UnaryOperationNode(
                NodeId.UNASSIGNED,
                span(context),
                UnaryOperator.LOGICAL_NOT,
                span(firstTerminal(context).getSymbol()),
                expression));
    }

    @Override
    public Optional<ExpressionNode> visitRootPassthroughOperation(ExpressionEvaluatorParser.RootPassthroughOperationContext context) {
        return visit(context.rootExpression());
    }

    @Override
    public Optional<ExpressionNode> visitRootChainOperation(ExpressionEvaluatorParser.RootChainOperationContext context) {
        if (context.exponentiationExpression().size() == 1) {
            return visit(context.exponentiationExpression(0));
        }
        return buildLeftAssociative(context.exponentiationExpression(), context, ExpressionEvaluatorParser.ROOT);
    }

    @Override
    public Optional<ExpressionNode> visitExponentiationOperation(ExpressionEvaluatorParser.ExponentiationOperationContext context) {
        if (context.unaryExpression() == null) {
            return visit(context.postfixExpression());
        }
        Optional<ExpressionNode> left = visit(context.postfixExpression());
        Optional<ExpressionNode> right = visit(context.unaryExpression());
        if (left.isEmpty() || right.isEmpty()) {
            return Optional.empty();
        }
        ExpressionNode leftNode = left.orElseThrow();
        ExpressionNode rightNode = right.orElseThrow();
        return Optional.of(new BinaryOperationNode(
                NodeId.UNASSIGNED,
                span(leftNode, rightNode),
                leftNode,
                BinaryOperator.EXPONENTIATE,
                span(context.EXPONENTIATION().getSymbol()),
                rightNode));
    }

    @Override
    public Optional<ExpressionNode> visitPostfixOperation(ExpressionEvaluatorParser.PostfixOperationContext context) {
        if (context.PERCENT().isEmpty() && context.EXCLAMATION().isEmpty()) {
            return visit(context.primaryExpression());
        }
        Optional<ExpressionNode> operand = visit(context.primaryExpression());
        if (operand.isEmpty()) {
            return Optional.empty();
        }
        List<PostfixOperatorOccurrence> operators = directOperatorTokens(
                context,
                ExpressionEvaluatorParser.PERCENT,
                ExpressionEvaluatorParser.EXCLAMATION).stream()
                .map(token -> new PostfixOperatorOccurrence(postfixOperator(token), span(token)))
                .toList();
        return Optional.of(new PostfixOperationNode(NodeId.UNASSIGNED, span(context), operand.orElseThrow(), operators));
    }

    @Override
    public Optional<ExpressionNode> visitParenthesisOperation(ExpressionEvaluatorParser.ParenthesisOperationContext context) {
        Optional<ExpressionNode> expression = visit(context.expression());
        return expression.map(node -> new GroupedExpressionNode(NodeId.UNASSIGNED, span(context), node));
    }

    @Override
    public Optional<ExpressionNode> visitDecisionOperation(ExpressionEvaluatorParser.DecisionOperationContext context) {
        return visit(context.ifExpression());
    }

    @Override
    public Optional<ExpressionNode> visitVectorLiteralOperation(ExpressionEvaluatorParser.VectorLiteralOperationContext context) {
        return visit(context.vectorLiteral());
    }

    @Override
    public Optional<ExpressionNode> visitIfThenElseOperation(ExpressionEvaluatorParser.IfThenElseOperationContext context) {
        List<ExpressionEvaluatorParser.ExpressionContext> expressionContexts = context.expression();
        int branchCount = (expressionContexts.size() - 1) / 2;
        List<ConditionalBranchNode> branches = new ArrayList<>(branchCount);
        for (int index = 0; index < branchCount; index++) {
            Token branchStart = index == 0 ? context.IF().getSymbol() : context.ELSEIF(index - 1).getSymbol();
            Optional<ConditionalBranchNode> branch = buildConditionalBranch(
                    branchStart,
                    expressionContexts.get(index * 2),
                    expressionContexts.get(index * 2 + 1));
            if (branch.isEmpty()) {
                return Optional.empty();
            }
            branches.add(branch.orElseThrow());
        }
        Optional<ExpressionNode> elseExpression = visit(expressionContexts.getLast());
        return elseExpression.map(expression -> new ConditionalNode(
                NodeId.UNASSIGNED,
                span(context),
                ConditionalSourceForm.CLASSIC,
                branches,
                expression));
    }

    @Override
    public Optional<ExpressionNode> visitFunctionalIfOperation(ExpressionEvaluatorParser.FunctionalIfOperationContext context) {
        List<ExpressionEvaluatorParser.ExpressionContext> expressionContexts = context.expression();
        int branchCount = (expressionContexts.size() - 1) / 2;
        List<ConditionalBranchNode> branches = new ArrayList<>(branchCount);
        for (int index = 0; index < branchCount; index++) {
            Optional<ConditionalBranchNode> branch = buildConditionalBranch(
                    null,
                    expressionContexts.get(index * 2),
                    expressionContexts.get(index * 2 + 1));
            if (branch.isEmpty()) {
                return Optional.empty();
            }
            branches.add(branch.orElseThrow());
        }
        Optional<ExpressionNode> elseExpression = visit(expressionContexts.getLast());
        return elseExpression.map(expression -> new ConditionalNode(
                NodeId.UNASSIGNED,
                span(context),
                ConditionalSourceForm.FUNCTIONAL,
                branches,
                expression));
    }

    @Override
    public Optional<ExpressionNode> visitVectorOfEntitiesOperation(
            ExpressionEvaluatorParser.VectorOfEntitiesOperationContext context) {
        List<ExpressionNode> elements = new ArrayList<>(context.expression().size());
        for (ExpressionEvaluatorParser.ExpressionContext expressionContext : context.expression()) {
            Optional<ExpressionNode> expression = visit(expressionContext);
            if (expression.isEmpty()) {
                return Optional.empty();
            }
            elements.add(expression.orElseThrow());
        }
        return Optional.of(new VectorLiteralNode(NodeId.UNASSIGNED, span(context), elements));
    }

    @Override
    public Optional<ExpressionNode> visitLiteralOperation(ExpressionEvaluatorParser.LiteralOperationContext context) {
        return visit(context.literal());
    }

    @Override
    public Optional<ExpressionNode> visitReferenceTargetOperation(ExpressionEvaluatorParser.ReferenceTargetOperationContext context) {
        return visit(context.referenceTarget());
    }

    @Override
    public Optional<ExpressionNode> visitIdentifierReferenceTarget(ExpressionEvaluatorParser.IdentifierReferenceTargetContext context) {
        ExpressionNode receiver = new IdentifierNode(NodeId.UNASSIGNED, span(context.IDENTIFIER().getSymbol()), context.IDENTIFIER().getText());
        return buildNavigationChain(receiver, context.memberChain());
    }

    @Override
    public Optional<ExpressionNode> visitFunctionReferenceTarget(ExpressionEvaluatorParser.FunctionReferenceTargetContext context) {
        Optional<FunctionCallNode> receiver = buildFunctionCall(context.function());
        if (receiver.isEmpty()) {
            return Optional.empty();
        }
        return buildNavigationChain(receiver.orElseThrow(), context.memberChain());
    }

    @Override
    public Optional<ExpressionNode> visitAtReferenceTarget(ExpressionEvaluatorParser.AtReferenceTargetContext context) {
        ExpressionNode receiver = new CurrentItemNode(NodeId.UNASSIGNED, span(context.AT().getSymbol()));
        return buildNavigationChain(receiver, context.memberChain());
    }

    @Override
    public Optional<ExpressionNode> visitIntConstantOperation(ExpressionEvaluatorParser.IntConstantOperationContext context) {
        return Optional.of(new LiteralNode(
                NodeId.UNASSIGNED,
                span(context),
                parseInteger(context.INT().getText())));
    }

    @Override
    public Optional<ExpressionNode> visitFloatConstantOperation(ExpressionEvaluatorParser.FloatConstantOperationContext context) {
        return Optional.of(new LiteralNode(
                NodeId.UNASSIGNED,
                span(context),
                new DecimalLiteralValue(new BigDecimal(context.FLOAT().getText()))));
    }

    @Override
    public Optional<ExpressionNode> visitStringConstantOperation(ExpressionEvaluatorParser.StringConstantOperationContext context) {
        return Optional.of(new LiteralNode(
                NodeId.UNASSIGNED,
                span(context),
                new StringLiteralValue(unquote(context.STRING().getText()))));
    }

    @Override
    public Optional<ExpressionNode> visitLogicalConstantOperation(
            ExpressionEvaluatorParser.LogicalConstantOperationContext context) {
        return Optional.of(new LiteralNode(NodeId.UNASSIGNED, span(context), new BooleanLiteralValue(context.TRUE() != null)));
    }

    @Override
    public Optional<ExpressionNode> visitNullConstantOperation(ExpressionEvaluatorParser.NullConstantOperationContext context) {
        return Optional.of(new LiteralNode(NodeId.UNASSIGNED, span(context), new NullLiteralValue()));
    }

    @Override
    public Optional<ExpressionNode> visitDateConstantOperation(ExpressionEvaluatorParser.DateConstantOperationContext context) {
        String value = unquoteTemporal(context.DATE().getText(), 2);
        try {
            return Optional.of(new LiteralNode(NodeId.UNASSIGNED, span(context), new DateLiteralValue(LocalDate.parse(value))));
        } catch (DateTimeParseException ignored) {
            addInvalidLiteralDiagnostic(DiagnosticCode.AST_INVALID_DATE_LITERAL, context, value);
            return Optional.empty();
        }
    }

    @Override
    public Optional<ExpressionNode> visitTimeConstantOperation(ExpressionEvaluatorParser.TimeConstantOperationContext context) {
        String value = unquoteTemporal(context.TIME().getText(), 2);
        try {
            return Optional.of(new LiteralNode(NodeId.UNASSIGNED, span(context), new TimeLiteralValue(LocalTime.parse(value))));
        } catch (DateTimeParseException ignored) {
            addInvalidLiteralDiagnostic(DiagnosticCode.AST_INVALID_TIME_LITERAL, context, value);
            return Optional.empty();
        }
    }

    @Override
    public Optional<ExpressionNode> visitDateTimeConstantOperation(
            ExpressionEvaluatorParser.DateTimeConstantOperationContext context) {
        String value = unquoteTemporal(context.DATETIME().getText(), 3);
        try {
            LiteralValue literalValue = hasOffset(value)
                    ? new OffsetDateTimeLiteralValue(OffsetDateTime.parse(value))
                    : new LocalDateTimeLiteralValue(LocalDateTime.parse(value));
            return Optional.of(new LiteralNode(NodeId.UNASSIGNED, span(context), literalValue));
        } catch (DateTimeParseException ignored) {
            addInvalidLiteralDiagnostic(DiagnosticCode.AST_INVALID_DATETIME_LITERAL, context, value);
            return Optional.empty();
        }
    }

    @Override
    public Optional<ExpressionNode> visitDateCurrentValueOperation(
            ExpressionEvaluatorParser.DateCurrentValueOperationContext context) {
        return Optional.of(new CurrentTemporalValueNode(NodeId.UNASSIGNED, span(context), CurrentTemporalValueKind.DATE));
    }

    @Override
    public Optional<ExpressionNode> visitTimeCurrentValueOperation(
            ExpressionEvaluatorParser.TimeCurrentValueOperationContext context) {
        return Optional.of(new CurrentTemporalValueNode(NodeId.UNASSIGNED, span(context), CurrentTemporalValueKind.TIME));
    }

    @Override
    public Optional<ExpressionNode> visitDateTimeCurrentValueOperation(
            ExpressionEvaluatorParser.DateTimeCurrentValueOperationContext context) {
        return Optional.of(new CurrentTemporalValueNode(NodeId.UNASSIGNED, span(context), CurrentTemporalValueKind.DATETIME));
    }

    private Optional<ExpressionNode> buildMembership(
            ParserRuleContext context,
            ParserRuleContext valueContext,
            ParserRuleContext candidatesContext,
            boolean negated,
            SourceSpan operatorSpan) {
        Optional<ExpressionNode> value = visit(valueContext);
        Optional<ExpressionNode> candidates = visit(candidatesContext);
        if (value.isEmpty() || candidates.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new MembershipNode(
                NodeId.UNASSIGNED,
                span(context),
                value.orElseThrow(),
                operatorSpan,
                negated,
                candidates.orElseThrow()));
    }

    private Optional<ExpressionNode> buildNavigationChain(
            ExpressionNode receiver,
            List<ExpressionEvaluatorParser.MemberChainContext> memberChainContexts) {
        if (memberChainContexts.isEmpty()) {
            return Optional.of(receiver);
        }
        List<NavigationLink> links = new ArrayList<>(memberChainContexts.size());
        for (ExpressionEvaluatorParser.MemberChainContext memberChainContext : memberChainContexts) {
            Optional<NavigationLink> link = buildNavigationLink(memberChainContext);
            if (link.isEmpty()) {
                return Optional.empty();
            }
            links.add(link.orElseThrow());
        }
        return Optional.of(new NavigationChainNode(NodeId.UNASSIGNED, span(receiver, links.getLast()), receiver, links));
    }

    private Optional<NavigationLink> buildNavigationLink(ExpressionEvaluatorParser.MemberChainContext context) {
        return switch (context) {
            case ExpressionEvaluatorParser.PropertyAccessContext property -> Optional.of(new PropertyNavigationLink(
                    NodeId.UNASSIGNED,
                    span(property),
                    memberName(property.memberName()),
                    false));
            case ExpressionEvaluatorParser.SafePropertyAccessContext property -> Optional.of(new PropertyNavigationLink(
                    NodeId.UNASSIGNED,
                    span(property),
                    memberName(property.memberName()),
                    true));
            case ExpressionEvaluatorParser.MethodCallAccessContext method -> buildMethodNavigationLink(
                    method,
                    method.memberName(),
                    method.expression(),
                    false);
            case ExpressionEvaluatorParser.SafeMethodCallAccessContext method -> buildMethodNavigationLink(
                    method,
                    method.memberName(),
                    method.expression(),
                    true);
            case ExpressionEvaluatorParser.ChildWildcardAccessContext wildcard -> Optional.of(new WildcardNavigationLink(
                    NodeId.UNASSIGNED,
                    span(wildcard)));
            case ExpressionEvaluatorParser.SubscriptAccessContext subscript -> buildSubscriptNavigationLink(
                    subscript,
                    subscript.subscript(),
                    false);
            case ExpressionEvaluatorParser.SafeSubscriptAccessContext subscript -> buildSubscriptNavigationLink(
                    subscript,
                    subscript.subscript(),
                    true);
            case ExpressionEvaluatorParser.CollectionFunctionAccessContext collection -> buildCollectionOperationNavigationLink(collection);
            default -> throw unsupported(context, "member chain");
        };
    }

    private Optional<NavigationLink> buildSubscriptNavigationLink(
            ParserRuleContext context,
            ExpressionEvaluatorParser.SubscriptContext subscript,
            boolean safeNavigation) {
        if (subscript instanceof ExpressionEvaluatorParser.FilterSubscriptContext filter) {
            return buildFilterNavigationLink(context, filter, safeNavigation);
        }
        return Optional.of(new SubscriptNavigationLink(
                NodeId.UNASSIGNED,
                span(context),
                buildSubscript(subscript),
                safeNavigation));
    }

    private Optional<NavigationLink> buildFilterNavigationLink(
            ParserRuleContext context,
            ExpressionEvaluatorParser.FilterSubscriptContext filter,
            boolean safeNavigation) {
        Optional<ExpressionNode> predicate = visit(filter.expression());
        return predicate.map(expression -> new FilterNavigationLink(
                NodeId.UNASSIGNED,
                span(context),
                expression,
                safeNavigation));
    }

    private Optional<NavigationLink> buildCollectionOperationNavigationLink(
            ExpressionEvaluatorParser.CollectionFunctionAccessContext context) {
        Optional<List<CollectionOperationArgument>> arguments = buildCollectionOperationArguments(
                context.collectionFunctionArguments());
        return arguments.map(nodes -> new CollectionOperationNavigationLink(
                NodeId.UNASSIGNED,
                span(context),
                memberName(context.memberName()),
                nodes));
    }

    private Optional<NavigationLink> buildMethodNavigationLink(
            ParserRuleContext context,
            ExpressionEvaluatorParser.MemberNameContext memberName,
            List<ExpressionEvaluatorParser.ExpressionContext> argumentContexts,
            boolean safeNavigation) {
        Optional<List<ExpressionNode>> arguments = buildArguments(argumentContexts);
        return arguments.map(nodes -> new MethodNavigationLink(
                NodeId.UNASSIGNED,
                span(context),
                memberName(memberName),
                safeNavigation,
                nodes));
    }

    private Optional<FunctionCallNode> buildFunctionCall(ExpressionEvaluatorParser.FunctionContext context) {
        if (!(context instanceof ExpressionEvaluatorParser.FunctionCallOperationContext functionCall)) {
            throw unsupported(context, "function call");
        }
        Optional<List<ExpressionNode>> arguments = buildArguments(functionCall.expression());
        return arguments.map(nodes -> new FunctionCallNode(
                NodeId.UNASSIGNED,
                span(functionCall),
                new MemberName(functionCall.IDENTIFIER().getText(), span(functionCall.IDENTIFIER().getSymbol())),
                nodes));
    }

    private Optional<List<CollectionOperationArgument>> buildCollectionOperationArguments(
            ExpressionEvaluatorParser.CollectionFunctionArgumentsContext context) {
        if (context == null) {
            return Optional.of(List.of());
        }
        return switch (context) {
            case ExpressionEvaluatorParser.LambdaCollectionFunctionArgumentsContext lambda -> buildLambdaCollectionArgument(lambda)
                    .map(List::of);
            case ExpressionEvaluatorParser.PositionalCollectionFunctionArgumentsContext positional ->
                    buildPositionalCollectionArguments(positional);
            default -> throw unsupported(context, "collection function arguments");
        };
    }

    private Optional<CollectionOperationArgument> buildLambdaCollectionArgument(
            ExpressionEvaluatorParser.LambdaCollectionFunctionArgumentsContext context) {
        Optional<ExpressionNode> body = visit(context.expression());
        if (body.isEmpty()) {
            return Optional.empty();
        }
        ExpressionNode bodyNode = body.orElseThrow();
        LambdaNode lambda = new LambdaNode(
                NodeId.UNASSIGNED,
                span(context.AT().getSymbol(), bodyNode),
                span(context.AT().getSymbol()),
                span(context.ARROW().getSymbol()),
                bodyNode);
        return Optional.of(new LambdaCollectionOperationArgument(
                NodeId.UNASSIGNED,
                span(context),
                lambda));
    }

    private Optional<List<CollectionOperationArgument>> buildPositionalCollectionArguments(
            ExpressionEvaluatorParser.PositionalCollectionFunctionArgumentsContext context) {
        List<CollectionOperationArgument> arguments = new ArrayList<>(context.expression().size());
        for (ExpressionEvaluatorParser.ExpressionContext argumentContext : context.expression()) {
            Optional<ExpressionNode> argument = visit(argumentContext);
            if (argument.isEmpty()) {
                return Optional.empty();
            }
            ExpressionNode expression = argument.orElseThrow();
            arguments.add(new PositionalCollectionOperationArgument(
                    NodeId.UNASSIGNED,
                    expression.sourceSpan(),
                    expression));
        }
        return Optional.of(arguments);
    }

    private Optional<List<ExpressionNode>> buildArguments(List<ExpressionEvaluatorParser.ExpressionContext> argumentContexts) {
        List<ExpressionNode> arguments = new ArrayList<>(argumentContexts.size());
        for (ExpressionEvaluatorParser.ExpressionContext argumentContext : argumentContexts) {
            Optional<ExpressionNode> argument = visit(argumentContext);
            if (argument.isEmpty()) {
                return Optional.empty();
            }
            arguments.add(argument.orElseThrow());
        }
        return Optional.of(arguments);
    }

    private Subscript buildSubscript(ExpressionEvaluatorParser.SubscriptContext context) {
        return switch (context) {
            case ExpressionEvaluatorParser.WildcardSubscriptContext ignored -> new WildcardSubscript();
            case ExpressionEvaluatorParser.StringKeySubscriptContext stringKey ->
                    new StringKeySubscript(unquote(stringKey.STRING().getText()));
            case ExpressionEvaluatorParser.IndexSubscriptContext index ->
                    new IndexSubscript(signedInteger(index.signedInteger()));
            case ExpressionEvaluatorParser.SliceWithStartSubscriptContext slice -> new SliceSubscript(
                    Optional.of(signedInteger(slice.signedInteger(0))),
                    slice.signedInteger().size() == 1
                            ? Optional.empty()
                            : Optional.of(signedInteger(slice.signedInteger(1))));
            case ExpressionEvaluatorParser.SliceToEndSubscriptContext slice -> new SliceSubscript(
                    Optional.empty(),
                    Optional.of(signedInteger(slice.signedInteger())));
            default -> throw unsupported(context, "subscript");
        };
    }

    private static MemberName memberName(ExpressionEvaluatorParser.MemberNameContext context) {
        return new MemberName(context.getText(), span(context.getStart()));
    }

    private static SignedIntegerLiteral signedInteger(ExpressionEvaluatorParser.SignedIntegerContext context) {
        if (!(context instanceof ExpressionEvaluatorParser.SignedIntegerOperationContext signedInteger)) {
            throw new IllegalArgumentException("Unsupported signed integer context: " + context.getClass().getName());
        }
        String unsignedText = signedInteger.INT().getText();
        IntegerLiteralFormat format = integerFormat(unsignedText);
        BigInteger value = switch (format) {
            case DECIMAL -> new BigInteger(unsignedText);
            case HEXADECIMAL -> new BigInteger(unsignedText.substring(2), 16);
            case OCTAL -> new BigInteger(unsignedText.substring(1), 8);
        };
        if (signedInteger.MINUS() != null) {
            value = value.negate();
        }
        return new SignedIntegerLiteral(value, format);
    }

    private static IntegerLiteralFormat integerFormat(String text) {
        if (text.startsWith("0x") || text.startsWith("0X")) {
            return IntegerLiteralFormat.HEXADECIMAL;
        }
        if (isOctalLiteral(text)) {
            return IntegerLiteralFormat.OCTAL;
        }
        return IntegerLiteralFormat.DECIMAL;
    }

    private Optional<ConditionalBranchNode> buildConditionalBranch(
            Token branchStart,
            ExpressionEvaluatorParser.ExpressionContext conditionContext,
            ExpressionEvaluatorParser.ExpressionContext resultContext) {
        Optional<ExpressionNode> condition = visit(conditionContext);
        Optional<ExpressionNode> resultExpression = visit(resultContext);
        if (condition.isEmpty() || resultExpression.isEmpty()) {
            return Optional.empty();
        }
        ExpressionNode conditionNode = condition.orElseThrow();
        ExpressionNode resultNode = resultExpression.orElseThrow();
        SourceSpan branchSpan = branchStart == null ? span(conditionNode, resultNode) : span(branchStart, resultNode);
        return Optional.of(new ConditionalBranchNode(
                NodeId.UNASSIGNED,
                branchSpan,
                conditionNode,
                resultNode));
    }

    private Optional<ExpressionNode> buildRegexOperation(
            ParserRuleContext context,
            ParserRuleContext leftContext,
            TerminalNode string,
            BinaryOperator operator,
            Token operatorToken) {
        Optional<ExpressionNode> left = visit(leftContext);
        if (left.isEmpty()) {
            return Optional.empty();
        }
        ExpressionNode leftNode = left.orElseThrow();
        ExpressionNode rightNode = new LiteralNode(
                NodeId.UNASSIGNED,
                span(string.getSymbol()),
                new StringLiteralValue(unquote(string.getText())));
        return Optional.of(new BinaryOperationNode(
                NodeId.UNASSIGNED,
                span(leftNode, rightNode),
                leftNode,
                operator,
                span(operatorToken),
                rightNode));
    }

    private Optional<ExpressionNode> buildNullCoalescence(ExpressionEvaluatorParser.CoalesceOperationContext context) {
        List<ExpressionNode> operands = new ArrayList<>(context.orExpression().size());
        for (ExpressionEvaluatorParser.OrExpressionContext operandContext : context.orExpression()) {
            Optional<ExpressionNode> operand = visit(operandContext);
            if (operand.isEmpty()) {
                return Optional.empty();
            }
            operands.add(operand.orElseThrow());
        }
        List<SourceSpan> operatorSpans = directOperatorTokens(context, ExpressionEvaluatorParser.NULLCOALESCE).stream()
                .map(SemanticAstBuilder::span)
                .toList();
        return Optional.of(new NullCoalescenceNode(NodeId.UNASSIGNED, span(context), operands, operatorSpans));
    }

    private Optional<ExpressionNode> buildLeftAssociative(
            List<? extends ParserRuleContext> operandContexts,
            ParserRuleContext context,
            int... operatorTypes) {
        List<Token> operatorTokens = directOperatorTokens(context, operatorTypes);
        Optional<ExpressionNode> first = visit(operandContexts.getFirst());
        if (first.isEmpty()) {
            return Optional.empty();
        }
        ExpressionNode current = first.orElseThrow();
        for (int index = 0; index < operatorTokens.size(); index++) {
            Optional<ExpressionNode> right = visit(operandContexts.get(index + 1));
            if (right.isEmpty()) {
                return Optional.empty();
            }
            ExpressionNode rightNode = right.orElseThrow();
            Token operatorToken = operatorTokens.get(index);
            current = new BinaryOperationNode(
                    NodeId.UNASSIGNED,
                    span(current, rightNode),
                    current,
                    binaryOperator(operatorToken),
                    span(operatorToken),
                    rightNode);
        }
        return Optional.of(current);
    }

    private static List<Token> directOperatorTokens(ParserRuleContext context, int... operatorTypes) {
        List<Token> tokens = new ArrayList<>();
        for (int index = 0; index < context.getChildCount(); index++) {
            if (context.getChild(index) instanceof TerminalNode terminal && isOneOf(terminal.getSymbol(), operatorTypes)) {
                tokens.add(terminal.getSymbol());
            }
        }
        return tokens;
    }

    private static boolean isOneOf(Token token, int... tokenTypes) {
        for (int tokenType : tokenTypes) {
            if (token.getType() == tokenType) {
                return true;
            }
        }
        return false;
    }

    private static TerminalNode firstTerminal(ParserRuleContext context) {
        for (int index = 0; index < context.getChildCount(); index++) {
            if (context.getChild(index) instanceof TerminalNode terminal) {
                return terminal;
            }
        }
        throw new IllegalArgumentException("Context has no terminal child: " + context.getClass().getName());
    }

    private static BinaryOperator comparisonOperator(ExpressionEvaluatorParser.ComparisonOperatorContext context) {
        return switch (context.getStart().getType()) {
            case ExpressionEvaluatorParser.GT -> BinaryOperator.GREATER_THAN;
            case ExpressionEvaluatorParser.GE -> BinaryOperator.GREATER_THAN_OR_EQUAL;
            case ExpressionEvaluatorParser.LT -> BinaryOperator.LESS_THAN;
            case ExpressionEvaluatorParser.LE -> BinaryOperator.LESS_THAN_OR_EQUAL;
            case ExpressionEvaluatorParser.EQ -> BinaryOperator.EQUAL;
            case ExpressionEvaluatorParser.NEQ -> BinaryOperator.NOT_EQUAL;
            default -> throw new IllegalArgumentException("Unsupported comparison operator: " + context.getText());
        };
    }

    private static BinaryOperator binaryOperator(Token token) {
        return switch (token.getType()) {
            case ExpressionEvaluatorParser.OR -> BinaryOperator.LOGICAL_OR;
            case ExpressionEvaluatorParser.AND -> BinaryOperator.LOGICAL_AND;
            case ExpressionEvaluatorParser.NAND -> BinaryOperator.LOGICAL_NAND;
            case ExpressionEvaluatorParser.NOR -> BinaryOperator.LOGICAL_NOR;
            case ExpressionEvaluatorParser.XOR -> BinaryOperator.LOGICAL_XOR;
            case ExpressionEvaluatorParser.XNOR -> BinaryOperator.LOGICAL_XNOR;
            case ExpressionEvaluatorParser.CONCAT -> BinaryOperator.CONCAT;
            case ExpressionEvaluatorParser.PLUS -> BinaryOperator.ADD;
            case ExpressionEvaluatorParser.MINUS -> BinaryOperator.SUBTRACT;
            case ExpressionEvaluatorParser.MULT -> BinaryOperator.MULTIPLY;
            case ExpressionEvaluatorParser.DIV -> BinaryOperator.DIVIDE;
            case ExpressionEvaluatorParser.MODULO -> BinaryOperator.MODULO;
            case ExpressionEvaluatorParser.ROOT -> BinaryOperator.ROOT;
            default -> throw new IllegalArgumentException("Unsupported binary operator token: " + token.getText());
        };
    }

    private static PostfixOperator postfixOperator(Token token) {
        return switch (token.getType()) {
            case ExpressionEvaluatorParser.PERCENT -> PostfixOperator.PERCENT;
            case ExpressionEvaluatorParser.EXCLAMATION -> PostfixOperator.FACTORIAL;
            default -> throw new IllegalArgumentException("Unsupported postfix operator token: " + token.getText());
        };
    }

    private static LiteralValue parseInteger(String text) {
        BigInteger value;
        if (text.startsWith("0x") || text.startsWith("0X")) {
            value = new BigInteger(text.substring(2), 16);
        } else if (isOctalLiteral(text)) {
            value = new BigInteger(text.substring(1), 8);
        } else {
            value = new BigInteger(text);
        }
        if (value.bitLength() < Long.SIZE) {
            return new LongLiteralValue(value.longValue());
        }
        return new BigIntegerLiteralValue(value);
    }

    private static boolean isOctalLiteral(String text) {
        if (text.length() <= 1 || text.charAt(0) != '0') {
            return false;
        }
        for (int index = 1; index < text.length(); index++) {
            char digit = text.charAt(index);
            if (digit < '0' || digit > '7') {
                return false;
            }
        }
        return true;
    }

    private static String unquote(String text) {
        String content = text.substring(1, text.length() - 1);
        StringBuilder builder = new StringBuilder(content.length());
        boolean escaping = false;
        for (int index = 0; index < content.length(); index++) {
            char current = content.charAt(index);
            if (!escaping) {
                if (current == '\\') {
                    escaping = true;
                } else {
                    builder.append(current);
                }
                continue;
            }
            switch (current) {
                case 'b' -> builder.append('\b');
                case 't' -> builder.append('\t');
                case 'n' -> builder.append('\n');
                case 'f' -> builder.append('\f');
                case 'r' -> builder.append('\r');
                case '"' -> builder.append('"');
                case '\'' -> builder.append('\'');
                case '\\' -> builder.append('\\');
                default -> throw new IllegalArgumentException("Unsupported string escape: \\" + current);
            }
            escaping = false;
        }
        return builder.toString();
    }

    private static String unquoteTemporal(String text, int prefixLength) {
        return text.substring(prefixLength, text.length() - 1);
    }

    private static boolean hasOffset(String dateTime) {
        int timeSeparator = dateTime.indexOf('T');
        for (int index = timeSeparator + 1; index < dateTime.length(); index++) {
            char current = dateTime.charAt(index);
            if (current == '+' || current == '-') {
                return true;
            }
        }
        return false;
    }

    private void addInvalidLiteralDiagnostic(
            DiagnosticCode code,
            ParserRuleContext context,
            String value) {
        diagnostics.add(new ExpressionDiagnostic(
                DiagnosticCategory.SEMANTIC,
                code,
                "Invalid literal value: \"" + value + "\"",
                span(context)));
    }

    private static SourceSpan fileSpan(List<? extends AstNode> topLevelNodes, ExpressionEvaluatorParser.StartInputContext context) {
        if (!topLevelNodes.isEmpty()) {
            SourceSpan first = topLevelNodes.getFirst().sourceSpan();
            SourceSpan last = topLevelNodes.getLast().sourceSpan();
            return new SourceSpan(first.offset(), last.endOffset(), first.line(), first.column());
        }
        return span(context.EOF().getSymbol());
    }

    private static SourceSpan span(ParserRuleContext context) {
        Token start = context.getStart();
        Token stop = context.getStop();
        if (stop.getType() == Token.EOF) {
            return new SourceSpan(
                    start.getStartIndex(),
                    start.getStartIndex(),
                    start.getLine(),
                    start.getCharPositionInLine() + 1);
        }
        return new SourceSpan(start.getStartIndex(), stop.getStopIndex() + 1, start.getLine(), start.getCharPositionInLine() + 1);
    }

    private static SourceSpan span(Token token) {
        int offset = Math.max(0, token.getStartIndex());
        int endOffset = token.getType() == Token.EOF ? offset : Math.max(offset, token.getStopIndex() + 1);
        return new SourceSpan(offset, endOffset, Math.max(1, token.getLine()), token.getCharPositionInLine() + 1);
    }

    private static SourceSpan span(Token start, Token stop) {
        SourceSpan startSpan = span(start);
        SourceSpan stopSpan = span(stop);
        return new SourceSpan(startSpan.offset(), stopSpan.endOffset(), startSpan.line(), startSpan.column());
    }

    private static SourceSpan span(Token start, AstNode stop) {
        SourceSpan startSpan = span(start);
        SourceSpan stopSpan = stop.sourceSpan();
        return new SourceSpan(startSpan.offset(), stopSpan.endOffset(), startSpan.line(), startSpan.column());
    }

    private static SourceSpan span(AstNode start, AstNode stop) {
        SourceSpan startSpan = start.sourceSpan();
        SourceSpan stopSpan = stop.sourceSpan();
        return new SourceSpan(startSpan.offset(), stopSpan.endOffset(), startSpan.line(), startSpan.column());
    }

    private static UnsupportedOperationException unsupported(ParserRuleContext context, String construct) {
        return new UnsupportedOperationException("AST tracer does not yet support " + construct + " at " + span(context));
    }
}
