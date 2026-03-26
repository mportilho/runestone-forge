// Generated from com/runestone/expeval2/internal/grammar/ExpressionEvaluatorV2.g4 by ANTLR 4.13.1
package com.runestone.expeval2.internal.grammar;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link ExpressionEvaluatorV2Parser}.
 */
public interface ExpressionEvaluatorV2Listener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by the {@code mathInput}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#mathStart}.
	 * @param ctx the parse tree
	 */
	void enterMathInput(ExpressionEvaluatorV2Parser.MathInputContext ctx);
	/**
	 * Exit a parse tree produced by the {@code mathInput}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#mathStart}.
	 * @param ctx the parse tree
	 */
	void exitMathInput(ExpressionEvaluatorV2Parser.MathInputContext ctx);
	/**
	 * Enter a parse tree produced by the {@code assignmentInput}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#assignmentStart}.
	 * @param ctx the parse tree
	 */
	void enterAssignmentInput(ExpressionEvaluatorV2Parser.AssignmentInputContext ctx);
	/**
	 * Exit a parse tree produced by the {@code assignmentInput}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#assignmentStart}.
	 * @param ctx the parse tree
	 */
	void exitAssignmentInput(ExpressionEvaluatorV2Parser.AssignmentInputContext ctx);
	/**
	 * Enter a parse tree produced by the {@code logicalInput}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalStart}.
	 * @param ctx the parse tree
	 */
	void enterLogicalInput(ExpressionEvaluatorV2Parser.LogicalInputContext ctx);
	/**
	 * Exit a parse tree produced by the {@code logicalInput}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalStart}.
	 * @param ctx the parse tree
	 */
	void exitLogicalInput(ExpressionEvaluatorV2Parser.LogicalInputContext ctx);
	/**
	 * Enter a parse tree produced by the {@code assignmentOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#assignmentExpression}.
	 * @param ctx the parse tree
	 */
	void enterAssignmentOperation(ExpressionEvaluatorV2Parser.AssignmentOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code assignmentOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#assignmentExpression}.
	 * @param ctx the parse tree
	 */
	void exitAssignmentOperation(ExpressionEvaluatorV2Parser.AssignmentOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code destructuringAssignmentOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#assignmentExpression}.
	 * @param ctx the parse tree
	 */
	void enterDestructuringAssignmentOperation(ExpressionEvaluatorV2Parser.DestructuringAssignmentOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code destructuringAssignmentOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#assignmentExpression}.
	 * @param ctx the parse tree
	 */
	void exitDestructuringAssignmentOperation(ExpressionEvaluatorV2Parser.DestructuringAssignmentOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code logicalOrOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalExpression}.
	 * @param ctx the parse tree
	 */
	void enterLogicalOrOperation(ExpressionEvaluatorV2Parser.LogicalOrOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code logicalOrOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalExpression}.
	 * @param ctx the parse tree
	 */
	void exitLogicalOrOperation(ExpressionEvaluatorV2Parser.LogicalOrOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code logicalOrChainOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalOrExpression}.
	 * @param ctx the parse tree
	 */
	void enterLogicalOrChainOperation(ExpressionEvaluatorV2Parser.LogicalOrChainOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code logicalOrChainOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalOrExpression}.
	 * @param ctx the parse tree
	 */
	void exitLogicalOrChainOperation(ExpressionEvaluatorV2Parser.LogicalOrChainOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code logicalAndChainOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalAndExpression}.
	 * @param ctx the parse tree
	 */
	void enterLogicalAndChainOperation(ExpressionEvaluatorV2Parser.LogicalAndChainOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code logicalAndChainOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalAndExpression}.
	 * @param ctx the parse tree
	 */
	void exitLogicalAndChainOperation(ExpressionEvaluatorV2Parser.LogicalAndChainOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code logicalComparisonOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void enterLogicalComparisonOperation(ExpressionEvaluatorV2Parser.LogicalComparisonOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code logicalComparisonOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void exitLogicalComparisonOperation(ExpressionEvaluatorV2Parser.LogicalComparisonOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code mathComparisonOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void enterMathComparisonOperation(ExpressionEvaluatorV2Parser.MathComparisonOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code mathComparisonOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void exitMathComparisonOperation(ExpressionEvaluatorV2Parser.MathComparisonOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code stringComparisonOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void enterStringComparisonOperation(ExpressionEvaluatorV2Parser.StringComparisonOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code stringComparisonOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void exitStringComparisonOperation(ExpressionEvaluatorV2Parser.StringComparisonOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateComparisonOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void enterDateComparisonOperation(ExpressionEvaluatorV2Parser.DateComparisonOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateComparisonOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void exitDateComparisonOperation(ExpressionEvaluatorV2Parser.DateComparisonOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code timeComparisonOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void enterTimeComparisonOperation(ExpressionEvaluatorV2Parser.TimeComparisonOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code timeComparisonOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void exitTimeComparisonOperation(ExpressionEvaluatorV2Parser.TimeComparisonOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateTimeComparisonOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void enterDateTimeComparisonOperation(ExpressionEvaluatorV2Parser.DateTimeComparisonOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateTimeComparisonOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void exitDateTimeComparisonOperation(ExpressionEvaluatorV2Parser.DateTimeComparisonOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code regexMatchOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void enterRegexMatchOperation(ExpressionEvaluatorV2Parser.RegexMatchOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code regexMatchOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void exitRegexMatchOperation(ExpressionEvaluatorV2Parser.RegexMatchOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code regexNotMatchOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void enterRegexNotMatchOperation(ExpressionEvaluatorV2Parser.RegexNotMatchOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code regexNotMatchOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 */
	void exitRegexNotMatchOperation(ExpressionEvaluatorV2Parser.RegexNotMatchOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code logicalBitwiseOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalBitwiseExpression}.
	 * @param ctx the parse tree
	 */
	void enterLogicalBitwiseOperation(ExpressionEvaluatorV2Parser.LogicalBitwiseOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code logicalBitwiseOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalBitwiseExpression}.
	 * @param ctx the parse tree
	 */
	void exitLogicalBitwiseOperation(ExpressionEvaluatorV2Parser.LogicalBitwiseOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code logicalNotOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalNotExpression}.
	 * @param ctx the parse tree
	 */
	void enterLogicalNotOperation(ExpressionEvaluatorV2Parser.LogicalNotOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code logicalNotOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalNotExpression}.
	 * @param ctx the parse tree
	 */
	void exitLogicalNotOperation(ExpressionEvaluatorV2Parser.LogicalNotOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code logicalPrimaryOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalNotExpression}.
	 * @param ctx the parse tree
	 */
	void enterLogicalPrimaryOperation(ExpressionEvaluatorV2Parser.LogicalPrimaryOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code logicalPrimaryOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalNotExpression}.
	 * @param ctx the parse tree
	 */
	void exitLogicalPrimaryOperation(ExpressionEvaluatorV2Parser.LogicalPrimaryOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code logicalExpressionParenthesisOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalPrimary}.
	 * @param ctx the parse tree
	 */
	void enterLogicalExpressionParenthesisOperation(ExpressionEvaluatorV2Parser.LogicalExpressionParenthesisOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code logicalExpressionParenthesisOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalPrimary}.
	 * @param ctx the parse tree
	 */
	void exitLogicalExpressionParenthesisOperation(ExpressionEvaluatorV2Parser.LogicalExpressionParenthesisOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code logicalEntityOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalPrimary}.
	 * @param ctx the parse tree
	 */
	void enterLogicalEntityOperation(ExpressionEvaluatorV2Parser.LogicalEntityOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code logicalEntityOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalPrimary}.
	 * @param ctx the parse tree
	 */
	void exitLogicalEntityOperation(ExpressionEvaluatorV2Parser.LogicalEntityOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code sumOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#mathExpression}.
	 * @param ctx the parse tree
	 */
	void enterSumOperation(ExpressionEvaluatorV2Parser.SumOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code sumOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#mathExpression}.
	 * @param ctx the parse tree
	 */
	void exitSumOperation(ExpressionEvaluatorV2Parser.SumOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code additiveOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#sumExpression}.
	 * @param ctx the parse tree
	 */
	void enterAdditiveOperation(ExpressionEvaluatorV2Parser.AdditiveOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code additiveOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#sumExpression}.
	 * @param ctx the parse tree
	 */
	void exitAdditiveOperation(ExpressionEvaluatorV2Parser.AdditiveOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code multiplicativeOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#multiplicationExpression}.
	 * @param ctx the parse tree
	 */
	void enterMultiplicativeOperation(ExpressionEvaluatorV2Parser.MultiplicativeOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code multiplicativeOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#multiplicationExpression}.
	 * @param ctx the parse tree
	 */
	void exitMultiplicativeOperation(ExpressionEvaluatorV2Parser.MultiplicativeOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code unaryMinusOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#unaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterUnaryMinusOperation(ExpressionEvaluatorV2Parser.UnaryMinusOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code unaryMinusOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#unaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitUnaryMinusOperation(ExpressionEvaluatorV2Parser.UnaryMinusOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code rootExpressionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#unaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterRootExpressionOperation(ExpressionEvaluatorV2Parser.RootExpressionOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code rootExpressionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#unaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitRootExpressionOperation(ExpressionEvaluatorV2Parser.RootExpressionOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code rootChainOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#rootExpression}.
	 * @param ctx the parse tree
	 */
	void enterRootChainOperation(ExpressionEvaluatorV2Parser.RootChainOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code rootChainOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#rootExpression}.
	 * @param ctx the parse tree
	 */
	void exitRootChainOperation(ExpressionEvaluatorV2Parser.RootChainOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code exponentiationOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#exponentiationExpression}.
	 * @param ctx the parse tree
	 */
	void enterExponentiationOperation(ExpressionEvaluatorV2Parser.ExponentiationOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code exponentiationOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#exponentiationExpression}.
	 * @param ctx the parse tree
	 */
	void exitExponentiationOperation(ExpressionEvaluatorV2Parser.ExponentiationOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code postfixOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#postfixExpression}.
	 * @param ctx the parse tree
	 */
	void enterPostfixOperation(ExpressionEvaluatorV2Parser.PostfixOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code postfixOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#postfixExpression}.
	 * @param ctx the parse tree
	 */
	void exitPostfixOperation(ExpressionEvaluatorV2Parser.PostfixOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code mathExpressionParenthesisOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#primaryMathExpression}.
	 * @param ctx the parse tree
	 */
	void enterMathExpressionParenthesisOperation(ExpressionEvaluatorV2Parser.MathExpressionParenthesisOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code mathExpressionParenthesisOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#primaryMathExpression}.
	 * @param ctx the parse tree
	 */
	void exitMathExpressionParenthesisOperation(ExpressionEvaluatorV2Parser.MathExpressionParenthesisOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code squareRootOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#primaryMathExpression}.
	 * @param ctx the parse tree
	 */
	void enterSquareRootOperation(ExpressionEvaluatorV2Parser.SquareRootOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code squareRootOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#primaryMathExpression}.
	 * @param ctx the parse tree
	 */
	void exitSquareRootOperation(ExpressionEvaluatorV2Parser.SquareRootOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code modulusOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#primaryMathExpression}.
	 * @param ctx the parse tree
	 */
	void enterModulusOperation(ExpressionEvaluatorV2Parser.ModulusOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code modulusOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#primaryMathExpression}.
	 * @param ctx the parse tree
	 */
	void exitModulusOperation(ExpressionEvaluatorV2Parser.ModulusOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code numericEntityOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#primaryMathExpression}.
	 * @param ctx the parse tree
	 */
	void enterNumericEntityOperation(ExpressionEvaluatorV2Parser.NumericEntityOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code numericEntityOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#primaryMathExpression}.
	 * @param ctx the parse tree
	 */
	void exitNumericEntityOperation(ExpressionEvaluatorV2Parser.NumericEntityOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code functionCallOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#function}.
	 * @param ctx the parse tree
	 */
	void enterFunctionCallOperation(ExpressionEvaluatorV2Parser.FunctionCallOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code functionCallOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#function}.
	 * @param ctx the parse tree
	 */
	void exitFunctionCallOperation(ExpressionEvaluatorV2Parser.FunctionCallOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code functionReferenceTarget}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#referenceTarget}.
	 * @param ctx the parse tree
	 */
	void enterFunctionReferenceTarget(ExpressionEvaluatorV2Parser.FunctionReferenceTargetContext ctx);
	/**
	 * Exit a parse tree produced by the {@code functionReferenceTarget}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#referenceTarget}.
	 * @param ctx the parse tree
	 */
	void exitFunctionReferenceTarget(ExpressionEvaluatorV2Parser.FunctionReferenceTargetContext ctx);
	/**
	 * Enter a parse tree produced by the {@code identifierReferenceTarget}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#referenceTarget}.
	 * @param ctx the parse tree
	 */
	void enterIdentifierReferenceTarget(ExpressionEvaluatorV2Parser.IdentifierReferenceTargetContext ctx);
	/**
	 * Exit a parse tree produced by the {@code identifierReferenceTarget}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#referenceTarget}.
	 * @param ctx the parse tree
	 */
	void exitIdentifierReferenceTarget(ExpressionEvaluatorV2Parser.IdentifierReferenceTargetContext ctx);
	/**
	 * Enter a parse tree produced by the {@code propertyAccess}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#memberChain}.
	 * @param ctx the parse tree
	 */
	void enterPropertyAccess(ExpressionEvaluatorV2Parser.PropertyAccessContext ctx);
	/**
	 * Exit a parse tree produced by the {@code propertyAccess}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#memberChain}.
	 * @param ctx the parse tree
	 */
	void exitPropertyAccess(ExpressionEvaluatorV2Parser.PropertyAccessContext ctx);
	/**
	 * Enter a parse tree produced by the {@code safePropertyAccess}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#memberChain}.
	 * @param ctx the parse tree
	 */
	void enterSafePropertyAccess(ExpressionEvaluatorV2Parser.SafePropertyAccessContext ctx);
	/**
	 * Exit a parse tree produced by the {@code safePropertyAccess}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#memberChain}.
	 * @param ctx the parse tree
	 */
	void exitSafePropertyAccess(ExpressionEvaluatorV2Parser.SafePropertyAccessContext ctx);
	/**
	 * Enter a parse tree produced by the {@code methodCallAccess}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#memberChain}.
	 * @param ctx the parse tree
	 */
	void enterMethodCallAccess(ExpressionEvaluatorV2Parser.MethodCallAccessContext ctx);
	/**
	 * Exit a parse tree produced by the {@code methodCallAccess}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#memberChain}.
	 * @param ctx the parse tree
	 */
	void exitMethodCallAccess(ExpressionEvaluatorV2Parser.MethodCallAccessContext ctx);
	/**
	 * Enter a parse tree produced by the {@code safeMethodCallAccess}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#memberChain}.
	 * @param ctx the parse tree
	 */
	void enterSafeMethodCallAccess(ExpressionEvaluatorV2Parser.SafeMethodCallAccessContext ctx);
	/**
	 * Exit a parse tree produced by the {@code safeMethodCallAccess}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#memberChain}.
	 * @param ctx the parse tree
	 */
	void exitSafeMethodCallAccess(ExpressionEvaluatorV2Parser.SafeMethodCallAccessContext ctx);
	/**
	 * Enter a parse tree produced by the {@code greaterThanOperator}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#comparisonOperator}.
	 * @param ctx the parse tree
	 */
	void enterGreaterThanOperator(ExpressionEvaluatorV2Parser.GreaterThanOperatorContext ctx);
	/**
	 * Exit a parse tree produced by the {@code greaterThanOperator}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#comparisonOperator}.
	 * @param ctx the parse tree
	 */
	void exitGreaterThanOperator(ExpressionEvaluatorV2Parser.GreaterThanOperatorContext ctx);
	/**
	 * Enter a parse tree produced by the {@code greaterThanOrEqualOperator}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#comparisonOperator}.
	 * @param ctx the parse tree
	 */
	void enterGreaterThanOrEqualOperator(ExpressionEvaluatorV2Parser.GreaterThanOrEqualOperatorContext ctx);
	/**
	 * Exit a parse tree produced by the {@code greaterThanOrEqualOperator}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#comparisonOperator}.
	 * @param ctx the parse tree
	 */
	void exitGreaterThanOrEqualOperator(ExpressionEvaluatorV2Parser.GreaterThanOrEqualOperatorContext ctx);
	/**
	 * Enter a parse tree produced by the {@code lessThanOperator}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#comparisonOperator}.
	 * @param ctx the parse tree
	 */
	void enterLessThanOperator(ExpressionEvaluatorV2Parser.LessThanOperatorContext ctx);
	/**
	 * Exit a parse tree produced by the {@code lessThanOperator}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#comparisonOperator}.
	 * @param ctx the parse tree
	 */
	void exitLessThanOperator(ExpressionEvaluatorV2Parser.LessThanOperatorContext ctx);
	/**
	 * Enter a parse tree produced by the {@code lessThanOrEqualOperator}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#comparisonOperator}.
	 * @param ctx the parse tree
	 */
	void enterLessThanOrEqualOperator(ExpressionEvaluatorV2Parser.LessThanOrEqualOperatorContext ctx);
	/**
	 * Exit a parse tree produced by the {@code lessThanOrEqualOperator}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#comparisonOperator}.
	 * @param ctx the parse tree
	 */
	void exitLessThanOrEqualOperator(ExpressionEvaluatorV2Parser.LessThanOrEqualOperatorContext ctx);
	/**
	 * Enter a parse tree produced by the {@code equalOperator}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#comparisonOperator}.
	 * @param ctx the parse tree
	 */
	void enterEqualOperator(ExpressionEvaluatorV2Parser.EqualOperatorContext ctx);
	/**
	 * Exit a parse tree produced by the {@code equalOperator}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#comparisonOperator}.
	 * @param ctx the parse tree
	 */
	void exitEqualOperator(ExpressionEvaluatorV2Parser.EqualOperatorContext ctx);
	/**
	 * Enter a parse tree produced by the {@code notEqualOperator}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#comparisonOperator}.
	 * @param ctx the parse tree
	 */
	void enterNotEqualOperator(ExpressionEvaluatorV2Parser.NotEqualOperatorContext ctx);
	/**
	 * Exit a parse tree produced by the {@code notEqualOperator}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#comparisonOperator}.
	 * @param ctx the parse tree
	 */
	void exitNotEqualOperator(ExpressionEvaluatorV2Parser.NotEqualOperatorContext ctx);
	/**
	 * Enter a parse tree produced by the {@code mathEntityType}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#allEntityTypes}.
	 * @param ctx the parse tree
	 */
	void enterMathEntityType(ExpressionEvaluatorV2Parser.MathEntityTypeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code mathEntityType}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#allEntityTypes}.
	 * @param ctx the parse tree
	 */
	void exitMathEntityType(ExpressionEvaluatorV2Parser.MathEntityTypeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code logicalEntityType}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#allEntityTypes}.
	 * @param ctx the parse tree
	 */
	void enterLogicalEntityType(ExpressionEvaluatorV2Parser.LogicalEntityTypeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code logicalEntityType}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#allEntityTypes}.
	 * @param ctx the parse tree
	 */
	void exitLogicalEntityType(ExpressionEvaluatorV2Parser.LogicalEntityTypeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateEntityType}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#allEntityTypes}.
	 * @param ctx the parse tree
	 */
	void enterDateEntityType(ExpressionEvaluatorV2Parser.DateEntityTypeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateEntityType}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#allEntityTypes}.
	 * @param ctx the parse tree
	 */
	void exitDateEntityType(ExpressionEvaluatorV2Parser.DateEntityTypeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code timeEntityType}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#allEntityTypes}.
	 * @param ctx the parse tree
	 */
	void enterTimeEntityType(ExpressionEvaluatorV2Parser.TimeEntityTypeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code timeEntityType}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#allEntityTypes}.
	 * @param ctx the parse tree
	 */
	void exitTimeEntityType(ExpressionEvaluatorV2Parser.TimeEntityTypeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateTimeEntityType}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#allEntityTypes}.
	 * @param ctx the parse tree
	 */
	void enterDateTimeEntityType(ExpressionEvaluatorV2Parser.DateTimeEntityTypeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateTimeEntityType}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#allEntityTypes}.
	 * @param ctx the parse tree
	 */
	void exitDateTimeEntityType(ExpressionEvaluatorV2Parser.DateTimeEntityTypeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code stringEntityType}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#allEntityTypes}.
	 * @param ctx the parse tree
	 */
	void enterStringEntityType(ExpressionEvaluatorV2Parser.StringEntityTypeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code stringEntityType}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#allEntityTypes}.
	 * @param ctx the parse tree
	 */
	void exitStringEntityType(ExpressionEvaluatorV2Parser.StringEntityTypeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code vectorEntityType}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#allEntityTypes}.
	 * @param ctx the parse tree
	 */
	void enterVectorEntityType(ExpressionEvaluatorV2Parser.VectorEntityTypeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code vectorEntityType}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#allEntityTypes}.
	 * @param ctx the parse tree
	 */
	void exitVectorEntityType(ExpressionEvaluatorV2Parser.VectorEntityTypeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code nullEntityType}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#allEntityTypes}.
	 * @param ctx the parse tree
	 */
	void enterNullEntityType(ExpressionEvaluatorV2Parser.NullEntityTypeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code nullEntityType}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#allEntityTypes}.
	 * @param ctx the parse tree
	 */
	void exitNullEntityType(ExpressionEvaluatorV2Parser.NullEntityTypeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code genericAssignmentValue}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#assignmentValue}.
	 * @param ctx the parse tree
	 */
	void enterGenericAssignmentValue(ExpressionEvaluatorV2Parser.GenericAssignmentValueContext ctx);
	/**
	 * Exit a parse tree produced by the {@code genericAssignmentValue}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#assignmentValue}.
	 * @param ctx the parse tree
	 */
	void exitGenericAssignmentValue(ExpressionEvaluatorV2Parser.GenericAssignmentValueContext ctx);
	/**
	 * Enter a parse tree produced by the {@code mathAssignmentValue}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#assignmentValue}.
	 * @param ctx the parse tree
	 */
	void enterMathAssignmentValue(ExpressionEvaluatorV2Parser.MathAssignmentValueContext ctx);
	/**
	 * Exit a parse tree produced by the {@code mathAssignmentValue}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#assignmentValue}.
	 * @param ctx the parse tree
	 */
	void exitMathAssignmentValue(ExpressionEvaluatorV2Parser.MathAssignmentValueContext ctx);
	/**
	 * Enter a parse tree produced by the {@code logicalAssignmentValue}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#assignmentValue}.
	 * @param ctx the parse tree
	 */
	void enterLogicalAssignmentValue(ExpressionEvaluatorV2Parser.LogicalAssignmentValueContext ctx);
	/**
	 * Exit a parse tree produced by the {@code logicalAssignmentValue}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#assignmentValue}.
	 * @param ctx the parse tree
	 */
	void exitLogicalAssignmentValue(ExpressionEvaluatorV2Parser.LogicalAssignmentValueContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateAssignmentValue}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#assignmentValue}.
	 * @param ctx the parse tree
	 */
	void enterDateAssignmentValue(ExpressionEvaluatorV2Parser.DateAssignmentValueContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateAssignmentValue}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#assignmentValue}.
	 * @param ctx the parse tree
	 */
	void exitDateAssignmentValue(ExpressionEvaluatorV2Parser.DateAssignmentValueContext ctx);
	/**
	 * Enter a parse tree produced by the {@code timeAssignmentValue}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#assignmentValue}.
	 * @param ctx the parse tree
	 */
	void enterTimeAssignmentValue(ExpressionEvaluatorV2Parser.TimeAssignmentValueContext ctx);
	/**
	 * Exit a parse tree produced by the {@code timeAssignmentValue}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#assignmentValue}.
	 * @param ctx the parse tree
	 */
	void exitTimeAssignmentValue(ExpressionEvaluatorV2Parser.TimeAssignmentValueContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateTimeAssignmentValue}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#assignmentValue}.
	 * @param ctx the parse tree
	 */
	void enterDateTimeAssignmentValue(ExpressionEvaluatorV2Parser.DateTimeAssignmentValueContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateTimeAssignmentValue}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#assignmentValue}.
	 * @param ctx the parse tree
	 */
	void exitDateTimeAssignmentValue(ExpressionEvaluatorV2Parser.DateTimeAssignmentValueContext ctx);
	/**
	 * Enter a parse tree produced by the {@code stringAssignmentValue}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#assignmentValue}.
	 * @param ctx the parse tree
	 */
	void enterStringAssignmentValue(ExpressionEvaluatorV2Parser.StringAssignmentValueContext ctx);
	/**
	 * Exit a parse tree produced by the {@code stringAssignmentValue}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#assignmentValue}.
	 * @param ctx the parse tree
	 */
	void exitStringAssignmentValue(ExpressionEvaluatorV2Parser.StringAssignmentValueContext ctx);
	/**
	 * Enter a parse tree produced by the {@code vectorAssignmentValue}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#assignmentValue}.
	 * @param ctx the parse tree
	 */
	void enterVectorAssignmentValue(ExpressionEvaluatorV2Parser.VectorAssignmentValueContext ctx);
	/**
	 * Exit a parse tree produced by the {@code vectorAssignmentValue}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#assignmentValue}.
	 * @param ctx the parse tree
	 */
	void exitVectorAssignmentValue(ExpressionEvaluatorV2Parser.VectorAssignmentValueContext ctx);
	/**
	 * Enter a parse tree produced by the {@code genericDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#genericEntity}.
	 * @param ctx the parse tree
	 */
	void enterGenericDecisionExpression(ExpressionEvaluatorV2Parser.GenericDecisionExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code genericDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#genericEntity}.
	 * @param ctx the parse tree
	 */
	void exitGenericDecisionExpression(ExpressionEvaluatorV2Parser.GenericDecisionExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code genericFunctionDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#genericEntity}.
	 * @param ctx the parse tree
	 */
	void enterGenericFunctionDecisionExpression(ExpressionEvaluatorV2Parser.GenericFunctionDecisionExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code genericFunctionDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#genericEntity}.
	 * @param ctx the parse tree
	 */
	void exitGenericFunctionDecisionExpression(ExpressionEvaluatorV2Parser.GenericFunctionDecisionExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code castExpressionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#genericEntity}.
	 * @param ctx the parse tree
	 */
	void enterCastExpressionOperation(ExpressionEvaluatorV2Parser.CastExpressionOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code castExpressionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#genericEntity}.
	 * @param ctx the parse tree
	 */
	void exitCastExpressionOperation(ExpressionEvaluatorV2Parser.CastExpressionOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code referenceTargetOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#genericEntity}.
	 * @param ctx the parse tree
	 */
	void enterReferenceTargetOperation(ExpressionEvaluatorV2Parser.ReferenceTargetOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code referenceTargetOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#genericEntity}.
	 * @param ctx the parse tree
	 */
	void exitReferenceTargetOperation(ExpressionEvaluatorV2Parser.ReferenceTargetOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code typeCastOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#castExpression}.
	 * @param ctx the parse tree
	 */
	void enterTypeCastOperation(ExpressionEvaluatorV2Parser.TypeCastOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code typeCastOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#castExpression}.
	 * @param ctx the parse tree
	 */
	void exitTypeCastOperation(ExpressionEvaluatorV2Parser.TypeCastOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code booleanTypeHint}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#typeHint}.
	 * @param ctx the parse tree
	 */
	void enterBooleanTypeHint(ExpressionEvaluatorV2Parser.BooleanTypeHintContext ctx);
	/**
	 * Exit a parse tree produced by the {@code booleanTypeHint}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#typeHint}.
	 * @param ctx the parse tree
	 */
	void exitBooleanTypeHint(ExpressionEvaluatorV2Parser.BooleanTypeHintContext ctx);
	/**
	 * Enter a parse tree produced by the {@code numberTypeHint}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#typeHint}.
	 * @param ctx the parse tree
	 */
	void enterNumberTypeHint(ExpressionEvaluatorV2Parser.NumberTypeHintContext ctx);
	/**
	 * Exit a parse tree produced by the {@code numberTypeHint}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#typeHint}.
	 * @param ctx the parse tree
	 */
	void exitNumberTypeHint(ExpressionEvaluatorV2Parser.NumberTypeHintContext ctx);
	/**
	 * Enter a parse tree produced by the {@code stringTypeHint}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#typeHint}.
	 * @param ctx the parse tree
	 */
	void enterStringTypeHint(ExpressionEvaluatorV2Parser.StringTypeHintContext ctx);
	/**
	 * Exit a parse tree produced by the {@code stringTypeHint}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#typeHint}.
	 * @param ctx the parse tree
	 */
	void exitStringTypeHint(ExpressionEvaluatorV2Parser.StringTypeHintContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateTypeHint}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#typeHint}.
	 * @param ctx the parse tree
	 */
	void enterDateTypeHint(ExpressionEvaluatorV2Parser.DateTypeHintContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateTypeHint}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#typeHint}.
	 * @param ctx the parse tree
	 */
	void exitDateTypeHint(ExpressionEvaluatorV2Parser.DateTypeHintContext ctx);
	/**
	 * Enter a parse tree produced by the {@code timeTypeHint}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#typeHint}.
	 * @param ctx the parse tree
	 */
	void enterTimeTypeHint(ExpressionEvaluatorV2Parser.TimeTypeHintContext ctx);
	/**
	 * Exit a parse tree produced by the {@code timeTypeHint}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#typeHint}.
	 * @param ctx the parse tree
	 */
	void exitTimeTypeHint(ExpressionEvaluatorV2Parser.TimeTypeHintContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateTimeTypeHint}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#typeHint}.
	 * @param ctx the parse tree
	 */
	void enterDateTimeTypeHint(ExpressionEvaluatorV2Parser.DateTimeTypeHintContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateTimeTypeHint}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#typeHint}.
	 * @param ctx the parse tree
	 */
	void exitDateTimeTypeHint(ExpressionEvaluatorV2Parser.DateTimeTypeHintContext ctx);
	/**
	 * Enter a parse tree produced by the {@code vectorTypeHint}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#typeHint}.
	 * @param ctx the parse tree
	 */
	void enterVectorTypeHint(ExpressionEvaluatorV2Parser.VectorTypeHintContext ctx);
	/**
	 * Exit a parse tree produced by the {@code vectorTypeHint}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#typeHint}.
	 * @param ctx the parse tree
	 */
	void exitVectorTypeHint(ExpressionEvaluatorV2Parser.VectorTypeHintContext ctx);
	/**
	 * Enter a parse tree produced by the {@code logicalConstantOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalEntity}.
	 * @param ctx the parse tree
	 */
	void enterLogicalConstantOperation(ExpressionEvaluatorV2Parser.LogicalConstantOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code logicalConstantOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalEntity}.
	 * @param ctx the parse tree
	 */
	void exitLogicalConstantOperation(ExpressionEvaluatorV2Parser.LogicalConstantOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code logicalDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalEntity}.
	 * @param ctx the parse tree
	 */
	void enterLogicalDecisionOperation(ExpressionEvaluatorV2Parser.LogicalDecisionOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code logicalDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalEntity}.
	 * @param ctx the parse tree
	 */
	void exitLogicalDecisionOperation(ExpressionEvaluatorV2Parser.LogicalDecisionOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code logicalFunctionDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalEntity}.
	 * @param ctx the parse tree
	 */
	void enterLogicalFunctionDecisionOperation(ExpressionEvaluatorV2Parser.LogicalFunctionDecisionOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code logicalFunctionDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalEntity}.
	 * @param ctx the parse tree
	 */
	void exitLogicalFunctionDecisionOperation(ExpressionEvaluatorV2Parser.LogicalFunctionDecisionOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code logicalReferenceOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalEntity}.
	 * @param ctx the parse tree
	 */
	void enterLogicalReferenceOperation(ExpressionEvaluatorV2Parser.LogicalReferenceOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code logicalReferenceOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalEntity}.
	 * @param ctx the parse tree
	 */
	void exitLogicalReferenceOperation(ExpressionEvaluatorV2Parser.LogicalReferenceOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code mathDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#numericEntity}.
	 * @param ctx the parse tree
	 */
	void enterMathDecisionOperation(ExpressionEvaluatorV2Parser.MathDecisionOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code mathDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#numericEntity}.
	 * @param ctx the parse tree
	 */
	void exitMathDecisionOperation(ExpressionEvaluatorV2Parser.MathDecisionOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code mathFunctionDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#numericEntity}.
	 * @param ctx the parse tree
	 */
	void enterMathFunctionDecisionOperation(ExpressionEvaluatorV2Parser.MathFunctionDecisionOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code mathFunctionDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#numericEntity}.
	 * @param ctx the parse tree
	 */
	void exitMathFunctionDecisionOperation(ExpressionEvaluatorV2Parser.MathFunctionDecisionOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code numericConstantOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#numericEntity}.
	 * @param ctx the parse tree
	 */
	void enterNumericConstantOperation(ExpressionEvaluatorV2Parser.NumericConstantOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code numericConstantOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#numericEntity}.
	 * @param ctx the parse tree
	 */
	void exitNumericConstantOperation(ExpressionEvaluatorV2Parser.NumericConstantOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code numericReferenceOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#numericEntity}.
	 * @param ctx the parse tree
	 */
	void enterNumericReferenceOperation(ExpressionEvaluatorV2Parser.NumericReferenceOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code numericReferenceOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#numericEntity}.
	 * @param ctx the parse tree
	 */
	void exitNumericReferenceOperation(ExpressionEvaluatorV2Parser.NumericReferenceOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code stringConcatenationOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#stringConcatExpression}.
	 * @param ctx the parse tree
	 */
	void enterStringConcatenationOperation(ExpressionEvaluatorV2Parser.StringConcatenationOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code stringConcatenationOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#stringConcatExpression}.
	 * @param ctx the parse tree
	 */
	void exitStringConcatenationOperation(ExpressionEvaluatorV2Parser.StringConcatenationOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code stringDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#stringEntity}.
	 * @param ctx the parse tree
	 */
	void enterStringDecisionOperation(ExpressionEvaluatorV2Parser.StringDecisionOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code stringDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#stringEntity}.
	 * @param ctx the parse tree
	 */
	void exitStringDecisionOperation(ExpressionEvaluatorV2Parser.StringDecisionOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code stringFunctionDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#stringEntity}.
	 * @param ctx the parse tree
	 */
	void enterStringFunctionDecisionOperation(ExpressionEvaluatorV2Parser.StringFunctionDecisionOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code stringFunctionDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#stringEntity}.
	 * @param ctx the parse tree
	 */
	void exitStringFunctionDecisionOperation(ExpressionEvaluatorV2Parser.StringFunctionDecisionOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code stringConstantOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#stringEntity}.
	 * @param ctx the parse tree
	 */
	void enterStringConstantOperation(ExpressionEvaluatorV2Parser.StringConstantOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code stringConstantOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#stringEntity}.
	 * @param ctx the parse tree
	 */
	void exitStringConstantOperation(ExpressionEvaluatorV2Parser.StringConstantOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code stringReferenceOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#stringEntity}.
	 * @param ctx the parse tree
	 */
	void enterStringReferenceOperation(ExpressionEvaluatorV2Parser.StringReferenceOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code stringReferenceOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#stringEntity}.
	 * @param ctx the parse tree
	 */
	void exitStringReferenceOperation(ExpressionEvaluatorV2Parser.StringReferenceOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateEntity}.
	 * @param ctx the parse tree
	 */
	void enterDateDecisionOperation(ExpressionEvaluatorV2Parser.DateDecisionOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateEntity}.
	 * @param ctx the parse tree
	 */
	void exitDateDecisionOperation(ExpressionEvaluatorV2Parser.DateDecisionOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateFunctionDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateEntity}.
	 * @param ctx the parse tree
	 */
	void enterDateFunctionDecisionOperation(ExpressionEvaluatorV2Parser.DateFunctionDecisionOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateFunctionDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateEntity}.
	 * @param ctx the parse tree
	 */
	void exitDateFunctionDecisionOperation(ExpressionEvaluatorV2Parser.DateFunctionDecisionOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateConstantOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateEntity}.
	 * @param ctx the parse tree
	 */
	void enterDateConstantOperation(ExpressionEvaluatorV2Parser.DateConstantOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateConstantOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateEntity}.
	 * @param ctx the parse tree
	 */
	void exitDateConstantOperation(ExpressionEvaluatorV2Parser.DateConstantOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateCurrentValueOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateEntity}.
	 * @param ctx the parse tree
	 */
	void enterDateCurrentValueOperation(ExpressionEvaluatorV2Parser.DateCurrentValueOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateCurrentValueOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateEntity}.
	 * @param ctx the parse tree
	 */
	void exitDateCurrentValueOperation(ExpressionEvaluatorV2Parser.DateCurrentValueOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateReferenceOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateEntity}.
	 * @param ctx the parse tree
	 */
	void enterDateReferenceOperation(ExpressionEvaluatorV2Parser.DateReferenceOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateReferenceOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateEntity}.
	 * @param ctx the parse tree
	 */
	void exitDateReferenceOperation(ExpressionEvaluatorV2Parser.DateReferenceOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code timeDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#timeEntity}.
	 * @param ctx the parse tree
	 */
	void enterTimeDecisionOperation(ExpressionEvaluatorV2Parser.TimeDecisionOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code timeDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#timeEntity}.
	 * @param ctx the parse tree
	 */
	void exitTimeDecisionOperation(ExpressionEvaluatorV2Parser.TimeDecisionOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code timeFunctionDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#timeEntity}.
	 * @param ctx the parse tree
	 */
	void enterTimeFunctionDecisionOperation(ExpressionEvaluatorV2Parser.TimeFunctionDecisionOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code timeFunctionDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#timeEntity}.
	 * @param ctx the parse tree
	 */
	void exitTimeFunctionDecisionOperation(ExpressionEvaluatorV2Parser.TimeFunctionDecisionOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code timeConstantOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#timeEntity}.
	 * @param ctx the parse tree
	 */
	void enterTimeConstantOperation(ExpressionEvaluatorV2Parser.TimeConstantOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code timeConstantOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#timeEntity}.
	 * @param ctx the parse tree
	 */
	void exitTimeConstantOperation(ExpressionEvaluatorV2Parser.TimeConstantOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code timeCurrentValueOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#timeEntity}.
	 * @param ctx the parse tree
	 */
	void enterTimeCurrentValueOperation(ExpressionEvaluatorV2Parser.TimeCurrentValueOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code timeCurrentValueOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#timeEntity}.
	 * @param ctx the parse tree
	 */
	void exitTimeCurrentValueOperation(ExpressionEvaluatorV2Parser.TimeCurrentValueOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code timeReferenceOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#timeEntity}.
	 * @param ctx the parse tree
	 */
	void enterTimeReferenceOperation(ExpressionEvaluatorV2Parser.TimeReferenceOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code timeReferenceOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#timeEntity}.
	 * @param ctx the parse tree
	 */
	void exitTimeReferenceOperation(ExpressionEvaluatorV2Parser.TimeReferenceOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateTimeDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateTimeEntity}.
	 * @param ctx the parse tree
	 */
	void enterDateTimeDecisionOperation(ExpressionEvaluatorV2Parser.DateTimeDecisionOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateTimeDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateTimeEntity}.
	 * @param ctx the parse tree
	 */
	void exitDateTimeDecisionOperation(ExpressionEvaluatorV2Parser.DateTimeDecisionOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateTimeFunctionDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateTimeEntity}.
	 * @param ctx the parse tree
	 */
	void enterDateTimeFunctionDecisionOperation(ExpressionEvaluatorV2Parser.DateTimeFunctionDecisionOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateTimeFunctionDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateTimeEntity}.
	 * @param ctx the parse tree
	 */
	void exitDateTimeFunctionDecisionOperation(ExpressionEvaluatorV2Parser.DateTimeFunctionDecisionOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateTimeConstantOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateTimeEntity}.
	 * @param ctx the parse tree
	 */
	void enterDateTimeConstantOperation(ExpressionEvaluatorV2Parser.DateTimeConstantOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateTimeConstantOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateTimeEntity}.
	 * @param ctx the parse tree
	 */
	void exitDateTimeConstantOperation(ExpressionEvaluatorV2Parser.DateTimeConstantOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateTimeCurrentValueOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateTimeEntity}.
	 * @param ctx the parse tree
	 */
	void enterDateTimeCurrentValueOperation(ExpressionEvaluatorV2Parser.DateTimeCurrentValueOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateTimeCurrentValueOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateTimeEntity}.
	 * @param ctx the parse tree
	 */
	void exitDateTimeCurrentValueOperation(ExpressionEvaluatorV2Parser.DateTimeCurrentValueOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateTimeReferenceOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateTimeEntity}.
	 * @param ctx the parse tree
	 */
	void enterDateTimeReferenceOperation(ExpressionEvaluatorV2Parser.DateTimeReferenceOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateTimeReferenceOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateTimeEntity}.
	 * @param ctx the parse tree
	 */
	void exitDateTimeReferenceOperation(ExpressionEvaluatorV2Parser.DateTimeReferenceOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code vectorDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#vectorEntity}.
	 * @param ctx the parse tree
	 */
	void enterVectorDecisionOperation(ExpressionEvaluatorV2Parser.VectorDecisionOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code vectorDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#vectorEntity}.
	 * @param ctx the parse tree
	 */
	void exitVectorDecisionOperation(ExpressionEvaluatorV2Parser.VectorDecisionOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code vectorFunctionDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#vectorEntity}.
	 * @param ctx the parse tree
	 */
	void enterVectorFunctionDecisionOperation(ExpressionEvaluatorV2Parser.VectorFunctionDecisionOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code vectorFunctionDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#vectorEntity}.
	 * @param ctx the parse tree
	 */
	void exitVectorFunctionDecisionOperation(ExpressionEvaluatorV2Parser.VectorFunctionDecisionOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code vectorOfEntitiesOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#vectorEntity}.
	 * @param ctx the parse tree
	 */
	void enterVectorOfEntitiesOperation(ExpressionEvaluatorV2Parser.VectorOfEntitiesOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code vectorOfEntitiesOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#vectorEntity}.
	 * @param ctx the parse tree
	 */
	void exitVectorOfEntitiesOperation(ExpressionEvaluatorV2Parser.VectorOfEntitiesOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code vectorReferenceOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#vectorEntity}.
	 * @param ctx the parse tree
	 */
	void enterVectorReferenceOperation(ExpressionEvaluatorV2Parser.VectorReferenceOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code vectorReferenceOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#vectorEntity}.
	 * @param ctx the parse tree
	 */
	void exitVectorReferenceOperation(ExpressionEvaluatorV2Parser.VectorReferenceOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code vectorOfVariablesOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#vectorOfVariables}.
	 * @param ctx the parse tree
	 */
	void enterVectorOfVariablesOperation(ExpressionEvaluatorV2Parser.VectorOfVariablesOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code vectorOfVariablesOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#vectorOfVariables}.
	 * @param ctx the parse tree
	 */
	void exitVectorOfVariablesOperation(ExpressionEvaluatorV2Parser.VectorOfVariablesOperationContext ctx);
}