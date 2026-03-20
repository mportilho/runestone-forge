package com.runestone.expeval2.environment;

import com.runestone.converters.DataConversionService;
import com.runestone.converters.impl.DefaultDataConversionService;
import com.runestone.expeval2.catalog.ExternalSymbolCatalog;
import com.runestone.expeval2.catalog.ExternalSymbolDescriptor;
import com.runestone.expeval2.catalog.FunctionCatalog;
import com.runestone.expeval2.catalog.FunctionDescriptor;
import com.runestone.expeval2.catalog.functions.ComparableFunctions;
import com.runestone.expeval2.catalog.functions.DateTimeFunctions;
import com.runestone.expeval2.catalog.functions.DoubleExcelFinancialFunctions;
import com.runestone.expeval2.catalog.functions.ExcelFinancialFunctions;
import com.runestone.expeval2.catalog.functions.MathFunctions;
import com.runestone.expeval2.catalog.functions.TrigonometryFunctions;
import com.runestone.expeval2.types.ResolvedType;
import com.runestone.expeval2.types.ResolvedTypes;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.MathContext;
import java.util.*;

public final class ExpressionEnvironmentBuilder {

    private static final DataConversionService DEFAULT_DATA_CONVERSION_SERVICE = new DefaultDataConversionService();
    private static final ExpressionEnvironment EMPTY_ENVIRONMENT = new ExpressionEnvironmentBuilder().build();

    private DataConversionService conversionService;
    private MathContext mathContext = MathContext.DECIMAL128;
    private final List<Class<?>> staticProviders = new ArrayList<>();
    private final List<Object> instanceProviders = new ArrayList<>();
    private final Map<String, ExternalSymbolRegistration> externalSymbols = new LinkedHashMap<>();

    public static ExpressionEnvironment empty() {
        return EMPTY_ENVIRONMENT;
    }

    public ExpressionEnvironmentBuilder conversionService(DataConversionService conversionService) {
        this.conversionService = Objects.requireNonNull(conversionService, "conversionService must not be null");
        return this;
    }

    public ExpressionEnvironmentBuilder withMathContext(MathContext mathContext) {
        this.mathContext = Objects.requireNonNull(mathContext, "mathContext must not be null");
        return this;
    }

    public ExpressionEnvironmentBuilder registerStaticProvider(Class<?> providerClass) {
        staticProviders.add(Objects.requireNonNull(providerClass, "providerClass must not be null"));
        return this;
    }

    public ExpressionEnvironmentBuilder registerInstanceProvider(Object providerInstance) {
        instanceProviders.add(Objects.requireNonNull(providerInstance, "providerInstance must not be null"));
        return this;
    }

    public ExpressionEnvironmentBuilder addComparableFunctions() {
        return registerStaticProvider(ComparableFunctions.class);
    }

    public ExpressionEnvironmentBuilder addDateTimeFunctions() {
        return registerStaticProvider(DateTimeFunctions.class);
    }

    public ExpressionEnvironmentBuilder addFastExcelFunctions() {
        return registerStaticProvider(DoubleExcelFinancialFunctions.class);
    }

    public ExpressionEnvironmentBuilder addExcelFunctions() {
        return registerStaticProvider(ExcelFinancialFunctions.class);
    }

    public ExpressionEnvironmentBuilder addMathFunctions() {
        return registerStaticProvider(MathFunctions.class);
    }

    public ExpressionEnvironmentBuilder addTrigonometryFunctions() {
        return registerStaticProvider(TrigonometryFunctions.class);
    }

    public ExpressionEnvironmentBuilder addAllFunctions() {
        return addComparableFunctions()
                .addDateTimeFunctions()
                .addExcelFunctions()
                .addMathFunctions()
                .addTrigonometryFunctions();
    }

    public ExpressionEnvironmentBuilder registerExternalSymbol(String name, ResolvedType declaredType,
                                                               Object defaultValue, boolean overridable) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        externalSymbols.put(name, new ExternalSymbolRegistration(name, Objects.requireNonNull(declaredType, "declaredType must not be null"), defaultValue, overridable));
        return this;
    }

    public ExpressionEnvironment build() {
        DataConversionService effectiveConversionService = conversionService == null ? DEFAULT_DATA_CONVERSION_SERVICE : conversionService;

        List<FunctionDescriptor> functionDescriptors = new ArrayList<>();
        staticProviders.forEach(providerClass -> functionDescriptors.addAll(discoverFunctions(providerClass, null, true)));
        instanceProviders.forEach(providerInstance -> functionDescriptors.addAll(discoverFunctions(providerInstance.getClass(), providerInstance, false)));
        functionDescriptors.sort(Comparator.comparing(FunctionDescriptor::name).thenComparing(FunctionDescriptor::arity));

        Map<String, List<FunctionDescriptor>> descriptorsByName = new LinkedHashMap<>();
        functionDescriptors.forEach(descriptor ->
                descriptorsByName.computeIfAbsent(descriptor.name(), ignored -> new ArrayList<>()).add(descriptor)
        );
        FunctionCatalog functionCatalog = new FunctionCatalog(descriptorsByName);

        Map<String, ExternalSymbolDescriptor> symbolsByName = new LinkedHashMap<>();
        externalSymbols.values().stream()
                .sorted(Comparator.comparing(ExternalSymbolRegistration::name))
                .forEach(registration -> symbolsByName.put(
                        registration.name(),
                        new ExternalSymbolDescriptor(
                                registration.name(),
                                registration.declaredType(),
                                registration.defaultValue(),
                                registration.overridable()
                        )
                ));
        ExternalSymbolCatalog externalSymbolCatalog = new ExternalSymbolCatalog(symbolsByName);

        return new ExpressionEnvironment(new ExpressionEnvironmentId(UUID.randomUUID().toString()),
                functionCatalog, externalSymbolCatalog, effectiveConversionService, mathContext);
    }

    private Collection<FunctionDescriptor> discoverFunctions(Class<?> providerClass, Object providerInstance, boolean staticOnly) {
        List<FunctionDescriptor> descriptors = new ArrayList<>();
        for (Method method : providerClass.getMethods()) {
            if (method.getDeclaringClass() == Object.class || method.isSynthetic() || method.isBridge()) {
                continue;
            }
            if (staticOnly != Modifier.isStatic(method.getModifiers())) {
                continue;
            }
            descriptors.add(toDescriptor(method, providerInstance));
        }
        return descriptors;
    }

    private FunctionDescriptor toDescriptor(Method method, Object providerInstance) {
        try {
            MethodHandle handle = MethodHandles.lookup().unreflect(method);
            if (providerInstance != null) {
                handle = handle.bindTo(providerInstance);
            }
            Class<?>[] rawParams = method.getParameterTypes();
            int firstDataParam = 0;
            if (rawParams.length > 0 && rawParams[0] == MathContext.class) {
                handle = MethodHandles.insertArguments(handle, 0, mathContext);
                firstDataParam = 1;
            }
            List<Class<?>> parameterTypes = List.of(rawParams).subList(firstDataParam, rawParams.length);
            List<ResolvedType> parameterResolvedTypes = parameterTypes.stream().map(ResolvedTypes::fromJavaType).toList();
            return new FunctionDescriptor(method.getName(), parameterTypes, parameterResolvedTypes, ResolvedTypes.fromJavaType(method.getReturnType()), handle);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("failed to create method handle for " + method, exception);
        }
    }

    private record ExternalSymbolRegistration(
            String name,
            ResolvedType declaredType,
            Object defaultValue,
            boolean overridable
    ) {
    }
}
