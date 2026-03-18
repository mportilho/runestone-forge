package com.runestone.expeval2.internal.runtime;

import com.runestone.expeval2.catalog.ExternalSymbolCatalog;
import com.runestone.expeval2.catalog.FunctionCatalog;
import com.runestone.expeval2.internal.grammar.ExpressionResultType;

import java.util.Objects;

public final class ResolutionContext {

    private final ExpressionResultType resultType;
    private final FunctionCatalog functionCatalog;
    private final ExternalSymbolCatalog externalSymbolCatalog;

    public ResolutionContext(ExpressionResultType resultType, FunctionCatalog functionCatalog, ExternalSymbolCatalog externalSymbolCatalog) {
        this.resultType = Objects.requireNonNull(resultType, "resultType must not be null");
        this.functionCatalog = Objects.requireNonNull(functionCatalog, "functionCatalog must not be null");
        this.externalSymbolCatalog = Objects.requireNonNull(externalSymbolCatalog, "externalSymbolCatalog must not be null");
    }

    public ExpressionResultType resultType() {
        return resultType;
    }

    public FunctionCatalog functionCatalog() {
        return functionCatalog;
    }

    public ExternalSymbolCatalog externalSymbolCatalog() {
        return externalSymbolCatalog;
    }
}
