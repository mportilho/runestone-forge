package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.internal.source.SourceSpan;

import java.util.Objects;

public record RegexPatternSource(NodeId nodeId, String pattern, SourceSpan sourceSpan) {

    public RegexPatternSource {
        Objects.requireNonNull(nodeId, "nodeId");
        Objects.requireNonNull(pattern, "pattern");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
    }
}
