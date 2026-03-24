package com.runestone.expeval2.internal.runtime;

import com.runestone.expeval2.internal.ast.*;
import com.runestone.expeval2.catalog.FunctionDescriptor;
import com.runestone.expeval2.types.ResolvedType;
import com.runestone.expeval2.types.ScalarType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;

final class ExecutionPlanBuilder {

    ExecutionPlan build(SemanticModel model, RuntimeServices runtimeServices) {
        ExpressionFileNode ast = model.ast();
        List<ExecutableAssignment> assignments = ast.assignments().stream()
                .map(assignment -> buildAssignment(assignment, model, runtimeServices))
                .toList();
        ExecutableNode resultNode = ast.resultExpression() != null ? buildNode(ast.resultExpression(), model, runtimeServices) : null;
        int maxAuditEvents = countMaxAuditEvents(assignments, resultNode);
        return new ExecutionPlan(assignments, resultNode, maxAuditEvents);
    }

    private int countMaxAuditEvents(List<ExecutableAssignment> assignments, ExecutableNode resultExpression) {
        int count = 0;
        for (ExecutableAssignment assignment : assignments) {
            count += switch (assignment) {
                case ExecutableSimpleAssignment s -> 1 + countNodeEvents(s.value());
                case ExecutableDestructuringAssignment d -> d.targets().size() + countNodeEvents(d.value());
            };
        }
        if (resultExpression != null) {
            count += countNodeEvents(resultExpression);
        }
        return count;
    }

    private int countNodeEvents(ExecutableNode node) {
        return switch (node) {
            case ExecutableLiteral ignored -> 0;
            case ExecutableDynamicLiteral ignored -> 1;
            case ExecutableIdentifier ignored -> 1;
            case ExecutableFunctionCall f -> {
                int sum = 1;
                for (ExecutableNode arg : f.arguments()) sum += countNodeEvents(arg);
                yield sum;
            }
            case ExecutableBinaryOp b -> countNodeEvents(b.left()) + countNodeEvents(b.right());
            case ExecutableUnaryOp u -> countNodeEvents(u.operand());
            case ExecutablePostfixOp p -> countNodeEvents(p.operand());
            case ExecutableConditional c -> {
                int condCost = 0;
                for (ExecutableNode cond : c.conditions()) condCost += countNodeEvents(cond);

                int maxBranchCost = countNodeEvents(c.elseExpression());
                for (ExecutableNode res : c.results()) {
                    maxBranchCost = Math.max(maxBranchCost, countNodeEvents(res));
                }
                yield condCost + maxBranchCost;
            }
            case ExecutableSimpleConditional sc -> {
                int condCost = countNodeEvents(sc.condition());
                int maxBranchCost = Math.max(countNodeEvents(sc.thenExpression()), countNodeEvents(sc.elseExpression()));
                yield condCost + maxBranchCost;
            }
            case ExecutableVectorLiteral v -> {
                int sum = 0;
                for (ExecutableNode el : v.elements()) sum += countNodeEvents(el);
                yield sum;
            }
        };
    }

    private ExecutableAssignment buildAssignment(AssignmentNode assignment, SemanticModel model, RuntimeServices runtimeServices) {
        return switch (assignment) {
            case SimpleAssignmentNode s -> {
                SymbolRef target = model.internalSymbolsByName().get(s.targetName());
                yield new ExecutableSimpleAssignment(target, buildNode(s.value(), model, runtimeServices));
            }
            case DestructuringAssignmentNode d -> {
                List<SymbolRef> targets = d.targetNames().stream()
                        .map(name -> model.internalSymbolsByName().get(name))
                        .toList();
                yield new ExecutableDestructuringAssignment(targets, buildNode(d.value(), model, runtimeServices));
            }
        };
    }

    private ExecutableNode buildNode(ExpressionNode node, SemanticModel model, RuntimeServices runtimeServices) {
        return switch (node) {
            case LiteralNode lit -> buildLiteral(lit, model);
            case IdentifierNode id -> buildIdentifier(id, model);
            case FunctionCallNode f -> buildFunctionCall(f, model, runtimeServices);
            case BinaryOperationNode b -> new ExecutableBinaryOp(
                    b.operator(),
                    buildNode(b.left(), model, runtimeServices),
                    buildNode(b.right(), model, runtimeServices)
            );
            case UnaryOperationNode u -> new ExecutableUnaryOp(
                    u.operator(),
                    buildNode(u.operand(), model, runtimeServices)
            );
            case PostfixOperationNode p -> new ExecutablePostfixOp(
                    p.operator(),
                    buildNode(p.operand(), model, runtimeServices)
            );
            case ConditionalNode c -> {
                if (c.conditions().size() == 1) {
                    yield new ExecutableSimpleConditional(
                            buildNode(c.conditions().get(0), model, runtimeServices),
                            buildNode(c.results().get(0), model, runtimeServices),
                            buildNode(c.elseExpression(), model, runtimeServices)
                    );
                }
                yield new ExecutableConditional(
                        c.conditions().stream().map(cond -> buildNode(cond, model, runtimeServices)).toList(),
                        c.results().stream().map(res -> buildNode(res, model, runtimeServices)).toList(),
                        buildNode(c.elseExpression(), model, runtimeServices)
                );
            }
            case VectorLiteralNode v -> {
                List<ExecutableNode> elements = v.elements().stream()
                        .map(e -> buildNode(e, model, runtimeServices))
                        .toList();
                if (elements.stream().allMatch(this::isConstantNode)) {
                    List<Object> foldedValues = elements.stream().map(this::constantValue).toList();
                    yield new ExecutableVectorLiteral(elements, foldedValues);
                }
                yield new ExecutableVectorLiteral(elements);
            }
        };
    }

