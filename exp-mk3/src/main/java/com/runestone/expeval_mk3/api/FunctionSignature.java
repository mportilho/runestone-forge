package com.runestone.expeval_mk3.api;

import java.util.List;
import java.util.Objects;

/**
 * Assinatura de Funcao: language-level callable identity.
 */
public record FunctionSignature(String languageName, List<ExpressionType> parameterTypes) {

    public FunctionSignature {
        languageName = validateLanguageName(languageName);
        parameterTypes = List.copyOf(Objects.requireNonNull(parameterTypes, "parameterTypes"));
        for (ExpressionType parameterType : parameterTypes) {
            Objects.requireNonNull(parameterType, "parameterType");
        }
    }

    public static FunctionSignature of(String languageName, List<ExpressionType> parameterTypes) {
        return new FunctionSignature(languageName, parameterTypes);
    }

    public int arity() {
        return parameterTypes.size();
    }

    static String validateLanguageName(String languageName) {
        Objects.requireNonNull(languageName, "languageName");
        if (languageName.isBlank()) {
            throw new IllegalArgumentException("function language name must not be blank");
        }
        return languageName;
    }
}
