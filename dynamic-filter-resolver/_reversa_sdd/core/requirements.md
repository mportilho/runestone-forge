# Core

> Spec operacional gerada pelo Reversa Writer em 2026-05-16.
> Escala de confiança: 🟢 CONFIRMADO, 🟡 INFERIDO, 🔴 LACUNA.

## Visão Geral

🟢 **CONFIRMADO** — A unit `core` define o núcleo agnóstico de framework do `dynamic-filter-resolver`, localizado em `src/main/java/com/runestone/dynafilter/core/`.

🟢 **CONFIRMADO** — Sua função é transformar annotations e parâmetros de filtro em uma árvore lógica intermediária (`StatementWrapper`) composta por filtros, composições, negações e ausência de filtro.

🟡 **INFERIDO** — O `core` é a base de extensibilidade para adaptadores como JPA e OpenAPI, permitindo que novas integrações consumam a mesma árvore sem alterar as annotations públicas.

## Responsabilidades

- 🟢 **CONFIRMADO** — Declarar o contrato de filtros por annotations como `@Filter`, `@Conjunction`, `@Disjunction`, `@ConjunctionFrom`, `@DisjunctionFrom`, `@Statement`, `@StatementFrom`, `@FilterTarget` e `@FilterDecorators`.
- 🟢 **CONFIRMADO** — Extrair annotations diretas, meta-annotations, interfaces não-`java.*`, superclasses e annotations de parâmetro por meio de `TypeAnnotationUtils`.
- 🟢 **CONFIRMADO** — Validar metadados de filtro, incluindo parâmetros obrigatórios, tamanhos compatíveis de arrays e existência de paths quando a entidade alvo é conhecida.
- 🟢 **CONFIRMADO** — Computar valores efetivos de filtro com precedência `constantValues > parâmetros recebidos > defaultValues`.
- 🟢 **CONFIRMADO** — Resolver operações dinâmicas a partir de códigos curtos como `EQ`, `IN`, `BT` e prefixo de negação `N`/`n`.
- 🟢 **CONFIRMADO** — Construir a árvore de statements com `LogicalStatement`, `CompoundStatement`, `NegatedStatement` e `NoOpStatement`.
- 🟢 **CONFIRMADO** — Separar filtros decorados e listar todos os filtros requisitáveis em `StatementWrapper` para consumidores como JPA e OpenAPI.
- 🟢 **CONFIRMADO** — Expor portas de extensão para operação (`FilterOperationService`), resolução (`DynamicFilterResolver`) e decoração (`FilterDecorator`).

## Regras de Negócio

- 🟢 **CONFIRMADO** — Um `@Filter` deve possuir lista de `parameters`; filtros sem parâmetros ou com nomes vazios são inválidos.
- 🟢 **CONFIRMADO** — `defaultValues` e `constantValues`, quando presentes, devem ter o mesmo tamanho de `parameters`.
- 🟢 **CONFIRMADO** — `constantValues` têm prioridade absoluta e ignoram valores enviados pelo usuário.
- 🟢 **CONFIRMADO** — Na ausência de constantes, valores enviados no mapa de parâmetros têm prioridade sobre `defaultValues`.
- 🟢 **CONFIRMADO** — `ValueExpressionResolver` pode transformar strings simples ou strings dentro de arrays antes de construir o filtro.
- 🟢 **CONFIRMADO** — Filtro obrigatório ausente interrompe a geração com erro, em vez de produzir `NoOpStatement`.
- 🟢 **CONFIRMADO** — Ausência de filtros aplicáveis, sem erro obrigatório, gera `NoOpStatement`.
- 🟢 **CONFIRMADO** — `@Conjunction` combina filtros por AND e aceita sub-statements por OR; `@Disjunction` combina filtros por OR e aceita sub-statements por AND.
- 🟢 **CONFIRMADO** — Vários blocos raiz extraídos para o mesmo input são combinados por `LogicOperator.CONJUNCTION`.
- 🟢 **CONFIRMADO** — Operação `Dynamic` exige valor em formato de array; o primeiro item deve ser string com código de operação.
- 🟢 **CONFIRMADO** — Código dinâmico de 2 caracteres resolve operação positiva; código de 3 caracteres deve iniciar com `N`/`n` e gera filtro negado.
- 🟢 **CONFIRMADO** — Operação dinâmica `IN` empacota múltiplos valores como um array quando necessário.
- 🟢 **CONFIRMADO** — Operação dinâmica `BT` exige exatamente dois valores e renomeia parâmetros para `<param>From` e `<param>To`.
- 🟢 **CONFIRMADO** — `FilterDecorator` deve ser thread-safe e stateless; `CompositeFilterDecorator` rejeita retorno `null` de decorator.
- 🟢 **CONFIRMADO** — O legado assume que collections em dot-notation expõem `ParameterizedType` cujo primeiro argumento é `Class<?>`; collection raw, wildcard ou tipo genérico não materializado podem falhar por cast em runtime (`TypeAnnotationUtils.java:345-349`).

