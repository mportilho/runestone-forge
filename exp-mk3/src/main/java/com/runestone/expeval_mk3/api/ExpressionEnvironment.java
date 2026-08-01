package com.runestone.expeval_mk3.api;

import com.runestone.converters.DataConversionService;

import java.math.MathContext;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
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
    private static final int DEFAULT_MAX_MATERIALIZED_SIZE = BoundaryCoercion.DEFAULT_MAX_MATERIALIZED_SIZE;
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
        externalSymbols = builder.externalSymbols.build(boundaryCoercion, maxMaterializedSize);
        javaTypes = builder.javaTypes.build();
        functions = FunctionCatalogAssembly.assemble(
                boundaryCoercion,
                builder.mathContext,
                builder.transcendentalMathContext,
                maxMaterializedSize,
                javaTypes,
                builder.functions,
                builder.functionProviders,
                builder.functionReplacements,
                builder.functionReplacementProviders);
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
            this.mathContext = requireUsableMathContext(mathContext, "mathContext");
            return this;
        }

        public Builder transcendentalMathContext(MathContext transcendentalMathContext) {
            this.transcendentalMathContext = requireUsableMathContext(
                    transcendentalMathContext, "transcendentalMathContext");
            return this;
        }

        private static MathContext requireUsableMathContext(MathContext mathContext, String parameterName) {
            Objects.requireNonNull(mathContext, parameterName);
            if (mathContext.getPrecision() <= 0) {
                throw new IllegalArgumentException(parameterName + " must have positive precision");
            }
            if (mathContext.getRoundingMode() == RoundingMode.UNNECESSARY) {
                throw new IllegalArgumentException(parameterName + " must not use RoundingMode.UNNECESSARY");
            }
            return mathContext;
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
            return functions(ReflectedFunctionImporter.importAll(providerClass, purity));
        }

        public Builder functionsFrom(Object providerInstance, FunctionPurity purity) {
            return functions(ReflectedFunctionImporter.importAll(providerInstance, purity));
        }

        public Builder functionsFrom(
                Class<?> exposureType,
                Object providerInstance,
                FunctionPurity purity) {
            return functions(ReflectedFunctionImporter.importAll(exposureType, providerInstance, purity));
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

        public Builder registerJavaTypeWildcardChildren(Class<?> javaType, String... memberNames) {
            javaTypes.registerJavaTypeWildcardChildren(javaType, memberNames);
            return this;
        }

        public Builder registerJavaTypeWildcardChildren(Class<?> javaType, Set<String> memberNames) {
            javaTypes.registerJavaTypeWildcardChildren(javaType, memberNames);
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
