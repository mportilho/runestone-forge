# Análise de Performance do Módulo `exp-eval-mk2`

## Escopo

Este documento consolida uma análise estática do módulo `exp-eval-mk2` com foco em possíveis problemas de performance.

Importante:

- Os achados abaixo são hipóteses fundamentadas em revisão de código, não prova empírica de regressão.
- O módulo já possui histórico de benchmark para parsing em `exp-eval-mk2/docs/perf/performance-history.md`.
- Não há, no momento, benchmark equivalente para o caminho de execução do runtime (`compute()`), que é onde estão os principais smells encontrados.

## Resumo Executivo

O parsing do módulo já recebeu atenção e medições, com ganhos claros a partir da estratégia `SLL -> LL fallback` e de warmup de corpus. O principal risco atual não parece estar no parser em si, mas no caminho de execução após a compilação.

Os maiores suspeitos são:

1. Reprocessamento de literais a cada execução.
2. Cópias redundantes de mapas e recriação de objetos por `compute()`.
3. Baixa efetividade potencial do cache de compilação quando o `ExpressionEnvironment` é reconstruído com frequência.
4. Uso de exceções como fluxo normal na inferência semântica de tipos literais.
5. Alocação extra em chamadas de função durante a avaliação.

## Achados

### 1. Reprocessamento de literais constantes em toda execução

Severidade: Alta

Arquivos:

- `exp-eval-mk2/src/main/java/com/runestone/expeval2/internal/runtime/AbstractRuntimeEvaluator.java`

Trechos relevantes:

- `evaluateLiteral(...)`
- criação de `BigDecimal`
- `LocalDate.parse(...)`
- `LocalTime.parse(...)`
- `LocalDateTime.parse(...)`
- `OffsetDateTime.parse(...)`
- desserialização de string com `substring(...).replace(...)`

Descrição:

No método `evaluateLiteral(...)`, os mesmos literais do AST são convertidos novamente em toda chamada de `compute()`. Isso inclui parsing numérico, parsing temporal e unescape de strings. Como esses nós já são conhecidos na fase de compilação, esse custo tende a ser fixo e repetitivo.

Por que é suspeito:

- A API pública do módulo favorece o fluxo "compilar uma vez, executar muitas".
- Nesse modelo, literais constantes deveriam ser um custo amortizado na compilação, não na execução.
- Em expressões com muitos literais, esse trabalho se repete desnecessariamente.

Impacto esperado:

- Mais CPU por execução.
- Mais alocação de objetos transitórios.
- Maior pressão de GC em cenários de alta repetição.

Menor refatoração segura:

- Materializar literais constantes em objetos runtime durante a compilação ou na construção do plano de execução.
- Evitar parsing repetido de literais imutáveis no hot path.

Validação recomendada:

- Benchmark JMH para `compute()` com expressões contendo muitos literais numéricos, datas e strings.

Estratégias consideradas para decisão posterior:

- Árvore de runtime compilada uma vez: consiste em transformar o AST semântico em uma representação executável durante a compilação e reutilizá-la em todo `compute()`. A principal vantagem é remover parsing, branches e decisões repetidas do hot path. O principal custo é aumentar o uso de memória se a estrutura executável duplicar grande parte da árvore já existente.
- Cache seletivo de literais pré-materializados: consiste em manter, junto do `CompiledExpression`, um repositório de literais constantes já convertidos. É menos invasivo e consome menos memória do que uma segunda árvore completa, preservando boa parte do ganho no ponto mais caro. Como contrapartida, o evaluator continua navegando o AST genérico e fazendo lookups durante a execução.
- Cache por `ExpressionNode` dentro do evaluator: foi discutido como atalho, mas a efetividade depende do ciclo de vida do evaluator. No desenho atual, o evaluator é recriado em cada `compute()`, o que tornaria esse cache efêmero e pouco útil se ele ficasse apenas como estado interno do evaluator. Para funcionar de forma persistente, o cache precisaria ser promovido para o `CompiledExpression` ou estrutura equivalente.
- Enriquecimento do modelo de literais sem introduzir tipos de runtime no AST: foi considerada a possibilidade de trocar o `String value` de `LiteralNode` por um valor já materializado. A direção é válida para reduzir custo de execução, mas usar `RuntimeValue` diretamente no pacote de AST aumentaria o acoplamento entre as fases sintática e de runtime. A alternativa mais limpa é manter a representação original no AST e armazenar o valor resolvido em um artefato neutro da fase semântica ou compilada.
- Pré-compilação seletiva em vez de duplicação estrutural total: também foi discutido que manter duas árvores completas pode ser um custo de memória desnecessário, especialmente se muitas expressões permanecerem em cache. Por isso, a abordagem incremental mais prudente é começar pré-compilando apenas os nós mais caros, em especial literais constantes, antes de avaliar se uma árvore executável maior se justifica.
- Tratamento separado para pseudo-literais dinâmicos: qualquer estratégia escolhida precisa preservar o comportamento de valores como `currDate`, `currTime` e `currDateTime`, que não devem ser congelados na compilação como se fossem constantes estáticas.

