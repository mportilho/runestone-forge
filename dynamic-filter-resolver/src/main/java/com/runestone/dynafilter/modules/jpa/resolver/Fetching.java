package com.runestone.dynafilter.modules.jpa.resolver;

import jakarta.persistence.criteria.JoinType;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Adds fetching capabilities to a dynamic filter provider for JPA Criteria API. This annotation is repeatable allowing multiple fetches
 * to be defined, but beware the {@link org.hibernate.loader.MultipleBagFetchException} when using multiple fetches on the same entity.
 *
 * <p>
 * {@link org.hibernate.loader.MultipleBagFetchException} indicates that it's not possible to fetch multiple bags in the same query. Refer
 * to the JPA provider's documentation for more information.
 * </p>
 *
 * @author Marcelo Portilho
 */
@Documented
@Retention(RUNTIME)
@Target({TYPE, ANNOTATION_TYPE})
@Repeatable(Fetches.class)
public @interface Fetching {

    /**
     * @return the entity's attributes that must be eagerly fetched
     */
    String[] value();

    /**
     * @return The join Type used for fetching the desired attributes
     */
    JoinType joinType() default JoinType.LEFT;
}
