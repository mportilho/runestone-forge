package com.runestone.dynafilter.core.generator;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TestSortContract {

    @Test
    void reusableConstructionSnapshotsPathsAliasesAndOptionalRootWithoutChangingPreviousContracts() {
        String[] aliases = {"publicName"};
        var builder = SortContract.builder().include("name", aliases);
        var first = builder.build();
        aliases[0] = "internalName";
        var second = builder.root(String.class).include("id").build();

        assertThat(first.declaredRoot()).isNull();
        assertThat(second.declaredRoot()).isEqualTo(String.class);
        assertThat(first.candidates()).extracting(SortContract.Candidate::path).containsExactly("name");
        assertThat(first.candidates().getFirst().aliases()).containsExactly("publicName");
        assertThat(second.candidates()).extracting(SortContract.Candidate::path).containsExactly("name", "id");
        assertThat(second.candidates().getFirst()).isSameAs(first.candidates().getFirst());
        assertThat(second.candidates()).allSatisfy(candidate -> assertThat(candidate.explicit()).isTrue());
        assertThatThrownBy(() -> first.candidates().clear()).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> first.candidates().getFirst().aliases().clear()).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void directConstructionSnapshotsCollectionsAndRootParticipatesInContractEquality() {
        var aliases = new ArrayList<>(List.of("publicName"));
        var candidates = new ArrayList<>(List.of(new SortContract.Candidate("name", aliases, true)));
        var contract = new SortContract(candidates, String.class);
        aliases.clear();
        candidates.clear();

        assertThat(contract).isEqualTo(SortContract.builder().root(String.class).include("name", "publicName").build())
                .isNotEqualTo(SortContract.builder().root(Object.class).include("name", "publicName").build())
                .isNotEqualTo(SortContract.builder().include("name", "publicName").build());
        assertThat(contract.candidates().getFirst().aliases()).containsExactly("publicName");
    }

    @Test
    void nullDeclarationsFailImmediatelyWithoutChangingTheReusableBuilder() {
        var builder = SortContract.builder().include("name", "publicName");
        var original = builder.build();
        assertThatThrownBy(() -> builder.root(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> builder.include(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> builder.include("id", (String[]) null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> builder.include("id", (String) null)).isInstanceOf(NullPointerException.class);
        assertThat(builder.build()).isEqualTo(original);
    }
}
