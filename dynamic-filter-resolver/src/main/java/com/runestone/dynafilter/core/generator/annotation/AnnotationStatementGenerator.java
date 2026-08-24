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

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.runestone.dynafilter.core.generator.DefaultStatementGenerator;
import com.runestone.dynafilter.core.generator.StatementWrapper;
import com.runestone.dynafilter.core.generator.ValueExpressionResolver;
import com.runestone.dynafilter.core.exceptions.DynamicFilterConfigurationException;
import com.runestone.dynafilter.core.model.FilterData;
import com.runestone.dynafilter.core.model.statement.*;
import com.runestone.dynafilter.core.operation.ComparisonOperation;
import com.runestone.dynafilter.core.operation.types.Dynamic;
import com.runestone.dynafilter.core.transformer.FilterValueTransformerResolver;

import java.util.*;

public class AnnotationStatementGenerator extends DefaultStatementGenerator<AnnotationStatementInput> {

    private static final FilterData[] EMPTY_FILTER_DATA = {};
    private static final NoOpStatement NO_OP_STATEMENT = new NoOpStatement();
    private static final FilterValueTransformerResolver NO_TRANSFORMERS = transformerType -> {
        throw new DynamicFilterConfigurationException(
                "Filter value transformer '%s' is not registered".formatted(transformerType.getCanonicalName())
        );
    };
    private final Cache<AnnotationStatementInput, CompiledAnnotationPlan> planCache;
    private final FilterValueTransformerResolver transformerResolver;

    public AnnotationStatementGenerator() {
        this(null, NO_TRANSFORMERS, TypeAnnotationUtils.cacheMaxSize());
    }

    public AnnotationStatementGenerator(ValueExpressionResolver<?> valueExpressionResolver) {
        this(valueExpressionResolver, NO_TRANSFORMERS, TypeAnnotationUtils.cacheMaxSize());
    }

    AnnotationStatementGenerator(ValueExpressionResolver<?> valueExpressionResolver, int planCacheMaxSize) {
        this(valueExpressionResolver, NO_TRANSFORMERS, planCacheMaxSize);
    }

    public AnnotationStatementGenerator(ValueExpressionResolver<?> valueExpressionResolver,
                                        FilterValueTransformerResolver transformerResolver) {
        this(valueExpressionResolver, Objects.requireNonNull(transformerResolver, "transformerResolver is required"),
                TypeAnnotationUtils.cacheMaxSize());
    }

    AnnotationStatementGenerator(ValueExpressionResolver<?> valueExpressionResolver,
                                 FilterValueTransformerResolver transformerResolver, int planCacheMaxSize) {
        super(valueExpressionResolver);
        Objects.requireNonNull(transformerResolver, "transformerResolver is required");
        if (planCacheMaxSize <= 0) {
            throw new IllegalArgumentException("planCacheMaxSize must be greater than zero");
        }
        this.planCache = Caffeine.newBuilder()
                .maximumSize(planCacheMaxSize)
                .executor(Runnable::run)
                .build();
        this.transformerResolver = transformerResolver;
    }

    @Override
    public StatementWrapper generateStatements(AnnotationStatementInput filterInputs, Map<String, Object> filterParameters) {
        Objects.requireNonNull(filterInputs, "annotationStatementInput is required");
        Map<String, Object> parametersMap = filterParameters != null ? filterParameters : Collections.emptyMap();
        List<AbstractStatement> statementList = new ArrayList<>();

        CompiledAnnotationPlan plan = planCache.get(filterInputs, this::compilePlan);
        for (CompiledAnnotationPlan.StatementPlan data : plan.statements()) {
            AbstractStatement statements = createStatements(data, parametersMap);
            if (statements != null) {
                statementList.add(statements);
            }
        }

        Map<String, FilterData> decoratedFilters = createDecoratedFiltersData(plan.decoratedFilters(), parametersMap);

        if (statementList.isEmpty()) {
            return new StatementWrapper(NO_OP_STATEMENT, decoratedFilters, plan.requestFilters());
        } else if (statementList.size() == 1) {
            return new StatementWrapper(statementList.getFirst(), decoratedFilters, plan.requestFilters());
        } else {
            AbstractStatement finalStatement = statementList.getFirst();
            for (int i = 1; i < statementList.size(); i++) {
                finalStatement = new CompoundStatement(finalStatement, statementList.get(i), LogicOperator.CONJUNCTION);
            }
            return new StatementWrapper(finalStatement, decoratedFilters, plan.requestFilters());
        }
    }

    /**
     * Compiles and caches the immutable plan for the supplied annotation input.
     */
    public void warmup(AnnotationStatementInput filterInputs) {
        Objects.requireNonNull(filterInputs, "annotationStatementInput is required");
        planCache.get(filterInputs, this::compilePlan);
    }

    /**
     * Invalidates the plans owned by this generator instance.
     */
    public void clearCache() {
        planCache.invalidateAll();
    }

