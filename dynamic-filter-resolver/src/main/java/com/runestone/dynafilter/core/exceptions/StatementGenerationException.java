package com.runestone.dynafilter.core.exceptions;

public class StatementGenerationException extends RuntimeException{

    public StatementGenerationException(String message) {
        super(message);
    }

    public StatementGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
