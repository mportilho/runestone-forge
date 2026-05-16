# Domínio Técnico — dynamic-filter-resolver

> Gerado pelo Reversa Detective em 2026-05-16.
> Escala de confiança: 🟢 CONFIRMADO, 🟡 INFERIDO, 🔴 LACUNA.

## Leitura Geral

🟢 **CONFIRMADO** — Este módulo não implementa um domínio de negócio final como vendas, cadastro ou atendimento. Ele implementa um domínio técnico de biblioteca: declarar filtros em código Java, resolver valores recebidos em requests, transformar esses filtros em uma árvore lógica e adaptar essa árvore para Spring Data JPA `Specification` e SpringDoc OpenAPI.

🟢 **CONFIRMADO** — O vocabulário central vem dos pacotes `com.runestone.dynafilter.core`, `modules.jpa` e `modules.openapi`, dos artefatos `_reversa_sdd/code-analysis.md`, `_reversa_sdd/data-dictionary.md` e do histórico Git do módulo.

🟡 **INFERIDO** — O usuário de negócio deste módulo é o desenvolvedor de APIs Spring/JPA que precisa expor filtros consistentes, documentados e reutilizáveis sem escrever manualmente a mesma lógica de Criteria API, parse de parâmetros e documentação OpenAPI em cada endpoint.

## Glossário

| Termo | Definição | Confiança | Evidência |
|---|---|---|---|
| Filtro dinâmico | Declaração que associa um parâmetro externo, um path de entidade e uma operação de comparação. | 🟢 | `@Filter`, `FilterRequestData`, `FilterData` |
| Path de filtro | Caminho textual para atributo filtrado, simples ou dot-notation, como `addresses.location`. | 🟢 | `Filter.path`, `JpaPredicateUtils.computeAttributePath` |
| Parâmetro técnico | Parâmetro Java de controller que carrega a configuração dinâmica (`ConditionalStatement` ou interface `Specification`). | 🟢 | `SpecificationDynamicFilterArgumentResolver.supportsParameter` |
| Parâmetro exposto | Nome de query/path parameter informado pelo chamador da API e derivado de `Filter.parameters`. | 🟢 | `FilterRequestData.parameters`, `DynaFilterOperationCustomizer.customizeParameter` |
| Statement | Nó lógico intermediário que representa filtro simples, composição, negação ou ausência de filtro. | 🟢 | `LogicalStatement`, `CompoundStatement`, `NegatedStatement`, `NoOpStatement` |
| Conjunction | Agrupamento lógico por AND. | 🟢 | `@Conjunction`, `LogicOperator.CONJUNCTION` |
| Disjunction | Agrupamento lógico por OR. | 🟢 | `@Disjunction`, `LogicOperator.DISJUNCTION` |
| Operação dinâmica | Operação definida pelo primeiro valor enviado pelo usuário usando código curto como `EQ`, `IN` ou `BT`. | 🟢 | `ComparisonOperation`, `DefaultStatementGenerator.createFilterData` |
| Decorator de filtro | Extensão que altera ou envolve o filtro concreto gerado. | 🟢 | `FilterDecorator`, `CompositeFilterDecorator`, `SpringFilterDecoratorFactory` |
| Fetching | Decorator JPA declarativo para criar fetch joins e evitar carregamento lazy quando necessário. | 🟢 | `@Fetching`, `FetchingFilterDecorator` |
| Modificador de filtro | Classe marcadora que altera comportamento da operação, por exemplo join type ou ignore-case. | 🟢 | `FilterModifier`, `ModJoinTypeLeft`, `ModJoinTypeRight`, `ModIgnoreCase` |
| Filtro decorado | Filtro marcado como `Decorated` para ser tratado por decorator e não exposto como operação comum. | 🟢 | `Decorated`, commit `f27f37e` |
| OpenAPI expansion | Substituição do parâmetro técnico por parâmetros OpenAPI individuais derivados dos filtros requisitáveis. | 🟢 | `DynaFilterOperationCustomizer` |
| Warmup de metadados | Validação antecipada dos filtros durante inicialização dos controllers, antes do request. | 🟢 | `FilterConfigurationAnalyserBeanPostProcessor`, commit `2884333` |

