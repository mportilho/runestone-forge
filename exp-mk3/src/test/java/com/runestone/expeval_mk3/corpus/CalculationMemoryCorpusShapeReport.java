package com.runestone.expeval_mk3.corpus;

import com.runestone.expeval_mk3.api.ExpressionEnvironment;
import com.runestone.expeval_mk3.api.FunctionDescriptor;
import com.runestone.expeval_mk3.internal.ast.BetweenNode;
import com.runestone.expeval_mk3.internal.ast.BinaryOperationNode;
import com.runestone.expeval_mk3.internal.ast.CallArgument;
import com.runestone.expeval_mk3.internal.ast.CallNavigationLink;
import com.runestone.expeval_mk3.internal.ast.CollectionLiteralNode;
import com.runestone.expeval_mk3.internal.ast.ConditionalNode;
import com.runestone.expeval_mk3.internal.ast.CurrentItemNode;
import com.runestone.expeval_mk3.internal.ast.CurrentTemporalValueNode;
import com.runestone.expeval_mk3.internal.ast.ExpressionCallArgument;
import com.runestone.expeval_mk3.internal.ast.ExpressionFileNode;
import com.runestone.expeval_mk3.internal.ast.ExpressionNode;
import com.runestone.expeval_mk3.internal.ast.FilterNavigationLink;
import com.runestone.expeval_mk3.internal.ast.FunctionCallNode;
import com.runestone.expeval_mk3.internal.ast.GroupedExpressionNode;
import com.runestone.expeval_mk3.internal.ast.IdentifierNode;
import com.runestone.expeval_mk3.internal.ast.LiteralNode;
import com.runestone.expeval_mk3.internal.ast.MembershipNode;
import com.runestone.expeval_mk3.internal.ast.NavigationChainNode;
import com.runestone.expeval_mk3.internal.ast.NavigationLink;
import com.runestone.expeval_mk3.internal.ast.NullCoalesceNode;
import com.runestone.expeval_mk3.internal.ast.PostfixOperationNode;
import com.runestone.expeval_mk3.internal.ast.SemanticAstBuildSuccess;
import com.runestone.expeval_mk3.internal.ast.SemanticAstBuilder;
import com.runestone.expeval_mk3.internal.ast.UnaryOperationNode;
import com.runestone.expeval_mk3.internal.parser.ExpressionParser;
import com.runestone.expeval_mk3.internal.parser.ParseSuccess;
import com.runestone.expeval_mk3.internal.memory.CalculationMemorySchema;
import com.runestone.expeval_mk3.internal.memory.VariableMemorySchema;
import com.runestone.expeval_mk3.internal.plan.ExecutionPlan;
import com.runestone.expeval_mk3.internal.plan.OraclePlanFixtures;
import com.runestone.expeval_mk3.internal.semantics.CollectionOperationBinding;
import com.runestone.expeval_mk3.internal.semantics.RegisteredMethodNavigationBinding;
import com.runestone.expeval_mk3.internal.semantics.RegisteredPropertyNavigationBinding;
import com.runestone.expeval_mk3.internal.semantics.SemanticModel;
import com.runestone.expeval_mk3.internal.semantics.SemanticResolutionSuccess;
import com.runestone.expeval_mk3.internal.semantics.SemanticResolver;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Reports semantic frame and observable calculation-point sizes for valid corpus expressions. */
public final class CalculationMemoryCorpusShapeReport {

    private static final String SCALAR_ASSERTION_OWNER =
            "com.runestone.expeval_mk3.api.AssertionBuiltInFunctions";

    private CalculationMemoryCorpusShapeReport() {
    }

