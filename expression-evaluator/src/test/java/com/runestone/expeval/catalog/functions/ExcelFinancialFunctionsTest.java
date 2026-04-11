package com.runestone.expeval.catalog.functions;

import org.assertj.core.api.Assertions;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.math.MathContext;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Excel Financial Functions Tests")
class ExcelFinancialFunctionsTest {

    private static final MathContext MC = MathContext.DECIMAL128;
    private static final Offset<BigDecimal> EPSILON = Assertions.within(new BigDecimal("0.0001"));

    @Nested
    @DisplayName("Future Value (FV) Tests")
    class FutureValueTests {

        @ParameterizedTest(name = "rate={0}, periods={1}, pmt={2}, pv={3}, beginning={4} -> expected={5}")
        @CsvSource({
                "0.05, 10, -100, -1000, false, 2886.6838",
                "0.00, 10, -100, -1000, false, 2000.0000",
                "0.05, 10, -100, -1000, true, 2949.5733"
        })
        @DisplayName("Calculates FV correctly for various scenarios")
        void calculatesFvCorrectly(String r, String n, String y, String p, boolean t, String expected) {
            BigDecimal result = ExcelFinancialFunctions.fv(
                    MC,
                    new BigDecimal(r),
                    new BigDecimal(n),
                    new BigDecimal(y),
                    new BigDecimal(p),
                    t
            );
            assertThat(result).isCloseTo(new BigDecimal(expected), EPSILON);
        }

        @Test
        @DisplayName("Calculates FV using integer overloads")
        void calculatesFvWithIntegerOverloads() {
            BigDecimal r = new BigDecimal("0.05");
            BigDecimal pmt = new BigDecimal("-100");
            BigDecimal pv = new BigDecimal("-1000");
            BigDecimal expected = new BigDecimal("2886.6838");

            assertThat(ExcelFinancialFunctions.fv(MC, r, 10, pmt, pv, 0)).isCloseTo(expected, EPSILON);
            assertThat(ExcelFinancialFunctions.fv(MC, r, 10, pmt, pv)).isCloseTo(expected, EPSILON);
        }
    }

    @Nested
    @DisplayName("Present Value (PV) Tests")
    class PresentValueTests {

        @ParameterizedTest(name = "rate={0}, periods={1}, pmt={2}, fv={3}, beginning={4} -> expected={5}")
        @CsvSource({
                "0.05, 10, -100, 1000, false, 158.2602",
                "0.00, 10, -100, 1000, false, 0.0000",
                "0.05, 10, -100, 1000, true, 196.8689"
        })
        @DisplayName("Calculates PV correctly for various scenarios")
        void calculatesPvCorrectly(String r, String n, String y, String f, boolean t, String expected) {
            BigDecimal result = ExcelFinancialFunctions.pv(
                    MC,
                    new BigDecimal(r),
                    new BigDecimal(n),
                    new BigDecimal(y),
                    new BigDecimal(f),
                    t
            );
            assertThat(result).isCloseTo(new BigDecimal(expected), EPSILON);
        }
    }

    @Nested
    @DisplayName("Net Present Value (NPV) Tests")
    class NetPresentValueTests {

        @Test
        @DisplayName("Calculates NPV for a series of cash flows")
        void calculatesNpvCorrectly() {
            BigDecimal rate = new BigDecimal("0.10");
            BigDecimal[] cashflows = {
                    new BigDecimal("-10000"),
                    new BigDecimal("3000"),
                    new BigDecimal("4200"),
                    new BigDecimal("6800")
            };
            BigDecimal expected = new BigDecimal("1188.4434");

            BigDecimal result = ExcelFinancialFunctions.npv(MC, rate, cashflows);

            assertThat(result).isCloseTo(expected, EPSILON);
        }
    }

    @Nested
    @DisplayName("Periodic Payment (PMT) Tests")
    class PaymentTests {

        @ParameterizedTest(name = "rate={0}, periods={1}, pv={2}, fv={3}, beginning={4} -> expected={5}")
        @CsvSource({
                "0.05, 10, 1000, 0, false, -129.5045",
                "0.00, 10, 1000, 0, false, -100.0000",
                "0.05, 10, 1000, 0, true, -123.3377"
        })
        @DisplayName("Calculates PMT correctly for various scenarios")
        void calculatesPmtCorrectly(String r, String n, String p, String f, boolean t, String expected) {
            BigDecimal result = ExcelFinancialFunctions.pmt(
                    MC,
                    new BigDecimal(r),
                    new BigDecimal(n),
                    new BigDecimal(p),
                    new BigDecimal(f),
                    t
            );
            assertThat(result).isCloseTo(new BigDecimal(expected), EPSILON);
        }

