package com.runestone.expeval2.internal.runtime;

import com.runestone.expeval2.catalog.*;
import com.runestone.expeval2.internal.ast.*;
import com.runestone.expeval2.internal.ast.BinaryOperator;
import com.runestone.expeval2.internal.ast.TernaryOperationNode;
import com.runestone.expeval2.types.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

final class ExecutionPlanBuilder {

    ExecutionPlan build(SemanticModel model, RuntimeServices runtimeServices, ExternalSymbolCatalog externalSymbolCatalog, TypeHintCatalog typeHintCatalog) {
        ExpressionFileNode ast = model.ast();
        List<ExecutableAssignment> assignments = ast.assignments().stream()
                .map(assignment -> buildAssignment(assignment, model, runtimeServices, externalSymbolCatalog, typeHintCatalog))
                .toList();
        ExecutableNode resultNode = ast.resultExpression() != null
                ? buildNode(ast.resultExpression(), model, runtimeServices, externalSymbolCatalog, typeHintCatalog)
                : null;
        int maxAuditEvents = countMaxAuditEvents(assignments, resultNode);
        Map<SymbolRef, Object> defaults = seedDefaults(model, externalSymbolCatalog, runtimeServices);
        Map<String, ExternalBindingPlan> externalBindingPlans = seedExternalBindingPlans(model, externalSymbolCatalog);
        return new ExecutionPlan(assignments, resultNode, defaults, externalBindingPlans, maxAuditEvents);
    }

    private static Map<SymbolRef, Object> seedDefaults(SemanticModel semanticModel, ExternalSymbolCatalog catalog, RuntimeServices runtimeServices) {
        if (semanticModel.externalSymbolsByName().isEmpty()) {
            return Map.of();
        }
        Map<SymbolRef, Object> defaults = new HashMap<>();
        semanticModel.externalSymbolsByName().forEach((name, symbolRef) ->
                catalog.find(name)
                        .ifPresent(descriptor -> defaults.put(
                                symbolRef,
                                runtimeServices.coerceToResolvedType(descriptor.defaultValue(), descriptor.declaredType())
                        ))
        );
        return defaults;
    }

