package com.runestone.dynafilter.core.generator.annotation;

import com.runestone.dynafilter.core.generator.DefaultStatementGenerator;
import com.runestone.dynafilter.core.generator.StatementWrapper;
import com.runestone.dynafilter.core.generator.ValueExpressionResolver;
import com.runestone.dynafilter.core.model.FilterData;
import com.runestone.dynafilter.core.model.statement.AbstractStatement;
import com.runestone.dynafilter.core.model.statement.CompoundStatement;
import com.runestone.dynafilter.core.model.statement.LogicOperator;
import com.runestone.dynafilter.core.model.statement.NegatedStatement;
import com.runestone.dynafilter.core.operation.types.Decorated;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AnnotationStatementGenerator extends DefaultStatementGenerator<AnnotationStatementInput> {

    private static final FilterData[] EMPTY_FILTER_DATA = {};

    public AnnotationStatementGenerator() {
        super();
    }

    public AnnotationStatementGenerator(ValueExpressionResolver<?> valueExpressionResolver) {
        super(valueExpressionResolver);
    }

    @Override
    public StatementWrapper generateStatements(AnnotationStatementInput filterInputs, Map<String, Object> filterParameters) {
        Map<String, Object> parametersMap = filterParameters != null ? filterParameters : Collections.emptyMap();
        List<AbstractStatement> statementList = new ArrayList<>();

        for (Annotation ann : TypeAnnotationUtils.findStatementAnnotations(filterInputs)) {
            AbstractStatement statements = createStatements(ann, parametersMap);
            if (statements != null) {
                statementList.add(statements);
            }
        }

        Map<String, FilterData> decoratedFilters = createDecoratedFiltersData(filterInputs, parametersMap);

        if (statementList.isEmpty()) {
            return null;
        } else if (statementList.size() == 1) {
            return new StatementWrapper(statementList.get(0), decoratedFilters);
        } else {
            AbstractStatement finalStatement = statementList.get(0);
            for (int i = 1; i < statementList.size(); i++) {
                finalStatement = new CompoundStatement(finalStatement, statementList.get(i), LogicOperator.CONJUNCTION);
            }
            return new StatementWrapper(finalStatement, decoratedFilters);
        }
    }

    private Map<String, FilterData> createDecoratedFiltersData(AnnotationStatementInput filterInputs, Map<String, Object> parametersMap) {
        return TypeAnnotationUtils.retrieveFilterAnnotations(filterInputs)
                .stream()
                .filter(filter -> Decorated.class.equals(filter.operation()))
                .map(filter -> {
                    Object[] values = computeValues(filter.parameters(), filter.defaultValues(), filter.constantValues(), parametersMap);
                    return values != null ? createFilterData(filter.path(), filter.parameters(), filter.targetType(), filter.operation(),
                            filter.negate(), values, List.of(filter.modifiers()), filter.description()) : null;
                }).filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableMap(FilterData::path, Function.identity()));
    }

    /**
     *
     */
    private AbstractStatement createStatements(Annotation annotation, Map<String, Object> userParameters) {
        AnnotationData data = createAnnotationData(annotation);
        boolean negate = computeNegatingParameter(data.negate());

        FilterData[] clauses = processFilterAnnotations(data.filters(), userParameters);
        AbstractStatement statement = createStatements(clauses, data.logicOperator());
        AbstractStatement statementFromStatements = createStatementFromFilterStatements(data.filterStatements(), data.logicOperator(), userParameters);

        if (statement == null && statementFromStatements == null) {
            return null;
        } else if (statement == null) {
            return negate ? new NegatedStatement(statementFromStatements) : statementFromStatements;
        } else if (statementFromStatements == null) {
            return negate ? new NegatedStatement(statement) : statement;
        } else {
            return negate
                    ? new NegatedStatement(new CompoundStatement(statement, statementFromStatements, data.logicOperator()))
                    : new CompoundStatement(statement, statementFromStatements, data.logicOperator());
        }
    }

    private AnnotationData createAnnotationData(Annotation annotation) {
        Conjunction conjunction = Conjunction.class.equals(annotation.annotationType()) ? (Conjunction) annotation : null;
        Disjunction disjunction = Disjunction.class.equals(annotation.annotationType()) ? (Disjunction) annotation : null;
        if (conjunction != null || disjunction != null) {
            LogicOperator logicType = conjunction != null ? LogicOperator.CONJUNCTION : LogicOperator.DISJUNCTION;
            Filter[] filters = conjunction != null ? conjunction.value() : disjunction.value();
            Statement[] filterStatements = conjunction != null ? conjunction.disjunctions() : disjunction.conjunctions();
            String strNegate = conjunction != null ? conjunction.negate() : disjunction.negate();
            return new AnnotationData(logicType, filters, filterStatements, strNegate);
        }
        throw new IllegalArgumentException("Filter Annotation is not a conjunction or disjunction");
    }

    private AbstractStatement createStatementFromFilterStatements(Statement[] statements, LogicOperator logicType, Map<String, Object> userParameters) {
        AbstractStatement resultStatement = null;
        for (Statement filterStatement : statements) {
            boolean negate = computeNegatingParameter(filterStatement.negate());
            FilterData[] params = processFilterAnnotations(filterStatement.value(), userParameters);
            AbstractStatement currStatement = createStatements(params, logicType.opposite());
            currStatement = negate ? new NegatedStatement(currStatement) : currStatement;

            if (resultStatement == null) {
                resultStatement = currStatement;
            } else {
                resultStatement = new CompoundStatement(resultStatement, currStatement, logicType);
            }
        }
        return resultStatement;
    }

    /**
     *
     */
    private FilterData[] processFilterAnnotations(Filter[] filters, Map<String, Object> userParameters) {
        if (filters == null || filters.length == 0) {
            return EMPTY_FILTER_DATA;
        }
        for (Filter filter : filters) { // fail fast
            validateFilter(filter);
        }

        List<FilterData> filterParameters = new ArrayList<>(filters.length);
        for (Filter filter : filters) {
            if (Decorated.class.equals(filter.operation())) {
                continue;
            }
            Object[] values = computeValues(filter.parameters(), filter.defaultValues(), filter.constantValues(), userParameters);
            if (values == null || values.length == 0) {
                if (filter.required()) {
                    String pluralChar = filter.parameters().length > 1 ? "s" : "";
                    String parameters = String.join(", ", filter.parameters());
                    throw new IllegalArgumentException(String.format("Parameter%s '%s' required", pluralChar, parameters));
                }
                continue;
            }
            var filterData = createFilterData(filter.path(), filter.parameters(), filter.targetType(), filter.operation(), filter.negate(), values, List.of(filter.modifiers()), filter.description());
            filterParameters.add(filterData);
        }
        return filterParameters.toArray(FilterData[]::new);
    }

    /**
     *
     */
    private void validateFilter(Filter filter) {
        if (filter.parameters().length == 0) {
            throw new IllegalArgumentException("No parameter configured for filter of path " + filter.path());
        }
        if (filter.constantValues().length != 0 && filter.constantValues().length != filter.parameters().length) {
            throw new IllegalArgumentException(String.format("Parameters and constant values have different sizes. Parameters required: '%s'",
                    String.join(", ", Arrays.asList(filter.parameters()))));
        }
        if (filter.defaultValues().length != 0 && filter.defaultValues().length != filter.parameters().length) {
            throw new IllegalArgumentException(String.format("Parameters and default values have different sizes. Parameters required: '%s'",
                    String.join(", ", Arrays.asList(filter.parameters()))));
        }
    }

    private record AnnotationData(LogicOperator logicOperator, Filter[] filters, Statement[] filterStatements, String negate) {
    }
}
