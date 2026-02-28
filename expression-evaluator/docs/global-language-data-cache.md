# Cache Global de `LanguageData`

## 1. Contexto e Problema

O `LanguageData` encapsula o resultado do parsing ANTLR de uma expressão: a árvore de operações pronta para execução.

```java
public record LanguageData(
    AbstractOperation operation,
    Map<String, AbstractVariableValueOperation> variables,
    Map<String, AssignedVariableOperation> assignedVariables
) {}
```

O parsing via ANTLR é uma das operações mais custosas do módulo (lexer, parser, visitor, construção da árvore de nós). O problema identificado no [performance-analysis.md](performance-analysis.md):

> "Não existe um cache global de `LanguageData` compartilhado entre diferentes instâncias de calculadoras ou serviços que não utilizem o supplier padrão."

### Cache atual: por instância

O `DefaultExpressionSupplier` já possui um cache via `MemoizedExpiringFunction`, mas ele é:

- **Por instância** do supplier — múltiplos suppliers com a mesma expressão fazem parsing redundante.
- **Com TTL de 10 minutos** — após expirar, o ANTLR roda novamente para uma gramática que nunca muda.
- **Inexistente** para quem cria `Expression` diretamente (sem usar o supplier).

### Condição de corrida adicional

O `MemoizedExpiringFunction.applyWithExpiry()` possui uma janela onde múltiplas threads podem disparar o parsing da mesma expressão antes do `FutureTask` ser registrado no mapa, gerando parsing redundante em ambientes concorrentes durante o "cold start".

---

## 2. Por que `LanguageData` não pode ser compartilhado diretamente

`LanguageData` é **mutável em runtime**: as instâncias de `AbstractVariableValueOperation` no mapa `variables` são modificadas diretamente durante o uso (`setVariable`, `setVariables`). Compartilhar a mesma instância entre avaliadores causaria corrupção de dados.

A solução exige dois níveis:

```
expressão "x + y * 2"
        │
        ▼
GLOBAL_CACHE (ConcurrentHashMap<String, LanguageData>)
        │  ← template imutável: ANTLR roda APENAS uma vez por expressão única
        │
   computeIfAbsent() — atômico por chave
        │
        ▼
   CloningContext.copy() → clone mutável por instância de ExpressionEvaluator
        │
        ▼
   this.languageData  ← recebe setVariable(), warmUp(), evaluate()
```

O **template** no cache global nunca é modificado. Cada `ExpressionEvaluator` trabalha com seu próprio **clone**.

---

## 3. Por que `computeIfAbsent` resolve a condição de corrida

`ConcurrentHashMap.computeIfAbsent` é **atômico por chave**: a função de mapeamento é chamada **no máximo uma vez** para uma dada chave, mesmo com múltiplas threads tentando simultaneamente. As demais threads ficam aguardando o resultado da thread vencedora.

Isso elimina a janela de corrida presente no `MemoizedExpiringFunction`, sem precisar de locks manuais.

---

## 4. Por que o cache é permanente (sem TTL)

A gramática ANTLR é **imutável em runtime** — o resultado do parsing de `"x + y * 2"` é sempre o mesmo. Não há razão para expirar esse cache. Um `ConcurrentHashMap` permanente é correto e mais eficiente que um cache com TTL.

O único risco é memory leak em cenários onde expressões são geradas **dinamicamente** em quantidade ilimitada (ex: `"soma(1, 2)"`, `"soma(1, 3)"`, ...). Isso deve ser documentado como antipadrão de uso.

---

## 5. Por que `warmUp()` não é cacheado globalmente

```java
public void warmUp() {
    parseExpression();
    OperationContext operationContext = createOperationContext(true, null); // usa this.expressionContext
    languageData.operation().accept(new WarmUpOperationVisitor(operationContext));
}
```

O `warmUp()` percorre a árvore e pré-avalia operações usando o `expressionContext` da instância (funções registradas, dicionários). Como esse contexto varia por instância, o resultado do warm-up **não é portável** entre avaliadores.

O template armazenado no cache global contém a árvore **antes** do warm-up. O `DefaultExpressionSupplier` continua relevante para cachear o **estado pós-warm-up** por `expressionContext`.

