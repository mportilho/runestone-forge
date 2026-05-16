package com.runestone.dynafilter.core.security;

import com.runestone.dynafilter.core.exception.DynamicFilterConfigurationException;
import com.runestone.dynafilter.core.generator.annotation.TypeAnnotationUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FilterPathPolicyTest {

    @Test
    @DisplayName("permits any path when no allowlist or denylist is configured")
    void permitsAnyPathByDefault() {
        assertThat(TypeAnnotationUtils.findFilterField(Person.class, "name", FilterPathPolicy.PERMISSIVE).getName())
                .isEqualTo("name");
    }

    @Test
    @DisplayName("allows only paths present in allowlist")
    void allowsOnlyAllowlistedPaths() {
        FilterPathPolicy policy = FilterPathPolicy.allowOnly(Set.of("name"));

        assertThat(TypeAnnotationUtils.findFilterField(Person.class, "name", policy).getName()).isEqualTo("name");
        assertThatThrownBy(() -> TypeAnnotationUtils.findFilterField(Person.class, "passwordHash", policy))
                .isInstanceOf(DynamicFilterConfigurationException.class)
                .hasMessageContaining("not allowed");
    }

    @Test
    @DisplayName("denies sensitive paths before reflection resolution")
    void deniesSensitivePaths() {
        FilterPathPolicy policy = FilterPathPolicy.deny(Set.of("passwordHash"));

        assertThatThrownBy(() -> TypeAnnotationUtils.findFilterField(Person.class, "passwordHash", policy))
                .isInstanceOf(DynamicFilterConfigurationException.class)
                .hasMessageContaining("denied");
    }

    private static class Person {
        private String name;
        private String passwordHash;
    }
}
