# Dynamic Filter Resolver - Plano para Metadados de Operacao

## Objetivo

Permitir que operacoes customizadas registrem metadados neutros sobre o formato esperado do valor de entrada, sem acoplar o `core` a OpenAPI, JPA ou SpringDoc.

O caso direcionador e a operacao customizada `IsFimVigente`, que deve ser registrada por uma aplicacao consumidora e documentada no OpenAPI como parametro booleano.

## Decisoes Fechadas

| Decisao | Escolha |
|---|---|
| Local do plano | `dynamic-filter-resolver/docs/` |
| Escopo | Infraestrutura de metadados + `IsFimVigente` como exemplo customizado |
| Nome da operacao exemplo | `IsFimVigente` |
| Entrega de `IsFimVigente` | Exemplo customizado, nao built-in da biblioteca |
| Semantica de `true` | `campo IS NULL OR campo < now` |
| Semantica de `false` | Negacao de `campo IS NULL OR campo < now` |
| Tipos temporais esperados | `java.util.Date` + tipos `java.time` comparaveis |
| Conversoes temporais | Verificar e reutilizar `DateUtils` / `DataConversionService` quando fizer sentido |
| Origem de `now` | `Clock` injetavel, com padrao `Clock.systemDefaultZone()` na aplicacao consumidora |
| Parametro ausente | Ignorar filtro; `defaultValues = "true"` pode ser usado por quem quiser aplicar por padrao |
| OpenAPI | `IsFimVigente` deve aparecer como `boolean` |

## Diagnostico do Codigo Atual

O `FilterOperationRegistry<T>` registra apenas a fabrica runtime da operacao:

```java
private final Map<Class<? extends FilterOperation>, FilterOperation<T>> operations = new LinkedHashMap<>();
```

Isso resolve a extensibilidade JPA, mas nao oferece ao modulo OpenAPI uma forma generica de saber que uma operacao customizada espera `boolean`, `array`, `string` ou o tipo do campo alvo.

Hoje o OpenAPI depende de regras especiais no `DynaFilterOperationCustomizer`:

- `Dynamic.class` vira array com `minItems(2)`.
- `IsIn.class` vira array.
- `IsNull.class` vira boolean.
- Operacoes customizadas usam schema do campo alvo ou fallback `StringSchema`.

Para `IsFimVigente`, esse fallback nao e suficiente, pois o campo alvo e temporal, mas o parametro de entrada deve ser booleano.

## Diretriz Arquitetural

Usar o registry como ponto de entrada para metadados, mas registrar apenas metadados neutros de entrada. O `core` nao deve conhecer `BooleanSchema`, `ArraySchema`, SpringDoc ou qualquer classe OpenAPI.

Modelo recomendado:

```java
public enum FilterValueShape {
    TARGET_FIELD,
    STRING,
    BOOLEAN,
    ARRAY,
    RANGE
}
```

```java
public record FilterOperationMetadata(FilterValueShape valueShape) {

    public static FilterOperationMetadata targetField() {
        return new FilterOperationMetadata(FilterValueShape.TARGET_FIELD);
    }

    public static FilterOperationMetadata stringValue() {
        return new FilterOperationMetadata(FilterValueShape.STRING);
    }

    public static FilterOperationMetadata booleanValue() {
        return new FilterOperationMetadata(FilterValueShape.BOOLEAN);
    }

    public static FilterOperationMetadata arrayValue() {
        return new FilterOperationMetadata(FilterValueShape.ARRAY);
    }

    public static FilterOperationMetadata rangeValue() {
        return new FilterOperationMetadata(FilterValueShape.RANGE);
    }
}
```

Todos os built-ins registraveis tambem devem ter metadados explicitos no registry. Isso deixa o registry como fonte unica do formato esperado do parametro e reduz regras especiais no OpenAPI.

`Dynamic.class` e `Decorated.class` continuam como pseudo-operacoes e nao devem ser registradas diretamente no JPA. `Dynamic.class` permanece especial no OpenAPI porque seu valor e composto por codigo de operacao + valores, nao pela operacao final registrada.

## API Desejada no Registry

Manter o registro atual como overload de compatibilidade, usando `TARGET_FIELD` como default:

```java
public void register(Class<? extends FilterOperation> operationType, FilterOperation<T> operation) {
    register(operationType, operation, FilterOperationMetadata.targetField());
}
```

Adicionar overload com metadados:

