package com.runestone.expeval_mk3.api;

import com.runestone.converters.DataConversionService;

import java.math.MathContext;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Immutable compilation configuration for expressions.
 */
public final class ExpressionEnvironment {

    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.systemDefault();
    private static final MathContext DEFAULT_MATH_CONTEXT = MathContext.DECIMAL128;
    private static final MathContext DEFAULT_TRANSCENDENTAL_MATH_CONTEXT = MathContext.DECIMAL128;
    private static final int DEFAULT_MAX_CURRENT_ITEM_DEPTH = 32;
    private static final int DEFAULT_MAX_MATERIALIZED_SIZE = 10_000;
    private static final int DEFAULT_MAX_FACTORIAL_INPUT = 1_000;
    private static final ExpressionEnvironment STANDARD = builder().build();

    private final ZoneId zoneId;
    private final MathContext mathContext;
    private final MathContext transcendentalMathContext;
    private final int maxCurrentItemDepth;
    private final int maxMaterializedSize;
    private final int maxFactorialInput;
    private final String conversionProfileIdentity;
    private final String conversionProfileHash;
    private final BoundaryCoercion boundaryCoercion;
    private final ExternalSymbolCatalog externalSymbols;
    private final FunctionCatalog functions;
    private final JavaTypeCatalog javaTypes;
    private final CollectionOperationCatalog collectionOperations;
    private final String environmentId;

    private ExpressionEnvironment(Builder builder, CollectionOperationCatalog collectionOperations) {
        zoneId = builder.zoneId;
        mathContext = builder.mathContext;
        transcendentalMathContext = builder.transcendentalMathContext;
        maxCurrentItemDepth = builder.maxCurrentItemDepth;
        maxMaterializedSize = builder.maxMaterializedSize;
        maxFactorialInput = builder.maxFactorialInput;
        boundaryCoercion = builder.boundaryCoercion;
        conversionProfileIdentity = boundaryCoercion.profileIdentity();
        conversionProfileHash = boundaryCoercion.profileHash();
        externalSymbols = builder.externalSymbols.build(boundaryCoercion);
        javaTypes = builder.javaTypes.build();
        functions = buildFunctions(builder, javaTypes);
        CollectionOperationCatalog.validateOfficial(collectionOperations);
        this.collectionOperations = collectionOperations;
        environmentId = UUID.randomUUID().toString();
    }

    /**
     * Returns the standard Ambiente de Expressao used when callers do not need custom policies.
     */
    public static ExpressionEnvironment standard() {
        return STANDARD;
    }

    /**
     * Starts a new builder initialized with the standard Ambiente de Expressao defaults.
     */
    public static Builder builder() {
        return new Builder();
    }

    public ZoneId zoneId() {
        return zoneId;
    }

    public MathContext mathContext() {
        return mathContext;
    }

    public MathContext transcendentalMathContext() {
        return transcendentalMathContext;
    }

    public int maxCurrentItemDepth() {
        return maxCurrentItemDepth;
    }

    public int maxMaterializedSize() {
        return maxMaterializedSize;
    }

    public int maxFactorialInput() {
        return maxFactorialInput;
    }

    public String conversionProfileIdentity() {
        return conversionProfileIdentity;
    }

    public String conversionProfileHash() {
        return conversionProfileHash;
    }

    public BoundaryCoercion boundaryCoercion() {
        return boundaryCoercion;
    }

    public String environmentId() {
        return environmentId;
    }

    public ExternalSymbolCatalog externalSymbols() {
        return externalSymbols;
    }

    public FunctionCatalog functions() {
        return functions;
    }

    public JavaTypeCatalog javaTypes() {
        return javaTypes;
    }

    public CollectionOperationCatalog collectionOperations() {
        return collectionOperations;
    }

    public OffsetDateTimeLiteralNormalization normalizeOffsetDateTimeLiteral(OffsetDateTime originalLiteral) {
        Objects.requireNonNull(originalLiteral, "originalLiteral");
        return OffsetDateTimeLiteralNormalization.of(originalLiteral, zoneId);
    }

