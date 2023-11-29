package com.runestone.expeval.expression;

import com.runestone.converters.DataConversionService;
import com.runestone.converters.impl.DefaultDataConversionService;

import java.math.MathContext;
import java.time.ZoneId;

/**
 * Options for configuring the expression evaluation.
 *
 * @param mathContext       the math context to be used for calculations
 * @param scale             the scale to be used for calculations. If null, assumes BigDecimal default behavior
 * @param conversionService the conversion service to be used for converting values
 * @param zoneId            the zone id to be used for date/time operations
 * @author Marcelo Portilho
 */
public record ExpressionOptions(MathContext mathContext, Integer scale, DataConversionService conversionService, ZoneId zoneId) {

    private static final DataConversionService DEFAULT_CONVERSION_SERVICE = new DefaultDataConversionService();

    /**
     * Creates a new expression options with the given math context, scale, conversion service and zone id.
     *
     * @return the expression options
     */
    public static ExpressionOptions defaultOptions() {
        return new ExpressionOptions(MathContext.DECIMAL64, null, DEFAULT_CONVERSION_SERVICE, ZoneId.systemDefault());
    }

    /**
     * Creates a new expression options with the given math context and scale.
     *
     * @param mathContext the math context to be used for calculations
     * @param scale       the scale to be used for calculations. If null, assumes BigDecimal default behavior
     * @return the expression options
     */
    public static ExpressionOptions of(MathContext mathContext, Integer scale) {
        return new ExpressionOptions(mathContext, scale, DEFAULT_CONVERSION_SERVICE, ZoneId.systemDefault());
    }

}
