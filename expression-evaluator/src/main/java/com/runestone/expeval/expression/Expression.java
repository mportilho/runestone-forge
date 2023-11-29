package com.runestone.expeval.expression;

import com.runestone.expeval.operation.OperationVisitor;
import com.runestone.expeval.operation.values.VariableProvider;
import com.runestone.expeval.support.callsite.OperationCallSite;

import java.lang.invoke.MethodType;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * An expression that can be evaluated for a number or boolean result.
 *
 * @author Marcelo Portilho
 */
public class Expression {

    private final ExpressionEvaluator evaluator;

    /**
     * Creates a new expression with default options and an empty expression context.
     *
     * @param expression the expression to be evaluated
     */
    public Expression(String expression) {
        this(expression, ExpressionOptions.defaultOptions(), new ExpressionContext());
    }

    /**
     * Creates a new expression with the given options and an empty expression context.
     *
     * @param expression the expression to be evaluated
     * @param options    the expression options
     */
    public Expression(String expression, ExpressionOptions options) {
        this(expression, options, new ExpressionContext());
    }

    /**
     * Creates a new expression with default options and the given expression context.
     *
     * @param expression        the expression to be evaluated
     * @param expressionContext the expression context with initial values
     */
    public Expression(String expression, ExpressionContext expressionContext) {
        this(expression, ExpressionOptions.defaultOptions(), expressionContext);
    }

    /**
     * Creates a new expression with the given options and expression context.
     *
     * @param expression        the expression to be evaluated
     * @param options           the expression options
     * @param expressionContext the expression context with initial values
     */
    public Expression(String expression, ExpressionOptions options, ExpressionContext expressionContext) {
        this.evaluator = new ExpressionEvaluator(expression, options, expressionContext);
    }

    private Expression(ExpressionEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    /**
     * Evaluates the expression and returns the result as a number or boolean.
     *
     * @param <T> the result type
     * @return the result of the expression
     */
    public <T> T evaluate() {
        return evaluator.evaluate(null);
    }

    /**
     * Evaluates the expression and returns the result as a number or boolean.
     *
     * @param expressionContext the expression context that will have precedence over the current expression context
     * @param <T>               the result type
     * @return the result of the expression
     */
    public <T> T evaluate(ExpressionContext expressionContext) {
        return evaluator.evaluate(expressionContext);
    }

    /**
     * Adds a new entry to the expression context dictionary used during variable evaluation.
     *
     * @param key   the name of the entry
     * @param value the value of the entry
     */
    public void addDictionaryEntry(String key, Object value) {
        evaluator.getExpressionContext().putDictionaryEntry(key, value);
    }

    /**
     * Adds a dictionary of entries to the expression context. The dictionary cannot be null.
     *
     * @param dictionary the dictionary of entries
     */
    public void addDictionary(Map<String, Object> dictionary) {
        evaluator.getExpressionContext().putDictionary(dictionary);
    }

    /**
     * Adds a new function to the expression context.
     *
     * @param function the function to be added
     */
    public void addFunction(OperationCallSite function) {
        evaluator.getExpressionContext().putFunction(function);
    }

    /**
     * Adds a new function to the expression context. Each position of the array of objects passed to the function represents a parameter.
     * <p>
     * Ex.: The following code adds a function called "adder" that receives two parameters and returns the sum of them.
     * <pre>{@code
     * Expression expression = new Expression("adder(1, 2)");
     * var methodType = MethodType.methodType(BigDecimal.class, BigDecimal.class, BigDecimal.class);
     * expression.addFunction("adder", methodType, p -> ((BigDecimal) p[0]).add((BigDecimal) p[1]));
     * expression.evaluate();
     * }</pre>
     *
     * @param functionName the name of the function to be used in the expression
     * @param methodType   the method type of the function
     * @param function     the function implementation
     */
    public void addFunction(String functionName, MethodType methodType, Function<Object[], Object> function) {
        evaluator.getExpressionContext().putFunction(functionName, methodType, function);
    }

    /**
     * Adds all public functions found in the given object to the expression context.
     * <p>
     * If the provider is an instance, all public methods will be added as functions.
     * <p>
     * If the provider is a {@link Class}, all public static methods will be added as functions.
     *
     * @param functionProvider the object containing the functions
     */
    public void addFunctionFromObject(Object functionProvider) {
        evaluator.getExpressionContext().putFunctionsFromProvider(functionProvider);
    }

    /**
     * Sets a new value to be used during the expression evaluation.
     *
     * @param variableName variable name
     * @param value        variable value
     */
    public void setVariable(String variableName, Object value) {
        evaluator.setVariable(variableName, value);
    }

    /**
     * Sets the variables to be used during the expression evaluation.
     *
     * @param variables variables to be set
     */
    public void setVariables(Map<String, Object> variables) {
        variables.forEach((name, value) -> {
            if (value instanceof VariableProvider variableProvider) {
                setVariableProvider(name, variableProvider);
            } else if (value instanceof Supplier<?> supplier) {
                setVariableProvider(name, supplier);
            } else {
                setVariable(name, value);
            }
        });
    }

    /**
     * Sets a provider for a variable value to be used during the expression evaluation.
     *
     * @param variableName variable name
     * @param provider     variable provider
     */
    public void setVariableProvider(String variableName, VariableProvider provider) {
        setVariable(variableName, provider);
    }

    /**
     * Sets a supplier for a variable value to be used during the expression evaluation.
     *
     * @param variableName variable name
     * @param supplier     variable supplier
     */
    public void setVariableProvider(String variableName, Supplier<?> supplier) {
        setVariable(variableName, (VariableProvider) context -> supplier.get());
    }

    /**
     * @return a map of all assigned variables and their current values
     */
    public Map<String, Object> getAssignedVariables() {
        return evaluator.listAssignedVariables();
    }

    /**
     * @return a map of all common variables and their current values
     */
    public Map<String, Object> getVariables() {
        return evaluator.listVariables();
    }

    /**
     * Creates a copy of this expression. The copy will have a blank id.
     *
     * @return a copy of this expression
     */
    public final Expression copy() {
        return new Expression(evaluator.copy());
    }

    /**
     * Traverses the expression tree evaluating each operation when possible. This is useful to speed up the subsequent evaluations of the expression.
     * <p>
     * Ex.: creating a cache of semi evaluated expressions to be cloned and evaluated as needed.
     *
     * @return this expression
     */
    public final Expression warmUp() {
        evaluator.warmUp();
        return this;
    }

    /**
     * Visits all operations in the expression tree.
     *
     * @param visitor the visitor
     * @param <T>     the result type
     * @return the final result of the visitor or null if the visitor does not return any result
     */
    public <T> T visitOperations(OperationVisitor<T> visitor) {
        evaluator.getLanguageData().operation().accept(visitor);
        return visitor.getFinalResult();
    }

    /**
     * @return the expression options
     */
    public ExpressionOptions getOptions() {
        return evaluator.getOptions();
    }

    /**
     * @return the original textual expression
     */
    public String getExpression() {
        return evaluator.getExpression();
    }

    /**
     * @return the string representation of the parsed expression tree
     */
    @Override
    public String toString() {
        return evaluator.getLanguageData().operation().toString();
    }

}