## Regras De Domínio Técnico

### Declaração e Metadados

🟢 **CONFIRMADO** — Um filtro declarado por `@Filter` precisa ter `path`, `parameters` e `operation`. A validação de metadados rejeita filtros sem parâmetros e arrays de `parameters`, `defaultValues` e `constantValues` com tamanhos incompatíveis.

🟢 **CONFIRMADO** — `@Conjunction` combina filtros por AND; `@Disjunction` combina filtros por OR. Sub-statements internos usam o operador oposto e depois são combinados pelo operador externo.

🟢 **CONFIRMADO** — `@ConjunctionFrom` e `@DisjunctionFrom` permitem separar o contrato de filtros em classes externas, desde que a entidade alvo seja resolvível por `@FilterTarget` quando necessário.

🟢 **CONFIRMADO** — Metadados de annotations são cacheados por `AnnotationStatementInput`, com cópia defensiva do array de annotations e hash pré-computado para estabilidade e performance.

🟢 **CONFIRMADO** — O cache de metadados é limitado por Caffeine com tamanho padrão `4096` e override por system property `runestone.dynafilter.annotation.cache.max-size`.

### Resolução de Valores

🟢 **CONFIRMADO** — A precedência de valores é: `constantValues` primeiro, parâmetros recebidos no request em segundo, `defaultValues` em terceiro.

🟢 **CONFIRMADO** — Quando `constantValues` existe, o valor do usuário é ignorado e o filtro também não deve ser documentado como parâmetro OpenAPI.

🟢 **CONFIRMADO** — Valores default e constantes podem ser resolvidos por `ValueExpressionResolver`; em contexto Spring MVC há fallback para `StringValueResolver`.

🟢 **CONFIRMADO** — Filtro obrigatório ausente interrompe a geração com erro, em vez de virar `NoOpStatement`.

🟢 **CONFIRMADO** — Operação dinâmica exige que o primeiro item seja uma string com código curto. Código de dois caracteres resolve operação positiva; código de três caracteres deve começar com `N`/`n` para negar a operação.

🟢 **CONFIRMADO** — Operação dinâmica `BT` exige exatamente dois valores e renomeia parâmetros para `<param>From` e `<param>To`.

🟢 **CONFIRMADO** — Operação dinâmica `IN` empacota múltiplos valores como array quando necessário.

### Árvore Lógica

🟢 **CONFIRMADO** — Ausência de filtros aplicáveis gera `NoOpStatement`, que no adaptador JPA vira `Specification.unrestricted()`.

🟢 **CONFIRMADO** — Filtro com `negate=true` é encapsulado em `NegatedStatement`; agrupamentos também podem ser negados por `negate` no bloco.

🟢 **CONFIRMADO** — Múltiplos blocos de filtros extraídos para um mesmo parâmetro são combinados por conjunction no nível raiz.

🟡 **INFERIDO** — O modelo de statements funciona como DSL intermediária estável, permitindo que outros adaptadores além de JPA possam ser adicionados sem alterar as annotations públicas.

### JPA e Criteria API

🟢 **CONFIRMADO** — Cada operação declarativa do core é mapeada para uma implementação `Specification*` registrada em `SpecificationFilterOperationService`.

🟢 **CONFIRMADO** — Paths simples usam `root.get(attribute)`; paths compostos criam joins para segmentos intermediários.

🟢 **CONFIRMADO** — Join type padrão é `INNER`; `ModJoinTypeLeft` muda para `LEFT`; `ModJoinTypeRight` muda para `RIGHT`.

🟢 **CONFIRMADO** — Joins existentes são reutilizados quando atributo e `JoinType` são iguais.

🟢 **CONFIRMADO** — `SpecificationIsIn` faz join também no segmento final quando o atributo é coleção e força `query.distinct(true)` para evitar duplicidade de entidade.

🟢 **CONFIRMADO** — `@Fetching` usa `LEFT` por padrão, não aplica fetch em count query, aplica `distinct(true)` em query normal e deduplica paths equivalentes.

