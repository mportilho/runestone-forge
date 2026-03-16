package com.runestone.expeval2.semantic;

import com.runestone.expeval2.api.ExpressionEnvironment;
import com.runestone.expeval2.catalog.ExternalSymbolCatalog;
import com.runestone.expeval2.catalog.FunctionCatalog;
import com.runestone.expeval2.grammar.language.ExpressionResultType;

import java.util.Objects;

public final class ResolutionContext {

    private final ExpressionResultType resultType;
    private final FunctionCatalog functionCatalog;
    private final ExternalSymbolCatalog externalSymbolCatalog;

    private ResolutionContext(
        ExpressionResultType resultType,
        FunctionCatalog functionCatalog,
        ExternalSymbolCatalog externalSymbolCatalog
    ) {
        this.resultType = Objects.requireNonNull(resultType, "resultType must not be null");
        this.functionCatalog = Objects.requireNonNull(functionCatalog, "functionCatalog must not be null");
        this.externalSymbolCatalog = Objects.requireNonNull(externalSymbolCatalog, "externalSymbolCatalog must not be null");
    }

    public static ResolutionContext from(ExpressionResultType resultType, ExpressionEnvironment environment) {
        Objects.requireNonNull(environment, "environment must not be null");
        return new ResolutionContext(resultType, environment.functionCatalog(), environment.externalSymbolCatalog());
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
