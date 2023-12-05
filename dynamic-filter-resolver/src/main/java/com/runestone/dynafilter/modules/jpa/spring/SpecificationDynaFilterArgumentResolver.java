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

import com.runestone.dynafilter.core.generator.StatementWrapper;
import com.runestone.dynafilter.core.generator.annotation.AnnotationStatementGenerator;
import com.runestone.dynafilter.core.generator.annotation.AnnotationStatementInput;
import com.runestone.dynafilter.core.generator.annotation.FilterDecorators;
import com.runestone.dynafilter.core.resolver.DynamicFilterResolver;
import com.runestone.dynafilter.core.resolver.FilterDecorator;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.HandlerMapping;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SpecificationDynaFilterArgumentResolver implements HandlerMethodArgumentResolver {

    private final AnnotationStatementGenerator statementGenerator;
    private final DynamicFilterResolver<Specification<?>> dynamicFilterResolver;
    private final FilterDecoratorSpringFactory filterDecoratorFactory;

    public SpecificationDynaFilterArgumentResolver(AnnotationStatementGenerator statementGenerator,
                                                   DynamicFilterResolver<Specification<?>> dynamicFilterResolver,
                                                   FilterDecoratorSpringFactory filterDecoratorFactory) {
        this.statementGenerator = Objects.requireNonNull(statementGenerator, "Statement generator cannot be null");
        this.dynamicFilterResolver = Objects.requireNonNull(dynamicFilterResolver, "Dynamic filter resolver cannot be null");
        this.filterDecoratorFactory = Objects.requireNonNull(filterDecoratorFactory, "Filter decorator factory cannot be null");
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        Class<?> parameterType = parameter.getParameterType();
        return parameterType.isInterface() && Specification.class.isAssignableFrom(parameterType);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        Map<String, Object> userParameters = createParametersMap(webRequest);
        AnnotationStatementInput input = new AnnotationStatementInput(parameter.getParameterType(), parameter.getParameterAnnotations());

        var fetchingAnnotations = parameter.getParameterAnnotations();
        FilterDecorators parameterAnnotation = parameter.getParameterAnnotation(FilterDecorators.class);
        var decoratorClasses = parameterAnnotation != null ? parameterAnnotation.value() : null;
        FilterDecorator<Specification<?>> filterDecorator = filterDecoratorFactory.createFilterDecorators(fetchingAnnotations, decoratorClasses);

        StatementWrapper statementWrapper = statementGenerator.generateStatements(input, userParameters);
        Specification<?> specification = dynamicFilterResolver.createFilter(statementWrapper, filterDecorator);
        return specification != null ? createProxy(specification, parameter.getParameterType()) : null;
    }

    @SuppressWarnings("unchecked")
    protected Map<String, Object> createParametersMap(NativeWebRequest webRequest) {
        HttpServletRequest httpServletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
        if (httpServletRequest == null) {
            throw new IllegalStateException("Obligatory HTTP Context not found");
        }

        Map<String, Object> parametersMap = new HashMap<>(15);
        webRequest.getParameterMap().forEach((key, value) -> {
            if (value.length == 1) {
                parametersMap.put(key, value[0]);
            } else if (value.length > 1) {
                parametersMap.put(key, value);
            }
        });

        Map<String, Object> pathVariables = (Map<String, Object>) httpServletRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (pathVariables != null) {
            parametersMap.putAll(pathVariables);
        }
        return parametersMap;
    }

    /**
     * Creates a new proxy object for interfaces that extends {@link Specification}
     */
    @SuppressWarnings("unchecked")
    private <T> T createProxy(Object target, Class<T> targetInterface) {
        if (targetInterface.isAssignableFrom(target.getClass())) {
            return (T) target;
        }
        return (T) Proxy.newProxyInstance(ClassUtils.getDefaultClassLoader(), new Class[]{targetInterface}, (proxy, method, args) -> method.invoke(target, args));
    }

}
