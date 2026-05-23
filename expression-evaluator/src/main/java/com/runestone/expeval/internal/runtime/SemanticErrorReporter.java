package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.api.IssueCode;
import com.runestone.expeval.internal.ast.SourceSpan;

@FunctionalInterface
interface SemanticErrorReporter {

    void error(IssueCode code, String message, SourceSpan sourceSpan);
}
