package com.runestone.expeval.exceptions;

public class ExpressionEvaluatorException extends RuntimeException {

    public ExpressionEvaluatorException(String message) {
        super(message);
    }

    public ExpressionEvaluatorException(String message, Throwable cause) {
        super(message, cause);
    }
}
