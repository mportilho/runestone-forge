package com.runestone.expeval_mk3.api;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Imports explicitly supplied Java function providers into expression function descriptors.
 */
public final class ReflectedFunctionImporter {

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private static final MethodHandles.Lookup PUBLIC_LOOKUP = MethodHandles.publicLookup();
    private static final Set<Class<?>> NUMERIC_TYPES = Set.of(
            BigDecimal.class,
            byte.class,
            Byte.class,
            short.class,
            Short.class,
            int.class,
            Integer.class,
            long.class,
            Long.class,
            float.class,
            Float.class,
            double.class,
            Double.class);

    private ReflectedFunctionImporter() {
    }

    public static ImportPlan importAll(Class<?> providerClass, FunctionPurity purity) {
        return new ImportPlanImpl(Source.staticProvider(providerClass, purity), SelectionMode.allMode());
    }

    public static ImportPlan importAll(Object providerInstance, FunctionPurity purity) {
        Objects.requireNonNull(providerInstance, "providerInstance");
        return new ImportPlanImpl(
                Source.instanceProvider(providerInstance.getClass(), providerInstance, purity),
                SelectionMode.allMode());
    }

    public static ImportPlan importAll(
            Class<?> exposureType,
            Object providerInstance,
            FunctionPurity purity) {
        return new ImportPlanImpl(
                Source.exposedInstanceProvider(exposureType, providerInstance, purity),
                SelectionMode.allMode());
    }

    public static Selection importSelected(Class<?> providerClass, FunctionPurity purity) {
        return new ImportPlanImpl(Source.staticProvider(providerClass, purity), SelectionMode.selectedMode());
    }

    public static Selection importSelected(Object providerInstance, FunctionPurity purity) {
        Objects.requireNonNull(providerInstance, "providerInstance");
        return new ImportPlanImpl(
                Source.instanceProvider(providerInstance.getClass(), providerInstance, purity),
                SelectionMode.selectedMode());
    }

    public static Selection importSelected(
            Class<?> exposureType,
            Object providerInstance,
            FunctionPurity purity) {
        return new ImportPlanImpl(
                Source.exposedInstanceProvider(exposureType, providerInstance, purity),
                SelectionMode.selectedMode());
    }

    public interface ImportPlan {
        ImportPlan rename(String javaMethodName, String languageName);

        List<FunctionDescriptor> toList();
    }

    public interface Selection extends ImportPlan {
        Selection methods(String... javaMethodNames);

        Selection method(String javaMethodName, Class<?>... javaParameterTypes);

        @Override
        Selection rename(String javaMethodName, String languageName);
    }

    private static final class ImportPlanImpl implements ImportPlan, Selection {

        private final Source source;
        private final SelectionMode selectionMode;
        private final Map<String, String> renames = new LinkedHashMap<>();

        private ImportPlanImpl(Source source, SelectionMode selectionMode) {
            this.source = source;
            this.selectionMode = selectionMode;
        }

        @Override
        public Selection rename(String javaMethodName, String languageName) {
            addRename(javaMethodName, languageName);
            return this;
        }

        @Override
        public Selection methods(String... javaMethodNames) {
            Objects.requireNonNull(javaMethodNames, "javaMethodNames");
            if (javaMethodNames.length == 0) {
                throw new IllegalArgumentException("javaMethodNames must not be empty");
            }
            for (String javaMethodName : javaMethodNames) {
                selectionMode.addName(javaMethodName);
            }
            return this;
        }

        @Override
        public Selection method(String javaMethodName, Class<?>... javaParameterTypes) {
            selectionMode.addExact(javaMethodName, javaParameterTypes);
            return this;
        }

