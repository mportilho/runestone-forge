package com.runestone.expeval2.environment;

import com.runestone.converters.DataConversionService;
import com.runestone.expeval2.catalog.ExternalSymbolCatalog;
import com.runestone.expeval2.catalog.FunctionCatalog;
import com.runestone.expeval2.internal.runtime.RuntimeServices;

import java.math.MathContext;
import java.util.Objects;

public final class ExpressionEnvironment {

    private final ExpressionEnvironmentId environmentId;
    private final FunctionCatalog functionCatalog;
    private final ExternalSymbolCatalog externalSymbolCatalog;
    private final DataConversionService dataConversionService;
    private final MathContext mathContext;
    private final MathContext transcendentalMathContext;
    private final RuntimeServices runtimeServices;

    ExpressionEnvironment(ExpressionEnvironmentId environmentId, FunctionCatalog functionCatalog, ExternalSymbolCatalog externalSymbolCatalog,
                          DataConversionService dataConversionService, MathContext mathContext, MathContext transcendentalMathContext) {
        this.environmentId = Objects.requireNonNull(environmentId, "environmentId must not be null");
        this.functionCatalog = Objects.requireNonNull(functionCatalog, "functionCatalog must not be null");
        this.externalSymbolCatalog = Objects.requireNonNull(externalSymbolCatalog, "externalSymbolCatalog must not be null");
        this.dataConversionService = Objects.requireNonNull(dataConversionService, "dataConversionService must not be null");
        this.mathContext = Objects.requireNonNull(mathContext, "mathContext must not be null");
        this.transcendentalMathContext = Objects.requireNonNull(transcendentalMathContext, "transcendentalMathContext must not be null");
        this.runtimeServices = new RuntimeServices(dataConversionService);
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

    public MathContext mathContext() {
        return mathContext;
    }

    public MathContext transcendentalMathContext() {
        return transcendentalMathContext;
    }

    public RuntimeServices runtimeServices() {
        return runtimeServices;
    }
}
