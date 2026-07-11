# Plano de Implementação — `expression-evaluator` v2

Plano de execução do módulo descrito na estratégia v2, dividido em etapas incrementais. O sequenciamento segue três princípios: (1) obter um *walking skeleton* de ponta a ponta o mais cedo possível, com semântica correta antes de qualquer otimização; (2) tratar a gramática (`ExpressionEvaluator.g4`) como contrato congelado desde a Etapa 1 — qualquer mudança nela é evento de exceção; (3) desempenho como requisito testável contínuo, com benchmarks JMH criados junto com o runtime mínimo (Etapa 5) e usados como gate de regressão a partir daí, não como fase final.

Cada etapa lista objetivo, entregas, critérios de aceite e dependências. As referências entre parênteses (ex.: §12) apontam para as seções do documento de estratégia.

---

## Marcos

| Marco | Conteúdo | Etapas |
|---|---|---|
| **M1 — Walking skeleton** | `compile → compute` funcionando para aritmética/lógica decimal, sem otimizações | 0–5 |
| **M2 — Feature-complete** | Toda a linguagem da gramática coberta: navegação, filtros, coleções, `?.`, `@` | 6 |
| **M3 — Desempenho** | Folding/CSE, nós especializados preservando semântica decimal, cache dois níveis, metas JMH atingidas | 7–9 |
| **M4 — GA** | Auditoria, diagnósticos de migração v1→v2, endurecimento e verificação diferencial | 10–12 |
| **Fase 2 (pós-GA)** | Parser Pratt, Tier 1 de compilação, fusão de pipelines de coleção | 13 |

---

## Etapa 0 — Fundação do projeto

**Objetivo:** infraestrutura de build, teste e medição pronta antes da primeira linha de produto.

**Entregas**
- Estrutura de módulos (sugestão: módulo único com pacotes `parser`, `ast`, `semantics`, `types`, `env`, `plan`, `runtime`, `cache`, `audit`, `api`; separar em módulos Maven/Gradle apenas se o Pratt da fase 2 exigir isolar a dependência do ANTLR).
- Plugin ANTLR integrado ao build gerando lexer/parser a partir do `.g4` versionado.
- CI com: testes unitários, testes de propriedade (jqwik ou similar), harness JMH executável localmente e perfil de alocação (JFR/async-profiler) preparado como job opcional.
- **Corpus de expressões**: repositório de casos reais (v1) + casos sintéticos por feature, em formato dado (arquivo por caso: fonte, ambiente, entradas, resultado esperado). Este corpus alimenta testes de todas as etapas seguintes e, depois, os testes diferenciais (§21).

**Critérios de aceite:** build reprodutível; `.g4` compila sem warnings do ANTLR; corpus inicial com ≥ 100 expressões cobrindo cada construção da gramática; pipeline de CI verde.

---

## Etapa 1 — Parsing (ANTLR configurado para velocidade)

**Objetivo:** transformar texto em parse tree com diagnósticos posicionados, já com a mecânica de desempenho de parsing da §3.1.

**Entregas**
- Fachada `ExpressionParser` encapsulando: predição em dois estágios (`SLL` + `BailErrorStrategy`, retry `LL` com error listener completo); reuso de `Lexer`/`Parser` via `ThreadLocal`/pool com `setInputStream`/`setTokenStream`; warm-up de ATN na inicialização do engine com expressões representativas do corpus.
- Error listener que converte erros do ANTLR e tokens `ERROR_CHAR` em diagnósticos com `SourceSpan` (linha/coluna/offset), código estável e mensagem — a infraestrutura de diagnóstico criada aqui é a mesma que as camadas semântica e de runtime usarão (§19).
- Suíte de testes do parser dirigida pelo corpus: casos válidos (parseiam), inválidos (falham com o span certo) e os resíduos documentados da gramática (`5!~"x"`, `:=` vs `:` em slices, `[-10:20]` válido, `[]` válido, literais `d"…"`/`t"…"`/`dt"…"` com e sem offset, precedência completa `?? < or < … < ^ < pós-fixados`).

