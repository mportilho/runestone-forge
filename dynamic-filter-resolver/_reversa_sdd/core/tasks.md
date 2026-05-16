# Core, Tarefas de Implementação

> Spec operacional gerada pelo Reversa Writer em 2026-05-16.
> Escala de confiança: 🟢 CONFIRMADO, 🟡 INFERIDO, 🔴 LACUNA.

## Pré-requisitos

- [ ] 🟢 Dependências Java 21 e Maven configuradas conforme `pom.xml` e parent do workspace.
- [ ] 🟢 Biblioteca Caffeine disponível para cache local de metadados.
- [ ] 🟢 Dependências de suporte do `runestone-toolkit` disponíveis para assertions e contratos internos.
- [ ] 🟢 API Spring Data JPA disponível em escopo compatível quando a inferência de `Specification<T>` for necessária.
- [ ] 🟢 Testes unitários com JUnit Jupiter e AssertJ disponíveis para validar regras de geração, metadata, dynamic operation e decorators.
- [ ] 🟢 Política esperada para collections raw, wildcards e generics não materializados definida: falhar com `DynamicFilterConfigurationException` explícita.

## Tarefas

> Cada tarefa referencia o arquivo do legado de onde o comportamento foi extraído.

- [ ] T-CORE-01, Criar os modelos operacionais `FilterData`, `FilterRequestData`, `FilterModifier` e `ModIgnoreCase`.
  - Origem no legado: `src/main/java/com/runestone/dynafilter/core/model/FilterData.java:49`, `src/main/java/com/runestone/dynafilter/core/model/FilterRequestData.java:23`, `src/main/java/com/runestone/dynafilter/core/model/modifiers/ModIgnoreCase.java`
  - Critério de pronto: `FilterData` valida `parameters` e `values` não vazios e com tamanhos iguais; `findOneValue` rejeita múltiplos valores; `FilterRequestData` representa todos os campos de contrato documentados em `requirements.md`.
  - Confiança: 🟢

- [ ] T-CORE-02, Implementar a árvore lógica de statements com visitor/analyser.
  - Origem no legado: `src/main/java/com/runestone/dynafilter/core/model/statement/LogicalStatement.java`, `CompoundStatement.java`, `NegatedStatement.java`, `NoOpStatement.java`, `LogicOperator.java`, `StatementAnalyser.java`, `StatementVisitor.java`
  - Critério de pronto: folhas encapsulam `FilterData`; composições mantêm left/right/operator; negação encapsula statement; `NoOpStatement` representa ausência de filtro; visitantes/analisadores percorrem todos os tipos.
  - Confiança: 🟢

- [ ] T-CORE-03, Implementar annotations públicas de declaração de filtros.
  - Origem no legado: `src/main/java/com/runestone/dynafilter/core/generator/annotation/Filter.java`, `Conjunction.java`, `Disjunction.java`, `Statement.java`, `ConjunctionFrom.java`, `DisjunctionFrom.java`, `StatementFrom.java`, `FilterTarget.java`, `FilterDecorators.java`
  - Critério de pronto: annotations suportam declaração inline e externa de filtros, sub-statements, entidade alvo e decorators com os campos descritos em `data-dictionary.md`.
  - Confiança: 🟢

- [ ] T-CORE-04, Implementar `AnnotationStatementInput` como chave de análise segura.
  - Origem no legado: `src/main/java/com/runestone/dynafilter/core/generator/annotation/AnnotationStatementInput.java:31-40`
  - Critério de pronto: construtor clona defensivamente o array de annotations, calcula hash no construtor e preserva igualdade para entradas semanticamente equivalentes.
  - Confiança: 🟢

