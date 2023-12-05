/*
 * MIT License
 * <p>
 * Copyright (c) 2023-2023 Marcelo Silva Portilho
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.runestone.expeval.grammar.parser.impl;

import com.runestone.expeval.exceptions.ExpressionParsingException;
import com.runestone.expeval.grammar.language.ExpressionEvaluatorBaseVisitor;
import com.runestone.expeval.grammar.language.ExpressionEvaluatorLexer;
import com.runestone.expeval.grammar.language.ExpressionEvaluatorParser;
import com.runestone.expeval.grammar.language.ExpressionEvaluatorParser.*;
import com.runestone.expeval.grammar.parser.LanguageData;
import com.runestone.expeval.grammar.parser.LanguageParser;
import com.runestone.expeval.operation.AbstractOperation;
import com.runestone.expeval.operation.BaseOperation;
import com.runestone.expeval.operation.datetime.*;
import com.runestone.expeval.operation.logic.*;
import com.runestone.expeval.operation.math.*;
import com.runestone.expeval.operation.math.NumberRoundingOperation.RoundingEnum;
import com.runestone.expeval.operation.math.logarithm.PreciseBinaryLogarithmOperation;
import com.runestone.expeval.operation.math.logarithm.PreciseCommonLogarithmOperation;
import com.runestone.expeval.operation.math.logarithm.PreciseLogarithmOperation;
import com.runestone.expeval.operation.math.logarithm.PreciseNaturalLogarithmOperation;
import com.runestone.expeval.operation.other.DecisionOperation;
import com.runestone.expeval.operation.other.FunctionOperation;
import com.runestone.expeval.operation.values.AbstractVariableValueOperation;
import com.runestone.expeval.operation.values.constant.*;
import com.runestone.expeval.operation.values.variable.*;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.runestone.expeval.grammar.language.ExpressionEvaluatorLexer.MINUS_DAYS;
import static com.runestone.expeval.grammar.language.ExpressionEvaluatorLexer.PLUS_DAYS;
import static com.runestone.expeval.grammar.language.ExpressionEvaluatorLexer.PLUS_MONTHS;
import static com.runestone.expeval.grammar.language.ExpressionEvaluatorLexer.PLUS_YEARS;
import static com.runestone.expeval.grammar.language.ExpressionEvaluatorLexer.VOCABULARY;
import static java.util.Objects.nonNull;

/**
 * Default implementation of {@link LanguageParser}
 */
public class DefaultLanguageParser extends ExpressionEvaluatorBaseVisitor<AbstractOperation> implements LanguageParser {

    protected final Map<String, AbstractVariableValueOperation> userVariables;
    protected final Map<String, AssignedVariableOperation> assignedVariables;
    protected Stack<List<SequenceVariableValueOperation>> sequenceVariableStack;

    public DefaultLanguageParser() {
        this.userVariables = new HashMap<>();
        this.assignedVariables = new LinkedHashMap<>();
    }

    @Override
    public LanguageData parse(ExpressionEvaluatorParser.StartContext startContext) {
        AbstractOperation abstractOperation = visit(startContext);
        abstractOperation.accept(new InitialConfigurationOperationVisitor());
        return new LanguageData(abstractOperation, userVariables, assignedVariables);
    }

    @Override
    public AbstractOperation visitMathStart(MathStartContext ctx) {
        for (AssignmentExpressionContext expressionContext : ctx.assignmentExpression()) {
            expressionContext.accept(this);
        }
        AbstractOperation mathOperation = ctx.mathExpression() != null ? ctx.mathExpression().accept(this) : null;
        return new BaseOperation(mathOperation, assignedVariables);
    }

    @Override
    public AbstractOperation visitLogicalStart(LogicalStartContext ctx) {
        for (AssignmentExpressionContext expressionContext : ctx.assignmentExpression()) {
            expressionContext.accept(this);
        }
        AbstractOperation logicalOperation = ctx.logicalExpression() != null ? ctx.logicalExpression().accept(this) : null;
        return new BaseOperation(logicalOperation, assignedVariables);
    }

    @Override
    public AbstractOperation visitAssignOperation(AssignOperationContext ctx) {
        if (ctx.EQ() != null) {
            throw new ExpressionParsingException("Invalid assignment operation: '='. Use ':=' instead");
        }
        AbstractOperation abstractOperation = ctx.allEntityTypes().accept(this);
        AssignedVariableOperation resultOperation = new AssignedVariableOperation(ctx.IDENTIFIER().getText(), abstractOperation).expectedType(abstractOperation.getExpectedType());
        assignedVariables.put(resultOperation.getVariableName(), resultOperation);
        return resultOperation;
    }

    @Override
    public AbstractOperation visitDestructuringAssignment(DestructuringAssignmentContext ctx) {
        if (ctx.EQ() != null) {
            throw new ExpressionParsingException("Invalid assignment operation: '='. Use ':=' instead");
        }
        AbstractOperation abstractOperation = ctx.function() != null ? ctx.function().accept(this) : ctx.vectorEntity().accept(this);
        abstractOperation.expectedType(Object[].class);

        int index = 0;
        StringBuilder vectorAssignedVariableName = new StringBuilder("[");
        for (Iterator<TerminalNode> iterator = ctx.vectorOfVariables().IDENTIFIER().iterator(); iterator.hasNext(); ) {
            TerminalNode terminalNode = iterator.next();
            String variableName = terminalNode.getText();
            vectorAssignedVariableName.append(variableName);
            if (iterator.hasNext()) {
                vectorAssignedVariableName.append(", ");
            }
            assignedVariables.put(variableName, new AssignedVectorOperation(variableName, abstractOperation, index++));
        }
        vectorAssignedVariableName.append("]");
        return new AssignedVariableOperation(vectorAssignedVariableName.toString(), abstractOperation);
    }

    @Override
    public AbstractOperation visitStringExpression(StringExpressionContext ctx) {
        if (nonNull(ctx.comparisonOperator().GT())) {
            return new GreaterOperation(ctx.stringEntity(0).accept(this), ctx.stringEntity(1).accept(this));
        } else if (nonNull(ctx.comparisonOperator().GE())) {
            return new GreaterOrEqualsOperation(ctx.stringEntity(0).accept(this), ctx.stringEntity(1).accept(this));
        } else if (nonNull(ctx.comparisonOperator().LT())) {
            return new LessOperation(ctx.stringEntity(0).accept(this), ctx.stringEntity(1).accept(this));
        } else if (nonNull(ctx.comparisonOperator().LE())) {
            return new LessOrEqualsOperation(ctx.stringEntity(0).accept(this), ctx.stringEntity(1).accept(this));
        } else if (nonNull(ctx.comparisonOperator().EQ())) {
            return new EqualsOperation(ctx.stringEntity(0).accept(this), ctx.stringEntity(1).accept(this));
        } else if (nonNull(ctx.comparisonOperator().NEQ())) {
            return new NotEqualsOperation(ctx.stringEntity(0).accept(this), ctx.stringEntity(1).accept(this));
        }
        throw new IllegalStateException(String.format("Operação não implementada: %s", ctx.getText()));
    }

