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

public class TestSpecificationDynaFilterWebMvcConfigurer {

    @Test
    @SuppressWarnings({"unchecked"})
    public void test() {
        SpecificationDynaFilterWebMvcConfigurer configurer = new SpecificationDynaFilterWebMvcConfigurer(
                Mockito.mock(GenericApplicationContext.class), Mockito.mock(DataConversionService.class), null, null);
        List<HandlerMethodArgumentResolver> resolvers = Mockito.mock(List.class);
        configurer.addArgumentResolvers(resolvers);
        Mockito.verify(resolvers, Mockito.times(1)).add(Mockito.any(SpecificationDynaFilterArgumentResolver.class));
    }

    @Test
    public void testCreatingNullValueExpressionResolver() {
        SpecificationDynaFilterWebMvcConfigurer configurer = new SpecificationDynaFilterWebMvcConfigurer(
                Mockito.mock(GenericApplicationContext.class), Mockito.mock(DataConversionService.class), null, null);
        ValueExpressionResolver<String> valueExpressionResolver = configurer.getValueExpressionResolver();
        Assertions.assertThat(valueExpressionResolver).isNull();
    }

    @Test
    public void testCreatingWithValueExpressionResolver() {
        SpecificationDynaFilterWebMvcConfigurer configurer = new SpecificationDynaFilterWebMvcConfigurer(
                Mockito.mock(GenericApplicationContext.class), Mockito.mock(DataConversionService.class), null, value -> value);
        ValueExpressionResolver<String> valueExpressionResolver = configurer.getValueExpressionResolver();
        Assertions.assertThat(valueExpressionResolver).isInstanceOf(ValueExpressionResolver.class);
    }

    @Test
    public void testCreatingWithStringValueResolver() {
        SpecificationDynaFilterWebMvcConfigurer configurer = new SpecificationDynaFilterWebMvcConfigurer(
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

        SpecificationDynaFilterWebMvcConfigurer configurer = new SpecificationDynaFilterWebMvcConfigurer(
                Mockito.mock(GenericApplicationContext.class), Mockito.mock(DataConversionService.class), stringValueResolver, valueExpressionResolver);
        ValueExpressionResolver<String> resolver = configurer.getValueExpressionResolver();
        Assertions.assertThat(resolver).isInstanceOf(ValueExpressionResolver.class);

        Assertions.assertThat(resolver.resolveValue("test01")).isEqualTo("stringValueResolver");
        Assertions.assertThat(resolver.resolveValue("test02")).isEqualTo("valueExpressionResolver");
        Assertions.assertThat(resolver.resolveValue("test-null-01")).isNull();
        Assertions.assertThat(resolver.resolveValue("test-null-02")).isNull();
    }

}
