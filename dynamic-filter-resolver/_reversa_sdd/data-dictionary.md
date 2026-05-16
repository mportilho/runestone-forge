# Dicionário de Dados — dynamic-filter-resolver

> Gerado pelo Reversa Archaeologist em 2026-05-16.
> Escala de confiança: 🟢 CONFIRMADO, 🟡 INFERIDO, 🔴 LACUNA.

## Módulo `core`

### `FilterData`

🟢 CONFIRMADO — `src/main/java/com/runestone/dynafilter/core/model/FilterData.java:49`

| Campo | Tipo | Obrigatório | Regra / Observação |
|---|---|---:|---|
| `path` | `String` | 🟡 | Caminho do atributo filtrado. Não há validação direta de nulidade no construtor. |
| `parameters` | `String[]` | Sim | Não pode ser nulo/vazio; tamanho deve igualar `values`. |
| `targetType` | `Class<?>` | 🟡 | Tipo alvo para conversão; sem validação direta no record. |
| `operation` | `Class<? super DefinedFilterOperation>` | 🟡 | Operação usada pelo serviço de filtros; validação de registro ocorre em `AbstractFilterOperationService`. |
| `negate` | `boolean` | Sim | Indica se o statement lógico será negado. |
| `values` | `Object[]` | Sim | Não pode ser nulo/vazio; tamanho deve igualar `parameters`. |
| `modifiers` | `List<Class<? extends FilterModifier>>` | Não | `hasModifier` tolera `null`. |
| `description` | `String` | Não | Texto para documentação humana. |

### `FilterRequestData`

🟢 CONFIRMADO — `src/main/java/com/runestone/dynafilter/core/model/FilterRequestData.java:23`

| Campo | Tipo | Obrigatório | Regra / Observação |
|---|---|---:|---|
| `path` | `String` | Sim | Copiado de `@Filter.path`. |
| `parameters` | `String[]` | Sim | Copiado de `@Filter.parameters`. |
| `targetType` | `Class<?>` | Sim | Copiado de `@Filter.targetType`; default `Object.class`. |
| `operation` | `Class<? super DefinedFilterOperation>` | Sim | Copiado de `@Filter.operation`. |
| `negate` | `String` | Não | Pode ser `true`, `false` ou expressão resolvível. |
| `defaultValues` | `Object[]` | Não | Defaults usados quando o chamador não envia parâmetro. |
| `constantValues` | `Object[]` | Não | Constantes com prioridade sobre parâmetros/defaults. |
| `format` | `String` | Não | Padrão de conversão para o tipo alvo. |
| `required` | `boolean` | Sim | Quando verdadeiro, ausência de valor gera erro. |
| `modifiers` | `List<Class<? extends FilterModifier>>` | Não | Classes marcadoras de modificação. |
| `description` | `String` | Não | Texto para documentação humana. |

### `StatementWrapper`

🟢 CONFIRMADO — `src/main/java/com/runestone/dynafilter/core/generator/StatementWrapper.java:42`

| Campo | Tipo | Obrigatório | Regra / Observação |
|---|---|---:|---|
| `statement` | `AbstractStatement` | Sim | Construtor rejeita `null`. |
| `decoratedFilters` | `Map<String, FilterData>` | Não | Normalizado para `Map.of()` quando nulo. Chave é `FilterData.path`. |
| `allFilters` | `List<FilterRequestData>` | Não | Normalizado para `List.of()` quando nulo. |

### `ConditionalStatement`

🟢 CONFIRMADO — `src/main/java/com/runestone/dynafilter/core/generator/ConditionalStatement.java:5`

| Campo | Tipo | Obrigatório | Regra / Observação |
|---|---|---:|---|
| `statementWrapper` | `StatementWrapper` | Sim | Construtor rejeita `null`. |
| `filterDecorator` | `FilterDecorator<?>` | Não | Pode ser nulo; consumidores devem tratar. |

