package com.runestone.converters.impl.numbers;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.atomic.*;

import static org.assertj.core.api.Assertions.*;

public class TestNumberToShortConverter {

    @Test
    public void testNumbersConversions() {
        NumberToShortConverter converter = new NumberToShortConverter();
        assertThat(converter.convert((byte) 1)).isEqualTo((short) 1);
        assertThat(converter.convert((short) 1)).isEqualTo((short) 1);
        assertThat(converter.convert(1)).isEqualTo((short) 1);
        assertThat(converter.convert(1L)).isEqualTo((short) 1);
        assertThat(converter.convert(1F)).isEqualTo((short) 1);
        assertThat(converter.convert(1D)).isEqualTo((short) 1);
        assertThat(converter.convert(BigDecimal.valueOf(1))).isEqualTo((short) 1);
        assertThat(converter.convert(new BigInteger("1"))).isEqualTo((short) 1);
    }

    @Test
    public void testConcurrentNumbersConversions() {
        NumberToShortConverter converter = new NumberToShortConverter();
        assertThat(converter.convert(new AtomicInteger(1))).isEqualTo((short) 1);
        assertThat(converter.convert(new AtomicLong(1))).isEqualTo((short) 1);
        assertThat(converter.convert(new DoubleAccumulator(Double::sum, 1))).isEqualTo((short) 1);
        assertThat(converter.convert(new DoubleAdder())).isEqualTo((short) 0);
        assertThat(converter.convert(new LongAccumulator(Long::sum, 1))).isEqualTo((short) 1);
        assertThat(converter.convert(new LongAdder())).isEqualTo((short) 0);
    }

}