package com.runestone.dynafilter.core.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface StatementFrom {

    Class<?> value();

    String negate() default "false";
}
