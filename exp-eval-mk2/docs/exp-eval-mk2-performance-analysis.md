# Análise de Performance do Módulo `exp-eval-mk2`

## Escopo

Este documento consolida uma análise estática do módulo `exp-eval-mk2` com foco em possíveis problemas de performance.

Importante:

- Parte dos achados abaixo continua baseada em revisão de código, mas alguns pontos já possuem validação empírica por JMH e estão identificados como tal.
- O módulo já possui histórico de benchmark para parsing em `exp-eval-mk2/docs/perf/performance-history.md`.

## Resumo Executivo

O parsing do módulo já recebeu atenção e medições, com ganhos claros a partir da estratégia `SLL -> LL fallback` e de warmup de corpus. O principal risco atual não parece estar no parser em si, mas no caminho de execução após a compilação.

Os maiores suspeitos são:

1. Reprocessamento de literais a cada execução.
2. Cópias redundantes de mapas e recriação de objetos por `compute()`.
3. Baixa efetividade potencial do cache de compilação quando o `ExpressionEnvironment` é reconstruído com frequência.
4. Uso de exceções como fluxo normal na inferência semântica de tipos literais.
5. Alocação extra em chamadas de função durante a avaliação.
6. Overhead severo no caminho de função customizada quando comparado ao `expression-evaluator`.

## Achados

### 1. Reprocessamento de literais constantes em toda execução

Severidade: Alta
Situação: Resolvido

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
Situação: Pendente

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
Situação: Pendente

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
Situação: Pendente

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
Situação: Pendente

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

### 6. Caminho de função customizada está comprovadamente mais lento que o `expression-evaluator`

Severidade: Alta
Situação: Refatoração concluída (Fases 1–5 executadas; Fases 2 e 4 aceitas, Fase 3 descartada)

Arquivos:

- `exp-eval-mk2/src/main/java/com/runestone/expeval2/internal/runtime/AbstractRuntimeEvaluator.java`
- `exp-eval-mk2/src/main/java/com/runestone/expeval2/catalog/FunctionDescriptor.java`
- `exp-eval-mk2/src/main/java/com/runestone/expeval2/internal/runtime/ExpressionRuntimeSupport.java`
- `exp-eval-mk2/src/main/java/com/runestone/expeval2/internal/runtime/ExecutionScope.java`
- `exp-eval-mk2/src/main/java/com/runestone/expeval2/internal/runtime/MutableBindings.java`
- `expression-evaluator/src/main/java/com/runestone/expeval/operation/other/FunctionOperation.java`
- `expression-evaluator/src/main/java/com/runestone/expeval/support/callsite/OperationCallSite.java`
- `expression-evaluator/src/main/java/com/runestone/expeval/support/callsite/OperationCallSiteFactory.java`

Trechos relevantes:

- `AbstractRuntimeEvaluator.evaluateFunctionCall(...)`
- `FunctionDescriptor.invoke(...)`
- `ExpressionRuntimeSupport.createExecutionScope()`
- `MutableBindings.snapshot()`
- `ExecutionScope.from(...)`
- `FunctionOperation.resolve(...)`
- `OperationCallSite.call(...)`
- `OperationCallSiteFactory.createCallSiteInvoker(...)`

Descrição:

O benchmark cross-module criado para comparar `expression-evaluator` e `exp-eval-mk2` em cenários equivalentes mostrou que o `mk2` venceu com folga em expressão literal-densa, mas perdeu de forma muito significativa no cenário com função definida pelo usuário.

Resultados medidos no benchmark principal (`CrossModuleExpressionEngineBenchmark`):

| Cenário | expression-evaluator (ns/op) | exp-eval-mk2 (ns/op) | Diferença do MK2 |
|---------|-----------------------------:|---------------------:|-----------------:|
| literalDense | 4615.876 | 1726.136 | +62.60% |
| variableChurn | 1976.268 | 2245.198 | -13.61% |
| userFunction | 1212.905 | 3785.894 | -212.13% |

Para separar o custo base do runtime do custo específico da chamada de função, foi executada uma rodada adicional com `-prof gc`.

Resultados de alocação no cenário com função customizada:

| Benchmark | Score (ns/op) | Alocação (B/op) |
|-----------|--------------:|----------------:|
| `legacyUserFunction` | 1662.188 | 440.038 |
| `mk2UserFunction` | 4748.322 | 4144.109 |

