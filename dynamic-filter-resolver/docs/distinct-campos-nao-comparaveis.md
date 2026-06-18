# Distinct com campos nao comparaveis

Este documento descreve uma proposta para evitar a aplicacao automatica de `DISTINCT` em consultas JPA quando a entidade selecionada possui campos que nao podem ser usados como chave de comparacao no banco de dados.

O caso que motivou esta proposta foi um erro no Oracle causado por `SELECT DISTINCT` em uma consulta que tambem selecionava colunas `CLOB`. No Oracle, campos `CLOB` nao podem participar de operacoes que exigem comparacao, como `DISTINCT`, `GROUP BY`, `ORDER BY`, `UNION` e operacoes similares.

## Contexto atual

`JpaPaths` aplica `query.distinct(true)` quando a resolucao de um path cruza uma associacao plural:

```java
public static void applyDistinctIfNeeded(ResolvedJpaPath<?> path, CriteriaQuery<?> query) {
    if (path.crossedPluralAssociation()) {
        query.distinct(true);
    }
}
```

Esta decisao evita duplicidades causadas por `JOIN` com colecoes, mas pode gerar SQL invalido quando o `SELECT DISTINCT root` expande a entidade raiz para colunas nao comparaveis.

## Proposta

A decisao de aplicar `DISTINCT` pode ser feita em duas etapas:

1. Continuar considerando `DISTINCT` apenas quando a consulta cruzar uma associacao plural.
2. Antes de chamar `query.distinct(true)`, verificar se a entidade selecionada possui algum atributo basico nao comparavel.

O problema nao esta no path filtrado em si, mas na entidade selecionada pelo `CriteriaQuery`. Por isso, a verificacao deve considerar o `Root<?>` da consulta.

Uma assinatura possivel seria:

```java
public static void applyDistinctIfNeeded(
        ResolvedJpaPath<?> path,
        Root<?> root,
        CriteriaQuery<?> query
) {
    Objects.requireNonNull(path, "path cannot be null");
    Objects.requireNonNull(root, "root cannot be null");
    Objects.requireNonNull(query, "query cannot be null");

    if (path.crossedPluralAssociation() && canApplyDistinct(root)) {
        query.distinct(true);
    }
}
```

Os metodos que ja recebem `Root<?>` e `CriteriaQuery<?>` passariam a chamar o novo overload:

```java
public static <T> ResolvedJpaPath<T> resolveAttributePath(
        String path,
        FilterData filterData,
        Root<?> root,
        CriteriaQuery<?> query
) {
    Objects.requireNonNull(query, "query cannot be null");
    ResolvedJpaPath<T> resolvedPath = resolveAttributePath(path, filterData, root);
    applyDistinctIfNeeded(resolvedPath, root, query);
    return resolvedPath;
}
```

O mesmo ajuste se aplica a `resolveAttributeJoinPath`.

Se for necessario preservar compatibilidade com chamadas externas existentes, o metodo atual pode ser mantido com o comportamento antigo:

```java
public static void applyDistinctIfNeeded(ResolvedJpaPath<?> path, CriteriaQuery<?> query) {
    if (path.crossedPluralAssociation()) {
        query.distinct(true);
    }
}
```

## Cache da verificacao

Como o metamodel JPA e fixo depois do bootstrap da aplicacao, a verificacao pode ser cacheada por classe de entidade.

```java
private static final Map<Class<?>, Boolean> DISTINCT_COMPARABLE_ENTITY_CACHE = Caffeine.newBuilder()
        .maximumSize(1024)
        .executor(Runnable::run)
        .<Class<?>, Boolean>build()
        .asMap();
```

A semantica recomendada para o cache e positiva: `true` significa que a entidade pode usar `DISTINCT` com seguranca.

```java
private static boolean canApplyDistinct(Root<?> root) {
    return DISTINCT_COMPARABLE_ENTITY_CACHE.computeIfAbsent(
            root.getJavaType(),
            ignored -> !hasNonComparableAttribute(root.getModel())
    );
}
```

Esta abordagem evita repetir a introspeccao dos metadados JPA para a mesma entidade em cada filtro aplicado.

## Deteccao de atributos nao comparaveis

A verificacao pode usar o JPA Metamodel e sinais simples na anotacao ou no tipo Java do atributo.

Imports provaveis:

```java
import jakarta.persistence.Column;
import jakarta.persistence.Lob;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.ManagedType;
import jakarta.persistence.metamodel.SingularAttribute;

import java.lang.reflect.AnnotatedElement;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.NClob;
import java.util.Locale;
```

Verificacao principal:

```java
private static boolean hasNonComparableAttribute(ManagedType<?> managedType) {
    for (Attribute<?, ?> attribute : managedType.getAttributes()) {
        if (isNonComparableAttribute(attribute)) {
            return true;
        }

        if (attribute instanceof SingularAttribute<?, ?> singularAttribute
                && singularAttribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.EMBEDDED
                && singularAttribute.getType() instanceof ManagedType<?> embeddedType
                && hasNonComparableAttribute(embeddedType)) {
            return true;
        }
    }
    return false;
}
```

Deteccao local do atributo:

```java
private static boolean isNonComparableAttribute(Attribute<?, ?> attribute) {
    if (attribute.isAssociation()) {
        return false;
    }

    Class<?> javaType = attribute.getJavaType();
    if (Clob.class.isAssignableFrom(javaType)
            || NClob.class.isAssignableFrom(javaType)
            || Blob.class.isAssignableFrom(javaType)) {
        return true;
    }

    if (!(attribute.getJavaMember() instanceof AnnotatedElement member)) {
        return false;
    }

    if (member.isAnnotationPresent(Lob.class)) {
        return true;
    }

    Column column = member.getAnnotation(Column.class);
    return column != null && containsNonComparableColumnDefinition(column.columnDefinition());
}
```

Deteccao por `columnDefinition`:

```java
private static boolean containsNonComparableColumnDefinition(String columnDefinition) {
    if (columnDefinition == null || columnDefinition.isBlank()) {
        return false;
    }

    String normalized = columnDefinition.toUpperCase(Locale.ROOT);
    return normalized.contains("CLOB")
            || normalized.contains("NCLOB")
            || normalized.contains("BLOB")
            || normalized.contains("LONG");
}
```

## Cuidados

Nao aplicar `DISTINCT` evita a falha SQL, mas pode permitir duplicidades no resultado quando a consulta faz `JOIN` com colecao. Esta proposta troca uma excecao de banco por uma possivel duplicidade de registros.

Se a duplicidade for inaceitavel para alguns casos de uso, uma alternativa mais explicita e tornar a estrategia configuravel, por exemplo:

1. `SKIP_DISTINCT_WHEN_NON_COMPARABLE`: nao aplica `DISTINCT` quando a entidade possui campo nao comparavel.
2. `FAIL_FAST`: detecta a situacao e lanca uma excecao com mensagem clara antes de executar a query.
3. `ALWAYS_DISTINCT`: preserva o comportamento atual e deixa o banco rejeitar a consulta se ela for invalida.

## Recomendacao

A opcao mais simples para `JpaPaths` e usar `SKIP_DISTINCT_WHEN_NON_COMPARABLE` como comportamento interno inicial:

```java
if (path.crossedPluralAssociation() && canApplyDistinct(root)) {
    query.distinct(true);
}
```

Esta solucao mantem a decisao no ponto onde o `DISTINCT` e aplicado, nao executa consultas adicionais no banco, usa metadados JPA ja disponiveis e amortiza o custo com cache por classe de entidade.
