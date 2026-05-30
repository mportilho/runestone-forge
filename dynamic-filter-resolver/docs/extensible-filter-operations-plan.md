# Dynamic Filter Resolver - Plano para Operacoes Extensiveis

## Objetivo

Permitir que aplicacoes consumidoras do `dynamic-filter-resolver` definam novos tipos de operacao para uso direto em `@Filter(operation = ...)`, sem limitar a configuracao ao conjunto fixo de operacoes em `com.runestone.dynafilter.core.operation.types`.

O desenho deve preservar a separacao atual entre:

- `core`: metadados, anotacoes, geracao de statements e contrato generico de operacao.
- `modules.jpa`: traducao de operacoes para `Specification<?>`.
- `modules.openapi`: documentacao dos parametros expostos.

## Status Geral

| Item | Status |
|---|---|
| Planejamento documentado | Concluido |
| Implementacao no codigo | Em andamento |
| Testes automatizados | Em andamento |
| Documentacao de uso para consumidores | Pendente |

## Legenda de Status

| Status | Significado |
|---|---|
| Pendente | Ainda nao iniciado. |
| Em andamento | Alteracao iniciada, mas ainda nao validada. |
| Concluido | Implementado e validado. |
| Bloqueado | Depende de decisao ou informacao externa. |
| Descartado | Nao sera executado neste plano. |

## Indice de Tarefas

| ID | Tarefa | Status | Prioridade |
|---|---|---|---|
| EXT-001 | Abrir o tipo de `operation` no core | Concluido | Alta |
| EXT-002 | Remover completamente `DefinedFilterOperation` | Concluido | Alta |
| EXT-003 | Criar registry generico de operacoes | Concluido | Alta |
| EXT-004 | Atualizar `AbstractFilterOperationService` para usar o registry aberto | Concluido | Alta |
| EXT-005 | Criar ponto de extensao JPA via contributor | Concluido | Alta |
| EXT-006 | Refatorar `SpecificationFilterOperationService` para registrar built-ins e contributors | Concluido | Alta |
| EXT-007 | Atualizar a auto-configuracao Spring para coletar contributors | Concluido | Alta |
| EXT-008 | Validar operacoes nao registradas no startup quando possivel | Concluido | Media |
| EXT-009 | Manter tratamento especial de `Decorated` e `Dynamic` | Concluido | Alta |
| EXT-010 | Ajustar OpenAPI com fallback para operacoes customizadas | Pendente | Media |
| EXT-011 | Avaliar SPI OpenAPI especifica para operacoes customizadas | Pendente | Baixa |
| EXT-012 | Avaliar aliases customizados para `Dynamic.class` em fase posterior | Pendente | Baixa |
| EXT-013 | Adicionar testes de operacao customizada no core e JPA | Concluido | Alta |
| EXT-014 | Adicionar exemplo de uso para aplicacoes consumidoras | Pendente | Media |

## Diagnostico Inicial

Antes desta refatoracao, o desenho ja separava parcialmente o tipo abstrato de operacao e a implementacao por adaptador. O problema era que o contrato publico usava `DefinedFilterOperation`, fechando artificialmente o conjunto de operacoes permitidas.

| Local | Situacao atual | Impacto |
|---|---|---|
| `core/generator/annotation/Filter.java` | `Class<? super DefinedFilterOperation> operation()` | A anotacao so aceita tipos relacionados ao conjunto fixo atual. |
| `core/model/FilterData.java` | `Class<? super DefinedFilterOperation> operation` | O modelo interno tambem assume conjunto fechado. |
| `core/model/FilterRequestData.java` | Mesmo tipo fechado | A documentacao OpenAPI herda a mesma limitacao. |
| `core/operation/AbstractFilterOperationService.java` | `Map<Class<? super DefinedFilterOperation>, FilterOperation<T>>` | O registry por adaptador tambem e fechado. |
| `core/operation/ComparisonOperation.java` | Enum retorna `Class<? super DefinedFilterOperation>` | `Dynamic.class` continua dependendo do tipo agregador fechado para mapear codigos built-in. |
| `core/operation/DefinedFilterOperation.java` | Interface agregadora de todas as operacoes built-in | O tipo existe apenas para fechar artificialmente a API publica. |
| `modules/jpa/operation/SpecificationFilterOperationService.java` | Mapa fixo de operacoes JPA | Nao ha ponto simples para aplicacoes consumidoras registrarem novas operacoes. |
| `core/generator/DefaultStatementGenerator.java` | `Dynamic.class` usa `ComparisonOperation` | Operacoes dinamicas por codigo continuam limitadas ao enum built-in. |

