package com.runestone.expeval_mk3.api;

import ch.obermuhlner.math.big.BigDecimalMath;

import java.math.BigDecimal;
import java.math.MathContext;

final class StandardFinancialFunctions {

    private StandardFinancialFunctions() {
    }

    public static BigDecimal fv(
            MathContext mathContext,
            BigDecimal rate,
            BigDecimal periodCount,
            BigDecimal payment,
            BigDecimal presentValue,
            boolean paymentAtBeginning) {
        if (rate.compareTo(BigDecimal.ZERO) == 0) {
            return presentValue.add(periodCount.multiply(payment, mathContext), mathContext).negate(mathContext);
        }
        BigDecimal ratePlusOne = rate.add(BigDecimal.ONE, mathContext);
        BigDecimal ratePlusOnePowPeriods = BigDecimalMath.pow(ratePlusOne, periodCount, mathContext);
        return BigDecimal.ONE.subtract(ratePlusOnePowPeriods, mathContext)
                .multiply(paymentAtBeginning ? ratePlusOne : BigDecimal.ONE, mathContext)
                .multiply(payment, mathContext)
                .divide(rate, mathContext)
                .subtract(presentValue.multiply(ratePlusOnePowPeriods, mathContext), mathContext);
    }

    public static BigDecimal pv(
            MathContext mathContext,
            BigDecimal rate,
            BigDecimal periodCount,
            BigDecimal payment,
            BigDecimal futureValue,
            boolean paymentAtBeginning) {
        if (rate.compareTo(BigDecimal.ZERO) == 0) {
            return periodCount.multiply(payment, mathContext).add(futureValue, mathContext).negate(mathContext);
        }
        BigDecimal ratePlusOne = rate.add(BigDecimal.ONE, mathContext);
        BigDecimal ratePlusOnePowPeriods = BigDecimalMath.pow(ratePlusOne, periodCount, mathContext);
        return BigDecimal.ONE.subtract(ratePlusOnePowPeriods, mathContext)
                .divide(rate, mathContext)
                .multiply(paymentAtBeginning ? ratePlusOne : BigDecimal.ONE, mathContext)
                .multiply(payment, mathContext)
                .subtract(futureValue, mathContext)
                .divide(ratePlusOnePowPeriods, mathContext);
    }

    public static BigDecimal npv(MathContext mathContext, BigDecimal rate, BigDecimal[] cashFlows) {
        BigDecimal npv = BigDecimal.ZERO;
        BigDecimal ratePlusOne = rate.add(BigDecimal.ONE, mathContext);
        BigDecimal discountRate = ratePlusOne;
        for (BigDecimal cashFlow : cashFlows) {
            npv = npv.add(cashFlow.divide(discountRate, mathContext), mathContext);
            discountRate = discountRate.multiply(ratePlusOne, mathContext);
        }
        return npv;
    }

    public static BigDecimal pmt(
            MathContext mathContext,
            BigDecimal rate,
            BigDecimal periodCount,
            BigDecimal presentValue,
            BigDecimal futureValue,
            boolean paymentAtBeginning) {
        if (rate.compareTo(BigDecimal.ZERO) == 0) {
            return futureValue.add(presentValue, mathContext).negate(mathContext).divide(periodCount, mathContext);
        }
        BigDecimal ratePlusOne = rate.add(BigDecimal.ONE, mathContext);
        BigDecimal ratePlusOnePowPeriods = BigDecimalMath.pow(ratePlusOne, periodCount, mathContext);
        return futureValue.add(presentValue.multiply(ratePlusOnePowPeriods, mathContext), mathContext)
                .multiply(rate, mathContext)
                .divide((paymentAtBeginning ? ratePlusOne : BigDecimal.ONE)
                                .multiply(BigDecimal.ONE.subtract(ratePlusOnePowPeriods, mathContext), mathContext),
                        mathContext);
    }

    public static BigDecimal nper(
            MathContext mathContext,
            BigDecimal rate,
            BigDecimal payment,
            BigDecimal presentValue,
            BigDecimal futureValue,
            boolean paymentAtBeginning) {
        if (rate.compareTo(BigDecimal.ZERO) == 0) {
            return futureValue.add(presentValue, mathContext).negate(mathContext).divide(payment, mathContext);
        }
        BigDecimal ratePlusOne = rate.add(BigDecimal.ONE, mathContext);
        BigDecimal ratePaymentRatio = (paymentAtBeginning ? ratePlusOne : BigDecimal.ONE)
                .multiply(payment, mathContext)
                .divide(rate, mathContext);
        BigDecimal ratePaymentRatioMinusFuture = ratePaymentRatio.subtract(futureValue, mathContext);
        BigDecimal first = BigDecimalMath.log(ratePaymentRatioMinusFuture.abs(mathContext), mathContext);
        BigDecimal second = ratePaymentRatioMinusFuture.compareTo(BigDecimal.ZERO) < 0
                ? BigDecimalMath.log(presentValue.add(ratePaymentRatio, mathContext).negate(mathContext), mathContext)
                : BigDecimalMath.log(presentValue.add(ratePaymentRatio, mathContext), mathContext);
        BigDecimal third = BigDecimalMath.log(ratePlusOne, mathContext);
        return first.subtract(second, mathContext).divide(third, mathContext);
    }

