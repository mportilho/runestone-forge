package com.runestone.expeval_mk3.internal.regex;

import com.google.re2j.Pattern;
import com.google.re2j.PatternSyntaxException;
import com.google.re2j.Matcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.IntConsumer;

/** The single RE2/J-backed regular-expression seam used by the expression language. */
public final class LinearRegex {

    private static final int REGEX_UNITS = 256;
    private static final int COMPILED_UNITS_PER_SOURCE_CHARACTER = 32;

    private final Pattern pattern;
    private final int estimatedRetainedWeight;

    private LinearRegex(Pattern pattern, int sourceLength) {
        this.pattern = pattern;
        estimatedRetainedWeight = Math.toIntExact(Math.min(
                Integer.MAX_VALUE, REGEX_UNITS + (long) sourceLength * COMPILED_UNITS_PER_SOURCE_CHARACTER));
    }

    public static LinearRegex compile(String source) {
        Objects.requireNonNull(source, "source");
        try {
            return new LinearRegex(Pattern.compile(source), source.length());
        } catch (PatternSyntaxException exception) {
            throw new InvalidRegexPatternException("invalid linear regex pattern: " + exception.getMessage(), exception);
        }
    }

    public boolean matches(String value) {
        return pattern.matcher(Objects.requireNonNull(value, "value")).matches();
    }

    /** Conservative source-controlled payload retained by this compiled RE2/J pattern. */
    public int estimatedRetainedWeight() {
        return estimatedRetainedWeight;
    }

    public String replaceAll(String value, String replacement) {
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(replacement, "replacement");
        return pattern.matcher(value).replaceAll(replacement);
    }

    public List<String> split(String value) {
        return List.of(pattern.split(Objects.requireNonNull(value, "value"), -1));
    }

    /** Bounds each append before it can grow the output beyond the caller's policy. */
    public String replaceAllBounded(String value, String replacement, int maxLength, IntConsumer exceeded) {
        Matcher matcher = pattern.matcher(value);
        StringBuilder result = new StringBuilder();
        int last = 0;
        while (matcher.find()) {
            append(result, value, last, matcher.start(), maxLength, exceeded);
            appendReplacement(result, matcher, replacement, maxLength, exceeded);
            last = matcher.end();
        }
        append(result, value, last, value.length(), maxLength, exceeded);
        return result.toString();
    }

    public List<String> splitBounded(String value, int maxSize, int maxTextLength,
                                     Runnable sizeExceeded, Runnable textExceeded) {
        Matcher matcher = pattern.matcher(value);
        ArrayList<String> segments = new ArrayList<>();
        int last = 0;
        while (matcher.find()) {
            if (last == 0 && matcher.end() == 0) {
                last = matcher.end();
                continue;
            }
            addSegment(segments, value, last, matcher.start(), maxSize, maxTextLength, sizeExceeded, textExceeded);
            last = matcher.end();
        }
        addSegment(segments, value, last, value.length(), maxSize, maxTextLength, sizeExceeded, textExceeded);
        return List.copyOf(segments);
    }

    private static void addSegment(List<String> segments, String value, int start, int end,
                                   int maxSize, int maxTextLength, Runnable sizeExceeded, Runnable textExceeded) {
        if (segments.size() >= maxSize) {
            sizeExceeded.run();
        }
        if (end - start > maxTextLength) {
            textExceeded.run();
        }
        segments.add(value.substring(start, end));
    }

    private static void append(StringBuilder result, String text, int start, int end,
                               int maximum, IntConsumer exceeded) {
        if ((long) result.length() + end - start > maximum) {
            exceeded.accept(maximum);
        }
        result.append(text, start, end);
    }

    private static void appendReplacement(StringBuilder result, Matcher matcher, String replacement,
                                          int maximum, IntConsumer exceeded) {
        int last = 0;
        int length = replacement.length();
        for (int index = 0; index < length - 1; index++) {
            char character = replacement.charAt(index);
            if (character == '\\') {
                append(result, replacement, last, index, maximum, exceeded);
                index++;
                last = index;
            } else if (character == '$' && replacement.charAt(index + 1) == '{') {
                append(result, replacement, last, index, maximum, exceeded);
                int end = replacement.indexOf('}', index + 2);
                int space = replacement.indexOf(' ', index + 2);
                if (end < 0 || (space >= 0 && space < end)) {
                    throw new IllegalArgumentException("named capture group is missing trailing '}'");
                }
                String group = matcher.group(replacement.substring(index + 2, end));
                group = group == null ? "null" : group;
                append(result, group, 0, group.length(), maximum, exceeded);
                index = end;
                last = end + 1;
            } else if (character == '$' && Character.isDigit(replacement.charAt(index + 1))
                    && replacement.charAt(index + 1) <= '9') {
                append(result, replacement, last, index, maximum, exceeded);
                int groupNumber = replacement.charAt(++index) - '0';
                while (index + 1 < length && replacement.charAt(index + 1) >= '0'
                        && replacement.charAt(index + 1) <= '9'
                        && groupNumber <= (matcher.groupCount() - (replacement.charAt(index + 1) - '0')) / 10) {
                    groupNumber = groupNumber * 10 + replacement.charAt(++index) - '0';
                }
                String group = matcher.group(groupNumber);
                if (group != null) {
                    append(result, group, 0, group.length(), maximum, exceeded);
                }
                last = index + 1;
            }
        }
        append(result, replacement, last, length, maximum, exceeded);
    }
}