        @Override
        public List<FunctionDescriptor> toList() {
            List<ImportedMethod> importedMethods = discover(source, selectionMode);
            validateSelectionTargets(selectionMode, importedMethods);
            validateRenames(importedMethods);

            List<ImportedDescriptor> importedDescriptors = new ArrayList<>(importedMethods.size());
            Set<FunctionSignature> signatures = new LinkedHashSet<>();
            for (ImportedMethod importedMethod : importedMethods) {
                String languageName = renames.getOrDefault(importedMethod.method().getName(), importedMethod.method().getName());
                FunctionDescriptor descriptor = descriptor(languageName, source, importedMethod);
                if (!signatures.add(descriptor.signature())) {
                    throw new IllegalArgumentException("duplicate function signature imported: "
                            + descriptor.signature().canonical());
                }
                importedDescriptors.add(new ImportedDescriptor(descriptor, importedMethod.method().getName()));
            }

            importedDescriptors.sort(Comparator
                    .comparing((ImportedDescriptor imported) -> imported.descriptor().signature())
                    .thenComparing(ImportedDescriptor::javaMethodName));
            return importedDescriptors.stream()
                    .map(ImportedDescriptor::descriptor)
                    .toList();
        }

        private void addRename(String javaMethodName, String languageName) {
            javaMethodName = requireMethodName(javaMethodName);
            languageName = FunctionSignature.validateLanguageName(languageName);
            String previous = renames.putIfAbsent(javaMethodName, languageName);
            if (previous != null && !previous.equals(languageName)) {
                throw new IllegalArgumentException("java method already renamed differently: " + javaMethodName);
            }
        }

        private void validateRenames(List<ImportedMethod> importedMethods) {
            Set<String> importedNames = new LinkedHashSet<>();
            for (ImportedMethod importedMethod : importedMethods) {
                importedNames.add(importedMethod.method().getName());
            }
            for (String renamedMethod : renames.keySet()) {
                if (!importedNames.contains(renamedMethod)) {
                    throw new IllegalArgumentException("rename has no imported target: " + renamedMethod);
                }
            }
        }
    }

    private static FunctionDescriptor descriptor(String languageName, Source source, ImportedMethod importedMethod) {
        return descriptor(languageName, source, importedMethod, LOOKUP, false);
    }

    private static FunctionDescriptor descriptor(
            String languageName,
            Source source,
            ImportedMethod importedMethod,
            MethodHandles.Lookup lookup,
            boolean guardNulls) {
        Method method = importedMethod.method();
        try {
            MethodHandle handle = lookup.unreflect(method);
            if (!source.importStatic()) {
                handle = handle.bindTo(source.providerInstance());
            }
            handle = FunctionHandleAdapters.adapt(handle, importedMethod.parameterTypes(), importedMethod.returnType());
            if (guardNulls) {
                handle = FunctionHandleAdapters.guardNonNullBoundaries(handle);
            }
            return FunctionDescriptor.fromHandle(
                    languageName,
                    handle,
                    source.importStatic()
                            ? FunctionImplementationMetadata.forStaticMethod(method)
                            : FunctionImplementationMetadata.forInstanceMethod(method),
                    importedMethod.parameterTypes(),
                    importedMethod.returnType(),
                    source.purity());
        } catch (IllegalAccessException exception) {
            throw new IllegalArgumentException("function provider method is not accessible: " + method, exception);
        }
    }

    private static List<ImportedMethod> discover(Source source, SelectionMode selectionMode) {
        List<ImportedMethod> importedMethods = new ArrayList<>();
        for (Method method : source.exposureType().getDeclaredMethods()) {
            if (!isEligible(method, source) || !selectionMode.matches(method)) {
                continue;
            }
            importedMethods.add(importMethod(method));
        }
        return importedMethods;
    }

    private static boolean isEligible(Method method, Source source) {
        int modifiers = method.getModifiers();
        if (!Modifier.isPublic(modifiers) || method.isBridge() || method.isSynthetic()) {
            return false;
        }
        return source.importStatic() == Modifier.isStatic(modifiers);
    }

    private static ImportedMethod importMethod(Method method) {
        if (method.isVarArgs()) {
            throw new IllegalArgumentException("varargs provider methods are not supported: " + method);
        }
        List<ExpressionType> parameterTypes = new ArrayList<>(method.getParameterCount());
        Type[] genericParameterTypes = method.getGenericParameterTypes();
        Class<?>[] rawParameterTypes = method.getParameterTypes();
        for (int index = 0; index < genericParameterTypes.length; index++) {
            parameterTypes.add(expressionType(genericParameterTypes[index], rawParameterTypes[index], TypePosition.PARAMETER));
        }
        ExpressionType returnType = expressionType(method.getGenericReturnType(), method.getReturnType(), TypePosition.RETURN);
        return new ImportedMethod(method, List.copyOf(parameterTypes), returnType);
    }

