package com.runestone.dynafilter.modules.jpa.spring;

import com.runestone.dynafilter.core.exceptions.DynamicFilterConfigurationException;
import com.runestone.dynafilter.core.generator.ConditionalStatement;
import com.runestone.dynafilter.core.generator.annotation.AnnotationStatementInput;
import com.runestone.dynafilter.core.generator.annotation.AnnotationStatementGenerator;
import com.runestone.dynafilter.core.generator.annotation.TypeAnnotationUtils;
import com.runestone.dynafilter.core.model.FilterRequestData;
import com.runestone.dynafilter.core.operation.FilterOperationMetadata;
import com.runestone.dynafilter.core.operation.FilterOperationService;
import com.runestone.dynafilter.core.operation.types.Decorated;
import com.runestone.dynafilter.core.operation.types.Dynamic;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.framework.autoproxy.AutoProxyUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.runestone.dynafilter.core.generator.annotation.TypeAnnotationUtils.listAllFilterRequestData;
import static com.runestone.dynafilter.helpers.StringHelper.formatPath;

public class FilterConfigurationAnalyserBeanPostProcessor implements BeanFactoryAware, SmartInitializingSingleton {

    private final FilterOperationService<Specification<?>> filterOperationService;
    private final AnnotationStatementGenerator statementGenerator;
    private ConfigurableListableBeanFactory beanFactory;

    public FilterConfigurationAnalyserBeanPostProcessor(FilterOperationService<Specification<?>> filterOperationService,
                                                        AnnotationStatementGenerator statementGenerator) {
        this.filterOperationService = filterOperationService;
        this.statementGenerator = statementGenerator;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        if (!(beanFactory instanceof ConfigurableListableBeanFactory configurableBeanFactory)) {
            throw new IllegalStateException("A ConfigurableListableBeanFactory is required to validate dynamic filter controllers");
        }
        this.beanFactory = configurableBeanFactory;
    }

    @Override
    public void afterSingletonsInstantiated() {
        String[] beanNames = beanFactory.getBeanNamesForType(Object.class, true, false);
        for (String beanName : beanNames) {
            if (beanFactory.findAnnotationOnBean(beanName, Controller.class, false) != null) {
                Class<?> beanType = AutoProxyUtils.determineTargetClass(beanFactory, beanName);
                if (beanType != null) {
                    validateControllerType(beanType);
                }
            }
        }
    }

    public Object postProcessAfterInitialization(Object bean, String beanName) {
        validateControllerType(AopProxyUtils.ultimateTargetClass(bean));
        return bean;
    }

    private void validateControllerType(Class<?> controllerType) {
        for (Method method : controllerType.getMethods()) {
            for (Parameter parameter : method.getParameters()) {
                warmupAndCheckFilterConfiguration(parameter);
            }
        }
    }

    private void warmupAndCheckFilterConfiguration(Parameter parameter) {
        if (!isDynamicFilterParameter(parameter)) {
            return;
        }

        AnnotationStatementInput input = new AnnotationStatementInput(parameter.getType(), parameter.getAnnotations());
        List<FilterRequestData> allFilters = listAllFilterRequestData(input);
        validateUniqueFilterParameters(allFilters, parameter);
        allFilters.forEach(FilterOperationConfigurationValidator::validateOperationSpecificConfiguration);

        Class<?> entityClass = TypeAnnotationUtils.findFilterTargetClass(parameter);
        if (entityClass != null) {
            allFilters.forEach(filter -> FilterOperationConfigurationValidator.validateEntityConfiguration(filter, entityClass));
        }
        allFilters.forEach(this::checkRegisteredOperation);
        statementGenerator.warmup(input);
    }

    private static void validateUniqueFilterParameters(List<FilterRequestData> filters, Parameter parameter) {
        Map<String, FilterRequestData> filtersByParameterName = new HashMap<>();
        for (FilterRequestData filter : filters) {
            for (String parameterName : filter.parameters()) {
                FilterRequestData existingFilter = filtersByParameterName.putIfAbsent(parameterName, filter);
                if (existingFilter != null) {
                    throw new DynamicFilterConfigurationException(
                            "Filter parameter name '%s' is configured more than once across filters at %s. First filter: operation '%s' on path '%s'; duplicated filter: operation '%s' on path '%s'"
                                    .formatted(parameterName, formatParameterLocation(parameter), existingFilter.operation().getCanonicalName(), formatPath(existingFilter.path()), filter.operation().getCanonicalName(), formatPath(filter.path()))
                    );
                }
            }
        }
    }

    private static String formatParameterLocation(Parameter parameter) {
        Executable executable = parameter.getDeclaringExecutable();
        return "%s#%s, parameter %d '%s' (%s)".formatted(
                executable.getDeclaringClass().getCanonicalName(),
                executable.getName(),
                findParameterIndex(parameter, executable.getParameters()),
                parameter.getName(),
                parameter.getParameterizedType().getTypeName()
        );
    }

    private static int findParameterIndex(Parameter parameter, Parameter[] parameters) {
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].equals(parameter)) {
                return i;
            }
        }
        return -1;
    }

    private void checkRegisteredOperation(FilterRequestData filter) {
        if (!Decorated.class.equals(filter.operation()) && !Dynamic.class.equals(filter.operation())
            && !filterOperationService.supports(filter.operation())) {
            throw new DynamicFilterConfigurationException("Filter operation '%s' used on path '%s' is not registered for JPA specifications"
                    .formatted(filter.operation().getCanonicalName(), formatPath(filter.path())));
        }

        FilterOperationMetadata metadata = filterOperationService.findMetadata(filter.operation());
        FilterOperationConfigurationValidator.validateMetadata(filter, metadata);
    }

    private static boolean isDynamicFilterParameter(Parameter parameter) {
        Class<?> parameterType = parameter.getType();
        return ConditionalStatement.class.equals(parameterType)
               || (parameterType.isInterface() && Specification.class.isAssignableFrom(parameterType));
    }
}
