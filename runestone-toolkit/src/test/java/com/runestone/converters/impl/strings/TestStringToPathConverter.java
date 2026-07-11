package com.runestone.converters.impl.strings;

import com.runestone.converters.ConversionContext;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.InvalidPathException;
import java.nio.file.Paths;

public class TestStringToPathConverter {

    @Test
    public void testConvertValidValues() {
        StringToPathConverter converter = new StringToPathConverter();
        Assertions.assertThat(converter.convert("/var/log/syslog", ConversionContext.standard())).isEqualTo(Paths.get("/var/log/syslog"));
        Assertions.assertThat(converter.convert("C:\\Windows\\System32", ConversionContext.standard())).isEqualTo(Paths.get("C:\\Windows\\System32"));
    }

    @Test
    public void testConvertNull() {
        StringToPathConverter converter = new StringToPathConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(null, ConversionContext.standard()))
                .isInstanceOf(NullPointerException.class);
    }
    
    @Test
    public void testConvertMalformed() {
        StringToPathConverter converter = new StringToPathConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("invalid\u0000path", ConversionContext.standard())) // Null byte is invalid in Paths
                .isInstanceOf(InvalidPathException.class);
    }
}