    private static Map<String, ExternalBindingPlan> seedExternalBindingPlans(SemanticModel semanticModel, ExternalSymbolCatalog catalog) {
        if (semanticModel.externalSymbolsByName().isEmpty()) {
            return Map.of();
        }
        Map<String, ExternalBindingPlan> bindings = new HashMap<>();
        semanticModel.externalSymbolsByName().forEach((name, symbolRef) -> {
            ExternalSymbolDescriptor descriptor = catalog.findOrNull(name);
            bindings.put(name, new ExternalBindingPlan(
                    symbolRef,
                    descriptor != null ? descriptor.declaredType() : null,
                    descriptor == null || descriptor.overridable()
            ));
        });
        return bindings;
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
            case ExecutablePropertyChain chain -> countPropertyChainEvents(chain);
            case ExecutableFunctionCall f -> {
                int sum = 1;
                for (ExecutableNode arg : f.arguments()) sum += countNodeEvents(arg);
                yield sum;
            }
            case ExecutableBinaryOp b -> countNodeEvents(b.left()) + countNodeEvents(b.right());
            case ExecutableTernaryOp t -> countNodeEvents(t.first()) + countNodeEvents(t.second()) + countNodeEvents(t.third());
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
            case ExecutableNullCoalesce nc ->
                    countNodeEvents(nc.left()) + countNodeEvents(nc.right());
            case ExecutableRegexOp r -> countNodeEvents(r.subject());
        };
    }

    private int countPropertyChainEvents(ExecutablePropertyChain chain) {
        int count = 1;
        for (ExecutablePropertyChain.ExecutableAccess access : chain.chain()) {
            if (access instanceof ExecutablePropertyChain.ExecutableMethodInvoke methodInvoke) {
                for (ExecutableNode argument : methodInvoke.arguments()) {
                    count += countNodeEvents(argument);
                }
                continue;
            }
            if (access instanceof ExecutablePropertyChain.ReflectiveMethodInvoke reflectiveMethodInvoke) {
                for (ExecutableNode argument : reflectiveMethodInvoke.arguments()) {
                    count += countNodeEvents(argument);
                }
            }
        }
        return count;
    }

    private ExecutableAssignment buildAssignment(
            AssignmentNode assignment,
            SemanticModel model,
            RuntimeServices runtimeServices,
            ExternalSymbolCatalog externalSymbolCatalog,
            TypeHintCatalog typeHintCatalog) {
        return switch (assignment) {
            case SimpleAssignmentNode s -> {
                SymbolRef target = model.internalSymbolsByName().get(s.targetName());
                yield new ExecutableSimpleAssignment(
                        target,
                        buildNode(s.value(), model, runtimeServices, externalSymbolCatalog, typeHintCatalog)
                );
            }
            case DestructuringAssignmentNode d -> {
                List<SymbolRef> targets = d.targetNames().stream()
                        .map(name -> model.internalSymbolsByName().get(name))
                        .toList();
                yield new ExecutableDestructuringAssignment(
                        targets,
                        buildNode(d.value(), model, runtimeServices, externalSymbolCatalog, typeHintCatalog)
                );
            }
        };
    }

    private ExecutableNode buildNode(
            ExpressionNode node,
            SemanticModel model,
            RuntimeServices runtimeServices,
            ExternalSymbolCatalog externalSymbolCatalog,
            TypeHintCatalog typeHintCatalog) {
        return switch (node) {
            case LiteralNode lit -> buildLiteral(lit, model);
            case IdentifierNode id -> buildIdentifier(id, model);
            case PropertyChainNode chain -> buildPropertyChain(
                    chain, model, runtimeServices, externalSymbolCatalog, typeHintCatalog);
            case FunctionCallNode f ->
                    buildFunctionCall(f, model, runtimeServices, externalSymbolCatalog, typeHintCatalog);
            case BinaryOperationNode b when b.operator() == BinaryOperator.NULL_COALESCE -> new ExecutableNullCoalesce(
                    buildNode(b.left(), model, runtimeServices, externalSymbolCatalog, typeHintCatalog),
                    buildNode(b.right(), model, runtimeServices, externalSymbolCatalog, typeHintCatalog)
            );
            case BinaryOperationNode b when b.operator() == BinaryOperator.REGEX_MATCH
                                         || b.operator() == BinaryOperator.REGEX_NOT_MATCH -> {
                boolean negate = b.operator() == BinaryOperator.REGEX_NOT_MATCH;
                ExecutableNode subjectNode = buildNode(b.left(), model, runtimeServices, externalSymbolCatalog, typeHintCatalog);
                LiteralNode patternLit = (LiteralNode) b.right();
                yield new ExecutableRegexOp(subjectNode, Pattern.compile(unquote(patternLit.value())), negate);
            }
            case BinaryOperationNode b -> new ExecutableBinaryOp(
                    b.operator(),
                    buildNode(b.left(), model, runtimeServices, externalSymbolCatalog, typeHintCatalog),
                    buildNode(b.right(), model, runtimeServices, externalSymbolCatalog, typeHintCatalog)
            );
            case TernaryOperationNode t -> new ExecutableTernaryOp(
                    t.operator(),
                    buildNode(t.first(),  model, runtimeServices, externalSymbolCatalog, typeHintCatalog),
                    buildNode(t.second(), model, runtimeServices, externalSymbolCatalog, typeHintCatalog),
                    buildNode(t.third(),  model, runtimeServices, externalSymbolCatalog, typeHintCatalog)
            );
            case UnaryOperationNode u -> new ExecutableUnaryOp(
                    u.operator(),
                    buildNode(u.operand(), model, runtimeServices, externalSymbolCatalog, typeHintCatalog)
            );
            case PostfixOperationNode p -> new ExecutablePostfixOp(
                    p.operator(),
                    buildNode(p.operand(), model, runtimeServices, externalSymbolCatalog, typeHintCatalog)
            );
            case ConditionalNode c -> {
                if (c.conditions().size() == 1) {
                    yield new ExecutableSimpleConditional(
                            buildNode(c.conditions().get(0), model, runtimeServices, externalSymbolCatalog, typeHintCatalog),
                            buildNode(c.results().get(0), model, runtimeServices, externalSymbolCatalog, typeHintCatalog),
                            buildNode(c.elseExpression(), model, runtimeServices, externalSymbolCatalog, typeHintCatalog)
                    );
                }
                yield new ExecutableConditional(
                        c.conditions().stream()
                                .map(cond -> buildNode(cond, model, runtimeServices, externalSymbolCatalog, typeHintCatalog))
                                .toList(),
                        c.results().stream()
                                .map(res -> buildNode(res, model, runtimeServices, externalSymbolCatalog, typeHintCatalog))
                                .toList(),
                        buildNode(c.elseExpression(), model, runtimeServices, externalSymbolCatalog, typeHintCatalog)
                );
            }
            case VectorLiteralNode v -> {
                List<ExecutableNode> elements = v.elements().stream()
                        .map(e -> buildNode(e, model, runtimeServices, externalSymbolCatalog, typeHintCatalog))
                        .toList();
                if (elements.stream().allMatch(this::isConstantNode)) {
                    List<Object> foldedValues = elements.stream().map(this::constantValue).toList();
                    yield new ExecutableVectorLiteral(elements, foldedValues);
                }
                yield new ExecutableVectorLiteral(elements);
            }
        };
    }

    private ExecutableNode buildPropertyChain(
            PropertyChainNode node,
            SemanticModel model,
            RuntimeServices runtimeServices,
            ExternalSymbolCatalog externalSymbolCatalog,
            TypeHintCatalog typeHintCatalog) {
        SymbolRef root = model.findSymbol(node.nodeId())
                .orElseThrow(() -> new IllegalStateException(
                        "missing symbol for property chain '" + node.rootIdentifier() + "'"));
        ResolvedType currentType = resolveRootType(root, model, externalSymbolCatalog);
        List<ExecutablePropertyChain.ExecutableAccess> steps = new ArrayList<>(node.chain().size());

        for (PropertyChainNode.MemberAccess access : node.chain()) {
            boolean safe = access instanceof PropertyChainNode.SafePropertyAccess
                    || access instanceof PropertyChainNode.SafeMethodCallAccess;
            List<ExecutableNode> argumentNodes = switch (access) {
                case PropertyChainNode.MethodCallAccess methodCall ->
                        methodCall.arguments().stream()
                                .map(argument -> buildNode(argument, model, runtimeServices, externalSymbolCatalog, typeHintCatalog))
                                .toList();
                case PropertyChainNode.SafeMethodCallAccess safeMethodCall ->
                        safeMethodCall.arguments().stream()
                                .map(argument -> buildNode(argument, model, runtimeServices, externalSymbolCatalog, typeHintCatalog))
                                .toList();
                default -> List.of();
            };

            if (currentType instanceof ObjectType objectType) {
                TypeMetadata metadata = typeHintCatalog.find(objectType.javaClass()).orElse(null);
                if (metadata != null) {
                    steps.add(buildStaticAccess(access, argumentNodes, model, metadata, typeHintCatalog, safe));
                    currentType = nextType(access, model, metadata, typeHintCatalog);
                    continue;
                }
            }

            steps.add(buildReflectiveAccess(access, argumentNodes, safe));
            currentType = UnknownType.INSTANCE;
        }
        return new ExecutablePropertyChain(root, steps);
    }

    private ExecutablePropertyChain.ExecutableAccess buildStaticAccess(
            PropertyChainNode.MemberAccess access,
            List<ExecutableNode> argumentNodes,
            SemanticModel model,
            TypeMetadata metadata,
            TypeHintCatalog typeHintCatalog,
            boolean safe) {
        return switch (access) {
            case PropertyChainNode.PropertyAccess propertyAccess -> {
                var descriptor = metadata.properties().get(propertyAccess.name());
                if (descriptor == null) {
                    throw new IllegalStateException("missing property metadata for '" + propertyAccess.name()
                                                    + "' on " + metadata.javaClass().getName());
                }
                yield new ExecutablePropertyChain.ExecutableFieldGet(
                        propertyAccess.name(),
                        descriptor.getter(),
                        descriptor.resolvedType(),
                        safe
                );
            }
            case PropertyChainNode.SafePropertyAccess safePropertyAccess -> {
                var descriptor = metadata.properties().get(safePropertyAccess.name());
                if (descriptor == null) {
                    throw new IllegalStateException("missing property metadata for '" + safePropertyAccess.name()
                                                    + "' on " + metadata.javaClass().getName());
                }
                yield new ExecutablePropertyChain.ExecutableFieldGet(
                        safePropertyAccess.name(),
                        descriptor.getter(),
                        descriptor.resolvedType(),
                        true
                );
            }
            case PropertyChainNode.MethodCallAccess methodCall -> {
                MethodDescriptor descriptor = resolveMethodDescriptor(metadata, methodCall, model, typeHintCatalog);
                yield new ExecutablePropertyChain.ExecutableMethodInvoke(
                        methodCall.name(),
                        descriptor.handle(),
                        argumentNodes,
                        descriptor.parameterTypes(),
                        descriptor.returnType(),
                        safe
                );
            }
            case PropertyChainNode.SafeMethodCallAccess safeMethodCall -> {
                PropertyChainNode.MethodCallAccess asMethodCall =
                        new PropertyChainNode.MethodCallAccess(safeMethodCall.name(), safeMethodCall.arguments());
                MethodDescriptor descriptor = resolveMethodDescriptor(metadata, asMethodCall, model, typeHintCatalog);
                yield new ExecutablePropertyChain.ExecutableMethodInvoke(
                        safeMethodCall.name(),
                        descriptor.handle(),
                        argumentNodes,
                        descriptor.parameterTypes(),
                        descriptor.returnType(),
                        true
                );
            }
        };
    }

    private ExecutablePropertyChain.ExecutableAccess buildReflectiveAccess(
            PropertyChainNode.MemberAccess access,
            List<ExecutableNode> argumentNodes,
            boolean safe) {
        return switch (access) {
            case PropertyChainNode.PropertyAccess propertyAccess ->
                    new ExecutablePropertyChain.ReflectivePropertyAccess(propertyAccess.name(), safe);
            case PropertyChainNode.SafePropertyAccess safePropertyAccess ->
                    new ExecutablePropertyChain.ReflectivePropertyAccess(safePropertyAccess.name(), true);
            case PropertyChainNode.MethodCallAccess methodCall ->
                    new ExecutablePropertyChain.ReflectiveMethodInvoke(methodCall.name(), argumentNodes, safe);
            case PropertyChainNode.SafeMethodCallAccess safeMethodCall ->
                    new ExecutablePropertyChain.ReflectiveMethodInvoke(safeMethodCall.name(), argumentNodes, true);
        };
    }

    private ResolvedType nextType(
            PropertyChainNode.MemberAccess access,
            SemanticModel model,
            TypeMetadata metadata,
            TypeHintCatalog typeHintCatalog) {
        return switch (access) {
            case PropertyChainNode.PropertyAccess propertyAccess -> {
                var descriptor = metadata.properties().get(propertyAccess.name());
                if (descriptor == null) {
                    throw new IllegalStateException("missing property metadata for '" + propertyAccess.name()
                                                    + "' on " + metadata.javaClass().getName());
                }
                yield descriptor.resolvedType();
            }
            case PropertyChainNode.SafePropertyAccess safePropertyAccess -> {
                var descriptor = metadata.properties().get(safePropertyAccess.name());
                if (descriptor == null) {
                    throw new IllegalStateException("missing property metadata for '" + safePropertyAccess.name()
                                                    + "' on " + metadata.javaClass().getName());
                }
                yield descriptor.resolvedType();
            }
            case PropertyChainNode.MethodCallAccess methodCall ->
                    resolveMethodDescriptor(metadata, methodCall, model, typeHintCatalog).returnType();
            case PropertyChainNode.SafeMethodCallAccess safeMethodCall ->
                    resolveMethodDescriptor(metadata,
                            new PropertyChainNode.MethodCallAccess(safeMethodCall.name(), safeMethodCall.arguments()),
                            model, typeHintCatalog).returnType();
        };
    }

    private MethodDescriptor resolveMethodDescriptor(
            TypeMetadata metadata,
            PropertyChainNode.MethodCallAccess methodCall,
            SemanticModel model,
            TypeHintCatalog typeHintCatalog) {
        List<MethodDescriptor> candidates = metadata.methods().get(methodCall.name());
        if (candidates == null || candidates.isEmpty()) {
            throw new IllegalStateException("missing method metadata for '" + methodCall.name()
                                            + "' on " + metadata.javaClass().getName());
        }

        List<MethodDescriptor> arityMatches = candidates.stream()
                .filter(candidate -> candidate.arity() == methodCall.arguments().size())
                .toList();
        if (arityMatches.isEmpty()) {
            throw new IllegalStateException("missing method overload for '" + methodCall.name()
                                            + "' with arity " + methodCall.arguments().size()
                                            + " on " + metadata.javaClass().getName());
        }

        List<ResolvedType> argumentTypes = methodCall.arguments().stream()
                .map(argument -> model.findResolvedType(argument.nodeId()).orElse(UnknownType.INSTANCE))
                .toList();

        MethodDescriptor match = null;
        for (MethodDescriptor candidate : arityMatches) {
            if (!matchesMethodArguments(candidate, argumentTypes, typeHintCatalog)) {
                continue;
            }
            if (match != null) {
                throw new IllegalStateException("ambiguous method metadata for '" + methodCall.name()
                                                + "' on " + metadata.javaClass().getName());
            }
            match = candidate;
        }
        if (match == null) {
            throw new IllegalStateException("missing compatible method overload for '" + methodCall.name()
                                            + "' on " + metadata.javaClass().getName());
        }
        return match;
    }

    private boolean matchesMethodArguments(
            MethodDescriptor descriptor,
            List<ResolvedType> argumentTypes,
            TypeHintCatalog typeHintCatalog) {
        for (int index = 0; index < argumentTypes.size(); index++) {
            ResolvedType actualType = argumentTypes.get(index);
            ResolvedType expectedType = resolveJavaType(descriptor.parameterTypes().get(index), typeHintCatalog);
            if (actualType != UnknownType.INSTANCE
                && expectedType != UnknownType.INSTANCE
                && !actualType.equals(expectedType)) {
                return false;
            }
        }
        return true;
    }

    private ResolvedType resolveRootType(
            SymbolRef root,
            SemanticModel model,
            ExternalSymbolCatalog externalSymbolCatalog) {
        if (root.kind() == SymbolKind.EXTERNAL) {
            ExternalSymbolDescriptor descriptor = externalSymbolCatalog.findOrNull(root.name());
            return descriptor == null ? UnknownType.INSTANCE : descriptor.declaredType();
        }
        return resolveInternalSymbolType(root, model);
    }

    private ResolvedType resolveInternalSymbolType(SymbolRef root, SemanticModel model) {
        List<AssignmentNode> assignments = model.assignmentUsages().get(root);
        if (assignments == null || assignments.isEmpty()) {
            return UnknownType.INSTANCE;
        }
        ResolvedType resolvedType = UnknownType.INSTANCE;
        for (AssignmentNode assignment : assignments) {
            if (assignment instanceof DestructuringAssignmentNode) {
                return UnknownType.INSTANCE;
            }
            resolvedType = ResolvedTypes.merge(
                    resolvedType,
                    model.findResolvedType(assignment.nodeId()).orElse(UnknownType.INSTANCE)
            );
        }
        return resolvedType;
    }

    private ResolvedType resolveJavaType(Class<?> javaType, TypeHintCatalog typeHintCatalog) {
        return typeHintCatalog.isRegistered(javaType)
                ? new ObjectType(javaType)
                : ResolvedTypes.fromJavaType(javaType);
    }

    private ExecutableNode buildLiteral(LiteralNode lit, SemanticModel model) {
        String text = lit.value();
        return switch (text) {
            case "currDate" -> new ExecutableDynamicLiteral(DynamicInstant.CURR_DATE);
            case "currTime" -> new ExecutableDynamicLiteral(DynamicInstant.CURR_TIME);
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
        if (resolvedType == NullType.INSTANCE) return null;
        if (resolvedType == ScalarType.NUMBER) return new BigDecimal(text);
        if (resolvedType == ScalarType.BOOLEAN) return Boolean.parseBoolean(text);
        if (resolvedType == ScalarType.STRING) return unquote(text);
        if (resolvedType == ScalarType.DATE) return LocalDate.parse(text);
        if (resolvedType == ScalarType.TIME) return LocalTime.parse(text);
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

    private ExecutableNode buildFunctionCall(
            FunctionCallNode f,
            SemanticModel model,
            RuntimeServices runtimeServices,
            ExternalSymbolCatalog externalSymbolCatalog,
            TypeHintCatalog typeHintCatalog) {
        ResolvedFunctionBinding binding = model.findFunctionBinding(f.nodeId())
                .orElseThrow(() -> new IllegalStateException(
                        "missing function binding for '" + f.functionName() + "'"));
        List<ExecutableNode> arguments = f.arguments().stream()
                .map(arg -> buildNode(arg, model, runtimeServices, externalSymbolCatalog, typeHintCatalog))
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
            case ExecutableLiteral ignored -> true;
            case ExecutableFunctionCall f -> f.isFolded();
            case ExecutableVectorLiteral v -> v.isFolded();
            default -> false;
        };
    }

    private Object constantValue(ExecutableNode node) {
        return switch (node) {
            case ExecutableLiteral lit -> lit.precomputed();
            case ExecutableFunctionCall f -> f.foldedResult();
            case ExecutableVectorLiteral v -> v.foldedValue();
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
