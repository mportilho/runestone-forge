package com.runestone.expeval.expression.calculator;

import com.runestone.expeval.expression.Expression;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents the memory of a calculation. It contains the input expression, it's result and the variables used in the evaluation.
 *
 * @param inputData         the input expression to be calculated
 * @param expression        the evaluated expression
 * @param result            the result of the expression's evaluation
 * @param variables         the variables used in the evaluation
 * @param assignedVariables the variables that were assigned during the evaluation
 * @param contextVariables  the variables that were present as context during each evaluation
 * @author Marcelo Portilho
 */
public record CalculationMemory(

        CalculatorInput inputData,
        Expression expression,
        Object result,
        Map<String, Object> variables,
        Map<String, Object> assignedVariables,
        Map<String, Object> contextVariables

) {

    public CalculationMemory {
        Objects.requireNonNull(expression, "Expression cannot be null");
        variables = Collections.unmodifiableMap(variables);
        assignedVariables = Collections.unmodifiableMap(assignedVariables);
        contextVariables = Collections.unmodifiableMap(contextVariables);
    }

    /**
     * Finds a numeric variable in the computation memory by its name. It will first look for assigned variable list and then for variable list.
     * <p>
     * The variable must be of a BigDecimal type or an empty optional will be returned.
     *
     * @param variableName the name of the variable
     * @return the numeric value of the variable or empty if it does not exist
     */
    public Optional<BigDecimal> findNumericVariable(String variableName) {
        if (assignedVariables.get(variableName) instanceof BigDecimal number) {
            return Optional.of(number);
        } else if (variables.get(variableName) instanceof BigDecimal number) {
            return Optional.of(number);
        } else if (contextVariables.get(variableName) instanceof BigDecimal number) {
            return Optional.of(number);
        }
        return Optional.empty();
    }

    /**
     * Finds a boolean variable in the computation memory by its name. It will first look for assigned variable list and then for variable list.
     * <p>
     * The variable must be a boolean value or an empty optional will be returned.
     *
     * @param variableName the name of the variable
     * @return the boolean value of the variable or empty if it does not exist
     */
    public Optional<Boolean> findBooleanVariable(String variableName) {
        if (assignedVariables.get(variableName) instanceof Boolean aBoolean) {
            return Optional.of(aBoolean);
        } else if (variables.get(variableName) instanceof Boolean aBoolean) {
            return Optional.of(aBoolean);
        } else if (contextVariables.get(variableName) instanceof Boolean aBoolean) {
            return Optional.of(aBoolean);
        }
        return Optional.empty();
    }

}
