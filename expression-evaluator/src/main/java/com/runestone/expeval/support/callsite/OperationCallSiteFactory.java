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

import java.lang.invoke.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.lang.reflect.Modifier.isStatic;

/**
 * Factory for creating operation call sites for dynamic functions.
 *
 * @author Marcelo Portilho
 */
public class OperationCallSiteFactory {

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    private static final ClassValue<MethodTemplate[]> METHOD_TEMPLATES = new ClassValue<>() {
        @Override
        protected MethodTemplate[] computeValue(Class<?> type) {
            Method[] methods = type.getMethods();
            Arrays.sort(methods, Comparator.comparing(Method::getName)
                    .thenComparingInt(Method::getParameterCount)
                    .thenComparing(Method::toGenericString));

            List<MethodTemplate> templates = new ArrayList<>(methods.length);
            for (Method method : methods) {
                if (Object.class.equals(method.getDeclaringClass())
                        || !Modifier.isPublic(method.getModifiers())
                        || void.class.equals(method.getReturnType())) {
                    continue;
                }
                try {
                    templates.add(createMethodTemplate(method));
                } catch (Throwable e) {
                    throw new IllegalStateException("Error creating method template for method [" + method + "]", e);
                }
            }
            return templates.toArray(new MethodTemplate[0]);
        }
    };

    private static final ConcurrentMap<StaticProviderCacheKey, Map<String, OperationCallSite>> STATIC_PROVIDER_CACHE = new ConcurrentHashMap<>();

    /**
     * Creates a map of lambda call sites for all public methods of the given provider object, including static methods.
     *
     * @param provider the provider object for extracting methods. Can be an instance or a class object
     * @return a map of lambda call sites
     */
    public static Map<String, OperationCallSite> createLambdaCallSites(Object provider) {
        try {
            return makeLambdaCallSites(provider);
        } catch (Throwable e) {
            throw new OperationCallSiteException("Error creating lambda call sites", e);
        }
    }

    /**
     * Creates a map of lambda call sites for the given method names of the given provider object, including static methods.
     *
     * @param provider    the provider object for extracting methods. Can be an instance or a class object
     * @param methodNames list of functions to be extracted
     * @return a map of lambda call sites
     */
    public static Map<String, OperationCallSite> createLambdaCallSites(Object provider, String... methodNames) {
        try {
            return makeLambdaCallSites(provider, methodNames);
        } catch (Throwable e) {
            throw new OperationCallSiteException("Error creating lambda call sites", e);
        }
    }

    private static Map<String, OperationCallSite> makeLambdaCallSites(Object provider, String... methodNames) throws Throwable {
        Objects.requireNonNull(provider, "Provider object for extracting methods required");
        boolean classProvider = provider instanceof Class<?>;
        Class<?> providerClass = classProvider ? (Class<?>) provider : provider.getClass();
        Set<String> methodNameFilter = normalizeMethodNames(methodNames);

        if (classProvider) {
            Map<String, OperationCallSite> cached = STATIC_PROVIDER_CACHE.computeIfAbsent(
                    new StaticProviderCacheKey(providerClass, canonicalFilterKey(methodNameFilter)),
                    key -> Collections.unmodifiableMap(createCallSites(providerClass, null, methodNameFilter, true)));
            return new HashMap<>(cached);
        }

        return createCallSites(providerClass, provider, methodNameFilter, false);
    }

    private static Map<String, OperationCallSite> createCallSites(
            Class<?> providerClass,
            Object provider,
            Set<String> methodNameFilter,
            boolean classProvider) {
        Map<String, OperationCallSite> dynamicCallerPool = new HashMap<>();
        for (MethodTemplate methodTemplate : METHOD_TEMPLATES.get(providerClass)) {
            if (methodNameFilter != null && !methodNameFilter.contains(methodTemplate.methodName())) {
                continue;
            }
            if (!methodTemplate.isStatic() && classProvider) {
                continue;
            }
            OperationCallSite callSite;
            try {
                callSite = methodTemplate.create(provider);
            } catch (Throwable e) {
                throw new IllegalStateException("Error creating call site for method [" + methodTemplate.methodName() + "]", e);
            }
            dynamicCallerPool.put(callSite.getKeyName(), callSite);
        }
        return dynamicCallerPool;
    }

    private static Set<String> normalizeMethodNames(String[] methodNames) {
        if (methodNames == null || methodNames.length == 0) {
            return null;
        }
        Set<String> names = new HashSet<>(methodNames.length);
        for (String methodName : methodNames) {
            if (methodName != null && !methodName.isBlank()) {
                names.add(methodName);
            }
        }
        return names.isEmpty() ? null : names;
    }

