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
import com.runestone.dynafilter.core.generator.ValueExpressionResolver;
import com.runestone.dynafilter.core.generator.annotation.AnnotationStatementGenerator;
import com.runestone.dynafilter.core.resolver.DynamicFilterResolver;
import com.runestone.dynafilter.modules.jpa.operation.SpecificationFilterOperationService;
import com.runestone.dynafilter.modules.jpa.resolver.SpecificationDynamicFilterResolver;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringValueResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

class SpecificationDynamicFilterWebMvcConfigurer implements WebMvcConfigurer {

    private final ApplicationContext applicationContext;
    private final StringValueResolver stringValueResolver;
    private final ValueExpressionResolver<String> valueExpressionResolver;

    private final DynamicFilterResolver<Specification<?>> dynamicFilterResolver;

    public SpecificationDynamicFilterWebMvcConfigurer(ApplicationContext applicationContext, StringValueResolver stringValueResolver,
                                                      ValueExpressionResolver<String> valueExpressionResolver, DynamicFilterResolver<Specification<?>> dynamicFilterResolver) {
        this.applicationContext = applicationContext;
        this.stringValueResolver = stringValueResolver;
        this.valueExpressionResolver = valueExpressionResolver;
        this.dynamicFilterResolver = dynamicFilterResolver;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        AnnotationStatementGenerator generator = new AnnotationStatementGenerator(getValueExpressionResolver());
        FilterDecoratorSpringFactory filterDecoratorFactory = new FilterDecoratorSpringFactory((GenericApplicationContext) applicationContext);
        resolvers.add(new SpecificationDynamicFilterArgumentResolver(generator, dynamicFilterResolver, filterDecoratorFactory));
    }

    protected ValueExpressionResolver<String> getValueExpressionResolver() {
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
