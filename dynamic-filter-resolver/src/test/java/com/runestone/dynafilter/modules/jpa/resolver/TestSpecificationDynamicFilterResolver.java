package com.runestone.dynafilter.modules.jpa.resolver;

import com.runestone.converters.impl.DefaultDataConversionService;
import com.runestone.dynafilter.core.generator.StatementWrapper;
import com.runestone.dynafilter.core.resolver.FilterDecorator;
import com.runestone.dynafilter.modules.jpa.operation.SpecificationFilterOperationService;
import com.runestone.dynafilter.modules.jpa.tools.MockStatementFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.jpa.domain.Specification;

import java.util.concurrent.atomic.AtomicInteger;

public class TestSpecificationDynamicFilterResolver {

    private SpecificationFilterOperationService filterOperationService;

    @BeforeEach
    public void setup() {
        filterOperationService = Mockito.spy(new SpecificationFilterOperationService(new DefaultDataConversionService()));
    }

    @Test
    public void testSpecResolverWithoutDecorator() {
        SpecificationDynamicFilterResolver resolver = new SpecificationDynamicFilterResolver(filterOperationService);
        var logicalStatementOnName = MockStatementFactory.createLogicalStatementOnName();
        Specification<?> specification = resolver.createFilter(new StatementWrapper(logicalStatementOnName, null), null);

        Assertions.assertThat(specification).isNotNull();
        Mockito.verify(filterOperationService, Mockito.times(1)).createFilter(Mockito.eq(logicalStatementOnName.getFilterData()));
    }

    @Test
    public void testSpecResolverWithDecorator() {
        AtomicInteger decoratorCallCount = new AtomicInteger(0);
        SpecificationDynamicFilterResolver resolver = new SpecificationDynamicFilterResolver(filterOperationService);
        var logicalStatementOnName = MockStatementFactory.createLogicalStatementOnName();
        FilterDecorator<Specification<?>> decorator = (spec, statement) -> {
            decoratorCallCount.incrementAndGet();
            return spec.and((root, query, builder) -> builder.equal(root.get("name"), "TestValue"));
        };

        Specification<?> specification = resolver.createFilter(new StatementWrapper(logicalStatementOnName, null), decorator);

        Assertions.assertThat(specification).isNotNull();
        Mockito.verify(filterOperationService, Mockito.times(1)).createFilter(Mockito.eq(logicalStatementOnName.getFilterData()));
        Assertions.assertThat(decoratorCallCount.get()).isEqualTo(1);
    }

}
