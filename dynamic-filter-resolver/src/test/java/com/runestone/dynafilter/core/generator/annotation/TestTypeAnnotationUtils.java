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

import com.runestone.dynafilter.core.generator.annotation.testdata.annotations.FilterByConjunctionFromAnnotation;
import com.runestone.dynafilter.core.generator.annotation.testdata.annotations.FilterByDisjunctionFromAnnotation;
import com.runestone.dynafilter.core.generator.annotation.testdata.interfaces.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;

public class TestTypeAnnotationUtils {

    @Test
    public void testNullValuedInput() {
        var annotationStatementInput = new AnnotationStatementInput(null, null);
        var annotations = TypeAnnotationUtils.findAnnotationData(annotationStatementInput);
        Assertions.assertThat(annotations).isEmpty();
    }

    @Test
    public void testWithSingleAnnotation() {
        var annotationStatementInput = new AnnotationStatementInput(null, StatusOkInterface.class.getAnnotations());
        var annotations = TypeAnnotationUtils.findAnnotationData(annotationStatementInput);
        Assertions.assertThat(annotations).hasSize(1);
        Assertions.assertThat(annotations.stream().filter(a -> a.logicOperator().isConjunction()).count()).isEqualTo(1);
        Assertions.assertThat(annotations.stream().filter(a -> a.logicOperator().isDisjunction()).count()).isEqualTo(0);
    }

    @Test
    public void testWithSingleInterface() {
        var annotationStatementInput = new AnnotationStatementInput(StatusOkInterface.class, null);
        var annotations = TypeAnnotationUtils.findAnnotationData(annotationStatementInput);
        Assertions.assertThat(annotations).hasSize(1);
        Assertions.assertThat(annotations.stream().filter(a -> a.logicOperator().isConjunction()).count()).isEqualTo(1);
        Assertions.assertThat(annotations.stream().filter(a -> a.logicOperator().isDisjunction()).count()).isEqualTo(0);
    }

    @Test
    public void testWithAnnotationAndType() {
        var annotationStatementInput = new AnnotationStatementInput(NoDeleteInterface.class, StatusOkInterface.class.getAnnotations());
        var annotations = TypeAnnotationUtils.findAnnotationData(annotationStatementInput);
        Assertions.assertThat(annotations).hasSize(2);
        Assertions.assertThat(annotations.stream().filter(a -> a.logicOperator().isConjunction()).count()).isEqualTo(2);
        Assertions.assertThat(annotations.stream().filter(a -> a.logicOperator().isDisjunction()).count()).isEqualTo(0);
    }

    @Test
    public void testWithAnnotationAndTypeCombinedAndRepeatedFilters() {
        var annotationStatementInput = new AnnotationStatementInput(CombinedAnnotations.class, StatusOkInterface.class.getAnnotations());
        var annotations = TypeAnnotationUtils.findAnnotationData(annotationStatementInput);
        Assertions.assertThat(annotations).hasSize(4);
        Assertions.assertThat(annotations.stream().filter(a -> a.logicOperator().isConjunction()).count()).isEqualTo(3);
        Assertions.assertThat(annotations.stream().filter(a -> a.logicOperator().isDisjunction()).count()).isEqualTo(1);
        Assertions.assertThat(annotations.stream()
                .mapToLong(f -> f.filters().size()).sum()).isEqualTo(5);
        Assertions.assertThat(annotations.stream().flatMap(filterAnnotationData -> filterAnnotationData.filterStatements().stream())
                .mapToLong(stmt -> stmt.filters().size()).sum()).isEqualTo(4);
    }

    @Test
    public void testWithInterfaceAndConjunctionFromAnnotation() {
        var annotationStatementInput = new AnnotationStatementInput(FilterByConjunctionFromInterface.class, null);
        var annotations = TypeAnnotationUtils.findAnnotationData(annotationStatementInput);
        Assertions.assertThat(annotations).hasSize(1);
        Assertions.assertThat(annotations.stream().filter(a -> a.logicOperator().isConjunction()).count()).isEqualTo(1);
        Assertions.assertThat(annotations.stream().filter(a -> a.logicOperator().isDisjunction()).count()).isEqualTo(0);
        Assertions.assertThat(annotations.stream()
                .mapToLong(f -> f.filters().size()).sum()).isEqualTo(2);
        Assertions.assertThat(annotations.stream().flatMap(filterAnnotationData -> filterAnnotationData.filterStatements().stream())
                .mapToLong(stmt -> stmt.filters().size()).sum()).isEqualTo(2);
    }

