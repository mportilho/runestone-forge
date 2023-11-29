package com.runestone.converters.impl.numbers;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.atomic.*;

import static org.assertj.core.api.Assertions.assertThat;

public class TestNumberToBigDecimalConverter {

    @Test
    public void testNumbersConversions() {
        NumberToBigDecimalConverter converter = new NumberToBigDecimalConverter();
        assertThat(converter.convert((byte) 1)).isEqualTo(BigDecimal.valueOf(1));
        assertThat(converter.convert((short) 1)).isEqualTo(BigDecimal.valueOf(1));
        assertThat(converter.convert(1)).isEqualTo(BigDecimal.valueOf(1));
        assertThat(converter.convert(1L)).isEqualTo(BigDecimal.valueOf(1L));
        assertThat(converter.convert(1F)).isEqualTo(BigDecimal.valueOf(1F));
        assertThat(converter.convert(1D)).isEqualTo(BigDecimal.valueOf(1D));
        assertThat(converter.convert(BigDecimal.valueOf(1))).isEqualTo(BigDecimal.valueOf(1));
        assertThat(converter.convert(new BigInteger("1"))).isEqualTo(BigDecimal.valueOf(1));
    }

    @Test
    public void testConcurrentNumbersConversions() {
        NumberToBigDecimalConverter converter = new NumberToBigDecimalConverter();
        assertThat(converter.convert(new AtomicInteger(1))).isEqualTo(BigDecimal.valueOf(1));
        assertThat(converter.convert(new AtomicLong(1))).isEqualTo(BigDecimal.valueOf(1));
        assertThat(converter.convert(new DoubleAccumulator(Double::sum, 1))).isEqualByComparingTo(BigDecimal.valueOf(1));
        assertThat(converter.convert(new DoubleAdder())).isEqualByComparingTo(BigDecimal.valueOf(0));
        assertThat(converter.convert(new LongAccumulator(Long::sum, 1))).isEqualTo(BigDecimal.valueOf(1));
        assertThat(converter.convert(new LongAdder())).isEqualTo(BigDecimal.valueOf(0));
    }

}
