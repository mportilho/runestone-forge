package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.internal.grammar.ExpressionEvaluatorBaseVisitor;
import com.runestone.expeval_mk3.internal.grammar.ExpressionEvaluatorParser;
import com.runestone.expeval_mk3.internal.diagnostics.DiagnosticCategory;
import com.runestone.expeval_mk3.internal.diagnostics.DiagnosticCode;
import com.runestone.expeval_mk3.internal.diagnostics.ExpressionDiagnostic;
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
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

final class SemanticAstBuilder {

    SemanticAstBuildResult build(ParseSuccess parseSuccess) {
        return new SemanticAstBuildSession().build(parseSuccess);
    }
}

final class SemanticAstBuildSession extends ExpressionEvaluatorBaseVisitor<ExpressionNode> {

    private final List<ExpressionDiagnostic> diagnostics = new ArrayList<>();

    SemanticAstBuildResult build(ParseSuccess parseSuccess) {
        Objects.requireNonNull(parseSuccess, "parseSuccess");
        if (!(parseSuccess.tree() instanceof ExpressionEvaluatorParser.StartInputContext start)) {
            throw new IllegalArgumentException(
                    "Unsupported parser start context: " + parseSuccess.tree().getClass().getName());
        }

        List<AssignmentNode> assignments = new ArrayList<>(start.assignmentExpression().size());
        for (ExpressionEvaluatorParser.AssignmentExpressionContext assignmentContext : start.assignmentExpression()) {
            assignments.add(buildAssignment(assignmentContext));
        }
        Optional<ExpressionNode> resultExpression = Optional.ofNullable(start.expression()).map(this::visit);
        List<AstNode> topLevelNodes = new ArrayList<>(assignments.size() + resultExpression.map(expression -> 1).orElse(0));
        topLevelNodes.addAll(assignments);
        resultExpression.ifPresent(topLevelNodes::add);
        ExpressionFileNode unassigned = new ExpressionFileNode(
                NodeId.UNASSIGNED,
                fileSpan(topLevelNodes, start),
                assignments,
                resultExpression);
        if (!diagnostics.isEmpty()) {
            return new SemanticAstBuildFailure(diagnostics);
        }
        return new SemanticAstBuildSuccess(new AstNodeIdAssigner().assign(unassigned));
    }

    private AssignmentNode buildAssignment(ExpressionEvaluatorParser.AssignmentExpressionContext context) {
        if (context instanceof ExpressionEvaluatorParser.AssignmentOperationContext assignment) {
            TerminalNode identifier = assignment.IDENTIFIER();
            return new AssignmentNode(
                    NodeId.UNASSIGNED,
                    span(assignment),
                    new IdentifierAssignmentTargetNode(
                            NodeId.UNASSIGNED,
                            span(identifier.getSymbol()),
                            identifier.getText()),
                    visit(assignment.expression()));
        }
        throw unsupported(context, "destructuring assignment");
    }

    @Override
    public ExpressionNode visitComparisonOperation(ExpressionEvaluatorParser.ComparisonOperationContext context) {
        ExpressionNode left = visit(context.bitwiseLogicalExpression(0));
        ExpressionNode right = visit(context.bitwiseLogicalExpression(1));
        return new BinaryOperationNode(
                NodeId.UNASSIGNED,
                span(context),
                left,
                comparisonOperator(context.comparisonOperator().getStart()),
                span(context.comparisonOperator()),
                right);
    }

    @Override
    public ExpressionNode visitInOperation(ExpressionEvaluatorParser.InOperationContext context) {
        SourceSpan operatorSpan = context.NOT_KW() == null
                ? span(context.IN().getSymbol())
                : span(context.NOT_KW().getSymbol(), context.IN().getSymbol());
        return new MembershipNode(
                NodeId.UNASSIGNED,
                span(context),
                visit(context.bitwiseLogicalExpression(0)),
                context.NOT_KW() != null,
                operatorSpan,
                visit(context.bitwiseLogicalExpression(1)));
    }