    private static ImportedMethod importCanonicalScalarMethod(Method method) {
        if (method.isVarArgs()) {
            throw new IllegalArgumentException("varargs provider methods are not supported: " + method);
        }
        List<ExpressionType> parameterTypes = new ArrayList<>(method.getParameterCount());
        for (Class<?> parameterType : method.getParameterTypes()) {
            parameterTypes.add(canonicalScalarType(parameterType));
        }
        return new ImportedMethod(method, List.copyOf(parameterTypes), canonicalScalarType(method.getReturnType()));
    }

    private static ExpressionType canonicalScalarType(Class<?> rawType) {
        if (rawType == BigDecimal.class) {
            return ScalarType.NUMBER;
        }
        if (rawType == boolean.class || rawType == Boolean.class) {
            return ScalarType.BOOLEAN;
        }
        if (rawType == String.class) {
            return ScalarType.STRING;
        }
        if (rawType == LocalDate.class) {
            return ScalarType.DATE;
        }
        if (rawType == LocalTime.class) {
            return ScalarType.TIME;
        }
        if (rawType == LocalDateTime.class) {
            return ScalarType.DATETIME;
        }
        throw new IllegalArgumentException("unsupported canonical scalar provider method type: " + rawType.getName());
    }

    private static ExpressionType expressionType(Type genericType, Class<?> rawType, TypePosition position) {
        if (rawType == void.class) {
            throw new IllegalArgumentException("void provider method returns are not supported");
        }
        if (rawType.isArray()) {
            throw new IllegalArgumentException("array provider method types are not supported");
        }
        if (Map.class.isAssignableFrom(rawType)) {
            throw new IllegalArgumentException("Map provider method types are not supported");
        }
        if (Optional.class.isAssignableFrom(rawType)) {
            throw new IllegalArgumentException("Optional provider method types are not supported");
        }
        if (NUMERIC_TYPES.contains(rawType)) {
            return ScalarType.NUMBER;
        }
        if (rawType == boolean.class || rawType == Boolean.class) {
            return ScalarType.BOOLEAN;
        }
        if (rawType == String.class) {
            return ScalarType.STRING;
        }
        if (rawType == LocalDate.class) {
            return ScalarType.DATE;
        }
        if (rawType == LocalTime.class) {
            return ScalarType.TIME;
        }
        if (rawType == LocalDateTime.class) {
            return ScalarType.DATETIME;
        }
        if (rawType == Object.class) {
            throw new IllegalArgumentException("Object provider method types are not supported");
        }
        if (List.class.isAssignableFrom(rawType)) {
            return vectorType(genericType, "raw List");
        }
        if (Collection.class.isAssignableFrom(rawType)) {
            if (position == TypePosition.RETURN) {
                throw new IllegalArgumentException("Collection return types are not supported");
            }
            return vectorType(genericType, "raw Collection");
        }
        throw new IllegalArgumentException("unsupported provider method type: " + rawType.getName());
    }

    private static ExpressionType vectorType(Type genericType, String rawMessage) {
        if (!(genericType instanceof ParameterizedType parameterizedType)) {
            throw new IllegalArgumentException(rawMessage + " provider method types are not supported");
        }
        Type elementType = parameterizedType.getActualTypeArguments()[0];
        if (elementType instanceof WildcardType wildcardType) {
            if (wildcardType.getLowerBounds().length == 0
                    && wildcardType.getUpperBounds().length == 1
                    && wildcardType.getUpperBounds()[0] == Object.class) {
                throw new IllegalArgumentException("wildcard vector element types are not supported");
            }
            throw new IllegalArgumentException("bounded wildcard vector element types are not supported");
        }
        if (!(elementType instanceof Class<?> elementClass)) {
            throw new IllegalArgumentException("unresolvable vector element type is not supported");
        }
        return new VectorType(expressionType(elementClass, elementClass, TypePosition.VECTOR_ELEMENT));
    }

