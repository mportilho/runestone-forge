package com.runestone.dynafilter.core.generator.annotation;

import com.runestone.dynafilter.core.resolver.FilterDecorator;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Retention(RUNTIME)
@Target({PARAMETER, TYPE})
public @interface FilterDecorators {

    Class<? extends FilterDecorator<?>>[] value();

}
