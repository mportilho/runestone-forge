# Estratégia — `expression-evaluator` v2

> **Documento histórico e não normativo.** As decisões vigentes estão no plano de implementação, nos ADRs e em `exp-mk3/docs/planning/etapa-4/etapa-4-resolver-semantico.md`. Em particular, referências abaixo a hex/octal, símbolos implícitos, `UnknownType`, `NullType`, strict mode, `NumericMode`/`FAST`, `maxFilterDepth` e `maxVectorSize` foram substituídas pelos ADRs 0007–0014.

Nova estratégia de construção do analisador/executor de expressões, alinhada à gramática revisada (`ExpressionEvaluator.g4`, cadeia de precedência unificada, literais temporais prefixados, `:=` para atribuição, **sem type hints sintáticos**) e reorganizada com foco em **altíssimo desempenho de execução**, mantendo a arquitetura em camadas que provou seu valor na versão anterior.

---

## 1. Princípio norteador

A expressão é compilada **uma única vez** em um plano imutável e thread-safe, e executada **milhões de vezes**. Portanto:

- O custo de compilação é amortizado pelo cache — pode ser "caro" se comprar desempenho de runtime.
- O caminho quente é `compute(...)`: a meta é **zero alocação em regime estacionário** para expressões puramente numéricas/lógicas, e alocação mínima nos demais casos.
- Toda decisão possível é tomada em compilação: binding de símbolos, resolução de função, compilação de regex, dobra de constantes, especialização de operadores por tipo.

Pipeline (inalterado em espírito, refinado em cada etapa):

```text
texto da expressão
  -> parser (gramática unificada)
  -> AST interna
  -> resolução semântica (agora responsável por TODA a tipagem)
  -> plano de execução especializado
  -> avaliação com escopo isolado por chamada
```

---

## 2. API pública — compilação única, visões tipadas

A gramática nova tem **um único ponto de entrada** (`start: assignmentExpression* expression? EOF`). Não existem mais `mathStart` / `logicalStart` / `assignmentStart`. A consequência arquitetural correta é: **compilar uma vez e tipar na borda**.

```java
ExpressionEngine engine = ExpressionEngine.defaultEngine();
CompiledExpression ce = engine.compileOrThrow("a + b * 2", env);

MathExpression math = ce.asMath();          // valida: resultado compatível com NUMBER
LogicalExpression log = ce.asLogical();     // valida: resultado compatível com BOOLEAN
AssignmentsExpression asg = ce.asAssignments(); // valida: há atribuições

BigDecimal r = math.compute(Map.of("a", 10, "b", 5));
```

Regras de validação das visões (checagens semânticas, não gramaticais):

- `asMath()` — exige `resultExpression` presente e tipo resolvido coercível a `NUMBER`.
- `asLogical()` — exige `resultExpression` presente e tipo coercível a `BOOLEAN`.
- `asAssignments()` — exige ao menos uma atribuição; `resultExpression` opcional.
- Programa vazio (nenhuma atribuição e nenhuma expressão — permitido pela gramática) é **erro semântico** com mensagem clara, não erro de parse.

As classes `MathExpression`, `LogicalExpression` e `AssignmentExpression` continuam existindo como fachadas finas sobre o mesmo `ExecutionPlan`. Isso simplifica o cache (seção 17): a chave deixa de incluir `resultType`.

---

## 3. Parsing

### 3.1 ANTLR como referência, configurado para velocidade

- **Predição em dois estágios**: primeiro `PredictionMode.SLL` com `BailErrorStrategy`; em falha, retry com `PredictionMode.LL` e error listener completo. Para a esmagadora maioria das expressões, SLL basta e é muito mais barato.
- A gramática nova já eliminou o pior custo de predição da antiga: com `:=` para atribuição e `=` exclusivo para igualdade, **não há mais lookahead até `;`** para distinguir atribuição de comparação. `IDENTIFIER ASSIGN` decide em 2 tokens.
- **Warm-up do ATN**: compilar algumas expressões representativas na inicialização do engine popula os DFAs compartilhados do ANTLR e remove o pico de latência da primeira compilação.
- **Reuso de instâncias**: `Lexer`/`Parser` reutilizados via pool ou `ThreadLocal` (com `setInputStream`/`setTokenStream`), preservando os DFAs.
- Parse tree consumida em **uma única passada** pelo `SemanticAstBuilder` e descartada imediatamente — nunca retida no plano nem no cache.
- `ERROR_CHAR` e o error listener alimentam diagnósticos com `SourceSpan`; nenhuma exceção de parsing escapa sem posição.

### 3.2 Trilha futura: parser Pratt artesanal (opcional)

A unificação da precedência em uma cadeia única (`?? < or < and < comparação < nand/nor/xor/xnor < || < +- < */mod < unário < root < ^ < pós-fixados < primário`) torna a linguagem **ideal para um parser Pratt dirigido por tabela** (binding powers por token). Isso permite, numa fase 2:

