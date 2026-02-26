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
        this.annotations = annotations != null ? annotations.clone() : null;
        this.cachedHashCode = 31 * Objects.hashCode(type) + Arrays.hashCode(this.annotations);
    }

    public Class<?> type() {
        return type;
    }

    public Annotation[] annotations() {
        return annotations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AnnotationStatementInput that = (AnnotationStatementInput) o;
        return Objects.equals(type, that.type) && Arrays.equals(annotations, that.annotations);
    }

    @Override
    public int hashCode() {
        return cachedHashCode;
    }

    @Override
    public String toString() {
        return "AnnotationStatementInput{" +
                "type=" + type +
                ", annotations=" + Arrays.toString(annotations) +
                '}';
    }
}
