package com.runestone.dynafilter.modules.jpa.spring;

import com.runestone.converters.DataConversionService;
import com.runestone.converters.impl.DefaultDataConversionService;
import com.runestone.dynafilter.core.DynamicFilterResolver;
import com.runestone.dynafilter.core.decorator.FilterDecoratorFactory;
import com.runestone.dynafilter.core.generator.annotation.AnnotationStatementGenerator;
import com.runestone.dynafilter.modules.jpa.operation.SpecificationFilterOperationService;
import com.runestone.dynafilter.modules.jpa.repository.DynamicFilterJpaRepositoryBeanPostProcessor;
import com.runestone.dynafilter.modules.jpa.resolver.SpecificationDynamicFilterResolver;
import com.runestone.dynafilter.modules.jpa.resolver.SpecificationStatementAnalyser;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.domain.Specification;

@AutoConfiguration
public class DynamicFilterServletAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public DataConversionService dataConversionService() {
        return new DefaultDataConversionService(false);
    }

    @Bean
    @ConditionalOnMissingBean
    public AnnotationStatementGenerator annotationStatementGenerator() {
        return new AnnotationStatementGenerator();
    }

    @Bean
    @ConditionalOnMissingBean
    public SpecificationFilterOperationService specificationFilterOperationService(DataConversionService conversionService) {
        return new SpecificationFilterOperationService(conversionService);
    }

    @Bean
    @ConditionalOnMissingBean
    public SpecificationStatementAnalyser specificationStatementAnalyser(SpecificationFilterOperationService operationService) {
        return new SpecificationStatementAnalyser(operationService);
    }

    @Bean
    @ConditionalOnMissingBean
    public SpecificationDynamicFilterResolver specificationDynamicFilterResolver(SpecificationStatementAnalyser analyser) {
        return new SpecificationDynamicFilterResolver(analyser);
    }

    @Bean
    @ConditionalOnMissingBean
    public FilterDecoratorFactory filterDecoratorFactory(ApplicationContext applicationContext) {
        return new SpringFilterDecoratorFactory(applicationContext);
    }

    @Bean
    @ConditionalOnMissingBean
    public SpecificationDynamicFilterArgumentResolver specificationDynamicFilterArgumentResolver(
            AnnotationStatementGenerator statementGenerator,
            SpecificationDynamicFilterResolver dynamicFilterResolver,
            FilterDecoratorFactory filterDecoratorFactory
    ) {
        return new SpecificationDynamicFilterArgumentResolver(statementGenerator, dynamicFilterResolver, filterDecoratorFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    public SpecificationDynamicFilterWebMvcConfigurer specificationDynamicFilterWebMvcConfigurer(
            SpecificationDynamicFilterArgumentResolver argumentResolver
    ) {
        return new SpecificationDynamicFilterWebMvcConfigurer(argumentResolver);
    }

    @Bean
    @ConditionalOnMissingBean
    public DynamicFilterJpaRepositoryBeanPostProcessor dynamicFilterJpaRepositoryBeanPostProcessor(
            DynamicFilterResolver<Specification<?>> dynamicFilterResolver
    ) {
        return new DynamicFilterJpaRepositoryBeanPostProcessor(dynamicFilterResolver);
    }

    @Bean
    @ConditionalOnMissingBean
    public FilterConfigurationAnalyserBeanPostProcessor filterConfigurationAnalyserBeanPostProcessor() {
        return new FilterConfigurationAnalyserBeanPostProcessor();
    }
}
