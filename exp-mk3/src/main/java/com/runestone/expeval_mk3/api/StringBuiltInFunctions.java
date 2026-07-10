package com.runestone.expeval_mk3.api;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.regex.Pattern;

final class StringBuiltInFunctions {

    private StringBuiltInFunctions() {
    }

    static List<FunctionDescriptor> descriptors() {
        return ReflectedFunctionImporter
                .importAll(StringBuiltInFunctions.class, FunctionPurity.FOLDABLE)
                .rename("substringFrom", "substring")
                .rename("substringBetween", "substring")
                .rename("padLeftWith", "padLeft")
                .rename("padRightWith", "padRight")
                .toList();
    }

    public static String concat(List<String> values) {
        Objects.requireNonNull(values, "values");
        StringBuilder builder = new StringBuilder();
        for (String value : values) {
            builder.append(BuiltInFunctionSupport.requireString(value, "value"));
        }
        return builder.toString();
    }

    public static String toUpper(String value) {
        return BuiltInFunctionSupport.requireText(value).toUpperCase(Locale.ROOT);
    }

    public static String toLower(String value) {
        return BuiltInFunctionSupport.requireText(value).toLowerCase(Locale.ROOT);
    }

    public static String trim(String value) {
        return BuiltInFunctionSupport.requireText(value).strip();
    }

    public static String trimLeft(String value) {
        return BuiltInFunctionSupport.requireText(value).stripLeading();
    }

    public static String trimRight(String value) {
        return BuiltInFunctionSupport.requireText(value).stripTrailing();
    }

    public static String substringFrom(String value, BigDecimal beginIndex) {
        return BuiltInFunctionSupport.requireText(value).substring(BuiltInFunctionSupport.integer(beginIndex));
    }

    public static String substringBetween(String value, BigDecimal beginIndex, BigDecimal endIndex) {
        return BuiltInFunctionSupport.requireText(value)
                .substring(BuiltInFunctionSupport.integer(beginIndex), BuiltInFunctionSupport.integer(endIndex));
    }

    public static String substringBefore(String value, String separator) {
        String text = BuiltInFunctionSupport.requireText(value);
        String token = BuiltInFunctionSupport.requireString(separator, "separator");
        if (token.isEmpty()) {
            return "";
        }
        int index = text.indexOf(token);
        return index >= 0 ? text.substring(0, index) : text;
    }

    public static String substringAfter(String value, String separator) {
        String text = BuiltInFunctionSupport.requireText(value);
        String token = BuiltInFunctionSupport.requireString(separator, "separator");
        if (token.isEmpty()) {
            return text;
        }
        int index = text.indexOf(token);
        return index >= 0 ? text.substring(index + token.length()) : "";
    }

    public static String substringBeforeLast(String value, String separator) {
        String text = BuiltInFunctionSupport.requireText(value);
        String token = BuiltInFunctionSupport.requireString(separator, "separator");
        if (token.isEmpty()) {
            return text;
        }
        int index = text.lastIndexOf(token);
        return index >= 0 ? text.substring(0, index) : text;
    }

    public static String substringAfterLast(String value, String separator) {
        String text = BuiltInFunctionSupport.requireText(value);
        String token = BuiltInFunctionSupport.requireString(separator, "separator");
        if (token.isEmpty()) {
            return "";
        }
        int index = text.lastIndexOf(token);
        return index >= 0 ? text.substring(index + token.length()) : "";
    }

    public static String padLeft(String value, BigDecimal size) {
        return padLeftWith(value, size, " ");
    }

    public static String padLeftWith(String value, BigDecimal size, String padding) {
        return BuiltInFunctionSupport.pad(
                BuiltInFunctionSupport.requireText(value),
                BuiltInFunctionSupport.integer(size),
                BuiltInFunctionSupport.requirePadding(padding),
                true);
    }

    public static String padRight(String value, BigDecimal size) {
        return padRightWith(value, size, " ");
    }

    public static String padRightWith(String value, BigDecimal size, String padding) {
        return BuiltInFunctionSupport.pad(
                BuiltInFunctionSupport.requireText(value),
                BuiltInFunctionSupport.integer(size),
                BuiltInFunctionSupport.requirePadding(padding),
                false);
    }

    public static String repeat(String value, BigDecimal times) {
        return BuiltInFunctionSupport.requireText(value).repeat(BuiltInFunctionSupport.integer(times));
    }

    public static String replace(String value, String target, String replacement) {
        return BuiltInFunctionSupport.requireText(value).replace(
                BuiltInFunctionSupport.requireString(target, "target"),
                BuiltInFunctionSupport.requireString(replacement, "replacement"));
    }

    public static String replaceFirst(String value, String target, String replacement) {
        String text = BuiltInFunctionSupport.requireText(value);
        String token = BuiltInFunctionSupport.requireString(target, "target");
        String replacementText = BuiltInFunctionSupport.requireString(replacement, "replacement");
        if (token.isEmpty()) {
            return replacementText + text;
        }
        int index = text.indexOf(token);
        if (index < 0) {
            return text;
        }
        return text.substring(0, index) + replacementText + text.substring(index + token.length());
    }

    public static String replaceAll(String value, String regex, String replacement) {
        return Pattern.compile(BuiltInFunctionSupport.requireString(regex, "regex"))
                .matcher(BuiltInFunctionSupport.requireText(value))
                .replaceAll(BuiltInFunctionSupport.requireString(replacement, "replacement"));
    }

    public static BigDecimal indexOf(String value, String token) {
        return BigDecimal.valueOf(BuiltInFunctionSupport.requireText(value)
                .indexOf(BuiltInFunctionSupport.requireString(token, "token")));
    }

    public static BigDecimal lastIndexOf(String value, String token) {
        return BigDecimal.valueOf(BuiltInFunctionSupport.requireText(value)
                .lastIndexOf(BuiltInFunctionSupport.requireString(token, "token")));
    }

    public static Boolean startsWith(String value, String prefix) {
        return BuiltInFunctionSupport.requireText(value)
                .startsWith(BuiltInFunctionSupport.requireString(prefix, "prefix"));
    }

    public static Boolean endsWith(String value, String suffix) {
        return BuiltInFunctionSupport.requireText(value)
                .endsWith(BuiltInFunctionSupport.requireString(suffix, "suffix"));
    }

    public static Boolean contains(String value, String token) {
        return BuiltInFunctionSupport.requireText(value)
                .contains(BuiltInFunctionSupport.requireString(token, "token"));
    }

    public static Boolean isEmpty(String value) {
        return BuiltInFunctionSupport.requireText(value).isEmpty();
    }

    public static Boolean isBlank(String value) {
        return BuiltInFunctionSupport.requireText(value).isBlank();
    }

    public static BigDecimal length(String value) {
        return BigDecimal.valueOf(BuiltInFunctionSupport.requireText(value).length());
    }

    public static List<String> split(String value, String regex) {
        return List.of(Pattern.compile(BuiltInFunctionSupport.requireString(regex, "regex"))
                .split(BuiltInFunctionSupport.requireText(value), -1));
    }

    public static String join(List<?> values, String delimiter) {
        Objects.requireNonNull(values, "values");
        StringJoiner joiner = new StringJoiner(BuiltInFunctionSupport.requireString(delimiter, "delimiter"));
        for (Object value : values) {
            joiner.add(String.valueOf(value));
        }
        return joiner.toString();
    }
}
