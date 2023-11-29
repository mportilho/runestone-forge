package com.runestone.converters.impl;

import com.runestone.converters.DataConversionService;
import com.runestone.converters.NoDataConverterFoundException;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;

public class TestDataConversionService {

    public enum Status {
        ACTIVE,
        INACTIVE
    }

    @Test
    public void testDefaultDataConversionService() {
        DataConversionService service = new DefaultDataConversionService();
        Assertions.assertThat(service.convert("123", Integer.class)).isEqualTo(Integer.valueOf("123"));
        Assertions.assertThat(service.convert("123", int.class)).isEqualTo(123);
        Assertions.assertThat(service.convert(123, String.class)).isEqualTo("123");
        Assertions.assertThat(service.convert("20200102", LocalDate.class)).isEqualTo(LocalDate.of(2020, 1, 2));
    }

    @Test
    public void testStringToEnumConversion() {
        DataConversionService service = new DefaultDataConversionService();
        Assertions.assertThat(service.convert("ACTIVE", Status.class)).isEqualTo(Status.ACTIVE);
        Assertions.assertThat(service.convert("INACTIVE", Status.class)).isEqualTo(Status.INACTIVE);
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
