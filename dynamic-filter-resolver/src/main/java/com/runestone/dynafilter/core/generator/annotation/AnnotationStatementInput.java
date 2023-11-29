package com.runestone.dynafilter.core.generator.annotation;

import java.lang.annotation.Annotation;

public record AnnotationStatementInput(Class<?> type, Annotation[] annotations) {
}
