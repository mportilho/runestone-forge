package com.runestone.expeval2.api;

import com.runestone.expeval2.environment.ExpressionEnvironment;
import org.assertj.core.api.Assertions;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

// npv(BigDecimal r, BigDecimal[] cfs) is excluded: the array parameter cannot be coerced
// from VectorValue to BigDecimal[] via the expression API. See runtime-internals.md §5.
//
// Overload disambiguation for fv and pmt:
//   boolean 5th arg  → fv(BigDecimal,BigDecimal,BigDecimal,BigDecimal,boolean)   ← BigDecimal overload
//   numeric 5th arg  → fv(BigDecimal,int,BigDecimal,BigDecimal,int)              ← int overload
// The SemanticResolver selects by matching BOOLEAN vs NUMBER for the 5th parameter type.
// See runtime-internals.md §6.
@DisplayName("ExcelFinancialFunctions scalar overloads via MathExpression/LogicalExpression API")
class ExcelFinancialFunctionsExpressionTest {

    private static final Offset<BigDecimal> EPSILON = Assertions.within(new BigDecimal("0.0001"));
    private static final ExpressionEnvironment ENV = ExpressionEnvironment.builder().addExcelFunctions().build();

    @Nested
    @DisplayName("fv — future value")
    class FutureValue {

        @Test
        @DisplayName("fv with boolean type=false (BigDecimal overload): standard end-of-period payments")
        void fvWithBooleanTypeFalse() {
            // fv(0.05, 10, -100, -1000, false) ≈ 2886.6838
            BigDecimal result = MathExpression.compile("fv(0.05, 10, -100, -1000, false)", ENV).compute();
            assertThat(result).isCloseTo(new BigDecimal("2886.6838"), EPSILON);
        }

        @Test
        @DisplayName("fv with boolean type=true (BigDecimal overload): beginning-of-period payments")
        void fvWithBooleanTypeTrue() {
            // fv(0.05, 10, -100, -1000, true) ≈ 2949.5733
            BigDecimal result = MathExpression.compile("fv(0.05, 10, -100, -1000, true)", ENV).compute();
            assertThat(result).isCloseTo(new BigDecimal("2949.5733"), EPSILON);
        }

        @Test
        @DisplayName("fv with int type=0 (int overload): disambiguates to integer-parameter overload")
        void fvWithIntTypeZero() {
            // fv(0.05, 10, -100, -1000, 0) → int overload → same result as boolean false
            BigDecimal result = MathExpression.compile("fv(0.05, 10, -100, -1000, 0)", ENV).compute();
            assertThat(result).isCloseTo(new BigDecimal("2886.6838"), EPSILON);
        }

        @Test
        @DisplayName("fv with zero rate: collapses to arithmetic sum")
        void fvWithZeroRate() {
            // fv(0, 10, -100, -1000, false) = -(1000 + 10*100) = 2000
            BigDecimal result = MathExpression.compile("fv(0, 10, -100, -1000, false)", ENV).compute();
            assertThat(result).isCloseTo(new BigDecimal("2000"), EPSILON);
        }

        @Test
        @DisplayName("fv via variables")
        void fvViaVariables() {
            BigDecimal result = MathExpression.compile("fv(rate, nper, pmt, pv, false)", ENV)
                    .setValue("rate", new BigDecimal("0.05"))
                    .setValue("nper", new BigDecimal("10"))
                    .setValue("pmt", new BigDecimal("-100"))
                    .setValue("pv", new BigDecimal("-1000"))
                    .compute();
            assertThat(result).isCloseTo(new BigDecimal("2886.6838"), EPSILON);
        }

