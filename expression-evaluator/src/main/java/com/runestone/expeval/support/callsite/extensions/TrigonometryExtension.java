package com.runestone.expeval.support.callsite.extensions;

import ch.obermuhlner.math.big.BigDecimalMath;
import com.runestone.expeval.support.callsite.OperationCallSite;

import java.lang.invoke.MethodType;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

class TrigonometryExtension {

    static Map<String, OperationCallSite> trigonometryFunctionFactory() {
        OperationCallSite callSite;
        Map<String, OperationCallSite> extensions = new HashMap<>();

        callSite = new OperationCallSite("sin", MethodType.methodType(BigDecimal.class, BigDecimal.class),
                (context, parameters) -> BigDecimalMath.sin(((BigDecimal) parameters[0]), context.mathContext()));
        extensions.put(callSite.getKeyName(), callSite);

        callSite = new OperationCallSite("cos", MethodType.methodType(BigDecimal.class, BigDecimal.class),
                (context, parameters) -> BigDecimalMath.cos(((BigDecimal) parameters[0]), context.mathContext()));
        extensions.put(callSite.getKeyName(), callSite);

        callSite = new OperationCallSite("tan", MethodType.methodType(BigDecimal.class, BigDecimal.class),
                (context, parameters) -> BigDecimalMath.tan(((BigDecimal) parameters[0]), context.mathContext()));
        extensions.put(callSite.getKeyName(), callSite);

        callSite = new OperationCallSite("asin", MethodType.methodType(BigDecimal.class, BigDecimal.class),
                (context, parameters) -> BigDecimalMath.asin(((BigDecimal) parameters[0]), context.mathContext()));
        extensions.put(callSite.getKeyName(), callSite);

        callSite = new OperationCallSite("acos", MethodType.methodType(BigDecimal.class, BigDecimal.class),
                (context, parameters) -> BigDecimalMath.acos(((BigDecimal) parameters[0]), context.mathContext()));
        extensions.put(callSite.getKeyName(), callSite);

        callSite = new OperationCallSite("atan", MethodType.methodType(BigDecimal.class, BigDecimal.class),
                (context, parameters) -> BigDecimalMath.atan(((BigDecimal) parameters[0]), context.mathContext()));
        extensions.put(callSite.getKeyName(), callSite);

        callSite = new OperationCallSite("atan2", MethodType.methodType(BigDecimal.class, BigDecimal.class, BigDecimal.class),
                (context, parameters) -> BigDecimalMath.atan2(((BigDecimal) parameters[0]), ((BigDecimal) parameters[1]), context.mathContext()));
        extensions.put(callSite.getKeyName(), callSite);

        callSite = new OperationCallSite("sinh", MethodType.methodType(BigDecimal.class, BigDecimal.class),
                (context, parameters) -> BigDecimalMath.sinh(((BigDecimal) parameters[0]), context.mathContext()));
        extensions.put(callSite.getKeyName(), callSite);

        callSite = new OperationCallSite("cosh", MethodType.methodType(BigDecimal.class, BigDecimal.class),
                (context, parameters) -> BigDecimalMath.cosh(((BigDecimal) parameters[0]), context.mathContext()));
        extensions.put(callSite.getKeyName(), callSite);

        callSite = new OperationCallSite("tanh", MethodType.methodType(BigDecimal.class, BigDecimal.class),
                (context, parameters) -> BigDecimalMath.tanh(((BigDecimal) parameters[0]), context.mathContext()));
        extensions.put(callSite.getKeyName(), callSite);

        callSite = new OperationCallSite("asinh", MethodType.methodType(BigDecimal.class, BigDecimal.class),
                (context, parameters) -> BigDecimalMath.asinh(((BigDecimal) parameters[0]), context.mathContext()));
        extensions.put(callSite.getKeyName(), callSite);

        callSite = new OperationCallSite("acosh", MethodType.methodType(BigDecimal.class, BigDecimal.class),
                (context, parameters) -> BigDecimalMath.acosh(((BigDecimal) parameters[0]), context.mathContext()));
        extensions.put(callSite.getKeyName(), callSite);

        callSite = new OperationCallSite("atanh", MethodType.methodType(BigDecimal.class, BigDecimal.class),
                (context, parameters) -> BigDecimalMath.atanh(((BigDecimal) parameters[0]), context.mathContext()));
        extensions.put(callSite.getKeyName(), callSite);

        return extensions;
    }

}
