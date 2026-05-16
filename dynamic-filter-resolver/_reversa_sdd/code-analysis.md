# Análise de Código — dynamic-filter-resolver

> Gerado pelo Reversa Archaeologist em 2026-05-16.
> Escala de confiança: 🟢 CONFIRMADO, 🟡 INFERIDO, 🔴 LACUNA.

## Módulo `core`

### Propósito

🟢 CONFIRMADO — O módulo `core` define o modelo intermediário e o pipeline independente de implementação para transformar anotações de filtro em uma árvore de statements. Essa árvore é consumida por módulos concretos, como JPA, por meio das abstrações `StatementGenerator`, `DynamicFilterResolver`, `FilterOperationService` e `FilterDecorator`.

Evidências principais:
- `src/main/java/com/runestone/dynafilter/core/generator/StatementGenerator.java:41` define o contrato para gerar `StatementWrapper`.
- `src/main/java/com/runestone/dynafilter/core/generator/annotation/AnnotationStatementGenerator.java:52` implementa geração a partir de anotações.
- `src/main/java/com/runestone/dynafilter/core/resolver/DynamicFilterResolver.java:30` define o contrato de resolução para o tipo de filtro concreto.
- `src/main/java/com/runestone/dynafilter/core/operation/FilterOperationService.java:1` e `AbstractFilterOperationService.java:43` definem o dispatch de operações.

### Arquivos Primários

🟢 CONFIRMADO
- `src/main/java/com/runestone/dynafilter/core/generator/annotation/AnnotationStatementGenerator.java`
- `src/main/java/com/runestone/dynafilter/core/generator/annotation/TypeAnnotationUtils.java`
- `src/main/java/com/runestone/dynafilter/core/generator/DefaultStatementGenerator.java`
- `src/main/java/com/runestone/dynafilter/core/generator/StatementWrapper.java`
- `src/main/java/com/runestone/dynafilter/core/model/FilterData.java`
- `src/main/java/com/runestone/dynafilter/core/model/FilterRequestData.java`
- `src/main/java/com/runestone/dynafilter/core/model/statement/*`
- `src/main/java/com/runestone/dynafilter/core/operation/*`
- `src/main/java/com/runestone/dynafilter/core/resolver/*`

### Inspeção Semântica Java LSP

🟢 CONFIRMADO — A inspeção foi feita com `jdtls` usando `textDocument/documentSymbol` e diagnósticos para os arquivos Java do pacote `com.runestone.dynafilter.core`.

Resultado:
- Foram extraídos símbolos semânticos de classes, records, annotations, enums, interfaces e métodos do módulo `core`.
- Não apareceram erros de compilação via diagnóstico LSP nos arquivos inspecionados.
- O LSP reportou avisos em `DefinedFilterOperation.java:30` sobre uso de raw types para interfaces genéricas como `Between<T>`, `Decorated<T>`, `Dynamic<T>`, `Equals<T>`, `Like<T>` e demais operações.

### Fluxo de Controle Principal

#### Geração de statements a partir de anotações

🟢 CONFIRMADO — `AnnotationStatementGenerator.generateStatements` executa o fluxo abaixo:

1. Normaliza `filterParameters` nulo para `Collections.emptyMap()` (`AnnotationStatementGenerator.java:54`).
2. Extrai metadados de anotações com cache via `TypeAnnotationUtils.findAnnotationData` (`AnnotationStatementGenerator.java:57`).
3. Para cada `FilterAnnotationData`, monta statements com `createStatements(data, parametersMap)` (`AnnotationStatementGenerator.java:58-63`).
4. Separa filtros decorados (`Decorated.class`) em `decoratedFilters` (`AnnotationStatementGenerator.java:65`, `AnnotationStatementGenerator.java:81-92`).
5. Lista todos os filtros requisitáveis para documentação/OpenAPI via `TypeAnnotationUtils.listAllFilterRequestData` (`AnnotationStatementGenerator.java:66`).
6. Retorna `NoOpStatement` quando nenhum statement aplicável foi criado (`AnnotationStatementGenerator.java:68-70`).
7. Retorna statement único quando só existe um bloco (`AnnotationStatementGenerator.java:70-72`).
8. Combina blocos múltiplos com `LogicOperator.CONJUNCTION` (`AnnotationStatementGenerator.java:72-78`).

#### Construção de árvores lógicas

🟢 CONFIRMADO — A árvore usa quatro tipos principais:
- `LogicalStatement` encapsula um `FilterData` (`LogicalStatement.java:29-49`).
- `CompoundStatement` combina dois statements e um `LogicOperator` (`CompoundStatement.java:27-61`).
- `NegatedStatement` encapsula um statement negado (`NegatedStatement.java:27-47`).
- `NoOpStatement` representa ausência de filtro aplicável, conforme contrato de `StatementGenerator` (`StatementGenerator.java:47-49`).

🟢 CONFIRMADO — Em `DefaultStatementGenerator.createStatements`, o primeiro filtro vira `LogicalStatement`; os seguintes são encadeados em `CompoundStatement` com o operador recebido (`DefaultStatementGenerator.java:52-60`). Cada filtro pode ser envolvido em `NegatedStatement` quando `filterData.negate()` é verdadeiro (`DefaultStatementGenerator.java:63-66`).

#### Agrupamento por `Conjunction` e `Disjunction`

🟢 CONFIRMADO — `Conjunction` usa `AND` como operação principal e aceita sub-statements disjuntivos (`Conjunction.java:35-68`). `Disjunction` usa `OR` como operação principal e aceita sub-statements conjuntivos (`Disjunction.java:35-67`).

🟢 CONFIRMADO — `AnnotationStatementGenerator.createStatementFromFilterStatements` aplica `logicType.opposite()` dentro dos sub-statements e combina os sub-statements entre si com o `logicType` do bloco externo (`AnnotationStatementGenerator.java:116-130`).

### Algoritmos e Regras Embutidas

#### Precedência de valores

🟢 CONFIRMADO — `DefaultStatementGenerator.computeValues` impõe a precedência:
- `constantValues` têm prioridade absoluta e ignoram o mapa de parâmetros (`DefaultStatementGenerator.java:151-153`).
- Na ausência de constantes, usa valores do `parametersMap` quando a chave existe (`DefaultStatementGenerator.java:163-165`).
- Se a chave não existe, tenta `defaultValues` com resolução opcional por `ValueExpressionResolver` (`DefaultStatementGenerator.java:154`, `DefaultStatementGenerator.java:178-192`).

Regras de validação:
- `parameters` não pode ser nulo ou vazio (`DefaultStatementGenerator.java:143-145`).
- `defaultValues` e `constantValues`, quando presentes, devem ter o mesmo tamanho de `parameters` (`DefaultStatementGenerator.java:145-149`).
- Cada nome de parâmetro deve ser não vazio (`DefaultStatementGenerator.java:159-162`).

#### Resolução de expressões

