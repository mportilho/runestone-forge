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
import com.runestone.expeval.operation.values.VariableProvider;
import com.runestone.expeval.operation.values.variable.AssignedVariableOperation;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.atn.PredictionMode;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import com.runestone.utils.cache.LruCache;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Responsible for evaluating an expression. It parses the text expression and creates an operation tree that can be evaluated, navigated or cloned.
 *
 * @author Marcelo Portilho
 */
class ExpressionEvaluator {

    private static final ParsingErrorListener PARSING_ERROR_LISTENER = new ParsingErrorListener();
    private static final LruCache<String, LanguageData> GLOBAL_LANGUAGE_DATA_CACHE = new LruCache<>(1024);

    private final String expression;
    private final ExpressionOptions options;
    private final ExpressionContext expressionContext;

    private LanguageData languageData;

    // Cached eval context — avoids allocating OperationContext + CurrentDateTimeSupplier on each evaluate()
    private ExpressionContext lastUserContext;
    private OperationContext cachedEvalContext;
    private CurrentDateTimeSupplier sharedDateTimeSupplier;

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
        OperationContext operationContext = getOrCreateEvalContext(expressionContext);
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

    public void setVariables(Map<String, Object> variables) {
        Objects.requireNonNull(variables, "Variables cannot be null");
        if (variables.isEmpty()) {
            return;
        }
        parseExpression();
        for (Map.Entry<String, AbstractVariableValueOperation> variableEntry : languageData.variables().entrySet()) {
            String variableName = variableEntry.getKey();
            if (!variables.containsKey(variableName)) {
                continue;
            }
            AbstractVariableValueOperation variableOperation = variableEntry.getValue();
            Object value = variables.get(variableName);
            if (value instanceof VariableProvider variableProvider) {
                variableOperation.setValue(variableProvider);
            } else if (value instanceof Supplier<?> supplier) {
                variableOperation.setValue((VariableProvider) context -> supplier.get());
            } else {
                variableOperation.setValue(value);
            }
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
        Map<String, Object> map = new HashMap<>(languageData.assignedVariables().size());
        for (Map.Entry<String, AssignedVariableOperation> entry : languageData.assignedVariables().entrySet()) {
            map.put(entry.getKey(), entry.getValue().getCurrentResult());
        }
        return map;
    }

    /**
     * @return a map of all common variables and their current values
     */
    public Map<String, Object> listVariables() {
        Map<String, Object> map = new HashMap<>(languageData.variables().size());
        for (Map.Entry<String, AbstractVariableValueOperation> entry : languageData.variables().entrySet()) {
            map.put(entry.getKey(), entry.getValue().getCurrentResult());
        }
        return map;
    }

    private void parseExpression() {
        if (languageData == null) {
            LanguageData template = GLOBAL_LANGUAGE_DATA_CACHE.computeIfAbsent(expression, this::doParse);
            CloningContext cloningCtx = new CloningContext();
            AbstractOperation copy = template.operation().copy(cloningCtx);
            languageData = new LanguageData(copy, cloningCtx.getVariables(), cloningCtx.getAssignedVariables());
        }
    }

    private LanguageData doParse(String expr) {
        LanguageParser languageParser = new DefaultLanguageParser();
        StartContext startContext = createLanguageGrammarContext(CharStreams.fromString(expr), PredictionMode.SLL)
                .start();
        return languageParser.parse(startContext);
    }

    static void clearGlobalCache() {
        GLOBAL_LANGUAGE_DATA_CACHE.clear();
    }

    private ExpressionEvaluatorParser createLanguageGrammarContext(CharStream expression,
            PredictionMode predictionMode) {
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

    /**
     * Returns a cached {@link OperationContext} for evaluate() calls, avoiding per-call allocation.
     * The {@link CurrentDateTimeSupplier} is reset on each call so date/time stays current.
     * A new context is built only when the userContext identity changes.
     */
    private OperationContext getOrCreateEvalContext(ExpressionContext userContext) {
        ExpressionContext resolvedUser = userContext != null ? userContext : this.expressionContext;
        if (sharedDateTimeSupplier == null) {
            sharedDateTimeSupplier = new CurrentDateTimeSupplier(options.zoneId());
        } else {
            sharedDateTimeSupplier.reset();
        }
        if (cachedEvalContext != null && resolvedUser == lastUserContext) {
            return cachedEvalContext;
        }
        lastUserContext = resolvedUser;
        cachedEvalContext = new OperationContext(options.mathContext(), options.scale(), false,
                sharedDateTimeSupplier, options.conversionService(), this.expressionContext, resolvedUser,
                options.zoneId());
        return cachedEvalContext;
    }

    private OperationContext createOperationContext(boolean allowNull, ExpressionContext expressionContext) {
        ExpressionContext context = expressionContext != null ? expressionContext : this.expressionContext;
        return new OperationContext(options.mathContext(), options.scale(), allowNull,
                new CurrentDateTimeSupplier(options.zoneId()),
                options.conversionService(), this.expressionContext, context, options.zoneId());
    }

    private static final class CurrentDateTimeSupplier implements Supplier<Temporal> {

        private final java.time.ZoneId zoneId;
        private Temporal currentDateTime;

        private CurrentDateTimeSupplier(java.time.ZoneId zoneId) {
            this.zoneId = zoneId;
        }

        void reset() {
            currentDateTime = null;
        }

        @Override
        public Temporal get() {
            Temporal value = currentDateTime;
            if (value == null) {
                value = ZonedDateTime.now(zoneId).with(ChronoField.MICRO_OF_SECOND, 0);
                currentDateTime = value;
            }
            return value;
        }
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
