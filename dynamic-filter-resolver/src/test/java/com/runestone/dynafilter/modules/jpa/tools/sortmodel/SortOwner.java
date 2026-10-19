package com.runestone.dynafilter.modules.jpa.tools.sortmodel;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;

import java.util.Set;

@Entity
public class SortOwner {
    @Id
    private Long id;
    private String name;
    @OneToMany(mappedBy = "owner")
    private Set<SortEntry> entries;
    @Transient
    private String manager;
    @Transient
    private SortOwner managerStorage;

    protected SortOwner() {
    }

    public SortOwner(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Access(AccessType.PROPERTY)
    @ManyToOne(fetch = FetchType.LAZY)
    public SortOwner getManager() {
        return managerStorage;
    }

    public void setManager(SortOwner manager) {
        managerStorage = manager;
    }
}
