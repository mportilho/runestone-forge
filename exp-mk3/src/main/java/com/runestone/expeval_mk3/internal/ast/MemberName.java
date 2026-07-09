package com.runestone.expeval_mk3.internal.ast;

record MemberName(String value) {

    MemberName {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Member name must not be blank");
        }
    }
}
