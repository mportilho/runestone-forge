package com.runestone.dynafilter.modules.jpa.spring;

import com.runestone.dynafilter.core.generator.annotation.AnnotationStatementInput;
import com.runestone.dynafilter.core.generator.annotation.TypeAnnotationUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public final class FilterConfigurationAnalyserBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanType = bean.getClass();
        if (!isController(beanType)) {
            return bean;
        }
        for (Method method : beanType.getDeclaredMethods()) {
            for (Parameter parameter : method.getParameters()) {
                TypeAnnotationUtils.listAllFilterRequestData(new AnnotationStatementInput(parameter.getType(), parameter.getAnnotations()));
            }
        }
        return bean;
    }

    private static boolean isController(Class<?> beanType) {
        return AnnotatedElementUtils.hasAnnotation(beanType, Controller.class)
                || AnnotatedElementUtils.hasAnnotation(beanType, RestController.class);
    }
}
