package com.runestone.dynafilter.core.generator.annotation;

import com.runestone.dynafilter.core.annotation.Conjunction;
import com.runestone.dynafilter.core.annotation.Filter;
import com.runestone.dynafilter.core.annotation.FilterTarget;
import com.runestone.dynafilter.core.exception.DynamicFilterConfigurationException;
import com.runestone.dynafilter.core.operation.Equals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TypeAnnotationUtilsTest {

    @Test
    @DisplayName("extracts metadata from type annotation")
    void extractsMetadataFromTypeAnnotation() {
        AnnotationStatementInput input = new AnnotationStatementInput(TypeContract.class, TypeContract.class.getAnnotations());

        List<FilterAnnotationData> data = TypeAnnotationUtils.findAnnotationData(input);

        assertThat(data).singleElement().satisfies(annotationData ->
                assertThat(annotationData.filters()).singleElement().satisfies(filter ->
                        assertThat(filter.parameters()).containsExactly("name")
                )
        );
        assertThat(TypeAnnotationUtils.listAllFilterRequestData(input)).hasSize(1);
    }

    @Test
    @DisplayName("extracts metadata from non java interface")
    void extractsMetadataFromInterface() {
        AnnotationStatementInput input = new AnnotationStatementInput(InterfaceContractImplementation.class, new java.lang.annotation.Annotation[0]);

        assertThat(TypeAnnotationUtils.findAnnotationData(input)).hasSize(1);
    }

    @Test
    @DisplayName("resolves simple and nested filter fields")
    void resolvesSimpleAndNestedFields() {
        Field name = TypeAnnotationUtils.findFilterField(Person.class, "name");
        Field city = TypeAnnotationUtils.findFilterField(Person.class, "addresses.city");

        assertThat(name.getName()).isEqualTo("name");
        assertThat(city.getName()).isEqualTo("city");
    }

    @Test
    @DisplayName("fails explicitly for raw collection path segment")
    void failsForRawCollectionPathSegment() {
        assertThatThrownBy(() -> TypeAnnotationUtils.findFilterField(RawCollectionHolder.class, "items.name"))
                .isInstanceOf(DynamicFilterConfigurationException.class)
                .hasMessageContaining("concrete generic type");
    }

    @Test
    @DisplayName("fails explicitly for wildcard collection path segment")
    void failsForWildcardCollectionPathSegment() {
        assertThatThrownBy(() -> TypeAnnotationUtils.findFilterField(WildcardCollectionHolder.class, "items.name"))
                .isInstanceOf(DynamicFilterConfigurationException.class)
                .hasMessageContaining("materialized");
    }

    @Test
    @DisplayName("validates paths against target class declared in conjunction")
    void validatesPathAgainstConjunctionTarget() {
        AnnotationStatementInput input = new AnnotationStatementInput(TargetedContract.class, TargetedContract.class.getAnnotations());

        assertThat(TypeAnnotationUtils.findFilterTargetClass(input)).isEqualTo(Person.class);
        assertThat(TypeAnnotationUtils.findAnnotationData(input)).hasSize(1);
    }

    @Conjunction(@Filter(path = "name", parameters = "name", operation = Equals.class))
    private static class TypeContract {
    }

    @Conjunction(@Filter(path = "name", parameters = "name", operation = Equals.class))
    private interface InterfaceContract {
    }

    private static class InterfaceContractImplementation implements InterfaceContract {
    }

    @Conjunction(target = Person.class, value = @Filter(path = "addresses.city", parameters = "city", operation = Equals.class))
    private static class TargetedContract {
    }

    @FilterTarget(Person.class)
    private static class FilterTargetContract {
    }

    private static class Person {
        private String name;
        private List<Address> addresses;
    }

    private static class Address {
        private String city;
    }

    @SuppressWarnings("rawtypes")
    private static class RawCollectionHolder {
        private List items;
    }

    private static class WildcardCollectionHolder {
        private List<?> items;
    }
}
