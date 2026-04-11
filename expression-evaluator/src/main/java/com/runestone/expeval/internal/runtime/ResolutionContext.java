package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.catalog.ExternalSymbolCatalog;
import com.runestone.expeval.catalog.FunctionCatalog;
import com.runestone.expeval.catalog.TypeHintCatalog;
import com.runestone.expeval.internal.grammar.ExpressionResultType;

import java.util.Objects;

public final class ResolutionContext {

    private final ExpressionResultType resultType;
    private final FunctionCatalog functionCatalog;
    private final ExternalSymbolCatalog externalSymbolCatalog;
    private final TypeHintCatalog typeHintCatalog;

    public ResolutionContext(ExpressionResultType resultType, FunctionCatalog functionCatalog,
                             ExternalSymbolCatalog externalSymbolCatalog, TypeHintCatalog typeHintCatalog) {
        this.resultType = Objects.requireNonNull(resultType, "resultType must not be null");
        this.functionCatalog = Objects.requireNonNull(functionCatalog, "functionCatalog must not be null");
        this.externalSymbolCatalog = Objects.requireNonNull(externalSymbolCatalog, "externalSymbolCatalog must not be null");
        this.typeHintCatalog = Objects.requireNonNull(typeHintCatalog, "typeHintCatalog must not be null");
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

    public TypeHintCatalog typeHintCatalog() {
        return typeHintCatalog;
    }
}
