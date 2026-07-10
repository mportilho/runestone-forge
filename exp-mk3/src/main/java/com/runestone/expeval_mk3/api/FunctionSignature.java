package com.runestone.expeval_mk3.api;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Assinatura de Funcao: language-level function identity.
 */
public record FunctionSignature(String languageName, List<ExpressionType> parameterTypes)
        implements Comparable<FunctionSignature> {

    private static final Pattern IDENTIFIER = Pattern.compile("[a-zA-Z_][a-zA-Z_0-9]*");

    public FunctionSignature {
        languageName = validateLanguageName(languageName);
        parameterTypes = ExpressionTypes.copyOf(parameterTypes, "parameterTypes");
    }

    public int arity() {
        return parameterTypes.size();
    }

    String canonical() {
        StringBuilder builder = new StringBuilder(64);
        appendCanonicalPart(builder, languageName);
        appendCanonicalPart(builder, Integer.toString(arity()));
        for (ExpressionType parameterType : parameterTypes) {
            appendCanonicalPart(builder, ExpressionTypes.canonical(parameterType));
        }
        return builder.toString();
    }

    @Override
    public int compareTo(FunctionSignature other) {
        Objects.requireNonNull(other, "other");
        int languageNameComparison = languageName.compareTo(other.languageName);
        if (languageNameComparison != 0) {
            return languageNameComparison;
        }
        int arityComparison = Integer.compare(arity(), other.arity());
        if (arityComparison != 0) {
            return arityComparison;
        }
        for (int index = 0; index < parameterTypes.size(); index++) {
            int parameterComparison = ExpressionTypes.canonical(parameterTypes.get(index))
                    .compareTo(ExpressionTypes.canonical(other.parameterTypes.get(index)));
            if (parameterComparison != 0) {
                return parameterComparison;
            }
        }
        return 0;
    }

    static String validateLanguageName(String languageName) {
        Objects.requireNonNull(languageName, "languageName");
        if (!IDENTIFIER.matcher(languageName).matches()) {
            throw new IllegalArgumentException("function language name must be a valid identifier");
        }
        return languageName;
    }

    private static void appendCanonicalPart(StringBuilder builder, String value) {
        builder.append(value.length()).append(':').append(value).append(';');
    }
}
