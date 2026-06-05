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

package com.runestone.dynafilter.modules.jpa.spring;

import com.runestone.converters.impl.DefaultDataConversionService;
import com.runestone.dynafilter.core.exceptions.DynamicFilterConfigurationException;
import com.runestone.dynafilter.core.generator.annotation.Conjunction;
import com.runestone.dynafilter.core.generator.annotation.Filter;
import com.runestone.dynafilter.core.generator.annotation.TypeAnnotationUtils;
import com.runestone.dynafilter.core.model.modifiers.ModIgnorePath;
import com.runestone.dynafilter.core.operation.FilterOperation;
import com.runestone.dynafilter.core.operation.FilterOperationMetadata;
import com.runestone.dynafilter.core.operation.FilterOperationService;
import com.runestone.dynafilter.core.operation.types.Decorated;
import com.runestone.dynafilter.core.operation.types.Dynamic;
import com.runestone.dynafilter.core.operation.types.Equals;
import com.runestone.dynafilter.modules.jpa.api.JpaFilterOperationContributor;
import com.runestone.dynafilter.modules.jpa.api.JpaFilterOperationService;
import com.runestone.dynafilter.modules.jpa.spring.tools.SearchState;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

public class TestFilterConfigurationAnalyserBeanPostProcessor {

    @Test
    public void testWarmupDoesNotFailForValidSpecificationInterface() {
        TypeAnnotationUtils.clearCaches();
        FilterConfigurationAnalyserBeanPostProcessor postProcessor = newPostProcessor();

        Assertions.assertThatCode(() -> postProcessor.postProcessAfterInitialization(new ValidController(), "validController"))
                .doesNotThrowAnyException();
    }

