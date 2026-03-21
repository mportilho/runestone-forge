// Generated from exp-eval-mk2/src/main/antlr4/com/runestone/expeval2/internal/grammar/ExpressionEvaluatorV2.g4 by ANTLR 4.13.1
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
	 * Visit a parse tree produced by the {@code mathInput}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#mathStart}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMathInput(ExpressionEvaluatorV2Parser.MathInputContext ctx);
	/**
	 * Visit a parse tree produced by the {@code assignmentInput}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#assignmentStart}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignmentInput(ExpressionEvaluatorV2Parser.AssignmentInputContext ctx);
	/**
	 * Visit a parse tree produced by the {@code logicalInput}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalStart}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalInput(ExpressionEvaluatorV2Parser.LogicalInputContext ctx);
	/**
	 * Visit a parse tree produced by the {@code assignmentOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#assignmentExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignmentOperation(ExpressionEvaluatorV2Parser.AssignmentOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code destructuringAssignmentOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#assignmentExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDestructuringAssignmentOperation(ExpressionEvaluatorV2Parser.DestructuringAssignmentOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code logicalOrOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalOrOperation(ExpressionEvaluatorV2Parser.LogicalOrOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code logicalOrChainOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalOrExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalOrChainOperation(ExpressionEvaluatorV2Parser.LogicalOrChainOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code logicalAndChainOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalAndExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalAndChainOperation(ExpressionEvaluatorV2Parser.LogicalAndChainOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code logicalComparisonOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalComparisonOperation(ExpressionEvaluatorV2Parser.LogicalComparisonOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code mathComparisonOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMathComparisonOperation(ExpressionEvaluatorV2Parser.MathComparisonOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code stringComparisonOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringComparisonOperation(ExpressionEvaluatorV2Parser.StringComparisonOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateComparisonOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateComparisonOperation(ExpressionEvaluatorV2Parser.DateComparisonOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code timeComparisonOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTimeComparisonOperation(ExpressionEvaluatorV2Parser.TimeComparisonOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateTimeComparisonOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalComparisonExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateTimeComparisonOperation(ExpressionEvaluatorV2Parser.DateTimeComparisonOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code logicalBitwiseOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalBitwiseExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalBitwiseOperation(ExpressionEvaluatorV2Parser.LogicalBitwiseOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code logicalNotOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalNotExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalNotOperation(ExpressionEvaluatorV2Parser.LogicalNotOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code logicalPrimaryOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalNotExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalPrimaryOperation(ExpressionEvaluatorV2Parser.LogicalPrimaryOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code logicalExpressionParenthesisOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalPrimary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalExpressionParenthesisOperation(ExpressionEvaluatorV2Parser.LogicalExpressionParenthesisOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code logicalEntityOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalPrimary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalEntityOperation(ExpressionEvaluatorV2Parser.LogicalEntityOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code sumOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#mathExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSumOperation(ExpressionEvaluatorV2Parser.SumOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code additiveOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#sumExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAdditiveOperation(ExpressionEvaluatorV2Parser.AdditiveOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code multiplicativeOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#multiplicationExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMultiplicativeOperation(ExpressionEvaluatorV2Parser.MultiplicativeOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code unaryMinusOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#unaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnaryMinusOperation(ExpressionEvaluatorV2Parser.UnaryMinusOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code rootExpressionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#unaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRootExpressionOperation(ExpressionEvaluatorV2Parser.RootExpressionOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code rootChainOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#rootExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRootChainOperation(ExpressionEvaluatorV2Parser.RootChainOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code exponentiationOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#exponentiationExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExponentiationOperation(ExpressionEvaluatorV2Parser.ExponentiationOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code postfixOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#postfixExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPostfixOperation(ExpressionEvaluatorV2Parser.PostfixOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code mathExpressionParenthesisOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#primaryMathExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMathExpressionParenthesisOperation(ExpressionEvaluatorV2Parser.MathExpressionParenthesisOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code squareRootOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#primaryMathExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSquareRootOperation(ExpressionEvaluatorV2Parser.SquareRootOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code modulusOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#primaryMathExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitModulusOperation(ExpressionEvaluatorV2Parser.ModulusOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code numericEntityOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#primaryMathExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumericEntityOperation(ExpressionEvaluatorV2Parser.NumericEntityOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code functionCallOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#function}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionCallOperation(ExpressionEvaluatorV2Parser.FunctionCallOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code functionReferenceTarget}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#referenceTarget}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionReferenceTarget(ExpressionEvaluatorV2Parser.FunctionReferenceTargetContext ctx);
	/**
	 * Visit a parse tree produced by the {@code identifierReferenceTarget}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#referenceTarget}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentifierReferenceTarget(ExpressionEvaluatorV2Parser.IdentifierReferenceTargetContext ctx);
	/**
	 * Visit a parse tree produced by the {@code greaterThanOperator}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#comparisonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGreaterThanOperator(ExpressionEvaluatorV2Parser.GreaterThanOperatorContext ctx);
	/**
	 * Visit a parse tree produced by the {@code greaterThanOrEqualOperator}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#comparisonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGreaterThanOrEqualOperator(ExpressionEvaluatorV2Parser.GreaterThanOrEqualOperatorContext ctx);
	/**
	 * Visit a parse tree produced by the {@code lessThanOperator}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#comparisonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLessThanOperator(ExpressionEvaluatorV2Parser.LessThanOperatorContext ctx);
	/**
	 * Visit a parse tree produced by the {@code lessThanOrEqualOperator}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#comparisonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLessThanOrEqualOperator(ExpressionEvaluatorV2Parser.LessThanOrEqualOperatorContext ctx);
	/**
	 * Visit a parse tree produced by the {@code equalOperator}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#comparisonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEqualOperator(ExpressionEvaluatorV2Parser.EqualOperatorContext ctx);
	/**
	 * Visit a parse tree produced by the {@code notEqualOperator}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#comparisonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNotEqualOperator(ExpressionEvaluatorV2Parser.NotEqualOperatorContext ctx);
	/**
	 * Visit a parse tree produced by the {@code mathEntityType}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#allEntityTypes}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMathEntityType(ExpressionEvaluatorV2Parser.MathEntityTypeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code logicalEntityType}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#allEntityTypes}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalEntityType(ExpressionEvaluatorV2Parser.LogicalEntityTypeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateEntityType}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#allEntityTypes}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateEntityType(ExpressionEvaluatorV2Parser.DateEntityTypeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code timeEntityType}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#allEntityTypes}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTimeEntityType(ExpressionEvaluatorV2Parser.TimeEntityTypeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateTimeEntityType}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#allEntityTypes}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateTimeEntityType(ExpressionEvaluatorV2Parser.DateTimeEntityTypeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code stringEntityType}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#allEntityTypes}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringEntityType(ExpressionEvaluatorV2Parser.StringEntityTypeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code vectorEntityType}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#allEntityTypes}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVectorEntityType(ExpressionEvaluatorV2Parser.VectorEntityTypeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code genericAssignmentValue}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#assignmentValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGenericAssignmentValue(ExpressionEvaluatorV2Parser.GenericAssignmentValueContext ctx);
	/**
	 * Visit a parse tree produced by the {@code mathAssignmentValue}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#assignmentValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMathAssignmentValue(ExpressionEvaluatorV2Parser.MathAssignmentValueContext ctx);
	/**
	 * Visit a parse tree produced by the {@code logicalAssignmentValue}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#assignmentValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalAssignmentValue(ExpressionEvaluatorV2Parser.LogicalAssignmentValueContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateAssignmentValue}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#assignmentValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateAssignmentValue(ExpressionEvaluatorV2Parser.DateAssignmentValueContext ctx);
	/**
	 * Visit a parse tree produced by the {@code timeAssignmentValue}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#assignmentValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTimeAssignmentValue(ExpressionEvaluatorV2Parser.TimeAssignmentValueContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateTimeAssignmentValue}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#assignmentValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateTimeAssignmentValue(ExpressionEvaluatorV2Parser.DateTimeAssignmentValueContext ctx);
	/**
	 * Visit a parse tree produced by the {@code stringAssignmentValue}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#assignmentValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringAssignmentValue(ExpressionEvaluatorV2Parser.StringAssignmentValueContext ctx);
	/**
	 * Visit a parse tree produced by the {@code vectorAssignmentValue}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#assignmentValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVectorAssignmentValue(ExpressionEvaluatorV2Parser.VectorAssignmentValueContext ctx);
	/**
	 * Visit a parse tree produced by the {@code genericDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#genericEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGenericDecisionExpression(ExpressionEvaluatorV2Parser.GenericDecisionExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code genericFunctionDecisionExpression}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#genericEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGenericFunctionDecisionExpression(ExpressionEvaluatorV2Parser.GenericFunctionDecisionExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code castExpressionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#genericEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCastExpressionOperation(ExpressionEvaluatorV2Parser.CastExpressionOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code referenceTargetOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#genericEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReferenceTargetOperation(ExpressionEvaluatorV2Parser.ReferenceTargetOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code typeCastOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#castExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeCastOperation(ExpressionEvaluatorV2Parser.TypeCastOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code booleanTypeHint}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#typeHint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBooleanTypeHint(ExpressionEvaluatorV2Parser.BooleanTypeHintContext ctx);
	/**
	 * Visit a parse tree produced by the {@code numberTypeHint}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#typeHint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumberTypeHint(ExpressionEvaluatorV2Parser.NumberTypeHintContext ctx);
	/**
	 * Visit a parse tree produced by the {@code stringTypeHint}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#typeHint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringTypeHint(ExpressionEvaluatorV2Parser.StringTypeHintContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateTypeHint}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#typeHint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateTypeHint(ExpressionEvaluatorV2Parser.DateTypeHintContext ctx);
	/**
	 * Visit a parse tree produced by the {@code timeTypeHint}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#typeHint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTimeTypeHint(ExpressionEvaluatorV2Parser.TimeTypeHintContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateTimeTypeHint}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#typeHint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateTimeTypeHint(ExpressionEvaluatorV2Parser.DateTimeTypeHintContext ctx);
	/**
	 * Visit a parse tree produced by the {@code vectorTypeHint}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#typeHint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVectorTypeHint(ExpressionEvaluatorV2Parser.VectorTypeHintContext ctx);
	/**
	 * Visit a parse tree produced by the {@code logicalConstantOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalConstantOperation(ExpressionEvaluatorV2Parser.LogicalConstantOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code logicalDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalDecisionOperation(ExpressionEvaluatorV2Parser.LogicalDecisionOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code logicalFunctionDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalFunctionDecisionOperation(ExpressionEvaluatorV2Parser.LogicalFunctionDecisionOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code logicalReferenceOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#logicalEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalReferenceOperation(ExpressionEvaluatorV2Parser.LogicalReferenceOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code mathDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#numericEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMathDecisionOperation(ExpressionEvaluatorV2Parser.MathDecisionOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code mathFunctionDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#numericEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMathFunctionDecisionOperation(ExpressionEvaluatorV2Parser.MathFunctionDecisionOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code numericConstantOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#numericEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumericConstantOperation(ExpressionEvaluatorV2Parser.NumericConstantOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code numericReferenceOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#numericEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumericReferenceOperation(ExpressionEvaluatorV2Parser.NumericReferenceOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code stringDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#stringEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringDecisionOperation(ExpressionEvaluatorV2Parser.StringDecisionOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code stringFunctionDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#stringEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringFunctionDecisionOperation(ExpressionEvaluatorV2Parser.StringFunctionDecisionOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code stringConstantOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#stringEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringConstantOperation(ExpressionEvaluatorV2Parser.StringConstantOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code stringReferenceOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#stringEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringReferenceOperation(ExpressionEvaluatorV2Parser.StringReferenceOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateDecisionOperation(ExpressionEvaluatorV2Parser.DateDecisionOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateFunctionDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateFunctionDecisionOperation(ExpressionEvaluatorV2Parser.DateFunctionDecisionOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateConstantOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateConstantOperation(ExpressionEvaluatorV2Parser.DateConstantOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateCurrentValueOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateCurrentValueOperation(ExpressionEvaluatorV2Parser.DateCurrentValueOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateReferenceOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateReferenceOperation(ExpressionEvaluatorV2Parser.DateReferenceOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code timeDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#timeEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTimeDecisionOperation(ExpressionEvaluatorV2Parser.TimeDecisionOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code timeFunctionDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#timeEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTimeFunctionDecisionOperation(ExpressionEvaluatorV2Parser.TimeFunctionDecisionOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code timeConstantOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#timeEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTimeConstantOperation(ExpressionEvaluatorV2Parser.TimeConstantOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code timeCurrentValueOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#timeEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTimeCurrentValueOperation(ExpressionEvaluatorV2Parser.TimeCurrentValueOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code timeReferenceOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#timeEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTimeReferenceOperation(ExpressionEvaluatorV2Parser.TimeReferenceOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateTimeDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateTimeEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateTimeDecisionOperation(ExpressionEvaluatorV2Parser.DateTimeDecisionOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateTimeFunctionDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateTimeEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateTimeFunctionDecisionOperation(ExpressionEvaluatorV2Parser.DateTimeFunctionDecisionOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateTimeConstantOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateTimeEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateTimeConstantOperation(ExpressionEvaluatorV2Parser.DateTimeConstantOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateTimeCurrentValueOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateTimeEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateTimeCurrentValueOperation(ExpressionEvaluatorV2Parser.DateTimeCurrentValueOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dateTimeReferenceOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#dateTimeEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateTimeReferenceOperation(ExpressionEvaluatorV2Parser.DateTimeReferenceOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code vectorDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#vectorEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVectorDecisionOperation(ExpressionEvaluatorV2Parser.VectorDecisionOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code vectorFunctionDecisionOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#vectorEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVectorFunctionDecisionOperation(ExpressionEvaluatorV2Parser.VectorFunctionDecisionOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code vectorOfEntitiesOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#vectorEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVectorOfEntitiesOperation(ExpressionEvaluatorV2Parser.VectorOfEntitiesOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code vectorReferenceOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#vectorEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVectorReferenceOperation(ExpressionEvaluatorV2Parser.VectorReferenceOperationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code vectorOfVariablesOperation}
	 * labeled alternative in {@link ExpressionEvaluatorV2Parser#vectorOfVariables}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVectorOfVariablesOperation(ExpressionEvaluatorV2Parser.VectorOfVariablesOperationContext ctx);
}