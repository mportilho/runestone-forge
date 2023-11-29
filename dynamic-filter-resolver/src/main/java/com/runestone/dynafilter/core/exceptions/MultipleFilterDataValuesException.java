package com.runestone.dynafilter.core.exceptions;

/**
 * Exception thrown when a filter data has multiple values when it should have only one
 *
 * @author Marcelo Portilho
 */
public class MultipleFilterDataValuesException extends RuntimeException {

    public MultipleFilterDataValuesException(String message) {
        super(message);
    }

}
