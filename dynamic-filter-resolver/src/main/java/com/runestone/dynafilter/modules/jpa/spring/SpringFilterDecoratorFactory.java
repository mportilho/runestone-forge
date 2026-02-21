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

import com.runestone.dynafilter.core.generator.annotation.AnnotationStatementInput;
import com.runestone.dynafilter.core.generator.annotation.TypeAnnotationUtils;
import com.runestone.dynafilter.core.resolver.CompositeFilterDecorator;
import com.runestone.dynafilter.core.resolver.FilterDecorator;
import com.runestone.dynafilter.core.resolver.FilterDecoratorFactory;
import com.runestone.dynafilter.modules.jpa.resolver.Fetches;
import com.runestone.dynafilter.modules.jpa.resolver.Fetching;
import com.runestone.dynafilter.modules.jpa.resolver.FetchingFilterDecorator;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.data.jpa.domain.Specification;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SpringFilterDecoratorFactory implements FilterDecoratorFactory<Specification<?>> {

    private final GenericApplicationContext applicationContext;
    private final ConcurrentMap<AnnotationStatementInput, Optional<FilterDecorator<Specification<?>>>> decoratorCache;
    private final ConcurrentMap<Class<? extends FilterDecorator<?>>, List<FilterDecorator<Specification<?>>>> decoratorsByClass;

    public SpringFilterDecoratorFactory(GenericApplicationContext applicationContext) {
        this.applicationContext = Objects.requireNonNull(applicationContext, "applicationContext is required");
        this.decoratorCache = new ConcurrentHashMap<>();
        this.decoratorsByClass = new ConcurrentHashMap<>();
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public FilterDecorator<Specification<?>> createFilterDecorators(AnnotationStatementInput input) {
        if (input == null) {
            return null;
        }
        Optional<FilterDecorator<Specification<?>>> cachedDecorator = decoratorCache.get(input);
        if (cachedDecorator != null) {
            return cachedDecorator.orElse(null);
        }
        FilterDecorator<Specification<?>> createdDecorator = createFilterDecoratorsInternal(input);
        Optional<FilterDecorator<Specification<?>>> cacheValue = Optional.ofNullable(createdDecorator);
        Optional<FilterDecorator<Specification<?>>> existing = decoratorCache.putIfAbsent(input, cacheValue);
        return existing != null ? existing.orElse(null) : createdDecorator;
    }

    private FilterDecorator<Specification<?>> createFilterDecoratorsInternal(AnnotationStatementInput input) {
        Annotation[] fetchingAnnotations = input.annotations();
        List<Class<? extends FilterDecorator<?>>> decoratorClasses = TypeAnnotationUtils.findFilterDecorators(input);
        FetchingFilterDecorator fetchingDecorator = createFetchingDecorator(fetchingAnnotations);
        if (decoratorClasses.isEmpty()) {
            return fetchingDecorator;
        }

        List<FilterDecorator<Specification<?>>> decoratorList = new ArrayList<>(decoratorClasses.size() + 1);
        for (Class<? extends FilterDecorator<?>> decoratorClass : decoratorClasses) {
            decoratorList.addAll(resolveDecorators(decoratorClass));
        }
        if (fetchingDecorator != null) {
            decoratorList.add(fetchingDecorator);
        }
        return decoratorList.isEmpty() ? null : new CompositeFilterDecorator<>(decoratorList);
    }

    private List<FilterDecorator<Specification<?>>> resolveDecorators(Class<? extends FilterDecorator<?>> decoratorClass) {
        return decoratorsByClass.computeIfAbsent(decoratorClass, this::resolveDecoratorsFromContext);
    }

    @SuppressWarnings({"unchecked"})
    private List<FilterDecorator<Specification<?>>> resolveDecoratorsFromContext(Class<? extends FilterDecorator<?>> decoratorClass) {
        Map<String, ? extends FilterDecorator<?>> beansOfType = applicationContext.getBeansOfType(decoratorClass);
        if (beansOfType.isEmpty()) {
            applicationContext.registerBean(decoratorClass.getSimpleName(), decoratorClass);
            beansOfType = applicationContext.getBeansOfType(decoratorClass);
        }
        if (beansOfType.isEmpty()) {
            return List.of();
        }
        List<FilterDecorator<Specification<?>>> decorators = new ArrayList<>(beansOfType.size());
        for (FilterDecorator<?> filterDecorator : beansOfType.values()) {
            decorators.add((FilterDecorator<Specification<?>>) filterDecorator);
        }
        return List.copyOf(decorators);
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
