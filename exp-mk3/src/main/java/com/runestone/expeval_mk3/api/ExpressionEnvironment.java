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
 * Immutable Ambiente de Expressao used as the public compilation configuration.
 */
public final class ExpressionEnvironment {

    public static final String STANDARD_CONVERSION_PROFILE_ID = "standard";

    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("UTC");
    private static final NumericMode DEFAULT_NUMERIC_MODE = NumericMode.DECIMAL;
    private static final MathContext DEFAULT_MATH_CONTEXT = MathContext.DECIMAL128;
    private static final MathContext DEFAULT_TRANSCENDENTAL_MATH_CONTEXT = MathContext.DECIMAL128;
    private static final boolean DEFAULT_STRICT_MODE = false;
    private static final int DEFAULT_MAX_CURRENT_ITEM_DEPTH = 64;
    private static final int DEFAULT_MATERIALIZATION_LIMIT = 10_000;
    private static final ExpressionEnvironment STANDARD = builder().build();

    private final ZoneId zoneId;
    private final NumericMode numericMode;
    private final MathContext mathContext;
    private final MathContext transcendentalMathContext;
    private final boolean strictMode;
    private final int maxCurrentItemDepth;
    private final int materializationLimit;
    private final String conversionProfileId;
    private final BoundaryCoercion boundaryCoercion;
    private final ExternalSymbolCatalog externalSymbols;
    private final ExpressionEnvironmentId environmentId;

    private ExpressionEnvironment(Builder builder) {
        zoneId = Objects.requireNonNull(builder.zoneId, "zoneId");
        numericMode = Objects.requireNonNull(builder.numericMode, "numericMode");
        mathContext = Objects.requireNonNull(builder.mathContext, "mathContext");
        transcendentalMathContext = Objects.requireNonNull(
                builder.transcendentalMathContext,
                "transcendentalMathContext");
        strictMode = builder.strictMode;
        maxCurrentItemDepth = validateMaxCurrentItemDepth(builder.maxCurrentItemDepth);
        materializationLimit = validateMaterializationLimit(builder.materializationLimit);
        boundaryCoercion = Objects.requireNonNull(builder.boundaryCoercion, "boundaryCoercion");
        conversionProfileId = boundaryCoercion.profileId();
        externalSymbols = builder.externalSymbols.build(boundaryCoercion);
        environmentId = createEnvironmentId();
    }