### `AnnotationStatementInput`

🟢 CONFIRMADO — `src/main/java/com/runestone/dynafilter/core/generator/annotation/AnnotationStatementInput.java:31`

| Campo | Tipo | Obrigatório | Regra / Observação |
|---|---|---:|---|
| `type` | `Class<?>` | Não | Tipo base usado para encontrar anotações e interfaces. |
| `annotations` | `Annotation[]` | Não | Clonado defensivamente no construtor. |
| `cachedHashCode` | `int` | Sim | Calculado no construtor com `type` e `annotations`. |

### `FilterAnnotationData`

🟢 CONFIRMADO — `src/main/java/com/runestone/dynafilter/core/generator/annotation/FilterAnnotationData.java`

| Campo | Tipo | Obrigatório | Regra / Observação |
|---|---|---:|---|
| `logicOperator` | `LogicOperator` | Sim | `CONJUNCTION` ou `DISJUNCTION`. |
| `filters` | `List<Filter>` | Não | Filtros diretos do bloco. |
| `filterStatements` | `List<FilterAnnotationStatement>` | Não | Sub-statements aninhados. |
| `negate` | `String` | Não | Negação do bloco inteiro. |

### `FilterAnnotationStatement`

🟢 CONFIRMADO — `src/main/java/com/runestone/dynafilter/core/generator/annotation/FilterAnnotationStatement.java`

| Campo | Tipo | Obrigatório | Regra / Observação |
|---|---|---:|---|
| `filters` | `List<Filter>` | Não | Filtros dentro do sub-statement. |
| `negate` | `String` | Não | Negação do sub-statement. |

### Árvore de Statements

🟢 CONFIRMADO

| Tipo | Campos | Função |
|---|---|---|
| `LogicalStatement` | `FilterData filterData` | Folha lógica que representa um filtro aplicável. |
| `CompoundStatement` | `leftStatement`, `rightStatement`, `logicOperator` | Nó binário que combina statements por `CONJUNCTION` ou `DISJUNCTION`. |
| `NegatedStatement` | `AbstractStatement statement` | Nó de negação lógica. |
| `NoOpStatement` | nenhum | Sentinela para ausência de filtros aplicáveis. |

### Anotações de Configuração

🟢 CONFIRMADO

| Anotação | Target | Campos principais | Função |
|---|---|---|---|
| `@Filter` | `FIELD` | `path`, `parameters`, `targetType`, `operation`, `negate`, `defaultValues`, `constantValues`, `format`, `required`, `modifiers`, `description` | Define uma cláusula filtrável. |
| `@Conjunction` | `PARAMETER`, `TYPE` | `value`, `disjunctions`, `negate` | Agrupa filtros por AND. |
| `@Disjunction` | `PARAMETER`, `TYPE` | `value`, `conjunctions`, `negate` | Agrupa filtros por OR. |
| `@Statement` | annotation element | `value`, `negate` | Define sub-statement aninhado. |
| `@ConjunctionFrom` | `PARAMETER`, `TYPE` | `value`, `disjunctions`, `negate` | Usa filtros declarados em classe externa e combina por AND. |
| `@DisjunctionFrom` | `PARAMETER`, `TYPE` | `value`, `conjunctions`, `negate` | Usa filtros declarados em classe externa e combina por OR. |
| `@StatementFrom` | annotation element | `value`, `negate` | Sub-statement externo. |
| `@FilterTarget` | 🟢 classe/anotação conforme código | `value` | Declara entidade alvo para classe externa/conditional statement. |
| `@FilterDecorators` | 🟢 classe/anotação conforme código | `value` | Declara decorators de filtro. |

### Operações

🟢 CONFIRMADO