    private static String canonicalFilterKey(Set<String> methodNameFilter) {
        if (methodNameFilter == null) {
            return "*";
        }
        List<String> names = new ArrayList<>(methodNameFilter);
        names.sort(String::compareTo);
        return String.join("\u0001", names);
    }

    private static MethodTemplate createMethodTemplate(Method method) throws Throwable {
        Class<?> factoryInterface = findFactoryInterface(method.getParameterCount());
        boolean forceMethodHandle = factoryInterface == null || hasPrimitives(method);

        if (isStatic(method.getModifiers())) {
            if (forceMethodHandle) {
                return new StaticOperationCallSiteTemplate(method.getName(), createMethodHandleCaller(method, null));
            }
            return new StaticOperationCallSiteTemplate(method.getName(), createStaticCaller(method));
        }
        if (forceMethodHandle) {
            return new InstanceMethodHandleTemplate(method);
        }
        return new InstanceLambdaTemplate(method, factoryInterface);
    }

    // slower
    private static OperationCallSite createMethodHandleCaller(Method method, Object instance) throws Throwable {
        MethodHandle methodHandle = instance == null
                ? OperationCallSiteFactory.LOOKUP.unreflect(method)
                : OperationCallSiteFactory.LOOKUP.unreflect(method).bindTo(instance);
        MethodHandle callableMethodHandle = methodHandle
                .asType(methodHandle.type().generic())
                .asSpreader(Object[].class, methodHandle.type().parameterCount());

        CallSiteInvoker supplier = (context, parameters) -> {
            try {
                return callableMethodHandle.invokeExact(parameters);
            } catch (Throwable e) {
                throw new IllegalStateException("Error calling dynamic function", e);
            }
        };
        MethodType functionMethodType = MethodType.methodType(method.getReturnType(), method.getParameterTypes());
        return new OperationCallSite(method.getName(), functionMethodType, supplier);
    }

    private static OperationCallSite createStaticCaller(Method method) throws Throwable {
        Class<?> clazz = method.getDeclaringClass();
        Class<?> factoryInterface = findFactoryInterface(method.getParameterCount());
        MethodType functionMethodType = MethodType.methodType(method.getReturnType(), method.getParameterTypes());
        MethodHandle implementationMethodHandle = OperationCallSiteFactory.LOOKUP.findStatic(clazz, method.getName(), functionMethodType);
        CallSite callSite = LambdaMetafactory.metafactory(OperationCallSiteFactory.LOOKUP,
                "call",
                MethodType.methodType(factoryInterface),
                functionMethodType.generic(),
                implementationMethodHandle,
                functionMethodType);

        return new OperationCallSite(method.getName(), functionMethodType, createCallSiteInvoker(callSite.getTarget().invoke()));
    }

    private static boolean hasPrimitives(Method method) {
        if (method.getReturnType().isPrimitive()) {
            return true;
        }
        for (Class<?> parameterType : method.getParameterTypes()) {
            if (parameterType.isPrimitive()) {
                return true;
            }
        }
        return false;
    }


    private static Class<?> findFactoryInterface(int parameterNumber) {
        return switch (parameterNumber) {
            case 1 -> InterfaceWrappers.Function1.class;
            case 2 -> InterfaceWrappers.Function2.class;
            case 3 -> InterfaceWrappers.Function3.class;
            case 4 -> InterfaceWrappers.Function4.class;
            case 5 -> InterfaceWrappers.Function5.class;
            case 6 -> InterfaceWrappers.Function6.class;
            case 7 -> InterfaceWrappers.Function7.class;
            case 8 -> InterfaceWrappers.Function8.class;
            case 9 -> InterfaceWrappers.Function9.class;
            case 10 -> InterfaceWrappers.Function10.class;
            case 11 -> InterfaceWrappers.Function11.class;
            default -> null;
        };
    }

