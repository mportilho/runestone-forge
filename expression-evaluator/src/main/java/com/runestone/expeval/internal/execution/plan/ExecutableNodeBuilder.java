package com.runestone.expeval.internal.execution.plan;

import com.runestone.expeval.catalog.ExternalSymbolCatalog;
import com.runestone.expeval.catalog.TypeHintCatalog;
import com.runestone.expeval.internal.LanguageSymbols;
import com.runestone.expeval.internal.ast.AssignmentNode;
import com.runestone.expeval.internal.ast.BinaryOperationNode;
import com.runestone.expeval.internal.ast.BinaryOperator;
import com.runestone.expeval.internal.ast.ConditionalNode;
import com.runestone.expeval.internal.ast.DestructuringAssignmentNode;
import com.runestone.expeval.internal.ast.ExpressionNode;
import com.runestone.expeval.internal.ast.FunctionCallNode;
import com.runestone.expeval.internal.ast.IdentifierNode;
import com.runestone.expeval.internal.ast.LiteralNode;
import com.runestone.expeval.internal.ast.PostfixOperationNode;
import com.runestone.expeval.internal.ast.PropertyChainNode;
import com.runestone.expeval.internal.ast.SimpleAssignmentNode;
import com.runestone.expeval.internal.ast.TernaryOperationNode;
import com.runestone.expeval.internal.ast.UnaryOperationNode;
import com.runestone.expeval.internal.ast.VectorLiteralNode;
import com.runestone.expeval.internal.runtime.RuntimeServices;
import com.runestone.expeval.internal.semantic.ResolvedFunctionBinding;
import com.runestone.expeval.internal.semantic.SemanticModel;
import com.runestone.expeval.internal.semantic.SymbolRef;

import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

final class ExecutableNodeBuilder {

    private final SemanticModel model;
    private final RuntimeServices runtimeServices;
    private final ExternalSymbolCatalog externalSymbolCatalog;
    private final TypeHintCatalog typeHintCatalog;
    private final MathContext mathContext;
    private final ConstantFoldContext foldContext;

    ExecutableNodeBuilder(
            SemanticModel model,
            RuntimeServices runtimeServices,
            ExternalSymbolCatalog externalSymbolCatalog,
            TypeHintCatalog typeHintCatalog,
            MathContext mathContext,
            ConstantFoldContext foldContext) {
        this.model = model;
        this.runtimeServices = runtimeServices;
        this.externalSymbolCatalog = externalSymbolCatalog;
        this.typeHintCatalog = typeHintCatalog;
        this.mathContext = mathContext;
        this.foldContext = foldContext;
    }

    ExecutableAssignment buildAssignment(AssignmentNode assignment) {
        return switch (assignment) {
            case SimpleAssignmentNode simpleAssignment -> {
                SymbolRef target = model.internalSymbolsByName().get(simpleAssignment.targetName());
                ExecutableNode value = buildNode(simpleAssignment.value());
                if (ConstantNodeValues.isConstant(value)) {
                    foldContext.symbols.put(target, ConstantNodeValues.value(value));
                } else {
                    foldContext.symbols.remove(target);
                }
                yield new ExecutableSimpleAssignment(target, value);
            }
            case DestructuringAssignmentNode destructuringAssignment -> {
                List<SymbolRef> targets = destructuringAssignment.targetNames().stream()
                        .map(name -> model.internalSymbolsByName().get(name))
                        .toList();
                yield new ExecutableDestructuringAssignment(targets, buildNode(destructuringAssignment.value()));
            }
        };
    }