    @Test
    public void testWarmupFailsFastForInvalidSpecificationInterfaceConfiguration() {
        TypeAnnotationUtils.clearCaches();
        FilterConfigurationAnalyserBeanPostProcessor postProcessor = newPostProcessor();

        Assertions.assertThatThrownBy(() -> postProcessor.postProcessAfterInitialization(new InvalidController(), "invalidController"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No parameter configured")
                .hasMessageContaining("state");
    }

    @Test
    public void testWarmupFailsFastForUnregisteredSpecificationOperation() {
        TypeAnnotationUtils.clearCaches();
        FilterConfigurationAnalyserBeanPostProcessor postProcessor = newPostProcessor();

        Assertions.assertThatThrownBy(() -> postProcessor.postProcessAfterInitialization(new UnregisteredOperationController(), "unregisteredOperationController"))
                .isInstanceOf(DynamicFilterConfigurationException.class)
                .hasMessageContaining(CustomOperation.class.getCanonicalName())
                .hasMessageContaining("custom")
                .hasMessageContaining("not registered for JPA specifications");
    }

    @Test
    public void testWarmupAcceptsCustomOperationRegisteredByContributor() {
        TypeAnnotationUtils.clearCaches();
        JpaFilterOperationContributor contributor = registry ->
                registry.register(CustomOperation.class, FilterOperationMetadata.targetField(), context -> (root, query, builder) -> null);
        FilterConfigurationAnalyserBeanPostProcessor postProcessor = newPostProcessor(
                new JpaFilterOperationService(new DefaultDataConversionService(), List.of(contributor))
        );

        Assertions.assertThatCode(() -> postProcessor.postProcessAfterInitialization(new UnregisteredOperationController(), "registeredOperationController"))
                .doesNotThrowAnyException();
    }

    @Test
    public void testWarmupIgnoresDynamicAndDecoratedPseudoOperations() {
        TypeAnnotationUtils.clearCaches();
        FilterConfigurationAnalyserBeanPostProcessor postProcessor = newPostProcessor();

        Assertions.assertThatCode(() -> postProcessor.postProcessAfterInitialization(new PseudoOperationsController(), "pseudoOperationsController"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Warmup fails when a filter path does not exist and ModIgnorePath is absent")
    public void testWarmupFailsForMissingEntityPathWithoutIgnorePathModifier() {
        TypeAnnotationUtils.clearCaches();
        FilterConfigurationAnalyserBeanPostProcessor postProcessor = newPostProcessor();

        Assertions.assertThatThrownBy(() -> postProcessor.postProcessAfterInitialization(new MissingPathController(), "missingPathController"))
                .isInstanceOf(DynamicFilterConfigurationException.class)
                .hasMessageContaining("missingPath")
                .hasMessageContaining(FilterValidationEntity.class.getCanonicalName());
    }

    @Test
    @DisplayName("Warmup skips entity path validation when a filter uses ModIgnorePath")
    public void testWarmupSkipsMissingEntityPathWithIgnorePathModifier() {
        TypeAnnotationUtils.clearCaches();
        FilterConfigurationAnalyserBeanPostProcessor postProcessor = newPostProcessor();

        Assertions.assertThatCode(() -> postProcessor.postProcessAfterInitialization(new IgnorePathController(), "ignorePathController"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("ModIgnorePath does not skip operation registration validation")
    public void testWarmupStillValidatesOperationWithIgnorePathModifier() {
        TypeAnnotationUtils.clearCaches();
        FilterConfigurationAnalyserBeanPostProcessor postProcessor = newPostProcessor();

        Assertions.assertThatThrownBy(() -> postProcessor.postProcessAfterInitialization(new IgnorePathWithUnregisteredOperationController(), "ignorePathWithUnregisteredOperationController"))
                .isInstanceOf(DynamicFilterConfigurationException.class)
                .hasMessageContaining(CustomOperation.class.getCanonicalName())
                .hasMessageContaining("missingPath")
                .hasMessageContaining("not registered for JPA specifications");
    }

    private static FilterConfigurationAnalyserBeanPostProcessor newPostProcessor() {
        return newPostProcessor(new JpaFilterOperationService(new DefaultDataConversionService()));
    }

    private static FilterConfigurationAnalyserBeanPostProcessor newPostProcessor(FilterOperationService<Specification<?>> filterOperationService) {
        return new FilterConfigurationAnalyserBeanPostProcessor(filterOperationService);
    }

    @RestController
    private static class ValidController {
        @SuppressWarnings("unused")
        public void search(SearchState<Object> specification) {
        }
    }

    @RestController
    private static class InvalidController {
        @SuppressWarnings("unused")
        public void search(InvalidSearchState<Object> specification) {
        }
    }

    @RestController
    private static class UnregisteredOperationController {
        @SuppressWarnings("unused")
        public void search(SearchWithCustomOperation<Object> specification) {
        }
    }

    @RestController
    private static class PseudoOperationsController {
        @SuppressWarnings("unused")
        public void search(SearchWithPseudoOperations<Object> specification) {
        }
    }

    @RestController
    private static class MissingPathController {
        @SuppressWarnings("unused")
        public void search(
                @Conjunction({@Filter(path = "missingPath", parameters = "missingPath", operation = Equals.class)})
                Specification<FilterValidationEntity> specification) {
        }
    }

    @RestController
    private static class IgnorePathController {
        @SuppressWarnings("unused")
        public void search(
                @Conjunction({@Filter(path = "missingPath", parameters = "missingPath", operation = Equals.class, modifiers = ModIgnorePath.class)})
                Specification<FilterValidationEntity> specification) {
        }
    }

    @RestController
    private static class IgnorePathWithUnregisteredOperationController {
        @SuppressWarnings("unused")
        public void search(
                @Conjunction({@Filter(path = "missingPath", parameters = "missingPath", operation = CustomOperation.class, modifiers = ModIgnorePath.class)})
                Specification<FilterValidationEntity> specification) {
        }
    }

    @Conjunction({
            @Filter(path = "state", parameters = {}, operation = Equals.class),
    })
    private interface InvalidSearchState<T> extends Specification<T> {
    }

    @Conjunction({
            @Filter(path = "custom", parameters = "custom", operation = CustomOperation.class),
    })
    private interface SearchWithCustomOperation<T> extends Specification<T> {
    }

    @Conjunction({
            @Filter(path = "dynamic", parameters = "dynamic", operation = Dynamic.class),
            @Filter(path = "decorated", parameters = "decorated", operation = Decorated.class),
    })
    private interface SearchWithPseudoOperations<T> extends Specification<T> {
    }

    private interface CustomOperation<T> extends FilterOperation<T> {
    }

    private static class FilterValidationEntity {
        @SuppressWarnings("unused")
        private String existingPath;
    }
}