    @Override
    public AbstractOperation visitLogicComparisonExpression(LogicComparisonExpressionContext ctx) {
        if (nonNull(ctx.comparisonOperator().GT())) {
            return new GreaterOperation(ctx.logicalExpression(0).accept(this), ctx.logicalExpression(1).accept(this));
        } else if (nonNull(ctx.comparisonOperator().GE())) {
            return new GreaterOrEqualsOperation(ctx.logicalExpression(0).accept(this), ctx.logicalExpression(1).accept(this));
        } else if (nonNull(ctx.comparisonOperator().LT())) {
            return new LessOperation(ctx.logicalExpression(0).accept(this), ctx.logicalExpression(1).accept(this));
        } else if (nonNull(ctx.comparisonOperator().LE())) {
            return new LessOrEqualsOperation(ctx.logicalExpression(0).accept(this), ctx.logicalExpression(1).accept(this));
        } else if (nonNull(ctx.comparisonOperator().EQ())) {
            return new EqualsOperation(ctx.logicalExpression(0).accept(this), ctx.logicalExpression(1).accept(this));
        } else if (nonNull(ctx.comparisonOperator().NEQ())) {
            return new NotEqualsOperation(ctx.logicalExpression(0).accept(this), ctx.logicalExpression(1).accept(this));
        }
        throw new IllegalStateException(String.format("Operação não implementada: %s", ctx.getText()));
    }

    @Override
    public AbstractOperation visitLogicExpression(LogicExpressionContext ctx) {
        if (nonNull(ctx.logicalOperator().AND())) {
            return new AndOperation(ctx.logicalExpression(0).accept(this), ctx.logicalExpression(1).accept(this));
        } else if (nonNull(ctx.logicalOperator().OR())) {
            return new OrOperation(ctx.logicalExpression(0).accept(this), ctx.logicalExpression(1).accept(this));
        } else if (nonNull(ctx.logicalOperator().XOR())) {
            return new XorOperation(ctx.logicalExpression(0).accept(this), ctx.logicalExpression(1).accept(this));
        } else if (nonNull(ctx.logicalOperator().XNOR())) {
            return new XnorOperation(ctx.logicalExpression(0).accept(this), ctx.logicalExpression(1).accept(this));
        } else if (nonNull(ctx.logicalOperator().NOR())) {
            return new NorOperation(ctx.logicalExpression(0).accept(this), ctx.logicalExpression(1).accept(this));
        } else if (nonNull(ctx.logicalOperator().NAND())) {
            return new NandOperation(ctx.logicalExpression(0).accept(this), ctx.logicalExpression(1).accept(this));
        }
        throw new IllegalStateException(String.format("Operation %s not implemented", ctx.getText()));
    }

    @Override
    public AbstractOperation visitDateExpression(DateExpressionContext ctx) {
        if (nonNull(ctx.comparisonOperator().GT())) {
            return new GreaterOperation(ctx.dateOperation(0).accept(this), ctx.dateOperation(1).accept(this));
        } else if (nonNull(ctx.comparisonOperator().GE())) {
            return new GreaterOrEqualsOperation(ctx.dateOperation(0).accept(this), ctx.dateOperation(1).accept(this));
        } else if (nonNull(ctx.comparisonOperator().LT())) {
            return new LessOperation(ctx.dateOperation(0).accept(this), ctx.dateOperation(1).accept(this));
        } else if (nonNull(ctx.comparisonOperator().LE())) {
            return new LessOrEqualsOperation(ctx.dateOperation(0).accept(this), ctx.dateOperation(1).accept(this));
        } else if (nonNull(ctx.comparisonOperator().EQ())) {
            return new EqualsOperation(ctx.dateOperation(0).accept(this), ctx.dateOperation(1).accept(this));
        } else if (nonNull(ctx.comparisonOperator().NEQ())) {
            return new NotEqualsOperation(ctx.dateOperation(0).accept(this), ctx.dateOperation(1).accept(this));
        }
        throw new IllegalStateException(String.format("Operation %s not implemented", ctx.getText()));
    }

    @Override
    public AbstractOperation visitNotExpression(NotExpressionContext ctx) {
        return new NegationOperation(ctx.logicalExpression().accept(this));
    }

    @Override
    public AbstractOperation visitComparisonMathExpression(ComparisonMathExpressionContext ctx) {
        if (nonNull(ctx.comparisonOperator().GT())) {
            return new GreaterOperation(ctx.mathExpression(0).accept(this), ctx.mathExpression(1).accept(this));
        } else if (nonNull(ctx.comparisonOperator().GE())) {
            return new GreaterOrEqualsOperation(ctx.mathExpression(0).accept(this), ctx.mathExpression(1).accept(this));
        } else if (nonNull(ctx.comparisonOperator().LT())) {
            return new LessOperation(ctx.mathExpression(0).accept(this), ctx.mathExpression(1).accept(this));
        } else if (nonNull(ctx.comparisonOperator().LE())) {
            return new LessOrEqualsOperation(ctx.mathExpression(0).accept(this), ctx.mathExpression(1).accept(this));
        } else if (nonNull(ctx.comparisonOperator().EQ())) {
            return new EqualsOperation(ctx.mathExpression(0).accept(this), ctx.mathExpression(1).accept(this));
        } else if (nonNull(ctx.comparisonOperator().NEQ())) {
            return new NotEqualsOperation(ctx.mathExpression(0).accept(this), ctx.mathExpression(1).accept(this));
        }
        throw new IllegalStateException(String.format("Operation %s not implemented", ctx.getText()));
    }

