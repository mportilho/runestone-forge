package com.runestone.expeval_mk3.api;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Imports explicitly supplied Java function providers into expression function descriptors.
 */
public final class ReflectedFunctionImporter {

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private static final MethodHandles.Lookup PUBLIC_LOOKUP = MethodHandles.publicLookup();

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

    public interface ImportPlan {
        ImportPlan rename(String javaMethodName, String languageName);

        ImportPlan rename(String javaMethodName, Class<?> javaParameterType, String languageName);

        ImportPlan rename(String javaMethodName, String languageName, Class<?>... javaParameterTypes);
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

        private ImportResolution resolve(
                MethodHandles.Lookup lookup,
                JavaTypeCatalog javaTypes,
                BoundaryCoercion boundaryCoercion,
                int maxMaterializedSize) {
            List<Method> eligibleMethods = eligibleMethods(source);
            List<ProviderConfigurationProblem> problems = new ArrayList<>();
            if (eligibleMethods.isEmpty()) {
                problems.add(ProviderConfigurationProblem.providerLevel(
                        ProviderConfigurationProblem.Category.NO_ELIGIBLE_METHODS,
                        source.exposureType(),
                        source.purity(),
                        "function provider declares no eligible methods: " + source.exposureType().getName(),
                        null));
            }

            List<Method> selectedMethods = eligibleMethods.stream()
                    .filter(selectionMode::matches)
                    .toList();
            validateSelectionTargets(selectionMode, source.exposureType(), source.purity(), selectedMethods, problems);
            validateRenames(selectedMethods, problems);

            List<FunctionDescriptor> descriptors = new ArrayList<>(selectedMethods.size());
            for (Method method : selectedMethods) {
                try {
                    String languageName = languageName(method);
                    ProviderMethodAdapter.PreparedMethod prepared = ProviderMethodAdapter.prepare(
                            method, javaTypes, boundaryCoercion, maxMaterializedSize);
                    descriptors.add(descriptor(languageName, source, method, prepared, lookup));
                } catch (IllegalArgumentException exception) {
                    problems.add(ProviderConfigurationProblem.methodLevel(
                            ProviderConfigurationProblem.Category.METHOD_REJECTED,
                            source.exposureType(),
                            method,
                            source.purity(),
                            exception.getMessage(),
                            exception));
                }
            }
            problems.sort(ProviderConfigurationProblem.ORDER);
            return new ImportResolution(descriptors, problems);
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

        private void validateRenames(List<Method> selectedMethods, List<ProviderConfigurationProblem> problems) {
            for (Rename rename : renames) {
                if (selectedMethods.stream().noneMatch(rename::matches)) {
                    problems.add(ProviderConfigurationProblem.providerLevel(
                            ProviderConfigurationProblem.Category.RENAME,
                            source.exposureType(),
                            source.purity(),
                            "invalid function provider " + source.exposureType().getName()
                                    + ": rename has no imported target: " + rename.target(),
                            null));
                }
            }
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

    private static FunctionDescriptor descriptor(
            String languageName,
            Source source,
            Method method,
            ProviderMethodAdapter.PreparedMethod prepared,
            MethodHandles.Lookup lookup) {
        try {
            MethodHandle handle = lookup.unreflect(method);
            if (!source.importStatic()) {
                handle = handle.bindTo(source.providerInstance());
            }
            handle = prepared.adapt(handle);
            return FunctionDescriptor.fromHandle(
                    languageName,
                    handle,
                    source.importStatic()
                            ? FunctionImplementationMetadata.forStaticMethod(method)
                            : FunctionImplementationMetadata.forInstanceMethod(method),
                    prepared.parameterTypes(),
                    prepared.returnType(),
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

    private static void validateSelectionTargets(
            SelectionMode selectionMode,
            Class<?> exposureType,
            FunctionPurity purity,
            List<Method> selectedMethods,
            List<ProviderConfigurationProblem> problems) {
        if (selectionMode.isAll()) {
            return;
        }
        for (String selectedName : selectionMode.names()) {
            boolean found = selectedMethods.stream().anyMatch(method -> method.getName().equals(selectedName));
            if (!found) {
                problems.add(ProviderConfigurationProblem.providerLevel(
                        ProviderConfigurationProblem.Category.SELECTION,
                        exposureType,
                        purity,
                        "selected method has no imported target: " + selectedName,
                        null));
            }
        }
        for (MethodKey selectedMethod : selectionMode.exactMethods()) {
            boolean found = selectedMethods.stream().anyMatch(selectedMethod::matches);
            if (!found) {
                problems.add(ProviderConfigurationProblem.providerLevel(
                        ProviderConfigurationProblem.Category.SELECTION,
                        exposureType,
                        purity,
                        "selected method has no imported target: " + selectedMethod,
                        null));
            }
        }
        if (selectionMode.names().isEmpty() && selectionMode.exactMethods().isEmpty()) {
            problems.add(ProviderConfigurationProblem.providerLevel(
                    ProviderConfigurationProblem.Category.SELECTION,
                    exposureType,
                    purity,
                    "selective function import declares no methods",
                    null));
        }
    }

    private static String requireMethodName(String javaMethodName) {
        Objects.requireNonNull(javaMethodName, "javaMethodName");
        if (javaMethodName.isBlank()) {
            throw new IllegalArgumentException("javaMethodName must not be blank");
        }
        return javaMethodName;
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

    /**
     * Resolves an arbitrary caller-supplied plan against the real environment configuration, using
     * the public lookup: external provider classes and their exposed methods must be public. This
     * is the sole entry point used while building an {@link ExpressionEnvironment}.
     */
    static ImportResolution resolveForEnvironment(
            ImportPlan plan,
            JavaTypeCatalog javaTypes,
            BoundaryCoercion boundaryCoercion,
            int maxMaterializedSize) {
        return resolve(PUBLIC_LOOKUP, plan, javaTypes, boundaryCoercion, maxMaterializedSize);
    }

    private static ImportResolution resolve(
            MethodHandles.Lookup lookup,
            ImportPlan plan,
            JavaTypeCatalog javaTypes,
            BoundaryCoercion boundaryCoercion,
            int maxMaterializedSize) {
        Objects.requireNonNull(plan, "plan");
        Objects.requireNonNull(javaTypes, "javaTypes");
        Objects.requireNonNull(boundaryCoercion, "boundaryCoercion");
        if (!(plan instanceof ImportPlanImpl implementation)) {
            throw new IllegalArgumentException("unsupported reflected function import plan implementation");
        }
        return implementation.resolve(lookup, javaTypes, boundaryCoercion, maxMaterializedSize);
    }

    /**
     * Resolves a plan against a standard, unbounded environment and returns its descriptors in
     * deterministic order, or throws if any configuration problem was discovered. Package-visible
     * only, using the module-private lookup so package-private provider classes (built-ins, and
     * test fixtures within this package) remain importable without opening them up publicly.
     */
    static List<FunctionDescriptor> toList(ImportPlan plan) {
        ImportResolution resolution = resolve(
                LOOKUP, plan, JavaTypeCatalog.empty(), BoundaryCoercion.standard(), Integer.MAX_VALUE);
        if (!resolution.failures().isEmpty()) {
            throw combinedFailure(resolution.failures());
        }
        validateUniqueSignatures(resolution.descriptors());
        return sortedDescriptors(resolution.descriptors());
    }

    /**
     * Resolves a trusted internal (built-in) plan against the real environment configuration,
     * using the module-private lookup, and returns its descriptors, or fails fast with
     * {@link IllegalStateException} since a rejected built-in method is a defect in the engine, not
     * a caller configuration error.
     */
    static List<FunctionDescriptor> importTrustedOrThrow(ImportPlan plan, BuiltInResolutionContext context) {
        ImportResolution resolution = resolve(
                LOOKUP, plan, context.javaTypes(), context.boundaryCoercion(), context.maxMaterializedSize());
        if (!resolution.failures().isEmpty()) {
            throw new IllegalStateException("built-in function provider misconfigured: " + resolution.failures()
                    .stream()
                    .map(ProviderConfigurationProblem::message)
                    .distinct()
                    .reduce((first, second) -> first + "; " + second)
                    .orElseThrow());
        }
        validateUniqueSignatures(resolution.descriptors());
        return sortedDescriptors(resolution.descriptors());
    }

    record ImportResolution(
            List<FunctionDescriptor> descriptors,
            List<ProviderConfigurationProblem> failures) {

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

    private static EnvironmentConfigurationException combinedFailure(List<ProviderConfigurationProblem> problems) {
        return EnvironmentConfigurationException.of(problems);
    }

}
