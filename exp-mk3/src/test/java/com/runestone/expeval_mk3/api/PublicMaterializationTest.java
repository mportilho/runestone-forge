package com.runestone.expeval_mk3.api;

import com.runestone.expeval_mk3.internal.runtime.PublicMaterialization;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Direct tests of the shared Public Materialization boundary. The current M1 language surface always
 * produces already-sanitized values (boundary coercion and provider adapters reject null elements and
 * oversized containers before a plan ever sees them), so the defense-in-depth cases below - null and
 * nested-overflow rejection - are exercised straight against the shared materializer rather than through
 * a DSL expression that cannot reach them yet.
 */
class PublicMaterializationTest {

    @Test
    void scalarsMaterializeAsIsPreservingDecimalScale() {
        BigDecimal scaled = new BigDecimal("1.50");

        Object materialized = PublicMaterialization.materialize(scaled, ScalarType.NUMBER, 10, null);

        assertThat(materialized).isSameAs(scaled);
        assertThat(((BigDecimal) materialized).toPlainString()).isEqualTo("1.50");
    }

    @Test
    void collectionsMaterializeAsImmutableOrderedSnapshots() {
        List<BigDecimal> source = new ArrayList<>(List.of(new BigDecimal("1"), new BigDecimal("2")));

        Object materialized = PublicMaterialization.materialize(
                source, new CollectionType(ScalarType.NUMBER), 10, null);

        assertThat(materialized).isEqualTo(List.of(new BigDecimal("1"), new BigDecimal("2")));
        assertThatThrownBy(() -> ((List<Object>) materialized).add(BigDecimal.TEN))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void collectionSnapshotIsDetachedFromTheSourceContainer() {
        List<BigDecimal> source = new ArrayList<>(List.of(new BigDecimal("1")));

        List<Object> materialized = (List<Object>) PublicMaterialization.materialize(
                source, new CollectionType(ScalarType.NUMBER), 10, null);
        source.add(new BigDecimal("2"));

        assertThat(materialized).containsExactly(new BigDecimal("1"));
    }

    @Test
    void mapsMaterializeWithTextualKeysInCanonicalOrderAndAreImmutable() {
        Map<String, Object> source = new LinkedHashMap<>();
        source.put("zeta", new BigDecimal("1"));
        source.put("alpha", new BigDecimal("2"));

        Object materialized = PublicMaterialization.materialize(source, new MapType(ScalarType.NUMBER), 10, null);

        assertThat(((Map<String, Object>) materialized).keySet()).containsExactly("alpha", "zeta");
        assertThatThrownBy(() -> ((Map<String, Object>) materialized).put("beta", BigDecimal.ONE))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void nestedContainersMaterializeRecursivelyAndAreImmutableAtEveryLevel() {
        List<Object> source = List.of(List.of(new BigDecimal("1"), new BigDecimal("2")));

        Object materialized = PublicMaterialization.materialize(
                source, new CollectionType(new CollectionType(ScalarType.NUMBER)), 10, null);

        List<Object> outer = (List<Object>) materialized;
        assertThat(outer).hasSize(1);
        List<Object> inner = (List<Object>) outer.get(0);
        assertThatThrownBy(() -> inner.add(BigDecimal.TEN)).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void mapsNestedInsideCollectionsMaterializeRecursivelyAndAreImmutable() {
        Map<String, Object> innerSource = new LinkedHashMap<>();
        innerSource.put("zeta", new BigDecimal("1"));
        innerSource.put("alpha", new BigDecimal("2"));
        List<Object> source = List.of(innerSource);

        Object materialized = PublicMaterialization.materialize(
                source, new CollectionType(new MapType(ScalarType.NUMBER)), 10, null);

        List<Object> outer = (List<Object>) materialized;
        Map<String, Object> inner = (Map<String, Object>) outer.get(0);
        assertThat(inner.keySet()).containsExactly("alpha", "zeta");
        assertThatThrownBy(() -> inner.put("beta", BigDecimal.ONE)).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void nullElementIsRejectedEvenWhenTheOuterContainerIsWithinLimit() {
        List<Object> source = new ArrayList<>();
        source.add(null);

        assertThatThrownBy(() -> PublicMaterialization.materialize(
                source, new CollectionType(ScalarType.NUMBER), 10, null))
                .isInstanceOf(ExpressionExecutionException.class);
    }

    @Test
    void nullMapValueIsRejected() {
        Map<String, Object> source = new LinkedHashMap<>();
        source.put("key", null);

        assertThatThrownBy(() -> PublicMaterialization.materialize(source, new MapType(ScalarType.NUMBER), 10, null))
                .isInstanceOf(ExpressionExecutionException.class);
    }

    @Test
    void nullTopLevelValueIsRejected() {
        assertThatThrownBy(() -> PublicMaterialization.materialize(null, ScalarType.NUMBER, 10, null))
                .isInstanceOf(ExpressionExecutionException.class);
    }

    @Test
    void nestedOverflowIsRejectedEvenWhenTheOuterContainerIsWithinLimit() {
        List<Object> inner = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            inner.add(new BigDecimal(i));
        }
        List<Object> outer = List.of(inner);

        assertThatThrownBy(() -> PublicMaterialization.materialize(
                outer, new CollectionType(new CollectionType(ScalarType.NUMBER)), 3, null))
                .isInstanceOf(ExpressionExecutionException.class);
    }

    @Test
    void objectTypeIsRejected() {
        assertThatThrownBy(() -> PublicMaterialization.materialize(
                new Object(), new ObjectType(Object.class.getName()), 10, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void isPubliclyExposableRejectsObjectTypeDirectlyAndNested() {
        assertThat(PublicMaterialization.isPubliclyExposable(ScalarType.NUMBER)).isTrue();
        assertThat(PublicMaterialization.isPubliclyExposable(new CollectionType(ScalarType.STRING))).isTrue();
        assertThat(PublicMaterialization.isPubliclyExposable(new MapType(ScalarType.BOOLEAN))).isTrue();

        ObjectType objectType = new ObjectType("com.example.Thing");
        assertThat(PublicMaterialization.isPubliclyExposable(objectType)).isFalse();
        assertThat(PublicMaterialization.isPubliclyExposable(new CollectionType(objectType))).isFalse();
        assertThat(PublicMaterialization.isPubliclyExposable(new MapType(objectType))).isFalse();
        assertThat(PublicMaterialization.isPubliclyExposable(new CollectionType(new MapType(objectType)))).isFalse();
    }
}