    @Override
    public AbstractOperation visitTimeExpression(TimeExpressionContext ctx) {
        if (nonNull(ctx.comparisonOperator().GT())) {
            return new GreaterOperation(ctx.timeOperation(0).accept(this), ctx.timeOperation(1).accept(this));
        } else if (nonNull(ctx.comparisonOperator().GE())) {
            return new GreaterOrEqualsOperation(ctx.timeOperation(0).accept(this), ctx.timeOperation(1).accept(this));
        } else if (nonNull(ctx.comparisonOperator().LT())) {
            return new LessOperation(ctx.timeOperation(0).accept(this), ctx.timeOperation(1).accept(this));
        } else if (nonNull(ctx.comparisonOperator().LE())) {
            return new LessOrEqualsOperation(ctx.timeOperation(0).accept(this), ctx.timeOperation(1).accept(this));
        } else if (nonNull(ctx.comparisonOperator().EQ())) {
            return new EqualsOperation(ctx.timeOperation(0).accept(this), ctx.timeOperation(1).accept(this));
        } else if (nonNull(ctx.comparisonOperator().NEQ())) {
            return new NotEqualsOperation(ctx.timeOperation(0).accept(this), ctx.timeOperation(1).accept(this));
        }
        throw new IllegalStateException(String.format("Operation %s not implemented", ctx.getText()));
    }

    @Override
    public AbstractOperation visitLogicalParenthesis(LogicalParenthesisContext ctx) {
        AbstractOperation operation = ctx.logicalExpression().accept(this);
        operation.applyingParenthesis();
        return operation;
    }

    @Override
    public AbstractOperation visitDateParenthesis(DateParenthesisContext ctx) {
        AbstractOperation operation = ctx.dateOperation().accept(this);
        operation.applyingParenthesis();
        return operation;
    }

    @Override
    public AbstractOperation visitDateFunction(DateFunctionContext ctx) {
        AbstractOperation leftOperand = ctx.dateEntity().accept(this);
        int count = ctx.DATE_OPERATIONS().size();

        for (int i = 0; i < count; i++) {
            String text = "'" + ctx.DATE_OPERATIONS(i).getSymbol().getText() + "'";
            if (VOCABULARY.getDisplayName(PLUS_DAYS).equals(text)) {
                leftOperand = new DateAdditionOperation(leftOperand, ctx.mathExpression(i).accept(this), DateElementEnum.DAY);
            } else if (VOCABULARY.getDisplayName(PLUS_MONTHS).equals(text)) {
                leftOperand = new DateAdditionOperation(leftOperand, ctx.mathExpression(i).accept(this), DateElementEnum.MONTH);
            } else if (VOCABULARY.getDisplayName(PLUS_YEARS).equals(text)) {
                leftOperand = new DateAdditionOperation(leftOperand, ctx.mathExpression(i).accept(this), DateElementEnum.YEAR);
            } else if (VOCABULARY.getDisplayName(MINUS_DAYS).equals(text)) {
                leftOperand = new DateSubtractionOperation(leftOperand, ctx.mathExpression(i).accept(this), DateElementEnum.DAY);
            } else if (VOCABULARY.getDisplayName(ExpressionEvaluatorLexer.MINUS_MONTHS).equals(text)) {
                leftOperand = new DateSubtractionOperation(leftOperand, ctx.mathExpression(i).accept(this), DateElementEnum.MONTH);
            } else if (VOCABULARY.getDisplayName(ExpressionEvaluatorLexer.MINUS_YEARS).equals(text)) {
                leftOperand = new DateSubtractionOperation(leftOperand, ctx.mathExpression(i).accept(this), DateElementEnum.YEAR);
            } else if (VOCABULARY.getDisplayName(ExpressionEvaluatorLexer.SET_DAYS).equals(text)) {
                leftOperand = new DateSetOperation(leftOperand, ctx.mathExpression(i).accept(this), DateElementEnum.DAY);
            } else if (VOCABULARY.getDisplayName(ExpressionEvaluatorLexer.SET_MONTHS).equals(text)) {
                leftOperand = new DateSetOperation(leftOperand, ctx.mathExpression(i).accept(this), DateElementEnum.MONTH);
            } else if (VOCABULARY.getDisplayName(ExpressionEvaluatorLexer.SET_YEARS).equals(text)) {
                leftOperand = new DateSetOperation(leftOperand, ctx.mathExpression(i).accept(this), DateElementEnum.YEAR);
            } else {
                throw new IllegalStateException(String.format("Operation %s not implemented", ctx.DATE_OPERATIONS(i).getText()));
            }
        }
        return leftOperand;
    }

    @Override
    public AbstractOperation visitTimeParenthesis(TimeParenthesisContext ctx) {
        AbstractOperation operation = ctx.timeOperation().accept(this);
        operation.applyingParenthesis();
        return operation;
    }

    @Override
    public AbstractOperation visitTimeFunction(TimeFunctionContext ctx) {
        AbstractOperation leftOperand = ctx.timeEntity().accept(this);
        int count = ctx.TIME_OPERATIONS().size();

        for (int i = 0; i < count; i++) {
            String text = "'" + ctx.TIME_OPERATIONS(i).getSymbol().getText() + "'";
            if (VOCABULARY.getDisplayName(ExpressionEvaluatorLexer.PLUS_HOURS).equals(text)) {
                leftOperand = new TimeAdditionOperation(leftOperand, ctx.mathExpression(i).accept(this), DateElementEnum.HOUR);
            } else if (VOCABULARY.getDisplayName(ExpressionEvaluatorLexer.PLUS_MINUTES).equals(text)) {
                leftOperand = new TimeAdditionOperation(leftOperand, ctx.mathExpression(i).accept(this), DateElementEnum.MINUTE);
            } else if (VOCABULARY.getDisplayName(ExpressionEvaluatorLexer.PLUS_SECONDS).equals(text)) {
                leftOperand = new TimeAdditionOperation(leftOperand, ctx.mathExpression(i).accept(this), DateElementEnum.SECOND);
            } else if (VOCABULARY.getDisplayName(ExpressionEvaluatorLexer.MINUS_HOURS).equals(text)) {
                leftOperand = new TimeSubtractionOperation(leftOperand, ctx.mathExpression(i).accept(this), DateElementEnum.HOUR);
            } else if (VOCABULARY.getDisplayName(ExpressionEvaluatorLexer.MINUS_MINUTES).equals(text)) {
                leftOperand = new TimeSubtractionOperation(leftOperand, ctx.mathExpression(i).accept(this), DateElementEnum.MINUTE);
            } else if (VOCABULARY.getDisplayName(ExpressionEvaluatorLexer.MINUS_SECONDS).equals(text)) {
                leftOperand = new TimeSubtractionOperation(leftOperand, ctx.mathExpression(i).accept(this), DateElementEnum.SECOND);
            } else if (VOCABULARY.getDisplayName(ExpressionEvaluatorLexer.SET_HOURS).equals(text)) {
                leftOperand = new TimeSetOperation(leftOperand, ctx.mathExpression(i).accept(this), DateElementEnum.HOUR);
            } else if (VOCABULARY.getDisplayName(ExpressionEvaluatorLexer.SET_MINUTES).equals(text)) {
                leftOperand = new TimeSetOperation(leftOperand, ctx.mathExpression(i).accept(this), DateElementEnum.MINUTE);
            } else if (VOCABULARY.getDisplayName(ExpressionEvaluatorLexer.SET_SECONDS).equals(text)) {
                leftOperand = new TimeSetOperation(leftOperand, ctx.mathExpression(i).accept(this), DateElementEnum.SECOND);
            } else {
                throw new IllegalStateException(String.format("Operation %s not implemented", ctx.TIME_OPERATIONS(i).getText()));
            }
        }
        return leftOperand;
    }

