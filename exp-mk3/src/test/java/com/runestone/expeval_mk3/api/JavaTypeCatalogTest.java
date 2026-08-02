package com.runestone.expeval_mk3.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JavaTypeCatalogTest {

    @Test
    @DisplayName("registered Java types are nominal and expose bean and record properties")
    void registeredJavaTypesAreNominalAndExposeBeanAndRecordProperties() throws Throwable {
        JavaTypeCatalog catalog = JavaTypeCatalog.builder()
                .registerJavaType(CustomerBean.class)
                .registerJavaType(CustomerRecord.class)
                .registerJavaType(SameShapeRecord.class)
                .build();

        JavaTypeDescriptor bean = catalog.find(CustomerBean.class).orElseThrow();
        JavaTypeDescriptor record = catalog.find(CustomerRecord.class).orElseThrow();
        JavaTypeDescriptor sameShape = catalog.find(SameShapeRecord.class).orElseThrow();

        assertThat(bean.objectType()).isEqualTo(new ObjectType(CustomerBean.class.getName()));
        assertThat(record.objectType()).isNotEqualTo(sameShape.objectType());
        assertThat(bean.properties()).containsOnlyKeys("active", "name");
        assertThat(bean.properties().get("active").type()).isEqualTo(ScalarType.BOOLEAN);
        assertThat(bean.properties().get("name").accessorHandle().invoke(new CustomerBean("Ana", true)))
                .isEqualTo("Ana");
        assertThat(record.properties()).containsOnlyKeys("name", "score");
        assertThat(record.properties().get("score").type()).isEqualTo(ScalarType.NUMBER);
        assertThat(record.properties().get("score").accessorHandle()
                .invoke(new CustomerRecord("Ana", BigDecimal.TEN)))
                .isEqualTo(BigDecimal.TEN);
    }

    @Test
    @DisplayName("wildcard children are explicit and independent from navigable members")
    void wildcardChildrenAreExplicitAndIndependentFromNavigableMembers() throws Throwable {
        JavaTypeCatalog catalog = JavaTypeCatalog.builder()
                .registerJavaTypeWildcardChildren(WildcardChildProvider.class, "second", "first")
                .build();

        JavaTypeDescriptor descriptor = catalog.find(WildcardChildProvider.class).orElseThrow();

        assertThat(descriptor.properties()).containsOnlyKeys("first", "navigableOnly");
        assertThat(descriptor.methods()).isEmpty();
        assertThat(descriptor.wildcardChildren())
                .extracting(JavaWildcardChildDescriptor::name)
                .containsExactly("second", "first");
        assertThat(descriptor.wildcardChildType()).contains(ScalarType.STRING);
        assertThat(descriptor.wildcardChildren().getFirst().accessorHandle()
                .invoke(new WildcardChildProvider()))
                .isEqualTo("second");

        JavaTypeDescriptor withoutDeclaration = JavaTypeCatalog.builder()
                .registerJavaType(WildcardChildProvider.class)
                .build()
                .find(WildcardChildProvider.class)
                .orElseThrow();
        assertThat(withoutDeclaration.wildcardChildren()).isEmpty();
        assertThat(withoutDeclaration.wildcardChildType()).isEmpty();
    }

    @Test
    @DisplayName("unordered reflected wildcard child selections use binary name order")
    void unorderedReflectedWildcardChildSelectionsUseBinaryNameOrder() {
        JavaTypeDescriptor descriptor = JavaTypeCatalog.builder()
                .registerJavaTypeWildcardChildren(
                        WildcardChildProvider.class,
                        Set.of("second", "first"))
                .build()
                .find(WildcardChildProvider.class)
                .orElseThrow();

        assertThat(descriptor.wildcardChildren())
                .extracting(JavaWildcardChildDescriptor::name)
                .containsExactly("first", "second");
    }

    @Test
    @DisplayName("invalid wildcard child declarations fail catalog construction atomically")
    void invalidWildcardChildDeclarationsFailCatalogConstructionAtomically() {
        assertThatThrownBy(() -> JavaTypeCatalog.builder()
                .registerJavaTypeWildcardChildren(WildcardChildProvider.class)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("wildcard child declaration must select at least one member");
        assertThatThrownBy(() -> JavaTypeCatalog.builder()
                .registerJavaTypeWildcardChildren(WildcardChildProvider.class, Set.of())
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("wildcard child declaration must select at least one member");
        assertThatThrownBy(() -> JavaTypeCatalog.builder()
                .registerJavaTypeWildcardChildren(WildcardChildProvider.class, "missing")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("selected wildcard child member has no target: missing");
        assertThatThrownBy(() -> JavaTypeCatalog.builder()
                .registerJavaTypeWildcardChildren(WildcardChildProvider.class, "first", "first")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("duplicate wildcard child member: first");
        assertThatThrownBy(() -> JavaTypeCatalog.builder()
                .registerJavaTypeWildcardChildren(InvalidWildcardChildProvider.class, "unsupported")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("unsupported Java member type: java.util.Optional");
        assertThatThrownBy(() -> JavaTypeCatalog.builder()
                .registerJavaTypeWildcardChildren(InvalidWildcardChildProvider.class, "text", "number")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("wildcard child members must have one expression type")
                .hasMessageContaining("STRING")
                .hasMessageContaining("NUMBER");
    }

    @Test
    @DisplayName("wildcard child accessors reject null Java results")
    void wildcardChildAccessorsRejectNullJavaResults() {
        JavaWildcardChildDescriptor child = JavaTypeCatalog.builder()
                .registerJavaTypeWildcardChildren(InvalidWildcardChildProvider.class, "nullable")
                .build()
                .find(InvalidWildcardChildProvider.class)
                .orElseThrow()
                .wildcardChildren()
                .getFirst();

        assertThatThrownBy(() -> child.accessorHandle().invoke(new InvalidWildcardChildProvider()))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("function arguments and results must not be null");
    }

    @Test
    @DisplayName("Java type registration exposes explicit methods and eligible public methods")
    void javaTypeRegistrationExposesExplicitMethodsAndEligiblePublicMethods() throws Throwable {
        JavaTypeCatalog catalog = JavaTypeCatalog.builder()
                .registerJavaTypeMethod(MethodProvider.class, "privatePrice", BigDecimal.class)
                .registerJavaTypeWithPublicMethods(MethodProvider.class)
                .build();

        JavaTypeDescriptor descriptor = catalog.find(MethodProvider.class).orElseThrow();

        assertThat(descriptor.methods())
                .extracting(JavaMethodDescriptor::signature)
                .containsExactly(
                        signature("calculate", ScalarType.NUMBER),
                        signature("label"),
                        signature("privatePrice", ScalarType.NUMBER),
                        signature("value", ScalarType.STRING));
        assertThat(descriptor.findMethod(signature("calculate", ScalarType.NUMBER)))
                .get()
                .extracting(JavaMethodDescriptor::returnType)
                .isEqualTo(ScalarType.NUMBER);
        JavaMethodDescriptor calculate = descriptor.findMethod(signature("calculate", ScalarType.NUMBER)).orElseThrow();
        assertThat(calculate.invocationHandle().invoke(new MethodProvider(), BigDecimal.ONE))
                .isEqualTo(new BigDecimal("2"));
    }

    @Test
    @DisplayName("explicit method registration declares purity with impure default; batch registration is always impure")
    void explicitMethodRegistrationDeclaresPurityWithImpureDefaultBatchRegistrationIsAlwaysImpure() {
        JavaTypeCatalog catalog = JavaTypeCatalog.builder()
                .registerJavaTypeMethod(MethodProvider.class, "privatePrice", BigDecimal.class)
                .registerJavaTypeMethod(MethodProvider.class, "label", FunctionPurity.PURE)
                .registerJavaTypeWithPublicMethods(MethodProvider.class)
                .build();

        JavaTypeDescriptor descriptor = catalog.find(MethodProvider.class).orElseThrow();

        assertThat(descriptor.findMethod(signature("privatePrice", ScalarType.NUMBER)).orElseThrow().purity())
                .isEqualTo(FunctionPurity.IMPURE);
        assertThat(descriptor.findMethod(signature("label")).orElseThrow().purity())
                .isEqualTo(FunctionPurity.PURE);
        assertThat(descriptor.findMethod(signature("calculate", ScalarType.NUMBER)).orElseThrow().pure())
                .isFalse();
        assertThat(descriptor.findMethod(signature("value", ScalarType.STRING)).orElseThrow().pure())
                .isFalse();
    }

    @Test
    @DisplayName("an explicitly declared purity takes precedence over the same method's batch discovery")
    void explicitlyDeclaredPurityTakesPrecedenceOverTheSameMethodsBatchDiscovery() {
        JavaTypeCatalog catalog = JavaTypeCatalog.builder()
                .registerJavaTypeMethod(MethodProvider.class, "calculate", FunctionPurity.PURE, BigDecimal.class)
                .registerJavaTypeWithPublicMethods(MethodProvider.class)
                .build();

        JavaTypeDescriptor descriptor = catalog.find(MethodProvider.class).orElseThrow();

        assertThat(descriptor.findMethod(signature("calculate", ScalarType.NUMBER)).orElseThrow().purity())
                .isEqualTo(FunctionPurity.PURE);
    }

    @Test
    @DisplayName("registering the same explicit method twice fails catalog construction, even with matching purity")
    void registeringTheSameExplicitMethodTwiceFailsCatalogConstructionEvenWithMatchingPurity() {
        assertThatThrownBy(() -> JavaTypeCatalog.builder()
                .registerJavaTypeMethod(MethodProvider.class, "calculate", FunctionPurity.PURE, BigDecimal.class)
                .registerJavaTypeMethod(MethodProvider.class, "calculate", FunctionPurity.IMPURE, BigDecimal.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("duplicate explicit Java method registration")
                .hasMessageContaining("calculate");
        assertThatThrownBy(() -> JavaTypeCatalog.builder()
                .registerJavaTypeMethod(MethodProvider.class, "calculate", BigDecimal.class)
                .registerJavaTypeMethod(MethodProvider.class, "calculate", BigDecimal.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("duplicate explicit Java method registration")
                .hasMessageContaining("calculate");
    }

    @Test
    @DisplayName("varargs methods are excluded from public mode but supported when explicitly registered")
    void varargsMethodsAreExcludedFromPublicModeButSupportedWhenExplicitlyRegistered() throws Throwable {
        JavaTypeDescriptor publicOnly = JavaTypeCatalog.builder()
                .registerJavaTypeWithPublicMethods(MethodProvider.class)
                .build()
                .find(MethodProvider.class)
                .orElseThrow();
        JavaTypeDescriptor explicitVarargs = JavaTypeCatalog.builder()
                .registerJavaTypeMethod(MethodProvider.class, "varargs", BigDecimal[].class)
                .build()
                .find(MethodProvider.class)
                .orElseThrow();

        FunctionSignature varargsSignature = signature("varargs", new CollectionType(ScalarType.NUMBER));

        assertThat(publicOnly.findMethod(varargsSignature)).isEmpty();
        assertThat(explicitVarargs.findMethod(varargsSignature)).isPresent();
        assertThat(explicitVarargs.findMethod(varargsSignature).orElseThrow()
                .invocationHandle()
                .invoke(new MethodProvider(), List.of(BigDecimal.TEN, BigDecimal.ONE)))
                .isEqualTo(BigDecimal.TEN);

        FunctionSignature primitiveVarargsSignature = signature("sumInts", new CollectionType(ScalarType.NUMBER));
        assertThat(explicitVarargs.findMethod(primitiveVarargsSignature)).isEmpty();

        JavaMethodDescriptor primitiveVarargs = JavaTypeCatalog.builder()
                .registerJavaTypeMethod(MethodProvider.class, "sumInts", int[].class)
                .build()
                .find(MethodProvider.class)
                .orElseThrow()
                .findMethod(primitiveVarargsSignature)
                .orElseThrow();
        assertThat(primitiveVarargs.invocationHandle()
                .invoke(new MethodProvider(), List.of(BigDecimal.ONE, BigDecimal.TEN)))
                .isEqualTo(new BigDecimal("11"));
    }

    @Test
    @DisplayName("arrays collections sets and iterables share collection member metadata")
    void sequentialJavaMembersShareCollectionMetadata() throws Throwable {
        JavaTypeDescriptor descriptor = JavaTypeCatalog.builder()
                .registerJavaTypeWithPublicMethods(SequentialMemberProvider.class)
                .build()
                .find(SequentialMemberProvider.class)
                .orElseThrow();
        CollectionType numbers = new CollectionType(ScalarType.NUMBER);
        CollectionType texts = new CollectionType(ScalarType.STRING);

        JavaMethodDescriptor array = descriptor.findMethod(signature("array")).orElseThrow();
        JavaMethodDescriptor iterable = descriptor.findMethod(signature("iterable")).orElseThrow();
        JavaMethodDescriptor set = descriptor.findMethod(signature("set", texts)).orElseThrow();
        JavaMethodDescriptor integerSet = descriptor.findMethod(signature("integerSet", numbers)).orElseThrow();

        assertThat(array.returnType()).isEqualTo(numbers);
        assertThat(iterable.returnType()).isEqualTo(texts);
        assertThat(set.returnType()).isEqualTo(texts);
        assertThat(array.invocationHandle().invoke(new SequentialMemberProvider()))
                .isEqualTo(List.of(BigDecimal.ONE, BigDecimal.TWO));
        assertThat(iterable.invocationHandle().invoke(new SequentialMemberProvider()))
                .isEqualTo(List.of("a", "b"));
        assertThat(set.invocationHandle().invoke(new SequentialMemberProvider(), List.of("a", "b")))
                .isEqualTo(List.of("a", "b"));
        assertThat(integerSet.invocationHandle()
                .invoke(new SequentialMemberProvider(), List.of(BigDecimal.ONE, BigDecimal.TWO)))
                .isEqualTo(List.of(BigDecimal.ONE, BigDecimal.TWO));
    }

    @Test
    @DisplayName("property and method members may share names")
    void propertyAndMethodMembersMayShareNames() {
        JavaTypeCatalog catalog = JavaTypeCatalog.builder()
                .registerJavaTypeMethod(CustomerRecord.class, "name")
                .build();

        JavaTypeDescriptor descriptor = catalog.find(CustomerRecord.class).orElseThrow();

        assertThat(descriptor.properties()).containsOnlyKeys("name", "score");
        assertThat(descriptor.findMethod(signature("name"))).isPresent();
    }

    @Test
    @DisplayName("registered Java member returns are never null by contract")
    void registeredJavaMemberReturnsAreNeverNullByContract() {
        assertThat(JavaTypeCatalog.registeredMemberReturnNullability()).isEqualTo(RuntimeNullability.NEVER_NULL);
    }

    @Test
    @DisplayName("unmappable properties fail catalog construction")
    void unmappablePropertiesFailCatalogConstruction() {
        assertThatThrownBy(() -> JavaTypeCatalog.builder()
                .registerJavaType(UnmappablePropertyProvider.class)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("unsupported Java member type: java.util.Optional");
    }

    @Test
    @DisplayName("explicit methods with unmappable returns fail catalog construction")
    void explicitMethodsWithUnmappableReturnsFailCatalogConstruction() {
        assertThatThrownBy(() -> JavaTypeCatalog.builder()
                .registerJavaTypeMethod(MethodProvider.class, "unsupportedReturn")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("unsupported Java member type: java.util.Optional");
    }

    @Test
    @DisplayName("lambda parameters are excluded from public methods and rejected when explicitly registered")
    void registeredJavaMethodsDoNotExposeLambdaParameters() {
        JavaTypeDescriptor publicMethods = JavaTypeCatalog.builder()
                .registerJavaTypeWithPublicMethods(LambdaMethodProvider.class)
                .build()
                .find(LambdaMethodProvider.class)
                .orElseThrow();

        assertThat(publicMethods.methods())
                .extracting(JavaMethodDescriptor::signature)
                .containsExactly(
                        signature("annotation", new ObjectType(SingleMemberAnnotation.class.getName())),
                        signature("sealed", new ObjectType(SealedContract.class.getName())));
        assertThatThrownBy(() -> JavaTypeCatalog.builder()
                .registerJavaTypeMethod(LambdaMethodProvider.class, "apply", Function.class, String.class)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("lambda parameters are not supported")
                .hasMessageContaining(Function.class.getName());
    }

    @Test
    @DisplayName("object member types require their own catalog registration for navigation")
    void objectMemberTypesRequireTheirOwnCatalogRegistrationForNavigation() {
        JavaTypeCatalog catalog = JavaTypeCatalog.builder()
                .registerJavaType(ObjectMemberProvider.class)
                .build();

        JavaPropertyDescriptor child = catalog.find(ObjectMemberProvider.class)
                .orElseThrow()
                .findProperty("child")
                .orElseThrow();

        assertThat(child.type()).isEqualTo(new ObjectType(UnregisteredChild.class.getName()));
        assertThat(catalog.find((ObjectType) child.type())).isEmpty();
    }

    @Test
    @DisplayName("duplicate property names fail catalog construction deterministically")
    void duplicatePropertyNamesFailCatalogConstructionDeterministically() {
        assertThatThrownBy(() -> JavaTypeCatalog.builder()
                .registerJavaType(DuplicatePropertyProvider.class)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("duplicate Java property name: active");
    }

    @Test
    @DisplayName("duplicate member method signatures fail catalog construction deterministically")
    void duplicateMemberMethodSignaturesFailCatalogConstructionDeterministically() {
        assertThatThrownBy(() -> JavaTypeCatalog.builder()
                .registerJavaTypeWithPublicMethods(DuplicateMethodProvider.class)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("duplicate Java member method signature");
    }

    @Test
    @DisplayName("registered Java type metadata is ordered deterministically")
    void registeredJavaTypeMetadataIsOrderedDeterministically() {
        ExpressionEnvironment first = ExpressionEnvironment.builder()
                .registerJavaTypeMethod(MethodProvider.class, "privatePrice", BigDecimal.class)
                .registerJavaType(CustomerBean.class)
                .build();
        ExpressionEnvironment sameContentDifferentOrder = ExpressionEnvironment.builder()
                .registerJavaType(CustomerBean.class)
                .registerJavaTypeMethod(MethodProvider.class, "privatePrice", BigDecimal.class)
                .build();
        assertThat(first.javaTypes().size()).isEqualTo(2);
        assertThat(first.javaTypes().values()).extracting(descriptor -> descriptor.javaType().getName())
                .containsExactlyElementsOf(sameContentDifferentOrder.javaTypes().values().stream()
                        .map(descriptor -> descriptor.javaType().getName())
                        .toList());
    }

    private static FunctionSignature signature(String languageName, ExpressionType... parameterTypes) {
        return new FunctionSignature(languageName, List.of(parameterTypes));
    }

    static final class CustomerBean {

        private final String name;
        private final boolean active;

        CustomerBean(String name, boolean active) {
            this.name = name;
            this.active = active;
        }

        public String getName() {
            return name;
        }

        public boolean isActive() {
            return active;
        }
    }

    record CustomerRecord(String name, BigDecimal score) {
    }

    record SameShapeRecord(String name, BigDecimal score) {
    }

    interface GenericProvider<T> {
        T value(T value);
    }

    static final class MethodProvider implements GenericProvider<String> {

        public BigDecimal calculate(BigDecimal value) {
            return value.add(BigDecimal.ONE);
        }

        public String label() {
            return "label";
        }

        @Override
        public String value(String value) {
            return value;
        }

        @Override
        public String toString() {
            return "method-provider";
        }

        public static BigDecimal staticPrice(BigDecimal value) {
            return value;
        }

        public void clear() {
        }

        public BigDecimal varargs(BigDecimal... values) {
            return values[0];
        }

        public String unsupported(Optional<String> value) {
            return value.orElse("");
        }

        public Optional<String> unsupportedReturn() {
            return Optional.empty();
        }

        public String character(char value) {
            return Character.toString(value);
        }

        public BigDecimal privatePrice(BigDecimal value) {
            return value.add(BigDecimal.TEN);
        }

        public int sumInts(int... values) {
            int sum = 0;
            for (int value : values) {
                sum += value;
            }
            return sum;
        }
    }

    static final class DuplicatePropertyProvider {

        public boolean getActive() {
            return true;
        }

        public boolean isActive() {
            return true;
        }
    }

    static final class UnmappablePropertyProvider {

        public Optional<String> getValue() {
            return Optional.empty();
        }
    }

    static final class ObjectMemberProvider {

        public UnregisteredChild getChild() {
            return new UnregisteredChild();
        }
    }

    static final class UnregisteredChild {
    }

    static final class LambdaMethodProvider {

        public String apply(Function<String, String> function, String value) {
            return function.apply(value);
        }

        public String annotation(SingleMemberAnnotation annotation) {
            return annotation.value();
        }

        public String sealed(SealedContract contract) {
            return contract.value();
        }
    }

    @interface SingleMemberAnnotation {
        String value();
    }

    sealed interface SealedContract permits SealedContractImpl {
        String value();
    }

    static final class SealedContractImpl implements SealedContract {
        @Override
        public String value() {
            return "value";
        }
    }

    static final class DuplicateMethodProvider {

        public BigDecimal same(BigDecimal value) {
            return value;
        }

        public int same(int value) {
            return value;
        }
    }

    static final class SequentialMemberProvider {

        public int[] array() {
            return new int[]{1, 2};
        }

        public Iterable<String> iterable() {
            return List.of("a", "b");
        }

        public Set<String> set(Set<String> values) {
            return values;
        }

        public Set<Integer> integerSet(Set<Integer> values) {
            return values;
        }
    }

    static final class WildcardChildProvider {

        public String getFirst() {
            return "first";
        }

        public String second() {
            return "second";
        }

        public String getNavigableOnly() {
            return "navigable";
        }
    }

    static final class InvalidWildcardChildProvider {

        public String text() {
            return "text";
        }

        public BigDecimal number() {
            return BigDecimal.ONE;
        }

        public Optional<String> unsupported() {
            return Optional.empty();
        }

        public String nullable() {
            return null;
        }
    }
}