## Requisitos Funcionais

| ID | Requisito | Prioridade | Critério de Aceite |
|----|-----------|-----------|-------------------|
| RF-CORE-01 | 🟢 Extrair metadados de filtros a partir de annotations diretas, meta-annotations, interfaces e superclasses. | Must | Dado um `AnnotationStatementInput`, quando `TypeAnnotationUtils.findAnnotationData` for chamado, então deve retornar `FilterAnnotationData` correspondente aos filtros declarados e reutilizar cache para entradas equivalentes. |
| RF-CORE-02 | 🟢 Validar contratos de filtro antes de gerar statements. | Must | Dado um filtro sem parâmetros ou com arrays incompatíveis, quando o metadata for processado, então deve ocorrer erro de configuração em vez de gerar statement inválido. |
| RF-CORE-03 | 🟢 Gerar `StatementWrapper` a partir de annotations e mapa de parâmetros. | Must | Dado input anotado e parâmetros aplicáveis, quando `AnnotationStatementGenerator.generateStatements` for chamado, então deve retornar `StatementWrapper` com statement, filtros decorados e catálogo de filtros requisitáveis. |
| RF-CORE-04 | 🟢 Produzir `NoOpStatement` quando nenhum filtro aplicável existir. | Must | Dado input válido sem valores enviados e sem filtros obrigatórios ausentes, quando a geração ocorrer, então o statement raiz deve representar ausência de filtro. |
| RF-CORE-05 | 🟢 Aplicar precedência de valores `constantValues > request > defaultValues`. | Must | Dado filtro com constantes, parâmetros e defaults, quando os valores forem computados, então constantes devem prevalecer sobre qualquer entrada do usuário. |
| RF-CORE-06 | 🟢 Resolver expressões de default e constante por `ValueExpressionResolver`. | Should | Dado default ou constante string resolvível, quando a geração ocorrer, então o valor efetivo deve ser o valor resolvido ou erro encapsulado em `StatementGenerationException`. |
| RF-CORE-07 | 🟢 Interpretar operação dinâmica por código curto. | Must | Dado valor dinâmico como array com primeiro item `EQ`, `IN`, `BT` ou equivalente negado, quando `createFilterData` processar o filtro, então a operação concreta e a negação devem ser derivadas do código. |
| RF-CORE-08 | 🟢 Construir árvore lógica com composição e negação. | Must | Dado múltiplos filtros e sub-statements, quando statements forem criados, então folhas devem virar `LogicalStatement`, combinações devem virar `CompoundStatement` e filtros negados devem virar `NegatedStatement`. |
| RF-CORE-09 | 🟢 Expor contratos de operação e resolução sem acoplar o core a JPA. | Must | Dado um adaptador externo, quando ele implementar `DynamicFilterResolver` e `FilterOperationService`, então deve conseguir consumir a árvore do core sem dependência direta do Spring MVC runtime. |
| RF-CORE-10 | 🟢 Compor decorators em ordem e falhar se algum decorator retornar `null`. | Should | Dado `FilterDecorator.of` com múltiplos decorators, quando a composição for aplicada, então cada decorator deve receber o resultado anterior e retorno `null` deve gerar erro. |
| RF-CORE-11 | 🟡 Evitar regressões de performance no cache de annotations. | Should | Dado entradas equivalentes de `AnnotationStatementInput`, quando metadados forem buscados repetidamente, então o cache limitado por Caffeine deve reduzir recomputação conforme testes existentes. |
| RF-CORE-12 | 🟢 Falhar explicitamente para fields genéricos não triviais. | Could | Dado path que passa por collection raw, wildcard ou generic não materializado, quando `findFilterField` for chamado, então deve ocorrer `DynamicFilterConfigurationException` clara em vez de `ClassCastException` implícita. |

## Requisitos Não Funcionais

