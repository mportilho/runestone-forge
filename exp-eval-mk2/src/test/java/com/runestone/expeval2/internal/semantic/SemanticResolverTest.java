package com.runestone.expeval2.internal.semantic;

import com.runestone.expeval2.environment.ExpressionEnvironment;
import com.runestone.expeval2.environment.ExpressionEnvironmentBuilder;
import com.runestone.expeval2.internal.ast.AssignmentNode;
import com.runestone.expeval2.internal.ast.BinaryOperationNode;
import com.runestone.expeval2.internal.ast.ExpressionFileNode;
import com.runestone.expeval2.internal.ast.ExpressionNode;
import com.runestone.expeval2.internal.ast.FunctionCallNode;
import com.runestone.expeval2.internal.ast.IdentifierNode;
import com.runestone.expeval2.internal.runtime.CompiledExpression;
import com.runestone.expeval2.internal.runtime.ExpressionCompiler;
import com.runestone.expeval2.internal.grammar.ExpressionResultType;
import com.runestone.expeval2.internal.runtime.SemanticModel;
import com.runestone.expeval2.internal.runtime.SymbolRef;
import com.runestone.expeval2.types.ScalarType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

class SemanticResolverTest {

    private final ExpressionCompiler compiler = new ExpressionCompiler();

    @Test
    void shouldResolveTypesAndBuildSymbolIndexesForAssignmentsAndComparisons() {
        ExpressionEnvironment environment = new ExpressionEnvironmentBuilder()
            .registerExternalSymbol("principal", BigDecimal.ZERO, true)
            .registerExternalSymbol("rate", BigDecimal.ZERO, true)
            .registerExternalSymbol("limit", BigDecimal.ZERO, true)
            .build();

        CompiledExpression compiled = compiler.compile(
            "fee = principal + principal * rate; fee > limit",
            ExpressionResultType.LOGICAL,
            environment
        );
        SemanticModel semanticModel = compiled.semanticModel();
        ExpressionFileNode ast = semanticModel.ast();

        BinaryOperationNode comparison = (BinaryOperationNode) ast.resultExpression();
        BinaryOperationNode addition = find(ast, BinaryOperationNode.class, node -> node.operator().name().equals("ADD"));

        assertThat(semanticModel.findResolvedType(ast.resultExpression().nodeId())).contains(ScalarType.BOOLEAN);
        assertThat(semanticModel.findResolvedType(comparison.left().nodeId())).contains(ScalarType.NUMBER);
        assertThat(semanticModel.findResolvedType(addition.nodeId())).contains(ScalarType.NUMBER);

        SymbolRef principal = semanticModel.externalSymbolsByName().get("principal");
        SymbolRef rate = semanticModel.externalSymbolsByName().get("rate");
        SymbolRef limit = semanticModel.externalSymbolsByName().get("limit");
        SymbolRef fee = semanticModel.internalSymbolsByName().get("fee");

        assertThat(principal).isNotNull();
        assertThat(rate).isNotNull();
        assertThat(limit).isNotNull();
        assertThat(fee).isNotNull();

        assertThat(semanticModel.identifierUsages().get(principal)).hasSize(2);
        assertThat(semanticModel.identifierUsages().get(rate)).singleElement().isInstanceOf(IdentifierNode.class);
        assertThat(semanticModel.identifierUsages().get(limit)).singleElement().isInstanceOf(IdentifierNode.class);
        assertThat(semanticModel.assignmentUsages().get(fee)).singleElement().isInstanceOf(AssignmentNode.class);
    }

    @Test
    void shouldResolveStableFunctionBindingsInsideSemanticModel() {
        ExpressionEnvironment environment = new ExpressionEnvironmentBuilder()
            .registerStaticProvider(FunctionFixtures.class)
            .registerExternalSymbol("principal", BigDecimal.TEN, true)
            .build();

        CompiledExpression compiled = compiler.compile(
            "bonus(principal) + 1",
            ExpressionResultType.MATH,
            environment
        );
        SemanticModel semanticModel = compiled.semanticModel();
        FunctionCallNode functionCallNode = find(semanticModel.ast().resultExpression(), FunctionCallNode.class, ignored -> true);

        assertThat(semanticModel.findResolvedType(functionCallNode.nodeId())).contains(ScalarType.NUMBER);
        assertThat(semanticModel.functionBindings()).containsKey(functionCallNode.nodeId());
        assertThat(semanticModel.functionBindings().get(functionCallNode.nodeId()).descriptor().name()).isEqualTo("bonus");
        assertThat(semanticModel.functionBindings().get(functionCallNode.nodeId()).functionRef().arity()).isEqualTo(1);
    }

    private static <T extends ExpressionNode> T find(ExpressionFileNode file, Class<T> type, Predicate<T> predicate) {
        return collect(file).stream()
            .filter(type::isInstance)
            .map(type::cast)
            .filter(predicate)
            .findFirst()
            .orElseThrow();
    }

    private static <T extends ExpressionNode> T find(ExpressionNode root, Class<T> type, Predicate<T> predicate) {
        return collect(root).stream()
            .filter(type::isInstance)
            .map(type::cast)
            .filter(predicate)
            .findFirst()
            .orElseThrow();
    }

    private static List<ExpressionNode> collect(ExpressionFileNode file) {
        List<ExpressionNode> nodes = new ArrayList<>();
        file.assignments().forEach(assignment -> {
            switch (assignment) {
                case com.runestone.expeval2.internal.ast.SimpleAssignmentNode simpleAssignment -> collect(simpleAssignment.value(), nodes);
                case com.runestone.expeval2.internal.ast.DestructuringAssignmentNode destructuringAssignment -> collect(destructuringAssignment.value(), nodes);
            }
        });
        collect(file.resultExpression(), nodes);
        return nodes;
    }

    private static List<ExpressionNode> collect(ExpressionNode root) {
        List<ExpressionNode> nodes = new ArrayList<>();
        collect(root, nodes);
        return nodes;
    }

    private static void collect(ExpressionNode node, List<ExpressionNode> nodes) {
        nodes.add(node);
        switch (node) {
            case BinaryOperationNode binaryOperationNode -> {
                collect(binaryOperationNode.left(), nodes);
                collect(binaryOperationNode.right(), nodes);
            }
            case com.runestone.expeval2.internal.ast.UnaryOperationNode unaryOperationNode -> collect(unaryOperationNode.operand(), nodes);
            case com.runestone.expeval2.internal.ast.PostfixOperationNode postfixOperationNode -> collect(postfixOperationNode.operand(), nodes);
            case com.runestone.expeval2.internal.ast.ConditionalNode conditionalNode -> {
                conditionalNode.conditions().forEach(condition -> collect(condition, nodes));
                conditionalNode.results().forEach(result -> collect(result, nodes));
                collect(conditionalNode.elseExpression(), nodes);
            }
            case FunctionCallNode functionCallNode -> functionCallNode.arguments().forEach(argument -> collect(argument, nodes));
            case com.runestone.expeval2.internal.ast.VectorLiteralNode vectorLiteralNode -> vectorLiteralNode.elements().forEach(element -> collect(element, nodes));
            case IdentifierNode ignored -> {
            }
            case com.runestone.expeval2.internal.ast.LiteralNode ignoredLiteral -> {
            }
        }
    }

    public static final class FunctionFixtures {

        private FunctionFixtures() {
        }

        public static BigDecimal bonus(BigDecimal principal) {
            return principal.multiply(BigDecimal.valueOf(0.10));
        }
    }
}
