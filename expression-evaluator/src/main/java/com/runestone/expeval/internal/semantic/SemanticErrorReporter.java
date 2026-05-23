package com.runestone.expeval.internal.semantic;

import com.runestone.expeval.api.IssueCode;
import com.runestone.expeval.internal.ast.SourceSpan;

@FunctionalInterface
interface SemanticErrorReporter {

    void error(IssueCode code, String message, SourceSpan sourceSpan);
}