    @Test
    public void testWithInterfaceAndDisjunctionFromAnnotation() {
        var annotationStatementInput = new AnnotationStatementInput(FilterByDisjunctionFromInterface.class, null);
        var annotations = TypeAnnotationUtils.findAnnotationData(annotationStatementInput);
        Assertions.assertThat(annotations).hasSize(1);
        Assertions.assertThat(annotations.stream().filter(a -> a.logicOperator().isConjunction()).count()).isEqualTo(0);
        Assertions.assertThat(annotations.stream().filter(a -> a.logicOperator().isDisjunction()).count()).isEqualTo(1);
        Assertions.assertThat(annotations.stream()
                .mapToLong(f -> f.filters().size()).sum()).isEqualTo(2);
        Assertions.assertThat(annotations.stream().flatMap(filterAnnotationData -> filterAnnotationData.filterStatements().stream())
                .mapToLong(stmt -> stmt.filters().size()).sum()).isEqualTo(2);
    }

    @Test
    public void testWithAnnotationAndConjunctionFromAnnotation() {
        var annotationStatementInput = new AnnotationStatementInput(null, FilterByConjunctionFromAnnotation.class.getAnnotations());
        var annotations = TypeAnnotationUtils.findAnnotationData(annotationStatementInput);
        Assertions.assertThat(annotations).hasSize(1);
        Assertions.assertThat(annotations.stream().filter(a -> a.logicOperator().isConjunction()).count()).isEqualTo(1);
        Assertions.assertThat(annotations.stream().filter(a -> a.logicOperator().isDisjunction()).count()).isEqualTo(0);
        Assertions.assertThat(annotations.stream()
                .mapToLong(f -> f.filters().size()).sum()).isEqualTo(2);
        Assertions.assertThat(annotations.stream().flatMap(filterAnnotationData -> filterAnnotationData.filterStatements().stream())
                .mapToLong(stmt -> stmt.filters().size()).sum()).isEqualTo(2);
    }

    @Test
    public void testWithAnnotationAndDisjunctionFromAnnotation() {
        var annotationStatementInput = new AnnotationStatementInput(null, FilterByDisjunctionFromAnnotation.class.getAnnotations());
        var annotations = TypeAnnotationUtils.findAnnotationData(annotationStatementInput);
        Assertions.assertThat(annotations).hasSize(1);
        Assertions.assertThat(annotations.stream().filter(a -> a.logicOperator().isConjunction()).count()).isEqualTo(0);
        Assertions.assertThat(annotations.stream().filter(a -> a.logicOperator().isDisjunction()).count()).isEqualTo(1);
        Assertions.assertThat(annotations.stream()
                .mapToLong(f -> f.filters().size()).sum()).isEqualTo(2);
        Assertions.assertThat(annotations.stream().flatMap(filterAnnotationData -> filterAnnotationData.filterStatements().stream())
                .mapToLong(stmt -> stmt.filters().size()).sum()).isEqualTo(2);
    }

    @Test
    public void testCacheHitWithEquivalentInputs() {
        TypeAnnotationUtils.clearCaches();
        AnnotationStatementInput firstInput = new AnnotationStatementInput(CombinedAnnotations.class, StatusOkInterface.class.getAnnotations());
        AnnotationStatementInput equivalentInput = new AnnotationStatementInput(CombinedAnnotations.class, StatusOkInterface.class.getAnnotations());

        var firstFilters = TypeAnnotationUtils.findAnnotationData(firstInput);
        var secondFilters = TypeAnnotationUtils.findAnnotationData(equivalentInput);
        Assertions.assertThat(secondFilters).isSameAs(firstFilters);

        var firstRequestFilters = TypeAnnotationUtils.listAllFilterRequestData(firstInput);
        var secondRequestFilters = TypeAnnotationUtils.listAllFilterRequestData(equivalentInput);
        Assertions.assertThat(secondRequestFilters).isSameAs(firstRequestFilters);

        var firstDecorators = TypeAnnotationUtils.findFilterDecorators(firstInput);
        var secondDecorators = TypeAnnotationUtils.findFilterDecorators(equivalentInput);
        Assertions.assertThat(secondDecorators).isSameAs(firstDecorators);
    }

    @Test
    public void testAnnotationStatementInputDefensiveCopy() {
        Annotation[] originalAnnotations = StatusOkInterface.class.getAnnotations();
        AnnotationStatementInput annotationStatementInput = new AnnotationStatementInput(StatusOkInterface.class, originalAnnotations);

        originalAnnotations[0] = null;

        AnnotationStatementInput equivalentInput = new AnnotationStatementInput(StatusOkInterface.class, StatusOkInterface.class.getAnnotations());
        Assertions.assertThat(annotationStatementInput).isEqualTo(equivalentInput);
    }

}