### Varredura de Codigo para Remocao de `DefinedFilterOperation`

Varredura executada no modulo `dynamic-filter-resolver` encontrou os seguintes pontos que devem entrar na refatoracao:

| Local | Referencia encontrada | Ajuste planejado |
|---|---|---|
| `core/operation/DefinedFilterOperation.java` | Interface agregadora `DefinedFilterOperation extends Between, Decorated, Dynamic, ...` | Excluir o arquivo. As operacoes built-in ja estendem `FilterOperation<T>` diretamente. |
| `core/generator/annotation/Filter.java` | Import de `DefinedFilterOperation` e assinatura `Class<? super DefinedFilterOperation> operation()` | Trocar para `@SuppressWarnings("rawtypes") Class<? extends FilterOperation> operation()` e importar `FilterOperation`. |
| `core/model/FilterData.java` | Import de `DefinedFilterOperation`, componente `operation` e factory `of(...)` com tipo fechado | Trocar ambos para `Class<? extends FilterOperation>` e importar `FilterOperation`. |
| `core/model/FilterRequestData.java` | Import de `DefinedFilterOperation` e componente `operation` com tipo fechado | Trocar para `Class<? extends FilterOperation>` e importar `FilterOperation`. |
| `core/operation/ComparisonOperation.java` | Campo, construtor e getter retornando `Class<? super DefinedFilterOperation>` | Trocar para `Class<? extends FilterOperation>`; manter os valores built-in apontando para as interfaces em `operation.types`. |
| `core/generator/DefaultStatementGenerator.java` | Import de `DefinedFilterOperation` e parametro `operation` de `createFilterData(...)` com tipo fechado | Trocar para `Class<? extends FilterOperation>` e manter a traducao de `Dynamic.class` via `ComparisonOperation`. |
| `core/operation/FilterOperationService.java` | Contrato ainda nao expoe consulta de suporte por operacao | Adicionar `supports(Class<? extends FilterOperation>)` para validacao antecipada sem depender da implementacao concreta. |
| `core/operation/AbstractFilterOperationService.java` | `Map<Class<? super DefinedFilterOperation>, FilterOperation<T>>` e construtor com supplier do mesmo tipo | Trocar para `Map<Class<? extends FilterOperation>, FilterOperation<T>>`; adicionar `supports(...)` no contrato para validacao antecipada. |
| `modules/jpa/operation/SpecificationFilterOperationService.java` | Import de `DefinedFilterOperation` e mapa local com tipo fechado | Trocar para registry/mapa aberto baseado em `Class<? extends FilterOperation>` e aplicar contributors JPA. |
| `modules/jpa/spring/FilterConfigurationAnalyserBeanPostProcessor.java` | Hoje valida metadados e campos, mas nao valida suporte da operacao no adapter JPA | Injetar/usar `FilterOperationService<Specification<?>>` em EXT-008 para falhar no startup quando uma operacao nao estiver registrada. |
| `core/generator/ConditionalStatementBuilder.java` | Arquivo inteiro comentado ainda menciona `DefinedFilterOperation` | Remover o arquivo comentado ou limpar as referencias durante a refatoracao para evitar codigo morto com API removida. |
| Testes em `src/test/java` | `StringFilterOperationService` usa o construtor fechado indiretamente; testes usam built-ins, `Dynamic.class` e `Decorated.class` | Ajustar testes para o novo tipo aberto e adicionar cenarios de operacao customizada sem depender de `DefinedFilterOperation`. |

