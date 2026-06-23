package com.runestone.expeval.catalog.functions;

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
 */
public class ExcelFinancialFunctions {

    public static BigDecimal fv(MathContext mc, BigDecimal r, BigDecimal n, BigDecimal y, BigDecimal p, boolean t) {
        if (r.compareTo(ZERO) == 0) {
            return p.add(n.multiply(y)).negate();
        } else {
            BigDecimal r1 = r.add(ONE);
            BigDecimal r1powN = BigDecimalMath.pow(r1, n, mc);
            return ONE.subtract(r1powN)
                    .multiply(t ? r1 : ONE)
                    .multiply(y)
                    .divide(r, mc)
                    .subtract(p.multiply(r1powN));
        }
    }

    public static BigDecimal pv(MathContext mc, BigDecimal r, BigDecimal n, BigDecimal y, BigDecimal f, boolean t) {
        if (r.compareTo(ZERO) == 0) {
            return n.multiply(y).add(f).negate();
        } else {
            BigDecimal r1 = r.add(ONE);
            BigDecimal r1powN = BigDecimalMath.pow(r1, n, mc);
            return ONE.subtract(r1powN)
                    .divide(r, mc)
                    .multiply(t ? r1 : ONE)
                    .multiply(y)
                    .subtract(f)
                    .divide(r1powN, mc);
        }
    }

    public static BigDecimal npv(MathContext mc, BigDecimal r, BigDecimal[] cfs) {
        BigDecimal npv = ZERO;
        BigDecimal r1 = r.add(ONE);
        BigDecimal trate = r1;
        for (BigDecimal cf : cfs) {
            npv = npv.add(cf.divide(trate, mc));
            trate = trate.multiply(r1);
        }
        return npv;
    }

    public static BigDecimal pmt(MathContext mc, BigDecimal r, BigDecimal n, BigDecimal p, BigDecimal f, boolean t) {
        if (r.compareTo(ZERO) == 0) {
            return f.add(p).negate().divide(n, mc);
        } else {
            BigDecimal r1 = r.add(ONE);
            BigDecimal r1powN = BigDecimalMath.pow(r1, n, mc);
            return f.add(p.multiply(r1powN))
                    .multiply(r)
                    .divide(
                            (t ? r1 : ONE).multiply(ONE.subtract(r1powN)),
                            mc
                    );
        }
    }

    public static BigDecimal nper(MathContext mc, BigDecimal r, BigDecimal y, BigDecimal p, BigDecimal f, boolean t) {
        if (r.compareTo(ZERO) == 0) {
            return f.add(p).negate().divide(y, mc);
        } else {
            BigDecimal r1 = r.add(ONE);
            BigDecimal ryr = (t ? r1 : ONE).multiply(y).divide(r, mc);
            BigDecimal ryrMinusF = ryr.subtract(f);
            BigDecimal a1 = BigDecimalMath.log(ryrMinusF.abs(), mc);
            BigDecimal a2 = ryrMinusF.compareTo(ZERO) < 0
                    ? BigDecimalMath.log(p.add(ryr).negate(), mc)
                    : BigDecimalMath.log(p.add(ryr), mc);
            BigDecimal a3 = BigDecimalMath.log(r1, mc);
            return a1.subtract(a2).divide(a3, mc);
        }
    }

    // http://arachnoid.com/lutusp/finance.html
    public static BigDecimal pmt(MathContext mc, BigDecimal r, int nper, BigDecimal pv, BigDecimal fv, int type) {
        BigDecimal r1 = r.add(ONE);
        BigDecimal r1powN = BigDecimalMath.pow(r1, BigDecimal.valueOf(nper), mc);
        BigDecimal numerator = r.negate().multiply(pv.multiply(r1powN).add(fv));
        BigDecimal denominator = ONE.add(r.multiply(BigDecimal.valueOf(type))).multiply(r1powN.subtract(ONE));
        return numerator.divide(denominator, mc);
    }

    public static BigDecimal pmt(MathContext mc, BigDecimal r, int nper, BigDecimal pv, BigDecimal fv) {
        return pmt(mc, r, nper, pv, fv, 0);
    }

    public static BigDecimal pmt(MathContext mc, BigDecimal r, int nper, BigDecimal pv) {
        return pmt(mc, r, nper, pv, ZERO);
    }

    // http://doc.optadata.com/en/dokumentation/application/expression/functions/financial.html
    public static BigDecimal ipmt(MathContext mc, BigDecimal r, int per, int nper, BigDecimal pv, BigDecimal fv, int type) {
        BigDecimal ipmt = fv(mc, r, per - 1, pmt(mc, r, nper, pv, fv, type), pv, type).multiply(r, mc);
        if (type == 1) ipmt = ipmt.divide(ONE.add(r), mc);
        return ipmt;
    }

    public static BigDecimal ipmt(MathContext mc, BigDecimal r, int per, int nper, BigDecimal pv, BigDecimal fv) {
        return ipmt(mc, r, per, nper, pv, fv, 0);
    }

    public static BigDecimal ipmt(MathContext mc, BigDecimal r, int per, int nper, BigDecimal pv) {
        return ipmt(mc, r, per, nper, pv, ZERO);
    }

    public static BigDecimal ppmt(MathContext mc, BigDecimal r, int per, int nper, BigDecimal pv, BigDecimal fv, int type) {
        return pmt(mc, r, nper, pv, fv, type).subtract(ipmt(mc, r, per, nper, pv, fv, type));
    }

    public static BigDecimal ppmt(MathContext mc, BigDecimal r, int per, int nper, BigDecimal pv, BigDecimal fv) {
        return pmt(mc, r, nper, pv, fv).subtract(ipmt(mc, r, per, nper, pv, fv));
    }

    public static BigDecimal ppmt(MathContext mc, BigDecimal r, int per, int nper, BigDecimal pv) {
        return pmt(mc, r, nper, pv).subtract(ipmt(mc, r, per, nper, pv));
    }

    // http://en.wikipedia.org/wiki/Future_value
    public static BigDecimal fv(MathContext mc, BigDecimal r, int nper, BigDecimal pmt, BigDecimal pv, int type) {
        BigDecimal r1 = r.add(ONE);
        BigDecimal r1powN = BigDecimalMath.pow(r1, BigDecimal.valueOf(nper), mc);
        return pv.multiply(r1powN)
                .add(pmt.multiply(ONE.add(r.multiply(BigDecimal.valueOf(type)))).multiply(r1powN.subtract(ONE)).divide(r, mc))
                .negate();
    }

    public static BigDecimal fv(MathContext mc, BigDecimal r, int nper, BigDecimal c, BigDecimal pv) {
        return fv(mc, r, nper, c, pv, 0);
    }
}
