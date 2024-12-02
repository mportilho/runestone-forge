package com.runestone.dynafilter.core.generator.annotation;

import com.runestone.dynafilter.core.exceptions.DynamicFilterConfigurationException;
import com.runestone.dynafilter.core.generator.ConditionalStatement;
import com.runestone.dynafilter.core.operation.types.Like;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

public class TestTypeAnnotationUtilsFindEntityClass {

    @Test
    public void testInternalConfig() throws NoSuchMethodException {
        TypeAnnotationUtils.findFilterTargetClass(InternalConfigClass.class.getMethod("testConjunction", Specification.class).getParameters()[0]);
        TypeAnnotationUtils.findFilterTargetClass(InternalConfigClass.class.getMethod("testDisjunction", Specification.class).getParameters()[0]);
        TypeAnnotationUtils.findFilterTargetClass(InternalConfigClass.class.getMethod("testConjunctionWithConditionalStatement", ConditionalStatement.class).getParameters()[0]);
        TypeAnnotationUtils.findFilterTargetClass(InternalConfigClass.class.getMethod("testDisjunctionWithConditionalStatement", ConditionalStatement.class).getParameters()[0]);
    }

    @Test
    public void testInternalConfigNoFilterClass() throws NoSuchMethodException {
        Assertions
                .assertThatThrownBy(() -> TypeAnnotationUtils.findFilterTargetClass(InternalConfigClass.class.getMethod("testConjunctionWithConditionalStatementNoFilterClass", ConditionalStatement.class).getParameters()[0]))
                .isInstanceOf(DynamicFilterConfigurationException.class);

        Assertions
                .assertThatThrownBy(() -> TypeAnnotationUtils.findFilterTargetClass(InternalConfigClass.class.getMethod("testDisjunctionWithConditionalStatementNoFilterClass", ConditionalStatement.class).getParameters()[0]))
                .isInstanceOf(DynamicFilterConfigurationException.class);
    }

    @Test
    public void testExternalConfig() throws NoSuchMethodException {
        TypeAnnotationUtils.findFilterTargetClass(ExternalConfigClass.class.getMethod("testConjunctionFrom", ConditionalStatement.class).getParameters()[0]);
        TypeAnnotationUtils.findFilterTargetClass(ExternalConfigClass.class.getMethod("testDisjunctionFrom", ConditionalStatement.class).getParameters()[0]);
    }

    @Test
    public void testExternalConfigNoFilterClass() throws NoSuchMethodException {
        Assertions
                .assertThatThrownBy(() -> TypeAnnotationUtils.findFilterTargetClass(ExternalConfigClass.class.getMethod("testConjunctionFromNoFilterClass", ConditionalStatement.class).getParameters()[0]))
                .isInstanceOf(DynamicFilterConfigurationException.class);

        Assertions
                .assertThatThrownBy(() -> TypeAnnotationUtils.findFilterTargetClass(ExternalConfigClass.class.getMethod("testDisjunctionFromNoFilterClass", ConditionalStatement.class).getParameters()[0]))
                .isInstanceOf(DynamicFilterConfigurationException.class);
    }

    static class InternalConfigClass {
        public void testConjunction(@Conjunction({@Filter(path = "name", parameters = "name", operation = Like.class)}) Specification<Entity> specification) {
        }

        public void testDisjunction(@Disjunction({@Filter(path = "name", parameters = "name", operation = Like.class)}) Specification<Entity> specification) {
        }

        public void testConjunctionWithConditionalStatement(
                @FilterTarget(Entity.class)
                @Conjunction({@Filter(path = "name", parameters = "name", operation = Like.class)}) ConditionalStatement conditionalStatement) {
        }

        public void testDisjunctionWithConditionalStatement(
                @FilterTarget(Entity.class)
                @Disjunction({@Filter(path = "name", parameters = "name", operation = Like.class)}) ConditionalStatement conditionalStatement) {
        }

        public void testConjunctionWithConditionalStatementNoFilterClass(@Conjunction({@Filter(path = "name", parameters = "name", operation = Like.class)}) ConditionalStatement conditionalStatement) {
        }

        public void testDisjunctionWithConditionalStatementNoFilterClass(@Disjunction({@Filter(path = "name", parameters = "name", operation = Like.class)}) ConditionalStatement conditionalStatement) {
        }
    }

    static class ExternalConfigClass {
        public void testConjunctionFrom(@ConjunctionFrom(GetEntity.class) ConditionalStatement conditionalStatement) {
        }

        public void testConjunctionFromNoFilterClass(@ConjunctionFrom(GetEntityNoFilterClass.class) ConditionalStatement conditionalStatement) {
        }

        public void testDisjunctionFrom(@DisjunctionFrom(GetEntity.class) ConditionalStatement conditionalStatement) {
        }

        public void testDisjunctionFromNoFilterClass(@DisjunctionFrom(GetEntityNoFilterClass.class) ConditionalStatement conditionalStatement) {
        }
    }

    @FilterTarget(Entity.class)
    record GetEntity(
            @Filter(path = "name", parameters = "name", operation = Like.class) String name
    ) {
    }

    record GetEntityNoFilterClass(
            @Filter(path = "name", parameters = "name", operation = Like.class) String name
    ) {
    }

    record Entity(
            String name
    ) {
    }


}
