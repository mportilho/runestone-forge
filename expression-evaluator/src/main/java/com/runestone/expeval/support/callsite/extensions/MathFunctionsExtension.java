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