    @Override
    public ExpressionNode visitNinOperation(ExpressionEvaluatorParser.NinOperationContext context) {
        return new MembershipNode(
                NodeId.UNASSIGNED,
                span(context),
                visit(context.bitwiseLogicalExpression(0)),
                true,
                span(context.NIN().getSymbol()),
                visit(context.bitwiseLogicalExpression(1)));
    }

    @Override
    public ExpressionNode visitBetweenOperation(ExpressionEvaluatorParser.BetweenOperationContext context) {
        SourceSpan operatorSpan = context.NOT_KW() == null
                ? span(context.BETWEEN().getSymbol())
                : span(context.NOT_KW().getSymbol(), context.BETWEEN().getSymbol());
        return new BetweenNode(
                NodeId.UNASSIGNED,
                span(context),
                visit(context.bitwiseLogicalExpression(0)),
                context.NOT_KW() != null,
                operatorSpan,
                visit(context.bitwiseLogicalExpression(1)),
                span(context.AND().getSymbol()),
                visit(context.bitwiseLogicalExpression(2)));
    }

    @Override
    public ExpressionNode visitRegexMatchOperation(ExpressionEvaluatorParser.RegexMatchOperationContext context) {
        return new BinaryOperationNode(
                NodeId.UNASSIGNED,
                span(context),
                visit(context.bitwiseLogicalExpression()),
                BinaryOperator.REGEX_MATCH,
                span(context.REGEX_MATCH().getSymbol()),
                stringLiteral(context.STRING().getSymbol()));
    }

    @Override
    public ExpressionNode visitRegexNotMatchOperation(ExpressionEvaluatorParser.RegexNotMatchOperationContext context) {
        return new BinaryOperationNode(
                NodeId.UNASSIGNED,
                span(context),
                visit(context.bitwiseLogicalExpression()),
                BinaryOperator.REGEX_NOT_MATCH,
                span(context.REGEX_NOT_MATCH().getSymbol()),
                stringLiteral(context.STRING().getSymbol()));
    }

    @Override
    public ExpressionNode visitExpressionOperation(ExpressionEvaluatorParser.ExpressionOperationContext context) {
        return visit(context.coalesceExpression());
    }

    @Override
    public ExpressionNode visitCoalesceOperation(ExpressionEvaluatorParser.CoalesceOperationContext context) {
        if (context.orExpression().size() == 1) {
            return visit(context.orExpression(0));
        }
        List<ExpressionNode> operands = new ArrayList<>(context.orExpression().size());
        for (ExpressionEvaluatorParser.OrExpressionContext operand : context.orExpression()) {
            operands.add(visit(operand));
        }
        List<SourceSpan> operatorSpans = new ArrayList<>(operands.size() - 1);
        for (int operatorIndex = 1; operatorIndex < context.getChildCount(); operatorIndex += 2) {
            operatorSpans.add(span(terminalToken(context, operatorIndex)));
        }
        return new NullCoalesceNode(NodeId.UNASSIGNED, span(context), operands, operatorSpans);
    }

    @Override
    public ExpressionNode visitLogicalOrOperation(ExpressionEvaluatorParser.LogicalOrOperationContext context) {
        if (context.andExpression().size() == 1) {
            return visit(context.andExpression(0));
        }
        return buildLeftAssociative(context, context.andExpression(), ignored -> BinaryOperator.LOGICAL_OR);
    }

    @Override
    public ExpressionNode visitLogicalAndOperation(ExpressionEvaluatorParser.LogicalAndOperationContext context) {
        if (context.comparisonExpression().size() == 1) {
            return visit(context.comparisonExpression(0));
        }
        return buildLeftAssociative(context, context.comparisonExpression(), ignored -> BinaryOperator.LOGICAL_AND);
    }

    @Override
    public ExpressionNode visitBitwisePassthroughOperation(ExpressionEvaluatorParser.BitwisePassthroughOperationContext context) {
        return visit(context.bitwiseLogicalExpression());
    }

    @Override
    public ExpressionNode visitLogicalBitwiseOperation(ExpressionEvaluatorParser.LogicalBitwiseOperationContext context) {
        if (context.concatExpression().size() == 1) {
            return visit(context.concatExpression(0));
        }
        return buildLeftAssociative(context, context.concatExpression(), SemanticAstBuildSession::bitwiseOperator);
    }

