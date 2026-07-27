package com.runestone.expeval_mk3.internal.ast;

public record MemberName(String value) {

    public MemberName {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Member name must not be blank");
        }
    }
}