    public static void main(String[] args) throws ReflectiveOperationException {
        List<Shape> shapes = new ArrayList<>();
        for (ExpressionCase expressionCase : ExpressionCaseLoader.loadAll()) {
            if (expressionCase.kind() != CaseKind.VALID
                    || (expressionCase.phase() != CasePhase.SEMANTIC
                    && expressionCase.phase() != CasePhase.RUNTIME)) {
                continue;
            }
            ExpressionEnvironment environment = ExpressionCaseEnvironments.environment(expressionCase);
            ParseSuccess parse = (ParseSuccess) new ExpressionParser().parse(expressionCase.source());
            ExpressionFileNode ast = ((SemanticAstBuildSuccess) new SemanticAstBuilder().build(parse)).file();
            SemanticModel model = ((SemanticResolutionSuccess) new SemanticResolver().resolve(ast, environment)).model();
            int calculationCount = ast.assignments().stream()
                    .mapToInt(assignment -> count(assignment.expression(), model))
                    .sum() + ast.resultExpression().map(expression -> count(expression, model)).orElse(0);
            ReachRange reach = ast.assignments().stream()
                    .map(assignment -> reach(assignment.expression(), model))
                    .reduce(ReachRange.ZERO, ReachRange::plus)
                    .plus(ast.resultExpression().map(expression -> reach(expression, model)).orElse(ReachRange.ZERO));
            PlanShape planShape = planShape(model, environment);
            shapes.add(new Shape(
                    expressionCase.id(), planShape.frameSlots, planShape.variableCount, calculationCount,
                    reach.minimum, reach.maximum));
        }

        System.out.println("caseId,executionFrameSlots,participatingVariables,observableCalculationPoints,minReached,maxReached");
        shapes.forEach(shape -> System.out.printf(
                "%s,%d,%d,%d,%d,%d%n", shape.caseId, shape.frameSlots, shape.variableCount,
                shape.calculationPoints, shape.minimumReached, shape.maximumReached));
        System.out.printf(
                "summary,count=%d,frame[p50=%d,p95=%d,max=%d],variables[p50=%d,p95=%d,max=%d],calculations[p50=%d,p95=%d,max=%d]%n",
                shapes.size(), percentile(shapes, true, 0.50), percentile(shapes, true, 0.95), maximum(shapes, true),
                variablePercentile(shapes, 0.50), variablePercentile(shapes, 0.95), variableMaximum(shapes),
                percentile(shapes, false, 0.50), percentile(shapes, false, 0.95), maximum(shapes, false));
    }

    private static ReachRange reach(ExpressionNode node, SemanticModel model) {
        return switch (node) {
            case LiteralNode ignored -> ReachRange.ZERO;
            case IdentifierNode ignored -> ReachRange.ZERO;
            case CurrentItemNode ignored -> ReachRange.ZERO;
            case CurrentTemporalValueNode ignored -> ReachRange.ONE;
            case GroupedExpressionNode grouped -> reach(grouped.expression(), model);
            case CollectionLiteralNode collection -> collection.elements().stream()
                    .map(element -> reach(element, model)).reduce(ReachRange.ZERO, ReachRange::plus);
            case BinaryOperationNode binary -> switch (binary.operator()) {
                case LOGICAL_AND, LOGICAL_OR -> reach(binary.left(), model)
                        .plusOptional(reach(binary.right(), model));
                default -> reach(binary.left(), model).plus(reach(binary.right(), model));
            };
            case UnaryOperationNode unary -> reach(unary.operand(), model);
            case PostfixOperationNode postfix -> reach(postfix.operand(), model);
            case BetweenNode between -> reach(between.value(), model)
                    .plus(reach(between.lowerBound(), model))
                    .plus(reach(between.upperBound(), model));
            case MembershipNode membership -> reach(membership.element(), model)
                    .plus(reach(membership.collection(), model));
            case NullCoalesceNode coalesce -> reachCoalesce(coalesce, model);
            case ConditionalNode conditional -> reachConditional(conditional, model, 0);
            case FunctionCallNode function -> reachArguments(function.arguments(), model)
                    .plus(isElidedAssertion(function, model) ? ReachRange.ZERO : ReachRange.ONE);
            case NavigationChainNode navigation -> reachNavigation(navigation, model);
        };
    }

