package com.runestone.converters.impl.stable.strings;

import com.runestone.converters.ConversionContext;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;

public class TestStringToFileConverter {

    @Test
    public void testConvertValidValues() {
        StringToFileConverter converter = new StringToFileConverter();
        Assertions.assertThat(converter.convert("/var/log/syslog", ConversionContext.standard())).isEqualTo(new File("/var/log/syslog"));
        Assertions.assertThat(converter.convert("C:\\Windows\\System32", ConversionContext.standard())).isEqualTo(new File("C:\\Windows\\System32"));
    }

    @Test
    public void testConvertNull() {
        StringToFileConverter converter = new StringToFileConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(null, ConversionContext.standard()))
                .isInstanceOf(NullPointerException.class);
    }
}
