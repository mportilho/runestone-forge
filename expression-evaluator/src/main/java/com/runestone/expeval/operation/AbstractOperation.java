/*
 * MIT License
 * <p>
 * Copyright (c) 2023-2021-2023 Marcelo Silva Portilho
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

package com.runestone.expeval.operation;

import com.runestone.expeval.exceptions.ExpressionEvaluatorException;
import com.runestone.expeval.operation.values.AbstractVariableValueOperation;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Objects;
import java.util.SplittableRandom;

/**
 * Represents an operation node on the operation tree.
 *
 * @author Marcelo Portilho
 */
public abstract class AbstractOperation {

    private static final AbstractOperation[] EMPTY_ARRAY = {};
    private static final SplittableRandom RANDOM = new SplittableRandom(System.nanoTime());
    private static final int INITIAL_PARENT_CAPACITY = 4;

    private Object cache;
    private Object lastResult;
    private AbstractOperation[] cacheBlockingSemaphores;

    private AbstractOperation[] parents = EMPTY_ARRAY;
    private int parentCount;
    private Class<?> expectedType;
    private boolean applyingParenthesis;
    private int fixedHash = 0;

    private Class<?> lastResultClass;
    private boolean lastResultWasInstance;

    public AbstractOperation() {
        this.expectedType = Object.class;
    }

    protected abstract Object resolve(OperationContext context);

    protected abstract AbstractOperation createClone(CloningContext context);

    public abstract void accept(OperationVisitor<?> visitor);

    protected abstract void formatRepresentation(StringBuilder builder);

    protected abstract String getOperationToken();

    /**
     * @return signal for enabling caches
     */
    public boolean getCacheHint() {
        return true;
    }

    /**
     * Evaluates the current node or returns the current cached value when available
     *
     * @param <T>     Dynamic return type
     * @param context a holder of the evaluation process properties
     * @return the operation's evaluation result
     */
    @SuppressWarnings("unchecked")
    public final <T> T evaluate(OperationContext context) {
        Object result;
        try {
            if (isCaching()) {
                if (cache != null) {
                    result = cache;
                } else {
                    result = cache = castOperationResult(resolve(context), context);
                }
            } else {
                result = castOperationResult(resolve(context), context);
                cache = null;
            }
        } catch (NullPointerException | ExpressionEvaluatorException e) {
            throw e;
        } catch (Exception e) {
            throw new ExpressionEvaluatorException(String.format("Error computing expression [%s]", this), e);
        }
        lastResult = result;
        return (T) result;
    }

    protected Object castOperationResult(Object result, OperationContext context) {
        if (result == null) {
            if (!context.allowingNull()) {
                if (this instanceof AbstractVariableValueOperation variable) {
                    throw new ExpressionEvaluatorException(
                            String.format("Variable [%s] requires a value", variable.getVariableName()));
                } else {
                    throw new NullPointerException(String.format("Invalid null result for expression [%s] ", this));
                }
            }
            lastResultClass = null;
            lastResultWasInstance = false;
            return null;
        }

        Class<?> resultClass = result.getClass();
        if (resultClass == lastResultClass && lastResultWasInstance) {
            return result;
        }

        if (!resultClass.isArray() && !getExpectedType().isArray() && !getExpectedType().isInstance(result)) {
            Class<?> initialExpectedType = getExpectedType();
            Object convertedValue = tryConvertValue(result, initialExpectedType, context);
            if (convertedValue != null) {
                lastResultClass = convertedValue.getClass();
                lastResultWasInstance = true;
                return convertedValue;
            }

            expectedTypeByValue(result, initialExpectedType);
            Class<?> inferredExpectedType = getExpectedType();
            if (inferredExpectedType.isInstance(result)) {
                lastResultClass = resultClass;
                lastResultWasInstance = true;
                return result;
            }
            if (!inferredExpectedType.equals(initialExpectedType)) {
                convertedValue = tryConvertValue(result, inferredExpectedType, context);
                if (convertedValue != null) {
                    lastResultClass = convertedValue.getClass();
                    lastResultWasInstance = true;
                    return convertedValue;
                }
            }
            lastResultWasInstance = false;
            throw new ExpressionEvaluatorException(
                    String.format("Cannot convert [%s] to [%s]", resultClass, inferredExpectedType));
        }

        lastResultClass = resultClass;
        lastResultWasInstance = true;
        return result;
    }

