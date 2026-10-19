package com.runestone.dynafilter.core.generator.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Optional sort exceptions on a search contract or the endpoint parameter declaring it. */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.PARAMETER})
public @interface SortPolicy {
    /** Additional exact persistent paths, validated by the persistence adapter. */
    String[] include() default {};

    /** Deliberate mappings to single canonical paths, including compound search parameters. */
    SortAlias[] aliases() default {};

    /** Exact canonical destinations forbidden across every contributing declaration. */
    String[] exclude() default {};
}
