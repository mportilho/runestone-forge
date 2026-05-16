package com.runestone.dynafilter.core.exception;

public class StatementGenerationException extends RuntimeException {

    public StatementGenerationException(String message) {
        super(message);
    }

    public StatementGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
