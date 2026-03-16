package com.runestone.expeval2.engine.context;

import com.runestone.expeval2.catalog.ExternalSymbolCatalog;
import com.runestone.expeval2.catalog.FunctionCatalog;

public interface CompilationEnvironment {

    ExpressionEnvironmentId environmentId();

    FunctionCatalog functionCatalog();

    ExternalSymbolCatalog externalSymbolCatalog();
}