Observacao da varredura: as interfaces built-in em `core/operation/types` (`Between`, `Equals`, `Like`, `Dynamic`, `Decorated`, etc.) ja estendem `FilterOperation<T>` diretamente. Portanto, remover `DefinedFilterOperation` nao exige alterar a hierarquia das operacoes built-in, apenas os pontos que usam a interface agregadora como limite generico.

## Decisao Arquitetural Recomendada

Abrir o contrato de `@Filter.operation()` para qualquer tipo que estenda `FilterOperation`, remover `DefinedFilterOperation` e manter a implementacao concreta registrada por adaptador.

Exemplo de tipo de operacao na aplicacao consumidora:

```java
package com.example.filters;

import com.runestone.dynafilter.core.operation.FilterOperation;

public interface FullTextSearch<T> extends FilterOperation<T> {
}
```

Uso no filtro:

```java
@Filter(
        path = "description",
        parameters = "q",
        operation = FullTextSearch.class,
        description = "Full-text search on description"
)
```

Registro no adaptador JPA:

```java
@Bean
SpecificationFilterOperationContributor fullTextSearchOperation(DataConversionService conversionService) {
    return registry -> registry.register(
            FullTextSearch.class,
            filterData -> new FullTextSearchSpecification<>(filterData, conversionService)
    );
}
```

## Tarefas Detalhadas

### EXT-001 - Abrir o tipo de `operation` no core

Status: Concluido

Objetivo: substituir o limite `DefinedFilterOperation` por um contrato aberto baseado em `FilterOperation`.

Arquivos esperados:

- `core/generator/annotation/Filter.java`
- `core/model/FilterData.java`
- `core/model/FilterRequestData.java`
- `core/operation/ComparisonOperation.java`
- `core/generator/DefaultStatementGenerator.java`
- `core/operation/DefinedFilterOperation.java` deve ser removido por EXT-002
- Chamadores afetados por esses tipos

Tipo recomendado:

```java
@SuppressWarnings("rawtypes")
Class<? extends FilterOperation> operation();
```

Motivo: anotacoes Java nao conseguem preservar bem o parametro generico de `FilterOperation<T>` nesse ponto. O tipo bruto fica contido no metadado da operacao, enquanto a seguranca do retorno continua no adaptador via `FilterOperationService<T>`.

### EXT-002 - Remover completamente `DefinedFilterOperation`

Status: Concluido

Objetivo: eliminar a interface agregadora fechada para que `FilterOperation` seja o unico contrato publico de operacao.

Diretriz:

- Remover `core/operation/DefinedFilterOperation.java`.
- Remover todos os imports, assinaturas e generics baseados em `DefinedFilterOperation`.
- Usar `Class<? extends FilterOperation>` nos metadados e registries de operacao.
- Manter `Between`, `Equals`, `Like`, `Dynamic`, `Decorated` e demais built-ins como interfaces publicas que estendem `FilterOperation<T>` diretamente.
- Tratar a remocao como mudanca de API publica e registrar no changelog/release notes quando a implementacao for concluida.

Impacto esperado:

- Codigo consumidor que declare variaveis, metodos ou imports de `DefinedFilterOperation` deixara de compilar e devera migrar para `FilterOperation` ou para a interface concreta da operacao.
- Usos normais de `@Filter(operation = Equals.class)`, `@Filter(operation = Dynamic.class)` e demais built-ins continuam compilando apos EXT-001, pois essas interfaces ja implementam o contrato aberto.

### EXT-003 - Criar registry generico de operacoes

Status: Concluido

Objetivo: centralizar registro, validacao e exposicao imutavel das operacoes disponiveis.

Classe sugerida:

```java
public final class FilterOperationRegistry<T> {

    private final Map<Class<? extends FilterOperation>, FilterOperation<T>> operations = new LinkedHashMap<>();

    public void register(Class<? extends FilterOperation> operationType, FilterOperation<T> operation) {
        Objects.requireNonNull(operationType, "operationType cannot be null");
        Objects.requireNonNull(operation, "operation cannot be null");

        FilterOperation<T> previous = operations.putIfAbsent(operationType, operation);
        if (previous != null) {
            throw new DynamicFilterConfigurationException(
                    "Filter operation '%s' is already registered".formatted(operationType.getCanonicalName())
            );
        }
    }

    Map<Class<? extends FilterOperation>, FilterOperation<T>> toMap() {
        return Map.copyOf(operations);
    }
}
```

