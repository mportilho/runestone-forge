package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.api.AuditEvent;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

final class AssignmentEvaluator {

    private final NodeEvaluator nodeEvaluator;

    AssignmentEvaluator(NodeEvaluator nodeEvaluator) {
        this.nodeEvaluator = Objects.requireNonNull(nodeEvaluator, "nodeEvaluator");
    }

    void execute(List<ExecutableAssignment> assignments, ExecutionScope scope) {
        for (ExecutableAssignment assignment : assignments) {
            execute(assignment, scope);
        }
    }

    Map<String, Object> evaluateAssignments(List<ExecutableAssignment> assignments, ExecutionScope scope) {
        execute(assignments, scope);
        Map<String, Object> result = new LinkedHashMap<>(assignments.size());
        for (ExecutableAssignment assignment : assignments) {
            switch (assignment) {
                case ExecutableSimpleAssignment simpleAssignment -> readSimpleAssignment(simpleAssignment, scope, result);
                case ExecutableDestructuringAssignment destructuringAssignment ->
                        readDestructuringAssignment(destructuringAssignment, scope, result);
            }
        }
        return result;
    }

    private void execute(ExecutableAssignment assignment, ExecutionScope scope) {
        switch (assignment) {
            case ExecutableSimpleAssignment simpleAssignment -> executeSimpleAssignment(simpleAssignment, scope);
            case ExecutableDestructuringAssignment destructuringAssignment ->
                    executeDestructuringAssignment(destructuringAssignment, scope);
        }
    }

    private void executeSimpleAssignment(ExecutableSimpleAssignment assignment, ExecutionScope scope) {
        Object value = nodeEvaluator.evaluate(assignment.value(), scope);
        scope.assign(assignment.target(), value);
        AuditCollector audit = scope.audit();
        if (audit != null) {
            audit.record(new AuditEvent.AssignmentEvent(assignment.target().name(), value));
        }
    }

    private void executeDestructuringAssignment(ExecutableDestructuringAssignment assignment, ExecutionScope scope) {
        @SuppressWarnings("unchecked")
        List<Object> elements = (List<Object>) nodeEvaluator.evaluate(assignment.value(), scope);
        AuditCollector audit = scope.audit();
        List<SymbolRef> targets = assignment.targets();
        for (int index = 0; index < targets.size(); index++) {
            SymbolRef target = targets.get(index);
            Object element = index < elements.size() ? elements.get(index) : null;
            scope.assign(target, element);
            if (audit != null) {
                audit.record(new AuditEvent.AssignmentEvent(target.name(), element));
            }
        }
    }

    private static void readSimpleAssignment(
            ExecutableSimpleAssignment assignment,
            ExecutionScope scope,
            Map<String, Object> result) {
        Object value = scope.find(assignment.target());
        result.put(assignment.target().name(), value == ExecutionScope.UNBOUND ? null : value);
    }

    private static void readDestructuringAssignment(
            ExecutableDestructuringAssignment assignment,
            ExecutionScope scope,
            Map<String, Object> result) {
        for (SymbolRef target : assignment.targets()) {
            Object value = scope.find(target);
            result.put(target.name(), value == ExecutionScope.UNBOUND ? null : value);
        }
    }
}
