package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.internal.source.SourceSpan;

import java.util.Objects;

public record MethodNavigationSource(
        NodeId nodeId,
        SourceSpan sourceSpan,
        MethodNavigationSignature signature,
        NavigationSafety safety) implements NavigationLinkSource {

    public MethodNavigationSource {
        Objects.requireNonNull(nodeId, "nodeId");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(signature, "signature");
        Objects.requireNonNull(safety, "safety");
    }
}
