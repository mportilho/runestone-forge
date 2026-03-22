# Guia de Benchmark Comparativo entre Componentes

Este documento descreve a metodologia usada para projetar, executar e interpretar benchmarks de comparação de desempenho entre dois componentes que realizam a mesma função lógica mas diferem em implementação. O guia é genérico e reprodutível: qualquer par de componentes com API e semântica equivalentes pode ser comparado seguindo estas etapas.

O documento foi produzido a partir da experiência concreta de comparar `expression-evaluator` (legacy) contra `exp-eval-mk2` (mk2) no contexto deste repositório, mas todas as etapas são descritas de forma suficientemente abstrata para se aplicarem a outros contextos.

---

## 1. Premissas e Escopo

Antes de qualquer linha de código, é necessário responder três perguntas:

**1.1. Os componentes são realmente equivalentes?**
Dois componentes são equivalentes para fins de benchmark quando, dado o mesmo input, produzem o mesmo resultado observável (ou um resultado semanticamente igual, mesmo que internamente diferente). Diferenças de tipo de retorno aceitáveis pelo cliente (ex.: `BigDecimal` vs `double`) não invalidam a comparação, mas devem ser documentadas.

**1.2. Quais aspectos do desempenho importam para o cliente?**
Desempenho tem pelo menos duas dimensões mensuráveis diretamente no JMH:
- **Tempo por operação (ns/op)** — latência de cada chamada individual.
- **Alocação por operação (B/op)** — pressão sobre o GC por chamada, medida pelo profiler `gc.alloc.rate.norm`.

Throughput (ops/s) e tempo de warmup também podem ser relevantes, mas ns/op com GC profiler cobre a maioria dos casos de decisão arquitetural.

**1.3. Existe algum modo de operação que deve ser comparado separadamente?**
Se um dos componentes expõe modos distintos de execução (ex.: com vs. sem auditoria, com vs. sem cache), cada modo forma um *grupo de comparação* independente. Agrupamentos incorretos (ex.: misturar modo com auditoria e sem auditoria em um único quadro) produzem leituras enganosas.

---

## 2. Levantamento dos Cenários de Teste

O ponto de partida para os cenários de benchmark é a **suíte de testes existente**, não hipóteses sobre o que é importante. Isso garante que os cenários reflitam uso real documentado e que o benchmark produza resultados interpretáveis à luz dos testes de correção.

### 2.1. Como identificar cenários relevantes

Percorra os testes unitários de ambos os componentes e classifique as expressões/entradas em **famílias de carga**:

| Família | Características | Exemplo |
|---|---|---|
| **Literal Dense** | Alta concentração de constantes, pouca ou nenhuma variável | `1001.0 + 1002.0 + … + 1064.0` |
| **Variable Churn** | Muitas variáveis, poucas constantes, bindings trocados a cada chamada | `a * b + c * d − e + f * g − …` |
| **User Function** | Despacho de funções registradas pelo usuário | `weighted(a,b,c) + weighted(d,e,f) + …` |
| **Conditional** | Branching dependente de variáveis | `if a > b then a * c − d else g * h − i endif` |
| **Math Function Chain** | Funções transcendentais em cadeia | `ln(a) + ln(b) + … + lb(k) + lb(l)` |
| **Power Chain** | Exponenciação repetida | `a^2 + b^2 − c^2 + d^2 − …` |

Cada família estressará uma parte diferente do pipeline de execução. A escolha deve cobrir ao menos: caminho puro de constantes, caminho puro de variáveis, despacho de função, e ramificação.

### 2.2. Critério de inclusão de um cenário

Um cenário é incluído se:
1. Existe ao menos um teste de correção que o exercita em algum dos dois componentes.
2. É representativo de uso real ou de uma fronteira de desempenho identificada.
3. Pode ser expresso com a mesma expressão de entrada nos dois componentes (ou com transformação documental clara).

Um cenário é **excluído** se não existir equivalente em algum dos componentes (ex.: sintaxe suportada apenas por um deles), a não ser que o objetivo seja documentar exatamente essa diferença de cobertura.

### 2.3. Frames de dados

Para cenários com variáveis, pré-compute um pool rotativo de frames de dados (`N` frames, tipicamente 256, acessados como `frames[index & (N-1)]`). Isso garante:
- **Repetibilidade**: o mesmo conjunto de valores é usado em todos os runs.
- **Ausência de bias de cache**: o pool de 256 entradas excede qualquer cache de linha e impede que o JIT elimine o processamento das variáveis.
- **Diversidade controlada**: valores gerados deterministicamente a partir de seeds diferentes por cenário (ex.: `baseOffset + frameIndex * multiplier`) previnem que o JIT specialize no caso escalar.