**Critérios de aceite:** 100% do corpus válido parseia em SLL sem fallback (medir taxa de fallback); nenhum erro sem posição; teste demonstrando que a segunda compilação é significativamente mais barata que a primeira (efeito do warm-up/DFA).

**Depende de:** Etapa 0.

---

## Etapa 2 — AST semântica

**Objetivo:** a árvore imutável que é a moeda de troca entre parser, resolver e plano (§4).

**Entregas**
- Hierarquia selada de records: `ExpressionFileNode` (com `resultExpression` opcional), `AssignmentNode` (incluindo destructuring), `LiteralNode`, `IdentifierNode`, `BinaryOperationNode`, `UnaryOperationNode`, `PostfixOperationNode`, `TernaryOperationNode` (between, com flag de negação), `FunctionCallNode`, `ConditionalNode` (forma clássica e funcional na mesma AST), `VectorLiteralNode`, `NullCoalesceNode` (variádico), `PropertyChainNode` (elos com flag de navegação segura, inclusive `?.[…]`), `FilterNode`/`LambdaNode`. Todos com `NodeId` estável e `SourceSpan`.
- `SemanticAstBuilder`: visitor que consome a parse tree em **uma única passada** e a descarta. Nesta passada já ocorrem as materializações da §3.3: literais temporais viram `LocalDate`/`LocalTime`/`LocalDateTime`/`OffsetDateTime` prontos; `INT` vira `long` ou `BigInteger`; `FLOAT` vira o tipo do modo numérico; `nin` e `not in` produzem o mesmo nó; `[]` vira o singleton de lista vazia; `x%` **ainda não** é reescrito (reescritas ficam no plano, §7 deste documento — a AST espelha a fonte para auditoria fiel).
- Pretty-printer da AST (necessário para o teste de propriedade round-trip e para mensagens de diagnóstico).

**Critérios de aceite:** teste de propriedade round-trip (pretty-print → reparse → AST estruturalmente igual) verde sobre geração aleatória e sobre o corpus; nenhum nó sem `SourceSpan`; parse tree comprovadamente não retida (teste de heap ou por design — builder não guarda referência).

**Depende de:** Etapa 1.

---

## Etapa 3 — Sistema de tipos e ambiente

**Objetivo:** o vocabulário de tipos (§6) e o `ExpressionEnvironment` com seus catálogos (§8, §9), antes do resolver que os consome.

**Entregas**
- Tipos: `ScalarType` (`NUMBER`, `BOOLEAN`, `STRING`, `DATE`, `TIME`, `DATETIME`), `VectorType`/`CollectionType` (distintos, ambos com tipo de elemento), `MapType` (chave textual), `ObjectType` (nominal). Decisão implementada da §6 e da ADR 0004: a AST preserva `OffsetDateTime`; o resolver normaliza para `LocalDateTime` no fuso do ambiente, mantendo um único `DATETIME`.
- `ExpressionEnvironment` + builder imutável após construção: `MathContext`, `transcendentalMathContext`, `ZoneId` (default = fuso padrão da JVM; configurar explicitamente quando a identidade precisar ser reprodutível entre ambientes), `maxCurrentItemDepth`, `maxMaterializedSize`, `maxFactorialInput`, perfil de coerção, `ExpressionEnvironmentId` (hash canônico e estável de todo o conteúdo relevante do ambiente completo — insumo do cache da Etapa 9).
- `ExternalSymbolCatalog`: todo símbolo externo exige default não nulo e política de sobrescrita; o tipo é declarado e validado contra o default, ou inferido do default. Declarações sem default, sem política, ou com tipo desconhecido não fazem parte da v2.
- `FunctionCatalog`: descoberta por reflexão → `FunctionDescriptor` (`MethodHandle` adaptado em registro, tipos, retorno, flags `foldable`/`pure`); despacho por aridade+tipos como estrutura de consulta para o resolver. **Nesta etapa a invocação pode ser `invokeExact` simples**; `LambdaMetafactory` e inline caches ficam para a Etapa 8.
- Built-ins completos desde já no escopo de catálogo (§9): matemática **incluindo `abs` e `sqrt`** (obrigatórios — saíram da gramática), transcendentais, strings, datas/horas, comparáveis, financeiras, e as funções de asserção `asNumber/asText/asBool/asDate/asTime/asDateTime` (`pure`, `foldable`). Asserções vetoriais devem declarar elemento conhecido, como `asVectorOfNumber`, em vez de `asVector` genérico. Validação no builder: `addMathFunctions()` sem `abs`/`sqrt` presentes é erro de construção.
- `JavaTypeCatalog` (`registerJavaType`), `CollectionOperationCatalog` com operações oficiais mínimas (`map`, `sum`, `count`, `keys`, `values`, `any`, `all`) e `DataConversionService`/`RuntimeCoercionService` com a matriz de coerções de borda.