    public static BigDecimal pmt(
            MathContext mathContext,
            BigDecimal rate,
            BigDecimal periodCount,
            BigDecimal presentValue,
            BigDecimal futureValue,
            BigDecimal paymentType) {
        BigDecimal ratePlusOne = rate.add(BigDecimal.ONE, mathContext);
        BigDecimal ratePlusOnePowPeriods = BigDecimalMath.pow(ratePlusOne, periodCount, mathContext);
        BigDecimal numerator = rate.negate(mathContext)
                .multiply(presentValue.multiply(ratePlusOnePowPeriods, mathContext).add(futureValue, mathContext), mathContext);
        BigDecimal denominator = BigDecimal.ONE.add(rate.multiply(paymentType, mathContext), mathContext)
                .multiply(ratePlusOnePowPeriods.subtract(BigDecimal.ONE, mathContext), mathContext);
        return numerator.divide(denominator, mathContext);
    }

    public static BigDecimal pmt(
            MathContext mathContext,
            BigDecimal rate,
            BigDecimal periodCount,
            BigDecimal presentValue,
            BigDecimal futureValue) {
        return pmt(mathContext, rate, periodCount, presentValue, futureValue, BigDecimal.ZERO);
    }

    public static BigDecimal pmt(MathContext mathContext, BigDecimal rate, BigDecimal periodCount, BigDecimal presentValue) {
        return pmt(mathContext, rate, periodCount, presentValue, BigDecimal.ZERO);
    }

    public static BigDecimal ipmt(
            MathContext mathContext,
            BigDecimal rate,
            BigDecimal period,
            BigDecimal periodCount,
            BigDecimal presentValue,
            BigDecimal futureValue,
            BigDecimal paymentType) {
        BigDecimal interestPayment = fv(
                mathContext,
                rate,
                period.subtract(BigDecimal.ONE),
                pmt(mathContext, rate, periodCount, presentValue, futureValue, paymentType),
                presentValue,
                paymentType)
                .multiply(rate, mathContext);
        return paymentType.compareTo(BigDecimal.ONE) == 0
                ? interestPayment.divide(BigDecimal.ONE.add(rate, mathContext), mathContext)
                : interestPayment;
    }

    public static BigDecimal ipmt(
            MathContext mathContext,
            BigDecimal rate,
            BigDecimal period,
            BigDecimal periodCount,
            BigDecimal presentValue,
            BigDecimal futureValue) {
        return ipmt(mathContext, rate, period, periodCount, presentValue, futureValue, BigDecimal.ZERO);
    }

    public static BigDecimal ipmt(
            MathContext mathContext,
            BigDecimal rate,
            BigDecimal period,
            BigDecimal periodCount,
            BigDecimal presentValue) {
        return ipmt(mathContext, rate, period, periodCount, presentValue, BigDecimal.ZERO);
    }

    public static BigDecimal ppmt(
            MathContext mathContext,
            BigDecimal rate,
            BigDecimal period,
            BigDecimal periodCount,
            BigDecimal presentValue,
            BigDecimal futureValue,
            BigDecimal paymentType) {
        return pmt(mathContext, rate, periodCount, presentValue, futureValue, paymentType)
                .subtract(ipmt(mathContext, rate, period, periodCount, presentValue, futureValue, paymentType), mathContext);
    }

    public static BigDecimal ppmt(
            MathContext mathContext,
            BigDecimal rate,
            BigDecimal period,
            BigDecimal periodCount,
            BigDecimal presentValue,
            BigDecimal futureValue) {
        return pmt(mathContext, rate, periodCount, presentValue, futureValue)
                .subtract(ipmt(mathContext, rate, period, periodCount, presentValue, futureValue), mathContext);
    }

    public static BigDecimal ppmt(
            MathContext mathContext,
            BigDecimal rate,
            BigDecimal period,
            BigDecimal periodCount,
            BigDecimal presentValue) {
        return pmt(mathContext, rate, periodCount, presentValue)
                .subtract(ipmt(mathContext, rate, period, periodCount, presentValue), mathContext);
    }

    public static BigDecimal fv(
            MathContext mathContext,
            BigDecimal rate,
            BigDecimal periodCount,
            BigDecimal payment,
            BigDecimal presentValue,
            BigDecimal paymentType) {
        BigDecimal ratePlusOne = rate.add(BigDecimal.ONE, mathContext);
        BigDecimal ratePlusOnePowPeriods = BigDecimalMath.pow(ratePlusOne, periodCount, mathContext);
        return presentValue.multiply(ratePlusOnePowPeriods, mathContext)
                .add(payment.multiply(BigDecimal.ONE.add(rate.multiply(paymentType, mathContext), mathContext), mathContext)
                        .multiply(ratePlusOnePowPeriods.subtract(BigDecimal.ONE, mathContext), mathContext)
                        .divide(rate, mathContext), mathContext)
                .negate(mathContext);
    }

    public static BigDecimal fv(
            MathContext mathContext,
            BigDecimal rate,
            BigDecimal periodCount,
            BigDecimal payment,
            BigDecimal presentValue) {
        return fv(mathContext, rate, periodCount, payment, presentValue, BigDecimal.ZERO);
    }
}
