/*
 * MIT License
 * <p>
 * Copyright (c) 2023-2023 Marcelo Silva Portilho
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.runestone.expeval.support.callsite.extensions;

import com.runestone.expeval.support.callsite.OperationCallSite;
import com.runestone.expeval.support.functions.math.ExcelFinancialFunctions;

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

        return extensions;
    }

}
