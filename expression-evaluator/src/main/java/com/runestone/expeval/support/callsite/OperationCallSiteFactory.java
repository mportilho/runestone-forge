package com.runestone.expeval.support.callsite;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.lang.invoke.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static java.lang.reflect.Modifier.isStatic;

/**
 * Factory for creating operation call sites for dynamic functions.
 *
 * @author Marcelo Portilho
 */
public class OperationCallSiteFactory {

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

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
        boolean isClassObject = provider instanceof Class<?>;
        Class<?> clazz = isClassObject ? (Class<?>) provider : provider.getClass();
        BeanInfo beanInfo = Introspector.getBeanInfo(clazz, Object.class);

        Map<String, OperationCallSite> dynamicCallerPool = new HashMap<>();
        for (MethodDescriptor methodDescriptor : beanInfo.getMethodDescriptors()) {
            if (verifyIgnorableMethod(methodDescriptor, methodNames)) continue;
            OperationCallSite lambdaCallSite = createLambdaCallSite(methodDescriptor.getMethod(), isClassObject, provider);
            if (lambdaCallSite != null) {
                dynamicCallerPool.put(lambdaCallSite.getKeyName(), lambdaCallSite);
            }
        }
        return dynamicCallerPool;
    }

    private static boolean verifyIgnorableMethod(MethodDescriptor methodDescriptor, String[] methodNames) {
        if (methodNames != null && methodNames.length > 0) {
            boolean found = false;
            for (String methodName : methodNames) {
                if (methodName.equals(methodDescriptor.getName())) {
                    found = true;
                    break;
                }
            }
            return !found;
        }
        return false;
    }

    private static OperationCallSite createLambdaCallSite(Method method, boolean isClassObject, Object provider) throws Throwable {
        if (void.class.equals(method.getReturnType()) || !Modifier.isPublic(method.getModifiers())) {
            return null;
        }
        if (findFactoryInterface(method.getParameterCount()) == null || hasPrimitives(method)) {
            return createMethodHandleCaller(method, isClassObject ? null : provider);
        } else if (isStatic(method.getModifiers())) {
            return createStaticCaller(method);
        } else if (!isClassObject) {
            return createDynamicCaller(method, provider);
        }
        return null;
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
        return new OperationCallSite(method.getName(), methodHandle.type(), supplier);
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

    private static OperationCallSite createDynamicCaller(Method method, Object instance) throws Throwable {
        Class<?> clazz = method.getDeclaringClass();
        Class<?> factoryInterface = findFactoryInterface(method.getParameterCount());
        MethodType functionMethodType = MethodType.methodType(method.getReturnType(), method.getParameterTypes());
        MethodHandle implementationMethodHandle = OperationCallSiteFactory.LOOKUP.findVirtual(clazz, method.getName(), functionMethodType);

        CallSite callSite = LambdaMetafactory.metafactory(OperationCallSiteFactory.LOOKUP,
                "call",
                MethodType.methodType(factoryInterface, instance.getClass()), // factoryMethodType
                MethodType.genericMethodType(method.getParameterCount()), // method params plus instance object
                implementationMethodHandle,
                functionMethodType); // method params plus instance object
        return new OperationCallSite(method.getName(), functionMethodType, createCallSiteInvoker(callSite.getTarget().invoke(instance)));
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

}
