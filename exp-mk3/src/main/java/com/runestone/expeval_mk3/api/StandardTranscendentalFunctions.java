package com.runestone.expeval_mk3.api;

import ch.obermuhlner.math.big.BigDecimalMath;

import java.math.BigDecimal;
import java.math.MathContext;

final class StandardTranscendentalFunctions {

    private StandardTranscendentalFunctions() {
    }

    public static BigDecimal sin(MathContext mathContext, BigDecimal value) {
        return BigDecimalMath.sin(value, mathContext);
    }

    public static BigDecimal cos(MathContext mathContext, BigDecimal value) {
        return BigDecimalMath.cos(value, mathContext);
    }

    public static BigDecimal tan(MathContext mathContext, BigDecimal value) {
        return BigDecimalMath.tan(value, mathContext);
    }

    public static BigDecimal asin(MathContext mathContext, BigDecimal value) {
        return BigDecimalMath.asin(value, mathContext);
    }

    public static BigDecimal acos(MathContext mathContext, BigDecimal value) {
        return BigDecimalMath.acos(value, mathContext);
    }

    public static BigDecimal atan(MathContext mathContext, BigDecimal value) {
        return BigDecimalMath.atan(value, mathContext);
    }

    public static BigDecimal atan2(MathContext mathContext, BigDecimal y, BigDecimal x) {
        return BigDecimalMath.atan2(y, x, mathContext);
    }

    public static BigDecimal sinh(MathContext mathContext, BigDecimal value) {
        return BigDecimalMath.sinh(value, mathContext);
    }

    public static BigDecimal cosh(MathContext mathContext, BigDecimal value) {
        return BigDecimalMath.cosh(value, mathContext);
    }

    public static BigDecimal tanh(MathContext mathContext, BigDecimal value) {
        return BigDecimalMath.tanh(value, mathContext);
    }

    public static BigDecimal asinh(MathContext mathContext, BigDecimal value) {
        return BigDecimalMath.asinh(value, mathContext);
    }

    public static BigDecimal acosh(MathContext mathContext, BigDecimal value) {
        return BigDecimalMath.acosh(value, mathContext);
    }

    public static BigDecimal atanh(MathContext mathContext, BigDecimal value) {
        return BigDecimalMath.atanh(value, mathContext);
    }

    public static BigDecimal ln(MathContext mathContext, BigDecimal value) {
        return BigDecimalMath.log(value, mathContext);
    }

    public static BigDecimal lb(MathContext mathContext, BigDecimal value) {
        return BigDecimalMath.log2(value, mathContext);
    }

    public static BigDecimal log(MathContext mathContext, BigDecimal base, BigDecimal value) {
        return BigDecimalMath.log(value, mathContext).divide(BigDecimalMath.log(base, mathContext), mathContext);
    }
}
