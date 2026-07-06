package com.runestone.expeval_mk3.api;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.regex.Pattern;

final class StandardStringFunctions {

    private static final String SPACE = " ";

    private StandardStringFunctions() {
    }

    public static String concat(String[] values) {
        Objects.requireNonNull(values, "values");
        StringBuilder builder = new StringBuilder();
        for (String value : values) {
            builder.append(requireText(value));
        }
        return builder.toString();
    }

    public static String toUpper(String value) {
        return requireText(value).toUpperCase(java.util.Locale.ROOT);
    }

    public static String toLower(String value) {
        return requireText(value).toLowerCase(java.util.Locale.ROOT);
    }

    public static String trim(String value) {
        return requireText(value).strip();
    }

    public static String trimLeft(String value) {
        return requireText(value).stripLeading();
    }

    public static String trimRight(String value) {
        return requireText(value).stripTrailing();
    }

    public static String substring(String value, BigDecimal beginIndex) {
        return requireText(value).substring(toInt(beginIndex, "beginIndex"));
    }

    public static String substring(String value, BigDecimal beginIndex, BigDecimal endIndex) {
        return requireText(value).substring(toInt(beginIndex, "beginIndex"), toInt(endIndex, "endIndex"));
    }

    public static String substringBefore(String value, String separator) {
        String text = requireText(value);
        if (separator.isEmpty()) {
            return "";
        }
        int index = text.indexOf(separator);
        return index >= 0 ? text.substring(0, index) : text;
    }

    public static String substringAfter(String value, String separator) {
        String text = requireText(value);
        if (separator.isEmpty()) {
            return text;
        }
        int index = text.indexOf(separator);
        return index >= 0 ? text.substring(index + separator.length()) : "";
    }

    public static String substringBeforeLast(String value, String separator) {
        String text = requireText(value);
        if (separator.isEmpty()) {
            return text;
        }
        int index = text.lastIndexOf(separator);
        return index >= 0 ? text.substring(0, index) : text;
    }

    public static String substringAfterLast(String value, String separator) {
        String text = requireText(value);
        if (separator.isEmpty()) {
            return "";
        }
        int index = text.lastIndexOf(separator);
        return index >= 0 ? text.substring(index + separator.length()) : "";
    }

    public static String padLeft(String value, BigDecimal size) {
        return padLeft(value, size, SPACE);
    }

    public static String padLeft(String value, BigDecimal size, String padding) {
        return pad(requireText(value), toInt(size, "size"), requirePadding(padding), true);
    }

    public static String padRight(String value, BigDecimal size) {
        return padRight(value, size, SPACE);
    }

    public static String padRight(String value, BigDecimal size, String padding) {
        return pad(requireText(value), toInt(size, "size"), requirePadding(padding), false);
    }

    public static String repeat(String value, BigDecimal times) {
        return requireText(value).repeat(toInt(times, "times"));
    }

    public static String replace(String value, String target, String replacement) {
        return requireText(value).replace(target, replacement);
    }

    public static String replaceFirst(String value, String target, String replacement) {
        String text = requireText(value);
        if (target.isEmpty()) {
            return replacement + text;
        }
        int index = text.indexOf(target);
        return index >= 0 ? text.substring(0, index) + replacement + text.substring(index + target.length()) : text;
    }

    public static String replaceAll(String value, String regex, String replacement) {
        return Pattern.compile(regex).matcher(requireText(value)).replaceAll(replacement);
    }

    public static BigDecimal indexOf(String value, String token) {
        return BigDecimal.valueOf(requireText(value).indexOf(token));
    }

    public static BigDecimal lastIndexOf(String value, String token) {
        return BigDecimal.valueOf(requireText(value).lastIndexOf(token));
    }

    public static Boolean startsWith(String value, String prefix) {
        return requireText(value).startsWith(prefix);
    }

    public static Boolean endsWith(String value, String suffix) {
        return requireText(value).endsWith(suffix);
    }

    public static Boolean contains(String value, String token) {
        return requireText(value).contains(token);
    }

    public static Boolean isEmpty(String value) {
        return requireText(value).isEmpty();
    }

    public static Boolean isBlank(String value) {
        return requireText(value).isBlank();
    }

    public static BigDecimal length(String value) {
        return BigDecimal.valueOf(requireText(value).length());
    }

    public static List<String> split(String value, String regex) {
        return List.of(Pattern.compile(regex).split(requireText(value), -1));
    }

    public static String join(List<?> values, String delimiter) {
        Objects.requireNonNull(values, "values");
        StringJoiner joiner = new StringJoiner(delimiter);
        for (Object value : values) {
            joiner.add(String.valueOf(value));
        }
        return joiner.toString();
    }

    private static String pad(String value, int size, String padding, boolean leftAlignedPadding) {
        if (size <= value.length()) {
            return value;
        }
        String filler = padding.repeat((int) Math.ceil((double) (size - value.length()) / padding.length()));
        filler = filler.substring(0, size - value.length());
        return leftAlignedPadding ? filler + value : value + filler;
    }

    private static String requirePadding(String padding) {
        if (padding.isEmpty()) {
            throw new IllegalArgumentException("padding must not be empty");
        }
        return padding;
    }

    private static String requireText(String value) {
        return Objects.requireNonNull(value, "value");
    }

    private static int toInt(BigDecimal value, String argumentName) {
        try {
            return Objects.requireNonNull(value, argumentName).intValueExact();
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException(argumentName + " must be an exact integer", exception);
        }
    }
}
