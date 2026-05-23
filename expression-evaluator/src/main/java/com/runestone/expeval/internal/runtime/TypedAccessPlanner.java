package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.catalog.MethodDescriptor;
import com.runestone.expeval.catalog.PropertyDescriptor;
import com.runestone.expeval.catalog.TypeHintCatalog;
import com.runestone.expeval.catalog.TypeMetadata;
import com.runestone.expeval.internal.ast.ExpressionNode;
import com.runestone.expeval.internal.ast.PropertyChainNode;
import com.runestone.expeval.types.ObjectType;
import com.runestone.expeval.types.ResolvedType;
import com.runestone.expeval.types.ResolvedTypes;
import com.runestone.expeval.types.UnknownType;

import java.util.List;

final class TypedAccessPlanner {

    private TypedAccessPlanner() {
    }

    static TypedAccessStep build(
            PropertyChainNode.MemberAccess access,
            List<ExecutableNode> argumentNodes,
            SemanticModel model,
            TypeMetadata metadata,
            TypeHintCatalog typeHintCatalog,
            boolean safe) {
        return switch (access) {
            case PropertyChainNode.PropertyAccess propertyAccess -> {
                PropertyDescriptor descriptor = requireProperty(metadata, propertyAccess.name());
                yield new TypedAccessStep(
                        new ExecutablePropertyChain.ExecutableFieldGet(
                                propertyAccess.name(),
                                descriptor.getter(),
                                descriptor.resolvedType(),
                                safe
                        ),
                        descriptor.resolvedType()
                );
            }
            case PropertyChainNode.SafePropertyAccess safePropertyAccess -> {
                PropertyDescriptor descriptor = requireProperty(metadata, safePropertyAccess.name());
                yield new TypedAccessStep(
                        new ExecutablePropertyChain.ExecutableFieldGet(
                                safePropertyAccess.name(),
                                descriptor.getter(),
                                descriptor.resolvedType(),
                                true
                        ),
                        descriptor.resolvedType()
                );
            }
            case PropertyChainNode.MethodCallAccess methodCall -> {
                MethodDescriptor descriptor = resolveMethodDescriptor(metadata, methodCall, model, typeHintCatalog);
                yield new TypedAccessStep(
                        new ExecutablePropertyChain.ExecutableMethodInvoke(
                                methodCall.name(),
                                descriptor.handle(),
                                argumentNodes,
                                descriptor.parameterTypes(),
                                descriptor.returnType(),
                                safe
                        ),
                        descriptor.returnType()
                );
            }
            case PropertyChainNode.SafeMethodCallAccess safeMethodCall -> {
                MethodDescriptor descriptor = resolveMethodDescriptor(
                        metadata,
                        safeMethodCall.name(),
                        safeMethodCall.arguments(),
                        model,
                        typeHintCatalog
                );
                yield new TypedAccessStep(
                        new ExecutablePropertyChain.ExecutableMethodInvoke(
                                safeMethodCall.name(),
                                descriptor.handle(),
                                argumentNodes,
                                descriptor.parameterTypes(),
                                descriptor.returnType(),
                                true
                        ),
                        descriptor.returnType()
                );
            }
            default -> throw new IllegalStateException("Unexpected access type in typed access planner: " + access);
        };
    }

    private static PropertyDescriptor requireProperty(TypeMetadata metadata, String propertyName) {
        PropertyDescriptor descriptor = metadata.properties().get(propertyName);
        if (descriptor == null) {
            throw new IllegalStateException("missing property metadata for '" + propertyName
                                            + "' on " + metadata.javaClass().getName());
        }
        return descriptor;
    }

    private static MethodDescriptor resolveMethodDescriptor(
            TypeMetadata metadata,
            PropertyChainNode.MethodCallAccess methodCall,
            SemanticModel model,
            TypeHintCatalog typeHintCatalog) {
        return resolveMethodDescriptor(
                metadata,
                methodCall.name(),
                methodCall.arguments(),
                model,
                typeHintCatalog
        );
    }

    private static MethodDescriptor resolveMethodDescriptor(
            TypeMetadata metadata,
            String methodName,
            List<ExpressionNode> arguments,
            SemanticModel model,
            TypeHintCatalog typeHintCatalog) {
        List<MethodDescriptor> candidates = metadata.methods().get(methodName);
        if (candidates == null || candidates.isEmpty()) {
            throw new IllegalStateException("missing method metadata for '" + methodName
                                            + "' on " + metadata.javaClass().getName());
        }

        List<MethodDescriptor> arityMatches = candidates.stream()
                .filter(candidate -> candidate.arity() == arguments.size())
                .toList();
        if (arityMatches.isEmpty()) {
            throw new IllegalStateException("missing method overload for '" + methodName
                                            + "' with arity " + arguments.size()
                                            + " on " + metadata.javaClass().getName());
        }

        List<ResolvedType> argumentTypes = arguments.stream()
                .map(argument -> model.findResolvedType(argument.nodeId()).orElse(UnknownType.INSTANCE))
                .toList();

        MethodDescriptor match = null;
        for (MethodDescriptor candidate : arityMatches) {
            if (!matchesMethodArguments(candidate, argumentTypes, typeHintCatalog)) {
                continue;
            }
            if (match != null) {
                throw new IllegalStateException("ambiguous method metadata for '" + methodName
                                                + "' on " + metadata.javaClass().getName());
            }
            match = candidate;
        }
        if (match == null) {
            throw new IllegalStateException("missing compatible method overload for '" + methodName
                                            + "' on " + metadata.javaClass().getName());
        }
        return match;
    }

    private static boolean matchesMethodArguments(
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

    private static ResolvedType resolveJavaType(Class<?> javaType, TypeHintCatalog typeHintCatalog) {
        return typeHintCatalog.isRegistered(javaType)
                ? new ObjectType(javaType)
                : ResolvedTypes.fromJavaType(javaType);
    }

    record TypedAccessStep(
            ExecutablePropertyChain.ExecutableAccess access,
            ResolvedType nextType
    ) {
    }
}
