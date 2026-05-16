# Modules OpenAPI, Perguntas e Lacunas

> Spec operacional gerada pelo Reversa Writer em 2026-05-16.
> Escala de confiança: 🟢 CONFIRMADO, 🟡 INFERIDO, 🔴 LACUNA.

| ID | Pergunta | Impacto | Evidência | Confiança |
|---|---|---|---|---|
| Q-OAI-01 | `@DisjunctionFrom` isolado deve ser suportado pelo customizer na reconstrução? | Respondido pelo usuário: corrigir a detecção e documentar esse caso. | `DynaFilterOperationCustomizer.java:63-64`, `_reversa_sdd/questions.md#pergunta-2` | 🟢 |
| Q-OAI-02 | Deve haver teste de snapshot/contrato OpenAPI completo? | Aumenta garantia de documentação gerada. | Não há teste direto de `DynaFilterOperationCustomizer`. | 🟡 |
| Q-OAI-03 | Como documentar filtros decorados em OpenAPI? | Histórico indica que decorados não devem aparecer como comuns. | ADR 004 e domínio técnico. | 🟢 |

## Lacunas

- 🟡 Ausência de testes diretos para o fluxo principal de `DynaFilterOperationCustomizer`.
- 🔴 Não há OpenAPI produtivo no módulo para comparação end-to-end.
