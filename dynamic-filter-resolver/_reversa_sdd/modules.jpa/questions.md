# Modules JPA, Perguntas e Lacunas

> Spec operacional gerada pelo Reversa Writer em 2026-05-16.
> Escala de confiança: 🟢 CONFIRMADO, 🟡 INFERIDO, 🔴 LACUNA.

| ID | Pergunta | Impacto | Evidência | Confiança |
|---|---|---|---|---|
| Q-JPA-01 | Interfaces customizadas de `Specification` devem suportar default methods e métodos de `Object`? | Respondido pelo usuário: contrato limitado a `toPredicate`; default methods/Object methods fora do escopo obrigatório. | Testes relacionados desabilitados em `TestSpecDynaFilterArgumentResolver`, `_reversa_sdd/questions.md#pergunta-4` | 🟢 |
| Q-JPA-02 | Uso manual de `DynamicFilterJpaRepositoryImpl` fora do Spring deve ser suportado? | Respondido pelo usuário: uso sem resolver deve falhar com erro explícito. | `DynamicFilterJpaRepositoryImpl` depende de BPP, `_reversa_sdd/questions.md#pergunta-3` | 🟢 |
| Q-JPA-03 | Há limites documentados para múltiplas bags/fetch joins com Hibernate? | Respondido pelo usuário: documentar como responsabilidade da aplicação consumidora. | `FetchingFilterDecorator` cria fetches declarativos, `_reversa_sdd/questions.md#pergunta-5` | 🟢 |

## Lacunas Assumidas

- 🟢 Proxy customizado deve ser tratado como compatibilidade limitada a `toPredicate`, conforme decisão explícita do usuário.
- 🟢 O typo `convertoToSpecification` é comportamento público legado e não deve ser corrigido sem breaking change.
