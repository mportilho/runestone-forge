# Permissões e Acesso — dynamic-filter-resolver

> Gerado pelo Reversa Detective em 2026-05-16.
> Escala de confiança: 🟢 CONFIRMADO, 🟡 INFERIDO, 🔴 LACUNA.

## Sumário

🟢 **CONFIRMADO** — Não há implementação de autenticação, autorização, RBAC, ACL, `@PreAuthorize`, `@Secured`, `SecurityContext`, `GrantedAuthority` ou papéis de usuário no código de produção do módulo.

🟢 **CONFIRMADO** — O módulo é uma biblioteca de infraestrutura para filtros dinâmicos em aplicações Spring/JPA. Ele não decide quem pode acessar endpoints ou dados; ele constrói filtros e documentação com base em annotations declaradas por aplicações consumidoras.

🟢 **CONFIRMADO PELO USUÁRIO** — A autenticação/autorização real permanece na aplicação consumidora, mas a biblioteca deve oferecer mecanismo próprio de allowlist/denylist para reduzir risco de filtros sobre campos sensíveis.

## Matriz De Capacidades Técnicas

| Ator técnico | Capacidade | Restrição | Confiança |
|---|---|---|---|
| Desenvolvedor da aplicação consumidora | Declarar filtros por annotations `@Filter`, `@Conjunction`, `@Disjunction`, `@ConjunctionFrom`, `@DisjunctionFrom`. | Deve apontar paths válidos e parâmetros consistentes. | 🟢 |
| Desenvolvedor da aplicação consumidora | Declarar entidade alvo por `@FilterTarget`. | Necessário quando o tipo do parâmetro não permite inferir a entidade. | 🟢 |
| Desenvolvedor da aplicação consumidora | Declarar decorators customizados por `@FilterDecorators`. | Decorators devem ser stateless/thread-safe conforme contrato de `FilterDecorator`. | 🟢 |
| Desenvolvedor da aplicação consumidora | Declarar fetch joins por `@Fetching`. | Deve considerar limitações do provedor JPA, como múltiplas bags no Hibernate. | 🟢 |
| Chamador HTTP da API consumidora | Enviar query parameters ou path variables documentados/derivados dos filtros. | Só influencia filtros cujo parâmetro está exposto; `constantValues` ignoram entrada do usuário. | 🟢 |
| Chamador HTTP da API consumidora | Usar operação dinâmica quando o filtro permitir `Dynamic`. | Primeiro item precisa ser código válido; `BT` exige dois valores; prefixo `N`/`n` nega. | 🟢 |
| Spring MVC | Resolver parâmetro técnico para `ConditionalStatement` ou `Specification`. | Requer configuração servlet dinâmica habilitada. | 🟢 |
| SpringDoc OpenAPI | Expandir filtro técnico em parâmetros OpenAPI. | Não deve mostrar filtros constantes ou decorados como entrada comum. | 🟢 |

## Matriz RBAC

| Papel | Permissão | Evidência | Status |
|---|---|---|---|
| Usuário autenticado | N/A | Nenhum código de segurança no módulo. | 🔴 LACUNA |
| Administrador | N/A | Nenhum código de segurança no módulo. | 🔴 LACUNA |
| Sistema interno | N/A | Nenhum mecanismo de service account ou token. | 🔴 LACUNA |

## Restrições De Segurança Indiretas

🟢 **CONFIRMADO** — Paths de filtros são definidos em código por annotations, não enviados diretamente como path arbitrário pelo usuário. Isso reduz a superfície de injeção de propriedades JPA no request.

🟢 **CONFIRMADO** — Valores recebidos são convertidos para o tipo real do path antes de construir predicados Criteria API, delegando parametrização ao Criteria Builder em vez de concatenar JPQL/SQL manualmente.

🟢 **CONFIRMADO** — `FilterConfigurationAnalyserBeanPostProcessor` valida paths de filtros em controllers durante inicialização quando a entidade alvo é conhecida.

🟢 **CONFIRMADO** — `constantValues` permitem impor filtros invisíveis ao usuário, como `deleted=false`, desde que a aplicação consumidora declare essa regra.

🟡 **INFERIDO** — Filtros constantes podem funcionar como restrição de escopo de dados, mas não substituem autorização, pois o módulo não valida identidade, tenant, papel ou ownership.

## Lacunas

🟢 **CONFIRMADO PELO USUÁRIO** — Autorização por papel, tenant, ownership ou escopo de usuário permanece responsabilidade da aplicação consumidora.

🟢 **CONFIRMADO PELO USUÁRIO** — Integração direta com Spring Security não é requisito da biblioteca neste escopo.

🟢 **CONFIRMADO PELO USUÁRIO** — A reconstrução deve prever mecanismo próprio de allowlist/denylist para campos sensíveis.

🟢 **CONFIRMADO PELO USUÁRIO** — Auditoria de identidade e eventos de segurança permanece na aplicação consumidora; a biblioteca não precisa emitir auditoria própria.
