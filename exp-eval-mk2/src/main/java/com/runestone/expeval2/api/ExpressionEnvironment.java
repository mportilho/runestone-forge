package com.runestone.expeval2.api;

import com.runestone.expeval2.catalog.ExternalSymbolCatalog;
import com.runestone.expeval2.catalog.FunctionCatalog;
import com.runestone.expeval2.engine.context.CompilationEnvironment;
import com.runestone.expeval2.engine.context.ExpressionEnvironmentId;
import com.runestone.expeval2.engine.context.RuntimeEnvironment;
import com.runestone.expeval2.runtime.RuntimeCoercionService;
import com.runestone.expeval2.runtime.RuntimeValueFactory;

import java.util.Objects;

public final class ExpressionEnvironment implements CompilationEnvironment, RuntimeEnvironment {

    private final ExpressionEnvironmentId environmentId;
    private final FunctionCatalog functionCatalog;
    private final ExternalSymbolCatalog externalSymbolCatalog;
    private final RuntimeValueFactory runtimeValueFactory;
    private final RuntimeCoercionService runtimeCoercionService;

    ExpressionEnvironment(ExpressionEnvironmentId environmentId, FunctionCatalog functionCatalog, ExternalSymbolCatalog externalSymbolCatalog,
                          RuntimeValueFactory runtimeValueFactory, RuntimeCoercionService runtimeCoercionService) {
        this.environmentId = Objects.requireNonNull(environmentId, "environmentId must not be null");
        this.functionCatalog = Objects.requireNonNull(functionCatalog, "functionCatalog must not be null");
        this.externalSymbolCatalog = Objects.requireNonNull(externalSymbolCatalog, "externalSymbolCatalog must not be null");
        this.runtimeValueFactory = Objects.requireNonNull(runtimeValueFactory, "runtimeValueFactory must not be null");
        this.runtimeCoercionService = Objects.requireNonNull(runtimeCoercionService, "runtimeCoercionService must not be null");
    }

    public static ExpressionEnvironmentBuilder builder() {
        return new ExpressionEnvironmentBuilder();
    }

    public ExpressionEnvironmentId environmentId() {
        return environmentId;
    }

    public FunctionCatalog functionCatalog() {
        return functionCatalog;
    }

    public ExternalSymbolCatalog externalSymbolCatalog() {
        return externalSymbolCatalog;
    }

    public RuntimeValueFactory runtimeValueFactory() {
        return runtimeValueFactory;
    }

    public RuntimeCoercionService runtimeCoercionService() {
        return runtimeCoercionService;
    }
}
