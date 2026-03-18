package com.runestone.expeval2.internal.ast.mapping;

import com.runestone.expeval2.internal.ast.BinaryOperationNode;
import com.runestone.expeval2.internal.ast.BinaryOperator;
import com.runestone.expeval2.internal.ast.ConditionalNode;
import com.runestone.expeval2.internal.ast.DestructuringAssignmentNode;
import com.runestone.expeval2.internal.ast.ExpressionFileNode;
import com.runestone.expeval2.internal.ast.ExpressionNode;
import com.runestone.expeval2.internal.ast.FunctionCallNode;
import com.runestone.expeval2.internal.ast.IdentifierNode;
import com.runestone.expeval2.internal.ast.LiteralNode;
import com.runestone.expeval2.internal.ast.Node;
import com.runestone.expeval2.internal.ast.NodeId;
import com.runestone.expeval2.internal.ast.PostfixOperationNode;
import com.runestone.expeval2.internal.ast.PostfixOperator;
import com.runestone.expeval2.internal.ast.SimpleAssignmentNode;
import com.runestone.expeval2.internal.ast.SourceSpan;
import com.runestone.expeval2.internal.ast.UnaryOperationNode;
import com.runestone.expeval2.internal.ast.UnaryOperator;
import com.runestone.expeval2.internal.ast.VectorLiteralNode;
import com.runestone.expeval2.internal.grammar.ExpressionEvaluatorV2Parser;
import com.runestone.expeval2.internal.grammar.ExpressionEvaluatorV2ParserFacade;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SemanticAstBuilderTest {

    private final ExpressionEvaluatorV2ParserFacade parserFacade = new ExpressionEvaluatorV2ParserFacade();
    private final SemanticAstBuilder builder = new SemanticAstBuilder();

    @Test
    void shouldBuildMathAstForStablePhaseTwoSubset() {
        ExpressionFileNode fileNode = builder.buildMath(parseMath("base = 100; score(base, bonus) + -rate * 5!%"));

        assertThat(fileNode.assignments()).singleElement().isInstanceOfSatisfying(SimpleAssignmentNode.class, assignment -> {
            assertThat(assignment.targetName()).isEqualTo("base");
            assertThat(assignment.value()).isInstanceOfSatisfying(LiteralNode.class, literal -> {
                assertThat(literal.value()).isEqualTo("100");
            });
        });

        assertThat(fileNode.resultExpression()).isInstanceOfSatisfying(BinaryOperationNode.class, addition -> {
            assertThat(addition.operator()).isEqualTo(BinaryOperator.ADD);
            assertThat(addition.left()).isInstanceOfSatisfying(FunctionCallNode.class, functionCall -> {
                assertThat(functionCall.functionName()).isEqualTo("score");
                assertThat(functionCall.arguments()).hasSize(2);
                assertThat(functionCall.arguments().getFirst()).isInstanceOfSatisfying(IdentifierNode.class, identifier -> {
                    assertThat(identifier.name()).isEqualTo("base");
                });
                assertThat(functionCall.arguments().get(1)).isInstanceOfSatisfying(IdentifierNode.class, identifier -> {
                    assertThat(identifier.name()).isEqualTo("bonus");
                });
            });
            assertThat(addition.right()).isInstanceOfSatisfying(BinaryOperationNode.class, multiplication -> {
                assertThat(multiplication.operator()).isEqualTo(BinaryOperator.MULTIPLY);
                assertThat(multiplication.left()).isInstanceOfSatisfying(UnaryOperationNode.class, unary -> {
                    assertThat(unary.operator()).isEqualTo(UnaryOperator.NEGATE);
                    assertThat(unary.operand()).isInstanceOfSatisfying(IdentifierNode.class, identifier -> {
                        assertThat(identifier.name()).isEqualTo("rate");
                    });
                });
                assertThat(multiplication.right()).isInstanceOfSatisfying(PostfixOperationNode.class, percent -> {
                    assertThat(percent.operator()).isEqualTo(PostfixOperator.PERCENT);
                    assertThat(percent.operand()).isInstanceOfSatisfying(PostfixOperationNode.class, factorial -> {
                        assertThat(factorial.operator()).isEqualTo(PostfixOperator.FACTORIAL);
                        assertThat(factorial.operand()).isInstanceOfSatisfying(LiteralNode.class, literal -> {
                            assertThat(literal.value()).isEqualTo("5");
                        });
                    });
                });
            });
        });
    }

    @Test
    void shouldBuildLogicalAstWithUnifiedComparisonNodes() {
        ExpressionFileNode fileNode = builder.buildLogical(parseLogical("flag = ready(); principal + bonus >= limit() and !blocked"));

        assertThat(fileNode.assignments()).singleElement().isInstanceOfSatisfying(SimpleAssignmentNode.class, assignment -> {
            assertThat(assignment.targetName()).isEqualTo("flag");
            assertThat(assignment.value()).isInstanceOfSatisfying(FunctionCallNode.class, functionCall -> {
                assertThat(functionCall.functionName()).isEqualTo("ready");
                assertThat(functionCall.arguments()).isEmpty();
            });
        });

        assertThat(fileNode.resultExpression()).isInstanceOfSatisfying(BinaryOperationNode.class, andOperation -> {
            assertThat(andOperation.operator()).isEqualTo(BinaryOperator.AND);
            assertThat(andOperation.left()).isInstanceOfSatisfying(BinaryOperationNode.class, comparison -> {
                assertThat(comparison.operator()).isEqualTo(BinaryOperator.GREATER_THAN_OR_EQUAL);
                assertThat(comparison.left()).isInstanceOfSatisfying(BinaryOperationNode.class, addition -> {
                    assertThat(addition.operator()).isEqualTo(BinaryOperator.ADD);
                });
                assertThat(comparison.right()).isInstanceOfSatisfying(FunctionCallNode.class, functionCall -> {
                    assertThat(functionCall.functionName()).isEqualTo("limit");
                    assertThat(functionCall.arguments()).isEmpty();
                });
            });
            assertThat(andOperation.right()).isInstanceOfSatisfying(UnaryOperationNode.class, unary -> {
                assertThat(unary.operator()).isEqualTo(UnaryOperator.LOGICAL_NOT);
                assertThat(unary.operand()).isInstanceOfSatisfying(IdentifierNode.class, identifier -> {
                    assertThat(identifier.name()).isEqualTo("blocked");
                });
            });
        });
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("stablePhaseTwoMathInputs")
    void shouldBuildMathAstAcrossStablePhaseTwoCoverage(String scenario, String input) {
        assertThat(builder.buildMath(parseMath(input)))
            .as("math input for scenario '%s' should build without errors", scenario)
            .isNotNull();
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("stablePhaseTwoLogicalInputs")
    void shouldBuildLogicalAstAcrossStablePhaseTwoCoverage(String scenario, String input) {
        assertThat(builder.buildLogical(parseLogical(input)))
            .as("logical input for scenario '%s' should build without errors", scenario)
            .isNotNull();
    }

    @Test
    void shouldNormalizeIdentifierReferencesThatOnlyDifferByParserDisambiguationHints() {
        ExpressionNode genericIdentifier = assignmentValueOf("target = foo; 1");
        ExpressionNode typedReference = assignmentValueOf("target = <number>foo; 1");
        ExpressionNode typedCast = assignmentValueOf("target = <number>(foo); 1");

        assertThat(genericIdentifier).isInstanceOfSatisfying(IdentifierNode.class, identifier ->
            assertThat(identifier.name()).isEqualTo("foo")
        );
        assertIdentifierReference(typedReference, "foo");
        assertIdentifierReference(typedCast, "foo");
    }

    @Test
    void shouldUnifyComparisonNodesAcrossDifferentSyntacticFamilies() {
        assertComparisonOperator(builder.buildLogical(parseLogical("1 + 2 > 3")).resultExpression(), BinaryOperator.GREATER_THAN);
        assertComparisonOperator(builder.buildLogical(parseLogical("\"a\" = formatName()")).resultExpression(), BinaryOperator.EQUAL);
        assertComparisonOperator(builder.buildLogical(parseLogical("<date>startDate <= currDate")).resultExpression(), BinaryOperator.LESS_THAN_OR_EQUAL);
        assertComparisonOperator(builder.buildLogical(parseLogical("<time>startTime < currTime")).resultExpression(), BinaryOperator.LESS_THAN);
        assertComparisonOperator(builder.buildLogical(parseLogical("<datetime>startAt >= currDateTime")).resultExpression(), BinaryOperator.GREATER_THAN_OR_EQUAL);
        assertComparisonOperator(builder.buildLogical(parseLogical("true != false")).resultExpression(), BinaryOperator.NOT_EQUAL);
    }

    @Test
    void shouldPopulateNodeIdsAndSourceSpansAcrossBuiltTree() {
        String input = "base = 100; score(base, bonus) + rate";

        ExpressionFileNode fileNode = builder.buildMath(parseMath(input));
        List<Node> nodes = collectNodes(fileNode);

        assertThat(nodes).isNotEmpty();
        assertThat(nodes)
            .extracting(Node::nodeId)
            .extracting(NodeId::value)
            .allSatisfy(value -> assertThat(value).isNotBlank());
        assertThat(nodes)
            .extracting(Node::sourceSpan)
            .allSatisfy(sourceSpan -> assertThat(sourceSpan).isNotNull());
        assertThat(fileNode.sourceSpan()).isEqualTo(new SourceSpan(0, input.length() - 1, 1, 0, 1, input.length()));
    }

    @Test
    void shouldBuildConditionalNodesForKeywordAndFunctionalMathDecisions() {
        ExpressionNode keywordDecision = builder.buildMath(parseMath("if true then 1 elsif false then 2 else 3 endif")).resultExpression();
        ExpressionNode functionalDecision = builder.buildMath(parseMath("if(true, 1, false, 2, 3)")).resultExpression();

        assertConditionalStructure(keywordDecision, List.of("true", "false"), List.of("1", "2"), "3");
        assertConditionalStructure(functionalDecision, List.of("true", "false"), List.of("1", "2"), "3");
    }

    @Test
    void shouldBuildConditionalNodesForLogicalDecisions() {
        ExpressionNode keywordDecision = builder.buildLogical(parseLogical("if true then false elsif false then true else false endif")).resultExpression();
        ExpressionNode functionalDecision = builder.buildLogical(parseLogical("if(true, false, false, true, false)")).resultExpression();

        assertConditionalStructure(keywordDecision, List.of("true", "false"), List.of("false", "true"), "false");
        assertConditionalStructure(functionalDecision, List.of("true", "false"), List.of("false", "true"), "false");
    }

    @Test
    void shouldNormalizeGenericDecisionBranchesAndOuterTypeHints() {
        ExpressionNode genericDecision = assignmentValueOf("target = if true then foo elsif false then <number>(bar) else <text>(baz) endif; 1");
        ExpressionNode wrappedDecision = assignmentValueOf("target = <number>(if(true, foo, false, <number>(bar), <text>(baz))); 1");

        assertConditionalIdentifiers(genericDecision, List.of("foo", "bar"), "baz");
        assertConditionalIdentifiers(wrappedDecision, List.of("foo", "bar"), "baz");
    }

    @Test
    void shouldBuildVectorLiteralNodeWithHeterogeneousElements() {
        ExpressionNode vectorValue = assignmentValueOf("items = [1, true, 2024-12-31, 12:30, 2024-12-31T12:30, \"x\", [2]]; 1");

        assertThat(vectorValue).isInstanceOfSatisfying(VectorLiteralNode.class, vector -> {
            assertThat(vector.elements()).hasSize(7);
            assertThat(vector.elements().get(0)).isInstanceOfSatisfying(LiteralNode.class, literal -> assertThat(literal.value()).isEqualTo("1"));
            assertThat(vector.elements().get(1)).isInstanceOfSatisfying(LiteralNode.class, literal -> assertThat(literal.value()).isEqualTo("true"));
            assertThat(vector.elements().get(2)).isInstanceOfSatisfying(LiteralNode.class, literal -> assertThat(literal.value()).isEqualTo("2024-12-31"));
            assertThat(vector.elements().get(3)).isInstanceOfSatisfying(LiteralNode.class, literal -> assertThat(literal.value()).isEqualTo("12:30"));
            assertThat(vector.elements().get(4)).isInstanceOfSatisfying(LiteralNode.class, literal -> assertThat(literal.value()).isEqualTo("2024-12-31T12:30"));
            assertThat(vector.elements().get(5)).isInstanceOfSatisfying(LiteralNode.class, literal -> assertThat(literal.value()).isEqualTo("\"x\""));
            assertThat(vector.elements().get(6)).isInstanceOfSatisfying(VectorLiteralNode.class, nested ->
                assertThat(nested.elements())
                    .singleElement()
                    .isInstanceOfSatisfying(LiteralNode.class, literal -> assertThat(literal.value()).isEqualTo("2"))
            );
        });
    }

    @Test
    void shouldNormalizeTypedVectorReferencesAndCasts() {
        ExpressionNode typedReference = assignmentValueOf("items = <vector>payload; 1");
        ExpressionNode typedCast = assignmentValueOf("items = <vector>(payload); 1");

        assertIdentifierReference(typedReference, "payload");
        assertIdentifierReference(typedCast, "payload");
    }

    @Test
    void shouldBuildConditionalNodesForVectorDecisions() {
        ExpressionNode keywordDecision = assignmentValueOf("items = if true then [1] elsif false then [2] else [3] endif; 1");
        ExpressionNode functionalDecision = assignmentValueOf("items = if(true, [1], false, [2], [3]); 1");

        assertVectorConditional(keywordDecision, List.of("1", "2"), "3");
        assertVectorConditional(functionalDecision, List.of("1", "2"), "3");
    }

    @Test
    void shouldBuildDestructuringAssignmentsForVectorLiteralAndFunction() {
        ExpressionFileNode literalAssignmentFile = builder.buildMath(parseMath("[left,right] = [1,2]; 1"));
        ExpressionFileNode functionAssignmentFile = builder.buildMath(parseMath("[left,right] = makeVec(); 1"));

        assertThat(literalAssignmentFile.assignments()).singleElement().isInstanceOfSatisfying(DestructuringAssignmentNode.class, assignment -> {
            assertThat(assignment.targetNames()).containsExactly("left", "right");
            assertThat(assignment.value()).isInstanceOfSatisfying(VectorLiteralNode.class, vector ->
                assertThat(vector.elements())
                    .extracting(element -> ((LiteralNode) element).value())
                    .containsExactly("1", "2")
            );
        });

        assertThat(functionAssignmentFile.assignments()).singleElement().isInstanceOfSatisfying(DestructuringAssignmentNode.class, assignment -> {
            assertThat(assignment.targetNames()).containsExactly("left", "right");
            assertThat(assignment.value()).isInstanceOfSatisfying(FunctionCallNode.class, functionCall -> {
                assertThat(functionCall.functionName()).isEqualTo("makeVec");
                assertThat(functionCall.arguments()).isEmpty();
            });
        });
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("phaseThreeMathInputs")
    void shouldBuildMathAstAcrossPhaseThreeCoverage(String scenario, String input) {
        assertThat(builder.buildMath(parseMath(input)))
            .as("phase three math input for scenario '%s' should build without errors", scenario)
            .isNotNull();
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("phaseThreeLogicalInputs")
    void shouldBuildLogicalAstAcrossPhaseThreeCoverage(String scenario, String input) {
        assertThat(builder.buildLogical(parseLogical(input)))
            .as("phase three logical input for scenario '%s' should build without errors", scenario)
            .isNotNull();
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("phaseFourMathInputs")
    void shouldBuildMathAstAcrossPhaseFourCoverage(String scenario, String input) {
        assertThat(builder.buildMath(parseMath(input)))
            .as("phase four math input for scenario '%s' should build without errors", scenario)
            .isNotNull();
    }

    private ExpressionEvaluatorV2Parser.MathStartContext parseMath(String input) {
        return parserFacade.parseMath(input).root();
    }

    private ExpressionEvaluatorV2Parser.LogicalStartContext parseLogical(String input) {
        return parserFacade.parseLogical(input).root();
    }

    private static List<Node> collectNodes(ExpressionFileNode fileNode) {
        List<Node> nodes = new ArrayList<>();
        nodes.add(fileNode);
        fileNode.assignments().forEach(assignment -> {
            nodes.add(assignment);
            if (assignment instanceof SimpleAssignmentNode simpleAssignmentNode) {
                collectExpressionNodes(simpleAssignmentNode.value(), nodes);
            }
            if (assignment instanceof DestructuringAssignmentNode destructuringAssignmentNode) {
                collectExpressionNodes(destructuringAssignmentNode.value(), nodes);
            }
        });
        collectExpressionNodes(fileNode.resultExpression(), nodes);
        return nodes;
    }

    private static void collectExpressionNodes(ExpressionNode expressionNode, List<Node> nodes) {
        nodes.add(expressionNode);
        switch (expressionNode) {
            case BinaryOperationNode binaryOperationNode -> {
                collectExpressionNodes(binaryOperationNode.left(), nodes);
                collectExpressionNodes(binaryOperationNode.right(), nodes);
            }
            case ConditionalNode conditionalNode -> {
                conditionalNode.conditions().forEach(condition -> collectExpressionNodes(condition, nodes));
                conditionalNode.results().forEach(result -> collectExpressionNodes(result, nodes));
                collectExpressionNodes(conditionalNode.elseExpression(), nodes);
            }
            case FunctionCallNode functionCallNode -> functionCallNode.arguments()
                .forEach(argument -> collectExpressionNodes(argument, nodes));
            case PostfixOperationNode postfixOperationNode -> collectExpressionNodes(postfixOperationNode.operand(), nodes);
            case UnaryOperationNode unaryOperationNode -> collectExpressionNodes(unaryOperationNode.operand(), nodes);
            case VectorLiteralNode vectorLiteralNode -> vectorLiteralNode.elements()
                .forEach(element -> collectExpressionNodes(element, nodes));
            case IdentifierNode ignored -> {
            }
            case LiteralNode ignored -> {
            }
            default -> throw new IllegalStateException("unexpected expression node: " + expressionNode.getClass().getSimpleName());
        }
    }

    private ExpressionNode assignmentValueOf(String input) {
        return ((SimpleAssignmentNode) builder.buildMath(parseMath(input)).assignments().getFirst()).value();
    }

    private static void assertIdentifierReference(ExpressionNode node, String expectedName) {
        assertThat(node).isInstanceOfSatisfying(IdentifierNode.class, identifier ->
            assertThat(identifier.name()).isEqualTo(expectedName)
        );
    }

    private static void assertComparisonOperator(ExpressionNode node, BinaryOperator expectedOperator) {
        assertThat(node).isInstanceOfSatisfying(BinaryOperationNode.class, comparison ->
            assertThat(comparison.operator()).isEqualTo(expectedOperator)
        );
    }

    private static void assertConditionalStructure(
        ExpressionNode node,
        List<String> expectedConditions,
        List<String> expectedResults,
        String expectedElse
    ) {
        assertThat(node).isInstanceOfSatisfying(ConditionalNode.class, conditional -> {
            assertThat(conditional.conditions()).hasSize(expectedConditions.size());
            assertThat(conditional.results()).hasSize(expectedResults.size());
            assertThat(conditional.conditions())
                .allSatisfy(condition -> assertThat(condition).isInstanceOf(LiteralNode.class));
            assertThat(conditional.results())
                .allSatisfy(result -> assertThat(result).isInstanceOf(LiteralNode.class));
            assertThat(conditional.conditions())
                .extracting(expression -> ((LiteralNode) expression).value())
                .containsExactlyElementsOf(expectedConditions);
            assertThat(conditional.results())
                .extracting(expression -> ((LiteralNode) expression).value())
                .containsExactlyElementsOf(expectedResults);
            assertThat(conditional.elseExpression()).isInstanceOfSatisfying(LiteralNode.class, elseLiteral ->
                assertThat(elseLiteral.value()).isEqualTo(expectedElse)
            );
        });
    }

    private static void assertConditionalIdentifiers(
        ExpressionNode node,
        List<String> expectedResults,
        String expectedElse
    ) {
        assertThat(node).isInstanceOfSatisfying(ConditionalNode.class, conditional -> {
            assertThat(conditional.conditions())
                .extracting(expression -> ((LiteralNode) expression).value())
                .containsExactly("true", "false");
            assertThat(conditional.results())
                .extracting(expression -> ((IdentifierNode) expression).name())
                .containsExactlyElementsOf(expectedResults);
            assertThat(conditional.elseExpression()).isInstanceOfSatisfying(IdentifierNode.class, elseIdentifier ->
                assertThat(elseIdentifier.name()).isEqualTo(expectedElse)
            );
        });
    }

    private static void assertVectorConditional(ExpressionNode node, List<String> expectedResults, String expectedElse) {
        assertThat(node).isInstanceOfSatisfying(ConditionalNode.class, conditional -> {
            assertThat(conditional.conditions())
                .extracting(expression -> ((LiteralNode) expression).value())
                .containsExactly("true", "false");
            assertThat(conditional.results())
                .extracting(result -> ((LiteralNode) ((VectorLiteralNode) result).elements().getFirst()).value())
                .containsExactlyElementsOf(expectedResults);
            assertThat(conditional.elseExpression()).isInstanceOfSatisfying(VectorLiteralNode.class, elseVector ->
                assertThat(elseVector.elements())
                    .singleElement()
                    .isInstanceOfSatisfying(LiteralNode.class, literal -> assertThat(literal.value()).isEqualTo(expectedElse))
            );
        });
    }

    private static Stream<Arguments> stablePhaseTwoMathInputs() {
        return Stream.of(
            Arguments.of("typed numeric references", "<number>principal + rate"),
            Arguments.of("cached function with supported argument families", "$.metric(1, true, 2024-12-31, 12:30, 2024-12-31T12:30, \"txt\")"),
            Arguments.of("function with semicolon separated arguments", "score(1; false; \"x\")"),
            Arguments.of("parenthesized arithmetic precedence", "(1 + 2) * 3"),
            Arguments.of("square root function", "sqrt(16)"),
            Arguments.of("modulus bars", "|1 - 3|"),
            Arguments.of("postfix factorial and percent", "5!%"),
            Arguments.of("exponentiation with unary operand", "2 ^ -3"),
            Arguments.of("root operator chain", "64 root 4 root 2"),
            Arguments.of("multiplication division and modulo", "10 * 3 / 2 mod 4"),
            Arguments.of("assignments with explicit cast types except vector", "boolCast = <bool>(flag); numCast = <number>(amount); textCast = <text>(label); dateCast = <date>(createdOn); timeCast = <time>(createdAt); dateTimeCast = <datetime>(timestamp); 1"),
            Arguments.of("generic assignment using identifier reference", "target = foo; 1"),
            Arguments.of("generic assignment using function reference", "target = resolve(); 1"),
            Arguments.of("scalar assignment with math expression", "answer = 1 + 2 * 3; 1"),
            Arguments.of("scalar assignment with logical expression", "flag = true and !false; 1"),
            Arguments.of("scalar assignment with string constant", "textValue = \"alpha\"; 1"),
            Arguments.of("date constant assignment", "dateValue = 2024-12-31; 1"),
            Arguments.of("date current value assignment", "dateValue = currDate; 1"),
            Arguments.of("typed date reference assignment", "dateValue = <date>holiday; 1"),
            Arguments.of("time constant assignment", "timeValue = 12:30:45; 1"),
            Arguments.of("time current value assignment", "timeValue = currTime; 1"),
            Arguments.of("typed time reference assignment", "timeValue = <time>meetingTime; 1"),
            Arguments.of("datetime constant with offset assignment", "dateTimeValue = 2024-12-31T12:30:45-03:00; 1"),
            Arguments.of("datetime current value assignment", "dateTimeValue = currDateTime; 1"),
            Arguments.of("typed datetime reference assignment", "dateTimeValue = <datetime>lastUpdatedAt; 1")
        );
    }

    private static Stream<Arguments> stablePhaseTwoLogicalInputs() {
        return Stream.of(
            Arguments.of("logical constant", "true"),
            Arguments.of("typed logical reference", "<bool>enabled"),
            Arguments.of("logical function reference", "isReady()"),
            Arguments.of("logical not with tilde", "~true"),
            Arguments.of("logical not with exclamation", "!false"),
            Arguments.of("parenthesized or and expression", "(true or false) and true"),
            Arguments.of("bitwise boolean operator chain", "true nand false xor true xnor false nor true"),
            Arguments.of("boolean comparison", "true != false"),
            Arguments.of("math comparison", "1 + 2 > 3 * 4"),
            Arguments.of("string comparison", "<text>left = formatName()"),
            Arguments.of("date comparison", "<date>startDate <= currDate"),
            Arguments.of("time comparison", "<time>startTime < currTime"),
            Arguments.of("datetime comparison", "<datetime>startAt >= currDateTime"),
            Arguments.of("logical input with supported leading assignment", "value = foo; 1 < 2 and isReady()")
        );
    }

    private static Stream<Arguments> phaseThreeMathInputs() {
        return Stream.of(
            Arguments.of("numeric if then elsif else decision", "if true then 1 elsif false then 2 else 3 endif"),
            Arguments.of("numeric functional decision", "if(true, 1, false, 2, 3)"),
            Arguments.of("generic assignment with if then elsif else", "target = if true then foo elsif false then bar else baz endif; 1"),
            Arguments.of("generic assignment with functional decision", "target = if(true, foo, false, bar, baz); 1"),
            Arguments.of("string decision assignment", "textValue = if true then \"a\" elsif false then \"b\" else \"c\" endif; 1"),
            Arguments.of("string functional decision assignment", "textValue = if(true, \"a\", false, \"b\", \"c\"); 1"),
            Arguments.of("date decision assignment", "dateValue = if true then 2024-12-31 elsif false then 2024-12-30 else currDate endif; 1"),
            Arguments.of("date functional decision assignment", "dateValue = if(true, 2024-12-31, false, 2024-12-30, currDate); 1"),
            Arguments.of("time decision assignment", "timeValue = if true then 12:30 elsif false then 13:45 else currTime endif; 1"),
            Arguments.of("time functional decision assignment", "timeValue = if(true, 12:30, false, 13:45, currTime); 1"),
            Arguments.of("datetime decision assignment", "dateTimeValue = if true then 2024-12-31T12:30 elsif false then 2024-12-30T08:15 else currDateTime endif; 1"),
            Arguments.of("datetime functional decision assignment", "dateTimeValue = if(true, 2024-12-31T12:30, false, 2024-12-30T08:15, currDateTime); 1")
        );
    }

    private static Stream<Arguments> phaseThreeLogicalInputs() {
        return Stream.of(
            Arguments.of("logical if then elsif else decision", "if true then false elsif false then true else false endif"),
            Arguments.of("logical functional decision", "if(true, false, false, true, false)")
        );
    }

    private static Stream<Arguments> phaseFourMathInputs() {
        return Stream.of(
            Arguments.of("assignments with every explicit cast type", "boolCast = <bool>(flag); numCast = <number>(amount); textCast = <text>(label); dateCast = <date>(createdOn); timeCast = <time>(createdAt); dateTimeCast = <datetime>(timestamp); vectorCast = <vector>(items); 1"),
            Arguments.of("typed vector reference assignment", "items = <vector>payload; 1"),
            Arguments.of("vector literal with all entity families", "items = [1, true, 2024-12-31, 12:30, 2024-12-31T12:30, \"x\", [2]]; 1"),
            Arguments.of("vector decision assignment", "items = if true then [1] elsif false then [2] else [3] endif; 1"),
            Arguments.of("vector functional decision assignment", "items = if(true, [1], false, [2], [3]); 1"),
            Arguments.of("destructuring assignment from vector literal", "[left,right] = [1,2]; 1"),
            Arguments.of("destructuring assignment from function", "[left,right] = makeVec(); 1")
        );
    }
}