🟢 CONFIRMADO — `ValueExpressionResolver` é um ponto de extensão funcional para transformar strings em valores dinâmicos (`ValueExpressionResolver.java:35-44`). `DefaultStatementGenerator` aplica o resolver a strings simples e a cada string dentro de arrays (`DefaultStatementGenerator.java:178-191`). Erros do resolver são encapsulados em `StatementGenerationException` (`DefaultStatementGenerator.java:224-229`).

#### Operação dinâmica

🟢 CONFIRMADO — Quando `operation == Dynamic.class`, o primeiro valor enviado pelo usuário define a operação real (`DefaultStatementGenerator.java:91-124`).

Regras confirmadas:
- O valor dinâmico deve ser `Object[]`; caso contrário, lança `StatementGenerationException` (`DefaultStatementGenerator.java:91-99`, `DefaultStatementGenerator.java:122-124`).
- O primeiro item deve ser `String`; caso contrário, lança erro específico (`DefaultStatementGenerator.java:94-98`).
- Código de 2 caracteres, como `GE`, `IN`, `BT`, resolve uma `ComparisonOperation` sem negação (`DefaultStatementGenerator.java:107-110`).
- Código de 3 caracteres deve iniciar por `N`/`n`; o restante resolve a operação e marca `negate = true` (`DefaultStatementGenerator.java:100-106`).
- `IN` empacota múltiplos valores em array único quando o primeiro valor ainda não é array (`DefaultStatementGenerator.java:114-116`).
- `BT` exige exatamente dois valores e renomeia parâmetros para `<param>From` e `<param>To` (`DefaultStatementGenerator.java:116-121`).

#### Extração e cache de metadados de anotações

🟢 CONFIRMADO — `TypeAnnotationUtils` usa Caffeine para cache limitado de `AnnotationMetadata`, com tamanho padrão `4096` e override por system property `runestone.dynafilter.annotation.cache.max-size` (`TypeAnnotationUtils.java:44-50`, `TypeAnnotationUtils.java:429-439`). O executor do cache é síncrono (`Runnable::run`) (`TypeAnnotationUtils.java:49`).

🟢 CONFIRMADO — O cache usa `AnnotationStatementInput` como chave. Essa classe faz clone defensivo de `annotations` e pré-calcula o hash no construtor (`AnnotationStatementInput.java:37-40`). Os testes confirmam hit de cache com entradas equivalentes e limite de tamanho (`TestTypeAnnotationUtils.java:139-181`).

#### Descoberta de anotações em classes e interfaces

🟢 CONFIRMADO — `TypeAnnotationUtils.findStatementAnnotations` combina anotações extraídas do tipo base, de interfaces não-`java.*`, superclasses e anotações diretas do parâmetro (`TypeAnnotationUtils.java:244-250`, `TypeAnnotationUtils.java:254-275`). A extração ignora meta-anotações de `java.lang.annotation` (`TypeAnnotationUtils.java:281-288`, `TypeAnnotationUtils.java:291-302`).

🟢 CONFIRMADO — `getAllAnnotations` percorre meta-anotações recursivamente e retorna como processáveis apenas `Conjunction`, `ConjunctionFrom`, `Disjunction` e `DisjunctionFrom` (`TypeAnnotationUtils.java:305-327`).

#### Resolução de campos por path

🟢 CONFIRMADO — `TypeAnnotationUtils.findFilterField` navega paths separados por ponto, descendo por propriedades simples ou pelo tipo genérico de coleções (`TypeAnnotationUtils.java:337-356`). Se o campo não existe na classe nem em superclasses, lança `DynamicFilterConfigurationException` (`TypeAnnotationUtils.java:359-366`).

🔴 LACUNA — O método assume que campos de coleção têm `ParameterizedType` com primeiro argumento `Class<?>`. Não foi validado neste módulo como ele se comporta com wildcard, tipo genérico não materializado ou coleção raw.

#### Descoberta da entidade-alvo

🟢 CONFIRMADO — `TypeAnnotationUtils.findFilterTargetClass` identifica a classe alvo por:
- `@ConjunctionFrom` ou `@DisjunctionFrom` no parâmetro, exigindo `@FilterTarget` na classe de configuração (`TypeAnnotationUtils.java:380-395`).
- Parâmetro anotado com `@Conjunction`/`@Disjunction` e tipo compatível com `Specification<T>` (`TypeAnnotationUtils.java:399-402`).
- Parâmetro do tipo `ConditionalStatement`, exigindo `@FilterTarget` direto no parâmetro (`TypeAnnotationUtils.java:402-408`).

### Tratamento de Erros

🟢 CONFIRMADO
- `FilterData` valida que `parameters` e `values` existem e têm tamanhos iguais (`FilterData.java:60-69`).
- `FilterData.findOneValue` rejeita múltiplos valores (`FilterData.java:92-98`).
- `AnnotationStatementGenerator` rejeita parâmetro obrigatório ausente (`AnnotationStatementGenerator.java:146-153`).
- `TypeAnnotationUtils.validateFilter` rejeita filtros sem parâmetros e tamanhos divergentes entre parâmetros, constantes e defaults (`TypeAnnotationUtils.java:176-188`).
- `AbstractFilterOperationService` rejeita `FilterData` nulo e operação sem implementação registrada (`AbstractFilterOperationService.java:43-50`).
- `CompositeFilterDecorator` rejeita filtro/statement nulos e decorador que retorna `null` (`CompositeFilterDecorator.java:42-50`).

### Estruturas de Dados

🟢 CONFIRMADO — O módulo não define entidades JPA. Ele define modelos imutáveis/records e nós de árvore lógica:
- `FilterData`: valor operacional já resolvido para criação de filtro.
- `FilterRequestData`: descrição do que pode ser solicitado ao chamador.
- `StatementWrapper`: raiz da árvore, filtros decorados e catálogo de filtros requisitáveis.
- `ConditionalStatement`: wrapper de `StatementWrapper` com `FilterDecorator<?>`.
- `FilterAnnotationData` e `FilterAnnotationStatement`: agregados internos para metadados extraídos de anotações.
- `LogicalStatement`, `CompoundStatement`, `NegatedStatement`, `NoOpStatement`: árvore de decisão visitável/analisável.

### Operações Suportadas

🟢 CONFIRMADO — `ComparisonOperation` mapeia códigos curtos para operações declarativas (`ComparisonOperation.java:29-50`):
- `EQ` → `Equals`
- `LT` → `Less`
- `LE` → `LessOrEquals`
- `GT` → `Greater`
- `GE` → `GreaterOrEquals`
- `LK` → `Like`
- `SW` → `StartsWith`
- `EW` → `EndsWith`
- `IN` → `IsIn`
- `BT` → `Between`

🟢 CONFIRMADO — `DefinedFilterOperation` é uma interface marcador/agregadora que estende as interfaces de operação (`DefinedFilterOperation.java:29-31`).

### Decorators

🟢 CONFIRMADO — `FilterDecorator` declara que implementações devem ser thread-safe e stateless (`FilterDecorator.java:31-35`). `FilterDecorator.of` cria um `CompositeFilterDecorator`, que aplica decorators em ordem e interrompe com erro se algum retorno for `null` (`FilterDecorator.java:54-56`, `CompositeFilterDecorator.java:45-50`).

