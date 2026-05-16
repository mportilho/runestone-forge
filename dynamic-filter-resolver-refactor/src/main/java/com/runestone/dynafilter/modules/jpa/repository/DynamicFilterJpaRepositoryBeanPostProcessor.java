package com.runestone.dynafilter.modules.jpa.repository;

import com.runestone.dynafilter.core.DynamicFilterResolver;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.data.jpa.domain.Specification;

import java.util.Objects;

public final class DynamicFilterJpaRepositoryBeanPostProcessor implements BeanPostProcessor {

    private final DynamicFilterResolver<Specification<?>> dynamicFilterResolver;

    public DynamicFilterJpaRepositoryBeanPostProcessor(DynamicFilterResolver<Specification<?>> dynamicFilterResolver) {
        this.dynamicFilterResolver = Objects.requireNonNull(dynamicFilterResolver, "dynamicFilterResolver must not be null");
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof DynamicFilterJpaRepositoryImpl<?, ?> repository) {
            repository.setDynamicFilterResolver(dynamicFilterResolver);
        }
        return bean;
    }
}