    @Override
    public ExpressionNode visitStringConcatenationOperation(ExpressionEvaluatorParser.StringConcatenationOperationContext context) {
        if (context.additiveExpression().size() == 1) {
            return visit(context.additiveExpression(0));
        }
        return buildLeftAssociative(context, context.additiveExpression(), ignored -> BinaryOperator.CONCATENATE);
    }

    @Override
    public ExpressionNode visitAdditiveOperation(ExpressionEvaluatorParser.AdditiveOperationContext context) {
        if (context.multiplicativeExpression().size() == 1) {
            return visit(context.multiplicativeExpression(0));
        }
        return buildLeftAssociative(context, context.multiplicativeExpression(), SemanticAstBuildSession::additiveOperator);
    }

    @Override
    public ExpressionNode visitMultiplicativeOperation(ExpressionEvaluatorParser.MultiplicativeOperationContext context) {
        if (context.unaryExpression().size() == 1) {
            return visit(context.unaryExpression(0));
        }
        return buildLeftAssociative(context, context.unaryExpression(), SemanticAstBuildSession::multiplicativeOperator);
    }

    @Override
    public ExpressionNode visitUnaryMinusOperation(ExpressionEvaluatorParser.UnaryMinusOperationContext context) {
        return new UnaryOperationNode(
                NodeId.UNASSIGNED,
                span(context),
                UnaryOperator.NEGATE,
                span(context.MINUS().getSymbol()),
                visit(context.unaryExpression()));
    }

    @Override
    public ExpressionNode visitLogicalNotOperation(ExpressionEvaluatorParser.LogicalNotOperationContext context) {
        Token operator = context.NOT() == null ? context.EXCLAMATION().getSymbol() : context.NOT().getSymbol();
        return new UnaryOperationNode(
                NodeId.UNASSIGNED,
                span(context),
                UnaryOperator.LOGICAL_NOT,
                span(operator),
                visit(context.unaryExpression()));
    }

    @Override
    public ExpressionNode visitRootPassthroughOperation(ExpressionEvaluatorParser.RootPassthroughOperationContext context) {
        return visit(context.rootExpression());
    }

    @Override
    public ExpressionNode visitRootChainOperation(ExpressionEvaluatorParser.RootChainOperationContext context) {
        if (context.exponentiationExpression().size() == 1) {
            return visit(context.exponentiationExpression(0));
        }
        return buildLeftAssociative(context, context.exponentiationExpression(), ignored -> BinaryOperator.ROOT);
    }

    @Override
    public ExpressionNode visitExponentiationOperation(ExpressionEvaluatorParser.ExponentiationOperationContext context) {
        if (context.unaryExpression() == null) {
            return visit(context.postfixExpression());
        }
        return new BinaryOperationNode(
                NodeId.UNASSIGNED,
                span(context),
                visit(context.postfixExpression()),
                BinaryOperator.EXPONENTIATE,
                span(context.EXPONENTIATION().getSymbol()),
                visit(context.unaryExpression()));
    }

    @Override
    public ExpressionNode visitPostfixOperation(ExpressionEvaluatorParser.PostfixOperationContext context) {
        if (context.PERCENT().isEmpty() && context.EXCLAMATION().isEmpty()) {
            return visit(context.primaryExpression());
        }
        List<PostfixOperatorOccurrence> operations = new ArrayList<>(context.getChildCount() - 1);
        for (int childIndex = 1; childIndex < context.getChildCount(); childIndex++) {
            Token operator = terminalToken(context, childIndex);
            operations.add(new PostfixOperatorOccurrence(postfixOperator(operator), span(operator)));
        }
        return new PostfixOperationNode(NodeId.UNASSIGNED, span(context), visit(context.primaryExpression()), operations);
    }

    @Override
    public ExpressionNode visitParenthesisOperation(ExpressionEvaluatorParser.ParenthesisOperationContext context) {
        return new GroupedExpressionNode(NodeId.UNASSIGNED, span(context), visit(context.expression()));
    }

    @Override
    public ExpressionNode visitDecisionOperation(ExpressionEvaluatorParser.DecisionOperationContext context) {
        return visit(context.ifExpression());
    }