🟢 CONFIRMADO — Decorators são descobertos tanto por `@FilterDecorators` no tipo quanto por `@FilterDecorators` nas anotações diretas e nas classes referenciadas por `@ConjunctionFrom`/`@DisjunctionFrom` (`TypeAnnotationUtils.java:55-101`).

### Pontos de Atenção

🟡 INFERIDO — `AnnotationStatementGenerator.createStatementFromFilterStatements` pode envolver `null` em `NegatedStatement` se `processFilterAnnotations` retornar array vazio para um sub-statement sem filtros aplicáveis (`AnnotationStatementGenerator.java:116-123`). A árvore resultante pode depender dos consumidores lidarem com esse caso. Não foi confirmado por teste negativo direto neste módulo.

🟢 CONFIRMADO — Há código antigo comentado integralmente em `ConditionalStatementBuilder.java:1-46`; o LSP não o trata como símbolo ativo. Esse arquivo não participa do comportamento atual.

🟢 CONFIRMADO — O LSP reporta raw types suprimidos em `DefinedFilterOperation.java:29-31`; isso não impede uso atual, mas reduz precisão de tipos genéricos para as operações agregadas.

### Testes Relevantes

🟢 CONFIRMADO
- `TestDefaultStatementGenerator` cobre precedência de valores, defaults, constantes, resolução de expressão e validações de tamanho/nome (`TestDefaultStatementGenerator.java:73-248`).
- `TestAnnotationStatementGenerator` cobre `NoOpStatement`, constantes, parâmetros requeridos, expressão, negation e filtros decorados (`TestAnnotationStatementGenerator.java:63-210`).
- `TestStatementGeneratorWithDynamicFilters` cobre operações dinâmicas positivas, negadas, `IN`, `BT` e erros de formato (`TestStatementGeneratorWithDynamicFilters.java:43-267`).
- `TestTypeAnnotationUtils` cobre extração por annotation/type/interface, cache, eviction e cópia defensiva (`TestTypeAnnotationUtils.java:40-181`).

## Módulo `modules.jpa`

### Propósito

🟢 CONFIRMADO — O módulo `modules.jpa` adapta a árvore lógica do `core` para Spring Data JPA `Specification<?>`, integra a resolução em controllers Spring MVC e adiciona APIs de repository que aceitam `ConditionalStatement` diretamente.

Evidências principais:
- `src/main/java/com/runestone/dynafilter/modules/jpa/operation/SpecificationFilterOperationService.java:38` registra operações declarativas em implementações JPA `Specification`.
- `src/main/java/com/runestone/dynafilter/modules/jpa/resolver/SpecificationDynamicFilterResolver.java:35` converte `StatementWrapper` em `Specification<?>`.
- `src/main/java/com/runestone/dynafilter/modules/jpa/spring/SpecificationDynamicFilterArgumentResolver.java:49` resolve parâmetros MVC como `ConditionalStatement` ou interfaces `Specification`.
- `src/main/java/com/runestone/dynafilter/modules/jpa/repository/DynamicFilterJpaRepository.java:50` expõe métodos de repository dinâmico.

### Arquivos Primários

🟢 CONFIRMADO
- `src/main/java/com/runestone/dynafilter/modules/jpa/operation/SpecificationFilterOperationService.java`
- `src/main/java/com/runestone/dynafilter/modules/jpa/operation/specification/*`
- `src/main/java/com/runestone/dynafilter/modules/jpa/resolver/SpecificationDynamicFilterResolver.java`
- `src/main/java/com/runestone/dynafilter/modules/jpa/resolver/SpecificationStatementAnalyser.java`
- `src/main/java/com/runestone/dynafilter/modules/jpa/resolver/FetchingFilterDecorator.java`
- `src/main/java/com/runestone/dynafilter/modules/jpa/repository/DynamicFilterJpaRepository.java`
- `src/main/java/com/runestone/dynafilter/modules/jpa/repository/DynamicFilterJpaRepositoryImpl.java`
- `src/main/java/com/runestone/dynafilter/modules/jpa/repository/DynamicFilterJpaRepositoryBeanPostProcessor.java`
- `src/main/java/com/runestone/dynafilter/modules/jpa/spring/*`

### Inspeção Semântica Java

🟡 INFERIDO — Por preferência do usuário, a análise deve priorizar LSP Java nas próximas leituras semânticas. Nesta sessão, a interface do agente não expôs um endpoint LSP direto; a análise de `modules.jpa` foi feita por inspeção estática dos arquivos Java e dos testes JUnit relevantes. Não foram executados diagnósticos LSP neste módulo.

### Fluxo de Controle Principal

#### Registro das operações JPA

🟢 CONFIRMADO — `SpecificationFilterOperationService` instancia um `AbstractFilterOperationService<Specification<?>>` com mapa de operações declarativas para classes `Specification*` (`SpecificationFilterOperationService.java:40-55`). O mapeamento confirmado inclui `Between`, `EndsWith`, `Equals`, `Greater`, `GreaterOrEquals`, `IsIn`, `IsNull`, `Less`, `LessOrEquals`, `Like` e `StartsWith`.

#### Conversão da árvore lógica em `Specification`

🟢 CONFIRMADO — `SpecificationDynamicFilterResolver.createFilter` valida `StatementWrapper`, chama `statementWrapper.statement().acceptAnalyser(statementAnalyser)` e aplica `FilterDecorator` quando fornecido (`SpecificationDynamicFilterResolver.java:45-48`).

🟢 CONFIRMADO — `SpecificationStatementAnalyser` traduz os nós do `core` assim:
- `NegatedStatement` → `Specification.not(specification)` (`SpecificationStatementAnalyser.java:40-42`).
- `LogicalStatement` → `filterOperationService.createFilter(filterData)` (`SpecificationStatementAnalyser.java:46-48`).
- `CompoundStatement` → `left.and(right)` ou `left.or(right)` conforme `LogicOperator` (`SpecificationStatementAnalyser.java:52-55`).
- `NoOpStatement` → `Specification.unrestricted()` (`SpecificationStatementAnalyser.java:59-60`).

#### Resolução de paths e joins no Criteria API

🟢 CONFIRMADO — `JpaPredicateUtils.computeAttributePath` normaliza `filterData.path()`, usa cache Caffeine de paths parseados com limite de 4096 entradas e cria joins para segmentos intermediários (`JpaPredicateUtils.java:45-79`).

Regras confirmadas:
- Paths simples retornam `root.get(attribute)` (`JpaPredicateUtils.java:137-145`).
- Paths compostos criam/reutilizam joins para todos os segmentos de associação e retornam o atributo final (`JpaPredicateUtils.java:76-79`).
- O join type é `INNER` por padrão, `LEFT` quando o filtro tem `ModJoinTypeLeft` e `RIGHT` quando tem `ModJoinTypeRight` (`JpaPredicateUtils.java:106-113`).
- Joins existentes com mesmo atributo e mesmo `JoinType` são reutilizados (`JpaPredicateUtils.java:123-130`).
- Segmentos vazios, ponto inicial ou ponto final lançam `IllegalStateException` (`JpaPredicateUtils.java:137-176`).

