// Generated from com/runestone/expeval/internal/grammar/ExpressionEvaluator.g4 by ANTLR 4.13.1
package com.runestone.expeval.internal.grammar;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link ExpressionEvaluatorParser}.
 */
public interface ExpressionEvaluatorListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by the {@code mathInput}
	 * labeled alternative in {@link ExpressionEvaluatorParser#mathStart}.
	 * @param ctx the parse tree
	 */
	void enterMathInput(ExpressionEvaluatorParser.MathInputContext ctx);
	/**
	 * Exit a parse tree produced by the {@code mathInput}
	 * labeled alternative in {@link ExpressionEvaluatorParser#mathStart}.
	 * @param ctx the parse tree
	 */
	void exitMathInput(ExpressionEvaluatorParser.MathInputContext ctx);
	/**
	 * Enter a parse tree produced by the {@code assignmentInput}
	 * labeled alternative in {@link ExpressionEvaluatorParser#assignmentStart}.
	 * @param ctx the parse tree
	 */
	void enterAssignmentInput(ExpressionEvaluatorParser.AssignmentInputContext ctx);
	/**
	 * Exit a parse tree produced by the {@code assignmentInput}
	 * labeled alternative in {@link ExpressionEvaluatorParser#assignmentStart}.
	 * @param ctx the parse tree
	 */
	void exitAssignmentInput(ExpressionEvaluatorParser.AssignmentInputContext ctx);
	/**
	 * Enter a parse tree produced by the {@code logicalInput}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalStart}.
	 * @param ctx the parse tree
	 */
	void enterLogicalInput(ExpressionEvaluatorParser.LogicalInputContext ctx);
	/**
	 * Exit a parse tree produced by the {@code logicalInput}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalStart}.
	 * @param ctx the parse tree
	 */
	void exitLogicalInput(ExpressionEvaluatorParser.LogicalInputContext ctx);
	/**
	 * Enter a parse tree produced by the {@code assignmentOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#assignmentExpression}.
	 * @param ctx the parse tree
	 */
	void enterAssignmentOperation(ExpressionEvaluatorParser.AssignmentOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code assignmentOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#assignmentExpression}.
	 * @param ctx the parse tree
	 */
	void exitAssignmentOperation(ExpressionEvaluatorParser.AssignmentOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code destructuringAssignmentOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#assignmentExpression}.
	 * @param ctx the parse tree
	 */
	void enterDestructuringAssignmentOperation(ExpressionEvaluatorParser.DestructuringAssignmentOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code destructuringAssignmentOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#assignmentExpression}.
	 * @param ctx the parse tree
	 */
	void exitDestructuringAssignmentOperation(ExpressionEvaluatorParser.DestructuringAssignmentOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code logicalOrOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalExpression}.
	 * @param ctx the parse tree
	 */
	void enterLogicalOrOperation(ExpressionEvaluatorParser.LogicalOrOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code logicalOrOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalExpression}.
	 * @param ctx the parse tree
	 */
	void exitLogicalOrOperation(ExpressionEvaluatorParser.LogicalOrOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code logicalOrChainOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalOrExpression}.
	 * @param ctx the parse tree
	 */
	void enterLogicalOrChainOperation(ExpressionEvaluatorParser.LogicalOrChainOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code logicalOrChainOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalOrExpression}.
	 * @param ctx the parse tree
	 */
	void exitLogicalOrChainOperation(ExpressionEvaluatorParser.LogicalOrChainOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code logicalAndChainOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalAndExpression}.
	 * @param ctx the parse tree
	 */
	void enterLogicalAndChainOperation(ExpressionEvaluatorParser.LogicalAndChainOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code logicalAndChainOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalAndExpression}.
	 * @param ctx the parse tree
	 */
	void exitLogicalAndChainOperation(ExpressionEvaluatorParser.LogicalAndChainOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code logicalComparisonOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void enterLogicalComparisonOperation(ExpressionEvaluatorParser.LogicalComparisonOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code logicalComparisonOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void exitLogicalComparisonOperation(ExpressionEvaluatorParser.LogicalComparisonOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code mathComparisonOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void enterMathComparisonOperation(ExpressionEvaluatorParser.MathComparisonOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code mathComparisonOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void exitMathComparisonOperation(ExpressionEvaluatorParser.MathComparisonOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code stringComparisonOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void enterStringComparisonOperation(ExpressionEvaluatorParser.StringComparisonOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code stringComparisonOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void exitStringComparisonOperation(ExpressionEvaluatorParser.StringComparisonOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateComparisonOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void enterDateComparisonOperation(ExpressionEvaluatorParser.DateComparisonOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateComparisonOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void exitDateComparisonOperation(ExpressionEvaluatorParser.DateComparisonOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code timeComparisonOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void enterTimeComparisonOperation(ExpressionEvaluatorParser.TimeComparisonOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code timeComparisonOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void exitTimeComparisonOperation(ExpressionEvaluatorParser.TimeComparisonOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateTimeComparisonOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void enterDateTimeComparisonOperation(ExpressionEvaluatorParser.DateTimeComparisonOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateTimeComparisonOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void exitDateTimeComparisonOperation(ExpressionEvaluatorParser.DateTimeComparisonOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code regexMatchOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void enterRegexMatchOperation(ExpressionEvaluatorParser.RegexMatchOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code regexMatchOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void exitRegexMatchOperation(ExpressionEvaluatorParser.RegexMatchOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code regexNotMatchOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void enterRegexNotMatchOperation(ExpressionEvaluatorParser.RegexNotMatchOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code regexNotMatchOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void exitRegexNotMatchOperation(ExpressionEvaluatorParser.RegexNotMatchOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code mathInOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void enterMathInOperation(ExpressionEvaluatorParser.MathInOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code mathInOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void exitMathInOperation(ExpressionEvaluatorParser.MathInOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code stringInOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void enterStringInOperation(ExpressionEvaluatorParser.StringInOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code stringInOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void exitStringInOperation(ExpressionEvaluatorParser.StringInOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateInOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void enterDateInOperation(ExpressionEvaluatorParser.DateInOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateInOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void exitDateInOperation(ExpressionEvaluatorParser.DateInOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code timeInOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void enterTimeInOperation(ExpressionEvaluatorParser.TimeInOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code timeInOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void exitTimeInOperation(ExpressionEvaluatorParser.TimeInOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateTimeInOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void enterDateTimeInOperation(ExpressionEvaluatorParser.DateTimeInOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateTimeInOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void exitDateTimeInOperation(ExpressionEvaluatorParser.DateTimeInOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code logicalInOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void enterLogicalInOperation(ExpressionEvaluatorParser.LogicalInOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code logicalInOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void exitLogicalInOperation(ExpressionEvaluatorParser.LogicalInOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code nullInOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void enterNullInOperation(ExpressionEvaluatorParser.NullInOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code nullInOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void exitNullInOperation(ExpressionEvaluatorParser.NullInOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code mathNotInOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void enterMathNotInOperation(ExpressionEvaluatorParser.MathNotInOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code mathNotInOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void exitMathNotInOperation(ExpressionEvaluatorParser.MathNotInOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code stringNotInOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void enterStringNotInOperation(ExpressionEvaluatorParser.StringNotInOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code stringNotInOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void exitStringNotInOperation(ExpressionEvaluatorParser.StringNotInOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateNotInOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void enterDateNotInOperation(ExpressionEvaluatorParser.DateNotInOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateNotInOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void exitDateNotInOperation(ExpressionEvaluatorParser.DateNotInOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code timeNotInOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void enterTimeNotInOperation(ExpressionEvaluatorParser.TimeNotInOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code timeNotInOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void exitTimeNotInOperation(ExpressionEvaluatorParser.TimeNotInOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateTimeNotInOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void enterDateTimeNotInOperation(ExpressionEvaluatorParser.DateTimeNotInOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateTimeNotInOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void exitDateTimeNotInOperation(ExpressionEvaluatorParser.DateTimeNotInOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code logicalNotInOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void enterLogicalNotInOperation(ExpressionEvaluatorParser.LogicalNotInOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code logicalNotInOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void exitLogicalNotInOperation(ExpressionEvaluatorParser.LogicalNotInOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code nullNotInOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void enterNullNotInOperation(ExpressionEvaluatorParser.NullNotInOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code nullNotInOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void exitNullNotInOperation(ExpressionEvaluatorParser.NullNotInOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code mathBetweenOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void enterMathBetweenOperation(ExpressionEvaluatorParser.MathBetweenOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code mathBetweenOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void exitMathBetweenOperation(ExpressionEvaluatorParser.MathBetweenOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code stringBetweenOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void enterStringBetweenOperation(ExpressionEvaluatorParser.StringBetweenOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code stringBetweenOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void exitStringBetweenOperation(ExpressionEvaluatorParser.StringBetweenOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateBetweenOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void enterDateBetweenOperation(ExpressionEvaluatorParser.DateBetweenOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateBetweenOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void exitDateBetweenOperation(ExpressionEvaluatorParser.DateBetweenOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code timeBetweenOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void enterTimeBetweenOperation(ExpressionEvaluatorParser.TimeBetweenOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code timeBetweenOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void exitTimeBetweenOperation(ExpressionEvaluatorParser.TimeBetweenOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateTimeBetweenOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void enterDateTimeBetweenOperation(ExpressionEvaluatorParser.DateTimeBetweenOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateTimeBetweenOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void exitDateTimeBetweenOperation(ExpressionEvaluatorParser.DateTimeBetweenOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code mathNotBetweenOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void enterMathNotBetweenOperation(ExpressionEvaluatorParser.MathNotBetweenOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code mathNotBetweenOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void exitMathNotBetweenOperation(ExpressionEvaluatorParser.MathNotBetweenOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code stringNotBetweenOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void enterStringNotBetweenOperation(ExpressionEvaluatorParser.StringNotBetweenOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code stringNotBetweenOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void exitStringNotBetweenOperation(ExpressionEvaluatorParser.StringNotBetweenOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateNotBetweenOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void enterDateNotBetweenOperation(ExpressionEvaluatorParser.DateNotBetweenOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateNotBetweenOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void exitDateNotBetweenOperation(ExpressionEvaluatorParser.DateNotBetweenOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code timeNotBetweenOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void enterTimeNotBetweenOperation(ExpressionEvaluatorParser.TimeNotBetweenOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code timeNotBetweenOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void exitTimeNotBetweenOperation(ExpressionEvaluatorParser.TimeNotBetweenOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateTimeNotBetweenOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void enterDateTimeNotBetweenOperation(ExpressionEvaluatorParser.DateTimeNotBetweenOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateTimeNotBetweenOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void exitDateTimeNotBetweenOperation(ExpressionEvaluatorParser.DateTimeNotBetweenOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code logicalBitwiseOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalBitwiseExpression}.
	 * @param ctx the parse tree
	 */
	void enterLogicalBitwiseOperation(ExpressionEvaluatorParser.LogicalBitwiseOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code logicalBitwiseOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalBitwiseExpression}.
	 * @param ctx the parse tree
	 */
	void exitLogicalBitwiseOperation(ExpressionEvaluatorParser.LogicalBitwiseOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code logicalNotOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalNotExpression}.
	 * @param ctx the parse tree
	 */
	void enterLogicalNotOperation(ExpressionEvaluatorParser.LogicalNotOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code logicalNotOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalNotExpression}.
	 * @param ctx the parse tree
	 */
	void exitLogicalNotOperation(ExpressionEvaluatorParser.LogicalNotOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code logicalPrimaryOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalNotExpression}.
	 * @param ctx the parse tree
	 */
	void enterLogicalPrimaryOperation(ExpressionEvaluatorParser.LogicalPrimaryOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code logicalPrimaryOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalNotExpression}.
	 * @param ctx the parse tree
	 */
	void exitLogicalPrimaryOperation(ExpressionEvaluatorParser.LogicalPrimaryOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code logicalExpressionParenthesisOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalPrimary}.
	 * @param ctx the parse tree
	 */
	void enterLogicalExpressionParenthesisOperation(ExpressionEvaluatorParser.LogicalExpressionParenthesisOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code logicalExpressionParenthesisOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalPrimary}.
	 * @param ctx the parse tree
	 */
	void exitLogicalExpressionParenthesisOperation(ExpressionEvaluatorParser.LogicalExpressionParenthesisOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code logicalEntityOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalPrimary}.
	 * @param ctx the parse tree
	 */
	void enterLogicalEntityOperation(ExpressionEvaluatorParser.LogicalEntityOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code logicalEntityOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalPrimary}.
	 * @param ctx the parse tree
	 */
	void exitLogicalEntityOperation(ExpressionEvaluatorParser.LogicalEntityOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code sumOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#mathExpression}.
	 * @param ctx the parse tree
	 */
	void enterSumOperation(ExpressionEvaluatorParser.SumOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code sumOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#mathExpression}.
	 * @param ctx the parse tree
	 */
	void exitSumOperation(ExpressionEvaluatorParser.SumOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code additiveOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#sumExpression}.
	 * @param ctx the parse tree
	 */
	void enterAdditiveOperation(ExpressionEvaluatorParser.AdditiveOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code additiveOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#sumExpression}.
	 * @param ctx the parse tree
	 */
	void exitAdditiveOperation(ExpressionEvaluatorParser.AdditiveOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code multiplicativeOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#multiplicationExpression}.
	 * @param ctx the parse tree
	 */
	void enterMultiplicativeOperation(ExpressionEvaluatorParser.MultiplicativeOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code multiplicativeOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#multiplicationExpression}.
	 * @param ctx the parse tree
	 */
	void exitMultiplicativeOperation(ExpressionEvaluatorParser.MultiplicativeOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code unaryMinusOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#unaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterUnaryMinusOperation(ExpressionEvaluatorParser.UnaryMinusOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code unaryMinusOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#unaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitUnaryMinusOperation(ExpressionEvaluatorParser.UnaryMinusOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code rootExpressionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#unaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterRootExpressionOperation(ExpressionEvaluatorParser.RootExpressionOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code rootExpressionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#unaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitRootExpressionOperation(ExpressionEvaluatorParser.RootExpressionOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code rootChainOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#rootExpression}.
	 * @param ctx the parse tree
	 */
	void enterRootChainOperation(ExpressionEvaluatorParser.RootChainOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code rootChainOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#rootExpression}.
	 * @param ctx the parse tree
	 */
	void exitRootChainOperation(ExpressionEvaluatorParser.RootChainOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code exponentiationOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#exponentiationExpression}.
	 * @param ctx the parse tree
	 */
	void enterExponentiationOperation(ExpressionEvaluatorParser.ExponentiationOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code exponentiationOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#exponentiationExpression}.
	 * @param ctx the parse tree
	 */
	void exitExponentiationOperation(ExpressionEvaluatorParser.ExponentiationOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code postfixOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#postfixExpression}.
	 * @param ctx the parse tree
	 */
	void enterPostfixOperation(ExpressionEvaluatorParser.PostfixOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code postfixOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#postfixExpression}.
	 * @param ctx the parse tree
	 */
	void exitPostfixOperation(ExpressionEvaluatorParser.PostfixOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code mathExpressionParenthesisOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#primaryMathExpression}.
	 * @param ctx the parse tree
	 */
	void enterMathExpressionParenthesisOperation(ExpressionEvaluatorParser.MathExpressionParenthesisOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code mathExpressionParenthesisOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#primaryMathExpression}.
	 * @param ctx the parse tree
	 */
	void exitMathExpressionParenthesisOperation(ExpressionEvaluatorParser.MathExpressionParenthesisOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code squareRootOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#primaryMathExpression}.
	 * @param ctx the parse tree
	 */
	void enterSquareRootOperation(ExpressionEvaluatorParser.SquareRootOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code squareRootOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#primaryMathExpression}.
	 * @param ctx the parse tree
	 */
	void exitSquareRootOperation(ExpressionEvaluatorParser.SquareRootOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code modulusOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#primaryMathExpression}.
	 * @param ctx the parse tree
	 */
	void enterModulusOperation(ExpressionEvaluatorParser.ModulusOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code modulusOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#primaryMathExpression}.
	 * @param ctx the parse tree
	 */
	void exitModulusOperation(ExpressionEvaluatorParser.ModulusOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code numericEntityOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#primaryMathExpression}.
	 * @param ctx the parse tree
	 */
	void enterNumericEntityOperation(ExpressionEvaluatorParser.NumericEntityOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code numericEntityOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#primaryMathExpression}.
	 * @param ctx the parse tree
	 */
	void exitNumericEntityOperation(ExpressionEvaluatorParser.NumericEntityOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code functionCallOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#function}.
	 * @param ctx the parse tree
	 */
	void enterFunctionCallOperation(ExpressionEvaluatorParser.FunctionCallOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code functionCallOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#function}.
	 * @param ctx the parse tree
	 */
	void exitFunctionCallOperation(ExpressionEvaluatorParser.FunctionCallOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code functionReferenceTarget}
	 * labeled alternative in {@link ExpressionEvaluatorParser#referenceTarget}.
	 * @param ctx the parse tree
	 */
	void enterFunctionReferenceTarget(ExpressionEvaluatorParser.FunctionReferenceTargetContext ctx);
	/**
	 * Exit a parse tree produced by the {@code functionReferenceTarget}
	 * labeled alternative in {@link ExpressionEvaluatorParser#referenceTarget}.
	 * @param ctx the parse tree
	 */
	void exitFunctionReferenceTarget(ExpressionEvaluatorParser.FunctionReferenceTargetContext ctx);
	/**
	 * Enter a parse tree produced by the {@code identifierReferenceTarget}
	 * labeled alternative in {@link ExpressionEvaluatorParser#referenceTarget}.
	 * @param ctx the parse tree
	 */
	void enterIdentifierReferenceTarget(ExpressionEvaluatorParser.IdentifierReferenceTargetContext ctx);
	/**
	 * Exit a parse tree produced by the {@code identifierReferenceTarget}
	 * labeled alternative in {@link ExpressionEvaluatorParser#referenceTarget}.
	 * @param ctx the parse tree
	 */
	void exitIdentifierReferenceTarget(ExpressionEvaluatorParser.IdentifierReferenceTargetContext ctx);
	/**
	 * Enter a parse tree produced by the {@code propertyAccess}
	 * labeled alternative in {@link ExpressionEvaluatorParser#memberChain}.
	 * @param ctx the parse tree
	 */
	void enterPropertyAccess(ExpressionEvaluatorParser.PropertyAccessContext ctx);
	/**
	 * Exit a parse tree produced by the {@code propertyAccess}
	 * labeled alternative in {@link ExpressionEvaluatorParser#memberChain}.
	 * @param ctx the parse tree
	 */
	void exitPropertyAccess(ExpressionEvaluatorParser.PropertyAccessContext ctx);
	/**
	 * Enter a parse tree produced by the {@code safePropertyAccess}
	 * labeled alternative in {@link ExpressionEvaluatorParser#memberChain}.
	 * @param ctx the parse tree
	 */
	void enterSafePropertyAccess(ExpressionEvaluatorParser.SafePropertyAccessContext ctx);
	/**
	 * Exit a parse tree produced by the {@code safePropertyAccess}
	 * labeled alternative in {@link ExpressionEvaluatorParser#memberChain}.
	 * @param ctx the parse tree
	 */
	void exitSafePropertyAccess(ExpressionEvaluatorParser.SafePropertyAccessContext ctx);
	/**
	 * Enter a parse tree produced by the {@code methodCallAccess}
	 * labeled alternative in {@link ExpressionEvaluatorParser#memberChain}.
	 * @param ctx the parse tree
	 */
	void enterMethodCallAccess(ExpressionEvaluatorParser.MethodCallAccessContext ctx);
	/**
	 * Exit a parse tree produced by the {@code methodCallAccess}
	 * labeled alternative in {@link ExpressionEvaluatorParser#memberChain}.
	 * @param ctx the parse tree
	 */
	void exitMethodCallAccess(ExpressionEvaluatorParser.MethodCallAccessContext ctx);
	/**
	 * Enter a parse tree produced by the {@code safeMethodCallAccess}
	 * labeled alternative in {@link ExpressionEvaluatorParser#memberChain}.
	 * @param ctx the parse tree
	 */
	void enterSafeMethodCallAccess(ExpressionEvaluatorParser.SafeMethodCallAccessContext ctx);
	/**
	 * Exit a parse tree produced by the {@code safeMethodCallAccess}
	 * labeled alternative in {@link ExpressionEvaluatorParser#memberChain}.
	 * @param ctx the parse tree
	 */
	void exitSafeMethodCallAccess(ExpressionEvaluatorParser.SafeMethodCallAccessContext ctx);
	/**
	 * Enter a parse tree produced by the {@code greaterThanOperator}
	 * labeled alternative in {@link ExpressionEvaluatorParser#comparisonOperator}.
	 * @param ctx the parse tree
	 */
	void enterGreaterThanOperator(ExpressionEvaluatorParser.GreaterThanOperatorContext ctx);
	/**
	 * Exit a parse tree produced by the {@code greaterThanOperator}
	 * labeled alternative in {@link ExpressionEvaluatorParser#comparisonOperator}.
	 * @param ctx the parse tree
	 */
	void exitGreaterThanOperator(ExpressionEvaluatorParser.GreaterThanOperatorContext ctx);
	/**
	 * Enter a parse tree produced by the {@code greaterThanOrEqualOperator}
	 * labeled alternative in {@link ExpressionEvaluatorParser#comparisonOperator}.
	 * @param ctx the parse tree
	 */
	void enterGreaterThanOrEqualOperator(ExpressionEvaluatorParser.GreaterThanOrEqualOperatorContext ctx);
	/**
	 * Exit a parse tree produced by the {@code greaterThanOrEqualOperator}
	 * labeled alternative in {@link ExpressionEvaluatorParser#comparisonOperator}.
	 * @param ctx the parse tree
	 */
	void exitGreaterThanOrEqualOperator(ExpressionEvaluatorParser.GreaterThanOrEqualOperatorContext ctx);
	/**
	 * Enter a parse tree produced by the {@code lessThanOperator}
	 * labeled alternative in {@link ExpressionEvaluatorParser#comparisonOperator}.
	 * @param ctx the parse tree
	 */
	void enterLessThanOperator(ExpressionEvaluatorParser.LessThanOperatorContext ctx);
	/**
	 * Exit a parse tree produced by the {@code lessThanOperator}
	 * labeled alternative in {@link ExpressionEvaluatorParser#comparisonOperator}.
	 * @param ctx the parse tree
	 */
	void exitLessThanOperator(ExpressionEvaluatorParser.LessThanOperatorContext ctx);
	/**
	 * Enter a parse tree produced by the {@code lessThanOrEqualOperator}
	 * labeled alternative in {@link ExpressionEvaluatorParser#comparisonOperator}.
	 * @param ctx the parse tree
	 */
	void enterLessThanOrEqualOperator(ExpressionEvaluatorParser.LessThanOrEqualOperatorContext ctx);
	/**
	 * Exit a parse tree produced by the {@code lessThanOrEqualOperator}
	 * labeled alternative in {@link ExpressionEvaluatorParser#comparisonOperator}.
	 * @param ctx the parse tree
	 */
	void exitLessThanOrEqualOperator(ExpressionEvaluatorParser.LessThanOrEqualOperatorContext ctx);
	/**
	 * Enter a parse tree produced by the {@code equalOperator}
	 * labeled alternative in {@link ExpressionEvaluatorParser#comparisonOperator}.
	 * @param ctx the parse tree
	 */
	void enterEqualOperator(ExpressionEvaluatorParser.EqualOperatorContext ctx);
	/**
	 * Exit a parse tree produced by the {@code equalOperator}
	 * labeled alternative in {@link ExpressionEvaluatorParser#comparisonOperator}.
	 * @param ctx the parse tree
	 */
	void exitEqualOperator(ExpressionEvaluatorParser.EqualOperatorContext ctx);
	/**
	 * Enter a parse tree produced by the {@code notEqualOperator}
	 * labeled alternative in {@link ExpressionEvaluatorParser#comparisonOperator}.
	 * @param ctx the parse tree
	 */
	void enterNotEqualOperator(ExpressionEvaluatorParser.NotEqualOperatorContext ctx);
	/**
	 * Exit a parse tree produced by the {@code notEqualOperator}
	 * labeled alternative in {@link ExpressionEvaluatorParser#comparisonOperator}.
	 * @param ctx the parse tree
	 */
	void exitNotEqualOperator(ExpressionEvaluatorParser.NotEqualOperatorContext ctx);
	/**
	 * Enter a parse tree produced by the {@code mathEntityType}
	 * labeled alternative in {@link ExpressionEvaluatorParser#allEntityTypes}.
	 * @param ctx the parse tree
	 */
	void enterMathEntityType(ExpressionEvaluatorParser.MathEntityTypeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code mathEntityType}
	 * labeled alternative in {@link ExpressionEvaluatorParser#allEntityTypes}.
	 * @param ctx the parse tree
	 */
	void exitMathEntityType(ExpressionEvaluatorParser.MathEntityTypeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code logicalEntityType}
	 * labeled alternative in {@link ExpressionEvaluatorParser#allEntityTypes}.
	 * @param ctx the parse tree
	 */
	void enterLogicalEntityType(ExpressionEvaluatorParser.LogicalEntityTypeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code logicalEntityType}
	 * labeled alternative in {@link ExpressionEvaluatorParser#allEntityTypes}.
	 * @param ctx the parse tree
	 */
	void exitLogicalEntityType(ExpressionEvaluatorParser.LogicalEntityTypeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateEntityType}
	 * labeled alternative in {@link ExpressionEvaluatorParser#allEntityTypes}.
	 * @param ctx the parse tree
	 */
	void enterDateEntityType(ExpressionEvaluatorParser.DateEntityTypeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateEntityType}
	 * labeled alternative in {@link ExpressionEvaluatorParser#allEntityTypes}.
	 * @param ctx the parse tree
	 */
	void exitDateEntityType(ExpressionEvaluatorParser.DateEntityTypeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code timeEntityType}
	 * labeled alternative in {@link ExpressionEvaluatorParser#allEntityTypes}.
	 * @param ctx the parse tree
	 */
	void enterTimeEntityType(ExpressionEvaluatorParser.TimeEntityTypeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code timeEntityType}
	 * labeled alternative in {@link ExpressionEvaluatorParser#allEntityTypes}.
	 * @param ctx the parse tree
	 */
	void exitTimeEntityType(ExpressionEvaluatorParser.TimeEntityTypeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateTimeEntityType}
	 * labeled alternative in {@link ExpressionEvaluatorParser#allEntityTypes}.
	 * @param ctx the parse tree
	 */
	void enterDateTimeEntityType(ExpressionEvaluatorParser.DateTimeEntityTypeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateTimeEntityType}
	 * labeled alternative in {@link ExpressionEvaluatorParser#allEntityTypes}.
	 * @param ctx the parse tree
	 */
	void exitDateTimeEntityType(ExpressionEvaluatorParser.DateTimeEntityTypeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code stringEntityType}
	 * labeled alternative in {@link ExpressionEvaluatorParser#allEntityTypes}.
	 * @param ctx the parse tree
	 */
	void enterStringEntityType(ExpressionEvaluatorParser.StringEntityTypeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code stringEntityType}
	 * labeled alternative in {@link ExpressionEvaluatorParser#allEntityTypes}.
	 * @param ctx the parse tree
	 */
	void exitStringEntityType(ExpressionEvaluatorParser.StringEntityTypeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code vectorEntityType}
	 * labeled alternative in {@link ExpressionEvaluatorParser#allEntityTypes}.
	 * @param ctx the parse tree
	 */
	void enterVectorEntityType(ExpressionEvaluatorParser.VectorEntityTypeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code vectorEntityType}
	 * labeled alternative in {@link ExpressionEvaluatorParser#allEntityTypes}.
	 * @param ctx the parse tree
	 */
	void exitVectorEntityType(ExpressionEvaluatorParser.VectorEntityTypeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code nullEntityType}
	 * labeled alternative in {@link ExpressionEvaluatorParser#allEntityTypes}.
	 * @param ctx the parse tree
	 */
	void enterNullEntityType(ExpressionEvaluatorParser.NullEntityTypeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code nullEntityType}
	 * labeled alternative in {@link ExpressionEvaluatorParser#allEntityTypes}.
	 * @param ctx the parse tree
	 */
	void exitNullEntityType(ExpressionEvaluatorParser.NullEntityTypeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code genericAssignmentValue}
	 * labeled alternative in {@link ExpressionEvaluatorParser#assignmentValue}.
	 * @param ctx the parse tree
	 */
	void enterGenericAssignmentValue(ExpressionEvaluatorParser.GenericAssignmentValueContext ctx);
	/**
	 * Exit a parse tree produced by the {@code genericAssignmentValue}
	 * labeled alternative in {@link ExpressionEvaluatorParser#assignmentValue}.
	 * @param ctx the parse tree
	 */
	void exitGenericAssignmentValue(ExpressionEvaluatorParser.GenericAssignmentValueContext ctx);
	/**
	 * Enter a parse tree produced by the {@code mathAssignmentValue}
	 * labeled alternative in {@link ExpressionEvaluatorParser#assignmentValue}.
	 * @param ctx the parse tree
	 */
	void enterMathAssignmentValue(ExpressionEvaluatorParser.MathAssignmentValueContext ctx);
	/**
	 * Exit a parse tree produced by the {@code mathAssignmentValue}
	 * labeled alternative in {@link ExpressionEvaluatorParser#assignmentValue}.
	 * @param ctx the parse tree
	 */
	void exitMathAssignmentValue(ExpressionEvaluatorParser.MathAssignmentValueContext ctx);
	/**
	 * Enter a parse tree produced by the {@code logicalAssignmentValue}
	 * labeled alternative in {@link ExpressionEvaluatorParser#assignmentValue}.
	 * @param ctx the parse tree
	 */
	void enterLogicalAssignmentValue(ExpressionEvaluatorParser.LogicalAssignmentValueContext ctx);
	/**
	 * Exit a parse tree produced by the {@code logicalAssignmentValue}
	 * labeled alternative in {@link ExpressionEvaluatorParser#assignmentValue}.
	 * @param ctx the parse tree
	 */
	void exitLogicalAssignmentValue(ExpressionEvaluatorParser.LogicalAssignmentValueContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateAssignmentValue}
	 * labeled alternative in {@link ExpressionEvaluatorParser#assignmentValue}.
	 * @param ctx the parse tree
	 */
	void enterDateAssignmentValue(ExpressionEvaluatorParser.DateAssignmentValueContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateAssignmentValue}
	 * labeled alternative in {@link ExpressionEvaluatorParser#assignmentValue}.
	 * @param ctx the parse tree
	 */
	void exitDateAssignmentValue(ExpressionEvaluatorParser.DateAssignmentValueContext ctx);
	/**
	 * Enter a parse tree produced by the {@code timeAssignmentValue}
	 * labeled alternative in {@link ExpressionEvaluatorParser#assignmentValue}.
	 * @param ctx the parse tree
	 */
	void enterTimeAssignmentValue(ExpressionEvaluatorParser.TimeAssignmentValueContext ctx);
	/**
	 * Exit a parse tree produced by the {@code timeAssignmentValue}
	 * labeled alternative in {@link ExpressionEvaluatorParser#assignmentValue}.
	 * @param ctx the parse tree
	 */
	void exitTimeAssignmentValue(ExpressionEvaluatorParser.TimeAssignmentValueContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateTimeAssignmentValue}
	 * labeled alternative in {@link ExpressionEvaluatorParser#assignmentValue}.
	 * @param ctx the parse tree
	 */
	void enterDateTimeAssignmentValue(ExpressionEvaluatorParser.DateTimeAssignmentValueContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateTimeAssignmentValue}
	 * labeled alternative in {@link ExpressionEvaluatorParser#assignmentValue}.
	 * @param ctx the parse tree
	 */
	void exitDateTimeAssignmentValue(ExpressionEvaluatorParser.DateTimeAssignmentValueContext ctx);
	/**
	 * Enter a parse tree produced by the {@code stringAssignmentValue}
	 * labeled alternative in {@link ExpressionEvaluatorParser#assignmentValue}.
	 * @param ctx the parse tree
	 */
	void enterStringAssignmentValue(ExpressionEvaluatorParser.StringAssignmentValueContext ctx);
	/**
	 * Exit a parse tree produced by the {@code stringAssignmentValue}
	 * labeled alternative in {@link ExpressionEvaluatorParser#assignmentValue}.
	 * @param ctx the parse tree
	 */
	void exitStringAssignmentValue(ExpressionEvaluatorParser.StringAssignmentValueContext ctx);
	/**
	 * Enter a parse tree produced by the {@code vectorAssignmentValue}
	 * labeled alternative in {@link ExpressionEvaluatorParser#assignmentValue}.
	 * @param ctx the parse tree
	 */
	void enterVectorAssignmentValue(ExpressionEvaluatorParser.VectorAssignmentValueContext ctx);
	/**
	 * Exit a parse tree produced by the {@code vectorAssignmentValue}
	 * labeled alternative in {@link ExpressionEvaluatorParser#assignmentValue}.
	 * @param ctx the parse tree
	 */
	void exitVectorAssignmentValue(ExpressionEvaluatorParser.VectorAssignmentValueContext ctx);
	/**
	 * Enter a parse tree produced by the {@code genericDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#genericEntity}.
	 * @param ctx the parse tree
	 */
	void enterGenericDecisionExpression(ExpressionEvaluatorParser.GenericDecisionExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code genericDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#genericEntity}.
	 * @param ctx the parse tree
	 */
	void exitGenericDecisionExpression(ExpressionEvaluatorParser.GenericDecisionExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code genericFunctionDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#genericEntity}.
	 * @param ctx the parse tree
	 */
	void enterGenericFunctionDecisionExpression(ExpressionEvaluatorParser.GenericFunctionDecisionExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code genericFunctionDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#genericEntity}.
	 * @param ctx the parse tree
	 */
	void exitGenericFunctionDecisionExpression(ExpressionEvaluatorParser.GenericFunctionDecisionExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code castExpressionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#genericEntity}.
	 * @param ctx the parse tree
	 */
	void enterCastExpressionOperation(ExpressionEvaluatorParser.CastExpressionOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code castExpressionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#genericEntity}.
	 * @param ctx the parse tree
	 */
	void exitCastExpressionOperation(ExpressionEvaluatorParser.CastExpressionOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code referenceTargetOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#genericEntity}.
	 * @param ctx the parse tree
	 */
	void enterReferenceTargetOperation(ExpressionEvaluatorParser.ReferenceTargetOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code referenceTargetOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#genericEntity}.
	 * @param ctx the parse tree
	 */
	void exitReferenceTargetOperation(ExpressionEvaluatorParser.ReferenceTargetOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code typeCastOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#castExpression}.
	 * @param ctx the parse tree
	 */
	void enterTypeCastOperation(ExpressionEvaluatorParser.TypeCastOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code typeCastOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#castExpression}.
	 * @param ctx the parse tree
	 */
	void exitTypeCastOperation(ExpressionEvaluatorParser.TypeCastOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code booleanTypeHint}
	 * labeled alternative in {@link ExpressionEvaluatorParser#typeHint}.
	 * @param ctx the parse tree
	 */
	void enterBooleanTypeHint(ExpressionEvaluatorParser.BooleanTypeHintContext ctx);
	/**
	 * Exit a parse tree produced by the {@code booleanTypeHint}
	 * labeled alternative in {@link ExpressionEvaluatorParser#typeHint}.
	 * @param ctx the parse tree
	 */
	void exitBooleanTypeHint(ExpressionEvaluatorParser.BooleanTypeHintContext ctx);
	/**
	 * Enter a parse tree produced by the {@code numberTypeHint}
	 * labeled alternative in {@link ExpressionEvaluatorParser#typeHint}.
	 * @param ctx the parse tree
	 */
	void enterNumberTypeHint(ExpressionEvaluatorParser.NumberTypeHintContext ctx);
	/**
	 * Exit a parse tree produced by the {@code numberTypeHint}
	 * labeled alternative in {@link ExpressionEvaluatorParser#typeHint}.
	 * @param ctx the parse tree
	 */
	void exitNumberTypeHint(ExpressionEvaluatorParser.NumberTypeHintContext ctx);
	/**
	 * Enter a parse tree produced by the {@code stringTypeHint}
	 * labeled alternative in {@link ExpressionEvaluatorParser#typeHint}.
	 * @param ctx the parse tree
	 */
	void enterStringTypeHint(ExpressionEvaluatorParser.StringTypeHintContext ctx);
	/**
	 * Exit a parse tree produced by the {@code stringTypeHint}
	 * labeled alternative in {@link ExpressionEvaluatorParser#typeHint}.
	 * @param ctx the parse tree
	 */
	void exitStringTypeHint(ExpressionEvaluatorParser.StringTypeHintContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateTypeHint}
	 * labeled alternative in {@link ExpressionEvaluatorParser#typeHint}.
	 * @param ctx the parse tree
	 */
	void enterDateTypeHint(ExpressionEvaluatorParser.DateTypeHintContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateTypeHint}
	 * labeled alternative in {@link ExpressionEvaluatorParser#typeHint}.
	 * @param ctx the parse tree
	 */
	void exitDateTypeHint(ExpressionEvaluatorParser.DateTypeHintContext ctx);
	/**
	 * Enter a parse tree produced by the {@code timeTypeHint}
	 * labeled alternative in {@link ExpressionEvaluatorParser#typeHint}.
	 * @param ctx the parse tree
	 */
	void enterTimeTypeHint(ExpressionEvaluatorParser.TimeTypeHintContext ctx);
	/**
	 * Exit a parse tree produced by the {@code timeTypeHint}
	 * labeled alternative in {@link ExpressionEvaluatorParser#typeHint}.
	 * @param ctx the parse tree
	 */
	void exitTimeTypeHint(ExpressionEvaluatorParser.TimeTypeHintContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateTimeTypeHint}
	 * labeled alternative in {@link ExpressionEvaluatorParser#typeHint}.
	 * @param ctx the parse tree
	 */
	void enterDateTimeTypeHint(ExpressionEvaluatorParser.DateTimeTypeHintContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateTimeTypeHint}
	 * labeled alternative in {@link ExpressionEvaluatorParser#typeHint}.
	 * @param ctx the parse tree
	 */
	void exitDateTimeTypeHint(ExpressionEvaluatorParser.DateTimeTypeHintContext ctx);
	/**
	 * Enter a parse tree produced by the {@code vectorTypeHint}
	 * labeled alternative in {@link ExpressionEvaluatorParser#typeHint}.
	 * @param ctx the parse tree
	 */
	void enterVectorTypeHint(ExpressionEvaluatorParser.VectorTypeHintContext ctx);
	/**
	 * Exit a parse tree produced by the {@code vectorTypeHint}
	 * labeled alternative in {@link ExpressionEvaluatorParser#typeHint}.
	 * @param ctx the parse tree
	 */
	void exitVectorTypeHint(ExpressionEvaluatorParser.VectorTypeHintContext ctx);
	/**
	 * Enter a parse tree produced by the {@code logicalConstantOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalEntity}.
	 * @param ctx the parse tree
	 */
	void enterLogicalConstantOperation(ExpressionEvaluatorParser.LogicalConstantOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code logicalConstantOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalEntity}.
	 * @param ctx the parse tree
	 */
	void exitLogicalConstantOperation(ExpressionEvaluatorParser.LogicalConstantOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code logicalDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalEntity}.
	 * @param ctx the parse tree
	 */
	void enterLogicalDecisionOperation(ExpressionEvaluatorParser.LogicalDecisionOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code logicalDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalEntity}.
	 * @param ctx the parse tree
	 */
	void exitLogicalDecisionOperation(ExpressionEvaluatorParser.LogicalDecisionOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code logicalFunctionDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalEntity}.
	 * @param ctx the parse tree
	 */
	void enterLogicalFunctionDecisionOperation(ExpressionEvaluatorParser.LogicalFunctionDecisionOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code logicalFunctionDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalEntity}.
	 * @param ctx the parse tree
	 */
	void exitLogicalFunctionDecisionOperation(ExpressionEvaluatorParser.LogicalFunctionDecisionOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code logicalReferenceOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalEntity}.
	 * @param ctx the parse tree
	 */
	void enterLogicalReferenceOperation(ExpressionEvaluatorParser.LogicalReferenceOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code logicalReferenceOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalEntity}.
	 * @param ctx the parse tree
	 */
	void exitLogicalReferenceOperation(ExpressionEvaluatorParser.LogicalReferenceOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code mathDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#numericEntity}.
	 * @param ctx the parse tree
	 */
	void enterMathDecisionOperation(ExpressionEvaluatorParser.MathDecisionOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code mathDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#numericEntity}.
	 * @param ctx the parse tree
	 */
	void exitMathDecisionOperation(ExpressionEvaluatorParser.MathDecisionOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code mathFunctionDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#numericEntity}.
	 * @param ctx the parse tree
	 */
	void enterMathFunctionDecisionOperation(ExpressionEvaluatorParser.MathFunctionDecisionOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code mathFunctionDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#numericEntity}.
	 * @param ctx the parse tree
	 */
	void exitMathFunctionDecisionOperation(ExpressionEvaluatorParser.MathFunctionDecisionOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code numericConstantOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#numericEntity}.
	 * @param ctx the parse tree
	 */
	void enterNumericConstantOperation(ExpressionEvaluatorParser.NumericConstantOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code numericConstantOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#numericEntity}.
	 * @param ctx the parse tree
	 */
	void exitNumericConstantOperation(ExpressionEvaluatorParser.NumericConstantOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code numericReferenceOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#numericEntity}.
	 * @param ctx the parse tree
	 */
	void enterNumericReferenceOperation(ExpressionEvaluatorParser.NumericReferenceOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code numericReferenceOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#numericEntity}.
	 * @param ctx the parse tree
	 */
	void exitNumericReferenceOperation(ExpressionEvaluatorParser.NumericReferenceOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code stringConcatenationOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#stringConcatExpression}.
	 * @param ctx the parse tree
	 */
	void enterStringConcatenationOperation(ExpressionEvaluatorParser.StringConcatenationOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code stringConcatenationOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#stringConcatExpression}.
	 * @param ctx the parse tree
	 */
	void exitStringConcatenationOperation(ExpressionEvaluatorParser.StringConcatenationOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code stringDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#stringEntity}.
	 * @param ctx the parse tree
	 */
	void enterStringDecisionOperation(ExpressionEvaluatorParser.StringDecisionOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code stringDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#stringEntity}.
	 * @param ctx the parse tree
	 */
	void exitStringDecisionOperation(ExpressionEvaluatorParser.StringDecisionOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code stringFunctionDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#stringEntity}.
	 * @param ctx the parse tree
	 */
	void enterStringFunctionDecisionOperation(ExpressionEvaluatorParser.StringFunctionDecisionOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code stringFunctionDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#stringEntity}.
	 * @param ctx the parse tree
	 */
	void exitStringFunctionDecisionOperation(ExpressionEvaluatorParser.StringFunctionDecisionOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code stringConstantOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#stringEntity}.
	 * @param ctx the parse tree
	 */
	void enterStringConstantOperation(ExpressionEvaluatorParser.StringConstantOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code stringConstantOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#stringEntity}.
	 * @param ctx the parse tree
	 */
	void exitStringConstantOperation(ExpressionEvaluatorParser.StringConstantOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code stringReferenceOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#stringEntity}.
	 * @param ctx the parse tree
	 */
	void enterStringReferenceOperation(ExpressionEvaluatorParser.StringReferenceOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code stringReferenceOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#stringEntity}.
	 * @param ctx the parse tree
	 */
	void exitStringReferenceOperation(ExpressionEvaluatorParser.StringReferenceOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateEntity}.
	 * @param ctx the parse tree
	 */
	void enterDateDecisionOperation(ExpressionEvaluatorParser.DateDecisionOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateEntity}.
	 * @param ctx the parse tree
	 */
	void exitDateDecisionOperation(ExpressionEvaluatorParser.DateDecisionOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateFunctionDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateEntity}.
	 * @param ctx the parse tree
	 */
	void enterDateFunctionDecisionOperation(ExpressionEvaluatorParser.DateFunctionDecisionOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateFunctionDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateEntity}.
	 * @param ctx the parse tree
	 */
	void exitDateFunctionDecisionOperation(ExpressionEvaluatorParser.DateFunctionDecisionOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateConstantOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateEntity}.
	 * @param ctx the parse tree
	 */
	void enterDateConstantOperation(ExpressionEvaluatorParser.DateConstantOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateConstantOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateEntity}.
	 * @param ctx the parse tree
	 */
	void exitDateConstantOperation(ExpressionEvaluatorParser.DateConstantOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateCurrentValueOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateEntity}.
	 * @param ctx the parse tree
	 */
	void enterDateCurrentValueOperation(ExpressionEvaluatorParser.DateCurrentValueOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateCurrentValueOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateEntity}.
	 * @param ctx the parse tree
	 */
	void exitDateCurrentValueOperation(ExpressionEvaluatorParser.DateCurrentValueOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateReferenceOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateEntity}.
	 * @param ctx the parse tree
	 */
	void enterDateReferenceOperation(ExpressionEvaluatorParser.DateReferenceOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateReferenceOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateEntity}.
	 * @param ctx the parse tree
	 */
	void exitDateReferenceOperation(ExpressionEvaluatorParser.DateReferenceOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code timeDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#timeEntity}.
	 * @param ctx the parse tree
	 */
	void enterTimeDecisionOperation(ExpressionEvaluatorParser.TimeDecisionOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code timeDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#timeEntity}.
	 * @param ctx the parse tree
	 */
	void exitTimeDecisionOperation(ExpressionEvaluatorParser.TimeDecisionOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code timeFunctionDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#timeEntity}.
	 * @param ctx the parse tree
	 */
	void enterTimeFunctionDecisionOperation(ExpressionEvaluatorParser.TimeFunctionDecisionOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code timeFunctionDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#timeEntity}.
	 * @param ctx the parse tree
	 */
	void exitTimeFunctionDecisionOperation(ExpressionEvaluatorParser.TimeFunctionDecisionOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code timeConstantOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#timeEntity}.
	 * @param ctx the parse tree
	 */
	void enterTimeConstantOperation(ExpressionEvaluatorParser.TimeConstantOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code timeConstantOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#timeEntity}.
	 * @param ctx the parse tree
	 */
	void exitTimeConstantOperation(ExpressionEvaluatorParser.TimeConstantOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code timeCurrentValueOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#timeEntity}.
	 * @param ctx the parse tree
	 */
	void enterTimeCurrentValueOperation(ExpressionEvaluatorParser.TimeCurrentValueOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code timeCurrentValueOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#timeEntity}.
	 * @param ctx the parse tree
	 */
	void exitTimeCurrentValueOperation(ExpressionEvaluatorParser.TimeCurrentValueOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code timeReferenceOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#timeEntity}.
	 * @param ctx the parse tree
	 */
	void enterTimeReferenceOperation(ExpressionEvaluatorParser.TimeReferenceOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code timeReferenceOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#timeEntity}.
	 * @param ctx the parse tree
	 */
	void exitTimeReferenceOperation(ExpressionEvaluatorParser.TimeReferenceOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateTimeDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateTimeEntity}.
	 * @param ctx the parse tree
	 */
	void enterDateTimeDecisionOperation(ExpressionEvaluatorParser.DateTimeDecisionOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateTimeDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateTimeEntity}.
	 * @param ctx the parse tree
	 */
	void exitDateTimeDecisionOperation(ExpressionEvaluatorParser.DateTimeDecisionOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateTimeFunctionDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateTimeEntity}.
	 * @param ctx the parse tree
	 */
	void enterDateTimeFunctionDecisionOperation(ExpressionEvaluatorParser.DateTimeFunctionDecisionOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateTimeFunctionDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateTimeEntity}.
	 * @param ctx the parse tree
	 */
	void exitDateTimeFunctionDecisionOperation(ExpressionEvaluatorParser.DateTimeFunctionDecisionOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateTimeConstantOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateTimeEntity}.
	 * @param ctx the parse tree
	 */
	void enterDateTimeConstantOperation(ExpressionEvaluatorParser.DateTimeConstantOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateTimeConstantOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateTimeEntity}.
	 * @param ctx the parse tree
	 */
	void exitDateTimeConstantOperation(ExpressionEvaluatorParser.DateTimeConstantOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateTimeCurrentValueOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateTimeEntity}.
	 * @param ctx the parse tree
	 */
	void enterDateTimeCurrentValueOperation(ExpressionEvaluatorParser.DateTimeCurrentValueOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateTimeCurrentValueOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateTimeEntity}.
	 * @param ctx the parse tree
	 */
	void exitDateTimeCurrentValueOperation(ExpressionEvaluatorParser.DateTimeCurrentValueOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateTimeReferenceOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateTimeEntity}.
	 * @param ctx the parse tree
	 */
	void enterDateTimeReferenceOperation(ExpressionEvaluatorParser.DateTimeReferenceOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateTimeReferenceOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateTimeEntity}.
	 * @param ctx the parse tree
	 */
	void exitDateTimeReferenceOperation(ExpressionEvaluatorParser.DateTimeReferenceOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code vectorDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#vectorEntity}.
	 * @param ctx the parse tree
	 */
	void enterVectorDecisionOperation(ExpressionEvaluatorParser.VectorDecisionOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code vectorDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#vectorEntity}.
	 * @param ctx the parse tree
	 */
	void exitVectorDecisionOperation(ExpressionEvaluatorParser.VectorDecisionOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code vectorFunctionDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#vectorEntity}.
	 * @param ctx the parse tree
	 */
	void enterVectorFunctionDecisionOperation(ExpressionEvaluatorParser.VectorFunctionDecisionOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code vectorFunctionDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#vectorEntity}.
	 * @param ctx the parse tree
	 */
	void exitVectorFunctionDecisionOperation(ExpressionEvaluatorParser.VectorFunctionDecisionOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code vectorOfEntitiesOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#vectorEntity}.
	 * @param ctx the parse tree
	 */
	void enterVectorOfEntitiesOperation(ExpressionEvaluatorParser.VectorOfEntitiesOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code vectorOfEntitiesOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#vectorEntity}.
	 * @param ctx the parse tree
	 */
	void exitVectorOfEntitiesOperation(ExpressionEvaluatorParser.VectorOfEntitiesOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code vectorReferenceOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#vectorEntity}.
	 * @param ctx the parse tree
	 */
	void enterVectorReferenceOperation(ExpressionEvaluatorParser.VectorReferenceOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code vectorReferenceOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#vectorEntity}.
	 * @param ctx the parse tree
	 */
	void exitVectorReferenceOperation(ExpressionEvaluatorParser.VectorReferenceOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code vectorOfVariablesOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#vectorOfVariables}.
	 * @param ctx the parse tree
	 */
	void enterVectorOfVariablesOperation(ExpressionEvaluatorParser.VectorOfVariablesOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code vectorOfVariablesOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#vectorOfVariables}.
	 * @param ctx the parse tree
	 */
	void exitVectorOfVariablesOperation(ExpressionEvaluatorParser.VectorOfVariablesOperationContext ctx);
}