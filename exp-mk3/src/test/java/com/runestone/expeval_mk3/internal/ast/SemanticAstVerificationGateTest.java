package com.runestone.expeval_mk3.internal.ast;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.runestone.expeval_mk3.internal.parser.ExpressionParser;
import com.runestone.expeval_mk3.internal.parser.ParseResult;
import com.runestone.expeval_mk3.internal.parser.ParseSuccess;
import com.runestone.expeval_mk3.internal.source.SourceSpan;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Provide;
import net.jqwik.api.Property;
import net.jqwik.api.lifecycle.BeforeProperty;
import org.junit.jupiter.api.BeforeEach;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class SemanticAstVerificationGateTest {

    private static final ObjectMapper YAML = new ObjectMapper(new YAMLFactory());
    private static final NodeId GENERATED_ID = NodeId.UNASSIGNED;
    private static final SourceSpan GENERATED_SPAN = new SourceSpan(0, 1, 1, 1);

    private ExpressionParser parser;
    private SemanticAstBuilder astBuilder;

    @BeforeEach
    void setUp() {
        parser = new ExpressionParser();
        astBuilder = new SemanticAstBuilder();
    }

    @BeforeProperty
    void setUpProperty() {
        parser = new ExpressionParser();
        astBuilder = new SemanticAstBuilder();
    }

    @Test
    @DisplayName("valid Etapa 2 corpus cases build semantic ASTs with deterministic metadata")
    void validEtapa2CorpusCasesBuildSemanticAstsWithDeterministicMetadata() {
        for (CorpusCase corpusCase : loadCorpusCases()) {
            if (!corpusCase.valid()) {
                continue;
            }

            ExpressionFileNode ast = build(corpusCase.source(), corpusCase.path());
            List<AstNode> nodes = preOrder(ast);

            assertThat(nodes).as("%s", corpusCase.path()).isNotEmpty();
            assertThat(nodes).as("%s", corpusCase.path())
                    .extracting(node -> node.id().value())
                    .containsExactlyElementsOf(contiguousIds(nodes.size()));
            assertThat(nodes).as("%s", corpusCase.path()).allSatisfy(node -> {
                assertThat(node.id()).isNotEqualTo(NodeId.UNASSIGNED);
                assertThat(node.sourceSpan().offset()).isBetween(0, corpusCase.source().length());
                assertThat(node.sourceSpan().endOffset()).isBetween(node.sourceSpan().offset(), corpusCase.source().length());
                assertThat(node.sourceSpan().endOffset()).isGreaterThan(node.sourceSpan().offset());
            });
            assertThat(AstStructuralEquality.equals(ast, build(AstPrettyPrinter.print(ast), corpusCase.path()))).isTrue();
        }
    }

    @Test
    @DisplayName("destructuring assignments build source-faithful target nodes")
    void destructuringAssignmentsBuildSourceFaithfulTargetNodes() {
        String source = "[a, b] := pair; a";

        ExpressionFileNode ast = build(source, Path.of("destructuring"));

        assertThat(ast.assignments()).singleElement().satisfies(assignment -> {
            assertThat(assignment.target()).isInstanceOf(DestructuringAssignmentTargetNode.class);
            DestructuringAssignmentTargetNode target = (DestructuringAssignmentTargetNode) assignment.target();
            assertThat(target.elements()).extracting(IdentifierAssignmentTargetNode::name).containsExactly("a", "b");
            assertThat(source.substring(target.sourceSpan().offset(), target.sourceSpan().endOffset())).isEqualTo("[a, b]");
            assertThat(target.elements()).extracting(element -> source.substring(
                    element.sourceSpan().offset(),
                    element.sourceSpan().endOffset())).containsExactly("a", "b");
        });
        assertThat(AstPrettyPrinter.print(ast)).isEqualTo("[a, b] := pair;\na");
        assertThat(AstStructuralEquality.equals(ast, build(AstPrettyPrinter.print(ast), Path.of("destructuring")))).isTrue();
    }

    @Property(tries = 100)
    void boundedSemanticAstValuesRoundTripThroughPrettyPrintAndReparse(
            @ForAll("boundedSemanticAstFiles") ExpressionFileNode generated) {
        ExpressionFileNode reparsed = build(AstPrettyPrinter.print(generated), Path.of("property"));

        assertThat(AstStructuralEquality.equals(generated, reparsed)).isTrue();
    }

    @Provide
    Arbitrary<ExpressionFileNode> boundedSemanticAstFiles() {
        return Arbitraries.integers().between(0, 999).map(SemanticAstVerificationGateTest::generatedFile);
    }

    @Test
    @DisplayName("semantic AST records do not retain ANTLR parse tree types")
    void semanticAstRecordsDoNotRetainAntlrParseTreeTypes() {
        for (Class<?> astType : astTypes()) {
            if (!astType.isRecord()) {
                continue;
            }
            for (RecordComponent component : astType.getRecordComponents()) {
                assertThat(containsAntlrType(component.getGenericType()))
                        .as("%s.%s", astType.getSimpleName(), component.getName())
                        .isFalse();
            }
        }

        ExpressionFileNode ast = build(
                "[a, b] := pair; selected := account.name?.if(total, @.value)[\"key\"]?.[2]; selected",
                Path.of("antlr-retention"));
        assertNoAntlrObjects(ast, Collections.newSetFromMap(new IdentityHashMap<>()));
    }

    @Test
    @DisplayName("AST builder returns success trees or expression diagnostic failures")
    void astBuilderReturnsSuccessTreesOrExpressionDiagnosticFailures() {
        ParseResult parseResult = parser.parse("bad := d\"2024-02-30\"; bad");
        assertThat(parseResult).isInstanceOf(ParseSuccess.class);

        SemanticAstBuildResult result = astBuilder.build((ParseSuccess) parseResult);

        assertThat(result).isInstanceOf(SemanticAstBuildFailure.class);
        assertThat(((SemanticAstBuildFailure) result).diagnostics()).isNotEmpty();

        for (CorpusCase corpusCase : loadCorpusCases()) {
            ParseResult corpusParseResult = parser.parse(corpusCase.source());
            if (corpusParseResult instanceof ParseSuccess success) {
                SemanticAstBuildResult corpusBuildResult = astBuilder.build(success);
                assertThat(corpusBuildResult)
                        .as("%s", corpusCase.path())
                        .isInstanceOfAny(SemanticAstBuildSuccess.class, SemanticAstBuildFailure.class);
                if (corpusBuildResult instanceof SemanticAstBuildFailure failure) {
                    assertThat(failure.diagnostics()).as("%s", corpusCase.path()).isNotEmpty();
                }
            }
        }
    }

    private static ExpressionFileNode generatedFile(int seed) {
        long first = seed % 100L;
        long second = seed / 10L % 100L;
        ExpressionNode one = literal(new LongLiteralValue(first));
        ExpressionNode two = literal(new LongLiteralValue(second));
        ExpressionNode text = literal(new StringLiteralValue("text" + seed));
        ExpressionNode decimal = literal(new DecimalLiteralValue(new BigDecimal((seed % 9 + 1) + ".25")));
        ExpressionNode flag = identifier("flag" + seed);
        ExpressionNode value = identifier("value" + seed);
        ExpressionNode sum = binary(grouped(identifier("a" + seed)), BinaryOperator.ADD, grouped(one));
        ExpressionNode negated = unary(UnaryOperator.NEGATE, grouped(decimal));
        ExpressionNode coalesce = new NullCoalesceNode(
                GENERATED_ID,
                GENERATED_SPAN,
                List.of(grouped(identifier("primary" + seed)), grouped(identifier("fallback" + seed)), grouped(text)),
                List.of(GENERATED_SPAN, GENERATED_SPAN));
        ExpressionNode vector = new VectorLiteralNode(GENERATED_ID, GENERATED_SPAN, List.of(one, grouped(sum), text));
        ExpressionNode functionCall = new FunctionCallNode(
                GENERATED_ID,
                GENERATED_SPAN,
                new FunctionName("max"),
                List.of(one, two));
        ExpressionNode navigation = new NavigationChainNode(
                GENERATED_ID,
                GENERATED_SPAN,
                identifier("account" + seed),
                List.of(
                        new PropertyNavigationLink(GENERATED_ID, GENERATED_SPAN, new MemberName("name"), false),
                        new IndexSubscriptNavigationLink(
                                GENERATED_ID,
                                GENERATED_SPAN,
                                new SubscriptIntegerLiteral(seed % 10L),
                                false)));
        ExpressionNode conditional = new ConditionalNode(
                GENERATED_ID,
                GENERATED_SPAN,
                ConditionalSyntax.CLASSIC,
                List.of(new ConditionalBranchNode(GENERATED_ID, GENERATED_SPAN, flag, one)),
                List.of(),
                two);
        return switch (seed % 9) {
            case 0 -> file(List.of(), Optional.of(sum));
            case 1 -> file(List.of(assignment("result" + seed, coalesce)), Optional.of(identifier("result" + seed)));
            case 2 -> file(List.of(assignment("negative" + seed, negated)), Optional.of(identifier("negative" + seed)));
            case 3 -> file(List.of(assignment("items" + seed, vector)), Optional.of(identifier("items" + seed)));
            case 4 -> file(List.of(assignment("largest" + seed, functionCall)), Optional.of(identifier("largest" + seed)));
            case 5 -> file(List.of(assignment("selected" + seed, navigation)), Optional.of(identifier("selected" + seed)));
            case 6 -> file(List.of(assignment("decision" + seed, conditional)), Optional.of(identifier("decision" + seed)));
            case 7 -> file(
                    List.of(destructuringAssignment(List.of("left" + seed, "right" + seed), identifier("pair" + seed))),
                    Optional.of(value));
            default -> file(List.of(assignment("only" + seed, literal(new BooleanLiteralValue(true)))), Optional.empty());
        };
    }

    private ExpressionFileNode build(String source, Path path) {
        ParseResult parseResult = parser.parse(source);
        assertThat(parseResult).as("%s", path).isInstanceOf(ParseSuccess.class);
        SemanticAstBuildResult buildResult = astBuilder.build((ParseSuccess) parseResult);
        assertThat(buildResult).as("%s", path).isInstanceOf(SemanticAstBuildSuccess.class);
        return ((SemanticAstBuildSuccess) buildResult).file();
    }

    private static List<CorpusCase> loadCorpusCases() {
        Path root = corpusRoot();
        try (Stream<Path> files = Files.walk(root)) {
            return files
                    .filter(Files::isRegularFile)
                    .filter(SemanticAstVerificationGateTest::isYamlFile)
                    .sorted()
                    .map(SemanticAstVerificationGateTest::loadCorpusCase)
                    .toList();
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to scan expression corpus", exception);
        }
    }

    private static Path corpusRoot() {
        var resource = SemanticAstVerificationGateTest.class.getClassLoader().getResource("corpus");
        if (resource == null) {
            throw new IllegalStateException("Expression corpus resource not found");
        }
        try {
            return Path.of(resource.toURI());
        } catch (URISyntaxException exception) {
            throw new IllegalStateException("Invalid expression corpus resource URI", exception);
        }
    }

    private static CorpusCase loadCorpusCase(Path path) {
        try {
            JsonNode root = YAML.readTree(path.toFile());
            return new CorpusCase(
                    path,
                    "valid".equals(requiredText(root, "kind", path)),
                    requiredText(root, "source", path));
        } catch (IOException exception) {
            throw new IllegalArgumentException("Invalid YAML in expression case: " + path, exception);
        }
    }

    private static String requiredText(JsonNode root, String field, Path path) {
        JsonNode node = root.get(field);
        if (node == null || !node.isTextual()) {
            throw new IllegalArgumentException("Missing text field '" + field + "' in " + path);
        }
        return node.textValue();
    }

    private static boolean isYamlFile(Path path) {
        String fileName = path.getFileName().toString();
        return fileName.endsWith(".yml") || fileName.endsWith(".yaml");
    }

    private static List<Integer> contiguousIds(int count) {
        List<Integer> ids = new ArrayList<>(count);
        for (int id = 0; id < count; id++) {
            ids.add(id);
        }
        return ids;
    }

    private static List<AstNode> preOrder(ExpressionFileNode file) {
        List<AstNode> nodes = new ArrayList<>();
        collect(file, nodes);
        return nodes;
    }

    private static void collect(ExpressionFileNode file, List<AstNode> nodes) {
        nodes.add(file);
        for (AssignmentNode assignment : file.assignments()) {
            collect(assignment, nodes);
        }
        file.resultExpression().ifPresent(expression -> collect(expression, nodes));
    }

    private static void collect(AssignmentNode assignment, List<AstNode> nodes) {
        nodes.add(assignment);
        collect(assignment.target(), nodes);
        collect(assignment.expression(), nodes);
    }

    private static void collect(AssignmentTargetNode target, List<AstNode> nodes) {
        nodes.add(target);
        if (target instanceof DestructuringAssignmentTargetNode destructuring) {
            nodes.addAll(destructuring.elements());
        }
    }

    private static void collect(ExpressionNode expression, List<AstNode> nodes) {
        nodes.add(expression);
        switch (expression) {
            case BetweenNode between -> {
                collect(between.value(), nodes);
                collect(between.lowerBound(), nodes);
                collect(between.upperBound(), nodes);
            }
            case BinaryOperationNode binary -> {
                collect(binary.left(), nodes);
                collect(binary.right(), nodes);
            }
            case ConditionalNode conditional -> {
                for (ConditionalBranchNode branch : conditional.branches()) {
                    collect(branch, nodes);
                }
                collect(conditional.elseExpression(), nodes);
            }
            case CurrentItemNode ignored -> {
            }
            case CurrentTemporalValueNode ignored -> {
            }
            case FunctionCallNode functionCall -> functionCall.arguments().forEach(argument -> collect(argument, nodes));
            case GroupedExpressionNode grouped -> collect(grouped.expression(), nodes);
            case IdentifierNode ignored -> {
            }
            case LiteralNode ignored -> {
            }
            case MembershipNode membership -> {
                collect(membership.element(), nodes);
                collect(membership.collection(), nodes);
            }
            case NavigationChainNode navigation -> {
                collect(navigation.receiver(), nodes);
                navigation.links().forEach(link -> collect(link, nodes));
            }
            case NullCoalesceNode coalesce -> coalesce.operands().forEach(operand -> collect(operand, nodes));
            case PostfixOperationNode postfix -> collect(postfix.operand(), nodes);
            case UnaryOperationNode unary -> collect(unary.operand(), nodes);
            case VectorLiteralNode vector -> vector.elements().forEach(element -> collect(element, nodes));
        }
    }

    private static void collect(ConditionalBranchNode branch, List<AstNode> nodes) {
        nodes.add(branch);
        collect(branch.condition(), nodes);
        collect(branch.consequence(), nodes);
    }

    private static void collect(NavigationLink link, List<AstNode> nodes) {
        nodes.add(link);
        switch (link) {
            case CollectionOperationNavigationLink collectionOperation -> collectionOperation.arguments()
                    .forEach(argument -> collect(argument, nodes));
            case FilterNavigationLink filter -> collect(filter.predicate(), nodes);
            case IndexSubscriptNavigationLink ignored -> {
            }
            case MethodNavigationLink method -> method.arguments().forEach(argument -> collect(argument, nodes));
            case PropertyNavigationLink ignored -> {
            }
            case SliceSubscriptNavigationLink ignored -> {
            }
            case StringKeySubscriptNavigationLink ignored -> {
            }
            case WildcardNavigationLink ignored -> {
            }
        }
    }

    private static void collect(CollectionOperationArgument argument, List<AstNode> nodes) {
        switch (argument) {
            case LambdaCollectionOperationArgument lambda -> collect(lambda.lambda(), nodes);
            case PositionalCollectionOperationArgument positional -> collect(positional.expression(), nodes);
        }
    }

    private static void collect(LambdaNode lambda, List<AstNode> nodes) {
        nodes.add(lambda);
        collect(lambda.currentItem(), nodes);
        collect(lambda.body(), nodes);
    }

    private static Set<Class<?>> astTypes() {
        Set<Class<?>> types = new LinkedHashSet<>();
        collectAstType(AstNode.class, types);
        collectAstType(CollectionOperationArgument.class, types);
        collectAstType(LiteralValue.class, types);
        collectAstType(SubscriptSliceBound.class, types);
        return types;
    }

    private static void collectAstType(Class<?> type, Set<Class<?>> types) {
        if (!isAstPackage(type) || !types.add(type)) {
            return;
        }
        Class<?>[] permittedSubclasses = type.getPermittedSubclasses();
        if (permittedSubclasses != null) {
            for (Class<?> permittedSubclass : permittedSubclasses) {
                collectAstType(permittedSubclass, types);
            }
        }
        if (!type.isRecord()) {
            return;
        }
        for (RecordComponent component : type.getRecordComponents()) {
            collectAstType(component.getType(), types);
            collectAstTypes(component.getGenericType(), types);
        }
    }

    private static void collectAstTypes(Type type, Set<Class<?>> types) {
        if (type instanceof Class<?> componentType) {
            collectAstType(componentType, types);
        } else if (type instanceof ParameterizedType parameterizedType) {
            collectAstTypes(parameterizedType.getRawType(), types);
            for (Type argument : parameterizedType.getActualTypeArguments()) {
                collectAstTypes(argument, types);
            }
        } else if (type instanceof GenericArrayType genericArrayType) {
            collectAstTypes(genericArrayType.getGenericComponentType(), types);
        } else if (type instanceof WildcardType wildcardType) {
            for (Type upperBound : wildcardType.getUpperBounds()) {
                collectAstTypes(upperBound, types);
            }
            for (Type lowerBound : wildcardType.getLowerBounds()) {
                collectAstTypes(lowerBound, types);
            }
        }
    }

    private static boolean containsAntlrType(Type type) {
        if (type instanceof Class<?> componentType) {
            return isAntlrType(componentType);
        }
        if (type instanceof ParameterizedType parameterizedType) {
            if (containsAntlrType(parameterizedType.getRawType())) {
                return true;
            }
            for (Type argument : parameterizedType.getActualTypeArguments()) {
                if (containsAntlrType(argument)) {
                    return true;
                }
            }
            return false;
        }
        if (type instanceof GenericArrayType genericArrayType) {
            return containsAntlrType(genericArrayType.getGenericComponentType());
        }
        if (type instanceof WildcardType wildcardType) {
            for (Type upperBound : wildcardType.getUpperBounds()) {
                if (containsAntlrType(upperBound)) {
                    return true;
                }
            }
            for (Type lowerBound : wildcardType.getLowerBounds()) {
                if (containsAntlrType(lowerBound)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void assertNoAntlrObjects(Object value, Set<Object> visited) {
        if (value == null || !visited.add(value)) {
            return;
        }
        assertThat(isAntlrType(value.getClass()))
                .as("retained object %s", value.getClass().getName())
                .isFalse();
        if (value instanceof Optional<?> optional) {
            optional.ifPresent(element -> assertNoAntlrObjects(element, visited));
            return;
        }
        if (value instanceof Iterable<?> iterable) {
            for (Object element : iterable) {
                assertNoAntlrObjects(element, visited);
            }
            return;
        }
        if (!value.getClass().isRecord() || !isAstPackage(value.getClass())) {
            return;
        }
        for (RecordComponent component : value.getClass().getRecordComponents()) {
            try {
                assertNoAntlrObjects(component.getAccessor().invoke(value), visited);
            } catch (ReflectiveOperationException exception) {
                throw new AssertionError("Could not inspect " + value.getClass().getName() + "." + component.getName(), exception);
            }
        }
    }

    private static boolean isAntlrType(Class<?> type) {
        Package typePackage = type.getPackage();
        return ParserRuleContext.class.isAssignableFrom(type)
                || Token.class.isAssignableFrom(type)
                || ParseTree.class.isAssignableFrom(type)
                || (typePackage != null && typePackage.getName().startsWith("org.antlr"));
    }

    private static boolean isAstPackage(Class<?> type) {
        Package typePackage = type.getPackage();
        return typePackage != null && typePackage.getName().equals(AstNode.class.getPackageName());
    }

    private static ExpressionFileNode file(List<AssignmentNode> assignments, Optional<ExpressionNode> resultExpression) {
        return new ExpressionFileNode(GENERATED_ID, GENERATED_SPAN, assignments, resultExpression);
    }

    private static AssignmentNode assignment(String name, ExpressionNode expression) {
        return new AssignmentNode(
                GENERATED_ID,
                GENERATED_SPAN,
                new IdentifierAssignmentTargetNode(GENERATED_ID, GENERATED_SPAN, name),
                expression);
    }

    private static AssignmentNode destructuringAssignment(List<String> names, ExpressionNode expression) {
        List<IdentifierAssignmentTargetNode> elements = names.stream()
                .map(name -> new IdentifierAssignmentTargetNode(GENERATED_ID, GENERATED_SPAN, name))
                .toList();
        return new AssignmentNode(
                GENERATED_ID,
                GENERATED_SPAN,
                new DestructuringAssignmentTargetNode(GENERATED_ID, GENERATED_SPAN, elements),
                expression);
    }

    private static ExpressionNode identifier(String name) {
        return new IdentifierNode(GENERATED_ID, GENERATED_SPAN, name);
    }

    private static ExpressionNode literal(LiteralValue value) {
        return new LiteralNode(GENERATED_ID, GENERATED_SPAN, value);
    }

    private static ExpressionNode grouped(ExpressionNode expression) {
        return new GroupedExpressionNode(GENERATED_ID, GENERATED_SPAN, expression);
    }

    private static ExpressionNode unary(UnaryOperator operator, ExpressionNode operand) {
        return new UnaryOperationNode(GENERATED_ID, GENERATED_SPAN, operator, GENERATED_SPAN, operand);
    }

    private static ExpressionNode binary(ExpressionNode left, BinaryOperator operator, ExpressionNode right) {
        return new BinaryOperationNode(GENERATED_ID, GENERATED_SPAN, left, operator, GENERATED_SPAN, right);
    }

    private record CorpusCase(Path path, boolean valid, String source) {
    }
}
