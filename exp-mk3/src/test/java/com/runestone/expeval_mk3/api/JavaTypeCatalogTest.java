package com.runestone.expeval_mk3.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JavaTypeCatalogTest {

    @Test
    @DisplayName("Java type registration creates nominal object type metadata")
    void javaTypeRegistrationCreatesNominalObjectTypeMetadata() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .registerJavaType(JavaTypeCatalog.registerJavaType(CustomerBean.class, "Customer").build())
                .registerJavaType(JavaTypeCatalog.registerJavaType(AccountBean.class, "Account").build())
                .build();

        JavaTypeCatalog.RegisteredJavaType customerType = environment.javaTypeCatalog()
                .find(new ObjectType("Customer"))
                .orElseThrow();
        JavaTypeCatalog.RegisteredJavaType accountType = environment.javaTypeCatalog()
                .find(new ObjectType("Account"))
                .orElseThrow();

        assertThat(customerType.objectType()).isEqualTo(new ObjectType("Customer"));
        assertThat(accountType.objectType()).isEqualTo(new ObjectType("Account"));
        assertThat(customerType.objectType()).isNotEqualTo(accountType.objectType());
        assertThat(customerType.properties().keySet()).isEqualTo(accountType.properties().keySet());
    }

    @Test
    @DisplayName("properties are exposed from JavaBean getters and record accessors")
    void propertiesAreExposedFromJavaBeanGettersAndRecordAccessors() {
        JavaTypeCatalog catalog = JavaTypeCatalog.builder()
                .registerJavaType(JavaTypeCatalog.registerJavaType(CustomerBean.class, "Customer").build())
                .registerJavaType(JavaTypeCatalog.registerJavaType(AddressRecord.class, "Address").build())
                .build();

        JavaTypeCatalog.RegisteredJavaType customerType = catalog.find(new ObjectType("Customer")).orElseThrow();
        JavaTypeCatalog.RegisteredJavaType addressType = catalog.find(new ObjectType("Address")).orElseThrow();

        assertThat(customerType.properties())
                .containsOnlyKeys("active", "name")
                .extractingByKey("name")
                .extracting(JavaTypeCatalog.PropertyMember::returnType)
                .isEqualTo(ScalarType.STRING);
        assertThat(customerType.properties().get("active").returnType()).isEqualTo(ScalarType.BOOLEAN);
        assertThat(addressType.properties())
                .containsOnlyKeys("city", "zipCode")
                .extractingByKey("zipCode")
                .extracting(JavaTypeCatalog.PropertyMember::returnType)
                .isEqualTo(ScalarType.NUMBER);
    }

    @Test
    @DisplayName("methods are exposed from explicit registrations and optional public method discovery")
    void methodsAreExposedFromExplicitRegistrationsAndOptionalPublicMethodDiscovery() throws NoSuchMethodException {
        Method explicitVarargs = MethodFixture.class.getMethod("join", String[].class);
        JavaTypeCatalog.RegisteredJavaType type = JavaTypeCatalog.registerJavaType(MethodFixture.class, "Fixture")
                .method("join", explicitVarargs, ScalarType.STRING, List.of(new VectorType(ScalarType.STRING)))
                .includePublicMethods()
                .build();

        assertThat(type.methods())
                .containsKeys(
                        FunctionSignature.of("displayName", List.of()),
                        FunctionSignature.of("score", List.of(ScalarType.NUMBER)),
                        FunctionSignature.of("convert", List.of(ScalarType.STRING)),
                        FunctionSignature.of("join", List.of(new VectorType(ScalarType.STRING))))
                .doesNotContainKeys(
                        FunctionSignature.of("clear", List.of()),
                        FunctionSignature.of("staticName", List.of()),
                        FunctionSignature.of("unsupportedParameter", List.of(UnknownType.INSTANCE)),
                        FunctionSignature.of("toString", List.of()));

        assertThat(type.methods().get(FunctionSignature.of("score", List.of(ScalarType.NUMBER))).returnType())
                .isEqualTo(ScalarType.NUMBER);
    }

    @Test
    @DisplayName("property exposure can be controlled explicitly")
    void propertyExposureCanBeControlledExplicitly() throws NoSuchMethodException {
        Method nameAccessor = CustomerBean.class.getMethod("getName");

        JavaTypeCatalog.RegisteredJavaType type = JavaTypeCatalog.registerJavaType(CustomerBean.class, "Customer")
                .withoutProperties()
                .property("name", nameAccessor, ScalarType.STRING)
                .build();

        assertThat(type.properties())
                .containsOnlyKeys("name")
                .extractingByKey("name")
                .extracting(JavaTypeCatalog.PropertyMember::accessor)
                .isEqualTo(nameAccessor);
    }

    @Test
    @DisplayName("public metadata rejects inconsistent property and method declarations")
    void publicMetadataRejectsInconsistentPropertyAndMethodDeclarations() throws NoSuchMethodException {
        Method nameAccessor = CustomerBean.class.getMethod("getName");
        Method scoreMethod = MethodFixture.class.getMethod("score", BigDecimal.class);
        JavaTypeCatalog.PropertyMember property = new JavaTypeCatalog.PropertyMember(
                "name",
                nameAccessor,
                ScalarType.STRING);
        JavaTypeCatalog.MethodMember method = new JavaTypeCatalog.MethodMember(
                FunctionSignature.of("score", List.of(ScalarType.NUMBER)),
                scoreMethod,
                ScalarType.NUMBER,
                true);
        Map<String, JavaTypeCatalog.PropertyMember> nullPropertyKey = new HashMap<>();
        nullPropertyKey.put(null, property);
        Map<FunctionSignature, JavaTypeCatalog.MethodMember> nullMethodKey = new HashMap<>();
        nullMethodKey.put(null, method);

        assertThatThrownBy(() -> JavaTypeCatalog.registerJavaType(MethodFixture.class, "Fixture")
                .method("score", scoreMethod, ScalarType.NUMBER, List.of())
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("arity");
        assertThatThrownBy(() -> JavaTypeCatalog.registerJavaType(MethodFixture.class, "Fixture")
                .method("score", scoreMethod, ScalarType.NUMBER, null)
                .build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("parameterTypes");
        assertThatThrownBy(() -> new JavaTypeCatalog.RegisteredJavaType(
                CustomerBean.class,
                new ObjectType("Customer"),
                Map.of("wrong", property),
                Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("property key");
        assertThatThrownBy(() -> new JavaTypeCatalog.RegisteredJavaType(
                MethodFixture.class,
                new ObjectType("Fixture"),
                Map.of(),
                Map.of(FunctionSignature.of("wrong", List.of(ScalarType.NUMBER)), method)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("method key");
        assertThatThrownBy(() -> new JavaTypeCatalog.RegisteredJavaType(
                AccountBean.class,
                new ObjectType("Account"),
                Map.of("name", property),
                Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("member")
                .hasMessageContaining(AccountBean.class.getName());
        assertThatThrownBy(() -> new JavaTypeCatalog.RegisteredJavaType(
                AccountBean.class,
                new ObjectType("Account"),
                Map.of(),
                Map.of(method.signature(), method)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("member")
                .hasMessageContaining(AccountBean.class.getName());
        assertThatThrownBy(() -> new JavaTypeCatalog.RegisteredJavaType(
                CustomerBean.class,
                new ObjectType("Customer"),
                nullPropertyKey,
                Map.of()))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("propertyName");
        assertThatThrownBy(() -> new JavaTypeCatalog.RegisteredJavaType(
                MethodFixture.class,
                new ObjectType("Fixture"),
                Map.of(),
                nullMethodKey))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("methodSignature");
    }

    @Test
    @DisplayName("property and method members may share a name")
    void propertyAndMethodMembersMayShareName() throws NoSuchMethodException {
        Method method = AddressRecord.class.getMethod("city", String.class);

        JavaTypeCatalog.RegisteredJavaType type = JavaTypeCatalog.registerJavaType(AddressRecord.class, "Address")
                .method("city", method, ScalarType.STRING, List.of(ScalarType.STRING))
                .build();

        assertThat(type.properties()).containsKey("city");
        assertThat(type.methods()).containsKey(FunctionSignature.of("city", List.of(ScalarType.STRING)));
    }

    @Test
    @DisplayName("duplicate property names and member function signatures are rejected")
    void duplicatePropertyNamesAndMemberFunctionSignaturesAreRejected() {
        assertThatThrownBy(() -> JavaTypeCatalog.registerJavaType(DuplicatePropertyBean.class, "DuplicateProperty")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("duplicate property")
                .hasMessageContaining("name");

        assertThatThrownBy(() -> JavaTypeCatalog.registerJavaType(DuplicateMethodBean.class, "DuplicateMethod")
                .includePublicMethods()
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("duplicate member function")
                .hasMessageContaining("value");
    }

    @Test
    @DisplayName("registered Java type metadata contributes stably to the environment id")
    void registeredJavaTypeMetadataContributesStablyToEnvironmentId() {
        JavaTypeCatalog.RegisteredJavaType customer = JavaTypeCatalog.registerJavaType(CustomerBean.class, "Customer").build();
        JavaTypeCatalog.RegisteredJavaType account = JavaTypeCatalog.registerJavaType(AccountBean.class, "Account").build();

        ExpressionEnvironment first = ExpressionEnvironment.builder()
                .registerJavaType(customer)
                .registerJavaType(account)
                .build();
        ExpressionEnvironment sameContentDifferentOrder = ExpressionEnvironment.builder()
                .registerJavaType(account)
                .registerJavaType(customer)
                .build();
        ExpressionEnvironment differentNominalType = ExpressionEnvironment.builder()
                .registerJavaType(JavaTypeCatalog.registerJavaType(CustomerBean.class, "PreferredCustomer").build())
                .registerJavaType(account)
                .build();
        ExpressionEnvironment differentExposure = ExpressionEnvironment.builder()
                .registerJavaType(JavaTypeCatalog.registerJavaType(CustomerBean.class, "Customer")
                        .includePublicMethods()
                        .build())
                .registerJavaType(account)
                .build();

        assertThat(first.environmentId()).isEqualTo(sameContentDifferentOrder.environmentId());
        assertThat(first.environmentId()).isNotEqualTo(differentNominalType.environmentId());
        assertThat(first.environmentId()).isNotEqualTo(differentExposure.environmentId());
    }

    public static final class CustomerBean {

        public String getName() {
            return "Ada";
        }

        public boolean isActive() {
            return true;
        }
    }

    public static final class AccountBean {

        public String getName() {
            return "main";
        }

        public boolean isActive() {
            return true;
        }
    }

    public record AddressRecord(String city, int zipCode) {

        public String city(String fallback) {
            return city == null ? fallback : city;
        }
    }

    public interface GenericConverter<T> {

        T convert(T value);
    }

    public static final class MethodFixture implements GenericConverter<String> {

        public String displayName() {
            return "fixture";
        }

        public BigDecimal score(BigDecimal amount) {
            return amount;
        }

        @Override
        public String convert(String value) {
            return value;
        }

        public void clear() {
        }

        public static String staticName() {
            return "static";
        }

        public String unsupportedParameter(Thread thread) {
            return thread.getName();
        }

        public String join(String... values) {
            return String.join(",", values);
        }

        @Override
        public String toString() {
            return "fixture";
        }
    }

    public static final class DuplicatePropertyBean {

        public String getName() {
            return "text";
        }

        public boolean isName() {
            return true;
        }
    }

    public static final class DuplicateMethodBean {

        public String value(int value) {
            return Integer.toString(value);
        }

        public String value(long value) {
            return Long.toString(value);
        }
    }
}
