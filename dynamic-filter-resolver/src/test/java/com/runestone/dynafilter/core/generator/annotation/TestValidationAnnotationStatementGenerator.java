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

}
