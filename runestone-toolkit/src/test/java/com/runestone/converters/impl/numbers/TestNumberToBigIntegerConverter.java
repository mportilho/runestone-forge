package com.runestone.converters.impl.numbers;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.atomic.*;

import static org.assertj.core.api.Assertions.assertThat;

public class TestNumberToBigIntegerConverter {

    @Test
    public void testNumbersConversions() {
        NumberToBigIntegerConverter converter = new NumberToBigIntegerConverter();
        assertThat(converter.convert((byte) 1)).isEqualTo(BigInteger.valueOf(1));
        assertThat(converter.convert((short) 1)).isEqualTo(BigInteger.valueOf(1));
        assertThat(converter.convert(1)).isEqualTo(BigInteger.valueOf(1));
        assertThat(converter.convert(1L)).isEqualTo(BigInteger.valueOf(1));
        assertThat(converter.convert(1F)).isEqualTo(BigInteger.valueOf(1));
        assertThat(converter.convert(1D)).isEqualTo(BigInteger.valueOf(1));
        assertThat(converter.convert(BigDecimal.valueOf(1))).isEqualTo(BigInteger.valueOf(1));
        assertThat(converter.convert(new BigInteger("1"))).isEqualTo(BigInteger.valueOf(1));
    }

    @Test
    public void testConcurrentNumbersConversions() {
        NumberToBigIntegerConverter converter = new NumberToBigIntegerConverter();
        assertThat(converter.convert(new AtomicInteger(1))).isEqualTo(BigInteger.valueOf(1));
        assertThat(converter.convert(new AtomicLong(1))).isEqualTo(BigInteger.valueOf(1));
        assertThat(converter.convert(new DoubleAccumulator(Double::sum, 1))).isEqualTo(BigInteger.valueOf(1));
        assertThat(converter.convert(new DoubleAdder())).isEqualTo(BigInteger.valueOf(0));
        assertThat(converter.convert(new LongAccumulator(Long::sum, 1))).isEqualTo(BigInteger.valueOf(1));
        assertThat(converter.convert(new LongAdder())).isEqualTo(BigInteger.valueOf(0));
    }
}
