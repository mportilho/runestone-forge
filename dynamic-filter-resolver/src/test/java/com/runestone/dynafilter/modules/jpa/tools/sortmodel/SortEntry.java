package com.runestone.dynafilter.modules.jpa.tools.sortmodel;

import jakarta.persistence.*;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.JoinFormula;

import java.util.List;
import java.util.UUID;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class SortEntry extends SortBase {
    @ManyToOne(fetch = FetchType.LAZY)
    private SortOwner owner;
    @Embedded
    private SortDetails details;
    @ElementCollection
    private List<String> tags;
    private UUID externalId;
    private String scope;
    private String labelStorage;
    @Convert(converter = SortValueConverter.class)
    private SortValue convertedValue;
    @Formula("lower(property_label)")
    private String computedName;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinFormula(value = "owner_id", referencedColumnName = "id")
    private SortOwner formulaOwner;
    @Lob
    private String lobText;
    private byte[] binaryValue;
    private Byte[] boxedBinary;
    @Lob
    private byte[] largeBinary;
    @Transient
    private String largeTextStorage;

    protected SortEntry() {
    }

    public SortEntry(Long id, String label, SortOwner owner, UUID externalId, String scope, List<String> tags) {
        super(id, label);
        this.labelStorage = label;
        this.convertedValue = new SortValue(label);
        this.owner = owner;
        this.details = new SortDetails(label);
        this.externalId = externalId;
        this.scope = scope;
        this.tags = tags;
    }

    @Access(AccessType.PROPERTY)
    @Column(name = "property_label")
    public String getPropertyName() {
        return labelStorage;
    }

    public void setPropertyName(String name) {
        labelStorage = name;
    }

    public SortOwner getOwner() {
        return owner;
    }

    @Access(AccessType.PROPERTY)
    @Lob
    public String getLargeText() {
        return largeTextStorage;
    }

    public void setLargeText(String text) {
        largeTextStorage = text;
    }
}