#### Operações de predicado

🟢 CONFIRMADO — Cada implementação `Specification*` resolve o path JPA, converte valores via `DataConversionService` para o tipo real da expressão e cria o predicado Criteria.

Regras por operação:
- `SpecificationEquals` usa `criteriaBuilder.equal`; se o atributo é `String` e há `ModIgnoreCase`, aplica `upper` na expressão e no valor (`SpecificationEquals.java:45-53`).
- `SpecificationLike` envolve o valor com `%valor%`; `StartsWith` usa `valor%`; `EndsWith` usa `%valor` (`SpecificationLike.java:44-56`, `SpecificationStartsWith.java:44-56`, `SpecificationEndsWith.java:44-57`).
- `SpecificationGreater`, `GreaterOrEquals`, `Less` e `LessOrEquals` usam `JpaPredicateUtils.toComparablePredicate`, selecionando overload numérico (`gt`, `ge`, `lt`, `le`) quando o valor é `Number` (`SpecificationGreater.java:45-52`, `JpaPredicateUtils.java:55-63`).
- `SpecificationBetween` converte os dois valores e chama `criteriaBuilder.between` (`SpecificationBetween.java:45-55`).
- `SpecificationIsNull` converte o valor único para `Boolean`; `true` gera `isNull`, `false` gera `isNotNull` (`SpecificationIsNull.java:46-49`).
- `SpecificationIsIn` aceita valor único ou `Object[]`, converte cada item e aplica `expression.in(arr)` (`SpecificationIsIn.java:49-65`).

#### `IN` sobre collections / `@ElementCollection`

🟢 CONFIRMADO — Quando a expressão JPA calculada é uma `Collection`, `SpecificationIsIn` chama `computeAttributeJoinPath`, faz join também do segmento final e marca a query como `distinct(true)` (`SpecificationIsIn.java:53-56`). Testes de integração confirmam filtro sobre `Set<TipoProduto>` sem duplicar entidade com múltiplos valores correspondentes (`TestSpecificationIsInIntegration.java:71-140`).

#### Fetching via decorator

🟢 CONFIRMADO — `@Fetching` declara atributos a buscar com `JoinType` padrão `LEFT` e é repeatable via `@Fetches` (`Fetching.java:49-64`, `Fetches.java:40-48`).

🟢 CONFIRMADO — `FetchingFilterDecorator` envolve uma `Specification` para criar fetch joins antes do predicado base (`FetchingFilterDecorator.java:61-74`). Regras confirmadas:
- Rejeita coleção de fetches vazia no construtor (`FetchingFilterDecorator.java:51-54`).
- Ignora fetch join em count query (`Long` ou `long`) (`FetchingFilterDecorator.java:63-71`).
- Em query normal, aplica `query.distinct(true)` e cria fetches para paths simples ou aninhados (`FetchingFilterDecorator.java:65-82`).
- Deduplica paths iguais preservando ordem (`FetchingFilterDecorator.java:106-119`).
- Reutiliza fetch existente do mesmo atributo e join type por parent (`FetchingFilterDecorator.java:85-103`).
- Rejeita path nulo/branco/com segmento vazio (`FetchingFilterDecorator.java:122-151`).

🟢 CONFIRMADO — Testes de integração confirmam carga eager de associação solicitada, fetch aninhado `addresses.location`, ausência de fetch em count query, deduplicação e falha para path inválido (`TestFetchingFilterDecoratorIntegration.java:106-181`).

#### Resolução MVC

🟢 CONFIRMADO — `SpecificationDynamicFilterArgumentResolver.supportsParameter` aceita `ConditionalStatement` ou interface que estende/implementa `Specification` (`SpecificationDynamicFilterArgumentResolver.java:63-67`).

🟢 CONFIRMADO — `resolveArgument` monta o mapa de parâmetros, gera `StatementWrapper`, cria decorators, e retorna:
- `ConditionalStatement` quando o parâmetro tem esse tipo (`SpecificationDynamicFilterArgumentResolver.java:70-78`).
- `Specification<?>` ou proxy de interface específica quando o parâmetro é interface `Specification` (`SpecificationDynamicFilterArgumentResolver.java:80-117`).

🟢 CONFIRMADO — O mapa de entrada combina query parameters e URI template variables; query parameter escalar vira `String`, múltiplos valores viram `String[]`, e path variables sobrescrevem/adicionam chaves quando presentes (`SpecificationDynamicFilterArgumentResolver.java:85-106`). Testes cobrem mapa vazio, escalares, múltiplos valores, URI variables e origem mista (`TestSpecDynaFilterArgumentResolver.java:82-180`).

#### Decorators via Spring

🟢 CONFIRMADO — `SpringFilterDecoratorFactory` cacheia decorators por `AnnotationStatementInput` e cacheia instâncias por classe de decorator (`SpringFilterDecoratorFactory.java:45-68`, `SpringFilterDecoratorFactory.java:89-108`).

Regras confirmadas:
- Entrada nula retorna `null` (`SpringFilterDecoratorFactory.java:57-60`).
- `@Fetching`/`@Fetches` no input gera `FetchingFilterDecorator` (`SpringFilterDecoratorFactory.java:110-124`).
- Classes em `@FilterDecorators` são resolvidas do `ApplicationContext`; se não houver bean, a classe é registrada por `registerBean` e buscada novamente (`SpringFilterDecoratorFactory.java:93-99`).
- Decorators customizados e fetching são combinados em `CompositeFilterDecorator`, com fetching por último (`SpringFilterDecoratorFactory.java:79-86`).
- Testes confirmam null handling, fetching simples/múltiplo, decorators com construtor sem args ou multiargs, composição e cache por input equivalente (`TestSpringFilterDecoratorFactory.java:63-133`).

#### Auto-configuração e validação antecipada

🟢 CONFIRMADO — `@EnableDynamicFilterServletConfiguration` importa `DynamicFilterServletAutoConfiguration`, `DynamicFilterJpaRepositoryBeanPostProcessor` e `FilterConfigurationAnalyserBeanPostProcessor` (`EnableDynamicFilterServletConfiguration.java:13-17`).

🟢 CONFIRMADO — `DynamicFilterServletAutoConfiguration` fornece `DefaultDataConversionService` e `SpecificationDynamicFilterResolver` apenas quando não houver bean equivalente, e registra um `WebMvcConfigurer` para adicionar o argument resolver (`DynamicFilterServletAutoConfiguration.java:58-75`).

🟢 CONFIRMADO — `SpecificationDynamicFilterWebMvcConfigurer` cria `AnnotationStatementGenerator`, `SpringFilterDecoratorFactory` e `SpecificationDynamicFilterArgumentResolver` em `addArgumentResolvers` (`SpecificationDynamicFilterWebMvcConfigurer.java:55-60`). O resolver de expressão usa `ValueExpressionResolver` primeiro e cai para `StringValueResolver` quando o primeiro retorna `null` (`SpecificationDynamicFilterWebMvcConfigurer.java:62-73`).

