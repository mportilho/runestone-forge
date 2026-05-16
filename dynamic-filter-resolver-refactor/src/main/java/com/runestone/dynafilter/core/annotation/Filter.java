package com.runestone.dynafilter.core.annotation;

import com.runestone.dynafilter.core.modifier.FilterModifier;
import com.runestone.dynafilter.core.operation.DefinedFilterOperation;
import com.runestone.dynafilter.core.operation.Dynamic;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Filter {

    String path();

    String[] parameters();

    Class<?> targetType() default Object.class;

    Class<? extends DefinedFilterOperation> operation() default Dynamic.class;

    String negate() default "false";

    String[] defaultValues() default {};

    String[] constantValues() default {};

    String format() default "";

    boolean required() default false;

    Class<? extends FilterModifier>[] modifiers() default {};

    String description() default "";
}
