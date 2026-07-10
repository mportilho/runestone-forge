package com.runestone.expeval_mk3.api;

import com.runestone.converters.DataConversionService;

import java.math.MathContext;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.ZoneId;
import java.util.HexFormat;
import java.util.Objects;

/**
 * Immutable compilation configuration for expressions.
 */
public final class ExpressionEnvironment {

    static final String STANDARD_CONVERSION_PROFILE_IDENTITY = "standard";

    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.systemDefault();
    private static final NumericMode DEFAULT_NUMERIC_MODE = NumericMode.DECIMAL;
    private static final MathContext DEFAULT_MATH_CONTEXT = MathContext.DECIMAL128;
    private static final MathContext DEFAULT_TRANSCENDENTAL_MATH_CONTEXT = MathContext.DECIMAL128;
    private static final boolean DEFAULT_STRICT_MODE = false;
    private static final int DEFAULT_MAX_CURRENT_ITEM_DEPTH = 32;
    private static final int DEFAULT_MATERIALIZATION_LIMIT = 10_000;
    private static final ExpressionEnvironment STANDARD = builder().build();

    private final NumericMode numericMode;
    private final ZoneId zoneId;
    private final MathContext mathContext;
    private final MathContext transcendentalMathContext;
    private final boolean strictMode;
    private final int maxCurrentItemDepth;
    private final int materializationLimit;
    private final String conversionProfileIdentity;
    private final BoundaryCoercion boundaryCoercion;
    private final ExternalSymbolCatalog externalSymbols;
    private final FunctionCatalog functions;
    private final JavaTypeCatalog javaTypes;
    private final ExpressionEnvironmentId environmentId;

