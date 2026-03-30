/*
 * MIT License
 * <p>
 * Copyright (c) 2023-2023 Marcelo Silva Portilho
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.runestone.expeval2.catalog.functions;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.regex.Pattern;

/**
 * Contains a list of string-focused utility functions.
 *
 * @author Marcelo Portilho
 */
public class StringFunctions {

    private static final long REGEX_CACHE_MAX_SIZE = 128;
    private static final String SPACE = " ";
    private static final Cache<String, Pattern> REGEX_CACHE = Caffeine.newBuilder()
            .maximumSize(REGEX_CACHE_MAX_SIZE)
            .build();

    public static String toUpper(String value) {
        return requireText(value).toUpperCase(Locale.ROOT);
    }

    public static String toLower(String value) {
        return requireText(value).toLowerCase(Locale.ROOT);
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

    public static String substring(String value, int beginIndex) {
        return requireText(value).substring(beginIndex);
    }

    public static String substring(String value, int beginIndex, int endIndex) {
        return requireText(value).substring(beginIndex, endIndex);
    }

    public static String substringBefore(String value, String separator) {
        String text = requireText(value);
        String token = requireString(separator, "separator");
        if (token.isEmpty()) {
            return "";
        }
        int index = text.indexOf(token);
        return index >= 0 ? text.substring(0, index) : text;
    }

    public static String substringAfter(String value, String separator) {
        String text = requireText(value);
        String token = requireString(separator, "separator");
        if (token.isEmpty()) {
            return text;
        }
        int index = text.indexOf(token);
        return index >= 0 ? text.substring(index + token.length()) : "";
    }

    public static String substringBeforeLast(String value, String separator) {
        String text = requireText(value);
        String token = requireString(separator, "separator");
        if (token.isEmpty()) {
            return text;
        }
        int index = text.lastIndexOf(token);
        return index >= 0 ? text.substring(0, index) : text;
    }

    public static String substringAfterLast(String value, String separator) {
        String text = requireText(value);
        String token = requireString(separator, "separator");
        if (token.isEmpty()) {
            return "";
        }
        int index = text.lastIndexOf(token);
        return index >= 0 ? text.substring(index + token.length()) : "";
    }

    public static String padLeft(String value, int size) {
        return padLeft(value, size, SPACE);
    }

    public static String padLeft(String value, int size, String padding) {
        return pad(requireText(value), size, requirePadding(padding), true);
    }

    public static String padRight(String value, int size) {
        return padRight(value, size, SPACE);
    }

    public static String padRight(String value, int size, String padding) {
        return pad(requireText(value), size, requirePadding(padding), false);
    }

    public static String repeat(String value, int times) {
        return requireText(value).repeat(times);
    }

    public static String replace(String value, String target, String replacement) {
        return requireText(value).replace(requireString(target, "target"), requireString(replacement, "replacement"));
    }

    public static String replaceFirst(String value, String target, String replacement) {
        String text = requireText(value);
        String token = requireString(target, "target");
        String replacementText = requireString(replacement, "replacement");
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
        String text = requireText(value);
        String regexText = requireString(regex, "regex");
        String replacementText = requireString(replacement, "replacement");
        return compiledPattern(regexText).matcher(text).replaceAll(replacementText);
    }

    public static Integer indexOf(String value, String token) {
        return requireText(value).indexOf(requireString(token, "token"));
    }

    public static Integer lastIndexOf(String value, String token) {
        return requireText(value).lastIndexOf(requireString(token, "token"));
    }

    public static Boolean startsWith(String value, String prefix) {
        return requireText(value).startsWith(requireString(prefix, "prefix"));
    }

    public static Boolean endsWith(String value, String suffix) {
        return requireText(value).endsWith(requireString(suffix, "suffix"));
    }

    public static Boolean contains(String value, String token) {
        return requireText(value).contains(requireString(token, "token"));
    }

    public static Boolean isEmpty(String value) {
        return requireText(value).isEmpty();
    }

    public static Boolean isBlank(String value) {
        return requireText(value).isBlank();
    }

    public static Integer length(String value) {
        return requireText(value).length();
    }

    public static List<String> split(String value, String regex) {
        return Arrays.asList(compiledPattern(requireString(regex, "regex")).split(requireText(value), -1));
    }

    public static String join(List<?> values, String delimiter) {
        Objects.requireNonNull(values, "values must not be null");
        String effectiveDelimiter = requireString(delimiter, "delimiter");

        StringJoiner joiner = new StringJoiner(effectiveDelimiter);
        for (Object value : values) {
            joiner.add(String.valueOf(value));
        }
        return joiner.toString();
    }

    private static String pad(String value, int size, String padding, boolean leftAlignedPadding) {
        if (size <= value.length()) {
            return value;
        }
        String filler = repeatToLength(padding, size - value.length());
        return leftAlignedPadding ? filler + value : value + filler;
    }

    private static String repeatToLength(String padding, int size) {
        StringBuilder builder = new StringBuilder(size);
        while (builder.length() < size) {
            builder.append(padding);
        }
        if (builder.length() > size) {
            builder.setLength(size);
        }
        return builder.toString();
    }

    private static String requireText(String value) {
        return requireString(value, "value");
    }

    private static String requirePadding(String padding) {
        String value = requireString(padding, "padding");
        if (value.isEmpty()) {
            throw new IllegalArgumentException("padding must not be empty");
        }
        return value;
    }

    private static Pattern compiledPattern(String regex) {
        return REGEX_CACHE.get(regex, Pattern::compile);
    }

    private static String requireString(String value, String argumentName) {
        return Objects.requireNonNull(value, argumentName + " must not be null");
    }
}
