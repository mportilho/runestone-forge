package com.runestone.dynafilter.core.generator.annotation;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Objects;

public final class AnnotationStatementInput {

    private final Class<?> type;
    private final Annotation[] annotations;
    private final int cachedHashCode;

    public AnnotationStatementInput(Class<?> type, Annotation[] annotations) {
        this.type = type;
        this.annotations = annotations == null ? new Annotation[0] : annotations.clone();
        this.cachedHashCode = Objects.hash(type, Arrays.hashCode(this.annotations));
    }

    public Class<?> type() {
        return type;
    }

    public Annotation[] annotations() {
        return annotations.clone();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof AnnotationStatementInput that)) {
            return false;
        }
        return Objects.equals(type, that.type) && Arrays.equals(annotations, that.annotations);
    }

    @Override
    public int hashCode() {
        return cachedHashCode;
    }
}
