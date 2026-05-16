package com.runestone.dynafilter.modules.jpa.spring;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.runestone.dynafilter.core.annotation.FilterDecorators;
import com.runestone.dynafilter.core.decorator.CompositeFilterDecorator;
import com.runestone.dynafilter.core.decorator.FilterDecorator;
import com.runestone.dynafilter.core.decorator.FilterDecoratorFactory;
import com.runestone.dynafilter.core.generator.annotation.AnnotationStatementInput;
import com.runestone.dynafilter.modules.jpa.resolver.Fetches;
import com.runestone.dynafilter.modules.jpa.resolver.Fetching;
import com.runestone.dynafilter.modules.jpa.resolver.FetchingFilterDecorator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.data.jpa.domain.Specification;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class SpringFilterDecoratorFactory implements FilterDecoratorFactory {

    private final ApplicationContext applicationContext;
    private final Cache<AnnotationStatementInput, FilterDecorator<Specification<?>>> decoratorCache = Caffeine.newBuilder()
            .maximumSize(1024L)
            .build();

    public SpringFilterDecoratorFactory(ApplicationContext applicationContext) {
        this.applicationContext = Objects.requireNonNull(applicationContext, "applicationContext must not be null");
    }

    @Override
    public FilterDecorator<Specification<?>> createDecorator(AnnotationStatementInput input) {
        return createFilterDecorators(input);
    }

    public FilterDecorator<Specification<?>> createFilterDecorators(AnnotationStatementInput input) {
        Objects.requireNonNull(input, "input must not be null");
        return decoratorCache.get(input, this::buildDecorator);
    }

    private FilterDecorator<Specification<?>> buildDecorator(AnnotationStatementInput input) {
        List<FilterDecorator<Specification<?>>> decorators = new ArrayList<>();
        List<Fetching> fetches = new ArrayList<>();
        for (Annotation annotation : input.annotations()) {
            collect(annotation, decorators, fetches);
        }
        collectType(input.type(), decorators, fetches);
        if (!fetches.isEmpty()) {
            decorators.add(new FetchingFilterDecorator(fetches));
        }
        return new CompositeFilterDecorator<>(decorators);
    }

    private void collectType(Class<?> type, List<FilterDecorator<Specification<?>>> decorators, List<Fetching> fetches) {
        if (type == null || type.getPackageName().startsWith("java.")) {
            return;
        }
        for (Annotation annotation : type.getAnnotations()) {
            collect(annotation, decorators, fetches);
        }
        for (Class<?> interfaceType : type.getInterfaces()) {
            collectType(interfaceType, decorators, fetches);
        }
        collectType(type.getSuperclass(), decorators, fetches);
    }

    @SuppressWarnings("unchecked")
    private void collect(Annotation annotation, List<FilterDecorator<Specification<?>>> decorators, List<Fetching> fetches) {
        if (annotation instanceof Fetching fetching) {
            fetches.add(fetching);
        } else if (annotation instanceof Fetches repeatableFetches) {
            fetches.addAll(List.of(repeatableFetches.value()));
        } else if (annotation instanceof FilterDecorators filterDecorators) {
            for (Class<? extends FilterDecorator<?>> decoratorClass : filterDecorators.value()) {
                decorators.add((FilterDecorator<Specification<?>>) decorator(decoratorClass));
            }
        }
        for (Annotation metaAnnotation : annotation.annotationType().getAnnotations()) {
            if (!metaAnnotation.annotationType().getPackageName().startsWith("java.lang.annotation")) {
                collect(metaAnnotation, decorators, fetches);
            }
        }
    }

    private FilterDecorator<?> decorator(Class<? extends FilterDecorator<?>> decoratorClass) {
        String[] beanNames = applicationContext.getBeanNamesForType(decoratorClass);
        if (beanNames.length > 0) {
            return applicationContext.getBean(beanNames[0], decoratorClass);
        }
        if (applicationContext instanceof GenericApplicationContext genericApplicationContext) {
            genericApplicationContext.registerBean(decoratorClass);
            return genericApplicationContext.getBean(decoratorClass);
        }
        return applicationContext.getAutowireCapableBeanFactory().createBean(decoratorClass);
    }
}