- Substituir o ANTLR no caminho quente de compilação por um recursive-descent/Pratt escrito à mão, ~10–50x mais rápido e sem dependência em runtime.
- Manter o `.g4` como **especificação executável**: testes diferenciais parseiam um corpus grande com ambos e comparam as ASTs.

Como a compilação é cacheada, essa troca é otimização de segunda ordem — o runtime vem primeiro.

### 3.3 Ajustes exigidos pela nova gramática

- **Literais temporais prefixados** (`d"..."`, `t"..."`, `dt"..."`): o lexer já valida a estrutura. O `SemanticAstBuilder` converte o token diretamente para `LocalDate` / `LocalTime` / `LocalDateTime` ou `OffsetDateTime` (quando há offset), de modo que o nó nasce **como constante já materializada** — sem parse de data em runtime, jamais.
- **`INT` vs `FLOAT`**: `INT` (inclui hex/octal) vira `long` quando couber, senão `BigInteger`; `FLOAT` vira `BigDecimal`/`double` conforme o modo numérico (seção 12). Índices/slices já são `INT` por gramática; hex/octal em subscript (`[0x1F]`) é aceito pelo parser e **rejeitado (ou aceito, decisão de produto) no resolver semântico**, com mensagem específica — mantendo o lexer simples.
- **`sqrt` e `abs` são funções de runtime**: precisam constar no catálogo built-in desde o dia 1 (a v1 as tinha na gramática; removê-las de lá sem tê-las no catálogo quebraria expressões existentes).
- **`??` é operador geral encadeável**: um único nó variádico com avaliação preguiçosa da esquerda para a direita (seção 10).
- **Filtros reutilizam `expression`**: some a mini-gramática de filtros; a validade de `@` vira checagem semântica contextual (seção 5).
- **`in` / `not in` / `nin`**: `nin` e `not in` produzem o **mesmo nó de AST** (sinônimos), evitando duplicação a jusante.
- **Vetor vazio `[]`** é literal válido → constante dobrada para lista imutável vazia compartilhada.
- **Subscript seguro `?.[...]`**: reutiliza `SAFE_NAV` antes de qualquer forma de subscript (`?.[i]`, `?.[a:b]`, `?.["key"]`, `?.[*]`, `?.[?(...)]`). A semântica protege apenas receptor `null`; erros de tipo, índice, chave ou predicado continuam diagnósticos normais.

---

## 4. AST

Estrutura mantida, com pequenos ajustes:

- `Node` (interface raiz **selada**), `ExpressionFileNode`, `ExpressionNode`, `AssignmentNode`, `NodeId`, `SourceSpan`.
- `ExpressionFileNode { List<AssignmentNode> assignments; ExpressionNode resultExpression; /* pode ser null */ }` — `resultExpression` agora é opcional, refletindo `expression?` na regra `start`.
- Nós: `LiteralNode`, `IdentifierNode`, `BinaryOperationNode`, `UnaryOperationNode`, `PostfixOperationNode`, `FunctionCallNode`, `ConditionalNode` (cobre `if/then/elsif/else/endif` e a forma funcional `if(c; a; b)` — mesma AST), `VectorLiteralNode`, `PropertyChainNode`, `NullCoalesceNode` (variádico), `FilterNode`/`LambdaNode` (corpo de `[?(...)]` e `@ -> ...`). Elos de `PropertyChainNode` carregam flag de navegação segura quando aplicável, inclusive em subscript seguro (`?.[...]`). Não existe mais `TypeHintNode`: os hints saíram da gramática (seção 5.1) e as funções de asserção `asNumber(x)` etc. são `FunctionCallNode` comuns.
- `TernaryOperationNode` da v1 (usado por `between`) permanece: `x between a and b` vira nó ternário; `not between` é o mesmo nó com flag de negação (idem `not in`).
- Todos os nós são **records imutáveis** com `NodeId` estável e `SourceSpan` — pré-requisito para CSE (seção 11) e auditoria.

---

## 5. Modelo semântico

O `SemanticResolver` ganha responsabilidades que antes eram (mal) distribuídas pela gramática tipada. É a mudança conceitual central da v2: **a gramática aceita mais, a semântica valida tudo**.

Além do que a v1 já fazia (símbolos, tipos, overloads, propriedades, métodos), o resolver agora verifica:

1. **Compatibilidade de tipos de operadores** — `"a" * 2`, `d"2021-01-02" and true` etc. são erros semânticos com span, já que a gramática unificada os aceita sintaticamente.
2. **Contexto de `@`** — `@` só é válido dentro de `[?(...)]` e de lambdas `@ -> ...`. O resolver mantém uma pilha de contexto de filtro/lambda durante a travessia; `@` fora dela é erro.
3. **Comparações não encadeáveis** — a gramática já garante (um operador por nível), mas o resolver produz mensagem didática quando detecta o padrão `(a < b) < c` com tipos incoerentes.
4. **Resíduo documentado da gramática**, transformado em diagnóstico de qualidade:
  - `5!~"x"` lexa como `5 !~ "x"` (maximal munch de `REGEX_NOT_MATCH`); se o operando esquerdo de regex não for `STRING`, a mensagem sugere: "para fatorial seguido de `~`, insira espaço: `5! ~ ...`".
  - (A antiga colisão do type hint `a<bool>c` deixou de existir: com os hints fora da gramática, `<` volta a ser inequívoco — ver 5.1.)
5. **Hex/octal em subscripts** — política única, aplicada aqui (recomendação: rejeitar com sugestão do decimal equivalente).
6. **Regex sempre literal à direita** (garantido pela gramática) → o resolver valida a sintaxe do padrão **em compilação** e o `Pattern` compilado é anexado ao binding do nó. Regex inválida nunca chega ao runtime.
7. **`ROOT` e `EXPONENTIATION`** — tipagem numérica estrita; define-se aqui a semântica de `n root x` = `x^(1/n)` com tratamento exato para raízes inteiras de potências perfeitas quando em modo `DECIMAL`.

`SemanticModel` mantém a mesma forma:

```java
SemanticModel(
        ExpressionFileNode ast,
        Map<NodeId, ResolvedType> resolvedTypes,
        Map<NodeId, SymbolRef> symbolByNodeId,
        Map<String, SymbolRef> externalSymbolsByName,
        Map<String, SymbolRef> internalSymbolsByName,
        Map<NodeId, ResolvedFunctionBinding> functionBindings,
        List<SemanticIssue> issues
)
```

com um acréscimo: `Map<NodeId, NumericKind> numericKinds` (seção 12).

### 5.1 Type hints removidos — onde a validação de tipos passa a viver

Os hints sintáticos (`<bool>(x)`, `<number>(x)`, ...) saem da gramática e da estratégia. Avaliação de onde a responsabilidade deve ficar:

| Camada candidata | Veredito | Motivo |
|---|---|---|
| Parser | **Não** | O parser não conhece (nem deve conhecer) tipos — foi exatamente essa premissa errada que gerou as oito famílias tipadas da v1 e os hints como muleta. Com a cadeia unificada, o hint perdeu sua razão de existir. |
| **Modelo semântico** | **Sim — lugar principal** | É a única camada que enxerga tudo ao mesmo tempo: literais, assinaturas do `FunctionCatalog`, tipos declarados no `ExternalSymbolCatalog`, metadados Java de navegação e a estrutura completa da AST. Validação acontece **uma vez por compilação**, com `SourceSpan` e mensagens boas — e é ela que habilita a especialização de nós (seção 10). |
| Runtime | **Só o irredutível** | Pagar validação por execução contraria a meta de desempenho. O runtime fica apenas com o que é logicamente impossível provar em compilação (valores `Unknown` vindos de fora), tratado **nas bordas**, uma vez por valor, e com inline caches na navegação. |

O que substitui cada uso legítimo do hint:

1. **Declarar o tipo de um símbolo externo** (uso dominante na prática): `registerExternalSymbol("flag", ScalarType.BOOLEAN)` — declaração de tipo **sem** default passa a ser suportada pelo `ExternalSymbolCatalog` (seção 8). O resolver usa a declaração na inferência; o valor passado em `compute` é coerido/validado uma única vez no binding do frame.
2. **Afirmar um tipo pontualmente no meio da expressão**: funções de runtime `asNumber(x)`, `asText(x)`, `asBool(x)`, `asDate(x)`, `asTime(x)`, `asDateTime(x)`, `asVector(x)` (seção 9). O resolver conhece o tipo de retorno dessas funções e **refina a inferência a jusante exatamente como o hint fazia** — mesma expressividade, sem sintaxe dedicada, sem `TypeHintNode`, e componível (é uma função como outra qualquer: dobrável quando o argumento é constante, utilizável em filtros, lambdas etc.).
3. **Inferência bidirecional simples** cobre boa parte do resto: `x + 1` fixa `x: NUMBER`; `if x then ...` fixa `x: BOOLEAN`; `x || "s"` fixa `x: STRING`. Símbolos externos sem declaração e sem uso restritivo permanecem `UnknownType` — compilam, e a checagem migra para a borda de runtime (política da v1, preservada). Um **modo estrito** opcional no ambiente transforma `UnknownType` residual em erro de compilação, para quem quer tudo estático.

Ganhos colaterais da remoção: some a armadilha léxica `a<bool>c` (o token `<bool>` roubava uma comparação por maximal munch), `<` volta a ter um único papel no lexer, `primaryExpression` perde uma alternativa e a AST perde um tipo de nó.

---

## 6. Sistema de tipos

