package com.runestone.dynafilter.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.TYPE})
public @interface Conjunction {

    Class<?> target() default Object.class;

    Filter[] value() default {};

    Statement[] disjunctions() default {};

    String negate() default "false";
}
