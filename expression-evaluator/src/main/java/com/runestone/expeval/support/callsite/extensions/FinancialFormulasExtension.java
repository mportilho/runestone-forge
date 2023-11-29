package com.runestone.expeval.support.callsite.extensions;

import com.runestone.expeval.support.callsite.OperationCallSite;
import com.runestone.expeval.support.functions.math.ExcelFinancialFunctions;
import com.runestone.expeval.support.functions.math.xirr.Transaction;
import com.runestone.expeval.support.functions.math.xirr.Xirr;

import java.lang.invoke.MethodType;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

class FinancialFormulasExtension {

    private FinancialFormulasExtension() {
    }

    static Map<String, OperationCallSite> financialFunctionsFactory() {
        OperationCallSite callSite;
        Map<String, OperationCallSite> extensions = new HashMap<>();

        callSite = new OperationCallSite("eir",
                MethodType.methodType(BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class),
                (context, parameters) -> ExcelFinancialFunctions.eir(
                        (BigDecimal) parameters[0],
                        (BigDecimal) parameters[1],
                        (BigDecimal) parameters[2],
                        context.mathContext()
                ));
        extensions.put(callSite.getKeyName(), callSite);

        callSite = new OperationCallSite("r",
                MethodType.methodType(BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class),
                (context, parameters) -> ExcelFinancialFunctions.r(
                        (BigDecimal) parameters[0],
                        (BigDecimal) parameters[1],
                        (BigDecimal) parameters[2],
                        (BigDecimal) parameters[3],
                        context.mathContext()
                ));
        extensions.put(callSite.getKeyName(), callSite);

        callSite = new OperationCallSite("pv",
                MethodType.methodType(BigDecimal.class,
                        BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class),
                (context, parameters) -> ExcelFinancialFunctions.pv(
                        (BigDecimal) parameters[0],
                        (BigDecimal) parameters[1],
                        (BigDecimal) parameters[2],
                        (BigDecimal) parameters[3],
                        (BigDecimal) parameters[4],
                        (BigDecimal) parameters[5],
                        context.mathContext()
                ));
        extensions.put(callSite.getKeyName(), callSite);

        callSite = new OperationCallSite("pv",
                MethodType.methodType(BigDecimal.class,
                        BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class),
                (context, parameters) -> ExcelFinancialFunctions.pv(
                        (BigDecimal) parameters[0],
                        (BigDecimal) parameters[1],
                        (BigDecimal) parameters[2],
                        (BigDecimal) parameters[3],
                        (BigDecimal) parameters[4],
                        context.mathContext()
                ));
        extensions.put(callSite.getKeyName(), callSite);

        callSite = new OperationCallSite("pv",
                MethodType.methodType(BigDecimal.class,
                        BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class),
                (context, parameters) -> ExcelFinancialFunctions.pv(
                        (BigDecimal) parameters[0],
                        (BigDecimal) parameters[1],
                        (BigDecimal) parameters[2],
                        (BigDecimal) parameters[3],
                        context.mathContext()
                ));
        extensions.put(callSite.getKeyName(), callSite);

        callSite = new OperationCallSite("pv",
                MethodType.methodType(BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class),
                (context, parameters) -> ExcelFinancialFunctions.pv(
                        (BigDecimal) parameters[0],
                        (BigDecimal) parameters[1],
                        (BigDecimal) parameters[2],
                        context.mathContext()
                ));
        extensions.put(callSite.getKeyName(), callSite);

        callSite = new OperationCallSite("nper",
                MethodType.methodType(BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class),
                (context, parameters) -> ExcelFinancialFunctions.nper(
                        (BigDecimal) parameters[0],
                        (BigDecimal) parameters[1],
                        (BigDecimal) parameters[2],
                        (BigDecimal) parameters[3],
                        context.mathContext()
                ));
        extensions.put(callSite.getKeyName(), callSite);

        callSite = new OperationCallSite("fv",
                MethodType.methodType(BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class),
                (context, parameters) -> ExcelFinancialFunctions.fv(
                        (BigDecimal) parameters[0],
                        (BigDecimal) parameters[1],
                        (BigDecimal) parameters[2],
                        (BigDecimal) parameters[3],
                        (BigDecimal) parameters[4],
                        (BigDecimal) parameters[5],
                        context.mathContext()
                ));
        extensions.put(callSite.getKeyName(), callSite);

        callSite = new OperationCallSite("fv",
                MethodType.methodType(BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class),
                (context, parameters) -> ExcelFinancialFunctions.fv(
                        (BigDecimal) parameters[0],
                        (BigDecimal) parameters[1],
                        (BigDecimal) parameters[2],
                        (BigDecimal) parameters[3],
                        (BigDecimal) parameters[4],
                        context.mathContext()
                ));
        extensions.put(callSite.getKeyName(), callSite);

        callSite = new OperationCallSite("fv",
                MethodType.methodType(BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class),
                (context, parameters) -> ExcelFinancialFunctions.fv(
                        (BigDecimal) parameters[0],
                        (BigDecimal) parameters[1],
                        (BigDecimal) parameters[2],
                        (BigDecimal) parameters[3],
                        context.mathContext()
                ));
        extensions.put(callSite.getKeyName(), callSite);

        callSite = new OperationCallSite("fvs",
                MethodType.methodType(BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class),
                (context, parameters) -> ExcelFinancialFunctions.fvs(
                        (BigDecimal) parameters[0],
                        (BigDecimal) parameters[1],
                        (BigDecimal) parameters[2],
                        (BigDecimal) parameters[3],
                        (BigDecimal) parameters[4],
                        context.mathContext()
                ));
        extensions.put(callSite.getKeyName(), callSite);

        callSite = new OperationCallSite("fvs",
                MethodType.methodType(BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class),
                (context, parameters) -> ExcelFinancialFunctions.fvs(
                        (BigDecimal) parameters[0],
                        (BigDecimal) parameters[1],
                        (BigDecimal) parameters[2],
                        (BigDecimal) parameters[3],
                        (BigDecimal) parameters[4],
                        (BigDecimal) parameters[5],
                        context.mathContext()
                ));
        extensions.put(callSite.getKeyName(), callSite);

        callSite = new OperationCallSite("pmt",
                MethodType.methodType(BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class),
                (context, parameters) -> ExcelFinancialFunctions.pmt(
                        (BigDecimal) parameters[0],
                        (BigDecimal) parameters[1],
                        (BigDecimal) parameters[2],
                        (BigDecimal) parameters[3],
                        (BigDecimal) parameters[4],
                        context.mathContext()
                ));
        extensions.put(callSite.getKeyName(), callSite);

        callSite = new OperationCallSite("pmt",
                MethodType.methodType(BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class),
                (context, parameters) -> ExcelFinancialFunctions.pmt(
                        (BigDecimal) parameters[0],
                        (BigDecimal) parameters[1],
                        (BigDecimal) parameters[2],
                        (BigDecimal) parameters[3],
                        context.mathContext()
                ));
        extensions.put(callSite.getKeyName(), callSite);

        callSite = new OperationCallSite("pmt",
                MethodType.methodType(BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class),
                (context, parameters) -> ExcelFinancialFunctions.pmt(
                        (BigDecimal) parameters[0],
                        (BigDecimal) parameters[1],
                        (BigDecimal) parameters[2],
                        context.mathContext()
                ));
        extensions.put(callSite.getKeyName(), callSite);

        callSite = new OperationCallSite("ipmt",
                MethodType.methodType(BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class),
                (context, parameters) -> ExcelFinancialFunctions.ipmt(
                        (BigDecimal) parameters[0],
                        (BigDecimal) parameters[1],
                        (BigDecimal) parameters[2],
                        (BigDecimal) parameters[3],
                        (BigDecimal) parameters[4],
                        (BigDecimal) parameters[5],
                        context.mathContext()
                ));
        extensions.put(callSite.getKeyName(), callSite);

        callSite = new OperationCallSite("ipmt",
                MethodType.methodType(BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class),
                (context, parameters) -> ExcelFinancialFunctions.ipmt(
                        (BigDecimal) parameters[0],
                        (BigDecimal) parameters[1],
                        (BigDecimal) parameters[2],
                        (BigDecimal) parameters[3],
                        (BigDecimal) parameters[4],
                        context.mathContext()
                ));
        extensions.put(callSite.getKeyName(), callSite);

        callSite = new OperationCallSite("ipmt",
                MethodType.methodType(BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class),
                (context, parameters) -> ExcelFinancialFunctions.ipmt(
                        (BigDecimal) parameters[0],
                        (BigDecimal) parameters[1],
                        (BigDecimal) parameters[2],
                        (BigDecimal) parameters[3],
                        context.mathContext()
                ));
        extensions.put(callSite.getKeyName(), callSite);

        callSite = new OperationCallSite("ppmt",
                MethodType.methodType(BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class),
                (context, parameters) -> ExcelFinancialFunctions.ppmt(
                        (BigDecimal) parameters[0],
                        (BigDecimal) parameters[1],
                        (BigDecimal) parameters[2],
                        (BigDecimal) parameters[3],
                        (BigDecimal) parameters[4],
                        (BigDecimal) parameters[5],
                        context.mathContext()
                ));
        extensions.put(callSite.getKeyName(), callSite);

        callSite = new OperationCallSite("ppmt",
                MethodType.methodType(BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class),
                (context, parameters) -> ExcelFinancialFunctions.ppmt(
                        (BigDecimal) parameters[0],
                        (BigDecimal) parameters[1],
                        (BigDecimal) parameters[2],
                        (BigDecimal) parameters[3],
                        (BigDecimal) parameters[4],
                        context.mathContext()
                ));
        extensions.put(callSite.getKeyName(), callSite);

        callSite = new OperationCallSite("ppmt",
                MethodType.methodType(BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class),
                (context, parameters) -> ExcelFinancialFunctions.ppmt(
                        (BigDecimal) parameters[0],
                        (BigDecimal) parameters[1],
                        (BigDecimal) parameters[2],
                        (BigDecimal) parameters[3],
                        context.mathContext()
                ));
        extensions.put(callSite.getKeyName(), callSite);

        callSite = new OperationCallSite("npv",
                MethodType.methodType(BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal[].class),
                (context, parameters) -> ExcelFinancialFunctions.npv(
                        (BigDecimal) parameters[0],
                        (BigDecimal) parameters[1],
                        (BigDecimal[]) parameters[2],
                        context.mathContext()
                ));
        extensions.put(callSite.getKeyName(), callSite);

        callSite = new OperationCallSite("xnpv",
                MethodType.methodType(BigDecimal.class, BigDecimal.class, BigDecimal[].class, ZonedDateTime[].class),
                (context, parameters) -> ExcelFinancialFunctions.xnpv(
                        (BigDecimal) parameters[0],
                        (BigDecimal[]) parameters[1],
                        (ZonedDateTime[]) parameters[2],
                        context.mathContext()
                ));
        extensions.put(callSite.getKeyName(), callSite);

        callSite = new OperationCallSite("irr",
                MethodType.methodType(BigDecimal.class, BigDecimal[].class, BigDecimal.class),
                (context, parameters) -> ExcelFinancialFunctions.irr(
                        (BigDecimal[]) parameters[0],
                        (BigDecimal) parameters[1],
                        context.mathContext()
                ));
        extensions.put(callSite.getKeyName(), callSite);

        callSite = new OperationCallSite("mirr",
                MethodType.methodType(BigDecimal.class, BigDecimal[].class, BigDecimal.class, BigDecimal.class),
                (context, parameters) -> ExcelFinancialFunctions.mirr(
                        (BigDecimal[]) parameters[0],
                        (BigDecimal) parameters[1],
                        (BigDecimal) parameters[2],
                        context.mathContext()
                ));
        extensions.put(callSite.getKeyName(), callSite);

        callSite = new OperationCallSite("xirr",
                MethodType.methodType(BigDecimal.class, BigDecimal[].class, ZonedDateTime[].class, BigDecimal.class),
                (context, parameters) -> {
                    BigDecimal[] values = (BigDecimal[]) parameters[0];
                    ZonedDateTime[] dates = (ZonedDateTime[]) parameters[1];
                    Transaction[] transactions = new Transaction[values.length];
                    for (int i = 0; i < transactions.length; i++) {
                        transactions[i] = new Transaction(values[i].doubleValue(), dates[i].toInstant());
                    }
                    return Xirr.builder().withTransactions(transactions)
                            .withGuess(((BigDecimal) parameters[2]).doubleValue()).xirr();
                });
        extensions.put(callSite.getKeyName(), callSite);

        return extensions;
    }

}