    private static void validateSelectionTargets(SelectionMode selectionMode, List<ImportedMethod> importedMethods) {
        if (selectionMode.isAll()) {
            return;
        }
        for (String selectedName : selectionMode.names()) {
            boolean found = importedMethods.stream().anyMatch(imported -> imported.method().getName().equals(selectedName));
            if (!found) {
                throw new IllegalArgumentException("selected method has no imported target: " + selectedName);
            }
        }
        for (MethodKey selectedMethod : selectionMode.exactMethods()) {
            boolean found = importedMethods.stream().anyMatch(imported -> selectedMethod.matches(imported.method()));
            if (!found) {
                throw new IllegalArgumentException("selected method has no imported target: " + selectedMethod);
            }
        }
    }

    private static String requireMethodName(String javaMethodName) {
        Objects.requireNonNull(javaMethodName, "javaMethodName");
        if (javaMethodName.isBlank()) {
            throw new IllegalArgumentException("javaMethodName must not be blank");
        }
        return javaMethodName;
    }

    private enum TypePosition {
        PARAMETER,
        RETURN,
        VECTOR_ELEMENT
    }

    private record Source(
            Class<?> exposureType,
            Object providerInstance,
            FunctionPurity purity,
            boolean importStatic) {

        private Source {
            exposureType = Objects.requireNonNull(exposureType, "exposureType");
            purity = Objects.requireNonNull(purity, "purity");
            if (!importStatic) {
                Objects.requireNonNull(providerInstance, "providerInstance");
                if (!exposureType.isInstance(providerInstance)) {
                    throw new IllegalArgumentException("providerInstance must be an instance of exposureType");
                }
            }
        }

        private static Source staticProvider(Class<?> providerClass, FunctionPurity purity) {
            return new Source(Objects.requireNonNull(providerClass, "providerClass"), null, purity, true);
        }

        private static Source instanceProvider(
                Class<?> exposureType,
                Object providerInstance,
                FunctionPurity purity) {
            return new Source(exposureType, providerInstance, purity, false);
        }

        private static Source exposedInstanceProvider(
                Class<?> exposureType,
                Object providerInstance,
                FunctionPurity purity) {
            return new Source(exposureType, providerInstance, purity, false);
        }
    }

    private static final class SelectionMode {

        private final boolean all;
        private final Set<String> names = new LinkedHashSet<>();
        private final Set<MethodKey> exactMethods = new LinkedHashSet<>();

        private SelectionMode(boolean all) {
            this.all = all;
        }

        private static SelectionMode allMode() {
            return new SelectionMode(true);
        }

        private static SelectionMode selectedMode() {
            return new SelectionMode(false);
        }

        private boolean isAll() {
            return all;
        }

        private Set<String> names() {
            return names;
        }

        private Set<MethodKey> exactMethods() {
            return exactMethods;
        }

        private void addName(String javaMethodName) {
            ensureSelectedMode();
            names.add(requireMethodName(javaMethodName));
        }

        private void addExact(String javaMethodName, Class<?>... javaParameterTypes) {
            ensureSelectedMode();
            exactMethods.add(new MethodKey(javaMethodName, javaParameterTypes));
        }

        private boolean matches(Method method) {
            if (all) {
                return true;
            }
            if (names.contains(method.getName())) {
                return true;
            }
            for (MethodKey exactMethod : exactMethods) {
                if (exactMethod.matches(method)) {
                    return true;
                }
            }
            return false;
        }

        private void ensureSelectedMode() {
            if (all) {
                throw new IllegalStateException("selection is not available for importAll plans");
            }
        }
    }

    private record MethodKey(String name, List<Class<?>> parameterTypes) {

        private MethodKey(String name, Class<?>... parameterTypes) {
            this(requireMethodName(name), copyParameterTypes(parameterTypes));
        }

        private boolean matches(Method method) {
            return name.equals(method.getName()) && parameterTypes.equals(List.of(method.getParameterTypes()));
        }

