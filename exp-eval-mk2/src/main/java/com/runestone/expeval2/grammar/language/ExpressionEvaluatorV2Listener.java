// Generated from /home/marcelo/dev/git/runestone-forge/exp-eval-mk2/src/main/resources/ExpressionEvaluatorV2.g4 by ANTLR 4.13.1

package com.runestone.expeval2.grammar.language;

import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link ExpressionEvaluatorV2Parser}.
 */
public interface ExpressionEvaluatorV2Listener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link ExpressionEvaluatorV2Parser#startAssignments}.
	 * @param ctx the parse tree
	 */
	void enterStartAssignments(ExpressionEvaluatorV2Parser.StartAssignmentsContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExpressionEvaluatorV2Parser#startAssignments}.
	 * @param ctx the parse tree
	 */
	void exitStartAssignments(ExpressionEvaluatorV2Parser.StartAssignmentsContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExpressionEvaluatorV2Parser#mathStart}.
	 * @param ctx the parse tree
	 */
	void enterMathStart(ExpressionEvaluatorV2Parser.MathStartContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExpressionEvaluatorV2Parser#mathStart}.
	 * @param ctx the parse tree
	 */
	void exitMathStart(ExpressionEvaluatorV2Parser.MathStartContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExpressionEvaluatorV2Parser#logicalStart}.
	 * @param ctx the parse tree
	 */
	void enterLogicalStart(ExpressionEvaluatorV2Parser.LogicalStartContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExpressionEvaluatorV2Parser#logicalStart}.
	 * @param ctx the parse tree
	 */
	void exitLogicalStart(ExpressionEvaluatorV2Parser.LogicalStartContext ctx);
	/**
	 * Enter a parse tree produced by the {@code assignOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#assignmentExpression}.
	 * @param ctx the parse tree
	 */
	void enterAssignOperation(ExpressionEvaluatorV2Parser.AssignOperationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code assignOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#assignmentExpression}.
	 * @param ctx the parse tree
	 */
	void exitAssignOperation(ExpressionEvaluatorV2Parser.AssignOperationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code destructuringAssignment}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#assignmentExpression}.
	 * @param ctx the parse tree
	 */
	void enterDestructuringAssignment(ExpressionEvaluatorV2Parser.DestructuringAssignmentContext ctx);
	/**
	 * Exit a parse tree produced by the {@code destructuringAssignment}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#assignmentExpression}.
	 * @param ctx the parse tree
	 */
	void exitDestructuringAssignment(ExpressionEvaluatorV2Parser.DestructuringAssignmentContext ctx);
	/**
	 * Enter a parse tree produced by the {@code orExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalExpression}.
	 * @param ctx the parse tree
	 */
	void enterOrExpression(ExpressionEvaluatorV2Parser.OrExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code orExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalExpression}.
	 * @param ctx the parse tree
	 */
	void exitOrExpression(ExpressionEvaluatorV2Parser.OrExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateTimeExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalExpression}.
	 * @param ctx the parse tree
	 */
	void enterDateTimeExpression(ExpressionEvaluatorV2Parser.DateTimeExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateTimeExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalExpression}.
	 * @param ctx the parse tree
	 */
	void exitDateTimeExpression(ExpressionEvaluatorV2Parser.DateTimeExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code andExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalExpression}.
	 * @param ctx the parse tree
	 */
	void enterAndExpression(ExpressionEvaluatorV2Parser.AndExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code andExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalExpression}.
	 * @param ctx the parse tree
	 */
	void exitAndExpression(ExpressionEvaluatorV2Parser.AndExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code stringExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalExpression}.
	 * @param ctx the parse tree
	 */
	void enterStringExpression(ExpressionEvaluatorV2Parser.StringExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code stringExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalExpression}.
	 * @param ctx the parse tree
	 */
	void exitStringExpression(ExpressionEvaluatorV2Parser.StringExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code logicalValue}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalExpression}.
	 * @param ctx the parse tree
	 */
	void enterLogicalValue(ExpressionEvaluatorV2Parser.LogicalValueContext ctx);
	/**
	 * Exit a parse tree produced by the {@code logicalValue}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalExpression}.
	 * @param ctx the parse tree
	 */
	void exitLogicalValue(ExpressionEvaluatorV2Parser.LogicalValueContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalExpression}.
	 * @param ctx the parse tree
	 */
	void enterDateExpression(ExpressionEvaluatorV2Parser.DateExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalExpression}.
	 * @param ctx the parse tree
	 */
	void exitDateExpression(ExpressionEvaluatorV2Parser.DateExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code notExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalExpression}.
	 * @param ctx the parse tree
	 */
	void enterNotExpression(ExpressionEvaluatorV2Parser.NotExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code notExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalExpression}.
	 * @param ctx the parse tree
	 */
	void exitNotExpression(ExpressionEvaluatorV2Parser.NotExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code logicComparisonExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalExpression}.
	 * @param ctx the parse tree
	 */
	void enterLogicComparisonExpression(ExpressionEvaluatorV2Parser.LogicComparisonExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code logicComparisonExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalExpression}.
	 * @param ctx the parse tree
	 */
	void exitLogicComparisonExpression(ExpressionEvaluatorV2Parser.LogicComparisonExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code comparisonMathExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalExpression}.
	 * @param ctx the parse tree
	 */
	void enterComparisonMathExpression(ExpressionEvaluatorV2Parser.ComparisonMathExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code comparisonMathExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalExpression}.
	 * @param ctx the parse tree
	 */
	void exitComparisonMathExpression(ExpressionEvaluatorV2Parser.ComparisonMathExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code timeExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalExpression}.
	 * @param ctx the parse tree
	 */
	void enterTimeExpression(ExpressionEvaluatorV2Parser.TimeExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code timeExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalExpression}.
	 * @param ctx the parse tree
	 */
	void exitTimeExpression(ExpressionEvaluatorV2Parser.TimeExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code bitwiseLogicExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalExpression}.
	 * @param ctx the parse tree
	 */
	void enterBitwiseLogicExpression(ExpressionEvaluatorV2Parser.BitwiseLogicExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code bitwiseLogicExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalExpression}.
	 * @param ctx the parse tree
	 */
	void exitBitwiseLogicExpression(ExpressionEvaluatorV2Parser.BitwiseLogicExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code logicalParenthesis}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalExpression}.
	 * @param ctx the parse tree
	 */
	void enterLogicalParenthesis(ExpressionEvaluatorV2Parser.LogicalParenthesisContext ctx);
	/**
	 * Exit a parse tree produced by the {@code logicalParenthesis}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalExpression}.
	 * @param ctx the parse tree
	 */
	void exitLogicalParenthesis(ExpressionEvaluatorV2Parser.LogicalParenthesisContext ctx);
	/**
	 * Enter a parse tree produced by the {@code modulusExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#mathExpression}.
	 * @param ctx the parse tree
	 */
	void enterModulusExpression(ExpressionEvaluatorV2Parser.ModulusExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code modulusExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#mathExpression}.
	 * @param ctx the parse tree
	 */
	void exitModulusExpression(ExpressionEvaluatorV2Parser.ModulusExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code mathParenthesis}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#mathExpression}.
	 * @param ctx the parse tree
	 */
	void enterMathParenthesis(ExpressionEvaluatorV2Parser.MathParenthesisContext ctx);
	/**
	 * Exit a parse tree produced by the {@code mathParenthesis}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#mathExpression}.
	 * @param ctx the parse tree
	 */
	void exitMathParenthesis(ExpressionEvaluatorV2Parser.MathParenthesisContext ctx);
	/**
	 * Enter a parse tree produced by the {@code multiplicationExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#mathExpression}.
	 * @param ctx the parse tree
	 */
	void enterMultiplicationExpression(ExpressionEvaluatorV2Parser.MultiplicationExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code multiplicationExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#mathExpression}.
	 * @param ctx the parse tree
	 */
	void exitMultiplicationExpression(ExpressionEvaluatorV2Parser.MultiplicationExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code mathSpecificExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#mathExpression}.
	 * @param ctx the parse tree
	 */
	void enterMathSpecificExpression(ExpressionEvaluatorV2Parser.MathSpecificExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code mathSpecificExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#mathExpression}.
	 * @param ctx the parse tree
	 */
	void exitMathSpecificExpression(ExpressionEvaluatorV2Parser.MathSpecificExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code factorialExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#mathExpression}.
	 * @param ctx the parse tree
	 */
	void enterFactorialExpression(ExpressionEvaluatorV2Parser.FactorialExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code factorialExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#mathExpression}.
	 * @param ctx the parse tree
	 */
	void exitFactorialExpression(ExpressionEvaluatorV2Parser.FactorialExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code negateMathParenthesis}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#mathExpression}.
	 * @param ctx the parse tree
	 */
	void enterNegateMathParenthesis(ExpressionEvaluatorV2Parser.NegateMathParenthesisContext ctx);
	/**
	 * Exit a parse tree produced by the {@code negateMathParenthesis}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#mathExpression}.
	 * @param ctx the parse tree
	 */
	void exitNegateMathParenthesis(ExpressionEvaluatorV2Parser.NegateMathParenthesisContext ctx);
	/**
	 * Enter a parse tree produced by the {@code squareRootExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#mathExpression}.
	 * @param ctx the parse tree
	 */
	void enterSquareRootExpression(ExpressionEvaluatorV2Parser.SquareRootExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code squareRootExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#mathExpression}.
	 * @param ctx the parse tree
	 */
	void exitSquareRootExpression(ExpressionEvaluatorV2Parser.SquareRootExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code percentExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#mathExpression}.
	 * @param ctx the parse tree
	 */
	void enterPercentExpression(ExpressionEvaluatorV2Parser.PercentExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code percentExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#mathExpression}.
	 * @param ctx the parse tree
	 */
	void exitPercentExpression(ExpressionEvaluatorV2Parser.PercentExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code unaryMinusExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#mathExpression}.
	 * @param ctx the parse tree
	 */
	void enterUnaryMinusExpression(ExpressionEvaluatorV2Parser.UnaryMinusExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code unaryMinusExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#mathExpression}.
	 * @param ctx the parse tree
	 */
	void exitUnaryMinusExpression(ExpressionEvaluatorV2Parser.UnaryMinusExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code rootExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#mathExpression}.
	 * @param ctx the parse tree
	 */
	void enterRootExpression(ExpressionEvaluatorV2Parser.RootExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code rootExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#mathExpression}.
	 * @param ctx the parse tree
	 */
	void exitRootExpression(ExpressionEvaluatorV2Parser.RootExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code sumExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#mathExpression}.
	 * @param ctx the parse tree
	 */
	void enterSumExpression(ExpressionEvaluatorV2Parser.SumExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code sumExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#mathExpression}.
	 * @param ctx the parse tree
	 */
	void exitSumExpression(ExpressionEvaluatorV2Parser.SumExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code degreeExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#mathExpression}.
	 * @param ctx the parse tree
	 */
	void enterDegreeExpression(ExpressionEvaluatorV2Parser.DegreeExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code degreeExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#mathExpression}.
	 * @param ctx the parse tree
	 */
	void exitDegreeExpression(ExpressionEvaluatorV2Parser.DegreeExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code numberValue}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#mathExpression}.
	 * @param ctx the parse tree
	 */
	void enterNumberValue(ExpressionEvaluatorV2Parser.NumberValueContext ctx);
	/**
	 * Exit a parse tree produced by the {@code numberValue}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#mathExpression}.
	 * @param ctx the parse tree
	 */
	void exitNumberValue(ExpressionEvaluatorV2Parser.NumberValueContext ctx);
	/**
	 * Enter a parse tree produced by the {@code exponentiationExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#mathExpression}.
	 * @param ctx the parse tree
	 */
	void enterExponentiationExpression(ExpressionEvaluatorV2Parser.ExponentiationExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code exponentiationExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#mathExpression}.
	 * @param ctx the parse tree
	 */
	void exitExponentiationExpression(ExpressionEvaluatorV2Parser.ExponentiationExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code logarithmExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#mathSpecificFunction}.
	 * @param ctx the parse tree
	 */
	void enterLogarithmExpression(ExpressionEvaluatorV2Parser.LogarithmExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code logarithmExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#mathSpecificFunction}.
	 * @param ctx the parse tree
	 */
	void exitLogarithmExpression(ExpressionEvaluatorV2Parser.LogarithmExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code roundingExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#mathSpecificFunction}.
	 * @param ctx the parse tree
	 */
	void enterRoundingExpression(ExpressionEvaluatorV2Parser.RoundingExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code roundingExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#mathSpecificFunction}.
	 * @param ctx the parse tree
	 */
	void exitRoundingExpression(ExpressionEvaluatorV2Parser.RoundingExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code sequenceExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#mathSpecificFunction}.
	 * @param ctx the parse tree
	 */
	void enterSequenceExpression(ExpressionEvaluatorV2Parser.SequenceExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code sequenceExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#mathSpecificFunction}.
	 * @param ctx the parse tree
	 */
	void exitSequenceExpression(ExpressionEvaluatorV2Parser.SequenceExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code fixedLogarithm}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logarithmFunction}.
	 * @param ctx the parse tree
	 */
	void enterFixedLogarithm(ExpressionEvaluatorV2Parser.FixedLogarithmContext ctx);
	/**
	 * Exit a parse tree produced by the {@code fixedLogarithm}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logarithmFunction}.
	 * @param ctx the parse tree
	 */
	void exitFixedLogarithm(ExpressionEvaluatorV2Parser.FixedLogarithmContext ctx);
	/**
	 * Enter a parse tree produced by the {@code variableLogarithm}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logarithmFunction}.
	 * @param ctx the parse tree
	 */
	void enterVariableLogarithm(ExpressionEvaluatorV2Parser.VariableLogarithmContext ctx);
	/**
	 * Exit a parse tree produced by the {@code variableLogarithm}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logarithmFunction}.
	 * @param ctx the parse tree
	 */
	void exitVariableLogarithm(ExpressionEvaluatorV2Parser.VariableLogarithmContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExpressionEvaluatorV2Parser#roundingFunction}.
	 * @param ctx the parse tree
	 */
	void enterRoundingFunction(ExpressionEvaluatorV2Parser.RoundingFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExpressionEvaluatorV2Parser#roundingFunction}.
	 * @param ctx the parse tree
	 */
	void exitRoundingFunction(ExpressionEvaluatorV2Parser.RoundingFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExpressionEvaluatorV2Parser#sequenceFunction}.
	 * @param ctx the parse tree
	 */
	void enterSequenceFunction(ExpressionEvaluatorV2Parser.SequenceFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExpressionEvaluatorV2Parser#sequenceFunction}.
	 * @param ctx the parse tree
	 */
	void exitSequenceFunction(ExpressionEvaluatorV2Parser.SequenceFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExpressionEvaluatorV2Parser#dateUnit}.
	 * @param ctx the parse tree
	 */
	void enterDateUnit(ExpressionEvaluatorV2Parser.DateUnitContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExpressionEvaluatorV2Parser#dateUnit}.
	 * @param ctx the parse tree
	 */
	void exitDateUnit(ExpressionEvaluatorV2Parser.DateUnitContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExpressionEvaluatorV2Parser#timeUnit}.
	 * @param ctx the parse tree
	 */
	void enterTimeUnit(ExpressionEvaluatorV2Parser.TimeUnitContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExpressionEvaluatorV2Parser#timeUnit}.
	 * @param ctx the parse tree
	 */
	void exitTimeUnit(ExpressionEvaluatorV2Parser.TimeUnitContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateParenthesis}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateOperation}.
	 * @param ctx the parse tree
	 */
	void enterDateParenthesis(ExpressionEvaluatorV2Parser.DateParenthesisContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateParenthesis}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateOperation}.
	 * @param ctx the parse tree
	 */
	void exitDateParenthesis(ExpressionEvaluatorV2Parser.DateParenthesisContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateFunction}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateOperation}.
	 * @param ctx the parse tree
	 */
	void enterDateFunction(ExpressionEvaluatorV2Parser.DateFunctionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateFunction}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateOperation}.
	 * @param ctx the parse tree
	 */
	void exitDateFunction(ExpressionEvaluatorV2Parser.DateFunctionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code timeParenthesis}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#timeOperation}.
	 * @param ctx the parse tree
	 */
	void enterTimeParenthesis(ExpressionEvaluatorV2Parser.TimeParenthesisContext ctx);
	/**
	 * Exit a parse tree produced by the {@code timeParenthesis}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#timeOperation}.
	 * @param ctx the parse tree
	 */
	void exitTimeParenthesis(ExpressionEvaluatorV2Parser.TimeParenthesisContext ctx);
	/**
	 * Enter a parse tree produced by the {@code timeFunction}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#timeOperation}.
	 * @param ctx the parse tree
	 */
	void enterTimeFunction(ExpressionEvaluatorV2Parser.TimeFunctionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code timeFunction}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#timeOperation}.
	 * @param ctx the parse tree
	 */
	void exitTimeFunction(ExpressionEvaluatorV2Parser.TimeFunctionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateTimeParenthesis}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateTimeOperation}.
	 * @param ctx the parse tree
	 */
	void enterDateTimeParenthesis(ExpressionEvaluatorV2Parser.DateTimeParenthesisContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateTimeParenthesis}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateTimeOperation}.
	 * @param ctx the parse tree
	 */
	void exitDateTimeParenthesis(ExpressionEvaluatorV2Parser.DateTimeParenthesisContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateTimeFunction}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateTimeOperation}.
	 * @param ctx the parse tree
	 */
	void enterDateTimeFunction(ExpressionEvaluatorV2Parser.DateTimeFunctionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateTimeFunction}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateTimeOperation}.
	 * @param ctx the parse tree
	 */
	void exitDateTimeFunction(ExpressionEvaluatorV2Parser.DateTimeFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExpressionEvaluatorV2Parser#function}.
	 * @param ctx the parse tree
	 */
	void enterFunction(ExpressionEvaluatorV2Parser.FunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExpressionEvaluatorV2Parser#function}.
	 * @param ctx the parse tree
	 */
	void exitFunction(ExpressionEvaluatorV2Parser.FunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExpressionEvaluatorV2Parser#comparisonOperator}.
	 * @param ctx the parse tree
	 */
	void enterComparisonOperator(ExpressionEvaluatorV2Parser.ComparisonOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExpressionEvaluatorV2Parser#comparisonOperator}.
	 * @param ctx the parse tree
	 */
	void exitComparisonOperator(ExpressionEvaluatorV2Parser.ComparisonOperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExpressionEvaluatorV2Parser#logicalOperator}.
	 * @param ctx the parse tree
	 */
	void enterLogicalOperator(ExpressionEvaluatorV2Parser.LogicalOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExpressionEvaluatorV2Parser#logicalOperator}.
	 * @param ctx the parse tree
	 */
	void exitLogicalOperator(ExpressionEvaluatorV2Parser.LogicalOperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExpressionEvaluatorV2Parser#allEntityTypes}.
	 * @param ctx the parse tree
	 */
	void enterAllEntityTypes(ExpressionEvaluatorV2Parser.AllEntityTypesContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExpressionEvaluatorV2Parser#allEntityTypes}.
	 * @param ctx the parse tree
	 */
	void exitAllEntityTypes(ExpressionEvaluatorV2Parser.AllEntityTypesContext ctx);
	/**
	 * Enter a parse tree produced by the {@code logicalConstant}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalEntity}.
	 * @param ctx the parse tree
	 */
	void enterLogicalConstant(ExpressionEvaluatorV2Parser.LogicalConstantContext ctx);
	/**
	 * Exit a parse tree produced by the {@code logicalConstant}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalEntity}.
	 * @param ctx the parse tree
	 */
	void exitLogicalConstant(ExpressionEvaluatorV2Parser.LogicalConstantContext ctx);
	/**
	 * Enter a parse tree produced by the {@code logicalDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalEntity}.
	 * @param ctx the parse tree
	 */
	void enterLogicalDecisionExpression(ExpressionEvaluatorV2Parser.LogicalDecisionExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code logicalDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalEntity}.
	 * @param ctx the parse tree
	 */
	void exitLogicalDecisionExpression(ExpressionEvaluatorV2Parser.LogicalDecisionExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code logicalFunctionDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalEntity}.
	 * @param ctx the parse tree
	 */
	void enterLogicalFunctionDecisionExpression(ExpressionEvaluatorV2Parser.LogicalFunctionDecisionExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code logicalFunctionDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalEntity}.
	 * @param ctx the parse tree
	 */
	void exitLogicalFunctionDecisionExpression(ExpressionEvaluatorV2Parser.LogicalFunctionDecisionExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code logicalFunctionResult}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalEntity}.
	 * @param ctx the parse tree
	 */
	void enterLogicalFunctionResult(ExpressionEvaluatorV2Parser.LogicalFunctionResultContext ctx);
	/**
	 * Exit a parse tree produced by the {@code logicalFunctionResult}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalEntity}.
	 * @param ctx the parse tree
	 */
	void exitLogicalFunctionResult(ExpressionEvaluatorV2Parser.LogicalFunctionResultContext ctx);
	/**
	 * Enter a parse tree produced by the {@code logicalVariable}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalEntity}.
	 * @param ctx the parse tree
	 */
	void enterLogicalVariable(ExpressionEvaluatorV2Parser.LogicalVariableContext ctx);
	/**
	 * Exit a parse tree produced by the {@code logicalVariable}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalEntity}.
	 * @param ctx the parse tree
	 */
	void exitLogicalVariable(ExpressionEvaluatorV2Parser.LogicalVariableContext ctx);
	/**
	 * Enter a parse tree produced by the {@code mathDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#numericEntity}.
	 * @param ctx the parse tree
	 */
	void enterMathDecisionExpression(ExpressionEvaluatorV2Parser.MathDecisionExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code mathDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#numericEntity}.
	 * @param ctx the parse tree
	 */
	void exitMathDecisionExpression(ExpressionEvaluatorV2Parser.MathDecisionExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code mathFunctionDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#numericEntity}.
	 * @param ctx the parse tree
	 */
	void enterMathFunctionDecisionExpression(ExpressionEvaluatorV2Parser.MathFunctionDecisionExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code mathFunctionDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#numericEntity}.
	 * @param ctx the parse tree
	 */
	void exitMathFunctionDecisionExpression(ExpressionEvaluatorV2Parser.MathFunctionDecisionExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code summationVariable}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#numericEntity}.
	 * @param ctx the parse tree
	 */
	void enterSummationVariable(ExpressionEvaluatorV2Parser.SummationVariableContext ctx);
	/**
	 * Exit a parse tree produced by the {@code summationVariable}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#numericEntity}.
	 * @param ctx the parse tree
	 */
	void exitSummationVariable(ExpressionEvaluatorV2Parser.SummationVariableContext ctx);
	/**
	 * Enter a parse tree produced by the {@code productSequenceVariable}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#numericEntity}.
	 * @param ctx the parse tree
	 */
	void enterProductSequenceVariable(ExpressionEvaluatorV2Parser.ProductSequenceVariableContext ctx);
	/**
	 * Exit a parse tree produced by the {@code productSequenceVariable}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#numericEntity}.
	 * @param ctx the parse tree
	 */
	void exitProductSequenceVariable(ExpressionEvaluatorV2Parser.ProductSequenceVariableContext ctx);
	/**
	 * Enter a parse tree produced by the {@code numericConstant}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#numericEntity}.
	 * @param ctx the parse tree
	 */
	void enterNumericConstant(ExpressionEvaluatorV2Parser.NumericConstantContext ctx);
	/**
	 * Exit a parse tree produced by the {@code numericConstant}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#numericEntity}.
	 * @param ctx the parse tree
	 */
	void exitNumericConstant(ExpressionEvaluatorV2Parser.NumericConstantContext ctx);
	/**
	 * Enter a parse tree produced by the {@code numericFunctionResult}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#numericEntity}.
	 * @param ctx the parse tree
	 */
	void enterNumericFunctionResult(ExpressionEvaluatorV2Parser.NumericFunctionResultContext ctx);
	/**
	 * Exit a parse tree produced by the {@code numericFunctionResult}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#numericEntity}.
	 * @param ctx the parse tree
	 */
	void exitNumericFunctionResult(ExpressionEvaluatorV2Parser.NumericFunctionResultContext ctx);
	/**
	 * Enter a parse tree produced by the {@code numericVariable}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#numericEntity}.
	 * @param ctx the parse tree
	 */
	void enterNumericVariable(ExpressionEvaluatorV2Parser.NumericVariableContext ctx);
	/**
	 * Exit a parse tree produced by the {@code numericVariable}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#numericEntity}.
	 * @param ctx the parse tree
	 */
	void exitNumericVariable(ExpressionEvaluatorV2Parser.NumericVariableContext ctx);
	/**
	 * Enter a parse tree produced by the {@code stringDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#stringEntity}.
	 * @param ctx the parse tree
	 */
	void enterStringDecisionExpression(ExpressionEvaluatorV2Parser.StringDecisionExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code stringDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#stringEntity}.
	 * @param ctx the parse tree
	 */
	void exitStringDecisionExpression(ExpressionEvaluatorV2Parser.StringDecisionExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code stringFunctionDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#stringEntity}.
	 * @param ctx the parse tree
	 */
	void enterStringFunctionDecisionExpression(ExpressionEvaluatorV2Parser.StringFunctionDecisionExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code stringFunctionDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#stringEntity}.
	 * @param ctx the parse tree
	 */
	void exitStringFunctionDecisionExpression(ExpressionEvaluatorV2Parser.StringFunctionDecisionExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code stringConstant}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#stringEntity}.
	 * @param ctx the parse tree
	 */
	void enterStringConstant(ExpressionEvaluatorV2Parser.StringConstantContext ctx);
	/**
	 * Exit a parse tree produced by the {@code stringConstant}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#stringEntity}.
	 * @param ctx the parse tree
	 */
	void exitStringConstant(ExpressionEvaluatorV2Parser.StringConstantContext ctx);
	/**
	 * Enter a parse tree produced by the {@code stringFunctionResult}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#stringEntity}.
	 * @param ctx the parse tree
	 */
	void enterStringFunctionResult(ExpressionEvaluatorV2Parser.StringFunctionResultContext ctx);
	/**
	 * Exit a parse tree produced by the {@code stringFunctionResult}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#stringEntity}.
	 * @param ctx the parse tree
	 */
	void exitStringFunctionResult(ExpressionEvaluatorV2Parser.StringFunctionResultContext ctx);
	/**
	 * Enter a parse tree produced by the {@code stringVariable}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#stringEntity}.
	 * @param ctx the parse tree
	 */
	void enterStringVariable(ExpressionEvaluatorV2Parser.StringVariableContext ctx);
	/**
	 * Exit a parse tree produced by the {@code stringVariable}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#stringEntity}.
	 * @param ctx the parse tree
	 */
	void exitStringVariable(ExpressionEvaluatorV2Parser.StringVariableContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateEntity}.
	 * @param ctx the parse tree
	 */
	void enterDateDecisionExpression(ExpressionEvaluatorV2Parser.DateDecisionExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateEntity}.
	 * @param ctx the parse tree
	 */
	void exitDateDecisionExpression(ExpressionEvaluatorV2Parser.DateDecisionExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateFunctionDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateEntity}.
	 * @param ctx the parse tree
	 */
	void enterDateFunctionDecisionExpression(ExpressionEvaluatorV2Parser.DateFunctionDecisionExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateFunctionDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateEntity}.
	 * @param ctx the parse tree
	 */
	void exitDateFunctionDecisionExpression(ExpressionEvaluatorV2Parser.DateFunctionDecisionExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateConstant}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateEntity}.
	 * @param ctx the parse tree
	 */
	void enterDateConstant(ExpressionEvaluatorV2Parser.DateConstantContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateConstant}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateEntity}.
	 * @param ctx the parse tree
	 */
	void exitDateConstant(ExpressionEvaluatorV2Parser.DateConstantContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateCurrentValue}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateEntity}.
	 * @param ctx the parse tree
	 */
	void enterDateCurrentValue(ExpressionEvaluatorV2Parser.DateCurrentValueContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateCurrentValue}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateEntity}.
	 * @param ctx the parse tree
	 */
	void exitDateCurrentValue(ExpressionEvaluatorV2Parser.DateCurrentValueContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateVariable}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateEntity}.
	 * @param ctx the parse tree
	 */
	void enterDateVariable(ExpressionEvaluatorV2Parser.DateVariableContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateVariable}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateEntity}.
	 * @param ctx the parse tree
	 */
	void exitDateVariable(ExpressionEvaluatorV2Parser.DateVariableContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateFunctionResult}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateEntity}.
	 * @param ctx the parse tree
	 */
	void enterDateFunctionResult(ExpressionEvaluatorV2Parser.DateFunctionResultContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateFunctionResult}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateEntity}.
	 * @param ctx the parse tree
	 */
	void exitDateFunctionResult(ExpressionEvaluatorV2Parser.DateFunctionResultContext ctx);
	/**
	 * Enter a parse tree produced by the {@code timeDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#timeEntity}.
	 * @param ctx the parse tree
	 */
	void enterTimeDecisionExpression(ExpressionEvaluatorV2Parser.TimeDecisionExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code timeDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#timeEntity}.
	 * @param ctx the parse tree
	 */
	void exitTimeDecisionExpression(ExpressionEvaluatorV2Parser.TimeDecisionExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code timeFunctionDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#timeEntity}.
	 * @param ctx the parse tree
	 */
	void enterTimeFunctionDecisionExpression(ExpressionEvaluatorV2Parser.TimeFunctionDecisionExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code timeFunctionDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#timeEntity}.
	 * @param ctx the parse tree
	 */
	void exitTimeFunctionDecisionExpression(ExpressionEvaluatorV2Parser.TimeFunctionDecisionExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code timeConstant}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#timeEntity}.
	 * @param ctx the parse tree
	 */
	void enterTimeConstant(ExpressionEvaluatorV2Parser.TimeConstantContext ctx);
	/**
	 * Exit a parse tree produced by the {@code timeConstant}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#timeEntity}.
	 * @param ctx the parse tree
	 */
	void exitTimeConstant(ExpressionEvaluatorV2Parser.TimeConstantContext ctx);
	/**
	 * Enter a parse tree produced by the {@code timeCurrentValue}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#timeEntity}.
	 * @param ctx the parse tree
	 */
	void enterTimeCurrentValue(ExpressionEvaluatorV2Parser.TimeCurrentValueContext ctx);
	/**
	 * Exit a parse tree produced by the {@code timeCurrentValue}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#timeEntity}.
	 * @param ctx the parse tree
	 */
	void exitTimeCurrentValue(ExpressionEvaluatorV2Parser.TimeCurrentValueContext ctx);
	/**
	 * Enter a parse tree produced by the {@code timeVariable}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#timeEntity}.
	 * @param ctx the parse tree
	 */
	void enterTimeVariable(ExpressionEvaluatorV2Parser.TimeVariableContext ctx);
	/**
	 * Exit a parse tree produced by the {@code timeVariable}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#timeEntity}.
	 * @param ctx the parse tree
	 */
	void exitTimeVariable(ExpressionEvaluatorV2Parser.TimeVariableContext ctx);
	/**
	 * Enter a parse tree produced by the {@code timeFunctionResult}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#timeEntity}.
	 * @param ctx the parse tree
	 */
	void enterTimeFunctionResult(ExpressionEvaluatorV2Parser.TimeFunctionResultContext ctx);
	/**
	 * Exit a parse tree produced by the {@code timeFunctionResult}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#timeEntity}.
	 * @param ctx the parse tree
	 */
	void exitTimeFunctionResult(ExpressionEvaluatorV2Parser.TimeFunctionResultContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateTimeDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateTimeEntity}.
	 * @param ctx the parse tree
	 */
	void enterDateTimeDecisionExpression(ExpressionEvaluatorV2Parser.DateTimeDecisionExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateTimeDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateTimeEntity}.
	 * @param ctx the parse tree
	 */
	void exitDateTimeDecisionExpression(ExpressionEvaluatorV2Parser.DateTimeDecisionExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateTimeFunctionDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateTimeEntity}.
	 * @param ctx the parse tree
	 */
	void enterDateTimeFunctionDecisionExpression(ExpressionEvaluatorV2Parser.DateTimeFunctionDecisionExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateTimeFunctionDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateTimeEntity}.
	 * @param ctx the parse tree
	 */
	void exitDateTimeFunctionDecisionExpression(ExpressionEvaluatorV2Parser.DateTimeFunctionDecisionExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateTimeConstant}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateTimeEntity}.
	 * @param ctx the parse tree
	 */
	void enterDateTimeConstant(ExpressionEvaluatorV2Parser.DateTimeConstantContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateTimeConstant}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateTimeEntity}.
	 * @param ctx the parse tree
	 */
	void exitDateTimeConstant(ExpressionEvaluatorV2Parser.DateTimeConstantContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateTimeCurrentValue}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateTimeEntity}.
	 * @param ctx the parse tree
	 */
	void enterDateTimeCurrentValue(ExpressionEvaluatorV2Parser.DateTimeCurrentValueContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateTimeCurrentValue}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateTimeEntity}.
	 * @param ctx the parse tree
	 */
	void exitDateTimeCurrentValue(ExpressionEvaluatorV2Parser.DateTimeCurrentValueContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateTimeVariable}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateTimeEntity}.
	 * @param ctx the parse tree
	 */
	void enterDateTimeVariable(ExpressionEvaluatorV2Parser.DateTimeVariableContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateTimeVariable}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateTimeEntity}.
	 * @param ctx the parse tree
	 */
	void exitDateTimeVariable(ExpressionEvaluatorV2Parser.DateTimeVariableContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dateTimeFunctionResult}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateTimeEntity}.
	 * @param ctx the parse tree
	 */
	void enterDateTimeFunctionResult(ExpressionEvaluatorV2Parser.DateTimeFunctionResultContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dateTimeFunctionResult}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateTimeEntity}.
	 * @param ctx the parse tree
	 */
	void exitDateTimeFunctionResult(ExpressionEvaluatorV2Parser.DateTimeFunctionResultContext ctx);
	/**
	 * Enter a parse tree produced by the {@code vectorOfEntities}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#vectorEntity}.
	 * @param ctx the parse tree
	 */
	void enterVectorOfEntities(ExpressionEvaluatorV2Parser.VectorOfEntitiesContext ctx);
	/**
	 * Exit a parse tree produced by the {@code vectorOfEntities}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#vectorEntity}.
	 * @param ctx the parse tree
	 */
	void exitVectorOfEntities(ExpressionEvaluatorV2Parser.VectorOfEntitiesContext ctx);
	/**
	 * Enter a parse tree produced by the {@code vectorVariable}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#vectorEntity}.
	 * @param ctx the parse tree
	 */
	void enterVectorVariable(ExpressionEvaluatorV2Parser.VectorVariableContext ctx);
	/**
	 * Exit a parse tree produced by the {@code vectorVariable}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#vectorEntity}.
	 * @param ctx the parse tree
	 */
	void exitVectorVariable(ExpressionEvaluatorV2Parser.VectorVariableContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExpressionEvaluatorV2Parser#vectorOfVariables}.
	 * @param ctx the parse tree
	 */
	void enterVectorOfVariables(ExpressionEvaluatorV2Parser.VectorOfVariablesContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExpressionEvaluatorV2Parser#vectorOfVariables}.
	 * @param ctx the parse tree
	 */
	void exitVectorOfVariables(ExpressionEvaluatorV2Parser.VectorOfVariablesContext ctx);
}