**Critérios de aceite:** dois ambientes com o mesmo conteúdo produzem o mesmo `environmentId`; catálogo resolve overloads deterministicamente; todas as funções built-in com testes unitários próprios (independentes do runtime de expressões).

**Depende de:** Etapa 0 (paralelizável com 1–2).

---

## Etapa 3.5 — Saneamento para o resolver semântico

**Objetivo:** alinhar gramática, AST, tipos, ambiente, catálogos, corpus e testes aos ADRs 0007–0013 antes de implementar o resolver.

**Entregas**
- Remoção de `UnknownType`, `NullType`, `strictMode`, `NumericMode`/`FAST`, literal fonte `null`, inteiros hexadecimais/octais e declarações de símbolo externo sem default/política do contrato público e do caminho planejável.
- Ajuste de `ExpressionEnvironmentId`: inclui símbolos, catálogos, coerção, `ZoneId`, math contexts, limites, funções e tipos Java; não inclui `strictMode` nem `NumericMode`.
- Validações de builder: defaults/overrides sem null validável; mapas sem chave/valor null validável; funções sem tipo desconhecido; função dobrável sempre pura; Java member exposto sempre com tipo mapeável; descriptors de operação de coleção válidos.
- `CollectionOperationCatalog` preparado com descriptors oficiais e seam interno para extensão futura, sem API pública de operações custom na v2 inicial.
- Atualização do corpus para `phase: semantic`, ADRs 0007–0013, `maxMaterializedSize` e nulidade estrita.

**Critérios de aceite:** `mvn -pl exp-mk3 -am test` verde; gramática rejeita `null`, `0x10` e `077`; ambiente não expõe `strictMode`/`NumericMode`; catálogos e corpus alinhados aos ADRs 0007–0013.

**Depende de:** Etapas 2 e 3.

---

## Etapa 4 — Resolver semântico

**Objetivo:** a mudança conceitual central da v2 — 100% da tipagem sai da gramática e entra aqui (§5).

