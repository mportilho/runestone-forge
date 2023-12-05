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

package com.runestone.expeval.support.callsite;

import java.lang.invoke.MethodType;
import java.lang.reflect.Array;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * This class represents a call site for a virtual method, containing the method name, the method type and the invoker function.
 * <p>
 * To avoid naming collision on overloaded methods, the key name of the call site is composed by the method name and the number of parameters,
 * separated by an underscore. For example, the method {@code String concat(String, String)} will have the key name {@code concat_2}. The
 * static method keyName can be used to generate the key name for a method.
 * </p>
 */
public class OperationCallSite {

    private final String methodName;
    private final MethodType methodType;
    private final CallSiteInvoker callSiteInvoker;

    /**
     * Creates a new call site for a virtual method
     *
     * @param methodName      method name to be used in the expression
     * @param methodType      method type (return type and parameter types)
     * @param callSiteInvoker invoker function
     */
    public OperationCallSite(String methodName, MethodType methodType, CallSiteInvoker callSiteInvoker) {
        this.methodName = methodName;
        this.methodType = methodType;
        this.callSiteInvoker = callSiteInvoker;
    }

    /**
     * Creates a new call site for a virtual method.
     *
     * @param methodName method name
     * @param methodType method type (return type and parameter types)
     * @param function   function to be invoked. Gets wrapped internally in a {@link CallSiteInvoker} instance
     */
    public OperationCallSite(String methodName, MethodType methodType, Function<Object[], Object> function) {
        this(methodName, methodType, ((context, parameters) -> function.apply(parameters)));
    }

    /**
     * Executes the call site, converting the parameters and return value to the expected types.
     *
     * @param context    call site context
     * @param parameters parameters to be passed to the call site
     * @param converter  function to convert the parameters and return value to the expected types
     * @param <R>        return type
     * @return the return value of the call site
     */
    @SuppressWarnings({"unchecked"})
    public <R> R call(CallSiteContext context, Object[] parameters, BiFunction<Object, Class<?>, Object> converter) {
        Object[] params;
        Objects.requireNonNull(context, "CallSite context cannot be null");
        if (methodType.parameterCount() == 1 && methodType.lastParameterType().getComponentType() != null
                && (parameters.length > 1 || (parameters.length == 1 && !(parameters[0].getClass().isArray())))) {
            params = new Object[]{parameters};
        } else {
            params = parameters;
        }
        Object[] convertedParams = new Object[params.length];

        Class<?>[] methodParameterTypes = methodType.parameterArray();
        for (int i = 0, parametersLength = params.length; i < parametersLength; i++) {
            Object parameter = params[i];
            Class<?> parameterType = methodParameterTypes[i];
            if (parameterType.isArray()) {
                if (!parameter.getClass().isArray()) {
                    throw new IllegalArgumentException(String.format("Parameter on position [%s] of virtual method [%s] should be an array", i, methodName));
                }
                int arrLength = Array.getLength(parameter);
                Object convertedArray = Array.newInstance(parameterType.getComponentType(), arrLength);
                for (int j = 0; j < arrLength; j++) {
                    Array.set(convertedArray, j, converter.apply(Array.get(parameter, j), parameterType.getComponentType()));
                }
                convertedParams[i] = convertedArray;
            } else {
                if (parameterType.equals(parameter.getClass())) {
                    convertedParams[i] = parameter;
                } else {
                    convertedParams[i] = converter.apply(parameter, parameterType);
                }
            }
        }

        Object value = callSiteInvoker.invoke(context, convertedParams);
        if (methodType.returnType().equals(value.getClass())) {
            return (R) value;
        }
        return (R) converter.apply(value, methodType.returnType());
    }

    /**
     * @return the key name of the call site. The key name is composed by the method name and the number of parameters, separated by an underscore.
     */
    public String getKeyName() {
        return keyName(getMethodName(), getMethodType().parameterCount());
    }

    /**
     * @param methodName     method name
     * @param parameterCount number of parameters
     * @return the key name of the call site. The key name is composed by the method name and the number of parameters, separated by an underscore.
     */
    public static String keyName(String methodName, int parameterCount) {
        return methodName + "_" + parameterCount;
    }

    /**
     * @return method name
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * @return method type (return type and parameter types)
     */
    public MethodType getMethodType() {
        return methodType;
    }

}
