# Collection Navigation — JFR Hotspot Analysis

**Data:** 2026-04-12
**Branch:** `refac-springboot-4`
**Ferramenta:** Java Flight Recorder (JFR, profile settings, 21 s de gravação) + JMH GCProfiler

## Metodologia

O benchmark `CollectionNavigationBenchmarkRunner` foi executado com JFR habilitado desde o início
(`-XX:StartFlightRecording=settings=profile,dumponexit=true`). O JFR capturou
1 916 amostras `jdk.ExecutionSample` e 6 179 amostras `jdk.ObjectAllocationSample`.
Os frames foram agregados por frequência para identificar os caminhos mais quentes de CPU e alocação.

## Resultados JMH (ns/op | B/op)

| Benchmark              | ns/op  | B/op  |
|------------------------|-------:|------:|
| `indexAccess`          |   87.5 |   136 |
| `mapValuesCount`       |  169.1 |   224 |
| `customFunctionCount`  |  316.6 |   296 |
| `listFilterCount`      |  547.2 |   424 |
| `mapFilterCount`       |  673.7 |   688 |
| `deepScanCount`        | 1164.6 | 1096  |

## Tabela de Hotspots

| ID  | Hipótese / Suspeita | Evidência JFR | Tipo | Severidade | Benchmark mais afetado | Conclusão |
|-----|---------------------|---------------|------|-----------|------------------------|-----------|
| H1  | `buildOverrides` aloca `Object[]` + `Arrays.fill` a cada `compute()` | 1 852 samples `Object[]`; 107 CPU em `Arrays.fill`; 373 CPU (19,5%) em `createExecutionScope` | CPU + Alloc | **Alta** | Todos (baseline ~136 B/op em `indexAccess`) | Custo fixo pago em toda chamada a `compute()`, independente do tipo de expressão. `new Object[n]` + fill a cada invocação. |
| H2  | `ExecutionScope` novo por chamada | 808 samples de alocação de `ExecutionScope` | Alloc | **Alta** | Todos | Criado em `createExecutionScope` via `ExecutionScope.readOnly(overrides, defaultValues)`; descartado após cada avaliação. |
| H3  | `FilterContext` record por elemento em predicados de filtro | 536 samples `FilterContext`; 233 CPU em `applyFilter` (linhas 585 e 600 combinadas) | CPU + Alloc | **Alta** | `listFilterCount`, `mapFilterCount` | Um `FilterContext` por elemento testado no predicado — 4 objetos por chamada para lista/mapa com 4 entradas. |
| H4  | Iteração de `ImmutableCollections$MapN` em `applyFilter` gera iterator e wrappers de entrada | 384 samples `MapNIterator` + 564 samples `KeyValueHolder`; ambos concentrados no thread `mapFilterCount-worker` | Alloc | **Alta** | `mapFilterCount` (688 B/op) | `applyFilter` faz `map.entrySet()` em um mapa `Map.copyOf()` (`MapN`); cada iteração aloca `MapNIterator` e um `KeyValueHolder` por entrada do mapa. |
| H5  | `applyDeepScan` aloca 3 estruturas temporárias por chamada | 6 samples `IdentityHashMap`; 674 samples `ArrayList.grow()` indicando crescimento além da capacidade inicial da lista de resultados | Alloc | **Alta** | `deepScanCount` (1 096 B/op, 1 164 ns/op) | O caminho mais caro de todos. Aloca `new ArrayList<>()`, `new IdentityHashMap<>()` e `new ArrayDeque<>()` a cada invocação de `applyDeepScan`. A lista de resultados ainda sofre `grow()` ao coletar nós da `store`. |
| H6  | `applyMapProjection` clona values/keys em `ArrayList` mesmo quando consumido por `count()` | 384 samples `MapNIterator` no path `mapValuesCount` (224 B/op) | Alloc | **Média** | `mapValuesCount` | `new ArrayList<>(typed.values())` a cada chamada; quando seguido de `..count()`, a lista é materializada apenas para chamar `list.size()`. |
| H7  | `ImmutableCollections$SubList` no caminho de `mapFilterCount` | 397 samples `SubList`, concentrados no thread `mapFilterCount-worker` | Alloc | **Média** | `mapFilterCount` | Provavelmente originado na iteração do `entrySet()` de `ImmutableCollections$MapN`, ou de `List.subList()` interno a `FunctionDescriptor.parameterTypes()`. |
| H8  | `applyFilter` (lista) cria `ArrayList` sem capacidade inicial | 36 samples `ArrayList` | Alloc | **Baixa** | `listFilterCount` | `new ArrayList<>()` sem pre-size; para 4 elementos não é crítico, mas em listas maiores gera `grow()`. |

