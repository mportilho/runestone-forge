# Core, Perguntas e Lacunas

> Spec operacional gerada pelo Reversa Writer em 2026-05-16.
> Escala de confiança: 🟢 CONFIRMADO, 🟡 INFERIDO, 🔴 LACUNA.

## Perguntas Bloqueantes

| ID | Pergunta | Por que importa | Evidência | Severidade | Confiança |
|---|---|---|---|---|---|
| Q-CORE-01 | Qual deve ser o comportamento oficial de `findFilterField` para collection raw, wildcard e generic type não materializado? | Respondido pelo usuário: falhar com `DynamicFilterConfigurationException` explícita. | `TypeAnnotationUtils.java:337-356`, `_reversa_sdd/questions.md#pergunta-1` | Alta | 🟢 |
| Q-CORE-02 | Um sub-statement vazio com `negate=true` deve ser ignorado, virar `NoOpStatement` negado ou falhar? | Pode haver construção de `NegatedStatement` sobre valor nulo em caso de sub-statement sem filtros aplicáveis. | `AnnotationStatementGenerator.java:116-123` | Média | 🟡 |
| Q-CORE-03 | A inferência de entidade alvo por `Specification<T>` deve continuar no core? | Em uma arquitetura estritamente agnóstica, essa regra poderia pertencer ao adaptador JPA. | `TypeAnnotationUtils.findFilterTargetClass`, ADR 001 | Média | 🟡 |

## Lacunas Não Bloqueantes

| ID | Lacuna | Tratamento recomendado | Confiança |
|---|---|---|---|
| L-CORE-01 | Não há telemetria runtime própria no core. | Delegar logs/métricas à aplicação consumidora ou aos adaptadores Spring. | 🔴 |
| L-CORE-02 | `DefinedFilterOperation` usa interfaces genéricas agregadas com raw types/precisão reduzida. | Preservar compatibilidade; só refatorar com testes de API pública. | 🟢 |
| L-CORE-03 | `ConditionalStatementBuilder.java` contém código comentado. | Não reimplementar como funcionalidade ativa sem requisito explícito. | 🟢 |

## Respostas Assumidas nas Specs

- 🟡 **INFERIDO** — Até validação humana, a reimplementação deve preservar o comportamento legado confirmado e falhar explicitamente em casos de generic type não suportado.
- 🟡 **INFERIDO** — Até validação humana, sub-statements vazios negados devem receber teste de caracterização antes de qualquer mudança.
