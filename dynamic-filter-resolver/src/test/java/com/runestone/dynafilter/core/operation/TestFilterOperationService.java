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

package com.runestone.dynafilter.core.operation;

import com.runestone.dynafilter.core.exceptions.FilterOperationNotDefinedException;
import com.runestone.dynafilter.core.model.FilterData;
import com.runestone.dynafilter.core.operation.types.Decorated;
import com.runestone.dynafilter.core.operation.types.Dynamic;
import com.runestone.dynafilter.core.operation.types.Equals;
import com.runestone.dynafilter.core.operation.types.Like;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestFilterOperationService {

    @Test
    @DisplayName("FilterOperationService creates a filter with a registered operation")
    public void testFilterOperationService() {
        var stringFilterOperationService = new StringFilterOperationService();
        var filterData = FilterData.of("name", new String[]{"clientName"}, String.class, Equals.class, new Object[]{"Joe"});
        String filter = stringFilterOperationService.createFilter(filterData);
        Assertions.assertThat(filter).isEqualTo("=Joe");
    }

    @Test
    @DisplayName("FilterOperationService rejects an unregistered operation")
    public void testNoFilterOperationFoundOnService() {
        var stringFilterOperationService = new StringFilterOperationService();
        var filterData = FilterData.of("name", new String[]{"clientName"}, String.class, Like.class, new Object[][]{{"Joe"}});
        Assertions.assertThatThrownBy(() -> stringFilterOperationService.createFilter(filterData)).isInstanceOf(FilterOperationNotDefinedException.class);
    }

    @Test
    @DisplayName("FilterOperationService reports support for registered operations")
    public void testSupportsRegisteredOperation() {
        var stringFilterOperationService = new StringFilterOperationService();

        Assertions.assertThat(stringFilterOperationService.supports(Equals.class)).isTrue();
        Assertions.assertThat(stringFilterOperationService.supports(Like.class)).isFalse();
    }

    @Test
    @DisplayName("FilterOperationService rejects null operation support checks")
    public void testSupportsNullOperationFails() {
        var stringFilterOperationService = new StringFilterOperationService();

        Assertions.assertThatThrownBy(() -> stringFilterOperationService.supports(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("operationType cannot be null");
    }

    @Test
    @DisplayName("FilterOperationService exposes metadata registered through a registry")
    public void testFindMetadataForRegistryBackedService() {
        FilterOperationRegistry<String> registry = new FilterOperationRegistry<>();
        registry.register(Equals.class, new EqualsTestFilter(), FilterOperationMetadata.booleanValue());
        var stringFilterOperationService = new RegistryBackedStringFilterOperationService(registry);

        Assertions.assertThat(stringFilterOperationService.findMetadata(Equals.class))
                .isEqualTo(FilterOperationMetadata.booleanValue());
    }

    @Test
    @DisplayName("FilterOperationService exposes metadata for pseudo operations")
    public void testFindMetadataForPseudoOperations() {
        var stringFilterOperationService = new StringFilterOperationService();

        Assertions.assertThat(stringFilterOperationService.findMetadata(Dynamic.class))
                .isEqualTo(FilterOperationMetadata.dynamicValue());
        Assertions.assertThat(stringFilterOperationService.findMetadata(Decorated.class))
                .isEqualTo(FilterOperationMetadata.stringValue());
    }

    @Test
    @DisplayName("FilterOperationService rejects metadata lookups for unregistered operations")
    public void testFindMetadataForUnregisteredOperationFails() {
        var stringFilterOperationService = new StringFilterOperationService();

        Assertions.assertThatThrownBy(() -> stringFilterOperationService.findMetadata(Like.class))
                .isInstanceOf(FilterOperationNotDefinedException.class)
                .hasMessageContaining("No filter metadata found")
                .hasMessageContaining("Like")
                .hasMessageContaining(StringFilterOperationService.class.getCanonicalName());
    }

    @Test
    @DisplayName("FilterOperationService rejects null metadata lookups")
    public void testFindMetadataNullOperationFails() {
        var stringFilterOperationService = new StringFilterOperationService();

        Assertions.assertThatThrownBy(() -> stringFilterOperationService.findMetadata(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("operationType cannot be null");
    }

    private static final class RegistryBackedStringFilterOperationService extends AbstractFilterOperationService<String> {

        private RegistryBackedStringFilterOperationService(FilterOperationRegistry<String> registry) {
            super(registry);
        }
    }

}
