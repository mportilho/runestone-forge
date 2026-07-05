package com.runestone.dynafilter.modules.jpa.spring;

import com.runestone.dynafilter.core.generator.ConditionalStatement;
import com.runestone.dynafilter.core.generator.annotation.AnnotationStatementInput;
import com.runestone.dynafilter.core.generator.annotation.TypeAnnotationUtils;
import com.runestone.dynafilter.core.exceptions.DynamicFilterConfigurationException;
import com.runestone.dynafilter.core.exceptions.FilterOperationNotDefinedException;
import com.runestone.dynafilter.core.model.FilterRequestData;
import com.runestone.dynafilter.core.operation.FilterOperationMetadata;
import com.runestone.dynafilter.core.operation.FilterOperationService;
import com.runestone.dynafilter.helpers.StringHelper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

import static com.runestone.dynafilter.core.generator.annotation.TypeAnnotationUtils.listAllFilterRequestData;

public class FilterConfigurationAnalyserBeanPostProcessor implements BeanPostProcessor {

    private final FilterOperationService<?> filterOperationService;

    public FilterConfigurationAnalyserBeanPostProcessor(FilterOperationService<?> filterOperationService) {
        this.filterOperationService = filterOperationService;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (AnnotationUtils.findAnnotation(bean.getClass(), RestController.class) != null) {
            for (Method method : bean.getClass().getMethods()) {
                Parameter[] parameters = method.getParameters();
                for (int i = 0; i < parameters.length; i++) {
                    warmupAndCheckFilterConfiguration(bean.getClass(), method, i, parameters[i]);
                }
            }
        }
        return bean;
    }

    private void warmupAndCheckFilterConfiguration(Class<?> beanClass, Method method, int parameterIndex, Parameter parameter) {
        if (!isDynamicFilterParameter(parameter)) {
            return;
        }

        AnnotationStatementInput input = new AnnotationStatementInput(parameter.getType(), parameter.getAnnotations());
        List<FilterRequestData> allFilters = listAllFilterRequestData(input);
        validateUniqueParameterNames(beanClass, method, parameterIndex, allFilters);

        Class<?> entityClass = TypeAnnotationUtils.findFilterTargetClass(parameter);
        for (FilterRequestData filter : allFilters) {
            FilterOperationMetadata metadata = findMetadata(filter);
            FilterOperationConfigurationValidator.validateMetadata(filter, metadata);
            FilterOperationConfigurationValidator.validateOperationSpecificConfiguration(filter);
            if (entityClass != null) {
                FilterOperationConfigurationValidator.validateEntityConfiguration(filter, entityClass);
            }
        }
    }

    private FilterOperationMetadata findMetadata(FilterRequestData filter) {
        try {
            return filterOperationService.findMetadata(filter.operation());
        } catch (FilterOperationNotDefinedException e) {
            throw new DynamicFilterConfigurationException(
                    "Filter operation '%s' used on path '%s' is not registered for JPA specifications"
                            .formatted(filter.operation().getCanonicalName(), StringHelper.formatPath(filter.path())), e);
        }
    }

    private static void validateUniqueParameterNames(Class<?> beanClass, Method method, int parameterIndex, List<FilterRequestData> allFilters) {
        Map<String, FilterRequestData> seen = new HashMap<>();
        for (FilterRequestData filter : allFilters) {
            for (String parameterName : filter.parameters()) {
                FilterRequestData previous = seen.putIfAbsent(parameterName, filter);
                if (previous != null) {
                    throw new DynamicFilterConfigurationException(
                            "Filter parameter '%s' is configured more than once across filters on %s#%s parameter %d: %s on path '%s' and %s on path '%s'"
                                    .formatted(parameterName, beanClass.getCanonicalName(), method.getName(), parameterIndex,
                                            previous.operation().getCanonicalName(), StringHelper.formatPath(previous.path()),
                                            filter.operation().getCanonicalName(), StringHelper.formatPath(filter.path())));
                }
            }
        }
    }

    private static boolean isDynamicFilterParameter(Parameter parameter) {
        Class<?> parameterType = parameter.getType();
        return ConditionalStatement.class.equals(parameterType)
                || (parameterType.isInterface() && Specification.class.isAssignableFrom(parameterType));
    }
}
