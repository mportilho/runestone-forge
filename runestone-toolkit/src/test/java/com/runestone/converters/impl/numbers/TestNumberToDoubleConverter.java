package com.runestone.converters.impl.numbers;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.atomic.*;

import static org.assertj.core.api.Assertions.assertThat;

public class TestNumberToDoubleConverter {

    @Test
    public void testNumbersConversions() {
        NumberToDoubleConverter converter = new NumberToDoubleConverter();
        assertThat(converter.convert((byte) 1)).isEqualTo(1D);
        assertThat(converter.convert((short) 1)).isEqualTo(1D);
        assertThat(converter.convert(1)).isEqualTo(1D);
        assertThat(converter.convert(1L)).isEqualTo(1D);
        assertThat(converter.convert(1F)).isEqualTo(1D);
        assertThat(converter.convert(1D)).isEqualTo(1D);
        assertThat(converter.convert(BigDecimal.valueOf(1))).isEqualTo(1D);
        assertThat(converter.convert(new BigInteger("1"))).isEqualTo(1D);
    }

    @Test
    public void testConcurrentNumbersConversions() {
        NumberToDoubleConverter converter = new NumberToDoubleConverter();
        assertThat(converter.convert(new AtomicInteger(1))).isEqualTo(1D);
        assertThat(converter.convert(new AtomicLong(1))).isEqualTo(1D);
        assertThat(converter.convert(new DoubleAccumulator(Double::sum, 1))).isEqualTo(1D);
        assertThat(converter.convert(new DoubleAdder())).isEqualTo(0D);
        assertThat(converter.convert(new LongAccumulator(Long::sum, 1))).isEqualTo(1D);
        assertThat(converter.convert(new LongAdder())).isEqualTo(0D);
    }
}
