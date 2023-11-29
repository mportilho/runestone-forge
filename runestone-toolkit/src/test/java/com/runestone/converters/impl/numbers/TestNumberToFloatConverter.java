package com.runestone.converters.impl.numbers;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.atomic.*;

import static org.assertj.core.api.Assertions.assertThat;

public class TestNumberToFloatConverter {

    @Test
    public void testNumbersConversions() {
        NumberToFloatConverter converter = new NumberToFloatConverter();
        assertThat(converter.convert((byte) 1)).isEqualTo(1F);
        assertThat(converter.convert((short) 1)).isEqualTo(1F);
        assertThat(converter.convert(1)).isEqualTo(1F);
        assertThat(converter.convert(1L)).isEqualTo(1F);
        assertThat(converter.convert(1F)).isEqualTo(1F);
        assertThat(converter.convert(1D)).isEqualTo(1F);
        assertThat(converter.convert(BigDecimal.valueOf(1))).isEqualTo(1F);
        assertThat(converter.convert(new BigInteger("1"))).isEqualTo(1F);
    }

    @Test
    public void testConcurrentNumbersConversions() {
        NumberToFloatConverter converter = new NumberToFloatConverter();
        assertThat(converter.convert(new AtomicInteger(1))).isEqualTo(1F);
        assertThat(converter.convert(new AtomicLong(1))).isEqualTo(1F);
        assertThat(converter.convert(new DoubleAccumulator(Double::sum, 1))).isEqualTo(1F);
        assertThat(converter.convert(new DoubleAdder())).isEqualTo(0F);
        assertThat(converter.convert(new LongAccumulator(Long::sum, 1))).isEqualTo(1F);
        assertThat(converter.convert(new LongAdder())).isEqualTo(0F);
    }
}
