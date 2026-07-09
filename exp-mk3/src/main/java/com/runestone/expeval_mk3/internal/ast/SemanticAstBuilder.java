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
        throw unsupported(context, "comparison operation");
    }

    @Override
    public ExpressionNode visitInOperation(ExpressionEvaluatorParser.InOperationContext context) {
        throw unsupported(context, "in operation");
    }

    @Override
    public ExpressionNode visitNinOperation(ExpressionEvaluatorParser.NinOperationContext context) {
        throw unsupported(context, "nin operation");
    }

    @Override
    public ExpressionNode visitBetweenOperation(ExpressionEvaluatorParser.BetweenOperationContext context) {
        throw unsupported(context, "between operation");
    }

    @Override
    public ExpressionNode visitRegexMatchOperation(ExpressionEvaluatorParser.RegexMatchOperationContext context) {
        throw unsupported(context, "regex match operation");
    }

    @Override
    public ExpressionNode visitRegexNotMatchOperation(ExpressionEvaluatorParser.RegexNotMatchOperationContext context) {
        throw unsupported(context, "regex not-match operation");
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
        throw unsupported(context, "null coalescence");
    }

    @Override
    public ExpressionNode visitLogicalOrOperation(ExpressionEvaluatorParser.LogicalOrOperationContext context) {
        if (context.andExpression().size() == 1) {
            return visit(context.andExpression(0));
        }
        throw unsupported(context, "logical or");
    }

    @Override
    public ExpressionNode visitLogicalAndOperation(ExpressionEvaluatorParser.LogicalAndOperationContext context) {
        if (context.comparisonExpression().size() == 1) {
            return visit(context.comparisonExpression(0));
        }
        throw unsupported(context, "logical and");
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
        throw unsupported(context, "bitwise logical operation");
    }

    @Override
    public ExpressionNode visitStringConcatenationOperation(ExpressionEvaluatorParser.StringConcatenationOperationContext context) {
        if (context.additiveExpression().size() == 1) {
            return visit(context.additiveExpression(0));
        }
        throw unsupported(context, "string concatenation");
    }

    @Override
    public ExpressionNode visitAdditiveOperation(ExpressionEvaluatorParser.AdditiveOperationContext context) {
        if (context.multiplicativeExpression().size() == 1) {
            return visit(context.multiplicativeExpression(0));
        }
        throw unsupported(context, "additive operation");
    }

    @Override
    public ExpressionNode visitMultiplicativeOperation(ExpressionEvaluatorParser.MultiplicativeOperationContext context) {
        if (context.unaryExpression().size() == 1) {
            return visit(context.unaryExpression(0));
        }
        throw unsupported(context, "multiplicative operation");
    }

    @Override
    public ExpressionNode visitUnaryMinusOperation(ExpressionEvaluatorParser.UnaryMinusOperationContext context) {
        throw unsupported(context, "unary minus operation");
    }

    @Override
    public ExpressionNode visitLogicalNotOperation(ExpressionEvaluatorParser.LogicalNotOperationContext context) {
        throw unsupported(context, "logical not operation");
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
        throw unsupported(context, "root operation");
    }

    @Override
    public ExpressionNode visitExponentiationOperation(ExpressionEvaluatorParser.ExponentiationOperationContext context) {
        if (context.unaryExpression() == null) {
            return visit(context.postfixExpression());
        }
        throw unsupported(context, "exponentiation");
    }

    @Override
    public ExpressionNode visitPostfixOperation(ExpressionEvaluatorParser.PostfixOperationContext context) {
        if (context.PERCENT().isEmpty() && context.EXCLAMATION().isEmpty()) {
            return visit(context.primaryExpression());
        }
        throw unsupported(context, "postfix operation");
    }

    @Override
    public ExpressionNode visitParenthesisOperation(ExpressionEvaluatorParser.ParenthesisOperationContext context) {
        throw unsupported(context, "source grouping");
    }

    @Override
    public ExpressionNode visitDecisionOperation(ExpressionEvaluatorParser.DecisionOperationContext context) {
        throw unsupported(context, "conditional expression");
    }

    @Override
    public ExpressionNode visitVectorLiteralOperation(ExpressionEvaluatorParser.VectorLiteralOperationContext context) {
        throw unsupported(context, "vector literal");
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

    private static UnsupportedOperationException unsupported(ParserRuleContext context, String construct) {
        return new UnsupportedOperationException("AST tracer does not yet support " + construct + " at " + span(context));
    }
}
