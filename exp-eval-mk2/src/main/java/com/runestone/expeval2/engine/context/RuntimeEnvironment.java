package com.runestone.expeval2.engine.context;

import com.runestone.expeval2.catalog.ExternalSymbolCatalog;
import com.runestone.expeval2.runtime.RuntimeCoercionService;
import com.runestone.expeval2.runtime.RuntimeValueFactory;

public interface RuntimeEnvironment {

    ExternalSymbolCatalog externalSymbolCatalog();

    RuntimeValueFactory runtimeValueFactory();

    RuntimeCoercionService runtimeCoercionService();
}
