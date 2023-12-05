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

// Generated from C:/dev/git/runestone-forge/expression-evaluator/src/main/resources/ExpressionEvaluator.g4 by ANTLR 4.13.1
package com.runestone.expeval.grammar.language;
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
	 * Visit a parse tree produced by {@link ExpressionEvaluatorParser#start}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStart(ExpressionEvaluatorParser.StartContext ctx);
	/**
	 * Visit a parse tree produced by {@link ExpressionEvaluatorParser#mathStart}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMathStart(ExpressionEvaluatorParser.MathStartContext ctx);
	/**
	 * Visit a parse tree produced by {@link ExpressionEvaluatorParser#logicalStart}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalStart(ExpressionEvaluatorParser.LogicalStartContext ctx);
	/**
	 * Visit a parse tree produced by the {@code assignOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#assignmentExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignOperation(ExpressionEvaluatorParser.AssignOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code destructuringAssignment}
	 * labeled alternative in {@link ExpressionEvaluatorParser#assignmentExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDestructuringAssignment(ExpressionEvaluatorParser.DestructuringAssignmentContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateTimeExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateTimeExpression(ExpressionEvaluatorParser.DateTimeExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code stringExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringExpression(ExpressionEvaluatorParser.StringExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code logicalValue}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalValue(ExpressionEvaluatorParser.LogicalValueContext ctx);
	/**
	 * Visit a parse tree produced by the {@code logicExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicExpression(ExpressionEvaluatorParser.LogicExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateExpression(ExpressionEvaluatorParser.DateExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code notExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNotExpression(ExpressionEvaluatorParser.NotExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code logicComparisonExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicComparisonExpression(ExpressionEvaluatorParser.LogicComparisonExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code comparisonMathExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComparisonMathExpression(ExpressionEvaluatorParser.ComparisonMathExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code timeExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTimeExpression(ExpressionEvaluatorParser.TimeExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code logicalParenthesis}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalParenthesis(ExpressionEvaluatorParser.LogicalParenthesisContext ctx);
	/**
	 * Visit a parse tree produced by the {@code modulusExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#mathExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitModulusExpression(ExpressionEvaluatorParser.ModulusExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code mathParenthesis}
	 * labeled alternative in {@link ExpressionEvaluatorParser#mathExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMathParenthesis(ExpressionEvaluatorParser.MathParenthesisContext ctx);
	/**
	 * Visit a parse tree produced by the {@code multiplicationExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#mathExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMultiplicationExpression(ExpressionEvaluatorParser.MultiplicationExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code mathSpecificExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#mathExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMathSpecificExpression(ExpressionEvaluatorParser.MathSpecificExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code factorialExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#mathExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFactorialExpression(ExpressionEvaluatorParser.FactorialExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code negateMathParenthesis}
	 * labeled alternative in {@link ExpressionEvaluatorParser#mathExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNegateMathParenthesis(ExpressionEvaluatorParser.NegateMathParenthesisContext ctx);
	/**
	 * Visit a parse tree produced by the {@code squareRootExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#mathExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSquareRootExpression(ExpressionEvaluatorParser.SquareRootExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code percentExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#mathExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPercentExpression(ExpressionEvaluatorParser.PercentExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code rootExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#mathExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRootExpression(ExpressionEvaluatorParser.RootExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code sumExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#mathExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSumExpression(ExpressionEvaluatorParser.SumExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code degreeExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#mathExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDegreeExpression(ExpressionEvaluatorParser.DegreeExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code numberValue}
	 * labeled alternative in {@link ExpressionEvaluatorParser#mathExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumberValue(ExpressionEvaluatorParser.NumberValueContext ctx);
	/**
	 * Visit a parse tree produced by the {@code exponentiationExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#mathExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExponentiationExpression(ExpressionEvaluatorParser.ExponentiationExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code logarithmExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#mathSpecificFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogarithmExpression(ExpressionEvaluatorParser.LogarithmExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code roundingExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#mathSpecificFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRoundingExpression(ExpressionEvaluatorParser.RoundingExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code sequenceExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#mathSpecificFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSequenceExpression(ExpressionEvaluatorParser.SequenceExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code fixedLogarithm}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logarithmFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFixedLogarithm(ExpressionEvaluatorParser.FixedLogarithmContext ctx);
	/**
	 * Visit a parse tree produced by the {@code variableLogarithm}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logarithmFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariableLogarithm(ExpressionEvaluatorParser.VariableLogarithmContext ctx);
	/**
	 * Visit a parse tree produced by {@link ExpressionEvaluatorParser#roundingFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRoundingFunction(ExpressionEvaluatorParser.RoundingFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ExpressionEvaluatorParser#sequenceFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSequenceFunction(ExpressionEvaluatorParser.SequenceFunctionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateParenthesis}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateOperation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateParenthesis(ExpressionEvaluatorParser.DateParenthesisContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateFunction}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateOperation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateFunction(ExpressionEvaluatorParser.DateFunctionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code timeParenthesis}
	 * labeled alternative in {@link ExpressionEvaluatorParser#timeOperation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTimeParenthesis(ExpressionEvaluatorParser.TimeParenthesisContext ctx);
	/**
	 * Visit a parse tree produced by the {@code timeFunction}
	 * labeled alternative in {@link ExpressionEvaluatorParser#timeOperation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTimeFunction(ExpressionEvaluatorParser.TimeFunctionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateTimeParenthesis}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateTimeOperation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateTimeParenthesis(ExpressionEvaluatorParser.DateTimeParenthesisContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateTimeFunction}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateTimeOperation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateTimeFunction(ExpressionEvaluatorParser.DateTimeFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ExpressionEvaluatorParser#function}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunction(ExpressionEvaluatorParser.FunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ExpressionEvaluatorParser#comparisonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComparisonOperator(ExpressionEvaluatorParser.ComparisonOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link ExpressionEvaluatorParser#logicalOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalOperator(ExpressionEvaluatorParser.LogicalOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link ExpressionEvaluatorParser#allEntityTypes}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAllEntityTypes(ExpressionEvaluatorParser.AllEntityTypesContext ctx);
	/**
	 * Visit a parse tree produced by the {@code logicalConstant}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalConstant(ExpressionEvaluatorParser.LogicalConstantContext ctx);
	/**
	 * Visit a parse tree produced by the {@code logicalDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalDecisionExpression(ExpressionEvaluatorParser.LogicalDecisionExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code logicalFunctionDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalFunctionDecisionExpression(ExpressionEvaluatorParser.LogicalFunctionDecisionExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code logicalFunctionResult}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalFunctionResult(ExpressionEvaluatorParser.LogicalFunctionResultContext ctx);
	/**
	 * Visit a parse tree produced by the {@code logicalVariable}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalVariable(ExpressionEvaluatorParser.LogicalVariableContext ctx);
	/**
	 * Visit a parse tree produced by the {@code mathDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#numericEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMathDecisionExpression(ExpressionEvaluatorParser.MathDecisionExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code mathFunctionDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#numericEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMathFunctionDecisionExpression(ExpressionEvaluatorParser.MathFunctionDecisionExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code eulerConstant}
	 * labeled alternative in {@link ExpressionEvaluatorParser#numericEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEulerConstant(ExpressionEvaluatorParser.EulerConstantContext ctx);
	/**
	 * Visit a parse tree produced by the {@code piConstant}
	 * labeled alternative in {@link ExpressionEvaluatorParser#numericEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPiConstant(ExpressionEvaluatorParser.PiConstantContext ctx);
	/**
	 * Visit a parse tree produced by the {@code summationVariable}
	 * labeled alternative in {@link ExpressionEvaluatorParser#numericEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSummationVariable(ExpressionEvaluatorParser.SummationVariableContext ctx);
	/**
	 * Visit a parse tree produced by the {@code productSequenceVariable}
	 * labeled alternative in {@link ExpressionEvaluatorParser#numericEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProductSequenceVariable(ExpressionEvaluatorParser.ProductSequenceVariableContext ctx);
	/**
	 * Visit a parse tree produced by the {@code numericConstant}
	 * labeled alternative in {@link ExpressionEvaluatorParser#numericEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumericConstant(ExpressionEvaluatorParser.NumericConstantContext ctx);
	/**
	 * Visit a parse tree produced by the {@code numericFunctionResult}
	 * labeled alternative in {@link ExpressionEvaluatorParser#numericEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumericFunctionResult(ExpressionEvaluatorParser.NumericFunctionResultContext ctx);
	/**
	 * Visit a parse tree produced by the {@code numericVariable}
	 * labeled alternative in {@link ExpressionEvaluatorParser#numericEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumericVariable(ExpressionEvaluatorParser.NumericVariableContext ctx);
	/**
	 * Visit a parse tree produced by the {@code stringDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#stringEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringDecisionExpression(ExpressionEvaluatorParser.StringDecisionExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code stringFunctionDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#stringEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringFunctionDecisionExpression(ExpressionEvaluatorParser.StringFunctionDecisionExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code stringConstant}
	 * labeled alternative in {@link ExpressionEvaluatorParser#stringEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringConstant(ExpressionEvaluatorParser.StringConstantContext ctx);
	/**
	 * Visit a parse tree produced by the {@code stringFunctionResult}
	 * labeled alternative in {@link ExpressionEvaluatorParser#stringEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringFunctionResult(ExpressionEvaluatorParser.StringFunctionResultContext ctx);
	/**
	 * Visit a parse tree produced by the {@code stringVariable}
	 * labeled alternative in {@link ExpressionEvaluatorParser#stringEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringVariable(ExpressionEvaluatorParser.StringVariableContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateDecisionExpression(ExpressionEvaluatorParser.DateDecisionExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateFunctionDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateFunctionDecisionExpression(ExpressionEvaluatorParser.DateFunctionDecisionExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateConstant}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateConstant(ExpressionEvaluatorParser.DateConstantContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateCurrentValue}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateCurrentValue(ExpressionEvaluatorParser.DateCurrentValueContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateVariable}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateVariable(ExpressionEvaluatorParser.DateVariableContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateFunctionResult}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateFunctionResult(ExpressionEvaluatorParser.DateFunctionResultContext ctx);
	/**
	 * Visit a parse tree produced by the {@code timeDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#timeEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTimeDecisionExpression(ExpressionEvaluatorParser.TimeDecisionExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code timeFunctionDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#timeEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTimeFunctionDecisionExpression(ExpressionEvaluatorParser.TimeFunctionDecisionExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code timeConstant}
	 * labeled alternative in {@link ExpressionEvaluatorParser#timeEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTimeConstant(ExpressionEvaluatorParser.TimeConstantContext ctx);
	/**
	 * Visit a parse tree produced by the {@code timeCurrentValue}
	 * labeled alternative in {@link ExpressionEvaluatorParser#timeEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTimeCurrentValue(ExpressionEvaluatorParser.TimeCurrentValueContext ctx);
	/**
	 * Visit a parse tree produced by the {@code timeVariable}
	 * labeled alternative in {@link ExpressionEvaluatorParser#timeEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTimeVariable(ExpressionEvaluatorParser.TimeVariableContext ctx);
	/**
	 * Visit a parse tree produced by the {@code timeFunctionResult}
	 * labeled alternative in {@link ExpressionEvaluatorParser#timeEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTimeFunctionResult(ExpressionEvaluatorParser.TimeFunctionResultContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateTimeDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateTimeEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateTimeDecisionExpression(ExpressionEvaluatorParser.DateTimeDecisionExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateTimeFunctionDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateTimeEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateTimeFunctionDecisionExpression(ExpressionEvaluatorParser.DateTimeFunctionDecisionExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateTimeConstant}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateTimeEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateTimeConstant(ExpressionEvaluatorParser.DateTimeConstantContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateTimeCurrentValue}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateTimeEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateTimeCurrentValue(ExpressionEvaluatorParser.DateTimeCurrentValueContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateTimeVariable}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateTimeEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateTimeVariable(ExpressionEvaluatorParser.DateTimeVariableContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateTimeFunctionResult}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateTimeEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateTimeFunctionResult(ExpressionEvaluatorParser.DateTimeFunctionResultContext ctx);
	/**
	 * Visit a parse tree produced by the {@code vectorOfEntities}
	 * labeled alternative in {@link ExpressionEvaluatorParser#vectorEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVectorOfEntities(ExpressionEvaluatorParser.VectorOfEntitiesContext ctx);
	/**
	 * Visit a parse tree produced by the {@code vectorVariable}
	 * labeled alternative in {@link ExpressionEvaluatorParser#vectorEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVectorVariable(ExpressionEvaluatorParser.VectorVariableContext ctx);
	/**
	 * Visit a parse tree produced by {@link ExpressionEvaluatorParser#vectorOfVariables}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVectorOfVariables(ExpressionEvaluatorParser.VectorOfVariablesContext ctx);
}