| Código dinâmico | Operação | Interface marcador |
|---|---|---|
| `EQ` | igualdade | `Equals<T>` |
| `LT` | menor que | `Less<T>` |
| `LE` | menor ou igual | `LessOrEquals<T>` |
| `GT` | maior que | `Greater<T>` |
| `GE` | maior ou igual | `GreaterOrEquals<T>` |
| `LK` | contém/like | `Like<T>` |
| `SW` | começa com | `StartsWith<T>` |
| `EW` | termina com | `EndsWith<T>` |
| `IN` | pertence a lista | `IsIn<T>` |
| `BT` | intervalo | `Between<T>` |

Prefixar com `N`/`n` nega a operação dinâmica, por exemplo `NGE`.

## Módulo `modules.jpa`

### APIs de Repository

🟢 CONFIRMADO

| Tipo | Campos / Métodos Principais | Função |
|---|---|---|
| `DynamicFilterJpaRepository<T, I>` | `findOne`, `findAll`, `count`, `exists`, `findBy`, `convertoToSpecification`, `setDynamicFilterResolver` | Interface pública para executar `ConditionalStatement` via Spring Data JPA. |
| `DynamicFilterJpaRepositoryImpl<T, I>` | `EntityManager em`, `DynamicFilterResolver<Specification<T>> dynamicFilterResolver` | Implementação baseada em `SimpleJpaRepository`; converte statements em `Specification<T>`. |
| `DynamicFilterJpaRepositoryBeanPostProcessor` | `dynamicFilterResolver` | Injeta o resolver em repositories dinâmicos após inicialização do bean. |

### Resolução JPA

🟢 CONFIRMADO

| Tipo | Campos | Função |
|---|---|---|
| `SpecificationFilterOperationService` | mapa interno de operações | Registra operações do `core` para implementações `Specification*`. |
| `SpecificationDynamicFilterResolver` | `SpecificationStatementAnalyser statementAnalyser` | Cria `Specification<?>` a partir de `StatementWrapper` e aplica decorator opcional. |
| `SpecificationStatementAnalyser` | `FilterOperationService<Specification<?>> filterOperationService` | Visitor/analyser que traduz nós de statement para `Specification`. |

### Operações `Specification*`

🟢 CONFIRMADO

| Classe | Operação | Regras de valor |
|---|---|---|
| `SpecificationEquals` | Igualdade | Converte valor único para o tipo do path; suporta `ModIgnoreCase` em `String`. |
| `SpecificationLike` | Contém | Converte para `String`; aplica `%valor%`; suporta `ModIgnoreCase`. |
| `SpecificationStartsWith` | Prefixo | Aplica `valor%`; suporta `ModIgnoreCase`. |
| `SpecificationEndsWith` | Sufixo | Aplica `%valor`; suporta `ModIgnoreCase`. |
| `SpecificationGreater` | Maior que | Converte para tipo do path; usa overload numérico quando valor é `Number`. |
| `SpecificationGreaterOrEquals` | Maior ou igual | Converte para tipo do path; usa overload numérico quando valor é `Number`. |
| `SpecificationLess` | Menor que | Converte para tipo do path; usa overload numérico quando valor é `Number`. |
| `SpecificationLessOrEquals` | Menor ou igual | Converte para tipo do path; usa overload numérico quando valor é `Number`. |
| `SpecificationBetween` | Intervalo | Usa dois valores convertidos para o tipo do path. |
| `SpecificationIsIn` | Pertence a lista | Aceita valor único ou array; em collection final faz join no segmento final e `distinct(true)`. |
| `SpecificationIsNull` | Nulidade | Valor booleano `true` gera `isNull`; `false` gera `isNotNull`. |

### Paths, Joins e Fetches

🟢 CONFIRMADO

