package com.runestone.dynafilter.core.annotation;

import com.runestone.dynafilter.core.decorator.FilterDecorator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
public @interface FilterDecorators {

    Class<? extends FilterDecorator<?>>[] value();
}
