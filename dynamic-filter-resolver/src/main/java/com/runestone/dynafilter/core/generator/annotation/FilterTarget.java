package com.runestone.dynafilter.core.generator.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation to be used in the record class that will be used as a filter target
 *
 * @author Marcelo Portilho.
 */
@Documented
@Retention(RUNTIME)
@Target({ElementType.TYPE, ElementType.PARAMETER})
public @interface FilterTarget {

    /**
     * The target class that will be used as a filter target
     *
     * @return The target class
     */
    Class<?> value();

    /**
     * If true, the filter will use the target fields metadata to generate the filter
     *
     * @return If true, the filter will use the target fields metadata to generate the filter
     */
    boolean useTargetFieldsMetadata() default true;

}
