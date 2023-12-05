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

import com.runestone.dynafilter.core.generator.annotation.testdata.annotations.StatusOk;
import com.runestone.dynafilter.core.generator.annotation.testdata.annotations.StatusOkNoDeleteCombined;
import com.runestone.dynafilter.core.generator.annotation.testdata.interfaces.CombinedAnnotations;
import com.runestone.dynafilter.core.generator.annotation.testdata.interfaces.NoDeleteStatusOk;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;

public class TestTypeAnnotationUtilsAnnotationExtraction {

    @Test
    public void testNullValuedInput() {
        Assertions.assertThat(TypeAnnotationUtils.extractFilterAnnotations((Class<?>) null)).isEmpty();
        Assertions.assertThat(TypeAnnotationUtils.extractFilterAnnotations((Annotation[]) null)).isEmpty();
    }

    @Test
    public void testOneAnnotation() {
        var map = TypeAnnotationUtils.extractFilterAnnotations(StatusOk.class);
        Assertions.assertThat(map).hasSize(1);
    }

    @Test
    public void testTwoAnnotationsFromAnnotation() {
        var map = TypeAnnotationUtils.extractFilterAnnotations(StatusOkNoDeleteCombined.class);
        Assertions.assertThat(map).hasSize(2);
    }

    @Test
    public void testTwoAnnotationsFromInterface() {
        var map = TypeAnnotationUtils.extractFilterAnnotations(NoDeleteStatusOk.class);
        Assertions.assertThat(map).hasSize(2);
    }

    @Test
    public void testTwoAnnotationsFromInterfaceAndAnnotationCombined() {
        var map = TypeAnnotationUtils.extractFilterAnnotations(CombinedAnnotations.class);
        Assertions.assertThat(map).hasSize(3);
    }

}