Mantido: `ScalarType` (`NUMBER`, `BOOLEAN`, `STRING`, `DATE`, `TIME`, `DATETIME`), `VectorType`, `CollectionType`, `MapType`, `ObjectType` (classe Java registrada no `JavaTypeCatalog`), `UnknownType`, `NullType`.

Acréscimos:

- **`DATETIME` com offset**: o literal `dt"...+02:00"` produz `OffsetDateTime`. Decisão de projeto: normalizar para `LocalDateTime` no fuso configurado no ambiente, ou introduzir `ZONED_DATETIME` como subtipo. Recomendação: normalizar na borda (conversão em compilação, já que o offset está no literal) e manter um único tipo `DATETIME` — menos combinações no runtime.
- **`NumericKind`** ortogonal a `NUMBER`: `LONG`, `DOUBLE`, `DECIMAL` — usado só para especialização de plano, invisível na API.
- Inferência de elemento em vetores literais homogêneos (`[1,2,3]` → `CollectionType(NUMBER)`), melhorando resolução de `..sum()` e afins.

---

## 7. Símbolos e frames

Mantido e endurecido:

- `SymbolRef` com `INTERNAL`/`EXTERNAL`; cada símbolo recebe **índice estável** em compilação.
- O plano define um **layout de frame** fixo: `[internos... | externos... | slots de @ para filtros aninhados...]`. Slots de `@` são reservados por profundidade máxima de aninhamento de filtros/lambdas, calculada em compilação — `@` vira acesso a array, não thread-local nem lookup.
- Atribuições com destructuring (`[a, b] := expr;`) resolvem cada alvo para seu slot em compilação; o runtime só indexa.
- Sombras e reatribuição de símbolo externo por atribuição interna: política explícita (recomendação: permitir, com warning semântico), resolvida em compilação para um único slot.

---

## 8. Ambiente

`ExpressionEnvironment` mantido: `FunctionCatalog`, `ExternalSymbolCatalog`, `JavaTypeCatalog` (ex-`TypeHintCatalog`, renomeado: são **metadados de classes Java para navegação em objetos**, via `registerJavaType(Customer.class)` — nada a ver com os hints sintáticos removidos), `DataConversionService`, `MathContext`, `transcendentalMathContext` e `environmentId` como UUID textual opaco por instância para o cache.

Acréscimos:

- **Declaração de tipo de símbolo externo sem default**: `registerExternalSymbol("flag", ScalarType.BOOLEAN)` — substitui o uso dominante dos antigos hints (seção 5.1) e melhora a inferência/especialização.
- **Modo estrito** opcional: `UnknownType` residual vira erro de compilação.
- **`NumericMode`**: `DECIMAL` (default, `BigDecimal`, compatível com v1) ou `FAST` (long/double com promoção controlada — seção 12).
- **`maxFilterDepth`** e **`maxVectorSize`** opcionais (guard-rails para uso multi-tenant).
- Builder valida na construção que `abs`, `sqrt` e demais funções antes-gramaticais estão presentes quando `addAllFunctions()`/`addMathFunctions()` é usado.

---

## 9. Catálogo de funções

Mantida a descoberta por reflexão → `FunctionDescriptor` com `MethodHandle`, tipos, retorno e flag `foldable`. Melhorias de desempenho:

- **Invocação sem reflexão em runtime**: cada binding resolvido gera um invoker especializado. Preferência: `LambdaMetafactory` para assinaturas comuns (1–4 args), caindo para `MethodHandle.invokeExact` com handle **adaptado em compilação** (asType feito uma vez). Nunca `Method.invoke` no caminho quente.
- **Despacho por aridade + tipos resolvido em compilação** — overload nunca é decidido em runtime; quando os tipos são `Unknown`, o binding vira um *inline cache* por call-site (seção 15).
- **Varargs materializados uma vez**: o plano pré-aloca o array de argumentos por call-site quando os tamanhos são fixos (com cópia por execução apenas se a função puder reter o array).
- `foldable` mantido e usado agressivamente pelo folding (seção 11). Funções puras adicionais anotadas como `pure` habilitam CSE.
- Built-ins: matemática (incluindo `abs`, `sqrt`), logaritmos, trigonometria (usando `transcendentalMathContext`), strings, datas/horas, comparáveis, financeiras estilo Excel — inalterado no escopo.
- **Funções de asserção de tipo** `asNumber`, `asText`, `asBool`, `asDate`, `asTime`, `asDateTime`, `asVector`: substituem os hints sintáticos removidos (seção 5.1). São `pure` e `foldable`; o resolver usa seu tipo de retorno para refinar a inferência a jusante; em runtime, quando o tipo do argumento já foi provado em compilação, o `ExecutionPlanBuilder` **elide a chamada por completo** (viram no-ops) — asserção redundante custa zero.

---

## 10. Plano de execução

