package com.runestone.expeval_mk3.api;

import ch.obermuhlner.math.big.BigDecimalMath;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.Objects;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;

final class FinancialBuiltInFunctions {

    private final MathContext mathContext;

    FinancialBuiltInFunctions(MathContext mathContext) {
        this.mathContext = Objects.requireNonNull(mathContext, "mathContext");
    }

    static List<FunctionDescriptor> descriptors(MathContext mathContext) {
        return ReflectedFunctionImporter
                .importAll(
                        new FinancialBuiltInFunctions(mathContext),
                        BuiltInFunctionSupport.mathContextProviderId("built-in.financial", mathContext),
                        FunctionPurity.FOLDABLE)
                .rename("fvRegular", "fv")
                .rename("fvTyped", "fv")
                .rename("fvNoType", "fv")
                .rename("pmtRegular", "pmt")
                .rename("pmtTyped", "pmt")
                .rename("pmtNoType", "pmt")
                .rename("pmtPresent", "pmt")
                .rename("ipmtTyped", "ipmt")
                .rename("ipmtNoType", "ipmt")
                .rename("ipmtPresent", "ipmt")
                .rename("ppmtTyped", "ppmt")
                .rename("ppmtNoType", "ppmt")
                .rename("ppmtPresent", "ppmt")
                .toList();
    }

    public BigDecimal fvRegular(
            BigDecimal rate,
            BigDecimal periods,
            BigDecimal payment,
            BigDecimal presentValue,
            Boolean dueAtPeriodStart) {
        if (rate.compareTo(ZERO) == 0) {
            return presentValue.add(periods.multiply(payment)).negate();
        }
        BigDecimal ratePlusOne = rate.add(ONE);
        BigDecimal ratePowerPeriods = BigDecimalMath.pow(ratePlusOne, periods, mathContext);
        return ONE.subtract(ratePowerPeriods)
                .multiply(dueAtPeriodStart ? ratePlusOne : ONE)
                .multiply(payment)
                .divide(rate, mathContext)
                .subtract(presentValue.multiply(ratePowerPeriods));
    }

    public BigDecimal pv(
            BigDecimal rate,
            BigDecimal periods,
            BigDecimal payment,
            BigDecimal futureValue,
            Boolean dueAtPeriodStart) {
        if (rate.compareTo(ZERO) == 0) {
            return periods.multiply(payment).add(futureValue).negate();
        }
        BigDecimal ratePlusOne = rate.add(ONE);
        BigDecimal ratePowerPeriods = BigDecimalMath.pow(ratePlusOne, periods, mathContext);
        return ONE.subtract(ratePowerPeriods)
                .divide(rate, mathContext)
                .multiply(dueAtPeriodStart ? ratePlusOne : ONE)
                .multiply(payment)
                .subtract(futureValue)
                .divide(ratePowerPeriods, mathContext);
    }

    public BigDecimal npv(BigDecimal rate, List<BigDecimal> cashFlows) {
        BigDecimal npv = ZERO;
        BigDecimal ratePlusOne = rate.add(ONE);
        BigDecimal currentRate = ratePlusOne;
        for (BigDecimal cashFlow : BuiltInFunctionSupport.numbers(cashFlows)) {
            npv = npv.add(cashFlow.divide(currentRate, mathContext));
            currentRate = currentRate.multiply(ratePlusOne);
        }
        return npv;
    }

    public BigDecimal pmtRegular(
            BigDecimal rate,
            BigDecimal periods,
            BigDecimal presentValue,
            BigDecimal futureValue,
            Boolean dueAtPeriodStart) {
        if (rate.compareTo(ZERO) == 0) {
            return futureValue.add(presentValue).negate().divide(periods, mathContext);
        }
        BigDecimal ratePlusOne = rate.add(ONE);
        BigDecimal ratePowerPeriods = BigDecimalMath.pow(ratePlusOne, periods, mathContext);
        return futureValue.add(presentValue.multiply(ratePowerPeriods))
                .multiply(rate)
                .divide((dueAtPeriodStart ? ratePlusOne : ONE).multiply(ONE.subtract(ratePowerPeriods)), mathContext);
    }

    public BigDecimal nper(
            BigDecimal rate,
            BigDecimal payment,
            BigDecimal presentValue,
            BigDecimal futureValue,
            Boolean dueAtPeriodStart) {
        if (rate.compareTo(ZERO) == 0) {
            return futureValue.add(presentValue).negate().divide(payment, mathContext);
        }
        BigDecimal ratePlusOne = rate.add(ONE);
        BigDecimal ratePayment = (dueAtPeriodStart ? ratePlusOne : ONE).multiply(payment).divide(rate, mathContext);
        BigDecimal ratePaymentMinusFuture = ratePayment.subtract(futureValue);
        BigDecimal firstLog = BigDecimalMath.log(ratePaymentMinusFuture.abs(), mathContext);
        BigDecimal secondLog = ratePaymentMinusFuture.compareTo(ZERO) < 0
                ? BigDecimalMath.log(presentValue.add(ratePayment).negate(), mathContext)
                : BigDecimalMath.log(presentValue.add(ratePayment), mathContext);
        return firstLog.subtract(secondLog).divide(BigDecimalMath.log(ratePlusOne, mathContext), mathContext);
    }