---

## 6. Implementação

### 6.1. Alterações em `ExpressionEvaluator`

Adicionar um campo estático e refatorar `parseExpression()` em dois métodos.

**Arquivo:** `expression/ExpressionEvaluator.java`

```java
// Campo estático — logo após PARSING_ERROR_LISTENER (linha 61)
private static final ConcurrentHashMap<String, LanguageData> GLOBAL_LANGUAGE_DATA_CACHE =
        new ConcurrentHashMap<>(256);
```

```java
// Substituir parseExpression()
private void parseExpression() {
    if (languageData == null) {
        LanguageData template = GLOBAL_LANGUAGE_DATA_CACHE.computeIfAbsent(expression, this::doParse);
        CloningContext cloningCtx = new CloningContext();
        AbstractOperation copy = template.operation().copy(cloningCtx);
        languageData = new LanguageData(copy, cloningCtx.getVariables(), cloningCtx.getAssignedVariables());
    }
}

// Extração do parsing puro (sem clonagem)
private LanguageData doParse(String expr) {
    LanguageParser languageParser = new DefaultLanguageParser();
    StartContext startContext = createLanguageGrammarContext(
            CharStreams.fromString(expr), PredictionMode.SLL).start();
    return languageParser.parse(startContext);
}

// Utilitário para testes e manutenção
static void clearGlobalCache() {
    GLOBAL_LANGUAGE_DATA_CACHE.clear();
}
```

### 6.2. Nenhuma alteração necessária em outros arquivos

- `DefaultExpressionSupplier` — continua funcionando. O cache interno passa a cachear o estado pós-warm-up, não mais o parsing ANTLR.
- `Expression` — nenhuma mudança.
- `LanguageData` — nenhuma mudança.

---

## 7. Impacto por cenário

| Cenário | Comportamento antes | Comportamento depois |
|---|---|---|
| Mesma expressão, criação direta repetida | ANTLR a cada instância | ANTLR 1x + clone por instância |
| Cache do supplier expirado (10 min) + re-criação | ANTLR novamente | Clone do global (0 ANTLR) |
| Múltiplos suppliers com a mesma expressão | ANTLR por supplier | ANTLR 1x global compartilhado |
| Concorrência cold-start (múltiplas threads) | Race condition possível | Atômico via `computeIfAbsent` |
| Expressões únicas e dinâmicas em excesso | Cache por supplier expira | Cache global cresce sem bound |

---

## 8. Considerações de produção

### Memory bound (opcional)

Se o conjunto de expressões únicas for potencialmente ilimitado, substituir o `ConcurrentHashMap` por um cache com limite de tamanho usando Caffeine:

```java
// Requer dependência: com.github.ben-manes.caffeine:caffeine
private static final Cache<String, LanguageData> GLOBAL_LANGUAGE_DATA_CACHE =
        Caffeine.newBuilder()
                .maximumSize(10_000)
                .build();

// Em parseExpression():
LanguageData template = GLOBAL_LANGUAGE_DATA_CACHE.get(expression, this::doParse);
```

`Caffeine.get()` tem a mesma semântica atômica de `ConcurrentHashMap.computeIfAbsent`.

### Antipadrão a evitar

```java
// RUIM: expressões geradas dinamicamente com valores embutidos
for (Pedido p : pedidos) {
    new Expression("desconto = " + p.getPercentual() + " * valor").evaluate(...);
}

// BOM: usar variáveis
Expression expr = new Expression("desconto = percentual * valor");
for (Pedido p : pedidos) {
    expr.copy().setVariable("percentual", p.getPercentual()).evaluate(...);
}
```

---

## 9. Relação com outras melhorias do relatório

Esta mudança complementa diretamente os itens do [performance-analysis.md](performance-analysis.md):

- **§4.1 Custo do ANTLR** — resolvido: parsing ocorre uma única vez por expressão por JVM.
- **§4.2 Condição de corrida no Memoization** — resolvido: `computeIfAbsent` é atômico.
- **§3.2 Clonagem de árvore** — permanece necessária, mas o custo de clonar (instanciar nós Java) é significativamente menor que o parsing ANTLR completo.