`ExecutionPlan` mantém a forma geral:

```java
ExecutionPlan(
        List<ExecutableAssignment> assignments,
        ExecutableNode resultExpression,        // pode ser null (script só de atribuições)
        Object[] defaults,
        ExternalBindingPlan[] externalBindings, // array ordenado, não Map
        int frameSize,                          // internos + externos + slots de @
        int maxFilterDepth,
        AuditPlan auditPlan                     // lazy, ver seção 18
)
```

### Nós executáveis — especialização é a regra

O runtime executa uma segunda árvore (`ExecutableNode`), como na v1, mas com um princípio novo: **um tipo de nó por combinação relevante de operador × tipo**, para que os call-sites do JIT sejam monomórficos.

Exemplos:

- `AddDecimal`, `AddLong`, `AddDouble`, `SubDecimal`, `MulLong`, ... em vez de um `ExecutableBinaryOp` genérico que despacha por `switch` em runtime.
- `CompareDecimalLT`, `CompareStringEQ`, `CompareDateLE`, ...
- `ConcatStrings2`, `ConcatStringsN` (com estimativa de capacidade do `StringBuilder` calculada em compilação a partir dos operandos).
- `NullCoalesceN` variádico, curto-circuito no primeiro não-nulo (novo `??` geral encadeável).
- `AndShortCircuit` / `OrShortCircuit`; `NandN`/`NorN`/`XorN`/`XnorN` para a camada bitwise-lógica.
- `PercentPostfix` **não existe**: `x%` é reescrito em compilação como `x * 0.01` (constante compartilhada); cadeias `x%%` compõem a constante. `FactorialPostfix` usa tabela memoizada para `n ≤ 20` (long) e `BigInteger` acima, com limite configurável.
- `ExponentRight` preserva associatividade à direita e o caso `2^-3` (expoente é `unaryExpression` por gramática).
- `RootChain` reescrito em compilação para exponenciação (`x^(1/n)`), com nó exato para raízes inteiras em modo `DECIMAL`.
- `RegexMatch`/`RegexNotMatch` carregam `Pattern` pré-compilado (garantido: a gramática só aceita `STRING` literal à direita).
- `Between` (e negado) como nó ternário com curto-circuito (`x >= a` falso → não avalia `b`).
- `InVector` especializado: quando o lado direito é vetor **constante**, compila para lookup em `HashSet`/array ordenado + busca binária, escolhido pelo tamanho; `null in v` funciona (null é primário comum na nova gramática).
- `Conditional` avalia apenas o ramo necessário; cadeias `elsif` viram lista linear, não árvore aninhada.

### Trilha futura: tier de compilação

Se ativado por demanda medida, um **Tier 1** opcional introduz e valida sua propria politica de observacao dos planos quentes antes de compor a árvore executável em uma única lambda via `LambdaMetafactory`/composição de `MethodHandle`s, ou gerar bytecode com `Lookup.defineHiddenClass` — eliminando o overhead de travessia. O Tier 0 (árvore especializada) permanece o padrão e o fallback; nenhuma contagem e antecipada no caminho quente enquanto o Tier 1 nao existir.

---

## 11. Otimizações de compilação

Mantidas da v1 e ampliadas. Todas rodam no `ExecutionPlanBuilder`, sobre a AST semântica:

1. **Constant folding** (v1, mantido): binários, unários, postfix, `between` constante, `??` com esquerda constante não-nula, funções `foldable` com args constantes, vetores constantes, condicionais com condição constante, prefixos constantes de navegação, regex → `Pattern`. Novo: literais temporais **nascem dobrados** (seção 3.3), e `[]` vazio é singleton.
2. **Simplificações algébricas / strength reduction**: `x^2 → x*x`, `x^1 → x`, `x^0 → 1` (com cuidado semântico para `0^0`), `x*1`, `x+0`, `x*0` (atenção a efeitos de curto-circuito ausentes — só quando o operando é puro), `not not x → x`, `x% → x*0.01`.
3. **CSE (common subexpression elimination)**: subexpressões puras idênticas (mesma estrutura + mesmos símbolos) são computadas uma vez por execução em slot interno sintético. Habilitado pela imutabilidade dos nós e pela flag `pure` das funções.
4. **Eliminação de atribuições mortas**: atribuição interna nunca lida a jusante gera warning e é removida do plano (preservada apenas em `AssignmentExpression`, cujo contrato é devolver o `Map` completo).
5. **Reordenação segura de curto-circuito**: em `and`/`or`, operandos constantes ou baratos e puros podem ser antecipados (nunca reordenar operandos com efeitos, i.e., chamadas não-puras).
6. **Fusão de pipeline de coleção** (opcional, fase 2): `v..map(@ -> f)..sum()` funde em um único loop sem lista intermediária.

Toda otimização registra no plano os `foldedVariableReads`/eventos necessários para que a **auditoria continue explicável** mesmo com nós dobrados (compatível com o conceito da v1).

