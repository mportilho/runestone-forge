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

import com.runestone.converters.DataConversionService;
import com.runestone.converters.impl.DefaultDataConversionService;
import com.runestone.dynafilter.core.generator.ValueExpressionResolver;
import com.runestone.dynafilter.core.generator.annotation.AnnotationStatementGenerator;
import com.runestone.dynafilter.core.operation.FilterOperationService;
import com.runestone.dynafilter.core.resolver.DynamicFilterResolver;
import com.runestone.dynafilter.core.transformer.FilterValueTransformerResolver;
import com.runestone.dynafilter.modules.jpa.api.JpaFilterOperationContributor;
import com.runestone.dynafilter.modules.jpa.api.JpaFilterOperationService;
import com.runestone.dynafilter.modules.jpa.resolver.SpecificationDynamicFilterResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringValueResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

public class DynamicFilterServletAutoConfiguration implements EmbeddedValueResolverAware, ApplicationContextAware {

    private ApplicationContext applicationContext;
    private StringValueResolver stringValueResolver;

    @Override
    public void setEmbeddedValueResolver(StringValueResolver resolver) {
        this.stringValueResolver = resolver;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Bean
    @ConditionalOnMissingBean
    public DataConversionService dataConversionService() {
        return new DefaultDataConversionService();
    }

    @Bean
    @ConditionalOnMissingBean
    public FilterOperationService<Specification<?>> specificationFilterOperationService(
            DataConversionService dataConversionService,
            ObjectProvider<JpaFilterOperationContributor> contributors
    ) {
        return new JpaFilterOperationService(dataConversionService, contributors.orderedStream().toList());
    }

    @Bean
    @ConditionalOnMissingBean
    public DynamicFilterResolver<Specification<?>> dynamicFilterResolver(FilterOperationService<Specification<?>> filterOperationService) {
        return new SpecificationDynamicFilterResolver(filterOperationService);
    }

    @Bean
    @ConditionalOnMissingBean
    public FilterValueTransformerResolver filterValueTransformerResolver(ConfigurableListableBeanFactory beanFactory) {
        return new SpringFilterValueTransformerResolver(beanFactory);
    }

    @Bean(destroyMethod = "clearCache")
    @ConditionalOnMissingBean
    public AnnotationStatementGenerator annotationStatementGenerator(
            FilterValueTransformerResolver transformerResolver,
            @Autowired(required = false) ValueExpressionResolver<String> valueExpressionResolver) {
        return new AnnotationStatementGenerator(combineValueExpressionResolvers(valueExpressionResolver), transformerResolver);
    }

    @Bean
    public WebMvcConfigurer webMvcConfigurer(DynamicFilterResolver<Specification<?>> dynamicFilterResolver,
                                             AnnotationStatementGenerator statementGenerator) {
        return new SpecificationDynamicFilterWebMvcConfigurer(applicationContext, statementGenerator, dynamicFilterResolver);
    }

    ValueExpressionResolver<String> combineValueExpressionResolvers(ValueExpressionResolver<String> valueExpressionResolver) {
        if (valueExpressionResolver == null && stringValueResolver == null) {
            return null;
        } else if (valueExpressionResolver == null) {
            return stringValueResolver::resolveStringValue;
        } else if (stringValueResolver == null) {
            return valueExpressionResolver;
        }
        return key -> {
            String response = valueExpressionResolver.resolveValue(key);
            return response != null ? response : stringValueResolver.resolveStringValue(key);
        };
    }

}
