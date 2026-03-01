# Análise Técnica: runestone-toolkit

Este documento apresenta uma revisão detalhada do módulo `runestone-toolkit`, focando em qualidade de código, performance, concorrência e possíveis problemas de design identificados durante a análise.

## Sumário de Achados por Severidade

| Severidade | Descrição | Impacto |
|------------|-----------|---------|
| **Crítica** | Vazamento de Memória em Memoization | `OutOfMemoryError` em aplicações de longa duração. |
| **Alta** | Sensibilidade ao Relógio do Sistema | Inconsistência na expiração de cache (NTP/ajustes manuais). |
| **Média** | Performance em Conversão de Enums | Latência O(N) em operações frequentes de conversão. |
| **Média** | Gargalo de Concorrência em `LruCache` | Contenção de threads em ambientes de alta carga. |
| **Baixa** | Inconsistência de API e Precisão | Riscos menores de NPE e imprecisão em ponto flutuante. |

---

## Detalhamento dos Problemas

### 1. Vazamento de Memória (Memory Leak)
**Arquivos:** `MemoizedFunction.java`, `MemoizedBiFunction.java` e variantes expirantes.
- **Problema:** Utilizam `ConcurrentHashMap` para armazenar resultados sem qualquer política de despejo (eviction) ou limite de tamanho.
- **Risco:** Em cenários onde as funções são chamadas com uma grande variedade de parâmetros únicos, o mapa crescerá indefinidamente até esgotar a memória Heap.
- **Sugestão:** Implementar um limite de tamanho (Bounded Cache) ou utilizar referências fracas (`WeakReference`) para as chaves/valores.

### 2. Sensibilidade ao Relógio do Sistema
**Arquivos:** `MemoizedExpiringFunction.java`, `MemoizedExpiringBiFunction.java`.
- **Problema:** O gerenciamento de expiração utiliza `System.currentTimeMillis()`.
- **Risco:** Se o relógio do servidor sofrer ajustes (via NTP ou manualmente), o cálculo de expiração pode falhar, resultando em caches que nunca expiram ou expiram antes do tempo.
- **Sugestão:** Migrar para `System.nanoTime()`, que é monotônico e imune a saltos no relógio do sistema, conforme já utilizado em `MemoizedExpiringSupplier.java`.

### 3. Performance na Conversão de Enums
**Arquivo:** `DefaultDataConversionService.java` (método `convertEnum`).
- **Problema:** A busca por constantes de enum percorre o array `getEnumConstants()` e realiza `equalsIgnoreCase` em cada iteração (O(N)).
- **Risco:** Degradadação de performance em aplicações que realizam muitas conversões dinâmicas de strings para enums grandes.
- **Sugestão:** Implementar um cache interno (`Map<Class<?>, Map<String, Enum<?>>>`) para resoluções O(1).

### 4. Gargalo de Concorrência (Contenção)
**Arquivo:** `LruCache.java`.
- **Problema:** O uso de `synchronized` em todos os métodos públicos garante thread-safety, mas causa serialização total dos acessos.
- **Risco:** Baixa escalabilidade em ambientes multi-thread intensivos.
- **Sugestão:** Considerar o uso de `ConcurrentHashMap` com uma estratégia de manutenção de ordem LRU baseada em amostragem ou filas de acesso, ou utilizar bibliotecas especializadas como Caffeine se o projeto permitir dependências externas.

### 5. Precisão de Ponto Flutuante
**Arquivo:** `NumberToBigDecimalConverter.java`.
- **Problema:** O uso de `BigDecimal.valueOf(float/double)` pode introduzir erros de arredondamento inerentes à representação binária de tipos `float`.
- **Exemplo:** `0.1f` pode ser convertido para `0.10000000149011612`.
- **Sugestão:** Para conversões que exigem precisão absoluta do valor literal, prefira `new BigDecimal(String.valueOf(data))`.

### 6. Inconsistências de API e Estilo
- **Certify:** A classe `Certify` está subutilizada, contendo apenas `requireNonBlank`. Recomenda-se expandir para cobrir as validações comuns presentes em `Asserts`.
- **Null Safety:** `NumberToIntConverter` valida `null` no switch, mas `NumberToBigDecimalConverter` não, criando um comportamento inconsistente para usuários que utilizam as classes de conversão diretamente.
- **Typo:** Variável `convetersMap` no `DataConverterLoader` (deveria ser `convertersMap`).

---

## Práticas Positivas Observadas
- **Thundering Herd Prevention:** O uso de `FutureTask` em memoizações evita que múltiplas threads calculem o mesmo valor simultaneamente, protegendo recursos computacionais caros.
- **Safe-Publishing:** Implementação correta de `volatile` e `synchronized` para inicialização preguiçosa (Lazy Initialization) segura.
- **Modernismo:** Código limpo e idiomático utilizando recursos de Java 21 como Pattern Matching e Switch Expressions.