---

## 12. Representação numérica de alto desempenho

`BigDecimal` é o maior custo do runtime da v1 (alocação + aritmética). Estratégia em dois modos, decidida por ambiente:

- **`DECIMAL` (default)** — semântica idêntica à v1 (`BigDecimal` + `MathContext`). Otimizações mesmo aqui:
  - cache de constantes pequenas (`ZERO`, `ONE`, `0.01`, inteiros −128..127 e potências de 10 comuns);
  - reuso do `MathContext` sem revalidação;
  - evitar `stripTrailingZeros`/`setScale` intermediários — normalização só na borda de saída.
- **`FAST` (opt-in)** — o `SemanticResolver` anota cada nó numérico com `NumericKind`:
  - `LONG` quando todos os insumos são inteiros e as operações fecham em inteiros (`+ - * mod !`), com **checagem de overflow** (`Math.addExact` etc.) e promoção automática do nó para `DECIMAL` em overflow (fallback estrutural: o plano contém o nó lento como irmão);
  - `DOUBLE` para `/ ^ root %` e funções transcendentais;
  - `DECIMAL` quando o usuário passa `BigDecimal` externo ou exige precisão.
    Em `FAST`, expressões numéricas puras executam **sem boxing** via caminho `computeAsLong/computeAsDouble` nos nós especializados; o boxing acontece apenas no retorno da API.

A conversão para o tipo de retorno público (`BigDecimal` em `MathExpression`) continua na borda, como na v1 ("coerção apenas nas bordas").

---

## 13. ExecutionScope

Mantido o desenho vencedor da v1, com refinamentos:

- **Um único `Object[] frame`** com o layout da seção 7 (em `FAST`, acompanhado de `long[]`/`double[]` paralelos para slots numéricos primitivos).
- Sentinela `UNBOUND` distinto de `null` — preservado exatamente como na v1 (`UNBOUND` = "não há valor"; `null` = "há valor e é nulo").
- Camadas *defaults externos → overrides externos → internos* resolvidas por **preenchimento único no início do compute** (copiar defaults, aplicar overrides via `ExternalBindingPlan[]`), eliminando a busca em três camadas por leitura: cada leitura é um único acesso indexado.
- `currDate`/`currTime`/`currDateTime` cacheados por escopo (mesmo valor dentro da mesma execução), derivados de um único `Clock.instant()` por compute — coerência entre os três.
- Slots de `@` com disciplina save/restore para filtros aninhados (pilha implícita nos slots reservados).
- Escopos são baratos; opcionalmente, pool por thread para `frameSize` grandes (medido antes de adotar).

Binding externo no `compute(Map)`: itera-se o **array de bindings do plano** (não o `Map` do usuário), buscando cada nome uma vez e aplicando a coerção uma vez. Chaves extras no `Map` do usuário são ignoradas (ou warning em modo estrito). Complexidade O(externos), ordem determinística.

---

## 14. Runtime e coerção

Fluxo mantido:

```text
cria ExecutionScope
  -> preenche frame (defaults + overrides coeridos)
  -> executa assignments em ordem
  -> avalia resultExpression (se houver)
  -> converte resultado para o tipo da visão (Math/Logical)
```

- `MathEvaluator` exige `BigDecimal` (ou converte de long/double em `FAST`); `LogicalEvaluator` exige `Boolean`; `AssignmentExpression` materializa o `Map` de internos ao final (nomes → valores do frame).
- `RuntimeServices`/`RuntimeCoercionService` mantidos, com coerções **somente nas bordas**: entrada externa, chamada de função, retorno de propriedade/método, resultado final. Nenhuma coerção especulativa entre nós — a especialização de tipos do plano a torna desnecessária.
- Nenhuma exceção usada para controle de fluxo no caminho quente; erros de runtime carregam `SourceSpan` do nó de origem.

---

## 15. Navegação em objetos

> **Superseded em parte.** O ADR 0010 (Tipagem Conhecida) eliminou `UnknownType`: não existe elo sem metadados, e o inline cache reflexivo descrito abaixo não faz parte da v2 — tipo ou membro desconhecido é erro semântico. A Etapa 6 decidiu também que **navegação por campo público está fora da v2** (a exposição é componente de record, getter bean e método registrado) e que subscripts seguem o ADR 0018. Os demais pontos permanecem válidos.

`PropertyChainNode` → `ExecutablePropertyChain`, com upgrade de mecânica:

- **Com metadados Java** (`registerJavaType`): cada elo da cadeia resolve em compilação para um acessor compilado — record component/getter via `LambdaMetafactory` ou `VarHandle`; método via `MethodHandle` adaptado. Zero reflexão em runtime.
- ~~**Sem metadados** (`UnknownType`): inline cache polimórfico por call-site.~~ Removido pelo ADR 0010.
- `?.` (`SAFE_NAV`) compila para checagem de null antes do elo — sem try/catch. Cobre propriedades (`obj?.prop`), métodos (`obj?.method()`) e subscripts (`obj?.[i]`, `obj?.[a:b]`, `obj?.[?(...)]`).
- Subscripts: `[i]` (com índice negativo = a partir do fim, agora natural pois `[-10:20]` lexa corretamente na nova gramática), slices `[a:b]`/`[a:]`/`[:b]`, `["key"]`, `[*]`, `[?(...)]`. A forma segura `?.[...]` retorna `null` apenas quando o receptor do elo é `null`; não mascara coleção não indexável, índice fora do intervalo, chave inválida ou erro no predicado.
- Filtros `[?( ... )]`: o predicado é um `ExecutableNode` comum (a gramática nova reusa `expression`), com `@` ligado ao slot reservado. Sem materialização quando seguido de agregação fundível.
- Operações de coleção `..sum()`, `..map(@ -> e)`, `..keys()`, `..values()` etc.: implementações especializadas por tipo de coleção resolvido; loops indexados para `List`/arrays; sem streams no caminho quente.

---

## 16. Condicionais, `if` funcional e curto-circuito

- `if ... then ... elsif ... else ... endif` e `if(c; a; b; ...)` compilam para o mesmo `ExecutableConditional` linear; apenas o ramo escolhido é avaliado.
- `and`/`or` sempre curto-circuito; `nand/nor/xor/xnor` avaliam ambos os lados (semântica definida e documentada).
- `??` avalia operandos até o primeiro não-nulo; com esquerda constante não-nula, o folding elimina o resto **exceto** quando os descartados têm efeitos (funções não-puras) — nesse caso mantém-se avaliação preguiçosa correta (não avaliados = não executados, coerente com curto-circuito).

---

## 17. Cache — por engine, uma chave menor

Com a compilação unificada, o cache melhora estruturalmente:

- **Chave**: `(source, environmentId)` com igualdade textual exata, sem normalizacao ou hash exclusivo — `resultType` sai da chave (era necessário na v1 porque cada tipo tinha um parse diferente). O compartilhamento ocorre apenas ao reutilizar a mesma instância de ambiente; dentro dela, o mesmo texto usado como `MathExpression` e como `LogicalExpression` compartilha **um único plano** enquanto a geracao esta residente, e as visões só validam.
- **Valor**: `ExpressionCompilationResult` completo. Sucesso retém a `CompiledExpression` (plano + metadados semânticos mínimos para as validações de visão e para auditoria); falhas sintáticas e semânticas determinísticas também são cacheadas, sem cache negativo separado. AST e parse tree **não** são retidas.
- Um unico Caffeine por engine: `ExpressionEngine.defaultEngine()` fornece o singleton padrao e `ExpressionEngine.builder()` cria engines isolados com `Clock` UTC e `CacheConfig.defaults()` quando nao configurados. `CacheConfig` possui builder imutavel, limita a quantidade positiva de resultados (1024 no engine default), sem *weigher*, e admite expiracao positiva desde o ultimo acesso opcional e desabilitada por default. Nao existe segundo nivel global nem compartilhamento de entrada entre engines.
- Chamadas concorrentes da mesma chave executam o pipeline uma unica vez por geracao, inclusive em falha. Expiracao ou eviction permitem uma geracao futura sem invalidar expressoes ja entregues; falhas internas inesperadas nao instalam entrada. O cache nao participa de `compute`.
- Nenhum contador de execucoes e instalado antecipadamente; o Tier 1 opcional introduz e mede sua propria politica de observacao apenas se for ativado.
- A API publica nao expoe bypass do cache, estatisticas, invalidacao, manutencao ou lifecycle na primeira versao. Um seam interno preserva a compilacao realmente sem cache para o carregador, testes e JMH.

---

## 18. Auditoria com custo zero quando desligada

A v1 pagava (pouco, mas pagava) pela possibilidade de auditar. Na v2:

- O plano normal **não contém nenhum branch de auditoria**.
- `computeWithAudit(...)` usa um **plano instrumentado**, construído **preguiçosamente** na primeira chamada auditada e cacheado junto ao plano normal. Instrumentação = decoradores sobre os nós executáveis que emitem eventos (leituras de variáveis, valores dinâmicos, chamadas de função, atribuições) em um ring buffer pré-alocado limitado por `maxAuditEvents`.
- `foldedVariableReads` do plano permitem que o trace explique valores dobrados em compilação (fidelidade preservada).
- Retorno inalterado: `AuditResult<T>` com resultado + `ExpressionAuditTrace`.

---

## 19. Erros e diagnósticos