Politica recomendada: registros duplicados devem falhar. Override de operacoes built-in deve ser uma funcionalidade explicita futura, nao um efeito colateral.

### EXT-004 - Atualizar `AbstractFilterOperationService`

Status: Concluido

Objetivo: trocar o mapa fechado por um mapa aberto.

Mudanca esperada:

```java
private final Map<Class<? extends FilterOperation>, FilterOperation<T>> operationMap;
```

Construtor esperado:

```java
public AbstractFilterOperationService(Supplier<Map<Class<? extends FilterOperation>, FilterOperation<T>>> operationMap) {
    this.operationMap = Map.copyOf(operationMap.get());
}
```

Tambem e necessario expor suporte a consulta no contrato `FilterOperationService<T>`, nao apenas na classe abstrata, para que validadores e adaptadores possam depender da interface:

```java
boolean supports(Class<? extends FilterOperation> operationType);
```

Implementacao esperada na classe abstrata:

```java
public boolean supports(Class<? extends FilterOperation> operationType) {
    return operationMap.containsKey(operationType);
}
```

Essa consulta permite validacao antecipada no startup.

### EXT-005 - Criar ponto de extensao JPA via contributor

Status: Concluido

Objetivo: permitir que a aplicacao consumidora registre operacoes JPA sem substituir todo o `DynamicFilterResolver`.

Interface sugerida em `modules.jpa.operation`:

```java
@FunctionalInterface
public interface SpecificationFilterOperationContributor {

    void contribute(FilterOperationRegistry<Specification<?>> registry);

}
```

Motivo: uma interface especifica para JPA e mais facil de consumir via Spring do que um contributor generico parametrizado.

### EXT-006 - Refatorar `SpecificationFilterOperationService`

Status: Concluido

Objetivo: registrar operacoes built-in e aplicar contributors externos.

Diretriz:

- Registrar `Between`, `EndsWith`, `Equals`, `Greater`, `GreaterOrEquals`, `IsIn`, `IsNull`, `Less`, `LessOrEquals`, `Like` e `StartsWith` no registry.
- Aplicar `SpecificationFilterOperationContributor` apos os built-ins.
- Falhar se contributor tentar registrar uma operacao ja existente.
- Manter construtor antigo com `List.of()` para compatibilidade interna e testes existentes.

### EXT-007 - Atualizar auto-configuracao Spring

Status: Concluido

Objetivo: permitir injecao automatica dos contributors declarados pela aplicacao consumidora.

Mudanca esperada em `DynamicFilterServletAutoConfiguration`:

```java
@Bean
@ConditionalOnMissingBean
public FilterOperationService<Specification<?>> specificationFilterOperationService(
        DataConversionService dataConversionService,
        ObjectProvider<SpecificationFilterOperationContributor> contributors
) {
    return new SpecificationFilterOperationService(
            dataConversionService,
            contributors.orderedStream().toList()
    );
}

@Bean
@ConditionalOnMissingBean
public DynamicFilterResolver<Specification<?>> dynamicFilterResolver(
        FilterOperationService<Specification<?>> filterOperationService
) {
    return new SpecificationDynamicFilterResolver(filterOperationService);
}
```

### EXT-008 - Validar operacoes nao registradas no startup

Status: Concluido

Objetivo: detectar erro de configuracao antes da primeira chamada HTTP.

Local sugerido: `FilterConfigurationAnalyserBeanPostProcessor`.

Regras:

- Validar operacoes usadas por filtros JPA contra `FilterOperationService<Specification<?>>`.
- Ignorar `Decorated.class`, pois e resolvido por decorators.
- Ignorar `Dynamic.class`, pois e traduzido para outra operacao antes da criacao do filtro.
- Falhar com `DynamicFilterConfigurationException` ou excecao equivalente quando uma operacao customizada nao tiver registro JPA.

