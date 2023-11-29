package com.runestone.converters.impl;

import com.runestone.converters.DataConverter;
import com.runestone.converters.impl.dates.*;
import com.runestone.converters.impl.numbers.*;
import com.runestone.converters.impl.strings.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.*;
import java.time.temporal.Temporal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

class DataConverterLoader {

    static void loadDateConverters(Map<ConverterPairKey, DataConverter<?, ?>> converterMap) {
        converterMap.put(new ConverterPairKey(java.sql.Date.class, LocalDate.class), new SqlDateToLocalDateConverter());
        converterMap.put(new ConverterPairKey(java.sql.Date.class, LocalDateTime.class), new SqlDateToLocalDateTimeConverter());
        converterMap.put(new ConverterPairKey(java.sql.Date.class, LocalTime.class), new SqlDateToLocalTimeConverter());
        converterMap.put(new ConverterPairKey(java.sql.Date.class, ZonedDateTime.class), new SqlDateToZonedDateTimeConverter());

        converterMap.put(new ConverterPairKey(java.util.Date.class, LocalDate.class), new UtilDateToLocalDateConverter());
        converterMap.put(new ConverterPairKey(java.util.Date.class, LocalDateTime.class), new UtilDateToLocalDateTimeConverter());
        converterMap.put(new ConverterPairKey(java.util.Date.class, LocalTime.class), new UtilDateToLocalTimeConverter());
        converterMap.put(new ConverterPairKey(java.util.Date.class, ZonedDateTime.class), new UtilDateToZonedDateTimeConverter());

        TemporalToZonedDateTimeConverter temporalToZonedDateTimeConverter = new TemporalToZonedDateTimeConverter();
        converterMap.put(new ConverterPairKey(LocalDate.class, ZonedDateTime.class), temporalToZonedDateTimeConverter);
        converterMap.put(new ConverterPairKey(LocalDateTime.class, ZonedDateTime.class), temporalToZonedDateTimeConverter);
        converterMap.put(new ConverterPairKey(OffsetDateTime.class, ZonedDateTime.class), temporalToZonedDateTimeConverter);

        TemporalToLocalDateTimeConverter temporalToLocalDateTimeConverter = new TemporalToLocalDateTimeConverter();
        converterMap.put(new ConverterPairKey(ZonedDateTime.class, LocalDateTime.class), temporalToLocalDateTimeConverter);
        converterMap.put(new ConverterPairKey(LocalDate.class, LocalDateTime.class), temporalToLocalDateTimeConverter);
        converterMap.put(new ConverterPairKey(OffsetDateTime.class, LocalDateTime.class), temporalToLocalDateTimeConverter);

        TemporalToLocalDateConverter temporalToLocalDateConverter = new TemporalToLocalDateConverter();
        converterMap.put(new ConverterPairKey(ZonedDateTime.class, LocalDate.class), temporalToLocalDateConverter);
        converterMap.put(new ConverterPairKey(LocalDateTime.class, LocalDate.class), temporalToLocalDateConverter);
        converterMap.put(new ConverterPairKey(OffsetDateTime.class, LocalDate.class), temporalToLocalDateConverter);

        TemporalToLocalTimeConverter temporalToLocalTimeConverter = new TemporalToLocalTimeConverter();
        converterMap.put(new ConverterPairKey(ZonedDateTime.class, LocalTime.class), temporalToLocalTimeConverter);
        converterMap.put(new ConverterPairKey(LocalDateTime.class, LocalTime.class), temporalToLocalTimeConverter);
        converterMap.put(new ConverterPairKey(OffsetDateTime.class, LocalTime.class), temporalToLocalTimeConverter);
    }

    static void loadNumberConverters(Map<ConverterPairKey, DataConverter<?, ?>> formattedConverters) {
        NumberToStringConverter numberToStringConverter = new NumberToStringConverter();
        NumberToBigIntegerConverter numberToBigIntegerConverter = new NumberToBigIntegerConverter();
        NumberToBigDecimalConverter numberToBigDecimalConverter = new NumberToBigDecimalConverter();
        NumberToByteConverter numberToByteConverter = new NumberToByteConverter();
        NumberToShortConverter numberToShortConverter = new NumberToShortConverter();
        NumberToIntConverter numberToIntConverter = new NumberToIntConverter();
        NumberToLongConverter numberToLongConverter = new NumberToLongConverter();
        NumberToFloatConverter numberToFloatConverter = new NumberToFloatConverter();
        NumberToDoubleConverter numberToDoubleConverter = new NumberToDoubleConverter();

        Map<Class<?>, DataConverter<?, ?>> convetersMap = new HashMap<>();
        convetersMap.put(String.class, numberToStringConverter);
        convetersMap.put(BigInteger.class, numberToBigIntegerConverter);
        convetersMap.put(BigDecimal.class, numberToBigDecimalConverter);
        convetersMap.put(byte.class, numberToByteConverter);
        convetersMap.put(Byte.class, numberToByteConverter);
        convetersMap.put(short.class, numberToShortConverter);
        convetersMap.put(Short.class, numberToShortConverter);
        convetersMap.put(int.class, numberToIntConverter);
        convetersMap.put(Integer.class, numberToIntConverter);
        convetersMap.put(long.class, numberToLongConverter);
        convetersMap.put(Long.class, numberToLongConverter);
        convetersMap.put(float.class, numberToFloatConverter);
        convetersMap.put(Float.class, numberToFloatConverter);
        convetersMap.put(double.class, numberToDoubleConverter);
        convetersMap.put(Double.class, numberToDoubleConverter);

        for (Class<?> sourceClass : convetersMap.keySet()) {
            for (Class<?> targetClass : convetersMap.keySet()) {
                // Skip same class and String because there is already a converter for String source in another method
                if (sourceClass.getSimpleName().equalsIgnoreCase(targetClass.getSimpleName()) || sourceClass.equals(String.class)) {
                    continue;
                }
                formattedConverters.put(new ConverterPairKey(sourceClass, targetClass), convetersMap.get(targetClass));
            }
        }
    }