```java
public void register(
        Class<? extends FilterOperation> operationType,
        FilterOperation<T> operation,
        FilterOperationMetadata metadata
) {
    // valida null, duplicidade e registra operacao + metadata
}
```

Para preservar chamadas existentes, o registry pode manter `toMap()` retornando somente as operacoes e adicionar `toMetadataMap()`:

```java
public Map<Class<? extends FilterOperation>, FilterOperation<T>> toMap()
public Map<Class<? extends FilterOperation>, FilterOperationMetadata> toMetadataMap()
```

## Exposicao Pelo Service

O OpenAPI nao deve depender diretamente do registry JPA. A fonte publica deve ser o service ou uma interface neutra.

Opcao recomendada:

```java
public interface FilterOperationService<T> {

    T createFilter(FilterData filterData);

    boolean supports(Class<? extends FilterOperation> operationType);

    Optional<FilterOperationMetadata> findMetadata(Class<? extends FilterOperation> operationType);
}
```

`AbstractFilterOperationService<T>` passa a armazenar dois mapas imutaveis:

```java
private final Map<Class<? extends FilterOperation>, FilterOperation<T>> operationMap;
private final Map<Class<? extends FilterOperation>, FilterOperationMetadata> metadataMap;
```

Para evitar conflito por erasure com o construtor atual baseado em `Supplier<Map<...>>`, adicionar um construtor direto por registry e manter o construtor antigo:

```java
protected AbstractFilterOperationService(FilterOperationRegistry<T> registry) {
    this.operationMap = registry.toMap();
    this.metadataMap = registry.toMetadataMap();
}

protected AbstractFilterOperationService(Supplier<Map<Class<? extends FilterOperation>, FilterOperation<T>>> operationMap) {
    this.operationMap = Map.copyOf(operationMap.get());
    this.metadataMap = Map.of();
}
```

O construtor antigo preserva compatibilidade interna, mas operacoes registradas por ele nao terao metadados customizados.

## Ajuste no JPA Adapter

`SpecificationFilterOperationService` deve criar um `FilterOperationRegistry<Specification<?>>` e passar o registry completo ao `AbstractFilterOperationService`.

Built-ins recomendados:

| Operacao | Metadata |
|---|---|
| `IsNull.class` | `BOOLEAN` |
| `IsIn.class` | `ARRAY` |
| `Between.class` | `RANGE` |
| Demais comparacoes escalares | `TARGET_FIELD` |
| `Dynamic.class` | Nao registrar; pseudo-operacao especial |
| `Decorated.class` | Nao registrar; pseudo-operacao resolvida por decorators |

Mapeamento detalhado recomendado:

| Operacao | Metadata | Observacao |
|---|---|---|
| `Equals.class` | `TARGET_FIELD` | Valor unico convertido para o tipo do campo. |
| `Like.class` | `TARGET_FIELD` | Valor unico; normalmente string, mas preserva comportamento atual baseado no campo. |
| `StartsWith.class` | `TARGET_FIELD` | Valor unico. |
| `EndsWith.class` | `TARGET_FIELD` | Valor unico. |
| `Greater.class` | `TARGET_FIELD` | Valor unico convertido para o tipo do campo. |
| `GreaterOrEquals.class` | `TARGET_FIELD` | Valor unico convertido para o tipo do campo. |
| `Less.class` | `TARGET_FIELD` | Valor unico convertido para o tipo do campo. |
| `LessOrEquals.class` | `TARGET_FIELD` | Valor unico convertido para o tipo do campo. |
| `IsNull.class` | `BOOLEAN` | Parametro indica se deve testar nulo ou nao nulo. |
| `IsIn.class` | `ARRAY` | Lista aberta de valores. |
| `Between.class` | `RANGE` | Exatamente dois valores: inicio e fim. |

Exemplo esperado no registro dos built-ins:

```java
registry.register(IsNull.class, isNullOperation, FilterOperationMetadata.booleanValue());
registry.register(IsIn.class, isInOperation, FilterOperationMetadata.arrayValue());
registry.register(Between.class, betweenOperation, FilterOperationMetadata.rangeValue());

registry.register(Equals.class, equalsOperation, FilterOperationMetadata.targetField());
registry.register(Like.class, likeOperation, FilterOperationMetadata.targetField());
registry.register(StartsWith.class, startsWithOperation, FilterOperationMetadata.targetField());
registry.register(EndsWith.class, endsWithOperation, FilterOperationMetadata.targetField());
registry.register(Greater.class, greaterOperation, FilterOperationMetadata.targetField());
registry.register(GreaterOrEquals.class, greaterOrEqualsOperation, FilterOperationMetadata.targetField());
registry.register(Less.class, lessOperation, FilterOperationMetadata.targetField());
registry.register(LessOrEquals.class, lessOrEqualsOperation, FilterOperationMetadata.targetField());
```