### EXT-009 - Manter tratamento especial de `Decorated` e `Dynamic`

Status: Concluido

Objetivo: evitar regressao em pseudo-operacoes existentes.

Comportamento esperado:

- `Decorated.class` continua sendo processado por `AnnotationStatementGenerator` e `FilterDecoratorFactory`.
- `Dynamic.class` continua sendo interpretado por `DefaultStatementGenerator` via codigos built-in.
- A abertura para operacoes customizadas diretas nao altera a semantica de `Dynamic.class` nesta fase.

### EXT-010 - Ajustar OpenAPI com fallback para operacoes customizadas

Status: Pendente

Objetivo: garantir que operacoes customizadas aparecam na documentacao sem exigir SPI adicional na primeira fase.

Comportamento recomendado:

- `Dynamic.class` continua como array com pelo menos dois itens.
- `IsIn.class` continua como array.
- `IsNull.class` continua como boolean.
- Operacoes customizadas usam o schema comum baseado no campo alvo ou `StringSchema` quando o campo nao puder ser resolvido.

### EXT-011 - Avaliar SPI OpenAPI especifica

Status: Pendente

Objetivo: permitir documentacao especializada para operacoes customizadas com formato proprio.

Interface possivel:

```java
public interface FilterOperationOpenApiCustomizer {

    boolean supports(Class<? extends FilterOperation> operationType);

    void customize(FilterRequestData filter, Parameter parameter);
}
```

Decisao recomendada: nao implementar na primeira fase, a menos que ja exista um caso real de operacao customizada com parametros especiais.

### EXT-012 - Avaliar aliases customizados para `Dynamic.class`

Status: Pendente

Objetivo: permitir que filtros dinamicos aceitem codigos alem de `EQ`, `LT`, `LE`, `GT`, `GE`, `LK`, `SW`, `EW`, `IN` e `BT`.

Decisao recomendada: tratar como fase posterior.

Motivo: esse caso exige metadados adicionais sobre formato dos valores, como valor unico, array ou intervalo. Misturar essa alteracao com a abertura de `@Filter.operation()` aumenta o risco da primeira entrega.

Modelo possivel para fase futura:

```java
public record DynamicOperationAlias(
        String code,
        Class<? extends FilterOperation> operationType,
        DynamicValueShape valueShape
) {
}
```

```java
public enum DynamicValueShape {
    SINGLE,
    ARRAY,
    RANGE
}
```

### EXT-013 - Adicionar testes de operacao customizada

Status: Concluido

Objetivo: provar a extensibilidade, a remocao de `DefinedFilterOperation` e a compatibilidade dos usos normais de operacoes built-in.

Cenarios minimos:

| Cenario | Resultado esperado |
|---|---|
| `@Filter(operation = CustomOperation.class)` compila | A anotacao aceita operacao fora de `DefinedFilterOperation`. |
| Codigo do modulo nao referencia `DefinedFilterOperation` | A interface foi removida e nao restam imports, generics ou comentarios dependentes dela. |
| `AnnotationStatementGenerator` processa `CustomOperation.class` | `FilterData.operation()` preserva a operacao customizada. |
| Contributor JPA registra operacao customizada | `SpecificationFilterOperationService` resolve e cria a specification. |
| Operacao customizada sem contributor | Falha com mensagem clara. |
| Dois contributors registram a mesma operacao | Falha no startup ou na criacao do registry. |
| Operacoes built-in continuam funcionando | Sem regressao para `Equals`, `Like`, `Between`, etc. |
| `Decorated` e `Dynamic` continuam funcionando | Sem regressao para pseudo-operacoes existentes. |

### EXT-014 - Adicionar exemplo de uso para aplicacoes consumidoras

Status: Pendente

Objetivo: documentar como criar e registrar uma operacao customizada.

Conteudo recomendado:

- Criar interface de operacao estendendo `FilterOperation<T>`.
- Usar a operacao em `@Filter`.
- Implementar uma `Specification<T>`.
- Registrar via `SpecificationFilterOperationContributor`.
- Explicar comportamento quando o registro estiver ausente.

## Exemplo de Implementacao JPA Customizada