- Três classes de erro, cada uma com `SourceSpan`, código estável e sugestão quando aplicável:
  - **léxico/sintático** (listener ANTLR + `ERROR_CHAR`);
  - **semântico** (`SemanticIssue` acumulados — reportar todos, não só o primeiro);
  - **runtime** (divisão por zero em `DECIMAL`, overflow em `FAST` sem fallback, propriedade inexistente sem metadados Java, `@` reentrante além de `maxFilterDepth`...).
- Mensagens específicas para as armadilhas documentadas da gramática (seção 5, item 4) — transformar limitações do lexer em diagnósticos guiados é mais barato e mais robusto que predicados léxicos.

---

## 20. Compatibilidade e migração (v1 → v2)

Mudanças de linguagem visíveis ao usuário, com detecção e mensagem dedicada:

| v1 | v2 | Diagnóstico sugerido |
|---|---|---|
| `x = 2 + 3;` (atribuição) | `x := 2 + 3;` | erro sintático em `=`+`;` → "atribuição agora usa `:=`" |
| `!=` | `<>` | token `!` seguido de `=`: "use `<>` para desigualdade" |
| `|x|` | `abs(x)` | `||` inesperado / `|` isolado → "módulo agora é `abs(x)`" |
| `sqrt` como sintaxe | `sqrt(x)` função | transparente se catálogo tiver `sqrt` |
| `2021-01-02` literal | `d"2021-01-02"` | sequência `INT - INT - INT` com forma de data → sugerir prefixo |
| `10:30` literal | `t"10:30"` | `INT : INT` fora de subscript → sugerir prefixo |
| `not in` apenas | `not in` e `nin` | — |
| `??` por família | `??` geral encadeável | — |
| `[]` inválido | `[]` válido | — |
| `<number>(x)` etc. | tipo declarado no ambiente ou `asNumber(x)` | token `<` + nome de tipo + `>` → "type hints saíram da sintaxe; declare o tipo em `registerExternalSymbol` ou use `asNumber(x)`" |

Ferramenta opcional: um **migrador de fonte** (regex + reparse) que converte expressões v1 armazenadas em banco para v2, validando por compilação.

---

## 21. Verificação e desempenho como requisito testável

- **Testes diferenciais** ANTLR × Pratt (quando fase 2 existir) e v1 × v2 sobre corpus real de expressões.
- **Property-based testing** do parser (round-trip: pretty-print da AST re-parseia para AST igual) e do folding (plano otimizado ≡ plano ingênuo para entradas aleatórias).
- **JMH** com metas explícitas:
  - `a + b * 2` (`FAST`): ordem de dezenas de ns por compute, **zero alocação** em regime estacionário;
  - `a + b * 2` (`DECIMAL`): alocação limitada aos `BigDecimal` de resultado;
  - navegação com metadados Java: custo ≈ getter direto + indireção constante;
  - compilação fria (cache miss) e quente (hit) medidas separadamente;
  - filtro + agregação sobre lista de 10k elementos, com e sem fusão.
- Perfil de alocação (async-profiler/JFR) como gate de regressão em CI.

---

## 22. Resumo — o que preservar, o que muda

**Preservado da v1** (fundamentos que continuam corretos):

- Camadas estritamente separadas: parse → AST → semântica → plano → runtime.
- API `compile(...)` / `compute(...)`; compilar uma vez, executar muitas.
- `Environment` explícito; modelo semântico separado da AST; representação executável intermediária imutável; símbolos com índices e frames em array; `UNBOUND ≠ null`; coerção só nas bordas; auditoria opcional; cache por fonte + ambiente.

**Novo na v2** (habilitado pela gramática e pelo foco em desempenho):

1. Compilação **unificada** com visões tipadas — cache por `(source, envId)`, sem `resultType` na chave.
2. Semântica assume 100% da tipagem (a gramática deixou de fingir que tipava); **type hints sintáticos removidos** — substituídos por tipos declarados no ambiente, funções `as*(x)` e inferência (seção 5.1).
3. Literais temporais materializados em compilação; regex garantidamente pré-compilada; `abs`/`sqrt` e `asNumber`/`asText`/... no catálogo.
4. Nós executáveis **especializados por operador × tipo** (call-sites monomórficos), reescritas (`% → *0.01`, `root → ^`), `in` constante como set-lookup.
5. Modo numérico `FAST` (long/double sem boxing) ao lado do `DECIMAL` compatível.
6. Otimizações novas: CSE, strength reduction, dead-assignment elimination, fusão de coleções (fase 2).
7. Navegação por `MethodHandle`/`LambdaMetafactory` + inline caches por call-site; zero `Method.invoke` no caminho quente.
8. Navegação segura, incluindo `?.[...]`.
9. `@` de filtros como slot de frame com save/restore — sem thread-locals.
10. Auditoria por plano instrumentado lazy — custo zero quando desligada.
11. Parsing SLL+bail com fallback LL, warm-up de ATN, e trilha para parser Pratt artesanal com o `.g4` como especificação.
