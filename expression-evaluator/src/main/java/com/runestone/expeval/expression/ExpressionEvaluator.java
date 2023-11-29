package com.runestone.expeval.expression;

import com.runestone.assertions.Certify;
import com.runestone.expeval.grammar.language.ExpressionEvaluatorLexer;
import com.runestone.expeval.grammar.language.ExpressionEvaluatorParser;
import com.runestone.expeval.grammar.language.ExpressionEvaluatorParser.StartContext;
import com.runestone.expeval.grammar.parser.LanguageData;
import com.runestone.expeval.grammar.parser.LanguageParser;
import com.runestone.expeval.grammar.parser.ParsingErrorListener;
import com.runestone.expeval.grammar.parser.impl.DefaultLanguageParser;
import com.runestone.expeval.operation.AbstractOperation;
import com.runestone.expeval.operation.CloningContext;
import com.runestone.expeval.operation.OperationContext;
import com.runestone.expeval.operation.values.AbstractVariableValueOperation;
import com.runestone.expeval.operation.values.variable.AssignedVariableOperation;
import com.runestone.memoization.MemoizedSupplier;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.atn.PredictionMode;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Responsible for evaluating an expression. It parses the text expression and creates an operation tree that can be evaluated, navigated or cloned.
 *
 * @author Marcelo Portilho
 */
class ExpressionEvaluator {

    private static final ParsingErrorListener PARSING_ERROR_LISTENER = new ParsingErrorListener();

    private final String expression;
    private final ExpressionOptions options;
    private final ExpressionContext expressionContext;

    private LanguageData languageData;

    /**
     * Create a new expression evaluator.
     *
     * @param expression        the expression to be evaluated
     * @param options           the expression options
     * @param expressionContext the expression context
     */
    public ExpressionEvaluator(String expression, ExpressionOptions options, ExpressionContext expressionContext) {
        this.expression = Certify.requireNonBlank(expression, "Expression cannot be blank");
        this.options = Objects.requireNonNull(options, "Expression options cannot be null");
        this.expressionContext = Objects.requireNonNull(expressionContext, "Expression context cannot be null");
    }

    /**
     * Evaluates the expression and returns the result as a number or boolean.
     *
     * @param expressionContext the expression context. It has priority over the internal context and can be null if no addition context is required.
     * @param <T>               the expected result type
     * @return the result of the expression
     */
    public <T> T evaluate(ExpressionContext expressionContext) {
        parseExpression();
        OperationContext operationContext = createOperationContext(false, expressionContext);
        return languageData.operation().evaluate(operationContext);
    }

    public void validate(String expression) {
        createLanguageGrammarContext(CharStreams.fromString(expression), PredictionMode.LL_EXACT_AMBIG_DETECTION).start();
    }

    /**
     * Traverses the expression tree and warms up the operations. This is useful to speed up the subsequent evaluations of the expression.
     * <p>
     * Ex.: creating a cache of semi evaluated expressions to be cloned and evaluated as needed.
     */
    public void warmUp() {
        parseExpression();
        OperationContext operationContext = createOperationContext(true, null);
        languageData.operation().accept(new WarmUpOperationVisitor(operationContext));
    }

    /**
     * Sets a new value to be used during the expression evaluation.
     *
     * @param name  variable name
     * @param value variable value
     */
    public void setVariable(String name, Object value) {
        parseExpression();
        AbstractVariableValueOperation variableOperation = getVariableOperation(name);
        if (variableOperation != null) {
            variableOperation.setValue(value);
        }
    }

    /**
     * @return a new expression evaluator with the same expression, options and expression context as the original evaluator
     */
    public ExpressionEvaluator copy() {
        if (languageData == null) {
            return new ExpressionEvaluator(expression, options, expressionContext);
        }
        CloningContext cloningCtx = new CloningContext();
        AbstractOperation copy = languageData.operation().copy(cloningCtx);
        ExpressionEvaluator evaluatorCopy = new ExpressionEvaluator(expression, options, expressionContext);
        evaluatorCopy.languageData = new LanguageData(copy, cloningCtx.getVariables(), cloningCtx.getAssignedVariables());
        return evaluatorCopy;
    }

    /**
     * @return a map of all assigned variables and their current values
     */
    public Map<String, Object> listAssignedVariables() {
        Map<String, Object> map = new HashMap<>();
        for (Map.Entry<String, AssignedVariableOperation> entry : languageData.assignedVariables().entrySet()) {
            map.put(entry.getKey(), entry.getValue().getCurrentResult());
        }
        return map;
    }

    /**
     * @return a map of all common variables and their current values
     */
    public Map<String, Object> listVariables() {
        Map<String, Object> map = new HashMap<>();
        for (Map.Entry<String, AbstractVariableValueOperation> entry : languageData.variables().entrySet()) {
            map.put(entry.getKey(), entry.getValue().getCurrentResult());
        }
        return map;
    }

    private void parseExpression() {
        if (languageData == null) {
            LanguageParser languageParser = new DefaultLanguageParser();
            StartContext startContext = createLanguageGrammarContext(CharStreams.fromString(expression), PredictionMode.SLL).start();
            languageData = languageParser.parse(startContext);
        }
    }

    private ExpressionEvaluatorParser createLanguageGrammarContext(CharStream expression, PredictionMode predictionMode) {
        ExpressionEvaluatorLexer lexer = new ExpressionEvaluatorLexer(expression);
        lexer.removeErrorListeners();
        lexer.addErrorListener(PARSING_ERROR_LISTENER);
        ExpressionEvaluatorParser parser = new ExpressionEvaluatorParser(new CommonTokenStream(lexer));
        try {
            parser.getInterpreter().setPredictionMode(predictionMode);
            parser.removeErrorListeners();
            parser.addErrorListener(PARSING_ERROR_LISTENER);
            return parser;
        } catch (Exception e) {
            throw new IllegalStateException("Error during expression parsing", e);
        }
    }

    private OperationContext createOperationContext(boolean allowNull, ExpressionContext expressionContext) {
        ExpressionContext context = expressionContext != null ? expressionContext : this.expressionContext;
        return new OperationContext(options.mathContext(), options.scale(), allowNull,
                new MemoizedSupplier<>(() -> ZonedDateTime.now(options.zoneId()).with(ChronoField.MICRO_OF_SECOND, 0)),
                options.conversionService(), this.expressionContext, context, options.zoneId());
    }

    private AbstractVariableValueOperation getVariableOperation(String name) {
        Certify.requireNonBlank(name, "Parameter name must be provided");
        return languageData.variables().get(name);
    }

    /**
     * @return the expression text
     */
    public String getExpression() {
        return expression;
    }

    /**
     * @return the object that represents the parsed expression and it's contents.
     */
    public LanguageData getLanguageData() {
        parseExpression();
        return languageData;
    }

    /**
     * @return the expression options
     */
    public ExpressionOptions getOptions() {
        return options;
    }

    /**
     * @return the expression context
     */
    public ExpressionContext getExpressionContext() {
        return expressionContext;
    }
}
