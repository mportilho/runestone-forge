package com.runestone.expeval2.environment;

import com.runestone.converters.DataConversionService;
import com.runestone.expeval2.catalog.ExternalSymbolCatalog;
import com.runestone.expeval2.catalog.FunctionCatalog;

import java.util.Objects;

public final class ExpressionEnvironment {

    private final ExpressionEnvironmentId environmentId;
    private final FunctionCatalog functionCatalog;
    private final ExternalSymbolCatalog externalSymbolCatalog;
    private final DataConversionService dataConversionService;

    ExpressionEnvironment(ExpressionEnvironmentId environmentId, FunctionCatalog functionCatalog, ExternalSymbolCatalog externalSymbolCatalog,
                          DataConversionService dataConversionService) {
        this.environmentId = Objects.requireNonNull(environmentId, "environmentId must not be null");
        this.functionCatalog = Objects.requireNonNull(functionCatalog, "functionCatalog must not be null");
        this.externalSymbolCatalog = Objects.requireNonNull(externalSymbolCatalog, "externalSymbolCatalog must not be null");
        this.dataConversionService = Objects.requireNonNull(dataConversionService, "dataConversionService must not be null");
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

    public DataConversionService getDataConversionService() {
        return dataConversionService;
    }
}
