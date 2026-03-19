package com.runestone.expeval2.internal.runtime;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class VectorMaterializationBenchmarkSupport {

    private final List<RuntimeValue> runtimeValues;
    private final List<ExecutableNode> executableNodes;

    public VectorMaterializationBenchmarkSupport(int vectorSize) {
        this.runtimeValues = new ArrayList<>(vectorSize);
        this.executableNodes = new ArrayList<>(vectorSize);
        for (int i = 0; i < vectorSize; i++) {
            RuntimeValue value = new RuntimeValue.NumberValue(BigDecimal.valueOf(i + 1L));
            runtimeValues.add(value);
            executableNodes.add(new ExecutableLiteral(value));
        }
    }

    public Object baselineWrapMutableList() {
        return new BaselineVectorValue(new ArrayList<>(runtimeValues));
    }

    public Object optimizedWrapMutableList() {
        return OptimizedVectorValue.fromTrustedElements(new ArrayList<>(runtimeValues));
    }

    public Object baselineEvaluateVectorLiteral() {
        List<RuntimeValue> elements = executableNodes.stream()
                .map(this::evaluateLiteral)
                .toList();
        return new BaselineVectorValue(elements);
    }

    public Object optimizedEvaluateVectorLiteral() {
        List<RuntimeValue> elements = new ArrayList<>(executableNodes.size());
        for (ExecutableNode node : executableNodes) {
            elements.add(evaluateLiteral(node));
        }
        return OptimizedVectorValue.fromTrustedElements(elements);
    }

    private RuntimeValue evaluateLiteral(ExecutableNode node) {
        if (node instanceof ExecutableLiteral literal) {
            return literal.precomputed();
        }
        throw new IllegalStateException("unsupported executable node for benchmark");
    }

    private record BaselineVectorValue(List<RuntimeValue> elements) {
        private BaselineVectorValue {
            elements = List.copyOf(Objects.requireNonNull(elements, "elements must not be null"));
        }
    }

    private static final class OptimizedVectorValue {
        private final List<RuntimeValue> elements;

        private OptimizedVectorValue(List<RuntimeValue> elements) {
            this.elements = Objects.requireNonNull(elements, "elements must not be null");
        }

        private static OptimizedVectorValue fromTrustedElements(List<RuntimeValue> elements) {
            return new OptimizedVectorValue(elements);
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof OptimizedVectorValue that && elements.equals(that.elements);
        }

        @Override
        public int hashCode() {
            return elements.hashCode();
        }

        @Override
        public String toString() {
            return "OptimizedVectorValue[elements=" + elements + "]";
        }
    }
}