    @Override
    public AbstractOperation visitDateTimeParenthesis(DateTimeParenthesisContext ctx) {
        AbstractOperation operation = ctx.dateTimeOperation().accept(this);
        operation.applyingParenthesis();
        return operation;
    }

    @Override
    public AbstractOperation visitDateTimeFunction(DateTimeFunctionContext ctx) {
        AbstractOperation leftOperand = ctx.dateTimeEntity().accept(this);
        int count = ctx.DATE_OPERATIONS().size() + ctx.TIME_OPERATIONS().size();

        for (int i = 0; i < count; i++) {
            String text = "'" + ctx.getChild(1 + (2 * i)).getText() + "'";
            if (VOCABULARY.getDisplayName(PLUS_DAYS).equals(text)) {
                leftOperand = new DateTimeAdditionOperation(leftOperand, ctx.mathExpression(i).accept(this), DateElementEnum.DAY);
            } else if (VOCABULARY.getDisplayName(PLUS_MONTHS).equals(text)) {
                leftOperand = new DateTimeAdditionOperation(leftOperand, ctx.mathExpression(i).accept(this), DateElementEnum.MONTH);
            } else if (VOCABULARY.getDisplayName(PLUS_YEARS).equals(text)) {
                leftOperand = new DateTimeAdditionOperation(leftOperand, ctx.mathExpression(i).accept(this), DateElementEnum.YEAR);
            } else if (VOCABULARY.getDisplayName(MINUS_DAYS).equals(text)) {
                leftOperand = new DateTimeSubtractionOperation(leftOperand, ctx.mathExpression(i).accept(this), DateElementEnum.DAY);
            } else if (VOCABULARY.getDisplayName(ExpressionEvaluatorLexer.MINUS_MONTHS).equals(text)) {
                leftOperand = new DateTimeSubtractionOperation(leftOperand, ctx.mathExpression(i).accept(this), DateElementEnum.MONTH);
            } else if (VOCABULARY.getDisplayName(ExpressionEvaluatorLexer.MINUS_YEARS).equals(text)) {
                leftOperand = new DateTimeSubtractionOperation(leftOperand, ctx.mathExpression(i).accept(this), DateElementEnum.YEAR);
            } else if (VOCABULARY.getDisplayName(ExpressionEvaluatorLexer.SET_DAYS).equals(text)) {
                leftOperand = new DateTimeSetOperation(leftOperand, ctx.mathExpression(i).accept(this), DateElementEnum.DAY);
            } else if (VOCABULARY.getDisplayName(ExpressionEvaluatorLexer.SET_MONTHS).equals(text)) {
                leftOperand = new DateTimeSetOperation(leftOperand, ctx.mathExpression(i).accept(this), DateElementEnum.MONTH);
            } else if (VOCABULARY.getDisplayName(ExpressionEvaluatorLexer.SET_YEARS).equals(text)) {
                leftOperand = new DateTimeSetOperation(leftOperand, ctx.mathExpression(i).accept(this), DateElementEnum.YEAR);
            } else if (VOCABULARY.getDisplayName(ExpressionEvaluatorLexer.PLUS_HOURS).equals(text)) {
                leftOperand = new DateTimeAdditionOperation(leftOperand, ctx.mathExpression(i).accept(this), DateElementEnum.HOUR);
            } else if (VOCABULARY.getDisplayName(ExpressionEvaluatorLexer.PLUS_MINUTES).equals(text)) {
                leftOperand = new DateTimeAdditionOperation(leftOperand, ctx.mathExpression(i).accept(this), DateElementEnum.MINUTE);
            } else if (VOCABULARY.getDisplayName(ExpressionEvaluatorLexer.PLUS_SECONDS).equals(text)) {
                leftOperand = new DateTimeAdditionOperation(leftOperand, ctx.mathExpression(i).accept(this), DateElementEnum.SECOND);
            } else if (VOCABULARY.getDisplayName(ExpressionEvaluatorLexer.MINUS_HOURS).equals(text)) {
                leftOperand = new DateTimeSubtractionOperation(leftOperand, ctx.mathExpression(i).accept(this), DateElementEnum.HOUR);
            } else if (VOCABULARY.getDisplayName(ExpressionEvaluatorLexer.MINUS_MINUTES).equals(text)) {
                leftOperand = new DateTimeSubtractionOperation(leftOperand, ctx.mathExpression(i).accept(this), DateElementEnum.MINUTE);
            } else if (VOCABULARY.getDisplayName(ExpressionEvaluatorLexer.MINUS_SECONDS).equals(text)) {
                leftOperand = new DateTimeSubtractionOperation(leftOperand, ctx.mathExpression(i).accept(this), DateElementEnum.SECOND);
            } else if (VOCABULARY.getDisplayName(ExpressionEvaluatorLexer.SET_HOURS).equals(text)) {
                leftOperand = new DateTimeSetOperation(leftOperand, ctx.mathExpression(i).accept(this), DateElementEnum.HOUR);
            } else if (VOCABULARY.getDisplayName(ExpressionEvaluatorLexer.SET_MINUTES).equals(text)) {
                leftOperand = new DateTimeSetOperation(leftOperand, ctx.mathExpression(i).accept(this), DateElementEnum.MINUTE);
            } else if (VOCABULARY.getDisplayName(ExpressionEvaluatorLexer.SET_SECONDS).equals(text)) {
                leftOperand = new DateTimeSetOperation(leftOperand, ctx.mathExpression(i).accept(this), DateElementEnum.SECOND);
            } else {
                throw new IllegalStateException(String.format("Operation %s not implemented", ctx.DATE_OPERATIONS(i).getText()));
            }
        }
        return leftOperand;
    }