Direção recomendada neste momento:

- Priorizar uma solução incremental com pré-materialização seletiva de literais constantes, medindo o ganho antes de considerar uma árvore completa de runtime.
- Evitar, por enquanto, introduzir dependência direta de `internal.ast` para `internal.runtime`.
- Usar benchmark para decidir se o ganho adicional de uma representação executável dedicada compensa o aumento de memória e complexidade.

### 2. Cópias redundantes de bindings e recriação de avaliadores por execução

Severidade: Alta

Arquivos:

- `exp-eval-mk2/src/main/java/com/runestone/expeval2/internal/runtime/ExpressionRuntimeSupport.java`
- `exp-eval-mk2/src/main/java/com/runestone/expeval2/internal/runtime/MutableBindings.java`
- `exp-eval-mk2/src/main/java/com/runestone/expeval2/internal/runtime/ExecutionScope.java`

Trechos relevantes:

- `ExpressionRuntimeSupport.createExecutionScope()`
- `MutableBindings.snapshot()`
- `ExecutionScope.from(...)`
- recriação de `MathEvaluator` e `LogicalEvaluator` em cada `compute()`

Descrição:

Cada chamada de `compute()` cria um snapshot imutável dos bindings com `Map.copyOf(...)` e, em seguida, copia o conteúdo novamente para um novo `HashMap` dentro de `ExecutionScope`. Além disso, a cada execução também é criado um novo avaliador (`MathEvaluator` ou `LogicalEvaluator`).

Por que é suspeito:

- Há duas cópias sucessivas do mesmo conjunto de bindings por execução.
- O custo é proporcional à quantidade de símbolos.
- A recriação dos avaliadores adiciona mais alocação em um caminho que tende a ser frequente.

Impacto esperado:

- Alocação O(n) por execução.
- Overhead perceptível quando a expressão é reutilizada muitas vezes.
- Pressão de GC em workloads com alto throughput.

Menor refatoração segura:

- Remover a dupla cópia do mapa.
- Avaliar se o `ExecutionScope` pode copiar uma única vez a partir do mapa mutável base.
- Reutilizar avaliadores quando eles forem stateless, ou incorporar a lógica diretamente ao runtime compilado.

Validação recomendada:

- Benchmark JMH com número crescente de símbolos externos.
- Perfil de alocação para confirmar custo por `compute()`.

### 3. Cache de compilação possivelmente pouco efetivo quando o ambiente é reconstruído

Severidade: Média-Alta

Arquivos:

- `exp-eval-mk2/src/main/java/com/runestone/expeval2/internal/compiler/ExpressionCompiler.java`
- `exp-eval-mk2/src/main/java/com/runestone/expeval2/environment/ExpressionEnvironmentBuilder.java`

Trechos relevantes:

- `ExpressionCompiler.compile(...)`
- chave `ExpressionCacheKey(source, environment.environmentId(), resultType)`
- `new ExpressionEnvironmentId(UUID.randomUUID().toString())`

Descrição:

O cache de compilação considera o `environmentId` como parte da chave. Entretanto, cada `build()` do `ExpressionEnvironmentBuilder` gera um identificador aleatório novo com `UUID.randomUUID()`.

Por que é suspeito:

- Dois ambientes semanticamente idênticos, mas reconstruídos em momentos diferentes, não compartilham cache.
- Se a aplicação criar o ambiente por request, o cache praticamente não ajuda.
- Nesse cenário, parse + AST + resolução semântica voltam ao caminho quente.

Impacto esperado:

- Baixa taxa de acerto do cache em aplicações que não reutilizam a mesma instância de ambiente.
- Mais CPU e alocação na fase de compilação.

Inferência importante:

Este problema depende fortemente do padrão de uso da aplicação. Se o ambiente for singleton e reutilizado, o risco cai bastante. Se for recriado com frequência, o impacto pode ser alto.

Menor refatoração segura:

- Documentar explicitamente que `ExpressionEnvironment` deve ser reutilizado.
- Medir a taxa de acerto real do cache.
- Considerar uma chave estável derivada do conteúdo do ambiente, se o custo e a complexidade compensarem.

Validação recomendada:

- Benchmark comparando:
    - mesma instância de ambiente reutilizada;
    - ambientes equivalentes recriados a cada compilação.

### 4. Inferência semântica de literais usa exceções como fluxo normal

Severidade: Média

Arquivos:

- `exp-eval-mk2/src/main/java/com/runestone/expeval2/internal/semantic/SemanticResolver.java`

