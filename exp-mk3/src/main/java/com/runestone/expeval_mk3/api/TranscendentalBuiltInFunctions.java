package com.runestone.expeval_mk3.api;

import ch.obermuhlner.math.big.BigDecimalMath;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.Objects;

final class TranscendentalBuiltInFunctions {

    private static final double LN_2 = Math.log(2);

    private final MathContext mathContext;
    TranscendentalBuiltInFunctions(MathContext mathContext) {
        this.mathContext = Objects.requireNonNull(mathContext, "mathContext");
    }

    static List<FunctionDescriptor> descriptors(BuiltInResolutionContext context, MathContext mathContext) {
        ReflectedFunctionImporter.ImportPlan plan = ReflectedFunctionImporter.importAll(
                new TranscendentalBuiltInFunctions(mathContext),
                FunctionPurity.FOLDABLE);
        return ReflectedFunctionImporter.importTrustedOrThrow(plan, context);
    }

    public BigDecimal sin(BigDecimal value) {
        return BigDecimalMath.sin(value, mathContext);
    }

    public BigDecimal cos(BigDecimal value) {
        return BigDecimalMath.cos(value, mathContext);
    }

    public BigDecimal tan(BigDecimal value) {
        return BigDecimalMath.tan(value, mathContext);
    }

    public BigDecimal asin(BigDecimal value) {
        return BigDecimalMath.asin(value, mathContext);
    }

    public BigDecimal acos(BigDecimal value) {
        return BigDecimalMath.acos(value, mathContext);
    }

    public BigDecimal atan(BigDecimal value) {
        return BigDecimalMath.atan(value, mathContext);
    }

    public BigDecimal atan2(BigDecimal y, BigDecimal x) {
        return BigDecimalMath.atan2(y, x, mathContext);
    }

    public BigDecimal sinh(BigDecimal value) {
        return BigDecimalMath.sinh(value, mathContext);
    }

    public BigDecimal cosh(BigDecimal value) {
        return BigDecimalMath.cosh(value, mathContext);
    }

    public BigDecimal tanh(BigDecimal value) {
        return BigDecimalMath.tanh(value, mathContext);
    }

    public BigDecimal asinh(BigDecimal value) {
        return BigDecimalMath.asinh(value, mathContext);
    }

    public BigDecimal acosh(BigDecimal value) {
        return BigDecimalMath.acosh(value, mathContext);
    }

    public BigDecimal atanh(BigDecimal value) {
        return BigDecimalMath.atanh(value, mathContext);
    }

    public BigDecimal ln(BigDecimal value) {
        return BigDecimalMath.log(value, mathContext);
    }

    public BigDecimal lb(BigDecimal value) {
        return BigDecimalMath.log2(value, mathContext);
    }

    public BigDecimal log(BigDecimal base, BigDecimal value) {
        return BigDecimalMath.log(value, mathContext).divide(BigDecimalMath.log(base, mathContext), mathContext);
    }

    public BigDecimal lnFast(BigDecimal value) {
        return BigDecimal.valueOf(Math.log(value.doubleValue()));
    }

    public BigDecimal lbFast(BigDecimal value) {
        return BigDecimal.valueOf(Math.log(value.doubleValue()) / LN_2);
    }

    public BigDecimal logFast(BigDecimal base, BigDecimal value) {
        return BigDecimal.valueOf(Math.log(value.doubleValue()) / Math.log(base.doubleValue()));
    }
}
