package com.runestone.dynafilter.modules.jpa.resolver;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE, ElementType.PARAMETER})
@Repeatable(Fetches.class)
public @interface Fetching {

    String path();

    Class<?> joinType() default Object.class;
}
