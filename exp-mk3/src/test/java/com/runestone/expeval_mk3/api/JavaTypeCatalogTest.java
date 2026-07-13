package com.runestone.expeval_mk3.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

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
    @DisplayName("registered Java type metadata contributes stably to environment identity")
    void registeredJavaTypeMetadataContributesStablyToEnvironmentIdentity() {
        ExpressionEnvironment first = ExpressionEnvironment.builder()
                .registerJavaTypeMethod(MethodProvider.class, "privatePrice", BigDecimal.class)
                .registerJavaType(CustomerBean.class)
                .build();
        ExpressionEnvironment sameContentDifferentOrder = ExpressionEnvironment.builder()
                .registerJavaType(CustomerBean.class)
                .registerJavaTypeMethod(MethodProvider.class, "privatePrice", BigDecimal.class)
                .build();
        ExpressionEnvironment withoutJavaTypes = ExpressionEnvironment.builder().build();
        ExpressionEnvironment withPublicMethods = ExpressionEnvironment.builder()
                .registerJavaType(CustomerBean.class)
                .registerJavaTypeWithPublicMethods(MethodProvider.class)
                .build();

        assertThat(first.javaTypes().size()).isEqualTo(2);
        assertThat(first.environmentId()).isEqualTo(sameContentDifferentOrder.environmentId());
        assertThat(first.environmentId()).isNotEqualTo(withoutJavaTypes.environmentId());
        assertThat(first.environmentId()).isNotEqualTo(withPublicMethods.environmentId());
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

    static final class DuplicateMethodProvider {

        public BigDecimal same(BigDecimal value) {
            return value;
        }

        public int same(int value) {
            return value;
        }
    }
}