Trechos relevantes:

- `inferLiteralType(...)`
- `canParseDate(...)`
- `canParseTime(...)`
- `canParseDateTime(...)`

Descrição:

Na inferência de tipo literal, um valor numérico comum passa por várias tentativas de parse temporal antes de chegar no `BigDecimal`. Essas tentativas dependem de exceções (`DateTimeParseException`) para sinalizar que o literal não é data/hora/datetime.

Por que é suspeito:

- Exceções são caras quando acontecem com frequência.
- Literais numéricos tendem a ser comuns em expressões matemáticas.
- O custo se concentra na compilação, não na execução.

Impacto esperado:

- Mais CPU no caminho de compilação.
- Maior ruído em perfis quando o cache de compilação não tem bom reaproveitamento.

Menor refatoração segura:

- Aplicar heurísticas baratas antes do parse, por exemplo formato esperado, presença de separadores e comprimento mínimo.
- Reduzir tentativas de parse incompatíveis com a forma do literal.

Validação recomendada:

- Benchmark de compilação com grande volume de literais numéricos e temporais.

### 5. Chamadas de função acumulam alocação desnecessária

Severidade: Média

Arquivos:

- `exp-eval-mk2/src/main/java/com/runestone/expeval2/internal/runtime/AbstractRuntimeEvaluator.java`
- `exp-eval-mk2/src/main/java/com/runestone/expeval2/catalog/FunctionDescriptor.java`
- `exp-eval-mk2/src/main/java/com/runestone/expeval2/internal/runtime/RuntimeCoercionService.java`

Trechos relevantes:

- `evaluateFunctionCall(...)`
- criação de `List<RuntimeValue>`
- criação de `List<Object>`
- `FunctionDescriptor.invoke(...)`
- `List.copyOf(arguments)`
- `invokeWithArguments(...)`

Descrição:

Uma chamada de função cria uma lista com os argumentos avaliados, outra lista com os argumentos coercidos e, em seguida, faz uma nova cópia defensiva antes da invocação do `MethodHandle`.

Por que é suspeito:

- Há múltiplas alocações por chamada.
- `invokeWithArguments(...)` é o caminho mais flexível e tende a ter overhead maior que invocações mais especializadas.
- Funções pequenas e frequentes podem ficar dominadas por overhead de infraestrutura.

Impacto esperado:

- Mais alocação em expressões com muitos calls encadeados.
- Overhead maior do que o necessário para funções simples.

Menor refatoração segura:

- Evitar cópia defensiva redundante se a lista já for local e controlada.
- Explorar um caminho de invocação com menos boxing/cópias, se os benchmarks justificarem.

Validação recomendada:

- Benchmark com expressões ricas em chamadas de função pequenas.

## Pontos Positivos Observados

O módulo já mostra preocupação explícita com performance de parsing:

- existe benchmark JMH para parsing;
- existe benchmark JMH para warmup de corpus;
- o histórico em `exp-eval-mk2/docs/perf/performance-history.md` registra decisões aceitas e descartadas com números.

Isso é um bom sinal: o parser já está sendo tratado como uma área crítica com disciplina de medição.

## Lacunas Atuais de Medição

Hoje, a cobertura de benchmark visível no módulo está concentrada em parsing e warmup. Não foi identificado benchmark equivalente para:

- `compile()` fim a fim;
- `compute()` em expressões já compiladas;
- alocação por execução;
- impacto de volume de símbolos externos;
- impacto de chamadas de função frequentes.

Sem essa medição, otimizações no runtime correm o risco de serem guiadas apenas por intuição.

## Próximos Passos Recomendados

### Prioridade 1

Criar benchmarks JMH para o runtime de execução:

- expressão simples com muitos literais;
- expressão com muitos símbolos externos;
- expressão com várias chamadas de função;
- cenário "compila uma vez, executa muitas";
- cenário "compila toda vez".

### Prioridade 2

Validar empiricamente os dois suspeitos mais fortes:

1. custo de reprocessamento de literais;
2. custo da dupla cópia de bindings e recriação de objetos por `compute()`.

### Prioridade 3

Medir taxa de acerto do cache de compilação sob dois modelos de uso:

- ambiente singleton reutilizado;
- ambiente reconstruído por request.

## Conclusão

O maior risco de performance em `exp-eval-mk2`, neste momento, não parece ser o parser. Essa frente já foi estudada e tem histórico de benchmark consistente.

Os principais suspeitos migraram para o runtime:

- trabalho repetido com literais constantes;
- cópias de estruturas e alocação por execução;
- overhead de chamadas de função;
- dependência da reutilização do ambiente para que o cache de compilação realmente funcione.

O próximo passo tecnicamente correto não é refatorar de imediato, mas medir o runtime com JMH e usar esses resultados para priorizar as otimizações de maior retorno.
