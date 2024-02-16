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
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.util.StringValueResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;

import java.util.List;

public class TestSpecificationDynamicFilterWebMvcConfigurer {

    @Test
    @SuppressWarnings({"unchecked"})
    public void test() {
        SpecificationDynamicFilterWebMvcConfigurer configurer = new SpecificationDynamicFilterWebMvcConfigurer(
                Mockito.mock(GenericApplicationContext.class), Mockito.mock(DataConversionService.class), null, null);
        List<HandlerMethodArgumentResolver> resolvers = Mockito.mock(List.class);
        configurer.addArgumentResolvers(resolvers);
        Mockito.verify(resolvers, Mockito.times(1)).add(Mockito.any(SpecificationDynamicFilterArgumentResolver.class));
    }

    @Test
    public void testCreatingNullValueExpressionResolver() {
        SpecificationDynamicFilterWebMvcConfigurer configurer = new SpecificationDynamicFilterWebMvcConfigurer(
                Mockito.mock(GenericApplicationContext.class), Mockito.mock(DataConversionService.class), null, null);
        ValueExpressionResolver<String> valueExpressionResolver = configurer.getValueExpressionResolver();
        Assertions.assertThat(valueExpressionResolver).isNull();
    }

    @Test
    public void testCreatingWithValueExpressionResolver() {
        SpecificationDynamicFilterWebMvcConfigurer configurer = new SpecificationDynamicFilterWebMvcConfigurer(
                Mockito.mock(GenericApplicationContext.class), Mockito.mock(DataConversionService.class), null, value -> value);
        ValueExpressionResolver<String> valueExpressionResolver = configurer.getValueExpressionResolver();
        Assertions.assertThat(valueExpressionResolver).isInstanceOf(ValueExpressionResolver.class);
    }

    @Test
    public void testCreatingWithStringValueResolver() {
        SpecificationDynamicFilterWebMvcConfigurer configurer = new SpecificationDynamicFilterWebMvcConfigurer(
                Mockito.mock(GenericApplicationContext.class), Mockito.mock(DataConversionService.class), Mockito.mock(StringValueResolver.class), null);
        ValueExpressionResolver<String> valueExpressionResolver = configurer.getValueExpressionResolver();
        Assertions.assertThat(valueExpressionResolver).isInstanceOf(ValueExpressionResolver.class);
    }

    @Test
    @SuppressWarnings({"unchecked"})
    public void testCreatingWithBothValueExpressionResolverAndStringValueResolver() {

        StringValueResolver stringValueResolver = Mockito.mock(StringValueResolver.class);
        Mockito.when(stringValueResolver.resolveStringValue(Mockito.eq("test01"))).thenReturn("stringValueResolver");
        Mockito.when(stringValueResolver.resolveStringValue(Mockito.eq("test02"))).thenReturn(null);
        Mockito.when(stringValueResolver.resolveStringValue(Mockito.eq("test-null-01"))).thenReturn(null);

        ValueExpressionResolver<String> valueExpressionResolver = Mockito.mock(ValueExpressionResolver.class);
        Mockito.when(valueExpressionResolver.resolveValue(Mockito.eq("test02"))).thenReturn("valueExpressionResolver");
        Mockito.when(valueExpressionResolver.resolveValue(Mockito.eq("test-null-02"))).thenReturn(null);

        SpecificationDynamicFilterWebMvcConfigurer configurer = new SpecificationDynamicFilterWebMvcConfigurer(
                Mockito.mock(GenericApplicationContext.class), Mockito.mock(DataConversionService.class), stringValueResolver, valueExpressionResolver);
        ValueExpressionResolver<String> resolver = configurer.getValueExpressionResolver();
        Assertions.assertThat(resolver).isInstanceOf(ValueExpressionResolver.class);

        Assertions.assertThat(resolver.resolveValue("test01")).isEqualTo("stringValueResolver");
        Assertions.assertThat(resolver.resolveValue("test02")).isEqualTo("valueExpressionResolver");
        Assertions.assertThat(resolver.resolveValue("test-null-01")).isNull();
        Assertions.assertThat(resolver.resolveValue("test-null-02")).isNull();
    }

}