| Tipo | Campo | Obrigatório | Regra / Observação |
|---|---|---:|---|
| `JpaPredicateUtils.ParsedPath` | `associationSegments` | Sim | Segmentos intermediários usados para joins. |
| `JpaPredicateUtils.ParsedPath` | `attributeSegment` | Sim | Segmento final usado como atributo ou join final em collection. |
| `FetchingFilterDecorator.FetchPath` | `value` | Sim | Path textual original do fetch. |
| `FetchingFilterDecorator.FetchPath` | `joinType` | Sim | Join type usado para deduplicação. |
| `FetchingFilterDecorator.FetchSegment` | `segment` | Sim | Segmento individual de fetch. |
| `FetchingFilterDecorator.FetchSegment` | `joinType` | Sim | Join type associado ao segmento. |
| `FetchingFilterDecorator.ResolvedFetchPath` | `segments` | Sim | Path dividido e validado. |
| `FetchingFilterDecorator.ResolvedFetchPath` | `joinType` | Sim | Join type aplicado a todos os segmentos do path. |

### Anotações e Marcadores

🟢 CONFIRMADO

| Tipo | Target / Super tipo | Campos | Função |
|---|---|---|---|
| `@EnableDynamicFilterServletConfiguration` | `TYPE` | nenhum | Importa configuração servlet, BPP de repositories e BPP de análise de filtros. |
| `@Fetching` | `TYPE`, `ANNOTATION_TYPE` | `value`, `joinType` | Declara atributos a buscar eagerly via Criteria fetch join. |
| `@Fetches` | `TYPE`, `ANNOTATION_TYPE` | `value` | Container de `@Fetching` repeatable. |
| `ModJoinTypeInner` | `FilterModifier` | nenhum | Marcador de join interno explícito; o default também é `INNER`. |
| `ModJoinTypeLeft` | `FilterModifier` | nenhum | Marcador para `JoinType.LEFT`. |
| `ModJoinTypeRight` | `FilterModifier` | nenhum | Marcador para `JoinType.RIGHT`. |

### MVC e Spring

🟢 CONFIRMADO

| Tipo | Campo | Obrigatório | Regra / Observação |
|---|---|---:|---|
| `DynamicFilterServletAutoConfiguration` | `applicationContext` | Sim após callback | Usado pelo web configurer e factory de decorators. |
| `DynamicFilterServletAutoConfiguration` | `stringValueResolver` | Não | Fallback para resolver expressões de string. |
| `SpecificationDynamicFilterWebMvcConfigurer` | `applicationContext` | Sim | Deve ser `GenericApplicationContext` para `SpringFilterDecoratorFactory`. |
| `SpecificationDynamicFilterWebMvcConfigurer` | `stringValueResolver` | Não | Fallback de expressão. |
| `SpecificationDynamicFilterWebMvcConfigurer` | `valueExpressionResolver` | Não | Prioritário sobre `StringValueResolver`. |
| `SpecificationDynamicFilterWebMvcConfigurer` | `dynamicFilterResolver` | Sim | Cria filtros JPA a partir dos statements. |
| `SpecificationDynamicFilterArgumentResolver` | `statementGenerator` | Sim | Gera `StatementWrapper` com parâmetros HTTP. |
| `SpecificationDynamicFilterArgumentResolver` | `dynamicFilterResolver` | Sim | Converte wrapper em `Specification`. |
| `SpecificationDynamicFilterArgumentResolver` | `filterDecoratorFactory` | Sim | Resolve decorators por anotação/tipo. |
| `SpringFilterDecoratorFactory` | `applicationContext` | Sim | Busca/registra beans de decorator. |
| `SpringFilterDecoratorFactory` | `decoratorCache` | Sim | Cache por `AnnotationStatementInput`, incluindo ausência via `Optional.empty()`. |
| `SpringFilterDecoratorFactory` | `decoratorsByClass` | Sim | Cache de beans por classe de decorator. |

## Módulo `modules.openapi`

### Classes de Produção

🟢 CONFIRMADO

