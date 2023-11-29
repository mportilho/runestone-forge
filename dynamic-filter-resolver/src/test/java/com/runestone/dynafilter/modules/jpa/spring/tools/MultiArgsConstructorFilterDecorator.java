package com.runestone.dynafilter.modules.jpa.spring.tools;

import com.runestone.dynafilter.core.generator.StatementWrapper;
import com.runestone.dynafilter.core.resolver.FilterDecorator;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;

import java.util.Objects;

public class MultiArgsConstructorFilterDecorator implements FilterDecorator<String> {

    private ApplicationContext applicationContext;
    private BeanFactory beanFactory;

    public MultiArgsConstructorFilterDecorator(ApplicationContext applicationContext, BeanFactory beanFactory) {
        this.applicationContext = Objects.requireNonNull(applicationContext);
        this.beanFactory = Objects.requireNonNull(beanFactory);
    }

    @Override
    public String decorate(String filter, StatementWrapper statementWrapper) {
        return filter + " - %s and %s".formatted(applicationContext.getClass().getSimpleName(), beanFactory.getClass().getSimpleName());
    }
}
