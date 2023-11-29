package com.runestone.expeval.support.callsite;

/**
 * Exception thrown when an error occurs while trying to create an operation call site
 *
 * @author Marcelo Portilho
 */
public class OperationCallSiteException extends RuntimeException {

    public OperationCallSiteException(String message, Throwable cause) {
        super(message, cause);
    }
}