Contributors customizados continuam usando o mesmo ponto de extensao, agora com a possibilidade de informar metadados:

```java
registry.register(
        IsFimVigente.class,
        filterData -> new SpecificationIsFimVigente<>(filterData, conversionService, clock),
        FilterOperationMetadata.booleanValue()
);
```

## Ajuste no OpenAPI

`DynaFilterOperationCustomizer` deve consultar os metadados da operacao antes do fallback comum.

Fluxo recomendado:

1. Se `operation == Dynamic.class`, manter schema array com `minItems(2)`.
2. Buscar metadata no `FilterOperationService<Specification<?>>` ou em uma abstracao neutra equivalente.
3. Se metadata existir, traduzir `FilterValueShape` para schema OpenAPI.
4. Se metadata nao existir, manter fallback atual: schema do campo alvo ou `StringSchema` para customizadas.

Mapeamento OpenAPI:

| `FilterValueShape` | Schema OpenAPI |
|---|---|
| `BOOLEAN` | `BooleanSchema` |
| `ARRAY` | `ArraySchema` com item baseado no schema atual ou `StringSchema` |
| `RANGE` | `ArraySchema` com `minItems(2)`, `maxItems(2)` e item baseado no campo alvo |
| `STRING` | `StringSchema` |
| `TARGET_FIELD` | Schema do campo alvo, com fallback atual |

Essa abordagem permite documentar `IsFimVigente` como boolean sem adicionar `if (IsFimVigente.class)` ao OpenAPI.

## Exemplo de Operacao Customizada `IsFimVigente`

Contrato da operacao na aplicacao consumidora:

```java
package com.example.filters;

import com.runestone.dynafilter.core.operation.FilterOperation;

public interface IsFimVigente<T> extends FilterOperation<T> {
}
```

Uso em filtro:

```java
@Filter(
        path = "fimVigencia",
        parameters = "vigente",
        operation = IsFimVigente.class,
        description = "true aplica fim vigente; false aplica a negacao"
)
```

Semantica runtime:

| Valor do parametro | Predicate |
|---|---|
| `true` | `fimVigencia IS NULL OR fimVigencia < now` |
| `false` | `NOT (fimVigencia IS NULL OR fimVigencia < now)` |
| ausente | filtro ignorado, salvo `defaultValues` ou `required` existentes |

Registro JPA na aplicacao consumidora:

```java
@Bean
Clock dynamicFilterClock() {
    return Clock.systemDefaultZone();
}

@Bean
SpecificationFilterOperationContributor isFimVigenteOperation(
        DataConversionService conversionService,
        Clock clock
) {
    return registry -> registry.register(
            IsFimVigente.class,
            filterData -> new SpecificationIsFimVigente<>(filterData, conversionService, clock),
            FilterOperationMetadata.booleanValue()
    );
}
```

## Conversao Temporal para `now`

O toolkit ja possui:

- `DateUtils`, com formatadores para parsing textual.
- `DataConversionService`, com conversores entre `String`, `java.util.Date` e varios tipos `java.time`.
- Conversores internos em `com.runestone.converters.impl.dates`, incluindo suporte de `java.util.Date` para `LocalDate`, `LocalDateTime`, `LocalTime` e `ZonedDateTime`.

Para `IsFimVigente`, o plano de implementacao deve evitar depender de classes package-private do toolkit. A opcao recomendada e criar, no exemplo ou no modulo de teste, um pequeno resolvedor de `now` a partir de `Clock` e do tipo Java do path:

```java
Object nowFor(Class<?> pathType, Clock clock) {
    if (java.util.Date.class.isAssignableFrom(pathType)) {
        return java.util.Date.from(clock.instant());
    }
    if (Instant.class.equals(pathType)) {
        return clock.instant();
    }
    if (LocalDate.class.equals(pathType)) {
        return LocalDate.now(clock);
    }
    if (LocalDateTime.class.equals(pathType)) {
        return LocalDateTime.now(clock);
    }
    if (OffsetDateTime.class.equals(pathType)) {
        return OffsetDateTime.now(clock);
    }
    if (ZonedDateTime.class.equals(pathType)) {
        return ZonedDateTime.now(clock);
    }
    throw new DynamicFilterConfigurationException("Unsupported temporal type for IsFimVigente: " + pathType.getCanonicalName());
}
```

