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
        return new ExecutionPlan(assignments, resultNode);
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
            case ConditionalNode c -> new ExecutableConditional(
                    c.conditions().stream().map(cond -> buildNode(cond, model, runtimeServices)).toList(),
                    c.results().stream().map(res -> buildNode(res, model, runtimeServices)).toList(),
                    buildNode(c.elseExpression(), model, runtimeServices)
            );
            case VectorLiteralNode v -> new ExecutableVectorLiteral(
                    v.elements().stream().map(e -> buildNode(e, model, runtimeServices)).toList()
            );
        };
    }

    private ExecutableNode buildLiteral(LiteralNode lit, SemanticModel model) {
        String raw = lit.value();
        return switch (raw) {
            case "currDate" -> new ExecutableDynamicLiteral(DynamicInstant.CURR_DATE);
            case "currTime" -> new ExecutableDynamicLiteral(DynamicInstant.CURR_TIME);
            case "currDateTime" -> new ExecutableDynamicLiteral(DynamicInstant.CURR_DATETIME);
            default -> {
                ResolvedType resolvedType = model.findResolvedType(lit.nodeId())
                        .orElseThrow(() -> new IllegalStateException(
                                "missing resolved type for literal '" + raw + "'"));
                yield new ExecutableLiteral(materialize(raw, resolvedType));
            }
        };
    }

    private RuntimeValue materialize(String raw, ResolvedType resolvedType) {
        if (resolvedType == ScalarType.NUMBER) {
            return new RuntimeValue.NumberValue(new BigDecimal(raw));
        }
        if (resolvedType == ScalarType.BOOLEAN) {
            return new RuntimeValue.BooleanValue(Boolean.parseBoolean(raw));
        }
        if (resolvedType == ScalarType.STRING) {
            return new RuntimeValue.StringValue(unquote(raw));
        }
        if (resolvedType == ScalarType.DATE) {
            return new RuntimeValue.DateValue(LocalDate.parse(raw));
        }
        if (resolvedType == ScalarType.TIME) {
            return new RuntimeValue.TimeValue(LocalTime.parse(raw));
        }
        if (resolvedType == ScalarType.DATETIME) {
            return raw.contains("+") || raw.endsWith("Z")
                    ? new RuntimeValue.DateTimeValue(OffsetDateTime.parse(raw).toLocalDateTime())
                    : new RuntimeValue.DateTimeValue(LocalDateTime.parse(raw));
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
                RuntimeValue rv = constantRuntimeValue(arguments.get(i), runtimeServices);
                args[i] = runtimeServices.coerce(rv, descriptor.parameterTypes().get(i));
            }
            Object rawResult = descriptor.invoke(args);
            return ExecutableFunctionCall.folded(binding, arguments, args, rawResult);
        }

        return ExecutableFunctionCall.of(binding, arguments);
    }

    private boolean isConstantNode(ExecutableNode node) {
        return switch (node) {
            case ExecutableLiteral ignored -> true;
            case ExecutableFunctionCall f -> f.isFolded();
            default -> false;
        };
    }

    private RuntimeValue constantRuntimeValue(ExecutableNode node, RuntimeServices runtimeServices) {
        return switch (node) {
            case ExecutableLiteral lit -> lit.precomputed();
            case ExecutableFunctionCall f -> runtimeServices.from(f.foldedResult(), f.binding().returnType());
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
