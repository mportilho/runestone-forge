package com.runestone.expeval2.catalog.functions;

import ch.obermuhlner.math.big.BigDecimalMath;

import java.math.BigDecimal;
import java.math.MathContext;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;

/**
 * Emulated financial formulas from Microsoft Excel, using BigDecimal class instead of double
 * <p>
 * Based on <a href="https://github.com/apache/poi">Apache Poi</a> implementation, it is adapted to use BigDecimal
 * and formulas considering different values for compounding periods, number of payments and total number of periods
 * <p>
 * This class is a function library for common fiscal functions.
 * <b>Glossary of terms/abbreviations:</b>
 * <br>
 * <ul>
 * <li><em>FV:</em> Future Value</li>
 * <li><em>PV:</em> Present Value</li>
 * <li><em>NPV:</em> Net Present Value</li>
 * <li><em>PMT:</em> (Periodic) Payment</li>
 *
 * </ul>
 * For more info on the terms/abbreviations please use the references below
 * (hyperlinks are subject to change):
 * <p>
 * Online References:
 * <ol>
 * <li>GNU Emacs Calc 2.02 Manual: http://theory.uwinnipeg.ca/gnu/calc/calc_203.html</li>
 * <li>Yahoo Financial Glossary: http://biz.yahoo.com/f/g/nn.html#y</li>
 * <li>MS Excel function reference: http://office.microsoft.com/en-us/assistance/CH062528251033.aspx</li>
 * </ol>
 * <p>
 * Implementation Notes:<p>
 * <p>
 * Symbols used in the formulae that follow:<br>
 * <ul>
 * <li>p: present value</li>
 * <li>f: future value</li>
 * <li>n: number of periods</li>
 * <li>y: payment (in each period)</li>
 * <li>r: rate</li>
 * <li>^: the power operator (NOT the java bitwise XOR operator!)</li>
 * </ul>
 * [From MS Excel function reference] Following are some of the key formulas
 * that are used in this implementation:
 * <pre>
 * p(1+r)^n + y(1+rt)((1+r)^n-1)/r + f=0   ...{when r!=0}
 * ny + p + f=0                            ...{when r=0}
 * </pre>
 */
public class ExcelFinancialFunctions {

    private static final MathContext MC = MathContext.DECIMAL128;

    /**
     * Future value of an amount given the number of payments, rate, amount
     * of individual payment, present value and boolean value indicating whether
     * payments are due at the beginning of period
     * (false =&gt; payments are due at end of period)
     *
     * @param r rate
     * @param n num of periods
     * @param y pmt per period
     * @param p present value
     * @param t type (true=pmt at beginning of period, false=pmt at end of period)
     */
    public static BigDecimal fv(BigDecimal r, BigDecimal n, BigDecimal y, BigDecimal p, boolean t) {
        if (r.compareTo(ZERO) == 0) {
            return p.add(n.multiply(y)).negate();
        } else {
            BigDecimal r1 = r.add(ONE);
            BigDecimal r1powN = BigDecimalMath.pow(r1, n, MC);
            return ONE.subtract(r1powN)
                    .multiply(t ? r1 : ONE)
                    .multiply(y)
                    .divide(r, MC)
                    .subtract(p.multiply(r1powN));
        }
    }

    /**
     * Present value of an amount given the number of future payments, rate, amount
     * of individual payment, future value and boolean value indicating whether
     * payments are due at the beginning of period
     * (false =&gt; payments are due at end of period)
     *
     * @param r rate
     * @param n num of periods
     * @param y pmt per period
     * @param f future value
     * @param t type (true=pmt at beginning of period, false=pmt at end of period)
     */
    public static BigDecimal pv(BigDecimal r, BigDecimal n, BigDecimal y, BigDecimal f, boolean t) {
        if (r.compareTo(ZERO) == 0) {
            return n.multiply(y).add(f).negate();
        } else {
            BigDecimal r1 = r.add(ONE);
            BigDecimal r1powN = BigDecimalMath.pow(r1, n, MC);
            return ONE.subtract(r1powN)
                    .divide(r, MC)
                    .multiply(t ? r1 : ONE)
                    .multiply(y)
                    .subtract(f)
                    .divide(r1powN, MC);
        }
    }