        @Test
        @DisplayName("Calculates PMT using integer overloads")
        void calculatesPmtWithIntegerOverloads() {
            BigDecimal r = new BigDecimal("0.05");
            BigDecimal pv = new BigDecimal("1000");
            BigDecimal expected = new BigDecimal("-129.5045");

            assertThat(ExcelFinancialFunctions.pmt(MC, r, 10, pv, BigDecimal.ZERO, 0)).isCloseTo(expected, EPSILON);
            assertThat(ExcelFinancialFunctions.pmt(MC, r, 10, pv, BigDecimal.ZERO)).isCloseTo(expected, EPSILON);
            assertThat(ExcelFinancialFunctions.pmt(MC, r, 10, pv)).isCloseTo(expected, EPSILON);
        }
    }

    @Nested
    @DisplayName("Number of Periods (NPER) Tests")
    class NumberOfPeriodsTests {

        @ParameterizedTest(name = "rate={0}, pmt={1}, pv={2}, fv={3}, beginning={4} -> expected={5}")
        @CsvSource({
                "0.05, -129.50457, 1000, 0, false, 10.0000",
                "0.00, -100, 1000, 0, false, 10.0000"
        })
        @DisplayName("Calculates NPER correctly")
        void calculatesNperCorrectly(String r, String y, String p, String f, boolean t, String expected) {
            BigDecimal result = ExcelFinancialFunctions.nper(
                    MC,
                    new BigDecimal(r),
                    new BigDecimal(y),
                    new BigDecimal(p),
                    new BigDecimal(f),
                    t
            );
            assertThat(result).isCloseTo(new BigDecimal(expected), EPSILON);
        }
    }

    @Nested
    @DisplayName("Amortization (IPMT/PPMT) Tests")
    class AmortizationTests {

        @Test
        @DisplayName("Calculates Interest Payment (IPMT) correctly")
        void calculatesIpmtCorrectly() {
            BigDecimal r = new BigDecimal("0.05");
            BigDecimal pv = new BigDecimal("1000");

            // First period interest
            assertThat(ExcelFinancialFunctions.ipmt(MC, r, 1, 10, pv, BigDecimal.ZERO, 0))
                    .isCloseTo(new BigDecimal("-50.00"), EPSILON);

            // Second period interest
            assertThat(ExcelFinancialFunctions.ipmt(MC, r, 2, 10, pv, BigDecimal.ZERO, 0))
                    .isCloseTo(new BigDecimal("-46.0247"), EPSILON);
        }

        @Test
        @DisplayName("Calculates Principal Payment (PPMT) correctly")
        void calculatesPpmtCorrectly() {
            BigDecimal r = new BigDecimal("0.05");
            BigDecimal pv = new BigDecimal("1000");

            // First period principal
            assertThat(ExcelFinancialFunctions.ppmt(MC, r, 1, 10, pv, BigDecimal.ZERO, 0))
                    .isCloseTo(new BigDecimal("-79.5045"), EPSILON);

            // Second period principal
            assertThat(ExcelFinancialFunctions.ppmt(MC, r, 2, 10, pv, BigDecimal.ZERO, 0))
                    .isCloseTo(new BigDecimal("-83.4798"), EPSILON);
        }

        @Test
        @DisplayName("Sum of IPMT and PPMT equals PMT")
        void ipmtPlusPpmtEqualsPmt() {
            BigDecimal r = new BigDecimal("0.05");
            int nper = 10;
            BigDecimal pv = new BigDecimal("1000");
            int per = 5;

            BigDecimal pmt = ExcelFinancialFunctions.pmt(MC, r, nper, pv);
            BigDecimal ipmt = ExcelFinancialFunctions.ipmt(MC, r, per, nper, pv);
            BigDecimal ppmt = ExcelFinancialFunctions.ppmt(MC, r, per, nper, pv);

            assertThat(ipmt.add(ppmt)).isCloseTo(pmt, EPSILON);
        }
    }
}
