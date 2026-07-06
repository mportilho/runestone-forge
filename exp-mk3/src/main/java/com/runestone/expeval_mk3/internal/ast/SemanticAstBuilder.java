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
        throw unsupported(context, "comparison operation");
    }

    @Override
    public Optional<ExpressionNode> visitInOperation(ExpressionEvaluatorParser.InOperationContext context) {
        throw unsupported(context, "in operation");
    }

    @Override
    public Optional<ExpressionNode> visitNinOperation(ExpressionEvaluatorParser.NinOperationContext context) {
        throw unsupported(context, "nin operation");
    }

    @Override
    public Optional<ExpressionNode> visitBetweenOperation(ExpressionEvaluatorParser.BetweenOperationContext context) {
        throw unsupported(context, "between operation");
    }

    @Override
    public Optional<ExpressionNode> visitRegexMatchOperation(ExpressionEvaluatorParser.RegexMatchOperationContext context) {
        throw unsupported(context, "regex match operation");
    }

    @Override
    public Optional<ExpressionNode> visitRegexNotMatchOperation(ExpressionEvaluatorParser.RegexNotMatchOperationContext context) {
        throw unsupported(context, "regex not-match operation");
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
        throw unsupported(context, "null coalescence");
    }

    @Override
    public Optional<ExpressionNode> visitLogicalOrOperation(ExpressionEvaluatorParser.LogicalOrOperationContext context) {
        if (context.andExpression().size() == 1) {
            return visit(context.andExpression(0));
        }
        throw unsupported(context, "logical or");
    }

    @Override
    public Optional<ExpressionNode> visitLogicalAndOperation(ExpressionEvaluatorParser.LogicalAndOperationContext context) {
        if (context.comparisonExpression().size() == 1) {
            return visit(context.comparisonExpression(0));
        }
        throw unsupported(context, "logical and");
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
        throw unsupported(context, "bitwise logical operation");
    }

    @Override
    public Optional<ExpressionNode> visitStringConcatenationOperation(ExpressionEvaluatorParser.StringConcatenationOperationContext context) {
        if (context.additiveExpression().size() == 1) {
            return visit(context.additiveExpression(0));
        }
        throw unsupported(context, "string concatenation");
    }

    @Override
    public Optional<ExpressionNode> visitAdditiveOperation(ExpressionEvaluatorParser.AdditiveOperationContext context) {
        if (context.multiplicativeExpression().size() == 1) {
            return visit(context.multiplicativeExpression(0));
        }
        throw unsupported(context, "additive operation");
    }

    @Override
    public Optional<ExpressionNode> visitMultiplicativeOperation(ExpressionEvaluatorParser.MultiplicativeOperationContext context) {
        if (context.unaryExpression().size() == 1) {
            return visit(context.unaryExpression(0));
        }
        throw unsupported(context, "multiplicative operation");
    }

    @Override
    public Optional<ExpressionNode> visitUnaryMinusOperation(ExpressionEvaluatorParser.UnaryMinusOperationContext context) {
        throw unsupported(context, "unary minus operation");
    }

    @Override
    public Optional<ExpressionNode> visitLogicalNotOperation(ExpressionEvaluatorParser.LogicalNotOperationContext context) {
        throw unsupported(context, "logical not operation");
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
        throw unsupported(context, "root operation");
    }

    @Override
    public Optional<ExpressionNode> visitExponentiationOperation(ExpressionEvaluatorParser.ExponentiationOperationContext context) {
        if (context.unaryExpression() == null) {
            return visit(context.postfixExpression());
        }
        throw unsupported(context, "exponentiation");
    }

    @Override
    public Optional<ExpressionNode> visitPostfixOperation(ExpressionEvaluatorParser.PostfixOperationContext context) {
        if (context.PERCENT().isEmpty() && context.EXCLAMATION().isEmpty()) {
            return visit(context.primaryExpression());
        }
        throw unsupported(context, "postfix operation");
    }

    @Override
    public Optional<ExpressionNode> visitParenthesisOperation(ExpressionEvaluatorParser.ParenthesisOperationContext context) {
        throw unsupported(context, "source grouping");
    }

    @Override
    public Optional<ExpressionNode> visitDecisionOperation(ExpressionEvaluatorParser.DecisionOperationContext context) {
        throw unsupported(context, "conditional expression");
    }

    @Override
    public Optional<ExpressionNode> visitVectorLiteralOperation(ExpressionEvaluatorParser.VectorLiteralOperationContext context) {
        throw unsupported(context, "vector literal");
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
        if (context.memberChain().isEmpty()) {
            return Optional.of(new IdentifierNode(NodeId.UNASSIGNED, span(context), context.IDENTIFIER().getText()));
        }
        throw unsupported(context, "member chain");
    }

    @Override
    public Optional<ExpressionNode> visitFunctionReferenceTarget(ExpressionEvaluatorParser.FunctionReferenceTargetContext context) {
        throw unsupported(context, "function call");
    }

    @Override
    public Optional<ExpressionNode> visitAtReferenceTarget(ExpressionEvaluatorParser.AtReferenceTargetContext context) {
        throw unsupported(context, "current item reference");
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

    private static UnsupportedOperationException unsupported(ParserRuleContext context, String construct) {
        return new UnsupportedOperationException("AST tracer does not yet support " + construct + " at " + span(context));
    }
}