| Tipo | Requisito inferido | Evidência no código | Confiança |
|------|--------------------|---------------------|-----------|
| Performance | Cache de metadados de annotations deve ser limitado e reaproveitar entradas equivalentes. | `src/main/java/com/runestone/dynafilter/core/generator/annotation/TypeAnnotationUtils.java:44-50`, `TestTypeAnnotationUtils.java:139-181` | 🟢 |
| Performance | `AnnotationStatementInput` deve usar cópia defensiva e hash pré-computado para estabilidade de chave de cache. | `src/main/java/com/runestone/dynafilter/core/generator/annotation/AnnotationStatementInput.java:37-40` | 🟢 |
| Extensibilidade | O core deve expor portas (`DynamicFilterResolver`, `FilterOperationService`, `FilterDecorator`) sem depender de implementação JPA concreta. | `src/main/java/com/runestone/dynafilter/core/resolver/DynamicFilterResolver.java`, `src/main/java/com/runestone/dynafilter/core/operation/FilterOperationService.java` | 🟢 |
| Segurança indireta | Paths filtráveis são definidos por annotations no código, não por path arbitrário enviado pelo usuário. | `@Filter.path`, `TypeAnnotationUtils.findFilterField` | 🟢 |
| Robustez | Erros de geração e operação não registrada devem falhar explicitamente. | `StatementGenerationException`, `FilterOperationNotDefinedException`, `DynamicFilterConfigurationException` | 🟢 |

> Inferido a partir do código e dos artefatos Reversa. Validar com a equipe antes de transformar requisitos não funcionais em SLOs formais.

## Critérios de Aceitação

```gherkin
Cenário: gerar statement com filtro aplicável
Dado um AnnotationStatementInput com annotation de filtro válida
E um mapa de parâmetros contendo o nome esperado pelo filtro
Quando AnnotationStatementGenerator.generateStatements for executado
Então deve retornar um StatementWrapper com LogicalStatement ou CompoundStatement correspondente
E o filtro deve conter valores computados conforme a precedência definida

Cenário: gerar NoOpStatement sem filtros aplicáveis
Dado um AnnotationStatementInput válido sem filtros obrigatórios ausentes
E um mapa de parâmetros vazio
Quando AnnotationStatementGenerator.generateStatements for executado
Então deve retornar um StatementWrapper cujo statement representa NoOpStatement

Cenário: rejeitar filtro obrigatório ausente
Dado um filtro marcado como required
E ausência do parâmetro correspondente no mapa de entrada
Quando AnnotationStatementGenerator.generateStatements for executado
Então a geração deve falhar com erro de parâmetro obrigatório ausente

Cenário: aplicar constantValues acima de parâmetros do usuário
Dado um filtro com constantValues e parâmetro de mesmo nome enviado pelo usuário
Quando DefaultStatementGenerator.computeValues calcular os valores
Então o valor constante deve ser usado
E o valor enviado pelo usuário deve ser ignorado

Cenário: resolver operação dinâmica positiva
Dado um filtro com operation Dynamic
E valores cujo primeiro item é "GE"
Quando DefaultStatementGenerator.createFilterData processar o filtro
Então a operação resultante deve ser GreaterOrEquals
E o filtro não deve ser marcado como negado

Cenário: rejeitar operação dinâmica BETWEEN inválida
Dado um filtro com operation Dynamic
E valores cujo primeiro item é "BT" sem exatamente dois valores de intervalo
Quando DefaultStatementGenerator.createFilterData processar o filtro
Então a geração deve falhar com erro de formato da operação dinâmica

Cenário: compor decorators sem aceitar retorno nulo
Dado um CompositeFilterDecorator com múltiplos decorators
E um decorator que retorna null
Quando a composição for aplicada
Então o fluxo deve falhar explicitamente em vez de retornar filtro nulo
```

## Prioridade (MoSCoW)

| Requisito | MoSCoW | Justificativa |
|-----------|--------|---------------|
| RF-CORE-01 | Must | Extração de metadados é a entrada de todos os fluxos de geração e documentação. |
| RF-CORE-02 | Must | Metadados inválidos contaminam todos os adaptadores consumidores. |
| RF-CORE-03 | Must | `StatementWrapper` é o contrato operacional consumido por JPA e OpenAPI. |
| RF-CORE-04 | Must | `NoOpStatement` define o comportamento seguro quando nenhum filtro se aplica. |
| RF-CORE-05 | Must | Precedência de valores altera diretamente o resultado de consultas e filtros constantes. |
| RF-CORE-07 | Must | Operação dinâmica é contrato público de filtros flexíveis. |
| RF-CORE-08 | Must | A árvore lógica preserva AND, OR e negação para todos os adaptadores. |
| RF-CORE-09 | Must | Separação core/adapters é a decisão arquitetural central do módulo. |
| RF-CORE-06 | Should | Resolução de expressões é importante, mas existe fallback para valores literais. |
| RF-CORE-10 | Should | Decorators estendem comportamento, mas nem todo filtro precisa deles. |
| RF-CORE-11 | Should | Cache é relevante para performance, mas não muda a semântica funcional. |
| RF-CORE-12 | Could | Casos de generics/wildcards são lacunas de borda ainda não validadas. |