🟢 CONFIRMADO — `FilterConfigurationAnalyserBeanPostProcessor` percorre beans `@RestController`, identifica parâmetros dinâmicos, lista filtros e valida antecipadamente se cada path existe na entidade alvo (`FilterConfigurationAnalyserBeanPostProcessor.java:20-52`).

#### Repositories dinâmicos

🟢 CONFIRMADO — `DynamicFilterJpaRepository` estende `JpaRepository` e `JpaSpecificationExecutor` e adiciona métodos que recebem `ConditionalStatement` para `findOne`, `findAll`, paginação, sort, entity graph, `count`, `exists`, `findBy` e conversão para `Specification` (`DynamicFilterJpaRepository.java:49-192`).

🟢 CONFIRMADO — `DynamicFilterJpaRepositoryImpl` delega a execução ao `SimpleJpaRepository` após converter `ConditionalStatement` em `Specification<T>` com `dynamicFilterResolver` (`DynamicFilterJpaRepositoryImpl.java:66-144`).

🟢 CONFIRMADO — `DynamicFilterJpaRepositoryBeanPostProcessor` injeta `DynamicFilterResolver<Specification<?>>` nos beans que implementam `DynamicFilterJpaRepository` depois da inicialização (`DynamicFilterJpaRepositoryBeanPostProcessor.java:42-49`).

#### Tradução de sort por parâmetro de filtro

🟢 CONFIRMADO — `DynamicFilterJpaRepositoryImpl.updateSortFilterPath` traduz propriedades de `Sort` baseadas no primeiro parâmetro de cada `FilterRequestData` para o path real do filtro (`DynamicFilterJpaRepositoryImpl.java:151-205`).

Regras confirmadas:
- Sort não ordenado ou lista de filtros nula/vazia retorna a instância original (`DynamicFilterJpaRepositoryImpl.java:151-167`).
- Apenas o primeiro parâmetro de cada filtro participa do mapa (`DynamicFilterJpaRepositoryImpl.java:170-184`).
- Se parâmetro e path são iguais, não há mapeamento (`DynamicFilterJpaRepositoryImpl.java:182-185`).
- Parâmetros duplicados preservam o primeiro path translatável encontrado (`DynamicFilterJpaRepositoryImpl.java:177-180`).
- Testes confirmam tradução, preservação de direção, retorno da instância original quando nada muda e regra de duplicidade (`TestDynamicFilterJpaRepositorySortTranslation.java:36-88`).

### Estruturas de Dados

🟢 CONFIRMADO — O módulo não define entidades de produção. Define APIs, annotations e estruturas auxiliares:
- `DynamicFilterJpaRepository<T, I>`: API pública para repositories com `ConditionalStatement`.
- `DynamicFilterJpaRepositoryImpl<T, I>`: implementação baseada em `SimpleJpaRepository`.
- `@EnableDynamicFilterServletConfiguration`: annotation de ativação Spring.
- `@Fetching` e `@Fetches`: annotations de fetch eager por Criteria API.
- `FetchingFilterDecorator.FetchPath`, `FetchSegment`, `ResolvedFetchPath`: records internos para deduplicação e execução de fetches.
- `JpaPredicateUtils.ParsedPath`: record interno para cache de path parseado.
- `ModJoinTypeInner`, `ModJoinTypeLeft`, `ModJoinTypeRight`: marcadores de join type para filtros.

### Tratamento de Erros

🟢 CONFIRMADO
- `SpecificationDynamicFilterResolver` rejeita `StatementWrapper` nulo com `NullPointerException` via `Objects.requireNonNull` (`SpecificationDynamicFilterResolver.java:45-47`).
- `SpecificationDynamicFilterArgumentResolver` rejeita dependências nulas no construtor (`SpecificationDynamicFilterArgumentResolver.java:55-60`).
- `createParametersMap` lança `IllegalStateException` quando não há `HttpServletRequest` (`SpecificationDynamicFilterArgumentResolver.java:85-89`).
- `JpaPredicateUtils.computeAttributePath` rejeita path nulo e `parsePath` rejeita path vazio ou segmentos inválidos (`JpaPredicateUtils.java:69-79`, `JpaPredicateUtils.java:137-176`).
- `FetchingFilterDecorator` rejeita fetches vazios e paths inválidos (`FetchingFilterDecorator.java:51-54`, `FetchingFilterDecorator.java:122-151`).

### Pontos de Atenção

🟡 INFERIDO — `DynamicFilterJpaRepositoryImpl` inicializa `dynamicFilterResolver` como `null` e depende do `DynamicFilterJpaRepositoryBeanPostProcessor` para injeção posterior (`DynamicFilterJpaRepositoryImpl.java:57-64`, `DynamicFilterJpaRepositoryBeanPostProcessor.java:42-49`). Uso fora da configuração esperada pode gerar `NullPointerException` ao chamar os métodos dinâmicos.

🟢 CONFIRMADO — `convertoToSpecification` mantém erro de digitação no nome do método público (`DynamicFilterJpaRepository.java:183`, `DynamicFilterJpaRepositoryImpl.java:142`). Por ser API pública, qualquer renomeação seria breaking change.

🟡 INFERIDO — O proxy criado por `SpecificationDynamicFilterArgumentResolver` delega toda chamada com `method.invoke(target, args)` (`SpecificationDynamicFilterArgumentResolver.java:117`). Testes de suporte a default methods e métodos de `Object` estão desabilitados, indicando comportamento não garantido para esses casos (`TestSpecDynaFilterArgumentResolver.java:229-328`).

### Testes Relevantes

🟢 CONFIRMADO
- `TestJpaPredicateUtils` cobre path simples, join path e múltiplos joins (`TestJpaPredicateUtils.java:58-131`).
- `TestSpecification*` cobre as operações individuais JPA por predicado.
- `TestSpecificationIsInIntegration` cobre `IN` sobre `@ElementCollection` com enum e ausência de duplicidade (`TestSpecificationIsInIntegration.java:71-140`).
- `TestSpecificationDynamicFilterResolver` e `TestSpecificationStatementAnalyser` cobrem conversão de statements em `Specification`.
- `TestFetchingFilterDecorator` e integrações cobrem fetch joins, deduplicação, count query e paths inválidos.
- `TestSpecDynaFilterArgumentResolver` cobre criação do mapa de parâmetros e resolução MVC.
- `TestSpringFilterDecoratorFactory` cobre resolução, composição e cache de decorators Spring.
- `TestDynamicFilterServletAutoConfiguration` e `TestSpecificationDynamicFilterWebMvcConfigurer` cobrem configuração MVC.
- `TestDynamicFilterJpaRepositorySortTranslation` cobre tradução de sort por parâmetro de filtro.

## Módulo `modules.openapi`

### Propósito

🟢 CONFIRMADO — O módulo `modules.openapi` integra os filtros dinâmicos com SpringDoc OpenAPI. Ele remove o parâmetro técnico do método controller e cria parâmetros OpenAPI individuais para cada filtro definido por `@Conjunction`, `@ConjunctionFrom`, `@Disjunction` ou `@DisjunctionFrom`.

