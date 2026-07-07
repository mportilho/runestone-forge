package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.api.ExpressionType;
import com.runestone.expeval_mk3.internal.source.SourceSpan;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class AssignmentSymbolFlow {

    private final List<SymbolReadSource> expressionReads;
    private final SymbolReadSource expressionRootRead;
    private final NodeId expressionNodeId;
    private final List<AssignedSymbolSource> targetSymbols;
    private final ExpressionType expressionType;
    private final Metadata metadata;

    AssignmentSymbolFlow(
            List<SymbolReadSource> expressionReads,
            SymbolReadSource expressionRootRead,
            NodeId expressionNodeId,
            List<AssignedSymbolSource> targetSymbols,
            ExpressionType expressionType,
            Metadata metadata) {
        Objects.requireNonNull(expressionReads, "expressionReads");
        this.expressionReads = List.copyOf(expressionReads);
        this.expressionRootRead = expressionRootRead;
        this.expressionNodeId = Objects.requireNonNull(expressionNodeId, "expressionNodeId");
        Objects.requireNonNull(targetSymbols, "targetSymbols");
        this.targetSymbols = List.copyOf(targetSymbols);
        this.expressionType = expressionType;
        this.metadata = Objects.requireNonNull(metadata, "metadata");
    }

    public List<SymbolReadSource> expressionReads() {
        return expressionReads;
    }

    public Optional<SymbolReadSource> expressionRootRead() {
        return Optional.ofNullable(expressionRootRead);
    }

    public NodeId expressionNodeId() {
        return expressionNodeId;
    }

    public List<AssignedSymbolSource> targetSymbols() {
        return targetSymbols;
    }

    public Optional<ExpressionType> expressionType() {
        return Optional.ofNullable(expressionType);
    }

    static Metadata metadata(
            SourceSpan sourceSpan,
            SourceSpan destructuringTargetSpan,
            AssignmentSourceShape knownSourceShape) {
        return new Metadata(sourceSpan, destructuringTargetSpan, knownSourceShape);
    }

    public Optional<SourceSpan> destructuringTargetSpan() {
        return Optional.ofNullable(metadata.destructuringTargetSpan());
    }

    public Optional<AssignmentSourceShape> knownSourceShape() {
        return Optional.ofNullable(metadata.knownSourceShape());
    }

    public SourceSpan sourceSpan() {
        return metadata.sourceSpan();
    }

    record Metadata(
            SourceSpan sourceSpan,
            SourceSpan destructuringTargetSpan,
            AssignmentSourceShape knownSourceShape) {

        Metadata {
            Objects.requireNonNull(sourceSpan, "sourceSpan");
        }
    }
}
