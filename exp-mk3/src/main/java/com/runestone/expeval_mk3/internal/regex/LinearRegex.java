package com.runestone.expeval_mk3.internal.regex;

import com.google.re2j.Pattern;
import com.google.re2j.PatternSyntaxException;

import java.util.List;
import java.util.Objects;

/** The single RE2/J-backed regular-expression seam used by the expression language. */
public final class LinearRegex {

    private final Pattern pattern;

    private LinearRegex(Pattern pattern) {
        this.pattern = pattern;
    }

    public static LinearRegex compile(String source) {
        Objects.requireNonNull(source, "source");
        try {
            return new LinearRegex(Pattern.compile(source));
        } catch (PatternSyntaxException exception) {
            throw new InvalidRegexPatternException("invalid linear regex pattern: " + exception.getMessage(), exception);
        }
    }

    public boolean matches(String value) {
        return pattern.matcher(Objects.requireNonNull(value, "value")).matches();
    }

    public String replaceAll(String value, String replacement) {
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(replacement, "replacement");
        return pattern.matcher(value).replaceAll(replacement);
    }

    public List<String> split(String value) {
        return List.of(pattern.split(Objects.requireNonNull(value, "value"), -1));
    }
}
