package com.runestone.expeval_mk3.api;

import java.util.Objects;

final class StandardComparableFunctions {

    private StandardComparableFunctions() {
    }

    public static <T extends Comparable<? super T>> T max(T[] values) {
        return extreme(values, true);
    }

    public static <T extends Comparable<? super T>> T min(T[] values) {
        return extreme(values, false);
    }

    private static <T extends Comparable<? super T>> T extreme(T[] values, boolean maximum) {
        Objects.requireNonNull(values, "values");
        if (values.length == 0) {
            throw new IllegalArgumentException((maximum ? "max" : "min") + " requires at least one value");
        }
        T result = values[0];
        for (int index = 1; index < values.length; index++) {
            int comparison = result.compareTo(values[index]);
            if (maximum && comparison < 0 || !maximum && comparison > 0) {
                result = values[index];
            }
        }
        return result;
    }
}
