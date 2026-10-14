package com.runestone.converters.impl.stable.strings;

import com.runestone.converters.ConversionContext;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class TestStringToInetAddressConverter {

    @Test
    public void testConvertValidIPv4() throws UnknownHostException {
        StringToInetAddressConverter converter = new StringToInetAddressConverter();
        Assertions.assertThat(converter.convert("127.0.0.1", ConversionContext.standard())).isEqualTo(InetAddress.getByName("127.0.0.1"));
        Assertions.assertThat(converter.convert("8.8.8.8", ConversionContext.standard())).isEqualTo(InetAddress.getByName("8.8.8.8"));
    }
    
    @Test
    public void testConvertValidIPv6() throws UnknownHostException {
        StringToInetAddressConverter converter = new StringToInetAddressConverter();
        Assertions.assertThat(converter.convert("::1", ConversionContext.standard())).isEqualTo(InetAddress.getByName("::1"));
    }

    @Test
    public void testConvertNull() {
        StringToInetAddressConverter converter = new StringToInetAddressConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(null, ConversionContext.standard()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown host");
    }

    @Test
    public void testConvertUnknownHost() {
        StringToInetAddressConverter converter = new StringToInetAddressConverter();
        // Since getByName() might try to resolve via DNS, we use a specifically bad formatted generic text 
        // to forcefully trigger UnknownHostException without hanging CI environments in DNS lookup.
        Assertions.assertThatThrownBy(() -> converter.convert("local host not valid format", ConversionContext.standard()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown host");
    }
}
