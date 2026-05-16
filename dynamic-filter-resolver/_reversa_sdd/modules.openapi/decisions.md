# Modules OpenAPI, Decisões de Design

> Spec operacional gerada pelo Reversa Writer em 2026-05-16.
> Escala de confiança: 🟢 CONFIRMADO, 🟡 INFERIDO, 🔴 LACUNA.

| ID | Decisão | Consequência | Evidência | Confiança |
|---|---|---|---|---|
| DEC-OAI-01 | Remover parâmetro técnico da documentação. | Usuário vê apenas parâmetros reais de filtro. | `DynaFilterOperationCustomizer.customize` | 🟢 |
| DEC-OAI-02 | Omitir filtros com constantes. | OpenAPI não sugere parâmetros que o usuário não controla. | ADR 004, `customizeParameter` | 🟢 |
| DEC-OAI-03 | Modelar `Dynamic` como array string. | Primeiro item carrega código de operação; demais carregam valores. | `customizeParameter` | 🟢 |
| DEC-OAI-04 | Preservar parâmetro existente como `path`. | Contratos de path variables não são degradados para query. | `customizeParameter` | 🟢 |
| DEC-OAI-05 | Usar Bean Validation do field alvo no schema. | Documentação OpenAPI reflete constraints do domínio consumidor. | `SchemaValidationUtils` | 🟢 |

## Pendências

- 🟢 Decisão confirmada pelo usuário: corrigir a ausência de `DisjunctionFrom` na condição inicial, mesmo que isso altere a documentação OpenAPI gerada para parâmetros isolados.
- 🟡 Expandir cobertura de testes do customizer principal.