    public static ExpressionEnvironment standard() {
        return STANDARD;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public ZoneId zoneId() {
        return zoneId;
    }

    public NumericMode numericMode() {
        return numericMode;
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

    public String conversionProfileId() {
        return conversionProfileId;
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

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ExpressionEnvironment that)) {
            return false;
        }
        return strictMode == that.strictMode
                && maxCurrentItemDepth == that.maxCurrentItemDepth
                && materializationLimit == that.materializationLimit
                && zoneId.equals(that.zoneId)
                && numericMode == that.numericMode
                && mathContext.equals(that.mathContext)
                && transcendentalMathContext.equals(that.transcendentalMathContext)
                && conversionProfileId.equals(that.conversionProfileId)
                && externalSymbols.equals(that.externalSymbols);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                zoneId,
                numericMode,
                mathContext,
                transcendentalMathContext,
                strictMode,
                maxCurrentItemDepth,
                materializationLimit,
                conversionProfileId,
                externalSymbols);
    }

    @Override
    public String toString() {
        return "ExpressionEnvironment[environmentId=" + environmentId + ']';
    }

    private ExpressionEnvironmentId createEnvironmentId() {
        StringBuilder canonical = new StringBuilder(256);
        canonical.append("ExpressionEnvironment:v2\n");
        appendCanonicalField(canonical, "zoneId", zoneId.getId());
        appendCanonicalField(canonical, "numericMode", numericMode.name());
        appendCanonicalField(canonical, "mathContext", canonicalMathContext(mathContext));
        appendCanonicalField(
                canonical,
                "transcendentalMathContext",
                canonicalMathContext(transcendentalMathContext));
        appendCanonicalField(canonical, "strictMode", Boolean.toString(strictMode));
        appendCanonicalField(canonical, "maxCurrentItemDepth", Integer.toString(maxCurrentItemDepth));
        appendCanonicalField(canonical, "materializationLimit", Integer.toString(materializationLimit));
        appendCanonicalField(canonical, "conversionProfileId", conversionProfileId);
        appendCanonicalField(canonical, "externalSymbols.count", Integer.toString(externalSymbols.size()));
        for (ExternalSymbol externalSymbol : externalSymbols.values()) {
            appendCanonicalField(canonical, "externalSymbol.name", externalSymbol.name());
            appendCanonicalField(canonical, "externalSymbol.type", ExpressionTypes.canonical(externalSymbol.type()));
            appendCanonicalField(
                    canonical,
                    "externalSymbol.hasDefaultValue",
                    Boolean.toString(externalSymbol.hasDefaultValue()));
            externalSymbol.defaultValue().ifPresent(defaultValue -> appendCanonicalField(
                    canonical,
                    "externalSymbol.defaultValue",
                    ExternalSymbolDefaults.canonicalValue(defaultValue.value())));
        }

        return new ExpressionEnvironmentId(sha256Hex(canonical.toString()));
    }

    private static void appendCanonicalField(StringBuilder target, String name, String value) {
        byte[] valueBytes = value.getBytes(StandardCharsets.UTF_8);
        target.append(name)
                .append(':')
                .append(valueBytes.length)
                .append(':')
                .append(value)
                .append('\n');
    }

    private static String canonicalMathContext(MathContext value) {
        return value.getPrecision() + ":" + value.getRoundingMode().name();
    }

    private static String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 digest is unavailable", exception);
        }
    }

    private static int validateMaxCurrentItemDepth(int value) {
        if (value < 0) {
            throw new IllegalArgumentException("maxCurrentItemDepth must not be negative");
        }
        return value;
    }

    private static int validateMaterializationLimit(int value) {
        if (value < 1) {
            throw new IllegalArgumentException("materializationLimit must be positive");
        }
        return value;
    }

    public static final class Builder {

        private ZoneId zoneId = DEFAULT_ZONE_ID;
        private NumericMode numericMode = DEFAULT_NUMERIC_MODE;
        private MathContext mathContext = DEFAULT_MATH_CONTEXT;
        private MathContext transcendentalMathContext = DEFAULT_TRANSCENDENTAL_MATH_CONTEXT;
        private boolean strictMode = DEFAULT_STRICT_MODE;
        private int maxCurrentItemDepth = DEFAULT_MAX_CURRENT_ITEM_DEPTH;
        private int materializationLimit = DEFAULT_MATERIALIZATION_LIMIT;
        private BoundaryCoercion boundaryCoercion = BoundaryCoercion.standard();
        private ExternalSymbolCatalog.Builder externalSymbols = ExternalSymbolCatalog.builder();

        private Builder() {
        }

        private Builder(ExpressionEnvironment environment) {
            zoneId = environment.zoneId;
            numericMode = environment.numericMode;
            mathContext = environment.mathContext;
            transcendentalMathContext = environment.transcendentalMathContext;
            strictMode = environment.strictMode;
            maxCurrentItemDepth = environment.maxCurrentItemDepth;
            materializationLimit = environment.materializationLimit;
            boundaryCoercion = environment.boundaryCoercion;
            externalSymbols = environment.externalSymbols.toBuilder();
        }

        public Builder zoneId(ZoneId zoneId) {
            this.zoneId = Objects.requireNonNull(zoneId, "zoneId");
            return this;
        }

        public Builder numericMode(NumericMode numericMode) {
            this.numericMode = Objects.requireNonNull(numericMode, "numericMode");
            return this;
        }

        public Builder mathContext(MathContext mathContext) {
            this.mathContext = Objects.requireNonNull(mathContext, "mathContext");
            return this;
        }

        public Builder transcendentalMathContext(MathContext transcendentalMathContext) {
            this.transcendentalMathContext = Objects.requireNonNull(
                    transcendentalMathContext,
                    "transcendentalMathContext");
            return this;
        }

        public Builder strictMode(boolean strictMode) {
            this.strictMode = strictMode;
            return this;
        }

        public Builder maxCurrentItemDepth(int maxCurrentItemDepth) {
            this.maxCurrentItemDepth = validateMaxCurrentItemDepth(maxCurrentItemDepth);
            return this;
        }

        public Builder materializationLimit(int materializationLimit) {
            this.materializationLimit = validateMaterializationLimit(materializationLimit);
            return this;
        }

        public Builder conversionProfileId(String conversionProfileId) {
            boundaryCoercion = boundaryCoercion.withProfileId(conversionProfileId);
            return this;
        }

        public Builder boundaryCoercion(String conversionProfileId, DataConversionService dataConversionService) {
            boundaryCoercion = BoundaryCoercion.of(conversionProfileId, dataConversionService);
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

        public ExpressionEnvironment build() {
            return new ExpressionEnvironment(this);
        }
    }
}
