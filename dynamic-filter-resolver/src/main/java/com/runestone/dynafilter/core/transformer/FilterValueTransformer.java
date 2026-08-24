package com.runestone.dynafilter.core.transformer;

/**
 * Interprets a non-null filter value before the filter operation converts it.
 * Implementations are shared between requests and must be stateless or thread-safe. Returning
 * {@code null} for a non-null input violates the contract and aborts statement generation.
 */
@FunctionalInterface
public interface FilterValueTransformer {

    Object transform(Object value, FilterValueContext context);
}
