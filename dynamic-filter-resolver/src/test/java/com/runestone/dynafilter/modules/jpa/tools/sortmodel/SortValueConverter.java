package com.runestone.dynafilter.modules.jpa.tools.sortmodel;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class SortValueConverter implements AttributeConverter<SortValue, String> {
    @Override
    public String convertToDatabaseColumn(SortValue value) {
        return value == null ? null : value.value();
    }

    @Override
    public SortValue convertToEntityAttribute(String value) {
        return value == null ? null : new SortValue(value);
    }
}
