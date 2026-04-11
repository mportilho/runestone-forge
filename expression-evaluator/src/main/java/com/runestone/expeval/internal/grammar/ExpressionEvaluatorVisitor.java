// Generated from com/runestone/expeval/internal/grammar/ExpressionEvaluator.g4 by ANTLR 4.13.1
package com.runestone.expeval.internal.grammar;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link ExpressionEvaluatorParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface ExpressionEvaluatorVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by the {@code mathInput}
	 * labeled alternative in {@link ExpressionEvaluatorParser#mathStart}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMathInput(ExpressionEvaluatorParser.MathInputContext ctx);
	/**
	 * Visit a parse tree produced by the {@code assignmentInput}
	 * labeled alternative in {@link ExpressionEvaluatorParser#assignmentStart}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignmentInput(ExpressionEvaluatorParser.AssignmentInputContext ctx);
	/**
	 * Visit a parse tree produced by the {@code logicalInput}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalStart}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalInput(ExpressionEvaluatorParser.LogicalInputContext ctx);
	/**
	 * Visit a parse tree produced by the {@code assignmentOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#assignmentExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignmentOperation(ExpressionEvaluatorParser.AssignmentOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code destructuringAssignmentOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#assignmentExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDestructuringAssignmentOperation(ExpressionEvaluatorParser.DestructuringAssignmentOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code logicalOrOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalOrOperation(ExpressionEvaluatorParser.LogicalOrOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code logicalOrChainOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalOrExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalOrChainOperation(ExpressionEvaluatorParser.LogicalOrChainOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code logicalAndChainOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalAndExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalAndChainOperation(ExpressionEvaluatorParser.LogicalAndChainOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code logicalComparisonOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalComparisonOperation(ExpressionEvaluatorParser.LogicalComparisonOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code mathComparisonOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMathComparisonOperation(ExpressionEvaluatorParser.MathComparisonOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code stringComparisonOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringComparisonOperation(ExpressionEvaluatorParser.StringComparisonOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateComparisonOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateComparisonOperation(ExpressionEvaluatorParser.DateComparisonOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code timeComparisonOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTimeComparisonOperation(ExpressionEvaluatorParser.TimeComparisonOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateTimeComparisonOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateTimeComparisonOperation(ExpressionEvaluatorParser.DateTimeComparisonOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code regexMatchOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRegexMatchOperation(ExpressionEvaluatorParser.RegexMatchOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code regexNotMatchOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRegexNotMatchOperation(ExpressionEvaluatorParser.RegexNotMatchOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code mathInOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMathInOperation(ExpressionEvaluatorParser.MathInOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code stringInOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringInOperation(ExpressionEvaluatorParser.StringInOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateInOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateInOperation(ExpressionEvaluatorParser.DateInOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code timeInOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTimeInOperation(ExpressionEvaluatorParser.TimeInOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateTimeInOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateTimeInOperation(ExpressionEvaluatorParser.DateTimeInOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code logicalInOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalInOperation(ExpressionEvaluatorParser.LogicalInOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code nullInOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNullInOperation(ExpressionEvaluatorParser.NullInOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code mathNotInOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMathNotInOperation(ExpressionEvaluatorParser.MathNotInOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code stringNotInOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringNotInOperation(ExpressionEvaluatorParser.StringNotInOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateNotInOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateNotInOperation(ExpressionEvaluatorParser.DateNotInOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code timeNotInOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTimeNotInOperation(ExpressionEvaluatorParser.TimeNotInOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateTimeNotInOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateTimeNotInOperation(ExpressionEvaluatorParser.DateTimeNotInOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code logicalNotInOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalNotInOperation(ExpressionEvaluatorParser.LogicalNotInOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code nullNotInOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNullNotInOperation(ExpressionEvaluatorParser.NullNotInOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code mathBetweenOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMathBetweenOperation(ExpressionEvaluatorParser.MathBetweenOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code stringBetweenOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringBetweenOperation(ExpressionEvaluatorParser.StringBetweenOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateBetweenOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateBetweenOperation(ExpressionEvaluatorParser.DateBetweenOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code timeBetweenOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTimeBetweenOperation(ExpressionEvaluatorParser.TimeBetweenOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateTimeBetweenOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateTimeBetweenOperation(ExpressionEvaluatorParser.DateTimeBetweenOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code mathNotBetweenOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMathNotBetweenOperation(ExpressionEvaluatorParser.MathNotBetweenOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code stringNotBetweenOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringNotBetweenOperation(ExpressionEvaluatorParser.StringNotBetweenOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateNotBetweenOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateNotBetweenOperation(ExpressionEvaluatorParser.DateNotBetweenOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code timeNotBetweenOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTimeNotBetweenOperation(ExpressionEvaluatorParser.TimeNotBetweenOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateTimeNotBetweenOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateTimeNotBetweenOperation(ExpressionEvaluatorParser.DateTimeNotBetweenOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code logicalBitwiseOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalBitwiseExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalBitwiseOperation(ExpressionEvaluatorParser.LogicalBitwiseOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code logicalNotOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalNotExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalNotOperation(ExpressionEvaluatorParser.LogicalNotOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code logicalPrimaryOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalNotExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalPrimaryOperation(ExpressionEvaluatorParser.LogicalPrimaryOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code logicalExpressionParenthesisOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalPrimary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalExpressionParenthesisOperation(ExpressionEvaluatorParser.LogicalExpressionParenthesisOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code logicalEntityOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalPrimary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalEntityOperation(ExpressionEvaluatorParser.LogicalEntityOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code sumOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#mathExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSumOperation(ExpressionEvaluatorParser.SumOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code additiveOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#sumExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAdditiveOperation(ExpressionEvaluatorParser.AdditiveOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code multiplicativeOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#multiplicationExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMultiplicativeOperation(ExpressionEvaluatorParser.MultiplicativeOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code unaryMinusOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#unaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnaryMinusOperation(ExpressionEvaluatorParser.UnaryMinusOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code rootExpressionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#unaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRootExpressionOperation(ExpressionEvaluatorParser.RootExpressionOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code rootChainOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#rootExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRootChainOperation(ExpressionEvaluatorParser.RootChainOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code exponentiationOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#exponentiationExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExponentiationOperation(ExpressionEvaluatorParser.ExponentiationOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code postfixOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#postfixExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPostfixOperation(ExpressionEvaluatorParser.PostfixOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code mathExpressionParenthesisOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#primaryMathExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMathExpressionParenthesisOperation(ExpressionEvaluatorParser.MathExpressionParenthesisOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code squareRootOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#primaryMathExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSquareRootOperation(ExpressionEvaluatorParser.SquareRootOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code modulusOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#primaryMathExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitModulusOperation(ExpressionEvaluatorParser.ModulusOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code numericEntityOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#primaryMathExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumericEntityOperation(ExpressionEvaluatorParser.NumericEntityOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code functionCallOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#function}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionCallOperation(ExpressionEvaluatorParser.FunctionCallOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code functionReferenceTarget}
	 * labeled alternative in {@link ExpressionEvaluatorParser#referenceTarget}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionReferenceTarget(ExpressionEvaluatorParser.FunctionReferenceTargetContext ctx);
	/**
	 * Visit a parse tree produced by the {@code identifierReferenceTarget}
	 * labeled alternative in {@link ExpressionEvaluatorParser#referenceTarget}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentifierReferenceTarget(ExpressionEvaluatorParser.IdentifierReferenceTargetContext ctx);
	/**
	 * Visit a parse tree produced by the {@code propertyAccess}
	 * labeled alternative in {@link ExpressionEvaluatorParser#memberChain}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPropertyAccess(ExpressionEvaluatorParser.PropertyAccessContext ctx);
	/**
	 * Visit a parse tree produced by the {@code safePropertyAccess}
	 * labeled alternative in {@link ExpressionEvaluatorParser#memberChain}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSafePropertyAccess(ExpressionEvaluatorParser.SafePropertyAccessContext ctx);
	/**
	 * Visit a parse tree produced by the {@code methodCallAccess}
	 * labeled alternative in {@link ExpressionEvaluatorParser#memberChain}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethodCallAccess(ExpressionEvaluatorParser.MethodCallAccessContext ctx);
	/**
	 * Visit a parse tree produced by the {@code safeMethodCallAccess}
	 * labeled alternative in {@link ExpressionEvaluatorParser#memberChain}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSafeMethodCallAccess(ExpressionEvaluatorParser.SafeMethodCallAccessContext ctx);
	/**
	 * Visit a parse tree produced by the {@code greaterThanOperator}
	 * labeled alternative in {@link ExpressionEvaluatorParser#comparisonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGreaterThanOperator(ExpressionEvaluatorParser.GreaterThanOperatorContext ctx);
	/**
	 * Visit a parse tree produced by the {@code greaterThanOrEqualOperator}
	 * labeled alternative in {@link ExpressionEvaluatorParser#comparisonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGreaterThanOrEqualOperator(ExpressionEvaluatorParser.GreaterThanOrEqualOperatorContext ctx);
	/**
	 * Visit a parse tree produced by the {@code lessThanOperator}
	 * labeled alternative in {@link ExpressionEvaluatorParser#comparisonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLessThanOperator(ExpressionEvaluatorParser.LessThanOperatorContext ctx);
	/**
	 * Visit a parse tree produced by the {@code lessThanOrEqualOperator}
	 * labeled alternative in {@link ExpressionEvaluatorParser#comparisonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLessThanOrEqualOperator(ExpressionEvaluatorParser.LessThanOrEqualOperatorContext ctx);
	/**
	 * Visit a parse tree produced by the {@code equalOperator}
	 * labeled alternative in {@link ExpressionEvaluatorParser#comparisonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEqualOperator(ExpressionEvaluatorParser.EqualOperatorContext ctx);
	/**
	 * Visit a parse tree produced by the {@code notEqualOperator}
	 * labeled alternative in {@link ExpressionEvaluatorParser#comparisonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNotEqualOperator(ExpressionEvaluatorParser.NotEqualOperatorContext ctx);
	/**
	 * Visit a parse tree produced by the {@code mathEntityType}
	 * labeled alternative in {@link ExpressionEvaluatorParser#allEntityTypes}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMathEntityType(ExpressionEvaluatorParser.MathEntityTypeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code logicalEntityType}
	 * labeled alternative in {@link ExpressionEvaluatorParser#allEntityTypes}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalEntityType(ExpressionEvaluatorParser.LogicalEntityTypeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateEntityType}
	 * labeled alternative in {@link ExpressionEvaluatorParser#allEntityTypes}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateEntityType(ExpressionEvaluatorParser.DateEntityTypeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code timeEntityType}
	 * labeled alternative in {@link ExpressionEvaluatorParser#allEntityTypes}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTimeEntityType(ExpressionEvaluatorParser.TimeEntityTypeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateTimeEntityType}
	 * labeled alternative in {@link ExpressionEvaluatorParser#allEntityTypes}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateTimeEntityType(ExpressionEvaluatorParser.DateTimeEntityTypeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code stringEntityType}
	 * labeled alternative in {@link ExpressionEvaluatorParser#allEntityTypes}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringEntityType(ExpressionEvaluatorParser.StringEntityTypeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code vectorEntityType}
	 * labeled alternative in {@link ExpressionEvaluatorParser#allEntityTypes}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVectorEntityType(ExpressionEvaluatorParser.VectorEntityTypeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code nullEntityType}
	 * labeled alternative in {@link ExpressionEvaluatorParser#allEntityTypes}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNullEntityType(ExpressionEvaluatorParser.NullEntityTypeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code genericAssignmentValue}
	 * labeled alternative in {@link ExpressionEvaluatorParser#assignmentValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGenericAssignmentValue(ExpressionEvaluatorParser.GenericAssignmentValueContext ctx);
	/**
	 * Visit a parse tree produced by the {@code mathAssignmentValue}
	 * labeled alternative in {@link ExpressionEvaluatorParser#assignmentValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMathAssignmentValue(ExpressionEvaluatorParser.MathAssignmentValueContext ctx);
	/**
	 * Visit a parse tree produced by the {@code logicalAssignmentValue}
	 * labeled alternative in {@link ExpressionEvaluatorParser#assignmentValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalAssignmentValue(ExpressionEvaluatorParser.LogicalAssignmentValueContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateAssignmentValue}
	 * labeled alternative in {@link ExpressionEvaluatorParser#assignmentValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateAssignmentValue(ExpressionEvaluatorParser.DateAssignmentValueContext ctx);
	/**
	 * Visit a parse tree produced by the {@code timeAssignmentValue}
	 * labeled alternative in {@link ExpressionEvaluatorParser#assignmentValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTimeAssignmentValue(ExpressionEvaluatorParser.TimeAssignmentValueContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateTimeAssignmentValue}
	 * labeled alternative in {@link ExpressionEvaluatorParser#assignmentValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateTimeAssignmentValue(ExpressionEvaluatorParser.DateTimeAssignmentValueContext ctx);
	/**
	 * Visit a parse tree produced by the {@code stringAssignmentValue}
	 * labeled alternative in {@link ExpressionEvaluatorParser#assignmentValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringAssignmentValue(ExpressionEvaluatorParser.StringAssignmentValueContext ctx);
	/**
	 * Visit a parse tree produced by the {@code vectorAssignmentValue}
	 * labeled alternative in {@link ExpressionEvaluatorParser#assignmentValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVectorAssignmentValue(ExpressionEvaluatorParser.VectorAssignmentValueContext ctx);
	/**
	 * Visit a parse tree produced by the {@code genericDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#genericEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGenericDecisionExpression(ExpressionEvaluatorParser.GenericDecisionExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code genericFunctionDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#genericEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGenericFunctionDecisionExpression(ExpressionEvaluatorParser.GenericFunctionDecisionExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code castExpressionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#genericEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCastExpressionOperation(ExpressionEvaluatorParser.CastExpressionOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code referenceTargetOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#genericEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReferenceTargetOperation(ExpressionEvaluatorParser.ReferenceTargetOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code typeCastOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#castExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeCastOperation(ExpressionEvaluatorParser.TypeCastOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code booleanTypeHint}
	 * labeled alternative in {@link ExpressionEvaluatorParser#typeHint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBooleanTypeHint(ExpressionEvaluatorParser.BooleanTypeHintContext ctx);
	/**
	 * Visit a parse tree produced by the {@code numberTypeHint}
	 * labeled alternative in {@link ExpressionEvaluatorParser#typeHint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumberTypeHint(ExpressionEvaluatorParser.NumberTypeHintContext ctx);
	/**
	 * Visit a parse tree produced by the {@code stringTypeHint}
	 * labeled alternative in {@link ExpressionEvaluatorParser#typeHint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringTypeHint(ExpressionEvaluatorParser.StringTypeHintContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateTypeHint}
	 * labeled alternative in {@link ExpressionEvaluatorParser#typeHint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateTypeHint(ExpressionEvaluatorParser.DateTypeHintContext ctx);
	/**
	 * Visit a parse tree produced by the {@code timeTypeHint}
	 * labeled alternative in {@link ExpressionEvaluatorParser#typeHint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTimeTypeHint(ExpressionEvaluatorParser.TimeTypeHintContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateTimeTypeHint}
	 * labeled alternative in {@link ExpressionEvaluatorParser#typeHint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateTimeTypeHint(ExpressionEvaluatorParser.DateTimeTypeHintContext ctx);
	/**
	 * Visit a parse tree produced by the {@code vectorTypeHint}
	 * labeled alternative in {@link ExpressionEvaluatorParser#typeHint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVectorTypeHint(ExpressionEvaluatorParser.VectorTypeHintContext ctx);
	/**
	 * Visit a parse tree produced by the {@code logicalConstantOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalConstantOperation(ExpressionEvaluatorParser.LogicalConstantOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code logicalDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalDecisionOperation(ExpressionEvaluatorParser.LogicalDecisionOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code logicalFunctionDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalFunctionDecisionOperation(ExpressionEvaluatorParser.LogicalFunctionDecisionOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code logicalReferenceOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalReferenceOperation(ExpressionEvaluatorParser.LogicalReferenceOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code mathDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#numericEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMathDecisionOperation(ExpressionEvaluatorParser.MathDecisionOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code mathFunctionDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#numericEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMathFunctionDecisionOperation(ExpressionEvaluatorParser.MathFunctionDecisionOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code numericConstantOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#numericEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumericConstantOperation(ExpressionEvaluatorParser.NumericConstantOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code numericReferenceOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#numericEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumericReferenceOperation(ExpressionEvaluatorParser.NumericReferenceOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code stringConcatenationOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#stringConcatExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringConcatenationOperation(ExpressionEvaluatorParser.StringConcatenationOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code stringDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#stringEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringDecisionOperation(ExpressionEvaluatorParser.StringDecisionOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code stringFunctionDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#stringEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringFunctionDecisionOperation(ExpressionEvaluatorParser.StringFunctionDecisionOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code stringConstantOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#stringEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringConstantOperation(ExpressionEvaluatorParser.StringConstantOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code stringReferenceOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#stringEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringReferenceOperation(ExpressionEvaluatorParser.StringReferenceOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateDecisionOperation(ExpressionEvaluatorParser.DateDecisionOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateFunctionDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateFunctionDecisionOperation(ExpressionEvaluatorParser.DateFunctionDecisionOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateConstantOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateConstantOperation(ExpressionEvaluatorParser.DateConstantOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateCurrentValueOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateCurrentValueOperation(ExpressionEvaluatorParser.DateCurrentValueOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateReferenceOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateReferenceOperation(ExpressionEvaluatorParser.DateReferenceOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code timeDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#timeEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTimeDecisionOperation(ExpressionEvaluatorParser.TimeDecisionOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code timeFunctionDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#timeEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTimeFunctionDecisionOperation(ExpressionEvaluatorParser.TimeFunctionDecisionOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code timeConstantOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#timeEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTimeConstantOperation(ExpressionEvaluatorParser.TimeConstantOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code timeCurrentValueOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#timeEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTimeCurrentValueOperation(ExpressionEvaluatorParser.TimeCurrentValueOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code timeReferenceOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#timeEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTimeReferenceOperation(ExpressionEvaluatorParser.TimeReferenceOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateTimeDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateTimeEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateTimeDecisionOperation(ExpressionEvaluatorParser.DateTimeDecisionOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateTimeFunctionDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateTimeEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateTimeFunctionDecisionOperation(ExpressionEvaluatorParser.DateTimeFunctionDecisionOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateTimeConstantOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateTimeEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateTimeConstantOperation(ExpressionEvaluatorParser.DateTimeConstantOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateTimeCurrentValueOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateTimeEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateTimeCurrentValueOperation(ExpressionEvaluatorParser.DateTimeCurrentValueOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateTimeReferenceOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateTimeEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateTimeReferenceOperation(ExpressionEvaluatorParser.DateTimeReferenceOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code vectorDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#vectorEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVectorDecisionOperation(ExpressionEvaluatorParser.VectorDecisionOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code vectorFunctionDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#vectorEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVectorFunctionDecisionOperation(ExpressionEvaluatorParser.VectorFunctionDecisionOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code vectorOfEntitiesOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#vectorEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVectorOfEntitiesOperation(ExpressionEvaluatorParser.VectorOfEntitiesOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code vectorReferenceOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#vectorEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVectorReferenceOperation(ExpressionEvaluatorParser.VectorReferenceOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code vectorOfVariablesOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#vectorOfVariables}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVectorOfVariablesOperation(ExpressionEvaluatorParser.VectorOfVariablesOperationContext ctx);
}