    @Override
    public AbstractOperation visitFunction(FunctionContext ctx) {
        List<AllEntityTypesContext> entities = ctx.allEntityTypes();
        AbstractOperation[] parameters = new AbstractOperation[entities.size()];
        for (int i = 0, entitiesSize = entities.size(); i < entitiesSize; i++) {
            parameters[i] = entities.get(i).accept(this);
        }
        // If CACHE_FUNCTION_PREFIX is present, so cache must be disabled
        return new FunctionOperation(ctx.IDENTIFIER().getText(), parameters, ctx.CACHE_FUNCTION_PREFIX() == null);
    }

    @Override
    public AbstractOperation visitLogicalConstant(LogicalConstantContext ctx) {
        return new BooleanConstantValueOperation(ctx.getText());
    }

    @Override
    public AbstractOperation visitLogicalDecisionExpression(LogicalDecisionExpressionContext ctx) {
        List<AbstractOperation> operations = new ArrayList<>();
        List<LogicalExpressionContext> logicalExpressions = ctx.logicalExpression();

        for (int i = 0; i < logicalExpressions.size() - 1; i += 2) {
            operations.add(logicalExpressions.get(i).accept(this));
            operations.add(logicalExpressions.get(i + 1).accept(this));
        }
        operations.add(logicalExpressions.get(logicalExpressions.size() - 1).accept(this));
        return new DecisionOperation(true, operations.toArray(AbstractOperation[]::new)).expectedType(Boolean.class);
    }

    @Override
    public AbstractOperation visitLogicalFunctionDecisionExpression(LogicalFunctionDecisionExpressionContext ctx) {
        List<AbstractOperation> operations = new ArrayList<>();
        List<LogicalExpressionContext> logicalExpressions = ctx.logicalExpression();

        for (int i = 0; i < logicalExpressions.size() - 1; i += 2) {
            operations.add(logicalExpressions.get(i).accept(this));
            operations.add(logicalExpressions.get(i + 1).accept(this));
        }
        operations.add(logicalExpressions.get(logicalExpressions.size() - 1).accept(this));
        return new DecisionOperation(false, operations.toArray(AbstractOperation[]::new)).expectedType(Boolean.class);
    }

    @Override
    public AbstractOperation visitLogicalFunctionResult(LogicalFunctionResultContext ctx) {
        return ctx.function().accept(this).expectedType(Boolean.class);
    }

    @Override
    public AbstractOperation visitLogicalVariable(LogicalVariableContext ctx) {
        return createNewUserVariable(ctx).expectedType(Boolean.class);
    }

    @Override
    public AbstractOperation visitDateTimeExpression(DateTimeExpressionContext ctx) {
        if (nonNull(ctx.comparisonOperator().GT())) {
            return new GreaterOperation(ctx.dateTimeOperation(0).accept(this), ctx.dateTimeOperation(1).accept(this));
        } else if (nonNull(ctx.comparisonOperator().GE())) {
            return new GreaterOrEqualsOperation(ctx.dateTimeOperation(0).accept(this), ctx.dateTimeOperation(1).accept(this));
        } else if (nonNull(ctx.comparisonOperator().LT())) {
            return new LessOperation(ctx.dateTimeOperation(0).accept(this), ctx.dateTimeOperation(1).accept(this));
        } else if (nonNull(ctx.comparisonOperator().LE())) {
            return new LessOrEqualsOperation(ctx.dateTimeOperation(0).accept(this), ctx.dateTimeOperation(1).accept(this));
        } else if (nonNull(ctx.comparisonOperator().EQ())) {
            return new EqualsOperation(ctx.dateTimeOperation(0).accept(this), ctx.dateTimeOperation(1).accept(this));
        } else if (nonNull(ctx.comparisonOperator().NEQ())) {
            return new NotEqualsOperation(ctx.dateTimeOperation(0).accept(this), ctx.dateTimeOperation(1).accept(this));
        }
        throw new IllegalStateException(String.format("Operation %s not implemented", ctx.getText()));
    }

    @Override
    public AbstractOperation visitStringDecisionExpression(StringDecisionExpressionContext ctx) {
        List<AbstractOperation> operations = new ArrayList<>();
        List<LogicalExpressionContext> logicalExpressions = ctx.logicalExpression();
        List<StringEntityContext> stringEntities = ctx.stringEntity();

        for (int i = 0; i < logicalExpressions.size(); i++) {
            operations.add(logicalExpressions.get(i).accept(this));
            operations.add(stringEntities.get(i).accept(this));
        }
        operations.add(stringEntities.get(stringEntities.size() - 1).accept(this));
        return new DecisionOperation(true, operations.toArray(AbstractOperation[]::new)).expectedType(String.class);
    }

    @Override
    public AbstractOperation visitStringFunctionDecisionExpression(StringFunctionDecisionExpressionContext ctx) {
        List<AbstractOperation> operations = new ArrayList<>();
        List<LogicalExpressionContext> logicalExpressions = ctx.logicalExpression();
        List<StringEntityContext> stringEntities = ctx.stringEntity();

        for (int i = 0; i < logicalExpressions.size(); i++) {
            operations.add(logicalExpressions.get(i).accept(this));
            operations.add(stringEntities.get(i).accept(this));
        }
        operations.add(stringEntities.get(stringEntities.size() - 1).accept(this));
        return new DecisionOperation(false, operations.toArray(AbstractOperation[]::new)).expectedType(String.class);
    }

    @Override
    public AbstractOperation visitStringConstant(StringConstantContext ctx) {
        return new StringConstantValueOperation(ctx.getText().substring(1, ctx.getText().length() - 1));
    }

    @Override
    public AbstractOperation visitStringFunctionResult(StringFunctionResultContext ctx) {
        return ctx.function().accept(this).expectedType(String.class);
    }

    @Override
    public AbstractOperation visitStringVariable(StringVariableContext ctx) {
        return createNewUserVariable(ctx).expectedType(String.class);
    }

    @Override
    public AbstractOperation visitSequenceExpression(SequenceExpressionContext ctx) {
        if (sequenceVariableStack == null) {
            sequenceVariableStack = new Stack<>();
        }
        return super.visitSequenceExpression(ctx);
    }

