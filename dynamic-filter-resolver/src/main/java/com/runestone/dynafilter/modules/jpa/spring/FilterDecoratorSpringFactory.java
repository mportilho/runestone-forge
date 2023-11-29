package com.runestone.dynafilter.modules.jpa.spring;

import com.runestone.dynafilter.core.resolver.CompositeFilterDecorator;
import com.runestone.dynafilter.core.resolver.FilterDecorator;
import com.runestone.dynafilter.modules.jpa.resolver.Fetches;
import com.runestone.dynafilter.modules.jpa.resolver.Fetching;
import com.runestone.dynafilter.modules.jpa.resolver.FetchingFilterDecorator;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.data.jpa.domain.Specification;

import java.lang.annotation.Annotation;
import java.util.*;

public class FilterDecoratorSpringFactory {

    private final GenericApplicationContext applicationContext;

    public FilterDecoratorSpringFactory(GenericApplicationContext applicationContext) {
        this.applicationContext = Objects.requireNonNull(applicationContext, "applicationContext is required");
    }

    @SuppressWarnings({"unchecked"})
    public FilterDecorator<Specification<?>> createFilterDecorators(Annotation[] fetchingAnnotations,
                                                                    Class<? extends FilterDecorator<?>>[] decoratorClasses) {
        List<FilterDecorator<Specification<?>>> decoratorList = null;
        FetchingFilterDecorator fetchingDecorator = createFetchingDecorator(fetchingAnnotations);
        if (decoratorClasses != null) {
            decoratorList = new ArrayList<>(decoratorClasses.length + 1);
            for (Class<? extends FilterDecorator<?>> aClass : decoratorClasses) {
                Map<String, ? extends FilterDecorator<?>> beansOfType = applicationContext.getBeansOfType(aClass);
                if (beansOfType.isEmpty()) {
                    applicationContext.registerBean(aClass.getSimpleName(), aClass);
                    beansOfType = applicationContext.getBeansOfType(aClass);
                }
                for (FilterDecorator<?> filterDecorator : beansOfType.values()) {
                    decoratorList.add((FilterDecorator<Specification<?>>) filterDecorator);
                }
            }
            if (fetchingDecorator != null) {
                decoratorList.add(fetchingDecorator);
            }
        }
        return decoratorList != null && !decoratorList.isEmpty() ? new CompositeFilterDecorator<>(decoratorList) : fetchingDecorator;
    }

    private FetchingFilterDecorator createFetchingDecorator(Annotation[] fetchingAnnotations) {
        if (fetchingAnnotations != null && fetchingAnnotations.length != 0) {
            List<Fetching> fetches = new ArrayList<>();
            for (Annotation annotation : fetchingAnnotations) {
                if (annotation.annotationType().equals(Fetching.class)) {
                    fetches.add((Fetching) annotation);
                } else if (annotation.annotationType().equals(Fetches.class)) {
                    Fetches fetchesAnnotation = (Fetches) annotation;
                    fetches.addAll(Arrays.asList(fetchesAnnotation.value()));
                }
            }
            return fetches.isEmpty() ? null : new FetchingFilterDecorator(fetches);
        }
        return null;
    }
}
