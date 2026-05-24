package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.internal.execution.eval.ExecutionScope;
import com.runestone.expeval.internal.execution.eval.FilterContextStack;
import com.runestone.expeval.internal.execution.plan.*;

import com.runestone.expeval.api.AuditEvent;
import com.runestone.expeval.api.CompilationPosition;
import com.runestone.expeval.api.ExpressionEvaluationException;
import com.runestone.expeval.internal.audit.AuditCollector;
import com.runestone.expeval.internal.LanguageSymbols;
import com.runestone.expeval.internal.ast.SourceSpan;

import java.util.Objects;

final class SymbolValueEvaluator {

    private final String source;

    SymbolValueEvaluator(String source) {
        this.source = Objects.requireNonNull(source, "source");
    }

    Object evaluateDynamicLiteral(ExecutableDynamicLiteral literal, ExecutionScope scope) {
        Object value = scope.resolveDynamic(literal.kind());
        AuditCollector audit = scope.audit();
        if (audit != null) {
            audit.record(new AuditEvent.VariableRead(literal.kind().canonicalName(), true, value));
        }
        return value;
    }

    Object evaluateIdentifier(ExecutableIdentifier identifier, ExecutionScope scope) {
        if (LanguageSymbols.CURRENT_ELEMENT.equals(identifier.ref().name())) {
            return evaluateCurrentElement();
        }
        Object value = scope.find(identifier.ref());
        if (value == ExecutionScope.UNBOUND) {
            throw unboundVariableException(identifier);
        }
        AuditCollector audit = scope.audit();
        if (audit != null) {
            audit.record(new AuditEvent.VariableRead(identifier.ref().name(), false, value));
        }
        return value;
    }

    private Object evaluateCurrentElement() {
        var context = FilterContextStack.INSTANCE.get().peek();
        if (context == null) {
            throw new ExpressionEvaluationException(source,
                    "INVALID_CURRENT_ELEMENT",
                    "'@' used outside of a filter predicate context", null);
        }
        return context.isMapContext() ? context.mapValue() : context.element();
    }

    private ExpressionEvaluationException unboundVariableException(ExecutableIdentifier identifier) {
        SourceSpan span = identifier.sourceSpan();
        CompilationPosition position = new CompilationPosition(span.startLine(), span.startColumn(), span.endColumn());
        String name = identifier.ref().name();
        String message = "variable '" + name + "' has no value; call setValue(\"" + name + "\", ...) before compute()";
        return new ExpressionEvaluationException(source, "UNBOUND_VARIABLE", message, position);
    }
}
