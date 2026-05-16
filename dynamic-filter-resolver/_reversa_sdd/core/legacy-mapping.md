# Legacy Mapping — Módulo `core`

> Gerado pelo Reversa Archaeologist em 2026-05-16.

## Unidade

🟢 CONFIRMADO — `core` corresponde ao pacote `src/main/java/com/runestone/dynafilter/core/`.

## Arquivos por responsabilidade

### Geração de statements

- `src/main/java/com/runestone/dynafilter/core/generator/StatementGenerator.java` — contrato de geração de `StatementWrapper`.
- `src/main/java/com/runestone/dynafilter/core/generator/DefaultStatementGenerator.java` — base para computar valores, negação, operações dinâmicas e encadear `FilterData` em statements.
- `src/main/java/com/runestone/dynafilter/core/generator/ValueExpressionResolver.java` — extensão funcional para resolução de expressões/defaults.
- `src/main/java/com/runestone/dynafilter/core/generator/StatementWrapper.java` — container da árvore, filtros decorados e catálogo de filtros.
- `src/main/java/com/runestone/dynafilter/core/generator/ConditionalStatement.java` — container de statement + decorator.
- `src/main/java/com/runestone/dynafilter/core/generator/ConditionalStatementBuilder.java` — código comentado, sem símbolos ativos no LSP.

### Anotações e metadados

- `src/main/java/com/runestone/dynafilter/core/generator/annotation/AnnotationStatementGenerator.java` — gerador concreto baseado em anotações.
- `src/main/java/com/runestone/dynafilter/core/generator/annotation/TypeAnnotationUtils.java` — extração, validação, cache e utilitários reflexivos.
- `src/main/java/com/runestone/dynafilter/core/generator/annotation/AnnotationStatementInput.java` — chave de entrada com cópia defensiva e hash cacheado.
- `src/main/java/com/runestone/dynafilter/core/generator/annotation/Filter.java` — cláusula de filtro.
- `src/main/java/com/runestone/dynafilter/core/generator/annotation/Conjunction.java` — grupo AND.
- `src/main/java/com/runestone/dynafilter/core/generator/annotation/Disjunction.java` — grupo OR.
- `src/main/java/com/runestone/dynafilter/core/generator/annotation/Statement.java` — sub-statement inline.
- `src/main/java/com/runestone/dynafilter/core/generator/annotation/ConjunctionFrom.java` — grupo AND com filtros externos.
- `src/main/java/com/runestone/dynafilter/core/generator/annotation/DisjunctionFrom.java` — grupo OR com filtros externos.
- `src/main/java/com/runestone/dynafilter/core/generator/annotation/StatementFrom.java` — sub-statement externo.
- `src/main/java/com/runestone/dynafilter/core/generator/annotation/FilterTarget.java` — alvo de filtro.
- `src/main/java/com/runestone/dynafilter/core/generator/annotation/FilterDecorators.java` — declaração de decorators.
- `src/main/java/com/runestone/dynafilter/core/generator/annotation/FilterAnnotationData.java` — DTO interno de bloco de anotações.
- `src/main/java/com/runestone/dynafilter/core/generator/annotation/FilterAnnotationStatement.java` — DTO interno de sub-statement.
- `src/main/java/com/runestone/dynafilter/core/generator/annotation/VirtualAnnotationHolder.java` — wrapper para percorrer meta-anotações.

### Modelo

- `src/main/java/com/runestone/dynafilter/core/model/FilterData.java` — dados resolvidos para operação concreta.
- `src/main/java/com/runestone/dynafilter/core/model/FilterRequestData.java` — dados requisitáveis/documentáveis de filtro.
- `src/main/java/com/runestone/dynafilter/core/model/FilterModifier.java` — marcador de modificadores.
- `src/main/java/com/runestone/dynafilter/core/model/modifiers/ModIgnoreCase.java` — modificador de case-insensitive.

### Árvore de statements

- `src/main/java/com/runestone/dynafilter/core/model/statement/AbstractStatement.java` — base visitável/analisável.
- `src/main/java/com/runestone/dynafilter/core/model/statement/LogicalStatement.java` — folha com `FilterData`.
- `src/main/java/com/runestone/dynafilter/core/model/statement/CompoundStatement.java` — nó binário lógico.
- `src/main/java/com/runestone/dynafilter/core/model/statement/NegatedStatement.java` — nó de negação.
- `src/main/java/com/runestone/dynafilter/core/model/statement/NoOpStatement.java` — statement vazio.
- `src/main/java/com/runestone/dynafilter/core/model/statement/LogicOperator.java` — enum `CONJUNCTION`/`DISJUNCTION`.
- `src/main/java/com/runestone/dynafilter/core/model/statement/StatementAnalyser.java` — visitor com retorno.
- `src/main/java/com/runestone/dynafilter/core/model/statement/StatementVisitor.java` — visitor sem retorno.

### Operações

- `src/main/java/com/runestone/dynafilter/core/operation/FilterOperation.java` — contrato de criação de filtro por `FilterData`.
- `src/main/java/com/runestone/dynafilter/core/operation/FilterOperationService.java` — contrato de serviço de operação.
- `src/main/java/com/runestone/dynafilter/core/operation/AbstractFilterOperationService.java` — dispatch por mapa de classes de operação.
- `src/main/java/com/runestone/dynafilter/core/operation/ComparisonOperation.java` — enum de códigos dinâmicos.
- `src/main/java/com/runestone/dynafilter/core/operation/DefinedFilterOperation.java` — interface marcador agregada.
- `src/main/java/com/runestone/dynafilter/core/operation/types/*.java` — interfaces marcadoras das operações suportadas.

### Resolução e decorators

- `src/main/java/com/runestone/dynafilter/core/resolver/DynamicFilterResolver.java` — contrato de resolução da árvore para filtro concreto.
- `src/main/java/com/runestone/dynafilter/core/resolver/FilterDecorator.java` — contrato de decoração thread-safe/stateless.
- `src/main/java/com/runestone/dynafilter/core/resolver/CompositeFilterDecorator.java` — composição ordenada de decorators.
- `src/main/java/com/runestone/dynafilter/core/resolver/FilterDecoratorFactory.java` — contrato de factory de decorators.

### Exceções

- `src/main/java/com/runestone/dynafilter/core/exceptions/StatementGenerationException.java`
- `src/main/java/com/runestone/dynafilter/core/exceptions/MultipleFilterDataValuesException.java`
- `src/main/java/com/runestone/dynafilter/core/exceptions/FilterOperationNotDefinedException.java`
- `src/main/java/com/runestone/dynafilter/core/exceptions/DynamicFilterConfigurationException.java`
