package com.runestone.dynafilter.core.generator.annotation;

import com.runestone.dynafilter.core.model.FilterModifier;
import com.runestone.dynafilter.core.model.FilterRequestData;
import com.runestone.dynafilter.core.model.statement.LogicOperator;
import com.runestone.dynafilter.core.operation.FilterOperation;
import com.runestone.dynafilter.core.operation.types.Decorated;

import java.util.ArrayList;
import java.util.List;

final class CompiledAnnotationPlan {

    private final List<StatementPlan> statements;
    private final List<FilterPlan> decoratedFilters;
    private final List<FilterRequestData> requestFilters;

    private CompiledAnnotationPlan(List<StatementPlan> statements, List<FilterPlan> decoratedFilters,
                                   List<FilterRequestData> requestFilters) {
        this.statements = List.copyOf(statements);
        this.decoratedFilters = List.copyOf(decoratedFilters);
        this.requestFilters = requestFilters;
    }

    static CompiledAnnotationPlan compile(TypeAnnotationUtils.AnnotationMetadata metadata) {
        List<FilterAnnotationData> annotationData = metadata.statementData();
        List<StatementPlan> statements = new ArrayList<>(annotationData.size());
        List<FilterPlan> decoratedFilters = new ArrayList<>();
        for (FilterAnnotationData data : annotationData) {
            List<FilterPlan> filters = compileFilters(data.filters(), decoratedFilters, true);
            List<NestedStatementPlan> nestedStatements = new ArrayList<>(data.filterStatements().size());
            for (FilterAnnotationStatement statement : data.filterStatements()) {
                nestedStatements.add(new NestedStatementPlan(
                        compileFilters(statement.filters(), decoratedFilters, false),
                        statement.negate()
                ));
            }
            statements.add(new StatementPlan(data.logicOperator(), filters, nestedStatements, data.negate()));
        }
        return new CompiledAnnotationPlan(statements, decoratedFilters, metadata.requestFilters());
    }

    private static List<FilterPlan> compileFilters(List<Filter> filters, List<FilterPlan> decoratedFilters,
                                                   boolean collectDecorated) {
        List<FilterPlan> compiledFilters = new ArrayList<>(filters.size());
        for (Filter filter : filters) {
            FilterPlan compiledFilter = new FilterPlan(filter);
            if (Decorated.class.equals(compiledFilter.operation())) {
                if (collectDecorated) {
                    decoratedFilters.add(compiledFilter);
                }
            } else {
                compiledFilters.add(compiledFilter);
            }
        }
        return List.copyOf(compiledFilters);
    }

    List<StatementPlan> statements() {
        return statements;
    }

    List<FilterPlan> decoratedFilters() {
        return decoratedFilters;
    }

    List<FilterRequestData> requestFilters() {
        return requestFilters;
    }

    record StatementPlan(LogicOperator logicOperator, List<FilterPlan> filters,
                         List<NestedStatementPlan> nestedStatements, String negate) {

        StatementPlan {
            filters = List.copyOf(filters);
            nestedStatements = List.copyOf(nestedStatements);
        }
    }

    record NestedStatementPlan(List<FilterPlan> filters, String negate) {

        NestedStatementPlan {
            filters = List.copyOf(filters);
        }
    }

    static final class FilterPlan {

        private final String[] path;
        private final String[] parameters;
        private final Class<?> targetType;
        @SuppressWarnings("rawtypes")
        private final Class<? extends FilterOperation> operation;
        private final String negate;
        private final Object[] defaultValues;
        private final Object[] constantValues;
        private final boolean required;
        private final List<Class<? extends FilterModifier>> modifiers;
        private final String description;
        private final int invalidParameterIndex;

        private FilterPlan(Filter filter) {
            this.path = filter.path();
            this.parameters = filter.parameters();
            this.targetType = filter.targetType();
            this.operation = filter.operation();
            this.negate = filter.negate();
            this.defaultValues = filter.defaultValues();
            this.constantValues = filter.constantValues();
            this.required = filter.required();
            this.modifiers = List.of(filter.modifiers());
            this.description = filter.description();
            this.invalidParameterIndex = findInvalidParameterIndex();
        }

        private int findInvalidParameterIndex() {
            for (int i = 0; i < parameters.length; i++) {
                if (parameters[i] == null || parameters[i].isEmpty()) {
                    return i;
                }
            }
            return -1;
        }

        String[] path() {
            return path;
        }

        String[] parameters() {
            return parameters;
        }

        Class<?> targetType() {
            return targetType;
        }

        @SuppressWarnings("rawtypes")
        Class<? extends FilterOperation> operation() {
            return operation;
        }

        String negate() {
            return negate;
        }

        Object[] defaultValues() {
            return defaultValues;
        }

        Object[] constantValues() {
            return constantValues;
        }

        int invalidParameterIndex() {
            return invalidParameterIndex;
        }

        boolean required() {
            return required;
        }

        List<Class<? extends FilterModifier>> modifiers() {
            return modifiers;
        }

        String description() {
            return description;
        }
    }
}
