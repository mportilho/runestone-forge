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

package com.runestone.dynafilter.core.generator.annotation;

import com.runestone.dynafilter.core.generator.DefaultStatementGenerator;
import com.runestone.dynafilter.core.generator.StatementWrapper;
import com.runestone.dynafilter.core.generator.ValueExpressionResolver;
import com.runestone.dynafilter.core.model.FilterData;
import com.runestone.dynafilter.core.model.statement.*;
import com.runestone.dynafilter.core.operation.types.Decorated;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AnnotationStatementGenerator extends DefaultStatementGenerator<AnnotationStatementInput> {

    private static final FilterData[] EMPTY_FILTER_DATA = {};
    private static final StatementWrapper EMPTY_STATEMENT_WRAPPER = new StatementWrapper(new NoOpStatement(), Collections.emptyMap());

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

        List<FilterAnnotationData> filterAnnotationDataList = TypeAnnotationUtils.findAnnotationData(filterInputs);
        for (FilterAnnotationData data : filterAnnotationDataList) {
            AbstractStatement statements = createStatements(data, parametersMap);
            if (statements != null) {
                statementList.add(statements);
            }
        }

        Map<String, FilterData> decoratedFilters = createDecoratedFiltersData(filterAnnotationDataList, parametersMap);

        if (statementList.isEmpty()) {
            return EMPTY_STATEMENT_WRAPPER;
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

    private Map<String, FilterData> createDecoratedFiltersData(List<FilterAnnotationData> filterAnnotationDataList, Map<String, Object> parametersMap) {
        return filterAnnotationDataList
                .stream()
                .flatMap(data -> data.filters().stream())
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
    private AbstractStatement createStatements(FilterAnnotationData data, Map<String, Object> userParameters) {
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

    private AbstractStatement createStatementFromFilterStatements(List<FilterAnnotationStatement> statements, LogicOperator logicType, Map<String, Object> userParameters) {
        AbstractStatement resultStatement = null;
        for (FilterAnnotationStatement filterStatement : statements) {
            boolean negate = computeNegatingParameter(filterStatement.negate());
            FilterData[] params = processFilterAnnotations(filterStatement.filters(), userParameters);
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
    private FilterData[] processFilterAnnotations(List<Filter> filters, Map<String, Object> userParameters) {
        if (filters == null || filters.isEmpty()) {
            return EMPTY_FILTER_DATA;
        }
        for (Filter filter : filters) { // fail fast
            validateFilter(filter);
        }

        List<FilterData> filterParameters = new ArrayList<>(filters.size());
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
}