**Entregas**
- `SemanticResolver` produzindo `SemanticResolutionSuccess` ou `SemanticResolutionFailure`; o `SemanticModel` só existe em sucesso e inclui AST, `resolvedTypes`, bindings de símbolos/funções/navegação/operações de coleção, fatos numéricos, nulidade de runtime, formas conhecidas, valores semânticos preparados, checagens diferidas, layout de frame e warnings.
- Inferência contextual local para literais, vetores vazios, condicionais, `??`, funções e operações de coleção, sem criar símbolos fonte implícitos e sem permitir tipo desconhecido em sucesso.
- Nulidade estrita: `?.` produz `MAY_BE_NULL`; `??` descarrega nulidade; resultado final, atribuições, operadores, funções, predicados e navegação não segura exigem `NEVER_NULL`.
- Checagens contextuais: compatibilidade de tipos por operador; `ObjectType` apenas como intermediário navegável/passável para função; regex com lado direito literal e `Pattern` pré-compilado; `root` e fatorial com fatos numéricos e checagens diferidas; programa vazio como erro semântico claro.
- Resolução de símbolos e **layout de frame** (§7): índice estável por símbolo usado, slots de `@` por profundidade simultânea máxima, resolução de destructuring para slots, política de sombreamento externo→interno (permitir com warning).
- Resolução completa de navegação, filtros, lambdas e operações de coleção; Etapa 6 apenas executa os bindings.
- Diagnósticos semânticos didáticos, incluindo família específica para `MAY_BE_NULL` escapando para contexto que exige `NEVER_NULL`.

**Critérios de aceite:** suíte cobrindo cada regra de tipagem/nulidade com caso positivo e negativo (com asserção do span e do código do diagnóstico); `@` fora de filtro/lambda é erro; regex inválida ou não literal nunca passa da compilação; `SemanticModel` de sucesso não contém placeholder/tipo inválido/binding ausente; corpus inteiro resolve sem `issues` inesperados.

**Depende de:** Etapas 2, 3 e 3.5.

---

## Etapa 5 — Runtime mínimo de ponta a ponta + API pública (M1)

**Objetivo:** fechar o ciclo `compile → plan → compute` com **semântica correta e simples** — nós executáveis genéricos, modo `DECIMAL` apenas, zero otimizações. Este é o baseline funcional e de desempenho contra o qual as etapas 7–9 serão medidas.

**Entregas**
- `ExecutionPlanBuilder` ingênuo: AST semântica → árvore de `ExecutableNode` genéricos (um `ExecutableBinaryOp` com switch é aceitável **aqui e só aqui**), `ExecutionPlan` com a forma da §10 (assignments, `resultExpression` opcional, defaults, `ExternalBindingPlan[]` ordenado, `frameSize`, `maxFilterDepth`).
- `ExecutionScope` (§13): `Object[] frame` único, sentinela `UNBOUND ≠ null`, preenchimento único no início do compute (defaults → overrides via array de bindings, coerção uma vez por valor), `currDate/currTime/currDateTime` derivados de um único `Clock.instant()` por execução.
- Semântica completa dos operadores escalares: aritmética `DECIMAL`, comparações, `and`/`or` com curto-circuito, `nand/nor/xor/xnor` (ambos os lados), `??` variádico preguiçoso, `between` ternário com curto-circuito, `in`/`not in` (avaliação linear por ora), regex com `Pattern` do binding, `%` e `!` pós-fixados, `^` associativo à direita com `2^-3`, `root`, concatenação, condicionais (as duas formas → mesmo `ExecutableConditional` linear), vetores literais, chamadas de função via handle do catálogo, atribuições em ordem + destructuring.
- API pública (§2): `ExpressionCompiler.compile(source, env)` → `CompiledExpression`; visões `asMath()`/`asLogical()`/`asAssignments()` como fachadas finas com as validações semânticas de borda; conversão do resultado só na borda (§14); nenhuma exceção como controle de fluxo no caminho quente; erros de runtime com `SourceSpan`.
- **Benchmarks JMH baseline** criados agora: `a + b * 2` em `DECIMAL`, expressão lógica típica, compilação fria. Registrar números — são a referência das etapas 7–9.

**Critérios de aceite:** corpus completo (exceto navegação/filtros, Etapa 6) executa com resultados corretos; testes de curto-circuito provando não-avaliação (funções com contador de efeitos); visões validam/rejeitam corretamente; baseline JMH registrado no repositório.

**Depende de:** Etapa 4.

---

## Etapa 6 — Navegação em objetos, subscripts, filtros e coleções (M2)

**Objetivo:** completar a linguagem — a parte com mais superfície de casos de borda (§15).

