# Lacunas Pendentes — dynamic-filter-resolver

> Gerado pelo Revisor em 2026-05-16.
> Nível de documentação: completo.

---

## Decisões Humanas Respondidas

| ID | Área | Lacuna | Pergunta |
|----|------|--------|----------|
| G-001 | Core | `findFilterField` deve falhar com `DynamicFilterConfigurationException` explícita para collection raw, wildcard e generic type não materializado. | `questions.md#pergunta-1` |
| G-002 | OpenAPI | `@DisjunctionFrom` isolado deve ser corrigido e documentado pelo customizer. | `questions.md#pergunta-2` |
| G-003 | JPA | Uso de `DynamicFilterJpaRepositoryImpl` sem resolver deve falhar com erro explícito. | `questions.md#pergunta-3` |
| G-004 | JPA | Proxy de `Specification` tem contrato limitado a `toPredicate`; default methods/Object methods fora do escopo obrigatório. | `questions.md#pergunta-4` |
| G-005 | JPA | Limitações de múltiplas bags/fetch joins com Hibernate devem ser documentadas como responsabilidade da aplicação consumidora. | `questions.md#pergunta-5` |
| G-006 | Segurança | A biblioteca deve oferecer mecanismo próprio de allowlist/denylist para campos sensíveis. | `questions.md#pergunta-6` |
| G-007 | Observabilidade | Logs, métricas e traces permanecem responsabilidade da aplicação consumidora. | `questions.md#pergunta-7` |

---

## Lacunas Confirmadas Como Fora Do Escopo Local Atual

- 🔴 Não há schema produtivo, DDL ou migrations neste módulo; o ERD representa fixtures de teste.
- 🔴 Não há endpoint produtivo próprio; os controllers analisados são fixtures ou pontos de integração de aplicações consumidoras.
- 🔴 Não há runtime autônomo, Dockerfile, deployment ou pipeline CI/CD local neste módulo.
- 🟢 Não há RBAC/ACL/autenticação no código de produção local; isso permanece responsabilidade da aplicação consumidora conforme confirmação do usuário.

---

## Reclassificações Feitas Pelo Revisor

| De | Para | Afirmação | Evidência |
|----|------|-----------|-----------|
| 🔴 | 🟢 | `findFilterField` com generics não triviais deixou de ser “não validado”: a limitação técnica foi confirmada e a política foi definida como erro explícito. | `TypeAnnotationUtils.java:345-349`, `_reversa_sdd/questions.md#pergunta-1` |
| 🟡/🔴 | 🟢 | `@DisjunctionFrom` isolado deixou de ser “possível”: a não detecção foi confirmada e a política foi definida como correção. | `DynaFilterOperationCustomizer.java:63-64`, `_reversa_sdd/questions.md#pergunta-2` |

---

## Observação

As decisões acima removem os bloqueios humanos identificados pelo Revisor. As lacunas restantes descrevem ausência estrutural do módulo legado, não dúvidas pendentes para reconstrução.