```java
// Padrão de pré-computação de frames
private static final Frame[] FRAMES = buildFrames(3L, 11L);

private static Frame[] buildFrames(long baseOffset, long multiplier) {
    Frame[] frames = new Frame[FRAME_COUNT]; // 256
    for (int frameIndex = 0; frameIndex < FRAME_COUNT; frameIndex++) {
        BigDecimal[] values = new BigDecimal[VAR_COUNT];
        for (int vi = 0; vi < VAR_COUNT; vi++) {
            long intPart  = baseOffset + frameIndex + (vi + 1L) * multiplier;
            long fracPart = (frameIndex * 53L + vi * 17L) % 1000L;
            values[vi] = new BigDecimal("%d.%03d".formatted(intPart, fracPart));
        }
        frames[frameIndex] = new Frame(values);
    }
    return frames;
}
```

Use seeds diferentes por cenário (ex.: `variableFrame` usa `(3L, 11L)`, `userFunctionFrame` usa `(17L, 23L)`) para evitar que o JIT detecte padrão comum entre cenários.

---

## 3. Estrutura do Benchmark JMH

### 3.1. Classe de benchmark

Crie uma única classe de benchmark para todos os cenários comparativos. Isso garante que warmup e condições de JIT sejam consistentes entre os benchmarks.

**Anotações canônicas para runs de produção:**
```java
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(value = 3, jvmArgsAppend = {"-Xms1g", "-Xmx1g"})
```

A escolha de `AverageTime` em vez de `Throughput` facilita a leitura direta dos quadros (ns/op é mais intuitivo que ops/s para comparação de latência). O heap fixo (`-Xms` = `-Xmx`) elimina variação por expansão de heap durante a medição.

### 3.2. Grupos de comparação e convenção de nomes

Nomeie os métodos de benchmark seguindo a convenção:

```
{cenário}_{componente}
```

Exemplos:
- `literalDense_legacy` — componente legado no cenário literalDense
- `literalDense_mk2Compute` — componente novo, modo `compute()`
- `literalDense_mk2Audit` — componente novo, modo `computeWithAudit()`

Essa convenção permite que ferramentas de análise (incluindo o próprio JMH em modo texto) agrupem naturalmente os benchmarks por cenário e por componente.

### 3.3. Classes de estado (State)

Cada cenário deve ter sua própria classe de estado. Use `@State(Scope.Thread)` para isolamento entre benchmarks. O `@Setup(Level.Trial)` garante que cada benchmark receba sua própria instância pré-aquecida.

```java
@State(Scope.Thread)
public static class LiteralDenseState {
    ComponentA legacy;
    ComponentB mk2;
    int legacyIndex;
    int mk2Index;

    @Setup(Level.Trial)
    public void setUp() {
        legacy = /* constrói componente legado */;
        mk2    = /* compila expressão no componente novo */;
    }
}
```

**Regra**: o estado deve conter **ambos** os componentes, mesmo que um benchmark use apenas um deles. Isso garante que a memória alocada durante o setup seja comparável entre os benchmarks do mesmo cenário.

**Regra**: use índices separados por componente (`legacyIndex`, `mk2Index`) para que a rotação de frames seja independente entre modos de execução do mesmo cenário.

### 3.4. Métodos de benchmark

Cada método deve:
1. Avançar o índice de frame.
2. Aplicar o frame ao componente (binding de variáveis).
3. Invocar a operação a ser medida.
4. Retornar o resultado (para evitar eliminação pelo JIT via dead-code elimination).

```java
@Benchmark
public BigDecimal literalDense_legacy(LiteralDenseState s) {
    BenchmarkSupport.applyFrame(s.legacy, BenchmarkSupport.frame(s.legacyIndex++));
    return s.legacy.evaluate();
}

@Benchmark
public BigDecimal literalDense_mk2Compute(LiteralDenseState s) {
    BenchmarkSupport.applyFrame(s.mk2, BenchmarkSupport.frame(s.mk2Index++));
    return s.mk2.compute();
}

@Benchmark
public AuditResult<BigDecimal> literalDense_mk2Audit(LiteralDenseState s) {
    BenchmarkSupport.applyFrame(s.mk2, BenchmarkSupport.frame(s.mk2Index++));
    return s.mk2.computeWithAudit();
}
```

---

## 4. Classe de Suporte de Benchmark

Centralize em uma classe utilitária (`*BenchmarkSupport`) toda lógica de construção de expressões e frames. Isso:
- Evita duplicação entre o benchmark de comparação e benchmarks unitários existentes.
- Garante que ambos os componentes sejam inicializados com as mesmas expressões e dados.
- Facilita extensão futura (adicionar cenários sem duplicar lógica de setup).

