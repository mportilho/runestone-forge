package com.runestone.converters.impl.strings;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.InvalidPathException;
import java.nio.file.Paths;

public class TestStringToPathConverter {

    @Test
    public void testConvertValidValues() {
        StringToPathConverter converter = new StringToPathConverter();
        Assertions.assertThat(converter.convert("/var/log/syslog")).isEqualTo(Paths.get("/var/log/syslog"));
        Assertions.assertThat(converter.convert("C:\\Windows\\System32")).isEqualTo(Paths.get("C:\\Windows\\System32"));
    }

    @Test
    public void testConvertNull() {
        StringToPathConverter converter = new StringToPathConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(null))
                .isInstanceOf(NullPointerException.class);
    }
    
    @Test
    public void testConvertMalformed() {
        StringToPathConverter converter = new StringToPathConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("invalid\u0000path")) // Null byte is invalid in Paths
                .isInstanceOf(InvalidPathException.class);
    }
}