**Entregas**
- `ExecutablePropertyChain`: com metadados Java, acessores resolvidos em compilação (nesta etapa pode ser `MethodHandle` adaptado; `LambdaMetafactory`/`VarHandle` na Etapa 8); sem metadados, resolução reflexiva com memoização simples por call-site (o inline cache mono→bi→megamórfico completo também fica para a Etapa 8).
- `?.` para propriedades, métodos e todas as formas de subscript, com a semântica exata da §3.3/§15: protege **apenas** receptor `null`; erros de tipo/índice/chave/predicado continuam diagnósticos normais.
- Subscripts completos: `[i]` com índice negativo a partir do fim, `[a:b]`/`[a:]`/`[:b]`, `["key"]`, `[*]`, `.` `*` (wildcard de filho), `[?(...)]`.
- Filtros e lambdas: predicado é `ExecutableNode` comum; `@` como slot de frame com disciplina save/restore para aninhamento; enforcement de `maxFilterDepth`.
- Operações de coleção `..sum()`, `..map(@ -> e)`, `..keys()`, `..values()` etc., com loops indexados para `List`/arrays (sem streams no caminho quente); sem fusão ainda.

**Critérios de aceite:** corpus completo executa; matriz de testes de `?.` (cada forma × receptor null × erro real não mascarado); filtros aninhados até `maxFilterDepth` com valores de `@` corretos por nível; testes de coleção sobre `List`, array e `Map`.

**Depende de:** Etapa 5.

---

## Etapa 7 — Otimizações de compilação

**Objetivo:** as transformações da §11, sobre a AST semântica, no `ExecutionPlanBuilder`. A partir daqui vale a regra: **todo plano otimizado é validado por equivalência contra o plano ingênuo da Etapa 5**, que permanece disponível atrás de flag.

**Entregas, na ordem interna sugerida**
1. Constant folding completo (binários, unários, postfix, between constante, `??` com esquerda constante não-nula respeitando efeitos, funções `foldable`, vetores constantes, condicionais com condição constante, prefixos constantes de navegação). Registrar `foldedVariableReads` no plano desde já (pré-requisito da auditoria, Etapa 10).
2. Reescritas/strength reduction: `x% → x*0.01` (constante compartilhada, cadeias compostas), `root → ^(1/n)` com nó exato para raízes inteiras em `DECIMAL`, `x^2 → x*x`, `x^1/x^0` (cuidado com `0^0`), `x*1`/`x+0`/`x*0` só com operando puro, `not not x → x`.
3. Elisão de `as*` quando o tipo já foi provado (§9) — as asserções redundantes viram no-ops.
4. `in` com lado direito constante → `HashSet` ou array ordenado + busca binária, por tamanho.
5. CSE sobre subexpressões puras (slots internos sintéticos no frame).
6. Eliminação de atribuições mortas (com warning; preservadas em `asAssignments()`).
7. Reordenação segura de curto-circuito (nunca operandos com efeitos).

**Critérios de aceite:** teste de propriedade "plano otimizado ≡ plano ingênuo" (entradas aleatórias, corpus inteiro, incluindo funções com efeitos para provar que folding/reordenação os respeita); ganho mensurável no JMH sobre o baseline da Etapa 5.

**Depende de:** Etapa 6 (folding de navegação precisa dela).

---

## Etapa 8 — Especialização de nós e invocação sem reflexão

**Objetivo:** o coração do desempenho de runtime (§10, §12, §15): nós especializados, call-sites monomórficos e execução eficiente preservando a semântica decimal única da v2.