- [ ] T-CORE-05, Implementar extração e cache de metadados em `TypeAnnotationUtils`.
  - Origem no legado: `src/main/java/com/runestone/dynafilter/core/generator/annotation/TypeAnnotationUtils.java:44-50`, `TypeAnnotationUtils.java:244-327`, `TypeAnnotationUtils.java:429-439`
  - Critério de pronto: metadata é cacheada por `AnnotationStatementInput`; cache tem limite padrão `4096` e override por `runestone.dynafilter.annotation.cache.max-size`; extração percorre annotations diretas, meta-annotations, interfaces não-`java.*` e superclasses.
  - Confiança: 🟢

- [ ] T-CORE-06, Implementar validação de metadata de filtros.
  - Origem no legado: `src/main/java/com/runestone/dynafilter/core/generator/annotation/TypeAnnotationUtils.java:176-188`
  - Critério de pronto: filtros sem parâmetros, com `defaultValues` incompatíveis ou com `constantValues` incompatíveis falham antes de gerar statements.
  - Confiança: 🟢

- [ ] T-CORE-07, Implementar resolução de fields por path e entidade alvo.
  - Origem no legado: `src/main/java/com/runestone/dynafilter/core/generator/annotation/TypeAnnotationUtils.java:337-408`
  - Critério de pronto: paths simples e dot-notation são resolvidos em fields; navegação por collection parametrizada usa o primeiro argumento genérico; fields em superclasses são aceitos; ausência de field lança `DynamicFilterConfigurationException`; target class é inferido por annotations `From`, `Specification<T>` ou `ConditionalStatement` com `@FilterTarget`.
  - Confiança: 🟢

- [ ] T-CORE-08, Implementar contratos de geração e wrapper de statement.
  - Origem no legado: `src/main/java/com/runestone/dynafilter/core/generator/StatementGenerator.java`, `StatementWrapper.java`, `ConditionalStatement.java`
  - Critério de pronto: `StatementGenerator` define retorno `StatementWrapper`; `StatementWrapper` rejeita statement nulo e normaliza mapas/listas nulos; `ConditionalStatement` encapsula wrapper e decorator opcional.
  - Confiança: 🟢

- [ ] T-CORE-09, Implementar `DefaultStatementGenerator.computeValues`.
  - Origem no legado: `src/main/java/com/runestone/dynafilter/core/generator/DefaultStatementGenerator.java:143-192`
  - Critério de pronto: nomes de parâmetros vazios falham; constantes prevalecem; parâmetros enviados prevalecem sobre defaults; defaults e constantes podem ser transformados por `ValueExpressionResolver`.
  - Confiança: 🟢

- [ ] T-CORE-10, Implementar `DefaultStatementGenerator.createFilterData` com operação dinâmica.
  - Origem no legado: `src/main/java/com/runestone/dynafilter/core/generator/DefaultStatementGenerator.java:91-124`, `src/main/java/com/runestone/dynafilter/core/operation/ComparisonOperation.java:29-50`
  - Critério de pronto: códigos `EQ`, `LT`, `LE`, `GT`, `GE`, `LK`, `SW`, `EW`, `IN`, `BT` são resolvidos; prefixo `N`/`n` nega; `IN` empacota valores; `BT` exige dois valores e renomeia parâmetros para `From`/`To`; formatos inválidos lançam `StatementGenerationException`.
  - Confiança: 🟢

- [ ] T-CORE-11, Implementar criação de statements por blocos lógicos.
  - Origem no legado: `src/main/java/com/runestone/dynafilter/core/generator/DefaultStatementGenerator.java:52-66`, `src/main/java/com/runestone/dynafilter/core/generator/annotation/AnnotationStatementGenerator.java:116-130`
  - Critério de pronto: primeiro filtro vira `LogicalStatement`; filtros seguintes são encadeados em `CompoundStatement`; filtros negados viram `NegatedStatement`; sub-statements aplicam operador oposto no interior e operador externo entre sub-statements.
  - Confiança: 🟢

- [ ] T-CORE-12, Implementar `AnnotationStatementGenerator.generateStatements`.
  - Origem no legado: `src/main/java/com/runestone/dynafilter/core/generator/annotation/AnnotationStatementGenerator.java:52-78`
  - Critério de pronto: parâmetros nulos viram mapa vazio; metadados são extraídos; filtros obrigatórios ausentes falham; `decoratedFilters` e `allFilters` são preenchidos; nenhum statement gera `NoOpStatement`; múltiplos statements raiz são combinados por conjunction.
  - Confiança: 🟢