Resultados de alocação no cenário sem função, apenas churn de variáveis:

| Benchmark | Score (ns/op) | Alocação (B/op) |
|-----------|--------------:|----------------:|
| `legacyVariableChurn` | 2355.107 | 1888.898 |
| `mk2VariableChurn` | 2705.551 | 2000.062 |

Interpretação dos dados:

- O custo base do `mk2` no runtime geral existe, mas é relativamente pequeno no cenário sem função.
- A regressão grande aparece quando a expressão entra no caminho de função customizada.
- O principal sinal objetivo é a alocação: o `mk2` consome aproximadamente `9.4x` mais memória por operação no cenário de função customizada (`4144.109 B/op` contra `440.038 B/op`).
- Isso indica que o problema não está apenas em aritmética, binding de variáveis ou compilação, mas no dispatch de função e nas estruturas intermediárias criadas por chamada.

Por que está acontecendo:

1. Em `AbstractRuntimeEvaluator.evaluateFunctionCall(...)`, o `mk2` cria uma lista nova com os argumentos avaliados e outra lista nova com os argumentos coercidos em toda invocação.
2. Em `FunctionDescriptor.invoke(...)`, o `mk2` ainda cria uma cópia defensiva adicional com `List.copyOf(arguments)` antes de invocar a função.
3. A invocação final usa `MethodHandle.invokeWithArguments(...)`, que é o caminho mais genérico e normalmente mais caro do que um invoker especializado por aridade.
4. Depois da função, o retorno ainda passa por `RuntimeValueFactory.from(...)`, adicionando mais wrapping no hot path.
5. Em paralelo, o `mk2` continua recriando objetos por `compute()`, como avaliador e `ExecutionScope`, o que piora o cenário, embora isso por si só não explique a regressão de `-212.13%`.

Comparação com o caminho legado:

- O `expression-evaluator` resolve e cacheia o call site na própria operação.
- Ele reutiliza um `Object[]` de parâmetros por invocação da operação.
- O dispatch final passa por wrappers especializados por aridade (`Function1`, `Function2`, `Function3`, etc.), evitando o custo de `invokeWithArguments(...)` e reduzindo a necessidade de estruturas intermediárias.

Conclusão do achado:

Este ponto deixou de ser apenas um smell. Há evidência empírica suficiente para tratá-lo como um hotspot confirmado e de prioridade alta no runtime do `mk2`.

Menor refatoração segura:

- Remover do hot path a criação de coleções transitórias no caminho de função.
- Substituir o dispatch genérico baseado em `invokeWithArguments(...)` por um invoker pré-resolvido e especializado.
- Reduzir recriação de objetos auxiliares por `compute()` onde isso não for semanticamente necessário.

Plano detalhado de refatoração proposto:

1. Fase 1: eliminar alocação redundante dentro de `evaluateFunctionCall(...)`
   - Trocar `stream().toList()` por preenchimento direto em buffer indexado.
   - Evitar `new ArrayList<>(...)` por invocação.
   - Passar a operar com `Object[]` reutilizável por nó executável ou por evaluator.
   - Critério de saída: reduzir claramente o `B/op` do cenário `mk2UserFunction`.

2. Fase 2: substituir `FunctionDescriptor.invoke(List<Object>)`
   - Situação: Resolvido (PERF-012)
   - Introduzir um invoker compilado no bind da função, com assinatura mais direta que `List<Object>`.
   - Priorizar um caminho `Object[] -> retorno` ou wrappers por aridade pequena, que é o caso dominante das expressões avaliadas.
   - Remover `List.copyOf(arguments)` do hot path, já que os argumentos são locais e controlados pelo runtime.
   - Critério de saída: derrubar o custo de dispatch puro da função e reduzir a distância para o legado.
   - Resultado medido: `mk2UserFunction` melhorou +22.08% (1381 → 1076 ns/op); alocação caiu 31.2% (2672 → 1840 B/op). Decisão: ACCEPT.

