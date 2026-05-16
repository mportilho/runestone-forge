# Core, Decisões de Design

> Spec operacional gerada pelo Reversa Writer em 2026-05-16.
> Escala de confiança: 🟢 CONFIRMADO, 🟡 INFERIDO, 🔴 LACUNA.

## Decisões Vigentes

| ID | Decisão | Consequência operacional | Evidência | Confiança |
|---|---|---|---|---|
| DEC-CORE-01 | Usar árvore de statements como modelo intermediário. | Adaptadores como JPA consomem `StatementWrapper` em vez de annotations diretamente. | `_reversa_sdd/adrs/001-statement-tree-as-intermediate-model.md`, `core/model/statement/*` | 🟢 |
| DEC-CORE-02 | Permitir contratos externos por `ConjunctionFrom`, `DisjunctionFrom` e `StatementFrom`. | Filtros podem ser reutilizados fora do parâmetro técnico, exigindo `FilterTarget` em alguns cenários. | `_reversa_sdd/adrs/002-filter-contracts-can-live-outside-controller-parameters.md` | 🟢 |
| DEC-CORE-03 | Separar `FilterRequestData` de `FilterData`. | Um modelo documenta a entrada possível; outro representa filtro já resolvido para execução. | `FilterRequestData.java`, `FilterData.java` | 🟢 |
| DEC-CORE-04 | Aplicar precedência `constantValues > request > defaultValues`. | Filtros constantes podem impor escopo técnico invisível ao usuário. | `DefaultStatementGenerator.java:151-165` | 🟢 |
| DEC-CORE-05 | Representar ausência de filtro com `NoOpStatement`. | Adaptadores conseguem tratar consulta irrestrita sem `null`. | `StatementGenerator.java`, `AnnotationStatementGenerator.java:68-70` | 🟢 |
| DEC-CORE-06 | Resolver operação dinâmica por código curto. | O usuário externo pode escolher operação quando o contrato permite `Dynamic`. | `ComparisonOperation.java`, `DefaultStatementGenerator.java:91-124` | 🟢 |
| DEC-CORE-07 | Cachear metadata de annotations com Caffeine. | Reduz custo de reflection; cache é limitado e configurável. | `TypeAnnotationUtils.java:44-50`, `TypeAnnotationUtils.java:429-439` | 🟢 |
| DEC-CORE-08 | Exigir decorators stateless/thread-safe. | Decorators podem ser reaproveitados e compostos com menor risco de estado compartilhado. | `FilterDecorator.java:31-35` | 🟢 |
| DEC-CORE-09 | Rejeitar retorno `null` em composição de decorators. | Falha explícita evita filtro concreto inválido. | `CompositeFilterDecorator.java:45-50` | 🟢 |
| DEC-CORE-10 | Manter código comentado de `ConditionalStatementBuilder` fora do runtime. | Reimplementação não deve tratá-lo como comportamento ativo. | `ConditionalStatementBuilder.java:1-46` | 🟢 |

## Decisões Pendentes

| ID | Decisão pendente | Opções conhecidas | Impacto | Confiança |
|---|---|---|---|---|
| DEC-CORE-04 | Tratar collection raw, wildcard e generic type não materializado em `findFilterField` com erro explícito. | A reconstrução deve lançar `DynamicFilterConfigurationException` clara, em vez de expor falhas de cast implícitas. | `TypeAnnotationUtils.java:337-356`, `_reversa_sdd/questions.md#pergunta-1` | 🟢 |
| PEND-CORE-02 | Comportamento esperado para sub-statement vazio negado. | Ignorar sub-statement; gerar `NoOpStatement` negado; falhar explicitamente. | Afeta árvores com filtros opcionais aninhados. | 🟡 |
| PEND-CORE-03 | Se a referência a `Specification<T>` deve permanecer no core em uma reimplementação nova. | Preservar legado; mover inferência para adaptador JPA. | Afeta pureza arquitetural do core. | 🟡 |

## Regras de Compatibilidade

- 🟢 **CONFIRMADO** — Alterar a semântica de `constantValues`, `defaultValues`, `Dynamic`, `NoOpStatement` ou composição AND/OR é alteração comportamental de alto risco.
- 🟢 **CONFIRMADO** — `StatementWrapper` é contrato comum entre `core`, `modules.jpa` e `modules.openapi`; mudanças nele exigem atualização coordenada.
- 🟡 **INFERIDO** — Novos adaptadores devem implementar `DynamicFilterResolver` e `FilterOperationService` sem alterar o modelo de annotations.