    @Override
    public ExpressionNode visitIfThenElseOperation(ExpressionEvaluatorParser.IfThenElseOperationContext context) {
        List<ExpressionNode> expressions = visitExpressions(context.expression());
        List<ConditionalBranchNode> branches = new ArrayList<>(context.THEN().size());
        branches.add(conditionalBranch(expressions.get(0), expressions.get(1)));
        for (int elseifIndex = 0; elseifIndex < context.ELSEIF().size(); elseifIndex++) {
            int expressionIndex = 2 + elseifIndex * 2;
            branches.add(conditionalBranch(expressions.get(expressionIndex), expressions.get(expressionIndex + 1)));
        }
        return new ConditionalNode(
                NodeId.UNASSIGNED,
                span(context),
                ConditionalSyntax.CLASSIC,
                branches,
                List.of(),
                expressions.getLast());
    }

    @Override
    public ExpressionNode visitFunctionalIfOperation(ExpressionEvaluatorParser.FunctionalIfOperationContext context) {
        List<ExpressionNode> expressions = visitExpressions(context.expression());
        List<ConditionalBranchNode> branches = new ArrayList<>(expressions.size() / 2);
        for (int expressionIndex = 0; expressionIndex < expressions.size() - 1; expressionIndex += 2) {
            branches.add(conditionalBranch(expressions.get(expressionIndex), expressions.get(expressionIndex + 1)));
        }
        return new ConditionalNode(
                NodeId.UNASSIGNED,
                span(context),
                ConditionalSyntax.FUNCTIONAL,
                branches,
                functionalSeparators(context),
                expressions.getLast());
    }

    @Override
    public ExpressionNode visitVectorLiteralOperation(ExpressionEvaluatorParser.VectorLiteralOperationContext context) {
        return visit(context.vectorLiteral());
    }

    @Override
    public ExpressionNode visitVectorOfEntitiesOperation(ExpressionEvaluatorParser.VectorOfEntitiesOperationContext context) {
        List<ExpressionNode> elements = new ArrayList<>(context.expression().size());
        for (ExpressionEvaluatorParser.ExpressionContext expression : context.expression()) {
            elements.add(visit(expression));
        }
        return new VectorLiteralNode(NodeId.UNASSIGNED, span(context), elements);
    }

    @Override
    public ExpressionNode visitLiteralOperation(ExpressionEvaluatorParser.LiteralOperationContext context) {
        return visit(context.literal());
    }

    @Override
    public ExpressionNode visitReferenceTargetOperation(ExpressionEvaluatorParser.ReferenceTargetOperationContext context) {
        return visit(context.referenceTarget());
    }

    @Override
    public ExpressionNode visitIdentifierReferenceTarget(ExpressionEvaluatorParser.IdentifierReferenceTargetContext context) {
        if (context.memberChain().isEmpty()) {
            return new IdentifierNode(NodeId.UNASSIGNED, span(context), context.IDENTIFIER().getText());
        }
        throw unsupported(context, "member chain");
    }

    @Override
    public ExpressionNode visitFunctionReferenceTarget(ExpressionEvaluatorParser.FunctionReferenceTargetContext context) {
        throw unsupported(context, "function call");
    }

    @Override
    public ExpressionNode visitAtReferenceTarget(ExpressionEvaluatorParser.AtReferenceTargetContext context) {
        throw unsupported(context, "current item reference");
    }

    @Override
    public ExpressionNode visitIntConstantOperation(ExpressionEvaluatorParser.IntConstantOperationContext context) {
        return new LiteralNode(
                NodeId.UNASSIGNED,
                span(context),
                parseInteger(context.INT().getText()));
    }

    @Override
    public ExpressionNode visitFloatConstantOperation(ExpressionEvaluatorParser.FloatConstantOperationContext context) {
        return new LiteralNode(
                NodeId.UNASSIGNED,
                span(context),
                new DecimalLiteralValue(new BigDecimal(context.FLOAT().getText())));
    }

    @Override
    public ExpressionNode visitStringConstantOperation(ExpressionEvaluatorParser.StringConstantOperationContext context) {
        return new LiteralNode(
                NodeId.UNASSIGNED,
                span(context),
                new StringLiteralValue(unquote(context.STRING().getText())));
    }