A classe deve expor apenas métodos estáticos e ser `final` com construtor privado.

```java
public final class BenchmarkSupport {
    private static final int FRAME_COUNT = 256;
    public static final String EXPRESSION_A = "…";

    public static ComponentA newLegacyForA() { … }
    public static ComponentB newMk2ForA() { … }

    public static Frame frame(int index) {
        return FRAMES[index & (FRAME_COUNT - 1)];
    }

    public static void applyFrame(ComponentA c, Frame f) { … }
    public static void applyFrame(ComponentB c, Frame f) { … }
}
```

---

## 5. Runner para Execução Rápida

A classe de benchmark com `@Fork(3)` e 15 iterações leva vários minutos para um conjunto grande de benchmarks. Para ciclos de iteração rápida (verificação, CI informal), crie uma classe `*BenchmarkMain` que usa o `OptionsBuilder` do JMH com configuração reduzida:

```java
Options opts = new OptionsBuilder()
    .include(MyComparisonBenchmark.class.getSimpleName())
    .warmupIterations(3)
    .warmupTime(TimeValue.milliseconds(500))
    .measurementIterations(5)
    .measurementTime(TimeValue.milliseconds(500))
    .forks(0)                        // in-process: resultados indicativos, não de produção
    .addProfiler(GCProfiler.class)   // captura B/op via gc.alloc.rate.norm
    .resultFormat(ResultFormatType.TEXT)
    .result("/tmp/benchmark-results.txt")
    .build();

new Runner(opts).run();
```

**`forks(0)` vs `forks(N)`**: `forks(0)` executa no mesmo JVM do host (ex.: Maven), o que contamina o estado de JIT e GC. Use-o apenas para verificar que o benchmark compila e roda, e para obter direções aproximadas. Para resultados de registro, use sempre `forks >= 1` (isolamento de JVM) executando o benchmark standalone ou via fat jar.

**Como rodar via Maven exec:**
```shell
mvn exec:java \
  -pl <modulo> \
  -Dexec.mainClass=<pacote>.MyBenchmarkMain \
  -Dexec.classpathScope=test
```

---

## 6. Coleta de Métricas

### 6.1. Métricas obrigatórias

| Métrica | Fonte JMH | O que mede |
|---|---|---|
| `ns/op` | `AverageTime` | Latência média por operação |
| `B/op` | `gc.alloc.rate.norm` via GCProfiler | Bytes alocados por operação (pressão no GC) |

### 6.2. Métricas opcionais

| Métrica | Quando usar |
|---|---|
| `ops/s` (`Throughput`) | Quando o objetivo é maximizar taxa, não minimizar latência |
| `p99 ns/op` | Quando latência de cauda é relevante (ex.: SLAs) |
| `gc.count`, `gc.time` | Para detectar pausas de GC que inflam ns/op |

### 6.3. Interpretação de `gc.alloc.rate.norm`

`gc.alloc.rate.norm` é a métrica mais estável para alocação: expressa bytes alocados **por operação**, independente da frequência de invocação. Valores fixos (sem variação entre iterações) indicam caminhos alocativos determinísticos — qualquer variação >1% é sinal de alocação condicional (ex.: cache miss, lazy init).

---

## 7. Apresentação dos Resultados

### 7.1. Estrutura dos quadros comparativos

Organize um quadro por **grupo de comparação** (não por cenário). Cada linha é um cenário; as colunas são: componente A (baseline), componente B (novo), delta de tempo e delta de alocação.

**Template de quadro:**

```
## Grupo N — ComponenteA.método() vs ComponenteB.método()

| Cenário        | A ns/op | B ns/op | Δ tempo | A B/op  | B B/op  | Δ alocação |
|---|---:|---:|:---:|---:|---:|:---:|
| literalDense   | 5 416   | 2 092   | −61% ✅  | 9 792   | 3 632   | −63% ✅     |
| variableChurn  | 2 113   | 1 536   | −27% ✅  | 2 609   | 1 064   | −59% ✅     |
| …              | …       | …       | …        | …       | …       | …           |
```

**Regras de formatação:**
- Arredonde ns/op para números inteiros e separe milhares com espaço (ex.: `1 536`).
- Expresse Δ como percentual, do ponto de vista do componente B em relação ao A: `(B − A) / A × 100%`.
- Use ✅ quando B melhora em relação a A, ⚠️ para variação marginal (±10–20%), ❌ quando A vence com folga (>20%).
- Mostre **ambas** as colunas delta (tempo e alocação) juntas: uma melhora sem a outra é informação incompleta.

### 7.2. Seção de interpretação

Após os quadros, inclua uma seção narrativa que responda:

