package com.runestone.dynafilter.modules.jpa.spring;

import com.runestone.dynafilter.core.generator.annotation.*;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;

public class FilterConfigurationAnalyserBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (AnnotationUtils.findAnnotation(bean.getClass(), RestController.class) != null) {
            for (Method method : bean.getClass().getMethods()) {
                for (Parameter parameter : method.getParameters()) {
                    analyseExternalFilterConfiguration(parameter);
                    analyseTypedFilterConfiguration(parameter);
                }
            }
        }
        return bean;
    }

    private static void analyseTypedFilterConfiguration(Parameter parameter) {
        Filter[] filters;
        Conjunction conjunction = parameter.getAnnotation(Conjunction.class);
        if (conjunction != null) {
            filters = conjunction.value();
        } else {
            Disjunction disjunction = parameter.getAnnotation(Disjunction.class);
            filters = disjunction != null ? disjunction.value() : null;
        }
        if (filters != null && filters.length > 0 && parameter.getType().isAssignableFrom(Specification.class)) {
            Class<?> entityClass = (Class<?>) ((ParameterizedType) parameter.getParameterizedType()).getActualTypeArguments()[0];
            for (Filter filter : filters) {
                TypeAnnotationUtils.findFilterField(entityClass, filter.path());
            }

        }
    }

    private static void analyseExternalFilterConfiguration(Parameter parameter) {
        Class<?> filterClass;
        ConjunctionFrom conjunctionFrom = parameter.getAnnotation(ConjunctionFrom.class);
        if (conjunctionFrom != null) {
            filterClass = conjunctionFrom.value();
        } else {
            DisjunctionFrom disjunctionFrom = parameter.getAnnotation(DisjunctionFrom.class);
            filterClass = disjunctionFrom != null ? disjunctionFrom.value() : null;
        }
        if (filterClass != null) {
            FilterTarget filterTargetAnnotation = filterClass.getAnnotation(FilterTarget.class);
            if (filterTargetAnnotation == null) {
                throw new IllegalArgumentException(""); //FIXME colocar mensagem na exceção
            }
            Class<?> entityClass = filterTargetAnnotation.value();
            for (Field declaredField : filterClass.getDeclaredFields()) {
                Filter filter = declaredField.getAnnotation(Filter.class);
                TypeAnnotationUtils.findFilterField(entityClass, filter.path());
            }
        }
    }

}
