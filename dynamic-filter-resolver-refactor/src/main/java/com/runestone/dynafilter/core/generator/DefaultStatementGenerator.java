package com.runestone.dynafilter.core.generator;

import com.runestone.dynafilter.core.annotation.Filter;
import com.runestone.dynafilter.core.exception.StatementGenerationException;
import com.runestone.dynafilter.core.expression.ValueExpressionResolver;
import com.runestone.dynafilter.core.generator.annotation.FilterAnnotationData;
import com.runestone.dynafilter.core.generator.annotation.FilterAnnotationStatement;
import com.runestone.dynafilter.core.model.FilterData;
import com.runestone.dynafilter.core.modifier.FilterModifier;
import com.runestone.dynafilter.core.operation.Decorated;
import com.runestone.dynafilter.core.operation.DefinedFilterOperation;
import com.runestone.dynafilter.core.operation.Dynamic;
import com.runestone.dynafilter.core.operation.DynamicOperationRequest;
import com.runestone.dynafilter.core.operation.DynamicOperationResolver;
import com.runestone.dynafilter.core.statement.AbstractStatement;
import com.runestone.dynafilter.core.statement.CompoundStatement;
import com.runestone.dynafilter.core.statement.LogicOperator;
import com.runestone.dynafilter.core.statement.LogicalStatement;
import com.runestone.dynafilter.core.statement.NegatedStatement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DefaultStatementGenerator {

    private final ValueExpressionResolver valueExpressionResolver;
    private final DynamicOperationResolver dynamicOperationResolver;

    public DefaultStatementGenerator() {
        this(null);
    }

    public DefaultStatementGenerator(ValueExpressionResolver valueExpressionResolver) {
        this.valueExpressionResolver = valueExpressionResolver;
        this.dynamicOperationResolver = new DynamicOperationResolver();
    }

    public AbstractStatement createStatements(FilterAnnotationData data, Map<String, Object> parametersMap) {
        Objects.requireNonNull(data, "data must not be null");
        Map<String, Object> safeParameters = parametersMap == null ? Map.of() : parametersMap;
        List<AbstractStatement> statements = new ArrayList<>();
        for (Filter filter : data.filters()) {
            AbstractStatement statement = createStatement(filter, safeParameters);
            if (statement != null) {
                statements.add(statement);
            }
        }

        LogicOperator nestedOperator = opposite(data.logicOperator());
        for (FilterAnnotationStatement filterStatement : data.filterStatements()) {
            AbstractStatement statement = createStatementFromFilterStatement(filterStatement, nestedOperator, safeParameters);
            if (statement != null) {
                statements.add(statement);
            }
        }

        AbstractStatement root = combine(statements, data.logicOperator());
        if (root != null && computeNegatingParameter(data.negate())) {
            return new NegatedStatement(root);
        }
        return root;
    }

    public AbstractStatement createStatement(Filter filter, Map<String, Object> parametersMap) {
        Objects.requireNonNull(filter, "filter must not be null");
        if (filter.operation().equals(Decorated.class)) {
            return null;
        }
        Object[] values = computeValues(filter.parameters(), filter.defaultValues(), filter.constantValues(), parametersMap);
        if (values.length == 0) {
            if (filter.required()) {
                throw new StatementGenerationException("Required filter parameter is missing: " + String.join(",", filter.parameters()));
            }
            return null;
        }
        FilterData filterData = createFilterData(
                filter.path(),
                filter.parameters(),
                filter.targetType(),
                filter.operation(),
                filter.negate(),
                values,
                List.of(filter.modifiers()),
                filter.description()
        );
        AbstractStatement statement = new LogicalStatement(filterData);
        return filterData.negate() ? new NegatedStatement(statement) : statement;
    }

    public FilterData createFilterData(
            String path,
            String[] parameters,
            Class<?> targetType,
            Class<? extends DefinedFilterOperation> operation,
            Object negateParameter,
            Object[] values,
            List<Class<? extends FilterModifier>> modifiers,
            String description
    ) {
        Objects.requireNonNull(operation, "operation must not be null");
        Object[] safeValues = values == null ? new Object[0] : values.clone();
        boolean negate = computeNegatingParameter(negateParameter);
        if (operation.equals(Dynamic.class)) {
            try {
                FilterData dynamicFilter = dynamicOperationResolver.resolve(new DynamicOperationRequest(
                        path,
                        firstParameter(parameters),
                        targetType,
                        safeValues,
                        modifiers,
                        description
                ));
                if (negate && !dynamicFilter.negate()) {
                    return new FilterData(
                            dynamicFilter.path(),
                            dynamicFilter.parameters(),
                            dynamicFilter.targetType(),
                            dynamicFilter.operation(),
                            true,
                            dynamicFilter.values(),
                            dynamicFilter.modifiers(),
                            dynamicFilter.description()
                    );
                }
                return dynamicFilter;
            } catch (RuntimeException exception) {
                throw new StatementGenerationException("Invalid dynamic operation", exception);
            }
        }
        return new FilterData(path, parameters, targetType, operation, negate, safeValues, modifiers, description);
    }

    public Object[] computeValues(
            String[] parameters,
            Object[] defaultValues,
            Object[] constantValues,
            Map<String, Object> parametersMap
    ) {
        validateParameters(parameters);
        Map<String, Object> safeParameters = parametersMap == null ? Map.of() : parametersMap;
        Object[] constants = constantValues == null ? new Object[0] : constantValues.clone();
        Object[] defaults = defaultValues == null ? new Object[0] : defaultValues.clone();
        if (constants.length > 0) {
            return resolveValues(constants);
        }
        Object[] requestValues = new Object[parameters.length];
        for (int index = 0; index < parameters.length; index++) {
            if (!safeParameters.containsKey(parameters[index])) {
                requestValues = null;
                break;
            }
            requestValues[index] = safeParameters.get(parameters[index]);
        }
        if (requestValues != null) {
            return requestValues;
        }
        if (defaults.length > 0) {
            return resolveValues(defaults);
        }
        return new Object[0];
    }

    private AbstractStatement createStatementFromFilterStatement(
            FilterAnnotationStatement filterStatement,
            LogicOperator operator,
            Map<String, Object> parametersMap
    ) {
        List<AbstractStatement> statements = new ArrayList<>();
        for (Filter filter : filterStatement.filters()) {
            AbstractStatement statement = createStatement(filter, parametersMap);
            if (statement != null) {
                statements.add(statement);
            }
        }
        AbstractStatement root = combine(statements, operator);
        if (root != null && computeNegatingParameter(filterStatement.negate())) {
            return new NegatedStatement(root);
        }
        return root;
    }

    private Object[] resolveValues(Object[] values) {
        Object[] resolvedValues = values.clone();
        for (int index = 0; index < resolvedValues.length; index++) {
            resolvedValues[index] = resolveValue(resolvedValues[index]);
        }
        return resolvedValues;
    }

    private Object resolveValue(Object value) {
        if (valueExpressionResolver == null) {
            return value;
        }
        if (value instanceof String stringValue) {
            return valueExpressionResolver.resolveValue(stringValue);
        }
        if (value instanceof String[] stringValues) {
            Object[] resolvedValues = new Object[stringValues.length];
            for (int index = 0; index < stringValues.length; index++) {
                resolvedValues[index] = valueExpressionResolver.resolveValue(stringValues[index]);
            }
            return resolvedValues;
        }
        return value;
    }

    private boolean computeNegatingParameter(Object negateParameter) {
        Object resolvedNegate = resolveValue(negateParameter);
        return resolvedNegate instanceof Boolean booleanValue
                ? booleanValue
                : Boolean.parseBoolean(String.valueOf(resolvedNegate));
    }

    private static AbstractStatement combine(List<AbstractStatement> statements, LogicOperator operator) {
        if (statements.isEmpty()) {
            return null;
        }
        AbstractStatement current = statements.getFirst();
        for (int index = 1; index < statements.size(); index++) {
            current = new CompoundStatement(current, statements.get(index), operator);
        }
        return current;
    }

    private static LogicOperator opposite(LogicOperator operator) {
        return operator == LogicOperator.CONJUNCTION ? LogicOperator.DISJUNCTION : LogicOperator.CONJUNCTION;
    }

    private static String firstParameter(String[] parameters) {
        validateParameters(parameters);
        return parameters[0];
    }

    private static void validateParameters(String[] parameters) {
        Objects.requireNonNull(parameters, "parameters must not be null");
        if (parameters.length == 0) {
            throw new StatementGenerationException("Filter parameters must not be empty");
        }
        for (String parameter : parameters) {
            if (parameter == null || parameter.isBlank()) {
                throw new StatementGenerationException("Filter parameter names must not be blank");
            }
        }
    }
}
