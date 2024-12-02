package com.runestone.dynafilter.modules.jpa.spring;

import com.runestone.dynafilter.core.generator.annotation.AnnotationStatementInput;
import com.runestone.dynafilter.core.generator.annotation.TypeAnnotationUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.RestController;

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
                    checkFilterEntityPath(parameter);
                }
            }
        }
        return bean;
    }

    private static void checkFilterEntityPath(Parameter parameter) {
        Class<?> entityClass = TypeAnnotationUtils.findFilterTargetClass(parameter);
        if (entityClass != null) {
            AnnotationStatementInput input = new AnnotationStatementInput(parameter.getType(), parameter.getAnnotations());
            listAllFilterRequestData(input).forEach(filter -> findFilterField(entityClass, filter.path()));
        }
    }
}
