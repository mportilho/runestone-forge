package com.runestone.dynafilter.modules.jpa.spring;

import com.runestone.converters.DataConversionService;
import com.runestone.dynafilter.core.generator.ValueExpressionResolver;
import com.runestone.dynafilter.core.generator.annotation.AnnotationStatementGenerator;
import com.runestone.dynafilter.modules.jpa.operation.SpecificationFilterOperationService;
import com.runestone.dynafilter.modules.jpa.resolver.SpecificationDynamicFilterResolver;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.util.StringValueResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

class SpecificationDynaFilterWebMvcConfigurer implements WebMvcConfigurer {

    private final ApplicationContext applicationContext;
    private final DataConversionService dataConversionService;
    private final StringValueResolver stringValueResolver;
    private final ValueExpressionResolver<String> valueExpressionResolver;

    public SpecificationDynaFilterWebMvcConfigurer(ApplicationContext applicationContext, DataConversionService dataConversionService,
                                                   StringValueResolver stringValueResolver, ValueExpressionResolver<String> valueExpressionResolver) {
        this.applicationContext = applicationContext;
        this.dataConversionService = dataConversionService;
        this.stringValueResolver = stringValueResolver;
        this.valueExpressionResolver = valueExpressionResolver;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        AnnotationStatementGenerator generator = new AnnotationStatementGenerator(getValueExpressionResolver());
        SpecificationFilterOperationService service = new SpecificationFilterOperationService(dataConversionService);
        SpecificationDynamicFilterResolver resolver = new SpecificationDynamicFilterResolver(service);
        FilterDecoratorSpringFactory filterDecoratorFactory = new FilterDecoratorSpringFactory((GenericApplicationContext) applicationContext);
        resolvers.add(new SpecificationDynaFilterArgumentResolver(generator, resolver, filterDecoratorFactory));
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