    private CompiledAnnotationPlan compilePlan(AnnotationStatementInput input) {
        return CompiledAnnotationPlan.compile(TypeAnnotationUtils.findMetadata(input), transformerResolver);
    }

    private Map<String, FilterData> createDecoratedFiltersData(List<CompiledAnnotationPlan.FilterPlan> filters,
                                                                Map<String, Object> parametersMap) {
        Map<String, FilterData> decoratedFilters = new HashMap<>();
        for (CompiledAnnotationPlan.FilterPlan filter : filters) {
            Object[] values = computePrevalidatedValues(filter.parameters(), filter.defaultValues(), filter.constantValues(),
                    parametersMap, filter.invalidParameterIndex());
            if (values == null) {
                continue;
            }
            FilterData filterData = createTransformedFilterData(filter, values);
            for (String path : filter.path()) {
                decoratedFilters.put(path, filterData);
            }
        }
        return Collections.unmodifiableMap(decoratedFilters);
    }

    /**
     *
     */
    private AbstractStatement createStatements(CompiledAnnotationPlan.StatementPlan data, Map<String, Object> userParameters) {
        boolean negate = computeNegatingParameter(data.negate());
        FilterData[] clauses = processFilterAnnotations(data.filters(), userParameters);
        AbstractStatement statement = createStatements(clauses, data.logicOperator());
        AbstractStatement statementFromStatements = createStatementFromFilterStatements(data.nestedStatements(), data.logicOperator(), userParameters);

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

    private AbstractStatement createStatementFromFilterStatements(List<CompiledAnnotationPlan.NestedStatementPlan> statements,
                                                                   LogicOperator logicType, Map<String, Object> userParameters) {
        AbstractStatement resultStatement = null;
        for (CompiledAnnotationPlan.NestedStatementPlan filterStatement : statements) {
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
    private FilterData[] processFilterAnnotations(List<CompiledAnnotationPlan.FilterPlan> filters, Map<String, Object> userParameters) {
        if (filters == null || filters.isEmpty()) {
            return EMPTY_FILTER_DATA;
        }

        List<FilterData> filterParameters = new ArrayList<>(filters.size());
        for (CompiledAnnotationPlan.FilterPlan filter : filters) {
            Object[] values = computePrevalidatedValues(filter.parameters(), filter.defaultValues(), filter.constantValues(),
                    userParameters, filter.invalidParameterIndex());
            if (values == null || values.length == 0) {
                if (filter.required()) {
                    String pluralChar = filter.parameters().length > 1 ? "s" : "";
                    String parameters = String.join(", ", filter.parameters());
                    throw new IllegalArgumentException(String.format("Parameter%s '%s' required", pluralChar, parameters));
                }
                continue;
            }
            var filterData = createTransformedFilterData(filter, values);
            filterParameters.add(filterData);
        }
        return filterParameters.toArray(FilterData[]::new);
    }

    private FilterData createTransformedFilterData(CompiledAnnotationPlan.FilterPlan filter, Object[] values) {
        BoundFilterValueTransformerChain chain = filter.transformerChain();
        if (!Dynamic.class.equals(filter.operation()) || chain.isEmpty()) {
            chain.transformValues(values);
            return createFilterData(filter.path(), filter.parameters(), filter.targetType(), filter.operation(),
                    filter.negate(), values, filter.modifiers(), filter.description());
        }

        Object[] dynamicValues = dynamicValues(values);
        String operationValue = dynamicOperationValue(dynamicValues);
        ComparisonOperation comparisonOperation = resolveDynamicOperation(operationValue, filter.path());
        int payloadSize = dynamicValues.length - 1;
        if (ComparisonOperation.BT.equals(comparisonOperation) && payloadSize != 2) {
            throw new com.runestone.dynafilter.core.exceptions.StatementGenerationException(
                    "Between operation must have two values");
        }

        Object[] transformedPayload;
        if (ComparisonOperation.IN.equals(comparisonOperation) && payloadSize == 1
                && (dynamicValues[1] instanceof Object[] || dynamicValues[1] instanceof Collection<?>)) {
            transformedPayload = new Object[]{chain.transformDynamicValue(dynamicValues[1], comparisonOperation)};
        } else {
            transformedPayload = chain.transformDynamicPayload(dynamicValues, 1, comparisonOperation);
            if (ComparisonOperation.IN.equals(comparisonOperation)) {
                transformedPayload = new Object[]{transformedPayload};
            }
        }

        String[] parameters = ComparisonOperation.BT.equals(comparisonOperation)
                ? dynamicBetweenParameters(filter.parameters()) : filter.parameters();
        return new FilterData(filter.path(), parameters, filter.targetType(), comparisonOperation.getOperation(),
                operationValue.length() == 3, transformedPayload, filter.modifiers(), filter.description());
    }

    int planCacheSize() {
        planCache.cleanUp();
        return Math.toIntExact(planCache.estimatedSize());
    }
}
