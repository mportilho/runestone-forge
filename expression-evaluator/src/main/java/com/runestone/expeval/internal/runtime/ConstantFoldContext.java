package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.api.AuditEvent;
import com.runestone.expeval.internal.semantic.SymbolRef;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class ConstantFoldContext {

    final Map<SymbolRef, Object> symbols = new HashMap<>();
    final List<AuditEvent> variableReads = new ArrayList<>();
}
