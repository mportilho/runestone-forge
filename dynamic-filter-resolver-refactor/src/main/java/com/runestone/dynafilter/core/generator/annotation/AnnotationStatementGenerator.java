package com.runestone.dynafilter.core.generator.annotation;

import com.runestone.dynafilter.core.annotation.Filter;
import com.runestone.dynafilter.core.generator.DefaultStatementGenerator;
import com.runestone.dynafilter.core.generator.StatementGenerator;
import com.runestone.dynafilter.core.generator.StatementWrapper;
import com.runestone.dynafilter.core.model.FilterData;
import com.runestone.dynafilter.core.model.FilterRequestData;
import com.runestone.dynafilter.core.operation.Decorated;
import com.runestone.dynafilter.core.statement.AbstractStatement;
import com.runestone.dynafilter.core.statement.CompoundStatement;
import com.runestone.dynafilter.core.statement.LogicOperator;
import com.runestone.dynafilter.core.statement.NoOpStatement;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class AnnotationStatementGenerator implements StatementGenerator {

    private final DefaultStatementGenerator statementGenerator;

    public AnnotationStatementGenerator() {
        this(new DefaultStatementGenerator());
    }

    public AnnotationStatementGenerator(DefaultStatementGenerator statementGenerator) {
        this.statementGenerator = Objects.requireNonNull(statementGenerator, "statementGenerator must not be null");
    }

    @Override
    public StatementWrapper generateStatements(AnnotationStatementInput filterInputs, Map<String, Object> filterParameters) {
        Objects.requireNonNull(filterInputs, "filterInputs must not be null");
        Map<String, Object> safeParameters = filterParameters == null ? Map.of() : filterParameters;
        List<FilterAnnotationData> annotationData = TypeAnnotationUtils.findAnnotationData(filterInputs);
        List<AbstractStatement> statements = new ArrayList<>();
        for (FilterAnnotationData data : annotationData) {
            AbstractStatement statement = statementGenerator.createStatements(data, safeParameters);
            if (statement != null) {
                statements.add(statement);
            }
        }

        List<FilterRequestData> allFilters = TypeAnnotationUtils.listAllFilterRequestData(filterInputs);
        Map<String, FilterData> decoratedFilters = decoratedFilters(annotationData, safeParameters);
        AbstractStatement rootStatement = rootStatement(statements);
        return new StatementWrapper(rootStatement, decoratedFilters, allFilters);
    }

    private Map<String, FilterData> decoratedFilters(
            List<FilterAnnotationData> annotationData,
            Map<String, Object> safeParameters
    ) {
        Map<String, FilterData> decoratedFilters = new LinkedHashMap<>();
        for (FilterAnnotationData data : annotationData) {
            addDecoratedFilters(data.filters(), safeParameters, decoratedFilters);
            for (FilterAnnotationStatement statement : data.filterStatements()) {
                addDecoratedFilters(statement.filters(), safeParameters, decoratedFilters);
            }
        }
        return decoratedFilters;
    }

    private void addDecoratedFilters(
            List<Filter> filters,
            Map<String, Object> safeParameters,
            Map<String, FilterData> decoratedFilters
    ) {
        for (Filter filter : filters) {
            if (!filter.operation().equals(Decorated.class)) {
                continue;
            }
            Object[] values = statementGenerator.computeValues(
                    filter.parameters(),
                    filter.defaultValues(),
                    filter.constantValues(),
                    safeParameters
            );
            if (values.length == 0) {
                continue;
            }
            FilterData filterData = statementGenerator.createFilterData(
                    filter.path(),
                    filter.parameters(),
                    filter.targetType(),
                    filter.operation(),
                    filter.negate(),
                    values,
                    List.of(filter.modifiers()),
                    filter.description()
            );
            decoratedFilters.put(filterData.path(), filterData);
        }
    }

    private static AbstractStatement rootStatement(List<AbstractStatement> statements) {
        if (statements.isEmpty()) {
            return new NoOpStatement();
        }
        AbstractStatement current = statements.getFirst();
        for (int index = 1; index < statements.size(); index++) {
            current = new CompoundStatement(current, statements.get(index), LogicOperator.CONJUNCTION);
        }
        return current;
    }
}