- [ ] T-CORE-13, Implementar portas e dispatch de operações.
  - Origem no legado: `src/main/java/com/runestone/dynafilter/core/operation/FilterOperation.java`, `FilterOperationService.java`, `AbstractFilterOperationService.java:43-50`, `DefinedFilterOperation.java`, `operation/types/*.java`
  - Critério de pronto: operações são registráveis por classe de marker interface; `createFilter` rejeita `FilterData` nulo e operação não registrada com erro explícito.
  - Confiança: 🟢

- [ ] T-CORE-14, Implementar decorators e composição segura.
  - Origem no legado: `src/main/java/com/runestone/dynafilter/core/resolver/FilterDecorator.java:31-56`, `src/main/java/com/runestone/dynafilter/core/resolver/CompositeFilterDecorator.java:42-50`, `FilterDecoratorFactory.java`
  - Critério de pronto: contrato documenta stateless/thread-safe; `FilterDecorator.of` compõe decorators; composição aplica em ordem e falha quando filtro, statement ou retorno intermediário é nulo.
  - Confiança: 🟢

- [ ] T-CORE-15, Implementar exceções de domínio técnico.
  - Origem no legado: `src/main/java/com/runestone/dynafilter/core/exceptions/StatementGenerationException.java`, `MultipleFilterDataValuesException.java`, `FilterOperationNotDefinedException.java`, `DynamicFilterConfigurationException.java`
  - Critério de pronto: erros de geração, múltiplos valores indevidos, operação não definida e configuração inválida possuem tipos próprios e são usados nos fluxos correspondentes.
  - Confiança: 🟢

- [ ] T-CORE-16, Preservar ou remover conscientemente código legado comentado fora do runtime.
  - Origem no legado: `src/main/java/com/runestone/dynafilter/core/generator/ConditionalStatementBuilder.java:1-46`
  - Critério de pronto: decisão explícita registrada; se reimplementação do zero, não recriar comportamento comentado como ativo sem requisito novo.
  - Confiança: 🟢

- [ ] T-CORE-17, Implementar erro explícito para generics não triviais em path resolution.
  - Origem no legado: `src/main/java/com/runestone/dynafilter/core/generator/annotation/TypeAnnotationUtils.java:337-356`
  - Critério de pronto: collection raw, wildcard e generic não materializado falham com `DynamicFilterConfigurationException` explícita e coberta por teste.
  - Confiança: 🟢

## Tarefas de Teste

- [ ] TT-CORE-01, Testar geração de `NoOpStatement`, statement único e múltiplos statements combinados por conjunction.
  - Origem no legado: `src/test/java/com/runestone/dynafilter/core/generator/TestAnnotationStatementGenerator.java:63-210`
  - Critério de pronto: cobre cenários feliz, vazio e múltiplo descritos em `requirements.md`.
  - Confiança: 🟢

- [ ] TT-CORE-02, Testar precedência de valores, defaults, constantes e resolução de expressão.
  - Origem no legado: `src/test/java/com/runestone/dynafilter/core/generator/TestDefaultStatementGenerator.java:73-248`
  - Critério de pronto: constantes vencem parâmetros; parâmetros vencem defaults; strings e arrays de strings podem ser resolvidos por `ValueExpressionResolver`; erros são encapsulados.
  - Confiança: 🟢

- [ ] TT-CORE-03, Testar operação dinâmica positiva, negada, `IN`, `BT` e formatos inválidos.
  - Origem no legado: `src/test/java/com/runestone/dynafilter/core/generator/TestStatementGeneratorWithDynamicFilters.java:43-267`
  - Critério de pronto: cada código curto conhecido possui teste; prefixo `N` nega; `BT` inválido falha.
  - Confiança: 🟢