```java
final class FullTextSearchSpecification<T> implements Specification<T> {

    private final FilterData filterData;
    private final DataConversionService conversionService;

    FullTextSearchSpecification(FilterData filterData, DataConversionService conversionService) {
        this.filterData = Objects.requireNonNull(filterData, "filterData cannot be null");
        this.conversionService = Objects.requireNonNull(conversionService, "conversionService cannot be null");
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
        Path<String> path = root.get(filterData.path());
        String value = conversionService.convert(filterData.findOneValue(), String.class);
        return builder.like(builder.upper(path), "%" + value.toUpperCase() + "%");
    }
}
```

Observacao: esse exemplo e propositalmente simples. Uma implementacao real pode precisar reutilizar utilitarios JPA existentes para path aninhado, joins, conversao por tipo do atributo e modifiers como `ModIgnoreCase`.

## Riscos e Mitigacoes

| Risco | Mitigacao |
|---|---|
| Quebra binaria por alterar assinatura de `Filter.operation()` e remover `DefinedFilterOperation` | Tratar como mudanca de API publica, documentar migracao para `FilterOperation` e validar os usos built-in existentes. |
| Duplicidade de registro de operacao | Registry deve falhar em duplicidade por padrao. |
| Operacao customizada usada sem implementacao JPA | Validar no startup quando o contexto JPA estiver disponivel. |
| Acoplamento indevido do core com JPA ou OpenAPI | Manter contributors especificos nos modulos adaptadores. |
| OpenAPI incompleto para operacoes customizadas complexas | Usar fallback comum na primeira fase e planejar SPI especifica depois. |
| Misturar extensao direta com aliases de `Dynamic.class` | Separar em duas fases. |

## Decisoes Pendentes

| Decisao | Recomendacao | Impacto |
|---|---|---|
| Permitir override de operacoes built-in | Nao permitir na primeira fase | Evita comportamento inesperado e facilita diagnostico. |
| Implementar aliases customizados para `Dynamic.class` agora | Nao implementar agora | Mantem a primeira entrega menor e focada. |
| Criar SPI OpenAPI customizada agora | Nao implementar agora | O fallback comum atende operacoes escalares simples. |
| Compatibilidade com codigo que importa `DefinedFilterOperation` | Nao preservar nesta mudanca | A remocao simplifica a API e elimina o tipo fechado, mas exige nota de migracao. |

## Ordem Recomendada de Execucao

1. Executar EXT-001 e EXT-002 para abrir a API e remover completamente `DefinedFilterOperation`.
2. Executar EXT-003 e EXT-004 para criar a base de registro extensivel.
3. Executar EXT-005, EXT-006 e EXT-007 para expor o ponto de extensao JPA via Spring.
4. Executar EXT-009 para garantir que `Decorated` e `Dynamic` nao regrediram.
5. Executar EXT-013 com foco em testes de compatibilidade e extensibilidade.
6. Executar EXT-008 para melhorar feedback de configuracao no startup.
7. Executar EXT-010 para manter OpenAPI funcional com operacoes customizadas simples.
8. Executar EXT-014 para publicar exemplo de consumo.
9. Reavaliar EXT-011 e EXT-012 somente apos existir demanda concreta.

## Criterios de Aceite

| Criterio | Status |
|---|---|
| Uma aplicacao consumidora consegue declarar uma interface propria que estende `FilterOperation<T>`. | Concluido |
| Essa interface pode ser usada diretamente em `@Filter(operation = MinhaOperacao.class)`. | Concluido |
| `DefinedFilterOperation` foi removido e nao ha referencias remanescentes no codigo fonte. | Concluido |
| O modulo JPA consegue registrar a traducao da operacao customizada para `Specification<?>`. | Concluido |
| Operacoes built-in continuam funcionando sem mudanca no codigo consumidor. | Concluido |
| `Decorated.class` e `Dynamic.class` mantem comportamento atual. | Concluido |
| Operacao customizada sem registro falha com mensagem diagnostica. | Concluido |
| Testes do modulo `dynamic-filter-resolver` passam. | Concluido |
