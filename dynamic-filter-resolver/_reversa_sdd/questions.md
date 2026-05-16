# Perguntas para Validação — dynamic-filter-resolver

> Gerado pelo Revisor em 2026-05-16.
> Responda aqui no chat ou preencha o campo **Resposta** de cada pergunta e me avise quando terminar.

---

## Pergunta 1

✅ Respondida em chat pelo usuário em 2026-05-16.

**Contexto:** `core` — `TypeAnnotationUtils.findFilterField` navega collections por cast direto em `TypeAnnotationUtils.java:345-349`.
**Spec afetada:** [`_reversa_sdd/core/requirements.md`]
**Pergunta:** Na reconstrução, collection raw, wildcard ou generic type não materializado devem preservar a falha legada ou devem falhar com `DynamicFilterConfigurationException` explícita?
**Impacto:** Define o critério de aceite de `RF-CORE-12`, `T-CORE-17` e `TT-CORE-07`.

**Resposta:** Usar erro explícito: trocar falhas de cast por `DynamicFilterConfigurationException` clara.

---

## Pergunta 2

✅ Respondida em chat pelo usuário em 2026-05-16.

**Contexto:** `modules.openapi` — `DynaFilterOperationCustomizer.customize` verifica `Disjunction.class` duas vezes e não verifica `DisjunctionFrom.class` em `DynaFilterOperationCustomizer.java:63-64`.
**Spec afetada:** [`_reversa_sdd/modules.openapi/requirements.md`]
**Pergunta:** Parâmetro anotado apenas com `@DisjunctionFrom` deve passar a ser documentado no OpenAPI, ou a reconstrução deve preservar a não detecção do legado?
**Impacto:** Define se `RF-OAI-07` é correção intencional ou compatibilidade legada.

**Resposta:** Corrigir detecção: incluir `DisjunctionFrom.class` e documentar filtros anotados apenas com `@DisjunctionFrom`.

---

## Pergunta 3

✅ Respondida em chat pelo usuário em 2026-05-16.

**Contexto:** `modules.jpa` — `DynamicFilterJpaRepositoryImpl.dynamicFilterResolver` nasce `null` e é preenchido pelo `DynamicFilterJpaRepositoryBeanPostProcessor`.
**Spec afetada:** [`_reversa_sdd/modules.jpa/design.md`]
**Pergunta:** Uso manual de `DynamicFilterJpaRepositoryImpl` fora da configuração Spring deve ser suportado com erro explícito, ou é fora do contrato público?
**Impacto:** Define se a reconstrução precisa validar resolver ausente nos métodos públicos ou documentar o BPP como pré-condição.

**Resposta:** Suportar erro explícito quando o resolver estiver ausente.

---

## Pergunta 4

✅ Respondida em chat pelo usuário em 2026-05-16.

**Contexto:** `modules.jpa` — testes para default methods e métodos de `Object` em proxy de `Specification` estão desabilitados com nota de reversão do PERF-006.
**Spec afetada:** [`_reversa_sdd/modules.jpa/requirements.md`]
**Pergunta:** Interfaces customizadas de `Specification` devem suportar default methods, `equals`, `hashCode` e `toString`, ou apenas `toPredicate` é contrato suportado?
**Impacto:** Define o escopo de `RF-JPA-11` e `TT-JPA-10`.

**Resposta:** O contrato suportado do proxy customizado fica limitado ao uso essencial de `Specification`, especialmente `toPredicate`.

---

## Pergunta 5

✅ Respondida em chat pelo usuário em 2026-05-16.

**Contexto:** `modules.jpa` — `FetchingFilterDecorator` cria fetch joins declarativos; não há contrato explícito sobre múltiplas bags/fetch joins com Hibernate.
**Spec afetada:** [`_reversa_sdd/modules.jpa/questions.md`]
**Pergunta:** A biblioteca deve documentar limitações de múltiplas bags/fetch joins como responsabilidade da aplicação consumidora, ou deve impor validação preventiva?
**Impacto:** Pode adicionar requisito de validação ou apenas nota operacional nos contratos JPA.

**Resposta:** Documentar a limitação como responsabilidade da aplicação consumidora.

---

## Pergunta 6

✅ Respondida em chat pelo usuário em 2026-05-16.

**Contexto:** Segurança transversal — filtros são declarados por annotations, mas não há política local para impedir filtro sobre campo sensível.
**Spec afetada:** [`_reversa_sdd/permissions.md`]
**Pergunta:** O controle de exposição de campos sensíveis deve permanecer totalmente na aplicação consumidora, ou a biblioteca deve oferecer um mecanismo próprio de allowlist/denylist?
**Impacto:** Define se a lacuna de segurança vira requisito futuro da biblioteca ou permanece fora do escopo.

**Resposta:** A biblioteca deve oferecer mecanismo próprio de allowlist/denylist para campos sensíveis.

---

## Pergunta 7

✅ Respondida em chat pelo usuário em 2026-05-16.

**Contexto:** Observabilidade transversal — o módulo não emite logs, métricas ou traces próprios.
**Spec afetada:** [`_reversa_sdd/architecture.md`]
**Pergunta:** A reconstrução deve manter observabilidade como responsabilidade da aplicação consumidora, ou a biblioteca deve expor eventos/logs/métricas próprios?
**Impacto:** Define se as lacunas de observabilidade permanecem fora do escopo ou viram requisitos não funcionais.

**Resposta:** Observabilidade permanece responsabilidade da aplicação consumidora.