Evidências principais:
- `src/main/java/com/runestone/dynafilter/modules/openapi/DynaFilterOperationCustomizer.java:52` implementa `OperationCustomizer`.
- `src/main/java/com/runestone/dynafilter/modules/openapi/DynaFilterOperationCustomizer.java:62-84` percorre parâmetros do handler e customiza a operação OpenAPI.
- `src/main/java/com/runestone/dynafilter/modules/openapi/SchemaValidationUtils.java:40` aplica constraints Jakarta Bean Validation ao schema gerado.

### Arquivos Primários

🟢 CONFIRMADO
- `src/main/java/com/runestone/dynafilter/modules/openapi/DynaFilterOperationCustomizer.java`
- `src/main/java/com/runestone/dynafilter/modules/openapi/SchemaValidationUtils.java`

### Inspeção Semântica Java

🟡 INFERIDO — Seguindo a preferência do usuário, a análise deve priorizar LSP Java quando houver endpoint disponível. Nesta sessão, não há ferramenta LSP exposta diretamente; a análise de `modules.openapi` foi feita por inspeção estática dos arquivos Java e dos testes unitários relevantes.

### Fluxo de Controle Principal

#### Customização da operação OpenAPI

🟢 CONFIRMADO — `DynaFilterOperationCustomizer.customize` executa o fluxo abaixo:

1. Percorre `handlerMethod.getMethodParameters()` (`DynaFilterOperationCustomizer.java:62`).
2. Ignora parâmetros sem annotations de filtro dinâmico (`DynaFilterOperationCustomizer.java:63-66`).
3. Descobre o nome do parâmetro técnico (`DynaFilterOperationCustomizer.java:68`).
4. Usa `TypeAnnotationUtils.listAllFilterRequestData` para obter todos os filtros requisitáveis do parâmetro (`DynaFilterOperationCustomizer.java:69-70`).
5. Remove da operação OpenAPI o parâmetro técnico original com o mesmo nome (`DynaFilterOperationCustomizer.java:72-73`).
6. Para cada `FilterRequestData`, chama `customizeParameter` (`DynaFilterOperationCustomizer.java:74-76`).
7. Encapsula falhas em `IllegalStateException` com o método handler no texto (`DynaFilterOperationCustomizer.java:77-80`).

🟡 INFERIDO — A condição de filtro verifica `Disjunction.class` duas vezes e não verifica `DisjunctionFrom.class` (`DynaFilterOperationCustomizer.java:63-64`). Isso parece um bug de cobertura para parâmetros anotados apenas com `@DisjunctionFrom`, mas não foi confirmado por teste dedicado neste módulo.

#### Customização de parâmetro por filtro

🟢 CONFIRMADO — `customizeParameter` transforma cada filtro em um ou mais parâmetros OpenAPI de query (`DynaFilterOperationCustomizer.java:91-137`).

Regras confirmadas:
- Filtros com `constantValues` são omitidos da documentação, pois não devem ser informados pelo usuário (`DynaFilterOperationCustomizer.java:92-94`).
- Filtro `Dynamic` com mais de um parâmetro lança `IllegalStateException` (`DynaFilterOperationCustomizer.java:96-98`).
- Cada nome em `filter.parameters()` gera/atualiza um parâmetro OpenAPI (`DynaFilterOperationCustomizer.java:100-106`).
- Operação `Dynamic` gera `ArraySchema` com `type=array`, `minItems=2` e items `StringSchema` (`DynaFilterOperationCustomizer.java:108-113`).
- Operação `IsIn` gera `ArraySchema`, preservando schema existente como item quando houver (`DynaFilterOperationCustomizer.java:114-118`).
- Demais operações usam a classe alvo do filtro, localizam o field por path e criam schema comum (`DynaFilterOperationCustomizer.java:119-123`).
- `required` do parâmetro vem de `filter.required()` (`DynaFilterOperationCustomizer.java:125`).
- Parâmetro existente sem `in` ou com `DEFAULT` vira `query` (`DynaFilterOperationCustomizer.java:126-129`).
- Parâmetro existente `path` permanece `path` e força `required=true` (`DynaFilterOperationCustomizer.java:129-131`).
- Parâmetro novo entra como `query` e é adicionado em `operation.getParameters()` (`DynaFilterOperationCustomizer.java:132-134`).

#### Schema comum

🟢 CONFIRMADO — `createCommonSchema` resolve o schema do tipo do field via `AnnotationsUtils.resolveSchemaFromType` e considera `@JsonView` do método handler (`DynaFilterOperationCustomizer.java:139-167`, `DynaFilterOperationCustomizer.java:173-180`).

Regras confirmadas:
- Quando o field não é encontrado, usa `StringSchema` como fallback (`DynaFilterOperationCustomizer.java:141-147`).
- Operação `IsNull` sempre usa `BooleanSchema` (`DynaFilterOperationCustomizer.java:151-152`).
- Se já existe schema no parâmetro OpenAPI, cria um novo `Schema` copiando `type` e `enum` do schema resolvido, preservando a decisão de atualizar o parâmetro existente (`DynaFilterOperationCustomizer.java:149-158`).
- A descrição vem de `filter.description()` (`DynaFilterOperationCustomizer.java:161-162`).
- Default é aplicado apenas quando `filter.defaultValues()` tem exatamente um valor (`DynaFilterOperationCustomizer.java:164-166`).
- Constraints Bean Validation são aplicadas pelo `SchemaValidationUtils` (`DynaFilterOperationCustomizer.java:167`).

#### Nome do parâmetro técnico

🟢 CONFIRMADO — `getParameterName` prioriza `@io.swagger.v3.oas.annotations.Parameter.name`, depois usa `ParameterNameDiscoverer` se existir, e por fim `Parameter.getName()` (`DynaFilterOperationCustomizer.java:185-193`).

### Aplicação de Bean Validation em Schemas

🟢 CONFIRMADO — `SchemaValidationUtils.applyValidations` só atua quando há `AnnotatedElement`; se o elemento é nulo, retorna sem alteração (`SchemaValidationUtils.java:40-43`).

Regras confirmadas por tipo de schema:
- Para `integer` ou `number`, aplica `@PositiveOrZero`, `@Min`, `@Max`, `@DecimalMin` e `@DecimalMax` (`SchemaValidationUtils.java:44-71`).
- `@PositiveOrZero` define `minimum=0` e `maximum=Long.MAX_VALUE` (`SchemaValidationUtils.java:45-48`).
- `@DecimalMin` e `@DecimalMax` respeitam `inclusive()` invertendo para `exclusiveMinimum`/`exclusiveMaximum` (`SchemaValidationUtils.java:60-70`).
- Para `string`, aplica `@Size` como `minLength`/`maxLength` e `@Pattern` como `pattern` (`SchemaValidationUtils.java:73-84`).
- Para `array`, aplica `@Size` como `minItems`/`maxItems` (`SchemaValidationUtils.java:86-92`).