> Prioridade inferida por posição na cadeia de dependências, cobertura de testes e impacto nos adaptadores JPA/OpenAPI.

## Rastreabilidade de Código

| Arquivo | Função / Classe | Cobertura |
|---------|-----------------|-----------|
| `src/main/java/com/runestone/dynafilter/core/generator/StatementGenerator.java` | `StatementGenerator` | 🟢 |
| `src/main/java/com/runestone/dynafilter/core/generator/DefaultStatementGenerator.java` | `createStatements`, `createFilterData`, `computeValues` | 🟢 |
| `src/main/java/com/runestone/dynafilter/core/generator/ValueExpressionResolver.java` | `ValueExpressionResolver` | 🟢 |
| `src/main/java/com/runestone/dynafilter/core/generator/StatementWrapper.java` | `StatementWrapper` | 🟢 |
| `src/main/java/com/runestone/dynafilter/core/generator/ConditionalStatement.java` | `ConditionalStatement` | 🟢 |
| `src/main/java/com/runestone/dynafilter/core/generator/ConditionalStatementBuilder.java` | Código comentado, sem símbolos ativos | n/a |
| `src/main/java/com/runestone/dynafilter/core/generator/annotation/AnnotationStatementGenerator.java` | `generateStatements`, `createStatements`, `createStatementFromFilterStatements` | 🟢 |
| `src/main/java/com/runestone/dynafilter/core/generator/annotation/TypeAnnotationUtils.java` | `findAnnotationData`, `listAllFilterRequestData`, `findFilterField`, `findFilterTargetClass` | 🟢 |
| `src/main/java/com/runestone/dynafilter/core/generator/annotation/AnnotationStatementInput.java` | Cache key e cópia defensiva | 🟢 |
| `src/main/java/com/runestone/dynafilter/core/generator/annotation/Filter.java` | Contrato de filtro | 🟢 |
| `src/main/java/com/runestone/dynafilter/core/generator/annotation/Conjunction.java` | Grupo AND | 🟢 |
| `src/main/java/com/runestone/dynafilter/core/generator/annotation/Disjunction.java` | Grupo OR | 🟢 |
| `src/main/java/com/runestone/dynafilter/core/generator/annotation/ConjunctionFrom.java` | Grupo externo AND | 🟢 |
| `src/main/java/com/runestone/dynafilter/core/generator/annotation/DisjunctionFrom.java` | Grupo externo OR | 🟢 |
| `src/main/java/com/runestone/dynafilter/core/generator/annotation/FilterTarget.java` | Entidade alvo | 🟢 |
| `src/main/java/com/runestone/dynafilter/core/generator/annotation/FilterDecorators.java` | Declaração de decorators | 🟢 |
| `src/main/java/com/runestone/dynafilter/core/model/FilterData.java` | Dados operacionais do filtro | 🟢 |
| `src/main/java/com/runestone/dynafilter/core/model/FilterRequestData.java` | Dados requisitáveis/documentáveis | 🟢 |
| `src/main/java/com/runestone/dynafilter/core/model/statement/*` | Árvore lógica de statements | 🟢 |
| `src/main/java/com/runestone/dynafilter/core/operation/ComparisonOperation.java` | Códigos de operação dinâmica | 🟢 |
| `src/main/java/com/runestone/dynafilter/core/operation/AbstractFilterOperationService.java` | Dispatch de operação | 🟢 |
| `src/main/java/com/runestone/dynafilter/core/resolver/DynamicFilterResolver.java` | Porta de resolução | 🟢 |
| `src/main/java/com/runestone/dynafilter/core/resolver/FilterDecorator.java` | Contrato de decorator | 🟢 |
| `src/main/java/com/runestone/dynafilter/core/resolver/CompositeFilterDecorator.java` | Composição de decorators | 🟢 |
| `src/main/java/com/runestone/dynafilter/core/exceptions/*.java` | Erros de geração/configuração/operação | 🟢 |

## Lacunas e Perguntas Relacionadas

- 🟢 **CONFIRMADO** — A limitação técnica atual de `TypeAnnotationUtils.findFilterField` para collections raw, wildcard e tipos genéricos não materializados foi confirmada no código (`TypeAnnotationUtils.java:345-349`).
- 🟢 **CONFIRMADO PELO USUÁRIO** — Na reconstrução, a limitação de collection raw, wildcard e tipos genéricos não materializados deve falhar com `DynamicFilterConfigurationException` explícita.
- 🟡 **INFERIDO** — Confirmar se outros adaptadores além de JPA são objetivo explícito de evolução ou apenas consequência da separação arquitetural atual.