    private static CallSiteInvoker createCallSiteInvoker(Object lambda) {
        return switch (lambda) {
            case InterfaceWrappers.Function1 f -> (c, p) -> f.call(p[0]);
            case InterfaceWrappers.Function2 f -> (c, p) -> f.call(p[0], p[1]);
            case InterfaceWrappers.Function3 f -> (c, p) -> f.call(p[0], p[1], p[2]);
            case InterfaceWrappers.Function4 f -> (c, p) -> f.call(p[0], p[1], p[2], p[3]);
            case InterfaceWrappers.Function5 f -> (c, p) -> f.call(p[0], p[1], p[2], p[3], p[4]);
            case InterfaceWrappers.Function6 f -> (c, p) -> f.call(p[0], p[1], p[2], p[3], p[4], p[5]);
            case InterfaceWrappers.Function7 f -> (c, p) -> f.call(p[0], p[1], p[2], p[3], p[4], p[5], p[6]);
            case InterfaceWrappers.Function8 f -> (c, p) -> f.call(p[0], p[1], p[2], p[3], p[4], p[5], p[6], p[7]);
            case InterfaceWrappers.Function9 f -> (c, p) -> f.call(p[0], p[1], p[2], p[3], p[4], p[5], p[6], p[7], p[8]);
            case InterfaceWrappers.Function10 f -> (c, p) -> f.call(p[0], p[1], p[2], p[3], p[4], p[5], p[6], p[7], p[8], p[9]);
            case InterfaceWrappers.Function11 f -> (c, p) -> f.call(p[0], p[1], p[2], p[3], p[4], p[5], p[6], p[7], p[8], p[9], p[10]);
            default -> null;
        };
    }

    private interface MethodTemplate {

        String methodName();

        boolean isStatic();

        OperationCallSite create(Object provider) throws Throwable;
    }

    private static final class StaticOperationCallSiteTemplate implements MethodTemplate {

        private final String methodName;
        private final OperationCallSite operationCallSite;

        private StaticOperationCallSiteTemplate(String methodName, OperationCallSite operationCallSite) {
            this.methodName = methodName;
            this.operationCallSite = operationCallSite;
        }

        @Override
        public String methodName() {
            return methodName;
        }

        @Override
        public boolean isStatic() {
            return true;
        }

        @Override
        public OperationCallSite create(Object provider) {
            return operationCallSite;
        }
    }

    private static final class InstanceLambdaTemplate implements MethodTemplate {

        private final String methodName;
        private final MethodType functionMethodType;
        private final MethodHandle factoryTarget;

        private InstanceLambdaTemplate(Method method, Class<?> factoryInterface) throws Throwable {
            Class<?> clazz = method.getDeclaringClass();
            this.methodName = method.getName();
            this.functionMethodType = MethodType.methodType(method.getReturnType(), method.getParameterTypes());
            MethodHandle implementationMethodHandle = OperationCallSiteFactory.LOOKUP.findVirtual(clazz, methodName, functionMethodType);
            CallSite callSite = LambdaMetafactory.metafactory(OperationCallSiteFactory.LOOKUP,
                    "call",
                    MethodType.methodType(factoryInterface, clazz),
                    MethodType.genericMethodType(method.getParameterCount()),
                    implementationMethodHandle,
                    functionMethodType);
            this.factoryTarget = callSite.getTarget();
        }

        @Override
        public String methodName() {
            return methodName;
        }

        @Override
        public boolean isStatic() {
            return false;
        }

        @Override
        public OperationCallSite create(Object provider) throws Throwable {
            Object lambda = factoryTarget.invoke(provider);
            return new OperationCallSite(methodName, functionMethodType, createCallSiteInvoker(lambda));
        }
    }

    private static final class InstanceMethodHandleTemplate implements MethodTemplate {

        private final String methodName;
        private final MethodType functionMethodType;
        private final MethodHandle unboundMethodHandle;

        private InstanceMethodHandleTemplate(Method method) throws IllegalAccessException {
            this.methodName = method.getName();
            this.functionMethodType = MethodType.methodType(method.getReturnType(), method.getParameterTypes());
            this.unboundMethodHandle = OperationCallSiteFactory.LOOKUP.unreflect(method);
        }

        @Override
        public String methodName() {
            return methodName;
        }

        @Override
        public boolean isStatic() {
            return false;
        }

        @Override
        public OperationCallSite create(Object provider) {
            Objects.requireNonNull(provider, "Provider cannot be null for instance method call sites");
            MethodHandle methodHandle = unboundMethodHandle.bindTo(provider);
            MethodHandle callableMethodHandle = methodHandle
                    .asType(methodHandle.type().generic())
                    .asSpreader(Object[].class, functionMethodType.parameterCount());

            CallSiteInvoker supplier = (context, parameters) -> {
                try {
                    return callableMethodHandle.invokeExact(parameters);
                } catch (Throwable e) {
                    throw new IllegalStateException("Error calling dynamic function", e);
                }
            };
            return new OperationCallSite(methodName, functionMethodType, supplier);
        }
    }

    private record StaticProviderCacheKey(Class<?> providerClass, String methodFilter) {
    }

}
