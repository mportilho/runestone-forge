package compat;

import com.runestone.dynafilter.core.operation.FilterOperationService;
import com.runestone.dynafilter.modules.openapi.DynaFilterOperationCustomizer;

// The adjacent Base64 class file was compiled with Java 21 against the constructor
// API snapshot of de5927a, not against the implementation under test. Do not recompile in tests.
public final class LegacyCustomizerClient {
    public static DynaFilterOperationCustomizer create(FilterOperationService<?> operations) {
        return new DynaFilterOperationCustomizer(null, operations);
    }
}
