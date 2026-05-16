package com.runestone.dynafilter.modules.jpa.tools.app.database.jpamodels;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "produto")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "produto_tipo", joinColumns = @JoinColumn(name = "id_produto"))
    @Column(name = "en_tipo", nullable = false)
    @Enumerated(EnumType.STRING)
    private final Set<TipoProduto> tipos = EnumSet.noneOf(TipoProduto.class);

    protected Produto() {
        // Required by JPA.
    }

    public Produto(String nome, Set<TipoProduto> tipos) {
        this.nome = Objects.requireNonNull(nome, "nome must not be null");
        this.tipos.addAll(Objects.requireNonNull(tipos, "tipos must not be null"));
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public Set<TipoProduto> getTipos() {
        return Collections.unmodifiableSet(tipos);
    }
}
