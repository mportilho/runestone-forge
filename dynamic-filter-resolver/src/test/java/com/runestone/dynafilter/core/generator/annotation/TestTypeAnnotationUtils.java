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

import com.runestone.dynafilter.core.generator.annotation.testdata.interfaces.CombinedAnnotations;
import com.runestone.dynafilter.core.generator.annotation.testdata.interfaces.NoDeleteInterface;
import com.runestone.dynafilter.core.generator.annotation.testdata.interfaces.StatusOkInterface;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestTypeAnnotationUtils {

    @Test
    public void testNullValuedInput() {
        var annotationStatementInput = new AnnotationStatementInput(null, null);
        var annotations = TypeAnnotationUtils.findStatementAnnotations(annotationStatementInput);
        Assertions.assertThat(annotations).isEmpty();
    }

    @Test
    public void testWithSingleAnnotation() {
        var annotationStatementInput = new AnnotationStatementInput(null, StatusOkInterface.class.getAnnotations());
        var annotations = TypeAnnotationUtils.findStatementAnnotations(annotationStatementInput);
        Assertions.assertThat(annotations).hasSize(1);
        Assertions.assertThat(annotations.stream().filter(a -> a.annotationType().equals(Conjunction.class)).count()).isEqualTo(1);
        Assertions.assertThat(annotations.stream().filter(a -> a.annotationType().equals(Disjunction.class)).count()).isEqualTo(0);
    }

    @Test
    public void testWithSingleInterface() {
        var annotationStatementInput = new AnnotationStatementInput(StatusOkInterface.class, null);
        var annotations = TypeAnnotationUtils.findStatementAnnotations(annotationStatementInput);
        Assertions.assertThat(annotations).hasSize(1);
        Assertions.assertThat(annotations.stream().filter(a -> a.annotationType().equals(Conjunction.class)).count()).isEqualTo(1);
        Assertions.assertThat(annotations.stream().filter(a -> a.annotationType().equals(Disjunction.class)).count()).isEqualTo(0);
    }

    @Test
    public void testWithAnnotationAndType() {
        var annotationStatementInput = new AnnotationStatementInput(NoDeleteInterface.class, StatusOkInterface.class.getAnnotations());
        var annotations = TypeAnnotationUtils.findStatementAnnotations(annotationStatementInput);
        Assertions.assertThat(annotations).hasSize(2);
        Assertions.assertThat(annotations.stream().filter(a -> a.annotationType().equals(Conjunction.class)).count()).isEqualTo(2);
        Assertions.assertThat(annotations.stream().filter(a -> a.annotationType().equals(Disjunction.class)).count()).isEqualTo(0);
    }

    @Test
    public void testWithAnnotationAndTypeCombinedAndRepeatedFilters() {
        var annotationStatementInput = new AnnotationStatementInput(CombinedAnnotations.class, StatusOkInterface.class.getAnnotations());
        var annotations = TypeAnnotationUtils.findStatementAnnotations(annotationStatementInput);
        Assertions.assertThat(annotations).hasSize(4);
        Assertions.assertThat(annotations.stream().filter(a -> a.annotationType().equals(Conjunction.class)).count()).isEqualTo(3);
        Assertions.assertThat(annotations.stream().filter(a -> a.annotationType().equals(Disjunction.class)).count()).isEqualTo(1);
    }

}
