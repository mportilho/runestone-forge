package com.runestone.expeval_mk3.internal.semantics;

import com.runestone.expeval_mk3.api.ExpressionType;

public sealed interface ResolvedSymbol permits CurrentItemResolvedSymbol, NamedResolvedSymbol {

    String name();

    ResolvedSymbolKind kind();

    ExpressionType type();

    int slot();
}