🟢 CONFIRMADO — `AnnotationUtils.findAnnotation` permite detectar constraints presentes em annotations compostas, como `@ParticipantName` contendo `@Size` e `@Pattern` (`SchemaValidationUtils.java:50-87`, `ParticipantName.java:38-43`).

### Estruturas de Dados

🟢 CONFIRMADO — O módulo não define entidades nem records de produção. Ele define duas classes utilitárias/integração:
- `DynaFilterOperationCustomizer`: customizer SpringDoc com dependência opcional de `ParameterNameDiscoverer`.
- `SchemaValidationUtils`: utilitário stateless para copiar constraints Jakarta Validation para `Schema<?>` OpenAPI.

### Tratamento de Erros

🟢 CONFIRMADO
- `customize` encapsula qualquer falha de `customizeParameter` em `IllegalStateException` contendo a assinatura do método handler (`DynaFilterOperationCustomizer.java:77-80`).
- `Dynamic` com mais de um parâmetro é rejeitado explicitamente (`DynaFilterOperationCustomizer.java:96-98`).
- `getJsonViewFromMethod` usa `requireNonNull(methodParameter.getMethod())`; método nulo causaria `NullPointerException` (`DynaFilterOperationCustomizer.java:173-174`).
- `SchemaValidationUtils.applyValidations` tolera `annotatedElement` nulo (`SchemaValidationUtils.java:40-43`).

### Pontos de Atenção

🟡 INFERIDO — A duplicidade da condição `Disjunction.class` no filtro inicial pode deixar `@DisjunctionFrom` sem customização quando ele for a única annotation dinâmica no parâmetro (`DynaFilterOperationCustomizer.java:63-64`). Recomenda-se teste específico antes de alterar comportamento.

🟡 INFERIDO — Não há testes diretos para `DynaFilterOperationCustomizer` no diretório `modules.openapi`; a cobertura confirmada deste módulo concentra-se em `SchemaValidationUtils`.

🟢 CONFIRMADO — `Decorated` é importado em `DynaFilterOperationCustomizer`, mas não é usado no arquivo (`DynaFilterOperationCustomizer.java:30`).

### Testes Relevantes

🟢 CONFIRMADO
- `TestSchemaValidationUtils.testIntegerValidations` cobre `@Min` e `@Max` em schema `integer` (`TestSchemaValidationUtils.java:37-49`).
- `TestSchemaValidationUtils.testIntegerPositiveOrZero` cobre `@PositiveOrZero` (`TestSchemaValidationUtils.java:51-61`).
- `TestSchemaValidationUtils.testCustomAnnotation` cobre constraints via annotation composta `@ParticipantName` (`TestSchemaValidationUtils.java:63-81`).
- `TestSchemaValidationUtils.testDecimalMinMax` cobre `@DecimalMin` e `@DecimalMax` em schema `number` (`TestSchemaValidationUtils.java:83-101`).
- `TestSchemaValidationUtils.testListParameter` cobre `@Size` em schema `array` (`TestSchemaValidationUtils.java:103-120`).

## Módulo `test-support.performance`

### Propósito

🟢 CONFIRMADO — O módulo `test-support.performance` contém benchmarks JMH e fixtures de medição para validar custo de geração de statements, resolução de `Specification`, argument resolver MVC, cache de metadata de anotações, criação de predicados Criteria, fetch decorators, tradução de sort e proxy de interfaces `Specification`.

Evidências principais:
- `src/test/java/com/runestone/dynafilter/performance/DynamicFilterResolverBenchmark.java:29-35` define benchmark JMH geral em microssegundos.
- `src/test/java/com/runestone/dynafilter/performance/DynamicFilterResolverPerf02Benchmark.java:46-52` define cenários JPA/Criteria/cache/fetch mais pesados.
- `src/test/java/com/runestone/dynafilter/modules/jpa/repository/DynamicFilterRepositorySortPerfBenchmark.java:23-29` compara tradução de sort otimizada e legado.
- `src/test/java/com/runestone/dynafilter/performance/DynamicFilterResolverBenchmarkRunner.java:14-20` roda JMH e grava JSON em `dynamic-filter-resolver/target/jmh-result.json`.

### Arquivos Primários

🟢 CONFIRMADO
- `src/test/java/com/runestone/dynafilter/performance/DynamicFilterResolverBenchmarkRunner.java`
- `src/test/java/com/runestone/dynafilter/performance/DynamicFilterResolverBenchmark.java`
- `src/test/java/com/runestone/dynafilter/performance/DynamicFilterResolverPerf02Benchmark.java`
- `src/test/java/com/runestone/dynafilter/performance/DynamicFilterResolverPerf06ProxyBenchmark.java`
- `src/test/java/com/runestone/dynafilter/modules/jpa/repository/DynamicFilterRepositorySortPerfBenchmark.java`

### Inspeção Semântica Java

🟡 INFERIDO — A preferência do usuário é priorizar LSP Java para arquivos Java. Nesta sessão, não há ferramenta LSP exposta; a análise foi feita por inspeção estática dos benchmarks JMH.

### Fluxo de Controle Principal

#### Runner JMH

🟢 CONFIRMADO — `DynamicFilterResolverBenchmarkRunner.main` monta `Options` com `.include(DynamicFilterResolverBenchmark.class.getSimpleName())`, formato JSON e saída `dynamic-filter-resolver/target/jmh-result.json`, depois executa `new Runner(options).run()` (`DynamicFilterResolverBenchmarkRunner.java:14-20`).

#### Benchmark geral do resolver

🟢 CONFIRMADO — `DynamicFilterResolverBenchmark` mede cenários básicos de alto nível:
- `statementGenerator_searchPeopleAndGames`: gera `StatementWrapper` a partir de `AnnotationStatementGenerator` e parâmetros simulados (`DynamicFilterResolverBenchmark.java:37-41`).
- `specificationResolver_createFilter`: converte statement pré-computado em `Specification` (`DynamicFilterResolverBenchmark.java:43-46`).
- `argumentResolver_interfaceProxy`: resolve argumento MVC para interface `SearchState` (`DynamicFilterResolverBenchmark.java:48-51`).
- `argumentResolver_fetchingDecorator`: resolve argumento MVC com annotations de fetching/decorator (`DynamicFilterResolverBenchmark.java:53-56`).
- `annotationUtils_reusedInput`: mede hit/cache usando `AnnotationStatementInput` compartilhado (`DynamicFilterResolverBenchmark.java:58-61`).
- `annotationUtils_newInputInstance`: mede entrada equivalente nova para o cache (`DynamicFilterResolverBenchmark.java:63-67`).

🟢 CONFIRMADO — O estado `StatementState` prepara gerador, resolver JPA, input `SearchPeopleAndGames`, parâmetros representativos e statement pré-computado no setup de trial (`DynamicFilterResolverBenchmark.java:69-93`).

🟢 CONFIRMADO — O estado `ArgumentResolverState` usa Mockito para simular `MethodParameter`, `NativeWebRequest`, `HttpServletRequest`, query parameters e URI template variables (`DynamicFilterResolverBenchmark.java:106-155`).

