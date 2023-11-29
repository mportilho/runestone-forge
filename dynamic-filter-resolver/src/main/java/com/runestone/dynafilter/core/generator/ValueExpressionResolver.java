package com.runestone.dynafilter.core.generator;

/**
 * Resolves a variable representation (or expression language value) to a
 * dynamic provided value. Useful for configuring default values for the
 * filter's parameters. It can encapsulate others converters like the
 * Spring Framework's expression language converter
 *
 * @author Marcelo Portilho
 */
@FunctionalInterface
public interface ValueExpressionResolver<V> {

    /**
     * Resolves an expression to a dynamic value
     *
     * @param value The source String value
     * @return The resolved value from the source or null if no value was found
     */
    V resolveValue(String value);

}