    static void loadStringConverters(Map<ConverterPairKey, DataConverter<?, ?>> formattedConverters) {
        StringToByteConverter stringToByteConverter = new StringToByteConverter();
        StringToDoubleConverter stringToDoubleConverter = new StringToDoubleConverter();
        StringToFloatConverter stringToFloatConverter = new StringToFloatConverter();
        StringToIntegerConverter stringToIntegerConverter = new StringToIntegerConverter();
        StringToLongConverter stringToLongConverter = new StringToLongConverter();
        StringToShortConverter stringToShortConverter = new StringToShortConverter();

        formattedConverters.put(new ConverterPairKey(String.class, Byte.class), stringToByteConverter);
        formattedConverters.put(new ConverterPairKey(String.class, byte.class), stringToByteConverter);
        formattedConverters.put(new ConverterPairKey(String.class, Double.class), stringToDoubleConverter);
        formattedConverters.put(new ConverterPairKey(String.class, double.class), stringToDoubleConverter);
        formattedConverters.put(new ConverterPairKey(String.class, Float.class), stringToFloatConverter);
        formattedConverters.put(new ConverterPairKey(String.class, float.class), stringToFloatConverter);
        formattedConverters.put(new ConverterPairKey(String.class, Integer.class), stringToIntegerConverter);
        formattedConverters.put(new ConverterPairKey(String.class, int.class), stringToIntegerConverter);
        formattedConverters.put(new ConverterPairKey(String.class, Long.class), stringToLongConverter);
        formattedConverters.put(new ConverterPairKey(String.class, long.class), stringToLongConverter);
        formattedConverters.put(new ConverterPairKey(String.class, Short.class), stringToShortConverter);
        formattedConverters.put(new ConverterPairKey(String.class, short.class), stringToShortConverter);
        formattedConverters.put(new ConverterPairKey(String.class, BigDecimal.class), new StringToBigDecimalConverter());
        formattedConverters.put(new ConverterPairKey(String.class, BigInteger.class), new StringToBigIntegerConverter());

        formattedConverters.put(new ConverterPairKey(String.class, Boolean.class), new StringToBooleanConverter());

        formattedConverters.put(new ConverterPairKey(String.class, Instant.class), new StringToInstantConverter());
        formattedConverters.put(new ConverterPairKey(String.class, java.sql.Date.class), new StringToJavaSqlDateConverter());
        formattedConverters.put(new ConverterPairKey(String.class, Date.class), new StringToJavaUtilDateConverter());
        formattedConverters.put(new ConverterPairKey(String.class, LocalDate.class), new StringToLocalDateConverter());
        formattedConverters.put(new ConverterPairKey(String.class, LocalDateTime.class), new StringToLocalDateTimeConverter());
        formattedConverters.put(new ConverterPairKey(String.class, LocalTime.class), new StringToLocalTimeConverter());
        formattedConverters.put(new ConverterPairKey(String.class, OffsetDateTime.class), new StringToOffsetDateTimeConverter());
        formattedConverters.put(new ConverterPairKey(String.class, OffsetTime.class), new StringToOffsetTimeConverter());
        formattedConverters.put(new ConverterPairKey(String.class, Timestamp.class), new StringToTimestampConverter());
        formattedConverters.put(new ConverterPairKey(String.class, Year.class), new StringToYearConverter());
        formattedConverters.put(new ConverterPairKey(String.class, YearMonth.class), new StringToYearMonthConverter());
        formattedConverters.put(new ConverterPairKey(String.class, ZonedDateTime.class), new StringToZonedDateTimeConverter());
        formattedConverters.put(new ConverterPairKey(String.class, Temporal.class), new StringToTemporalConverter());
    }

}
