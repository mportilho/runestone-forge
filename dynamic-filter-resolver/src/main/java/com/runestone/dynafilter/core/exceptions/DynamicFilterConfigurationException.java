package com.runestone.dynafilter.core.exceptions;

/**
 * Indicates a malformed or incomplete filter configuration
 *
 * @author Marcelo Portilho
 */
public class DynamicFilterConfigurationException extends RuntimeException {

    public DynamicFilterConfigurationException() {
    }

    public DynamicFilterConfigurationException(String message) {
        super(message);
    }

    public DynamicFilterConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
