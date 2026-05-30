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

import com.runestone.dynafilter.core.exceptions.DynamicFilterConfigurationException;
import com.runestone.dynafilter.core.operation.types.Equals;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestFilterOperationRegistry {

    @Test
    @DisplayName("FilterOperationRegistry exposes registered operations as an immutable map")
    public void testRegisteredOperationsAreExposedAsImmutableMap() {
        FilterOperationRegistry<String> registry = new FilterOperationRegistry<>();
        EqualsTestFilter operation = new EqualsTestFilter();

        registry.register(Equals.class, operation);

        Assertions.assertThat(registry.toMap()).containsEntry(Equals.class, operation);
        Assertions.assertThat(registry.toMetadataMap()).containsEntry(Equals.class, FilterOperationMetadata.targetField());
        Assertions.assertThatThrownBy(() -> registry.toMap().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("FilterOperationRegistry exposes custom metadata as an immutable map")
    public void testRegisteredOperationMetadataIsExposedAsImmutableMap() {
        FilterOperationRegistry<String> registry = new FilterOperationRegistry<>();

        registry.register(Equals.class, new EqualsTestFilter(), FilterOperationMetadata.booleanValue());

        Assertions.assertThat(registry.toMetadataMap()).containsEntry(Equals.class, FilterOperationMetadata.booleanValue());
        Assertions.assertThatThrownBy(() -> registry.toMetadataMap().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("FilterOperationRegistry rejects duplicate operation registrations")
    public void testDuplicateOperationRegistrationFails() {
        FilterOperationRegistry<String> registry = new FilterOperationRegistry<>();
        registry.register(Equals.class, new EqualsTestFilter());

        Assertions.assertThatThrownBy(() -> registry.register(Equals.class, new EqualsTestFilter()))
                .isInstanceOf(DynamicFilterConfigurationException.class)
                .hasMessageContaining(Equals.class.getCanonicalName())
                .hasMessageContaining("already registered");
    }

    @Test
    @DisplayName("FilterOperationRegistry rejects null operation type")
    public void testNullOperationTypeFails() {
        FilterOperationRegistry<String> registry = new FilterOperationRegistry<>();

        Assertions.assertThatThrownBy(() -> registry.register(null, new EqualsTestFilter()))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("operationType cannot be null");
    }

    @Test
    @DisplayName("FilterOperationRegistry rejects null operation implementation")
    public void testNullOperationImplementationFails() {
        FilterOperationRegistry<String> registry = new FilterOperationRegistry<>();

        Assertions.assertThatThrownBy(() -> registry.register(Equals.class, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("operation cannot be null");
    }

    @Test
    @DisplayName("FilterOperationRegistry rejects null operation metadata")
    public void testNullOperationMetadataFails() {
        FilterOperationRegistry<String> registry = new FilterOperationRegistry<>();

        Assertions.assertThatThrownBy(() -> registry.register(Equals.class, new EqualsTestFilter(), null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("operationMetadata cannot be null");
    }

}
