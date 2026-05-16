package com.runestone.dynafilter.core.exception;

public class DynamicFilterConfigurationException extends RuntimeException {

    public DynamicFilterConfigurationException(String message) {
        super(message);
    }

    public DynamicFilterConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
