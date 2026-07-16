package com.runestone.converters;

/**
 * A conversion rule selected and bound during setup for repeated invocation.
 */
@FunctionalInterface
public interface PreparedDataConversion {

    Object convert(Object source);
}
