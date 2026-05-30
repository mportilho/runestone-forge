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

import java.util.Map;
import java.util.Objects;

public abstract class AbstractFilterOperationService<T> implements FilterOperationService<T> {

    @SuppressWarnings("rawtypes")
    private static final Map<Class<? extends FilterOperation>, FilterOperationMetadata> PSEUDO_OPERATION_METADATA = Map.of(
            Dynamic.class, FilterOperationMetadata.dynamicValue(),
            Decorated.class, FilterOperationMetadata.stringValue()
    );

    @SuppressWarnings("rawtypes")
    private final Map<Class<? extends FilterOperation>, FilterOperation<T>> operationMap;

    @SuppressWarnings("rawtypes")
    private final Map<Class<? extends FilterOperation>, FilterOperationMetadata> metadataMap;

    public AbstractFilterOperationService(FilterOperationRegistry<T> registry) {
        Objects.requireNonNull(registry, "registry cannot be null");
        this.operationMap = registry.toMap();
        this.metadataMap = registry.toMetadataMap();
    }

    @Override
    public T createFilter(FilterData filterData) {
        Objects.requireNonNull(filterData, "filterData cannot be null");
        FilterOperation<T> filterOperation = operationMap.get(filterData.operation());
        if (filterOperation == null) {
            throw new FilterOperationNotDefinedException(String.format("No filter found for operation '%s' on service %s",
                    filterData.operation().getSimpleName(), this.getClass().getCanonicalName()));
        }
        return filterOperation.createFilter(filterData);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public boolean supports(Class<? extends FilterOperation> operationType) {
        Objects.requireNonNull(operationType, "operationType cannot be null");
        return operationMap.containsKey(operationType);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public FilterOperationMetadata findMetadata(Class<? extends FilterOperation> operationType) {
        Objects.requireNonNull(operationType, "operationType cannot be null");
        FilterOperationMetadata pseudoOperationMetadata = PSEUDO_OPERATION_METADATA.get(operationType);
        if (pseudoOperationMetadata != null) {
            return pseudoOperationMetadata;
        }

        FilterOperationMetadata operationMetadata = metadataMap.get(operationType);
        if (operationMetadata == null) {
            throw new FilterOperationNotDefinedException(String.format("No filter metadata found for operation '%s' on service %s",
                    operationType.getSimpleName(), this.getClass().getCanonicalName()));
        }
        return operationMetadata;
    }

}