    @Override
    public AbstractOperation visitModulusExpression(ModulusExpressionContext ctx) {
        return new ModulusOperation(ctx.mathExpression().accept(this));
    }

    @Override
    public AbstractOperation visitMathParenthesis(MathParenthesisContext ctx) {
        AbstractOperation operation = ctx.mathExpression().accept(this);
        operation.applyingParenthesis();
        return operation;
    }

    @Override
    public AbstractOperation visitSquareRootExpression(SquareRootExpressionContext ctx) {
        return new SquareRootOperation(ctx.mathExpression().accept(this));
    }

    @Override
    public AbstractOperation visitRootExpression(RootExpressionContext ctx) {
        return new RootOperation(ctx.mathExpression(1).accept(this), ctx.mathExpression(0).accept(this));
    }

    @Override
    public AbstractOperation visitMultiplicationExpression(MultiplicationExpressionContext ctx) {
        if (ctx.MULT() != null) {
            return new MultiplicationOperation(ctx.mathExpression(0).accept(this), ctx.mathExpression(1).accept(this));
        } else if (nonNull(ctx.DIV())) {
            return new DivisionOperation(ctx.mathExpression(0).accept(this), ctx.mathExpression(1).accept(this));
        } else if (nonNull(ctx.PERCENT())) {
            return new ModuloOperation(ctx.mathExpression(0).accept(this), ctx.mathExpression(1).accept(this));
        }
        throw new IllegalStateException(String.format("Operation %s not implemented", ctx.getText()));
    }

    @Override
    public AbstractOperation visitFactorialExpression(FactorialExpressionContext ctx) {
        return new FactorialOperation(ctx.mathExpression().accept(this));
    }

    @Override
    public AbstractOperation visitNegateMathParenthesis(NegateMathParenthesisContext ctx) {
        AbstractOperation operation = ctx.mathExpression().accept(this);
        operation.applyingParenthesis();
        return new NegativeParenthesisOperation(operation);
    }

    @Override
    public AbstractOperation visitPercentExpression(PercentExpressionContext ctx) {
        return new PercentualOperation(ctx.mathExpression().accept(this));
    }

    @Override
    public AbstractOperation visitSumExpression(SumExpressionContext ctx) {
        if (ctx.PLUS() != null) {
            return new AdditionOperation(ctx.mathExpression(0).accept(this), ctx.mathExpression(1).accept(this));
        } else if (nonNull(ctx.MINUS())) {
            return new SubtractionOperation(ctx.mathExpression(0).accept(this), ctx.mathExpression(1).accept(this));
        }
        throw new IllegalStateException(String.format("Operation %s not implemented", ctx.getText()));
    }

    @Override
    public AbstractOperation visitExponentiationExpression(ExponentiationExpressionContext ctx) {
        return new ExponentialOperation(ctx.mathExpression(0).accept(this), ctx.mathExpression(1).accept(this));
    }

    @Override
    public AbstractOperation visitFixedLogarithm(FixedLogarithmContext ctx) {
        if (nonNull(ctx.BINARY_LOGARITHM())) {
            return new PreciseBinaryLogarithmOperation(ctx.mathExpression().accept(this));
        } else if (nonNull(ctx.COMMOM_LOGARITHM())) {
            return new PreciseCommonLogarithmOperation(ctx.mathExpression().accept(this));
        } else if (nonNull(ctx.NATURAL_LOGARITHM())) {
            return new PreciseNaturalLogarithmOperation(ctx.mathExpression().accept(this));
        }
        throw new IllegalStateException(String.format("Operation %s not implemented", ctx.getText()));
    }

    @Override
    public AbstractOperation visitVariableLogarithm(VariableLogarithmContext ctx) {
        return new PreciseLogarithmOperation(ctx.mathExpression(0).accept(this), ctx.mathExpression(1).accept(this));
    }

    @Override
    public AbstractOperation visitRoundingFunction(RoundingFunctionContext ctx) {
        if (ctx.R_HALF_EVEN() != null) {
            return new NumberRoundingOperation(ctx.mathExpression(0).accept(this), ctx.mathExpression(1).accept(this), RoundingEnum.HALF_EVEN);
        } else if (ctx.R_DOWN() != null) {
            return new NumberRoundingOperation(ctx.mathExpression(0).accept(this), ctx.mathExpression(1).accept(this), RoundingEnum.DOWN);
        } else if (ctx.R_CEILING() != null) {
            return new NumberRoundingOperation(ctx.mathExpression(0).accept(this), ctx.mathExpression(1).accept(this), RoundingEnum.CEILING);
        } else if (ctx.R_FLOOR() != null) {
            return new NumberRoundingOperation(ctx.mathExpression(0).accept(this), ctx.mathExpression(1).accept(this), RoundingEnum.FLOOR);
        } else if (ctx.R_HALF_UP() != null) {
            return new NumberRoundingOperation(ctx.mathExpression(0).accept(this), ctx.mathExpression(1).accept(this), RoundingEnum.HALF_UP);
        } else if (ctx.R_UP() != null) {
            return new NumberRoundingOperation(ctx.mathExpression(0).accept(this), ctx.mathExpression(1).accept(this), RoundingEnum.UP);
        } else if (ctx.R_HALF_DOWN() != null) {
            return new NumberRoundingOperation(ctx.mathExpression(0).accept(this), ctx.mathExpression(1).accept(this), RoundingEnum.HALF_DOWN);
        } else if (ctx.R_UNNECESSARY() != null) {
            return new NumberRoundingOperation(ctx.mathExpression(0).accept(this), ctx.mathExpression(1).accept(this), RoundingEnum.UNNECESSARY);
        } else {
            throw new IllegalStateException("No rounding method defined for operation " + ctx.getText());
        }
    }

    @Override
    public AbstractOperation visitSequenceFunction(SequenceFunctionContext ctx) {
        if (sequenceVariableStack == null) {
            sequenceVariableStack = new Stack<>();
        }
        sequenceVariableStack.add(new ArrayList<>());

        AbstractOperation input = ctx.vectorEntity().accept(this);
        AbstractOperation mathExpression = ctx.mathExpression().accept(this);

        List<SequenceVariableValueOperation> sequenceVariableContainer = sequenceVariableStack.pop();
        SequenceVariableValueOperation sequenceVariable = sequenceVariableContainer.isEmpty() ? null : sequenceVariableContainer.get(0);
        if (ctx.SUMMATION() != null) {
            return new SummationOperation(input, mathExpression, sequenceVariable);
        } else if (ctx.PRODUCT_SEQUENCE() != null) {
            return new ProductOfSequenceOperation(input, mathExpression, sequenceVariable);
        }
        throw new IllegalStateException(String.format("Operation %s not implemented", ctx.getText()));
    }


