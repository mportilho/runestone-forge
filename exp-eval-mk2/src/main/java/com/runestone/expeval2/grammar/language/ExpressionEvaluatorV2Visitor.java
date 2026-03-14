// Generated from /home/marcelo/dev/git/runestone-forge/exp-eval-mk2/src/main/resources/ExpressionEvaluatorV2.g4 by ANTLR 4.13.1

package com.runestone.expeval2.grammar.language;

import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link ExpressionEvaluatorV2Parser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface ExpressionEvaluatorV2Visitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link ExpressionEvaluatorV2Parser#startAssignments}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStartAssignments(ExpressionEvaluatorV2Parser.StartAssignmentsContext ctx);
	/**
	 * Visit a parse tree produced by {@link ExpressionEvaluatorV2Parser#mathStart}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMathStart(ExpressionEvaluatorV2Parser.MathStartContext ctx);
	/**
	 * Visit a parse tree produced by {@link ExpressionEvaluatorV2Parser#logicalStart}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalStart(ExpressionEvaluatorV2Parser.LogicalStartContext ctx);
	/**
	 * Visit a parse tree produced by the {@code assignOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#assignmentExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignOperation(ExpressionEvaluatorV2Parser.AssignOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code destructuringAssignment}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#assignmentExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDestructuringAssignment(ExpressionEvaluatorV2Parser.DestructuringAssignmentContext ctx);
	/**
	 * Visit a parse tree produced by the {@code orExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrExpression(ExpressionEvaluatorV2Parser.OrExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateTimeExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateTimeExpression(ExpressionEvaluatorV2Parser.DateTimeExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code andExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAndExpression(ExpressionEvaluatorV2Parser.AndExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code stringExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringExpression(ExpressionEvaluatorV2Parser.StringExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code logicalValue}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalValue(ExpressionEvaluatorV2Parser.LogicalValueContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateExpression(ExpressionEvaluatorV2Parser.DateExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code notExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNotExpression(ExpressionEvaluatorV2Parser.NotExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code logicComparisonExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicComparisonExpression(ExpressionEvaluatorV2Parser.LogicComparisonExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code comparisonMathExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComparisonMathExpression(ExpressionEvaluatorV2Parser.ComparisonMathExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code timeExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTimeExpression(ExpressionEvaluatorV2Parser.TimeExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code bitwiseLogicExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBitwiseLogicExpression(ExpressionEvaluatorV2Parser.BitwiseLogicExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code logicalParenthesis}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalParenthesis(ExpressionEvaluatorV2Parser.LogicalParenthesisContext ctx);
	/**
	 * Visit a parse tree produced by the {@code modulusExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#mathExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitModulusExpression(ExpressionEvaluatorV2Parser.ModulusExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code mathParenthesis}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#mathExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMathParenthesis(ExpressionEvaluatorV2Parser.MathParenthesisContext ctx);
	/**
	 * Visit a parse tree produced by the {@code multiplicationExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#mathExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMultiplicationExpression(ExpressionEvaluatorV2Parser.MultiplicationExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code mathSpecificExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#mathExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMathSpecificExpression(ExpressionEvaluatorV2Parser.MathSpecificExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code factorialExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#mathExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFactorialExpression(ExpressionEvaluatorV2Parser.FactorialExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code negateMathParenthesis}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#mathExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNegateMathParenthesis(ExpressionEvaluatorV2Parser.NegateMathParenthesisContext ctx);
	/**
	 * Visit a parse tree produced by the {@code squareRootExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#mathExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSquareRootExpression(ExpressionEvaluatorV2Parser.SquareRootExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code percentExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#mathExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPercentExpression(ExpressionEvaluatorV2Parser.PercentExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code unaryMinusExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#mathExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnaryMinusExpression(ExpressionEvaluatorV2Parser.UnaryMinusExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code rootExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#mathExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRootExpression(ExpressionEvaluatorV2Parser.RootExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code sumExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#mathExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSumExpression(ExpressionEvaluatorV2Parser.SumExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code degreeExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#mathExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDegreeExpression(ExpressionEvaluatorV2Parser.DegreeExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code numberValue}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#mathExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumberValue(ExpressionEvaluatorV2Parser.NumberValueContext ctx);
	/**
	 * Visit a parse tree produced by the {@code exponentiationExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#mathExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExponentiationExpression(ExpressionEvaluatorV2Parser.ExponentiationExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code logarithmExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#mathSpecificFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogarithmExpression(ExpressionEvaluatorV2Parser.LogarithmExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code roundingExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#mathSpecificFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRoundingExpression(ExpressionEvaluatorV2Parser.RoundingExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code sequenceExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#mathSpecificFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSequenceExpression(ExpressionEvaluatorV2Parser.SequenceExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code fixedLogarithm}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logarithmFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFixedLogarithm(ExpressionEvaluatorV2Parser.FixedLogarithmContext ctx);
	/**
	 * Visit a parse tree produced by the {@code variableLogarithm}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logarithmFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariableLogarithm(ExpressionEvaluatorV2Parser.VariableLogarithmContext ctx);
	/**
	 * Visit a parse tree produced by {@link ExpressionEvaluatorV2Parser#roundingFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRoundingFunction(ExpressionEvaluatorV2Parser.RoundingFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ExpressionEvaluatorV2Parser#sequenceFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSequenceFunction(ExpressionEvaluatorV2Parser.SequenceFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ExpressionEvaluatorV2Parser#dateUnit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateUnit(ExpressionEvaluatorV2Parser.DateUnitContext ctx);
	/**
	 * Visit a parse tree produced by {@link ExpressionEvaluatorV2Parser#timeUnit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTimeUnit(ExpressionEvaluatorV2Parser.TimeUnitContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateParenthesis}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateOperation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateParenthesis(ExpressionEvaluatorV2Parser.DateParenthesisContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateFunction}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateOperation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateFunction(ExpressionEvaluatorV2Parser.DateFunctionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code timeParenthesis}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#timeOperation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTimeParenthesis(ExpressionEvaluatorV2Parser.TimeParenthesisContext ctx);
	/**
	 * Visit a parse tree produced by the {@code timeFunction}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#timeOperation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTimeFunction(ExpressionEvaluatorV2Parser.TimeFunctionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateTimeParenthesis}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateTimeOperation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateTimeParenthesis(ExpressionEvaluatorV2Parser.DateTimeParenthesisContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateTimeFunction}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateTimeOperation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateTimeFunction(ExpressionEvaluatorV2Parser.DateTimeFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ExpressionEvaluatorV2Parser#function}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunction(ExpressionEvaluatorV2Parser.FunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ExpressionEvaluatorV2Parser#comparisonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComparisonOperator(ExpressionEvaluatorV2Parser.ComparisonOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link ExpressionEvaluatorV2Parser#logicalOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalOperator(ExpressionEvaluatorV2Parser.LogicalOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link ExpressionEvaluatorV2Parser#allEntityTypes}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAllEntityTypes(ExpressionEvaluatorV2Parser.AllEntityTypesContext ctx);
	/**
	 * Visit a parse tree produced by the {@code logicalConstant}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalConstant(ExpressionEvaluatorV2Parser.LogicalConstantContext ctx);
	/**
	 * Visit a parse tree produced by the {@code logicalDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalDecisionExpression(ExpressionEvaluatorV2Parser.LogicalDecisionExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code logicalFunctionDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalFunctionDecisionExpression(ExpressionEvaluatorV2Parser.LogicalFunctionDecisionExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code logicalFunctionResult}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalFunctionResult(ExpressionEvaluatorV2Parser.LogicalFunctionResultContext ctx);
	/**
	 * Visit a parse tree produced by the {@code logicalVariable}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalVariable(ExpressionEvaluatorV2Parser.LogicalVariableContext ctx);
	/**
	 * Visit a parse tree produced by the {@code mathDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#numericEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMathDecisionExpression(ExpressionEvaluatorV2Parser.MathDecisionExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code mathFunctionDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#numericEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMathFunctionDecisionExpression(ExpressionEvaluatorV2Parser.MathFunctionDecisionExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code summationVariable}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#numericEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSummationVariable(ExpressionEvaluatorV2Parser.SummationVariableContext ctx);
	/**
	 * Visit a parse tree produced by the {@code productSequenceVariable}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#numericEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProductSequenceVariable(ExpressionEvaluatorV2Parser.ProductSequenceVariableContext ctx);
	/**
	 * Visit a parse tree produced by the {@code numericConstant}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#numericEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumericConstant(ExpressionEvaluatorV2Parser.NumericConstantContext ctx);
	/**
	 * Visit a parse tree produced by the {@code numericFunctionResult}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#numericEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumericFunctionResult(ExpressionEvaluatorV2Parser.NumericFunctionResultContext ctx);
	/**
	 * Visit a parse tree produced by the {@code numericVariable}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#numericEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumericVariable(ExpressionEvaluatorV2Parser.NumericVariableContext ctx);
	/**
	 * Visit a parse tree produced by the {@code stringDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#stringEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringDecisionExpression(ExpressionEvaluatorV2Parser.StringDecisionExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code stringFunctionDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#stringEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringFunctionDecisionExpression(ExpressionEvaluatorV2Parser.StringFunctionDecisionExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code stringConstant}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#stringEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringConstant(ExpressionEvaluatorV2Parser.StringConstantContext ctx);
	/**
	 * Visit a parse tree produced by the {@code stringFunctionResult}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#stringEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringFunctionResult(ExpressionEvaluatorV2Parser.StringFunctionResultContext ctx);
	/**
	 * Visit a parse tree produced by the {@code stringVariable}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#stringEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringVariable(ExpressionEvaluatorV2Parser.StringVariableContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateDecisionExpression(ExpressionEvaluatorV2Parser.DateDecisionExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateFunctionDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateFunctionDecisionExpression(ExpressionEvaluatorV2Parser.DateFunctionDecisionExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateConstant}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateConstant(ExpressionEvaluatorV2Parser.DateConstantContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateCurrentValue}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateCurrentValue(ExpressionEvaluatorV2Parser.DateCurrentValueContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateVariable}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateVariable(ExpressionEvaluatorV2Parser.DateVariableContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateFunctionResult}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateFunctionResult(ExpressionEvaluatorV2Parser.DateFunctionResultContext ctx);
	/**
	 * Visit a parse tree produced by the {@code timeDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#timeEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTimeDecisionExpression(ExpressionEvaluatorV2Parser.TimeDecisionExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code timeFunctionDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#timeEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTimeFunctionDecisionExpression(ExpressionEvaluatorV2Parser.TimeFunctionDecisionExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code timeConstant}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#timeEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTimeConstant(ExpressionEvaluatorV2Parser.TimeConstantContext ctx);
	/**
	 * Visit a parse tree produced by the {@code timeCurrentValue}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#timeEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTimeCurrentValue(ExpressionEvaluatorV2Parser.TimeCurrentValueContext ctx);
	/**
	 * Visit a parse tree produced by the {@code timeVariable}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#timeEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTimeVariable(ExpressionEvaluatorV2Parser.TimeVariableContext ctx);
	/**
	 * Visit a parse tree produced by the {@code timeFunctionResult}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#timeEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTimeFunctionResult(ExpressionEvaluatorV2Parser.TimeFunctionResultContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateTimeDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateTimeEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateTimeDecisionExpression(ExpressionEvaluatorV2Parser.DateTimeDecisionExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateTimeFunctionDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateTimeEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateTimeFunctionDecisionExpression(ExpressionEvaluatorV2Parser.DateTimeFunctionDecisionExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateTimeConstant}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateTimeEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateTimeConstant(ExpressionEvaluatorV2Parser.DateTimeConstantContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateTimeCurrentValue}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateTimeEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateTimeCurrentValue(ExpressionEvaluatorV2Parser.DateTimeCurrentValueContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateTimeVariable}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateTimeEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateTimeVariable(ExpressionEvaluatorV2Parser.DateTimeVariableContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateTimeFunctionResult}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateTimeEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateTimeFunctionResult(ExpressionEvaluatorV2Parser.DateTimeFunctionResultContext ctx);
	/**
	 * Visit a parse tree produced by the {@code vectorOfEntities}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#vectorEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVectorOfEntities(ExpressionEvaluatorV2Parser.VectorOfEntitiesContext ctx);
	/**
	 * Visit a parse tree produced by the {@code vectorVariable}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#vectorEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVectorVariable(ExpressionEvaluatorV2Parser.VectorVariableContext ctx);
	/**
	 * Visit a parse tree produced by {@link ExpressionEvaluatorV2Parser#vectorOfVariables}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVectorOfVariables(ExpressionEvaluatorV2Parser.VectorOfVariablesContext ctx);
}