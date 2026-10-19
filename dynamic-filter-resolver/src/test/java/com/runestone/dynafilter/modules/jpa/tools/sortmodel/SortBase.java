package com.runestone.dynafilter.modules.jpa.tools.sortmodel;

import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class SortBase {
    @Id
    private Long id;
    private String inheritedName;

    protected SortBase() {
    }

    protected SortBase(Long id, String inheritedName) {
        this.id = id;
        this.inheritedName = inheritedName;
    }

    public Long getId() {
        return id;
    }
}