    private static ReachRange reachCoalesce(NullCoalesceNode coalesce, SemanticModel model) {
        ReachRange first = reach(coalesce.operands().getFirst(), model);
        int maximum = first.maximum;
        for (int index = 1; index < coalesce.operands().size(); index++) {
            maximum += reach(coalesce.operands().get(index), model).maximum;
        }
        return new ReachRange(first.minimum, maximum);
    }

    private static ReachRange reachConditional(ConditionalNode conditional, SemanticModel model, int index) {
        if (index == conditional.branches().size()) {
            return reach(conditional.elseExpression(), model);
        }
        var branch = conditional.branches().get(index);
        ReachRange condition = reach(branch.condition(), model);
        ReachRange consequence = reach(branch.consequence(), model);
        ReachRange remaining = reachConditional(conditional, model, index + 1);
        return new ReachRange(
                condition.minimum + Math.min(consequence.minimum, remaining.minimum),
                condition.maximum + Math.max(consequence.maximum, remaining.maximum));
    }

    private static ReachRange reachNavigation(NavigationChainNode navigation, SemanticModel model) {
        ReachRange result = reach(navigation.receiver(), model);
        boolean optional = false;
        for (NavigationLink link : navigation.links()) {
            Object binding = model.navigationBindings().get(link.id());
            ReachRange current = ReachRange.ZERO;
            if (binding instanceof RegisteredPropertyNavigationBinding) {
                current = ReachRange.ONE;
            } else if (binding instanceof RegisteredMethodNavigationBinding) {
                current = reachArguments(((CallNavigationLink) link).arguments(), model).plus(ReachRange.ONE);
            }
            if (optional || link.safe()) {
                result = result.plusOptional(current);
                optional = true;
            } else {
                result = result.plus(current);
            }
        }
        return result;
    }

    private static ReachRange reachArguments(List<CallArgument> arguments, SemanticModel model) {
        ReachRange result = ReachRange.ZERO;
        for (CallArgument argument : arguments) {
            if (argument instanceof ExpressionCallArgument expressionArgument) {
                result = result.plus(reach(expressionArgument.expression(), model));
            }
        }
        return result;
    }

    private static PlanShape planShape(SemanticModel model, ExpressionEnvironment environment)
            throws ReflectiveOperationException {
        ExecutionPlan plan = OraclePlanFixtures.buildOptimized(model, environment);
        var frameTemplate = ExecutionPlan.class.getDeclaredField("frameTemplate");
        frameTemplate.setAccessible(true);
        var memorySchema = ExecutionPlan.class.getDeclaredField("fullCalculationMemorySchema");
        memorySchema.setAccessible(true);
        var variableSchema = CalculationMemorySchema.class.getDeclaredField("variableSchema");
        variableSchema.setAccessible(true);
        var keys = VariableMemorySchema.class.getDeclaredField("keys");
        keys.setAccessible(true);
        int variableCount = ((List<?>) keys.get(variableSchema.get(memorySchema.get(plan)))).size();
        return new PlanShape(((Object[]) frameTemplate.get(plan)).length, variableCount);
    }

