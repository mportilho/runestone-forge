package com.runestone.expeval.internal.execution.plan;

import com.runestone.expeval.api.AuditEvent;
import com.runestone.expeval.internal.LanguageSymbols;
import com.runestone.expeval.internal.ast.SourceSpan;
import com.runestone.expeval.internal.semantic.SymbolKind;
import com.runestone.expeval.internal.semantic.SymbolRef;

final class SymbolReadPlanner {

    private SymbolReadPlanner() {
    }

    static ExecutableNode currentElement(SourceSpan sourceSpan) {
        return new ExecutableIdentifier(
                new SymbolRef(LanguageSymbols.CURRENT_ELEMENT, SymbolKind.EXTERNAL),
                sourceSpan);
    }

    static ExecutableNode read(SymbolRef ref, SourceSpan sourceSpan, ConstantFoldContext foldContext) {
        if (foldContext.symbols.containsKey(ref)) {
            Object value = foldContext.symbols.get(ref);
            foldContext.variableReads.add(new AuditEvent.VariableRead(ref.name(), false, value));
            return new ExecutableLiteral(value);
        }
        return new ExecutableIdentifier(ref, sourceSpan);
    }
}
