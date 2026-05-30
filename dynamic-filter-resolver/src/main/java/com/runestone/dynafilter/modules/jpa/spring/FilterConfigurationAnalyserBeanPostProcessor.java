package com.runestone.dynafilter.modules.jpa.spring;

import com.runestone.dynafilter.core.exceptions.DynamicFilterConfigurationException;
import com.runestone.dynafilter.core.generator.ConditionalStatement;
import com.runestone.dynafilter.core.generator.annotation.AnnotationStatementInput;
import com.runestone.dynafilter.core.generator.annotation.TypeAnnotationUtils;
import com.runestone.dynafilter.core.model.FilterRequestData;
import com.runestone.dynafilter.core.operation.FilterOperationService;
import com.runestone.dynafilter.core.operation.types.Decorated;
import com.runestone.dynafilter.core.operation.types.Dynamic;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import static com.runestone.dynafilter.core.generator.annotation.TypeAnnotationUtils.findFilterField;
import static com.runestone.dynafilter.core.generator.annotation.TypeAnnotationUtils.listAllFilterRequestData;

public class FilterConfigurationAnalyserBeanPostProcessor implements BeanPostProcessor {

    private final FilterOperationService<Specification<?>> filterOperationService;

    public FilterConfigurationAnalyserBeanPostProcessor(FilterOperationService<Specification<?>> filterOperationService) {
        this.filterOperationService = filterOperationService;
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

        Class<?> entityClass = TypeAnnotationUtils.findFilterTargetClass(parameter);
        if (entityClass != null) {
            allFilters.forEach(filter -> findFilterField(entityClass, filter.path()));
        }
        allFilters.forEach(this::checkRegisteredOperation);
    }

    private void checkRegisteredOperation(FilterRequestData filter) {
        if (Decorated.class.equals(filter.operation()) || Dynamic.class.equals(filter.operation())) {
            return;
        }
        if (!filterOperationService.supports(filter.operation())) {
            throw new DynamicFilterConfigurationException("Filter operation '%s' used on path '%s' is not registered for JPA specifications"
                    .formatted(filter.operation().getCanonicalName(), filter.path()));
        }
    }

    private static boolean isDynamicFilterParameter(Parameter parameter) {
        Class<?> parameterType = parameter.getType();
        return ConditionalStatement.class.equals(parameterType)
                || (parameterType.isInterface() && Specification.class.isAssignableFrom(parameterType));
    }
}