    private ExpressionEnvironment(Builder builder) {
        numericMode = builder.numericMode;
        zoneId = builder.zoneId;
        mathContext = builder.mathContext;
        transcendentalMathContext = builder.transcendentalMathContext;
        strictMode = builder.strictMode;
        maxCurrentItemDepth = builder.maxCurrentItemDepth;
        materializationLimit = builder.materializationLimit;
        boundaryCoercion = builder.boundaryCoercion;
        conversionProfileIdentity = boundaryCoercion.profileIdentity();
        externalSymbols = builder.externalSymbols.build(boundaryCoercion);
        functions = buildFunctions(builder);
        javaTypes = builder.javaTypes.build();
        environmentId = calculateEnvironmentId(this);
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

    public NumericMode numericMode() {
        return numericMode;
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

    public boolean strictMode() {
        return strictMode;
    }

    public int maxCurrentItemDepth() {
        return maxCurrentItemDepth;
    }

    public int materializationLimit() {
        return materializationLimit;
    }

    public String conversionProfileIdentity() {
        return conversionProfileIdentity;
    }

    public BoundaryCoercion boundaryCoercion() {
        return boundaryCoercion;
    }

    public ExpressionEnvironmentId environmentId() {
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

    private static FunctionCatalog buildFunctions(Builder builder) {
        FunctionCatalog.Builder functionBuilder = FunctionCatalog.builder();
        StandardBuiltInFunctions.registerAll(
                functionBuilder,
                builder.boundaryCoercion,
                builder.mathContext,
                builder.transcendentalMathContext,
                builder.materializationLimit);
        for (FunctionDescriptor descriptor : builder.functions.build().values()) {
            functionBuilder.register(descriptor);
        }
        FunctionCatalog catalog = functionBuilder.build();
        StandardBuiltInFunctions.validate(catalog);
        return catalog;
    }

    private static ExpressionEnvironmentId calculateEnvironmentId(ExpressionEnvironment environment) {
        String canonical = canonicalRepresentation(environment);
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(canonical.getBytes(StandardCharsets.UTF_8));
            return new ExpressionEnvironmentId("sha256:" + HexFormat.of().formatHex(hash));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    private static String canonicalRepresentation(ExpressionEnvironment environment) {
        StringBuilder builder = new StringBuilder(256);
        appendCanonicalField(builder, "schema", "4");
        appendCanonicalField(builder, "numericMode", environment.numericMode.name());
        appendCanonicalField(builder, "zoneId", environment.zoneId.getId());
        appendCanonicalField(builder, "mathContext", canonicalMathContext(environment.mathContext));
        appendCanonicalField(builder, "transcendentalMathContext",
                canonicalMathContext(environment.transcendentalMathContext));
        appendCanonicalField(builder, "strictMode", Boolean.toString(environment.strictMode));
        appendCanonicalField(builder, "maxCurrentItemDepth", Integer.toString(environment.maxCurrentItemDepth));
        appendCanonicalField(builder, "materializationLimit", Integer.toString(environment.materializationLimit));
        appendCanonicalField(builder, "conversionProfileIdentity", environment.conversionProfileIdentity);
        appendCanonicalField(builder, "externalSymbols.count", Integer.toString(environment.externalSymbols.size()));
        for (ExternalSymbol externalSymbol : environment.externalSymbols.values()) {
            appendCanonicalField(builder, "externalSymbol.name", externalSymbol.name());
            appendCanonicalField(builder, "externalSymbol.type", ExpressionTypes.canonical(externalSymbol.type()));
            appendCanonicalField(builder, "externalSymbol.hasDefaultValue",
                    Boolean.toString(externalSymbol.hasDefaultValue()));
            externalSymbol.defaultValue().ifPresent(defaultValue -> appendCanonicalField(
                    builder,
                    "externalSymbol.defaultValue",
                    ExternalSymbolDefaults.canonicalValue(defaultValue.value())));
        }
        appendCanonicalField(builder, "functions.count", Integer.toString(environment.functions.size()));
        for (FunctionDescriptor function : environment.functions.values()) {
            appendCanonicalField(builder, "function.languageName", function.languageName());
            appendCanonicalField(builder, "function.arity", Integer.toString(function.arity()));
            appendCanonicalField(builder, "function.parameterTypes.count",
                    Integer.toString(function.parameterTypes().size()));
            for (ExpressionType parameterType : function.parameterTypes()) {
                appendCanonicalField(builder, "function.parameterType", ExpressionTypes.canonical(parameterType));
            }
            appendCanonicalField(builder, "function.returnType", ExpressionTypes.canonical(function.returnType()));
            appendCanonicalField(builder, "function.pure", Boolean.toString(function.pure()));
            appendCanonicalField(builder, "function.foldable", Boolean.toString(function.foldable()));
            FunctionImplementationMetadata implementationMetadata = function.implementationMetadata();
            appendCanonicalField(builder, "function.implementation.kind", implementationMetadata.kind());
            appendCanonicalField(builder, "function.implementation.owner", implementationMetadata.owner());
            appendCanonicalField(builder, "function.implementation.memberName", implementationMetadata.memberName());
            appendCanonicalField(builder, "function.implementation.methodType", implementationMetadata.methodType());
        }
        appendCanonicalField(builder, "javaTypes.count", Integer.toString(environment.javaTypes.size()));
        for (JavaTypeDescriptor javaType : environment.javaTypes.values()) {
            appendCanonicalField(builder, "javaType.class", javaType.javaType().getName());
            appendCanonicalField(builder, "javaType.objectType", ExpressionTypes.canonical(javaType.objectType()));
            appendCanonicalField(builder, "javaType.properties.count", Integer.toString(javaType.propertyCount()));
            for (JavaPropertyDescriptor property : javaType.properties().values()) {
                appendCanonicalField(builder, "javaType.property.name", property.name());
                appendCanonicalField(builder, "javaType.property.type", ExpressionTypes.canonical(property.type()));
                appendJavaMemberMetadata(builder, "javaType.property", property.implementationMetadata());
            }
            appendCanonicalField(builder, "javaType.methods.count", Integer.toString(javaType.methodCount()));
            for (JavaMethodDescriptor method : javaType.methods()) {
                appendCanonicalField(builder, "javaType.method.languageName", method.languageName());
                appendCanonicalField(builder, "javaType.method.arity", Integer.toString(method.arity()));
                appendCanonicalField(builder, "javaType.method.parameterTypes.count",
                        Integer.toString(method.parameterTypes().size()));
                for (ExpressionType parameterType : method.parameterTypes()) {
                    appendCanonicalField(builder, "javaType.method.parameterType", ExpressionTypes.canonical(parameterType));
                }
                appendCanonicalField(builder, "javaType.method.returnType", ExpressionTypes.canonical(method.returnType()));
                appendJavaMemberMetadata(builder, "javaType.method", method.implementationMetadata());
            }
        }
        return builder.toString();
    }

    private static void appendJavaMemberMetadata(
            StringBuilder builder,
            String prefix,
            JavaMemberImplementationMetadata implementationMetadata) {
        appendCanonicalField(builder, prefix + ".implementation.kind", implementationMetadata.kind());
        appendCanonicalField(builder, prefix + ".implementation.owner", implementationMetadata.owner());
        appendCanonicalField(builder, prefix + ".implementation.memberName", implementationMetadata.memberName());
        appendCanonicalField(builder, prefix + ".implementation.methodType", implementationMetadata.methodType());
    }

    private static String canonicalMathContext(MathContext mathContext) {
        return mathContext.getPrecision() + ":" + mathContext.getRoundingMode().name();
    }

    private static void appendCanonicalField(StringBuilder builder, String name, String value) {
        builder.append(name)
                .append('=')
                .append(value.length())
                .append(':')
                .append(value)
                .append('\n');
    }

    /**
     * Mutable builder that produces immutable Ambiente de Expressao snapshots.
     */
    public static final class Builder {

        private NumericMode numericMode = DEFAULT_NUMERIC_MODE;
        private ZoneId zoneId = DEFAULT_ZONE_ID;
        private MathContext mathContext = DEFAULT_MATH_CONTEXT;
        private MathContext transcendentalMathContext = DEFAULT_TRANSCENDENTAL_MATH_CONTEXT;
        private boolean strictMode = DEFAULT_STRICT_MODE;
        private int maxCurrentItemDepth = DEFAULT_MAX_CURRENT_ITEM_DEPTH;
        private int materializationLimit = DEFAULT_MATERIALIZATION_LIMIT;
        private BoundaryCoercion boundaryCoercion = BoundaryCoercion.standard();
        private final ExternalSymbolCatalog.Builder externalSymbols = ExternalSymbolCatalog.builder();
        private final FunctionCatalog.Builder functions = FunctionCatalog.builder();
        private final JavaTypeCatalog.Builder javaTypes = JavaTypeCatalog.builder();

        private Builder() {
        }

        public Builder numericMode(NumericMode numericMode) {
            this.numericMode = Objects.requireNonNull(numericMode, "numericMode");
            return this;
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

        public Builder strictMode(boolean strictMode) {
            this.strictMode = strictMode;
            return this;
        }

        public Builder maxCurrentItemDepth(int maxCurrentItemDepth) {
            if (maxCurrentItemDepth < 0) {
                throw new IllegalArgumentException("maxCurrentItemDepth must not be negative");
            }
            this.maxCurrentItemDepth = maxCurrentItemDepth;
            return this;
        }

        public Builder materializationLimit(int materializationLimit) {
            if (materializationLimit < 0) {
                throw new IllegalArgumentException("materializationLimit must not be negative");
            }
            this.materializationLimit = materializationLimit;
            return this;
        }

        public Builder conversionProfileIdentity(String conversionProfileIdentity) {
            Objects.requireNonNull(conversionProfileIdentity, "conversionProfileIdentity");
            if (conversionProfileIdentity.isBlank()) {
                throw new IllegalArgumentException("conversionProfileIdentity must not be blank");
            }
            boundaryCoercion = boundaryCoercion.withProfileIdentity(conversionProfileIdentity);
            return this;
        }

        public Builder boundaryCoercion(String conversionProfileIdentity, DataConversionService dataConversionService) {
            boundaryCoercion = BoundaryCoercion.of(conversionProfileIdentity, dataConversionService);
            return this;
        }

        public Builder deterministicBoundaryCoercion(
                String conversionProfileIdentity,
                DataConversionService dataConversionService) {
            boundaryCoercion = BoundaryCoercion.of(
                    conversionProfileIdentity,
                    dataConversionService,
                    true);
            return this;
        }

        public Builder externalSymbol(String name) {
            externalSymbols.externalSymbol(name);
            return this;
        }

        public Builder externalSymbol(String name, ExpressionType type) {
            externalSymbols.externalSymbol(name, type);
            return this;
        }

        public Builder externalSymbolWithDefault(String name, Object defaultValue) {
            externalSymbols.externalSymbolWithDefault(name, defaultValue);
            return this;
        }

        public Builder externalSymbolWithDefault(String name, ExpressionType type, Object defaultValue) {
            externalSymbols.externalSymbolWithDefault(name, type, defaultValue);
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
            return new ExpressionEnvironment(this);
        }
    }
}