## Análise por Benchmark

### `deepScanCount` — 1 164 ns/op, 1 096 B/op

O caminho mais caro. `applyDeepScan` aloca três estruturas de dados por chamada e a lista de
resultados sofre crescimento dinâmico. A lógica de detecção de ciclo via `IdentityHashMap` e a
fila BFS via `ArrayDeque` são recriadas a cada invocação.

### `mapFilterCount` — 673 ns/op, 688 B/op

Custo dominado pela iteração sobre `ImmutableCollections$MapN` (H4): o `entrySet()` cria
`MapNIterator` + `KeyValueHolder` por entrada, somados ao `FilterContext` por elemento (H3) e ao
`LinkedHashMap` de resultado. O alocador de `ImmutableCollections$SubList` (H7) sugere
um caminho secundário adicional neste benchmark.

### `listFilterCount` — 547 ns/op, 424 B/op

Similar ao `mapFilterCount`, mas sem o overhead de iteração de `MapN`. O custo vem de
`FilterContext` por elemento (H3) e do `ArrayList` de resultado (H8).

### `customFunctionCount` — 316 ns/op, 296 B/op

Custo misto: overhead de `buildOverrides`/`ExecutionScope` (H1+H2) mais o `Object[]` de
argumentos alocado em `applyCollectionFunction` a cada chamada.

### `mapValuesCount` — 169 ns/op, 224 B/op

Custo vem principalmente do `ArrayList` clonado de `applyMapProjection` (H6) mais o baseline de
`buildOverrides`/`ExecutionScope`.

### `indexAccess` — 87 ns/op, 136 B/op

Praticamente só o baseline de `buildOverrides` (H1) + `ExecutionScope` (H2). A operação de acesso
por índice em si (`list.get(index)`) não aloca.

## Oportunidades de Otimização (em prioridade)

### O1 — Reutilizar o array de overrides e o `ExecutionScope` (H1 + H2)

Custo base pago por todos os benchmarks. Candidatos para um `ThreadLocal` com reset seletivo:
reutilizar o `Object[]` de overrides preenchendo com `UNBOUND` apenas os slots que foram escritos
e reaproveitando a instância de `ExecutionScope`.

### O2 — Reciclar as estruturas de `applyDeepScan` via `ThreadLocal` (H5)

Substituir `new ArrayList<>()`, `new IdentityHashMap<>()` e `new ArrayDeque<>()` por instâncias
mantidas em `ThreadLocal` com limpeza por `results.clear()` / `visited.clear()` / `queue.clear()`
antes de cada uso. Pré-dimensionar a lista de resultados (ex.: capacidade inicial 16) eliminaria o
`ArrayList.grow()`.

### O3 — Evitar `entrySet()` em `ImmutableCollections$MapN` no `applyFilter` (H4)

Iterar sobre `map.keySet()` + `map.get(key)` evita a alocação de `MapNIterator` e `KeyValueHolder`.
Custo de lookup O(1) em `MapN` é idêntico ao acesso direto por chave.

### O4 — Substituir `FilterContext` record por estado mutável por thread (H3)

Como o contexto é sempre push/pop imediato em uma pilha ThreadLocal, substituir o record imutável
por dois campos `Object` em uma classe de contexto reutilizável eliminaria 4 alocações por chamada
de filtro.

### O5 — Fold antecipado de `..count()` sobre `..values()` / `..keys()` (H6)

Quando `applyMapProjection` for imediatamente seguido de `applyAggregation(COUNT)`, o plano de
execução pode retornar `BigDecimal.valueOf(map.size())` diretamente, sem materializar a lista.
Requer detecção na fase de construção do plano (`ExecutionPlanBuilder`).

### O6 — Pre-size `applyFilter` result collections (H8)

Usar `new ArrayList<>(list.size())` e `new LinkedHashMap<>(map.size() * 2)` nos paths de filtro
para evitar rehash/grow nos casos onde todos os elementos passam pelo predicado.