    @Override
    public ExpressionNode visitLogicalConstantOperation(ExpressionEvaluatorParser.LogicalConstantOperationContext context) {
        return new LiteralNode(NodeId.UNASSIGNED, span(context), new BooleanLiteralValue(context.TRUE() != null));
    }

    @Override
    public ExpressionNode visitNullConstantOperation(ExpressionEvaluatorParser.NullConstantOperationContext context) {
        return new LiteralNode(NodeId.UNASSIGNED, span(context), new NullLiteralValue());
    }

    @Override
    public ExpressionNode visitDateConstantOperation(ExpressionEvaluatorParser.DateConstantOperationContext context) {
        return new LiteralNode(
                NodeId.UNASSIGNED,
                span(context),
                materializeDate(context));
    }

    @Override
    public ExpressionNode visitTimeConstantOperation(ExpressionEvaluatorParser.TimeConstantOperationContext context) {
        return new LiteralNode(
                NodeId.UNASSIGNED,
                span(context),
                materializeTime(context));
    }

    @Override
    public ExpressionNode visitDateTimeConstantOperation(ExpressionEvaluatorParser.DateTimeConstantOperationContext context) {
        return new LiteralNode(NodeId.UNASSIGNED, span(context), materializeDateTime(context));
    }

    @Override
    public ExpressionNode visitDateCurrentValueOperation(ExpressionEvaluatorParser.DateCurrentValueOperationContext context) {
        return new CurrentTemporalValueNode(NodeId.UNASSIGNED, span(context), CurrentTemporalValueKind.DATE);
    }

    @Override
    public ExpressionNode visitTimeCurrentValueOperation(ExpressionEvaluatorParser.TimeCurrentValueOperationContext context) {
        return new CurrentTemporalValueNode(NodeId.UNASSIGNED, span(context), CurrentTemporalValueKind.TIME);
    }

    @Override
    public ExpressionNode visitDateTimeCurrentValueOperation(
            ExpressionEvaluatorParser.DateTimeCurrentValueOperationContext context) {
        return new CurrentTemporalValueNode(NodeId.UNASSIGNED, span(context), CurrentTemporalValueKind.DATE_TIME);
    }

    private ExpressionNode buildLeftAssociative(
            ParserRuleContext context,
            List<? extends ParserRuleContext> operandContexts,
            Function<Token, BinaryOperator> operatorMapper) {
        ExpressionNode result = visit(operandContexts.getFirst());
        for (int operandIndex = 1; operandIndex < operandContexts.size(); operandIndex++) {
            Token operator = terminalToken(context, operandIndex * 2 - 1);
            ExpressionNode right = visit(operandContexts.get(operandIndex));
            result = new BinaryOperationNode(
                    NodeId.UNASSIGNED,
                    span(result.sourceSpan(), right.sourceSpan()),
                    result,
                    operatorMapper.apply(operator),
                    span(operator),
                    right);
        }
        return result;
    }

    private List<ExpressionNode> visitExpressions(List<ExpressionEvaluatorParser.ExpressionContext> expressionContexts) {
        List<ExpressionNode> expressions = new ArrayList<>(expressionContexts.size());
        for (ExpressionEvaluatorParser.ExpressionContext expressionContext : expressionContexts) {
            expressions.add(visit(expressionContext));
        }
        return expressions;
    }

    private ConditionalBranchNode conditionalBranch(ExpressionNode condition, ExpressionNode consequence) {
        return new ConditionalBranchNode(
                NodeId.UNASSIGNED,
                span(condition.sourceSpan(), consequence.sourceSpan()),
                condition,
                consequence);
    }

    private List<ConditionalSeparatorOccurrence> functionalSeparators(
            ExpressionEvaluatorParser.FunctionalIfOperationContext context) {
        List<ConditionalSeparatorOccurrence> separators = new ArrayList<>(context.expression().size() - 1);
        for (int childIndex = 0; childIndex < context.getChildCount(); childIndex++) {
            if (context.getChild(childIndex) instanceof TerminalNode terminal) {
                Token token = terminal.getSymbol();
                if (token.getType() == ExpressionEvaluatorParser.COMMA
                        || token.getType() == ExpressionEvaluatorParser.SEMI) {
                    separators.add(new ConditionalSeparatorOccurrence(conditionalSeparator(token), span(token)));
                }
            }
        }
        return separators;
    }