    private static FunctionCatalog buildFunctions(Builder builder, JavaTypeCatalog javaTypes) {
        FunctionCatalog.Builder functionBuilder = FunctionCatalog.builder();
        List<ProviderConfigurationProblem> problems = new ArrayList<>();
        StandardBuiltInFunctions.registerAll(
                functionBuilder,
                builder.boundaryCoercion,
                builder.mathContext,
                builder.transcendentalMathContext);

        List<FunctionDeclaration> registrations = new ArrayList<>();
        for (FunctionDescriptor descriptor : builder.functions) {
            registrations.add(FunctionDeclaration.custom(descriptor));
        }
        for (ReflectedFunctionImporter.ImportPlan providerImport : builder.functionProviders) {
            ReflectedFunctionImporter.ImportResolution resolution =
                    ReflectedFunctionImporter.resolveForEnvironment(providerImport, javaTypes);
            for (FunctionDescriptor descriptor : resolution.descriptors()) {
                registrations.add(FunctionDeclaration.imported(descriptor));
            }
            for (IllegalArgumentException exception : resolution.failures()) {
                problems.add(new ProviderConfigurationProblem(exception.getMessage(), exception));
            }
        }
        registrations.sort(FunctionDeclaration.ORDER);
        for (FunctionDeclaration registration : registrations) {
            try {
                registration.register(functionBuilder);
            } catch (IllegalArgumentException exception) {
                problems.add(new ProviderConfigurationProblem(exception.getMessage(), exception));
            }
        }

        List<FunctionDeclaration> replacements = new ArrayList<>();
        for (FunctionDescriptor descriptor : builder.functionReplacements) {
            replacements.add(FunctionDeclaration.custom(descriptor));
        }
        for (ReflectedFunctionImporter.ImportPlan replacementImport : builder.functionReplacementProviders) {
            ReflectedFunctionImporter.ImportResolution resolution =
                    ReflectedFunctionImporter.resolveForEnvironment(replacementImport, javaTypes);
            for (IllegalArgumentException exception : resolution.failures()) {
                problems.add(new ProviderConfigurationProblem(exception.getMessage(), exception));
            }
            if (resolution.descriptors().size() == 1) {
                replacements.add(FunctionDeclaration.imported(resolution.descriptors().getFirst()));
            } else if (resolution.descriptors().size() > 1) {
                IllegalArgumentException exception = invalidReplacementCardinality(resolution.descriptors());
                problems.add(new ProviderConfigurationProblem(exception.getMessage(), exception));
            }
        }
        replacements.sort(FunctionDeclaration.ORDER);
        Set<FunctionSignature> replacedSignatures = new HashSet<>();
        for (FunctionDeclaration replacement : replacements) {
            if (!replacedSignatures.add(replacement.descriptor().signature())) {
                IllegalArgumentException exception = new IllegalArgumentException(
                        "more than one replacement declared for function signature: "
                                + replacement.descriptor().signature().canonical());
                problems.add(new ProviderConfigurationProblem(exception.getMessage(), exception));
                continue;
            }
            try {
                replacement.replace(functionBuilder);
            } catch (IllegalArgumentException exception) {
                problems.add(new ProviderConfigurationProblem(exception.getMessage(), exception));
            }
        }
        if (!problems.isEmpty()) {
            throw providerConfigurationException(problems);
        }
        FunctionCatalog catalog = functionBuilder.build();
        StandardBuiltInFunctions.validate(catalog);
        return catalog;
    }

    private static IllegalArgumentException invalidReplacementCardinality(List<FunctionDescriptor> descriptors) {
        List<FunctionSignature> signatures = descriptors.stream()
                .map(FunctionDescriptor::signature)
                .distinct()
                .sorted()
                .toList();
        if (signatures.size() == 1) {
            return new IllegalArgumentException("more than one imported method converges on replacement target: "
                    + signatures.getFirst().canonical());
        }
        return new IllegalArgumentException("function replacement plan must import exactly one method; imported signatures: "
                + signatures.stream().map(FunctionSignature::canonical).toList());
    }