    public BigDecimal pmtTyped(
            BigDecimal rate,
            BigDecimal periods,
            BigDecimal presentValue,
            BigDecimal futureValue,
            BigDecimal type) {
        BigDecimal ratePlusOne = rate.add(ONE);
        BigDecimal ratePowerPeriods = BigDecimalMath.pow(ratePlusOne, periods, mathContext);
        BigDecimal numerator = rate.negate().multiply(presentValue.multiply(ratePowerPeriods).add(futureValue));
        BigDecimal denominator = ONE.add(rate.multiply(BigDecimal.valueOf(BuiltInFunctionSupport.integer(type))))
                .multiply(ratePowerPeriods.subtract(ONE));
        return numerator.divide(denominator, mathContext);
    }

    public BigDecimal pmtNoType(
            BigDecimal rate,
            BigDecimal periods,
            BigDecimal presentValue,
            BigDecimal futureValue) {
        return pmtTyped(rate, periods, presentValue, futureValue, ZERO);
    }

    public BigDecimal pmtPresent(BigDecimal rate, BigDecimal periods, BigDecimal presentValue) {
        return pmtNoType(rate, periods, presentValue, ZERO);
    }

    public BigDecimal ipmtTyped(
            BigDecimal rate,
            BigDecimal period,
            BigDecimal periods,
            BigDecimal presentValue,
            BigDecimal futureValue,
            BigDecimal type) {
        BigDecimal interestPayment = fvTyped(
                rate,
                period.subtract(ONE),
                pmtTyped(rate, periods, presentValue, futureValue, type),
                presentValue,
                type)
                .multiply(rate, mathContext);
        if (BuiltInFunctionSupport.integer(type) == 1) {
            interestPayment = interestPayment.divide(ONE.add(rate), mathContext);
        }
        return interestPayment;
    }

    public BigDecimal ipmtNoType(
            BigDecimal rate,
            BigDecimal period,
            BigDecimal periods,
            BigDecimal presentValue,
            BigDecimal futureValue) {
        return ipmtTyped(rate, period, periods, presentValue, futureValue, ZERO);
    }

    public BigDecimal ipmtPresent(BigDecimal rate, BigDecimal period, BigDecimal periods, BigDecimal presentValue) {
        return ipmtNoType(rate, period, periods, presentValue, ZERO);
    }

    public BigDecimal ppmtTyped(
            BigDecimal rate,
            BigDecimal period,
            BigDecimal periods,
            BigDecimal presentValue,
            BigDecimal futureValue,
            BigDecimal type) {
        return pmtTyped(rate, periods, presentValue, futureValue, type)
                .subtract(ipmtTyped(rate, period, periods, presentValue, futureValue, type));
    }

    public BigDecimal ppmtNoType(
            BigDecimal rate,
            BigDecimal period,
            BigDecimal periods,
            BigDecimal presentValue,
            BigDecimal futureValue) {
        return pmtNoType(rate, periods, presentValue, futureValue)
                .subtract(ipmtNoType(rate, period, periods, presentValue, futureValue));
    }

    public BigDecimal ppmtPresent(BigDecimal rate, BigDecimal period, BigDecimal periods, BigDecimal presentValue) {
        return pmtPresent(rate, periods, presentValue)
                .subtract(ipmtPresent(rate, period, periods, presentValue));
    }

    public BigDecimal fvTyped(
            BigDecimal rate,
            BigDecimal periods,
            BigDecimal payment,
            BigDecimal presentValue,
            BigDecimal type) {
        BigDecimal ratePlusOne = rate.add(ONE);
        BigDecimal ratePowerPeriods = BigDecimalMath.pow(ratePlusOne, periods, mathContext);
        return presentValue.multiply(ratePowerPeriods)
                .add(payment.multiply(ONE.add(rate.multiply(BigDecimal.valueOf(BuiltInFunctionSupport.integer(type)))))
                        .multiply(ratePowerPeriods.subtract(ONE))
                        .divide(rate, mathContext))
                .negate();
    }

    public BigDecimal fvNoType(
            BigDecimal rate,
            BigDecimal periods,
            BigDecimal payment,
            BigDecimal presentValue) {
        return fvTyped(rate, periods, payment, presentValue, ZERO);
    }
}