🟢 **CONFIRMADO** — Tradução de `Sort` usa o primeiro parâmetro de cada `FilterRequestData` como chave para substituir ordenação por parâmetro externo pelo path real da entidade.

### Spring MVC

🟢 **CONFIRMADO** — O argument resolver aceita parâmetros `ConditionalStatement` e interfaces assignable para `Specification`.

🟢 **CONFIRMADO** — Query parameters escalares viram `String`, múltiplos valores viram `String[]` e URI template variables são mescladas depois dos query parameters, podendo sobrescrever chaves iguais.

🟢 **CONFIRMADO** — Para interfaces customizadas que estendem `Specification`, o resolver cria proxy dinâmico delegando chamadas ao `Specification` gerado.

🟢 **CONFIRMADO PELO USUÁRIO** — Suporte completo a default methods e métodos de `Object` em proxies customizados fica fora do contrato obrigatório; o suporte essencial é `toPredicate`.

### OpenAPI

🟢 **CONFIRMADO** — O customizador remove o parâmetro técnico do método controller e adiciona parâmetros OpenAPI individuais para cada filtro requisitável.

🟢 **CONFIRMADO** — Filtros com `constantValues` não aparecem no OpenAPI.

🟢 **CONFIRMADO** — `Dynamic` é documentado como array de strings com `minItems=2`; `IsIn` é documentado como array; `IsNull` é documentado como booleano.

🟢 **CONFIRMADO** — Parâmetro OpenAPI existente em `path` permanece em `path` e é sempre `required=true`.

🟢 **CONFIRMADO** — Constraints Jakarta Bean Validation do field alvo são propagadas para schemas numéricos, string e array.

🟢 **CONFIRMADO** — A condição inicial de `DynaFilterOperationCustomizer.customize` verifica `Disjunction.class` duas vezes e não verifica `DisjunctionFrom.class`; a reconstrução deve corrigir esse caso conforme confirmação do usuário.

## Regras Implícitas Extraídas Do Histórico Git

| Regra / decisão | Evidência Git | Confiança |
|---|---|---|
| Filtros decorados não devem aparecer no Swagger/OpenAPI como parâmetros comuns. | Commits `c6140b2` e `f27f37e` | 🟢 |
| Filtros com `constantValues` não devem aparecer no OpenAPI porque não são entrada do usuário. | Commit `e593d47` | 🟢 |
| Ordenação externa deve aceitar nome de parâmetro de filtro, mas executar pelo path real da entidade. | Commit `466d33c` | 🟢 |
| Validação de configuração de filtros deve acontecer o mais cedo possível, em warmup/metadados, não apenas por request. | Commits `a0b7f81`, `e69f53e`, `2884333` | 🟢 |
| Join type padrão foi deliberadamente definido como `INNER`. | Commit `a863cf9` | 🟢 |
| Cache e otimizações só são aceitos quando JMH confirma ausência de regressão relevante. | `docs/performance-history.md`, commits `3405adb`, `385532b`, `2884333` | 🟢 |
| Melhorias com ganho pequeno mas aumento de complexidade podem ser descartadas. | PERF-006 em `docs/performance-history.md` | 🟢 |
| Loops manuais não devem substituir streams por hipótese sem benchmark favorável. | PERF-008 em `docs/performance-history.md` | 🟢 |

## Lacunas

🔴 **LACUNA** — Não há domínio final de consumidor no módulo; qualquer regra de negócio de entidades reais deve ser extraída do projeto que usa esta biblioteca, não deste módulo.

🔴 **LACUNA** — Não há evidência de autenticação, autorização, papéis de usuário ou ACL em produção neste módulo.

🔴 **LACUNA** — Não há schema de banco produtivo, migrations ou entidades de produção; entidades JPA existentes são fixtures de teste.

🔴 **LACUNA** — Não foi encontrado log de runtime para análise de eventos recorrentes ou falhas operacionais.

🟢 **CONFIRMADO PELO USUÁRIO** — `TypeAnnotationUtils.findFilterField` deve falhar com `DynamicFilterConfigurationException` explícita para wildcards, tipos genéricos não materializados ou collection raw.