    private ExecutableNode buildLiteral(LiteralNode lit, SemanticModel model) {
        String text = lit.value();
        return switch (text) {
            case "currDate"     -> new ExecutableDynamicLiteral(DynamicInstant.CURR_DATE);
            case "currTime"     -> new ExecutableDynamicLiteral(DynamicInstant.CURR_TIME);
            case "currDateTime" -> new ExecutableDynamicLiteral(DynamicInstant.CURR_DATETIME);
            default -> {
                ResolvedType resolvedType = model.findResolvedType(lit.nodeId())
                        .orElseThrow(() -> new IllegalStateException(
                                "missing resolved type for literal '" + text + "'"));
                yield new ExecutableLiteral(materialize(text, resolvedType));
            }
        };
    }

    private Object materialize(String text, ResolvedType resolvedType) {
        if (resolvedType == ScalarType.NUMBER)   return new BigDecimal(text);
        if (resolvedType == ScalarType.BOOLEAN)  return Boolean.parseBoolean(text);
        if (resolvedType == ScalarType.STRING)   return unquote(text);
        if (resolvedType == ScalarType.DATE)     return LocalDate.parse(text);
        if (resolvedType == ScalarType.TIME)     return LocalTime.parse(text);
        if (resolvedType == ScalarType.DATETIME) {
            return text.contains("+") || text.endsWith("Z")
                    ? OffsetDateTime.parse(text).toLocalDateTime()
                    : LocalDateTime.parse(text);
        }
        throw new IllegalStateException("unsupported literal type: " + resolvedType);
    }

    private ExecutableNode buildIdentifier(IdentifierNode id, SemanticModel model) {
        SymbolRef ref = model.findSymbol(id.nodeId())
                .orElseThrow(() -> new IllegalStateException(
                        "missing symbol for identifier '" + id.name() + "'"));
        return new ExecutableIdentifier(ref, id.sourceSpan());
    }

    private ExecutableNode buildFunctionCall(FunctionCallNode f, SemanticModel model, RuntimeServices runtimeServices) {
        ResolvedFunctionBinding binding = model.findFunctionBinding(f.nodeId())
                .orElseThrow(() -> new IllegalStateException(
                        "missing function binding for '" + f.functionName() + "'"));
        List<ExecutableNode> arguments = f.arguments().stream()
                .map(arg -> buildNode(arg, model, runtimeServices))
                .toList();

        FunctionDescriptor descriptor = binding.descriptor();
        if (descriptor.isFoldable() && arguments.stream().allMatch(this::isConstantNode)) {
            int arity = descriptor.arity();
            Object[] args = new Object[arity];
            for (int i = 0; i < arity; i++) {
                args[i] = runtimeServices.coerce(
                        constantValue(arguments.get(i)),
                        descriptor.parameterTypes().get(i));
            }
            Object result = descriptor.invoke(args);
            return ExecutableFunctionCall.folded(binding, arguments, args, result);
        }

        return ExecutableFunctionCall.of(binding, arguments);
    }

    private boolean isConstantNode(ExecutableNode node) {
        return switch (node) {
            case ExecutableLiteral ignored       -> true;
            case ExecutableFunctionCall f        -> f.isFolded();
            case ExecutableVectorLiteral v       -> v.isFolded();
            default -> false;
        };
    }

    private Object constantValue(ExecutableNode node) {
        return switch (node) {
            case ExecutableLiteral lit           -> lit.precomputed();
            case ExecutableFunctionCall f        -> f.foldedResult();
            case ExecutableVectorLiteral v       -> v.foldedValue();
            default -> throw new IllegalStateException("not a constant node: " + node);
        };
    }

    private String unquote(String value) {
        if (value.length() < 2) {
            return value;
        }
        if (value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1)
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\");
        }
        return value;
    }
}