        @Override
        public String toString() {
            return name + parameterTypes;
        }

        private static List<Class<?>> copyParameterTypes(Class<?>[] parameterTypes) {
            Objects.requireNonNull(parameterTypes, "parameterTypes");
            ArrayList<Class<?>> copy = new ArrayList<>(parameterTypes.length);
            for (Class<?> parameterType : parameterTypes) {
                copy.add(Objects.requireNonNull(parameterType, "parameterType"));
            }
            return List.copyOf(copy);
        }
    }

    private record ImportedMethod(Method method, List<ExpressionType> parameterTypes, ExpressionType returnType) {
    }

    private record ImportedDescriptor(FunctionDescriptor descriptor, String javaMethodName) {
    }

    static final class EnvironmentImport {

        private final Source source;

        private EnvironmentImport(Source source) {
            this.source = source;
        }

        static EnvironmentImport staticProvider(Class<?> providerClass, FunctionPurity purity) {
            return new EnvironmentImport(Source.staticProvider(providerClass, purity));
        }

        static EnvironmentImport instanceProvider(Object providerInstance, FunctionPurity purity) {
            Objects.requireNonNull(providerInstance, "providerInstance");
            return new EnvironmentImport(Source.instanceProvider(
                    providerInstance.getClass(), providerInstance, purity));
        }

        static EnvironmentImport exposedInstanceProvider(
                Class<?> exposureType,
                Object providerInstance,
                FunctionPurity purity) {
            return new EnvironmentImport(Source.exposedInstanceProvider(exposureType, providerInstance, purity));
        }

        EnvironmentImportResolution resolve() {
            List<ImportedMethod> importedMethods = new ArrayList<>();
            List<IllegalArgumentException> failures = new ArrayList<>();
            List<Method> declaredMethods = Arrays.stream(source.exposureType().getDeclaredMethods())
                    .sorted(Comparator.comparing(Method::toGenericString))
                    .toList();
            int eligibleMethodCount = 0;
            for (Method method : declaredMethods) {
                if (isEligible(method, source)) {
                    eligibleMethodCount++;
                    try {
                        importedMethods.add(importCanonicalScalarMethod(method));
                    } catch (IllegalArgumentException exception) {
                        failures.add(providerFailure(method.toGenericString() + ": " + exception.getMessage(), exception));
                    }
                }
            }
            if (eligibleMethodCount == 0) {
                failures.add(new IllegalArgumentException(
                        "function provider declares no eligible methods: " + source.exposureType().getName()));
            }

            List<FunctionDescriptor> descriptors = new ArrayList<>(importedMethods.size());
            Set<FunctionSignature> signatures = new LinkedHashSet<>();
            for (ImportedMethod importedMethod : importedMethods) {
                try {
                    FunctionDescriptor descriptor = descriptor(
                            importedMethod.method().getName(), source, importedMethod, PUBLIC_LOOKUP, true);
                    if (!signatures.add(descriptor.signature())) {
                        IllegalArgumentException exception = new IllegalArgumentException(
                                "duplicate function signature imported: " + descriptor.signature().canonical());
                        failures.add(providerFailure(exception.getMessage(), exception));
                    } else {
                        descriptors.add(descriptor);
                    }
                } catch (IllegalArgumentException exception) {
                    failures.add(providerFailure(
                            importedMethod.method().toGenericString() + ": " + exception.getMessage(), exception));
                }
            }
            descriptors.sort(Comparator.comparing(FunctionDescriptor::signature));
            failures.sort(Comparator.comparing(Throwable::getMessage));
            return new EnvironmentImportResolution(descriptors, failures);
        }

        private IllegalArgumentException providerFailure(String detail, IllegalArgumentException cause) {
            return new IllegalArgumentException(
                    "invalid function provider " + source.exposureType().getName() + ": " + detail, cause);
        }
    }

    record EnvironmentImportResolution(
            List<FunctionDescriptor> descriptors,
            List<IllegalArgumentException> failures) {

        EnvironmentImportResolution {
            descriptors = List.copyOf(descriptors);
            failures = List.copyOf(failures);
        }
    }
}
