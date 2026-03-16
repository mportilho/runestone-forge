package com.runestone.expeval2.catalog;

public record FunctionCatalogId(String value) {

    public FunctionCatalogId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("value must not be blank");
        }
    }
}
