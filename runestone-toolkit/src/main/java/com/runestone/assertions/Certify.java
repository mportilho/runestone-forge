package com.runestone.assertions;

public class Certify {

    public static String requireNonBlank(String value, String message) {
        if (Asserts.isBlank(value)) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

}
