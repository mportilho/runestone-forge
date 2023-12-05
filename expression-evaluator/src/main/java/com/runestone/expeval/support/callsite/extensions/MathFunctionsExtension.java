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

import com.runestone.expeval.support.callsite.CallSiteInvoker;
import com.runestone.expeval.support.callsite.OperationCallSite;
import com.runestone.expeval.support.functions.math.MathFunctions;

import java.lang.invoke.MethodType;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

class MathFunctionsExtension {

    static Map<String, OperationCallSite> mathFunctionsFactory() {
        Map<String, OperationCallSite> extensions = new HashMap<>();
        OperationCallSite callSite;

        CallSiteInvoker invoker = (context, parameters) -> MathFunctions.mean((BigDecimal[]) parameters[0], context.mathContext());
        callSite = new OperationCallSite("avg", MethodType.methodType(BigDecimal.class, BigDecimal[].class), invoker);
        extensions.put(callSite.getKeyName(), callSite);
        callSite = new OperationCallSite("mean", MethodType.methodType(BigDecimal.class, BigDecimal[].class), invoker);
        extensions.put(callSite.getKeyName(), callSite);

        callSite = new OperationCallSite("variance", MethodType.methodType(BigDecimal.class, BigDecimal[].class),
                (context, parameters) -> MathFunctions.variance((BigDecimal[]) parameters[0], 0, context.mathContext()));
        extensions.put(callSite.getKeyName(), callSite);

        callSite = new OperationCallSite("sampleVariance", MethodType.methodType(BigDecimal.class, BigDecimal[].class),
                (context, parameters) -> MathFunctions.variance((BigDecimal[]) parameters[0], 1, context.mathContext()));
        extensions.put(callSite.getKeyName(), callSite);

        callSite = new OperationCallSite("stdDev", MethodType.methodType(BigDecimal.class, BigDecimal[].class),
                (context, parameters) -> MathFunctions.stdDev((BigDecimal[]) parameters[0], 0, context.mathContext()));
        extensions.put(callSite.getKeyName(), callSite);

        callSite = new OperationCallSite("sampleStdDev", MethodType.methodType(BigDecimal.class, BigDecimal[].class),
                (context, parameters) -> MathFunctions.stdDev((BigDecimal[]) parameters[0], 1, context.mathContext()));
        extensions.put(callSite.getKeyName(), callSite);

        callSite = new OperationCallSite("meanDev", MethodType.methodType(BigDecimal.class, BigDecimal[].class),
                (context, parameters) -> MathFunctions.meanDev((BigDecimal[]) parameters[0], context.mathContext()));
        extensions.put(callSite.getKeyName(), callSite);

        callSite = new OperationCallSite("geometricMean", MethodType.methodType(BigDecimal.class, BigDecimal[].class),
                (context, parameters) -> MathFunctions.geometricMean((BigDecimal[]) parameters[0], context.mathContext()));
        extensions.put(callSite.getKeyName(), callSite);

        callSite = new OperationCallSite("harmonicMean", MethodType.methodType(BigDecimal.class, BigDecimal[].class),
                (context, parameters) -> MathFunctions.harmonicMean((BigDecimal[]) parameters[0], context.mathContext()));
        extensions.put(callSite.getKeyName(), callSite);

        callSite = new OperationCallSite("rule3d", MethodType.methodType(BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class),
                (context, parameters) -> MathFunctions.rule3d((BigDecimal) parameters[0], (BigDecimal) parameters[1], (BigDecimal) parameters[2], context.mathContext()));
        extensions.put(callSite.getKeyName(), callSite);

        callSite = new OperationCallSite("rule3i", MethodType.methodType(BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class),
                (context, parameters) -> MathFunctions.rule3i((BigDecimal) parameters[0], (BigDecimal) parameters[1], (BigDecimal) parameters[2], context.mathContext()));
        extensions.put(callSite.getKeyName(), callSite);

        callSite = new OperationCallSite("distribute", MethodType.methodType(BigDecimal[].class, BigDecimal.class, BigDecimal.class, BigDecimal[].class, BigDecimal[].class),
                (context, parameters) -> MathFunctions.distribute((BigDecimal) parameters[0], (BigDecimal) parameters[1], (BigDecimal[]) parameters[2], (BigDecimal[]) parameters[3]));
        extensions.put(callSite.getKeyName(), callSite);

        callSite = new OperationCallSite("spread", MethodType.methodType(BigDecimal[].class, BigDecimal.class, BigDecimal.class, BigDecimal[].class),
                (context, parameters) -> MathFunctions.spread((BigDecimal) parameters[0], (BigDecimal) parameters[1], (BigDecimal[]) parameters[2]));
        extensions.put(callSite.getKeyName(), callSite);

        return extensions;
    }

}
