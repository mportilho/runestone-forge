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