    private static ConditionalSeparator conditionalSeparator(Token token) {
        return switch (token.getType()) {
            case ExpressionEvaluatorParser.COMMA -> ConditionalSeparator.COMMA;
            case ExpressionEvaluatorParser.SEMI -> ConditionalSeparator.SEMICOLON;
            default -> throw new IllegalArgumentException("Unsupported conditional separator: " + token.getText());
        };
    }

    private ExpressionNode stringLiteral(Token token) {
        return new LiteralNode(NodeId.UNASSIGNED, span(token), new StringLiteralValue(unquote(token.getText())));
    }

    private static BinaryOperator comparisonOperator(Token token) {
        return switch (token.getType()) {
            case ExpressionEvaluatorParser.GT -> BinaryOperator.GREATER_THAN;
            case ExpressionEvaluatorParser.GE -> BinaryOperator.GREATER_THAN_OR_EQUAL;
            case ExpressionEvaluatorParser.LT -> BinaryOperator.LESS_THAN;
            case ExpressionEvaluatorParser.LE -> BinaryOperator.LESS_THAN_OR_EQUAL;
            case ExpressionEvaluatorParser.EQ -> BinaryOperator.EQUAL;
            case ExpressionEvaluatorParser.NEQ -> BinaryOperator.NOT_EQUAL;
            default -> throw new IllegalArgumentException("Unsupported comparison operator: " + token.getText());
        };
    }

    private static BinaryOperator bitwiseOperator(Token token) {
        return switch (token.getType()) {
            case ExpressionEvaluatorParser.NAND -> BinaryOperator.LOGICAL_NAND;
            case ExpressionEvaluatorParser.NOR -> BinaryOperator.LOGICAL_NOR;
            case ExpressionEvaluatorParser.XOR -> BinaryOperator.LOGICAL_XOR;
            case ExpressionEvaluatorParser.XNOR -> BinaryOperator.LOGICAL_XNOR;
            default -> throw new IllegalArgumentException("Unsupported bitwise logical operator: " + token.getText());
        };
    }

    private static BinaryOperator additiveOperator(Token token) {
        return switch (token.getType()) {
            case ExpressionEvaluatorParser.PLUS -> BinaryOperator.ADD;
            case ExpressionEvaluatorParser.MINUS -> BinaryOperator.SUBTRACT;
            default -> throw new IllegalArgumentException("Unsupported additive operator: " + token.getText());
        };
    }

    private static BinaryOperator multiplicativeOperator(Token token) {
        return switch (token.getType()) {
            case ExpressionEvaluatorParser.MULT -> BinaryOperator.MULTIPLY;
            case ExpressionEvaluatorParser.DIV -> BinaryOperator.DIVIDE;
            case ExpressionEvaluatorParser.MODULO -> BinaryOperator.MODULO;
            default -> throw new IllegalArgumentException("Unsupported multiplicative operator: " + token.getText());
        };
    }

