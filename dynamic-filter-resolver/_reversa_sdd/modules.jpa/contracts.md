# Modules JPA, Contratos Externos

> Spec operacional gerada pelo Reversa Writer em 2026-05-16.
> Escala de confiança: 🟢 CONFIRMADO, 🟡 INFERIDO, 🔴 LACUNA.

## Contratos Java Públicos

| Contrato | Consumidor | Entrada | Saída | Confiança |
|---|---|---|---|---|
| `@EnableDynamicFilterServletConfiguration` | Aplicação Spring | Annotation em configuração/aplicação | Importa auto-config, BPPs e MVC configurer | 🟢 |
| `SpecificationDynamicFilterArgumentResolver` | Spring MVC | `MethodParameter`, `NativeWebRequest` | `ConditionalStatement`, `Specification` ou proxy | 🟢 |
| `DynamicFilterJpaRepository<T, I>` | Repositories Spring Data | `ConditionalStatement`, `Pageable`, `Sort`, `EntityGraph` | Resultados JPA equivalentes aos métodos Spring Data | 🟢 |
| `@Fetching` / `@Fetches` | Desenvolvedor consumidor | Paths de fetch e `JoinType` | Fetch joins declarativos na query | 🟢 |
| `ModJoinTypeLeft/Right/Inner` | Contrato de filtro | Modifier no `@Filter` | Join type usado em path com associação | 🟢 |

## Contrato MVC

```text
Entrada HTTP -> query parameters + URI template variables
Parâmetro Java -> ConditionalStatement ou Specification/interface Specification
Saída -> objeto resolvido para o método controller
```

Regras:
- 🟢 Query parameter com um valor vira `String`.
- 🟢 Query parameter com múltiplos valores vira `String[]`.
- 🟢 URI template variables são adicionadas depois e podem sobrescrever chaves iguais.
- 🟢 Parâmetro sem annotation dinâmica compatível não é suportado pelo resolver.

## Contrato Repository

Regras:
- 🟢 `ConditionalStatement` é convertido para `Specification<T>` antes de delegar ao `SimpleJpaRepository`.
- 🟢 `findAll`, `findOne`, `count`, `exists`, `findBy` e overloads com paginação/sort/entity graph devem preservar semântica Spring Data.
- 🟢 `convertoToSpecification` mantém typo por compatibilidade pública.
- 🟡 A implementação exige injeção de resolver pelo BPP no uso Spring esperado.
