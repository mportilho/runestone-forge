package com.runestone.dynafilter.modules.jpa.spring;

import com.runestone.dynafilter.core.generator.ConditionalStatement;
import com.runestone.dynafilter.core.generator.annotation.AnnotationStatementInput;
import com.runestone.dynafilter.core.generator.annotation.TypeAnnotationUtils;
import com.runestone.dynafilter.core.model.FilterRequestData;
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

    private static void warmupAndCheckFilterConfiguration(Parameter parameter) {
        if (!isDynamicFilterParameter(parameter)) {
            return;
        }

        AnnotationStatementInput input = new AnnotationStatementInput(parameter.getType(), parameter.getAnnotations());
        List<FilterRequestData> allFilters = listAllFilterRequestData(input);

        Class<?> entityClass = TypeAnnotationUtils.findFilterTargetClass(parameter);
        if (entityClass != null) {
            allFilters.forEach(filter -> findFilterField(entityClass, filter.path()));
        }
    }

    private static boolean isDynamicFilterParameter(Parameter parameter) {
        Class<?> parameterType = parameter.getType();
        return ConditionalStatement.class.equals(parameterType)
                || (parameterType.isInterface() && Specification.class.isAssignableFrom(parameterType));
    }
}
