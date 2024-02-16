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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringValueResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

public class ServletDynamicFilterAutoConfiguration implements EmbeddedValueResolverAware, ApplicationContextAware {

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
    public WebMvcConfigurer webMvcConfigurer(@Autowired(required = false) final DataConversionService dataConversionService,
                                             @Autowired(required = false) final ValueExpressionResolver<String> valueExpressionResolver) {
        return new SpecificationDynamicFilterWebMvcConfigurer(applicationContext, dataConversionService, stringValueResolver, valueExpressionResolver);
    }

}
