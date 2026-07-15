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
        return new ImportPlanImpl(Source.staticProvider(providerClass, purity), SelectionMode.allMode(), List.of());
    }

    public static ImportPlan importAll(Object providerInstance, FunctionPurity purity) {
        Objects.requireNonNull(providerInstance, "providerInstance");
        return new ImportPlanImpl(
                Source.instanceProvider(providerInstance.getClass(), providerInstance, purity),
                SelectionMode.allMode(),
                List.of());
    }

    public static ImportPlan importAll(
            Class<?> exposureType,
            Object providerInstance,
            FunctionPurity purity) {
        return new ImportPlanImpl(
                Source.exposedInstanceProvider(exposureType, providerInstance, purity),
                SelectionMode.allMode(),
                List.of());
    }

    public static Selection importSelected(Class<?> providerClass, FunctionPurity purity) {
        return new SelectionPlanImpl(
                Source.staticProvider(providerClass, purity), SelectionMode.selectedMode(), List.of());
    }

    public static Selection importSelected(Object providerInstance, FunctionPurity purity) {
        Objects.requireNonNull(providerInstance, "providerInstance");
        return new SelectionPlanImpl(
                Source.instanceProvider(providerInstance.getClass(), providerInstance, purity),
                SelectionMode.selectedMode(),
                List.of());
    }

    public static Selection importSelected(
            Class<?> exposureType,
            Object providerInstance,
            FunctionPurity purity) {
        return new SelectionPlanImpl(
                Source.exposedInstanceProvider(exposureType, providerInstance, purity),
                SelectionMode.selectedMode(),
                List.of());
    }

    static ImportPlan importCanonicalAll(Class<?> providerClass, FunctionPurity purity) {
        return new ImportPlanImpl(
                Source.canonicalStaticProvider(providerClass, purity), SelectionMode.allMode(), List.of());
    }

    static ImportPlan importCanonicalAll(Object providerInstance, FunctionPurity purity) {
        Objects.requireNonNull(providerInstance, "providerInstance");
        return new ImportPlanImpl(
                Source.canonicalInstanceProvider(providerInstance.getClass(), providerInstance, purity),
                SelectionMode.allMode(),
                List.of());
    }

    static ImportPlan importCanonicalAll(
            Class<?> exposureType,
            Object providerInstance,
            FunctionPurity purity) {
        return new ImportPlanImpl(
                Source.canonicalInstanceProvider(exposureType, providerInstance, purity),
                SelectionMode.allMode(),
                List.of());
    }

    public interface ImportPlan {
        ImportPlan rename(String javaMethodName, String languageName);

        ImportPlan rename(String javaMethodName, Class<?> javaParameterType, String languageName);

        ImportPlan rename(String javaMethodName, String languageName, Class<?>... javaParameterTypes);

        List<FunctionDescriptor> toList();
    }

    public interface Selection extends ImportPlan {
        Selection methods(String... javaMethodNames);

        Selection method(String javaMethodName, Class<?>... javaParameterTypes);

        @Override
        Selection rename(String javaMethodName, String languageName);

        @Override
        Selection rename(String javaMethodName, Class<?> javaParameterType, String languageName);

        @Override
        Selection rename(String javaMethodName, String languageName, Class<?>... javaParameterTypes);
    }

    private static class ImportPlanImpl implements ImportPlan {

        private final Source source;
        private final SelectionMode selectionMode;
        private final List<Rename> renames;

        private ImportPlanImpl(Source source, SelectionMode selectionMode, List<Rename> renames) {
            this.source = source;
            this.selectionMode = selectionMode;
            this.renames = List.copyOf(renames);
        }

        @Override
        public ImportPlan rename(String javaMethodName, String languageName) {
            return withRename(Rename.byName(javaMethodName, languageName));
        }

        @Override
        public ImportPlan rename(String javaMethodName, Class<?> javaParameterType, String languageName) {
            Objects.requireNonNull(javaParameterType, "javaParameterType");
            return rename(javaMethodName, languageName, javaParameterType);
        }

        @Override
        public ImportPlan rename(String javaMethodName, String languageName, Class<?>... javaParameterTypes) {
            return withRename(Rename.exact(javaMethodName, javaParameterTypes, languageName));
        }

        @Override
        public List<FunctionDescriptor> toList() {
            ImportResolution resolution = resolve(LOOKUP, false, JavaTypeCatalog.empty());
            if (!resolution.failures().isEmpty()) {
                throw combinedFailure(resolution.failures());
            }
            validateUniqueSignatures(resolution.descriptors());
            return sortedDescriptors(resolution.descriptors());
        }

        private ImportResolution resolve(
                MethodHandles.Lookup lookup,
                boolean guardNulls,
                JavaTypeCatalog javaTypes) {
            List<Method> eligibleMethods = eligibleMethods(source);
            List<IllegalArgumentException> failures = new ArrayList<>();
            if (eligibleMethods.isEmpty()) {
                failures.add(new IllegalArgumentException(
                        "function provider declares no eligible methods: " + source.exposureType().getName()));
            }

            List<Method> selectedMethods = eligibleMethods.stream()
                    .filter(selectionMode::matches)
                    .toList();
            validateSelectionTargets(selectionMode, selectedMethods, failures);
            validateRenames(selectedMethods, failures);

            List<FunctionDescriptor> descriptors = new ArrayList<>(selectedMethods.size());
            for (Method method : selectedMethods) {
                try {
                    ImportedMethod importedMethod = source.canonicalScalarsOnly()
                            ? importCanonicalScalarMethod(method)
                            : importMethod(method, javaTypes);
                    descriptors.add(descriptor(
                            languageName(method), source, importedMethod, lookup, guardNulls));
                } catch (IllegalArgumentException exception) {
                    failures.add(providerFailure(method.toGenericString() + ": " + exception.getMessage(), exception));
                }
            }
            failures.sort(Comparator.comparing(Throwable::getMessage));
            return new ImportResolution(descriptors, failures);
        }

        private ImportPlanImpl withRename(Rename rename) {
            ArrayList<Rename> updated = new ArrayList<>(renames.size() + 1);
            updated.addAll(renames);
            updated.add(rename);
            return copy(selectionMode, updated);
        }

        ImportPlanImpl copy(SelectionMode updatedSelectionMode, List<Rename> updatedRenames) {
            return new ImportPlanImpl(source, updatedSelectionMode, updatedRenames);
        }

        private String languageName(Method method) {
            List<String> languageNames = renames.stream()
                    .filter(rename -> rename.matches(method))
                    .map(Rename::languageName)
                    .distinct()
                    .sorted()
                    .toList();
            if (languageNames.size() > 1) {
                throw new IllegalArgumentException("Java method has conflicting renames: " + method.toGenericString());
            }
            String languageName = languageNames.isEmpty() ? method.getName() : languageNames.getFirst();
            return FunctionSignature.validateLanguageName(languageName);
        }

        private void validateRenames(List<Method> selectedMethods, List<IllegalArgumentException> failures) {
            for (Rename rename : renames) {
                if (selectedMethods.stream().noneMatch(rename::matches)) {
                    failures.add(providerFailure("rename has no imported target: " + rename.target(), null));
                }
            }
        }

        private IllegalArgumentException providerFailure(String detail, IllegalArgumentException cause) {
            return new IllegalArgumentException(
                    "invalid function provider " + source.exposureType().getName() + ": " + detail, cause);
        }
    }

    private static final class SelectionPlanImpl extends ImportPlanImpl implements Selection {

        private SelectionPlanImpl(Source source, SelectionMode selectionMode, List<Rename> renames) {
            super(source, selectionMode, renames);
        }

        @Override
        public Selection rename(String javaMethodName, String languageName) {
            return (Selection) super.rename(javaMethodName, languageName);
        }

        @Override
        public Selection rename(String javaMethodName, Class<?> javaParameterType, String languageName) {
            return (Selection) super.rename(javaMethodName, javaParameterType, languageName);
        }

        @Override
        public Selection rename(String javaMethodName, String languageName, Class<?>... javaParameterTypes) {
            return (Selection) super.rename(javaMethodName, languageName, javaParameterTypes);
        }

        @Override
        public Selection methods(String... javaMethodNames) {
            Objects.requireNonNull(javaMethodNames, "javaMethodNames");
            if (javaMethodNames.length == 0) {
                throw new IllegalArgumentException("javaMethodNames must not be empty");
            }
            SelectionMode updated = selectionMode();
            for (String javaMethodName : javaMethodNames) {
                updated = updated.withName(javaMethodName);
            }
            return (Selection) copy(updated, renames());
        }

        @Override
        public Selection method(String javaMethodName, Class<?>... javaParameterTypes) {
            return (Selection) copy(
                    selectionMode().withExact(javaMethodName, javaParameterTypes), renames());
        }

        @Override
        ImportPlanImpl copy(SelectionMode updatedSelectionMode, List<Rename> updatedRenames) {
            return new SelectionPlanImpl(source(), updatedSelectionMode, updatedRenames);
        }

        private Source source() {
            return super.source;
        }

        private SelectionMode selectionMode() {
            return super.selectionMode;
        }

        private List<Rename> renames() {
            return super.renames;
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

    private static List<Method> eligibleMethods(Source source) {
        return Arrays.stream(source.exposureType().getDeclaredMethods())
                .sorted(Comparator.comparing(Method::toGenericString))
                .filter(method -> isEligible(method, source))
                .toList();
    }

    private static boolean isEligible(Method method, Source source) {
        int modifiers = method.getModifiers();
        if (!Modifier.isPublic(modifiers) || method.isBridge() || method.isSynthetic()) {
            return false;
        }
        return source.importStatic() == Modifier.isStatic(modifiers);
    }

    private static ImportedMethod importMethod(Method method, JavaTypeCatalog javaTypes) {
        if (method.isVarArgs()) {
            throw new IllegalArgumentException("varargs provider methods are not supported: " + method);
        }
        List<ExpressionType> parameterTypes = new ArrayList<>(method.getParameterCount());
        Type[] genericParameterTypes = method.getGenericParameterTypes();
        Class<?>[] rawParameterTypes = method.getParameterTypes();
        for (int index = 0; index < genericParameterTypes.length; index++) {
            parameterTypes.add(expressionType(
                    genericParameterTypes[index], rawParameterTypes[index], TypePosition.PARAMETER, javaTypes));
        }
        ExpressionType returnType = expressionType(
                method.getGenericReturnType(), method.getReturnType(), TypePosition.RETURN, javaTypes);
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

    private static ExpressionType expressionType(
            Type genericType,
            Class<?> rawType,
            TypePosition position,
            JavaTypeCatalog javaTypes) {
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
            return vectorType(genericType, "raw List", javaTypes);
        }
        if (Collection.class.isAssignableFrom(rawType)) {
            if (position == TypePosition.RETURN) {
                throw new IllegalArgumentException("Collection return types are not supported");
            }
            return vectorType(genericType, "raw Collection", javaTypes);
        }
        return javaTypes.find(rawType)
                .map(JavaTypeDescriptor::objectType)
                .orElseThrow(() -> new IllegalArgumentException(
                        "unsupported provider method type; not a registered Java type: " + rawType.getName()));
    }

    private static ExpressionType vectorType(
            Type genericType,
            String rawMessage,
            JavaTypeCatalog javaTypes) {
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
        return new VectorType(expressionType(
                elementClass, elementClass, TypePosition.VECTOR_ELEMENT, javaTypes));
    }

    private static void validateSelectionTargets(
            SelectionMode selectionMode,
            List<Method> selectedMethods,
            List<IllegalArgumentException> failures) {
        if (selectionMode.isAll()) {
            return;
        }
        for (String selectedName : selectionMode.names()) {
            boolean found = selectedMethods.stream().anyMatch(method -> method.getName().equals(selectedName));
            if (!found) {
                failures.add(new IllegalArgumentException("selected method has no imported target: " + selectedName));
            }
        }
        for (MethodKey selectedMethod : selectionMode.exactMethods()) {
            boolean found = selectedMethods.stream().anyMatch(selectedMethod::matches);
            if (!found) {
                failures.add(new IllegalArgumentException("selected method has no imported target: " + selectedMethod));
            }
        }
        if (selectionMode.names().isEmpty() && selectionMode.exactMethods().isEmpty()) {
            failures.add(new IllegalArgumentException("selective function import declares no methods"));
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
            boolean importStatic,
            boolean canonicalScalarsOnly) {

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
            return new Source(Objects.requireNonNull(providerClass, "providerClass"), null, purity, true, false);
        }

        private static Source instanceProvider(
                Class<?> exposureType,
                Object providerInstance,
                FunctionPurity purity) {
            return new Source(exposureType, providerInstance, purity, false, false);
        }

        private static Source exposedInstanceProvider(
                Class<?> exposureType,
                Object providerInstance,
                FunctionPurity purity) {
            return new Source(exposureType, providerInstance, purity, false, false);
        }

        private static Source canonicalStaticProvider(Class<?> providerClass, FunctionPurity purity) {
            return new Source(Objects.requireNonNull(providerClass, "providerClass"), null, purity, true, true);
        }

        private static Source canonicalInstanceProvider(
                Class<?> exposureType,
                Object providerInstance,
                FunctionPurity purity) {
            return new Source(exposureType, providerInstance, purity, false, true);
        }
    }

    private static final class SelectionMode {

        private final boolean all;
        private final Set<String> names;
        private final Set<MethodKey> exactMethods;

        private SelectionMode(boolean all, Set<String> names, Set<MethodKey> exactMethods) {
            this.all = all;
            this.names = Set.copyOf(names);
            this.exactMethods = Set.copyOf(exactMethods);
        }

        private static SelectionMode allMode() {
            return new SelectionMode(true, Set.of(), Set.of());
        }

        private static SelectionMode selectedMode() {
            return new SelectionMode(false, Set.of(), Set.of());
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

        private SelectionMode withName(String javaMethodName) {
            ensureSelectedMode();
            LinkedHashSet<String> updated = new LinkedHashSet<>(names);
            updated.add(requireMethodName(javaMethodName));
            return new SelectionMode(false, updated, exactMethods);
        }

        private SelectionMode withExact(String javaMethodName, Class<?>... javaParameterTypes) {
            ensureSelectedMode();
            LinkedHashSet<MethodKey> updated = new LinkedHashSet<>(exactMethods);
            updated.add(new MethodKey(javaMethodName, javaParameterTypes));
            return new SelectionMode(false, names, updated);
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

    private record Rename(MethodKey exactMethod, String javaMethodName, String languageName) {

        private Rename {
            if (exactMethod == null) {
                javaMethodName = requireMethodName(javaMethodName);
            }
            Objects.requireNonNull(languageName, "languageName");
        }

        private static Rename byName(String javaMethodName, String languageName) {
            return new Rename(null, javaMethodName, languageName);
        }

        private static Rename exact(String javaMethodName, Class<?>[] parameterTypes, String languageName) {
            return new Rename(new MethodKey(javaMethodName, parameterTypes), null, languageName);
        }

        private boolean matches(Method method) {
            return exactMethod == null
                    ? javaMethodName.equals(method.getName())
                    : exactMethod.matches(method);
        }

        private String target() {
            return exactMethod == null ? javaMethodName : exactMethod.toString();
        }
    }

    static ImportResolution resolveForEnvironment(ImportPlan plan, JavaTypeCatalog javaTypes) {
        Objects.requireNonNull(plan, "plan");
        Objects.requireNonNull(javaTypes, "javaTypes");
        if (!(plan instanceof ImportPlanImpl implementation)) {
            throw new IllegalArgumentException("unsupported reflected function import plan implementation");
        }
        return implementation.resolve(PUBLIC_LOOKUP, true, javaTypes);
    }

    record ImportResolution(
            List<FunctionDescriptor> descriptors,
            List<IllegalArgumentException> failures) {

        ImportResolution {
            descriptors = List.copyOf(descriptors);
            failures = List.copyOf(failures);
        }
    }

    private static List<FunctionDescriptor> sortedDescriptors(List<FunctionDescriptor> descriptors) {
        return descriptors.stream()
                .sorted(Comparator
                        .comparing(FunctionDescriptor::signature)
                        .thenComparing(descriptor -> descriptor.implementationMetadata().owner())
                        .thenComparing(descriptor -> descriptor.implementationMetadata().methodType()))
                .toList();
    }

    private static void validateUniqueSignatures(List<FunctionDescriptor> descriptors) {
        Map<FunctionSignature, List<FunctionDescriptor>> bySignature = descriptors.stream()
                .collect(java.util.stream.Collectors.groupingBy(FunctionDescriptor::signature));
        bySignature.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .sorted(Map.Entry.comparingByKey())
                .findFirst()
                .ifPresent(entry -> {
                    List<String> methods = entry.getValue().stream()
                            .map(FunctionDescriptor::implementationMetadata)
                            .map(FunctionImplementationMetadata::describeImplementation)
                            .sorted()
                            .toList();
                    throw new IllegalArgumentException("duplicate function signature imported: "
                            + entry.getKey().canonical() + " from " + String.join(" and ", methods));
                });
    }

    private static IllegalArgumentException combinedFailure(List<IllegalArgumentException> failures) {
        IllegalArgumentException failure = new IllegalArgumentException(failures.stream()
                .map(Throwable::getMessage)
                .distinct()
                .sorted()
                .reduce((first, second) -> first + "; " + second)
                .orElseThrow(), failures.getFirst());
        for (int index = 1; index < failures.size(); index++) {
            failure.addSuppressed(failures.get(index));
        }
        return failure;
    }

}
