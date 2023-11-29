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

public class ServletDynaFilterAutoConfiguration implements EmbeddedValueResolverAware, ApplicationContextAware {

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
        return new SpecificationDynaFilterWebMvcConfigurer(applicationContext, dataConversionService, stringValueResolver, valueExpressionResolver);
    }

}