**Entregas**
- Substituição dos nós genéricos por famílias especializadas operador × tipo (`AddDecimal`, `CompareStringEQ`, `ConcatStrings2/N` com capacidade estimada, `FactorialPostfix`, `NullCoalesceN`, `Between` especializado, etc.), guiadas por `resolvedTypes`, fatos numéricos e nulidade.
- Otimizações decimais da §12: cache de constantes pequenas, evitar `stripTrailingZeros`/`setScale` intermediários, reduzir boxing/alocação onde isso não mudar o contrato público.
- Invocação de funções sem reflexão: `LambdaMetafactory` para aridades 1–4, `invokeExact` com handle pré-adaptado no resto; pré-alocação de arrays de varargs por call-site quando justificado por perfil.
- Navegação: acessores via `LambdaMetafactory`/`VarHandle` com metadados registrados; `?.` como checagem de null, sem try/catch e sem fallback reflexivo por tipo desconhecido.
- Pool opcional de `ExecutionScope` por thread — **implementar somente se o perfil de alocação justificar** (§13).

**Critérios de aceite (gates JMH da §21):** expressões decimais comuns com ganho mensurável sobre o baseline da Etapa 5; alocação decimal limitada aos valores necessários de resultado/intermediários inevitáveis; navegação com metadados ≈ getter direto + indireção constante; equivalência com o plano ingênuo mantida.

**Depende de:** Etapa 7.

---

## Etapa 9 — Cache de compilação (M3)

**Objetivo:** amortização da compilação (§17) e fechamento do marco de desempenho.

**Entregas**
- Cache Caffeine com chave `(source, environmentId)` — sem `resultType`; valor `CompiledExpression` (plano + metadados mínimos de visão/auditoria; AST e parse tree não retidas — verificar por teste de heap).
- Engine default singleton + engines isolados; `CacheConfig` (tamanho máximo, TTL opcional, weigher por número de nós).
- Contador de execuções por entrada (insumo do Tier 1 futuro).
- Medição separada de compilação fria × quente no JMH; warm-up de ATN da Etapa 1 revisitado com o corpus final.

**Critérios de aceite:** mesmo texto usado como `asMath()` e `asLogical()` comprovadamente compartilha um único plano; hit de cache na ordem do custo de um lookup + validação de visão; ausência de retenção de AST/parse tree confirmada.

**Depende de:** Etapa 5 (funcional) e 8 (números finais).

---

## Etapa 10 — Auditoria com custo zero quando desligada

**Objetivo:** §18 — explicabilidade sem tocar o caminho quente.

**Entregas**
- Plano instrumentado construído lazy na primeira chamada de `computeWithAudit`, cacheado ao lado do plano normal; decoradores sobre `ExecutableNode` emitindo eventos (leituras, valores dinâmicos, chamadas, atribuições) em ring buffer pré-alocado com `maxAuditEvents`.
- Integração dos `foldedVariableReads` (Etapa 7) para explicar valores dobrados.
- `AuditResult<T>` com resultado + `ExpressionAuditTrace`.

**Critérios de aceite:** JMH provando que o plano normal não regrediu (nenhum branch de auditoria nele); trace explica corretamente expressões com folding agressivo; ring buffer limita memória sob expressões grandes.

**Depende de:** Etapas 7–9.

---

## Etapa 11 — Migração v1 → v2

**Objetivo:** a tabela da §20 como produto: cada quebra de linguagem vira diagnóstico guiado, mais o migrador opcional.

**Entregas**
- Diagnósticos dedicados no parser/resolver: `=` + `;` → "atribuição agora usa `:=`"; `!` + `=` → "use `<>`"; `|` isolado / `||` inesperado → "módulo agora é `abs(x)`"; `INT-INT-INT` com forma de data → sugerir `d"…"`; `INT:INT` fora de subscript → sugerir `t"…"`; `<tipo>(…)` → mensagem sobre hints removidos com as duas alternativas.
- Migrador de fonte (ferramenta separada): reescrita textual + validação por recompilação; relatório por expressão (migrada / precisa de revisão manual); execução em lote sobre base v1.
- Testes diferenciais v1 × v2 sobre o corpus real: para expressões semanticamente inalteradas, mesmos resultados; para as alteradas, migração + equivalência pós-migração.

