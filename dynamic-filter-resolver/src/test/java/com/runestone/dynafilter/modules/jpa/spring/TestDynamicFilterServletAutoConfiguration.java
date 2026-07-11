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

package com.runestone.dynafilter.modules.jpa.spring;

import com.runestone.converters.RuntimeDataConversionService;
import com.runestone.converters.impl.runtime.DefaultRuntimeDataConversionService;
import com.runestone.dynafilter.core.generator.StatementWrapper;
import com.runestone.dynafilter.core.model.FilterData;
import com.runestone.dynafilter.core.model.statement.LogicalStatement;
import com.runestone.dynafilter.core.operation.FilterOperation;
import com.runestone.dynafilter.core.operation.FilterOperationMetadata;
import com.runestone.dynafilter.core.operation.FilterOperationService;
import com.runestone.dynafilter.core.operation.types.Like;
import com.runestone.dynafilter.core.resolver.DynamicFilterResolver;
import com.runestone.dynafilter.modules.jpa.api.JpaFilterOperationContributor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringValueResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.stream.Stream;

public class TestDynamicFilterServletAutoConfiguration {

    @Test
    public void testServletConfigurationCreation() {
        DynamicFilterServletAutoConfiguration servletConfig = new DynamicFilterServletAutoConfiguration();
        servletConfig.setApplicationContext(Mockito.mock(ApplicationContext.class));
        servletConfig.setEmbeddedValueResolver(Mockito.mock(StringValueResolver.class));
        DynamicFilterResolver<Specification<?>> dynamicFilterResolver = Mockito.mock(DynamicFilterResolver.class);
        WebMvcConfigurer webMvcConfigurer = servletConfig.webMvcConfigurer(dynamicFilterResolver, null);
        Assertions.assertThat(webMvcConfigurer).isNotNull();
    }

    @Test
    @DisplayName("Auto-configuration exposes the default runtime conversion service")
    public void testRuntimeDataConversionServiceCreation() {
        DynamicFilterServletAutoConfiguration servletConfig = new DynamicFilterServletAutoConfiguration();

        RuntimeDataConversionService conversionService = servletConfig.runtimeDataConversionService();

        Assertions.assertThat(conversionService).isInstanceOf(DefaultRuntimeDataConversionService.class);
    }

    @Test
    @DisplayName("Auto-configuration creates a resolver backed by the default JPA operation service")
    public void testDynamicFilterResolverCreation() {
        DynamicFilterServletAutoConfiguration servletConfig = new DynamicFilterServletAutoConfiguration();

        FilterOperationService<Specification<?>> operationService = servletConfig.specificationFilterOperationService(
                DefaultRuntimeDataConversionService.standard(),
                emptyContributors()
        );
        DynamicFilterResolver<Specification<?>> resolver = servletConfig.dynamicFilterResolver(operationService);
        FilterData filterData = FilterData.of("name", new String[]{"name"}, String.class, Like.class, new Object[]{"John"});
        StatementWrapper statementWrapper = new StatementWrapper(new LogicalStatement(filterData), null, null);

        Specification<?> specification = resolver.createFilter(statementWrapper, null);

        Assertions.assertThat(specification).isNotNull();
    }

    @Test
    @DisplayName("Auto-configuration creates an operation service with application contributors")
    public void testSpecificationFilterOperationServiceCreationWithContributors() {
        DynamicFilterServletAutoConfiguration servletConfig = new DynamicFilterServletAutoConfiguration();
        Specification<?> customSpecification = (root, query, builder) -> null;
        JpaFilterOperationContributor contributor = registry ->
                registry.register(CustomOperation.class, FilterOperationMetadata.targetField(), context -> customSpecification);

        FilterOperationService<Specification<?>> operationService = servletConfig.specificationFilterOperationService(
                DefaultRuntimeDataConversionService.standard(),
                contributors(contributor)
        );
        FilterData filterData = FilterData.of("name", new String[]{"name"}, String.class, CustomOperation.class, new Object[]{"John"});

        Specification<?> specification = operationService.createFilter(filterData);

        Assertions.assertThat(specification).isSameAs(customSpecification);
        Assertions.assertThat(operationService.supports(CustomOperation.class)).isTrue();
    }

    private ObjectProvider<JpaFilterOperationContributor> emptyContributors() {
        return contributors();
    }

    @SuppressWarnings("unchecked")
    private ObjectProvider<JpaFilterOperationContributor> contributors(JpaFilterOperationContributor... contributors) {
        ObjectProvider<JpaFilterOperationContributor> provider = Mockito.mock(ObjectProvider.class);
        Mockito.when(provider.orderedStream()).thenReturn(Stream.of(contributors));
        return provider;
    }

    private interface CustomOperation<T> extends FilterOperation<T> {
    }

}
