package com.runestone.converters.impl.numbers;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.atomic.*;

import static org.assertj.core.api.Assertions.assertThat;

public class TestNumberToByteConverter {

    @Test
    public void testNumbersConversions() {
        NumberToByteConverter converter = new NumberToByteConverter();
        assertThat(converter.convert((byte) 1)).isEqualTo((byte) 1);
        assertThat(converter.convert((short) 1)).isEqualTo((byte) 1);
        assertThat(converter.convert(1)).isEqualTo((byte) 1);
        assertThat(converter.convert(1L)).isEqualTo((byte) 1);
        assertThat(converter.convert(1F)).isEqualTo((byte) 1);
        assertThat(converter.convert(1D)).isEqualTo((byte) 1);
        assertThat(converter.convert(BigDecimal.valueOf(1))).isEqualTo((byte) 1);
        assertThat(converter.convert(new BigInteger("1"))).isEqualTo((byte) 1);
    }

    @Test
    public void testConcurrentNumbersConversions() {
        NumberToByteConverter converter = new NumberToByteConverter();
        assertThat(converter.convert(new AtomicInteger(1))).isEqualTo((byte) 1);
        assertThat(converter.convert(new AtomicLong(1))).isEqualTo((byte) 1);
        assertThat(converter.convert(new DoubleAccumulator(Double::sum, 1))).isEqualTo((byte) 1);
        assertThat(converter.convert(new DoubleAdder())).isEqualTo((byte) 0);
        assertThat(converter.convert(new LongAccumulator(Long::sum, 1))).isEqualTo((byte) 1);
        assertThat(converter.convert(new LongAdder())).isEqualTo((byte) 0);
    }
}
