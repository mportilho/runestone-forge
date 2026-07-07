package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.internal.source.SourceSpan;

import java.util.List;

public interface ExpressionSemanticTree {

    SourceSpan sourceSpan();

    boolean isEmptyFile();

    List<AssignedSymbolSource> assignedSymbols();

    SemanticSymbolFlow symbolFlow();
}