- [ ] TT-CORE-04, Testar extração de annotations por tipo, interface, annotation direta, meta-annotation e contratos externos.
  - Origem no legado: `src/test/java/com/runestone/dynafilter/core/generator/annotation/TestTypeAnnotationUtils.java:40-181`
  - Critério de pronto: metadata encontrada corresponde ao contrato declarado e ignora meta-annotations de `java.lang.annotation`.
  - Confiança: 🟢

- [ ] TT-CORE-05, Testar cache de `TypeAnnotationUtils`.
  - Origem no legado: `src/test/java/com/runestone/dynafilter/core/generator/annotation/TestTypeAnnotationUtils.java:139-181`
  - Critério de pronto: entradas equivalentes fazem hit; limite de cache é respeitado; array de annotations mutado externamente não altera chave interna.
  - Confiança: 🟢

- [ ] TT-CORE-06, Testar validações de erro em `FilterData`, `AbstractFilterOperationService` e `CompositeFilterDecorator`.
  - Origem no legado: `src/main/java/com/runestone/dynafilter/core/model/FilterData.java:60-98`, `AbstractFilterOperationService.java:43-50`, `CompositeFilterDecorator.java:42-50`
  - Critério de pronto: nulos, tamanhos incompatíveis, operação ausente e decorator com retorno nulo falham explicitamente.
  - Confiança: 🟢

- [ ] TT-CORE-07, Criar teste novo para erro explícito em collection raw, wildcard e generic não materializado em `findFilterField`.
  - Origem no legado: `src/main/java/com/runestone/dynafilter/core/generator/annotation/TypeAnnotationUtils.java:337-356`
  - Critério de pronto: teste confirma `DynamicFilterConfigurationException` explícita e rastreável.
  - Confiança: 🟢

- [ ] TT-CORE-08, Criar teste novo para sub-statement vazio negado.
  - Origem no legado: `src/main/java/com/runestone/dynafilter/core/generator/annotation/AnnotationStatementGenerator.java:116-123`
  - Critério de pronto: confirma se o legado aceita, ignora ou falha quando sub-statement sem filtros aplicáveis é negado; resultado deve ser documentado em `questions.md` se depender de decisão humana.
  - Confiança: 🟡

## Tarefas de Migração de Dados

- [ ] TM-CORE-01, Nenhuma migração de dados é aplicável para a unit `core`.
  - Origem no legado: `_reversa_sdd/architecture.md`, seção Dados e Persistência.
  - Critério de pronto: reimplementação não cria schema ou migration para o core, pois ele não define entidades JPA produtivas.
  - Confiança: 🟢

## Ordem Sugerida

1. Implementar modelos e árvore lógica: T-CORE-01, T-CORE-02.
2. Implementar annotations e input de metadata: T-CORE-03, T-CORE-04.
3. Implementar extração, cache e validação de metadata: T-CORE-05, T-CORE-06, T-CORE-07.
4. Implementar wrappers e geração base: T-CORE-08, T-CORE-09, T-CORE-10, T-CORE-11, T-CORE-12.
5. Implementar extensões e erros: T-CORE-13, T-CORE-14, T-CORE-15.
6. Tratar decisões de borda: T-CORE-16, T-CORE-17.
7. Executar testes na ordem TT-CORE-01 a TT-CORE-08, priorizando primeiro as regras Must de `requirements.md`.

## Lacunas Pendentes (🔴)

- 🟢 Política oficial definida pelo usuário: `TypeAnnotationUtils.findFilterField` deve falhar explicitamente com `DynamicFilterConfigurationException` para collection raw, wildcard e generic type não materializado.
- 🟡 Confirmar comportamento esperado para sub-statement vazio negado em `AnnotationStatementGenerator.createStatementFromFilterStatements`.
- 🟡 Confirmar se a reimplementação deve preservar a referência de tipo a `Specification<T>` dentro do core ou se essa inferência deve migrar para adaptador específico em uma arquitetura nova.
