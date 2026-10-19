package com.runestone.dynafilter.core.exceptions;

import java.util.List;

/** A requested sort contains names or expressions outside its public contract. */
public final class InvalidSortException extends RuntimeException {
    private final List<String> rejectedNames;

    public InvalidSortException(List<String> rejectedNames) {
        super("Invalid sort names: " + String.join(", ", rejectedNames));
        this.rejectedNames = List.copyOf(rejectedNames);
    }

    public List<String> rejectedNames() {
        return rejectedNames;
    }
}
