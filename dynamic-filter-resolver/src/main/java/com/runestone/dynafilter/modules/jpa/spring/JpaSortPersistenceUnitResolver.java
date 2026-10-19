package com.runestone.dynafilter.modules.jpa.spring;

import jakarta.persistence.EntityManagerFactory;

import java.lang.reflect.Parameter;

/** Optional adapter configuration for contracts whose entity belongs to multiple persistence units. */
@FunctionalInterface
public interface JpaSortPersistenceUnitResolver {
    /**
     * Returns a registered, open factory for this contract, or null to use unambiguous metamodel inference.
     * This association affects metadata validation and documentation, never repository dispatch.
     */
    EntityManagerFactory resolve(Parameter contract, Class<?> entity);
}