| Tipo | Campo | Obrigatório | Regra / Observação |
|---|---|---:|---|
| `DynaFilterOperationCustomizer` | `parameterNameDiscoverer` | Não | Usado para descobrir nome do parâmetro técnico quando não há `@Parameter(name=...)`. |
| `SchemaValidationUtils` | nenhum | N/A | Classe utilitária stateless com métodos estáticos. |

### Parâmetros OpenAPI Gerados

🟢 CONFIRMADO

| Operação de filtro | Schema OpenAPI | Regras |
|---|---|---|
| `Dynamic` | `ArraySchema` de `StringSchema` | `type=array`, `minItems=2`; rejeita filtros dinâmicos com mais de um parâmetro. |
| `IsIn` | `ArraySchema` | Items usam schema existente do parâmetro se houver; caso contrário `StringSchema`. |
| `IsNull` | `BooleanSchema` | Ignora o tipo real do field e documenta booleano. |
| Outras operações | Schema resolvido do tipo do field | Usa `AnnotationsUtils.resolveSchemaFromType`; fallback para `StringSchema` se field for nulo. |
| Filtro com `constantValues` | Nenhum parâmetro | Filtro é omitido da documentação porque o valor é constante. |

### Campos de `FilterRequestData` Usados pelo OpenAPI

🟢 CONFIRMADO

| Campo | Uso |
|---|---|
| `parameters` | Define os nomes dos parâmetros OpenAPI criados/atualizados. |
| `operation` | Escolhe schema especial para `Dynamic`, `IsIn` e `IsNull`. |
| `path` | Localiza field na entidade alvo para inferir schema e validações. |
| `required` | Define `parameter.required(...)`; path parameter existente força `true`. |
| `defaultValues` | Define `schema.default` quando há exatamente um valor. |
| `constantValues` | Quando presente, impede criação de parâmetro OpenAPI. |
| `description` | Copiado para `parameter.description`. |

### Bean Validation para Schema

🟢 CONFIRMADO

| Constraint | Tipos de schema afetados | Campos OpenAPI alterados |
|---|---|---|
| `@PositiveOrZero` | `integer`, `number` | `minimum=0`, `maximum=Long.MAX_VALUE`. |
| `@Min` | `integer`, `number` | `minimum`. |
| `@Max` | `integer`, `number` | `maximum`. |
| `@DecimalMin` | `integer`, `number` | `minimum`, `exclusiveMinimum=!inclusive`. |
| `@DecimalMax` | `integer`, `number` | `maximum`, `exclusiveMaximum=!inclusive`. |
| `@Size` | `string` | `minLength`, `maxLength`. |
| `@Size` | `array` | `minItems`, `maxItems`. |
| `@Pattern` | `string` | `pattern`. |

### Test Fixtures

🟢 CONFIRMADO

| Tipo | Campos / Anotações | Função |
|---|---|---|
| `ObjectValidations` | `negativeValues`, `positiveOrZeroNumbers`, `limitedBigDecimal`, `participantName`, `schedule` | Record usado para validar cópia de Bean Validation para schemas. |
| `@ParticipantName` | `@NotBlank`, `@Size(max=150)`, `@Pattern` | Annotation composta usada para confirmar descoberta de constraints meta-anotadas. |

## Módulo `test-support.performance`

### Benchmarks JMH

🟢 CONFIRMADO

