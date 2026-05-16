# Relatório de Confiança — dynamic-filter-resolver

> Gerado pelo Revisor em 2026-05-16.
> Escopo da contagem: artefatos existentes antes dos arquivos próprios do Revisor (`questions.md`, `gaps.md`, `confidence-report.md`).

---

## Resumo Geral

| Nível | Quantidade | Percentual |
|-------|------------|------------|
| 🟢 CONFIRMADO | 1160 | 83.8% |
| 🟡 INFERIDO | 144 | 10.4% |
| 🔴 LACUNA | 80 | 5.8% |
| **Total** | 1384 | 100% |

**Confiança geral:** 89.0%.

---

## Por Spec Principal

| Spec | 🟢 | 🟡 | 🔴 | Confiança |
|------|----|----|----|-----------|
| `_reversa_sdd/core/requirements.md` | 68 | 4 | 1 | 96% |
| `_reversa_sdd/core/design.md` | 89 | 6 | 2 | 95% |
| `_reversa_sdd/core/tasks.md` | 33 | 4 | 2 | 90% |
| `_reversa_sdd/modules.jpa/requirements.md` | 49 | 1 | 1 | 97% |
| `_reversa_sdd/modules.jpa/design.md` | 38 | 1 | 2 | 94% |
| `_reversa_sdd/modules.jpa/tasks.md` | 25 | 1 | 1 | 94% |
| `_reversa_sdd/modules.openapi/requirements.md` | 30 | 1 | 1 | 95% |
| `_reversa_sdd/modules.openapi/design.md` | 24 | 2 | 1 | 93% |
| `_reversa_sdd/modules.openapi/tasks.md` | 12 | 5 | 1 | 81% |
| `_reversa_sdd/test-support.performance/requirements.md` | 23 | 1 | 1 | 94% |
| `_reversa_sdd/test-support.performance/design.md` | 21 | 3 | 1 | 90% |
| `_reversa_sdd/test-support.performance/tasks.md` | 12 | 3 | 1 | 84% |
| `_reversa_sdd/architecture.md` | 46 | 7 | 3 | 88% |
| `_reversa_sdd/domain.md` | 58 | 3 | 5 | 90% |
| `_reversa_sdd/permissions.md` | 20 | 2 | 4 | 81% |
| `_reversa_sdd/traceability/code-spec-matrix.md` | 50 | 4 | 2 | 93% |
| `_reversa_sdd/traceability/spec-impact-matrix.md` | 33 | 4 | 4 | 85% |

---

## Lacunas Pendentes 🔴

- **Core:** decisão respondida: `findFilterField` deve falhar explicitamente com `DynamicFilterConfigurationException` para generics não triviais.
- **OpenAPI:** decisão respondida: `@DisjunctionFrom` isolado deve ser corrigido e documentado.
- **JPA:** decisões respondidas: erro explícito sem resolver, proxy limitado a `toPredicate`, limitações de fetch joins documentadas como responsabilidade da aplicação consumidora.
- **Segurança:** decisão respondida: biblioteca deve oferecer allowlist/denylist para campos sensíveis; autenticação/autorização permanece na aplicação consumidora.
- **Observabilidade:** decisão respondida: logs/métricas/traces ficam fora do escopo da biblioteca.
- **Escopo local:** schema produtivo, endpoints finais, deployment e RBAC não existem neste módulo.

Perguntas respondidas: `_reversa_sdd/questions.md`.

---

## Histórico de Reclassificações

| De | Para | Afirmação | Evidência |
|----|------|-----------|-----------|
| 🔴 | 🟢 | Limitação de `TypeAnnotationUtils.findFilterField` para generics não triviais confirmada e política oficial definida como erro explícito. | `TypeAnnotationUtils.java:345-349`, confirmação do usuário em `_reversa_sdd/questions.md#pergunta-1` |
| 🟡/🔴 | 🟢 | Não detecção de `@DisjunctionFrom` isolado confirmada e política definida como correção. | `DynaFilterOperationCustomizer.java:63-64`, confirmação do usuário em `_reversa_sdd/questions.md#pergunta-2` |
| 🔴 | 🟢 | Política de `findFilterField` definida: erro explícito por `DynamicFilterConfigurationException`. | Confirmação do usuário em `_reversa_sdd/questions.md#pergunta-1` |
| 🔴 | 🟢 | Política de `@DisjunctionFrom` isolado definida: corrigir detecção. | Confirmação do usuário em `_reversa_sdd/questions.md#pergunta-2` |
| 🟡 | 🟢 | Contratos JPA definidos: erro explícito sem resolver e proxy limitado a `toPredicate`. | Confirmação do usuário em `_reversa_sdd/questions.md#pergunta-3` e `#pergunta-4` |
| 🔴 | 🟢 | Segurança de campos sensíveis definida: biblioteca deve oferecer allowlist/denylist. | Confirmação do usuário em `_reversa_sdd/questions.md#pergunta-6` |

---

## Revisão Cruzada

- Engine externa consultada: não realizada.
- Motivo: nenhuma ferramenta `codex:*` estava disponível nesta sessão.
- Apontamentos recebidos: 0.

---

## Recomendações

- Implementar testes de caracterização para as decisões respondidas antes de reconstruir comportamento divergente do legado.
- Documentar explicitamente as limitações de fetch joins no contrato JPA.
- Modelar allowlist/denylist de campos sensíveis como requisito de segurança da biblioteca em qualquer roadmap de evolução.