Durante a implementacao, validar se `DataConversionService` consegue reduzir essa logica sem perder previsibilidade. Se for usado, a origem primaria ainda deve ser `Clock`, nao `LocalDateTime.now()` direto.

## Tarefas Detalhadas

| ID | Tarefa | Status | Prioridade |
|---|---|---|---|
| META-001 | Criar `FilterValueShape` no core | Pendente | Alta |
| META-002 | Criar `FilterOperationMetadata` no core | Pendente | Alta |
| META-003 | Estender `FilterOperationRegistry` com overload que recebe metadata | Pendente | Alta |
| META-004 | Expor mapa imutavel de metadata pelo registry | Pendente | Alta |
| META-005 | Expor `findMetadata(...)` em `FilterOperationService` | Pendente | Alta |
| META-006 | Atualizar `AbstractFilterOperationService` para armazenar metadata | Pendente | Alta |
| META-007 | Atualizar `SpecificationFilterOperationService` para passar registry completo | Pendente | Alta |
| META-008 | Registrar metadata dos built-ins JPA (`IsNull`, `IsIn`, demais) | Pendente | Alta |
| META-009 | Atualizar `DynaFilterOperationCustomizer` para usar metadata antes do fallback | Pendente | Alta |
| META-010 | Adicionar testes de registry/service para metadata default e customizada | Pendente | Alta |
| META-011 | Adicionar testes OpenAPI para operacao customizada `BOOLEAN` | Pendente | Alta |
| META-012 | Adicionar testes OpenAPI para built-ins usando metadata (`IsNull`, `IsIn`, `Between`) | Pendente | Alta |
| META-013 | Adicionar exemplo/teste de `IsFimVigente` customizado com `Clock` fixo | Pendente | Media |
| META-014 | Documentar uso de metadata em `custom-filter-operations.md` | Pendente | Media |

## Criterios de Aceite

| Criterio | Status |
|---|---|
| Uma operacao registrada sem metadata continua funcionando com shape `TARGET_FIELD`. | Pendente |
| Uma operacao customizada pode registrar metadata `BOOLEAN`. | Pendente |
| O OpenAPI usa `BooleanSchema` para operacao customizada com metadata `BOOLEAN`. | Pendente |
| `IsNull.class` continua aparecendo como boolean no OpenAPI. | Pendente |
| `IsIn.class` continua aparecendo como array no OpenAPI. | Pendente |
| `Between.class` aparece como array com exatamente dois itens no OpenAPI. | Pendente |
| `Dynamic.class` continua com schema array e `minItems(2)`. | Pendente |
| `Decorated.class` continua sem registro direto no JPA e sem metadata propria. | Pendente |
| `IsFimVigente` customizado consegue aplicar `true` e `false` com `Clock` fixo em teste. | Pendente |
| Parametro ausente em `IsFimVigente` continua ignorando o filtro. | Pendente |
| Nao ha dependencia de OpenAPI no pacote `core`. | Pendente |
| Testes do modulo `dynamic-filter-resolver` passam. | Pendente |

## Riscos e Mitigacoes

| Risco | Mitigacao |
|---|---|
| Acoplar `core` ao OpenAPI | Usar `FilterValueShape`, nao `Schema` ou classes SpringDoc. |
| Quebrar contributors existentes | Manter overload `register(type, operation)` com metadata default. |
| OpenAPI nao encontrar metadata quando o service e customizado | Se `findMetadata(...)` retornar vazio, manter fallback atual. |
| Duplicar regras especiais no OpenAPI | Tratar built-ins registraveis por metadata e manter apenas `Dynamic.class` como pseudo-operacao especial. |
| `IsFimVigente` variar por timezone | Usar `Clock` injetavel e documentar zona padrao. |
| Tipos temporais nao suportados gerarem predicate incorreto | Falhar com erro de configuracao claro para tipo temporal desconhecido. |

## Fora de Escopo Nesta Fase

- Transformar `IsFimVigente` em built-in da biblioteca.
- Criar SPI OpenAPI baseada diretamente em `Parameter` ou `Schema`.
- Criar aliases customizados para `Dynamic.class`.
- Alterar a semantica de `@Filter.negate` para depender do valor do parametro.