    private Object tryConvertValue(Object sourceValue, Class<?> targetType, OperationContext context) {
        if (!shouldAttemptConversion(sourceValue, targetType, context)) {
            return null;
        }
        try {
            return context.conversionService().convert(sourceValue, targetType);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean shouldAttemptConversion(Object sourceValue, Class<?> targetType, OperationContext context) {
        if (targetType.isInstance(sourceValue)) {
            return true;
        }
        if (targetType.isPrimitive()) {
            return true;
        }
        if (targetType.isEnum() && (sourceValue instanceof String || sourceValue instanceof Number)) {
            return true;
        }
        return context.conversionService().canConvert(sourceValue.getClass(), targetType);
    }

    /**
     * Formats a value to be used on the string representation of the current
     * operation
     *
     * @param unformattedValue the value to be formatted
     * @return the formatted value
     */
    protected String formatValue(Object unformattedValue) {
        if (unformattedValue == null) {
            return "null";
        }
        if (unformattedValue.getClass().isArray()) {
            int arrayLength = Array.getLength(unformattedValue);
            if (arrayLength <= 4) {
                StringBuilder builder = new StringBuilder("[");
                for (int i = 0; i < arrayLength; i++) {
                    builder.append(formatByType(Array.get(unformattedValue, i)));
                    if (i < arrayLength - 1) {
                        builder.append(", ");
                    }
                }
                return builder.append("]").toString();
            } else {
                return String.format("[%s, %s .. %s, %s]", formatByType(Array.get(unformattedValue, 0)),
                        formatByType(Array.get(unformattedValue, 1)),
                        formatByType(Array.get(unformattedValue, arrayLength - 2)),
                        formatByType(Array.get(unformattedValue, arrayLength - 1)));
            }
        }
        return formatByType(unformattedValue);
    }

    private String formatByType(Object object) {
        if (object instanceof String || String.class.equals(getExpectedType())) {
            return String.format("'%s'", object);
        } else if (object instanceof ZonedDateTime dateTime) {
            return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(dateTime);
        }
        return object.toString();
    }

    /**
     * Makes a copy of the current operation, with its properties and cache value
     *
     * @param cloningContext a holder of the cloning operation temporary properties
     * @return a new copy of this object
     */
    public final AbstractOperation copy(CloningContext cloningContext) {
        AbstractOperation copy;
        if (cloningContext.getClonedOperationsMap().containsKey(this)) {
            copy = cloningContext.getClonedOperationsMap().get(this);
        } else {
            copy = createClone(cloningContext).copyAtributes(this);
            cloningContext.getClonedOperationsMap().put(this, copy);
        }
        if (this.cacheBlockingSemaphores != null && this.cacheBlockingSemaphores.length != 0) {
            copy.cacheBlockingSemaphores = new AbstractOperation[this.cacheBlockingSemaphores.length];
            for (int i = 0,
                    blockingSemaphoresLength = this.cacheBlockingSemaphores.length; i < blockingSemaphoresLength; i++) {
                copy.cacheBlockingSemaphores[i] = cloningContext.getClonedOperationsMap()
                        .get(this.cacheBlockingSemaphores[i]);
            }
        }
        return copy;
    }

    /**
     * Sets the expected type for the operation result
     *
     * @param expectedType the expected type for the operation result
     * @param <T>          Dynamic return type
     * @return the current operation
     */
    @SuppressWarnings({ "unchecked" })
    public final <T extends AbstractOperation> T expectedType(Class<?> expectedType) {
        this.expectedType = Objects.requireNonNull(expectedType);
        this.lastResultClass = null;
        this.lastResultWasInstance = false;
        return (T) this;
    }

    /**
     * Sets the expected type for the operation result based on the current value.
     * If there's no corresponding type, the expected type
     * will remain the same as before.
     *
     * @param value the current value
     */
    protected final void expectedTypeByValue(Object value, Class<?> defaultType) {
        if (!this.expectedType.isInstance(value)) {
            if (value instanceof Number) {
                expectedType(BigDecimal.class);
            } else if (value instanceof Boolean) {
                expectedType(Boolean.class);
            } else if (value instanceof String) {
                expectedType(String.class);
            } else if (value instanceof LocalDate) {
                expectedType(LocalDate.class);
            } else if (value instanceof LocalTime) {
                expectedType(LocalTime.class);
            } else if (value instanceof LocalDateTime || value instanceof ZonedDateTime) {
                expectedType(ZonedDateTime.class);
            } else {
                expectedType(defaultType);
            }
            this.lastResultClass = null;
            this.lastResultWasInstance = false;
        }
    }

    private AbstractOperation copyAtributes(AbstractOperation sourceOperation) {
        this.cache = sourceOperation.cache;
        this.applyingParenthesis = sourceOperation.applyingParenthesis;
        this.expectedType = sourceOperation.expectedType;
        this.lastResultClass = null;
        this.lastResultWasInstance = false;
        return this;
    }

    /**
     * Indicates the string representation must be wrapped by parenthesis
     */
    public void applyingParenthesis() {
        this.applyingParenthesis = true;
    }

    /**
     * Adds a parent operation node
     *
     * @param operation the parent of this operation
     */
    public void addParent(AbstractOperation operation) {
        if (operation == null) {
            return;
        }
        int currentCount = parentCount;
        if (currentCount == parents.length) {
            int newLength = currentCount == 0 ? INITIAL_PARENT_CAPACITY : currentCount + (currentCount >> 1);
            parents = Arrays.copyOf(parents, newLength);
        }
        parents[currentCount] = operation;
        parentCount = currentCount + 1;
    }

    /**
     * Configures the current operation to cache its result. It affects the current
     * operation and all of its parents
     *
     * @param enable <code>true</code> to enable caching for this operation or
     *               <code>false</code> to disable it
     */
    public void configureCaching(boolean enable) {
        if (enable && !isCaching()) {
            enableCaching(this, this);
        } else if (!enable) {
            disableCaching(this, this);
        }
    }

    private void enableCaching(AbstractOperation semaphore, AbstractOperation operation) {
        if (operation.cacheBlockingSemaphores != null && operation.cacheBlockingSemaphores.length > 0) {
            if (operation.cacheBlockingSemaphores.length == 1 && operation.cacheBlockingSemaphores[0] == semaphore) {
                operation.cacheBlockingSemaphores = EMPTY_ARRAY;
                for (int i = 0; i < operation.parentCount; i++) {
                    enableCaching(semaphore, operation.parents[i]);
                }
            } else if (operation.cacheBlockingSemaphores.length > 1) {
                int index = findSemaphoreIndex(semaphore, operation);
                if (index != -1) {
                    AbstractOperation[] temp = new AbstractOperation[operation.cacheBlockingSemaphores.length - 1];
                    System.arraycopy(operation.cacheBlockingSemaphores, 0, temp, 0, index);
                    System.arraycopy(operation.cacheBlockingSemaphores, index + 1, temp, index,
                            operation.cacheBlockingSemaphores.length - index - 1);
                    operation.cacheBlockingSemaphores = temp;
                    for (int i = 0; i < operation.parentCount; i++) {
                        enableCaching(semaphore, operation.parents[i]);
                    }
                }
            }
        }
    }

    private void disableCaching(AbstractOperation semaphore, AbstractOperation operation) {
        operation.cache = null;
        if (operation.cacheBlockingSemaphores == null) {
            operation.cacheBlockingSemaphores = new AbstractOperation[1];
            operation.cacheBlockingSemaphores[0] = semaphore;
            for (int i = 0; i < operation.parentCount; i++) {
                disableCaching(semaphore, operation.parents[i]);
            }
        } else {
            if (findSemaphoreIndex(semaphore, operation) == -1) {
                AbstractOperation[] temp = new AbstractOperation[operation.cacheBlockingSemaphores.length + 1];
                System.arraycopy(operation.cacheBlockingSemaphores, 0, temp, 0,
                        operation.cacheBlockingSemaphores.length);
                temp[temp.length - 1] = semaphore;
                operation.cacheBlockingSemaphores = temp;
                for (int i = 0; i < operation.parentCount; i++) {
                    disableCaching(semaphore, operation.parents[i]);
                }
            }
        }
    }

    private int findSemaphoreIndex(AbstractOperation semaphore, AbstractOperation operation) {
        for (int i = 0, cacheLength = operation.cacheBlockingSemaphores.length; i < cacheLength; i++) {
            if (operation.cacheBlockingSemaphores[i] == semaphore) {
                return i;
            }
        }
        return -1;
    }

    /**
     * @return <code>true</code> if the current operation is caching its result
     */
    protected boolean isCaching() {
        return cacheBlockingSemaphores == null || cacheBlockingSemaphores.length == 0;
    }

    /**
     * Clears the current cache for this operation and all its parents
     */
    public void clearCache() {
        clearCache(this);
    }

    private void clearCache(AbstractOperation operation) {
        operation.cache = null;
        for (int i = 0; i < operation.parentCount; i++) {
            AbstractOperation parent = operation.parents[i];
            if (parent.cache != null) {
                clearCache(parent);
            }
        }
    }

    /**
     * Retrieves the current cached value for this operation
     *
     * @return The current cache for this operation. Returns <code>null</code> if
     *         the operation was not evaluated or cache is disabled
     */
    public Object getCache() {
        return cache;
    }

    /**
     * @return the expected type for the operation result
     */
    public Class<?> getExpectedType() {
        return expectedType;
    }

    /**
     * @return the last result of the operation
     */
    public Object getCurrentResult() {
        return lastResult != null ? lastResult : cache;
    }

    @Override
    public int hashCode() {
        if (fixedHash == 0) {
            fixedHash = RANDOM.nextInt();
        }
        return fixedHash;
    }

    /**
     * Builds a String representation of the current operation
     *
     * @param builder the {@link StringBuilder} object where the representation will
     *                be placed
     */
    public final void toString(StringBuilder builder) {
        if (applyingParenthesis) {
            builder.append('(');
            formatRepresentation(builder);
            builder.append(')');
        } else {
            formatRepresentation(builder);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        toString(builder);
        return builder.toString();
    }

}