    @Override
    public AbstractOperation visitMathDecisionExpression(MathDecisionExpressionContext ctx) {
        List<AbstractOperation> operations = new ArrayList<>();
        List<LogicalExpressionContext> logicalExpressions = ctx.logicalExpression();
        List<MathExpressionContext> mathExpressions = ctx.mathExpression();

        for (int i = 0; i < logicalExpressions.size(); i++) {
            operations.add(logicalExpressions.get(i).accept(this));
            operations.add(mathExpressions.get(i).accept(this));
        }
        operations.add(mathExpressions.get(mathExpressions.size() - 1).accept(this));
        return new DecisionOperation(true, operations.toArray(AbstractOperation[]::new)).expectedType(BigDecimal.class);
    }

    @Override
    public AbstractOperation visitMathFunctionDecisionExpression(MathFunctionDecisionExpressionContext ctx) {
        List<AbstractOperation> operations = new ArrayList<>();
        List<LogicalExpressionContext> logicalExpressions = ctx.logicalExpression();
        List<MathExpressionContext> mathExpressions = ctx.mathExpression();

        for (int i = 0; i < logicalExpressions.size(); i++) {
            operations.add(logicalExpressions.get(i).accept(this));
            operations.add(mathExpressions.get(i).accept(this));
        }
        operations.add(mathExpressions.get(mathExpressions.size() - 1).accept(this));
        return new DecisionOperation(false, operations.toArray(AbstractOperation[]::new)).expectedType(BigDecimal.class);
    }

    @Override
    public AbstractOperation visitEulerConstant(EulerConstantContext ctx) {
        return new EulerNumberConstantValueOperation();
    }

    @Override
    public AbstractOperation visitPiConstant(PiConstantContext ctx) {
        return new PiNumberConstantValueOperation();
    }

    @Override
    public AbstractOperation visitSummationVariable(SummationVariableContext ctx) {
        if (sequenceVariableStack.peek().isEmpty()) {
            SequenceVariableValueOperation sequenceVariable = new SequenceVariableValueOperation("S");
            sequenceVariableStack.peek().add(sequenceVariable);
            return sequenceVariable;
        }
        return sequenceVariableStack.peek().get(0).expectedType(BigDecimal.class);
    }

    @Override
    public AbstractOperation visitProductSequenceVariable(ProductSequenceVariableContext ctx) {
        if (sequenceVariableStack.peek().isEmpty()) {
            SequenceVariableValueOperation sequenceVariable = new SequenceVariableValueOperation("P");
            sequenceVariableStack.peek().add(sequenceVariable);
            return sequenceVariable;
        }
        return sequenceVariableStack.peek().get(0).expectedType(BigDecimal.class);
    }

    @Override
    public AbstractOperation visitNumericConstant(NumericConstantContext ctx) {
        return new NumberConstantValueOperation(ctx.getText());
    }

    @Override
    public AbstractOperation visitNumericFunctionResult(NumericFunctionResultContext ctx) {
        return ctx.function().accept(this).expectedType(BigDecimal.class);
    }

    @Override
    public AbstractOperation visitNumericVariable(NumericVariableContext ctx) {
        if (nonNull(ctx.IDENTIFIER())) {
            return createNewUserVariable(ctx).expectedType(BigDecimal.class);
        } else if (nonNull(ctx.NEGATIVE_IDENTIFIER())) {
            return new NegativeParenthesisOperation(createNewUserVariable(ctx,
                    name -> new VariableValueOperation(name).expectedType(BigDecimal.class),
                    () -> ctx.getText().substring(1)));
        }
        throw new IllegalStateException("Invalid numeric operation: " + ctx.getText());
    }

    @Override
    public AbstractOperation visitDateDecisionExpression(DateDecisionExpressionContext ctx) {
        List<AbstractOperation> operations = new ArrayList<>();
        List<LogicalExpressionContext> logicalExpressions = ctx.logicalExpression();
        List<DateOperationContext> dateOperations = ctx.dateOperation();

        for (int i = 0; i < logicalExpressions.size(); i++) {
            operations.add(logicalExpressions.get(i).accept(this));
            operations.add(dateOperations.get(i).accept(this));
        }
        operations.add(dateOperations.get(dateOperations.size() - 1).accept(this));
        return new DecisionOperation(true, operations.toArray(AbstractOperation[]::new)).expectedType(LocalDate.class);
    }

    @Override
    public AbstractOperation visitDateFunctionDecisionExpression(DateFunctionDecisionExpressionContext ctx) {
        List<AbstractOperation> operations = new ArrayList<>();
        List<LogicalExpressionContext> logicalExpressions = ctx.logicalExpression();
        List<DateOperationContext> dateOperations = ctx.dateOperation();

        for (int i = 0; i < logicalExpressions.size(); i++) {
            operations.add(logicalExpressions.get(i).accept(this));
            operations.add(dateOperations.get(i).accept(this));
        }
        operations.add(dateOperations.get(dateOperations.size() - 1).accept(this));
        return new DecisionOperation(false, operations.toArray(AbstractOperation[]::new)).expectedType(LocalDate.class);
    }

    @Override
    public AbstractOperation visitDateConstant(DateConstantContext ctx) {
        return new DateConstantValueOperation(ctx.getText());
    }

    @Override
    public AbstractOperation visitDateCurrentValue(DateCurrentValueContext ctx) {
        return new InternallyMutableValueOperation("currDate",
                (op, context) -> LocalDate.from(context.currentDateTime().get())).expectedType(LocalDate.class);
    }

    @Override
    public AbstractOperation visitDateVariable(DateVariableContext ctx) {
        return createNewUserVariable(ctx).expectedType(LocalDate.class);
    }

    @Override
    public AbstractOperation visitDateFunctionResult(DateFunctionResultContext ctx) {
        return ctx.function().accept(this).expectedType(LocalDate.class);
    }