    private static int count(ExpressionNode node, SemanticModel model) {
        return switch (node) {
            case LiteralNode ignored -> 0;
            case IdentifierNode ignored -> 0;
            case CurrentItemNode ignored -> 0;
            case CurrentTemporalValueNode ignored -> 1;
            case GroupedExpressionNode grouped -> count(grouped.expression(), model);
            case CollectionLiteralNode collection -> collection.elements().stream()
                    .mapToInt(element -> count(element, model)).sum();
            case BinaryOperationNode binary -> count(binary.left(), model) + count(binary.right(), model);
            case UnaryOperationNode unary -> count(unary.operand(), model);
            case PostfixOperationNode postfix -> count(postfix.operand(), model);
            case BetweenNode between -> count(between.value(), model)
                    + count(between.lowerBound(), model)
                    + count(between.upperBound(), model);
            case MembershipNode membership -> count(membership.element(), model) + count(membership.collection(), model);
            case NullCoalesceNode coalesce -> coalesce.operands().stream()
                    .mapToInt(operand -> count(operand, model)).sum();
            case ConditionalNode conditional -> conditional.branches().stream()
                    .mapToInt(branch -> count(branch.condition(), model) + count(branch.consequence(), model))
                    .sum() + count(conditional.elseExpression(), model);
            case FunctionCallNode function -> countArguments(function.arguments(), model)
                    + (isElidedAssertion(function, model) ? 0 : 1);
            case NavigationChainNode navigation -> countNavigation(navigation, model);
        };
    }

    private static boolean isElidedAssertion(FunctionCallNode function, SemanticModel model) {
        FunctionDescriptor descriptor = model.functionBindings().get(function.id());
        return descriptor.arity() == 1
                && SCALAR_ASSERTION_OWNER.equals(descriptor.implementationMetadata().owner())
                && descriptor.parameterTypes().getFirst().equals(descriptor.returnType());
    }

    private static int countNavigation(NavigationChainNode navigation, SemanticModel model) {
        int count = count(navigation.receiver(), model);
        for (NavigationLink link : navigation.links()) {
            Object binding = model.navigationBindings().get(link.id());
            if (binding instanceof RegisteredPropertyNavigationBinding) {
                count++;
            } else if (binding instanceof RegisteredMethodNavigationBinding) {
                count += countArguments(((CallNavigationLink) link).arguments(), model) + 1;
            } else if (binding instanceof CollectionOperationBinding || link instanceof FilterNavigationLink) {
                // Collection operations and repeated predicates/lambdas are opaque calculation boundaries.
            }
        }
        return count;
    }

    private static int countArguments(List<CallArgument> arguments, SemanticModel model) {
        int count = 0;
        for (CallArgument argument : arguments) {
            if (argument instanceof ExpressionCallArgument expressionArgument) {
                count += count(expressionArgument.expression(), model);
            }
        }
        return count;
    }

    private static int percentile(List<Shape> shapes, boolean frame, double percentile) {
        List<Integer> values = shapes.stream()
                .map(shape -> frame ? shape.frameSlots : shape.calculationPoints)
                .sorted()
                .toList();
        return values.get((int) Math.ceil(percentile * values.size()) - 1);
    }

    private static int maximum(List<Shape> shapes, boolean frame) {
        return shapes.stream()
                .max(Comparator.comparingInt(shape -> frame ? shape.frameSlots : shape.calculationPoints))
                .map(shape -> frame ? shape.frameSlots : shape.calculationPoints)
                .orElse(0);
    }

    private static int variablePercentile(List<Shape> shapes, double percentile) {
        List<Integer> values = shapes.stream().map(Shape::variableCount).sorted().toList();
        return values.get((int) Math.ceil(percentile * values.size()) - 1);
    }

    private static int variableMaximum(List<Shape> shapes) {
        return shapes.stream().mapToInt(Shape::variableCount).max().orElse(0);
    }

    private record PlanShape(int frameSlots, int variableCount) {
    }

    private record ReachRange(int minimum, int maximum) {

        private static final ReachRange ZERO = new ReachRange(0, 0);
        private static final ReachRange ONE = new ReachRange(1, 1);

        ReachRange plus(ReachRange other) {
            return new ReachRange(minimum + other.minimum, maximum + other.maximum);
        }

        ReachRange plusOptional(ReachRange other) {
            return new ReachRange(minimum, maximum + other.maximum);
        }
    }

    private record Shape(
            String caseId,
            int frameSlots,
            int variableCount,
            int calculationPoints,
            int minimumReached,
            int maximumReached) {
    }
}