1. **Onde o componente novo domina** — e por quê (ex.: "o plano compilado elimina parse por chamada").
2. **Onde o componente legado mantém vantagem** — e por quê (ex.: "overhead de despacho fixo pesa mais que o custo da operação em sub-microsegundo").
3. **Overhead por modo de operação** — compare `compute()` vs `computeWithAudit()` linha a linha para quantificar o custo do modo mais rico.
4. **Ressalvas metodológicas** — `forks=0`, ruído de GC, diferenças de precisão matemática ou de contexto que afetam resultados específicos.

### 7.3. Análise de overhead de modo

Quando há múltiplos modos no componente novo (ex.: `compute()` vs `computeWithAudit()`), produza uma tabela auxiliar de overhead:

```
| Cenário       | compute() ns/op | audit() ns/op | Δ tempo audit | compute() B/op | audit() B/op | Δ aloc audit |
|---|---:|---:|:---:|---:|---:|:---:|
| literalDense  | 2 092           | 2 259         | +8%           | 3 632          | 3 832        | +5.5%        |
| variableChurn | 1 536           | 1 886         | +23%          | 1 064          | 1 680        | +58%         |
```

Isso permite ao leitor entender o custo **incremental** de cada modo separadamente do custo absoluto do componente.

---

## 8. Armadilhas Comuns

### 8.1. Comparar modos não equivalentes

Se um componente retorna um `AuditResult<T>` (com metadados) e o outro retorna apenas `T`, a comparação direta é válida apenas se o custo dos metadados for irrelevante para o uso que se quer comparar. Documente isso explicitamente.

### 8.2. Não isolar o setup do caminho quente

Toda lógica de inicialização (compilação de expressão, registro de funções, construção de frames) deve estar em `@Setup(Level.Trial)`, nunca dentro do método de benchmark. O JMH mede apenas o corpo do método anotado com `@Benchmark`.

### 8.3. Precisão matemática diferente entre componentes

Se os componentes usam `MathContext` diferente (ex.: `DECIMAL128` vs `DECIMAL64` vs padrão `BigDecimal`), o tempo de cálculo de transcendentais pode variar por um fator de 2–5×. Isso não é custo do pipeline, mas da precisão. Documente qual contexto cada componente usa e considere parametrizar o benchmark por `MathContext` se a comparação for sensível a isso.

### 8.4. Resultados com alta variância

Variância alta (`Error` > 20% do `Score` no JMH) indica instabilidade de medição. Causas comuns:
- `forks=0`: JVM compartilhada com Maven ou outro processo.
- GC em curso durante medição: verifique `gc.count` elevado nas iterações de medição.
- JIT ainda convergindo: aumente `warmupIterations` ou `warmupTime`.

Resultados com alta variância devem ser marcados com ⚠️ nos quadros e exigem repetição com `forks >= 3`.

### 8.5. Dead-code elimination

O método de benchmark **deve retornar** o resultado da operação. Não capture em variável local sem retornar; o JIT pode eliminar o cálculo inteiro. Se o método não tem retorno natural, use o `Blackhole` do JMH:

```java
@Benchmark
public void myBenchmark(MyState s, Blackhole bh) {
    bh.consume(s.component.evaluate());
}
```

---

## 9. Checklist de Reprodutibilidade

Antes de publicar resultados, verifique:

- [ ] Expressões de teste idênticas para ambos os componentes (ou diferenças documentadas).
- [ ] Mesmo pool de frames, com seeds documentadas.
- [ ] `@Setup(Level.Trial)` para toda inicialização.
- [ ] Cada benchmark retorna um valor (sem void).
- [ ] JVM idêntica (`-Xms` = `-Xmx`, mesmo JDK, mesmos flags) para ambos os componentes.
- [ ] `forks >= 1` para resultados de registro (não usar `forks=0`).
- [ ] GCProfiler ativo para capturar `B/op`.
- [ ] Quadros organizados por grupo de comparação (não por cenário).
- [ ] Seção de interpretação cobre onde cada componente vence e por quê.
- [ ] Ressalvas metodológicas documentadas (precisão, modo de execução, versão JDK).

---

## 10. Referências Internas

- `CrossModuleComparisonBenchmark.java` — benchmark de comparação entre `expression-evaluator` e `exp-eval-mk2`, com 6 cenários e 3 variantes por cenário (legacy, mk2Compute, mk2Audit).
- `CrossModuleExpressionBenchmarkSupport.java` — suporte compartilhado: expressões, frames de variáveis, construção de componentes.
- `CrossModuleComparisonBenchmarkMain.java` — runner de execução rápida (`forks=0`, iterações reduzidas).
- `docs/perf/performance-history.md` — histórico de decisões de desempenho com formato PERF-NNN.
- `docs/runtime-internals.md` — detalhes do pipeline de compilação do exp-eval-mk2.