#### Benchmarks de Criteria, fetch e cache

🟢 CONFIRMADO — `DynamicFilterResolverPerf02Benchmark` mede caminhos mais pesados:
- `perf02_specification_toPredicate_manyFilters`: executa `toPredicate` em uma conjunction de 50 filtros com paths variados (`DynamicFilterResolverPerf02Benchmark.java:54-62`, `DynamicFilterResolverPerf02Benchmark.java:171-195`).
- `perf02_specification_toPredicate_repeatedNestedPath`: executa `toPredicate` em 50 filtros alternando paths aninhados repetidos (`DynamicFilterResolverPerf02Benchmark.java:64-72`, `DynamicFilterResolverPerf02Benchmark.java:197-214`).
- `perf02_fetchingDecorator_deepPaths`: mede fetch decorator com paths profundos (`DynamicFilterResolverPerf02Benchmark.java:74-83`, `DynamicFilterResolverPerf02Benchmark.java:280-286`).
- `perf02_fetchingDecorator_overlappingPaths`: mede deduplicação/reuso em paths sobrepostos (`DynamicFilterResolverPerf02Benchmark.java:85-94`, `DynamicFilterResolverPerf02Benchmark.java:288-295`).
- `perf02_annotationUtils_reusedInput_afterCacheGrowth` e `newEquivalentInput_afterCacheGrowth`: medem cache depois de preenchimento com 20.000 annotations sintéticas (`DynamicFilterResolverPerf02Benchmark.java:96-106`, `DynamicFilterResolverPerf02Benchmark.java:230-252`).
- `perf04_annotationUtils_hitLatency_lruBoundedCache`: mede hit depois de preencher o cache acima do limite com 50.000 entradas sintéticas (`DynamicFilterResolverPerf02Benchmark.java:108-110`, `DynamicFilterResolverPerf02Benchmark.java:255-277`).

🟢 CONFIRMADO — `JpaPredicateState` cria `SpringApplicationBuilder` sem web, H2 em memória, `ddl-auto=create-drop`, lazy initialization e logging root `ERROR` para obter `EntityManager` real nos benchmarks (`DynamicFilterResolverPerf02Benchmark.java:124-143`).

#### Benchmark de proxy PERF-006

🟢 CONFIRMADO — `DynamicFilterResolverPerf06ProxyBenchmark` compara `optimizedProxy` criado por reflection chamando o método privado `SpecificationDynamicFilterArgumentResolver.createProxy` com `legacyProxy` criado por `Proxy.newProxyInstance` e invocation handler reflexivo (`DynamicFilterResolverPerf06ProxyBenchmark.java:43-80`).

🟡 INFERIDO — O nome `optimizedProxy` no benchmark parece refletir uma hipótese de otimização PERF-006, mas o código atual de produção ainda usa proxy reflexivo simples. Testes desabilitados em `TestSpecDynaFilterArgumentResolver` indicam que parte dessa otimização foi revertida.

#### Benchmark de sort translation

🟢 CONFIRMADO — `DynamicFilterRepositorySortPerfBenchmark` compara `DynamicFilterJpaRepositoryImpl.updateSortFilterPath` com uma implementação legada O(n*m) que percorre todos os filtros para cada order (`DynamicFilterRepositorySortPerfBenchmark.java:31-49`, `DynamicFilterRepositorySortPerfBenchmark.java:99-113`).

🟢 CONFIRMADO — `HeavyMappingsState` prepara 50 orders e 600 filtros, incluindo 50 pares com mesmo parâmetro e path translatável `entity.pN` (`DynamicFilterRepositorySortPerfBenchmark.java:51-75`). `NoTranslationState` prepara 50 orders e 600 filtros sem tradução necessária (`DynamicFilterRepositorySortPerfBenchmark.java:77-97`).

### Estruturas de Dados

🟢 CONFIRMADO — O módulo é composto por classes de benchmark e estados JMH:
- `DynamicFilterResolverBenchmarkRunner`: ponto de entrada manual para JMH.
- `DynamicFilterResolverBenchmark.StatementState`: gerador, resolver, input, parâmetros e statement pré-computado.
- `DynamicFilterResolverBenchmark.AnnotationCacheState`: input compartilhado para cache.
- `DynamicFilterResolverBenchmark.ArgumentResolverState`: mocks MVC para medir argument resolver.
- `DynamicFilterResolverPerf02Benchmark.JpaPredicateState`: contexto Spring/H2, `EntityManager` e specifications pré-montadas.
- `DynamicFilterResolverPerf02Benchmark.CacheGrowthState`: cache de annotations após crescimento com 20.000 entradas.
- `DynamicFilterResolverPerf02Benchmark.BoundedCacheHitState`: cache preenchido acima do limite com 50.000 entradas.
- `DynamicFilterResolverPerf02Benchmark.SyntheticAnnotation`: annotation sintética com `equals`, `hashCode` e `toString` para pressão de cache.
- `DynamicFilterRepositorySortPerfBenchmark.HeavyMappingsState` e `NoTranslationState`: datasets sintéticos de sort/filtros.
- `DynamicFilterResolverPerf06ProxyBenchmark.ProxyInvocationState`: proxies e mocks Criteria para comparar invocação.

### Tratamento de Recursos

🟢 CONFIRMADO — `DynamicFilterResolverPerf02Benchmark.JpaPredicateState.tearDown` fecha `EntityManager` quando aberto e fecha `ConfigurableApplicationContext` no fim do trial (`DynamicFilterResolverPerf02Benchmark.java:161-169`). Estados de cache chamam `TypeAnnotationUtils.clearCaches()` no teardown (`DynamicFilterResolverPerf02Benchmark.java:249-252`, `DynamicFilterResolverPerf02Benchmark.java:274-277`).

### Pontos de Atenção

🟡 INFERIDO — `DynamicFilterResolverBenchmarkRunner` inclui apenas `DynamicFilterResolverBenchmark`, não os benchmarks `Perf02`, `Perf06` ou `DynamicFilterRepositorySortPerfBenchmark` (`DynamicFilterResolverBenchmarkRunner.java:15-18`). Esses outros benchmarks precisam ser executados por include específico ou por runner externo.

🟢 CONFIRMADO — `DynamicFilterResolverPerf06ProxyBenchmark` acessa método privado por reflection (`DynamicFilterResolverPerf06ProxyBenchmark.java:66-74`), portanto é sensível a mudança de assinatura do método privado.

### Testes / Benchmarks Relevantes

🟢 CONFIRMADO
- `DynamicFilterResolverBenchmark` cobre baseline de geração, resolução, argument resolver e cache de annotation metadata.
- `DynamicFilterResolverPerf02Benchmark` cobre hotspots JPA Criteria, fetching e cache sob crescimento/eviction.
- `DynamicFilterResolverPerf06ProxyBenchmark` cobre custo de invocação de proxy de interface `Specification`.
- `DynamicFilterRepositorySortPerfBenchmark` cobre custo de tradução de sort otimizada versus legado.