    ExecutableNode buildNode(ExpressionNode node) {
        return switch (node) {
            case LiteralNode literal -> LiteralMaterializer.build(literal, model);
            case IdentifierNode identifier -> buildIdentifier(identifier);
            case PropertyChainNode chain -> buildPropertyChain(chain);
            case FunctionCallNode functionCall -> buildFunctionCall(functionCall);
            case BinaryOperationNode binaryOperation when binaryOperation.operator() == BinaryOperator.NULL_COALESCE ->
                    operatorPlanner().buildNullCoalesce(binaryOperation);
            case BinaryOperationNode binaryOperation when binaryOperation.operator() == BinaryOperator.REGEX_MATCH
                                                    || binaryOperation.operator() == BinaryOperator.REGEX_NOT_MATCH ->
                    operatorPlanner().buildRegexOperation(binaryOperation);
            case BinaryOperationNode binaryOperation -> operatorPlanner().buildBinaryOperation(binaryOperation);
            case TernaryOperationNode ternaryOperation -> operatorPlanner().buildTernaryOperation(ternaryOperation);
            case UnaryOperationNode unaryOperation -> operatorPlanner().buildUnaryOperation(unaryOperation);
            case PostfixOperationNode postfixOperation -> operatorPlanner().buildPostfixOperation(postfixOperation);
            case ConditionalNode conditional -> buildConditional(conditional);
            case VectorLiteralNode vectorLiteral -> buildVectorLiteral(vectorLiteral);
        };
    }

    private OperatorNodePlanner operatorPlanner() {
        return new OperatorNodePlanner(runtimeServices, mathContext, this::buildNode);
    }

    private ExecutableNode buildConditional(ConditionalNode conditional) {
        List<ExecutableNode> conditions = new ArrayList<>();
        List<ExecutableNode> results = new ArrayList<>();
        ExecutableNode elseExpression = null;

        for (int index = 0; index < conditional.conditions().size(); index++) {
            ExecutableNode conditionNode = buildNode(conditional.conditions().get(index));
            if (ConstantNodeValues.isConstant(conditionNode)) {
                if (Boolean.TRUE.equals(ConstantNodeValues.value(conditionNode))) {
                    elseExpression = buildNode(conditional.results().get(index));
                    break;
                }
            } else {
                conditions.add(conditionNode);
                results.add(buildNode(conditional.results().get(index)));
            }
        }

        if (elseExpression == null) {
            elseExpression = buildNode(conditional.elseExpression());
        }
        if (conditions.isEmpty()) {
            return elseExpression;
        }
        if (conditions.size() == 1) {
            return new ExecutableSimpleConditional(conditions.getFirst(), results.getFirst(), elseExpression);
        }
        return new ExecutableConditional(conditions, results, elseExpression);
    }

    private ExecutableNode buildVectorLiteral(VectorLiteralNode vectorLiteral) {
        List<ExecutableNode> elements = vectorLiteral.elements().stream()
                .map(this::buildNode)
                .toList();
        if (elements.stream().allMatch(ConstantNodeValues::isConstant)) {
            List<Object> foldedValues = elements.stream().map(ConstantNodeValues::value).toList();
            return new ExecutableVectorLiteral(elements, foldedValues);
        }
        return new ExecutableVectorLiteral(elements);
    }

    private ExecutableNode buildPropertyChain(PropertyChainNode node) {
        return new PropertyChainPlanner(
                model,
                runtimeServices,
                externalSymbolCatalog,
                typeHintCatalog,
                mathContext,
                foldContext,
                this::buildNode
        ).build(node);
    }

    private ExecutableNode buildIdentifier(IdentifierNode identifier) {
        if (LanguageSymbols.CURRENT_ELEMENT.equals(identifier.name())) {
            return SymbolReadPlanner.currentElement(identifier.sourceSpan());
        }
        SymbolRef ref = model.findSymbol(identifier.nodeId())
                .orElseThrow(() -> new IllegalStateException(
                        "missing symbol for identifier '" + identifier.name() + "'"));
        return SymbolReadPlanner.read(ref, identifier.sourceSpan(), foldContext);
    }

    private ExecutableNode buildFunctionCall(FunctionCallNode functionCall) {
        ResolvedFunctionBinding binding = model.findFunctionBinding(functionCall.nodeId())
                .orElseThrow(() -> new IllegalStateException(
                        "missing function binding for '" + functionCall.functionName() + "'"));
        List<ExecutableNode> arguments = functionCall.arguments().stream()
                .map(this::buildNode)
                .toList();

        return FunctionCallPlanner.build(binding, arguments, runtimeServices);
    }
}
