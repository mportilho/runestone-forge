package com.runestone.expeval_mk3.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExpressionEnvironmentProviderNominalContractTest {

    @Test
    @DisplayName("exact registered nominal objects preserve identity in both directions")
    void exactRegisteredNominalObjectsPreserveIdentityInBothDirections() throws Throwable {
        Customer customer = new Customer("stone");
        NominalProvider provider = new NominalProvider(customer);
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functionsFrom(provider, FunctionPurity.PURE)
                .registerJavaType(Customer.class)
                .build();
        ObjectType customerType = new ObjectType(Customer.class.getName());

        FunctionDescriptor descriptor = resolve(environment, "same", customerType);

        assertThat(descriptor.returnType()).isEqualTo(customerType);
        assertThat(descriptor.implementationHandle().invoke(customer)).isSameAs(customer);
        assertThat(resolve(environment, "customer").implementationHandle().invoke()).isSameAs(customer);
    }

    @Test
    @DisplayName("unregistered nominal provider contracts fail during environment build")
    void unregisteredNominalProviderContractsFailDuringEnvironmentBuild() {
        ExpressionEnvironment.Builder builder = ExpressionEnvironment.builder()
                .functionsFrom(new NominalProvider(new Customer("stone")), FunctionPurity.PURE);

        assertThatThrownBy(builder::build)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("registered Java type")
                .hasMessageContaining(Customer.class.getName());
    }

    @Test
    @DisplayName("nominal provider boundaries reject runtime subtypes")
    void nominalProviderBoundariesRejectRuntimeSubtypes() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functionsFrom(SubtypeProvider.class, FunctionPurity.PURE)
                .registerJavaType(Customer.class)
                .build();
        ObjectType customerType = new ObjectType(Customer.class.getName());

        assertThatThrownBy(() -> resolve(environment, "same", customerType)
                .implementationHandle().invoke(new PreferredCustomer("stone")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("exact runtime class")
                .hasMessageContaining(Customer.class.getName());
        assertThatThrownBy(() -> resolve(environment, "subtypeResult")
                .implementationHandle().invoke())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("exact runtime class")
                .hasMessageContaining(Customer.class.getName());
    }

    @Test
    @DisplayName("concrete collection classes use collection semantics unless registered as nominal objects")
    void collectionClassesChooseExactlyOneDeclaredSemantics() throws Throwable {
        ExpressionEnvironment collectionEnvironment = ExpressionEnvironment.builder()
                .functionsFrom(CollectionClassProvider.class, FunctionPurity.PURE)
                .build();
        ExpressionEnvironment nominalEnvironment = ExpressionEnvironment.builder()
                .functionsFrom(CollectionClassProvider.class, FunctionPurity.PURE)
                .registerJavaTypeWildcardChildren(GenericCollection.class, "size")
                .build();
        CollectionType collectionType = new CollectionType(ScalarType.STRING);
        ObjectType nominalType = new ObjectType(GenericCollection.class.getName());

        assertThat(resolve(collectionEnvironment, "values").returnType())
                .isEqualTo(collectionType);
        assertThat(resolve(collectionEnvironment, "values").implementationHandle().invoke())
                .isEqualTo(List.of("stone"));
        assertThat(resolve(nominalEnvironment, "values").returnType()).isEqualTo(nominalType);
        assertThat(resolve(nominalEnvironment, "values").implementationHandle().invoke())
                .isSameAs(CollectionClassProvider.VALUES);
        assertThat(nominalEnvironment.javaTypes().find(GenericCollection.class))
                .get()
                .satisfies(descriptor -> {
                    assertThat(descriptor.objectType()).isEqualTo(nominalType);
                    assertThat(descriptor.wildcardChildType()).contains(ScalarType.NUMBER);
                    assertThat(descriptor.wildcardChildren())
                            .extracting(JavaWildcardChildDescriptor::name)
                            .containsExactly("size");
                });
    }

    @Test
    @DisplayName("registered abstract and asynchronous types remain unsupported provider contracts")
    void registeredAbstractAndAsynchronousTypesRemainUnsupported() {
        assertThatThrownBy(() -> ExpressionEnvironment.builder()
                .functionsFrom(AbstractProvider.class, FunctionPurity.PURE)
                .registerJavaType(AbstractCustomer.class)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must be concrete")
                .hasMessageContaining(AbstractCustomer.class.getName());
        assertThatThrownBy(() -> ExpressionEnvironment.builder()
                .functionsFrom(AsyncProvider.class, FunctionPurity.PURE)
                .registerJavaType(RegisteredFuture.class)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("asynchronous")
                .hasMessageContaining(RegisteredFuture.class.getName());
        assertThatThrownBy(() -> ExpressionEnvironment.builder()
                .functionsFrom(PublisherProvider.class, FunctionPurity.PURE)
                .registerJavaType(RegisteredPublisher.class)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("asynchronous")
                .hasMessageContaining(RegisteredPublisher.class.getName());
        assertThatThrownBy(() -> ExpressionEnvironment.builder()
                .functionsFrom(VoidWrapperProvider.class, FunctionPurity.PURE)
                .registerJavaType(Void.class)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("void provider method returns are not supported");
    }

    private static FunctionDescriptor resolve(
            ExpressionEnvironment environment,
            String name,
            ExpressionType... parameterTypes) {
        return environment.functions()
                .find(new FunctionSignature(name, List.of(parameterTypes)))
                .orElseThrow();
    }

    public static class Customer {
        private final String name;

        public Customer(String name) {
            this.name = name;
        }

        public String name() {
            return name;
        }
    }

    public static final class PreferredCustomer extends Customer {
        public PreferredCustomer(String name) {
            super(name);
        }
    }

    public static final class NominalProvider {
        private final Customer customer;

        private NominalProvider(Customer customer) {
            this.customer = customer;
        }

        public Customer same(Customer value) {
            return value;
        }

        public Customer customer() {
            return customer;
        }
    }

    public static final class SubtypeProvider {
        public static Customer same(Customer value) {
            return value;
        }

        public static Customer subtypeResult() {
            return new PreferredCustomer("stone");
        }
    }

    public static final class CollectionClassProvider {
        private static final GenericCollection<String> VALUES = new GenericCollection<>(List.of("stone"));

        public static GenericCollection<String> values() {
            return VALUES;
        }
    }

    public static final class GenericCollection<T> extends AbstractCollection<T> {
        private final List<T> values;

        private GenericCollection(List<T> values) {
            this.values = List.copyOf(values);
        }

        @Override
        public Iterator<T> iterator() {
            return values.iterator();
        }

        @Override
        public int size() {
            return values.size();
        }
    }

    public abstract static class AbstractCustomer {
    }

    public static final class AbstractProvider {
        public static AbstractCustomer same(AbstractCustomer value) {
            return value;
        }
    }

    public static final class RegisteredFuture implements java.util.concurrent.Future<String> {
        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return true;
        }

        @Override
        public String get() {
            return "stone";
        }

        @Override
        public String get(long timeout, java.util.concurrent.TimeUnit unit) {
            return "stone";
        }
    }

    public static final class AsyncProvider {
        public static RegisteredFuture same(RegisteredFuture value) {
            return value;
        }
    }

    public static final class RegisteredPublisher implements java.util.concurrent.Flow.Publisher<String> {
        @Override
        public void subscribe(java.util.concurrent.Flow.Subscriber<? super String> subscriber) {
            subscriber.onComplete();
        }
    }

    public static final class PublisherProvider {
        public static RegisteredPublisher same(RegisteredPublisher value) {
            return value;
        }
    }

    public static final class VoidWrapperProvider {
        public static Void value() {
            return null;
        }
    }
}
