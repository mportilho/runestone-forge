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
