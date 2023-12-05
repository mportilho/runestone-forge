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
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link ExpressionEvaluatorParser}.
 */
public interface ExpressionEvaluatorListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link ExpressionEvaluatorParser#start}.
	 * @param ctx the parse tree
	 */
	void enterStart(ExpressionEvaluatorParser.StartContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExpressionEvaluatorParser#start}.
	 * @param ctx the parse tree
	 */
	void exitStart(ExpressionEvaluatorParser.StartContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExpressionEvaluatorParser#mathStart}.
	 * @param ctx the parse tree
	 */
	void enterMathStart(ExpressionEvaluatorParser.MathStartContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExpressionEvaluatorParser#mathStart}.
	 * @param ctx the parse tree
	 */
	void exitMathStart(ExpressionEvaluatorParser.MathStartContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExpressionEvaluatorParser#logicalStart}.
	 * @param ctx the parse tree
	 */
	void enterLogicalStart(ExpressionEvaluatorParser.LogicalStartContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExpressionEvaluatorParser#logicalStart}.
	 * @param ctx the parse tree
	 */
	void exitLogicalStart(ExpressionEvaluatorParser.LogicalStartContext ctx);
	/**
	 * Enter a parse tree produced by the {@code assignOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#assignmentExpression}.
	 * @param ctx the parse tree
	 */
	void enterAssignOperation(ExpressionEvaluatorParser.AssignOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code assignOperation}
	 * labeled alternative in {@link ExpressionEvaluatorParser#assignmentExpression}.
	 * @param ctx the parse tree
	 */
	void exitAssignOperation(ExpressionEvaluatorParser.AssignOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code destructuringAssignment}
	 * labeled alternative in {@link ExpressionEvaluatorParser#assignmentExpression}.
	 * @param ctx the parse tree
	 */
	void enterDestructuringAssignment(ExpressionEvaluatorParser.DestructuringAssignmentContext ctx);
	/**
	 * Exit a parse tree produced by the {@code destructuringAssignment}
	 * labeled alternative in {@link ExpressionEvaluatorParser#assignmentExpression}.
	 * @param ctx the parse tree
	 */
	void exitDestructuringAssignment(ExpressionEvaluatorParser.DestructuringAssignmentContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateTimeExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalExpression}.
	 * @param ctx the parse tree
	 */
	void enterDateTimeExpression(ExpressionEvaluatorParser.DateTimeExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateTimeExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalExpression}.
	 * @param ctx the parse tree
	 */
	void exitDateTimeExpression(ExpressionEvaluatorParser.DateTimeExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code stringExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalExpression}.
	 * @param ctx the parse tree
	 */
	void enterStringExpression(ExpressionEvaluatorParser.StringExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code stringExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalExpression}.
	 * @param ctx the parse tree
	 */
	void exitStringExpression(ExpressionEvaluatorParser.StringExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code logicalValue}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalExpression}.
	 * @param ctx the parse tree
	 */
	void enterLogicalValue(ExpressionEvaluatorParser.LogicalValueContext ctx);
	/**
	 * Exit a parse tree produced by the {@code logicalValue}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalExpression}.
	 * @param ctx the parse tree
	 */
	void exitLogicalValue(ExpressionEvaluatorParser.LogicalValueContext ctx);
	/**
	 * Enter a parse tree produced by the {@code logicExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalExpression}.
	 * @param ctx the parse tree
	 */
	void enterLogicExpression(ExpressionEvaluatorParser.LogicExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code logicExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalExpression}.
	 * @param ctx the parse tree
	 */
	void exitLogicExpression(ExpressionEvaluatorParser.LogicExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalExpression}.
	 * @param ctx the parse tree
	 */
	void enterDateExpression(ExpressionEvaluatorParser.DateExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalExpression}.
	 * @param ctx the parse tree
	 */
	void exitDateExpression(ExpressionEvaluatorParser.DateExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code notExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalExpression}.
	 * @param ctx the parse tree
	 */
	void enterNotExpression(ExpressionEvaluatorParser.NotExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code notExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalExpression}.
	 * @param ctx the parse tree
	 */
	void exitNotExpression(ExpressionEvaluatorParser.NotExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code logicComparisonExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalExpression}.
	 * @param ctx the parse tree
	 */
	void enterLogicComparisonExpression(ExpressionEvaluatorParser.LogicComparisonExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code logicComparisonExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalExpression}.
	 * @param ctx the parse tree
	 */
	void exitLogicComparisonExpression(ExpressionEvaluatorParser.LogicComparisonExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code comparisonMathExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalExpression}.
	 * @param ctx the parse tree
	 */
	void enterComparisonMathExpression(ExpressionEvaluatorParser.ComparisonMathExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code comparisonMathExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalExpression}.
	 * @param ctx the parse tree
	 */
	void exitComparisonMathExpression(ExpressionEvaluatorParser.ComparisonMathExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code timeExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalExpression}.
	 * @param ctx the parse tree
	 */
	void enterTimeExpression(ExpressionEvaluatorParser.TimeExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code timeExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalExpression}.
	 * @param ctx the parse tree
	 */
	void exitTimeExpression(ExpressionEvaluatorParser.TimeExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code logicalParenthesis}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalExpression}.
	 * @param ctx the parse tree
	 */
	void enterLogicalParenthesis(ExpressionEvaluatorParser.LogicalParenthesisContext ctx);
	/**
	 * Exit a parse tree produced by the {@code logicalParenthesis}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalExpression}.
	 * @param ctx the parse tree
	 */
	void exitLogicalParenthesis(ExpressionEvaluatorParser.LogicalParenthesisContext ctx);
	/**
	 * Enter a parse tree produced by the {@code modulusExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#mathExpression}.
	 * @param ctx the parse tree
	 */
	void enterModulusExpression(ExpressionEvaluatorParser.ModulusExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code modulusExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#mathExpression}.
	 * @param ctx the parse tree
	 */
	void exitModulusExpression(ExpressionEvaluatorParser.ModulusExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code mathParenthesis}
	 * labeled alternative in {@link ExpressionEvaluatorParser#mathExpression}.
	 * @param ctx the parse tree
	 */
	void enterMathParenthesis(ExpressionEvaluatorParser.MathParenthesisContext ctx);
	/**
	 * Exit a parse tree produced by the {@code mathParenthesis}
	 * labeled alternative in {@link ExpressionEvaluatorParser#mathExpression}.
	 * @param ctx the parse tree
	 */
	void exitMathParenthesis(ExpressionEvaluatorParser.MathParenthesisContext ctx);
	/**
	 * Enter a parse tree produced by the {@code multiplicationExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#mathExpression}.
	 * @param ctx the parse tree
	 */
	void enterMultiplicationExpression(ExpressionEvaluatorParser.MultiplicationExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code multiplicationExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#mathExpression}.
	 * @param ctx the parse tree
	 */
	void exitMultiplicationExpression(ExpressionEvaluatorParser.MultiplicationExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code mathSpecificExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#mathExpression}.
	 * @param ctx the parse tree
	 */
	void enterMathSpecificExpression(ExpressionEvaluatorParser.MathSpecificExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code mathSpecificExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#mathExpression}.
	 * @param ctx the parse tree
	 */
	void exitMathSpecificExpression(ExpressionEvaluatorParser.MathSpecificExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code factorialExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#mathExpression}.
	 * @param ctx the parse tree
	 */
	void enterFactorialExpression(ExpressionEvaluatorParser.FactorialExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code factorialExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#mathExpression}.
	 * @param ctx the parse tree
	 */
	void exitFactorialExpression(ExpressionEvaluatorParser.FactorialExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code negateMathParenthesis}
	 * labeled alternative in {@link ExpressionEvaluatorParser#mathExpression}.
	 * @param ctx the parse tree
	 */
	void enterNegateMathParenthesis(ExpressionEvaluatorParser.NegateMathParenthesisContext ctx);
	/**
	 * Exit a parse tree produced by the {@code negateMathParenthesis}
	 * labeled alternative in {@link ExpressionEvaluatorParser#mathExpression}.
	 * @param ctx the parse tree
	 */
	void exitNegateMathParenthesis(ExpressionEvaluatorParser.NegateMathParenthesisContext ctx);
	/**
	 * Enter a parse tree produced by the {@code squareRootExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#mathExpression}.
	 * @param ctx the parse tree
	 */
	void enterSquareRootExpression(ExpressionEvaluatorParser.SquareRootExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code squareRootExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#mathExpression}.
	 * @param ctx the parse tree
	 */
	void exitSquareRootExpression(ExpressionEvaluatorParser.SquareRootExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code percentExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#mathExpression}.
	 * @param ctx the parse tree
	 */
	void enterPercentExpression(ExpressionEvaluatorParser.PercentExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code percentExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#mathExpression}.
	 * @param ctx the parse tree
	 */
	void exitPercentExpression(ExpressionEvaluatorParser.PercentExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code rootExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#mathExpression}.
	 * @param ctx the parse tree
	 */
	void enterRootExpression(ExpressionEvaluatorParser.RootExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code rootExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#mathExpression}.
	 * @param ctx the parse tree
	 */
	void exitRootExpression(ExpressionEvaluatorParser.RootExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code sumExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#mathExpression}.
	 * @param ctx the parse tree
	 */
	void enterSumExpression(ExpressionEvaluatorParser.SumExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code sumExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#mathExpression}.
	 * @param ctx the parse tree
	 */
	void exitSumExpression(ExpressionEvaluatorParser.SumExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code degreeExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#mathExpression}.
	 * @param ctx the parse tree
	 */
	void enterDegreeExpression(ExpressionEvaluatorParser.DegreeExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code degreeExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#mathExpression}.
	 * @param ctx the parse tree
	 */
	void exitDegreeExpression(ExpressionEvaluatorParser.DegreeExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code numberValue}
	 * labeled alternative in {@link ExpressionEvaluatorParser#mathExpression}.
	 * @param ctx the parse tree
	 */
	void enterNumberValue(ExpressionEvaluatorParser.NumberValueContext ctx);
	/**
	 * Exit a parse tree produced by the {@code numberValue}
	 * labeled alternative in {@link ExpressionEvaluatorParser#mathExpression}.
	 * @param ctx the parse tree
	 */
	void exitNumberValue(ExpressionEvaluatorParser.NumberValueContext ctx);
	/**
	 * Enter a parse tree produced by the {@code exponentiationExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#mathExpression}.
	 * @param ctx the parse tree
	 */
	void enterExponentiationExpression(ExpressionEvaluatorParser.ExponentiationExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code exponentiationExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#mathExpression}.
	 * @param ctx the parse tree
	 */
	void exitExponentiationExpression(ExpressionEvaluatorParser.ExponentiationExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code logarithmExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#mathSpecificFunction}.
	 * @param ctx the parse tree
	 */
	void enterLogarithmExpression(ExpressionEvaluatorParser.LogarithmExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code logarithmExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#mathSpecificFunction}.
	 * @param ctx the parse tree
	 */
	void exitLogarithmExpression(ExpressionEvaluatorParser.LogarithmExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code roundingExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#mathSpecificFunction}.
	 * @param ctx the parse tree
	 */
	void enterRoundingExpression(ExpressionEvaluatorParser.RoundingExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code roundingExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#mathSpecificFunction}.
	 * @param ctx the parse tree
	 */
	void exitRoundingExpression(ExpressionEvaluatorParser.RoundingExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code sequenceExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#mathSpecificFunction}.
	 * @param ctx the parse tree
	 */
	void enterSequenceExpression(ExpressionEvaluatorParser.SequenceExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code sequenceExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#mathSpecificFunction}.
	 * @param ctx the parse tree
	 */
	void exitSequenceExpression(ExpressionEvaluatorParser.SequenceExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code fixedLogarithm}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logarithmFunction}.
	 * @param ctx the parse tree
	 */
	void enterFixedLogarithm(ExpressionEvaluatorParser.FixedLogarithmContext ctx);
	/**
	 * Exit a parse tree produced by the {@code fixedLogarithm}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logarithmFunction}.
	 * @param ctx the parse tree
	 */
	void exitFixedLogarithm(ExpressionEvaluatorParser.FixedLogarithmContext ctx);
	/**
	 * Enter a parse tree produced by the {@code variableLogarithm}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logarithmFunction}.
	 * @param ctx the parse tree
	 */
	void enterVariableLogarithm(ExpressionEvaluatorParser.VariableLogarithmContext ctx);
	/**
	 * Exit a parse tree produced by the {@code variableLogarithm}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logarithmFunction}.
	 * @param ctx the parse tree
	 */
	void exitVariableLogarithm(ExpressionEvaluatorParser.VariableLogarithmContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExpressionEvaluatorParser#roundingFunction}.
	 * @param ctx the parse tree
	 */
	void enterRoundingFunction(ExpressionEvaluatorParser.RoundingFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExpressionEvaluatorParser#roundingFunction}.
	 * @param ctx the parse tree
	 */
	void exitRoundingFunction(ExpressionEvaluatorParser.RoundingFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExpressionEvaluatorParser#sequenceFunction}.
	 * @param ctx the parse tree
	 */
	void enterSequenceFunction(ExpressionEvaluatorParser.SequenceFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExpressionEvaluatorParser#sequenceFunction}.
	 * @param ctx the parse tree
	 */
	void exitSequenceFunction(ExpressionEvaluatorParser.SequenceFunctionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateParenthesis}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateOperation}.
	 * @param ctx the parse tree
	 */
	void enterDateParenthesis(ExpressionEvaluatorParser.DateParenthesisContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateParenthesis}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateOperation}.
	 * @param ctx the parse tree
	 */
	void exitDateParenthesis(ExpressionEvaluatorParser.DateParenthesisContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateFunction}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateOperation}.
	 * @param ctx the parse tree
	 */
	void enterDateFunction(ExpressionEvaluatorParser.DateFunctionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateFunction}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateOperation}.
	 * @param ctx the parse tree
	 */
	void exitDateFunction(ExpressionEvaluatorParser.DateFunctionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code timeParenthesis}
	 * labeled alternative in {@link ExpressionEvaluatorParser#timeOperation}.
	 * @param ctx the parse tree
	 */
	void enterTimeParenthesis(ExpressionEvaluatorParser.TimeParenthesisContext ctx);
	/**
	 * Exit a parse tree produced by the {@code timeParenthesis}
	 * labeled alternative in {@link ExpressionEvaluatorParser#timeOperation}.
	 * @param ctx the parse tree
	 */
	void exitTimeParenthesis(ExpressionEvaluatorParser.TimeParenthesisContext ctx);
	/**
	 * Enter a parse tree produced by the {@code timeFunction}
	 * labeled alternative in {@link ExpressionEvaluatorParser#timeOperation}.
	 * @param ctx the parse tree
	 */
	void enterTimeFunction(ExpressionEvaluatorParser.TimeFunctionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code timeFunction}
	 * labeled alternative in {@link ExpressionEvaluatorParser#timeOperation}.
	 * @param ctx the parse tree
	 */
	void exitTimeFunction(ExpressionEvaluatorParser.TimeFunctionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateTimeParenthesis}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateTimeOperation}.
	 * @param ctx the parse tree
	 */
	void enterDateTimeParenthesis(ExpressionEvaluatorParser.DateTimeParenthesisContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateTimeParenthesis}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateTimeOperation}.
	 * @param ctx the parse tree
	 */
	void exitDateTimeParenthesis(ExpressionEvaluatorParser.DateTimeParenthesisContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateTimeFunction}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateTimeOperation}.
	 * @param ctx the parse tree
	 */
	void enterDateTimeFunction(ExpressionEvaluatorParser.DateTimeFunctionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateTimeFunction}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateTimeOperation}.
	 * @param ctx the parse tree
	 */
	void exitDateTimeFunction(ExpressionEvaluatorParser.DateTimeFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExpressionEvaluatorParser#function}.
	 * @param ctx the parse tree
	 */
	void enterFunction(ExpressionEvaluatorParser.FunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExpressionEvaluatorParser#function}.
	 * @param ctx the parse tree
	 */
	void exitFunction(ExpressionEvaluatorParser.FunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExpressionEvaluatorParser#comparisonOperator}.
	 * @param ctx the parse tree
	 */
	void enterComparisonOperator(ExpressionEvaluatorParser.ComparisonOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExpressionEvaluatorParser#comparisonOperator}.
	 * @param ctx the parse tree
	 */
	void exitComparisonOperator(ExpressionEvaluatorParser.ComparisonOperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExpressionEvaluatorParser#logicalOperator}.
	 * @param ctx the parse tree
	 */
	void enterLogicalOperator(ExpressionEvaluatorParser.LogicalOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExpressionEvaluatorParser#logicalOperator}.
	 * @param ctx the parse tree
	 */
	void exitLogicalOperator(ExpressionEvaluatorParser.LogicalOperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExpressionEvaluatorParser#allEntityTypes}.
	 * @param ctx the parse tree
	 */
	void enterAllEntityTypes(ExpressionEvaluatorParser.AllEntityTypesContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExpressionEvaluatorParser#allEntityTypes}.
	 * @param ctx the parse tree
	 */
	void exitAllEntityTypes(ExpressionEvaluatorParser.AllEntityTypesContext ctx);
	/**
	 * Enter a parse tree produced by the {@code logicalConstant}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalEntity}.
	 * @param ctx the parse tree
	 */
	void enterLogicalConstant(ExpressionEvaluatorParser.LogicalConstantContext ctx);
	/**
	 * Exit a parse tree produced by the {@code logicalConstant}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalEntity}.
	 * @param ctx the parse tree
	 */
	void exitLogicalConstant(ExpressionEvaluatorParser.LogicalConstantContext ctx);
	/**
	 * Enter a parse tree produced by the {@code logicalDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalEntity}.
	 * @param ctx the parse tree
	 */
	void enterLogicalDecisionExpression(ExpressionEvaluatorParser.LogicalDecisionExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code logicalDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalEntity}.
	 * @param ctx the parse tree
	 */
	void exitLogicalDecisionExpression(ExpressionEvaluatorParser.LogicalDecisionExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code logicalFunctionDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalEntity}.
	 * @param ctx the parse tree
	 */
	void enterLogicalFunctionDecisionExpression(ExpressionEvaluatorParser.LogicalFunctionDecisionExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code logicalFunctionDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalEntity}.
	 * @param ctx the parse tree
	 */
	void exitLogicalFunctionDecisionExpression(ExpressionEvaluatorParser.LogicalFunctionDecisionExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code logicalFunctionResult}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalEntity}.
	 * @param ctx the parse tree
	 */
	void enterLogicalFunctionResult(ExpressionEvaluatorParser.LogicalFunctionResultContext ctx);
	/**
	 * Exit a parse tree produced by the {@code logicalFunctionResult}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalEntity}.
	 * @param ctx the parse tree
	 */
	void exitLogicalFunctionResult(ExpressionEvaluatorParser.LogicalFunctionResultContext ctx);
	/**
	 * Enter a parse tree produced by the {@code logicalVariable}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalEntity}.
	 * @param ctx the parse tree
	 */
	void enterLogicalVariable(ExpressionEvaluatorParser.LogicalVariableContext ctx);
	/**
	 * Exit a parse tree produced by the {@code logicalVariable}
	 * labeled alternative in {@link ExpressionEvaluatorParser#logicalEntity}.
	 * @param ctx the parse tree
	 */
	void exitLogicalVariable(ExpressionEvaluatorParser.LogicalVariableContext ctx);
	/**
	 * Enter a parse tree produced by the {@code mathDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#numericEntity}.
	 * @param ctx the parse tree
	 */
	void enterMathDecisionExpression(ExpressionEvaluatorParser.MathDecisionExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code mathDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#numericEntity}.
	 * @param ctx the parse tree
	 */
	void exitMathDecisionExpression(ExpressionEvaluatorParser.MathDecisionExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code mathFunctionDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#numericEntity}.
	 * @param ctx the parse tree
	 */
	void enterMathFunctionDecisionExpression(ExpressionEvaluatorParser.MathFunctionDecisionExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code mathFunctionDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#numericEntity}.
	 * @param ctx the parse tree
	 */
	void exitMathFunctionDecisionExpression(ExpressionEvaluatorParser.MathFunctionDecisionExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code eulerConstant}
	 * labeled alternative in {@link ExpressionEvaluatorParser#numericEntity}.
	 * @param ctx the parse tree
	 */
	void enterEulerConstant(ExpressionEvaluatorParser.EulerConstantContext ctx);
	/**
	 * Exit a parse tree produced by the {@code eulerConstant}
	 * labeled alternative in {@link ExpressionEvaluatorParser#numericEntity}.
	 * @param ctx the parse tree
	 */
	void exitEulerConstant(ExpressionEvaluatorParser.EulerConstantContext ctx);
	/**
	 * Enter a parse tree produced by the {@code piConstant}
	 * labeled alternative in {@link ExpressionEvaluatorParser#numericEntity}.
	 * @param ctx the parse tree
	 */
	void enterPiConstant(ExpressionEvaluatorParser.PiConstantContext ctx);
	/**
	 * Exit a parse tree produced by the {@code piConstant}
	 * labeled alternative in {@link ExpressionEvaluatorParser#numericEntity}.
	 * @param ctx the parse tree
	 */
	void exitPiConstant(ExpressionEvaluatorParser.PiConstantContext ctx);
	/**
	 * Enter a parse tree produced by the {@code summationVariable}
	 * labeled alternative in {@link ExpressionEvaluatorParser#numericEntity}.
	 * @param ctx the parse tree
	 */
	void enterSummationVariable(ExpressionEvaluatorParser.SummationVariableContext ctx);
	/**
	 * Exit a parse tree produced by the {@code summationVariable}
	 * labeled alternative in {@link ExpressionEvaluatorParser#numericEntity}.
	 * @param ctx the parse tree
	 */
	void exitSummationVariable(ExpressionEvaluatorParser.SummationVariableContext ctx);
	/**
	 * Enter a parse tree produced by the {@code productSequenceVariable}
	 * labeled alternative in {@link ExpressionEvaluatorParser#numericEntity}.
	 * @param ctx the parse tree
	 */
	void enterProductSequenceVariable(ExpressionEvaluatorParser.ProductSequenceVariableContext ctx);
	/**
	 * Exit a parse tree produced by the {@code productSequenceVariable}
	 * labeled alternative in {@link ExpressionEvaluatorParser#numericEntity}.
	 * @param ctx the parse tree
	 */
	void exitProductSequenceVariable(ExpressionEvaluatorParser.ProductSequenceVariableContext ctx);
	/**
	 * Enter a parse tree produced by the {@code numericConstant}
	 * labeled alternative in {@link ExpressionEvaluatorParser#numericEntity}.
	 * @param ctx the parse tree
	 */
	void enterNumericConstant(ExpressionEvaluatorParser.NumericConstantContext ctx);
	/**
	 * Exit a parse tree produced by the {@code numericConstant}
	 * labeled alternative in {@link ExpressionEvaluatorParser#numericEntity}.
	 * @param ctx the parse tree
	 */
	void exitNumericConstant(ExpressionEvaluatorParser.NumericConstantContext ctx);
	/**
	 * Enter a parse tree produced by the {@code numericFunctionResult}
	 * labeled alternative in {@link ExpressionEvaluatorParser#numericEntity}.
	 * @param ctx the parse tree
	 */
	void enterNumericFunctionResult(ExpressionEvaluatorParser.NumericFunctionResultContext ctx);
	/**
	 * Exit a parse tree produced by the {@code numericFunctionResult}
	 * labeled alternative in {@link ExpressionEvaluatorParser#numericEntity}.
	 * @param ctx the parse tree
	 */
	void exitNumericFunctionResult(ExpressionEvaluatorParser.NumericFunctionResultContext ctx);
	/**
	 * Enter a parse tree produced by the {@code numericVariable}
	 * labeled alternative in {@link ExpressionEvaluatorParser#numericEntity}.
	 * @param ctx the parse tree
	 */
	void enterNumericVariable(ExpressionEvaluatorParser.NumericVariableContext ctx);
	/**
	 * Exit a parse tree produced by the {@code numericVariable}
	 * labeled alternative in {@link ExpressionEvaluatorParser#numericEntity}.
	 * @param ctx the parse tree
	 */
	void exitNumericVariable(ExpressionEvaluatorParser.NumericVariableContext ctx);
	/**
	 * Enter a parse tree produced by the {@code stringDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#stringEntity}.
	 * @param ctx the parse tree
	 */
	void enterStringDecisionExpression(ExpressionEvaluatorParser.StringDecisionExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code stringDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#stringEntity}.
	 * @param ctx the parse tree
	 */
	void exitStringDecisionExpression(ExpressionEvaluatorParser.StringDecisionExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code stringFunctionDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#stringEntity}.
	 * @param ctx the parse tree
	 */
	void enterStringFunctionDecisionExpression(ExpressionEvaluatorParser.StringFunctionDecisionExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code stringFunctionDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#stringEntity}.
	 * @param ctx the parse tree
	 */
	void exitStringFunctionDecisionExpression(ExpressionEvaluatorParser.StringFunctionDecisionExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code stringConstant}
	 * labeled alternative in {@link ExpressionEvaluatorParser#stringEntity}.
	 * @param ctx the parse tree
	 */
	void enterStringConstant(ExpressionEvaluatorParser.StringConstantContext ctx);
	/**
	 * Exit a parse tree produced by the {@code stringConstant}
	 * labeled alternative in {@link ExpressionEvaluatorParser#stringEntity}.
	 * @param ctx the parse tree
	 */
	void exitStringConstant(ExpressionEvaluatorParser.StringConstantContext ctx);
	/**
	 * Enter a parse tree produced by the {@code stringFunctionResult}
	 * labeled alternative in {@link ExpressionEvaluatorParser#stringEntity}.
	 * @param ctx the parse tree
	 */
	void enterStringFunctionResult(ExpressionEvaluatorParser.StringFunctionResultContext ctx);
	/**
	 * Exit a parse tree produced by the {@code stringFunctionResult}
	 * labeled alternative in {@link ExpressionEvaluatorParser#stringEntity}.
	 * @param ctx the parse tree
	 */
	void exitStringFunctionResult(ExpressionEvaluatorParser.StringFunctionResultContext ctx);
	/**
	 * Enter a parse tree produced by the {@code stringVariable}
	 * labeled alternative in {@link ExpressionEvaluatorParser#stringEntity}.
	 * @param ctx the parse tree
	 */
	void enterStringVariable(ExpressionEvaluatorParser.StringVariableContext ctx);
	/**
	 * Exit a parse tree produced by the {@code stringVariable}
	 * labeled alternative in {@link ExpressionEvaluatorParser#stringEntity}.
	 * @param ctx the parse tree
	 */
	void exitStringVariable(ExpressionEvaluatorParser.StringVariableContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateEntity}.
	 * @param ctx the parse tree
	 */
	void enterDateDecisionExpression(ExpressionEvaluatorParser.DateDecisionExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateEntity}.
	 * @param ctx the parse tree
	 */
	void exitDateDecisionExpression(ExpressionEvaluatorParser.DateDecisionExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateFunctionDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateEntity}.
	 * @param ctx the parse tree
	 */
	void enterDateFunctionDecisionExpression(ExpressionEvaluatorParser.DateFunctionDecisionExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateFunctionDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateEntity}.
	 * @param ctx the parse tree
	 */
	void exitDateFunctionDecisionExpression(ExpressionEvaluatorParser.DateFunctionDecisionExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateConstant}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateEntity}.
	 * @param ctx the parse tree
	 */
	void enterDateConstant(ExpressionEvaluatorParser.DateConstantContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateConstant}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateEntity}.
	 * @param ctx the parse tree
	 */
	void exitDateConstant(ExpressionEvaluatorParser.DateConstantContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateCurrentValue}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateEntity}.
	 * @param ctx the parse tree
	 */
	void enterDateCurrentValue(ExpressionEvaluatorParser.DateCurrentValueContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateCurrentValue}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateEntity}.
	 * @param ctx the parse tree
	 */
	void exitDateCurrentValue(ExpressionEvaluatorParser.DateCurrentValueContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateVariable}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateEntity}.
	 * @param ctx the parse tree
	 */
	void enterDateVariable(ExpressionEvaluatorParser.DateVariableContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateVariable}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateEntity}.
	 * @param ctx the parse tree
	 */
	void exitDateVariable(ExpressionEvaluatorParser.DateVariableContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateFunctionResult}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateEntity}.
	 * @param ctx the parse tree
	 */
	void enterDateFunctionResult(ExpressionEvaluatorParser.DateFunctionResultContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateFunctionResult}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateEntity}.
	 * @param ctx the parse tree
	 */
	void exitDateFunctionResult(ExpressionEvaluatorParser.DateFunctionResultContext ctx);
	/**
	 * Enter a parse tree produced by the {@code timeDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#timeEntity}.
	 * @param ctx the parse tree
	 */
	void enterTimeDecisionExpression(ExpressionEvaluatorParser.TimeDecisionExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code timeDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#timeEntity}.
	 * @param ctx the parse tree
	 */
	void exitTimeDecisionExpression(ExpressionEvaluatorParser.TimeDecisionExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code timeFunctionDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#timeEntity}.
	 * @param ctx the parse tree
	 */
	void enterTimeFunctionDecisionExpression(ExpressionEvaluatorParser.TimeFunctionDecisionExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code timeFunctionDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#timeEntity}.
	 * @param ctx the parse tree
	 */
	void exitTimeFunctionDecisionExpression(ExpressionEvaluatorParser.TimeFunctionDecisionExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code timeConstant}
	 * labeled alternative in {@link ExpressionEvaluatorParser#timeEntity}.
	 * @param ctx the parse tree
	 */
	void enterTimeConstant(ExpressionEvaluatorParser.TimeConstantContext ctx);
	/**
	 * Exit a parse tree produced by the {@code timeConstant}
	 * labeled alternative in {@link ExpressionEvaluatorParser#timeEntity}.
	 * @param ctx the parse tree
	 */
	void exitTimeConstant(ExpressionEvaluatorParser.TimeConstantContext ctx);
	/**
	 * Enter a parse tree produced by the {@code timeCurrentValue}
	 * labeled alternative in {@link ExpressionEvaluatorParser#timeEntity}.
	 * @param ctx the parse tree
	 */
	void enterTimeCurrentValue(ExpressionEvaluatorParser.TimeCurrentValueContext ctx);
	/**
	 * Exit a parse tree produced by the {@code timeCurrentValue}
	 * labeled alternative in {@link ExpressionEvaluatorParser#timeEntity}.
	 * @param ctx the parse tree
	 */
	void exitTimeCurrentValue(ExpressionEvaluatorParser.TimeCurrentValueContext ctx);
	/**
	 * Enter a parse tree produced by the {@code timeVariable}
	 * labeled alternative in {@link ExpressionEvaluatorParser#timeEntity}.
	 * @param ctx the parse tree
	 */
	void enterTimeVariable(ExpressionEvaluatorParser.TimeVariableContext ctx);
	/**
	 * Exit a parse tree produced by the {@code timeVariable}
	 * labeled alternative in {@link ExpressionEvaluatorParser#timeEntity}.
	 * @param ctx the parse tree
	 */
	void exitTimeVariable(ExpressionEvaluatorParser.TimeVariableContext ctx);
	/**
	 * Enter a parse tree produced by the {@code timeFunctionResult}
	 * labeled alternative in {@link ExpressionEvaluatorParser#timeEntity}.
	 * @param ctx the parse tree
	 */
	void enterTimeFunctionResult(ExpressionEvaluatorParser.TimeFunctionResultContext ctx);
	/**
	 * Exit a parse tree produced by the {@code timeFunctionResult}
	 * labeled alternative in {@link ExpressionEvaluatorParser#timeEntity}.
	 * @param ctx the parse tree
	 */
	void exitTimeFunctionResult(ExpressionEvaluatorParser.TimeFunctionResultContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateTimeDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateTimeEntity}.
	 * @param ctx the parse tree
	 */
	void enterDateTimeDecisionExpression(ExpressionEvaluatorParser.DateTimeDecisionExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateTimeDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateTimeEntity}.
	 * @param ctx the parse tree
	 */
	void exitDateTimeDecisionExpression(ExpressionEvaluatorParser.DateTimeDecisionExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateTimeFunctionDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateTimeEntity}.
	 * @param ctx the parse tree
	 */
	void enterDateTimeFunctionDecisionExpression(ExpressionEvaluatorParser.DateTimeFunctionDecisionExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateTimeFunctionDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateTimeEntity}.
	 * @param ctx the parse tree
	 */
	void exitDateTimeFunctionDecisionExpression(ExpressionEvaluatorParser.DateTimeFunctionDecisionExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateTimeConstant}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateTimeEntity}.
	 * @param ctx the parse tree
	 */
	void enterDateTimeConstant(ExpressionEvaluatorParser.DateTimeConstantContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateTimeConstant}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateTimeEntity}.
	 * @param ctx the parse tree
	 */
	void exitDateTimeConstant(ExpressionEvaluatorParser.DateTimeConstantContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateTimeCurrentValue}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateTimeEntity}.
	 * @param ctx the parse tree
	 */
	void enterDateTimeCurrentValue(ExpressionEvaluatorParser.DateTimeCurrentValueContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateTimeCurrentValue}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateTimeEntity}.
	 * @param ctx the parse tree
	 */
	void exitDateTimeCurrentValue(ExpressionEvaluatorParser.DateTimeCurrentValueContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateTimeVariable}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateTimeEntity}.
	 * @param ctx the parse tree
	 */
	void enterDateTimeVariable(ExpressionEvaluatorParser.DateTimeVariableContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateTimeVariable}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateTimeEntity}.
	 * @param ctx the parse tree
	 */
	void exitDateTimeVariable(ExpressionEvaluatorParser.DateTimeVariableContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateTimeFunctionResult}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateTimeEntity}.
	 * @param ctx the parse tree
	 */
	void enterDateTimeFunctionResult(ExpressionEvaluatorParser.DateTimeFunctionResultContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateTimeFunctionResult}
	 * labeled alternative in {@link ExpressionEvaluatorParser#dateTimeEntity}.
	 * @param ctx the parse tree
	 */
	void exitDateTimeFunctionResult(ExpressionEvaluatorParser.DateTimeFunctionResultContext ctx);
	/**
	 * Enter a parse tree produced by the {@code vectorOfEntities}
	 * labeled alternative in {@link ExpressionEvaluatorParser#vectorEntity}.
	 * @param ctx the parse tree
	 */
	void enterVectorOfEntities(ExpressionEvaluatorParser.VectorOfEntitiesContext ctx);
	/**
	 * Exit a parse tree produced by the {@code vectorOfEntities}
	 * labeled alternative in {@link ExpressionEvaluatorParser#vectorEntity}.
	 * @param ctx the parse tree
	 */
	void exitVectorOfEntities(ExpressionEvaluatorParser.VectorOfEntitiesContext ctx);
	/**
	 * Enter a parse tree produced by the {@code vectorVariable}
	 * labeled alternative in {@link ExpressionEvaluatorParser#vectorEntity}.
	 * @param ctx the parse tree
	 */
	void enterVectorVariable(ExpressionEvaluatorParser.VectorVariableContext ctx);
	/**
	 * Exit a parse tree produced by the {@code vectorVariable}
	 * labeled alternative in {@link ExpressionEvaluatorParser#vectorEntity}.
	 * @param ctx the parse tree
	 */
	void exitVectorVariable(ExpressionEvaluatorParser.VectorVariableContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExpressionEvaluatorParser#vectorOfVariables}.
	 * @param ctx the parse tree
	 */
	void enterVectorOfVariables(ExpressionEvaluatorParser.VectorOfVariablesContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExpressionEvaluatorParser#vectorOfVariables}.
	 * @param ctx the parse tree
	 */
	void exitVectorOfVariables(ExpressionEvaluatorParser.VectorOfVariablesContext ctx);
}