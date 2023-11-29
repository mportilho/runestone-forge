package com.runestone.expeval.exceptions;

public class ExpressionConfigurationException extends RuntimeException {

    public ExpressionConfigurationException(String message) {
        super(message);
    }

    public ExpressionConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
