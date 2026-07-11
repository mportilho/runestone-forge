package com.runestone.converters.impl.numbers;

import com.runestone.converters.DataConverter;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class TestNumericConverterContract {

    @Test
    void declaresExplicitNumericConversionRules() {
        List<DataConverter<Number, ?>> converters = List.of(
                new NumberToByteConverter(),
                new NumberToShortConverter(),
                new NumberToIntConverter(),
                new NumberToLongConverter(),
                new NumberToFloatConverter(),
                new NumberToDoubleConverter(),
                new NumberToBigIntegerConverter(),
                new NumberToBigDecimalConverter(),
                new NumberToStringConverter());

        assertThat(converters)
                .extracting(DataConverter::sourceType, DataConverter::targetType, DataConverter::ruleIdentity)
                .containsExactly(
                        tuple(Number.class, Byte.class, "number:byte"),
                        tuple(Number.class, Short.class, "number:short"),
                        tuple(Number.class, Integer.class, "number:integer"),
                        tuple(Number.class, Long.class, "number:long"),
                        tuple(Number.class, Float.class, "number:float"),
                        tuple(Number.class, Double.class, "number:double"),
                        tuple(Number.class, BigInteger.class, "number:biginteger"),
                        tuple(Number.class, BigDecimal.class, "number:bigdecimal"),
                        tuple(Number.class, String.class, "number:string"));
        assertThat(converters)
                .extracting(DataConverter::ruleIdentity)
                .doesNotHaveDuplicates()
                .allMatch(identity -> identity.matches("[a-z0-9][a-z0-9._:-]*"));
    }
}