    /**
     * calculates the Net Present Value of a principal amount
     * given the discount rate and a sequence of cash flows
     * (supplied as an array). If the amounts are income the value should
     * be positive, else if they are payments and not income, the
     * value should be negative.
     *
     * @param r   rate
     * @param cfs cashflow amounts
     */
    public static BigDecimal npv(BigDecimal r, BigDecimal[] cfs) {
        BigDecimal npv = ZERO;
        BigDecimal r1 = r.add(ONE);
        BigDecimal trate = r1;
        for (BigDecimal cf : cfs) {
            npv = npv.add(cf.divide(trate, MC));
            trate = trate.multiply(r1);
        }
        return npv;
    }

    /**
     *
     * @param r rate
     * @param n num of periods
     * @param p present value
     * @param f future value
     * @param t type (true=pmt at beginning of period, false=pmt at end of period)
     */
    public static BigDecimal pmt(BigDecimal r, BigDecimal n, BigDecimal p, BigDecimal f, boolean t) {
        if (r.compareTo(ZERO) == 0) {
            return f.add(p).negate().divide(n, MC);
        } else {
            BigDecimal r1 = r.add(ONE);
            BigDecimal r1powN = BigDecimalMath.pow(r1, n, MC);
            return f.add(p.multiply(r1powN))
                    .multiply(r)
                    .divide(
                            (t ? r1 : ONE).multiply(ONE.subtract(r1powN)),
                            MC
                    );
        }
    }

    /**
     *
     * @param r rate
     * @param y pmt per period
     * @param p present value
     * @param f future value
     * @param t type (true=pmt at beginning of period, false=pmt at end of period)
     */
    public static BigDecimal nper(BigDecimal r, BigDecimal y, BigDecimal p, BigDecimal f, boolean t) {
        if (r.compareTo(ZERO) == 0) {
            return f.add(p).negate().divide(y, MC);
        } else {
            BigDecimal r1 = r.add(ONE);
            BigDecimal ryr = (t ? r1 : ONE).multiply(y).divide(r, MC);
            BigDecimal ryrMinusF = ryr.subtract(f);
            BigDecimal a1 = BigDecimalMath.log(ryrMinusF.abs(), MC);
            BigDecimal a2 = ryrMinusF.compareTo(ZERO) < 0
                    ? BigDecimalMath.log(p.add(ryr).negate(), MC)
                    : BigDecimalMath.log(p.add(ryr), MC);
            BigDecimal a3 = BigDecimalMath.log(r1, MC);
            return a1.subtract(a2).divide(a3, MC);
        }
    }

    /**
     * Emulates Excel/Calc's PMT(interest_rate, number_payments, PV, FV, Type)
     * function, which calculates the payments for a loan or the future value of an investment
     *
     * @param r    - periodic interest rate represented as a decimal.
     * @param nper - number of total payments / periods.
     * @param pv   - present value -- borrowed or invested principal.
     * @param fv   - future value of loan or annuity.
     * @param type - when payment is made: beginning of period is 1; end, 0.
     * @return <code>double</code> representing periodic payment amount.
     */
    // http://arachnoid.com/lutusp/finance.html
    static public BigDecimal pmt(BigDecimal r, int nper, BigDecimal pv, BigDecimal fv, int type) {
        BigDecimal r1 = r.add(ONE);
        BigDecimal r1powN = BigDecimalMath.pow(r1, BigDecimal.valueOf(nper), MC);
        BigDecimal numerator = r.negate().multiply(pv.multiply(r1powN).add(fv));
        BigDecimal denominator = ONE.add(r.multiply(BigDecimal.valueOf(type))).multiply(r1powN.subtract(ONE));
        return numerator.divide(denominator, MC);
    }


    /**
     * Overloaded pmt() call omitting type, which defaults to 0.
     *
     * @see #pmt(BigDecimal, int, BigDecimal, BigDecimal, int)
     */
    static public BigDecimal pmt(BigDecimal r, int nper, BigDecimal pv, BigDecimal fv) {
        return pmt(r, nper, pv, fv, 0);
    }

    /**
     * Overloaded pmt() call omitting fv and type, which both default to 0.
     *
     * @see #pmt(BigDecimal, int, BigDecimal, BigDecimal, int)
     */
    static public BigDecimal pmt(BigDecimal r, int nper, BigDecimal pv) {
        return pmt(r, nper, pv, ZERO);
    }


