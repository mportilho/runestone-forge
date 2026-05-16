package com.runestone.dynafilter.modules.jpa.resolver;

import com.runestone.dynafilter.core.DynamicFilterResolver;
import com.runestone.dynafilter.core.decorator.FilterDecorator;
import com.runestone.dynafilter.core.generator.StatementWrapper;
import com.runestone.dynafilter.modules.jpa.operation.SpecificationFilterOperationService;
import org.springframework.data.jpa.domain.Specification;

import java.util.Objects;

public final class SpecificationDynamicFilterResolver implements DynamicFilterResolver<Specification<?>> {

    private final SpecificationStatementAnalyser analyser;

    public SpecificationDynamicFilterResolver() {
        this(new SpecificationStatementAnalyser(new SpecificationFilterOperationService()));
    }

    public SpecificationDynamicFilterResolver(SpecificationStatementAnalyser analyser) {
        this.analyser = Objects.requireNonNull(analyser, "analyser must not be null");
    }

    @Override
    public Specification<?> createFilter(StatementWrapper statementWrapper, FilterDecorator<Specification<?>> decorator) {
        Objects.requireNonNull(statementWrapper, "statementWrapper must not be null");
        Specification<?> specification = analyser.analyse(statementWrapper.statement());
        return decorator == null ? specification : decorator.decorate(specification, statementWrapper);
    }
}