    private static IllegalArgumentException providerConfigurationException(
            List<ProviderConfigurationProblem> problems) {
        problems.sort(Comparator.comparing(ProviderConfigurationProblem::message));
        IllegalArgumentException exception = new IllegalArgumentException(
                "invalid function provider configuration: " + problems.stream()
                        .map(ProviderConfigurationProblem::message)
                        .distinct()
                        .reduce((first, second) -> first + "; " + second)
                        .orElseThrow(),
                problems.getFirst().failure());
        for (int index = 1; index < problems.size(); index++) {
            exception.addSuppressed(problems.get(index).failure());
        }
        return exception;
    }

    private record ProviderConfigurationProblem(String message, IllegalArgumentException failure) {
    }

    private record FunctionDeclaration(FunctionDescriptor descriptor, boolean imported) {

        private static final Comparator<FunctionDeclaration> ORDER = Comparator
                .comparing((FunctionDeclaration declaration) -> declaration.descriptor().signature())
                .thenComparing(declaration -> declaration.descriptor().implementationMetadata().owner())
                .thenComparing(declaration -> declaration.descriptor().implementationMetadata().memberName())
                .thenComparing(declaration -> declaration.descriptor().implementationMetadata().methodType());

        private FunctionDeclaration {
            Objects.requireNonNull(descriptor, "descriptor");
        }

        private static FunctionDeclaration custom(FunctionDescriptor descriptor) {
            return new FunctionDeclaration(descriptor, false);
        }

        private static FunctionDeclaration imported(FunctionDescriptor descriptor) {
            return new FunctionDeclaration(descriptor, true);
        }

        private void register(FunctionCatalog.Builder builder) {
            if (imported) {
                builder.registerImported(descriptor);
            } else {
                builder.register(descriptor);
            }
        }

        private void replace(FunctionCatalog.Builder builder) {
            if (imported) {
                builder.replaceImported(descriptor);
            } else {
                builder.replace(descriptor);
            }
        }
    }

    /**
     * Mutable builder that produces immutable Ambiente de Expressao snapshots.
     */
    public static final class Builder {

        private ZoneId zoneId = DEFAULT_ZONE_ID;
        private MathContext mathContext = DEFAULT_MATH_CONTEXT;
        private MathContext transcendentalMathContext = DEFAULT_TRANSCENDENTAL_MATH_CONTEXT;
        private int maxCurrentItemDepth = DEFAULT_MAX_CURRENT_ITEM_DEPTH;
        private int maxMaterializedSize = DEFAULT_MAX_MATERIALIZED_SIZE;
        private int maxFactorialInput = DEFAULT_MAX_FACTORIAL_INPUT;
        private BoundaryCoercion boundaryCoercion = BoundaryCoercion.standard();
        private final ExternalSymbolCatalog.Builder externalSymbols = ExternalSymbolCatalog.builder();
        private final List<FunctionDescriptor> functions = new ArrayList<>();
        private final List<FunctionDescriptor> functionReplacements = new ArrayList<>();
        private final JavaTypeCatalog.Builder javaTypes = JavaTypeCatalog.builder();
        private final List<ReflectedFunctionImporter.ImportPlan> functionProviders = new ArrayList<>();
        private final List<ReflectedFunctionImporter.ImportPlan> functionReplacementProviders = new ArrayList<>();

        private Builder() {
        }

        public Builder zoneId(ZoneId zoneId) {
            this.zoneId = Objects.requireNonNull(zoneId, "zoneId");
            return this;
        }

        public Builder mathContext(MathContext mathContext) {
            this.mathContext = Objects.requireNonNull(mathContext, "mathContext");
            return this;
        }

