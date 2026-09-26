package com.runestone.expeval_mk3.api;

/**
 * Immutable expression resource configuration. Zero disables the corresponding capacity;
 * there is no unlimited configuration sentinel. Enforcement follows {@link ExpressionTrustMode}.
 */
public final class ExpressionResourceLimits {
    private static final ExpressionResourceLimits DEFAULTS = builder().build();

    private final int maxSourceLength;
    private final int maxTokenCount;
    private final int maxSyntaxDepth;
    private final int maxAstNodeCount;
    private final int maxCurrentItemDepth;
    private final int maxMaterializedSize;
    private final int maxFactorialInput;
    private final int maxTextLength;
    private final int maxValueDepth;
    private final int maxNumericPrecision;
    private final int maxNumericScaleMagnitude;
    private final int maxRegexPatternLength;
    private final int maxEvaluationWork;

    private ExpressionResourceLimits(Builder builder) {
        maxSourceLength = builder.maxSourceLength;
        maxTokenCount = builder.maxTokenCount;
        maxSyntaxDepth = builder.maxSyntaxDepth;
        maxAstNodeCount = builder.maxAstNodeCount;
        maxCurrentItemDepth = builder.maxCurrentItemDepth;
        maxMaterializedSize = builder.maxMaterializedSize;
        maxFactorialInput = builder.maxFactorialInput;
        maxTextLength = builder.maxTextLength;
        maxValueDepth = builder.maxValueDepth;
        maxNumericPrecision = builder.maxNumericPrecision;
        maxNumericScaleMagnitude = builder.maxNumericScaleMagnitude;
        maxRegexPatternLength = builder.maxRegexPatternLength;
        maxEvaluationWork = builder.maxEvaluationWork;
    }

    public static ExpressionResourceLimits defaults() {
        return DEFAULTS;
    }

    public static Builder builder() {
        return new Builder();
    }

    public int maxSourceLength() {
        return maxSourceLength;
    }

    public int maxTokenCount() {
        return maxTokenCount;
    }

    public int maxSyntaxDepth() {
        return maxSyntaxDepth;
    }

    public int maxAstNodeCount() {
        return maxAstNodeCount;
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

    public int maxTextLength() {
        return maxTextLength;
    }

    public int maxValueDepth() {
        return maxValueDepth;
    }

    public int maxNumericPrecision() {
        return maxNumericPrecision;
    }

    public int maxNumericScaleMagnitude() {
        return maxNumericScaleMagnitude;
    }

    public int maxRegexPatternLength() {
        return maxRegexPatternLength;
    }

    public int maxEvaluationWork() {
        return maxEvaluationWork;
    }

    /** Mutable configuration builder; each build produces an independent immutable snapshot. */
    public static final class Builder {
        private int maxSourceLength = 16_384;
        private int maxTokenCount = 4_096;
        private int maxSyntaxDepth = 64;
        private int maxAstNodeCount = 4_096;
        private int maxCurrentItemDepth = 32;
        private int maxMaterializedSize = 10_000;
        private int maxFactorialInput = 1_000;
        private int maxTextLength = 1_048_576;
        private int maxValueDepth = 64;
        private int maxNumericPrecision = 10_000;
        private int maxNumericScaleMagnitude = 10_000;
        private int maxRegexPatternLength = 1_024;
        private int maxEvaluationWork = 1_000_000;

        private Builder() {
        }

        public Builder maxSourceLength(int value) {
            maxSourceLength = requireRange("maxSourceLength", value, 262_144);
            return this;
        }
        public Builder maxTokenCount(int value) {
            maxTokenCount = requireRange("maxTokenCount", value, 65_536);
            return this;
        }
        public Builder maxSyntaxDepth(int value) {
            maxSyntaxDepth = requireRange("maxSyntaxDepth", value, 256);
            return this;
        }
        public Builder maxAstNodeCount(int value) {
            maxAstNodeCount = requireRange("maxAstNodeCount", value, 65_536);
            return this;
        }
        public Builder maxCurrentItemDepth(int value) {
            maxCurrentItemDepth = requireRange("maxCurrentItemDepth", value, 64);
            return this;
        }
        public Builder maxMaterializedSize(int value) {
            maxMaterializedSize = requireRange("maxMaterializedSize", value, 100_000);
            return this;
        }
        public Builder maxFactorialInput(int value) {
            maxFactorialInput = requireRange("maxFactorialInput", value, 10_000);
            return this;
        }
        public Builder maxTextLength(int value) {
            maxTextLength = requireRange("maxTextLength", value, 8_388_608);
            return this;
        }
        public Builder maxValueDepth(int value) {
            maxValueDepth = requireRange("maxValueDepth", value, 256);
            return this;
        }
        public Builder maxNumericPrecision(int value) {
            maxNumericPrecision = requireRange("maxNumericPrecision", value, 100_000);
            return this;
        }
        public Builder maxNumericScaleMagnitude(int value) {
            maxNumericScaleMagnitude = requireRange("maxNumericScaleMagnitude", value, 100_000);
            return this;
        }
        public Builder maxRegexPatternLength(int value) {
            maxRegexPatternLength = requireRange("maxRegexPatternLength", value, 8_192);
            return this;
        }
        public Builder maxEvaluationWork(int value) {
            maxEvaluationWork = requireRange("maxEvaluationWork", value, 100_000_000);
            return this;
        }
        public ExpressionResourceLimits build() {
            return new ExpressionResourceLimits(this);
        }
        private static int requireRange(String property, int value, int maximum) {
            if (value < 0 || value > maximum) {
                throw new IllegalArgumentException(property + " must be in [0, " + maximum + "]: " + value);
            }
            return value;
        }
    }
}
