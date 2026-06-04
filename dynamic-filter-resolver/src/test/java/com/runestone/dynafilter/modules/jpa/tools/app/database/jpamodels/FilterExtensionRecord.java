/*
 * MIT License
 * <p>
 * Copyright (c) 2023-2023 Marcelo Silva Portilho
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.runestone.dynafilter.modules.jpa.tools.app.database.jpamodels;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "filter_extension_record")
public class FilterExtensionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;

    private String name;

    private String description;

    private LocalDate effectiveStart;
    private LocalDate effectiveEnd;
    private LocalDateTime createdAt;
    private Instant publishedAt;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "filter_extension_record_tag", joinColumns = @JoinColumn(name = "record_id"))
    @Column(name = "tag")
    private Set<String> tags;

    protected FilterExtensionRecord() {
    }

    public FilterExtensionRecord(
            String code,
            String name,
            String description,
            Set<String> tags,
            LocalDate effectiveStart,
            LocalDate effectiveEnd,
            LocalDateTime createdAt,
            Instant publishedAt
    ) {
        this.code = Objects.requireNonNull(code, "code cannot be null");
        this.name = name;
        this.description = description;
        this.tags = tags == null ? null : new HashSet<>(tags);
        this.effectiveStart = effectiveStart;
        this.effectiveEnd = effectiveEnd;
        this.createdAt = createdAt;
        this.publishedAt = publishedAt;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Set<String> getTags() {
        return tags;
    }

    public LocalDate getEffectiveStart() {
        return effectiveStart;
    }

    public LocalDate getEffectiveEnd() {
        return effectiveEnd;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        FilterExtensionRecord other = (FilterExtensionRecord) obj;
        return getId() != null && Objects.equals(id, other.id);
    }
}
