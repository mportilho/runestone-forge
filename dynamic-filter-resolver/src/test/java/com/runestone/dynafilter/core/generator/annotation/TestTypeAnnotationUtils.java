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
