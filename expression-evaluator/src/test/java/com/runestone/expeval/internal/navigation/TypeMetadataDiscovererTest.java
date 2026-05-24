package com.runestone.expeval.internal.navigation;

import com.runestone.expeval.catalog.TypeMetadata;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandle;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Type metadata discovery policies")
class TypeMetadataDiscovererTest {

    @Test
    @DisplayName("public metadata exposes only public getters, fields, and methods")
    void shouldDiscoverOnlyPublicMembersForTypeHintMetadata() {
        TypeMetadata metadata = TypeMetadataDiscoverer.discoverPublicMetadata(PublicMetadataFixture.class, List.of());

        assertThat(metadata.properties())
                .containsKeys("name", "publicField")
                .doesNotContainKeys("packageField", "secret");
        assertThat(metadata.methods())
                .containsKey("greet")
                .doesNotContainKeys("packageMethod", "secretMethod");
    }

    @Test
    @DisplayName("runtime fallback discovers declared getters, fields, and methods")
    void shouldDiscoverDeclaredMembersForRuntimeFallback() throws Throwable {
        RuntimeFallbackFixture fixture = new RuntimeFallbackFixture();

        Map<String, MethodHandle> properties = TypeMetadataDiscoverer.discoverRuntimePropertyHandles(
                RuntimeFallbackFixture.class);
        Map<String, Map<Integer, MethodHandle>> methods = TypeMetadataDiscoverer.discoverRuntimeMethodHandles(
                RuntimeFallbackFixture.class);

        assertThat(properties).containsKeys("secret", "packageField");
        assertThat((String) properties.get("secret").invoke(fixture)).isEqualTo("getter-secret");
        assertThat((String) properties.get("packageField").invoke(fixture)).isEqualTo("field-secret");
        assertThat((String) methods.get("packageMethod").get(0).invoke(fixture)).isEqualTo("method-secret");
    }

    static class PublicMetadataFixture {
        public String publicField = "public";
        String packageField = "package";

        public String getName() {
            return "name";
        }

        String getSecret() {
            return "secret";
        }

        public String greet(String suffix) {
            return "hello " + suffix;
        }

        String packageMethod() {
            return "package";
        }

        private String secretMethod() {
            return "secret";
        }
    }

    static class RuntimeFallbackFixture {
        String packageField = "field-secret";

        String getSecret() {
            return "getter-secret";
        }

        String packageMethod() {
            return "method-secret";
        }
    }
}