| Classe | Benchmark / Estado | Função |
|---|---|---|
| `DynamicFilterResolverBenchmarkRunner` | `main` | Executa JMH para `DynamicFilterResolverBenchmark` e grava JSON em `dynamic-filter-resolver/target/jmh-result.json`. |
| `DynamicFilterResolverBenchmark` | `statementGenerator_searchPeopleAndGames` | Mede geração de `StatementWrapper`. |
| `DynamicFilterResolverBenchmark` | `specificationResolver_createFilter` | Mede conversão de statement pré-computado para `Specification`. |
| `DynamicFilterResolverBenchmark` | `argumentResolver_interfaceProxy` | Mede resolução MVC para interface `Specification`. |
| `DynamicFilterResolverBenchmark` | `argumentResolver_fetchingDecorator` | Mede resolução MVC com fetching/decorator. |
| `DynamicFilterResolverBenchmark` | `annotationUtils_reusedInput` | Mede busca de metadata com input reutilizado. |
| `DynamicFilterResolverBenchmark` | `annotationUtils_newInputInstance` | Mede busca de metadata com input equivalente novo. |
| `DynamicFilterResolverPerf02Benchmark` | `perf02_specification_toPredicate_manyFilters` | Mede Criteria predicate em conjunction pesada. |
| `DynamicFilterResolverPerf02Benchmark` | `perf02_specification_toPredicate_repeatedNestedPath` | Mede reuso/cache em paths aninhados repetidos. |
| `DynamicFilterResolverPerf02Benchmark` | `perf02_fetchingDecorator_deepPaths` | Mede fetch decorator com paths profundos. |
| `DynamicFilterResolverPerf02Benchmark` | `perf02_fetchingDecorator_overlappingPaths` | Mede fetch decorator com paths sobrepostos. |
| `DynamicFilterResolverPerf02Benchmark` | `perf02_annotationUtils_*` / `perf04_annotationUtils_*` | Mede cache após crescimento e eviction. |
| `DynamicFilterResolverPerf06ProxyBenchmark` | `perf06_proxyInvocation_*` | Mede invocação de proxy `Specification`. |
| `DynamicFilterRepositorySortPerfBenchmark` | `perf05_sortTranslation_*` | Mede tradução de sort otimizada versus legado. |

### Estados de Benchmark

🟢 CONFIRMADO

| Estado | Campos Principais | Setup |
|---|---|---|
| `StatementState` | `generator`, `resolver`, `input`, `parameters`, `precomputedStatement` | Cria pipeline de annotation statement e resolver JPA. |
| `AnnotationCacheState` | `sharedInput` | Prepara input `SearchPeopleAndGames`. |
| `ArgumentResolverState` | `argumentResolver`, `interfaceParameter`, `fetchingParameter`, `webRequest` | Mocka MVC request e method parameters. |
| `JpaPredicateState` | `applicationContext`, `entityManager`, `heavySpecification`, `repeatedNestedPathSpecification`, `deepFetchingSpecification`, `overlappingFetchingSpecification` | Sobe Spring/H2 sem web e cria specifications pesadas. |
| `CacheGrowthState` | `reusedInput` | Preenche cache com 20.000 annotations sintéticas. |
| `BoundedCacheHitState` | `hotInput` | Preenche cache com 50.000 annotations sintéticas. |
| `ProxyInvocationState` | `optimizedProxy`, `legacyProxy`, `root`, `query`, `criteriaBuilder` | Cria proxies e mocks Criteria. |
| `HeavyMappingsState` | `sort`, `filters` | Cria 50 orders e 600 filtros com traduções. |
| `NoTranslationState` | `sort`, `filters` | Cria 50 orders e 600 filtros sem tradução necessária. |

### Configuração JMH

🟢 CONFIRMADO

| Classe | Mode | Unidade | Warmup | Measurement | Fork | Threads |
|---|---|---|---|---|---:|---:|
| `DynamicFilterResolverBenchmark` | `AverageTime` | microssegundos | 5 x 500 ms | 8 x 500 ms | 1 | 1 |
| `DynamicFilterResolverPerf02Benchmark` | `AverageTime` | microssegundos | 5 x 500 ms | 10 x 500 ms | 1 | 1 |
| `DynamicFilterResolverPerf06ProxyBenchmark` | `AverageTime` | microssegundos | 5 x 500 ms | 10 x 500 ms | 1 | 1 |
| `DynamicFilterRepositorySortPerfBenchmark` | `AverageTime` | microssegundos | 5 x 500 ms | 10 x 500 ms | 1 | 1 |