3. Fase 3: reduzir wrapping e coerção redundantes
   - Situação: Descartado (PERF-013)
   - Verificar se o binding compilado da função pode armazenar metadados já preparados para coerção sem consultar listas de tipos a cada chamada.
   - Avaliar se o retorno da função pode evitar round-trip desnecessário por `RuntimeValueFactory.from(...)` quando o tipo já estiver alinhado.
   - Critério de saída: reduzir CPU e alocação residual depois da remoção das listas.
   - Resultado medido: `mk2UserFunction` −0.67% (ruído), B/op inalterado (1,840). Descoberta importante: a coerção já tem fast-path via `targetType.isInstance(value.raw())` e o `DataConversionService.convert` para conversões de identidade não aloca. O gap de 1,400 B/op em relação ao legado vem do overhead base do runtime (ExecutionScope + HashMap, snapshot de bindings, criação de avaliador) — alvo da Fase 4. Decisão: ADJUST.

4. Fase 4: atacar overhead base do `compute()`
   - Situação: Resolvido (PERF-014)
   - Reutilizar avaliadores quando forem efetivamente stateless.
   - Remover a dupla cópia `snapshot() -> new HashMap<>(...)` entre `MutableBindings` e `ExecutionScope`.
   - Critério de saída: aproximar `mk2VariableChurn` do legado e evitar que o restante do runtime masque o ganho no caminho de função.
   - Resultado medido: `mk2UserFunction` melhorou +15.35% (1,084 → 918 ns/op); `mk2VariableChurn` melhorou +15.72% (977 → 824 ns/op); alocação em `mk2UserFunction` caiu de 1,840 para 1,400 B/op (−23.9%). Gap de alocação para o legado: 960 B/op (antes 1,400 B/op). Decisão: ACCEPT.

5. Fase 5: validar cada etapa com JMH
   - Situação: Concluído (validação integrada às Fases 2, 3 e 4).
   - `CrossModuleExpressionEngineBenchmark` executado após cada mudança relevante, com comparação lado a lado com `expression-evaluator`.
   - Cenário `userFunction` repetido com `-prof gc` confirmando redução consistente de alocação a cada fase aceita.

Risco de não agir:

- O `mk2` pode parecer mais moderno e mais rápido em parsing e em literais, mas permanecer inadequado para workloads com funções pequenas e frequentes, que são comuns em motores de expressão.
- Isso compromete a expectativa central de uso da API "compila uma vez, executa muitas" exatamente no cenário em que extensibilidade por função customizada deveria ser um ponto forte.

## Pontos Positivos Observados

O módulo já mostra preocupação explícita com performance de parsing:

- existe benchmark JMH para parsing;
- existe benchmark JMH para warmup de corpus;
- o histórico em `exp-eval-mk2/docs/perf/performance-history.md` registra decisões aceitas e descartadas com números.

Isso é um bom sinal: o parser já está sendo tratado como uma área crítica com disciplina de medição.

## Lacunas Atuais de Medição

Hoje, a cobertura de benchmark visível no módulo já cobre parsing, warmup e uma primeira comparação de runtime. Ainda faltam medições mais granulares para:

- `compile()` fim a fim;
- `compute()` segmentado por tipo de operação;
- alocação por execução;
- impacto de volume de símbolos externos;
- custo de dispatch puro de chamadas de função frequentes após isolar o overhead base do runtime.

Sem essa medição, otimizações no runtime correm o risco de serem guiadas apenas por intuição.

## Próximos Passos Recomendados

### Prioridade 1

Refatorar e medir o caminho de função customizada do runtime:

- remover listas e cópias redundantes em `evaluateFunctionCall(...)`;
- substituir `invokeWithArguments(...)` por invocação pré-resolvida e menos genérica;
- repetir benchmark JMH do cenário `userFunction`;
- repetir benchmark com `-prof gc` para acompanhar `B/op`;
- registrar cada iteração no `performance-history.md`.

### Prioridade 2

Continuar a expansão da cobertura JMH de runtime:

- expressão simples com muitos literais;
- expressão com muitos símbolos externos;
- cenário "compila uma vez, executa muitas";
- cenário "compila toda vez".

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

O próximo passo tecnicamente correto não é mais descobrir se existe problema no runtime de função. Esse problema já foi medido. Agora a prioridade correta é refatorar o caminho de função customizada com disciplina de benchmark, mantendo o mesmo protocolo JMH para confirmar redução de `ns/op` e `B/op` a cada etapa.
