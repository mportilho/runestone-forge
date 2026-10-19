package com.runestone.dynafilter.modules.jpa.tools.sortmodel;

import jakarta.persistence.Entity;

import java.util.List;
import java.util.UUID;

@Entity
public class SpecialSortEntry extends SortEntry {
    protected SpecialSortEntry() {
    }

    public SpecialSortEntry(Long id, String label, SortOwner owner, UUID externalId, String scope, List<String> tags) {
        super(id, label, owner, externalId, scope, tags);
    }
}