        public Builder transcendentalMathContext(MathContext transcendentalMathContext) {
            this.transcendentalMathContext = Objects.requireNonNull(
                    transcendentalMathContext, "transcendentalMathContext");
            return this;
        }

        public Builder maxCurrentItemDepth(int maxCurrentItemDepth) {
            if (maxCurrentItemDepth < 0) {
                throw new IllegalArgumentException("maxCurrentItemDepth must not be negative");
            }
            this.maxCurrentItemDepth = maxCurrentItemDepth;
            return this;
        }

        public Builder maxMaterializedSize(int maxMaterializedSize) {
            if (maxMaterializedSize < 0) {
                throw new IllegalArgumentException("maxMaterializedSize must not be negative");
            }
            this.maxMaterializedSize = maxMaterializedSize;
            return this;
        }

        public Builder maxFactorialInput(int maxFactorialInput) {
            if (maxFactorialInput < 0) {
                throw new IllegalArgumentException("maxFactorialInput must not be negative");
            }
            this.maxFactorialInput = maxFactorialInput;
            return this;
        }

        public Builder boundaryCoercion(DataConversionService dataConversionService) {
            boundaryCoercion = BoundaryCoercion.of(dataConversionService);
            return this;
        }

        public Builder externalSymbol(String name, Object defaultValue, ExternalSymbolOverwritePolicy overwritePolicy) {
            externalSymbols.externalSymbol(name, defaultValue, overwritePolicy);
            return this;
        }

        public Builder externalSymbol(
                String name,
                ExpressionType type,
                Object defaultValue,
                ExternalSymbolOverwritePolicy overwritePolicy) {
            externalSymbols.externalSymbol(name, type, defaultValue, overwritePolicy);
            return this;
        }

        public Builder function(FunctionDescriptor descriptor) {
            functions.add(Objects.requireNonNull(descriptor, "descriptor"));
            return this;
        }

        public Builder replaceFunction(FunctionDescriptor descriptor) {
            functionReplacements.add(Objects.requireNonNull(descriptor, "descriptor"));
            return this;
        }

        public Builder functions(ReflectedFunctionImporter.ImportPlan importPlan) {
            functionProviders.add(Objects.requireNonNull(importPlan, "importPlan"));
            return this;
        }

        public Builder replaceFunctions(ReflectedFunctionImporter.ImportPlan importPlan) {
            functionReplacementProviders.add(Objects.requireNonNull(importPlan, "importPlan"));
            return this;
        }

        public Builder functionsFrom(Class<?> providerClass, FunctionPurity purity) {
            return functions(ReflectedFunctionImporter.importCanonicalAll(providerClass, purity));
        }

        public Builder functionsFrom(Object providerInstance, FunctionPurity purity) {
            return functions(ReflectedFunctionImporter.importCanonicalAll(providerInstance, purity));
        }

        public Builder functionsFrom(
                Class<?> exposureType,
                Object providerInstance,
                FunctionPurity purity) {
            return functions(ReflectedFunctionImporter.importCanonicalAll(exposureType, providerInstance, purity));
        }

        public Builder registerJavaType(Class<?> javaType) {
            javaTypes.registerJavaType(javaType);
            return this;
        }

        public Builder registerJavaTypeWithPublicMethods(Class<?> javaType) {
            javaTypes.registerJavaTypeWithPublicMethods(javaType);
            return this;
        }

        public Builder registerJavaTypeMethod(Class<?> javaType, String methodName, Class<?>... javaParameterTypes) {
            javaTypes.registerJavaTypeMethod(javaType, methodName, javaParameterTypes);
            return this;
        }

        public ExpressionEnvironment build() {
            return build(CollectionOperationCatalog.standard());
        }

        ExpressionEnvironment build(CollectionOperationCatalog collectionOperations) {
            return new ExpressionEnvironment(this, Objects.requireNonNull(collectionOperations, "collectionOperations"));
        }
    }
}
