package com.runestone.dynafilter.modules.jpa.spring;

import com.runestone.dynafilter.core.decorator.FilterDecorator;
import com.runestone.dynafilter.core.decorator.FilterDecoratorFactory;
import com.runestone.dynafilter.core.generator.ConditionalStatement;
import com.runestone.dynafilter.core.generator.StatementWrapper;
import com.runestone.dynafilter.core.generator.annotation.AnnotationStatementGenerator;
import com.runestone.dynafilter.core.generator.annotation.AnnotationStatementInput;
import com.runestone.dynafilter.modules.jpa.resolver.SpecificationDynamicFilterResolver;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.core.MethodParameter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.HandlerMapping;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class SpecificationDynamicFilterArgumentResolver implements HandlerMethodArgumentResolver {

    private final AnnotationStatementGenerator statementGenerator;
    private final SpecificationDynamicFilterResolver dynamicFilterResolver;
    private final FilterDecoratorFactory filterDecoratorFactory;

    public SpecificationDynamicFilterArgumentResolver(
            AnnotationStatementGenerator statementGenerator,
            SpecificationDynamicFilterResolver dynamicFilterResolver,
            FilterDecoratorFactory filterDecoratorFactory
    ) {
        this.statementGenerator = Objects.requireNonNull(statementGenerator, "statementGenerator must not be null");
        this.dynamicFilterResolver = Objects.requireNonNull(dynamicFilterResolver, "dynamicFilterResolver must not be null");
        this.filterDecoratorFactory = Objects.requireNonNull(filterDecoratorFactory, "filterDecoratorFactory must not be null");
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        Class<?> parameterType = parameter.getParameterType();
        return ConditionalStatement.class.equals(parameterType) || Specification.class.isAssignableFrom(parameterType);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        AnnotationStatementInput input = new AnnotationStatementInput(parameter.getParameterType(), parameter.getParameterAnnotations());
        StatementWrapper statementWrapper = statementGenerator.generateStatements(input, requestParameters(webRequest));
        @SuppressWarnings("unchecked")
        FilterDecorator<Specification<?>> decorator = (FilterDecorator<Specification<?>>) filterDecoratorFactory.createDecorator(input);
        ConditionalStatement conditionalStatement = new ConditionalStatement(statementWrapper, decorator);
        if (ConditionalStatement.class.equals(parameter.getParameterType())) {
            return conditionalStatement;
        }
        Specification<?> specification = dynamicFilterResolver.createFilter(statementWrapper, decorator);
        if (Specification.class.equals(parameter.getParameterType())) {
            return specification;
        }
        return specificationProxy(parameter.getParameterType(), specification);
    }

    private static Map<String, Object> requestParameters(NativeWebRequest webRequest) {
        Map<String, Object> parameters = new LinkedHashMap<>();
        webRequest.getParameterMap().forEach((key, values) -> parameters.put(key, values.length == 1 ? values[0] : values));
        @SuppressWarnings("unchecked")
        Map<String, String> uriVariables = (Map<String, String>) webRequest.getAttribute(
                HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE,
                NativeWebRequest.SCOPE_REQUEST
        );
        if (uriVariables != null) {
            parameters.putAll(uriVariables);
        }
        return parameters;
    }

    private static Object specificationProxy(Class<?> parameterType, Specification<?> specification) {
        InvocationHandler invocationHandler = new SpecificationInvocationHandler(specification);
        return Proxy.newProxyInstance(parameterType.getClassLoader(), new Class<?>[]{parameterType}, invocationHandler);
    }

    private record SpecificationInvocationHandler(Specification<?> specification) implements InvocationHandler {

        private SpecificationInvocationHandler {
            Objects.requireNonNull(specification, "specification must not be null");
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().equals("toPredicate")) {
                return cast(specification).toPredicate((Root<Object>) args[0], (CriteriaQuery<?>) args[1], (CriteriaBuilder) args[2]);
            }
            throw new UnsupportedOperationException("Only Specification.toPredicate is supported by dynamic filter proxy");
        }

        @SuppressWarnings("unchecked")
        private static Specification<Object> cast(Specification<?> specification) {
            return (Specification<Object>) specification;
        }
    }
}
