package com.runestone.expeval_mk3.internal.regex;

/** Identifies a language-controlled pattern rejected by the RE2/J subset. */
public final class InvalidRegexPatternException extends IllegalArgumentException {

    InvalidRegexPatternException(String message, Throwable cause) {
        super(message, cause);
    }
}
