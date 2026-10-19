package com.runestone.dynafilter.core.generator.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** One public sort name mapped to exactly one persistent path. */
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface SortAlias {
    String name();

    String path();
}
