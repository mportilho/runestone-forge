package com.runestone.dynafilter.modules.jpa.spring;

import com.runestone.dynafilter.core.exceptions.DynamicFilterConfigurationException;
import com.runestone.dynafilter.core.exceptions.FilterOperationNotDefinedException;
import com.runestone.dynafilter.core.generator.ConditionalStatement;
import com.runestone.dynafilter.core.generator.annotation.AnnotationStatementInput;
import com.runestone.dynafilter.core.generator.annotation.TypeAnnotationUtils;
import com.runestone.dynafilter.core.model.FilterRequestData;
import com.runestone.dynafilter.core.operation.FilterOperationMetadata;
import com.runestone.dynafilter.core.operation.FilterOperationService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.runestone.dynafilter.core.generator.annotation.TypeAnnotationUtils.listAllFilterRequestData;
import static com.runestone.dynafilter.helpers.StringHelper.formatPath;

public class FilterConfigurationAnalyserBeanPostProcessor implements BeanPostProcessor {

    private final FilterOperationService<Specification<?>> filterOperationService;

    public FilterConfigurationAnalyserBeanPostProcessor(FilterOperationService<Specification<?>> filterOperationService) {
        this.filterOperationService = Objects.requireNonNull(filterOperationService, "filterOperationService cannot be null");
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (AnnotationUtils.findAnnotation(bean.getClass(), RestController.class) != null) {
            for (Method method : bean.getClass().getMethods()) {
                for (Parameter parameter : method.getParameters()) {
                    warmupAndCheckFilterConfiguration(parameter);
                }
            }
        }
        return bean;
    }

    private void warmupAndCheckFilterConfiguration(Parameter parameter) {
        if (!isDynamicFilterParameter(parameter)) {
            return;
        }

        AnnotationStatementInput input = new AnnotationStatementInput(parameter.getType(), parameter.getAnnotations());
        List<FilterRequestData> allFilters = listAllFilterRequestData(input);
        validateUniqueParameterNames(allFilters, parameter);

        Class<?> entityClass = TypeAnnotationUtils.findFilterTargetClass(parameter);
        for (FilterRequestData filter : allFilters) {
            FilterOperationConfigurationValidator.validateOperationSpecificConfiguration(filter);
            FilterOperationMetadata metadata = findMetadata(filter);
            FilterOperationConfigurationValidator.validateMetadata(filter, metadata);
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
                            .formatted(filter.operation().getCanonicalName(), formatPath(filter.path())),
                    e
            );
        }
    }

    private static void validateUniqueParameterNames(List<FilterRequestData> filters, Parameter parameter) {
        Map<String, FilterRequestData> seen = new HashMap<>();
        for (FilterRequestData filter : filters) {
            for (String parameterName : filter.parameters()) {
                FilterRequestData previous = seen.putIfAbsent(parameterName, filter);
                if (previous != null) {
                    throw new DynamicFilterConfigurationException(
                            "Filter parameter '%s' configured more than once across filters on %s#%s parameter %d: %s on path '%s' conflicts with %s on path '%s'"
                                    .formatted(
                                            parameterName,
                                            parameter.getDeclaringExecutable().getDeclaringClass().getCanonicalName(),
                                            parameter.getDeclaringExecutable().getName(),
                                            findParameterIndex(parameter),
                                            previous.operation().getCanonicalName(),
                                            formatPath(previous.path()),
                                            filter.operation().getCanonicalName(),
                                            formatPath(filter.path())
                                    )
                    );
                }
            }
        }
    }

    private static int findParameterIndex(Parameter parameter) {
        Parameter[] parameters = parameter.getDeclaringExecutable().getParameters();
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].equals(parameter)) {
                return i;
            }
        }
        return -1;
    }

    private static boolean isDynamicFilterParameter(Parameter parameter) {
        Class<?> parameterType = parameter.getType();
        return ConditionalStatement.class.equals(parameterType)
                || (parameterType.isInterface() && Specification.class.isAssignableFrom(parameterType));
    }
}