    private static PostfixOperator postfixOperator(Token token) {
        return switch (token.getType()) {
            case ExpressionEvaluatorParser.PERCENT -> PostfixOperator.PERCENT;
            case ExpressionEvaluatorParser.EXCLAMATION -> PostfixOperator.FACTORIAL;
            default -> throw new IllegalArgumentException("Unsupported postfix operator: " + token.getText());
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

    private LiteralValue materializeDate(ExpressionEvaluatorParser.DateConstantOperationContext context) {
        Optional<LocalDate> date = parseDate(unquoteTemporal(context.DATE().getText(), 2));
        if (date.isEmpty()) {
            addDiagnostic(DiagnosticCode.AST_INVALID_DATE_LITERAL, "Invalid date literal", span(context));
            return new NullLiteralValue();
        }
        return new DateLiteralValue(date.orElseThrow());
    }

    private LiteralValue materializeTime(ExpressionEvaluatorParser.TimeConstantOperationContext context) {
        return new TimeLiteralValue(parseTime(unquoteTemporal(context.TIME().getText(), 2)));
    }

    private LiteralValue materializeDateTime(ExpressionEvaluatorParser.DateTimeConstantOperationContext context) {
        String value = unquoteTemporal(context.DATETIME().getText(), 3);
        int timeSeparator = value.indexOf('T');
        int offsetIndex = offsetIndex(value, timeSeparator + 1);
        String localDateTimeText = offsetIndex == -1 ? value : value.substring(0, offsetIndex);
        Optional<LocalDate> date = parseDate(localDateTimeText.substring(0, timeSeparator));
        LocalTime time = parseTime(localDateTimeText.substring(timeSeparator + 1));
        Optional<ZoneOffset> offset = offsetIndex == -1
                ? Optional.empty()
                : parseOffset(value.substring(offsetIndex));
        if (date.isEmpty() || (offsetIndex != -1 && offset.isEmpty())) {
            addDiagnostic(DiagnosticCode.AST_INVALID_DATE_TIME_LITERAL, "Invalid date-time literal", span(context));
            return new NullLiteralValue();
        }
        LocalDateTime localDateTime = LocalDateTime.of(date.orElseThrow(), time);
        if (offsetIndex == -1) {
            return new LocalDateTimeLiteralValue(localDateTime);
        }
        return new OffsetDateTimeLiteralValue(OffsetDateTime.of(localDateTime, offset.orElseThrow()));
    }

    private void addDiagnostic(DiagnosticCode code, String message, SourceSpan span) {
        diagnostics.add(new ExpressionDiagnostic(DiagnosticCategory.SEMANTIC, code, message, span));
    }

    private static Optional<LocalDate> parseDate(String value) {
        int year = parseFixedDigits(value, 0, 4);
        int month = parseFixedDigits(value, 5, 7);
        int day = parseFixedDigits(value, 8, 10);
        if (day > YearMonth.of(year, month).lengthOfMonth()) {
            return Optional.empty();
        }
        return Optional.of(LocalDate.of(year, month, day));
    }

    private static LocalTime parseTime(String value) {
        int hour = parseFixedDigits(value, 0, 2);
        int minute = parseFixedDigits(value, 3, 5);
        int second = value.length() == 8 ? parseFixedDigits(value, 6, 8) : 0;
        return LocalTime.of(hour, minute, second);
    }

    private static Optional<ZoneOffset> parseOffset(String value) {
        int sign = value.charAt(0) == '-' ? -1 : 1;
        int hour = parseFixedDigits(value, 1, 3);
        int minute = parseFixedDigits(value, 4, 6);
        if (hour > 18 || (hour == 18 && minute > 0)) {
            return Optional.empty();
        }
        return Optional.of(ZoneOffset.ofTotalSeconds(sign * (hour * 3_600 + minute * 60)));
    }

    private static int parseFixedDigits(String value, int startIndex, int endIndex) {
        int result = 0;
        for (int index = startIndex; index < endIndex; index++) {
            result = result * 10 + value.charAt(index) - '0';
        }
        return result;
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

    private static int offsetIndex(String dateTime, int startIndex) {
        for (int index = startIndex; index < dateTime.length(); index++) {
            char current = dateTime.charAt(index);
            if (current == '+' || current == '-') {
                return index;
            }
        }
        return -1;
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
        int offset = Math.max(0, start.getStartIndex());
        int endOffset = stop.getType() == Token.EOF ? offset : Math.max(offset, stop.getStopIndex() + 1);
        return new SourceSpan(offset, endOffset, Math.max(1, start.getLine()), start.getCharPositionInLine() + 1);
    }

    private static SourceSpan span(SourceSpan start, SourceSpan stop) {
        return new SourceSpan(start.offset(), stop.endOffset(), start.line(), start.column());
    }

    private static Token terminalToken(ParserRuleContext context, int childIndex) {
        return ((TerminalNode) context.getChild(childIndex)).getSymbol();
    }

    private static UnsupportedOperationException unsupported(ParserRuleContext context, String construct) {
        return new UnsupportedOperationException("AST tracer does not yet support " + construct + " at " + span(context));
    }
}