        @Test
        @DisplayName("fv result in logical expression: fv > 2000 → true")
        void fvInLogicalExpression() {
            boolean result = LogicalExpression.compile("fv(0.05, 10, -100, -1000, false) > 2000", ENV).compute();
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("pv — present value")
    class PresentValue {

        @Test
        @DisplayName("pv with boolean type=false: standard end-of-period discounting")
        void pvWithBooleanTypeFalse() {
            // pv(0.05, 10, -100, 1000, false) ≈ 158.2602
            BigDecimal result = MathExpression.compile("pv(0.05, 10, -100, 1000, false)", ENV).compute();
            assertThat(result).isCloseTo(new BigDecimal("158.2602"), EPSILON);
        }

        @Test
        @DisplayName("pv with boolean type=true: beginning-of-period discounting")
        void pvWithBooleanTypeTrue() {
            // pv(0.05, 10, -100, 1000, true) ≈ 196.8689
            BigDecimal result = MathExpression.compile("pv(0.05, 10, -100, 1000, true)", ENV).compute();
            assertThat(result).isCloseTo(new BigDecimal("196.8689"), EPSILON);
        }

        @Test
        @DisplayName("pv with zero rate: collapses to arithmetic sum")
        void pvWithZeroRate() {
            // pv(0, 10, -100, 1000, false) = -(10*(-100) + 1000) = 0
            BigDecimal result = MathExpression.compile("pv(0, 10, -100, 1000, false)", ENV).compute();
            assertThat(result).isCloseTo(BigDecimal.ZERO, EPSILON);
        }

        @Test
        @DisplayName("pv via variables")
        void pvViaVariables() {
            BigDecimal result = MathExpression.compile("pv(rate, nper, pmt, fv, false)", ENV)
                    .setValue("rate", new BigDecimal("0.05"))
                    .setValue("nper", new BigDecimal("10"))
                    .setValue("pmt", new BigDecimal("-100"))
                    .setValue("fv", new BigDecimal("1000"))
                    .compute();
            assertThat(result).isCloseTo(new BigDecimal("158.2602"), EPSILON);
        }

        @Test
        @DisplayName("pv result in logical expression: pv > 0 → true")
        void pvInLogicalExpression() {
            boolean result = LogicalExpression.compile("pv(0.05, 10, -100, 1000, false) > 0", ENV).compute();
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("pmt — periodic payment")
    class PeriodicPayment {

        @Test
        @DisplayName("pmt with boolean type=false: end-of-period payment on 1000 loan at 5% for 10 periods")
        void pmtWithBooleanTypeFalse() {
            // pmt(0.05, 10, 1000, 0, false) ≈ -129.5045
            BigDecimal result = MathExpression.compile("pmt(0.05, 10, 1000, 0, false)", ENV).compute();
            assertThat(result).isCloseTo(new BigDecimal("-129.5045"), EPSILON);
        }

        @Test
        @DisplayName("pmt with boolean type=true: beginning-of-period payment")
        void pmtWithBooleanTypeTrue() {
            // pmt(0.05, 10, 1000, 0, true) ≈ -123.3377
            BigDecimal result = MathExpression.compile("pmt(0.05, 10, 1000, 0, true)", ENV).compute();
            assertThat(result).isCloseTo(new BigDecimal("-123.3377"), EPSILON);
        }

        @Test
        @DisplayName("pmt with int type=0 (int overload): disambiguates to integer-parameter overload")
        void pmtWithIntTypeZero() {
            // pmt(0.05, 10, 1000, 0, 0) → int overload → same result as boolean false
            BigDecimal result = MathExpression.compile("pmt(0.05, 10, 1000, 0, 0)", ENV).compute();
            assertThat(result).isCloseTo(new BigDecimal("-129.5045"), EPSILON);
        }

        @Test
        @DisplayName("pmt with zero rate: equal installments")
        void pmtWithZeroRate() {
            // pmt(0, 10, 1000, 0, false) = -100
            BigDecimal result = MathExpression.compile("pmt(0, 10, 1000, 0, false)", ENV).compute();
            assertThat(result).isCloseTo(new BigDecimal("-100"), EPSILON);
        }

        @Test
        @DisplayName("pmt via variables")
        void pmtViaVariables() {
            BigDecimal result = MathExpression.compile("pmt(rate, nper, pv, fv, false)", ENV)
                    .setValue("rate", new BigDecimal("0.05"))
                    .setValue("nper", new BigDecimal("10"))
                    .setValue("pv", new BigDecimal("1000"))
                    .setValue("fv", new BigDecimal("0"))
                    .compute();
            assertThat(result).isCloseTo(new BigDecimal("-129.5045"), EPSILON);
        }

        @Test
        @DisplayName("pmt result in logical expression: payment is negative → true")
        void pmtInLogicalExpression() {
            boolean result = LogicalExpression.compile("pmt(0.05, 10, 1000, 0, false) < 0", ENV).compute();
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("nper — number of periods")
    class NumberOfPeriods {

        @Test
        @DisplayName("nper: periods to repay 1000 at 5% with payments of -129.50457 ≈ 10")
        void nperStandardLoan() {
            // nper(0.05, -129.50457, 1000, 0, false) ≈ 10.0
            BigDecimal result = MathExpression.compile("nper(0.05, -129.50457, 1000, 0, false)", ENV).compute();
            assertThat(result).isCloseTo(new BigDecimal("10.0"), EPSILON);
        }

        @Test
        @DisplayName("nper with zero rate: collapses to arithmetic division")
        void nperWithZeroRate() {
            // nper(0, -100, 1000, 0, false) = 10.0
            BigDecimal result = MathExpression.compile("nper(0, -100, 1000, 0, false)", ENV).compute();
            assertThat(result).isCloseTo(new BigDecimal("10.0"), EPSILON);
        }

        @Test
        @DisplayName("nper via variables")
        void nperViaVariables() {
            BigDecimal result = MathExpression.compile("nper(rate, pmt, pv, fv, false)", ENV)
                    .setValue("rate", new BigDecimal("0.05"))
                    .setValue("pmt", new BigDecimal("-129.50457"))
                    .setValue("pv", new BigDecimal("1000"))
                    .setValue("fv", new BigDecimal("0"))
                    .compute();
            assertThat(result).isCloseTo(new BigDecimal("10.0"), EPSILON);
        }

        @Test
        @DisplayName("nper result in logical expression: nper ≈ 10 → between 9.9 and 10.1")
        void nperInLogicalExpression() {
            boolean result = LogicalExpression.compile(
                    "nper(0.05, -129.50457, 1000, 0, false) > 9.9 and nper(0.05, -129.50457, 1000, 0, false) < 10.1",
                    ENV).compute();
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("fv and pv as inverse operations")
    class InverseRelationship {

        @Test
        @DisplayName("pv of fv returns approximately the original principal")
        void pvOfFvIsOriginalPrincipal() {
            // invest 1000 for 10 periods at 5%, then discount back
            BigDecimal fvResult = MathExpression.compile("fv(0.05, 10, 0, -1000, false)", ENV).compute();
            BigDecimal pvResult = MathExpression.compile("pv(rate, 10, 0, fv, false)", ENV)
                    .setValue("rate", new BigDecimal("0.05"))
                    .setValue("fv", fvResult)
                    .compute();
            assertThat(pvResult).isCloseTo(new BigDecimal("-1000"), EPSILON);
        }
    }
}
