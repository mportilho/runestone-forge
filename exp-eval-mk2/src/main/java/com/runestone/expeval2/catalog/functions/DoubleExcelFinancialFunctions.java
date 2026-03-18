package com.runestone.expeval2.catalog.functions;

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
public class DoubleExcelFinancialFunctions {

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
    public static double fv(double r, double n, double y, double p, boolean t) {
        if (r == 0) {
            return -1 * (p + (n * y));
        } else {
            double r1 = r + 1;
            return ((1 - Math.pow(r1, n)) * (t ? r1 : 1) * y) / r
                   -
                   p * Math.pow(r1, n);
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
    public static double pv(double r, double n, double y, double f, boolean t) {
        if (r == 0) {
            return -1 * ((n * y) + f);
        } else {
            double r1 = r + 1;
            return (((1 - Math.pow(r1, n)) / r) * (t ? r1 : 1) * y - f)
                   /
                   Math.pow(r1, n);
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
    public static double npv(double r, double[] cfs) {
        double npv = 0;
        double r1 = r + 1;
        double trate = r1;
        for (double cf : cfs) {
            npv += cf / trate;
            trate *= r1;
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
    public static double pmt(double r, double n, double p, double f, boolean t) {
        if (r == 0) {
            return -1 * (f + p) / n;
        } else {
            double r1 = r + 1;
            return (f + p * Math.pow(r1, n)) * r
                   /
                   ((t ? r1 : 1) * (1 - Math.pow(r1, n)));
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
    public static double nper(double r, double y, double p, double f, boolean t) {
        if (r == 0) {
            return -1 * (f + p) / y;
        } else {
            double r1 = r + 1;
            double ryr = (t ? r1 : 1) * y / r;
            double a1 = ((ryr - f) < 0)
                    ? Math.log(f - ryr)
                    : Math.log(ryr - f);
            double a2 = ((ryr - f) < 0)
                    ? Math.log(-p - ryr)
                    : Math.log(p + ryr);
            double a3 = Math.log(r1);
            return (a1 - a2) / a3;
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
    static public double pmt(double r, int nper, double pv, double fv, int type) {
        return -r * (pv * Math.pow(1 + r, nper) + fv) / ((1 + r * type) * (Math.pow(1 + r, nper) - 1));
    }


    /**
     * Overloaded pmt() call omitting type, which defaults to 0.
     *
     * @see #pmt(double, int, double, double, int)
     */
    static public double pmt(double r, int nper, double pv, double fv) {
        return pmt(r, nper, pv, fv, 0);
    }

    /**
     * Overloaded pmt() call omitting fv and type, which both default to 0.
     *
     * @see #pmt(double, int, double, double, int)
     */
    static public double pmt(double r, int nper, double pv) {
        return pmt(r, nper, pv, 0);
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
     * @see #pmt(double, int, double, double, int)
     * @see #fv(double, int, double, double, int)
     */
    // http://doc.optadata.com/en/dokumentation/application/expression/functions/financial.html
    static public double ipmt(double r, int per, int nper, double pv, double fv, int type) {
        double ipmt = fv(r, per - 1, pmt(r, nper, pv, fv, type), pv, type) * r;
        if (type == 1) ipmt /= (1 + r);
        return ipmt;
    }

    static public double ipmt(double r, int per, int nper, double pv, double fv) {
        return ipmt(r, per, nper, pv, fv, 0);
    }

    static public double ipmt(double r, int per, int nper, double pv) {
        return ipmt(r, per, nper, pv, 0);
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
     * @see #pmt(double, int, double, double, int)
     * @see #ipmt(double, int, int, double, double, int)
     */
    static public double ppmt(double r, int per, int nper, double pv, double fv, int type) {
        return pmt(r, nper, pv, fv, type) - ipmt(r, per, nper, pv, fv, type);
    }

    static public double ppmt(double r, int per, int nper, double pv, double fv) {
        return pmt(r, nper, pv, fv) - ipmt(r, per, nper, pv, fv);
    }

    static public double ppmt(double r, int per, int nper, double pv) {
        return pmt(r, nper, pv) - ipmt(r, per, nper, pv);
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
    static public double fv(double r, int nper, double pmt, double pv, int type) {
        return -(pv * Math.pow(1 + r, nper) + pmt * (1 + r * type) * (Math.pow(1 + r, nper) - 1) / r);
    }

    /**
     * Overloaded fv() call omitting type, which defaults to 0.
     *
     * @see #fv(double, int, double, double, int)
     */
    static public double fv(double r, int nper, double c, double pv) {
        return fv(r, nper, c, pv, 0);
    }

}
