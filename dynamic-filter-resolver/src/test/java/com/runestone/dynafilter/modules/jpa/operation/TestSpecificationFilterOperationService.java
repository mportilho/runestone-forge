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

package com.runestone.dynafilter.modules.jpa.operation;

import com.runestone.converters.impl.DefaultDataConversionService;
import com.runestone.dynafilter.core.exceptions.DynamicFilterConfigurationException;
import com.runestone.dynafilter.core.exceptions.FilterOperationNotDefinedException;
import com.runestone.dynafilter.core.model.FilterData;
import com.runestone.dynafilter.core.operation.FilterOperation;
import com.runestone.dynafilter.core.operation.types.*;
import com.runestone.dynafilter.modules.jpa.operation.specification.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class TestSpecificationFilterOperationService {

    @Test
    @DisplayName("SpecificationFilterOperationService resolves every built-in operation currently registered")
    public void testBuiltInOperationRegistrations() {
        SpecificationFilterOperationService service = new SpecificationFilterOperationService(new DefaultDataConversionService());

        List<RegisteredOperation> operations = List.of(
                new RegisteredOperation(Between.class, new String[]{"from", "to"}, new Object[]{"a", "z"}, SpecificationBetween.class),
                new RegisteredOperation(EndsWith.class, new String[]{"value"}, new Object[]{"value"}, SpecificationEndsWith.class),
                new RegisteredOperation(Equals.class, new String[]{"value"}, new Object[]{"value"}, SpecificationEquals.class),
                new RegisteredOperation(Greater.class, new String[]{"value"}, new Object[]{"value"}, SpecificationGreater.class),
                new RegisteredOperation(GreaterOrEquals.class, new String[]{"value"}, new Object[]{"value"}, SpecificationGreaterOrEquals.class),
                new RegisteredOperation(IsIn.class, new String[]{"values"}, new Object[]{new Object[]{"a", "b"}}, SpecificationIsIn.class),
                new RegisteredOperation(IsNull.class, new String[]{"value"}, new Object[]{true}, SpecificationIsNull.class),
                new RegisteredOperation(Less.class, new String[]{"value"}, new Object[]{"value"}, SpecificationLess.class),
                new RegisteredOperation(LessOrEquals.class, new String[]{"value"}, new Object[]{"value"}, SpecificationLessOrEquals.class),
                new RegisteredOperation(Like.class, new String[]{"value"}, new Object[]{"value"}, SpecificationLike.class),
                new RegisteredOperation(StartsWith.class, new String[]{"value"}, new Object[]{"value"}, SpecificationStartsWith.class)
        );

        for (RegisteredOperation operation : operations) {
            FilterData filterData = FilterData.of("name", operation.parameters(), String.class, operation.operation(), operation.values());

            Specification<?> specification = service.createFilter(filterData);

            Assertions.assertThat(specification).isInstanceOf(operation.specificationType());
        }
    }

    @Test
    @DisplayName("SpecificationFilterOperationService rejects pseudo operations that are not registered directly")
    public void testUnregisteredOperationFails() {
        SpecificationFilterOperationService service = new SpecificationFilterOperationService(new DefaultDataConversionService());
        FilterData filterData = FilterData.of("name", new String[]{"name"}, String.class, Dynamic.class, new Object[]{"John"});

        Assertions.assertThatThrownBy(() -> service.createFilter(filterData))
                .isInstanceOf(FilterOperationNotDefinedException.class)
                .hasMessageContaining("Dynamic")
                .hasMessageContaining(SpecificationFilterOperationService.class.getCanonicalName());
    }

    @Test
    @DisplayName("SpecificationFilterOperationService reports support for built-in operations only")
    public void testSupportsRegisteredOperations() {
        SpecificationFilterOperationService service = new SpecificationFilterOperationService(new DefaultDataConversionService());

        Assertions.assertThat(service.supports(Equals.class)).isTrue();
        Assertions.assertThat(service.supports(Dynamic.class)).isFalse();
    }

    @Test
    @DisplayName("SpecificationFilterOperationService resolves operations registered by contributors")
    public void testContributorRegistersCustomOperation() {
        Specification<?> customSpecification = (root, query, builder) -> null;
        SpecificationFilterOperationContributor contributor = registry ->
                registry.register(CustomOperation.class, filterData -> customSpecification);
        SpecificationFilterOperationService service = new SpecificationFilterOperationService(
                new DefaultDataConversionService(),
                List.of(contributor)
        );
        FilterData filterData = FilterData.of("name", new String[]{"name"}, String.class, CustomOperation.class, new Object[]{"John"});

        Specification<?> specification = service.createFilter(filterData);

        Assertions.assertThat(specification).isSameAs(customSpecification);
        Assertions.assertThat(service.supports(CustomOperation.class)).isTrue();
    }

    @Test
    @DisplayName("SpecificationFilterOperationService rejects custom operations without a contributor")
    public void testCustomOperationWithoutContributorFails() {
        SpecificationFilterOperationService service = new SpecificationFilterOperationService(new DefaultDataConversionService());
        FilterData filterData = FilterData.of("name", new String[]{"name"}, String.class, CustomOperation.class, new Object[]{"John"});

        Assertions.assertThatThrownBy(() -> service.createFilter(filterData))
                .isInstanceOf(FilterOperationNotDefinedException.class)
                .hasMessageContaining("CustomOperation")
                .hasMessageContaining(SpecificationFilterOperationService.class.getCanonicalName());
    }

    @Test
    @DisplayName("SpecificationFilterOperationService rejects contributors that override built-in operations")
    public void testContributorCannotOverrideBuiltInOperation() {
        SpecificationFilterOperationContributor contributor = registry ->
                registry.register(Equals.class, filterData -> (root, query, builder) -> null);

        Assertions.assertThatThrownBy(() -> new SpecificationFilterOperationService(
                        new DefaultDataConversionService(),
                        List.of(contributor)
                ))
                .isInstanceOf(DynamicFilterConfigurationException.class)
                .hasMessageContaining(Equals.class.getCanonicalName())
                .hasMessageContaining("already registered");
    }

    @Test
    @DisplayName("SpecificationFilterOperationService rejects duplicate registrations from contributors")
    public void testDuplicateContributorRegistrationFails() {
        SpecificationFilterOperationContributor firstContributor = registry ->
                registry.register(CustomOperation.class, filterData -> (root, query, builder) -> null);
        SpecificationFilterOperationContributor secondContributor = registry ->
                registry.register(CustomOperation.class, filterData -> (root, query, builder) -> null);

        Assertions.assertThatThrownBy(() -> new SpecificationFilterOperationService(
                        new DefaultDataConversionService(),
                        List.of(firstContributor, secondContributor)
                ))
                .isInstanceOf(DynamicFilterConfigurationException.class)
                .hasMessageContaining(CustomOperation.class.getCanonicalName())
                .hasMessageContaining("already registered");
    }

    private record RegisteredOperation(
            @SuppressWarnings("rawtypes") Class<? extends FilterOperation> operation,
            String[] parameters,
            Object[] values,
            Class<? extends Specification> specificationType
    ) {
    }

    private interface CustomOperation<T> extends FilterOperation<T> {
    }
}
