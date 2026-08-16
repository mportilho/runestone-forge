package com.runestone.expeval_mk3.internal.cache;

import java.util.Objects;

/**
 * Combines the exact source text, compared by {@link String#equals}, and the Environment Instance
 * Identifier. Neither the source nor the identifier is normalized, copied, interned, or replaced by a
 * hash; the key retains the exact reference it was built from for as long as the entry stays resident.
 */
record CompilationCacheKey(String source, String environmentId) {

    CompilationCacheKey {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(environmentId, "environmentId");
    }
}