    @Override
    public AbstractOperation visitTimeDecisionExpression(TimeDecisionExpressionContext ctx) {
        List<AbstractOperation> operations = new ArrayList<>();
        List<LogicalExpressionContext> logicalExpressions = ctx.logicalExpression();
        List<TimeOperationContext> timeOperations = ctx.timeOperation();

        for (int i = 0; i < logicalExpressions.size(); i++) {
            operations.add(logicalExpressions.get(i).accept(this));
            operations.add(timeOperations.get(i).accept(this));
        }
        operations.add(timeOperations.get(timeOperations.size() - 1).accept(this));
        return new DecisionOperation(true, operations.toArray(AbstractOperation[]::new)).expectedType(LocalTime.class);
    }

    @Override
    public AbstractOperation visitTimeFunctionDecisionExpression(TimeFunctionDecisionExpressionContext ctx) {
        List<AbstractOperation> operations = new ArrayList<>();
        List<LogicalExpressionContext> logicalExpressions = ctx.logicalExpression();
        List<TimeOperationContext> timeOperations = ctx.timeOperation();

        for (int i = 0; i < logicalExpressions.size(); i++) {
            operations.add(logicalExpressions.get(i).accept(this));
            operations.add(timeOperations.get(i).accept(this));
        }
        operations.add(timeOperations.get(timeOperations.size() - 1).accept(this));
        return new DecisionOperation(false, operations.toArray(AbstractOperation[]::new)).expectedType(LocalTime.class);
    }

    @Override
    public AbstractOperation visitTimeConstant(TimeConstantContext ctx) {
        return new TimeConstantValueOperation(ctx.getText());
    }

    @Override
    public AbstractOperation visitTimeCurrentValue(TimeCurrentValueContext ctx) {
        return new InternallyMutableValueOperation("currTime",
                (op, context) -> LocalTime.from(context.currentDateTime().get())).expectedType(LocalTime.class);
    }

    @Override
    public AbstractOperation visitTimeVariable(TimeVariableContext ctx) {
        return createNewUserVariable(ctx).expectedType(LocalTime.class);
    }

    @Override
    public AbstractOperation visitTimeFunctionResult(TimeFunctionResultContext ctx) {
        return ctx.function().accept(this).expectedType(LocalTime.class);
    }

    @Override
    public AbstractOperation visitDateTimeDecisionExpression(DateTimeDecisionExpressionContext ctx) {
        List<AbstractOperation> operations = new ArrayList<>();
        List<LogicalExpressionContext> logicalExpressions = ctx.logicalExpression();
        List<DateTimeOperationContext> dateTimeOperations = ctx.dateTimeOperation();

        for (int i = 0; i < logicalExpressions.size(); i++) {
            operations.add(logicalExpressions.get(i).accept(this));
            operations.add(dateTimeOperations.get(i).accept(this));
        }
        operations.add(dateTimeOperations.get(dateTimeOperations.size() - 1).accept(this));
        return new DecisionOperation(true, operations.toArray(AbstractOperation[]::new)).expectedType(ZonedDateTime.class);
    }

    @Override
    public AbstractOperation visitDateTimeFunctionDecisionExpression(DateTimeFunctionDecisionExpressionContext ctx) {
        List<AbstractOperation> operations = new ArrayList<>();
        List<LogicalExpressionContext> logicalExpressions = ctx.logicalExpression();
        List<DateTimeOperationContext> dateTimeOperations = ctx.dateTimeOperation();

        for (int i = 0; i < logicalExpressions.size(); i++) {
            operations.add(logicalExpressions.get(i).accept(this));
            operations.add(dateTimeOperations.get(i).accept(this));
        }
        operations.add(dateTimeOperations.get(dateTimeOperations.size() - 1).accept(this));
        return new DecisionOperation(false, operations.toArray(AbstractOperation[]::new)).expectedType(ZonedDateTime.class);
    }

    @Override
    public AbstractOperation visitDateTimeConstant(DateTimeConstantContext ctx) {
        String offset = ctx.TIME_OFFSET() != null ? ctx.TIME_OFFSET().getText() : null;
        return new DateTimeConstantValueOperation(ctx.getText(), offset);
    }

    @Override
    public AbstractOperation visitDateTimeCurrentValue(DateTimeCurrentValueContext ctx) {
        return new InternallyMutableValueOperation("currDateTime",
                (op, context) -> context.currentDateTime().get()).expectedType(ZonedDateTime.class);
    }

    @Override
    public AbstractOperation visitDateTimeVariable(DateTimeVariableContext ctx) {
        return createNewUserVariable(ctx).expectedType(ZonedDateTime.class);
    }

    @Override
    public AbstractOperation visitDateTimeFunctionResult(DateTimeFunctionResultContext ctx) {
        return ctx.function().accept(this).expectedType(ZonedDateTime.class);
    }

    @Override
    public AbstractOperation visitDegreeExpression(DegreeExpressionContext ctx) {
        return new DegreeOperation(ctx.mathExpression().accept(this));
    }

    @Override
    public AbstractOperation visitVectorOfEntities(VectorOfEntitiesContext ctx) {
        AbstractOperation[] operations = new AbstractOperation[ctx.allEntityTypes().size()];
        for (int i = 0, size = ctx.allEntityTypes().size(); i < size; i++) {
            operations[i] = ctx.allEntityTypes().get(i).accept(this);
        }
        return new VectorValueOperation(ctx.getText(), operations).expectedType(Object[].class);
    }

    protected AbstractOperation createNewUserVariable(ParserRuleContext context) {
        return createNewUserVariable(context, VariableValueOperation::new, null);
    }

    protected AbstractOperation createNewUserVariable(
            ParserRuleContext context, Function<String, AbstractVariableValueOperation> supplier,
            Supplier<String> nameSupplier) {
        String name;
        if (nameSupplier != null) {
            name = nameSupplier.get();
        } else {
            if (context.getChildCount() == 2) {
                name = context.getChild(1).getText();
            } else {
                name = context.getText();
            }
        }

        boolean containsAssignedVariable = assignedVariables.containsKey(name);
        if (containsAssignedVariable && userVariables.containsKey(name)) {
            throw new ExpressionParsingException(
                    String.format("Duplicate variable name [%s] found between assigned and user variables", name));
        }

        if (containsAssignedVariable) {
            AssignedVariableOperation valueOperation = assignedVariables.get(name);
            if (valueOperation == null) {
                throw new ExpressionParsingException(String.format(
                        "Assigned variable [%s] not declared before requiring operation [%s]", name, context.getParent().getText()));
            }
            return valueOperation;
        } else {
            if (!userVariables.containsKey(name)) {
                userVariables.put(name, supplier.apply(name));
            }
            return userVariables.get(name);
        }
    }

}
