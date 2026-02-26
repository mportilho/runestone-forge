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

import com.runestone.dynafilter.core.generator.annotation.testvalidation.BlankParameter;
import com.runestone.dynafilter.core.generator.annotation.testvalidation.ConstantAndParameterWithDiffLength;
import com.runestone.dynafilter.core.generator.annotation.testvalidation.DefaultAndParameterWithDiffLength;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestValidationAnnotationStatementGenerator {

    @Test
    public void testThrowOnUnfulfilledRequiredParam() {
        var annotationStatementInput = new AnnotationStatementInput(BlankParameter.class, null);
        AnnotationStatementGenerator generator = new AnnotationStatementGenerator();
        Assertions.assertThatThrownBy(() -> generator.generateStatements(annotationStatementInput, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No parameter configured")
                .hasMessageContaining("genre");
    }

    @Test
    public void testThrowOnConstantAndParamsWithDiffLength() {
        var annotationStatementInput = new AnnotationStatementInput(ConstantAndParameterWithDiffLength.class, null);
        AnnotationStatementGenerator generator = new AnnotationStatementGenerator();
        Assertions.assertThatThrownBy(() -> generator.generateStatements(annotationStatementInput, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Parameters and constant values have different sizes")
                .hasMessageContaining("first, second");
    }

    @Test
    public void testThrowOnDefaultAndParamsWithDiffLength() {
        var annotationStatementInput = new AnnotationStatementInput(DefaultAndParameterWithDiffLength.class, null);
        AnnotationStatementGenerator generator = new AnnotationStatementGenerator();
        Assertions.assertThatThrownBy(() -> generator.generateStatements(annotationStatementInput, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Parameters and default values have different sizes")
                .hasMessageContaining("first, second");
    }

    @Test
    public void testFailFastDuringMetadataWarmup() {
        TypeAnnotationUtils.clearCaches();
        var annotationStatementInput = new AnnotationStatementInput(BlankParameter.class, null);

        Assertions.assertThatThrownBy(() -> TypeAnnotationUtils.listAllFilterRequestData(annotationStatementInput))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No parameter configured")
                .hasMessageContaining("genre");
    }

}