**Critérios de aceite:** cada linha da tabela da §20 com teste de diagnóstico próprio; migrador converte o corpus v1 real com taxa de intervenção manual conhecida e documentada.

**Depende de:** Etapas 1–6 (diagnósticos), 9 (migrador valida por compilação).

---

## Etapa 12 — Endurecimento, verificação e release (M4)

**Objetivo:** fechar §19 e §21 como estado permanente, não como esforço pontual.

**Entregas**
- Revisão de exaustividade dos diagnósticos: todo erro com `SourceSpan`, código estável e sugestão quando aplicável; erros semânticos sempre acumulados.
- Guard-rails multi-tenant sob teste de estresse: `maxCurrentItemDepth`, `maxMaterializedSize`, `maxFactorialInput`, expressões patológicas (aninhamento profundo, vetores enormes, regex custosas) sem degradar o processo.
- Testes de concorrência: plano compartilhado entre threads com escopos isolados; pool de parser sob contenção.
- Consolidação dos gates de CI: JMH com limiares, perfil de alocação como gate, property-based e diferenciais no pipeline.
- Documentação: referência da linguagem (derivada do `.g4` + semântica do resolver), guia de API, guia de migração, tabela de precedência, resíduos documentados.

**Critérios de aceite:** todos os gates verdes por N execuções consecutivas de CI; documentação revisada; versão GA taggeada.

**Depende de:** todas as anteriores.

---

## Etapa 13 — Fase 2 (pós-GA, opcional e independente entre si)

Três trilhas já previstas na estratégia, cada uma ativável por demanda medida:

1. **Parser Pratt artesanal** (§3.2): tabela de binding powers derivada da cadeia única de precedência; o `.g4` permanece como especificação executável com testes diferenciais ANTLR × Pratt sobre o corpus antes de qualquer troca no caminho quente.
2. **Tier 1 de compilação** (§10): promoção dos planos mais quentes (pelo contador da Etapa 9) para lambda composta via `LambdaMetafactory`/`MethodHandle` ou bytecode com `defineHiddenClass`; Tier 0 permanece default e fallback.
3. **Fusão de pipelines de coleção** (§11.6): `v..map(@ -> f)..sum()` em loop único, validada por equivalência contra a forma não fundida.

---

## Mapa de dependências (resumo)

```text
E0 ─┬─ E1 ── E2 ─┐
    └─ E3 ───────┴─ E3.5 ── E4 ── E5 (M1) ── E6 (M2) ── E7 ── E8 ── E9 (M3)
                                                  │            │
                                     E11 ◄────────┘      E10 ◄─┘
                                                  │
                                            E12 (M4) ── E13 (fase 2)
```

Paralelismos úteis: E3 corre em paralelo com E1–E2; os built-ins do catálogo (E3) e o corpus (E0) podem ser expandidos continuamente por uma pessoa dedicada; E3.5 bloqueia E4 porque remove conceitos obsoletos do contrato planejável; os diagnósticos de migração (E11) podem começar assim que parser e resolver estabilizarem, sem esperar o desempenho.

## Riscos principais e mitigações embutidas no plano

- **Otimização quebrar semântica** → plano ingênuo da E5 mantido atrás de flag como oráculo permanente; equivalência por property-based em E7/E8.
- **Metas de desempenho descobertas tarde** → JMH baseline nasce na E5 e vira gate; nada de "fase de otimização" descolada da medição.
- **Explosão de casos em navegação/`?.`** → E6 isolada como a etapa de maior superfície de teste, antes de qualquer especialização, para que E8 otimize comportamento já provado.
- **Cache reter memória (AST/parse tree)** → critério de aceite explícito de não-retenção nas E2 e E9.
- **Migração v1 subestimada** → corpus real v1 entra na E0 e os diferenciais v1×v2 são critério de aceite da E11, não tarefa de rodapé.
