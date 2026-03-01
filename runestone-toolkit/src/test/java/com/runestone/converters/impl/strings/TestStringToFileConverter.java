package com.runestone.converters.impl.strings;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;

public class TestStringToFileConverter {

    @Test
    public void testConvertValidValues() {
        StringToFileConverter converter = new StringToFileConverter();
        Assertions.assertThat(converter.convert("/var/log/syslog")).isEqualTo(new File("/var/log/syslog"));
        Assertions.assertThat(converter.convert("C:\\Windows\\System32")).isEqualTo(new File("C:\\Windows\\System32"));
    }

    @Test
    public void testConvertNull() {
        StringToFileConverter converter = new StringToFileConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(null))
                .isInstanceOf(NullPointerException.class);
    }
}
