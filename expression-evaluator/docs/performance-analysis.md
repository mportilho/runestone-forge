# Relatório de Análise de Performance: expression-evaluator

## 1. Resumo Executivo
Este relatório detalha a análise de performance do módulo `expression-evaluator`. Embora o módulo tenha passado por rodadas recentes de otimização (Fevereiro de 2026), ainda existem oportunidades críticas para redução de latência e pressão sobre o Garbage Collector (GC), especialmente em cenários de alta frequência de avaliação e expressões complexas.

---

## 2. Caminhos Críticos (Hot Paths)

### 2.1. Motor de Avaliação (`AbstractOperation`)
O método `evaluate` é o coração do sistema. A lógica de `castOperationResult` é executada em cada nó da árvore durante a avaliação.
- **Problema:** A verificação constante de tipos e a possível invocação do `conversionService` em cada nó geram um overhead cumulativo significativo.
- **Impacto:** Latência aumentada em árvores de expressão profundas.

### 2.2. Resolução de Variáveis (`VariableValueOperation`)
- **Problema:** No método `resolve`, se o `userContext` e o `expressionContext` forem a mesma instância (comportamento padrão), a busca por variáveis é realizada de forma redundante em caso de ausência do valor.
- **Impacto:** Chamadas duplicadas a `findValue`, o que é crítico se o `variablesSupplier` for uma operação custosa (ex: consulta a banco ou cache externo).

### 2.3. Operações Aritméticas (`BaseOperation`)
- **Problema:** A conversão de resultados numéricos não-`BigDecimal` utiliza `new BigDecimal(result.toString())`.
- **Impacto:** O uso de `toString()` seguido de parsing é consideravelmente mais lento que `BigDecimal.valueOf()` ou conversões diretas de tipos primitivos.

---

## 3. Alocações de Objetos e Pressão de GC

### 3.1. Contextos de Avaliação e Invocação
- **OperationContext:** Uma nova instância de `OperationContext` (record) é criada a cada chamada de `evaluate` no `ExpressionEvaluator`.
- **CallSiteContext:** Na `FunctionOperation`, um novo `CallSiteContext` é criado sempre que o `OperationContext` muda. Como cada avaliação gera um novo `OperationContext`, as funções **nunca** reusam o `CallSiteContext` entre avaliações.
- **Parâmetros de Funções:** A cada chamada de função, um novo `Object[]` é alocado para transportar os parâmetros.

### 3.2. Clonagem de Árvore (`Expression.copy()`)
- **Problema:** O `DefaultExpressionSupplier` realiza um `copy()` (clone profundo da árvore) a cada requisição de expressão cacheada.
- **Impacto:** Em expressões grandes, o custo de instanciar centenas de nós de operação pode superar o tempo da avaliação em si.

---

## 4. Parsing e Cache

### 4.1. Custo do ANTLR
O parsing de expressões via ANTLR é uma operação pesada. O cache atual é por instância de `ExpressionEvaluator` ou via `DefaultExpressionSupplier`.
- **Oportunidade:** Não existe um cache global de `LanguageData` (árvore parseada e pronta) compartilhado entre diferentes instâncias de calculadoras ou serviços que não utilizem o supplier padrão.

### 4.2. Condição de Corrida no Memoization (`runestone-toolkit`)
- **Classe:** `MemoizedExpiringFunction`
- **Problema:** O método `applyWithExpiry` possui uma janela de tempo onde múltiplas threads podem disparar o parsing da mesma expressão simultaneamente antes do `FutureTask` ser registrado no mapa.
- **Impacto:** Picos de CPU e parsing redundante em ambientes multithreaded durante o "cold start" do cache.

---

## 5. Concorrência e Segurança

### 5.1. Isolamento de Contexto
- **Problema:** O `ExpressionContext` compartilha o `HashMap` de dicionário e funções entre a expressão original e suas cópias.
- **Risco:** `HashMap` não é thread-safe. Se uma thread modificar o dicionário de uma cópia (via `setVariable` ou `addDictionary`) enquanto outra thread avalia outra cópia, pode ocorrer corrupção de dados ou `ConcurrentModificationException`.

---

## 6. Recomendações de Otimização

1.  **Reuso de Contextos:** Transformar `CallSiteContext` e `VariableValueProviderContext` em objetos mais persistentes ou utilizar pools/caches baseados em identidade para reduzir alocações.
2.  **Fast-path para Variáveis:** Na `VariableValueOperation`, adicionar um check `if (context.userContext() != context.expressionContext())` antes da segunda busca.
3.  **Otimização de BigDecimal:** Substituir `new BigDecimal(result.toString())` por caminhos otimizados baseados no tipo real do número (ex: `Long`, `Double`).
4.  **Cópia Lazy de Contextos:** No `ExpressionContext`, utilizar mecanismos de *Copy-on-Write* para o dicionário e funções ao realizar o clone da expressão, garantindo thread-safety com custo mínimo.
5.  **Melhoria no Memoization:** Substituir a lógica de trava manual por `ConcurrentHashMap.computeIfAbsent` para garantir que o parsing de uma string única ocorra exatamente uma vez.
6.  **Estratégia de Pooling de Arrays:** Avaliar o reuso de buffers de parâmetros (`Object[]`) em chamadas de funções de mesma aridade através de `ThreadLocal`.