    /**
     * Emulates Excel/Calc's IPMT(interest_rate, period, number_payments, PV,
     * FV, Type) function, which calculates the portion of the payment at a
     * given period that is the interest on previous balance.
     *
     * @param r    - periodic interest rate represented as a decimal.
     * @param per  - period (payment number) to check value at.
     * @param nper - number of total payments / periods.
     * @param pv   - present value -- borrowed or invested principal.
     * @param fv   - future value of loan or annuity.
     * @param type - when payment is made: beginning of period is 1; end, 0.
     * @return <code>double</code> representing interest portion of payment.
     * @see #pmt(BigDecimal, int, BigDecimal, BigDecimal, int)
     * @see #fv(BigDecimal, int, BigDecimal, BigDecimal, int)
     */
    // http://doc.optadata.com/en/dokumentation/application/expression/functions/financial.html
    static public BigDecimal ipmt(BigDecimal r, int per, int nper, BigDecimal pv, BigDecimal fv, int type) {
        BigDecimal ipmt = fv(r, per - 1, pmt(r, nper, pv, fv, type), pv, type).multiply(r, MC);
        if (type == 1) ipmt = ipmt.divide(ONE.add(r), MC);
        return ipmt;
    }

    static public BigDecimal ipmt(BigDecimal r, int per, int nper, BigDecimal pv, BigDecimal fv) {
        return ipmt(r, per, nper, pv, fv, 0);
    }

    static public BigDecimal ipmt(BigDecimal r, int per, int nper, BigDecimal pv) {
        return ipmt(r, per, nper, pv, ZERO);
    }

    /**
     * Emulates Excel/Calc's PPMT(interest_rate, period, number_payments, PV,
     * FV, Type) function, which calculates the portion of the payment at a
     * given period that will apply to principal.
     *
     * @param r    - periodic interest rate represented as a decimal.
     * @param per  - period (payment number) to check value at.
     * @param nper - number of total payments / periods.
     * @param pv   - present value -- borrowed or invested principal.
     * @param fv   - future value of loan or annuity.
     * @param type - when payment is made: beginning of period is 1; end, 0.
     * @return <code>double</code> representing principal portion of payment.
     * @see #pmt(BigDecimal, int, BigDecimal, BigDecimal, int)
     * @see #ipmt(BigDecimal, int, int, BigDecimal, BigDecimal, int)
     */
    static public BigDecimal ppmt(BigDecimal r, int per, int nper, BigDecimal pv, BigDecimal fv, int type) {
        return pmt(r, nper, pv, fv, type).subtract(ipmt(r, per, nper, pv, fv, type));
    }

    static public BigDecimal ppmt(BigDecimal r, int per, int nper, BigDecimal pv, BigDecimal fv) {
        return pmt(r, nper, pv, fv).subtract(ipmt(r, per, nper, pv, fv));
    }

    static public BigDecimal ppmt(BigDecimal r, int per, int nper, BigDecimal pv) {
        return pmt(r, nper, pv).subtract(ipmt(r, per, nper, pv));
    }

    /**
     * Emulates Excel/Calc's FV(interest_rate, number_payments, payment, PV,
     * Type) function, which calculates future value or principal at period N.
     *
     * @param r    - periodic interest rate represented as a decimal.
     * @param nper - number of total payments / periods.
     * @param pmt  - periodic payment amount.
     * @param pv   - present value -- borrowed or invested principal.
     * @param type - when payment is made: beginning of period is 1; end, 0.
     * @return <code>double</code> representing future principal value.
     */
    //http://en.wikipedia.org/wiki/Future_value
    static public BigDecimal fv(BigDecimal r, int nper, BigDecimal pmt, BigDecimal pv, int type) {
        BigDecimal r1 = r.add(ONE);
        BigDecimal r1powN = BigDecimalMath.pow(r1, BigDecimal.valueOf(nper), MC);
        return pv.multiply(r1powN)
                .add(pmt.multiply(ONE.add(r.multiply(BigDecimal.valueOf(type)))).multiply(r1powN.subtract(ONE)).divide(r, MC))
                .negate();
    }

    /**
     * Overloaded fv() call omitting type, which defaults to 0.
     *
     * @see #fv(BigDecimal, int, BigDecimal, BigDecimal, int)
     */
    static public BigDecimal fv(BigDecimal r, int nper, BigDecimal c, BigDecimal pv) {
        return fv(r, nper, c, pv, 0);
    }

}
