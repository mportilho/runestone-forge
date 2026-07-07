package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.api.ExpressionEnvironment;
import com.runestone.expeval_mk3.api.JavaTypeCatalog;
import com.runestone.expeval_mk3.api.CollectionType;
import com.runestone.expeval_mk3.api.MapType;
import com.runestone.expeval_mk3.api.NullType;
import com.runestone.expeval_mk3.api.ObjectType;
import com.runestone.expeval_mk3.api.ScalarType;
import com.runestone.expeval_mk3.api.UnknownType;
import com.runestone.expeval_mk3.api.VectorType;
import com.runestone.expeval_mk3.internal.diagnostics.DiagnosticCode;
import com.runestone.expeval_mk3.internal.semantics.NavigationBindingDetail;
import com.runestone.expeval_mk3.internal.semantics.NavigationBindingKind;
import com.runestone.expeval_mk3.internal.semantics.NavigationBindingTarget;
import com.runestone.expeval_mk3.internal.semantics.ResolvedNavigationBinding;
import com.runestone.expeval_mk3.internal.semantics.SemanticModel;
import com.runestone.expeval_mk3.internal.semantics.SemanticResolver;
import com.runestone.expeval_mk3.internal.semantics.SemanticResolutionResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SemanticNavigationResolverTest {

    private static final ObjectType CUSTOMER_TYPE = new ObjectType("Customer");

    private final SemanticResolver resolver = new SemanticResolver();

    @Test
    @DisplayName("registered object properties and methods resolve to navigation bindings and result types")
    void registeredObjectPropertiesAndMethodsResolveToNavigationBindingsAndResultTypes() throws NoSuchMethodException {
        ExpressionFileNode ast = AstTestSupport.build("customer.name || customer.label(\"VIP \")");
        ExpressionEnvironment environment = customerEnvironment();

        SemanticModel model = resolveModel(ast, environment);

        PropertyNavigationLink property = node(ast, PropertyNavigationLink.class, 0);
        MethodNavigationLink method = node(ast, MethodNavigationLink.class, 0);
        assertThat(model.navigationBindings())
                .containsEntry(property.id(), new ResolvedNavigationBinding(
                        NavigationBindingKind.OBJECT_PROPERTY,
                        new NavigationBindingTarget(CUSTOMER_TYPE, ScalarType.STRING),
                        NavigationSafety.REGULAR,
                        NavigationBindingDetail.named("name")))
                .containsEntry(method.id(), new ResolvedNavigationBinding(
                        NavigationBindingKind.OBJECT_METHOD,
                        new NavigationBindingTarget(CUSTOMER_TYPE, ScalarType.STRING),
                        NavigationSafety.REGULAR,
                        NavigationBindingDetail.withArguments("label", List.of(ScalarType.STRING))));
        assertThat(model.resolvedTypes())
                .containsEntry(property.id(), ScalarType.STRING)
                .containsEntry(method.id(), ScalarType.STRING)
                .containsEntry(node(ast, NavigationChainNode.class, 0).id(), ScalarType.STRING)
                .containsEntry(node(ast, NavigationChainNode.class, 1).id(), ScalarType.STRING);
    }

    @Test
    @DisplayName("safe navigation does not mask missing object members on known receiver types")
    void safeNavigationDoesNotMaskMissingObjectMembersOnKnownReceiverTypes() throws NoSuchMethodException {
        ExpressionFileNode ast = AstTestSupport.build("customer?.missing");
        ExpressionEnvironment environment = customerEnvironment();

        SemanticResolutionResult result = resolver.resolve(ast, environment);

        assertThat(result.hasErrors()).isTrue();
        assertThat(result.model()).isEmpty();
        assertThat(result.diagnostics())
                .singleElement()
                .satisfies(diagnostic -> {
                    assertThat(diagnostic.code()).isEqualTo(DiagnosticCode.SEMANTIC_NAVIGATION_MEMBER_NOT_FOUND);
                    assertThat(diagnostic.message()).contains("missing").contains("Customer");
                    assertThat(diagnostic.span()).isEqualTo(node(ast, PropertyNavigationLink.class, 0).sourceSpan());
                });
    }

    @Test
    @DisplayName("map textual subscript resolves as key access while dot property remains invalid")
    void mapTextualSubscriptResolvesAsKeyAccessWhileDotPropertyRemainsInvalid() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("bag", new MapType(ScalarType.NUMBER))
                .build();
        ExpressionFileNode subscriptAst = AstTestSupport.build("bag[\"total\"]");

        SemanticModel model = resolveModel(subscriptAst, environment);

        SubscriptNavigationLink subscript = node(subscriptAst, SubscriptNavigationLink.class, 0);
        assertThat(model.navigationBindings())
                .containsEntry(subscript.id(), new ResolvedNavigationBinding(
                        NavigationBindingKind.MAP_KEY,
                        new NavigationBindingTarget(new MapType(ScalarType.NUMBER), ScalarType.NUMBER),
                        NavigationSafety.REGULAR,
                        NavigationBindingDetail.named("total")));
        assertThat(model.resolvedTypes()).containsEntry(node(subscriptAst, NavigationChainNode.class, 0).id(), ScalarType.NUMBER);

        ExpressionFileNode propertyAst = AstTestSupport.build("bag.total");

        SemanticResolutionResult result = resolver.resolve(propertyAst, environment);

        assertThat(result.hasErrors()).isTrue();
        assertThat(result.diagnostics())
                .singleElement()
                .satisfies(diagnostic -> assertThat(diagnostic.code())
                        .isEqualTo(DiagnosticCode.SEMANTIC_NAVIGATION_RECEIVER_TYPE_MISMATCH));
    }

    @Test
    @DisplayName("string vector and collection indexes and slices validate known receiver compatibility")
    void stringVectorAndCollectionIndexesAndSlicesValidateKnownReceiverCompatibility() {
        ExpressionFileNode ast = AstTestSupport.build(
                "letter := text[0]; part := text[1:3]; first := values[0]; tail := values[1:]; item := items[0]; rest := items[1:]; first + item");
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("text", ScalarType.STRING)
                .externalSymbol("values", new VectorType(ScalarType.NUMBER))
                .externalSymbol("items", new CollectionType(ScalarType.NUMBER))
                .build();

        SemanticModel model = resolveModel(ast, environment);

        assertThat(model.navigationBindings())
                .containsEntry(node(ast, SubscriptNavigationLink.class, 0).id(), new ResolvedNavigationBinding(
                        NavigationBindingKind.STRING_INDEX,
                        new NavigationBindingTarget(ScalarType.STRING, ScalarType.STRING),
                        NavigationSafety.REGULAR,
                        NavigationBindingDetail.empty()))
                .containsEntry(node(ast, SubscriptNavigationLink.class, 1).id(), new ResolvedNavigationBinding(
                        NavigationBindingKind.STRING_SLICE,
                        new NavigationBindingTarget(ScalarType.STRING, ScalarType.STRING),
                        NavigationSafety.REGULAR,
                        NavigationBindingDetail.empty()))
                .containsEntry(node(ast, SubscriptNavigationLink.class, 2).id(), new ResolvedNavigationBinding(
                        NavigationBindingKind.VECTOR_INDEX,
                        new NavigationBindingTarget(new VectorType(ScalarType.NUMBER), ScalarType.NUMBER),
                        NavigationSafety.REGULAR,
                        NavigationBindingDetail.empty()))
                .containsEntry(node(ast, SubscriptNavigationLink.class, 3).id(), new ResolvedNavigationBinding(
                        NavigationBindingKind.VECTOR_SLICE,
                        new NavigationBindingTarget(
                                new VectorType(ScalarType.NUMBER),
                                new VectorType(ScalarType.NUMBER)),
                        NavigationSafety.REGULAR,
                        NavigationBindingDetail.empty()))
                .containsEntry(node(ast, SubscriptNavigationLink.class, 4).id(), new ResolvedNavigationBinding(
                        NavigationBindingKind.COLLECTION_INDEX,
                        new NavigationBindingTarget(new CollectionType(ScalarType.NUMBER), ScalarType.NUMBER),
                        NavigationSafety.REGULAR,
                        NavigationBindingDetail.empty()))
                .containsEntry(node(ast, SubscriptNavigationLink.class, 5).id(), new ResolvedNavigationBinding(
                        NavigationBindingKind.COLLECTION_SLICE,
                        new NavigationBindingTarget(
                                new CollectionType(ScalarType.NUMBER),
                                new CollectionType(ScalarType.NUMBER)),
                        NavigationSafety.REGULAR,
                        NavigationBindingDetail.empty()));

        ExpressionFileNode invalidAst = AstTestSupport.build("amount[0]");
        SemanticResolutionResult result = resolver.resolve(
                invalidAst,
                ExpressionEnvironment.builder().externalSymbol("amount", ScalarType.NUMBER).build());

        assertThat(result.hasErrors()).isTrue();
        assertThat(result.diagnostics())
                .singleElement()
                .satisfies(diagnostic -> assertThat(diagnostic.code())
                        .isEqualTo(DiagnosticCode.SEMANTIC_NAVIGATION_RECEIVER_TYPE_MISMATCH));
    }

    @Test
    @DisplayName("hexadecimal and octal subscript indexes produce decimal suggestions")
    void hexadecimalAndOctalSubscriptIndexesProduceDecimalSuggestions() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("values", new VectorType(ScalarType.NUMBER))
                .build();
        ExpressionFileNode indexAst = AstTestSupport.build("values[0x0A]");

        SemanticResolutionResult indexResult = resolver.resolve(indexAst, environment);

        assertThat(indexResult.hasErrors()).isTrue();
        assertThat(indexResult.diagnostics())
                .singleElement()
                .satisfies(diagnostic -> {
                    assertThat(diagnostic.code()).isEqualTo(DiagnosticCode.SEMANTIC_INVALID_SUBSCRIPT_INDEX_FORMAT);
                    assertThat(diagnostic.message()).contains("decimal").contains("10");
                });

        ExpressionFileNode sliceAst = AstTestSupport.build("values[-07:010]");

        SemanticResolutionResult sliceResult = resolver.resolve(sliceAst, environment);

        assertThat(sliceResult.hasErrors()).isTrue();
        assertThat(sliceResult.diagnostics())
                .anySatisfy(diagnostic -> {
                    assertThat(diagnostic.code()).isEqualTo(DiagnosticCode.SEMANTIC_INVALID_SUBSCRIPT_INDEX_FORMAT);
                    assertThat(diagnostic.message()).contains("decimal").contains("-7");
                })
                .anySatisfy(diagnostic -> {
                    assertThat(diagnostic.code()).isEqualTo(DiagnosticCode.SEMANTIC_INVALID_SUBSCRIPT_INDEX_FORMAT);
                    assertThat(diagnostic.message()).contains("decimal").contains("8");
                });
    }

    @Test
    @DisplayName("unknown receivers create deferred bindings and fail when strict mode rejects residual navigation")
    void unknownReceiversCreateDeferredBindingsAndFailWhenStrictModeRejectsResidualNavigation() {
        ExpressionFileNode nonStrictAst = AstTestSupport.build("late.name");
        ExpressionEnvironment nonStrictEnvironment = ExpressionEnvironment.builder()
                .externalSymbol("late")
                .build();

        SemanticModel model = resolveModel(nonStrictAst, nonStrictEnvironment);

        PropertyNavigationLink link = node(nonStrictAst, PropertyNavigationLink.class, 0);
        assertThat(model.navigationBindings())
                .containsEntry(link.id(), new ResolvedNavigationBinding(
                        NavigationBindingKind.DEFERRED_UNKNOWN_RECEIVER,
                        new NavigationBindingTarget(UnknownType.INSTANCE, UnknownType.INSTANCE),
                        NavigationSafety.REGULAR,
                        NavigationBindingDetail.empty()));
        assertThat(model.residualTypeChecks())
                .singleElement()
                .satisfies(check -> assertThat(check.description()).contains("Navigation receiver type remains unknown"));

        ExpressionFileNode strictAst = AstTestSupport.build("late.name");
        ExpressionEnvironment strictEnvironment = nonStrictEnvironment.toBuilder().strictMode(true).build();

        SemanticResolutionResult strictResult = resolver.resolve(strictAst, strictEnvironment);

        assertThat(strictResult.hasErrors()).isTrue();
        assertThat(strictResult.diagnostics())
                .anySatisfy(diagnostic -> assertThat(diagnostic.code())
                        .isEqualTo(DiagnosticCode.SEMANTIC_STRICT_UNKNOWN_TYPE_RESTRICTION));
    }

    @Test
    @DisplayName("wildcards filters and collection operations validate compatible known receiver types")
    void wildcardsFiltersAndCollectionOperationsValidateCompatibleKnownReceiverTypes() {
        ExpressionFileNode ast = AstTestSupport.build(
                "all := values[*]; filtered := values[?(true)]; total := values..sum(); keys := bag..keys(); vals := bag..values(); total");
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("values", new VectorType(ScalarType.NUMBER))
                .externalSymbol("bag", new MapType(ScalarType.NUMBER))
                .build();

        SemanticModel model = resolveModel(ast, environment);

        assertThat(model.navigationBindings())
                .containsEntry(node(ast, SubscriptNavigationLink.class, 0).id(), new ResolvedNavigationBinding(
                        NavigationBindingKind.WILDCARD,
                        new NavigationBindingTarget(
                                new VectorType(ScalarType.NUMBER),
                                new CollectionType(ScalarType.NUMBER)),
                        NavigationSafety.REGULAR,
                        NavigationBindingDetail.empty()))
                .containsEntry(node(ast, FilterNavigationLink.class, 0).id(), new ResolvedNavigationBinding(
                        NavigationBindingKind.FILTER,
                        new NavigationBindingTarget(
                                new VectorType(ScalarType.NUMBER),
                                new VectorType(ScalarType.NUMBER)),
                        NavigationSafety.REGULAR,
                        NavigationBindingDetail.empty()))
                .containsEntry(node(ast, CollectionOperationNavigationLink.class, 0).id(), new ResolvedNavigationBinding(
                        NavigationBindingKind.COLLECTION_OPERATION,
                        new NavigationBindingTarget(new VectorType(ScalarType.NUMBER), ScalarType.NUMBER),
                        NavigationSafety.REGULAR,
                        NavigationBindingDetail.named("sum")))
                .containsEntry(node(ast, CollectionOperationNavigationLink.class, 1).id(), new ResolvedNavigationBinding(
                        NavigationBindingKind.COLLECTION_OPERATION,
                        new NavigationBindingTarget(
                                new MapType(ScalarType.NUMBER),
                                new CollectionType(ScalarType.STRING)),
                        NavigationSafety.REGULAR,
                        NavigationBindingDetail.named("keys")))
                .containsEntry(node(ast, CollectionOperationNavigationLink.class, 2).id(), new ResolvedNavigationBinding(
                        NavigationBindingKind.COLLECTION_OPERATION,
                        new NavigationBindingTarget(
                                new MapType(ScalarType.NUMBER),
                                new CollectionType(ScalarType.NUMBER)),
                        NavigationSafety.REGULAR,
                        NavigationBindingDetail.named("values")));

        ExpressionFileNode invalidAst = AstTestSupport.build("amount..sum()");
        SemanticResolutionResult result = resolver.resolve(
                invalidAst,
                ExpressionEnvironment.builder().externalSymbol("amount", ScalarType.NUMBER).build());

        assertThat(result.hasErrors()).isTrue();
        assertThat(result.diagnostics())
                .singleElement()
                .satisfies(diagnostic -> assertThat(diagnostic.code())
                        .isEqualTo(DiagnosticCode.SEMANTIC_NAVIGATION_RECEIVER_TYPE_MISMATCH));
    }

    @Test
    @DisplayName("filter current item navigation validates the known element metadata")
    void filterCurrentItemNavigationValidatesTheKnownElementMetadata() throws NoSuchMethodException {
        ExpressionFileNode ast = AstTestSupport.build("customers[?(@.missing)]");
        ExpressionEnvironment environment = customerEnvironment().toBuilder()
                .externalSymbol("customers", new VectorType(CUSTOMER_TYPE))
                .build();

        SemanticResolutionResult result = resolver.resolve(ast, environment);

        assertThat(result.hasErrors()).isTrue();
        assertThat(result.diagnostics())
                .singleElement()
                .satisfies(diagnostic -> {
                    assertThat(diagnostic.code()).isEqualTo(DiagnosticCode.SEMANTIC_NAVIGATION_MEMBER_NOT_FOUND);
                    assertThat(diagnostic.message()).contains("missing").contains("Customer");
                });
    }

    @Test
    @DisplayName("collection operations reject unexpected positional arguments")
    void collectionOperationsRejectUnexpectedPositionalArguments() {
        ExpressionFileNode ast = AstTestSupport.build("values..sum(1)");
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("values", new VectorType(ScalarType.NUMBER))
                .build();

        SemanticResolutionResult result = resolver.resolve(ast, environment);

        assertThat(result.hasErrors()).isTrue();
        assertThat(result.diagnostics())
                .singleElement()
                .satisfies(diagnostic -> assertThat(diagnostic.code())
                        .isEqualTo(DiagnosticCode.SEMANTIC_NAVIGATION_ARGUMENT_TYPE_MISMATCH));
    }

    @Test
    @DisplayName("safe navigation over a statically null receiver resolves to a null binding")
    void safeNavigationOverAStaticallyNullReceiverResolvesToANullBinding() {
        ExpressionFileNode ast = AstTestSupport.build("value := null; value?.name");

        SemanticModel model = resolveModel(ast, ExpressionEnvironment.standard());

        PropertyNavigationLink link = node(ast, PropertyNavigationLink.class, 0);
        assertThat(model.navigationBindings())
                .containsEntry(link.id(), new ResolvedNavigationBinding(
                        NavigationBindingKind.SAFE_NULL_RECEIVER,
                        new NavigationBindingTarget(NullType.INSTANCE, NullType.INSTANCE),
                        NavigationSafety.SAFE,
                        NavigationBindingDetail.empty()));
        assertThat(model.resolvedTypes()).containsEntry(node(ast, NavigationChainNode.class, 0).id(), NullType.INSTANCE);
    }

    @Test
    @DisplayName("safe navigation over null does not mask invalid subscript index formats")
    void safeNavigationOverNullDoesNotMaskInvalidSubscriptIndexFormats() {
        ExpressionFileNode ast = AstTestSupport.build("value := null; value?.[0x0A]");

        SemanticResolutionResult result = resolver.resolve(ast, ExpressionEnvironment.standard());

        assertThat(result.hasErrors()).isTrue();
        assertThat(result.diagnostics())
                .singleElement()
                .satisfies(diagnostic -> assertThat(diagnostic.code())
                        .isEqualTo(DiagnosticCode.SEMANTIC_INVALID_SUBSCRIPT_INDEX_FORMAT));
    }

    private SemanticModel resolveModel(ExpressionFileNode ast, ExpressionEnvironment environment) {
        SemanticResolutionResult result = resolver.resolve(ast, environment);
        assertThat(result.hasErrors()).isFalse();
        assertThat(result.diagnostics()).isEmpty();
        return result.model().orElseThrow();
    }

    private static ExpressionEnvironment customerEnvironment() throws NoSuchMethodException {
        return ExpressionEnvironment.builder()
                .externalSymbol("customer", CUSTOMER_TYPE)
                .registerJavaType(JavaTypeCatalog.registerJavaType(Customer.class, CUSTOMER_TYPE)
                        .method(
                                "label",
                                Customer.class.getMethod("label", String.class),
                                ScalarType.STRING,
                                List.of(ScalarType.STRING))
                        .build())
                .build();
    }

    private static <T extends AstNode> T node(ExpressionFileNode ast, Class<T> type, int occurrence) {
        return AstTestSupport.flatten(ast).stream()
                .filter(type::isInstance)
                .map(type::cast)
                .skip(occurrence)
                .findFirst()
                .orElseThrow();
    }

    public record Customer(String name, boolean active) {

        public String label(String prefix) {
            return prefix + name;
        }
    }
}
