package com.runestone.dynafilter.modules.jpa.resolver;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Aggregates repeatable Fetching annotations
 *
 * @author Marcelo Portilho
 */
@Documented
@Retention(RUNTIME)
@Target({TYPE, ANNOTATION_TYPE})
public @interface Fetches {

    /**
     * @return array of repeatable Fetching annotations
     */
    Fetching[] value() default {};
}
