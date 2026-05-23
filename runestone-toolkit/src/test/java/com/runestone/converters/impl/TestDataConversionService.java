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

package com.runestone.converters.impl;

import com.runestone.converters.DataConversionService;
import com.runestone.converters.NoDataConverterFoundException;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class TestDataConversionService {

    public enum Status {
        ACTIVE,
        INACTIVE
    }

    @Test
    public void testDefaultDataConversionService() {
        DataConversionService service = new DefaultDataConversionService();
        Assertions.assertThat(service.canConvert(Integer.class, BigDecimal.class)).isTrue();
        Assertions.assertThat(service.convert("123", Integer.class)).isEqualTo(Integer.valueOf("123"));
        Assertions.assertThat(service.convert("123", int.class)).isEqualTo(123);
        Assertions.assertThat(service.convert(123, BigDecimal.class)).isEqualByComparingTo("123");
        Assertions.assertThat(service.convert(123, String.class)).isEqualTo("123");
        Assertions.assertThat(service.convert("20200102", LocalDate.class)).isEqualTo(LocalDate.of(2020, 1, 2));
    }

    @Test
    public void testNumericWrapperConversions() {
        DataConversionService service = new DefaultDataConversionService();

        Assertions.assertThat(service.convert(new BigDecimal("12.9"), Integer.class)).isEqualTo(12);
        Assertions.assertThat(service.convert(new BigDecimal("12.9"), Long.class)).isEqualTo(12L);
        Assertions.assertThat(service.convert(new BigDecimal("12.9"), Double.class)).isEqualTo(12.9d);
    }

    @Test
    public void testAssignableTypeShortCircuit() {
        DataConversionService service = new DefaultDataConversionService();

        Integer source = 123;
        Number converted = service.convert(source, Number.class);

        Assertions.assertThat(converted).isSameAs(source);
    }

    @Test
    public void testNullSourceAndTargetContracts() {
        DataConversionService service = new DefaultDataConversionService();

        Assertions.assertThat(service.convert(null, Integer.class)).isNull();
        Assertions.assertThat(service.canConvert(null, Integer.class)).isFalse();
        Assertions.assertThat(service.canConvert(String.class, null)).isFalse();
        Assertions.assertThatThrownBy(() -> service.convert("1", null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Target Type must be provided");
    }

    @Test
    public void testPrimitiveCanConvertOnlyForSupportedSources() {
        DataConversionService service = new DefaultDataConversionService();

        Assertions.assertThat(service.canConvert(Object.class, int.class)).isFalse();
        Assertions.assertThat(service.canConvert(Integer.class, int.class)).isTrue();
        Assertions.assertThat(service.canConvert(String.class, int.class)).isTrue();
        Assertions.assertThat(service.canConvert(Boolean.class, boolean.class)).isTrue();
        Assertions.assertThat(service.canConvert(Character.class, char.class)).isTrue();
        Assertions.assertThat(service.canConvert(Integer.class, char.class)).isFalse();

        Assertions.assertThat(service.convert('a', char.class)).isEqualTo('a');
        Assertions.assertThatThrownBy(() -> service.convert(new Object(), int.class))
                .isInstanceOf(NoDataConverterFoundException.class);
    }

    @Test
    public void testListToPrimitiveArrayConversion() {
        DataConversionService service = new DefaultDataConversionService();

        Assertions.assertThat(service.canConvert(List.class, int[].class)).isTrue();
        Assertions.assertThat(service.convert(List.of(new BigDecimal("1"), new BigDecimal("2"), new BigDecimal("3")), int[].class))
                .containsExactly(1, 2, 3);
        Assertions.assertThat(service.convert(List.of(new BigDecimal("1"), new BigDecimal("2"), new BigDecimal("3")), long[].class))
                .containsExactly(1L, 2L, 3L);
        Assertions.assertThat(service.convert(List.of(new BigDecimal("1"), new BigDecimal("2"), new BigDecimal("3")), double[].class))
                .containsExactly(1.0d, 2.0d, 3.0d);
        Assertions.assertThat(service.convert(List.of("true", "false", "1"), boolean[].class))
                .containsExactly(true, false, true);
        Assertions.assertThat(service.convert(List.of("a", "b", "c"), char[].class))
                .containsExactly('a', 'b', 'c');
        Assertions.assertThat(service.convert(List.of("1.5", "2.5"), float[].class))
                .containsExactly(1.5f, 2.5f);
        Assertions.assertThat(service.convert(List.of("1", "2"), short[].class))
                .containsExactly((short) 1, (short) 2);
        Assertions.assertThat(service.convert(List.of("1", "2"), byte[].class))
                .containsExactly((byte) 1, (byte) 2);
    }

    @Test
    public void testNonRandomAccessCollectionToPrimitiveArrayConversion() {
        DataConversionService service = new DefaultDataConversionService();

        LinkedHashSet<String> orderedValues = new LinkedHashSet<>(List.of("1", "2", "3"));

        Assertions.assertThat(service.convert(orderedValues, int[].class)).containsExactly(1, 2, 3);
    }

    @Test
    public void testCollectionToReferenceArrayConversion() {
        DataConversionService service = new DefaultDataConversionService();

        Assertions.assertThat(service.canConvert(LinkedHashSet.class, Integer[].class)).isTrue();

        LinkedHashSet<String> orderedValues = new LinkedHashSet<>(List.of("1", "2", "3"));
        Assertions.assertThat(service.convert(orderedValues, Integer[].class)).containsExactly(1, 2, 3);
        Assertions.assertThat(service.convert(List.of(1, 2, 3), Number[].class)).containsExactly(1, 2, 3);
        Assertions.assertThat(service.convert(java.util.Arrays.asList("1", null, "3"), Integer[].class)).containsExactly(1, null, 3);
        Assertions.assertThat(service.convert(List.of(new BigDecimal("1"), new BigDecimal("2")), BigDecimal[].class))
                .containsExactly(new BigDecimal("1"), new BigDecimal("2"));
    }

    @Test
    public void testEmptyCollectionsToArrays() {
        DataConversionService service = new DefaultDataConversionService();

        Assertions.assertThat(service.convert(List.of(), int[].class)).isEmpty();
        Assertions.assertThat(service.convert(List.of(), Integer[].class)).isEmpty();
    }

    @Test
    public void testInvalidCollectionElementsFailPredictably() {
        DataConversionService service = new DefaultDataConversionService();

        Assertions.assertThatThrownBy(() -> service.convert(Arrays.asList("1", null), int[].class))
                .isInstanceOf(NoDataConverterFoundException.class);
        Assertions.assertThatThrownBy(() -> service.convert(List.of("x"), int[].class))
                .isInstanceOf(NumberFormatException.class);
        Assertions.assertThatThrownBy(() -> service.convert(List.of("x"), Integer[].class))
                .isInstanceOf(NumberFormatException.class);
    }

    @Test
    public void testCollectionToArrayCanConvertNegativeCases() {
        DataConversionService service = new DefaultDataConversionService();

        Assertions.assertThat(service.canConvert(Object.class, int[].class)).isFalse();
        Assertions.assertThat(service.canConvert(List.class, Integer.class)).isFalse();
        Assertions.assertThat(service.canConvert(Set.class, int[].class)).isTrue();
    }

    @Test
    public void testStringToEnumConversion() {
        DataConversionService service = new DefaultDataConversionService();
        Assertions.assertThat(service.convert("ACTIVE", Status.class)).isEqualTo(Status.ACTIVE);
        Assertions.assertThat(service.convert("INACTIVE", Status.class)).isEqualTo(Status.INACTIVE);
        Assertions.assertThat(service.convert("active", Status.class)).isEqualTo(Status.ACTIVE);
        Assertions.assertThat(service.convert("InAcTiVe", Status.class)).isEqualTo(Status.INACTIVE);
    }

    @Test
    public void testOrdinalToEnumConversion() {
        DataConversionService service = new DefaultDataConversionService();
        Assertions.assertThat(service.convert(0, Status.class)).isEqualTo(Status.ACTIVE);
        Assertions.assertThat(service.convert(1, Status.class)).isEqualTo(Status.INACTIVE);

        Assertions.assertThat(service.convert(0L, Status.class)).isEqualTo(Status.ACTIVE);
        Assertions.assertThat(service.convert(0D, Status.class)).isEqualTo(Status.ACTIVE);
        Assertions.assertThat(service.convert(BigDecimal.ZERO, Status.class)).isEqualTo(Status.ACTIVE);
        Assertions.assertThat(service.convert(BigInteger.ZERO, Status.class)).isEqualTo(Status.ACTIVE);
    }

    @Test
    public void testInvalidOrdinalToEnumConversion() {
        DataConversionService service = new DefaultDataConversionService();

        Assertions.assertThatThrownBy(() -> service.convert(-1, Status.class))
                .isInstanceOf(NoDataConverterFoundException.class);
        Assertions.assertThatThrownBy(() -> service.convert(2, Status.class))
                .isInstanceOf(NoDataConverterFoundException.class);
        Assertions.assertThatThrownBy(() -> service.convert(1.9D, Status.class))
                .isInstanceOf(NoDataConverterFoundException.class);
    }

    @Test
    public void testDelegateDataConversionService() {
        DataConversionService delegate = Mockito.mock(DataConversionService.class);
        Mockito.when(delegate.convert(delegate.getClass(), Integer.class)).thenReturn(123);
        DataConversionService service = new DelegateDataConversionService(delegate);

        // should not call delegate for known conversions
        Assertions.assertThat(service.convert("123", Integer.class)).isEqualTo(123);

        // there's no converter for this, so it should delegate
        Assertions.assertThat(service.convert(delegate.getClass(), Integer.class)).isEqualTo(123);
        Mockito.verify(delegate, Mockito.times(1)).convert(delegate.getClass(), Integer.class);
    }

    @Test
    public void testDelegateCanConvertBehavior() {
        DataConversionService delegate = Mockito.mock(DataConversionService.class);
        Mockito.when(delegate.canConvert(Object.class, Integer.class)).thenReturn(true);
        DataConversionService service = new DelegateDataConversionService(delegate);

        Assertions.assertThat(service.canConvert(String.class, Integer.class)).isTrue();
        Assertions.assertThat(service.canConvert(Object.class, Integer.class)).isTrue();
        Assertions.assertThat(service.canConvert(Object.class, LocalDate.class)).isFalse();
    }

    @Test
    public void testDelegateNullAndFailureBehavior() {
        Assertions.assertThatThrownBy(() -> new DelegateDataConversionService(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Delegate conversion service must be provided");

        DataConversionService delegate = Mockito.mock(DataConversionService.class);
        Object source = new Object();
        Mockito.when(delegate.convert(source, Integer.class)).thenThrow(new IllegalStateException("delegate failed"));

        DataConversionService service = new DelegateDataConversionService(delegate);

        Assertions.assertThatThrownBy(() -> service.convert(source, Integer.class))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("delegate failed");
    }

    @Test
    public void testDefaultDataConversionServiceWithoutNullThrowing() {
        DataConversionService service = new DefaultDataConversionService(false);

        Assertions.assertThat(service.convert(new Object(), Integer.class)).isNull();
    }

    @Test
    public void testConcurrentAssignableConverterLookup() throws Exception {
        DataConversionService service = new DefaultDataConversionService();
        List<Future<BigDecimal>> conversions = new ArrayList<>();

        try (ExecutorService executorService = Executors.newFixedThreadPool(4)) {
            for (int value = 0; value < 50; value++) {
                int sourceValue = value;
                conversions.add(executorService.submit(() -> service.convert(new AtomicInteger(sourceValue), BigDecimal.class)));
            }
        }

        for (int value = 0; value < conversions.size(); value++) {
            Assertions.assertThat(conversions.get(value).get()).isEqualByComparingTo(BigDecimal.valueOf(value));
        }
    }

    @Test
    public void testPrimitiveAliasesForStringConverters() {
        DataConversionService service = new DefaultDataConversionService();

        Assertions.assertThat(service.convert("true", boolean.class)).isTrue();
        Assertions.assertThat(service.convert("7", byte.class)).isEqualTo((byte) 7);
        Assertions.assertThat(service.convert("a", char.class)).isEqualTo('a');
        Assertions.assertThat(service.convert("8", short.class)).isEqualTo((short) 8);
        Assertions.assertThat(service.convert("9", int.class)).isEqualTo(9);
        Assertions.assertThat(service.convert("10", long.class)).isEqualTo(10L);
        Assertions.assertThat(service.convert("1.5", float.class)).isEqualTo(1.5f);
        Assertions.assertThat(service.convert("2.5", double.class)).isEqualTo(2.5d);
    }

    @Test
    public void testDataServiceException() {
        DataConversionService service = new DefaultDataConversionService();
        Assertions.assertThatThrownBy(() -> service.convert("123", Status.class))
                .isInstanceOf(NoDataConverterFoundException.class)
                .hasMessageStartingWith("No converter found for source")
                .is(new Condition<>(e -> {
                    NoDataConverterFoundException exception = (NoDataConverterFoundException) e;
                    return exception.getSourceType().equals(String.class) && exception.getTargetType().equals(Status.class);
                }, "NoDataConverterFoundException with correct source and target types"));
    }

}
