package com.runestone.dynafilter.modules.jpa.resolver;

import com.runestone.dynafilter.core.generator.StatementWrapper;
import com.runestone.dynafilter.core.operation.FilterOperationService;
import com.runestone.dynafilter.core.resolver.DynamicFilterResolver;
import com.runestone.dynafilter.core.resolver.FilterDecorator;
import org.springframework.data.jpa.domain.Specification;

import java.util.Objects;

public class SpecificationDynamicFilterResolver implements DynamicFilterResolver<Specification<?>> {

    private final SpecificationStatementAnalyser statementAnalyser;

    public SpecificationDynamicFilterResolver(FilterOperationService<Specification<?>> filterOperationService) {
        this.statementAnalyser = new SpecificationStatementAnalyser(filterOperationService);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R extends Specification<?>> R createFilter(StatementWrapper statementWrapper, FilterDecorator<Specification<?>> decorator) {
        Objects.requireNonNull(statementWrapper, "Statement wrapper cannot be null");
        Specification<?> specification = statementWrapper.statement().acceptAnalyser(statementAnalyser);
        return (R) (decorator != null ? decorator.decorate(specification, statementWrapper) : specification);
    }

}
