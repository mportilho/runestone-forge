package com.runestone.dynafilter.core.generator.annotation;

import com.runestone.dynafilter.core.annotation.Conjunction;
import com.runestone.dynafilter.core.annotation.Filter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;

import static org.assertj.core.api.Assertions.assertThat;

class AnnotationStatementInputTest {

    @Test
    @DisplayName("defensively copies annotation arrays and preserves stable hash")
    void defensivelyCopiesAnnotationArrays() {
        Annotation[] annotations = FilterContract.class.getAnnotations();
        AnnotationStatementInput input = new AnnotationStatementInput(FilterContract.class, annotations);
        int hashCode = input.hashCode();

        annotations[0] = null;
        Annotation[] returnedAnnotations = input.annotations();
        returnedAnnotations[0] = null;

        assertThat(input.annotations()[0]).isInstanceOf(Conjunction.class);
        assertThat(input.hashCode()).isEqualTo(hashCode);
    }

    @Test
    @DisplayName("considers equivalent annotation inputs equal")
    void considersEquivalentInputsEqual() {
        AnnotationStatementInput first = new AnnotationStatementInput(FilterContract.class, FilterContract.class.getAnnotations());
        AnnotationStatementInput second = new AnnotationStatementInput(FilterContract.class, FilterContract.class.getAnnotations());

        assertThat(first).isEqualTo(second).hasSameHashCodeAs(second);
    }

    @Conjunction(@Filter(path = "name", parameters = "name"))
    private static class FilterContract {
    }
}
