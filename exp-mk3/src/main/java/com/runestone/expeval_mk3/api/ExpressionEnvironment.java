package com.runestone.expeval_mk3.api;

import com.runestone.converters.DataConversionService;

import java.math.MathContext;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Objects;
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
        functions = buildFunctions(builder);
        javaTypes = builder.javaTypes.build();
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

    private static FunctionCatalog buildFunctions(Builder builder) {
        FunctionCatalog.Builder functionBuilder = FunctionCatalog.builder();
        StandardBuiltInFunctions.registerAll(
                functionBuilder,
                builder.boundaryCoercion,
                builder.mathContext,
                builder.transcendentalMathContext);
        for (FunctionDescriptor descriptor : builder.functions.build().values()) {
            functionBuilder.register(descriptor);
        }
        FunctionCatalog catalog = functionBuilder.build();
        StandardBuiltInFunctions.validate(catalog);
        return catalog;
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
        private final FunctionCatalog.Builder functions = FunctionCatalog.builder();
        private final JavaTypeCatalog.Builder javaTypes = JavaTypeCatalog.builder();

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
            functions.register(descriptor);
            return this;
        }

        public Builder replaceFunction(FunctionDescriptor descriptor) {
            functions.replace(descriptor);
            return this;
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
