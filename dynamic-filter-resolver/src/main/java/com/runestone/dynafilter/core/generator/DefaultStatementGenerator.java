package com.runestone.dynafilter.core.generator;

import com.runestone.assertions.Asserts;
import com.runestone.dynafilter.core.exceptions.StatementGenerationException;
import com.runestone.dynafilter.core.model.FilterData;
import com.runestone.dynafilter.core.model.FilterModifier;
import com.runestone.dynafilter.core.model.statement.*;
import com.runestone.dynafilter.core.operation.ComparisonOperation;
import com.runestone.dynafilter.core.operation.DefinedFilterOperation;
import com.runestone.dynafilter.core.operation.types.Decorated;
import com.runestone.dynafilter.core.operation.types.Dynamic;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public abstract class DefaultStatementGenerator<T> implements StatementGenerator<T> {

    protected final ValueExpressionResolver<?> valueExpressionResolver;

    protected DefaultStatementGenerator() {
        this.valueExpressionResolver = null;
    }

    protected DefaultStatementGenerator(ValueExpressionResolver<?> valueExpressionResolver) {
        this.valueExpressionResolver = valueExpressionResolver;
    }

    protected AbstractStatement createStatements(FilterData[] filtersData, LogicOperator logicType) {
        if (filtersData == null || filtersData.length == 0) {
            return null;
        }
        AbstractStatement statement = ofLogicalStatement(filtersData[0]);
        for (int i = 1; i < filtersData.length; i++) {
            statement = new CompoundStatement(statement, ofLogicalStatement(filtersData[i]), logicType);
        }
        return statement;
    }

    private AbstractStatement ofLogicalStatement(FilterData filterData) {
        LogicalStatement statement = new LogicalStatement(filterData);
        return filterData.negate() ? new NegatedStatement(statement) : statement;
    }

    /**
     * Creates a new instance of {@link FilterData} with the provided parameters.
     *
     * <p>
     * On dynamic filters, the first value is the operation and the second is the value. In addition, the user's choice of negating
     * the filter has priority over the annotation's choice.
     * </p>
     *
     * @param path            the path to the attribute to be filtered
     * @param parameters      the parameters that will be used to build the filter
     * @param targetType      the target type of the attribute to be filtered
     * @param operation       the operation that will be used to build the filter
     * @param negateParameter if the filter's result should be negated
     * @param values          values to be used in the filter. Each line represents a parameter
     * @param modifiers       the modifiers that will be used to build the filter
     * @param description     the user readable description of the filter
     * @return a new instance of {@link FilterData}
     */
    protected FilterData createFilterData(String path, String[] parameters, Class<?> targetType, Class<? super DefinedFilterOperation> operation,
                                          Object negateParameter, Object[] values, List<Class<? extends FilterModifier>> modifiers, String description) {
        Object[] comparisonValue;
        boolean negate;

        if (Dynamic.class.equals(operation)) {
            if (values[0] instanceof Object[] dynamicValuesArray) {
                String comparisonOperationValue;
                try {
                    comparisonOperationValue = (String) dynamicValuesArray[0];
                } catch (ClassCastException e) {
                    throw new StatementGenerationException("Dynamic operation must have a string as first value that indicates the operation");
                }
                comparisonValue = Arrays.copyOfRange(dynamicValuesArray, 1, dynamicValuesArray.length);
                if (comparisonOperationValue.length() == 3) {
                    if (comparisonOperationValue.charAt(0) != 'N' && comparisonOperationValue.charAt(0) != 'n') {
                        throw new StatementGenerationException("Invalid negating character [%s] on path [%s]. 'N' should be preceding the operation, case insensitive"
                                .formatted(comparisonOperationValue.charAt(0), path));
                    }
                    negate = true;
                    operation = ComparisonOperation.valueOf(comparisonOperationValue.substring(1).toUpperCase()).getOperation();
                } else if (comparisonOperationValue.length() == 2) {
                    negate = false;
                    operation = ComparisonOperation.valueOf(comparisonOperationValue.toUpperCase()).getOperation();
                } else {
                    throw new StatementGenerationException("Invalid comparison operation [%s] on path [%s]".formatted(comparisonOperationValue, path));
                }

                if (ComparisonOperation.IN.getOperation().equals(operation) && comparisonValue.length > 0 && !comparisonValue[0].getClass().isArray()) {
                    comparisonValue = new Object[]{comparisonValue};
                } else if (ComparisonOperation.BT.getOperation().equals(operation)) {
                    if (comparisonValue.length != 2) {
                        throw new StatementGenerationException("Between operation must have two values");
                    }
                    parameters = new String[]{parameters[0] + "From", parameters[0] + "To"};
                }
            } else {
                throw new StatementGenerationException("Dynamic operation must have two values, one indicating the operation and another the value");
            }
        } else {
            negate = computeNegatingParameter(negateParameter);
            comparisonValue = values;
        }

        return new FilterData(path, parameters, targetType, operation, negate, comparisonValue, modifiers, description);
    }

    /**
     * Computes the values that will be used to build the filter.
     *
     * @param parameters     the parameters that will be used to build the filter
     * @param defaultValues  the default values that will be used to build the filter
     * @param constantValues the constant values that will be used to build the filter. There values have priority over the default and provided values
     * @param parametersMap  the map of parameters and values provided by the user
     * @return the values that will be used to build the filter
     */
    protected Object[] computeValues(String[] parameters, Object[] defaultValues, Object[] constantValues, Map<String, Object> parametersMap) {
        if (Asserts.isEmpty(parameters)) {
            throw new IllegalArgumentException("Parameters cannot be null or empty");
        } else if (!Asserts.isEmpty(defaultValues) && parameters.length != defaultValues.length) {
            throw new IllegalArgumentException("Parameters and default values have different sizes");
        } else if (!Asserts.isEmpty(constantValues) && parameters.length != constantValues.length) {
            throw new IllegalArgumentException("Parameters and constant values have different sizes");
        }

        if (constantValues != null && constantValues.length > 0) {
            return computeInternalValues(parameters, constantValues, Map.of());
        }
        return computeInternalValues(parameters, defaultValues, parametersMap);
    }

    private Object[] computeInternalValues(String[] parameters, Object[] internalValues, Map<String, Object> parametersMap) {
        Object[] valueList = null;
        for (int i = 0; i < parameters.length; i++) {
            if (Asserts.isEmpty(parameters[i])) {
                throw new IllegalArgumentException("Parameter name cannot be null or empty");
            }
            Object internalValue = internalValues != null && internalValues.length > 0 ? internalValues[i] : null;
            Object value = parametersMap.containsKey(parameters[i]) ? parametersMap.get(parameters[i]) : computeExpressionResolver(internalValue);
            if (value != null) {
                if (valueList == null) {
                    valueList = new Object[parameters.length];
                }
                valueList[i] = value;
            }
        }
        return valueList;
    }

    /**
     *
     */
    private Object computeExpressionResolver(Object value) {
        if (value instanceof Object[] array) {
            return Arrays.stream(array)
                    .mapMulti((obj, mapper) -> {
                        if (obj instanceof String str) {
                            Object resolvedValue = applyExpressionResolver(str);
                            mapper.accept(resolvedValue != null ? resolvedValue : str);
                        } else {
                            mapper.accept(obj);
                        }
                    }).toArray();
        } else if (value instanceof String str) {
            return applyExpressionResolver(str);
        }
        return value;
    }

    /**
     * Computes the negating parameter
     *
     * @param negate the negating parameter to be computed. It's value can be a boolean or a string that will be resolved by the provided {@link ValueExpressionResolver}.
     * @return a boolean indicating if the filter should be negated
     */
    protected Boolean computeNegatingParameter(Object negate) {
        if (negate instanceof Boolean) {
            return (Boolean) negate;
        } else if (negate instanceof String strNegate && !strNegate.isBlank()) {
            if ("true".equalsIgnoreCase(strNegate)) {
                return Boolean.TRUE;
            } else if ("false".equalsIgnoreCase(strNegate)) {
                return Boolean.FALSE;
            }
            Object negateParamResponse = computeExpressionResolver(strNegate);
            if (negateParamResponse != null) {
                if (negateParamResponse instanceof Object[] arr && arr.length > 1) {
                    throw new StatementGenerationException("Default value for negating parameter produced more than one value");
                } else if (negateParamResponse instanceof Object[] arr && arr.length == 1) {
                    return Boolean.parseBoolean(arr[0].toString());
                } else {
                    return Boolean.parseBoolean(negateParamResponse.toString());
                }
            }
        }
        return Boolean.FALSE;
    }

    private Object applyExpressionResolver(String value) {
        try {
            return valueExpressionResolver != null ? valueExpressionResolver.resolveValue(value) : value;
        } catch (Exception e) {
            throw new StatementGenerationException("Provided expression resolver threw an error", e);
        }
    }

}
