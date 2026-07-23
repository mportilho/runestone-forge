package com.runestone.expeval_mk3.internal.semantics;

import com.runestone.expeval_mk3.api.CollectionType;
import com.runestone.expeval_mk3.api.ExpressionEnvironment;
import com.runestone.expeval_mk3.api.ExpressionType;
import com.runestone.expeval_mk3.api.ExternalSymbol;
import com.runestone.expeval_mk3.api.MapType;
import com.runestone.expeval_mk3.api.ObjectType;
import com.runestone.expeval_mk3.api.RuntimeNullability;
import com.runestone.expeval_mk3.api.ScalarType;
import com.runestone.expeval_mk3.internal.ast.BigIntegerLiteralValue;
import com.runestone.expeval_mk3.internal.ast.BinaryOperationNode;
import com.runestone.expeval_mk3.internal.ast.BinaryOperator;
import com.runestone.expeval_mk3.internal.ast.BooleanLiteralValue;
import com.runestone.expeval_mk3.internal.ast.CollectionLiteralNode;
import com.runestone.expeval_mk3.internal.ast.DateLiteralValue;
import com.runestone.expeval_mk3.internal.ast.DecimalLiteralValue;
import com.runestone.expeval_mk3.internal.ast.ExpressionFileNode;
import com.runestone.expeval_mk3.internal.ast.ExpressionNode;
import com.runestone.expeval_mk3.internal.ast.GroupedExpressionNode;
import com.runestone.expeval_mk3.internal.ast.IdentifierNode;
import com.runestone.expeval_mk3.internal.ast.LiteralNode;
import com.runestone.expeval_mk3.internal.ast.LiteralValue;
import com.runestone.expeval_mk3.internal.ast.LocalDateTimeLiteralValue;
import com.runestone.expeval_mk3.internal.ast.LongLiteralValue;
import com.runestone.expeval_mk3.internal.ast.NodeId;
import com.runestone.expeval_mk3.internal.ast.OffsetDateTimeLiteralValue;
import com.runestone.expeval_mk3.internal.ast.StringLiteralValue;
import com.runestone.expeval_mk3.internal.ast.TimeLiteralValue;
import com.runestone.expeval_mk3.internal.diagnostics.DiagnosticCategory;
import com.runestone.expeval_mk3.internal.diagnostics.DiagnosticCode;
import com.runestone.expeval_mk3.internal.diagnostics.ExpressionDiagnostic;
import com.runestone.expeval_mk3.internal.source.SourceSpan;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class SemanticResolver {

    public SemanticResolutionResult resolve(ExpressionFileNode ast, ExpressionEnvironment environment) {
        Objects.requireNonNull(ast, "ast");
        Objects.requireNonNull(environment, "environment");
        return new ResolutionSession(environment).resolve(ast);
    }

    private static final class ResolutionSession {

        private final ExpressionEnvironment environment;
        private final List<ExpressionDiagnostic> diagnostics = new ArrayList<>();
        private final Map<NodeId, ExpressionType> resolvedTypes = new HashMap<>();
        private final Map<NodeId, RuntimeNullability> nullability = new HashMap<>();
        private final Map<NodeId, Object> preparedValues = new HashMap<>();
        private final Map<NodeId, CollectionShape> collectionShapes = new HashMap<>();
        private final Map<NodeId, SymbolBinding> symbolBindings = new HashMap<>();
        private final Map<NodeId, ExpressionType> equalityOperandTypes = new HashMap<>();
        private final Map<String, SymbolBinding> externalBindings;
        private final FrameLayout frameLayout;

        private ResolutionSession(ExpressionEnvironment environment) {
            this.environment = environment;
            externalBindings = buildExternalBindings(environment);
            frameLayout = new FrameLayout(List.copyOf(externalBindings.values()), externalBindings.size());
        }

        private SemanticResolutionResult resolve(ExpressionFileNode ast) {
            if (!ast.assignments().isEmpty()) {
                diagnostic(
                        DiagnosticCode.SEMANTIC_UNSUPPORTED_EXPRESSION,
                        "Assignments are not supported by this compilation slice",
                        ast.assignments().getFirst().sourceSpan());
            }
            if (ast.resultExpression().isEmpty()) {
                if (ast.assignments().isEmpty()) {
                    diagnostic(DiagnosticCode.SEMANTIC_EMPTY_EXPRESSION, "Expression file is empty", ast.sourceSpan());
                }
            } else {
                ExpressionNode result = ast.resultExpression().orElseThrow();
                Resolution resolution = resolveExpression(result, null);
                if (resolution.pending()) {
                    emptyCollectionDiagnostic(resolution.pendingSpan());
                }
            }
            if (!diagnostics.isEmpty()) {
                return new SemanticResolutionFailure(diagnostics);
            }
            return new SemanticResolutionSuccess(new SemanticModel(
                    ast,
                    resolvedTypes,
                    nullability,
                    preparedValues,
                    collectionShapes,
                    symbolBindings,
                    equalityOperandTypes,
                    frameLayout), List.of());
        }

        private Resolution resolveExpression(ExpressionNode expression, ExpressionType expectedType) {
            if (expression instanceof LiteralNode literal) {
                return resolveLiteral(literal);
            }
            if (expression instanceof CollectionLiteralNode collection) {
                return resolveCollection(collection, expectedType);
            }
            if (expression instanceof BinaryOperationNode binary) {
                return resolveBinary(binary);
            }
            if (expression instanceof IdentifierNode identifier) {
                return resolveIdentifier(identifier);
            }
            if (expression instanceof GroupedExpressionNode grouped) {
                Resolution resolution = resolveExpression(grouped.expression(), expectedType);
                if (resolution.known()) {
                    recordValue(grouped.id(), resolution.type());
                }
                return resolution;
            }
            diagnostic(
                    DiagnosticCode.SEMANTIC_UNSUPPORTED_EXPRESSION,
                    "Expression construct is not supported by this compilation slice",
                    expression.sourceSpan());
            return Resolution.invalidResolution();
        }

        private Resolution resolveLiteral(LiteralNode literal) {
            LiteralResolution resolution = literalResolution(literal.value());
            recordValue(literal.id(), resolution.type());
            preparedValues.put(literal.id(), resolution.value());
            return Resolution.known(resolution.type());
        }

        private LiteralResolution literalResolution(LiteralValue value) {
            return switch (value) {
                case LongLiteralValue literal -> new LiteralResolution(
                        ScalarType.NUMBER, BigDecimal.valueOf(literal.value()));
                case BigIntegerLiteralValue literal -> new LiteralResolution(
                        ScalarType.NUMBER, new BigDecimal(literal.value()));
                case DecimalLiteralValue literal -> new LiteralResolution(ScalarType.NUMBER, literal.value());
                case BooleanLiteralValue literal -> new LiteralResolution(ScalarType.BOOLEAN, literal.value());
                case StringLiteralValue literal -> new LiteralResolution(ScalarType.STRING, literal.value());
                case DateLiteralValue literal -> new LiteralResolution(ScalarType.DATE, literal.value());
                case TimeLiteralValue literal -> new LiteralResolution(ScalarType.TIME, literal.value());
                case LocalDateTimeLiteralValue literal -> new LiteralResolution(ScalarType.DATETIME, literal.value());
                case OffsetDateTimeLiteralValue literal -> new LiteralResolution(
                        ScalarType.DATETIME,
                        environment.normalizeOffsetDateTimeLiteral(literal.value()).normalizedLocalDateTime());
            };
        }

        private Resolution resolveCollection(CollectionLiteralNode collection, ExpressionType expectedType) {
            if (collection.elements().size() > environment.maxMaterializedSize()) {
                diagnostic(
                        DiagnosticCode.SEMANTIC_MATERIALIZATION_LIMIT_EXCEEDED,
                        "Collection literal exceeds maxMaterializedSize " + environment.maxMaterializedSize(),
                        collection.sourceSpan());
                return Resolution.invalidResolution();
            }

            ExpressionType expectedElementType = null;
            if (expectedType instanceof CollectionType expectedCollection) {
                expectedElementType = expectedCollection.elementType();
            } else if (expectedType != null) {
                diagnostic(
                        DiagnosticCode.SEMANTIC_COLLECTION_ELEMENT_TYPE_MISMATCH,
                        "Collection literal is not compatible with expected type " + expectedType,
                        collection.sourceSpan());
                return Resolution.invalidResolution();
            }

            if (collection.elements().isEmpty()) {
                collectionShapes.put(collection.id(), new CollectionShape(0));
                if (expectedElementType == null) {
                    return Resolution.pendingResolution(collection.sourceSpan());
                }
                CollectionType type = new CollectionType(expectedElementType);
                recordValue(collection.id(), type);
                return Resolution.known(type);
            }

            ExpressionType elementType = expectedElementType;
            List<ExpressionNode> pendingElements = new ArrayList<>();
            for (ExpressionNode element : collection.elements()) {
                Resolution elementResolution = resolveExpression(element, elementType);
                if (elementResolution.invalid()) {
                    continue;
                }
                if (elementResolution.pending()) {
                    pendingElements.add(element);
                    continue;
                }
                if (elementType == null) {
                    elementType = elementResolution.type();
                } else if (!elementType.equals(elementResolution.type())) {
                    diagnostic(
                            DiagnosticCode.SEMANTIC_COLLECTION_ELEMENT_TYPE_MISMATCH,
                            "Collection elements must have one known type",
                            element.sourceSpan());
                }
            }

            if (elementType == null) {
                if (pendingElements.isEmpty()) {
                    return Resolution.invalidResolution();
                }
                return Resolution.pendingResolution(pendingElements.getFirst().sourceSpan());
            }
            for (ExpressionNode pendingElement : pendingElements) {
                resolveExpression(pendingElement, elementType);
            }
            CollectionType collectionType = new CollectionType(elementType);
            recordValue(collection.id(), collectionType);
            collectionShapes.put(collection.id(), new CollectionShape(collection.elements().size()));
            return Resolution.known(collectionType);
        }

        private Resolution resolveBinary(BinaryOperationNode binary) {
            if (binary.operator() != BinaryOperator.EQUAL && binary.operator() != BinaryOperator.NOT_EQUAL) {
                diagnostic(
                        DiagnosticCode.SEMANTIC_UNSUPPORTED_EXPRESSION,
                        "Only equality operators are supported by this compilation slice",
                        binary.operatorSpan());
                return Resolution.invalidResolution();
            }

            Resolution left = resolveExpression(binary.left(), null);
            Resolution right = resolveExpression(binary.right(), left.known() ? left.type() : null);
            if (left.pending() && right.known()) {
                left = resolveExpression(binary.left(), right.type());
            } else if (right.pending() && left.known()) {
                right = resolveExpression(binary.right(), left.type());
            }
            if (left.pending()) {
                emptyCollectionDiagnostic(left.pendingSpan());
            }
            if (right.pending()) {
                emptyCollectionDiagnostic(right.pendingSpan());
            }
            if (left.invalid() || right.invalid() || left.pending() || right.pending()) {
                return Resolution.invalidResolution();
            }
            if (!left.type().equals(right.type())) {
                diagnostic(
                        DiagnosticCode.SEMANTIC_EQUALITY_TYPE_MISMATCH,
                        "Equality operands must have the same known type",
                        binary.operatorSpan());
                return Resolution.invalidResolution();
            }
            if (!supportsStructuralEquality(left.type())) {
                diagnostic(
                        DiagnosticCode.SEMANTIC_OBJECT_EQUALITY_NOT_SUPPORTED,
                        "Nominal object values do not support generic equality",
                        binary.operatorSpan());
                return Resolution.invalidResolution();
            }
            equalityOperandTypes.put(binary.id(), left.type());
            recordValue(binary.id(), ScalarType.BOOLEAN);
            return Resolution.known(ScalarType.BOOLEAN);
        }

        private Resolution resolveIdentifier(IdentifierNode identifier) {
            SymbolBinding binding = externalBindings.get(identifier.name());
            if (binding == null) {
                diagnostic(
                        DiagnosticCode.SEMANTIC_UNKNOWN_SYMBOL,
                        "Unknown external symbol '" + identifier.name() + "'",
                        identifier.sourceSpan());
                return Resolution.invalidResolution();
            }
            symbolBindings.put(identifier.id(), binding);
            recordValue(identifier.id(), binding.symbol().type());
            return Resolution.known(binding.symbol().type());
        }

        private void emptyCollectionDiagnostic(SourceSpan span) {
            diagnostic(
                    DiagnosticCode.SEMANTIC_EMPTY_COLLECTION_REQUIRES_CONTEXT,
                    "Empty collection literal requires a known element type from local context",
                    span);
        }

        private void recordValue(NodeId nodeId, ExpressionType type) {
            resolvedTypes.put(nodeId, type);
            nullability.put(nodeId, RuntimeNullability.NEVER_NULL);
        }

        private void diagnostic(DiagnosticCode code, String message, SourceSpan span) {
            diagnostics.add(new ExpressionDiagnostic(DiagnosticCategory.SEMANTIC, code, message, span));
        }

        private static boolean supportsStructuralEquality(ExpressionType type) {
            if (type instanceof ObjectType) {
                return false;
            }
            if (type instanceof CollectionType collectionType) {
                return supportsStructuralEquality(collectionType.elementType());
            }
            if (type instanceof MapType mapType) {
                return supportsStructuralEquality(mapType.valueType());
            }
            return true;
        }

        private static Map<String, SymbolBinding> buildExternalBindings(ExpressionEnvironment environment) {
            Map<String, SymbolBinding> bindings = new LinkedHashMap<>();
            int slot = 0;
            for (ExternalSymbol symbol : environment.externalSymbols().values()) {
                bindings.put(symbol.name(), new SymbolBinding(symbol, slot++, RuntimeNullability.NEVER_NULL));
            }
            return Map.copyOf(bindings);
        }
    }

    private record LiteralResolution(ExpressionType type, Object value) {
    }

    private record Resolution(ExpressionType type, SourceSpan pendingSpan, boolean pending, boolean invalid) {

        private static Resolution known(ExpressionType type) {
            return new Resolution(Objects.requireNonNull(type, "type"), null, false, false);
        }

        private static Resolution pendingResolution(SourceSpan span) {
            return new Resolution(null, Objects.requireNonNull(span, "span"), true, false);
        }

        private static Resolution invalidResolution() {
            return new Resolution(null, null, false, true);
        }

        private boolean known() {
            return type != null && !pending && !invalid;
        }
    }
}
